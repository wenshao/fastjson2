package bench11;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Round 9: the item loops of ObjectWriterImplList that 833c666ee changed (writeJSONB for typed bean lists, write for
 * nested lists), with no sort feature anywhere. Runs on main too.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ListJsonb7880 {
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

    Basket basket = new Basket();
    List<Item> rootList = new ArrayList<>();
    List<List<Item>> nested = new ArrayList<>();

    @Setup
    public void setup() {
        for (int i = 0; i < 100; i++) {
            basket.items.add(new Item(i));
            rootList.add(new Item(i));
        }
        for (int i = 0; i < 10; i++) {
            List<Item> row = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                row.add(new Item(i * 10 + j));
            }
            nested.add(row);
        }
    }

    @Benchmark
    public byte[] basketJSONB() {
        return JSONB.toBytes(basket);
    }

    @Benchmark
    public byte[] rootListJSONB() {
        return JSONB.toBytes(rootList);
    }

    @Benchmark
    public String nestedListJSON() {
        return JSON.toJSONString(nested);
    }
}
