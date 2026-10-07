package p27g;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

/** Type-level SortFieldNamesAlphabetically: positional (BeanToArray) round trip, and dependence on the first write's context. */
public class Q27g {
    @JSONType(alphabetic = false, serializeFeatures = JSONWriter.Feature.SortFieldNamesAlphabetically)
    public static class TypeSorted {
        public int zulu = 9;
        public int apple = 1;
    }

    static ObjectWriterCreator[] creators() {
        return new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE};
    }

    static String cn(ObjectWriterCreator c) {
        return c == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
    }

    static String rt(TypeSorted t) {
        return "zulu=" + t.zulu + ",apple=" + t.apple;
    }

    public static void main(String[] args) {
        System.out.println("[G2] positional round trip (writer BeanToArray, reader SupportArrayToBean); written zulu=9, apple=1");
        for (ObjectWriterCreator c : creators()) {
            ObjectWriterProvider p = new ObjectWriterProvider(c);
            String arr = JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p, JSONWriter.Feature.BeanToArray));
            TypeSorted back = JSON.parseObject(arr, TypeSorted.class, JSONReader.Feature.SupportArrayToBean);
            byte[] jsonb = JSONB.toBytes(new TypeSorted(), new JSONWriter.Context(p, JSONWriter.Feature.BeanToArray));
            TypeSorted backB = JSONB.parseObject(jsonb, TypeSorted.class, JSONReader.Feature.SupportArrayToBean);
            System.out.printf("  %-8s JSON %-7s -> %-18s JSONB -> %s%n", cn(c), arr, rt(back), rt(backB));
        }
        System.out.println("[G3] first write decides: fresh provider, write A then B");
        for (ObjectWriterCreator c : creators()) {
            ObjectWriterProvider p1 = new ObjectWriterProvider(c);
            String plainFirst = JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p1));
            String btaSecond = JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p1, JSONWriter.Feature.BeanToArray));
            ObjectWriterProvider p2 = new ObjectWriterProvider(c);
            String btaFirst = JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p2, JSONWriter.Feature.BeanToArray));
            String plainSecond = JSON.toJSONString(new TypeSorted(), new JSONWriter.Context(p2));
            System.out.printf("  %-8s plain then BTA: %s %s   BTA then plain: %s %s%n", cn(c), plainFirst, btaSecond, btaFirst, plainSecond);
        }
    }
}
