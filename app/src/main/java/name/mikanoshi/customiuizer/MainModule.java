package name.mikanoshi.customiuizer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.mikanoshi.customiuizer.mods.Launcher;
import name.mikanoshi.customiuizer.mods.Controls;
import name.mikanoshi.customiuizer.mods.System;
import name.mikanoshi.customiuizer.mods.Various;
import name.mikanoshi.customiuizer.utils.GlobalActions;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.PackagePermissions;

@SuppressWarnings("WeakerAccess")
public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	public static String MODULE_PATH = null;
	public static XSharedPreferences pref;

	public static int pref_swipedown = 1;
	public static int pref_swipeup = 1;
	public static int pref_swipedown2 = 1;
	public static int pref_swipeup2 = 1;
	public static int pref_swiperight = 1;
	public static int pref_swipeleft = 1;
	public static int pref_shake = 1;
	public static int pref_screenanim = 0;
	public static int pref_appsort = 0;
	public static int pref_etoasts = 1;
	public static boolean pref_powerflash = false;
	public static boolean pref_nolightup = false;
	public static boolean pref_appdetails = false;
	public static boolean pref_scramblepin = false;
	public static boolean pref_securelock = false;

	public void initZygote(StartupParam startParam) {
		MODULE_PATH = startParam.modulePath;

		pref = new XSharedPreferences(Helpers.modulePkg, Helpers.prefsName);
		pref.makeWorldReadable();
		pref_swipedown = pref.getInt("pref_key_launcher_swipedown_action", 1);
		pref_swipeup = pref.getInt("pref_key_launcher_swipeup_action", 1);
		pref_swipedown2 = pref.getInt("pref_key_launcher_swipedown2_action", 1);
		pref_swipeup2 = pref.getInt("pref_key_launcher_swipeup2_action", 1);
		pref_swiperight = pref.getInt("pref_key_launcher_swiperight_action", 1);
		pref_swipeleft = pref.getInt("pref_key_launcher_swipeleft_action", 1);
		pref_shake = pref.getInt("pref_key_launcher_shake_action", 1);
		pref_screenanim = pref.getInt("pref_key_system_screenanim_duration", 0);
		pref_powerflash = pref.getBoolean("pref_key_controls_powerflash", false);
		pref_nolightup = pref.getBoolean("pref_key_system_nolightuponcharge", false);
		pref_appdetails = pref.getBoolean("pref_key_various_appdetails", false);
		pref_appsort = Integer.parseInt(MainModule.pref.getString("pref_key_various_appsort", "0"));
		pref_scramblepin = pref.getBoolean("pref_key_system_scramblepin", false);
		pref_securelock = pref.getBoolean("pref_key_system_securelock", false);
		pref_etoasts = Integer.parseInt(MainModule.pref.getString("pref_key_system_iconlabletoasts", "1"));

		if (pref_etoasts > 1) System.IconLabelToastsHook();
	}

	public void handleLoadPackage(final LoadPackageParam lpparam) {
		String pkg = lpparam.packageName;

		if (pkg.equals("android") && lpparam.processName.equals("android")) {
			PackagePermissions.init(lpparam);
			GlobalActions.setupGlobalActions(lpparam);

			if (pref_powerflash) Controls.PowerKeyHook(lpparam);
			if (pref_screenanim > 0) System.ScreenAnimHook(lpparam);
			if (pref_nolightup) System.NoLightUpOnChargeHook(lpparam);
			if (pref_securelock) System.EnhancedSecurityHook(lpparam);
		}

		if (pkg.equals(Helpers.modulePkg)) {
			GlobalActions.miuizerInit(lpparam);
		}

		if (pkg.equals("com.android.systemui")) {
			GlobalActions.setupStatusBar(lpparam);

			if (pref_scramblepin) System.ScramblePINHook(lpparam);
		}

//		if (pkg.equals("com.android.incallui")) {
//			Various.LargeCallerPhotoHook(lpparam);
//		}

		if (pkg.equals("com.miui.securitycenter")) {
			if (pref_appdetails) Various.AppInfoHook(lpparam);
			if (pref_appsort > 0) Various.AppsDefaultSortHook(lpparam);
		}

		if (pkg.equals("com.android.settings")) {
			GlobalActions.miuizerSettingsInit(lpparam);
		}

		if (pkg.equals("com.miui.home")) {
			if (pref_swipedown != 1 || pref_swipeup != 1 || pref_swipedown2 != 1 || pref_swipeup2 != 1)
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