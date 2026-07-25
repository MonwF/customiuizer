package tv.withaibuild.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.MiuiMultiWindowUtils;
import android.util.SparseBooleanArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import tv.withaibuild.customiuizer.MainModule;
import tv.withaibuild.customiuizer.R;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;
import tv.withaibuild.customiuizer.utils.Helpers;

public class GlobalActionsIntentHelper {
    enum IntentType {
        APP, ACTIVITY, SHORTCUT
    }

    @SuppressLint("WrongConstant")
    public static Intent getIntent(Context context, String pref, IntentType intentType, boolean skipLock) {
        try {
            if (intentType == IntentType.APP) pref += "_app";
            else if (intentType == IntentType.ACTIVITY) pref += "_activity";
            else if (intentType == IntentType.SHORTCUT) pref += "_shortcut_intent";

            String prefValue = MainModule.mPrefs.getString(pref, null);
            if (prefValue == null) return null;

            Intent intent = new Intent();
            if (intentType == IntentType.SHORTCUT) {
                intent = Intent.parseUri(prefValue, 0);
            } else {
                String[] pkgAppArray = prefValue.split("\\|");
                if (pkgAppArray.length < 2) return null;
                ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
                intent.setComponent(name);
                int user = MainModule.mPrefs.getInt(pref + "_user", 0);
                if (user != 0) intent.putExtra("user", user);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            if (intentType == IntentType.APP) {
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
            }

            if (skipLock) {
                intent.addFlags(335544320);
                intent.putExtra("StartActivityWhenLocked", true);
            }

            return intent;
        } catch (Throwable t) {
            XposedHelpers.log(t);
            return null;
        }
    }

    public static boolean launchAppIntent(Context context, String key, boolean skipLock) {
        return launchIntent(context, getIntent(context, key, IntentType.APP, skipLock));
    }

    public static boolean launchActivityIntent(Context context, String key, boolean skipLock) {
        return launchIntent(context, getIntent(context, key, IntentType.ACTIVITY, skipLock));
    }

    public static boolean launchShortcutIntent(Context context, String key, boolean skipLock) {
        return launchIntent(context, getIntent(context, key, IntentType.SHORTCUT, skipLock));
    }

    public static boolean launchIntent(Context context, Intent intent) {
        if (intent == null) return false;
        Intent bIntent = new Intent(GlobalActions.ACTION_PREFIX + "LaunchIntent");
        bIntent.putExtra("intent", intent);
        context.sendBroadcast(bIntent);
        return true;
    }
}
