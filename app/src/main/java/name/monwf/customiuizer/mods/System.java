package name.monwf.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static name.monwf.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findMethodExactIfExists;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.MiuiNotification;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
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
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.ArrayMap;
import android.util.MiuiMultiWindowUtils;
import android.util.Pair;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedInterface.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.SystemServerLoadedParam;
import miui.app.MiuiFreeFormManager;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.mods.utils.HookerClassHelper;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.ResourceHooks;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.AudioVisualizer;
import name.monwf.customiuizer.utils.Helpers;
import name.monwf.customiuizer.utils.Helpers.MimeType;

public class System {
    private final static String StatusBarCls = "com.android.systemui.statusbar.phone.CentralSurfacesImpl";

    public static void ScreenAnimHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.getClassLoader(), "initialize", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                try {
                    XposedHelpers.setObjectField(param.getThisObject(), "mColorFadeEnabled", true);
                    XposedHelpers.setObjectField(param.getThisObject(), "mColorFadeFadesConfig", true);
                } catch (Throwable ignore) {}
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ObjectAnimator mColorFadeOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.getThisObject(), "mColorFadeOffAnimator");
                if (mColorFadeOffAnimator != null) {
                    int val = MainModule.mPrefs.getInt("system_screenanim_duration", 0);
                    if (val == 0) val = 250;
                    mColorFadeOffAnimator.setDuration(val);
                }
                ModuleHelper.observePreferenceChange( new ModuleHelper.PreferenceObserver() {
                    @Override
                    public void onChange(String key) {
                        if (key.contains("system_screenanim_duration")) {
                            if (mColorFadeOffAnimator == null) return;
                            int val = MainModule.mPrefs.getInt("system_screenanim_duration", 0);
                            if (val == 0) val = 250;
                            mColorFadeOffAnimator.setDuration(val);
                        }
                    }
                });
            }
        });
    }

    public static void NoAccessDeviceLogsRequest(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.logcat.LogcatManagerService", lpparam.getClassLoader(), "onLogAccessRequested", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                XposedHelpers.callMethod(param.getThisObject(), "declineRequest", param.getArgs()[0]);
                param.returnAndSkip(null);
            }
        });
    }
    public static void NoLightUpOnChargeHook(SystemServerLoadedParam lpparam) {
        String methodName = "wakePowerGroupLocked";
        ModuleHelper.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.getClassLoader(), methodName, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String reason = (String)param.getArgs()[3];
                if (reason == null) return;

                if (Integer.parseInt(MainModule.mPrefs.getString("system_nolightuponcharges", "1")) == 3 &&
                        (reason.equals("android.server.power:POWER") || reason.startsWith("android.server.power:PLUGGED"))) {
                    param.returnAndSkip(false);
                    return;
                }
                if (Integer.parseInt(MainModule.mPrefs.getString("system_nolightuponcharges", "1")) == 2 && (
                        reason.equals("android.server.power:POWER") ||
                                reason.startsWith("android.server.power:PLUGGED") ||
                                reason.equals("com.android.systemui:RAPID_CHARGE") ||
                                reason.equals("com.android.systemui:WIRELESS_CHARGE") ||
                                reason.equals("com.android.systemui:WIRELESS_RAPID_CHARGE")
                )) param.returnAndSkip(false);
            }
        });
    }

    public static void ScramblePINHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardPINView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View[][] mViews = (View[][])XposedHelpers.getObjectField(param.getThisObject(), "mViews");
                ArrayList<View> mRandomViews = new ArrayList<View>();
                for (int row = 1; row <= 3; row++)
                    for (int col = 0; col <= 2; col++)
                        if (mViews[row][col] != null)
                            mRandomViews.add(mViews[row][col]);
                mRandomViews.add(mViews[4][1]);
                Collections.shuffle(mRandomViews);

                View pinview = (View)param.getThisObject();
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

                XposedHelpers.setObjectField(param.getThisObject(), "mViews", mViews);
            }
        });
    }

    public static void NoPasswordHook(SystemServerLoadedParam lpparam) {
        String isAllowed = "isBiometricAllowedForUser";
        ModuleHelper.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", lpparam.getClassLoader(), isAllowed, boolean.class, int.class, HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.android.internal.widget.LockPatternUtils", lpparam.getClassLoader(), isAllowed, int.class, HookerClassHelper.returnConstant(true));
    }

    public static void EnhancedSecurityHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "interceptPowerKeyDown", KeyEvent.class, boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) {
                    Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
                    if (mHandler != null) {
                        Runnable mEndCallLongPress = (Runnable)XposedHelpers.getObjectField(param.getThisObject(), "mEndCallLongPress");
                        if (mEndCallLongPress != null) mHandler.removeCallbacks(mEndCallLongPress);
                    }
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "powerLongPress", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.returnAndSkip(null);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "showGlobalActions", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.returnAndSkip(null);
            }
        });

        ModuleHelper.findAndHookMethodSilently("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "showGlobalActionsInternal", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mPWMContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.returnAndSkip(null);
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
            XposedHelpers.log(t);
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
    public static void NoScreenLockHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader(), "handleKeyguardDone", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (isUnlockedInnerCall) {
                    isUnlockedInnerCall = false;
                    return;
                }
                isUnlockedWithStrong = true;
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.getClassLoader(), "onFingerprintAuthenticated", int.class, boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                isUnlockedWithFingerprint = true;
            }
        });

        ModuleHelper.hookAllConstructors("com.android.keyguard.KeyguardSecurityContainerController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.callMethod(param.getThisObject(), "getContext");
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            Object mCallback = XposedHelpers.getObjectField(param.getThisObject(), "mKeyguardSecurityCallback");
                            XposedHelpers.callMethod(mCallback, "reportUnlockAttempt", 0, true, 0, 0);
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    }
                }, new IntentFilter(ACTION_PREFIX + "UnlockStrongAuth"));
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader(), "doKeyguardLocked", Bundle.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (forcedOption == 0) return;
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                if (!isUnlocked(mContext, lpparam.getClassLoader())) return;

                boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
                if (skip) {
                    param.returnAndSkip(null);
                    XposedHelpers.callMethod(param.getThisObject(), "keyguardDone");
                }
                isUnlockedInnerCall = true;
                Intent unlockIntent = new Intent(ACTION_PREFIX + "UnlockStrongAuth");
                mContext.sendBroadcast(unlockIntent);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader(), "setupLocked", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
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

                        boolean isShowing = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isShowing");
                        if (!isShowing) return;
                        if (!isAuthOnce()) return;

                        boolean isTrusted = false;
                        if (forcedOption == 0) isTrusted = false;
                        else if (forcedOption == 1) isTrusted = true;
                        else if (MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1) == 3)
                            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                                if (netInfo.getState() != NetworkInfo.State.CONNECTED && netInfo.getState() != NetworkInfo.State.DISCONNECTED)
                                    return;
                                if (netInfo.isConnected()) isTrusted = isTrustedWiFi(mContext);
                            } else if (action.equals(ACTION_PREFIX + "UnlockBTConnection")) {
                                isTrusted = isTrustedBt(lpparam.getClassLoader());
                            }

                        XposedHelpers.setAdditionalStaticField(param.getThisObject(), "isScreenLockDisabled", isTrusted);
                        if (isTrusted) {
                            boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
                            if (skip)
                                XposedHelpers.callMethod(param.getThisObject(), "keyguardDone");
                            else
                                XposedHelpers.callMethod(param.getThisObject(), "resetStateLocked");
                            isUnlockedInnerCall = true;
//                            XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
                            Intent unlockIntent = new Intent(ACTION_PREFIX + "UnlockStrongAuth");
                            mContext.sendBroadcast(unlockIntent);
                        } else try {
                            Object mLockUserManager = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", findClassIfExists("com.android.systemui.statusbar.NotificationLockscreenUserManager", lpparam.getClassLoader()));
                            XposedHelpers.callMethod(mLockUserManager, "updatePublicMode");
                        } catch (Throwable t) {
                            XposedHelpers.log(t);
                        }
                    }
                }, filter);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardSecurityModel", lpparam.getClassLoader(), "getSecurityMode", int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (forcedOption == 0) return;
                boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
                if (skip) return;
                Context mContext = ModuleHelper.findContext(lpparam);
                if (!isUnlocked(mContext, lpparam.getClassLoader())) return;

                Class<?> securityModeEnum = findClass("com.android.keyguard.KeyguardSecurityModel$SecurityMode", lpparam.getClassLoader());
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

        Class<?> startClass = findClassIfExists("com.android.keyguard.faceunlock.FaceUnlockManager", lpparam.getClassLoader());
        if (startClass != null) {
            ModuleHelper.hookAllMethods(startClass, "startFaceUnlock", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_nofaceunlock");
                    if (!skip) return;
                    if (param.getArgs().length == 0) return;
                    Boolean isScreenLockDisabled = (Boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader()), "isScreenLockDisabled");
                    isScreenLockDisabled = isScreenLockDisabled != null && isScreenLockDisabled;
                    if (isScreenLockDisabled) param.returnAndSkip(null);
                }
            });

            Method showMsgMethod = findMethodExactIfExists(startClass, "isShowMessageWhenFaceUnlockSuccess");
            if (showMsgMethod == null) findMethodExactIfExists(startClass, "isFaceUnlockSuccessAndShowMessage");
            if (showMsgMethod == null) {
                XposedHelpers.log("NoScreenLockHook", "Show notification message method not found");
            } else {
                ModuleHelper.hookAllMethods(startClass, showMsgMethod.getName(), new MethodHook() {
                    @Override
                    protected void after(final AfterHookCallback param) throws Throwable {
                        boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_nofaceunlock");
                        if (!skip || !(boolean)param.getResult()) return;
                        boolean isScreenLockDisabled = (boolean)XposedHelpers.getAdditionalStaticField(findClass("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader()), "isScreenLockDisabled");
                        if (isScreenLockDisabled) param.setResult(false);
                    }
                });
            }
        }

        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) {
                Context mContext = (Context)param.getArgs()[0];
                mContext.registerReceiver(new BroadcastReceiver() {
                    public void onReceive(final Context context, Intent intent) {
                        ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
                        Intent updateIntent = new Intent(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE");
                        Collection<?> cachedDevices = (Collection<?>)XposedHelpers.callMethod(param.getThisObject(), "getDevices");
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

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.getClassLoader(), "updateConnected", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                if (mContext != null) {
                    mContext.sendBroadcast(new Intent(ACTION_PREFIX + "UnlockBTConnection"));
                }
            }
        });
    }

    public static void DoubleTapToSleepHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View view = (View)param.getThisObject();
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

    public static void NotificationVolumeServiceHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.audio.AudioService", lpparam.getClassLoader(), "updateStreamVolumeAlias", boolean.class, String.class, new MethodHook() {
            protected void after(final AfterHookCallback param) throws Throwable {
                int[] mStreamVolumeAlias = (int[])XposedHelpers.getObjectField(param.getThisObject(), "mStreamVolumeAlias");
                mStreamVolumeAlias[1] = 1;
                mStreamVolumeAlias[5] = 5;
                XposedHelpers.setObjectField(param.getThisObject(), "mStreamVolumeAlias", mStreamVolumeAlias);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.audio.AudioService$VolumeStreamState", lpparam.getClassLoader(), "readSettings", new MethodHook() {
            protected void before(final BeforeHookCallback param) throws Throwable {
                int mStreamType = XposedHelpers.getIntField(param.getThisObject(), "mStreamType");
                if (mStreamType != 1) return;
                synchronized (param.getMember().getDeclaringClass()) {
                    Class<?> audioSystem = findClass("android.media.AudioSystem", lpparam.getClassLoader());
                    Set<Integer> DEVICE_OUT_ALL = (Set<Integer>) XposedHelpers.getStaticObjectField(audioSystem, "DEVICE_OUT_ALL_SET");
                    int DEVICE_OUT_DEFAULT = XposedHelpers.getStaticIntField(audioSystem, "DEVICE_OUT_DEFAULT");
                    int[] DEFAULT_STREAM_VOLUME = (int[])XposedHelpers.getStaticObjectField(audioSystem, "DEFAULT_STREAM_VOLUME");
                    Set<Integer> remainingDevices = DEVICE_OUT_ALL;
                    Object mContentResolver = XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.getThisObject()), "mContentResolver");
                    SparseIntArray mIndexMap = (SparseIntArray)XposedHelpers.getObjectField(param.getThisObject(), "mIndexMap");
                    for (Integer deviceType : remainingDevices) {
                        int device = deviceType;
                        String name = (String)XposedHelpers.callMethod(param.getThisObject(), "getSettingNameForDevice", device);
                        int index = (int)XposedHelpers.callStaticMethod(Settings.System.class, "getIntForUser", mContentResolver, name, device == DEVICE_OUT_DEFAULT ? DEFAULT_STREAM_VOLUME[mStreamType] : -1, -2);
                        if (index != -1) {
                            mIndexMap.put(device, (int)XposedHelpers.callMethod(param.getThisObject(), "getValidIndex", 10 * index, true));
                        }
                    }
                    XposedHelpers.setObjectField(param.getThisObject(), "mIndexMap", mIndexMap);
                }
                param.returnAndSkip(null);
            }
        });

        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently("com.android.server.audio.AudioService", lpparam.getClassLoader(), "shouldZenMuteStream", int.class, new MethodHook() {
            protected void after(final AfterHookCallback param) throws Throwable {
                int mStreamType = (int)param.getArgs()[0];
                if (mStreamType == 5 && !(boolean)param.getResult()) {
                    int mZenMode = (int)XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mNm"), "getZenMode");
                    if (mZenMode == 1) param.setResult(true);
                }
            }
        });
    }

    public static void NotificationVolumeSettingsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.settings.MiuiSoundSettings", lpparam.getClassLoader(), "onCreate", Bundle.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object fragment = param.getThisObject();
                Context context = (Context)XposedHelpers.callMethod(fragment, "getActivity");
                Resources modRes = ModuleHelper.getModuleRes(context);
                int order = 6;

                Class<?> vsbCls;
                Method[] initSeekBar;
                String addPreference = "addPreference";
                try {
                    vsbCls = findClassIfExists("com.android.settings.sound.VolumeSeekBarPreference", lpparam.getClassLoader());
                    initSeekBar = XposedHelpers.findMethodsByExactParameters(fragment.getClass(), void.class, String.class, int.class, int.class);
                    if (vsbCls == null || initSeekBar.length == 0) {
                        XposedHelpers.log("NotificationVolumeSettingsHook", "Unable to find class/method in Settings to hook");
                        return;
                    } else {
                        initSeekBar[0].setAccessible(true);
                    }
                    Class<?> pgCls = findClassIfExists("androidx.preference.PreferenceGroup", lpparam.getClassLoader());
                    Class<?> pCls = findClassIfExists("androidx.preference.Preference", lpparam.getClassLoader());
                    Method[] methods = XposedHelpers.findMethodsByExactParameters(pgCls, void.class, pCls);
                    for (Method method: methods)
                        if (Modifier.isPublic(method.getModifiers())) {
                            addPreference = method.getName();
                            break;
                        }
                } catch (Throwable t) {
                    XposedHelpers.log("NotificationVolumeSettingsHook", "Unable to find class/method in Settings to hook");
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

                Object mRingVolume = XposedHelpers.callMethod(param.getThisObject(), "findPreference", "ring_volume");
                XposedHelpers.callMethod(mRingVolume, "setTitle", callsResId);
            }
        });
    }

    public static void ViewWifiPasswordHook(PackageLoadedParam lpparam) {
        int titleId = MainModule.resHooks.addResource("system_wifipassword_btn_title", R.string.system_wifipassword_btn_title);
        int dlgTitleId = MainModule.resHooks.addResource("system_wifi_password_dlgtitle", R.string.system_wifi_password_dlgtitle);
        ModuleHelper.hookAllMethods("com.android.settings.wifi.SavedAccessPointPreference", lpparam.getClassLoader(), "onBindViewHolder", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View view = (View) XposedHelpers.getObjectField(param.getThisObject(), "mView");
                int btnId = view.getResources().getIdentifier("btn_delete", "id", "com.android.settings");
                Button button = view.findViewById(btnId);
                button.setText(titleId);
            }
        });
        final String[] wifiSharedKey = new String[1];
        final String[] passwordTitle = new String[1];
        ModuleHelper.findAndHookMethod("miuix.appcompat.app.AlertDialog$Builder", lpparam.getClassLoader(), "setTitle", int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (wifiSharedKey[0] != null) {
                    param.getArgs()[0] = dlgTitleId;
                }
            }
        });

        ModuleHelper.findAndHookMethod("miuix.appcompat.app.AlertDialog$Builder", lpparam.getClassLoader(), "setMessage", CharSequence.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (wifiSharedKey[0] != null) {
                    CharSequence str = (CharSequence) param.getArgs()[0];
                    str = str + "\n" + passwordTitle[0] + ": " + wifiSharedKey[0];
                    param.getArgs()[0] = str;
                }
            }
        });
        ModuleHelper.hookAllMethods("miuix.appcompat.app.AlertDialog", lpparam.getClassLoader(), "onCreate", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (wifiSharedKey[0] != null) {
                    TextView messageView = (TextView) XposedHelpers.callMethod(param.getThisObject(), "getMessageView");
                    messageView.setTextIsSelectable(true);
                }
            }
        });
        ModuleHelper.hookAllMethods("com.android.settings.wifi.MiuiSavedAccessPointsWifiSettings", lpparam.getClassLoader(), "showDeleteDialog", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object wifiEntry = param.getArgs()[0];
                boolean canShare = (boolean) XposedHelpers.callMethod(wifiEntry, "canShare");
                if (canShare) {
                    if (passwordTitle[0] == null) {
                        Resources modRes = ModuleHelper.getModuleRes((Context) XposedHelpers.callMethod(param.getThisObject(), "getContext"));
                        passwordTitle[0] = modRes.getString(R.string.system_wifi_password_label);
                    }
                    Object mWifiManager = XposedHelpers.getObjectField(param.getThisObject(), "mWifiManager");
                    Object wifiConfiguration = XposedHelpers.callMethod(wifiEntry, "getWifiConfiguration");
                    Class <?> WifiDppUtilsClass = findClass("com.android.settings.wifi.dpp.WifiDppUtils", lpparam.getClassLoader());
                    String sharedKey = (String) XposedHelpers.callStaticMethod(WifiDppUtilsClass, "getPresharedKey", mWifiManager, wifiConfiguration);
                    sharedKey = (String) XposedHelpers.callStaticMethod(WifiDppUtilsClass, "removeFirstAndLastDoubleQuotes", sharedKey);
                    wifiSharedKey[0] = sharedKey;
                }
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object wifiEntry = param.getArgs()[0];
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
    public static void StatusBarClockTweakHook(PackageLoadedParam lpparam) {
        boolean statusbarClockTweak = MainModule.mPrefs.getBoolean("system_statusbar_clocktweak");
        boolean ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak");
        MethodHook ScheduleHook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                initSecondTimer(param.getThisObject());
                Context mContext = (Context) XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                IntentFilter timeSetIntent = new IntentFilter();
                timeSetIntent.addAction("android.intent.action.TIME_SET");
                BroadcastReceiver mUpdateTimeReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        initSecondTimer(param.getThisObject());
                    }
                };
                mContext.registerReceiver(mUpdateTimeReceiver, timeSetIntent);
            }
        };
        if (ccClockTweak || statusbarClockTweak) {
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.getClassLoader(), ScheduleHook);
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.getClassLoader(), "fireTimeChange", new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    Object clockController = param.getThisObject();
                    ArrayList<Object> mClockListeners = (ArrayList<Object>) XposedHelpers.getObjectField(clockController, "mClockListeners");
                    Iterator<Object> it = mClockListeners.iterator();
                    while (it.hasNext()) {
                        Object clock = it.next();
                        Object showSeconds = XposedHelpers.getAdditionalInstanceField(clock, "showSeconds");
                        if (showSeconds == null) {
                            XposedHelpers.callMethod(clock, "onTimeChange");
                        }
                    }
                    param.returnAndSkip(null);
                }
            });
        }
        String ccDateFormat = MainModule.mPrefs.getString("system_cc_dateformat", "");
        boolean ccDateCustom = ccDateFormat.length() > 0;
        boolean hideDateView = MainModule.mPrefs.getBoolean("system_cc_hidedate");
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) {
                final TextView clock = (TextView)param.getThisObject();
                if (param.getArgs().length != 3) return;
                int clockId = clock.getResources().getIdentifier("clock", "id", "com.android.systemui");
                int bigClockId = clock.getResources().getIdentifier("big_time", "id", "com.android.systemui");
                int dateClockId = clock.getResources().getIdentifier("date_time", "id", "com.android.systemui");
                int horizDateClockId = clock.getResources().getIdentifier("horizontal_date_time", "id", "com.android.systemui");
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
                else if ((dateClockId == thisClockId || thisClockId == horizDateClockId) && (ccDateCustom || hideDateView)) {
                    XposedHelpers.setAdditionalInstanceField(clock, "clockName", "ccDate");
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "updateTime", new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                TextView clock = (TextView)param.getThisObject();
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
                    param.returnAndSkip(null);
                }
            }
        });
        if (hideDateView) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "setClockVisibility", int.class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    TextView clock = (TextView)param.getThisObject();
                    String clockName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "clockName");
                    if ("ccDate".equals(clockName)) {
                        XposedHelpers.setObjectField(param.getThisObject(), "mVisibility", 8);
                        clock.setVisibility(View.GONE);
                        param.returnAndSkip(null);
                    }
                }
            });
        }
        if (statusbarClockTweak) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onAttachedToWindow", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    TextView clock = (TextView) XposedHelpers.getObjectField(param.getThisObject(), "mMiuiClock");
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
                    protected void after(final AfterHookCallback param) throws Throwable {
                        TextView clock = (TextView)XposedHelpers.getObjectField(param.getThisObject(), "mBigTime");
                        if (ccClockFontSize > 9) {
                            clock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ccClockFontSize);
                        }
                        String clsName = param.getThisObject().getClass().getSimpleName();
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
                ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateResources", setSizeHook);
                ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.getClassLoader(), "updateResources", setSizeHook);
            }
        }
    }

    public static void ExpandNotificationsHook(PackageLoadedParam lpparam) {
        String feedbackMethod = "setFeedbackIcon";
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), feedbackMethod, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean mOnKeyguard = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isOnKeyguard");
                if (!mOnKeyguard) {
                    Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.getThisObject(), "getEntry"), "mSbn");
                    String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                    int opt = Integer.parseInt(MainModule.mPrefs.getString("system_expandnotifs", "1"));
                    boolean isSelected = MainModule.mPrefs.getStringSet("system_expandnotifs_apps").contains(pkgName);
                    if (opt == 2 && !isSelected || opt == 3 && isSelected)
                        XposedHelpers.callMethod(param.getThisObject(), "setSystemExpanded", true);
                }
            }
        });
    }

    public static void ExpandHeadsUpHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), "setHeadsUp", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean mOnKeyguard = (boolean) XposedHelpers.callMethod(param.getThisObject(), "isOnKeyguard");
                boolean showHeadsUp = (boolean) param.getArgs()[0];
                if (!mOnKeyguard && showHeadsUp) {
                    View notifyRow = (View) param.getThisObject();
                    Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.getThisObject(), "getEntry"), "mSbn");
                    String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
                    int opt = Integer.parseInt(MainModule.mPrefs.getString("system_expandheadups", "1"));
                    boolean isSelected = MainModule.mPrefs.getStringSet("system_expandheadups_apps").contains(pkgName);
                    if (opt == 2 && !isSelected || opt == 3 && isSelected) {
                        Runnable expandNotify = new Runnable() {
                            @Override
                            public void run() {
                                XposedHelpers.callMethod(param.getThisObject(), "expandNotification");
                            }
                        };
                        notifyRow.postDelayed(expandNotify, 60);
                    }
                }
            }
        });
    }

    public static void DrawerBlurRatioHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View mView = (View) param.getArgs()[0];
                Context mContext = mView.getContext();
                Handler mHandler = new Handler(mContext.getMainLooper());

                Object mControlPanelWindowManager = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", lpparam.getClassLoader()), "get", findClass("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.getClassLoader()));
                Object notificationShadeDepthController = XposedHelpers.getObjectField(param.getThisObject(), "notificationShadeDepthController");
                int initBlurRatio = MainModule.mPrefs.getInt("system_drawer_blur", 100);
                XposedHelpers.setAdditionalInstanceField(notificationShadeDepthController, "mCustomBlurModifier", initBlurRatio);
                XposedHelpers.setAdditionalInstanceField(mControlPanelWindowManager, "mCustomBlurModifier", initBlurRatio);
                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
                    public void onChange(String key) {
                        if (key.contains("system_drawer_blur")) {
                            int opt = MainModule.mPrefs.getInt("system_drawer_blur", 100);
                            XposedHelpers.setAdditionalInstanceField(notificationShadeDepthController, "mCustomBlurModifier", opt);
                            XposedHelpers.setAdditionalInstanceField(mControlPanelWindowManager, "mCustomBlurModifier", opt);
                        }
                    }
                });
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationShadeDepthController$updateBlurCallback$1", lpparam.getClassLoader(), "doFrame", long.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object parentCtrl = XposedHelpers.getSurroundingThis(param.getThisObject());
                Object blurRatio = XposedHelpers.getAdditionalInstanceField(parentCtrl, "mCustomBlurModifier");
                Object mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt");
                XposedHelpers.setAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier", blurRatio);
            }
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object parentCtrl = XposedHelpers.getSurroundingThis(param.getThisObject());
                Object mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt");
                XposedHelpers.removeAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier");
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BlurUtilsExt", lpparam.getClassLoader(), "applyBlurByRadius", View.class, int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object multiplier = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mCustomBlurModifier");
                if (multiplier != null) {
                    Object blurUtils = XposedHelpers.getObjectField(param.getThisObject(), "blurUtils");
                    float ratio = (float) XposedHelpers.callMethod(blurUtils, "ratioOfBlurRadius", 1.0f * (int)param.getArgs()[1]);
                    float newRatio = ratio * (int)multiplier / 100f;
                    param.getArgs()[1] = Math.round((float)XposedHelpers.callMethod(blurUtils, "blurRadiusOfRatio", newRatio));
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.getClassLoader(), "setBlurRatio", float.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = (float)param.getArgs()[0] * (int)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mCustomBlurModifier") / 100f;
            }
        });
    }

    public static void ChargeAnimationHook(PackageLoadedParam lpparam) {
        Class<?> ccCls;
        try {
            ccCls = findClass("com.android.keyguard.charge.MiuiWirelessChargeController", lpparam.getClassLoader());
        } catch (Throwable t1) {
            try {
                ccCls = findClass("com.android.keyguard.charge.MiuiChargeController", lpparam.getClassLoader());
            } catch (Throwable t2) {
                XposedHelpers.log(t1);
                XposedHelpers.log(t2);
                return;
            }
        }

        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently(ccCls, "showWirelessChargeAnimation", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
                Runnable mScreenOffRunnable = (Runnable)XposedHelpers.getObjectField(param.getThisObject(), "mScreenOffRunnable");
                WakeLock mScreenOnWakeLock = (WakeLock)XposedHelpers.getObjectField(param.getThisObject(), "mScreenOnWakeLock");

                if (mContext != null && mHandler != null && mScreenOffRunnable != null && mScreenOnWakeLock != null) {
                    if (mScreenOnWakeLock.isHeld()) {
                        int timeout = MainModule.mPrefs.getInt("system_chargeanimtime", 20) * 1000;
                        mHandler.postDelayed(mScreenOnWakeLock::release, timeout);
                        mHandler.removeCallbacks(mScreenOffRunnable);
                        mHandler.postDelayed(mScreenOffRunnable, timeout);
                    }
                } else XposedHelpers.log("ChargeAnimationHook1", "Something is NULL! :)");
            }
        });

        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently(ccCls, "showRapidChargeAnimation", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
                Runnable mScreenOffRunnable = (Runnable)XposedHelpers.getObjectField(param.getThisObject(), "mScreenOffRunnable");
                WakeLock mScreenOnWakeLock = (WakeLock)XposedHelpers.getObjectField(param.getThisObject(), "mScreenOnWakeLock");

                if (mContext != null && mHandler != null && mScreenOffRunnable != null && mScreenOnWakeLock != null) {
                    if (mScreenOnWakeLock.isHeld()) {
                        int timeout = MainModule.mPrefs.getInt("system_chargeanimtime", 20) * 1000;
                        mHandler.postDelayed(mScreenOnWakeLock::release, timeout);
                        mHandler.removeCallbacks(mScreenOffRunnable);
                        mHandler.postDelayed(mScreenOffRunnable, timeout);
                    }
                } else XposedHelpers.log("ChargeAnimationHook2", "Something is NULL! :)");
            }
        });

        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently(ccCls, "showWirelessRapidChargeAnimation", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
                Runnable mScreenOffRunnable = (Runnable)XposedHelpers.getObjectField(param.getThisObject(), "mScreenOffRunnable");
                WakeLock mScreenOnWakeLock = (WakeLock)XposedHelpers.getObjectField(param.getThisObject(), "mScreenOnWakeLock");

                if (mContext != null && mHandler != null && mScreenOffRunnable != null && mScreenOnWakeLock != null) {
                    if (mScreenOnWakeLock.isHeld()) {
                        int timeout = MainModule.mPrefs.getInt("system_chargeanimtime", 20) * 1000;
                        mHandler.postDelayed(mScreenOnWakeLock::release, timeout);
                        mHandler.removeCallbacks(mScreenOffRunnable);
                        mHandler.postDelayed(mScreenOffRunnable, timeout);
                    }
                } else XposedHelpers.log("ChargeAnimationHook3", "Something is NULL! :)");
            }
        });
    }

    public static void VolumeStepsHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.audio.AudioService", lpparam.getClassLoader(), "createStreamStates", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) {
                Class<?> audioCls = findClass("com.android.server.audio.AudioService", lpparam.getClassLoader());
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

    public static void AutoBrightnessRangeHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.AutomaticBrightnessController", lpparam.getClassLoader(), "clampScreenBrightness", float.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                float val = (float)param.getResult();
                if (val >= 0) {
                    float res = constrainValue(val);
                    param.setResult(res);
                }
            }
        });

        ModuleHelper.hookAllConstructors("com.android.server.display.AutomaticBrightnessController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
            XposedHelpers.setLongField(param.getThisObject(), "mBrighteningLightDebounceConfig", 1000L);
            XposedHelpers.setLongField(param.getThisObject(), "mDarkeningLightDebounceConfig", 1200L);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.getClassLoader(), "clampScreenBrightness", float.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                float val = (float)param.getResult();
                if (val >= 0) {
                    float res = constrainValue(val);
                    param.setResult(res);
                }
            }
        });

        ModuleHelper.hookAllConstructors("com.android.server.display.DisplayPowerController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Resources res = Resources.getSystem();
                int minBrightnessLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"));
                int maxBrightnessLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android"));
                int backlightBit = res.getInteger(res.getIdentifier("config_backlightBit", "integer", "android.miui"));
                backlightMaxLevel = (1 << backlightBit) - 1;
                mMinimumBacklight = (minBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1);
                mMaximumBacklight = (maxBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1);
            }
        });
    }

    public static void BetterPopupsHideDelayHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethodSilently(MiuiNotification.class, "getFloatTime", HookerClassHelper.returnConstant(0));
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int delay = MainModule.mPrefs.getInt("system_betterpopups_delay", 0) * 1000;
                if (delay == 0) delay = 5000;
                XposedHelpers.setIntField(param.getThisObject(), "mMinimumDisplayTime", delay);
                XposedHelpers.setIntField(param.getThisObject(), "mHeadsUpNotificationDecay", delay);
                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
                    public void onChange(String key) {
                        if (key.contains("system_betterpopups_delay")) {
                            int delay = MainModule.mPrefs.getInt("system_betterpopups_delay", 0) * 1000;
                            if (delay == 0) delay = 5000;
                            XposedHelpers.setIntField(param.getThisObject(), "mMinimumDisplayTime", delay);
                            XposedHelpers.setIntField(param.getThisObject(), "mHeadsUpNotificationDecay", delay);
                        }
                    }
                });
            }
        });
    }

    public static void BetterPopupsNoHideHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "removeHeadsUpNotification", HookerClassHelper.DO_NOTHING);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "removeOldHeadsUpNotification", HookerClassHelper.DO_NOTHING);

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager$HeadsUpEntry", lpparam.getClassLoader(), "updateEntry", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                XposedHelpers.setObjectField(param.getThisObject(), "mRemoveHeadsUpRunnable", new Runnable() {
                    @Override
                    public void run() {}
                });
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "onExpandingFinished", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                XposedHelpers.setBooleanField(param.getThisObject(), "mReleaseOnExpandFinish", true);
            }
        });

//		ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "onReorderingAllowed", new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				XposedHelpers.log("BetterPopupsNoHideHook", "onReorderingAllowed");
//			}
//		});
    }

    public static void BetterPopupsSwipeDownHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.policy.AppMiniWindowManager", lpparam.getClassLoader(), "canNotificationSlide", String.class, PendingIntent.class, HookerClassHelper.returnConstant(false));
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

    public static void RotationAnimationHook(SystemServerLoadedParam lpparam) {
        MethodHook animEnter = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_rotateanim", "1"));
                Animation anim = (Animation)param.getResult();
                if (opt == 2) {
                    anim.setDuration(0);
                    param.setResult(anim);
                } else if (opt == 3) {
                    Animation alphaAnim = new AlphaAnimation(1.0f, 1.0f);
                    alphaAnim.setInterpolator((Interpolator)XposedHelpers.getStaticObjectField(findClass("com.android.server.wm.AppTransitionInjector", lpparam.getClassLoader()), "QUART_EASE_OUT_INTERPOLATOR"));
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
            protected void after(final AfterHookCallback param) throws Throwable {
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_rotateanim", "1"));
                Animation anim = (Animation)param.getResult();
                if (opt == 2) {
                    anim.setDuration(0);
                } else if (opt == 3) {
                    AlphaAnimation alphaAnim = new AlphaAnimation(1.0f, 0.0f);
                    alphaAnim.setInterpolator((Interpolator)XposedHelpers.getStaticObjectField(findClass("com.android.server.wm.AppTransitionInjector", lpparam.getClassLoader()), "QUART_EASE_OUT_INTERPOLATOR"));
                    alphaAnim.setDuration(300);
                    alphaAnim.setFillAfter(true);
                    alphaAnim.setFillBefore(true);
                    alphaAnim.setFillEnabled(true);
                    param.setResult(alphaAnim);
                }
            }
        };

        ModuleHelper.findAndHookMethod("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.getClassLoader(), "createRotation180Enter", animEnter);
        ModuleHelper.findAndHookMethod("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.getClassLoader(), "createRotation180Exit", animExit);
        ModuleHelper.hookAllMethods("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.getClassLoader(), "createRotationEnter", animEnter);
        ModuleHelper.hookAllMethods("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.getClassLoader(), "createRotationEnterWithBackColor", animEnter);
        ModuleHelper.hookAllMethods("com.android.server.wm.ScreenRotationAnimationImpl", lpparam.getClassLoader(), "createRotationExit", animExit);
    }

    public static void NoVersionCheckHook(SystemServerLoadedParam lpparam) {
        String PMSCls = "com.android.server.pm.PackageManagerServiceUtils";
        ModuleHelper.hookAllMethods(PMSCls, lpparam.getClassLoader(), "checkDowngrade", HookerClassHelper.DO_NOTHING);
    }

    public static void ColorizeNotificationCardHook(PackageLoadedParam lpparam) {
        Class<?> ColorScheme = findClassIfExists("com.android.systemui.monet.ColorScheme", lpparam.getClassLoader());
        Object contentStyle = null;
        Class<?> MonetStyle = findClassIfExists("com.android.systemui.monet.Style", lpparam.getClassLoader());
        Object[] styles = MonetStyle.getEnumConstants();
        for (Object o:styles) {
//                if (o.toString().contains("VIBRANT")) {
//                if (o.toString().contains("TONAL_SPOT")) {
            if (o.toString().contains("CONTENT")) {
                contentStyle = o;
                break;
            }
        }
        Object finalContentStyle = contentStyle;

        ModuleHelper.findAndHookConstructor("android.app.Notification$Builder", lpparam.getClassLoader(), Context.class, Notification.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs()[1] != null) {
                    Notification mN = (Notification) param.getArgs()[1];
                    if (XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") != null) {
                        Object builder = param.getThisObject();
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

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), "updateNotificationColor", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object mEntry = XposedHelpers.getObjectField(param.getThisObject(), "mEntry");
                Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
                Notification notify = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
                Object overflowColor = XposedHelpers.getAdditionalInstanceField(notify, "mSecondaryTextColor");
                if (overflowColor != null) {
                    XposedHelpers.setObjectField(param.getThisObject(), "mNotificationColor", overflowColor);
                    param.returnAndSkip(null);
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), "onNotificationUpdated", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mEntry = XposedHelpers.getObjectField(param.getThisObject(), "mEntry");
                if (mEntry != null) {
                    Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
                    Notification notify = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
                    Object mNotifyBackgroundColor = XposedHelpers.getAdditionalInstanceField(notify, "mNotifyBackgroundColor");
                    if (mNotifyBackgroundColor != null) {
                        int bgColor = (int) mNotifyBackgroundColor;
                        int mCurrentBackgroundTint = XposedHelpers.getIntField(param.getThisObject(), "mCurrentBackgroundTint");
                        if (mCurrentBackgroundTint != bgColor) {
                            bgColor = Color.argb(158, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor));
                            XposedHelpers.callMethod(param.getThisObject(), "setBackgroundTintColor", bgColor);
                        }
                    }
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationBackgroundView", lpparam.getClassLoader(), "setTint", int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if ((int)param.getArgs()[0] == 0) {
                    param.returnAndSkip(null);
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper", lpparam.getClassLoader(), "getCustomBackgroundColor", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(XposedHelpers.getObjectField(param.getThisObject(), "mBackgroundColor"));
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.HybridGroupManager", lpparam.getClassLoader(), "bindFromNotificationWithStyle", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Notification mN = (Notification) XposedHelpers.callMethod(param.getArgs()[2], "getNotification");
                if (XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor") != null) {
                    LinearLayout hybridNotificationView = (LinearLayout) param.getResult();
                    TextView mTitleView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTitleView");
                    TextView mTextView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTextView");
                    mTitleView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
                    mTextView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
                }
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "handle3thThemeColor", new MethodHook() {
            private Object sAppIconManager = null;
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Notification.Builder builder = (Notification.Builder) param.getArgs()[1];
                Notification mN = (Notification) XposedHelpers.getObjectField(builder, "mN");
                if ((boolean)XposedHelpers.callMethod(mN, "isColorized")) return;
                if ((boolean)XposedHelpers.callMethod(mN, "isMediaNotification")) return;
                ApplicationInfo applicationInfo = mN.extras.getParcelable("android.appInfo");
                if (applicationInfo == null) {
                    return;
                }
                Context mContext = (Context) param.getArgs()[0];
                String pkgName = applicationInfo.packageName;
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_colorizenotifs", "1"));
                boolean isSelected = MainModule.mPrefs.getStringSet("system_colorizenotifs_apps").contains(pkgName);
                if (opt == 2 && !isSelected || opt == 3 && isSelected) {
                    XposedHelpers.callMethod(builder, "makeNotificationGroupHeader");
                    if (sAppIconManager == null) {
                        Class<?> Dependency = findClassIfExists("com.android.systemui.Dependency", lpparam.getClassLoader());
                        Class<?> AppIconManager = findClassIfExists("com.miui.systemui.graphics.AppIconsManager", lpparam.getClassLoader());
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
                    cs = XposedHelpers.newInstance(ColorScheme, primaryColor, dark, finalContentStyle);
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
                    param.returnAndSkip(null);
                }
            }
        });

        MethodHook textColorHook = new MethodHook() {
            private int titleResId = 0;
            private int subTextResId = 0;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                RemoteViews baseContent = (RemoteViews) param.getResult();
                if (baseContent != null) {
                    Context mContext = (Context) param.getArgs()[param.getArgs().length - 1];
                    if (titleResId == 0) {
                        titleResId = mContext.getResources().getIdentifier("title", "id", "com.android.systemui");
                        subTextResId = mContext.getResources().getIdentifier("text", "id", "com.android.systemui");
                    }
                    Notification.Builder builder = (Notification.Builder) param.getArgs()[0];
                    Notification mN = (Notification) XposedHelpers.getObjectField(builder, "mN");
                    if ((boolean)XposedHelpers.callMethod(mN, "isMediaNotification")) return;
                    if (XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") != null) {
                        baseContent.setTextColor(titleResId, (int)XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
                        baseContent.setTextColor(subTextResId, (int)XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
                    }
                }
            }
        };

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "createMiuiContentView", textColorHook);
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "createMiuiExpandedView", textColorHook);
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "createMiuiPublicView", textColorHook);
    }

    public static int abHeight = 39;

    public static void CompactNotificationsHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper", lpparam.getClassLoader(), "wrap", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getArgs().length > 3) return;
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
//            protected void after(final AfterHookCallback param) throws Throwable {
//                View view = (View)param.getArgs()[0];
//                FrameLayout container = view.findViewById(view.getResources().getIdentifier("actions_container", "id", "android"));
//                if (container == null || container.getVisibility() != View.VISIBLE) return;
//                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
//                if ("MiuiStyleProcessor".equals(param.getThisObject().getClass().getSimpleName())) {
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
//        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$GoogleStyleProcessor", lpparam.getClassLoader(), "setGoogleContentMargins", View.class, hook);
//        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper$MiuiStyleProcessor", lpparam.getClassLoader(), "setMiuiContentMargins", View.class, hook);
    }

    private static final List<String> hookedTiles = new ArrayList<String>();

    @SuppressLint("MissingPermission")
    public static void QSHapticHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSFactoryImpl", lpparam.getClassLoader(), "createTileInternal", String.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object tile = param.getResult();
                if (tile == null) return;
                String tileClass = tile.getClass().getCanonicalName();
                if (!hookedTiles.contains(tileClass)) {
                    ModuleHelper.hookAllMethods(tileClass, lpparam.getClassLoader(), "handleClick", new MethodHook() {
                        @Override
                        protected void after(final AfterHookCallback param) throws Throwable {
                            Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                            boolean ignoreSystem = MainModule.mPrefs.getBoolean("system_qshaptics_ignore");
                            int opt = MainModule.mPrefs.getStringAsInt("system_qshaptics", 1);
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

    public static void HideCCOperatorHook(PackageLoadedParam lpparam) {
        MethodHook hideOperatorHook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object carrierView;
                TextView mCarrierText;
                try {
                    carrierView = XposedHelpers.getObjectField(param.getThisObject(), "carrierText");
                }
                catch (Throwable e) {
                    carrierView = XposedHelpers.getObjectField(param.getThisObject(), "mCarrierText");
                }
                mCarrierText = (TextView) carrierView;
                mCarrierText.setVisibility(View.GONE);
            }
        };

        boolean hookedFlaresInfo = ModuleHelper.hookAllMethodsSilently("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", lpparam.getClassLoader(), "updateFlaresInfo", hideOperatorHook);
        if (!hookedFlaresInfo) {
            ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.phone.widget.ControlCenterStatusBar", lpparam.getClassLoader(), "onFinishInflate", hideOperatorHook);
        }

        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateCarrierTextVisibility", hideOperatorHook);

        hookedFlaresInfo = ModuleHelper.findAndHookMethodSilently("com.android.systemui.qs.MiuiQSHeaderView", lpparam.getClassLoader(), "updateCarrierVisibility", hideOperatorHook);
        if (!hookedFlaresInfo) {
            ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.getClassLoader(), "onFinishInflate", hideOperatorHook);
        }
    }

    public static void HideCCOperatorDelimiterHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiCarrierTextController", lpparam.getClassLoader(), "fireCarrierTextChanged", String.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String mCurrentCarrier = (String) param.getArgs()[0];
                param.getArgs()[0] = mCurrentCarrier.replace(" | ", "");
            }
        });
    }

    public static void CollapseCCAfterClickHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.getClassLoader(), "click", View.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mState = XposedHelpers.callMethod(param.getThisObject(), "getState");
                int state = XposedHelpers.getIntField(mState, "state");
                if (state != 0) {
                    String tileSpec = (String) XposedHelpers.callMethod(param.getThisObject(), "getTileSpec");
                    if (!"edit".equals(tileSpec)) {
                        Object mHost = XposedHelpers.getObjectField(param.getThisObject(), "mHost");
                        XposedHelpers.callMethod(mHost, "collapsePanels");
                    }
                }
            }
        });
    }

    public static void DisableBluetoothRestrictHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.settingslib.bluetooth.LocalBluetoothAdapter", lpparam.getClassLoader(), "isSupportBluetoothRestrict", Context.class, HookerClassHelper.returnConstant(false));
    }

    public static void AutoGroupNotificationsHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.getClassLoader(), "adjustAutogroupingSummary", int.class, String.class, String.class, boolean.class, new MethodHook() {
            @Override
            @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
            protected void before(final BeforeHookCallback param) throws Throwable {
            int opt = Integer.parseInt(MainModule.mPrefs.getString("system_autogroupnotif", "1"));
            if (opt == 2) {
                param.returnAndSkip(null);
                return;
            }
            Map<Integer, Map<String, LinkedHashSet<String>>> mUngroupedNotifications = (Map<Integer, Map<String, LinkedHashSet<String>>>)XposedHelpers.getObjectField(param.getThisObject(), "mUngroupedNotifications");
            Map<String, LinkedHashSet<String>> obj = mUngroupedNotifications.get(param.getArgs()[0]);
            if (obj != null) {
                LinkedHashSet<String> list = obj.get(param.getArgs()[1]);
                if (list != null && list.size() < opt) param.returnAndSkip(null);
            }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.getClassLoader(), "adjustNotificationBundling", List.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
            List<?> list = (List<?>)param.getArgs()[0];
            int opt = Integer.parseInt(MainModule.mPrefs.getString("system_autogroupnotif", "1"));
            if (opt == 2 || (list != null && list.size() < opt)) param.returnAndSkip(null);
            }
        });
    }

    public static void NoMoreIconHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.getClassLoader(), "setIconsVisibility", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object mMoreIcon = XposedHelpers.getObjectField(param.getThisObject(), "mMoreIcon");
                if (mMoreIcon != null) XposedHelpers.setBooleanField(param.getThisObject(), "mForceHideMoreIcon", true);
            }
        });
    }

    public static void ShowNotificationsAfterUnlockHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.getClassLoader(), "hasShownAfterUnlock", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.getClassLoader(), "setHasShownAfterUnlock", boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setBooleanField(param.getThisObject(), "mHasShownAfterUnlock", false);
            }
        });
        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.notification.MiuiNotificationCompat", lpparam.getClassLoader(), "isKeptOnKeyguard", Notification.class, HookerClassHelper.returnConstant(true));
    }

    public static void NotificationRowMenuHook(PackageLoadedParam lpparam) {
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

        Class<?> MiuiNotificationMenuItem = findClass("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow.MiuiNotificationMenuItem", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.getClassLoader(), "createMenuViews", boolean.class, boolean.class, new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                ArrayList<Object> mMenuItems = (ArrayList<Object>)XposedHelpers.getObjectField(param.getThisObject(), "mMenuItems");

                Object infoBtn = null;
                Object forceCloseBtn = null;
                Object openFwBtn = null;
                Constructor MenuItem = MiuiNotificationMenuItem.getConstructors()[0];
                try {
                    infoBtn = MenuItem.newInstance(param.getThisObject(), mContext, appInfoDescId, null, appInfoIconResId);
                    forceCloseBtn = MenuItem.newInstance(param.getThisObject(), mContext, forceCloseDescId, null, forceCloseIconResId);
                    openFwBtn = MenuItem.newInstance(param.getThisObject(), mContext, openInFwDescId, null, openInFwIconResId);
                } catch (Throwable t1) {
                    XposedHelpers.log(t1);
                }
                if (infoBtn == null || forceCloseBtn == null || openFwBtn == null) return;
                Object notification = XposedHelpers.getObjectField(param.getThisObject(), "mSbn");
                Object expandNotifyRow = XposedHelpers.getObjectField(param.getThisObject(), "mParent");
                mMenuItems.add(infoBtn);
                mMenuItems.add(forceCloseBtn);
                mMenuItems.add(openFwBtn);
                XposedHelpers.setObjectField(param.getThisObject(), "mMenuItems", mMenuItems);
                int menuMargin = (int) XposedHelpers.getObjectField(param.getThisObject(), "mMenuMargin");
                LinearLayout mMenuContainer = (LinearLayout)XposedHelpers.getObjectField(param.getThisObject(), "mMenuContainer");
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
                            XposedHelpers.log(t);
                        }

                        if (view == mInfoBtn) {
                            ModuleHelper.openAppInfo(mContext, pkgName, user);
                            mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                        } else if (view == mForceCloseBtn) {
                            ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
                            if (user != 0)
                                XposedHelpers.callMethod(am, "forceStopPackageAsUser", pkgName, user);
                            else
                                XposedHelpers.callMethod(am, "forceStopPackage", pkgName);
                            try {
                                CharSequence appName = mContext.getPackageManager().getApplicationLabel(mContext.getPackageManager().getApplicationInfo(pkgName, 0));
                                Toast.makeText(mContext, ModuleHelper.getModuleRes(mContext).getString(R.string.force_closed, appName), Toast.LENGTH_SHORT).show();
                            } catch (Throwable ignore) {}
                        }
                        else if (view == mOpenFwBtn) {
                            Class<?> Dependency = findClass("com.android.systemui.Dependency", lpparam.getClassLoader());
                            Object AppMiniWindowManager = XposedHelpers.callStaticMethod(Dependency, "get", findClassIfExists("com.android.systemui.statusbar.notification.policy.AppMiniWindowManager", lpparam.getClassLoader()));
                            String miniWindowPkg = (String) XposedHelpers.callMethod(expandNotifyRow, "getMiniWindowTargetPkg");
                            PendingIntent notifyIntent = (PendingIntent) XposedHelpers.callMethod(expandNotifyRow, "getPendingIntent");
                            String ModalControllerForDep = "com.android.systemui.statusbar.notification.modal.ModalController";
                            Object ModalController = XposedHelpers.callStaticMethod(Dependency, "get", findClass(ModalControllerForDep, lpparam.getClassLoader()));
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
                int titleId = mContext.getResources().getIdentifier("modal_menu_title", "id", lpparam.getPackageName());
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
            XposedHelpers.log(t);
            return false;
        }
    }

    public static void SelectiveVibrationHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.vibrator.VibratorManagerService", lpparam.getClassLoader(), "systemReady", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mVibrationMode", Integer.parseInt(MainModule.mPrefs.getString("system_vibration", "1")));
                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
                    public void onChange(String key) {
                        if (key.endsWith("system_vibration")) {
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mVibrationMode", MainModule.mPrefs.getStringAsInt("system_vibration", 1));
                        }
                    }
                });

                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mVibrationApps", MainModule.mPrefs.getStringSet("system_vibration_apps"));
                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
                    public void onChange(String key) {
                        if (key.contains("system_vibration_apps")) {
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mVibrationApps", MainModule.mPrefs.getStringSet("system_vibration_apps"));
                        }
                    }
                });
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.vibrator.VibratorManagerService", lpparam.getClassLoader(), "vibrate", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String pkgName = (String)param.getArgs()[1];
                if (pkgName == null) return;
                if (checkVibration(pkgName, param.getThisObject())) param.returnAndSkip(null);
            }
        });
    }

    public static void NoDuckingHook(SystemServerLoadedParam lpparam) {
        //ModuleHelper.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor", lpparam.getClassLoader(), "duckPlayers", HookerClassHelper.returnConstant(true));
        //ModuleHelper.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor$DuckingManager", lpparam.getClassLoader(), "addDuck", HookerClassHelper.DO_NOTHING);
        ModuleHelper.hookAllMethods("com.android.server.audio.FocusRequester", lpparam.getClassLoader(), "handleFocusLoss", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if ((int)param.getArgs()[0] == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) param.returnAndSkip(null);
            }
        });
    }

    public static void OrientationLockHook(SystemServerLoadedParam lpparam) {
        String windowClass = "com.android.server.wm.DisplayRotation";
        String rotMethod = "rotationForOrientation";
        ModuleHelper.hookAllMethods(windowClass, lpparam.getClassLoader(), rotMethod, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                //Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                //XposedHelpers.log("rotationForOrientationLw: " + param.getArgs()[0] + ", " + param.getArgs()[1] + " = " + param.getResult());
                if ((int)param.getArgs()[0] == -1) {
                    int opt = MainModule.mPrefs.getInt("qs_autorotate_state", 0);
                    int prevOrient = (int)param.getArgs()[1];
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
    public static void HideMemoryCleanHook(PackageLoadedParam lpparam, boolean isInLauncher) {
        String raClass = isInLauncher ? "com.miui.home.recents.views.RecentsContainer" : "com.android.systemui.recents.RecentsActivity";
        if (isInLauncher && findClassIfExists(raClass, lpparam.getClassLoader()) == null) return;
        ModuleHelper.findAndHookMethod(raClass, lpparam.getClassLoader(), "setupVisible", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
            ViewGroup mMemoryAndClearContainer = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mMemoryAndClearContainer");
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

    public static void StatusBarBackgroundHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.getClassLoader(), "generateLayout", "com.android.internal.policy.DecorView", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Window wnd = (Window)param.getThisObject();
                if (isIgnored(wnd.getContext())) return;
                int mStatusBarColor = XposedHelpers.getIntField(param.getThisObject(), "mStatusBarColor");
                if (mStatusBarColor == -16777216) return;
                int newColor = getActionBarColor(wnd, mStatusBarColor);
                if (newColor != mStatusBarColor)
                    XposedHelpers.callMethod(param.getThisObject(), "setStatusBarColor", newColor);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.getClassLoader(), "setStatusBarColor", int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Window wnd = (Window)param.getThisObject();
                if (isIgnored(wnd.getContext())) return;
                if (actionBarColor != NOCOLOR) param.getArgs()[0] = actionBarColor;
                else if (Color.alpha((int)param.getArgs()[0]) < 255) param.getArgs()[0] = Color.TRANSPARENT;
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.app.ToolbarActionBar", lpparam.getClassLoader(), "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                hookToolbar(param.getThisObject(), (Drawable)param.getArgs()[0]);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.app.WindowDecorActionBar", lpparam.getClassLoader(), "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                hookWindowDecor(param.getThisObject(), (Drawable)param.getArgs()[0]);
            }
        });
    }

    public static void StatusBarBackgroundCompatHook(PackageLoadedParam lpparam) {
        boolean androidx = false;

        // androidx
        Method sbdMethod = null;
        Class<?> tabCls = findClassIfExists("androidx.appcompat.app.ToolbarActionBar", lpparam.getClassLoader());
        if (tabCls != null) sbdMethod = findMethodExactIfExists(tabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    hookToolbar(param.getThisObject(), (Drawable)param.getArgs()[0]);
                }
            });

        sbdMethod = null;
        Class<?> wdabCls = findClassIfExists("androidx.appcompat.app.WindowDecorActionBar", lpparam.getClassLoader());
        if (wdabCls != null) sbdMethod = findMethodExactIfExists(wdabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    hookWindowDecor(param.getThisObject(), (Drawable)param.getArgs()[0]);
                }
            });

        // old appcompat lib
        if (!androidx) {
            sbdMethod = null;
            Class<?> tabv7Cls = findClassIfExists("android.support.v7.internal.app.ToolbarActionBar", lpparam.getClassLoader());
            if (tabv7Cls != null) sbdMethod = findMethodExactIfExists(tabv7Cls, "setBackgroundDrawable", Drawable.class);
            if (sbdMethod != null)
                ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                    @Override
                    protected void before(final BeforeHookCallback param) throws Throwable {
                        hookToolbar(param.getThisObject(), (Drawable)param.getArgs()[0]);
                    }
                });

            sbdMethod = null;
            Class<?> wdabv7Cls = findClassIfExists("android.support.v7.internal.app.WindowDecorActionBar", lpparam.getClassLoader());
            if (wdabv7Cls != null) sbdMethod = findMethodExactIfExists(wdabv7Cls, "setBackgroundDrawable", Drawable.class);
            if (sbdMethod != null)
                ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                    @Override
                    protected void before(final BeforeHookCallback param) throws Throwable {
                        hookWindowDecor(param.getThisObject(), (Drawable)param.getArgs()[0]);
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
            XposedHelpers.log(t);
            return false;
        }
    }

    public static void SelectiveToastsHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.notification.NotificationManagerService", lpparam.getClassLoader(), "tryShowToast", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String pkgName = (String) XposedHelpers.getObjectField(param.getArgs()[0], "pkg");
                if (pkgName == null) return;
                if (checkToast(pkgName)) param.returnAndSkip(false);
            }
        });
    }

    public static void CleanShareMenuHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", lpparam.getClassLoader(), "run", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Intent mOriginalIntent = (Intent)XposedHelpers.getObjectField(param.getThisObject(), "mOriginalIntent");
                if (mOriginalIntent == null) return;
                String action = mOriginalIntent.getAction();
                if (action == null) return;
                if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) return;
                if (mOriginalIntent.getDataString() != null && mOriginalIntent.getDataString().contains(":")) return;

                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                String mAimPackageName = (String)XposedHelpers.getObjectField(param.getThisObject(), "mAimPackageName");
                if (mContext == null || mAimPackageName == null) return;
                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps");
                View mRootView = (View)XposedHelpers.getObjectField(param.getThisObject(), "mRootView");
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

    public static void CleanShareMenuServiceHook(SystemServerLoadedParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                try {
                    if (param.getArgs()[0] == null) return;
                    if (param.getArgs().length < 6) return;
                    Intent origIntent = (Intent)param.getArgs()[0];
                    String action = origIntent.getAction();
                    if (action == null) return;
                    if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) return;
                    Intent intent = (Intent)origIntent.clone();
                    if (intent.getDataString() != null && intent.getDataString().contains(":")) return;
                    if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) return;
                    Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps");
                    List<ResolveInfo> resolved = (List<ResolveInfo>)param.getResult();
                    ResolveInfo resolveInfo;
                    Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
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
                    if (!(t instanceof BadParcelableException)) XposedHelpers.log(t);
                }
            }
        };

        String ActQueryService = "com.android.server.pm.ComputerEngine";
        ModuleHelper.hookAllMethods(ActQueryService, lpparam.getClassLoader(), "queryIntentActivitiesInternal", hook);
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
            mimeFlags0 = MainModule.mPrefs.getInt(key + "_" + pkgName + "|0", MimeType.ALL);
            mimeFlags999 = MainModule.mPrefs.getInt(key + "_" + pkgName + "|999", MimeType.ALL);
        } else {
            mimeFlags0 = MainModule.mPrefs.getInt(key + "_" + pkgName + "|0", MimeType.ALL);
            mimeFlags999 = MainModule.mPrefs.getInt(key + "_" + pkgName + "|999", MimeType.ALL);
        }
        boolean removeOriginal = (selectedApps.contains(pkgName) || selectedApps.contains(pkgName + "|0")) && hideMimeType(mimeFlags0, mimeType);
        boolean removeDual = selectedApps.contains(pkgName + "|999") && hideMimeType(mimeFlags999, mimeType);
        return new Pair<Boolean, Boolean>(removeOriginal, removeDual);
    }

    public static void CleanOpenWithMenuHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", lpparam.getClassLoader(), "run", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Intent mOriginalIntent = (Intent)XposedHelpers.getObjectField(param.getThisObject(), "mOriginalIntent");
                if (mOriginalIntent == null) return;
                String action = mOriginalIntent.getAction();
                if (!Intent.ACTION_VIEW.equals(action)) return;
                //if (mOriginalIntent.getDataString() != null && mOriginalIntent.getDataString().contains(":")) return;

                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                String mAimPackageName = (String)XposedHelpers.getObjectField(param.getThisObject(), "mAimPackageName");
                if (mContext == null || mAimPackageName == null) return;
                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanopenwith_apps");
                String mimeType = getContentType(mContext, mOriginalIntent);
                Pair<Boolean, Boolean> isRemove = isRemoveApp(true, mContext, mAimPackageName, selectedApps, mimeType);

                View mRootView = (View)XposedHelpers.getObjectField(param.getThisObject(), "mRootView");
                int appResId1 = mContext.getResources().getIdentifier("app1", "id", "android.miui");
                int appResId2 = mContext.getResources().getIdentifier("app2", "id", "android.miui");
                View originalApp = mRootView.findViewById(appResId1);
                View dualApp = mRootView.findViewById(appResId2);
                if (isRemove.first) dualApp.performClick();
                else if (isRemove.second) originalApp.performClick();
            }
        });
    }

    public static void CleanOpenWithMenuServiceHook(SystemServerLoadedParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(final AfterHookCallback param) throws Throwable {
                try {
                    if (param.getArgs()[0] == null) return;
                    if (param.getArgs().length < 6) return;
                    Intent origIntent = (Intent)param.getArgs()[0];
                    Intent intent = (Intent)origIntent.clone();
                    String action = intent.getAction();
                    //XposedHelpers.log(action + ": " + intent.getType() + " | " + intent.getDataString());
                    if (!Intent.ACTION_VIEW.equals(action)) return;
                    if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) return;
                    String scheme = intent.getScheme();
                    boolean validSchemes = "http".equals(scheme) || "https".equals(scheme) || "vnd.youtube".equals(scheme);
                    if (intent.getType() == null && !validSchemes) return;

                    Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                    String mimeType = getContentType(mContext, intent);
                    //XposedHelpers.log("mimeType: " + mimeType);

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
                    if (!(t instanceof BadParcelableException)) XposedHelpers.log(t);
                }
            }
        };

        String ActQueryService = "com.android.server.pm.ComputerEngine";
        ModuleHelper.hookAllMethods(ActQueryService, lpparam.getClassLoader(), "queryIntentActivitiesInternal", hook);
    }

    public static void AppLockHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "removeAccessControlPassLocked", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (!"*".equals(param.getArgs()[1])) return;
                int mode = (int)XposedHelpers.callMethod(param.getThisObject(), "getAccessControlLockMode", param.getArgs()[0]);
                if (mode != 1) param.returnAndSkip(null);
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

    public static void AppLockTimeoutHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "addAccessControlPassForUser", String.class, int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                saveLastCheck(param.getThisObject(), (String)param.getArgs()[0], (int)param.getArgs()[1]);
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                checkLastCheck(param.getThisObject(), (int)param.getArgs()[1]);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "checkAccessControlPassLocked", String.class, Intent.class, int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                saveLastCheck(param.getThisObject(), (String)param.getArgs()[0], (int)param.getArgs()[2]);
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                checkLastCheck(param.getThisObject(), (int)param.getArgs()[2]);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "activityResume", Intent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Intent intent = (Intent)param.getArgs()[0];
                if (intent.getComponent() != null)
                    saveLastCheck(param.getThisObject(), intent.getComponent().getPackageName(), 0);
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Intent intent = (Intent)param.getArgs()[0];
                if (intent.getComponent() != null)
                    checkLastCheck(param.getThisObject(), 0);
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
    public static void AudioVisualizerHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods(StatusBarCls, lpparam.getClassLoader(), "makeStatusBarView", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object viewController = XposedHelpers.getObjectField(param.getThisObject(), "mNotificationPanelViewController");
                FrameLayout mNotificationPanel = (FrameLayout) XposedHelpers.getObjectField(viewController, "mView");
                if (mNotificationPanel == null) {
                    XposedHelpers.log("AudioVisualizerHook", "Cannot find mNotificationPanel");
                    return;
                }

                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                ViewGroup visFrame = new FrameLayout(mContext);
                visFrame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                audioViz = new AudioVisualizer(mContext);
                audioViz.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM));
                audioViz.setClickable(false);
                visFrame.addView(audioViz);
                visFrame.setClickable(false);
                View wallpaper = mNotificationPanel.findViewById(mContext.getResources().getIdentifier("wallpaper", "id", lpparam.getPackageName()));
                View themebkg = mNotificationPanel.findViewById(mContext.getResources().getIdentifier("theme_background", "id", lpparam.getPackageName()));
                View awesome = mNotificationPanel.findViewById(mContext.getResources().getIdentifier("awesome_lock_screen_container", "id", lpparam.getPackageName()));

                int order = 0;
                if (awesome != null) order = Math.max(order, mNotificationPanel.indexOfChild(awesome));
                if (themebkg != null) order = Math.max(order, mNotificationPanel.indexOfChild(themebkg));
                if (wallpaper != null) order = Math.max(order, mNotificationPanel.indexOfChild(wallpaper));
                mNotificationPanel.addView(visFrame, order + 1);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.ScrimController", lpparam.getClassLoader(), "onScreenTurnedOff", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (audioViz != null) audioViz.updateScreenOn(false);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.ScrimController", lpparam.getClassLoader(), "onScreenTurnedOn", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (audioViz != null) audioViz.updateScreenOn(true);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.KeyguardStateControllerImpl", lpparam.getClassLoader(), "notifyKeyguardState", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean isKeyguardShowingNew = (boolean) param.getArgs()[0];
                if (isKeyguardShowing != isKeyguardShowingNew) {
                    isKeyguardShowing = isKeyguardShowingNew;
                    isNotificationPanelExpanded = false;
                    updateAudioVisualizerState((Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext"));
                }
            }
        });

        ModuleHelper.hookAllMethods(StatusBarCls, lpparam.getClassLoader(), "setPanelExpanded", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean isNotificationPanelExpandedNew = XposedHelpers.getBooleanField(param.getThisObject(), "mPanelExpanded");
                if (isNotificationPanelExpanded != isNotificationPanelExpandedNew) {
                    isNotificationPanelExpanded = isNotificationPanelExpandedNew;
                    updateAudioVisualizerState((Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext"));
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.getClassLoader(), "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (audioViz == null) return;
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                PowerManager powerMgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = powerMgr.isInteractive();
                if (!isScreenOn) {
                    audioViz.updateScreenOn(false);
                    return;
                } else audioViz.isScreenOn = true;

                MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(param.getThisObject(), "mMediaMetadata");
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

                mMediaController = (MediaController)XposedHelpers.getObjectField(param.getThisObject(), "mMediaController");
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

    public static void NoCallInterruptionHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.audio.AudioService", lpparam.getClassLoader(), "requestAudioFocus", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if ("AudioFocus_For_Phone_Ring_And_Calls".equals(param.getArgs()[4]) && audioFocusPkg != null && MainModule.mPrefs.getStringSet("system_ignorecalls_apps").contains(audioFocusPkg))
                    param.returnAndSkip(1);
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int res = (int)param.getResult();
                if (res != AudioManager.AUDIOFOCUS_REQUEST_FAILED && !"AudioFocus_For_Phone_Ring_And_Calls".equals(param.getArgs()[4]))
                    audioFocusPkg = (String)param.getArgs()[5];
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.getClassLoader(), "notifyCallState", int.class, String.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                removeListener(param.getThisObject());
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.getClassLoader(), "notifyCallStateForPhoneId", int.class, int.class, int.class, String.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                removeListener(param.getThisObject());
            }
        });
    }

    public static void AllRotationsHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.server.wm.DisplayRotation", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setIntField(param.getThisObject(), "mAllowAllRotations", MainModule.mPrefs.getStringAsInt("system_allrotations2", 1) == 2 ? 1 : 0);
            }
        });
    }

    private static boolean mUSBConnected = false;
    public static void USBConfigHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.getClassLoader(), "systemReady", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                if (mContext != null)
                    mContext.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            try {
                                boolean mConnected = intent.getBooleanExtra("connected", false);
                                if (mConnected && mConnected != mUSBConnected) try {
                                    int mPlugType = XposedHelpers.getIntField(param.getThisObject(), "mPlugType");
                                    if (mPlugType != BatteryManager.BATTERY_PLUGGED_USB) return;
                                    String func = MainModule.mPrefs.getString("system_defaultusb", "none");
                                    if ("none".equals(func) || "1".equals(func)) return;
                                    UsbManager usbMgr = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
                                    if ((boolean)XposedHelpers.callMethod(usbMgr, "isFunctionEnabled", func))
                                        return;
                                    XposedHelpers.callMethod(usbMgr, "setCurrentFunction", func, MainModule.mPrefs.getBoolean("system_defaultusb_unsecure"));
                                } catch (Throwable t) {
                                    XposedHelpers.log(t);
                                }
                                mUSBConnected = mConnected;
                            } catch (Throwable t) {
                                XposedHelpers.log(t);
                            }
                        }
                    }, new IntentFilter("android.hardware.usb.action.USB_STATE"));
            }
        });

        if (MainModule.mPrefs.getBoolean("system_defaultusb_unsecure")) {
            if (!ModuleHelper.findAndHookMethodSilently("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.getClassLoader(), "isUsbDataTransferActive", long.class, HookerClassHelper.returnConstant(true)))
                ModuleHelper.findAndHookMethod("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.getClassLoader(), "isUsbDataTransferActive", HookerClassHelper.returnConstant(true));
            ModuleHelper.findAndHookMethod("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.getClassLoader(), "handleMessage", Message.class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    Message msg = (Message)param.getArgs()[0];
                    int setUnlockedFunc = 12;
                    try {
                        setUnlockedFunc = XposedHelpers.getStaticIntField(findClass("com.android.server.usb.UsbDeviceManager", lpparam.getClassLoader()), "MSG_SET_SCREEN_UNLOCKED_FUNCTIONS");
                    } catch (Throwable t) {
                    }
                    if (msg.what == setUnlockedFunc) {
                        msg.obj = 0L;
                        param.getArgs()[0] = msg;
                    }
                }
            });
        }
    }

    public static void USBConfigSettingsHook(PackageLoadedParam lpparam) {
        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently("com.android.settings.connecteddevice.usb.UsbModeChooserReceiver", lpparam.getClassLoader(), "onReceive", Context.class, Intent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String func = MainModule.mPrefs.getString("system_defaultusb", "none");
                if (!"none".equals(func) && !"1".equals(func)) param.returnAndSkip(null);
            }
        });
    }

    public static void HideIconsBattery1Hook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "initMiuiView", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ImageView mBatteryIconView = (ImageView)XposedHelpers.getObjectField(param.getThisObject(), "mBatteryIconView");
                mBatteryIconView.setVisibility(View.GONE);
            }
        });
    }

    public static void HideIconsBattery2Hook(PackageLoadedParam lpparam) {
        boolean hideNormalPercentage = MainModule.mPrefs.getBoolean("system_statusbaricons_battery2");
        int batteryId = ResourceHooks.getFakeResId("batterview_in_statusbar");
        if (hideNormalPercentage) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    View mBatteryView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mBattery");
                    mBatteryView.setTag(batteryId, true);
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    View mBatteryView = (View) XposedHelpers.getObjectField(param.getThisObject(), "mBatteryView");
                    mBatteryView.setTag(batteryId, true);
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "updateChargeAndText", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery4")) {
                    TextView mBatteryPercentMarkView = (TextView)XposedHelpers.getObjectField(param.getThisObject(), "mBatteryPercentMarkView");
                    mBatteryPercentMarkView.setVisibility(View.GONE);
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery3")) {
                    ImageView mBatteryChargingView = (ImageView)XposedHelpers.getObjectField(param.getThisObject(), "mBatteryChargingView");
                    mBatteryChargingView.setVisibility(View.GONE);
                    try {
                        ImageView mBatteryChargingInView = (ImageView)XposedHelpers.getObjectField(param.getThisObject(), "mBatteryChargingInView");
                        mBatteryChargingInView.setVisibility(View.GONE);
                    } catch (Throwable ignore) {}
                }
                if (hideNormalPercentage) {
                    View mBatteryView = (View) param.getThisObject();
                    if (mBatteryView.getTag(batteryId) != null) {
                        View percentView = (View)XposedHelpers.getObjectField(param.getThisObject(), "mBatteryPercentMarkView");
                        percentView.setVisibility(View.GONE);
                        percentView = (View)XposedHelpers.getObjectField(param.getThisObject(), "mBatteryPercentView");
                        percentView.setVisibility(View.GONE);
                    }
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
                nextTime = ModuleHelper.getNextMIUIAlarmTime(mContext);
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
            XposedHelpers.log(t);
        }
    }

    public static void HideIconsSelectiveAlarmHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mNextAlarmTime", ModuleHelper.getNextMIUIAlarmTime(mContext));
                ContentResolver resolver = mContext.getContentResolver();
                ContentObserver alarmObserver = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        if (selfChange) return;
                        XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mNextAlarmTime", ModuleHelper.getNextMIUIAlarmTime(mContext));
                        updateAlarmVisibility(param.getThisObject(), lastState);
                    }
                };
                resolver.registerContentObserver(Settings.System.getUriFor("next_alarm_clock_formatted"), false, alarmObserver);

                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.TIME_TICK");
                filter.addAction("android.intent.action.TIME_SET");
                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                filter.addAction("android.intent.action.LOCALE_CHANGED");
                final Object thisObject = param.getThisObject();
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        updateAlarmVisibility(thisObject, lastState);
                    }
                }, filter);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.getClassLoader(), "updateAlarm", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                lastState = (boolean)XposedHelpers.getObjectField(param.getThisObject(), "mHasAlarm");
                updateAlarmVisibility(param.getThisObject(), lastState);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.getClassLoader(), "onMiuiAlarmChanged", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                lastState = (boolean)XposedHelpers.getObjectField(param.getThisObject(), "mHasAlarm");
                updateAlarmVisibility(param.getThisObject(), lastState);
                param.returnAndSkip(null);
            }
        });
    }

    public static void HideIconsBluetoothHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.getClassLoader(), "updateBluetooth", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int opt = Integer.parseInt(MainModule.mPrefs.getString("system_statusbaricons_bluetooth", "1"));
                boolean isBluetoothConnected = (boolean)XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mBluetooth"), "isBluetoothConnected");
                if (opt == 3 || (opt == 2 && !isBluetoothConnected)) {
                    Object mIconController = XposedHelpers.getObjectField(param.getThisObject(), "mIconController");
                    XposedHelpers.callMethod(mIconController, "setIconVisibility", "bluetooth", false);
                }
            }
        });
    }

    public static void DisplayWifiStandardHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.getClassLoader(), "applyWifiState", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object wifiState = param.getArgs()[0];
                if (wifiState != null) {
                    int opt = MainModule.mPrefs.getStringAsInt("system_statusbaricons_wifistandard", 1);
                    if (opt == 1) return;
                    int wifiStandard = (int) XposedHelpers.getObjectField(wifiState, "wifiStandard");
                    XposedHelpers.setObjectField(wifiState, "showWifiStandard", opt == 2 && wifiStandard > 0);
                }
            }
        });
    }

    public static void ForceCloseHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(final AfterHookCallback param) throws Throwable {
                HashSet<String> mSystemKeyPackages = (HashSet<String>)XposedHelpers.getObjectField(param.getThisObject(), "mSystemKeyPackages");
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

    public static void DisableAnyNotificationBlockHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "isBlockable", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "setBlockable", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = true;
            }
        });
    }
    public static void DisableAnyNotificationBlockHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "isBlockable", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "setBlockable", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = true;
            }
        });
    }

    public static void DisableAnyNotificationHook(PackageLoadedParam lpparam) {
        if (lpparam.getPackageName().contains("systemui")) {
            Class<?> NotifyManagerCls = findClass("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.getClassLoader());
            XposedHelpers.setStaticBooleanField(NotifyManagerCls, "USE_WHITE_LISTS", false);
            ModuleHelper.findAndHookMethod("com.miui.systemui.NotificationCloudData$Companion", lpparam.getClassLoader(), "getFloatBlacklist", Context.class, new MethodHook() {
                @Override
                protected void before(final BeforeHookCallback param) throws Throwable {
                    param.returnAndSkip(new ArrayList<String>());
                }
            });
        }
        ModuleHelper.hookAllMethods("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "isNotificationForcedEnabled", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "isNotificationForcedFor", Context.class, String.class, HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "canSystemNotificationBeBlocked", String.class, HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "containNonBlockableChannel", String.class, HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "getNotificationForcedEnabledList", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(new HashSet<String>());
            }
        });
    }

    public static void NotificationImportanceHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.settings.notification.BaseNotificationSettings", lpparam.getClassLoader(), "setPrefVisible", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object pref = param.getArgs()[0];
                if (pref != null) {
                    String prefKey = (String) XposedHelpers.callMethod(pref, "getKey");
                    if ("importance".equals(prefKey)) {
                        param.getArgs()[1] = true;
                    }
                }
            }
        });
        ModuleHelper.findAndHookMethod("com.android.settings.notification.ChannelNotificationSettings", lpparam.getClassLoader(), "setupChannelDefaultPrefs", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object pref = XposedHelpers.callMethod(param.getThisObject(), "findPreference", "importance");
                XposedHelpers.setObjectField(param.getThisObject(), "mImportance", pref);
                int mBackupImportance = (int)XposedHelpers.getObjectField(param.getThisObject(), "mBackupImportance");
                if (mBackupImportance > 0) {
                    int index = (int)XposedHelpers.callMethod(pref, "findSpinnerIndexOfValue", String.valueOf(mBackupImportance));
                    if (index > -1) {
                        XposedHelpers.callMethod(pref, "setValueIndex", index);
                    }
                    Class<?> ImportanceListener = findClassIfExists("androidx.preference.Preference$OnPreferenceChangeListener", lpparam.getClassLoader());
                    InvocationHandler handler = new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("onPreferenceChange")) {
                                int mBackupImportance = Integer.parseInt((String) args[1]);
                                XposedHelpers.setObjectField(param.getThisObject(), "mBackupImportance", mBackupImportance);
                                NotificationChannel mChannel = (NotificationChannel) XposedHelpers.getObjectField(param.getThisObject(), "mChannel");
                                mChannel.setImportance(mBackupImportance);
                                XposedHelpers.callMethod(mChannel, "lockFields", 4);
                                Object mBackend = XposedHelpers.getObjectField(param.getThisObject(), "mBackend");
                                String mPkg = (String) XposedHelpers.getObjectField(param.getThisObject(), "mPkg");
                                int mUid = (int) XposedHelpers.getObjectField(param.getThisObject(), "mUid");
                                XposedHelpers.callMethod(mBackend, "updateChannel", mPkg, mUid, mChannel);
                                XposedHelpers.callMethod(param.getThisObject(), "updateDependents", false);
                            }
                            return true;
                        }
                    };
                    Object mImportanceListener = Proxy.newProxyInstance(
                        lpparam.getClassLoader(),
                        new Class[] { ImportanceListener },
                        handler
                    );
                    XposedHelpers.callMethod(pref, "setOnPreferenceChangeListener", mImportanceListener);
                }
            }
        });
    }

    public static void MobileNetworkTypeHook(PackageLoadedParam lpparam) {
        String MobileController = "com.android.systemui.statusbar.connectivity.MobileSignalController";
        ModuleHelper.findAndHookMethod(MobileController, lpparam.getClassLoader(), "getMobileTypeName", int.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
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

    public static void HideProximityWarningHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.getClassLoader(), "showHint", HookerClassHelper.DO_NOTHING);
        ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.getClassLoader(), "prepareHintWindow", HookerClassHelper.DO_NOTHING);
    }

    public static void HideLockScreenClockHook(PackageLoadedParam lpparam) {
        MethodHook hideClockHook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                View mClockFrame = (View) XposedHelpers.getObjectField(param.getThisObject(), "mClockFrame");
                mClockFrame.setVisibility(View.INVISIBLE);
                mClockFrame = (View) XposedHelpers.getObjectField(param.getThisObject(), "mLargeClockFrame");
                mClockFrame.setVisibility(View.INVISIBLE);
            }
        };
        ModuleHelper.hookAllMethods("com.android.keyguard.KeyguardClockSwitch", lpparam.getClassLoader(), "setClockPlugin", hideClockHook);
        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardClockSwitch", lpparam.getClassLoader(), "updateClockViews", boolean.class, boolean.class, hideClockHook);
    }

    public static void FirstVolumePressHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.audio.AudioService$VolumeController", lpparam.getClassLoader(), "suppressAdjustment", int.class, int.class, boolean.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int streamType = (int)param.getArgs()[0];
                if (streamType != AudioManager.STREAM_MUSIC) return;
                boolean isMuteAdjust = (boolean)param.getArgs()[2];
                if (isMuteAdjust) return;
                Object mController = XposedHelpers.getObjectField(param.getThisObject(), "mController");
                if (mController == null) return;
                param.setResult(false);
            }
        });
    }

    public static void DisableSystemIntegrityHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("android.util.apk.ApkSignatureVerifier", lpparam.getClassLoader(), "getMinimumSignatureSchemeVersionForTargetSdk", int.class, HookerClassHelper.returnConstant(1));
    }

    public static void NoSignatureVerifyServiceHook(SystemServerLoadedParam lpparam) {
        Class <?> SignDetails = findClassIfExists("android.content.pm.SigningDetails", lpparam.getClassLoader());
        Object signUnknown = XposedHelpers.getStaticObjectField(SignDetails, "UNKNOWN");
        ModuleHelper.hookAllMethods(SignDetails, "checkCapability", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (param.getThisObject() == signUnknown || param.getArgs()[0] == signUnknown) {
                    param.returnAndSkip(false);
                    return;
                }
                int flags = (int) param.getArgs()[1];
                if (flags != 4) param.returnAndSkip(true);
            }
        });

        ModuleHelper.hookAllConstructors("android.util.jar.StrictJarVerifier", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                XposedHelpers.setObjectField(param.getThisObject(), "signatureSchemeRollbackProtectionsEnforced", false);
            }
        });
        ModuleHelper.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.getClassLoader(), "verifyMessageDigest", HookerClassHelper.returnConstant(true));
        ModuleHelper.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.getClassLoader(), "verify", HookerClassHelper.returnConstant(true));
        ModuleHelper.hookAllMethods("com.android.server.pm.PackageManagerServiceUtils", lpparam.getClassLoader(), "verifySignatures", HookerClassHelper.returnConstant(false));
        ModuleHelper.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.getClassLoader(), "doesSignatureMatchForPermissions", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String packageName = (String) XposedHelpers.callMethod(param.getArgs()[1], "getPackageName");
                String sourcePackageName = (String) param.getArgs()[0];
                if (sourcePackageName.equals(packageName)) {
                    param.returnAndSkip(true);
                }
            }
        });
        ModuleHelper.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.getClassLoader(), "cannotInstallWithBadPermissionGroups", HookerClassHelper.returnConstant(false));
        ModuleHelper.hookAllMethods("com.android.server.pm.permission.PermissionManagerServiceImpl", lpparam.getClassLoader(), "shouldGrantPermissionBySignature", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean isSystem = (boolean) XposedHelpers.callMethod(param.getArgs()[0], "isSystem");
                if (isSystem) {
                    param.returnAndSkip(true);
                }
            }
        });
    }

    public static void ScreenDimTimeHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.getClassLoader(), "readConfigurationLocked", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                float opt = MainModule.mPrefs.getInt("system_dimtime", 0) / 100f;
                XposedHelpers.setIntField(param.getThisObject(), "mMaximumScreenDimDurationConfig", 600000);
                XposedHelpers.setFloatField(param.getThisObject(), "mMaximumScreenDimRatioConfig", opt);
//				Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
//				int minScreenOffTimeout = mContext.getResources().getInteger(mContext.getResources().getIdentifier("config_minimumScreenOffTimeout", "integer", "android"));
            }
        });

//		ModuleHelper.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.getClassLoader(), "getScreenOffTimeoutLocked", new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				//XposedHelpers.log("getScreenOffTimeoutLocked (mSleepTimeoutSetting): " + param.getArgs()[0]);
//				//XposedHelpers.log("mMaximumScreenOffTimeoutFromDeviceAdmin: " + XposedHelpers.getLongField(param.getThisObject(), "mMaximumScreenOffTimeoutFromDeviceAdmin"));
//				XposedHelpers.log("mUserActivityTimeoutOverrideFromWindowManager: " + XposedHelpers.getLongField(param.getThisObject(), "mUserActivityTimeoutOverrideFromWindowManager"));
//			}
//		});
//
//		ModuleHelper.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.getClassLoader(), "updateUserActivitySummaryLocked", new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				if ((int)param.getArgs()[1] == 1) return;
//				XposedHelpers.log("updateUserActivitySummaryLocked: " + param.getArgs()[0] + ", " + param.getArgs()[1]);
//			}
//		});
    }

    public static void NoOverscrollAppHook(PackageLoadedParam lpparam) {
        MethodHook hookParam = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = false;
            }
        };

        Class<?> sblCls = findClassIfExists("miuix.springback.view.SpringBackLayout", lpparam.getClassLoader());
        if (sblCls != null) {
            ModuleHelper.hookAllConstructors(sblCls, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    try {
                        XposedHelpers.callMethod(param.getThisObject(), "setSpringBackEnable", false);
                    } catch (Throwable t) {
                        try { XposedHelpers.setBooleanField(param.getThisObject(), "mSpringBackEnable", false); } catch (Throwable ignore) {}
                    }
                }
            });
            //noinspection ResultOfMethodCallIgnored
            ModuleHelper.findAndHookMethodSilently(sblCls, "setSpringBackEnable", boolean.class, hookParam);
        }

        Class<?> rrvCls = findClassIfExists("androidx.recyclerview.widget.RemixRecyclerView", lpparam.getClassLoader());
        if (rrvCls != null) {
            ModuleHelper.hookAllConstructors(rrvCls, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    ((View)param.getThisObject()).setOverScrollMode(View.OVER_SCROLL_NEVER);
                    try {
                        XposedHelpers.callMethod(param.getThisObject(), "setSpringEnabled", false);
                    } catch (Throwable t) {
                        try { XposedHelpers.setBooleanField(param.getThisObject(), "mSpringEnabled", false); } catch (Throwable ignore) {}
                    }
                }
            });
            //noinspection ResultOfMethodCallIgnored
            ModuleHelper.findAndHookMethodSilently(rrvCls, "setSpringEnabled", boolean.class, hookParam);
        }

        ModuleHelper.findAndHookMethod("android.widget.AbsListView", lpparam.getClassLoader(), "initAbsListView", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                ((AbsListView)param.getThisObject()).setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        });
    }

    public static void RemoveSecureHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowState", lpparam.getClassLoader(), "isSecureLocked", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowSurfaceController", lpparam.getClassLoader(), "setSecure", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = false;
            }
        });
        ModuleHelper.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                int flags = (int) param.getArgs()[2];
                int secureFlag = 128;
                flags &= ~secureFlag;
                param.getArgs()[2] = flags;
            }
        });
    }

    public static void RemoveActStartConfirmHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethodsSilently("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "checkAllowStartActivity", HookerClassHelper.returnConstant(true));
    }

    public static void AllowAllKeyguardHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod(MiuiNotification.class, "setEnableKeyguard", boolean.class, new MethodHook() {
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = true;
            }
        });

        ModuleHelper.findAndHookMethod(MiuiNotification.class, "isEnableKeyguard", HookerClassHelper.returnConstant(true));
        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.notification.MiuiNotificationCompat", lpparam.getClassLoader(), "isEnableKeyguard", Notification.class, HookerClassHelper.returnConstant(true));
    }

    public static void AllowAllFloatHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod(MiuiNotification.class, "setEnableFloat", boolean.class, new MethodHook() {
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.getArgs()[0] = true;
            }
        });

        ModuleHelper.findAndHookMethod(MiuiNotification.class, "isEnableFloat", HookerClassHelper.returnConstant(true));

        //noinspection ResultOfMethodCallIgnored
        ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.notification.MiuiNotificationCompat", lpparam.getClassLoader(), "isEnableFloat", Notification.class, HookerClassHelper.returnConstant(true));
    }

    public static void AllowDirectReplyHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl", lpparam.getClassLoader(), "shouldAllowLockscreenRemoteInput", HookerClassHelper.returnConstant(true));
        if (!ModuleHelper.findAndHookMethodSilently("com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl", lpparam.getClassLoader(), "setLockscreenAllowRemoteInput", boolean.class, HookerClassHelper.returnConstant(true))) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl", lpparam.getClassLoader(), "setLockScreenAllowRemoteInput", boolean.class, HookerClassHelper.returnConstant(true));
        }
    }

    public static void HideQSHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.getClassLoader(), "onStateChanged", int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object mNotificationPanel = XposedHelpers.getObjectField(param.getThisObject(), "mQSContainer");
                if ((int)param.getArgs()[0] == 1) {
                    XposedHelpers.callMethod(mNotificationPanel, "setShowQSPanel", false);
                } else {
                    Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
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

    public static void LockScreenTimeoutHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.NotificationShadeWindowControllerImpl", lpparam.getClassLoader(), "applyUserActivityTimeout", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mLpChanged = XposedHelpers.getObjectField(param.getThisObject(), "mLpChanged");
                if (mLpChanged == null) return;
                long userActivityTimeout = XposedHelpers.getLongField(mLpChanged, "userActivityTimeout");
                if (userActivityTimeout > 0)
                    XposedHelpers.setLongField(mLpChanged, "userActivityTimeout", MainModule.mPrefs.getInt("system_lstimeout", 3) * 1000L);
            }
        });
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat("H:m", Locale.ENGLISH);
    public static void MuffledVibrationHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.VibratorService", lpparam.getClassLoader(), "doVibratorOn", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                float ratio_ringer = MainModule.mPrefs.getInt("system_vibration_amp_ringer", 100) / 100f;
                float ratio_notif = MainModule.mPrefs.getInt("system_vibration_amp_notif", 100) / 100f;
                float ratio_other = MainModule.mPrefs.getInt("system_vibration_amp_other", 100) / 100f;

                boolean isRingtone = false;
                boolean isNotification = false;
                Object mCurrentVibration = XposedHelpers.getObjectField(param.getThisObject(), "mCurrentVibration");
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
                    mSupportsAmplitudeControl = XposedHelpers.getBooleanField(param.getThisObject(), "mSupportsAmplitudeControl");
                } catch (Throwable ignored) {}

                if (mSupportsAmplitudeControl)
                    param.getArgs()[1] = Math.round(((int)param.getArgs()[1] == -1 ? XposedHelpers.getIntField(param.getThisObject(), "mDefaultVibrationAmplitude") : (int)param.getArgs()[1]) * ratio);
                else
                    param.getArgs()[0] = Math.max(3, (long)Math.round((long)param.getArgs()[0] * ratio));
            }
        });

//		if (Helpers.isNougat())
//		ModuleHelper.hookAllMethods("com.android.server.VibratorService", lpparam.getClassLoader(), "vibratePattern", new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//
//			}
//		});
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

            long timestamp = ModuleHelper.getNextMIUIAlarmTime(mContext);
            if (timestamp == 0 && MainModule.mPrefs.getBoolean("system_lsalarm_all"))
                timestamp = Helpers.getNextStockAlarmTime(mContext);
            if (timestamp == 0) return;

            StringBuilder alarmStr = new StringBuilder();
            alarmStr.append("\n").append(ModuleHelper.getModuleRes(mContext).getString(R.string.system_statusbaricons_alarm_title)).append(" ");
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
            XposedHelpers.log(t);
        }
    }

    public static void LockScreenAlarmHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.keyguard.clock.MiuiKeyguardSingleClock", lpparam.getClassLoader(), "updateTime", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mMiuiBaseClock = XposedHelpers.getObjectField(param.getThisObject(), "mMiuiBaseClock");
                if (mMiuiBaseClock != null) hookUpdateTime(mMiuiBaseClock, true);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.keyguard.clock.MiuiKeyguardDualClock", lpparam.getClassLoader(), "updateTime", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mMiuiDualClock = XposedHelpers.getObjectField(param.getThisObject(), "mMiuiDualClock");
                if (mMiuiDualClock != null) hookUpdateTime(mMiuiDualClock, false);
            }
        });
    }

    public static void ScreenshotConfigHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("android.content.ContentResolver", lpparam.getClassLoader(), "update", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (param.getArgs().length != 4) return;
                ContentValues contentValues = (ContentValues) param.getArgs()[1];
                String displayName = contentValues.getAsString("_display_name");
                if (displayName != null && displayName.contains("Screenshot")) {
                    int format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2);
                    String ext = format <= 2 ? ".jpg" : (format == 3 ? ".png" : ".webp");

                    displayName = displayName.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext;
                    contentValues.put("_display_name", displayName);
                }
            }
        });
        ModuleHelper.findAndHookMethod("android.content.ContentResolver", lpparam.getClassLoader(), "insert", Uri.class, ContentValues.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Uri imgUri = (Uri) param.getArgs()[0];
                ContentValues contentValues = (ContentValues) param.getArgs()[1];
                String displayName = contentValues.getAsString("_display_name");
                if (MediaStore.Images.Media.EXTERNAL_CONTENT_URI.equals(imgUri) && displayName != null && displayName.contains("Screenshot")) {
                    int folder = MainModule.mPrefs.getStringAsInt("system_screenshot_path", 1);
                    String dir = MainModule.mPrefs.getString("system_screenshot_mypath", "");
                    int format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2);
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
            ModuleHelper.findAndHookMethod("com.miui.screenshot.MiuiScreenshotApplication", lpparam.getClassLoader(), "attachBaseContext", Context.class, new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Context mContext = (Context) param.getArgs()[0];
                    long versionCode = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
                    MethodHook changeFormatHook = new MethodHook() {
                        @Override
                        protected void before(final BeforeHookCallback param) throws Throwable {
                            if (param.getArgs().length < 7) return;
                            Bitmap.CompressFormat compress = format <= 2 ? Bitmap.CompressFormat.JPEG : (format == 3 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
                            param.getArgs()[4] = compress;
                        }
                    };
                    if (versionCode >= 10400056) {
                        ModuleHelper.hookAllMethods("com.miui.screenshot.u0.f$a", lpparam.getClassLoader(), "a", changeFormatHook);
                    }
                    else if (versionCode >= 10400034) {
                        ModuleHelper.hookAllMethods("com.miui.screenshot.x0.e$a", lpparam.getClassLoader(), "a", changeFormatHook);
                    }
                }
            });
        }

        ModuleHelper.hookAllMethods("android.graphics.Bitmap", lpparam.getClassLoader(), "compress", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                int quality = (int) param.getArgs()[1];
                if (quality != 100 || (param.getArgs()[2] instanceof ByteArrayOutputStream)) return;
                int format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2);
                quality = MainModule.mPrefs.getInt("system_screenshot_quality", 100);
                if (format == 3) {
                    quality = 100;
                }
                Bitmap.CompressFormat compress = format <= 2 ? Bitmap.CompressFormat.JPEG : (format == 3 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
                param.getArgs()[0] = compress;
                param.getArgs()[1] = quality;
            }
        });
    }

    public static void ToastTimeHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.notification.NotificationManagerService", lpparam.getClassLoader(), "showNextToastLocked", new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.callMethod(param.getThisObject(), "getContext");
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
                ArrayList<Object> mToastQueue = (ArrayList<Object>)XposedHelpers.getObjectField(param.getThisObject(), "mToastQueue");
                if (mContext == null || mHandler == null || mToastQueue == null || mToastQueue.size() == 0) return;
                int mod = (MainModule.mPrefs.getInt("system_toasttime", 0) - 4) * 1000;
                for (Object record: mToastQueue)
                    if (record != null && mHandler.hasMessages(2, record)) {
                        mHandler.removeCallbacksAndMessages(record);
                        int duration = XposedHelpers.getIntField(record, "duration");
                        int delay = Math.max(1000, (duration == 1 ? 3500 : 2000) + mod);
                        mHandler.sendMessageDelayed(Message.obtain(mHandler, 2, record), delay);
                    }
            }
        });

        String windowClass = "com.android.server.wm.DisplayPolicy";
        ModuleHelper.hookAllMethods(windowClass, lpparam.getClassLoader(), "adjustWindowParamsLw", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object lp = param.getArgs().length == 1 ? param.getArgs()[0] : param.getArgs()[1];
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mPrevHideTimeout", XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds"));
            }

            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object lp = param.getArgs().length == 1 ? param.getArgs()[0] : param.getArgs()[1];
                long mPrevHideTimeout = (long)XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mPrevHideTimeout");
                long mHideTimeout = XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds");
                if (mPrevHideTimeout == -1 || mHideTimeout == -1) return;

                long dur = 0;
                if (mPrevHideTimeout == 1000 || mPrevHideTimeout == 4000 || mPrevHideTimeout == 5000 || mPrevHideTimeout == 7000 || mPrevHideTimeout != mHideTimeout)
                    dur = Math.max(1000, 3500 + (MainModule.mPrefs.getInt("system_toasttime", 0) - 4) * 1000);
                if (dur != 0) XposedHelpers.setLongField(lp, "hideTimeoutMilliseconds", dur);
            }
        });
    }

    public static void ClearAllTasksHook(SystemServerLoadedParam lpparam) {
        String wpuClass = "com.android.server.wm.WindowProcessUtils";
        ModuleHelper.hookAllMethods(wpuClass, lpparam.getClassLoader(), "getPerceptibleRecentAppList", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                param.setResult(null);
            }
        });
    }

    public static void InactiveBrightnessSliderHook(PackageLoadedParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int opt = MainModule.mPrefs.getStringAsInt("system_inactivebrightness", 1);
                if (opt == 2) {
                    SeekBar mSlider = (SeekBar)XposedHelpers.getObjectField(param.getThisObject(), "mSlider");
                    if (mSlider != null)
                        mSlider.setOnTouchListener(new View.OnTouchListener(){
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return true;
                            }
                        });
                } else if (opt == 3) try {
                    View sliderView = (View)param.getThisObject();
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)sliderView.getLayoutParams();
                    lp.height = 0;
                    lp.topMargin = Math.round(2 * sliderView.getResources().getDisplayMetrics().density);
                    lp.bottomMargin = 0;
                    sliderView.setLayoutParams(lp);
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.settings.brightness.BrightnessSliderView", lpparam.getClassLoader(), "onFinishInflate", hook);
    }

    private static final float[] startPos = new float[2];
    private static void processLSEvent(AfterHookCallback param) {
        MotionEvent event = (MotionEvent)param.getArgs()[0];
        if (event.getPointerCount() > 1) return;
        int action = event.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP) return;

        ViewGroup mKeyguardBottomArea = (ViewGroup)XposedHelpers.getObjectField(param.getThisObject(), "mBottomAreaView");
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
            Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
            int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
            if (Math.abs(event.getX() - startPos[0]) > slop || Math.abs(event.getY() - startPos[1]) > slop) return;
            Object mPanelViewController = XposedHelpers.getObjectField(param.getThisObject(), "mPanelViewController");
            Object statusBarKeyguardViewManager = XposedHelpers.getObjectField(mPanelViewController, "statusBarKeyguardViewManager");

            XposedHelpers.callMethod(statusBarKeyguardViewManager, "showGenericBouncer", true);
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
    }

    public static void TapToUnlockHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.keyguard.injector.KeyguardPanelViewInjector", lpparam.getClassLoader(), "onTouchEvent", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                processLSEvent(param);
            }
        });

        ModuleHelper.hookAllMethods("com.android.keyguard.injector.KeyguardPanelViewInjector", lpparam.getClassLoader(), "onInterceptTouchEvent", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                processLSEvent(param);
            }
        });
    }

    public static void NoLowBatteryWarningHook() {
        MethodHook settingHook = new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String key = (String)param.getArgs()[1];
                if ("low_battery_dialog_disabled".equals(key)) param.returnAndSkip(1);
                else if ("low_battery_sound".equals(key)) param.returnAndSkip(null);
            }
        };
        ModuleHelper.hookAllMethods(Settings.System.class, "getInt", settingHook);
        ModuleHelper.hookAllMethods(Settings.Global.class, "getString", settingHook);
    }

    public static void TempHideOverlayAppHook(XposedModuleInterface.SystemServerLoadedParam lpparam) {
        final int flagIndex = 2;
        ModuleHelper.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                int windowType = (int) param.getArgs()[4];
                if (windowType != WindowManager.LayoutParams.TYPE_PHONE
                    && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                    && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                    && windowType != WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) return;
                int flags = (int) param.getArgs()[flagIndex];
                int skipFlag = 64;
                flags |= skipFlag;
                param.getArgs()[flagIndex] = flags;
            }
        });
    }

    public static void GalleryScreenshotPathHook(PackageLoadedParam lpparam) {
        Class<?> MIUIStorageConstants = findClass("com.miui.gallery.storage.constants.MIUIStorageConstants", lpparam.getClassLoader());
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

    public static void ScreenshotFloatTimeHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.screenshot.GlobalScreenshot", lpparam.getClassLoader(), "startGotoThumbnailAnimation", Runnable.class, new MethodHook() {
            @Override
            @SuppressWarnings("ConstantConditions")
            protected void after(final AfterHookCallback param) throws Throwable {
                boolean mIsShowLongScreenShotGuide = false;
                try {
                    mIsShowLongScreenShotGuide = XposedHelpers.getBooleanField(param.getThisObject(), "mIsShowLongScreenShotGuide");
                } catch (Throwable ignore) {}
                if (mIsShowLongScreenShotGuide) return;
                int opt = MainModule.mPrefs.getInt("system_screenshot_floattime", 0);
                if (opt <= 0) return;
                Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
                Runnable mQuitThumbnailRunnable = (Runnable)XposedHelpers.getObjectField(param.getThisObject(), "mQuitThumbnailRunnable");
                mHandler.removeCallbacks(mQuitThumbnailRunnable);
                mHandler.postDelayed(mQuitThumbnailRunnable, opt * 1000L);
            }
        });
    }

    public static void ScrambleAppLockPINHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.miui.applicationlock.widget.MiuiNumericInputView", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                LinearLayout keys = (LinearLayout)param.getThisObject();
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

    public static void ChargingInfoHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.keyguard.charge.ChargeUtils", lpparam.getClassLoader(), "getChargingHintText", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int charge = (int)param.getArgs()[2];
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

        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.KeyguardIndicationTextView", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                int opt = MainModule.mPrefs.getStringAsInt("system_charginginfo_view", 1);
                if (opt != 1) return;
                TextView indicator = (TextView)param.getThisObject();
                if (indicator != null) indicator.setSingleLine(false);
            }
        });
    }

    public static void NoSOSHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.keyguard.EmergencyButton", lpparam.getClassLoader(), "updateEmergencyCallButton", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Button mSOS = (Button)param.getThisObject();
                if (mSOS.getVisibility() == View.VISIBLE) {
                    mSOS.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void NoDarkForceHook(SystemServerLoadedParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                ContentResolver contentResolver = ((Context)XposedHelpers.callMethod(param.getThisObject(), "getContext")).getContentResolver();
                XposedHelpers.callStaticMethod(Settings.System.class, "putIntForUser", contentResolver, "smart_dark_enable", 0, XposedHelpers.callStaticMethod(UserHandle.class, "getCallingUserId"));
                XposedHelpers.callStaticMethod(findClassIfExists("android.os.SystemProperties", lpparam.getClassLoader()), "set", "debug.hwui.force_dark", "false");
            }
        };
        ModuleHelper.findAndHookMethodSilently("com.android.server.UiModeManagerService", lpparam.getClassLoader(), "setForceDark", Context.class, hook);
        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "getAppDarkModeForUser", String.class, int.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(false);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.server.DarkModeAppSettingsInfo", lpparam.getClassLoader(), "getOverrideEnableValue", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(2);
            }
        });
    }

    public static void MaxNotificationIconsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconContainer", lpparam.getClassLoader(), "miuiShowNotificationIcons", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean isShow = (boolean) param.getArgs()[0];
                if (isShow) {
                    int opt = MainModule.mPrefs.getStringAsInt("system_maxsbicons", 0);
                    opt = opt == -1 ? 999 : opt;
                    XposedHelpers.setObjectField(param.getThisObject(), "MAX_DOTS", 3);
                    XposedHelpers.setObjectField(param.getThisObject(), "MAX_STATIC_ICONS", opt);
                    String fieldLockVisible = "MAX_ICONS_ON_LOCKSCREEN";
                    XposedHelpers.setObjectField(param.getThisObject(), fieldLockVisible, opt);
                    XposedHelpers.callMethod(param.getThisObject(), "updateState");
                    param.returnAndSkip(null);
                }
            }
        });
    }

    public static void MoreNotificationsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy", lpparam.getClassLoader(), "checkNotificationCountLimit", String.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
            String pkgName = (String) param.getArgs()[0];
            Collection<Object> mNotifications = (Collection<Object>) XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mEntryManager"), "getAllNotifs");
            List list = (List) mNotifications.stream().filter(new Predicate() {
                @Override
                public boolean test(Object obj) {
                    String notifyPkgName = (String) XposedHelpers.callMethod(XposedHelpers.callMethod(obj, "getSbn"), "getPackageName");
                    return pkgName.equals(notifyPkgName);
                }
            }).collect(Collectors.toList());
            if (list.size() < 24) param.returnAndSkip(null);
            }
        });
    }

    public static void AutoDismissExpandedPopupsHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpManagerPhone$HeadsUpEntryPhone", lpparam.getClassLoader(), "setExpanded", boolean.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                boolean newValue = (boolean) param.getArgs()[0];
                if (newValue) {
                    boolean expanded = XposedHelpers.getBooleanField(param.getThisObject(), "expanded");
                    if (expanded != newValue) {
                        XposedHelpers.callMethod(param.getThisObject(), "removeAutoRemovalCallbacks");
                        Object nm = XposedHelpers.getSurroundingThis(param.getThisObject());
                        Handler mHandler = (Handler) XposedHelpers.getObjectField(nm, "mHandler");
                        Runnable mRemoveAlertRunnable = (Runnable) XposedHelpers.getObjectField(param.getThisObject(), "mRemoveAlertRunnable");
                        mHandler.postDelayed(mRemoveAlertRunnable, 5000);
                    }
                    param.returnAndSkip(null);
                }
            }
        });
    }

    public static void BetterPopupsAllowFloatHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.getClassLoader(), "canSlide", String.class, new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                String pkgName = (String) param.getArgs()[0];
                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps");
                Set<String> selectedAppsBlack = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps_black");
                if (selectedApps.contains(pkgName)) param.setResult(true);
                else if (selectedAppsBlack.contains(pkgName)) param.setResult(false);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.policy.MiniWindowPolicy", lpparam.getClassLoader(), "canSlidePackage", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(true);
            }
        });
    }

    private static void DisableFloatingWindowBlacklistHook(ClassLoader cl) {
        MethodHook clearHook = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                List<String> blackList = (List<String>)param.getResult();
                if (blackList != null) blackList.clear();
                blackList.add("com.android.camera");
                param.setResult(blackList);
            }
        };
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", cl, "getListFromCloudData", clearHook);
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", cl, "getStartFromFreeformBlackListFromCloud", clearHook);
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "getFreeformBlackList", clearHook);
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "getFreeformBlackListFromCloud", clearHook);
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "setFreeformBlackList", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                List<String> blackList = new ArrayList<String>();
                blackList.add("com.android.camera");
                param.getArgs()[0] = blackList;
            }
        });
        ModuleHelper.findAndHookMethod("android.util.MiuiMultiWindowUtils", cl, "isForceResizeable", HookerClassHelper.returnConstant(true));
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowUtils", cl, "isPkgMainActivityResizeable", HookerClassHelper.returnConstant(true));
    }

    public static void DisableSideBarSuggestionHook(PackageLoadedParam lpparam) {
        DisableFloatingWindowBlacklistHook(lpparam.getClassLoader());
    }

    public static void NoFloatingWindowBlacklistHook(SystemServerLoadedParam lpparam) {
        MainModule.resHooks.setResReplacement("android", "array", "freeform_black_list", R.array.miui_resize_black_list);
        MainModule.resHooks.setResReplacement("com.miui.rom", "array", "freeform_black_list", R.array.miui_resize_black_list);
        DisableFloatingWindowBlacklistHook(lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod("com.android.server.wm.MiuiFreeformServicesUtils", lpparam.getClassLoader(), "supportsFreeform", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                param.returnAndSkip(true);
            }
        });
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
            XposedHelpers.log(ignore);
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

    public static void OpenAppInFreeFormHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", lpparam.getClassLoader(), "onSystemReady", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
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
        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityStarter", lpparam.getClassLoader(), "executeRequest", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object request = param.getArgs()[0];
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
//                XposedHelpers.log("startAct: " + callingPackage
//                    + " reason| " + reason
//                    + " intent| " + intent
//                    + " openInFw| " + openInFw
//                    + " activityOptions| " + Helpers.stringifyBundle(ao)
//                    + " intentExtra| " + Helpers.stringifyBundle(intent.getExtras())
//                );

                if (openInFw) {
                    Context mContext = (Context) XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.getThisObject(), "mService"), "mContext");
                    ActivityOptions options = MiuiMultiWindowUtils.getActivityOptions(mContext, intent.getComponent().getPackageName(), true, false);
                    XposedHelpers.callMethod(param.getThisObject(), "setActivityOptions", options.toBundle());
                }
            }
        });
//        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityStarterInjector", lpparam.getClassLoader(), "modifyLaunchActivityOptionIfNeed", new MethodHook() {
//            @Override
//            protected void after(final AfterHookCallback param) throws Throwable {
//                if (!Modifier.isPrivate(param.getMember().getModifiers())) {
//                    return;
//                }
//                Intent intent = (Intent)param.getArgs()[5];
//                if (intent == null || intent.getComponent() == null) return;
//                ActivityOptions ao = (ActivityOptions) param.getResult();
//                String callingPackage = (String) param.getArgs()[1];
//                ActivityOptions baseAO = (ActivityOptions) param.getArgs()[2];
//                XposedHelpers.log("modifyOptions: " + callingPackage
//                    + " baseOptions| " + Helpers.stringifyBundle(baseAO != null ? baseAO.toBundle() : null)
//                    + " intent| " + intent
//                    + " activityOptions| " + Helpers.stringifyBundle(ao != null ? ao.toBundle() : null)
//                    + " intentExtra| " + Helpers.stringifyBundle(intent.getExtras())
//                );
//            }
//        });
    }

    public static void StickyFloatingWindowsHook(SystemServerLoadedParam lpparam) {
        final List<String> fwBlackList = new ArrayList<String>();
        fwBlackList.add("com.miui.securitycenter");
        fwBlackList.add("com.miui.home");
        fwBlackList.add("com.android.camera");

        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityStarterInjector", lpparam.getClassLoader(), "modifyLaunchActivityOptionIfNeed", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (!Modifier.isPublic(param.getMember().getModifiers())) {
                    return;
                }
                Intent intent = (Intent)param.getArgs()[5];
                if (intent == null || intent.getComponent() == null) return;
                String pkgName = intent.getComponent().getPackageName();
                Context mContext;
                try {
                    mContext = (Context)XposedHelpers.getObjectField(param.getArgs()[0], "mContext");
                } catch (Throwable ignore) {
                    mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.getArgs()[0], "mService"), "mContext");
                }
                if (fwBlackList.contains(pkgName)) return;
                ActivityOptions options = (ActivityOptions)param.getResult();
                int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");

                if (windowingMode != 5 && fwApps.containsKey(pkgName)) {
                    try {
                        options = patchActivityOptions(mContext, options, pkgName);
                        param.setResult(options);
                    } catch (Throwable t) {
                        XposedHelpers.log(t);
                    }
                }
                else if (windowingMode == 5 && !fwApps.containsKey(pkgName)) {
                    fwApps.put(pkgName, new Pair<Float, Rect>(0f, null));
                    storeFwAppsInSetting(mContext);
                }
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityTaskSupervisor", lpparam.getClassLoader(), "startActivityFromRecents", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object safeOptions = param.getArgs()[3];
                ActivityOptions options = (ActivityOptions)XposedHelpers.callMethod(safeOptions, "getOptions", param.getThisObject());
                if (options != null) {
                    Object windowToken = XposedHelpers.callMethod(options, "getLaunchRootTask");
                    if (windowToken != null) return;
                }
                String pkgName = (String) XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "startPackageName");
                XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "startPackageName");
                if (fwBlackList.contains(pkgName)) return;
                int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");
                if (windowingMode == 5 && pkgName != null) {
                    fwApps.put(pkgName, new Pair<Float, Rect>(0f, null));
                    Context mContext = (Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.getThisObject(), "mService"), "mContext");
                    storeFwAppsInSetting(mContext);
                }
            }
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Object safeOptions = param.getArgs()[3];
                ActivityOptions options = (ActivityOptions)XposedHelpers.callMethod(safeOptions, "getOptions", param.getThisObject());
                if (options != null) {
                    Object windowToken = XposedHelpers.callMethod(options, "getLaunchRootTask");
                    if (windowToken != null) return;
                }
                String pkgName = getTaskPackageName(param.getThisObject(), (int)param.getArgs()[2], options);
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "startPackageName",  pkgName);
                if (!fwBlackList.contains(pkgName)) {
                    int windowingMode = options == null ? -1 : (int)XposedHelpers.callMethod(options, "getLaunchWindowingMode");
                    if (windowingMode != 5 && fwApps.containsKey(pkgName)) {
                        Object mService = XposedHelpers.getObjectField(param.getThisObject(), "mService");
                        Context mContext = (Context)XposedHelpers.getObjectField(mService, "mContext");
                        options = patchActivityOptions(mContext, options, pkgName);
                        XposedHelpers.setObjectField(safeOptions, "mOriginalOptions", options);
                        param.getArgs()[3] = safeOptions;
                        Intent intent = new Intent(ACTION_PREFIX + "dismissRecentsWhenFreeWindowOpen");
                        intent.putExtra("package", pkgName);
                        mContext.sendBroadcast(intent);
                    }
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.wm.MiuiFreeFormGestureController$FreeFormReceiver", lpparam.getClassLoader(), "onReceive", Context.class, Intent.class, new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Intent intent = (Intent) param.getArgs()[1];
                String action = intent.getAction();
                if ("miui.intent.action_launch_fullscreen_from_freeform".equals(action)) {
                    Object parentThis = XposedHelpers.getSurroundingThis(param.getThisObject());
                    XposedHelpers.setAdditionalInstanceField(parentThis, "skipFreeFormStateClear", true);
                }
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.wm.MiuiFreeFormGestureController", lpparam.getClassLoader(), "notifyFullScreenWidnowModeStart", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                if (param.getArgs().length != 3) return;
                String pkgName = (String)XposedHelpers.callMethod(param.getArgs()[1], "getStackPackageName");
                Object skipClear = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "skipFreeFormStateClear");
                boolean skipFreeFormStateClear = false;
                if (skipClear != null) {
                    skipFreeFormStateClear = (boolean) skipClear;
                }
                if (!skipFreeFormStateClear) {
                    if (fwBlackList.contains(pkgName)) return;
                    if (fwApps.remove(pkgName) != null) {
                        storeFwAppsInSetting((Context)XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.getThisObject(), "mService"), "mContext"));
                    }
                }
                else {
                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "skipFreeFormStateClear", false);
                }
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityTaskManagerService", lpparam.getClassLoader(), "launchSmallFreeFormWindow", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object taskId = XposedHelpers.getObjectField(param.getArgs()[0], "taskId");
                Object mMiuiFreeFormManagerService = XposedHelpers.getObjectField(param.getThisObject(), "mMiuiFreeFormManagerService");
                Object miuiFreeFormActivityStack = XposedHelpers.callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", taskId);
                String pkgName = (String) XposedHelpers.callMethod(miuiFreeFormActivityStack, "getStackPackageName");
                if (fwBlackList.contains(pkgName)) return;
                if (!fwApps.containsKey(pkgName)) {
                    fwApps.put(pkgName, new Pair<Float, Rect>(0f, null));
                    Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                    storeFwAppsInSetting(mContext);
                }
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", lpparam.getClassLoader(), "onSystemReady", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                restoreFwAppsInSetting(mContext);
                Class<?> MiuiMultiWindowAdapter = findClass("android.util.MiuiMultiWindowAdapter", lpparam.getClassLoader());
                List<String> blackList = (List<String>) XposedHelpers.getStaticObjectField(MiuiMultiWindowAdapter, "FREEFORM_BLACK_LIST");
                blackList.clear();
                mContext.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if ("miui.intent.action_launch_fullscreen_from_freeform".equals(action)) {
                            XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "skipFreeFormStateClear", true);
                        }
                    }
                }, new IntentFilter("miui.intent.action_launch_fullscreen_from_freeform"));
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityTaskManagerService", lpparam.getClassLoader(), "resizeTask", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String pkgName = getTaskPackageName(param.getThisObject(), (int)param.getArgs()[0]);
                if (pkgName != null) {
                    Object skipClear = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "skipFreeFormStateClear");
                    boolean skipFreeFormStateClear = false;
                    if (skipClear != null) {
                        skipFreeFormStateClear = (boolean) skipClear;
                    }
                    if (skipFreeFormStateClear) {
                        XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "skipFreeFormStateClear", false);
                    }
                    else {
                        if (fwBlackList.contains(pkgName)) return;
                        Object mMiuiFreeFormManagerService = XposedHelpers.getObjectField(param.getThisObject(), "mMiuiFreeFormManagerService");
                        Object miuiFreeFormActivityStack = XposedHelpers.callMethod(mMiuiFreeFormManagerService, "getMiuiFreeFormActivityStack", param.getArgs()[0]);
                        if (fwApps.containsKey(pkgName)) {
                            Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                            float sScale = (float) XposedHelpers.callMethod(miuiFreeFormActivityStack, "getFreeFormScale");
                            fwApps.put(pkgName, new Pair<Float, Rect>(sScale, new Rect((Rect)param.getArgs()[1])));
                            storeFwAppsInSetting(mContext);
                        }
                    }
                }
            }
        });
    }

    public static void MessagingStyleLinesHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.notification.NotificationMessagingTemplateViewWrapper", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mMessagingLinearLayout = XposedHelpers.getObjectField(param.getThisObject(), "mMessagingLinearLayout");
                int mMaxDisplayedLines = XposedHelpers.getIntField(mMessagingLinearLayout, "mMaxDisplayedLines");
                if (mMaxDisplayedLines == Integer.MAX_VALUE) XposedHelpers.callMethod(mMessagingLinearLayout, "setMaxDisplayedLines", MainModule.mPrefs.getInt("system_messagingstylelines", Integer.MAX_VALUE));
            }
        });
    }

    public static void MultiWindowPlusHook(SystemServerLoadedParam lpparam) {
        MainModule.resHooks.setResReplacement("android", "array", "miui_resize_black_list", R.array.miui_resize_black_list);
        MainModule.resHooks.setResReplacement("com.miui.rom", "array", "miui_resize_black_list", R.array.miui_resize_black_list);
        Class <?> AtmClass = findClassIfExists("com.android.server.wm.ActivityTaskManagerServiceImpl", lpparam.getClassLoader());
        if (AtmClass != null) {
            ModuleHelper.findAndHookMethod(AtmClass, "updateResizeBlackList", Context.class, HookerClassHelper.DO_NOTHING);
            ModuleHelper.findAndHookMethod(AtmClass, "getSplitScreenBlackListFromXml", HookerClassHelper.DO_NOTHING);
            ModuleHelper.hookAllMethods(AtmClass, "inResizeBlackList", HookerClassHelper.returnConstant(false));
        }
    }

    public static void MultiWindowPlusHook(PackageLoadedParam lpparam) {
        if (lpparam.getPackageName().equals("com.miui.home")) {
            ModuleHelper.findAndHookMethodSilently("com.android.systemui.shared.recents.model.Task", lpparam.getClassLoader(), "isSupportSplit", HookerClassHelper.returnConstant(true));
            ModuleHelper.hookAllMethods("com.miui.home.recents.views.RecentMenuView", lpparam.getClassLoader(), "onMessageEvent", new MethodHook() {
                @Override
                protected void after(final AfterHookCallback param) throws Throwable {
                    Handler mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ImageView mMenuItemMultiWindow = (ImageView)XposedHelpers.getObjectField(param.getThisObject(), "mMenuItemMultiWindow");
                            ImageView mMenuItemSmallWindow = (ImageView)XposedHelpers.getObjectField(param.getThisObject(), "mMenuItemSmallWindow");
                            mMenuItemMultiWindow.setEnabled(true);
                            mMenuItemMultiWindow.setImageAlpha(255);
                            mMenuItemSmallWindow.setEnabled(true);
                            mMenuItemSmallWindow.setImageAlpha(255);
                        }
                    }, 200);
                }
            });
        }
    }

    public static void SecureControlCenterHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethodSilently("com.android.keyguard.utils.MiuiKeyguardUtils", lpparam.getClassLoader(), "supportExpandableStatusbarUnderKeyguard", HookerClassHelper.returnConstant(false));
        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.ControlCenterControllerImpl", lpparam.getClassLoader(), "onContentChanged", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                String key = (String) param.getArgs()[0];
                if ("expandable_under_lock_screen".equals(key)) {
                    param.getArgs()[1] = "0";
                }
            }
        });
    }

    public static void MinimalNotificationViewHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBar", lpparam.getClassLoader(), "updateNotification", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
            if (param.getArgs().length != 3) return;
            Object expandableRow = XposedHelpers.getObjectField(param.getArgs()[0], "row");
            Object mNotificationData = XposedHelpers.getObjectField(param.getThisObject(), "mNotificationData");
            boolean newLowPriority = (boolean)XposedHelpers.callMethod(mNotificationData, "isAmbient", XposedHelpers.callMethod(param.getArgs()[1], "getKey")) && !(boolean)XposedHelpers.callMethod(XposedHelpers.callMethod(param.getArgs()[1], "getNotification"), "isGroupSummary");
            boolean hasEntry = XposedHelpers.callMethod(mNotificationData, "get", XposedHelpers.getObjectField(param.getArgs()[0], "key")) != null;
            boolean isLowPriority = (boolean)XposedHelpers.callMethod(expandableRow, "isLowPriority");
            XposedHelpers.callMethod(expandableRow, "setIsLowPriority", newLowPriority);
            boolean hasLowPriorityChanged = hasEntry && isLowPriority != newLowPriority;
            XposedHelpers.callMethod(expandableRow, "setLowPriorityStateUpdated", hasLowPriorityChanged);
            XposedHelpers.callMethod(expandableRow, "updateNotification", param.getArgs()[0]);
            }
        });
    }

    public static void NotificationChannelSettingsHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.getClassLoader(), "onClickInfoItem", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context)param.getArgs()[0];
                Object entry = XposedHelpers.callMethod(XposedHelpers.getObjectField(param.getThisObject(), "mParent"), "getEntry");
                String id = (String)XposedHelpers.callMethod(XposedHelpers.callMethod(entry, "getChannel"), "getId");
                if ("miscellaneous".equals(id)) return;
                Object notification = XposedHelpers.callMethod(entry, "getSbn");
                Class<?> nuCls = findClassIfExists("com.android.systemui.miui.statusbar.notification.NotificationUtil", lpparam.getClassLoader());
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
                    param.returnAndSkip(null);
                    Object ModalController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", mContext.getClassLoader()), "get", findClass("com.android.systemui.statusbar.notification.modal.ModalController", mContext.getClassLoader()));
                    XposedHelpers.callMethod(ModalController, "animExitModelCollapsePanels");
                } catch (Throwable ignore) {
                    XposedHelpers.log(ignore);
                }
            }
        });
    }

    public static void SkipAppLockHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.server.AccessController", lpparam.getClassLoader(), "skipActivity", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Intent intent = (Intent)param.getArgs()[0];
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

    public static void HideLockScreenHintHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardIndicationRotateTextViewController", lpparam.getClassLoader(), "hasIndicationsExceptResting", HookerClassHelper.returnConstant(true));
    }

    public static void HideLockScreenStatusBarHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods(StatusBarCls, lpparam.getClassLoader(), "makeStatusBarView", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
            View mKeyguardStatusBar = (View) XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.getThisObject(), "mNotificationPanelViewController"), "mKeyguardStatusBar");
            mKeyguardStatusBar.setTranslationY(-999f);
            }
        });
    }

    public static void MuteVisibleNotificationsHook(PackageLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.policy.NotificationAlertController", lpparam.getClassLoader(), "buzzBeepBlink", new MethodHook() {
            @Override
            protected void before(final BeforeHookCallback param) throws Throwable {
                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                PowerManager powerMgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
                if (powerMgr.isInteractive()) {
                    param.returnAndSkip(null);
                }
            }
        });
    }

    public static void NetworkIndicatorWifi(PackageLoadedParam lpparam) {
        MethodHook hideWifiActivity = new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Object mWifiActivityView = XposedHelpers.getObjectField(param.getThisObject(), "mWifiActivityView");
                XposedHelpers.callMethod(mWifiActivityView, "setVisibility", 4);
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.getClassLoader(), "applyWifiState", hideWifiActivity);
    }

    public static void SetLockscreenWallpaperHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.wallpaper.WallpaperManagerService", lpparam.getClassLoader(), "setWallpaper", new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                if (param.getThrowable() != null || param.getResult() == null || (int)param.getArgs()[5] == 1 || "com.android.thememanager".equals(param.getArgs()[1])) return;

                Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
                if (mContext == null) return;

                int handleIncomingUser = 0;
                try {
                    handleIncomingUser = (int)XposedHelpers.callStaticMethod(ActivityManager.class, "handleIncomingUser", Binder.getCallingPid(), Binder.getCallingUid(), param.getArgs()[7], false, true, "changing wallpaper", null);
                } catch (Throwable ignore) {}
                Object wallpaperData = XposedHelpers.callMethod(param.getThisObject(), "getWallpaperSafeLocked", handleIncomingUser, param.getArgs()[5]);
                File wallpaper = (File)XposedHelpers.getObjectField(wallpaperData, "wallpaperFile");

                new Handler(mContext.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!wallpaper.exists()) return;

                        String lockWallpaperPath = "/data/system/theme/thirdparty_lock_wallpaper";
                        Helpers.copyFile(wallpaper.getAbsolutePath(), lockWallpaperPath);
                        Class<?> ThemeUtils = findClass("miui.content.res.ThemeNativeUtils", lpparam.getClassLoader());
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
                                    .put("authority", "name.monwf.customiuizer.mods.set_lockscreen_wallpaper")
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
                            XposedHelpers.log(t);
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

    public static void BetterPopupsCenteredHook(PackageLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManagerInjector", lpparam.getClassLoader(), "miuiHeadsUpInset", android.content.Context.class, new MethodHook() {
            private int mHeadsUpPaddingTop = 0;
            private int mHeadsUpHeight = 0;
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                Context context = (Context) param.getArgs()[0];
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
    }

    public static void WallpaperScaleLevelHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.server.wm.WallpaperController", lpparam.getClassLoader(), new MethodHook() {
            @Override
            protected void after(final AfterHookCallback param) throws Throwable {
                float scale = MainModule.mPrefs.getInt("system_other_wallpaper_scale", 6) / 10.0f;
                XposedHelpers.setObjectField(param.getThisObject(), "mMaxWallpaperScale", scale);
                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
                    public void onChange(String key) {
                        if (key.contains("system_other_wallpaper_scale")) {
                            int val = MainModule.mPrefs.getInt("system_other_wallpaper_scale", 6);
                            XposedHelpers.setObjectField(param.getThisObject(), "mMaxWallpaperScale", val / 10.0f);
                        }
                    }
                });
            }
        });
    }

    public static void Disable72hStrongAuthHook(SystemServerLoadedParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.locksettings.LockSettingsStrongAuth", lpparam.getClassLoader(), "rescheduleStrongAuthTimeoutAlarm", long.class, int.class, HookerClassHelper.DO_NOTHING);
    }
}
