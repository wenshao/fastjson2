package p19b;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.util.*;

/** Registered writers for the runtime class of field values, beyond maps: lists, sets, beans, JSONObject subclasses. */
public class Q19b {
    public static class MyList extends ArrayList<Object> {}
    public static class MySet extends LinkedHashSet<Object> {}
    public static class MyJSONObject extends com.alibaba.fastjson2.JSONObject {}
    public static class Base { public int a = 1; }
    public static class Sub extends Base { public int b = 2; }

    public static class HList { public List<Object> f = new MyList(); }
    public static class HMyList { public MyList f = new MyList(); }
    public static class HCollection { public Collection<Object> f = new MySet(); }
    public static class HSet { public Set<Object> f = new MySet(); }
    public static class HBase { public Base f = new Sub(); }
    public static class HJSONObject { public com.alibaba.fastjson2.JSONObject f = new MyJSONObject(); }
    public static class HMapJ { public Map<String, Object> f = new MyJSONObject(); }

    static final ObjectWriter CUSTOM = new ObjectWriter() {
        @Override
        public void write(JSONWriter w, Object o, Object fieldName, Type fieldType, long features) {
            w.writeString("CUSTOM");
        }
    };

    static String run(Object h, ObjectWriterCreator creator, Class<?> registered, boolean filter) {
        try {
            ObjectWriterProvider p = new ObjectWriterProvider(creator);
            p.register(registered, CUSTOM);
            JSONWriter.Context c = new JSONWriter.Context(p);
            if (filter) {
                c.setValueFilter((ValueFilter) (o, n, v) -> v);
            }
            return JSON.toJSONString(h, c);
        } catch (Throwable e) {
            return "EXC " + e;
        }
    }

    public static void main(String[] args) {
        Object[][] cases = {
                {"List<Object>=MyList", new HList(), MyList.class},
                {"MyList=MyList", new HMyList(), MyList.class},
                {"Collection=MySet", new HCollection(), MySet.class},
                {"Set=MySet", new HSet(), MySet.class},
                {"Base=Sub", new HBase(), Sub.class},
                {"JSONObject=MyJSONObject", new HJSONObject(), MyJSONObject.class},
                {"Map=MyJSONObject", new HMapJ(), MyJSONObject.class},
        };
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            for (boolean filter : new boolean[]{false, true}) {
                StringBuilder sb = new StringBuilder(cn + (filter ? "/filter" : "/plain"));
                for (Object[] c : cases) {
                    sb.append("  ").append(c[0]).append("=").append(run(c[1], creator, (Class<?>) c[2], filter));
                }
                System.out.println(sb);
            }
        }
    }
}
