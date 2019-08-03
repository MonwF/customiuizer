package name.mikanoshi.customiuizer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.LruCache;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import miui.util.HapticFeedbackUtil;
import name.mikanoshi.customiuizer.GateWayLauncher;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SharedPrefsProvider;
import name.mikanoshi.customiuizer.prefs.PreferenceCategoryEx;

@SuppressWarnings("WeakerAccess")
public class Helpers {

	public static final String modulePkg = "name.mikanoshi.customiuizer";
	public static final String prefsName = "customiuizer_prefs";
	public static final String externalFolder = "/CustoMIUIzer/";
	public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	public static final String MIUIZER_NS = "http://schemas.android.com/apk/res-auto";
	public static final String ACCESS_SECURITY_CENTER = "com.miui.securitycenter.permission.ACCESS_SECURITY_CENTER_PROVIDER";
	public static SharedPreferences prefs = null;
	public static ArrayList<AppData> shareAppsList = null;
	public static ArrayList<AppData> openWithAppsList = null;
	public static ArrayList<AppData> installedAppsList = null;
	public static ArrayList<AppData> launchableAppsList = null;
	public static ArrayList<ModData> allModsList = new ArrayList<ModData>();
	public static ArrayList<ModData> allSubsList = new ArrayList<ModData>();
	public static String backupFile = "settings_backup";
	public static final int markColor = Color.rgb(205, 73, 97);
	public static final int markColorVibrant = Color.rgb(222, 45, 73);
	public static final int REQUEST_PERMISSIONS_BACKUP = 1;
	public static final int REQUEST_PERMISSIONS_RESTORE = 2;
	public static final int REQUEST_PERMISSIONS_WIFI = 3;
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
	public static final String[] newMods = new String[] {
		"system_statusbaricons_cat",
		"system_batteryindicator_cat",
		"system_magnifier",
		"system_forceclose"
	};

	public enum SettingsType {
		Preference, Edit
	}

	public enum AppAdapterType {
		Default, Standalone, Mutli, CustomTitles, Activities
	}

	public enum ActionBarType {
		HomeUp, Edit
	}

	public static void setMiuiTheme(Activity act, int overrideTheme) {
		int themeResId = 0;
		try {
			themeResId = act.getResources().getIdentifier("Theme.DayNight", "style", "miui");
		} catch (Throwable t) {}
		if (themeResId == 0) themeResId = act.getResources().getIdentifier(isNightMode(act) ? "Theme.Dark" : "Theme.Light", "style", "miui");
		act.setTheme(themeResId);
		act.getTheme().applyStyle(overrideTheme, true);
	}

	public static boolean isNightMode(Context context) {
		return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
	}

	public static boolean isNougat() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
	}

	public static boolean isPiePlus() {
		return Build.VERSION.SDK_INT >=  Build.VERSION_CODES.P;
	}

	public static boolean isLauncherIconVisible(Context context) {
		return context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, GateWayLauncher.class)) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
	}

	public static boolean isXposedInstallerInstalled(Context mContext) {
		PackageManager pm = mContext.getPackageManager();
		try {
			pm.getPackageInfo("com.solohsu.android.edxp.manager", PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {}

		try {
			pm.getPackageInfo("org.meowcat.edxposed.manager", PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {}

		try {
			pm.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {}

		return false;
	}

	public static boolean areXposedResourceHooksDisabled() {
		File d1 = new File("/data/user_de/0/com.solohsu.android.edxp.manager/conf/disable_resources");
		File d2 = new File("/data/user_de/0/org.meowcat.edxposed.manager/conf/disable_resources");
		File d3 = new File("/data/user_de/0/de.robv.android.xposed.installer/conf/disable_resources");
		return d1.exists() || d2.exists() || d3.exists();
	}

	public static String getXposedInstallerErrorLog(Context mContext) {
		String baseDir = null;
		File file;
		PackageManager pm = mContext.getPackageManager();

		try {
			pm.getPackageInfo("com.solohsu.android.edxp.manager", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/com.solohsu.android.edxp.manager/";
			file = new File(baseDir + "log/all.log");
			if (file.exists()) return baseDir + "log/all.log";
			file = new File(baseDir + "log/error.log");
			if (file.exists()) return baseDir + "log/error.log";
		} catch (PackageManager.NameNotFoundException e) {}

		try {
			pm.getPackageInfo("org.meowcat.edxposed.manager", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/org.meowcat.edxposed.manager/";
			file = new File(baseDir + "log/all.log");
			if (file.exists()) return baseDir + "log/all.log";
			file = new File(baseDir + "log/error.log");
			if (file.exists()) return baseDir + "log/error.log";
		} catch (PackageManager.NameNotFoundException e) {}

		try {
			pm.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/de.robv.android.xposed.installer/";
			file = new File(baseDir + "log/error.log");
			if (file.exists()) return baseDir + "log/error.log";
		} catch (PackageManager.NameNotFoundException e) {}

		if (baseDir == null)
			return null;
		else
			return baseDir + "log/error.log";
	}

	public static boolean isSysAppUpdaterInstalled(Context mContext) {
		PackageManager pm = mContext.getPackageManager();
		boolean res = false;
		try {
			pm.getPackageInfo("com.xiaomi.discover", PackageManager.GET_ACTIVITIES);
			res = true;
		} catch (PackageManager.NameNotFoundException e) {}
		return res;
	}

	public static void showOKDialog(Context mContext, int title, int text) {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
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

	public static void showInputDialog(Context mContext, final String key, int titleRes, InputCallback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(titleRes);
		final EditText input = new EditText(mContext);
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
		input.setText(prefs.getString(key, ""));
		builder.setView(input);
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
	
	public static boolean checkStorageReadable(Context mContext) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			showOKDialog(mContext, R.string.warning, R.string.storage_unavailable);
			return false;
		}
	}

	public static boolean checkStoragePerm(Activity act, int action) {
		if (act.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			act.requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, action);
			return false;
		} else return true;
	}

	public static boolean checkCoarsePerm(Activity act, int action) {
		if (act.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			act.requestPermissions(new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION }, action);
			return false;
		} else return true;
	}

	public static boolean checkFinePerm(Activity act, int action) {
		if (act.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			act.requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, action);
			return false;
		} else return true;
	}
	
	public static boolean preparePathForBackup(Activity act, String path) {
		if (!checkStoragePerm(act, REQUEST_PERMISSIONS_BACKUP)) return false;

		String state = Environment.getExternalStorageState();
		switch (state) {
			case Environment.MEDIA_MOUNTED_READ_ONLY:
				showOKDialog(act, R.string.warning, R.string.storage_read_only);
				return false;
			case Environment.MEDIA_MOUNTED:
				File file = new File(path);
				if (!file.exists() && !file.mkdirs()) {
					showOKDialog(act, R.string.warning, R.string.storage_cannot_mkdir);
					return false;
				}
				return true;
			default:
				showOKDialog(act, R.string.warning, R.string.storage_unavailable);
				return false;
		}
	}

	public static void emptyFile(String pathToFile, boolean forceClear) {
		File f = new File(pathToFile);
		if (f.exists() && (f.length() > 150 * 1024 || forceClear)) {
			try (FileOutputStream fOut = new FileOutputStream(f, false)) {
				try (OutputStreamWriter output = new OutputStreamWriter(fOut)) {
					output.write("");
				} catch (Throwable e) {}
			} catch (Throwable e) {}
		}
	}

	public static long getNextMIUIAlarmTime(Context mContext) {
		String nextAlarm = Settings.System.getString(mContext.getContentResolver(), "next_alarm_clock_formatted");
		long nextTime = 0;
		if (!TextUtils.isEmpty(nextAlarm)) try {
			TimeZone timeZone = TimeZone.getTimeZone("UTC");
			SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(mContext) ? "EHm" : "Ehma"), Locale.getDefault());
			dateFormat.setTimeZone(timeZone);
			long nextTimePart = dateFormat.parse(nextAlarm).getTime();

			Calendar cal = Calendar.getInstance(timeZone);
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			cal.setTimeInMillis(nextTimePart);
			int targetDay = cal.get(Calendar.DAY_OF_WEEK);
			int targetHour = cal.get(Calendar.HOUR_OF_DAY);
			int targetMinute = cal.get(Calendar.MINUTE);

			cal = Calendar.getInstance();
			int diff = targetDay - cal.get(Calendar.DAY_OF_WEEK);
			if (diff < 0) diff += 7;

			cal.add(Calendar.DAY_OF_MONTH, diff);
			cal.set(Calendar.HOUR_OF_DAY, targetHour);
			cal.set(Calendar.MINUTE, targetMinute);
			cal.clear(Calendar.SECOND);
			cal.clear(Calendar.MILLISECOND);

			nextTime = cal.getTimeInMillis();
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		return nextTime;
	}

	public static long getNextStockAlarmTime(Context mContext) {
		AlarmManager alarmMgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		AlarmManager.AlarmClockInfo aci = alarmMgr.getNextAlarmClock();
		return aci == null ? 0 : aci.getTriggerTime();
	}

	@SuppressWarnings("ConstantConditions")
	public static void updateNewModsMarking(Context mContext) {
		updateNewModsMarking(mContext, Integer.parseInt(prefs.getString("pref_key_miuizer_marknewmods", "2")));
	}

	public static void updateNewModsMarking(Context mContext, int opt) {
		try {
			ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(Helpers.modulePkg, 0);
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

	public static void openURL(Context mContext, String url) {
		if (mContext == null) return;
		Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		if (uriIntent.resolveActivity(mContext.getPackageManager()) != null)
			mContext.startActivity(uriIntent);
		else
			showOKDialog(mContext, R.string.warning, R.string.no_browser);
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

	public static void getInstalledApps(Context mContext) {
		final PackageManager pm = mContext.getPackageManager();
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Collections.sort(installedAppsList, new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				return app1.label.compareToIgnoreCase(app2.label);
			}
		});
	}
	
	public static void getLaunchableApps(Context mContext) {
		PackageManager pm = mContext.getPackageManager();
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Collections.sort(launchableAppsList, new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				return app1.label.compareToIgnoreCase(app2.label);
			}
		});
	}

	public static void getShareApps(Context mContext) {
		PackageManager pm = mContext.getPackageManager();
		final Intent mainIntent = new Intent();
		mainIntent.setAction(Intent.ACTION_SEND);
		mainIntent.setType("*/*");
		mainIntent.putExtra("CustoMIUIzer", true);
		List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL);
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Collections.sort(shareAppsList, new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				return app1.label.compareToIgnoreCase(app2.label);
			}
		});
	}

	public static void getOpenWithApps(Context mContext) {
		PackageManager pm = mContext.getPackageManager();
		final Intent mainIntent = new Intent();
		mainIntent.setAction(Intent.ACTION_VIEW);
		mainIntent.setDataAndType(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/test/5"), "*/*");
		mainIntent.putExtra("CustoMIUIzer", true);
		List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL);
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Collections.sort(openWithAppsList, new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				return app1.label.compareToIgnoreCase(app2.label);
			}
		});
	}

	public static CharSequence getAppName(Context mContext, String pkgActName) {
		return getAppName(mContext, pkgActName, false);
	}

	public static CharSequence getAppName(Context mContext, String pkgActName, boolean forcePkg) {
		PackageManager pm = mContext.getPackageManager();
		String not_selected = mContext.getResources().getString(R.string.notselected);
		String[] pkgActArray = pkgActName.split("\\|");
		ApplicationInfo ai;

		if (!pkgActName.equals(not_selected))
		if (pkgActArray.length >= 1 && pkgActArray[0] != null) try {
			if (!forcePkg && pkgActArray.length >= 2 && pkgActArray[1] != null && !pkgActArray[1].trim().equals("")) {
				return pm.getActivityInfo(new ComponentName(pkgActArray[0], pkgActArray[1]), 0).loadLabel(pm).toString();
			} else if (!pkgActArray[0].trim().equals("")) {
				ai = pm.getApplicationInfo(pkgActArray[0], 0);
				return (ai != null ? pm.getApplicationLabel(ai) : null);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getActionName(Context context, int action, String key) {
		try {
			Resources modRes = getModuleRes(context);
			if (action == 4)
				return modRes.getString(R.string.array_global_actions_lock);
			else if (action == 5)
				return modRes.getString(R.string.array_global_actions_sleep);
			else if (action == 8)
				return (String)Helpers.getAppName(getModuleContext(context), Helpers.getSharedStringPref(context, key + "_app", ""));
			else if (action == 9)
				return Helpers.getSharedStringPref(context, key + "_shortcut_name", "");
			else if (action == 10) {
				int what = getSharedIntPref(context, key + "_toggle", 0);
				switch (what) {
					case 1: return modRes.getString(R.string.array_global_toggle_wifi);
					case 2: return modRes.getString(R.string.array_global_toggle_bt);
					case 3: return modRes.getString(R.string.array_global_toggle_gps);
					case 4: return modRes.getString(R.string.array_global_toggle_nfc);
					case 5: return modRes.getString(R.string.array_global_toggle_sound);
					case 6: return modRes.getString(R.string.array_global_toggle_brightness);
					case 7: return modRes.getString(R.string.array_global_toggle_rotation);
					case 8: return modRes.getString(R.string.array_global_toggle_torch);
					case 9: return modRes.getString(R.string.array_global_toggle_mobiledata);
					default: return null;
				}
			} else if (action == 12)
				return modRes.getString(R.string.array_global_actions_powermenu_short);
			else if (action == 14)
				return modRes.getString(R.string.array_global_actions_invertcolors);
			else
				return null;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	private static void parsePrefXml(Context mContext, int xmlResId) {
		Resources res = mContext.getResources();
		String lastPrefScreen = null;
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
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) try {
					if (xml.getName().equals(PreferenceCategory.class.getSimpleName()) || xml.getName().equals(PreferenceCategoryEx.class.getCanonicalName())) {
						lastPrefScreen = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"));
						ModData modData = new ModData();
						modData.title = lastPrefScreen;
						modData.key = xml.getAttributeValue(ANDROID_NS, "key");
						modData.cat = catPrefKey;
						modData.order = order - 1;
						allSubsList.add(modData);
						eventType = xml.next();
						order++;
						continue;
					}

					ModData modData = new ModData();
					boolean isChild = xml.getAttributeBooleanValue(MIUIZER_NS, "child", false);
					if (!isChild) {
						modData.title = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"));
						if (modData.title != null) {
							modData.breadcrumbs = res.getString(catResId) + (lastPrefScreen == null ? "" : "/" + lastPrefScreen);
							modData.key = xml.getAttributeValue(ANDROID_NS, "key");
							modData.cat = catPrefKey;
							modData.order = order - 1;
							allModsList.add(modData);
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

	public static void getAllMods(Context mContext) {
		if (allModsList.size() > 0) return;
		parsePrefXml(mContext, R.xml.prefs_system);
		parsePrefXml(mContext, R.xml.prefs_launcher);
		parsePrefXml(mContext, R.xml.prefs_controls);
		parsePrefXml(mContext, R.xml.prefs_various);
	}

	public static void performLightVibration(Context mContext) {
		performLightVibration(mContext, false);
	}

	public static void performLightVibration(Context mContext, boolean ignoreOff) {
		performVibration(mContext, false, ignoreOff);
	}

	public static void performStrongVibration(Context mContext) {
		performVibration(mContext, true, false);
	}

	public static void performStrongVibration(Context mContext, boolean ignoreOff) {
		performVibration(mContext, true, ignoreOff);
	}

	public static void performVibration(Context mContext, boolean isStrong, boolean ignoreOff) {
		if (mContext == null) return;
		HapticFeedbackUtil mHapticFeedbackUtil = new HapticFeedbackUtil(mContext, false);
		mHapticFeedbackUtil.performHapticFeedback(isStrong ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.VIRTUAL_KEY, ignoreOff);

//		int state = 1;
//		int level = 1;
//		try {
//			state = Settings.System.getInt(mContext.getContentResolver(), "haptic_feedback_enabled");
//			level = Settings.System.getInt(mContext.getContentResolver(), "haptic_feedback_level");
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//		if (state == 0) return;
//
//		Vibrator v = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
//		if (Build.VERSION.SDK_INT >= 26)
//			v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
//		else
//			v.vibrate(30);
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
	@SuppressLint({"SetWorldReadable", "SetWorldWritable"})
	public static void fixPermissionsAsync(Context context) {
		AsyncTask.execute(() -> {
			try { Thread.sleep(500); } catch (Throwable t) {}
			File pkgFolder = context.getDataDir();
			if (pkgFolder.exists()) {
				pkgFolder.setExecutable(true, false);
				pkgFolder.setReadable(true, false);
				pkgFolder.setWritable(true, false);
				File sharedPrefsFolder = new File(pkgFolder.getAbsolutePath() + "/shared_prefs");
				if (sharedPrefsFolder.exists()) {
					sharedPrefsFolder.setExecutable(true, false);
					sharedPrefsFolder.setReadable(true, false);
					sharedPrefsFolder.setWritable(true, false);
					File f = new File(sharedPrefsFolder.getAbsolutePath() + "/" + prefsName + ".xml");
					if (f.exists()) {
						f.setReadable(true, false);
						f.setExecutable(true, false);
						f.setWritable(true, false);
					}
				}
			}
		});
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

	public static synchronized Context getLocaleContext(Context context) throws Throwable {
		if (prefs != null && prefs.getBoolean("pref_key_miuizer_forcelocale", false)) {
			Configuration config = context.getResources().getConfiguration();
			config.setLocale(Locale.ENGLISH);
			return context.createConfigurationContext(config);
		} else {
			return context;
		}
	}

	public static synchronized Context getModuleContext(Context context) throws Throwable {
		return context.createPackageContext(modulePkg, Context.CONTEXT_IGNORE_SECURITY).createDeviceProtectedStorageContext();
	}

	public static synchronized Context getProtectedContext(Context context, Configuration config) throws Throwable {
		Context mContext = context.isDeviceProtectedStorage() ? context : context.createDeviceProtectedStorageContext();
		return getLocaleContext(config == null ? mContext : mContext.createConfigurationContext(config));
	}

	public static synchronized Context getProtectedContext(Context context) throws Throwable {
		return getProtectedContext(context, null);
	}

	public static synchronized Resources getModuleRes(Context context) throws Throwable {
		Configuration config = context.getResources().getConfiguration();
		Context moduleContext = getModuleContext(context);
		return (config == null ? moduleContext.getResources() : moduleContext.createConfigurationContext(config).getResources());
	}

	public static Uri stringPrefToUri(String name, String defValue) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/string/" + name + "/" + defValue);
	}

	public static Uri stringSetPrefToUri(String name) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/stringset/" + name);
	}

	public static Uri intPrefToUri(String name, int defValue) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/integer/" + name + "/" + defValue);
	}

	public static Uri boolPrefToUri(String name, boolean defValue) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/boolean/" + name + "/" + (defValue ? '1' : '0'));
	}

	public static Uri anyPrefToUri() {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/pref/");
	}

	public static String getSharedStringPref(Context context, String name, String defValue) {
		Uri uri = stringPrefToUri(name, defValue);
		try {
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String prefValue = cursor.getString(0);
				cursor.close();
				return prefValue;
			} else log("ContentResolver", "[" + name + "] Cursor fail: " + cursor);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		if (MainModule.mPrefs.containsKey(name))
			return (String)MainModule.mPrefs.getObject(name, defValue);
		else
			return defValue;
	}

	@SuppressWarnings("unchecked")
	public static Set<String> getSharedStringSetPref(Context context, String name) {
		Uri uri = stringSetPrefToUri(name);
		try {
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null) {
				Set<String> prefValue = new LinkedHashSet<String>();
				while (cursor.moveToNext()) prefValue.add(cursor.getString(0));
				cursor.close();
				return prefValue;
			} else log("ContentResolver", "[" + name + "] Cursor fail: null");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		LinkedHashSet<String> empty = new LinkedHashSet<String>();
		if (MainModule.mPrefs.containsKey(name))
			return (Set<String>)MainModule.mPrefs.getObject(name, empty);
		else
			return empty;
	}

	public static int getSharedIntPref(Context context, String name, int defValue) {
		Uri uri = intPrefToUri(name, defValue);
		try {
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int prefValue = cursor.getInt(0);
				cursor.close();
				return prefValue;
			} else log("ContentResolver", "[" + name + "] Cursor fail: " + cursor);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		if (MainModule.mPrefs.containsKey(name))
			return (int)MainModule.mPrefs.getObject(name, defValue);
		else
			return defValue;
	}

	public static boolean getSharedBoolPref(Context context, String name, boolean defValue) {
		Uri uri = boolPrefToUri(name, defValue);
		try {
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int prefValue = cursor.getInt(0);
				cursor.close();
				return prefValue == 1;
			} else log("ContentResolver", "[" + name + "] Cursor fail: " + cursor);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		if (MainModule.mPrefs.containsKey(name))
			return (boolean)MainModule.mPrefs.getObject(name, false);
		else
			return defValue;
	}

	public static class SharedPrefObserver extends ContentObserver {

		enum PrefType {
			Any, String, StringSet, Integer//, Boolean
		}

		PrefType prefType;
		Context ctx;
		String prefName;
		String prefDefValueString;
		int prefDefValueInt;
//		boolean prefDefValueBool;

		public SharedPrefObserver(Context context, Handler handler) {
			super(handler);
			ctx = context;
			prefType = PrefType.Any;
			registerObserver();
		}

		public SharedPrefObserver(Context context, Handler handler, String name, String defValue) {
			super(handler);
			ctx = context;
			prefName = name;
			prefType = PrefType.String;
			prefDefValueString = defValue;
			registerObserver();
		}

		public SharedPrefObserver(Context context, Handler handler, String name) {
			super(handler);
			ctx = context;
			prefName = name;
			prefType = PrefType.StringSet;
			registerObserver();
		}

		public SharedPrefObserver(Context context, Handler handler, String name, int defValue) {
			super(handler);
			ctx = context;
			prefType = PrefType.Integer;
			prefName = name;
			prefDefValueInt = defValue;
			registerObserver();
		}

//		public SharedPrefObserver(Context context, Handler handler, String name, boolean defValue) {
//			super(handler);
//			ctx = context;
//			prefType = PrefType.Boolean;
//			prefName = name;
//			prefDefValueBool = defValue;
//			registerObserver();
//		}

		void registerObserver() {
			Uri uri = null;
			if (prefType == PrefType.String)
				uri = stringPrefToUri(prefName, prefDefValueString);
			else if (prefType == PrefType.StringSet)
				uri = stringSetPrefToUri(prefName);
			else if (prefType == PrefType.Integer)
				uri = intPrefToUri(prefName, prefDefValueInt);
//			else if (prefType == PrefType.Boolean)
//				uri = boolPrefToUri(prefName, prefDefValueBool);
			else if (prefType == PrefType.Any)
				uri = anyPrefToUri();
			if (uri != null) ctx.getContentResolver().registerContentObserver(uri, prefType == PrefType.Any, this);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			if (prefType == PrefType.Any)
				onChange(uri);
			else
				onChange(selfChange);
		}

		@Override
		public void onChange(boolean selfChange) {
			if (selfChange) return;
			if (prefType == PrefType.String)
				onChange(prefName, prefDefValueString);
			else if (prefType == PrefType.StringSet)
				onChange(prefName);
			else if (prefType == PrefType.Integer)
				onChange(prefName, prefDefValueInt);
//			else if (prefType == PrefType.Boolean)
//				onChange(prefName, prefDefValueBool);
		}

		public void onChange(Uri uri) {}
		public void onChange(String name) {}
		public void onChange(String name, String defValue) {}
		public void onChange(String name, int defValue) {}
//		public void onChange(String name, boolean defValue) {}
	}

	private static String getCallerMethod() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement el: stackTrace)
		if (el != null && el.getClassName().startsWith(modulePkg + ".mods")) return el.getMethodName();
		return stackTrace[4].getMethodName();
	}

	public static void log(String line) {
		XposedBridge.log("[CustoMIUIzer] " + line);
	}

	public static void log(String mod, String line) {
		XposedBridge.log("[CustoMIUIzer][" + mod + "] " + line);
	}

	public static void hookMethod(Method method, XC_MethodHook callback) {
		try {
			XposedBridge.hookMethod(method, callback);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean findAndHookMethodSilently(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean findAndHookMethodSilently(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	public static void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
		try {
			XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void hookAllConstructors(String className, ClassLoader classLoader, XC_MethodHook callback) {
		try {
			Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
			if (hookClass == null || XposedBridge.hookAllConstructors(hookClass, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + className + " constructor");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
		try {
			if (XposedBridge.hookAllConstructors(hookClass, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + hookClass + " constructor");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void hookAllMethods(String className, ClassLoader classLoader, String methodName, XC_MethodHook callback) {
		try {
			Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
			if (hookClass == null || XposedBridge.hookAllMethods(hookClass, methodName, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + methodName + " method in " + className);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
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

	/*
	public static int[][] cellArray = {
		{ 0, 0, 0 },
		{ R.id.cell1, R.id.cell1img, R.id.cell1txt },
		{ R.id.cell2, R.id.cell2img, R.id.cell2txt },
		{ R.id.cell3, R.id.cell3img, R.id.cell3txt },
		{ R.id.cell4, R.id.cell4img, R.id.cell4txt },
		{ R.id.cell5, R.id.cell5img, R.id.cell5txt },
		{ R.id.cell6, R.id.cell6img, R.id.cell6txt }
	};
*/
}