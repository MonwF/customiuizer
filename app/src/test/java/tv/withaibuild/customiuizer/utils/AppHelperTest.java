package tv.withaibuild.customiuizer.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.SharedPreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AppHelperTest {

    private FakeSharedPreferences fakePrefs;

    @Before
    public void setUp() {
        fakePrefs = new FakeSharedPreferences();
        AppHelper.appPrefs = fakePrefs;
    }

    @After
    public void tearDown() {
        AppHelper.appPrefs = null;
    }

    @Test
    public void getIntOfAppPrefsPrefixesKeyAndReadsValue() {
        fakePrefs.put("pref_key_timeout", 1500);
        assertEquals(1500, AppHelper.getIntOfAppPrefs("timeout", 0));
    }

    @Test
    public void getIntOfAppPrefsReturnsDefaultWhenMissing() {
        assertEquals(42, AppHelper.getIntOfAppPrefs("missing", 42));
    }

    @Test
    public void getStringAsIntOfAppPrefsParsesStoredString() {
        fakePrefs.put("pref_key_mode", "3");
        assertEquals(3, AppHelper.getStringAsIntOfAppPrefs("mode", 0));
    }

    @Test
    public void getStringAsIntOfAppPrefsReturnsDefaultForNull() {
        assertEquals(7, AppHelper.getStringAsIntOfAppPrefs("unset", 7));
    }

    @Test
    public void getBooleanOfAppPrefsPrefixesKeyAndReadsValue() {
        fakePrefs.put("pref_key_enabled", true);
        assertTrue(AppHelper.getBooleanOfAppPrefs("enabled", false));
    }

    @Test
    public void getBooleanOfAppPrefsReturnsDefaultWhenMissing() {
        assertFalse(AppHelper.getBooleanOfAppPrefs("unset"));
    }

    @Test
    public void addStringPairJoinsWithPipe() {
        Set<String> set = new HashSet<>();
        AppHelper.addStringPair(set, "com.example.app", "Shortcut");
        assertTrue(set.contains("com.example.app|Shortcut"));
    }

    @Test
    public void removeStringPairFindsAndRemovesFirstNeedle() {
        Set<String> set = new HashSet<>();
        set.add("com.example.app|Shortcut");
        set.add("com.other.app|Other");

        AppHelper.removeStringPair(set, "com.example.app");
        assertFalse(set.contains("com.example.app|Shortcut"));
        assertTrue(set.contains("com.other.app|Other"));
    }

    @Test
    public void syncPrefsToAnotherWritesEntriesToTarget() {
        Map<String, Object> entries = new HashMap<>();
        entries.put("pref_key_enabled", true);
        entries.put("pref_key_count", 5);
        entries.put("pref_key_label", "test");

        FakeSharedPreferences target = new FakeSharedPreferences();
        AppHelper.syncPrefsToAnother(entries, target, 0, null, true);

        assertEquals(true, target.getBoolean("pref_key_enabled", false));
        assertEquals(5, target.getInt("pref_key_count", 0));
        assertEquals("test", target.getString("pref_key_label", null));
    }

    @Test
    public void syncPrefsToAnotherClearsTargetWhenClearTypeIsOne() {
        Map<String, Object> entries = new HashMap<>();
        entries.put("pref_key_enabled", true);

        FakeSharedPreferences target = new FakeSharedPreferences();
        target.put("pref_key_old", "keep");
        AppHelper.syncPrefsToAnother(entries, target, 1, null, true);

        assertTrue(target.getBoolean("pref_key_enabled", false));
        assertNull(target.getString("pref_key_old", null));
    }

    @Test
    public void syncPrefsToAnotherRemovesKeysMissingFromEntriesWhenClearTypeIsTwo() {
        Map<String, Object> entries = new HashMap<>();
        entries.put("pref_key_keep", true);

        FakeSharedPreferences target = new FakeSharedPreferences();
        target.put("pref_key_keep", false);
        target.put("pref_key_drop", true);
        AppHelper.syncPrefsToAnother(entries, target, 2, null, true);

        assertTrue(target.getBoolean("pref_key_keep", false));
        assertNull(target.getString("pref_key_drop", null));
    }

    private static class FakeSharedPreferences implements SharedPreferences {
        private final Map<String, Object> values = new HashMap<>();

        void put(String key, Object value) {
            values.put(key, value);
        }

        @Override
        public Map<String, ?> getAll() {
            return new HashMap<>(values);
        }

        @Override
        public String getString(String key, String defValue) {
            Object v = values.get(key);
            return v instanceof String ? (String) v : defValue;
        }

        @Override
        public Set<String> getStringSet(String key, Set<String> defValues) {
            Object v = values.get(key);
            return v instanceof Set ? (Set<String>) v : defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            Object v = values.get(key);
            return v instanceof Integer ? (Integer) v : defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            Object v = values.get(key);
            return v instanceof Long ? (Long) v : defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            Object v = values.get(key);
            return v instanceof Float ? (Float) v : defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object v = values.get(key);
            return v instanceof Boolean ? (Boolean) v : defValue;
        }

        @Override
        public boolean contains(String key) {
            return values.containsKey(key);
        }

        @Override
        public Editor edit() {
            return new FakeEditor(this);
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        }
    }

    private static class FakeEditor implements SharedPreferences.Editor {
        private final FakeSharedPreferences prefs;

        FakeEditor(FakeSharedPreferences prefs) {
            this.prefs = prefs;
        }

        @Override
        public SharedPreferences.Editor putString(String key, String value) {
            prefs.values.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
            prefs.values.put(key, values);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            prefs.values.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            prefs.values.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            prefs.values.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            prefs.values.put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            prefs.values.remove(key);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            prefs.values.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return true;
        }

        @Override
        public void apply() {
        }
    }
}
