package name.mikanoshi.customiuizer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import de.robv.android.xposed.XposedBridge;
import name.mikanoshi.customiuizer.BuildConfig;
import name.mikanoshi.customiuizer.MainActivity;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.PreferenceFragmentBase;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SharedPrefsProvider;

public class Helpers {

	public static final String modulePkg = "name.mikanoshi.customiuizer";
	public static final String prefsName = "customiuizer_prefs";
	public static SharedPreferences prefs = null;
	public static ArrayList<AppData> installedAppsList = null;
	public static ArrayList<AppData> launchableAppsList = null;
	public static float strings_total = 806.0f;
	public static int buildVersion = 289;
	public static String dataPath;
	public static String externalPath = null;
	public static String backupFile = "settings_backup";
	public static LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>((int)(Runtime.getRuntime().maxMemory() / 1024) / 2) {
		@Override
		protected int sizeOf(String key, Bitmap icon) {
			if (icon != null)
				return icon.getAllocationByteCount() / 1024;
			else
				return 130 * 130 * 4 / 1024;
		}
	};
	public static ArrayList<Integer> allStyles;
	public static SparseArray<Object[]> colors = new SparseArray<Object[]>();
	//public static int mFlashlightLevel = 0;
	public static WakeLock mWakeLock;
	public static LinkedHashMap<String, Integer> colorValues = new LinkedHashMap<String, Integer>();
	public static LinkedHashMap<String, Integer> colorValuesHeader = new LinkedHashMap<String, Integer>();

	public enum SettingsType {
		Preference, Edit
	}

	public enum ActionBarType {
		HomeUp, Edit
	}

	private static ArrayList<Preference> getPreferenceList(Preference p, ArrayList<Preference> list) {
		if (p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
			PreferenceGroup pGroup = (PreferenceGroup) p;
			int pCount = pGroup.getPreferenceCount();
			for (int i = 0; i < pCount; i++)
			getPreferenceList(pGroup.getPreference(i), list);
		}
		list.add(p);
		return list;
	}
	
	private static String titleResName2EntriesResName(String titleResName, int titleResId) {
		String entriesResName;
		if (titleResName.equals("controls_vol_up_media_title") || titleResName.equals("controls_vol_down_media_title"))
			entriesResName = "media_action";
		else if (titleResName.equals("controls_vol_up_cam_title") || titleResName.equals("controls_vol_down_cam_title"))
			entriesResName = "cam_actions";
		else if (titleResName.equals("various_popupnotify_clock_title"))
			entriesResName = "various_clock_style";
		else if (titleResName.equals("various_popupnotify_back_title"))
			entriesResName = "various_background_style";
		else if (titleResName.equals("controls_extendedpanel_left_title") || titleResName.equals("controls_extendedpanel_right_title"))
			entriesResName = "extendedpanel_actions";
		else if (titleResName.equals("sense_gappwidget_title"))
			entriesResName = "googleapp_widget";
		else if (titleResName.equals("sense_transitions_title"))
			entriesResName = "transitions";
		else if (titleResName.equals("toolbox_material_background_title"))
			entriesResName = "material_background";
		else if (titleResName.contains("controls_headsetonaction") || titleResName.contains("controls_headsetoffaction"))
			entriesResName = "audio_actions";
		else if (titleResName.contains("controls_clockaction"))
			entriesResName = "clock_actions";
		else if (titleResName.contains("controls_dateaction"))
			entriesResName = "date_actions";
		else if (titleResName.contains("controls_headsetoneffect") || titleResName.contains("controls_headsetoffeffect"))
			entriesResName = "global_effects";
		else if (titleResName.contains("sense_") || titleResName.contains("controls_"))
			entriesResName = "global_actions";
		else if (titleResName.contains("wakegestures_"))
			entriesResName = "wakegest_actions";
//		else if (titleResId == R.string.array_global_actions_toggle)
//			entriesResName = "global_toggles";
		else
			entriesResName = titleResName.replace("_title", "");
		return entriesResName;
	}

//	public void updateSummary(Preference p) {
//		if (p instanceof ListPreference) {
//			ListPreference listPref = (ListPreference)p;
//			listPref.setSummary(listPref.getEntry());
//		} else if (p instanceof EditTextPreference) {
//			EditTextPreference editTextPref = (EditTextPreference)p;
//			if (editTextPref.getTitle().toString().toLowerCase().contains("password"))
//				editTextPref.setSummary("******");
//			else
//				editTextPref.setSummary(editTextPref.getText());
//		} else if (p instanceof MultiSelectListPreference) {
//			EditTextPreference editTextPref = (EditTextPreference)p;
//			editTextPref.setSummary(editTextPref.getText());
//		}
//	}

//	public static void applyLang(Activity act, PreferenceFragmentBase frag) {
//		ArrayList<Preference> list = getPreferenceList(frag.getPreferenceScreen(), new ArrayList<Preference>());
//
//		for (Preference p: list) {
//			int titleResId = p.getTitleRes();
//			if (titleResId == 0) continue;
//			p.setTitle(l10n(act, titleResId));
//
//			CharSequence summ = p.getSummary();
//
//			if (p.getClass() == ListPreferenceEx.class) {
//				ListPreferenceEx listPref = (ListPreferenceEx)p;
//				if (listPref.entriedRes == 0) continue;
//				TypedArray ar = act.getResources().obtainTypedArray(listPref.entriedRes);
//				int len = ar.length();
//				int[] resIds = new int[len];
//				for (int i = 0; i < len; i++)
//				resIds[i] = ar.getResourceId(i, 0);
//				ar.recycle();
//
//				int valPos = listPref.findIndexOfValue(listPref.getValue());
//				if (valPos < resIds.length)
//				p.setSummary(l10n(act, resIds[valPos]));
//			} else if (summ != null && summ != "") {
//				if (titleResId == R.string.array_global_actions_launch || titleResId == R.string.array_global_actions_toggle) {
//					p.setSummary(l10n(act, "notselected"));
//				} else {
//					String titleResName = act.getResources().getResourceEntryName(titleResId);
//					String summResName = titleResName.replace("_title", "_summ");
//					p.setSummary(l10n(act, summResName));
//				}
//			}
//
//			if (p.getClass() == ListPreference.class /*|| p.getClass() == ListPreferenceEx.class || p.getClass() == ImageListPreference.class*/ || p.getClass() == MultiSelectListPreference.class) {
//				String titleResName = act.getResources().getResourceEntryName(titleResId);
//				String entriesResName = titleResName2EntriesResName(titleResName, titleResId);
//				int arrayId = act.getResources().getIdentifier(entriesResName, "array", act.getPackageName());
//				if (arrayId != 0) {
//					TypedArray ids = act.getResources().obtainTypedArray(arrayId);
//					List<String> newEntries = new ArrayList<String>();
//					for (int i = 0; i < ids.length(); i++) {
//						int id = ids.getResourceId(i, 0);
//						if (id != 0)
//							newEntries.add(l10n(act, id));
//						else
//							newEntries.add("???");
//					}
//					ids.recycle();
//
//					if (p.getClass() == MultiSelectListPreference.class) {
//						MultiSelectListPreference lst = ((MultiSelectListPreference)p);
//						lst.setEntries(newEntries.toArray(new CharSequence[newEntries.size()]));
//						lst.setDialogTitle(l10n(act, titleResId));
//					} else {
//						ListPreference lst = ((ListPreference)p);
//						lst.setEntries(newEntries.toArray(new CharSequence[newEntries.size()]));
//						lst.setDialogTitle(l10n(act, titleResId));
//					}
//				}
//			}
//		}
//	}

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

	public static int getThemePrimaryTextColor(Context mContext) {
		TypedValue tv = new TypedValue();
		mContext.getTheme().resolveAttribute(mContext.getResources().getIdentifier("colorPrimaryDark", "attr", "android"), tv, true);
		return tv.data;
	}
	
	public static void themeNumberPicker(NumberPicker picker) {
		if (picker == null) return;
		Context mContext = picker.getContext();
		float density = mContext.getResources().getDisplayMetrics().density;
		
		java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
		for (java.lang.reflect.Field pf : pickerFields)
		if (pf.getName().equals("mSelectionDivider")) try {
			pf.setAccessible(true);
			TypedValue typedValue = new TypedValue();
			mContext.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
			pf.set(picker, new ColorDrawable(typedValue.data));
			break;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		int count = picker.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = picker.getChildAt(i);
			if (child != null) try {
				if (child instanceof EditText) {
					int mThemeBackground = Integer.parseInt(prefs.getString("pref_key_miuizer_material_background", "1"));
					int sColor = mContext.getResources().getColor(R.color.material_text_secondary_dark);
					Field selectorWheelPaintField = picker.getClass().getDeclaredField("mSelectorWheelPaint");
					selectorWheelPaintField.setAccessible(true);
					if (mThemeBackground == 2)
					((Paint)selectorWheelPaintField.get(picker)).setColor(sColor);
					((Paint)selectorWheelPaintField.get(picker)).setTextSize(density * 20f);
					
					EditText txt = ((EditText)child);
					txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					txt.setFocusable(false);
					txt.setHighlightColor(Color.TRANSPARENT);
					if (mThemeBackground == 2)
					txt.setTextColor(sColor);
				}
				picker.invalidate();
			} catch(Throwable e){
				e.printStackTrace();
			}
		}
	}

	public static BitmapDrawable applyMaterialTheme(Context context, Drawable img) {
		return applyMaterialTheme(context, img, false);
	}
	
	public static BitmapDrawable applyMaterialTheme(Context context, Drawable img, boolean makeWhite) {
		int accent_color;
		if (makeWhite) {
			accent_color = 0xd9ffffff;
		} else {
			TypedValue typedValue = new TypedValue();
			context.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
			accent_color = typedValue.data;
		}
		
		TypedValue typedValue2 = new TypedValue();
		context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue2, true);
		int header_color = typedValue2.data;
		
		if ((context.getClass() == MainActivity.class) && accent_color == header_color) accent_color = 0xd9ffffff;
		
		Bitmap src = ((BitmapDrawable)img).getBitmap();
		Bitmap bitmap = src.copy(Bitmap.Config.ARGB_8888, true);
		return new BitmapDrawable(context.getResources(), shiftRGB(bitmap, accent_color));
	}

	public static Bitmap shiftRGB(Bitmap input, int reqColor) {
		int outR = Color.red(reqColor);
		int outG = Color.green(reqColor);
		int outB = Color.blue(reqColor);

		int w = input.getWidth();
		int h = input.getHeight();

		int[] pix = new int[w * h];
		input.getPixels(pix, 0, w, 0, 0, w, h);

		for (int i = 0; i < w * h; i++) {
			int pixColor = pix[i];
			int curR = Color.red(pixColor);
			int curG = Color.green(pixColor);
			int curB = Color.blue(pixColor);

			int deltaR = Math.abs(80 - curR);
			int deltaG = Math.abs(144 - curG);
			int deltaB = Math.abs(154 - curB);

			if (deltaR < 60 && deltaG < 60 && deltaB < 60) {
				int newR = outR - Math.round(deltaR / 3);
				int newG = outG - Math.round(deltaG / 3);
				int newB = outB - Math.round(deltaB / 3);

				if (newR < 0) newR = 0;
				if (newG < 0) newG = 0;
				if (newB < 0) newB = 0;

				pix[i] = Color.argb(Color.alpha(pixColor), newR, newG, newB);
			}
		}

		input.setPixels(pix, 0, w, 0, 0, w, h);
		return input;
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
	
	public static boolean preparePathForBackup(Activity act, String path) {
		if (act.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			act.requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
			return false;
		}

		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			showOKDialog(act, R.string.warning, R.string.storage_read_only);
			return false;
		} else if (state.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(path);
			if (!file.exists() && !file.mkdirs()) {
				showOKDialog(act, R.string.warning, R.string.storage_cannot_mkdir);
				return false;
			}
			return true;
		} else {
			showOKDialog(act, R.string.warning, R.string.storage_unavailable);
			return false;
		}
	}
	
	public static boolean preparePathSilently(String path) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) return false; else
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(path);
			if (!file.exists() && !file.mkdirs()) return false;
			return true;
		} else return false;
	}
	
	public static void emptyFile(String pathToFile, boolean forceClear) {
		File f = new File(pathToFile);
		if (f.exists() && (f.length() > 150 * 1024 || forceClear)) {
			Log.i("ST", "Clearing uncaught exceptions log...");
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
			if (app.actName != null)
				app.label = (String)pack.activityInfo.loadLabel(pm);
			else
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
		ApplicationInfo ai = null;

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
	
	public static void removePref(PreferenceFragmentBase frag, String prefName, String catName) {
		if (frag.findPreference(prefName) != null) {
			Preference cat = frag.findPreference(catName);
			if (cat instanceof PreferenceScreen) ((PreferenceScreen)cat).removePreference(frag.findPreference(prefName));
			else if (cat instanceof PreferenceCategory) ((PreferenceCategory)cat).removePreference(frag.findPreference(prefName));
		}
	}
	
	public static void disablePref(PreferenceFragmentBase frag, String prefName, String reasonText) {
		Preference pref = frag.findPreference(prefName);
		if (pref != null) {
			pref.setEnabled(false);
			pref.setSummary(reasonText);
		}
	}

	public static synchronized Resources getModuleRes(Context context) throws Throwable {
		Configuration config = context.getResources().getConfiguration();
		Context moduleContext = context.createPackageContext(modulePkg, Context.CONTEXT_IGNORE_SECURITY);
		return (config == null ? moduleContext.getResources() : moduleContext.createConfigurationContext(config).getResources());
	}

	public static Uri stringPrefToUri(String name, String defValue) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/string/" + name + "/" + defValue);
	}

	public static Uri intPrefToUri(String name, int defValue) {
		return Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/integer/" + name + "/" + String.valueOf(defValue));
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
	static {
		// Sense
		colorValues.put("sense_1", 0xff4ea770);
		colorValues.put("sense_2", 0xff00afab);
		colorValues.put("sense_3", 0xff07b7b9);
		colorValues.put("sense_4", 0xff118b9c);
		
		colorValues.put("sense_5", 0xff17b7cd);
		colorValues.put("sense_6", 0xff62b1bd);
		colorValues.put("sense_7", 0xff0091b3);
		colorValues.put("sense_8", 0xff0086cb);
		
		colorValues.put("sense_9", 0xff3786e6);
		colorValues.put("sense_10", 0xff0761b9);
		colorValues.put("sense_11", 0xff255999);
		colorValues.put("sense_12", 0xff6658cf);
		
		colorValues.put("sense_13", 0xffa325a3);
		colorValues.put("sense_14", 0xffffa63d);
		colorValues.put("sense_15", 0xffff813d);
		colorValues.put("sense_16", 0xffff5d3d);
		
		colorValues.put("sense_17", 0xffff7376);
		colorValues.put("sense_18", 0xffff647e);
		colorValues.put("sense_19", 0xfffe4a5d);
		colorValues.put("sense_20", 0xffe74457);
		
		colorValues.put("sense_21", 0xfff64541);
		colorValues.put("sense_22", 0xffd0343a);
		colorValues.put("sense_23", 0xfff3cbb1);
		colorValues.put("sense_24", 0xfff1b08e);
		
		colorValues.put("sense_25", 0xffc9a892);
		colorValues.put("sense_26", 0xffce9374);
		colorValues.put("sense_27", 0xffd1a194);
		colorValues.put("sense_28", 0xffa57f74);
		
		colorValuesHeader.putAll(colorValues);
		
		// Material
		colorValues.put("red_50", 0xffFFEBEE);
		colorValues.put("red_100", 0xFFFFCDD2);
		colorValues.put("red_200", 0xFFEF9A9A);
		colorValues.put("red_300", 0xFFE57373);
		colorValues.put("red_400", 0xFFEF5350);
		colorValues.put("red_500", 0xFFF44336);
		colorValuesHeader.put("red_500", 0xFFF44336);
		colorValues.put("red_600", 0xFFE53935);
		colorValues.put("red_700", 0xFFD32F2F);
		colorValues.put("red_800", 0xFFC62828);
		colorValues.put("red_900", 0xFFB71C1C);
		colorValues.put("red_A100", 0xFFFF8A80);
		colorValues.put("red_A200", 0xFFFF5252);
		colorValues.put("red_A400", 0xFFFF1744);
		colorValues.put("red_A700", 0xFFD50000);

		colorValues.put("pink_50", 0xFFFCE4EC);
		colorValues.put("pink_100", 0xFFF8BBD0);
		colorValues.put("pink_200", 0xFFF48FB1);
		colorValues.put("pink_300", 0xFFF06292);
		colorValues.put("pink_400", 0xFFEC407A);
		colorValues.put("pink_500", 0xFFE91E63);
		colorValuesHeader.put("pink_500", 0xFFE91E63);
		colorValues.put("pink_600", 0xFFD81B60);
		colorValues.put("pink_700", 0xFFC2185B);
		colorValues.put("pink_800", 0xFFAD1457);
		colorValues.put("pink_900", 0xFF880E4F);
		colorValues.put("pink_A100", 0xFFFF80AB);
		colorValues.put("pink_A200", 0xFFFF4081);
		colorValues.put("pink_A400", 0xFFF50057);
		colorValues.put("pink_A700", 0xFFC51162);

		colorValues.put("purple_50", 0xFFF3E5F5);
		colorValues.put("purple_100", 0xFFE1BEE7);
		colorValues.put("purple_200", 0xFFCE93D8);
		colorValues.put("purple_300", 0xFFBA68C8);
		colorValues.put("purple_400", 0xFFAB47BC);
		colorValues.put("purple_500", 0xFF9C27B0);
		colorValuesHeader.put("purple_500", 0xFF9C27B0);
		colorValues.put("purple_600", 0xFF8E24AA);
		colorValues.put("purple_700", 0xFF7B1FA2);
		colorValues.put("purple_800", 0xFF6A1B9A);
		colorValues.put("purple_900", 0xFF4A148C);
		colorValues.put("purple_A100", 0xFFEA80FC);
		colorValues.put("purple_A200", 0xFFE040FB);
		colorValues.put("purple_A400", 0xFFD500F9);
		colorValues.put("purple_A700", 0xFFAA00FF);

		colorValues.put("deep_purple_50", 0xFFEDE7F6);
		colorValues.put("deep_purple_100", 0xFFD1C4E9);
		colorValues.put("deep_purple_200", 0xFFB39DDB);
		colorValues.put("deep_purple_300", 0xFF9575CD);
		colorValues.put("deep_purple_400", 0xFF7E57C2);
		colorValues.put("deep_purple_500", 0xFF673AB7);
		colorValuesHeader.put("deep_purple_500", 0xFF673AB7);
		colorValues.put("deep_purple_600", 0xFF5E35B1);
		colorValues.put("deep_purple_700", 0xFF512DA8);
		colorValues.put("deep_purple_800", 0xFF4527A0);
		colorValues.put("deep_purple_900", 0xFF311B92);
		colorValues.put("deep_purple_A100", 0xFFB388FF);
		colorValues.put("deep_purple_A200", 0xFF7C4DFF);
		colorValues.put("deep_purple_A400", 0xFF651FFF);
		colorValues.put("deep_purple_A700", 0xFF6200EA);

		colorValues.put("indigo_50", 0xFFE8EAF6);
		colorValues.put("indigo_100", 0xFFC5CAE9);
		colorValues.put("indigo_200", 0xFF9FA8DA);
		colorValues.put("indigo_300", 0xFF7986CB);
		colorValues.put("indigo_400", 0xFF5C6BC0);
		colorValues.put("indigo_500", 0xFF3F51B5);
		colorValuesHeader.put("indigo_500", 0xFF3F51B5);
		colorValues.put("indigo_600", 0xFF3949AB);
		colorValues.put("indigo_700", 0xFF303F9F);
		colorValues.put("indigo_800", 0xFF283593);
		colorValues.put("indigo_900", 0xFF1A237E);
		colorValues.put("indigo_A100", 0xFF8C9EFF);
		colorValues.put("indigo_A200", 0xFF536DFE);
		colorValues.put("indigo_A400", 0xFF3D5AFE);
		colorValues.put("indigo_A700", 0xFF304FFE);

		colorValues.put("blue_50", 0xFFE3F2FD);
		colorValues.put("blue_100", 0xFFBBDEFB);
		colorValues.put("blue_200", 0xFF90CAF9);
		colorValues.put("blue_300", 0xFF64B5F6);
		colorValues.put("blue_400", 0xFF42A5F5);
		colorValues.put("blue_500", 0xFF2196F3);
		colorValuesHeader.put("blue_500", 0xFF2196F3);
		colorValues.put("blue_600", 0xFF1E88E5);
		colorValues.put("blue_700", 0xFF1976D2);
		colorValues.put("blue_800", 0xFF1565C0);
		colorValues.put("blue_900", 0xFF0D47A1);
		colorValues.put("blue_A100", 0xFF82B1FF);
		colorValues.put("blue_A200", 0xFF448AFF);
		colorValues.put("blue_A400", 0xFF2979FF);
		colorValues.put("blue_A700", 0xFF2962FF);

		colorValues.put("light_blue_50", 0xFFE1F5FE);
		colorValues.put("light_blue_100", 0xFFB3E5FC);
		colorValues.put("light_blue_200", 0xFF81D4fA);
		colorValues.put("light_blue_300", 0xFF4fC3F7);
		colorValues.put("light_blue_400", 0xFF29B6FC);
		colorValues.put("light_blue_500", 0xFF03A9F4);
		colorValuesHeader.put("light_blue_500", 0xFF03A9F4);
		colorValues.put("light_blue_600", 0xFF039BE5);
		colorValues.put("light_blue_700", 0xFF0288D1);
		colorValues.put("light_blue_800", 0xFF0277BD);
		colorValues.put("light_blue_900", 0xFF01579B);
		colorValues.put("light_blue_A100", 0xFF80D8FF);
		colorValues.put("light_blue_A200", 0xFF40C4FF);
		colorValues.put("light_blue_A400", 0xFF00B0FF);
		colorValues.put("light_blue_A700", 0xFF0091EA);

		colorValues.put("cyan_50", 0xFFE0F7FA);
		colorValues.put("cyan_100", 0xFFB2EBF2);
		colorValues.put("cyan_200", 0xFF80DEEA);
		colorValues.put("cyan_300", 0xFF4DD0E1);
		colorValues.put("cyan_400", 0xFF26C6DA);
		colorValues.put("cyan_500", 0xFF00BCD4);
		colorValuesHeader.put("cyan_500", 0xFF00BCD4);
		colorValues.put("cyan_600", 0xFF00ACC1);
		colorValues.put("cyan_700", 0xFF0097A7);
		colorValues.put("cyan_800", 0xFF00838F);
		colorValues.put("cyan_900", 0xFF006064);
		colorValues.put("cyan_A100", 0xFF84FFFF);
		colorValues.put("cyan_A200", 0xFF18FFFF);
		colorValues.put("cyan_A400", 0xFF00E5FF);
		colorValues.put("cyan_A700", 0xFF00B8D4);

		colorValues.put("teal_50", 0xFFE0F2F1);
		colorValues.put("teal_100", 0xFFB2DFDB);
		colorValues.put("teal_200", 0xFF80CBC4);
		colorValues.put("teal_300", 0xFF4DB6AC);
		colorValues.put("teal_400", 0xFF26A69A);
		colorValues.put("teal_500", 0xFF009688);
		colorValuesHeader.put("teal_500", 0xFF009688);
		colorValues.put("teal_600", 0xFF00897B);
		colorValues.put("teal_700", 0xFF00796B);
		colorValues.put("teal_800", 0xFF00695C);
		colorValues.put("teal_900", 0xFF004D40);
		colorValues.put("teal_A100", 0xFFA7FFEB);
		colorValues.put("teal_A200", 0xFF64FFDA);
		colorValues.put("teal_A400", 0xFF1DE9B6);
		colorValues.put("teal_A700", 0xFF00BFA5);

		colorValues.put("green_50", 0xFFE8F5E9);
		colorValues.put("green_100", 0xFFC8E6C9);
		colorValues.put("green_200", 0xFFA5D6A7);
		colorValues.put("green_300", 0xFF81C784);
		colorValues.put("green_400", 0xFF66BB6A);
		colorValues.put("green_500", 0xFF4CAF50);
		colorValuesHeader.put("green_500", 0xFF4CAF50);
		colorValues.put("green_600", 0xFF43A047);
		colorValues.put("green_700", 0xFF388E3C);
		colorValues.put("green_800", 0xFF2E7D32);
		colorValues.put("green_900", 0xFF1B5E20);
		colorValues.put("green_A100", 0xFFB9F6CA);
		colorValues.put("green_A200", 0xFF69F0AE);
		colorValues.put("green_A400", 0xFF00E676);
		colorValues.put("green_A700", 0xFF00C853);

		colorValues.put("light_green_50", 0xFFF1F8E9);
		colorValues.put("light_green_100", 0xFFDCEDC8);
		colorValues.put("light_green_200", 0xFFC5E1A5);
		colorValues.put("light_green_300", 0xFFAED581);
		colorValues.put("light_green_400", 0xFF9CCC65);
		colorValues.put("light_green_500", 0xFF8BC34A);
		colorValuesHeader.put("light_green_500", 0xFF8BC34A);
		colorValues.put("light_green_600", 0xFF7CB342);
		colorValues.put("light_green_700", 0xFF689F38);
		colorValues.put("light_green_800", 0xFF558B2F);
		colorValues.put("light_green_900", 0xFF33691E);
		colorValues.put("light_green_A100", 0xFFCCFF90);
		colorValues.put("light_green_A200", 0xFFB2FF59);
		colorValues.put("light_green_A400", 0xFF76FF03);
		colorValues.put("light_green_A700", 0xFF64DD17);

		colorValues.put("lime_50", 0xFFF9FBE7);
		colorValues.put("lime_100", 0xFFF0F4C3);
		colorValues.put("lime_200", 0xFFE6EE9C);
		colorValues.put("lime_300", 0xFFDCE775);
		colorValues.put("lime_400", 0xFFD4E157);
		colorValues.put("lime_500", 0xFFCDDC39);
		colorValuesHeader.put("lime_500", 0xFFCDDC39);
		colorValues.put("lime_600", 0xFFC0CA33);
		colorValues.put("lime_700", 0xFFA4B42B);
		colorValues.put("lime_800", 0xFF9E9D24);
		colorValues.put("lime_900", 0xFF827717);
		colorValues.put("lime_A100", 0xFFF4FF81);
		colorValues.put("lime_A200", 0xFFEEFF41);
		colorValues.put("lime_A400", 0xFFC6FF00);
		colorValues.put("lime_A700", 0xFFAEEA00);

		colorValues.put("yellow_50", 0xFFFFFDE7);
		colorValues.put("yellow_100", 0xFFFFF9C4);
		colorValues.put("yellow_200", 0xFFFFF590);
		colorValues.put("yellow_300", 0xFFFFF176);
		colorValues.put("yellow_400", 0xFFFFEE58);
		colorValues.put("yellow_500", 0xFFFFEB3B);
		colorValuesHeader.put("yellow_500", 0xFFFFEB3B);
		colorValues.put("yellow_600", 0xFFFDD835);
		colorValues.put("yellow_700", 0xFFFBC02D);
		colorValues.put("yellow_800", 0xFFF9A825);
		colorValues.put("yellow_900", 0xFFF57F17);
		colorValues.put("yellow_A100", 0xFFFFFF82);
		colorValues.put("yellow_A200", 0xFFFFFF00);
		colorValues.put("yellow_A400", 0xFFFFEA00);
		colorValues.put("yellow_A700", 0xFFFFD600);

		colorValues.put("amber_50", 0xFFFFF8E1);
		colorValues.put("amber_100", 0xFFFFECB3);
		colorValues.put("amber_200", 0xFFFFE082);
		colorValues.put("amber_300", 0xFFFFD54F);
		colorValues.put("amber_400", 0xFFFFCA28);
		colorValues.put("amber_500", 0xFFFFC107);
		colorValuesHeader.put("amber_500", 0xFFFFC107);
		colorValues.put("amber_600", 0xFFFFB300);
		colorValues.put("amber_700", 0xFFFFA000);
		colorValues.put("amber_800", 0xFFFF8F00);
		colorValues.put("amber_900", 0xFFFF6F00);
		colorValues.put("amber_A100", 0xFFFFE57F);
		colorValues.put("amber_A200", 0xFFFFD740);
		colorValues.put("amber_A400", 0xFFFFC400);
		colorValues.put("amber_A700", 0xFFFFAB00);

		colorValues.put("orange_50", 0xFFFFF3E0);
		colorValues.put("orange_100", 0xFFFFE0B2);
		colorValues.put("orange_200", 0xFFFFCC80);
		colorValues.put("orange_300", 0xFFFFB74D);
		colorValues.put("orange_400", 0xFFFFA726);
		colorValues.put("orange_500", 0xFFFF9800);
		colorValuesHeader.put("orange_500", 0xFFFF9800);
		colorValues.put("orange_600", 0xFFFB8C00);
		colorValues.put("orange_700", 0xFFF57C00);
		colorValues.put("orange_800", 0xFFEF6C00);
		colorValues.put("orange_900", 0xFFE65100);
		colorValues.put("orange_A100", 0xFFFFD180);
		colorValues.put("orange_A200", 0xFFFFAB40);
		colorValues.put("orange_A400", 0xFFFF9100);
		colorValues.put("orange_A700", 0xFFFF6D00);

		colorValues.put("deep_orange_50", 0xFFFBE9A7);
		colorValues.put("deep_orange_100", 0xFFFFCCBC);
		colorValues.put("deep_orange_200", 0xFFFFAB91);
		colorValues.put("deep_orange_300", 0xFFFF8A65);
		colorValues.put("deep_orange_400", 0xFFFF7043);
		colorValues.put("deep_orange_500", 0xFFFF5722);
		colorValuesHeader.put("deep_orange_500", 0xFFFF5722);
		colorValues.put("deep_orange_600", 0xFFF4511E);
		colorValues.put("deep_orange_700", 0xFFE64A19);
		colorValues.put("deep_orange_800", 0xFFD84315);
		colorValues.put("deep_orange_900", 0xFFBF360C);
		colorValues.put("deep_orange_A100", 0xFFFF9E80);
		colorValues.put("deep_orange_A200", 0xFFFF6E40);
		colorValues.put("deep_orange_A400", 0xFFFF3D00);
		colorValues.put("deep_orange_A700", 0xFFDD2600);

		colorValues.put("brown_50", 0xFFEFEBE9);
		colorValues.put("brown_100", 0xFFD7CCC8);
		colorValues.put("brown_200", 0xFFBCAAA4);
		colorValues.put("brown_300", 0xFFA1887F);
		colorValues.put("brown_400", 0xFF8D6E63);
		colorValues.put("brown_500", 0xFF795548);
		colorValuesHeader.put("brown_500", 0xFF795548);
		colorValues.put("brown_600", 0xFF6D4C41);
		colorValues.put("brown_700", 0xFF5D4037);
		colorValues.put("brown_800", 0xFF4E342E);
		colorValues.put("brown_900", 0xFF3E2723);

		colorValues.put("grey_50", 0xFFFAFAFA);
		colorValues.put("grey_100", 0xFFF5F5F5);
		colorValues.put("grey_200", 0xFFEEEEEE);
		colorValues.put("grey_300", 0xFFE0E0E0);
		colorValues.put("grey_400", 0xFFBDBDBD);
		colorValues.put("grey_500", 0xFF9E9E9E);
		colorValuesHeader.put("grey_500", 0xFF9E9E9E);
		colorValues.put("grey_600", 0xFF757575);
		colorValues.put("grey_700", 0xFF616161);
		colorValues.put("grey_800", 0xFF424242);
		colorValues.put("grey_900", 0xFF212121);

		colorValues.put("blue_grey_50", 0xFFECEFF1);
		colorValues.put("blue_grey_100", 0xFFCFD8DC);
		colorValues.put("blue_grey_200", 0xFFB0BBC5);
		colorValues.put("blue_grey_300", 0xFF90A4AE);
		colorValues.put("blue_grey_400", 0xFF78909C);
		colorValues.put("blue_grey_500", 0xFF607D8B);
		colorValuesHeader.put("blue_grey_500", 0xFF607D8B);
		colorValues.put("blue_grey_600", 0xFF546E7A);
		colorValues.put("blue_grey_700", 0xFF455A64);
		colorValues.put("blue_grey_800", 0xFF37474F);
		colorValues.put("blue_grey_900", 0xFF263238);
		
		colorValues.put("white_1000", 0xFFFFFFFF);
		colorValues.put("black_1000", 0xFF000000);
		
		colorValuesHeader.put("grey_sense", 0xFF252525);
	}
}