package p12;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import p11.Q11;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/**
 * Round-8 probe: do writes of a field-level sorted holder (S) and a plain holder (P) with the same declared field
 * type leak state into each other through shared nested writers? One provider per sequence; every output must equal
 * the output of the same holder written alone on a fresh provider.
 */
public class Q12 {
    static String out(Object o, ObjectWriterProvider p, String ch) {
        JSONWriter.Context c = new JSONWriter.Context(p);
        try {
            switch (ch) {
                case "json":
                    return JSON.toJSONString(o, c);
                case "utf8":
                    return new String(JSON.toJSONBytes(o, StandardCharsets.UTF_8, c), StandardCharsets.UTF_8);
                default:
                    return JSON.toJSONString(JSONB.parse(JSONB.toBytes(o, c)));
            }
        } catch (Throwable e) {
            return "EXC " + e;
        }
    }

    public static void main(String[] args) throws Exception {
        java.lang.reflect.Method shapesM = Q11.class.getDeclaredMethod("shapes");
        shapesM.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Supplier<Object[]>> shapes = (Map<String, Supplier<Object[]>>) shapesM.invoke(null);
        int cells = 0, bad = 0;
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            String cn = creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm";
            for (String ch : new String[]{"json", "utf8", "jsonb"}) {
                for (Map.Entry<String, Supplier<Object[]>> e : shapes.entrySet()) {
                    Object[] h = e.getValue().get();
                    String refS = out(h[0], new ObjectWriterProvider(creator), ch);
                    String refP = out(h[1], new ObjectWriterProvider(creator), ch);
                    for (String seq : new String[]{"PSPS", "SPSP"}) {
                        ObjectWriterProvider p = new ObjectWriterProvider(creator);
                        StringBuilder sb = new StringBuilder();
                        for (char c : seq.toCharArray()) {
                            Object[] hh = e.getValue().get();
                            String o = out(c == 'S' ? hh[0] : hh[1], p, ch);
                            cells++;
                            if (!o.equals(c == 'S' ? refS : refP)) {
                                sb.append(' ').append(c).append('=').append(o);
                            }
                        }
                        if (sb.length() > 0) {
                            bad++;
                            System.out.println(String.format("  %-7s %-5s %-32s %s:%s   (fresh S=%s P=%s)", cn, ch, e.getKey(), seq, sb, refS, refP));
                        }
                    }
                }
            }
        }
        System.out.println("L1 field-level sorted vs plain holders sharing nested writers: writes=" + cells + " sequences-with-leak=" + bad);
    }
}
