package p25c;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderCreatorASM;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.util.List;
import java.util.Map;

/** Field-level ErrorOnDuplicateKeys on a List<Map> constructor parameter (FieldReaderList.readFieldValue). */
public class Q25c {
    public static class Rows {
        final List<Map<String, Object>> rows;

        @JSONCreator(parameterNames = "rows")
        public Rows(@JSONField(name = "rows", deserializeFeatures = JSONReader.Feature.ErrorOnDuplicateKeys) List<Map<String, Object>> rows) {
            this.rows = rows;
        }
    }

    public static void main(String[] args) {
        for (String cn : new String[]{"asm", "reflect"}) {
            ObjectReaderProvider p = new ObjectReaderProvider("asm".equals(cn) ? ObjectReaderCreatorASM.INSTANCE : ObjectReaderCreator.INSTANCE);
            String r;
            try (JSONReader reader = JSONReader.of("{\"rows\":[{\"b\":1,\"b\":2}]}", new JSONReader.Context(p))) {
                Rows rows = reader.read(Rows.class);
                r = "ok " + rows.rows;
            } catch (Throwable e) {
                boolean dup = false;
                for (Throwable t = e; t != null; t = t.getCause()) dup |= String.valueOf(t.getMessage()).contains("duplicate key");
                r = dup ? "DUP" : "EXC " + e;
            }
            System.out.println(cn + " constructor List<Map> param: " + r);
        }
    }
}
