package p19c;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/** A Map-declared field after the provider cell of its runtime Map class is warm: typed or untyped map writer? */
public class Q19c {
    @JSONType(alphabetic = false)
    public static class V {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class WithMap {
        public Map<String, V> entries = new LinkedHashMap<>();
        public int id = 9;

        public WithMap() {
            entries.put("k", new V());
        }
    }

    public static void main(String[] args) {
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            for (boolean warm : new boolean[]{false, true}) {
                ObjectWriterProvider p = new ObjectWriterProvider(creator);
                if (warm) {
                    JSON.toJSONString(new LinkedHashMap<>(), new JSONWriter.Context(p));
                }
                String s = JSON.toJSONString(new WithMap(), new JSONWriter.Context(p, JSONWriter.Feature.WriteClassName));
                System.out.println((creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm") + (warm ? " warm " : " cold ") + s);
            }
        }
    }
}
