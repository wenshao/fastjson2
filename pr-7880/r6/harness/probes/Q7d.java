package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.*;

public class Q7d {
    @JSONType(alphabetic = false)
    public static class Child {
        public int zebra = 3;
        private int hidden = 4;
        public int mid = 5;
        public int getMid() { return mid; }
    }

    public static class FB {
        @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased)
        public Map<String, Child> map = new LinkedHashMap<>(Collections.singletonMap("k", new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased)
        public List<Child> list = new ArrayList<>(Collections.singletonList(new Child()));
        @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased)
        public Child single = new Child();
    }

    public static void main(String[] args) {
        System.out.println("jar " + JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", ""));
        for (String c : new String[]{"asm", "reflect"}) {
            for (JSONWriter.Feature[] f : new JSONWriter.Feature[][]{{}, {JSONWriter.Feature.BeanToArray}, {JSONWriter.Feature.WriteNulls}}) {
                ObjectWriterProvider p = c.equals("asm") ? new ObjectWriterProvider() : new ObjectWriterProvider(ObjectWriterCreator.INSTANCE);
                System.out.println(c + " " + Arrays.toString(f) + " json : " + JSON.toJSONString(new FB(), new JSONWriter.Context(p, f)));
                System.out.println(c + " " + Arrays.toString(f) + " jsonb: " + JSONB.toJSONString(JSONB.toBytes(new FB(), new JSONWriter.Context(p, f))));
            }
        }
    }
}
