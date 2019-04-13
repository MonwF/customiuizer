package name.mikanoshi.customiuizer.utils;

import java.util.HashMap;

@SuppressWarnings("ConstantConditions")
public class PrefMap<K, V> extends HashMap<K, V> {

	public int getInt(String key, int defValue) {
		key = "pref_key_" + key;
		return get(key) == null ? defValue : (Integer)get(key);
	}

	public String getString(String key, String defValue) {
		key = "pref_key_" + key;
		return get(key) == null ? defValue : (String)get(key);
	}

	public boolean getBoolean(String key) {
		key = "pref_key_" + key;
		return get(key) == null ? false : (Boolean)get(key);
	}

}
