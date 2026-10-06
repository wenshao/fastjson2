package p26;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.util.List;

/**
 * A list field given a single object instead of an array: do field-level reader features reach that object,
 * and do the ASM and reflective creators agree? Only features that exist on main.
 */
public class Q26 {
    public static class B {
        public int b;
        public int userId;
        public String s;
    }

    public static class Unknown {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnUnknownProperties)
        public List<B> f;
    }

    public static class UnknownOne {
        @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnUnknownProperties)
        public B f;
    }

    public static class Smart {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch)
        public List<B> f;
    }

    public static class SmartOne {
        @JSONField(deserializeFeatures = JSONReader.Feature.SupportSmartMatch)
        public B f;
    }

    public static class Trim {
        @JSONField(deserializeFeatures = JSONReader.Feature.TrimString)
        public List<B> f;
    }

    public static class TrimOne {
        @JSONField(deserializeFeatures = JSONReader.Feature.TrimString)
        public B f;
    }

    public static class Empty {
        @JSONField(deserializeFeatures = JSONReader.Feature.InitStringFieldAsEmpty)
        public List<B> f;
    }

    public static class EmptyOne {
        @JSONField(deserializeFeatures = JSONReader.Feature.InitStringFieldAsEmpty)
        public B f;
    }

    static String show(Object o) {
        Object f;
        try {
            f = o.getClass().getField("f").get(o);
        } catch (Exception e) {
            return "?" + e;
        }
        B b = f instanceof List ? (((List<?>) f).isEmpty() ? null : (B) ((List<?>) f).get(0)) : (B) f;
        return b == null ? "null" : "b=" + b.b + " userId=" + b.userId + " s=" + (b.s == null ? "null" : "'" + b.s + "'");
    }

    static String parse(ObjectReaderProvider p, Class<?> c, String json) {
        try (JSONReader r = JSONReader.of(json, new JSONReader.Context(p))) {
            return show(r.read(c));
        } catch (Throwable e) {
            String m = String.valueOf(e.getMessage());
            return "EXC " + e.getClass().getSimpleName() + ": " + (m.length() > 50 ? m.substring(0, 50) : m);
        }
    }

    public static void main(String[] args) {
        Object[][] cases = {
                {"ErrorOnUnknownProperties", Unknown.class, UnknownOne.class, "{\"b\":1,\"zz\":2}"},
                {"SupportSmartMatch", Smart.class, SmartOne.class, "{\"user_id\":7}"},
                {"TrimString", Trim.class, TrimOne.class, "{\"s\":\" x \"}"},
                {"InitStringFieldAsEmpty", Empty.class, EmptyOne.class, "{\"b\":1}"},
        };
        System.out.println("feature | input form | ASM | reflect");
        for (Object[] c : cases) {
            String item = (String) c[3];
            String[][] forms = {
                    {"List<B> f, array", "{\"f\":[" + item + "]}", "list"},
                    {"List<B> f, single object", "{\"f\":" + item + "}", "list"},
                    {"B f (control)", "{\"f\":" + item + "}", "one"},
            };
            for (String[] form : forms) {
                Class<?> type = form[2].equals("list") ? (Class<?>) c[1] : (Class<?>) c[2];
                String asm = parse(new ObjectReaderProvider(ObjectReaderCreatorASM.INSTANCE), type, form[1]);
                String ref = parse(new ObjectReaderProvider(ObjectReaderCreator.INSTANCE), type, form[1]);
                System.out.println(c[0] + " | " + form[0] + " | " + asm + " | " + ref + (asm.equals(ref) ? "" : "   <-- differs"));
            }
        }
    }
}
