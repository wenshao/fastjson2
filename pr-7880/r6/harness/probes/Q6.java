package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** Independent probes for the 9 "Critical" claims of the 15:34 automated review, run on main and on the PR head. */
public class Q6 {
    static JSONWriter.Feature SORT;
    static JSONReader.Feature DUP;
    static {
        try { SORT = JSONWriter.Feature.valueOf("SortFieldNamesAlphabetically"); } catch (IllegalArgumentException e) { SORT = null; }
        try { DUP = JSONReader.Feature.valueOf("ErrorOnDuplicateKeys"); } catch (IllegalArgumentException e) { DUP = null; }
    }

    interface Call { Object call() throws Exception; }

    static void p(String label, Call c) {
        String out;
        try {
            Object o = c.call();
            out = String.valueOf(o);
        } catch (Throwable e) {
            out = "THROWS " + e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()).replace('\n', ' ');
            if (out.length() > 140) out = out.substring(0, 140);
        }
        System.out.printf("  %-58s %s%n", label, out);
    }

    // R2-26
    public static class Metrics { public AtomicLong count = new AtomicLong(6); }

    // R1-7
    public static class Frame { @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch) public Object payload; }

    // R2-11
    public static class ObjBox { public Object data; }

    // R1-1
    @JSONType(alphabetic = false) public static class Item { public int zebra = 3; public int apple = 1; }
    public static class Host {
        @JSONField(contentAs = Item.class) public List<Object> contentAsList = new ArrayList<>(Collections.singletonList(new Item()));
        public Item single = new Item();
        public List<Item> typed = new ArrayList<>(Collections.singletonList(new Item()));
    }

    // R2-30
    @JSONType(alphabetic = false) public static class MapBean { public Map<String, Item> map = new LinkedHashMap<>(); public int id = 9; { map.put("k", new Item()); } }

    // R1-8 (3)
    @JSONType(alphabetic = false) public static class Inner2 { public int zz = 1; public int aa = 2; }
    @JSONType(alphabetic = false) public static class Unwrapped { public int top = 0; @JSONField(unwrapped = true) public Inner2 inner = new Inner2(); }

    // R2-1
    @JSONType(alphabetic = false) public static class Base { public int zebra = 3; }
    @JSONType(alphabetic = false) public static class Sub extends Base { public int apple = 1; }
    public static class HolderSuper { public Base data = new Sub(); }

    // R2-12
    @JSONType(alphabetic = false) public static class Mixed { private int zebra = 3; private int apple = 1; public int getZebra() { return zebra; } public int getApple() { return apple; } }
    public interface HideZebra { @JSONField(serialize = false) int getZebra(); }
    public static class HideZebraFields { @JSONField(serialize = false) private int zebra; }

    // R2-33
    public static class PInner { private int hidden = 42; public String name = "n"; }
    public static class PInner$$EnhancerByCGLIB$$probe extends PInner { }

    static String w(Object o, ObjectWriterProvider p, JSONWriter.Feature... f) {
        return JSON.toJSONString(o, new JSONWriter.Context(p, f));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("fastjson2 jar: " + JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", ""));

        System.out.println("R2-26 canConvertToLong on a raw AtomicLong");
        p("JSON.toJSON(metrics) value class", () -> ((JSONObject) JSON.toJSON(new Metrics())).get("count").getClass().getSimpleName());
        p("JSONObject.from(metrics) value class", () -> JSONObject.from(new Metrics()).get("count").getClass().getSimpleName());
        p("o.put(count, AtomicLong(6)).canConvertToLong", () -> { JSONObject o = new JSONObject(); o.put("count", new AtomicLong(6)); return JSONObject.class.getMethod("canConvertToLong", String.class).invoke(o, "count"); });
        p("o.getLongValue(count)", () -> { JSONObject o = new JSONObject(); o.put("count", new AtomicLong(6)); return o.getLongValue("count"); });

        System.out.println("R1-7 ErrorOnDuplicateKeys and objects inside arrays");
        if (DUP != null) {
            p("parse({payload:[{a:1,a:2}]}, Frame, DUP)", () -> JSON.parseObject("{\"payload\":[{\"a\":1,\"a\":2}]}", Frame.class, DUP).payload);
            p("parseObject([{a:1,a:2}] inside object, DUP)", () -> JSON.parseObject("{\"x\":[{\"a\":1,\"a\":2}]}", DUP));
            p("parseArray([{a:1,a:2}], DUP)", () -> JSON.parseArray("[{\"a\":1,\"a\":2}]", DUP));
            p("parseObject({p:{a:1,a:2}}, Frame, DUP)", () -> JSON.parseObject("{\"payload\":{\"a\":1,\"a\":2}}", Frame.class, DUP).payload);
        }

        System.out.println("R2-11 duplicated @type under ErrorOnDuplicateKeys");
        if (DUP != null) {
            p("parseObject({data:{@type:x,@type:y}}, ObjBox, DUP)", () -> JSON.toJSONString(JSON.parseObject("{\"data\":{\"@type\":\"com.example.NoSuchType\",\"@type\":\"other\"}}", ObjBox.class, DUP).data));
            p("parse({@type:a,@type:b}, DUP)", () -> JSON.parse("{\"@type\":\"a\",\"@type\":\"b\"}", DUP));
            p("parseObject({@type:a,@type:b}, DUP)", () -> JSON.parseObject("{\"@type\":\"a\",\"@type\":\"b\"}", DUP));
            p("parseObject({k:1,k:2}, DUP) (control)", () -> JSON.parseObject("{\"k\":1,\"k\":2}", DUP));
        }

        ObjectWriterProvider dp = JSONFactory.getDefaultObjectWriterProvider();
        if (SORT != null) {
            System.out.println("R1-1 contentAs list, context-level Sort (field-level annotation case needs Sort on the field)");
            p("toJSONString(host, Sort)", () -> JSON.toJSONString(new Host(), SORT));

            System.out.println("R2-30 Map<String,Item> field with WriteClassName");
            p("WriteClassName", () -> JSON.toJSONString(new MapBean(), JSONWriter.Feature.WriteClassName));
            p("WriteClassName + Sort", () -> JSON.toJSONString(new MapBean(), JSONWriter.Feature.WriteClassName, SORT));

            System.out.println("R1-8 unwrapped field in tree conversion");
            p("toJSONString(u, Sort)", () -> JSON.toJSONString(new Unwrapped(), SORT));
            p("JSONObject.from(u, Sort)", () -> JSONObject.from(new Unwrapped(), SORT));
            p("JSONObject.from(u)", () -> JSONObject.from(new Unwrapped()));

            System.out.println("R2-1 polymorphic field in tree conversion under Sort");
            p("JSONObject.from(h) (natural, warms)", () -> JSONObject.from(new HolderSuper()));
            p("JSONObject.from(h, Sort)", () -> JSONObject.from(new HolderSuper(), SORT));
            p("toJSONString(h, Sort)", () -> JSON.toJSONString(new HolderSuper(), SORT));
            p("JSONObject.from(h, Sort) cold provider", () -> { JSON.config(JSONWriter.Feature.WriteNulls, false); return JSONObject.from(new HolderSuper(), SORT); });

            System.out.println("R2-33 CGLIB-named proxy with FieldBased + Sort");
            p("toJSONString(proxy, FieldBased)", () -> JSON.toJSONString(new PInner$$EnhancerByCGLIB$$probe(), JSONWriter.Feature.FieldBased));
            p("toJSONString(proxy, FieldBased, Sort)", () -> JSON.toJSONString(new PInner$$EnhancerByCGLIB$$probe(), JSONWriter.Feature.FieldBased, SORT));
            p("cold provider: proxy FieldBased+Sort", () -> w(new PInner$$EnhancerByCGLIB$$probe(), new ObjectWriterProvider(), JSONWriter.Feature.FieldBased, SORT));
            p("cold provider: target FieldBased+Sort", () -> w(new PInner(), new ObjectWriterProvider(), JSONWriter.Feature.FieldBased, SORT));
        }
        p("main control: cold provider: proxy FieldBased", () -> w(new PInner$$EnhancerByCGLIB$$probe(), new ObjectWriterProvider(), JSONWriter.Feature.FieldBased));

        System.out.println("R2-12 mixIn after first write, per cache cell");
        for (int mode = 0; mode < 4; mode++) {
            final int m = mode;
            boolean fb = (m & 1) != 0, srt = (m & 2) != 0;
            if (srt && SORT == null) continue;
            p("mode " + (fb ? "FieldBased" : "") + (srt ? "+Sort" : "") + (m == 0 ? "natural" : ""), () -> {
                ObjectWriterProvider p = new ObjectWriterProvider();
                List<JSONWriter.Feature> fs = new ArrayList<>();
                if (fb) fs.add(JSONWriter.Feature.FieldBased);
                if (srt) fs.add(SORT);
                JSONWriter.Feature[] fa = fs.toArray(new JSONWriter.Feature[0]);
                String before = w(new Mixed(), p, fa);
                p.mixIn(Mixed.class, fb ? HideZebraFields.class : HideZebra.class);
                String after = w(new Mixed(), p, fa);
                ObjectWriterProvider cold = new ObjectWriterProvider();
                cold.mixIn(Mixed.class, fb ? HideZebraFields.class : HideZebra.class);
                return before + " -> after mixIn " + after + "  (cold: " + w(new Mixed(), cold, fa) + ")";
            });
        }
    }
}
