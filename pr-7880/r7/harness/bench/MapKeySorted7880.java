package bench9;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.openjdk.jmh.annotations.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Bean map keys under a field-level SortFieldNamesAlphabetically (the PR's opt-in branch); PR builds only. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MapKeySorted7880 {
    public static class SortedBeanKeys {
        @JSONField(serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
        public Map<MapKey7880.Key, Integer> m = new LinkedHashMap<>();
    }

    SortedBeanKeys sorted = new SortedBeanKeys();

    @Setup
    public void setup() {
        for (int i = 0; i < 50; i++) {
            sorted.m.put(new MapKey7880.Key(i), i);
        }
    }

    @Benchmark
    public String beanKeysFieldSorted() {
        return JSON.toJSONString(sorted);
    }
}
