package name.mikanoshi.customiuizer.utils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class PrefMap<K, V> extends HashMap<K, V> {

	public Object getObject(String key, Object defValue) {
		return get(key) == null ? defValue : get(key);
	}

	public int getInt(String key, int defValue) {
		key = "pref_key_" + key;
		return get(key) == null ? defValue : (Integer)get(key);
	}

	public String getString(String key, String defValue) {
		key = "pref_key_" + key;
		return get(key) == null ? defValue : (String)get(key);
	}

	public int getStringAsInt(String key, int defValue) {
		key = "pref_key_" + key;
		return get(key) == null ? defValue : Integer.parseInt((String)get(key));
	}

	@SuppressWarnings("unchecked")
	public Set<String> getStringSet(String key) {
		key = "pref_key_" + key;
		return get(key) == null ? new LinkedHashSet<String>() : (Set<String>)get(key);
	}

	public boolean getBoolean(String key) {
		key = "pref_key_" + key;
		return get(key) == null ? false : (Boolean)get(key);
	}

}
