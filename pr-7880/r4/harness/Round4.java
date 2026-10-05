package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static verify.Verify7880.SORTED;
import static verify.Verify7880.expect;
import static verify.Verify7880.record;

/**
 * Round-4 additions: writer filters under the feature, and the tree-conversion fallback changed in 78104ea4e.
 */
public class Round4 {
    static void run() throws Exception {
        filters();
        treeFeatures();
        toJSONLongRoot();
        apiSurface();
    }

    // ------------------------------------------------------------------ G1 writer filters
    public static class Mask implements ValueFilter {
        public Object apply(Object o, String n, Object v) {
            return "password".equals(n) ? "***" : v;
        }
    }

    public static class Drop implements PropertyFilter {
        public boolean apply(Object o, String n, Object v) {
            return !"password".equals(n);
        }
    }

    public static class Rename implements NameFilter {
        public String process(Object o, String n, Object v) {
            return "password".equals(n) ? "secret" : n;
        }
    }

    public static class GA { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class GA2 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class GA3 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class GAHolder { public GA user = new GA(); }
    public static class GB { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class GB2 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class GB3 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class GC { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    @JSONType(alphabetic = false, serializeFilters = Mask.class)
    public static class GD { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    @JSONType(alphabetic = false, serializeFilters = Mask.class)
    public static class GE { public long id = 7; public String password = "hunter2"; public String name = "alice"; }

    static String bytes(Object o, JSONWriter.Feature... f) {
        return new String(JSON.toJSONBytes(o, f), StandardCharsets.UTF_8);
    }

    static String jsonb(Object o, JSONWriter.Context ctx) {
        return JSON.toJSONString(JSONB.parse(JSONB.toBytes(o, ctx)));
    }

    // writer-level filters are not applied on the JSONB path on main either; only parity with the natural order is checked
    static void jsonbParity(String what, String natural, String sorted) {
        if (leaks(natural)) {
            record("INFO", "G1", what + ": JSONB natural (pre-existing, filters not applied to JSONB)", natural);
        }
        expect("G1", what + ": JSONB WITH feature same as natural", leaks(natural) == leaks(sorted), "natural=" + natural + " sorted=" + sorted);
    }

    static boolean leaks(String s) {
        return s.contains("hunter2") && !s.contains("secret");
    }

    static void filters() {
        System.out.println("[G1] writer filters under the feature");

        // JSON.register(Class, Filter): the documented way to attach a filter to a type
        JSON.register(GA.class, new Mask());
        String natural = JSON.toJSONString(new GA());
        expect("G1", "JSON.register(Class, ValueFilter): natural", !leaks(natural), natural);
        String[][] sorted = {
                {"toJSONString", JSON.toJSONString(new GA(), SORTED)},
                {"toJSONBytes", bytes(new GA(), SORTED)},
                {"nested field", JSON.toJSONString(new GAHolder(), SORTED)},
        };
        for (String[] r : sorted) {
            expect("G1", "JSON.register(Class, ValueFilter) WITH feature: " + r[0], !leaks(r[1]), r[1]);
        }
        jsonbParity("JSON.register(Class, ValueFilter)",
                JSON.toJSONString(JSONB.parse(JSONB.toBytes(new GA()))),
                JSON.toJSONString(JSONB.parse(JSONB.toBytes(new GA(), SORTED))));

        // sorted variant already warm when the filter is registered
        String warm = JSON.toJSONString(new GA2(), SORTED);
        record("INFO", "G1", "GA2 written sorted before the filter", warm);
        JSON.register(GA2.class, new Drop());
        String n2 = JSON.toJSONString(new GA2());
        String s2 = JSON.toJSONString(new GA2(), SORTED);
        expect("G1", "JSON.register(Class, PropertyFilter) after sorted warm-up: natural", !leaks(n2), n2);
        expect("G1", "JSON.register(Class, PropertyFilter) after sorted warm-up: WITH feature", !leaks(s2), s2);

        JSON.register(GA3.class, new Rename());
        String n3 = JSON.toJSONString(new GA3());
        String s3 = JSON.toJSONString(new GA3(), SORTED);
        expect("G1", "JSON.register(Class, NameFilter): WITH feature renames like natural",
                n3.contains("secret") == s3.contains("secret"), "natural=" + n3 + " sorted=" + s3);

        // the same mechanism on a custom provider: setFilter on the writer it hands out
        ObjectWriterProvider pc = new ObjectWriterProvider();
        pc.getObjectWriter(GC.class).setFilter(new Mask());
        String nc = JSON.toJSONString(new GC(), new JSONWriter.Context(pc));
        String sc = JSON.toJSONString(new GC(), new JSONWriter.Context(pc, SORTED));
        String jcn = jsonb(new GC(), new JSONWriter.Context(pc));
        String jc = jsonb(new GC(), new JSONWriter.Context(pc, SORTED));
        expect("G1", "provider.getObjectWriter(T).setFilter: natural", !leaks(nc), nc);
        expect("G1", "provider.getObjectWriter(T).setFilter: WITH feature", !leaks(sc), sc);
        jsonbParity("provider.getObjectWriter(T).setFilter", jcn, jc);

        // registered ObjectWriters adapter that carries its own filter; field order not alphabetical
        ObjectWriter wb = ObjectWriters.objectWriter(GB.class,
                ObjectWriters.fieldWriter("password", String.class, (GB u) -> u.password),
                ObjectWriters.fieldWriter("name", String.class, (GB u) -> u.name));
        wb.setFilter(new Mask());
        ObjectWriterProvider pb = new ObjectWriterProvider();
        pb.register(GB.class, wb);
        String nb = JSON.toJSONString(new GB(), new JSONWriter.Context(pb));
        String sb = JSON.toJSONString(new GB(), new JSONWriter.Context(pb, SORTED));
        expect("G1", "registered adapter with filter: natural", !leaks(nb), nb);
        expect("G1", "registered adapter with filter: WITH feature", !leaks(sb), sb);
        ObjectWriterProvider pb2 = new ObjectWriterProvider();
        pb2.registerIfAbsent(GB.class, wb);
        String sb2 = JSON.toJSONString(new GB(), new JSONWriter.Context(pb2, SORTED));
        expect("G1", "registerIfAbsent adapter with filter: WITH feature", !leaks(sb2), sb2);

        // filter attached after registration
        ObjectWriter wb3 = ObjectWriters.objectWriter(GB3.class,
                ObjectWriters.fieldWriter("password", String.class, (GB3 u) -> u.password),
                ObjectWriters.fieldWriter("name", String.class, (GB3 u) -> u.name));
        ObjectWriterProvider pb3 = new ObjectWriterProvider();
        pb3.register(GB3.class, wb3);
        wb3.setFilter(new Drop());
        String nb3 = JSON.toJSONString(new GB3(), new JSONWriter.Context(pb3));
        String sb3 = JSON.toJSONString(new GB3(), new JSONWriter.Context(pb3, SORTED));
        expect("G1", "adapter filter set after register: natural", !leaks(nb3), nb3);
        expect("G1", "adapter filter set after register: WITH feature", !leaks(sb3), sb3);

        // a creator-built writer of a @JSONType(serializeFilters) bean, registered explicitly
        ObjectWriterCreator[] creators = {ObjectWriterCreator.INSTANCE, ObjectWriterCreatorASM.INSTANCE};
        String[] names = {"reflect", "asm"};
        for (int i = 0; i < creators.length; i++) {
            ObjectWriterProvider pd = new ObjectWriterProvider();
            pd.register(GD.class, creators[i].createObjectWriter(GD.class));
            String nd = JSON.toJSONString(new GD(), new JSONWriter.Context(pd));
            String sd = JSON.toJSONString(new GD(), new JSONWriter.Context(pd, SORTED));
            expect("G1", "registered " + names[i] + " writer with @JSONType filter: natural", !leaks(nd), nd);
            expect("G1", "registered " + names[i] + " writer with @JSONType filter: WITH feature", !leaks(sd), sd);
        }

        // controls: annotation-only filter, and a filter passed per call
        String se = JSON.toJSONString(new GE(), SORTED);
        expect("G1", "control: @JSONType(serializeFilters) WITH feature", !leaks(se), se);
        String sf = JSON.toJSONString(new GB2(), new Mask(), SORTED);
        expect("G1", "control: per-call filter WITH feature", !leaks(sf), sf);
        System.out.println();
    }

    // ------------------------------------------------------------------ G2 tree conversion with caller features
    public enum Color {
        RED;

        @Override
        public String toString() {
            return "red!";
        }
    }

    public static class Rec {
        public long id = 9007199254740993L;
        public List<Long> ids = Arrays.asList(1L, 9007199254740993L);
        public List<Map<String, Object>> rows = Collections.singletonList(Collections.<String, Object>singletonMap("n", 5L));
        public BigDecimal amount = new BigDecimal("1E+2");
        public List<BigDecimal> amounts = Collections.singletonList(new BigDecimal("1E+2"));
        public Color color = Color.RED;
        public List<Color> colors = Collections.singletonList(Color.RED);
        public int zero;
        public List<Integer> zeros = Collections.singletonList(0);
    }

    @JSONType(alphabetic = false)
    public static class SortChild {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class NestedSortHolder {
        public List<List<SortChild>> rows = Collections.singletonList(Collections.singletonList(new SortChild()));
        public List<Map<String, SortChild>> indexBy = Collections.singletonList(Collections.singletonMap("k", new SortChild()));
    }

    static void treeFeatures() {
        System.out.println("[G2] JSONObject.from(obj, features): value-format features (as on main, not applied to the tree)");
        String plain = JSONObject.from(new Rec()).toString();
        record("INFO", "G2", "from(rec)", plain);
        JSONWriter.Feature[] fs = {
                JSONWriter.Feature.WriteLongAsString,
                JSONWriter.Feature.WriteNonStringValueAsString,
                JSONWriter.Feature.BrowserCompatible,
                JSONWriter.Feature.WriteBigDecimalAsPlain,
                JSONWriter.Feature.WriteEnumUsingToString,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.WriteClassName,
        };
        for (JSONWriter.Feature f : fs) {
            String s = JSONObject.from(new Rec(), f).toString();
            expect("G2", "from(rec, " + f + ") == from(rec)", s.equals(plain), s);
        }
        if (SORTED != null) {
            String s = JSONObject.from(new NestedSortHolder(), SORTED).toString();
            String t = JSON.toJSONString(new NestedSortHolder(), SORTED);
            expect("G2", "from(obj, Sort) sorts beans inside nested lists/maps", s.equals(t), "from=" + s + " toJSONString=" + t);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------ G3 JSON.toJSON(Object, long) at the root
    static void toJSONLongRoot() throws Exception {
        System.out.println("[G3] JSON.toJSON(Object, long) at the root vs the Feature... overload");
        Method m;
        try {
            m = JSON.class.getMethod("toJSON", Object.class, long.class);
        } catch (NoSuchMethodException e) {
            record("INFO", "G3", "JSON.toJSON(Object, long)", "not present");
            System.out.println();
            return;
        }
        List<Object> list = new ArrayList<>();
        list.add(new SortChild());
        list.add(9007199254740993L);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("n", null);
        map.put("c", new SortChild());
        List<JSONWriter.Feature> fs = new ArrayList<>(Arrays.asList(JSONWriter.Feature.WriteLongAsString, JSONWriter.Feature.WriteNulls));
        if (SORTED != null) {
            fs.add(SORTED);
        }
        for (JSONWriter.Feature f : fs) {
            for (Object root : new Object[]{list, map}) {
                String viaLong = String.valueOf(m.invoke(null, root, f.mask));
                String viaVarargs = String.valueOf(JSON.toJSON(root, f));
                expect("G3", "toJSON(" + root.getClass().getSimpleName() + ", " + f + ".mask) == toJSON(.., " + f + ")",
                        viaLong.equals(viaVarargs), "long=" + viaLong + " varargs=" + viaVarargs);
            }
        }
        System.out.println();
    }

    // ------------------------------------------------------------------ G4 API surface
    static void apiSurface() {
        System.out.println("[G4] API surface");
        for (Method m : JSON.class.getDeclaredMethods()) {
            if (m.getName().equals("toJSON") && Arrays.equals(m.getParameterTypes(), new Class[]{Object.class, long.class})) {
                record("INFO", "G4", "JSON.toJSON(Object, long) modifiers", Modifier.toString(m.getModifiers())
                        + " (JSON is an interface: members without a modifier are public)");
            }
        }
        for (Method m : JSONReader.class.getDeclaredMethods()) {
            if (m.getName().equals("readObject") && Arrays.equals(m.getParameterTypes(), new Class[]{long.class})) {
                record("INFO", "G4", "JSONReader.readObject(long) modifiers", Modifier.toString(m.getModifiers()));
            }
        }
        System.out.println();
    }
}
