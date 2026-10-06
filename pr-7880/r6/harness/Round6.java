package verify;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.writer.HookedProvider7880;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static verify.Verify7880.SORTED;
import static verify.Verify7880.expect;
import static verify.Verify7880.record;

/**
 * Round-6 additions: the multi-variant filter links of 2dd085431/2e6afd5d4 (weak references, the link lock, link
 * before publication plus the reconciling re-link) and the tree helper's feature merge.
 */
public class Round6 {
    static void run() throws Exception {
        if (SORTED == null) {
            return;
        }
        filterFanOut();
        weakLinks();
        publicationOrder();
        stress();
        helperFeatureParity();
    }

    // ------------------------------------------------------------------ I1 every filter kind reaches every variant
    public static class Creds {
        public String password = "hunter2";
        public String name = "alice";
    }

    static ObjectWriter credsWriter() {
        return ObjectWriters.objectWriter(Creds.class,
                ObjectWriters.fieldWriter("password", String.class, (Creds c) -> c.password),
                ObjectWriters.fieldWriter("name", String.class, (Creds c) -> c.name));
    }

    static String json(Object o, ObjectWriterProvider p, JSONWriter.Feature... f) {
        return JSON.toJSONString(o, new JSONWriter.Context(p, f));
    }

    static String utf8(Object o, ObjectWriterProvider p, JSONWriter.Feature... f) {
        return new String(JSON.toJSONBytes(o, StandardCharsets.UTF_8, new JSONWriter.Context(p, f)), StandardCharsets.UTF_8);
    }

    static void filterFanOut() {
        System.out.println("[I1] one registered writer, 3 sorted cells, 4 filter kinds, filter set after / before / cleared");
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("ValueFilter", (ValueFilter) (o, n, v) -> "password".equals(n) ? "***" : v);
        filters.put("PropertyFilter", (PropertyFilter) (o, n, v) -> !"password".equals(n));
        filters.put("NameFilter", (NameFilter) (o, n, v) -> "password".equals(n) ? "pwd" : n);
        SimplePropertyPreFilter pre = new SimplePropertyPreFilter();
        pre.getExcludes().add("password");
        filters.put("PropertyPreFilter", pre);
        Map<String, String> expected = new HashMap<>();
        expected.put("ValueFilter", "{\"name\":\"alice\",\"password\":\"***\"}");
        expected.put("PropertyFilter", "{\"name\":\"alice\"}");
        expected.put("NameFilter", "{\"name\":\"alice\",\"pwd\":\"hunter2\"}");
        expected.put("PropertyPreFilter", "{\"name\":\"alice\"}");
        String clear = "{\"name\":\"alice\",\"password\":\"hunter2\"}";

        for (String mode : new String[]{"after registration", "before registration", "set, then cleared"}) {
            for (Map.Entry<String, Filter> e : filters.entrySet()) {
                ObjectWriter w = credsWriter();
                ObjectWriterProvider p1 = new ObjectWriterProvider();
                ObjectWriterProvider p2 = new ObjectWriterProvider();
                ObjectWriterProvider p3 = new ObjectWriterProvider();
                if (mode.startsWith("before")) {
                    w.setFilter(e.getValue());
                }
                p1.register(Creds.class, w);
                p2.register(Creds.class, w);
                p3.register(Creds.class, w, true);
                if (!mode.startsWith("before")) {
                    w.setFilter(e.getValue());
                }
                if (mode.startsWith("set")) {
                    clearFilter(w, e.getKey());
                }
                String want = mode.startsWith("set") ? clear : expected.get(e.getKey());
                List<String> bad = new ArrayList<>();
                String[][] cells = {
                        {"p1", json(new Creds(), p1, SORTED), utf8(new Creds(), p1, SORTED)},
                        {"p2", json(new Creds(), p2, SORTED), utf8(new Creds(), p2, SORTED)},
                        {"p3 FieldBased", json(new Creds(), p3, JSONWriter.Feature.FieldBased, SORTED),
                                utf8(new Creds(), p3, JSONWriter.Feature.FieldBased, SORTED)},
                };
                for (String[] c : cells) {
                    if (!want.equals(c[1]) || !want.equals(c[2])) {
                        bad.add(c[0] + "=" + c[1]);
                    }
                }
                expect("I1", e.getKey() + ", " + mode, bad.isEmpty(), bad.isEmpty() ? "3 cells x json/utf8 = " + want : String.join("; ", bad));
            }
        }
        System.out.println();
    }

    static void clearFilter(ObjectWriter w, String kind) {
        switch (kind) {
            case "ValueFilter": w.setValueFilter(null); break;
            case "PropertyFilter": w.setPropertyFilter(null); break;
            case "NameFilter": w.setNameFilter(null); break;
            default: w.setPropertyPreFilter(null); break;
        }
    }

    // ------------------------------------------------------------------ I2 weak links: released when dead, kept while live
    static Field linkedField() {
        try {
            Field f = ObjectWriterAdapter.class.getDeclaredField("linkedVariants");
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    static int[] liveAndLength(Object source, Field f) throws Exception {
        Object[] arr = (Object[]) f.get(source);
        int live = 0;
        for (Object o : arr) {
            Object r = o instanceof WeakReference ? ((WeakReference<?>) o).get() : o;
            if (r != null) {
                live++;
            }
        }
        return new int[]{live, arr.length};
    }

    static volatile Object sink;

    static void gcPressure() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            List<byte[]> junk = new ArrayList<>();
            for (int j = 0; j < 64; j++) {
                junk.add(new byte[1 << 20]);
            }
            sink = junk;
            sink = null;
            System.gc();
            Thread.sleep(20);
        }
    }

    @JSONType(alphabetic = false)
    public static class Account {
        public String password = "hunter2";
        public String name = "alice";
        public int id = 7;
    }

    static final ValueFilter MASK = (o, n, v) -> "password".equals(n) ? "***" : v;

    static void weakLinks() throws Exception {
        System.out.println("[I2] weak links");
        Field f = linkedField();
        if (f == null) {
            record("INFO", "I2", "ObjectWriterAdapter.linkedVariants", "not present on this jar");
        } else {
            ObjectWriter w = credsWriter();
            ObjectWriterProvider p = new ObjectWriterProvider();
            for (int i = 0; i < 50; i++) {
                p.register(Creds.class, w);
                p.unregister(Creds.class);
            }
            int[] before = liveAndLength(w, f);
            int rounds = 0;
            int[] after = before;
            while (after[0] > 0 && rounds++ < 20) {
                gcPressure();
                after = liveAndLength(w, f);
            }
            expect("I2", "50x register/unregister: dead variants become collectible", after[0] == 0,
                    "before GC live=" + before[0] + " len=" + before[1] + "; after " + rounds + " GC round(s) live=" + after[0]);
            p.register(Creds.class, w);
            int[] compact = liveAndLength(w, f);
            expect("I2", "next link compacts cleared entries", compact[1] == 1, "live=" + compact[0] + " len=" + compact[1]);
        }

        // a live variant must survive GC: the provider holds it, the link only weakly
        ObjectWriter w2 = credsWriter();
        ObjectWriterProvider keep = new ObjectWriterProvider();
        keep.register(Creds.class, w2);
        gcPressure();
        gcPressure();
        w2.setValueFilter(MASK);
        String s = json(new Creds(), keep, SORTED);
        expect("I2", "registered variant still linked after GC pressure", s.equals("{\"name\":\"alice\",\"password\":\"***\"}"), s);

        for (String creator : new String[]{"asm", "reflect"}) {
            ObjectWriterProvider p = Verify7880.newProvider(creator);
            ObjectWriter natural = p.getObjectWriter(Account.class, Account.class, false);
            json(new Account(), p, SORTED);
            gcPressure();
            natural.setFilter(MASK);
            String out = json(new Account(), p, SORTED);
            expect("I2", "created variants (" + creator + "): filter set after GC reaches sorted writer",
                    out.equals("{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}"), out);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------ I3 publication order, replayed deterministically
    static boolean await(CountDownLatch l, long ms) {
        try {
            return l.await(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    static String sortedOut(ObjectWriterProvider p) {
        return json(new Account(), p, SORTED);
    }

    static final String MASKED = "{\"id\":7,\"name\":\"alice\",\"password\":\"***\"}";

    static void publicationOrder() throws Exception {
        System.out.println("[I3] publication order (a test provider pauses one thread inside the cache lookups)");
        ExecutorService pool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            preInstalledFilter(pool);
            concurrentCreation(pool);
        } finally {
            pool.shutdownNow();
        }
        System.out.println();
    }

    /**
     * The 14:27 P1: the natural writer already carries a filter; thread B creates the sorted writer. B is paused at
     * its first lookup of the natural cell. Thread C writes sorted as soon as the sorted writer is published.
     */
    static void preInstalledFilter(ExecutorService pool) throws Exception {
        for (String creator : new String[]{"asm", "reflect"}) {
            HookedProvider7880 p = new HookedProvider7880(
                    "reflect".equals(creator) ? ObjectWriterCreator.INSTANCE : com.alibaba.fastjson2.writer.ObjectWriterCreatorASM.INSTANCE);
            p.getObjectWriter(Account.class, Account.class, false).setFilter(MASK);
            CountDownLatch bPaused = new CountDownLatch(1);
            CountDownLatch cDone = new CountDownLatch(1);
            AtomicInteger naturalLookups = new AtomicInteger();
            Thread[] b = new Thread[1];
            p.hook = (fb, sorted, key) -> {
                if (Thread.currentThread() == b[0] && !sorted && key == Account.class && naturalLookups.incrementAndGet() == 1) {
                    bPaused.countDown();
                    await(cDone, 1000);
                }
            };
            Future<String> bOut = pool.submit(() -> {
                b[0] = Thread.currentThread();
                return sortedOut(p);
            });
            await(bPaused, 5000);
            long deadline = System.currentTimeMillis() + 3000;
            while (!p.published(Account.class, false, true) && System.currentTimeMillis() < deadline) {
                Thread.sleep(1);
            }
            String c = p.published(Account.class, false, true) ? sortedOut(p) : "(sorted writer not published)";
            cDone.countDown();
            String bo = bOut.get(10, TimeUnit.SECONDS);
            p.hook = null;
            expect("I3", "pre-installed filter: a write that hits the just-published sorted writer (" + creator + ")",
                    MASKED.equals(c), "C=" + c + "  B=" + bo);
        }
    }

    /**
     * The residual window: neither writer exists. B (sorted write) is paused after its first look at the natural
     * cell; meanwhile A creates the natural writer and sets a filter on it. B is then paused again at its second
     * look (the re-link after publication, if the jar has one). C writes sorted after A's setFilter has returned
     * and the sorted writer is published.
     */
    static void concurrentCreation(ExecutorService pool) throws Exception {
        for (String creator : new String[]{"asm", "reflect"}) {
            String[] r = replayConcurrentCreation(pool, creator);
            expect("I3", "concurrent creation: sorted write that starts after setFilter returned (" + creator + ")",
                    MASKED.equals(r[0]), "C=" + r[0] + "  B(started before setFilter)=" + r[1] + "  later=" + r[2]
                            + "  natural lookups by B=" + r[3]);
            expect("I3", "concurrent creation: converged afterwards (" + creator + ")", MASKED.equals(r[2]), r[2]);
        }
    }

    /** Returns {C's output, B's output, a later sorted write, B's natural-cell lookups}. */
    public static String[] replayConcurrentCreation(ExecutorService pool, String creator) throws Exception {
        HookedProvider7880 p = new HookedProvider7880(
                "reflect".equals(creator) ? ObjectWriterCreator.INSTANCE : com.alibaba.fastjson2.writer.ObjectWriterCreatorASM.INSTANCE);
        CountDownLatch bFirst = new CountDownLatch(1);
        CountDownLatch aDone = new CountDownLatch(1);
        CountDownLatch cDone = new CountDownLatch(1);
        AtomicInteger naturalLookups = new AtomicInteger();
        Thread[] b = new Thread[1];
        p.hook = (fb, sorted, key) -> {
            if (Thread.currentThread() == b[0] && !sorted && key == Account.class) {
                int n = naturalLookups.incrementAndGet();
                if (n == 1) {
                    bFirst.countDown();
                    await(aDone, 1000);
                } else if (n == 2) {
                    await(cDone, 1000);
                }
            }
        };
        Future<String> bOut = pool.submit(() -> {
            b[0] = Thread.currentThread();
            return sortedOut(p);
        });
        await(bFirst, 5000);
        Future<?> a = pool.submit(() -> {
            p.getObjectWriter(Account.class, Account.class, false).setFilter(MASK);
            aDone.countDown();
            return null;
        });
        a.get(10, TimeUnit.SECONDS);
        long deadline = System.currentTimeMillis() + 3000;
        while (!p.published(Account.class, false, true) && System.currentTimeMillis() < deadline) {
            Thread.sleep(1);
        }
        String c = p.published(Account.class, false, true) ? sortedOut(p) : "(sorted writer not published)";
        cDone.countDown();
        String bo = bOut.get(10, TimeUnit.SECONDS);
        p.hook = null;
        return new String[]{c, bo, sortedOut(p), String.valueOf(naturalLookups.get())};
    }

    // ------------------------------------------------------------------ I3 unpaced stress and lock-order sanity
    static void stress() throws Exception {
        System.out.println("[I3] unpaced stress");
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            int rounds = 300;
            int leaks = 0;
            for (int r = 0; r < rounds; r++) {
                ObjectWriterProvider p = new ObjectWriterProvider();
                p.getObjectWriter(Account.class, Account.class, false).setFilter(MASK);
                CyclicBarrier go = new CyclicBarrier(threads);
                List<Future<String>> fs = new ArrayList<>();
                for (int t = 0; t < threads; t++) {
                    fs.add(pool.submit(() -> {
                        go.await();
                        return sortedOut(p);
                    }));
                }
                for (Future<String> fu : fs) {
                    if (!MASKED.equals(fu.get(30, TimeUnit.SECONDS))) {
                        leaks++;
                    }
                }
            }
            expect("I3", "pre-installed filter, 8 threads x 300 first sorted writes", leaks == 0, leaks + " unmasked of " + rounds * threads);

            // filters set concurrently with links into fresh providers and sorted writes; must finish (no deadlock)
            ObjectWriter w = credsWriter();
            AtomicInteger done = new AtomicInteger();
            List<Future<?>> fs = new ArrayList<>();
            long until = System.currentTimeMillis() + 1500;
            for (int t = 0; t < threads; t++) {
                final int k = t;
                fs.add(pool.submit(() -> {
                    while (System.currentTimeMillis() < until) {
                        if (k % 3 == 0) {
                            w.setValueFilter(MASK);
                            w.setPropertyFilter((PropertyFilter) (o, n, v) -> true);
                        } else if (k % 3 == 1) {
                            ObjectWriterProvider p = new ObjectWriterProvider();
                            p.register(Creds.class, w);
                            json(new Creds(), p, SORTED);
                        } else {
                            w.setNameFilter((NameFilter) (o, n, v) -> n);
                        }
                        done.incrementAndGet();
                    }
                    return null;
                }));
            }
            boolean finished = true;
            for (Future<?> fu : fs) {
                try {
                    fu.get(20, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    finished = false;
                }
            }
            expect("I3", "setters racing links on one writer, 1.5 s, 8 threads", finished, finished ? done.get() + " operations, no deadlock" : "timed out");
        } finally {
            pool.shutdownNow();
        }
        System.out.println();
    }

    // ------------------------------------------------------------------ I4 helper feature merge (setFeatures)
    public enum Color { RED, GREEN }

    public static class Mixed {
        public String nothing;
        public Long big = 9007199254740993L;
        public BigDecimal price = new BigDecimal("1.50");
        public Color color = Color.GREEN;
        public Date when = new Date(0);
        public List<Object> list = new ArrayList<>(Arrays.asList(1L, null, "s", Color.RED, new BigDecimal("2.0")));
        public Map<String, Object> map = new LinkedHashMap<>();
        public Child child = new Child();
        public boolean flag;
        public int[] ints = {1, 2};

        public Mixed() {
            map.put("b", null);
            map.put("a", 3L);
        }
    }

    @JSONType(alphabetic = false)
    public static class Child {
        public int zebra = 3;
        public String apple;
    }

    static void helperFeatureParity() throws Exception {
        System.out.println("[I4] tree helper: toJSON(obj, f.mask) vs JSON.toJSON(obj, f) for every Feature");
        Method helper;
        try {
            helper = ObjectWriterAdapter.class.getDeclaredMethod("toJSON", Object.class, long.class);
            helper.setAccessible(true);
        } catch (NoSuchMethodException e) {
            record("INFO", "I4", "ObjectWriterAdapter.toJSON(Object, long)", "not present");
            System.out.println();
            return;
        }
        List<String> diffs = new ArrayList<>();
        int n = 0;
        for (JSONWriter.Feature f : JSONWriter.Feature.values()) {
            n++;
            String a = render(() -> helper.invoke(null, new Mixed(), f.mask));
            String b = render(() -> JSON.toJSON(new Mixed(), f));
            if (!a.equals(b)) {
                diffs.add(f + ": helper=" + a + " varargs=" + b);
            }
        }
        expect("I4", "all " + n + " features: helper == varargs JSON.toJSON", diffs.isEmpty(),
                diffs.isEmpty() ? "identical" : diffs.size() + " differ: " + diffs.get(0));
        System.out.println();
    }

    interface Call {
        Object call() throws Exception;
    }

    static String render(Call c) {
        try {
            Object o = c.call();
            return (o == null ? "null" : o.getClass().getSimpleName()) + ":" + JSON.toJSONString(o, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.WriteMapNullValue);
        } catch (java.lang.reflect.InvocationTargetException e) {
            return "throws " + e.getCause().getClass().getSimpleName();
        } catch (Throwable e) {
            return "throws " + e.getClass().getSimpleName();
        }
    }
}
