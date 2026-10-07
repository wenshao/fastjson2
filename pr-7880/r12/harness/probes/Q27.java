package p27;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;

/**
 * Round 12 (302223125): the behaviors changed by fa7e339e3 and 01f410791.
 * A  bean keys inside container map keys under the sort feature (context-level vs field-level), per creator
 * B  scalar map keys: spelling with and without the sort feature
 * D  canConvertToInt / canConvertToLong on Number types other than the JDK boxes
 * E  JSONObject.required with primitive class literals
 * F  tree conversion with SortMapEntriesByKeys vs toJSONString
 * G  type-level @JSONType(serializeFeatures = SortFieldNamesAlphabetically), per creator
 * H  the default-order sentence of the SortFieldNamesAlphabetically javadoc
 * J  the typed map-value memo under alternating variants on one provider
 * K  the per-field sorted memo with a polymorphic (Object-typed) field
 */
public class Q27 {
    static final JSONWriter.Feature SORT = JSONWriter.Feature.SortFieldNamesAlphabetically;

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

    static ObjectWriterCreator[] creators() {
        return new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE};
    }

    static String cn(ObjectWriterCreator c) {
        return c == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
    }

    static String safe(Supplier<Object> s) {
        try {
            return String.valueOf(s.get());
        } catch (Throwable t) {
            String m = String.valueOf(t.getMessage());
            return "!" + t.getClass().getSimpleName() + ": " + (m.length() > 80 ? m.substring(0, 80) + "..." : m);
        }
    }

    static Map<String, Supplier<Object>> shapes() {
        Map<String, Supplier<Object>> k = new LinkedHashMap<>();
        k.put("B", B::new);
        k.put("ArrayList<B>", () -> new ArrayList<>(Collections.singletonList(new B())));
        k.put("LinkedList<B>", () -> new LinkedList<>(Collections.singletonList(new B())));
        k.put("Arrays.asList(B)", () -> Arrays.asList(new B()));
        k.put("singletonList(B)", () -> Collections.singletonList(new B()));
        k.put("unmodifiableList(B)", () -> Collections.unmodifiableList(new ArrayList<>(Collections.singletonList(new B()))));
        k.put("LinkedHashSet<B>", () -> new LinkedHashSet<>(Collections.singletonList(new B())));
        k.put("B[]", () -> new B[]{new B()});
        k.put("Object[]{B}", () -> new Object[]{new B()});
        k.put("Optional<B>", () -> Optional.of(new B()));
        k.put("AtomicReference<B>", () -> new AtomicReference<>(new B()));
        k.put("LinkedHashMap{k:B}", () -> new LinkedHashMap<>(Collections.singletonMap("k", new B())));
        k.put("JSONObject{k:B}", () -> JSONObject.of("k", new B()));
        k.put("JSONArray[B]", () -> JSONArray.of(new B()));
        return k;
    }

    static boolean sortedInside(String json) {
        int a = json.indexOf("a\\\":2");
        int z = json.indexOf("z\\\":1");
        if (a < 0) {
            a = json.indexOf("\"a\":2");
            z = json.indexOf("\"z\":1");
        }
        return a >= 0 && z >= 0 && a < z;
    }

    static void sectionA() {
        System.out.println("[A] bean inside a map key under the sort feature; 'sorted' = a before z inside the key");
        System.out.printf("  %-8s %-22s %-8s %-8s %-8s %s%n", "creator", "key shape", "none", "context", "field", "field-level output");
        for (ObjectWriterCreator c : creators()) {
            for (Map.Entry<String, Supplier<Object>> e : shapes().entrySet()) {
                ObjectWriterProvider p = new ObjectWriterProvider(c);
                HP hp = new HP();
                hp.m.put(e.getValue().get(), "v");
                HS hs = new HS();
                hs.m.put(e.getValue().get(), "v");
                String none = safe(() -> JSON.toJSONString(hp, new JSONWriter.Context(p)));
                String ctx = safe(() -> JSON.toJSONString(hp, new JSONWriter.Context(p, SORT)));
                String fld = safe(() -> JSON.toJSONString(hs, new JSONWriter.Context(p)));
                System.out.printf("  %-8s %-22s %-8s %-8s %-8s %s%n", cn(c), e.getKey(),
                        sortedInside(none) ? "sorted" : "natural", sortedInside(ctx) ? "sorted" : "natural",
                        sortedInside(fld) ? "sorted" : "natural", fld);
            }
        }
        System.out.println();
    }

    enum Color { RED }

    static void sectionB() {
        System.out.println("[B] scalar map keys: toJSONString(map) vs toJSONString(map, Sort)");
        Map<String, Object> keys = new LinkedHashMap<>();
        keys.put("BigDecimal 1.50", new BigDecimal("1.50"));
        keys.put("BigInteger", new BigInteger("123456789012345678901234567890"));
        keys.put("Boolean", Boolean.TRUE);
        keys.put("Double 2.5", 2.5d);
        keys.put("Float 3.5", 3.5f);
        keys.put("Integer", 7);
        keys.put("Long", 1L << 40);
        keys.put("Short", (short) 3);
        keys.put("Character", 'c');
        keys.put("Date", new Date(0));
        keys.put("UUID", UUID.fromString("00000000-0000-0000-0000-000000000001"));
        keys.put("LocalDate", java.time.LocalDate.of(2026, 10, 7));
        keys.put("enum", Color.RED);
        keys.put("Class", String.class);
        keys.put("Locale", Locale.CHINA);
        keys.put("int[]", new int[]{1, 2});
        keys.put("String[]", new String[]{"x"});
        for (Map.Entry<String, Object> e : keys.entrySet()) {
            Map<Object, Object> m = new LinkedHashMap<>();
            m.put(e.getValue(), "v");
            String a = safe(() -> JSON.toJSONString(m));
            String b = safe(() -> JSON.toJSONString(m, SORT));
            System.out.printf("  %-16s %-5s %s%s%n", e.getKey(), a.equals(b) ? "same" : "DIFF", a, a.equals(b) ? "" : "  vs sorted " + b);
        }
        System.out.println();
    }

    /** Spelled like commons-lang3 MutableDouble: longValue() saturates, as a cast does. */
    public static class MyDouble extends Number {
        final double v;

        MyDouble(double v) {
            this.v = v;
        }

        public int intValue() {
            return (int) v;
        }

        public long longValue() {
            return (long) v;
        }

        public float floatValue() {
            return (float) v;
        }

        public double doubleValue() {
            return v;
        }

        public String toString() {
            return "MyDouble(" + v + ")";
        }
    }

    /** Spelled like Guava UnsignedLong / a BigInteger wrapper: longValue() wraps, as BigInteger.longValue() does. */
    public static class MyBig extends Number {
        final BigInteger v;

        MyBig(BigInteger v) {
            this.v = v;
        }

        public int intValue() {
            return v.intValue();
        }

        public long longValue() {
            return v.longValue();
        }

        public float floatValue() {
            return v.floatValue();
        }

        public double doubleValue() {
            return v.doubleValue();
        }

        public String toString() {
            return "MyBig(" + v + ")";
        }
    }

    static void sectionD() {
        System.out.println("[D] canConvertToInt / canConvertToLong and the value getIntValue / getLongValue reads back");
        Map<String, Object> vals = new LinkedHashMap<>();
        double[] ds = {3.5, 1e30, -1e30, Double.NaN, Double.POSITIVE_INFINITY, 9.223372036854776E18, -9.223372036854776E18, 2147483647.5};
        for (double d : ds) {
            vals.put("Double " + d, d);
            DoubleAdder adder = new DoubleAdder();
            adder.add(d);
            vals.put("DoubleAdder " + d, adder);
            vals.put("MyDouble " + d, new MyDouble(d));
        }
        vals.put("MyBig 2^64-1", new MyBig(BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE)));
        vals.put("MyBig 2^63", new MyBig(BigInteger.ONE.shiftLeft(63)));
        vals.put("MyBig 2^63-1", new MyBig(BigInteger.ONE.shiftLeft(63).subtract(BigInteger.ONE)));
        vals.put("MyBig 2^70", new MyBig(BigInteger.ONE.shiftLeft(70)));
        vals.put("BigInteger 2^64-1", BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE));
        System.out.printf("  %-32s %-9s %-9s %-22s %s%n", "value", "canInt", "canLong", "getLongValue", "getIntValue");
        for (Map.Entry<String, Object> e : vals.entrySet()) {
            JSONObject o = new JSONObject();
            o.put("k", e.getValue());
            System.out.printf("  %-32s %-9s %-9s %-22s %s%n", e.getKey(), safe(() -> o.canConvertToInt("k")), safe(() -> o.canConvertToLong("k")),
                    safe(() -> o.getLongValue("k")), safe(() -> o.getIntValue("k")));
        }
        System.out.println();
    }

    static void sectionE() {
        System.out.println("[E] JSONObject.required with class literals");
        JSONObject o = new JSONObject();
        o.put("i", 1);
        o.put("l", 1L << 40);
        o.put("d", 2.5d);
        o.put("b", true);
        o.put("c", 'x');
        Object[][] cases = {{"i", int.class}, {"i", Integer.class}, {"i", long.class}, {"l", long.class}, {"d", double.class}, {"d", float.class},
                {"b", boolean.class}, {"c", char.class}, {"i", void.class}, {"i", Number.class}, {"missing", int.class}};
        for (Object[] c : cases) {
            String key = (String) c[0];
            Class<?> type = (Class<?>) c[1];
            System.out.printf("  required(%-8s %-18s -> %s%n", "\"" + key + "\",", type.getName() + ".class)", safe(() -> o.required(key, type)));
        }
        System.out.println();
    }

    public static class TreeHolder {
        public Map<Object, Object> m = new LinkedHashMap<>();
        public List<Map<String, Object>> items = new ArrayList<>();
    }

    static void sectionF() {
        System.out.println("[F] SortMapEntriesByKeys: toJSONString vs JSON.toJSON / JSONObject.from");
        TreeHolder h = new TreeHolder();
        h.m.put("b", 1);
        h.m.put("a", 2);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("zeta", 1);
        item.put("alpha", 2);
        h.items.add(item);
        Map<String, Object> rootMap = new LinkedHashMap<>(item);
        Object[][] cases = {{"string keys", h}, {"root Map", rootMap}};
        JSONWriter.Feature f = JSONWriter.Feature.SortMapEntriesByKeys;
        for (Object[] c : cases) {
            Object v = c[1];
            System.out.printf("  %-26s toJSONString  %s%n", c[0], safe(() -> JSON.toJSONString(v, f)));
            System.out.printf("  %-26s JSON.toJSON   %s%n", "", safe(() -> String.valueOf(JSON.toJSON(v, f))));
            if (!(v instanceof Map)) {
                System.out.printf("  %-26s JSONObject.from %s%n", "", safe(() -> String.valueOf(JSONObject.from(v, f))));
            }
        }
        System.out.println();
    }

    @JSONType(alphabetic = false, serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
    public static class TypeSorted {
        public int zulu = 9;
        public int apple = 1;
    }

    @JSONType(alphabetic = false, serializeFeatures = {JSONWriter.Feature.SortFieldNamesAlphabetically, JSONWriter.Feature.BeanToArray})
    public static class TypeSortedArray {
        public int zulu = 9;
        public int apple = 1;
    }

    public static class TypeSortedHolder {
        public TypeSorted t = new TypeSorted();
        public List<TypeSorted> l = new ArrayList<>(Collections.singletonList(new TypeSorted()));
    }

    static void sectionG() {
        System.out.println("[G] type-level @JSONType(serializeFeatures = SortFieldNamesAlphabetically)");
        for (ObjectWriterCreator c : creators()) {
            ObjectWriterProvider p = new ObjectWriterProvider(c);
            System.out.printf("  %-8s plain            %s%n", cn(c), safe(() -> JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p))));
            System.out.printf("  %-8s FieldBased       %s%n", cn(c), safe(() -> JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p, JSONWriter.Feature.FieldBased))));
            System.out.printf("  %-8s BeanToArray ctx  %s%n", cn(c), safe(() -> JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p, JSONWriter.Feature.BeanToArray))));
            System.out.printf("  %-8s +BeanToArray type %s%n", cn(c), safe(() -> JSON.toJSONString(new TypeSortedArray(), new JSONWriter.Context(p))));
            System.out.printf("  %-8s holder           %s%n", cn(c), safe(() -> JSON.toJSONString(new TypeSortedHolder(), new JSONWriter.Context(p))));
            System.out.printf("  %-8s JSONB->JSON      %s%n", cn(c), safe(() -> JSON.toJSONString(com.alibaba.fastjson2.JSONB.parseObject(
                    com.alibaba.fastjson2.JSONB.toBytes(new TypeSorted(), new JSONWriter.Context(p)), LinkedHashMap.class))));
        }
        System.out.println();
    }

    public static class Getters {
        private int zeta = 1;
        private int alpha = 2;

        public int getZeta() {
            return zeta;
        }

        public int getAlpha() {
            return alpha;
        }
    }

    public static class PublicFields {
        public int zeta = 1;
        public int alpha = 2;
    }

    @JSONType(alphabetic = false)
    public static class GettersDecl {
        private int zeta = 1;
        private int alpha = 2;

        public int getZeta() {
            return zeta;
        }

        public int getAlpha() {
            return alpha;
        }
    }

    static void sectionH() {
        System.out.println("[H] default order (no sort feature); fields declared zeta, alpha");
        for (ObjectWriterCreator c : creators()) {
            for (Object o : new Object[]{new Getters(), new PublicFields(), new GettersDecl()}) {
                ObjectWriterProvider p = new ObjectWriterProvider(c);
                System.out.printf("  %-8s %-12s method-based %-22s FieldBased %s%n", cn(c), o.getClass().getSimpleName(),
                        safe(() -> JSON.toJSONString(o, new JSONWriter.Context(p))),
                        safe(() -> JSON.toJSONString(o, new JSONWriter.Context(p, JSONWriter.Feature.FieldBased))));
            }
        }
        System.out.println();
    }

    public static class TM {
        public Map<String, B> m = new LinkedHashMap<>(Collections.singletonMap("k", new B()));
    }

    static void sectionJ() {
        System.out.println("[J] typed Map<String, B> value memo, one provider, alternating contexts vs a fresh provider");
        JSONWriter.Feature[][] seq = {{}, {SORT}, {JSONWriter.Feature.FieldBased}, {JSONWriter.Feature.BeanToArray}, {SORT, JSONWriter.Feature.BeanToArray}, {SORT}, {}};
        for (ObjectWriterCreator c : creators()) {
            ObjectWriterProvider p = new ObjectWriterProvider(c);
            int bad = 0;
            StringBuilder sb = new StringBuilder();
            for (int round = 0; round < 2; round++) {
                for (JSONWriter.Feature[] f : seq) {
                    String got = safe(() -> JSON.toJSONString(new TM(), new JSONWriter.Context(p, f)));
                    String exp = safe(() -> JSON.toJSONString(new TM(), new JSONWriter.Context(new ObjectWriterProvider(c), f)));
                    if (!got.equals(exp)) {
                        bad++;
                        sb.append(Arrays.toString(f)).append(' ').append(got).append(" expected ").append(exp).append("; ");
                    }
                }
            }
            System.out.printf("  %-8s %d writes, %d differ from a fresh provider %s%n", cn(c), 2 * seq.length, bad, sb);
        }
        System.out.println();
    }

    @JSONType(alphabetic = false)
    public static class B2 {
        public int y = 3;
        public int b = 4;
    }

    public static class Poly {
        public Object v;
    }

    static void sectionK() {
        System.out.println("[K] Object-typed field, values of two classes alternating, sorted context, one provider");
        for (ObjectWriterCreator c : creators()) {
            ObjectWriterProvider p = new ObjectWriterProvider(c);
            int bad = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                Poly poly = new Poly();
                poly.v = i % 2 == 0 ? new B() : new B2();
                JSONWriter.Feature[] f = i % 3 == 2 ? new JSONWriter.Feature[]{SORT, JSONWriter.Feature.FieldBased} : new JSONWriter.Feature[]{SORT};
                String got = safe(() -> JSON.toJSONString(poly, new JSONWriter.Context(p, f)));
                String exp = safe(() -> JSON.toJSONString(poly, new JSONWriter.Context(new ObjectWriterProvider(c), f)));
                if (!got.equals(exp)) {
                    bad++;
                    sb.append(got).append(" expected ").append(exp).append("; ");
                }
            }
            System.out.printf("  %-8s 6 writes, %d differ from a fresh provider %s%n", cn(c), bad, sb);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        String only = args.length > 0 ? args[0] : "ABDEFGHJK";
        if (only.contains("A")) sectionA();
        if (only.contains("B")) sectionB();
        if (only.contains("D")) sectionD();
        if (only.contains("E")) sectionE();
        if (only.contains("F")) sectionF();
        if (only.contains("G")) sectionG();
        if (only.contains("H")) sectionH();
        if (only.contains("J")) sectionJ();
        if (only.contains("K")) sectionK();
    }
}
