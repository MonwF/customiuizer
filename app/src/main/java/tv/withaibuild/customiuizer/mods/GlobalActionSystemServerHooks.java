package tv.withaibuild.customiuizer.mods;

import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findMethodExact;
import static tv.withaibuild.customiuizer.mods.GlobalActions.*;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.ViewConfiguration;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import miui.app.MiuiFreeFormManager;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import tv.withaibuild.customiuizer.R;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;

@SuppressWarnings("WeakerAccess")
public class GlobalActionSystemServerHooks {
    public static void setupGlobalActions(XposedModuleInterface.SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "initInternal", new MethodHook() {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
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
                                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
                                    IntentFilter intentfilter = new IntentFilter();
                                    intentfilter.addAction(ACTION_PREFIX + "SimulateMenu");
                                    intentfilter.addAction(ACTION_PREFIX + "ForceClose");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleColorInversion");
                                    intentfilter.addAction(ACTION_PREFIX + "SwitchToPrevApp");
                                    mContext.registerReceiver(new BroadcastReceiver() {
                                        @SuppressLint("MissingPermission")
                                        public void onReceive(final Context context, Intent intent) {
                                            String action = intent.getAction();
                                            if (action == null) return;

                                            if (action.equals(ACTION_PREFIX + "SimulateMenu")) {
                                                try {
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
                                            }
                                            else if (action.equals(ACTION_PREFIX + "ForceClose")) {
                                                try {
                                                    Method closeApp = findMethodExact(thisObject.getClass().getSuperclass(), "closeApp");
                                                    closeApp.setAccessible(true);
                                                    closeApp.invoke(thisObject);
                                                } catch (Throwable t) {
                                                    XposedHelpers.log(t);
                                                }
                                            }
                                            else if (action.equals(ACTION_PREFIX + "ToggleColorInversion")) {
                                                int opt = 0;
                                                try {
                                                    opt = Settings.Secure.getInt(context.getContentResolver(), "accessibility_display_inversion_enabled");
                                                    int conflictProp = (int) ModuleHelper.proxySystemProperties("getInt", "ro.df.effect.conflict", 0, null);
                                                    int conflictProp2 = (int) ModuleHelper.proxySystemProperties("getInt", "ro.vendor.df.effect.conflict", 0, null);
                                                    boolean hasConflict = conflictProp == 1 || conflictProp2 == 1;
                                                    Object dfMgr = XposedHelpers.callStaticMethod(XposedHelpers.findClass("miui.hardware.display.DisplayFeatureManager", null), "getInstance");
                                                    if (hasConflict && opt == 0) XposedHelpers.callMethod(dfMgr, "setScreenEffect", 15, 1);
                                                    Settings.Secure.putInt(context.getContentResolver(), "accessibility_display_inversion_enabled", opt == 0 ? 1 : 0);
                                                    if (hasConflict && opt != 0) XposedHelpers.callMethod(dfMgr, "setScreenEffect", 15, 0);
                                                } catch (Settings.SettingNotFoundException e) {
                                                    XposedHelpers.log(e);
                                                }
                                            }
                                            else if (action.equals(ACTION_PREFIX + "SwitchToPrevApp")) {
                                                PackageManager pm = context.getPackageManager();
                                                ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                                                List<RecentTaskInfo> rti = am.getRecentTasks(15, 0);

                                                Intent recentIntent;
                                                ActivityManager.RunningTaskInfo topAct = am.getRunningTasks(1).get(0);
                                                for (RecentTaskInfo rtitem: rti) {
                                                    if (topAct.topActivity == rtitem.topActivity)
                                                        continue;

                                                    boolean isLauncher = false;
                                                    recentIntent = new Intent(rtitem.baseIntent);
                                                    if (rtitem.origActivity != null)
                                                        recentIntent.setComponent(rtitem.origActivity);
                                                    ComponentName resolvedAct = recentIntent.resolveActivity(pm);
                                                    if (resolvedAct != null && "com.miui.home".equals(resolvedAct.getPackageName())) {
                                                        isLauncher = true;
                                                    }

                                                    if (!isLauncher) {
                                                        try {
                                                            if (rtitem.taskId >= 0)
                                                                am.moveTaskToFront(rtitem.taskId, 0);
                                                            else
                                                                context.startActivity(recentIntent);
                                                            break;
                                                        }
                                                        catch (Throwable e) {
                                                            XposedHelpers.log(e);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }, intentfilter, Context.RECEIVER_EXPORTED);
            
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
                return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void setupStatusBar(PackageReadyParam lpparam) {
        Class<?> StatusBarClass = findClassIfExists("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.getClassLoader());
        if (StatusBarClass == null) return;
        ModuleHelper.findAndHookMethod(StatusBarClass, "start", new MethodHook() {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
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

                                    Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
                                    IntentFilter intentfilter = new IntentFilter();

                                    intentfilter.addAction(ACTION_PREFIX + "ExpandNotifications");
                                    intentfilter.addAction(ACTION_PREFIX + "ExpandSettings");
                                    intentfilter.addAction(ACTION_PREFIX + "OpenRecents");
                                    intentfilter.addAction(ACTION_PREFIX + "OpenVolumeDialog");

                                    intentfilter.addAction(ACTION_PREFIX + "ToggleGPS");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleHotspot");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleZenMode");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleFlashlight");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleNightMode");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleWiFi");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleBluetooth");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleNFC");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleSoundProfile");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleAutoRotation");
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleMobileData");

                                    intentfilter.addAction(ACTION_PREFIX + "ClearMemory");
                                    intentfilter.addAction(ACTION_PREFIX + "ClearNotifications");
                                    intentfilter.addAction(ACTION_PREFIX + "RestartSystemUI");
                                    intentfilter.addAction(ACTION_PREFIX + "RestartLauncher");
                                    intentfilter.addAction(ACTION_PREFIX + "RestartSecurityCenter");
                                    intentfilter.addAction(ACTION_PREFIX + "FloatingWindow");
                                    intentfilter.addAction(ACTION_PREFIX + "SwitchOneHanded");
                                    intentfilter.addAction(ACTION_PREFIX + "FastReboot");

                                    intentfilter.addAction(ACTION_PREFIX + "ScrollToTop");

                                    intentfilter.addAction(ACTION_PREFIX + "WakeUp");
                                    intentfilter.addAction(ACTION_PREFIX + "GoToSleep");
                                    intentfilter.addAction(ACTION_PREFIX + "LockDevice");
                                    intentfilter.addAction(ACTION_PREFIX + "TakeScreenshot");
                                    intentfilter.addAction(ACTION_PREFIX + "OpenPowerMenu");
                                    intentfilter.addAction(ACTION_PREFIX + "VolumeUp");
                                    intentfilter.addAction(ACTION_PREFIX + "VolumeDown");
                                    intentfilter.addAction(ACTION_PREFIX + "GoBack");
                                    intentfilter.addAction(ACTION_PREFIX + "LaunchIntent");
                                    intentfilter.addAction(ACTION_PREFIX + "SaveLastMusicPausedTime");

                                    mContext.registerReceiver(mSBReceiver, intentfilter, Context.RECEIVER_EXPORTED);
            
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
                return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        if (hasActionCode(28)) {
        ModuleHelper.findAndHookMethod("com.android.wm.shell.miuifreeform.MiuiFreeformModeController", lpparam.getClassLoader(), "onInit", new MethodHook() {
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

                                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
                                    IntentFilter intentfilter = new IntentFilter();
                                    intentfilter.addAction(ACTION_PREFIX + "PinningWindow");
                                    BroadcastReceiver mFreeFormReceiver = new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            if (intent.getAction() == null) return;
                                            String action = intent.getAction();
                                            if (action.equals(ACTION_PREFIX + "PinningWindow")) {
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
                                                    List<MiuiFreeFormManager.MiuiFreeFormStackInfo> freeFormStackInfoList = MiuiFreeFormManager.getAllFreeFormStackInfosOnDisplay(0);
                                                    int freeFormCount = 0;
                                                    if (freeFormStackInfoList != null) {
                                                        freeFormCount = freeFormStackInfoList.size();
                                                    }
                                                    if (freeFormCount == 2) return;
                                                    List<Object> rootTaskInfos = (List<Object>) XposedHelpers.callMethod(activityTaskManager, "getAllRootTaskInfosOnDisplay", 0);
                                                    Object freeformController = thisObject;
                                                    for (Object rootTaskInfo : rootTaskInfos) {
                                                        Object conf = XposedHelpers.getObjectField(rootTaskInfo, "configuration");
                                                        Object windowConfiguration = XposedHelpers.getObjectField(conf, "windowConfiguration");
                                                        int wmode = XposedHelpers.getIntField(windowConfiguration, "mWindowingMode");
                                                        int mActivityType = XposedHelpers.getIntField(windowConfiguration, "mActivityType");
                                                        if (wmode < 2 && mActivityType < 2) {
                                                            int taskId = XposedHelpers.getIntField(rootTaskInfo, "taskId");
                                                            XposedHelpers.callMethod(freeformController, "freeformFullscreenTask", taskId);
                                                            Handler myhandler = new Handler(Looper.myLooper());
                                                            Runnable removeBg = new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    myhandler.removeCallbacks(this);
                                                                    XposedHelpers.callMethod(freeformController, "pinAllFreeForm");
                                                                }
                                                            };
                                                            myhandler.postDelayed(removeBg, 200);
                                                            return;
                                                        }
                                                    }
                                                } catch (Throwable err) {
                                                    XposedHelpers.log(err);
                                                }
                                            }
                                        }
                                    };
                                    BroadcastReceiver oldReceiver = (BroadcastReceiver) XposedHelpers.getAdditionalInstanceField(thisObject, "customiuizer_receiver");
                                    if (oldReceiver != null) try { mContext.unregisterReceiver(oldReceiver); } catch (Throwable ignore) {}
                                    XposedHelpers.setAdditionalInstanceField(thisObject, "customiuizer_receiver", mFreeFormReceiver);
                                    mContext.registerReceiver(mFreeFormReceiver, intentfilter, Context.RECEIVER_EXPORTED);
            
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
                return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        }
        if (hasActionCode(29)) {
        ModuleHelper.findAndHookMethod("com.android.wm.shell.sosc.SoScSplitScreenController", lpparam.getClassLoader(), "onInit", new MethodHook() {
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

                                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
                                    IntentFilter intentfilter = new IntentFilter();
                                    intentfilter.addAction(ACTION_PREFIX + "SplitScreen");
                                    BroadcastReceiver mFreeFormReceiver = new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            if (intent.getAction() == null) return;
                                            String action = intent.getAction();
                                            if (action.equals(ACTION_PREFIX + "SplitScreen")) {
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
                                                    Object freeformController = thisObject;
                                                    for (Object rootTaskInfo : rootTaskInfos) {
                                                        Object conf = XposedHelpers.getObjectField(rootTaskInfo, "configuration");
                                                        Object windowConfiguration = XposedHelpers.getObjectField(conf, "windowConfiguration");
                                                        int wmode = XposedHelpers.getIntField(windowConfiguration, "mWindowingMode");
                                                        int mActivityType = XposedHelpers.getIntField(windowConfiguration, "mActivityType");
                                                        if (wmode < 2 && mActivityType < 2) {
                                                            int taskId = XposedHelpers.getIntField(rootTaskInfo, "taskId");
                                                            XposedHelpers.callMethod(freeformController, "startTask", taskId, 0, null);
                                                            return;
                                                        }
                                                    }
                                                } catch (Throwable err) {
                                                    XposedHelpers.log(err);
                                                }
                                            }
                                        }
                                    };
                                    BroadcastReceiver oldReceiver = (BroadcastReceiver) XposedHelpers.getAdditionalInstanceField(thisObject, "customiuizer_receiver");
                                    if (oldReceiver != null) try { mContext.unregisterReceiver(oldReceiver); } catch (Throwable ignore) {}
                                    XposedHelpers.setAdditionalInstanceField(thisObject, "customiuizer_receiver", mFreeFormReceiver);
                                    mContext.registerReceiver(mFreeFormReceiver, intentfilter, Context.RECEIVER_EXPORTED);
            
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
                return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        }
        if (hasToggle(6)) {
        ModuleHelper.hookAllConstructors("com.android.systemui.controlcenter.policy.AutoBrightnessController", lpparam.getClassLoader(),  new MethodHook() {
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

                                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "context");
                                    IntentFilter intentfilter = new IntentFilter();
                                    intentfilter.addAction(ACTION_PREFIX + "ToggleAutoBrightness");
                                    BroadcastReceiver mFreeFormReceiver = new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            if (intent.getAction() == null) return;
                                            String action = intent.getAction();
                                            if (action.equals(ACTION_PREFIX + "ToggleAutoBrightness")) {
                                                Resources modRes = null;
                                                try {
                                                    modRes = ModuleHelper.getModuleRes(mContext);
                                                    boolean enabled = XposedHelpers.getBooleanField(thisObject, "enabled");
                                                    XposedHelpers.callMethod(thisObject, "toggleAutoBrightness");
                                                    if (enabled) {
                                                        Toast.makeText(context, modRes.getString(R.string.toggle_autobright_off), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, modRes.getString(R.string.toggle_autobright_on), Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (Throwable t) {
                                                    XposedHelpers.log(t);
                                                }
                                            }
                                        }
                                    };
                                    BroadcastReceiver oldReceiver = (BroadcastReceiver) XposedHelpers.getAdditionalInstanceField(thisObject, "customiuizer_receiver");
                                    if (oldReceiver != null) try { mContext.unregisterReceiver(oldReceiver); } catch (Throwable ignore) {}
                                    XposedHelpers.setAdditionalInstanceField(thisObject, "customiuizer_receiver", mFreeFormReceiver);
                                    mContext.registerReceiver(mFreeFormReceiver, intentfilter, Context.RECEIVER_EXPORTED);
            
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
                return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        }
    }

}
