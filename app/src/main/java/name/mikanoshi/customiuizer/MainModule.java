package name.mikanoshi.customiuizer;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.mikanoshi.customiuizer.mods.Controls;
import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.mods.Launcher;
import name.mikanoshi.customiuizer.mods.PackagePermissions;
import name.mikanoshi.customiuizer.mods.System;
import name.mikanoshi.customiuizer.mods.Various;
import name.mikanoshi.customiuizer.utils.Helpers;

@SuppressWarnings("WeakerAccess")
public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	public static String MODULE_PATH = null;
	public static XSharedPreferences pref = null;

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
	public static int pref_recents_blur = 100;
	public static int pref_drawer_blur = 100;
	public static int pref_volumemedia_up = 0;
	public static int pref_volumemedia_down = 0;
	public static int pref_navbarleft = 1;
	public static int pref_navbarleftlong = 1;
	public static int pref_navbarright = 1;
	public static int pref_navbarrightlong = 1;
	//public static int pref_rotateanim = 1;
	public static boolean pref_powerflash = false;
	public static boolean pref_nolightup1 = false;
	public static boolean pref_nolightup2 = false;
	public static boolean pref_appdetails = false;
	public static boolean pref_scramblepin = false;
	public static boolean pref_securelock = false;
	public static boolean pref_nopassword = false;
	public static boolean pref_dttosleep = false;
	public static boolean pref_separatevolume = false;
	public static boolean pref_volumecursor = false;
	public static boolean pref_clockseconds = false;
	public static boolean pref_expandnotif = false;
	public static boolean pref_hidemobiletype = false;
	public static boolean pref_fixmeter = false;

	public void initZygote(StartupParam startParam) {
		MODULE_PATH = startParam.modulePath;

		try {
			//pref = new XSharedPreferences(Helpers.modulePkg, Helpers.prefsName);
			pref = new XSharedPreferences(new File("/data/user_de/0/" + Helpers.modulePkg + "/shared_prefs/" + Helpers.prefsName + ".xml"));
			pref.makeWorldReadable();
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		if (pref == null || pref.getAll().size() == 0) {
			XposedBridge.log("[CustoMIUIzer] Cannot read module's SharedPreferences, mods won't work!");
			return;
		}

		pref_swipedown = pref.getInt("pref_key_launcher_swipedown_action", 1);
		pref_swipeup = pref.getInt("pref_key_launcher_swipeup_action", 1);
		pref_swipedown2 = pref.getInt("pref_key_launcher_swipedown2_action", 1);
		pref_swipeup2 = pref.getInt("pref_key_launcher_swipeup2_action", 1);
		pref_swiperight = pref.getInt("pref_key_launcher_swiperight_action", 1);
		pref_swipeleft = pref.getInt("pref_key_launcher_swipeleft_action", 1);
		pref_shake = pref.getInt("pref_key_launcher_shake_action", 1);
		pref_screenanim = pref.getInt("pref_key_system_screenanim_duration", 0);
		pref_powerflash = pref.getBoolean("pref_key_controls_powerflash", false);
		pref_nolightup1 = pref.getBoolean("pref_key_system_nolightuponcharge", false);
		pref_nolightup2 = pref.getBoolean("pref_key_system_nolightuponheadset", false);
		pref_appdetails = pref.getBoolean("pref_key_various_appdetails", false);
		pref_appsort = Integer.parseInt(pref.getString("pref_key_various_appsort", "0"));
		pref_scramblepin = pref.getBoolean("pref_key_system_scramblepin", false);
		pref_securelock = pref.getBoolean("pref_key_system_securelock", false);
		pref_nopassword = pref.getBoolean("pref_key_system_nopassword", false);
		pref_etoasts = Integer.parseInt(pref.getString("pref_key_system_iconlabletoasts", "1"));
		pref_dttosleep = pref.getBoolean("pref_key_system_dttosleep", false);
		pref_separatevolume = pref.getBoolean("pref_key_system_separatevolume", false);
		pref_volumecursor = pref.getBoolean("pref_key_controls_volumecursor", false);
		pref_clockseconds = pref.getBoolean("pref_key_system_clockseconds", false);
		pref_expandnotif = pref.getBoolean("pref_key_system_expandnotif", false);
		pref_recents_blur = pref.getInt("pref_key_system_recents_blur", 100);
		pref_drawer_blur = pref.getInt("pref_key_system_drawer_blur", 100);
		pref_volumemedia_up = Integer.parseInt(pref.getString("pref_key_controls_volumemedia_up", "0"));
		pref_volumemedia_down = Integer.parseInt(pref.getString("pref_key_controls_volumemedia_down", "0"));
		pref_navbarleft = pref.getInt("pref_key_controls_navbarleft_action", 1);
		pref_navbarleftlong = pref.getInt("pref_key_controls_navbarleftlong_action", 1);
		pref_navbarright = pref.getInt("pref_key_controls_navbarright_action", 1);
		pref_navbarrightlong = pref.getInt("pref_key_controls_navbarrightlong_action", 1);
		pref_hidemobiletype = pref.getBoolean("pref_key_system_hidemobiletype", false);
		pref_fixmeter = pref.getBoolean("pref_key_system_fixmeter", false);
		//pref_rotateanim = Integer.parseInt(pref.getString("pref_key_system_rotateanim", "1"));

		if (pref_etoasts > 1) System.IconLabelToastsHook();
		if (pref_volumecursor) Controls.VolumeCursorHook();

		Controls.VolumeMediaPlayerHook();
		GlobalActions.setupUnhandledCatcher();
	}

	public void handleLoadPackage(final LoadPackageParam lpparam) {
		String pkg = lpparam.packageName;

		if (pkg.equals("android") && lpparam.processName.equals("android")) {
			PackagePermissions.init(lpparam);
			GlobalActions.setupGlobalActions(lpparam);

			if (pref_powerflash) Controls.PowerKeyHook(lpparam);
			if (pref_screenanim > 0) System.ScreenAnimHook(lpparam);
			if (pref_nolightup1) System.NoLightUpOnChargeHook(lpparam);
			if (pref_nolightup2) System.NoLightUpOnHeadsetHook(lpparam);
			if (pref_securelock) System.EnhancedSecurityHook(lpparam);
			if (pref_separatevolume) System.NotificationVolumeServiceHook(lpparam);
			if (pref_volumemedia_up > 0 || pref_volumemedia_down > 0) Controls.VolumeMediaButtonsHook(lpparam);
			//if (pref_rotateanim > 1) System.RotationAnimationHook(lpparam);
		}

		if (pkg.equals(Helpers.modulePkg)) {
			GlobalActions.miuizerInit(lpparam);
		}

		if (pkg.equals("com.android.systemui")) {
			GlobalActions.setupStatusBar(lpparam);

			if (pref_scramblepin) System.ScramblePINHook(lpparam);
			if (pref_nopassword) System.NoPasswordHook(lpparam);
			if (pref_dttosleep) System.DoubleTapToSleepHook(lpparam);
			if (pref_clockseconds) System.ClockSecondsHook(lpparam);
			if (pref_expandnotif) System.ExpandNotificationsHook(lpparam);
			if (pref_recents_blur < 100) System.RecentsBlurRatioHook(lpparam);
			if (pref_drawer_blur < 100) System.DrawerBlurRatioHook(lpparam);
			if (pref_navbarleft > 1 || pref_navbarleftlong > 1 || pref_navbarright > 1 || pref_navbarrightlong > 1) Controls.NavBarButtonsHook(lpparam);
			if (pref_hidemobiletype) System.HideNetworkTypeHook(lpparam);
			if (pref_fixmeter) System.TrafficSpeedSpacingHook(lpparam);
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

			if (pref_separatevolume) System.NotificationVolumeSettingsHook(lpparam);
		}

		if (pkg.equals("com.miui.home")) {
			if (pref_swipedown != 1 || pref_swipeup != 1 || pref_swipedown2 != 1 || pref_swipeup2 != 1)
			Launcher.HomescreenSwipesHook(lpparam);
			if (pref_swipeleft != 1 || pref_swiperight != 1)
			Launcher.HotSeatSwipesHook(lpparam);
			if (pref_shake != 1)
			Launcher.ShakeHook(lpparam);
		}
	}
}