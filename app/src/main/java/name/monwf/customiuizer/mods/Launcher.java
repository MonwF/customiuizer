package name.monwf.customiuizer.mods;

import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;
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
import android.net.Uri;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import name.monwf.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import io.github.libxposed.api.XposedInterface;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import miui.security.SecurityManager;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.mods.utils.HookerClassHelper;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.ShakeManager;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;

public class Launcher {

    private static GestureDetector mDetectorHorizontal;

    public static void HomescreenSwipesHook(final PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "onVerticalGesture", int.class, MotionEvent.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

                                    if ((boolean)XposedHelpers.callMethod(thisObject, "isInNormalEditingMode")) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                String key = null;
            		                Context helperContext = ((ViewGroup)thisObject).getContext();
            		                int numOfFingers = 1;
            		                if (args[1] != null) numOfFingers = ((MotionEvent)args[1]).getPointerCount();
            		                if ((int)args[0] == 11) {
            		                    if (numOfFingers == 1)
            		                        key = "launcher_swipedown";
            		                    else if (numOfFingers == 2)
            		                        key = "launcher_swipedown2";
            		                    if (GlobalActions.handleAction(helperContext, key)) { skipped = true; result = true; throwable = null; }
            		                } else if ((int)args[0] == 10) {
            		                    if (numOfFingers == 1)
            		                        key = "launcher_swipeup";
            		                    else if (numOfFingers == 2)
            		                        key = "launcher_swipeup2";
            		                    if (GlobalActions.handleAction(helperContext, key)) { skipped = true; result = true; throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.uioverrides.StatusBarSwipeController", lpparam.getClassLoader(), "canInterceptTouch", MotionEvent.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) { skipped = true; result = false; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.uioverrides.AllAppsSwipeController", lpparam.getClassLoader(), "canInterceptTouch", MotionEvent.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        // content_center, global_search, notification_bar
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.getClassLoader(), "getPullDownGesture", Context.class, new MethodHook() {
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

            		                if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) { result = "no_action"; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        // content_center, global_search
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.getClassLoader(), "getSlideUpGesture", Context.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = "no_action"; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        if (ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "isGlobalSearchEnable", Context.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        })) {
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.getClassLoader(), "isTopSearchEnable", new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	boolean skipped = false;
                	Object result = null;
                	Throwable throwable = null;
                	try {

                		                    if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) { skipped = true; result = false; throwable = null; }

                		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                        result = chain.proceed();
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.getClassLoader(), "isBottomGlobalSearchEnable", new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	boolean skipped = false;
                	Object result = null;
                	Throwable throwable = null;
                	try {

                		                    if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null; }

                		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                        result = chain.proceed();
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "isGlobalSearchBottomEffectEnable", Context.class, new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	boolean skipped = false;
                	Object result = null;
                	Throwable throwable = null;
                	try {

                		                    if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null; }

                		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                        result = chain.proceed();
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        } else if (!ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "allowedSlidingUpToStartGolbalSearch", Context.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        })) if (lpparam.getPackageName().equals("com.miui.home")) XposedHelpers.log("HomescreenSwipesHook", "Cannot disable swipe up search");
    }

    public static void HotSeatSwipesHook(final PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.overlay.assistant.AssistantOverlaySwipeController", lpparam.getClassLoader(), "canInterceptTouch", MotionEvent.class, new MethodHook() {
            private Rect mHotHeatTouchRect = null;
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
            		Object thisObject = chain.getThisObject();
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                boolean canInterceptTouch = (boolean) result;
            		                if (canInterceptTouch) {
            		                    if (mHotHeatTouchRect == null) {
            		                        Object mLauncher = XposedHelpers.getObjectField(thisObject, "mLauncher");
            		                        FrameLayout mHotSeats = (FrameLayout) XposedHelpers.callMethod(mLauncher, "getHotSeats");
            		                        mHotHeatTouchRect = new Rect();
            		                        mHotSeats.getHitRect(mHotHeatTouchRect);
            		                    }
            		                    MotionEvent motionEvent = (MotionEvent) args[0];
            		                    if (mHotHeatTouchRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
            		                        { result = false; throwable = null; }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.hotseats.HotSeats", lpparam.getClassLoader(), "dispatchTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                MotionEvent ev = (MotionEvent)args[0];
                                    if (ev == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }

            		                ViewGroup hotSeat = (ViewGroup)thisObject;
            		                Context helperContext = hotSeat.getContext();
                                    if (helperContext == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                if (mDetectorHorizontal == null) mDetectorHorizontal = new GestureDetector(helperContext, new SwipeListenerHorizontal(hotSeat));
            		                mDetectorHorizontal.onTouchEvent(ev);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void ShakeHook(final PackageReadyParam lpparam) {
        final String shakeMgrKey = "MIUIZER_SHAKE_MGR";

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onResume", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                ShakeManager shakeMgr = (ShakeManager)XposedHelpers.getAdditionalInstanceField(thisObject, shakeMgrKey);
            		                if (shakeMgr == null) {
            		                    shakeMgr = new ShakeManager((Context)thisObject);
            		                    XposedHelpers.setAdditionalInstanceField(thisObject, shakeMgrKey, shakeMgr);
            		                }
            		                Activity launcherActivity = (Activity)thisObject;
            		                SensorManager sensorMgr = (SensorManager)launcherActivity.getSystemService(Context.SENSOR_SERVICE);
            		                shakeMgr.reset();
            		                sensorMgr.registerListener(shakeMgr, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onPause", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                if (XposedHelpers.getAdditionalInstanceField(thisObject, shakeMgrKey) == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Activity launcherActivity = (Activity)thisObject;
            		                SensorManager sensorMgr = (SensorManager)launcherActivity.getSystemService(Context.SENSOR_SERVICE);
            		                sensorMgr.unregisterListener((ShakeManager)XposedHelpers.getAdditionalInstanceField(thisObject, shakeMgrKey));

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void NoClockHideHook(final PackageReadyParam lpparam) {
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

    public static void RenameShortcutsHook(final PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
            		                    @Override
            		                    public void onChange(String key) {
            		                        try {
            		                            if (!key.contains("pref_key_launcher_renameapps_list")) return;
            		                            CharSequence newTitle = MainModule.mPrefs.getString(key, "");
            		                            HashSet<?> mAllLoadedApps = null;
            		                            if (XposedHelpers.findFieldIfExists(thisObject.getClass(), "mAllLoadedShortcut") != null)
            		                                mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(thisObject, "mAllLoadedShortcut");
            		                            else if (XposedHelpers.findFieldIfExists(thisObject.getClass(), "mAllLoadedApps") != null)
            		                                mAllLoadedApps = (HashSet<?>)XposedHelpers.getObjectField(thisObject, "mAllLoadedApps");
            		                            Activity act = (Activity)thisObject;
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
            		                                                if (buddyIconView != null) XposedHelpers.callMethod(buddyIconView, "updateInfo", thisObject, shortcut);
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

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllConstructors("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), new MethodHook() {
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
            		Object thisObject = chain.getThisObject();
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mLabelOrig", XposedHelpers.getObjectField(thisObject, "mLabel"));
            		                if (args != null && args.length > 0) modifyTitle(thisObject);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), "loadToggleInfo", Context.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mLabelOrig", XposedHelpers.getObjectField(thisObject, "mLabel"));
            		                modifyTitle(thisObject);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), "setLabelAndUpdateDB", CharSequence.class, Context.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mLabelOrig", args[0]);
            		                modifyTitle(thisObject);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ShortcutInfo", lpparam.getClassLoader(), "load", Context.class, Cursor.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                modifyTitle(thisObject);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethodsSilently("com.miui.home.launcher.BaseAppInfo", lpparam.getClassLoader(), "resetTitle", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                modifyTitle(thisObject);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void CloseFolderOnLaunchHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "launch", "com.miui.home.launcher.ShortcutInfo", View.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) != 2) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                boolean mHasLaunchedAppFromFolder = XposedHelpers.getBooleanField(thisObject, "mHasLaunchedAppFromFolder");
            		                if (mHasLaunchedAppFromFolder) XposedHelpers.callMethod(thisObject, "closeFolder");

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void FSGesturesHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "usingFsGesture", HookerClassHelper.returnConstant(true));

        ModuleHelper.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader(), "createAndAddNavStubView", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader()), "REAL_FORCE_FSG_NAV_BAR");
            		                if (!fsg) { skipped = true; result = null; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader(), "updateFsgWindowState", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                boolean fsg = (boolean)XposedHelpers.getAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader()), "REAL_FORCE_FSG_NAV_BAR");
            		                if (fsg) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                Object mNavStubView = XposedHelpers.getObjectField(thisObject, "mNavStubView");
            		                Object mWindowManager = XposedHelpers.getObjectField(thisObject, "mWindowManager");
            		                if (mWindowManager != null && mNavStubView != null) {
            		                    XposedHelpers.callMethod(mWindowManager, "removeView", mNavStubView);
            		                    XposedHelpers.setObjectField(thisObject, "mNavStubView", null);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.getClassLoader(), "getGlobalBoolean", ContentResolver.class, String.class, new MethodHook() {
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                if (!"force_fsg_nav_bar".equals(args[1])) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                for (StackTraceElement el: Thread.currentThread().getStackTrace()) {
            		                    if ("com.miui.home.recents.BaseRecentsImpl".equals(el.getClassName())) {
            		                        XposedHelpers.setAdditionalStaticField(XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.getClassLoader()), "REAL_FORCE_FSG_NAV_BAR", result);
            		                        { result = true; throwable = null; }
            		                        { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "onTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                MotionEvent event = (MotionEvent)args[0];
                                    if (event.getAction() != MotionEvent.ACTION_DOWN) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
            		                if (foregroundInfo != null) {
            		                    String pkgName = foregroundInfo.mForegroundPackageName;
            		                    if (MainModule.mPrefs.getStringSet("controls_fsg_horiz_apps").contains(pkgName)) { skipped = true; result = false; throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void LauncherDoubleTapHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), new MethodHook() {
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
            		Object thisObject = chain.getThisObject();
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                if (args.length != 3) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Object mDoubleTapControllerEx = XposedHelpers.getAdditionalInstanceField(thisObject, "mDoubleTapControllerEx");
            		                if (mDoubleTapControllerEx != null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                mDoubleTapControllerEx = new DoubleTapController((Context)args[0], "launcher_doubletap");
            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mDoubleTapControllerEx", mDoubleTapControllerEx);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "dispatchTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                DoubleTapController mDoubleTapControllerEx = (DoubleTapController)XposedHelpers.getAdditionalInstanceField(thisObject, "mDoubleTapControllerEx");
                                    if (mDoubleTapControllerEx == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
                                    if (!mDoubleTapControllerEx.isDoubleTapEvent((MotionEvent)args[0])) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                int mCurrentScreenIndex = XposedHelpers.getIntField(thisObject, lpparam.getPackageName().equals("com.miui.home") ? "mCurrentScreenIndex" : "mCurrentScreen");
            		                Object cellLayout = XposedHelpers.callMethod(thisObject, "getCellLayout", mCurrentScreenIndex);
                                    if ((boolean)XposedHelpers.callMethod(cellLayout, "lastDownOnOccupiedCell")) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
                                    if ((boolean)XposedHelpers.callMethod(thisObject, "isInNormalEditingMode")) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                mDoubleTapControllerEx.onDoubleTapEvent();


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void TitleShadowHook(PackageReadyParam lpparam) {
        if (lpparam.getPackageName().equals("com.miui.home"))
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.getClassLoader(), "getIconTitleShadowColor", new MethodHook() {
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

                		                    int color = (int)result;
                		                    if (color == Color.TRANSPARENT) { return XposedHelpers.throwOrReturn(throwable, result); }
                		                    { result = Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color)); throwable = null; }

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            }); else
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.getClassLoader(), "getTitleShadowColor", int.class, new MethodHook() {
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

                		                    int color = (int)result;
                		                    if (color == Color.TRANSPARENT) { return XposedHelpers.throwOrReturn(throwable, result); }
                		                    { result = Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color)); throwable = null; }

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
    }

    public static void HideNavBarHook(PackageReadyParam lpparam) {
        final boolean[] showNavBar = {true};
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.getClassLoader(), "onSystemUiFlagsChanged", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                int flags = (int) args[0];
            		                boolean newState = (flags & 2) == 0;
            		                if (newState != showNavBar[0]) {
            		                    showNavBar[0] = newState;
            		                }
            		                args[0] = flags & -3;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.recents.views.RecentsContainer", lpparam.getClassLoader(), "showLandscapeOverviewGestureView", boolean.class, HookerClassHelper.DO_NOTHING);
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.getClassLoader(), "isImmersive", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                { skipped = true; result = !showNavBar[0]; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.getClassLoader(), "onPointerEvent", MotionEvent.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                boolean mIsInFsMode = XposedHelpers.getBooleanField(thisObject, "mIsInFsMode");
            		                if (!mIsInFsMode) {
            		                    MotionEvent motionEvent = (MotionEvent) args[0];
            		                    if (motionEvent.getAction() == 0) {
            		                        XposedHelpers.setObjectField(thisObject, "mHideGestureLine", true);
            		                    }
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.getClassLoader(), "updateScreenSize", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                XposedHelpers.setObjectField(thisObject, "mHideGestureLine", false);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideSeekPointsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.pageindicators.AllAppsIndicator", lpparam.getClassLoader(), "shouldHide", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.pageindicators.AllAppsIndicator", lpparam.getClassLoader(), "hideAllAppsArrow", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                Object mLauncher = XposedHelpers.getObjectField(thisObject, "mLauncher");
            		                if (mLauncher == null) { return XposedHelpers.throwOrReturn(throwable, result); }
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
            		                View mScreenSeekBar = (View)XposedHelpers.getObjectField(thisObject, "mScreenIndicator");
            		                mScreenSeekBar.animate().cancel();
            		                if (!isInEditingMode && MainModule.mPrefs.getBoolean("launcher_hideseekpoints_edit")) {
            		                    mScreenSeekBar.setAlpha(0.0f);
            		                    mScreenSeekBar.setVisibility(View.GONE);
            		                    { return XposedHelpers.throwOrReturn(throwable, result); }
            		                }
            		                mScreenSeekBar.setVisibility(View.VISIBLE);
            		                mScreenSeekBar.animate().alpha(1.0f).setDuration(300);
            		                if (!isInEditingMode) {
            		                    Message msg = Message.obtain(mHandler, 666);
            		                    msg.obj = mScreenSeekBar;
            		                    mHandler.sendMessageDelayed(msg, 600);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void InfiniteScrollHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.getClassLoader(), "getSnapToScreenIndex", int.class, int.class, int.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                if (args[0] != result) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int screenCount = (int)XposedHelpers.callMethod(thisObject, "getScreenCount");
            		                if ((int)args[2] == -1 && (int)args[0] == 0)
            		                    { result = screenCount; throwable = null; }
            		                else if ((int)args[2] == 1 && (int)args[0] == screenCount - 1)
            		                    { result = 0; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.getClassLoader(), "getSnapUnitIndex", int.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                int mCurrentScreenIndex = XposedHelpers.getIntField(thisObject, lpparam.getPackageName().equals("com.miui.home") ? "mCurrentScreenIndex" : "mCurrentScreen");
            		                if (mCurrentScreenIndex != (int)result) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int screenCount = (int)XposedHelpers.callMethod(thisObject, "getScreenCount");
            		                if ((int)result == 0)
            		                    { result = screenCount; throwable = null; }
            		                else if ((int)result == screenCount - 1)
            		                    { result = 0; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void UnlockGridsRes() {
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_x", 3);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_y", 4);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_x_min", 3);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_y_min", 4);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_x_max", 8);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_y_max", 10);
    }

    public static void UnlockGridsHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethodsSilently("com.miui.home.launcher.compat.LauncherCellCountCompatDevice", lpparam.getClassLoader(), "shouldUseDeviceValue", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.compat.LauncherCellCountCompatDeviceFold", lpparam.getClassLoader(), "shouldUseDeviceValue", Context.class, int.class, HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.miui.home.settings.MiuiHomeSettings", lpparam.getClassLoader(), "onCreatePreferences", Bundle.class, String.class, new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mScreenCellsConfig"), "setVisible", true);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        Class <?> DeviceConfigClass = XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod(DeviceConfigClass, "loadCellsCountConfig", Context.class, boolean.class, new MethodHook() {
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

            		                int sCellCountY = (int) XposedHelpers.getStaticObjectField(DeviceConfigClass, "sCellCountY");
            		                if (sCellCountY > 6) {
            		                    int cellHeight = (int) XposedHelpers.callStaticMethod(DeviceConfigClass, "getCellHeight");
            		                    XposedHelpers.setStaticObjectField(DeviceConfigClass, "sFolderCellHeight", cellHeight);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenUtils", lpparam.getClassLoader(), "getScreenCellsSizeOptions", Context.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

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
            		                { skipped = true; result = arrayList; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.compat.LauncherCellCountCompatNoWord", lpparam.getClassLoader(), "setLoadResCellConfig", boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                args[0] = true;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "isCellSizeChangedByTheme", new MethodHook() {
            HookerClassHelper.CustomMethodUnhooker nowordHook;
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	__beforeBody__: {
                        		try {


	            		                nowordHook = ModuleHelper.findAndHookMethod("com.miui.home.launcher.common.Utilities", lpparam.getClassLoader(), "isNoWordModel", HookerClassHelper.returnConstant(false));


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}

                        	try {
                                result = chain.proceed();
                        	} catch (Throwable t) {
                        		throwable = t;
                        		result = null;
                        	}
                        	__afterBody__: {
                        		try {

	            		                if (nowordHook != null) nowordHook.unhook();
	            		                nowordHook = null;


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
    }

    public static void FolderColumnsRes(int folderCols) {
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_folder_columns_count", folderCols);
    }


    private static void setFolderWidth(Object thisObject) {
        if (MainModule.mPrefs.getBoolean("launcher_folderwidth")) {
            GridView mContent = (GridView)XposedHelpers.getObjectField(thisObject, "mContent");
            ViewGroup.LayoutParams lp = mContent.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mContent.setLayoutParams(lp);
        }
    }

    public static void FolderColumnsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                setFolderWidth(thisObject);
            		                int cols = MainModule.mPrefs.getInt("launcher_folder_cols", 1);
            		                if (cols > 3 && MainModule.mPrefs.getBoolean("launcher_folderspace")) {
            		                    ViewGroup mBackgroundView = (ViewGroup)XposedHelpers.getObjectField(thisObject, "mBackgroundView");
            		                    if (mBackgroundView != null)
            		                        mBackgroundView.setPadding(
            		                            mBackgroundView.getPaddingLeft() / 3,
            		                            mBackgroundView.getPaddingTop(),
            		                            mBackgroundView.getPaddingRight() / 3,
            		                            mBackgroundView.getPaddingBottom()
            		                        );
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.getClassLoader(), "resetViewsLayoutParams", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                setFolderWidth(thisObject);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.Folder", lpparam.getClassLoader(), "onLayout", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                if (!MainModule.mPrefs.getBoolean("launcher_folderwidth")) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                GridView mContent = (GridView)XposedHelpers.getObjectField(thisObject, "mContent");
            		                ImageView mFakeIcon = (ImageView)XposedHelpers.getObjectField(thisObject, "mFakeIcon");
            		                mFakeIcon.layout(mContent.getLeft(), mContent.getTop(), mContent.getRight(), mContent.getTop() + mContent.getWidth());

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void IconScaleHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ShortcutIcon", lpparam.getClassLoader(), "restoreToInitState", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(thisObject, "mIconContainer");
            		                if (mIconContainer == null || mIconContainer.getChildAt(0) == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
            		                mIconContainer.getChildAt(0).setScaleX(multx);
            		                mIconContainer.getChildAt(0).setScaleY(multx);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);

            		                ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(thisObject, "mIconContainer");
            		                if (mIconContainer != null && mIconContainer.getChildAt(0) != null) {
            		                    mIconContainer.getChildAt(0).setScaleX(multx);
            		                    mIconContainer.getChildAt(0).setScaleY(multx);
            		                    mIconContainer.setClipToPadding(false);
            		                    mIconContainer.setClipChildren(false);
            		                }

            		                if (multx > 1) {
            		                    final TextView mMessage = (TextView)XposedHelpers.getObjectField(thisObject, "mMessage");
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

            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mMessageAnimationOrig", XposedHelpers.getObjectField(thisObject, "mMessageAnimation"));
            		                XposedHelpers.setObjectField(thisObject, "mMessageAnimation", new Runnable() {
            		                    @Override
            		                    public void run() {
            		                        try {
            		                            Runnable mMessageAnimationOrig = (Runnable)XposedHelpers.getAdditionalInstanceField(thisObject, "mMessageAnimationOrig");
            		                            mMessageAnimationOrig.run();
            		                            boolean mIsShowMessageAnimation = XposedHelpers.getBooleanField(thisObject, "mIsShowMessageAnimation");
            		                            if (mIsShowMessageAnimation) {
            		                                View mMessage = (View)XposedHelpers.getObjectField(thisObject, "mMessage");
            		                                mMessage.animate().cancel();
            		                                mMessage.animate().scaleX(multx).scaleY(multx).setStartDelay(0).start();
            		                            }
            		                        } catch (Throwable t) {
            		                            XposedHelpers.log(t);
            		                        }
            		                    }
            		                });

            		//				if (mult <= 1) return;
            		//				TextView mMessage = (TextView)XposedHelpers.getObjectField(thisObject, "mMessage");
            		//				if (mMessage != null) {
            		//					int width = mMessage.getResources().getDimensionPixelSize(mMessage.getResources().getIdentifier("icon_message_max_width", "dimen", lpparam.getPackageName()));
            		//					mMessage.setTranslationX(-width/2f * (1f - 1f / mult));
            		//					mMessage.setTranslationY(width/2f * (1f - 1f / mult));
            		//				}

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "getIconLocation", new MethodHook() {
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

            		                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
            		                Rect rect = (Rect)result;
            		                if (rect == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                rect.right = rect.left + Math.round(rect.width() * multx);
            		                rect.bottom = rect.top + Math.round(rect.height() * multx);
            		                { result = rect; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.gadget.ClearButton", lpparam.getClassLoader(), "onCreate", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                ViewGroup mIconContainer = (ViewGroup)XposedHelpers.getObjectField(thisObject, "mIconContainer");
            		                if (mIconContainer == null || mIconContainer.getChildAt(0) == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                float multx = (float)Math.sqrt(MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f);
            		                mIconContainer.getChildAt(0).setScaleX(multx);
            		                mIconContainer.getChildAt(0).setScaleY(multx);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void TitleFontSizeHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                TextView mTitle = (TextView)XposedHelpers.getObjectField(thisObject, "mTitle");
            		                if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.getClassLoader(), "fromXml", new MethodHook() {
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                Object buddyIcon = XposedHelpers.callMethod(args[3], "getBuddyIconView", args[2]);
            		                if (buddyIcon == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                TextView mTitle = (TextView)XposedHelpers.getObjectField(buddyIcon, "mTitle");
            		                if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.getClassLoader(), "createShortcutIcon", new MethodHook() {
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

            		                Object buddyIcon = result;
            		                if (buddyIcon == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                TextView mTitle = (TextView)XposedHelpers.getObjectField(buddyIcon, "mTitle");
            		                if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.common.Utilities", lpparam.getClassLoader(), "adaptTitleStyleToWallpaper", new MethodHook() {
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                TextView mTitle = (TextView)args[1];
            		                if (mTitle != null && mTitle.getId() == mTitle.getResources().getIdentifier("icon_title", "id", "com.miui.home"))
            		                    mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5));

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void TitleTopMarginHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                ViewGroup mTitleContainer = (ViewGroup)XposedHelpers.getObjectField(thisObject, "mTitleContainer");
            		                if (mTitleContainer == null) { return XposedHelpers.throwOrReturn(throwable, result); }
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

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void PrivacyFolderHook(PackageReadyParam lpparam) {
        if (MainModule.mPrefs.getBoolean("launcher_privacyapps_gest")) {
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "registerBroadcastReceivers", new MethodHook() {
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
                		Object thisObject = chain.getThisObject();

                		                    final Activity act = (Activity)thisObject;
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
                		                                    XposedHelpers.setAdditionalInstanceField(thisObject, "fromSecretCode", true);
                		                                    XposedHelpers.callMethod(thisObject, "startSecurityHide");
                		                                }
                		                            } catch (Throwable t) {
                		                                XposedHelpers.log(t);
                		                            }
                		                        }
                		                    }, intentFilter, Context.RECEIVER_NOT_EXPORTED);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "startSecurityHide", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                if (XposedHelpers.getAdditionalInstanceField(thisObject, "fromSecretCode") != null) {
            		                    XposedHelpers.removeAdditionalInstanceField(thisObject, "fromSecretCode");
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                }
            		                if (GlobalActions.handleAction((Activity)thisObject, "launcher_spread")) {
            		                    { skipped = true; result = null; throwable = null; }
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                }
            		                boolean opt = MainModule.mPrefs.getBoolean("launcher_privacyapps_gest");
            		                if (opt) { skipped = true; result = null; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideTitlesHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                View mTitleContainer = (View)XposedHelpers.getObjectField(thisObject, "mTitleContainer");
            		                if (mTitleContainer != null) mTitleContainer.setVisibility(View.GONE);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HorizontalSpacingRes() {
        int opt = MainModule.mPrefs.getInt("launcher_horizmargin", 0) - 21;
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "workspace_cell_padding_side", opt);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "workspace_cell_padding_side_no_word", opt);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "workspace_cell_padding_side_rotatable", opt);
    }

    public static void IndicatorHeightRes() {
        int opt = MainModule.mPrefs.getInt("launcher_indicatorheight", 9);
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "slide_bar_height", opt);
    }

    public static void ShowHotseatTitlesHook(PackageReadyParam lpparam) {
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "bool", "config_hide_hotseats_app_title", false);
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "createItemIcon", ViewGroup.class, "com.miui.home.launcher.ItemInfo", boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                args[2] = false;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void FolderBlurHook(PackageReadyParam lpparam) {
        Class<?> BlurUtils = findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.getClassLoader());
        if (BlurUtils != null) {
            ModuleHelper.hookAllMethods(BlurUtils, "getLauncherBlur", new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	boolean skipped = false;
                	Object result = null;
                	Throwable throwable = null;
                	Object[] args = XposedHelpers.getArgsArray(chain);
                	try {

                		                    boolean isFolderShowing = (boolean) XposedHelpers.callMethod(args[0], "isFolderShowing");
                		                    if (isFolderShowing) {
                		                        int blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0);
                		                        float blurRatio = blurPct / 100f;
                		                        { skipped = true; result = blurRatio; throwable = null; }
                		                    }

                		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                		result = chain.proceed(args);
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.getClassLoader(), "open", new MethodHook() {
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
                		Object thisObject = chain.getThisObject();

                		                    Activity launcher = (Activity) XposedHelpers.getObjectField(thisObject, "mLauncher");

                		                    int blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0);
                		                    float blurRatio = blurPct / 100f;
                		                    XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", blurRatio, launcher.getWindow(), true);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.getClassLoader(), "close", boolean.class, new MethodHook() {
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
                		Object thisObject = chain.getThisObject();
                		Object[] args = XposedHelpers.getArgsArray(chain);

                		                    Activity launcher = (Activity) XposedHelpers.getObjectField(thisObject, "mLauncher");
                		                    XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", 0f, launcher.getWindow(), args[0]);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "cancelShortcutMenu", int.class, "com.miui.home.launcher.shortcuts.CancelShortcutMenuReason", new MethodHook() {
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
                		Object thisObject = chain.getThisObject();

                		                    boolean isFolderShowing = (boolean) XposedHelpers.callMethod(thisObject, "isFolderShowing");
                		                    if (isFolderShowing) {
                		                        int blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0);
                		                        float blurRatio = blurPct / 100f;
                		                        Activity launcher = (Activity) thisObject;
                		                        XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", blurRatio, launcher.getWindow(), true);
                		                    }

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
    }

    private static float scaleStiffness(float val, float scale) {
        return (scale < 1.0f ? 2f / scale : 1.0f / scale) * val;
    }

    public static void FixAnimHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.animate.SpringAnimator", lpparam.getClassLoader(), "getSpringForce", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                float scale = Helpers.getAnimationScale(2);
                                    if (scale == 1.0f) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                if (scale == 0) scale = 0.01f;
            		                args[2] = scaleStiffness((float)args[2], scale);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        MethodHook hook = new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                float scale = Helpers.getAnimationScale(2);
                                    if (scale == 1.0f) { return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                if (scale == 0) scale = 0.01f;
            		                XposedHelpers.setFloatField(thisObject, "mCenterXStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mCenterXStiffness"), scale));
            		                XposedHelpers.setFloatField(thisObject, "mCenterYStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mCenterYStiffness"), scale));
            		                XposedHelpers.setFloatField(thisObject, "mWidthStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mWidthStiffness"), scale));
            		                XposedHelpers.setFloatField(thisObject, "mRadiusStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mRadiusStiffness"), scale));
            		                XposedHelpers.setFloatField(thisObject, "mAlphaStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mAlphaStiffness"), scale));
            		                try {
            		                    XposedHelpers.setFloatField(thisObject, "mRatioStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mRatioStiffness"), scale));
            		                } catch (Throwable t) {
            		                    XposedHelpers.setFloatField(thisObject, "mRadioStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mRadioStiffness"), scale));
            		                }


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void DockMarginTopHook(PackageReadyParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_dock_topmargin", 0);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "calcHotSeatsMarginTop", Context.class, boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                { skipped = true; result = Math.round(Helpers.dp2px(opt)); throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
    public static void DockMarginBottomHook(PackageReadyParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_dock_bottommargin", 0);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "calcHotSeatsMarginBottom", Context.class, boolean.class, boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                { skipped = true; result = Math.round(Helpers.dp2px(opt)); throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
    public static void DockHeightHook(PackageReadyParam lpparam) {
        int dockHeight = MainModule.mPrefs.getInt("launcher_dock_height", 60);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "calcHotSeatsHeight", Context.class, boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                { skipped = true; result = Math.round(Helpers.dp2px(dockHeight)); throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
    public static void WorkspaceCellPaddingTopHook(PackageReadyParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_topmargin", 0) - 21;
        MethodHook hook = new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                { skipped = true; result = Math.round(Helpers.dp2px(opt)); throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };

        boolean newLauncher = ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "getWorkspaceCellPaddingTop", Context.class, hook);
        if (!newLauncher) {
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "getWorkspaceCellPaddingTop", hook);
        }
    }

    public static void IndicatorMarginTopHook(PackageReadyParam lpparam) {
        int opt = MainModule.mPrefs.getInt("launcher_indicator_topmargin", 0) - 21;
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "slide_bar_margin_top", opt);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.util.DimenUtils1X", lpparam.getClassLoader(), "getDimensionPixelSize", Context.class, String.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                String resKey = (String) args[1];
            		                if ("slide_bar_margin_top".equals(resKey)) {
            		                    { skipped = true; result = Math.round(Helpers.dp2px(opt)); throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HorizontalWidgetSpacingHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "getMiuiWidgetSizeSpec", new MethodHook() {
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                if (args.length < 4) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                long spec = (long)result;
            		                long width = spec >> 32;
            		                long height = spec - ((spec >> 32) << 32);
            		                int opt = Math.round((MainModule.mPrefs.getInt("launcher_horizwidgetmargin", 0) - 21) * Resources.getSystem().getDisplayMetrics().density) * 2;
            		                width -= opt;
            		                { result = (width << 32) | height; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.miui.home.launcher.MIUIWidgetUtil", lpparam.getClassLoader(), "getMiuiWidgetPadding", new MethodHook() {
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

            		                { result = new Rect(); throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void FixAppInfoLaunchHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.shortcuts.ShortcutMenuManager", lpparam.getClassLoader(), "startAppDetailsActivity", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                ComponentName component = (ComponentName)XposedHelpers.callMethod(args[0], "getComponentName");
                                    if (component == null) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                View view = (View)args[1];
                                    if (view == null) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                UserHandle userHandle = (UserHandle)XposedHelpers.callMethod(args[0], "getUserHandle");
            		                ModuleHelper.openAppInfo(view.getContext(), component.getPackageName(), userHandle != null ? userHandle.hashCode() : 0);
            		                { skipped = true; result = null; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void NoWidgetOnlyHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.CellLayout", lpparam.getClassLoader(), "setScreenType", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                args[0] = 0;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void NoUnlockAnimationHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.getClassLoader(), "isSystemAnimationOpen", HookerClassHelper.returnConstant(false));
    }

    public static void NoZoomAnimationHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.getClassLoader(), "startShortcutMenuLayerFadeOutAnim", HookerClassHelper.DO_NOTHING);
        ModuleHelper.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.getClassLoader(), "startShortcutMenuLayerFadeInAnim", HookerClassHelper.DO_NOTHING);
    }

    public static void UseOldLaunchAnimationHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.recents.QuickstepAppTransitionManagerImpl", lpparam.getClassLoader(), "hasControlRemoteAppTransitionPermission", HookerClassHelper.returnConstant(false));
    }

    public static void ReverseLauncherPortraitHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
            @Override
            @SuppressLint("SourceLockedOrientationActivity")
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
            		Object thisObject = chain.getThisObject();

            		                Activity act = (Activity)thisObject;
            		                act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideFromRecentsHook(PackageReadyParam lpparam) {
        Class<?> ActiviyManagerWrapper = findClassIfExists("com.android.systemui.shared.recents.system.ActivityManagerWrapper", lpparam.getClassLoader());
        Class<?> TaskInfoCompat = findClassIfExists("com.android.systemui.shared.recents.model.GroupedRecentTaskInfoCompat", lpparam.getClassLoader());
        if (TaskInfoCompat == null) {
            XposedHelpers.log("HideFromRecentsHook", "hook failed");
            return;
        }
        ModuleHelper.findAndHookMethod(ActiviyManagerWrapper, "needRemoveTask", TaskInfoCompat, new MethodHook() {
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                if (args[0] != null) {
            		                    Object mainTask = XposedHelpers.getObjectField(args[0], "mMainTaskInfo");
            		                    ComponentName componentName = (ComponentName) XposedHelpers.getObjectField(mainTask, "topActivity");
            		                    String pkgName = null;
            		                    if (componentName != null) {
            		                        pkgName = componentName.getPackageName();
            		                    }
            		                    else {
            		                        Intent baseIntent = (Intent) XposedHelpers.getObjectField(mainTask, "baseIntent");
            		                        if (baseIntent != null && baseIntent.getComponent() != null) {
            		                            pkgName = baseIntent.getComponent().getPackageName();
            		                        }
            		                    }
            		                    if (pkgName != null) {
            		                        Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_hidefromrecents_apps");
            		                        if (selectedApps.contains(pkgName)) {
            		                            { result = true; throwable = null; }
            		                        }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void MaxHotseatIconsCountHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "getHotseatMaxCount", HookerClassHelper.returnConstant(666));
    }

    public static void RecentsBlurRatioHook(PackageReadyParam lpparam) {
        Class<?> utilsClass = findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.getClassLoader());
        if (utilsClass == null) {
            XposedHelpers.log("RecentsBlurRatioHook", "Cannot find blur utility class");
            return;
        }

        ModuleHelper.hookAllMethods(utilsClass, "fastBlurWhenEnterRecents", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                boolean mIsFromFsGesture = XposedHelpers.getBooleanField(args[1], "mIsFromFsGesture");
            		                if (!mIsFromFsGesture) {
            		                    Activity launcher = (Activity) args[0];
            		                    float blurRatio = MainModule.mPrefs.getInt("system_recents_blur", 100) / 100f;
            		                    XposedHelpers.callStaticMethod(utilsClass, "fastBlur", blurRatio, launcher.getWindow(), args[2]);
            		                    { skipped = true; result = null; throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.hookAllMethods(utilsClass, "fastBlurWhenGestureResetTaskView", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                XposedHelpers.setAdditionalStaticField(utilsClass, "customBlurRatio", true);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods(utilsClass, "fastBlur", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if (args.length == 3) {
            		                    if (XposedHelpers.getAdditionalStaticField(utilsClass, "customBlurRatio") != null) {
            		                        float blurRatio = MainModule.mPrefs.getInt("system_recents_blur", 100) / 100f;
            		                        args[0] = blurRatio;
            		                        XposedHelpers.removeAdditionalStaticField(utilsClass, "customBlurRatio");
            		                    }
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void CloseFolderOrDrawerOnLaunchShortcutMenuHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.shortcuts.AppShortcutMenuItem", lpparam.getClassLoader(), "getOnClickListener", new MethodHook() {
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

            		                final View.OnClickListener listener = (View.OnClickListener)result;
            		                { result = new View.OnClickListener() {
            		                    public void onClick(View view) {
            		                        listener.onClick(view);
            		                        Class<?> appCls = findClassIfExists("com.miui.home.launcher.Application", lpparam.getClassLoader());
            		                        if (appCls == null) return;
            		                        Object launcher = XposedHelpers.callStaticMethod(appCls, "getLauncher");
            		                        if (launcher == null) return;
            		                        if (MainModule.mPrefs.getBoolean("launcher_closedrawer")) XposedHelpers.callMethod(launcher, "hideAppView");
            		                        if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) > 1) XposedHelpers.callMethod(launcher, "closeFolder");
            		                    }
            		                }; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void CloseDrawerOnLaunchHook(PackageReadyParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mLauncher"), "hideAppView");


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.AppsListFragment", lpparam.getClassLoader(), "onClick", View.class, hook);
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.RecommendCategoryAppListFragment", lpparam.getClassLoader(), "onClick", View.class, hook);
    }

    public static void AssistGestureActionHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shared.recents.system.AssistManager", lpparam.getClassLoader(), "isSupportGoogleAssist", int.class, HookerClassHelper.returnConstant(true));
        final Class<?> FsGestureHelper = findClassIfExists("com.miui.home.recents.FsGestureAssistHelper", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod(FsGestureHelper, "canTriggerAssistantAction", float.class, float.class, int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                boolean isDisabled = (boolean) XposedHelpers.callStaticMethod(FsGestureHelper, "isAssistantGestureDisabled", args[2]);
            		                if (!isDisabled) {
            		                    int mAssistantWidth = XposedHelpers.getIntField(thisObject, "mAssistantWidth");
            		                    float f = (float) args[0];
            		                    float f2 = (float) args[1];
            		                    if (f < mAssistantWidth || f > f2 - mAssistantWidth) {
            		                        { skipped = true; result = true; throwable = null; }
                                            { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                    }
            		                }
            		                { skipped = true; result = false; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        final int[] inDirection = {0};

        ModuleHelper.hookAllMethods(FsGestureHelper, "handleTouchEvent", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                MotionEvent motionEvent = (MotionEvent) args[0];
            		                if (motionEvent.getAction() == 0) {
            		                    float mDownX = XposedHelpers.getFloatField(thisObject, "mDownX");
            		                    int mAssistantWidth = XposedHelpers.getIntField(thisObject, "mAssistantWidth");
            		                    inDirection[0] = mDownX < mAssistantWidth ? 0 : 1;
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.recents.SystemUiProxyWrapper", lpparam.getClassLoader(), "startAssistant", Bundle.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Bundle bundle = (Bundle) args[0];
            		                bundle.putInt("inDirection", inDirection[0]);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void SwipeAndStopActionHook(PackageReadyParam lpparam) {
        Class<?> ReadyStateEnum = findClassIfExists("com.miui.home.recents.GestureBackArrowView$ReadyState", lpparam.getClassLoader());
        if (ReadyStateEnum == null) return;
        Object[] states = ReadyStateEnum.getEnumConstants();
        Object recentState = null;
        Object backState = null;
        for (Object o:states) {
            String enumStr = o.toString();
            if ("READY_STATE_RECENT".equals(enumStr)) {
                recentState = o;
            }
            else if ("READY_STATE_BACK".equals(enumStr)) {
                backState = o;
            }
        }
        Object finalBackState = backState;
        Object finalRecentState = recentState;
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureBackArrowView", lpparam.getClassLoader(), "setReadyFinish", ReadyStateEnum, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                Object mReadyState = XposedHelpers.getObjectField(thisObject, "mReadyState");
            		                Object readyState = args[0];
            		                if (readyState != mReadyState) {
            		                    boolean disableVibrate = MainModule.mPrefs.getBoolean("controls_fsg_swipeandstop_disablevibrate");
            		                    View view = (View) thisObject;
            		                    XposedHelpers.setObjectField(view, "mRecentTaskIcon", null);
            		                    if (mReadyState == finalBackState && readyState == finalRecentState) {
            		                        float mScale = XposedHelpers.getFloatField(view, "mScale");
            		                        XposedHelpers.callMethod(view, "changeScale", mScale, 1.17f, 200, false);
            		                        if (!disableVibrate) {
            		                            Helpers.performStrongVibration(view.getContext(), true);
            		                        }
            		                    } else if (mReadyState == finalRecentState) {
            		                        float mScale = XposedHelpers.getFloatField(view, "mScale");
            		                        XposedHelpers.callMethod(view, "changeScale", mScale, 1.0f, 200, true);
            		                    }
            		                    XposedHelpers.setObjectField(view, "mReadyState", readyState);
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        Class<?> GestureStubViewClass = findClass("com.miui.home.recents.GestureStubView", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod(GestureStubViewClass, "disableQuickSwitch", boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                args[0] = false;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod(GestureStubViewClass, "isDisableQuickSwitch", HookerClassHelper.returnConstant(false));
        final Object[] gestureStubViews = {null};
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView$3", lpparam.getClassLoader(), "onSwipeStop", boolean.class, float.class, boolean.class, new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                boolean isFinished = (boolean) args[0];
	            		                if (isFinished) {
	            		                    Object outerThis = XposedHelpers.getSurroundingThis(thisObject);
	            		                    gestureStubViews[0] = outerThis;
	            		                }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}

                        	try {
                        		result = chain.proceed(args);
                        	} catch (Throwable t) {
                        		throwable = t;
                        		result = null;
                        	}
                        	__afterBody__: {
                        		try {

	            		                boolean isFinished = (boolean) args[0];
	            		                if (isFinished) {
	            		                    gestureStubViews[0] = null;
	            		                }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "getNextTask", Context.class, boolean.class, int.class, new MethodHook() {
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
            		Object[] args = XposedHelpers.getArgsArray(chain);

            		                boolean nextTaskInfo = (boolean) args[1];
            		                if (!nextTaskInfo || gestureStubViews[0] == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Object outerThis = gestureStubViews[0];
            		                ModuleHelper.callMethodSilently(outerThis, "onBackCancelled");
            		                Context mContext = (Context) XposedHelpers.getObjectField(outerThis, "mContext");
            		                int mGestureStubPos = (int) args[2];
            		                Bundle bundle = new Bundle();
            		                bundle.putInt("inDirection", mGestureStubPos);
            		                GlobalActions.handleAction(mContext, "controls_fsg_swipeandstop", false, bundle);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void DisableUnlockWallpaperScale(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.miwallpaper.manager.WallpaperServiceController", lpparam.getClassLoader(), "noNeedDesktopWallpaperScaleAnim",
            HookerClassHelper.returnConstant(true)
        );
    }

    public static void DisableLauncherWallpaperScale(PackageReadyParam lpparam) {
        Class<?> WallpaperZoomManagerKtClass = findClassIfExists("com.miui.home.launcher.wallpaper.WallpaperZoomManagerKt", lpparam.getClassLoader());
        if (MainModule.mPrefs.getBoolean("launcher_disable_wallpaperscale")) {
            XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", false);
            ModuleHelper.findAndHookMethod("com.miui.home.recents.DimLayer", lpparam.getClassLoader(), "isSupportDim", HookerClassHelper.returnConstant(false));
            return;
        }
        ModuleHelper.hookAllMethods("com.miui.home.recents.OverviewState", lpparam.getClassLoader(), "onStateEnabled", new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	__beforeBody__: {
                        		try {


	            		                if (WallpaperZoomManagerKtClass != null) {
	            		                    XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", false);
	            		                }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}

                        	try {
                                result = chain.proceed();
                        	} catch (Throwable t) {
                        		throwable = t;
                        		result = null;
                        	}
                        	__afterBody__: {
                        		try {

	            		                if (WallpaperZoomManagerKtClass != null) {
	            		                    XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", true);
	            		                }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
    }

    public static void HideStatusBarInRecentsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.common.DeviceLevelUtils", lpparam.getClassLoader(), "isHideStatusBarWhenEnterRecents", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.getClassLoader(), "keepStatusBarShowingForBetterPerformance", HookerClassHelper.returnConstant(false));
    }

    public static void DisableLauncherLogHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.AnalyticalDataCollectorJobService", lpparam.getClassLoader(), "onStartJob", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.AnalyticalDataCollector", lpparam.getClassLoader(), "canTrackLaunchAppEvent", HookerClassHelper.returnConstant(false));
        Class <?> OneTrackInterfaceUtils = findClassIfExists("com.miui.home.launcher.common.OneTrackInterfaceUtils", lpparam.getClassLoader());
        if (OneTrackInterfaceUtils != null) {
            XposedHelpers.setStaticObjectField(OneTrackInterfaceUtils, "IS_ENABLE", false);
        }
    }

    public static void LauncherPinchHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "onPinching", float.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                float dampingScale = (float)XposedHelpers.callMethod(thisObject, "getDampingScale", args[0]);
            		                float screenScaleRatio = (float)XposedHelpers.callMethod(thisObject, "getScreenScaleRatio");
            		                if (dampingScale < screenScaleRatio)
            		                    if (MainModule.mPrefs.getInt("launcher_pinch_action", 1) > 1) { skipped = true; result = false; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.getClassLoader(), "onPinchingEnd", float.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                float dampingScale = (float)XposedHelpers.callMethod(thisObject, "getDampingScale", args[0]);
            		                float screenScaleRatio = (float)XposedHelpers.callMethod(thisObject, "getScreenScaleRatio");
            		                if (dampingScale < screenScaleRatio)
            		                    if (GlobalActions.handleAction(((View)thisObject).getContext(), "launcher_pinch")) {
            		                        XposedHelpers.callMethod(thisObject, "finishCurrentGesture");

            		                        Class<?> pinchingStateEnum = XposedHelpers.findClass("com.miui.home.launcher.Workspace$PinchingState", lpparam.getClassLoader());
            		                        Object stateFollow = XposedHelpers.getStaticObjectField(pinchingStateEnum, "FOLLOW");
            		                        Object stateReadyToEdit = XposedHelpers.getStaticObjectField(pinchingStateEnum, "READY_TO_EDIT");

            		                        Object mState = XposedHelpers.getObjectField(thisObject, "mState");
            		                        XposedHelpers.setObjectField(thisObject, "mState", stateFollow);
            		                        if (mState == stateReadyToEdit)
            		                            XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mLauncher"), "changeEditingEntryViewToHotseats");
            		                        XposedHelpers.callMethod(thisObject, "resetCellScreenScale", args[0]);

            		                        { skipped = true; result = null; throwable = null; }
            		                    }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
    public static void ResizableWidgetsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("android.appwidget.AppWidgetHostView", lpparam.getClassLoader(), "getAppWidgetInfo", new MethodHook() {
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

            		                AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo) result;
            		                if (widgetInfo == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                widgetInfo.resizeMode = AppWidgetProviderInfo.RESIZE_VERTICAL | AppWidgetProviderInfo.RESIZE_HORIZONTAL;
            		                widgetInfo.minHeight = 0;
            		                widgetInfo.minWidth = 0;
            		                widgetInfo.minResizeHeight = 0;
            		                widgetInfo.minResizeWidth = 0;
            		                { result = widgetInfo; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
    public static void WallpaperColorModeHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.getClassLoader(), "setCurrentStatusBarAreaColorMode", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                int val = MainModule.mPrefs.getStringAsInt("launcher_wallpaper_colormode", 1);
            		                if (val > 1) {
            		                    args[0] = val == 2 ? 2 : 0;
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.getClassLoader(), "setCurrentWallpaperColorMode", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                int val = MainModule.mPrefs.getStringAsInt("launcher_wallpaper_colormode", 1);
            		                if (val > 1) {
            		                    args[0] = val == 2 ? 2 : 0;
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
    public static void setupLauncher(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.getClassLoader(), "registerBroadcastReceivers", new MethodHook() {
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
            		Object thisObject = chain.getThisObject();

            		                final Activity act = (Activity)thisObject;
            		                IntentFilter intentFilter = new IntentFilter();
            		                intentFilter.addAction(GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG");

            		                act.registerReceiver(new BroadcastReceiver() {
            		                    @Override
            		                    public void onReceive(Context context, Intent intent) {
            		                        try {
            		                            if (intent.getAction() == null) return;
            		                            if ((GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG").equals(intent.getAction())) {
            		                                Intent pushIntent = new Intent(GlobalActions.EVENT_PREFIX + "PUSHAPPCONFIG");
            		                                pushIntent.setPackage(Helpers.modulePkg);
            		                                String datatype = intent.getStringExtra("DATATYPE");
            		                                pushIntent.putExtra("DATATYPE", datatype);
														if ("privacy".equals(datatype)) {
															//noinspection WrongConstant -- MIUI private service name
															SecurityManager mSecurityManager = (SecurityManager) context.getSystemService("security");
            		                                    HashMap<Integer, List<String>> privacyAppsMap = new HashMap<>();
            		                                    privacyAppsMap.put(0, mSecurityManager.getAllPrivacyApps(0));
            		                                    privacyAppsMap.put(999, mSecurityManager.getAllPrivacyApps(999));
            		                                    pushIntent.putExtra("privacyAppsMap", privacyAppsMap);
            		                                    context.sendBroadcast(pushIntent);
            		                                }
														else if ("privacy_change".equals(datatype)) {
            		                                    int userId = intent.getIntExtra("userId", 0);
            		                                    String pkgName = intent.getStringExtra("app");
            		                                    boolean privacy = intent.getBooleanExtra("privacy", false);
															//noinspection WrongConstant -- MIUI private service name
															SecurityManager mSecurityManager = (SecurityManager) context.getSystemService("security");
            		                                    mSecurityManager.setPrivacyApp(pkgName, userId, privacy);
            		                                    context.getContentResolver().notifyChange(Uri.parse("content://com.miui.securitycenter.provider/update_privacyapps_icon"), null);
            		                                }
            		                            }
            		                        } catch (Throwable t) {
            		                            XposedHelpers.log(t);
            		                        }
            		                    }
            		                }, intentFilter, Context.RECEIVER_EXPORTED);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
}
