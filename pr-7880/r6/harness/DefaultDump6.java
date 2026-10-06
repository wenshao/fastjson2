package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;

import java.math.BigDecimal;
import java.util.*;

/**
 * Round-6 dump: tree conversion (JSONObject.from / JSONArray.from / JSON.toJSON) under every JSONWriter.Feature, the
 * path whose feature merge 2dd085431 rewrote. Uses only API present on main, so the same class runs on every jar.
 */
public class DefaultDump6 {
    public enum Color { RED, GREEN }

    @JSONType(alphabetic = false)
    public static class Child {
        public int zebra = 3;
        public String apple;
    }

    public static class Mixed {
        public String nothing;
        public Long big = 9007199254740993L;
        public BigDecimal price = new BigDecimal("1.50");
        public Color color = Color.GREEN;
        public Date when = new Date(0);
        public List<Object> list = new ArrayList<>(Arrays.asList(1L, null, "s", Color.RED, new BigDecimal("2.0"), new Child()));
        public Map<String, Object> map = new LinkedHashMap<>();
        public Child child = new Child();
        public Child[] children = {new Child()};
        public boolean flag;
        public int[] ints = {1, 2};

        public Mixed() {
            map.put("b", null);
            map.put("a", 3L);
            map.put("c", new Child());
        }
    }

    static String render(Object o) {
        return (o == null ? "null" : o.getClass().getSimpleName()) + ":"
                + JSON.toJSONString(o, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteMapNullValue);
    }

    interface Call {
        Object call();
    }

    static String safe(Call c) {
        try {
            return render(c.call());
        } catch (Throwable e) {
            return "throws " + e.getClass().getSimpleName();
        }
    }

    public static void main(String[] args) {
        for (JSONWriter.Feature f : JSONWriter.Feature.values()) {
            if (f.name().equals("SortFieldNamesAlphabetically")) {
                continue; // absent on main; covered by the harness
            }
            System.out.println(f + " from     = " + safe(() -> JSONObject.from(new Mixed(), f)));
            System.out.println(f + " arr.from = " + safe(() -> JSONArray.from(Arrays.asList(new Mixed(), new Child()), f)));
            System.out.println(f + " toJSON   = " + safe(() -> JSON.toJSON(new Mixed(), f)));
        }
        System.out.println("none from = " + safe(() -> JSONObject.from(new Mixed())));
        System.out.println("none toJSON = " + safe(() -> JSON.toJSON(new Mixed())));
    }
}
