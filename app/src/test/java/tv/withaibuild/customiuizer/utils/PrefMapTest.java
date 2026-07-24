package tv.withaibuild.customiuizer.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PrefMapTest {
    @Test
    public void normalizesRemoteKeysOnlyWhenStored() {
        PrefMap prefs = new PrefMap();

        prefs.put("pref_key_enabled", true);
        prefs.put("count", 3);

        assertTrue(prefs.getBoolean("enabled"));
        assertTrue(prefs.getBoolean("pref_key_enabled"));
        assertEquals(3, prefs.getInt("count", 0));
        assertTrue(prefs.containsKey("enabled"));
        assertFalse(prefs.containsKey("pref_key_enabled"));
    }

    @Test
    public void putAllAndRemoveUseTheSameCanonicalKey() {
        PrefMap prefs = new PrefMap();
        Map<String, Object> remoteValues = new HashMap<>();
        remoteValues.put("pref_key_mode", "2");
        remoteValues.put("pref_key_timeout", 1500L);

        prefs.putAll(remoteValues);

        assertEquals(2, prefs.getStringAsInt("mode", 0));
        assertEquals(1500L, prefs.getLong("timeout", 0L));
        prefs.remove("pref_key_mode");
        assertEquals(7, prefs.getStringAsInt("mode", 7));
    }

    @Test
    public void missingValuesReuseImmutableDefaults() {
        PrefMap prefs = new PrefMap();

        assertEquals("fallback", prefs.getString("missing", "fallback"));
        assertFalse(prefs.getBoolean("missing"));
        assertSame(prefs.getStringSet("missing"), prefs.getStringSet("missing"));
    }
}
