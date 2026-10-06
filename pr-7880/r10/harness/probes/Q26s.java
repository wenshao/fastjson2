package p26s;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.util.List;
import java.util.Map;

/** The same single-object shape with the PR's field-level ErrorOnDuplicateKeys. */
public class Q26s {
    public static class Strict {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public List<Map<String, Object>> f;
    }

    public static class StrictOne {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys)
        public Map<String, Object> f;
    }

    static String parse(ObjectReaderProvider p, Class<?> c, String json) {
        try (JSONReader r = JSONReader.of(json, new JSONReader.Context(p))) {
            r.read(c);
            return "ok";
        } catch (Throwable e) {
            boolean dup = false;
            for (Throwable t = e; t != null; t = t.getCause()) dup |= String.valueOf(t.getMessage()).contains("duplicate key");
            return dup ? "DUP" : "EXC " + e;
        }
    }

    public static void main(String[] args) {
        String item = "{\"b\":1,\"b\":2}";
        String[][] forms = {
                {"List<Map> f, array", "{\"f\":[" + item + "]}", "list"},
                {"List<Map> f, single object", "{\"f\":" + item + "}", "list"},
                {"Map f (control)", "{\"f\":" + item + "}", "one"},
        };
        System.out.println("feature | input form | ASM | reflect");
        for (String[] form : forms) {
            Class<?> type = form[2].equals("list") ? Strict.class : StrictOne.class;
            String asm = parse(new ObjectReaderProvider(ObjectReaderCreatorASM.INSTANCE), type, form[1]);
            String ref = parse(new ObjectReaderProvider(ObjectReaderCreator.INSTANCE), type, form[1]);
            System.out.println("ErrorOnDuplicateKeys | " + form[0] + " | " + asm + " | " + ref + (asm.equals(ref) ? "" : "   <-- differs"));
        }
    }
}
