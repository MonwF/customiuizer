package name.monwf.customiuizer.mods;

import static name.monwf.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClassIfExists;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedInterface.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.mods.utils.HookerClassHelper;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.ShakeManager;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;

public class Launcher {

    private static GestureDetector mDetectorHorizontal;

    public static void HomescreenSwipesHook(final PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "onVerticalGesture", int.class, MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if ((boolean)XposedHelpers.callMethod(param.getThisObject(), "isInNormalEditingMode")) return;
                String key = null;
                Context helperContext = ((ViewGroup)param.getThisObject()).getContext();
                int numOfFingers = 1;
                if (param.getArgs()[1] != null) numOfFingers = ((MotionEvent)param.getArgs()[1]).getPointerCount();
                if ((int)param.getArgs()[0] == 11) {
                    if (numOfFingers == 1)
                        key = "launcher_swipedown";
                    else if (numOfFingers == 2)
                        key = "launcher_swipedown2";
                    if (GlobalActions.handleAction(helperContext, key)) param.returnAndSkip(true);
                } else if ((int)param.getArgs()[0] == 10) {
                    if (numOfFingers == 1)
                        key = "launcher_swipeup";
                    else if (numOfFingers == 2)
                        key = "launcher_swipeup2";
                    if (GlobalActions.handleAction(helperContext, key)) param.returnAndSkip(true);
                }
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.uioverrides.StatusBarSwipeController", lpparam.getClassLoader(), "canInterceptTouch", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) param.returnAndSkip(false);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.uioverrides.AllAppsSwipeController", lpparam.getClassLoader(), "canInterceptTouch", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) param.returnAndSkip(false);
            }
        });

        // content_center, global_search, notification_bar
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.getClassLoader(), "getPullDownGesture", Context.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) param.setResult("no_action");
            }
        });

        // content_center, global_search
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.getClassLoader(), "getSlideUpGesture", Context.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) param.returnAndSkip("no_action");
            }
        });

        if (ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "isGlobalSearchEnable", Context.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) param.returnAndSkip(false);
            }
        })) {
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.getClassLoader(), "isTopSearchEnable", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) param.returnAndSkip(false);
                }
            });
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.getClassLoader(), "isBottomGlobalSearchEnable", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) param.returnAndSkip(false);
                }
            });
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "isGlobalSearchBottomEffectEnable", Context.class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) param.returnAndSkip(false);
                }
            });
        } else if (!ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "allowedSlidingUpToStartGolbalSearch", Context.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) param.returnAndSkip(false);
            }
        })) if (lpparam.getPackageName().equals("com.miui.home")) XposedHelpers.log("HomescreenSwipesHook", "Cannot disable swipe up search");
    }

    public static void HotSeatSwipesHook(final PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.overlay.assistant.AssistantOverlaySwipeController", lpparam.getClassLoader(), "canInterceptTouch", MotionEvent.class, new MethodHook() {
            private Rect mHotHeatTouchRect = null;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean canInterceptTouch = (boolean) param.getResult();
                if (canInterceptTouch) {
                    if (mHotHeatTouchRect == null) {
                        Object mLauncher = XposedHelpers.getObjectField(param.getThisObject(), "mLauncher");
                        FrameLayout mHotSeats = (FrameLayout) XposedHelpers.callMethod(mLauncher, "getHotSeats");
                        mHotHeatTouchRect = new Rect();
                        mHotSeats.getHitRect(mHotHeatTouchRect);
                    }
                    MotionEvent motionEvent = (MotionEvent) param.getArgs()[0];
                    if (mHotHeatTouchRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                        param.setResult(false);
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.hotseats.HotSeats", lpparam.getClassLoader(), "dispatchTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                MotionEvent ev = (MotionEvent)param.getArgs()[0];
                if (ev == null) return;

                ViewGroup hotSeat = (ViewGroup)param.getThisObject();
                Context helperContext = hotSeat.getContext();
                if (helperContext == null) return;
                if (mDetectorHorizontal == null) mDetectorHorizontal = new GestureDetector(helperContext, new SwipeListenerHorizontal(hotSeat));
                mDetectorHorizontal.onTouchEvent(ev);
            }
        });
    }

    // Listener for horizontal swipes on hotseats
    private static class SwipeListenerHorizontal extends GestureDetector.SimpleOnGestureListener {

        private final int SWIPE_MIN_DISTANCE_HORIZ;
        private final int SWIPE_THRESHOLD_VELOCITY;

        final Context helperContext;

        SwipeListenerHorizontal(Object cellLayout) {
            helperContext = ((ViewGroup)cellLayout).getContext();
            float density = helperContext.getResources().getDisplayMetrics().density;
            SWIPE_MIN_DISTANCE_HORIZ = Math.round(75 * density);
            SWIPE_THRESHOLD_VELOCITY = Math.round(33 * density);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) return false;

            if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                return GlobalActions.handleAction(helperContext, "launcher_swiperight");

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                return GlobalActions.handleAction(helperContext, "launcher_swipeleft");

            return false;
        }
    }

    public static void ShakeHook(final PackageLoadedParam lpparam) {
        final String shakeMgrKey = "MIUIZER_SHAKE_MGR";

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onResume", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ShakeManager shakeMgr = (ShakeManager)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), shakeMgrKey);
                if (shakeMgr == null) {
                    shakeMgr = new ShakeManager((Context)param.getThisObject());
                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), shakeMgrKey, shakeMgr);
                }
                Activity launcherActivity = (Activity)param.getThisObject();
                SensorManager sensorMgr = (SensorManager)launcherActivity.getSystemService(Context.SENSOR_SERVICE);
                shakeMgr.reset();
                sensorMgr.registerListener(shakeMgr, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onPause", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.getThisObject(), shakeMgrKey) == null) return;
                Activity launcherActivity = (Activity)param.getThisObject();
                SensorManager sensorMgr = (SensorManager)launcherActivity.getSystemService(Context.SENSOR_SERVICE);
                sensorMgr.unregisterListener((ShakeManager)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), shakeMgrKey));
            }
        });
    }

    public static void NoClockHideHook(final PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "updateStatusBarClock", long.class, HookerClassHelper.DO_NOTHING);
    }

    private static void modifyTitle(Object thisObject) {
        boolean isApplicatoin = (boolean)XposedHelpers.callMethod(thisObject, "isApplicatoin");
        if (!isApplicatoin) return;
        String pkgName = (String)XposedHelpers.callMethod(thisObject, "getPackageName");
        String actName = (String)XposedHelpers.callMethod(thisObject, "getClassName");
        UserHandle user = (UserHandle)XposedHelpers.getObjectField(thisObject, "user");
        String newTitle = MainModule.mPrefs.getString("launcher_renameapps_list:" + pkgName + "|" + actName + "|" + user.hashCode(), "");
        if (!TextUtils.isEmpty(newTitle)) XposedHelpers.setObjectField(thisObject, "mLabel", newTitle);
    }

    public static void RenameShortcutsHook(final PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
                    @Override
                    public void onChange(String key) {
                        try {
                            if (!key.contains("pref_key_launcher_renameapps_list")) return;
                            CharSequence newTitle = MainModule.mPrefs.getString(key, "");
                            HashSet<?> mAllLoadedApps;
                            if (XposedHelpers.findFieldIfExists(param.getThisObject().getClass(), "mAllLoadedShortcut") != null)
                                mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(param.getThisObject(), "mAllLoadedShortcut");
                            else if (XposedHelpers.findFieldIfExists(param.getThisObject().getClass(), "mAllLoadedApps") != null)
                                mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(param.getThisObject(), "mAllLoadedApps");
                            else
                                mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(param.getThisObject(), "mLoadedAppsAndShortcut");
                            Activity act = (Activity)param.getThisObject();
                            if (mAllLoadedApps != null)
                                for (Object shortcut: mAllLoadedApps) {
                                    boolean isApplicatoin = (boolean)XposedHelpers.callMethod(shortcut, "isApplicatoin");
                                    if (!isApplicatoin) continue;
                                    String pkgName = (String)XposedHelpers.callMethod(shortcut, "getPackageName");
                                    String actName = (String)XposedHelpers.callMethod(shortcut, "getClassName");
                                    UserHandle user = (UserHandle)XposedHelpers.getObjectField(shortcut, "user");
                                    if (("pref_key_launcher_renameapps_list:" + pkgName + "|" + actName + "|" + user.hashCode()).equals(key)) {
                                        CharSequence newStr = TextUtils.isEmpty(newTitle) ? (CharSequence)XposedHelpers.getAdditionalInstanceField(shortcut, "mLabelOrig") : newTitle;
                                        XposedHelpers.setObjectField(shortcut, "mLabel", newStr);

                                        act.runOnUiThread(() -> {
                                            if (lpparam.getPackageName().equals("com.miui.home")) {
                                                XposedHelpers.callMethod(shortcut, "updateBuddyIconView", act);
                                            } else {
                                                Object buddyIconView = XposedHelpers.callMethod(shortcut, "getBuddyIconView");
                                                if (buddyIconView != null) XposedHelpers.callMethod(buddyIconView, "updateInfo", param.getThisObject(), shortcut);
                                            }
                                        });
                                        break;
                                    }
                                }
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    }
                });
            }
        });

        ModuleHelper.hookAllConstructors("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mLabelOrig", XposedHelpers.getObjectField(param.getThisObject(), "mLabel"));
                if (param.getArgs() != null && param.getArgs().length > 0) modifyTitle(param.getThisObject());
            }
        });

        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), "loadToggleInfo", Context.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mLabelOrig", XposedHelpers.getObjectField(param.getThisObject(), "mLabel"));
                modifyTitle(param.getThisObject());
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), "setLabelAndUpdateDB", CharSequence.class, Context.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mLabelOrig", param.getArgs()[0]);
                modifyTitle(param.getThisObject());
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), "load", Context.class, Cursor.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                modifyTitle(param.getThisObject());
            }
        });

        ModuleHelper.hookAllMethodsSilently("com.miui.home.launcher.BaseAppInfo", lpparam.getClassLoader(), "resetTitle", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                modifyTitle(param.getThisObject());
            }
        });
    }

    public static void CloseFolderOnLaunchHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "launch", "com.miui.home.launcher.ShortcutInfo", View.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) != 2) return;
                boolean mHasLaunchedAppFromFolder = XposedHelpers.getBooleanField(param.getThisObject(), "mHasLaunchedAppFromFolder");
                if (mHasLaunchedAppFromFolder) XposedHelpers.callMethod(param.getThisObject(), "closeFolder");
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void FSGesturesHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "usingFsGesture", HookerClassHelper.returnConstant(true));

        ModuleHelper.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader(), "createAndAddNavStubView", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader()), "REAL_FORCE_FSG_NAV_BAR");
                if (!fsg) param.returnAndSkip(null);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader(), "updateFsgWindowState", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader()), "REAL_FORCE_FSG_NAV_BAR");
                if (fsg) return;

                Object mNavStubView = XposedHelpers.getObjectField(param.getThisObject(), "mNavStubView");
                Object mWindowManager = XposedHelpers.getObjectField(param.getThisObject(), "mWindowManager");
                if (mWindowManager != null && mNavStubView != null) {
                    XposedHelpers.callMethod(mWindowManager, "removeView", mNavStubView);
                    XposedHelpers.setObjectField(param.getThisObject(), "mNavStubView", null);
                }
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.getClassLoader(), "getGlobalBoolean", ContentResolver.class, String.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (!"force_fsg_nav_bar".equals(param.getArgs()[1])) return;

                for (StackTraceElement el: Thread.currentThread().getStackTrace()) {
                    if ("com.miui.home.recents.BaseRecentsImpl".equals(el.getClassName())) {
                        XposedHelpers.setAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader()), "REAL_FORCE_FSG_NAV_BAR", param.getResult());
                        param.setResult(true);
                        return;
                    }
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "onTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                MotionEvent event = (MotionEvent)param.getArgs()[0];
                if (event.getAction() != MotionEvent.ACTION_DOWN) return;
                ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
                if (foregroundInfo != null) {
                    String pkgName = foregroundInfo.mForegroundPackageName;
                    if (MainModule.mPrefs.getStringSet("controls_fsg_horiz_apps").contains(pkgName)) param.returnAndSkip(false);
                }
            }
        });
    }

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    public static class DoubleTapController {
        private final long MAX_DURATION = 500;
        private float mActionDownRawX;
        private float mActionDownRawY;
        private int mClickCount;
        public final Context mContext;
        private final String mActionKey;
        private float mFirstClickRawX;
        private float mFirstClickRawY;
        private long mLastClickTime;
        private int mTouchSlop;

        DoubleTapController(Context context, String actionKey) {
            this.mContext = context;
            this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
            this.mActionKey = actionKey;
        }

        boolean isDoubleTapEvent(MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                this.mActionDownRawX = motionEvent.getRawX();
                this.mActionDownRawY = motionEvent.getRawY();
                return false;
            } else if (action != MotionEvent.ACTION_UP) {
                return false;
            } else {
                float rawX = motionEvent.getRawX();
                float rawY = motionEvent.getRawY();
                if (Math.abs(rawX - this.mActionDownRawX) <= ((float) this.mTouchSlop) && Math.abs(rawY - this.mActionDownRawY) <= ((float) this.mTouchSlop)) {
                    if (SystemClock.elapsedRealtime() - this.mLastClickTime > MAX_DURATION || rawY - this.mFirstClickRawY > (float)this.mTouchSlop || rawX - this.mFirstClickRawX > (float)this.mTouchSlop) {
                        this.mClickCount = 0;
                    }
                    this.mClickCount++;
                    if (this.mClickCount == 1) {
                        this.mFirstClickRawX = rawX;
                        this.mFirstClickRawY = rawY;
                        this.mLastClickTime = SystemClock.elapsedRealtime();
                        return false;
                    } else if (Math.abs(rawY - this.mFirstClickRawY) <= ((float) this.mTouchSlop) && Math.abs(rawX - this.mFirstClickRawX) <= ((float) this.mTouchSlop) && SystemClock.elapsedRealtime() - this.mLastClickTime <= MAX_DURATION) {
                        this.mClickCount = 0;
                        return true;
                    }
                }
                this.mClickCount = 0;
                return false;
            }
        }

        void onDoubleTapEvent() {
            GlobalActions.handleAction(mContext, mActionKey);
        }
    }

    public static void LauncherDoubleTapHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs().length != 3) return;
                Object mDoubleTapControllerEx = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mDoubleTapControllerEx");
                if (mDoubleTapControllerEx != null) return;
                mDoubleTapControllerEx = new DoubleTapController((Context)param.getArgs()[0], "launcher_doubletap");
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mDoubleTapControllerEx", mDoubleTapControllerEx);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "dispatchTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                DoubleTapController mDoubleTapControllerEx = (DoubleTapController)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mDoubleTapControllerEx");
                if (mDoubleTapControllerEx == null) return;
                if (!mDoubleTapControllerEx.isDoubleTapEvent((MotionEvent)param.getArgs()[0])) return;
                int mCurrentScreenIndex = XposedHelpers.getIntField(param.getThisObject(), lpparam.getPackageName().equals("com.miui.home") ? "mCurrentScreenIndex" : "mCurrentScreen");
                Object cellLayout = XposedHelpers.callMethod(param.getThisObject(), "getCellLayout", mCurrentScreenIndex);
                if ((boolean)XposedHelpers.callMethod(cellLayout, "lastDownOnOccupiedCell")) return;
                if ((boolean)XposedHelpers.callMethod(param.getThisObject(), "isInNormalEditingMode")) return;
                mDoubleTapControllerEx.onDoubleTapEvent();
            }
        });
    }

    public static void TitleShadowHook(PackageLoadedParam lpparam) {
        if (lpparam.getPackageName().equals("com.miui.home"))
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.getClassLoader(), "getIconTitleShadowColor", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    int color = (int)param.getResult();
                    if (color == Color.TRANSPARENT) return;
                    param.setResult(Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color)));
                }
            }); else
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.getClassLoader(), "getTitleShadowColor", int.class, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    int color = (int)param.getResult();
                    if (color == Color.TRANSPARENT) return;
                    param.setResult(Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color)));
                }
            });
    }

    public static void HideNavBarHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "loadScreenSize", Context.class, Resources.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Settings.Global.putInt(((Context)param.getArgs()[0]).getContentResolver(), "force_immersive_nav_bar", 1);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.recents.views.RecentsContainer", lpparam.getClassLoader(), "showLandscapeOverviewGestureView", boolean.class, HookerClassHelper.DO_NOTHING);
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.getClassLoader(), "isMistakeTouch", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                View navView = (View) param.getThisObject();
                boolean misTouch = false;
                boolean setting = Settings.Global.getInt(navView.getContext().getContentResolver(), "show_mistake_touch_toast", 1) != 0;
                if (setting) {
                    boolean mIsShowStatusBar = XposedHelpers.getBooleanField(param.getThisObject(), "mIsShowStatusBar");
                    if (!mIsShowStatusBar) {
                        misTouch = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isLandScapeActually");
                    }
                }
                param.returnAndSkip(misTouch);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.getClassLoader(), "onPointerEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean mIsInFsMode = XposedHelpers.getBooleanField(param.getThisObject(), "mIsInFsMode");
                if (!mIsInFsMode) {
                    MotionEvent motionEvent = (MotionEvent) param.getArgs()[0];
                    if (motionEvent.getAction() == 0) {
                        XposedHelpers.setObjectField(param.getThisObject(), "mHideGestureLine", true);
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.getClassLoader(), "updateScreenSize", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                XposedHelpers.setObjectField(param.getThisObject(), "mHideGestureLine", false);
            }
        });
    }

    public static void HideSeekPointsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.pageindicators.AllAppsIndicator", lpparam.getClassLoader(), "shouldHide", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.pageindicators.AllAppsIndicator", lpparam.getClassLoader(), "hideAllAppsArrow", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mLauncher = XposedHelpers.getObjectField(param.getThisObject(), "mLauncher");
                if (mLauncher == null) return;
                View workspace = (View) XposedHelpers.getObjectField(mLauncher, "mWorkspace");
                boolean isInEditingMode = (boolean)XposedHelpers.callMethod(workspace, "isInNormalEditingMode");
                Context mContext = workspace.getContext();
                Handler mHandler = (Handler)XposedHelpers.getAdditionalInstanceField(workspace, "mHandlerEx");
                if (mHandler == null) {
                    mHandler = new Handler(mContext.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            View seekBar = (View)msg.obj;
                            if (seekBar != null) {
                                seekBar.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        seekBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    };
                    XposedHelpers.setAdditionalInstanceField(workspace, "mHandlerEx", mHandler);
                }
                if (mHandler.hasMessages(666)) mHandler.removeMessages(666);
                View mScreenSeekBar = (View)XposedHelpers.getObjectField(param.getThisObject(), "mScreenIndicator");
                mScreenSeekBar.animate().cancel();
                if (!isInEditingMode && MainModule.mPrefs.getBoolean("launcher_hideseekpoints_edit")) {
                    mScreenSeekBar.setAlpha(0.0f);
                    mScreenSeekBar.setVisibility(View.GONE);
                    return;
                }
                mScreenSeekBar.setVisibility(View.VISIBLE);
                mScreenSeekBar.animate().alpha(1.0f).setDuration(300);
                if (!isInEditingMode) {
                    Message msg = Message.obtain(mHandler, 666);
                    msg.obj = mScreenSeekBar;
                    mHandler.sendMessageDelayed(msg, 600);
                }
            }
        });
    }

    public static void InfiniteScrollHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.getClassLoader(), "getSnapToScreenIndex", int.class, int.class, int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs()[0] != param.getResult()) return;
                int screenCount = (int)XposedHelpers.callMethod(param.getThisObject(), "getScreenCount");
                if ((int)param.getArgs()[2] == -1 && (int)param.getArgs()[0] == 0)
                    param.setResult(screenCount);
                else if ((int)param.getArgs()[2] == 1 && (int)param.getArgs()[0] == screenCount - 1)
                    param.setResult(0);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.getClassLoader(), "getSnapUnitIndex", int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int mCurrentScreenIndex = XposedHelpers.getIntField(param.getThisObject(), lpparam.getPackageName().equals("com.miui.home") ? "mCurrentScreenIndex" : "mCurrentScreen");
                if (mCurrentScreenIndex != (int)param.getResult()) return;
                int screenCount = (int)XposedHelpers.callMethod(param.getThisObject(), "getScreenCount");
                if ((int)param.getResult() == 0)
                    param.setResult(screenCount);
                else if ((int)param.getResult() == screenCount - 1)
                    param.setResult(0);
            }
        });
    }

    public static void UnlockGridsRes() {
        MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_x", 3);
        MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_y", 4);
        MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_x_min", 3);
        MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_y_min", 4);
        MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_x_max", 8);
        MainModule.resHooks.setObjectReplacement("com.miui.home", "integer", "config_cell_count_y_max", 10);
    }

    public static void UnlockGridsHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethodsSilently("com.miui.home.launcher.compat.LauncherCellCountCompatDevice", lpparam.getClassLoader(), "shouldUseDeviceValue", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.compat.LauncherCellCountCompatDeviceFold", lpparam.getClassLoader(), "shouldUseDeviceValue", Context.class, int.class, HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.miui.home.settings.MiuiHomeSettings", lpparam.getClassLoader(), "onCreatePreferences", Bundle.class, String.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mScreenCellsConfig"), "setVisible", true);
            }
        });
        Class <?> DeviceConfigClass = XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod(DeviceConfigClass, "loadCellsCountConfig", Context.class, boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int sCellCountY = (int) XposedHelpers.getStaticObjectField(DeviceConfigClass, "sCellCountY");
                if (sCellCountY > 6) {
                    int cellHeight = (int) XposedHelpers.callStaticMethod(DeviceConfigClass, "getCellHeight");
                    XposedHelpers.setStaticObjectField(DeviceConfigClass, "sFolderCellHeight", cellHeight);
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenUtils", lpparam.getClassLoader(), "getScreenCellsSizeOptions", Context.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                ArrayList<CharSequence> arrayList = new ArrayList<>();
                int cellCountXMin = 3;
                int cellCountXMax = 8;
                int cellCountYMin = 4;
                int cellCountYMax = 10;
                while (cellCountXMin <= cellCountXMax) {
                    for (int i = cellCountYMin; i <= cellCountYMax; i++) {
                        arrayList.add(cellCountXMin + "x" + i);
                    }
                    cellCountXMin++;
                }
                param.returnAndSkip(arrayList);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.compat.LauncherCellCountCompatNoWord", lpparam.getClassLoader(), "setLoadResCellConfig", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = true;
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "isCellSizeChangedByTheme", new MethodHook() {
            HookerClassHelper.CustomMethodUnhooker nowordHook;
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                nowordHook = ModuleHelper.findAndHookMethod("com.miui.home.launcher.common.Utilities", lpparam.getClassLoader(), "isNoWordModel", HookerClassHelper.returnConstant(false));
            }
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (nowordHook != null) nowordHook.unhook();
                nowordHook = null;
            }
        });
    }

    public static void FolderColumnsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int cols = MainModule.mPrefs.getInt("launcher_folder_cols", 1);

                GridView mContent = (GridView)XposedHelpers.getObjectField(param.getThisObject(), "mContent");
                mContent.setNumColumns(cols);

                if (MainModule.mPrefs.getBoolean("launcher_folderwidth")) {
                    ViewGroup.LayoutParams lp = mContent.getLayoutParams();
                    lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    mContent.setLayoutParams(lp);
                }

                if (cols > 3 && MainModule.mPrefs.getBoolean("launcher_folderspace")) {
                    ViewGroup mBackgroundView = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mBackgroundView");
                    if (mBackgroundView != null)
                        mBackgroundView.setPadding(
                            mBackgroundView.getPaddingLeft() / 3,
                            mBackgroundView.getPaddingTop(),
                            mBackgroundView.getPaddingRight() / 3,
                            mBackgroundView.getPaddingBottom()
                        );
                }
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.Folder", lpparam.getClassLoader(), "onLayout", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (!MainModule.mPrefs.getBoolean("launcher_folderwidth")) return;
                GridView mContent = (GridView)XposedHelpers.getObjectField(param.getThisObject(), "mContent");
                ImageView mFakeIcon = (ImageView)XposedHelpers.getObjectField(param.getThisObject(), "mFakeIcon");
                mFakeIcon.layout(mContent.getLeft(), mContent.getTop(), mContent.getRight(), mContent.getTop() + mContent.getWidth());
            }
        });
    }

    public static void IconScaleHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ShortcutIcon", lpparam.getClassLoader(), "restoreToInitState", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mIconContainer");
                if (mIconContainer == null || mIconContainer.getChildAt(0) == null) return;
                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
                mIconContainer.getChildAt(0).setScaleX(multx);
                mIconContainer.getChildAt(0).setScaleY(multx);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);

                ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mIconContainer");
                if (mIconContainer != null && mIconContainer.getChildAt(0) != null) {
                    mIconContainer.getChildAt(0).setScaleX(multx);
                    mIconContainer.getChildAt(0).setScaleY(multx);
                    mIconContainer.setClipToPadding(false);
                    mIconContainer.setClipChildren(false);
                }

                if (multx > 1) {
                    final TextView mMessage = (TextView)XposedHelpers.getObjectField(param.getThisObject(), "mMessage");
                    if (mMessage != null)
                        mMessage.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {}

                            @Override
                            public void afterTextChanged(Editable s) {
                                int maxWidth = mMessage.getResources().getDimensionPixelSize(mMessage.getResources().getIdentifier("icon_message_max_width", "dimen", lpparam.getPackageName()));
                                mMessage.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST));
                                mMessage.setTranslationX(-mMessage.getMeasuredWidth() * (multx - 1) / 2f);
                                mMessage.setTranslationY(mMessage.getMeasuredHeight() * (multx - 1) / 2f);
                            }
                        });
                }

                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mMessageAnimationOrig", XposedHelpers.getObjectField(param.getThisObject(), "mMessageAnimation"));
                XposedHelpers.setObjectField(param.getThisObject(), "mMessageAnimation", new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Runnable mMessageAnimationOrig = (Runnable)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mMessageAnimationOrig");
                            mMessageAnimationOrig.run();
                            boolean mIsShowMessageAnimation = XposedHelpers.getBooleanField(param.getThisObject(), "mIsShowMessageAnimation");
                            if (mIsShowMessageAnimation) {
                                View mMessage = (View)XposedHelpers.getObjectField(param.getThisObject(), "mMessage");
                                mMessage.animate().cancel();
                                mMessage.animate().scaleX(multx).scaleY(multx).setStartDelay(0).start();
                            }
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    }
                });

//				if (mult <= 1) return;
//				TextView mMessage = (TextView)XposedHelpers.getObjectField(param.getThisObject(), "mMessage");
//				if (mMessage != null) {
//					int width = mMessage.getResources().getDimensionPixelSize(mMessage.getResources().getIdentifier("icon_message_max_width", "dimen", lpparam.getPackageName()));
//					mMessage.setTranslationX(-width/2f * (1f - 1f / mult));
//					mMessage.setTranslationY(width/2f * (1f - 1f / mult));
//				}
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "getIconLocation", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
                Rect rect = (Rect)param.getResult();
                if (rect == null) return;
                rect.right = rect.left + Math.round(rect.width() * multx);
                rect.bottom = rect.top + Math.round(rect.height() * multx);
                param.setResult(rect);
            }
        });

        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.gadget.ClearButton", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mIconContainer");
                if (mIconContainer == null || mIconContainer.getChildAt(0) == null) return;
                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
                mIconContainer.getChildAt(0).setScaleX(multx);
                mIconContainer.getChildAt(0).setScaleY(multx);
            }
        });

//		ModuleHelper.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.getClassLoader(), "onOpen", boolean.class, new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				XposedHelpers.setFloatField(param.getThisObject(), "mItemIconToPreviewIconScale", -1.0f);
//			}
//		});
//
//		ModuleHelper.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.getClassLoader(), "changeItemsInFolderDuringOpenAndCloseAnimation", float.class, new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
//				ViewGroup mContent = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mContent");
//				for (int i = 0; i < mContent.getChildCount(); i++) {
//					String cls = mContent.getChildAt(i).getClass().getSimpleName();
//					if ("ItemIcon".equals(cls) || "ShortcutIcon".equals(cls) || "FolderIcon".equals(cls)) {
//						View iconContainer = (View)XposedHelpers.callMethod(mContent.getChildAt(i), "getIconContainer");
//						float mult = (float)param.getArgs()[0] * multx;
//						iconContainer.setScaleX(mult);
//						iconContainer.setScaleY(mult);
//					}
//				}
//			}
//		});
    }

    public static void TitleFontSizeHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                TextView mTitle = (TextView)XposedHelpers.getObjectField(param.getThisObject(), "mTitle");
                if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
            }
        });

        if (lpparam.getPackageName().equals("com.mi.android.globallauncher"))
            ModuleHelper.hookAllMethods("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "setTitleColorMode", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    TextView mTitle = (TextView)XposedHelpers.getObjectField(param.getThisObject(), "mTitle");
                    if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
                }
            });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.getClassLoader(), "fromXml", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object buddyIcon = XposedHelpers.callMethod(param.getArgs()[3], "getBuddyIconView", param.getArgs()[2]);
                if (buddyIcon == null) return;
                TextView mTitle = (TextView)XposedHelpers.getObjectField(buddyIcon, "mTitle");
                if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
            }
        });

        if (lpparam.getPackageName().equals("com.miui.home")) {
            ModuleHelper.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.getClassLoader(), "createShortcutIcon", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Object buddyIcon = param.getResult();
                    if (buddyIcon == null) return;
                    TextView mTitle = (TextView)XposedHelpers.getObjectField(buddyIcon, "mTitle");
                    if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
                }
            });

            ModuleHelper.hookAllMethods("com.miui.home.launcher.common.Utilities", lpparam.getClassLoader(), "adaptTitleStyleToWallpaper", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    TextView mTitle = (TextView)param.getArgs()[1];
                    if (mTitle != null && mTitle.getId() == mTitle.getResources().getIdentifier("icon_title", "id", "com.miui.home"))
                        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));
                }
            });
        }
    }

    public static void TitleTopMarginHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ViewGroup mTitleContainer = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mTitleContainer");
                if (mTitleContainer == null) return;
                ViewGroup.LayoutParams lp = mTitleContainer.getLayoutParams();
                int opt = Math.round((MainModule.mPrefs.getInt("launcher_titletopmargin", 0) - 11) * mTitleContainer.getResources().getDisplayMetrics().density);
                if (lp instanceof RelativeLayout.LayoutParams) {
                    ((RelativeLayout.LayoutParams)lp).topMargin = opt;
                    mTitleContainer.setLayoutParams(lp);
                } else {
                    mTitleContainer.setTranslationY(opt);
                    mTitleContainer.setClipChildren(false);
                    mTitleContainer.setClipToPadding(false);
                    ((ViewGroup)mTitleContainer.getParent()).setClipChildren(false);
                    ((ViewGroup)mTitleContainer.getParent()).setClipToPadding(false);
                }
            }
        });
    }

    public static void PrivacyFolderHook(PackageLoadedParam lpparam) {
        if (MainModule.mPrefs.getBoolean("launcher_privacyapps_gest")) {
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "registerBroadcastReceivers", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    final Activity act = (Activity)param.getThisObject();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.telephony.action.SECRET_CODE");
                    intentFilter.addDataAuthority("233233", null);
                    intentFilter.addDataScheme("android_secret_code");

                    act.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            try {
                                if (intent.getAction() == null) return;
                                if ("android.telephony.action.SECRET_CODE".equals(intent.getAction())) {
                                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "fromSecretCode", true);
                                    XposedHelpers.callMethod(param.getThisObject(), "startSecurityHide");
                                }
                            } catch (Throwable t) {
                                XposedHelpers.log(t);
                            }
                        }
                    }, intentFilter);
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "startSecurityHide", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "fromSecretCode") != null) {
                    XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "fromSecretCode");
                    return;
                }
                if (GlobalActions.handleAction((Activity)param.getThisObject(), "launcher_spread")) {
                    param.returnAndSkip(null);
                    return;
                }
                boolean opt = MainModule.mPrefs.getBoolean("launcher_privacyapps_gest");
                if (opt) param.returnAndSkip(null);
            }
        });
    }

    public static void HideTitlesHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View mTitleContainer = (View)XposedHelpers.getObjectField(param.getThisObject(), "mTitleContainer");
                if (mTitleContainer != null) mTitleContainer.setVisibility(View.GONE);
            }
        });
    }

    public static void HorizontalSpacingRes() {
        int opt = MainModule.mPrefs.getInt("launcher_horizmargin", 0) - 21;
        MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "workspace_cell_padding_side", opt);
        MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "workspace_cell_padding_side_no_word", opt);
        MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "workspace_cell_padding_side_rotatable", opt);
        MainModule.resHooks.setDensityReplacement("com.mi.android.globallauncher", "dimen", "workspace_cell_padding_side", opt);
    }

    public static void IndicatorHeightRes() {
        int opt = MainModule.mPrefs.getInt("launcher_indicatorheight", 9);
        MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "slide_bar_height", opt);
        MainModule.resHooks.setDensityReplacement("com.mi.android.globallauncher", "dimen", "slide_bar_height", opt);
    }

    public static void ShowHotseatTitlesRes() {
        MainModule.resHooks.setObjectReplacement("com.miui.home", "bool", "config_hide_hotseats_app_title", false);
        MainModule.resHooks.setObjectReplacement("com.mi.android.globallauncher", "bool", "config_hide_hotseats_app_title", false);
    }

    public static void FolderBlurHook(PackageLoadedParam lpparam) {
        Class<?> BlurUtils = findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.getClassLoader());
        if (BlurUtils != null) {
            ModuleHelper.hookAllMethods(BlurUtils, "getLauncherBlur", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    boolean isFolderShowing = (boolean) XposedHelpers.callMethod(param.getArgs()[0], "isFolderShowing");
                    if (isFolderShowing) {
                        int blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0);
                        float blurRatio = blurPct / 100f;
                        param.returnAndSkip(blurRatio);
                    }
                }
            });

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.getClassLoader(), "open", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Activity launcher = (Activity) XposedHelpers.getObjectField(param.getThisObject(), "mLauncher");

                    int blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0);
                    float blurRatio = blurPct / 100f;
                    XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", blurRatio, launcher.getWindow(), true);
                }
            });

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.getClassLoader(), "close", boolean.class, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Activity launcher = (Activity) XposedHelpers.getObjectField(param.getThisObject(), "mLauncher");
                    XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", 0f, launcher.getWindow(), param.getArgs()[0]);
                }
            });

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "cancelShortcutMenu", int.class, "com.miui.home.launcher.shortcuts.CancelShortcutMenuReason", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    boolean isFolderShowing = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isFolderShowing");
                    if (isFolderShowing) {
                        int blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0);
                        float blurRatio = blurPct / 100f;
                        Activity launcher = (Activity) param.getThisObject();
                        XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", blurRatio, launcher.getWindow(), true);
                    }
                }
            });
        }
    }

    private static float scaleStiffness(float val, float scale) {
        return (scale < 1.0f ? 2f / scale : 1.0f / scale) * val;
    }

    public static void FixAnimHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.animate.SpringAnimator", lpparam.getClassLoader(), "getSpringForce", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                float scale = Helpers.getAnimationScale(2);
                if (scale == 1.0f) return;
                if (scale == 0) scale = 0.01f;
                param.getArgs()[2] = scaleStiffness((float)param.getArgs()[2], scale);
            }
        });

        MethodHook hook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                float scale = Helpers.getAnimationScale(2);
                if (scale == 1.0f) return;
                if (scale == 0) scale = 0.01f;
                XposedHelpers.setFloatField(param.getThisObject(), "mCenterXStiffness", scaleStiffness(XposedHelpers.getFloatField(param.getThisObject(), "mCenterXStiffness"), scale));
                XposedHelpers.setFloatField(param.getThisObject(), "mCenterYStiffness", scaleStiffness(XposedHelpers.getFloatField(param.getThisObject(), "mCenterYStiffness"), scale));
                XposedHelpers.setFloatField(param.getThisObject(), "mWidthStiffness", scaleStiffness(XposedHelpers.getFloatField(param.getThisObject(), "mWidthStiffness"), scale));
                XposedHelpers.setFloatField(param.getThisObject(), "mRadiusStiffness", scaleStiffness(XposedHelpers.getFloatField(param.getThisObject(), "mRadiusStiffness"), scale));
                XposedHelpers.setFloatField(param.getThisObject(), "mAlphaStiffness", scaleStiffness(XposedHelpers.getFloatField(param.getThisObject(), "mAlphaStiffness"), scale));
                try {
                    XposedHelpers.setFloatField(param.getThisObject(), "mRatioStiffness", scaleStiffness(XposedHelpers.getFloatField(param.getThisObject(), "mRatioStiffness"), scale));
                } catch (Throwable t) {
                    XposedHelpers.setFloatField(param.getThisObject(), "mRadioStiffness", scaleStiffness(XposedHelpers.getFloatField(param.getThisObject(), "mRadioStiffness"), scale));
                }
            }
        };

        if (!ModuleHelper.hookAllMethodsSilently("com.miui.home.recents.util.RectFSpringAnim", lpparam.getClassLoader(), "start", hook))
            ModuleHelper.hookAllMethods("com.miui.home.recents.util.RectFSpringAnim", lpparam.getClassLoader(), "initAllAnimations", hook);

//		if (XposedHelpers.findClassIfExists("com.android.systemui.shared.recents.system.RemoteAnimationAdapterCompat", lpparam.getClassLoader()) != null)
//		Helpers.hookAllConstructors("com.android.systemui.shared.recents.system.RemoteAnimationAdapterCompat", lpparam.getClassLoader(), new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				float scale = Helpers.getAnimationScale(2);
//				if (scale == 1.0f) return;
//				param.getArgs()[1] = (long)((long)param.getArgs()[1] * scale);
//				param.getArgs()[2] = (long)((long)param.getArgs()[2] * scale);
//			}
//		});
    }

    public static void DockMarginTopHook(PackageLoadedParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_dock_topmargin", 0);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "calcHotSeatsMarginTop", Context.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(Math.round(Helpers.dp2px(opt)));
            }
        });
    }
    public static void DockMarginBottomHook(PackageLoadedParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_dock_bottommargin", 0);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "calcHotSeatsMarginBottom", Context.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(Math.round(Helpers.dp2px(opt)));
            }
        });
    }
    public static void WorkspaceCellPaddingTopHook(PackageLoadedParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_topmargin", 0) - 21;
        MethodHook hook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(Math.round(Helpers.dp2px(opt)));
            }
        };

        boolean newLauncher = ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "getWorkspaceCellPaddingTop", Context.class, hook);
        if (!newLauncher) {
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "getWorkspaceCellPaddingTop", hook);
        }
    }

    public static void IndicatorMarginTopHook(PackageLoadedParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_indicator_topmargin", 0) - 21;
        MainModule.resHooks.setDensityReplacement("com.miui.home", "dimen", "slide_bar_margin_top", opt);
        MainModule.resHooks.setDensityReplacement("com.mi.android.globallauncher", "dimen", "slide_bar_margin_top", opt);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.util.DimenUtils1X", lpparam.getClassLoader(), "getDimensionPixelSize", Context.class, String.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String resKey = (String) param.getArgs()[1];
                if ("slide_bar_margin_top".equals(resKey)) {
                    param.returnAndSkip(Math.round(Helpers.dp2px(opt)));
                }
            }
        });
    }

    public static void HorizontalWidgetSpacingHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "getMiuiWidgetSizeSpec", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs().length < 4) return;
                long spec = (long)param.getResult();
                long width = spec >> 32;
                long height = spec - ((spec >> 32) << 32);
                int opt = Math.round((MainModule.mPrefs.getInt("launcher_horizwidgetmargin", 0) - 21) * Resources.getSystem().getDisplayMetrics().density) * 2;
                width -= opt;
                param.setResult((width << 32) | height);
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.MIUIWidgetUtil", lpparam.getClassLoader(), "getMiuiWidgetPadding", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                param.setResult(new Rect());
            }
        });
    }

    public static void FixAppInfoLaunchHook(PackageLoadedParam lpparam) {
        if (lpparam.getPackageName().equals("com.mi.android.globallauncher"))
            ModuleHelper.hookAllMethods("com.miui.home.launcher.util.Utilities", lpparam.getClassLoader(), "startDetailsActivityForInfo", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    Object itemInfo = param.getArgs()[0];
                    ComponentName component;
                    try {
                        component = (ComponentName)XposedHelpers.callMethod(itemInfo, "getComponentName");
                    } catch (Throwable t1) {
                        try {
                            component = (ComponentName)XposedHelpers.callMethod(XposedHelpers.getObjectField(itemInfo, "intent"), "getComponent");
                        } catch (Throwable t2) {
                            try {
                                component = (ComponentName)XposedHelpers.getObjectField(itemInfo, "providerName");
                            } catch (Throwable t3) {
                                component = (ComponentName)XposedHelpers.getObjectField(XposedHelpers.getObjectField(itemInfo, "providerInfo"), "provider");
                            }
                        }
                    }
                    if (component == null) return;
                    Context context = (Context)param.getArgs()[1];
                    if (context == null) return;
                    UserHandle userHandle = (UserHandle)XposedHelpers.callMethod(param.getArgs()[0], "getUser");
                    ModuleHelper.openAppInfo(context, component.getPackageName(), userHandle != null ? userHandle.hashCode() : 0);
                    param.returnAndSkip(true);
                }
            });
        else
            ModuleHelper.hookAllMethods("com.miui.home.launcher.shortcuts.ShortcutMenuManager", lpparam.getClassLoader(), "startAppDetailsActivity", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    ComponentName component = (ComponentName)XposedHelpers.callMethod(param.getArgs()[0], "getComponentName");
                    if (component == null) return;
                    View view = (View)param.getArgs()[1];
                    if (view == null) return;
                    UserHandle userHandle = (UserHandle)XposedHelpers.callMethod(param.getArgs()[0], "getUserHandle");
                    ModuleHelper.openAppInfo(view.getContext(), component.getPackageName(), userHandle != null ? userHandle.hashCode() : 0);
                    param.returnAndSkip(null);
                }
            });
    }

    public static void NoWidgetOnlyHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.CellLayout", lpparam.getClassLoader(), "setScreenType", int.class, new MethodHook() {
            @Override
            protected void before(BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = 0;
            }
        });
    }

    public static void NoUnlockAnimationHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.getClassLoader(), "isSystemAnimationOpen", HookerClassHelper.returnConstant(false));
    }

    public static void NoZoomAnimationHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.getClassLoader(), "startShortcutMenuLayerFadeOutAnim", HookerClassHelper.DO_NOTHING);
        ModuleHelper.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.getClassLoader(), "startShortcutMenuLayerFadeInAnim", HookerClassHelper.DO_NOTHING);
    }

    public static void UseOldLaunchAnimationHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.recents.QuickstepAppTransitionManagerImpl", lpparam.getClassLoader(), "hasControlRemoteAppTransitionPermission", HookerClassHelper.returnConstant(false));
    }

    public static void ReverseLauncherPortraitHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
            @Override
            @SuppressLint("SourceLockedOrientationActivity")
            protected void after(AfterHookCallback param) throws Throwable {
                Activity act = (Activity)param.getThisObject();
                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        });
    }

    public static void HideFromRecentsHook(PackageLoadedParam lpparam) {
        Class<?> ActiviyManagerWrapper = findClassIfExists("com.android.systemui.shared.recents.system.ActivityManagerWrapper", lpparam.getClassLoader());
        Class<?> TaskInfoCompat = findClassIfExists("com.android.systemui.shared.recents.model.GroupedRecentTaskInfoCompat", lpparam.getClassLoader());
        if (TaskInfoCompat == null) {
            XposedHelpers.log("HideFromRecentsHook", "hook failed");
            return;
        }
        ModuleHelper.findAndHookMethod(ActiviyManagerWrapper, "needRemoveTask", TaskInfoCompat, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs()[0] != null) {
                    Object mainTask = XposedHelpers.getObjectField(param.getArgs()[0], "mMainTaskInfo");
                    if (mainTask != null) {
                        ComponentName componentName = (ComponentName) XposedHelpers.getObjectField(mainTask, "topActivity");
                        if (componentName != null) {
                            String pkgName = componentName.getPackageName();
                            Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_hidefromrecents_apps");
                            if (selectedApps.contains(pkgName)) {
                                param.setResult(true);
                            }
                        }
                    }
                }
            }
        });
    }

    public static void MaxHotseatIconsCountHook(PackageLoadedParam lpparam) {
        String methodName = lpparam.getPackageName().equals("com.mi.android.globallauncher") ? "getHotseatCount" : "getHotseatMaxCount";
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), methodName, HookerClassHelper.returnConstant(666));
    }

    public static void RecentsBlurRatioHook(PackageLoadedParam lpparam) {
        Class<?> utilsClass = findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.getClassLoader());
        if (utilsClass == null) {
            XposedHelpers.log("RecentsBlurRatioHook", "Cannot find blur utility class");
            return;
        }

        ModuleHelper.hookAllMethods(utilsClass, "fastBlurWhenEnterRecents", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean mIsFromFsGesture = XposedHelpers.getBooleanField(param.getArgs()[1], "mIsFromFsGesture");
                if (!mIsFromFsGesture) {
                    Activity launcher = (Activity) param.getArgs()[0];
                    float blurRatio = MainModule.mPrefs.getInt("system_recents_blur", 100) / 100f;
                    XposedHelpers.callStaticMethod(utilsClass, "fastBlur", blurRatio, launcher.getWindow(), param.getArgs()[2]);
                    param.returnAndSkip(null);
                }
            }
        });
        ModuleHelper.hookAllMethods(utilsClass, "fastBlurWhenGestureResetTaskView", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                XposedHelpers.setAdditionalStaticField(utilsClass, "customBlurRatio", true);
            }
        });

        ModuleHelper.hookAllMethods(utilsClass, "fastBlur", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (param.getArgs().length == 3) {
                    if (XposedHelpers.getAdditionalStaticField(utilsClass, "customBlurRatio") != null) {
                        float blurRatio = MainModule.mPrefs.getInt("system_recents_blur", 100) / 100f;
                        param.getArgs()[0] = blurRatio;
                        XposedHelpers.removeAdditionalStaticField(utilsClass, "customBlurRatio");
                    }
                }
            }
        });
    }

    public static void CloseFolderOrDrawerOnLaunchShortcutMenuHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.shortcuts.AppShortcutMenuItem", lpparam.getClassLoader(), "getOnClickListener", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                final View.OnClickListener listener = (View.OnClickListener)param.getResult();
                param.setResult(new View.OnClickListener() {
                    public final void onClick(View view) {
                        listener.onClick(view);
                        Class<?> appCls = findClassIfExists("com.miui.home.launcher.Application", lpparam.getClassLoader());
                        if (appCls == null) return;
                        Object launcher = XposedHelpers.callStaticMethod(appCls, "getLauncher");
                        if (launcher == null) return;
                        if (MainModule.mPrefs.getBoolean("launcher_closedrawer")) XposedHelpers.callMethod(launcher, "hideAppView");
                        if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) > 1) XposedHelpers.callMethod(launcher, "closeFolder");
                    }
                });
            }
        });
    }

    public static void StickyFloatingWindowsLauncherHook(PackageLoadedParam lpparam) {
        final List<String> fwBlackList = new ArrayList<String>();
        fwBlackList.add("com.miui.securitycenter");
        fwBlackList.add("com.miui.home");
        fwBlackList.add("com.android.camera");
        ModuleHelper.findAndHookMethod("com.miui.home.recents.views.RecentsContainer", lpparam.getClassLoader(), "onAttachedToWindow", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.callMethod(param.getThisObject(), "getContext");
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            String pkgName = intent.getStringExtra("package");
                            if (pkgName != null) {
                                XposedHelpers.callMethod(param.getThisObject(), "dismissRecentsToLaunchTargetTaskOrHome", pkgName, true);
                            }
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    }
                }, new IntentFilter(ACTION_PREFIX + "dismissRecentsWhenFreeWindowOpen"));
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher$PerformLaunchAction", lpparam.getClassLoader(), "run", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Intent intent = (Intent) XposedHelpers.getObjectField(param.getThisObject(), "mIntent");
                if (intent != null) {
                    String pkgName = intent.getComponent().getPackageName();
                    if (fwBlackList.contains(pkgName)) {
                        return;
                    }
                    Object launcher = XposedHelpers.getSurroundingThis(param.getThisObject());
                    Object mAppTransitionManager = XposedHelpers.getObjectField(launcher, "mAppTransitionManager");
                    String fwApps = (String) XposedHelpers.getAdditionalInstanceField(launcher, "fwApps");
                    if (fwApps != null && fwApps.contains(pkgName)) {
                        XposedHelpers.setAdditionalInstanceField(mAppTransitionManager, "isFwApps", true);
                    }
                }
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object launcher = XposedHelpers.getSurroundingThis(param.getThisObject());
                Object mAppTransitionManager = XposedHelpers.getObjectField(launcher, "mAppTransitionManager");
                XposedHelpers.removeAdditionalInstanceField(mAppTransitionManager, "isFwApps");
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "launch", "com.miui.home.launcher.ShortcutInfo", View.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Activity act = (Activity) param.getThisObject();
                String fwApps = Settings.Global.getString(act.getContentResolver(), Helpers.modulePkg + ".fw.apps");
                if (fwApps == null) {
                    fwApps = "";
                }
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "fwApps", fwApps);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.recents.QuickstepAppTransitionManagerImpl", lpparam.getClassLoader(), "hasControlRemoteAppTransitionPermission", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object isFwApps = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "isFwApps");
                if (isFwApps != null) {
                    param.returnAndSkip(false);
                }
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.recents.views.TaskView", lpparam.getClassLoader(), "getActivityOptions", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String pkgName = (String) XposedHelpers.callMethod(param.getThisObject(), "getBasePackageName");
                if (fwBlackList.contains(pkgName)) {
                    return;
                }
                View taskView = (View) param.getThisObject();
                String fwApps = Settings.Global.getString(taskView.getContext().getContentResolver(), Helpers.modulePkg + ".fw.apps");
                if (fwApps != null && fwApps.contains(pkgName)) {
                    param.returnAndSkip(XposedHelpers.callMethod(param.getThisObject(), "getActivityLaunchOptions", taskView));
                }
            }
        });
    }

    public static void CloseDrawerOnLaunchHook(PackageLoadedParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mLauncher"), "hideAppView");
            }
        };
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.AppsListFragment", lpparam.getClassLoader(), "onClick", View.class, hook);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.RecommendCategoryAppListFragment", lpparam.getClassLoader(), "onClick", View.class, hook);
    }

    public static void AssistGestureActionHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shared.recents.system.AssistManager", lpparam.getClassLoader(), "isSupportGoogleAssist", int.class, HookerClassHelper.returnConstant(true));
        final Class<?> FsGestureHelper = findClassIfExists("com.miui.home.recents.FsGestureAssistHelper", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod(FsGestureHelper, "canTriggerAssistantAction", float.class, float.class, int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean isDisabled = (boolean) XposedHelpers.callStaticMethod(FsGestureHelper, "isAssistantGestureDisabled", param.getArgs()[2]);
                if (!isDisabled) {
                    int mAssistantWidth = XposedHelpers.getIntField(param.getThisObject(), "mAssistantWidth");
                    float f = (float) param.getArgs()[0];
                    float f2 = (float) param.getArgs()[1];
                    if (f < mAssistantWidth || f > f2 - mAssistantWidth) {
                        param.returnAndSkip(true);
                        return;
                    }
                }
                param.returnAndSkip(false);
            }
        });

        final int[] inDirection = {0};

        ModuleHelper.hookAllMethods(FsGestureHelper, "handleTouchEvent", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                MotionEvent motionEvent = (MotionEvent) param.getArgs()[0];
                if (motionEvent.getAction() == 0) {
                    float mDownX = XposedHelpers.getFloatField(param.getThisObject(), "mDownX");
                    int mAssistantWidth = XposedHelpers.getIntField(param.getThisObject(), "mAssistantWidth");
                    inDirection[0] = mDownX < mAssistantWidth ? 0 : 1;
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.recents.SystemUiProxyWrapper", lpparam.getClassLoader(), "startAssistant", Bundle.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Bundle bundle = (Bundle) param.getArgs()[0];
                bundle.putInt("inDirection", inDirection[0]);
            }
        });
    }

    public static void SwipeAndStopActionHook(PackageLoadedParam lpparam) {
        if (MainModule.mPrefs.getBoolean("controls_fsg_swipeandstop_disablevibrate")) {
            Class<?> VibratorCls = findClassIfExists("android.os.Vibrator", lpparam.getClassLoader());
            ModuleHelper.hookAllMethods("com.miui.home.recents.GestureBackArrowView", lpparam.getClassLoader(), "setReadyFinish", new MethodHook() {
                private HookerClassHelper.CustomMethodUnhooker vibratorHook = null;
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    vibratorHook = ModuleHelper.findAndHookMethod(VibratorCls, "vibrate", long.class, HookerClassHelper.DO_NOTHING);
                }
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    if (vibratorHook != null) {
                        vibratorHook.unhook();
                    }
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "disableQuickSwitch", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = false;
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "isDisableQuickSwitch", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "getNextTask", Context.class, boolean.class, int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean switchApp = (boolean) param.getArgs()[1];
                if (switchApp) {
                    Context mContext = (Context) param.getArgs()[0];
                    Bundle bundle = new Bundle();
                    bundle.putInt("inDirection", (int)param.getArgs()[2]);
                    if (GlobalActions.handleAction(mContext, "controls_fsg_swipeandstop", false, bundle)) {
                        Class<?> Task = findClassIfExists("com.android.systemui.shared.recents.model.Task", lpparam.getClassLoader());
                        param.returnAndSkip(XposedHelpers.newInstance(Task));
                        return;
                    }
                }
                param.returnAndSkip(null);
            }
        });
    }

    public static void DisableUnlockWallpaperScale(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.miwallpaper.manager.WallpaperServiceController", lpparam.getClassLoader(), "noNeedDesktopWallpaperScaleAnim",
            HookerClassHelper.returnConstant(true)
        );
    }

    public static void DisableLauncherWallpaperScale(PackageLoadedParam lpparam) {
        Class<?> WallpaperZoomManagerKtClass = findClassIfExists("com.miui.home.launcher.wallpaper.WallpaperZoomManagerKt", lpparam.getClassLoader());
        if (MainModule.mPrefs.getBoolean("launcher_disable_wallpaperscale")) {
            XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", false);
            ModuleHelper.findAndHookMethod("com.miui.home.recents.DimLayer", lpparam.getClassLoader(), "isSupportDim", HookerClassHelper.returnConstant(false));
            return;
        }
        ModuleHelper.hookAllMethods("com.miui.home.recents.OverviewState", lpparam.getClassLoader(), "onStateEnabled", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (WallpaperZoomManagerKtClass != null) {
                    XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", false);
                }
            }
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (WallpaperZoomManagerKtClass != null) {
                    XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", true);
                }
            }
        });
    }

    public static void HideStatusBarInRecentsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.common.DeviceLevelUtils", lpparam.getClassLoader(), "isHideStatusBarWhenEnterRecents", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "keepStatusBarShowingForBetterPerformance", HookerClassHelper.returnConstant(false));
    }

    public static void DisableLauncherLogHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.AnalyticalDataCollectorJobService", lpparam.getClassLoader(), "onStartJob", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.AnalyticalDataCollector", lpparam.getClassLoader(), "canTrackLaunchAppEvent", HookerClassHelper.returnConstant(false));
        Class <?> OneTrackInterfaceUtils = findClassIfExists("com.miui.home.launcher.common.OneTrackInterfaceUtils", lpparam.getClassLoader());
        if (OneTrackInterfaceUtils != null) {
            XposedHelpers.setStaticObjectField(OneTrackInterfaceUtils, "IS_ENABLE", false);
        }
    }

    public static void LauncherPinchHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "onPinching", float.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                float dampingScale = (float)XposedHelpers.callMethod(param.getThisObject(), "getDampingScale", param.getArgs()[0]);
                float screenScaleRatio = (float)XposedHelpers.callMethod(param.getThisObject(), "getScreenScaleRatio");
                if (dampingScale < screenScaleRatio)
                    if (MainModule.mPrefs.getInt("launcher_pinch_action", 1) > 1) param.returnAndSkip(false);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "onPinchingEnd", float.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                float dampingScale = (float)XposedHelpers.callMethod(param.getThisObject(), "getDampingScale", param.getArgs()[0]);
                float screenScaleRatio = (float)XposedHelpers.callMethod(param.getThisObject(), "getScreenScaleRatio");
                if (dampingScale < screenScaleRatio)
                    if (GlobalActions.handleAction(((View)param.getThisObject()).getContext(), "launcher_pinch")) {
                        XposedHelpers.callMethod(param.getThisObject(), "finishCurrentGesture");

                        Class<?> pinchingStateEnum = XposedHelpers.findClass("com.miui.home.launcher.Workspace$PinchingState", lpparam.getClassLoader());
                        Object stateFollow = XposedHelpers.getStaticObjectField(pinchingStateEnum, "FOLLOW");
                        Object stateReadyToEdit = XposedHelpers.getStaticObjectField(pinchingStateEnum, "READY_TO_EDIT");

                        Object mState = XposedHelpers.getObjectField(param.getThisObject(), "mState");
                        XposedHelpers.setObjectField(param.getThisObject(), "mState", stateFollow);
                        if (mState == stateReadyToEdit)
                            XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mLauncher"), "changeEditingEntryViewToHotseats");
                        XposedHelpers.callMethod(param.getThisObject(), "resetCellScreenScale", param.getArgs()[0]);

                        param.returnAndSkip(null);
                    }
            }
        });
    }
    public static void ResizableWidgetsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("android.appwidget.AppWidgetHostView", lpparam.getClassLoader(), "getAppWidgetInfo", new MethodHook() {
            @Override
            protected void after(AfterHookCallback param) throws Throwable {
                AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo) param.getResult();
                if (widgetInfo == null) return;
                widgetInfo.resizeMode = AppWidgetProviderInfo.RESIZE_VERTICAL | AppWidgetProviderInfo.RESIZE_HORIZONTAL;
                widgetInfo.minHeight = 0;
                widgetInfo.minWidth = 0;
                widgetInfo.minResizeHeight = 0;
                widgetInfo.minResizeWidth = 0;
                param.setResult(widgetInfo);
            }
        });
    }
}