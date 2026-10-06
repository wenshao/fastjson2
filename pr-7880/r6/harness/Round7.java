package verify;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static verify.Verify7880.expect;

/**
 * Round-6 additions for 82514ab10 (section J): writer creation under the provider lock vs class initializers that
 * serialize, and map keys written outside the caller's context. Uses only API present on main.
 */
public class Round7 {
    static void run() throws Exception {
        System.out.println("[J1] writer creation vs a class initializer that serializes (default path, no features)");
        String[] c = creationDeadlockCtor();
        expect("J1", "creator's default-value constructor waits for a serializing initializer", c[0].equals("completed"), c[1]);
        String[] e = creationDeadlockEnum();
        expect("J1", "creator's enum field writer waits for a serializing enum initializer", e[0].equals("completed"), e[1]);
        System.out.println();

        System.out.println("[J2] non-String map keys and the caller's context (date format, filters)");
        String[] d = dateKeys();
        expect("J2", "Date/LocalDate/LocalDateTime keys keep the context date format when a filter is set", d[0].equals(d[1]), "with filter " + d[1] + "  without " + d[0]);
        String k = beanKeyMasked();
        expect("J2", "context ValueFilter applies to a bean used as a map key", k.contains("***") && !k.contains("hunter2"), k);
        System.out.println();
    }

    static void await(CountDownLatch l) {
        try {
            l.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void awaitBlockedOrDone(Thread t) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline && t.isAlive() && t.getState() != Thread.State.BLOCKED) {
            Thread.yield();
        }
    }

    // ---- J1 constructor variant: the creator instantiates the bean for default values
    static final ObjectWriterProvider CTOR_PROVIDER = new ObjectWriterProvider();
    static final CountDownLatch CTOR_A_STARTED = new CountDownLatch(1);
    static final CountDownLatch CTOR_B_IN_CREATOR = new CountDownLatch(1);
    static volatile Thread ctorA;

    public static class Meta {
        public String name = "meta";
    }

    public static class Registry {
        static final String DESCRIPTION;

        static {
            CTOR_A_STARTED.countDown();
            await(CTOR_B_IN_CREATOR);
            DESCRIPTION = JSON.toJSONString(new Meta(), new JSONWriter.Context(CTOR_PROVIDER));
        }
    }

    public static class Order {
        static final AtomicInteger CONSTRUCTED = new AtomicInteger();
        public int id = 1;
        public String registry;

        public Order() {
            if (CONSTRUCTED.incrementAndGet() == 2) {
                CTOR_B_IN_CREATOR.countDown();
                awaitBlockedOrDone(ctorA);
                registry = Registry.DESCRIPTION;
            }
        }
    }

    /** Returns {"completed" | "HUNG", detail}. */
    public static String[] creationDeadlockCtor() throws InterruptedException {
        Thread a = new Thread(() -> {
            Object o = Registry.DESCRIPTION;
        }, "class-initializer");
        a.setDaemon(true);
        ctorA = a;
        a.start();
        await(CTOR_A_STARTED);
        String[] out = new String[1];
        Thread b = new Thread(() -> out[0] = JSON.toJSONString(new Order(), new JSONWriter.Context(CTOR_PROVIDER)), "first-write");
        b.setDaemon(true);
        b.start();
        a.join(10_000);
        b.join(10_000);
        return verdict(a, b, out[0]);
    }

    // ---- J1 enum variant: FieldWriterEnum calls getEnumConstants()
    static final ObjectWriterProvider ENUM_PROVIDER = new ObjectWriterProvider();
    static final CountDownLatch ENUM_A_STARTED = new CountDownLatch(1);
    static final CountDownLatch ENUM_B_SERIALIZING = new CountDownLatch(1);

    public static class Meta2 {
        public String name = "meta2";
    }

    public enum Color {
        RED;
        static final String DESCRIPTION;

        static {
            ENUM_A_STARTED.countDown();
            await(ENUM_B_SERIALIZING);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            DESCRIPTION = JSON.toJSONString(new Meta2(), new JSONWriter.Context(ENUM_PROVIDER));
        }
    }

    public static class Paint {
        public int id = 1;
        public Color color;
    }

    public static String[] creationDeadlockEnum() throws InterruptedException {
        Thread a = new Thread(() -> {
            Object o = Color.DESCRIPTION;
        }, "enum-initializer");
        a.setDaemon(true);
        a.start();
        await(ENUM_A_STARTED);
        String[] out = new String[1];
        Thread b = new Thread(() -> {
            Paint p = new Paint();
            ENUM_B_SERIALIZING.countDown();
            out[0] = JSON.toJSONString(p, new JSONWriter.Context(ENUM_PROVIDER));
        }, "first-write");
        b.setDaemon(true);
        b.start();
        a.join(10_000);
        b.join(10_000);
        return verdict(a, b, out[0]);
    }

    static String[] verdict(Thread a, Thread b, String out) {
        if (!a.isAlive() && !b.isAlive()) {
            return new String[]{"completed", "both threads finished; first write = " + out};
        }
        return new String[]{"HUNG", "after 10 s: " + a.getName() + " " + a.getState() + ", " + b.getName() + " " + b.getState()
                + " (the initializer waits for the provider lock, the lock holder waits for the initializer)"};
    }

    // ---- J2
    public static class Dated {
        public Map<Date, String> byDate = new LinkedHashMap<>(Collections.singletonMap(new Date(86_400_000L), "a"));
        public Map<LocalDate, String> byLocalDate = new LinkedHashMap<>(Collections.singletonMap(LocalDate.of(2026, 10, 6), "b"));
        public Map<LocalDateTime, String> byLocalDateTime = new LinkedHashMap<>(Collections.singletonMap(LocalDateTime.of(2026, 10, 6, 1, 2, 3), "c"));
    }

    /** Returns {without filter, with a no-op filter}. */
    public static String[] dateKeys() {
        JSONWriter.Context plain = new JSONWriter.Context();
        plain.setDateFormat("yyyy/MM/dd");
        JSONWriter.Context filtered = new JSONWriter.Context();
        filtered.setDateFormat("yyyy/MM/dd");
        filtered.configFilter((PropertyFilter) (o, n, v) -> true);
        return new String[]{JSON.toJSONString(new Dated(), plain), JSON.toJSONString(new Dated(), filtered)};
    }

    public static class Secret {
        public String password = "hunter2";
        public String user = "u";
    }

    public static class KeyedBySecret {
        public Map<Secret, String> m = new LinkedHashMap<>(Collections.singletonMap(new Secret(), "v"));
    }

    public static String beanKeyMasked() {
        JSONWriter.Context masked = new JSONWriter.Context();
        masked.configFilter((ValueFilter) (o, n, v) -> "password".equals(n) ? "***" : v);
        return JSON.toJSONString(new KeyedBySecret(), masked);
    }
}
