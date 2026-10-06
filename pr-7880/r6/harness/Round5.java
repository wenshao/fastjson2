package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

import static verify.Verify7880.SORTED;
import static verify.Verify7880.expect;
import static verify.Verify7880.record;

/**
 * Round-5 additions: writer caches shared by the variants of a registered writer, field-level BeanToArray on bean
 * arrays, and the tree-conversion helper moved in b874205b6.
 */
public class Round5 {
    static void run() throws Exception {
        if (SORTED == null) {
            return;
        }
        sharedCaches();
        sameInstance();
        arrayPositional();
        treeHelper();
        apiSurface();
    }

    // ------------------------------------------------------------------ H1 shared caches of registered variants
    @JSONType(alphabetic = false)
    public static class Child {
        public int zebra = 3;
        public int apple = 1;
    }

    @JSONType(alphabetic = false)
    public static final class FChild {
        public int zebra = 3;
        public int apple = 1;
    }

    @JSONType(alphabetic = false) public static class HList { public int zulu = 9; public List<Child> f = new ArrayList<>(Collections.singletonList(new Child())); }
    @JSONType(alphabetic = false) public static class HArrayList { public int zulu = 9; public ArrayList<Child> f = new ArrayList<>(Collections.singletonList(new Child())); }
    @JSONType(alphabetic = false) public static class HSet { public int zulu = 9; public Set<Child> f = new LinkedHashSet<>(Collections.singletonList(new Child())); }
    @JSONType(alphabetic = false) public static class HColl { public int zulu = 9; public Collection<Child> f = new ArrayDeque<>(Collections.singletonList(new Child())); }
    @JSONType(alphabetic = false) public static class HArr { public int zulu = 9; public Child[] f = {new Child()}; }
    @JSONType(alphabetic = false) public static class HMap { public int zulu = 9; public Map<String, Child> f = Collections.singletonMap("k", new Child()); }
    @JSONType(alphabetic = false) public static class HObj { public int zulu = 9; public Child f = new Child(); }
    @JSONType(alphabetic = false) public static class HAny { public int zulu = 9; public Object f = new Child(); }
    @JSONType(alphabetic = false) public static class HOpt { public int zulu = 9; public Optional<Child> f = Optional.of(new Child()); }
    @JSONType(alphabetic = false) public static class HNested { public int zulu = 9; public List<List<Child>> f = Collections.singletonList(Collections.singletonList(new Child())); }
    @JSONType(alphabetic = false) public static class HListObj { public int zulu = 9; public List<Object> f = new ArrayList<>(Collections.singletonList(new Child())); }
    @JSONType(alphabetic = false) public static class HListMap { public int zulu = 9; public List<Map<String, Child>> f = Collections.singletonList(Collections.singletonMap("k", new Child())); }
    @JSONType(alphabetic = false) public static class HMapList { public int zulu = 9; public Map<String, List<Child>> f = Collections.singletonMap("k", Collections.singletonList(new Child())); }
    @JSONType(alphabetic = false) public static class HDates {
        public int zulu = 9;
        @JSONField(format = "yyyy-MM-dd")
        public List<Date> f = Collections.singletonList(new Date(0));
    }
    @JSONType(alphabetic = false) public static class HContentAs {
        public int zulu = 9;
        @JSONField(contentAs = Child.class)
        public List<Object> f = new ArrayList<>(Collections.singletonList(new Child()));
    }
    @JSONType(alphabetic = false) public static class FObj { public int zulu = 9; public FChild f = new FChild(); }
    @JSONType(alphabetic = false) public static class FList { public int zulu = 9; public List<FChild> f = new ArrayList<>(Collections.singletonList(new FChild())); }
    @JSONType(alphabetic = false) public static class FArr { public int zulu = 9; public FChild[] f = {new FChild()}; }
    @JSONType(alphabetic = false) public static class FMap { public int zulu = 9; public Map<String, FChild> f = Collections.singletonMap("k", new FChild()); }
    @JSONType(alphabetic = false) public static class FSet { public int zulu = 9; public Set<FChild> f = new LinkedHashSet<>(Collections.singletonList(new FChild())); }

    static Map<String, Supplier<Object>> shapes() {
        Map<String, Supplier<Object>> m = new LinkedHashMap<>();
        m.put("List<Child>", HList::new);
        m.put("ArrayList<Child>", HArrayList::new);
        m.put("Set<Child>", HSet::new);
        m.put("Collection<Child>", HColl::new);
        m.put("Child[]", HArr::new);
        m.put("Map<String,Child>", HMap::new);
        m.put("Child", HObj::new);
        m.put("Object=Child", HAny::new);
        m.put("Optional<Child>", HOpt::new);
        m.put("List<List<Child>>", HNested::new);
        m.put("List<Object>", HListObj::new);
        m.put("List<Map<String,Child>>", HListMap::new);
        m.put("Map<String,List<Child>>", HMapList::new);
        m.put("@format List<Date>", HDates::new);
        m.put("@contentAs List<Object>", HContentAs::new);
        m.put("final FChild", FObj::new);
        m.put("List<final FChild>", FList::new);
        m.put("final FChild[]", FArr::new);
        m.put("Map<String,final FChild>", FMap::new);
        m.put("Set<final FChild>", FSet::new);
        return m;
    }

    static String out(Object o, ObjectWriterProvider p, boolean sorted, String mode) {
        JSONWriter.Context ctx = sorted ? new JSONWriter.Context(p, SORTED) : new JSONWriter.Context(p);
        switch (mode) {
            case "json":
                return JSON.toJSONString(o, ctx);
            case "utf8":
                return new String(JSON.toJSONBytes(o, StandardCharsets.UTF_8, ctx), StandardCharsets.UTF_8);
            default:
                return JSON.toJSONString(JSONB.parse(JSONB.toBytes(o, ctx)));
        }
    }

    static void sharedCaches() {
        System.out.println("[H1] registered writers: natural and sorted variants share field writers (U-S-U-S, S-U-S-U; JSON, UTF-8, JSONB)");
        int asmNaturalBad = 0;
        StringBuilder asmDetail = new StringBuilder();
        for (Map.Entry<String, Supplier<Object>> e : shapes().entrySet()) {
            Supplier<Object> s = e.getValue();
            Class<?> type = s.get().getClass();
            StringBuilder bad = new StringBuilder();
            for (String mode : new String[]{"json", "utf8", "jsonb"}) {
                String refU = out(s.get(), new ObjectWriterProvider(), false, mode);
                String refS = out(s.get(), new ObjectWriterProvider(), true, mode);
                for (String seq : new String[]{"USUS", "SUSU"}) {
                    // reflective writer: a plain adapter, rebuilt for the sorted cell from the same field writers
                    ObjectWriterProvider p = new ObjectWriterProvider();
                    p.register(type, ObjectWriterCreator.INSTANCE.createObjectWriter(type));
                    // ASM writer: kept as registered in both cells (same instance)
                    ObjectWriterProvider pa = new ObjectWriterProvider();
                    pa.register(type, ObjectWriterCreatorASM.INSTANCE.createObjectWriter(type));
                    String firstU = null;
                    for (char c : seq.toCharArray()) {
                        boolean sorted = c == 'S';
                        String o = out(s.get(), p, sorted, mode);
                        if (!o.equals(sorted ? refS : refU)) {
                            bad.append(' ').append(mode).append(':').append(seq).append(':').append(c).append('=').append(o);
                        }
                        String a = out(s.get(), pa, sorted, mode);
                        if (!sorted) {
                            if (firstU == null) {
                                firstU = a;
                            }
                            if (!a.equals(refU)) {
                                asmNaturalBad++;
                                asmDetail.append(' ').append(e.getKey()).append(':').append(mode).append(':').append(seq).append('=').append(a);
                            }
                        }
                    }
                }
            }
            expect("H1", "registered reflective writer, " + e.getKey(), bad.length() == 0,
                    bad.length() == 0 ? "natural and sorted match unregistered output in all sequences" : bad.toString().trim());
        }
        expect("H1", "registered ASM writers: output without the feature unaffected by earlier sorted writes (20 shapes)",
                asmNaturalBad == 0, asmNaturalBad == 0 ? "ok" : asmDetail.toString().trim());
        record("INFO", "H1", "registered ASM writers under the feature", "served as registered (field order not sorted), by design since round 2");
        System.out.println();
    }

    // ------------------------------------------------------------------ H2 both cells hold the registered writer
    public static class AlphaArr { public int apple; public Child[] f = {new Child()}; }
    public static class AlphaList { public int apple; public List<Child> f = Collections.singletonList(new Child()); }
    public static class AlphaObj { public int apple; public Child f = new Child(); }
    public static class AlphaMap { public int apple; public Map<String, Child> f = Collections.singletonMap("k", new Child()); }

    static void sameInstance() {
        System.out.println("[H2] registered writer already in alphabetical order (same instance in both cells): a sorted write first");
        Supplier<?>[] cases = {AlphaArr::new, AlphaList::new, AlphaObj::new, AlphaMap::new};
        for (Supplier<?> s : cases) {
            Object o = s.get();
            String ref = out(o, new ObjectWriterProvider(), false, "json");
            ObjectWriterProvider p = new ObjectWriterProvider();
            p.register(o.getClass(), ObjectWriterCreator.INSTANCE.createObjectWriter(o.getClass()));
            out(o, p, true, "json");
            String after = out(o, p, false, "json");
            expect("H2", o.getClass().getSimpleName() + ": output without the feature after a sorted write", after.equals(ref),
                    "expected " + ref + " got " + after);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------ H3 field-level BeanToArray on bean arrays
    @JSONType(alphabetic = false)
    public static class Transfer {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    public static class ArrHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Transfer[] arr = {new Transfer()};
    }

    static void arrayPositional() {
        System.out.println("[H3] field-level @JSONField(serializeFeatures = BeanToArray) on a bean array, reflective creator");
        String natural = JSON.toJSONString(new ArrHolder(), new JSONWriter.Context(new ObjectWriterProvider(ObjectWriterCreator.INSTANCE)));
        record("INFO", "H3", "natural", natural);
        String cold = JSON.toJSONString(new ArrHolder(), new JSONWriter.Context(new ObjectWriterProvider(ObjectWriterCreator.INSTANCE), SORTED));
        ObjectWriterProvider p = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
        JSON.toJSONString(new ArrHolder(), new JSONWriter.Context(p));
        String warm = JSON.toJSONString(new ArrHolder(), new JSONWriter.Context(p, SORTED));
        expect("H3", "WITH feature, first write on the provider", cold.equals(natural), cold);
        expect("H3", "WITH feature, after a natural write", warm.equals(natural), warm);
        String asm = JSON.toJSONString(new ArrHolder(), new JSONWriter.Context(new ObjectWriterProvider(ObjectWriterCreatorASM.INSTANCE)));
        record("INFO", "H3", "ASM creator, natural (field BeanToArray not applied to array items, as on main)", asm);
        System.out.println();
    }

    // ------------------------------------------------------------------ H4 tree-conversion helper
    public static class Inner {
        public String note;
        public int rank = 1;
    }

    public static class Outer {
        public String title;
        public List<Inner> rows = Collections.singletonList(new Inner());
        public List<Map<String, Object>> maps = Collections.singletonList(Collections.<String, Object>singletonMap("k", null));
    }

    public static class Node {
        public String name;
        public Node peer;
    }

    static void treeHelper() throws Exception {
        System.out.println("[H4] tree conversion with global defaults (JSON.config)");
        JSON.config(JSONWriter.Feature.WriteNulls, true);
        try {
            String s = JSONObject.from(new Outer()).toString();
            String mainOut = "{\"maps\":[{\"k\":null}],\"rows\":[{\"note\":null,\"rank\":1}],\"title\":null}";
            expect("H4", "JSONObject.from(obj) under global WriteNulls == main", s.equals(mainOut), s);
        } finally {
            JSON.config(JSONWriter.Feature.WriteNulls, false);
        }
        Method helper = null;
        try {
            helper = ObjectWriterAdapter.class.getDeclaredMethod("toJSON", Object.class, long.class);
            helper.setAccessible(true);
        } catch (NoSuchMethodException e) {
            record("INFO", "H4", "ObjectWriterAdapter.toJSON(Object, long)", "not present");
        }
        if (helper != null) {
            Node a = new Node();
            Node b = new Node();
            a.name = "a";
            b.name = "b";
            a.peer = b;
            b.peer = a;
            JSON.config(JSONWriter.Feature.ReferenceDetection, true);
            String r;
            try {
                r = String.valueOf(helper.invoke(null, a, 0L));
            } catch (java.lang.reflect.InvocationTargetException e) {
                r = e.getCause().getClass().getSimpleName();
            } finally {
                JSON.config(JSONWriter.Feature.ReferenceDetection, false);
            }
            expect("H4", "helper on a two-node cycle under global ReferenceDetection", !r.contains("StackOverflowError"), r);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------ H5 API surface
    static void apiSurface() {
        System.out.println("[H5] API surface");
        boolean onInterface = false;
        for (Method m : JSON.class.getDeclaredMethods()) {
            if (m.getName().equals("toJSON") && Arrays.equals(m.getParameterTypes(), new Class[]{Object.class, long.class})) {
                onInterface = true;
            }
        }
        expect("H5", "JSON.toJSON(Object, long) is not part of the public API", !onInterface,
                onInterface ? "still declared on the JSON interface (public)" : "not declared on JSON");
        System.out.println();
    }
}
