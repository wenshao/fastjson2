package p19;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.util.*;

/**
 * N-40: a custom ObjectWriter registered for the runtime Map class of a field value.
 * Holders differ only in the declared type of field "m". Prints what each build/creator writes.
 */
public class Q19 {
    public static class MyMap extends HashMap<String, Object> {
    }

    public static class HMap { public Map<String, Object> m; }
    public static class HMyMap { public MyMap m; }
    public static class HHashMap { public HashMap<String, Object> m; }
    public static class HObject { public Object m; }
    public static class HMapSort { @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically) public Map<String, Object> m; }

    static final ObjectWriter CUSTOM = new ObjectWriter() {
        @Override
        public void write(JSONWriter w, Object o, Object fieldName, Type fieldType, long features) {
            w.writeString("CUSTOM");
        }
    };

    static String run(Object h, ObjectWriterCreator creator, Class<?> registered, String mode) {
        ObjectWriterProvider p = new ObjectWriterProvider(creator);
        p.register(registered, CUSTOM);
        JSONWriter.Context c;
        try {
        switch (mode) {
            case "sort":
                c = new JSONWriter.Context(p, JSONWriter.Feature.SortFieldNamesAlphabetically);
                break;
            case "filter":
                c = new JSONWriter.Context(p);
                c.setValueFilter((ValueFilter) (o, n, v) -> v);
                break;
            default:
                c = new JSONWriter.Context(p);
        }
        } catch (Throwable e) {
            return "n/a";
        }
        try {
            return JSON.toJSONString(h, c);
        } catch (Throwable e) {
            return "EXC " + e;
        }
    }

    static Map<String, Object> fill(Map<String, Object> m) {
        m.put("k", 1);
        return m;
    }

    public static void main(String[] args) {
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            for (String mode : new String[]{"plain", "sort", "filter"}) {
                // custom writer for a Map subclass
                HMap a = new HMap(); a.m = fill(new MyMap());
                HMyMap b = new HMyMap(); b.m = (MyMap) fill(new MyMap());
                HObject c = new HObject(); c.m = fill(new MyMap());
                HMapSort d = new HMapSort(); d.m = fill(new MyMap());
                // custom writer for HashMap itself
                HMap e = new HMap(); e.m = fill(new HashMap<>());
                HHashMap f = new HHashMap(); f.m = (HashMap<String, Object>) fill(new HashMap<>());
                HObject g = new HObject(); g.m = fill(new HashMap<>());
                System.out.println(cn + "/" + mode + " MyMap  : Map=" + run(a, creator, MyMap.class, mode)
                        + " MyMap=" + run(b, creator, MyMap.class, mode)
                        + " Object=" + run(c, creator, MyMap.class, mode)
                        + " Map+fieldSort=" + run(d, creator, MyMap.class, mode));
                System.out.println(cn + "/" + mode + " HashMap: Map=" + run(e, creator, HashMap.class, mode)
                        + " HashMap=" + run(f, creator, HashMap.class, mode)
                        + " Object=" + run(g, creator, HashMap.class, mode));
            }
        }
    }
}
