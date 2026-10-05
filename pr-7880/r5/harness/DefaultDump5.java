package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Round-5 additions to the default-path dump: list and array item writers (the code b874205b6 and the round-5 patch
 * touch), with the new features off. Uses only API that exists on main, so the same class runs on every jar.
 */
public class DefaultDump5 {
    @JSONType(alphabetic = false)
    public static class Child {
        public int zebra = 3;
        public int apple = 1;
    }

    public static class SubChild extends Child {
        public int extra = 5;
    }

    @JSONType(alphabetic = false)
    public static class Transfer {
        public long to = 1001;
        public long from = 2002;
        public long amount = 300;
    }

    @JSONType(alphabetic = false)
    public static class Lists {
        public int zulu = 9;
        public List<Child> plain = new ArrayList<>(Arrays.asList(new Child(), new Child()));
        public List<Child> poly = new ArrayList<>(Arrays.asList(new Child(), new SubChild(), new Child()));
        public List<Object> mixed = new ArrayList<>(Arrays.asList(new Child(), "s", 1, new SubChild(), null));
        public List<Child> shared;
        public Child[] arr = {new Child(), new SubChild()};
        public Object[] objArr = {new Child(), "s", 2L};
        @JSONField(format = "yyyy-MM-dd")
        public List<Date> dates = Collections.singletonList(new Date(0));
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public Transfer[] positional = {new Transfer()};
        @JSONField(serializeFeatures = JSONWriter.Feature.BeanToArray)
        public List<Transfer> positionalList = Collections.singletonList(new Transfer());

        public Lists() {
            Child c = new Child();
            shared = new ArrayList<>(Arrays.asList(c, c));
        }
    }

    static void line(String k, Object v) {
        System.out.println(k + " = " + v);
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    static void dump(String label, ObjectWriterProvider p) {
        Lists o = new Lists();
        JSONWriter.Feature[][] fs = {{}, {JSONWriter.Feature.ReferenceDetection}, {JSONWriter.Feature.WriteClassName},
                {JSONWriter.Feature.BeanToArray}, {JSONWriter.Feature.NotWriteDefaultValue}};
        for (JSONWriter.Feature[] f : fs) {
            String n = label + " " + Arrays.toString(f);
            JSONWriter.Context ctx = new JSONWriter.Context(p, f);
            line(n + " json", JSON.toJSONString(o, ctx));
            line(n + " utf8", new String(JSON.toJSONBytes(o, StandardCharsets.UTF_8, new JSONWriter.Context(p, f)), StandardCharsets.UTF_8));
            byte[] jb = JSONB.toBytes(o, new JSONWriter.Context(p, f));
            line(n + " jsonb", hex(jb));
        }
    }

    public static void main(String[] args) {
        dump("default provider", JSONFactory.getDefaultObjectWriterProvider());
        dump("reflect provider", new ObjectWriterProvider(ObjectWriterCreator.INSTANCE));
        ObjectWriterProvider registered = new ObjectWriterProvider();
        registered.register(Lists.class, ObjectWriterCreator.INSTANCE.createObjectWriter(Lists.class));
        dump("registered reflect writer", registered);
        line("tree from(lists)", JSONObject.from(new Lists()));
        line("tree toJSON(list)", JSON.toJSON(new Lists().poly));
    }
}
