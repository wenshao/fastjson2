package p;

import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterCreatorASM;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Size6 {
    public static class Bean { public int id; public String name; }

    static long deep(Object o) throws Exception {
        long n = SizeAgent.inst.getObjectSize(o);
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.getName().equals("linkedVariantsLock")) {
                    f.setAccessible(true);
                    n += SizeAgent.inst.getObjectSize(f.get(o));
                }
            }
        }
        return n;
    }

    public static void main(String[] a) throws Exception {
        ObjectWriter reflect = ObjectWriterCreator.INSTANCE.createObjectWriter(Bean.class);
        ObjectWriter asm = ObjectWriterCreatorASM.INSTANCE.createObjectWriter(Bean.class);
        System.out.println("reflect " + reflect.getClass().getSimpleName() + " shallow=" + SizeAgent.inst.getObjectSize(reflect) + " +lock=" + deep(reflect)
                + " | asm shallow=" + SizeAgent.inst.getObjectSize(asm) + " +lock=" + deep(asm));
    }
}
