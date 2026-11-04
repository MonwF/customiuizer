package name.monwf.customiuizer.mods;

import java.util.Map;

/** A validated, immutable snapshot; no framework IPC on a Binder transaction thread. */
public final class NativeFolderSettings {
    public static final int TRANSACTION = 0x0050464c;
    public static final int ACK = 0x50464c31;
    public static final int SCHEMA = 1;
    private static final String PREFIX = "pref_key_launcher_native_folder_";
    public final boolean enabled, hideNewMarker;
    public final int columns, rows, gap, density;

    private NativeFolderSettings(Map<String, ?> prefs) {
        enabled = bool(prefs, PREFIX + "enabled");
        hideNewMarker = bool(prefs, "pref_key_launcher_hide_new_marker");
        columns = number(prefs, "pref_key_launcher_folder_cols", 3, 3, 6);
        rows = number(prefs, PREFIX + "rows", 5, 5, 8);
        gap = number(prefs, PREFIX + "gap", 0, 0, 20);
        density = number(prefs, PREFIX + "density", 1000, 848, 1000);
    }
    public static NativeFolderSettings read(Map<String, ?> prefs) {
        if (prefs == null) throw new IllegalArgumentException("Missing preferences");
        return new NativeFolderSettings(prefs);
    }
    private static boolean bool(Map<String, ?> prefs, String key) {
        Object value = prefs.get(key);
        if (value == null) return false;
        if (!(value instanceof Boolean)) throw new IllegalArgumentException(key);
        return (Boolean) value;
    }
    private static int number(Map<String, ?> prefs, String key, int fallback, int min, int max) {
        Object value = prefs.get(key);
        if (value == null) return fallback;
        if (!(value instanceof Integer) || (Integer)value < min || (Integer)value > max)
            throw new IllegalArgumentException(key);
        return (Integer)value;
    }
    public int[] wire() {
        return new int[]{ACK, SCHEMA, (enabled ? 1 : 0) | (hideNewMarker ? 2 : 0),
                         columns, rows, gap, density};
    }
}
