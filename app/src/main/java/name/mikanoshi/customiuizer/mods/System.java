package name.mikanoshi.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.findMethodExactIfExists;
import static name.mikanoshi.customiuizer.mods.GlobalActions.ACTION_PREFIX;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AndroidAppHelper;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.MiuiNotification;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.TaskStackBuilder;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetProviderInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BadParcelableException;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Magnifier;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.callbacks.XCallback;
import miui.telephony.TelephonyManager;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.AudioVisualizer;
import name.mikanoshi.customiuizer.utils.BatteryIndicator;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;
import name.mikanoshi.customiuizer.utils.Helpers.MimeType;
import name.mikanoshi.customiuizer.utils.SoundData;

public class System {
    private static String StatusBarCls = Helpers.isTPlus() ? "com.android.systemui.statusbar.phone.CentralSurfacesImpl" : "com.android.systemui.statusbar.phone.StatusBar";

    public static void ScreenAnimHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "initialize", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                try {
                    XposedHelpers.setObjectField(param.thisObject, "mColorFadeEnabled", true);
                    XposedHelpers.setObjectField(param.thisObject, "mColorFadeFadesConfig", true);
                } catch (Throwable ignore) {}
            }

            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

                //ObjectAnimator mColorFadeOnAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mColorFadeOnAnimator");
                //mColorFadeOnAnimator.setDuration(250);
                ObjectAnimator mColorFadeOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mColorFadeOffAnimator");
                //mColorFadeOffAnimator.setDuration(Helpers.getSharedIntPref(mContext, "pref_key_system_screenanim_duration", 0));
                if (mColorFadeOffAnimator != null) {
                    int val = MainModule.mPrefs.getInt("system_screenanim_duration", 0);
                    if (val == 0) val = 250;
                    mColorFadeOffAnimator.setDuration(val);
                }
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_screenanim_duration", 0) {
                    @Override
                    public void onChange(String name, int defValue) {
                        if (mColorFadeOffAnimator == null) return;
                        int val = Helpers.getSharedIntPref(mContext, name, defValue);
                        if (val == 0) val = 250;
                        mColorFadeOffAnimator.setDuration(val);
                    }
                };
            }
        });
    }

    public static void NoLightUpOnChargeHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpNoUpdateLocked", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String reason = param.args[1] instanceof String ? (String)param.args[1] : (String)param.args[2];
                if (reason == null) return;

                if (Integer.parseInt(MainModule.mPrefs.getString("system_nolightuponcharges", "1")) == 3 &&
                        (reason.equals("android.server.power:POWER") || reason.startsWith("android.server.power:PLUGGED"))) {
                    param.setResult(false);
                    return;
                }
                if (Integer.parseInt(MainModule.mPrefs.getString("system_nolightuponcharges", "1")) == 2 && (
                        reason.equals("android.server.power:POWER") ||
                                reason.startsWith("android.server.power:PLUGGED") ||
                                reason.equals("com.android.systemui:RAPID_CHARGE") ||
                                reason.equals("com.android.systemui:WIRELESS_CHARGE") ||
                                reason.equals("com.android.systemui:WIRELESS_RAPID_CHARGE")
                )) param.setResult(false);
                //Helpers.log("wakeUpNoUpdateLocked: " + param.args[0] + " | " + param.args[1] + " | " + param.args[2] + " | " + param.args[3] + " | " + param.args[4]);
            }
        });
    }

    public static void NoLightUpOnHeadsetHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpNoUpdateLocked", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String reason = param.args[1] instanceof String ? (String)param.args[1] : (String)param.args[2];
                if ("com.android.systemui:HEADSET".equals(reason)) param.setResult(false);
            }
        });
    }

    public static void ScramblePINHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.keyguard.KeyguardPINView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                View[][] mViews = (View[][])XposedHelpers.getObjectField(param.thisObject, "mViews");
                ArrayList<View> mRandomViews = new ArrayList<View>();
                for (int row = 1; row <= 4; row++)
                    for (int col = 0; col <= 2; col++)
                        if (mViews[row][col] != null)
                            mRandomViews.add(mViews[row][col]);
                Collections.shuffle(mRandomViews);

                View pinview = (View)param.thisObject;
                ViewGroup row1 = pinview.findViewById(pinview.getResources().getIdentifier("row1", "id", "com.android.systemui"));
                ViewGroup row2 = pinview.findViewById(pinview.getResources().getIdentifier("row2", "id", "com.android.systemui"));
                ViewGroup row3 = pinview.findViewById(pinview.getResources().getIdentifier("row3", "id", "com.android.systemui"));
                ViewGroup row4 = pinview.findViewById(pinview.getResources().getIdentifier("row4", "id", "com.android.systemui"));

                row1.removeAllViews();
                row2.removeAllViews();
                row3.removeAllViews();
                row4.removeAllViews();

                mViews[1] = new View[]{ mRandomViews.get(0), mRandomViews.get(1), mRandomViews.get(2)};
                row1.addView(mRandomViews.get(0));
                row1.addView(mRandomViews.get(1));
                row1.addView(mRandomViews.get(2));

                mViews[2] = new View[]{ mRandomViews.get(3), mRandomViews.get(4), mRandomViews.get(5)};
                row2.addView(mRandomViews.get(3));
                row2.addView(mRandomViews.get(4));
                row2.addView(mRandomViews.get(5));

                mViews[3] = new View[]{ mRandomViews.get(6), mRandomViews.get(7), mRandomViews.get(8)};
                row3.addView(mRandomViews.get(6));
                row3.addView(mRandomViews.get(7));
                row3.addView(mRandomViews.get(8));

                mViews[4] = new View[]{ null, mRandomViews.get(9), null};
                row4.addView(mRandomViews.get(9));

                XposedHelpers.setObjectField(param.thisObject, "mViews", mViews);
            }
        });
    }

    public static void NoPasswordHook() {
        String isAllowed = "isBiometricAllowedForUser";
        Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, isAllowed, boolean.class, int.class, XC_MethodReplacement.returnConstant(true));
        Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils", null, isAllowed, int.class, XC_MethodReplacement.returnConstant(true));
    }

//	public static void NoPasswordKeyguardHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", XC_MethodReplacement.returnConstant(true));
//		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", int.class, XC_MethodReplacement.returnConstant(true));
//		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "isUnlockWithFingerprintPossible", int.class, XC_MethodReplacement.returnConstant(true));
//	}

    public static void EnhancedSecurityHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptPowerKeyDown", KeyEvent.class, boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) {
                    Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                    if (mHandler != null) {
                        Runnable mEndCallLongPress = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mEndCallLongPress");
                        if (mEndCallLongPress != null) mHandler.removeCallbacks(mEndCallLongPress);
                    }
                }
            }
        });

        Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "powerLongPress", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.setResult(null);
            }
        });

        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActions", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.setResult(null);
            }
        });

        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActionsInternal", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.setResult(null);
            }
        });
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean isAuthOnce() {
        int req = MainModule.mPrefs.getStringAsInt("system_noscreenlock_req", 1);
        if (req <= 1) return true;
        if (req == 2 && !isUnlockedWithFingerprint && !isUnlockedWithStrong) return false;
        if (req == 3 && !isUnlockedWithStrong) return false;
        return true;
    }

    private static boolean isTrusted(Context mContext, ClassLoader classLoader) {
        return isTrustedWiFi(mContext) || isTrustedBt(classLoader);
    }

    private static boolean isTrustedWiFi(Context mContext) {
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) return false;
        if (!wifiManager.isWifiEnabled()) return false;
        Set<String> trustedNetworks = MainModule.mPrefs.getStringSet("system_noscreenlock_wifi");
        return Helpers.containsStringPair(trustedNetworks, wifiManager.getConnectionInfo().getBSSID());
    }

    @SuppressLint("MissingPermission")
    private static boolean isTrustedBt(ClassLoader classLoader) {
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) return false;
            Set<String> trustedDevices = MainModule.mPrefs.getStringSet("system_noscreenlock_bt");
            Object mController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", classLoader), "get", findClass("com.android.systemui.statusbar.policy.BluetoothController", classLoader));
            Collection<?> cachedDevices = (Collection<?>)XposedHelpers.callMethod(mController, "getDevices");
            if (cachedDevices != null)
                for (Object device: cachedDevices) {
                    BluetoothDevice mDevice = (BluetoothDevice)XposedHelpers.getObjectField(device, "mDevice");
                    if (mDevice == null) continue;
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED &&
                            (boolean)XposedHelpers.callMethod(device, "isConnected") &&
                            Helpers.containsStringPair(trustedDevices, mDevice.getAddress())) return true;
                }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        return false;
    }

    private static boolean isUnlocked(Context mContext, ClassLoader classLoader) {
        Class<?> kmvClas = findClass("com.android.systemui.keyguard.KeyguardViewMediator", classLoader);
        XposedHelpers.setAdditionalStaticField(kmvClas, "isScreenLockDisabled", false);
        if (!isAuthOnce()) return false;
        int opt = MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1);
        if (forcedOption == 1) opt = 2;
        boolean isTrusted = false;
        if (opt == 3) isTrusted = isTrusted(mContext, classLoader);
        if (opt == 2 || opt == 3 && isTrusted) {
            XposedHelpers.setAdditionalStaticField(kmvClas, "isScreenLockDisabled", true);
            return true;
        }
        return false;
    }

    private static void checkBTConnections(Context mContext) {
        if (mContext == null)
            Helpers.log("checkBTConnections", "mContext is NULL!");
        else
            mContext.sendBroadcast(new Intent(ACTION_PREFIX + "UnlockBTConnection"));
    }

    private static boolean isUnlockedInnerCall = false;
    private static boolean isUnlockedWithFingerprint = false;
    private static boolean isUnlockedWithStrong = false;
    private static int forcedOption = -1;
    public static void NoScreenLockHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "handleKeyguardDone", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (isUnlockedInnerCall) {
                    isUnlockedInnerCall = false;
                    return;
                }
                isUnlockedWithStrong = true;
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "onFingerprintAuthenticated", int.class, boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                isUnlockedWithFingerprint = true;
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.KeyguardSecurityContainer", lpparam.classLoader, "onFinishInflate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            Object mCallback = XposedHelpers.getObjectField(param.thisObject, "mCallback");
                            XposedHelpers.callMethod(mCallback, "reportUnlockAttempt", 0, true, 0, 0);
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                }, new IntentFilter(ACTION_PREFIX + "UnlockStrongAuth"));
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "doKeyguardLocked", Bundle.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (forcedOption == 0) return;
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (!isUnlocked(mContext, lpparam.classLoader)) return;

                boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
                if (skip) {
                    param.setResult(null);
                    XposedHelpers.callMethod(param.thisObject, "keyguardDone");
                }
                isUnlockedInnerCall = true;
                Intent unlockIntent = new Intent(ACTION_PREFIX + "UnlockStrongAuth");
                mContext.sendBroadcast(unlockIntent);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "setupLocked", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                filter.addAction(ACTION_PREFIX + "UnlockSetForced");
                filter.addAction(ACTION_PREFIX + "UnlockBTConnection");
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();

                        if (action.equals(ACTION_PREFIX + "UnlockSetForced"))
                            forcedOption = intent.getIntExtra("system_noscreenlock_force", -1);

                        boolean isShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isShowing");
                        if (!isShowing) return;
                        if (!isAuthOnce()) return;

                        boolean isTrusted = false;
                        if (forcedOption == 0) isTrusted = false;
                        else if (forcedOption == 1) isTrusted = true;
                        else if (MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1) == 3)
                            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                                if (netInfo.getState() != NetworkInfo.State.CONNECTED && netInfo.getState() != NetworkInfo.State.DISCONNECTED) return;
                                if (netInfo.isConnected()) isTrusted = isTrustedWiFi(mContext);
                            } else if (action.equals(ACTION_PREFIX + "UnlockBTConnection")) {
                                isTrusted = isTrustedBt(lpparam.classLoader);
                            }

                        XposedHelpers.setAdditionalStaticField(param.thisObject, "isScreenLockDisabled", isTrusted);
                        if (isTrusted) {
                            boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
                            if (skip)
                                XposedHelpers.callMethod(param.thisObject, "keyguardDone");
                            else
                                XposedHelpers.callMethod(param.thisObject, "resetStateLocked");
                            isUnlockedInnerCall = true;
//                            XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
                            Intent unlockIntent = new Intent(ACTION_PREFIX + "UnlockStrongAuth");
                            mContext.sendBroadcast(unlockIntent);
                        } else try {
                            Object mLockUserManager = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClassIfExists("com.android.systemui.statusbar.NotificationLockscreenUserManager", lpparam.classLoader));
                            XposedHelpers.callMethod(mLockUserManager, "updatePublicMode");
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                }, filter);

                new Helpers.SharedPrefObserver(mContext, new Handler(mContext.getMainLooper())) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String type = uri.getPathSegments().get(1);
                            String key = uri.getPathSegments().get(2);
                            if (!key.contains("pref_key_system_noscreenlock")) return;

                            switch (type) {
                                case "string":
                                    MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, ""));
                                    break;
                                case "stringset":
                                    MainModule.mPrefs.put(key, Helpers.getSharedStringSetPref(mContext, key));
                                    break;
                                case "integer":
                                    MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 1));
                                    break;
                                case "boolean":
                                    MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(mContext, key, false));
                                    break;
                            }
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.KeyguardSecurityModel", lpparam.classLoader, "getSecurityMode", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (forcedOption == 0) return;
                boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
                if (skip) return;
                Context mContext = (Context) AndroidAppHelper.currentApplication();
                if (!isUnlocked(mContext, lpparam.classLoader)) return;

                Class<?> securityModeEnum = XposedHelpers.findClass("com.android.keyguard.KeyguardSecurityModel$SecurityMode", lpparam.classLoader);
                Object securityModeNone = XposedHelpers.getStaticObjectField(securityModeEnum, "None");
                Object securityModePassword = XposedHelpers.getStaticObjectField(securityModeEnum, "Password");
                Object securityModePattern = XposedHelpers.getStaticObjectField(securityModeEnum, "Pattern");
                Object securityModePin = XposedHelpers.getStaticObjectField(securityModeEnum, "PIN");

                Object secModeResult = param.getResult();
                if (securityModePassword.equals(secModeResult) ||
                        securityModePattern.equals(secModeResult) ||
                        securityModePin.equals(secModeResult))
                    param.setResult(securityModeNone);
            }
        });

        Class<?> startClass = findClassIfExists("com.android.keyguard.faceunlock.FaceUnlockManager", lpparam.classLoader);
        if (startClass != null) {
            Helpers.hookAllMethods(startClass, "startFaceUnlock", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_nofaceunlock");
                    if (!skip) return;
                    if (Helpers.is12() && param.args.length == 0) return;
                    Boolean isScreenLockDisabled = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader), "isScreenLockDisabled");
                    isScreenLockDisabled = isScreenLockDisabled != null && isScreenLockDisabled;
                    if (isScreenLockDisabled) param.setResult(null);
                }
            });

            Method showMsgMethod = findMethodExactIfExists(startClass, "isShowMessageWhenFaceUnlockSuccess");
            if (showMsgMethod == null) findMethodExactIfExists(startClass, "isFaceUnlockSuccessAndShowMessage");
            if (showMsgMethod == null) {
                Helpers.log("NoScreenLockHook", "Show notification message method not found");
            } else {
                Helpers.hookAllMethods(startClass, showMsgMethod.getName(), new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) throws Throwable {
                        boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_nofaceunlock");
                        if (!skip || !(boolean)param.getResult()) return;
                        boolean isScreenLockDisabled = (boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader), "isScreenLockDisabled");
                        if (isScreenLockDisabled) param.setResult(false);
                    }
                });
            }
        }

        Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, new MethodHook(10) {
            @Override
            protected void after(MethodHookParam param) {
                Context mContext = (Context)param.args[0];
                mContext.registerReceiver(new BroadcastReceiver() {
                    public void onReceive(final Context context, Intent intent) {
                        ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
                        Intent updateIntent = new Intent(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE");
                        Collection<?> cachedDevices = (Collection<?>)XposedHelpers.callMethod(param.thisObject, "getDevices");
                        if (cachedDevices != null)
                            for (Object device: cachedDevices) {
                                BluetoothDevice mDevice = (BluetoothDevice)XposedHelpers.getObjectField(device, "mDevice");
                                if (mDevice != null) deviceList.add(mDevice);
                            }
                        updateIntent.putParcelableArrayListExtra("device_list", deviceList);
                        updateIntent.setPackage(Helpers.modulePkg);
                        mContext.sendBroadcast(updateIntent);
                    }
                }, new IntentFilter(ACTION_PREFIX + "FetchCachedDevices"));
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, "updateConnected", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
            checkBTConnections((Context)XposedHelpers.getObjectField(param.thisObject, "mContext"));
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, "onBluetoothStateChanged", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
            int state = (int)param.args[0];
            if (state != 10) checkBTConnections((Context)XposedHelpers.getObjectField(param.thisObject, "mContext"));
            }
        });
    }

    private static ImageView createIcon(Context ctx, int baseSize) {
        float density = ctx.getResources().getDisplayMetrics().density;
        ImageView iv = new ImageView(ctx);
        try {
            iv.setImageDrawable(ctx.getPackageManager().getApplicationIcon(ctx.getPackageName()));
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int size = Math.round(baseSize * density);
        LinearLayout.LayoutParams lpi = new LinearLayout.LayoutParams(size, size);
        lpi.setMargins(0, 0, Math.round(8 * density), 0);
        lpi.gravity = Gravity.CENTER;
        iv.setLayoutParams(lpi);

        return iv;
    }

    private static TextView createLabel(Context ctx, TextView toastText) {
        TextView tv = new TextView(ctx);
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setText(ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()));
        tv.setTextColor(Helpers.isNightMode(ctx) ? Color.WHITE : Color.BLACK);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, toastText.getTextSize());
        tv.setTypeface(toastText.getTypeface());
        tv.setSingleLine(true);
        tv.setAlpha(0.6f);
        return tv;
    }

    private static void modifyIconLabelToast(MethodHookParam param) {
        Context ctx = (Context)param.args[0];
        float density = ctx.getResources().getDisplayMetrics().density;

        int option = Integer.parseInt(Helpers.getSharedStringPref(ctx, "pref_key_system_iconlabletoasts", "1"));
        if (option == 1) return;

        Object res = param.getResult();
        if (res == null) return;
        LinearLayout toast = (LinearLayout)XposedHelpers.getObjectField(res, "mNextView");
        if (toast == null) return;
        toast.setGravity(Gravity.START);
        toast.setPadding(toast.getPaddingLeft() - Math.round(5 * density), toast.getPaddingTop() - Math.round(3 * density), toast.getPaddingRight(), toast.getPaddingBottom() - Math.round(3 * density));

        TextView toastText = toast.findViewById(android.R.id.message);
        if (toastText == null) return;
        toast.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        toastText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        toastText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        toastText.setLetterSpacing(0.015f);
        LinearLayout.LayoutParams lpt = (LinearLayout.LayoutParams)toastText.getLayoutParams();
        lpt.gravity = Gravity.START;

        switch (option) {
            case 2:
                ImageView iv;
                LinearLayout textOnly = new LinearLayout(ctx);
                textOnly.setOrientation(LinearLayout.VERTICAL);
                textOnly.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                textOnly.setPadding(0, Math.round(5 * density), 0, Math.round(6 * density));
                iv = createIcon(ctx, 22);

                toast.removeAllViews();
                textOnly.addView(toastText);
                toast.setOrientation(LinearLayout.HORIZONTAL);
                toast.addView(iv);
                toast.addView(textOnly);
                break;
            case 3:
                TextView tv = createLabel(ctx, toastText);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)tv.getLayoutParams();
                lp.leftMargin = Math.round(5 * density);
                lp.rightMargin = Math.round(5 * density);
                lp.topMargin = Math.round(5 * density);
                tv.setLayoutParams(lp);
                lp = (LinearLayout.LayoutParams)toastText.getLayoutParams();
                lp.leftMargin = Math.round(5 * density);
                lp.rightMargin = Math.round(5 * density);
                lp.bottomMargin = Math.round(5 * density);
                toastText.setLayoutParams(lp);
                toast.setOrientation(LinearLayout.VERTICAL);
                toast.addView(tv, 0);
                break;
            case 4:
                LinearLayout textLabel = new LinearLayout(ctx);
                textLabel.setOrientation(LinearLayout.VERTICAL);
                textLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                textLabel.setPadding(0, Math.round(5 * density), 0, Math.round(6 * density));
                ImageView iv2 = createIcon(ctx, 42);
                TextView tv2 = createLabel(ctx, toastText);

                toast.removeAllViews();
                textLabel.addView(tv2);
                textLabel.addView(toastText);
                toast.setOrientation(LinearLayout.HORIZONTAL);
                toast.addView(iv2);
                toast.addView(textLabel);
                break;
        }
        XposedHelpers.setObjectField(res, "mNextView", toast);
        param.setResult(res);
    }

    public static void IconLabelToastsHook() {
        if (!Helpers.findAndHookMethodSilently("android.widget.Toast", null, "makeText", Context.class, Looper.class, CharSequence.class, int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
            modifyIconLabelToast(param);
            }
        })) Helpers.findAndHookMethod("android.widget.Toast", null, "makeText", Context.class, CharSequence.class, int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
            modifyIconLabelToast(param);
            }
        });

        Helpers.findAndHookMethod("android.widget.ToastInjectorImpl", null, "addAppName", Context.class, CharSequence.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
            Context context = (Context)param.args[0];
            int opt = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_iconlabletoasts", "1"));
            if (opt != 1) param.setResult(param.args[1]);
            }
        });
    }

    public static void DoubleTapToSleepHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer", lpparam.classLoader, "onFinishInflate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                View view = (View)param.thisObject;
                XposedHelpers.setAdditionalInstanceField(view, "currentTouchTime", 0L);
                XposedHelpers.setAdditionalInstanceField(view, "currentTouchX", 0F);
                XposedHelpers.setAdditionalInstanceField(view, "currentTouchY", 0F);

                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    @SuppressLint("ClickableViewAccessibility")
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() != MotionEvent.ACTION_DOWN) return false;

                        long currentTouchTime = (long)XposedHelpers.getAdditionalInstanceField(view, "currentTouchTime");
                        float currentTouchX = (float)XposedHelpers.getAdditionalInstanceField(view, "currentTouchX");
                        float currentTouchY = (float)XposedHelpers.getAdditionalInstanceField(view, "currentTouchY");

                        long lastTouchTime = currentTouchTime;
                        float lastTouchX = currentTouchX;
                        float lastTouchY = currentTouchY;
                        currentTouchTime = currentTimeMillis();
                        currentTouchX = event.getX();
                        currentTouchY = event.getY();

                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(currentTouchX - lastTouchX) < 100F && Math.abs(currentTouchY - lastTouchY) < 100F) {
                            KeyguardManager keyguardMgr = (KeyguardManager)v.getContext().getSystemService(Context.KEYGUARD_SERVICE);
                            if (keyguardMgr.isKeyguardLocked()) GlobalActions.goToSleep(v.getContext());
                            currentTouchTime = 0L;
                            currentTouchX = 0F;
                            currentTouchY = 0F;
                        }

                        XposedHelpers.setAdditionalInstanceField(view, "currentTouchTime", currentTouchTime);
                        XposedHelpers.setAdditionalInstanceField(view, "currentTouchX", currentTouchX);
                        XposedHelpers.setAdditionalInstanceField(view, "currentTouchY", currentTouchY);

                        return false;
                    }
                });
            }
        });
    }

    private static int notifVolumeOnResId;
    private static int notifVolumeOffResId;
    public static void NotificationVolumeDialogRes() {
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_content_width_expanded", R.dimen.miui_volume_content_width_expanded);
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_ringer_layout_width_expanded", R.dimen.miui_volume_ringer_layout_width_expanded);
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_width_expanded", R.dimen.miui_volume_column_width_expanded);
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_margin_horizontal_expanded", R.dimen.miui_volume_column_margin_horizontal_expanded);
        notifVolumeOnResId = MainModule.resHooks.addResource("ic_miui_volume_notification", R.drawable.ic_miui_volume_notification);
        notifVolumeOffResId = MainModule.resHooks.addResource("ic_miui_volume_notification_mute", R.drawable.ic_miui_volume_notification_mute);
    }

    private static int settingsSystemResId;
    private static int callsResId;
    public static void NotificationVolumeSettingsRes() {
        settingsSystemResId = MainModule.resHooks.addResource("ic_audio_system", R.drawable.ic_audio_system);
        callsResId = MainModule.resHooks.addResource("ring_volume_option_newtitle", R.string.calls);
    }

    public static void NotificationVolumeServiceHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "updateStreamVolumeAlias", boolean.class, String.class, new MethodHook() {
            protected void after(MethodHookParam param) throws Throwable {
                int[] mStreamVolumeAlias = (int[])XposedHelpers.getObjectField(param.thisObject, "mStreamVolumeAlias");
                mStreamVolumeAlias[1] = 1;
                mStreamVolumeAlias[5] = 5;
                XposedHelpers.setObjectField(param.thisObject, "mStreamVolumeAlias", mStreamVolumeAlias);
            }
        });

        Helpers.findAndHookMethod("com.android.server.audio.AudioService$VolumeStreamState", lpparam.classLoader, "readSettings", new MethodHook() {
            protected void before(MethodHookParam param) throws Throwable {
                int mStreamType = XposedHelpers.getIntField(param.thisObject, "mStreamType");
                if (mStreamType != 1) return;
                synchronized (param.method.getDeclaringClass()) {
                    Class<?> audioSystem = findClass("android.media.AudioSystem", lpparam.classLoader);
                    Set<Integer> DEVICE_OUT_ALL = (Set<Integer>) XposedHelpers.getStaticObjectField(audioSystem, "DEVICE_OUT_ALL_SET");
                    int DEVICE_OUT_DEFAULT = XposedHelpers.getStaticIntField(audioSystem, "DEVICE_OUT_DEFAULT");
                    int[] DEFAULT_STREAM_VOLUME = (int[])XposedHelpers.getStaticObjectField(audioSystem, "DEFAULT_STREAM_VOLUME");
                    Set<Integer> remainingDevices = DEVICE_OUT_ALL;
                    Object mContentResolver = XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mContentResolver");
                    SparseIntArray mIndexMap = (SparseIntArray)XposedHelpers.getObjectField(param.thisObject, "mIndexMap");
                    for (Integer deviceType : remainingDevices) {
                        int device = deviceType.intValue();
                        String name = (String)XposedHelpers.callMethod(param.thisObject, "getSettingNameForDevice", device);
                        int index = (int)XposedHelpers.callStaticMethod(Settings.System.class, "getIntForUser", mContentResolver, name, device == DEVICE_OUT_DEFAULT ? DEFAULT_STREAM_VOLUME[mStreamType] : -1, -2);
                        if (index != -1) {
                            mIndexMap.put(device, (int)XposedHelpers.callMethod(param.thisObject, "getValidIndex", 10 * index, true));
                        }
                    }
                    XposedHelpers.setObjectField(param.thisObject, "mIndexMap", mIndexMap);
                }
                param.setResult(null);
            }
        });

        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.server.audio.AudioService", lpparam.classLoader, "shouldZenMuteStream", int.class, new MethodHook() {
            protected void after(MethodHookParam param) throws Throwable {
                int mStreamType = (int)param.args[0];
                if (mStreamType == 5 && !(boolean)param.getResult()) {
                    int mZenMode = (int)XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mNm"), "getZenMode");
                    if (mZenMode == 1) param.setResult(true);
                }
            }
        });
    }

    private static ClassLoader pluginLoader = null;

    public static void MIUIVolumeDialogHook(LoadPackageParam lpparam) {
        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    Class<?> MiuiVolumeDialogImpl = XposedHelpers.findClassIfExists("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", pluginLoader);
                    if (MainModule.mPrefs.getBoolean("system_separatevolume") && MainModule.mPrefs.getBoolean("system_separatevolume_slider")) {
                        Helpers.hookAllMethods(MiuiVolumeDialogImpl, "addColumn", new MethodHook() {
                            @Override
                            protected void before(MethodHookParam param) throws Throwable {
                                if (param.args.length != 4) return;
                                int streamType = (int) param.args[0];
                                if (streamType == 4) {
                                    XposedHelpers.callMethod(param.thisObject, "addColumn", 5, notifVolumeOnResId, notifVolumeOffResId, true, false);
                                }
                            }
                        });
                    }
                    if (MainModule.mPrefs.getBoolean("system_nosilentvibrate")) {
                        Helpers.hookAllMethods(MiuiVolumeDialogImpl, "vibrateH", XC_MethodReplacement.DO_NOTHING);
                    }
                    if (MainModule.mPrefs.getInt("system_volumedialogdelay_collapsed", 0) > 0 || MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0) > 0) {
                        VolumeDialogAutohideDelayHook(pluginLoader);
                    }
                    if (MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) > 0 || MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) > 0) {
                        BlurVolumeDialogBackgroundHook(pluginLoader);
                    }
                }
            }
        });
    }

    public static void NotificationVolumeSettingsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.settings.MiuiSoundSettings", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object fragment = param.thisObject;
                Context context = (Context)XposedHelpers.callMethod(fragment, "getActivity");
                Resources modRes = Helpers.getModuleRes(context);
                int order = 6;

                Class<?> vsbCls;
                Method[] initSeekBar;
                String addPreference = "addPreference";
                try {
                    vsbCls = XposedHelpers.findClassIfExists("com.android.settings.sound.VolumeSeekBarPreference", lpparam.classLoader);
                    initSeekBar = XposedHelpers.findMethodsByExactParameters(fragment.getClass(), void.class, String.class, int.class, int.class);
                    if (vsbCls == null || initSeekBar.length == 0) {
                        Helpers.log("NotificationVolumeSettingsHook", "Unable to find class/method in Settings to hook");
                        return;
                    } else {
                        initSeekBar[0].setAccessible(true);
                    }
                    Class<?> pgCls = XposedHelpers.findClassIfExists("androidx.preference.PreferenceGroup", lpparam.classLoader);
                    Class<?> pCls = XposedHelpers.findClassIfExists("androidx.preference.Preference", lpparam.classLoader);
                    Method[] methods = XposedHelpers.findMethodsByExactParameters(pgCls, void.class, pCls);
                    for (Method method: methods)
                        if (Modifier.isPublic(method.getModifiers())) {
                            addPreference = method.getName();
                            break;
                        }
                } catch (Throwable t) {
                    Helpers.log("NotificationVolumeSettingsHook", "Unable to find class/method in Settings to hook");
                    return;
                }

                Object media = XposedHelpers.callMethod(fragment, "findPreference", "media_volume");
                if (media != null) order = (int)XposedHelpers.callMethod(media, "getOrder");

                Object prefScreen = XposedHelpers.callMethod(fragment, "getPreferenceScreen");
                Object pref = XposedHelpers.newInstance(vsbCls, context);
                XposedHelpers.callMethod(pref, "setKey", "notification_volume");
                XposedHelpers.callMethod(pref, "setTitle", modRes.getString(R.string.system_mods_notifications));
                XposedHelpers.callMethod(pref, "setPersistent", true);
                XposedHelpers.callMethod(prefScreen, addPreference, pref);
                initSeekBar[0].invoke(fragment, "notification_volume", 5, context.getResources().getIdentifier("ic_audio_notification", "drawable", context.getPackageName()));
                XposedHelpers.callMethod(pref, "setOrder", order);

                pref = XposedHelpers.newInstance(vsbCls, context);
                XposedHelpers.callMethod(pref, "setKey", "system_volume");
                XposedHelpers.callMethod(pref, "setTitle", modRes.getString(R.string.system_volume));
                XposedHelpers.callMethod(pref, "setPersistent", true);
                XposedHelpers.callMethod(prefScreen, addPreference, pref);
                initSeekBar[0].invoke(fragment, "system_volume", 1, Helpers.is12() ? context.getResources().getIdentifier("ic_audio_vol", "drawable", context.getPackageName()) : settingsSystemResId);
                XposedHelpers.callMethod(pref, "setOrder", order);

                Object mRingVolume = XposedHelpers.callMethod(param.thisObject, "findPreference", "ring_volume");
                XposedHelpers.callMethod(mRingVolume, "setTitle", callsResId);
            }
        });
    }

    public static void ViewWifiPasswordHook(LoadPackageParam lpparam) {
        int titleId = MainModule.resHooks.addResource("system_wifipassword_btn_title", R.string.system_wifipassword_btn_title);
        int dlgTitleId = MainModule.resHooks.addResource("system_wifi_password_dlgtitle", R.string.system_wifi_password_dlgtitle);
        Helpers.hookAllMethods("com.android.settings.wifi.SavedAccessPointPreference", lpparam.classLoader, "onBindViewHolder", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                View view = (View) XposedHelpers.getObjectField(param.thisObject, "mView");
                int btnId = view.getResources().getIdentifier("btn_delete", "id", "com.android.settings");
                Button button = view.findViewById(btnId);
                button.setText(titleId);
            }
        });
        final String[] wifiSharedKey = new String[1];
        final String[] passwordTitle = new String[1];
        Helpers.findAndHookMethod("miuix.appcompat.app.AlertDialog$Builder", lpparam.classLoader, "setTitle", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (wifiSharedKey[0] != null) {
                    param.args[0] = dlgTitleId;
                }
            }
        });

        Helpers.findAndHookMethod("miuix.appcompat.app.AlertDialog$Builder", lpparam.classLoader, "setMessage", CharSequence.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (wifiSharedKey[0] != null) {
                    CharSequence str = (CharSequence) param.args[0];
                    str = str + "\n" + passwordTitle[0] + ": " + wifiSharedKey[0];
                    param.args[0] = str;
                }
            }
        });
        Helpers.hookAllMethods("miuix.appcompat.app.AlertDialog", lpparam.classLoader, "onCreate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (wifiSharedKey[0] != null) {
                    TextView messageView = (TextView) XposedHelpers.callMethod(param.thisObject, "getMessageView");
                    messageView.setTextIsSelectable(true);
                }
            }
        });
        Helpers.hookAllMethods("com.android.settings.wifi.MiuiSavedAccessPointsWifiSettings", lpparam.classLoader, "showDeleteDialog", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object wifiEntry = param.args[0];
                boolean canShare = (boolean) XposedHelpers.callMethod(wifiEntry, "canShare");
                if (canShare) {
                    if (passwordTitle[0] == null) {
                        Resources modRes = Helpers.getModuleRes((Context) XposedHelpers.callMethod(param.thisObject, "getContext"));
                        passwordTitle[0] = modRes.getString(R.string.system_wifi_password_label);
                    }
                    Object mWifiManager = XposedHelpers.getObjectField(param.thisObject, "mWifiManager");
                    Object wifiConfiguration = XposedHelpers.callMethod(wifiEntry, "getWifiConfiguration");
                    Class <?> WifiDppUtilsClass = XposedHelpers.findClass("com.android.settings.wifi.dpp.WifiDppUtils", lpparam.classLoader);
                    String sharedKey = (String) XposedHelpers.callStaticMethod(WifiDppUtilsClass, "getPresharedKey", mWifiManager, wifiConfiguration);
                    sharedKey = (String) XposedHelpers.callStaticMethod(WifiDppUtilsClass, "removeFirstAndLastDoubleQuotes", sharedKey);
                    wifiSharedKey[0] = sharedKey;
                }
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object wifiEntry = param.args[0];
                boolean canShare = (boolean) XposedHelpers.callMethod(wifiEntry, "canShare");
                if (canShare) {
                    wifiSharedKey[0] = null;
                }
            }
        });
    }

    public static void ClockSecondsHook(LoadPackageParam lpparam) {
        boolean showSeconds = MainModule.mPrefs.getBoolean("system_clockseconds") || MainModule.mPrefs.getBoolean("system_drawer_clockseconds");
        MethodHook ScheduleHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean isTimeSet = param.args.length == 2;
                Context mContext = (Context) param.args[0];
                if (isTimeSet) {
                    Intent intent = (Intent) param.args[1];
                    String action = intent.getAction();
                    if ("android.intent.action.TIME_SET".equals(action)) {
                        Timer scheduleTimer = (Timer) XposedHelpers.getAdditionalInstanceField(param.thisObject, "scheduleTimer");
                        scheduleTimer.cancel();
                    }
                    else {
                        return;
                    }
                }
                final Handler mClockHandler = new Handler(mContext.getMainLooper());
                long delay = 1000 - SystemClock.elapsedRealtime() % 1000;
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        mClockHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "updateFromTimer", true);
                                XposedHelpers.callMethod(param.thisObject, "updateTime");
                            }
                        });
                    }
                }, delay, 1000);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "scheduleTimer", timer);
            }
        };
        if (showSeconds) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.classLoader, "onReceive", Context.class, Intent.class, ScheduleHook);
            Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.classLoader, ScheduleHook);
            Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.classLoader, "fireTimeChange", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    ArrayList<Object> mClockListeners = (ArrayList<Object>) XposedHelpers.getObjectField(param.thisObject, "mClockListeners");
                    Iterator<Object> it = mClockListeners.iterator();
                    while (it.hasNext()) {
                        Object clock = it.next();
                        if (XposedHelpers.getAdditionalInstanceField(param.thisObject, "updateFromTimer") != null
                            && XposedHelpers.getAdditionalInstanceField(clock, "showSeconds") != null) {
                            XposedHelpers.callMethod(clock, "onTimeChange");
                        }
                        else if (XposedHelpers.getAdditionalInstanceField(param.thisObject, "updateFromTimer") == null
                            && XposedHelpers.getAdditionalInstanceField(clock, "showSeconds") == null) {
                            XposedHelpers.callMethod(clock, "onTimeChange");
                        }
                    }
                    XposedHelpers.removeAdditionalInstanceField(param.thisObject, "updateFromTimer");
                    param.setResult(null);
                }
            });
        }
        Helpers.hookAllConstructors("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                final TextView clock = (TextView)param.thisObject;
                if (param.args.length != 3) return;
                int clockId = clock.getResources().getIdentifier("clock", "id", "com.android.systemui");
                int bigClockId = clock.getResources().getIdentifier("big_time", "id", "com.android.systemui");
                if ((clock.getId() == clockId && MainModule.mPrefs.getBoolean("system_clockseconds"))
                    || (clock.getId() == bigClockId && MainModule.mPrefs.getBoolean("system_drawer_clockseconds"))) {
                    XposedHelpers.setAdditionalInstanceField(clock, "showSeconds", true);
                    if (clock.getId() == clockId) {
                        XposedHelpers.setAdditionalInstanceField(clock, "mFixedWidth", true);
                    }
                }
                if (clock.getId() == clockId && MainModule.mPrefs.getBoolean("system_clockleadingzero")) {
                    XposedHelpers.setAdditionalInstanceField(clock, "showLeadingZero", true);
                }
                if (clock.getId() == clockId && MainModule.mPrefs.getBoolean("system_clock_show_ampm")) {
                    XposedHelpers.setAdditionalInstanceField(clock, "showAmPm", true);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, "updateTime", new MethodHook(XCallback.PRIORITY_HIGHEST) {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                TextView clock = (TextView)param.thisObject;
                boolean clockShowSeconds = XposedHelpers.getAdditionalInstanceField(clock, "showSeconds") != null;
                boolean clockShowLeadingZero = XposedHelpers.getAdditionalInstanceField(clock, "showLeadingZero") != null;
                boolean clockShowAmPm = XposedHelpers.getAdditionalInstanceField(clock, "showAmPm") != null;
                if (clockShowSeconds || clockShowLeadingZero) {
                    Context mContext = clock.getContext();
                    Object mMiuiStatusBarClockController = XposedHelpers.getObjectField(clock, "mMiuiStatusBarClockController");
                    int mAmPmStyle = (int) XposedHelpers.getObjectField(clock, "mAmPmStyle");
                    boolean is24 = (boolean) XposedHelpers.callMethod(mMiuiStatusBarClockController, "getIs24");
                    Object mCalendar = XposedHelpers.callMethod(mMiuiStatusBarClockController, "getCalendar");
                    String fmt;
                    if (clockShowSeconds) {
                        if (is24) {
                            fmt = "fmt_time_24hour_minute_second";
                        }
                        else {
                            if (mAmPmStyle == 0) {
                                fmt = "fmt_time_12hour_minute_second_pm";
                            }
                            else {
                                fmt = "fmt_time_12hour_minute_second";
                            }
                        }
                    }
                    else {
                        if (is24) {
                            fmt = "fmt_time_24hour_minute";
                        }
                        else {
                            if (mAmPmStyle == 0) {
                                fmt = "fmt_time_12hour_minute_pm";
                            }
                            else {
                                fmt = "fmt_time_12hour_minute";
                            }
                        }
                    }
                    int fmtResId = mContext.getResources().getIdentifier(fmt, "string", "com.android.systemui");
                    String timeFmt = mContext.getString(fmtResId);
                    if (clockShowSeconds) {
                        timeFmt = timeFmt.replaceFirst("mm:ms", "mm:ss").replaceFirst("mm:s$", "mm:ss");
                    }
                    if (clockShowLeadingZero) {
                        timeFmt = timeFmt.replaceFirst("^H:mm", "HH:mm").replaceFirst("^h:mm", "hh:mm")
                            .replaceFirst("ah:mm", "ahh:mm").replaceFirst(" h:mm", " hh:mm");
                    }
                    if (clockShowAmPm) {
                        timeFmt = "aa" + timeFmt;
                    }
                    StringBuilder formatSb = new StringBuilder(timeFmt);
                    StringBuilder textSb = new StringBuilder();
                    XposedHelpers.callMethod(mCalendar, "format", mContext, textSb, formatSb);

                    if (XposedHelpers.getAdditionalInstanceField(clock, "mFixedWidth") != null) {
                        Object mTextLength = XposedHelpers.getAdditionalInstanceField(clock, "mTextLength");
                        Object mHourText = XposedHelpers.getAdditionalInstanceField(clock, "mHourText");
                        int len = textSb.toString().length();
                        String hour = textSb.toString().replaceFirst(".*?(\\d+):\\d+:\\d+.*", "$1");
                        if (
                            mTextLength == null || (int) mTextLength != len
                            || mHourText == null || !hour.equals(mHourText)
                        ) {
                            XposedHelpers.setAdditionalInstanceField(clock, "mTextLength", len);
                            XposedHelpers.setAdditionalInstanceField(clock, "mHourText", hour);
                            String maxLenText = textSb.toString().replaceAll(":\\d+:\\d+", ":88:88");
                            clock.setText(maxLenText);
                            clock.measure(0, 0);
                            ViewGroup.LayoutParams lp = clock.getLayoutParams();
                            float extraWidth = MainModule.mPrefs.getInt("system_clock_extra_width", 2) * 0.5f;
                            lp.width = clock.getMeasuredWidth() + (int)(mContext.getResources().getDisplayMetrics().density * extraWidth);
                            clock.setLayoutParams(lp);
                        }
                    }
                    clock.setText(textSb.toString());
                    param.setResult(null);
                }
            }
        });
    }

    public static void ExpandNotificationsHook(LoadPackageParam lpparam) {
        String feedbackMethod = Helpers.isTPlus() ? "setFeedbackIcon" : "showFeedbackIcon";
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader, feedbackMethod, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean mOnKeyguard = (boolean) XposedHelpers.callMethod(param.thisObject, "isOnKeyguard");
                if (!mOnKeyguard) {
                    Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.thisObject, "getEntry"), "mSbn");
                    String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                    int opt = Integer.parseInt(MainModule.mPrefs.getString("system_expandnotifs", "1"));
                    boolean isSelected = MainModule.mPrefs.getStringSet("system_expandnotifs_apps").contains(pkgName);
                    if (opt == 2 && !isSelected || opt == 3 && isSelected)
                        XposedHelpers.callMethod(param.thisObject, "setSystemExpanded", true);
                }
            }
        });
    }

    public static void ExpandHeadsUpHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader, "setHeadsUp", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean mOnKeyguard = (boolean) XposedHelpers.callMethod(param.thisObject, "isOnKeyguard");
                boolean showHeadsUp = (boolean) param.args[0];
                if (!mOnKeyguard && showHeadsUp) {
                    View notifyRow = (View) param.thisObject;
                    Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.thisObject, "getEntry"), "mSbn");
                    String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                    int opt = Integer.parseInt(MainModule.mPrefs.getString("system_expandheadups", "1"));
                    boolean isSelected = MainModule.mPrefs.getStringSet("system_expandheadups_apps").contains(pkgName);
                    if (opt == 2 && !isSelected || opt == 3 && isSelected) {
                        Runnable expandNotify = new Runnable() {
                            @Override
                            public void run() {
                                XposedHelpers.callMethod(param.thisObject, "expandNotification");
                            }
                        };
                        notifyRow.postDelayed(expandNotify, 60);
                    }
                }
            }
        });
    }

    public static void PopupNotificationsHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "addNotification", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (param.args[0] == null) return;

                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (MainModule.mPrefs.getBoolean("system_popupnotif_fs"))
                    if (Settings.Global.getInt(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.fullscreen", 0) == 1) return;

                Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_popupnotif_apps");
                String pkgName = (String)XposedHelpers.callMethod(param.args[0], "getPackageName");
                if (selectedApps.contains(pkgName)) {
                    Intent expandNotif = new Intent(ACTION_PREFIX + "ExpandNotifications");
                    expandNotif.putExtra("expand_only", true);
                    mContext.sendBroadcast(expandNotif);
                }
            }
        });
    }

    public static void RecentsBlurRatioHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.recents.views.RecentsView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)param.args[0];
                Handler mHandler = new Handler(mContext.getMainLooper());

                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", MainModule.mPrefs.getInt("system_recents_blur", 100));
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_recents_blur", 100) {
                    @Override
                    public void onChange(String name, int defValue) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", Helpers.getSharedIntPref(mContext, name, defValue));
                    }
                };

                XposedHelpers.setFloatField(param.thisObject, "mDefaultScrimAlpha", 0.15f);
                XposedHelpers.setObjectField(param.thisObject, "mBackgroundScrim", new ColorDrawable(Color.argb(38, 0, 0, 0)).mutate());
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView", lpparam.classLoader, "updateBlurRatio", float.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
            }
        });
    }

    public static void DrawerBlurRatioHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.StatusBarWindowManager", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)param.args[0];
                Handler mHandler = new Handler(mContext.getMainLooper());

                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", MainModule.mPrefs.getInt("system_drawer_blur", 100));
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_drawer_blur", 100) {
                    @Override
                    public void onChange(String name, int defValue) {
                        int opt = Helpers.getSharedIntPref(mContext, name, defValue);
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", opt);
                        if (Helpers.is12()) {
                            Object mControlPanelWindowManager = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClass("com.android.systemui.miui.statusbar.phone.ControlPanelWindowManager", lpparam.classLoader));
                            XposedHelpers.setAdditionalInstanceField(mControlPanelWindowManager, "mCustomBlurModifier", opt);
                        }
                    }
                };
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarWindowManager", lpparam.classLoader, "setBlurRatio", float.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
            }
        });

        if (!Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onBlurRatioChanged", float.class, new MethodHook(1000) {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = XposedHelpers.callMethod(param.thisObject, "getAppearFraction");
            }
        })) Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateStatusBarWindowBlur", new MethodHook(100) {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int mStatusBarState = XposedHelpers.getIntField(param.thisObject, "mStatusBarState");
                if (mStatusBarState != 0) return;
                View mThemeBackgroundView = (View)XposedHelpers.getObjectField(param.thisObject, "mThemeBackgroundView");
                if (mThemeBackgroundView != null)
                    mThemeBackgroundView.setAlpha((float)XposedHelpers.callMethod(param.thisObject, "getAppearFraction"));
            }
        });

        Helpers.hookAllConstructors("com.android.systemui.miui.statusbar.phone.ControlPanelWindowManager", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", MainModule.mPrefs.getInt("system_drawer_blur", 100));
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.miui.statusbar.phone.ControlPanelWindowManager", lpparam.classLoader, "applyBlurRatio", float.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
            }
        });
    }

    public static void DrawerThemeBackgroundHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)param.args[0];
                Handler mHandler = new Handler(mContext.getMainLooper());

                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier", MainModule.mPrefs.getInt("system_drawer_opacity", 100));
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_drawer_opacity", 100) {
                    @Override
                    public void onChange(String name, int defValue) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier", Helpers.getSharedIntPref(mContext, name, defValue));
                    }
                };
            }
        });

        if (!Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onBlurRatioChanged", float.class, new MethodHook(100) {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier") / 100f;
            }
        })) Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateStatusBarWindowBlur", new MethodHook(1000) {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int mStatusBarState = XposedHelpers.getIntField(param.thisObject, "mStatusBarState");
                if (mStatusBarState != 0) return;
                View mThemeBackgroundView = (View)XposedHelpers.getObjectField(param.thisObject, "mThemeBackgroundView");
                if (mThemeBackgroundView != null)
                    mThemeBackgroundView.setAlpha(mThemeBackgroundView.getAlpha() * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier") / 100f);
            }
        });
    }

    public static void HideNetworkTypeHook(LoadPackageParam lpparam) {
        MethodHook hideMobileActivity = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_mobiletypeicon", "1"));
                TextView mMobileType = (TextView) XposedHelpers.getObjectField(param.thisObject, "mMobileType");
                TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.thisObject, "mMobileTypeSingle");
                boolean isMobileConnected = false;
                if (opt == 2) {
                    isMobileConnected = (boolean) XposedHelpers.getObjectField(param.args[0], "dataConnected");
                }
                if (opt == 3 || (opt == 2 && !isMobileConnected)) {
                    mMobileTypeSingle.setVisibility(View.GONE);
                    mMobileType.setVisibility(View.GONE);
                }
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", hideMobileActivity);
    }

    public static void TrafficSpeedSpacingHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                TextView meter = (TextView)param.thisObject;
                if (meter == null) return;
                if (meter.getTag() == null || !"slot_text_icon".equals(meter.getTag())) {
                    int margin = Math.round(meter.getResources().getDisplayMetrics().density * 4);
                    meter.setPaddingRelative(margin, 0, margin, 0);
                }
            }
        });
    }

    public static void ChargeAnimationHook(LoadPackageParam lpparam) {
        Class<?> ccCls;
        try {
            ccCls = XposedHelpers.findClass("com.android.keyguard.charge.MiuiWirelessChargeController", lpparam.classLoader);
        } catch (Throwable t1) {
            try {
                ccCls = XposedHelpers.findClass("com.android.keyguard.charge.MiuiChargeController", lpparam.classLoader);
            } catch (Throwable t2) {
                XposedBridge.log(t1);
                XposedBridge.log(t2);
                return;
            }
        }

        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently(ccCls, "showWirelessChargeAnimation", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                Runnable mScreenOffRunnable = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mScreenOffRunnable");
                WakeLock mScreenOnWakeLock = (WakeLock)XposedHelpers.getObjectField(param.thisObject, "mScreenOnWakeLock");

                if (mContext != null && mHandler != null && mScreenOffRunnable != null && mScreenOnWakeLock != null) {
                    if (mScreenOnWakeLock.isHeld()) {
                        int timeout = Helpers.getSharedIntPref(mContext, "pref_key_system_chargeanimtime", 20) * 1000;
                        mHandler.postDelayed(mScreenOnWakeLock::release, timeout);
                        mHandler.removeCallbacks(mScreenOffRunnable);
                        mHandler.postDelayed(mScreenOffRunnable, timeout);
                    }
                } else Helpers.log("ChargeAnimationHook1", "Something is NULL! :)");
            }
        });

        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently(ccCls, "showRapidChargeAnimation", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                Runnable mScreenOffRunnable = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mScreenOffRunnable");
                WakeLock mScreenOnWakeLock = (WakeLock)XposedHelpers.getObjectField(param.thisObject, "mScreenOnWakeLock");

                if (mContext != null && mHandler != null && mScreenOffRunnable != null && mScreenOnWakeLock != null) {
                    if (mScreenOnWakeLock.isHeld()) {
                        int timeout = Helpers.getSharedIntPref(mContext, "pref_key_system_chargeanimtime", 20) * 1000;
                        mHandler.postDelayed(mScreenOnWakeLock::release, timeout);
                        mHandler.removeCallbacks(mScreenOffRunnable);
                        mHandler.postDelayed(mScreenOffRunnable, timeout);
                    }
                } else Helpers.log("ChargeAnimationHook2", "Something is NULL! :)");
            }
        });

        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently(ccCls, "showWirelessRapidChargeAnimation", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                Runnable mScreenOffRunnable = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mScreenOffRunnable");
                WakeLock mScreenOnWakeLock = (WakeLock)XposedHelpers.getObjectField(param.thisObject, "mScreenOnWakeLock");

                if (mContext != null && mHandler != null && mScreenOffRunnable != null && mScreenOnWakeLock != null) {
                    if (mScreenOnWakeLock.isHeld()) {
                        int timeout = Helpers.getSharedIntPref(mContext, "pref_key_system_chargeanimtime", 20) * 1000;
                        mHandler.postDelayed(mScreenOnWakeLock::release, timeout);
                        mHandler.removeCallbacks(mScreenOffRunnable);
                        mHandler.postDelayed(mScreenOffRunnable, timeout);
                    }
                } else Helpers.log("ChargeAnimationHook3", "Something is NULL! :)");
            }
        });
    }

    public static void VolumeStepsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "createStreamStates", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) {
                Class<?> audioCls = findClass("com.android.server.audio.AudioService", lpparam.classLoader);
                int[] maxStreamVolume = (int[])XposedHelpers.getStaticObjectField(audioCls, "MAX_STREAM_VOLUME");
                int mult = MainModule.mPrefs.getInt("system_volumesteps", 0);
                if (mult <= 0) return;
                for (int i = 0; i < maxStreamVolume.length; i++)
                    maxStreamVolume[i] = Math.round(maxStreamVolume[i] * mult / 100.0f);
                XposedHelpers.setStaticObjectField(audioCls, "MAX_STREAM_VOLUME", maxStreamVolume);
            }
        });
    }

    private static float mMaximumBacklight;
    private static float mMinimumBacklight;
    private static int backlightMaxLevel;
    private static float constrainValue(float val) {
        if (val < 0) val = 0;
        if (val > 1) val = 1;

        boolean limitmin = MainModule.mPrefs.getBoolean("system_autobrightness_limitmin");
        boolean limitmax = MainModule.mPrefs.getBoolean("system_autobrightness_limitmax");
        int min_pct = MainModule.mPrefs.getInt("system_autobrightness_min", 25);
        int max_pct = MainModule.mPrefs.getInt("system_autobrightness_max", 75);

        float min, max;
        min = Helpers.convertGammaToLinearFloat(min_pct / 100f * backlightMaxLevel, backlightMaxLevel, mMinimumBacklight, mMaximumBacklight);
        max = Helpers.convertGammaToLinearFloat(max_pct / 100f * backlightMaxLevel, backlightMaxLevel, mMinimumBacklight, mMaximumBacklight);

        if (limitmin && val < min) val = min;
        if (limitmax && val > max) val = max;
        return val;
    }

    public static void AutoBrightnessRangeHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, "clampScreenBrightness", float.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                float val = (float)param.getResult();
                if (val >= 0) {
                    float res = constrainValue(val);
                    param.setResult(res);
                }
            }
        });

        Helpers.hookAllConstructors("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
            XposedHelpers.setLongField(param.thisObject, "mBrighteningLightDebounceConfig", 1000L);
            XposedHelpers.setLongField(param.thisObject, "mDarkeningLightDebounceConfig", 1200L);
            }
        });

        Helpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "clampScreenBrightness", float.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                float val = (float)param.getResult();
                if (val >= 0) {
                    float res = constrainValue(val);
                    param.setResult(res);
                }
            }
        });

        Helpers.hookAllConstructors("com.android.server.display.DisplayPowerController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Resources res = Resources.getSystem();
                int minBrightnessLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"));
                int maxBrightnessLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android"));
                int backlightBit = res.getInteger(res.getIdentifier("config_backlightBit", "integer", "android.miui"));
                backlightMaxLevel = (1 << backlightBit) - 1;
                mMinimumBacklight = (minBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1);
                mMaximumBacklight = (maxBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1);
            }
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String type = uri.getPathSegments().get(1);
                            String key = uri.getPathSegments().get(2);
                            if (key.contains("pref_key_system_autobrightness_")) {
                                switch (type) {
                                    case "integer":
                                        int defVal = "pref_key_system_autobrightness_min".equals(key) ? 25 : 75;
                                        MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, defVal));
                                        break;
                                    case "boolean":
                                        MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(mContext, key, false));
                                        break;
                                }
                            }
                        } catch (Throwable t) {
                            Helpers.log(t);
                        }
                    }
                };
            }
        });
    }

    private static long measureTime = 0;
    private static long txBytesTotal = 0;
    private static long rxBytesTotal = 0;
    private static long txSpeed = 0;
    private static long rxSpeed = 0;

    private static Pair<Long, Long> getTrafficBytes(Object thisObject) {
        long tx = -1L;
        long rx = -1L;

        try {
            for (Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces(); list.hasMoreElements();) {
                NetworkInterface iface = list.nextElement();
                if (iface.isUp() && !iface.isVirtual() && !iface.isLoopback() && !iface.isPointToPoint() && !"".equals(iface.getName())) {
                    tx += (long)XposedHelpers.callStaticMethod(TrafficStats.class, "getTxBytes", iface.getName());
                    rx += (long)XposedHelpers.callStaticMethod(TrafficStats.class, "getRxBytes", iface.getName());
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
            tx = TrafficStats.getTotalTxBytes();
            rx = TrafficStats.getTotalRxBytes();
        }

        return new Pair<Long, Long>(tx, rx);
    }

    @SuppressLint("DefaultLocale")
    private static String humanReadableByteCount(Context ctx, long bytes) {
        try {
            Resources modRes = Helpers.getModuleRes(ctx);
            boolean hideSecUnit = MainModule.mPrefs.getBoolean("system_detailednetspeed_secunit");
            String unitSuffix = modRes.getString(R.string.Bs);
            if (hideSecUnit) {
                unitSuffix = "";
            }
            if (bytes < 1024) return bytes + " " + (hideSecUnit ? "B" : unitSuffix);
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            char pre = modRes.getString(R.string.speedunits).charAt(exp-1);
            DecimalFormat df = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            df.setMinimumFractionDigits(0);
            df.setMaximumFractionDigits(1);
            return df.format(bytes / Math.pow(1024, exp)) + " " + String.format("%s" + unitSuffix, pre);
        } catch (Throwable t) {
            XposedBridge.log(t);
            return "";
        }
    }

    public static void NetSpeedIntervalHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, "postUpdateNetworkSpeedDelay", long.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                long originInterval = (long) param.args[0];
                if (originInterval == 4000L) {
                    long newInterval = MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000;
                    param.args[0] = newInterval;
                }
            }
        });
    }

    public static void DetailedNetSpeedHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                TextView meter = (TextView)param.thisObject;
                if (meter.getTag() == null || !"slot_text_icon".equals(meter.getTag())) {
                    float density = meter.getResources().getDisplayMetrics().density;
                    int font = Integer.parseInt(MainModule.mPrefs.getString("system_detailednetspeed_font", "3"));
                    float size = 8.0f;
                    float spacing = 0.9f;
                    int top = 0;
                    switch (font) {
                        case 1: size = 9f; spacing = 0.85f; top = Math.round(density);break;
                        case 2: size = 8.5f; break;
                        case 4: size = 7.5f; break;
                    }
                    meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                    meter.setSingleLine(false);
                    meter.setLines(2);
                    meter.setMaxLines(2);
                    meter.setLineSpacing(0, spacing);
                    meter.setPadding(meter.getPaddingLeft(), meter.getPaddingTop() - top, meter.getPaddingRight(), meter.getPaddingBottom());
                }
            }
        });
        boolean reduceVis = MainModule.mPrefs.getBoolean("system_detailednetspeed_zero");
        if (reduceVis) {
            Helpers.hookAllMethods("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader, "applyNetworkSpeedState", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    TextView meter = (TextView) param.thisObject;
                    boolean isZero = rxSpeed == 0 && txSpeed == 0;
                    meter.setAlpha(isZero ? 0.3f : 1.0f);
                }
            });
        }

        Class<?> nscCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader);
        if (nscCls == null) {
            Helpers.log("DetailedNetSpeedHook", "No NetworkSpeed view or controller");
            return;
        }

        Helpers.findAndHookMethod(nscCls, "getTotalByte", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Pair<Long, Long> bytes = getTrafficBytes(param.thisObject);
                txBytesTotal = bytes.first;
                rxBytesTotal = bytes.second;
                measureTime = nanoTime();
            }
        });

        Helpers.findAndHookMethod(nscCls, "updateNetworkSpeed", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                boolean isConnected = false;
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network nw = mConnectivityManager.getActiveNetwork();
                if (nw != null) {
                    NetworkCapabilities capabilities = mConnectivityManager.getNetworkCapabilities(nw);
                    if (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
                        isConnected = true;
                    }
                }
                if (isConnected) {
                    long nanoTime = nanoTime();
                    long newTime = nanoTime - measureTime;
                    measureTime = nanoTime;
                    if (newTime == 0) newTime = Math.round(4 * Math.pow(10, 9));
                    Pair<Long, Long> bytes = getTrafficBytes(param.thisObject);
                    long newTxBytes = bytes.first;
                    long newRxBytes = bytes.second;
                    long newTxBytesFixed = newTxBytes - txBytesTotal;
                    long newRxBytesFixed = newRxBytes - rxBytesTotal;
                    if (newTxBytesFixed < 0 || txBytesTotal == 0) newTxBytesFixed = 0;
                    if (newRxBytesFixed < 0 || rxBytesTotal == 0) newRxBytesFixed = 0;
                    txSpeed = Math.round(newTxBytesFixed / (newTime / Math.pow(10, 9)));
                    rxSpeed = Math.round(newRxBytesFixed / (newTime / Math.pow(10, 9)));
                    txBytesTotal = newTxBytes;
                    rxBytesTotal = newRxBytes;
                } else {
                    txSpeed = 0;
                    rxSpeed = 0;
                }
            }
        });

        Helpers.findAndHookMethod(nscCls, "updateText", String.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                boolean hideLow = MainModule.mPrefs.getBoolean("system_detailednetspeed_low");
                int lowLevel = MainModule.mPrefs.getInt("system_detailednetspeed_lowlevel", 1) * 1024;
                int icons = Integer.parseInt(MainModule.mPrefs.getString("system_detailednetspeed_icon", "2"));

                String txarrow = "";
                String rxarrow = "";
                if (icons == 2) {
                    txarrow = txSpeed < lowLevel ? "△" : "▲";
                    rxarrow = rxSpeed < lowLevel ? "▽" : "▼";
                } else if (icons == 3) {
                    txarrow = txSpeed < lowLevel ? " ☖" : " ☗";
                    rxarrow = rxSpeed < lowLevel ? " ⛉" : " ⛊";
                }

                String tx = hideLow && txSpeed < lowLevel ? "" : humanReadableByteCount(mContext, txSpeed) + txarrow;
                String rx = hideLow && rxSpeed < lowLevel ? "" : humanReadableByteCount(mContext, rxSpeed) + rxarrow;
                param.args[0] = tx + "\n" + rx;
            }
        });
    }

    private static Bitmap processAlbumArt(Context context, Bitmap bitmap) {
        if (context == null || bitmap == null) return bitmap;
        int rescale = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_albumartonlock_scale", "1"));
        boolean grayscale = Helpers.getSharedBoolPref(context, "pref_key_system_albumartonlock_gray", false);
        if (rescale == 1 && !grayscale) return bitmap;

        Paint paint = new Paint();
        Matrix transformation = new Matrix();
        int width = 0;
        int height = 0;

        if (grayscale) {
            width = bitmap.getWidth();
            height = bitmap.getHeight();

            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        }

        if (rescale != 1) {
            Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            width = point.x;
            height = point.y;

            float originalWidth = bitmap.getWidth();
            float originalHeight = bitmap.getHeight();
            float scale = rescale == 2 ? Math.min(width / originalWidth, height / originalHeight) : Math.max(width / originalWidth, height / originalHeight);
            float xTranslation = (width - originalWidth * scale) / 2.0f;
            float yTranslation = (height - originalHeight * scale) / 2.0f;

            transformation.postTranslate(xTranslation, yTranslation);
            transformation.preScale(scale, scale);

            paint.setFilterBitmap(true);
        }

        Bitmap processed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(processed);
        canvas.drawBitmap(bitmap, transformation, paint);
        return processed;
    }

    public static void LockScreenAlbumArtHook(LoadPackageParam lpparam) {
        Class<?> MiuiThemeUtilsClass = XposedHelpers.findClassIfExists("com.android.keyguard.utils.MiuiKeyguardUtils", lpparam.classLoader);

        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (isDefaultLockScreenTheme) {
                    Object mBlurRatioChangedListener = XposedHelpers.getObjectField(param.thisObject, "mBlurRatioChangedListener");
                    Object notificationShadeDepthController = XposedHelpers.getObjectField(param.thisObject, "notificationShadeDepthController");
                    XposedHelpers.callMethod(notificationShadeDepthController, "removeListener", mBlurRatioChangedListener);
                    View view = (View) XposedHelpers.getObjectField(param.thisObject, "mThemeBackgroundView");
                    view.setAlpha(1.0f);
                }
            }
        });
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, "updateThemeBackground", new MethodHook() {
            private boolean isListened = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (!isDefaultLockScreenTheme) {
                    return ;
                }
                View view = (View) XposedHelpers.getObjectField(param.thisObject, "mThemeBackgroundView");
                if (!isListened) {
                    isListened = true;
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART");
                    view.getContext().registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (action == null) return;
                            if (action.equals(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART")) {
                                XposedHelpers.callMethod(param.thisObject, "updateThemeBackground");
                            }
                        }
                    }, intentFilter);
                }
                boolean isOnShade = (boolean) XposedHelpers.callMethod(param.thisObject, "isOnShade");
                if (isOnShade) {
                    view.setVisibility(View.GONE);
                }
                else {
                    Object mAlbumArt = XposedHelpers.getAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt");
                    if (mAlbumArt != null) {
                        view.setBackground(new BitmapDrawable(view.getContext().getResources(), (Bitmap) mAlbumArt));
                    }
                    view.setVisibility(mAlbumArt != null ? View.VISIBLE : View.GONE);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                if (!isDefaultLockScreenTheme) {
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null);
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null);
                    return;
                }
                MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(param.thisObject, "mMediaMetadata");
                Bitmap art = null;
                if (mMediaMetadata != null) {
                    art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
                }
                Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource");
                try {
                    if (art == null && mAlbumArt == null) return;
                    if (art != null && art.sameAs(mAlbumArt)) return;
                } catch (Throwable ignore) {}
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", art);

                int blur = Helpers.getSharedIntPref(mContext, "pref_key_system_albumartonlock_blur", 0);
                Bitmap blurArt = processAlbumArt(mContext, art != null && blur > 0 ? Helpers.fastBlur(art, blur + 1) : art);
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", blurArt);

                Intent updateAlbumWallpaper = new Intent(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART");
                updateAlbumWallpaper.setPackage("com.android.systemui");
                mContext.sendBroadcast(updateAlbumWallpaper);

                if (blurArt != null) {
                    Intent updateFakeWallpaper = new Intent("miui.intent.action.LOCK_WALLPAPER_CHANGED");
                    updateFakeWallpaper.setPackage("com.android.systemui");
                    WallpaperColors fromBitmap = WallpaperColors.fromBitmap(blurArt);
                    boolean isWallpaperColorLight = (fromBitmap.getColorHints() & 1) == 1;
                    updateFakeWallpaper.putExtra("is_wallpaper_color_light", isWallpaperColorLight);
                    mContext.sendBroadcast(updateFakeWallpaper);
                }
            }
        });
        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "clearCurrentMediaNotification", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                boolean isDefaultLockScreenTheme = (boolean) XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultLockScreenTheme");
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null);
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null);
                if (isDefaultLockScreenTheme) {
                    Intent updateAlbumWallpaper = new Intent(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART");
                    updateAlbumWallpaper.setPackage("com.android.systemui");
                    mContext.sendBroadcast(updateAlbumWallpaper);
                }
            }
        });
    }

    public static void BetterPopupsHideDelaySysHook() {
        Helpers.findAndHookMethod(MiuiNotification.class, "getFloatTime", XC_MethodReplacement.returnConstant(0));
    }

    public static void BetterPopupsHideDelayHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

                int delay = MainModule.mPrefs.getInt("system_betterpopups_delay", 0) * 1000;
                if (delay == 0) delay = 5000;
                XposedHelpers.setIntField(param.thisObject, "mMinimumDisplayTime", delay);
                XposedHelpers.setIntField(param.thisObject, "mHeadsUpNotificationDecay", delay);
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_betterpopups_delay", 0) {
                    @Override
                    public void onChange(String name, int defValue) {
                        int delay = Helpers.getSharedIntPref(mContext, name, defValue) * 1000;
                        if (delay == 0) delay = 5000;
                        XposedHelpers.setIntField(param.thisObject, "mMinimumDisplayTime", delay);
                        XposedHelpers.setIntField(param.thisObject, "mHeadsUpNotificationDecay", delay);
                    }
                };
            }
        });
    }

    public static void BetterPopupsNoHideHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "removeHeadsUpNotification", XC_MethodReplacement.DO_NOTHING);
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "removeOldHeadsUpNotification", XC_MethodReplacement.DO_NOTHING);

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager$HeadsUpEntry", lpparam.classLoader, "updateEntry", boolean.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                XposedHelpers.setObjectField(param.thisObject, "mRemoveHeadsUpRunnable", new Runnable() {
                    @Override
                    public void run() {}
                });
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "onExpandingFinished", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                XposedHelpers.setBooleanField(param.thisObject, "mReleaseOnExpandFinish", true);
            }
        });

//		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "onReorderingAllowed", new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				Helpers.log("BetterPopupsNoHideHook", "onReorderingAllowed");
//			}
//		});
    }

    public static void BetterPopupsSwipeDownHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpTouchHelper", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
            @SuppressWarnings("ConstantConditions")
            protected void after(final MethodHookParam param) throws Throwable {
                MotionEvent me = (MotionEvent)param.args[0];
                if (me.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    boolean mAllowSwipingDown = true;
                    try {
                        Object mPickedChild = XposedHelpers.getObjectField(param.thisObject, "mPickedChild");
                        if (mPickedChild != null) {
                            View mMiniBar = (View)XposedHelpers.callMethod(mPickedChild, "getMiniWindowBar");
                            if (mMiniBar != null && mMiniBar.getVisibility() == View.VISIBLE) mAllowSwipingDown = false;
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(t);
                    }
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mAllowSwipingDown", mAllowSwipingDown);
                }
                if (me.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    Object mAllowSwipingDown = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mAllowSwipingDown");
                    if (mAllowSwipingDown instanceof Boolean && !(boolean)mAllowSwipingDown) return;
                    boolean mTouchingHeadsUpView = (boolean)XposedHelpers.getObjectField(param.thisObject, "mTouchingHeadsUpView");
                    if (!mTouchingHeadsUpView) return;
                    float mTouchSlop = (float)XposedHelpers.getObjectField(param.thisObject, "mTouchSlop");
                    float mInitialTouchY = (float)XposedHelpers.getObjectField(param.thisObject, "mInitialTouchY");
                    if (me.getY() - mInitialTouchY > mTouchSlop) {
                        XposedHelpers.setObjectField(param.thisObject, "mTouchingHeadsUpView", false);

                        ViewGroup mStackScroller = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mStackScroller");
                        Context mContext = mStackScroller != null ? mStackScroller.getContext() : null;
                        if (mContext == null) {
                            Helpers.log("BetterPopupsSwipeDownHook", "Cannot get context!");
                            return;
                        }

                        Intent expandNotif = new Intent(ACTION_PREFIX + "ExpandNotifications");
                        expandNotif.putExtra("expand_only", true);
                        mContext.sendBroadcast(expandNotif);
                    }
                }
            }
        });
    }

    public static void RotationAnimationRes() {
        int opt = Integer.parseInt(MainModule.mPrefs.getString("system_rotateanim", "1"));
        int enter = 0;
        int exit = 0;
        if (opt == 2) {
            enter = R.anim.no_enter;
            exit = R.anim.no_exit;
        } else if (opt == 3) {
            enter = R.anim.xfade_enter;
            exit = R.anim.xfade_exit;
        }

        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_0_enter", enter);
        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_0_exit", exit);
        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_180_enter", enter);
        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_180_exit", exit);
        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_minus_90_enter", enter);
        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_minus_90_exit", exit);
        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_plus_90_enter", enter);
        MainModule.resHooks.setResReplacement("android", "anim", "screen_rotate_plus_90_exit", exit);
    }

    public static void RotationAnimationHook(LoadPackageParam lpparam) {
        MethodHook animEnter = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_rotateanim", "1"));
                Animation anim = (Animation)param.getResult();
                if (opt == 2) {
                    anim.setDuration(0);
                    param.setResult(anim);
                } else if (opt == 3) {
                    Animation alphaAnim = new AlphaAnimation(1.0f, 1.0f);
                    alphaAnim.setInterpolator((Interpolator)XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.android.server.wm.AppTransitionInjector", lpparam.classLoader), "QUART_EASE_OUT_INTERPOLATOR"));
                    alphaAnim.setDuration(300);
                    alphaAnim.setFillAfter(true);
                    alphaAnim.setFillBefore(true);
                    alphaAnim.setFillEnabled(true);
                    param.setResult(alphaAnim);
                }
            }
        };

        MethodHook animExit = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_rotateanim", "1"));
                Animation anim = (Animation)param.getResult();
                if (opt == 2) {
                    anim.setDuration(0);
                } else if (opt == 3) {
                    AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
                    alphaAnim.setInterpolator((Interpolator)XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.android.server.wm.AppTransitionInjector", lpparam.classLoader), "QUART_EASE_OUT_INTERPOLATOR"));
                    alphaAnim.setDuration(300);
                    alphaAnim.setFillAfter(true);
                    alphaAnim.setFillBefore(true);
                    alphaAnim.setFillEnabled(true);
                    param.setResult(alphaAnim);
                }
            }
        };

        Helpers.findAndHookMethod("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.classLoader, "createRotation180Enter", animEnter);
        Helpers.findAndHookMethod("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.classLoader, "createRotation180Exit", animExit);
        Helpers.hookAllMethods("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.classLoader, "createRotationEnter", animEnter);
        Helpers.hookAllMethods("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.classLoader, "createRotationEnterWithBackColor", animEnter);
        Helpers.hookAllMethods("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.classLoader, "createRotationExit", animExit);
    }

    public static void NoVersionCheckHook(LoadPackageParam lpparam) {
        String PMSCls = Helpers.isTPlus() ? "com.android.server.pm.PackageManagerServiceUtils" : "com.android.server.pm.PackageManagerService";
        Helpers.hookAllMethods(PMSCls, lpparam.classLoader, "checkDowngrade", XC_MethodReplacement.DO_NOTHING);
    }

    public static void ColorizedNotificationTitlesHook() {
        Helpers.hookAllMethods("android.app.Notification.Builder", null, "bindHeaderAppName", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                try {
                    Object mN = XposedHelpers.getObjectField(param.thisObject, "mN");
                    if (mN != null) {
                        if ((boolean)XposedHelpers.callMethod(mN, "isColorized")) return;
                    }
                } catch (Throwable ignore) {}

                RemoteViews rv = (RemoteViews)param.args[0];
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (rv != null && mContext != null) {
                    int contrastColor = (int)XposedHelpers.callMethod(param.thisObject, "getPrimaryAccentColor", param.args[1]);
                    rv.setTextColor(mContext.getResources().getIdentifier("app_name_text", "id", "android"), contrastColor);
                }
            }
        });
    }

    public static int abHeight = 39;

    public static void CompactNotificationsRes() {
        MainModule.resHooks.setDensityReplacement("android", "dimen", "notification_action_height", abHeight);
        MainModule.resHooks.setDensityReplacement("android", "dimen", "android_notification_action_height", abHeight);
        MainModule.resHooks.setDensityReplacement("android", "dimen", "notification_action_list_height", abHeight);
        MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_row_extra_padding", 0);
    }

    public static void CompactNotificationsHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper", lpparam.classLoader, "wrap", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (param.args.length > 3) return;
                Object res = param.getResult();
                if (res == null) return;
                View mView = (View)XposedHelpers.getObjectField(res, "mView");
                if (mView == null) return;
                //if (mView.getId() != mView.getResources().getIdentifier("status_bar_latest_event_content", "id", "android")) return;
                FrameLayout container = mView.findViewById(mView.getResources().getIdentifier("actions_container", "id", "android"));
                if (container == null) return;
                float density = mView.getResources().getDisplayMetrics().density;
                int height = Math.round(density * abHeight);
                ViewGroup actions = (ViewGroup)container.getChildAt(0);
                FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams)actions.getLayoutParams();
                lp1.height = height;
                actions.setLayoutParams(lp1);
                actions.setPadding(0, 0, 0, 0);
                for (int c = 0; c < actions.getChildCount(); c++) {
                    View button = actions.getChildAt(c);
                    ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams)button.getLayoutParams();
                    lp2.height = height;
                    lp2.bottomMargin = 0;
                    lp2.topMargin = 0;
                }
            }
        });

//        MethodHook hook = new MethodHook() {
//            @Override
//            protected void after(MethodHookParam param) throws Throwable {
//                View view = (View)param.args[0];
//                FrameLayout container = view.findViewById(view.getResources().getIdentifier("actions_container", "id", "android"));
//                if (container == null || container.getVisibility() != View.VISIBLE) return;
//                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
//                if ("MiuiStyleProcessor".equals(param.thisObject.getClass().getSimpleName())) {
//                    float density = container.getResources().getDisplayMetrics().density;
//                    lp.bottomMargin = Math.round(lp.bottomMargin * 0.666f);
//                    ViewGroup.LayoutParams lpc = container.getLayoutParams();
//                    lpc.height = Math.round(density * abHeight);
//                    container.setLayoutParams(lpc);
//                } else {
//                    lp.bottomMargin = 0;
//                }
//                view.setLayoutParams(lp);
//            }
//        };
//        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$GoogleStyleProcessor", lpparam.classLoader, "setGoogleContentMargins", View.class, hook);
//        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$MiuiStyleProcessor", lpparam.classLoader, "setMiuiContentMargins", View.class, hook);
    }

    public static void HideFromRecentsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.wm.Task", lpparam.classLoader, "setIntent", Intent.class, ActivityInfo.class, new MethodHook() {
            @Override
            @SuppressLint("WrongConstant")
            protected void after(final MethodHookParam param) throws Throwable {
            String pkgName = null;
            Intent newIntent = (Intent) param.args[0];
            ActivityInfo activityInfo = (ActivityInfo) param.args[1];

            if (newIntent != null && newIntent.getComponent() != null) {
                pkgName = newIntent.getComponent().getPackageName();
            }
            if (pkgName == null && activityInfo != null) {
                pkgName = activityInfo.packageName;
            }
            if (pkgName != null) {
                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_hidefromrecents_apps");
                if (selectedApps.contains(pkgName)) {
                    Intent intent = (Intent)XposedHelpers.getObjectField(param.thisObject, "intent");
                    if (intent != null) intent.addFlags(8388608);
                }
            }
            }
        });
    }

    private static final List<String> hookedTiles = new ArrayList<String>();

    @SuppressLint("MissingPermission")
    public static void QSHapticHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSFactoryImpl", lpparam.classLoader, "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object tile = param.getResult();
                if (tile == null) return;
                String tileClass = tile.getClass().getCanonicalName();
                if (!hookedTiles.contains(tileClass)) {
                    Helpers.hookAllMethods(tileClass, lpparam.classLoader, "handleClick", new MethodHook(20) {
                        @Override
                        protected void after(final MethodHookParam param) throws Throwable {
                            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                            boolean ignoreSystem = Helpers.getSharedBoolPref(mContext, "pref_key_system_qshaptics_ignore", false);
                            int opt = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_qshaptics", "1"));
                            if (opt == 2)
                                Helpers.performLightVibration(mContext, ignoreSystem);
                            else if (opt == 3)
                                Helpers.performStrongVibration(mContext, ignoreSystem);
                        }
                    });
                    hookedTiles.add(tileClass);
                }
            }
        });
    }

    public static void HideCCOperatorHook(LoadPackageParam lpparam) {
        MethodHook hideOperatorHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object carrierView;
                TextView mCarrierText;
                try {
                    carrierView = XposedHelpers.getObjectField(param.thisObject, "carrierText");
                }
                catch (Throwable e) {
                    carrierView = XposedHelpers.getObjectField(param.thisObject, "mCarrierText");
                }
                mCarrierText = (TextView) carrierView;
                mCarrierText.setVisibility(View.GONE);
            }
        };

        boolean hookedFlaresInfo = Helpers.hookAllMethodsSilently("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", lpparam.classLoader, "updateFlaresInfo", hideOperatorHook);
        if (!hookedFlaresInfo) {
            Helpers.findAndHookMethod("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", lpparam.classLoader, "onFinishInflate", hideOperatorHook);
        }

        Helpers.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.classLoader, "updateCarrierTextVisibility", hideOperatorHook);

        hookedFlaresInfo = Helpers.hookAllMethodsSilently("com.android.systemui.qs.MiuiQSHeaderView", lpparam.classLoader, "updateFlaresInfo", hideOperatorHook);
        if (!hookedFlaresInfo) {
            Helpers.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.classLoader, "onFinishInflate", hideOperatorHook);
        }
    }

    public static void CollapseCCAfterClickHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader, "click", View.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mState = XposedHelpers.callMethod(param.thisObject, "getState");
                int state = XposedHelpers.getIntField(mState, "state");
                if (state != 0) {
                    String tileSpec = (String) XposedHelpers.callMethod(param.thisObject, "getTileSpec");
                    if (!"edit".equals(tileSpec)) {
                        Object mHost = XposedHelpers.getObjectField(param.thisObject, "mHost");
                        XposedHelpers.callMethod(mHost, "collapsePanels");
                    }
                }
            }
        });
    }

    public static void DisableBluetoothRestrictHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.settingslib.bluetooth.LocalBluetoothAdapter", lpparam.classLoader, "isSupportBluetoothRestrict", Context.class, XC_MethodReplacement.returnConstant(false));
    }

    public static void AutoGroupNotificationsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.classLoader, "adjustAutogroupingSummary", int.class, String.class, String.class, boolean.class, new MethodHook() {
            @Override
            @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
            protected void before(final MethodHookParam param) throws Throwable {
            int opt = Integer.parseInt(MainModule.mPrefs.getString("system_autogroupnotif", "1"));
            if (opt == 2) {
                param.setResult(null);
                return;
            }
            Map<Integer, Map<String, LinkedHashSet<String>>> mUngroupedNotifications = (Map<Integer, Map<String, LinkedHashSet<String>>>)XposedHelpers.getObjectField(param.thisObject, "mUngroupedNotifications");
            Map<String, LinkedHashSet<String>> obj = mUngroupedNotifications.get(param.args[0]);
            if (obj != null) {
                LinkedHashSet<String> list = obj.get(param.args[1]);
                if (list != null && list.size() < opt) param.setResult(null);
            }
            }
        });

        Helpers.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.classLoader, "adjustNotificationBundling", List.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
            List<?> list = (List<?>)param.args[0];
            int opt = Integer.parseInt(MainModule.mPrefs.getString("system_autogroupnotif", "1"));
            if (opt == 2 || (list != null && list.size() < opt)) param.setResult(null);
            }
        });
    }

    public static void NoMoreIconHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "setIconsVisibility", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object mMoreIcon = XposedHelpers.getObjectField(param.thisObject, "mMoreIcon");
                if (mMoreIcon != null) XposedHelpers.setBooleanField(param.thisObject, "mForceHideMoreIcon", true);
            }
        });
    }

    public static void ShowNotificationsAfterUnlockHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.classLoader, "hasShownAfterUnlock", XC_MethodReplacement.returnConstant(false));
        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.classLoader, "setHasShownAfterUnlock", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                XposedHelpers.setBooleanField(param.thisObject, "mHasShownAfterUnlock", false);
            }
        });
        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.notification.MiuiNotificationCompat", lpparam.classLoader, "isKeptOnKeyguard", Notification.class, XC_MethodReplacement.returnConstant(true));
    }

    private static int appInfoIconResId;
    private static int appInfoDescId;
    private static int forceCloseIconResId;
    private static int forceCloseDescId;
    public static void NotificationRowMenuRes() {
        appInfoIconResId = MainModule.resHooks.addResource("ic_appinfo", R.drawable.ic_appinfo12);
        forceCloseIconResId = MainModule.resHooks.addResource("ic_forceclose", R.drawable.ic_forceclose12);
        appInfoDescId = MainModule.resHooks.addResource("miui_notification_menu_appinfo_title", R.string.system_notifrowmenu_appinfo);
        forceCloseDescId = MainModule.resHooks.addResource("miui_notification_menu_forceclose_title", R.string.system_notifrowmenu_forceclose);
        MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_menu_icon_padding", 0);
    }

    public static void NotificationRowMenuHook(LoadPackageParam lpparam) {
        MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "miui_notification_modal_menu_margin_left_right", 8);

        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.classLoader, "createMenuViews", boolean.class, boolean.class, new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                ArrayList<Object> mMenuItems = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mMenuItems");
                Class<?> nmiCls = findClass("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow.MiuiNotificationMenuItem", lpparam.classLoader);
                Object infoBtn = null;
                Object forceCloseBtn = null;
                Constructor MenuItem = nmiCls.getConstructors()[0];
                try {
                    infoBtn = MenuItem.newInstance(param.thisObject, mContext, appInfoDescId, null, appInfoIconResId);
                    forceCloseBtn = MenuItem.newInstance(param.thisObject, mContext, forceCloseDescId, null, forceCloseIconResId);
                } catch (Throwable t1) {
                    XposedBridge.log(t1);
                }
                if (infoBtn == null || forceCloseBtn == null) return;
                Object notification = XposedHelpers.getObjectField(param.thisObject, "mSbn");
                String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                int menuMargin = (int) XposedHelpers.getObjectField(param.thisObject, "mMenuMargin");
                mMenuItems.add(infoBtn);
                mMenuItems.add(forceCloseBtn);
                XposedHelpers.setObjectField(param.thisObject, "mMenuItems", mMenuItems);
                LinearLayout mMenuContainer = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
                if (mMenuContainer != null) {
                    View mInfoBtn = (View) XposedHelpers.callMethod(infoBtn, "getMenuView");
                    View mForceCloseBtn = (View) XposedHelpers.callMethod(forceCloseBtn, "getMenuView");

                    View.OnClickListener itemClick = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (view == null) return;
                            int uid = (int)XposedHelpers.callMethod(notification, "getAppUid");
                            int user = 0;
                            try {
                                user = (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", uid);
                            } catch (Throwable t) {
                                XposedBridge.log(t);
                            }

                            if (view == mInfoBtn) {
                                Helpers.openAppInfo(mContext, pkgName, user);
                                mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                            } else if (view == mForceCloseBtn) {
                                ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                                if (user != 0)
                                    XposedHelpers.callMethod(am, "forceStopPackageAsUser", pkgName, user);
                                else
                                    XposedHelpers.callMethod(am, "forceStopPackage", pkgName);
                                try {
                                    CharSequence appName = mContext.getPackageManager().getApplicationLabel(mContext.getPackageManager().getApplicationInfo(pkgName, 0));
                                    Toast.makeText(mContext, Helpers.getModuleRes(mContext).getString(R.string.force_closed, appName), Toast.LENGTH_SHORT).show();
                                } catch (Throwable ignore) {}
                            }
                        }
                    };
                    mInfoBtn.setOnClickListener(itemClick);
                    mForceCloseBtn.setOnClickListener(itemClick);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
                    layoutParams.leftMargin = menuMargin;
                    layoutParams.rightMargin = menuMargin;
                    XposedHelpers.callMethod(mMenuContainer, "addView", mInfoBtn, layoutParams);
                    XposedHelpers.callMethod(mMenuContainer, "addView", mForceCloseBtn, layoutParams);
                    int size = mMenuItems.size();
                    int dimensionPixelOffset = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("notification_panel_width", "dimen", lpparam.packageName));
                    if (dimensionPixelOffset <= 0) {
                        dimensionPixelOffset = mContext.getResources().getDisplayMetrics().widthPixels;
                    }
                    int menuWidth = (dimensionPixelOffset / size) - (menuMargin * 2);
                    int titleId = mContext.getResources().getIdentifier("modal_menu_title", "id", lpparam.packageName);
                    mMenuItems.forEach(new Consumer() {
                        @Override
                        public void accept(Object obj) {
                            View menuView = (View) XposedHelpers.callMethod(obj, "getMenuView");
                            ((TextView) menuView.findViewById(titleId)).setMaxWidth(menuWidth);
                        }
                    });
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static boolean checkVibration(String pkgName, Object thisObject) {
        try {
            int opt = (int)XposedHelpers.getAdditionalInstanceField(thisObject, "mVibrationMode");
            Set<String> selectedApps = (Set<String>)XposedHelpers.getAdditionalInstanceField(thisObject, "mVibrationApps");
            boolean isSelected = selectedApps != null && selectedApps.contains(pkgName);
            return opt == 2 && !isSelected || opt == 3 && isSelected;
        } catch (Throwable t) {
            XposedBridge.log(t);
            return false;
        }
    }

    public static void SelectiveVibrationHook(LoadPackageParam lpparam) {
        Helpers.findAndHookConstructor("android.os.SystemVibrator", lpparam.classLoader, Context.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = new Handler(mContext.getMainLooper());

                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVibrationMode", Integer.parseInt(MainModule.mPrefs.getString("system_vibration", "1")));
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_vibration", "1") {
                    @Override
                    public void onChange(String name, String defValue) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVibrationMode", Integer.parseInt(Helpers.getSharedStringPref(mContext, name, defValue)));
                    }
                };

                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVibrationApps", MainModule.mPrefs.getStringSet("system_vibration_apps"));
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_vibration_apps") {
                    @Override
                    public void onChange(String name) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "mVibrationApps", Helpers.getSharedStringSetPref(mContext, name));
                    }
                };
            }
        });

        Helpers.hookAllMethods("android.os.SystemVibrator", lpparam.classLoader, "vibrate", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String pkgName = (String)param.args[1];
                if (pkgName == null) return;
                if (checkVibration(pkgName, param.thisObject)) param.setResult(null);
            }
        });
    }

    public static void QQSGridRes() {
        int cols = MainModule.mPrefs.getInt("system_qqsgridcolumns", 2);
        int colsResId = R.integer.quick_quick_settings_num_rows_5;
        switch (cols) {
            case 3: colsResId = R.integer.quick_quick_settings_num_rows_3; break;
            case 4: colsResId = R.integer.quick_quick_settings_num_rows_4; break;
            case 5: colsResId = R.integer.quick_quick_settings_num_rows_5; break;
            case 6: colsResId = R.integer.quick_quick_settings_num_rows_6; break;
            case 7: colsResId = R.integer.quick_quick_settings_num_rows_7; break;
        }
        MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_qqs_count", colsResId);
    }

    public static void QSGridRes() {
        int cols = MainModule.mPrefs.getInt("system_qsgridcolumns", 2);
        int rows = MainModule.mPrefs.getInt("system_qsgridrows", 1);
        int colsRes = R.integer.quick_settings_num_columns_3;
        int rowsRes = R.integer.quick_settings_num_rows_4;

        switch (cols) {
            case 3: colsRes = R.integer.quick_settings_num_columns_3; break;
            case 4: colsRes = R.integer.quick_settings_num_columns_4; break;
            case 5: colsRes = R.integer.quick_settings_num_columns_5; break;
            case 6: colsRes = R.integer.quick_settings_num_columns_6; break;
            case 7: colsRes = R.integer.quick_settings_num_columns_7; break;
        }

        switch (rows) {
            case 2: rowsRes = R.integer.quick_settings_num_rows_2; break;
            case 3: rowsRes = R.integer.quick_settings_num_rows_3; break;
            case 4: rowsRes = R.integer.quick_settings_num_rows_4; break;
            case 5: rowsRes = R.integer.quick_settings_num_rows_5; break;
        }

        if (cols > 2) MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_num_columns", colsRes);
        if (rows > 1) MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_num_rows", rowsRes);
    }

    private static void updateLabelsVisibility(Object mRecord, int mRows, int orientation) {
        if (mRecord == null) return;
        Object tileView = XposedHelpers.getObjectField(mRecord, "tileView");
        if (tileView != null) {
            ViewGroup mLabelContainer = null;
            try {
                mLabelContainer = (ViewGroup)XposedHelpers.getObjectField(tileView, "mLabelContainer");
            }
            catch (Throwable ignore) {}

            if (mLabelContainer != null) {
                mLabelContainer.setVisibility(
                    MainModule.mPrefs.getBoolean("system_qsnolabels") ||
                        orientation == Configuration.ORIENTATION_PORTRAIT && mRows >= 5 ||
                        orientation == Configuration.ORIENTATION_LANDSCAPE && mRows >= 3 ? View.GONE : View.VISIBLE
                );
            }
        }
    }

    public static void QSGridLabelsHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.qs.MiuiTileLayout", lpparam.classLoader, "addTile", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                updateLabelsVisibility(param.args[0], XposedHelpers.getIntField(param.thisObject, "mRows"), ((ViewGroup)param.thisObject).getResources().getConfiguration().orientation);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.qs.MiuiPagedTileLayout", lpparam.classLoader, "addTile", new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void before(MethodHookParam param) throws Throwable {
                ArrayList<Object> mPages = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mPages");
                if (mPages == null) return;
                int mRows = 0;
                if (mPages.size() > 0) mRows = XposedHelpers.getIntField(mPages.get(0), "mRows");
                updateLabelsVisibility(param.args[0], mRows, ((ViewGroup)param.thisObject).getResources().getConfiguration().orientation);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.qs.MiuiTileLayout", lpparam.classLoader, "updateResources", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getInt("system_qsgridrows", 1) != 2) return;
                if (!(boolean)param.getResult()) return;
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) return;
                XposedHelpers.setIntField(param.thisObject, "mCellHeight", Math.round(XposedHelpers.getIntField(param.thisObject, "mCellHeight") / 1.5f));
                ((ViewGroup)param.thisObject).requestLayout();
            }
        });

        if (MainModule.mPrefs.getInt("system_qsgridrows", 1) == 4)
            Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.MiuiQSTileView", lpparam.classLoader, "createLabel", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    ViewGroup mLabelContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mLabelContainer");
                    if (mLabelContainer != null) mLabelContainer.setPadding(
                            mLabelContainer.getPaddingLeft(),
                            Math.round(mLabelContainer.getResources().getDisplayMetrics().density * 2),
                            mLabelContainer.getPaddingRight(),
                            mLabelContainer.getPaddingBottom()
                    );
                }
            });

        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_cell_height", 85.0f);
                    Class<?> QSController = XposedHelpers.findClassIfExists("miui.systemui.controlcenter.qs.tileview.StandardTileView", pluginLoader);
                    Helpers.hookAllMethods(QSController, "init", new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            if (param.args.length != 1) return;
                            View mLabelContainer = (View)XposedHelpers.getObjectField(param.thisObject, "labelContainer");
                            if (mLabelContainer != null) {
                                mLabelContainer.setVisibility(
                                    MainModule.mPrefs.getBoolean("system_qsnolabels")? View.GONE : View.VISIBLE
                                );
                            }
                        }
                    });
                }
            }
        });
    }

    public static void NoDuckingHook(LoadPackageParam lpparam) {
        //Helpers.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor", lpparam.classLoader, "duckPlayers", XC_MethodReplacement.returnConstant(true));
        //Helpers.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor$DuckingManager", lpparam.classLoader, "addDuck", XC_MethodReplacement.DO_NOTHING);
        Helpers.hookAllMethods("com.android.server.audio.FocusRequester", lpparam.classLoader, "handleFocusLoss", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if ((int)param.args[0] == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) param.setResult(null);
            }
        });
    }

    public static void OrientationLockHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "systemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_qs_autorotate_state", 0) {
                    @Override
                    public void onChange(String name, int defValue) {
                        MainModule.mPrefs.put(name, Helpers.getSharedIntPref(mContext, name, defValue));
                    }
                };
            }
        });

        String windowClass = "com.android.server.wm.DisplayRotation";
        String rotMethod = "rotationForOrientation";
        Helpers.hookAllMethods(windowClass, lpparam.classLoader, rotMethod, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                //Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                //Helpers.log("rotationForOrientationLw: " + param.args[0] + ", " + param.args[1] + " = " + param.getResult());
                if ((int)param.args[0] == -1) {
                    int opt = MainModule.mPrefs.getInt("qs_autorotate_state", 0);
                    int prevOrient = (int)param.args[1];
                    if (opt == 1) {
                        if (prevOrient != 0 && prevOrient != 2) prevOrient = 0;
                        if ((int)param.getResult() == 1 || (int)param.getResult() == 3) param.setResult(prevOrient);
                    } else if (opt == 2) {
                        if (prevOrient != 1 && prevOrient != 3) prevOrient = 1;
                        if ((int)param.getResult() == 0 || (int)param.getResult() == 2) param.setResult(prevOrient);
                    }
                }
            }
        });
    }

    public static void StatusBarHeightRes() {
        int opt = MainModule.mPrefs.getInt("system_statusbarheight", 19);
        int heightDpi = opt == 19 ? 27 : opt;
        MainModule.resHooks.setDensityReplacement("*", "dimen", "status_bar_height", heightDpi);
        MainModule.resHooks.setDensityReplacement("*", "dimen", "status_bar_height_portrait", heightDpi);
        MainModule.resHooks.setDensityReplacement("*", "dimen", "status_bar_height_landscape", heightDpi);
    }

    @SuppressWarnings("ConstantConditions")
    public static void HideMemoryCleanHook(LoadPackageParam lpparam, boolean isInLauncher) {
        String raClass = isInLauncher ? "com.miui.home.recents.views.RecentsContainer" : "com.android.systemui.recents.RecentsActivity";
        if (isInLauncher && findClassIfExists(raClass, lpparam.classLoader) == null) return;
        Helpers.findAndHookMethod(raClass, lpparam.classLoader, "setupVisible", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
            ViewGroup mMemoryAndClearContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mMemoryAndClearContainer");
            if (mMemoryAndClearContainer != null) mMemoryAndClearContainer.setVisibility(View.GONE);
            }
        });
    }

    public static void ExtendedPowerMenuHook(LoadPackageParam lpparam) {
        final boolean[] isListened = {false};
        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isListened[0]) {
                    isListened[0] = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    File powermenu = new File(mContext.getCacheDir(), "extended_power_menu");
                    if (powermenu == null) {
                        Helpers.log("ExtendedPowerMenuHook", "No writable path found!");
                        return;
                    }
                    if (powermenu.exists()) powermenu.delete();

                    InputStream inputStream;
                    FileOutputStream outputStream;
                    byte[] fileBytes;
                    Resources resources = Helpers.getModuleRes(mContext);
                    inputStream = resources.openRawResource(resources.getIdentifier("extended_power_menu", "raw", Helpers.modulePkg));
                    fileBytes = new byte[inputStream.available()];
                    inputStream.read(fileBytes);
                    outputStream = new FileOutputStream(powermenu);
                    outputStream.write(fileBytes);
                    outputStream.close();
                    inputStream.close();

                    if (!powermenu.exists()) {
                        Helpers.log("ExtendedPowerMenuHook", "MAML file not found in cache");
                    }
                    else {
                        Helpers.findAndHookConstructor("com.miui.maml.util.ZipResourceLoader", lpparam.classLoader, String.class, new MethodHook() {
                            @Override
                            protected void before(MethodHookParam param) throws Throwable {
                                String res = (String) param.args[0];
                                if ("/system/media/theme/default/powermenu".equals(res)) {
                                    param.args[0] = powermenu.getPath();
                                }
                            }
                        });
                    }
                }
            }
        });
        Helpers.findAndHookMethod("com.miui.maml.ScreenElementRoot", lpparam.classLoader, "issueExternCommand", String.class, Double.class, String.class, new MethodHook() {
            @Override
            @SuppressLint("MissingPermission")
            protected void before(MethodHookParam param) throws Throwable {
                String cmd = (String)param.args[0];
                Object scrContext = XposedHelpers.getObjectField(param.thisObject, "mContext");
                Context mContext = (Context)XposedHelpers.getObjectField(scrContext, "mContext");
                PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                Object mService = XposedHelpers.getObjectField(pm, "mService");
                Object mSystemExternCommandListener = XposedHelpers.getObjectField(param.thisObject, "mSystemExternCommandListener");

                boolean custom = false;
                if ("recovery".equals(cmd)) {
                    XposedHelpers.callMethod(mService, "reboot", false, "recovery", false);
                    custom = true;
                } else if ("bootloader".equals(cmd)) {
                    XposedHelpers.callMethod(mService, "reboot", false, "bootloader", false);
                    custom = true;
                } else if ("softreboot".equals(cmd)) {
                    mContext.sendBroadcast(new Intent(ACTION_PREFIX + "FastReboot"));
                    custom = true;
                } else if ("killsysui".equals(cmd)) {
                    mContext.sendBroadcast(new Intent(ACTION_PREFIX + "RestartSystemUI"));
                    custom = true;
                } else if ("killlauncher".equals(cmd)) {
                    ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    ResolveInfo launcherInfo = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (launcherInfo != null) {
                        String pkgName = launcherInfo.activityInfo.packageName;
                        if (pkgName != null) XposedHelpers.callMethod(am, "forceStopPackage", pkgName);
                    }
                    custom = true;
                }

                if (custom) {
                    if (mSystemExternCommandListener != null) XposedHelpers.callMethod(mSystemExternCommandListener, "onCommand", param.args[0], param.args[1], param.args[2]);
                    param.setResult(null);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.plugins.PluginEnablerImpl", lpparam.classLoader, "isEnabled", ComponentName.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                ComponentName componentName = (ComponentName) param.args[0];
                if (componentName.getClassName().contains("GlobalActions")) {
                    param.setResult(false);
                }
            }
        });
    }

    public static void HideDismissViewHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, "updateDismissView", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View mDismissView = (View)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
                if (mDismissView != null) {
                    Object mKeyguardShowing = XposedHelpers.getObjectField(param.thisObject, "mKeyguardShowing");
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "currentKeyguard", mKeyguardShowing);
                    XposedHelpers.setObjectField(param.thisObject, "mKeyguardShowing", true);
                }
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                View mDismissView = (View)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
                if (mDismissView != null) {
                    Object mKeyguardShowing = XposedHelpers.getAdditionalInstanceField(param.thisObject, "currentKeyguard");
                    XposedHelpers.setObjectField(param.thisObject, "mKeyguardShowing", mKeyguardShowing);
                }
            }
        });
    }

    public static void ReplaceShortcutAppHook(LoadPackageParam lpparam) {
        MethodHook openAppHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = AndroidAppHelper.currentApplication();
                int user = 0;
                String pkgAppName = "";
                if (param.method.getName().equals("startCalendarApp")) {
                    user = Helpers.getSharedIntPref(mContext, "pref_key_system_calendar_app_user", 0);
                    pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_calendar_app", "");
                }
                else if (param.method.getName().equals("startClockApp")) {
                    user = Helpers.getSharedIntPref(mContext, "pref_key_system_clock_app_user", 0);
                    pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_clock_app", "");
                }
                else if (param.method.getName().equals("startSettingsApp")) {
                    user = Helpers.getSharedIntPref(mContext, "pref_key_system_shortcut_app_user", 0);
                    pkgAppName = Helpers.getSharedStringPref(mContext, "pref_key_system_shortcut_app", "");
                }
                if (pkgAppName != null && !pkgAppName.equals("")) {
                    String[] pkgAppArray = pkgAppName.split("\\|");
                    if (pkgAppArray.length < 2) return;

                    ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.setComponent(name);
                    if (user != 0) {
                        try {
                            Object mStatusBar = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", mContext.getClassLoader()), "get", findClass("com.android.systemui.statusbar.phone.StatusBar", mContext.getClassLoader()));
                            XposedHelpers.callMethod(mStatusBar, "collapsePanels");
                            XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    } else {
                        Object activiyStarter = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", mContext.getClassLoader()), "get", findClass("com.android.systemui.plugins.ActivityStarter", mContext.getClassLoader()));
                        XposedHelpers.callMethod(activiyStarter, "startActivity", intent, true);
                    }
                    param.setResult(null);
                }
            }
        };
        if (!MainModule.mPrefs.getString("system_shortcut_app", "").equals("")) {
            Helpers.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startSettingsApp", openAppHook);
        }
        if (!MainModule.mPrefs.getString("system_calendar_app", "").equals("")) {
            Helpers.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startCalendarApp", Context.class, openAppHook);
        }
        if (!MainModule.mPrefs.getString("system_clock_app", "").equals("")) {
            Helpers.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startClockApp", openAppHook);
        }
    }

    private static final int NOCOLOR = 0x01010101;
    private static int actionBarColor = NOCOLOR;

    private static boolean isIgnored(Context context) {
        return MainModule.mPrefs.getStringSet("system_statusbarcolor_apps").contains(context.getPackageName());
    }

    private static int getActionBarColor(Window window, int oldColor) {
        if (actionBarColor != NOCOLOR) return actionBarColor;

        TypedValue outValue = new TypedValue();
        window.getContext().getTheme().resolveAttribute(android.R.attr.actionBarStyle, outValue, true);
        TypedArray abStyle = window.getContext().getTheme().obtainStyledAttributes(outValue.resourceId, new int[] { android.R.attr.background });
        Drawable bg = abStyle.getDrawable(0);
        abStyle.recycle();

        if (bg instanceof ColorDrawable)
            return ((ColorDrawable)bg).getColor();
        else
            return oldColor;
    }

    @SuppressWarnings("unchecked")
    private static void hookToolbar(Object thisObject, Drawable bg) {
        if (!(bg instanceof ColorDrawable)) return;
        actionBarColor = ((ColorDrawable)bg).getColor();
        Object mDecorToolbar = XposedHelpers.getObjectField(thisObject, "mDecorToolbar");
        ViewGroup mToolbar = (ViewGroup)XposedHelpers.getObjectField(mDecorToolbar, "mToolbar");
        Context mDecorContext = mToolbar.getRootView().getContext();
        if (mDecorContext != null) {
            WeakReference<Context> mActivityContext = (WeakReference<Context>)XposedHelpers.getObjectField(mDecorContext, "mActivityContext");
            Context mContext = mActivityContext.get();
            if (mContext != null && !isIgnored(mContext))
                ((Activity)mContext).getWindow().setStatusBarColor(actionBarColor);
        }
    }

    private static void hookWindowDecor(Object thisObject, Drawable bg) {
        if (!(bg instanceof ColorDrawable)) return;
        actionBarColor = ((ColorDrawable)bg).getColor();
        Activity mActivity = (Activity)XposedHelpers.getObjectField(thisObject, "mActivity");
        if (mActivity != null && !isIgnored(mActivity))
            mActivity.getWindow().setStatusBarColor(actionBarColor);
    }

    public static void StatusBarBackgroundHook() {
        Helpers.findAndHookMethod("com.android.internal.policy.PhoneWindow", null, "generateLayout", "com.android.internal.policy.DecorView", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Window wnd = (Window)param.thisObject;
                if (isIgnored(wnd.getContext())) return;
                int mStatusBarColor = XposedHelpers.getIntField(param.thisObject, "mStatusBarColor");
                if (mStatusBarColor == -16777216) return;
                int newColor = getActionBarColor(wnd, mStatusBarColor);
                if (newColor != mStatusBarColor)
                    XposedHelpers.callMethod(param.thisObject, "setStatusBarColor", newColor);
            }
        });

        Helpers.findAndHookMethod("com.android.internal.policy.PhoneWindow", null, "setStatusBarColor", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Window wnd = (Window)param.thisObject;
                if (isIgnored(wnd.getContext())) return;
                if (actionBarColor != NOCOLOR) param.args[0] = actionBarColor;
                else if (Color.alpha((int)param.args[0]) < 255) param.args[0] = Color.TRANSPARENT;
            }
        });

        Helpers.findAndHookMethod("com.android.internal.app.ToolbarActionBar", null, "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                hookToolbar(param.thisObject, (Drawable)param.args[0]);
            }
        });

        Helpers.findAndHookMethod("com.android.internal.app.WindowDecorActionBar", null, "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                hookWindowDecor(param.thisObject, (Drawable)param.args[0]);
            }
        });
    }

    public static void StatusBarBackgroundCompatHook(LoadPackageParam lpparam) {
        boolean androidx = false;

        // androidx
        Method sbdMethod = null;
        Class<?> tabCls = findClassIfExists("androidx.appcompat.app.ToolbarActionBar", lpparam.classLoader);
        if (tabCls != null) sbdMethod = findMethodExactIfExists(tabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            Helpers.hookMethod(sbdMethod, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    hookToolbar(param.thisObject, (Drawable)param.args[0]);
                }
            });

        sbdMethod = null;
        Class<?> wdabCls = findClassIfExists("androidx.appcompat.app.WindowDecorActionBar", lpparam.classLoader);
        if (wdabCls != null) sbdMethod = findMethodExactIfExists(wdabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            Helpers.hookMethod(sbdMethod, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    hookWindowDecor(param.thisObject, (Drawable)param.args[0]);
                }
            });

        // old appcompat lib
        if (!androidx) {
            sbdMethod = null;
            Class<?> tabv7Cls = findClassIfExists("android.support.v7.internal.app.ToolbarActionBar", lpparam.classLoader);
            if (tabv7Cls != null) sbdMethod = findMethodExactIfExists(tabv7Cls, "setBackgroundDrawable", Drawable.class);
            if (sbdMethod != null)
                Helpers.hookMethod(sbdMethod, new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) throws Throwable {
                        hookToolbar(param.thisObject, (Drawable)param.args[0]);
                    }
                });

            sbdMethod = null;
            Class<?> wdabv7Cls = findClassIfExists("android.support.v7.internal.app.WindowDecorActionBar", lpparam.classLoader);
            if (wdabv7Cls != null) sbdMethod = findMethodExactIfExists(wdabv7Cls, "setBackgroundDrawable", Drawable.class);
            if (sbdMethod != null)
                Helpers.hookMethod(sbdMethod, new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) throws Throwable {
                        hookWindowDecor(param.thisObject, (Drawable)param.args[0]);
                    }
                });
        }
    }

    private static boolean checkToast(Context mContext, String pkgName) {
        try {
            int opt = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_blocktoasts", "1"));
            Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_blocktoasts_apps");
            boolean isSelected = selectedApps != null && selectedApps.contains(pkgName);
            return opt == 2 && !isSelected || opt == 3 && isSelected;
        } catch (Throwable t) {
            XposedBridge.log(t);
            return false;
        }
    }

    public static void SelectiveToastsHook() {
        Helpers.findAndHookMethod("android.widget.Toast", null, "show", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                String pkgName = (String)XposedHelpers.callMethod(mContext, "getOpPackageName");
                if (pkgName == null) return;
                if (checkToast(mContext, pkgName)) param.setResult(null);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    public static void CustomRecommendedHook(LoadPackageParam lpparam, boolean isInLauncher) {
        String rrvClass = isInLauncher ? "com.miui.home.recents.views.RecentsRecommendView" : "com.android.systemui.recents.views.RecentsRecommendView";
        if (isInLauncher && findClassIfExists(rrvClass, lpparam.classLoader) == null) return;
        Helpers.findAndHookConstructor(rrvClass, lpparam.classLoader, Context.class, AttributeSet.class, int.class, int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)param.args[0];
                Handler mHandler = new Handler(mContext.getMainLooper());

                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if (key.contains("pref_key_system_recommended"))
                                XposedHelpers.callMethod(param.thisObject, "onFinishInflate");
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.findAndHookMethod(rrvClass, lpparam.classLoader, "initItem", int.class, int.class, int.class, new MethodHook() {
            @Override
            @SuppressLint("WrongConstant")
            protected void after(MethodHookParam param) throws Throwable {
                LinearLayout view = (LinearLayout)param.thisObject;
                Context context = view.getContext();
                Resources resources = view.getResources();
                Resources modRes = Helpers.getModuleRes(context);
                int itemFirstResId = resources.getIdentifier("first_item", "id", lpparam.packageName);
                int itemSecondResId = resources.getIdentifier("second_item", "id", lpparam.packageName);
                int itemThirdResId = resources.getIdentifier("third_item", "id", lpparam.packageName);
                int itemFourthResId = resources.getIdentifier("fourth_item", "id", lpparam.packageName);
                int itemIconResId = resources.getIdentifier("item_icon", "id", lpparam.packageName);
                int itemTitleResId = resources.getIdentifier("item_title", "id", lpparam.packageName);

                int action = 1;
                String title = null;
                if ((int)param.args[0] == itemFirstResId) {
                    action = Helpers.getSharedIntPref(context, "pref_key_system_recommended_first_action", 1);
                    title = Helpers.getActionName(context, "pref_key_system_recommended_first");
                } else if ((int)param.args[0] == itemSecondResId) {
                    action = Helpers.getSharedIntPref(context, "pref_key_system_recommended_second_action", 1);
                    title = Helpers.getActionName(context, "pref_key_system_recommended_second");
                } else if ((int)param.args[0] == itemThirdResId) {
                    action = Helpers.getSharedIntPref(context, "pref_key_system_recommended_third_action", 1);
                    title = Helpers.getActionName(context, "pref_key_system_recommended_third");
                } else if ((int)param.args[0] == itemFourthResId) {
                    action = Helpers.getSharedIntPref(context, "pref_key_system_recommended_fourth_action", 1);
                    title = Helpers.getActionName(context, "pref_key_system_recommended_fourth");
                }
                if (action <= 1) return;

                int icon = R.drawable.recents_icon_custom;
                if (action == 8 || action == 9 || action == 20) icon = R.drawable.recents_icon_launch;
                else if (action == 10) icon = R.drawable.recents_icon_toggle;

                View item = view.findViewById((int)param.args[0]);
                ImageView item_icon = item.findViewById(itemIconResId);
                item_icon.setMinimumWidth(item_icon.getDrawable().getIntrinsicWidth());
                item_icon.setMinimumHeight(item_icon.getDrawable().getIntrinsicHeight());
                item_icon.setImageDrawable(modRes.getDrawable(icon, view.getContext().getTheme()));
                TextView item_title = item.findViewById(itemTitleResId);
                item_title.setText(title == null ? "-" : title);
                if (((String)item_title.getText()).contains(" ")) {
                    item_title.setSingleLine(false);
                    item_title.setMaxLines(2);
                } else {
                    item_title.setMaxLines(1);
                    item_title.setSingleLine(true);
                }
                item_title.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);
                item_title.setEllipsize(TextUtils.TruncateAt.END);
            }
        });

        Helpers.findAndHookMethod(rrvClass, lpparam.classLoader, "onClick", View.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View view = ((View)param.args[0]);
                if (view == null) return;

                Context context = view.getContext();
                Resources resources = view.getResources();
                int itemFirstResId = resources.getIdentifier("first_item", "id", lpparam.packageName);
                int itemSecondResId = resources.getIdentifier("second_item", "id", lpparam.packageName);
                int itemThirdResId = resources.getIdentifier("third_item", "id", lpparam.packageName);
                int itemFourthResId = resources.getIdentifier("fourth_item", "id", lpparam.packageName);

                String key = null;
                if (view.getId() == itemFirstResId)
                    key = "pref_key_system_recommended_first";
                else if (view.getId() == itemSecondResId)
                    key = "pref_key_system_recommended_second";
                else if (view.getId() == itemThirdResId)
                    key = "pref_key_system_recommended_third";
                else if (view.getId() == itemFourthResId)
                    key = "pref_key_system_recommended_fourth";

                int action = Helpers.getSharedIntPref(context, key + "_action", 1);
                if (action <= 1) return;

                // Close recents after app/shortcut launch
                if (action == 8 || action == 9 || action == 20) {
                    GlobalActions.IntentType intentType = GlobalActions.IntentType.APP;
                    if (action == 9)
                        intentType = GlobalActions.IntentType.SHORTCUT;
                    else if (action == 20)
                        intentType = GlobalActions.IntentType.ACTIVITY;
                    Intent intent = GlobalActions.getIntent(context, key, intentType, false);
                    if (intent != null) {
                        param.setResult(null);
                        TaskStackBuilder.create(context.getApplicationContext()).addNextIntentWithParentStack(intent).startActivities();
                        return;
                    }
                }

                // Do not close after other actions
                if (GlobalActions.handleAction(context, key)) param.setResult(null);
            }
        });
    }

    public static void UnblockThirdLaunchersHook(LoadPackageParam lpparam) {
        String thirdCls = "com.miui.securitycenter.provider.ThirdMonitorProvider";
        Helpers.hookAllMethods(thirdCls, lpparam.classLoader, "call", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Bundle bundle = new Bundle();
                bundle.putInt("mode", 1);
                bundle.putStringArrayList("list", new ArrayList<String>());
                param.setResult(bundle);
            }
        });
    }

    public static void CleanShareMenuHook() {
        Helpers.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", null, "run", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Intent mOriginalIntent = (Intent)XposedHelpers.getObjectField(param.thisObject, "mOriginalIntent");
                if (mOriginalIntent == null) return;
                String action = mOriginalIntent.getAction();
                if (action == null) return;
                if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) return;
                if (mOriginalIntent.getDataString() != null && mOriginalIntent.getDataString().contains(":")) return;

                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                String mAimPackageName = (String)XposedHelpers.getObjectField(param.thisObject, "mAimPackageName");
                if (mContext == null || mAimPackageName == null) return;
                Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_cleanshare_apps");
                View mRootView = (View)XposedHelpers.getObjectField(param.thisObject, "mRootView");
                int appResId1 = mContext.getResources().getIdentifier("app1", "id", "android.miui");
                int appResId2 = mContext.getResources().getIdentifier("app2", "id", "android.miui");
                boolean removeOriginal = selectedApps.contains(mAimPackageName) || selectedApps.contains(mAimPackageName + "|0");
                boolean removeDual = selectedApps.contains(mAimPackageName + "|999");
                View originalApp = mRootView.findViewById(appResId1);
                View dualApp = mRootView.findViewById(appResId2);
                if (removeOriginal)dualApp.performClick();
                else if (removeDual) originalApp.performClick();
            }
        });
    }

    public static void CleanShareMenuServiceHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "systemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_cleanshare_apps") {
                    @Override
                    public void onChange(String name) {
                        MainModule.mPrefs.put(name, Helpers.getSharedStringSetPref(mContext, name));
                    }
                };
            }
        });

        MethodHook hook = new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(MethodHookParam param) throws Throwable {
                try {
                    if (param.args[0] == null) return;
                    Intent origIntent = (Intent)param.args[0];
                    String action = origIntent.getAction();
                    if (action == null) return;
                    if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) return;
                    Intent intent = (Intent)origIntent.clone();
                    if (intent.getDataString() != null && intent.getDataString().contains(":")) return;
                    if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) return;
                    Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps");
                    List<ResolveInfo> resolved = (List<ResolveInfo>)param.getResult();
                    ResolveInfo resolveInfo;
                    Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                    PackageManager pm = mContext.getPackageManager();
                    Iterator<ResolveInfo> itr = resolved.iterator();
                    while (itr.hasNext()) {
                        resolveInfo = itr.next();
                        boolean removeOriginal = selectedApps.contains(resolveInfo.activityInfo.packageName) || selectedApps.contains(resolveInfo.activityInfo.packageName + "|0");
                        boolean removeDual = selectedApps.contains(resolveInfo.activityInfo.packageName + "|999");
                        boolean hasDual = false;
                        try {
                            hasDual = XposedHelpers.callMethod(pm, "getPackageInfoAsUser", resolveInfo.activityInfo.packageName, 0, 999) != null;
                        } catch (Throwable ignore) {}
                        if ((removeOriginal && !hasDual) || removeOriginal && hasDual && removeDual) itr.remove();
                    }
                    param.setResult(resolved);
                } catch (Throwable t) {
                    if (!(t instanceof BadParcelableException)) XposedBridge.log(t);
                }
            }
        };

        if (!Helpers.findAndHookMethodSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", Intent.class, String.class, int.class, int.class, int.class, boolean.class, boolean.class, hook))
            Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", Intent.class, String.class, int.class, int.class, hook);
    }

    private static boolean hideMimeType(int mimeFlags, String mimeType) {
        int dataType = MimeType.OTHERS;
        if (mimeType != null)
            if (mimeType.startsWith("image/")) dataType = MimeType.IMAGE;
            else if (mimeType.startsWith("audio/")) dataType = MimeType.AUDIO;
            else if (mimeType.startsWith("video/")) dataType = MimeType.VIDEO;
            else if (mimeType.startsWith("text/") ||
                    mimeType.startsWith("application/pdf") ||
                    mimeType.startsWith("application/msword") ||
                    mimeType.startsWith("application/vnd.ms-") ||
                    mimeType.startsWith("application/vnd.openxmlformats-")) dataType = MimeType.DOCUMENT;
            else if (mimeType.startsWith("application/vnd.android.package-archive") ||
                    mimeType.startsWith("application/zip") ||
                    mimeType.startsWith("application/x-zip") ||
                    mimeType.startsWith("application/octet-stream") ||
                    mimeType.startsWith("application/rar") ||
                    mimeType.startsWith("application/x-rar") ||
                    mimeType.startsWith("application/x-tar") ||
                    mimeType.startsWith("application/x-bzip") ||
                    mimeType.startsWith("application/gzip") ||
                    mimeType.startsWith("application/x-lz") ||
                    mimeType.startsWith("application/x-compress") ||
                    mimeType.startsWith("application/x-7z") ||
                    mimeType.startsWith("application/java-archive")) dataType = MimeType.ARCHIVE;
            else if (mimeType.startsWith("link/")) dataType = MimeType.LINK;
        return (mimeFlags & dataType) == dataType;
    }

    private static String getContentType(Context context, Intent intent) {
        String scheme = intent.getScheme();
        boolean linkSchemes = "http".equals(scheme) || "https".equals(scheme) || "vnd.youtube".equals(scheme);
        String mimeType = intent.getType();
        if (mimeType == null && linkSchemes) mimeType = "link/*";
        if (mimeType == null && intent.getData() != null) try {
            mimeType = context.getContentResolver().getType(intent.getData());
        } catch (Throwable ignore) {}
        return mimeType;
    }

    private static Pair<Boolean, Boolean> isRemoveApp(boolean dynamic, Context context, String pkgName, Set<String> selectedApps, String mimeType) {
        String key = "system_cleanopenwith_apps";
        int mimeFlags0;
        int mimeFlags999;
        if (dynamic) {
            mimeFlags0 = Helpers.getSharedIntPref(context, "pref_key_" + key + "_" + pkgName + "|0", MimeType.ALL);
            mimeFlags999 = Helpers.getSharedIntPref(context, "pref_key_" + key + "_" + pkgName + "|999", MimeType.ALL);
        } else {
            mimeFlags0 = MainModule.mPrefs.getInt(key + "_" + pkgName + "|0", MimeType.ALL);
            mimeFlags999 = MainModule.mPrefs.getInt(key + "_" + pkgName + "|999", MimeType.ALL);
        }
        boolean removeOriginal = (selectedApps.contains(pkgName) || selectedApps.contains(pkgName + "|0")) && hideMimeType(mimeFlags0, mimeType);
        boolean removeDual = selectedApps.contains(pkgName + "|999") && hideMimeType(mimeFlags999, mimeType);
        return new Pair<Boolean, Boolean>(removeOriginal, removeDual);
    }

    public static void CleanOpenWithMenuHook() {
        Helpers.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", null, "run", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Intent mOriginalIntent = (Intent)XposedHelpers.getObjectField(param.thisObject, "mOriginalIntent");
                if (mOriginalIntent == null) return;
                String action = mOriginalIntent.getAction();
                if (!Intent.ACTION_VIEW.equals(action)) return;
                //if (mOriginalIntent.getDataString() != null && mOriginalIntent.getDataString().contains(":")) return;

                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                String mAimPackageName = (String)XposedHelpers.getObjectField(param.thisObject, "mAimPackageName");
                if (mContext == null || mAimPackageName == null) return;
                Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_cleanopenwith_apps");
                String mimeType = getContentType(mContext, mOriginalIntent);
                Pair<Boolean, Boolean> isRemove = isRemoveApp(true, mContext, mAimPackageName, selectedApps, mimeType);

                View mRootView = (View)XposedHelpers.getObjectField(param.thisObject, "mRootView");
                int appResId1 = mContext.getResources().getIdentifier("app1", "id", "android.miui");
                int appResId2 = mContext.getResources().getIdentifier("app2", "id", "android.miui");
                View originalApp = mRootView.findViewById(appResId1);
                View dualApp = mRootView.findViewById(appResId2);
                if (isRemove.first) dualApp.performClick();
                else if (isRemove.second) originalApp.performClick();
            }
        });
    }

    public static void CleanOpenWithMenuServiceHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "systemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String type = uri.getPathSegments().get(1);
                            String key = uri.getPathSegments().get(2);
                            if (!key.contains("pref_key_system_cleanopenwith_apps")) return;

                            switch (type) {
                                case "stringset":
                                    MainModule.mPrefs.put(key, Helpers.getSharedStringSetPref(mContext, key));
                                    break;
                                case "integer":
                                    MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 0));
                                    break;
                            }
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        MethodHook hook = new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(MethodHookParam param) throws Throwable {
                try {
                    if (param.args[0] == null) return;
                    Intent origIntent = (Intent)param.args[0];
                    Intent intent = (Intent)origIntent.clone();
                    String action = intent.getAction();
                    //XposedBridge.log(action + ": " + intent.getType() + " | " + intent.getDataString());
                    if (!Intent.ACTION_VIEW.equals(action)) return;
                    if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) return;
                    String scheme = intent.getScheme();
                    boolean validSchemes = "http".equals(scheme) || "https".equals(scheme) || "vnd.youtube".equals(scheme);
                    if (intent.getType() == null && !validSchemes) return;

                    Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                    String mimeType = getContentType(mContext, intent);
                    //XposedBridge.log("mimeType: " + mimeType);

                    String key = "system_cleanopenwith_apps";
                    Set<String> selectedApps = MainModule.mPrefs.getStringSet(key);
                    List<ResolveInfo> resolved = (List<ResolveInfo>)param.getResult();
                    ResolveInfo resolveInfo;
                    PackageManager pm = mContext.getPackageManager();
                    Iterator<ResolveInfo> itr = resolved.iterator();
                    while (itr.hasNext()) {
                        resolveInfo = itr.next();
                        Pair<Boolean, Boolean> isRemove = isRemoveApp(false, mContext, resolveInfo.activityInfo.packageName, selectedApps, mimeType);
                        boolean hasDual = false;
                        try {
                            hasDual = XposedHelpers.callMethod(pm, "getPackageInfoAsUser", resolveInfo.activityInfo.packageName, 0, 999) != null;
                        } catch (Throwable ignore) {}
                        if ((isRemove.first && !hasDual) || isRemove.first && hasDual && isRemove.second) itr.remove();
                    }

                    param.setResult(resolved);
                } catch (Throwable t) {
                    if (!(t instanceof BadParcelableException)) XposedBridge.log(t);
                }
            }
        };

        if (!Helpers.findAndHookMethodSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", Intent.class, String.class, int.class, int.class, int.class, boolean.class, boolean.class, hook))
            Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", Intent.class, String.class, int.class, int.class, hook);
    }

    public static void VolumeTimerValuesRes(LoadPackageParam lpparam) {
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "array", "miui_volume_timer_segments", R.array.miui_volume_timer_segments);

        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "initTimerString", new MethodHook() {
                        @Override
                        protected void after(MethodHookParam param) throws Throwable {
                            Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            String[] mTimeSegmentTitle = new String[11];
                            int timerOffId = mContext.getResources().getIdentifier("timer_off", "string", "miui.systemui.plugin");
                            int minuteId = mContext.getResources().getIdentifier("timer_30_minutes", "string", "miui.systemui.plugin");
                            int hourId = mContext.getResources().getIdentifier("timer_1_hour", "string", "miui.systemui.plugin");
                            mTimeSegmentTitle[0] = mContext.getResources().getString(timerOffId);
                            mTimeSegmentTitle[1] = mContext.getResources().getString(minuteId, 30);
                            mTimeSegmentTitle[2] = mContext.getResources().getString(hourId, 1);
                            mTimeSegmentTitle[3] = mContext.getResources().getString(hourId, 2);
                            mTimeSegmentTitle[4] = mContext.getResources().getString(hourId, 3);
                            mTimeSegmentTitle[5] = mContext.getResources().getString(hourId, 4);
                            mTimeSegmentTitle[6] = mContext.getResources().getString(hourId, 5);
                            mTimeSegmentTitle[7] = mContext.getResources().getString(hourId, 6);
                            mTimeSegmentTitle[8] = mContext.getResources().getString(hourId, 8);
                            mTimeSegmentTitle[9] = mContext.getResources().getString(hourId, 10);
                            mTimeSegmentTitle[10] = mContext.getResources().getString(hourId, 12);
                            XposedHelpers.setObjectField(param.thisObject, "mTimeSegmentTitle", mTimeSegmentTitle);
                        }
                    });
                    Helpers.findAndHookMethod("com.android.systemui.miui.volume.TimerItem", pluginLoader, "getTimePos", int.class, new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            Object timer = XposedHelpers.getObjectField(param.thisObject, "mTimerTime");
                            float halfTimerWidth = ((int) XposedHelpers.callMethod(timer, "getWidth")) / 2.0f;
                            Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            float seekWidth = mContext.getResources().getDimension(mContext.getResources().getIdentifier("miui_volume_timer_seelbar_width", "dimen", "miui.systemui.plugin"));
                            int marginLeft = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("miui_volume_timer_seekbar_margin_left", "dimen", "miui.systemui.plugin"));
                            int seg = (int) XposedHelpers.getObjectField(param.thisObject, "mDeterminedSegment");
                            param.setResult(seekWidth / 10 * seg + marginLeft - halfTimerWidth);
                        }
                    });
                }
            }
        });
    }

    public static void AppLockHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.miui.server.SecurityManagerService", lpparam.classLoader, "removeAccessControlPassLocked", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (!"*".equals(param.args[1])) return;
                int mode = (int)XposedHelpers.callMethod(param.thisObject, "getAccessControlLockMode", param.args[0]);
                if (mode != 1) param.setResult(null);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static void saveLastCheck(Object thisObject, String pkgName, int userId) {
        boolean enabled = false;
        if (pkgName != null && !"com.miui.home".equals(pkgName)) enabled = (boolean)XposedHelpers.callMethod(thisObject, "getApplicationAccessControlEnabledAsUser", pkgName, userId);
        Object userState = XposedHelpers.callMethod(thisObject, "getUserStateLocked", userId);
        XposedHelpers.setAdditionalInstanceField(userState, "mAccessControlLastCheckSaved",
                enabled ? new ArrayMap<String, Long>((ArrayMap<String, Long>)XposedHelpers.getObjectField(userState, "mAccessControlLastCheck")) : null
        );
    }

    @SuppressWarnings({"unchecked"})
    private static void checkLastCheck(Object thisObject, int userId) {
        Object userState = XposedHelpers.callMethod(thisObject, "getUserStateLocked", userId);
        ArrayMap<String, Long> mAccessControlLastCheckSaved = (ArrayMap<String, Long>)XposedHelpers.getAdditionalInstanceField(userState, "mAccessControlLastCheckSaved");
        if (mAccessControlLastCheckSaved == null) return;
        ArrayMap<String, Long> mAccessControlLastCheck = (ArrayMap<String, Long>)XposedHelpers.getObjectField(userState, "mAccessControlLastCheck");
        if (mAccessControlLastCheck.size() == 0) return;
        long timeout = MainModule.mPrefs.getInt("system_applock_timeout", 1) * 60L * 1000L;
        for (Map.Entry<String, Long> pair: mAccessControlLastCheck.entrySet()) {
            String pkg = pair.getKey();
            Long time = pair.getValue();
            if (mAccessControlLastCheckSaved.containsKey(pkg)) {
                Long oldTime = mAccessControlLastCheckSaved.get(pkg);
                if (!time.equals(oldTime)) {
                    mAccessControlLastCheck.put(pkg, time + (timeout - 60000L));
                    XposedHelpers.setObjectField(userState, "mAccessControlLastCheck", mAccessControlLastCheck);
                }
            } else {
                mAccessControlLastCheck.put(pkg, time + (timeout - 60000L));
                XposedHelpers.setObjectField(userState, "mAccessControlLastCheck", mAccessControlLastCheck);
            }
        }
    }

    public static void AppLockTimeoutHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "addAccessControlPassForUser", String.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                saveLastCheck(param.thisObject, (String)param.args[0], (int)param.args[1]);
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                checkLastCheck(param.thisObject, (int)param.args[1]);
            }
        });

        Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "checkAccessControlPassLocked", String.class, Intent.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                saveLastCheck(param.thisObject, (String)param.args[0], (int)param.args[2]);
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                checkLastCheck(param.thisObject, (int)param.args[2]);
            }
        });

        Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "activityResume", Intent.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Intent intent = (Intent)param.args[0];
                if (intent.getComponent() != null)
                    saveLastCheck(param.thisObject, intent.getComponent().getPackageName(), 0);
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Intent intent = (Intent)param.args[0];
                if (intent.getComponent() != null)
                    checkLastCheck(param.thisObject, 0);
            }
        });
    }

    private static AudioVisualizer audioViz = null;
    private static boolean isKeyguardShowing = false;
    private static boolean isNotificationPanelExpanded = false;
    private static MediaController mMediaController = null;
    private static void updateAudioVisualizerState(Context context) {
        if (audioViz == null || context == null) return;
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        boolean isMusicPlaying = am != null && am.isMusicActive();
        boolean isPlaying = false;
        if (mMediaController == null || mMediaController.getPlaybackState() == null || mMediaController.getPlaybackState().getState() != PlaybackState.STATE_PLAYING) {
            if (!audioViz.showWithControllerOnly) isPlaying = isMusicPlaying;
        } else {
            isPlaying = isMusicPlaying && mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
        }
        audioViz.updateViewState(isPlaying, isKeyguardShowing, isNotificationPanelExpanded);
    }
    public static void AudioVisualizerHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "makeStatusBarView", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object viewController = XposedHelpers.getObjectField(param.thisObject, "mNotificationPanelViewController");
                FrameLayout mNotificationPanel = (FrameLayout) XposedHelpers.getObjectField(viewController, "mView");
                if (mNotificationPanel == null) {
                    Helpers.log("AudioVisualizerHook", "Cannot find mNotificationPanel");
                    return;
                }

                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                ViewGroup visFrame = new FrameLayout(mContext);
                visFrame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                audioViz = new AudioVisualizer(mContext);
                audioViz.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM));
                audioViz.setClickable(false);
                visFrame.addView(audioViz);
                visFrame.setClickable(false);
                View wallpaper = mNotificationPanel.findViewById(mContext.getResources().getIdentifier("wallpaper", "id", lpparam.packageName));
                View themebkg = mNotificationPanel.findViewById(mContext.getResources().getIdentifier("theme_background", "id", lpparam.packageName));
                View awesome = mNotificationPanel.findViewById(mContext.getResources().getIdentifier("awesome_lock_screen_container", "id", lpparam.packageName));

                int order = 0;
                if (awesome != null) order = Math.max(order, mNotificationPanel.indexOfChild(awesome));
                if (themebkg != null) order = Math.max(order, mNotificationPanel.indexOfChild(themebkg));
                if (wallpaper != null) order = Math.max(order, mNotificationPanel.indexOfChild(wallpaper));
                mNotificationPanel.addView(visFrame, order + 1);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.ScrimController", lpparam.classLoader, "onScreenTurnedOff", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (audioViz != null) audioViz.updateScreenOn(false);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.ScrimController", lpparam.classLoader, "onScreenTurnedOn", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (audioViz != null) audioViz.updateScreenOn(true);
            }
        });
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.KeyguardStateControllerImpl", lpparam.classLoader, "notifyKeyguardState", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowingNew = (boolean) param.args[0];
                if (isKeyguardShowing != isKeyguardShowingNew) {
                    isKeyguardShowing = isKeyguardShowingNew;
                    isNotificationPanelExpanded = false;
                    updateAudioVisualizerState((Context)XposedHelpers.getObjectField(param.thisObject, "mContext"));
                }
            }
        });

        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "setPanelExpanded", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isNotificationPanelExpandedNew = XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
                if (isNotificationPanelExpanded != isNotificationPanelExpandedNew) {
                    isNotificationPanelExpanded = isNotificationPanelExpandedNew;
                    updateAudioVisualizerState((Context)XposedHelpers.getObjectField(param.thisObject, "mContext"));
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                if (audioViz == null) return;
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                PowerManager powerMgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = powerMgr.isInteractive();
                if (!isScreenOn) {
                    audioViz.updateScreenOn(false);
                    return;
                } else audioViz.isScreenOn = true;

                MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(param.thisObject, "mMediaMetadata");
                Bitmap art = null;
                if (mMediaMetadata != null) {
                    art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
                }
                if (art == null) {
                    WallpaperManager wallpaperMgr = WallpaperManager.getInstance(mContext);
                    @SuppressLint("MissingPermission") Drawable wallpaperDrawable = wallpaperMgr.getDrawable();
                    if (wallpaperDrawable instanceof BitmapDrawable) {
                        art = ((BitmapDrawable)wallpaperDrawable).getBitmap();
                    }
                }

                mMediaController = (MediaController)XposedHelpers.getObjectField(param.thisObject, "mMediaController");
                updateAudioVisualizerState(mContext);
                audioViz.updateMusicArt(art);
            }
        });
    }

    private static String audioFocusPkg = null;

    @SuppressWarnings("unchecked")
    private static void removeListener(Object thisObject) {
        ArrayList<Object> mRecords = (ArrayList<Object>)XposedHelpers.getObjectField(thisObject, "mRecords");
        if (mRecords == null) return;
        for (Object record: mRecords) {
            String callingPackage = (String)XposedHelpers.getObjectField(record, "callingPackage");
            int events = XposedHelpers.getIntField(record, "events");
            if ((events & PhoneStateListener.LISTEN_CALL_STATE) == PhoneStateListener.LISTEN_CALL_STATE &&
                    callingPackage != null && MainModule.mPrefs.getStringSet("system_ignorecalls_apps").contains(callingPackage)) {
                events &= ~PhoneStateListener.LISTEN_CALL_STATE;
                XposedHelpers.setIntField(record, "events", events);
            }
        }
    }

    public static void NoCallInterruptionHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.audio.AudioService", lpparam.classLoader, "requestAudioFocus", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if ("AudioFocus_For_Phone_Ring_And_Calls".equals(param.args[4]) && audioFocusPkg != null && MainModule.mPrefs.getStringSet("system_ignorecalls_apps").contains(audioFocusPkg))
                    param.setResult(1);
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int res = (int)param.getResult();
                if (res != AudioManager.AUDIOFOCUS_REQUEST_FAILED && !"AudioFocus_For_Phone_Ring_And_Calls".equals(param.args[4]))
                    audioFocusPkg = (String)param.args[5];
            }
        });

        Helpers.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.classLoader, "notifyCallState", int.class, String.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                removeListener(param.thisObject);
            }
        });

        Helpers.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.classLoader, "notifyCallStateForPhoneId", int.class, int.class, int.class, String.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                removeListener(param.thisObject);
            }
        });
    }

    public static void AllRotationsRes() {
        MainModule.resHooks.setObjectReplacement("android", "bool", "config_allowAllRotations", MainModule.mPrefs.getStringAsInt("system_allrotations2", 1) == 2);
    }

    public static void AllRotationsHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.server.wm.DisplayRotation", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                XposedHelpers.setIntField(param.thisObject, "mAllowAllRotations", MainModule.mPrefs.getStringAsInt("system_allrotations2", 1) == 2 ? 1 : 0);
            }
        });
    }

    private static boolean mUSBConnected = false;
    public static void USBConfigHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "systemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (mContext != null)
                    mContext.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            try {
                                boolean mConnected = intent.getBooleanExtra("connected", false);
                                if (mConnected && mConnected != mUSBConnected) try {
                                    int mPlugType = XposedHelpers.getIntField(param.thisObject, "mPlugType");
                                    if (mPlugType != BatteryManager.BATTERY_PLUGGED_USB) return;
                                    String func = MainModule.mPrefs.getString("system_defaultusb", "none");
                                    if ("none".equals(func) || "1".equals(func)) return;
                                    UsbManager usbMgr = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
                                    if ((boolean)XposedHelpers.callMethod(usbMgr, "isFunctionEnabled", func))
                                        return;
                                    XposedHelpers.callMethod(usbMgr, "setCurrentFunction", func, MainModule.mPrefs.getBoolean("system_defaultusb_unsecure"));
                                } catch (Throwable t) {
                                    XposedBridge.log(t);
                                }
                                mUSBConnected = mConnected;
                            } catch (Throwable t) {
                                XposedBridge.log(t);
                            }
                        }
                    }, new IntentFilter("android.hardware.usb.action.USB_STATE"));
            }
        });

        if (MainModule.mPrefs.getBoolean("system_defaultusb_unsecure")) {
            if (!Helpers.findAndHookMethodSilently("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.classLoader, "isUsbDataTransferActive", long.class, XC_MethodReplacement.returnConstant(true)))
                Helpers.findAndHookMethod("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.classLoader, "isUsbDataTransferActive", XC_MethodReplacement.returnConstant(true));
            Helpers.findAndHookMethod("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.classLoader, "handleMessage", Message.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    Message msg = (Message)param.args[0];
                    int setUnlockedFunc = 12;
                    try {
                        setUnlockedFunc = XposedHelpers.getStaticIntField(findClass("com.android.server.usb.UsbDeviceManager", lpparam.classLoader), "MSG_SET_SCREEN_UNLOCKED_FUNCTIONS");
                    } catch (Throwable t) {
                    }
                    if (msg.what == setUnlockedFunc) {
                        msg.obj = 0L;
                        param.args[0] = msg;
                    }
                }
            });
        }
    }

    public static void USBConfigSettingsHook(LoadPackageParam lpparam) {
        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.settings.connecteddevice.usb.UsbModeChooserReceiver", lpparam.classLoader, "onReceive", Context.class, Intent.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String func = MainModule.mPrefs.getString("system_defaultusb", "none");
                if (!"none".equals(func) && !"1".equals(func)) param.setResult(null);
            }
        });
    }

    public static void HideIconsBattery1Hook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.classLoader, "initMiuiView", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ImageView mBatteryIconView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryIconView");
                mBatteryIconView.setVisibility(View.GONE);
            }
        });
    }

    public static void HideIconsBattery2Hook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.classLoader, "updateChargeAndText", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery2")) {
                    TextView mBatteryTextDigitView = (TextView)XposedHelpers.getObjectField(param.thisObject, "mBatteryTextDigitView");
                    mBatteryTextDigitView.setVisibility(View.GONE);
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery3")) {
                    ImageView mBatteryChargingView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryChargingView");
                    mBatteryChargingView.setVisibility(View.GONE);
                    try {
                        ImageView mBatteryChargingInView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryChargingInView");
                        mBatteryChargingInView.setVisibility(View.GONE);
                    } catch (Throwable ignore) {}
                }
            }
        });
    }

    private static boolean lastState = false;
    private static void updateAlarmVisibility(Object thisObject, boolean state) {
        try {
            Object mIconController = XposedHelpers.getObjectField(thisObject, "mIconController");
            if (!state) {
                XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", false);
                return;
            }

            Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            long nowTime = java.lang.System.currentTimeMillis();
            long nextTime;
            try {
                nextTime = (long)XposedHelpers.getAdditionalInstanceField(thisObject, "mNextAlarmTime");
            } catch (Throwable t) {
                nextTime = Helpers.getNextMIUIAlarmTime(mContext);
            }
            if (nextTime == 0) nextTime = Helpers.getNextStockAlarmTime(mContext);

            long diffMSec = nextTime - nowTime;
            if (diffMSec < 0) diffMSec += 7 * 24 * 60 *60 * 1000;
            float diffHours = (diffMSec - 59 * 1000) / (1000f * 60f * 60f);
            boolean vis = diffHours <= MainModule.mPrefs.getInt("system_statusbaricons_alarmn", 0);
            XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", vis);
            mIconController = XposedHelpers.getObjectField(thisObject, "miuiDripLeftStatusBarIconController");
            XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", vis);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void HideIconsSelectiveAlarmHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mNextAlarmTime", Helpers.getNextMIUIAlarmTime(mContext));
                ContentResolver resolver = mContext.getContentResolver();
                ContentObserver alarmObserver = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        if (selfChange) return;
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "mNextAlarmTime", Helpers.getNextMIUIAlarmTime(mContext));
                        updateAlarmVisibility(param.thisObject, lastState);
                    }
                };
                resolver.registerContentObserver(Settings.System.getUriFor("next_alarm_clock_formatted"), false, alarmObserver);

                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.TIME_TICK");
                filter.addAction("android.intent.action.TIME_SET");
                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                filter.addAction("android.intent.action.LOCALE_CHANGED");
                final Object thisObject = param.thisObject;
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        updateAlarmVisibility(thisObject, lastState);
                    }
                }, filter);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, "updateAlarm", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                lastState = (boolean)XposedHelpers.getObjectField(param.thisObject, "mHasAlarm");
                updateAlarmVisibility(param.thisObject, lastState);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.classLoader, "onMiuiAlarmChanged", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                lastState = (boolean)XposedHelpers.getObjectField(param.thisObject, "mHasAlarm");
                updateAlarmVisibility(param.thisObject, lastState);
                param.setResult(null);
            }
        });
    }

    public static void HideIconsBluetoothHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.classLoader, "updateBluetooth", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_statusbaricons_bluetooth", "1"));
                boolean isBluetoothConnected = (boolean)XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mBluetooth"), "isBluetoothConnected");
                if (opt == 3 || (opt == 2 && !isBluetoothConnected)) {
                    Object mIconController = XposedHelpers.getObjectField(param.thisObject, "mIconController");
                    XposedHelpers.callMethod(mIconController, "setIconVisibility", "bluetooth", false);
                }
            }
        });
    }

    public static void DisplayWifiStandardHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.classLoader, "applyWifiState", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object wifiState = param.args[0];
                if (wifiState != null) {
                    int opt = MainModule.mPrefs.getStringAsInt("system_statusbaricons_wifistandard", 1);
                    if (opt == 1) return;
                    int wifiStandard = (int) XposedHelpers.getObjectField(wifiState, "wifiStandard");
                    XposedHelpers.setObjectField(wifiState, "showWifiStandard", opt == 2 && wifiStandard > 0);
                }
            }
        });
    }

    public static void HideIconsPrivacyHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("android.app.StatusBarManager", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String iconType = (String)param.args[0];
                if (iconType.equals("stealth")) {
                    param.args[1] = false;
                }
            }
        });
    }

    public static void HideIconsVoWiFiHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethodSilently("com.android.systemui.MiuiOperatorCustomizedPolicy$MiuiOperatorConfig", lpparam.classLoader, "getHideVowifi", XC_MethodReplacement.returnConstant(true));
    }

    public static void HideIconsSignalHook(LoadPackageParam lpparam) {
        MethodHook beforeUpdate = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object mobileIconState = param.args[0];
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_signal")) {
                    XposedHelpers.setObjectField(mobileIconState, "visible", false);
                    return;
                }
                int subId = (int) XposedHelpers.getObjectField(mobileIconState, "subId");
                if ((MainModule.mPrefs.getBoolean("system_statusbaricons_sim1") && subId == 1)
                    || (MainModule.mPrefs.getBoolean("system_statusbaricons_sim2") && subId == 2)
                ) {
                    XposedHelpers.setObjectField(mobileIconState, "visible", false);
                    return;
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_roaming")) {
                    XposedHelpers.setObjectField(mobileIconState, "roaming", false);
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_volte")) {
                    XposedHelpers.setObjectField(mobileIconState, "volte", false);
                    XposedHelpers.setObjectField(mobileIconState, "speechHd", false);
                }
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", beforeUpdate);
    }

    private static boolean checkSlot(String slotName) {
        try {
            return "headset".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_headset") ||
                "volume".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_sound") ||
                "zen".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_dnd") ||
                "volume".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_mute") ||
                "speakerphone".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_speaker") ||
                "call_record".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_record") ||
                "alarm_clock".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_alarm") ||
                "managed_profile".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_profile") ||
                "vpn".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_vpn") ||
                "nfc".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_nfc") ||
                "location".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_gps") ||
                "wifi".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_wifi") ||
                "hotspot".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_hotspot") ||
                "no_sim".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_nosims") ||
                "bluetooth_handsfree_battery".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_btbattery") ||
                "hd".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_volte");
        } catch (Throwable t) {
            XposedBridge.log(t);
            return false;
        }
    }

    public static void HideIconsHook(LoadPackageParam lpparam) {
        MethodHook iconHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String iconType = (String)param.args[0];
                if (checkSlot(iconType)) {
                    param.args[1] = false;
                }
            }
        };
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, iconHook);
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, iconHook);
    }

    public static void BatteryIndicatorHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "makeStatusBarView", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                FrameLayout mStatusBarWindow = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mPhoneStatusBarWindow");
                BatteryIndicator indicator = new BatteryIndicator(mContext);
                View panel = mStatusBarWindow.findViewById(mContext.getResources().getIdentifier("notification_panel", "id", lpparam.packageName));
                mStatusBarWindow.addView(indicator, panel != null ? mStatusBarWindow.indexOfChild(panel) + 1 : Math.max(mStatusBarWindow.getChildCount() - 1, 2));
                indicator.setAdjustViewBounds(false);
                indicator.init(param.thisObject);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mBatteryIndicator", indicator);
                Object mNotificationIconAreaController = XposedHelpers.getObjectField(param.thisObject, "mNotificationIconAreaController");
                XposedHelpers.setAdditionalInstanceField(mNotificationIconAreaController, "mBatteryIndicator", indicator);
                Object mBatteryController = XposedHelpers.getObjectField(param.thisObject, "mBatteryController");
                XposedHelpers.setAdditionalInstanceField(mBatteryController, "mBatteryIndicator", indicator);
                XposedHelpers.callMethod(mBatteryController, "fireBatteryLevelChanged");
                XposedHelpers.callMethod(mBatteryController, "firePowerSaveChanged");
//                XposedHelpers.callMethod(mBatteryController, "fireExtremePowerSaveChanged");
            }
        });

        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, "setPanelExpanded", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged(!isKeyguardShowing && (boolean)param.args[0]);
            }
        });

        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, "setQsExpanded", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
                if (!isKeyguardShowing) return;
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onExpandingChanged((boolean)param.args[0]);
            }
        });

        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, "updateKeyguardState", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onKeyguardStateChanged(isKeyguardShowing);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "onDarkChanged", Rect.class, float.class, int.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onDarkModeChanged((float)param.args[1], (int)param.args[2]);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl", lpparam.classLoader, "fireBatteryLevelChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                int mLevel = XposedHelpers.getIntField(param.thisObject, "mLevel");
                boolean mCharging = XposedHelpers.getBooleanField(param.thisObject, "mCharging");
                boolean mCharged = XposedHelpers.getBooleanField(param.thisObject, "mCharged");
                if (indicator != null) indicator.onBatteryLevelChanged(mLevel, mCharging, mCharged);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "firePowerSaveChanged", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
                if (indicator != null) indicator.onPowerSaveChanged(XposedHelpers.getBooleanField(param.thisObject, "mPowerSave"));
            }
        });

//        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "fireExtremePowerSaveChanged", new MethodHook() {
//            @Override
//            protected void after(final MethodHookParam param) throws Throwable {
//                BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
//                if (indicator != null) indicator.onExtremePowerSaveChanged(XposedHelpers.getBooleanField(param.thisObject, "mIsExtremePowerSaveMode"));
//            }
//        });
    }

    private static boolean obtainMagnifierShowCoordinates(Object mEditor, int type, final MotionEvent event, final PointF showPosInView) {
        TextView mTextView = (TextView)XposedHelpers.getObjectField(mEditor, "mTextView");
        final int offset;
        final int otherHandleOffset;
        switch (type) {
            case 0:
                offset = mTextView.getSelectionStart();
                otherHandleOffset = -1;
                break;
            case 1:
                offset = mTextView.getSelectionStart();
                otherHandleOffset = mTextView.getSelectionEnd();
                break;
            case 2:
                offset = mTextView.getSelectionEnd();
                otherHandleOffset = mTextView.getSelectionStart();
                break;
            default:
                offset = -1;
                otherHandleOffset = -1;
                break;
        }

        if (offset == -1) return false;

        final Layout layout = mTextView.getLayout();
        final int lineNumber = layout.getLineForOffset(offset);
        final boolean sameLineSelection = otherHandleOffset != -1 && lineNumber == layout.getLineForOffset(otherHandleOffset);
        final boolean rtl = sameLineSelection && (offset < otherHandleOffset) != (mTextView.getLayout().getPrimaryHorizontal(offset) < mTextView.getLayout().getPrimaryHorizontal(otherHandleOffset));

        final int[] textViewLocationOnScreen = new int[2];
        mTextView.getLocationOnScreen(textViewLocationOnScreen);
        final float touchXInView = event.getRawX() - textViewLocationOnScreen[0];
        float leftBound = mTextView.getTotalPaddingLeft() - mTextView.getScrollX();
        float rightBound = mTextView.getTotalPaddingLeft() - mTextView.getScrollX();

        if (sameLineSelection && ((type == 2) ^ rtl))
            leftBound += mTextView.getLayout().getPrimaryHorizontal(otherHandleOffset);
        else
            leftBound += mTextView.getLayout().getLineLeft(lineNumber);

        if (sameLineSelection && ((type == 1) ^ rtl))
            rightBound += mTextView.getLayout().getPrimaryHorizontal(otherHandleOffset);
        else
            rightBound += mTextView.getLayout().getLineRight(lineNumber);

        showPosInView.x = Math.max(leftBound - 1, Math.min(rightBound + 1, touchXInView));
        showPosInView.y = (mTextView.getLayout().getLineTop(lineNumber)	+ mTextView.getLayout().getLineBottom(lineNumber)) / 2.0f + mTextView.getTotalPaddingTop() - mTextView.getScrollY();
        return true;
    }

    @SuppressLint("NewApi")
    private static void processHandleMotionEvent(Object handleView, int type, MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            Object mEditor = XposedHelpers.getSurroundingThis(handleView);
            Magnifier mMagnifier = (Magnifier)XposedHelpers.getAdditionalInstanceField(mEditor, "mMagnifier");
            if (mMagnifier == null) return;
            PointF coords = new PointF();
            if (obtainMagnifierShowCoordinates(mEditor, type, ev, coords))
                mMagnifier.show(coords.x, coords.y);
            else
                mMagnifier.dismiss();
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            Magnifier mMagnifier = (Magnifier)XposedHelpers.getAdditionalInstanceField(XposedHelpers.getSurroundingThis(handleView), "mMagnifier");
            if (mMagnifier != null) mMagnifier.dismiss();
        }
    }

    public static void TextMagnifierHook() {
        Helpers.findAndHookMethod("android.widget.Magnifier", null, "obtainContentCoordinates", float.class, float.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Point mClampedCenterZoomCoords = (Point)XposedHelpers.getObjectField(param.thisObject, "mClampedCenterZoomCoords");
                Object mView = XposedHelpers.getObjectField(param.thisObject, "mView");
                float f = (float)param.args[0];
                int x;
                if (mView instanceof SurfaceView) {
                    x = Math.round(f);
                } else {
                    int[] mViewCoordinatesInSurface = (int[])XposedHelpers.getObjectField(param.thisObject, "mViewCoordinatesInSurface");
                    x = Math.round(f + (float)mViewCoordinatesInSurface[0]);
                }
                mClampedCenterZoomCoords.x = x;
            }
        });

        Helpers.findAndHookConstructor("android.widget.Editor", null, TextView.class, new MethodHook() {
            @Override
            @SuppressLint("NewApi")
            protected void after(final MethodHookParam param) throws Throwable {
                TextView mTextView = (TextView)XposedHelpers.getObjectField(param.thisObject, "mTextView");
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mMagnifier", new Magnifier(mTextView));
            }
        });

        Helpers.findAndHookMethod("android.widget.Editor$InsertionHandleView", null, "onTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                processHandleMotionEvent(param.thisObject, 0, (MotionEvent)param.args[0]);
            }
        });

        Helpers.findAndHookMethod("android.widget.Editor$SelectionHandleView", null, "onTouchEvent", MotionEvent.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                processHandleMotionEvent(param.thisObject, "SelectionEndHandleView".equals(param.thisObject.getClass().getSimpleName()) ? 2 : 1, (MotionEvent)param.args[0]);
            }
        });
    }

    public static void ForceCloseHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(final MethodHookParam param) throws Throwable {
                HashSet<String> mSystemKeyPackages = (HashSet<String>)XposedHelpers.getObjectField(param.thisObject, "mSystemKeyPackages");
                mSystemKeyPackages.remove("com.miui.securitycenter");
                mSystemKeyPackages.remove("com.miui.securityadd");
                mSystemKeyPackages.remove("com.android.phone");
                mSystemKeyPackages.remove("com.android.mms");
                mSystemKeyPackages.remove("com.android.contacts");
                mSystemKeyPackages.remove("com.miui.home");
                mSystemKeyPackages.remove("com.jeejen.family.miui");
                mSystemKeyPackages.remove("com.miui.backup");
                mSystemKeyPackages.remove("com.xiaomi.mihomemanager");
                mSystemKeyPackages.addAll(MainModule.mPrefs.getStringSet("system_forceclose_apps"));
            }
        });
    }

    public static void DisableAnyNotificationBlockHook() {
        Helpers.hookAllConstructors("android.app.NotificationChannel", null, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
            XposedHelpers.setBooleanField(param.thisObject, "mBlockableSystem", true);
            }
        });

        Helpers.findAndHookMethod("android.app.NotificationChannel", null, "setBlockable", boolean.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
            XposedHelpers.setBooleanField(param.thisObject, "mBlockableSystem", true);
            }
        });
    }

    public static void DisableAnyNotificationHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("miui.util.NotificationFilterHelper", lpparam.classLoader, "isNotificationForcedEnabled", XC_MethodReplacement.returnConstant(false));
        Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "isNotificationForcedFor", Context.class, String.class, XC_MethodReplacement.returnConstant(false));
        Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "canSystemNotificationBeBlocked", String.class, XC_MethodReplacement.returnConstant(true));
        Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "containNonBlockableChannel", String.class, XC_MethodReplacement.returnConstant(false));
        Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "getNotificationForcedEnabledList", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
            param.setResult(new HashSet<String>());
            }
        });
    }

    public static void NotificationImportanceHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.settings.notification.BaseNotificationSettings", lpparam.classLoader, "setPrefVisible", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object pref = param.args[0];
                if (pref != null) {
                    String prefKey = (String) XposedHelpers.callMethod(pref, "getKey");
                    if ("importance".equals(prefKey)) {
                        param.args[1] = true;
                    }
                }
            }
        });
        Helpers.findAndHookMethod("com.android.settings.notification.ChannelNotificationSettings", lpparam.classLoader, "setupChannelDefaultPrefs", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object pref = XposedHelpers.callMethod(param.thisObject, "findPreference", "importance");
                XposedHelpers.setObjectField(param.thisObject, "mImportance", pref);
                int mBackupImportance = (int)XposedHelpers.getObjectField(param.thisObject, "mBackupImportance");
                if (mBackupImportance > 0) {
                    int index = (int)XposedHelpers.callMethod(pref, "findSpinnerIndexOfValue", String.valueOf(mBackupImportance));
                    if (index > -1) {
                        XposedHelpers.callMethod(pref, "setValueIndex", index);
                    }
                    Class<?> ImportanceListener = XposedHelpers.findClassIfExists("androidx.preference.Preference$OnPreferenceChangeListener", lpparam.classLoader);
                    InvocationHandler handler = new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("onPreferenceChange")) {
                                int mBackupImportance = Integer.parseInt((String) args[1]);
                                XposedHelpers.setObjectField(param.thisObject, "mBackupImportance", mBackupImportance);
                                NotificationChannel mChannel = (NotificationChannel) XposedHelpers.getObjectField(param.thisObject, "mChannel");
                                mChannel.setImportance(mBackupImportance);
                                XposedHelpers.callMethod(mChannel, "lockFields", 4);
                                Object mBackend = XposedHelpers.getObjectField(param.thisObject, "mBackend");
                                String mPkg = (String) XposedHelpers.getObjectField(param.thisObject, "mPkg");
                                int mUid = (int) XposedHelpers.getObjectField(param.thisObject, "mUid");
                                XposedHelpers.callMethod(mBackend, "updateChannel", mPkg, mUid, mChannel);
                                XposedHelpers.callMethod(param.thisObject, "updateDependents", false);
                            }
                            return true;
                        }
                    };
                    Object mImportanceListener = Proxy.newProxyInstance(
                        lpparam.classLoader,
                        new Class[] { ImportanceListener },
                        handler
                    );
                    XposedHelpers.callMethod(pref, "setOnPreferenceChangeListener", mImportanceListener);
                }
            }
        });
    }

    private static boolean modifyCameraImage(Context mContext, View mKeyguardRightView, boolean mDarkMode) {
        if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        final String key = "pref_key_system_lockscreenshortcuts_right";

        int action = Helpers.getSharedIntPref(mContext, key + "_action", 1);
        if (action <= 1) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        String str = Helpers.getActionName(mContext, key);
        if (str == null) {
            restoreCameraImage(mKeyguardRightView);
            return false;
        }

        Drawable icon = Helpers.getActionImage(mContext, key);
        mKeyguardRightView.setBackgroundColor(Color.TRANSPARENT);
        mKeyguardRightView.setForeground(icon);
        mKeyguardRightView.setForegroundGravity(Gravity.CENTER);

        float density = mContext.getResources().getDisplayMetrics().density;
        int size = Math.round(mContext.getResources().getConfiguration().smallestScreenWidthDp * density);

        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShadowLayer(2 * density, 0, 0, mDarkMode ? Color.argb(90, 255, 255, 255) : Color.argb(90, 0, 0, 0));
        paint.setTextSize(20 * density);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(density);
        paint.setColor(mDarkMode ? Color.WHITE : Color.BLACK);
        paint.setAlpha(90);

        Rect bounds = new Rect();
        paint.getTextBounds(str, 0, str.length(), bounds);
        float x = size / 2f - bounds.width() / 2f;
        float y = size / 2f + bounds.height() / 2f + (icon == null ? 0 : icon.getIntrinsicHeight() / 2f + 30 * density);
        canvas.drawText(str, x, y, paint);
        paint.setStyle(Paint.Style.FILL);
//		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.clearShadowLayer();
        paint.setColor(mDarkMode ? Color.BLACK : Color.WHITE);
        paint.setAlpha(mDarkMode ? 160 : 230);
        canvas.drawText(str, x, y, paint);

        BitmapDrawable bmpDrawable = new BitmapDrawable(mContext.getResources(), bmp);
        if (mKeyguardRightView instanceof ImageView) {
            ((ImageView)mKeyguardRightView).setScaleType(ImageView.ScaleType.CENTER);
            ((ImageView)mKeyguardRightView).setImageDrawable(bmpDrawable);
        } else {
            bmpDrawable.setGravity(Gravity.CENTER);
            mKeyguardRightView.setBackground(bmpDrawable);
        }

        return true;
    }

    private static void restoreCameraImage(View mKeyguardRightView) {
        mKeyguardRightView.setBackgroundColor(Color.BLACK);
        mKeyguardRightView.setForeground(null);
        if (mKeyguardRightView instanceof ImageView)
            ((ImageView)mKeyguardRightView).setScaleType(ImageView.ScaleType.FIT_END);
    }

    private static void initLeftView(final Object thisObject) {
        Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
        try { XposedHelpers.setObjectField(thisObject, "mMiWalletCardNum", new TextView(mContext)); } catch (Throwable ignore) {}
        try { XposedHelpers.setObjectField(thisObject, "mRemoteControllerNum", new TextView(mContext)); } catch (Throwable ignore) {}
        try { XposedHelpers.setObjectField(thisObject, "mSmartHomeNum", new TextView(mContext)); } catch (Throwable ignore) {}

        Handler mHandler = (Handler)XposedHelpers.getAdditionalInstanceField(thisObject, "myHandler");
        mHandler.removeMessages(1);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = thisObject;
        mHandler.sendMessageDelayed(msg, 1000);
    }

    private static Object notificationPanelView = null;
    public static void LockScreenShortcutHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$MiuiDefaultLeftButton", lpparam.classLoader, "getIcon", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object img = param.getResult();
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
                    Object thisObject = XposedHelpers.getSurroundingThis(param.thisObject);
                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
                    boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkStyle");
                    Drawable flashlightDrawable;
                    Object flashlightController = XposedHelpers.getObjectField(thisObject, "mFlashlightController");
                    boolean isOn = (boolean) XposedHelpers.callMethod(flashlightController, "isEnabled");
                    if (isOn) {
                        flashlightDrawable = Helpers.getModuleRes(mContext).getDrawable(
                            mDarkMode ? R.drawable.keyguard_bottom_flashlight_on_img_dark : R.drawable.keyguard_bottom_flashlight_on_img_light,
                            mContext.getTheme()
                        );
                    }
                    else {
                        flashlightDrawable = Helpers.getModuleRes(mContext).getDrawable(
                            mDarkMode ? R.drawable.keyguard_bottom_flashlight_img_dark : R.drawable.keyguard_bottom_flashlight_img_light,
                            mContext.getTheme()
                        );
                    }
                    XposedHelpers.setObjectField(img, "drawable", flashlightDrawable);
                }
                else if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    XposedHelpers.setObjectField(img, "drawable", null);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$MiuiDefaultRightButton", lpparam.classLoader, "getIcon", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object img = param.getResult();
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
                    XposedHelpers.setObjectField(img, "drawable", null);
                    return;
                }

                boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
                if (!opt) return;
                Object thisObject = XposedHelpers.getSurroundingThis(param.thisObject);
                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
                boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkStyle");
                XposedHelpers.setObjectField(img, "drawable", Helpers.getModuleRes(mContext).getDrawable(
                    mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light,
                    mContext.getTheme()
                ));
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "initTipsView", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
                if (!opt) return;
                boolean isLeft = (boolean) param.args[0];
                if (!isLeft) {
                    TextView mRightAffordanceViewTips = (TextView)XposedHelpers.getObjectField(param.thisObject, "mRightAffordanceViewTips");
                    if (mRightAffordanceViewTips != null) mRightAffordanceViewTips.setText(Helpers.getModuleRes(mRightAffordanceViewTips.getContext()).getString(R.string.system_lockscreenshortcuts_right_image_hint));
                }
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "launchCamera", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (GlobalActions.handleAction(mContext, "pref_key_system_lockscreenshortcuts_right", true)) {
                    param.setResult(null);
                    Object PanelInjector = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClass("com.android.keyguard.injector.KeyguardPanelViewInjector", lpparam.classLoader));
                    Object panelController = XposedHelpers.getObjectField(PanelInjector, "mPanelViewController");
                    final View mNotificationPanelView = (View)XposedHelpers.getObjectField(PanelInjector, "mPanelView");
                    mNotificationPanelView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            XposedHelpers.callMethod(panelController, "resetViews", false);
                        }
                    }, 500);
                }
            }
        });


        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "onClick", View.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
                    View view = (View) param.args[0];
                    View mLeftAffordanceView = (View) XposedHelpers.getObjectField(param.thisObject, "mLeftAffordanceView");
                    if (view == mLeftAffordanceView) {
                        Object flashlightController = XposedHelpers.getObjectField(param.thisObject, "mFlashlightController");
                        boolean z = !(boolean) XposedHelpers.callMethod(flashlightController, "isEnabled");
                        XposedHelpers.callMethod(flashlightController, "setFlashlight", z);
                        XposedHelpers.callMethod(param.thisObject, "updateLeftAffordanceIcon");
                        param.setResult(null);
                    }
                }
            }
        });

        Helpers.hookAllMethods("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "setDarkStyle", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image")) {
                    Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                    boolean mDarkMode = XposedHelpers.getBooleanField(param.thisObject, "mDarkStyle");
                    XposedHelpers.callMethod(param.thisObject, "setPreviewImageDrawable", Helpers.getModuleRes(mContext).getDrawable(mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light, mContext.getTheme()));
                }
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "updatePreView", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View mPreViewContainer = (View)XposedHelpers.getObjectField(param.thisObject, "mPreViewContainer");
                if ("active".equals(mPreViewContainer.getTag())) {
                    XposedHelpers.setFloatField(param.thisObject, "mIconCircleAlpha", 0.0f);
                    ((View)param.thisObject).invalidate();
                }
            }
        });

        Helpers.hookAllMethods("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "setPreviewImageDrawable", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");

                boolean mDarkMode = XposedHelpers.getBooleanField(param.thisObject, "mDarkStyle");
                ImageView mIconView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mIconView");
                if (mIconView != null)
                    if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image"))
                        mIconView.setImageDrawable(Helpers.getModuleRes(mContext).getDrawable(mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img_light, mContext.getTheme()));
                    else
                        mIconView.setImageDrawable(mContext.getDrawable(mDarkMode ? mContext.getResources().getIdentifier("keyguard_bottom_camera_img_dark", "drawable", lpparam.packageName) : mContext.getResources().getIdentifier("keyguard_bottom_camera_img", "drawable", lpparam.packageName)));

                View mPreView = (View)XposedHelpers.getObjectField(param.thisObject, "mPreView");
                View mPreViewContainer = (View)XposedHelpers.getObjectField(param.thisObject, "mPreViewContainer");
                View mBackgroundView = (View)XposedHelpers.getObjectField(param.thisObject, "mBackgroundView");
                Paint mIconCircleStrokePaint = (Paint)XposedHelpers.getObjectField(param.thisObject, "mIconCircleStrokePaint");
                ViewOutlineProvider mPreViewOutlineProvider = (ViewOutlineProvider)XposedHelpers.getObjectField(param.thisObject, "mPreViewOutlineProvider");
                boolean result = modifyCameraImage(mContext, mPreView, mDarkMode);
                if (result) param.setResult(null);
                if (mPreViewContainer != null) {
                    mPreViewContainer.setBackgroundColor(result ? Color.TRANSPARENT : Color.BLACK);
                    mPreViewContainer.setOutlineProvider(result ? null : mPreViewOutlineProvider);
                    mPreViewContainer.setTag(result ? "active" : "inactive");
                }
                if (mBackgroundView != null) mBackgroundView.setBackgroundColor(result ? Color.TRANSPARENT : Color.BLACK);
                if (mIconCircleStrokePaint != null) mIconCircleStrokePaint.setColor(result ? Color.TRANSPARENT : Color.WHITE);
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "handleMoveDistanceChanged", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                View mIconView = (View)XposedHelpers.getObjectField(param.thisObject, "mIconView");
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
                    if (mIconView != null) mIconView.setVisibility(View.GONE);
                    param.setResult(null);
                } else if (mIconView != null) mIconView.setVisibility(View.VISIBLE);
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.MiuiKeyguardCameraView", lpparam.classLoader, "startFullScreenAnim", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int action = MainModule.mPrefs.getInt("system_lockscreenshortcuts_right_action", 1);
                if (action <= 1) return;
                AnimatorSet mAnimatorSet = (AnimatorSet)XposedHelpers.getObjectField(param.thisObject, "mAnimatorSet");
                if (mAnimatorSet == null) return;
                param.setResult(null);
                mAnimatorSet.pause();
                mAnimatorSet.removeAllListeners();
                mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                        GlobalActions.handleAction(mContext, "pref_key_system_lockscreenshortcuts_right", true);
                        Object mCallBack = XposedHelpers.getObjectField(param.thisObject, "mCallBack");
                        if (mCallBack != null) XposedHelpers.callMethod(mCallBack, "onCompletedAnimationEnd");
                        XposedHelpers.setBooleanField(param.thisObject, "mIsPendingStartCamera", false);
                        XposedHelpers.callMethod(param.thisObject, "dismiss");
                        View mBackgroundView = (View)XposedHelpers.getObjectField(param.thisObject, "mBackgroundView");
                        if (mBackgroundView != null) mBackgroundView.setAlpha(1.0f);
                    }
                });
                mAnimatorSet.resume();
            }
        });

        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                notificationPanelView = param.thisObject;
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                new Helpers.SharedPrefObserver(mContext, new Handler(mContext.getMainLooper())) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String type = uri.getPathSegments().get(1);
                            String key = uri.getPathSegments().get(2);
                            if (!key.contains("pref_key_system_lockscreenshortcuts")) return;

                            switch (type) {
                                case "string":
                                    MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, ""));
                                    break;
                                case "integer":
                                    MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 1));
                                    break;
                                case "boolean":
                                    MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(mContext, key, false));
                                    break;
                            }

                            if (key.contains("pref_key_system_lockscreenshortcuts_right"))
                                try {
                                    XposedHelpers.callMethod(notificationPanelView, "setCameraImage", false);
                                } catch (Throwable t1) {
                                    try {
                                        XposedHelpers.callMethod(notificationPanelView, "setCameraImage");
                                    } catch (Throwable t2) {}
                                }

//                            if (key.contains("pref_key_system_lockscreenshortcuts_left")) {
//                                Object leftView = null;
//                                try {
//                                    leftView = XposedHelpers.getObjectField(XposedHelpers.getObjectField(notificationPanelView, "mKeyguardLeftView"), "mKeyguardMoveLeftView");
//                                } catch (Throwable t) {
//                                    XposedBridge.log(t);
//                                }
//
//                                if (leftView != null) try {
//                                    XposedHelpers.callMethod(leftView, "reloadListItems");
//                                } catch (Throwable t1) {
//                                    try {
//                                        XposedHelpers.callMethod(leftView, "updateShortcuts");
//                                    } catch (Throwable t2) {
//                                        try {
//                                            XposedHelpers.callMethod(leftView, "initKeyguardLeftItems");
//                                        } catch (Throwable t3) {
//                                            try {
//                                                XposedHelpers.callMethod(leftView, "initKeyguardLeftItemInfos");
//                                            } catch (Throwable t4) {
//                                                XposedBridge.log(t4);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.classLoader, "setTranslation", float.class, boolean.class, boolean.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                int mCurrentScreen = XposedHelpers.getIntField(param.thisObject, "mCurrentScreen");
                if (mCurrentScreen != 1) return;
                if ((float)param.args[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) param.args[0] = 0.0f;
                else if ((float)param.args[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off")) param.args[0] = 0.0f;
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.classLoader, "fling", float.class, boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                int mCurrentScreen = XposedHelpers.getIntField(param.thisObject, "mCurrentScreen");
                if (mCurrentScreen != 1) return;
                if ((float)param.args[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) param.setResult(null);
                else if ((float)param.args[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off")) param.setResult(null);
            }
        });

        View.OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int action = MainModule.mPrefs.getInt(v.getTag() + "_action", 1);
                boolean skip = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_skiplock");
                if (!skip && (action == 8 || action == 9 || action == 20))
                    XposedHelpers.callStaticMethod(findClass("com.android.systemui.SystemUICompat", lpparam.classLoader), "dismissKeyguardOnNextActivity");
                GlobalActions.handleAction(v.getContext(), "pref_key_" + v.getTag(), skip);
            }
        };

        class LeftControlCenterHandler extends Handler {

            LeftControlCenterHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (msg.what == 1) try {
                    ViewGroup leftView = (ViewGroup)msg.obj;
                    Context mContext = (Context)XposedHelpers.getObjectField(leftView, "mContext");
                    int listResId = mContext.getResources().getIdentifier("keyguard_move_left", "id", lpparam.packageName);
                    ViewGroup oldList = leftView.findViewById(listResId);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)oldList.getLayoutParams();
                    ScrollView container = new ScrollView(oldList.getContext());
                    container.setVerticalScrollBarEnabled(false);
                    ViewGroup parent = ((ViewGroup)oldList.getParent());
                    LinearLayout leftList = new LinearLayout(oldList.getContext());
                    parent.removeView(oldList);
                    leftList.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
                    leftList.setOrientation(LinearLayout.VERTICAL);

                    try {
                        float density = mContext.getResources().getDisplayMetrics().density;
                        int align = MainModule.mPrefs.getStringAsInt("system_lockscreenshortcuts_left_align", 2);
                        int margin = Math.round(density * 40);
                        lp.topMargin = lp.bottomMargin;
                        if (lp.topMargin < margin) lp.topMargin = margin;
                        if (lp.bottomMargin < margin) lp.bottomMargin = margin;
                        boolean center = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_center");
                        lp.leftMargin = Math.round((center ? 36.33f : 20) * density);
                        lp.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                        if (align == 1)
                            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        else
                            lp.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
                        container.setLayoutParams(lp);
                    } catch (Throwable t) {
                        XposedBridge.log(t);
                    }

                    container.addView(leftList);
                    container.setId(listResId);
                    parent.addView(container);

                    LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    int layoutResId = mContext.getResources().getIdentifier("miui_keyguard_left_view_control_center_item", "layout", lpparam.packageName);
                    if (layoutResId == 0) layoutResId = mContext.getResources().getIdentifier("miui_keyguard_left_view_item", "layout", lpparam.packageName);
                    int imgResId = mContext.getResources().getIdentifier("keyguard_left_list_item_img", "id", lpparam.packageName);
                    int nameResId = mContext.getResources().getIdentifier("keyguard_left_list_item_name", "id", lpparam.packageName);
                    //int numberResId = mContext.getResources().getIdentifier("keyguard_left_list_item_number", "id", lpparam.packageName);
                    int margin = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("keyguard_move_left_item_margin", "dimen", lpparam.packageName));

                    String key = "system_lockscreenshortcuts_left";
                    String itemStr = MainModule.mPrefs.getString(key, "");
                    if (itemStr == null || itemStr.isEmpty()) return;
                    String[] itemArr = itemStr.trim().split("\\|");

                    int i = 0;
                    for (String uuid: itemArr) {
                        LinearLayout item = (LinearLayout)inflater.inflate(layoutResId, leftList, false);
                        item.setTag(key + "_" + uuid);
                        leftList.addView(item);

                        ImageView img = item.findViewById(imgResId);
                        int size = (int)(32 * img.getResources().getDisplayMetrics().density);
                        ViewGroup.LayoutParams lp1 = img.getLayoutParams();
                        lp1.width = size;
                        lp1.height = size;
                        img.setLayoutParams(lp1);
                        Drawable image = Helpers.getActionImage(mContext, "pref_key_" + key + "_" + uuid);
                        img.setImageDrawable(image != null ? image : mContext.getPackageManager().getApplicationIcon(Helpers.modulePkg));
                        img.setBackgroundResource(0);

                        TextView title = item.findViewById(nameResId);
                        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        title.setText(Helpers.getActionName(mContext, "pref_key_" + key + "_" + uuid));

                        LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)item.getLayoutParams();
                        if (i > 0) lp2.topMargin = margin;
                        lp2.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        item.setLayoutParams(lp2);

                        int padding = (int)(14 * img.getResources().getDisplayMetrics().density);
                        item.setPadding(padding, padding, padding, padding);

                        i++;
                        item.setOnClickListener(mListener);
                    }
                } catch (Throwable t) {
                    XposedBridge.log(t);
                }
            }
        }

//        String leftViewCls = "com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView";
//        Helpers.findAndHookConstructor(leftViewCls, lpparam.classLoader, Context.class, AttributeSet.class, new MethodHook() {
//            @Override
//            protected void after(MethodHookParam param) throws Throwable {
//                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//                Handler mHandler = new LeftControlCenterHandler(mContext.getMainLooper());
//                XposedHelpers.setAdditionalInstanceField(param.thisObject, "myHandler", mHandler);
//            }
//        });
//
//        Helpers.findAndHookMethodSilently(leftViewCls, lpparam.classLoader, "updateShortcuts", new MethodHook() {
//            @Override
//            protected void before(final MethodHookParam param) throws Throwable {
//                param.setResult(null);
//                initLeftView(param.thisObject);
//            }
//        });
//        Helpers.findAndHookMethod(leftViewCls, lpparam.classLoader, "onFinishInflate", new MethodHook() {
//            @Override
//            protected void after(MethodHookParam param) throws Throwable {
//                View mSmartHomeLinearLayout = (View)XposedHelpers.getObjectField(param.thisObject, "mSmartHomeImageView");
//                View mRemoteCenterLinearLayout = (View)XposedHelpers.getObjectField(param.thisObject, "mRemoteCenterImageView");
//                final View.OnClickListener mListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mClickListener");
//                View.OnClickListener mNewListener = new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (!handleStockShortcut(view))
//                            if (mListener != null) mListener.onClick(view);
//                    }
//                };
//                mSmartHomeLinearLayout.setOnClickListener(mNewListener);
//                mRemoteCenterLinearLayout.setOnClickListener(mNewListener);
//            }
//        });
    }

    private static boolean handleStockShortcut(View view) {
        if (view == null) return false;
        boolean skip = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_skiplock");
        if (!skip) return false;
        Context context = view.getContext();
        int id = view.getId();
        try {
            if (id == view.getResources().getIdentifier("keyguard_remote_controller_info", "id", context.getPackageName())) {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.duokan.phone.remotecontroller");
                if (intent == null) return false;
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_skiplock")) {
                    intent.putExtra("ShowCameraWhenLocked", true);
                    intent.putExtra("StartActivityWhenLocked", true);
                }
                context.startActivity(intent);
                return true;
            } else if (id == view.getResources().getIdentifier("keyguard_smarthome_info", "id", context.getPackageName())) {
                Intent intent = new Intent();
                intent.setPackage("com.xiaomi.smarthome");
                intent.setData(Uri.parse("http://home.mi.com/main"));
                intent.setAction("android.intent.action.VIEW");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("source", 11);
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_skiplock")) {
                    intent.putExtra("ShowCameraWhenLocked", true);
                    intent.putExtra("StartActivityWhenLocked", true);
                }
                context.startActivity(intent);
                return true;
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        return false;
    }

    public static void LockScreenSecureLaunchHook() {
        Helpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new MethodHook() {
            @SuppressWarnings("ConstantConditions")
            protected void after(MethodHookParam param) throws Throwable {
                Activity act = (Activity)param.thisObject;
                if (act == null) return;
                Intent intent = act.getIntent();
                if (intent == null) return;
                boolean mFromSecureKeyguard = intent.getBooleanExtra("StartActivityWhenLocked", false);
                boolean mStartedFromLockScreen = false;
                try {
                    mStartedFromLockScreen = (boolean)XposedHelpers.getAdditionalInstanceField(act.getApplication(), "wasStartedFromLockScreen");
                } catch (Throwable ignore) {}
                if (mFromSecureKeyguard || mStartedFromLockScreen) {
                    XposedHelpers.setAdditionalInstanceField(act.getApplication(), "wasStartedFromLockScreen", true);
                    act.setShowWhenLocked(true);
                    act.setInheritShowWhenLocked(true);
                }
            }
        });
    }

    private static Handler stateHandler;

    public static class StateRunnable implements Runnable {
        Context context;
        MediaController controller;

        StateRunnable(Context ctx, MediaController ctrl) {
            context = ctx;
            controller = ctrl;
        }

        @Override
        public void run() {
            try {
                if (stateHandler != null) stateHandler.postDelayed(this, 1000);
            } catch (Throwable t) {
                XposedBridge.log(t);
            }
        }
    }

    public static class SeekBarReceiver extends BroadcastReceiver {
        View parent;
        TextView posDur;
        SeekBar seekBar;

        SeekBarReceiver(View view, SeekBar sBar, TextView pos) {
            parent = view;
            posDur = pos;
            seekBar = sBar;
        }

        @Override
        public void onReceive(Context context, Intent intent) {}
    }

    public static class MediaControllerReceiver extends BroadcastReceiver {
        MediaController.TransportControls transportControls;

        MediaControllerReceiver(MediaController.TransportControls transport) {
            transportControls = transport;
        }

        @Override
        public void onReceive(Context context, Intent intent) {}
    }

    private static void sendSeekBarUpdate(Context mContext, MediaController controller) {
        try {
            if (mContext == null) return;
            String pkgName = (String)XposedHelpers.callMethod(controller, "getPackageName");
            Intent intent = new Intent(ACTION_PREFIX + "UpdateMediaPosition:" + pkgName);
            MediaMetadata medaData = controller.getMetadata();
            if (medaData == null) return;
            intent.putExtra(MediaMetadata.METADATA_KEY_DURATION, medaData.getLong(MediaMetadata.METADATA_KEY_DURATION));
            intent.putExtra("android.media.metadata.POSITION", controller.getPlaybackState().getPosition());
            mContext.sendBroadcast(intent);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void MediaNotificationSeekBarHook() {
        MethodHook hook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                stateHandler = new Handler();
                MediaController mController = (MediaController)XposedHelpers.callMethod(param.thisObject, "getController");
                MediaController.TransportControls mTransportControls = (MediaController.TransportControls)XposedHelpers.getObjectField(mController, "mTransportControls");
                MediaControllerReceiver mSeekToReceiver = new MediaControllerReceiver(mTransportControls) {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (transportControls == null) return;
                        long position = intent.getLongExtra("android.media.metadata.POSITION", 0L);
                        transportControls.seekTo(position);
                    }
                };
                StateRunnable mStateRunnable = new StateRunnable((Context)param.args[0], mController) {
                    @Override
                    public void run() {
                        if (context != null && controller != null) try {
                            sendSeekBarUpdate(context, controller);
                            PlaybackState state = controller.getPlaybackState();
                            if (state != null && state.getState() != PlaybackState.STATE_PLAYING) return;
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                        super.run();
                    }
                };
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mSeekToReceiver", mSeekToReceiver);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mStateRunnable", mStateRunnable);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mContext", param.args[0]);
            }
        };

        Helpers.findAndHookConstructor("android.media.session.MediaSession", null, Context.class, String.class, Bundle.class, hook);
        Helpers.findAndHookMethod("android.media.session.MediaSession", null, "setActive", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean mActive = XposedHelpers.getBooleanField(param.thisObject, "mActive");
                boolean newActive = (boolean)param.args[0];
                if (mActive == newActive) return;

                Context mContext = (Context)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mContext");
                if (mContext == null) return;

                MediaControllerReceiver mSeekToReceiver = (MediaControllerReceiver)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSeekToReceiver");
                if (newActive)
                    mContext.registerReceiver(mSeekToReceiver, new IntentFilter(ACTION_PREFIX + "SeekToMediaPosition:" + mContext.getPackageName()));
                else
                    mContext.unregisterReceiver(mSeekToReceiver);
            }
        });

        Helpers.findAndHookMethod("android.media.session.MediaSession", null, "setPlaybackState", PlaybackState.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mContext");
                if (mContext == null) return;
                MediaController mController = (MediaController)XposedHelpers.callMethod(param.thisObject, "getController");
                if (mController == null) return;
                StateRunnable mStateRunnable = (StateRunnable)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mStateRunnable");
                if (mStateRunnable == null) return;

                stateHandler.removeCallbacks(mStateRunnable);
                if (((PlaybackState)param.args[0]).getState() == PlaybackState.STATE_PLAYING)
                    stateHandler.postDelayed(mStateRunnable, 100);
                else
                    sendSeekBarUpdate(mContext, mController);
            }
        });
    }

    public static void Network4GtoLTEHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.MobileSignalController", lpparam.classLoader, "getMobileTypeName", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                String net = (String)param.getResult();
                if ("4G".equals(net)) param.setResult("LTE");
                else if ("4G+".equals(net)) param.setResult("LTE+");
            }
        });
    }

    private static TextView createBatteryDetailView(Context mContext, LinearLayout.LayoutParams lp) {
        TextView batteryView = (TextView) LayoutInflater.from(mContext).inflate(statusbarTextIconLayoutResId, null);
        XposedHelpers.setObjectField(batteryView, "mVisibilityByDisableInfo", 0);
        XposedHelpers.setObjectField(batteryView, "mVisibleByController", true);
        XposedHelpers.setObjectField(batteryView, "mShown", true);
        XposedHelpers.setAdditionalInstanceField(batteryView, "mCustomSlot", "battery_detail");
        Resources res = mContext.getResources();
        int styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui");
        batteryView.setTextAppearance(styleId);
        float fontSize = MainModule.mPrefs.getInt("system_statusbar_batterytempandcurrent_fontsize", 16) * 0.5f;
        int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_content", 1);
        if (opt == 1) {
            batteryView.setLineSpacing(0, fontSize > 8.5f ? 0.85f : 0.9f);
            batteryView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        batteryView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        int leftMargin = MainModule.mPrefs.getInt("system_statusbar_batterytempandcurrent_leftmargin", 8);
        leftMargin = (int)TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            leftMargin * 0.5f,
            res.getDisplayMetrics()
        );
        int rightMargin = MainModule.mPrefs.getInt("system_statusbar_batterytempandcurrent_rightmargin", 8);
        rightMargin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            rightMargin * 0.5f,
            res.getDisplayMetrics()
        );
        batteryView.setPaddingRelative(leftMargin, 0, rightMargin, 0);
        batteryView.setLayoutParams(lp);
        return batteryView;
    }
    static final ArrayList<TextView> mBatteryDetailViews = new ArrayList<TextView>();

    private static void updateTempAndCurrent(Class<?> ChargeUtilsClass) {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable refreshRunner = new Runnable() {
            @Override
            public void run() {
                String batteryInfo = "";
                FileInputStream fis = null;
                Properties props = null;
                boolean showInfo = true;
                if (MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_incharge") && ChargeUtilsClass != null) {
                    Object batteryStatus = Helpers.getStaticObjectFieldSilently(ChargeUtilsClass, "sBatteryStatus");
                    if (batteryStatus == null) {
                        showInfo = false;
                    }
                    else {
                        showInfo = (boolean) XposedHelpers.callMethod(batteryStatus, "isCharging");
                    }
                }
                if (showInfo) {
                    try {
                        fis = new FileInputStream("/sys/class/power_supply/battery/uevent");
                        props = new Properties();
                        props.load(fis);
                    }
                    catch (Throwable ign) {}
                    finally {
                        try {
                            fis.close();
                        }
                        catch (Throwable ign) {}
                    }
                    if (props != null) {
                        int tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"));
                        int currVal = Math.abs(Math.round(Integer.parseInt(props.getProperty("POWER_SUPPLY_CURRENT_NOW")) / 1000));
                        int opt = MainModule.mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_content", 1);
                        if (opt == 1) {
                            batteryInfo = tempVal / 10f + "℃" + "\n" + currVal + "mA";
                        }
                        else if (opt == 2) {
                            batteryInfo = tempVal / 10f + "℃";
                        }
                        else {
                            batteryInfo = currVal + "mA";
                        }
                    }
                }
                if (!showInfo || batteryInfo.isEmpty()) {
                    for (TextView tv:mBatteryDetailViews) {
                        tv.setVisibility(View.GONE);
                    }
                }
                else {
                    for (TextView tv:mBatteryDetailViews) {
                        tv.setVisibility(View.VISIBLE);
                        XposedHelpers.callMethod(tv, "setNetworkSpeed", batteryInfo);
                    }
                }
                handler.postDelayed(this, 1500);
            }
        };
        handler.post(refreshRunner);
    }
    private static int statusbarTextIconLayoutResId = 0;
    public static void setupStatusBar(LoadPackageParam lpparam) {
        statusbarTextIconLayoutResId = MainModule.resHooks.addResource("statusbar_text_icon", R.layout.statusbar_text_icon);
    }
    public static void DisplayBatteryDetailHook(LoadPackageParam lpparam) {
        Class <?> ChargeUtilsClass = findClass("com.android.keyguard.charge.ChargeUtils", lpparam.classLoader);
        Class <?> DarkIconDispatcherClass = XposedHelpers.findClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.classLoader);
        Class <?> Dependency = findClass("com.android.systemui.Dependency", lpparam.classLoader);
        Class <?> StatusBarIconHolder = XposedHelpers.findClass("com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.classLoader);
        boolean atRight = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright");
        if (atRight) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
                private boolean isHooked = false;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object iconController = XposedHelpers.getObjectField(param.thisObject, "mStatusBarIconController");
                    int slotIndex = (int) XposedHelpers.callMethod(iconController, "getSlotIndex", "battery_detail");
                    Object iconHolder = XposedHelpers.callMethod(iconController, "getIcon", slotIndex, 0);
                    if (iconHolder == null) {
                        iconHolder = XposedHelpers.newInstance(StatusBarIconHolder);
                        XposedHelpers.setObjectField(iconHolder, "mType", 91);
                        XposedHelpers.callMethod(iconController, "setIcon", slotIndex, iconHolder);
                    }
                    if (!isHooked) {
                        isHooked = true;
                        updateTempAndCurrent(ChargeUtilsClass);
                    }
                }
            });

            Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconController$IconManager", lpparam.classLoader, "addHolder", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    if (param.args.length != 4) return;
                    Object iconHolder = param.args[3];
                    int type = (int) XposedHelpers.callMethod(iconHolder, "getType");
                    if (type == 91) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) XposedHelpers.callMethod(param.thisObject, "onCreateLayoutParams");
                        TextView batteryView = createBatteryDetailView(mContext, lp);
                        int i = (int) param.args[0];
                        ViewGroup mGroup = (ViewGroup) XposedHelpers.getObjectField(param.thisObject, "mGroup");
                        mGroup.addView(batteryView, i);
                        mBatteryDetailViews.add(batteryView);
                        param.setResult(batteryView);
                    }
                }
            });
            Class <?> NetworkSpeedViewClass = XposedHelpers.findClass("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader);
            Helpers.findAndHookMethod(NetworkSpeedViewClass, "getSlot", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    Object customSlot = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomSlot");
                    if (customSlot != null) {
                        param.setResult(customSlot);
                    }
                }
            });
        }
        else {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "initMiuiViewsOnViewCreated", View.class, new MethodHook() {
                private boolean isHooked = false;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getContext");
                    TextView mSplitter = (TextView) XposedHelpers.getObjectField(param.thisObject, "mDripNetworkSpeedSplitter");
                    ViewGroup batteryViewContainer = (ViewGroup) mSplitter.getParent();
                    int bvIndex = batteryViewContainer.indexOfChild(mSplitter);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mSplitter.getLayoutParams();
                    TextView batteryView = createBatteryDetailView(mContext, lp);
                    batteryViewContainer.addView(batteryView, bvIndex + 1);
                    mBatteryDetailViews.add(batteryView);
                    Object DarkIconDispatcher = XposedHelpers.callStaticMethod(Dependency, "get", DarkIconDispatcherClass);
                    XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", batteryView);
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mBatteryView", batteryView);
                    if (!isHooked) {
                        isHooked = true;
                        updateTempAndCurrent(ChargeUtilsClass);
                    }
                }
            });
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "showClock", boolean.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object bv = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryView");
                    if (bv != null) {
                        TextView batteryView = (TextView) bv;
                        batteryView.setVisibility(View.VISIBLE);
                    }
                }
            });
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "hideClockInternal", int.class, boolean.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object bv = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryView");
                    if (bv != null) {
                        TextView batteryView = (TextView) bv;
                        batteryView.setVisibility((Integer) param.args[0]);
                    }
                }
            });
        }
    }

    public static void StatusBarIconsAtRightHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiDripLeftStatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String slot = (String) param.args[0];
                if (("alarm_clock".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_alarm_atright"))
                    || ("volume".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_sound_atright"))
                    || ("zen".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_dnd_atright"))
                    || ("nfc".equals(slot) && MainModule.mPrefs.getBoolean("system_statusbar_nfc_atright"))
                ) {
                    param.args[1] = false;
                }
            }
        });

        final boolean[] isListened = {false};
        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isListened[0]) {
                    isListened[0] = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    Class <?> MiuiEndIconManager = findClass("com.android.systemui.statusbar.phone.MiuiEndIconManager", lpparam.classLoader);
                    Object blockList = Helpers.getStaticObjectFieldSilently(MiuiEndIconManager, "RIGHT_BLOCK_LIST");
                    ArrayList rightBlockList;
                    Resources res = mContext.getResources();
                    if (blockList != null) {
                        rightBlockList = (ArrayList<String>) blockList;
                    }
                    else {
                        int blockResId = res.getIdentifier("config_drip_right_block_statusBarIcons", "array", lpparam.packageName);
                        rightBlockList = new ArrayList<String>(Arrays.asList(res.getStringArray(blockResId)));
                    }
                    if (MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atright")) {
                        rightBlockList.remove("network_speed");
                    }
                    if (MainModule.mPrefs.getBoolean("system_statusbar_alarm_atright")) {
                        rightBlockList.remove("alarm_clock");
                    }
                    if (MainModule.mPrefs.getBoolean("system_statusbar_sound_atright")) {
                        rightBlockList.remove("volume");
                    }
                    if (MainModule.mPrefs.getBoolean("system_statusbar_dnd_atright")) {
                        rightBlockList.remove("zen");
                    }
                    if (MainModule.mPrefs.getBoolean("system_statusbar_btbattery_atright")) {
                        rightBlockList.remove("bluetooth_handsfree_battery");
                    }
                    if (MainModule.mPrefs.getBoolean("system_statusbar_nfc_atright")) {
                        rightBlockList.remove("nfc");
                    }
                    if (blockList != null) {
                        XposedHelpers.setStaticObjectField(MiuiEndIconManager, "RIGHT_BLOCK_LIST", rightBlockList);
                    }
                    else {
                        MainModule.resHooks.setObjectReplacement(lpparam.packageName, "array", "config_drip_right_block_statusBarIcons", rightBlockList.toArray(new String[0]));
                    }
                }
            }
        });

        if (MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atright")) {
            Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "updateCutoutLocation", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.thisObject, "mCurrentStatusBarType");
                    if (mCurrentStatusBarType == 1) {
                        Object mDripNetworkSpeedView = XposedHelpers.getObjectField(param.thisObject, "mDripNetworkSpeedView");
                        XposedHelpers.callMethod(mDripNetworkSpeedView, "setBlocked", true);
                    }
                }
            });
            Helpers.hookAllMethods("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, "setDripNetworkSpeedView", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.args[0] = null;
                }
            });
        }
    }

    public static void StatusBarClockAtRightHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                FrameLayout sbView = (FrameLayout) param.thisObject;
                Context mContext = sbView.getContext();
                Resources res = mContext.getResources();
                TextView mClockView = (TextView) XposedHelpers.getObjectField(param.thisObject, "mMiuiClock");
                ((ViewGroup)mClockView.getParent()).removeView(mClockView);
                int contentId = res.getIdentifier("status_bar_contents", "id", lpparam.packageName);
                LinearLayout mContentsContainer = sbView.findViewById(contentId);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_clock_leftmargin", 6);
                lp.leftMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    leftMargin * 0.5f,
                    res.getDisplayMetrics()
                );
                mContentsContainer.addView(mClockView, lp);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "updateCutoutLocation", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int mCurrentStatusBarType = (int) XposedHelpers.getObjectField(param.thisObject, "mCurrentStatusBarType");
                if (mCurrentStatusBarType == 0) {
                    View mSystemIconArea = (View) XposedHelpers.getObjectField(param.thisObject, "mSystemIconArea");
                    LinearLayout.LayoutParams mSystemIconAreaLp = (LinearLayout.LayoutParams) mSystemIconArea.getLayoutParams();
                    mSystemIconAreaLp.width = 0;
                    mSystemIconAreaLp.weight = 1.0f;
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private static TextView mPct = null;
    private static void initPct(ViewGroup container, int source, Context context) {
        Resources res = context.getResources();
        if (mPct == null) {
            mPct = new TextView(container.getContext());
            mPct.setTag("mirrorBrightnessPct");
            mPct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            mPct.setGravity(Gravity.CENTER);
            mPct.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            float density = res.getDisplayMetrics().density;
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Math.round(MainModule.mPrefs.getInt("system_showpct_top", 26) * density);
            lp.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
            mPct.setPadding(Math.round(20 * density), Math.round(10 * density), Math.round(18 * density), Math.round(12 * density));
            mPct.setLayoutParams(lp);
            container.addView(mPct);
        }
        mPct.setTag(source);
        int panelResId = res.getIdentifier("panel_round_corner_bg", "drawable", "com.android.systemui");
        mPct.setBackground(res.getDrawable(panelResId, context.getTheme()));
        mPct.setAlpha(0.0f);
        mPct.setVisibility(View.GONE);
    }

    public static void BrightnessPctHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.classLoader, "showMirror", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                ViewGroup mStatusBarWindow = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mStatusBarWindow");
                if (mStatusBarWindow == null) {
                    Helpers.log("BrightnessPctHook", "mStatusBarWindow is null");
                    return;
                }
                initPct(mStatusBarWindow, 1, mStatusBarWindow.getContext());
                mPct.setVisibility(View.VISIBLE);
                mPct.animate().alpha(1.0f).setDuration(300).start();
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.classLoader, "hideMirror", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (mPct != null) mPct.setVisibility(View.GONE);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.classLoader, "onStart", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Object mMirror = XposedHelpers.getObjectField(param.thisObject, "mControl");
                Object controlCenterWindowViewController = XposedHelpers.getObjectField(mMirror, "controlCenterWindowViewController");
                Object windowView = XposedHelpers.callMethod(controlCenterWindowViewController, "getView");
                if (windowView == null) {
                    Helpers.log("BrightnessPctHook", "mControlPanelContentView is null");
                    return;
                }
                initPct((ViewGroup) windowView, 1, mContext);
                mPct.setVisibility(View.VISIBLE);
                mPct.animate().alpha(1.0f).setDuration(300).start();
            }
        });

        Helpers.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.classLoader, "onStop", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (mPct != null) mPct.setVisibility(View.GONE);
            }
        });

        final Class<?> BrightnessUtils = XposedHelpers.findClassIfExists("com.android.systemui.controlcenter.policy.BrightnessUtils", lpparam.classLoader);
        Helpers.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.classLoader, "onChanged", new MethodHook() {
            @Override
            @SuppressLint("SetTextI18n")
            protected void after(final MethodHookParam param) throws Throwable {
                if (mPct == null || (int)mPct.getTag() != 1) return;
                int currentLevel = (int)param.args[3];
                if (BrightnessUtils != null) {
                    int maxLevel = (int) XposedHelpers.getStaticObjectField(BrightnessUtils, "GAMMA_SPACE_MAX");
                    mPct.setText(((currentLevel * 100) / maxLevel) + "%");
                }
            }
        });
    }

    public static void HideProximityWarningHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.classLoader, "showHint", XC_MethodReplacement.DO_NOTHING);
    }

    public static void HideLockScreenClockHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "setKeyguardStatusViewVisibility", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                View mKeyguardClockView = (View)XposedHelpers.getObjectField(param.thisObject, "mKeyguardClockView");
                if (mKeyguardClockView == null) {
                    Helpers.log("HideLockScreenClockHook", "mKeyguardClockView is null");
                    return;
                }
                ((ViewGroup)mKeyguardClockView).getChildAt(0).setVisibility(View.INVISIBLE);
                mKeyguardClockView.animate().cancel();
                XposedHelpers.setBooleanField(param.thisObject, "mKeyguardStatusViewAnimating", false);
                mKeyguardClockView.setAlpha(0.0f);
                mKeyguardClockView.setVisibility(View.INVISIBLE);
            }
        });
    }

    public static void FirstVolumePressHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.audio.AudioService$VolumeController", lpparam.classLoader, "suppressAdjustment", int.class, int.class, boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int streamType = (int)param.args[0];
                if (streamType != AudioManager.STREAM_MUSIC) return;
                boolean isMuteAdjust = (boolean)param.args[2];
                if (isMuteAdjust) return;
                Object mController = XposedHelpers.getObjectField(param.thisObject, "mController");
                if (mController == null) return;
                param.setResult(false);
            }
        });
    }

    public static void HideThemeBackgroundBrightnessHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.classLoader, "showMirror", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object mNotificationPanel = XposedHelpers.getObjectField(param.thisObject, "mNotificationPanel");
                if (mNotificationPanel == null) return;
                View mThemeBackgroundView = (View)XposedHelpers.getObjectField(mNotificationPanel, "mThemeBackgroundView");
                if (mThemeBackgroundView != null) mThemeBackgroundView.setVisibility(View.GONE);
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.classLoader, "hideMirror", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mNotificationPanel = XposedHelpers.getObjectField(param.thisObject, "mNotificationPanel");
                if (mNotificationPanel == null) return;
                View mThemeBackgroundView = (View)XposedHelpers.getObjectField(mNotificationPanel, "mThemeBackgroundView");
                if (mThemeBackgroundView != null) mThemeBackgroundView.setVisibility(View.VISIBLE);
            }
        });
    }

    public static void NoSignatureVerifyHook() {
        Helpers.hookAllMethods("android.content.pm.PackageParser.SigningDetails", null, "checkCapability", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethods("android.content.pm.PackageParser.SigningDetails", null, "checkCapabilityRecover", XC_MethodReplacement.returnConstant(true));
    }

    public static void NoSignatureVerifyServiceHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethodsSilently("com.miui.server.SecurityManagerService", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethodsSilently("com.miui.server.SecurityManagerService", lpparam.classLoader, "checkSysAppCrack", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(0));
        Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "matchSignaturesCompat", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "matchSignaturesRecover", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethodsSilently("miui.util.CertificateUtils", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(true));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void NoSignatureVerifyMiuiHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethodSilently("com.android.packageinstaller.InstallAppProgress.a", lpparam.classLoader, "a", Boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });

        Helpers.findAndHookMethodSilently("com.android.packageinstaller.InstallAppProgress.a", lpparam.classLoader, "onPostExecute", Boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });

        Helpers.findAndHookMethodSilently("com.android.packageinstaller.IncrementInstallProgress.a", lpparam.classLoader, "a", Boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });

        Helpers.findAndHookMethodSilently("com.android.packageinstaller.IncrementInstallProgress.a", lpparam.classLoader, "onPostExecute", Boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });
    }

    public static void ScreenDimTimeHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "readConfigurationLocked", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                float opt = MainModule.mPrefs.getInt("system_dimtime", 0) / 100f;
                XposedHelpers.setIntField(param.thisObject, "mMaximumScreenDimDurationConfig", 600000);
                XposedHelpers.setFloatField(param.thisObject, "mMaximumScreenDimRatioConfig", opt);
//				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//				int minScreenOffTimeout = mContext.getResources().getInteger(mContext.getResources().getIdentifier("config_minimumScreenOffTimeout", "integer", "android"));
            }
        });

//		Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "getScreenOffTimeoutLocked", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				//XposedBridge.log("getScreenOffTimeoutLocked (mSleepTimeoutSetting): " + param.args[0]);
//				//XposedBridge.log("mMaximumScreenOffTimeoutFromDeviceAdmin: " + XposedHelpers.getLongField(param.thisObject, "mMaximumScreenOffTimeoutFromDeviceAdmin"));
//				XposedBridge.log("mUserActivityTimeoutOverrideFromWindowManager: " + XposedHelpers.getLongField(param.thisObject, "mUserActivityTimeoutOverrideFromWindowManager"));
//			}
//		});
//
//		Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "updateUserActivitySummaryLocked", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				if ((int)param.args[1] == 1) return;
//				XposedBridge.log("updateUserActivitySummaryLocked: " + param.args[0] + ", " + param.args[1]);
//			}
//		});
    }

    public static void NoOverscrollHook() {
        Helpers.findAndHookMethod("android.widget.AbsListView", null, "initAbsListView", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                ((AbsListView)param.thisObject).setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        });
    }

    public static void NoOverscrollAppHook(LoadPackageParam lpparam) {
        MethodHook hookParam = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = false;
            }
        };

        Class<?> sblCls = findClassIfExists("miuix.springback.view.SpringBackLayout", lpparam.classLoader);
        if (sblCls != null) {
            Helpers.hookAllConstructors(sblCls, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    try {
                        XposedHelpers.callMethod(param.thisObject, "setSpringBackEnable", false);
                    } catch (Throwable t) {
                        try { XposedHelpers.setBooleanField(param.thisObject, "mSpringBackEnable", false); } catch (Throwable ignore) {}
                    }
                }
            });
            //noinspection ResultOfMethodCallIgnored
            Helpers.findAndHookMethodSilently(sblCls, "setSpringBackEnable", boolean.class, hookParam);
        }

        Class<?> rrvCls = findClassIfExists("androidx.recyclerview.widget.RemixRecyclerView", lpparam.classLoader);
        if (rrvCls != null) {
            Helpers.hookAllConstructors(rrvCls, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    ((View)param.thisObject).setOverScrollMode(View.OVER_SCROLL_NEVER);
                    try {
                        XposedHelpers.callMethod(param.thisObject, "setSpringEnabled", false);
                    } catch (Throwable t) {
                        try { XposedHelpers.setBooleanField(param.thisObject, "mSpringEnabled", false); } catch (Throwable ignore) {}
                    }
                }
            });
            //noinspection ResultOfMethodCallIgnored
            Helpers.findAndHookMethodSilently(rrvCls, "setSpringEnabled", boolean.class, hookParam);
        }
    }

    private static Dialog mDialog = null;
    private static float blurCollapsed = 0.0f;
    private static float blurExpanded = 0.0f;

    @SuppressWarnings("deprecation")
    private static void updateBlurRatio(Object thisObject) {
        if (mDialog == null || mDialog.getWindow() == null) return;
        View rootView = mDialog.getWindow().getDecorView();
        if (rootView.isAttachedToWindow() && rootView.getLayoutParams() instanceof WindowManager.LayoutParams) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)rootView.getLayoutParams();
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            boolean isExpanded = (boolean)XposedHelpers.callMethod(thisObject, "isExpanded");
            XposedHelpers.setFloatField(layoutParams, "blurRatio", isExpanded ? blurExpanded : blurCollapsed);
            mDialog.getWindow().getWindowManager().updateViewLayout(rootView, layoutParams);
        }
    }

    public static void BlurVolumeDialogBackgroundHook(ClassLoader classLoader) {
        Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogView", classLoader, "onAttachedToWindow", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                updateBlurRatio(param.thisObject);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogView", classLoader, "onExpandStateUpdated", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                updateBlurRatio(param.thisObject);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "initDialog", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = new Handler(mContext.getMainLooper());

                mDialog = (Dialog)XposedHelpers.getObjectField(param.thisObject, "mDialog");
                blurCollapsed = MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) / 100f;
                blurExpanded = MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) / 100f;
                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if (key.equals("pref_key_system_volumeblur_collapsed")) blurCollapsed = Helpers.getSharedIntPref(mContext, key, 0) / 100f;
                            if (key.equals("pref_key_system_volumeblur_expanded")) blurExpanded = Helpers.getSharedIntPref(mContext, key, 0) / 100f;
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });
    }

    public static void BlurVolumeDialogBackgroundRes() {
        MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "fraction", "miui_volume_dim_behind_collapsed", 0.0f);
        MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "fraction", "miui_volume_dim_behind_expanded", 0.0f);
    }

    public static void RemoveSecureHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.wm.WindowState", lpparam.classLoader, "isSecureLocked", XC_MethodReplacement.returnConstant(false));
    }

    public static void RemoveActStartConfirmHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethodsSilently("com.miui.server.SecurityManagerService", lpparam.classLoader, "checkAllowStartActivity", XC_MethodReplacement.returnConstant(true));
    }

    public static void AllowAllKeyguardHook(LoadPackageParam lpparam) {
        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.notification.MiuiNotificationCompat", lpparam.classLoader, "isEnableKeyguard", Notification.class, XC_MethodReplacement.returnConstant(true));
    }

    public static void AllowAllKeyguardSysHook() {
        Helpers.findAndHookMethod(MiuiNotification.class, "setEnableKeyguard", boolean.class, new MethodHook() {
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });

        Helpers.findAndHookMethod(MiuiNotification.class, "isEnableKeyguard", XC_MethodReplacement.returnConstant(true));
    }

    public static void AllowAllFloatHook(LoadPackageParam lpparam) {
        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.notification.MiuiNotificationCompat", lpparam.classLoader, "isEnableFloat", Notification.class, XC_MethodReplacement.returnConstant(true));
    }

    public static void AllowAllFloatSysHook() {
        Helpers.findAndHookMethod(MiuiNotification.class, "setEnableFloat", boolean.class, new MethodHook() {
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });

        Helpers.findAndHookMethod(MiuiNotification.class, "isEnableFloat", XC_MethodReplacement.returnConstant(true));
    }

    public static void AllowDirectReplyHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setLockScreenAllowRemoteInput", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });
    }

    public static void HideQSHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, "onStateChanged", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object mNotificationPanel = XposedHelpers.getObjectField(param.thisObject, "mQSContainer");
                if ((int)param.args[0] == 1) {
                    XposedHelpers.callMethod(mNotificationPanel, "setShowQSPanel", false);
                } else {
                    Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            XposedHelpers.callMethod(mNotificationPanel, "setShowQSPanel", true);
                        }
                    }, 300);
                }
            }
        });
    }

    public static void RemoveDrawerBackgroundHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "drawChild", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mStatusBarStateOrig", XposedHelpers.getIntField(param.thisObject, "mStatusBarState"));
                XposedHelpers.setIntField(param.thisObject, "mStatusBarState", 0);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mKeyguardShowingOrig", XposedHelpers.getBooleanField(param.thisObject, "mKeyguardShowing"));
                XposedHelpers.setBooleanField(param.thisObject, "mKeyguardShowing", false);
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                XposedHelpers.setIntField(param.thisObject, "mStatusBarState", (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mStatusBarStateOrig"));
                XposedHelpers.setBooleanField(param.thisObject, "mKeyguardShowing", (boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mKeyguardShowingOrig"));
            }
        });
    }

    public static void LockScreenTimeoutHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarWindowManager", lpparam.classLoader, "applyUserActivityTimeout", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mLpChanged = XposedHelpers.getObjectField(param.thisObject, "mLpChanged");
                if (mLpChanged == null) return;
                long userActivityTimeout = XposedHelpers.getLongField(mLpChanged, "userActivityTimeout");
                if (userActivityTimeout > 0)
                    XposedHelpers.setLongField(mLpChanged, "userActivityTimeout", MainModule.mPrefs.getInt("system_lstimeout", 9) * 1000L);
            }
        });
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat("H:m", Locale.ENGLISH);
    public static void MuffledVibrationHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.VibratorService", lpparam.classLoader, "systemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = new Handler(mContext.getMainLooper());
                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if (key.contains("pref_key_system_vibration_amp_")) MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 100));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "doVibratorOn", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                float ratio_ringer = MainModule.mPrefs.getInt("system_vibration_amp_ringer", 100) / 100f;
                float ratio_notif = MainModule.mPrefs.getInt("system_vibration_amp_notif", 100) / 100f;
                float ratio_other = MainModule.mPrefs.getInt("system_vibration_amp_other", 100) / 100f;

                boolean isRingtone = false;
                boolean isNotification = false;
                Object mCurrentVibration = XposedHelpers.getObjectField(param.thisObject, "mCurrentVibration");
                if (mCurrentVibration != null) try {
                    isRingtone = (boolean)XposedHelpers.callMethod(mCurrentVibration, "isRingtone");
                    isNotification = (boolean)XposedHelpers.callMethod(mCurrentVibration, "isNotification");
                } catch (Throwable t) {
                    int mUsageHint = XposedHelpers.getIntField(mCurrentVibration, "mUsageHint");
                    isRingtone = mUsageHint == 6;
                    isNotification = mUsageHint == 5 || mUsageHint == 7 || mUsageHint == 8 || mUsageHint == 9;
                }

                float ratio;
                if (isRingtone) ratio = ratio_ringer;
                else if (isNotification) ratio = ratio_notif;
                else ratio = ratio_other;
                if (ratio == 1.0f) return;

                String key = "system_vibration_amp_period";
                int start_hour = MainModule.mPrefs.getInt(key + "_start_hour", 0);
                int start_minute = MainModule.mPrefs.getInt(key + "_start_minute", 0);
                int end_hour = MainModule.mPrefs.getInt(key + "_end_hour", 0);
                int end_minute = MainModule.mPrefs.getInt(key + "_end_minute", 0);

                formatter.setTimeZone(TimeZone.getDefault());
                Date start = formatter.parse(start_hour + ":" + start_minute);
                Date end = formatter.parse(end_hour + ":" + end_minute);
                Date now = formatter.parse(formatter.format(new Date()));

                boolean insidePeriod = start.before(end) ? now.after(start) && now.before(end) : now.before(end) || now.after(start);
                if (!insidePeriod) return;

                boolean mSupportsAmplitudeControl = false;
                try {
                    mSupportsAmplitudeControl = XposedHelpers.getBooleanField(param.thisObject, "mSupportsAmplitudeControl");
                } catch (Throwable ignored) {}

                if (mSupportsAmplitudeControl)
                    param.args[1] = Math.round(((int)param.args[1] == -1 ? XposedHelpers.getIntField(param.thisObject, "mDefaultVibrationAmplitude") : (int)param.args[1]) * ratio);
                else
                    param.args[0] = Math.max(3, (long)Math.round((long)param.args[0] * ratio));
            }
        });

//		if (Helpers.isNougat())
//		Helpers.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "vibratePattern", new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//
//			}
//		});
    }

    public static void ResizableWidgetsHook() {
        Helpers.findAndHookMethod("android.appwidget.AppWidgetHostView", null, "getAppWidgetInfo", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo)param.getResult();
                if (widgetInfo == null) return;
                widgetInfo.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
                widgetInfo.minHeight = 0;
                widgetInfo.minWidth = 0;
                widgetInfo.minResizeHeight = 0;
                widgetInfo.minResizeWidth = 0;
                param.setResult(widgetInfo);
            }
        });

        Helpers.findAndHookMethod("android.appwidget.AppWidgetManager", null, "getAppWidgetInfo", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo)param.getResult();
                if (widgetInfo == null) return;
                widgetInfo.resizeMode = AppWidgetProviderInfo.RESIZE_BOTH;
                widgetInfo.minHeight = 0;
                widgetInfo.minWidth = 0;
                widgetInfo.minResizeHeight = 0;
                widgetInfo.minResizeWidth = 0;
                param.setResult(widgetInfo);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private static void hookUpdateTime(Object thisObject, boolean isSingle) {
        try {
            TextView mCurrentDate = null;
            TextView mCurrentDateLarge = null;
            if (isSingle) {
                try { mCurrentDate = (TextView)XposedHelpers.getObjectField(thisObject, "mCurrentDate"); } catch (Throwable ignore) {}
                try { mCurrentDateLarge = (TextView)XposedHelpers.getObjectField(thisObject, "mCurrentDateLarge"); } catch (Throwable ignore) {}
            }
            else {
                try { mCurrentDate = (TextView)XposedHelpers.getObjectField(thisObject, "mLocalDate"); } catch (Throwable ignore) {}
            }
            if (mCurrentDate == null && mCurrentDateLarge == null) return;
            Context mContext = mCurrentDate != null ? mCurrentDate.getContext() : mCurrentDateLarge.getContext();

            long timestamp = Helpers.getNextMIUIAlarmTime(mContext);
            if (timestamp == 0 && MainModule.mPrefs.getBoolean("system_lsalarm_all"))
                timestamp = Helpers.getNextStockAlarmTime(mContext);
            if (timestamp == 0) return;

            StringBuilder alarmStr = new StringBuilder();
            alarmStr.append("\n").append(Helpers.getModuleRes(mContext).getString(R.string.system_statusbaricons_alarm_title)).append(" ");
            int format = MainModule.mPrefs.getStringAsInt("system_lsalarm_format", 1);
            if (format == 1 || format == 3) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(mContext) ? "EHmm" : "EHmma"), Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getDefault());
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(timestamp);
                alarmStr.append(dateFormat.format(calendar.getTime()));
            }
            if (format == 2 || format == 3) {
                StringBuilder timeStr = new StringBuilder(DateUtils.getRelativeTimeSpanString(timestamp, currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
                timeStr.setCharAt(0, Character.toLowerCase(timeStr.charAt(0)));
                alarmStr.append(format == 3 ? " (" + timeStr + ")" : timeStr);
            }
            if (mCurrentDate != null) {
                mCurrentDate.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
                mCurrentDate.setLineSpacing(0, 1.5f);
                mCurrentDate.append(alarmStr);
                if (isSingle) {
                    int pos = Settings.System.getInt(mContext.getContentResolver(), "selected_keyguard_clock_position", 0);
                    if (pos != 2 && pos != 4) mCurrentDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
            }
            if (mCurrentDateLarge != null) {
                int resId = mCurrentDateLarge.getResources().getIdentifier("miui_clock_date_text_size", "dimen", "com.android.systemui");
                int fontSize = resId == 0 ? Math.round(mCurrentDateLarge.getResources().getDisplayMetrics().density * 14.0f) : mCurrentDateLarge.getResources().getDimensionPixelSize(resId);
                alarmStr.insert(1, "\n\n ");
                SpannableString span = new SpannableString(alarmStr);
                span.setSpan(new AbsoluteSizeSpan(fontSize, false), 0, alarmStr.length(), 0);
                span.setSpan(new TypefaceSpan("sans-serif"), 0, alarmStr.length(), 0);
                mCurrentDateLarge.append(span);
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void LockScreenAlaramHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                new Helpers.SharedPrefObserver(mContext, new Handler(mContext.getMainLooper())) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if ("pref_key_system_lsalarm_all".equals(key)) MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(mContext, key, false));
                            if ("pref_key_system_lsalarm_format".equals(key)) MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, "1"));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.findAndHookMethod("com.android.keyguard.clock.MiuiKeyguardSingleClock", lpparam.classLoader, "updateTime", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mMiuiBaseClock = XposedHelpers.getObjectField(param.thisObject, "mMiuiBaseClock");
                if (mMiuiBaseClock != null) hookUpdateTime(mMiuiBaseClock, true);
            }
        });
        Helpers.findAndHookMethod("com.android.keyguard.clock.MiuiKeyguardDualClock", lpparam.classLoader, "updateTime", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mMiuiDualClock = XposedHelpers.getObjectField(param.thisObject, "mMiuiDualClock");
                if (mMiuiDualClock != null) hookUpdateTime(mMiuiDualClock, false);
            }
        });
    }

    private static boolean isSlidingStart = false;
    private static boolean isSliding = false;
    private static float tapStartX = 0;
    private static float tapStartY = 0;
    private static float tapStartPointers = 0;
    private static float tapStartBrightness = 0;
    private static float tapCurrentBrightness = 0;
    private static float topMinimumBacklight = 0.0f;
    private static float topMaximumBacklight = 1.0f;
    private static float currentTouchX = 0;
    private static long currentTouchTime = 0;

    public static void StatusBarGesturesHook(LoadPackageParam lpparam) {

        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "makeStatusBarView", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                new Helpers.SharedPrefObserver(mContext, new Handler(mContext.getMainLooper())) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if ("pref_key_system_statusbarcontrols_single".equals(key) || "pref_key_system_statusbarcontrols_dual".equals(key))
                                MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, "1"));
                            else if ("pref_key_system_statusbarcontrols_sens_bright".equals(key) || "pref_key_system_statusbarcontrols_sens_vol".equals(key))
                                MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, "2"));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, "setExpandedHeightInternal", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                float mExpandedFraction = (float) XposedHelpers.callMethod(param.thisObject, "getExpandedFraction");
                if (mExpandedFraction > 0.33f) {
                    currentTouchTime = 0;
                    currentTouchX = 0;
                }
            }
        });

        final Class<?> BrightnessUtils = XposedHelpers.findClassIfExists("com.android.systemui.controlcenter.policy.BrightnessUtils", lpparam.classLoader);

        MethodHook hook = new MethodHook() {
            Object mBrightnessController = null;
            private int sbHeight = 0;
            @Override
            @SuppressLint("SetTextI18n")
            protected void before(final MethodHookParam param) throws Throwable {
                String clsName = param.thisObject.getClass().getSimpleName();
                boolean isInControlCenter = "ControlPanelWindowView".equals(clsName) || "ControlCenterWindowViewImpl".equals(clsName);
                Context mContext = isInControlCenter ? ((View)param.thisObject).getContext() : (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Resources res = mContext.getResources();
                if (sbHeight == 0) {
                    sbHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
                }
                MotionEvent event = (MotionEvent)param.args[0];
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        tapStartX = event.getX();
                        tapStartY = event.getY();
                        isSlidingStart = isInControlCenter ? tapStartY <= sbHeight : !XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
                        tapStartPointers = 1;
                        if (mBrightnessController == null) {
                            Object mControlCenterController;
                            if (Helpers.isTPlus() && isInControlCenter) {
                                mControlCenterController = XposedHelpers.getObjectField(param.thisObject, "controlCenterController");
                            }
                            else {
                                mControlCenterController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClassIfExists("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.classLoader));
                            }
                            mBrightnessController = XposedHelpers.callMethod(XposedHelpers.getObjectField(mControlCenterController, "brightnessController"), "get");
                        }
                        Object mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager");
                        int mDisplayId = mContext.getDisplay().getDisplayId();
                        topMinimumBacklight = (float) XposedHelpers.getObjectField(mBrightnessController, "mMinimumBacklight");
                        topMaximumBacklight = (float) XposedHelpers.getObjectField(mBrightnessController, "mMaximumBacklight");
                        tapStartBrightness = (float) XposedHelpers.callMethod(mDisplayManager, "getBrightness", mDisplayId);
                        if (MainModule.mPrefs.getBoolean("system_showpct")) {
                            ViewGroup mStatusBarWindow;
                            if (isInControlCenter) {
                                mStatusBarWindow = (ViewGroup)param.thisObject;
                            }
                            else {
                                mStatusBarWindow = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mNotificationShadeWindowView");
                            }
                            if (mStatusBarWindow == null)
                                Helpers.log("StatusBarGesturesHook", "mStatusBarWindow is null");
                            else
                                initPct(mStatusBarWindow, 2, mContext);
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        tapStartPointers = event.getPointerCount();
                        break;
                    case MotionEvent.ACTION_UP:
                        long lastTouchTime = currentTouchTime;
                        float lastTouchX = currentTouchX;
                        currentTouchTime = currentTimeMillis();
                        currentTouchX = event.getX();
                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(currentTouchX - lastTouchX) < 100F) {
                            currentTouchTime = 0L;
                            currentTouchX = 0F;
                            GlobalActions.handleAction(mContext, "pref_key_system_statusbarcontrols_dt");
                        }
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isSlidingStart = false;
                        isSliding = false;
                        if (mPct != null) {
                            mPct.setVisibility(View.GONE);
                            if (tapCurrentBrightness > -0.5f) {
                                mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager");
                                XposedHelpers.callMethod(mDisplayManager, "setBrightness", mContext.getDisplay().getDisplayId(), tapCurrentBrightness);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isSlidingStart) return;
                        DisplayMetrics metrics = res.getDisplayMetrics();
                        if (event.getY() - tapStartY > sbHeight) return;
                        float delta = event.getX() - tapStartX;
                        if (delta == 0) return;
                        if (!isSliding && Math.abs(delta) > metrics.widthPixels / 10f) isSliding = true;
                        if (!isSliding) return;
                        int opt = MainModule.mPrefs.getStringAsInt(tapStartPointers == 2 ? "system_statusbarcontrols_dual" : "system_statusbarcontrols_single", 1);
                        if (opt == 2) {
                            int sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_bright", 2);
                            float ratio = delta / metrics.widthPixels;
                            ratio = (sens == 1 ? 0.66f : (sens == 3 ? 1.66f : 1.0f)) * ratio * 0.618f;
                            float nextLevel = Math.min(topMaximumBacklight, Math.max(topMinimumBacklight, tapStartBrightness + (topMaximumBacklight - topMinimumBacklight) * ratio));
                            XposedHelpers.callMethod(mBrightnessController, "setBrightness", nextLevel);
                            tapCurrentBrightness = nextLevel;
                            if (MainModule.mPrefs.getBoolean("system_showpct") && mPct != null) {
                                if (mPct.getVisibility() == View.GONE) {
                                    mPct.setVisibility(View.VISIBLE);
                                    mPct.animate().alpha(1.0f).setDuration(300).start();
                                }
                                if ((int)mPct.getTag() == 2) {
                                    int currentLevel = (int) XposedHelpers.callStaticMethod(BrightnessUtils, "convertLinearToGammaFloat", nextLevel, topMinimumBacklight, topMaximumBacklight);
                                    int maxLevel = (int) XposedHelpers.getStaticObjectField(BrightnessUtils, "GAMMA_SPACE_MAX");
                                    mPct.setText(((currentLevel * 100) / maxLevel) + "%");
                                }
                            }
                        } else if (opt == 3) {
                            tapCurrentBrightness = -1.0f;
                            int sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_vol", 2);
                            if (Math.abs(delta) < metrics.widthPixels / ((sens == 1 ? 0.66f : (sens == 3 ? 1.66f : 1.0f)) * 20 * metrics.density)) return;
                            tapStartX = event.getX();
                            AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
                            audioManager.adjustVolume(delta > 0 ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_VIBRATE);
                        }
                        break;
                }
            }
        };
        String eventMethod = Helpers.isTPlus() ? "onTouchEvent" : "interceptTouchEvent";
        Helpers.findAndHookMethod(StatusBarCls, lpparam.classLoader, eventMethod, MotionEvent.class, hook);
        if (Helpers.isTPlus()) {
            String pluginLoaderClass = "com.android.systemui.shared.plugins.PluginInstance$Factory";
            Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
                private boolean isHooked = false;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                    if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                        isHooked = true;
                        if (pluginLoader == null) {
                            pluginLoader = (ClassLoader) param.getResult();
                        }
                        Helpers.findAndHookMethod("miui.systemui.controlcenter.windowview.ControlCenterWindowViewImpl", pluginLoader, "handleMotionEvent", MotionEvent.class, boolean.class, hook);
                    }
                }
            });
        }
        else {
            Helpers.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowView", lpparam.classLoader, "onTouchEvent", MotionEvent.class, hook);
        }
    }

    public static void ScreenshotConfigHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("android.content.ContentResolver", lpparam.classLoader, "update", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (param.args.length != 4) return;
                ContentValues contentValues = (ContentValues) param.args[1];
                String displayName = contentValues.getAsString("_display_name");
                if (displayName != null && displayName.contains("Screenshot")) {
                    Context context = Helpers.findContext();
                    int format = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_screenshot_format", "2"));
                    String ext = format <= 2 ? ".jpg" : (format == 3 ? ".png" : ".webp");

                    displayName = displayName.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext;
                    contentValues.put("_display_name", displayName);
                }
            }
        });
        Helpers.findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "insert", Uri.class, ContentValues.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Uri imgUri = (Uri) param.args[0];
                ContentValues contentValues = (ContentValues) param.args[1];
                String displayName = contentValues.getAsString("_display_name");
                if (MediaStore.Images.Media.EXTERNAL_CONTENT_URI.equals(imgUri) && displayName != null && displayName.contains("Screenshot")) {
                    Context context = Helpers.findContext();
                    int folder = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_screenshot_path", "1"));
                    String dir = Helpers.getSharedStringPref(context, "pref_key_system_screenshot_mypath", "");
                    int format = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_screenshot_format", "2"));
                    String ext = format <= 2 ? ".jpg" : (format == 3 ? ".png" : ".webp");

                    File mScreenshotDir;
                    displayName = displayName.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext;
                    if (folder > 1) {
                        if (folder == 4 && !TextUtils.isEmpty(dir))
                            mScreenshotDir = new File(dir);
                        else
                            mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(folder == 2 ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DCIM), "Screenshots");
                        if (!mScreenshotDir.exists()) mScreenshotDir.mkdirs();
                        String relativePath = mScreenshotDir.getPath().replace(Environment.getExternalStorageDirectory().getPath() + File.separator, "");
                        contentValues.put("relative_path", relativePath);
                        if (contentValues.getAsString("_data") != null) {
                            contentValues.put("_data", mScreenshotDir.getPath() + "/" + displayName);
                        }
                    }
                    contentValues.put("_display_name", displayName);
                }
            }
        });

        Helpers.hookAllMethods("android.graphics.Bitmap", lpparam.classLoader, "compress", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context context = Helpers.findContext();
                int quality = (int) param.args[1];
                if (quality != 100 || (param.args[2] instanceof ByteArrayOutputStream)) return;
                int format = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_screenshot_format", "2"));
                quality = Helpers.getSharedIntPref(context, "pref_key_system_screenshot_quality", 100);
                if (format == 3) {
                    quality = 100;
                }
                Bitmap.CompressFormat compress = format <= 2 ? Bitmap.CompressFormat.JPEG : (format == 3 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
                param.args[0] = compress;
                param.args[1] = quality;
            }
        });
    }

    public static void NoNetworkSpeedSeparatorHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedSplitter", lpparam.classLoader, "updateVisibility", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                XposedHelpers.setObjectField(param.thisObject, "mNetworkSpeedVisibility", View.GONE);
            }
        });
    }

    public static void ToastTimeHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.notification.NotificationManagerService", lpparam.classLoader, "showNextToastLocked", new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.callMethod(param.thisObject, "getContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                ArrayList<Object> mToastQueue = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mToastQueue");
                if (mContext == null || mHandler == null || mToastQueue == null || mToastQueue.size() == 0) return;
                int mod = (Helpers.getSharedIntPref(mContext, "pref_key_system_toasttime", 0) - 4) * 1000;
                for (Object record: mToastQueue)
                    if (record != null && mHandler.hasMessages(2, record)) {
                        mHandler.removeCallbacksAndMessages(record);
                        int duration = XposedHelpers.getIntField(record, "duration");
                        int delay = Math.max(1000, (duration == 1 ? 3500 : 2000) + mod);
                        mHandler.sendMessageDelayed(Message.obtain(mHandler, 2, record), delay);
                    }
            }
        });

        Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "systemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_toasttime", 0) {
                    @Override
                    public void onChange(String name, int defValue) {
                        MainModule.mPrefs.put(name, Helpers.getSharedIntPref(mContext, name, defValue));
                    }
                };
            }
        });

        String windowClass = "com.android.server.wm.DisplayPolicy";
        Helpers.hookAllMethods(windowClass, lpparam.classLoader, "adjustWindowParamsLw", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object lp = param.args.length == 1 ? param.args[0] : param.args[1];
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mPrevHideTimeout", XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds"));
            }

            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object lp = param.args.length == 1 ? param.args[0] : param.args[1];
                long mPrevHideTimeout = (long)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mPrevHideTimeout");
                long mHideTimeout = XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds");
                if (mPrevHideTimeout == -1 || mHideTimeout == -1) return;

                long dur = 0;
                if (mPrevHideTimeout == 1000 || mPrevHideTimeout == 4000 || mPrevHideTimeout == 5000 || mPrevHideTimeout == 7000 || mPrevHideTimeout != mHideTimeout)
                    dur = Math.max(1000, 3500 + (MainModule.mPrefs.getInt("system_toasttime", 0) - 4) * 1000);
                if (dur != 0) XposedHelpers.setLongField(lp, "hideTimeoutMilliseconds", dur);
            }
        });
    }

    public static void ClearAllTasksHook(LoadPackageParam lpparam) {
        String wpuClass = "com.android.server.wm.WindowProcessUtils";
        Helpers.hookAllMethods(wpuClass, lpparam.classLoader, "getPerceptibleRecentAppList", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                param.setResult(null);
            }
        });
    }

    private static int hours3ResId;
    private static int hours4ResId;
    private static int hours5ResId;
    private static int hours6ResId;
    private static int hours8ResId;
    private static int hours10ResId;
    private static int hours12ResId;
    private static int foreverResId;
    public static void MoreSnoozeOptionsRes() {
        hours3ResId = MainModule.resHooks.addResource("time_3h", R.string.time_3h);
        hours4ResId = MainModule.resHooks.addResource("time_4h", R.string.time_4h);
        hours5ResId = MainModule.resHooks.addResource("time_5h", R.string.time_5h);
        hours6ResId = MainModule.resHooks.addResource("time_6h", R.string.time_6h);
        hours8ResId = MainModule.resHooks.addResource("time_8h", R.string.time_8h);
        hours10ResId = MainModule.resHooks.addResource("time_10h", R.string.time_10h);
        hours12ResId = MainModule.resHooks.addResource("time_12h", R.string.time_12h);
        foreverResId = MainModule.resHooks.addResource("time_forever", R.string.time_forever);
    }

    public static void MoreSnoozeOptionsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationSnooze", lpparam.classLoader, "getDefaultSnoozeOptions", new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(MethodHookParam param) throws Throwable {
                ArrayList<Object> options = (ArrayList<Object>)param.getResult();
                if (options == null) return;
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", hours3ResId, 180));
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", hours4ResId, 240));
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", hours5ResId, 300));
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", hours6ResId, 360));
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", hours8ResId, 480));
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", hours10ResId, 600));
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", hours12ResId, 720));
                options.add(XposedHelpers.callMethod(param.thisObject, "createOption", foreverResId, 1024));
            }
        });

        if (Helpers.is12())
            Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationSnooze", lpparam.classLoader, "createOptionViews", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    LinearLayout mSnoozeOptionContainer = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mSnoozeOptionContainer");
                    ViewGroup parent = ((ViewGroup)mSnoozeOptionContainer.getParent());
                    if (parent.getClass() == ScrollView.class) return;
                    parent.removeView(mSnoozeOptionContainer);
                    HorizontalScrollView scrollView = new HorizontalScrollView(mSnoozeOptionContainer.getContext());
                    scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    scrollView.setVerticalScrollBarEnabled(false);
                    scrollView.setHorizontalScrollBarEnabled(false);
                    scrollView.addView(mSnoozeOptionContainer);
                    parent.addView(scrollView);
                    ViewGroup.LayoutParams lp1 = scrollView.getLayoutParams();
                    lp1.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    scrollView.setLayoutParams(lp1);
                    ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams)mSnoozeOptionContainer.getLayoutParams();
                    lp2.setMarginStart(0);
                    lp2.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    lp2.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    mSnoozeOptionContainer.setLayoutParams(lp2);
                }
            });
    }

    public static void MoreSnoozeOptionsServiceHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.notification.SnoozeHelper", lpparam.classLoader, "scheduleRepost", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if ((long)param.args[3] == 1024 * 60000) param.setResult(null);
            }
            @Override
            @SuppressWarnings("unchecked")
            protected void after(MethodHookParam param) throws Throwable {
                if ((long)param.args[3] == 1024 * 60000) return;
                ArrayMap<String, Long> mSnoozedNotificationDelays = (ArrayMap<String, Long>)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSnoozedNotificationDelays");
                if (mSnoozedNotificationDelays == null) mSnoozedNotificationDelays = new ArrayMap<String, Long>();
                mSnoozedNotificationDelays.put((String)param.args[1], currentTimeMillis() + (long)param.args[3]);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mSnoozedNotificationDelays", mSnoozedNotificationDelays);
            }
        });

        Helpers.findAndHookMethod("com.android.server.notification.SnoozeHelper", lpparam.classLoader, "repost", String.class, int.class, new MethodHook() {
            @Override
            @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
            protected void after(MethodHookParam param) throws Throwable {
                ArrayMap<String, Long> mSnoozedNotificationDelays = (ArrayMap<String, Long>)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSnoozedNotificationDelays");
                if (mSnoozedNotificationDelays != null) mSnoozedNotificationDelays.remove(param.args[0]);
            }
        });

        Helpers.hookAllConstructors("com.android.server.notification.SnoozeHelper", lpparam.classLoader, new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(MethodHookParam param) throws Throwable {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PREFIX + "GetSnoozedNotifications");
                filter.addAction(ACTION_PREFIX + "UnsnoozeNotification");
                filter.addAction(ACTION_PREFIX + "CancelNotification");
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        ArrayMap<Integer, ArrayMap<String, ArrayMap<String, Object>>> mSnoozedNotifications = (ArrayMap<Integer, ArrayMap<String, ArrayMap<String, Object>>>)XposedHelpers.getObjectField(param.thisObject, "mSnoozedNotifications");
                        ArrayMap<String, Long> mSnoozedNotificationDelays = (ArrayMap<String, Long>)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSnoozedNotificationDelays");

                        if (action.equals(ACTION_PREFIX + "CancelNotification")) try {
                            ArrayMap<String, ArrayMap<String, Object>> packages = mSnoozedNotifications.get(intent.getIntExtra("user", 0));
                            if (packages == null) return;
                            ArrayMap<String, Object> notificatios = packages.get(intent.getStringExtra("package"));
                            if (notificatios == null) return;
                            if (notificatios.containsKey(intent.getStringExtra("key")))
                                XposedHelpers.setBooleanField(notificatios.get(intent.getStringExtra("key")), "isCanceled", intent.getBooleanExtra("canceled", false));
                            return;
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }

                        if (action.equals(ACTION_PREFIX + "UnsnoozeNotification")) try {
                            XposedHelpers.callMethod(param.thisObject, "repost", intent.getStringExtra("key"), intent.getIntExtra("user", 0));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }

                        try {
                            Bundle notifications = new Bundle();
                            int user;
                            String pkg;
                            for (int i = 0; i < mSnoozedNotifications.size(); i++ ) {
                                user = mSnoozedNotifications.keyAt(i);
                                ArrayMap<String, ArrayMap<String, Object>> mSnoozedNotification = mSnoozedNotifications.get(mSnoozedNotifications.keyAt(i));
                                for (int j = 0; j < mSnoozedNotification.size(); j++ ) {
                                    pkg = mSnoozedNotification.keyAt(j);
                                    ArrayMap<String, Object> mSnoozed = mSnoozedNotification.get(mSnoozedNotification.keyAt(j));
                                    for (int k = 0; k < mSnoozed.size(); k++) {
                                        String key = mSnoozed.keyAt(k);
                                        Bundle notif = new Bundle();
                                        notif.putInt("user", user);
                                        notif.putString("package", pkg);
                                        notif.putString("key", key);
                                        Object record = mSnoozed.get(key);
                                        notif.putBoolean("canceled", XposedHelpers.getBooleanField(record, "isCanceled"));
                                        notif.putLong("created", XposedHelpers.getLongField(record, "mCreationTimeMs"));
                                        notif.putLong("updated", XposedHelpers.getLongField(record, "mUpdateTimeMs"));
                                        if (mSnoozedNotificationDelays != null && mSnoozedNotificationDelays.get(key) != null)
                                            notif.putLong("reposted", mSnoozedNotificationDelays.get(key));
                                        Object sbn = XposedHelpers.getObjectField(record, "sbn");
                                        Notification notification = (Notification)XposedHelpers.getObjectField(sbn, "notification");
                                        if (notification != null) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                                notif.putString("channel", notification.getChannelId());
                                            notif.putInt("color", notification.color);
                                            if (notification.getLargeIcon() != null)
                                                notif.putParcelable("icon", notification.getLargeIcon());
                                            if (notification.extras != null) {
                                                notif.putString("title", notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString());
                                                CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT, null);
                                                if (text == null || "" == text) text = notification.extras.getCharSequence(Notification.EXTRA_TEXT, null);
                                                if (text != null && "" != text) {
                                                    String[] lines = text.toString().split("\\n");
                                                    notif.putString("text", lines[0] + (lines.length == 1 ? "" : "..."));
                                                }
                                                Parcelable[] messages = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES);
                                                if (messages != null) notif.putInt("messages", messages.length);
                                            }
                                        }
                                        notifications.putBundle(mSnoozed.keyAt(k), notif);
                                    }
                                }
                            }
                            Intent snoozedIntent = new Intent(GlobalActions.EVENT_PREFIX + "UpdateSnoozedNotifications");
                            snoozedIntent.setPackage(Helpers.modulePkg);
                            snoozedIntent.putExtras(notifications);
                            mContext.sendBroadcast(snoozedIntent);
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                }, filter);
            }
        });
    }

    public static void InactiveBrightnessSliderHook(LoadPackageParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int opt = MainModule.mPrefs.getStringAsInt("system_inactivebrightness", 1);
                if (opt == 2) {
                    SeekBar mSlider = (SeekBar)XposedHelpers.getObjectField(param.thisObject, "mSlider");
                    if (mSlider != null)
                        mSlider.setOnTouchListener(new View.OnTouchListener(){
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return true;
                            }
                        });
                } else if (opt == 3) try {
                    View sliderView = (View)param.thisObject;
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)sliderView.getLayoutParams();
                    lp.height = 0;
                    lp.topMargin = Math.round(2 * sliderView.getResources().getDisplayMetrics().density);
                    lp.bottomMargin = 0;
                    sliderView.setLayoutParams(lp);
                } catch (Throwable t) {
                    XposedBridge.log(t);
                }
            }
        };
        Helpers.findAndHookMethod("com.android.systemui.settings.brightness.BrightnessSliderView", lpparam.classLoader, "onFinishInflate", hook);
//        Helpers.findAndHookMethod("com.android.systemui.controlcenter.phone.widget.QCToggleSliderView", lpparam.classLoader, "onAttachedToWindow", hook);
    }

    private static final float[] startPos = new float[2];
    private static void processLSEvent(MethodHookParam param) {
        MotionEvent event = (MotionEvent)param.args[0];
        if (event.getPointerCount() > 1) return;
        int action = event.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP) return;

        ViewGroup mKeyguardBottomArea = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mBottomAreaView");
        if (mKeyguardBottomArea == null) return;
        ViewGroup mIndicationArea = (ViewGroup)XposedHelpers.getObjectField(mKeyguardBottomArea, "mIndicationArea");
        if (!Helpers.isReallyVisible(mIndicationArea)) return;

        int[] coord = new int[2];
        mIndicationArea.getLocationOnScreen(coord);
        Rect rect = new Rect(coord[0], coord[1], coord[0] + mIndicationArea.getWidth(), coord[1] + mIndicationArea.getHeight());
        if (!rect.contains((int)event.getX(), (int)event.getY())) return;

        if (action == MotionEvent.ACTION_DOWN) {
            startPos[0] = event.getX();
            startPos[1] = event.getY();
        } else if (action == MotionEvent.ACTION_UP) try {
            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
            int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
            if (Math.abs(event.getX() - startPos[0]) > slop || Math.abs(event.getY() - startPos[1]) > slop) return;
            Object mPanelViewController = XposedHelpers.getObjectField(param.thisObject, "mPanelViewController");
            Object statusBarKeyguardViewManager = XposedHelpers.getObjectField(mPanelViewController, "statusBarKeyguardViewManager");

            XposedHelpers.callMethod(statusBarKeyguardViewManager, "showGenericBouncer", true);
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    public static void TapToUnlockHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.keyguard.injector.KeyguardPanelViewInjector", lpparam.classLoader, "onTouchEvent", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                processLSEvent(param);
            }
        });

        Helpers.hookAllMethods("com.android.keyguard.injector.KeyguardPanelViewInjector", lpparam.classLoader, "onInterceptTouchEvent", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                processLSEvent(param);
            }
        });
    }

    public static void NoSafeVolumeWarningRes() {
        MainModule.resHooks.setObjectReplacement("android", "bool", "config_safe_media_volume_enabled", false);
        MainModule.resHooks.setObjectReplacement("android", "bool", "config_safe_media_disable_on_volume_up", false);
    }

    public static void NoLowBatteryWarningHook() {
        Helpers.hookAllMethods(Settings.System.class, "getIntForUser", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String key = (String)param.args[1];
                if ("low_battery_dialog_disabled".equals(key)) param.setResult(1);
            }
        });
    }

    public static void TempHideOverlaySystemUIHook(LoadPackageParam lpparam) {
        final boolean[] isActListened = {false};

        Helpers.hookAllMethods("com.android.wm.shell.pip.PipTaskOrganizer", lpparam.classLoader, "onTaskAppeared", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (!isActListened[0]) {
                    isActListened[0] = true;
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("miui.intent.TAKE_SCREENSHOT");
                    mContext.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (action == null) return;
                            if (action.equals("miui.intent.TAKE_SCREENSHOT")) {
                                boolean state = intent.getBooleanExtra("IsFinished", true);
                                Object mState = XposedHelpers.getObjectField(param.thisObject, "mState");
                                boolean isPip = (boolean) XposedHelpers.callMethod(mState, "isInPip");
                                if (isPip) {
                                    Object mSurfaceControlTransactionFactory = XposedHelpers.getObjectField(param.thisObject, "mSurfaceControlTransactionFactory");
                                    SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) XposedHelpers.callMethod(mSurfaceControlTransactionFactory, "getTransaction");
                                    SurfaceControl mLeash = (SurfaceControl) XposedHelpers.getObjectField(param.thisObject, "mLeash");
                                    transaction.setVisibility(mLeash, state);
                                    transaction.apply();
                                }
                            }
                        }
                    }, intentFilter);
                }
            }
        });
    }
    public static void TempHideOverlayAppHook(LoadPackageParam lpparam) {
        final boolean[] isListened = {false};
        final ArrayList<View> mViews = new ArrayList<>();
        MethodHook addViewHook = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (param.args[0] == null || !(param.args[1] instanceof WindowManager.LayoutParams) || param.getThrowable() != null) return;
                WindowManager.LayoutParams params = (WindowManager.LayoutParams)param.args[1];
                if (params.type != WindowManager.LayoutParams.TYPE_PHONE
                    && params.type != WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                    && params.type != WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) return;
                View view = (View)param.args[0];
                Context mContext = view.getContext();
                mViews.add(view);

                if (!isListened[0]) {
                    isListened[0] = true;
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("miui.intent.TAKE_SCREENSHOT");
                    mContext.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (action == null) return;
                            if (action.equals("miui.intent.TAKE_SCREENSHOT")) {
                                boolean state = intent.getBooleanExtra("IsFinished", true);
                                for (int size = mViews.size() - 1; size >= 0; size--) {
                                    View vs = mViews.get(size);
                                    if (vs != null) {
                                        if (state) {
                                            if (vs.getVisibility() != View.VISIBLE && XposedHelpers.getAdditionalInstanceField(vs, "mSavedVisibility") != null) {
                                                XposedHelpers.removeAdditionalInstanceField(vs, "mSavedVisibility");
                                                vs.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        else {
                                            if (vs.getVisibility() == View.VISIBLE) {
                                                vs.setVisibility(View.INVISIBLE);
                                                XposedHelpers.setAdditionalInstanceField(vs, "mSavedVisibility", true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }, intentFilter);
                }
            }
        };
        MethodHook removeViewHook = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                mViews.remove(param.args[0]);
            }
        };
        Helpers.hookAllMethods("android.view.WindowManagerGlobal", lpparam.classLoader, "addView", addViewHook);
        Helpers.hookAllMethods("android.view.WindowManagerGlobal", lpparam.classLoader, "removeView", removeViewHook);
    }

    public static void GalleryScreenshotPathHook(LoadPackageParam lpparam) {
        Class<?> MIUIStorageConstants = findClass("com.miui.gallery.storage.constants.MIUIStorageConstants", lpparam.classLoader);
        int folder = MainModule.mPrefs.getStringAsInt("system_gallery_screenshots_path", 1);
        String ssPath = "";
        if (folder == 2) {
            ssPath = Environment.DIRECTORY_PICTURES + File.separator + "Screenshots";
        }
        else if (folder == 3) {
            ssPath = Environment.DIRECTORY_DCIM + File.separator + "Screenshots";
        }
        if (folder > 1) {
            XposedHelpers.setStaticObjectField(MIUIStorageConstants, "DIRECTORY_SCREENSHOT_PATH", ssPath);
        }
    }

    public static void ScreenshotFloatTimeHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.miui.screenshot.GlobalScreenshot", lpparam.classLoader, "startGotoThumbnailAnimation", Runnable.class, new MethodHook() {
            @Override
            @SuppressWarnings("ConstantConditions")
            protected void after(MethodHookParam param) throws Throwable {
                boolean mIsShowLongScreenShotGuide = false;
                try {
                    mIsShowLongScreenShotGuide = XposedHelpers.getBooleanField(param.thisObject, "mIsShowLongScreenShotGuide");
                } catch (Throwable ignore) {}
                if (mIsShowLongScreenShotGuide) return;
                int opt = MainModule.mPrefs.getInt("system_screenshot_floattime", 0);
                if (opt <= 0) return;
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                Runnable mQuitThumbnailRunnable = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mQuitThumbnailRunnable");
                mHandler.removeCallbacks(mQuitThumbnailRunnable);
                mHandler.postDelayed(mQuitThumbnailRunnable, opt * 1000L);
            }
        });
    }

    public static void ScrambleAppLockPINHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.miui.applicationlock.widget.MiuiNumericInputView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                LinearLayout keys = (LinearLayout)param.thisObject;
                ArrayList<View> mRandomViews = new ArrayList<View>();
                View bottom0 = null; View bottom2 = null;
                for (int row = 0; row <= 3; row++) {
                    ViewGroup cols = (ViewGroup)keys.getChildAt(row);
                    for (int col = 0; col <= 2; col++) {
                        if (row == 3)
                            if (col == 0) {
                                bottom0 = cols.getChildAt(col);
                                continue;
                            } else if (col == 2) {
                                bottom2 = cols.getChildAt(col);
                                continue;
                            }
                        mRandomViews.add(cols.getChildAt(col));
                    }
                    cols.removeAllViews();
                }

                Collections.shuffle(mRandomViews);

                int cnt = 0;
                for (int row = 0; row <= 3; row++)
                    for (int col = 0; col <= 2; col++) {
                        ViewGroup cols = (ViewGroup)keys.getChildAt(row);
                        if (row == 3)
                            if (col == 0) {
                                cols.addView(bottom0);
                                continue;
                            } else if (col == 2) {
                                cols.addView(bottom2);
                                continue;
                            }
                        cols.addView(mRandomViews.get(cnt));
                        cnt++;
                    }
            }
        });
    }

    public static void ChargingInfoServiceHook(LoadPackageParam lpparam) {
        if (!Helpers.hookAllMethodsSilently("com.android.server.BatteryService$BatteryPropertiesRegistrar", lpparam.classLoader, "getProperty", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                int req = (int)param.args[0];
                if (req < 1000) return;
                long value = Long.MIN_VALUE;
                if (req == 1001)
                    value = XposedHelpers.getIntField(XposedHelpers.getSurroundingThis(param.thisObject), "mLastBatteryVoltage");
                else if (req == 1002)
                    value = XposedHelpers.getIntField(XposedHelpers.getSurroundingThis(param.thisObject), "mLastBatteryTemperature");
                else if (req == 1003)
                    value = XposedHelpers.getIntField(XposedHelpers.getSurroundingThis(param.thisObject), "mLastBatteryHealth");
                XposedHelpers.callMethod(param.args[1], "setLong", value);
                param.setResult(0);
            }
        })) Helpers.findAndHookMethod("com.android.server.BatteryService", lpparam.classLoader, "processValuesLocked", boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                try {
                    Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                    ContentResolver provider = mContext.getContentResolver();
                    Settings.Global.putInt(provider, Helpers.modulePkg + ".battery.voltage", XposedHelpers.getIntField(param.thisObject, "mLastBatteryVoltage"));
                    Settings.Global.putInt(provider, Helpers.modulePkg + ".battery.temperature", XposedHelpers.getIntField(param.thisObject, "mLastBatteryTemperature"));
                    Settings.Global.putInt(provider, Helpers.modulePkg + ".battery.health", XposedHelpers.getIntField(param.thisObject, "mLastBatteryHealth"));
                } catch (Throwable ignore) {}
            }
        });
    }

    public static void ChargingInfoHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)param.args[0];
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String type = uri.getPathSegments().get(1);
                            String key = uri.getPathSegments().get(2);
                            if (!key.contains("pref_key_system_charginginfo_")) return;

                            switch (type) {
                                case "string":
                                    MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, ""));
                                    break;
                                case "integer":
                                    MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 1));
                                    break;
                                case "boolean":
                                    MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(mContext, key, false));
                                    break;
                            }
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.hookAllMethods("com.android.keyguard.charge.ChargeUtils", lpparam.classLoader, "getChargingHintText", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context context = (Context)param.args[0];
                int charge = (int)param.args[2];
                String hint = (String)param.getResult();
                String PROVIDER_POWER_CENTER = (String)XposedHelpers.getStaticObjectField(findClass("com.android.keyguard.charge.ChargeUtils", lpparam.classLoader), "PROVIDER_POWER_CENTER");

                Bundle bundle = null;
                try {
                    bundle = context.getContentResolver().call(Uri.parse(PROVIDER_POWER_CENTER), "getBatteryCurrent", null, null);
                } catch (Exception ignore) {}

                if (bundle != null && charge < 100) {
                    boolean showCurr = MainModule.mPrefs.getBoolean("system_charginginfo_current");
                    boolean showVolt = MainModule.mPrefs.getBoolean("system_charginginfo_voltage");
                    boolean showWatt = MainModule.mPrefs.getBoolean("system_charginginfo_wattage");
                    boolean showTemp = MainModule.mPrefs.getBoolean("system_charginginfo_temp");

                    ArrayList<String> values = new ArrayList<>();
                    BatteryManager btrMgr = (BatteryManager)context.getSystemService(Context.BATTERY_SERVICE);
                    int currVal = Math.abs(bundle.getInt("current_now"));
                    long voltVal = btrMgr.getLongProperty(1001);
                    if (voltVal == Long.MIN_VALUE) voltVal = Settings.Global.getInt(context.getContentResolver(), Helpers.modulePkg + ".battery.voltage", 0);
                    if (voltVal == Long.MIN_VALUE) voltVal = 0;
                    long tempVal = btrMgr.getLongProperty(1002);
                    if (tempVal == Long.MIN_VALUE) tempVal = Settings.Global.getInt(context.getContentResolver(), Helpers.modulePkg + ".battery.temperature", 0);
                    if (tempVal == Long.MIN_VALUE) tempVal = 0;

                    if (showCurr) values.add(currVal + " mA");
                    if (showVolt) values.add(String.format(Locale.getDefault(), "%.1f", voltVal / 1000f) + " V");
                    if (showWatt) values.add(String.format(Locale.getDefault(), "%.1f", voltVal / 1000f * currVal / 1000f) + " W");
                    if (showTemp) values.add(Math.round(tempVal / 10f) + " ℃");
                    if (values.size() == 0) return;
                    String info = TextUtils.join(" · ", values);

                    int opt = MainModule.mPrefs.getStringAsInt("system_charginginfo_view", 1);
                    if (opt == 1)
                        param.setResult(hint + "\n" + info);
                    else if (opt == 2)
                        param.setResult(hint + " · " + info);
                    else if (opt == 3)
                        param.setResult(info + " · " + hint);
                }
            }
        });

        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.KeyguardIndicationTextView", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                int opt = MainModule.mPrefs.getStringAsInt("system_charginginfo_view", 1);
                if (opt != 1) return;
                TextView indicator = (TextView)param.thisObject;
                if (indicator != null) indicator.setSingleLine(false);
            }
        });
    }

    public static void UseNativeRecentsHook(LoadPackageParam lpparam) {
        //noinspection ResultOfMethodCallIgnored
        Helpers.findAndHookMethodSilently("com.android.systemui.recents.misc.SystemServicesProxy", lpparam.classLoader, "isRecentsWithinLauncher", Context.class, XC_MethodReplacement.returnConstant(false));
    }

    public static void UseNativeRecentsFixHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.wm.TaskRecord", lpparam.classLoader, "isVisible", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if ((boolean)param.getResult()) return;
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement el: stackTrace)
                    if (el != null) if ("getPerceptibleRecentAppList".equals(el.getMethodName())) {
                        param.setResult(true);
                        return;
                    }
            }
        });
    }

    public static void NoSOSHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.keyguard.EmergencyButton", lpparam.classLoader, "updateEmergencyCallButton", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Button mSOS = (Button)param.thisObject;
                if (mSOS.getVisibility() == View.VISIBLE) {
                    mSOS.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void NoDarkForceHook(LoadPackageParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ContentResolver contentResolver = ((Context)XposedHelpers.callMethod(param.thisObject, "getContext")).getContentResolver();
                XposedHelpers.callStaticMethod(Settings.System.class, "putIntForUser", contentResolver, "smart_dark_enable", 0, XposedHelpers.callStaticMethod(UserHandle.class, "getCallingUserId"));
                XposedHelpers.callStaticMethod(XposedHelpers.findClassIfExists("android.os.SystemProperties", lpparam.classLoader), "set", "debug.hwui.force_dark", "false");
            }
        };
        Helpers.findAndHookMethodSilently("com.android.server.UiModeManagerService", lpparam.classLoader, "setForceDark", Context.class, hook);
        Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "getAppDarkModeForUser", String.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.setResult(false);
            }
        });
        Helpers.findAndHookMethod("com.android.server.DarkModeAppSettingsInfo", lpparam.classLoader, "getOverrideEnableValue", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.setResult(2);
            }
        });
    }

    public static void MaxNotificationIconsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconContainer", lpparam.classLoader, "miuiShowNotificationIcons", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean isShow = (boolean) param.args[0];
                if (isShow) {
                    int opt = MainModule.mPrefs.getStringAsInt("system_maxsbicons", 0);
                    opt = opt == -1 ? 999 : opt;
                    XposedHelpers.setObjectField(param.thisObject, "MAX_DOTS", 3);
                    XposedHelpers.setObjectField(param.thisObject, "MAX_STATIC_ICONS", opt);
                    String fieldLockVisible = Helpers.isTPlus() ? "MAX_ICONS_ON_LOCKSCREEN" : "MAX_VISIBLE_ICONS_ON_LOCK";
                    XposedHelpers.setObjectField(param.thisObject, fieldLockVisible, opt);
                    XposedHelpers.callMethod(param.thisObject, "updateState");
                    param.setResult(null);
                }
            }
        });
    }

    public static void MoreNotificationsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy", lpparam.classLoader, "checkNotificationCountLimit", String.class, new MethodHook() {
            @Override
            @SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
            protected void before(MethodHookParam param) throws Throwable {
            String pkgName = (String) param.args[0];
            Collection<Object> mNotifications = (Collection<Object>) XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mEntryManager"), "getAllNotifs");
            List list = (List) mNotifications.stream().filter(new Predicate() {
                @Override
                public final boolean test(Object obj) {
                    String notifyPkgName = (String) XposedHelpers.callMethod(XposedHelpers.callMethod(obj, "getSbn"), "getPackageName");
                    return pkgName.equals(notifyPkgName);
                }
            }).collect(Collectors.toList());
            if (list.size() < 24) param.setResult(null);
            }
        });
    }

    public static void VolumeDialogDNDSwitchHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiRingerModeLayout.RingerButtonHelper", lpparam.classLoader, "updateState", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                boolean mExpanded = XposedHelpers.getBooleanField(param.thisObject, "mExpanded");
                if (mExpanded) return;
                View mStandardView = (View)XposedHelpers.getObjectField(param.thisObject, "mStandardView");
                View mDndView = (View)XposedHelpers.getObjectField(param.thisObject, "mDndView");
                if (mStandardView != null) mStandardView.setVisibility(View.GONE);
                if (mDndView != null) mDndView.setVisibility(View.VISIBLE);
            }
        });
    }

    public static void NoMediaMuteInDNDHook() {
        Helpers.hookAllMethods("android.media.AudioServiceInjector", null, "handleZenModeChangedForMusic", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if ((int)param.args[2] == 1 || (int)param.args[3] == 1) param.setResult(null);
            }
        });
    }

    public static void VolumeDialogAutohideDelayHook(ClassLoader classLoader) {
        Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "computeTimeoutH", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean mHovering = XposedHelpers.getBooleanField(param.thisObject, "mHovering");
                if (mHovering) {
                    param.setResult(16000);
                    return;
                }
                boolean mSafetyWarning = (boolean) XposedHelpers.getObjectField(param.thisObject, "mSafetyWarning");
                if (mSafetyWarning) {
                    int opt = MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0);
                    param.setResult(opt > 0 ? opt : 5000);
                    return;
                }
                boolean mExpanded = XposedHelpers.getBooleanField(param.thisObject, "mExpanded");
                int opt = MainModule.mPrefs.getInt(mExpanded ? "system_volumedialogdelay_expanded" : "system_volumedialogdelay_collapsed", 0);
                if (opt > 0) param.setResult(opt);
            }
        });
    }

    public static ArrayList<SoundData> mLastPlayedSounds = new ArrayList<SoundData>();

    public static void AudioSilencerServiceHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "init", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PREFIX + "SavePlayedSound");
                filter.addAction(ACTION_PREFIX + "FetchPlayedSounds");
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                mContext.registerReceiver(new BroadcastReceiver() {
                    public void onReceive(final Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action != null) try {
                            if (action.equals(ACTION_PREFIX + "SavePlayedSound")) {
                                SoundData data = intent.getParcelableExtra("data");
                                mLastPlayedSounds.add(0, data);
                                if (mLastPlayedSounds.size() > 10)
                                    mLastPlayedSounds = new ArrayList<SoundData>(mLastPlayedSounds.subList(0, 10));
                            } else if (action.equals(ACTION_PREFIX + "FetchPlayedSounds")) {
                                Intent soundsIntent = new Intent(GlobalActions.EVENT_PREFIX + "FetchPlayedSoundsData");
                                soundsIntent.putParcelableArrayListExtra("sounds", mLastPlayedSounds);
                                soundsIntent.setPackage(Helpers.modulePkg);
                                mContext.sendBroadcast(soundsIntent);
                            }
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                }, filter);
            }
        });
    }

    public static void SavePlayedSound(Context context, SoundData data) {
        Intent saveSoundIntent = new Intent(ACTION_PREFIX + "SavePlayedSound");
        saveSoundIntent.putExtra("data", data);
        saveSoundIntent.setPackage("android");
        context.sendBroadcast(saveSoundIntent);
    }

    @SuppressWarnings({"unchecked"})
    public static void AudioSilencerHook() {
        Helpers.hookAllConstructors(SoundPool.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSourceSoundData") == null) {
                    SparseArray<SoundData> mSourceSoundData = new SparseArray<SoundData>();
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mSourceSoundData", mSourceSoundData);
                }
            }
        });

        Helpers.hookAllMethods(SoundPool.class, "load", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                SparseArray<SoundData> mSourceSoundData = (SparseArray<SoundData>)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSourceSoundData");
                Context callerContext = Helpers.findContext();
                if (callerContext == null) return;
                String caller = callerContext.getPackageName();
                if (param.args[0] instanceof Context) {
                    Context mContext = (Context)param.args[0];
                    mSourceSoundData.put((int)param.getResult(), new SoundData(caller, "resource", mContext.getResources().getResourceName((int)param.args[1])));
                } else if (param.args[0] instanceof String) {
                    mSourceSoundData.put((int)param.getResult(), new SoundData(caller, "file", (String)param.args[0]));
                }
            }
        });

        Helpers.hookAllMethods(SoundPool.class, "play", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                SparseArray<SoundData> mSourceSoundData = (SparseArray<SoundData>)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSourceSoundData");
                if (mSourceSoundData != null) {
                    Context mContext = Helpers.findContext();
                    if (mContext == null) return;
                    SoundData data = mSourceSoundData.get((Integer)param.args[0]);
                    SavePlayedSound(mContext, data);
                    Set<String> silencedSounds = Helpers.getSharedStringSetPref(mContext, "pref_key_system_audiosilencer_sounds");
                    if (silencedSounds.contains(data.toPref())) param.setResult(null);
                }
            }
        });

        Helpers.hookAllMethods(MediaPlayer.class, "setDataSource", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (Modifier.isPrivate(param.method.getModifiers())) return;
                Context callerContext = Helpers.findContext();
                if (callerContext == null) return;
                String caller = callerContext.getPackageName();
                SoundData soundData = null;
                if (param.args.length > 1 && param.args[0] instanceof Context && param.args[1] instanceof Uri)
                    soundData = new SoundData(caller, "uri", ((Uri)param.args[1]).getPath());
                else if (param.args[0] instanceof String)
                    soundData = new SoundData(caller, "file", (String)param.args[0]);
                if (soundData != null)
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "mSourceSoundData", soundData);
            }
        });

        Helpers.hookAllMethods(MediaPlayer.class, "start", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                SoundData mSourceSoundData = (SoundData)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSourceSoundData");
                if (mSourceSoundData != null) {
                    Context mContext = Helpers.findContext();
                    if (mContext == null) return;
                    SavePlayedSound(mContext, mSourceSoundData);
                    Set<String> silencedSounds = Helpers.getSharedStringSetPref(mContext, "pref_key_system_audiosilencer_sounds");
                    if (silencedSounds.contains(mSourceSoundData.toPref())) param.setResult(null);
                }
            }
        });
    }

    public static void BetterPopupsAllowFloatHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.notification.policy.AppMiniWindowManager", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "context");
                Handler mHandler = new Handler(Looper.getMainLooper());

                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if (key.contains("pref_key_system_betterpopups_allowfloat_apps"))
                                MainModule.mPrefs.put(key, Helpers.getSharedStringSetPref(mContext, key));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.classLoader, "canSlide", String.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                String pkgName = (String) param.args[0];
                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps");
                Set<String> selectedAppsBlack = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps_black");
                if (selectedApps.contains(pkgName)) param.setResult(true);
                else if (selectedAppsBlack.contains(pkgName)) param.setResult(false);
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.policy.MiniWindowPolicy", lpparam.classLoader, "canSlidePackage", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.setResult(true);
            }
        });
    }

    public static void NoFloatingWindowBlacklistHook(LoadPackageParam lpparam) {
        MainModule.resHooks.setResReplacement("android", "array", "freeform_black_list", R.array.miui_resize_black_list);
        MethodHook clearHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                List<String> blackList = (List<String>)param.getResult();
                if (blackList != null) blackList.clear();
                param.setResult(blackList);
            }
        };
        Helpers.hookAllMethods("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "getFreeformBlackList", clearHook);
        Helpers.hookAllMethods("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "getFreeformBlackListFromCloud", clearHook);
        Helpers.hookAllMethods("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "setFreeformBlackList", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                List<String> blackList = new ArrayList<String>();
                blackList.add("ab.cd.xyz");
                param.args[0] = blackList;
            }
        });
        Helpers.findAndHookMethod("android.util.MiuiMultiWindowUtils", lpparam.classLoader, "isForceResizeable", XC_MethodReplacement.returnConstant(true));
        Helpers.findAndHookMethod("android.util.MiuiMultiWindowUtils", lpparam.classLoader, "supportFreeform", XC_MethodReplacement.returnConstant(true));
    }

    public static ConcurrentHashMap<String, Pair<Float, Rect>> fwApps = new ConcurrentHashMap<String, Pair<Float, Rect>>();

    public static String getTaskPackageName(Object thisObject, int taskId) {
        return getTaskPackageName(thisObject, taskId, false, null);
    }

    public static String getTaskPackageName(Object thisObject, int taskId, ActivityOptions options) {
        return getTaskPackageName(thisObject, taskId, true, options);
    }

    public static String getTaskPackageName(Object thisObject, int taskId, boolean withOptions, ActivityOptions options) {
        Object mRootWindowContainer = XposedHelpers.getObjectField(thisObject, "mRootWindowContainer");
        if (mRootWindowContainer == null) return null;
        Object task = withOptions ?
                XposedHelpers.callMethod(mRootWindowContainer, "anyTaskForId", taskId, 2, options, true) :
                XposedHelpers.callMethod(mRootWindowContainer, "anyTaskForId", taskId, 0);
        if (task == null) return null;
        Intent intent = (Intent)XposedHelpers.getObjectField(task, "intent");
        return intent == null ? null : intent.getComponent().getPackageName();
    }

    public static String serializeFwApps() {
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, Pair<Float, Rect>> entry: fwApps.entrySet()) {
            Pair<Float, Rect> val = entry.getValue();
            data.append(entry.getKey());
            data.append(":");
            data.append(val.first);
            data.append(":");
            data.append(val.second == null ? "-" : val.second.flattenToString());
            data.append("|");
        }
        return data.toString().replaceFirst("\\|$", "");
    }

    public static void unserializeFwApps(String data) {
        fwApps.clear();
        if (data == null || "".equals(data)) return;
        String[] dataArr = data.split("\\|");
        for (String appData: dataArr) {
            if ("".equals(appData)) continue;
            String[] appDataArr = appData.split(":");
            fwApps.put(appDataArr[0], new Pair<Float, Rect>(Float.parseFloat(appDataArr[1]), "-".equals(appDataArr[2]) ? null : Rect.unflattenFromString(appDataArr[2])));
        }
    }

    public static void storeFwAppsInSetting(Context context) {
        Settings.Global.putString(context.getContentResolver(), Helpers.modulePkg + ".fw.apps", serializeFwApps());
    }

    public static void restoreFwAppsInSetting(Context context) {
        unserializeFwApps(Settings.Global.getString(context.getContentResolver(), Helpers.modulePkg + ".fw.apps"));
    }
    private static ActivityOptions patchActivityOptions(Context mContext, ActivityOptions options, String pkgName, Class<?> MiuiMultiWindowUtils) {
        if (options == null) options = ActivityOptions.makeBasic();
        XposedHelpers.callMethod(options, "setLaunchWindowingMode", 5);
        XposedHelpers.callMethod(options, "setMiuiConfigFlag", 2);

        Float scale;
        Rect rect;
        Pair<Float, Rect> values = fwApps.get(pkgName);
        if (values == null || values.first == 0f || values.second == null) {
            scale = 0.7f;
            rect = (Rect)XposedHelpers.callStaticMethod(MiuiMultiWindowUtils, "getFreeformRect", mContext);
        } else {
            scale = values.first;
            rect = values.second;
        }
        options.setLaunchBounds(rect);
        try {
            Object injector = XposedHelpers.callMethod(options, "getActivityOptionsInjector");
            XposedHelpers.callMethod(injector, "setFreeformScale", scale);
        } catch (Throwable ignore) {
            XposedBridge.log(ignore);
        }
        return options;
    }

    public static void StickyFloatingWindowsHook(LoadPackageParam lpparam) {
        final List<String> fwBlackList = new ArrayList<String>();
        fwBlackList.add("com.miui.securitycenter");
        fwBlackList.add("com.miui.home");
        fwBlackList.add("com.android.camera");
        Class<?> MiuiMultiWindowUtils = findClass("android.util.MiuiMultiWindowUtils", lpparam.classLoader);
        Helpers.hookAllMethods("com.android.server.wm.ActivityStarterInjector", lpparam.classLoader, "modifyLaunchActivityOptionIfNeed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (param.args.length != 8) return;
                Intent intent = (Intent)param.args[5];
                if (intent == null || intent.getComponent() == null) return;
                ActivityOptions options = (ActivityOptions)param.getResult();
                int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");
                String pkgName = intent.getComponent().getPackageName();
                if (fwBlackList.contains(pkgName)) return;
                Context mContext;
                try {
                    mContext = (Context)XposedHelpers.getObjectField(param.args[0], "mContext");
                } catch (Throwable ignore) {
                    mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.args[0], "mService"), "mContext");
                }
                if (windowingMode != 5 && fwApps.containsKey(pkgName)) {
                    try {
                        if (MiuiMultiWindowUtils == null) {
                            Helpers.log("StickyFloatingWindowsHook", "Cannot find MiuiMultiWindowUtils class");
                            return;
                        }
                        options = patchActivityOptions(mContext, options, pkgName, MiuiMultiWindowUtils);
                        param.setResult(options);
                    } catch (Throwable t) {
                        XposedBridge.log(t);
                    }
                }
                else if (windowingMode == 5 && !fwApps.containsKey(pkgName)) {
                    fwApps.put(pkgName, new Pair<Float, Rect>(0f, null));
                    storeFwAppsInSetting(mContext);
                }
            }
        });

        Helpers.hookAllMethods("com.android.server.wm.ActivityTaskSupervisor", lpparam.classLoader, "startActivityFromRecents", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object safeOptions = param.args[3];
                ActivityOptions options = (ActivityOptions)XposedHelpers.callMethod(safeOptions, "getOptions", param.thisObject);
                int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");
                String pkgName = getTaskPackageName(param.thisObject, (int)param.args[2], options);
                if (fwBlackList.contains(pkgName)) return;
                if (windowingMode == 5 && pkgName != null) {
                    fwApps.put(pkgName, new Pair<Float, Rect>(0f, null));
                    Context mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "mService"), "mContext");
                    storeFwAppsInSetting(mContext);
                }
            }
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object safeOptions = param.args[3];
                ActivityOptions options = (ActivityOptions)XposedHelpers.callMethod(safeOptions, "getOptions", param.thisObject);
                int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");
                String pkgName = getTaskPackageName(param.thisObject, (int)param.args[2], options);
                if (fwBlackList.contains(pkgName)) return;
                if (windowingMode != 5 && fwApps.containsKey(pkgName)) {
                    Context mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "mService"), "mContext");
                    options = patchActivityOptions(mContext, options, pkgName, MiuiMultiWindowUtils);
                    XposedHelpers.setObjectField(safeOptions, "mOriginalOptions", options);
                    param.args[3] = safeOptions;
                    Intent intent = new Intent(ACTION_PREFIX + "dismissRecentsWhenFreeWindowOpen");
                    intent.putExtra("package", pkgName);
                    mContext.sendBroadcast(intent);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController$FreeFormReceiver", lpparam.classLoader, "onReceive", new Object[]{Context.class, Intent.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[1];
                String action = intent.getAction();
                if (action == "miui.intent.action_launch_fullscreen_from_freeform") {
                    Object parentThis = XposedHelpers.getSurroundingThis(param.thisObject);
                    XposedHelpers.setAdditionalInstanceField(parentThis, "skipFreeFormStateClear", true);
                }
            }
        }});

        Helpers.hookAllMethods("com.android.server.wm.MiuiFreeFormGestureController", lpparam.classLoader, "notifyFullScreenWidnowModeStart", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (param.args.length != 3) return;
                String pkgName = (String)XposedHelpers.callMethod(param.args[1], "getStackPackageName");
                Object skipClear = XposedHelpers.getAdditionalInstanceField(param.thisObject, "skipFreeFormStateClear");
                boolean skipFreeFormStateClear = false;
                if (skipClear != null) {
                    skipFreeFormStateClear = (boolean) skipClear;
                }
                if (!skipFreeFormStateClear) {
                    if (fwBlackList.contains(pkgName)) return;
                    if (fwApps.remove(pkgName) != null) {
                        storeFwAppsInSetting((Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "mService"), "mContext"));
                    }
                }
                else {
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "skipFreeFormStateClear", false);
                }
            }
        });

        Helpers.hookAllMethods("com.android.server.wm.ActivityTaskManagerService", lpparam.classLoader, "launchSmallFreeFormWindow", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object taskId = XposedHelpers.getObjectField(param.args[0], "taskId");
                Object mMiuiFreeFormManagerService = XposedHelpers.getObjectField(param.thisObject, "mMiuiFreeFormManagerService");
                Object miuiFreeFormActivityStack = XposedHelpers.callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", taskId);
                String pkgName = (String) XposedHelpers.callMethod(miuiFreeFormActivityStack, "getStackPackageName");
                if (fwBlackList.contains(pkgName)) return;
                if (!fwApps.containsKey(pkgName)) {
                    fwApps.put(pkgName, new Pair<Float, Rect>(0f, null));
                    Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                    storeFwAppsInSetting(mContext);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", lpparam.classLoader, "onSystemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                restoreFwAppsInSetting(mContext);
                Class<?> MiuiMultiWindowAdapter = findClass("android.util.MiuiMultiWindowAdapter", lpparam.classLoader);
                List<String> blackList = (List<String>) XposedHelpers.getStaticObjectField(MiuiMultiWindowAdapter, "FREEFORM_BLACK_LIST");
                blackList.clear();
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action == "miui.intent.action_launch_fullscreen_from_freeform") {
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "skipFreeFormStateClear", true);
                        }
                    }
                }, new IntentFilter("miui.intent.action_launch_fullscreen_from_freeform"));
            }
        });

        Helpers.hookAllMethods("com.android.server.wm.ActivityTaskManagerService", lpparam.classLoader, "resizeTask", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String pkgName = getTaskPackageName(param.thisObject, (int)param.args[0]);
                if (pkgName != null) {
                    Object skipClear = XposedHelpers.getAdditionalInstanceField(param.thisObject, "skipFreeFormStateClear");
                    boolean skipFreeFormStateClear = false;
                    if (skipClear != null) {
                        skipFreeFormStateClear = (boolean) skipClear;
                    }
                    if (skipFreeFormStateClear) {
                        XposedHelpers.setAdditionalInstanceField(param.thisObject, "skipFreeFormStateClear", false);
                    }
                    else {
                        if (fwBlackList.contains(pkgName)) return;
                        Object mMiuiFreeFormManagerService = XposedHelpers.getObjectField(param.thisObject, "mMiuiFreeFormManagerService");
                        Object miuiFreeFormActivityStack = XposedHelpers.callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", param.args[0]);
                        if (fwApps.containsKey(pkgName)) {
                            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                            float sScale = (float) XposedHelpers.callMethod(miuiFreeFormActivityStack, "getFreeFormScale");
                            fwApps.put(pkgName, new Pair<Float, Rect>(sScale, new Rect((Rect)param.args[1])));
                            storeFwAppsInSetting(mContext);
                        }
                    }
                }
            }
        });
    }

    public static void StickyFloatingWindowsLauncherHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.miui.home.recents.views.RecentsContainer", lpparam.classLoader, "onAttachedToWindow", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getContext");
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            String pkgName = intent.getStringExtra("package");
                            if (pkgName != null) {
                                XposedHelpers.callMethod(param.thisObject, "dismissRecentsToLaunchTargetTaskOrHome", pkgName, true);
                            }
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                }, new IntentFilter(ACTION_PREFIX + "dismissRecentsWhenFreeWindowOpen"));
            }
        });
    }

    public static void MessagingStyleLinesSysHook() {
        Helpers.findAndHookMethod("android.app.Notification.MessagingStyle", null, "makeMessagingView", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if ((boolean)param.args[0] && (boolean)param.args[1]) return;
                RemoteViews remote = (RemoteViews)param.getResult();
                Context mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "mBuilder"), "mContext");
                remote.setInt(mContext.getResources().getIdentifier("notification_messaging", "id", "android"), "setMaxDisplayedLines", MainModule.mPrefs.getInt("system_messagingstylelines", Integer.MAX_VALUE));
            }
        });
    }

    public static void MessagingStyleLinesHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.notification.NotificationMessagingTemplateViewWrapper", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mMessagingLinearLayout = XposedHelpers.getObjectField(param.thisObject, "mMessagingLinearLayout");
                int mMaxDisplayedLines = XposedHelpers.getIntField(mMessagingLinearLayout, "mMaxDisplayedLines");
                if (mMaxDisplayedLines == Integer.MAX_VALUE) XposedHelpers.callMethod(mMessagingLinearLayout, "setMaxDisplayedLines", MainModule.mPrefs.getInt("system_messagingstylelines", Integer.MAX_VALUE));
            }
        });
    }

    public static void MultiWindowPlusHook(LoadPackageParam lpparam) {
        MainModule.resHooks.setResReplacement("android", "array", "miui_resize_black_list", R.array.miui_resize_black_list);
        if (!lpparam.packageName.equals("android")) {
            Helpers.hookAllMethods("com.miui.home.recents.views.RecentMenuView", lpparam.classLoader, "onMessageEvent", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ImageView mMenuItemMultiWindow = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mMenuItemMultiWindow");
                            ImageView mMenuItemSmallWindow = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mMenuItemSmallWindow");
                            mMenuItemMultiWindow.setEnabled(true);
                            mMenuItemMultiWindow.setImageAlpha(255);
                            mMenuItemSmallWindow.setEnabled(true);
                            mMenuItemSmallWindow.setImageAlpha(255);
                        }
                    }, 200);
                }
            });
        }
        else {
            Class <?> AtmClass = XposedHelpers.findClassIfExists("com.android.server.wm.ActivityTaskManagerServiceImpl", lpparam.classLoader);
            if (AtmClass != null) {
                Helpers.hookAllMethods(AtmClass, "inResizeBlackList", new MethodHook() {
                    @Override
                    protected void before(MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                });
                return;
            }
        }
    }

    public static void SecureControlCenterHook(LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("com.android.keyguard.utils.MiuiKeyguardUtils", lpparam.classLoader, "supportExpandableStatusbarUnderKeyguard", XC_MethodReplacement.returnConstant(false));

        Helpers.hookAllMethods("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.classLoader, "onContentChanged", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String key = (String) param.args[0];
                if ("expandable_under_lock_screen".equals(key)) {
                    param.args[1] = "0";
                }
            }
        });
    }

    public static void MinimalNotificationViewHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateNotification", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
            if (param.args.length != 3) return;
            Object expandableRow = XposedHelpers.getObjectField(param.args[0], "row");
            Object mNotificationData = XposedHelpers.getObjectField(param.thisObject, "mNotificationData");
            boolean newLowPriority = (boolean)XposedHelpers.callMethod(mNotificationData, "isAmbient", XposedHelpers.callMethod(param.args[1], "getKey")) && !(boolean)XposedHelpers.callMethod(XposedHelpers.callMethod(param.args[1], "getNotification"), "isGroupSummary");
            boolean hasEntry = XposedHelpers.callMethod(mNotificationData, "get", XposedHelpers.getObjectField(param.args[0], "key")) != null;
            boolean isLowPriority = (boolean)XposedHelpers.callMethod(expandableRow, "isLowPriority");
            XposedHelpers.callMethod(expandableRow, "setIsLowPriority", newLowPriority);
            boolean hasLowPriorityChanged = hasEntry && isLowPriority != newLowPriority;
            XposedHelpers.callMethod(expandableRow, "setLowPriorityStateUpdated", hasLowPriorityChanged);
            XposedHelpers.callMethod(expandableRow, "updateNotification", param.args[0]);
            }
        });
    }

    public static void NotificationChannelSettingsHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.classLoader, "onClickInfoItem", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)param.args[0];
                Object entry = XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mParent"), "getEntry");
                String id = (String)XposedHelpers.callMethod(XposedHelpers.callMethod(entry, "getChannel"), "getId");
                if ("miscellaneous".equals(id)) return;
                Object notification = XposedHelpers.callMethod(entry, "getSbn");
                Class<?> nuCls = findClassIfExists("com.android.systemui.miui.statusbar.notification.NotificationUtil", lpparam.classLoader);
                if (nuCls != null) {
                    boolean isHybrid = (boolean)XposedHelpers.callStaticMethod(nuCls, "isHybrid", notification);
                    if (isHybrid) return;
                }
                String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                int user = (int)XposedHelpers.callMethod(notification, "getAppUid");

                Bundle bundle = new Bundle();
                bundle.putString("android.provider.extra.CHANNEL_ID", id);
                bundle.putString("package", pkgName);
                bundle.putInt("uid", user);
                bundle.putString("miui.targetPkg", pkgName);
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(":android:show_fragment", "com.android.settings.notification.ChannelNotificationSettings");
                intent.putExtra(":android:show_fragment_args", bundle);
                intent.setClassName("com.android.settings", "com.android.settings.SubSettings");
                try {
                    XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, android.os.Process.myUserHandle());
                    param.setResult(null);
                    Object ModalController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", mContext.getClassLoader()), "get", findClass("com.android.systemui.statusbar.notification.modal.ModalController", mContext.getClassLoader()));
                    XposedHelpers.callMethod(ModalController, "animExitModelCollapsePanels");
                } catch (Throwable ignore) {
                    Helpers.log(ignore);
                }
            }
        });
    }

    public static void SkipAppLockHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.miui.server.AccessController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mWorkHandler");

                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String type = uri.getPathSegments().get(1);
                            String key = uri.getPathSegments().get(2);
                            if (key.contains("pref_key_system_applock_skip_activities") && "string".equals(type))
                                MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, ""));
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
            }
        });

        Helpers.hookAllMethods("com.miui.server.AccessController", lpparam.classLoader, "skipActivity", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Intent intent = (Intent)param.args[0];
                if (intent == null || intent.getComponent() == null) return;
                String pkgName = intent.getComponent().getPackageName();
                String actName = intent.getComponent().getClassName();
                String key = "system_applock_skip_activities";
                String itemStr = MainModule.mPrefs.getString(key, "");
                if (itemStr == null || itemStr.isEmpty()) return;
                String[] itemArr = itemStr.trim().split("\\|");
                for (String uuid: itemArr) {
                    String pkgAct = MainModule.mPrefs.getString(key + "_" + uuid + "_activity", "");
                    if (pkgAct.equals(pkgName + "|" + actName)) param.setResult(true);
                }
            }
        });
    }

    private static final List<String> securedTiles = new ArrayList<String>();

    public static void SecureQSTilesHook(LoadPackageParam lpparam) {
        Class<?> tileHostCls = XposedHelpers.findClassIfExists("com.android.systemui.qs.QSTileHost", lpparam.classLoader);

        MethodHook hook = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                BroadcastReceiver mAfterUnlockReceiver = new BroadcastReceiver() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void onReceive(Context context, Intent intent) {
                        try {
                            String tileName = intent.getStringExtra("tileName");
                            boolean expandAfter = intent.getBooleanExtra("expandAfter", false);
                            boolean usingCenter = intent.getBooleanExtra("usingCenter", false);
                            if ("edit".equals(tileName) || expandAfter) {
                                Intent expandIntent = new Intent(ACTION_PREFIX + "ExpandSettings");
                                expandIntent.putExtra("forceExpand", true);
                                context.sendBroadcast(expandIntent);
                            }
                            LinkedHashMap<String, ?> mTiles = (LinkedHashMap<String, ?>)XposedHelpers.getObjectField(param.thisObject, "mTiles");
                            Object tile = mTiles.get(tileName);
                            if (tile == null) {
                                if (usingCenter) {
                                    Object mController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClassIfExists("com.android.systemui.miui.statusbar.policy.ControlPanelController", lpparam.classLoader));
                                    Object mControlCenter = XposedHelpers.getObjectField(mController, "mControlCenter");
                                    Object mControlPanelContentView = XposedHelpers.getObjectField(mControlCenter, "mControlPanelContentView");
                                    Object mControlCenterPanel = XposedHelpers.callMethod(mControlPanelContentView, "getControlCenterPanel");
                                    Object mBigTile = null;
                                    if ("bt".equals(tileName)) mBigTile = XposedHelpers.getObjectField(mControlCenterPanel, "mBigTile1");
                                    else if ("cell".equals(tileName)) mBigTile = XposedHelpers.getObjectField(mControlCenterPanel, "mBigTile2");
                                    else if ("wifi".equals(tileName)) mBigTile = XposedHelpers.getObjectField(mControlCenterPanel, "mBigTile3");
                                    if (mBigTile != null) tile = XposedHelpers.getObjectField(mBigTile, "mQSTile");
                                    if (tile == null) return;
                                }
                                return;
                            }
                            XposedHelpers.setAdditionalInstanceField(tile, "mCalledAfterUnlock", true);
                            Method clickHandler = XposedHelpers.findMethodExact(tile.getClass(), "handleClick", View.class);
                            clickHandler.invoke(tile, new Object[]{ null });
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }
                    }
                };
                mContext.registerReceiver(mAfterUnlockReceiver, new IntentFilter(ACTION_PREFIX + "HandleQSTileClick"));
            }
        };

        Helpers.hookAllConstructors(tileHostCls, hook);

        Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSFactoryImpl", lpparam.classLoader, "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object tile = param.getResult();
                if (tile == null) return;
                String tileClass = tile.getClass().getCanonicalName();
                final String tileName = (String)param.args[0];
                String name = tileName;
                if (name.startsWith("intent(")) name = "intent";
                else if (name.startsWith("custom(")) name = "custom";
                final HashSet<String> secureTitles = new HashSet<String>();
                if (MainModule.mPrefs.getBoolean("system_secureqs_wifi")) secureTitles.add("wifi");
                if (MainModule.mPrefs.getBoolean("system_secureqs_bt")) secureTitles.add("bt");
                if (MainModule.mPrefs.getBoolean("system_secureqs_mobiledata")) secureTitles.add("cell");
                if (MainModule.mPrefs.getBoolean("system_secureqs_airplane")) secureTitles.add("airplane");
                if (MainModule.mPrefs.getBoolean("system_secureqs_location")) secureTitles.add("gps");
                if (MainModule.mPrefs.getBoolean("system_secureqs_hotspot")) secureTitles.add("hotspot");
                if (MainModule.mPrefs.getBoolean("system_secureqs_nfc")) secureTitles.add("nfc");
                if (MainModule.mPrefs.getBoolean("system_secureqs_sync")) secureTitles.add("sync");
                if (MainModule.mPrefs.getBoolean("system_secureqs_edit")) secureTitles.add("edit");
                if (MainModule.mPrefs.getBoolean("system_secureqs_custom")) {
                    secureTitles.add("intent");
                    secureTitles.add("custom");
                }
                if (secureTitles.contains(name) && !securedTiles.contains(tileClass)) {
                    MethodHook hook = new MethodHook(10) {
                        @Override
                        protected void before(final MethodHookParam param) throws Throwable {
                            Boolean mCalledAfterUnlock = (Boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCalledAfterUnlock");
                            if (mCalledAfterUnlock != null && mCalledAfterUnlock) {
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCalledAfterUnlock", false);
                                return;
                            }
                            Boolean isScreenLockDisabled = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader), "isScreenLockDisabled");
                            isScreenLockDisabled = isScreenLockDisabled != null && isScreenLockDisabled;
                            if (isScreenLockDisabled) return;
                            Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                            KeyguardManager kgMgr = (KeyguardManager)mContext.getSystemService(Context.KEYGUARD_SERVICE);
                            if (!kgMgr.isKeyguardLocked() || !kgMgr.isKeyguardSecure()) return;
                            Handler mHandler = new Handler(mContext.getMainLooper());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Class<?> DependencyClass = findClass("com.android.systemui.Dependency", lpparam.classLoader);
                                        Object mStatusBar = XposedHelpers.callStaticMethod(DependencyClass, "get", findClassIfExists("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader));
                                        boolean usingControlCenter;
                                        Object mController = XposedHelpers.callStaticMethod(DependencyClass, "get", findClassIfExists("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.classLoader));
                                        usingControlCenter = (boolean)XposedHelpers.callMethod(mController, "isUseControlCenter");
                                        if (usingControlCenter) XposedHelpers.callMethod(mController, "collapseControlCenter", true);
                                        boolean keepOpened = MainModule.mPrefs.getBoolean("system_secureqs_keepopened");
                                        final boolean usingCenter = usingControlCenter;
                                        final boolean expandAfter = usingControlCenter && keepOpened;
//                                        if (!usingControlCenter && keepOpened)
//                                            XposedHelpers.callMethod(XposedHelpers.getObjectField(mStatusBar, "mStatusBarStateController"), "setLeaveOpenOnKeyguardHide", true);
                                        XposedHelpers.callMethod(mStatusBar, "postQSRunnableDismissingKeyguard", !keepOpened, new Runnable() {
                                            public void run() {
                                                Intent intent = new Intent(ACTION_PREFIX + "HandleQSTileClick");
                                                intent.putExtra("tileName", tileName);
                                                intent.putExtra("expandAfter", expandAfter);
                                                intent.putExtra("usingCenter", usingCenter);
                                                mContext.sendBroadcast(intent);
                                            }
                                        });
                                    } catch (Throwable t) {
                                        XposedBridge.log(t);
                                    }
                                }
                            });
                            param.setResult(null);
                        }
                    };
                    Helpers.findAndHookMethod(tileClass, lpparam.classLoader, "handleClick", View.class, hook);
                    Helpers.hookAllMethodsSilently(tileClass, lpparam.classLoader, "handleSecondaryClick", hook);
                    securedTiles.add(tileClass);
                }
            }
        });
    }

    public static void HideLockScreenHintHook(LoadPackageParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                XposedHelpers.setObjectField(param.thisObject, "mUpArrowIndication", null);
            }
        };
        if (Helpers.isTPlus()) {
            Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardIndicationRotateTextViewController", lpparam.classLoader, "hasIndicationsExceptResting", XC_MethodReplacement.returnConstant(true));
        }
        else {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.KeyguardIndicationController", lpparam.classLoader, "updateIndication", boolean.class, boolean.class, hook);
        }
    }

    public static void HideLockScreenStatusBarHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods(StatusBarCls, lpparam.classLoader, "makeStatusBarView", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
            View mKeyguardStatusBar = (View) XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "mNotificationPanelViewController"), "mKeyguardStatusBar");
            mKeyguardStatusBar.setTranslationY(-999f);
            }
        });
    }

    public static void MuteVisibleNotificationsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.media.NotificationPlayer$CreationAndCompletionThread", lpparam.classLoader, "run", new MethodHook() {
            @Override
            @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
            protected void before(final MethodHookParam param) throws Throwable {
                Object mCmd = XposedHelpers.getObjectField(param.thisObject, "mCmd");
                if (mCmd == null) return;
                int code = XposedHelpers.getIntField(mCmd, "code");
                if (code != 1) return;
                AudioAttributes attributes = (AudioAttributes)XposedHelpers.getObjectField(mCmd, "attributes");
                if (attributes.getUsage() == AudioAttributes.USAGE_NOTIFICATION || attributes.getUsage() == AudioAttributes.USAGE_NOTIFICATION_RINGTONE || attributes.getUsage() == AudioAttributes.USAGE_UNKNOWN)
                    if (attributes.getContentType() == AudioAttributes.CONTENT_TYPE_SONIFICATION || attributes.getContentType() == AudioAttributes.CONTENT_TYPE_UNKNOWN) {
                        Context context = (Context)XposedHelpers.getObjectField(mCmd, "context");
                        PowerManager powerMgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                        if (powerMgr.isInteractive()) {
                            Thread thread = (Thread)param.thisObject;
                            synchronized (thread) { thread.notify(); }
                            param.setResult(null);
                        }
                    }
            }
        });
    }

    public static void NetworkIndicatorRes(LoadPackageParam lpparam) {
        MethodHook hideMobileActivity = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object mLeftInOut = XposedHelpers.getObjectField(param.thisObject, "mLeftInOut");
                Object mRightInOut = XposedHelpers.getObjectField(param.thisObject, "mRightInOut");
                XposedHelpers.callMethod(mLeftInOut, "setVisibility", 8);
                XposedHelpers.callMethod(mRightInOut, "setVisibility", 8);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", hideMobileActivity);

        MethodHook hideWifiActivity = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object mWifiActivityView = XposedHelpers.getObjectField(param.thisObject, "mWifiActivityView");
                XposedHelpers.callMethod(mWifiActivityView, "setVisibility", 4);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.classLoader, "applyWifiState", hideWifiActivity);
    }

    public static void ClearBrightnessMirrorHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.classLoader, "showMirror", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                View mBrightnessMirror = (View)XposedHelpers.getObjectField(param.thisObject, "mBrightnessMirror");
                mBrightnessMirror.setElevation(0);
                mBrightnessMirror.setBackgroundColor(Color.TRANSPARENT);
            }
        });
    }

    public static void SetLockscreenWallpaperHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.wallpaper.WallpaperManagerService", lpparam.classLoader, "setWallpaper", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                if (param.getThrowable() != null || param.getResult() == null || (int)param.args[5] == 1 || "com.android.thememanager".equals(param.args[1])) return;

                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (mContext == null) return;

                int handleIncomingUser = 0;
                try {
                    handleIncomingUser = (int)XposedHelpers.callStaticMethod(ActivityManager.class, "handleIncomingUser", Binder.getCallingPid(), Binder.getCallingUid(), param.args[7], false, true, "changing wallpaper", null);
                } catch (Throwable ignore) {}
                Object wallpaperData = XposedHelpers.callMethod(param.thisObject, "getWallpaperSafeLocked", handleIncomingUser, param.args[5]);
                File wallpaper = (File)XposedHelpers.getObjectField(wallpaperData, "wallpaperFile");

                new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!wallpaper.exists()) return;

                        String lockWallpaperPath = "/data/system/theme/thirdparty_lock_wallpaper";
                        Helpers.copyFile(wallpaper.getAbsolutePath(), lockWallpaperPath);
                        Class<?> ThemeUtils = XposedHelpers.findClass("miui.content.res.ThemeNativeUtils", lpparam.classLoader);
                        XposedHelpers.callStaticMethod(ThemeUtils, "updateFilePermissionWithThemeContext", lockWallpaperPath);
                        JSONObject data = new JSONObject();
                        JSONObject ex = new JSONObject();
                        try {
                            File lockWallpaper = new File(lockWallpaperPath);
                            ex
                                    .put("link_type", "0")
                                    .put("title_size", "26")
                                    .put("item_id", "wallpaper1")
                                    .put("title_color", "#ffffffff")
                                    .put("index_in_album", "1")
                                    .put("tag_list", "CustoMIUIzer,mod")
                                    .put("content_color", "#ffffffff")
                                    .put("total_of_album", "1")
                                    .put("img_level", "0")
                                    .put("album_id", "1")
                                    .put("title_customized", "0")
                                    .put("lks_entry_text", "Some wallpaper");

                            data
                                    .put("authority", "name.mikanoshi.customiuizer.mods.set_lockscreen_wallpaper")
                                    .put("content", "Wallpaper set by some app")
                                    .put("contentColorValue", 0)
                                    .put("cp", "CustoMIUIzer")
                                    .put("cpColorValue", 0)
                                    .put("definition", -1)
                                    .put("ex", ex.toString())
                                    .put("fromColorValue", 0)
                                    .put("hasAcc", false)
                                    .put("indexInAlbum", -1)
                                    .put("isAd", false)
                                    .put("isCustom", false)
                                    .put("isFd", false)
                                    .put("isFrontCover", false)
                                    .put("key", "wallpaper1")
                                    .put("like", false)
                                    .put("linkType", 0)
                                    .put("noApply", false)
                                    .put("noDislike", false)
                                    .put("noSave", false)
                                    .put("noShare", false)
                                    .put("pos", 0)
                                    .put("supportLike", true)
                                    .put("title", "Some wallpaper")
                                    .put("titleColorValue", 0)
                                    .put("titleTextSize", -1)
                                    .put("totalOfAlbum", -1)
                                    .put("wallpaperUri", lockWallpaper.toURI());
                        } catch (Throwable t) {
                            XposedBridge.log(t);
                        }

                        Intent setIntent = new Intent("com.miui.miwallpaper.UPDATE_LOCKSCREEN_WALLPAPER");
                        setIntent.putExtra("wallpaperInfo", data.toString());
                        setIntent.putExtra("apply", true);
                        mContext.sendBroadcast(setIntent);
                    }
                }, 1800);
            }
        });


    }

    public static void BetterPopupsCenteredHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "getHeadsUpTopMargin", Context.class, new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context context = (Context)param.args[0];
                Resources res = context.getResources();
                int maxPopupHeight = res.getDimensionPixelSize(res.getIdentifier("notification_max_heads_up_height", "dimen", "com.android.systemui"));
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) maxPopupHeight /= 3;
                param.setResult(Math.round(context.getResources().getDisplayMetrics().heightPixels / 2.0f - maxPopupHeight / 2.0f));
            }
        });
    }

    public static void HorizMarginHook(LoadPackageParam lpparam) {
        MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "status_bar_padding_start", 0);
        MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "status_bar_padding_end", 0);
        String StatusBarWindowViewCls = Helpers.isTPlus() ? "com.android.systemui.statusbar.window.StatusBarWindowView" : "com.android.systemui.statusbar.phone.StatusBarWindowView";
        Helpers.hookAllMethods(StatusBarWindowViewCls, lpparam.classLoader, "paddingNeededForCutoutAndRoundedCorner", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Context context = Helpers.findContext();
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_left", 16);
                float marginLeft = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    leftMargin,
                    context.getResources().getDisplayMetrics()
                );
                leftMargin = (int) marginLeft;
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_right", 16);
                float marginRight = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    rightMargin,
                    context.getResources().getDisplayMetrics()
                );
                rightMargin = (int) marginRight;
                param.setResult(new Pair<Integer, Integer>(Integer.valueOf(leftMargin), Integer.valueOf(rightMargin)));
            }
        });
    }

    public static void MobileTypeSingleHook(LoadPackageParam lpparam) {
        MethodHook showSingleMobileType = new MethodHook(MethodHook.PRIORITY_HIGHEST) {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object mobileIconState = param.args[0];
                XposedHelpers.setObjectField(mobileIconState, "showMobileDataTypeSingle", true);
                XposedHelpers.setObjectField(mobileIconState, "fiveGDrawableId", 0);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", showSingleMobileType);

        MethodHook afterUpdate = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object mMobileLeftContainer = XposedHelpers.getObjectField(param.thisObject, "mMobileLeftContainer");
                XposedHelpers.callMethod(mMobileLeftContainer, "setVisibility", 8);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", afterUpdate);

        Helpers.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "init", new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                Resources res = mContext.getResources();
                LinearLayout mMobileGroup = (LinearLayout) XposedHelpers.getObjectField(param.thisObject, "mMobileGroup");
                TextView mMobileTypeSingle = (TextView) XposedHelpers.getObjectField(param.thisObject, "mMobileTypeSingle");
                mMobileGroup.removeView(mMobileTypeSingle);
                mMobileGroup.addView(mMobileTypeSingle);
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mMobileTypeSingle.getLayoutParams();
                int leftMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_leftmargin", 4);
                float marginLeft = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    leftMargin * 0.5f,
                    res.getDisplayMetrics()
                );
                mlp.leftMargin = (int) marginLeft;
                int rightMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_rightmargin", 0);
                if (rightMargin > 0) {
                    float marginRight = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        rightMargin * 0.5f,
                        res.getDisplayMetrics()
                    );
                    mlp.rightMargin = (int) marginRight;
                }
                int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_verticaloffset", 8);
                if (verticalOffset != 8) {
                    float marginTop = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        (verticalOffset - 8) * 0.5f,
                        res.getDisplayMetrics()
                    );
                    mlp.topMargin = (int) marginTop;
                }
                mMobileTypeSingle.setLayoutParams(mlp);
                int fontSize = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_fontsize", 27);
                mMobileTypeSingle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f);
            }
        });
    }

    public static void DualRowSignalHook(LoadPackageParam lpparam) {
        boolean mobileTypeSingle = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single");
        if (!mobileTypeSingle) {
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_half_to_top_distance", 3);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_mobile_left_inout_over_strength", 0);
            MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_middle_to_strength_start", -0.4f);
        }

        SparseIntArray signalResToLevelMap = new SparseIntArray();
        int signalRes1_0 = MainModule.resHooks.addResource("signalRes1_0", R.drawable.statusbar_signal_1_0);
        int signalRes1_1 = MainModule.resHooks.addResource("signalRes1_1", R.drawable.statusbar_signal_1_1);
        int signalRes1_2 = MainModule.resHooks.addResource("signalRes1_2", R.drawable.statusbar_signal_1_2);
        int signalRes1_3 = MainModule.resHooks.addResource("signalRes1_3", R.drawable.statusbar_signal_1_3);
        int signalRes1_4 = MainModule.resHooks.addResource("signalRes1_4", R.drawable.statusbar_signal_1_4);
        int signalRes1_5 = MainModule.resHooks.addResource("signalRes1_5", R.drawable.statusbar_signal_1_5);
        int signalRes2_0 = MainModule.resHooks.addResource("signalRes2_0", R.drawable.statusbar_signal_2_0);
        int signalRes2_1 = MainModule.resHooks.addResource("signalRes2_1", R.drawable.statusbar_signal_2_1);
        int signalRes2_2 = MainModule.resHooks.addResource("signalRes2_2", R.drawable.statusbar_signal_2_2);
        int signalRes2_3 = MainModule.resHooks.addResource("signalRes2_3", R.drawable.statusbar_signal_2_3);
        int signalRes2_4 = MainModule.resHooks.addResource("signalRes2_4", R.drawable.statusbar_signal_2_4);
        int signalRes2_5 = MainModule.resHooks.addResource("signalRes2_5", R.drawable.statusbar_signal_2_5);
        SparseIntArray signalLevelToRes1Map = new SparseIntArray();
        SparseIntArray signalLevelToRes2Map = new SparseIntArray();
        signalLevelToRes1Map.put(0, signalRes1_0);
        signalLevelToRes1Map.put(1, signalRes1_1);
        signalLevelToRes1Map.put(2, signalRes1_2);
        signalLevelToRes1Map.put(3, signalRes1_3);
        signalLevelToRes1Map.put(4, signalRes1_4);
        signalLevelToRes1Map.put(5, signalRes1_5);
        signalLevelToRes1Map.put(6, signalRes1_0);
        signalLevelToRes2Map.put(0, signalRes2_0);
        signalLevelToRes2Map.put(1, signalRes2_1);
        signalLevelToRes2Map.put(2, signalRes2_2);
        signalLevelToRes2Map.put(3, signalRes2_3);
        signalLevelToRes2Map.put(4, signalRes2_4);
        signalLevelToRes2Map.put(5, signalRes2_5);
        signalLevelToRes2Map.put(6, signalRes2_0);

        int signalTintRes1_0 = MainModule.resHooks.addResource("signalTintRes1_0", R.drawable.statusbar_signal_1_0_tint);
        int signalTintRes1_1 = MainModule.resHooks.addResource("signalTintRes1_1", R.drawable.statusbar_signal_1_1_tint);
        int signalTintRes1_2 = MainModule.resHooks.addResource("signalTintRes1_2", R.drawable.statusbar_signal_1_2_tint);
        int signalTintRes1_3 = MainModule.resHooks.addResource("signalTintRes1_3", R.drawable.statusbar_signal_1_3_tint);
        int signalTintRes1_4 = MainModule.resHooks.addResource("signalTintRes1_4", R.drawable.statusbar_signal_1_4_tint);
        int signalTintRes1_5 = MainModule.resHooks.addResource("signalTintRes1_5", R.drawable.statusbar_signal_1_5_tint);
        int signalTintRes2_0 = MainModule.resHooks.addResource("signalTintRes2_0", R.drawable.statusbar_signal_2_0_tint);
        int signalTintRes2_1 = MainModule.resHooks.addResource("signalTintRes2_1", R.drawable.statusbar_signal_2_1_tint);
        int signalTintRes2_2 = MainModule.resHooks.addResource("signalTintRes2_2", R.drawable.statusbar_signal_2_2_tint);
        int signalTintRes2_3 = MainModule.resHooks.addResource("signalTintRes2_3", R.drawable.statusbar_signal_2_3_tint);
        int signalTintRes2_4 = MainModule.resHooks.addResource("signalTintRes2_4", R.drawable.statusbar_signal_2_4_tint);
        int signalTintRes2_5 = MainModule.resHooks.addResource("signalTintRes2_5", R.drawable.statusbar_signal_2_5_tint);
        SparseIntArray signalTintLevelToRes1Map = new SparseIntArray();
        SparseIntArray signalTintLevelToRes2Map = new SparseIntArray();
        signalTintLevelToRes1Map.put(0, signalTintRes1_0);
        signalTintLevelToRes1Map.put(1, signalTintRes1_1);
        signalTintLevelToRes1Map.put(2, signalTintRes1_2);
        signalTintLevelToRes1Map.put(3, signalTintRes1_3);
        signalTintLevelToRes1Map.put(4, signalTintRes1_4);
        signalTintLevelToRes1Map.put(5, signalTintRes1_5);
        signalTintLevelToRes1Map.put(6, signalTintRes1_0);
        signalTintLevelToRes2Map.put(0, signalTintRes2_0);
        signalTintLevelToRes2Map.put(1, signalTintRes2_1);
        signalTintLevelToRes2Map.put(2, signalTintRes2_2);
        signalTintLevelToRes2Map.put(3, signalTintRes2_3);
        signalTintLevelToRes2Map.put(4, signalTintRes2_4);
        signalTintLevelToRes2Map.put(5, signalTintRes2_5);
        signalTintLevelToRes2Map.put(6, signalTintRes2_0);

        int signalDarkRes1_0 = MainModule.resHooks.addResource("signalDarkRes1_0", R.drawable.statusbar_signal_1_0_dark);
        int signalDarkRes1_1 = MainModule.resHooks.addResource("signalDarkRes1_1", R.drawable.statusbar_signal_1_1_dark);
        int signalDarkRes1_2 = MainModule.resHooks.addResource("signalDarkRes1_2", R.drawable.statusbar_signal_1_2_dark);
        int signalDarkRes1_3 = MainModule.resHooks.addResource("signalDarkRes1_3", R.drawable.statusbar_signal_1_3_dark);
        int signalDarkRes1_4 = MainModule.resHooks.addResource("signalDarkRes1_4", R.drawable.statusbar_signal_1_4_dark);
        int signalDarkRes1_5 = MainModule.resHooks.addResource("signalDarkRes1_5", R.drawable.statusbar_signal_1_5_dark);
        int signalDarkRes2_0 = MainModule.resHooks.addResource("signalDarkRes2_0", R.drawable.statusbar_signal_2_0_dark);
        int signalDarkRes2_1 = MainModule.resHooks.addResource("signalDarkRes2_1", R.drawable.statusbar_signal_2_1_dark);
        int signalDarkRes2_2 = MainModule.resHooks.addResource("signalDarkRes2_2", R.drawable.statusbar_signal_2_2_dark);
        int signalDarkRes2_3 = MainModule.resHooks.addResource("signalDarkRes2_3", R.drawable.statusbar_signal_2_3_dark);
        int signalDarkRes2_4 = MainModule.resHooks.addResource("signalDarkRes2_4", R.drawable.statusbar_signal_2_4_dark);
        int signalDarkRes2_5 = MainModule.resHooks.addResource("signalDarkRes2_5", R.drawable.statusbar_signal_2_5_dark);
        SparseIntArray signalDarkLevelToRes1Map = new SparseIntArray();
        SparseIntArray signalDarkLevelToRes2Map = new SparseIntArray();
        signalDarkLevelToRes1Map.put(0, signalDarkRes1_0);
        signalDarkLevelToRes1Map.put(1, signalDarkRes1_1);
        signalDarkLevelToRes1Map.put(2, signalDarkRes1_2);
        signalDarkLevelToRes1Map.put(3, signalDarkRes1_3);
        signalDarkLevelToRes1Map.put(4, signalDarkRes1_4);
        signalDarkLevelToRes1Map.put(5, signalDarkRes1_5);
        signalDarkLevelToRes1Map.put(6, signalDarkRes1_0);
        signalDarkLevelToRes2Map.put(0, signalDarkRes2_0);
        signalDarkLevelToRes2Map.put(1, signalDarkRes2_1);
        signalDarkLevelToRes2Map.put(2, signalDarkRes2_2);
        signalDarkLevelToRes2Map.put(3, signalDarkRes2_3);
        signalDarkLevelToRes2Map.put(4, signalDarkRes2_4);
        signalDarkLevelToRes2Map.put(5, signalDarkRes2_5);
        signalDarkLevelToRes2Map.put(6, signalDarkRes2_0);

        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setMobileIcons", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    Resources res = mContext.getResources();
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_0", "drawable", lpparam.packageName), 0);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_1", "drawable", lpparam.packageName), 1);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_2", "drawable", lpparam.packageName), 2);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_3", "drawable", lpparam.packageName), 3);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_4", "drawable", lpparam.packageName), 4);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_5", "drawable", lpparam.packageName), 5);
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_null", "drawable", lpparam.packageName), 6);
                }
                List<?> iconStates = (List<?>) param.args[1];
                if (iconStates.size() == 2) {
                    Object mainIconState = iconStates.get(0);
                    Object subIconState = iconStates.get(1);
                    boolean mainDataConnected = (boolean) XposedHelpers.getObjectField(mainIconState, "dataConnected");
                    boolean subDataConnected = (boolean) XposedHelpers.getObjectField(subIconState, "dataConnected");
                    XposedHelpers.setObjectField(mainIconState, "dataConnected", mainDataConnected || subDataConnected);
                    XposedHelpers.setObjectField(subIconState, "visible", false);
                    int mainSignalResId = (int) XposedHelpers.getObjectField(mainIconState, "strengthId");
                    int subSignalResId = (int) XposedHelpers.getObjectField(subIconState, "strengthId");
                    int mainLevel = signalResToLevelMap.get(mainSignalResId);
                    int subLevel = signalResToLevelMap.get(subSignalResId);
                    int level;
                    if (subDataConnected) {
                        level = subLevel * 10 + mainLevel;
                        String showName = (String) XposedHelpers.getObjectField(subIconState, "showName");
                        XposedHelpers.setObjectField(mainIconState, "showName", showName);
                    }
                    else {
                        level = mainLevel * 10 + subLevel;
                    }
                    XposedHelpers.setObjectField(mainIconState, "strengthId", level);
                    param.args[1] = iconStates;
                }
            }
        });

        MethodHook beforeUpdate = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                Object mobileIconState = param.args[0];
                boolean visible = (boolean) XposedHelpers.getObjectField(mobileIconState, "visible");
                boolean airplane = (boolean) XposedHelpers.getObjectField(mobileIconState, "airplane");
                int level = (int) XposedHelpers.getObjectField(mobileIconState, "strengthId");
                if (!visible || airplane || level == 0 || level > 100) {
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "subStrengthId", -1);
                }
                else {
                    XposedHelpers.setAdditionalInstanceField(param.thisObject, "subStrengthId", level % 10);
                    XposedHelpers.setObjectField(mobileIconState, "fiveGDrawableId", 0);
                }
            }
        };
        MethodHook afterUpdate = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                int subStrengthId = (int) XposedHelpers.getAdditionalInstanceField(param.thisObject, "subStrengthId");
                if (subStrengthId < 0) return;
                Object mSmallHd = XposedHelpers.getObjectField(param.thisObject, "mSmallHd");
                XposedHelpers.callMethod(mSmallHd, "setVisibility", 8);
                Object mSmallRoaming = XposedHelpers.getObjectField(param.thisObject, "mSmallRoaming");
                XposedHelpers.callMethod(mSmallRoaming, "setVisibility", 0);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", beforeUpdate);
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", afterUpdate);

        MethodHook resetImageDrawable = new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                int subStrengthId = (int) XposedHelpers.getAdditionalInstanceField(param.thisObject, "subStrengthId");
                if (subStrengthId < 0) return;
                Object mobileIconState = XposedHelpers.getObjectField(param.thisObject, "mState");
                int level1 = (int) XposedHelpers.getObjectField(mobileIconState, "strengthId");
                level1 = level1 / 10;
                int level2 = subStrengthId;
                boolean mLight = (boolean) XposedHelpers.getObjectField(param.thisObject, "mLight");
                boolean mUseTint = (boolean) XposedHelpers.getObjectField(param.thisObject, "mUseTint");
                Object mSmallRoaming = XposedHelpers.getObjectField(param.thisObject, "mSmallRoaming");
                Object mMobile = XposedHelpers.getObjectField(param.thisObject, "mMobile");
                int sim1ResId;
                int sim2ResId;
                if (mUseTint) {
                    sim1ResId = signalTintLevelToRes1Map.get(level1);
                    sim2ResId = signalTintLevelToRes2Map.get(level2);
                }
                else if (mLight) {
                    sim1ResId = signalLevelToRes1Map.get(level1);
                    sim2ResId = signalLevelToRes2Map.get(level2);
                }
                else {
                    sim1ResId = signalDarkLevelToRes1Map.get(level1);
                    sim2ResId = signalDarkLevelToRes2Map.get(level2);
                }
                XposedHelpers.callMethod(mMobile, "setImageResource", sim1ResId);
                XposedHelpers.callMethod(mSmallRoaming, "setImageResource", sim2ResId);
            }
        };
        Helpers.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyDarknessInternal", resetImageDrawable);
        int rightMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_rightmargin", 0);
        if (rightMargin > 0) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "init", new MethodHook() {
                @Override
                protected void after(final MethodHookParam param) throws Throwable {
                    LinearLayout mobileView = (LinearLayout) param.thisObject;
                    Context mContext = mobileView.getContext();
                    Resources res = mContext.getResources();
                    int rightSpacing = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        rightMargin * 0.5f,
                        res.getDisplayMetrics()
                    );
                    mobileView.setPadding(0, 0, rightSpacing, 0);
                }
            });
        }
    }

    public static void AddFiveGTileHook(LoadPackageParam lpparam) {
        final boolean[] isListened = {false};

        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isListened[0]) {
                    isListened[0] = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    int stockTilesResId = mContext.getResources().getIdentifier("miui_quick_settings_tiles_stock", "string", lpparam.packageName);
                    String stockTiles = mContext.getString(stockTilesResId) + ",custom_5G";
                    MainModule.resHooks.setObjectReplacement(lpparam.packageName, "string", "miui_quick_settings_tiles_stock", stockTiles);
                    MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "string", "miui_quick_settings_tiles_stock", stockTiles);
                    MainModule.resHooks.setObjectReplacement("miui.systemui.plugin", "string", "quick_settings_tiles_stock", stockTiles);
                }
            }
        });
        String QSFactoryCls = Helpers.isTPlus() ? "com.android.systemui.qs.tileimpl.MiuiQSFactory" : "com.android.systemui.qs.tileimpl.QSFactoryImpl";
        Class<?> ResourceIconClass = findClass("com.android.systemui.qs.tileimpl.QSTileImpl$ResourceIcon", lpparam.classLoader);
        Helpers.findAndHookMethod(QSFactoryCls, lpparam.classLoader, "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String tileName = (String) param.args[0];
                if (tileName.startsWith("custom_")) {
                    String nfcField = Helpers.isTPlus() ? "nfcTileProvider" : "mNfcTileProvider";
                    Object provider = XposedHelpers.getObjectField(param.thisObject, nfcField);
                    Object tile = XposedHelpers.callMethod(provider, "get");
                    XposedHelpers.setAdditionalInstanceField(tile, "customName", tileName);
                    param.setResult(tile);
                }
            }
        });
        String NfcTileCls = Helpers.isTPlus() ? "com.android.systemui.qs.tiles.MiuiNfcTile" : "com.android.systemui.qs.tiles.NfcTile";
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "isAvailable", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        param.setResult(TelephonyManager.getDefault().isFiveGCapable());
                    }
                    else {
                        param.setResult(false);
                    }
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "getTileLabel", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        Resources modRes = Helpers.getModuleRes(mContext);
                        param.setResult(modRes.getString(R.string.qs_toggle_5g));
                    }
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "handleSetListening", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        boolean mListening = (boolean) param.args[0];
                        if (mListening) {
                            ContentObserver contentObserver = new ContentObserver(new Handler(mContext.getMainLooper())) {
                                @Override
                                public void onChange(boolean z) {
                                    XposedHelpers.callMethod(param.thisObject, "refreshState");
                                }
                            };
                            mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("fiveg_user_enable"), false, contentObserver);
                            mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("dual_nr_enabled"), false, contentObserver);
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "tileListener", contentObserver);
                        }
                        else {
                            ContentObserver contentObserver = (ContentObserver) XposedHelpers.getAdditionalInstanceField(param.thisObject, "tileListener");
                            mContext.getContentResolver().unregisterContentObserver(contentObserver);
                        }
                    }
                    param.setResult(null);
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "getLongClickIntent", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.settings.PreferredNetworkTypeListPreference"));
                        param.setResult(intent);
                    }
                    else {
                        param.setResult(null);
                    }
                }
            }
        });
        Helpers.findAndHookMethod(NfcTileCls, lpparam.classLoader, "handleClick", View.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        TelephonyManager manager = TelephonyManager.getDefault();
                        manager.setUserFiveGEnabled(!manager.isUserFiveGEnabled());
                    }
                    param.setResult(null);
                }
            }
        });

        int fiveGIconResId = MainModule.resHooks.addResource("ic_qs_5g_on", R.drawable.ic_qs_5g_on);
        int fiveGIconOffResId = MainModule.resHooks.addResource("ic_qs_5g_off", R.drawable.ic_qs_5g_off);
        Helpers.hookAllMethods(NfcTileCls, lpparam.classLoader, "handleUpdateState", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object customName = XposedHelpers.getAdditionalInstanceField(param.thisObject, "customName");
                if (customName != null) {
                    String tileName = (String) customName;
                    if ("custom_5G".equals(tileName)) {
                        Object booleanState = param.args[0];
                        TelephonyManager manager = TelephonyManager.getDefault();
                        boolean isEnable = manager.isUserFiveGEnabled();
                        XposedHelpers.setObjectField(booleanState, "value", isEnable);
                        XposedHelpers.setObjectField(booleanState, "state", isEnable ? 2 : 1);
                        String tileLabel = (String) XposedHelpers.callMethod(param.thisObject, "getTileLabel");
                        XposedHelpers.setObjectField(booleanState, "label", tileLabel);
                        XposedHelpers.setObjectField(booleanState, "contentDescription", tileLabel);
                        XposedHelpers.setObjectField(booleanState, "expandedAccessibilityClassName", Switch.class.getName());
                        Object mIcon = XposedHelpers.callStaticMethod(ResourceIconClass, "get", isEnable ? fiveGIconResId : fiveGIconOffResId);
                        XposedHelpers.setObjectField(booleanState, "icon", mIcon);
                    }
                    param.setResult(null);
                }
            }
        });
    }

    private static float scaledTileWidthDim = -1f;
    public static void SystemCCGridHook(LoadPackageParam lpparam) {
        int cols = MainModule.mPrefs.getInt("system_ccgridcolumns", 4);
        int rows = MainModule.mPrefs.getInt("system_ccgridrows", 4);
        if (cols > 4) {
            MainModule.resHooks.setObjectReplacement(lpparam.packageName, "dimen", "qs_control_tiles_columns", cols);
        }
        if (rows > 2) {
            MainModule.resHooks.setObjectReplacement(lpparam.packageName, "dimen", "qs_control_tiles_min_rows", rows);
        }

        Helpers.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!isHooked) {
                    isHooked = true;
                    Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getApplicationContext");
                    Resources res = mContext.getResources();
                    float density = res.getDisplayMetrics().density;
                    int tileWidthResId = res.getIdentifier("qs_control_center_tile_width", "dimen", "com.android.systemui");
                    float tileWidthDim = res.getDimension(tileWidthResId);
                    if (cols > 4) {
                        tileWidthDim = tileWidthDim / density;
                        scaledTileWidthDim = tileWidthDim * 4 / cols;
                        MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "qs_control_center_tile_width", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_control_center_tile_width", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement(lpparam.packageName, "dimen", "qs_control_tile_icon_bg_size", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_control_tile_icon_bg_size", scaledTileWidthDim);
                        MainModule.resHooks.setDensityReplacement("miui.systemui.plugin", "dimen", "qs_cell_height", 85.0f);
                    }
                }
            }
        });

        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
        private boolean isHooked = false;
        @Override
        protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    if (cols > 4) {
                        Helpers.findAndHookConstructor("miui.systemui.controlcenter.qs.QSPager", pluginLoader, Context.class, AttributeSet.class, new MethodHook() {
                            @Override
                            protected void after(MethodHookParam param) throws Throwable {
                                XposedHelpers.setObjectField(param.thisObject, "columns", cols);
                            }
                        });
                        if (!MainModule.mPrefs.getBoolean("system_qsnolabels")) {
                            Helpers.hookAllMethods("miui.systemui.controlcenter.qs.tileview.StandardTileView", pluginLoader, "handleStateChanged", new MethodHook() {
                                @Override
                                protected void after(MethodHookParam param) throws Throwable {
                                    Object label = XposedHelpers.getObjectField(param.thisObject, "label");
                                    if (label != null) {
                                        TextView lb = (TextView) label;
                                        lb.setMaxLines(1);
                                        lb.setSingleLine(true);
                                        lb.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                        lb.setMarqueeRepeatLimit(0);
                                    }
                                }
                            });
                        }
                    }
                    if (rows > 2) {
                        Helpers.findAndHookMethod("miui.systemui.controlcenter.qs.QSPager", pluginLoader, "distributeTiles", new MethodHook() {
                            @Override
                            protected void after(MethodHookParam param) throws Throwable {
                                boolean collapse = (boolean) XposedHelpers.getObjectField(param.thisObject, "collapse");
                                if (collapse) {
                                    ArrayList<Object> pages = (ArrayList<Object>) XposedHelpers.getObjectField(param.thisObject, "pages");
                                    for (Object tileLayoutImpl : pages) {
                                        XposedHelpers.callMethod(tileLayoutImpl, "removeTiles");
                                    }
                                    ArrayList<Object> pageTiles = new ArrayList<Object>();
                                    int currentRow = 2;
                                    ArrayList<?> records = (ArrayList<?>) XposedHelpers.getObjectField(param.thisObject, "records");
                                    Iterator<?> it2 = records.iterator();
                                    int i3 = 0;
                                    int pageNow = 0;
                                    Object bigHeader = XposedHelpers.getObjectField(param.thisObject, "header");
                                    while (it2.hasNext()) {
                                        Object tileRecord = it2.next();
                                        pageTiles.add(tileRecord);
                                        i3++;
                                        if (i3 >= cols) {
                                            currentRow++;
                                            i3 = 0;
                                        }
                                        if (currentRow >= rows || !it2.hasNext()) {
                                            XposedHelpers.callMethod(pages.get(pageNow), "setTiles", pageTiles, pageNow == 0 ? bigHeader : null);
                                            pageTiles.clear();
                                            int totalRows = (int) XposedHelpers.getObjectField(param.thisObject, "rows");
                                            if (currentRow > totalRows) {
                                                XposedHelpers.setObjectField(param.thisObject, "rows", currentRow);
                                            }
                                            if (it2.hasNext()) {
                                                pageNow++;
                                                currentRow = 0;
                                            }
                                        }
                                    }
                                    Iterator<Object> it3 = pages.iterator();
                                    while (it3.hasNext()) {
                                        Object next2 = it3.next();
                                        boolean isEmpty = (boolean) XposedHelpers.callMethod(next2, "isEmpty");
                                        if (isEmpty) {
                                            it3.remove();
                                        }
                                    }
                                    Object pageIndicator = XposedHelpers.getObjectField(param.thisObject, "pageIndicator");
                                    if (pageIndicator != null) {
                                        XposedHelpers.callMethod(pageIndicator, "setNumPages", pages.size());
                                    }
                                    Object adapter = XposedHelpers.getObjectField(param.thisObject, "adapter");
                                    XposedHelpers.callMethod(param.thisObject, "setAdapter", adapter);
//                                    XposedHelpers.callMethod(param.thisObject, "notifyDataSetChanged");
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public static void CCTileCornerHook(LoadPackageParam lpparam) {
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_unavailable", R.drawable.ic_qs_tile_bg_disabled);
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_disabled", R.drawable.ic_qs_tile_bg_disabled);
        MainModule.resHooks.setResReplacement("miui.systemui.plugin", "drawable", "qs_background_warning", R.drawable.ic_qs_tile_bg_warning);

        String pluginLoaderClass = Helpers.isTPlus() ? "com.android.systemui.shared.plugins.PluginInstance$Factory" : "com.android.systemui.shared.plugins.PluginManagerImpl";
        Helpers.hookAllMethods(pluginLoaderClass, lpparam.classLoader, "getClassLoader", new MethodHook() {
            private boolean isHooked = false;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                ApplicationInfo appInfo = (ApplicationInfo) param.args[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) {
                        pluginLoader = (ClassLoader) param.getResult();
                    }
                    Helpers.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.ExpandableIconView", pluginLoader, "setCornerRadius", float.class, new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            Context mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getPluginContext");
                            float radius = 18;
                            if (scaledTileWidthDim > 0) {
                                radius *= scaledTileWidthDim / 65;
                            }
                            param.args[0] = mContext.getResources().getDisplayMetrics().density * radius;
                        }
                    });

                    Helpers.findAndHookMethod("miui.systemui.dagger.PluginComponentFactory", pluginLoader, "create", Context.class, new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            Context mContext = (Context) param.args[0];
                            int enabledTileBackgroundResId = mContext.getResources().getIdentifier("qs_background_enabled", "drawable", "miui.systemui.plugin");
                            int enabledTileColorResId = mContext.getResources().getIdentifier("qs_enabled_color", "color", "miui.systemui.plugin");
                            Helpers.findAndHookMethod("android.content.res.Resources", pluginLoader, "getDrawable", int.class, new MethodHook() {
                                @Override
                                protected void before(MethodHookParam param) throws Throwable {
                                    int resId = (int) param.args[0];
                                    if (resId == enabledTileBackgroundResId && resId != 0) {
                                        Resources modRes = Helpers.getModuleRes(mContext);
                                        Drawable enableTile = modRes.getDrawable(R.drawable.ic_qs_tile_bg_enabled, null);
                                        enableTile.setTint(mContext.getResources().getColor(enabledTileColorResId, null));
                                        param.setResult(enableTile);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}