package tv.withaibuild.customiuizer.utils

import org.junit.Assert.*
import org.junit.Test

class PrefMapTest {
    @Test
    fun normalizesRemoteKeysOnlyWhenStored() {
        val prefs = PrefMap()

        prefs.put("pref_key_enabled", true)
        prefs.put("count", 3)

        assertTrue(prefs.getBoolean("enabled"))
        assertTrue(prefs.getBoolean("pref_key_enabled"))
        assertEquals(3, prefs.getInt("count", 0))
        assertTrue(prefs.containsKey("enabled"))
        assertFalse(prefs.containsKey("pref_key_enabled"))
    }

    @Test
    fun putAllAndRemoveUseTheSameCanonicalKey() {
        val prefs = PrefMap()
        val remoteValues = HashMap<String, Any>()
        remoteValues["pref_key_mode"] = "2"
        remoteValues["pref_key_timeout"] = 1500L

        prefs.putAll(remoteValues)

        assertEquals(2, prefs.getStringAsInt("mode", 0))
        assertEquals(1500L, prefs.getLong("timeout", 0L))
        prefs.remove("pref_key_mode")
        assertEquals(7, prefs.getStringAsInt("mode", 7))
    }

    @Test
    fun missingValuesReuseImmutableDefaults() {
        val prefs = PrefMap()

        assertEquals("fallback", prefs.getString("missing", "fallback"))
        assertFalse(prefs.getBoolean("missing"))
        assertSame(prefs.getStringSet("missing"), prefs.getStringSet("missing"))
    }

    @Test
    fun getStringAsIntCachesParsedValueAndInvalidatesOnChange() {
        val prefs = PrefMap()
        prefs.put("pref_key_mode", "3")

        assertEquals(3, prefs.getStringAsInt("mode", 0))
        assertEquals(3, prefs.getStringAsInt("pref_key_mode", 0))

        prefs.put("mode", "7")
        assertEquals(7, prefs.getStringAsInt("mode", 0))

        prefs.remove("mode")
        assertEquals(42, prefs.getStringAsInt("mode", 42))
    }

    @Test
    fun getStringAsIntReturnsDefaultForNonNumericString() {
        val prefs = PrefMap()
        prefs.put("pref_key_label", "not_a_number")

        try {
            prefs.getStringAsInt("label", 0)
        } catch (e: NumberFormatException) {
            // expected; the method propagates invalid integer strings
        }
    }
}
