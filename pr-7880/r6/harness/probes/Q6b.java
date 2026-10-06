package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.*;

/** Second pass: field-level annotations, warm/cold paths and the main baseline. */
public class Q6b {
    static JSONWriter.Feature SORT;
    static {
        try { SORT = JSONWriter.Feature.valueOf("SortFieldNamesAlphabetically"); } catch (IllegalArgumentException e) { SORT = null; }
    }

    static void p(String label, Q6.Call c) { Q6.p(label, c); }

    @JSONType(alphabetic = false) public static class Item { public int zebra = 3; public int apple = 1; }

    // R1-7: field-level ErrorOnDuplicateKeys only (no context feature)
    public static class FrameDup { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Object payload; }

    // R1-1 / R2-25: field-level Sort only
    public static class Host {
        @JSONField(contentAs = Item.class, serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public List<Object> contentAsList = new ArrayList<>(Collections.singletonList(new Item()));
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Item single = new Item();
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public List<Item> typed = new ArrayList<>(Collections.singletonList(new Item()));
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Item[] array = {new Item()};
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<String, Item> map = Collections.singletonMap("k", new Item());
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Set<Item> set = new LinkedHashSet<>(Collections.singletonList(new Item()));
    }

    // R2-1
    @JSONType(alphabetic = false) public static class Base { public int zebra = 3; }
    @JSONType(alphabetic = false) public static class Sub extends Base { public int apple = 1; }
    public static class HolderSuper { public Base data = new Sub(); }
    public static class HolderSuper2 { public Base data = new Sub(); }
    public static class HolderSuper3 { public Base data = new Sub(); }

    // R2-33
    public static class PInner { private int hidden = 42; public String name = "n"; }
    public static class PInner$$EnhancerByCGLIB$$probe extends PInner { }
    public static class QInner { private int hidden = 42; public String name = "n"; }
    public static class QInner$$EnhancerByCGLIB$$probe extends QInner { }

    // R2-12
    public static class Mixed { public int zebra = 3; public int apple = 1; }
    public static class HideZebra { @JSONField(serialize = false) public int zebra; }

    static String w(Object o, ObjectWriterProvider p, JSONWriter.Feature... f) {
        return JSON.toJSONString(o, new JSONWriter.Context(p, f));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("fastjson2 jar: " + JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", ""));
        System.out.println("R2-1 polymorphic field, tree conversion");
        p("cold JSONObject.from(h)", () -> JSONObject.from(new HolderSuper()));
        p("toJSONString(h2) then JSONObject.from(h2)", () -> { JSON.toJSONString(new HolderSuper2()); return JSONObject.from(new HolderSuper2()); });
        if (SORT != null) {
            p("toJSONString(h3,Sort) then JSONObject.from(h3,Sort)", () -> { JSON.toJSONString(new HolderSuper3(), SORT); return JSONObject.from(new HolderSuper3(), SORT); });
            p("toJSONString(h3, Sort)", () -> JSON.toJSONString(new HolderSuper3(), SORT));
        }

        System.out.println("R2-33 proxy, FieldBased");
        p("warm target FB, then proxy FB", () -> { JSON.toJSONString(new PInner(), JSONWriter.Feature.FieldBased); return JSON.toJSONString(new PInner$$EnhancerByCGLIB$$probe(), JSONWriter.Feature.FieldBased); });
        if (SORT != null) {
            p("warm target FB only, then proxy FB+Sort", () -> JSON.toJSONString(new PInner$$EnhancerByCGLIB$$probe(), JSONWriter.Feature.FieldBased, SORT));
            p("warm target FB+Sort, then proxy FB+Sort", () -> { JSON.toJSONString(new QInner(), JSONWriter.Feature.FieldBased, SORT); return JSON.toJSONString(new QInner$$EnhancerByCGLIB$$probe(), JSONWriter.Feature.FieldBased, SORT); });
        }

        System.out.println("R2-12 mixIn after first write (natural mode with a class mixin)");
        for (boolean fb : new boolean[]{false, true}) {
            p("mode " + (fb ? "FieldBased" : "natural"), () -> {
                ObjectWriterProvider pr = new ObjectWriterProvider();
                JSONWriter.Feature[] fa = fb ? new JSONWriter.Feature[]{JSONWriter.Feature.FieldBased} : new JSONWriter.Feature[0];
                String before = w(new Mixed(), pr, fa);
                pr.mixIn(Mixed.class, HideZebra.class);
                return before + " -> " + w(new Mixed(), pr, fa);
            });
        }

        if (SORT == null) {
            return;
        }
        System.out.println("R1-7 field-level ErrorOnDuplicateKeys, no context feature");
        p("{payload:{a:1,a:2}}", () -> JSON.parseObject("{\"payload\":{\"a\":1,\"a\":2}}", FrameDup.class).payload);
        p("{payload:[{a:1,a:2}]}", () -> JSON.parseObject("{\"payload\":[{\"a\":1,\"a\":2}]}", FrameDup.class).payload);

        System.out.println("R1-1 / R2-25 field-level Sort only, no context feature");
        p("JSON", () -> JSON.toJSONString(new Host()));
        p("JSONB -> JSON", () -> JSONB.parseObject(JSONB.toBytes(new Host())).toString());
    }
}
