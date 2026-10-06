package p25f;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.util.List;
import java.util.Map;

/** Field-level ErrorOnDuplicateKeys: a list field given a single object, and a list field of a positional (array-mapped) bean. */
public class Q25f {
    public static class Single {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public List<Map<String, Object>> f;
    }

    @JSONType(deserializeFeatures = JSONReader.Feature.SupportArrayToBean)
    public static class Positional {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public List<Map<String, Object>> rows;
    }

    static String parse(ObjectReaderProvider p, Class<?> c, String json, JSONReader.Feature... f) {
        try (JSONReader r = JSONReader.of(json, new JSONReader.Context(p, f))) {
            r.read(c);
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
            System.out.println(cn + " list field given one object: " + parse(p, Single.class, "{\"f\":{\"b\":1,\"b\":2}}"));
            System.out.println(cn + " positional bean, list field: " + parse(p, Positional.class, "[[{\"b\":1,\"b\":2}]]"));
            System.out.println(cn + " positional bean, list field (context SupportArrayToBean): " + parse(p, Positional.class, "[[{\"b\":1,\"b\":2}]]", JSONReader.Feature.SupportArrayToBean));
        }
    }
}
