package name.mikanoshi.customiuizer;

import java.io.File;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.mikanoshi.customiuizer.mods.Controls;
import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.mods.Launcher;
import name.mikanoshi.customiuizer.mods.PackagePermissions;
import name.mikanoshi.customiuizer.mods.System;
import name.mikanoshi.customiuizer.mods.Various;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.PrefMap;

public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

	public static String MODULE_PATH = null;
	public static PrefMap<String, Object> mPrefs = new PrefMap<String, Object>();

	public void initZygote(StartupParam startParam) {
		MODULE_PATH = startParam.modulePath;

		XSharedPreferences pref = null;
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
		} else mPrefs.putAll(pref.getAll());

		if (Integer.parseInt(mPrefs.getString("system_iconlabletoasts", "1")) > 1) System.IconLabelToastsHook();
		if (mPrefs.getBoolean("controls_volumecursor")) Controls.VolumeCursorHook();
		if (mPrefs.getBoolean("system_popupnotif_fs")) System.PopupNotificationsFSHook();
		if (Integer.parseInt(mPrefs.getString("system_rotateanim", "1")) > 1) System.RotationAnimationHook();
		if (mPrefs.getBoolean("system_colorizenotiftitle")) System.ColorizedNotificationTitlesHook();
		if (mPrefs.getBoolean("system_compactnotif")) System.CompactNotificationsActionsRes();
		if (mPrefs.getBoolean("system_nopassword")) System.NoPasswordHook();

		Controls.VolumeMediaPlayerHook();
		GlobalActions.setupUnhandledCatcher();
	}

	@Override
	public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
		String pkg = resparam.packageName;

		if (pkg.equals("com.android.systemui")) {
			if (mPrefs.getBoolean("system_compactnotif")) System.CompactNotificationsPaddingRes(resparam);
			if (mPrefs.getBoolean("system_notifrowmenu")) System.NotificationRowMenuRes(resparam);
			//System.QSGridRes(resparam);
		}

		if (pkg.equals("com.android.settings")) {
			GlobalActions.miuizerSettingsResInit(resparam);
		}

		if (pkg.equals("com.miui.home")) {
			if (mPrefs.getBoolean("launcher_unlockgrids")) try {
				resparam.res.setReplacement(pkg, "integer", "config_cell_count_x", 3);
				resparam.res.setReplacement(pkg, "integer", "config_cell_count_y", 4);
				resparam.res.setReplacement(pkg, "integer", "config_cell_count_x_min", 3);
				resparam.res.setReplacement(pkg, "integer", "config_cell_count_y_min", 4);
				resparam.res.setReplacement(pkg, "integer", "config_cell_count_x_max", 6);
				resparam.res.setReplacement(pkg, "integer", "config_cell_count_y_max", 7);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}

			if (mPrefs.getInt("launcher_folder_cols", 3) != 3) try {
				resparam.res.setReplacement(pkg, "integer", "config_folder_columns_count", mPrefs.getInt("launcher_folder_cols", 3));
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	}

	public void handleLoadPackage(final LoadPackageParam lpparam) {
		String pkg = lpparam.packageName;

		if (pkg.equals("android") && lpparam.processName.equals("android")) {
			PackagePermissions.init(lpparam);
			GlobalActions.setupGlobalActions(lpparam);

			if (mPrefs.getBoolean("controls_powerflash")) Controls.PowerKeyHook(lpparam);
			if (mPrefs.getInt("system_screenanim_duration", 0) > 0) System.ScreenAnimHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("system_nolightuponcharges", "1")) > 1) System.NoLightUpOnChargeHook(lpparam);
			if (mPrefs.getBoolean("system_nolightuponheadset")) System.NoLightUpOnHeadsetHook(lpparam);
			if (mPrefs.getBoolean("system_securelock")) System.EnhancedSecurityHook(lpparam);
			if (mPrefs.getBoolean("system_separatevolume")) System.NotificationVolumeServiceHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("controls_volumemedia_up", "0")) > 0 ||
				Integer.parseInt(mPrefs.getString("controls_volumemedia_down", "0")) > 0) Controls.VolumeMediaButtonsHook(lpparam);
			if (mPrefs.getInt("controls_fingerprint1_action", 1) > 1 ||
				mPrefs.getInt("controls_fingerprint2_action", 1) > 1 ||
				mPrefs.getInt("controls_fingerprintlong_action", 1) > 1) Controls.FingerprintEventsHook(lpparam);
			if (mPrefs.getInt("system_volumesteps", 10) > 10) System.VolumeStepsHook(lpparam);
			if (mPrefs.getBoolean("system_downgrade")) System.NoVersionCheckHook(lpparam);
			if (mPrefs.getBoolean("system_hidefromrecents")) System.HideFromRecentsHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("system_autogroupnotif", "1")) > 1) System.AutoGroupNotificationsHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("system_vibration", "1")) > 1) System.SelectiveVibrationHook(lpparam);
			//System.AutoBrightnessHook(lpparam);
		}

		if (pkg.equals(Helpers.modulePkg)) {
			GlobalActions.miuizerInit(lpparam);
		}

		if (pkg.equals("com.android.systemui")) {
			GlobalActions.setupStatusBar(lpparam);

			if (mPrefs.getBoolean("system_scramblepin")) System.ScramblePINHook(lpparam);
			if (mPrefs.getBoolean("system_dttosleep")) System.DoubleTapToSleepHook(lpparam);
			if (mPrefs.getBoolean("system_clockseconds")) System.ClockSecondsHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("system_expandnotifs", "1")) > 1) System.ExpandNotificationsHook(lpparam);
			if (mPrefs.getInt("system_recents_blur", 100) < 100) System.RecentsBlurRatioHook(lpparam);
			if (mPrefs.getInt("system_drawer_blur", 100) < 100) System.DrawerBlurRatioHook(lpparam);
			if (mPrefs.getInt("system_drawer_opacity", 100) < 100) System.DrawerThemeBackgroundHook(lpparam);
			if (mPrefs.getInt("controls_navbarleft_action", 1) > 1 ||
				mPrefs.getInt("controls_navbarleftlong_action", 1) > 1 ||
				mPrefs.getInt("controls_navbarright_action", 1) > 1 ||
				mPrefs.getInt("controls_navbarrightlong_action", 1) > 1) Controls.NavBarButtonsHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("system_mobiletypeicon", "1")) > 1) System.HideNetworkTypeHook(lpparam);
			if (mPrefs.getBoolean("system_fixmeter")) System.TrafficSpeedSpacingHook(lpparam);
			if (mPrefs.getInt("system_chargeanimtime", 20) < 20) System.ChargeAnimationHook(lpparam);
			if (mPrefs.getBoolean("system_noscreenlock_act")) System.NoScreenLockHook(lpparam);
			if (mPrefs.getBoolean("system_detailednetspeed")) System.DetailedNetSpeedHook(lpparam);
			if (mPrefs.getInt("system_netspeedinterval", 4) != 4) System.NetSpeedIntervalHook(lpparam);
			if (mPrefs.getBoolean("system_albumartonlock")) System.LockScreenAlbumArtHook(lpparam);
			if (mPrefs.getBoolean("system_popupnotif")) System.PopupNotificationsHook(lpparam);
			if (mPrefs.getBoolean("system_betterpopups_nohide")) System.BetterPopupsNoHideHook(lpparam);
			if (mPrefs.getBoolean("system_betterpopups_swipedown")) System.BetterPopupsSwipeDownHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("system_qshaptics", "1")) > 1) System.QSHapticHook(lpparam);
			if (mPrefs.getInt("controls_fsg_coverage", 60) != 60) System.BackGestureAreaHook(lpparam);
			if (mPrefs.getBoolean("system_hidemoreicon")) System.NoMoreIconHook(lpparam);
			if (mPrefs.getBoolean("system_notifafterunlock")) System.ShowNotificationsAfterUnlockHook(lpparam);
			if (mPrefs.getBoolean("system_notifrowmenu")) System.NotificationRowMenuHook(lpparam);
		}

//		if (pkg.equals("com.android.incallui")) {
//			Various.LargeCallerPhotoHook(lpparam);
//		}

		if (pkg.equals("com.miui.securitycenter")) {
			if (mPrefs.getBoolean("various_appdetails")) Various.AppInfoHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("various_appsort", "0")) > 0) Various.AppsDefaultSortHook(lpparam);
		}

		if (pkg.equals("com.android.settings")) {
			GlobalActions.miuizerSettingsInit(lpparam);

			if (mPrefs.getBoolean("system_separatevolume")) System.NotificationVolumeSettingsHook(lpparam);
		}

		if (pkg.equals("com.miui.home")) {
			if (mPrefs.getInt("launcher_swipedown_action", 1) != 1 ||
				mPrefs.getInt("launcher_swipeup_action", 1) != 1 ||
				mPrefs.getInt("launcher_swipedown2_action", 1) != 1 ||
				mPrefs.getInt("launcher_swipeup2_action", 1) != 1) Launcher.HomescreenSwipesHook(lpparam);
			if (mPrefs.getInt("launcher_swipeleft_action", 1) != 1 ||
				mPrefs.getInt("launcher_swiperight_action", 1) != 1) Launcher.HotSeatSwipesHook(lpparam);
			if (mPrefs.getInt("launcher_shake_action", 1) != 1) Launcher.ShakeHook(lpparam);
			if (Integer.parseInt(mPrefs.getString("launcher_foldershade", "1")) > 1) Launcher.FolderShadeHook(lpparam);
			if (mPrefs.getBoolean("launcher_noclockhide")) Launcher.NoClockHideHook(lpparam);
			if (mPrefs.getBoolean("launcher_renameapps")) Launcher.RenameShortcutsHook(lpparam);
		}
	}

}