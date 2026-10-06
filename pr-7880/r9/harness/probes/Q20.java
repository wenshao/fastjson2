package p20;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Q20 {
    public static class G { public Map<String, Object> m; }
    public static class F0 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public List<Map<String, Object>> f; }
    public static class P0 { public List<Map<String, Object>> f; }
    public static class F1 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, Object>[] f; }
    public static class P1 { public Map<String, Object>[] f; }
    public static class F2 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Set<Map<String, Object>> f; }
    public static class P2 { public Set<Map<String, Object>> f; }
    public static class F3 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Collection<Map<String, Object>> f; }
    public static class P3 { public Collection<Map<String, Object>> f; }
    public static class F4 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public ArrayList<HashMap<String, Object>> f; }
    public static class P4 { public ArrayList<HashMap<String, Object>> f; }
    public static class F5 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Optional<Map<String, Object>> f; }
    public static class P5 { public Optional<Map<String, Object>> f; }
    public static class F6 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public AtomicReference<Map<String, Object>> f; }
    public static class P6 { public AtomicReference<Map<String, Object>> f; }
    public static class F7 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, Map<String, Object>> f; }
    public static class P7 { public Map<String, Map<String, Object>> f; }
    public static class F8 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, List<Map<String, Object>>> f; }
    public static class P8 { public Map<String, List<Map<String, Object>>> f; }
    public static class F9 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public List<List<Map<String, Object>>> f; }
    public static class P9 { public List<List<Map<String, Object>>> f; }
    public static class F10 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public List<Map<String, Object>> f; }
    public static class P10 { public List<Map<String, Object>> f; }
    public static class F11 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, Object> f; }
    public static class P11 { public Map<String, Object> f; }
    public static class F12 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, Object> f; }
    public static class P12 { public Map<String, Object> f; }
    public static class F13 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, Object> f; }
    public static class P13 { public Map<String, Object> f; }
    public static class F14 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Object f; }
    public static class P14 { public Object f; }
    public static class F15 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Object f; }
    public static class P15 { public Object f; }
    public static class F16 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public List<Object> f; }
    public static class P16 { public List<Object> f; }
    public static class F17 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public JSONObject f; }
    public static class P17 { public JSONObject f; }
    public static class F18 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public JSONObject f; }
    public static class P18 { public JSONObject f; }
    public static class F19 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public JSONArray f; }
    public static class P19 { public JSONArray f; }
    public static class F20 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public List<JSONObject> f; }
    public static class P20 { public List<JSONObject> f; }
    public static class F21 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, Integer> f; }
    public static class P21 { public Map<String, Integer> f; }
    public static class F22 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, String> f; }
    public static class P22 { public Map<String, String> f; }
    public static class F23 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<Integer, Object> f; }
    public static class P23 { public Map<Integer, Object> f; }
    public static class F24 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public G f; }
    public static class P24 { public G f; }
    public static class F25 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public List<G> f; }
    public static class P25 { public List<G> f; }
    public static class F26 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<String, G> f; }
    public static class P26 { public Map<String, G> f; }
    public static class F27 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<Map<String, Object>, String> f; }
    public static class P27 { public Map<Map<String, Object>, String> f; }
    public static class F28 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<List<Object>, String> f; }
    public static class P28 { public Map<List<Object>, String> f; }
    public static class F29 { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public Map<Object, String> f; }
    public static class P29 { public Map<Object, String> f; }
    static final String[][] SHAPES = {{"List<Map<String, Object>>", "[{\"b\":1,\"b\":2}]"},
            {"Map<String, Object>[]", "[{\"b\":1,\"b\":2}]"},
            {"Set<Map<String, Object>>", "[{\"b\":1,\"b\":2}]"},
            {"Collection<Map<String, Object>>", "[{\"b\":1,\"b\":2}]"},
            {"ArrayList<HashMap<String, Object>>", "[{\"b\":1,\"b\":2}]"},
            {"Optional<Map<String, Object>>", "{\"b\":1,\"b\":2}"},
            {"AtomicReference<Map<String, Object>>", "{\"b\":1,\"b\":2}"},
            {"Map<String, Map<String, Object>>", "{\"k\":{\"b\":1,\"b\":2}}"},
            {"Map<String, List<Map<String, Object>>>", "{\"k\":[{\"b\":1,\"b\":2}]}"},
            {"List<List<Map<String, Object>>>", "[[{\"b\":1,\"b\":2}]]"},
            {"List<Map<String, Object>> (depth 2)", "[{\"x\":{\"b\":1,\"b\":2}}]"},
            {"Map<String, Object>", "{\"b\":1,\"b\":2}"},
            {"Map<String, Object> (depth 2)", "{\"x\":{\"b\":1,\"b\":2}}"},
            {"Map<String, Object> (in list)", "{\"x\":[{\"b\":1,\"b\":2}]}"},
            {"Object", "{\"b\":1,\"b\":2}"},
            {"Object (in list)", "[{\"b\":1,\"b\":2}]"},
            {"List<Object>", "[{\"b\":1,\"b\":2}]"},
            {"JSONObject", "{\"b\":1,\"b\":2}"},
            {"JSONObject (depth 2)", "{\"x\":{\"b\":1,\"b\":2}}"},
            {"JSONArray", "[{\"b\":1,\"b\":2}]"},
            {"List<JSONObject>", "[{\"b\":1,\"b\":2}]"},
            {"Map<String, Integer>", "{\"b\":1,\"b\":2}"},
            {"Map<String, String>", "{\"b\":\"1\",\"b\":\"2\"}"},
            {"Map<Integer, Object>", "{\"1\":1,\"1\":2}"},
            {"G", "{\"m\":{\"b\":1,\"b\":2}}"},
            {"List<G>", "[{\"m\":{\"b\":1,\"b\":2}}]"},
            {"Map<String, G>", "{\"k\":{\"m\":{\"b\":1,\"b\":2}}}"},
            {"Map<Map<String, Object>, String>", "{{\"a\":1,\"a\":2}:\"v\"}"},
            {"Map<List<Object>, String>", "{[{\"a\":1,\"a\":2}]:\"v\"}"},
            {"Map<Object, String>", "{{\"a\":1,\"a\":2}:\"v\"}"}};

    static String parse(ObjectReaderProvider p, Class<?> c, String json, boolean utf8, JSONReader.Feature... f) {
        JSONReader.Context ctx = new JSONReader.Context(p, f);
        try (JSONReader r = utf8 ? JSONReader.of(json.getBytes(StandardCharsets.UTF_8), ctx) : JSONReader.of(json, ctx)) {
            Object o = r.read(c);
            return "ok " + String.valueOf(c.getField("f").get(o)).replaceAll("@[0-9a-f]{5,8}", "@H");
        } catch (Throwable e) {
            boolean dup = false;
            for (Throwable t = e; t != null; t = t.getCause()) {
                dup |= String.valueOf(t.getMessage()).contains("duplicate key");
            }
            return (dup ? "DUP " : "EXC " + e.getMessage() + " ") + e.getClass().getSimpleName();
        }
    }

    public static void main(String[] args) throws Exception {
        for (String cn : new String[]{"asm", "reflect"}) {
            for (String ch : new String[]{"string", "utf8"}) {
                for (int i = 0; i < SHAPES.length; i++) {
                    ObjectReaderProvider p = new ObjectReaderProvider("asm".equals(cn) ? ObjectReaderCreatorASM.INSTANCE : ObjectReaderCreator.INSTANCE);
                    Class<?> f = Class.forName("p20.Q20$F" + i), pl = Class.forName("p20.Q20$P" + i);
                    String dup = "{\"f\":" + SHAPES[i][1] + "}";
                    String nodup = dup.replace("\"b\":2", "\"c\":2").replace("\"a\":2", "\"z\":2").replace("\"1\":2", "\"2\":2").replace("\"b\":\"2\"", "\"c\":\"2\"");
                    boolean u = "utf8".equals(ch);
                    System.out.println(cn + "\t" + ch + "\t" + SHAPES[i][0]
                            + "\tfield=" + parse(p, f, dup, u)
                            + "\tcontext=" + parse(p, pl, dup, u, JSONReader.Feature.ErrorOnDuplicateKeys)
                            + "\tnone=" + parse(p, pl, dup, u)
                            + "\tfield-nodup=" + parse(p, f, nodup, u)
                            + "\tcontext-nodup=" + parse(p, pl, nodup, u, JSONReader.Feature.ErrorOnDuplicateKeys));
                }
            }
        }
    }
}
