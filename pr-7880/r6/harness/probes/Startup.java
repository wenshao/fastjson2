package g;

import com.alibaba.fastjson2.JSON;

import java.util.concurrent.CountDownLatch;

/** First serialization of 400 distinct bean types, split across T threads; prints wall time in ms. */
public class Startup {
    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        Object[] beans = new Object[Beans.ALL.length];
        for (int i = 0; i < beans.length; i++) {
            beans[i] = Beans.ALL[i].getConstructor().newInstance();
        }
        JSON.toJSONString(new java.util.HashMap<>());   // load the library itself first
        CountDownLatch start = new CountDownLatch(1);
        Thread[] ts = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            final int k = t;
            ts[t] = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    return;
                }
                for (int i = k; i < beans.length; i += threads) {
                    JSON.toJSONString(beans[i]);
                }
            });
            ts[t].start();
        }
        long t0 = System.nanoTime();
        start.countDown();
        for (Thread t : ts) {
            t.join();
        }
        System.out.println((System.nanoTime() - t0) / 1_000_000);
    }
}
