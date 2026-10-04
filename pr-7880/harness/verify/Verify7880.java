package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Independent verification harness for alibaba/fastjson2#7880.
 * Runs against a fastjson2 jar on the classpath; jackson-databind 2.17.2 is used as the reference oracle.
 */
public class Verify7880 {
    // ------------------------------------------------------------------ result bookkeeping
    static final List<String[]> RESULTS = new ArrayList<>();
    static int nPass, nFail, nDiff, nInfo;

    static void record(String status, String sec, String id, String detail) {
        RESULTS.add(new String[]{status, sec, id, detail});
        switch (status) {
            case "PASS": nPass++; break;
            case "FAIL": nFail++; break;
            case "DIFF": nDiff++; break;
            default: nInfo++;
        }
        if (!"PASS".equals(status) || VERBOSE) {
            System.out.printf("  %-4s %-8s %-46s %s%n", status, sec, id, detail);
        }
    }

    static void expect(String sec, String id, boolean ok, String detail) {
        record(ok ? "PASS" : "FAIL", sec, id, detail);
    }

    static boolean VERBOSE = Boolean.getBoolean("verbose");

    static final ObjectMapper JACKSON = new ObjectMapper();
    static final ObjectMapper JACKSON_STRICT = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
    static final ObjectMapper JACKSON_CANONICAL = JsonMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();

    // ------------------------------------------------------------------ beans
    @JSONType(alphabetic = false)
    public static final class Inner {
        public int z = 1;
        public String a = "x";
        public boolean m = true;
    }

    @JSONType(alphabetic = false)
    public static class InnerNF {
        public int z = 1;
        public String a = "x";
        public boolean m = true;
    }

    @JSONType(alphabetic = false)
    public static class SubNF extends InnerNF {
        public int b = 2;
    }

    @JSONType(alphabetic = false)
    public static class Holder<T> {
        public int zz;
        public T value;

        public Holder() {
        }

        public Holder(T value) {
            this.value = value;
        }
    }

    @JSONType(alphabetic = false) public static class HObj { public int zz; public Inner f = new Inner(); }
    @JSONType(alphabetic = false) public static class HObjNF { public int zz; public InnerNF f = new InnerNF(); }
    @JSONType(alphabetic = false) public static class HPoly { public int zz; public InnerNF f = new SubNF(); }
    @JSONType(alphabetic = false) public static class HAsObject { public int zz; public Object f = new Inner(); }
    @JSONType(alphabetic = false) public static class HList { public int zz; public List<Inner> f = new ArrayList<>(Arrays.asList(new Inner(), new Inner())); }
    @JSONType(alphabetic = false) public static class HListNF { public int zz; public List<InnerNF> f = new ArrayList<>(Arrays.asList(new InnerNF(), new SubNF())); }
    @JSONType(alphabetic = false) public static class HListObj { public int zz; public List<Object> f = new ArrayList<>(Arrays.asList(new Inner(), new InnerNF())); }
    @JSONType(alphabetic = false) public static class HAsList { public int zz; public List<Inner> f = Arrays.asList(new Inner(), new Inner()); }
    @JSONType(alphabetic = false) public static class HUnmodList { public int zz; public List<Inner> f = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(new Inner()))); }
    @JSONType(alphabetic = false) public static class HSet { public int zz; public Set<Inner> f = new LinkedHashSet<>(Collections.singletonList(new Inner())); }
    @JSONType(alphabetic = false) public static class HColl { public int zz; public Collection<Inner> f = new ArrayDeque<>(Collections.singletonList(new Inner())); }
    @JSONType(alphabetic = false) public static class HArr { public int zz; public Inner[] f = {new Inner(), new Inner()}; }
    @JSONType(alphabetic = false) public static class HArrNF { public int zz; public InnerNF[] f = {new InnerNF(), new SubNF()}; }
    @JSONType(alphabetic = false) public static class HObjArr { public int zz; public Object[] f = {new Inner(), "s"}; }
    @JSONType(alphabetic = false) public static class HMap { public int zz; public Map<String, Inner> f = lmap("k1", new Inner(), "k2", new Inner()); }
    @JSONType(alphabetic = false) public static class HMapNF { public int zz; public Map<String, InnerNF> f = lmap("k1", new InnerNF(), "k2", new SubNF()); }
    @JSONType(alphabetic = false) public static class HMapObj { public int zz; public Map<String, Object> f = lmap("k1", new Inner(), "k2", new InnerNF()); }
    @JSONType(alphabetic = false) public static class HMapList { public int zz; public Map<String, List<Inner>> f = lmap("k1", new ArrayList<>(Collections.singletonList(new Inner()))); }
    @JSONType(alphabetic = false) public static class HNested { public int zz; public List<List<Inner>> f = new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(new Inner())))); }
    @JSONType(alphabetic = false) public static class HOpt { public int zz; public Optional<Inner> f = Optional.of(new Inner()); }
    @JSONType(alphabetic = false) public static class HRef { public int zz; public AtomicReference<Inner> f = new AtomicReference<>(new Inner()); }
    @JSONType(alphabetic = false) public static class HGeneric { public int zz; public Holder<Inner> f = new Holder<>(new Inner()); }
    @JSONType(alphabetic = false) public static class HJsonObj { public int zz; public JSONObject f = JSONObject.of("k1", new Inner()); }
    @JSONType(alphabetic = false) public static class HJsonArr { public int zz; public JSONArray f = JSONArray.of(new Inner()); }

    @JSONType(alphabetic = false)
    public static class HGetters {
        private int zz;
        private Inner obj = new Inner();
        private List<Inner> list = new ArrayList<>(Collections.singletonList(new Inner()));
        private Inner[] arr = {new Inner()};
        private Map<String, Inner> map = lmap("k1", new Inner());

        public int getZz() { return zz; }
        public Inner getObj() { return obj; }
        public List<Inner> getList() { return list; }
        public Inner[] getArr() { return arr; }
        public Map<String, Inner> getMap() { return map; }
    }

    public static class Plain {
        public int z = 1;
        public String a = "x";
        public boolean m = true;
    }

    @JSONType(alphabetic = false)
    public static class Ordinals {
        @JSONField(ordinal = 2) public int z = 1;
        @JSONField(ordinal = 1) public int y = 2;
        public int b = 3;
        public int a = 4;
    }

    @JSONType(alphabetic = true)
    public static class OrdinalsAlpha {
        @JSONField(ordinal = 2) public int z = 1;
        @JSONField(ordinal = 1) public int y = 2;
        public int b = 3;
        public int a = 4;
    }

    @JSONType(alphabetic = false, serializeFeatures = JSONWriter.Feature.BeanToArray)
    public static class ArrayMapped {
        public int z = 1;
        public int a = 2;
        public int m = 3;
    }

    public static class Money {
        public long cents = 123;
    }

    @JSONType(alphabetic = false)
    public static class HMoney {
        public int zz;
        public Money price = new Money();
    }

    @JSONType(alphabetic = false)
    public static class MixTarget {
        public int z = 1;
        public int a = 2;
    }

    public abstract static class MixSource {
        @JSONField(name = "renamed")
        public int a;
    }

    @JSONType(alphabetic = false)
    public static class Order {
        public String zId = "o-1";
        public long amount = 100;
        public List<Inner> items = new ArrayList<>(Arrays.asList(new Inner(), new Inner()));
        public Map<String, Object> meta;
        public Inner inner = new Inner();
        public String currency = "CNY";
    }

    @JSONType(alphabetic = false)
    public static class Frame {
        public String id;
        public int seq;
        public Map<String, Object> payload;
    }

    public static class AnyBean {
        public int id;
        public final Map<String, Object> extra = new LinkedHashMap<>();

        @JSONField(unwrapped = true)
        public void set(String key, Object value) {
            extra.put(key, value);
        }
    }

    public static class StrictBean {
        public int id;
    }

    @SuppressWarnings("unchecked")
    static <K, V> LinkedHashMap<K, V> lmap(Object... kv) {
        LinkedHashMap<K, V> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((K) kv[i], (V) kv[i + 1]);
        }
        return m;
    }

    // ------------------------------------------------------------------ entry
    public static void main(String[] args) throws Exception {
        String only = args.length > 0 ? args[0] : "all";
        System.out.println("== PR #7880 independent verification harness ==");
        System.out.println("java.version   : " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.name") + ")");
        System.out.println("fastjson2 jar  : " + new File(JSON.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getName());
        System.out.println("fastjson2 ver  : " + JSON.VERSION);
        System.out.println("jackson oracle : " + com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION);
        System.out.println();

        if (only.equals("all") || only.equals("dup")) {
            sectionDuplicateKeys();
        }
        if (only.equals("all") || only.equals("sort")) {
            sectionSortMatrix();
            sectionSortSemantics();
            sectionBeanToArray();
            sectionProviderMaintenance();
            sectionClassLoaderLeak();
            sectionConcurrency();
            sectionCanonical();
        }
        if (only.equals("all") || only.equals("tree")) {
            sectionTree();
            sectionAnySetter();
        }
        if (only.equals("all") || only.equals("masks")) {
            sectionMasks();
        }
        if (only.equals("globalAlphabeticOff")) {
            sectionGlobalAlphabeticOff();
        }

        System.out.println();
        System.out.printf("SUMMARY  PASS=%d  FAIL=%d  DIFF=%d  INFO=%d  (java %s)%n",
                nPass, nFail, nDiff, nInfo, System.getProperty("java.version"));

        String out = System.getProperty("out");
        if (out != null) {
            JSONArray arr = new JSONArray();
            for (String[] r : RESULTS) {
                arr.add(JSONObject.of("status", r[0], "section", r[1], "id", r[2], "detail", r[3]));
            }
            JSONObject root = JSONObject.of(
                    "java", System.getProperty("java.version"),
                    "pass", nPass, "fail", nFail, "diff", nDiff, "info", nInfo,
                    "results", arr);
            try (Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8)) {
                w.write(root.toJSONString(JSONWriter.Feature.PrettyFormat));
            }
        }
    }

    // ================================================================== A. ErrorOnDuplicateKeys
    interface Parse {
        Object parse(String json, JSONReader.Feature... features) throws Exception;
    }

    static final Map<String, Parse> FJ_TARGETS = new LinkedHashMap<>();
    static final Map<String, JavaType> JK_TARGETS = new LinkedHashMap<>();

    static void fj(String name, Parse p, JavaType jk) {
        FJ_TARGETS.put(name, p);
        JK_TARGETS.put(name, jk);
    }

    static void sectionDuplicateKeys() throws Exception {
        System.out.println("[A] JSONReader.Feature.ErrorOnDuplicateKeys");
        final JSONReader.Feature DUP = JSONReader.Feature.valueOf("ErrorOnDuplicateKeys");
        com.fasterxml.jackson.databind.type.TypeFactory tf = JACKSON.getTypeFactory();

        fj("JSON.parseObject(String)", (s, f) -> JSON.parseObject(s, f), tf.constructType(ObjectNode.class));
        fj("JSON.parseObject(byte[] UTF8)", (s, f) -> JSON.parseObject(s.getBytes(StandardCharsets.UTF_8), f), tf.constructType(ObjectNode.class));
        fj("JSONReader.of(char[]).readObject", (s, f) -> {
            try (JSONReader r = JSONReader.of(s.toCharArray())) {
                r.getContext().config(f);
                return r.readObject();
            }
        }, tf.constructType(ObjectNode.class));
        fj("JSON.parseObject(InputStream)", (s, f) -> JSON.parseObject(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), f), tf.constructType(ObjectNode.class));
        fj("JSONReader.of(Reader).readObject", (s, f) -> {
            try (JSONReader r = JSONReader.of(new StringReader(s))) {
                r.getContext().config(f);
                return r.readObject();
            }
        }, tf.constructType(ObjectNode.class));
        fj("JSON.parse(String) -> Object", (s, f) -> JSON.parse(s, f), tf.constructType(Object.class));
        fj("JSON.parseObject(s, Object.class)", (s, f) -> JSON.parseObject(s, Object.class, f), tf.constructType(Object.class));
        fj("JSON.parseObject(s, Map.class)", (s, f) -> JSON.parseObject(s, Map.class, f), tf.constructType(Map.class));
        fj("JSON.parseObject(s, HashMap.class)", (s, f) -> JSON.parseObject(s, HashMap.class, f), tf.constructType(HashMap.class));
        fj("JSON.parseObject(s, TreeMap.class)", (s, f) -> JSON.parseObject(s, TreeMap.class, f), tf.constructType(TreeMap.class));
        fj("TypeReference<Map<String,Object>>", (s, f) -> JSON.parseObject(s, new TypeReference<Map<String, Object>>() {}, f),
                tf.constructMapType(LinkedHashMap.class, String.class, Object.class));
        fj("TypeReference<Map<String,String>>", (s, f) -> JSON.parseObject(s, new TypeReference<Map<String, String>>() {}, f),
                tf.constructMapType(LinkedHashMap.class, String.class, String.class));
        fj("TypeReference<Map<String,Integer>>", (s, f) -> JSON.parseObject(s, new TypeReference<Map<String, Integer>>() {}, f),
                tf.constructMapType(LinkedHashMap.class, String.class, Integer.class));
        fj("TypeReference<Map<Integer,Object>>", (s, f) -> JSON.parseObject(s, new TypeReference<Map<Integer, Object>>() {}, f),
                tf.constructMapType(LinkedHashMap.class, Integer.class, Object.class));
        fj("JSON.parseArray -> JSONArray", (s, f) -> JSON.parseArray("[" + s + "]", f), tf.constructType(Object[].class));
        fj("Bean{Map<String,Object> payload}", (s, f) -> JSON.parseObject("{\"id\":\"f\",\"seq\":1,\"payload\":" + s + "}", Frame.class, f),
                tf.constructType(Frame.class));
        fj("POJO target (StrictBean)", (s, f) -> JSON.parseObject(s, StrictBean.class, f), tf.constructType(StrictBean.class));

        // json -> (expected jackson strict dup error?)  Values are kept compatible with Map<String,Integer> / Integer keys / StrictBean.
        String[][] inputs = {
                {"{\"id\":1,\"id\":2}", "simple duplicate"},
                {"{\"id\":null,\"id\":2}", "first occurrence null"},
                {"{\"id\":1,\"\\u0069d\":2}", "escaped duplicate (\\u0069d == id)"},
                {"{\"id\":1,\"x\":{\"q\":1,\"q\":2}}", "duplicate in nested object"},
                {"{\"id\":1,\"ID\":2}", "case differs (not a duplicate)"},
                {"{\"id\":{\"id\":1}}", "same key at different depth (not a duplicate)"},
                {"{\"id\":1,\"x\":2}", "no duplicate"},
        };

        int agree = 0, total = 0;
        List<String> diffs = new ArrayList<>();
        for (String[] in : inputs) {
            for (Map.Entry<String, Parse> e : FJ_TARGETS.entrySet()) {
                String target = e.getKey();
                String json = in[0];
                if (target.startsWith("TypeReference<Map<String,Integer>>") || target.startsWith("TypeReference<Map<Integer")
                        || target.startsWith("POJO") || target.startsWith("TypeReference<Map<String,String>>")) {
                    if (json.contains("\"x\":{")) {
                        continue; // nested object not representable in these value/key types
                    }
                }
                if (target.startsWith("TypeReference<Map<Integer")) {
                    json = json.replace("\"id\"", "\"7\"").replace("\"\\u0069d\"", "\"\\u0037\"").replace("\"ID\"", "\"8\"").replace("\"x\"", "\"9\"");
                }
                if (target.startsWith("POJO") && (json.contains("\"ID\"") || json.contains("\"x\""))) {
                    continue; // unknown properties not relevant here
                }
                final String jsonIn = json;
                String fjOutcome = outcome(() -> e.getValue().parse(jsonIn, DUP));
                String jkOutcome;
                try {
                    JSON_JK(json, JK_TARGETS.get(target), target);
                    jkOutcome = "ok";
                } catch (Exception ex) {
                    jkOutcome = ex.getClass().getSimpleName().contains("Duplicate") || String.valueOf(ex.getMessage()).contains("Duplicate") ? "dup-error" : "error:" + ex.getClass().getSimpleName();
                }
                total++;
                boolean fjDup = fjOutcome.startsWith("dup-error");
                boolean jkDup = jkOutcome.startsWith("dup-error");
                String id = target + " | " + in[1];
                if (fjDup == jkDup) {
                    agree++;
                    record("PASS", "A.dup", id, "fastjson2=" + fjOutcome + " jackson=" + jkOutcome);
                } else if (target.startsWith("POJO")) {
                    record("DIFF", "A.dup", id, "fastjson2=" + fjOutcome + " jackson(STRICT_DUPLICATE_DETECTION)=" + jkOutcome + "  [documented scope: untyped Map/tree only]");
                    diffs.add(id);
                } else {
                    record("FAIL", "A.dup", id, "fastjson2=" + fjOutcome + " jackson(STRICT_DUPLICATE_DETECTION)=" + jkOutcome);
                    diffs.add(id);
                }
            }
        }
        System.out.printf("  -> duplicate-key differential vs jackson STRICT_DUPLICATE_DETECTION: %d/%d agree%n", agree, total);
        record("INFO", "A.dup", "differential agreement", agree + "/" + total + " combinations agree with jackson");

        // error message carries a location
        try {
            JSON.parseObject("{\"a\":1,\n \"a\":2}", DUP);
        } catch (JSONException ex) {
            expect("A.dup", "error message names key + position", ex.getMessage().contains("duplicate key : a") && ex.getMessage().contains("line"),
                    ex.getMessage().replace('\n', ' '));
        }

        // precedence over DuplicateKeyValueAsArray
        expect("A.dup", "ErrorOnDuplicateKeys wins over DuplicateKeyValueAsArray",
                outcome(() -> JSON.parseObject("{\"a\":1,\"a\":2}", DUP, JSONReader.Feature.DuplicateKeyValueAsArray)).startsWith("dup-error"), "");

        // feature off: behavior unchanged
        JSONObject off = JSON.parseObject("{\"a\":1,\"a\":2}");
        expect("A.dup", "feature off: last value wins (unchanged)", Integer.valueOf(2).equals(off.get("a")), off.toJSONString());
        JSONObject asArr = JSON.parseObject("{\"a\":1,\"a\":2}", JSONReader.Feature.DuplicateKeyValueAsArray);
        expect("A.dup", "feature off: DuplicateKeyValueAsArray unchanged", "[1,2]".equals(JSON.toJSONString(asArr.get("a"))), asArr.toJSONString());

        // IgnoreNullPropertyValue interaction
        record("INFO", "A.dup", "with IgnoreNullPropertyValue {a:null,a:1}",
                outcome(() -> JSON.parseObject("{\"a\":null,\"a\":1}", DUP, JSONReader.Feature.IgnoreNullPropertyValue))
                        + " (null skipped before detection)");

        // global enable via JSON.config
        JSON.config(DUP, true);
        try {
            expect("A.dup", "global JSON.config(ErrorOnDuplicateKeys,true)",
                    outcome(() -> JSON.parseObject("{\"a\":1,\"a\":2}")).startsWith("dup-error"), "");
        } finally {
            JSON.config(DUP, false);
        }
        expect("A.dup", "global config reset -> lenient again", outcome(() -> JSON.parseObject("{\"a\":1,\"a\":2}")).equals("ok"), "");

        // JSONB input
        byte[] jsonb;
        try (JSONWriter w = JSONWriter.ofJSONB()) {
            w.startObject();
            w.writeName("a");
            w.writeInt32(1);
            w.writeName("a");
            w.writeInt32(2);
            w.endObject();
            jsonb = w.getBytes();
        }
        final byte[] jb = jsonb;
        String jbDup = outcome(() -> JSONB.parseObject(jb, DUP));
        String jbArr = outcome(() -> JSON.toJSONString(JSONB.parseObject(jb, JSONReader.Feature.DuplicateKeyValueAsArray)));
        record("DIFF", "A.dup", "JSONB input {a:1,a:2} + ErrorOnDuplicateKeys", "-> " + jbDup
                + "  (JSONB reader is outside the feature's scope; DuplicateKeyValueAsArray is ignored there too: " + jbArr + ")");
        System.out.println();
    }

    static void JSON_JK(String json, JavaType type, String target) throws Exception {
        if (target.startsWith("JSON.parseArray")) {
            JACKSON_STRICT.readTree("[" + json + "]");
        } else if (target.startsWith("Bean{")) {
            JACKSON_STRICT.readValue("{\"id\":\"f\",\"seq\":1,\"payload\":" + json + "}", Frame.class);
        } else if (target.startsWith("POJO")) {
            JACKSON_STRICT.readerFor(StrictBean.class).without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(json);
        } else {
            JACKSON_STRICT.readValue(json, type);
        }
    }

    interface Call {
        Object call() throws Exception;
    }

    static String outcome(Call c) {
        try {
            Object r = c.call();
            return "ok" + (r instanceof String ? ":" + r : "");
        } catch (JSONException ex) {
            String m = String.valueOf(ex.getMessage());
            if (m.contains("duplicate key")) {
                return "dup-error";
            }
            for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
                if (String.valueOf(cause.getMessage()).contains("duplicate key")) {
                    return "dup-error(wrapped by '" + m.split(",")[0] + "')";
                }
            }
            return "error:" + m.split(",")[0];
        } catch (Throwable ex) {
            return "error:" + ex.getClass().getSimpleName();
        }
    }

    // ================================================================== B. SortFieldNamesAlphabetically
    static final JSONWriter.Feature SORTED = findSorted();

    static JSONWriter.Feature findSorted() {
        try {
            return JSONWriter.Feature.valueOf("SortFieldNamesAlphabetically");
        } catch (IllegalArgumentException e) {
            return null; // baseline jar
        }
    }

    static Map<String, Object> shapes() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("root Inner (final)", new Inner());
        s.put("root InnerNF", new InnerNF());
        s.put("field Inner(final)", new HObj());
        s.put("field InnerNF", new HObjNF());
        s.put("field polymorphic SubNF", new HPoly());
        s.put("field Object=Inner", new HAsObject());
        s.put("field List<Inner>", new HList());
        s.put("field List<InnerNF> poly", new HListNF());
        s.put("field List<Object>", new HListObj());
        s.put("field Arrays.asList", new HAsList());
        s.put("field unmodifiableList", new HUnmodList());
        s.put("field Set<Inner>", new HSet());
        s.put("field Collection(ArrayDeque)", new HColl());
        s.put("field Inner[] (final item)", new HArr());
        s.put("field InnerNF[]", new HArrNF());
        s.put("field Object[]", new HObjArr());
        s.put("field Map<String,Inner>", new HMap());
        s.put("field Map<String,InnerNF>", new HMapNF());
        s.put("field Map<String,Object>", new HMapObj());
        s.put("field Map<String,List<Inner>>", new HMapList());
        s.put("field List<List<Inner>>", new HNested());
        s.put("field Optional<Inner>", new HOpt());
        s.put("field AtomicReference<Inner>", new HRef());
        s.put("field Holder<Inner> generic", new HGeneric());
        s.put("field JSONObject{Inner}", new HJsonObj());
        s.put("field JSONArray[Inner]", new HJsonArr());
        s.put("getters obj/list/arr/map", new HGetters());
        s.put("root ArrayList<Inner>", new ArrayList<>(Arrays.asList(new Inner(), new InnerNF())));
        s.put("root LinkedHashMap<String,Inner>", lmap("k1", new Inner(), "k2", new SubNF()));
        s.put("root Inner[]", new Inner[]{new Inner()});
        s.put("root InnerNF[]", new InnerNF[]{new InnerNF(), new SubNF()});
        s.put("root Object[]", new Object[]{new Inner(), 1});
        s.put("root JSONObject{Inner}", JSONObject.of("k1", new Inner()));
        s.put("root singletonList", Collections.singletonList(new Inner()));
        return s;
    }

    interface Channel {
        String write(Object o, ObjectWriterProvider provider, JSONWriter.Feature... features);
    }

    static final Map<String, Channel> CHANNELS = new LinkedHashMap<>();

    static {
        CHANNELS.put("JSON.toJSONString(ctx)", (o, p, f) -> JSON.toJSONString(o, new JSONWriter.Context(p, f)));
        CHANNELS.put("JSONWriter.ofUTF8.writeAny", (o, p, f) -> {
            try (JSONWriter w = JSONWriter.ofUTF8(new JSONWriter.Context(p, f))) {
                w.writeAny(o);
                return new String(w.getBytes(), StandardCharsets.UTF_8);
            }
        });
        CHANNELS.put("JSONB.toBytes(ctx)", (o, p, f) -> JSON.toJSONString(JSONB.parse(JSONB.toBytes(o, new JSONWriter.Context(p, f)))));
    }

    static String norm(String json) {
        try {
            return JACKSON.writeValueAsString(JACKSON.readTree(json));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String sortNorm(String json) {
        try {
            return JACKSON.writeValueAsString(sortRec(JACKSON.readTree(json)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static JsonNode sortRec(JsonNode n) {
        if (n.isObject()) {
            ObjectNode o = JACKSON.createObjectNode();
            TreeMap<String, JsonNode> sorted = new TreeMap<>();
            Iterator<Map.Entry<String, JsonNode>> it = n.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                sorted.put(e.getKey(), sortRec(e.getValue()));
            }
            o.setAll(sorted);
            return o;
        }
        if (n.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode a = JACKSON.createArrayNode();
            for (JsonNode c : n) {
                a.add(sortRec(c));
            }
            return a;
        }
        return n;
    }

    static ObjectWriterProvider newProvider(String creator) {
        return "reflect".equals(creator) ? new ObjectWriterProvider(ObjectWriterCreator.INSTANCE) : new ObjectWriterProvider();
    }

    static JSONWriter.Feature[] feats(boolean sorted, boolean fieldBased) {
        List<JSONWriter.Feature> l = new ArrayList<>();
        if (sorted) {
            l.add(SORTED);
        }
        if (fieldBased) {
            l.add(JSONWriter.Feature.FieldBased);
        }
        return l.toArray(new JSONWriter.Feature[0]);
    }

    static final Map<String, int[]> SHAPE_STATS = new LinkedHashMap<>();
    static final List<String> SORT_FAILS = new ArrayList<>();

    static void sectionSortMatrix() {
        System.out.println("[B1] SortFieldNamesAlphabetically: shape x creator x base-features x sequence x channel matrix");
        String[] creators = {"asm", "reflect"};
        boolean[][] sequences = {
                {false, true, false, true},
                {true, false, true, false},
        };
        int scenarios = 0, serializations = 0, failures = 0;
        for (Map.Entry<String, Object> shape : shapes().entrySet()) {
            int[] st = new int[2];
            SHAPE_STATS.put(shape.getKey(), st);
            for (String creator : creators) {
                for (boolean fieldBased : new boolean[]{false, true}) {
                    for (Map.Entry<String, Channel> ch : CHANNELS.entrySet()) {
                        String expectedU;
                        String expectedS;
                        try {
                            expectedU = norm(ch.getValue().write(shape.getValue(), newProvider(creator), feats(false, fieldBased)));
                            expectedS = sortNorm(expectedU);
                        } catch (Throwable t) {
                            record("INFO", "B1", shape.getKey() + " / " + ch.getKey(), "oracle unavailable: " + t);
                            continue;
                        }
                        for (int q = 0; q < sequences.length + 1; q++) {
                            scenarios++;
                            ObjectWriterProvider p = newProvider(creator);
                            boolean[] seq = q < sequences.length ? sequences[q] : new boolean[]{true, false, true, false};
                            for (int i = 0; i < seq.length; i++) {
                                boolean sorted = seq[i];
                                // sequence #3 alternates channels on the same provider
                                Channel c = q < sequences.length ? ch.getValue() : (i % 2 == 0 ? CHANNELS.get("JSONB.toBytes(ctx)") : CHANNELS.get("JSON.toJSONString(ctx)"));
                                String got;
                                try {
                                    got = norm(c.write(shape.getValue(), p, feats(sorted, fieldBased)));
                                } catch (Throwable t) {
                                    got = "EXCEPTION " + t;
                                }
                                String want = sorted ? expectedS : expectedU;
                                if (q == sequences.length) {
                                    // cross-channel: compare structure only via sorted/unsorted normal forms
                                    want = sorted ? sortNorm(want) : want;
                                }
                                serializations++;
                                st[0]++;
                                if (!got.equals(want)) {
                                    st[1]++;
                                    failures++;
                                    String key = shape.getKey() + " | " + creator + (fieldBased ? "+FieldBased" : "") + " | " + ch.getKey() + " | seq" + q + " step" + i + (sorted ? " (sorted)" : " (unsorted)");
                                    if (SORT_FAILS.size() < 400) {
                                        SORT_FAILS.add(key + "\n      want " + want + "\n      got  " + got);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, int[]> e : SHAPE_STATS.entrySet()) {
            int[] st = e.getValue();
            expect("B1", e.getKey(), st[1] == 0, st[0] + " serializations, " + st[1] + " wrong order");
        }
        System.out.printf("  -> %d scenarios, %d serializations, %d wrong-order outputs%n", scenarios, serializations, failures);
        int shown = 0;
        Set<String> seenShape = new HashSet<>();
        for (String f : SORT_FAILS) {
            String shapeName = f.substring(0, f.indexOf(" | "));
            if (seenShape.add(shapeName) && shown++ < 12) {
                System.out.println("     e.g. " + f);
            }
        }
        System.out.println();
    }

    static void sectionSortSemantics() throws Exception {
        System.out.println("[B2] SortFieldNamesAlphabetically semantics");
        // same comparator as alphabetic=true, ordinals first
        String withFeature = JSON.toJSONString(new Ordinals(), SORTED);
        String alpha = JSON.toJSONString(new OrdinalsAlpha());
        expect("B2", "ordering == @JSONType(alphabetic=true) (ordinals honored)", withFeature.equals(alpha), withFeature + " vs " + alpha);
        expect("B2", "feature off keeps declaration order", JSON.toJSONString(new Ordinals()).equals("{\"z\":1,\"y\":2,\"b\":3,\"a\":4}"), JSON.toJSONString(new Ordinals()));

        // provider.setAlphabetic(false)
        ObjectWriterProvider p = new ObjectWriterProvider();
        p.setAlphabetic(false);
        String u = JSON.toJSONString(new Plain(), new JSONWriter.Context(p));
        String s = JSON.toJSONString(new Plain(), new JSONWriter.Context(p, SORTED));
        expect("B2", "provider.setAlphabetic(false): default declaration order", u.equals("{\"z\":1,\"a\":\"x\",\"m\":true}"), u);
        expect("B2", "provider.setAlphabetic(false): feature restores sort", s.equals("{\"a\":\"x\",\"m\":true,\"z\":1}"), s);

        // global default provider, interleaved (JSON.toJSONString(Object, Feature...) root path)
        boolean ok = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            boolean sorted = i % 2 == 1;
            String r = sorted ? JSON.toJSONString(new HList(), SORTED) : JSON.toJSONString(new HList());
            String want = sorted ? sortNorm(JSON.toJSONString(new HList())) : norm(JSON.toJSONString(new HList()));
            if (!norm(r).equals(want)) {
                ok = false;
                sb.append(r).append(' ');
            }
        }
        expect("B2", "global provider interleaved U/S/U/S (HList)", ok, sb.toString());
        String bytes = new String(JSON.toJSONBytes(new HMap(), SORTED), StandardCharsets.UTF_8);
        expect("B2", "JSON.toJSONBytes(obj, feature)", norm(bytes).equals(sortNorm(JSON.toJSONString(new HMap()))), bytes);
        String pretty = JSON.toJSONString(new HObj(), SORTED, JSONWriter.Feature.PrettyFormat);
        expect("B2", "combined with PrettyFormat", norm(pretty).equals(sortNorm(JSON.toJSONString(new HObj()))), "");
        System.out.println();
    }

    static void sectionBeanToArray() {
        System.out.println("[B3] BeanToArray positional output must never be reordered");
        for (String creator : new String[]{"asm", "reflect"}) {
            // per-call BeanToArray + per-call SortFieldNamesAlphabetically
            String base = JSON.toJSONString(new HObj(), new JSONWriter.Context(newProvider(creator), JSONWriter.Feature.BeanToArray));
            String both = JSON.toJSONString(new HObj(), new JSONWriter.Context(newProvider(creator), JSONWriter.Feature.BeanToArray, SORTED));
            expect("B3", creator + ": per-call BeanToArray + feature (fresh provider)", base.equals(both), base + " vs " + both);

            ObjectWriterProvider warm = newProvider(creator);
            JSON.toJSONString(new HObj(), new JSONWriter.Context(warm, SORTED));
            String afterWarm = JSON.toJSONString(new HObj(), new JSONWriter.Context(warm, JSONWriter.Feature.BeanToArray, SORTED));
            expect("B3", creator + ": per-call BeanToArray + feature (sorted cache warm)", base.equals(afterWarm), base + " vs " + afterWarm);

            String annBase = JSON.toJSONString(new ArrayMapped(), new JSONWriter.Context(newProvider(creator)));
            String annSorted = JSON.toJSONString(new ArrayMapped(), new JSONWriter.Context(newProvider(creator), SORTED));
            expect("B3", creator + ": @JSONType(serializeFeatures=BeanToArray) + feature", annBase.equals(annSorted), annBase + " vs " + annSorted);

            byte[] jb1 = JSONB.toBytes(new HObj(), new JSONWriter.Context(newProvider(creator), JSONWriter.Feature.BeanToArray));
            byte[] jb2 = JSONB.toBytes(new HObj(), new JSONWriter.Context(newProvider(creator), JSONWriter.Feature.BeanToArray, SORTED));
            expect("B3", creator + ": JSONB BeanToArray + feature", Arrays.equals(jb1, jb2), JSONB.toJSONString(jb1) + " vs " + JSONB.toJSONString(jb2));
        }
        System.out.println();
    }

    static final ObjectWriter<Money> MONEY_WRITER = new ObjectWriter<Money>() {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString("$" + ((Money) object).cents / 100.0);
        }
    };

    @SuppressWarnings("unchecked")
    static Map<Type, ObjectWriter> cacheField(ObjectWriterProvider p, String name) throws Exception {
        Field f = ObjectWriterProvider.class.getDeclaredField(name);
        f.setAccessible(true);
        return (Map<Type, ObjectWriter>) f.get(p);
    }

    static void sectionProviderMaintenance() throws Exception {
        System.out.println("[B4] ObjectWriterProvider maintenance APIs vs the new sorted caches");
        // register() before first use
        ObjectWriterProvider p = new ObjectWriterProvider();
        p.register(Money.class, MONEY_WRITER);
        String u = JSON.toJSONString(new Money(), new JSONWriter.Context(p));
        String s = JSON.toJSONString(new Money(), new JSONWriter.Context(p, SORTED));
        expect("B4", "register(Money) honored without feature", "\"$1.23\"".equals(u), u);
        expect("B4", "register(Money) honored WITH feature (root)", "\"$1.23\"".equals(s), s);
        String fu = JSON.toJSONString(new HMoney(), new JSONWriter.Context(p));
        String fs = JSON.toJSONString(new HMoney(), new JSONWriter.Context(p, SORTED));
        expect("B4", "register(Money) honored WITH feature (as bean field)", fs.contains("\"$1.23\""), "unsorted=" + fu + " sorted=" + fs);
        String fsb = JSON.toJSONString(JSONB.parse(JSONB.toBytes(new HMoney(), new JSONWriter.Context(p, SORTED))));
        expect("B4", "register(Money) honored WITH feature (JSONB field)", fsb.contains("$1.23"), fsb);

        // global JSON.register
        JSON.register(Money.class, MONEY_WRITER);
        try {
            String g = JSON.toJSONString(new Money(), SORTED);
            expect("B4", "JSON.register(Money) honored WITH feature (global)", "\"$1.23\"".equals(g), g);
        } finally {
            JSON.register(Money.class, (ObjectWriter) null);
        }

        // register() after the sorted writer was cached
        ObjectWriterProvider p2 = new ObjectWriterProvider();
        JSON.toJSONString(new Money(), new JSONWriter.Context(p2, SORTED));
        JSON.toJSONString(new Money(), new JSONWriter.Context(p2));
        p2.register(Money.class, MONEY_WRITER);
        String u2 = JSON.toJSONString(new Money(), new JSONWriter.Context(p2));
        String s2 = JSON.toJSONString(new Money(), new JSONWriter.Context(p2, SORTED));
        expect("B4", "register() after warm-up: visible without feature", "\"$1.23\"".equals(u2), u2);
        expect("B4", "register() after warm-up: visible WITH feature", "\"$1.23\"".equals(s2), s2);

        // mixIn after warm-up
        ObjectWriterProvider p3 = new ObjectWriterProvider();
        JSON.toJSONString(new MixTarget(), new JSONWriter.Context(p3));
        JSON.toJSONString(new MixTarget(), new JSONWriter.Context(p3, SORTED));
        p3.mixIn(MixTarget.class, MixSource.class);
        String mu = JSON.toJSONString(new MixTarget(), new JSONWriter.Context(p3));
        String ms = JSON.toJSONString(new MixTarget(), new JSONWriter.Context(p3, SORTED));
        expect("B4", "mixIn() after warm-up: applied without feature", mu.contains("renamed"), mu);
        expect("B4", "mixIn() after warm-up: applied WITH feature", ms.contains("renamed"), ms);

        // clear() / cleanup(Class)
        ObjectWriterProvider p4 = new ObjectWriterProvider();
        JSON.toJSONString(new Inner(), new JSONWriter.Context(p4));
        JSON.toJSONString(new Inner(), new JSONWriter.Context(p4, SORTED));
        p4.cleanup(Inner.class);
        expect("B4", "cleanup(Class) evicts natural cache", !cacheField(p4, "cache").containsKey(Inner.class), "");
        expect("B4", "cleanup(Class) evicts sorted cache", !cacheField(p4, "cacheFieldNamesSorted").containsKey(Inner.class),
                "cacheFieldNamesSorted still holds " + cacheField(p4, "cacheFieldNamesSorted").keySet());
        JSON.toJSONString(new Inner(), new JSONWriter.Context(p4, SORTED, JSONWriter.Feature.FieldBased));
        p4.clear();
        int left = cacheField(p4, "cacheFieldNamesSorted").size() + cacheField(p4, "cacheFieldNamesSortedFieldBased").size();
        expect("B4", "clear() empties sorted caches", left == 0, left + " sorted writers survive clear()");
        System.out.println();
    }

    static void sectionClassLoaderLeak() throws Exception {
        System.out.println("[B5] cleanup(ClassLoader) releases a hot-reloaded class loader");
        URL leakDir = new File(System.getProperty("leak.dir", "leak")).toURI().toURL();
        for (String creator : new String[]{"asm", "reflect"}) {
            for (boolean sorted : new boolean[]{false, true}) {
                ObjectWriterProvider provider = newProvider(creator);
                WeakReference<ClassLoader> ref = serializeInIsolatedLoader(leakDir, provider, sorted);
                boolean collected = false;
                for (int i = 0; i < 40 && !collected; i++) {
                    System.gc();
                    Thread.sleep(25);
                    collected = ref.get() == null;
                }
                String id = creator + (sorted ? " + SortFieldNamesAlphabetically" : " (feature off)");
                expect("B5", id, collected, collected ? "loader collected after cleanup(loader)" : "loader still strongly reachable after cleanup(loader) -> leak");
                // keep provider reachable until after the GC check
                provider.isAlphabetic();
            }
        }
        System.out.println();
    }

    static WeakReference<ClassLoader> serializeInIsolatedLoader(URL dir, ObjectWriterProvider provider, boolean sorted) throws Exception {
        URLClassLoader loader = new URLClassLoader(new URL[]{dir}, Verify7880.class.getClassLoader());
        Class<?> c = loader.loadClass("leak.LeakBean");
        Object bean = c.getConstructor().newInstance();
        String json = sorted
                ? JSON.toJSONString(bean, new JSONWriter.Context(provider, SORTED))
                : JSON.toJSONString(bean, new JSONWriter.Context(provider));
        if (!json.contains("leak")) {
            throw new IllegalStateException(json);
        }
        provider.cleanup(loader);
        loader.close();
        return new WeakReference<>(loader);
    }

    static void sectionConcurrency() throws Exception {
        System.out.println("[B6] concurrent mixed sorted/unsorted writes on one provider");
        final Map<String, Object> shapes = shapes();
        final List<String> names = new ArrayList<>(shapes.keySet());
        for (String creator : new String[]{"asm", "reflect"}) {
            final Map<String, String[]> oracle = new HashMap<>();
            for (String n : names) {
                String u = norm(JSON.toJSONString(shapes.get(n), new JSONWriter.Context(newProvider(creator))));
                oracle.put(n, new String[]{u, sortNorm(u)});
            }
            final ObjectWriterProvider p = newProvider(creator);
            final AtomicInteger bad = new AtomicInteger();
            final AtomicInteger done = new AtomicInteger();
            int threads = 16;
            ExecutorService es = Executors.newFixedThreadPool(threads);
            final CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> fs = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int seed = t;
                fs.add(es.submit(() -> {
                    Random r = new Random(seed);
                    start.await();
                    for (int i = 0; i < 3000; i++) {
                        String n = names.get(r.nextInt(names.size()));
                        boolean sorted = r.nextBoolean();
                        String got = norm(sorted
                                ? JSON.toJSONString(shapes.get(n), new JSONWriter.Context(p, SORTED))
                                : JSON.toJSONString(shapes.get(n), new JSONWriter.Context(p)));
                        if (!got.equals(oracle.get(n)[sorted ? 1 : 0])) {
                            bad.incrementAndGet();
                        }
                        done.incrementAndGet();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> f : fs) {
                f.get();
            }
            es.shutdown();
            expect("B6", creator + ": 16 threads x 3000 mixed writes", bad.get() == 0, done.get() + " writes, " + bad.get() + " wrong order");
        }
        System.out.println();
    }

    static void sectionCanonical() throws Exception {
        System.out.println("[B7] canonical JSON / digest parity with jackson");
        Order o1 = new Order();
        o1.meta = lmap("zeta", 1, "alpha", lmap("y", 2, "b", 3));
        Order o2 = new Order();
        o2.meta = lmap("alpha", lmap("b", 3, "y", 2), "zeta", 1);
        String c1 = JSON.toJSONString(o1, SORTED, JSONWriter.Feature.SortMapEntriesByKeys);
        String c2 = JSON.toJSONString(o2, SORTED, JSONWriter.Feature.SortMapEntriesByKeys);
        String jk = JACKSON_CANONICAL.writeValueAsString(o1);
        expect("B7", "same content, different map insertion order -> identical bytes", c1.equals(c2), sha256(c1).substring(0, 16) + " / " + sha256(c2).substring(0, 16));
        expect("B7", "byte-identical to jackson SORT_PROPERTIES_ALPHABETICALLY+ORDER_MAP_ENTRIES_BY_KEYS", c1.equals(jk),
                "fastjson2=" + c1 + "  jackson=" + jk);
        String nonCanon = JSON.toJSONString(o1);
        record("INFO", "B7", "default output (for contrast)", nonCanon);
        record("INFO", "B7", "canonical output", c1 + "  sha256=" + sha256(c1));
        System.out.println();
    }

    static String sha256(String s) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static void sectionGlobalAlphabeticOff() {
        System.out.println("[B8] -Dfastjson2.writer.alphabetic=false (global switch off)");
        String u = JSON.toJSONString(new Plain());
        String s = JSON.toJSONString(new Plain(), SORTED);
        expect("B8", "plain bean natural order with global switch off", u.equals("{\"z\":1,\"a\":\"x\",\"m\":true}"), u);
        expect("B8", "feature restores alphabetic order", s.equals("{\"a\":\"x\",\"m\":true,\"z\":1}"), s);
        String u2 = JSON.toJSONString(new Plain());
        expect("B8", "natural order still intact afterwards", u2.equals(u), u2);
        System.out.println();
    }

    // ================================================================== C. tree model
    static void sectionTree() throws Exception {
        System.out.println("[C] JSONObject/JSONArray helpers");
        JSONObject src = JSON.parseObject("{\"z\":1,\"a\":{\"b\":[1,{\"c\":2}],\"n\":null},\"s\":\"str\"}");
        src.put("hm", new HashMap<>(Collections.singletonMap("k", 1)));
        JSONObject copy = (JSONObject) JSONObject.class.getMethod("deepCopy").invoke(src);
        copy.getJSONObject("a").getJSONArray("b").getJSONObject(1).put("c", 99);
        copy.getJSONObject("a").put("added", true);
        expect("C.deep", "nested edits do not leak into source", src.getJSONObject("a").getJSONArray("b").getJSONObject(1).getIntValue("c") == 2
                && !src.getJSONObject("a").containsKey("added"), src.toJSONString());
        expect("C.deep", "key order preserved", new ArrayList<>(copy.keySet()).equals(new ArrayList<>(src.keySet())), copy.keySet().toString());
        expect("C.deep", "null values preserved", copy.getJSONObject("a").containsKey("n"), "");
        expect("C.deep", "nested containers are new instances", copy.get("a") != src.get("a")
                && copy.getJSONObject("a").get("b") != src.getJSONObject("a").get("b"), "");
        expect("C.deep", "leaf values shared (documented)", copy.get("s") == src.get("s"), "");
        record("INFO", "C.deep", "non-JSONObject Map value (HashMap) is shared, not copied", "same instance=" + (copy.get("hm") == src.get("hm")) + " (documented)");
        JSONArray arr = JSON.parseArray("[[1,2],{\"a\":[3]}]");
        JSONArray arrCopy = (JSONArray) JSONArray.class.getMethod("deepCopy").invoke(arr);
        arrCopy.getJSONArray(0).add(9);
        arrCopy.getJSONObject(1).getJSONArray("a").clear();
        expect("C.deep", "JSONArray.deepCopy isolation", arr.toJSONString().equals("[[1,2],{\"a\":[3]}]"), arr.toJSONString() + " / copy " + arrCopy.toJSONString());

        JSONObject self = new JSONObject();
        self.put("self", self);
        String cyc = outcome(() -> invokeUnwrapped(self, "deepCopy", new Class[0]));
        ObjectNode jself = JACKSON.createObjectNode();
        jself.set("self", jself);
        String jcyc = outcome(jself::deepCopy);
        record("INFO", "C.deep", "self-referencing object", "fastjson2=" + cyc + " jackson=" + jcyc + " (same behavior)");

        // required
        JSONObject r = JSON.parseObject("{\"id\":1,\"name\":\"n\",\"nil\":null,\"big\":12345678901}");
        expect("C.req", "required(present)", Integer.valueOf(1).equals(JSONObject.class.getMethod("required", String.class).invoke(r, "id")), "");
        String missing = outcome(() -> invokeUnwrapped(r, "required", new Class[]{String.class}, "absent"));
        expect("C.req", "required(missing) -> JSONException", missing.startsWith("error:required value missing : absent"), missing);
        String nil = outcome(() -> invokeUnwrapped(r, "required", new Class[]{String.class}, "nil"));
        expect("C.req", "required(null value) -> JSONException", nil.startsWith("error:required value missing : nil"), nil);
        expect("C.req", "required(key, String.class)", "n".equals(invokeUnwrapped(r, "required", new Class[]{String.class, Class.class}, "name", String.class)), "");
        String wrong = outcome(() -> invokeUnwrapped(r, "required", new Class[]{String.class, Class.class}, "name", Integer.class));
        expect("C.req", "required(key, wrong type) -> JSONException", wrong.startsWith("error:required value not of type"), wrong);
        String intAsLong = outcome(() -> invokeUnwrapped(r, "required", new Class[]{String.class, Class.class}, "id", Long.class));
        String bigAsLong = outcome(() -> invokeUnwrapped(r, "required", new Class[]{String.class, Class.class}, "big", Long.class));
        record("INFO", "C.req", "required(\"id\", Long.class) on {\"id\":1}", intAsLong
                + "  (no numeric widening; {\"big\":12345678901} as Long -> " + bigAsLong + "; getLong/getObject would convert)");
        String jkNull = outcome(() -> JACKSON.readTree("{\"nil\":null}").required("nil").getNodeType().toString());
        String jkMissing = outcome(() -> JACKSON.readTree("{}").required("absent"));
        record("INFO", "C.req", "jackson required(): explicit null / missing", "null -> " + jkNull + ", missing -> " + jkMissing
                + " (fastjson2 rejects explicit null; stricter, documented)");

        // canConvertToInt / canConvertToLong vs jackson
        String json = "{\"i\":1,\"l1\":2147483647,\"l2\":2147483648,\"l3\":-2147483648,\"l4\":-2147483649,"
                + "\"b1\":9223372036854775807,\"b2\":9223372036854775808,\"b3\":-9223372036854775808,\"b4\":-9223372036854775809,"
                + "\"d1\":1.0,\"d2\":1.5,\"d3\":1e2,\"d4\":3.0E10,\"s\":\"1\",\"t\":true,\"n\":null}";
        JSONObject fo = JSON.parseObject(json);
        JsonNode jn = JACKSON.readTree(json);
        List<String> keys = new ArrayList<>(fo.keySet());
        keys.add("absent");
        for (String k : keys) {
            boolean fi = (Boolean) JSONObject.class.getMethod("canConvertToInt", String.class).invoke(fo, k);
            boolean fl = (Boolean) JSONObject.class.getMethod("canConvertToLong", String.class).invoke(fo, k);
            JsonNode node = jn.get(k);
            boolean ji = node != null && node.canConvertToInt();
            boolean jl = node != null && node.canConvertToLong();
            boolean jiExact = node != null && node.canConvertToInt() && node.canConvertToExactIntegral();
            boolean jlExact = node != null && node.canConvertToLong() && node.canConvertToExactIntegral();
            String v = String.valueOf(fo.get(k)) + (fo.get(k) == null ? "" : " (" + fo.get(k).getClass().getSimpleName() + ")");
            String detail = String.format("value=%-32s fastjson2 int/long=%s/%s  jackson=%s/%s  jackson+exactIntegral=%s/%s",
                    v, fi, fl, ji, jl, jiExact, jlExact);
            if (fi == ji && fl == jl) {
                record("PASS", "C.conv", "canConvertTo*(" + k + ")", detail);
            } else {
                // type-based (Byte/Short/Integer/Long/BigInteger) vs jackson's value/range-based check on floating nodes
                record("DIFF", "C.conv", "canConvertTo*(" + k + ")", detail);
            }
        }
        System.out.println();
    }

    static Object invokeUnwrapped(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        try {
            return target.getClass().getMethod(name, types).invoke(target, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable c = e.getCause();
            if (c instanceof Exception) {
                throw (Exception) c;
            }
            throw (Error) c;
        }
    }

    static void sectionAnySetter() {
        System.out.println("[D] native any-setter (@JSONField(unwrapped=true) on (String,Object) method)");
        AnyBean b = JSON.parseObject("{\"id\":7,\"x\":1,\"y\":{\"k\":\"v\"}}", AnyBean.class);
        expect("D", "unknown fields collected", b.id == 7 && b.extra.size() == 2 && b.extra.containsKey("y"), b.extra.toString());
        String strict = outcome(() -> JSON.parseObject("{\"id\":7,\"x\":1}", StrictBean.class, JSONReader.Feature.ErrorOnUnknownProperties));
        expect("D", "ErrorOnUnknownProperties still rejects on beans without any-setter", strict.startsWith("error:"), strict);
        String strictAny = outcome(() -> JSON.toJSONString(JSON.parseObject("{\"id\":7,\"x\":1}", AnyBean.class, JSONReader.Feature.ErrorOnUnknownProperties).extra));
        record("INFO", "D", "any-setter bean + ErrorOnUnknownProperties", strictAny);
        System.out.println();
    }

    static void sectionMasks() {
        System.out.println("[E] feature mask uniqueness");
        JSONReader.Feature dup = JSONReader.Feature.valueOf("ErrorOnDuplicateKeys");
        List<String> clash = new ArrayList<>();
        for (JSONReader.Feature f : JSONReader.Feature.values()) {
            if (f != dup && (f.mask & dup.mask) != 0) {
                clash.add(f.name());
            }
        }
        expect("E", "ErrorOnDuplicateKeys mask is exclusive", clash.isEmpty(), "1L<<" + Long.numberOfTrailingZeros(dup.mask) + " clashes=" + clash);
        clash.clear();
        for (JSONWriter.Feature f : JSONWriter.Feature.values()) {
            if (f != SORTED && (f.mask & SORTED.mask) != 0) {
                clash.add(f.name());
            }
        }
        expect("E", "SortFieldNamesAlphabetically mask is exclusive", clash.isEmpty(), "1L<<" + Long.numberOfTrailingZeros(SORTED.mask) + " clashes=" + clash);
        System.out.println();
    }
}
