package verify;

import com.alibaba.fastjson2.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Dumps default-feature behavior using only pre-existing API, so the same class runs against
 * the baseline jar and the PR jar; the two dumps must be identical.
 */
public class DefaultDump {
    public static void main(String[] args) throws Exception {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Object> e : Verify7880.shapes().entrySet()) {
            Object o = e.getValue();
            out.append("W ").append(e.getKey()).append('\n');
            out.append("  json      ").append(JSON.toJSONString(o)).append('\n');
            out.append("  utf8      ").append(new String(JSON.toJSONBytes(o), StandardCharsets.UTF_8)).append('\n');
            out.append("  fieldBase ").append(JSON.toJSONString(o, JSONWriter.Feature.FieldBased)).append('\n');
            out.append("  sortMap   ").append(JSON.toJSONString(o, JSONWriter.Feature.SortMapEntriesByKeys)).append('\n');
            out.append("  b2a       ").append(JSON.toJSONString(o, JSONWriter.Feature.BeanToArray)).append('\n');
            out.append("  jsonb     ").append(hex(JSONB.toBytes(o))).append('\n');
            out.append("  jsonbB2A  ").append(hex(JSONB.toBytes(o, JSONWriter.Feature.BeanToArray))).append('\n');
            out.append("  jsonbFB   ").append(hex(JSONB.toBytes(o, JSONWriter.Feature.FieldBased, JSONWriter.Feature.WriteClassName))).append('\n');
        }
        String[] inputs = {
                "{\"a\":1,\"a\":2}", "{\"a\":null,\"a\":1}", "{\"a\":{\"b\":1,\"b\":[2]},\"c\":3}", "{\"k\":\"v\",\"k\":\"w\"}",
                "[{\"a\":1},{\"a\":1,\"a\":2}]", "{\"1\":1,\"1\":2}",
        };
        for (String in : inputs) {
            out.append("R ").append(in).append('\n');
            out.append("  parseObject      ").append(safe(() -> JSON.toJSONString(JSON.parseObject(in)))).append('\n');
            out.append("  parse            ").append(safe(() -> JSON.toJSONString(JSON.parse(in)))).append('\n');
            out.append("  dupAsArray       ").append(safe(() -> JSON.toJSONString(JSON.parse(in, JSONReader.Feature.DuplicateKeyValueAsArray)))).append('\n');
            out.append("  Map<String,Obj>  ").append(safe(() -> JSON.toJSONString(JSON.parseObject(in, new TypeReference<Map<String, Object>>() {})))).append('\n');
            out.append("  Map<String,Str>  ").append(safe(() -> JSON.toJSONString(JSON.parseObject(in, new TypeReference<Map<String, String>>() {})))).append('\n');
            out.append("  TreeMap          ").append(safe(() -> JSON.toJSONString(JSON.parseObject(in, TreeMap.class)))).append('\n');
            out.append("  utf8 bytes       ").append(safe(() -> JSON.toJSONString(JSON.parseObject(in.getBytes(StandardCharsets.UTF_8))))).append('\n');
            out.append("  jsonb roundtrip  ").append(safe(() -> JSON.toJSONString(JSONB.parse(JSONB.toBytes(JSON.parse(in)))))).append('\n');
        }
        System.out.print(out);
    }

    interface S {
        Object get() throws Exception;
    }

    static String safe(S s) {
        try {
            return String.valueOf(s.get());
        } catch (Throwable t) {
            return "EXC " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }
}
