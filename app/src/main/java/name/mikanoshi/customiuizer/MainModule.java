package name.mikanoshi.customiuizer;

import android.content.Context;
import android.content.res.XModuleResources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static de.robv.android.xposed.XposedHelpers.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.mikanoshi.customiuizer.mods.Launcher;
import name.mikanoshi.customiuizer.utils.GlobalActions;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.PackagePermissions;

public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	public static String MODULE_PATH = null;
	public static XSharedPreferences pref;

	public static int pref_swipedown = 1;
	public static int pref_swipeup = 1;
	public static int pref_swiperight = 1;
	public static int pref_swipeleft = 1;
	public static int pref_shake = 1;

	public static Map<String, String> langForHooks;

	public void initZygote(StartupParam startParam) throws Throwable {
		MODULE_PATH = startParam.modulePath;

		pref = new XSharedPreferences(Helpers.modulePkg, Helpers.prefsName);
		pref.makeWorldReadable();
		pref_swipedown = pref.getInt("pref_key_launcher_swipedown_action", 1);
		pref_swipeup = pref.getInt("pref_key_launcher_swipeup_action", 1);
		pref_swiperight = pref.getInt("pref_key_launcher_swiperight_action", 1);
		pref_swipeleft = pref.getInt("pref_key_launcher_swipeleft_action", 1);
		pref_shake = pref.getInt("pref_key_launcher_shake_action", 1);

		XModuleResources modRes = XModuleResources.createInstance(MainModule.MODULE_PATH, null);
		langForHooks = new HashMap<String, String>();
		langForHooks.put("toggle_wifi_off", modRes.getString(R.string.toggle_wifi_off));
		langForHooks.put("toggle_wifi_on", modRes.getString(R.string.toggle_wifi_on));
		langForHooks.put("toggle_bt_off", modRes.getString(R.string.toggle_bt_off));
		langForHooks.put("toggle_bt_on", modRes.getString(R.string.toggle_bt_on));
		langForHooks.put("toggle_gps_off", modRes.getString(R.string.toggle_gps_off));
		langForHooks.put("toggle_gps_on", modRes.getString(R.string.toggle_gps_on));
		langForHooks.put("toggle_nfc_off", modRes.getString(R.string.toggle_nfc_off));
		langForHooks.put("toggle_nfc_on", modRes.getString(R.string.toggle_nfc_on));
		langForHooks.put("toggle_sound_vibrate", modRes.getString(R.string.toggle_sound_vibrate));
		langForHooks.put("toggle_sound_normal", modRes.getString(R.string.toggle_sound_normal));
		langForHooks.put("toggle_sound_silent", modRes.getString(R.string.toggle_sound_silent));
		langForHooks.put("toggle_autobright_on", modRes.getString(R.string.toggle_autobright_on));
		langForHooks.put("toggle_autobright_off", modRes.getString(R.string.toggle_autobright_off));
		langForHooks.put("toggle_autorotate_on", modRes.getString(R.string.toggle_autorotate_on));
		langForHooks.put("toggle_autorotate_off", modRes.getString(R.string.toggle_autorotate_off));
		langForHooks.put("toggle_flash_on", modRes.getString(R.string.toggle_flash_on));
		langForHooks.put("toggle_flash_off", modRes.getString(R.string.toggle_flash_off));
		langForHooks.put("toggle_mobiledata_on", modRes.getString(R.string.toggle_mobiledata_on));
		langForHooks.put("toggle_mobiledata_off", modRes.getString(R.string.toggle_mobiledata_off));
	}

	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		String pkg = lpparam.packageName;

		if (pkg.equals("android") && lpparam.processName.equals("android")) {
			PackagePermissions.init(lpparam);
			GlobalActions.setupGlobalActions(lpparam);
		}

		if (pkg.equals(Helpers.modulePkg)) {
			GlobalActions.miuizerInit(lpparam);
		}

		if (pkg.equals("com.android.systemui")) {
			GlobalActions.setupStatusBar(lpparam);
		}

		if (pkg.equals("com.android.settings")) {
/*
			findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, Map.class, boolean.class, String.class, String.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[1];
					boolean bool = (boolean)param.args[2];
					String str1 = (String)param.args[3];
					String str2 = (String)param.args[4];

					XposedBridge.log("Tile with Map:");
					XposedBridge.log("Map:");
					for (Map.Entry<Pair<String, String>, Object> entry: map.entrySet())
					XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
					XposedBridge.log("- - - - - - - - - -");
					XposedBridge.log(String.valueOf(bool));
					XposedBridge.log("Str1: " + str1);
					XposedBridge.log("Str2: " + str2);
					XposedBridge.log("- - - - - - - - - -");
				}
			});

			findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, UserHandle.class, String.class, Map.class, String.class, ArrayList.class, boolean.class, boolean.class, String.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					UserHandle uh = (UserHandle)param.args[1];
					String str1 = (String)param.args[2];
					Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[3];
					String str2 = (String)param.args[4];
					ArrayList list = (ArrayList)param.args[5];
					boolean bool1 = (boolean)param.args[6];
					boolean bool2 = (boolean)param.args[7];
					String str3 = (String)param.args[8];

					XposedBridge.log("Tile with ArrayList:");
					XposedBridge.log(uh.toString());
					XposedBridge.log("Str1: " + str1);
					XposedBridge.log("Map:");
					for (Map.Entry<Pair<String, String>, Object> entry: map.entrySet())
					XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
					XposedBridge.log("- - - - - - - - - -");
					XposedBridge.log("Str2: " + str2);
					XposedBridge.log("ArrayList:");
					for (Object entry: list)
					XposedBridge.log(entry.toString());
					XposedBridge.log("- - - - - - - - - -");
					XposedBridge.log(String.valueOf(bool1));
					XposedBridge.log(String.valueOf(bool2));
					XposedBridge.log("Str3: " + str3);
					XposedBridge.log("= = = = = = = = = =");
				}
			});

			findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, UserHandle.class, Intent.class, Map.class, String.class, List.class, boolean.class, boolean.class, boolean.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					UserHandle uh = (UserHandle)param.args[1];
					Intent intent = (Intent)param.args[2];
					Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[3];
					String str = (String)param.args[4];
					List list = (List)param.args[5];
					boolean bool1 = (boolean)param.args[6];
					boolean bool2 = (boolean)param.args[7];
					boolean bool3 = (boolean)param.args[8];
					boolean bool4 = (boolean)param.args[9];

					XposedBridge.log("Tile with List:");
					XposedBridge.log(uh.toString());
					XposedBridge.log(intent.toString());
					XposedBridge.log("Map:");
					for (Map.Entry<Pair<String, String>, Object> entry: map.entrySet())
					XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
					XposedBridge.log("- - - - - - - - - -");
					XposedBridge.log("Str: " + str);
					XposedBridge.log("List:");
					for (Object entry: list)
					XposedBridge.log(entry.toString());
					XposedBridge.log("- - - - - - - - - -");
					XposedBridge.log(String.valueOf(bool1));
					XposedBridge.log(String.valueOf(bool2));
					XposedBridge.log(String.valueOf(bool3));
					XposedBridge.log(String.valueOf(bool4));
					XposedBridge.log("= = = = = = = = = =");
				}
			});

			findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, "com.android.settingslib.drawer.Tile", ActivityInfo.class, ApplicationInfo.class, PackageManager.class, Map.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					ApplicationInfo ai = (ApplicationInfo)param.args[3];
					if (ai.packageName.equalsIgnoreCase(modulePkg) && !(boolean)XposedHelpers.callMethod(ai, "isSystemApp")) ai.flags |= 1;
				}

				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Object tile = param.args[1];
					ActivityInfo ai = (ActivityInfo)param.args[2];
					ApplicationInfo ai2 = (ApplicationInfo)param.args[3];
					Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[5];
					boolean bool = (boolean)param.args[6];

					if (ai2.packageName.equalsIgnoreCase(modulePkg)) {
						XposedBridge.log("Result: " + String.valueOf((boolean)param.getResult()));
						XposedBridge.log("Title: " + XposedHelpers.getObjectField(tile, "title"));
						XposedBridge.log("Summary: " + XposedHelpers.getObjectField(tile, "summary"));
						XposedBridge.log("Cat: " + XposedHelpers.getObjectField(tile, "category"));
						XposedBridge.log("Key: " + XposedHelpers.getObjectField(tile, "key"));
						XposedBridge.log("Priority: " + String.valueOf((int)XposedHelpers.getObjectField(tile, "priority")));
						XposedBridge.log("cCN: " + ((ArrayList)XposedHelpers.getObjectField(tile, "cCN")).toString());
						XposedBridge.log("cCM: " + String.valueOf((boolean)XposedHelpers.getObjectField(tile, "cCM")));
						XposedBridge.log("Icon: " + ((Icon)XposedHelpers.getObjectField(tile, "icon")).toString());
						XposedBridge.log(ai.toString());
						XposedBridge.log(ai2.toString());
						//XposedBridge.log("Map:");
						//for (Map.Entry<Pair<String, String>, Object> entry : map.entrySet())
						//XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
						XposedBridge.log(String.valueOf(bool));
						XposedBridge.log("= = = = = = = = = =");
					}
				}
			});

			findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, Map.class, "com.android.settingslib.drawer.Tile", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Map map = (Map)param.args[1];
					Object tile = (Object)param.args[2];

					XposedBridge.log("Tile:");
					XposedBridge.log(tile.toString());
					XposedBridge.log(XposedHelpers.getObjectField(tile, "title").toString());
					XposedBridge.log(XposedHelpers.getObjectField(tile, "summary").toString());
					XposedBridge.log(String.valueOf((int)XposedHelpers.getObjectField(tile, "priority")));
					XposedBridge.log(map.toString());
					XposedBridge.log("- - - - - - - - - -");
				}
			});
*/
			findAndHookMethod("com.android.settings.MiuiSettings", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					Map<String, Integer> map = (Map<String, Integer>)XposedHelpers.getStaticObjectField(findClass("com.android.settings.MiuiSettings", lpparam.classLoader), "CATEGORY_MAP");
					PreferenceActivity act = (PreferenceActivity)param.thisObject;
					map.put("com.android.settings.category.customiuizer", act.getApplicationContext().getResources().getIdentifier("ic_miui_lab_settings", "drawable", "com.android.settings"));
					XposedHelpers.setStaticObjectField(findClass("com.android.settings.MiuiSettings", lpparam.classLoader), "CATEGORY_MAP", map);
					//XposedBridge.log(map.toString());
				}
			});
		}

		if (pkg.equals("com.miui.home")) {
			if (pref_swipedown != 1 || pref_swipeup != 1)
			Launcher.HomescreenSwipesHook(lpparam);
			if (pref_swipeleft != 1 || pref_swiperight != 1)
			Launcher.HotSeatSwipesHook(lpparam);
			if (pref_shake != 1)
			Launcher.ShakeHook(lpparam);
		}
/*
		if (pkg.equals("com.android.systemui"))
		findAndHookMethod("com.android.keyguard.MiuiKeyguardClock", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
			}
		});
*/
	}
}