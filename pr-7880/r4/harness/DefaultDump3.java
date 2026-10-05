package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Round-3 additions to the default-path dump: feature-off behavior on the code paths touched by
 * 51643676c..f382fe908. Uses only API that exists on main, so the same class runs on every jar.
 */
public class DefaultDump3 {
    @JSONType(alphabetic = false)
    public static class T {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    @JSONType(alphabetic = false)
    public static final class FT {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    public static class FieldArr {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public T t = new T();
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public FT ft = new FT();
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public List<T> list = new ArrayList<>(Collections.singletonList(new T()));
    }

    public static class Secret {
        public String value = "sensitive";
    }

    public static class MaskWriter implements ObjectWriter<Secret> {
        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            jsonWriter.writeString("MASKED");
        }
    }

    public static class Gates {
        @JSONField(writeUsing = MaskWriter.class)
        public Secret secret = new Secret();
        @JSONField(contentAs = T.class)
        public List<Object> content = new ArrayList<>(Collections.singletonList(new T()));
        @JSONField(format = "yyyy-MM-dd")
        public List<Date> dates = Collections.singletonList(new Date(0L));
    }

    public static class Inner {
        private int hidden = 42;
        private String name = "n";
    }

    public static class Inner$$EnhancerBySpringCGLIB$$d3 extends Inner {
    }

    public static class Priv {
        private int x = 1;
    }

    public static class NInner {
        public String a;
        public int b = 2;
    }

    public static class NOuter {
        public String top;
        public NInner inner = new NInner();
        public List<NInner> list = Collections.singletonList(new NInner());
        public Map<String, Object> map = new LinkedHashMap<>(Collections.singletonMap("m", new NInner()));
    }

    @JSONType(serializeFeatures = JSONWriter.Feature.WriteNulls)
    public static class NOuterAnn {
        public String top;
        public List<NInner> list = Collections.singletonList(new NInner());
    }

    public static class Bean {
        public int aId;
        public int bId;
    }

    public static class MapArrayToBean {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportArrayToBean)
        public Map<String, Bean> m;
    }

    public static class MapSmartMatch {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch)
        public Map<String, Bean> m;
    }

    public static class MapNative {
        @JSONField(deserializeFeatures = JSONReader.Feature.UseNativeObject)
        public Map<String, Map<String, Object>> m;
    }

    public static class MapNativeObj {
        @JSONField(deserializeFeatures = JSONReader.Feature.UseNativeObject)
        public Map<String, Object> m;
    }

    public static class MapAutoType {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType)
        public Map<String, Object> m;
    }

    public static class MapMapAutoType {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportAutoType)
        public Map<String, Map<String, Object>> m;
    }

    public static class MapBigDec {
        @JSONField(deserializeFeatures = JSONReader.Feature.UseBigDecimalForDoubles)
        public Map<String, Map<String, Object>> m;
    }

    public static class MapIgnoreNull {
        @JSONField(deserializeFeatures = JSONReader.Feature.IgnoreNullPropertyValue)
        public Map<String, Map<String, Object>> m;
    }

    public static class Pojo {
        public int v;
    }

    interface S {
        Object get() throws Exception;
    }

    static String safe(S s) {
        try {
            return String.valueOf(s.get());
        } catch (Throwable t) {
            return "EXC " + t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()).replaceAll("fastjson-version [^ ]+", "");
        }
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    static String types(Object o) {
        if (o instanceof Map) {
            StringBuilder sb = new StringBuilder(o.getClass().getSimpleName()).append('{');
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                sb.append(e.getKey()).append('=').append(types(e.getValue())).append(',');
            }
            return sb.append('}').toString();
        }
        if (o instanceof List) {
            StringBuilder sb = new StringBuilder(o.getClass().getSimpleName()).append('[');
            for (Object x : (List<?>) o) {
                sb.append(types(x)).append(',');
            }
            return sb.append(']').toString();
        }
        if (o instanceof Bean || o instanceof Pojo) {
            return o.getClass().getSimpleName() + ":" + JSON.toJSONString(o);
        }
        return o == null ? "null" : o.getClass().getSimpleName() + ":" + o;
    }

    static Object field(Object holder) throws Exception {
        return holder.getClass().getField("m").get(holder);
    }

    public static void main(String[] args) throws Exception {
        StringBuilder out = new StringBuilder();
        Object[] writes = {new FieldArr(), new Gates(), new NOuter(), new NOuterAnn()};
        for (Object o : writes) {
            String k = o.getClass().getSimpleName();
            out.append("W ").append(k).append('\n');
            out.append("  json      ").append(safe(() -> JSON.toJSONString(o))).append('\n');
            out.append("  utf8      ").append(safe(() -> new String(JSON.toJSONBytes(o), StandardCharsets.UTF_8))).append('\n');
            out.append("  utf8+cs   ").append(safe(() -> new String(JSON.toJSONBytes(o, StandardCharsets.UTF_8), StandardCharsets.UTF_8))).append('\n');
            out.append("  jsonb     ").append(safe(() -> hex(JSONB.toBytes(o)))).append('\n');
            out.append("  from      ").append(safe(() -> JSON.toJSONString(JSONObject.from(o), JSONWriter.Feature.WriteNulls))).append('\n');
            out.append("  fromNulls ").append(safe(() -> JSON.toJSONString(JSONObject.from(o, JSONWriter.Feature.WriteNulls), JSONWriter.Feature.WriteNulls))).append('\n');
            out.append("  toJSON+WN ").append(safe(() -> JSON.toJSONString(JSON.toJSON(o, JSONWriter.Feature.WriteNulls), JSONWriter.Feature.WriteNulls))).append('\n');
            out.append("  json+WN   ").append(safe(() -> JSON.toJSONString(o, JSONWriter.Feature.WriteNulls))).append('\n');
        }

        out.append("W proxy+FieldBased\n");
        ObjectWriterProvider p1 = new ObjectWriterProvider();
        out.append("  plainFirst ").append(safe(() -> JSON.toJSONString(new Inner(), new JSONWriter.Context(p1, JSONWriter.Feature.FieldBased))))
                .append(" | ").append(safe(() -> JSON.toJSONString(new Inner$$EnhancerBySpringCGLIB$$d3(), new JSONWriter.Context(p1, JSONWriter.Feature.FieldBased)))).append('\n');
        ObjectWriterProvider p2 = new ObjectWriterProvider();
        out.append("  proxyFirst ").append(safe(() -> JSON.toJSONString(new Inner$$EnhancerBySpringCGLIB$$d3(), new JSONWriter.Context(p2, JSONWriter.Feature.FieldBased))))
                .append(" | ").append(safe(() -> JSON.toJSONString(new Inner(), new JSONWriter.Context(p2, JSONWriter.Feature.FieldBased)))).append('\n');
        ObjectWriterProvider p3 = new ObjectWriterProvider();
        out.append("  proxyNatural ").append(safe(() -> JSON.toJSONString(new Inner(), new JSONWriter.Context(p3))))
                .append(" | ").append(safe(() -> JSON.toJSONString(new Inner$$EnhancerBySpringCGLIB$$d3(), new JSONWriter.Context(p3, JSONWriter.Feature.FieldBased)))).append('\n');

        out.append("W toJSONBytes FieldBased\n");
        out.append("  toJSONString(FB)            ").append(safe(() -> JSON.toJSONString(new Priv(), JSONWriter.Feature.FieldBased))).append('\n');
        out.append("  toJSONBytes(FB)             ").append(safe(() -> new String(JSON.toJSONBytes(new Priv(), JSONWriter.Feature.FieldBased)))).append('\n');
        out.append("  toJSONBytes(UTF8, FB)       ").append(safe(() -> new String(JSON.toJSONBytes(new Priv(), StandardCharsets.UTF_8, JSONWriter.Feature.FieldBased)))).append('\n');
        out.append("  toJSONBytes(UTF8, ctx(FB))  ").append(safe(() -> new String(JSON.toJSONBytes(new Priv(), StandardCharsets.UTF_8, new JSONWriter.Context(JSONWriter.Feature.FieldBased))))).append('\n');
        out.append("  JSONFactory.getObjectWriter(FB|Pretty).write ").append(safe(() -> {
            ObjectWriter w = JSONFactory.getObjectWriter(Priv.class, JSONWriter.Feature.FieldBased.mask | JSONWriter.Feature.PrettyFormat.mask);
            try (JSONWriter jw = JSONWriter.of()) {
                w.write(jw, new Priv(), null, null, 0);
                return jw.toString();
            }
        })).append('\n');

        String[][] reads = {
                {"MapArrayToBean", "{\"m\":{\"k\":[1,2]}}"},
                {"MapSmartMatch", "{\"m\":{\"k\":{\"A_ID\":1,\"b_id\":2}}}"},
                {"MapNative", "{\"m\":{\"k\":{\"a\":{\"b\":1}}}}"},
                {"MapNativeObj", "{\"m\":{\"k\":{\"a\":{\"b\":1}}}}"},
                {"MapAutoType", "{\"m\":{\"k\":{\"@type\":\"verify.DefaultDump3$Pojo\",\"v\":1}}}"},
                {"MapMapAutoType", "{\"m\":{\"k\":{\"x\":{\"@type\":\"verify.DefaultDump3$Pojo\",\"v\":1}}}}"},
                {"MapBigDec", "{\"m\":{\"k\":{\"d\":1.5}}}"},
                {"MapIgnoreNull", "{\"m\":{\"k\":{\"a\":null,\"b\":1}}}"},
        };
        for (String[] r : reads) {
            Class<?> c = Class.forName("verify.DefaultDump3$" + r[0]);
            out.append("R ").append(r[0]).append(' ').append(r[1]).append('\n');
            out.append("  ").append(safe(() -> types(field(JSON.parseObject(r[1], c))))).append('\n');
            out.append("  utf8 ").append(safe(() -> types(field(JSON.parseObject(r[1].getBytes(StandardCharsets.UTF_8), c))))).append('\n');
        }

        String[] untyped = {
                "{\"a\":null,\"a\":1}", "{\"a\":1,\"a\":null}", "{1:1,\"1\":2}", "{\"a\":1,\"a\":{\"$ref\":\"$\"}}",
                "{\"x\":{\"y\":{\"z\":1,\"z\":2}}}", "{\"x\":[{\"z\":1,\"z\":2}]}",
        };
        for (String in : untyped) {
            out.append("U ").append(in).append('\n');
            out.append("  parseObject(IgnoreNull) ").append(safe(() -> types(JSON.parseObject(in, JSONReader.Feature.IgnoreNullPropertyValue)))).append('\n');
            out.append("  LinkedHashMap           ").append(safe(() -> types(JSON.parseObject(in, LinkedHashMap.class)))).append('\n');
            out.append("  Map<String,Map>         ").append(safe(() -> types(JSON.parseObject(in, new TypeReference<Map<String, Map<String, Object>>>() {})))).append('\n');
            out.append("  dupAsArray              ").append(safe(() -> types(JSON.parseObject(in, JSONReader.Feature.DuplicateKeyValueAsArray)))).append('\n');
        }
        out.append("U read(preseeded)\n");
        out.append("  ").append(safe(() -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("a", 0);
            JSONReader.of("{\"a\":1,\"b\":2}").read(m, 0L);
            return m;
        })).append('\n');
        System.out.print(out);
        System.exit(0);
    }
}
