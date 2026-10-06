package p23;

import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/** N-1: JSONObject.canConvertToLong for Number types next to getLongValue. */
public class Q23 {
    static DoubleAdder da(double v) {
        DoubleAdder a = new DoubleAdder();
        a.add(v);
        return a;
    }

    /** An unsigned 64-bit value, like Guava's UnsignedLong: longValue() wraps above Long.MAX_VALUE. */
    static final class U64 extends Number {
        final long bits;
        U64(long bits) { this.bits = bits; }
        public int intValue() { return (int) bits; }
        public long longValue() { return bits; }
        public float floatValue() { return (float) doubleValue(); }
        public double doubleValue() { return new BigInteger(Long.toUnsignedString(bits)).doubleValue(); }
        public String toString() { return Long.toUnsignedString(bits); }
    }

    public static void main(String[] args) {
        Map<String, Object> vals = new LinkedHashMap<>();
        vals.put("Double 1.5", 1.5d);
        vals.put("Float 1.5", 1.5f);
        vals.put("BigDecimal 1.5", new BigDecimal("1.5"));
        vals.put("DoubleAdder 1.5", da(1.5));
        vals.put("DoubleAdder -1.5", da(-1.5));
        vals.put("DoubleAdder 3.0", da(3.0));
        vals.put("Double 1e30", 1e30d);
        vals.put("DoubleAdder 1e30", da(1e30));
        vals.put("DoubleAdder NaN", da(Double.NaN));
        vals.put("DoubleAdder 2^63", da(9.223372036854775807E18));
        vals.put("Double 2^63", 9.223372036854775807E18);
        vals.put("AtomicLong MAX", new AtomicLong(Long.MAX_VALUE));
        vals.put("AtomicLong MIN", new AtomicLong(Long.MIN_VALUE));
        vals.put("AtomicLong 2^53+1", new AtomicLong((1L << 53) + 1));
        vals.put("LongAdder 5", new LongAdder() {{ add(5); }});
        vals.put("AtomicInteger 7", new AtomicInteger(7));
        vals.put("U64 2^64-1", new U64(-1L));
        vals.put("U64 2^63+5", new U64(Long.MIN_VALUE + 5));
        vals.put("U64 42", new U64(42));
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> e : vals.entrySet()) {
            o.put("v", e.getValue());
            String can;
            try {
                can = String.valueOf(o.canConvertToLong("v"));
            } catch (Throwable t) {
                can = "EXC " + t.getClass().getSimpleName();
            }
            String get;
            try {
                get = String.valueOf(o.getLongValue("v"));
            } catch (Throwable t) {
                get = "EXC " + t.getClass().getSimpleName();
            }
            System.out.println(String.format("%-20s canConvertToLong=%-5s getLongValue=%s", e.getKey(), can, get));
        }
    }
}
