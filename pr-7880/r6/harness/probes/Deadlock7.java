package p;

import com.alibaba.fastjson2.JSON;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default path, default provider, no features. Thread A initializes a class whose static initializer serializes a
 * bean nobody has serialized before. Thread B serializes a bean whose writer creation runs code that needs that same
 * class: (ctor) the creator instantiates the bean for default values (setDefaultValue) and the constructor touches
 * Registry; (enum) the creator builds a FieldWriterEnum, which calls getEnumConstants() on an enum whose static
 * initializer serializes. The sleeps only fix the order of events; once both threads block, nothing unblocks them.
 */
public class Deadlock7 {
    static final CountDownLatch aStarted = new CountDownLatch(1);
    static final CountDownLatch bInCreator = new CountDownLatch(1);

    static void await(CountDownLatch l) {
        try {
            l.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static class Meta { public String name = "meta"; }

    // ---- variant ctor
    public static class Registry {
        static final String DESCRIPTION;
        static {
            aStarted.countDown();
            await(bInCreator);
            DESCRIPTION = JSON.toJSONString(new Meta());
        }
    }

    public static class Order {
        static final AtomicInteger CONSTRUCTED = new AtomicInteger();
        public int id = 1;
        public String registry;

        public Order() {
            if (CONSTRUCTED.incrementAndGet() == 2) {      // the 2nd instance is the creator's default-value probe
                bInCreator.countDown();
                sleep(300);
                registry = Registry.DESCRIPTION;
            }
        }
    }

    // ---- variant enum
    public static class Meta2 { public String name = "meta2"; }

    public enum Color {
        RED;
        static final String DESCRIPTION;
        static {
            aStarted.countDown();
            await(bInCreator);
            sleep(300);               // B is now inside the creator, building the FieldWriterEnum for Paint.color
            DESCRIPTION = JSON.toJSONString(new Meta2());
        }
    }

    public static class Paint {
        public int id = 1;
        public Color color;

        public Paint() {
            bInCreator.countDown();   // B's own instance; B goes straight on to create Paint's writer
        }
    }

    public static void main(String[] args) throws Exception {
        String variant = args[0];
        Thread a = new Thread(() -> {
            Object o = variant.equals("ctor") ? Registry.DESCRIPTION : Color.DESCRIPTION;
            System.out.println("  A done: " + o);
        }, "A-static-init");
        a.setDaemon(true);
        a.start();
        await(aStarted);
        Thread b = new Thread(() -> {
            String s = variant.equals("ctor") ? JSON.toJSONString(new Order()) : JSON.toJSONString(new Paint());
            System.out.println("  B done: " + s);
        }, "B-serialize");
        b.setDaemon(true);
        b.start();
        a.join(10_000);
        b.join(10_000);
        String jar = JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", "");
        if (a.isAlive() || b.isAlive()) {
            System.out.println(jar + " " + variant + " creator=" + System.getProperty("fastjson2.creator", "asm") + ": HUNG after 10 s");
            for (Thread t : new Thread[]{a, b}) {
                ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(t.getId(), 40);
                System.out.println("  " + t.getName() + " " + info.getThreadState()
                        + (info.getLockName() != null ? " on " + info.getLockName().replaceAll("@[0-9a-f]+", "") : "")
                        + (info.getLockOwnerName() != null ? " owned by " + info.getLockOwnerName() : ""));
                int shown = 0;
                for (StackTraceElement e : t.getStackTrace()) {
                    String c = e.getClassName();
                    if (c.startsWith("com.alibaba.fastjson2") || c.startsWith("p.")) {
                        System.out.println("      at " + e);
                        if (++shown == 6) {
                            break;
                        }
                    }
                }
            }
            System.exit(2);
        }
        System.out.println(jar + " " + variant + " creator=" + System.getProperty("fastjson2.creator", "asm") + ": completed");
    }
}
