package bench;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Default-path regression benchmarks (run against both jars) plus opt-in feature cost (PR jar only).
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Bench7880 {
    public static class Image {
        public String uri = "http://javaone.com/keynote_large.jpg";
        public String title = "Javaone Keynote";
        public int width = 1024;
        public int height = 768;
        public String size = "LARGE";
    }

    public static class Media {
        public String uri = "http://javaone.com/keynote.mpg";
        public String title = "Javaone Keynote";
        public int width = 640;
        public int height = 480;
        public String format = "video/mpg4";
        public long duration = 18000000;
        public long size = 58982400;
        public int bitrate = 262144;
        public boolean hasBitrate = true;
        public List<String> persons = Arrays.asList("Bill Gates", "Steve Jobs");
        public String player = "JAVA";
        public String copyright;
    }

    public static class MediaContent {
        public Media media = new Media();
        public List<Image> images = Arrays.asList(new Image(), new Image());
    }

    @JSONType(alphabetic = false)
    public static class Item {
        public long zId = 42;
        public String name = "item-name";
        public int qty = 3;
        public double price = 12.5;
        public String sku = "SKU-0001";
    }

    @JSONType(alphabetic = false)
    public static class Order {
        public String orderId = "o-0001";
        public List<Item> items = new ArrayList<>();
        public Map<String, Item> byKey = new LinkedHashMap<>();
        public Item primary = new Item();
    }

    MediaContent mediaContent;
    List<Item> itemList;
    Map<String, Item> itemMap;
    Order order;
    String objectText;
    String mapIntText;
    String mediaText;
    JSONWriter.Feature sorted;
    JSONReader.Feature errorOnDup;

    @Setup
    public void setup() {
        mediaContent = new MediaContent();
        itemList = new ArrayList<>();
        itemMap = new LinkedHashMap<>();
        order = new Order();
        for (int i = 0; i < 100; i++) {
            itemList.add(new Item());
            itemMap.put("k" + i, new Item());
        }
        for (int i = 0; i < 10; i++) {
            order.items.add(new Item());
            order.byKey.put("k" + i, new Item());
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < 40; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\"field").append(i).append("\":");
            switch (i % 4) {
                case 0: sb.append(i * 31); break;
                case 1: sb.append("\"value-").append(i).append('"'); break;
                case 2: sb.append("{\"a\":").append(i).append(",\"b\":\"x\",\"c\":[1,2,3]}"); break;
                default: sb.append("true"); break;
            }
        }
        objectText = sb.append('}').toString();
        StringBuilder mi = new StringBuilder("{");
        for (int i = 0; i < 64; i++) {
            mi.append(i > 0 ? "," : "").append("\"key").append(i).append("\":").append(i * 7);
        }
        mapIntText = mi.append('}').toString();
        mediaText = JSON.toJSONString(mediaContent);
        try {
            sorted = JSONWriter.Feature.valueOf("SortFieldNamesAlphabetically");
            errorOnDup = JSONReader.Feature.valueOf("ErrorOnDuplicateKeys");
        } catch (IllegalArgumentException baseline) {
            sorted = null;
            errorOnDup = null;
        }
    }

    // ---------------------------------------------------------------- default path (both jars)
    @Benchmark
    public String writeMediaContent() {
        return JSON.toJSONString(mediaContent);
    }

    @Benchmark
    public byte[] writeMediaContentUTF8() {
        return JSON.toJSONBytes(mediaContent);
    }

    @Benchmark
    public byte[] writeMediaContentJSONB() {
        return JSONB.toBytes(mediaContent);
    }

    @Benchmark
    public String writeList100Beans() {
        return JSON.toJSONString(itemList);
    }

    @Benchmark
    public String writeMap100Beans() {
        return JSON.toJSONString(itemMap);
    }

    @Benchmark
    public byte[] writeOrderJSONB() {
        return JSONB.toBytes(order);
    }

    @Benchmark
    public JSONObject parseJSONObject40() {
        return JSON.parseObject(objectText);
    }

    @Benchmark
    public Map<String, Integer> parseMapStringInteger64() {
        return JSON.parseObject(mapIntText, new TypeReference<Map<String, Integer>>() {});
    }

    @Benchmark
    public MediaContent parseMediaContentBean() {
        return JSON.parseObject(mediaText, MediaContent.class);
    }

    // ---------------------------------------------------------------- opt-in features (PR jar only)
    @Benchmark
    public String featureWriteOrderNatural() {
        return JSON.toJSONString(order);
    }

    @Benchmark
    public String featureWriteOrderSorted() {
        return JSON.toJSONString(order, sorted);
    }

    @Benchmark
    public String featureWriteList100Sorted() {
        return JSON.toJSONString(itemList, sorted);
    }

    @Benchmark
    public JSONObject featureParseJSONObject40ErrorOnDup() {
        return JSON.parseObject(objectText, errorOnDup);
    }
}
