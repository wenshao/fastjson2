package p27h;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

/** Main-safe: the default property order per creator (the SortFieldNamesAlphabetically javadoc describes it). */
public class Q27h {
    public static class GettersDecl0 {
        private int zeta = 1;
        private int alpha = 2;

        public int getZeta() {
            return zeta;
        }

        public int getAlpha() {
            return alpha;
        }
    }

    @JSONType(alphabetic = false)
    public static class GettersDecl {
        private int zeta = 1;
        private int alpha = 2;

        public int getZeta() {
            return zeta;
        }

        public int getAlpha() {
            return alpha;
        }
    }

    @JSONType(alphabetic = false)
    public static class FieldsDecl {
        public int zeta = 1;
        public int alpha = 2;
    }

    static ObjectWriterCreator[] creators() {
        return new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE};
    }

    static String cn(ObjectWriterCreator c) {
        return c == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
    }

    public static void main(String[] args) {
        System.out.println("[H] default order, fields declared zeta, alpha (global alphabetic=" + System.getProperty("fastjson2.writer.alphabetic", "default") + ")");
        for (ObjectWriterCreator c : creators()) {
            for (Object o : new Object[]{new GettersDecl0(), new GettersDecl(), new FieldsDecl()}) {
                ObjectWriterProvider p = new ObjectWriterProvider(c);
                System.out.printf("  %-8s %-13s method-based %-22s FieldBased %s%n", cn(c), o.getClass().getSimpleName(),
                        JSON.toJSONString(o, new JSONWriter.Context(p)), JSON.toJSONString(o, new JSONWriter.Context(p, JSONWriter.Feature.FieldBased)));
            }
        }
    }
}
