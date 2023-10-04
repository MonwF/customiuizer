package name.monwf.customiuizer.utils;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceScreen;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import miui.util.HapticFeedbackUtil;
import name.monwf.customiuizer.BuildConfig;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.prefs.PreferenceCategoryEx;

@SuppressWarnings("WeakerAccess")
public class Helpers {

    @SuppressLint("StaticFieldLeak")

    public static final String modulePkg = BuildConfig.APPLICATION_ID;
    //	public static final String versionFile = "xposed_version";
//	public static final String wallpaperFile = "lockscreen_wallpaper";
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    public static final String MIUIZER_NS = "http://schemas.android.com/apk/res-auto";
    public static final String ACCESS_SECURITY_CENTER = "com.miui.securitycenter.permission.ACCESS_SECURITY_CENTER_PROVIDER";
    public static final String NEW_MODS_SEARCH_QUERY = "\uD83C\uDD95";
    public static ArrayList<AppData> shareAppsList = null;
    public static ArrayList<AppData> openWithAppsList = null;
    public static ArrayList<AppData> installedAppsList = null;
    public static ArrayList<AppData> launchableAppsList = null;
    public static ArrayList<ModData> allModsList = new ArrayList<ModData>();
    public static final int markColor = Color.rgb(205, 73, 97);
    public static final int markColorVibrant = Color.rgb(255, 0, 0);
    public static final int REQUEST_PERMISSIONS_WIFI = 3;
    public static final int REQUEST_PERMISSIONS_REPORT = 4;
    public static final int REQUEST_PERMISSIONS_BLUETOOTH = 5;
    public static final int REQUEST_PERMISSIONS_SECURITY_CENTER = 6;
    public static boolean withinAppContext = false;

    public static final boolean isMIUI14 = Integer.parseInt(miui.os.Build.getMiUiVersionCode()) > 13;

    public static LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>((int)(Runtime.getRuntime().maxMemory() / 1024) / 2) {
        @Override
        protected int sizeOf(String key, Bitmap icon) {
            if (icon != null)
                return icon.getAllocationByteCount() / 1024;
            else
                return 130 * 130 * 4 / 1024;
        }
    };
    public static WakeLock mWakeLock;
    public static boolean showNewMods = true;
    public static final HashSet<String> newMods = new HashSet<String>(Arrays.asList(
        "pref_key_launcher_nozoomanim"
    ));

    public enum SettingsType {
        Preference, Edit
    }

    public enum AppAdapterType {
        Default, Standalone, Mutli, CustomTitles, Activities
    }

    public enum ActionBarType {
        HomeUp, Edit
    }

    public static class MimeType {
        public static int IMAGE = 1;
        public static int AUDIO = 2;
        public static int VIDEO = 4;
        public static int DOCUMENT = 8;
        public static int ARCHIVE = 16;
        public static int LINK = 32;
        public static int OTHERS = 64;
        public static int ALL = IMAGE | AUDIO | VIDEO | DOCUMENT | ARCHIVE | LINK | OTHERS;
    }

    public static void setMiuiCheckbox(CheckBox checkbox) {
        checkbox.setBackground(null);
        int btnResID = checkbox.getResources().getIdentifier(isNightMode(checkbox.getContext()) ? "btn_checkbox_dark" : "btn_checkbox_light", "drawable", "miui");
        try {
            checkbox.setButtonDrawable(btnResID == 0 ? R.drawable.btn_checkbox : btnResID);
        } catch (Throwable t) {
            checkbox.setButtonDrawable(R.drawable.btn_checkbox);
        }
    }

    public static void setMiuiPrefItem(View item) {
        item.setBackgroundResource(R.drawable.list_item_bg);
        TextView title = item.findViewById(android.R.id.title);
        int resId = item.getResources().getIdentifier("preference_item_bg", "drawable", "miui");
        if (resId != 0) item.setBackgroundResource(resId);
        resId = item.getResources().getIdentifier("normal_text_size", "dimen", "miui");
        if (resId != 0 && title != null) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, item.getResources().getDimensionPixelSize(resId));
        }
        resId = item.getResources().getIdentifier("secondary_text_size", "dimen", "miui");
        if (resId != 0) {
            TextView summary = item.findViewById(android.R.id.summary);
            TextView text1 = item.findViewById(android.R.id.text1);
            TextView text2 = item.findViewById(android.R.id.text2);
            int size = item.getResources().getDimensionPixelSize(resId);
            if (summary != null) summary.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            if (text1 != null) text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            if (text2 != null) text2.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
        if (title != null && "header".equals(title.getTag())) {
            int resIdSize = item.getResources().getIdentifier("preference_category_text_size", "dimen", "miui");
            if (resIdSize != 0) title.setTextSize(TypedValue.COMPLEX_UNIT_PX, item.getResources().getDimensionPixelSize(resIdSize));
        }

        int resIdLeft = item.getResources().getIdentifier("preference_item_padding_left", "dimen", "miui");
        int resIdRight = item.getResources().getIdentifier("preference_item_padding_right", "dimen", "miui");
        int resIdTop = item.getResources().getIdentifier("preference_item_padding_top", "dimen", "miui");
        int resIdBottom = item.getResources().getIdentifier("preference_item_padding_bottom", "dimen", "miui");
        int paddingLeft = resIdLeft == 0 ? item.getPaddingLeft() : item.getResources().getDimensionPixelSize(resIdLeft);
        int paddingRight = resIdRight == 0 ? item.getPaddingRight() : item.getResources().getDimensionPixelSize(resIdRight);
        int paddingTop = resIdTop == 0 ? item.getPaddingTop() : item.getResources().getDimensionPixelSize(resIdTop);
        int paddingBottom = resIdBottom == 0 ? item.getPaddingBottom() : item.getResources().getDimensionPixelSize(resIdBottom);
        item.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    public static boolean isNightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isUPlus() {
        return Build.VERSION.SDK_INT >= 34;
    }

    public static boolean isDeviceEncrypted(Context context) {
        DevicePolicyManager policyMgr = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        int encryption = policyMgr.getStorageEncryptionStatus();
        return
            encryption == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
                encryption == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING ||
                encryption == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER;
    }

//	public static boolean isLauncherIconVisible(Context context) {
//		return context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, GateWayLauncher.class)) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
//	}

    public static void launchActivity(AppCompatActivity act, String pkg, String cmp) {
        launchActivity(act, pkg, cmp, false);
    }
    public static boolean launchActivity(AppCompatActivity act, String pkg, String cmp, boolean silent) {
        PackageManager pm = act.getPackageManager();
        try {
            pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setComponent(new ComponentName(pkg, cmp));
            act.startActivity(intent);
            act.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
            return true;
        } catch (Throwable t) {
            if (!silent) Toast.makeText(act, R.string.various_hiddenfeatures_not_found, Toast.LENGTH_LONG).show();
            return false;
        }
    }
//	public static boolean isScreenOn(Context context) {
//		DisplayManager dispMgr = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
//		for (Display display: dispMgr.getDisplays())
//		if (display.getState() != Display.STATE_OFF) return true;
//		return false;
//	}

    public static void hideKeyboard(AppCompatActivity act, View view) {
        try {
            Context context = act == null ? view.getContext() : act;
            InputMethodManager inputManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager == null) return;
            IBinder token = null;
            View currentFocusedView = act == null ? view : act.getCurrentFocus();
            if (currentFocusedView != null)
                token = currentFocusedView.getWindowToken();
            if (token != null)
                inputManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void showOKDialog(Context context, int title, int text) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(text);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    public interface InputCallback {
        void onInputFinished(String key, String text);
    }

    public static boolean checkStorageReadable(Context context) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            showOKDialog(context, R.string.warning, R.string.storage_unavailable);
            return false;
        }
    }

    public static boolean checkSettingsPerm(AppCompatActivity act) {
        return act.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermAndRequest(AppCompatActivity act, String perm, int action) {
        if (act.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
            act.requestPermissions(new String[]{ perm }, action);
            return false;
        }
        return true;
    }

    public static void emptyFile(String pathToFile, boolean forceClear) {
        File f = new File(pathToFile);
        if (f.exists() && (f.length() > 150 * 1024 || forceClear)) {
            try (FileOutputStream fOut = new FileOutputStream(f, false)) {
                try (OutputStreamWriter output = new OutputStreamWriter(fOut)) {
                    output.write("");
                } catch (Throwable ignore) {}
            } catch (Throwable ignore) {}
        }
    }

    public static long getNextStockAlarmTime(Context context) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr == null) return 0;
        AlarmManager.AlarmClockInfo aci = alarmMgr.getNextAlarmClock();
        return aci == null ? 0 : aci.getTriggerTime();
    }

    public static void updateNewModsMarking(Context context, int opt) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(modulePkg, 0);
            long appInstalled = System.currentTimeMillis() - new File(appInfo.sourceDir).lastModified();
//			Log.e("miuizer", "installed: " + appInstalled + " msecs or " + appInstalled / (1000 * 60 * 60) + " hrs");
            if (opt == 0)
                showNewMods = false;
            else if (opt == 4)
                showNewMods = true;
            else
                showNewMods = appInstalled < (opt == 1 ? 1 : (opt == 2 ? 3 : 7)) * 24 * 60 * 60 * 1000;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void applyNewMod(TextView title) {
        CharSequence titleStr = title.getText();
        String newModStr = title.getResources().getString(R.string.miuizer_new_mod) + " ";
        int start = titleStr.length() + 3;
        int end = start + newModStr.length();
        SpannableStringBuilder ssb = new SpannableStringBuilder(title.getText() + "   " + newModStr);
        ssb.setSpan(new ForegroundColorSpan(markColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new RelativeSizeSpan(0.75f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setText(ssb);
    }

    public static void applySearchItemHighlight(View finalView) {
        int highColor = finalView.getResources().getColor(R.color.color_popup_background, finalView.getContext().getTheme());
        ObjectAnimator colorAnim = ObjectAnimator.ofInt(finalView, "backgroundColor", highColor, Color.TRANSPARENT);
        colorAnim.setDuration(1200);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(1);
        colorAnim.setStartDelay(300);
        colorAnim.start();
    }

    public static void openURL(Context context, String url) {
        if (context == null) return;
        Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(uriIntent);
    }

    public static float dp2px(float dp) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().getDisplayMetrics()
        );
    }

    public static boolean isReallyVisible(final View view) {
        if (view == null || !view.isShown() || view.getAlpha() == 0) return false;
        final Rect actualPosition = new Rect();
        view.getGlobalVisibleRect(actualPosition);
        return actualPosition.intersect(new Rect(0, 0, view.getResources().getDisplayMetrics().widthPixels, view.getResources().getDisplayMetrics().heightPixels));
    }

    public static ArrayList<View> getChildViewsRecursive(View view) {
        return getChildViewsRecursive(view, true);
    }

    public static ArrayList<View> getChildViewsRecursive(View view, boolean includeContainers) {
        if (view instanceof ViewGroup) {
            ArrayList<View> list2 = new ArrayList<View>();
            ViewGroup viewgroup = (ViewGroup)view;
            int i = 0;
            do {
                if (i >= viewgroup.getChildCount()) return list2;
                View view1 = viewgroup.getChildAt(i);
                ArrayList<View> list3 = new ArrayList<View>();
                if (includeContainers) list3.add(view);
                list3.addAll(getChildViewsRecursive(view1));
                list2.addAll(list3);
                i++;
            } while (true);
        } else {
            ArrayList<View> list1 = new ArrayList<View>();
            list1.add(view);
            return list1;
        }
    }

    private static String getModTitle(Resources res, String title) {
        if (title == null) return null;
        int titleResId = Integer.parseInt(title.substring(1));
        if (titleResId <= 0) return null;
        return res.getString(titleResId);
    }

    private static boolean checkMultiUserPermission(Context context) {
        return context.getPackageManager().checkPermission("android.permission.INTERACT_ACROSS_USERS", modulePkg) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings({"JavaReflectionInvocation", "ConstantConditions"})
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    public static float getAnimationScale(int type) {
        try {
            Class<?> smClass = Class.forName("android.os.ServiceManager");
            Method getService = smClass.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            Object manager = getService.invoke(smClass, "window");

            Class<?> wmsClass = Class.forName("android.view.IWindowManager$Stub");
            Method asInterface = wmsClass.getDeclaredMethod("asInterface", IBinder.class);
            asInterface.setAccessible(true);
            Object wm = asInterface.invoke(wmsClass, manager);

            Method getAnimationScale = wm.getClass().getDeclaredMethod("getAnimationScale", int.class);
            getAnimationScale.setAccessible(true);
            return (float)getAnimationScale.invoke(wm, type);
        } catch (Throwable t) {
            t.printStackTrace();
            return 1.0f;
        }
    }

    @SuppressWarnings({"JavaReflectionInvocation", "ConstantConditions"})
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    public static void setAnimationScale(int type, float value) {
        try {
            Class<?> smClass = Class.forName("android.os.ServiceManager");
            Method getService = smClass.getDeclaredMethod("getService", String.class);
            getService.setAccessible(true);
            Object manager = getService.invoke(smClass, "window");

            Class<?> wmsClass = Class.forName("android.view.IWindowManager$Stub");
            Method asInterface = wmsClass.getDeclaredMethod("asInterface", IBinder.class);
            asInterface.setAccessible(true);
            Object wm = asInterface.invoke(wmsClass, manager);

            Method setAnimationScale = wm.getClass().getDeclaredMethod("setAnimationScale", int.class, float.class);
            setAnimationScale.setAccessible(true);
            setAnimationScale.invoke(wm, type, value);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    private static Method getPackageInfoAsUser(Context context) {
        try {
            return context.getPackageManager().getClass().getDeclaredMethod("getPackageInfoAsUser", String.class, int.class, int.class);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static void getInstalledApps(Context context) {
        final PackageManager pm = context.getPackageManager();
        boolean includeDualApps = checkMultiUserPermission(context);
        Method getPackageInfoAsUser = getPackageInfoAsUser(context);
        if (getPackageInfoAsUser == null) includeDualApps = false;

        List<ApplicationInfo> packs = pm.getInstalledApplications(PackageManager.GET_META_DATA | PackageManager.MATCH_DISABLED_COMPONENTS);
        installedAppsList = new ArrayList<AppData>();
        AppData app;
        for (ApplicationInfo pack: packs) try {
            app = new AppData();
            app.enabled = pack.enabled;
            app.label = pack.loadLabel(pm).toString();
            app.pkgName = pack.packageName;
            app.actName = "-";
            installedAppsList.add(app);
            if (includeDualApps) try {
                if (getPackageInfoAsUser.invoke(pm, app.pkgName, 0, 999) != null) {
                    AppData appDual = new AppData();
                    appDual.enabled = pack.enabled;
                    appDual.label = pack.loadLabel(pm).toString();
                    appDual.pkgName = pack.packageName;
                    appDual.actName = "-";
                    appDual.user = 999;
                    installedAppsList.add(appDual);
                }
            } catch (Throwable ignore) {}
        } catch (Throwable e) {
            e.printStackTrace();
        }
        installedAppsList.sort(new Comparator<AppData>() {
            public int compare(AppData app1, AppData app2) {
                return app1.label.compareToIgnoreCase(app2.label);
            }
        });
    }

    @SuppressLint("DiscouragedPrivateApi")
    public static void getLaunchableApps(Context context) {
        final PackageManager pm = context.getPackageManager();
        boolean includeDualApps = checkMultiUserPermission(context);
        Method getPackageInfoAsUser = getPackageInfoAsUser(context);
        if (getPackageInfoAsUser == null) includeDualApps = false;

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, 0);
        launchableAppsList = new ArrayList<AppData>();
        AppData app;
        for (ResolveInfo pack: packs) try {
            app = new AppData();
            app.pkgName = pack.activityInfo.applicationInfo.packageName;
            app.actName = pack.activityInfo.name;
            app.enabled = pack.activityInfo.enabled;
            app.label = pack.loadLabel(pm).toString();
            launchableAppsList.add(app);
            if (includeDualApps) try {
                if (getPackageInfoAsUser.invoke(pm, app.pkgName, 0, 999) != null) {
                    AppData appDual = new AppData();
                    appDual.pkgName = pack.activityInfo.applicationInfo.packageName;
                    appDual.actName = pack.activityInfo.name;
                    appDual.enabled = pack.activityInfo.enabled;
                    appDual.label = pack.loadLabel(pm).toString();
                    appDual.user = 999;
                    launchableAppsList.add(appDual);
                }
            } catch (Throwable ignore) {}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        launchableAppsList.sort(new Comparator<AppData>() {
            public int compare(AppData app1, AppData app2) {
                return app1.label.compareToIgnoreCase(app2.label);
            }
        });
    }

    public static void getShareApps(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean includeDualApps = checkMultiUserPermission(context);
        Method getPackageInfoAsUser = getPackageInfoAsUser(context);
        if (getPackageInfoAsUser == null) includeDualApps = false;

        final Intent mainIntent = new Intent();
        mainIntent.setAction(Intent.ACTION_SEND);
        mainIntent.setType("*/*");
        mainIntent.putExtra("CustoMIUIzer", true);
        List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL | PackageManager.MATCH_DISABLED_COMPONENTS);
        shareAppsList = new ArrayList<AppData>();
        AppData app;
        for (ResolveInfo pack: packs) try {
            boolean exists = false;
            for (AppData shareApp: shareAppsList)
                if (shareApp.pkgName.equals(pack.activityInfo.applicationInfo.packageName)) {
                    exists = true;
                    break;
                }
            if (exists) continue;
            app = new AppData();
            app.pkgName = pack.activityInfo.applicationInfo.packageName;
            app.actName = "-";
            app.enabled = pack.activityInfo.applicationInfo.enabled;
            app.label = pack.activityInfo.applicationInfo.loadLabel(pm).toString();
            shareAppsList.add(app);
            if (includeDualApps) try {
                if (getPackageInfoAsUser.invoke(pm, app.pkgName, 0, 999) != null) {
                    AppData appDual = new AppData();
                    appDual.pkgName = pack.activityInfo.applicationInfo.packageName;
                    appDual.actName = "-";
                    appDual.enabled = pack.activityInfo.applicationInfo.enabled;
                    appDual.label = pack.activityInfo.applicationInfo.loadLabel(pm).toString();
                    appDual.user = 999;
                    shareAppsList.add(appDual);
                }
            } catch (Throwable ignore) {}
        } catch (Throwable e) {
            e.printStackTrace();
        }
        shareAppsList.sort(new Comparator<AppData>() {
            public int compare(AppData app1, AppData app2) {
                return app1.label.compareToIgnoreCase(app2.label);
            }
        });
    }

    public static void getOpenWithApps(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean includeDualApps = checkMultiUserPermission(context);
        Method getPackageInfoAsUser = getPackageInfoAsUser(context);
        if (getPackageInfoAsUser == null) includeDualApps = false;

        Intent mainIntent = new Intent();
        mainIntent.setAction(Intent.ACTION_VIEW);
        mainIntent.setDataAndType(null, "*/*");
//        mainIntent.setDataAndType(Uri.parse("content://" + PrefsProvider.AUTHORITY + "/test/5"), "*/*");
        mainIntent.putExtra("CustoMIUIzer", true);
        List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL | PackageManager.MATCH_DISABLED_COMPONENTS);

        mainIntent = new Intent();
        mainIntent.setAction(Intent.ACTION_VIEW);
        mainIntent.setData(Uri.parse("https://github.com"));
        mainIntent.putExtra("CustoMIUIzer", true);
        List<ResolveInfo> packs2 = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL);

        packs.addAll(packs2);

        openWithAppsList = new ArrayList<AppData>();
        AppData app;
        for (ResolveInfo pack: packs) try {
            boolean exists = false;
            for (AppData openWithApp: openWithAppsList)
                if (openWithApp.pkgName.equals(pack.activityInfo.applicationInfo.packageName)) {
                    exists = true;
                    break;
                }
            if (exists) continue;
            app = new AppData();
            app.pkgName = pack.activityInfo.applicationInfo.packageName;
            app.actName = "-";
            app.enabled = pack.activityInfo.applicationInfo.enabled;
            app.label = pack.activityInfo.applicationInfo.loadLabel(pm).toString();
            openWithAppsList.add(app);
            if (includeDualApps) try {
                if (getPackageInfoAsUser.invoke(pm, app.pkgName, 0, 999) != null) {
                    AppData appDual = new AppData();
                    appDual.pkgName = pack.activityInfo.applicationInfo.packageName;
                    appDual.actName = "-";
                    appDual.enabled = pack.activityInfo.applicationInfo.enabled;
                    appDual.label = pack.activityInfo.applicationInfo.loadLabel(pm).toString();
                    appDual.user = 999;
                    openWithAppsList.add(appDual);
                }
            } catch (Throwable ignore) {}
        } catch (Throwable e) {
            e.printStackTrace();
        }
        openWithAppsList.sort(new Comparator<AppData>() {
            public int compare(AppData app1, AppData app2) {
                return app1.label.compareToIgnoreCase(app2.label);
            }
        });
    }

    public static CharSequence getAppName(Context context, String pkgActName) {
        return getAppName(context, pkgActName, false);
    }

    public static CharSequence getAppName(Context context, String pkgActName, boolean forcePkg) {
        PackageManager pm = context.getPackageManager();
        String not_selected = context.getResources().getString(R.string.notselected);
        String[] pkgActArray = pkgActName.split("\\|");
        ApplicationInfo ai;

        if (!pkgActName.equals(not_selected))
            if (pkgActArray.length >= 1 && pkgActArray[0] != null) try {
                if (!forcePkg && pkgActArray.length >= 2 && pkgActArray[1] != null && !pkgActArray[1].trim().equals("")) {
                    return pm.getActivityInfo(new ComponentName(pkgActArray[0], pkgActArray[1]), 0).loadLabel(pm).toString();
                } else if (!pkgActArray[0].trim().equals("")) {
                    ai = pm.getApplicationInfo(pkgActArray[0], 0);
                    return pm.getApplicationLabel(ai);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        return null;
    }

    public static Drawable getAppIcon(Context context, String pkgActName) {
        return getAppIcon(context, pkgActName, false);
    }

    public static Drawable getAppIcon(Context context, String pkgActName, boolean forcePkg) {
        PackageManager pm = context.getPackageManager();
        String not_selected = context.getResources().getString(R.string.notselected);
        String[] pkgActArray = pkgActName.split("\\|");

        if (!pkgActName.equals(not_selected))
            if (pkgActArray.length >= 1 && pkgActArray[0] != null) try {
                if (!forcePkg && pkgActArray.length >= 2 && pkgActArray[1] != null && !pkgActArray[1].trim().equals(""))
                    return pm.getActivityIcon(new ComponentName(pkgActArray[0], pkgActArray[1]));
                else if (!pkgActArray[0].trim().equals(""))
                    return pm.getApplicationIcon(pkgActArray[0]);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        return null;
    }

    public static Drawable getShortcutIcon(Context context, String key) {
        Drawable shortcutIcon = null;
        String shortcutIconPath = context.getFilesDir() + "/shortcuts/" + key + "_shortcut.png";
        File shortcutIconFile = new File(shortcutIconPath);
        if (shortcutIconFile.exists())
            shortcutIcon = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(shortcutIconFile.getAbsolutePath()));
        Drawable[] layers = { shortcutIcon };
        LayerDrawable insetShortcutIcon = new LayerDrawable(layers);
        int padding = (int)(5 * context.getResources().getDisplayMetrics().density);
        insetShortcutIcon.setLayerInset(0, padding, padding, padding, padding);
        return insetShortcutIcon;
    }

    @SuppressWarnings("ConstantConditions")
    public static Drawable getActionImageLocal(Context context, String key) {
        try {
            int action = AppHelper.getIntOfAppPrefs(key + "_action", 1);
            if (action == 8)
                return getAppIcon(context, AppHelper.getStringOfAppPrefs(key + "_app", ""));
            else if (action == 9)
                return getShortcutIcon(context, key);
            else if (action == 20)
                return getAppIcon(context, AppHelper.getStringOfAppPrefs(key + "_activity", ""), true);
            else
                return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static void parsePrefXml(Context context, int xmlResId) {
        Resources res = context.getResources();
        String lastPrefSub = null;
        String lastPrefSubTitle = null;
        String lastPrefSubSubTitle = null;
        int catResId = 0;
        ModData.ModCat catPrefKey = null;

        switch(xmlResId) {
            case R.xml.prefs_system:
                catResId = R.string.system_mods;
                catPrefKey = ModData.ModCat.pref_key_system;
                break;
            case R.xml.prefs_launcher:
                catResId = R.string.launcher_title;
                catPrefKey = ModData.ModCat.pref_key_launcher;
                break;
            case R.xml.prefs_controls:
                catResId = R.string.controls_mods;
                catPrefKey = ModData.ModCat.pref_key_controls;
                break;
            case R.xml.prefs_various:
                catResId = R.string.various_mods;
                catPrefKey = ModData.ModCat.pref_key_various;
                break;
        }

        try (XmlResourceParser xml = res.getXml(xmlResId)) {
            int eventType = xml.getEventType();
            int order = 0;
            String PrefCatExName = PreferenceCategoryEx.class.getCanonicalName();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && !PreferenceScreen.class.getSimpleName().equals(xml.getName())) try {
                    if (xml.getName().equals(PrefCatExName)) {
                        if (xml.getAttributeValue(ANDROID_NS, "key") != null) {
                            lastPrefSub = xml.getAttributeValue(ANDROID_NS, "key");
                            lastPrefSubTitle = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"));
                            lastPrefSubSubTitle = null;
                            order = 1;
                        } else {
                            lastPrefSubSubTitle = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"));
                            order++;
                        }
                        eventType = xml.next();
                        continue;
                    }

                    boolean isChild = xml.getAttributeBooleanValue(MIUIZER_NS, "child", false);
                    if (!isChild) {
                        ModData modData = new ModData();
                        modData.title = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"));
                        if (modData.title != null) {
                            modData.breadcrumbs = res.getString(catResId) + (lastPrefSubTitle == null ? "" : ("/" + lastPrefSubTitle + (lastPrefSubSubTitle == null ? "" : "/" + lastPrefSubSubTitle)));
                            modData.key = xml.getAttributeValue(ANDROID_NS, "key");
                            modData.cat = catPrefKey;
                            modData.sub = lastPrefSub;
                            modData.order = order;
                            allModsList.add(modData);
                            //Log.e("miuizer", modData.key + " = " + modData.order);
                        }
                    }
                    order++;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                eventType = xml.next();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void getAllMods(Context context, boolean force) {
        if (force) allModsList.clear();
        else if (allModsList.size() > 0) return;
        parsePrefXml(context, R.xml.prefs_system);
        parsePrefXml(context, R.xml.prefs_launcher);
        parsePrefXml(context, R.xml.prefs_controls);
        parsePrefXml(context, R.xml.prefs_various);
    }

    public static void performLightVibration(Context context) {
        performLightVibration(context, false);
    }

    public static void performLightVibration(Context context, boolean ignoreOff) {
        performVibration(context, false, ignoreOff);
    }

    public static void performStrongVibration(Context context) {
        performVibration(context, true, false);
    }

    public static void performStrongVibration(Context context, boolean ignoreOff) {
        performVibration(context, true, ignoreOff);
    }

    public static void performVibration(Context context, boolean isStrong, boolean ignoreOff) {
        if (context == null) return;
        HapticFeedbackUtil mHapticFeedbackUtil = new HapticFeedbackUtil(context, false);
        mHapticFeedbackUtil.performHapticFeedback(isStrong ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.VIRTUAL_KEY, ignoreOff);
    }

    public static void performCustomVibration(Context context, int vibration, String ownPattern) {
        if (vibration == 0) return;
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;
        long[] pattern = new long[0];
        switch (vibration) {
            case 1: vibrator.vibrate(200); return;
            case 2: vibrator.vibrate(400); return;
            case 3: pattern = new long[] { 0, 250, 250, 250 }; break;
            case 4: pattern = new long[] { 0, 250, 150, 125, 100, 125 }; break;
            case 5: pattern = new long[] { 0, 150, 150, 100, 250, 150, 150, 100 }; break;
            case 6: pattern = new long[] { 0, 100, 150, 100, 150, 100 }; break;
            case 7:
                if (TextUtils.isEmpty(ownPattern)) return;
                pattern = getVibrationPattern(ownPattern);
                break;
        }
        try {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } catch (Throwable t) {
            //noinspection deprecation
            vibrator.vibrate(200);
        }
    }

    public static long[] getVibrationPattern(String patternStr) {
        try {
            if (TextUtils.isEmpty(patternStr)) return new long[0];
            String[] sPattern = patternStr.split(",");
            long[] pattern = new long[sPattern.length];
            for (int i = 0; i < sPattern.length; i++)
                pattern[i] = TextUtils.isEmpty(sPattern[i]) ? 0 : Long.parseLong(sPattern[i]);
            return pattern;
        } catch (Throwable t) {
            return new long[0];
        }
    }

//	public static void removePref(PreferenceFragmentBase frag, String prefName, String catName) {
//		if (frag.findPreference(prefName) != null) {
//			Preference cat = frag.findPreference(catName);
//			if (cat instanceof PreferenceScreen) ((PreferenceScreen)cat).removePreference(frag.findPreference(prefName));
//			else if (cat instanceof PreferenceCategory) ((PreferenceCategory)cat).removePreference(frag.findPreference(prefName));
//		}
//	}
//
//	public static void disablePref(PreferenceFragmentBase frag, String prefName, String reasonText) {
//		Preference pref = frag.findPreference(prefName);
//		if (pref != null) {
//			pref.setEnabled(false);
//			pref.setSummary(reasonText);
//		}
//	}

    public static String getCacheFilePath(String filename) {
        if (new File("/cache").canWrite()) return "/cache/" + filename;
        else if (new File("/data/cache").canWrite()) return "/data/cache/" + filename;
        else if (new File("/data/tmp").canWrite()) return "/data/tmp/" + filename;
        else return null;
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("", text);
        clipboard.setPrimaryClip(mClipData);
    }

    public static boolean copyFile(String from, String to) {
        try {
            Files.copy(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static boolean containsStringPair(Set<String> hayStack, String needle) {
        boolean res = false;
        if (hayStack == null || hayStack.size() == 0) return false;
        for (String pair: hayStack) {
            String[] needles = pair.split("\\|");
            if (needles[0].equalsIgnoreCase(needle)) {
                res = true;
                break;
            }
        }
        return res;
    }

    public static Bitmap fastBlur(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) return null;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int[] vmin = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) dv[i] = (i / divsum);

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {
                if (rsum < dv.length) r[yi] = dv[rsum];
                if (gsum < dv.length) g[yi] = dv[gsum];
                if (bsum < dv.length) b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return bitmap;
    }

    public static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
    public static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }
    public static float lerp(float start, float stop, float amount) {
        return start + (stop - start) * amount;
    }
    public static float lerp(int start, int stop, float amount) {
        return lerp((float) start, (float) stop, amount);
    }

    /**
     * Returns the interpolation scalar (s) that satisfies the equation: {@code value = }{@link
     * #lerp}{@code (a, b, s)}
     *
     * <p>If {@code a == b}, then this function will return 0.
     */
    public static float lerpInv(float a, float b, float value) {
        return a != b ? ((value - a) / (b - a)) : 0.0f;
    }
    /** Returns the single argument constrained between [0.0, 1.0]. */
    public static float saturate(float value) {
        return constrain(value, 0.0f, 1.0f);
    }
    /** Returns the saturated (constrained between [0, 1]) result of {@link #lerpInv}. */
    public static float lerpInvSat(float a, float b, float value) {
        return saturate(lerpInv(a, b, value));
    }
    public static float norm(float start, float stop, float value) {
        return (value - start) / (stop - start);
    }
    private static float sq(float f) {
        return f * f;
    }
    public static float exp(float f) {
        return (float)Math.exp(f);
    }

    public static final float convertGammaToLinearFloat(float i, int max, float f, float f2) {
        float norm = norm(0.0f, max, i);
        float R = 0.4f;
        float A = 0.2146f;
        float B = 0.2847f;
        float C = 0.4719f;
        return lerp(f, f2, constrain(norm <= R ? sq(norm / R) : exp((norm - C) / A) + B, 0.0f, 12.0f) / 12.0f);
    }

}