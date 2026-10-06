package p25;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.util.*;

/** Field-level ErrorOnDuplicateKeys on an any-setter and on a read-only (getter-only) map field. */
public class Q25 {
    public static class AnySetter {
        public Map<String, Object> extras = new LinkedHashMap<>();

        @JSONField(unwrapped = true, deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public void add(String name, Object value) {
            extras.put(name, value);
        }
    }

    public static class ReadOnly {
        private final Map<String, Object> m = new LinkedHashMap<>();

        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Object> getM() {
            return m;
        }
    }

    public static class ReadOnlyTyped {
        private final Map<String, Map<String, Object>> m = new LinkedHashMap<>();

        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Map<String, Object>> getM() {
            return m;
        }
    }

    public static class UnwrappedReadOnly {
        private final Map<String, Object> extras = new LinkedHashMap<>();

        @JSONField(unwrapped = true, deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Object> getExtras() {
            return extras;
        }
    }

    static String parse(ObjectReaderProvider p, Class<?> c, String json) {
        try (JSONReader r = JSONReader.of(json, new JSONReader.Context(p))) {
            Object o = r.read(c);
            return "ok";
        } catch (Throwable e) {
            boolean dup = false;
            for (Throwable t = e; t != null; t = t.getCause()) dup |= String.valueOf(t.getMessage()).contains("duplicate key");
            return dup ? "DUP" : "EXC " + e;
        }
    }

    public static void main(String[] args) {
        for (String cn : new String[]{"asm", "reflect"}) {
            ObjectReaderProvider p = new ObjectReaderProvider("asm".equals(cn) ? ObjectReaderCreatorASM.INSTANCE : ObjectReaderCreator.INSTANCE);
            System.out.println(cn + " any-setter nested map     " + parse(p, AnySetter.class, "{\"l\":{\"x\":1,\"x\":2}}"));
            System.out.println(cn + " any-setter map in list    " + parse(p, AnySetter.class, "{\"l\":[{\"x\":1,\"x\":2}]}"));
            System.out.println(cn + " read-only map, nested map " + parse(p, ReadOnly.class, "{\"m\":{\"k\":{\"a\":1,\"a\":2}}}"));
            System.out.println(cn + " read-only typed map       " + parse(p, ReadOnlyTyped.class, "{\"m\":{\"k\":{\"a\":1,\"a\":2}}}"));
            System.out.println(cn + " read-only map, top dup    " + parse(p, ReadOnly.class, "{\"m\":{\"k\":1,\"k\":2}}"));
            System.out.println(cn + " unwrapped read-only map   " + parse(p, UnwrappedReadOnly.class, "{\"x\":{\"a\":1,\"a\":2}}"));
        }
    }
}
