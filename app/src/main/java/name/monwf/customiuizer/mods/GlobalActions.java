package name.monwf.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findMethodExact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityOptions;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
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
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;
import miui.app.MiuiFreeFormManager;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;

@SuppressWarnings("WeakerAccess")
public class GlobalActions {

    public static Object mStatusBar = null;
    public static final String ACTION_PREFIX = "name.monwf.customiuizer.mods.action.";
    public static final String EVENT_PREFIX = "name.monwf.customiuizer.mods.event.";

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
        switch (action) {
            case 2: return commonSendAction(context, "ExpandNotifications");
            case 3: return commonSendAction(context, "ExpandSettings");
            case 4: return commonSendAction(context, "LockDevice");
            case 5: return commonSendAction(context, "GoToSleep");
            case 6: return commonSendAction(context, "TakeScreenshot");
            case 7: return commonSendAction(context, "OpenRecents");
            case 8: return launchAppIntent(context, key, skipLock);
            case 9: return launchShortcutIntent(context, key, skipLock);
            case 10: return toggleThis(context, MainModule.mPrefs.getInt(key + "_toggle", 0));
            case 11: return commonSendAction(context, "SwitchToPrevApp");
            case 12: return commonSendAction(context, "OpenPowerMenu");
            case 13: return commonSendAction(context, "ClearMemory");
            case 14: return commonSendAction(context, "ToggleColorInversion");
            case 15: return commonSendAction(context, "GoBack");
            case 16: return commonSendAction(context, "SimulateMenu");
            case 17: return commonSendAction(context, "OpenVolumeDialog");
            case 18: return commonSendAction(context, "VolumeUp");
            case 19: return commonSendAction(context, "VolumeDown");
            case 20: return launchActivityIntent(context, key, skipLock);
            case 21: return commonSendAction(context, "SwitchKeyboard");
            case 22: return commonSendAction(context, "SwitchOneHanded");
            case 23: return commonSendAction(context, "ClearNotifications");
            case 24: return commonSendAction(context, "ForceClose");
            case 25: return commonSendAction(context, "ScrollToTop");
            case 26: return showSidebar(context, bundle);
            case 27: return commonSendAction(context, "FloatingWindow");
            case 28: return commonSendAction(context, "PinningWindow");
            default: return false;
        }
    }

    public static int getActionResId(int action) {
        switch (action) {
            case 0:
            case 1: return R.string.notselected;
            case 2: return R.string.array_global_actions_notif;
            case 3: return R.string.array_global_actions_eqs;
            case 4: return R.string.array_global_actions_lock;
            case 5: return R.string.array_global_actions_sleep;
            case 6: return R.string.array_global_actions_screenshot;
            case 7: return R.string.array_global_actions_recents;
            case 11: return R.string.array_global_actions_back;
            case 12: return R.string.array_global_actions_powermenu_short;
            case 13: return R.string.array_global_actions_clearmemory;
            case 14: return R.string.array_global_actions_invertcolors;
            case 15: return R.string.array_global_actions_goback;
            case 16: return R.string.array_global_actions_menu;
            case 17: return R.string.array_global_actions_volume;
            case 18: return R.string.array_global_actions_volume_up;
            case 19: return R.string.array_global_actions_volume_down;
            case 21: return R.string.array_global_actions_switchkeyboard;
            case 22: return R.string.array_global_actions_onehanded_left;
            case 23: return R.string.array_global_actions_clear_notifs;
            case 24: return R.string.array_global_actions_forceclose;
            case 25: return R.string.array_global_actions_scrolltotop;
            case 26: return R.string.array_global_actions_expandsidebar;
            case 27: return R.string.array_global_actions_floatingwindow;
            case 28: return R.string.array_global_actions_pinningwindow;
            default: return 0;
        }
    }

    private static final BroadcastReceiver mSBReceiver = new BroadcastReceiver() {
        @SuppressLint("WrongConstant")
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
                        MiuiMultiWindowUtils.startSmallFreeform(context);
                    } catch (Throwable err) {
                        XposedHelpers.log(err);
                    }
                }
                else if (action.equals(ACTION_PREFIX + "PinningWindow")) {
                    try {
                        ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
                        if (foregroundInfo != null) {
                            String topPackage = foregroundInfo.mForegroundPackageName;
                            if ("com.miui.home".equals(topPackage)) {
                                return;
                            }
                        }
                        else {
                            return;
                        }
                        Class <?> ActivityTaskManagerCls = findClassIfExists("android.app.ActivityTaskManager", context.getClassLoader());
                        Object activityTaskManager = XposedHelpers.callStaticMethod(ActivityTaskManagerCls, "getService");
                        List<Object> rootTaskInfos = (List<Object>) XposedHelpers.callMethod(activityTaskManager, "getAllRootTaskInfosOnDisplay", 0);
                        List<MiuiFreeFormManager.MiuiFreeFormStackInfo> freeFormStackInfoList = MiuiFreeFormManager.getAllFreeFormStackInfosOnDisplay(context.getDisplay() != null ? context.getDisplay().getDisplayId() : 0);
                        int freeFormCount = 0;
                        if (freeFormStackInfoList != null) {
                            freeFormCount = freeFormStackInfoList.size();
                        }
                        if (freeFormCount == 2) return;
                        ActivityOptions ao = MiuiMultiWindowUtils.getActivityOptions(context, "com.android.mms", true, false);
                        for (Object rootTaskInfo : rootTaskInfos) {
                            Object conf = XposedHelpers.getObjectField(rootTaskInfo, "configuration");
                            Object windowConfiguration = XposedHelpers.getObjectField(conf, "windowConfiguration");
                            int wmode = XposedHelpers.getIntField(windowConfiguration, "mWindowingMode");
                            int mActivityType = XposedHelpers.getIntField(windowConfiguration, "mActivityType");
                            if (wmode < 2 && mActivityType < 2) {
                                int taskId = XposedHelpers.getIntField(rootTaskInfo, "taskId");
                                XposedHelpers.callMethod(activityTaskManager, "startActivityFromRecents", taskId, ao.toBundle());
                                Handler myhandler = new Handler(Looper.myLooper());
                                Runnable removeBg = new Runnable() {
                                    @Override
                                    public void run() {
                                        myhandler.removeCallbacks(this);
                                        try {
                                            Method injectInputEventMethod = InputManager.class.getDeclaredMethod("injectInputEvent", InputEvent.class, int.class);
                                            Method instanceMethod = InputManager.class.getDeclaredMethod("getInstance");
                                            InputManager im = (InputManager) instanceMethod.invoke(InputManager.class);
                                            long now = SystemClock.uptimeMillis();
                                            KeyEvent homeDown = new KeyEvent(now, now, 0, 3, 0, 0, -1, 0);
                                            KeyEvent homeUp = new KeyEvent(now, now, 1, 3, 0, 0, -1, 0);
                                            injectInputEventMethod.invoke(im, homeDown, 0);
                                            injectInputEventMethod.invoke(im, homeUp, 0);
                                        }
                                        catch (Throwable err) {}
                                    }
                                };
                                myhandler.postDelayed(removeBg, 120);
                                return;
                            }
                        }
                    } catch (Throwable err) {
                        XposedHelpers.log(err);
                    }
                }
                else if (action.equals(ACTION_PREFIX + "SwitchOneHanded")) {
                    Settings.Secure.putInt(context.getContentResolver(), "one_handed_mode_activated", 1);
                    return;
                }
                else if (action.equals(ACTION_PREFIX + "ScrollToTop")) {
                    new Handler().postDelayed(new Runnable() {
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

                if (mStatusBar != null) {
                    if (action.equals(ACTION_PREFIX + "ExpandNotifications")) try {
                        Object mNotificationPanel = XposedHelpers.getObjectField(mStatusBar, "mNotificationPanel");
                        boolean mPanelExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mPanelExpanded");
                        boolean mQsExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mQsExpanded");
                        boolean expandOnly = intent.getBooleanExtra("expand_only", false);
                        if (mPanelExpanded) {
                            if (!expandOnly)
                                if (mQsExpanded)
                                    XposedHelpers.callMethod(mStatusBar, "closeQs");
                                else
                                    XposedHelpers.callMethod(mStatusBar, "animateCollapsePanels");
                        } else {
                            XposedHelpers.callMethod(mStatusBar, "animateExpandNotificationsPanel");
                        }
                    } catch (Throwable t) {
                        // Expand only
                        long token = Binder.clearCallingIdentity();
                        XposedHelpers.callMethod(context.getSystemService("statusbar"), "expandNotificationsPanel");
                        Binder.restoreCallingIdentity(token);
                    }

                    if (action.equals(ACTION_PREFIX + "ExpandSettings")) try {
                        boolean forceExpand = intent.getBooleanExtra("forceExpand", false);
                        Object mControlCenterController = XposedHelpers.getObjectField(mStatusBar, "mControlCenterController");
                        boolean isUseControlCenter = (boolean)XposedHelpers.callMethod(mControlCenterController, "isUseControlCenter");
                        if (isUseControlCenter) {
                            if (forceExpand || (boolean)XposedHelpers.callMethod(mControlCenterController, "isCollapsed"))
                                XposedHelpers.callMethod(mControlCenterController, "openPanel");
                            else
                                XposedHelpers.callMethod(mControlCenterController, "collapseControlCenter", true);
                            return;
                        }

                        Object mNotificationPanel = XposedHelpers.getObjectField(mStatusBar, "mNotificationPanelViewController");
                        boolean mPanelExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mPanelExpanded");
                        boolean mQsExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mQsExpanded");
                        if (!forceExpand && mPanelExpanded) {
                            if (mQsExpanded)
                                XposedHelpers.callMethod(mStatusBar, "animateCollapsePanels", 0, false);
                            else
                                XposedHelpers.callMethod(mNotificationPanel, "setQsExpanded", true);
                        } else {
                            XposedHelpers.callMethod(mStatusBar, "animateExpandSettingsPanel", (Object)null);
                        }
                    } catch (Throwable t) {
                        // Expand only
                        long token = Binder.clearCallingIdentity();
                        XposedHelpers.callMethod(context.getSystemService("statusbar"), "expandSettingsPanel");
                        Binder.restoreCallingIdentity(token);
                    }

                    if (action.equals(ACTION_PREFIX + "OpenRecents")) {
                        Intent recentIntent = new Intent("SYSTEM_ACTION_RECENTS");
                        recentIntent.setPackage("com.android.systemui");
                        context.sendBroadcast(recentIntent);
                    }

                    if (action.equals(ACTION_PREFIX + "OpenVolumeDialog")) try {
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
                    } catch (Throwable t) {
                        XposedHelpers.log(t);
                    }

                    if (action.equals(ACTION_PREFIX + "ToggleHotspot")) {
                        Object mHotspotController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", context.getClassLoader()), "get", findClassIfExists("com.android.systemui.statusbar.policy.HotspotController", context.getClassLoader()));
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

                    if (action.equals(ACTION_PREFIX + "ToggleFlashlight")) {
                        XposedHelpers.callStaticMethod(findClass("com.miui.systemui.util.CommonUtil", context.getClassLoader()), "toggleTorch");
                    }
                    // @todo fix gps toggle
//					Object mToggleManager = XposedHelpers.getObjectField(mStatusBar, "mToggleManager");
//					if (mToggleManager == null) return;
//					if (action.equals(ACTION_PREFIX + "ToggleGPS")) {
//						boolean mGpsEnable = (boolean)XposedHelpers.getObjectField(mToggleManager, "mGpsEnable");
//						if (mGpsEnable)
//							Toast.makeText(context, modRes.getString(R.string.toggle_gps_off), Toast.LENGTH_SHORT).show();
//						else
//							Toast.makeText(context, modRes.getString(R.string.toggle_gps_on), Toast.LENGTH_SHORT).show();
//						XposedHelpers.callMethod(mToggleManager, "toggleGps");
//					}
                }
            } catch (Throwable t) {
                XposedHelpers.log(t);
            }
        }
    };

    private static final BroadcastReceiver mGlobalReceiver = new BroadcastReceiver() {
        @SuppressWarnings("ConstantConditions")
        @SuppressLint({"MissingPermission", "WrongConstant", "NewApi"})
        public void onReceive(final Context context, Intent intent) {
        try {
            Resources modRes = ModuleHelper.getModuleRes(context);
            String action = intent.getAction();
            if (action == null) return;
            // Actions
            if (action.equals(ACTION_PREFIX + "RunParasitic")) {
                Intent intent2 = new Intent();
                intent2.setAction("android.intent.action.MAIN");
                intent2.addCategory("org.lsposed.manager.LAUNCH_MANAGER");
                intent2.setClassName("com.android.shell", "com.android.shell.BugreportWarningActivity");
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(intent2);
            }
            if (action.equals(ACTION_PREFIX + "WakeUp")) {
                XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "wakeUp", SystemClock.uptimeMillis());
            }
            if (action.equals(ACTION_PREFIX + "GoToSleep")) {
                XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 4, 0);
            }
            if (action.equals(ACTION_PREFIX + "LockDevice")) {
                XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 7, 0);
            }
            if (action.equals(ACTION_PREFIX + "TakeScreenshot")) {
                context.sendBroadcast(new Intent("android.intent.action.CAPTURE_SCREENSHOT"));
            }

            if (action.equals(ACTION_PREFIX + "GoBack")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                    }
                }).start();
            }

            if (action.equals(ACTION_PREFIX + "SwitchToPrevApp")) {
                PackageManager pm = context.getPackageManager();
                Intent intent_home = new Intent(Intent.ACTION_MAIN);
                intent_home.addCategory(Intent.CATEGORY_HOME);
                intent_home.addCategory(Intent.CATEGORY_DEFAULT);
                List<ResolveInfo> launcherList = pm.queryIntentActivities(intent_home, 0);

                ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                @SuppressWarnings("deprecation")
                List<RecentTaskInfo> rti = am.getRecentTasks(Integer.MAX_VALUE, 0);

                Intent recentIntent;
                for (RecentTaskInfo rtitem: rti) try {
                    //noinspection deprecation
                    if (am.getRunningTasks(1).get(0).topActivity == rtitem.topActivity) continue;

                    boolean isLauncher = false;
                    recentIntent = new Intent(rtitem.baseIntent);
                    if (rtitem.origActivity != null) recentIntent.setComponent(rtitem.origActivity);
                    ComponentName resolvedAct = recentIntent.resolveActivity(pm);

                    if (resolvedAct != null)
                        for (ResolveInfo launcher: launcherList)
                            if (!launcher.activityInfo.packageName.equals("com.android.settings") && launcher.activityInfo.packageName.equals(resolvedAct.getPackageName())) {
                                isLauncher = true;
                                break;
                            }

                    if (!isLauncher) {
//						if (Helpers.getHtcHaptic(context)) {
//							Vibrator vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
//							if (XMain.pref.getBoolean("pref_key_controls_longpresshaptic_enable", false))
//								vibe.vibrate(XMain.pref.getInt("pref_key_controls_longpresshaptic", 21));
//							else
//								vibe.vibrate(21);
//						}
                        if (rtitem.id >= 0)
                            am.moveTaskToFront(rtitem.id, 0);
                        else
                            context.startActivity(recentIntent);
                        break;
                    }
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
            }

            if (action.equals(ACTION_PREFIX + "LaunchIntent")) {
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

            if (action.equals(ACTION_PREFIX + "VolumeUp")) {
                AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
            }

            if (action.equals(ACTION_PREFIX + "VolumeDown")) {
                AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
            }

            if (action.equals(ACTION_PREFIX + "OpenPowerMenu")) {
                Class<?> clsWMG = XposedHelpers.findClass("android.view.WindowManagerGlobal", null);
                Object wms = XposedHelpers.callStaticMethod(clsWMG, "getWindowManagerService");
                XposedHelpers.callMethod(wms, "showGlobalActions");
            }

            if (action.equals(ACTION_PREFIX + "SwitchKeyboard")) {
                context.sendBroadcast(
                    new Intent("com.android.server.InputMethodManagerService.SHOW_INPUT_METHOD_PICKER").setPackage("android")
                );
            }

            if (action.equals(ACTION_PREFIX + "ToggleColorInversion")) {
                int opt = Settings.Secure.getInt(context.getContentResolver(), "accessibility_display_inversion_enabled");
                int conflictProp = (int) ModuleHelper.proxySystemProperties("getInt", "ro.df.effect.conflict", 0, null);
                int conflictProp2 = (int) ModuleHelper.proxySystemProperties("getInt", "ro.vendor.df.effect.conflict", 0, null);
                boolean hasConflict = conflictProp == 1 || conflictProp2 == 1;
                Object dfMgr = XposedHelpers.callStaticMethod(XposedHelpers.findClass("miui.hardware.display.DisplayFeatureManager", null), "getInstance");
                if (hasConflict && opt == 0) XposedHelpers.callMethod(dfMgr, "setScreenEffect", 15, 1);
                Settings.Secure.putInt(context.getContentResolver(), "accessibility_display_inversion_enabled", opt == 0 ? 1 : 0);
                if (hasConflict && opt != 0) XposedHelpers.callMethod(dfMgr, "setScreenEffect", 15, 0);
            }

            // Toggles
            if (action.equals(ACTION_PREFIX + "ToggleWiFi")) {
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    Toast.makeText(context, modRes.getString(R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show();
                } else {
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(context, modRes.getString(R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show();
                }
            }
            if (action.equals(ACTION_PREFIX + "ToggleBluetooth")) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    Toast.makeText(context, modRes.getString(R.string.toggle_bt_off), Toast.LENGTH_SHORT).show();
                } else {
                    mBluetoothAdapter.enable();
                    Toast.makeText(context, modRes.getString(R.string.toggle_bt_on), Toast.LENGTH_SHORT).show();
                }
            }
            if (action.equals(ACTION_PREFIX + "ToggleNFC")) {
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
            if (action.equals(ACTION_PREFIX + "ToggleSoundProfile")) {
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
            if (action.equals(ACTION_PREFIX + "ToggleAutoBrightness")) {
                if (Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) == 0) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
                    Toast.makeText(context, modRes.getString(R.string.toggle_autobright_on), Toast.LENGTH_SHORT).show();
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
                    Toast.makeText(context, modRes.getString(R.string.toggle_autobright_off), Toast.LENGTH_SHORT).show();
                }
            }
            if (action.equals(ACTION_PREFIX + "ToggleAutoRotation")) {
                if (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show();
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation());
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                    Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show();
                }
            }
            if (action.equals(ACTION_PREFIX + "ToggleMobileData")) {
                TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                Method setMTE = TelephonyManager.class.getDeclaredMethod("setDataEnabled", boolean.class);
                @SuppressWarnings("ALL")
                Method getMTE = TelephonyManager.class.getDeclaredMethod("getDataEnabled");
                setMTE.setAccessible(true);
                getMTE.setAccessible(true);

                if ((Boolean)getMTE.invoke(telManager)) {
                    setMTE.invoke(telManager, false);
                    Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_off), Toast.LENGTH_SHORT).show();
                } else {
                    setMTE.invoke(telManager, true);
                    Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_on), Toast.LENGTH_SHORT).show();
                }
            }
        } catch(Throwable t) {
            XposedHelpers.log(t);
        }
        }
    };

    public static void miuizerSettingsHook(PackageLoadedParam lpparam) {
        int settingsIconResId = MainModule.resHooks.addResource("ic_miuizer_settings", R.drawable.ic_miuizer_settings);
        ModuleHelper.findAndHookMethod("com.android.settings.MiuiSettings", lpparam.getClassLoader(), "updateHeaderList", List.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs()[0] == null) return;

                Context mContext = ((Activity)param.getThisObject()).getBaseContext();
                int opt = MainModule.mPrefs.getStringAsInt("miuizer_settingsiconpos", 1);

                Class<?> headerCls = findClassIfExists("com.android.settingslib.miuisettings.preference.PreferenceActivity$Header", lpparam.getClassLoader());
                if (headerCls == null) return;

                Resources modRes = ModuleHelper.getModuleRes(mContext);
                Object header = XposedHelpers.newInstance(headerCls);
                XposedHelpers.setLongField(header, "id", 666);
                Intent intent = new Intent();
                intent.setClassName(Helpers.modulePkg, "name.monwf.customiuizer.MainActivity");
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

                List<Object> headers = (List<Object>)param.getArgs()[0];
                int position = 0;
                for (Object head: headers) {
                    position++;
                    long id = XposedHelpers.getLongField(head, "id");
                    if (opt == 1 && id == -1) { headers.add(position - 1, header); return; }
                    if (opt == 2 && id == themes) { headers.add(position, header); return; }
                    if (opt == 3 && id == special) { headers.add(position, header); return; }
                }
                if (headers.size() > 25)
                    headers.add(25, header);
                else
                    headers.add(header);
            }
        });
        ModuleHelper.hookAllMethods("com.android.settings.MiuiSettings$HeaderAdapter", lpparam.getClassLoader(), "setIcon", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int iconRes = XposedHelpers.getIntField(param.getArgs()[1], "iconRes");
                if (iconRes == settingsIconResId) {
                    ImageView icon = (ImageView) XposedHelpers.getObjectField(param.getArgs()[0], "icon");
                    int iconSize = XposedHelpers.getIntField(XposedHelpers.getSurroundingThis(param.getThisObject()), "mNormalIconSize");
                    icon.getLayoutParams().height = iconSize;
                }
            }
        });
    }

    public static void setupForegroundMonitor(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                final Context mContext = (Context) param.getArgs()[0];
                final Handler mBgHandler = (Handler) XposedHelpers.getObjectField(param.getThisObject(), "mBgHandler");
                ModuleHelper.hookAllMethods("com.miui.systemui.util.MiuiActivityUtil", lpparam.getClassLoader(), "updateTopActivity", new MethodHook() {
                    private String pkgName = "";
                    @Override
                    protected void after(final AfterHookCallback param) throws Throwable {
                        ComponentName mTopActivity = (ComponentName) XposedHelpers.getObjectField(param.getThisObject(), "mTopActivity");
                        if (mTopActivity != null && !pkgName.equals(mTopActivity.getPackageName())) {
                            pkgName = mTopActivity.getPackageName();
                            Settings.Global.putString(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.package", pkgName);
                        }
                    }
                });
                if (MainModule.mPrefs.getStringAsInt("various_showcallui", 0) > 0) {
                    ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarStateControllerImpl", lpparam.getClassLoader(), "setSystemBarAttributes", new MethodHook() {
                        private boolean fullScreen = false;
                        @Override
                        protected void after(final AfterHookCallback param) throws Throwable {
                            boolean isFullScreen = XposedHelpers.getBooleanField(param.getThisObject(), "mIsFullscreen");
                            if (fullScreen != isFullScreen) {
                                mBgHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Settings.Global.putInt(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.fullscreen", fullScreen ? 1 : 0);
                                    }
                                });
                            }
                            fullScreen = isFullScreen;
                        }
                    });
                }
            }
        });
    }

    public static void setupGlobalActions(XposedModuleInterface.SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.server.accessibility.AccessibilityManagerService", lpparam.getClassLoader(), new MethodHook() {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mGlobalContext = (Context)param.getArgs()[0];

                IntentFilter intentfilter = new IntentFilter();

                // Actions
                intentfilter.addAction(ACTION_PREFIX + "WakeUp");
                intentfilter.addAction(ACTION_PREFIX + "GoToSleep");
                intentfilter.addAction(ACTION_PREFIX + "LockDevice");
                intentfilter.addAction(ACTION_PREFIX + "TakeScreenshot");
                intentfilter.addAction(ACTION_PREFIX + "SwitchToPrevApp");
                intentfilter.addAction(ACTION_PREFIX + "GoBack");
                intentfilter.addAction(ACTION_PREFIX + "OpenPowerMenu");
                intentfilter.addAction(ACTION_PREFIX + "SwitchKeyboard");
                intentfilter.addAction(ACTION_PREFIX + "ToggleColorInversion");
                intentfilter.addAction(ACTION_PREFIX + "VolumeUp");
                intentfilter.addAction(ACTION_PREFIX + "VolumeDown");
                intentfilter.addAction(ACTION_PREFIX + "LaunchIntent");

                // Toggles
                intentfilter.addAction(ACTION_PREFIX + "ToggleWiFi");
                intentfilter.addAction(ACTION_PREFIX + "ToggleBluetooth");
                intentfilter.addAction(ACTION_PREFIX + "ToggleNFC");
                intentfilter.addAction(ACTION_PREFIX + "ToggleSoundProfile");
                intentfilter.addAction(ACTION_PREFIX + "ToggleAutoBrightness");
                intentfilter.addAction(ACTION_PREFIX + "ToggleAutoRotation");
                intentfilter.addAction(ACTION_PREFIX + "ToggleMobileData");

                // Tools
//				intentfilter.addAction(ACTION_PREFIX + "RunParasitic");
                //intentfilter.addAction(ACTION_PREFIX + "QueryXposedService");

                mGlobalContext.registerReceiver(mGlobalReceiver, intentfilter);
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "initInternal", new MethodHook() {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                IntentFilter intentfilter = new IntentFilter();
                intentfilter.addAction(ACTION_PREFIX + "SimulateMenu");
                intentfilter.addAction(ACTION_PREFIX + "ForceClose");
                intentfilter.addAction(ACTION_PREFIX + "SaveLastMusicPausedTime");
                final Object thisObject = param.getThisObject();
                mContext.registerReceiver(new BroadcastReceiver() {
                    public void onReceive(final Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action == null) return;

                        if (action.equals(ACTION_PREFIX + "SimulateMenu")) try {
                            Field fRequestShowMenu = XposedHelpers.findField(thisObject.getClass().getSuperclass(), "mRequestShowMenu");
                            fRequestShowMenu.setAccessible(true);
                            fRequestShowMenu.set(thisObject, true);
                            Method markShortcutTriggered = findMethodExact(thisObject.getClass().getSuperclass(), "markShortcutTriggered");
                            markShortcutTriggered.setAccessible(true);
                            markShortcutTriggered.invoke(thisObject);
                            Method injectEvent = findMethodExact(thisObject.getClass().getSuperclass(), "injectEvent", int.class);
                            injectEvent.setAccessible(true);
                            injectEvent.invoke(thisObject, 82);
                        } catch (Throwable t1) {
                            try {
                                Handler mHandler = (Handler)XposedHelpers.getObjectField(thisObject, "mHandler");
                                mHandler.sendMessageDelayed(mHandler.obtainMessage(1, "show_menu"), ViewConfiguration.getLongPressTimeout());
                            } catch (Throwable t2) {
                                XposedHelpers.log(t2);
                            }
                        }

                        if (action.equals(ACTION_PREFIX + "ForceClose")) try {
                            Method closeApp = findMethodExact(thisObject.getClass().getSuperclass(), "closeApp", boolean.class);
                            closeApp.setAccessible(true);
                            closeApp.invoke(thisObject, false);
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }

                        if (action.equals(ACTION_PREFIX + "SaveLastMusicPausedTime")) {
                            Settings.System.putLong(context.getContentResolver(), "last_music_paused_time", currentTimeMillis());
                        }
                    }
                }, intentfilter);
            }
        });
    }

    public static void setupStatusBar(PackageLoadedParam lpparam) {
        Class<?> StatusBarClass = findClassIfExists("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.getClassLoader());
        if (StatusBarClass == null) return;
        ModuleHelper.findAndHookMethod(StatusBarClass, "start", new MethodHook() {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                mStatusBar = param.getThisObject();
                Context mStatusBarContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                IntentFilter intentfilter = new IntentFilter();

                intentfilter.addAction(ACTION_PREFIX + "ExpandNotifications");
                intentfilter.addAction(ACTION_PREFIX + "ExpandSettings");
                intentfilter.addAction(ACTION_PREFIX + "OpenRecents");
                intentfilter.addAction(ACTION_PREFIX + "OpenVolumeDialog");

                intentfilter.addAction(ACTION_PREFIX + "ToggleGPS");
                intentfilter.addAction(ACTION_PREFIX + "ToggleHotspot");
                intentfilter.addAction(ACTION_PREFIX + "ToggleFlashlight");

                intentfilter.addAction(ACTION_PREFIX + "ClearMemory");
                intentfilter.addAction(ACTION_PREFIX + "ClearNotifications");
                intentfilter.addAction(ACTION_PREFIX + "RestartSystemUI");
                intentfilter.addAction(ACTION_PREFIX + "RestartLauncher");
                intentfilter.addAction(ACTION_PREFIX + "RestartSecurityCenter");
                intentfilter.addAction(ACTION_PREFIX + "FloatingWindow");
                intentfilter.addAction(ACTION_PREFIX + "PinningWindow");
                intentfilter.addAction(ACTION_PREFIX + "SwitchOneHanded");
//				intentfilter.addAction(ACTION_PREFIX + "CopyToExternal");
                intentfilter.addAction(ACTION_PREFIX + "FastReboot");

                intentfilter.addAction(ACTION_PREFIX + "ScrollToTop");

                mStatusBarContext.registerReceiver(mSBReceiver, intentfilter);
            }
        });
    }

    enum IntentType {
        APP, ACTIVITY, SHORTCUT
    }

    @SuppressLint("WrongConstant")
    public static Intent getIntent(Context context, String pref, IntentType intentType, boolean skipLock) {
        try {
            if (intentType == IntentType.APP) pref += "_app";
            else if (intentType == IntentType.ACTIVITY) pref += "_activity";
            else if (intentType == IntentType.SHORTCUT) pref += "_shortcut_intent";

            String prefValue = MainModule.mPrefs.getString(pref, null);
            if (prefValue == null) return null;

            Intent intent = new Intent();
            if (intentType == IntentType.SHORTCUT) {
                intent = Intent.parseUri(prefValue, 0);
            } else {
                String[] pkgAppArray = prefValue.split("\\|");
                if (pkgAppArray.length < 2) return null;
                ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
                intent.setComponent(name);
                int user = MainModule.mPrefs.getInt(pref + "_user", 0);
                if (user != 0) intent.putExtra("user", user);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            if (intentType == IntentType.APP) {
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
            }

            if (skipLock) {
                intent.addFlags(335544320);
                intent.putExtra("StartActivityWhenLocked", true);
            }

            return intent;
        } catch (Throwable t) {
            XposedHelpers.log(t);
            return null;
        }
    }

    public static boolean launchAppIntent(Context context, String key, boolean skipLock) {
        return launchIntent(context, getIntent(context, key, IntentType.APP, skipLock));
    }

    public static boolean launchActivityIntent(Context context, String key, boolean skipLock) {
        return launchIntent(context, getIntent(context, key, IntentType.ACTIVITY, skipLock));
    }

    public static boolean launchShortcutIntent(Context context, String key, boolean skipLock) {
        return launchIntent(context, getIntent(context, key, IntentType.SHORTCUT, skipLock));
    }

    public static boolean launchIntent(Context context, Intent intent) {
        if (intent == null) return false;
        Intent bIntent = new Intent(ACTION_PREFIX + "LaunchIntent");
        bIntent.putExtra("intent", intent);
        context.sendBroadcast(bIntent);
        return true;
    }

    public static boolean showSidebar(Context context, Bundle bundle) {
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

    public static boolean toggleThis(Context context, int what) {
        try {
            String whatStr;
            switch (what) {
                case 1: whatStr = "WiFi"; break;
                case 2: whatStr = "Bluetooth"; break;
                case 3: whatStr = "GPS"; break;
                case 4: whatStr = "NFC"; break;
                case 5: whatStr = "SoundProfile"; break;
                case 6: whatStr = "AutoBrightness"; break;
                case 7: whatStr = "AutoRotation"; break;
                case 8: whatStr = "Flashlight"; break;
                case 9: whatStr = "MobileData"; break;
                case 10: whatStr = "Hotspot"; break;
                default: return false;
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
