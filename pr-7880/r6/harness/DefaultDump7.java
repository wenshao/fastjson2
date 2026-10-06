package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Round-6 (82514ab10) default-path dump: the container writers and readers c47a3cf51 changed (map, list, collection,
 * arrays, Optional; nested arrays and objects in the readers), with no new feature in use. Uses only API that exists
 * on main, so the same class runs on every jar.
 */
public class DefaultDump7 {
    public enum Color { RED, GREEN }

    @JSONType(alphabetic = false)
    public static class Child {
        public int zebra = 3;
        public String apple;
        public Long big = 9007199254740993L;
        public BigDecimal price = new BigDecimal("1.50");
        public Color color = Color.GREEN;
        public Date when = new Date(0);

        @Override
        public String toString() {
            return "Child(toString)";
        }
    }

    public static final class FinalChild {
        public int a = 1;
        public String b;

        @Override
        public String toString() {
            return "FinalChild(toString)";
        }
    }

    public static class Key {
        public int id = 7;
        public String tag;

        @Override
        public String toString() {
            return "Key#" + id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key && ((Key) o).id == id;
        }
    }

    static Map<Object, Object> mixedMap() {
        Map<Object, Object> m = new LinkedHashMap<>();
        m.put("s", "v");
        m.put(1, new Child());
        m.put(2L, 9007199254740993L);
        m.put(Color.RED, new BigDecimal("2.50"));
        m.put(new Date(86_400_000L), "day");
        m.put(new Key(), "bean-key");
        m.put(UUID.fromString("00000000-0000-0000-0000-000000000001"), null);
        m.put("nested", new LinkedHashMap<>(Collections.singletonMap("k", new Child())));
        m.put("list", new ArrayList<>(Arrays.asList(new Child(), null, 3L)));
        return m;
    }

    @JSONType(alphabetic = false)
    public static class Holder {
        public Map<String, Child> typedMap = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
        public Map<Object, Object> mixed = mixedMap();
        public List<Child> list = new ArrayList<>(Arrays.asList(new Child(), new Child()));
        public Set<Child> set = new LinkedHashSet<>(Collections.singletonList(new Child()));
        public Collection<Object> coll = new ArrayDeque<>(Arrays.asList(new Child(), "s", 1L));
        public Object[] objArr = {new Child(), "s", 2L, null};
        public FinalChild[] finalArr = {new FinalChild(), null};
        public Child[] childArr = {new Child()};
        public Optional<Child> opt = Optional.of(new Child());
        public Map<String, Long> longs = new LinkedHashMap<>(Collections.singletonMap("n", 9007199254740993L));
        public Map<String, BigDecimal> decimals = new LinkedHashMap<>(Collections.singletonMap("d", new BigDecimal("1E+3")));
    }

    @JSONType(alphabetic = false)
    public static class Annotated {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public Map<String, Child> mapNulls = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public List<Child> listNulls = new ArrayList<>(Collections.singletonList(new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public Set<Child> setNulls = new LinkedHashSet<>(Collections.singletonList(new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public Object[] arrNulls = {new Child()};
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public FinalChild[] finalNulls = {new FinalChild()};
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public Optional<Child> optNulls = Optional.of(new Child());
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteLongAsString)
        public Map<String, Object> mapLongs = new LinkedHashMap<>(Collections.singletonMap("c", new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNonStringValueAsString)
        public List<Child> listAsString = new ArrayList<>(Collections.singletonList(new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteClassName)
        public Map<String, Object> mapClassName = new LinkedHashMap<>(Collections.singletonMap("c", new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteEnumUsingToString)
        public Map<String, Color> mapEnums = new LinkedHashMap<>(Collections.singletonMap("c", Color.RED));
        @JSONField(serializeFeatures = JSONWriter.Feature.BrowserCompatible)
        public Map<String, Long> mapBrowser = new LinkedHashMap<>(Collections.singletonMap("n", 9007199254740993L));
        @JSONField(serializeFeatures = JSONWriter.Feature.NotWriteDefaultValue)
        public Map<String, Child> mapNotDefault = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteMapNullValue)
        public Map<Object, Object> mapMixed = mixedMap();
        @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased)
        public Map<String, Child> mapFieldBased = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Map<String, Child> mapBeanToArray = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
    }

    static void line(String k, Object v) {
        System.out.println(k + " = " + v);
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    interface Call {
        Object call();
    }

    static String safe(Call c) {
        try {
            Object o = c.call();
            return o instanceof byte[] ? hex((byte[]) o) : String.valueOf(o);
        } catch (Throwable e) {
            return "throws " + e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()).replaceAll("\\s+", " ").replaceAll("fastjson-version \\S+", "");
        }
    }

    static final ObjectWriter<Long> CUSTOM_LONG = (w, o, fieldName, fieldType, features) -> w.writeString("L" + o + "/" + Long.toHexString(features));
    static final ObjectWriter<BigDecimal> CUSTOM_DECIMAL = (w, o, fieldName, fieldType, features) -> w.writeString("D" + o + "/" + Long.toHexString(features));

    static void writeAll(String label, ObjectWriterProvider p) {
        JSONWriter.Feature[][] fs = {
                {}, {JSONWriter.Feature.WriteNulls}, {JSONWriter.Feature.WriteClassName}, {JSONWriter.Feature.WriteNonStringKeyAsString},
                {JSONWriter.Feature.MapSortField}, {JSONWriter.Feature.SortMapEntriesByKeys}, {JSONWriter.Feature.BrowserCompatible},
                {JSONWriter.Feature.WriteLongAsString}, {JSONWriter.Feature.ReferenceDetection}, {JSONWriter.Feature.NotWriteDefaultValue},
                {JSONWriter.Feature.FieldBased}, {JSONWriter.Feature.BeanToArray}, {JSONWriter.Feature.WriteNonStringValueAsString},
                {JSONWriter.Feature.WriteEnumUsingToString}, {JSONWriter.Feature.PrettyFormat}};
        for (Object root : new Object[]{new Holder(), new Annotated(), mixedMap(), new ArrayList<>(Arrays.asList(new Child(), mixedMap())),
                new Object[]{new Child(), mixedMap()}, Optional.of(new Child())}) {
            String rn = root.getClass().getSimpleName();
            for (JSONWriter.Feature[] f : fs) {
                String n = label + " " + rn + " " + Arrays.toString(f);
                line(n + " json", safe(() -> JSON.toJSONString(root, new JSONWriter.Context(p, f))));
                line(n + " utf8", safe(() -> new String(JSON.toJSONBytes(root, StandardCharsets.UTF_8, new JSONWriter.Context(p, f)), StandardCharsets.UTF_8)));
                line(n + " jsonb", safe(() -> JSONB.toBytes(root, new JSONWriter.Context(p, f))));
            }
            // context settings beyond the feature word: date format, zone, and a filter on the context
            JSONWriter.Context fmt = new JSONWriter.Context(p);
            fmt.setDateFormat("yyyy-MM-dd");
            line(label + " " + rn + " ctx dateFormat json", safe(() -> JSON.toJSONString(root, fmt)));
            JSONWriter.Context fmtKeys = new JSONWriter.Context(p, JSONWriter.Feature.WriteNonStringKeyAsString);
            fmtKeys.setDateFormat("yyyy-MM-dd");
            line(label + " " + rn + " ctx dateFormat+NonStringKey json", safe(() -> JSON.toJSONString(root, fmtKeys)));
            JSONWriter.Context filtered = new JSONWriter.Context(p);
            filtered.configFilter((ValueFilter) (o, name, v) -> "zebra".equals(name) ? -1 : v, (NameFilter) (o, name, v) -> "tag".equals(name) ? "TAG" : name);
            line(label + " " + rn + " ctx filters json", safe(() -> JSON.toJSONString(root, filtered)));
            line(label + " " + rn + " ctx filters jsonb", safe(() -> JSONB.toBytes(root, filtered)));
        }
    }

    static void readAll(String label) {
        String[] texts = {
                "{\"a\":[{\"x\":1,\"x\":2},[{\"y\":1}],{\"@type\":\"java.util.HashMap\",\"z\":1}],\"b\":{\"c\":[1,2,{\"d\":[]}]}}",
                "[{\"x\":1},[{\"x\":[{\"y\":null}]}],{\"@type\":\"x\",\"@type\":\"y\"},{\"$ref\":\"$[0]\"}]",
                "{\"@type\":\"java.util.LinkedHashMap\",\"k\":[{\"a\":1,\"a\":2}]}",
                "{\"m\":{\"k\":[{\"zebra\":5,\"apple\":\"x\"}]},\"n\":[{\"zebra\":6}]}",
        };
        JSONReader.Feature[][] fs = {{}, {JSONReader.Feature.SupportAutoType}, {JSONReader.Feature.DuplicateKeyValueAsArray},
                {JSONReader.Feature.UseNativeObject}, {JSONReader.Feature.IgnoreNullPropertyValue}, {JSONReader.Feature.UseBigDecimalForDoubles}};
        Type mapOfLists = new TypeReference<Map<String, List<Child>>>() { }.getType();
        Type mapOfMaps = new TypeReference<Map<String, Map<String, Object>>>() { }.getType();
        for (int i = 0; i < texts.length; i++) {
            String t = texts[i];
            for (JSONReader.Feature[] f : fs) {
                String n = label + " text" + i + " " + Arrays.toString(f);
                line(n + " parse", safe(() -> JSON.toJSONString(JSON.parse(t, f), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls)));
                line(n + " parseObject", safe(() -> JSON.toJSONString(JSON.parseObject(t, f), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls)));
                line(n + " parseArray", safe(() -> JSON.toJSONString(JSON.parseArray(t, f), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls)));
                line(n + " Map<String,List<Child>>", safe(() -> JSON.toJSONString(JSON.parseObject(t, mapOfLists, f), JSONWriter.Feature.WriteNulls)));
                line(n + " Map<String,Map>", safe(() -> JSON.toJSONString(JSON.parseObject(t, mapOfMaps, f), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls)));
                line(n + " Object", safe(() -> JSON.toJSONString(JSON.parseObject(t, Object.class, f), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls)));
                line(n + " utf8 parse", safe(() -> JSON.toJSONString(JSON.parse(t.getBytes(StandardCharsets.UTF_8), f), JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNulls)));
                line(n + " jsonb roundtrip", safe(() -> JSON.toJSONString(JSONB.parse(JSONB.toBytes(JSON.parse(t))), JSONWriter.Feature.WriteClassName)));
            }
        }
    }

    public static void main(String[] args) {
        writeAll("default provider", JSONFactory.getDefaultObjectWriterProvider());
        writeAll("reflect provider", new ObjectWriterProvider(ObjectWriterCreator.INSTANCE));
        ObjectWriterProvider custom = new ObjectWriterProvider();
        custom.register(Long.class, CUSTOM_LONG);
        custom.register(BigDecimal.class, CUSTOM_DECIMAL);
        writeAll("custom Long/BigDecimal writers", custom);
        line("tree from(holder)", safe(() -> JSONObject.from(new Holder())));
        line("tree from(annotated)", safe(() -> JSONObject.from(new Annotated())));
        readAll("read");
    }
}
