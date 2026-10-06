package p22;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.TypeReference;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/** R2-2/R3-5: object and array map keys under ErrorOnDuplicateKeys, and the key objects read without duplicates. */
public class Q22 {
    static String desc(Object v) {
        if (v == null) return "null";
        if (v instanceof Map) {
            StringBuilder sb = new StringBuilder(v.getClass().getSimpleName()).append('{');
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) sb.append(desc(e.getKey())).append('=').append(desc(e.getValue())).append(',');
            return sb.append('}').toString();
        }
        if (v instanceof Collection) {
            StringBuilder sb = new StringBuilder(v.getClass().getSimpleName()).append('[');
            for (Object o : (Collection<?>) v) sb.append(desc(o)).append(',');
            return sb.append(']').toString();
        }
        return v.getClass().getSimpleName() + ":" + v;
    }

    static String run(Function<JSONReader.Feature[], Object> f, JSONReader.Feature... features) {
        try {
            return desc(f.apply(features));
        } catch (Throwable e) {
            boolean dup = false;
            for (Throwable t = e; t != null; t = t.getCause()) dup |= String.valueOf(t.getMessage()).contains("duplicate key");
            String m = String.valueOf(e.getMessage());
            int k = m.indexOf(", offset");
            if (k > 0) m = m.substring(0, k);
            return dup ? "DUP" : "EXC " + e.getClass().getSimpleName() + ": " + (m.length() > 60 ? m.substring(0, 60) : m);
        }
    }

    public static void main(String[] args) {
        String[][] inputs = {
                {"obj-dup", "{{\"a\":1,\"a\":2}:\"v\"}"},
                {"arr-dup", "{[{\"x\":1,\"x\":2}]:1}"},
                {"obj-nested-dup", "{{\"k\":{\"a\":1,\"a\":2}}:1}"},
                {"arr-nested-dup", "{[[{\"a\":1,\"a\":2}]]:1}"},
                {"obj", "{{\"b\":1,\"a\":2}:\"v\"}"},
                {"arr", "{[1,{\"a\":2}]:\"v\"}"},
                {"obj-type", "{{\"@type\":\"java.util.TreeMap\",\"b\":1,\"a\":2}:\"v\"}"},
                {"outer-dup", "{\"k\":1,\"k\":2}"},
        };
        Type mapObj = new TypeReference<Map<Object, Object>>() {}.getType();
        Type mapMap = new TypeReference<Map<Map<String, Object>, Object>>() {}.getType();
        Type mapList = new TypeReference<HashMap<List<Object>, Object>>() {}.getType();
        JSONReader.Feature E = JSONReader.Feature.ErrorOnDuplicateKeys;
        JSONReader.Feature[][] sets = {{}, {E}, {E, JSONReader.Feature.SupportAutoType}, {E, JSONReader.Feature.UseNativeObject}};
        String[] setNames = {"none", "EODK", "EODK+AutoType", "EODK+UseNativeObject"};
        for (String[] in : inputs) {
            String s = in[1];
            Map<String, Function<JSONReader.Feature[], Object>> apis = new LinkedHashMap<>();
            apis.put("JSON.parse", f -> JSON.parse(s, f));
            apis.put("parseObject", f -> JSON.parseObject(s, f));
            apis.put("Map<Object,Object>", f -> JSON.parseObject(s, mapObj, f));
            apis.put("Map<Map,Object>", f -> JSON.parseObject(s, mapMap, f));
            apis.put("HashMap<List,Object>", f -> JSON.parseObject(s, mapList, f));
            apis.put("reader.readObject(word)", f -> {
                long w = 0;
                for (JSONReader.Feature x : f) w |= x.mask;
                try (JSONReader r = JSONReader.of(s)) {
                    return r.readObject(w);
                }
            });
            for (Map.Entry<String, Function<JSONReader.Feature[], Object>> api : apis.entrySet()) {
                for (int i = 0; i < sets.length; i++) {
                    System.out.println(in[0] + "\t" + api.getKey() + "\t" + setNames[i] + "\t" + run(api.getValue(), sets[i]));
                }
            }
        }
    }
}
