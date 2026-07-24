package tv.withaibuild.customiuizer.utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-local preference snapshot used by hook callbacks.
 *
 * <p>Remote preferences use {@code pref_key_} names. They are normalized once when inserted so
 * hot hook paths can read their short, source-level keys without allocating a prefixed String on
 * every invocation. ConcurrentHashMap also provides visibility between the preference listener
 * and callbacks running on binder, UI, and system-server threads.</p>
 */
public final class PrefMap extends ConcurrentHashMap<String, Object> {
    private static final String STORAGE_PREFIX = "pref_key_";

    private final ConcurrentHashMap<String, CachedInt> parsedIntCache = new ConcurrentHashMap<>();

    private static final class CachedInt {
        final String raw;
        final int value;

        CachedInt(String raw, int value) {
            this.raw = raw;
            this.value = value;
        }
    }

    private static String normalizeStorageKey(String key) {
        return key.startsWith(STORAGE_PREFIX) ? key.substring(STORAGE_PREFIX.length()) : key;
    }

    private Object getValue(String key) {
        return get(key.startsWith(STORAGE_PREFIX) ? normalizeStorageKey(key) : key);
    }

    @Override
    public Object put(String key, Object value) {
        String normalized = normalizeStorageKey(key);
        parsedIntCache.remove(normalized);
        return super.put(normalized, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> values) {
        values.forEach(this::put);
    }

    @Override
    public Object remove(Object key) {
        if (key instanceof String) {
            String normalized = normalizeStorageKey((String) key);
            parsedIntCache.remove(normalized);
            return super.remove(normalized);
        }
        return null;
    }

    public int getInt(String key, int defaultValue) {
        Object value = getValue(key);
        return value == null ? defaultValue : (Integer) value;
    }

    public long getLong(String key, long defaultValue) {
        Object value = getValue(key);
        return value == null ? defaultValue : (Long) value;
    }

    public String getString(String key, String defaultValue) {
        Object value = getValue(key);
        return value == null ? defaultValue : (String) value;
    }

    public int getStringAsInt(String key, int defaultValue) {
        Object value = getValue(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();

        String raw = (String) value;
        String normalized = normalizeStorageKey(key);
        CachedInt cached = parsedIntCache.get(normalized);
        if (cached != null && cached.raw.equals(raw)) return cached.value;

        int parsed = Integer.parseInt(raw);
        parsedIntCache.put(normalized, new CachedInt(raw, parsed));
        return parsed;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key) {
        Object value = getValue(key);
        return value == null ? Collections.emptySet() : (Set<String>) value;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = getValue(key);
        return value == null ? defaultValue : (Boolean) value;
    }
}
