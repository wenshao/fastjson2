package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import verify.DefaultDump7;

import java.util.*;

public class Q7c {
    static String seg(String s) {
        int i = s.indexOf("mapBeanToArray");
        if (i < 0) {
            i = s.lastIndexOf("{\"k\":[");
            return i < 0 ? s : s.substring(i, Math.min(s.length(), i + 70));
        }
        return s.substring(i, Math.min(s.length(), i + 90));
    }

    public static void main(String[] args) {
        System.out.println("jar " + JSON.class.getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll(".*/", ""));
        JSONWriter.Feature[] all = {JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.WriteNonStringKeyAsString,
                JSONWriter.Feature.MapSortField, JSONWriter.Feature.SortMapEntriesByKeys, JSONWriter.Feature.BrowserCompatible,
                JSONWriter.Feature.WriteLongAsString, JSONWriter.Feature.ReferenceDetection, JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.FieldBased};
        ObjectWriterProvider fresh = new ObjectWriterProvider();
        System.out.println("fresh, BeanToArray only: " + seg(JSON.toJSONString(new DefaultDump7.Annotated(), new JSONWriter.Context(fresh, JSONWriter.Feature.BeanToArray))));
        for (JSONWriter.Feature f : all) {
            ObjectWriterProvider p = new ObjectWriterProvider();
            try {
                JSON.toJSONString(new DefaultDump7.Annotated(), new JSONWriter.Context(p, f));
            } catch (RuntimeException e) {
                System.out.println("  (" + f + " write threw " + e.getClass().getSimpleName() + ")");
            }
            System.out.println("after " + f + ": " + seg(JSON.toJSONString(new DefaultDump7.Annotated(), new JSONWriter.Context(p, JSONWriter.Feature.BeanToArray))));
        }
    }
}
