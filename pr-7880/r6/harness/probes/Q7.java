package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.*;

/** Follow-ups on the 82514ab10 default-path dump diffs. */
public class Q7 {
    @JSONType(alphabetic = false)
    public static class Child { public int zebra = 3; public String apple; public int mid = 5; }
    @JSONType(alphabetic = false)
    public static class SubChild extends Child { public int extra = 9; }

    public static class Poly {
        @JSONField(serializeFeatures = JSONWriter.Feature.WriteNulls)
        public Map<String, Child> annotated = new LinkedHashMap<>(Collections.singletonMap("k", new SubChild()));
        public Map<String, Child> plain = new LinkedHashMap<>(Collections.singletonMap("k", new SubChild()));
    }

    public static class Positional {
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Map<String, Child> map = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public List<Child> list = new ArrayList<>(Collections.singletonList(new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Child single = new Child();
    }

    public static class Secret { public String user = "u"; public String password = "hunter2"; }
    public static class KeyedBySecret { public Map<Secret, String> m = new LinkedHashMap<>(Collections.singletonMap(new Secret(), "v")); }

    public static void main(String[] args) {
        System.out.println("jar " + JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", ""));
        ObjectWriterProvider reflect = new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
        ObjectWriterProvider asm = new ObjectWriterProvider();
        for (ObjectWriterProvider p : new ObjectWriterProvider[]{asm, reflect}) {
            String c = p == asm ? "asm" : "reflect";
            System.out.println(c + " WriteClassName, SubChild in Map<String,Child>: " + JSON.toJSONString(new Poly(), new JSONWriter.Context(p, JSONWriter.Feature.WriteClassName)));
            byte[] jsonb = JSONB.toBytes(new Poly(), new JSONWriter.Context(p, JSONWriter.Feature.WriteClassName));
            Object back = JSONB.parseObject(jsonb, Poly.class, JSONReader.Feature.SupportAutoType);
            Poly pb = (Poly) back;
            System.out.println(c + "   JSONB round trip (SupportAutoType): annotated value class=" + pb.annotated.get("k").getClass().getSimpleName()
                    + ", plain value class=" + pb.plain.get("k").getClass().getSimpleName());
            System.out.println(c + " BeanToArray context: " + JSON.toJSONString(new Positional(), new JSONWriter.Context(p, JSONWriter.Feature.BeanToArray)));
            System.out.println(c + " no context feature : " + JSON.toJSONString(new Positional(), new JSONWriter.Context(p)));
            JSONWriter.Context masked = new JSONWriter.Context(p);
            masked.configFilter((ValueFilter) (o, n, v) -> "password".equals(n) ? "***" : v);
            System.out.println(c + " ValueFilter on context, bean map key: " + JSON.toJSONString(new KeyedBySecret(), masked));
        }
    }
}
