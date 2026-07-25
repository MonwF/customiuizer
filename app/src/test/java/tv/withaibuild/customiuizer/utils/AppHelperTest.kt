package tv.withaibuild.customiuizer.utils

import android.content.SharedPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppHelperTest {

    private lateinit var fakePrefs: FakeSharedPreferences

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        AppHelper.appPrefs = fakePrefs
    }

    @After
    fun tearDown() {
        AppHelper.appPrefs = null
    }

    @Test
    fun getIntOfAppPrefsPrefixesKeyAndReadsValue() {
        fakePrefs.put("pref_key_timeout", 1500)
        assertEquals(1500, AppHelper.getIntOfAppPrefs("timeout", 0))
    }

    @Test
    fun getIntOfAppPrefsReturnsDefaultWhenMissing() {
        assertEquals(42, AppHelper.getIntOfAppPrefs("missing", 42))
    }

    @Test
    fun getStringAsIntOfAppPrefsParsesStoredString() {
        fakePrefs.put("pref_key_mode", "3")
        assertEquals(3, AppHelper.getStringAsIntOfAppPrefs("mode", 0))
    }

    @Test
    fun getStringAsIntOfAppPrefsReturnsDefaultForNull() {
        assertEquals(7, AppHelper.getStringAsIntOfAppPrefs("unset", 7))
    }

    @Test
    fun getBooleanOfAppPrefsPrefixesKeyAndReadsValue() {
        fakePrefs.put("pref_key_enabled", true)
        assertTrue(AppHelper.getBooleanOfAppPrefs("enabled", false))
    }

    @Test
    fun getBooleanOfAppPrefsReturnsDefaultWhenMissing() {
        assertFalse(AppHelper.getBooleanOfAppPrefs("unset"))
    }

    @Test
    fun addStringPairJoinsWithPipe() {
        val set = HashSet<String>()
        AppHelper.addStringPair(set, "com.example.app", "Shortcut")
        assertTrue(set.contains("com.example.app|Shortcut"))
    }

    @Test
    fun removeStringPairFindsAndRemovesFirstNeedle() {
        val set = HashSet<String>()
        set.add("com.example.app|Shortcut")
        set.add("com.other.app|Other")

        AppHelper.removeStringPair(set, "com.example.app")
        assertFalse(set.contains("com.example.app|Shortcut"))
        assertTrue(set.contains("com.other.app|Other"))
    }

    @Test
    fun syncPrefsToAnotherWritesEntriesToTarget() {
        val entries = HashMap<String, Any>()
        entries["pref_key_enabled"] = true
        entries["pref_key_count"] = 5
        entries["pref_key_label"] = "test"

        val target = FakeSharedPreferences()
        AppHelper.syncPrefsToAnother(entries, target, 0, null, true)

        assertEquals(true, target.getBoolean("pref_key_enabled", false))
        assertEquals(5, target.getInt("pref_key_count", 0))
        assertEquals("test", target.getString("pref_key_label", null))
    }

    @Test
    fun syncPrefsToAnotherClearsTargetWhenClearTypeIsOne() {
        val entries = HashMap<String, Any>()
        entries["pref_key_enabled"] = true

        val target = FakeSharedPreferences()
        target.put("pref_key_old", "keep")
        AppHelper.syncPrefsToAnother(entries, target, 1, null, true)

        assertTrue(target.getBoolean("pref_key_enabled", false))
        assertNull(target.getString("pref_key_old", null))
    }

    @Test
    fun syncPrefsToAnotherRemovesKeysMissingFromEntriesWhenClearTypeIsTwo() {
        val entries = HashMap<String, Any>()
        entries["pref_key_keep"] = true

        val target = FakeSharedPreferences()
        target.put("pref_key_keep", false)
        target.put("pref_key_drop", true)
        AppHelper.syncPrefsToAnother(entries, target, 2, null, true)

        assertTrue(target.getBoolean("pref_key_keep", false))
        assertNull(target.getString("pref_key_drop", null))
    }

    @Test
    fun syncPrefsToAnotherKeepsExistingKeysWhenClearTypeIsZero() {
        val entries = HashMap<String, Any>()
        entries["pref_key_enabled"] = true

        val target = FakeSharedPreferences()
        target.put("pref_key_old", "keep")
        AppHelper.syncPrefsToAnother(entries, target, 0, null, true)

        assertTrue(target.getBoolean("pref_key_enabled", false))
        assertEquals("keep", target.getString("pref_key_old", null))
    }

    @Test(expected = NumberFormatException::class)
    fun getStringAsIntOfAppPrefsThrowsNumberFormatExceptionForNonNumericString() {
        fakePrefs.put("pref_key_count", "not_a_number")
        AppHelper.getStringAsIntOfAppPrefs("count", 7)
    }

    @Test
    fun removeStringPairIsNoOpWhenNeedleIsMissing() {
        val set = HashSet<String>()
        set.add("com.other.app|Other")
        AppHelper.removeStringPair(set, "com.example.app")
        assertEquals(1, set.size)
        assertTrue(set.contains("com.other.app|Other"))
    }

    private class FakeSharedPreferences : SharedPreferences {
        val values = HashMap<String, Any?>()

        fun put(key: String, value: Any?) {
            values[key] = value
        }

        override fun getAll(): Map<String, *> = HashMap(values)

        override fun getString(key: String, defValue: String?): String? {
            val v = values[key]
            return if (v is String) v else defValue
        }

        override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
            val v = values[key]
            @Suppress("UNCHECKED_CAST")
            return if (v is Set<*>) v as Set<String> else defValues
        }

        override fun getInt(key: String, defValue: Int): Int {
            val v = values[key]
            return if (v is Int) v else defValue
        }

        override fun getLong(key: String, defValue: Long): Long {
            val v = values[key]
            return if (v is Long) v else defValue
        }

        override fun getFloat(key: String, defValue: Float): Float {
            val v = values[key]
            return if (v is Float) v else defValue
        }

        override fun getBoolean(key: String, defValue: Boolean): Boolean {
            val v = values[key]
            return if (v is Boolean) v else defValue
        }

        override fun contains(key: String): Boolean = values.containsKey(key)

        override fun edit(): SharedPreferences.Editor = FakeEditor(this)

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
    }

    private class FakeEditor(private val prefs: FakeSharedPreferences) : SharedPreferences.Editor {

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value != null) prefs.values[key] = value
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            if (values != null) prefs.values[key] = values
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            prefs.values[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            prefs.values[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            prefs.values[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            prefs.values[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            prefs.values.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            prefs.values.clear()
            return this
        }

        override fun commit(): Boolean = true

        override fun apply() {}
    }
}
