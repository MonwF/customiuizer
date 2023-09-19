package name.monwf.customiuizer.utils;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class PrefMap<K, V> extends HashMap<K, V> {

	public Object getObject(String key, Object defValue) {
		return get(key) == null ? defValue : get(key);
	}

	public int getInt(String key, int defValue) {
		if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
		return get(key) == null ? defValue : (Integer)get(key);
	}

	public long getLong(String key, long defValue) {
		if (!key.startsWith("pref_key_")) {
			key = "pref_key_" + key;
		}
		return get(key) == null ? defValue : (Long)get(key);
	}

	public String getString(String key, String defValue) {
		if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
		return get(key) == null ? defValue : (String)get(key);
	}

	public int getStringAsInt(String key, int defValue) {
		if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
		return get(key) == null ? defValue : Integer.parseInt((String)get(key));
	}

	public Set<String> getStringSet(String key) {
		if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
		return get(key) == null ? new HashSet<>() : (Set<String>)get(key);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
	public boolean getBoolean(String key, boolean defValue) {
		if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
		return get(key) == null ? defValue : (Boolean)get(key);
	}
}
