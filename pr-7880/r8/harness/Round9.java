package verify;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static verify.Verify7880.expect;
import static verify.Verify7880.record;

/**
 * Round-8 additions for 61e081039 (section L): beans nested in map keys and values under a field-level
 * SortFieldNamesAlphabetically. The oracle is the same document written with the sort on the context.
 * L1 container map keys (the field-level sort branch of ObjectWriterImplMap#mapKeyToString), with and without a
 * context filter (a filter sends the write through the context, where the sort is merged into the context features).
 * L2 (INFO) value shapes where the field-level sort stops before the nested beans, per creator.
 */
public class Round9 {
    @JSONType(alphabetic = false)
    public static class B {
        public int z = 1;
        public int a = 2;
    }

    public static class HS {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<Object, String> m = new LinkedHashMap<>();
    }

    public static class HP {
        public Map<Object, String> m = new LinkedHashMap<>();
    }

    public static class VS {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Object m;
    }

    public static class VP {
        public Object m;
    }

    static List<B> lb() {
        return new ArrayList<>(Collections.singletonList(new B()));
    }

    static Map<String, Supplier<Object>> containers() {
        Map<String, Supplier<Object>> k = new LinkedHashMap<>();
        k.put("List<B>", Round9::lb);
        k.put("Set<B>", () -> new LinkedHashSet<>(lb()));
        k.put("B[]", () -> new B[]{new B()});
        k.put("Object[]{B}", () -> new Object[]{new B(), 1});
        k.put("Map{k:B}", () -> new LinkedHashMap<>(Collections.singletonMap("k", new B())));
        k.put("JSONObject{k:B}", () -> JSONObject.of("k", new B()));
        k.put("Optional<B>", () -> Optional.of(new B()));
        k.put("Map{k:List<B>}", () -> new LinkedHashMap<>(Collections.singletonMap("k", lb())));
        return k;
    }

    static Map<String, Supplier<Object>> nested() {
        Map<String, Supplier<Object>> k = new LinkedHashMap<>(containers());
        k.put("List<List<B>>", () -> new ArrayList<>(Collections.singletonList(lb())));
        k.put("AtomicReference<B>", () -> new AtomicReference<>(new B()));
        k.put("Optional<List<B>>", () -> Optional.of(lb()));
        k.put("B (bean)", B::new);
        return k;
    }

    static JSONWriter.Context ctx(ObjectWriterProvider p, boolean sort, boolean filter) {
        JSONWriter.Context c = sort ? new JSONWriter.Context(p, JSONWriter.Feature.SortFieldNamesAlphabetically) : new JSONWriter.Context(p);
        if (filter) {
            c.configFilter((PropertyFilter) (o, n, v) -> true);
        }
        return c;
    }

    static String norm(String s) {
        return s.replace("Round9$HS", "H").replace("Round9$HP", "H").replace("Round9$VS", "H").replace("Round9$VP", "H");
    }

    static ObjectWriterCreator[] creators() {
        return new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE};
    }

    static String name(ObjectWriterCreator c) {
        return c == ObjectWriterCreator.INSTANCE ? "reflect" : "ASM";
    }

    static void run() {
        System.out.println("[L1] field-level sorted container map keys: nested beans sorted as with the sort on the context");
        for (ObjectWriterCreator creator : creators()) {
            for (boolean filter : new boolean[]{false, true}) {
                List<String> bad = new ArrayList<>();
                for (Map.Entry<String, Supplier<Object>> e : containers().entrySet()) {
                    ObjectWriterProvider p = new ObjectWriterProvider(creator);
                    HS hs = new HS();
                    hs.m.put(e.getValue().get(), "v");
                    HP hp = new HP();
                    hp.m.put(e.getValue().get(), "v");
                    String a = norm(JSON.toJSONString(hs, ctx(p, false, filter)));
                    String b = norm(JSON.toJSONString(hp, ctx(p, true, filter)));
                    if (!a.equals(b)) {
                        bad.add(e.getKey() + " " + a);
                    }
                }
                expect("L1", name(creator) + (filter ? ", context filter" : ", no filter") + ": " + containers().size() + " key shapes",
                        bad.isEmpty(), bad.isEmpty() ? "" : bad.size() + " differ: " + bad);
            }
        }
        System.out.println();

        System.out.println("[L2] (INFO) shapes where a field-level sort does not reach the nested beans (key / value)");
        for (ObjectWriterCreator creator : creators()) {
            List<String> keys = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (Map.Entry<String, Supplier<Object>> e : nested().entrySet()) {
                ObjectWriterProvider p = new ObjectWriterProvider(creator);
                HS hs = new HS();
                hs.m.put(e.getValue().get(), "v");
                HP hp = new HP();
                hp.m.put(e.getValue().get(), "v");
                if (!norm(JSON.toJSONString(hs, ctx(p, false, false))).equals(norm(JSON.toJSONString(hp, ctx(p, true, false))))) {
                    keys.add(e.getKey());
                }
                VS vs = new VS();
                vs.m = e.getValue().get();
                VP vp = new VP();
                vp.m = e.getValue().get();
                if (!norm(JSON.toJSONString(vs, ctx(p, false, false))).equals(norm(JSON.toJSONString(vp, ctx(p, true, false))))) {
                    values.add(e.getKey());
                }
            }
            record("INFO", "L2", name(creator) + ": " + nested().size() + " shapes, Object-typed field",
                    "keys " + keys + "  values " + values);
        }
        System.out.println();
    }
}
