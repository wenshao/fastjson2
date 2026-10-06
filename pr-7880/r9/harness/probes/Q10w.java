package p10w;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * Round-8 probe for 61e081039: the field-level sort branch of ObjectWriterImplMap#mapKeyToString
 * (a hand-built JSONWriter) against the context branch (JSON.toJSONString(key, context)).
 *
 * D1: for each creator x context feature x key shape, the document written with the sort on the map field
 *     must equal the document written with the sort on the context (holder names normalized).
 * D2: the same with a context filter (both writes then take the context branch).
 * D3: nested beans inside a key vs the same bean as a field-level sorted value.
 */
public class Q10w {
    public enum Color { RED, GREEN }

    @JSONType(alphabetic = false)
    public static class Inner2 {
        public int z = 1;
        public int a = 2;
    }

    @JSONType(alphabetic = false)
    public static class KeyBean {
        public String zeta = "z";
        public Inner2 first = new Inner2();
        public Inner2 second = first;
        public String nul;
        public Color color = Color.GREEN;
        public BigDecimal amount = new BigDecimal("1.50");
        public long big = 9007199254740993L;
        public Date date = new Date(1700000000000L);
        public LocalDateTime ldt = LocalDateTime.of(2026, 10, 6, 9, 38, 9);
        public List<Inner2> list = Arrays.asList(new Inner2());
        public char ch = 'x';
        public boolean flag;
        public Integer zero = 0;
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
        public Object v;
    }

    public static class VP {
        public Object v;
    }

    static Map<String, Supplier<Object>> keys() {
        Map<String, Supplier<Object>> k = new LinkedHashMap<>();
        k.put("bean", KeyBean::new);
        k.put("enum", () -> Color.RED);
        k.put("UUID", () -> UUID.fromString("00000000-0000-0000-0000-000000000001"));
        k.put("BigDecimal", () -> new BigDecimal("1.50"));
        k.put("Double", () -> 1.5d);
        k.put("Boolean", () -> Boolean.TRUE);
        k.put("Character", () -> 'c');
        k.put("LocalDate", () -> LocalDate.of(2026, 10, 6));
        k.put("Date", () -> new Date(1700000000000L));
        k.put("List<bean>", () -> new ArrayList<>(Arrays.asList(new Inner2(), new Inner2())));
        k.put("Map", () -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("z", 1);
            m.put("a", new Inner2());
            return m;
        });
        k.put("JSONObject", () -> JSONObject.of("z", 1, "a", new Inner2()));
        k.put("int[]", () -> new int[]{3, 1});
        k.put("byte[]", () -> new byte[]{1, 2});
        k.put("Optional", () -> Optional.of(new Inner2()));
        k.put("List<List<bean>>", () -> new ArrayList<>(Collections.singletonList(new ArrayList<>(Collections.singletonList(new Inner2())))));
        k.put("bean[]", () -> new Inner2[]{new Inner2()});
        k.put("Object[]", () -> new Object[]{new Inner2(), 1});
        k.put("Set<bean>", () -> new LinkedHashSet<>(Collections.singletonList(new Inner2())));
        k.put("Map<String,List<bean>>", () -> new LinkedHashMap<>(Collections.singletonMap("k", Arrays.asList(new Inner2()))));
        k.put("AtomicReference", () -> new java.util.concurrent.atomic.AtomicReference<>(new Inner2()));
        return k;
    }

    static final JSONWriter.Feature[] FEATURES = {
            null,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.PrettyFormat,
            JSONWriter.Feature.UseSingleQuotes,
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.BeanToArray,
            JSONWriter.Feature.WriteEnumUsingToString,
            JSONWriter.Feature.WriteEnumUsingOrdinal,
            JSONWriter.Feature.BrowserCompatible,
            JSONWriter.Feature.WriteNonStringValueAsString,
            JSONWriter.Feature.WriteBigDecimalAsPlain,
            JSONWriter.Feature.WriteLongAsString,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.WriteBooleanAsNumber,
            JSONWriter.Feature.WriteNullStringAsEmpty,
            JSONWriter.Feature.NotWriteNumberClassName,
            JSONWriter.Feature.WriteByteArrayAsBase64,
            JSONWriter.Feature.SortMapEntriesByKeys,
            JSONWriter.Feature.EscapeNoneAscii,
            JSONWriter.Feature.WriteMapNullValue,
    };

    static JSONWriter.Context ctx(ObjectWriterProvider p, JSONWriter.Feature f, boolean sort, boolean filter, boolean dateFormat) {
        JSONWriter.Context c = new JSONWriter.Context(p);
        if (f != null) {
            c.config(f, true);
        }
        if (sort) {
            c.config(JSONWriter.Feature.SortFieldNamesAlphabetically, true);
        }
        if (filter) {
            c.configFilter((PropertyFilter) (o, n, v) -> true);
        }
        if (dateFormat) {
            c.setDateFormat("yyyy/MM/dd HH:mm");
        }
        return c;
    }

    static String write(Object holder, JSONWriter.Context c) {
        try {
            return JSON.toJSONString(holder, c).replace("p10w.Q10w$HS", "HOLDER").replace("p10w.Q10w$HP", "HOLDER")
                    .replace("Q10w$HS", "HOLDER").replace("Q10w$HP", "HOLDER");
        } catch (Throwable e) {
            return "EXC " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String only = args.length > 0 ? args[0] : null;
        int cells = 0, same = 0;
        List<String> diffs = new ArrayList<>();
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            for (boolean filter : new boolean[]{false, true}) {
                for (boolean df : new boolean[]{false, true}) {
                    for (JSONWriter.Feature f : FEATURES) {
                        for (Map.Entry<String, Supplier<Object>> e : keys().entrySet()) {
                            if (only != null && !e.getKey().equals(only)) {
                                continue;
                            }
                            ObjectWriterProvider p = new ObjectWriterProvider(creator);
                            HS hs = new HS();
                            hs.m.put(e.getValue().get(), "v");
                            HP hp = new HP();
                            hp.m.put(e.getValue().get(), "v");
                            // warm: the natural writers of the key types are cached before the field-level sorted write
                            write(hp, ctx(p, f, false, filter, df));
                            String a = write(hs, ctx(p, f, false, filter, df));
                            String b = write(hp, ctx(p, f, true, filter, df));
                            cells++;
                            if (a.equals(b)) {
                                same++;
                            } else {
                                diffs.add(String.format("%-7s %-6s %-10s %-28s %-11s%n   field-level: %s%n   context    : %s",
                                        cn, filter ? "filter" : "-", df ? "dateFormat" : "-", f, e.getKey(), a, b));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("(natural writers warm) D1/D2 field-level sorted map key == context-sorted map key: cells=" + cells + " same=" + same + " differ=" + diffs.size());
        for (String d : diffs) {
            System.out.println(d);
        }
        System.out.println();
        System.out.println("D4 the same shapes as a field-level sorted VALUE: value == value written with the sort on the context");
        int vcells = 0, vsame = 0;
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            for (Map.Entry<String, Supplier<Object>> e : keys().entrySet()) {
                ObjectWriterProvider p = new ObjectWriterProvider(creator);
                VS vs = new VS();
                vs.v = e.getValue().get();
                VP vp = new VP();
                vp.v = e.getValue().get();
                JSON.toJSONString(vp, new JSONWriter.Context(p));
                String a = JSON.toJSONString(vs, new JSONWriter.Context(p));
                String b = JSON.toJSONString(vp, new JSONWriter.Context(p, JSONWriter.Feature.SortFieldNamesAlphabetically));
                vcells++;
                if (a.equals(b)) {
                    vsame++;
                } else {
                    System.out.println(String.format("  %-7s %-22s field-level %s | context %s", cn, e.getKey(), a, b));
                }
            }
        }
        System.out.println("  cells=" + vcells + " same=" + vsame);
        System.out.println();
        System.out.println("D3 nested bean inside a field-level sorted key vs as a field-level sorted value (no context features)");
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            ObjectWriterProvider p = new ObjectWriterProvider(creator);
            HS hs = new HS();
            hs.m.put(new KeyBean(), "v");
            VS vs = new VS();
            vs.v = new KeyBean();
            System.out.println("  " + cn + " key  : " + JSON.toJSONString(hs, new JSONWriter.Context(p)));
            System.out.println("  " + cn + " value: " + JSON.toJSONString(vs, new JSONWriter.Context(p)));
        }
    }
}
