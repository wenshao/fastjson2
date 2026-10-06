# Generates probe7/src/g/Beans.java: 400 bean types with 6 int and 6 String fields and a List field each.
N = 400
lines = ["package g;", "public class Beans {"]
for i in range(N):
    fields = " ".join(f"public int f{j} = {j}; public String s{j} = \"v{j}\";" for j in range(6))
    lines.append(f"  public static class B{i} {{ {fields} public java.util.List<Integer> l = java.util.Arrays.asList(1,2); }}")
lines.append("  public static final Class<?>[] ALL = {" + ",".join(f"B{i}.class" for i in range(N)) + "};")
lines.append("}")
open('probe7/src/g/Beans.java', 'w').write("\n".join(lines))
