package p15;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.*;

/** Round-8 probe (T): a List key under a field-level sort, per creator, with and without a context filter. */
public class Q15 {
    @JSONType(alphabetic = false)
    public static class B {
        public int z = 1;
        public int a = 2;
    }

    public static class Holder {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<Object, String> m = new LinkedHashMap<>(Collections.singletonMap(new ArrayList<>(Collections.singletonList(new B())), "v"));
    }

    public static class ValueHolder {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public List<B> list = new ArrayList<>(Collections.singletonList(new B()));
    }

    public static void main(String[] args) {
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            for (boolean filter : new boolean[]{false, true}) {
                JSONWriter.Context c = new JSONWriter.Context(new ObjectWriterProvider(creator));
                if (filter) {
                    c.configFilter((PropertyFilter) (o, n, v) -> true);
                }
                String out = JSON.toJSONString(new Holder(), c);
                String key = JSON.parseObject(out).getJSONObject("m").keySet().iterator().next();
                System.out.println(cn + (filter ? " filter" : " -     ") + " key " + key + "   value " + JSON.toJSONString(new ValueHolder(), c));
            }
        }
    }
}
