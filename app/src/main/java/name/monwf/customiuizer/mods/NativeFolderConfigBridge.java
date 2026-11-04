package name.monwf.customiuizer.mods;

import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHookParam;

/** Read-only private transaction on an existing service; never reads clipboard data.
 * Transport pattern: WOMMO / NativeHookKit (GPL-3.0), see docs/native-folders.md.
 */
public final class NativeFolderConfigBridge {
    private static volatile NativeFolderSettings snapshot;
    private static SharedPreferences source;
    private static SharedPreferences.OnSharedPreferenceChangeListener listener;
    private static void refresh() {
        try { snapshot = NativeFolderSettings.read(source.getAll()); }
        catch (Throwable failure) { snapshot = null; XposedHelpers.log("NativeFolders", failure); }
    }
    public static void install(SharedPreferences preferences) {
        if (source != null) return;
        source = preferences;
        refresh();
        listener = (prefs, key) -> refresh();
        source.registerOnSharedPreferenceChangeListener(listener);
        ModuleHelper.findAndHookMethod("android.content.IClipboard$Stub", null, "onTransact",
                int.class, Parcel.class, Parcel.class, int.class, new MethodHook() {
            @Override protected void before(MethodHookParam param) {
                if ((int)param.getArgs()[0] != NativeFolderSettings.TRANSACTION) return;
                Parcel data = (Parcel)param.getArgs()[1];
                Parcel reply = (Parcel)param.getArgs()[2];
                int flags = (int)param.getArgs()[3];
                if (reply == null || (flags & IBinder.FLAG_ONEWAY) != 0) return;
                int position = data.dataPosition();
                try {
                    data.enforceInterface("android.content.IClipboard");
                    if (data.dataAvail() != 0 || !isLauncherCaller()) return;
                    NativeFolderSettings current = snapshot;
                    if (current == null) return; // Unavailable is not an enabled default.
                    reply.setDataSize(0);
                    reply.setDataPosition(0);
                    for (int value : current.wire()) reply.writeInt(value);
                    param.returnAndSkip(true);
                } catch (Throwable ignored) {
                    // An unrecognized/failed request keeps the original service behavior.
                } finally { data.setDataPosition(position); }
            }
        });
    }
    private static boolean isLauncherCaller() throws Exception {
        int uid = Binder.getCallingUid(), pid = Binder.getCallingPid();
        if (uid < 10000 || pid <= 0) return false;
        try (FileInputStream input = new FileInputStream("/proc/" + pid + "/cmdline")) {
            byte[] bytes = new byte[256];
            int n = input.read(bytes), end = 0;
            while (end < n && bytes[end] != 0) end++;
            if (!"com.miui.home".equals(new String(bytes, 0, end, StandardCharsets.UTF_8))) return false;
        }
        // Bind the process name check to the Binder caller, including multi-user UIDs.
        try (FileInputStream input = new FileInputStream("/proc/" + pid + "/status")) {
            byte[] bytes = new byte[8192];
            int n = input.read(bytes);
            if (n <= 0) return false;
            for (String line : new String(bytes, 0, n, StandardCharsets.UTF_8).split("\n")) {
                if (line.startsWith("Uid:")) {
                    String[] ids = line.substring(4).trim().split("\\s+");
                    return ids.length == 4 && Integer.parseInt(ids[0]) == uid && Integer.parseInt(ids[1]) == uid;
                }
            }
        }
        return false;
    }
}
