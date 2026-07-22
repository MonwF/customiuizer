package name.monwf.customiuizer.utils;

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

    private static String normalizeStorageKey(String key) {
        return key.startsWith(STORAGE_PREFIX) ? key.substring(STORAGE_PREFIX.length()) : key;
    }

    private Object getValue(String key) {
        return get(key.startsWith(STORAGE_PREFIX) ? normalizeStorageKey(key) : key);
    }

    @Override
    public Object put(String key, Object value) {
        return super.put(normalizeStorageKey(key), value);
    }

    @Override
    public void putAll(Map<? extends String, ?> values) {
        values.forEach(this::put);
    }

    @Override
    public Object remove(Object key) {
        return key instanceof String
            ? super.remove(normalizeStorageKey((String) key))
            : null;
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
        return value == null ? defaultValue : Integer.parseInt((String) value);
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
