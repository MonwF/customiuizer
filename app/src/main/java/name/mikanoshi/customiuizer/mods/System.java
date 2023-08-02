package name.mikanoshi.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.findMethodExactIfExists;
import static name.mikanoshi.customiuizer.mods.GlobalActions.ACTION_PREFIX;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AndroidAppHelper;
import android.app.KeyguardManager;
import android.app.MiuiNotification;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetProviderInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BadParcelableException;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
import android.util.MiuiMultiWindowUtils;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Magnifier;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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
import miui.app.MiuiFreeFormManager;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.AudioVisualizer;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;
import name.mikanoshi.customiuizer.utils.Helpers.MimeType;
import name.mikanoshi.customiuizer.utils.SoundData;

public class System {
    private final static String StatusBarCls = Helpers.isTPlus() ? "com.android.systemui.statusbar.phone.CentralSurfacesImpl" : "com.android.systemui.statusbar.phone.StatusBar";

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

    public static void NoAccessDeviceLogsRequest(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.logcat.LogcatManagerService", lpparam.classLoader, "onLogAccessRequested", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                XposedHelpers.callMethod(param.thisObject, "declineRequest", param.args[0]);
                param.setResult(null);
            }
        });
    }
    public static void NoLightUpOnChargeHook(LoadPackageParam lpparam) {
        String methodName = Helpers.isTPlus() ? "wakePowerGroupLocked" : "wakeDisplayGroupNoUpdateLocked";
        Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, methodName, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String reason = (String)param.args[3];
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
            }
        });
    }

    public static void NoLightUpOnHeadsetHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeDisplayGroupNoUpdateLocked", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String reason = (String)param.args[3];
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
                for (int row = 1; row <= 3; row++)
                    for (int col = 0; col <= 2; col++)
                        if (mViews[row][col] != null)
                            mRandomViews.add(mViews[row][col]);
                mRandomViews.add(mViews[4][1]);
                Collections.shuffle(mRandomViews);

                View pinview = (View)param.thisObject;
                ViewGroup row1 = pinview.findViewById(pinview.getResources().getIdentifier("row1", "id", "com.android.systemui"));
                ViewGroup row2 = pinview.findViewById(pinview.getResources().getIdentifier("row2", "id", "com.android.systemui"));
                ViewGroup row3 = pinview.findViewById(pinview.getResources().getIdentifier("row3", "id", "com.android.systemui"));
                ViewGroup row4 = pinview.findViewById(pinview.getResources().getIdentifier("row4", "id", "com.android.systemui"));

                row1.removeAllViews();
                row2.removeAllViews();
                row3.removeAllViews();
                row4.removeViewAt(1);

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

                mViews[4] = new View[]{ null, mRandomViews.get(9), mViews[4][2]};
                row4.addView(mRandomViews.get(9), 1);

                XposedHelpers.setObjectField(param.thisObject, "mViews", mViews);
            }
        });
    }

    public static void NoPasswordHook() {
        String isAllowed = "isBiometricAllowedForUser";
        Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, isAllowed, boolean.class, int.class, XC_MethodReplacement.returnConstant(true));
        Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils", null, isAllowed, int.class, XC_MethodReplacement.returnConstant(true));
    }

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

        Helpers.hookAllConstructors("com.android.keyguard.KeyguardSecurityContainerController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.callMethod(param.thisObject, "getContext");
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            Object mCallback = XposedHelpers.getObjectField(param.thisObject, "mKeyguardSecurityCallback");
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
                    if (param.args.length == 0) return;
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
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                if (mContext != null) {
                    mContext.sendBroadcast(new Intent(ACTION_PREFIX + "UnlockBTConnection"));
                }
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

                        long lastTouchTime = (long)XposedHelpers.getAdditionalInstanceField(view, "currentTouchTime");
                        float lastTouchX = (float)XposedHelpers.getAdditionalInstanceField(view, "currentTouchX");
                        float lastTouchY = (float)XposedHelpers.getAdditionalInstanceField(view, "currentTouchY");

                        long currentTouchTime = java.lang.System.currentTimeMillis();
                        float currentTouchX = event.getX();
                        float currentTouchY = event.getY();

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

    private static int callsResId;
    public static void NotificationVolumeSettingsRes() {
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
                        int device = deviceType;
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
                initSeekBar[0].invoke(fragment, "system_volume", 1, context.getResources().getIdentifier("ic_audio_vol", "drawable", context.getPackageName()));
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

    private static void initClockStyle(TextView mClock) {
        Resources res = mClock.getResources();
        String clockName = (String) XposedHelpers.getAdditionalInstanceField(mClock, "clockName");
        String subKey = "statusbar";
        boolean statusBarClock = clockName.equals("clock");
        if (!statusBarClock) {
            subKey = "cc";
        }
        boolean enableCustomFormat = !statusBarClock || MainModule.mPrefs.getBoolean("system_" + subKey + "_clock_customformat_enable");
        String customFormat = MainModule.mPrefs.getString("system_" + subKey + "_clock_customformat", "");
        boolean dualRows = enableCustomFormat && customFormat.contains("\n");
        if (statusBarClock) {
            float dimStep = 0.5f;
            int fontSize = MainModule.mPrefs.getInt("system_statusbar_clock_fontsize", 13);
            if (fontSize > 13) {
                mClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * dimStep);
            }
            if (dualRows) {
                mClock.setLineSpacing(0, 0.5 * fontSize > 8.5f ? 0.85f : 0.9f);
            }
            int align = MainModule.mPrefs.getStringAsInt("system_" + subKey + "_clock_align", 1);
            if (align == 2) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
            else if (align == 3) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            else if (align == 4) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            }
            if (MainModule.mPrefs.getBoolean("system_" + subKey + "_clock_bold")) {
                mClock.setTypeface(Typeface.DEFAULT_BOLD);
            }
            int leftMargin = MainModule.mPrefs.getInt("system_statusbar_clock_leftmargin", 0);
            leftMargin = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                leftMargin * dimStep,
                res.getDisplayMetrics()
            );
            int rightMargin = MainModule.mPrefs.getInt("system_statusbar_clock_rightmargin", 0);
            rightMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                rightMargin * dimStep,
                res.getDisplayMetrics()
            );
            int defaultVerticalOffset = 8;
            int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_clock_verticaloffset", defaultVerticalOffset);
            if (verticalOffset != defaultVerticalOffset) {
                float marginTop = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    (verticalOffset - defaultVerticalOffset) * dimStep,
                    res.getDisplayMetrics()
                );
                mClock.setTranslationY(marginTop);
            }

            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_chip")) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mClock.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                if (leftMargin > 0) {
                    lp.leftMargin = leftMargin;
                }
                if (rightMargin > 0) {
                    lp.rightMargin = rightMargin;
                }
                mClock.setLayoutParams(lp);

                boolean useMonet = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_usemonet");
                boolean enableCustomText = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_customtextcolor");
                if (useMonet || enableCustomText) {
                    XposedHelpers.setObjectField(mClock, "mUseWallpaperTextColor", true);
                }

                int startColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_startcolor", 0x8F7C4DFF);
                int endColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_endcolor", 0x2FA7FFEB);
                if (useMonet) {
                    mClock.setTextColor(mClock.getResources().getColor(android.R.color.system_accent1_0, null));
                    startColor = mClock.getResources().getColor(android.R.color.system_accent1_600, null);
                    endColor = startColor;
                }
                else if (enableCustomText) {
                    int textcolor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_textcolor", 0xFFFFFFFF);
                    mClock.setTextColor(textcolor);
                }
                GradientDrawable chipDrawable = new GradientDrawable();
                boolean verticalOrientation = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_orientation_vertical");
                chipDrawable.setOrientation(verticalOrientation ? GradientDrawable.Orientation.TOP_BOTTOM : GradientDrawable.Orientation.LEFT_RIGHT);
                chipDrawable.setColors(new int[]{startColor, endColor});
                chipDrawable.setShape(GradientDrawable.RECTANGLE);
                int horizPadding = MainModule.mPrefs.getInt("system_statusbar_clock_chip_horizpadding", 0);
                int vertPadding = MainModule.mPrefs.getInt("system_statusbar_clock_chip_verticalpadding", 0);
                if (horizPadding > 0) {
                    horizPadding = (int)TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        horizPadding,
                        res.getDisplayMetrics()
                    );
                }
                if (vertPadding > 0 || horizPadding > 0) {
                    chipDrawable.setPadding(horizPadding, vertPadding, horizPadding, vertPadding);
                }
                int radiusPx = MainModule.mPrefs.getInt("system_statusbar_clock_chip_radius", 0);
                if (radiusPx > 0) {
                    radiusPx = (int)TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        radiusPx,
                        res.getDisplayMetrics()
                    );
                    chipDrawable.setCornerRadius(radiusPx);
                }
                mClock.setBackground(chipDrawable);
            }
            else {
                if (leftMargin > 0 || rightMargin > 0) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mClock.getLayoutParams();
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    lp.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    if (leftMargin > 0) {
                        lp.leftMargin = leftMargin;
                    }
                    if (rightMargin > 0) {
                        lp.rightMargin = rightMargin;
                    }
                    mClock.setLayoutParams(lp);
                }
            }
        }
        if (dualRows) {
            mClock.setSingleLine(false);
            mClock.setMaxLines(2);
        }

        int fixedWidth = MainModule.mPrefs.getInt("system_" + subKey + "_clock_fixedcontent_width", 10);
        if (fixedWidth > 10) {
            ViewGroup.LayoutParams lp = mClock.getLayoutParams();
            lp.width = (int)(mClock.getResources().getDisplayMetrics().density * fixedWidth);
            mClock.setLayoutParams(lp);
        }
    }

    private static boolean getShowSeconds() {
        boolean sbShowSeconds = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_seconds");
        String customFormat = MainModule.mPrefs.getString("system_statusbar_clock_customformat", "");
        boolean enableCustomFormat = MainModule.mPrefs.getBoolean("system_statusbar_clock_customformat_enable");
        return (enableCustomFormat && customFormat.contains("ss")) || (!enableCustomFormat && sbShowSeconds);
    }

    private static boolean getCCShowSeconds() {
        String customFormat = MainModule.mPrefs.getString("system_cc_clock_customformat", "");
        return customFormat.contains("ss");
    }

    private static void initSecondTimer(Object clockController) {
        boolean ccShowSeconds = getCCShowSeconds();
        boolean finalSbShowSeconds = getShowSeconds();
        Context mContext = (Context) XposedHelpers.getObjectField(clockController, "mContext");
        Timer scheduleTimer = (Timer) XposedHelpers.getAdditionalInstanceField(clockController, "scheduleTimer");
        if (scheduleTimer != null) {
            scheduleTimer.cancel();
        }
        if (ccShowSeconds || finalSbShowSeconds) {
            final Handler mClockHandler = new Handler(mContext.getMainLooper());
            long delay = 1000 - java.lang.System.currentTimeMillis() % 1000;
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mClockHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Object mCalendar = XposedHelpers.getObjectField(clockController, "mCalendar");
                            XposedHelpers.callMethod(mCalendar, "setTimeInMillis", java.lang.System.currentTimeMillis());
                            XposedHelpers.setObjectField(clockController, "mIs24", DateFormat.is24HourFormat(mContext));
                            ArrayList<Object> mClockListeners = (ArrayList<Object>) XposedHelpers.getObjectField(clockController, "mClockListeners");
                            Iterator<Object> it = mClockListeners.iterator();
                            while (it.hasNext()) {
                                Object clock = it.next();
                                Object showSeconds = XposedHelpers.getAdditionalInstanceField(clock, "showSeconds");
                                if (showSeconds != null) {
                                    XposedHelpers.callMethod(clock, "onTimeChange");
                                }
                            }
                        }
                    });
                }
            }, delay, 1000);
            XposedHelpers.setAdditionalInstanceField(clockController, "scheduleTimer", timer);
        }
    }
    public static void StatusBarClockTweakHook(LoadPackageParam lpparam) {
        boolean statusbarClockTweak = MainModule.mPrefs.getBoolean("system_statusbar_clocktweak");
        boolean ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak");
        MethodHook ScheduleHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                initSecondTimer(param.thisObject);
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                IntentFilter timeSetIntent = new IntentFilter();
                timeSetIntent.addAction("android.intent.action.TIME_SET");
                BroadcastReceiver mUpdateTimeReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        initSecondTimer(param.thisObject);
                    }
                };
                mContext.registerReceiver(mUpdateTimeReceiver, timeSetIntent);
            }
        };
        if (ccClockTweak || statusbarClockTweak) {
            Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.classLoader, ScheduleHook);
            Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.classLoader, "fireTimeChange", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    Object clockController = param.thisObject;
                    ArrayList<Object> mClockListeners = (ArrayList<Object>) XposedHelpers.getObjectField(clockController, "mClockListeners");
                    Iterator<Object> it = mClockListeners.iterator();
                    while (it.hasNext()) {
                        Object clock = it.next();
                        Object showSeconds = XposedHelpers.getAdditionalInstanceField(clock, "showSeconds");
                        if (showSeconds == null) {
                            XposedHelpers.callMethod(clock, "onTimeChange");
                        }
                    }
                    param.setResult(null);
                }
            });
        }
        String ccDateFormat = MainModule.mPrefs.getString("system_cc_dateformat", "");
        boolean ccDateCustom = ccDateFormat.length() > 0;
        boolean hideDateView = MainModule.mPrefs.getBoolean("system_cc_hidedate");
        Helpers.hookAllConstructors("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) {
                final TextView clock = (TextView)param.thisObject;
                if (param.args.length != 3) return;
                int clockId = clock.getResources().getIdentifier("clock", "id", "com.android.systemui");
                int bigClockId = clock.getResources().getIdentifier("big_time", "id", "com.android.systemui");
                int dateClockId = clock.getResources().getIdentifier("date_time", "id", "com.android.systemui");
                int thisClockId = clock.getId();
                if (clockId == thisClockId && statusbarClockTweak) {
                    XposedHelpers.setAdditionalInstanceField(clock, "clockName", "clock");
                    if (getShowSeconds()) {
                        XposedHelpers.setAdditionalInstanceField(clock, "showSeconds", true);
                    }
                }
                else if (bigClockId == thisClockId && ccClockTweak) {
                    XposedHelpers.setAdditionalInstanceField(clock, "clockName", "ccClock");
                    if (getCCShowSeconds()) {
                        XposedHelpers.setAdditionalInstanceField(clock, "showSeconds", true);
                    }
                    initClockStyle(clock);
                }
                else if (dateClockId == thisClockId && (ccDateCustom || hideDateView)) {
                    XposedHelpers.setAdditionalInstanceField(clock, "clockName", "ccDate");
                }
            }
        });
        Helpers.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, "updateTime", new MethodHook(XCallback.PRIORITY_HIGHEST) {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                TextView clock = (TextView)param.thisObject;
                String clockName = (String) XposedHelpers.getAdditionalInstanceField(clock, "clockName");
                Context mContext = clock.getContext();
                Object mMiuiStatusBarClockController = XposedHelpers.getObjectField(clock, "mMiuiStatusBarClockController");
                Object mCalendar = XposedHelpers.callMethod(mMiuiStatusBarClockController, "getCalendar");
                String timeFmt = null;
                if ("ccClock".equals(clockName) && ccClockTweak) {
                    String customFormat = MainModule.mPrefs.getString("system_cc_clock_customformat", "");
                    if (customFormat.length() > 0) {
                        timeFmt = customFormat;
                    }
                }
                else if ("ccDate".equals(clockName) && (!hideDateView && ccDateCustom)) {
                    timeFmt = ccDateFormat;
                }
                else if ("clock".equals(clockName) && statusbarClockTweak) {
                    String customFormat = MainModule.mPrefs.getString("system_statusbar_clock_customformat", "");
                    boolean enableCustomFormat = MainModule.mPrefs.getBoolean("system_statusbar_clock_customformat_enable");
                    enableCustomFormat = enableCustomFormat && (customFormat.length() > 0);
                    if (enableCustomFormat) {
                        timeFmt = customFormat;
                    }
                    else {
                        boolean showSeconds = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_seconds");
                        boolean is24 = MainModule.mPrefs.getBoolean("system_statusbar_clock_24hour_format");
                        boolean showAmpm = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_ampm");
                        boolean hourIn2d = MainModule.mPrefs.getBoolean("system_statusbar_clock_leadingzero");
                        boolean addSpaceInHour = MainModule.mPrefs.getBoolean("system_statusbar_clock_leadingspace");
                        String fmt;
                        if (showAmpm) {
                            fmt = "fmt_time_12hour_minute_pm";
                        }
                        else {
                            fmt = "fmt_time_12hour_minute";
                        }
                        int fmtResId = mContext.getResources().getIdentifier(fmt, "string", "com.android.systemui");
                        timeFmt = mContext.getString(fmtResId);
                        if (showSeconds) {
                            timeFmt = timeFmt.replaceFirst(":mm", ":mm:ss");
                        }
                        String hourStr = "h";
                        if (is24) {
                            hourStr = "H";
                        }
                        if (hourIn2d) {
                            hourStr = hourStr + hourStr;
                        }
                        else if (addSpaceInHour) {
                            int h = (int) XposedHelpers.callMethod(mCalendar, "get", 18);
                            if (h < 10 || (!is24 && (h > 12 && h < 22))) {
                                hourStr = " " + hourStr;
                            }
                        }
                        timeFmt = timeFmt.replaceFirst("h+:", hourStr + ":");
                    }
                }
                if (timeFmt != null) {
                    StringBuilder formatSb = new StringBuilder(timeFmt);
                    StringBuilder textSb = new StringBuilder();
                    XposedHelpers.callMethod(mCalendar, "format", mContext, textSb, formatSb);
                    clock.setText(textSb.toString());
                    param.setResult(null);
                }
            }
        });
        if (hideDateView) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, "setClockVisibility", int.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    TextView clock = (TextView)param.thisObject;
                    String clockName = (String) XposedHelpers.getAdditionalInstanceField(param.thisObject, "clockName");
                    if ("ccDate".equals(clockName)) {
                        XposedHelpers.setObjectField(param.thisObject, "mVisibility", 8);
                        clock.setVisibility(View.GONE);
                        param.setResult(null);
                    }
                }
            });
        }
        if (statusbarClockTweak) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onAttachedToWindow", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    TextView clock = (TextView) XposedHelpers.getObjectField(param.thisObject, "mMiuiClock");
                    initClockStyle(clock);
                }
            });
        }
        if (ccClockTweak) {
            int ccClockFontSize = MainModule.mPrefs.getInt("system_cc_clock_fontsize", 9);
            int clockMarginTop = MainModule.mPrefs.getInt("system_cc_clock_topmargin_indrawer", 0);
            int defaultVerticalOffset = 10;
            int verticalOffset = MainModule.mPrefs.getInt("system_cc_clock_verticaloffset", defaultVerticalOffset);
            if (ccClockFontSize > 9 || clockMarginTop > 0 || verticalOffset != defaultVerticalOffset) {
                MethodHook setSizeHook = new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) throws Throwable {
                        TextView clock = (TextView)XposedHelpers.getObjectField(param.thisObject, "mBigTime");
                        if (ccClockFontSize > 9) {
                            clock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ccClockFontSize);
                        }
                        String clsName = param.thisObject.getClass().getSimpleName();
                        if (clockMarginTop > 0 && "MiuiNotificationHeaderView".equals(clsName)) {
                            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) clock.getLayoutParams();
                            float marginTop = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                clockMarginTop,
                                clock.getResources().getDisplayMetrics()
                            );
                            lp.topMargin = (int)marginTop;
                            clock.setLayoutParams(lp);
                        }
                        if (verticalOffset != defaultVerticalOffset) {
                            float marginTop = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                verticalOffset - defaultVerticalOffset,
                                clock.getResources().getDisplayMetrics()
                            );
                            clock.setTranslationY(marginTop);
                        }
                    }
                };
                Helpers.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.classLoader, "updateResources", setSizeHook);
                Helpers.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.classLoader, "updateResources", setSizeHook);
            }
        }
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

    public static void DrawerBlurRatioHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                View mView = (View) param.args[0];
                Context mContext = mView.getContext();
                Handler mHandler = new Handler(mContext.getMainLooper());

                Object mControlPanelWindowManager = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.classLoader), "get", findClass("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.classLoader));
                Object notificationShadeDepthController = XposedHelpers.getObjectField(param.thisObject, "notificationShadeDepthController");
                int initBlurRatio = MainModule.mPrefs.getInt("system_drawer_blur", 100);
                XposedHelpers.setAdditionalInstanceField(notificationShadeDepthController, "mCustomBlurModifier", initBlurRatio);
                XposedHelpers.setAdditionalInstanceField(mControlPanelWindowManager, "mCustomBlurModifier", initBlurRatio);
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_drawer_blur", 100) {
                    @Override
                    public void onChange(String name, int defValue) {
                        int opt = Helpers.getSharedIntPref(mContext, name, defValue);
                        XposedHelpers.setAdditionalInstanceField(notificationShadeDepthController, "mCustomBlurModifier", opt);
                        XposedHelpers.setAdditionalInstanceField(mControlPanelWindowManager, "mCustomBlurModifier", opt);
                    }
                };
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationShadeDepthController$updateBlurCallback$1", lpparam.classLoader, "doFrame", long.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object parentCtrl = XposedHelpers.getSurroundingThis(param.thisObject);
                Object blurRatio = XposedHelpers.getAdditionalInstanceField(parentCtrl, "mCustomBlurModifier");
                Object mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt");
                XposedHelpers.setAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier", blurRatio);
            }
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object parentCtrl = XposedHelpers.getSurroundingThis(param.thisObject);
                Object mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt");
                XposedHelpers.removeAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier");
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BlurUtilsExt", lpparam.classLoader, "applyBlurByRadius", View.class, int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object multiplier = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier");
                if (multiplier != null) {
                    Object blurUtils = XposedHelpers.getObjectField(param.thisObject, "blurUtils");
                    float ratio = (float) XposedHelpers.callMethod(blurUtils, "ratioOfBlurRadius", 1.0f * (int)param.args[1]);
                    float newRatio = ratio * (int)multiplier / 100f;
                    param.args[1] = Math.round((float)XposedHelpers.callMethod(blurUtils, "blurRadiusOfRatio", newRatio));
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.classLoader, "setBlurRatio", float.class, boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
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
        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.policy.AppMiniWindowManager", lpparam.classLoader, "canNotificationSlide", String.class, PendingIntent.class, XC_MethodReplacement.returnConstant(false));
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

    public static void ColorizeNotificationCardHook(LoadPackageParam lpparam) {
        Class<?> ColorScheme = findClassIfExists("com.android.systemui.monet.ColorScheme", lpparam.classLoader);
        Object contentStyle = null;
        if (Helpers.isTPlus()) {
            Class<?> MonetStyle = findClassIfExists("com.android.systemui.monet.Style", lpparam.classLoader);
            Object[] styles = MonetStyle.getEnumConstants();
            for (Object o:styles) {
//                if (o.toString().contains("VIBRANT")) {
//                if (o.toString().contains("TONAL_SPOT")) {
                if (o.toString().contains("CONTENT")) {
                    contentStyle = o;
                    break;
                }
            }
        }
        Object finalContentStyle = contentStyle;

        Helpers.findAndHookConstructor("android.app.Notification$Builder", lpparam.classLoader, Context.class, Notification.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (param.args[1] != null) {
                    Notification mN = (Notification) param.args[1];
                    if (XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") != null) {
                        Object builder = param.thisObject;
                        Object mParams = XposedHelpers.getObjectField(builder, "mParams");
                        XposedHelpers.callMethod(builder, "getColors", mParams);
                        Object mColors = XposedHelpers.getObjectField(builder, "mColors");
                        XposedHelpers.setObjectField(mColors, "mProtectionColor", XposedHelpers.getAdditionalInstanceField(mN, "mProtectionColor"));
                        XposedHelpers.setObjectField(mColors, "mPrimaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
                        XposedHelpers.setObjectField(mColors, "mSecondaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
                    }
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader, "updateNotificationColor", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object mEntry = XposedHelpers.getObjectField(param.thisObject, "mEntry");
                Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
                Notification notify = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
                Object overflowColor = XposedHelpers.getAdditionalInstanceField(notify, "mSecondaryTextColor");
                if (overflowColor != null) {
                    XposedHelpers.setObjectField(param.thisObject, "mNotificationColor", overflowColor);
                    param.setResult(null);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader, "onNotificationUpdated", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mEntry = XposedHelpers.getObjectField(param.thisObject, "mEntry");
                if (mEntry != null) {
                    Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
                    Notification notify = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
                    Object mNotifyBackgroundColor = XposedHelpers.getAdditionalInstanceField(notify, "mNotifyBackgroundColor");
                    if (mNotifyBackgroundColor != null) {
                        int bgColor = (int) mNotifyBackgroundColor;
                        int mCurrentBackgroundTint = XposedHelpers.getIntField(param.thisObject, "mCurrentBackgroundTint");
                        if (mCurrentBackgroundTint != bgColor) {
                            bgColor = Color.argb(158, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor));
                            XposedHelpers.callMethod(param.thisObject, "setBackgroundTintColor", bgColor);
                        }
                    }
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationBackgroundView", lpparam.classLoader, "setTint", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if ((int)param.args[0] == 0) {
                    param.setResult(null);
                }
            }
        });

        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper", lpparam.classLoader, "getCustomBackgroundColor", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                param.setResult(XposedHelpers.getObjectField(param.thisObject, "mBackgroundColor"));
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.HybridGroupManager", lpparam.classLoader, "bindFromNotificationWithStyle", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Notification mN = (Notification) XposedHelpers.callMethod(param.args[2], "getNotification");
                if (XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor") != null) {
                    LinearLayout hybridNotificationView = (LinearLayout) param.getResult();
                    TextView mTitleView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTitleView");
                    TextView mTextView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTextView");
                    mTitleView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
                    mTextView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
                }
            }
        });

        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.classLoader, "handle3thThemeColor", new MethodHook() {
            private Object sAppIconManager = null;
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Notification.Builder builder = (Notification.Builder) param.args[1];
                Notification mN = (Notification) XposedHelpers.getObjectField(builder, "mN");
                if ((boolean)XposedHelpers.callMethod(mN, "isColorized")) return;
                if ((boolean)XposedHelpers.callMethod(mN, "isMediaNotification")) return;
                ApplicationInfo applicationInfo = mN.extras.getParcelable("android.appInfo");
                if (applicationInfo == null) {
                    return;
                }
                Context mContext = (Context) param.args[0];
                String pkgName = applicationInfo.packageName;
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_colorizenotifs", "1"));
                boolean isSelected = MainModule.mPrefs.getStringSet("system_colorizenotifs_apps").contains(pkgName);
                if (opt == 2 && !isSelected || opt == 3 && isSelected) {
                    XposedHelpers.callMethod(builder, "makeNotificationGroupHeader");
                    if (sAppIconManager == null) {
                        Class<?> Dependency = findClassIfExists("com.android.systemui.Dependency", lpparam.classLoader);
                        Class<?> AppIconManager = findClassIfExists("com.miui.systemui.graphics.AppIconsManager", lpparam.classLoader);
                        sAppIconManager = XposedHelpers.callStaticMethod(Dependency, "get", AppIconManager);
                    }
                    Bitmap notifyIcon = (Bitmap) XposedHelpers.callMethod(sAppIconManager, "getAppIconBitmap", pkgName);
                    WallpaperColors wc = WallpaperColors.fromBitmap(notifyIcon);
                    int primaryColor = wc.getPrimaryColor().toArgb();
                    float lux = Color.luminance(primaryColor);
                    if (lux > 0.9) {
                        Color secColor = wc.getSecondaryColor();
                        if (secColor != null) {
                            primaryColor = secColor.toArgb();
                        }
                    }
                    Object cs;
                    boolean dark = mContext.getResources().getConfiguration().isNightModeActive();
                    if (Helpers.isTPlus()) {
                        cs = XposedHelpers.newInstance(ColorScheme, primaryColor, dark, finalContentStyle);
                    }
                    else {
                        cs = XposedHelpers.newInstance(ColorScheme, primaryColor, dark);
                    }
                    List<Integer> accent1 = (List<Integer>) XposedHelpers.callMethod(cs, "getAccent1");
//                    List<Integer> accent2 = (List<Integer>) XposedHelpers.callMethod(cs, "getAccent2");
                    List<Integer> n1 = (List<Integer>) XposedHelpers.getObjectField(cs, "neutral1");
                    List<Integer> n2 = (List<Integer>) XposedHelpers.getObjectField(cs, "neutral2");

                    int bgColor = accent1.get(dark ? 5 : 6);
                    Object mParams = XposedHelpers.getObjectField(builder, "mParams");
                    XposedHelpers.callMethod(mParams, "reset");
                    XposedHelpers.callMethod(builder, "getColors", mParams);
                    Object mColors = XposedHelpers.getObjectField(builder, "mColors");
                    int mProtectionColor = ColorUtils.blendARGB(n1.get(1), bgColor, 0.7f);
                    int mPrimaryTextColor = n1.get(dark ? 1 : 10);
                    int mSecondaryTextColor = n2.get(dark ? 3 : 8);
                    XposedHelpers.setObjectField(mColors, "mProtectionColor", mProtectionColor);
                    XposedHelpers.setAdditionalInstanceField(mN, "mProtectionColor", mProtectionColor);
                    XposedHelpers.setObjectField(mColors, "mPrimaryTextColor", mPrimaryTextColor);
                    XposedHelpers.setAdditionalInstanceField(mN, "mPrimaryTextColor", mPrimaryTextColor);
                    XposedHelpers.setObjectField(mColors, "mSecondaryTextColor", mSecondaryTextColor);
                    XposedHelpers.setAdditionalInstanceField(mN, "mSecondaryTextColor", mSecondaryTextColor);
                    XposedHelpers.setAdditionalInstanceField(mN, "mNotifyBackgroundColor", bgColor);
                    param.setResult(null);
                }
            }
        });

        MethodHook textColorHook = new MethodHook() {
            private int titleResId = 0;
            private int subTextResId = 0;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                RemoteViews baseContent = (RemoteViews) param.getResult();
                if (baseContent != null) {
                    Context mContext = (Context) param.args[param.args.length - 1];
                    if (titleResId == 0) {
                        titleResId = mContext.getResources().getIdentifier("title", "id", "com.android.systemui");
                        subTextResId = mContext.getResources().getIdentifier("text", "id", "com.android.systemui");
                    }
                    Notification.Builder builder = (Notification.Builder) param.args[0];
                    Notification mN = (Notification) XposedHelpers.getObjectField(builder, "mN");
                    if ((boolean)XposedHelpers.callMethod(mN, "isMediaNotification")) return;
                    if (XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") != null) {
                        baseContent.setTextColor(titleResId, (int)XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
                        baseContent.setTextColor(subTextResId, (int)XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
                    }
                }
            }
        };

        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.classLoader, "createMiuiContentView", textColorHook);
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.classLoader, "createMiuiExpandedView", textColorHook);
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.classLoader, "createMiuiPublicView", textColorHook);
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

        hookedFlaresInfo = Helpers.findAndHookMethodSilently("com.android.systemui.qs.MiuiQSHeaderView", lpparam.classLoader, "updateCarrierVisibility", hideOperatorHook);
        if (!hookedFlaresInfo) {
            Helpers.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.classLoader, "onFinishInflate", hideOperatorHook);
        }
    }

    public static void HideCCOperatorDelimiterHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiCarrierTextController", lpparam.classLoader, "fireCarrierTextChanged", String.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String mCurrentCarrier = (String) param.args[0];
                param.args[0] = mCurrentCarrier.replace(" | ", "");
            }
        });
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

    public static void NotificationRowMenuHook(LoadPackageParam lpparam) {
        int appInfoIconResId = MainModule.resHooks.addResource("ic_appinfo", R.drawable.ic_appinfo12);
        int forceCloseIconResId = MainModule.resHooks.addResource("ic_forceclose", R.drawable.ic_forceclose12);
        int openInFwIconResId = MainModule.resHooks.addResource("ic_openinfw", R.drawable.ic_openinfw);
        int appInfoDescId = MainModule.resHooks.addResource("miui_notification_menu_appinfo_title", R.string.system_notifrowmenu_appinfo);
        int forceCloseDescId = MainModule.resHooks.addResource("miui_notification_menu_forceclose_title", R.string.system_notifrowmenu_forceclose);
        int openInFwDescId = MainModule.resHooks.addResource("miui_notification_menu_openinfw_title", R.string.system_notifrowmenu_openinfw);
        MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_menu_icon_padding", 0);
        MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "miui_notification_modal_menu_margin_left_right", 3);
        MainModule.resHooks.setResReplacement("com.android.systemui", "drawable", "miui_notification_menu_ic_bg_active", R.drawable.miui_notification_menu_ic_bg_active);
        MainModule.resHooks.setResReplacement("com.android.systemui", "drawable", "miui_notification_menu_ic_bg_inactive", R.drawable.miui_notification_menu_ic_bg_inactive);

        Class<?> MiuiNotificationMenuItem = findClass("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow.MiuiNotificationMenuItem", lpparam.classLoader);
        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.classLoader, "createMenuViews", boolean.class, boolean.class, new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(final MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                ArrayList<Object> mMenuItems = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mMenuItems");

                Object infoBtn = null;
                Object forceCloseBtn = null;
                Object openFwBtn = null;
                Constructor MenuItem = MiuiNotificationMenuItem.getConstructors()[0];
                try {
                    infoBtn = MenuItem.newInstance(param.thisObject, mContext, appInfoDescId, null, appInfoIconResId);
                    forceCloseBtn = MenuItem.newInstance(param.thisObject, mContext, forceCloseDescId, null, forceCloseIconResId);
                    openFwBtn = MenuItem.newInstance(param.thisObject, mContext, openInFwDescId, null, openInFwIconResId);
                } catch (Throwable t1) {
                    Helpers.log(t1);
                }
                if (infoBtn == null || forceCloseBtn == null || openFwBtn == null) return;
                Object notification = XposedHelpers.getObjectField(param.thisObject, "mSbn");
                Object expandNotifyRow = XposedHelpers.getObjectField(param.thisObject, "mParent");
                mMenuItems.add(infoBtn);
                mMenuItems.add(forceCloseBtn);
                mMenuItems.add(openFwBtn);
                XposedHelpers.setObjectField(param.thisObject, "mMenuItems", mMenuItems);
                int menuMargin = (int) XposedHelpers.getObjectField(param.thisObject, "mMenuMargin");
                LinearLayout mMenuContainer = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
                View mInfoBtn = (View) XposedHelpers.callMethod(infoBtn, "getMenuView");
                View mForceCloseBtn = (View) XposedHelpers.callMethod(forceCloseBtn, "getMenuView");
                View mOpenFwBtn = (View) XposedHelpers.callMethod(openFwBtn, "getMenuView");

                View.OnClickListener itemClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (view == null) return;
                        String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                        int uid = (int)XposedHelpers.callMethod(notification, "getAppUid");
                        int user = 0;
                        try {
                            user = (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", uid);
                        } catch (Throwable t) {
                            Helpers.log(t);
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
                        else if (view == mOpenFwBtn) {
                            Class<?> Dependency = findClass("com.android.systemui.Dependency", lpparam.classLoader);
                            Object AppMiniWindowManager = XposedHelpers.callStaticMethod(Dependency, "get", findClassIfExists("com.android.systemui.statusbar.notification.policy.AppMiniWindowManager", lpparam.classLoader));
                            String miniWindowPkg = (String) XposedHelpers.callMethod(expandNotifyRow, "getMiniWindowTargetPkg");
                            PendingIntent notifyIntent = (PendingIntent) XposedHelpers.callMethod(expandNotifyRow, "getPendingIntent");
                            String ModalControllerForDep = "com.android.systemui.statusbar.notification.modal.ModalController";
                            Object ModalController = XposedHelpers.callStaticMethod(Dependency, "get", findClass(ModalControllerForDep, lpparam.classLoader));
                            XposedHelpers.callMethod(ModalController, "animExitModelCollapsePanels");
                            XposedHelpers.callMethod(AppMiniWindowManager, "launchMiniWindowActivity", miniWindowPkg, notifyIntent);
                        }
                    }
                };
                mInfoBtn.setOnClickListener(itemClick);
                mForceCloseBtn.setOnClickListener(itemClick);
                mOpenFwBtn.setOnClickListener(itemClick);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
                layoutParams.leftMargin = menuMargin;
                layoutParams.rightMargin = menuMargin;
                mMenuContainer.addView(mInfoBtn, layoutParams);
                mMenuContainer.addView(mForceCloseBtn, layoutParams);
                mMenuContainer.addView(mOpenFwBtn, layoutParams);
                int menuWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    52,
                    mContext.getResources().getDisplayMetrics()
                );
                int titleId = mContext.getResources().getIdentifier("modal_menu_title", "id", lpparam.packageName);
                mMenuItems.forEach(new Consumer() {
                    @Override
                    public void accept(Object obj) {
                        View menuView = (View) XposedHelpers.callMethod(obj, "getMenuView");
                        ((TextView) menuView.findViewById(titleId)).setMaxWidth(menuWidth);
                    }
                });
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
        Helpers.findAndHookMethod("com.android.server.vibrator.VibratorManagerService", lpparam.classLoader, "systemReady", new MethodHook() {
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

        Helpers.hookAllMethods("com.android.server.vibrator.VibratorManagerService", lpparam.classLoader, "vibrate", new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                String pkgName = (String)param.args[1];
                if (pkgName == null) return;
                if (checkVibration(pkgName, param.thisObject)) param.setResult(null);
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
        MainModule.resHooks.setDensityReplacement("*", "dimen", "status_bar_height_default", heightDpi);
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

    public static void StatusBarBackgroundHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader, "generateLayout", "com.android.internal.policy.DecorView", new MethodHook() {
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

        Helpers.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader, "setStatusBarColor", int.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Window wnd = (Window)param.thisObject;
                if (isIgnored(wnd.getContext())) return;
                if (actionBarColor != NOCOLOR) param.args[0] = actionBarColor;
                else if (Color.alpha((int)param.args[0]) < 255) param.args[0] = Color.TRANSPARENT;
            }
        });

        Helpers.findAndHookMethod("com.android.internal.app.ToolbarActionBar", lpparam.classLoader, "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                hookToolbar(param.thisObject, (Drawable)param.args[0]);
            }
        });

        Helpers.findAndHookMethod("com.android.internal.app.WindowDecorActionBar", lpparam.classLoader, "setBackgroundDrawable", Drawable.class, new MethodHook() {
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

    private static boolean checkToast(String pkgName) {
        try {
            int opt = MainModule.mPrefs.getStringAsInt("system_blocktoasts", 1);
            Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_blocktoasts_apps");
            boolean isSelected = selectedApps != null && selectedApps.contains(pkgName);
            return opt == 2 && !isSelected || opt == 3 && isSelected;
        } catch (Throwable t) {
            XposedBridge.log(t);
            return false;
        }
    }

    public static void SelectiveToastsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.notification.NotificationManagerServiceImpl", lpparam.classLoader, "registerPrivacyInputMode", Handler.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Handler mHandler = (Handler) param.args[0];
                Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                new Helpers.SharedPrefObserver(mContext, mHandler) {
                    @Override
                    public void onChange(Uri uri) {
                        try {
                            String key = uri.getPathSegments().get(2);
                            if (key.equals("pref_key_system_blocktoasts_apps"))
                                MainModule.mPrefs.put(key, Helpers.getSharedStringSetPref(mContext, key));
                            else if (key.equals("pref_key_system_blocktoasts")) {
                                MainModule.mPrefs.put(key, Helpers.getSharedStringPref(mContext, key, "1"));
                            }
                        } catch (Throwable t) {
                            Helpers.log(t);
                        }
                    }
                };
            }
        });
        Helpers.hookAllMethods("com.android.server.notification.NotificationManagerService", lpparam.classLoader, "tryShowToast", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String pkgName = (String) XposedHelpers.getObjectField(param.args[0], "pkg");
                if (pkgName == null) return;
                if (checkToast(pkgName)) param.setResult(false);
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
                    if (param.args.length < 6) return;
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

        String ActQueryService = Helpers.isTPlus() ? "com.android.server.pm.ComputerEngine" : "com.android.server.pm.PackageManagerService$ComputerEngine";
        Helpers.hookAllMethods(ActQueryService, lpparam.classLoader, "queryIntentActivitiesInternal", hook);
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
                    if (param.args.length < 6) return;
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

        String ActQueryService = Helpers.isTPlus() ? "com.android.server.pm.ComputerEngine" : "com.android.server.pm.PackageManagerService$ComputerEngine";
        Helpers.hookAllMethods(ActQueryService, lpparam.classLoader, "queryIntentActivitiesInternal", hook);
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
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery4")) {
                    TextView mBatteryPercentMarkView = (TextView)XposedHelpers.getObjectField(param.thisObject, "mBatteryPercentMarkView");
                    mBatteryPercentMarkView.setVisibility(View.GONE);
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

    public static void DisableAnyNotificationBlockHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("android.app.NotificationChannel", lpparam.classLoader, "isBlockable", XC_MethodReplacement.returnConstant(true));
        Helpers.findAndHookMethod("android.app.NotificationChannel", lpparam.classLoader, "setBlockable", boolean.class, new MethodHook() {
            @Override
            protected void before(final MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        });
    }

    public static void DisableAnyNotificationHook(LoadPackageParam lpparam) {
        if (lpparam.packageName.contains("systemui")) {
            Class<?> NotifyManagerCls = findClass("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(NotifyManagerCls, "USE_WHITE_LISTS", false);
            Helpers.findAndHookMethod("com.miui.systemui.NotificationCloudData$Companion", lpparam.classLoader, "getFloatBlacklist", Context.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.setResult(new ArrayList<String>());
                }
            });
        }
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

    public static void MobileNetworkTypeHook(LoadPackageParam lpparam) {
        String MobileController = Helpers.isTPlus() ? "com.android.systemui.statusbar.connectivity.MobileSignalController" : "com.android.systemui.statusbar.policy.MobileSignalController";
        Helpers.findAndHookMethod(MobileController, lpparam.classLoader, "getMobileTypeName", int.class, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                String net = (String)param.getResult();
                if (MainModule.mPrefs.getBoolean("system_4gtolte")) {
                    if ("4G".equals(net)) param.setResult("LTE");
                    else if ("4G+".equals(net)) param.setResult("LTE+");
                }
                else {
                    String mobileType = MainModule.mPrefs.getString("system_statusbar_mobile_showname", "");
                    param.setResult(mobileType);
                }
            }
        });
    }

    public static void HideProximityWarningHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.classLoader, "showHint", XC_MethodReplacement.DO_NOTHING);
        Helpers.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.classLoader, "prepareHintWindow", XC_MethodReplacement.DO_NOTHING);
    }

    public static void HideLockScreenClockHook(LoadPackageParam lpparam) {
        if (!Helpers.isTPlus()) {
            Helpers.findAndHookMethod("com.android.keyguard.clock.KeyguardClockContainer", lpparam.classLoader, "updateClock", float.class, int.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.args[0] = 0.0f;
                }
            });
            Helpers.hookAllMethods("com.android.keyguard.KeyguardVisibilityHelper", lpparam.classLoader, "setViewVisibility", new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Object KeyguardClockInjector = XposedHelpers.callStaticMethod(findClassIfExists("com.android.systemui.Dependency", lpparam.classLoader), "get", findClassIfExists("com.android.keyguard.injector.KeyguardClockInjector", lpparam.classLoader));
                    View mKeyguardClockView = (View)XposedHelpers.callMethod(KeyguardClockInjector, "getView");
                    if (mKeyguardClockView == null) {
                        Helpers.log("HideLockScreenClockHook", "mKeyguardClockView is null");
                        return;
                    }
                    mKeyguardClockView.animate().cancel();
                    XposedHelpers.setBooleanField(param.thisObject, "mKeyguardViewVisibilityAnimating", false);
                    mKeyguardClockView.setAlpha(0.0f);
                    mKeyguardClockView.setVisibility(View.INVISIBLE);
                }
            });
        }
        else {
            MethodHook hideClockHook = new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    View mClockFrame = (View) XposedHelpers.getObjectField(param.thisObject, "mClockFrame");
                    mClockFrame.setVisibility(View.INVISIBLE);
                    mClockFrame = (View) XposedHelpers.getObjectField(param.thisObject, "mLargeClockFrame");
                    mClockFrame.setVisibility(View.INVISIBLE);
                }
            };
            Helpers.hookAllMethods("com.android.keyguard.KeyguardClockSwitch", lpparam.classLoader, "setClockPlugin", hideClockHook);
            Helpers.findAndHookMethod("com.android.keyguard.KeyguardClockSwitch", lpparam.classLoader, "updateClockViews", boolean.class, boolean.class, hideClockHook);
        }
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

    public static void DisableSystemIntegrityHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("android.util.apk.ApkSignatureVerifier", lpparam.classLoader, "getMinimumSignatureSchemeVersionForTargetSdk", int.class, XC_MethodReplacement.returnConstant(1));
    }

    public static void NoSignatureVerifyServiceHook(LoadPackageParam lpparam) {
        if (!Helpers.isTPlus()) {
            Helpers.hookAllMethodsSilently("com.miui.server.SecurityManagerService", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(0));
            Helpers.hookAllMethodsSilently("com.miui.server.SecurityManagerService", lpparam.classLoader, "checkSysAppCrack", XC_MethodReplacement.returnConstant(false));
            Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(0));
            Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "matchSignaturesCompat", XC_MethodReplacement.returnConstant(true));
            Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "matchSignaturesRecover", XC_MethodReplacement.returnConstant(true));
            Helpers.hookAllMethodsSilently("miui.util.CertificateUtils", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(0));
            return;
        }
        Class <?> SignDetails = findClassIfExists("android.content.pm.SigningDetails", lpparam.classLoader);
        Object signUnknown = XposedHelpers.getStaticObjectField(SignDetails, "UNKNOWN");
        Helpers.hookAllMethods(SignDetails, "checkCapability", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                if (param.thisObject == signUnknown || param.args[0] == signUnknown) {
                    param.setResult(false);
                    return;
                }
                int flags = (int) param.args[1];
                if (flags != 4) param.setResult(true);
            }
        });

        Helpers.hookAllConstructors("android.util.jar.StrictJarVerifier", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                XposedHelpers.setObjectField(param.thisObject, "signatureSchemeRollbackProtectionsEnforced", false);
            }
        });
        Helpers.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.classLoader, "verifyMessageDigest", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.classLoader, "verify", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethods("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "verifySignatures", XC_MethodReplacement.returnConstant(false));
        Helpers.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.classLoader, "doesSignatureMatchForPermissions", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String packageName = (String) XposedHelpers.callMethod(param.args[1], "getPackageName");
                String sourcePackageName = (String) param.args[0];
                if (sourcePackageName.equals(packageName)) {
                    param.setResult(true);
                }
            }
        });
        Helpers.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.classLoader, "cannotInstallWithBadPermissionGroups", XC_MethodReplacement.returnConstant(false));
        Helpers.hookAllMethods("com.android.server.pm.permission.PermissionManagerServiceImpl", lpparam.classLoader, "shouldGrantPermissionBySignature", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean isSystem = (boolean) XposedHelpers.callMethod(param.args[0], "isSystem");
                if (isSystem) {
                    param.setResult(true);
                }
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

        Helpers.findAndHookMethod("android.widget.AbsListView", lpparam.classLoader, "initAbsListView", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                ((AbsListView)param.thisObject).setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        });
    }

    public static void RemoveSecureHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.wm.WindowState", lpparam.classLoader, "isSecureLocked", XC_MethodReplacement.returnConstant(false));
        if (Helpers.isTPlus()) {
            Helpers.findAndHookMethod("com.android.server.wm.WindowSurfaceController", lpparam.classLoader, "setSecure", boolean.class, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.args[0] = false;
                }
            });
            Helpers.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.classLoader, new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    int flags = (int) param.args[2];
                    int secureFlag = 128;
                    flags &= ~secureFlag;
                    param.args[2] = flags;
                }
            });
        }
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
        Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl", lpparam.classLoader, "shouldAllowLockscreenRemoteInput", XC_MethodReplacement.returnConstant(true));
        if (!Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl", lpparam.classLoader, "setLockscreenAllowRemoteInput", boolean.class, XC_MethodReplacement.returnConstant(true))) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl", lpparam.classLoader, "setLockScreenAllowRemoteInput", boolean.class, XC_MethodReplacement.returnConstant(true));
        }
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

    public static void LockScreenTimeoutHook(LoadPackageParam lpparam) {
        Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationShadeWindowControllerImpl", lpparam.classLoader, "applyUserActivityTimeout", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Object mLpChanged = XposedHelpers.getObjectField(param.thisObject, "mLpChanged");
                if (mLpChanged == null) return;
                long userActivityTimeout = XposedHelpers.getLongField(mLpChanged, "userActivityTimeout");
                if (userActivityTimeout > 0)
                    XposedHelpers.setLongField(mLpChanged, "userActivityTimeout", MainModule.mPrefs.getInt("system_lstimeout", 3) * 1000L);
            }
        });
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat("H:m", Locale.ENGLISH);
    public static void MuffledVibrationHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.vibrator.VibratorManagerService", lpparam.classLoader, "systemReady", new MethodHook() {
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
        Helpers.hookAllMethods("android.appwidget.AppWidgetHostView", null, "getAppWidgetInfo", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo)param.getResult();
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

    public static void LockScreenAlarmHook(LoadPackageParam lpparam) {
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

        int format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2);
        if (format > 2) {
            Helpers.findAndHookMethod("com.miui.screenshot.MiuiScreenshotApplication", lpparam.classLoader, "attachBaseContext", Context.class, new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Context mContext = (Context) param.args[0];
                    long versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
                    MethodHook changeFormatHook = new MethodHook() {
                        @Override
                        protected void before(MethodHookParam param) throws Throwable {
                            if (param.args.length != 7) return;
                            Bitmap.CompressFormat compress = format <= 2 ? Bitmap.CompressFormat.JPEG : (format == 3 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
                            param.args[4] = compress;
                        }
                    };
                    if (versionCode >= 10400056) {
                        Helpers.hookAllMethods("com.miui.screenshot.u0.f$a", lpparam.classLoader, "a", changeFormatHook);
                    }
                    else if (versionCode >= 10400034) {
                        Helpers.hookAllMethods("com.miui.screenshot.x0.e$a", lpparam.classLoader, "a", changeFormatHook);
                    }
                }
            });
        }

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

    public static void NoLowBatteryWarningHook() {
        MethodHook settingHook = new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                String key = (String)param.args[1];
                if ("low_battery_dialog_disabled".equals(key)) param.setResult(1);
                else if ("low_battery_sound".equals(key)) param.setResult(null);
            }
        };
        Helpers.hookAllMethods(Settings.System.class, "getInt", settingHook);
        Helpers.hookAllMethods(Settings.Global.class, "getString", settingHook);
    }

    public static void TempHideOverlayAppHook(LoadPackageParam lpparam) {
        final int flagIndex = Helpers.isTPlus() ? 2 : 4;
        Helpers.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                int windowType = (int) param.args[4];
                if (windowType != WindowManager.LayoutParams.TYPE_PHONE
                    && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                    && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    && windowType != WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) return;
                int flags = (int) param.args[flagIndex];
                int skipFlag = 64;
                flags |= skipFlag;
                param.args[flagIndex] = flags;
            }
        });
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

                if (charge < 100) {
                    boolean showCurr = MainModule.mPrefs.getBoolean("system_charginginfo_current");
                    boolean showVolt = MainModule.mPrefs.getBoolean("system_charginginfo_voltage");
                    boolean showWatt = MainModule.mPrefs.getBoolean("system_charginginfo_wattage");
                    boolean showTemp = MainModule.mPrefs.getBoolean("system_charginginfo_temp");

                    ArrayList<String> values = new ArrayList<>();
                    Properties props = null;
                    FileInputStream fis = null;
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
                        float currVal = Math.abs(Integer.parseInt(props.getProperty("POWER_SUPPLY_CURRENT_NOW")) / 1000f / 1000f);
                        if (showCurr) values.add(String.format(Locale.getDefault(), "%.2f", currVal) + " A");
                        float voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW")) / 1000f / 1000f;
                        if (showVolt)
                            values.add(String.format(Locale.getDefault(), "%.1f", voltVal) + " V");
                        if (showWatt)
                            values.add(String.format(Locale.getDefault(), "%.1f", voltVal * currVal) + " W");
                        if (showTemp) {
                            int tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"));
                            values.add(Math.round(tempVal / 10f) + " ℃");
                        }
                    }
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
            protected void before(MethodHookParam param) throws Throwable {
            String pkgName = (String) param.args[0];
            Collection<Object> mNotifications = (Collection<Object>) XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mEntryManager"), "getAllNotifs");
            List list = (List) mNotifications.stream().filter(new Predicate() {
                @Override
                public boolean test(Object obj) {
                    String notifyPkgName = (String) XposedHelpers.callMethod(XposedHelpers.callMethod(obj, "getSbn"), "getPackageName");
                    return pkgName.equals(notifyPkgName);
                }
            }).collect(Collectors.toList());
            if (list.size() < 24) param.setResult(null);
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

    public static void AutoDismissExpandedPopupsHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpManagerPhone$HeadsUpEntryPhone", lpparam.classLoader, "setExpanded", boolean.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                boolean newValue = (boolean) param.args[0];
                if (newValue) {
                    boolean expanded = XposedHelpers.getBooleanField(param.thisObject, "expanded");
                    if (expanded != newValue) {
                        XposedHelpers.callMethod(param.thisObject, "removeAutoRemovalCallbacks");
                        Object nm = XposedHelpers.getSurroundingThis(param.thisObject);
                        Handler mHandler = (Handler) XposedHelpers.getObjectField(nm, "mHandler");
                        Runnable mRemoveAlertRunnable = (Runnable) XposedHelpers.getObjectField(param.thisObject, "mRemoveAlertRunnable");
                        mHandler.postDelayed(mRemoveAlertRunnable, 5000);
                    }
                    param.setResult(null);
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

    private static void DisableFloatingWindowBlacklistHook(LoadPackageParam lpparam) {
        MethodHook clearHook = new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                List<String> blackList = (List<String>)param.getResult();
                if (blackList != null) blackList.clear();
                blackList.add("com.android.camera");
                param.setResult(blackList);
            }
        };
        Helpers.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "getListFromCloudData", clearHook);
        Helpers.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "getStartFromFreeformBlackListFromCloud", clearHook);
        Helpers.hookAllMethods("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "getFreeformBlackList", clearHook);
        Helpers.hookAllMethods("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "getFreeformBlackListFromCloud", clearHook);
        Helpers.hookAllMethods("android.util.MiuiMultiWindowAdapter", lpparam.classLoader, "setFreeformBlackList", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                List<String> blackList = new ArrayList<String>();
                blackList.add("com.android.camera");
                param.args[0] = blackList;
            }
        });
        Helpers.findAndHookMethod("android.util.MiuiMultiWindowUtils", lpparam.classLoader, "isForceResizeable", XC_MethodReplacement.returnConstant(true));
        Helpers.hookAllMethodsSilently("android.util.MiuiMultiWindowUtils", lpparam.classLoader, "isPkgMainActivityResizeable", XC_MethodReplacement.returnConstant(true));
    }

    public static void DisableSideBarSuggestionHook(LoadPackageParam lpparam) {
        DisableFloatingWindowBlacklistHook(lpparam);
    }

    public static void NoFloatingWindowBlacklistHook(LoadPackageParam lpparam) {
        MainModule.resHooks.setResReplacement("android", "array", "freeform_black_list", R.array.miui_resize_black_list);
        MainModule.resHooks.setResReplacement("com.miui.rom", "array", "freeform_black_list", R.array.miui_resize_black_list);
        DisableFloatingWindowBlacklistHook(lpparam);
        if (Helpers.isTPlus()) {
            Helpers.findAndHookMethod("com.android.server.wm.MiuiFreeformServicesUtils", lpparam.classLoader, "supportsFreeform", new MethodHook() {
                @Override
                protected void before(MethodHookParam param) throws Throwable {
                    param.setResult(true);
                }
            });
        }
    }

    public static ConcurrentHashMap<String, Pair<Float, Rect>> fwApps = new ConcurrentHashMap<String, Pair<Float, Rect>>();

    public static String getTaskPackageName(Object thisObject, int taskId) {
        return getTaskPackageName(thisObject, taskId, false, null);
    }

    public static String getTaskPackageName(Object thisObject, int taskId, ActivityOptions options) {
        return getTaskPackageName(thisObject, taskId, options != null, options);
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
    private static ActivityOptions patchActivityOptions(Context mContext, ActivityOptions options, String pkgName) {
        if (options == null) options = ActivityOptions.makeBasic();
        XposedHelpers.callMethod(options, "setLaunchWindowingMode", 5);
        XposedHelpers.callMethod(options, "setMiuiConfigFlag", 2);

        Float scale;
        Rect rect;
        Pair<Float, Rect> values = fwApps.get(pkgName);
        if (values == null || values.first == 0f || values.second == null) {
            scale = 0.7f;
            rect = MiuiMultiWindowUtils.getFreeformRect(mContext);
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

    private static boolean shouldOpenInFreeForm(Intent intent, String callingPackage) {
        if (intent == null || intent.getComponent() == null) return false;
        final List<String> fwBlackList = new ArrayList<String>();
        fwBlackList.add("com.miui.home");
        fwBlackList.add("com.android.camera");
        fwBlackList.add("com.android.systemui");
        String pkgName = intent.getComponent().getPackageName();
        if (fwBlackList.contains(pkgName)) return false;
        boolean openInFw = false;
        final boolean openFwWhenShare = MainModule.mPrefs.getBoolean("system_fw_forcein_actionsend");
        if (openFwWhenShare) {
            boolean whitelist = MainModule.mPrefs.getBoolean("system_fw_forcein_actionsend_in_whitelist");
            boolean appInList = MainModule.mPrefs.getStringSet("system_fw_forcein_actionsend_apps").contains(pkgName);
            if (whitelist ^ appInList) {
                return false;
            }
            if ("com.miui.packageinstaller".equals(pkgName) && intent.getComponent().getClassName().contains("com.miui.packageInstaller.NewPackageInstallerActivity")) {
                return true;
            }
            if (Intent.ACTION_SEND.equals(intent.getAction()) && !pkgName.equals(callingPackage)) {
                openInFw = true;
            }
            else if ("com.tencent.mm".equals(pkgName) && intent.getComponent().getClassName().contains(".plugin.base.stub.WXEntryActivity")) {
                openInFw = true;
            }
        }
        if (!openInFw) {
            Object pkg = XposedHelpers.getAdditionalStaticField(MiuiFreeFormManager.class, "nextFreeformPackage");
            openInFw = pkgName.equals(pkg);
            if (openInFw) {
                XposedHelpers.removeAdditionalStaticField(MiuiFreeFormManager.class, "nextFreeformPackage");
            }
        }
        return openInFw;
    }

    public static void OpenAppInFreeFormHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", lpparam.classLoader, "onSystemReady", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION_PREFIX + "SetFreeFormPackage");
                BroadcastReceiver mReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action == null) return;

                        if (action.equals(ACTION_PREFIX + "SetFreeFormPackage")) {
                            String pkg = intent.getStringExtra("package");
                            XposedHelpers.setAdditionalStaticField(MiuiFreeFormManager.class, "nextFreeformPackage", pkg);
                        }
                    }
                };
                mContext.registerReceiver(mReceiver, intentFilter);
            }
        });
        Helpers.hookAllMethods("com.android.server.wm.ActivityStarter", lpparam.classLoader, "executeRequest", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Object request = param.args[0];
                Intent intent = (Intent) XposedHelpers.getObjectField(request, "intent");
                Object safeOptions = XposedHelpers.getObjectField(request, "activityOptions");
                if (safeOptions != null) {
                    ActivityOptions ao = (ActivityOptions) XposedHelpers.getObjectField(safeOptions, "mOriginalOptions");
                    if (ao != null && XposedHelpers.getIntField(ao, "mLaunchWindowingMode") == 5) {
                        return;
                    }
                }
                String callingPackage = (String) XposedHelpers.getObjectField(request, "callingPackage");
                boolean openInFw = shouldOpenInFreeForm(intent, callingPackage);

//                Bundle ao = safeOptions != null ? (Bundle) XposedHelpers.callMethod(safeOptions, "getActivityOptionsBundle") : null;
//                String reason = (String) XposedHelpers.getObjectField(request, "reason");
//                Helpers.log("startAct: " + callingPackage
//                    + " reason| " + reason
//                    + " intent| " + intent
//                    + " openInFw| " + openInFw
//                    + " activityOptions| " + Helpers.stringifyBundle(ao)
//                    + " intentExtra| " + Helpers.stringifyBundle(intent.getExtras())
//                );

                if (openInFw) {
                    Context mContext = (Context) XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "mService"), "mContext");
                    ActivityOptions options = MiuiMultiWindowUtils.getActivityOptions(mContext, intent.getComponent().getPackageName(), true, false);
                    XposedHelpers.callMethod(param.thisObject, "setActivityOptions", options.toBundle());
                }
            }
        });
//        Helpers.hookAllMethods("com.android.server.wm.ActivityStarterInjector", lpparam.classLoader, "modifyLaunchActivityOptionIfNeed", new MethodHook() {
//            @Override
//            protected void after(MethodHookParam param) throws Throwable {
//                if (!Modifier.isPrivate(param.method.getModifiers())) {
//                    return;
//                }
//                Intent intent = (Intent)param.args[5];
//                if (intent == null || intent.getComponent() == null) return;
//                ActivityOptions ao = (ActivityOptions) param.getResult();
//                String callingPackage = (String) param.args[1];
//                ActivityOptions baseAO = (ActivityOptions) param.args[2];
//                Helpers.log("modifyOptions: " + callingPackage
//                    + " baseOptions| " + Helpers.stringifyBundle(baseAO != null ? baseAO.toBundle() : null)
//                    + " intent| " + intent
//                    + " activityOptions| " + Helpers.stringifyBundle(ao != null ? ao.toBundle() : null)
//                    + " intentExtra| " + Helpers.stringifyBundle(intent.getExtras())
//                );
//            }
//        });
    }

    public static void StickyFloatingWindowsHook(LoadPackageParam lpparam) {
        final List<String> fwBlackList = new ArrayList<String>();
        fwBlackList.add("com.miui.securitycenter");
        fwBlackList.add("com.miui.home");
        fwBlackList.add("com.android.camera");

        Helpers.hookAllMethods("com.android.server.wm.ActivityStarterInjector", lpparam.classLoader, "modifyLaunchActivityOptionIfNeed", new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                if (!Modifier.isPublic(param.method.getModifiers())) {
                    return;
                }
                Intent intent = (Intent)param.args[5];
                if (intent == null || intent.getComponent() == null) return;
                String pkgName = intent.getComponent().getPackageName();
                Context mContext;
                try {
                    mContext = (Context)XposedHelpers.getObjectField(param.args[0], "mContext");
                } catch (Throwable ignore) {
                    mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.args[0], "mService"), "mContext");
                }
                if (fwBlackList.contains(pkgName)) return;
                ActivityOptions options = (ActivityOptions)param.getResult();
                int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");

                if (windowingMode != 5 && fwApps.containsKey(pkgName)) {
                    try {
                        options = patchActivityOptions(mContext, options, pkgName);
                        param.setResult(options);
                    } catch (Throwable t) {
                        Helpers.log(t);
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
                if (Helpers.isTPlus()) {
                    if (options != null) {
                        Object windowToken = XposedHelpers.callMethod(options, "getLaunchRootTask");
                        if (windowToken != null) return;
                    }
                }
                String pkgName = (String) XposedHelpers.getAdditionalInstanceField(param.thisObject, "startPackageName");
                XposedHelpers.removeAdditionalInstanceField(param.thisObject, "startPackageName");
                if (fwBlackList.contains(pkgName)) return;
                int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");
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
                if (Helpers.isTPlus()) {
                    if (options != null) {
                        Object windowToken = XposedHelpers.callMethod(options, "getLaunchRootTask");
                        if (windowToken != null) return;
                    }
                }
                String pkgName = getTaskPackageName(param.thisObject, (int)param.args[2], options);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "startPackageName",  pkgName);
                if (!fwBlackList.contains(pkgName)) {
                    int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");
                    if (windowingMode != 5 && fwApps.containsKey(pkgName)) {
                        Object mService = XposedHelpers.getObjectField(param.thisObject, "mService");
                        Context mContext = (Context)XposedHelpers.getObjectField(mService, "mContext");
                        options = patchActivityOptions(mContext, options, pkgName);
                        XposedHelpers.setObjectField(safeOptions, "mOriginalOptions", options);
                        param.args[3] = safeOptions;
                        Intent intent = new Intent(ACTION_PREFIX + "dismissRecentsWhenFreeWindowOpen");
                        intent.putExtra("package", pkgName);
                        mContext.sendBroadcast(intent);
                    }
                }
            }
        });

        Helpers.findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController$FreeFormReceiver", lpparam.classLoader, "onReceive", Context.class, Intent.class, new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[1];
                String action = intent.getAction();
                if ("miui.intent.action_launch_fullscreen_from_freeform".equals(action)) {
                    Object parentThis = XposedHelpers.getSurroundingThis(param.thisObject);
                    XposedHelpers.setAdditionalInstanceField(parentThis, "skipFreeFormStateClear", true);
                }
            }
        });

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
                        if ("miui.intent.action_launch_fullscreen_from_freeform".equals(action)) {
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
        if (lpparam.packageName.equals("com.miui.home")) {
            Helpers.findAndHookMethodSilently("com.android.systemui.shared.recents.model.Task", lpparam.classLoader, "isSupportSplit", XC_MethodReplacement.returnConstant(true));
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
        else if (lpparam.packageName.equals("android")) {
            MainModule.resHooks.setResReplacement("android", "array", "miui_resize_black_list", R.array.miui_resize_black_list);
            MainModule.resHooks.setResReplacement("com.miui.rom", "array", "miui_resize_black_list", R.array.miui_resize_black_list);
            Class <?> AtmClass = XposedHelpers.findClassIfExists("com.android.server.wm.ActivityTaskManagerServiceImpl", lpparam.classLoader);
            if (AtmClass != null) {
                Helpers.findAndHookMethod(AtmClass, "updateResizeBlackList", Context.class, XC_MethodReplacement.DO_NOTHING);
                Helpers.findAndHookMethod(AtmClass, "getSplitScreenBlackListFromXml", XC_MethodReplacement.DO_NOTHING);
                Helpers.hookAllMethods(AtmClass, "inResizeBlackList", XC_MethodReplacement.returnConstant(false));
            }
        }
//        else {
//            Class <?> AtmClass = XposedHelpers.findClassIfExists("android.app.ActivityTaskManager", lpparam.classLoader);
//            if (AtmClass != null) {
//                Helpers.hookAllMethods(AtmClass, "supportsSplitScreen", XC_MethodReplacement.returnConstant(true));
//            }
//        }
    }

    public static void SecureControlCenterHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethodSilently("com.android.keyguard.utils.MiuiKeyguardUtils", lpparam.classLoader, "supportExpandableStatusbarUnderKeyguard", XC_MethodReplacement.returnConstant(false));
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
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.policy.NotificationAlertController", lpparam.classLoader, "buzzBeepBlink", new MethodHook() {
            @Override
            protected void before(MethodHookParam param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                PowerManager powerMgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                if (powerMgr.isInteractive()) {
                    param.setResult(null);
                }
            }
        });
    }

    public static void NetworkIndicatorWifi(LoadPackageParam lpparam) {
        MethodHook hideWifiActivity = new MethodHook() {
            @Override
            protected void after(final MethodHookParam param) throws Throwable {
                Object mWifiActivityView = XposedHelpers.getObjectField(param.thisObject, "mWifiActivityView");
                XposedHelpers.callMethod(mWifiActivityView, "setVisibility", 4);
            }
        };
        Helpers.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.classLoader, "applyWifiState", hideWifiActivity);
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
        if (Helpers.isTPlus()) {
            Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManagerInjector", lpparam.classLoader, "miuiHeadsUpInset", android.content.Context.class, new MethodHook() {
                private int mHeadsUpPaddingTop = 0;
                private int mHeadsUpHeight = 0;
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    Resources resources = context.getResources();
                    if (mHeadsUpPaddingTop == 0) {
                        int dimId = resources.getIdentifier("heads_up_status_bar_padding", "dimen", "com.android.systemui");
                        mHeadsUpPaddingTop = resources.getDimensionPixelSize(dimId);
                        mHeadsUpHeight = resources.getDimensionPixelSize(resources.getIdentifier("notification_max_heads_up_height", "dimen", "com.android.systemui"));
                    }
                    if (resources.getConfiguration().orientation != 2) {
                        int mHeadsUpInset = (int) param.getResult();
                        int mStatusBarHeight = mHeadsUpInset - mHeadsUpPaddingTop;
                        int topMargin = (context.getResources().getDisplayMetrics().heightPixels + mStatusBarHeight - mHeadsUpHeight) / 2;
                        param.setResult(topMargin);
                    }
                }
            });
            return;
        }
        MethodHook initViewHook = new MethodHook() {
            private int mHeadsUpPaddingTop = 0;
            private int mHeadsUpHeight = 0;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context context = (Context) param.args[0];
                Resources resources = context.getResources();
                if (mHeadsUpPaddingTop == 0) {
                    int dimId = resources.getIdentifier("heads_up_status_bar_padding", "dimen", "com.android.systemui");
                    mHeadsUpPaddingTop = resources.getDimensionPixelSize(dimId);
                    mHeadsUpHeight = resources.getDimensionPixelSize(resources.getIdentifier("notification_max_heads_up_height", "dimen", "com.android.systemui"));
                }
                if (resources.getConfiguration().orientation != 2) {
                    int mStatusBarHeight = XposedHelpers.getIntField(param.thisObject, "mStatusBarHeight");
                    int topMargin = (context.getResources().getDisplayMetrics().heightPixels + mStatusBarHeight - mHeadsUpHeight) / 2;
                    XposedHelpers.setObjectField(param.thisObject, "mHeadsUpInset", topMargin);
                }
            }
        };
        Helpers.findAndHookMethod("com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm", lpparam.classLoader, "initConstants", Context.class, initViewHook);
        Helpers.hookAllMethods("com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout", lpparam.classLoader, "initView", initViewHook);
        Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpManagerPhone", lpparam.classLoader, "updateResources", new MethodHook() {
            private int mHeadsUpPaddingTop = 0;
            private int mHeadsUpHeight = 0;
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                Resources resources = context.getResources();
                if (mHeadsUpPaddingTop == 0) {
                    int dimId = resources.getIdentifier("heads_up_status_bar_padding", "dimen", "com.android.systemui");
                    mHeadsUpPaddingTop = resources.getDimensionPixelSize(dimId);
                    mHeadsUpHeight = resources.getDimensionPixelSize(resources.getIdentifier("notification_max_heads_up_height", "dimen", "com.android.systemui"));
                }
                if (resources.getConfiguration().orientation != 2) {
                    int mHeadsUpInset = XposedHelpers.getIntField(param.thisObject, "mHeadsUpInset");
                    int mStatusBarHeight = mHeadsUpInset - mHeadsUpPaddingTop;
                    int topMargin = (context.getResources().getDisplayMetrics().heightPixels + mStatusBarHeight - mHeadsUpHeight) / 2;
                    XposedHelpers.setObjectField(param.thisObject, "mHeadsUpInset", topMargin);
                }
            }
        });
    }

    public static void WallpaperScaleLevelHook(LoadPackageParam lpparam) {
        Helpers.hookAllConstructors("com.android.server.wm.WallpaperController", lpparam.classLoader, new MethodHook() {
            @Override
            protected void after(MethodHookParam param) throws Throwable {
                float scale = MainModule.mPrefs.getInt("system_other_wallpaper_scale", 6) / 10.0f;
                XposedHelpers.setObjectField(param.thisObject, "mMaxWallpaperScale", scale);
                Context mContext = (Context) XposedHelpers.getObjectField(param.args[0], "mContext");
                Handler mHandler = new Handler(mContext.getMainLooper());
                new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_other_wallpaper_scale", 6) {
                    @Override
                    public void onChange(String name, int defValue) {
                        int val = Helpers.getSharedIntPref(mContext, name, defValue);
                        XposedHelpers.setObjectField(param.thisObject, "mMaxWallpaperScale", val / 10.0f);
                    }
                };
            }
        });
    }

    public static void Disable72hStrongAuthHook(LoadPackageParam lpparam) {
        Helpers.findAndHookMethod("com.android.server.locksettings.LockSettingsStrongAuth", lpparam.classLoader, "rescheduleStrongAuthTimeoutAlarm", long.class, int.class, XC_MethodReplacement.DO_NOTHING);
    }
}