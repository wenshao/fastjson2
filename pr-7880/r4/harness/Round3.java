package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.ToLongFunction;

import static verify.Verify7880.SORTED;
import static verify.Verify7880.expect;
import static verify.Verify7880.record;

/**
 * Round-3 additions: checks for the behavior introduced or changed between 51643676c and f382fe908.
 */
public class Round3 {
    static final JSONReader.Feature DUP = JSONReader.Feature.valueOf("ErrorOnDuplicateKeys");

    static void run() throws Exception {
        registeredAdapters();
        writerGates();
        readObjectLong();
        duplicateKeys();
        deepCopy();
        treeConversion();
        rootWriters();
        loaderLeakViaHolder();
    }

    // ------------------------------------------------------------------ F1 registered adapters
    public static class Account {
        public long id = 7;
        public String name = "alice";
        public String password = "hunter2";
    }

    public static class AccountM {
        public long id = 7;
        public String name = "alice";
        public String password = "hunter2";
    }

    public static class AccountHolder {
        public AccountM account = new AccountM();
    }

    public static class AccountP {
        public long id = 7;
        public String name = "alice";
        public String password = "hunter2";
    }

    static String jsonb(Object o, JSONWriter.Context ctx) {
        return JSON.toJSONString(JSONB.parse(JSONB.toBytes(o, ctx)));
    }

    static String jsonb(Object o, JSONWriter.Feature... f) {
        return JSON.toJSONString(JSONB.parse(JSONB.toBytes(o, f)));
    }

    static void registeredAdapters() {
        System.out.println("[F1] writers registered via ObjectWriters.objectWriter(...) under the feature");
        // omits password
        ToLongFunction<Account> id = a -> a.id;
        ObjectWriter omit = ObjectWriters.objectWriter(Account.class,
                ObjectWriters.fieldWriter("name", String.class, (Account a) -> a.name),
                ObjectWriters.fieldWriter("id", id));
        JSON.register(Account.class, omit);
        try {
            String natural = JSON.toJSONString(new Account());
            expect("F1", "omitting adapter: natural", !natural.contains("hunter2"), natural);
            String[][] sorted = {
                    {"toJSONString", JSON.toJSONString(new Account(), SORTED)},
                    {"toJSONBytes", new String(JSON.toJSONBytes(new Account(), SORTED), StandardCharsets.UTF_8)},
                    {"JSONB", jsonb(new Account(), SORTED)},
                    {"JSONObject.from", String.valueOf(JSONObject.from(new Account(), SORTED))},
            };
            for (String[] r : sorted) {
                expect("F1", "omitting adapter WITH feature: " + r[0], !r[1].contains("hunter2"), r[1]);
            }
            record("INFO", "F1", "omitting adapter WITH feature: key order", sorted[0][1]);
        } finally {
            JSON.register(Account.class, (ObjectWriter) null);
        }

        // masks password, used as a nested field, registerIfAbsent
        ObjectWriter mask = ObjectWriters.objectWriter(AccountM.class,
                ObjectWriters.fieldWriter("name", String.class, (AccountM a) -> a.name),
                ObjectWriters.fieldWriter("password", String.class, (AccountM a) -> "***"));
        JSON.registerIfAbsent(AccountM.class, mask);
        try {
            String natural = JSON.toJSONString(new AccountHolder());
            String sorted = JSON.toJSONString(new AccountHolder(), SORTED);
            String sortedJsonb = jsonb(new AccountHolder(), SORTED);
            expect("F1", "masking adapter (field): natural", natural.contains("***") && !natural.contains("hunter2"), natural);
            expect("F1", "masking adapter (field) WITH feature", sorted.contains("***") && !sorted.contains("hunter2"), sorted);
            expect("F1", "masking adapter (field) WITH feature: JSONB", sortedJsonb.contains("***") && !sortedJsonb.contains("hunter2"), sortedJsonb);
        } finally {
            JSON.register(AccountM.class, (ObjectWriter) null);
        }

        // custom provider, register before and after warm-up
        ObjectWriterProvider p = new ObjectWriterProvider();
        JSON.toJSONString(new AccountP(), new JSONWriter.Context(p, SORTED));
        p.register(AccountP.class, ObjectWriters.objectWriter(AccountP.class,
                ObjectWriters.fieldWriter("name", String.class, (AccountP a) -> a.name)));
        String s = JSON.toJSONString(new AccountP(), new JSONWriter.Context(p, SORTED));
        expect("F1", "custom provider, register after warm-up WITH feature", "{\"name\":\"alice\"}".equals(s), s);
        p.unregister(AccountP.class);
        String back = JSON.toJSONString(new AccountP(), new JSONWriter.Context(p, SORTED));
        expect("F1", "unregister() restores the created sorted writer", back.contains("hunter2") && back.startsWith("{\"id\""), back);
        System.out.println();
    }

    // ------------------------------------------------------------------ F2 field-writer gates
    public static class Secret {
        public String value = "sensitive";
    }

    public static class MaskWriter implements ObjectWriter<Secret> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString("MASKED");
        }
    }

    public static class WU {
        @JSONField(writeUsing = MaskWriter.class)
        public Secret secret = new Secret();
    }

    public static class DatesBean {
        @JSONField(format = "yyyy-MM-dd")
        public List<Date> dates = Collections.singletonList(new Date(0L));
    }

    @JSONType(alphabetic = false)
    public static class Item {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class ContentAsA {
        @JSONField(contentAs = Item.class)
        public List<Object> items = new ArrayList<>(Collections.singletonList(new Item()));
    }

    public static class ContentAsB {
        @JSONField(contentAs = Item.class)
        public List<Object> items = new ArrayList<>(Collections.singletonList(new Item()));
    }

    public static class ListA {
        public List<Item> items = new ArrayList<>(Collections.singletonList(new Item()));
    }

    public static class Inner3 {
        private int hidden = 42;
        private String name = "n";
    }

    public static class Inner3$$EnhancerBySpringCGLIB$$r3 extends Inner3 {
    }

    @JSONType(alphabetic = false)
    public static class Positional {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    @JSONType(alphabetic = false)
    public static final class PositionalFinal {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    public static class ColdHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Positional t = new Positional();
    }

    public static class ColdHolderFinal {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public PositionalFinal t = new PositionalFinal();
    }

    static void writerGates() {
        System.out.println("[F2] field-writer gates in the sorted path");
        String wn = JSON.toJSONString(new WU());
        String ws = JSON.toJSONString(new WU(), SORTED);
        String wb = jsonb(new WU(), SORTED);
        expect("F2", "writeUsing honored WITH feature", "{\"secret\":\"MASKED\"}".equals(ws), "natural=" + wn + " sorted=" + ws);
        expect("F2", "writeUsing honored WITH feature: JSONB", wb.contains("MASKED"), wb);

        String dn = JSON.toJSONString(new DatesBean());
        String ds = JSON.toJSONString(new DatesBean(), SORTED);
        expect("F2", "List<Date> item format kept WITH feature", dn.equals(ds) && ds.contains("1970-01-01"), "natural=" + dn + " sorted=" + ds);

        String an = JSON.toJSONString(new ContentAsA());
        String as = JSON.toJSONString(new ContentAsA(), SORTED);
        String an2 = JSON.toJSONString(new ContentAsA());
        String bs = JSON.toJSONString(new ContentAsB(), SORTED);
        String bn = JSON.toJSONString(new ContentAsB());
        expect("F2", "contentAs: natural -> sorted -> natural",
                an.equals("{\"items\":[{\"zebra\":3,\"apple\":1}]}") && as.equals("{\"items\":[{\"apple\":1,\"zebra\":3}]}") && an2.equals(an),
                an + " | " + as + " | " + an2);
        expect("F2", "contentAs: sorted -> natural", bs.equals(as) && bn.equals(an), bs + " | " + bn);

        String ln = JSON.toJSONString(new ListA());
        String ls = JSON.toJSONString(new ListA(), SORTED);
        String lb = jsonb(new ListA(), SORTED);
        String ln2 = JSON.toJSONString(new ListA());
        expect("F2", "List<Item>: natural -> sorted (text, JSONB) -> natural",
                ls.equals("{\"items\":[{\"apple\":1,\"zebra\":3}]}") && lb.equals(ls) && ln2.equals(ln), ln + " | " + ls + " | " + lb + " | " + ln2);

        // field-level BeanToArray when the first write on a provider is a sorted one
        for (Object holder : new Object[]{new ColdHolder(), new ColdHolderFinal()}) {
            ObjectWriterProvider cold = Verify7880.newProvider(System.getProperty("fastjson2.creator", "asm"));
            String sortedFirst = JSON.toJSONString(holder, new JSONWriter.Context(cold, SORTED));
            ObjectWriterProvider cold2 = Verify7880.newProvider(System.getProperty("fastjson2.creator", "asm"));
            String sortedFirstJsonb = JSON.toJSONString(JSONB.parse(JSONB.toBytes(holder, new JSONWriter.Context(cold2, SORTED))));
            String natural = JSON.toJSONString(holder, new JSONWriter.Context(cold));
            String k = holder.getClass().getSimpleName();
            expect("F2", "field BeanToArray, sorted first on a cold provider: " + k, "{\"t\":[1001,2002,300]}".equals(sortedFirst), sortedFirst);
            expect("F2", "field BeanToArray, sorted first on a cold provider: " + k + " JSONB", "{\"t\":[1001,2002,300]}".equals(sortedFirstJsonb), sortedFirstJsonb);
            expect("F2", "field BeanToArray, natural after a cold sorted write: " + k, "{\"t\":[1001,2002,300]}".equals(natural), natural);
        }

        ObjectWriterProvider provider = new ObjectWriterProvider();
        JSONWriter.Context fb = new JSONWriter.Context(provider, JSONWriter.Feature.FieldBased);
        String plain = JSON.toJSONString(new Inner3(), fb);
        String proxy = JSON.toJSONString(new Inner3$$EnhancerBySpringCGLIB$$r3(), fb);
        expect("F2", "FieldBased proxy reuses the target's field-based writer", plain.equals(proxy) && proxy.contains("hidden"), "plain=" + plain + " proxy=" + proxy);
        JSONWriter.Context fbs = new JSONWriter.Context(provider, JSONWriter.Feature.FieldBased, SORTED);
        String plainS = JSON.toJSONString(new Inner3(), fbs);
        String proxyS = JSON.toJSONString(new Inner3$$EnhancerBySpringCGLIB$$r3(), fbs);
        expect("F2", "FieldBased proxy WITH feature", plainS.equals(proxyS) && proxyS.contains("hidden"), "plain=" + plainS + " proxy=" + proxyS);
        System.out.println();
    }

    // ------------------------------------------------------------------ F3 JSONReader.readObject(long)
    static void readObjectLong() throws Exception {
        System.out.println("[F3] JSONReader#readObject(long)");
        Method m;
        try {
            m = JSONReader.class.getMethod("readObject", long.class);
        } catch (NoSuchMethodException e) {
            record("INFO", "F3", "readObject(long) not public on this build", "");
            System.out.println();
            return;
        }
        record("INFO", "F3", "readObject(long) is public", m.toString());
        byte[] jb = JSONB.toBytes(JSONObject.of("a", 1, "b", JSONObject.of("c", 2)));
        String expected = String.valueOf(JSONReader.ofJSONB(jb).readObject());
        String got;
        try {
            got = String.valueOf(m.invoke(JSONReader.ofJSONB(jb), 0L));
        } catch (java.lang.reflect.InvocationTargetException e) {
            got = "EXC " + e.getCause();
        }
        expect("F3", "JSONB reader: readObject(0L) == readObject()", expected.equals(got), "readObject()=" + expected + " readObject(0L)=" + got);
        String text = String.valueOf(m.invoke(JSONReader.of("{\"a\":1,\"b\":{\"c\":2}}"), 0L));
        expect("F3", "text reader: readObject(0L)", "{a=1, b={c=2}}".equals(text) || "{\"a\":1,\"b\":{\"c\":2}}".equals(text), text);
        String strict;
        try {
            strict = String.valueOf(m.invoke(JSONReader.of("{\"x\":{\"a\":1,\"a\":2}}"), DUP.mask));
        } catch (java.lang.reflect.InvocationTargetException e) {
            strict = "EXC " + e.getCause().getMessage();
        }
        expect("F3", "text reader: readObject(DUP.mask) rejects nested duplicate", strict.startsWith("EXC") && strict.contains("duplicate key"), strict);
        System.out.println();
    }

    // ------------------------------------------------------------------ F4 duplicate keys
    public static class DObj {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Object payload;
    }

    public static class DMap {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Object> payload;
    }

    public static class DJo {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public JSONObject payload;
    }

    public static class DList {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public List<Object> payload;
    }

    public static class DMapMap {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Map<String, Object>> payload;
    }

    interface Call {
        Object call() throws Exception;
    }

    static String rejects(Call c) {
        try {
            Object o = c.call();
            return "accepted " + (o instanceof Map ? JSON.toJSONString(o) : String.valueOf(o));
        } catch (Throwable t) {
            Throwable leaf = t;
            while (leaf.getCause() != null && leaf.getCause() != leaf) {
                leaf = leaf.getCause();
            }
            String m = String.valueOf(leaf.getMessage());
            return m.contains("duplicate key") ? "rejected" : "EXC " + leaf.getClass().getSimpleName() + ": " + m;
        }
    }

    static void duplicateKeys() {
        System.out.println("[F4] ErrorOnDuplicateKeys: seen-set semantics and propagation");
        // no false positives from the destination map
        String pre = rejects(() -> {
            Map<String, Object> m = new HashMap<>();
            m.put("a", 0);
            JSONReader r = JSONReader.of("{\"a\":1}", JSONFactory.createReadContext(DUP));
            r.read(m, 0L);
            return m;
        });
        expect("F4", "read(Map) into a pre-seeded map: accepted", pre.startsWith("accepted"), pre);
        String sup = rejects(() -> {
            JSONReader.Context ctx = JSONFactory.createReadContext(DUP);
            ctx.setObjectSupplier(() -> {
                Map<String, Object> m = new HashMap<>();
                m.put("a", 0);
                return m;
            });
            return JSON.parseObject("{\"a\":1}", Object.class, ctx);
        });
        expect("F4", "pre-seeded objectSupplier: accepted", sup.startsWith("accepted"), sup);

        String[][] strict = {
                {"{\"a\":null,\"a\":1}", "null then value + IgnoreNullPropertyValue"},
                {"{\"a\":1,\"a\":null}", "value then null + IgnoreNullPropertyValue"},
                {"{\"a\":1,\"a\":{\"$ref\":\"$\"}}", "$ref as the duplicate value"},
                {"{1:1,\"1\":2}", "numeric and string forms of one key"},
        };
        for (String[] c : strict) {
            String r1 = rejects(() -> JSON.parseObject(c[0], DUP, JSONReader.Feature.IgnoreNullPropertyValue));
            String r2 = rejects(() -> JSON.parseObject(c[0], LinkedHashMap.class, DUP, JSONReader.Feature.IgnoreNullPropertyValue));
            expect("F4", "rejects " + c[1], "rejected".equals(r1) && "rejected".equals(r2), c[0] + " JSONObject=" + r1 + " LinkedHashMap=" + r2);
        }
        String ign = rejects(() -> JSON.parseObject("{\"a\":null,\"b\":1}", DUP, JSONReader.Feature.IgnoreNullPropertyValue));
        expect("F4", "IgnoreNullPropertyValue still skips nulls under the feature", "accepted {\"b\":1}".equals(ign), ign);

        // annotation-driven, below level 1
        Class<?>[] holders = {DObj.class, DMap.class, DJo.class, DMapMap.class};
        String[] nested = {"{\"payload\":{\"x\":{\"z\":1,\"z\":2}}}", "{\"payload\":{\"x\":{\"y\":{\"z\":1,\"z\":2}}}}"};
        for (Class<?> h : holders) {
            for (String in : nested) {
                String r = rejects(() -> JSON.parseObject(in, h));
                expect("F4", "@JSONField strict, nested object: " + h.getSimpleName() + " depth " + (in.contains("\"y\"") ? 3 : 2), "rejected".equals(r), in + " -> " + r);
            }
        }
        // annotation-driven, object inside an array (per-call strictness rejects these)
        String[][] viaArray = {
                {"DObj", "{\"payload\":{\"x\":[{\"z\":1,\"z\":2}]}}"},
                {"DMap", "{\"payload\":{\"x\":[{\"z\":1,\"z\":2}]}}"},
                {"DJo", "{\"payload\":{\"x\":[{\"z\":1,\"z\":2}]}}"},
                {"DObj", "{\"payload\":[{\"z\":1,\"z\":2}]}"},
                {"DList", "{\"payload\":[{\"z\":1,\"z\":2}]}"},
        };
        Map<String, Class<?>> byName = new HashMap<>();
        byName.put("DObj", DObj.class);
        byName.put("DMap", DMap.class);
        byName.put("DJo", DJo.class);
        byName.put("DList", DList.class);
        for (String[] c : viaArray) {
            String perCall = rejects(() -> JSON.parseObject(c[1], DUP));
            String ann = rejects(() -> JSON.parseObject(c[1], byName.get(c[0])));
            record("rejected".equals(ann) ? "PASS" : "DIFF", "F4", "@JSONField strict, object inside array: " + c[0] + (c[1].startsWith("{\"payload\":[") ? " [..]" : " {x:[..]}"),
                    c[1] + " -> annotation " + ann + ", per-call " + perCall);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------ F5 deepCopy
    static void deepCopy() {
        System.out.println("[F5] deepCopy on cyclic and shared trees");
        String r;
        try {
            JSONObject o = JSON.parseObject("{\"a\":{\"$ref\":\"$\"}}");
            JSONObject c = o.deepCopy();
            r = (c != o && c.get("a") == c) ? "ok" : "shape " + (c.get("a") == c);
        } catch (Throwable t) {
            r = t.getClass().getSimpleName();
        }
        expect("F5", "self-cycle via $ref", "ok".equals(r), r);
        try {
            JSONObject o = JSON.parseObject("{\"a\":{\"b\":{\"$ref\":\"$.a\"}}}");
            JSONObject c = o.deepCopy();
            r = (c.getJSONObject("a") != o.getJSONObject("a") && c.getJSONObject("a").getJSONObject("b") == c.getJSONObject("a")) ? "ok" : "shape";
        } catch (Throwable t) {
            r = t.getClass().getSimpleName();
        }
        expect("F5", "nested cycle via $ref", "ok".equals(r), r);
        try {
            JSONArray a = new JSONArray();
            a.add(a);
            JSONArray c = a.deepCopy();
            r = (c != a && c.get(0) == c) ? "ok" : "shape";
        } catch (Throwable t) {
            r = t.getClass().getSimpleName();
        }
        expect("F5", "self-containing array", "ok".equals(r), r);
        JSONObject shared = JSONObject.of("k", 1);
        JSONObject root = new JSONObject();
        root.put("l", shared);
        root.put("r", shared);
        JSONObject c = root.deepCopy();
        record("INFO", "F5", "one instance referenced twice", "copy.l == copy.r: " + (c.get("l") == c.get("r")) + ", copy.l != source: " + (c.get("l") != shared));
        System.out.println();
    }

    // ------------------------------------------------------------------ F6 tree conversion
    public static class NInner {
        public String a;
        public int b = 2;
    }

    public static class NOuter {
        public String top;
        public NInner inner = new NInner();
        public List<NInner> list = Collections.singletonList(new NInner());
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNulls)
    public static class NOuterAnn {
        public String top;
        public List<NInner> list = Collections.singletonList(new NInner());
    }

    @JSONType(alphabetic = false)
    public static class SInner {
        public int zebra = 3;
        public int apple = 1;
    }

    @JSONType(alphabetic = false)
    public static class SOuter {
        public int zulu = 9;
        public SInner inner = new SInner();
        public List<SInner> items = Collections.singletonList(new SInner());
    }

    static void treeConversion() {
        System.out.println("[F6] JSONObject.from / JSON.toJSON match toJSONString");
        String t1 = JSON.toJSONString(new SOuter(), SORTED);
        String f1 = JSONObject.from(new SOuter(), SORTED).toString();
        expect("F6", "from(obj, Sort) == toJSONString(obj, Sort)", t1.equals(f1), "toJSONString=" + t1 + " from=" + f1);

        String t2 = JSON.toJSONString(new NOuter(), JSONWriter.Feature.WriteNulls);
        String f2 = JSON.toJSONString(JSONObject.from(new NOuter(), JSONWriter.Feature.WriteNulls), JSONWriter.Feature.WriteNulls);
        expect("F6", "from(obj, WriteNulls) == toJSONString(obj, WriteNulls)", t2.equals(f2), "toJSONString=" + t2 + " from=" + f2);

        String t3 = JSON.toJSONString(new NOuterAnn());
        String f3 = JSON.toJSONString(JSONObject.from(new NOuterAnn()), JSONWriter.Feature.WriteNulls);
        expect("F6", "from(@JSONType(WriteNulls) obj): type features stay on that type", t3.equals(f3), "toJSONString=" + t3 + " from=" + f3);
        System.out.println();
    }

    // ------------------------------------------------------------------ F7 root writers
    public static class Priv {
        private int x = 1;
    }

    static void rootWriters() {
        System.out.println("[F7] root-writer resolution in JSON.toJSONBytes overloads");
        String s = JSON.toJSONString(new SOuter(), SORTED);
        String b1 = new String(JSON.toJSONBytes(new SOuter(), StandardCharsets.UTF_8, SORTED), StandardCharsets.UTF_8);
        String b2 = new String(JSON.toJSONBytes(new SOuter(), StandardCharsets.UTF_8, new JSONWriter.Context(SORTED)), StandardCharsets.UTF_8);
        String b3 = new String(JSON.toJSONBytes(new SOuter(), SORTED), StandardCharsets.UTF_8);
        expect("F7", "toJSONBytes(charset, Sort) == toJSONString(Sort)", s.equals(b1), b1);
        expect("F7", "toJSONBytes(charset, Context(Sort)) == toJSONString(Sort)", s.equals(b2), b2);
        expect("F7", "toJSONBytes(Sort) == toJSONString(Sort)", s.equals(b3), b3);
        String fs = JSON.toJSONString(new Priv(), JSONWriter.Feature.FieldBased);
        String fb = new String(JSON.toJSONBytes(new Priv(), StandardCharsets.UTF_8, JSONWriter.Feature.FieldBased), StandardCharsets.UTF_8);
        record("INFO", "F7", "toJSONBytes(charset, FieldBased) (feature off)", "toJSONString=" + fs + " toJSONBytes(charset)=" + fb);
        System.out.println();
    }

    // ------------------------------------------------------------------ F8 cleanup(ClassLoader) through a parent-loader holder
    public static class Holder {
        public Object value;
    }

    static void loaderLeakViaHolder() throws Exception {
        System.out.println("[F8] cleanup(ClassLoader): sorted writers of a parent-loader holder");
        URL leakDir = new File(System.getProperty("leak.dir", "leak")).toURI().toURL();
        record("INFO", "F8", "natural variant", "a parent-loader holder pins the child loader on main too (pre-existing, not checked here)");
        for (String creator : new String[]{"asm", "reflect"}) {
            ObjectWriterProvider provider = Verify7880.newProvider(creator);
            WeakReference<ClassLoader> ref = holderInIsolatedLoader(leakDir, provider);
            boolean collected = false;
            for (int i = 0; i < 40 && !collected; i++) {
                System.gc();
                Thread.sleep(25);
                collected = ref.get() == null;
            }
            expect("F8", creator + ": sorted + sorted/FieldBased holder", collected, collected ? "loader collected" : "loader still reachable -> leak");
            provider.isAlphabetic();
        }
        System.out.println();
    }

    static WeakReference<ClassLoader> holderInIsolatedLoader(URL dir, ObjectWriterProvider provider) throws Exception {
        URLClassLoader loader = new URLClassLoader(new URL[]{dir}, Round3.class.getClassLoader());
        Class<?> c = loader.loadClass("leak.LeakBean");
        Holder h = new Holder();
        h.value = c.getConstructor().newInstance();
        for (JSONWriter.Feature[] f : new JSONWriter.Feature[][]{{SORTED}, {SORTED, JSONWriter.Feature.FieldBased}}) {
            String json = JSON.toJSONString(h, new JSONWriter.Context(provider, f));
            if (!json.contains("leak")) {
                throw new IllegalStateException(json);
            }
        }
        h.value = null;
        provider.cleanup(loader);
        loader.close();
        return new WeakReference<>(loader);
    }
}
