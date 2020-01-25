package name.mikanoshi.customiuizer;

import android.app.Application;
import android.content.Context;

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
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;
import name.mikanoshi.customiuizer.utils.PrefMap;
import name.mikanoshi.customiuizer.utils.ResourceHooks;

public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	public static PrefMap<String, Object> mPrefs = new PrefMap<String, Object>();
	public static ResourceHooks resHooks;
	private static boolean hideIconsActive = false;
	private static boolean fsMonitorActive = false;

	public void initZygote(StartupParam startParam) {
		if (mPrefs.size() == 0) {
			XSharedPreferences pref = null;
			try {
				pref = new XSharedPreferences(new File("/data/user_de/0/" + Helpers.modulePkg + "/shared_prefs/" + Helpers.prefsName + ".xml"));
				pref.makeWorldReadable();
			} catch (Throwable t) {
				XposedBridge.log(t);
			}

			if (pref == null || pref.getAll().size() == 0)
				Helpers.log("[UID " + android.os.Process.myUid() +"] Cannot read module's SharedPreferences, some mods might not work!");
			else
				mPrefs.putAll(pref.getAll());
		}

		resHooks = new ResourceHooks();
		GlobalActions.miuizerSettingsResInit();

		//resHooks.setObjectReplacement("android", "bool", "", true);

		if (mPrefs.getInt("system_statusbarheight", 19) > 19) System.StatusBarHeightRes();
		if (mPrefs.getInt("system_qsgridcolumns", 2) > 2 || mPrefs.getInt("system_qsgridrows", 1) > 1) System.QSGridRes();
		if (mPrefs.getInt("system_qqsgridcolumns", 2) > 2) System.QQSGridRes();
		if (mPrefs.getInt("controls_navbarheight", 26) > 26) Controls.NavbarHeightRes();
		if (mPrefs.getInt("launcher_horizmargin", 0) > 0) Launcher.HorizontalSpacingRes();
		if (mPrefs.getInt("launcher_topmargin", 0) > 0) Launcher.TopSpacingRes();
		if (mPrefs.getInt("launcher_bottommargin", 0) > 0) Launcher.BottomSpacingRes();
		if (mPrefs.getInt("launcher_indicatorheight", 9) > 9) Launcher.IndicatorHeightRes();
		if (mPrefs.getInt("system_volumeblur_collapsed", 0) > 0 || mPrefs.getInt("system_volumeblur_expanded", 0) > 0) System.BlurVolumeDialogBackgroundRes();
		if (mPrefs.getBoolean("system_notifrowmenu")) System.NotificationRowMenuRes();
		if (mPrefs.getBoolean("system_compactnotif")) System.CompactNotificationsRes();
		if (mPrefs.getBoolean("system_volumetimer")) System.VolumeTimerValuesRes();
		if (mPrefs.getBoolean("system_separatevolume")) System.NotificationVolumeDialogRes();
		if (mPrefs.getBoolean("system_snoozedmanager")) System.MoreSnoozeOptionsRes();
		if (mPrefs.getBoolean("system_nosafevolume")) System.NoSafeVolumeWarningRes();
		if (mPrefs.getBoolean("system_statusbaricons_volte")) System.HideIconsVoLTERes();
		if (mPrefs.getBoolean("launcher_unlockgrids")) Launcher.UnlockGridsRes();
		if (mPrefs.getBoolean("launcher_docktitles")) Launcher.ShowHotseatTitlesRes();
		if (mPrefs.getBoolean("controls_powerflash")) Controls.PowerKeyRes();
		if (mPrefs.getBoolean("controls_noscrchord")) Controls.NoScreenshotChordRes();
		if (mPrefs.getStringAsInt("system_allrotations2", 1) > 1) System.AllRotationsRes();
		if (mPrefs.getStringAsInt("system_rotateanim", 1) > 1) System.RotationAnimationRes();

		if (mPrefs.getInt("system_betterpopups_delay", 0) > 0 && !mPrefs.getBoolean("system_betterpopups_nohide")) System.BetterPopupsHideDelaySysHook();
		if (mPrefs.getBoolean("system_colorizenotiftitle")) System.ColorizedNotificationTitlesHook();
		if (mPrefs.getBoolean("system_nopassword")) System.NoPasswordHook();
		if (mPrefs.getBoolean("system_epm")) System.ExtendedPowerMenuHook();
		if (mPrefs.getBoolean("system_statusbarcolor")) System.StatusBarBackgroundHook();
		if (mPrefs.getBoolean("system_magnifier") && Helpers.isPiePlus()) System.TextMagnifierHook();
		if (mPrefs.getBoolean("system_lockscreenshortcuts") || mPrefs.getInt("controls_powerdt_action", 1) > 1) System.LockScreenSecureLaunchHook();
		if (mPrefs.getBoolean("system_notifmediaseekbar")) System.MediaNotificationSeekBarHook();
		if (mPrefs.getBoolean("system_disableanynotif") && !Helpers.isNougat()) System.DisableAnyNotificationBlockHook();
		if (mPrefs.getBoolean("system_apksign")) System.NoSignatureVerifyHook();
		if (mPrefs.getBoolean("system_nooverscroll")) System.NoOverscrollHook();
		if (mPrefs.getBoolean("system_cleanshare")) System.CleanShareMenuHook();
		if (mPrefs.getBoolean("system_cleanopenwith")) System.CleanOpenWithMenuHook();
		if (mPrefs.getBoolean("system_removesecure")) System.RemoveSecureHook();
		if (mPrefs.getBoolean("system_allownotifonkeyguard")) System.AllowAllKeyguardSysHook();
		if (mPrefs.getBoolean("system_allownotiffloat")) System.AllowAllFloatSysHook();
		if (mPrefs.getBoolean("system_resizablewidgets")) System.ResizableWidgetsHook();
		if (mPrefs.getBoolean("controls_volumecursor")) Controls.VolumeCursorHook();
		if (mPrefs.getBoolean("controls_fsg_horiz")) Controls.FSGesturesHook();
		if (mPrefs.getBoolean("various_alarmcompat")) Various.AlarmCompatHook();
		if (mPrefs.getStringAsInt("system_iconlabletoasts", 1) > 1) System.IconLabelToastsHook();
		if (mPrefs.getStringAsInt("system_blocktoasts", 1) > 1) System.SelectiveToastsHook();

		hideIconsActive =
			mPrefs.getBoolean("system_statusbaricons_alarm") ||
			mPrefs.getBoolean("system_statusbaricons_profile") ||
			mPrefs.getBoolean("system_statusbaricons_sound") ||
			mPrefs.getBoolean("system_statusbaricons_dnd") ||
			mPrefs.getBoolean("system_statusbaricons_headset") ||
			mPrefs.getBoolean("system_statusbaricons_mute") ||
			mPrefs.getBoolean("system_statusbaricons_speaker") ||
			mPrefs.getBoolean("system_statusbaricons_record");
		if (hideIconsActive) System.HideIconsSystemHook();

		fsMonitorActive = mPrefs.getBoolean("system_popupnotif_fs") || mPrefs.getStringAsInt("various_showcallui", 0) > 0;
		if (fsMonitorActive) GlobalActions.setupFullScreenMonitor();

		Controls.VolumeMediaPlayerHook();
		GlobalActions.setupSystemHelpers();
	}

	public void handleLoadPackage(final LoadPackageParam lpparam) {
		String pkg = lpparam.packageName;

		if (pkg.equals("android") && lpparam.processName.equals("android")) {
			PackagePermissions.init(lpparam);
			GlobalActions.setupGlobalActions(lpparam);
			if (fsMonitorActive) GlobalActions.setupFullScreenReceiver(lpparam);

			if (mPrefs.getInt("controls_fingerprint1_action", 1) > 1 ||
				mPrefs.getInt("controls_fingerprint2_action", 1) > 1 ||
				mPrefs.getInt("controls_fingerprintlong_action", 1) > 1 ||
				mPrefs.getStringAsInt("controls_fingerprint_accept", 1) > 1 ||
				mPrefs.getStringAsInt("controls_fingerprint_reject", 1) > 1 ||
				mPrefs.getStringAsInt("controls_fingerprint_hangup", 1) > 1) Controls.FingerprintEventsHook(lpparam);
			if (mPrefs.getInt("controls_backlong_action", 1) > 1 ||
				mPrefs.getInt("controls_homelong_action", 1) > 1 ||
				mPrefs.getInt("controls_menulong_action", 1) > 1) Controls.NavBarActionsHook(lpparam);
			if (mPrefs.getInt("controls_powerdt_action", 1) > 1) Controls.PowerDoubleTapActionHook(lpparam);
			if (mPrefs.getInt("system_screenanim_duration", 0) > 0) System.ScreenAnimHook(lpparam);
			if (mPrefs.getInt("system_volumesteps", 0) > 0) System.VolumeStepsHook(lpparam);
			if (mPrefs.getInt("system_applock_timeout", 1) > 1) System.AppLockTimeoutHook(lpparam);
			if (mPrefs.getInt("system_dimtime", 0) > 0) System.ScreenDimTimeHook(lpparam);
			if (mPrefs.getInt("system_toasttime", 0) > 0) System.ToastTimeHook(lpparam);
			if (!mPrefs.getString("system_defaultusb", "none").equals("none")) System.USBConfigHook(lpparam);
			if (mPrefs.getBoolean("system_nolightuponheadset")) System.NoLightUpOnHeadsetHook(lpparam);
			if (mPrefs.getBoolean("system_securelock")) System.EnhancedSecurityHook(lpparam);
			if (mPrefs.getBoolean("system_separatevolume")) System.NotificationVolumeServiceHook(lpparam);
			if (mPrefs.getBoolean("system_downgrade")) System.NoVersionCheckHook(lpparam);
			if (mPrefs.getBoolean("system_hidefromrecents")) System.HideFromRecentsHook(lpparam);
			if (mPrefs.getBoolean("system_orientationlock")) System.OrientationLockHook(lpparam);
			if (mPrefs.getBoolean("system_noducking")) System.NoDuckingHook(lpparam);
			if (mPrefs.getBoolean("system_epm")) System.ExtendedPowerMenuHook(lpparam);
			if (mPrefs.getBoolean("system_cleanshare")) System.CleanShareMenuServiceHook(lpparam);
			if (mPrefs.getBoolean("system_cleanopenwith")) System.CleanOpenWithMenuServiceHook(lpparam);
			if (mPrefs.getBoolean("system_autobrightness")) System.AutoBrightnessRangeHook(lpparam);
			if (mPrefs.getBoolean("system_applock")) System.AppLockHook(lpparam);
			if (mPrefs.getBoolean("various_alarmcompat")) Various.AlarmCompatServiceHook(lpparam);
			if (mPrefs.getBoolean("system_ignorecalls")) System.NoCallInterruptionHook(lpparam);
			if (mPrefs.getBoolean("system_forceclose")) System.ForceCloseHook(lpparam);
			if (mPrefs.getBoolean("system_hideproxywarn")) System.HideProximityWarningHook(lpparam);
			if (mPrefs.getBoolean("system_firstpress")) System.FirstVolumePressHook(lpparam);
			if (mPrefs.getBoolean("system_apksign")) System.NoSignatureVerifyServiceHook(lpparam);
			if (mPrefs.getBoolean("system_vibration_amp")) System.MuffledVibrationHook(lpparam);
			if (mPrefs.getBoolean("system_clearalltasks")) System.ClearAllTasksHook(lpparam);
			if (mPrefs.getBoolean("system_snoozedmanager")) System.MoreSnoozeOptionsServiceHook(lpparam);
			if (mPrefs.getBoolean("controls_powerflash")) Controls.PowerKeyHook(lpparam);
			if (mPrefs.getBoolean("controls_fingerprintfailure")) Controls.FingerprintHapticFailureHook(lpparam);
			if (mPrefs.getBoolean("controls_fingerprintscreen")) Controls.FingerprintScreenOnHook(lpparam);
			if (mPrefs.getBoolean("controls_fingerprintwake")) Controls.NoFingerprintWakeHook(lpparam);
			if (mPrefs.getBoolean("controls_volumecursor")) Controls.VolumeCursorFocusedHook(lpparam);
			if (mPrefs.getBoolean("various_miuiinstaller")) Various.MiuiPackageInstallerServiceHook(lpparam);
			if (mPrefs.getBoolean("various_disableapp")) Various.AppsDisableServiceHook(lpparam);
			if (mPrefs.getStringAsInt("system_allrotations2", 1) > 1) System.AllRotationsHook(lpparam);
			if (mPrefs.getStringAsInt("system_nolightuponcharges", 1) > 1) System.NoLightUpOnChargeHook(lpparam);
			if (mPrefs.getStringAsInt("system_autogroupnotif", 1) > 1) System.AutoGroupNotificationsHook(lpparam);
			if (mPrefs.getStringAsInt("system_vibration", 1) > 1) System.SelectiveVibrationHook(lpparam);
			if (mPrefs.getStringAsInt("controls_fingerprintsuccess", 1) > 1) Controls.FingerprintHapticSuccessHook(lpparam);
			if (mPrefs.getStringAsInt("controls_volumemedia_up", 0) > 0 ||
				mPrefs.getStringAsInt("controls_volumemedia_down", 0) > 0) Controls.VolumeMediaButtonsHook(lpparam);

			//Controls.AIButtonHook(lpparam);
		}

		if (pkg.equals("com.android.server.telecom")) {
			if (mPrefs.getBoolean("various_callreminder")) Various.CallReminderHook(lpparam);
		}

		if (pkg.equals(Helpers.modulePkg)) {
			GlobalActions.miuizerInit(lpparam);
		}

		if (pkg.equals("com.android.systemui")) {
			GlobalActions.setupStatusBar(lpparam);

			if (mPrefs.getInt("system_statusbarheight", 19) > 19) System.StatusBarHeightHook(lpparam);
			if (mPrefs.getInt("system_recents_blur", 100) < 100) System.RecentsBlurRatioHook(lpparam);
			if (mPrefs.getInt("system_drawer_blur", 100) < 100) System.DrawerBlurRatioHook(lpparam);
			if (mPrefs.getInt("system_drawer_opacity", 100) < 100) System.DrawerThemeBackgroundHook(lpparam);
			if (mPrefs.getInt("system_chargeanimtime", 20) < 20) System.ChargeAnimationHook(lpparam);
			if (mPrefs.getInt("system_betterpopups_delay", 0) > 0 && !mPrefs.getBoolean("system_betterpopups_nohide")) System.BetterPopupsHideDelayHook(lpparam);
			if (mPrefs.getInt("system_netspeedinterval", 4) != 4) System.NetSpeedIntervalHook(lpparam);
			if (mPrefs.getInt("system_qsgridrows", 1) > 1 || mPrefs.getBoolean("system_qsnolabels")) System.QSGridLabelsHook(lpparam);
			if (mPrefs.getInt("system_volumeblur_collapsed", 0) > 0 || mPrefs.getInt("system_volumeblur_expanded", 0) > 0) System.BlurVolumeDialogBackgroundHook(lpparam);
			if (mPrefs.getInt("system_lstimeout", 9) > 9) System.LockScreenTimeoutHook(lpparam);
			if (mPrefs.getInt("controls_fsg_coverage", 60) != 60) Controls.BackGestureAreaHeightHook(lpparam);
			if (mPrefs.getInt("controls_fsg_width", 100) > 100) Controls.BackGestureAreaWidthHook(lpparam);
			if (mPrefs.getInt("controls_navbarleft_action", 1) > 1 ||
				mPrefs.getInt("controls_navbarleftlong_action", 1) > 1 ||
				mPrefs.getInt("controls_navbarright_action", 1) > 1 ||
				mPrefs.getInt("controls_navbarrightlong_action", 1) > 1) Controls.NavBarButtonsHook(lpparam);
			if (mPrefs.getInt("system_recommended_first_action", 1) > 1 ||
				mPrefs.getInt("system_recommended_second_action", 1) > 1 ||
				mPrefs.getInt("system_recommended_third_action", 1) > 1 ||
				mPrefs.getInt("system_recommended_fourth_action", 1) > 1) System.CustomRecommendedHook(lpparam);
			if (mPrefs.getBoolean("system_scramblepin")) System.ScramblePINHook(lpparam);
			if (mPrefs.getBoolean("system_dttosleep")) System.DoubleTapToSleepHook(lpparam);
			if (mPrefs.getBoolean("system_clockseconds")) System.ClockSecondsHook(lpparam);
			if (mPrefs.getBoolean("system_fixmeter")) System.TrafficSpeedSpacingHook(lpparam);
			if (mPrefs.getBoolean("system_noscreenlock_act")) System.NoScreenLockHook(lpparam);
			if (mPrefs.getBoolean("system_detailednetspeed")) System.DetailedNetSpeedHook(lpparam);
			if (mPrefs.getBoolean("system_albumartonlock")) System.LockScreenAlbumArtHook(lpparam);
			if (mPrefs.getBoolean("system_popupnotif")) System.PopupNotificationsHook(lpparam);
			if (mPrefs.getBoolean("system_betterpopups_nohide")) System.BetterPopupsNoHideHook(lpparam);
			if (mPrefs.getBoolean("system_betterpopups_swipedown")) System.BetterPopupsSwipeDownHook(lpparam);
			if (mPrefs.getBoolean("system_hidemoreicon")) System.NoMoreIconHook(lpparam);
			if (mPrefs.getBoolean("system_notifafterunlock")) System.ShowNotificationsAfterUnlockHook(lpparam);
			if (mPrefs.getBoolean("system_notifrowmenu")) System.NotificationRowMenuHook(lpparam);
			if (mPrefs.getBoolean("system_compactnotif")) System.CompactNotificationsHook(lpparam);
			if (mPrefs.getBoolean("system_removecleaner")) System.HideMemoryCleanHook(lpparam);
			if (mPrefs.getBoolean("system_removedismiss")) System.HideDismissViewHook(lpparam);
			if (mPrefs.getBoolean("controls_nonavbar")) Controls.HideNavBarHook(lpparam);
			if (mPrefs.getBoolean("controls_imebackalticon")) Controls.ImeBackAltIconHook(lpparam);
			if (mPrefs.getBoolean("system_visualizer")) System.AudioVisualizerHook(lpparam);
			if (mPrefs.getBoolean("system_separatevolume") && mPrefs.getBoolean("system_separatevolume_slider")) System.NotificationVolumeDialogHook(lpparam);
			if (mPrefs.getBoolean("system_batteryindicator")) System.BatteryIndicatorHook(lpparam);
			if (mPrefs.getBoolean("system_disableanynotif")) System.DisableAnyNotificationHook();
			if (mPrefs.getBoolean("system_lockscreenshortcuts")) System.LockScreenShortcutHook(lpparam);
			if (mPrefs.getBoolean("system_notifmediaseekbar")) System.MediaNotificationSeekBarSysUIHook(lpparam);
			if (mPrefs.getBoolean("system_4gtolte")) System.Network4GtoLTEHook(lpparam);
			if (mPrefs.getBoolean("system_showlux")) System.BrightnessLuxHook(lpparam);
			if (mPrefs.getBoolean("system_showpct")) System.BrightnessPctHook(lpparam);
			if (mPrefs.getBoolean("system_hidelsclock")) System.HideLockScreenClockHook(lpparam);
			if (mPrefs.getBoolean("system_nosilentvibrate")) System.NoSilentVibrateHook(lpparam);
			if (mPrefs.getBoolean("system_drawer_hidebackground")) System.HideThemeBackgroundBrightnessHook(lpparam);
			if (mPrefs.getBoolean("system_allowdirectreply")) System.AllowDirectReplyHook(lpparam);
			if (mPrefs.getBoolean("system_allownotifonkeyguard")) System.AllowAllKeyguardHook(lpparam);
			if (mPrefs.getBoolean("system_allownotiffloat")) System.AllowAllFloatHook(lpparam);
			if (mPrefs.getBoolean("system_hideqs")) System.HideQSHook(lpparam);
			if (mPrefs.getBoolean("system_lsalarm")) System.LockScreenAlaramHook(lpparam);
			if (mPrefs.getBoolean("system_statusbarcontrols")) System.StatusBarGesturesHook(lpparam);
			if (mPrefs.getBoolean("system_screenshot")) System.ScreenshotConfigHook(lpparam);
			if (mPrefs.getBoolean("system_nodrawerbackground")) System.RemoveDrawerBackgroundHook(lpparam);
			if (mPrefs.getBoolean("system_nonetspeedseparator")) System.NoNetworkSpeedSeparatorHook(lpparam);
			if (mPrefs.getBoolean("system_snoozedmanager")) System.MoreSnoozeOptionsHook(lpparam);
			if (mPrefs.getBoolean("system_taptounlock")) System.TapToUnlockHook(lpparam);
			if (mPrefs.getBoolean("system_hidelowbatwarn")) System.NoLowBatteryWarningHook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_battery1")) System.HideIconsBattery1Hook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_battery2")) System.HideIconsBattery2Hook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_battery3")) System.HideIconsBattery3Hook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_signal")) System.HideIconsSignalHook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_vpn")) System.HideIconsVPNHook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_nosims")) System.HideIconsNoSIMsHook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_wifi")) System.HideIconsNoWiFiHook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_hotspot")) System.HideIconsHotspotHook(lpparam);
			if (mPrefs.getBoolean("system_statusbaricons_volte")) System.HideIconsVoLTEHook(lpparam);
			if (!mPrefs.getBoolean("system_statusbaricons_alarm") && mPrefs.getInt("system_statusbaricons_alarmn", 0) > 0) System.HideIconsSelectiveAlarmHook(lpparam);
			if (!mPrefs.getString("system_shortcut_app", "").equals("")) System.ReplaceShortcutAppHook(lpparam);
			if (!mPrefs.getString("system_clock_app", "").equals("")) System.ReplaceClockAppHook(lpparam);
			if (!mPrefs.getString("system_calendar_app", "").equals("")) System.ReplaceCalendarAppHook(lpparam);
			if (mPrefs.getStringAsInt("system_qshaptics", 1) > 1) System.QSHapticHook(lpparam);
			if (mPrefs.getStringAsInt("system_expandnotifs", 1) > 1) System.ExpandNotificationsHook(lpparam);
			if (mPrefs.getStringAsInt("system_inactivebrightness", 1) > 1) System.InactiveBrightnessSliderHook(lpparam);
			if (mPrefs.getStringAsInt("system_mobiletypeicon", 1) > 1) System.HideNetworkTypeHook(lpparam);
			if (mPrefs.getStringAsInt("system_statusbaricons_bluetooth", 1) > 1) System.HideIconsBluetoothHook(lpparam);
			if (hideIconsActive) System.HideIconsHook(lpparam);
		}

		if (pkg.equals("com.android.incallui")) {
			if (mPrefs.getStringAsInt("various_showcallui", 0) > 0) Various.ShowCallUIHook(lpparam);
			if (mPrefs.getBoolean("various_calluibright")) Various.InCallBrightnessHook(lpparam);
//			Various.LargeCallerPhotoHook(lpparam);
		}

		if (pkg.equals("com.miui.securitycenter")) {
			if (mPrefs.getBoolean("various_appdetails")) Various.AppInfoHook(lpparam);
			if (mPrefs.getBoolean("various_disableapp")) Various.AppsDisableHook(lpparam);
			if (mPrefs.getBoolean("system_unblockthird")) System.UnblockThirdLaunchersHook(lpparam);
			if (mPrefs.getStringAsInt("various_appsort", 0) > 0) Various.AppsDefaultSortHook(lpparam);
		}

		if (pkg.equals("com.android.settings")) {
			GlobalActions.miuizerSettingsInit(lpparam);
			if (mPrefs.getBoolean("system_separatevolume")) System.NotificationVolumeSettingsHook(lpparam);
			if (mPrefs.getBoolean("system_disableanynotif")) System.DisableAnyNotificationHook();
			if (!mPrefs.getString("system_defaultusb", "none").equals("none")) System.USBConfigSettingsHook(lpparam);
		}

		if (pkg.equals("com.google.android.packageinstaller") || pkg.equals("com.android.packageinstaller")) {
			if (mPrefs.getBoolean("various_installappinfo")) Various.AppInfoDuringInstallHook(lpparam);
		}

		if (pkg.equals("com.miui.packageinstaller")) {
			if (mPrefs.getBoolean("system_apksign")) System.NoSignatureVerifyMiuiHook(lpparam);
			if (mPrefs.getBoolean("various_miuiinstaller")) Various.MiuiPackageInstallerHook(lpparam);
			if (mPrefs.getBoolean("various_installappinfo")) Various.AppInfoDuringMiuiInstallHook(lpparam);
		}

		if (pkg.equals("com.miui.home") || pkg.equals("com.mi.android.globallauncher")) {
			if (mPrefs.getBoolean("launcher_compat")) handleLoadLauncher(lpparam); else
			Helpers.findAndHookMethod(Application.class, "attach", Context.class, new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					handleLoadLauncher(lpparam);
				}
			});
		}

		if (mPrefs.getBoolean("system_statusbarcolor") && !mPrefs.getStringSet("system_statusbarcolor_apps").contains(pkg))
		Helpers.findAndHookMethod(Application.class, "attach", Context.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				System.StatusBarBackgroundCompatHook(lpparam);
			}
		});
	}

	private void handleLoadLauncher(final LoadPackageParam lpparam) {
		if (mPrefs.getInt("launcher_swipedown_action", 1) != 1 ||
			mPrefs.getInt("launcher_swipeup_action", 1) != 1 ||
			mPrefs.getInt("launcher_swipedown2_action", 1) != 1 ||
			mPrefs.getInt("launcher_swipeup2_action", 1) != 1) Launcher.HomescreenSwipesHook(lpparam);
		if (mPrefs.getInt("launcher_swipeleft_action", 1) != 1 ||
			mPrefs.getInt("launcher_swiperight_action", 1) != 1) Launcher.HotSeatSwipesHook(lpparam);
		if (mPrefs.getInt("launcher_shake_action", 1) != 1) Launcher.ShakeHook(lpparam);
		if (mPrefs.getInt("launcher_doubletap_action", 1) != 1) Launcher.LauncherDoubleTapHook(lpparam);
		if (mPrefs.getInt("launcher_folder_cols", 1) > 1) Launcher.FolderColumnsHook(lpparam);
		if (mPrefs.getInt("launcher_iconscale", 45) > 45) Launcher.IconScaleHook(lpparam);
		if (mPrefs.getInt("launcher_titlefontsize", 5) > 5) Launcher.TitleFontSizeHook(lpparam);
		if (mPrefs.getInt("launcher_titletopmargin", 0) > 0) Launcher.TitleTopMarginHook(lpparam);
		if (mPrefs.getInt("system_statusbarheight", 19) > 19) Launcher.StatusBarHeightHook(lpparam);
		if (mPrefs.getBoolean("launcher_noclockhide")) Launcher.NoClockHideHook(lpparam);
		if (mPrefs.getBoolean("launcher_renameapps")) Launcher.RenameShortcutsHook(lpparam);
		if (mPrefs.getBoolean("launcher_darkershadow")) Launcher.TitleShadowHook(lpparam);
		if (mPrefs.getBoolean("controls_nonavbar")) Launcher.HideNavBarHook(lpparam);
		if (mPrefs.getBoolean("launcher_infinitescroll")) Launcher.InfiniteScrollHook(lpparam);
		if (mPrefs.getBoolean("launcher_hidetitles")) Launcher.HideTitlesHook(lpparam);
		if (mPrefs.getBoolean("launcher_fixlaunch")) Launcher.FixAppInfoLaunchHook(lpparam);
		if (mPrefs.getBoolean("launcher_nowidgetonly")) Launcher.NoWidgetOnlyHook(lpparam);
		if (mPrefs.getStringAsInt("launcher_foldershade", 1) > 1) Launcher.FolderShadeHook(lpparam);
		if (mPrefs.getStringAsInt("launcher_closefolders", 1) > 1) Launcher.CloseFolderOnLaunchHook(lpparam);
		if (lpparam.packageName.equals("com.miui.home")) {
			if (mPrefs.getBoolean("controls_fsg_horiz")) Launcher.FSGesturesHook(lpparam);
			if (mPrefs.getBoolean("launcher_fixstatusbarmode")) Launcher.FixStatusBarModeHook(lpparam);
			if (mPrefs.getBoolean("launcher_fixanim")) Launcher.FixAnimHook(lpparam);
			if (mPrefs.getBoolean("launcher_hideseekpoints")) Launcher.HideSeekPointsHook(lpparam);
			if (mPrefs.getBoolean("launcher_privacyapps_gest")) Launcher.PrivacyFolderHook(lpparam);
			if (mPrefs.getBoolean("launcher_googlediscover")) Launcher.GoogleDiscoverHook(lpparam);
			if (mPrefs.getBoolean("launcher_docktitles") && mPrefs.getInt("launcher_bottommargin", 0) == 0) Launcher.ShowHotseatTitlesHook(lpparam);
			if (mPrefs.getBoolean("launcher_folderblur")) Launcher.FolderBlurHook(lpparam);
			if (mPrefs.getInt("launcher_bottommargin", 0) > 0) Launcher.BottomSpacingHook(lpparam);
		}
		//if (!mPrefs.getString("system_clock_app", "").equals("")) Launcher.ReplaceClockAppHook(lpparam);
		//if (!mPrefs.getString("system_calendar_app", "").equals("")) Launcher.ReplaceCalendarAppHook(lpparam);
		//Launcher.NoInternationalBuildHook(lpparam);
	}

}