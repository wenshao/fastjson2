package p27f;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.writer.*;
import java.util.*;
/** Main-safe: a field-level FieldBased on Map, bean and List fields, per creator. */
public class Q27f {
    public static class FieldUser { private int hidden = 7; public int getVisible() { return 1; } }
    public static class Typed { @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased) public Map<String, FieldUser> m = new LinkedHashMap<>(Collections.singletonMap("k", new FieldUser())); }
    public static class Untyped { @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased) public Map<String, Object> m = new LinkedHashMap<>(Collections.singletonMap("k", new FieldUser())); }
    public static class BeanField { @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased) public FieldUser f = new FieldUser(); }
    public static class ListField { @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased) public List<FieldUser> l = new ArrayList<>(Collections.singletonList(new FieldUser())); }
    public static void main(String[] a) {
        for (ObjectWriterCreator c : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            StringBuilder sb = new StringBuilder(c == ObjectWriterCreator.INSTANCE ? "reflect " : "asm     ");
            for (Object o : new Object[]{new Typed(), new Untyped(), new BeanField(), new ListField()}) {
                sb.append(o.getClass().getSimpleName()).append('=').append(JSON.toJSONString(o, new JSONWriter.Context(new ObjectWriterProvider(c)))).append("  ");
            }
            System.out.println(sb);
        }
    }
}
