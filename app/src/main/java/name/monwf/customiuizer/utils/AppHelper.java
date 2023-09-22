package name.monwf.customiuizer.utils;

import static name.monwf.customiuizer.utils.Helpers.getAppName;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.github.libxposed.service.RemotePreferences;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.mods.GlobalActions;

public class AppHelper {
    public static final String prefsName = "customiuizer_prefs";
    public static SharedPreferences appPrefs = null;
    public static boolean moduleActive = false;

    public static RemotePreferences remotePrefs = null;
    private static final String TAG = "LSPosed-Bridge";
    public static boolean silentSync = false;
    public static String RESTORED_FROM_BACKUP = "restored_from_backup";

    public static void log(String line) {
        Log.i(TAG, "[CustoMIUIzer] " + line);
    }

    public static void log(Throwable t) {
        String logStr = Log.getStackTraceString(t);
        Log.e(TAG, "[CustoMIUIzer] " + logStr);
    }

    public static void log(String mod, String line) {
        Log.i(TAG, "[CustoMIUIzer][" + mod + "] " + line);
    }

    public static void log(String mod, Throwable t) {
        String logStr = Log.getStackTraceString(t);
        Log.e(TAG, "[CustoMIUIzer][" + mod + "] " + logStr);
    }

    public static SharedPreferences getSharedPrefs(Context context, boolean protectedStorage) {
        if (protectedStorage) context = getProtectedContext(context);
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    public static int getIntOfAppPrefs(String key, int defValue) {
        if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
        return appPrefs.getInt(key, defValue);
    }

    public static String getStringOfAppPrefs(String key, String defValue) {
        if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
        return appPrefs.getString(key, defValue);
    }

    public static int getStringAsIntOfAppPrefs(String key, int defValue) {
        if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
        String prefValue = getStringOfAppPrefs(key, null);
        if (prefValue == null) return defValue;
        return Integer.parseInt(prefValue);
    }

    public static Set<String> getStringSetOfAppPrefs(String key, Set<String> defValue) {
        if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
        return appPrefs.getStringSet(key, defValue);
    }

    public static boolean getBooleanOfAppPrefs(String key) {
        return getBooleanOfAppPrefs(key, false);
    }
    public static boolean getBooleanOfAppPrefs(String key, boolean defValue) {
        if (!key.startsWith("pref_key_")) {
            key = "pref_key_" + key;
        }
        return appPrefs.getBoolean(key, defValue);
    }

    public static synchronized Context getLocaleContext(Context context) throws Throwable {
        if (appPrefs != null) {
            String locale = getStringOfAppPrefs("pref_key_miuizer_locale", "auto");
            if ("auto".equals(locale) || "1".equals(locale)) return context;
            Configuration config = context.getResources().getConfiguration();
            config.setLocale(Locale.forLanguageTag(locale));
            return context.createConfigurationContext(config);
        } else {
            return context;
        }
    }

    public static synchronized Context getProtectedContext(Context context) {
        return getProtectedContext(context, null);
    }

    public static synchronized Context getProtectedContext(Context context, Configuration config) {
        try {
            Context mContext = context.isDeviceProtectedStorage() ? context : context.createDeviceProtectedStorageContext();
            return getLocaleContext(config == null ? mContext : mContext.createConfigurationContext(config));
        } catch (Throwable t) {
            return context;
        }
    }

    public static void showInputDialog(Context context, final String key, int titleRes, int summRes, int maxLines, Helpers.InputCallback callback) {
        showInputDialog(context, key, titleRes, summRes, maxLines, callback, true);
    }

    public static void showInputDialog(Context context, final String key, int titleRes, int summRes, int maxLines, Helpers.InputCallback callback, boolean prefDefault) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleRes);
        final EditText input = new EditText(context);
        if (prefDefault) {
            input.setText(getStringOfAppPrefs(key, ""));
        }
        else {
            input.setText(key);
        }

        if (maxLines > 1) {
            input.setSingleLine(false);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }
        else {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        }

        LinearLayout container = new LinearLayout(context);
        int horizPadding = context.getResources().getDimensionPixelSize(R.dimen.preference_item_child_padding);
        container.setPadding(horizPadding, 0, horizPadding, 0);
        container.setOrientation(LinearLayout.VERTICAL);
        if (summRes > 0) {
            final TextView msg = new TextView(context);
            msg.setText(summRes);
            msg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            container.addView(msg);
        }
        container.addView(input);
        builder.setView(container);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onInputFinished(key, input.getText().toString());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public static void addStringPair(Set<String> hayStack, String needle1, String needle2) {
        if (hayStack != null) hayStack.add(needle1 + "|" + needle2);
    }

    public static void removeStringPair(Set<String> hayStack, String needle) {
        if (hayStack != null)
            for (String pair: hayStack) {
                String[] needles = pair.split("\\|", 2);
                if (needles[0].equals(needle)) {
                    hayStack.remove(pair);
                    return;
                }
            }
    }
    public static Pair<String, String> getActionNameLocal(Context context, String key) {
        try {
            int action = AppHelper.getIntOfAppPrefs(key + "_action", 1);
            Resources modRes = context.getResources();
            Pair<String, String> pair = null;
            int resId = GlobalActions.getActionResId(action);
            if (resId != 0)
                pair = new Pair<>(modRes.getString(resId), "");
            else if (action == 8)
                pair = new Pair<>(modRes.getString(R.string.array_global_actions_launch), (String)getAppName(context, AppHelper.getStringOfAppPrefs(key + "_app", ""), true));
            else if (action == 9)
                pair = new Pair<>(modRes.getString(R.string.array_global_actions_shortcut), AppHelper.getStringOfAppPrefs(key + "_shortcut_name", ""));
            else if (action == 10) {
                int what = AppHelper.getIntOfAppPrefs(key + "_toggle", 0);
                String toggle = modRes.getString(R.string.array_global_actions_toggle);
                switch (what) {
                    case 1: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_wifi)); break;
                    case 2: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_bt)); break;
                    case 3: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_gps)); break;
                    case 4: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_nfc)); break;
                    case 5: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_sound)); break;
                    case 6: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_brightness)); break;
                    case 7: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_rotation)); break;
                    case 8: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_torch)); break;
                    case 9: pair = new Pair<>(toggle, modRes.getString(R.string.array_global_toggle_mobiledata)); break;
                }
            } else if (action == 20) {
                String pref = AppHelper.getStringOfAppPrefs(key + "_activity", "");
                String name = (String)getAppName(context, pref);
                if (name == null || name.isEmpty()) name = (String)getAppName(context, pref, true);
                pair = new Pair<>(modRes.getString(R.string.array_global_actions_activity), name);
            }
            return pair;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
    public static void syncPrefsToAnother(Map<String, ?> entries, SharedPreferences prefs, int clearType, Set<String> ignoreKeys, boolean commitAction) {
        if (entries == null || entries.isEmpty()) return;
        SharedPreferences.Editor prefEdit = prefs.edit();
        if (clearType == 1) {
            prefEdit.clear();
        }
        else if (clearType == 2) {
            for (String key:prefs.getAll().keySet()) {
                if (!entries.containsKey(key)) {
                    prefEdit.remove(key);
                }
            }
        }
        for (Map.Entry<String, ?> entry: entries.entrySet()) {
            String key = entry.getKey();
            if (ignoreKeys != null && ignoreKeys.contains(key)) continue;
            Object val = entry.getValue();

            if (val instanceof Boolean)
                prefEdit.putBoolean(key, (Boolean)val);
            else if (val instanceof Float)
                prefEdit.putFloat(key, (Float)val);
            else if (val instanceof Integer)
                prefEdit.putInt(key, (Integer)val);
            else if (val instanceof Long)
                prefEdit.putLong(key, (Long)val);
            else if (val instanceof String)
                prefEdit.putString(key, ((String)val));
            else if (val instanceof Set<?>)
                prefEdit.putStringSet(key, ((Set<String>)val));
        }
        if (commitAction) {
            prefEdit.commit();
        }
        else {
            prefEdit.apply();
        }
    }
}
