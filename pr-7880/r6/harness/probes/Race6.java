package p;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.writer.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/** Unpaced: natural writer created + filtered by A while sorted writers are created by B threads. */
public class Race6 {
    @JSONType(alphabetic = false)
    public static class Account {
        public String password = "hunter2";
        public String name = "alice";
        public int id = 7;
    }

    public static void main(String[] args) throws Exception {
        int rounds = Integer.parseInt(args[0]);
        int writers = Integer.parseInt(args[1]);
        boolean reflect = args.length > 2 && args[2].equals("reflect");
        JSONWriter.Feature sorted = JSONWriter.Feature.valueOf("SortFieldNamesAlphabetically");
        ValueFilter mask = (o, n, v) -> "password".equals(n) ? "***" : v;
        ExecutorService pool = Executors.newFixedThreadPool(writers + 1);
        long violations = 0, after = 0, unconverged = 0;
        for (int r = 0; r < rounds; r++) {
            ObjectWriterProvider p = reflect ? new ObjectWriterProvider(ObjectWriterCreator.INSTANCE) : new ObjectWriterProvider();
            AtomicBoolean set = new AtomicBoolean();
            CyclicBarrier go = new CyclicBarrier(writers + 1);
            List<Future<long[]>> fs = new ArrayList<>();
            fs.add(pool.submit(() -> {
                go.await();
                p.getObjectWriter(Account.class, Account.class, false).setFilter(mask);
                set.set(true);
                return new long[2];
            }));
            for (int w = 0; w < writers; w++) {
                fs.add(pool.submit(() -> {
                    go.await();
                    long v = 0, a = 0;
                    int extra = 0;
                    while (extra < 20) {
                        boolean afterSet = set.get();
                        String s = JSON.toJSONString(new Account(), new JSONWriter.Context(p, sorted));
                        if (afterSet) {
                            a++;
                            extra++;
                            if (s.contains("hunter2")) {
                                v++;
                            }
                        }
                    }
                    return new long[]{v, a};
                }));
            }
            for (Future<long[]> f : fs) {
                long[] x = f.get(30, TimeUnit.SECONDS);
                violations += x[0];
                after += x[1];
            }
            if (JSON.toJSONString(new Account(), new JSONWriter.Context(p, sorted)).contains("hunter2")) {
                unconverged++;
            }
        }
        pool.shutdownNow();
        System.out.println((reflect ? "reflect" : "asm") + " rounds=" + rounds + " writers=" + writers
                + " writes-after-setFilter=" + after + " unmasked=" + violations + " unconverged-rounds=" + unconverged);
    }
}
