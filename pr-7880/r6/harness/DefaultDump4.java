package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Round-4 additions to the default-path dump: writer filters (the code the round-4 patch touches) and tree
 * conversion of nested values (the fallback changed in 78104ea4e), with the new features off. Uses only API that
 * exists on main, so the same class runs on every jar.
 */
public class DefaultDump4 {
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

    public static class A1 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class A2 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class A3 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class AHolder {
        public A1 one = new A1();
        public List<A2> two = Collections.singletonList(new A2());
        public Map<String, A3> three = Collections.singletonMap("k", new A3());
    }
    public static class B1 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    public static class B2 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }
    @JSONType(alphabetic = false, serializeFilters = Mask.class)
    public static class C1 { public long id = 7; public String password = "hunter2"; public String name = "alice"; }

    public enum Color { RED }

    public static class Rec {
        public long id = 9007199254740993L;
        public List<Long> ids = Arrays.asList(1L, 9007199254740993L);
        public List<Map<String, Object>> rows = Collections.singletonList(Collections.<String, Object>singletonMap("n", 5L));
        public List<Map<String, Object>> nulls = Collections.singletonList(Collections.<String, Object>singletonMap("n", null));
        public BigDecimal amount = new BigDecimal("1E+2");
        public List<BigDecimal> amounts = Collections.singletonList(new BigDecimal("1E+2"));
        public Color color = Color.RED;
        public List<Color> colors = Collections.singletonList(Color.RED);
        public List<A1> beans = Collections.singletonList(new A1());
        public List<List<B2>> deep = Collections.singletonList(Collections.singletonList(new B2()));
        public String missing;
    }

    static void line(String k, Object v) {
        System.out.println(k + " = " + v);
    }

    static String jsonb(Object o) {
        return JSON.toJSONString(JSONB.parse(JSONB.toBytes(o)));
    }

    public static void main(String[] args) throws Exception {
        // trees first, before any filter is registered for the types they contain
        line("from(rec)", JSONObject.from(new Rec()));
        line("toJSON(rec)", JSON.toJSON(new Rec()));
        JSONWriter.Feature[] fs = {JSONWriter.Feature.WriteNulls, JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.WriteLongAsString, JSONWriter.Feature.WriteNonStringValueAsString,
                JSONWriter.Feature.BrowserCompatible, JSONWriter.Feature.WriteBigDecimalAsPlain,
                JSONWriter.Feature.WriteEnumUsingToString, JSONWriter.Feature.WriteEnumsUsingName,
                JSONWriter.Feature.NotWriteDefaultValue, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.MapSortField};
        for (JSONWriter.Feature f : fs) {
            line("from(rec, " + f + ")", JSONObject.from(new Rec(), f));
        }
        List<Object> list = new ArrayList<>(Arrays.asList(new A1(), 9007199254740993L, Collections.singletonMap("n", null)));
        line("toJSON(list)", JSON.toJSON(list));
        line("toJSON(list, WriteLongAsString)", JSON.toJSON(list, JSONWriter.Feature.WriteLongAsString));
        line("toJSON(list, WriteNulls)", JSON.toJSON(list, JSONWriter.Feature.WriteNulls));

        // JSON.register(Class, Filter) on the default provider
        JSON.register(A1.class, new Mask());
        JSON.register(A2.class, new Drop());
        JSON.register(A3.class, new Rename());
        for (Object o : new Object[]{new A1(), new A2(), new A3(), new AHolder()}) {
            String n = o.getClass().getSimpleName();
            line("register " + n + " json", JSON.toJSONString(o));
            line("register " + n + " utf8", new String(JSON.toJSONBytes(o), StandardCharsets.UTF_8));
            line("register " + n + " jsonb", jsonb(o));
            line("register " + n + " pretty", JSON.toJSONString(o, JSONWriter.Feature.PrettyFormat).replace("\n", "\\n").replace("\t", "\\t"));
            line("register " + n + " fieldBased", JSON.toJSONString(o, JSONWriter.Feature.FieldBased));
        }
        // filters replaced and cleared
        JSON.register(A1.class, new Drop());
        line("register A1 replaced (Mask+Drop)", JSON.toJSONString(new A1()));

        // custom provider: setFilter on the writer it hands out
        ObjectWriterProvider p = new ObjectWriterProvider();
        p.getObjectWriter(B1.class).setFilter(new Mask());
        line("provider B1", JSON.toJSONString(new B1(), new JSONWriter.Context(p)));
        line("provider B1 default provider", JSON.toJSONString(new B1()));

        // registered ObjectWriters adapter with a filter, before and after registration
        ObjectWriter w = ObjectWriters.objectWriter(B2.class,
                ObjectWriters.fieldWriter("password", String.class, (B2 b) -> b.password),
                ObjectWriters.fieldWriter("name", String.class, (B2 b) -> b.name));
        w.setFilter(new Mask());
        ObjectWriterProvider p2 = new ObjectWriterProvider();
        p2.register(B2.class, w);
        line("registered adapter B2", JSON.toJSONString(new B2(), new JSONWriter.Context(p2)));
        w.setFilter(new Drop());
        line("registered adapter B2 + Drop", JSON.toJSONString(new B2(), new JSONWriter.Context(p2)));
        line("registered adapter B2 unregister", p2.unregister(B2.class, w) + " " + JSON.toJSONString(new B2(), new JSONWriter.Context(p2)));

        // creator-built writer of a @JSONType(serializeFilters) bean
        ObjectWriterProvider p3 = new ObjectWriterProvider();
        p3.register(C1.class, ObjectWriterCreator.INSTANCE.createObjectWriter(C1.class));
        line("registered reflect C1", JSON.toJSONString(new C1(), new JSONWriter.Context(p3)));
        line("annotation C1", JSON.toJSONString(new C1()));

        // a tree of filtered beans: tree conversion does not apply writer filters (as on main)
        line("from(AHolder)", JSONObject.from(new AHolder()));
    }
}
