import java.util.HashMap;
import java.util.Map;
import name.monwf.customiuizer.mods.NativeFolderSettings;
public final class NativeFolderSettingsTest {
    public static void main(String[] args) {
        Map<String, Object> prefs = new HashMap<>();
        NativeFolderSettings off = NativeFolderSettings.read(prefs);
        assert !off.enabled && !off.hideNewMarker && off.wire()[2] == 0;
        assert off.columns == 3 && off.rows == 5 && off.density == 1000;
        prefs.put("pref_key_launcher_native_folder_enabled", true);
        prefs.put("pref_key_launcher_folder_cols", 6);
        prefs.put("pref_key_launcher_native_folder_rows", 8);
        prefs.put("pref_key_launcher_native_folder_density", 848);
        NativeFolderSettings layout = NativeFolderSettings.read(prefs);
        assert layout.wire()[2] == 1 && layout.rows == 8 && layout.columns == 6;
        prefs.put("pref_key_launcher_native_folder_enabled", false);
        prefs.put("pref_key_launcher_hide_new_marker", true);
        assert NativeFolderSettings.read(prefs).wire()[2] == 2;
        assert layout.enabled; // published snapshot must not follow mutable source map.
        prefs.put("pref_key_launcher_native_folder_rows", 99);
        try { NativeFolderSettings.read(prefs); throw new AssertionError("range accepted"); }
        catch (IllegalArgumentException expected) {}
        prefs.put("pref_key_launcher_native_folder_rows", "8");
        try { NativeFolderSettings.read(prefs); throw new AssertionError("type accepted"); }
        catch (IllegalArgumentException expected) {}
        try { NativeFolderSettings.read(null); throw new AssertionError("missing accepted"); }
        catch (IllegalArgumentException expected) {}
        System.out.println("Native folder preference tests passed");
    }
}
