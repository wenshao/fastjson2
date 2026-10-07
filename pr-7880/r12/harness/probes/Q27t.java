package p27t;

import com.alibaba.fastjson2.*;
import java.util.*;
/** SortMapEntriesByKeys: toJSONString vs JSON.toJSON for maps held by fields and inside a list. */
public class Q27t {
    public static class H { public Map<String, Object> m = new LinkedHashMap<>(); public List<Map<String, Object>> items = new ArrayList<>(); public Object o; }
    public static void main(String[] a) {
        H h = new H();
        h.m.put("b", 1); h.m.put("a", 2);
        Map<String, Object> it = new LinkedHashMap<>(); it.put("zeta", 1); it.put("alpha", 2);
        h.items.add(it);
        Map<String, Object> o = new LinkedHashMap<>(); o.put("y", 1); o.put("x", 2);
        h.o = o;
        JSONWriter.Feature f = JSONWriter.Feature.SortMapEntriesByKeys;
        System.out.println("toJSONString   " + JSON.toJSONString(h, f));
        JSONObject t = (JSONObject) JSON.toJSON(h, f);
        System.out.println("JSON.toJSON    " + t + "  m is the field's own map: " + (t.get("m") == h.m) + "  o is the field's own map: " + (t.get("o") == h.o));
    }
}
