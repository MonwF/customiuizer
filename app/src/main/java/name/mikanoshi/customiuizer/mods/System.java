package name.mikanoshi.customiuizer.mods;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.MiuiNotification;
import android.app.Notification;
import android.app.TaskStackBuilder;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetProviderInfo;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.text.Layout;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
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
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.findMethodExactIfExists;

import miui.os.SystemProperties;

import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.AudioVisualizer;
import name.mikanoshi.customiuizer.utils.BatteryIndicator;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;
import name.mikanoshi.customiuizer.utils.Helpers.MimeType;

public class System {

	public static void ScreenAnimHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "initialize", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				try {
					XposedHelpers.setObjectField(param.thisObject, "mColorFadeEnabled", true);
					XposedHelpers.setObjectField(param.thisObject, "mColorFadeFadesConfig", true);
				} catch (Throwable t) {}
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
		String isAllowed = Helpers.isQPlus() ? "isBiometricAllowedForUser" : "isFingerprintAllowedForUser";
		//Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, "handleStrongAuthRequiredChanged", int.class, int.class, XC_MethodReplacement.DO_NOTHING);
		Helpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, isAllowed, int.class, XC_MethodReplacement.returnConstant(true));
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

	private static boolean isTrustedBt(ClassLoader classLoader) {
		try {
			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (!mBluetoothAdapter.isEnabled()) return false;
			Set<String> trustedDevices = MainModule.mPrefs.getStringSet("system_noscreenlock_bt");
			Object mController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", classLoader), "get", findClass("com.android.systemui.statusbar.policy.BluetoothController", classLoader));
			Collection<?> cachedDevices = (Collection<?>)XposedHelpers.callMethod(mController, "getCachedDevicesCopy");
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

	private static void checkBTConnections(Context mContext) {
		if (mContext == null)
			Helpers.log("checkBTConnections", "mContext is NULL!");
		else
			mContext.sendBroadcast(new Intent(GlobalActions.ACTION_PREFIX + "UnlockBTConnection"));
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

	private static boolean isUnlockedInnerCall = false;
	private static boolean isUnlockedWithFingerprint = false;
	private static boolean isUnlockedWithStrong = false;
	private static int forcedOption = -1;
	public static void NoScreenLockHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "reportSuccessfulStrongAuthUnlockAttempt", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (isUnlockedInnerCall) {
					isUnlockedInnerCall = false;
					return;
				}
				isUnlockedWithStrong = true;
			}
		});

		Helpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitorCallback", lpparam.classLoader, "onFingerprintAuthenticated", int.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				isUnlockedWithFingerprint = true;
			}
		});

		Helpers.findAndHookMethod("com.android.keyguard.KeyguardHostView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
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
				}, new IntentFilter(GlobalActions.ACTION_PREFIX + "UnlockStrongAuth"));
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "doKeyguardLocked", Bundle.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if (forcedOption == 0) return;

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (!isAuthOnce()) return;

				int opt = MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1);
				if (forcedOption == 1) opt = 2;
				boolean isTrusted = false;
				if (opt == 3) isTrusted = isTrusted(mContext, lpparam.classLoader);
				if (opt == 2 || opt == 3 && isTrusted) {
					boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
					if (skip) {
						param.setResult(null);
						XposedHelpers.callMethod(param.thisObject, "keyguardDone");
					}
					isUnlockedInnerCall = true;
					XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
					Intent unlockIntent = new Intent(GlobalActions.ACTION_PREFIX + "UnlockStrongAuth");
					mContext.sendBroadcast(unlockIntent);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "setupLocked", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter filter = new IntentFilter();
				filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
				filter.addAction(GlobalActions.ACTION_PREFIX + "UnlockSetForced");
				filter.addAction(GlobalActions.ACTION_PREFIX + "UnlockBTConnection");
				mContext.registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();

						if (action.equals(GlobalActions.ACTION_PREFIX + "UnlockSetForced"))
						forcedOption = intent.getIntExtra("system_noscreenlock_force", -1);

						boolean isShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isShowing");
						if (!isShowing) return;
						if (!isAuthOnce()) return;

						boolean isTrusted = false;
						if (MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1) == 3)
						if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
							NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
							if (netInfo.isConnected()) isTrusted = isTrustedWiFi(mContext);
						} else if (action.equals(GlobalActions.ACTION_PREFIX + "UnlockBTConnection")) {
							isTrusted = isTrustedBt(lpparam.classLoader);
						}

						if (forcedOption == 0) isTrusted = false;
						else if (forcedOption == 1) isTrusted = true;

						if (isTrusted) {
							boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
							if (skip)
								XposedHelpers.callMethod(param.thisObject, "keyguardDone");
							else
								XposedHelpers.callMethod(param.thisObject, "resetStateLocked");
							isUnlockedInnerCall = true;
							XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
							Intent unlockIntent = new Intent(GlobalActions.ACTION_PREFIX + "UnlockStrongAuth");
							mContext.sendBroadcast(unlockIntent);
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

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				boolean skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip");
				if (skip) return;
				if (!isAuthOnce()) return;

				int opt = MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1);
				if (forcedOption == 1) opt = 2;
				boolean isTrusted = false;
				if (opt == 3) isTrusted = isTrusted(mContext, lpparam.classLoader);
				if (opt == 1 || opt == 3 && !isTrusted) return;

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

		Helpers.hookAllConstructors("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, new MethodHook(10) {
			@Override
			protected void after(MethodHookParam param) {
				Context mContext = (Context)param.args[0];
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mContextMy", mContext);
				mContext.registerReceiver(new BroadcastReceiver() {
					public void onReceive(final Context context, Intent intent) {
						ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
						Intent updateIntent = new Intent(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE");
						Collection<?> cachedDevices = (Collection<?>)XposedHelpers.callMethod(param.thisObject, "getCachedDevicesCopy");
						if (cachedDevices != null)
						for (Object device: cachedDevices) {
							BluetoothDevice mDevice = (BluetoothDevice)XposedHelpers.getObjectField(device, "mDevice");
							if (mDevice != null) deviceList.add(mDevice);
						}
						updateIntent.putParcelableArrayListExtra("device_list", deviceList);
						mContext.sendBroadcast(updateIntent);
					}
				}, new IntentFilter(GlobalActions.ACTION_PREFIX + "FetchCachedDevices"));
			}
		});

		if (!Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, "updateConnectionState", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) {
				checkBTConnections((Context)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mContextMy"));
			}
		})) Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, "updateConnected", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) {
				checkBTConnections((Context)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mContextMy"));
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, "onBluetoothStateChanged", int.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) {
				int state = (int)param.args[0];
				if (state != 10) checkBTConnections((Context)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mContextMy"));
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
		tv.setTextColor(Helpers.is11() ? (Helpers.isNightMode(ctx) ? Color.WHITE : Color.BLACK) : Color.WHITE);
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
		if (Helpers.is11()) {
			toast.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
			toastText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
			toastText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			toastText.setLetterSpacing(0.015f);
		} else {
			toastText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		}
		LinearLayout.LayoutParams lpt = (LinearLayout.LayoutParams)toastText.getLayoutParams();
		lpt.gravity = Gravity.START;

		switch (option) {
			case 2:
				ImageView iv;
				LinearLayout textOnly = new LinearLayout(ctx);
				textOnly.setOrientation(LinearLayout.VERTICAL);
				if (Helpers.is11()) {
					textOnly.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
					textOnly.setPadding(0, Math.round(5 * density), 0, Math.round(6 * density));
					iv = createIcon(ctx, 22);
				} else {
					textOnly.setGravity(Gravity.START);
					iv = createIcon(ctx, 21);
				}

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
				if (Helpers.is11()) lp.topMargin = Math.round(5 * density);
				tv.setLayoutParams(lp);
				lp = (LinearLayout.LayoutParams)toastText.getLayoutParams();
				lp.leftMargin = Math.round(5 * density);
				lp.rightMargin = Math.round(5 * density);
				if (Helpers.is11()) lp.bottomMargin = Math.round(5 * density);
				toastText.setLayoutParams(lp);
				toast.setOrientation(LinearLayout.VERTICAL);
				toast.addView(tv, 0);
				break;
			case 4:
				LinearLayout textLabel = new LinearLayout(ctx);
				textLabel.setOrientation(LinearLayout.VERTICAL);
				if (Helpers.is11()) {
					textLabel.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
					textLabel.setPadding(0, Math.round(5 * density), 0, Math.round(6 * density));
				} else {
					textLabel.setGravity(Gravity.START);
				}
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

		Helpers.findAndHookMethod("android.widget.ToastInjector", null, "addAppName", Context.class, CharSequence.class, new MethodHook() {
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
	private static int settingsNotifResId;
	private static int settingsSystemResId;
	private static int callsResId;
	public static void NotificationVolumeDialogRes() {
		MainModule.resHooks.setResReplacement("com.android.systemui", "dimen", "miui_volume_content_width_expanded", R.dimen.miui_volume_content_width_expanded);
		MainModule.resHooks.setResReplacement("com.android.systemui", "dimen", "miui_volume_ringer_layout_width_expanded", R.dimen.miui_volume_ringer_layout_width_expanded);
		notifVolumeOnResId = MainModule.resHooks.addResource("ic_miui_volume_notification", R.drawable.ic_miui_volume_notification);
		notifVolumeOffResId = MainModule.resHooks.addResource("ic_miui_volume_notification_mute", R.drawable.ic_miui_volume_notification_mute);
		settingsNotifResId = MainModule.resHooks.addResource("ic_audio_notification", R.drawable.ic_audio_notification);
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

	public static void NotificationVolumeDialogHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", lpparam.classLoader, "initDialog", new MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void before(MethodHookParam param) throws Throwable {
				List<Object> mColumns = (List<Object>)XposedHelpers.getObjectField(param.thisObject, "mColumns");
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mNoColumns", mColumns == null || mColumns.isEmpty());
			}

			@Override
			protected void after(MethodHookParam param) throws Throwable {
				boolean mNoColumns = (boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mNoColumns");
				if (mNoColumns) XposedHelpers.callMethod(param.thisObject, "addColumn", 5, notifVolumeOnResId, notifVolumeOffResId, true);
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
					if (Helpers.is12()) {
						Class<?> pgCls = XposedHelpers.findClassIfExists("androidx.preference.PreferenceGroup", lpparam.classLoader);
						Class<?> pCls = XposedHelpers.findClassIfExists("androidx.preference.Preference", lpparam.classLoader);
						Method[] methods = XposedHelpers.findMethodsByExactParameters(pgCls, void.class, pCls);
						for (Method method: methods)
						if (Modifier.isPublic(method.getModifiers())) {
							addPreference = method.getName();
							break;
						}
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
				initSeekBar[0].invoke(fragment, "notification_volume", 5, Helpers.is12() ? context.getResources().getIdentifier("ic_audio_notification", "drawable", context.getPackageName()) : settingsNotifResId);
				XposedHelpers.callMethod(pref, "setOrder", order);

				pref = XposedHelpers.newInstance(vsbCls, context);
				XposedHelpers.callMethod(pref, "setKey", "system_volume");
				XposedHelpers.callMethod(pref, "setTitle", modRes.getString(R.string.system_volume));
				XposedHelpers.callMethod(pref, "setPersistent", true);
				XposedHelpers.callMethod(prefScreen, addPreference, pref);
				initSeekBar[0].invoke(fragment, "system_volume", 1, Helpers.is12() ? context.getResources().getIdentifier("ic_audio_vol", "drawable", context.getPackageName()) : settingsSystemResId);
				XposedHelpers.callMethod(pref, "setOrder", order);

				if (Helpers.is12()) {
					Object mRingVolume = XposedHelpers.callMethod(param.thisObject, "findPreference", "ring_volume");
					XposedHelpers.callMethod(mRingVolume, "setTitle", callsResId);
				}
			}
		});
	}

	private static String putSecondsIn(CharSequence clockChr) {
		NumberFormat df = new DecimalFormat("00");
		String clockStr = clockChr.toString();
		String clockStrLower = clockStr.toLowerCase();
		int colons = clockStr.length() - clockStr.replace(":", "").length();
		if (colons >= 2) return clockStr;
		if (clockStrLower.endsWith("am") || clockStrLower.endsWith("pm"))
			return clockStr.replaceAll("(?i)(\\s?)(am|pm)", ":" + df.format(Calendar.getInstance().get(Calendar.SECOND)) + "$1$2").trim();
		else
			return clockStr.trim() + ":" + df.format(Calendar.getInstance().get(Calendar.SECOND));
	}

	public static void ClockSecondsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				TextView clock = (TextView)param.thisObject;
				if (clock.getId() == clock.getResources().getIdentifier("clock", "id", "com.android.systemui"))
				clock.setText(putSecondsIn(clock.getText()));
			}
		});

		Helpers.findAndHookConstructor("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, Context.class, AttributeSet.class, int.class, new MethodHook(10) {
			@Override
			protected void after(MethodHookParam param) {
				final TextView clock = (TextView)param.thisObject;
				if (clock.getId() != clock.getResources().getIdentifier("clock", "id", "com.android.systemui")) return;
				final Handler mClockHandler = new Handler(clock.getContext().getMainLooper());
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						mClockHandler.post(new Runnable() {
							@Override
							public void run() {
								XposedHelpers.callMethod(clock, "updateClock");
							}
						});
					}
				}, 0, 1000);
			}
		});
	}

	public static void ExpandNotificationsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateRowStates", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
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
		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "addNotification", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (param.args[0] == null) return;

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (MainModule.mPrefs.getBoolean("system_popupnotif_fs"))
				if (Settings.Global.getInt(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.fullscreen", 0) == 1) return;

				Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_popupnotif_apps");
				String pkgName = (String)XposedHelpers.callMethod(param.args[0], "getPackageName");
				if (selectedApps.contains(pkgName)) {
					Intent expandNotif = new Intent(GlobalActions.ACTION_PREFIX + "ExpandNotifications");
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
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", Helpers.getSharedIntPref(mContext, name, defValue));
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

	private static boolean is4GPlus = false;
	public static void HideNetworkTypeHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView$PhoneState", lpparam.classLoader, "updateMobileType", String.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				int opt = Integer.parseInt(MainModule.mPrefs.getString("system_mobiletypeicon", "1"));
				TextView mMobileType = (TextView)XposedHelpers.getObjectField(param.thisObject, "mMobileType");
				TextView mSignalDualNotchMobileType = (TextView)XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mSignalDualNotchMobileType");
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
				if (opt == 3 || (opt == 2 && !isMobileConnected)) {
					mMobileType.setText("");
					mSignalDualNotchMobileType.setText("");
				}

//				try {
//					View parent = (View)((View)XposedHelpers.getSurroundingThis(param.thisObject)).getParent().getParent().getParent().getParent();
//					parent != null && parent.getId() != parent.getResources().getIdentifier("header_content", "id", lpparam.packageName)
//					Helpers.log(parent + ", " + parent.getId() + " != " + mMobileType.getResources().getIdentifier("header_content", "id", lpparam.packageName));
//				} catch (Throwable t) {
//					XposedBridge.log(t);
//				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView$PhoneState", lpparam.classLoader, "updateMobileTypeForNormal", boolean.class, String.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				is4GPlus = (boolean)param.args[0];
				param.args[0] = true;
			}
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				param.setResult(is4GPlus);
			}
		});
	}

	public static void TrafficSpeedSpacingHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onAttachedToWindow", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				TextView meter = (TextView)param.thisObject;
				if (meter == null) return;
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)meter.getLayoutParams();
				int margin = Math.round(meter.getResources().getDisplayMetrics().density * 4);
				lp.rightMargin = margin;
				lp.leftMargin = margin;
				meter.setLayoutParams(lp);
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

	private static int minBrightnessLevel;
	private static int maxBrightnessLevel;
	private static int constrainValue(int val) {
		if (val < 0) return val;

		boolean limitmin = MainModule.mPrefs.getBoolean("system_autobrightness_limitmin");
		boolean limitmax = MainModule.mPrefs.getBoolean("system_autobrightness_limitmax");
		int min_pct = MainModule.mPrefs.getInt("system_autobrightness_min", 25);
		int max_pct = MainModule.mPrefs.getInt("system_autobrightness_max", 75);

		int range, min, max;
		range = maxBrightnessLevel - minBrightnessLevel;
		if (maxBrightnessLevel != 255 || MainModule.mPrefs.getBoolean("system_autobrightness_hlg")) {
			min = Helpers.convertGammaToLinear(minBrightnessLevel + (int)(range * min_pct / 100f), minBrightnessLevel, maxBrightnessLevel);
			max = Helpers.convertGammaToLinear(minBrightnessLevel + (int)(range * max_pct / 100f), minBrightnessLevel, maxBrightnessLevel);
		} else {
			min = minBrightnessLevel + (int)(range * min_pct / 100f);
			max = minBrightnessLevel + (int)(range * max_pct / 100f);
		}

		if (max <= min) max = min + 1;
		if (limitmin && val < min) val = min;
		if (limitmax && val > max) val = max;
		return val;
	}

	public static void AutoBrightnessRangeHook(LoadPackageParam lpparam) {
		Class<?> bmsCls = findClassIfExists("com.android.server.display.BrightnessMappingStrategy", lpparam.classLoader);
		if (bmsCls != null) {
			Helpers.hookAllConstructors("com.android.server.display.BrightnessMappingStrategy$SimpleMappingStrategy", lpparam.classLoader, new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					int[] values = (int[])param.args[1];
					for (int i = 0; i < values.length; i++)
					values[i] = constrainValue(values[i]);
					param.args[1] = values;
				}
			});

			Helpers.hookAllConstructors("com.android.server.display.BrightnessMappingStrategy$PhysicalMappingStrategy", lpparam.classLoader, new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					int[] values = (int[])param.args[2];
					for (int i = 0; i < values.length; i++)
					values[i] = constrainValue(values[i]);
					param.args[2] = values;
				}
			});
		}

		if (!Helpers.isPiePlus())
			Helpers.findAndHookMethod("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, "updateAutoBrightness", boolean.class, new MethodHook() {
				@Override
				protected void after(final MethodHookParam param) throws Throwable {
					int val = XposedHelpers.getIntField(param.thisObject, "mScreenAutoBrightness");
					int newVal = constrainValue(val);
					if (val >= 0 && val != newVal) {
						//XposedBridge.log("updateAutoBrightness: " + val + " -> " + newVal);
						XposedHelpers.setIntField(param.thisObject, "mScreenAutoBrightness", newVal);
						if ((boolean)param.args[0]) {
							//XposedHelpers.callStaticMethod(findClass("com.android.server.display.AutomaticBrightnessControllerInjector", lpparam.classLoader), "recordAutoBrightnessChange", newVal);
							Object mCallbacks = XposedHelpers.getObjectField(param.thisObject, "mCallbacks");
							XposedHelpers.callMethod(mCallbacks, "updateBrightness");
						}
					}
				}
			});
		else
			Helpers.hookAllMethods("com.android.server.display.AutomaticBrightnessControllerInjector", lpparam.classLoader, "changeBrightness", new MethodHook() {
				@Override
				protected void after(final MethodHookParam param) throws Throwable {
					int val = (int)param.getResult();
					if (val >= 0) param.setResult(constrainValue(val));
				}
			});

		Helpers.hookAllConstructors("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setLongField(param.thisObject, "mBrighteningLightDebounceConfig", 500L);
				XposedHelpers.setLongField(param.thisObject, "mDarkeningLightDebounceConfig", 500L);
			}
		});

		Helpers.hookAllConstructors("com.android.server.display.DisplayPowerController", lpparam.classLoader, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				Context context = (Context)param.args[0];
				Resources res = context.getResources();
				minBrightnessLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"));
				maxBrightnessLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android"));
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
				} catch (Exception e) {
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
		Helpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onAttachedToWindow", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Settings.System.putInt(mContext.getContentResolver(), "status_bar_network_speed_interval", MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000);
			}
		});
	}

	public static void DetailedNetSpeedHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onTextChanged", XC_MethodReplacement.DO_NOTHING);
		Helpers.hookAllConstructors("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				TextView meter = (TextView)param.thisObject;
				float density = meter.getResources().getDisplayMetrics().density;
				int font = Integer.parseInt(MainModule.mPrefs.getString("system_detailednetspeed_font", "3"));
				int icons = Integer.parseInt(MainModule.mPrefs.getString("system_detailednetspeed_icon", "2"));
				float size = 8.0f;
				float spacing = 0.7f;
				int top = 0;
				switch (font) {
					case 1: size = 10.0f; spacing = 0.75f; top = Math.round(density); break;
					case 2: size = 9.0f; break;
					case 3: size = 8.0f; break;
					case 4: size = 7.0f; break;
				}
				meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
				meter.setSingleLine(false);
				meter.setLines(2);
				meter.setMaxLines(2);
				meter.setLineSpacing(0, icons == 1 ? 0.85f : spacing);
				meter.setPadding(Math.round(meter.getPaddingLeft() + 3 * density), meter.getPaddingTop() - top, meter.getPaddingRight(), meter.getPaddingBottom());
			}
		});

		Class<?> nscCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.NetworkSpeedController", lpparam.classLoader);
		if (nscCls == null) nscCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader);
		if (nscCls == null) {
			Helpers.log("DetailedNetSpeedHook", "No NetworkSpeed view or controller");
			return;
		}

		Helpers.hookAllConstructors(nscCls, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Handler mHandler = new Handler(Looper.getMainLooper()) {
					public void handleMessage(Message message) {
						if (message.what == 200000) try {
							boolean show = message.arg1 != 0;
							XposedHelpers.callMethod(param.thisObject, "setVisibilityToViewList", show ? View.VISIBLE : View.GONE);
							if (show) XposedHelpers.callMethod(param.thisObject, "setTextToViewList", "-");
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
				XposedHelpers.setObjectField(param.thisObject, "mHandler", mHandler);
			}
		});

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
			}
		});

		Helpers.findAndHookMethod(nscCls, "setTextToViewList", CharSequence.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
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
				if (reduceVis) try {
					CopyOnWriteArrayList<?> mViewList = (CopyOnWriteArrayList<?>)XposedHelpers.getObjectField(param.thisObject, "mViewList");
					for (Object tv: mViewList)
					if (tv != null) ((TextView)tv).setAlpha(rxSpeed == 0 && txSpeed == 0 ? 0.3f : 1.0f);
				} catch (Throwable t1) {
					try {
						ArrayList<?> mViewList = (ArrayList<?>)XposedHelpers.getObjectField(param.thisObject, "mViewList");
						for (Object tv: mViewList)
						if (tv != null) ((TextView)tv).setAlpha(rxSpeed == 0 && txSpeed == 0 ? 0.3f : 1.0f);
					} catch (Throwable t2) {
						ArrayList<?> sViewList = (ArrayList<?>)XposedHelpers.getObjectField(param.thisObject, "sViewList");
						for (Object tv : sViewList)
						if (tv != null) ((TextView)tv).setAlpha(rxSpeed == 0 && txSpeed == 0 ? 0.3f : 1.0f);
					}
				}
				//Helpers.log("DetailedNetSpeedHook", "setTextToViewList: " + tx + ", " + rx);
				//Helpers.log("DetailedNetSpeedHook", "class: " + param.thisObject.getClass().getSimpleName());
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

		Helpers.hookMethod(getLockWallpaperPreview, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(utilCls, "mAlbumArt");
				if (mAlbumArt != null) param.setResult(new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt));
			}
		});

		if (getLockWallpaper != null)
		Helpers.hookMethod(getLockWallpaper, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(utilCls, "mAlbumArt");
				if (mAlbumArt != null) param.setResult(new Pair<File, Drawable>(new File(""), new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt)));
			}
		});

		if (getLockWallpaperCache != null)
		Helpers.hookMethod(getLockWallpaperCache, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(utilCls, "mAlbumArt");
				if (mAlbumArt != null) param.setResult(new Pair<File, Drawable>(new File(""), new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt)));
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
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
				XposedHelpers.setAdditionalStaticField(utilCls, "mAlbumArt", processAlbumArt(mContext, art != null && blur > 0 ? Helpers.fastBlur(art, blur + 1) : art));

				Intent setWallpaper = new Intent("com.miui.keyguard.setwallpaper");
				setWallpaper.putExtra("set_lock_wallpaper_result", true);
				mContext.sendBroadcast(setWallpaper);
			}
		});

//		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "setKeyguardOtherViewVisibility", int.class, new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				(int)param.args[0] == 1 ? View.VISIBLE : View.GONE
//			}
//		});
	}

	public static void BetterPopupsHideDelaySysHook() {
		Helpers.findAndHookMethod("android.app.MiuiNotification", null, "getFloatTime", XC_MethodReplacement.returnConstant(0));
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
			protected void after(final MethodHookParam param) throws Throwable {
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

						Intent expandNotif = new Intent(GlobalActions.ACTION_PREFIX + "ExpandNotifications");
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

	public static void RotationAnimatinoHook(LoadPackageParam lpparam) {
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
					alphaAnim.setInterpolator((Interpolator)XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.android.server.wm.ScreenRotationAnimationInjector", lpparam.classLoader), "QUART_EASE_OUT_INTERPOLATOR"));
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
					alphaAnim.setInterpolator((Interpolator)XposedHelpers.getStaticObjectField(XposedHelpers.findClass("com.android.server.wm.ScreenRotationAnimationInjector", lpparam.classLoader), "QUART_EASE_OUT_INTERPOLATOR"));
					alphaAnim.setDuration(300);
					alphaAnim.setFillAfter(true);
					alphaAnim.setFillBefore(true);
					alphaAnim.setFillEnabled(true);
					param.setResult(alphaAnim);
				}
			}
		};

		Helpers.findAndHookMethod("com.android.server.wm.ScreenRotationAnimationInjector", lpparam.classLoader, "createRotation180Enter", animEnter);
		Helpers.findAndHookMethod("com.android.server.wm.ScreenRotationAnimationInjector", lpparam.classLoader, "createRotation180Exit", animExit);
		Helpers.hookAllMethods("com.android.server.wm.ScreenRotationAnimationInjector", lpparam.classLoader, "createRotationEnter", animEnter);
		Helpers.hookAllMethods("com.android.server.wm.ScreenRotationAnimationInjector", lpparam.classLoader, "createRotationEnterWithBackColor", animEnter);
		Helpers.hookAllMethods("com.android.server.wm.ScreenRotationAnimationInjector", lpparam.classLoader, "createRotationExit", animExit);
	}

	public static void NoVersionCheckHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.pm.PackageManagerService", lpparam.classLoader, "checkDowngrade", XC_MethodReplacement.DO_NOTHING);
	}

	public static void ColorizedNotificationTitlesHook() {
		Helpers.hookAllMethods("android.app.Notification.Builder", null, "bindNotificationHeader", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (!Helpers.isNougat()) try {
					Object mN = XposedHelpers.getObjectField(param.thisObject, "mN");
					if (mN != null)
					if ((boolean)XposedHelpers.callMethod(mN, "isColorizedMedia")) return;
				} catch (Throwable ignore) {}

				RemoteViews rv = (RemoteViews)param.args[0];
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				int contrastColor;
				if (Helpers.isQPlus())
					contrastColor = (int)XposedHelpers.callMethod(param.thisObject, "resolveContrastColor", param.args[1]);
				else
					contrastColor = (int)XposedHelpers.callMethod(param.thisObject, "resolveContrastColor");
				if (rv != null && mContext != null)
				rv.setTextColor(mContext.getResources().getIdentifier("app_name_text", "id", "android"), contrastColor);
			}
		});

//		Helpers.hookAllMethods("android.app.Notification.Builder", null, "bindHeaderAppName", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				XposedBridge.log("bindHeaderAppName");
//			}
//		});
//
//		Helpers.findAndHookMethod("android.app.Notification.Builder", null, "bindHeaderAppName", RemoteViews.class, "android.app.Notification.StandardTemplateParams", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				XposedBridge.log("bindHeaderAppName2");
//			}
//		});
	}

	public static void CompactNotificationsRes() {
		int height = Helpers.is12() ? 26 : 39;
		MainModule.resHooks.setDensityReplacement("android", "dimen", "notification_action_height", height);
		MainModule.resHooks.setDensityReplacement("android", "dimen", "android_notification_action_height", height);
		MainModule.resHooks.setDensityReplacement("android", "dimen", "notification_action_list_height", height);
		MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_row_extra_padding", 0);
	}

	public static void CompactNotificationsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.notification.NotificationViewWrapper", lpparam.classLoader, "wrap", new MethodHook() {
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
				container.setPadding(0, 0, 0, 0);
				float density = mView.getResources().getDisplayMetrics().density;
				int height = Math.round(density * (Helpers.is12() ? 26 : 39));
				ViewGroup actions = (ViewGroup)container.getChildAt(0);
				for (int c = 0; c < actions.getChildCount(); c++) {
					View button = actions.getChildAt(c);
					ViewGroup.MarginLayoutParams lp2 = (ViewGroup.MarginLayoutParams)button.getLayoutParams();
					lp2.height = height;
					lp2.bottomMargin = 0;
					lp2.topMargin = 0;
					if (Helpers.is12()) ((Button)button).setGravity(Gravity.BOTTOM);
				}
				FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)actions.getLayoutParams();
				lp.height = height;
				lp.gravity = Gravity.BOTTOM;
				actions.setPadding(0, 0, 0, 0);
				actions.setLayoutParams(lp);
			}
		});
	}

	public static void HideFromRecentsHook(LoadPackageParam lpparam) {
		String taskRecordClass = Helpers.isQPlus() ? "com.android.server.wm.TaskRecord" : "com.android.server.am.TaskRecord";
		Helpers.hookAllConstructors(taskRecordClass, lpparam.classLoader, new MethodHook() {
			@Override
			@SuppressLint("WrongConstant")
			protected void after(final MethodHookParam param) throws Throwable {
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
			}
		});
	}

	private static final List<String> hookedTiles = new ArrayList<String>();
	@SuppressLint("StaticFieldLeak") private static Context qsCtx = null;

	@SuppressLint("MissingPermission")
	public static void QSHapticHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSFactoryImpl", lpparam.classLoader, "createTileInternal", String.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Object mHost = XposedHelpers.getObjectField(param.thisObject, "mHost");
				qsCtx = (Context)XposedHelpers.callMethod(mHost, "getContext");
				Object res = param.getResult();
				if (res != null && !hookedTiles.contains(res.getClass().getCanonicalName())) {
					Helpers.findAndHookMethod(res.getClass().getCanonicalName(), lpparam.classLoader, "handleClick", new MethodHook() {
						@Override
						protected void after(final MethodHookParam param) throws Throwable {
							boolean ignoreSystem = Helpers.getSharedBoolPref(qsCtx, "pref_key_system_qshaptics_ignore", false);
							int opt = Integer.parseInt(Helpers.getSharedStringPref(qsCtx, "pref_key_system_qshaptics", "1"));
							if (opt == 2)
								Helpers.performLightVibration(qsCtx, ignoreSystem);
							else if (opt == 3)
								Helpers.performStrongVibration(qsCtx, ignoreSystem);
						}
					});
					hookedTiles.add(res.getClass().getCanonicalName());
				}
			}
		});
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
		Helpers.findAndHookMethod("com.android.systemui.miui.statusbar.ExpandedNotification", lpparam.classLoader, "hasShownAfterUnlock", XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("com.android.systemui.miui.statusbar.ExpandedNotification", lpparam.classLoader, "setHasShownAfterUnlock", boolean.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setBooleanField(param.thisObject, "mHasShownAfterUnlock", false);
			}
		});
		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.notification.MiuiNotificationCompat", lpparam.classLoader, "isKeptOnKeyguard", Notification.class, XC_MethodReplacement.returnConstant(true));
	}

	private static int appInfoIconResId;
	private static int forceCloseIconResId;
	public static void NotificationRowMenuRes() {
		appInfoIconResId = MainModule.resHooks.addResource("ic_appinfo", Helpers.is12()? R.drawable.ic_appinfo12 : R.drawable.ic_appinfo);
		forceCloseIconResId = MainModule.resHooks.addResource("ic_forceclose", Helpers.is12()? R.drawable.ic_forceclose12 : R.drawable.ic_forceclose);
		if (!Helpers.is12())
		MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_menu_icon_size", 36);
		MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "notification_menu_icon_padding", 0);
	}

	public static void NotificationRowMenuHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "createMenuViews", boolean.class, new MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void after(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				ArrayList<Object> mMenuItems = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mMenuItems");
				Class<?> nmiCls = findClass("com.android.systemui.statusbar.NotificationMenuRow.NotificationMenuItem", lpparam.classLoader);
				Object infoBtn = null;
				Object forceCloseBtn = null;
				int nInfoResId = mContext.getResources().getIdentifier("notification_info", "layout", lpparam.packageName);
				try {
					infoBtn = XposedHelpers.newInstance(nmiCls, mContext, "Application info", nInfoResId, appInfoIconResId);
					forceCloseBtn = XposedHelpers.newInstance(nmiCls, mContext, "Force close", nInfoResId, forceCloseIconResId);
				} catch (Throwable t1) {
					try {
						infoBtn = XposedHelpers.newInstance(nmiCls, mContext, "Application info", LayoutInflater.from(mContext).inflate(nInfoResId, null, false), appInfoIconResId);
						forceCloseBtn = XposedHelpers.newInstance(nmiCls, mContext, "Force close", LayoutInflater.from(mContext).inflate(nInfoResId, null, false), forceCloseIconResId);
					} catch (Throwable t2) {
						XposedBridge.log(t2);
					}
				}
				if (infoBtn == null || forceCloseBtn == null) return;

				mMenuItems.add(infoBtn);
				mMenuItems.add(forceCloseBtn);
				XposedHelpers.setObjectField(param.thisObject, "mMenuItems", mMenuItems);
				FrameLayout mMenuContainer = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
				if (mMenuContainer != null)
				if (Helpers.is12()) {
					View mInfoBtn = (View)XposedHelpers.callMethod(infoBtn, "getMenuView");
					View mForceCloseBtn = (View)XposedHelpers.callMethod(forceCloseBtn, "getMenuView");
					mInfoBtn.setOnClickListener((View.OnClickListener)param.thisObject);
					mForceCloseBtn.setOnClickListener((View.OnClickListener)param.thisObject);
					XposedHelpers.callMethod(mMenuContainer, "addMenuView", mInfoBtn);
					XposedHelpers.callMethod(mMenuContainer, "addMenuView",mForceCloseBtn);
				} else {
					XposedHelpers.callMethod(param.thisObject, "addMenuView", infoBtn, mMenuContainer);
					XposedHelpers.callMethod(param.thisObject, "addMenuView", forceCloseBtn, mMenuContainer);
					//XposedHelpers.callMethod(param.thisObject, "setMenuLocation");
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "onClick", View.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				View view = (View)param.args[0];
				if (view == null || view.getTag() == null || !(view.getTag() instanceof Integer)) return;

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Object mParent = XposedHelpers.getObjectField(param.thisObject, "mParent");
				Object notification = XposedHelpers.callMethod(mParent, "getStatusBarNotification");
				String pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
				int uid = (int)XposedHelpers.callMethod(notification, "getAppUid");
				int user = 0;
				try {
					user = (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", uid);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}

				int iconResId = (int)view.getTag();
				if (iconResId == appInfoIconResId) {
					param.setResult(null);
					Helpers.openAppInfo(mContext, pkgName, user);
					mContext.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
				} else if (iconResId == forceCloseIconResId) {
					param.setResult(null);
					ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
					if (user != 0)
						XposedHelpers.callMethod(am, "forceStopPackageAsUser", pkgName, user);
					else
						XposedHelpers.callMethod(am, "forceStopPackage", pkgName);
					CharSequence appName = pkgName;
					try {
						appName = mContext.getPackageManager().getApplicationLabel(mContext.getPackageManager().getApplicationInfo(pkgName, 0));
					} catch (Throwable t) {}
					Toast.makeText(mContext, Helpers.getModuleRes(mContext).getString(R.string.force_closed, appName), Toast.LENGTH_SHORT).show();
				}
			}
		});

		if (!Helpers.is12()) {
			Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "onHeightUpdate", new MethodHook() {
				@Override
				protected void after(final MethodHookParam param) throws Throwable {
					FrameLayout mMenuContainer = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
					if (mMenuContainer != null) mMenuContainer.setTranslationY(0);
				}
			});

			Helpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "setMenuLocation", new MethodHook() {
				@Override
				protected void before(final MethodHookParam param) throws Throwable {
					param.setResult(null);
					float mHorizSpaceForIcon = XposedHelpers.getFloatField(param.thisObject, "mHorizSpaceForIcon");
					boolean mSnapping = XposedHelpers.getBooleanField(param.thisObject, "mSnapping");
					FrameLayout mMenuContainer = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
					Object mParent = XposedHelpers.getObjectField(param.thisObject, "mParent");
					if (mMenuContainer == null || mParent == null) return;
					int width = (int)XposedHelpers.callMethod(mParent, "getWidth");
					int height = (int)XposedHelpers.callMethod(mParent, "getIntrinsicHeight");
					boolean hasExtraTopPadding = (boolean)XposedHelpers.callMethod(mParent, "hasExtraTopPadding");
					int extraTopPadding = hasExtraTopPadding ? (int)XposedHelpers.callMethod(mParent, "getPaddingTop") : 0;
					float density = mMenuContainer.getResources().getDisplayMetrics().density;
					float padding = 10 * density;
					float startingHeight = height / 2.0f - mHorizSpaceForIcon - padding - extraTopPadding;
					Object sbNotification = XposedHelpers.callMethod(mParent, "getStatusBarNotification");
					int mImportance = XposedHelpers.getIntField(sbNotification, "mImportance");
					if (!mSnapping && mMenuContainer.isAttachedToWindow()) {
						int childCount = mMenuContainer.getChildCount();
						int row = 0;
						int col = 0;
						if (mImportance == 1) mHorizSpaceForIcon = 24 * density;
						for (int i = 0; i < childCount; i++)
							if (mImportance == 1) {
								View childAt = mMenuContainer.getChildAt(i);
								childAt.setX((float)width - padding - (mHorizSpaceForIcon + padding) * (col + 1));
								childAt.setY(height / 2.0f - mHorizSpaceForIcon / 2.0f - padding / 2.0f - extraTopPadding);
								col++;
							} else {
								View childAt = mMenuContainer.getChildAt(i);
								childAt.setX((float)width - (mHorizSpaceForIcon + (col == 0 ? 2 * padding : 1.5f * padding)) * (col + 1));
								childAt.setY(startingHeight + mHorizSpaceForIcon * row + padding);
								col++;
								if (i % 2 == 1) {
									col = 0;
									row++;
								}
							}
					}
				}
			});
		}
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
		Helpers.findAndHookMethod("com.android.server.VibratorService", lpparam.classLoader, "systemReady", new MethodHook() {
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

		Helpers.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "vibrate", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				//XposedBridge.log(Arrays.toString(new Throwable().getStackTrace()));
				String pkgName = (String)param.args[1];
				if (pkgName == null) return;
				if (checkVibration(pkgName, param.thisObject)) param.setResult(null);
			}
		});

		if (Helpers.isNougat())
		Helpers.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "vibratePattern", new MethodHook() {
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
		MainModule.resHooks.setObjectReplacement("com.android.systemui", "integer", "quick_settings_qqs_count_portrait", cols);
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
		Helpers.hookAllMethods("com.android.systemui.qs.TileLayout", lpparam.classLoader, "addTile", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				updateLabelsVisibility(param.args[0], XposedHelpers.getIntField(param.thisObject, "mRows"), ((ViewGroup)param.thisObject).getResources().getConfiguration().orientation);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.qs.PagedTileLayout", lpparam.classLoader, "addTile", new MethodHook() {
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

		Helpers.findAndHookMethod("com.android.systemui.qs.TileLayout", lpparam.classLoader, "updateResources", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (MainModule.mPrefs.getInt("system_qsgridrows", 1) != 2) return;
				if (!(boolean)param.getResult()) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) return;
				XposedHelpers.setIntField(param.thisObject, "mContentHeight", Math.round(XposedHelpers.getIntField(param.thisObject, "mContentHeight") / 1.5f));
				((ViewGroup)param.thisObject).requestLayout();
			}
		});

		if (MainModule.mPrefs.getInt("system_qsgridrows", 1) == 4)
		Helpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileView", lpparam.classLoader, "createLabel", new MethodHook() {
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

		String windowClass = Helpers.isQPlus() ? "com.android.server.wm.DisplayRotation" : "com.android.server.policy.PhoneWindowManager";
		String rotMethod = Helpers.isQPlus() ? "rotationForOrientation" : "rotationForOrientationLw";
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

	private static void applyHeight(Object thisObject) {
		if (thisObject == null) return;
		ViewGroup view = (ViewGroup)thisObject;
		ViewGroup.LayoutParams lp = view.getLayoutParams();
		Resources res = view.getResources();
		lp.height = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
		view.setLayoutParams(lp);
	}

	public static void StatusBarHeightHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardStatusBarView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				applyHeight(param.thisObject);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader, "setBar", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				applyHeight(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				try {
					ViewGroup mSignalDualNotchGroup = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mSignalDualNotchGroup");
					applyHeight(mSignalDualNotchGroup.findViewById(mSignalDualNotchGroup.getResources().getIdentifier("notch_mobile", "id", lpparam.packageName)));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}

				try {
					ViewGroup mSignalSimpleDualMobileContainer = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mSignalSimpleDualMobileContainer");
					applyHeight(mSignalSimpleDualMobileContainer);
				} catch (Throwable t) {}
			}
		});
	}

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
		Helpers.hookAllConstructors("com.android.server.policy.MiuiGlobalActions", lpparam.classLoader, new MethodHook() {
			@Override
			@SuppressWarnings("ResultOfMethodCallIgnored")
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)param.args[0];
				File powermenu = null;
				File path1 = new File("/cache");
				File path2 = new File("/data/cache");
				File path3 = new File("/data/tmp");
				if (path1.canWrite()) powermenu = new File("/cache/extended_power_menu");
				else if (path2.canWrite()) powermenu = new File("/data/cache/extended_power_menu");
				else if (path3.canWrite()) powermenu = new File("/data/tmp/extended_power_menu");

				if (powermenu == null) {
					Helpers.log("ExtendedPowerMenuHook", "No writable path found!");
					return;
				}
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
			}
		});
	}

	public static void ExtendedPowerMenuHook() {
		Helpers.findAndHookMethod("miui.maml.ScreenElementRoot", null, "issueExternCommand", String.class, Double.class, String.class, new MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
			protected void before(MethodHookParam param) throws Throwable {
				String cmd = (String)param.args[0];
				Object scrContext = XposedHelpers.getObjectField(param.thisObject, "mContext");
				Context mContext = (Context)XposedHelpers.getObjectField(scrContext, "mContext");
//				Handler mHandler = (Handler)XposedHelpers.getObjectField(scrContext, "mHandler");
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
					mContext.sendBroadcast(new Intent(GlobalActions.ACTION_PREFIX + "RestartSystemUI"));
//					final WallpaperManager wm = (WallpaperManager)mContext.getSystemService(Context.WALLPAPER_SERVICE);
//					Drawable drawable = wm.getDrawable();
//					ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
//					XposedHelpers.callMethod(am, "forceStopPackage", "com.android.systemui");
//					mHandler.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							if (drawable != null && drawable.getClass() == BitmapDrawable.class) try {
//								wm.setBitmap(((BitmapDrawable)drawable).getBitmap());
//							} catch (Throwable t) {
//								XposedBridge.log(t);
//							}
//						}
//					}, 1000);
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
	}

	public static void HideDismissViewHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "inflateDismissView", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mDismissView = (View)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
				if (mDismissView != null) mDismissView.setVisibility(View.GONE);
			}
		});

		if (!Helpers.findAndHookMethodSilently("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateDismissView", boolean.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mDismissView = (View)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
				if (mDismissView != null) mDismissView.setVisibility(View.GONE);
			}
		})) Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateDismissView", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mDismissView = (View)XposedHelpers.getObjectField(param.thisObject, "mDismissView");
				if (mDismissView != null) mDismissView.setVisibility(View.GONE);
			}
		});
	}

	public static void ReplaceShortcutAppHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.HeaderView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mShortcut = (View)XposedHelpers.getObjectField(param.thisObject, "mShortcut");
				mShortcut.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int user = Helpers.getSharedIntPref(v.getContext(), "pref_key_system_shortcut_app_user", 0);
						String pkgAppName = Helpers.getSharedStringPref(v.getContext(), "pref_key_system_shortcut_app", "");
						if (pkgAppName == null || pkgAppName.equals("")) try {
							View.OnClickListener mOnClickListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mOnClickListener");
							if (mOnClickListener != null) mOnClickListener.onClick(v);
						} catch (Throwable t) {
							XposedHelpers.callMethod(param.thisObject, "onClick", v);
						}

						String[] pkgAppArray = pkgAppName.split("\\|");
						if (pkgAppArray.length < 2) return;

						ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setComponent(name);

						Object mActStarter = XposedHelpers.getObjectField(param.thisObject, "mActStarter");
						if (user != 0) try {
							XposedHelpers.callMethod(mActStarter, "collapsePanels");
							Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
							XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
						} catch (Throwable t) {
							XposedBridge.log(t);
						} else {
							XposedHelpers.callMethod(mActStarter, "startActivity", intent, true);
						}
					}
				});
			}
		});
	}

	public static void ReplaceClockAppHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.HeaderView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mClock = (View)XposedHelpers.getObjectField(param.thisObject, "mClock");
				mClock.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int user = Helpers.getSharedIntPref(v.getContext(), "pref_key_system_clock_app_user", 0);
						String pkgAppName = Helpers.getSharedStringPref(v.getContext(), "pref_key_system_clock_app", "");
						if (pkgAppName == null || pkgAppName.equals("")) try {
							View.OnClickListener mOnClickListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mOnClickListener");
							if (mOnClickListener != null) mOnClickListener.onClick(v);
						} catch (Throwable t) {
							XposedHelpers.callMethod(param.thisObject, "onClick", v);
						}

						String[] pkgAppArray = pkgAppName.split("\\|");
						if (pkgAppArray.length < 2) return;

						ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setComponent(name);

						Object mActStarter = XposedHelpers.getObjectField(param.thisObject, "mActStarter");
						if (user != 0) try {
							XposedHelpers.callMethod(mActStarter, "collapsePanels");
							Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
							XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
						} catch (Throwable t) {
							XposedBridge.log(t);
						} else {
							XposedHelpers.callMethod(mActStarter, "startActivity", intent, true);
						}
					}
				});
			}
		});
	}

	public static void ReplaceCalendarAppHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.HeaderView", lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mDateView = (View)XposedHelpers.getObjectField(param.thisObject, "mDateView");
				mDateView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int user = Helpers.getSharedIntPref(v.getContext(), "pref_key_system_calendar_app_user", 0);
						String pkgAppName = Helpers.getSharedStringPref(v.getContext(), "pref_key_system_calendar_app", "");
						if (pkgAppName == null || pkgAppName.equals("")) try {
							View.OnClickListener mOnClickListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mOnClickListener");
							if (mOnClickListener != null) mOnClickListener.onClick(v);
						} catch (Throwable t) {
							XposedHelpers.callMethod(param.thisObject, "onClick", v);
						}

						String[] pkgAppArray = pkgAppName.split("\\|");
						if (pkgAppArray.length < 2) return;

						ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.setComponent(name);

						Object mActStarter = XposedHelpers.getObjectField(param.thisObject, "mActStarter");
						if (user != 0) try {
							XposedHelpers.callMethod(mActStarter, "collapsePanels");
							Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
							XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
						} catch (Throwable t) {
							XposedBridge.log(t);
						} else {
							XposedHelpers.callMethod(mActStarter, "startActivity", intent, true);
						}
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
		String thirdCls = Helpers.is11() ? "com.miui.securitycenter.provider.ThirdMonitorProvider" : "com.miui.securitycenter.provider.ThirdDesktopProvider";
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
					Intent intent = (Intent)origIntent.clone();
					String action = intent.getAction();
					if (action == null) return;
					if (!action.equals(Intent.ACTION_SEND) && !action.equals(Intent.ACTION_SENDTO) && !action.equals(Intent.ACTION_SEND_MULTIPLE)) return;
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
						} catch (Throwable t) {}
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
						} catch (Throwable t) {}
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

	public static void VolumeTimerValuesRes() {
		MainModule.resHooks.setResReplacement("com.android.systemui", "array", "miui_volume_timer_segments", R.array.miui_volume_timer_segments);
		MainModule.resHooks.setResReplacement("com.android.systemui", "array", "miui_volume_timer_segments_title", R.array.miui_volume_timer_segments_title);
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
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "makeStatusBarView", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				ViewGroup mNotificationPanel = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mNotificationPanel");
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

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "onScreenTurnedOff", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if (audioViz != null) audioViz.updateScreenOn(false);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateKeyguardState", boolean.class, boolean.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Object mStatusBarKeyguardViewManager = XposedHelpers.getObjectField(param.thisObject, "mStatusBarKeyguardViewManager");
				boolean isKeyguardShowingNew = (boolean)XposedHelpers.callMethod(mStatusBarKeyguardViewManager, "isShowing");
				if (isKeyguardShowing != isKeyguardShowingNew) {
					isKeyguardShowing = isKeyguardShowingNew;
					isNotificationPanelExpanded = false;
					updateAudioVisualizerState((Context)XposedHelpers.getObjectField(param.thisObject, "mContext"));
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onExpandingFinished", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean isNotificationPanelExpandedNew = XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
				if (isNotificationPanelExpanded != isNotificationPanelExpandedNew) {
					isNotificationPanelExpanded = isNotificationPanelExpandedNew;
					updateAudioVisualizerState((Context)XposedHelpers.getObjectField(param.thisObject, "mContext"));
				}
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new MethodHook() {
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
					Drawable wallpaperDrawable = wallpaperMgr.getDrawable();
					if (wallpaperDrawable instanceof BitmapDrawable)
					art = ((BitmapDrawable)wallpaperDrawable).getBitmap();
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
		if (Helpers.isQPlus())
			Helpers.hookAllConstructors("com.android.server.wm.DisplayRotation", lpparam.classLoader, new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					XposedHelpers.setIntField(param.thisObject, "mAllowAllRotations", MainModule.mPrefs.getStringAsInt("system_allrotations2", 1) == 2 ? 1 : 0);
				}
			});
		else
			Helpers.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "init", new MethodHook() {
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

		if (!Helpers.isNougat() && MainModule.mPrefs.getBoolean("system_defaultusb_unsecure")) {
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
		Helpers.findAndHookMethod("com.android.systemui.BatteryMeterView", lpparam.classLoader, "update", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				ImageView mBatteryIconView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryIconView");
				mBatteryIconView.setVisibility(View.GONE);
			}
		});
	}

	public static void HideIconsBattery2Hook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.BatteryMeterView", lpparam.classLoader, "update", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				TextView mBatteryTextDigitView = (TextView)XposedHelpers.getObjectField(param.thisObject, "mBatteryTextDigitView");
				mBatteryTextDigitView.setVisibility(View.GONE);
			}
		});
	}

	public static void HideIconsBattery3Hook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.BatteryMeterView", lpparam.classLoader, "updateChargingIconView", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				ImageView mBatteryChargingView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryChargingView");
				mBatteryChargingView.setVisibility(View.GONE);
				try {
					ImageView mBatteryChargingInView = (ImageView)XposedHelpers.getObjectField(param.thisObject, "mBatteryChargingInView");
					mBatteryChargingInView.setVisibility(View.GONE);
				} catch (Throwable ignore) {}
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
		Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, new MethodHook() {
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

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, "updateAlarm", boolean.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				lastState = (boolean)param.args[0];
				updateAlarmVisibility(param.thisObject, lastState);
			}
		});
	}

	public static void HideIconsBluetoothHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarPolicy", lpparam.classLoader, "updateBluetooth", String.class, new MethodHook() {
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

	public static void HideIconsSignalHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "apply", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View[] mMobileSignalGroup = (View[])XposedHelpers.getObjectField(param.thisObject, "mMobileSignalGroup");
				for (View mMobileSignal: mMobileSignalGroup) if (mMobileSignal != null) mMobileSignal.setVisibility(View.GONE);
			}
		});
	}

	public static void HideIconsVPNHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "apply", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mVpn = (View)XposedHelpers.getObjectField(param.thisObject, "mVpn");
				if (mVpn != null) mVpn.setVisibility(View.GONE);
			}
		});
	}

	public static void HideIconsNoSIMsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "apply", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mNoSimsCombo = (View)XposedHelpers.getObjectField(param.thisObject, "mNoSimsCombo");
				if (mNoSimsCombo != null) mNoSimsCombo.setVisibility(View.GONE);
			}
		});
	}

	public static void HideIconsNoWiFiHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "apply", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mWifiGroup = (View)XposedHelpers.getObjectField(param.thisObject, "mWifiGroup");
				if (mWifiGroup != null) mWifiGroup.setVisibility(View.GONE);
			}
		});
	}

	public static void HideIconsVoWiFiHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "apply", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View[] mVowifi = (View[])XposedHelpers.getObjectField(param.thisObject, "mVowifi");
				if (mVowifi == null) return;
				if (mVowifi[0] != null) mVowifi[0].setVisibility(View.GONE);
				if (mVowifi[1] != null) mVowifi[1].setVisibility(View.GONE);
			}
		});
	}

	public static void HideIconsHotspotHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView", lpparam.classLoader, "onHotspotChanged", boolean.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mWifiAp = (View)XposedHelpers.getObjectField(param.thisObject, "mWifiAp");
				if (mWifiAp != null) mWifiAp.setVisibility(View.GONE);
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
				"alarm_clock".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_alarm") ||
				"managed_profile".equals(slotName) && MainModule.mPrefs.getBoolean("system_statusbaricons_profile");
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static void HideIconsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String.class, boolean.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if (checkSlot((String)param.args[0])) param.args[1] = false;
			}
		});
	}

	public static void HideIconsSystemHook() {
		Helpers.findAndHookMethod("android.app.StatusBarManager", null, "setIconVisibility", String.class, boolean.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if (checkSlot((String)param.args[0])) param.args[1] = false;
			}
		});
	}

	public static void BatteryIndicatorHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "makeStatusBarView", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				FrameLayout mStatusBarWindow = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mStatusBarWindow");
				BatteryIndicator indicator = new BatteryIndicator(mContext);
				View panel = mStatusBarWindow.findViewById(mContext.getResources().getIdentifier("notification_panel", "id", lpparam.packageName));
				mStatusBarWindow.addView(indicator, panel != null ? mStatusBarWindow.indexOfChild(panel) + 1 : Math.max(mStatusBarWindow.getChildCount() - 1, 8));
				indicator.setAdjustViewBounds(false);
				indicator.init(param.thisObject);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mBatteryIndicator", indicator);
				Object mNotificationIconAreaController = XposedHelpers.getObjectField(param.thisObject, "mNotificationIconAreaController");
				XposedHelpers.setAdditionalInstanceField(mNotificationIconAreaController, "mBatteryIndicator", indicator);
				Object mBatteryController = XposedHelpers.getObjectField(param.thisObject, "mBatteryController");
				XposedHelpers.setAdditionalInstanceField(mBatteryController, "mBatteryIndicator", indicator);
				XposedHelpers.callMethod(mBatteryController, "fireBatteryLevelChanged");
				XposedHelpers.callMethod(mBatteryController, "firePowerSaveChanged");
				XposedHelpers.callMethod(mBatteryController, "fireExtremePowerSaveChanged");
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setPanelExpanded", boolean.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
				BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
				if (indicator != null) indicator.onExpandingChanged(!isKeyguardShowing && (boolean)param.args[0]);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setQsExpanded", boolean.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean isKeyguardShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isKeyguardShowing");
				if (!isKeyguardShowing) return;
				BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
				if (indicator != null) indicator.onExpandingChanged((boolean)param.args[0]);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateKeyguardState", boolean.class, boolean.class, new MethodHook() {
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

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "fireBatteryLevelChanged", new MethodHook() {
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
				if (indicator != null) indicator.onPowerSaveChanged(XposedHelpers.getBooleanField(param.thisObject, "mIsPowerSaveMode"));
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "fireExtremePowerSaveChanged", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				BatteryIndicator indicator = (BatteryIndicator)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mBatteryIndicator");
				if (indicator != null) indicator.onExtremePowerSaveChanged(XposedHelpers.getBooleanField(param.thisObject, "mIsExtremePowerSaveMode"));
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
				if (Helpers.isQPlus()) {
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
				} else {
					Point mCenterZoomCoords = (Point)XposedHelpers.getObjectField(param.thisObject, "mCenterZoomCoords");
					mClampedCenterZoomCoords.x = mCenterZoomCoords.x;
				}
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

		Helpers.findAndHookMethod("android.app.NotificationChannel", null, "setBlockableSystem", boolean.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				XposedHelpers.setBooleanField(param.thisObject, "mBlockableSystem", true);
			}
		});
	}

	public static void DisableAnyNotificationHook() {
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "isNotificationForcedEnabled", Context.class, String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "isNotificationForcedEnabled", Context.class, String.class, String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "isNotificationForcedFor", Context.class, String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "canSystemNotificationBeBlocked", String.class, XC_MethodReplacement.returnConstant(true));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "containNonBlockableChannel", String.class, XC_MethodReplacement.returnConstant(false));
		Helpers.findAndHookMethod("miui.util.NotificationFilterHelper", null, "getNotificationForcedEnabledList", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				param.setResult(new HashSet<String>());
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
		try { XposedHelpers.setObjectField(thisObject, "mMiWalletCardNum", new TextView(mContext)); } catch (Throwable t) {}
		try { XposedHelpers.setObjectField(thisObject, "mRemoteControllerNum", new TextView(mContext)); } catch (Throwable t) {}
		try { XposedHelpers.setObjectField(thisObject, "mSmartHomeNum", new TextView(mContext)); } catch (Throwable t) {}

		Handler mHandler = (Handler)XposedHelpers.getAdditionalInstanceField(thisObject, "mHandler");
		mHandler.removeMessages(1);
		Message msg = new Message();
		msg.what = 1;
		msg.obj = thisObject;
		mHandler.sendMessageDelayed(msg, 1000);
	}

	private static Object notificationPanelView = null;
	public static void LockScreenShortcutHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$DefaultLeftButton", lpparam.classLoader, "getIcon", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Object img = param.getResult();
				if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
				XposedHelpers.setObjectField(img, "drawable", null);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardBottomAreaView$DefaultRightButton", lpparam.classLoader, "getIcon", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Object img = param.getResult();
				if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
					XposedHelpers.setObjectField(img, "drawable", null);
					return;
				}

				Object thisObject = XposedHelpers.getSurroundingThis(param.thisObject);
				Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
				boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
				if (!opt) return;
				boolean mDarkMode = XposedHelpers.getBooleanField(thisObject, "mDarkMode");
				boolean isNewLS = findClassIfExists("com.android.keyguard.KeyguardCameraView", lpparam.classLoader) != null;
				XposedHelpers.setObjectField(img, "drawable", Helpers.getModuleRes(mContext).getDrawable(mDarkMode ?
					(isNewLS ? R.drawable.keyguard_bottom_miuizer_img_dark : R.drawable.keyguard_bottom_miuizer_oldimg_dark) :
					(isNewLS ? R.drawable.keyguard_bottom_miuizer_img_light : R.drawable.keyguard_bottom_miuizer_oldimg_light), mContext.getTheme()
				));
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "initTipsView", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				boolean opt = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_image");
				if (!opt) return;
				TextView mRightAffordanceViewTips = (TextView)XposedHelpers.getObjectField(param.thisObject, "mRightAffordanceViewTips");
				if (mRightAffordanceViewTips != null) mRightAffordanceViewTips.setText(Helpers.getModuleRes(mRightAffordanceViewTips.getContext()).getString(R.string.system_lockscreenshortcuts_right_image_hint));
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader, "launchCamera", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
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
			}
		});

		if (findClassIfExists("com.android.keyguard.KeyguardCameraView", lpparam.classLoader) != null) {
			Helpers.hookAllMethods("com.android.keyguard.KeyguardCameraView", lpparam.classLoader, "setDarkMode", new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					XposedHelpers.callMethod(param.thisObject, "updatePreViewBackground");
				}
			});

			Helpers.findAndHookMethod("com.android.keyguard.KeyguardCameraView", lpparam.classLoader, "updatePreView", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					View mPreViewContainer = (View)XposedHelpers.getObjectField(param.thisObject, "mPreViewContainer");
					if ("active".equals(mPreViewContainer.getTag())) {
						XposedHelpers.setFloatField(param.thisObject, "mIconCircleAlpha", 0.0f);
						((View)param.thisObject).invalidate();
					}
				}
			});

			Helpers.findAndHookMethod("com.android.keyguard.KeyguardCameraView", lpparam.classLoader, "updatePreViewBackground", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");

					boolean mDarkMode = XposedHelpers.getBooleanField(param.thisObject, "mDarkMode");
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

			Helpers.findAndHookMethod("com.android.keyguard.KeyguardCameraView", lpparam.classLoader, "handleMoveDistanceChanged", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					View mIconView = (View)XposedHelpers.getObjectField(param.thisObject, "mIconView");
					if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
						if (mIconView != null) mIconView.setVisibility(View.GONE);
						param.setResult(null);
					} else if (mIconView != null) mIconView.setVisibility(View.VISIBLE);
				}
			});

			Helpers.findAndHookMethod("com.android.keyguard.KeyguardCameraView", lpparam.classLoader, "startFullScreenAnim", new MethodHook() {
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
		} else {
			Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateWallpaper", boolean.class, new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					if ((boolean)param.args[0]) try {
						XposedHelpers.callMethod(param.thisObject, "setCameraImage", false);
					} catch (Throwable t1) {
						XposedHelpers.callMethod(param.thisObject, "setCameraImage");
					}
				}
			});

			Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "setCameraImage", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					View mView = (View)XposedHelpers.getObjectField(param.thisObject, "mKeyguardRightView");
					Object mUpdateMonitor = XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor");
					boolean mDarkMode = (boolean)XposedHelpers.callMethod(mUpdateMonitor, "isLightWallpaperBottom");
					if (modifyCameraImage(mContext, mView, mDarkMode)) {
						try {
							XposedHelpers.setBooleanField(param.thisObject, "mGetCameraImageSucceed", false);
						} catch (Throwable t) {}
						param.setResult(null);
					}
				}
			});
		}

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

							if (key.contains("pref_key_system_lockscreenshortcuts_left")) {
								Object leftView = null;
								try {
									leftView = XposedHelpers.getObjectField(XposedHelpers.getObjectField(notificationPanelView, "mKeyguardLeftView"), "mKeyguardMoveLeftView");
								} catch (Throwable t) {
									XposedBridge.log(t);
								}

								if (leftView != null) try {
									XposedHelpers.callMethod(leftView, "reloadListItems");
								} catch (Throwable t1) {
									try {
										XposedHelpers.callMethod(leftView, "updateShortcuts");
									} catch (Throwable t2) {
										try {
											XposedHelpers.callMethod(leftView, "initKeyguardLeftItems");
										} catch (Throwable t3) {
											try {
												XposedHelpers.callMethod(leftView, "initKeyguardLeftItemInfos");
											} catch (Throwable t4) {
												XposedBridge.log(t4);
											}
										}
									}
								}
							}
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardMoveHelper", lpparam.classLoader, "setTranslation", float.class, boolean.class, boolean.class, boolean.class, boolean.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				int mCurrentScreen = XposedHelpers.getIntField(param.thisObject, "mCurrentScreen");
				if (mCurrentScreen != 1) return;
				if ((float)param.args[0] < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) param.args[0] = 0.0f;
				else if ((float)param.args[0] > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off")) param.args[0] = 0.0f;
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardMoveHelper", lpparam.classLoader, "fling", float.class, boolean.class, boolean.class, new MethodHook() {
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
						int align = MainModule.mPrefs.getStringAsInt("system_lockscreenshortcuts_left_align", 2);
						lp.topMargin = lp.bottomMargin;
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

		String leftViewCls = "com.android.keyguard.negative.MiuiKeyguardMoveLeftControlCenterView";

		Helpers.findAndHookConstructor(leftViewCls, lpparam.classLoader, Context.class, AttributeSet.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Handler mHandler = new LeftControlCenterHandler(mContext.getMainLooper());
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mHandler", mHandler);
			}
		});

		if (!Helpers.findAndHookMethodSilently(leftViewCls, lpparam.classLoader, "reloadListItems", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				initLeftView(param.thisObject);
				param.setResult(null);
			}
		})) if (!Helpers.findAndHookMethodSilently(leftViewCls, lpparam.classLoader, "updateShortcuts", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				initLeftView(param.thisObject);
				param.setResult(null);
			}
		})) if (!Helpers.findAndHookMethodSilently(leftViewCls, lpparam.classLoader, "initKeyguardLeftItems", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				initLeftView(param.thisObject);
				param.setResult(null);
			}
		})) Helpers.findAndHookMethod(leftViewCls, lpparam.classLoader, "initKeyguardLeftItemInfos", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				initLeftView(param.thisObject);
				param.setResult(null);
			}
		});

		if (!Helpers.findAndHookMethodSilently(leftViewCls, lpparam.classLoader, "onClick", View.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				if (handleStockShortcut((View)param.args[0])) param.setResult(null);
			}
		})) Helpers.findAndHookMethod(leftViewCls, lpparam.classLoader, "onFinishInflate", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				View mSmartHomeLinearLayout = (View)XposedHelpers.getObjectField(param.thisObject, "mSmartHomeLinearLayout");
				View mRemoteCenterLinearLayout = (View)XposedHelpers.getObjectField(param.thisObject, "mRemoteCenterLinearLayout");
				final View.OnClickListener mListener = (View.OnClickListener)XposedHelpers.getObjectField(param.thisObject, "mListener");
				View.OnClickListener mNewListener = new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (!handleStockShortcut(view))
						if (mListener != null) mListener.onClick(view);
					}
				};
				mSmartHomeLinearLayout.setOnClickListener(mNewListener);
				mRemoteCenterLinearLayout.setOnClickListener(mNewListener);
			}
		});
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
			protected void after(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				if (act == null) return;
				Intent intent = act.getIntent();
				if (intent == null) return;
				boolean mFromSecureKeyguard = intent.getBooleanExtra("StartActivityWhenLocked", false);
				boolean mStartedFromLockScreen = false;
				try {
					mStartedFromLockScreen = (boolean)XposedHelpers.getAdditionalInstanceField(act.getApplication(), "wasStartedFromLockScreen");
				} catch (Throwable t) {}
				if (mFromSecureKeyguard || mStartedFromLockScreen) {
					XposedHelpers.setAdditionalInstanceField(act.getApplication(), "wasStartedFromLockScreen", true);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
						act.setShowWhenLocked(true);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
						act.setInheritShowWhenLocked(true);
					} else {
						act.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
					}
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
			Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "UpdateMediaPosition:" + pkgName);
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

		if (Helpers.isQPlus())
			Helpers.findAndHookConstructor("android.media.session.MediaSession", null, Context.class, String.class, Bundle.class, hook);
		else
			Helpers.findAndHookConstructor("android.media.session.MediaSession", null, Context.class, String.class, int.class, hook);

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
					mContext.registerReceiver(mSeekToReceiver, new IntentFilter(GlobalActions.ACTION_PREFIX + "SeekToMediaPosition:" + mContext.getPackageName()));
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

	@SuppressLint("DefaultLocale")
	private static String msToStr(long timeMillis) {
		return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(timeMillis), TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMillis)));
	}

	public static void MediaNotificationSeekBarSysUIHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.notification.NotificationViewWrapper", lpparam.classLoader, "wrap", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (param.args.length > 3) return;
				Object res = param.getResult();
				if (res == null) return;
				String cls = res.getClass().getSimpleName();
				if (!"NotificationMediaTemplateViewWrapper".equals(cls) && !"NotificationMediaCustomTemplateViewWrapper".equals(cls)) return;
				View mView = (View)XposedHelpers.getObjectField(res, "mView");
				Object mRow = XposedHelpers.getObjectField(res, "mRow");
				if (mView == null || mRow == null || !"bigMediaNarrow".equals(mView.getTag())) return;
				if (mView.getId() != mView.getResources().getIdentifier("status_bar_latest_event_content", "id", "android")) return;
				LinearLayout mediaLayout = mView.findViewById(mView.getResources().getIdentifier("notification_main_column", "id", "android"));
				if (mediaLayout == null || mediaLayout.findViewWithTag("notifmediaseekbar") != null) return;

				mediaLayout.setClipToPadding(false);
				mediaLayout.setClipChildren(false);

				float density = mView.getResources().getDisplayMetrics().density;
				String pkgName = (String)XposedHelpers.callMethod(XposedHelpers.callMethod(mRow, "getStatusBarNotification"), "getPackageName");

				Context ctx = mView.getContext();
				FrameLayout container = new FrameLayout(ctx);
				container.setTag("notifmediaseekbar");
				container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				container.setClipToPadding(false);
				container.setClipChildren(false);
				container.setPadding(0, Math.round(10 * density), 0, 0);
				//container.setTranslationY(-Math.round(7 * density));

				final TextView posDur = new TextView(ctx);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				lp.gravity = Gravity.END;
				posDur.setLayoutParams(lp);
				posDur.setGravity(Gravity.END);
				posDur.setTranslationY(-Math.round(15 * density));
				posDur.setPadding(0, 0, Math.round(16 * density), 0);
				try {
					posDur.setTextColor(mView.getResources().getColor(mView.getResources().getIdentifier("media_notification_app_name_text_color", "color", "android"), ctx.getTheme()));
					posDur.setTextSize(TypedValue.COMPLEX_UNIT_PX, mView.getResources().getDimensionPixelSize(mView.getResources().getIdentifier("media_notification_app_name_text_size", "dimen", "android")));
				} catch (Throwable t) {
					TextView appName = mView.findViewById(mView.getResources().getIdentifier("media_app_name", "id", "android"));
					if (appName != null) {
						posDur.setTextColor(appName.getCurrentTextColor());
						posDur.setTextSize(TypedValue.COMPLEX_UNIT_PX, appName.getTextSize());
					}
				}

				final SeekBar seekBar = new SeekBar(ctx);

				boolean opt = MainModule.mPrefs.getBoolean("system_notifmediaseekbar_full");
				if (opt) {
					seekBar.setPadding(Math.round(15 * density), Math.round(5 * density), Math.round(15 * density), Math.round(5 * density));
				} else {
					seekBar.setProgressDrawable(Helpers.getModuleRes(ctx).getDrawable(R.drawable.seekbar, ctx.getTheme()));
					seekBar.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, Math.round(10 * density)));
					seekBar.setThumb(null);
					seekBar.setBackground(null);
					seekBar.setPadding(Math.round(16 * density), 0, Math.round(16 * density), 0);
				}
				seekBar.setMax(100);
				seekBar.setProgress(0);
				seekBar.setTag(false);
				seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser) posDur.setText(msToStr(progress).concat(" / ").concat(msToStr(seekBar.getMax())));
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						seekBar.setTag(true);
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						seekBar.setTag(false);
						Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "SeekToMediaPosition:" + pkgName);
						intent.putExtra("android.media.metadata.POSITION", Long.valueOf(seekBar.getProgress()));
						seekBar.getContext().sendBroadcast(intent);
					}
				});
				long rnd = Math.round(Math.random() * 1000);
				seekBar.setContentDescription(String.valueOf(rnd));

				container.addView(seekBar);
				container.addView(posDur);
				mediaLayout.addView(container, 1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

				SeekBarReceiver seekBarUpdate = new SeekBarReceiver(mView, seekBar, posDur) {
					@Override
					public void onReceive(Context context, Intent intent) {
						try {
							if (!parent.isAttachedToWindow() || !seekBar.isAttachedToWindow()) {
								context.unregisterReceiver(this);
								seekBar = null;
								parent = null;
								return;
							}
							if (seekBar == null || (boolean)seekBar.getTag() || intent == null || intent.getExtras() == null) return;
							long dur = intent.getLongExtra(MediaMetadata.METADATA_KEY_DURATION, 0L);
							long pos = intent.getLongExtra("android.media.metadata.POSITION", 0L);
							seekBar.setMax((int)dur);
							seekBar.setProgress((int)pos);
							posDur.setText(msToStr(pos).concat(" / ").concat(msToStr(dur)));
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
				mView.getContext().registerReceiver(seekBarUpdate, new IntentFilter(GlobalActions.ACTION_PREFIX + "UpdateMediaPosition:" + pkgName));
			}
		});
	}

	public static void Network4GtoLTEHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.policy.TelephonyIcons", lpparam.classLoader, "getNetworkTypeName", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				String net = (String)param.getResult();
				if ("4G".equals(net)) param.setResult("LTE");
				else if ("4G+".equals(net)) param.setResult("LTE+");
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private static TextView lux = null;
	private static class LuxListener implements SensorEventListener {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (lux != null && lux.isAttachedToWindow()) try {
				Long last = (Long)lux.getTag();
				if (last != null && currentTimeMillis() - last < 750) return;
				lux.setText(Helpers.getModuleContext(lux.getContext()).getResources().getString(R.string.lux, String.valueOf(Math.round(event.values[0]))));
				lux.setTag(currentTimeMillis());
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	}

//	private static View getBrightnessSlider(Object statusBar) {
//		try {
//			View mStatusBarWindow = (View)XposedHelpers.getObjectField(statusBar, "mStatusBarWindow");
//			if (mStatusBarWindow == null) return null;
//			ViewGroup mQSFragment = mStatusBarWindow.findViewById(mStatusBarWindow.getResources().getIdentifier("qs_frame", "id", "com.android.systemui"));
//			if (mQSFragment == null || mQSFragment.getChildCount() == 0) return null;
//			Object mContainer = mQSFragment.getChildAt(0);
//			if (mContainer == null) return null;
//			return (View)XposedHelpers.callMethod(mContainer, "getBrightnessView");
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//			return null;
//		}
//	}

	private static void registerLuxListener(Object statusBar) {
		try {
			Context mContext = (Context)XposedHelpers.getObjectField(statusBar, "mContext");
			PowerManager powerMgr = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
			if (!powerMgr.isInteractive()) return;
			boolean sBootCompleted = XposedHelpers.getBooleanField(statusBar, "sBootCompleted");
			if (!sBootCompleted) return;
			SensorManager sensorMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			LuxListener mLuxListener = (LuxListener)XposedHelpers.getAdditionalInstanceField(statusBar, "mLuxListener");
			sensorMgr.registerListener(mLuxListener, sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
			XposedHelpers.setAdditionalInstanceField(statusBar, "mListenerEnabled", true);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static void unregisterLuxListener(Object statusBar) {
		try {
			Context mContext = (Context)XposedHelpers.getObjectField(statusBar, "mContext");
			SensorManager sensorMgr = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			LuxListener mLuxListener = (LuxListener)XposedHelpers.getAdditionalInstanceField(statusBar, "mLuxListener");
			sensorMgr.unregisterListener(mLuxListener);
			XposedHelpers.setAdditionalInstanceField(statusBar, "mListenerEnabled", false);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void BrightnessLuxHook(LoadPackageParam lpparam) {
		Helpers.findAndHookConstructor("com.android.systemui.settings.ToggleSliderView", lpparam.classLoader, Context.class, AttributeSet.class, int.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context context = (Context)param.args[0];
				if (((View)param.thisObject).getId() != context.getResources().getIdentifier("qs_brightness", "id", lpparam.packageName)) return;

				lux = new TextView(context);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				lp.alignWithParent = true;
				lp.addRule(RelativeLayout.CENTER_IN_PARENT);
				lux.setLayoutParams(lp);
				lux.setGravity(Gravity.CENTER);
				lux.setTextColor(Helpers.isNightMode(context) ? 0xFFBDBDBE : 0xFF606060);

				boolean isAlt = MainModule.mPrefs.getBoolean("system_showlux_style");
				if (Helpers.isNightMode(context))
					lux.setShadowLayer(isAlt ? 0.1f : lux.getResources().getDisplayMetrics().density * 1.5f, 0, 1f, isAlt ? 0x66222224 : 0x99222224);
				else
					lux.setShadowLayer(0, 0, 0, Color.WHITE);

				if (isAlt) {
					lux.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
					lux.setAlpha(0.25f);
				}

				((ViewGroup)param.thisObject).addView(lux);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "makeStatusBarView", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				LuxListener mLuxListener = new LuxListener();
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mLuxListener", mLuxListener);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mListenerEnabled", false);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setPanelExpanded", boolean.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean isExpanded = (boolean)param.args[0];
				boolean mListenerEnabled = (boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mListenerEnabled");

				if (isExpanded && !mListenerEnabled)
					registerLuxListener(param.thisObject);
				else if (!isExpanded && mListenerEnabled)
					unregisterLuxListener(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "onScreenTurnedOff", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				unregisterLuxListener(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "onScreenTurnedOn", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean mPanelExpanded = XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
				boolean mListenerEnabled = (boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mListenerEnabled");
				if (mPanelExpanded && !mListenerEnabled) registerLuxListener(param.thisObject);
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "onBootCompleted", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				boolean mPanelExpanded = XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
				boolean mListenerEnabled = (boolean)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mListenerEnabled");
				if (mPanelExpanded && !mListenerEnabled) registerLuxListener(param.thisObject);
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private static TextView mPct = null;
	private static void initPct(ViewGroup container, int source) {
		Context context = container.getContext();
		Resources res = container.getResources();
		if (mPct == null) {
			mPct = new TextView(context);
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
		int colorResId = res.getIdentifier("qs_tile_icon_disabled_color", "color", "com.android.systemui");
		mPct.setTextColor(res.getColor(colorResId, container.getContext().getTheme()));
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
				initPct(mStatusBarWindow, 1);
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

//		Helpers.findAndHookMethod("com.android.systemui.statusbar.policy.BrightnessMirrorController", lpparam.classLoader, "updateResources", new MethodHook() {
//			@Override
//			protected void after(final MethodHookParam param) throws Throwable {
//			}
//		});

		Helpers.hookAllMethods("com.android.systemui.settings.BrightnessController", lpparam.classLoader, "onChanged", new MethodHook() {
			@Override
			@SuppressLint("SetTextI18n")
			protected void after(final MethodHookParam param) throws Throwable {
				if (mPct == null || (int)mPct.getTag() != 1) return;
				int currentLevel = (int)(param.args[2] instanceof Integer ? param.args[2] : param.args[3]);
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				int maxLevel = mContext.getResources().getInteger(mContext.getResources().getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android"));
				mPct.setText(((currentLevel * 100) / maxLevel) + "%");
			}
		});
	}

	public static void HideProximityWarningHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.classLoader, "showHint", XC_MethodReplacement.DO_NOTHING);
	}

	public static void HideIconsVoLTERes() {
		MainModule.resHooks.setObjectReplacement("com.android.systemui", "bool", "status_bar_hide_volte", true);
	}

	public static void HideIconsVoLTEHook(LoadPackageParam lpparam) {
		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("com.android.systemui.MCCUtils", lpparam.classLoader, "isHideVolte", Context.class, String.class, XC_MethodReplacement.returnConstant(true));
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

	public static void NoSilentVibrateHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", lpparam.classLoader, "vibrateH", XC_MethodReplacement.DO_NOTHING);
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
		if (Helpers.isNougat()) return;
		Helpers.hookAllMethods("android.content.pm.PackageParser.SigningDetails", null, "checkCapability", XC_MethodReplacement.returnConstant(true));
		Helpers.hookAllMethods("android.content.pm.PackageParser.SigningDetails", null, "checkCapabilityRecover", XC_MethodReplacement.returnConstant(true));
	}

	public static void NoSignatureVerifyServiceHook(LoadPackageParam lpparam) {
		if (!Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(0))) {
			Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "compareSignatures", XC_MethodReplacement.returnConstant(0));
			Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "compareSignaturesCompat", XC_MethodReplacement.returnConstant(0));
			Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "compareSignaturesRecover", XC_MethodReplacement.returnConstant(0));
		}

		Helpers.hookAllMethods("com.android.server.pm.PackageManagerServiceInjector", lpparam.classLoader, "isAllowedInstall", XC_MethodReplacement.returnConstant(true));
		Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "canSkipFullPackageVerification", XC_MethodReplacement.returnConstant(true));
		Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "canSkipFullApkVerification", XC_MethodReplacement.returnConstant(true));
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
		if (Helpers.isQPlus())
			Helpers.findAndHookMethod("android.widget.AbsListView", null, "initAbsListView", new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					((AbsListView)param.thisObject).setOverScrollMode(View.OVER_SCROLL_NEVER);
				}
			});
		else
			Helpers.findAndHookMethod("android.widget.AbsListView", null, "setOverScrollMode", int.class, new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					param.args[0] = View.OVER_SCROLL_NEVER;
				}
			});
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

	public static void BlurVolumeDialogBackgroundHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogView", lpparam.classLoader, "onAttachedToWindow", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				updateBlurRatio(param.thisObject);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogView", lpparam.classLoader, "onExpandStateUpdated", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				updateBlurRatio(param.thisObject);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", lpparam.classLoader, "initDialog", new MethodHook() {
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
		MainModule.resHooks.setObjectReplacement("com.android.systemui", "fraction", "miui_volume_dim_behind_collapsed", 0.0f);
		MainModule.resHooks.setObjectReplacement("com.android.systemui", "fraction", "miui_volume_dim_behind_expanded", 0.0f);
	}

	public static void RemoveSecureHook() {
		Helpers.findAndHookMethod(Window.class, "setFlags", int.class, int.class, new MethodHook() {
			protected void before(MethodHookParam param) throws Throwable {
				int flags = (int)param.args[0];
				int mask = (int)param.args[1];
				flags &= ~WindowManager.LayoutParams.FLAG_SECURE;
				mask &= ~WindowManager.LayoutParams.FLAG_SECURE;
				param.args[0] = flags;
				param.args[1] = mask;
			}
		});
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
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setBarState", int.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Object mNotificationPanel = XposedHelpers.getObjectField(param.thisObject, "mNotificationPanel");
				final View mQsFrame = (View)XposedHelpers.getObjectField(mNotificationPanel, "mQsFrame");
				if ((int)param.args[0] == 2) {
					ViewGroup.LayoutParams lp = mQsFrame.getLayoutParams();
					lp.height = 1;
					mQsFrame.setLayoutParams(lp);
				} else {
					Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							ViewGroup.LayoutParams lp = mQsFrame.getLayoutParams();
							lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
							mQsFrame.setLayoutParams(lp);
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

	private static void hookUpdateTime(Object thisObject) {
		try {
			TextView mCurrentDate = (TextView)XposedHelpers.getObjectField(thisObject, "mCurrentDate");
			if (mCurrentDate == null) return;
			mCurrentDate.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
			mCurrentDate.setLineSpacing(0, 1.0f);

			long timestamp = Helpers.getNextMIUIAlarmTime(mCurrentDate.getContext());
			if (timestamp == 0 && MainModule.mPrefs.getBoolean("system_lsalarm_all"))
			timestamp = Helpers.getNextStockAlarmTime(mCurrentDate.getContext());
			if (timestamp == 0) return;

			StringBuilder alarmStr = new StringBuilder();
			alarmStr.append(mCurrentDate.getText()).append("\n").append(Helpers.getModuleRes(mCurrentDate.getContext()).getString(R.string.system_statusbaricons_alarm_title)).append(" ");
			int format = MainModule.mPrefs.getStringAsInt("system_lsalarm_format", 1);
			if (format == 2) {
				StringBuilder timeStr = new StringBuilder(DateUtils.getRelativeTimeSpanString(timestamp, currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
				timeStr.setCharAt(0, Character.toLowerCase(timeStr.charAt(0)));
				alarmStr.append(timeStr);
			} else {
				SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(mCurrentDate.getContext()) ? "EHmm" : "EHmma"), Locale.getDefault());
				dateFormat.setTimeZone(TimeZone.getDefault());
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.setTimeInMillis(timestamp);
				alarmStr.append(dateFormat.format(calendar.getTime()));
			}
			mCurrentDate.setLineSpacing(0, 1.5f);
			mCurrentDate.setText(alarmStr);
			int pos = Settings.System.getInt(mCurrentDate.getContext().getContentResolver(), "selected_keyguard_clock_position", 0);
			if (pos != 2) mCurrentDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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

		if (Helpers.is12())
			Helpers.findAndHookMethod("com.android.keyguard.clock.MiuiKeyguardSingleClock", lpparam.classLoader, "updateTime", new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					Object mMiuiBaseClock = XposedHelpers.getObjectField(param.thisObject, "mMiuiBaseClock");
					if (mMiuiBaseClock != null) hookUpdateTime(mMiuiBaseClock);
				}
			});
		else if (!Helpers.findAndHookMethodSilently("com.android.keyguard.MiuiKeyguardBaseClock", lpparam.classLoader, "updateTime", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				hookUpdateTime(param.thisObject);
			}
		})) Helpers.findAndHookMethod("com.android.keyguard.MiuiKeyguardClock", lpparam.classLoader, "updateTime", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				hookUpdateTime(param.thisObject);
			}
		});
	}

	private static boolean isSlidingStart = false;
	private static boolean isSliding = false;
	private static float tapStartX = 0;
	private static float tapStartY = 0;
	private static float tapStartPointers = 0;
	private static float tapStartBrightness = 0;
	private static float currentTouchX = 0;
	private static long currentTouchTime = 0;

	public static void StatusBarGesturesHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "makeStatusBarView", new MethodHook() {
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

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.PanelView", lpparam.classLoader, "setExpandedHeightInternal", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				float mExpandedFraction = XposedHelpers.getFloatField(param.thisObject, "mExpandedFraction");
				if (mExpandedFraction > 0.33f) {
					currentTouchTime = 0;
					currentTouchX = 0;
				}
			}
		});

		final Class<?> buCls = XposedHelpers.findClassIfExists("com.android.settingslib.display.BrightnessUtils", lpparam.classLoader);
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "interceptTouchEvent", MotionEvent.class, new MethodHook() {
			@Override
			@SuppressLint("SetTextI18n")
			protected void before(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				MotionEvent event = (MotionEvent)param.args[0];
				switch (event.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
						isSlidingStart = !XposedHelpers.getBooleanField(param.thisObject, "mPanelExpanded");
						tapStartX = event.getX();
						tapStartY = event.getY();
						tapStartPointers = 1;
						tapStartBrightness = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
						if (MainModule.mPrefs.getBoolean("system_showpct")) {
							ViewGroup mStatusBarWindow = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mStatusBarWindow");
							if (mStatusBarWindow == null)
								Helpers.log("StatusBarGesturesHook", "mStatusBarWindow is null");
							else
								initPct(mStatusBarWindow, 2);
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
							GlobalActions.handleAction(mContext, "pref_key_system_statusbarcontrols_dt");
							currentTouchTime = 0L;
							currentTouchX = 0F;
						}
					case MotionEvent.ACTION_POINTER_UP:
					case MotionEvent.ACTION_CANCEL:
						isSlidingStart = false;
						isSliding = false;
						if (mPct != null) mPct.setVisibility(View.GONE);
						break;
					case MotionEvent.ACTION_MOVE:
						if (!isSlidingStart) return;
						Resources res = mContext.getResources();
						DisplayMetrics metrics = res.getDisplayMetrics();
						int sbheight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"));
						if (event.getY() - tapStartY > sbheight) return;
						float delta = event.getX() - tapStartX;
						if (delta == 0) return;
						if (!isSliding && Math.abs(delta) > metrics.widthPixels / 10f) isSliding = true;
						if (!isSliding) return;
						int opt = MainModule.mPrefs.getStringAsInt(tapStartPointers == 2 ? "system_statusbarcontrols_dual" : "system_statusbarcontrols_single", 1);
						if (opt == 2) {
							ContentResolver resolver = mContext.getContentResolver();
							int sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_bright", 2);
							int minLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"));
							int maxLevel = res.getInteger(res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android"));
							boolean isHLG = maxLevel != 255;
							float ratio = delta / metrics.widthPixels;
							if (isHLG) ratio = (ratio >= 0 ? 1 : -1) * (sens == 1 ? 0.66f : (sens == 3 ? 1.66f : 1.0f)) * (float)Math.pow(ratio, 2);
							int nextLevel = Math.min(maxLevel, Math.max(minLevel, Math.round(tapStartBrightness + (maxLevel - minLevel) * ratio)));
							if (!Helpers.isPiePlus()) try {
								int brightnessMode = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
								if (brightnessMode == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
									float adj = nextLevel * 2.0f / (maxLevel - minLevel) - 1.0f;
									XposedHelpers.callStaticMethod(findClass("com.android.systemui.SystemUICompat", lpparam.classLoader), "setTemporaryScreenAutoBrightness", adj);
									Settings.System.putFloat(resolver, "screen_auto_brightness_adj", adj);
								} else {
									XposedHelpers.callStaticMethod(findClass("com.android.systemui.SystemUICompat", lpparam.classLoader), "setTemporaryScreenBrightness", nextLevel);
								}
							} catch (Throwable ignore) {}
							Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, nextLevel);
							if (MainModule.mPrefs.getBoolean("system_showpct") && mPct != null) {
								if (mPct.getVisibility() == View.GONE) {
									mPct.setVisibility(View.VISIBLE);
									mPct.animate().alpha(1.0f).setDuration(300).start();
								}
								int nextLevelGamma = nextLevel;
								if (isHLG && buCls != null)
								nextLevelGamma = (int)XposedHelpers.callStaticMethod(buCls, "convertLinearToGamma", nextLevel, minLevel, maxLevel);
								if ((int)mPct.getTag() == 2) mPct.setText(((nextLevelGamma * 100) / maxLevel) + "%");
							}
						} else if (opt == 3) {
							int sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_vol", 2);
							if (Math.abs(delta) < metrics.widthPixels / ((sens == 1 ? 0.66f : (sens == 3 ? 1.66f : 1.0f)) * 20 * metrics.density)) return;
							tapStartX = event.getX();
							AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
							audioManager.adjustVolume(delta > 0 ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_VIBRATE);
						}
						break;
				}
			}
		});
	}

	public static void ScreenshotConfigHook(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.systemui.screenshot.SaveImageInBackgroundTask", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				try {
					OutputStream mOutputStream = (OutputStream)XposedHelpers.getObjectField(param.thisObject, "mOutputStream");
					if (mOutputStream != null) mOutputStream.close();
					String filePath = (String)XposedHelpers.getObjectField(param.thisObject, "mImageFilePath");
					new File(filePath).delete();
				} catch (Throwable ignore) {}

				Context context = (Context)param.args[0];
				int folder = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_screenshot_path", "1"));
				String dir = Helpers.getSharedStringPref(context, "pref_key_system_screenshot_mypath", "");

				File mScreenshotDir;
				if (folder > 1) {
					if (folder == 4 && !TextUtils.isEmpty(dir))
						mScreenshotDir = new File(dir);
					else
						mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(folder == 2 ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DCIM), "Screenshots");
					if (!mScreenshotDir.exists()) mScreenshotDir.mkdirs();
				} else {
					mScreenshotDir = (File)XposedHelpers.getObjectField(param.thisObject, "mScreenshotDir");
				}

				boolean hasTemp = false;
				String mTempImageFilePath = null;
				try {
					mTempImageFilePath = (String)XposedHelpers.getObjectField(param.thisObject, "mTempImageFilePath");
					hasTemp = true;
				} catch (Throwable ignore) {}

				String mImageFileName = (String)XposedHelpers.getObjectField(param.thisObject, "mImageFileName");
				String mImageFilePath = String.format("%s/%s", mScreenshotDir, mImageFileName);
				if (hasTemp) mTempImageFilePath = String.format("%s/%s", mScreenshotDir, "." + mImageFileName);

				int quality = Helpers.getSharedIntPref(context, "pref_key_system_screenshot_quality", 100);
				int format = Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_screenshot_format", "2"));
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mScreenshotQuality", quality);
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "mScreenshotFormat", format);
				String ext = format <= 2 ? ".jpg" : (format == 3 ? ".png" : ".webp");
				mImageFileName = mImageFileName.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext;
				mImageFilePath = mImageFilePath.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext;

				XposedHelpers.setObjectField(param.thisObject, "mImageFileName", mImageFileName);
				XposedHelpers.setObjectField(param.thisObject, "mImageFilePath", mImageFilePath);
				Object mNotifyMediaStoreData = XposedHelpers.getObjectField(param.thisObject, "mNotifyMediaStoreData");
				XposedHelpers.setObjectField(mNotifyMediaStoreData, "imageFileName", mImageFileName);
				XposedHelpers.setObjectField(mNotifyMediaStoreData, "imageFilePath", mImageFilePath);
				if (hasTemp) {
					XposedHelpers.setObjectField(param.thisObject, "mTempImageFilePath", mTempImageFilePath);
					XposedHelpers.setObjectField(mNotifyMediaStoreData, "tempImageFilePath", mTempImageFilePath);
				}
			}
		});

		Helpers.hookAllMethods("com.android.systemui.screenshot.SaveImageInBackgroundTask", lpparam.classLoader, "doInBackground", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Object[] data = (Object[])param.args[0];
				if (data == null || data.length != 1) return;
				Bitmap image = (Bitmap)XposedHelpers.getObjectField(data[0], "image");
				if (image == null) return;

				int format = 2;
				int quality = 100;
				try {
					format = (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mScreenshotFormat");
					quality = (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mScreenshotQuality");
				} catch (Throwable t) {
					XposedBridge.log(t);
				}

				FileOutputStream mCustomOutputStream;
				try {
					String mTempImageFilePath = (String)XposedHelpers.getObjectField(param.thisObject, "mTempImageFilePath");
					File shot = new File(mTempImageFilePath);
					if (shot.exists()) shot.delete();
					mCustomOutputStream = new FileOutputStream(mTempImageFilePath);
				} catch (Throwable t) {
					String mImageFilePath = (String)XposedHelpers.getObjectField(param.thisObject, "mImageFilePath");
					File shot = new File(mImageFilePath);
					if (shot.exists()) shot.delete();
					mCustomOutputStream = new FileOutputStream(mImageFilePath);
				}

				Bitmap.CompressFormat compress = format <= 2 ? Bitmap.CompressFormat.JPEG : (format == 3 ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.WEBP);
				image.compress(compress, quality, mCustomOutputStream);
				mCustomOutputStream.flush();
				mCustomOutputStream.close();

				try {
					XposedHelpers.setObjectField(param.thisObject, "mOutputStream", new ByteArrayOutputStream());
				} catch (Throwable ignore) {}
			}
		});
	}

	public static void NoNetworkSpeedSeparatorHook(LoadPackageParam lpparam) {
//		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarFactory", lpparam.classLoader, "getCollapsedStatusBarFragmentController", new MethodHook() {
//			@Override
//			protected void before(MethodHookParam param) throws Throwable {
//				Class<?> cls = XposedHelpers.findClass("com.android.systemui.statusbar.phone.StatusBarTypeController$CutoutType", lpparam.classLoader);
//				XposedBridge.log("new: " + cls.getEnumConstants()[3]);
//				param.args[0] = cls.getEnumConstants()[3];
//			}
//		});

		Helpers.hookAllConstructors("com.android.systemui.statusbar.NetworkSpeedSplitter", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				((TextView)param.thisObject).setText("");
			}
		});

		Helpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedSplitter", lpparam.classLoader, "init", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				((TextView)param.thisObject).setText("");
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

		String windowClass = Helpers.isQPlus() ? "com.android.server.wm.DisplayPolicy" : "com.android.server.policy.PhoneWindowManager";
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
		String wpuClass = Helpers.isQPlus() ? "com.android.server.wm.WindowProcessUtils" : "com.android.server.am.ProcessUtils";
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
				filter.addAction(GlobalActions.ACTION_PREFIX + "GetSnoozedNotifications");
				filter.addAction(GlobalActions.ACTION_PREFIX + "UnsnoozeNotification");
				filter.addAction(GlobalActions.ACTION_PREFIX + "CancelNotification");
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mContext.registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();
						ArrayMap<Integer, ArrayMap<String, ArrayMap<String, Object>>> mSnoozedNotifications = (ArrayMap<Integer, ArrayMap<String, ArrayMap<String, Object>>>)XposedHelpers.getObjectField(param.thisObject, "mSnoozedNotifications");
						ArrayMap<String, Long> mSnoozedNotificationDelays = (ArrayMap<String, Long>)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mSnoozedNotificationDelays");

						if (action.equals(GlobalActions.ACTION_PREFIX + "CancelNotification")) try {
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

						if (action.equals(GlobalActions.ACTION_PREFIX + "UnsnoozeNotification")) try {
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
		Helpers.findAndHookMethod("com.android.systemui.settings.ToggleSliderView", lpparam.classLoader, "onAttachedToWindow", new MethodHook() {
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
		});
	}

	private static final float[] startPos = new float[2];
	private static void processLSEvent(MethodHookParam param) {
		MotionEvent event = (MotionEvent)param.args[0];
		if (event.getPointerCount() > 1) return;
		int action = event.getActionMasked();
		if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP) return;

		ViewGroup mKeyguardBottomArea = (ViewGroup)XposedHelpers.getObjectField(param.thisObject, "mKeyguardBottomArea");
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
			Object mStatusBar = XposedHelpers.getObjectField(param.thisObject, "mStatusBar");
			XposedHelpers.callMethod(mStatusBar, "showBouncer");
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void TapToUnlockHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onTouchEvent", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				processLSEvent(param);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onInterceptTouchEvent", new MethodHook() {
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

	public static void TempHideOverlayHook() {
		Helpers.hookAllMethods("android.view.WindowManagerGlobal", null, "addView", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if (param.args[0] == null || !(param.args[1] instanceof WindowManager.LayoutParams) || param.getThrowable() != null) return;
				WindowManager.LayoutParams params = (WindowManager.LayoutParams)param.args[1];
				final View view = (View)param.args[0];
				if (params.type != WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY && params.type != WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) return;

				XposedHelpers.setAdditionalInstanceField(view, "mSavedVisibility", view.getVisibility());
				view.getContext().registerReceiver(new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (view == null) return;
						boolean state = intent.getBooleanExtra("IsFinished", true);
						if (state) {
							view.setVisibility((int)XposedHelpers.getAdditionalInstanceField(view, "mSavedVisibility"));
						} else if (view.getVisibility() != View.GONE) {
							XposedHelpers.setAdditionalInstanceField(view, "mSavedVisibility", view.getVisibility());
							view.setVisibility(View.GONE);
						}
					}
				}, new IntentFilter("miui.intent.TAKE_SCREENSHOT"));
			}
		});
	}

	public static void ScreenshotFloatTimeHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.screenshot.GlobalScreenshot", lpparam.classLoader, "startGotoThumbnailAnimation", Runnable.class, new MethodHook() {
			@Override
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
				mHandler.postDelayed(mQuitThumbnailRunnable, opt * 1000);
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

				int opt = MainModule.mPrefs.getStringAsInt("system_lscurrentcharge", 1);
				if (bundle != null && charge < 100) {
					String curr = Math.abs(bundle.getInt("current_now")) + " mA";
					if (opt == 2)
						param.setResult(hint + " • " + curr);
					else if (opt == 3)
						param.setResult(curr + " • " + hint);
					else if (opt == 4)
						param.setResult(hint + "\n" + curr);
				}
			}
		});

		Helpers.hookAllConstructors("com.android.systemui.statusbar.phone.KeyguardIndicationTextView", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				int opt = MainModule.mPrefs.getStringAsInt("system_lscurrentcharge", 1);
				if (opt != 4) return;
				TextView indicator = (TextView)param.thisObject;
				if (indicator != null) indicator.setSingleLine(false);
			}
		});
	}

	public static void UseNativeRecentsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.recents.misc.SystemServicesProxy", lpparam.classLoader, "isRecentsWithinLauncher", Context.class, XC_MethodReplacement.returnConstant(false));
	}

	public static void NoUnlockAnimationHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.miui.ActivityObserverImpl", lpparam.classLoader, "isTopActivityLauncher", XC_MethodReplacement.returnConstant(false));
	}

	public static void NoSOSHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.keyguard.EmergencyButton", lpparam.classLoader, "updateEmergencyCallButton", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Button mSOS = (Button)param.thisObject;
				if (mSOS.getVisibility() == View.VISIBLE) mSOS.setEnabled(false);
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
		Helpers.findAndHookMethod("com.android.server.UiModeManagerService", lpparam.classLoader, "setForceDark", Context.class, hook);
		Helpers.findAndHookMethod("com.android.server.UiModeManagerService", lpparam.classLoader, "setDarkProp", int.class, int.class, hook);
	}

	public static void MaxNotificationIconsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconContainer", lpparam.classLoader, "getMaxIconCount", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				int opt = MainModule.mPrefs.getStringAsInt("system_maxsbicons", 0);
				param.setResult(opt == -1 ? 9999 : opt);
			}
		});
	}

	public static void MoreNotificationsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.NotificationData", lpparam.classLoader, "shouldRemove", new MethodHook() {
			@Override
			@SuppressWarnings({"unchecked", "SynchronizationOnLocalVariableOrMethodParameter"})
			protected void before(MethodHookParam param) throws Throwable {
				if (param.args[1] == null) return;
				ArrayMap<String, ?> mEntries = (ArrayMap<String, ?>)XposedHelpers.getObjectField(param.thisObject, "mEntries");
				if (mEntries == null) return;
				Object notification;
				String pkgName;
				String srcPkgName = (String)XposedHelpers.callMethod(XposedHelpers.getObjectField(param.args[1], "notification"), "getPackageName");
				int cnt = 0;
				synchronized (mEntries) {
					for (int i = 0; i < mEntries.size(); i++) {
						notification = XposedHelpers.getObjectField(mEntries.valueAt(i), "notification");
						if (notification == null) continue;
						pkgName = (String)XposedHelpers.callMethod(notification, "getPackageName");
						if (pkgName.equals(srcPkgName)) cnt++;
					}
				}
				if (cnt < 24) param.setResult(null);
			}
		});
	}

//	@SuppressWarnings("unchecked")
//	public static void MultiWindowHook() {
//		HashMap<String, Integer> sAppList = (HashMap<String, Integer>)XposedHelpers.getStaticObjectField(findClass("android.util.MiuiMultiWindowUtils", null), "sAppList");
//		sAppList.put(Helpers.modulePkg, 4);
//		XposedHelpers.setStaticObjectField(findClass("android.util.MiuiMultiWindowUtils", null), "sAppList", sAppList);
//	}
//
//	public static void MultiWindowServiceHook(LoadPackageParam lpparam) {
//		Helpers.hookAllMethods("com.android.server.am.ActivityStarterInjector", lpparam.classLoader, "checkFreeformSupport", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				XposedHelpers.setObjectField(param.args[0], "mSupportsFreeformWindowManagement", true);
//			}
//		});
//
//		Helpers.findAndHookMethod("com.android.server.am.ActivityStackSupervisorInjector", lpparam.classLoader, "supportsFreeform", XC_MethodReplacement.returnConstant(true));
//
//		Helpers.hookAllMethods("com.android.server.am.ActivityDisplay", lpparam.classLoader, "resolveWindowingMode", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				Object mSupervisor = XposedHelpers.getObjectField(param.thisObject, "mSupervisor");
//				Object mService = XposedHelpers.getObjectField(mSupervisor, "mService");
//				Object mSupportsMultiWindow = XposedHelpers.getObjectField(mService, "mSupportsMultiWindow");
//				Object mSupportsSplitScreenMultiWindow = XposedHelpers.getObjectField(mService, "mSupportsSplitScreenMultiWindow");
//				Object mSupportsFreeformWindowManagement = XposedHelpers.getObjectField(mService, "mSupportsFreeformWindowManagement");
//				Object mSupportsPictureInPicture = XposedHelpers.getObjectField(mService, "mSupportsPictureInPicture");
//				XposedBridge.log("mSupportsMultiWindow: " + mSupportsMultiWindow);
//				XposedBridge.log("mSupportsSplitScreenMultiWindow: " + mSupportsSplitScreenMultiWindow);
//				XposedBridge.log("mSupportsFreeformWindowManagement: " + mSupportsFreeformWindowManagement);
//				XposedBridge.log("mSupportsPictureInPicture: " + mSupportsPictureInPicture);
//				XposedBridge.log("resolveWindowingMode: " + param.args[0] + ", " + param.args[1] + ", " + param.args[2] + ", " + param.args[3] + " = " + param.getResult());
//			}
//		});
//
//		Helpers.hookAllMethods("com.android.server.pm.PackageManagerService", lpparam.classLoader, "hasSystemFeature", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				if ("android.software.freeform_window_management".equals(param.args[0])) param.setResult(true);
//			}
//		});
//	}
//
//	public static void QSFooterHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.systemui.qs.QSContainerImpl", lpparam.classLoader, "onFinishInflate", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				FrameLayout qs = (FrameLayout)param.thisObject;
//				Context context = qs.getContext();
//				Resources res = context.getResources();
//				LinearLayout mQSFooterContainer = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mQSFooterContainer");
//				LayoutInflater inflater = (LayoutInflater)Helpers.getModuleContext(context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//				View dataView = inflater.inflate(R.layout.qs_footer, mQSFooterContainer, false);
//				dataView.setTag("mydata");
//				LinearLayout.LayoutParams lp0 = (LinearLayout.LayoutParams)dataView.getLayoutParams();
//				int margin = res.getDimensionPixelSize(res.getIdentifier("qs_divider_margin_horizontal", "dimen", context.getPackageName()));
//				int height = res.getDimensionPixelSize(res.getIdentifier("qs_footer_height", "dimen", context.getPackageName()));
//				lp0.setMarginStart(margin);
//				lp0.topMargin = - height / 6;
//				lp0.bottomMargin = height / 4;
//				dataView.setLayoutParams(lp0);
//
//				ImageView icon = dataView.findViewById(R.id.qs_icon);
//				icon.setImageDrawable(Helpers.getModuleRes(context).getDrawable(R.drawable.ic_gps_task, context.getTheme()));
//				ViewGroup.LayoutParams lp1 = icon.getLayoutParams();
//				int iconSize = res.getDimensionPixelSize(res.getIdentifier("qs_footer_data_usage_icon_size", "dimen", context.getPackageName()));
//				lp1.width = iconSize;
//				lp1.height = iconSize;
//				icon.setLayoutParams(lp1);
//
//				TextView data = dataView.findViewById(R.id.qs_data);
//				LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)data.getLayoutParams();
//				margin = res.getDimensionPixelSize(res.getIdentifier("qs_footer_line_margin_start", "dimen", context.getPackageName()));
//				lp2.setMarginStart(margin);
//				lp2.setMarginEnd(margin);
//				data.setLayoutParams(lp2);
//				data.setText("Some data...");
//
//				int textColor = res.getColor(res.getIdentifier("qs_footer_data_usage_text_color", "color", context.getPackageName()), context.getTheme());
//				int textSize = res.getDimensionPixelSize(res.getIdentifier("qs_tile_label_text_size", "dimen", context.getPackageName()));
//				data.setTextColor(textColor);
//				data.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//
//				mQSFooterContainer.addView(dataView);
//			}
//		});
//
//		Helpers.findAndHookMethod("com.android.systemui.qs.QSContainerImpl", lpparam.classLoader, "updateQSDataUsage", boolean.class, new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				LinearLayout mQSFooterContainer = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mQSFooterContainer");
//				View dataView = mQSFooterContainer.findViewWithTag("mydata");
//				mQSFooterContainer.removeView(dataView);
//				mQSFooterContainer.addView(dataView);
//			}
//		});
//	}
//
//	public static void VolumeDialogTimeoutHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", lpparam.classLoader, "computeTimeoutH", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				param.setResult(Math.max((int)param.getResult(), 10000));
//			}
//		});
//	}

}