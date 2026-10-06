package p9;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

/**
 * Map keys written through the field-level sort branch of ObjectWriterImplMap#mapKeyToString (6bf404c01)
 * compared with the context branch (JSON.toJSONString(key, context)), which sets rootObject and path.
 */
public class Q9 {
    public static class Inner { public int a = 1; }

    public static class Key {
        public String zeta = "z";
        public Inner first;
        public Inner second;
        public String alpha = "a";
        public String password = "hunter2";
    }

    public static class Plain { public Map<Key, String> m; }

    public static class Sorted {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<Key, String> m;
    }

    public static class SortedDays {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<LocalDate, String> byDay;
    }

    static Key key(boolean shared) {
        Key k = new Key();
        k.first = new Inner();
        k.second = shared ? k.first : new Inner();
        return k;
    }

    static void run(String label, Object holder, JSONWriter.Context ctx) {
        String out;
        try {
            out = JSON.toJSONString(holder, ctx);
        } catch (Throwable e) {
            Throwable c = e;
            while (c.getCause() != null) c = c.getCause();
            out = "EXCEPTION " + e.getClass().getSimpleName() + " / root " + c.getClass().getSimpleName() + ": " + c.getMessage();
        }
        System.out.println(label + " -> " + out);
    }

    static JSONWriter.Context ctx(JSONWriter.Feature... f) { return new JSONWriter.Context(f); }

    public static void main(String[] args) {
        for (boolean shared : new boolean[]{false, true}) {
            String s = shared ? "shared Inner" : "distinct Inner";
            Plain p = new Plain(); p.m = Collections.singletonMap(key(shared), "v");
            Sorted q = new Sorted(); q.m = Collections.singletonMap(key(shared), "v");
            run("ReferenceDetection, " + s + ", no field sort   ", p, ctx(JSONWriter.Feature.ReferenceDetection));
            run("ReferenceDetection, " + s + ", field-level sort", q, ctx(JSONWriter.Feature.ReferenceDetection));
            run("no features,        " + s + ", field-level sort", q, ctx());
        }
        Sorted q = new Sorted(); q.m = Collections.singletonMap(key(false), "v");
        JSONWriter.Context c1 = ctx();
        c1.configFilter((ValueFilter) (o, n, v) -> "password".equals(n) ? "***" : v);
        run("masking ValueFilter, field-level sort            ", q, c1);
        SortedDays d = new SortedDays(); d.byDay = Collections.singletonMap(LocalDate.of(2026, 10, 6), "v");
        JSONWriter.Context c2 = ctx();
        c2.setDateFormat("yyyy/MM/dd");
        c2.configFilter((PropertyFilter) (o, n, v) -> true);
        run("dateFormat + PropertyFilter, field-level sort    ", d, c2);
        JSONWriter.Context c3 = ctx(JSONWriter.Feature.SortFieldNamesAlphabetically, JSONWriter.Feature.ReferenceDetection);
        Plain p = new Plain(); p.m = Collections.singletonMap(key(true), "v");
        run("context sort + ReferenceDetection, shared Inner  ", p, c3);
    }
}
