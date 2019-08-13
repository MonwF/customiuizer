package name.mikanoshi.customiuizer.mods;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.TaskStackBuilder;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;
import android.text.Layout;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Magnifier;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import miui.os.SystemProperties;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.AudioVisualizer;
import name.mikanoshi.customiuizer.utils.BatteryIndicator;
import name.mikanoshi.customiuizer.utils.Helpers;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.findMethodExactIfExists;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;

public class System {

	public static void ScreenAnimHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "initialize", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					XposedHelpers.setObjectField(param.thisObject, "mColorFadeEnabled", true);
					XposedHelpers.setObjectField(param.thisObject, "mColorFadeFadesConfig", true);
				} catch (Throwable t) {}
			}

			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
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
		Helpers.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpNoUpdateLocked", long.class, String.class, int.class, String.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if (Integer.parseInt(MainModule.mPrefs.getString("system_nolightuponcharges", "1")) == 3 && param.args[1].equals("android.server.power:POWER")) {
					param.setResult(false);
					return;
				}
				if (Integer.parseInt(MainModule.mPrefs.getString("system_nolightuponcharges", "1")) == 2 && (
					param.args[1].equals("android.server.power:POWER") ||
					param.args[1].equals("com.android.systemui:RAPID_CHARGE") ||
					param.args[1].equals("com.android.systemui:WIRELESS_CHARGE") ||
					param.args[1].equals("com.android.systemui:WIRELESS_RAPID_CHARGE")
				)) param.setResult(false);
				//Helpers.log("wakeUpNoUpdateLocked: " + String.valueOf(param.args[0]) + " | " + String.valueOf(param.args[1]) + " | " + String.valueOf(param.args[2]) + " | " + String.valueOf(param.args[3]) + " | " + String.valueOf(param.args[4]));
			}
		});
	}

	public static void NoLightUpOnHeadsetHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpNoUpdateLocked", long.class, String.class, int.class, String.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if (param.args[1].equals("com.android.systemui:HEADSET")) param.setResult(false);
			}
		});
	}

	public static void ScramblePINHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.keyguard.KeyguardPINView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
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
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void NoPasswordHook() {
		//Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, "handleStrongAuthRequiredChanged", int.class, int.class, XC_MethodReplacement.DO_NOTHING);
		Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, "isFingerprintAllowedForUser", int.class, XC_MethodReplacement.returnConstant(true));
		Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils", null, "isFingerprintAllowedForUser", int.class, XC_MethodReplacement.returnConstant(true));
	}

//	public static void NoPasswordKeyguardHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", XC_MethodReplacement.returnConstant(true));
//		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", int.class, XC_MethodReplacement.returnConstant(true));
//		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "isUnlockWithFingerprintPossible", int.class, XC_MethodReplacement.returnConstant(true));
//	}

	public static void EnhancedSecurityHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptPowerKeyDown", KeyEvent.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
					if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) {
						Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
						if (mHandler != null) {
							Runnable mEndCallLongPress = (Runnable)XposedHelpers.getObjectField(param.thisObject, "mEndCallLongPress");
							if (mEndCallLongPress != null) mHandler.removeCallbacks(mEndCallLongPress);
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "powerLongPress", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
					if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActions", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
					if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActionsInternal", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					KeyguardManager kgMgr = (KeyguardManager)mPWMContext.getSystemService(Context.KEYGUARD_SERVICE);
					if (kgMgr.isKeyguardLocked() && kgMgr.isKeyguardSecure()) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	private static boolean isTrusted(Context mContext, ClassLoader classLoader) {
		return isTrustedWiFi(mContext) || isTrustedBt(mContext, classLoader);
	}

	private static boolean isTrustedWiFi(Context mContext) {
		WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) return false;
		Set<String> trustedNetworks = Helpers.getSharedStringSetPref(mContext, "pref_key_system_noscreenlock_wifi");
		return Helpers.containsStringPair(trustedNetworks, wifiManager.getConnectionInfo().getBSSID());
	}

	private static boolean isTrustedBt(Context mContext, ClassLoader classLoader) {
		try {
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (!mBluetoothAdapter.isEnabled()) return false;
			Set<String> trustedDevices = Helpers.getSharedStringSetPref(mContext, "pref_key_system_noscreenlock_bt");
			Object mController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", classLoader), "get", findClass("com.android.systemui.statusbar.policy.BluetoothController", classLoader));
			Collection cachedDevices = (Collection)XposedHelpers.callMethod(mController, "getCachedDevicesCopy");
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

//	private static void setLockScreenDisabled(ClassLoader classLoader, Object thisObject, boolean state) {
//		try {
//			Class<?> kumCls = findClass("com.android.keyguard.KeyguardUpdateMonitor", classLoader);
//			int user = (int)XposedHelpers.callStaticMethod(kumCls, "getCurrentUser");
//			Object mLockPatternUtils = XposedHelpers.getObjectField(thisObject, "mLockPatternUtils");
//			XposedHelpers.callMethod(mLockPatternUtils, "setLockScreenDisabled", state, user);
//			Helpers.log("setLockScreenDisabled = " + state + ", " + user);
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

	private static boolean isUnlockedOnce = false;
	public static void NoScreenLockHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "reportSuccessfulStrongAuthUnlockAttempt", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				isUnlockedOnce = true;
			}
		});

		Helpers.findAndHookMethod("android.app.admin.DevicePolicyManagerCompat", lpparam.classLoader, "reportSuccessfulFingerprintAttempt", DevicePolicyManager.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				isUnlockedOnce = true;
			}
		});

		Helpers.findAndHookMethod("com.android.keyguard.KeyguardHostView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mContext.registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						try {
							Object mSecurityContainer = XposedHelpers.getObjectField(param.thisObject, "mSecurityContainer");
							Object mCallback = XposedHelpers.getObjectField(mSecurityContainer, "mCallback");
							XposedHelpers.callMethod(mCallback, "reportUnlockAttempt", 0, true, 0);
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				}, new IntentFilter("name.mikanoshi.customiuizer.mods.action.UnlockStrongAuth"));
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "doKeyguardLocked", Bundle.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				int opt = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_noscreenlock", "1"));
				boolean isTrusted = false;
				if (opt == 4) isTrusted = isTrusted(mContext, lpparam.classLoader);
				if (opt == 2 || opt == 3 && isUnlockedOnce || opt == 4 && isTrusted) {
					boolean skip = Helpers.getSharedBoolPref(mContext, "pref_key_system_noscreenlock_skip", false);
					if (skip) {
						param.setResult(null);
						XposedHelpers.callMethod(param.thisObject, "keyguardDone");
					}
					XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
					Intent unlockIntent = new Intent("name.mikanoshi.customiuizer.mods.action.UnlockStrongAuth");
					mContext.sendBroadcast(unlockIntent);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "setupLocked", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mContext.registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();

						boolean isShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isShowing");
						if (!isShowing) return;

						boolean isTrusted = false;
						if (Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_noscreenlock", "1")) == 4)
						if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
							NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
							if (netInfo.isConnected()) isTrusted = isTrustedWiFi(mContext);
						}

						if (isTrusted) {
							boolean skip = Helpers.getSharedBoolPref(mContext, "pref_key_system_noscreenlock_skip", false);
							if (skip)
								XposedHelpers.callMethod(param.thisObject, "keyguardDone");
							else
								XposedHelpers.callMethod(param.thisObject, "resetStateLocked");
							XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
							Intent unlockIntent = new Intent("name.mikanoshi.customiuizer.mods.action.UnlockStrongAuth");
							mContext.sendBroadcast(unlockIntent);
						}
					}
				}, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
			}
		});

		Helpers.findAndHookMethod("com.android.keyguard.KeyguardSecurityModel", lpparam.classLoader, "getSecurityMode", int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				boolean skip = Helpers.getSharedBoolPref(mContext, "pref_key_system_noscreenlock_skip", false);
				if (skip) return;

				int opt = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_noscreenlock", "1"));

				boolean isTrusted = false;
				if (opt == 4) isTrusted = isTrusted(mContext, lpparam.classLoader);
				if (opt == 1 || opt == 3 && !isUnlockedOnce || opt == 4 && !isTrusted) return;

				try {
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
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, new XC_MethodHook(10) {
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				Context mContext = (Context)param.args[0];
				mContext.registerReceiver(new BroadcastReceiver() {
					public void onReceive(final Context context, Intent intent) {
						ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
						Intent updateIntent = new Intent("name.mikanoshi.customiuizer.mods.event.CACHEDDEVICESUPDATE");
						Collection cachedDevices = (Collection)XposedHelpers.callMethod(param.thisObject, "getCachedDevicesCopy");
						if (cachedDevices != null)
						for (Object device: cachedDevices) {
							BluetoothDevice mDevice = (BluetoothDevice)XposedHelpers.getObjectField(device, "mDevice");
							if (mDevice != null) deviceList.add(mDevice);
						}
						updateIntent.putParcelableArrayListExtra("device_list", deviceList);
						mContext.sendBroadcast(updateIntent);
					}
				}, new IntentFilter("name.mikanoshi.customiuizer.mods.action.FetchCachedDevices"));
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
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, toastText.getTextSize());
		tv.setTypeface(toastText.getTypeface());
		tv.setSingleLine(true);
		tv.setAlpha(0.6f);
		return tv;
	}

	private static void modifyIconLabelToast(XC_MethodHook.MethodHookParam param) {
		try {
			Context ctx = (Context)param.args[0];
			float density = ctx.getResources().getDisplayMetrics().density;

			int option = Integer.parseInt(Helpers.getSharedStringPref(ctx, "pref_key_system_iconlabletoasts", "1"));
			if (option == 1) return;

			Object res = param.getResult();
			LinearLayout toast = (LinearLayout)XposedHelpers.getObjectField(res, "mNextView");
			if (toast == null) return;
			toast.setGravity(Gravity.START);
			toast.setPadding(toast.getPaddingLeft() - Math.round(5 * density), toast.getPaddingTop() - Math.round(3 * density), toast.getPaddingRight(), toast.getPaddingBottom() - Math.round(3 * density));

			TextView toastText = toast.findViewById(android.R.id.message);
			if (toastText == null) return;
			toastText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			LinearLayout.LayoutParams lpt = (LinearLayout.LayoutParams)toastText.getLayoutParams();
			lpt.gravity = Gravity.START;

			switch (option) {
				case 2:
					LinearLayout textOnly = new LinearLayout(ctx);
					textOnly.setOrientation(LinearLayout.VERTICAL);
					textOnly.setGravity(Gravity.START);
					ImageView iv = createIcon(ctx, 21);

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
					tv.setLayoutParams(lp);
					lp = (LinearLayout.LayoutParams)toastText.getLayoutParams();
					lp.leftMargin = Math.round(5 * density);
					lp.rightMargin = Math.round(5 * density);
					toastText.setLayoutParams(lp);
					toast.setOrientation(LinearLayout.VERTICAL);
					toast.addView(tv, 0);
					break;
				case 4:
					LinearLayout textLabel = new LinearLayout(ctx);
					textLabel.setOrientation(LinearLayout.VERTICAL);
					textLabel.setGravity(Gravity.START);
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void IconLabelToastsHook() {
		if (!Helpers.findAndHookMethodSilently("android.widget.Toast", null, "makeText", Context.class, Looper.class, CharSequence.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				modifyIconLabelToast(param);
			}
		})) Helpers.findAndHookMethod("android.widget.Toast", null, "makeText", Context.class, CharSequence.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				modifyIconLabelToast(param);
			}
		});
	}

	public static void DoubleTapToSleepHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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
		MainModule.resHooks.setResReplacement("com.android.systemui", "dimen", "miui_volume_content_width_expanded", R.dimen.miui_volume_content_width_expanded);
		MainModule.resHooks.setResReplacement("com.android.systemui", "dimen", "miui_volume_ringer_layout_width_expanded", R.dimen.miui_volume_ringer_layout_width_expanded);
		notifVolumeOnResId = MainModule.resHooks.addResource("ic_miui_volume_notification", R.drawable.ic_miui_volume_notification);
		notifVolumeOffResId = MainModule.resHooks.addResource("ic_miui_volume_notification_mute", R.drawable.ic_miui_volume_notification_mute);
	}

	public static void NotificationVolumeServiceHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "updateStreamVolumeAlias", boolean.class, String.class, new XC_MethodHook() {
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				int[] streamVolumeAlias = (int[])XposedHelpers.getObjectField(param.thisObject, "mStreamVolumeAlias");
				streamVolumeAlias[1] = 1;
				streamVolumeAlias[5] = 5;
				XposedHelpers.setObjectField(param.thisObject, "mStreamVolumeAlias", streamVolumeAlias);
			}
		});

		Helpers.findAndHookMethod("com.android.server.audio.AudioService$VolumeStreamState", lpparam.classLoader, "readSettings", new XC_MethodHook() {
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					int mStreamType = XposedHelpers.getIntField(param.thisObject, "mStreamType");
					if (mStreamType != 1) return;
					synchronized (param.method.getDeclaringClass()) {
						Class<?> audioSystem = findClass("android.media.AudioSystem", null);
						int DEVICE_OUT_ALL = XposedHelpers.getStaticIntField(audioSystem, "DEVICE_OUT_ALL");
						int DEVICE_OUT_DEFAULT = XposedHelpers.getStaticIntField(audioSystem, "DEVICE_OUT_DEFAULT");
						int[] DEFAULT_STREAM_VOLUME = (int[])XposedHelpers.getStaticObjectField(audioSystem, "DEFAULT_STREAM_VOLUME");
						int remainingDevices = DEVICE_OUT_ALL;
						for (int i = 0; remainingDevices != 0; i++) {
							int device = 1 << i;
							if ((device & remainingDevices) == 0) continue;
							remainingDevices &= ~device;
							String name = (String)XposedHelpers.callMethod(param.thisObject, "getSettingNameForDevice", device);
							Object mContentResolver = XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mContentResolver");
							int index = (int)XposedHelpers.callStaticMethod(Settings.System.class, "getIntForUser", mContentResolver, name, device == DEVICE_OUT_DEFAULT ? DEFAULT_STREAM_VOLUME[mStreamType] : -1, -2);
							if (index != -1) {
								SparseIntArray mIndexMap = (SparseIntArray)XposedHelpers.getObjectField(param.thisObject, "mIndexMap");
								mIndexMap.put(device, (Integer)XposedHelpers.callMethod(param.thisObject, "getValidIndex", 10 * index));
								XposedHelpers.setObjectField(param.thisObject, "mIndexMap", mIndexMap);
							}
						}
					}
					param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void NotificationVolumeDialogHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", lpparam.classLoader, "initDialog", new XC_MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					List<Object> mColumns = (List<Object>)XposedHelpers.getObjectField(param.thisObject, "mColumns");
					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mNoColumns", mColumns == null || mColumns.isEmpty());
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					boolean mNoColumns = (boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mNoColumns");
					if (mNoColumns) XposedHelpers.callMethod(param.thisObject, "addColumn", 5, notifVolumeOnResId, notifVolumeOffResId, true);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void NotificationVolumeSettingsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.settings.MiuiSoundSettings", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				PreferenceFragment fragment = (PreferenceFragment)param.thisObject;
				Resources modRes = Helpers.getModuleRes(fragment.getActivity());
				int iconRes = fragment.getResources().getIdentifier("ic_audio_notification", "drawable", "com.android.settings");
				int order = 6;

				Class<?> vsbCls;
				Method[] initSeekBar;
				try {
					vsbCls = XposedHelpers.findClassIfExists("com.android.settings.sound.VolumeSeekBarPreference", lpparam.classLoader);
					initSeekBar = XposedHelpers.findMethodsByExactParameters(fragment.getClass(), void.class, String.class, int.class, int.class);
					if (vsbCls == null || initSeekBar.length == 0) {
						Helpers.log("NotificationVolumeSettingsHook", "Unable to find class/method in Settings to hook");
						return;
					} else {
						initSeekBar[0].setAccessible(true);
					}
				} catch (Throwable t) {
					Helpers.log("NotificationVolumeSettingsHook", "Unable to find class/method in Settings to hook");
					return;
				}

				Preference media = fragment.findPreference("media_volume");
				if (media != null) order = media.getOrder();

				Preference pref = (Preference)XposedHelpers.newInstance(vsbCls, fragment.getActivity());
				pref.setKey("notification_volume");
				pref.setTitle(modRes.getString(R.string.notification_volume));
				pref.setPersistent(true);
				fragment.getPreferenceScreen().addPreference(pref);
				initSeekBar[0].invoke(fragment, "notification_volume", 5, iconRes);
				pref.setOrder(order);

				pref = (Preference)XposedHelpers.newInstance(vsbCls, fragment.getActivity());
				pref.setKey("system_volume");
				pref.setTitle(modRes.getString(R.string.system_volume));
				pref.setPersistent(true);
				fragment.getPreferenceScreen().addPreference(pref);
				initSeekBar[0].invoke(fragment, "system_volume", 1, iconRes);
				pref.setOrder(order);
			}
		});
	}

	private static String putSecondsIn(CharSequence clockChr) {
		NumberFormat df = new DecimalFormat("00");
		String clockStr = clockChr.toString();
		if (clockStr.toLowerCase().endsWith("am") || clockStr.toLowerCase().endsWith("pm"))
			return clockStr.replaceAll("(?i)(\\s?)(am|pm)", ":" + df.format(Calendar.getInstance().get(Calendar.SECOND)) + "$1$2").trim();
		else
			return clockStr.trim() + ":" + df.format(Calendar.getInstance().get(Calendar.SECOND));
	}

	public static void ClockSecondsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				TextView clock = (TextView)param.thisObject;
				if (clock.getId() == clock.getResources().getIdentifier("clock", "id", "com.android.systemui"))
				clock.setText(putSecondsIn(clock.getText()));
			}
		});

		Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, new XC_MethodHook(10) {
			@Override
			protected void afterHookedMethod(MethodHookParam param) {
				final TextView clock = (TextView)param.thisObject;
				XposedHelpers.setAdditionalInstanceField(clock, "mLastUpdate", 0L);
				if (clock.getId() != clock.getResources().getIdentifier("clock", "id", "com.android.systemui")) return;
				final Handler mClockHandler = new Handler(clock.getContext().getMainLooper());
				Runnable mTicker = new Runnable() {
					public void run() {
						mClockHandler.postDelayed(this, 499L);

						long mElapsedSystem = SystemClock.elapsedRealtime();
						long mLastUpdate = (Long)XposedHelpers.getAdditionalInstanceField(clock, "mLastUpdate");

						if (mElapsedSystem - mLastUpdate >= 998L) {
							XposedHelpers.callMethod(clock, "updateClock");
							XposedHelpers.setAdditionalInstanceField(clock, "mLastUpdate", mElapsedSystem);
						}
					}
				};
				mClockHandler.post(mTicker);
			}
		});
	}

	public static void ExpandNotificationsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateRowStates", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				ViewGroup mStackScroller = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mStackScroller");
				for (int i = mStackScroller.getChildCount() - 1; i >= 0; i--) {
					View enotificationrow = mStackScroller.getChildAt(i);
					if (enotificationrow != null && enotificationrow.getClass().getSimpleName().equalsIgnoreCase("ExpandableNotificationRow")) try {
						Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(enotificationrow, "getEntry"), "notification");
						String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
						int opt = Integer.parseInt(MainModule.mPrefs.getString("system_expandnotifs", "1"));
						boolean isSelected = MainModule.mPrefs.getStringSet("system_expandnotifs_apps").contains(pkgName);
						if (opt == 2 && !isSelected || opt == 3 && isSelected)
						XposedHelpers.callMethod(enotificationrow, "setSystemExpanded", true);
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			}
		});
	}

	public static void PopupNotificationsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "start", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter("name.mikanoshi.customiuizer.mods.event.CHANGEFULLSCREEN");
				final Object mStatusBar = param.thisObject;
				mContext.registerReceiver(new BroadcastReceiver() {
					public void onReceive(final Context context, Intent intent) {
						if (intent.getAction().equals("name.mikanoshi.customiuizer.mods.event.CHANGEFULLSCREEN")) {
							if (mStatusBar != null)
							XposedHelpers.setAdditionalInstanceField(mStatusBar, "mIsFullscreenApp", intent.getBooleanExtra("fullscreen", false));
						}
					}
				}, intentfilter);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "addNotification", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (param.args[0] == null) return;

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Object mIsFullscreenApp = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mIsFullscreenApp");
				boolean respectFS = MainModule.mPrefs.getBoolean("system_popupnotif_fs");
				if (respectFS && mIsFullscreenApp != null && (boolean)mIsFullscreenApp) return;

				Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_popupnotif_apps");
				String pkgName = (String)XposedHelpers.callMethod(param.args[0], "getPackageName");
				if (selectedApps.contains(pkgName)) {
					Intent expandNotif = new Intent("name.mikanoshi.customiuizer.mods.action.ExpandNotifications");
					expandNotif.putExtra("expand_only", true);
					mContext.sendBroadcast(expandNotif);
				}
			}
		});
	}

	public static void PopupNotificationsFSHook() {
		Helpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Activity act = (Activity)param.thisObject;
					if (act == null) return;
					int flags = act.getWindow().getAttributes().flags;
					Intent fullscreenIntent = new Intent("name.mikanoshi.customiuizer.mods.event.CHANGEFULLSCREEN");
					if (flags != 0 && (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN && !act.getPackageName().equals("com.android.systemui"))
						fullscreenIntent.putExtra("fullscreen", true);
					else
						fullscreenIntent.putExtra("fullscreen", false);
					act.sendBroadcast(fullscreenIntent);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void RecentsBlurRatioHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.systemui.recents.views.RecentsView", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)param.args[0];
				Handler mHandler = new Handler(mContext.getMainLooper());

				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", MainModule.mPrefs.getInt("system_recents_blur", 100));
				new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_recents_blur", 100) {
					@Override
					public void onChange(String name, int defValue) {
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", Helpers.getSharedIntPref(mContext, name, defValue));
					}
				};
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView", lpparam.classLoader, "updateBlurRatio", float.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
			}
		});
	}

	public static void DrawerBlurRatioHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.StatusBarWindowManager", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)param.args[0];
				Handler mHandler = new Handler(mContext.getMainLooper());

				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", MainModule.mPrefs.getInt("system_drawer_blur", 100));
				new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_drawer_blur", 100) {
					@Override
					public void onChange(String name, int defValue) {
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", Helpers.getSharedIntPref(mContext, name, defValue));
					}
				};
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarWindowManager", lpparam.classLoader, "setBlurRatio", float.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onBlurRatioChanged", float.class, new XC_MethodHook(2) {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					param.args[0] = XposedHelpers.callMethod(param.thisObject, "getAppearFraction");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void DrawerThemeBackgroundHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onBlurRatioChanged", float.class, new XC_MethodHook(1) {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier") / 100f;
			}
		});
	}

	public static void HideNetworkTypeHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView$PhoneState", lpparam.classLoader, "updateMobileType", String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				int opt = Integer.parseInt(MainModule.mPrefs.getString("system_mobiletypeicon", "1"));
				TextView mMobileType = (TextView)XposedHelpers.getObjectField(param.thisObject, "mMobileType");
				TextView mSignalDualNotchMobileType = (TextView)XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mSignalDualNotchMobileType");
				try {
					boolean isMobileConnected = false;
					if (opt == 2) {
						ConnectivityManager mgr = (ConnectivityManager)mMobileType.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
						Network net = mgr.getActiveNetwork();
						if (net != null) {
							NetworkCapabilities netCaps = mgr.getNetworkCapabilities(net);
							if (netCaps != null)
							isMobileConnected = netCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
						}
					}
					// View parent = (View)((View)XposedHelpers.getSurroundingThis(param.thisObject)).getParent().getParent().getParent().getParent();
					// parent != null && parent.getId() != parent.getResources().getIdentifier("header_content", "id", lpparam.packageName)
					if (opt == 3 || (opt == 2 && !isMobileConnected)) {
						mMobileType.setText("");
						mSignalDualNotchMobileType.setText("");
					}
					//Helpers.log(String.valueOf(parent) + ", " + String.valueOf(parent.getId()) + " != " + String.valueOf(mMobileType.getResources().getIdentifier("header_content", "id", lpparam.packageName)));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void TrafficSpeedSpacingHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				TextView meter = (TextView)param.thisObject;
				if (meter != null) try {
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)meter.getLayoutParams();
					int margin = Math.round(meter.getResources().getDisplayMetrics().density * 4);
					lp.rightMargin = margin;
					lp.leftMargin = margin;
					meter.setLayoutParams(lp);
				} catch (Throwable t) {
					XposedBridge.log(t);
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

		Helpers.findAndHookMethod(ccCls, "showWirelessChargeAnimation", int.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
		Helpers.findAndHookMethodSilently(ccCls, "showRapidChargeAnimation", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
		Helpers.findAndHookMethodSilently(ccCls, "showWirelessRapidChargeAnimation", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
		Helpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "createStreamStates", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
				try {
					Class<?> audioCls = findClass("com.android.server.audio.AudioService", lpparam.classLoader);
					int[] maxStreamVolume = (int[])XposedHelpers.getStaticObjectField(audioCls, "MAX_STREAM_VOLUME");
					for (int i = 0; i < maxStreamVolume.length; i++)
					maxStreamVolume[i] = Math.round(maxStreamVolume[i] * MainModule.mPrefs.getInt("system_volumesteps", 10) / 10.0f);
					XposedHelpers.setStaticObjectField(audioCls, "MAX_STREAM_VOLUME", maxStreamVolume);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void MinAutoBrightnessHook(LoadPackageParam lpparam) {
		if (Helpers.isNougat())
		Helpers.findAndHookMethod("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, "updateAutoBrightness", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				int val = XposedHelpers.getIntField(param.thisObject, "mScreenAutoBrightness");
				int opt = MainModule.mPrefs.getInt("system_minbrightness", 44);
				int newVal = Math.max(val, opt);
				if (val >= 0 && val != newVal) {
					XposedHelpers.setIntField(param.thisObject, "mScreenAutoBrightness", newVal);
					if ((boolean)param.args[0]) {
						Object mCallbacks = XposedHelpers.getObjectField(param.thisObject, "mCallbacks");
						XposedHelpers.callMethod(mCallbacks, "updateBrightness");
					}
				}
			}
		});
		else
		Helpers.findAndHookMethod("com.android.server.display.AutomaticBrightnessControllerInjector", lpparam.classLoader, "changeBrightness", float.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				int val = (int)param.getResult();
				int opt = MainModule.mPrefs.getInt("system_minbrightness", 44);
				if (val >= 0) param.setResult(Math.max(val, opt));
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
			Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
			Uri mNetworkUri = (Uri)XposedHelpers.getObjectField(thisObject, "mNetworkUri");
			Cursor query = mContext.getContentResolver().query(mNetworkUri, null, null, null, null);
			if (query != null) {
				try {
					if (query.moveToFirst()) {
						tx = query.getLong(query.getColumnIndex("total_tx_byte"));
						rx = query.getLong(query.getColumnIndex("total_rx_byte"));
					}
				} catch (@SuppressWarnings("deprecation") Exception e) {
					tx = 1L; rx = 1L;
				} catch (Throwable th) {
					query.close();
				}
				query.close();
			} else {
				tx = TrafficStats.getTotalTxBytes();
				rx = TrafficStats.getTotalRxBytes();
			}
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
		return new Pair<Long, Long>(tx, rx);
	}

	@SuppressLint("DefaultLocale")
	private static String humanReadableByteCount(Context ctx, long bytes) {
		try {
			Resources modRes = Helpers.getModuleRes(ctx);
			if (bytes < 1024) return bytes + " " + modRes.getString(R.string.Bs);
			int exp = (int) (Math.log(bytes) / Math.log(1024));
			char pre = modRes.getString(R.string.speedunits).charAt(exp-1);
			DecimalFormat df = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			df.setMinimumFractionDigits(0);
			df.setMaximumFractionDigits(1);
			return df.format(bytes / Math.pow(1024, exp)) + " " + String.format("%s" + modRes.getString(R.string.Bs), pre);
		} catch (Throwable t) {
			XposedBridge.log(t);
			return "";
		}
	}

	public static void NetSpeedIntervalHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Settings.System.putInt(mContext.getContentResolver(), "status_bar_network_speed_interval", MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000);
			}
		});
	}

	public static void DetailedNetSpeedHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onTextChanged", XC_MethodReplacement.DO_NOTHING);
		Helpers.hookAllConstructors("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				TextView meter = (TextView)param.thisObject;
				float density = meter.getResources().getDisplayMetrics().density;
				int opt = Integer.parseInt(MainModule.mPrefs.getString("system_detailednetspeed_font", "3"));
				float size = 8.0f;
				float spacing = 0.7f;
				int top = 0;
				switch (opt) {
					case 1: size = 10.0f; spacing = 0.75f; top = Math.round(density); break;
					case 2: size = 9.0f; break;
					case 3: size = 8.0f; break;
					case 4: size = 7.0f; break;
				}
				meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
				meter.setSingleLine(false);
				meter.setLines(2);
				meter.setMaxLines(2);
				meter.setLineSpacing(0, spacing);
				meter.setPadding(Math.round(meter.getPaddingLeft() + 3 * density), meter.getPaddingTop() - top, meter.getPaddingRight(), meter.getPaddingBottom());
			}
		});

		Class<?> nscCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.NetworkSpeedController", lpparam.classLoader);
		if (nscCls == null) nscCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader);
		if (nscCls == null) {
			Helpers.log("DetailedNetSpeedHook", "No NetworkSpeed view or controller");
			return;
		}

		Helpers.hookAllConstructors(nscCls, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Handler mHandler = new Handler(Looper.getMainLooper()) {
					public void handleMessage(Message message) {
						if (message.what == 200000) try {
							boolean show = message.arg1 != 0;
							XposedHelpers.callMethod(param.thisObject, "setVisibilityToViewList", show ? View.VISIBLE : View.GONE);
							if (show) XposedHelpers.callMethod(param.thisObject, "setTextToViewList", (CharSequence)"-");
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
				XposedHelpers.setObjectField(param.thisObject, "mHandler", mHandler);
			}
		});

		Helpers.findAndHookMethod(nscCls, "getTotalByte", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Pair<Long, Long> bytes = getTrafficBytes(param.thisObject);
				txBytesTotal = bytes.first;
				rxBytesTotal = bytes.second;
				measureTime = nanoTime();
			}
		});

		Helpers.findAndHookMethod(nscCls, "updateNetworkSpeed", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					boolean isConnected = false;
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					ConnectivityManager mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
					if (activeNetworkInfo != null)
						if (activeNetworkInfo.isConnected()) isConnected = true;
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
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod(nscCls, "setTextToViewList", CharSequence.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					boolean hideLow = MainModule.mPrefs.getBoolean("system_detailednetspeed_low");
					boolean reduceVis = MainModule.mPrefs.getBoolean("system_detailednetspeed_zero");
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
					if (param.thisObject.getClass().getSimpleName().equals("NetworkSpeedController")) {
						CopyOnWriteArrayList mViewList = (CopyOnWriteArrayList)XposedHelpers.getObjectField(param.thisObject, "mViewList");
						for (Object tv: mViewList)
						if (tv != null) ((TextView)tv).setAlpha(reduceVis && rxSpeed == 0 && txSpeed == 0 ? 0.3f : 1.0f);
					} else {
						ArrayList<?> sViewList = (ArrayList<?>)XposedHelpers.getObjectField(param.thisObject, "sViewList");
						for (Object tv: sViewList)
						if (tv != null) ((TextView)tv).setAlpha(reduceVis && rxSpeed == 0 && txSpeed == 0 ? 0.3f : 1.0f);
					}
//Helpers.log("DetailedNetSpeedHook", "setTextToViewList: " + tx + ", " + rx);
//Helpers.log("DetailedNetSpeedHook", "class: " + param.thisObject.getClass().getSimpleName());
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void LockScreenAlbumArtHook(LoadPackageParam lpparam) {
		Method getLockWallpaperPreview = null;
		Class<?> utilClsTmp = XposedHelpers.findClassIfExists("com.android.keyguard.wallpaper.KeyguardWallpaperUtils", lpparam.classLoader);
		if (utilClsTmp != null)
		getLockWallpaperPreview = XposedHelpers.findMethodExactIfExists(utilClsTmp, "getLockWallpaperPreview", Context.class);
		if (getLockWallpaperPreview == null) {
			utilClsTmp = XposedHelpers.findClassIfExists("com.android.keyguard.MiuiKeyguardUtils", lpparam.classLoader);
			if (utilClsTmp != null)
			getLockWallpaperPreview = XposedHelpers.findMethodExactIfExists(utilClsTmp, "getLockWallpaperPreview", Context.class);
		}

		if (utilClsTmp == null || getLockWallpaperPreview == null) {
			Helpers.log("LockScreenAlbumArtHook", "Method getLockWallpaperPreview not found");
			return;
		}

		Method getLockWallpaper = XposedHelpers.findMethodExactIfExists(utilClsTmp, "getLockWallpaper", Context.class);
		Method getLockWallpaperCache = XposedHelpers.findMethodExactIfExists(utilClsTmp, "getLockWallpaperCache", Context.class);

		if (getLockWallpaper == null && getLockWallpaperCache == null) {
			Helpers.log("LockScreenAlbumArtHook", "No getLockWallpaper(Cache) methods found");
			return;
		}

		final Class<?> utilCls = utilClsTmp;

		try {
			XposedBridge.hookMethod(getLockWallpaperPreview, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(utilCls, "mAlbumArt");
					if (mAlbumArt != null) param.setResult(new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt));
				}
			});

			if (getLockWallpaper != null)
			XposedBridge.hookMethod(getLockWallpaper, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(utilCls, "mAlbumArt");
					if (mAlbumArt != null) param.setResult(new Pair<File, Drawable>(new File(""), new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt)));
				}
			});

			if (getLockWallpaperCache != null)
			XposedBridge.hookMethod(getLockWallpaperCache, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(utilCls, "mAlbumArt");
					if (mAlbumArt != null) param.setResult(new Pair<File, Drawable>(new File(""), new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt)));
				}
			});

			Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					// com.miui.internal.policy.impl.AwesomeLockScreenImp.WallpaperScreenElement
					if (new File("/data/system/theme/lockscreen").exists()) {
						XposedHelpers.setAdditionalStaticField(utilCls, "mAlbumArtSource", null);
						XposedHelpers.setAdditionalStaticField(utilCls, "mAlbumArt", null);
						return;
					}

					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(param.thisObject, "mMediaMetadata");
					Bitmap art = null;
					if (mMediaMetadata != null) {
						art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
						if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
						if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
					}
					Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(utilCls, "mAlbumArtSource");
					try {
						if (art == null && mAlbumArt == null) return;
						if (art != null && art.sameAs(mAlbumArt)) return;
					} catch (Throwable t) {}
					XposedHelpers.setAdditionalStaticField(utilCls, "mAlbumArtSource", art);

					int blur = Helpers.getSharedIntPref(mContext, "pref_key_system_albumartonlock_blur", 0);
					if (art != null && blur > 0)
						XposedHelpers.setAdditionalStaticField(utilCls, "mAlbumArt", Helpers.fastBlur(art, blur + 1));
					else
						XposedHelpers.setAdditionalStaticField(utilCls, "mAlbumArt", art);

					Intent setWallpaper = new Intent("com.miui.keyguard.setwallpaper");
					setWallpaper.putExtra("set_lock_wallpaper_result", true);
					mContext.sendBroadcast(setWallpaper);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

//		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "setKeyguardOtherViewVisibility", int.class, new XC_MethodHook() {
//			@Override
//			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//				(int)param.args[0] == 1 ? View.VISIBLE : View.GONE
//			}
//		});
	}

	public static void BetterPopupsHideDelaySysHook() {
		Helpers.findAndHookMethod("android.app.MiuiNotification", null, "getFloatTime", XC_MethodReplacement.returnConstant(0));
	}

	public static void BetterPopupsHideDelayHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
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

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager$HeadsUpEntry", lpparam.classLoader, "updateEntry", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				XposedHelpers.setObjectField(param.thisObject, "mRemoveHeadsUpRunnable", new Runnable() {
					@Override
					public void run() {}
				});
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "onExpandingFinished", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				XposedHelpers.setBooleanField(param.thisObject, "mReleaseOnExpandFinish", true);
			}
		});

//		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "onReorderingAllowed", new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//				Helpers.log("BetterPopupsNoHideHook", "onReorderingAllowed");
//			}
//		});
	}

	public static void BetterPopupsSwipeDownHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpTouchHelper", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				MotionEvent me = (MotionEvent)param.args[0];
				if (me.getActionMasked() == 2) {
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

						Intent expandNotif = new Intent("name.mikanoshi.customiuizer.mods.action.ExpandNotifications");
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

	public static void NoVersionCheckHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.pm.PackageManagerService", lpparam.classLoader, "checkDowngrade", XC_MethodReplacement.DO_NOTHING);
	}

	public static void ColorizedNotificationTitlesHook() {
		Helpers.hookAllMethods("android.app.Notification$Builder", null, "bindHeaderAppName", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					RemoteViews rv = (RemoteViews)param.args[0];
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if (rv != null && mContext != null)
					rv.setTextColor(mContext.getResources().getIdentifier("app_name_text", "id", "android"), (int)XposedHelpers.callMethod(param.thisObject, "resolveContrastColor"));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void CompactNotificationsRes() {
		MainModule.resHooks.setDensityReplacement("android", "dimen", "notification_action_height", 38);
		MainModule.resHooks.setDensityReplacement("android", "dimen", "notification_action_list_height", 38);
		MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_row_extra_padding", 0);
	}

	public static void HideFromRecentsHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.server.am.TaskRecord", lpparam.classLoader, new XC_MethodHook() {
			@Override
			@SuppressLint("WrongConstant")
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					//boolean mBooted = XposedHelpers.getBooleanField(param.args[0], "mBooted");
					//if (!mBooted) return;

					String pkgName = null;
					ComponentName origActivity = (ComponentName)XposedHelpers.getObjectField(param.thisObject, "origActivity");
					ComponentName realActivity = (ComponentName)XposedHelpers.getObjectField(param.thisObject, "realActivity");
					String mCallingPackage = (String)XposedHelpers.getObjectField(param.thisObject, "mCallingPackage");

					if (realActivity != null) pkgName = realActivity.getPackageName();
					if (pkgName == null && origActivity != null) pkgName = origActivity.getPackageName();
					if (pkgName == null) pkgName = mCallingPackage;

					//Context mContext = (Context)XposedHelpers.getObjectField(param.args[0], "mContext");
					//Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_hidefromrecents_apps");
					Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_hidefromrecents_apps");
					if (selectedApps.contains(pkgName)) {
						Intent intent = (Intent)XposedHelpers.getObjectField(param.thisObject, "intent");
						Intent affinityIntent = (Intent)XposedHelpers.getObjectField(param.thisObject, "affinityIntent");
						if (intent != null) intent.addFlags(8388608);
						if (affinityIntent != null) affinityIntent.addFlags(8388608);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	private static List<String> hookedTiles = new ArrayList<String>();
	@SuppressLint("StaticFieldLeak") private static Context qsCtx = null;

	@SuppressLint("MissingPermission")
	public static void QSHapticHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSFactoryImpl", lpparam.classLoader, "createTileInternal", String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Object mHost = XposedHelpers.getObjectField(param.thisObject, "mHost");
				qsCtx = (Context)XposedHelpers.callMethod(mHost, "getContext");
				Object res = param.getResult();
				if (res != null && !hookedTiles.contains(res.getClass().getCanonicalName())) try {
					Helpers.findAndHookMethod(res.getClass().getCanonicalName(), lpparam.classLoader, "handleClick", new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
							boolean ignoreSystem = Helpers.getSharedBoolPref(qsCtx, "pref_key_system_qshaptics_ignore", false);
							int opt = Integer.parseInt(Helpers.getSharedStringPref(qsCtx, "pref_key_system_qshaptics", "1"));
							if (opt == 2)
								Helpers.performLightVibration(qsCtx, ignoreSystem);
							else if (opt == 3)
								Helpers.performStrongVibration(qsCtx, ignoreSystem);
						}
					});
					hookedTiles.add(res.getClass().getCanonicalName());
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void BackGestureAreaHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.fsgesture.GestureStubView", lpparam.classLoader, "getGestureStubWindowParam", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				WindowManager.LayoutParams lp = (WindowManager.LayoutParams)param.getResult();
				int pct = MainModule.mPrefs.getInt("controls_fsg_coverage", 60);
				lp.height = Math.round(lp.height / 60.0f * pct);
				param.setResult(lp);
			}
		});
	}

	public static void AutoGroupNotificationsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.classLoader, "adjustAutogroupingSummary", int.class, String.class, String.class, boolean.class, new XC_MethodHook() {
			@Override
			@SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
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
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.classLoader, "adjustNotificationBundling", List.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					List list = (List)param.args[0];
					int opt = Integer.parseInt(MainModule.mPrefs.getString("system_autogroupnotif", "1"));
					if (opt == 2 || (list != null && list.size() < opt)) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void NoMoreIconHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "setIconsVisibility", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Object mMoreIcon = XposedHelpers.getObjectField(param.thisObject, "mMoreIcon");
				if (mMoreIcon != null) XposedHelpers.setBooleanField(param.thisObject, "mForceHideMoreIcon", true);
			}
		});
	}

	public static void ShowNotificationsAfterUnlockHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.miui.statusbar.ExpandedNotification", lpparam.classLoader, "hasShownAfterUnlock", XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("com.android.systemui.miui.statusbar.ExpandedNotification", lpparam.classLoader, "setHasShownAfterUnlock", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				XposedHelpers.setBooleanField(param.thisObject, "mHasShownAfterUnlock", false);
			}
		});
	}

	private static int appInfoIconResId;
	private static int forceCloseIconResId;
	public static void NotificationRowMenuRes() {
		appInfoIconResId = MainModule.resHooks.addResource("ic_appinfo", R.drawable.ic_appinfo);
		forceCloseIconResId = MainModule.resHooks.addResource("ic_forceclose", R.drawable.ic_forceclose);
		MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_menu_icon_size", 36);
		MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_menu_icon_padding", 0);
	}

	public static void NotificationRowMenuHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "createMenuViews", boolean.class, new XC_MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					ArrayList<Object> mMenuItems = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mMenuItems");
					Class<?> nmiCls = findClass("com.android.systemui.statusbar.NotificationMenuRow$NotificationMenuItem", lpparam.classLoader);
					Constructor<?> nmiCtr;
					Object infoBtn = null;
					Object forceCloseBtn = null;
					int nInfoResId = mContext.getResources().getIdentifier("notification_info", "layout", lpparam.packageName);
					try {
						nmiCtr = nmiCls.getConstructor(Context.class, String.class, int.class, int.class);
						infoBtn = nmiCtr.newInstance(mContext, "Application info", nInfoResId, appInfoIconResId);
						forceCloseBtn = nmiCtr.newInstance(mContext, "Force close", nInfoResId, forceCloseIconResId);
					} catch (Throwable t1) {
						try {
							nmiCtr = nmiCls.getConstructor(Context.class, String.class, findClass("com.android.systemui.statusbar.NotificationGuts.GutsContent", lpparam.classLoader), int.class);
							infoBtn = nmiCtr.newInstance(mContext, "Application info", LayoutInflater.from(mContext).inflate(nInfoResId, null, false), appInfoIconResId);
							forceCloseBtn = nmiCtr.newInstance(mContext, "Force close", LayoutInflater.from(mContext).inflate(nInfoResId, null, false), forceCloseIconResId);
						} catch (Throwable t2) {
							XposedBridge.log(t2);
						}
					}
					if (infoBtn == null || forceCloseBtn == null) return;
					mMenuItems.add(infoBtn);
					mMenuItems.add(forceCloseBtn);
					FrameLayout mMenuContainer = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
					XposedHelpers.callMethod(param.thisObject, "addMenuView", infoBtn, mMenuContainer);
					XposedHelpers.callMethod(param.thisObject, "addMenuView", forceCloseBtn, mMenuContainer);
					//XposedHelpers.callMethod(param.thisObject, "setMenuLocation");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "onHeightUpdate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				FrameLayout mMenuContainer = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
				if (mMenuContainer != null) mMenuContainer.setTranslationY(0);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "setMenuLocation", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					param.setResult(null);
					float mHorizSpaceForIcon = XposedHelpers.getFloatField(param.thisObject, "mHorizSpaceForIcon");
					boolean mSnapping = XposedHelpers.getBooleanField(param.thisObject, "mSnapping");
					FrameLayout mMenuContainer = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
					Object mParent = XposedHelpers.getObjectField(param.thisObject, "mParent");
					if (mMenuContainer == null || mParent == null) return;
					int width = (int)XposedHelpers.callMethod(mParent, "getWidth");
					int height = (int)XposedHelpers.callMethod(mParent, "getIntrinsicHeight");
					int padding = 10;
					float density = mMenuContainer.getResources().getDisplayMetrics().density;
					float startingHeight = height / 2.0f - mHorizSpaceForIcon - padding * density;
					if (!mSnapping && mMenuContainer != null && mMenuContainer.isAttachedToWindow()) {
						int childCount = mMenuContainer.getChildCount();
						int row = 0; int col = 0;
						for (int i = 0; i < childCount; i++) {
							View childAt = mMenuContainer.getChildAt(i);
							childAt.setX((float)width - ((mHorizSpaceForIcon + (col == 0 ? 2 * padding : 1.5f * padding) * density) * (col + 1)));
							childAt.setY(startingHeight + mHorizSpaceForIcon * row + padding * density);
							col++;
							if (i % 2 == 1) {
								col = 0;
								row++;
							}
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.stack.NotificationStackScrollLayout", lpparam.classLoader, "onMenuClicked", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				Object menuItem = param.args[3];
				String desc = (String)XposedHelpers.callMethod(menuItem, "getContentDescription");
				if (param.args[0] != null) try {
					if ("Application info".equals(desc)) {
						param.setResult(null);
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.args[0], "getEntry"), "notification");
						String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
						Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setData(Uri.parse("package:" + pkgName));
						mContext.startActivity(intent);
						mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
					} else if ("Force close".equals(desc)) {
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
						Object notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(param.args[0], "getEntry"), "notification");
						String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
						XposedHelpers.callMethod(am, "forceStopPackage", pkgName);
						CharSequence appName = pkgName;
						try {
							appName = mContext.getPackageManager().getApplicationLabel(mContext.getPackageManager().getApplicationInfo(pkgName, 0));
						} catch (Throwable t) {}
						Toast.makeText(mContext, Helpers.getModuleRes(mContext).getString(R.string.force_closed, appName), Toast.LENGTH_SHORT).show();
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
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
		Helpers.findAndHookMethod("com.android.server.VibratorService", lpparam.classLoader, "systemReady", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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

		Helpers.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "vibrate", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				//XposedBridge.log(Arrays.toString(new Throwable().getStackTrace()));
				String pkgName = (String)param.args[1];
				if (pkgName == null) return;
				if (checkVibration(pkgName, param.thisObject)) param.setResult(null);
			}
		});

		if (Helpers.isNougat())
		Helpers.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "vibratePattern", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				String pkgName = (String)param.args[1];
				if (pkgName == null) return;
				if (checkVibration(pkgName, param.thisObject)) param.setResult(null);
			}
		});
	}

	public static void QQSGridRes() {
		int cols = MainModule.mPrefs.getInt("system_qqsgridcolumns", 2);
		MainModule.resHooks.setObjectReplacement("com.android.systemui", "integer", "quick_settings_qqs_count_portrait", cols);
		MainModule.resHooks.setObjectReplacement("com.android.systemui", "integer", "quick_settings_qqs_count", cols + 1);
	}

	public static void QSGridRes() {
		int cols = MainModule.mPrefs.getInt("system_qsgridcolumns", 2);
		int rows = MainModule.mPrefs.getInt("system_qsgridrows", 3);
		int colsRes = R.integer.quick_settings_num_columns_3;
		int rowsRes = R.integer.quick_settings_num_rows_4;

		switch (cols) {
			case 3: colsRes = R.integer.quick_settings_num_columns_3; break;
			case 4: colsRes = R.integer.quick_settings_num_columns_4; break;
			case 5: colsRes = R.integer.quick_settings_num_columns_5; break;
			case 6: colsRes = R.integer.quick_settings_num_columns_6; break;
		}

		switch (rows) {
			case 4: rowsRes = R.integer.quick_settings_num_rows_4; break;
			case 5: rowsRes = R.integer.quick_settings_num_rows_5; break;
		}

		if (cols > 2) MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_num_columns", colsRes);
		if (rows > 3) MainModule.resHooks.setResReplacement("com.android.systemui", "integer", "quick_settings_num_rows", rowsRes);
	}

	private static void updateLabelsVisibility(Object mRecord, int mRows, int orientation) {
		if (mRecord == null) return;
		Object tileView = XposedHelpers.getObjectField(mRecord, "tileView");
		if (tileView != null) {
			ViewGroup mLabelContainer = (ViewGroup)XposedHelpers.getObjectField(tileView, "mLabelContainer");
			if (mLabelContainer != null)
			mLabelContainer.setVisibility(
				MainModule.mPrefs.getBoolean("system_qsnolabels") ||
				orientation == Configuration.ORIENTATION_PORTRAIT && mRows >= 5 ||
				orientation == Configuration.ORIENTATION_LANDSCAPE && mRows >= 3 ? View.GONE : View.VISIBLE
			);
		}
	}

	public static void QSGridLabelsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.qs.TileLayout", lpparam.classLoader, "addTile", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					updateLabelsVisibility(param.args[0], XposedHelpers.getIntField(param.thisObject, "mRows"), ((ViewGroup)param.thisObject).getResources().getConfiguration().orientation);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.hookAllMethods("com.android.systemui.qs.PagedTileLayout", lpparam.classLoader, "addTile", new XC_MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					ArrayList<Object> mPages = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mPages");
					if (mPages == null) return;
					int mRows = 0;
					if (mPages.size() > 0) mRows = XposedHelpers.getIntField(mPages.get(0), "mRows");
					updateLabelsVisibility(param.args[0], mRows, ((ViewGroup)param.thisObject).getResources().getConfiguration().orientation);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		if (MainModule.mPrefs.getInt("system_qsgridrows", 3) == 4)
		Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileView", lpparam.classLoader, "createLabel", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					ViewGroup mLabelContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mLabelContainer");
					if (mLabelContainer != null) mLabelContainer.setPadding(
						mLabelContainer.getPaddingLeft(),
						Math.round(mLabelContainer.getResources().getDisplayMetrics().density * 2),
						mLabelContainer.getPaddingRight(),
						mLabelContainer.getPaddingBottom()
					);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void NoDuckingHook(LoadPackageParam lpparam) {
		//Helpers.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor", lpparam.classLoader, "duckPlayers", XC_MethodReplacement.returnConstant(true));
		//Helpers.hookAllMethods("com.android.server.audio.PlaybackActivityMonitor$DuckingManager", lpparam.classLoader, "addDuck", XC_MethodReplacement.DO_NOTHING);
		Helpers.hookAllMethods("com.android.server.audio.FocusRequester", lpparam.classLoader, "handleFocusLoss", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if ((int)param.args[0] == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) param.setResult(null);
			}
		});
	}

	public static void OrientationLockHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "systemReady", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mOrientationLockState", MainModule.mPrefs.getInt("qs_autorotate_state", 0));
				new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_qs_autorotate_state", 0) {
					@Override
					public void onChange(String name, int defValue) {
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mOrientationLockState", Helpers.getSharedIntPref(mContext, name, defValue));
					}
				};
			}
		});

		Helpers.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "rotationForOrientationLw", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					//Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					//Helpers.log("rotationForOrientationLw: " + param.args[0] + ", " + param.args[1] + " = " + param.getResult());
					if ((int)param.args[0] == -1) {
						int opt = (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mOrientationLockState");
						int prevOrient = (int)param.args[1];
						if (opt == 1) {
							if (prevOrient != 0 && prevOrient != 2) prevOrient = 0;
							if ((int)param.getResult() == 1 || (int)param.getResult() == 3) param.setResult(prevOrient);
						} else if (opt == 2) {
							if (prevOrient != 1 && prevOrient != 3) prevOrient = 1;
							if ((int)param.getResult() == 0 || (int)param.getResult() == 2) param.setResult(prevOrient);
						}
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
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

	private static void applyHeight(Object thisObject) {
		try {
			if (thisObject == null) return;
			ViewGroup view = (ViewGroup)thisObject;
			ViewGroup.LayoutParams lp = view.getLayoutParams();
			Resources res = view.getResources();
			lp.height = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
			view.setLayoutParams(lp);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void StatusBarHeightHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardStatusBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				applyHeight(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				applyHeight(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					ViewGroup mSignalSimpleDualMobileContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mSignalSimpleDualMobileContainer");
					applyHeight(mSignalSimpleDualMobileContainer);
					ViewGroup mSignalDualNotchGroup = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mSignalDualNotchGroup");
					applyHeight(mSignalDualNotchGroup.findViewById(mSignalDualNotchGroup.getResources().getIdentifier("notch_mobile", "id", lpparam.packageName)));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideMemoryCleanHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.recents.RecentsActivity", lpparam.classLoader, "setupVisible", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				ViewGroup mMemoryAndClearContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mMemoryAndClearContainer");
				if (mMemoryAndClearContainer != null) mMemoryAndClearContainer.setVisibility(View.GONE);
			}
		});
	}

	public static void ExtendedPowerMenuHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.server.policy.MiuiGlobalActions", lpparam.classLoader, new XC_MethodHook() {
			@Override
			@SuppressWarnings("ResultOfMethodCallIgnored")
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)param.args[0];
					File powermenu = new File("/cache/extended_power_menu");
					if (powermenu.exists()) powermenu.delete();

					InputStream inputStream;
					FileOutputStream outputStream;
					byte[] fileBytes;
					Context context = Helpers.getModuleContext(mContext);
					inputStream = context.getResources().openRawResource(context.getResources().getIdentifier("extended_power_menu", "raw", Helpers.modulePkg));
					fileBytes = new byte[inputStream.available()];
					inputStream.read(fileBytes);
					outputStream = new FileOutputStream(powermenu);
					outputStream.write(fileBytes);
					outputStream.close();
					inputStream.close();

					if (!powermenu.exists()) {
						Helpers.log("ExtendedPowerMenuHook", "MAML file not found in cache");
						return;
					}

					Class<?> ResourceManager = XposedHelpers.findClass("miui.maml.ResourceManager", lpparam.classLoader);
					Class<?> ZipResourceLoader = XposedHelpers.findClass("miui.maml.util.ZipResourceLoader", lpparam.classLoader);
					Class<?> ScreenContext = XposedHelpers.findClass("miui.maml.ScreenContext", lpparam.classLoader);
					Class<?> ScreenElementRoot = XposedHelpers.findClass("miui.maml.ScreenElementRoot", lpparam.classLoader);

					XposedHelpers.setObjectField(param.thisObject, "mResourceManager", XposedHelpers.newInstance(ResourceManager, XposedHelpers.newInstance(ZipResourceLoader, powermenu.getPath())));
					Object mResourceManager = XposedHelpers.getObjectField(param.thisObject, "mResourceManager");
					Object mScreenElementRoot = XposedHelpers.newInstance(ScreenElementRoot, XposedHelpers.newInstance(ScreenContext, mContext, mResourceManager));
					XposedHelpers.setObjectField(param.thisObject, "mScreenElementRoot", mScreenElementRoot);
					XposedHelpers.callMethod(mScreenElementRoot, "setOnExternCommandListener", XposedHelpers.getObjectField(param.thisObject, "mCommandListener"));
					XposedHelpers.callMethod(mScreenElementRoot, "setKeepResource", true);
					XposedHelpers.callMethod(mScreenElementRoot, "load");
					XposedHelpers.callMethod(mScreenElementRoot, "init");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void ExtendedPowerMenuHook() {
		Helpers.findAndHookMethod("miui.maml.ScreenElementRoot", null, "issueExternCommand", String.class, Double.class, String.class, new XC_MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					String cmd = (String)param.args[0];
					Object scrContext = XposedHelpers.getObjectField(param.thisObject, "mContext");
					Context mContext = (Context)XposedHelpers.getObjectField(scrContext, "mContext");
					Handler mHandler = (Handler)XposedHelpers.getObjectField(scrContext, "mHandler");
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
						SystemProperties.set("ctl.restart", "surfaceflinger");
						SystemProperties.set("ctl.restart", "zygote");
						custom = true;
					} else if ("killsysui".equals(cmd)) {
						final WallpaperManager wm = (WallpaperManager)mContext.getSystemService(Context.WALLPAPER_SERVICE);
						Drawable drawable = wm.getDrawable();
						ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
						XposedHelpers.callMethod(am, "forceStopPackage", "com.android.systemui");
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (drawable != null && drawable.getClass() == BitmapDrawable.class) try {
									wm.setBitmap(((BitmapDrawable)drawable).getBitmap());
								} catch (Throwable t) {
									XposedBridge.log(t);
								}
							}
						}, 1000);
						custom = true;
					} else if ("killlauncher".equals(cmd)) {
						ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_HOME);
						String pkgName = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
						if (pkgName != null) XposedHelpers.callMethod(am, "forceStopPackage", pkgName);
						custom = true;
					}

					if (custom) {
						if (mSystemExternCommandListener != null) XposedHelpers.callMethod(mSystemExternCommandListener, "onCommand", param.args[0], param.args[1], param.args[2]);
						param.setResult(null);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideDismissViewHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "inflateDismissView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				ImageButton mDismissView = (ImageButton)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
				if (mDismissView != null) mDismissView.setVisibility(View.GONE);
			}
		});

		if (!Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateDismissView", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				ImageButton mDismissView = (ImageButton)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
				if (mDismissView != null) mDismissView.setVisibility(View.GONE);
			}
		})) Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateDismissView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				ImageButton mDismissView = (ImageButton)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
				if (mDismissView != null) mDismissView.setVisibility(View.GONE);
			}
		});
	}

	public static void PocketModeHook() {
		Helpers.hookAllMethods("miui.util.ProximitySensorWrapper", null, "registerListener", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
				SensorManager mSensorManager = (SensorManager)XposedHelpers.getObjectField(param.thisObject, "mSensorManager");
				Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
				mSensorManager.registerListener(new SensorEventListener() {
					public void onAccuracyChanged(Sensor sensor, int i) {}
					public void onSensorChanged(SensorEvent sensorEvent) {
						float f = sensorEvent.values[0];
						boolean isCovered = f < mSensor.getMaximumRange();
						if (isCovered) {
							if (XposedHelpers.getIntField(param.thisObject, "mProximitySensorState") != 1) {
								XposedHelpers.setIntField(param.thisObject, "mProximitySensorState", 1);
								mHandler.removeMessages(1);
								mHandler.sendEmptyMessageDelayed(0, 300);
							}
						} else if (XposedHelpers.getIntField(param.thisObject, "mProximitySensorState") != 0) {
							XposedHelpers.setIntField(param.thisObject, "mProximitySensorState", 0);
							mHandler.removeMessages(0);
							mHandler.sendEmptyMessageDelayed(1, 300);
						}
					}
				}, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
			}
		});
	}

	public static void PocketModeSettingHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.settings.KeyguardAdvancedSettings", lpparam.classLoader, "isEllipticProximity", Context.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("com.android.settings.search.SecurityUpdateHelper", lpparam.classLoader, "isEllipticProximity", Context.class, XC_MethodReplacement.returnConstant(false));
		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.android.settings.AodAndLockScreenSettings", lpparam.classLoader, "isEllipticProximity", Context.class, XC_MethodReplacement.returnConstant(false));
	}

	public static void ReplaceShortcutAppHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.HeaderView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				View mShortcut = (View)XposedHelpers.getObjectField(param.thisObject, "mShortcut");
				mShortcut.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String pkgAppName = Helpers.getSharedStringPref(v.getContext(), "pref_key_system_shortcut_app", "");
						if (pkgAppName == null || pkgAppName.equals("")) {
							View.OnClickListener mOnClickListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mOnClickListener");
							if (mOnClickListener != null) mOnClickListener.onClick(v);
						}

						String[] pkgAppArray = pkgAppName.split("\\|");
						if (pkgAppArray.length < 2) return;

						ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setComponent(name);

						Object mActStarter = XposedHelpers.getObjectField(param.thisObject, "mActStarter");
						XposedHelpers.callMethod(mActStarter, "startActivity", intent, true);
					}
				});
			}
		});
	}

	public static void ReplaceClockAppHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.HeaderView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				View mClock = (View)XposedHelpers.getObjectField(param.thisObject, "mClock");
				mClock.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String pkgAppName = Helpers.getSharedStringPref(v.getContext(), "pref_key_system_clock_app", "");
						if (pkgAppName == null || pkgAppName.equals("")) {
							View.OnClickListener mOnClickListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mOnClickListener");
							if (mOnClickListener != null) mOnClickListener.onClick(v);
						}

						String[] pkgAppArray = pkgAppName.split("\\|");
						if (pkgAppArray.length < 2) return;

						ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setComponent(name);

						Object mActStarter = XposedHelpers.getObjectField(param.thisObject, "mActStarter");
						XposedHelpers.callMethod(mActStarter, "startActivity", intent, true);
					}
				});
			}
		});
	}

	public static void ReplaceCalendarAppHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.HeaderView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				View mDateView = (View)XposedHelpers.getObjectField(param.thisObject, "mDateView");
				mDateView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String pkgAppName = Helpers.getSharedStringPref(v.getContext(), "pref_key_system_calendar_app", "");
						if (pkgAppName == null || pkgAppName.equals("")) {
							View.OnClickListener mOnClickListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mOnClickListener");
							if (mOnClickListener != null) mOnClickListener.onClick(v);
						}

						String[] pkgAppArray = pkgAppName.split("\\|");
						if (pkgAppArray.length < 2) return;

						ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setComponent(name);

						Object mActStarter = XposedHelpers.getObjectField(param.thisObject, "mActStarter");
						XposedHelpers.callMethod(mActStarter, "startActivity", intent, true);
					}
				});
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
		if (bg instanceof ColorDrawable) try {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static void hookWindowDecor(Object thisObject, Drawable bg) {
		if (bg instanceof ColorDrawable) try {
			actionBarColor = ((ColorDrawable)bg).getColor();
			Activity mActivity = (Activity)XposedHelpers.getObjectField(thisObject, "mActivity");
			if (mActivity != null && !isIgnored(mActivity))
			mActivity.getWindow().setStatusBarColor(actionBarColor);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void StatusBarBackgroundHook() {
		Helpers.findAndHookMethod("com.android.internal.policy.PhoneWindow", null, "generateLayout", "com.android.internal.policy.DecorView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Window wnd = (Window)param.thisObject;
				if (isIgnored(wnd.getContext())) return;
				int mStatusBarColor = XposedHelpers.getIntField(param.thisObject, "mStatusBarColor");
				if (mStatusBarColor == -16777216) return;
				int newColor = getActionBarColor(wnd, mStatusBarColor);
				if (newColor != mStatusBarColor)
				XposedHelpers.callMethod(param.thisObject, "setStatusBarColor", newColor);
			}
		});

		Helpers.findAndHookMethod("com.android.internal.policy.PhoneWindow", null, "setStatusBarColor", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Window wnd = (Window)param.thisObject;
				if (isIgnored(wnd.getContext())) return;
				if (actionBarColor != NOCOLOR) param.args[0] = actionBarColor;
				else if (Color.alpha((int)param.args[0]) < 255) param.args[0] = Color.TRANSPARENT;
			}
		});

		Helpers.findAndHookMethod("com.android.internal.app.ToolbarActionBar", null, "setBackgroundDrawable", Drawable.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				hookToolbar(param.thisObject, (Drawable)param.args[0]);
			}
		});

		Helpers.findAndHookMethod("com.android.internal.app.WindowDecorActionBar", null, "setBackgroundDrawable", Drawable.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
		Helpers.hookMethod(sbdMethod, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				hookToolbar(param.thisObject, (Drawable)param.args[0]);
			}
		});

		sbdMethod = null;
		Class<?> wdabCls = findClassIfExists("androidx.appcompat.app.WindowDecorActionBar", lpparam.classLoader);
		if (wdabCls != null) sbdMethod = findMethodExactIfExists(wdabCls, "setBackgroundDrawable", Drawable.class);
		if (sbdMethod != null) androidx = true;
		if (sbdMethod != null)
		Helpers.hookMethod(sbdMethod, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				hookWindowDecor(param.thisObject, (Drawable)param.args[0]);
			}
		});

		// old appcompat lib
		if (!androidx) {
			sbdMethod = null;
			Class<?> tabv7Cls = findClassIfExists("android.support.v7.internal.app.ToolbarActionBar", lpparam.classLoader);
			if (tabv7Cls != null) sbdMethod = findMethodExactIfExists(tabv7Cls, "setBackgroundDrawable", Drawable.class);
			if (sbdMethod != null)
			Helpers.hookMethod(sbdMethod, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					hookToolbar(param.thisObject, (Drawable)param.args[0]);
				}
			});

			sbdMethod = null;
			Class<?> wdabv7Cls = findClassIfExists("android.support.v7.internal.app.WindowDecorActionBar", lpparam.classLoader);
			if (wdabv7Cls != null) sbdMethod = findMethodExactIfExists(wdabv7Cls, "setBackgroundDrawable", Drawable.class);
			if (sbdMethod != null)
			Helpers.hookMethod(sbdMethod, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
		Helpers.findAndHookMethod("android.widget.Toast", null, "show", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					String pkgName = (String)XposedHelpers.callMethod(mContext, "getOpPackageName");
					if (pkgName == null) return;
					if (checkToast(mContext, pkgName)) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void CustomRecommendedHook(LoadPackageParam lpparam) {
		Helpers.findAndHookConstructor("com.android.systemui.recents.views.RecentsRecommendView", lpparam.classLoader, Context.class, AttributeSet.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
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

		Helpers.findAndHookMethod("com.android.systemui.recents.views.RecentsRecommendView", lpparam.classLoader, "initItem", int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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

		Helpers.findAndHookMethod("com.android.systemui.recents.views.RecentsRecommendView", lpparam.classLoader, "onClick", View.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
		Helpers.hookAllMethods("com.miui.securitycenter.provider.ThirdDesktopProvider", lpparam.classLoader, "call", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Bundle bundle = new Bundle();
				bundle.putInt("mode", 1);
				bundle.putStringArrayList("list", new ArrayList<String>());
				param.setResult(bundle);
			}
		});
	}

	public static void CleanShareMenuHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "systemReady", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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

		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					if (param.args[0] == null) return;
					Intent origIntent = (Intent)param.args[0];
					Intent intent = (Intent)origIntent.clone();
					String action = intent.getAction();
					if (action == null) return;
					if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) return;
					if (intent.getDataString() != null && intent.getDataString().contains(":")) return;
					if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) return;
					Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps");
					List<ResolveInfo> resolved = (List<ResolveInfo>)param.getResult();
					Iterator itr = resolved.iterator();
					while (itr.hasNext())
					if (selectedApps.contains(((ResolveInfo)itr.next()).activityInfo.packageName)) itr.remove();
					param.setResult(resolved);
				} catch (Throwable t) {
					if (!(t instanceof BadParcelableException)) XposedBridge.log(t);
				}
			}
		};

		Object[] argsAndHook = { Intent.class, String.class, int.class, int.class, int.class, boolean.class, boolean.class, hook };
		if (Helpers.isNougat()) argsAndHook = new Object[] { Intent.class, String.class, int.class, int.class, hook };
		Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", argsAndHook);
	}

	public static void CleanOpenWithMenuHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "systemReady", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

				new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_cleanopenwith_apps") {
					@Override
					public void onChange(String name) {
						MainModule.mPrefs.put(name, Helpers.getSharedStringSetPref(mContext, name));
					}
				};
			}
		});

		XC_MethodHook hook = new XC_MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					if (param.args[0] == null) return;
					Intent origIntent = (Intent)param.args[0];
					Intent intent = (Intent)origIntent.clone();
					String action = intent.getAction();
//					XposedBridge.log(action + ": " + intent.getType() + " | " + intent.getDataString());
					if (!Intent.ACTION_VIEW.equals(action)) return;
					if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) return;
					//boolean validSchemes = "http".equals(intent.getScheme()) || "https".equals(intent.getScheme());
					//if (intent.getType() == null && !validSchemes) return;
					if (intent.getType() == null) return;
//					XposedBridge.log(intent.getPackage() + ": " + intent.getType() + " | " + intent.getDataString());
					Set<String> selectedApps = MainModule.mPrefs.getStringSet("system_cleanopenwith_apps");
					List<ResolveInfo> resolved = (List<ResolveInfo>)param.getResult();
					Iterator itr = resolved.iterator();
					while (itr.hasNext())
					if (selectedApps.contains(((ResolveInfo)itr.next()).activityInfo.packageName)) itr.remove();
					param.setResult(resolved);
				} catch (Throwable t) {
					if (!(t instanceof BadParcelableException)) XposedBridge.log(t);
				}
			}
		};

		Object[] argsAndHook = { Intent.class, String.class, int.class, int.class, int.class, boolean.class, boolean.class, hook };
		if (Helpers.isNougat()) argsAndHook = new Object[] { Intent.class, String.class, int.class, int.class, hook };
		Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", argsAndHook);
	}

	public static void VolumeTimerValuesRes() {
		MainModule.resHooks.setResReplacement("com.android.systemui", "array", "miui_volume_timer_segments", R.array.miui_volume_timer_segments);
		MainModule.resHooks.setResReplacement("com.android.systemui", "array", "miui_volume_timer_segments_title", R.array.miui_volume_timer_segments_title);
	}

	public static void AppLockHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "removeAccessControlPassLocked", "com.miui.server.SecurityManagerService$UserState", String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					if (!"*".equals(param.args[1])) return;
					int mode = (int)XposedHelpers.callMethod(param.thisObject, "getAccessControlLockMode", param.args[0]);
					if (mode != 1) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static void saveLastCheck(Object thisObject, String pkgName, int userId) {
		try {
			boolean enabled = false;
			if (pkgName != null && !"com.miui.home".equals(pkgName)) enabled = (boolean)XposedHelpers.callMethod(thisObject, "getApplicationAccessControlEnabledAsUser", pkgName, userId);
			Object userState = XposedHelpers.callMethod(thisObject, "getUserStateLocked", userId);
			XposedHelpers.setAdditionalInstanceField(userState, "mAccessControlLastCheckSaved",
				enabled ? new ArrayMap<String, Long>((ArrayMap<String, Long>)XposedHelpers.getObjectField(userState, "mAccessControlLastCheck")) : null
			);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	@SuppressWarnings({"unchecked"})
	private static void checkLastCheck(Object thisObject, int userId) {
		try {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void AppLockTimeoutHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "addAccessControlPassForUser", String.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				saveLastCheck(param.thisObject, (String)param.args[0], (int)param.args[1]);
			}

			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				checkLastCheck(param.thisObject, (int)param.args[1]);
			}
		});

		Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "checkAccessControlPassLocked", String.class, Intent.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				saveLastCheck(param.thisObject, (String)param.args[0], (int)param.args[2]);
			}

			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				checkLastCheck(param.thisObject, (int)param.args[2]);
			}
		});

		Helpers.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "activityResume", Intent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Intent intent = (Intent)param.args[0];
					if (intent.getComponent() != null)
					saveLastCheck(param.thisObject, intent.getComponent().getPackageName(), 0);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}

			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Intent intent = (Intent)param.args[0];
					if (intent.getComponent() != null)
					checkLastCheck(param.thisObject, 0);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideNavBarHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "addNavigationBar", XC_MethodReplacement.DO_NOTHING);
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "changeNavBarViewState", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				XposedHelpers.callMethod(param.thisObject, "removeNavBarView");
				XposedHelpers.callMethod(param.thisObject, "updateStatusBarPading");
				param.setResult(null);
			}
		});
	}

	private static AudioVisualizer audioViz = null;
	private static boolean isKeyguardShowing = false;
	private static boolean isNotificationPanelExpanded = false;
	private static void UpdateAudioVisualizerState() {
		if (audioViz != null) audioViz.updateState(isKeyguardShowing, isNotificationPanelExpanded);
	}
	public static void AudioVisualizerHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				ViewGroup mNotificationPanel = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mNotificationPanel");
				if (mNotificationPanel == null) {
					Helpers.log("AudioVisualizerHook", "Cannot find mNotificationPanel");
					return;
				}

				try {
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
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "onScreenTurnedOff", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				if (audioViz != null) audioViz.updateScreenOn(false);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateKeyguardState", boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Object mStatusBarKeyguardViewManager = XposedHelpers.getObjectField(param.thisObject, "mStatusBarKeyguardViewManager");
				boolean isKeyguardShowingNew = (boolean)XposedHelpers.callMethod(mStatusBarKeyguardViewManager, "isShowing");
				if (isKeyguardShowing != isKeyguardShowingNew) {
					isKeyguardShowing = isKeyguardShowingNew;
					isNotificationPanelExpanded = false;
					UpdateAudioVisualizerState();
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onExpandingFinished", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				boolean isNotificationPanelExpandedNew = XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
				if (isNotificationPanelExpanded != isNotificationPanelExpandedNew) {
					isNotificationPanelExpanded = isNotificationPanelExpandedNew;
					UpdateAudioVisualizerState();
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				if (audioViz == null) return;

				try {
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
						Drawable wallpaperDrawable = wallpaperMgr.getDrawable();
						art = ((BitmapDrawable)wallpaperDrawable).getBitmap();
					}

					MediaController mMediaController = (MediaController)XposedHelpers.getObjectField(param.thisObject, "mMediaController");
					boolean isPlaying = mMediaController != null && mMediaController.getPlaybackState() != null && mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING;
					audioViz.updateMusic(isPlaying, art);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
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
		Helpers.hookAllMethods("com.android.server.audio.AudioService", lpparam.classLoader, "requestAudioFocus", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					if ("AudioFocus_For_Phone_Ring_And_Calls".equals(param.args[4]) && audioFocusPkg != null && MainModule.mPrefs.getStringSet("system_ignorecalls_apps").contains(audioFocusPkg))
					param.setResult(1);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}

			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				int res = (int)param.getResult();
				if (res != AudioManager.AUDIOFOCUS_REQUEST_FAILED && !"AudioFocus_For_Phone_Ring_And_Calls".equals(param.args[4]))
				audioFocusPkg = (String)param.args[5];
			}
		});

		Helpers.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.classLoader, "notifyCallState", int.class, String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				removeListener(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.classLoader, "notifyCallStateForPhoneId", int.class, int.class, int.class, String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				removeListener(param.thisObject);
			}
		});
	}

	public static void AllRotationsRes() {
		MainModule.resHooks.setObjectReplacement("android", "bool", "config_allowAllRotations", true);
	}

	public static void AllRotationsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "init", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					XposedHelpers.setIntField(param.thisObject, "mAllowAllRotations", 1);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	private static boolean mUSBConnected = false;
	public static void USBConfigHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "systemReady", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
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
								if ("none".equals(func)) return;
								UsbManager usbMgr = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
								if ((boolean)XposedHelpers.callMethod(usbMgr, "isFunctionEnabled", func)) return;
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

		if (!Helpers.isNougat() && MainModule.mPrefs.getBoolean("system_defaultusb_unsecure")) {
			if (!Helpers.findAndHookMethodSilently("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.classLoader, "isUsbDataTransferActive", long.class, XC_MethodReplacement.returnConstant(true)))
			Helpers.findAndHookMethod("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.classLoader, "isUsbDataTransferActive", XC_MethodReplacement.returnConstant(true));
			Helpers.findAndHookMethod("com.android.server.usb.UsbDeviceManager$UsbHandler", lpparam.classLoader, "handleMessage", Message.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					try {
						Message msg = (Message)param.args[0];
						int setUnlockedFunc = 12;
						try {
							setUnlockedFunc = XposedHelpers.getStaticIntField(findClass("com.android.server.usb.UsbDeviceManager", lpparam.classLoader), "MSG_SET_SCREEN_UNLOCKED_FUNCTIONS");
						} catch (Throwable t) {}
						if (msg.what == setUnlockedFunc) {
							msg.obj = 0L;
							param.args[0] = msg;
						}
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		}
	}

	public static void HideIconsBattery1Hook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.BatteryMeterView", lpparam.classLoader, "update", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					ImageView mBatteryIconView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryIconView");
					mBatteryIconView.setVisibility(View.GONE);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideIconsBattery2Hook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.BatteryMeterView", lpparam.classLoader, "update", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					TextView mBatteryTextDigitView = (TextView)XposedHelpers.getObjectField(param.thisObject, "mBatteryTextDigitView");
					mBatteryTextDigitView.setVisibility(View.GONE);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideIconsBattery3Hook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.BatteryMeterView", lpparam.classLoader, "updateChargingIconView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					ImageView mBatteryChargingView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryChargingView");
					mBatteryChargingView.setVisibility(View.GONE);
				} catch (Throwable t) {
					XposedBridge.log(t);
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

			XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", diffHours <= MainModule.mPrefs.getInt("system_statusbaricons_alarmn", 0));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void HideIconsSelectiveAlarmHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
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
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, "updateAlarm", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				lastState = (boolean)param.args[0];
				updateAlarmVisibility(param.thisObject, lastState);
			}
		});
	}

	public static void HideIconsBluetoothHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, "updateBluetooth", String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					int opt = Integer.parseInt(MainModule.mPrefs.getString("system_statusbaricons_bluetooth", "1"));
					boolean isBluetoothConnected = (boolean)XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mBluetooth"), "isBluetoothConnected");
					if (opt == 3 || (opt == 2 && !isBluetoothConnected)) {
						Object mIconController = XposedHelpers.getObjectField(param.thisObject, "mIconController");
						XposedHelpers.callMethod(mIconController, "setIconVisibility", "bluetooth", false);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideIconsSignalHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "apply", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					View[] mMobileSignalGroup = (View[])XposedHelpers.getObjectField(param.thisObject, "mMobileSignalGroup");
					for (View mMobileSignal: mMobileSignalGroup) if (mMobileSignal != null) mMobileSignal.setVisibility(View.GONE);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideIconsVPNHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "apply", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					View mVpn = (View)XposedHelpers.getObjectField(param.thisObject, "mVpn");
					if (mVpn != null) mVpn.setVisibility(View.GONE);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void HideIconsHotspotHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "onHotspotChanged", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					View mWifiAp = (View)XposedHelpers.getObjectField(param.thisObject, "mWifiAp");
					if (mWifiAp != null) mWifiAp.setVisibility(View.GONE);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	private static boolean checkSlot(String slotName) {
		try {
			return "headset".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_headset") ||
				"volume".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_sound") ||
				"quiet".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_dnd") ||
				"mute".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_mute") ||
				"speakerphone".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_speaker") ||
				"call_record".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_record") ||
				"alarm_clock".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_alarm");
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static void HideIconsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (checkSlot((String)param.args[0])) param.args[1] = false;
			}
		});
	}

	public static void HideIconsSystemHook() {
		Helpers.findAndHookMethod("android.app.StatusBarManager", null, "setIconVisibility", String.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (checkSlot((String)param.args[0])) param.args[1] = false;
			}
		});
	}

	public static void BatteryIndicatorHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "makeStatusBarView", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					FrameLayout mStatusBarWindow = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mStatusBarWindow");
					BatteryIndicator indicator = new BatteryIndicator(mContext);
					View panel = mStatusBarWindow.findViewById(mContext.getResources().getIdentifier("notification_panel", "id", lpparam.packageName));
					mStatusBarWindow.addView(indicator, panel != null ? mStatusBarWindow.indexOfChild(panel) + 1 : Math.max(mStatusBarWindow.getChildCount() - 1, 8));
					FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)indicator.getLayoutParams();
					lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
					lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
					lp.gravity = Gravity.TOP;
					indicator.setAdjustViewBounds(false);
					indicator.setLayoutParams(lp);
					indicator.init(param.thisObject);
					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mBatteryIndicator", indicator);
					Object mNotificationIconAreaController = XposedHelpers.getObjectField(param.thisObject, "mNotificationIconAreaController");
					XposedHelpers.setAdditionalInstanceField(mNotificationIconAreaController, "mBatteryIndicator", indicator);
					Object mBatteryController = XposedHelpers.getObjectField(param.thisObject, "mBatteryController");
					XposedHelpers.setAdditionalInstanceField(mBatteryController, "mBatteryIndicator", indicator);
					XposedHelpers.callMethod(mBatteryController, "fireBatteryLevelChanged");
					XposedHelpers.callMethod(mBatteryController, "firePowerSaveChanged");
					XposedHelpers.callMethod(mBatteryController, "fireExtremePowerSaveChanged");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setPanelExpanded", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
					BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
					if (indicator != null) indicator.onExpandingChanged(!isKeyguardShowing && (boolean)param.args[0]);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setQsExpanded", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
					if (!isKeyguardShowing) return;
					BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
					if (indicator != null) indicator.onExpandingChanged((boolean)param.args[0]);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "onDarkChanged", Rect.class, float.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
					if (indicator != null) indicator.onDarkModeChanged((float)param.args[1], (int)param.args[2]);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "fireBatteryLevelChanged", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
					int mLevel = XposedHelpers.getIntField(param.thisObject, "mLevel");
					boolean mCharging = XposedHelpers.getBooleanField(param.thisObject, "mCharging");
					boolean mCharged = XposedHelpers.getBooleanField(param.thisObject, "mCharged");
					if (indicator != null) indicator.onBatteryLevelChanged(mLevel, mCharging, mCharged);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "firePowerSaveChanged", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
					if (indicator != null) indicator.onPowerSaveChanged(XposedHelpers.getBooleanField(param.thisObject, "mIsPowerSaveMode"));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "fireExtremePowerSaveChanged", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
					if (indicator != null) indicator.onExtremePowerSaveChanged(XposedHelpers.getBooleanField(param.thisObject, "mIsExtremePowerSaveMode"));
				} catch (Throwable t) {
					XposedBridge.log(t);
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

	@RequiresApi(api = Build.VERSION_CODES.P)
	private static void processHandleMotionEvent(Object handleView, int type, MotionEvent ev) {
		try {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.P)
	public static void TextMagnifierHook() {
		Helpers.findAndHookMethod("android.widget.Magnifier", null, "obtainContentCoordinates", float.class, float.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Point mCenterZoomCoords = (Point)XposedHelpers.getObjectField(param.thisObject, "mCenterZoomCoords");
				Point mClampedCenterZoomCoords = (Point)XposedHelpers.getObjectField(param.thisObject, "mClampedCenterZoomCoords");
				mClampedCenterZoomCoords.x = mCenterZoomCoords.x;
			}
		});

		Helpers.findAndHookConstructor("android.widget.Editor", null, TextView.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					TextView mTextView = (TextView)XposedHelpers.getObjectField(param.thisObject, "mTextView");
					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mMagnifier", new Magnifier(mTextView));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("android.widget.Editor$InsertionHandleView", null, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				processHandleMotionEvent(param.thisObject, 0, (MotionEvent)param.args[0]);
			}
		});

		Helpers.findAndHookMethod("android.widget.Editor$SelectionHandleView", null, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				processHandleMotionEvent(param.thisObject, "SelectionEndHandleView".equals(param.thisObject.getClass().getSimpleName()) ? 2 : 1, (MotionEvent)param.args[0]);
			}
		});
	}

	public static void ForceCloseHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, new XC_MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					HashSet<String> mSystemKeyPackages = (HashSet<String>)XposedHelpers.getObjectField(param.thisObject, "mSystemKeyPackages");
					mSystemKeyPackages.addAll(MainModule.mPrefs.getStringSet("system_forceclose_apps"));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void DisableAnyNotificationHook() {
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "isNotificationForcedEnabled", Context.class, String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "isNotificationForcedEnabled", Context.class, String.class, String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "isNotificationForcedFor", Context.class, String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "canSystemNotificationBeBlocked", String.class, XC_MethodReplacement.returnConstant(true));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "containNonBlockableChannel", String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "getNotificationForcedEnabledList", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					param.setResult(new HashSet<String>());
				} catch (Throwable t) {
				 	XposedBridge.log(t);
				}
			}
		});
	}

	private static int miuizerShortcutResId;
	public static void LockScreenShortcutRes() {
		miuizerShortcutResId = MainModule.resHooks.addResource("keyguard_bottom_miuizer_shortcut_img", R.drawable.keyguard_bottom_miuizer_shortcut_img);
	}

	public static void LockScreenShortcutHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$DefaultLeftButton", lpparam.classLoader, "getIcon", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Object img = param.getResult();
					if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
					XposedHelpers.setObjectField(img, "drawable", null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$DefaultRightButton", lpparam.classLoader, "getIcon", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Object img = param.getResult();
					if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
						XposedHelpers.setObjectField(img, "drawable", null);
						return;
					}

					Object thisObject = XposedHelpers.getSurroundingThis(param.thisObject);
					Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
					boolean opt = Helpers.getSharedBoolPref(mContext, "pref_key_system_lockscreenshortcuts_right_image", false);
					if (!opt) return;
					boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkMode");
					XposedHelpers.setObjectField(img, "drawable", Helpers.getModuleRes(mContext).getDrawable(mDarkMode ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_img, mContext.getTheme()));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "launchCamera", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if (GlobalActions.handleAction(mContext, "pref_key_system_lockscreenshortcuts_right", true)) {
						param.setResult(null);
						final View mNotificationPanelView = (View)XposedHelpers.getObjectField(param.thisObject, "mNotificationPanelView");
						mNotificationPanelView.postDelayed(new Runnable() {
							@Override
							public void run() {
								XposedHelpers.callMethod(mNotificationPanelView, "resetViews");
							}
						}, 500);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateWallpaper", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					if ((boolean)param.args[0]) try {
						XposedHelpers.callMethod(param.thisObject, "setCameraImage", false);
					} catch (Throwable t) {
						XposedHelpers.callMethod(param.thisObject, "setCameraImage");
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "setCameraImage", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) return;

					ImageView mKeyguardRightView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mKeyguardRightView");
					mKeyguardRightView.setBackgroundColor(Color.TRANSPARENT);

					final String key = "pref_key_system_lockscreenshortcuts_right";
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					Drawable icon = Helpers.getActionImage(mContext, key);
					mKeyguardRightView.setForeground(icon);
					mKeyguardRightView.setForegroundGravity(Gravity.CENTER);

					String str = Helpers.getActionName(mContext, key);
					if (str == null) return;

					Object mUpdateMonitor = XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor");
					boolean mDarkMode = (boolean)XposedHelpers.callMethod(mUpdateMonitor, "isLightWallpaperBottom");

					float density = mContext.getResources().getDisplayMetrics().density;
					int size = Math.round(mContext.getResources().getConfiguration().smallestScreenWidthDp * density);

					Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(bmp);
					Paint paint = new Paint();
					paint.setAntiAlias(true);
					paint.setShadowLayer(2 * density, 0, 0, mDarkMode ? Color.argb(90, 255, 255, 255): Color.argb(90, 0, 0, 0));
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
//					paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
					paint.clearShadowLayer();
					paint.setColor(mDarkMode ? Color.BLACK : Color.WHITE);
					paint.setAlpha(mDarkMode ? 160 : 230);
					canvas.drawText(str, x, y, paint);

					mKeyguardRightView.setScaleType(ImageView.ScaleType.CENTER);
					mKeyguardRightView.setImageDrawable(new BitmapDrawable(mContext.getResources(), bmp));

					XposedHelpers.setBooleanField(param.thisObject, "mGetCameraImageSucceed", false);
					param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.KeyguardMoveHelper", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					new Helpers.SharedPrefObserver(mContext, new Handler(mContext.getMainLooper())) {
						@Override
						public void onChange(Uri uri) {
							try {
								String type = uri.getPathSegments().get(1);
								if (!type.equals("boolean")) return;
								String key = uri.getPathSegments().get(2);
								if (key.contains("pref_key_system_lockscreenshortcuts"))
								MainModule.mPrefs.put(key, Helpers.getSharedBoolPref(mContext, key, false));
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
						}
					};
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardMoveHelper", lpparam.classLoader, "setTranslation", float.class, boolean.class, boolean.class, boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					int mCurrentScreen = XposedHelpers.getIntField(param.thisObject, "mCurrentScreen");
					if (mCurrentScreen != 1) return;
					if ((float)param.args[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) param.args[0] = 0.0f;
					else if ((float)param.args[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off")) param.args[0] = 0.0f;
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardMoveHelper", lpparam.classLoader, "fling", float.class, boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					int mCurrentScreen = XposedHelpers.getIntField(param.thisObject, "mCurrentScreen");
					if (mCurrentScreen != 1) return;
					if ((float)param.args[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) param.setResult(null);
					else if ((float)param.args[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off")) param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		View.OnClickListener mListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					int action = Helpers.getSharedIntPref(v.getContext(), v.getTag() + "_action", 1);
					boolean skip = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_skiplock");
					if (!skip && (action == 8 || action == 9 || action == 20))
					XposedHelpers.callStaticMethod(findClass("com.android.systemui.SystemUICompat", lpparam.classLoader), "dismissKeyguardOnNextActivity");
					GlobalActions.handleAction(v.getContext(), (String)v.getTag(), skip);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
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
					LinearLayout leftList = leftView.findViewById(listResId);
					leftList.removeAllViews();
					LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					int layoutResId = mContext.getResources().getIdentifier("miui_keyguard_left_view_control_center_item", "layout", lpparam.packageName);
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
						item.setTag("pref_key_" + key + "_" + uuid);
						leftList.addView(item);
						String iconResName = MainModule.mPrefs.getString(key + "_" + uuid + "_icon", "");
						int iconResId;
						if (iconResName == null || iconResName.isEmpty() || iconResName.equals("miuizer"))
							iconResId = miuizerShortcutResId;
						else
							iconResId = mContext.getResources().getIdentifier("keyguard_left_view_" + iconResName, "drawable", lpparam.packageName);
						item.findViewById(imgResId).setBackgroundResource(iconResId);
						((TextView)item.findViewById(nameResId)).setText(Helpers.getActionName(mContext, "pref_key_" + key + "_" + uuid));
						if (i > 0) {
							LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)item.getLayoutParams();
							lp.topMargin = margin;
							item.setLayoutParams(lp);
						}
						i++;
						item.setOnClickListener(mListener);
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		}

		Helpers.findAndHookConstructor("com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView", lpparam.classLoader, Context.class, AttributeSet.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					Handler mHandler = new LeftControlCenterHandler(mContext.getMainLooper());
					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mHandler", mHandler);
					new Helpers.SharedPrefObserver(mContext, mHandler) {
						@Override
						public void onChange(Uri uri) {
							try {
								String type = uri.getPathSegments().get(1);
								String key = uri.getPathSegments().get(2);
								if (key.contains("pref_key_system_lockscreenshortcuts_left")) {
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
									XposedHelpers.callMethod(param.thisObject, "initKeyguardLeftItems");
								}
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
						}
					};
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView", lpparam.classLoader, "initKeyguardLeftItems", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				try {
					Handler mHandler = (Handler)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mHandler");
					mHandler.removeMessages(1);
					Message msg = new Message();
					msg.what = 1;
					msg.obj = param.thisObject;
					mHandler.sendMessageDelayed(msg, 1000);
					param.setResult(null);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void LockScreenShortcutLaunchHook() {
		Helpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					Activity act = (Activity)param.thisObject;
					if (act == null) return;
					Intent intent = act.getIntent();
					if (intent == null) return;
					boolean mFromSecureKeyguard = intent.getBooleanExtra("StartActivityWhenLocked", false);
					if (mFromSecureKeyguard)
					if (Helpers.isPiePlus())
						act.setShowWhenLocked(true);
					else
						act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

//	public static void QSFooterHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.systemui.qs.QSContainerImpl", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
//			@Override
//			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				try {
//					FrameLayout qs = (FrameLayout)param.thisObject;
//					Context context = qs.getContext();
//					Resources res = context.getResources();
//					LinearLayout mQSFooterContainer = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mQSFooterContainer");
//					LayoutInflater inflater = (LayoutInflater)Helpers.getModuleContext(context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//					View dataView = inflater.inflate(R.layout.qs_footer, mQSFooterContainer, false);
//					dataView.setTag("mydata");
//					LinearLayout.LayoutParams lp0 = (LinearLayout.LayoutParams)dataView.getLayoutParams();
//					int margin = res.getDimensionPixelSize(res.getIdentifier("qs_divider_margin_horizontal", "dimen", context.getPackageName()));
//					int height = res.getDimensionPixelSize(res.getIdentifier("qs_footer_height", "dimen", context.getPackageName()));
//					lp0.setMarginStart(margin);
//					lp0.topMargin = - height / 6;
//					lp0.bottomMargin = height / 4;
//					dataView.setLayoutParams(lp0);
//
//					ImageView icon = dataView.findViewById(R.id.qs_icon);
//					icon.setImageDrawable(Helpers.getModuleRes(context).getDrawable(R.drawable.ic_gps_task, context.getTheme()));
//					ViewGroup.LayoutParams lp1 = icon.getLayoutParams();
//					int iconSize = res.getDimensionPixelSize(res.getIdentifier("qs_footer_data_usage_icon_size", "dimen", context.getPackageName()));
//					lp1.width = iconSize;
//					lp1.height = iconSize;
//					icon.setLayoutParams(lp1);
//
//					TextView data = dataView.findViewById(R.id.qs_data);
//					LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)data.getLayoutParams();
//					margin = res.getDimensionPixelSize(res.getIdentifier("qs_footer_line_margin_start", "dimen", context.getPackageName()));
//					lp2.setMarginStart(margin);
//					lp2.setMarginEnd(margin);
//					data.setLayoutParams(lp2);
//					data.setText("Some data...");
//
//					int textColor = res.getColor(res.getIdentifier("qs_footer_data_usage_text_color", "color", context.getPackageName()), context.getTheme());
//					int textSize = res.getDimensionPixelSize(res.getIdentifier("qs_tile_label_text_size", "dimen", context.getPackageName()));
//					data.setTextColor(textColor);
//					data.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//
//					mQSFooterContainer.addView(dataView);
//				} catch (Throwable t) {
//					XposedBridge.log(t);
//				}
//			}
//		});
//
//		Helpers.findAndHookMethod("com.android.systemui.qs.QSContainerImpl", lpparam.classLoader, "updateQSDataUsage", boolean.class, new XC_MethodHook() {
//			@Override
//			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				try {
//					LinearLayout mQSFooterContainer = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mQSFooterContainer");
//					View dataView = mQSFooterContainer.findViewWithTag("mydata");
//					mQSFooterContainer.removeView(dataView);
//					mQSFooterContainer.addView(dataView);
//				} catch (Throwable t) {
//					XposedBridge.log(t);
//				}
//			}
//		});
//	}
//
//	public static void VolumeDialogTimeoutHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", lpparam.classLoader, "computeTimeoutH", new XC_MethodHook() {
//			@Override
//			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//				param.setResult(Math.max((int)param.getResult(), 10000));
//			}
//		});
//	}

}