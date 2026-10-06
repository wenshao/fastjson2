package p24b;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;

/** N-11: a module that returns a plain bean writer, under a sorted context. */
public class Q24b {
    @JSONType(alphabetic = false)
    public static class Z {
        public int zeta = 1;
        public int alpha = 2;
    }

    public static void main(String[] args) {
        for (ObjectWriterCreator creator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
            for (ObjectWriterCreator moduleCreator : new ObjectWriterCreator[]{ObjectWriterCreatorASM.INSTANCE, ObjectWriterCreator.INSTANCE}) {
                ObjectWriterProvider p = new ObjectWriterProvider(creator);
                ObjectWriter w = moduleCreator.createObjectWriter(Z.class);
                p.register(new ObjectWriterModule() {
                    @Override
                    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
                        return objectClass == Z.class ? w : null;
                    }
                });
                String n = JSON.toJSONString(new Z(), new JSONWriter.Context(p));
                String s;
                try {
                    s = JSON.toJSONString(new Z(), new JSONWriter.Context(p, JSONWriter.Feature.valueOf("SortFieldNamesAlphabetically")));
                } catch (Throwable e) {
                    s = "n/a";
                }
                System.out.println((creator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm") + " provider, module writer from "
                        + (moduleCreator == ObjectWriterCreator.INSTANCE ? "reflect" : "asm") + " creator: natural=" + n + " sorted=" + s);
            }
        }
    }
}
