package com.alibaba.fastjson2.writer;

import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Test-only provider that lets the harness pause a chosen thread inside the provider's cache lookups, so a race
 * between writer creation, publication and linking can be replayed in a fixed order instead of hoping for it.
 * It overrides the package-private {@code cacheOf} (PR #7880); the maps themselves are the provider's own, only
 * wrapped. On a jar without {@code cacheOf} the override is never called and the provider behaves normally.
 */
public class HookedProvider7880 extends ObjectWriterProvider {
    public interface Hook {
        void onGet(boolean fieldBased, boolean sorted, Object key);
    }

    public volatile Hook hook;

    public HookedProvider7880(ObjectWriterCreator creator) {
        super(creator);
    }

    ConcurrentMap<Type, ObjectWriter> cacheOf(boolean fieldBased, boolean fieldNamesSorted) {
        ConcurrentMap<Type, ObjectWriter> map = super.cacheOf(fieldBased, fieldNamesSorted);
        Hook h = hook;
        return h == null ? map : new View(map, fieldBased, fieldNamesSorted, h);
    }

    /** Whether the given cell currently holds a writer, read without triggering the hook. */
    public boolean published(Type type, boolean fieldBased, boolean sorted) {
        return super.cacheOf(fieldBased, sorted).containsKey(type);
    }

    static final class View extends AbstractMap<Type, ObjectWriter> implements ConcurrentMap<Type, ObjectWriter> {
        final ConcurrentMap<Type, ObjectWriter> map;
        final boolean fieldBased;
        final boolean sorted;
        final Hook hook;

        View(ConcurrentMap<Type, ObjectWriter> map, boolean fieldBased, boolean sorted, Hook hook) {
            this.map = map;
            this.fieldBased = fieldBased;
            this.sorted = sorted;
            this.hook = hook;
        }

        @Override
        public ObjectWriter get(Object key) {
            ObjectWriter value = map.get(key);
            hook.onGet(fieldBased, sorted, key);
            return value;
        }

        @Override
        public ObjectWriter put(Type key, ObjectWriter value) {
            return map.put(key, value);
        }

        @Override
        public ObjectWriter remove(Object key) {
            return map.remove(key);
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public Set<Map.Entry<Type, ObjectWriter>> entrySet() {
            return map.entrySet();
        }

        @Override
        public ObjectWriter putIfAbsent(Type key, ObjectWriter value) {
            return map.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return map.remove(key, value);
        }

        @Override
        public boolean replace(Type key, ObjectWriter oldValue, ObjectWriter newValue) {
            return map.replace(key, oldValue, newValue);
        }

        @Override
        public ObjectWriter replace(Type key, ObjectWriter value) {
            return map.replace(key, value);
        }
    }
}
