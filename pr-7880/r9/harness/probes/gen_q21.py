#!/usr/bin/env python3
"""Generates Q21.java: field-level deserializeFeatures X on container fields of beans, both reader creators.

For each JSONReader.Feature X and field shape (List<B>, B[], Set<B>, Map<String,B>, B), a holder class carries
@JSONField(deserializeFeatures = X) on field "f"; an unannotated holder of the same shape is parsed with X on the
context as the oracle, and with no feature as the baseline. Each item input exercises one or more features.
"""
import sys
FEATURES = sys.argv[1].split()
SHAPES = [  # (tag, declared type, wrap item into the value of "f")
    ("L", "List<B>", "[%s]"),
    ("A", "B[]", "[%s]"),
    ("S", "Set<B>", "[%s]"),
    ("M", "Map<String, B>", '{"k":%s}'),
    ("O", "B", "%s"),
]
ITEMS = [
    ("plain", '{"id":1,"name":"a"}'),
    ("array", '[1,"a"]'),
    ("case", '{"ID":1,"Name":"a"}'),
    ("snake", '{"id":1,"na_me":"a"}'),
    ("autotype-sub", '{"@type":"p21.Q21$Sub","id":1,"extra":"x"}'),
    ("autotype-other", '{"@type":"java.util.HashMap","id":1}'),
    ("unknown", '{"id":1,"zzz":2}'),
    ("badint", '{"id":"x"}'),
    ("trim", '{"id":1,"name":" a "}'),
    ("nostring", '{"id":1}'),
    ("double", '{"id":1,"o":1.5}'),
    ("bigint", '{"id":1,"o":12345678901234567890}'),
    ("int", '{"id":1,"o":7}'),
    ("enum", '{"id":1,"e":"NOPE"}'),
    ("dup", '{"id":1,"id":2}'),
    ("nullprim", '{"id":null}'),
    ("nested", '{"id":1,"o":{"k":[1]}}'),
    ("base64", '{"id":1,"bytes":"AQI="}'),
    ("emptystr", '{"id":1,"n":""}'),
    ("ref", '{"id":1,"o":{"$ref":"$"}}'),
    ("nullname", '{"id":1,"name":null}'),
    ("numname", '{"id":1,"name":5}'),
    ("bool", '{"id":1,"o":true,"flag":2}'),
    ("overflow", '{"id":99999999999}'),
]
out = []
w = out.append
w("package p21;")
w("")
w("import com.alibaba.fastjson2.JSONReader;")
w("import com.alibaba.fastjson2.annotation.JSONField;")
w("import com.alibaba.fastjson2.reader.ObjectReaderCreator;")
w("import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;")
w("import com.alibaba.fastjson2.reader.ObjectReaderProvider;")
w("")
w("import java.lang.reflect.Array;")
w("import java.util.*;")
w("")
w("public class Q21 {")
w("    public enum E { A, B }")
w("    public static class B { public int id; public String name; public Object o; public E e; public Integer n; public byte[] bytes; public boolean flag; }")
w("    public static class Sub extends B { public String extra; }")
for tag, decl, _ in SHAPES:
    w("    public static class P%s { public %s f; }" % (tag, decl))
for i, x in enumerate(FEATURES):
    for tag, decl, _ in SHAPES:
        w("    public static class %s%d { @JSONField(deserializeFeatures = JSONReader.Feature.%s) public %s f; }" % (tag, i, x, decl))
w("    static final String[] FEATURES = {%s};" % ", ".join('"%s"' % x for x in FEATURES))
w("    static final String[][] SHAPES = {%s};" % ", ".join('{"%s", "%s"}' % (t, p.replace('"', '\\"')) for t, _, p in SHAPES))
w("    static final String[][] ITEMS = {%s};" % ", ".join('{"%s", "%s"}' % (n, v.replace('\\', '\\\\').replace('"', '\\"')) for n, v in ITEMS))
w(r'''
    static String desc(Object v) {
        if (v == null) return "null";
        if (v instanceof B) {
            B b = (B) v;
            String s = v.getClass().getSimpleName() + "(id=" + b.id + ",name=" + q(b.name) + ",o=" + desc(b.o) + ",e=" + b.e + ",n=" + b.n
                    + ",bytes=" + (b.bytes == null ? "null" : Arrays.toString(b.bytes)) + ",flag=" + b.flag;
            if (v instanceof Sub) s += ",extra=" + ((Sub) v).extra;
            return s + ")";
        }
        if (v instanceof Map) {
            StringBuilder sb = new StringBuilder(v.getClass().getSimpleName()).append('{');
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) sb.append(desc(e.getKey())).append('=').append(e.getValue() == v ? "<self>" : desc(e.getValue())).append(',');
            return sb.append('}').toString();
        }
        if (v instanceof Collection) {
            StringBuilder sb = new StringBuilder(v.getClass().getSimpleName()).append('[');
            for (Object o : (Collection<?>) v) sb.append(desc(o)).append(',');
            return sb.append(']').toString();
        }
        if (v.getClass().isArray()) {
            StringBuilder sb = new StringBuilder(v.getClass().getComponentType().getSimpleName()).append("[]{");
            for (int i = 0; i < Array.getLength(v); i++) sb.append(desc(Array.get(v, i))).append(',');
            return sb.append('}').toString();
        }
        if (v instanceof String) return q((String) v);
        return v.getClass().getSimpleName() + ":" + v;
    }

    static String q(String s) {
        return s == null ? "null" : "'" + s + "'";
    }

    static String parse(ObjectReaderProvider p, Class<?> c, String json, JSONReader.Feature... f) {
        try (JSONReader r = JSONReader.of(json, new JSONReader.Context(p, f))) {
            Object o = r.read(c);
            return desc(o == null ? null : c.getField("f").get(o));
        } catch (Throwable e) {
            String m = String.valueOf(e.getMessage());
            int k = m.indexOf(", offset");
            if (k > 0) m = m.substring(0, k);
            if (m.length() > 70) m = m.substring(0, 70);
            return "EXC " + e.getClass().getSimpleName() + ": " + m;
        }
    }

    public static void main(String[] args) throws Exception {
        String only = args.length > 0 ? args[0] : null;
        for (String cn : new String[]{"asm", "reflect"}) {
            for (int i = 0; i < FEATURES.length; i++) {
                if (only != null && !only.equals(FEATURES[i])) continue;
                JSONReader.Feature x;
                try {
                    x = JSONReader.Feature.valueOf(FEATURES[i]);
                } catch (IllegalArgumentException e) {
                    x = null;
                }
                for (String[] shape : SHAPES) {
                    ObjectReaderProvider p = new ObjectReaderProvider("asm".equals(cn) ? ObjectReaderCreatorASM.INSTANCE : ObjectReaderCreator.INSTANCE);
                    Class<?> annotated = Class.forName("p21.Q21$" + shape[0] + i);
                    Class<?> plain = Class.forName("p21.Q21$P" + shape[0]);
                    for (String[] item : ITEMS) {
                        String json = "{\"f\":" + String.format(shape[1], item[1]) + "}";
                        String field = x == null ? "n/a" : parse(p, annotated, json);
                        String ctx = x == null ? "n/a" : parse(p, plain, json, x);
                        String base = parse(p, plain, json);
                        System.out.println(cn + "\t" + FEATURES[i] + "\t" + shape[0] + "\t" + item[0] + "\tfield=" + field + "\tcontext=" + ctx + "\tbase=" + base);
                    }
                }
            }
        }
    }
}''')
open(sys.argv[2], "w").write("\n".join(out) + "\n")
