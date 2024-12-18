package name.mikanoshi.customiuizer;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.util.Map;

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
import name.mikanoshi.customiuizer.mods.SystemUI;
import name.mikanoshi.customiuizer.mods.Various;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;
import name.mikanoshi.customiuizer.utils.PrefMap;
import name.mikanoshi.customiuizer.utils.ResourceHooks;

public class MainModule implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    public static PrefMap<String, Object> mPrefs = new PrefMap<String, Object>();
    public static ResourceHooks resHooks;

    public void initZygote(StartupParam startParam) {
        //long startTime = SystemClock.elapsedRealtime();
        if (mPrefs.size() == 0) {
            XSharedPreferences pref = null;
            try {
                if (XposedBridge.getXposedVersion() >= 93)
                    pref = new XSharedPreferences(Helpers.modulePkg, Helpers.prefsName);
                else
                    pref = new XSharedPreferences(new File(Helpers.prefsFile));
                pref.makeWorldReadable();
            } catch (Throwable t) {
                XposedBridge.log(t);
            }

            Map<String, ?> allPrefs = pref == null ? null : pref.getAll();
            if (allPrefs == null || allPrefs.size() == 0)
                Helpers.log("[UID " + android.os.Process.myUid() +"] Cannot read module's SharedPreferences, some mods might not work!");
            else
                mPrefs.putAll(allPrefs);
        }

        resHooks = new ResourceHooks();

        if (mPrefs.getInt("system_statusbarheight", 19) > 19) System.StatusBarHeightRes();
        if (mPrefs.getInt("controls_navbarheight", 19) > 19) Controls.NavbarHeightRes();
        if (mPrefs.getBoolean("system_compactnotif")) System.CompactNotificationsRes();
        if (mPrefs.getStringAsInt("system_allrotations2", 1) > 1) System.AllRotationsRes();
        if (mPrefs.getStringAsInt("system_rotateanim", 1) > 1) System.RotationAnimationRes();

        if (mPrefs.getInt("system_betterpopups_delay", 0) > 0 && !mPrefs.getBoolean("system_betterpopups_nohide")) System.BetterPopupsHideDelaySysHook();
        if (mPrefs.getInt("system_messagingstylelines", 0) > 0) System.MessagingStyleLinesSysHook();
        if (mPrefs.getBoolean("system_nopassword")) System.NoPasswordHook();
        if (mPrefs.getBoolean("system_magnifier")) System.TextMagnifierHook();
        if (mPrefs.getBoolean("system_lockscreenshortcuts")) SystemUI.LockScreenSecureLaunchHook();
        if (mPrefs.getBoolean("system_notifmediaseekbar")) System.MediaNotificationSeekBarHook();
        if (mPrefs.getBoolean("system_cleanshare")) System.CleanShareMenuHook();
        if (mPrefs.getBoolean("system_cleanopenwith")) System.CleanOpenWithMenuHook();
        if (mPrefs.getBoolean("system_allownotifonkeyguard")) System.AllowAllKeyguardSysHook();
        if (mPrefs.getBoolean("system_allownotiffloat")) System.AllowAllFloatSysHook();
        if (mPrefs.getBoolean("system_resizablewidgets")) System.ResizableWidgetsHook();
        if (mPrefs.getBoolean("system_audiosilencer")) System.AudioSilencerHook();

        if (mPrefs.getStringAsInt("controls_volumemedia_up", 0) > 0 ||
            mPrefs.getStringAsInt("controls_volumemedia_down", 0) > 0) {
            Controls.VolumeMediaPlayerHook();
        }
//        GlobalActions.setupSystemHelpers();
        //Helpers.log("initZygote", String.valueOf(SystemClock.elapsedRealtime() - startTime));
    }

    public void handleLoadPackage(final LoadPackageParam lpparam) {
        String pkg = lpparam.packageName;

        if (pkg.equals("com.baidu.input")
            || pkg.equals("com.baidu.input_mi")
            || pkg.equals("com.iflytek.inputmethod")
            || pkg.equals("com.iflytek.inputmethod.miui")
            || pkg.equals("com.sohu.inputmethod.sogou")
            || pkg.equals("com.sohu.inputmethod.sogou.xiaomi")
            || pkg.startsWith("com.google.android.inputmethod")
            || pkg.startsWith("com.touchtype.swiftkey")
            || pkg.startsWith("com.tencent.wetype")
        ) {
            if (mPrefs.getBoolean("controls_volumecursor")) Controls.VolumeCursorHook();
            if (mPrefs.getBoolean("controls_nonavbar_fix_inputmethod")
                && mPrefs.getBoolean("controls_nonavbar")) {
                Various.FixInputMethodBottomMarginHook(lpparam);
            }
        }

        if (mPrefs.getBoolean("various_alarmcompat") && mPrefs.getStringSet("various_alarmcompat_apps").contains(pkg)) {
            Various.AlarmCompatHook();
        }

        if (pkg.equals("android") && lpparam.processName.equals("android")) {
            PackagePermissions.hook(lpparam);
            GlobalActions.setupGlobalActions(lpparam);

            if (mPrefs.getBoolean("system_screenshot_overlay")) {
                System.TempHideOverlayAppHook(lpparam);
            }

            if (mPrefs.getBoolean("system_notify_openinfw")
                || mPrefs.getBoolean("system_fw_forcein_actionsend")
                || mPrefs.getBoolean("system_betterpopups_allowfloat")
            ) {
                System.OpenAppInFreeFormHook(lpparam);
            }

//            if (mPrefs.getInt("controls_fingerprint1_action", 1) > 1 ||
//                    mPrefs.getInt("controls_fingerprint2_action", 1) > 1 ||
//                    mPrefs.getInt("controls_fingerprintlong_action", 1) > 1 ||
//                    mPrefs.getStringAsInt("controls_fingerprint_accept", 1) > 1 ||
//                    mPrefs.getStringAsInt("controls_fingerprint_reject", 1) > 1 ||
//                    mPrefs.getStringAsInt("controls_fingerprint_hangup", 1) > 1) Controls.FingerprintEventsHook(lpparam);
            if (mPrefs.getInt("controls_backlong_action", 1) > 1 ||
                    mPrefs.getInt("controls_homelong_action", 1) > 1 ||
                    mPrefs.getInt("controls_menulong_action", 1) > 1) Controls.NavBarActionsHook(lpparam);
            if (mPrefs.getInt("controls_powerdt_action", 1) > 1 || mPrefs.getBoolean("controls_volumedowndt_torch")) Controls.PowerDoubleTapActionHook(lpparam);
            if (mPrefs.getInt("system_screenanim_duration", 0) > 0) System.ScreenAnimHook(lpparam);
            if (mPrefs.getInt("system_volumesteps", 0) > 0) System.VolumeStepsHook(lpparam);
            if (mPrefs.getInt("system_applock_timeout", 1) > 1) System.AppLockTimeoutHook(lpparam);
            if (mPrefs.getInt("system_dimtime", 0) > 0) System.ScreenDimTimeHook(lpparam);
            if (mPrefs.getInt("system_toasttime", 0) > 0) System.ToastTimeHook(lpparam);
            if (!mPrefs.getString("system_defaultusb", "none").equals("none")) System.USBConfigHook(lpparam);
            if (!Helpers.isTPlus()) {
                if (mPrefs.getBoolean("system_nolightuponheadset")) System.NoLightUpOnHeadsetHook(lpparam);
            }
            if (mPrefs.getBoolean("system_removesecure")) System.RemoveSecureHook(lpparam);
            if (mPrefs.getBoolean("system_remove_startactconfirm")) System.RemoveActStartConfirmHook(lpparam);
            if (mPrefs.getBoolean("system_securelock")) System.EnhancedSecurityHook(lpparam);
            if (mPrefs.getBoolean("system_separatevolume")) System.NotificationVolumeServiceHook(lpparam);
            if (mPrefs.getBoolean("system_downgrade")) System.NoVersionCheckHook(lpparam);
            if (mPrefs.getBoolean("system_orientationlock")) System.OrientationLockHook(lpparam);
            if (mPrefs.getBoolean("system_noducking")) System.NoDuckingHook(lpparam);
            if (mPrefs.getBoolean("system_cleanshare")) System.CleanShareMenuServiceHook(lpparam);
            if (mPrefs.getBoolean("system_cleanopenwith")) System.CleanOpenWithMenuServiceHook(lpparam);
            if (mPrefs.getBoolean("system_autobrightness")) System.AutoBrightnessRangeHook(lpparam);
            if (mPrefs.getBoolean("system_lockscreen_disable_strongauth_72h")) System.Disable72hStrongAuthHook(lpparam);
            if (mPrefs.getBoolean("system_applock")) System.AppLockHook(lpparam);
            if (mPrefs.getBoolean("system_applock_skip")) System.SkipAppLockHook(lpparam);
            if (mPrefs.getBoolean("various_alarmcompat")) Various.AlarmCompatServiceHook(lpparam);
            if (mPrefs.getBoolean("system_ignorecalls")) System.NoCallInterruptionHook(lpparam);
            if (mPrefs.getBoolean("system_forceclose")) System.ForceCloseHook(lpparam);
            if (mPrefs.getBoolean("system_hideproxywarn")) System.HideProximityWarningHook(lpparam);
            if (mPrefs.getBoolean("system_firstpress")) System.FirstVolumePressHook(lpparam);
            if (mPrefs.getBoolean("system_apksign")) System.NoSignatureVerifyServiceHook(lpparam);
            if (mPrefs.getBoolean("system_disableintegrity")) System.DisableSystemIntegrityHook(lpparam);
            if (mPrefs.getBoolean("system_vibration_amp")) System.MuffledVibrationHook(lpparam);
            if (mPrefs.getBoolean("system_clearalltasks")) System.ClearAllTasksHook(lpparam);
            if (mPrefs.getBoolean("system_nodarkforce")) System.NoDarkForceHook(lpparam);
            if (mPrefs.getBoolean("system_audiosilencer")) System.AudioSilencerServiceHook(lpparam);
            if (mPrefs.getBoolean("system_fw_sticky")) System.StickyFloatingWindowsHook(lpparam);
            if (mPrefs.getBoolean("system_lswallpaper")) System.SetLockscreenWallpaperHook(lpparam);
            if (mPrefs.getBoolean("controls_powerflash")) Controls.PowerKeyHook(lpparam);
            if (mPrefs.getBoolean("controls_fingerprintfailure")) Controls.FingerprintHapticFailureHook(lpparam);
            if (mPrefs.getBoolean("controls_fingerprintscreen")) Controls.FingerprintScreenOnHook(lpparam);
            if (mPrefs.getBoolean("controls_fingerprintwake")) Controls.NoFingerprintWakeHook(lpparam);
            if (mPrefs.getBoolean("various_disableapp")) Various.AppsDisableServiceHook(lpparam);
            if (mPrefs.getBoolean("system_disableanynotif")) System.DisableAnyNotificationBlockHook(lpparam);
            if (mPrefs.getStringAsInt("system_allrotations2", 1) > 1) System.AllRotationsHook(lpparam);
            if (mPrefs.getStringAsInt("system_nolightuponcharges", 1) > 1) System.NoLightUpOnChargeHook(lpparam);
            if (mPrefs.getStringAsInt("system_autogroupnotif", 1) > 1) System.AutoGroupNotificationsHook(lpparam);
            if (mPrefs.getStringAsInt("system_vibration", 1) > 1) System.SelectiveVibrationHook(lpparam);
            if (mPrefs.getStringAsInt("system_blocktoasts", 1) > 1) System.SelectiveToastsHook(lpparam);
            if (mPrefs.getStringAsInt("system_rotateanim", 1) > 1) System.RotationAnimationHook(lpparam);
            if (mPrefs.getStringAsInt("controls_fingerprintsuccess", 1) > 1) Controls.FingerprintHapticSuccessHook(lpparam);
            if (mPrefs.getStringAsInt("controls_volumemedia_up", 0) > 0 ||
                    mPrefs.getStringAsInt("controls_volumemedia_down", 0) > 0) Controls.VolumeMediaButtonsHook(lpparam);

            if (mPrefs.getBoolean("system_fw_splitscreen")) System.MultiWindowPlusHook(lpparam);
            if (mPrefs.getBoolean("system_fw_noblacklist")) System.NoFloatingWindowBlacklistHook(lpparam);
            if (mPrefs.getBoolean("various_disable_access_devicelogs")) {
                System.NoAccessDeviceLogsRequest(lpparam);
            }
            if (mPrefs.getInt("system_other_wallpaper_scale", 6) > 6) System.WallpaperScaleLevelHook(lpparam);
        }

        if (pkg.equals("com.miui.miwallpaper")) {
            if (mPrefs.getBoolean("launcher_disable_wallpaperscale")) Launcher.DisableUnlockWallpaperScale(lpparam);
        }
        if (pkg.equals("com.android.systemui")) {
            SystemUI.setupStatusBar(lpparam);
            GlobalActions.setupStatusBar(lpparam);
            if (mPrefs.getStringAsInt("various_showcallui", 0) > 0
                || mPrefs.getBoolean("controls_volumecursor")
            ) GlobalActions.setupForegroundMonitor(lpparam);

            if (mPrefs.getBoolean("system_screenshot_overlay")) {
                SystemUI.TempHideOverlaySystemUIHook(lpparam);
            }

            if (
                mPrefs.getBoolean("system_fivegtile")
                || mPrefs.getBoolean("system_cc_fpstile")
            ) {
                SystemUI.AddCustomTileHook(lpparam);
            }

            if (mPrefs.getBoolean("system_hidestatusbar_whenscreenshot")) {
                SystemUI.HideStatusBarBeforeScreenshotHook(lpparam);
            }

            if (mPrefs.getInt("system_qsgridcolumns", 2) > 2 || mPrefs.getInt("system_qsgridrows", 1) > 1) SystemUI.QSGridRes();
            if (mPrefs.getInt("system_qqsgridcolumns", 2) > 2) SystemUI.QQSGridRes();
            if (mPrefs.getBoolean("system_networkindicator_wifi")) System.NetworkIndicatorWifi(lpparam);

            if (mPrefs.getInt("system_drawer_blur", 100) < 100) System.DrawerBlurRatioHook(lpparam);
            if (mPrefs.getInt("system_chargeanimtime", 20) < 20) System.ChargeAnimationHook(lpparam);
            if (mPrefs.getInt("system_betterpopups_delay", 0) > 0 && !mPrefs.getBoolean("system_betterpopups_nohide")) System.BetterPopupsHideDelayHook(lpparam);
            if (mPrefs.getInt("system_netspeedinterval", 4) != 4) SystemUI.NetSpeedIntervalHook(lpparam);
            if (mPrefs.getInt("system_qsgridrows", 1) > 1 || mPrefs.getBoolean("system_qsnolabels")) SystemUI.QSGridLabelsHook(lpparam);
            if (mPrefs.getInt("system_lstimeout", 3) > 3) System.LockScreenTimeoutHook(lpparam);
            if (mPrefs.getInt("controls_fsg_assist_action", 1) > 1) Controls.AssistGestureActionHook(lpparam);
            if (mPrefs.getInt("controls_navbarleft_action", 1) > 1 ||
                    mPrefs.getInt("controls_navbarleftlong_action", 1) > 1 ||
                    mPrefs.getInt("controls_navbarright_action", 1) > 1 ||
                    mPrefs.getInt("controls_navbarrightlong_action", 1) > 1) Controls.NavBarButtonsHook(lpparam);
            if (mPrefs.getInt("system_recommended_first_action", 1) > 1 ||
                    mPrefs.getInt("system_recommended_second_action", 1) > 1 ||
                    mPrefs.getInt("system_recommended_third_action", 1) > 1 ||
                    mPrefs.getInt("system_recommended_fourth_action", 1) > 1) System.CustomRecommendedHook(lpparam, false);
            if (mPrefs.getBoolean("system_scramblepin")) System.ScramblePINHook(lpparam);
            if (mPrefs.getBoolean("system_dttosleep")) System.DoubleTapToSleepHook(lpparam);
            if (mPrefs.getBoolean("system_statusbar_clocktweak")
                || mPrefs.getBoolean("system_cc_clocktweak")
                || mPrefs.getBoolean("system_cc_hidedate")
                || mPrefs.getString("system_cc_dateformat", "").length() > 0
            ) System.StatusBarClockTweakHook(lpparam);
            if (mPrefs.getBoolean("system_noscreenlock_act")) System.NoScreenLockHook(lpparam);
            if (
                mPrefs.getBoolean("system_detailednetspeed")
                && !mPrefs.getBoolean("system_detailednetspeed_fakedualrow")
            ) SystemUI.DetailedNetSpeedHook(lpparam);
            if (mPrefs.getBoolean("system_albumartonlock")) SystemUI.LockScreenAlbumArtHook(lpparam);
            if (mPrefs.getStringAsInt("system_expandheadups", 1) > 1) System.ExpandHeadsUpHook(lpparam);
            if (mPrefs.getBoolean("system_betterpopups_nohide")) System.BetterPopupsNoHideHook(lpparam);
            if (mPrefs.getBoolean("system_betterpopups_swipedown")) System.BetterPopupsSwipeDownHook(lpparam);
            if (mPrefs.getBoolean("system_betterpopups_center")) System.BetterPopupsCenteredHook(lpparam);
            if (mPrefs.getBoolean("system_hidemoreicon")) System.NoMoreIconHook(lpparam);
            if (mPrefs.getBoolean("system_notifafterunlock")) System.ShowNotificationsAfterUnlockHook(lpparam);
            if (mPrefs.getBoolean("system_notifrowmenu")) System.NotificationRowMenuHook(lpparam);
            if (mPrefs.getBoolean("system_compactnotif")) System.CompactNotificationsHook(lpparam);
            if (mPrefs.getBoolean("system_removedismiss")) SystemUI.HideDismissViewHook(lpparam);
            if (mPrefs.getBoolean("system_drawer_removeshortcut")) SystemUI.HideNoficationAccessIconHook(lpparam);
            if (mPrefs.getBoolean("controls_nonavbar")) Controls.HideNavBarHook(lpparam);
            else if (mPrefs.getBoolean("controls_hidenavbar_whenscreenshot")) SystemUI.HideNavBarBeforeScreenshotHook(lpparam);
            if (mPrefs.getBoolean("controls_imebackalticon")) Controls.ImeBackAltIconHook(lpparam);
            if (mPrefs.getBoolean("system_visualizer")) System.AudioVisualizerHook(lpparam);
            if (mPrefs.getBoolean("system_nosilentvibrate")
                || mPrefs.getBoolean("system_qs_force_systemfonts")
                || mPrefs.getBoolean("system_volumetimer")
                || mPrefs.getBoolean("system_qsnolabels")
                || mPrefs.getBoolean("system_cc_volume_showpct")
                || mPrefs.getBoolean("system_volumebar_blur_mtk")
                || mPrefs.getBoolean("system_cc_hidedate")
                || mPrefs.getBoolean("system_cc_hide_shortcuticons")
                || mPrefs.getBoolean("system_cc_clocktweak")
                || mPrefs.getBoolean("system_cc_tile_roundedrect")
                || (mPrefs.getBoolean("system_separatevolume") && mPrefs.getBoolean("system_separatevolume_slider"))
                || (mPrefs.getInt("system_volumedialogdelay_collapsed", 0) > 0 || mPrefs.getInt("system_volumedialogdelay_expanded", 0) > 0)
                || (mPrefs.getInt("system_volumeblur_collapsed", 0) > 0 || mPrefs.getInt("system_volumeblur_expanded", 0) > 0)
            ) {
                SystemUI.MIUIVolumeDialogHook(lpparam);
            }
            if (mPrefs.getBoolean("system_batteryindicator")) SystemUI.BatteryIndicatorHook(lpparam);
            if (mPrefs.getBoolean("system_disableanynotif")) System.DisableAnyNotificationHook(lpparam);
            if (mPrefs.getBoolean("system_lockscreenshortcuts")) SystemUI.LockScreenShortcutHook(lpparam);
            if (mPrefs.getBoolean("system_4gtolte")
                || (mPrefs.getBoolean("system_statusbar_mobiletype_single") &&
                    !mPrefs.getString("system_statusbar_mobile_showname", "").equals(""))
            ) System.MobileNetworkTypeHook(lpparam);
            boolean moveRight = mPrefs.getBoolean("system_statusbar_netspeed_atright")
                || mPrefs.getBoolean("system_statusbar_alarm_atright")
                || mPrefs.getBoolean("system_statusbar_sound_atright")
                || mPrefs.getBoolean("system_statusbar_dnd_atright")
                || mPrefs.getBoolean("system_statusbar_nfc_atright")
                || mPrefs.getBoolean("system_statusbar_btbattery_atright")
                || mPrefs.getBoolean("system_statusbar_headset_atright");
            boolean moveLeft = mPrefs.getBoolean("system_statusbar_alarm_atleft")
                || mPrefs.getBoolean("system_statusbar_sound_atleft")
                || mPrefs.getBoolean("system_statusbar_dnd_atleft")
                || mPrefs.getBoolean("system_statusbar_gps_atleft");
            if (moveRight || moveLeft
                || mPrefs.getBoolean("system_statusbar_netspeed_atleft")
                || (mPrefs.getBoolean("system_statusbar_dualrows") && mPrefs.getBoolean("system_statusbar_netspeed_atsecondrow"))
                || mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft")
                || mPrefs.getBoolean("system_statusbaricons_swap_wifi_mobile")
            ) {
                SystemUI.StatusBarIconsPositionAdjustHook(lpparam, moveRight, moveLeft);
            }
            if (mPrefs.getStringAsInt("system_statusbar_clock_position", 1) > 1 && !mPrefs.getBoolean("system_statusbar_dualrows")) {
                SystemUI.StatusBarClockPositionHook(lpparam);
            }
            if (mPrefs.getBoolean("system_statusbar_batterystyle")) {
                SystemUI.StatusBarStyleBatteryIconHook(lpparam);
            }
            if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent")
                || mPrefs.getBoolean("system_statusbar_showdevicetemperature")
            ) SystemUI.MonitorDeviceInfoHook(lpparam);
            if (mPrefs.getBoolean("system_statusbar_topmargin") && mPrefs.getBoolean("system_statusbar_topmargin_unset_lockscreen")) SystemUI.LockScreenTopMarginHook(lpparam);
            if (mPrefs.getBoolean("system_statusbar_horizmargin")) SystemUI.HorizMarginHook(lpparam);
            if (mPrefs.getBoolean("system_showpct")) SystemUI.BrightnessPctHook(lpparam);
            if (mPrefs.getBoolean("system_hidelsstatusbar")) System.HideLockScreenStatusBarHook(lpparam);
            if (mPrefs.getBoolean("system_hidelsclock")) System.HideLockScreenClockHook(lpparam);
            if (mPrefs.getBoolean("system_ls_force_systemfonts")) SystemUI.ForceClockUseSystemFontsHook(lpparam);
            if (mPrefs.getBoolean("system_hidelshint")) System.HideLockScreenHintHook(lpparam);
            if (mPrefs.getBoolean("system_allowdirectreply")) System.AllowDirectReplyHook(lpparam);
            if (mPrefs.getBoolean("system_allownotifonkeyguard")) System.AllowAllKeyguardHook(lpparam);
            if (mPrefs.getBoolean("system_allownotiffloat")) System.AllowAllFloatHook(lpparam);
            if (mPrefs.getBoolean("system_hideqs")) System.HideQSHook(lpparam);
            if (mPrefs.getBoolean("system_lsalarm")) System.LockScreenAlarmHook(lpparam);
            if (mPrefs.getBoolean("system_statusbarcontrols")) SystemUI.StatusBarGesturesHook(lpparam);
            if (mPrefs.getBoolean("system_nonetspeedseparator")) SystemUI.NoNetworkSpeedSeparatorHook(lpparam);
            if (mPrefs.getBoolean("system_statusbaricons_clock")) SystemUI.HideIconsClockHook(lpparam);
            if (mPrefs.getBoolean("system_detailednetspeed_fakedualrow")
                || (!mPrefs.getBoolean("system_detailednetspeed")
                    && (mPrefs.getBoolean("system_detailednetspeed_secunit")
                        || mPrefs.getBoolean("system_detailednetspeed_low")
                        )
                    )
            ) {
                SystemUI.FormatNetworkSpeedHook(lpparam);
            }
            if (
                mPrefs.getInt("system_netspeed_fontsize", 13) > 13
                || mPrefs.getInt("system_netspeed_verticaloffset", 8) != 8
                || mPrefs.getBoolean("system_detailednetspeed")
                || mPrefs.getBoolean("system_detailednetspeed_fakedualrow")
                || mPrefs.getBoolean("system_netspeed_bold")
                || mPrefs.getInt("system_netspeed_leftmargin", 0) > 0
                || mPrefs.getInt("system_netspeed_fixedcontent_width", 10) > 10
                || mPrefs.getInt("system_netspeed_rightmargin", 0) > 0
                || mPrefs.getStringAsInt("system_detailednetspeed_align", 1) > 1
            ) {
                SystemUI.NetSpeedStyleHook(lpparam);
            }
            if (mPrefs.getBoolean("system_taptounlock")) System.TapToUnlockHook(lpparam);
            if (mPrefs.getBoolean("system_nosos")) System.NoSOSHook(lpparam);
            if (mPrefs.getBoolean("system_morenotif")) System.MoreNotificationsHook(lpparam);
            if (mPrefs.getBoolean("system_charginginfo")) System.ChargingInfoHook(lpparam);
            if (mPrefs.getBoolean("system_secureqs")) SystemUI.SecureQSTilesHook(lpparam);
            if (mPrefs.getBoolean("system_mutevisiblenotif")) System.MuteVisibleNotificationsHook(lpparam);
            if (mPrefs.getBoolean("system_statusbaricons_battery1")) System.HideIconsBattery1Hook(lpparam);
            if (mPrefs.getBoolean("system_statusbaricons_battery3") || mPrefs.getBoolean("system_statusbaricons_battery4")) System.HideIconsBattery2Hook(lpparam);
            if (mPrefs.getStringAsInt("system_statusbaricons_wifistandard", 1) > 1) System.DisplayWifiStandardHook(lpparam);
            if (mPrefs.getBoolean("system_statusbaricons_signal")
                || mPrefs.getBoolean("system_statusbaricons_sim1")
                || mPrefs.getBoolean("system_statusbaricons_sim2")
                || mPrefs.getBoolean("system_statusbaricons_roaming")
                || mPrefs.getBoolean("system_statusbaricons_volte")
            ) SystemUI.HideIconsSignalHook(lpparam);
            if (mPrefs.getBoolean("system_statusbaricons_vowifi")) SystemUI.HideIconsVoWiFiHook(lpparam);
            if (!mPrefs.getBoolean("system_statusbaricons_alarm") && mPrefs.getInt("system_statusbaricons_alarmn", 0) > 0) System.HideIconsSelectiveAlarmHook(lpparam);
            if (!mPrefs.getString("system_shortcut_app", "").equals("")
                || !mPrefs.getString("system_calendar_app", "").equals("")
                || !mPrefs.getString("system_clock_app", "").equals("")) SystemUI.ReplaceShortcutAppHook(lpparam);
            if (mPrefs.getStringAsInt("system_qshaptics", 1) > 1) System.QSHapticHook(lpparam);
            if (mPrefs.getBoolean("system_qs_hideoperator")) System.HideCCOperatorHook(lpparam);
            if (mPrefs.getBoolean("system_cc_hideoperator_delimiter")) System.HideCCOperatorDelimiterHook(lpparam);
            if (mPrefs.getBoolean("system_cc_show_stepcount")) SystemUI.ShowCCStepCountHook(lpparam);
            if (mPrefs.getBoolean("system_cc_disable_bluetooth_restrict")) System.DisableBluetoothRestrictHook(lpparam);
            if (mPrefs.getBoolean("system_cc_collapse_after_clicked")) System.CollapseCCAfterClickHook(lpparam);
            if (mPrefs.getBoolean("system_cc_switch_qsandnotification")) SystemUI.SwitchCCAndNotificationHook(lpparam);
            if (mPrefs.getStringAsInt("system_expandnotifs", 1) > 1) System.ExpandNotificationsHook(lpparam);
            if (mPrefs.getStringAsInt("system_inactivebrightness", 1) > 1) System.InactiveBrightnessSliderHook(lpparam);
            if (mPrefs.getStringAsInt("system_mobiletypeicon", 1) > 1
                || mPrefs.getBoolean("system_networkindicator_mobile")
            ) {
                System.HideNetworkIndicatorHook(lpparam);
            }
            if (mPrefs.getStringAsInt("system_statusbaricons_bluetooth", 1) > 1) System.HideIconsBluetoothHook(lpparam);
            if (mPrefs.getBoolean("system_epm")) SystemUI.ExtendedPowerMenuHook(lpparam);

            boolean hideIconsActive =
                mPrefs.getBoolean("system_statusbaricons_wifi") ||
                mPrefs.getBoolean("system_statusbaricons_dualwifi") ||
                mPrefs.getBoolean("system_statusbaricons_alarm") ||
                mPrefs.getBoolean("system_statusbaricons_profile") ||
                mPrefs.getBoolean("system_statusbaricons_sound") ||
                mPrefs.getBoolean("system_statusbaricons_dnd") ||
                mPrefs.getBoolean("system_statusbaricons_headset") ||
                mPrefs.getBoolean("system_statusbaricons_nfc") ||
                mPrefs.getBoolean("system_statusbaricons_vpn") ||
                mPrefs.getBoolean("system_statusbaricons_hotspot") ||
                mPrefs.getBoolean("system_statusbaricons_nosims") ||
                mPrefs.getBoolean("system_statusbaricons_gps") ||
                mPrefs.getBoolean("system_statusbaricons_btbattery") ||
                mPrefs.getBoolean("system_statusbaricons_ble_unlock") ||
                mPrefs.getBoolean("system_statusbaricons_volte");
            if (hideIconsActive) SystemUI.HideIconsHook(lpparam);

            if (
                mPrefs.getBoolean("system_statusbaricons_privacy")
                || mPrefs.getBoolean("system_statusbaricons_mute")
                || mPrefs.getBoolean("system_statusbaricons_speaker")
                || mPrefs.getBoolean("system_statusbaricons_record")
            ) SystemUI.HideIconsFromSystemManager(lpparam);
            if (mPrefs.getInt("system_messagingstylelines", 0) > 0) System.MessagingStyleLinesHook(lpparam);
            if (mPrefs.getBoolean("system_betterpopups_allowfloat")) System.BetterPopupsAllowFloatHook(lpparam);
            if (mPrefs.getBoolean("system_betterpopups_autoclose_expanded")) System.AutoDismissExpandedPopupsHook(lpparam);
            if (mPrefs.getBoolean("system_betterpopups_disablewhenmute")) SystemUI.DisableHeadsUpWhenMuteHook(lpparam);
            if (mPrefs.getBoolean("system_securecontrolcenter")) System.SecureControlCenterHook(lpparam);
            if (mPrefs.getBoolean("system_minimalnotifview")) System.MinimalNotificationViewHook(lpparam);
            if (mPrefs.getBoolean("system_notifchannelsettings")) System.NotificationChannelSettingsHook(lpparam);
            if (mPrefs.getStringAsInt("system_maxsbicons", 0) != 0) System.MaxNotificationIconsHook(lpparam);
            if (mPrefs.getBoolean("system_statusbar_mobiletype_single")) {
                SystemUI.MobileTypeSingleHook(lpparam);
            }
            if (mPrefs.getBoolean("system_statusbar_dualsimin2rows")) {
                SystemUI.DualRowSignalHook(lpparam);
            }
            if (mPrefs.getBoolean("system_statusbar_dualrows")) {
                SystemUI.DualRowStatusbarHook(lpparam);
            }
            if (mPrefs.getInt("system_ccgridcolumns", 4) > 4 || mPrefs.getInt("system_ccgridrows", 4) != 4) SystemUI.SystemCCGridHook(lpparam);
            if (mPrefs.getStringAsInt("system_colorizenotifs", 1) > 1) System.ColorizeNotificationCardHook(lpparam);
            if (mPrefs.getBoolean("system_notify_openinfw")) SystemUI.OpenNotifyInFloatingWindowHook(lpparam);
            if (mPrefs.getBoolean("system_fw_noblacklist")) System.DisableSideBarSuggestionHook(lpparam);

            if (mPrefs.getBoolean("system_notify_openinfw")
                || mPrefs.getBoolean("system_notifrowmenu")
                || mPrefs.getBoolean("system_betterpopups_allowfloat")
            ) {
                SystemUI.FixOpenNotifyInFreeFormHook(lpparam);
            }
            if (mPrefs.getBoolean("system_nosafevolume")) {
                SystemUI.HideSafeVolumeDlgHook(lpparam);
            }
            if (mPrefs.getBoolean("system_lockscreen_hidezenmode")) {
                SystemUI.HideLockscreenZenModeHook(lpparam);
            }
        }

        if (pkg.equals(Helpers.modulePkg)) {
            GlobalActions.miuizerHook(lpparam);
        }

        if (pkg.equals("com.lbe.security.miui")) {
            if (mPrefs.getStringAsInt("various_clipboard_defaultaction", 1) > 1) {
                Various.SmartClipboardActionHook(lpparam);
            }
        }

        if (pkg.equals("com.android.incallui")) {
            if (mPrefs.getStringAsInt("various_showcallui", 0) > 0) Various.ShowCallUIHook(lpparam);
            if (mPrefs.getBoolean("various_calluibright")) Various.InCallBrightnessHook(lpparam);
            if (mPrefs.getBoolean("various_answerinheadup")) Various.AnswerCallInHeadUpHook(lpparam);
        }

        if (pkg.equals("com.miui.securitycenter")) {
            if (mPrefs.getBoolean("various_appdetails")) Various.AppInfoHook(lpparam);
            if (mPrefs.getBoolean("various_disableapp")) Various.AppsDisableHook(lpparam);
            if (mPrefs.getBoolean("various_restrictapp")) Various.AppsRestrictHook(lpparam);
            if (mPrefs.getBoolean("system_applock_scramblepin")) System.ScrambleAppLockPINHook(lpparam);
            if (mPrefs.getStringAsInt("various_appsort", 1) > 1) Various.AppsDefaultSortHook(lpparam);
            if (mPrefs.getStringAsInt("various_skip", 0) > 0) Various.AppsDefaultSortHook(lpparam);
            if (mPrefs.getBoolean("various_skip_interceptperm")) Various.InterceptPermHook(lpparam);
            if (mPrefs.getBoolean("various_skip_securityscan")) Various.SkipSecurityScanHook(lpparam);
            if (mPrefs.getBoolean("various_show_battery_temperature")) Various.ShowTempInBatteryHook(lpparam);
            if (mPrefs.getBoolean("various_enable_sc_ai_clipboard_location")) Various.UnlockClipboardAndLocationHook(lpparam);
            if (mPrefs.getBoolean("various_disable_freeform_suggest_blacklist")) System.DisableSideBarSuggestionHook(lpparam);
            if (mPrefs.getBoolean("various_disable_dock_suggest")) Various.DisableDockSuggestHook(lpparam);
            if (mPrefs.getBoolean("various_enable_expand_sidebar")) {
                Various.AddSideBarExpandReceiverHook(lpparam);
            }
            if (mPrefs.getBoolean("system_hidelowbatwarn")) {
                System.NoLowBatteryWarningHook();
            }
        }

        if (pkg.equals("com.miui.powerkeeper")) {
            if (mPrefs.getBoolean("various_restrictapp")) Various.AppsRestrictPowerHook(lpparam);
            if (mPrefs.getBoolean("various_persist_batteryoptimization")) Various.PersistBatteryOptimizationHook(lpparam);
        }

        if (pkg.equals("com.android.settings") && lpparam.processName.equals("com.android.settings")) {
            if (mPrefs.getStringAsInt("miuizer_settingsiconpos", 1) > 0) {
                GlobalActions.miuizerSettingsHook(lpparam);
            }
            if (mPrefs.getBoolean("system_separatevolume")) {
                System.NotificationVolumeSettingsRes();
                System.NotificationVolumeSettingsHook(lpparam);
            }
            if (mPrefs.getBoolean("system_disableanynotif")) {
                System.DisableAnyNotificationHook(lpparam);
                System.DisableAnyNotificationBlockHook(lpparam);
            }
            if (!mPrefs.getString("system_defaultusb", "none").equals("none")) System.USBConfigSettingsHook(lpparam);
            if (mPrefs.getBoolean("system_notifimportance")) {
                System.NotificationImportanceHook(lpparam);
            }
            if (mPrefs.getBoolean("system_wifipassword")) {
                System.ViewWifiPasswordHook(lpparam);
            }
        }

        if (pkg.equals("com.google.android.packageinstaller") || pkg.equals("com.android.packageinstaller")) {
            if (mPrefs.getBoolean("various_installappinfo")) Various.AppInfoDuringInstallHook(lpparam);
        }

        if (pkg.startsWith("com.google.android.inputmethod")) {
            if (mPrefs.getInt("various_gboardpadding_port", 0) > 0 || mPrefs.getInt("various_gboardpadding_land", 0) > 0) Various.GboardPaddingHook();
        }

        if (pkg.equals("com.miui.packageinstaller")) {
//            if (mPrefs.getBoolean("system_apksign")) System.NoSignatureVerifyMiuiHook(lpparam);
            if (mPrefs.getBoolean("various_miuiinstaller")) Various.MiuiPackageInstallerHook(lpparam);
            if (mPrefs.getBoolean("various_installappinfo")) Various.AppInfoDuringMiuiInstallHook(lpparam);
        }

        if (pkg.equals("com.miui.screenrecorder")) {
            if (mPrefs.getBoolean("various_unlockfps")) Various.ScreenRecorderFramerateHook(lpparam);
        }

        if (pkg.equals("com.miui.screenshot")) {
//            resHooks.setResReplacement(pkg, "array", "config_forbidenLongScreenshot", R.array.config_forbidenLongScreenshot);
            if (mPrefs.getBoolean("system_screenshot")) System.ScreenshotConfigHook(lpparam);
            if (mPrefs.getInt("system_screenshot_floattime", 0) > 0) System.ScreenshotFloatTimeHook(lpparam);
        }

        if (pkg.equals("com.miui.gallery")) {
            int folder = mPrefs.getStringAsInt("system_gallery_screenshots_path", 1);
            if (folder > 1) {
                System.GalleryScreenshotPathHook(lpparam);
            }
        }

        final boolean isMIUILauncherPkg = pkg.equals("com.miui.home");
        final boolean isLauncherPkg = isMIUILauncherPkg || pkg.equals("com.mi.android.globallauncher");
        final boolean isLauncherPerf = mPrefs.getBoolean("launcher_compat");
        final boolean isGoogleMinus = mPrefs.getBoolean("launcher_googleminus");
        final boolean isStatusBarColor = mPrefs.getBoolean("system_statusbarcolor") && mPrefs.getStringSet("system_statusbarcolor_apps").contains(pkg);
        final boolean isNoOverscroll = mPrefs.getBoolean("system_nooverscroll") && mPrefs.getStringSet("system_nooverscroll_apps").contains(pkg);

        if (isLauncherPkg) {
            if (mPrefs.getInt("launcher_horizmargin", 0) > 0) Launcher.HorizontalSpacingRes();
            if (mPrefs.getInt("launcher_topmargin", 0) > 0) Launcher.TopSpacingRes();
            if (mPrefs.getInt("launcher_bottommargin", 0) > 0) Launcher.BottomSpacingRes();
            if (mPrefs.getInt("launcher_indicatorheight", 9) > 9) Launcher.IndicatorHeightRes();
            if (mPrefs.getBoolean("launcher_unlockgrids")) {
                Launcher.UnlockGridsRes();
                Launcher.UnlockGridsHook(lpparam);
            }
            if (mPrefs.getBoolean("launcher_docktitles")) Launcher.ShowHotseatTitlesRes();
            if (mPrefs.getBoolean("launcher_disable_log")) {
                Launcher.DisableLauncherLogHook(lpparam);
            }
            if (isLauncherPerf) handleLoadLauncher(lpparam);
        }

        if ((isLauncherPkg && !isLauncherPerf) || (isMIUILauncherPkg && isGoogleMinus) || isStatusBarColor || isNoOverscroll)
            Helpers.findAndHookMethod(Application.class, "attach", Context.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    if (isLauncherPkg && !isLauncherPerf) handleLoadLauncher(lpparam);
                    if (isMIUILauncherPkg && isGoogleMinus) Launcher.GoogleMinusScreenHook(lpparam);
                    if (isStatusBarColor) {
                        System.StatusBarBackgroundCompatHook(lpparam);
                        System.StatusBarBackgroundHook(lpparam);
                    }
                    if (isNoOverscroll) System.NoOverscrollAppHook(lpparam);
                }
            });
    }

    private void handleLoadLauncher(final LoadPackageParam lpparam) {
        boolean closeOnLaunch = false;
        if (mPrefs.getInt("launcher_swipedown_action", 1) != 1 ||
                mPrefs.getInt("launcher_swipeup_action", 1) != 1 ||
                mPrefs.getInt("launcher_swipedown2_action", 1) != 1 ||
                mPrefs.getInt("launcher_swipeup2_action", 1) != 1) Launcher.HomescreenSwipesHook(lpparam);
        if (mPrefs.getInt("launcher_swipeleft_action", 1) != 1 ||
                mPrefs.getInt("launcher_swiperight_action", 1) != 1) Launcher.HotSeatSwipesHook(lpparam);
        if (mPrefs.getInt("launcher_shake_action", 1) != 1) Launcher.ShakeHook(lpparam);
        if (mPrefs.getInt("launcher_doubletap_action", 1) != 1) Launcher.LauncherDoubleTapHook(lpparam);
        if (mPrefs.getInt("launcher_pinch_action", 1) != 1) Launcher.LauncherPinchHook(lpparam);
        if (mPrefs.getInt("launcher_folder_cols", 1) > 1) Launcher.FolderColumnsHook(lpparam);
        if (mPrefs.getInt("launcher_iconscale", 45) > 45) Launcher.IconScaleHook(lpparam);
        if (mPrefs.getInt("launcher_titlefontsize", 5) > 5) Launcher.TitleFontSizeHook(lpparam);
        if (mPrefs.getInt("launcher_titletopmargin", 0) > 0) Launcher.TitleTopMarginHook(lpparam);
        if (mPrefs.getBoolean("launcher_noclockhide")) Launcher.NoClockHideHook(lpparam);
        if (mPrefs.getBoolean("launcher_renameapps")) Launcher.RenameShortcutsHook(lpparam);
        if (mPrefs.getBoolean("launcher_darkershadow")) Launcher.TitleShadowHook(lpparam);
        if (mPrefs.getBoolean("controls_nonavbar")) Launcher.HideNavBarHook(lpparam);
        if (mPrefs.getBoolean("launcher_infinitescroll")) Launcher.InfiniteScrollHook(lpparam);
        if (mPrefs.getBoolean("launcher_hidetitles")) Launcher.HideTitlesHook(lpparam);
        if (mPrefs.getBoolean("launcher_fixlaunch")) Launcher.FixAppInfoLaunchHook(lpparam);
        if (mPrefs.getBoolean("launcher_nowidgetonly")) Launcher.NoWidgetOnlyHook(lpparam);
        if (mPrefs.getBoolean("launcher_sensorportrait")) Launcher.ReverseLauncherPortraitHook(lpparam);
        if (mPrefs.getBoolean("launcher_unlockhotseat")) Launcher.MaxHotseatIconsCountHook(lpparam);
        if (mPrefs.getStringAsInt("launcher_foldershade", 1) > 1) Launcher.FolderShadeHook(lpparam);
        if (mPrefs.getStringAsInt("launcher_closefolders", 1) > 1) { Launcher.CloseFolderOnLaunchHook(lpparam); closeOnLaunch = true; }
        if (lpparam.packageName.equals("com.miui.home")) {
            if (mPrefs.getInt("system_recents_blur", 100) < 100) Launcher.RecentsBlurRatioHook(lpparam);
            if (mPrefs.getInt("system_recommended_first_action", 1) > 1 ||
                    mPrefs.getInt("system_recommended_second_action", 1) > 1 ||
                    mPrefs.getInt("system_recommended_third_action", 1) > 1 ||
                    mPrefs.getInt("system_recommended_fourth_action", 1) > 1) System.CustomRecommendedHook(lpparam, true);
            if (mPrefs.getInt("controls_fsg_coverage", 60) != 60) Controls.BackGestureAreaHeightHook(lpparam);
            if (mPrefs.getInt("controls_fsg_width", 100) > 100) Controls.BackGestureAreaWidthHook(lpparam);
            if (mPrefs.getBoolean("controls_fsg_horiz")) Launcher.FSGesturesHook(lpparam);
            if (mPrefs.getBoolean("system_removecleaner")) System.HideMemoryCleanHook(lpparam, true);
            if (mPrefs.getBoolean("system_recents_disable_wallpaperscale") || mPrefs.getBoolean("launcher_disable_wallpaperscale")) Launcher.DisableLauncherWallpaperScale(lpparam);
            if (mPrefs.getBoolean("system_fw_sticky")) Launcher.StickyFloatingWindowsLauncherHook(lpparam);
            if (mPrefs.getBoolean("system_recents_hide_statusbar")) Launcher.HideStatusBarInRecentsHook(lpparam);
            if (mPrefs.getBoolean("system_fw_splitscreen")) System.MultiWindowPlusHook(lpparam);
            if (mPrefs.getBoolean("launcher_fixanim")) Launcher.FixAnimHook(lpparam);
            if (mPrefs.getBoolean("launcher_hideseekpoints")) Launcher.HideSeekPointsHook(lpparam);
            if (mPrefs.getBoolean("launcher_privacyapps_gest") ||
                    mPrefs.getInt("launcher_spread_action", 1) != 1) Launcher.PrivacyFolderHook(lpparam);
            if (mPrefs.getBoolean("system_hidefromrecents")) Launcher.HideFromRecentsHook(lpparam);
            if (!mPrefs.getBoolean("launcher_googleminus") && mPrefs.getBoolean("launcher_googlediscover")) Launcher.GoogleDiscoverHook(lpparam);
            if (mPrefs.getBoolean("launcher_docktitles") && mPrefs.getInt("launcher_bottommargin", 0) == 0) Launcher.ShowHotseatTitlesHook(lpparam);
            if (mPrefs.getInt("launcher_folderblur_opacity", 0) > 0) Launcher.FolderBlurHook(lpparam);
            if (mPrefs.getBoolean("launcher_nounlockanim")) Launcher.NoUnlockAnimationHook(lpparam);
            if (mPrefs.getBoolean("launcher_nozoomanim")) Launcher.NoZoomAnimationHook(lpparam);
            if (mPrefs.getBoolean("launcher_oldlaunchanim")) Launcher.UseOldLaunchAnimationHook(lpparam);
            if (mPrefs.getBoolean("launcher_closedrawer")) { Launcher.CloseDrawerOnLaunchHook(lpparam); closeOnLaunch = true; }
            if (mPrefs.getInt("launcher_bottommargin", 0) > 0) Launcher.BottomSpacingHook(lpparam);
            if (mPrefs.getInt("launcher_horizwidgetmargin", 0) > 0) Launcher.HorizontalWidgetSpacingHook(lpparam);
            if (mPrefs.getInt("controls_fsg_assist_action", 1) > 1) Launcher.AssistGestureActionHook(lpparam);
            if (mPrefs.getInt("controls_fsg_swipeandstop_action", 1) > 1) Launcher.SwipeAndStopActionHook(lpparam);
        }
        if (closeOnLaunch) Launcher.CloseFolderOrDrawerOnLaunchShortcutMenuHook(lpparam);
        //if (!mPrefs.getString("system_clock_app", "").equals("")) Launcher.ReplaceClockAppHook(lpparam);
        //if (!mPrefs.getString("system_calendar_app", "").equals("")) Launcher.ReplaceCalendarAppHook(lpparam);
        //Launcher.NoInternationalBuildHook(lpparam);
    }

}