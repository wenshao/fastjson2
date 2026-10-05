package verify;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.writer.ObjectWriters;
import com.alibaba.fastjson2.JSONReader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Minimal reproductions for the findings on alibaba/fastjson2#7880 (SortFieldNamesAlphabetically).
 */
public class Repro7880 {
    static final JSONWriter.Feature SORTED = JSONWriter.Feature.SortFieldNamesAlphabetically;

    public static class Money {
        public long cents = 123;
    }

    @JSONType(alphabetic = false)
    public static class Transfer {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;

        @Override
        public String toString() {
            return "Transfer{to=" + to + ", from=" + from + ", amount=" + amount + "}";
        }
    }

    @JSONType(alphabetic = false)
    public static class Account {
        public int z = 1;
        public int a = 2;
    }

    public static class Holder {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Transfer transfer = new Transfer();
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public List<Transfer> transfers = new ArrayList<>(Collections.singletonList(new Transfer()));
    }

    public static class Credentials {
        public long id = 7;
        public String name = "alice";
        public String password = "hunter2";
    }

    public static class Profile {
        public Credentials login = new Credentials();
    }

    public static class Row {
        public String note;
        public int rank = 1;
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNulls)
    public static class Report {
        public String title;
        public List<Row> rows = Collections.singletonList(new Row());
    }

    public abstract static class AccountMixIn {
        @JSONField(name = "renamed")
        public int a;
    }

    static void show(String label, Object expected, Object actual) {
        boolean ok = String.valueOf(expected).equals(String.valueOf(actual));
        System.out.printf("   %-44s expected %-30s actual %-30s %s%n", label, expected, actual, ok ? "OK" : "<-- MISMATCH");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("PR #7880 repro | java " + System.getProperty("java.version")
                + " | " + new File(JSON.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName());
        System.out.println();

        System.out.println("1) custom ObjectWriter registered via JSON.register() is bypassed under SortFieldNamesAlphabetically");
        JSON.register(Money.class, new ObjectWriter<Money>() {
            @Override
            public void write(JSONWriter w, Object o, Object fieldName, Type fieldType, long features) {
                w.writeString("$" + ((Money) o).cents / 100.0);
            }
        });
        show("JSON.toJSONString(money)", "\"$1.23\"", JSON.toJSONString(new Money()));
        show("JSON.toJSONString(money, Sort...)", "\"$1.23\"", JSON.toJSONString(new Money(), SORTED));
        System.out.println();

        System.out.println("2) per-call BeanToArray + SortFieldNamesAlphabetically reorders the positional array");
        Transfer t = new Transfer();
        String positional = JSON.toJSONString(t, JSONWriter.Feature.BeanToArray);
        String sorted = JSON.toJSONString(t, JSONWriter.Feature.BeanToArray, SORTED);
        show("toJSONString(t, BeanToArray)", "[1001,2002,300]", positional);
        show("toJSONString(t, BeanToArray, Sort...)", "[1001,2002,300]", sorted);
        String jsonbPositional = JSONB.toJSONString(JSONB.toBytes(t, JSONWriter.Feature.BeanToArray)).replaceAll("\\s", "");
        String jsonbSorted = JSONB.toJSONString(JSONB.toBytes(t, JSONWriter.Feature.BeanToArray, SORTED)).replaceAll("\\s", "");
        show("JSONB.toBytes(t, BeanToArray, Sort...)", jsonbPositional, jsonbSorted);
        System.out.println();

        System.out.println("3) mixIn() after warm-up is not applied to the sorted writer cache");
        ObjectWriterProvider provider = new ObjectWriterProvider();
        JSON.toJSONString(new Account(), new JSONWriter.Context(provider));
        JSON.toJSONString(new Account(), new JSONWriter.Context(provider, SORTED));
        provider.mixIn(Account.class, AccountMixIn.class);
        show("after mixIn, natural", "{\"z\":1,\"renamed\":2}", JSON.toJSONString(new Account(), new JSONWriter.Context(provider)));
        show("after mixIn, Sort...", "{\"renamed\":2,\"z\":1}", JSON.toJSONString(new Account(), new JSONWriter.Context(provider, SORTED)));
        System.out.println();

        System.out.println("4) cleanup(ClassLoader) does not purge sorted writers -> class loader leak");
        URL leakDir = new File(System.getProperty("leak.dir", "leak")).toURI().toURL();
        for (boolean useSorted : new boolean[]{false, true}) {
            ObjectWriterProvider p = new ObjectWriterProvider();
            WeakReference<ClassLoader> ref = serializeAndCleanup(leakDir, p, useSorted);
            for (int i = 0; i < 40 && ref.get() != null; i++) {
                System.gc();
                Thread.sleep(25);
            }
            show(useSorted ? "loader collected? (Sort...)" : "loader collected? (natural)", "true", ref.get() == null);
            p.isAlphabetic(); // keep provider reachable
        }
        System.out.println();

        System.out.println("5) [round 2] field-level @JSONField(serializeFeatures = BeanToArray) + SortFieldNamesAlphabetically");
        for (boolean asm : new boolean[]{true, false}) {
            String c = asm ? "asm" : "reflect";
            ObjectWriterProvider p1 = asm ? new ObjectWriterProvider() : new ObjectWriterProvider(com.alibaba.fastjson2.writer.ObjectWriterCreator.INSTANCE);
            ObjectWriterProvider p2 = asm ? new ObjectWriterProvider() : new ObjectWriterProvider(com.alibaba.fastjson2.writer.ObjectWriterCreator.INSTANCE);
            show(c + " JSON  field, Sort...", JSON.toJSONString(new Holder(), new JSONWriter.Context(p1)),
                    JSON.toJSONString(new Holder(), new JSONWriter.Context(p2, SORTED)));
            show(c + " JSONB field, Sort...",
                    JSONB.toJSONString(JSONB.toBytes(new Holder(), new JSONWriter.Context(p1))).replaceAll("\\s", ""),
                    JSONB.toJSONString(JSONB.toBytes(new Holder(), new JSONWriter.Context(p2, SORTED))).replaceAll("\\s", ""));
        }
        System.out.println();

        System.out.println("6) [round 2] canConvertToInt/Long on decimals just outside the range (jackson DecimalNode/DoubleNode: false)");
        JSONObject o = JSON.parseObject("{\"a\":2147483647.5,\"b\":-2147483648.5,\"c\":9223372036854775807.5}");
        show("canConvertToInt(2147483647.5)", "false", o.canConvertToInt("a"));
        show("canConvertToInt(-2147483648.5)", "false", o.canConvertToInt("b"));
        show("canConvertToLong(9223372036854775807.5)", "false", o.canConvertToLong("c"));
        System.out.println();

        System.out.println("7) [round 2] canConvertToInt cost on BigDecimal 1E+10000000 carried by an 11-byte JSONB payload");
        byte[] payload = JSONB.toBytes(JSONObject.of("v", new BigDecimal(BigInteger.ONE, -10_000_000)));
        JSONObject huge = JSONB.parseObject(payload);
        ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
            Thread th = new Thread(r);
            th.setDaemon(true);
            return th;
        });
        long t0 = System.nanoTime();
        String res;
        try {
            res = String.valueOf(ex.submit(() -> huge.canConvertToInt("v")).get(30, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            res = "timeout";
        } finally {
            ex.shutdownNow();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("   payload %d bytes -> canConvertToInt = %s in %d ms %s%n", payload.length, res, ms, ms < 50 ? "OK" : "<-- SLOW (jackson DecimalNode: ~0.01 ms)");
        System.out.println();

        System.out.println("8) [round 3] writer registered via ObjectWriters.objectWriter(...) is replaced under SortFieldNamesAlphabetically");
        JSON.register(Credentials.class, ObjectWriters.objectWriter(Credentials.class,
                ObjectWriters.fieldWriter("name", String.class, (Credentials c) -> c.name),
                ObjectWriters.fieldWriter("password", String.class, (Credentials c) -> "***")));
        show("toJSONString(credentials)", "{\"name\":\"alice\",\"password\":\"***\"}", JSON.toJSONString(new Credentials()));
        show("toJSONString(credentials, Sort...)", "{\"name\":\"alice\",\"password\":\"***\"}", JSON.toJSONString(new Credentials(), SORTED));
        show("toJSONString(profile, Sort...)", "{\"login\":{\"name\":\"alice\",\"password\":\"***\"}}", JSON.toJSONString(new Profile(), SORTED));
        System.out.println();

        System.out.println("9) [round 3] new public JSONReader#readObject(long) on a JSONB reader");
        byte[] jsonb = JSONB.toBytes(JSONObject.of("a", 1));
        Object viaNoArg = JSONReader.ofJSONB(jsonb).readObject();
        show("JSONReader.ofJSONB(bytes).readObject()", "{\"a\":1}", viaNoArg);
        String viaLong;
        try {
            viaLong = String.valueOf(JSONReader.class.getMethod("readObject", long.class).invoke(JSONReader.ofJSONB(jsonb), 0L));
        } catch (java.lang.reflect.InvocationTargetException e) {
            viaLong = e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage().replaceAll("[^\\x20-\\x7e]", "").trim();
        }
        show("JSONReader.ofJSONB(bytes).readObject(0L)", viaNoArg, viaLong);
        System.out.println();

        System.out.println("10) [round 3] JSONObject.from: an owner's @JSONType(serializeFeatures = WriteNulls) reaches its items");
        show("toJSONString(report)", "{\"rows\":[{\"rank\":1}],\"title\":null}", JSON.toJSONString(new Report()));
        show("JSONObject.from(report) (printed with WriteNulls)", "{\"rows\":[{\"rank\":1}],\"title\":null}",
                JSON.toJSONString(JSONObject.from(new Report()), JSONWriter.Feature.WriteNulls));
        System.exit(0);
    }

    static WeakReference<ClassLoader> serializeAndCleanup(URL dir, ObjectWriterProvider p, boolean sorted) throws Exception {
        URLClassLoader loader = new URLClassLoader(new URL[]{dir}, Repro7880.class.getClassLoader());
        Object bean = loader.loadClass("leak.LeakBean").getConstructor().newInstance();
        JSON.toJSONString(bean, sorted ? new JSONWriter.Context(p, SORTED) : new JSONWriter.Context(p));
        p.cleanup(loader);
        loader.close();
        return new WeakReference<>(loader);
    }
}
