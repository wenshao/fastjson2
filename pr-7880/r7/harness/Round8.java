package verify;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static verify.Verify7880.expect;

/**
 * Round-7 additions for 6bf404c01 (section K): map keys written through the field-level sort branch of
 * ObjectWriterImplMap#mapKeyToString. The oracle is the same key written through the context branch
 * (JSON.toJSONString(key, context) with the sort feature on the context), which is the path main uses.
 */
public class Round8 {
    public static class Inner {
        public int a = 1;
    }

    @JSONType(alphabetic = false)
    public static class Key {
        public String zeta = "z";
        public Inner first = new Inner();
        public Inner second = first;
        public String password = "hunter2";
    }

    public static class FieldSorted {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<Key, String> m = new LinkedHashMap<>(Collections.singletonMap(new Key(), "v"));
    }

    public static class FieldSortedDays {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<LocalDate, String> byDay = new LinkedHashMap<>(Collections.singletonMap(LocalDate.of(2026, 10, 6), "v"));
    }

    public static class Plain {
        public Map<Key, String> m = new LinkedHashMap<>(Collections.singletonMap(new Key(), "v"));
    }

    /** the single key of {"m":{"<key>":"v"}}, unescaped */
    static String keyOf(String json) {
        return JSON.parseObject(json).getJSONObject("m").keySet().iterator().next();
    }

    static void run() {
        System.out.println("[K1] field-level sorted bean keys are the root of their own references (ReferenceDetection)");
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            ObjectWriterProvider provider = new ObjectWriterProvider(creator);
            String got = keyOf(JSON.toJSONString(new FieldSorted(), new JSONWriter.Context(provider, JSONWriter.Feature.ReferenceDetection)));
            String oracle = JSON.toJSONString(new Key(), new JSONWriter.Context(provider,
                    JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.SortFieldNamesAlphabetically));
            expect("K1", creator.getClass().getSimpleName() + ": key equals the key written with the sort feature on the context",
                    got.equals(oracle), "field-level " + got + "  context " + oracle);
        }
        System.out.println();

        System.out.println("[K2] field-level sorted keys keep the caller's context (date format, filters)");
        JSONWriter.Context dated = new JSONWriter.Context();
        dated.setDateFormat("yyyy/MM/dd");
        String days = JSON.toJSONString(new FieldSortedDays(), dated);
        expect("K2", "LocalDate key keeps the context date format (no filter: sort comes from the field)",
                days.equals("{\"byDay\":{\"2026/10/06\":\"v\"}}"), days);
        dated.configFilter((PropertyFilter) (o, n, v) -> true);
        days = JSON.toJSONString(new FieldSortedDays(), dated);
        expect("K2", "LocalDate key keeps the context date format (with a filter: sort is merged into the context)",
                days.equals("{\"byDay\":{\"2026/10/06\":\"v\"}}"), days);
        JSONWriter.Context masked = new JSONWriter.Context();
        masked.configFilter((ValueFilter) (o, n, v) -> "password".equals(n) ? "***" : v);
        String key = keyOf(JSON.toJSONString(new FieldSorted(), masked));
        expect("K2", "context ValueFilter masks a field-level sorted bean key", key.contains("***") && !key.contains("hunter2"), key);
        System.out.println();

        System.out.println("[K3] field-level sort selects the key's sorted writer; the default branch is main's");
        String sorted = keyOf(JSON.toJSONString(new FieldSorted()));
        expect("K3", "@JSONType(alphabetic=false) key is written in sorted order",
                sorted.equals("{\"first\":{\"a\":1},\"password\":\"hunter2\",\"second\":{\"a\":1},\"zeta\":\"z\"}"), sorted);
        JSONWriter.Context filtered = new JSONWriter.Context(JSONWriter.Feature.ReferenceDetection);
        filtered.configFilter((PropertyFilter) (o, n, v) -> true);
        String plain = keyOf(JSON.toJSONString(new Plain(), filtered));
        String main = JSON.toJSONString(new Key(), filtered);
        expect("K3", "unsorted key on the filtering path equals JSON.toJSONString(key, context)", plain.equals(main), plain);
        System.out.println();
    }
}
