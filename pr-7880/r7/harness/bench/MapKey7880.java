package bench9;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import org.openjdk.jmh.annotations.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Non-String map keys on the filtering write path (ObjectWriterImplMap#mapKeyToString); runs on main too. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MapKey7880 {
    public static class Key {
        public int id;
        public String name;
        public Key() {}
        Key(int id) { this.id = id; this.name = "k" + id; }
    }
    public static class DateKeys { public Map<LocalDate, Integer> m = new LinkedHashMap<>(); }
    public static class BeanKeys { public Map<Key, Integer> m = new LinkedHashMap<>(); }

    DateKeys dateKeys = new DateKeys();
    BeanKeys beanKeys = new BeanKeys();
    JSONWriter.Context filtered;

    @Setup
    public void setup() {
        LocalDate d = LocalDate.of(2026, 1, 1);
        for (int i = 0; i < 100; i++) {
            dateKeys.m.put(d.plusDays(i), i);
        }
        for (int i = 0; i < 50; i++) {
            beanKeys.m.put(new Key(i), i);
        }
        filtered = new JSONWriter.Context();
        filtered.configFilter((PropertyFilter) (o, n, v) -> true);
    }

    @Benchmark
    public String dateKeysFiltered() {
        return JSON.toJSONString(dateKeys, filtered);
    }

    @Benchmark
    public String beanKeysFiltered() {
        return JSON.toJSONString(beanKeys, filtered);
    }
}
