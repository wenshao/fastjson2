#!/usr/bin/env python3
"""Generates Q20.java: does a field-level ErrorOnDuplicateKeys reach maps nested in the field's value, like the context-level one?"""
import sys
M = "Map<String, Object>"
SHAPES = [  # (declared type, value with a duplicate key nested, same value without the duplicate)
    ("List<%s>" % M, '[{"b":1,"b":2}]'),
    ("%s[]" % M, '[{"b":1,"b":2}]'),
    ("Set<%s>" % M, '[{"b":1,"b":2}]'),
    ("Collection<%s>" % M, '[{"b":1,"b":2}]'),
    ("ArrayList<HashMap<String, Object>>", '[{"b":1,"b":2}]'),
    ("Optional<%s>" % M, '{"b":1,"b":2}'),
    ("AtomicReference<%s>" % M, '{"b":1,"b":2}'),
    ("Map<String, %s>" % M, '{"k":{"b":1,"b":2}}'),
    ("Map<String, List<%s>>" % M, '{"k":[{"b":1,"b":2}]}'),
    ("List<List<%s>>" % M, '[[{"b":1,"b":2}]]'),
    ("List<%s> (depth 2)" % M, '[{"x":{"b":1,"b":2}}]'),
    (M, '{"b":1,"b":2}'),
    (M + " (depth 2)", '{"x":{"b":1,"b":2}}'),
    (M + " (in list)", '{"x":[{"b":1,"b":2}]}'),
    ("Object", '{"b":1,"b":2}'),
    ("Object (in list)", '[{"b":1,"b":2}]'),
    ("List<Object>", '[{"b":1,"b":2}]'),
    ("JSONObject", '{"b":1,"b":2}'),
    ("JSONObject (depth 2)", '{"x":{"b":1,"b":2}}'),
    ("JSONArray", '[{"b":1,"b":2}]'),
    ("List<JSONObject>", '[{"b":1,"b":2}]'),
    ("Map<String, Integer>", '{"b":1,"b":2}'),
    ("Map<String, String>", '{"b":"1","b":"2"}'),
    ("Map<Integer, Object>", '{"1":1,"1":2}'),
    ("G", '{"m":{"b":1,"b":2}}'),
    ("List<G>", '[{"m":{"b":1,"b":2}}]'),
    ("Map<String, G>", '{"k":{"m":{"b":1,"b":2}}}'),
    ("Map<%s, String>" % M, '{{"a":1,"a":2}:"v"}'),
    ("Map<List<Object>, String>", '{[{"a":1,"a":2}]:"v"}'),
    ("Map<Object, String>", '{{"a":1,"a":2}:"v"}'),
]
def decl(t):
    return t.split(" (")[0]
out = []; w = out.append
w("package p20;")
w("")
w("import com.alibaba.fastjson2.JSONArray;")
w("import com.alibaba.fastjson2.JSONObject;")
w("import com.alibaba.fastjson2.JSONReader;")
w("import com.alibaba.fastjson2.annotation.JSONField;")
w("import com.alibaba.fastjson2.reader.ObjectReaderCreator;")
w("import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;")
w("import com.alibaba.fastjson2.reader.ObjectReaderProvider;")
w("")
w("import java.nio.charset.StandardCharsets;")
w("import java.util.*;")
w("import java.util.concurrent.atomic.AtomicReference;")
w("")
w("public class Q20 {")
w("    public static class G { public %s m; }" % M)
for i, (t, v) in enumerate(SHAPES):
    w("    public static class F%d { @JSONField(deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) public %s f; }" % (i, decl(t)))
    w("    public static class P%d { public %s f; }" % (i, decl(t)))
w("    static final String[][] SHAPES = {%s};" % ",\n            ".join('{"%s", "%s"}' % (t, v.replace('"', '\\"')) for t, v in SHAPES))
w(r'''
    static String parse(ObjectReaderProvider p, Class<?> c, String json, boolean utf8, JSONReader.Feature... f) {
        JSONReader.Context ctx = new JSONReader.Context(p, f);
        try (JSONReader r = utf8 ? JSONReader.of(json.getBytes(StandardCharsets.UTF_8), ctx) : JSONReader.of(json, ctx)) {
            Object o = r.read(c);
            return "ok " + String.valueOf(c.getField("f").get(o)).replaceAll("@[0-9a-f]{5,8}", "@H");
        } catch (Throwable e) {
            boolean dup = false;
            for (Throwable t = e; t != null; t = t.getCause()) {
                dup |= String.valueOf(t.getMessage()).contains("duplicate key");
            }
            return (dup ? "DUP " : "EXC " + e.getMessage() + " ") + e.getClass().getSimpleName();
        }
    }

    public static void main(String[] args) throws Exception {
        for (String cn : new String[]{"asm", "reflect"}) {
            for (String ch : new String[]{"string", "utf8"}) {
                for (int i = 0; i < SHAPES.length; i++) {
                    ObjectReaderProvider p = new ObjectReaderProvider("asm".equals(cn) ? ObjectReaderCreatorASM.INSTANCE : ObjectReaderCreator.INSTANCE);
                    Class<?> f = Class.forName("p20.Q20$F" + i), pl = Class.forName("p20.Q20$P" + i);
                    String dup = "{\"f\":" + SHAPES[i][1] + "}";
                    String nodup = dup.replace("\"b\":2", "\"c\":2").replace("\"a\":2", "\"z\":2").replace("\"1\":2", "\"2\":2").replace("\"b\":\"2\"", "\"c\":\"2\"");
                    boolean u = "utf8".equals(ch);
                    System.out.println(cn + "\t" + ch + "\t" + SHAPES[i][0]
                            + "\tfield=" + parse(p, f, dup, u)
                            + "\tcontext=" + parse(p, pl, dup, u, JSONReader.Feature.ErrorOnDuplicateKeys)
                            + "\tnone=" + parse(p, pl, dup, u)
                            + "\tfield-nodup=" + parse(p, f, nodup, u)
                            + "\tcontext-nodup=" + parse(p, pl, nodup, u, JSONReader.Feature.ErrorOnDuplicateKeys));
                }
            }
        }
    }
}''')
open(sys.argv[1], "w").write("\n".join(out) + "\n")
