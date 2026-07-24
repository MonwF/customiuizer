package tv.withaibuild.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static tv.withaibuild.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClass;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findClassIfExists;
import static tv.withaibuild.customiuizer.mods.utils.XposedHelpers.findMethodExactIfExists;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.ColorUtils;

import org.json.JSONObject;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;

import io.github.libxposed.api.XposedInterface;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam;
import tv.withaibuild.customiuizer.MainModule;
import tv.withaibuild.customiuizer.R;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper;
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper;
import tv.withaibuild.customiuizer.mods.utils.ResourceConstants;
import tv.withaibuild.customiuizer.mods.utils.ResourceHooks;
import tv.withaibuild.customiuizer.mods.utils.WeatherDataController;
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers;
import tv.withaibuild.customiuizer.utils.AudioVisualizer;
import tv.withaibuild.customiuizer.utils.Helpers;
import tv.withaibuild.customiuizer.utils.Helpers.MimeType;

public class System {
    public static void ScreenAnimHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.getClassLoader(), "initialize", new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                try {
	            		                    XposedHelpers.setObjectField(thisObject, "mColorFadeEnabled", true);
	            		                    XposedHelpers.setObjectField(thisObject, "mColorFadeFadesConfig", true);
	            		                } catch (Throwable ignore) {}


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

	            		                ObjectAnimator mColorFadeOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(thisObject, "mColorFadeOffAnimator");
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


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
    }

    public static void NoAccessDeviceLogsRequest(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.logcat.LogcatManagerService", lpparam.getClassLoader(), "onLogAccessRequested", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                XposedHelpers.callMethod(thisObject, "declineRequest", args[0]);
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
    public static void NoLightUpOnChargeHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.getClassLoader(), "wakePowerGroupLocked", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                String reason = (String)args[3];
                                    if (reason == null) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                if (
            		                    reason.startsWith("android.server.power:PLUGGED")
            		                    || reason.equals("com.android.systemui:RAPID_CHARGE")
            		                    || reason.equals("com.android.systemui:WIRELESS_CHARGE")
            		                    || reason.equals("com.android.systemui:WIRELESS_RAPID_CHARGE")
            		                ) { skipped = true; result = null; throwable = null; }

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

    public static void ScramblePINHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardPINView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

            		                View[][] mViews = (View[][])XposedHelpers.getObjectField(thisObject, "mViews");
            		                ArrayList<View> mRandomViews = new ArrayList<View>();
            		                for (int row = 1; row <= 3; row++)
            		                    for (int col = 0; col <= 2; col++)
            		                        if (mViews[row][col] != null)
            		                            mRandomViews.add(mViews[row][col]);
            		                mRandomViews.add(mViews[4][1]);
            		                Collections.shuffle(mRandomViews);

            		                View pinview = (View)thisObject;
            		                ViewGroup row1 = pinview.findViewById(Helpers.getResId(pinview.getResources(), "row1", "id", "com.android.systemui"));
            		                ViewGroup row2 = pinview.findViewById(Helpers.getResId(pinview.getResources(), "row2", "id", "com.android.systemui"));
            		                ViewGroup row3 = pinview.findViewById(Helpers.getResId(pinview.getResources(), "row3", "id", "com.android.systemui"));
            		                ViewGroup row4 = pinview.findViewById(Helpers.getResId(pinview.getResources(), "row4", "id", "com.android.systemui"));

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

            		                XposedHelpers.setObjectField(thisObject, "mViews", mViews);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void NoPasswordHook(PackageReadyParam lpparam) {
        String isAllowed = "isBiometricAllowedForUser";
        ModuleHelper.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", lpparam.getClassLoader(), isAllowed, boolean.class, int.class, HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.android.internal.widget.LockPatternUtils", lpparam.getClassLoader(), isAllowed, int.class, HookerClassHelper.returnConstant(true));
    }

    public static void EnhancedSecurityHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "interceptPowerKeyDown", KeyEvent.class, boolean.class, new MethodHook() {
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

            		                Context mPWMContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
            		                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) {
            		                    Handler mHandler = (Handler)XposedHelpers.getObjectField(thisObject, "mHandler");
            		                    if (mHandler != null) {
            		                        Runnable mEndCallLongPress = (Runnable)XposedHelpers.getObjectField(thisObject, "mEndCallLongPress");
            		                        if (mEndCallLongPress != null) mHandler.removeCallbacks(mEndCallLongPress);
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        MethodHook preventPowerHook = new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                Context mPWMContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
            		                if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) { skipped = true; result = null; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };

        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "powerLongPress", long.class, preventPowerHook);
        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "showGlobalActions", preventPowerHook);
        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "showGlobalActionsInternal", preventPowerHook);
    }

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
            Object mController = ModuleHelper.getDepInstance(classLoader, "com.android.systemui.statusbar.policy.BluetoothController");
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
        if (!isAuthOnce()) return false;
        int opt = MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1);
        if (forcedOption == 1) opt = 2;
        if (opt == 2) return true;
        boolean isTrusted = false;
        if (opt == 3) {
            isTrusted = isTrusted(mContext, classLoader);
        }
        return isTrusted;
    }

    private static boolean isUnlockedInnerCall = false;
    private static boolean isUnlockedWithFingerprint = false;
    private static boolean isUnlockedWithStrong = false;
    private static boolean isChargingInfoHooked = false;
    private static int forcedOption = -1;
    public static void NoScreenLockHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader(), "handleKeyguardDone", new MethodHook() {
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

            		                if (isUnlockedInnerCall) {
            		                    isUnlockedInnerCall = false;
            		                    { return XposedHelpers.throwOrReturn(throwable, result); }
            		                }
            		                isUnlockedWithStrong = true;

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.getClassLoader(), "onFingerprintAuthenticated", int.class, boolean.class, new MethodHook() {
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

            		                isUnlockedWithFingerprint = true;

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardSecurityContainerController", lpparam.getClassLoader(), "onInit", new MethodHook() {
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

            		                Context mContext = (Context)XposedHelpers.callMethod(thisObject, "getContext");
            		                mContext.registerReceiver(new BroadcastReceiver() {
            		                    @Override
            		                    public void onReceive(Context context, Intent intent) {
            		                        try {
            		                            Object mCallback = XposedHelpers.getObjectField(thisObject, "mKeyguardSecurityCallback");
            		                            XposedHelpers.callMethod(mCallback, "reportUnlockAttempt", 0, 0, 0, true);
            		                        } catch (Throwable t) {
            		                            XposedHelpers.log(t);
            		                        }
            		                    }
            		                }, new IntentFilter(ACTION_PREFIX + "UnlockStrongAuth"), Context.RECEIVER_NOT_EXPORTED);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader(), "doKeyguardLocked", Bundle.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

                                    if (forcedOption == 0) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
                                    if (!isUnlocked(mContext, lpparam.getClassLoader())) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, throwable); }

            		                boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
            		                if (skip) {
            		                    XposedHelpers.callMethod(thisObject, "keyguardDone");
            		                    { skipped = true; result = null; throwable = null; }
            		                }
            		                isUnlockedInnerCall = true;
            		                Intent unlockIntent = new Intent(ACTION_PREFIX + "UnlockStrongAuth");
            		                unlockIntent.setPackage("com.android.systemui");
            		                mContext.sendBroadcast(unlockIntent);

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.getClassLoader(), "setupLocked", new MethodHook() {
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

            		                Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
            		                IntentFilter filter = new IntentFilter();
            		                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            		                filter.addAction(ACTION_PREFIX + "UnlockSetForced");
            		                filter.addAction(ACTION_PREFIX + "BTConnectionChanged");
            		                mContext.registerReceiver(new BroadcastReceiver() {
            		                    @Override
            		                    public void onReceive(Context context, Intent intent) {
            		                        String action = intent.getAction();

            		                        if (action.equals(ACTION_PREFIX + "UnlockSetForced"))
            		                            forcedOption = intent.getIntExtra("system_noscreenlock_force", -1);

            		                        boolean isShowing = XposedHelpers.getBooleanField(thisObject, "mShowing");
            		                        if (!isShowing) return;
            		                        if (!isAuthOnce()) return;

            		                        boolean isTrusted = false;
            		                        if (forcedOption == 1) isTrusted = true;
            		                        else if (forcedOption != 0 && MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1) == 3) {
            		                            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            		                                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            		                                if (netInfo.getState() != NetworkInfo.State.CONNECTED && netInfo.getState() != NetworkInfo.State.DISCONNECTED)
            		                                    return;
            		                                if (netInfo.isConnected()) isTrusted = isTrustedWiFi(mContext);
            		                            } else if (action.equals(ACTION_PREFIX + "BTConnectionChanged")) {
            		                                isTrusted = isTrustedBt(lpparam.getClassLoader());
            		                            }
            		                        }

            		                        if (isTrusted) {
            		                            boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
            		                            if (skip)
            		                                XposedHelpers.callMethod(thisObject, "keyguardDone");
            		                            else
            		                                XposedHelpers.callMethod(thisObject, "resetStateLocked", false);
            		                            isUnlockedInnerCall = true;
            		                            Intent unlockIntent = new Intent(ACTION_PREFIX + "UnlockStrongAuth");
            		                            unlockIntent.setPackage("com.android.systemui");
            		                            mContext.sendBroadcast(unlockIntent);
            		                        } else try {
            		                            XposedHelpers.callMethod(thisObject, "resetStateLocked", true);
            		                        } catch (Throwable t) {
            		                            XposedHelpers.log(t);
            		                        }
            		                    }
            		                }, filter, Context.RECEIVER_EXPORTED);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardSecurityModel", lpparam.getClassLoader(), "getSecurityMode", int.class, new MethodHook() {
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

            		                if (forcedOption == 0) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
            		                if (skip) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Object mKeyguardUpdateMonitor = XposedHelpers.getObjectField(thisObject, "mKeyguardUpdateMonitor");
            		                Context mContext = (Context) XposedHelpers.getObjectField(mKeyguardUpdateMonitor, "mContext");
            		                if (!isUnlocked(mContext, lpparam.getClassLoader())) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                Class<?> securityModeEnum = findClass("com.android.keyguard.KeyguardSecurityModel$SecurityMode", lpparam.getClassLoader());
            		                Object securityModeNone = XposedHelpers.getStaticObjectField(securityModeEnum, "None");
            		                Object securityModePassword = XposedHelpers.getStaticObjectField(securityModeEnum, "Password");
            		                Object securityModePattern = XposedHelpers.getStaticObjectField(securityModeEnum, "Pattern");
            		                Object securityModePin = XposedHelpers.getStaticObjectField(securityModeEnum, "PIN");

            		                Object secModeResult = result;
            		                if (securityModePassword.equals(secModeResult) ||
            		                        securityModePattern.equals(secModeResult) ||
            		                        securityModePin.equals(secModeResult))
            		                    { result = securityModeNone; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.getClassLoader(), new MethodHook() {
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

            		                Context mContext = (Context)args[0];
            		                mContext.registerReceiver(new BroadcastReceiver() {
            		                    public void onReceive(final Context context, Intent intent) {
            		                        ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
            		                        Intent updateIntent = new Intent(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE");
            		                        Collection<?> cachedDevices = (Collection<?>)XposedHelpers.callMethod(thisObject, "getDevices");
            		                        if (cachedDevices != null)
            		                            for (Object device: cachedDevices) {
            		                                BluetoothDevice mDevice = (BluetoothDevice)XposedHelpers.getObjectField(device, "mDevice");
            		                                if (mDevice != null) deviceList.add(mDevice);
            		                            }
            		                        updateIntent.putParcelableArrayListExtra("device_list", deviceList);
            		                        updateIntent.setPackage(Helpers.modulePkg);
            		                        mContext.sendBroadcast(updateIntent);
            		                    }
            		                }, new IntentFilter(ACTION_PREFIX + "FetchCachedDevices"), Context.RECEIVER_EXPORTED);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.getClassLoader(), "updateConnected", new MethodHook() {
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

            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                if (mContext != null) {
            		                    mContext.sendBroadcast(new Intent(ACTION_PREFIX + "BTConnectionChanged"));
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void DoubleTapToSleepHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.NotificationsQuickSettingsContainer", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

            		                View view = (View)thisObject;
            		                ModuleHelper.setViewInfo(view, "currentTouchTime", 0L);
            		                ModuleHelper.setViewInfo(view, "currentTouchX", 0F);
            		                ModuleHelper.setViewInfo(view, "currentTouchY", 0F);

            		                view.setOnTouchListener(new View.OnTouchListener() {
            		                    @Override
            		                    @SuppressLint("ClickableViewAccessibility")
            		                    public boolean onTouch(View v, MotionEvent event) {
            		                        if (event.getAction() != MotionEvent.ACTION_DOWN) return false;

            		                        long lastTouchTime = (long)ModuleHelper.getViewInfo(view, "currentTouchTime");
            		                        float lastTouchX = (float)ModuleHelper.getViewInfo(view, "currentTouchX");
            		                        float lastTouchY = (float)ModuleHelper.getViewInfo(view, "currentTouchY");

            		                        long currentTouchTime = java.lang.System.currentTimeMillis();
            		                        float currentTouchX = event.getX();
            		                        float currentTouchY = event.getY();

            		                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(currentTouchX - lastTouchX) < 100F && Math.abs(currentTouchY - lastTouchY) < 100F) {
            		                            KeyguardManager keyguardMgr = (KeyguardManager)v.getContext().getSystemService(Context.KEYGUARD_SERVICE);
            		                            if (keyguardMgr.isKeyguardLocked()) GlobalActions.commonSendAction(v.getContext(), "GoToSleep");
            		                            currentTouchTime = 0L;
            		                            currentTouchX = 0F;
            		                            currentTouchY = 0F;
            		                        }

            		                        ModuleHelper.setViewInfo(view, "currentTouchTime", currentTouchTime);
            		                        ModuleHelper.setViewInfo(view, "currentTouchX", currentTouchX);
            		                        ModuleHelper.setViewInfo(view, "currentTouchY", currentTouchY);

            		                        return false;
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ViewWifiPasswordHook(PackageReadyParam lpparam) {
        int titleId = MainModule.resHooks.addFakeResource("system_wifipassword_btn_title", R.string.system_wifipassword_btn_title, "string");
        int dlgTitleId = MainModule.resHooks.addFakeResource("system_wifi_password_dlgtitle", R.string.system_wifi_password_dlgtitle, "string");
        ModuleHelper.hookAllMethods("com.android.settings.wifi.SavedAccessPointPreference", lpparam.getClassLoader(), "onBindViewHolder", new MethodHook() {
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

            		                View view = (View) XposedHelpers.getObjectField(thisObject, "mView");
            		                int btnId = Helpers.getResId(view.getResources(), "btn_delete", "id", "com.android.settings");
            		                Button button = view.findViewById(btnId);
            		                button.setText(titleId);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        final String[] wifiSharedKey = new String[1];
        final String[] passwordTitle = new String[1];
        ModuleHelper.findAndHookMethod("miuix.appcompat.app.AlertDialog$Builder", lpparam.getClassLoader(), "setTitle", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if (wifiSharedKey[0] != null) {
            		                    args[0] = dlgTitleId;
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("miuix.appcompat.app.AlertDialog$Builder", lpparam.getClassLoader(), "setMessage", CharSequence.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if (wifiSharedKey[0] != null) {
            		                    CharSequence str = (CharSequence) args[0];
            		                    str = str + "\n" + passwordTitle[0] + ": " + wifiSharedKey[0];
            		                    args[0] = str;
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.hookAllMethods("miuix.appcompat.app.AlertDialog", lpparam.getClassLoader(), "onCreate", new MethodHook() {
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

            		                if (wifiSharedKey[0] != null) {
            		                    TextView messageView = (TextView) XposedHelpers.callMethod(thisObject, "getMessageView");
            		                    messageView.setTextIsSelectable(true);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.hookAllMethods("com.android.settings.wifi.MiuiSavedAccessPointsWifiSettings", lpparam.getClassLoader(), "showDeleteDialog", new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                Object wifiEntry = args[0];
	            		                boolean canShare = (boolean) XposedHelpers.callMethod(wifiEntry, "canShare");
	            		                if (canShare) {
	            		                    if (passwordTitle[0] == null) {
	            		                        Resources modRes = ModuleHelper.getModuleRes((Context) XposedHelpers.callMethod(thisObject, "getContext"));
	            		                        passwordTitle[0] = modRes.getString(R.string.system_wifi_password_label);
	            		                    }
	            		                    Object mWifiManager = XposedHelpers.getObjectField(thisObject, "mWifiManager");
	            		                    Object wifiConfiguration = XposedHelpers.callMethod(wifiEntry, "getWifiConfiguration");
	            		                    Class <?> WifiDppUtilsClass = findClass("com.android.settings.wifi.dpp.WifiDppUtils", lpparam.getClassLoader());
	            		                    String sharedKey = (String) XposedHelpers.callStaticMethod(WifiDppUtilsClass, "getPresharedKey", mWifiManager, wifiConfiguration);
	            		                    sharedKey = (String) XposedHelpers.callStaticMethod(WifiDppUtilsClass, "removeFirstAndLastDoubleQuotes", sharedKey);
	            		                    wifiSharedKey[0] = sharedKey;
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

	            		                Object wifiEntry = args[0];
	            		                boolean canShare = (boolean) XposedHelpers.callMethod(wifiEntry, "canShare");
	            		                if (canShare) {
	            		                    wifiSharedKey[0] = null;
	            		                }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
    }

    private static void initClockStyle(TextView mClock, String clockName) {
        Resources res = mClock.getResources();
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
            int align = MainModule.mPrefs.getStringAsInt("system_statusbar_clock_align", 1);
            if (align == 2) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
            else if (align == 3) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            else if (align == 4) {
                mClock.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            }
            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_bold")) {
                mClock.setTypeface(Typeface.DEFAULT_BOLD);
            }
            int leftMargin = MainModule.mPrefs.getInt("system_statusbar_clock_leftmargin", 0);
            leftMargin = (int) Helpers.dp2px(leftMargin * dimStep);
            int rightMargin = MainModule.mPrefs.getInt("system_statusbar_clock_rightmargin", 0);
            rightMargin = (int) Helpers.dp2px(rightMargin * dimStep);
            int defaultVerticalOffset = 8;
            int verticalOffset = MainModule.mPrefs.getInt("system_statusbar_clock_verticaloffset", defaultVerticalOffset);
            if (verticalOffset != defaultVerticalOffset) {
                float marginTop = Helpers.dp2px((verticalOffset - defaultVerticalOffset) * dimStep);
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
                boolean customTextColor = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_customtextcolor");

                int startColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_startcolor", 0x8F7C4DFF);
                int endColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_endcolor", 0x2FA7FFEB);
                if (useMonet) {
                    mClock.setTextColor(mClock.getResources().getColor(android.R.color.system_accent1_0, null));
                    startColor = mClock.getResources().getColor(android.R.color.system_accent1_600, null);
                    endColor = startColor;
                }
                else if (customTextColor) {
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
            int fixedWidth = MainModule.mPrefs.getInt("system_statusbar_clock_fixedcontent_width", 10);
            if (fixedWidth > 10) {
                ViewGroup.LayoutParams lp = mClock.getLayoutParams();
                lp.width = (int)(mClock.getResources().getDisplayMetrics().density * fixedWidth);
                mClock.setLayoutParams(lp);
            }
        }
        if (dualRows) {
            mClock.setSingleLine(false);
            mClock.setMaxLines(2);
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

    private static final class SecondTicker implements Runnable {
        private final Object clockController;
        private final Context context;
        private final Handler handler;
        private boolean running;

        SecondTicker(Object clockController, Context context) {
            this.clockController = clockController;
            this.context = context;
            handler = new Handler(context.getMainLooper());
        }

        void start() {
            running = true;
            scheduleNextTick();
        }

        void stop() {
            running = false;
            handler.removeCallbacks(this);
        }

        @Override
        public void run() {
            if (!running) return;
            try {
                Object calendar = XposedHelpers.getObjectField(clockController, "mCalendar");
                XposedHelpers.callMethod(calendar, "setTimeInMillis", java.lang.System.currentTimeMillis());
                XposedHelpers.setObjectField(
                    clockController,
                    "mIs24",
                    DateFormat.is24HourFormat(context)
                );
                ArrayList<Object> clockListeners =
                    (ArrayList<Object>) XposedHelpers.getObjectField(clockController, "mClockListeners");
                for (Object listener : clockListeners) {
                    View clock = (View) listener;
                    if (ModuleHelper.getViewInfo(clock, "showSeconds") != null) {
                        XposedHelpers.callMethod(clock, "updateTime");
                    }
                }
            } catch (Throwable t) {
                XposedHelpers.log("SecondTicker", t);
            }
            scheduleNextTick();
        }

        private void scheduleNextTick() {
            if (!running) return;
            long delay = 1000L - java.lang.System.currentTimeMillis() % 1000L;
            handler.postDelayed(this, delay);
        }
    }

    private static void initSecondTicker(Object clockController) {
        boolean ccShowSeconds = getCCShowSeconds();
        boolean finalSbShowSeconds = getShowSeconds();
        Context mContext = (Context) XposedHelpers.getObjectField(clockController, "mContext");
        SecondTicker previousTicker = (SecondTicker) XposedHelpers.getAdditionalInstanceField(
            clockController,
            "secondTicker"
        );
        if (previousTicker != null) {
            previousTicker.stop();
            XposedHelpers.removeAdditionalInstanceField(clockController, "secondTicker");
        }
        if (ccShowSeconds || finalSbShowSeconds) {
            SecondTicker ticker = new SecondTicker(clockController, mContext);
            XposedHelpers.setAdditionalInstanceField(clockController, "secondTicker", ticker);
            ticker.start();
        }
    }
    private static void initWeatherInfoHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.getClassLoader(), new MethodHook() {
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

            		                Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
            		                Runnable mWeatherRunnable = new Runnable() {
            		                    @Override
            		                    public void run() {
            		                        XposedHelpers.callMethod(thisObject, "updateTime");
            		                    }
            		                };
            		                WeatherDataController.initContext(mContext, mWeatherRunnable);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

    }
    public static void StatusBarClockTweakHook(PackageReadyParam lpparam) {
        boolean enableWeatherParam = MainModule.mPrefs.getBoolean("system_statusbar_enable_weather_param");
        if (enableWeatherParam) {
            initWeatherInfoHook(lpparam);
        }
        boolean hideStatusbarClock = MainModule.mPrefs.getBoolean("system_statusbaricons_clock");
        boolean statusbarClockTweak = !hideStatusbarClock && MainModule.mPrefs.getBoolean("system_statusbar_clocktweak");
        boolean ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak");
        MethodHook ScheduleHook = new MethodHook() {
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

                                        initSecondTicker(thisObject);
            		                Context mContext = (Context) XposedHelpers.getObjectField(thisObject, "mContext");
            		                if (getShowSeconds() || getCCShowSeconds()) {
            		                    BroadcastReceiver oldReceiver = (BroadcastReceiver) XposedHelpers.getAdditionalInstanceField(thisObject, "customiuizer_timeSetReceiver");
            		                    if (oldReceiver != null) try { mContext.unregisterReceiver(oldReceiver); } catch (Throwable ignore) {}
            		                    BroadcastReceiver mUpdateTimeReceiver = new BroadcastReceiver() {
            		                        @Override
            		                        public void onReceive(Context context, Intent intent) {
                                                    initSecondTicker(thisObject);
            		                        }
            		                    };
            		                    XposedHelpers.setAdditionalInstanceField(thisObject, "customiuizer_timeSetReceiver", mUpdateTimeReceiver);
            		                    IntentFilter timeSetIntent = new IntentFilter();
            		                    timeSetIntent.addAction("android.intent.action.TIME_SET");
            		                    mContext.registerReceiver(mUpdateTimeReceiver, timeSetIntent, Context.RECEIVER_NOT_EXPORTED);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        if (ccClockTweak || statusbarClockTweak) {
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.getClassLoader(), ScheduleHook);
        }
        boolean hideDateView = MainModule.mPrefs.getBoolean("system_cc_hidedate");
        boolean hideDrawerDate = MainModule.mPrefs.getBoolean("system_drawer_hidedate");
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), new MethodHook() {
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

            		                final TextView clock = (TextView)thisObject;
            		                if (args.length != 3) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int clockId = Helpers.getResId(clock.getResources(), "clock", "id", "com.android.systemui");
            		                int bigClockId = Helpers.getResId(clock.getResources(), "big_time", "id", "com.android.systemui");
            		                int dateClockId = Helpers.getResId(clock.getResources(), "date_time", "id", "com.android.systemui");
            		                int horizDateClockId = Helpers.getResId(clock.getResources(), "horizontal_date_time", "id", "com.android.systemui");
            		                int thisClockId = clock.getId();
            		                if (clockId == thisClockId) {
            		                    ModuleHelper.setViewInfo(clock, "clockName", "clock");
            		                    if (statusbarClockTweak && getShowSeconds()) {
            		                        ModuleHelper.setViewInfo(clock, "showSeconds", true);
            		                    }
            		                }
            		                else if (bigClockId == thisClockId) {
            		                    ModuleHelper.setViewInfo(clock, "clockName", "ccClock");
            		                    if (ccClockTweak) {
            		                        if (getCCShowSeconds()) {
            		                            ModuleHelper.setViewInfo(clock, "showSeconds", true);
            		                        }
            		                        initClockStyle(clock, "ccClock");
            		                    }
            		                }
            		                else if (thisClockId == horizDateClockId) {
            		                    ModuleHelper.setViewInfo(clock, "clockName", "drawerDate");
            		                }
            		                else if (dateClockId == thisClockId) {
            		                    boolean ccDate = clock.getClass().getCanonicalName().contains("ControlCenterDateView");
            		                    if (ccDate) {
            		                        ModuleHelper.setViewInfo(clock, "clockName", "ccDate");
            		                    }
            		                    if (!ccDate) {
            		                        ModuleHelper.setViewInfo(clock, "clockName", "drawerDate");
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        MethodHook updateTimeHook = new MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                TextView clock = (TextView)thisObject;
            		                String clockName = (String) ModuleHelper.getViewInfo(clock, "clockName");
            		                Context mContext = clock.getContext();
            		                if (("ccDate".equals(clockName) && hideDateView)
            		                    || ("drawerDate".equals(clockName) && hideDrawerDate)
            		                    || ("clock".equals(clockName) && hideStatusbarClock)
            		                ) {
            		                    clock.setText("");
            		                    { skipped = true; result = null; throwable = null; }
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                }

            		                Object mMiuiStatusBarClockController = XposedHelpers.getObjectField(clock, "mMiuiStatusBarClockController");
            		                Object mCalendar = XposedHelpers.getObjectField(mMiuiStatusBarClockController, "mCalendar");
            		                String timeFmt = null;
            		                if ("ccClock".equals(clockName)) {
            		                    if (ccClockTweak) {
            		                        String customFormat = MainModule.mPrefs.getString("system_cc_clock_customformat", "");
            		                        if (customFormat.length() > 0) {
            		                            timeFmt = customFormat;
            		                        }
            		                    }
            		                }
            		                else if ("ccDate".equals(clockName)) {
            		                    String ccDateFormat = MainModule.mPrefs.getString("system_cc_dateformat", "");
            		                    if (ccDateFormat.length() > 0) {
            		                        timeFmt = ccDateFormat;
            		                    }
            		                }
            		                else if ("drawerDate".equals(clockName)) {
            		                    String drawerDateFormat = MainModule.mPrefs.getString("system_drawer_dateformat", "");
            		                    if (drawerDateFormat.length() > 0) {
            		                        timeFmt = drawerDateFormat;
            		                    }
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
            		                        String fmt;
            		                        if (showAmpm) {
            		                            fmt = "fmt_time_12hour_minute_pm";
            		                        }
            		                        else {
            		                            fmt = "fmt_time_12hour_minute";
            		                        }
            		                        int fmtResId = Helpers.getResId(mContext.getResources(), fmt, "string", "com.android.systemui");
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
            		                        timeFmt = timeFmt.replaceFirst("h+:", hourStr + ":");
            		                    }
            		                }
            		                if (timeFmt != null) {
            		                    if (enableWeatherParam) {
            		                        String weatherInfo = WeatherDataController.weatherInfo;
            		                        timeFmt = timeFmt.replace("tq", weatherInfo);
            		                    }
            		                    StringBuilder formatSb = new StringBuilder(timeFmt);
            		                    StringBuilder textSb = new StringBuilder();
            		                    XposedHelpers.callMethod(mCalendar, "format", mContext, textSb, formatSb);
            		                    clock.setText(textSb.toString());
            		                    { skipped = true; result = null; throwable = null; }
            		                }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "updateTime", updateTimeHook);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiStatusBarClock", lpparam.getClassLoader(), "updateTime", updateTimeHook);
        if (hideDateView || hideDrawerDate || hideStatusbarClock) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "onAttachedToWindow", new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	Object result = null;
                	Throwable throwable = null;
                	Object thisObject = chain.getThisObject();
                	try {

                		                    TextView clock = (TextView)thisObject;
                		                    String clockName = (String) ModuleHelper.getViewInfo(clock, "clockName");
                		                    if (("ccDate".equals(clockName) && hideDateView)
                		                        || ("drawerDate".equals(clockName) && hideDrawerDate)
                		                        || ("clock".equals(clockName) && hideStatusbarClock)
                		                    ) {
                		                        XposedHelpers.setObjectField(thisObject, "mAttached", true);
                		                    }


                        result = chain.proceed();
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
        if (statusbarClockTweak) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onAttachedToWindow", new MethodHook() {
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

                		                    TextView clock = (TextView) XposedHelpers.getObjectField(thisObject, "mClock");
                		                    initClockStyle(clock, "clock");

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            boolean customTextColor = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_customtextcolor");
            boolean useMonet = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_usemonet");
            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_chip") && (customTextColor || useMonet)) {
                ModuleHelper.hookAllMethods("com.android.systemui.statusbar.views.MiuiClock", lpparam.getClassLoader(), "onDarkChanged", new MethodHook() {
                    @Override
                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    	boolean skipped = false;
                    	Object result = null;
                    	Throwable throwable = null;
                    	Object thisObject = chain.getThisObject();
                    	try {

                    		                        TextView clock = (TextView)thisObject;
                    		                        String clockName = (String) ModuleHelper.getViewInfo(clock, "clockName");
                    		                        if ("clock".equals(clockName)) {
                    		                            { skipped = true; result = null; throwable = null; }
                    		                        }

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
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.FakeStatusBarClockController", lpparam.getClassLoader(), "initState", new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	boolean skipped = false;
                	Object result = null;
                	Throwable throwable = null;
                	Object thisObject = chain.getThisObject();
                	try {

                		                    boolean useLeft = XposedHelpers.getBooleanField(thisObject, "useLeft");
                		                    if (!useLeft) {
                		                        Object mFakeClock = XposedHelpers.getObjectField(thisObject, "fakeStatusBarClock");
                		                        if (mFakeClock == null) { skipped = true; result = null; throwable = null; }
                		                    }

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
    }

    public static void CCClockTweakHook(PackageReadyParam lpparam) {
        int ccClockSize = MainModule.mPrefs.getInt("system_cc_clock_fontsize", 9);
        if (ccClockSize > 9) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "qs_control_header_clock_size", ccClockSize);
        }
        MethodHook ccClockHook = new MethodHook() {
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

            		                TextView clock = (TextView)XposedHelpers.getObjectField(thisObject, "mBigTime");
            		                boolean ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak");
            		                boolean useSystemFonts = MainModule.mPrefs.getBoolean("system_qs_force_systemfonts");
            		                if (ccClockTweak) {
            		                    int defaultVerticalOffset = 10;
            		                    int verticalOffset = MainModule.mPrefs.getInt("system_cc_clock_verticaloffset", defaultVerticalOffset);
            		                    if (verticalOffset != defaultVerticalOffset) {
            		                        float marginTop = Helpers.dp2px(verticalOffset - defaultVerticalOffset);
            		                        clock.setTranslationY(marginTop);
            		                    }
            		                }
            		                if (useSystemFonts) {
            		                    clock.setTypeface(Typeface.DEFAULT);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateResources", ccClockHook);
    }
    public static void CCClockCenterAlignHook(PackageReadyParam lpparam) {
        boolean centerClock = MainModule.mPrefs.getBoolean("system_cc_clock_centeralign");
        boolean centerDate = !MainModule.mPrefs.getBoolean("system_drawer_hidedate") && MainModule.mPrefs.getBoolean("system_drawer_date_centeralign");
        MethodHook ccClockHook = new MethodHook() {
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

            		                TextView clock = (TextView)XposedHelpers.getObjectField(thisObject, "mBigTime");
            		                int mPolicyVisibility = XposedHelpers.getIntField(clock, "mPolicyVisibility");
            		                LinearLayout clockContainer = (LinearLayout) XposedHelpers.getObjectField(thisObject, "mNotificationHeaderClockContainer");
            		                if (mPolicyVisibility == 0 || mPolicyVisibility == 4) {
            		                    clockContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            		                }
            		                else {
            		                    clockContainer.setGravity(Gravity.START);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        if (centerClock) {
            ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "updateLayout", ccClockHook);
        }
        MethodHook clockMarginHook = new MethodHook() {
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

            		                if (centerClock) {
            		                    TextView clock = (TextView)XposedHelpers.getObjectField(thisObject, "mBigTime");
            		                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) clock.getLayoutParams();
            		                    lp.leftMargin = 0;
            		                    clock.setLayoutParams(lp);

            		                    Object mWeatherCity = ModuleHelper.getObjectFieldSilently(thisObject, "mWeatherCity");
            		                    if (!ModuleHelper.NOT_EXIST_SYMBOL.equals(mWeatherCity)) {
            		                        ViewGroup weatherContainer = (ViewGroup) ((View) mWeatherCity).getParent();
            		                        weatherContainer.setVisibility(View.GONE);
            		                    }
            		                }
            		                if (centerDate) {
            		                    TextView dateView = (TextView)XposedHelpers.getObjectField(thisObject, "mDateView");
            		                    LinearLayout dateContainer = (LinearLayout) dateView.getParent();
            		                    dateContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.getClassLoader(), "onFinishInflate", clockMarginHook);
    }

    public static void ExpandNotificationsHook(PackageReadyParam lpparam) {
        String feedbackMethod = "setFeedbackIcon";
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), feedbackMethod, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                boolean mOnKeyguard = XposedHelpers.getBooleanField(thisObject, "mOnKeyguard");
            		                if (!mOnKeyguard) {
            		                    Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getEntry"), "mSbn");
            		                    String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
            		                    int opt = Integer.parseInt(MainModule.mPrefs.getString("system_expandnotifs", "1"));
            		                    boolean isSelected = MainModule.mPrefs.getStringSet("system_expandnotifs_apps").contains(pkgName);
            		                    if (opt == 2 && !isSelected || opt == 3 && isSelected)
            		                        XposedHelpers.callMethod(thisObject, "setSystemExpanded", true);
            		                }


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ExpandHeadsUpHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), "setHeadsUp", new MethodHook() {
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

            		                boolean mOnKeyguard = XposedHelpers.getBooleanField(thisObject, "mOnKeyguard");
            		                boolean showHeadsUp = (boolean) args[0];
            		                if (!mOnKeyguard && showHeadsUp) {
            		                    View notifyRow = (View) thisObject;
            		                    Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getEntry"), "mSbn");
            		                    String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
            		                    int opt = MainModule.mPrefs.getStringAsInt("system_expandheadups", 1);
            		                    boolean isSelected = MainModule.mPrefs.getStringSet("system_expandheadups_apps").contains(pkgName);
            		                    if (opt == 2 && !isSelected || opt == 3 && isSelected) {
            		                        Runnable expandNotify = new Runnable() {
            		                            @Override
            		                            public void run() {
            		                                View.OnClickListener mExpandClickListener = (View.OnClickListener) XposedHelpers.getObjectField(thisObject, "mExpandClickListener");
            		                                mExpandClickListener.onClick(notifyRow);
            		                            }
            		                        };
            		                        notifyRow.postDelayed(expandNotify, 60);
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void DrawerBlurRatioHook(PackageReadyParam lpparam) {
        final int[] mCustomBlurModifier = {0};
        ModuleHelper.hookAllConstructors("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), new MethodHook() {
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

            		                mCustomBlurModifier[0] = MainModule.mPrefs.getInt("system_drawer_blur", 100);
            		                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
            		                    public void onChange(String key) {
            		                        if (key.contains("system_drawer_blur")) {
            		                            mCustomBlurModifier[0] = MainModule.mPrefs.getInt("system_drawer_blur", 100);
            		                        }
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationShadeDepthController$updateBlurCallback$1", lpparam.getClassLoader(), "doFrame", long.class, new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                Object parentCtrl = XposedHelpers.getSurroundingThis(thisObject);
	            		                Object mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt");
	            		                XposedHelpers.setAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier", mCustomBlurModifier[0]);


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

	            		                Object parentCtrl = XposedHelpers.getSurroundingThis(thisObject);
	            		                Object mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt");
	            		                XposedHelpers.removeAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier");


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BlurUtilsExt", lpparam.getClassLoader(), "applyBlur", View.class, float.class, boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                Object multiplier = XposedHelpers.getAdditionalInstanceField(thisObject, "mCustomBlurModifier");
            		                if (multiplier != null) {
            		                    float ratio = (float) args[1];
            		                    float newRatio = ratio * (int)multiplier / 100f;
            		                    args[1] = newRatio;
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.getClassLoader(), "setBlurRatio", float.class, boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                args[0] = (float)args[0] * mCustomBlurModifier[0] / 100f;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ChargeAnimationHook(PackageReadyParam lpparam) {
        int timeout = MainModule.mPrefs.getInt("system_chargeanimtime", 20) * 1000;
        ModuleHelper.findAndHookMethod("com.miui.charge.container.MiuiChargeAnimationView", lpparam.getClassLoader(), "getAnimationDuration", HookerClassHelper.returnConstant(timeout));
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

    public static void AutoBrightnessRangeHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.AutomaticBrightnessController", lpparam.getClassLoader(), "clampScreenBrightness", float.class, new MethodHook() {
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

            		                float val = (float)result;
            		                if (val >= 0) {
            		                    float res = constrainValue(val);
            		                    { result = res; throwable = null; }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllConstructors("com.android.server.display.AutomaticBrightnessController", lpparam.getClassLoader(), new MethodHook() {
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

            		                XposedHelpers.setLongField(thisObject, "mBrighteningLightDebounceConfig", 1000L);
            		                XposedHelpers.setLongField(thisObject, "mDarkeningLightDebounceConfig", 1200L);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.getClassLoader(), "clampScreenBrightness", float.class, new MethodHook() {
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

            		                float val = (float)result;
            		                if (val >= 0) {
            		                    float res = constrainValue(val);
            		                    { result = res; throwable = null; }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllConstructors("com.android.server.display.DisplayPowerController", lpparam.getClassLoader(), new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                Resources res = Resources.getSystem();
            		                int minBrightnessLevel = res.getInteger(Helpers.getResId(res, "config_screenBrightnessSettingMinimum", "integer", "android"));
            		                int maxBrightnessLevel = res.getInteger(Helpers.getResId(res, "config_screenBrightnessSettingMaximum", "integer", "android"));
            		                int backlightBit = res.getInteger(Helpers.getResId(res, "config_backlightBit", "integer", "android.miui"));
            		                backlightMaxLevel = (1 << backlightBit) - 1;
            		                mMinimumBacklight = (minBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1);
            		                mMaximumBacklight = (maxBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void AutoBrightnessAfterScreenOffHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.getClassLoader(), "setScreenState", int.class, boolean.class, new MethodHook() {
            boolean stateChanged = false;
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                int state = (int) args[0];
	            		                boolean reportOnly = (boolean) args[1];
	            		                boolean mUseAutoBrightness = XposedHelpers.getBooleanField(thisObject, "mUseAutoBrightness");
	            		                if (state == 1  && mUseAutoBrightness && !reportOnly) {
	            		                    Object mPowerState = XposedHelpers.getObjectField(thisObject, "mPowerState");
	            		                    int mScreenState = XposedHelpers.getIntField(mPowerState, "mScreenState");
	            		                    stateChanged = state != mScreenState;
	            		                }
	            		                else {
	            		                    stateChanged = false;
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

	            		                if (stateChanged) {
	            		                    boolean readyToUpdateDisplayState = (boolean) XposedHelpers.callMethod(thisObject, "readyToUpdateDisplayState");
	            		                    if (readyToUpdateDisplayState) {
	            		                        Handler mHandler = (Handler) XposedHelpers.getObjectField(thisObject, "mHandler");
	            		                        Message msg = mHandler.obtainMessage(255);
	            		                        mHandler.sendMessage(msg);
	            		                    }
	            		                }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
    }

    public static void BetterPopupsHideDelayHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethodSilently(MiuiNotification.class, "getFloatTime", HookerClassHelper.returnConstant(0));
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), new MethodHook() {
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

            		                int delay = MainModule.mPrefs.getInt("system_betterpopups_delay", 0) * 1000;
            		                if (delay == 0) delay = 5000;
            		                XposedHelpers.setIntField(thisObject, "mMinimumDisplayTime", delay);
            		                XposedHelpers.setIntField(thisObject, "mHeadsUpNotificationDecay", delay);
            		                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
            		                    public void onChange(String key) {
            		                        if (key.contains("system_betterpopups_delay")) {
            		                            int delay = MainModule.mPrefs.getInt("system_betterpopups_delay", 0) * 1000;
            		                            if (delay == 0) delay = 5000;
            		                            XposedHelpers.setIntField(thisObject, "mMinimumDisplayTime", delay);
            		                            XposedHelpers.setIntField(thisObject, "mHeadsUpNotificationDecay", delay);
            		                        }
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void BetterPopupsNoHideHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "removeHeadsUpNotification", HookerClassHelper.DO_NOTHING);
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "removeOldHeadsUpNotification", HookerClassHelper.DO_NOTHING);

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager$HeadsUpEntry", lpparam.getClassLoader(), "updateEntry", boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                XposedHelpers.setObjectField(thisObject, "mRemoveHeadsUpRunnable", new Runnable() {
            		                    @Override
            		                    public void run() {}
            		                });


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "onExpandingFinished", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                XposedHelpers.setBooleanField(thisObject, "mReleaseOnExpandFinish", true);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

//		ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.getClassLoader(), "onReorderingAllowed", new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				XposedHelpers.log("BetterPopupsNoHideHook", "onReorderingAllowed");
//			}
//		});
    }

    public static void NoVersionCheckHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.pm.PackageManagerServiceUtils", lpparam.getClassLoader(), "checkDowngrade", HookerClassHelper.DO_NOTHING);
    }

    public static void ColorizeNotificationCardHook(PackageReadyParam lpparam) {
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

            		                if (args[1] != null) {
            		                    Notification mN = (Notification) args[1];
            		                    if (XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor") != null) {
            		                        Object builder = thisObject;
            		                        Object mParams = XposedHelpers.getObjectField(builder, "mParams");
            		                        XposedHelpers.callMethod(builder, "getColors", mParams);
            		                        Object mColors = XposedHelpers.getObjectField(builder, "mColors");
            		                        XposedHelpers.setObjectField(mColors, "mProtectionColor", XposedHelpers.getAdditionalInstanceField(mN, "mProtectionColor"));
            		                        XposedHelpers.setObjectField(mColors, "mPrimaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
            		                        XposedHelpers.setObjectField(mColors, "mSecondaryTextColor", XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow", lpparam.getClassLoader(), "updateBlurBg", int.class, int.class, boolean.class, new MethodHook() {
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

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.getClassLoader(), "onNotificationUpdated", new MethodHook() {
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

            		                Object mEntry = XposedHelpers.getObjectField(thisObject, "mEntry");
            		                if (mEntry != null) {
            		                    Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
            		                    Notification notify = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
            		                    Object overflowColor = XposedHelpers.getAdditionalInstanceField(notify, "mSecondaryTextColor");
            		                    if (overflowColor != null) {
            		                        XposedHelpers.setObjectField(thisObject, "mNotificationColor", overflowColor);
            		                    }
            		                    Object mNotifyBackgroundColor = XposedHelpers.getAdditionalInstanceField(notify, "mNotifyBackgroundColor");
            		                    if (mNotifyBackgroundColor != null) {
            		                        int bgColor = (int) mNotifyBackgroundColor;
            		                        int mCurrentBackgroundTint = XposedHelpers.getIntField(thisObject, "mCurrentBackgroundTint");
            		                        if (mCurrentBackgroundTint != bgColor) {
            		                            bgColor = Color.argb(158, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor));
            		                            XposedHelpers.callMethod(thisObject, "setBackgroundTintColor", bgColor);
            		                        }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationBackgroundView", lpparam.getClassLoader(), "setTint", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if ((int)args[0] == 0) {
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

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper", lpparam.getClassLoader(), "getCustomBackgroundColor", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                { skipped = true; result = XposedHelpers.getObjectField(thisObject, "mBackgroundColor"); throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.NotificationContentView", lpparam.getClassLoader(), "updateAllSingleLineViews", new MethodHook() {
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

            		                Object mEntry = XposedHelpers.getObjectField(thisObject, "mNotificationEntry");
            		                Object singleLineView = XposedHelpers.getObjectField(thisObject, "mSingleLineView");
            		                if (mEntry != null && singleLineView != null) {
            		                    Object mSbn = XposedHelpers.getObjectField(mEntry, "mSbn");
            		                    Notification mN = (Notification) XposedHelpers.callMethod(mSbn, "getNotification");
            		                    if (XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor") != null) {
            		                        LinearLayout hybridNotificationView = (LinearLayout) singleLineView;
            		                        TextView mTitleView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTitleView");
            		                        TextView mTextView = (TextView) XposedHelpers.getObjectField(hybridNotificationView, "mTextView");
            		                        mTitleView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mPrimaryTextColor"));
            		                        mTextView.setTextColor((int)XposedHelpers.getAdditionalInstanceField(mN, "mSecondaryTextColor"));
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "handle3thThemeColor", new MethodHook() {
            private Object sAppIconManager = null;
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Notification.Builder builder = (Notification.Builder) args[0];
            		                Notification mN = (Notification) XposedHelpers.getObjectField(builder, "mN");
                                    if ((boolean)XposedHelpers.callMethod(mN, "isColorized")) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
                                    if ((boolean)XposedHelpers.callMethod(mN, "isMediaNotification")) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                ApplicationInfo applicationInfo = mN.extras.getParcelable("android.appInfo");
            		                if (applicationInfo == null) {
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                }
            		                Context mContext = (Context) args[1];
            		                String pkgName = applicationInfo.packageName;
            		                int opt = MainModule.mPrefs.getStringAsInt("system_colorizenotifs", 1);
            		                boolean isSelected = MainModule.mPrefs.getStringSet("system_colorizenotifs_apps").contains(pkgName);
            		                if (opt == 2 && !isSelected || opt == 3 && isSelected) {
            		                    XposedHelpers.callMethod(builder, "makeNotificationGroupHeader");
            		                    if (sAppIconManager == null) {
            		                        sAppIconManager = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.miui.systemui.graphics.AppIconsManager");
            		                    }
            		                    int userId = ModuleHelper.getUserId();
            		                    Bitmap notifyIcon = (Bitmap) XposedHelpers.callMethod(sAppIconManager, "getAppIconBitmap", userId, pkgName);
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
            		                    Object paletteAccent1 = XposedHelpers.getObjectField(cs, "accent1");
            		                    List<Integer> accent1 = (List<Integer>) XposedHelpers.getObjectField(paletteAccent1, "allShades");
            		                    Object paletteN1 = XposedHelpers.getObjectField(cs, "neutral1");
            		                    List<Integer> n1 = (List<Integer>) XposedHelpers.getObjectField(paletteN1, "allShades");
            		                    Object paletteN2 = XposedHelpers.getObjectField(cs, "neutral2");
            		                    List<Integer> n2 = (List<Integer>) XposedHelpers.getObjectField(paletteN2, "allShades");

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

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector", lpparam.getClassLoader(), "createRemoteViews", new MethodHook() {
            private int titleResId = 0;
            private int subTextResId = 0;
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

            		                Class<?> NotificationHelper = findClass("com.android.systemui.statusbar.notification.NotificationSettingsHelper", lpparam.getClassLoader());
            		                boolean miuiStyle = false;
            		                Notification.Builder builder = (Notification.Builder) args[1];
            		                Notification notification = builder.getNotification();
            		                if ((boolean)XposedHelpers.callMethod(notification, "isMediaNotification")) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                boolean isFoldEntrance = notification.extras.getBoolean("miui_unimportant", false);
            		                boolean showMiuiStyle = (boolean) XposedHelpers.callStaticMethod(NotificationHelper, "showMiuiStyle");
            		                if (showMiuiStyle || isFoldEntrance) {
            		                    Notification.Style style = builder.getStyle();
            		                    miuiStyle = style == null || (style instanceof Notification.BigPictureStyle) || (style instanceof Notification.BigTextStyle) || (style instanceof Notification.InboxStyle);
            		                }
            		                if (miuiStyle) {
            		                    Object inflationProgress = result;
            		                    Context mContext = (Context) args[args.length - 1];
            		                    if (titleResId == 0) {
            		                        titleResId = Helpers.getResId(mContext.getResources(), "title", "id", "com.android.systemui");
            		                        subTextResId = Helpers.getResId(mContext.getResources(), "text", "id", "com.android.systemui");
            		                    }
            		                    List<String> contents = List.of("newPublicView", "newContentView", "newExpandedView");
            		                    for (String contentType:contents) {
            		                        RemoteViews baseContent = (RemoteViews) XposedHelpers.getObjectField(inflationProgress, contentType);
            		                        if (baseContent != null && XposedHelpers.getAdditionalInstanceField(notification, "mPrimaryTextColor") != null) {
            		                            baseContent.setTextColor(titleResId, (int)XposedHelpers.getAdditionalInstanceField(notification, "mPrimaryTextColor"));
            		                            baseContent.setTextColor(subTextResId, (int)XposedHelpers.getAdditionalInstanceField(notification, "mSecondaryTextColor"));
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

    public static void QSHapticHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.getClassLoader(), "click", View.class, new MethodHook() {
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

            		                Object mState = XposedHelpers.callMethod(thisObject, "getState");
            		                int state = XposedHelpers.getIntField(mState, "state");
            		                if (state != 0) {
            		                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                    boolean ignoreSystem = MainModule.mPrefs.getBoolean("system_qshaptics_ignore");
            		                    int opt = MainModule.mPrefs.getStringAsInt("system_qshaptics", 1);
            		                    if (opt == 2)
            		                        Helpers.performLightVibration(mContext, ignoreSystem);
            		                    else if (opt == 3)
            		                        Helpers.performStrongVibration(mContext, ignoreSystem);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ShowNotificationsAfterUnlockHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.interruption.KeyguardNotificationVisibilityProviderImpl", lpparam.getClassLoader(), "shouldHideNotification", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Object notification = XposedHelpers.getObjectField(args[0], "mSbn");
            		                XposedHelpers.setObjectField(notification, "mHasShownAfterUnlock", false);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void NotificationRowMenuHook(PackageReadyParam lpparam) {
        int appInfoIconResId = MainModule.resHooks.addFakeResource("ic_appinfo", R.drawable.ic_appinfo12, "drawable");
        int forceCloseIconResId = MainModule.resHooks.addFakeResource("ic_forceclose", R.drawable.ic_forceclose12, "drawable");
        int openInFwIconResId = MainModule.resHooks.addFakeResource("ic_openinfw", R.drawable.ic_openinfw, "drawable");
        int appInfoDescId = MainModule.resHooks.addFakeResource("miui_notification_menu_appinfo_title", R.string.system_notifrowmenu_appinfo, "string");
        int forceCloseDescId = MainModule.resHooks.addFakeResource("miui_notification_menu_forceclose_title", R.string.system_notifrowmenu_forceclose, "string");
        int openInFwDescId = MainModule.resHooks.addFakeResource("miui_notification_menu_openinfw_title", R.string.system_notifrowmenu_openinfw, "string");
        MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "notification_menu_icon_padding", 0);
        MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "miui_notification_modal_menu_margin_left_right", 3);
        MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "miui_notification_modal_menu_icon_bg_size", 50);

        Class<?> MiuiNotificationMenuItem = findClass("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow.MiuiNotificationMenuItem", lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.getClassLoader(), "createMenuViews", boolean.class, new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
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

            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                ArrayList<Object> mMenuItems = (ArrayList<Object>)XposedHelpers.getObjectField(thisObject, "mMenuItems");

            		                Object infoBtn = null;
            		                Object forceCloseBtn = null;
            		                Object openFwBtn = null;
            		                Constructor MenuItem = MiuiNotificationMenuItem.getConstructors()[0];
            		                try {
            		                    infoBtn = MenuItem.newInstance(mContext, appInfoDescId, null, appInfoIconResId);
            		                    forceCloseBtn = MenuItem.newInstance(mContext, forceCloseDescId, null, forceCloseIconResId);
            		                    openFwBtn = MenuItem.newInstance(mContext, openInFwDescId, null, openInFwIconResId);
            		                } catch (Throwable t1) {
            		                    XposedHelpers.log(t1);
            		                }
            		                if (infoBtn == null || forceCloseBtn == null || openFwBtn == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Object notification = XposedHelpers.getObjectField(thisObject, "mSbn");
            		                mMenuItems.add(infoBtn);
            		                mMenuItems.add(forceCloseBtn);
            		                mMenuItems.add(openFwBtn);
            		                int menuMargin = (int) XposedHelpers.getObjectField(thisObject, "mMenuMargin");
            		                LinearLayout mMenuContainer = (LinearLayout)XposedHelpers.getObjectField(thisObject, "mMenuContainer");
            		                String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
            		                View mInfoBtn = (View) XposedHelpers.callMethod(infoBtn, "getMenuView");
            		                View mForceCloseBtn = null;
            		                if (!"android".equals(pkgName)) {
            		                    mForceCloseBtn = (View) XposedHelpers.callMethod(forceCloseBtn, "getMenuView");
            		                }
            		                View mOpenFwBtn = (View) XposedHelpers.callMethod(openFwBtn, "getMenuView");
            		                Object expandNotifyRow = XposedHelpers.getObjectField(thisObject, "mParent");
            		                View finalMForceCloseBtn = mForceCloseBtn;
            		                View.OnClickListener itemClick = new View.OnClickListener() {
            		                    @Override
            		                    public void onClick(View view) {
            		                        if (view == null) return;
            		                        int uid = XposedHelpers.getIntField(notification, "mAppUid");
            		                        int user = 0;
            		                        try {
            		                            user = (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", uid);
            		                        } catch (Throwable t) {
            		                            XposedHelpers.log(t);
            		                        }

            		                        if (view == mInfoBtn) {
            		                            ModuleHelper.openAppInfo(mContext, pkgName, user);
            		                        } else if (view == finalMForceCloseBtn) {
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
            		                            String miniWindowPkg = (String) XposedHelpers.callMethod(expandNotifyRow, "getMiniWindowTargetPkg");
            		                            PendingIntent notifyIntent = (PendingIntent) XposedHelpers.callMethod(expandNotifyRow, "getPendingIntent");
            		                            try {
            		                                Bundle options = ModuleHelper.getFreeformOptions(mContext, miniWindowPkg, notifyIntent, true);
            		                                notifyIntent.send(mContext, 0, ModuleHelper.getFreeformIntent(miniWindowPkg), null, null, null, options);
            		                            } catch (PendingIntent.CanceledException e) {
            		                                throw new RuntimeException(e);
            		                            }
            		                        }
            		                        String ModalControllerForDep = "com.android.systemui.statusbar.notification.modal.ModalController";
            		                        Object ModalController = ModuleHelper.getDepInstance(lpparam.getClassLoader(), ModalControllerForDep);
            		                        XposedHelpers.callMethod(ModalController, "animExitModal", "OTHER");
            		                        Object mCommandQueue = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.CommandQueue");
            		                        XposedHelpers.callMethod(mCommandQueue, "animateCollapsePanels", 0, false);
            		                    }
            		                };
            		                mInfoBtn.setOnClickListener(itemClick);
            		                mOpenFwBtn.setOnClickListener(itemClick);
            		                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            		                layoutParams.leftMargin = menuMargin * 2;
            		                layoutParams.rightMargin = menuMargin * 2;
            		                mMenuContainer.addView(mInfoBtn);
            		                if (finalMForceCloseBtn != null) {
            		                    finalMForceCloseBtn.setOnClickListener(itemClick);
            		                    mMenuContainer.addView(finalMForceCloseBtn);
            		                }
            		                mMenuContainer.addView(mOpenFwBtn);
            		                int titleId = Helpers.getResId(mContext.getResources(), "modal_menu_title", "id", "com.android.systemui");
            		                int panelWidth = mContext.getResources().getDisplayMetrics().widthPixels;
            		                int menuWidth = (panelWidth / mMenuItems.size()) - (menuMargin * 2);
            		                mMenuItems.forEach(new Consumer() {
            		                    @Override
            		                    public void accept(Object obj) {
            		                        View menuView = (View) XposedHelpers.callMethod(obj, "getMenuView");
            		                        menuView.setLayoutParams(layoutParams);
            		                        ((TextView) menuView.findViewById(titleId)).setMaxWidth(menuWidth);
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void SelectiveVibrationHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.vibrator.VibratorManagerService", lpparam.getClassLoader(), "systemReady", new MethodHook() {
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

            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationMode", Integer.parseInt(MainModule.mPrefs.getString("system_vibration", "1")));
            		                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
            		                    public void onChange(String key) {
            		                        if (key.endsWith("system_vibration")) {
            		                            XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationMode", MainModule.mPrefs.getStringAsInt("system_vibration", 1));
            		                        }
            		                    }
            		                });

            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationApps", MainModule.mPrefs.getStringSet("system_vibration_apps"));
            		                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
            		                    public void onChange(String key) {
            		                        if (key.contains("system_vibration_apps")) {
            		                            XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationApps", MainModule.mPrefs.getStringSet("system_vibration_apps"));
            		                        }
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.vibrator.VibratorManagerService", lpparam.getClassLoader(), "vibrate", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                String pkgName = (String)args[1];
                                    if (pkgName == null) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                if (checkVibration(pkgName, thisObject)) { skipped = true; result = null; throwable = null; }

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

    public static void NoDuckingHook(SystemServerStartingParam lpparam) {
        //ModuleHelper.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor", lpparam.getClassLoader(), "duckPlayers", HookerClassHelper.returnConstant(true));
        //ModuleHelper.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor$DuckingManager", lpparam.getClassLoader(), "addDuck", HookerClassHelper.DO_NOTHING);
        ModuleHelper.hookAllMethods("com.android.server.audio.FocusRequester", lpparam.getClassLoader(), "handleFocusLoss", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if ((int)args[0] == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) { skipped = true; result = null; throwable = null; }

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

    public static void OrientationLockHook(SystemServerStartingParam lpparam) {
        String windowClass = "com.android.server.wm.DisplayRotation";
        String rotMethod = "rotationForOrientation";
        ModuleHelper.hookAllMethods(windowClass, lpparam.getClassLoader(), rotMethod, new MethodHook() {
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

            		                //Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                //XposedHelpers.log("rotationForOrientationLw: " + args[0] + ", " + args[1] + " = " + result);
            		                if ((int)args[0] == -1) {
            		                    int opt = MainModule.mPrefs.getInt("qs_autorotate_state", 0);
            		                    int prevOrient = (int)args[1];
            		                    if (opt == 1) {
            		                        if (prevOrient != 0 && prevOrient != 2) prevOrient = 0;
            		                        if ((int)result == 1 || (int)result == 3) { result = prevOrient; throwable = null; }
            		                    } else if (opt == 2) {
            		                        if (prevOrient != 1 && prevOrient != 3) prevOrient = 1;
            		                        if ((int)result == 0 || (int)result == 2) { result = prevOrient; throwable = null; }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void StatusBarHeightHook(PackageReadyParam lpparam) {
        int opt = MainModule.mPrefs.getInt("system_statusbarheight", 11);
        int heightDpi = opt == 11 ? 27 : opt;
        String pkgName = lpparam.getPackageName();
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height_default", heightDpi);
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height", heightDpi);
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height_portrait", heightDpi);
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height_landscape", heightDpi);
    }

//    public static void StatusBarHeightHook(SystemServerStartingParam lpparam) {
//        int opt = MainModule.mPrefs.getInt("system_statusbarheight", 11);
//        int heightDpi = opt == 11 ? 27 : opt;
//
//        MethodHook hook = new MethodHook() {
//            @Override
//            protected void before(final BeforeHookCallback param) throws Throwable {
//                int barHeight = (int)Helpers.dp2px(heightDpi);
//                param.returnAndSkip(barHeight);
//            }
//        };
//        ModuleHelper.hookAllMethods("com.android.internal.policy.SystemBarUtils", lpparam.getClassLoader(), "getStatusBarHeight", hook);
//        ModuleHelper.hookAllMethods("com.android.internal.policy.SystemBarUtils", lpparam.getClassLoader(), "getStatusBarHeightForRotation", hook);
//    }

    public static void HideMemoryCleanHook(PackageReadyParam lpparam, boolean isInLauncher) {
        String raClass = isInLauncher ? "com.miui.home.recents.views.RecentsContainer" : "com.android.systemui.recents.RecentsActivity";
        if (isInLauncher && findClassIfExists(raClass, lpparam.getClassLoader()) == null) return;
        ModuleHelper.findAndHookMethod(raClass, lpparam.getClassLoader(), "setupVisible", new MethodHook() {
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

            		            ViewGroup mMemoryAndClearContainer = (ViewGroup)XposedHelpers.getObjectField(thisObject, "mMemoryAndClearContainer");
            		            if (mMemoryAndClearContainer != null) mMemoryAndClearContainer.setVisibility(View.GONE);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static final int NOCOLOR = 0x01010101;
    private static int actionBarColor = NOCOLOR;

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
            if (mContext != null)
                ((Activity)mContext).getWindow().setStatusBarColor(actionBarColor);
        }
    }

    private static void hookWindowDecor(Object thisObject, Drawable bg) {
        if (!(bg instanceof ColorDrawable)) return;
        actionBarColor = ((ColorDrawable)bg).getColor();
        Activity mActivity = (Activity)XposedHelpers.getObjectField(thisObject, "mActivity");
        if (mActivity != null)
            mActivity.getWindow().setStatusBarColor(actionBarColor);
    }

    public static void StatusBarBackgroundHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.getClassLoader(), "generateLayout", "com.android.internal.policy.DecorView", new MethodHook() {
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

            		                Window wnd = (Window)thisObject;
            		                int mStatusBarColor = XposedHelpers.getIntField(thisObject, "mStatusBarColor");
            		                if (mStatusBarColor == -16777216) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int newColor = getActionBarColor(wnd, mStatusBarColor);
            		                if (newColor != mStatusBarColor)
            		                    XposedHelpers.callMethod(thisObject, "setStatusBarColor", newColor);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.getClassLoader(), "setStatusBarColor", int.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if (actionBarColor != NOCOLOR) args[0] = actionBarColor;
            		                else if (Color.alpha((int)args[0]) < 255) args[0] = Color.TRANSPARENT;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.app.ToolbarActionBar", lpparam.getClassLoader(), "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                hookToolbar(thisObject, (Drawable)args[0]);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.internal.app.WindowDecorActionBar", lpparam.getClassLoader(), "setBackgroundDrawable", Drawable.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                hookWindowDecor(thisObject, (Drawable)args[0]);


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void StatusBarBackgroundCompatHook(PackageReadyParam lpparam) {
        boolean androidx = false;

        // androidx
        Method sbdMethod = null;
        Class<?> tabCls = findClassIfExists("androidx.appcompat.app.ToolbarActionBar", lpparam.getClassLoader());
        if (tabCls != null) sbdMethod = findMethodExactIfExists(tabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	Object result = null;
                	Throwable throwable = null;
                	Object[] args = XposedHelpers.getArgsArray(chain);
                	Object thisObject = chain.getThisObject();
                	try {

                		                    hookToolbar(thisObject, (Drawable)args[0]);


                		result = chain.proceed(args);
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });

        sbdMethod = null;
        Class<?> wdabCls = findClassIfExists("androidx.appcompat.app.WindowDecorActionBar", lpparam.getClassLoader());
        if (wdabCls != null) sbdMethod = findMethodExactIfExists(wdabCls, "setBackgroundDrawable", Drawable.class);
        if (sbdMethod != null) androidx = true;
        if (sbdMethod != null)
            ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	Object result = null;
                	Throwable throwable = null;
                	Object[] args = XposedHelpers.getArgsArray(chain);
                	Object thisObject = chain.getThisObject();
                	try {

                		                    hookWindowDecor(thisObject, (Drawable)args[0]);


                		result = chain.proceed(args);
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
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
                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    	Object result = null;
                    	Throwable throwable = null;
                    	Object[] args = XposedHelpers.getArgsArray(chain);
                    	Object thisObject = chain.getThisObject();
                    	try {

                    		                        hookToolbar(thisObject, (Drawable)args[0]);


                    		result = chain.proceed(args);
                    	} catch (Throwable t) {
                    		throwable = t;
                    		result = null;
                    	}
                    	return XposedHelpers.throwOrReturn(throwable, result);
                    }
                });

            sbdMethod = null;
            Class<?> wdabv7Cls = findClassIfExists("android.support.v7.internal.app.WindowDecorActionBar", lpparam.getClassLoader());
            if (wdabv7Cls != null) sbdMethod = findMethodExactIfExists(wdabv7Cls, "setBackgroundDrawable", Drawable.class);
            if (sbdMethod != null)
                ModuleHelper.hookMethod(sbdMethod, new MethodHook() {
                    @Override
                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                    	Object result = null;
                    	Throwable throwable = null;
                    	Object[] args = XposedHelpers.getArgsArray(chain);
                    	Object thisObject = chain.getThisObject();
                    	try {

                    		                        hookWindowDecor(thisObject, (Drawable)args[0]);


                    		result = chain.proceed(args);
                    	} catch (Throwable t) {
                    		throwable = t;
                    		result = null;
                    	}
                    	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void SelectiveToastsHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.notification.NotificationManagerService", lpparam.getClassLoader(), "tryShowToast", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                String pkgName = (String) XposedHelpers.getObjectField(args[0], "pkg");
                                    if (pkgName == null) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                if (checkToast(pkgName)) { skipped = true; result = false; throwable = null; }

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

    public static void CleanShareMenuHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", lpparam.getClassLoader(), "run", new MethodHook() {
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

            		                Intent mOriginalIntent = (Intent)XposedHelpers.getObjectField(thisObject, "mOriginalIntent");
            		                if (mOriginalIntent == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                String action = mOriginalIntent.getAction();
            		                if (action == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                if (mOriginalIntent.getDataString() != null && mOriginalIntent.getDataString().contains(":")) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                String mAimPackageName = (String)XposedHelpers.getObjectField(thisObject, "mAimPackageName");
            		                if (mContext == null || mAimPackageName == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps");
            		                View mRootView = (View)XposedHelpers.getObjectField(thisObject, "mRootView");
            		                int appResId1 = Helpers.getResId(mContext.getResources(), "app1", "id", "android.miui");
            		                int appResId2 = Helpers.getResId(mContext.getResources(), "app2", "id", "android.miui");
            		                boolean removeOriginal = selectedApps.contains(mAimPackageName) || selectedApps.contains(mAimPackageName + "|0");
            		                boolean removeDual = selectedApps.contains(mAimPackageName + "|999");
            		                View originalApp = mRootView.findViewById(appResId1);
            		                View dualApp = mRootView.findViewById(appResId2);
            		                if (removeOriginal)dualApp.performClick();
            		                else if (removeDual) originalApp.performClick();

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void CleanShareMenuServiceHook(SystemServerStartingParam lpparam) {
        MethodHook hook = new MethodHook() {
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

            		                try {
            		                    if (args[0] == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    if (args.length < 6) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    Intent origIntent = (Intent)args[0];
            		                    String action = origIntent.getAction();
            		                    if (action == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    Intent intent = (Intent)origIntent.clone();
            		                    if (intent.getDataString() != null && intent.getDataString().contains(":")) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps");
            		                    List<ResolveInfo> resolved = (List<ResolveInfo>)result;
            		                    ResolveInfo resolveInfo;
            		                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
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
            		                    { result = resolved; throwable = null; }
            		                } catch (Throwable t) {
            		                    if (!(t instanceof BadParcelableException)) XposedHelpers.log(t);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void CleanOpenWithMenuHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", lpparam.getClassLoader(), "run", new MethodHook() {
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

            		                Intent mOriginalIntent = (Intent)XposedHelpers.getObjectField(thisObject, "mOriginalIntent");
            		                if (mOriginalIntent == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                String action = mOriginalIntent.getAction();
            		                if (!Intent.ACTION_VIEW.equals(action)) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                //if (mOriginalIntent.getDataString() != null && mOriginalIntent.getDataString().contains(":")) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                String mAimPackageName = (String)XposedHelpers.getObjectField(thisObject, "mAimPackageName");
            		                if (mContext == null || mAimPackageName == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanopenwith_apps");
            		                String mimeType = getContentType(mContext, mOriginalIntent);
            		                Pair<Boolean, Boolean> isRemove = isRemoveApp(true, mContext, mAimPackageName, selectedApps, mimeType);

            		                View mRootView = (View)XposedHelpers.getObjectField(thisObject, "mRootView");
            		                int appResId1 = Helpers.getResId(mContext.getResources(), "app1", "id", "android.miui");
            		                int appResId2 = Helpers.getResId(mContext.getResources(), "app2", "id", "android.miui");
            		                View originalApp = mRootView.findViewById(appResId1);
            		                View dualApp = mRootView.findViewById(appResId2);
            		                if (isRemove.first) dualApp.performClick();
            		                else if (isRemove.second) originalApp.performClick();

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void CleanOpenWithMenuServiceHook(SystemServerStartingParam lpparam) {
        MethodHook hook = new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
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

            		                try {
            		                    if (args[0] == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    if (args.length < 6) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    Intent origIntent = (Intent)args[0];
            		                    Intent intent = (Intent)origIntent.clone();
            		                    String action = intent.getAction();
            		                    //XposedHelpers.log(action + ": " + intent.getType() + " | " + intent.getDataString());
            		                    if (!Intent.ACTION_VIEW.equals(action)) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    String scheme = intent.getScheme();
            		                    boolean validSchemes = "http".equals(scheme) || "https".equals(scheme) || "vnd.youtube".equals(scheme);
            		                    if (intent.getType() == null && !validSchemes) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                    Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                    String mimeType = getContentType(mContext, intent);
            		                    //XposedHelpers.log("mimeType: " + mimeType);

            		                    String key = "system_cleanopenwith_apps";
            		                    Set<String> selectedApps = MainModule.mPrefs.getStringSet(key);
            		                    List<ResolveInfo> resolved = (List<ResolveInfo>)result;
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

            		                    { result = resolved; throwable = null; }
            		                } catch (Throwable t) {
            		                    if (!(t instanceof BadParcelableException)) XposedHelpers.log(t);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };

        String ActQueryService = "com.android.server.pm.ComputerEngine";
        ModuleHelper.hookAllMethods(ActQueryService, lpparam.getClassLoader(), "queryIntentActivitiesInternal", hook);
    }

    public static void AppLockHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "removeAccessControlPassLocked", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

                                    if (!"*".equals(args[1])) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                int mode = (int)XposedHelpers.callMethod(thisObject, "getAccessControlLockMode", args[0]);
            		                if (mode != 1) { skipped = true; result = null; throwable = null; }

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

    public static void AppLockTimeoutHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "addAccessControlPassForUser", String.class, int.class, new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                saveLastCheck(thisObject, (String)args[0], (int)args[1]);


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

	            		                checkLastCheck(thisObject, (int)args[1]);


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "checkAccessControlPassLocked", String.class, Intent.class, int.class, new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                saveLastCheck(thisObject, (String)args[0], (int)args[2]);


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

	            		                checkLastCheck(thisObject, (int)args[2]);


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.getClassLoader(), "activityResume", Intent.class, new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                Intent intent = (Intent)args[0];
	            		                if (intent.getComponent() != null)
	            		                    saveLastCheck(thisObject, intent.getComponent().getPackageName(), 0);


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

	            		                Intent intent = (Intent)args[0];
	            		                if (intent.getComponent() != null)
	            		                    checkLastCheck(thisObject, 0);


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
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
    public static void AudioVisualizerHook(PackageReadyParam lpparam) {
        final boolean[] screenAndDoze = {false, false};
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "onViewAttachedToWindow", View.class, new MethodHook() {
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

            		                FrameLayout mNotificationPanel = (FrameLayout) XposedHelpers.getObjectField(thisObject, "panelView");
            		                if (mNotificationPanel == null) {
            		                    XposedHelpers.log("AudioVisualizerHook", "Cannot find mNotificationPanel");
            		                    { return XposedHelpers.throwOrReturn(throwable, result); }
            		                }

            		                Context mContext = mNotificationPanel.getContext();
            		                ViewGroup visFrame = new FrameLayout(mContext);
            		                visFrame.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            		                audioViz = new AudioVisualizer(mContext);
            		                audioViz.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM));
            		                audioViz.setClickable(false);
            		                visFrame.addView(audioViz);
            		                visFrame.setClickable(false);
            		                View themebkg = mNotificationPanel.findViewById(Helpers.getResId(mContext.getResources(), "keyguard_background_layer", "id", lpparam.getPackageName()));

            		                int order = 0;
            		                if (themebkg != null) order = Math.max(order, mNotificationPanel.indexOfChild(themebkg));
            		                mNotificationPanel.addView(visFrame, order + 1);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.getClassLoader(), "start", new MethodHook() {
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

            		                Object mScreenObserver = XposedHelpers.getObjectField(thisObject, "mScreenObserver");
            		                Class<?> ScreenObserverCls = mScreenObserver.getClass();
            		                ModuleHelper.findAndHookMethod(ScreenObserverCls, "onScreenTurnedOff", new MethodHook() {
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

            		                    		                        screenAndDoze[0] = false;
            		                    		                        if (audioViz != null) audioViz.updateScreenOn(false);

            		                    	} catch (Throwable t) {
            		                    		XposedHelpers.log(t);
            		                    	}
            		                    	return XposedHelpers.throwOrReturn(throwable, result);
            		                    }
            		                });

            		                ModuleHelper.findAndHookMethod(ScreenObserverCls, "onScreenTurnedOn", new MethodHook() {
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

            		                    		                        screenAndDoze[0] = true;
            		                    		                        if (audioViz != null) audioViz.updateScreenOn(!screenAndDoze[1]);

            		                    	} catch (Throwable t) {
            		                    		XposedHelpers.log(t);
            		                    	}
            		                    	return XposedHelpers.throwOrReturn(throwable, result);
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.getClassLoader(), "updateDozingState", new MethodHook() {
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

            		                boolean mDozing = XposedHelpers.getBooleanField(thisObject, "mDozing");
            		                screenAndDoze[1] = mDozing;
            		                if (audioViz != null) audioViz.updateScreenOn(!mDozing && screenAndDoze[0]);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.KeyguardStateControllerImpl", lpparam.getClassLoader(), "notifyKeyguardState", boolean.class, boolean.class, new MethodHook() {
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

            		                boolean isKeyguardShowingNew = (boolean) args[0];
            		                if (isKeyguardShowing != isKeyguardShowingNew) {
            		                    isKeyguardShowing = isKeyguardShowingNew;
            		                    isNotificationPanelExpanded = false;
            		                    updateAudioVisualizerState((Context)XposedHelpers.getObjectField(thisObject, "mContext"));
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.getClassLoader(), "updatePanelExpanded", new MethodHook() {
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

            		                boolean isNotificationPanelExpandedNew = XposedHelpers.getBooleanField(thisObject, "mPanelExpanded");
            		                if (isNotificationPanelExpanded != isNotificationPanelExpandedNew) {
            		                    isNotificationPanelExpanded = isNotificationPanelExpandedNew;
            		                    FrameLayout mNotificationPanel = (FrameLayout) XposedHelpers.getObjectField(thisObject, "panelView");
            		                    updateAudioVisualizerState(mNotificationPanel.getContext());
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.getClassLoader(), "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

                                    if (audioViz == null) { return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                if (!screenAndDoze[0] || screenAndDoze[1]) {
            		                    audioViz.updateScreenOn(false);
                                        { return XposedHelpers.proceedOrThrow(chain, throwable); }
            		                } else audioViz.isScreenOn = true;

            		                MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(thisObject, "mMediaMetadata");
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

            		                mMediaController = (MediaController)XposedHelpers.getObjectField(thisObject, "mMediaController");
            		                updateAudioVisualizerState(mContext);
            		                audioViz.updateMusicArt(art);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static String audioFocusPkg = null;

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

    public static void NoCallInterruptionHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.audio.AudioService", lpparam.getClassLoader(), "requestAudioFocus", new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	boolean skipped = false;
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	__beforeBody__: {
                        		try {


	            		                if ("AudioFocus_For_Phone_Ring_And_Calls".equals(args[4]) && audioFocusPkg != null && MainModule.mPrefs.getStringSet("system_ignorecalls_apps").contains(audioFocusPkg))
	            		                    { skipped = true; result = 1; throwable = null; }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
                        	try {
                        		result = chain.proceed(args);
                        	} catch (Throwable t) {
                        		throwable = t;
                        		result = null;
                        	}
                        	__afterBody__: {
                        		try {

	            		                int res = (int)result;
	            		                if (res != AudioManager.AUDIOFOCUS_REQUEST_FAILED && !"AudioFocus_For_Phone_Ring_And_Calls".equals(args[4]))
	            		                    audioFocusPkg = (String)args[5];


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });

        ModuleHelper.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.getClassLoader(), "notifyCallState", int.class, String.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                removeListener(thisObject);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.getClassLoader(), "notifyCallStateForPhoneId", int.class, int.class, int.class, String.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                removeListener(thisObject);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void AllRotationsHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.server.wm.DisplayRotation", lpparam.getClassLoader(), new MethodHook() {
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

            		                XposedHelpers.setIntField(thisObject, "mAllowAllRotations", MainModule.mPrefs.getStringAsInt("system_allrotations2", 1) == 2 ? 1 : 0);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideIconsBattery1Hook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "updateAll", new MethodHook() {
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

            		                ImageView mBatteryIconView = (ImageView)XposedHelpers.getObjectField(thisObject, "mBatteryIconView");
            		                mBatteryIconView.setVisibility(View.GONE);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideIconsBattery2Hook(PackageReadyParam lpparam) {
        boolean hideNormalPercentage = MainModule.mPrefs.getBoolean("system_statusbaricons_battery2");
        int batteryId = ResourceHooks.getFakeResId("batterview_in_statusbar");
        if (hideNormalPercentage) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

                		                    View mBatteryView = (View) XposedHelpers.getObjectField(thisObject, "mBattery");
                		                    mBatteryView.setTag(batteryId, true);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

                		                    ViewGroup mSystemIconsContainer = (ViewGroup) XposedHelpers.getObjectField(thisObject, "mSystemIconsContainer");
                		                    int batteryResId = Helpers.getResId(mSystemIconsContainer.getResources(), "battery", "id", "com.android.systemui");
                		                    View mBatteryView = mSystemIconsContainer.findViewById(batteryResId);
                		                    mBatteryView.setTag(batteryId, true);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.getClassLoader(), "updateChargeAndText", new MethodHook() {
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

            		                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery4")) {
            		                    TextView mBatteryPercentMarkView = (TextView)XposedHelpers.getObjectField(thisObject, "mBatteryPercentMarkView");
            		                    mBatteryPercentMarkView.setVisibility(View.GONE);
            		                }
            		                if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery3")) {
            		                    ImageView mBatteryChargingView = (ImageView)XposedHelpers.getObjectField(thisObject, "mBatteryChargingView");
            		                    mBatteryChargingView.setVisibility(View.GONE);
            		                    try {
            		                        ImageView mBatteryChargingInView = (ImageView)XposedHelpers.getObjectField(thisObject, "mBatteryChargingInView");
            		                        mBatteryChargingInView.setVisibility(View.GONE);
            		                    } catch (Throwable ignore) {}
            		                }
            		                if (hideNormalPercentage) {
            		                    View mBatteryView = (View) thisObject;
            		                    if (mBatteryView.getTag(batteryId) != null) {
            		                        View percentView = (View)XposedHelpers.getObjectField(thisObject, "mBatteryPercentMarkView");
            		                        percentView.setVisibility(View.GONE);
            		                        percentView = (View)XposedHelpers.getObjectField(thisObject, "mBatteryPercentView");
            		                        percentView.setVisibility(View.GONE);
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static boolean lastState = false;
    private static long mNextAlarmTime = 0L;
    private static void updateAlarmVisibility(Object thisObject) {
        try {
            Object mIconController = XposedHelpers.getObjectField(thisObject, "mIconController");
            if (!lastState) {
                XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", false);
                return;
            }

            Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            long nowTime = java.lang.System.currentTimeMillis();
            long nextTime = mNextAlarmTime;
            if (nextTime == 0) {
                nextTime = ModuleHelper.getNextMIUIAlarmTime(mContext);
            }
            if (nextTime == 0) nextTime = Helpers.getNextStockAlarmTime(mContext);

            long diffMSec = nextTime - nowTime;
            if (diffMSec < 0) diffMSec += 7 * 24 * 60 *60 * 1000;
            float diffHours = (diffMSec - 59 * 1000) / (1000f * 60f * 60f);
            boolean vis = diffHours <= MainModule.mPrefs.getInt("system_statusbaricons_alarmn", 0);
            XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", vis);
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
    }

    public static void HideIconsSelectiveAlarmHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.getClassLoader(), new MethodHook() {
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


            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                IntentFilter filter = new IntentFilter();
            		                filter.addAction("android.intent.action.TIME_TICK");
            		                filter.addAction("android.intent.action.TIME_SET");
            		                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            		                filter.addAction("android.intent.action.LOCALE_CHANGED");
            		                mContext.registerReceiver(new BroadcastReceiver() {
            		                    @Override
            		                    public void onReceive(Context context, Intent intent) {
            		                        updateAlarmVisibility(thisObject);
            		                    }
            		                }, filter, Context.RECEIVER_NOT_EXPORTED);

            		                Object mNextAlarmCallback = XposedHelpers.getObjectField(thisObject, "mNextAlarmCallback");
            		                ModuleHelper.findAndHookMethod(mNextAlarmCallback.getClass(), "onAlarmChanged", boolean.class, new MethodHook() {
            		                    @Override
            		                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            		                    	boolean skipped = false;
            		                    	Object result = null;
            		                    	Throwable throwable = null;
            		                    	Object[] args = XposedHelpers.getArgsArray(chain);
            		                    	Object thisObject = chain.getThisObject();
            		                    	try {

            		                    		                        lastState = (boolean) args[0];
            		                    		                        mNextAlarmTime = ModuleHelper.getNextMIUIAlarmTime(mContext);
            		                    		                        updateAlarmVisibility(thisObject);
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
            		                ModuleHelper.findAndHookMethod(mNextAlarmCallback.getClass(), "onNextAlarmChanged", AlarmManager.AlarmClockInfo.class, new MethodHook() {
            		                    @Override
            		                                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            		                    	boolean skipped = false;
            		                    	Object result = null;
            		                    	Throwable throwable = null;
            		                    	Object[] args = XposedHelpers.getArgsArray(chain);
            		                    	Object thisObject = chain.getThisObject();
            		                    	try {

            		                    		                        if (args[0] == null) {
            		                    		                            lastState = false;
            		                    		                        }
            		                    		                        mNextAlarmTime = ModuleHelper.getNextMIUIAlarmTime(mContext);
            		                    		                        updateAlarmVisibility(thisObject);
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

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void DisplayWifiStandardHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.getClassLoader(), "applyWifiState", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Object wifiState = args[0];
            		                if (wifiState != null) {
            		                    int opt = MainModule.mPrefs.getStringAsInt("system_statusbaricons_wifistandard", 1);
                                        if (opt == 1) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                    int wifiStandard = (int) XposedHelpers.getObjectField(wifiState, "wifiStandard");
            		                    XposedHelpers.setObjectField(wifiState, "showWifiStandard", opt == 2 && wifiStandard > 0);
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

    public static void ForceCloseHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
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

            		                HashSet<String> mSystemKeyPackages = (HashSet<String>)XposedHelpers.getObjectField(thisObject, "mSystemKeyPackages");
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

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void DisableAnyNotificationBlockHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "isBlockable", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "setBlockable", boolean.class, new MethodHook() {
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
    }
    public static void DisableAnyNotificationBlockHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "isBlockable", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.getClassLoader(), "setBlockable", boolean.class, new MethodHook() {
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
    }

    public static void DisableAnyNotificationHook(PackageReadyParam lpparam) {
        if (lpparam.getPackageName().contains("systemui")) {
            Class<?> NotifyManagerCls = findClass("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.getClassLoader());
            XposedHelpers.setStaticBooleanField(NotifyManagerCls, "USE_WHITE_LISTS", false);
        }
        ModuleHelper.hookAllMethods("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "isNotificationForcedEnabled", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "isNotificationForcedFor", Context.class, String.class, HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "canSystemNotificationBeBlocked", String.class, HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "containNonBlockableChannel", String.class, HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.getClassLoader(), "getNotificationForcedEnabledList", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                { skipped = true; result = new HashSet<String>(); throwable = null; }

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

    public static void NotificationImportanceHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.settings.notification.BaseNotificationSettings", lpparam.getClassLoader(), "setPrefVisible", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Object pref = args[0];
            		                if (pref != null) {
            		                    String prefKey = (String) XposedHelpers.callMethod(pref, "getKey");
            		                    if ("importance".equals(prefKey)) {
            		                        args[1] = true;
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
        ModuleHelper.findAndHookMethod("com.android.settings.notification.ChannelNotificationSettings", lpparam.getClassLoader(), "setupChannelDefaultPrefs", new MethodHook() {
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

            		                Object pref = XposedHelpers.callMethod(thisObject, "findPreference", "importance");
            		                XposedHelpers.setObjectField(thisObject, "mImportance", pref);
            		                int mBackupImportance = (int)XposedHelpers.getObjectField(thisObject, "mBackupImportance");
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
            		                                XposedHelpers.setObjectField(thisObject, "mBackupImportance", mBackupImportance);
            		                                NotificationChannel mChannel = (NotificationChannel) XposedHelpers.getObjectField(thisObject, "mChannel");
            		                                mChannel.setImportance(mBackupImportance);
            		                                XposedHelpers.callMethod(mChannel, "lockFields", 4);
            		                                Object mBackend = XposedHelpers.getObjectField(thisObject, "mBackend");
            		                                String mPkg = (String) XposedHelpers.getObjectField(thisObject, "mPkg");
            		                                int mUid = (int) XposedHelpers.getObjectField(thisObject, "mUid");
            		                                XposedHelpers.callMethod(mBackend, "updateChannel", mPkg, mUid, mChannel);
            		                                XposedHelpers.callMethod(thisObject, "updateDependents", false);
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

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideProximityWarningHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.getClassLoader(), "showHint", HookerClassHelper.DO_NOTHING);
        ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.getClassLoader(), "prepareHintWindow", HookerClassHelper.DO_NOTHING);
    }

    public static void HideLockScreenClockHook(PackageReadyParam lpparam) {
        final boolean[] mToAod = {false};
        MethodHook hideClockHook = new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                int visibility = (int)args[0];
            		                if (visibility == View.VISIBLE && !mToAod[0]) {
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
        };
        XposedHelpers.findAndHookMethod("com.android.keyguard.clock.KeyguardClockContainer", lpparam.getClassLoader(), "setVisibility", int.class, hideClockHook);
        XposedHelpers.findAndHookMethod("com.android.keyguard.clock.KeyguardClockContainer", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

            		                XposedHelpers.callMethod(thisObject, "setVisibility", View.GONE);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        XposedHelpers.findAndHookMethod("com.android.keyguard.clock.KeyguardClockContainer", lpparam.getClassLoader(), "doAnimationToAod", boolean.class, boolean.class, new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                mToAod[0] = (boolean)args[0];
	            		                if (mToAod[0]) {
	            		                    XposedHelpers.callMethod(thisObject, "setVisibility", View.VISIBLE);
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

	            		                boolean mToAod = (boolean)args[0];
	            		                if (!mToAod) {
	            		                    XposedHelpers.callMethod(thisObject, "setVisibility", View.GONE);
	            		                }


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
    }

    public static void FirstVolumePressHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.audio.AudioService$VolumeController", lpparam.getClassLoader(), "suppressAdjustment", int.class, int.class, boolean.class, new MethodHook() {
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

            		                int streamType = (int)args[0];
            		                if (streamType != AudioManager.STREAM_MUSIC) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                boolean isMuteAdjust = (boolean)args[2];
            		                if (isMuteAdjust) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Object mController = XposedHelpers.getObjectField(thisObject, "mController");
            		                if (mController == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                { result = false; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void DisableSystemIntegrityHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("android.util.apk.ApkSignatureVerifier", lpparam.getClassLoader(), "getMinimumSignatureSchemeVersionForTargetSdk", int.class, HookerClassHelper.returnConstant(1));
    }

    public static void NoSignatureVerifyServiceHook(SystemServerStartingParam lpparam) {
        Class <?> SignDetails = findClassIfExists("android.content.pm.SigningDetails", lpparam.getClassLoader());
        Object signUnknown = XposedHelpers.getStaticObjectField(SignDetails, "UNKNOWN");
        ModuleHelper.hookAllMethods(SignDetails, "checkCapability", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                if (thisObject == signUnknown || args[0] == signUnknown) {
            		                    { skipped = true; result = false; throwable = null; }
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                }
            		                int flags = (int) args[1];
            		                if (flags != 4) { skipped = true; result = true; throwable = null; }

            		if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); }
            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllConstructors("android.util.jar.StrictJarVerifier", lpparam.getClassLoader(), new MethodHook() {
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

            		                XposedHelpers.setObjectField(thisObject, "signatureSchemeRollbackProtectionsEnforced", false);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.getClassLoader(), "verifyMessageDigest", HookerClassHelper.returnConstant(true));
        ModuleHelper.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.getClassLoader(), "verify", HookerClassHelper.returnConstant(true));
        ModuleHelper.hookAllMethods("com.android.server.pm.PackageManagerServiceUtils", lpparam.getClassLoader(), "verifySignatures", HookerClassHelper.returnConstant(false));
        ModuleHelper.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.getClassLoader(), "doesSignatureMatchForPermissions", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                String packageName = (String) XposedHelpers.callMethod(args[1], "getPackageName");
            		                String sourcePackageName = (String) args[0];
            		                if (sourcePackageName.equals(packageName)) {
            		                    { skipped = true; result = true; throwable = null; }
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
        ModuleHelper.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.getClassLoader(), "cannotInstallWithBadPermissionGroups", HookerClassHelper.returnConstant(false));
        ModuleHelper.hookAllMethods("com.android.server.pm.permission.PermissionManagerServiceImpl", lpparam.getClassLoader(), "shouldGrantPermissionBySignature", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                boolean isSystem = (boolean) XposedHelpers.callMethod(args[0], "isSystem");
            		                if (isSystem) {
            		                    { skipped = true; result = true; throwable = null; }
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
        ModuleHelper.findAndHookMethod("android.content.pm.ApplicationInfo", lpparam.getClassLoader(), "isSignedWithPlatformKey", new MethodHook() {
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

            		                boolean isSystemSign = (boolean) result;
            		                if (!isSystemSign) {
            		                    int flags = XposedHelpers.getIntField(thisObject, "flags");
            		                    { result = (flags & 1) != 0 || (flags & 128) != 0; throwable = null; }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ScreenDimTimeHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.getClassLoader(), "readConfigurationLocked", new MethodHook() {
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

            		                float opt = MainModule.mPrefs.getInt("system_dimtime", 0) / 100f;
            		                XposedHelpers.setIntField(thisObject, "mMaximumScreenDimDurationConfig", 600000);
            		                XposedHelpers.setFloatField(thisObject, "mMaximumScreenDimRatioConfig", opt);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void NoOverscrollAppHook(PackageReadyParam lpparam) {
        MethodHook hookParam = new MethodHook() {
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
        };

        Class<?> sblCls = findClassIfExists("miuix.springback.view.SpringBackLayout", lpparam.getClassLoader());
        if (sblCls != null) {
            ModuleHelper.hookAllConstructors(sblCls, new MethodHook() {
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

                		                    try {
                		                        XposedHelpers.callMethod(thisObject, "setSpringBackEnable", false);
                		                    } catch (Throwable t) {
                		                        try { XposedHelpers.setBooleanField(thisObject, "mSpringBackEnable", false); } catch (Throwable ignore) {}
                		                    }

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            ModuleHelper.findAndHookMethodSilently(sblCls, "setSpringBackEnable", boolean.class, hookParam);
        }

        Class<?> rrvCls = findClassIfExists("androidx.recyclerview.widget.RemixRecyclerView", lpparam.getClassLoader());
        if (rrvCls != null) {
            ModuleHelper.hookAllConstructors(rrvCls, new MethodHook() {
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

                		                    ((View)thisObject).setOverScrollMode(View.OVER_SCROLL_NEVER);
                		                    try {
                		                        XposedHelpers.callMethod(thisObject, "setSpringEnabled", false);
                		                    } catch (Throwable t) {
                		                        try { XposedHelpers.setBooleanField(thisObject, "mSpringEnabled", false); } catch (Throwable ignore) {}
                		                    }

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
            ModuleHelper.findAndHookMethodSilently(rrvCls, "setSpringEnabled", boolean.class, hookParam);
        }

        ModuleHelper.findAndHookMethod("android.widget.AbsListView", lpparam.getClassLoader(), "initAbsListView", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                ((AbsListView)thisObject).setOverScrollMode(View.OVER_SCROLL_NEVER);


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void RemoveSecureHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowState", lpparam.getClassLoader(), "isSecureLocked", HookerClassHelper.returnConstant(false));
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowSurfaceController", lpparam.getClassLoader(), "setSecure", boolean.class, new MethodHook() {
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
        ModuleHelper.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.getClassLoader(), new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                int flags = (int) args[2];
            		                int secureFlag = 128;
            		                flags &= ~secureFlag;
            		                args[2] = flags;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.hookAllMethods("com.android.server.wm.WindowManagerServiceImpl", lpparam.getClassLoader(), "notAllowCaptureDisplay", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	try {

            		                { skipped = true; result = false; throwable = null; }

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

    public static void RemoveActStartConfirmHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.server.SecurityManagerService$LocalService", lpparam.getClassLoader(), "checkAllowStartActivity", HookerClassHelper.returnConstant(true));
    }

    public static void AllowAllKeyguardHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.getClassLoader(), "isEnableKeyguard", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.getClassLoader(), "canShowOnKeyguard", Context.class, String.class, String.class, HookerClassHelper.returnConstant(true));
    }

    public static void AllowAllFloatHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.getClassLoader(), "isEnableFloat", HookerClassHelper.returnConstant(true));
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.getClassLoader(), "canFloat", Context.class, String.class, String.class, HookerClassHelper.returnConstant(true));
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat("H:m", Locale.ENGLISH);
    public static void MuffledVibrationHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.VibratorService", lpparam.getClassLoader(), "doVibratorOn", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	Object thisObject = chain.getThisObject();
            	try {

            		                float ratio_ringer = MainModule.mPrefs.getInt("system_vibration_amp_ringer", 100) / 100f;
            		                float ratio_notif = MainModule.mPrefs.getInt("system_vibration_amp_notif", 100) / 100f;
            		                float ratio_other = MainModule.mPrefs.getInt("system_vibration_amp_other", 100) / 100f;

            		                boolean isRingtone = false;
            		                boolean isNotification = false;
            		                Object mCurrentVibration = XposedHelpers.getObjectField(thisObject, "mCurrentVibration");
            		                if (mCurrentVibration != null) try {
            		                    isRingtone = (boolean)XposedHelpers.callMethod(mCurrentVibration, "isRingtone");
            		                    isNotification = (boolean)XposedHelpers.callMethod(mCurrentVibration, "isNotification");
            		                } catch (Throwable t) {
            		                    int mUsageHint = XposedHelpers.getIntField(mCurrentVibration, "mUsageHint");
            		                    isRingtone = mUsageHint == 6;
            		                    isNotification = mUsageHint == 5 || mUsageHint == 7 || mUsageHint == 8 || mUsageHint == 9;
            		                }

                                        float ratio;
                                        if (isRingtone) {
                                            ratio = ratio_ringer;
                                        } else if (isNotification) {
                                            ratio = ratio_notif;
                                        } else {
                                            ratio = ratio_other;
                                        }
                                        if (ratio == 1.0f) {
                                            return XposedHelpers.proceedOrThrow(chain, args, throwable);
                                        }

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
                                    if (!insidePeriod) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }

            		                boolean mSupportsAmplitudeControl = false;
            		                try {
            		                    mSupportsAmplitudeControl = XposedHelpers.getBooleanField(thisObject, "mSupportsAmplitudeControl");
            		                } catch (Throwable ignored) {}

            		                if (mSupportsAmplitudeControl)
            		                    args[1] = Math.round(((int)args[1] == -1 ? XposedHelpers.getIntField(thisObject, "mDefaultVibrationAmplitude") : (int)args[1]) * ratio);
            		                else
            		                    args[0] = Math.max(3, (long)Math.round((long)args[0] * ratio));


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static void hookUpdateTime(TextView alarmTime) {
        try {
            Context mContext = alarmTime.getContext();
            long timestamp = ModuleHelper.getNextMIUIAlarmTime(mContext);
            if (timestamp == 0 && MainModule.mPrefs.getBoolean("system_lsalarm_all"))
                timestamp = Helpers.getNextStockAlarmTime(mContext);
            if (timestamp == 0) {
                alarmTime.setText("");
                return;
            }

            StringBuilder alarmStr = new StringBuilder();
            alarmStr.append(ModuleHelper.getModuleRes(mContext).getString(R.string.system_statusbaricons_alarm_title)).append(": ");
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
            alarmTime.setText(alarmStr);
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
    }

    public static void LockScreenAlarmHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.KeyguardIndicationController", lpparam.getClassLoader(), "setIndicationArea", ViewGroup.class, new MethodHook() {
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

            		                TextView mTopIndicationView = (TextView) XposedHelpers.getObjectField(thisObject, "mTopIndicationView");
            		                mTopIndicationView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            		                mTopIndicationView.setVisibility(View.VISIBLE);
            		                Class<?> MiuiGxzwUtils = findClassIfExists("com.miui.keyguard.biometrics.fod.MiuiGxzwUtils", lpparam.getClassLoader());
            		                boolean hasUdfs = true;
            		                if (MiuiGxzwUtils != null) {
            		                    boolean isGxzwLowPosition = (boolean) XposedHelpers.callStaticMethod(MiuiGxzwUtils, "isGxzwLowPosition");
            		                    hasUdfs = isGxzwLowPosition;
            		                }
            		                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mTopIndicationView.getLayoutParams();
            		                layoutParams.bottomMargin = (int) Helpers.dp2px(hasUdfs ? 80 : 20);
            		                mTopIndicationView.setLayoutParams(layoutParams);
            		                ColorStateList mInitialTextColorState = (ColorStateList) XposedHelpers.getObjectField(thisObject, "mInitialTextColorState");
            		                mTopIndicationView.setTextColor(mInitialTextColorState);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.KeyguardIndicationController", lpparam.getClassLoader(), "updateDeviceEntryIndication", boolean.class, new MethodHook() {
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

            		                TextView mTopIndicationView = (TextView) XposedHelpers.getObjectField(thisObject, "mTopIndicationView");
            		                hookUpdateTime(mTopIndicationView);
            		                ColorStateList mInitialTextColorState = (ColorStateList) XposedHelpers.getObjectField(thisObject, "mInitialTextColorState");
            		                mTopIndicationView.setTextColor(mInitialTextColorState);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("com.android.keyguard.injector.KeyguardBottomAreaInjector", lpparam.getClassLoader(), "handleBottomButtonClickedAnimation", boolean.class, new MethodHook() {
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

            		                TextView mTopIndicationView = (TextView) ModuleHelper.getObjectFieldByPath(thisObject, "mKeyguardIndicationInjector.mKeyguardIndicationController.mTopIndicationView");
            		                boolean showTips = (boolean) args[0];
            		                if (showTips) {
            		                    mTopIndicationView.setVisibility(View.GONE);
            		                }
            		                else {
            		                    mTopIndicationView.setVisibility(View.VISIBLE);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ScreenshotConfigHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("android.content.ContentResolver", lpparam.getClassLoader(), "update", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

                                    if (args.length != 4) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                ContentValues contentValues = (ContentValues) args[1];
            		                String displayName = contentValues.getAsString("_display_name");
            		                if (displayName != null && displayName.contains("Screenshot")) {
            		                    int format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2);
            		                    String ext = format <= 2 ? ".jpg" : (format == 3 ? ".png" : ".webp");

            		                    displayName = displayName.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext;
            		                    contentValues.put("_display_name", displayName);
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("android.content.ContentResolver", lpparam.getClassLoader(), "insert", Uri.class, ContentValues.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                Uri imgUri = (Uri) args[0];
            		                ContentValues contentValues = (ContentValues) args[1];
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


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        int format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2);
        if (format > 2) {
            MethodData methodData = XposedHelpers.bridge.findMethod(FindMethod.create()
                .excludePackages("android", "androidx", "com.xiaomi", "com.google.json", "kotlin", "kotlinx.coroutines", "miuix")
                .matcher(MethodMatcher.create().usingStrings("saveBitmapToUri: external storage"))
            ).firstOrThrow(() -> new RuntimeException("Method not found"));

            MethodHook changeFormatHook = new MethodHook() {
                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                	Object result = null;
                	Throwable throwable = null;
                	Object[] args = XposedHelpers.getArgsArray(chain);
                	try {

                                            if (args.length < 7) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
                		                    Bitmap.CompressFormat compress = format <= 2 ? Bitmap.CompressFormat.JPEG : (format == 3 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
                		                    args[4] = compress;


                		result = chain.proceed(args);
                	} catch (Throwable t) {
                		throwable = t;
                		result = null;
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            };
            try {
                Method method = methodData.getMethodInstance(lpparam.getClassLoader());
                ModuleHelper.hookMethod(method, changeFormatHook);
            }
            catch (Throwable ign) {
            }
        }

        ModuleHelper.hookAllMethods("android.graphics.Bitmap", lpparam.getClassLoader(), "compress", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                int quality = (int) args[1];
                                    if (quality != 100 || (args[2] instanceof ByteArrayOutputStream)) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                int format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2);
            		                quality = MainModule.mPrefs.getInt("system_screenshot_quality", 100);
            		                if (format == 3) {
            		                    quality = 100;
            		                }
            		                Bitmap.CompressFormat compress = format <= 2 ? Bitmap.CompressFormat.JPEG : (format == 3 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
            		                args[0] = compress;
            		                args[1] = quality;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ToastTimeHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.notification.NotificationManagerService", lpparam.getClassLoader(), "showNextToastLocked", new MethodHook() {
            @Override
            @SuppressWarnings("unchecked")
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

            		                Context mContext = (Context)XposedHelpers.callMethod(thisObject, "getContext");
            		                Handler mHandler = (Handler)XposedHelpers.getObjectField(thisObject, "mHandler");
            		                ArrayList<Object> mToastQueue = (ArrayList<Object>)XposedHelpers.getObjectField(thisObject, "mToastQueue");
            		                if (mContext == null || mHandler == null || mToastQueue == null || mToastQueue.size() == 0) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int mod = (MainModule.mPrefs.getInt("system_toasttime", 0) - 4) * 1000;
            		                for (Object record: mToastQueue)
            		                    if (record != null && mHandler.hasMessages(2, record)) {
            		                        mHandler.removeCallbacksAndMessages(record);
            		                        int duration = XposedHelpers.getIntField(record, "duration");
            		                        int delay = Math.max(1000, (duration == 1 ? 3500 : 2000) + mod);
            		                        mHandler.sendMessageDelayed(Message.obtain(mHandler, 2, record), delay);
            		                    }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        String windowClass = "com.android.server.wm.DisplayPolicy";
        ModuleHelper.hookAllMethods(windowClass, lpparam.getClassLoader(), "adjustWindowParamsLw", new MethodHook() {
                        @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
                        	Object result = null;
                        	Throwable throwable = null;
                        	Object[] args = XposedHelpers.getArgsArray(chain);
                        	Object thisObject = chain.getThisObject();
                        	__beforeBody__: {
                        		try {


	            		                Object lp = args.length == 1 ? args[0] : args[1];
	            		                XposedHelpers.setAdditionalInstanceField(thisObject, "mPrevHideTimeout", XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds"));


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

	            		                Object lp = args.length == 1 ? args[0] : args[1];
	            		                long mPrevHideTimeout = (long)XposedHelpers.getAdditionalInstanceField(thisObject, "mPrevHideTimeout");
	            		                long mHideTimeout = XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds");
	            		                if (mPrevHideTimeout == -1 || mHideTimeout == -1) { return XposedHelpers.throwOrReturn(throwable, result); }

	            		                long dur = 0;
	            		                if (mPrevHideTimeout == 1000 || mPrevHideTimeout == 4000 || mPrevHideTimeout == 5000 || mPrevHideTimeout == 7000 || mPrevHideTimeout != mHideTimeout)
	            		                    dur = Math.max(1000, 3500 + (MainModule.mPrefs.getInt("system_toasttime", 0) - 4) * 1000);
	            		                if (dur != 0) XposedHelpers.setLongField(lp, "hideTimeoutMilliseconds", dur);


                        		} catch (Throwable t) {
                        			XposedHelpers.log(t);
                        		}
                        	}
                        	return XposedHelpers.throwOrReturn(throwable, result);
                        }
        });
    }

    public static void ClearAllTasksHook(SystemServerStartingParam lpparam) {
        String wpuClass = "com.android.server.wm.WindowProcessUtils";
        ModuleHelper.hookAllMethods(wpuClass, lpparam.getClassLoader(), "getPerceptibleRecentAppList", new MethodHook() {
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

            		                { result = null; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void TapToUnlockHook(PackageReadyParam lpparam) {
        Class<?> NotificationPanelController = findClassIfExists("com.android.systemui.shade.NotificationPanelViewController", lpparam.getClassLoader());
        if (NotificationPanelController == null) {
            XposedHelpers.log("NotificationPanelController not found");
            return;
        }

        Field mTouchHandlerField = XposedHelpers.findField(NotificationPanelController, "mTouchHandler");
        XposedHelpers.findAndHookMethod(mTouchHandlerField.getType(), "handleMiuiTouch", MotionEvent.class, new MethodHook() {
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

            		                MotionEvent event = (MotionEvent)args[0];
            		                if (event.getPointerCount() > 1) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                int action = event.getActionMasked();
            		                if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Object thisObect = XposedHelpers.getSurroundingThis(thisObject);
            		                boolean isOnKeyguard = (boolean) XposedHelpers.callMethod(thisObect, "isOnKeyguard");
            		                Object mQsController = XposedHelpers.getObjectField(thisObect, "mQsController");
            		                boolean mExpanded = XposedHelpers.getBooleanField(mQsController, "mExpanded");
            		                if (isOnKeyguard && !mExpanded) {
            		                    if (action == MotionEvent.ACTION_UP) {
            		                        Object mKeyguardPanelViewInjector = XposedHelpers.getObjectField(thisObect, "mKeyguardPanelViewInjector");
            		                        Object mKeyguardMoveHelper = XposedHelpers.getObjectField(mKeyguardPanelViewInjector, "mKeyguardMoveHelper");
            		                        int mCurrentScreen = XposedHelpers.getIntField(mKeyguardMoveHelper, "mCurrentScreen");
            		                        if (mCurrentScreen == 0) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                        Object keyguardBottomAreaInjector = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.keyguard.injector.KeyguardBottomAreaInjector");
            		                        if (!XposedHelpers.getBooleanField(keyguardBottomAreaInjector, "mTouchAtKeyguardBottomArea")) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                        Context mContext = (Context) XposedHelpers.getObjectField(keyguardBottomAreaInjector, "mContext");
            		                        float mTouchDownX = XposedHelpers.getFloatField(keyguardBottomAreaInjector, "mTouchDownX");
            		                        float mTouchDownY = XposedHelpers.getFloatField(keyguardBottomAreaInjector, "mTouchDownY");
            		                        int slop = ViewConfiguration.get(mContext).getScaledTouchSlop();
            		                        if (Math.abs(event.getX() - mTouchDownX) > slop || Math.abs(event.getY() - mTouchDownY) > slop)
            		                            { return XposedHelpers.throwOrReturn(throwable, result); }
            		                        Object statusBarKeyguardViewManager = XposedHelpers.getObjectField(thisObect, "statusBarKeyguardViewManager");
            		                        XposedHelpers.callMethod(statusBarKeyguardViewManager, "showBouncer", true);
            		                        { result = true; throwable = null; }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void TempHideOverlayAppHook(XposedModuleInterface.SystemServerStartingParam lpparam) {
        final int flagIndex = 2;
        ModuleHelper.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.getClassLoader(), new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                int windowType = (int) args[4];
            		                if (windowType != WindowManager.LayoutParams.TYPE_PHONE
            		                    && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            		                    && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                                        && windowType != WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                int flags = (int) args[flagIndex];
            		                int skipFlag = 64;
            		                flags |= skipFlag;
            		                args[flagIndex] = flags;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void GalleryScreenshotPathHook(PackageReadyParam lpparam) {
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

    public static void ScrambleAppLockPINHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllConstructors("com.miui.applicationlock.widget.MiuiNumericInputView", lpparam.getClassLoader(), new MethodHook() {
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

            		                LinearLayout keys = (LinearLayout)thisObject;
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

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void ChargingInfoHook(PackageReadyParam lpparam) {
        if (isChargingInfoHooked) return;
        isChargingInfoHooked = true;
        ModuleHelper.findAndHookMethod("com.miui.charge.ChargeUtils", lpparam.getClassLoader(), "getChargingHintText", int.class, boolean.class, Context.class, new MethodHook() {
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

            		                int charge = (int)args[0];
            		                String hint = (String)result;

            		                if (charge <= 100 && hint != null && isKeyguardIndicationCaller()) {
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
            		                        if (showCurr) values.add(String.format(Locale.US, "%.2f", currVal) + " A");
            		                        float voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW")) / 1000f / 1000f;
            		                        if (showVolt)
            		                            values.add(String.format(Locale.US, "%.1f", voltVal) + " V");
            		                        if (showWatt)
            		                            values.add(String.format(Locale.US, "%.1f", voltVal * currVal) + " W");
            		                        if (showTemp) {
            		                            int tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"));
            		                            values.add(Math.round(tempVal / 10f) + " ℃");
            		                        }
            		                    }
            		                    if (values.size() == 0) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                    String info = TextUtils.join(" · ", values);

            		                    if (hint.contains(info)) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                    int opt = MainModule.mPrefs.getStringAsInt("system_charginginfo_view", 1);
            		                    if (opt == 1)
            		                        { result = hint + "\n" + info; throwable = null; }
            		                    else if (opt == 2)
            		                        { result = hint + " · " + info; throwable = null; }
            		                    else if (opt == 3)
            		                        { result = info + " · " + hint; throwable = null; }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardIndicationTextView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

            		                int opt = MainModule.mPrefs.getStringAsInt("system_charginginfo_view", 1);
            		                if (opt != 1) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                TextView indicator = (TextView)thisObject;
            		                if (indicator != null) indicator.setSingleLine(false);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static boolean isKeyguardIndicationCaller() {
        try {
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                String className = e.getClassName();
                if (className.contains("KeyguardIndication")) return true;
                if (className.contains("MiuiCharge") || className.contains("miui.charge")) return false;
            }
        } catch (Throwable ignore) {}
        return false;
    }

    public static void NoSOSHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.keyguard.EmergencyButtonController", lpparam.getClassLoader(), "updateEmergencyCallButton", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                Button mSOS = (Button) XposedHelpers.getObjectField(thisObject, "mView");
            		                if (mSOS.getVisibility() == View.VISIBLE) {
            		                    mSOS.setVisibility(View.INVISIBLE);
            		                }
            		                { skipped = true; result = null; throwable = null; }

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

    public static void ForceDarkAllAppsHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.ForceDarkAppListProvider", lpparam.getClassLoader(), "fillDarkModeAppSettingsInfo", new MethodHook() {
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

            		                XposedHelpers.callMethod(args[0], "setShowInSettings", true);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        if (!miui.os.Build.IS_INTERNATIONAL_BUILD) {
            ModuleHelper.findAndHookMethod("com.android.server.ForceDarkAppListManager", lpparam.getClassLoader(), "getDarkModeAppList", long.class, int.class, new MethodHook() {
                                @Override
                                public Object intercept(XposedInterface.Chain chain) throws Throwable {
                                	Object result = null;
                                	Throwable throwable = null;
                                	__beforeBody__: {
                                		try {


	                		                    XposedHelpers.setStaticBooleanField(miui.os.Build.class, "IS_INTERNATIONAL_BUILD", true);


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

	                		                    XposedHelpers.setStaticBooleanField(miui.os.Build.class, "IS_INTERNATIONAL_BUILD", false);


                                		} catch (Throwable t) {
                                			XposedHelpers.log(t);
                                		}
                                	}
                                	return XposedHelpers.throwOrReturn(throwable, result);
                                }
            });
        }
        ModuleHelper.findAndHookMethod("com.android.server.ForceDarkAppListManager", lpparam.getClassLoader(), "shouldShowInSettings", ApplicationInfo.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if (args[0] == null) {
            		                    { skipped = true; result = false; throwable = null; }
                                        { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                }
            		                ApplicationInfo applicationInfo = (ApplicationInfo) args[0];
            		                int flags = applicationInfo.flags;
            		                boolean systemApp = (flags & 1) != 0 || (flags & 128) != 0 || applicationInfo.uid < 10000;
            		                { skipped = true; result = !systemApp; throwable = null; }

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

    public static void MaxNotificationIconsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconContainer", lpparam.getClassLoader(), "resetViewStates", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                int opt = MainModule.mPrefs.getStringAsInt("system_maxsbicons", 0);
            		                int maxIcons = XposedHelpers.getIntField(thisObject, "mMaxStaticIcons");
            		                opt = opt == -1 ? 999 : opt;
            		                if (opt != maxIcons && maxIcons != 0) {
            		                    XposedHelpers.setObjectField(thisObject, "mMaxStaticIcons", opt);
            		                    XposedHelpers.setObjectField(thisObject, "mMaxIconsOnLockscreen", opt);
            		                }


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void AutoDismissExpandedPopupsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpManagerPhone$HeadsUpEntryPhone", lpparam.getClassLoader(), "updateEntry", boolean.class, new MethodHook() {
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

            		                Object headsUpEntry = thisObject;
            		                boolean expanded = XposedHelpers.getBooleanField(headsUpEntry, "expanded");
            		                boolean remoteInputActive = XposedHelpers.getBooleanField(headsUpEntry, "remoteInputActive");
            		                Object mEntry = XposedHelpers.getObjectField(headsUpEntry, "mEntry");
            		                boolean rowPinned = (boolean) XposedHelpers.callMethod(mEntry, "isRowPinned");
            		                if (expanded && rowPinned && !remoteInputActive) {
            		                    Object headsUpManagerPhone = XposedHelpers.getSurroundingThis(headsUpEntry);
            		                    Handler mHandler = (Handler) XposedHelpers.getObjectField(headsUpManagerPhone, "mHandler");
            		                    Runnable mRemoveAlertRunnable = (Runnable) XposedHelpers.getObjectField(headsUpEntry, "mRemoveAlertRunnable");
            		                    boolean extended = XposedHelpers.getBooleanField(headsUpEntry, "extended");
            		                    mHandler.postDelayed(mRemoveAlertRunnable, extended ? 10000 : 4500);
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarNotificationPresenter", lpparam.getClassLoader(), "onExpandClicked",  new MethodHook() {
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

            		                boolean expanded = (boolean) args[1];
            		                Object mKeyguardStateController = XposedHelpers.getObjectField(thisObject, "mKeyguardStateController");
            		                boolean mShowing = XposedHelpers.getBooleanField(mKeyguardStateController, "mShowing");
            		                if (expanded && !mShowing) {
            		                    Object headsUpManagerPhone = XposedHelpers.getObjectField(thisObject, "mHeadsUpManager");
            		                    Object headsUpEntry = XposedHelpers.callMethod(headsUpManagerPhone, "getHeadsUpEntry", XposedHelpers.getObjectField(args[0], "mKey"));
            		                    if (headsUpEntry != null) {
            		                        boolean isRowPinned = (boolean) XposedHelpers.callMethod(args[0], "isRowPinned");
            		                        if (isRowPinned) {
            		                            Handler mHandler = (Handler) XposedHelpers.getObjectField(headsUpManagerPhone, "mHandler");
            		                            Runnable mRemoveAlertRunnable = (Runnable) XposedHelpers.getObjectField(headsUpEntry, "mRemoveAlertRunnable");
            		                            mHandler.postDelayed(mRemoveAlertRunnable, 4500);
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

    public static void BetterPopupsAllowFloatHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow", lpparam.getClassLoader(), "updateMiniWindowBar", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                String pkgName = (String) XposedHelpers.callMethod(thisObject, "getMiniWindowTargetPkg");
            		                Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps");
            		                Set<String> selectedAppsBlack = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps_black");
            		                Object mAppMiniWindowManager = XposedHelpers.callMethod(thisObject, "getMAppMiniWindowManager");
            		                Object notificationSettingsManager = XposedHelpers.getObjectField(mAppMiniWindowManager, "notificationSettingsManager");
            		                List<String> mAllowNotificationSlide = (List<String>) XposedHelpers.getObjectField(notificationSettingsManager, "mAllowNotificationSlide");
            		                if (selectedApps.contains(pkgName)) {
            		                    mAllowNotificationSlide.add(pkgName);
            		                }
            		                else if (selectedAppsBlack.contains(pkgName)) {
            		                    mAllowNotificationSlide.remove(pkgName);
            		                }


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    private static void DisableFloatingWindowBlacklistHook(ClassLoader cl) {
        MethodHook clearHook = new MethodHook() {
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

            		                List<String> blackList = (List<String>)result;
            		                if (blackList != null) blackList.clear();
            		                blackList.add("com.android.camera");
            		                { result = blackList; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", cl, "getListFromCloudData", clearHook);
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", cl, "getStartFromFreeformBlackListFromCloud", clearHook);
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "getFreeformBlackList", clearHook);
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "getFreeformBlackListFromCloud", clearHook);
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "setFreeformBlackList", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                List<String> blackList = new ArrayList<String>();
            		                blackList.add("com.android.camera");
            		                args[0] = blackList;


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
        ModuleHelper.findAndHookMethod("android.util.MiuiMultiWindowUtils", cl, "isForceResizeable", HookerClassHelper.returnConstant(true));
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowUtils", cl, "isPkgMainActivityResizeable", HookerClassHelper.returnConstant(true));
    }

    public static void DisableSideBarSuggestionHook(PackageReadyParam lpparam) {
        DisableFloatingWindowBlacklistHook(lpparam.getClassLoader());
    }

    public static void NoFloatingWindowBlacklistHook(SystemServerStartingParam lpparam) {
        MainModule.resHooks.setThemeValueReplacement("android", "string-array", "freeform_black_list", ResourceConstants.module_resize_black_list);
        DisableFloatingWindowBlacklistHook(lpparam.getClassLoader());
        ModuleHelper.findAndHookMethod("com.android.server.wm.MiuiFreeformUtilImpl", lpparam.getClassLoader(), "supportsFreeform", HookerClassHelper.returnConstant(true));
    }

    private static String freeformCallingPackage = "SkipCheck";

    private static String nextFreeformPackage = ModuleHelper.NOT_EXIST_SYMBOL;

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
        String compClassName = intent.getComponent().getClassName();
        if (openFwWhenShare) {
            boolean whitelist = MainModule.mPrefs.getBoolean("system_fw_forcein_actionsend_in_whitelist");
            boolean appInList = MainModule.mPrefs.getStringSet("system_fw_forcein_actionsend_apps").contains(pkgName);
            if (whitelist ^ appInList) {
                return false;
            }
            if ("com.miui.packageinstaller".equals(pkgName) && compClassName.contains("InstallPrepareAlertActivity")) {
                    return true;
                }
            if (Intent.ACTION_SEND.equals(intent.getAction()) && !pkgName.equals(callingPackage)) {
                openInFw = true;
            }
            else if ("com.tencent.mm".equals(pkgName) && compClassName.contains(".plugin.base.stub.WXEntryActivity")) {
                openInFw = true;
            }
            else if ("com.tencent.mobileqq".equals(pkgName) && (
                compClassName.contains(".activity.JumpActivity")
                || compClassName.contains(".activity.LoginActivity")
                || compClassName.contains(".agent.AgentActivity")
            )) {
                openInFw = true;
            }
        }
        boolean openSettingFromSystemUI = MainModule.mPrefs.getBoolean("system_cc_freeform_when_longclick");
        if (openSettingFromSystemUI && "com.android.systemui".equals(callingPackage)
            && ("com.android.settings".equals(pkgName)
                || ("com.android.phone".equals(pkgName) && compClassName.contains(".settings.MobileNetworkSettings"))
            )
        ) {
            openInFw = true;
        }
        if (!openInFw) {
            openInFw = pkgName.equals(nextFreeformPackage);
        }
        return openInFw;
    }

    public static void OpenAppInFreeFormHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", lpparam.getClassLoader(), "onSystemReady", new MethodHook() {
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

            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                IntentFilter intentFilter = new IntentFilter();
            		                intentFilter.addAction(ACTION_PREFIX + "SetFreeFormPackage");
            		                BroadcastReceiver mReceiver = new BroadcastReceiver() {
            		                    @Override
            		                    public void onReceive(Context context, Intent intent) {
            		                        String action = intent.getAction();
            		                        if (action == null) return;

            		                        if (action.equals(ACTION_PREFIX + "SetFreeFormPackage")) {
            		                            nextFreeformPackage = intent.getStringExtra("package");
            		                        }
            		                    }
            		                };
            		                mContext.registerReceiver(mReceiver, intentFilter, Context.RECEIVER_EXPORTED);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService$LocalService", lpparam.getClassLoader(), "checkGameBoosterPayPassAsUser", String.class, Intent.class, int.class, new MethodHook() {
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

            		                if (freeformCallingPackage == null || "SkipCheck".equals(freeformCallingPackage)) {
            		                    { return XposedHelpers.throwOrReturn(throwable, result); }
            		                }
            		                if (!"com.miui.packageinstaller".equals(freeformCallingPackage) && freeformCallingPackage.equals(args[0])) {
            		                    { return XposedHelpers.throwOrReturn(throwable, result); }
            		                }
            		                boolean openInFw = (boolean) result;
            		                if (!openInFw) {
            		                    Intent intent = (Intent) args[1];
            		                    openInFw = shouldOpenInFreeForm(intent, freeformCallingPackage);
            		                }
            		//                XposedHelpers.log("actInfo: " + openInFw + " - " + args[0] +  " - " + freeformCallingPackage + " | " + args[1]);
            		                if (openInFw) {
            		                    nextFreeformPackage = ModuleHelper.NOT_EXIST_SYMBOL;
            		                }
            		                { result = openInFw; throwable = null; }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });

        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityStarterImpl", lpparam.getClassLoader(), "checkStartActivityByFreeForm", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object[] args = XposedHelpers.getArgsArray(chain);
            	try {

            		                if (args[1] != null) {
            		                    Object safeOptions = args[7];
            		                    if (safeOptions != null) {
            		                        ActivityOptions ao = (ActivityOptions) XposedHelpers.getObjectField(safeOptions, "mOriginalOptions");
            		                        if (ao != null && XposedHelpers.getIntField(ao, "mLaunchWindowingMode") == 5) {
            		                            freeformCallingPackage = "SkipCheck";
                                                { return XposedHelpers.proceedOrThrow(chain, args, throwable); }
            		                        }
            		                    }
            		                    freeformCallingPackage = (String) args[6];
            		//                Bundle ao = safeOptions != null ? (Bundle) XposedHelpers.callMethod(safeOptions, "getActivityOptionsBundle") : null;
            		//                String reason = (String) XposedHelpers.getObjectField(request, "reason");
            		//                XposedHelpers.log("startAct: " + callingPackage
            		//                    + " reason| " + reason
            		//                    + " intent| " + intent
            		//                    + " openInFw| " + openInFw
            		//                    + " activityOptions| " + Helpers.stringifyBundle(ao)
            		//                    + " intentExtra| " + Helpers.stringifyBundle(intent.getExtras())
            		//                );
            		                }


            		result = chain.proceed(args);
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
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

    public static void MultiWindowPlusHook(SystemServerStartingParam lpparam) {
        MainModule.resHooks.setThemeValueReplacement("android", "string-array", "miui_resize_black_list", ResourceConstants.module_resize_black_list);
        Class <?> AtmClass = findClassIfExists("com.android.server.wm.ActivityTaskManagerServiceImpl", lpparam.getClassLoader());
        if (AtmClass != null) {
            ModuleHelper.findAndHookMethod(AtmClass, "updateResizeBlackList", Context.class, HookerClassHelper.DO_NOTHING);
            ModuleHelper.findAndHookMethod(AtmClass, "getSplitScreenBlackListFromXml", HookerClassHelper.DO_NOTHING);
            ModuleHelper.hookAllMethods(AtmClass, "inResizeBlackList", HookerClassHelper.returnConstant(false));
        }
    }

    public static void MultiWindowPlusHook(PackageReadyParam lpparam) {
        if (lpparam.getPackageName().equals("com.miui.home")) {
            ModuleHelper.findAndHookMethodSilently("com.android.systemui.shared.recents.model.Task", lpparam.getClassLoader(), "isSupportSplit", HookerClassHelper.returnConstant(true));
            ModuleHelper.hookAllMethods("com.miui.home.recents.views.RecentMenuView", lpparam.getClassLoader(), "onMessageEvent", new MethodHook() {
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

                		                    Handler mHandler = (Handler)XposedHelpers.getObjectField(thisObject, "mHandler");
                		                    mHandler.postDelayed(new Runnable() {
                		                        @Override
                		                        public void run() {
                		                            ImageView mMenuItemMultiWindow = (ImageView)XposedHelpers.getObjectField(thisObject, "mMenuItemMultiWindow");
                		                            ImageView mMenuItemSmallWindow = (ImageView)XposedHelpers.getObjectField(thisObject, "mMenuItemSmallWindow");
                		                            mMenuItemMultiWindow.setEnabled(true);
                		                            mMenuItemMultiWindow.setImageAlpha(255);
                		                            mMenuItemSmallWindow.setEnabled(true);
                		                            mMenuItemSmallWindow.setImageAlpha(255);
                		                        }
                		                    }, 200);

                	} catch (Throwable t) {
                		XposedHelpers.log(t);
                	}
                	return XposedHelpers.throwOrReturn(throwable, result);
                }
            });
        }
    }

    public static void MinimalNotificationViewHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBar", lpparam.getClassLoader(), "updateNotification", new MethodHook() {
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
            		            Object expandableRow = XposedHelpers.getObjectField(args[0], "row");
            		            Object mNotificationData = XposedHelpers.getObjectField(thisObject, "mNotificationData");
            		            boolean newLowPriority = (boolean)XposedHelpers.callMethod(mNotificationData, "isAmbient", XposedHelpers.callMethod(args[1], "getKey")) && !(boolean)XposedHelpers.callMethod(XposedHelpers.callMethod(args[1], "getNotification"), "isGroupSummary");
            		            boolean hasEntry = XposedHelpers.callMethod(mNotificationData, "get", XposedHelpers.getObjectField(args[0], "key")) != null;
            		            boolean isLowPriority = (boolean)XposedHelpers.callMethod(expandableRow, "isLowPriority");
            		            XposedHelpers.callMethod(expandableRow, "setIsLowPriority", newLowPriority);
            		            boolean hasLowPriorityChanged = hasEntry && isLowPriority != newLowPriority;
            		            XposedHelpers.callMethod(expandableRow, "setLowPriorityStateUpdated", hasLowPriorityChanged);
            		            XposedHelpers.callMethod(expandableRow, "updateNotification", args[0]);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void NotificationChannelSettingsHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.getClassLoader(), "createMenuViews", boolean.class, new MethodHook() {
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

            		                Object entry = XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mParent"), "getEntry");
            		                String channelId = (String)XposedHelpers.callMethod(XposedHelpers.callMethod(entry, "getChannel"), "getId");
            		                if ("miscellaneous".equals(channelId)) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                Object notification = XposedHelpers.getObjectField(entry, "mSbn");
            		                Class<?> nuCls = findClassIfExists("com.android.systemui.statusbar.notification.NotificationUtil", lpparam.getClassLoader());
            		                boolean isHybrid = (boolean)XposedHelpers.callStaticMethod(nuCls, "isHybrid", notification);
            		                if (isHybrid) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                Object mInfoItem = XposedHelpers.getObjectField(thisObject, "mInfoItem");
            		                ImageView mIcon = (ImageView) XposedHelpers.getObjectField(mInfoItem, "mIcon");
            		                mIcon.setOnClickListener(new View.OnClickListener() {
            		                    @Override
            		                    public void onClick(View v) {
            		                        Bundle bundle = new Bundle();
            		                        bundle.putString("android.provider.extra.CHANNEL_ID", channelId);
            		                        String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
            		                        bundle.putString("package", pkgName);
            		                        int appUid = XposedHelpers.getIntField(notification, "mAppUid");
            		                        bundle.putInt("uid", appUid);
            		                        bundle.putString("miui.targetPkg", pkgName);
            		                        Intent intent = new Intent("android.intent.action.MAIN");
            		                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            		                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		                        intent.putExtra(":android:show_fragment", "com.android.settings.notification.ChannelNotificationSettings");
            		                        intent.putExtra(":android:show_fragment_args", bundle);
            		                        intent.setClassName("com.android.settings", "com.android.settings.SubSettings");
            		                        try {
            		                            XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.getStaticObjectField(UserHandle.class, "CURRENT"));
            		                            Object modalController = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.notification.modal.ModalController");
            		                            XposedHelpers.callMethod(modalController, "animExitModal", 50L, true, "MORE", false);
            		                            Object statusBar = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.android.systemui.statusbar.CommandQueue");
            		                            XposedHelpers.callMethod(statusBar, "animateCollapsePanels", 0, false);
            		                        } catch (Throwable ignore) {
            		                            XposedHelpers.log(ignore);
            		                        }
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void SkipAppLockHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.miui.server.AccessController", lpparam.getClassLoader(), "skipActivity", new MethodHook() {
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

            		                Intent intent = (Intent)args[0];
            		                if (intent == null || intent.getComponent() == null) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                String pkgName = intent.getComponent().getPackageName();
            		                String actName = intent.getComponent().getClassName();
            		                String key = "system_applock_skip_activities";
            		                String itemStr = MainModule.mPrefs.getString(key, "");
            		                if (itemStr == null || itemStr.isEmpty()) { return XposedHelpers.throwOrReturn(throwable, result); }
            		                String[] itemArr = itemStr.trim().split("\\|");
            		                for (String uuid: itemArr) {
            		                    String pkgAct = MainModule.mPrefs.getString(key + "_" + uuid + "_activity", "");
            		                    if (pkgAct.equals(pkgName + "|" + actName)) { result = true; throwable = null; }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideLockScreenHintHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.KeyguardIndicationController", lpparam.getClassLoader(), "updateDeviceEntryIndication", boolean.class, new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                XposedHelpers.setObjectField(thisObject, "mPersistentUnlockMessage", "");


                    result = chain.proceed();
            	} catch (Throwable t) {
            		throwable = t;
            		result = null;
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void HideLockScreenStatusBarHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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

            		                View mKeyguardStatusBar = (View) thisObject;
            		                mKeyguardStatusBar.setVisibility(View.GONE);
            		                mKeyguardStatusBar.setTranslationY(-499f);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void MuteVisibleNotificationsHook(PackageReadyParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.policy.MiuiAlertManager", lpparam.getClassLoader(), "buzzBeepBlink", new MethodHook() {
            @Override
                        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            	boolean skipped = false;
            	Object result = null;
            	Throwable throwable = null;
            	Object thisObject = chain.getThisObject();
            	try {

            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                PowerManager powerMgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            		                if (powerMgr.isInteractive()) {
            		                    { skipped = true; result = null; throwable = null; }
            		                }

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

    public static void NetworkIndicatorWifi(PackageReadyParam lpparam) {
        MethodHook hideWifiActivity = new MethodHook() {
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

            		                Object mWifiActivityView = XposedHelpers.getObjectField(thisObject, "mWifiActivityView");
            		                XposedHelpers.callMethod(mWifiActivityView, "setVisibility", 4);

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        };
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.getClassLoader(), "applyWifiState", hideWifiActivity);
    }

    public static void SetLockscreenWallpaperHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllMethods("com.android.server.wallpaper.WallpaperManagerService", lpparam.getClassLoader(), "setWallpaper", new MethodHook() {
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

            		                if (throwable != null || result == null || (int)args[5] == 1 || "com.android.thememanager".equals(args[1])) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
            		                if (mContext == null) { return XposedHelpers.throwOrReturn(throwable, result); }

            		                int handleIncomingUser = 0;
            		                try {
            		                    handleIncomingUser = (int)XposedHelpers.callStaticMethod(ActivityManager.class, "handleIncomingUser", Binder.getCallingPid(), Binder.getCallingUid(), args[7], false, true, "changing wallpaper", null);
            		                } catch (Throwable ignore) {}
            		                Object wallpaperData = XposedHelpers.callMethod(thisObject, "getWallpaperSafeLocked", handleIncomingUser, args[5]);
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
            		                                    .put("authority", "tv.withaibuild.customiuizer.mods.set_lockscreen_wallpaper")
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

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });


    }

    public static void BetterPopupsCenteredHook(PackageReadyParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManagerInjector", lpparam.getClassLoader(), "miuiHeadsUpInset", android.content.Context.class, new MethodHook() {
            private int mHeadsUpPaddingTop = 0;
            private int mHeadsUpHeight = 0;
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

            		                Context context = (Context) args[0];
            		                Resources resources = context.getResources();
            		                if (mHeadsUpPaddingTop == 0) {
            		                    int dimId = Helpers.getResId(resources, "heads_up_status_bar_padding", "dimen", "com.android.systemui");
            		                    mHeadsUpPaddingTop = resources.getDimensionPixelSize(dimId);
            		                    mHeadsUpHeight = resources.getDimensionPixelSize(Helpers.getResId(resources, "notification_max_heads_up_height", "dimen", "com.android.systemui"));
            		                }
            		                if (resources.getConfiguration().orientation != 2) {
            		                    int mHeadsUpInset = (int) result;
            		                    int mStatusBarHeight = mHeadsUpInset - mHeadsUpPaddingTop;
            		                    int topMargin = (context.getResources().getDisplayMetrics().heightPixels + mStatusBarHeight - mHeadsUpHeight) / 2;
            		                    { result = topMargin; throwable = null; }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void WallpaperScaleLevelHook(SystemServerStartingParam lpparam) {
        ModuleHelper.hookAllConstructors("com.android.server.wm.WallpaperController", lpparam.getClassLoader(), new MethodHook() {
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

            		                float scale = MainModule.mPrefs.getInt("system_other_wallpaper_scale", 6) / 10.0f;
            		                XposedHelpers.setObjectField(thisObject, "mMaxWallpaperScale", scale);
            		                ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
            		                    public void onChange(String key) {
            		                        if (key.contains("system_other_wallpaper_scale")) {
            		                            int val = MainModule.mPrefs.getInt("system_other_wallpaper_scale", 6);
            		                            XposedHelpers.setObjectField(thisObject, "mMaxWallpaperScale", val / 10.0f);
            		                        }
            		                    }
            		                });

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }

    public static void Disable72hStrongAuthHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.locksettings.LockSettingsStrongAuth", lpparam.getClassLoader(), "rescheduleStrongAuthTimeoutAlarm", long.class, int.class, HookerClassHelper.DO_NOTHING);
    }
    public static void AllowUntrustedTouchHook(SystemServerStartingParam lpparam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowState", lpparam.getClassLoader(), "getTouchOcclusionMode", new MethodHook() {
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

            		                int mode = (int) result;
            		                if (mode == 1) { result = 2; throwable = null; }
            		                else {
            		                    WindowManager.LayoutParams mAttrs = (WindowManager.LayoutParams) XposedHelpers.getObjectField(thisObject, "mAttrs");
            		                    if (mAttrs.type == 2005) {
            		                        { result = 2; throwable = null; }
            		                    }
            		                }

            	} catch (Throwable t) {
            		XposedHelpers.log(t);
            	}
            	return XposedHelpers.throwOrReturn(throwable, result);
            }
        });
    }
}
