package p11w;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Round-8 probe: does a field-level SortFieldNamesAlphabetically reach the beans nested in the field's value?
 * Oracle: the same value in an unannotated holder with the same declared type, written with the sort on the context.
 * Each shape is a pair of holders (S = @JSONField sort, P = plain) whose single field "f" has the declared type.
 */
public class Q11w {
    @JSONType(alphabetic = false)
    public static class B {
        public int z = 1;
        public int a = 2;
    }

    public static class G<T> {
        public T t;
        public int b = 0;
        G(T t) {
            this.t = t;
        }
    }

    static List<B> lb() {
        return new ArrayList<>(Collections.singletonList(new B()));
    }

    static <K, V> Map<K, V> m(K k, V v) {
        return new LinkedHashMap<>(Collections.singletonMap(k, v));
    }

    static final String SORT = "SortFieldNamesAlphabetically";

    // typed declarations
    public static class S01 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public List<List<B>> f = new ArrayList<>(Collections.singletonList(lb())); }
    public static class P01 { public List<List<B>> f = new ArrayList<>(Collections.singletonList(lb())); }
    public static class S02 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public AtomicReference<B> f = new AtomicReference<>(new B()); }
    public static class P02 { public AtomicReference<B> f = new AtomicReference<>(new B()); }
    public static class S03 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Optional<B> f = Optional.of(new B()); }
    public static class P03 { public Optional<B> f = Optional.of(new B()); }
    public static class S04 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public B[][] f = {{new B()}}; }
    public static class P04 { public B[][] f = {{new B()}}; }
    public static class S05 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public List<B[]> f = new ArrayList<>(Collections.singletonList(new B[]{new B()})); }
    public static class P05 { public List<B[]> f = new ArrayList<>(Collections.singletonList(new B[]{new B()})); }
    public static class S06 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Map<String, Map<String, B>> f = m("k", m("j", new B())); }
    public static class P06 { public Map<String, Map<String, B>> f = m("k", m("j", new B())); }
    public static class S07 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Map<String, List<B>> f = m("k", lb()); }
    public static class P07 { public Map<String, List<B>> f = m("k", lb()); }
    public static class S08 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public List<Map<String, B>> f = new ArrayList<>(Collections.singletonList(m("k", new B()))); }
    public static class P08 { public List<Map<String, B>> f = new ArrayList<>(Collections.singletonList(m("k", new B()))); }
    public static class S09 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Optional<List<B>> f = Optional.of(lb()); }
    public static class P09 { public Optional<List<B>> f = Optional.of(lb()); }
    public static class S10 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public AtomicReference<List<B>> f = new AtomicReference<>(lb()); }
    public static class P10 { public AtomicReference<List<B>> f = new AtomicReference<>(lb()); }
    public static class S11 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public G<B> f = new G<>(new B()); }
    public static class P11 { public G<B> f = new G<>(new B()); }
    public static class S12 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public G<List<B>> f = new G<>(lb()); }
    public static class P12 { public G<List<B>> f = new G<>(lb()); }
    public static class S13 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public List<Object> f = new ArrayList<>(Collections.singletonList(lb())); }
    public static class P13 { public List<Object> f = new ArrayList<>(Collections.singletonList(lb())); }
    public static class S14 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Map<String, Object> f = m("k", lb()); }
    public static class P14 { public Map<String, Object> f = m("k", lb()); }
    public static class S15 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public JSONArray f = JSONArray.of(lb()); }
    public static class P15 { public JSONArray f = JSONArray.of(lb()); }
    public static class S16 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public JSONObject f = JSONObject.of("k", lb()); }
    public static class P16 { public JSONObject f = JSONObject.of("k", lb()); }
    public static class S17 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Collection<B> f = new ArrayDeque<>(Collections.singletonList(new B())); }
    public static class P17 { public Collection<B> f = new ArrayDeque<>(Collections.singletonList(new B())); }
    public static class S18 { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Iterable<B> f = new ArrayList<>(lb()); }
    public static class P18 { public Iterable<B> f = new ArrayList<>(lb()); }
    // Object-typed declarations, value decided at run time
    public static class SO { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Object f; }
    public static class PO { public Object f; }

    static Map<String, Supplier<Object[]>> shapes() {
        Map<String, Supplier<Object[]>> s = new LinkedHashMap<>();
        s.put("List<List<B>>", () -> new Object[]{new S01(), new P01()});
        s.put("AtomicReference<B>", () -> new Object[]{new S02(), new P02()});
        s.put("Optional<B>", () -> new Object[]{new S03(), new P03()});
        s.put("B[][]", () -> new Object[]{new S04(), new P04()});
        s.put("List<B[]>", () -> new Object[]{new S05(), new P05()});
        s.put("Map<String,Map<String,B>>", () -> new Object[]{new S06(), new P06()});
        s.put("Map<String,List<B>>", () -> new Object[]{new S07(), new P07()});
        s.put("List<Map<String,B>>", () -> new Object[]{new S08(), new P08()});
        s.put("Optional<List<B>>", () -> new Object[]{new S09(), new P09()});
        s.put("AtomicReference<List<B>>", () -> new Object[]{new S10(), new P10()});
        s.put("G<B> (bean in bean)", () -> new Object[]{new S11(), new P11()});
        s.put("G<List<B>>", () -> new Object[]{new S12(), new P12()});
        s.put("List<Object>=[List<B>]", () -> new Object[]{new S13(), new P13()});
        s.put("Map<String,Object>={k:List<B>}", () -> new Object[]{new S14(), new P14()});
        s.put("JSONArray[List<B>]", () -> new Object[]{new S15(), new P15()});
        s.put("JSONObject{k:List<B>}", () -> new Object[]{new S16(), new P16()});
        s.put("Collection<B>(ArrayDeque)", () -> new Object[]{new S17(), new P17()});
        s.put("Iterable<B>", () -> new Object[]{new S18(), new P18()});
        Object[][] runtime = {
                {"Object=B", (Supplier<Object>) B::new},
                {"Object=List<B>", (Supplier<Object>) Q11w::lb},
                {"Object=List<List<B>>", (Supplier<Object>) () -> new ArrayList<>(Collections.singletonList(lb()))},
                {"Object=B[]", (Supplier<Object>) () -> new B[]{new B()}},
                {"Object=B[][]", (Supplier<Object>) () -> new B[][]{{new B()}}},
                {"Object=Object[]{List<B>}", (Supplier<Object>) () -> new Object[]{lb()}},
                {"Object=Map{k:B}", (Supplier<Object>) () -> m("k", new B())},
                {"Object=Map{k:List<B>}", (Supplier<Object>) () -> m("k", lb())},
                {"Object=Optional<B>", (Supplier<Object>) () -> Optional.of(new B())},
                {"Object=AtomicReference<B>", (Supplier<Object>) () -> new AtomicReference<>(new B())},
                {"Object=Set<B>", (Supplier<Object>) () -> new LinkedHashSet<>(lb())},
                {"Object=G<B>", (Supplier<Object>) () -> new G<>(new B())},
        };
        for (Object[] r : runtime) {
            @SuppressWarnings("unchecked") Supplier<Object> v = (Supplier<Object>) r[1];
            s.put((String) r[0], () -> {
                SO so = new SO();
                so.f = v.get();
                PO po = new PO();
                po.f = v.get();
                return new Object[]{so, po};
            });
        }
        return s;
    }

    static String out(Object o, JSONWriter.Context c, String ch) {
        try {
            switch (ch) {
                case "json":
                    return JSON.toJSONString(o, c);
                case "utf8":
                    return new String(JSON.toJSONBytes(o, StandardCharsets.UTF_8, c), StandardCharsets.UTF_8);
                default:
                    return JSON.toJSONString(JSONB.parse(JSONB.toBytes(o, c)));
            }
        } catch (Throwable e) {
            return "EXC " + e;
        }
    }

    public static void main(String[] args) {
        Map<String, Supplier<Object[]>> shapes = shapes();
        int cells = 0, same = 0;
        Map<String, List<String>> bad = new LinkedHashMap<>();
        Map<String, String> example = new LinkedHashMap<>();
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            for (String ch : new String[]{"json", "utf8", "jsonb"}) {
                for (Map.Entry<String, Supplier<Object[]>> e : shapes.entrySet()) {
                    Object[] h = e.getValue().get();
                    ObjectWriterProvider p = new ObjectWriterProvider(creator);
                    // warm: the natural writers of every nested type are cached before the field-level sorted write
                    out(e.getValue().get()[1], new JSONWriter.Context(p), ch);
                    out(new B(), new JSONWriter.Context(p), ch);
                    String a = out(h[0], new JSONWriter.Context(p), ch);
                    String b = out(h[1], new JSONWriter.Context(p, JSONWriter.Feature.SortFieldNamesAlphabetically), ch);
                    cells++;
                    if (a.equals(b)) {
                        same++;
                    } else {
                        bad.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(cn + "/" + ch);
                        example.putIfAbsent(e.getKey(), "field-level " + a + "  | context " + b);
                    }
                }
            }
        }
        System.out.println("V1w (natural writers warm) field-level sorted value == context-sorted value: shapes=" + shapes.size() + " cells=" + cells + " same=" + same);
        for (Map.Entry<String, List<String>> e : bad.entrySet()) {
            System.out.println(String.format("  %-32s %s", e.getKey(), e.getValue()));
            System.out.println("      " + example.get(e.getKey()));
        }
    }
}
