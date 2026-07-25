package tv.withaibuild.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.MiuiMultiWindowUtils;
import android.util.SparseBooleanArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import tv.withaibuild.customiuizer.MainModule;
import tv.withaibuild.customiuizer.R;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;
import tv.withaibuild.customiuizer.utils.Helpers;

@SuppressWarnings("WeakerAccess")
public class GlobalActions {

    public static final String ACTION_PREFIX = "tv.withaibuild.customiuizer.mods.action.";
    public static final String EVENT_PREFIX = "tv.withaibuild.customiuizer.mods.event.";

    public static boolean handleAction(Context context, String key) {
        return handleAction(context, key, false);
    }
    public static boolean handleAction(Context context, String key, boolean skipLock) {
        return handleAction(context, key, skipLock, null);
    }

    public static boolean handleAction(Context context, String key, boolean skipLock, Bundle bundle) {
        if (key == null || key.isEmpty()) return false;
        int action = MainModule.mPrefs.getInt(key + "_action", 1);
        if (action <= 1) return false;
        if (action >= 85 && action <= 88) {
            if (GlobalActions.isMediaActionsAllowed(context))
                GlobalActions.sendDownUpKeyEvent(context, action, false);
            return true;
        }
        return switch (action) {
            case 2 -> commonSendAction(context, "ExpandNotifications");
            case 3 -> commonSendAction(context, "ExpandSettings");
            case 4 -> commonSendAction(context, "LockDevice");
            case 5 -> commonSendAction(context, "GoToSleep");
            case 6 -> commonSendAction(context, "TakeScreenshot");
            case 7 -> commonSendAction(context, "OpenRecents");
            case 8 -> launchAppIntent(context, key, skipLock);
            case 9 -> launchShortcutIntent(context, key, skipLock);
            case 10 -> toggleThis(context, MainModule.mPrefs.getInt(key + "_toggle", 0));
            case 11 -> commonSendAction(context, "SwitchToPrevApp");
            case 12 -> commonSendAction(context, "OpenPowerMenu");
            case 13 -> commonSendAction(context, "ClearMemory");
            case 14 -> commonSendAction(context, "ToggleColorInversion");
            case 15 -> commonSendAction(context, "GoBack");
            case 16 -> commonSendAction(context, "SimulateMenu");
            case 17 -> commonSendAction(context, "OpenVolumeDialog");
            case 18 -> commonSendAction(context, "VolumeUp");
            case 19 -> commonSendAction(context, "VolumeDown");
            case 20 -> launchActivityIntent(context, key, skipLock);
            case 22 -> commonSendAction(context, "SwitchOneHanded");
            case 23 -> commonSendAction(context, "ClearNotifications");
            case 24 -> commonSendAction(context, "ForceClose");
            case 25 -> commonSendAction(context, "ScrollToTop");
            case 26 -> showSidebar(context, bundle);
            case 27 -> commonSendAction(context, "FloatingWindow");
            case 28 -> commonSendAction(context, "PinningWindow");
            case 29 -> commonSendAction(context, "SplitScreen");
            default -> false;
        };
    }

    public static int getActionResId(int action) {
        return switch (action) {
            case 0, 1 -> R.string.notselected;
            case 2 -> R.string.array_global_actions_notif;
            case 3 -> R.string.array_global_actions_eqs;
            case 4 -> R.string.array_global_actions_lock;
            case 5 -> R.string.array_global_actions_sleep;
            case 6 -> R.string.array_global_actions_screenshot;
            case 7 -> R.string.array_global_actions_recents;
            case 11 -> R.string.array_global_actions_back;
            case 12 -> R.string.array_global_actions_powermenu_short;
            case 13 -> R.string.array_global_actions_clearmemory;
            case 14 -> R.string.array_global_actions_invertcolors;
            case 15 -> R.string.array_global_actions_goback;
            case 16 -> R.string.array_global_actions_menu;
            case 17 -> R.string.array_global_actions_volume;
            case 18 -> R.string.array_global_actions_volume_up;
            case 19 -> R.string.array_global_actions_volume_down;
            case 22 -> R.string.array_global_actions_onehanded_left;
            case 23 -> R.string.array_global_actions_clear_notifs;
            case 24 -> R.string.array_global_actions_forceclose;
            case 25 -> R.string.array_global_actions_scrolltotop;
            case 26 -> R.string.array_global_actions_expandsidebar;
            case 27 -> R.string.array_global_actions_floatingwindow;
            case 28 -> R.string.array_global_actions_pinningwindow;
            case 29 -> R.string.array_global_actions_splitscreen;
            default -> 0;
        };
    }

    static final BroadcastReceiver mSBReceiver = new BroadcastReceiver() {
        @SuppressLint({"WrongConstant", "MissingPermission"})
        public void onReceive(final Context context, Intent intent) {
            try {
                Resources modRes = ModuleHelper.getModuleRes(context);
                String action = intent.getAction();
                if (action == null) return;

                if (action.equals(ACTION_PREFIX + "RestartSystemUI")) {
                    Process.killProcess(Process.myPid());
                }
                else if (action.equals(ACTION_PREFIX + "FastReboot")) {
                    PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                    Object mService = XposedHelpers.getObjectField(pm, "mService");
                    XposedHelpers.callMethod(mService, "reboot", false, null, false);
                }
                else if (action.equals(ACTION_PREFIX + "ClearNotifications")) {
                    Object nms =  XposedHelpers.callStaticMethod(NotificationManager.class, "getService");
                    XposedHelpers.callMethod(nms, "cancelAllNotifications", (String)null, 0);
                }
                else if (action.equals(ACTION_PREFIX + "ClearMemory")) {
                    Intent clearIntent = new Intent("com.android.systemui.taskmanager.Clear");
                    clearIntent.putExtra("show_toast", true);
                    //clearIntent.putExtra("clean_type", -1);
                    context.sendBroadcast(clearIntent);
                }
                else if (action.equals(ACTION_PREFIX + "RestartLauncher")) {
                    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                    XposedHelpers.callMethod(am, "forceStopPackage", "com.miui.home");
                }
                else if (action.equals(ACTION_PREFIX + "RestartSecurityCenter")) {
                    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                    XposedHelpers.callMethod(am, "forceStopPackage", "com.miui.securitycenter");
                }
                else if (action.equals(ACTION_PREFIX + "FloatingWindow")) {
                    try {
                        MiuiMultiWindowUtils.startSmallFreeformForControlCenter(context);
                    } catch (Throwable err) {
                        XposedHelpers.log(err);
                    }
                }
                else if (action.equals(ACTION_PREFIX + "SwitchOneHanded")) {
                    Settings.Secure.putInt(context.getContentResolver(), "one_handed_mode_activated", 1);
                    return;
                }
                else if (action.equals(ACTION_PREFIX + "ScrollToTop")) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Method injectInputEventMethod = InputManager.class.getDeclaredMethod("injectInputEvent", InputEvent.class, int.class);
                                Method instanceMethod = InputManager.class.getDeclaredMethod("getInstance");
                                InputManager im = (InputManager) instanceMethod.invoke(InputManager.class);
                                long uptimeMillis = SystemClock.uptimeMillis();
                                MotionEvent swipeDownEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_DOWN, 500, 500, 0);
                                swipeDownEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                                injectInputEventMethod.invoke(im, swipeDownEvt, 1);
                                MotionEvent swipeMoveEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis + 25, MotionEvent.ACTION_MOVE, 500, 240000, 0);
                                swipeMoveEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                                injectInputEventMethod.invoke(im, swipeMoveEvt, 2);
                                MotionEvent swipeUpEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis + 25, MotionEvent.ACTION_UP, 500, 240000, 0);
                                swipeUpEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                                injectInputEventMethod.invoke(im, swipeUpEvt, 2);
                                swipeDownEvt.recycle();
                                swipeMoveEvt.recycle();
                                swipeUpEvt.recycle();
                            }
                            catch (Throwable e) {
                                XposedHelpers.log("err: " + e);
                            }
                        }
                    }, 100L);
                }

                else if (action.equals(ACTION_PREFIX + "ExpandNotifications")) {
                    Object mStatusBar = ModuleHelper.getDepInstance(context.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                    Object callbacks = XposedHelpers.getObjectField(mStatusBar, "mCommandQueueCallbacks");
                    XposedHelpers.callMethod(callbacks, "animateExpandNotificationsPanel");
                }

                else if (action.equals(ACTION_PREFIX + "ExpandSettings")) {
                    boolean forceExpand = intent.getBooleanExtra("forceExpand", false);
                    Object mStatusBar = ModuleHelper.getDepInstance(context.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                    Object mControlCenterController = XposedHelpers.getObjectField(mStatusBar, "mControlCenterController");
                    boolean isUseControlCenter = (boolean)XposedHelpers.callMethod(mControlCenterController, "isUseControlCenter");
                    if (isUseControlCenter) {
                        if (forceExpand || (boolean)XposedHelpers.callMethod(mControlCenterController, "isCollapsed")) {
                            Object lazyControlCenter = XposedHelpers.getObjectField(mControlCenterController, "controlCenter");
                            Object controlCenter = XposedHelpers.callMethod(lazyControlCenter, "get");
                            XposedHelpers.callMethod(controlCenter, "animateExpandSettingsPanel", "");
                        }
                        else
                            XposedHelpers.callMethod(mControlCenterController, "collapseControlCenter", true, true);
                        return;
                    }
                    Object callbacks = XposedHelpers.getObjectField(mStatusBar, "mCommandQueueCallbacks");
                    XposedHelpers.callMethod(callbacks, "animateExpandSettingsPanel", "");
                }

                else if (action.equals(ACTION_PREFIX + "OpenRecents")) {
                    Intent recentIntent = new Intent("SYSTEM_ACTION_RECENTS");
                    recentIntent.setPackage("com.android.systemui");
                    context.sendBroadcast(recentIntent);
                }

                else if (action.equals(ACTION_PREFIX + "OpenVolumeDialog")) {
                    Object mStatusBar = ModuleHelper.getDepInstance(context.getClassLoader(), "com.android.systemui.statusbar.phone.CentralSurfaces");
                    Object mVolumeComponent = XposedHelpers.getObjectField(mStatusBar, "mVolumeComponent");
                    Object mVolumeDialogPlugin = XposedHelpers.getObjectField(mVolumeComponent, "mDialog");
                    Object miuiVolumeDialog = XposedHelpers.getObjectField(mVolumeDialogPlugin, "mVolumeDialogImpl");
                    if (miuiVolumeDialog == null) {
                        XposedHelpers.log("OpenVolumeDialog", "MIUI volume dialog is NULL!");
                        return;
                    }

                    Handler mHandler = (Handler)XposedHelpers.getObjectField(miuiVolumeDialog, "mHandler");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            boolean mShowing = XposedHelpers.getBooleanField(miuiVolumeDialog, "mShowing");
                            boolean mExpanded = XposedHelpers.getBooleanField(miuiVolumeDialog, "mExpanded");

                            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                            boolean isInCall = am.getMode() == AudioManager.MODE_IN_CALL || am.getMode() == AudioManager.MODE_IN_COMMUNICATION;
                            if (mShowing) {
                                if (mExpanded || isInCall)
                                    XposedHelpers.callMethod(miuiVolumeDialog, "dismissH", 1);
                                else {
                                    Object mDialogView = XposedHelpers.getObjectField(miuiVolumeDialog, "mDialogView");
                                    View mExpandButton = (View)XposedHelpers.getObjectField(mDialogView, "mExpandButton");
                                    View.OnClickListener mClickExpand = (View.OnClickListener)XposedHelpers.getObjectField(mDialogView, "expandListener");
                                    mClickExpand.onClick(mExpandButton);
                                }
                            } else {
                                Object mController = XposedHelpers.getObjectField(mVolumeDialogPlugin, "mController");
                                if (isInCall) {
                                    XposedHelpers.callMethod(mController, "setActiveStream", 0);
                                    XposedHelpers.setBooleanField(miuiVolumeDialog, "mNeedReInit", true);
                                } else if (am.isMusicActive()) {
                                    XposedHelpers.callMethod(mController, "setActiveStream", 3);
                                    XposedHelpers.setBooleanField(miuiVolumeDialog, "mNeedReInit", true);
                                }
                                XposedHelpers.callMethod(miuiVolumeDialog, "showH", 1);
                            }
                        }
                    });
                }

                else if (action.equals(ACTION_PREFIX + "ToggleHotspot")) {
                    Object mHotspotController = ModuleHelper.getDepInstance(context.getClassLoader(), "com.android.systemui.statusbar.policy.HotspotController");
                    if (mHotspotController == null) return;
                    boolean mHotspotSupported = (boolean)XposedHelpers.callMethod(mHotspotController, "isHotspotSupported");
                    if (!mHotspotSupported) return;
                    boolean mHotspotEnabled = (boolean)XposedHelpers.callMethod(mHotspotController, "isHotspotEnabled");
                    if (mHotspotEnabled)
                        Toast.makeText(context, modRes.getString(R.string.toggle_hotspot_off), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, modRes.getString(R.string.toggle_hotspot_on), Toast.LENGTH_SHORT).show();
                    XposedHelpers.callMethod(mHotspotController, "setHotspotEnabled", !mHotspotEnabled);
                }

                else if (action.equals(ACTION_PREFIX + "ToggleZenMode")) {
                    Object zenModeController = ModuleHelper.getDepInstance(context.getClassLoader(), "com.android.systemui.statusbar.policy.ZenModeController");
                    boolean zenModeEnabled = (boolean)XposedHelpers.callMethod(zenModeController, "isZenModeOn");
                    if (zenModeEnabled) {
                        XposedHelpers.callMethod(zenModeController, "setZen", 0, "DNDTile");
                    }
                    else {
                        XposedHelpers.callMethod(zenModeController, "setZen", 1, "DNDTile");
                    }
                }

                else if (action.equals(ACTION_PREFIX + "ToggleFlashlight")) {
                    XposedHelpers.callStaticMethod(findClass("com.miui.systemui.util.CommonUtil", context.getClassLoader()), "toggleTorch");
                }
                else if (action.equals(ACTION_PREFIX + "ToggleGPS")) {
                    Object locationController = ModuleHelper.getDepInstance(context.getClassLoader(), "com.android.systemui.statusbar.policy.LocationController");
                    boolean mGpsEnable = (boolean)XposedHelpers.callMethod(locationController, "isLocationEnabled");
                    if (mGpsEnable)
                        Toast.makeText(context, modRes.getString(R.string.toggle_gps_off), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, modRes.getString(R.string.toggle_gps_on), Toast.LENGTH_SHORT).show();
                    XposedHelpers.callMethod(locationController, "setLocationEnabled", !mGpsEnable);
                }
                else if (action.equals(ACTION_PREFIX + "ToggleNightMode")) {
                    Settings.System.putInt(context.getContentResolver(), "dark_mode_enable_by_setting", 1);
                    UiModeManager mUiModeManager = (UiModeManager) context.getSystemService("uimode");
                    boolean nightMode = mUiModeManager.getNightMode() == 2;
                    XposedHelpers.callMethod(mUiModeManager, "setNightModeActivated", !nightMode);
                }
                else if (action.equals(ACTION_PREFIX + "ToggleWiFi")) {
                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(false);
                        Toast.makeText(context, modRes.getString(R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show();
                    } else {
                        wifiManager.setWifiEnabled(true);
                        Toast.makeText(context, modRes.getString(R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show();
                    }
                }
                else if (action.equals(ACTION_PREFIX + "ToggleBluetooth")) {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                        Toast.makeText(context, modRes.getString(R.string.toggle_bt_off), Toast.LENGTH_SHORT).show();
                    } else {
                        mBluetoothAdapter.enable();
                        Toast.makeText(context, modRes.getString(R.string.toggle_bt_on), Toast.LENGTH_SHORT).show();
                    }
                }
                else if (action.equals(ACTION_PREFIX + "ToggleNFC")) {
                    Class<?> clsNfcAdapter = XposedHelpers.findClass("android.nfc.NfcAdapter", null);
                    NfcAdapter mNfcAdapter = (NfcAdapter)XposedHelpers.callStaticMethod(clsNfcAdapter, "getNfcAdapter", context);
                    if (mNfcAdapter == null) return;

                    Method enableNFC = clsNfcAdapter.getDeclaredMethod("enable");
                    Method disableNFC = clsNfcAdapter.getDeclaredMethod("disable");
                    enableNFC.setAccessible(true);
                    disableNFC.setAccessible(true);

                    if (mNfcAdapter.isEnabled()) {
                        disableNFC.invoke(mNfcAdapter);
                        Toast.makeText(context, modRes.getString(R.string.toggle_nfc_off), Toast.LENGTH_SHORT).show();
                    } else {
                        enableNFC.invoke(mNfcAdapter);
                        Toast.makeText(context, modRes.getString(R.string.toggle_nfc_on), Toast.LENGTH_SHORT).show();
                    }
                }
                else if (action.equals(ACTION_PREFIX + "ToggleSoundProfile")) {
                    AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    int currentMode = am.getRingerMode();
                    if (currentMode == 0) {
                        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        Toast.makeText(context, modRes.getString(R.string.toggle_sound_vibrate), Toast.LENGTH_SHORT).show();
                    } else if (currentMode == 1) {
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        Toast.makeText(context, modRes.getString(R.string.toggle_sound_normal), Toast.LENGTH_SHORT).show();
                    } else if (currentMode == 2) {
                        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        Toast.makeText(context, modRes.getString(R.string.toggle_sound_silent), Toast.LENGTH_SHORT).show();
                    }
                }
                else if (action.equals(ACTION_PREFIX + "ToggleAutoRotation")) {
                    if (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
                        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                        Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show();
                    } else {
                        Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation());
                        Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                        Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show();
                    }
                }
                else if (action.equals(ACTION_PREFIX + "ToggleMobileData")) {
                    TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telManager.isDataEnabled()) {
                        telManager.setDataEnabledForReason(0, false);
                        Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_off), Toast.LENGTH_SHORT).show();
                    } else {
                        telManager.setDataEnabledForReason(0, true);
                        Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_on), Toast.LENGTH_SHORT).show();
                    }
                }
                else if (action.equals(ACTION_PREFIX + "WakeUp")) {
                    XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "wakeUp", SystemClock.uptimeMillis());
                }
                else if (action.equals(ACTION_PREFIX + "GoToSleep")) {
                    XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 4, 0);
                }
                else if (action.equals(ACTION_PREFIX + "LockDevice")) {
                    XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 7, 0);
                }
                else if (action.equals(ACTION_PREFIX + "TakeScreenshot")) {
                    context.sendBroadcast(new Intent("android.intent.action.CAPTURE_SCREENSHOT"));
                }
                else if (action.equals(ACTION_PREFIX + "GoBack")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        }
                    }).start();
                }
                else if (action.equals(ACTION_PREFIX + "VolumeUp")) {
                    AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
                }
                else if (action.equals(ACTION_PREFIX + "VolumeDown")) {
                    AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
                }
                else if (action.equals(ACTION_PREFIX + "OpenPowerMenu")) {
                    Object mCommandQueue = ModuleHelper.getDepInstance(context.getClassLoader(), "com.android.systemui.statusbar.CommandQueue");
                    XposedHelpers.callMethod(mCommandQueue, "showGlobalActionsMenu");
                }
                else if (action.equals(ACTION_PREFIX + "LaunchIntent")) {
                    Intent launchIntent = intent.getParcelableExtra("intent");
                    if (launchIntent != null) {
                        int user = 0;
                        if (launchIntent.hasExtra("user")) {
                            user = launchIntent.getIntExtra("user", 0);
                            launchIntent.removeExtra("user");
                        }
                        if (user != 0)
                            XposedHelpers.callMethod(context, "startActivityAsUser", launchIntent, XposedHelpers.newInstance(UserHandle.class, user));
                        else
                            context.startActivity(launchIntent);
                    }
                }
                else if (action.equals(ACTION_PREFIX + "SaveLastMusicPausedTime")) {
                    Settings.System.putLong(context.getContentResolver(), "last_music_paused_time", currentTimeMillis());
                }
            } catch (Throwable t) {
                XposedHelpers.log(t);
            }
        }
    };

    public static void miuizerSettingsHook(PackageReadyParam lpparam) {
        int settingsIconResId = MainModule.resHooks.addFakeResource("ic_miuizer_settings", R.drawable.ic_miuizer_settings, "drawable");
        ModuleHelper.findAndHookMethod("com.android.settings.MiuiSettings", lpparam.getClassLoader(), "updateHeaderList", List.class, new MethodHook() {
            @Override
            public Object intercept(XposedInterface.Chain chain) throws Throwable {
        Object result;
        Throwable throwable = null;
        try {
            result = chain.proceed();
        } catch (Throwable t) {
            throwable = t;
            result = null;
        }
        try {
            if (chain.getArgs().get(0) == null) { return XposedHelpers.throwOrReturn(throwable, result); }

            Context mContext = ((Activity)chain.getThisObject()).getBaseContext();
            int opt = MainModule.mPrefs.getStringAsInt("miuizer_settingsiconpos", 1);

            Class<?> headerCls = findClassIfExists("com.android.settingslib.miuisettings.preference.PreferenceActivity$Header", lpparam.getClassLoader());
            if (headerCls == null) { return XposedHelpers.throwOrReturn(throwable, result); }

            Resources modRes = ModuleHelper.getModuleRes(mContext);
            Object header = XposedHelpers.newInstance(headerCls);
            XposedHelpers.setLongField(header, "id", 666);
            Intent intent = new Intent();
            intent.setClassName(Helpers.modulePkg, "tv.withaibuild.customiuizer.MainActivity");
            intent.putExtra("from.settings", true);
            XposedHelpers.setObjectField(header, "intent", intent);
            XposedHelpers.setIntField(header, "iconRes", settingsIconResId);
            XposedHelpers.setObjectField(header, "title", modRes.getString(R.string.app_name));
            Bundle bundle = new Bundle();
            ArrayList<UserHandle> users = new ArrayList<UserHandle>();
            users.add((UserHandle)XposedHelpers.newInstance(UserHandle.class, 0));
            bundle.putParcelableArrayList("header_user", users);
            XposedHelpers.setObjectField(header, "extras", bundle);

            int themes = mContext.getResources().getIdentifier("launcher_settings", "id", mContext.getPackageName());
            int special = mContext.getResources().getIdentifier("other_special_feature_settings", "id", mContext.getPackageName());

            List<Object> headers = (List<Object>)chain.getArgs().get(0);
            int position = 0;
            for (Object head: headers) {
                position++;
                long id = XposedHelpers.getLongField(head, "id");
                if (opt == 1 && id == -1) { headers.add(position - 1, header); return XposedHelpers.throwOrReturn(throwable, result); }
                if (opt == 2 && id == themes) { headers.add(position, header); return XposedHelpers.throwOrReturn(throwable, result); }
                if (opt == 3 && id == special) { headers.add(position, header); return XposedHelpers.throwOrReturn(throwable, result); }
            }
            if (headers.size() > 25)
                headers.add(25, header);
            else
                headers.add(header);
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
        return XposedHelpers.throwOrReturn(throwable, result);
    }
        });
        ModuleHelper.hookAllMethods("com.android.settings.MiuiSettings$HeaderAdapter", lpparam.getClassLoader(), "setIcon", new MethodHook() {
            @Override
            public Object intercept(XposedInterface.Chain chain) throws Throwable {
        Object result;
        Throwable throwable = null;
        try {
            result = chain.proceed();
        } catch (Throwable t) {
            throwable = t;
            result = null;
        }
        try {
            List<Object> args = chain.getArgs();
            int iconRes = XposedHelpers.getIntField(args.get(1), "iconRes");
            if (iconRes == settingsIconResId) {
                ImageView icon = (ImageView) XposedHelpers.getObjectField(args.get(0), "icon");
                int iconSize = XposedHelpers.getIntField(XposedHelpers.getSurroundingThis(chain.getThisObject()), "mNormalIconSize");
                icon.getLayoutParams().height = iconSize;
            }
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
        return XposedHelpers.throwOrReturn(throwable, result);
    }
        });
    }

    public static void setupForegroundMonitor(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                Object result;
                Throwable throwable = null;
                try {
                    result = chain.proceed();
                } catch (Throwable t) {
                    throwable = t;
                    result = null;
                }
                try {
                    final Object thisObject = chain.getThisObject();

                                    final Context mContext = (Context) chain.getArgs().get(0);
                                    final Handler mBgHandler = (Handler) XposedHelpers.getObjectField(thisObject, "mBgHandler");
                                    ModuleHelper.findAndHookMethod("com.miui.systemui.functions.MiuiTopActivityObserver", lpparam.getClassLoader(), "updateTopActivity", new MethodHook() {
                                        private String pkgName = "";
                                        @Override
                                                            public Object intercept(XposedInterface.Chain chain) throws Throwable {
                                            Object result;
                                            Throwable throwable = null;
                                            try {
                                                result = chain.proceed();
                                            } catch (Throwable t) {
                                                throwable = t;
                                                result = null;
                                            }
                                            try {
                                                final Object thisObject = chain.getThisObject();

                                                                        ComponentName mTopActivity = (ComponentName) XposedHelpers.getObjectField(thisObject, "mTopActivity");
                                                                        if (mTopActivity != null && !pkgName.equals(mTopActivity.getPackageName())) {
                                                                            pkgName = mTopActivity.getPackageName();
                                                                            Settings.Global.putString(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.package", pkgName);
                                                                        }
                    
                                            } catch (Throwable t) {
                                                XposedHelpers.log(t);
                                            }
                                            return XposedHelpers.throwOrReturn(throwable, result);
                                        }
                                    });
                                    if (MainModule.mPrefs.getStringAsInt("various_showcallui", 0) > 0) {
                                        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.SystemBarAttributesListener", lpparam.getClassLoader(), "onSystemBarAttributesChanged", new MethodHook() {
                                            private boolean fullScreen = false;
                                            @Override
                                                                    public Object intercept(XposedInterface.Chain chain) throws Throwable {
                                                Object result;
                                                Throwable throwable = null;
                                                try {
                                                    result = chain.proceed();
                                                } catch (Throwable t) {
                                                    throwable = t;
                                                    result = null;
                                                }
                                                try {
                                                    final Object thisObject = chain.getThisObject();

                                                                                Object statusBarStateController = XposedHelpers.getObjectField(thisObject, "statusBarStateController");
                                                                                boolean isFullScreen = XposedHelpers.getBooleanField(statusBarStateController, "mIsFullscreen");
                                                                                if (fullScreen != isFullScreen) {
                                                                                    mBgHandler.post(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            Settings.Global.putInt(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.fullscreen", fullScreen ? 1 : 0);
                                                                                        }
                                                                                    });
                                                                                }
                                                                                fullScreen = isFullScreen;
                        
                                                } catch (Throwable t) {
                                                    XposedHelpers.log(t);
                                                }
                                                return XposedHelpers.throwOrReturn(throwable, result);
                                            }
                                        });
                                    }
            
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
                return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static final String[] customActionKeys = {
        "controls_backlong", "controls_homelong", "controls_menulong",
        "controls_powerdt",
        "controls_fsg_assist_left", "controls_fsg_assist_right",
        "controls_fsg_swipeandstop",
        "controls_navbarleft", "controls_navbarleftlong",
        "controls_navbarright", "controls_navbarrightlong",
        "launcher_swipedown", "launcher_swipeup", "launcher_swipedown2", "launcher_swipeup2",
        "launcher_swipeleft", "launcher_swiperight",
        "launcher_doubletap", "launcher_pinch", "launcher_shake", "launcher_spread",
        "system_statusbarcontrols", "system_statusbarcontrols_longpress",
        "system_lockscreenshortcuts_left", "system_lockscreenshortcuts_right"
    };

    private static volatile SparseBooleanArray customActionCodeMap = null;
    private static volatile SparseBooleanArray customToggleMap = null;
    private static volatile boolean customActionsReady = false;

    private static void ensureCustomActionMaps() {
        if (customActionsReady) return;
        synchronized (GlobalActions.class) {
            if (customActionsReady) return;
            SparseBooleanArray actionMap = new SparseBooleanArray();
            SparseBooleanArray toggleMap = new SparseBooleanArray();
            for (String key : customActionKeys) {
                int action = MainModule.mPrefs.getInt(key + "_action", 1);
                if (action > 1) actionMap.put(action, true);
                if (action == 10) {
                    int toggle = MainModule.mPrefs.getInt(key + "_toggle", 0);
                    if (toggle > 0) toggleMap.put(toggle, true);
                }
            }
            customActionCodeMap = actionMap;
            customToggleMap = toggleMap;
            customActionsReady = true;
        }
    }

    public static boolean hasCustomActions() {
        ensureCustomActionMaps();
        return customActionCodeMap.size() > 0;
    }

    public static boolean hasActionCode(int code) {
        ensureCustomActionMaps();
        return customActionCodeMap.get(code);
    }

    public static boolean hasToggle(int what) {
        ensureCustomActionMaps();
        return customToggleMap.get(what);
    }

    public static void setupGlobalActions(XposedModuleInterface.SystemServerStartingParam lpparam) {
        GlobalActionSystemServerHooks.setupGlobalActions(lpparam);
    }

    public static void setupStatusBar(PackageReadyParam lpparam) {
        GlobalActionSystemServerHooks.setupStatusBar(lpparam);
    }

    public static boolean launchAppIntent(Context context, String key, boolean skipLock) { return GlobalActionsIntentHelper.launchAppIntent(context, key, skipLock); }
    public static boolean launchActivityIntent(Context context, String key, boolean skipLock) { return GlobalActionsIntentHelper.launchActivityIntent(context, key, skipLock); }
    public static boolean launchShortcutIntent(Context context, String key, boolean skipLock) { return GlobalActionsIntentHelper.launchShortcutIntent(context, key, skipLock); }
    public static boolean launchIntent(Context context, Intent intent) { return GlobalActionsIntentHelper.launchIntent(context, intent); }

    private static boolean showSidebar(Context context, Bundle bundle) {
        try {
            Intent showIntent = new Intent(ACTION_PREFIX + "ShowSideBar");
            showIntent.setPackage("com.miui.securitycenter");
            if (bundle != null) {
                showIntent.putExtra("actionInfo", bundle);
            }
            context.sendBroadcast(showIntent);
            return true;
        } catch (Throwable t) {
            XposedHelpers.log(t);
            return false;
        }
    }

    public static boolean commonSendAction(Context context, String action) {
        try {
            context.sendBroadcast(new Intent(ACTION_PREFIX + action));
            return true;
        } catch (Throwable t) {
            XposedHelpers.log(t);
            return false;
        }
    }

    private static boolean toggleThis(Context context, int what) {
        try {
            String whatStr;
            switch (what) {
                case 1 -> whatStr = "WiFi";
                case 2 -> whatStr = "Bluetooth";
                case 3 -> whatStr = "GPS";
                case 4 -> whatStr = "NFC";
                case 5 -> whatStr = "SoundProfile";
                case 6 -> whatStr = "AutoBrightness";
                case 7 -> whatStr = "AutoRotation";
                case 8 -> whatStr = "Flashlight";
                case 9 -> whatStr = "MobileData";
                case 10 -> whatStr = "Hotspot";
                case 11 -> whatStr = "ZenMode";
                case 12 -> whatStr = "NightMode";
                default -> {
                    return false;
                }
            }
            context.sendBroadcast(new Intent(ACTION_PREFIX + "Toggle" + whatStr));
            return true;
        } catch (Throwable t) {
            XposedHelpers.log(t);
            return false;
        }
    }

    public static boolean isMediaActionsAllowed(Context mContext) {
        AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean isMusicActive = am.isMusicActive();
        boolean isMusicActiveRemotely  = (Boolean)XposedHelpers.callMethod(am, "isMusicActiveRemotely");
        boolean isAllowed = isMusicActive || isMusicActiveRemotely;
        if (!isAllowed) {
            long mCurrentTime = currentTimeMillis();
            long mLastPauseTime = Settings.System.getLong(mContext.getContentResolver(), "last_music_paused_time", mCurrentTime);
            if (mCurrentTime - mLastPauseTime < 10 * 60 * 1000) isAllowed = true;
        }
        return isAllowed;
    }

    public static void sendDownUpKeyEvent(Context mContext, int keyCode, boolean vibrate) {
        AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));

        if (vibrate && MainModule.mPrefs.getBoolean("controls_volumemedia_vibrate", true))
            Helpers.performStrongVibration(mContext, MainModule.mPrefs.getBoolean("controls_volumemedia_vibrate_ignore"));
    }
}
