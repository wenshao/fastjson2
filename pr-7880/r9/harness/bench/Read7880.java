package bench12;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Round 10: the paths 575bd9fa2 changed, with no feature set anywhere. Typed list fields of generated readers
 * (basketParse*), the item loop of the typed list reader (rootListParse), nested values of the untyped object
 * reader (treeParse), and filtered map values (filteredMapWrite). Runs on main too.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Read7880 {
    public static class Item {
        public int id;
        public String name;
        public double price;
        public boolean active = true;
        public Item() {}
        Item(int i) { id = i; name = "item-" + i; price = i * 1.5; }
    }

    public static class Basket {
        public String id = "b-1";
        public List<Item> items = new ArrayList<>();
    }

    static final Type LIST_ITEM = new TypeReference<List<Item>>() {}.getType();

    String basketJson;
    byte[] basketUtf8;
    String rootListJson;
    String treeJson;
    Map<String, Item> map = new LinkedHashMap<>();
    ValueFilter filter = (o, n, v) -> v;

    @Setup
    public void setup() {
        Basket basket = new Basket();
        List<Item> root = new ArrayList<>();
        List<Object> rows = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            basket.items.add(new Item(i));
            root.add(new Item(i));
            map.put("k" + i, new Item(i));
        }
        for (int i = 0; i < 20; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i);
            row.put("tags", new int[]{i, i + 1, i + 2});
            Map<String, Object> inner = new LinkedHashMap<>();
            inner.put("x", i);
            inner.put("list", new String[]{"a", "b"});
            row.put("inner", inner);
            rows.add(row);
        }
        basketJson = JSON.toJSONString(basket);
        basketUtf8 = basketJson.getBytes(StandardCharsets.UTF_8);
        rootListJson = JSON.toJSONString(root);
        Map<String, Object> tree = new LinkedHashMap<>();
        tree.put("rows", rows);
        treeJson = JSON.toJSONString(tree);
    }

    @Benchmark
    public Basket basketParse() {
        return JSON.parseObject(basketJson, Basket.class);
    }

    @Benchmark
    public Basket basketParseUTF8() {
        return JSON.parseObject(basketUtf8, Basket.class);
    }

    @Benchmark
    public List<Item> rootListParse() {
        return JSON.parseObject(rootListJson, LIST_ITEM);
    }

    @Benchmark
    public Object treeParse() {
        return JSON.parse(treeJson);
    }

    @Benchmark
    public String filteredMapWrite() {
        return JSON.toJSONString(map, filter);
    }
}
