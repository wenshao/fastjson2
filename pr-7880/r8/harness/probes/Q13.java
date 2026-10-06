package p13;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import p11.Q11;

import java.lang.reflect.Type;
import java.util.*;

/** Round-8 probe: the item-writer cache of a typed list writer (ObjectWriterImplList.itemClassWriter) across sort words. */
public class Q13 {
    static String w(ObjectWriter ow, ObjectWriterProvider p, Type t, long features) {
        try (JSONWriter jw = JSONWriter.of(new JSONWriter.Context(p))) {
            List<Q11.B> l = new ArrayList<>(Collections.singletonList(new Q11.B()));
            ow.write(jw, l, null, t, features);
            return jw.toString();
        }
    }

    public static void main(String[] args) {
        long sort = JSONWriter.Feature.SortFieldNamesAlphabetically.mask;
        Type t = new TypeReference<List<Q11.B>>() {}.getType();
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            for (String seq : new String[]{"SUSU", "USUS"}) {
                ObjectWriterProvider p = new ObjectWriterProvider(creator);
                ObjectWriter ow = p.getObjectWriter(t, ArrayList.class);
                StringBuilder sb = new StringBuilder(creator.getClass().getSimpleName() + " " + ow.getClass().getSimpleName() + " " + seq + ":");
                for (char c : seq.toCharArray()) {
                    sb.append(' ').append(c).append('=').append(w(ow, p, t, c == 'S' ? sort : 0));
                }
                System.out.println(sb);
            }
        }
    }
}
