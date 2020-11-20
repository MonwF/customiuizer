package name.mikanoshi.customiuizer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
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
import android.util.Pair;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import miui.app.AlertDialog;
import miui.os.SystemProperties;
import miui.util.HapticFeedbackUtil;

import name.mikanoshi.customiuizer.GateWayLauncher;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SharedPrefsProvider;
import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.prefs.PreferenceCategoryEx;

@SuppressWarnings("WeakerAccess")
public class Helpers {

	@SuppressLint("StaticFieldLeak")
	public static Context mModuleContext = null;
	public static final String modulePkg = "name.mikanoshi.customiuizer";
	public static final String prefsName = "customiuizer_prefs";
	public static final String externalFolder = "/CustoMIUIzer/";
	public static final String backupFile = "settings_backup";
	public static final String logFile = "xposed_log";
	public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	public static final String MIUIZER_NS = "http://schemas.android.com/apk/res-auto";
	public static final String ACCESS_SECURITY_CENTER = "com.miui.securitycenter.permission.ACCESS_SECURITY_CENTER_PROVIDER";
	public static final String NEW_MODS_SEARCH_QUERY = "\uD83C\uDD95";
	public static SharedPreferences prefs = null;
	public static ArrayList<AppData> shareAppsList = null;
	public static ArrayList<AppData> openWithAppsList = null;
	public static ArrayList<AppData> installedAppsList = null;
	public static ArrayList<AppData> launchableAppsList = null;
	public static ArrayList<ModData> allModsList = new ArrayList<ModData>();
	public static int xposedVersion = 0;
	public static final int markColor = Color.rgb(205, 73, 97);
	public static final int markColorVibrant = Color.rgb(222, 45, 73);
	public static final int REQUEST_PERMISSIONS_BACKUP = 1;
	public static final int REQUEST_PERMISSIONS_RESTORE = 2;
	public static final int REQUEST_PERMISSIONS_WIFI = 3;
	public static final int REQUEST_PERMISSIONS_REPORT = 4;
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
	public static ValueAnimator shimmerAnim;
	public static boolean showNewMods = true;
	public static boolean miuizerModuleActive = false;
	public static final HashSet<String> newMods = new HashSet<String>(Arrays.asList(
		"pref_key_system_maxsbicons",
		"pref_key_system_morenotif",
		"pref_key_system_nodarkforce",
		"pref_key_various_collapsemiuititles"
	));
	public static final HashMap<String, String> l10nProgress = new HashMap<String, String>() {{
		put("ru-RU", "100.0%");
		put("zh-CN", "100.0%");
		put("id", "13.3%");
		put("tr", "98.6%");
		put("it", "98.6%");
		put("pt-BR", "97.3%");
		put("fr", "26.0%");
		put("uk-UK", "98.4%");
		put("es", "100.0%");
		put("sk", "3.2%");
		put("cs", "0.0%");
		put("de", "98.6%");
	}};

	public static final ArrayList<String> shortcutIcons = new ArrayList<String>();
	public static Holidays currentHoliday = Holidays.NONE;

	public enum Holidays {
		NONE, NEWYEAR, LUNARNEWYEAR, PANDEMIC
	}

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

	public static int getSystemBackgroundColor(Context context) {
		int black = Color.BLACK;
		int white = Color.WHITE;
		try {
			black = context.getResources().getColor(context.getResources().getIdentifier("black", "color", "miui"), context.getTheme());
			white = context.getResources().getColor(context.getResources().getIdentifier("white", "color", "miui"), context.getTheme());
		} catch (Throwable ignore) {}
		return isNightMode(context) ? black : white;
	}

	public static void setMiuiTheme(Activity act, int overrideTheme) {
		setMiuiTheme(act, overrideTheme, false);
	}

	public static void setMiuiTheme(Activity act, int overrideTheme, boolean noBackground) {
		int themeResId = 0;
		try { themeResId = act.getResources().getIdentifier("Theme.DayNight", "style", "miui"); } catch (Throwable t) {}
		if (themeResId == 0) themeResId = act.getResources().getIdentifier(isNightMode(act) ? "Theme.Dark" : "Theme.Light", "style", "miui");
		act.setTheme(themeResId);
		if (!Helpers.is11()) act.getTheme().applyStyle(R.style.ActivityAnimation10, true);
		act.getTheme().applyStyle(overrideTheme, true);
		act.getWindow().setBackgroundDrawable(noBackground ? null : new ColorDrawable(getSystemBackgroundColor(act)));
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

	public static void detectHoliday() {
		currentHoliday = Holidays.NONE;
		String opt = Helpers.prefs.getString("pref_key_miuizer_holiday", "0");
		int holiday = Integer.parseInt(opt);
		if (holiday > 0) currentHoliday = Holidays.values()[holiday];
		if (holiday == 0) {
			Calendar cal = Calendar.getInstance();
			int month = cal.get(Calendar.MONTH);
			int monthDay = cal.get(Calendar.DAY_OF_MONTH);
			int year = cal.get(Calendar.YEAR);

			// Lunar NY
			if ((month == 0 && monthDay > 15) || month == 1) currentHoliday = Holidays.LUNARNEWYEAR;
			// NY
			else if (month == 0 || month == 11) currentHoliday = Holidays.NEWYEAR;
			// COVID19
			else if (month >= 2 && month <= 10 && year == 2020) currentHoliday = Holidays.PANDEMIC;
		}
	}

	public static boolean is11() {
		return SystemProperties.getInt("ro.miui.ui.version.code", 8) >= 9;
	}

	public static boolean is12() {
		return SystemProperties.getInt("ro.miui.ui.version.code", 9) >= 10;
	}

	public static boolean isNightMode(Context context) {
		return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
	}

	public static boolean isNougat() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
	}

	public static boolean isPiePlus() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
	}

	public static boolean isQPlus() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
	}

	public static boolean isLauncherIconVisible(Context context) {
		return context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, GateWayLauncher.class)) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
	}

	public static boolean isXposedInstallerInstalled(Context context) {
		PackageManager pm = context.getPackageManager();

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

		try {
			pm.getPackageInfo("me.weishu.exp", PackageManager.GET_ACTIVITIES);
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

	public static boolean areXposedBlacklistsEnabled() {
		File d1 = new File("/data/user_de/0/com.solohsu.android.edxp.manager/conf/blackwhitelist");
		File d2 = new File("/data/user_de/0/org.meowcat.edxposed.manager/conf/blackwhitelist");
		File d3 = new File("/data/user_de/0/de.robv.android.xposed.installer/conf/blackwhitelist");
		return d1.exists() || d2.exists() || d3.exists();
	}

	public static boolean openXposedApp(Context context) {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.solohsu.android.edxp.manager");
		if (intent != null) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		try {
			context.startActivity(intent);
			return true;
		} catch (Throwable e1) {
			intent = context.getPackageManager().getLaunchIntentForPackage("org.meowcat.edxposed.manager");
			if (intent != null) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			try {
				context.startActivity(intent);
				return true;
			} catch (Throwable e2) {
				intent = context.getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
				if (intent != null) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				try {
					context.startActivity(intent);
					return true;
				} catch (Throwable e3) {
					intent =context.getPackageManager().getLaunchIntentForPackage("me.weishu.exp");
					if (intent != null) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					try {
						context.startActivity(intent);
						return true;
					} catch (Throwable e4) {
						Toast.makeText(context, R.string.xposed_not_found, Toast.LENGTH_LONG).show();
					}
				}
			}
			return false;
		}
	}

	public static String getXposedInstallerErrorLog(Context context) {
		String baseDir = null;
		File file;
		PackageManager pm = context.getPackageManager();

		try {
			pm.getPackageInfo("com.solohsu.android.edxp.manager", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/com.solohsu.android.edxp.manager/";
			file = new File(baseDir + "log/all.log");
			if (file.exists()) return baseDir + "log/all.log";
			file = new File(baseDir + "log/error.log");
			if (file.exists()) return baseDir + "log/error.log";
		} catch (Throwable t) {}

		try {
			pm.getPackageInfo("org.meowcat.edxposed.manager", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/org.meowcat.edxposed.manager/";
			file = new File(baseDir + "log/all.log");
			if (file.exists()) return baseDir + "log/all.log";
			file = new File(baseDir + "log/error.log");
			if (file.exists()) return baseDir + "log/error.log";
		} catch (Throwable t) {}

		try {
			pm.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/de.robv.android.xposed.installer/";
			file = new File(baseDir + "log/error.log");
			if (file.exists()) return baseDir + "log/error.log";
		} catch (Throwable t) {}

		if (baseDir == null)
			return null;
		else
			return baseDir + "log/error.log";
	}

	public static boolean isSysAppUpdaterInstalled(Context context) {
		PackageManager pm = context.getPackageManager();
		boolean res = false;
		try {
			pm.getPackageInfo("com.xiaomi.discover", PackageManager.GET_ACTIVITIES);
			res = true;
		} catch (PackageManager.NameNotFoundException e) {}
		return res;
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

	public static void showInputDialog(Context context, final String key, int titleRes, InputCallback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(titleRes);
		final EditText input = new EditText(context);
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
	
	public static boolean checkStorageReadable(Context context) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY) || state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			showOKDialog(context, R.string.warning, R.string.storage_unavailable);
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

	public static long getNextMIUIAlarmTime(Context context) {
		String nextAlarm = Settings.System.getString(context.getContentResolver(), "next_alarm_clock_formatted");
		long nextTime = 0;
		if (!TextUtils.isEmpty(nextAlarm)) try {
			TimeZone timeZone = TimeZone.getTimeZone("UTC");
			SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "EHm" : "Ehma"), Locale.getDefault());
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

	public static long getNextStockAlarmTime(Context context) {
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		if (alarmMgr == null) return 0;
		AlarmManager.AlarmClockInfo aci = alarmMgr.getNextAlarmClock();
		return aci == null ? 0 : aci.getTriggerTime();
	}

	public static void updateNewModsMarking(Context context) {
		updateNewModsMarking(context, Integer.parseInt(prefs.getString("pref_key_miuizer_marknewmods", "2")));
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

	public static void applyShimmer(TextView title) {
		if (title.getPaint().getShader() != null) return;
		int width = title.getResources().getDisplayMetrics().widthPixels;
		Shader shimmer = new LinearGradient(0, 0, width, 0, new int[]{ 0xFF5DA5FF, 0xFF9B8AFB, 0xFFD176F2, 0xFFFE88B2, 0xFFD176F2, 0xFF9B8AFB }, new float[]{ 0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f }, Shader.TileMode.REPEAT);
		Matrix matrix = new Matrix();
		matrix.setTranslate(0, 0);
		shimmer.setLocalMatrix(matrix);
		title.getPaint().setShader(shimmer);

		if (shimmerAnim != null) shimmerAnim.cancel();
		shimmerAnim = ValueAnimator.ofFloat(0, width, width / 1.8f, width * 1.3f);
		shimmerAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				matrix.setTranslate((float)animation.getAnimatedValue(), 0);
				title.getPaint().getShader().setLocalMatrix(matrix);
				title.invalidate();
			}
		});
		shimmerAnim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationEnd(Animator animation) {
				title.getPaint().setShader(null);
				title.invalidate();
			}

			@Override
			public void onAnimationStart(Animator animation) {}

			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationRepeat(Animator animation) {}
		});
		shimmerAnim.setStartDelay(0);
		shimmerAnim.setDuration(10000);
		shimmerAnim.start();
	}

	public static void openURL(Context context, String url) {
		if (context == null) return;
		Intent uriIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		if (uriIntent.resolveActivity(context.getPackageManager()) != null)
			context.startActivity(uriIntent);
		else
			showOKDialog(context, R.string.warning, R.string.no_browser);
	}

	public static void openAppInfo(Context context, String pkg, int user) {
		try {
			Intent intent = new Intent("miui.intent.action.APP_MANAGER_APPLICATION_DETAIL");
			intent.setPackage("com.miui.securitycenter");
			intent.putExtra("package_name", pkg);
			if (user != 0) intent.putExtra("miui.intent.extra.USER_ID", user);
			context.startActivity(intent);
		} catch (Throwable t) {
			try {
				Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setData(Uri.parse("package:" + pkg));
				if (user != 0)
					XposedHelpers.callMethod(context, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
				else
					context.startActivity(intent);
			} catch (Throwable t2) {
				XposedBridge.log(t2);
			}
		}
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
		return context.getPackageManager().checkPermission("android.permission.INTERACT_ACROSS_USERS", Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
	}

	@SuppressWarnings({"JavaReflectionInvocation"})
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

	@SuppressWarnings({"JavaReflectionInvocation"})
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
			} catch (Throwable t) {}
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
			} catch (Throwable t) {}
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
			} catch (Throwable t) {}
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
		mainIntent.setDataAndType(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/test/5"), "*/*");
		mainIntent.putExtra("CustoMIUIzer", true);
		List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL | PackageManager.MATCH_DISABLED_COMPONENTS);

		mainIntent = new Intent();
		mainIntent.setAction(Intent.ACTION_VIEW);
		mainIntent.setData(Uri.parse("https://google.com"));
		mainIntent.putExtra("CustoMIUIzer", true);
		List<ResolveInfo> packs2 = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL | PackageManager.MATCH_DISABLED_COMPONENTS);

		mainIntent = new Intent();
		mainIntent.setAction(Intent.ACTION_VIEW);
		mainIntent.setData(Uri.parse("vnd.youtube:n9AcG0glVu4"));
		mainIntent.putExtra("CustoMIUIzer", true);
		List<ResolveInfo> packs3 = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL | PackageManager.MATCH_DISABLED_COMPONENTS);

		packs.addAll(packs2);
		packs.addAll(packs3);

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
			} catch (Throwable t) {}
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
		String shortcutIconPath = Helpers.getProtectedContext(context).getFilesDir() + "/shortcuts/" + key + "_shortcut.png";
		File shortcutIconFile = new File(shortcutIconPath);
		if (shortcutIconFile.exists())
		shortcutIcon = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(shortcutIconFile.getAbsolutePath()));
		Drawable[] layers = { shortcutIcon };
		LayerDrawable insetShortcutIcon = new LayerDrawable(layers);
		int padding = (int)(5 * context.getResources().getDisplayMetrics().density);
		insetShortcutIcon.setLayerInset(0, padding, padding, padding, padding);
		return insetShortcutIcon;
	}

	public static String getActionName(Context context, String key) {
		try {
			int action = getSharedIntPref(context, key + "_action", 1);
			Resources modRes = getModuleRes(context);
			int resId = GlobalActions.getActionResId(action);
			if (resId != 0)
				return modRes.getString(resId);
			else if (action == 8)
				return (String)getAppName(getModuleContext(context), getSharedStringPref(context, key + "_app", ""), true);
			else if (action == 9)
				return getSharedStringPref(context, key + "_shortcut_name", "");
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
			} else if (action == 20) {
				Context ctx = getModuleContext(context);
				String pref = getSharedStringPref(context, key + "_activity", "");
				String name = (String)getAppName(ctx, pref);
				if (name == null || name.isEmpty()) name = (String)getAppName(ctx, pref, true);
				return name;
			} else
				return null;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	public static Pair<String, String> getActionNameLocal(Context context, String key) {
		try {
			int action = prefs.getInt(key + "_action", 1);
			Resources modRes = context.getResources();
			Pair<String, String> pair = null;
			int resId = GlobalActions.getActionResId(action);
			if (resId != 0)
				pair = new Pair<>(modRes.getString(resId), "");
			else if (action == 8)
				pair = new Pair<>(modRes.getString(R.string.array_global_actions_launch), (String)getAppName(context, prefs.getString(key + "_app", ""), true));
			else if (action == 9)
				pair = new Pair<>(modRes.getString(R.string.array_global_actions_shortcut), prefs.getString(key + "_shortcut_name", ""));
			else if (action == 10) {
				int what = prefs.getInt(key + "_toggle", 0);
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
				String pref = prefs.getString(key + "_activity", "");
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

	public static Drawable getActionImage(Context context, String key) {
		try {
			int action = getSharedIntPref(context, key + "_action", 1);
			Context modCtx = getModuleContext(context);
			if (action == 8)
				return getAppIcon(modCtx, getSharedStringPref(context, key + "_app", ""));
			else if (action == 9)
				return getSharedShortcutIconPref(modCtx, key);
			else if (action == 20)
				return getAppIcon(modCtx, getSharedStringPref(context, key + "_activity", ""), true);
			else
				return null;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	public static Drawable getActionImageLocal(Context context, String key) {
		try {
			int action = prefs.getInt(key + "_action", 1);
			if (action == 8)
				return getAppIcon(context, prefs.getString(key + "_app", ""));
			else if (action == 9)
				return getShortcutIcon(context, key);
			else if (action == 20)
				return getAppIcon(context, prefs.getString(key + "_activity", ""), true);
			else
				return null;
		} catch (Throwable t) {
			XposedBridge.log(t);
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
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG && !PreferenceScreen.class.getSimpleName().equals(xml.getName())) try {
					if (xml.getName().equals(PreferenceCategory.class.getSimpleName()) || xml.getName().equals(PreferenceCategoryEx.class.getCanonicalName())) {
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

					ModData modData = new ModData();
					boolean isChild = xml.getAttributeBooleanValue(MIUIZER_NS, "child", false);
					if (!isChild) {
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

//		int state = 1;
//		int level = 1;
//		try {
//			state = Settings.System.getInt(context.getContentResolver(), "haptic_feedback_enabled");
//			level = Settings.System.getInt(context.getContentResolver(), "haptic_feedback_level");
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//		if (state == 0) return;
//
//		Vibrator v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
//		if (Build.VERSION.SDK_INT >= 26)
//			v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
//		else
//			v.vibrate(30);
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
				pattern = Helpers.getVibrationPattern(ownPattern);
				break;
		}
		if (!Helpers.isNougat()) try {
			vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
		} catch (Throwable t) {
			//noinspection deprecation
			vibrator.vibrate(200);
		} else
			vibrator.vibrate(200);
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
		if (prefs != null) {
			String locale = prefs.getString("pref_key_miuizer_locale", "auto");
			if ("auto".equals(locale) || "1".equals(locale)) return context;
			Configuration config = context.getResources().getConfiguration();
			config.setLocale(Locale.forLanguageTag(locale));
			return context.createConfigurationContext(config);
		} else {
			return context;
		}
	}

	public static synchronized Context getModuleContext(Context context) throws Throwable {
		return getModuleContext(context, null);
	}

	public static synchronized Context getModuleContext(Context context, Configuration config) throws Throwable {
		if (mModuleContext == null)
		mModuleContext = context.createPackageContext(modulePkg, Context.CONTEXT_IGNORE_SECURITY).createDeviceProtectedStorageContext();
		return config == null ? mModuleContext : mModuleContext.createConfigurationContext(config);
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

	public static Uri shortcutIconPrefToUri(String name) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/shortcut_icon/" + name);
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

	public static Drawable getSharedShortcutIconPref(Context context, String name) {
		Uri uri = shortcutIconPrefToUri(name);
		try {
			InputStream inputStream = context.getContentResolver().openInputStream(uri);
			if (inputStream != null && inputStream.available() > 0) {
				Bitmap bmp = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
				if (bmp == null) return null;
				Drawable[] layers = { new BitmapDrawable(context.getResources(), bmp) };
				LayerDrawable insetShortcutIcon = new LayerDrawable(layers);
				int padding = (int)(5 * context.getResources().getDisplayMetrics().density);
				insetShortcutIcon.setLayerInset(0, padding, padding, padding, padding);
				return insetShortcutIcon;
			} else log("ContentResolver", "[" + name + "] InputStream fail: " + inputStream);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		return null;
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

	public static class MethodHook extends XC_MethodHook {
		protected void before(MethodHookParam param) throws Throwable {}
		protected void after(MethodHookParam param) throws Throwable {}

		public MethodHook() {
			super();
		}

		public MethodHook(int priority) {
			super(priority);
		}

		@Override
		final protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			try {
				this.before(param);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		@Override
		final protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			try {
				this.after(param);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}

	public static void hookMethod(Method method, MethodHook callback) {
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

	public static void hookAllConstructors(String className, ClassLoader classLoader, MethodHook callback) {
		try {
			Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
			if (hookClass == null || XposedBridge.hookAllConstructors(hookClass, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + className + " constructor");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void hookAllConstructors(Class<?> hookClass, MethodHook callback) {
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

	public static void hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
		try {
			if (XposedBridge.hookAllMethods(hookClass, methodName, callback).size() == 0)
			log(getCallerMethod(), "Failed to hook " + methodName + " method in " + hookClass.getSimpleName());
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static boolean hookAllMethodsSilently(String className, ClassLoader classLoader, String methodName, XC_MethodHook callback) {
		try {
			Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
			return hookClass != null && XposedBridge.hookAllMethods(hookClass, methodName, callback).size() > 0;
		} catch (Throwable t) {
			return false;
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

	private static float norm(float f, float f2, float f3) {
		return (f3 - f) / (f2 - f);
	}

	private static float lerp(float f, float f2, float f3) {
		return ((f2 - f) * f3) + f;
	}

	private static float sq(float f) {
		return f * f;
	}

	public static float exp(float f) {
		return (float)Math.exp(f);
	}

	public static float sqrt(float f) {
		return (float)Math.sqrt(f);
	}

	public static float log(float f) {
		return (float)Math.log(f);
	}

	public static int convertGammaToLinear(int val, int min, int max) {
		float norm = norm(0.0f, (float)max, (float) val);
		val = Math.round(lerp((float) min, (float) max, (norm <= 0.2f ? sq(norm / 0.2f) : exp((norm - 0.221f) / 0.314f) + 0.06f) / 12.0f));
		return Math.min(val, max);
	}

	public static int convertLinearToGamma(int val, int min, int max) {
		float norm = norm((float) min, (float)max, (float)val) * 12.0f;
		return Math.round(lerp(0.0f, (float)max, norm <= 1.0f ? sqrt(norm) * 0.2f : (log(norm - 0.06f) * 0.314f) + 0.221f));
	}

}