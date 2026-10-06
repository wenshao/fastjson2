package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.*;

/** Does a feature from one write leak into later writes through a cached container writer? */
public class Q7b {
    @JSONType(alphabetic = false)
    public static class Child { public int zebra = 3; public String apple; public int mid = 5; }

    public static class Positional {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Map<String, Child> map = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
    }

    public static class Plain {
        public Map<String, Child> map = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
        public List<Child> list = new ArrayList<>(Collections.singletonList(new Child()));
    }

    static String w(Object o, ObjectWriterProvider p, JSONWriter.Feature... f) {
        return JSON.toJSONString(o, new JSONWriter.Context(p, f));
    }

    public static void main(String[] args) {
        System.out.println("jar " + JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", ""));
        for (String c : new String[]{"asm", "reflect"}) {
            JSONWriter.Feature[][] firsts = {{JSONWriter.Feature.FieldBased}, {JSONWriter.Feature.WriteNulls}, {JSONWriter.Feature.WriteLongAsString}};
            for (JSONWriter.Feature[] first : firsts) {
                ObjectWriterProvider fresh = c.equals("asm") ? new ObjectWriterProvider() : new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
                ObjectWriterProvider warmed = c.equals("asm") ? new ObjectWriterProvider() : new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
                w(new Positional(), warmed, first);
                w(new Plain(), warmed, first);
                System.out.println(c + " after " + Arrays.toString(first) + ":");
                System.out.println("   Positional, BeanToArray ctx : fresh " + w(new Positional(), fresh, JSONWriter.Feature.BeanToArray)
                        + "   warmed " + w(new Positional(), warmed, JSONWriter.Feature.BeanToArray));
                System.out.println("   Positional, no feature      : fresh " + w(new Positional(), fresh) + "   warmed " + w(new Positional(), warmed));
                System.out.println("   Plain, no feature           : fresh " + w(new Plain(), fresh) + "   warmed " + w(new Plain(), warmed));
            }
        }
    }
}
