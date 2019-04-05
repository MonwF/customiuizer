package name.mikanoshi.customiuizer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SharedPrefsProvider;

@SuppressWarnings("WeakerAccess")
public class Helpers {

	public static final String modulePkg = "name.mikanoshi.customiuizer";
	public static final String prefsName = "customiuizer_prefs";
	public static final String externalFolder = "/CustoMIUIzer/";
	public static SharedPreferences prefs = null;
//	public static ArrayList<AppData> installedAppsList = null;
	public static ArrayList<AppData> launchableAppsList = null;
	public static String backupFile = "settings_backup";
	public static final int REQUEST_PERMISSIONS_BACKUP = 1;
	public static final int REQUEST_PERMISSIONS_RESTORE = 2;
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

	public enum SettingsType {
		Preference, Edit
	}

	public enum ActionBarType {
		HomeUp, Edit
	}

	public static boolean isXposedInstallerInstalled(Context mContext) {
		PackageManager pm = mContext.getPackageManager();
		boolean res = false;
		try {
			pm.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES);
			res = true;
		} catch (PackageManager.NameNotFoundException e) {}

		try {
			pm.getPackageInfo("com.solohsu.android.edxp.manager", PackageManager.GET_ACTIVITIES);
			res = true;
		} catch (PackageManager.NameNotFoundException e) {}
		return res;
	}

	public static String getXposedInstallerErrorLog(Context mContext) {
		String baseDir = null;

		PackageManager pm = mContext.getPackageManager();
		try {
			pm.getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/de.robv.android.xposed.installer/";
		} catch (PackageManager.NameNotFoundException e) {}

		try {
			pm.getPackageInfo("com.solohsu.android.edxp.manager", PackageManager.GET_ACTIVITIES);
			baseDir = "/data/user_de/0/com.solohsu.android.edxp.manager/";
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

//	public static void getInstalledApps(Context mContext) {
//		final PackageManager pm = mContext.getPackageManager();
//		List<ApplicationInfo> packs = pm.getInstalledApplications(PackageManager.GET_META_DATA | PackageManager.MATCH_DISABLED_COMPONENTS);
//		installedAppsList = new ArrayList<AppData>();
//		AppData app;
//		for (ApplicationInfo pack: packs) try {
//			app = new AppData();
//			app.enabled = pack.enabled;
//			app.label = pack.loadLabel(pm).toString();
//			app.pkgName = pack.packageName;
//			app.actName = "-";
//			installedAppsList.add(app);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		Collections.sort(installedAppsList, new Comparator<AppData>() {
//			public int compare(AppData app1, AppData app2) {
//				return app1.label.compareToIgnoreCase(app2.label);
//			}
//		});
//	}
	
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
	
	public static CharSequence getAppName(Context mContext, String pkgActName) {
		PackageManager pm = mContext.getPackageManager();
		String not_selected = mContext.getResources().getString(R.string.notselected);
		String[] pkgActArray = pkgActName.split("\\|");
		ApplicationInfo ai;

		if (!pkgActName.equals(not_selected))
		if (pkgActArray.length >= 1 && pkgActArray[0] != null) try {
			if (pkgActArray.length >= 2 && pkgActArray[1] != null && !pkgActArray[1].trim().equals("")) {
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
			Context mContext;
			try {
				mContext = Helpers.getProtectedContext(context);
			} catch (Throwable t) {
				Log.e("prefs", "Failed to fix permissions on protected storage!");
				mContext = context;
			}

			File pkgFolder = mContext.getDataDir();
			if (pkgFolder.exists()) {
				pkgFolder.setExecutable(true, false);
				pkgFolder.setReadable(true, false);
				File sharedPrefsFolder = new File(pkgFolder.getAbsolutePath() + "/shared_prefs");
				if (sharedPrefsFolder.exists()) {
					sharedPrefsFolder.setExecutable(true, false);
					sharedPrefsFolder.setReadable(true, false);
					File f = new File(sharedPrefsFolder.getAbsolutePath() + "/" + Helpers.prefsName + ".xml");
					if (f.exists()) {
						f.setReadable(true, false);
						f.setExecutable(true, false);
						f.setWritable(true, false);
					}
				}
			}
		});
	}

	public static synchronized Context getModuleContext(Context context) throws Throwable {
		return context.createPackageContext(modulePkg, Context.CONTEXT_IGNORE_SECURITY).createDeviceProtectedStorageContext();
	}

	public static synchronized Context getProtectedContext(Context context, Configuration config) throws Throwable {
		Context mContext = context.isDeviceProtectedStorage() ? context : context.createDeviceProtectedStorageContext();
		return (config == null ? mContext : mContext.createConfigurationContext(config));
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

	public static Uri intPrefToUri(String name, int defValue) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/integer/" + name + "/" + String.valueOf(defValue));
	}

	public static Uri boolPrefToUri(String name, boolean defValue) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/boolean/" + name + "/" + (defValue ? '1' : '0'));
	}

	public static String getSharedStringPref(Context context, String name, String defValue) {
		Uri uri = stringPrefToUri(name, defValue);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			String prefValue = cursor.getString(0);
			cursor.close();
			return prefValue;
		} else if (MainModule.pref != null)
			return MainModule.pref.getString(name, defValue);
		else
			return defValue;
	}

	public static int getSharedIntPref(Context context, String name, int defValue) {
		Uri uri = intPrefToUri(name, defValue);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int prefValue = cursor.getInt(0);
			cursor.close();
			return prefValue;
		} else if (MainModule.pref != null)
			return MainModule.pref.getInt(name, defValue);
		else
			return defValue;
	}

	public static boolean getSharedBoolPref(Context context, String name, boolean defValue) {
		Uri uri = boolPrefToUri(name, defValue);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int prefValue = cursor.getInt(0);
			cursor.close();
			return prefValue == 1;
		} else if (MainModule.pref != null)
			return MainModule.pref.getBoolean(name, defValue);
		else
			return defValue;
	}

	public static class SharedPrefObserver extends ContentObserver {

		enum PrefType {
			String, Integer
		}

		PrefType prefType;
		Context ctx;
		String prefName;
		String prefDefValueString;
		int prefDefValueInt;

		public SharedPrefObserver(Context context, Handler handler, String name, String defValue) {
			super(handler);
			ctx = context;
			prefName = name;
			prefType = PrefType.String;
			prefDefValueString = defValue;
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

		void registerObserver() {
			Uri uri = null;
			if (prefType == PrefType.String)
				uri = stringPrefToUri(prefName, prefDefValueString);
			else if (prefType == PrefType.Integer)
				uri = intPrefToUri(prefName, prefDefValueInt);
			if (uri != null) ctx.getContentResolver().registerContentObserver(uri, false, this);
		}

		@Override
		public void onChange(boolean selfChange) {
			if (selfChange) return;
			if (prefType == PrefType.String)
				onChange(prefName, prefDefValueString);
			else if (prefType == PrefType.Integer)
				onChange(prefName, prefDefValueInt);
		}

		public void onChange(String name, String defValue) {}
		public void onChange(String name, int defValue) {}
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