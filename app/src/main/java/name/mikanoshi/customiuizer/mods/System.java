package name.mikanoshi.customiuizer.mods;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;

public class System {

	public static void ScreenAnimHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "initialize", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

//	Needs res hooks support
//	public static void RotateAnimationHook(XC_LoadPackage.LoadPackageParam lpparam) {
//		try {
//			XposedHelpers.findAndHookMethod("com.android.server.wm.ScreenRotationAnimation", lpparam.classLoader, "startAnimation", "android.view.SurfaceControl.Transaction", long.class, float.class, int.class, int.class, boolean.class, int.class, int.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//					int rotateanim = Integer.parseInt(MainModule.pref.getString("pref_key_system_screenrotate", "0"));
//					if (rotateanim == 1) {
//						param.args[1] = 0;
//						param.args[2] = 0;
//					} else if (rotateanim == 2) {
//						param.args[6] = xfade_exit_id;
//						param.args[7] = xfade_enter_id;
//					}
//				}
//			});
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

	public static void NoLightUpOnChargeHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpNoUpdateLocked", long.class, String.class, int.class, String.class, int.class, new XC_MethodHook() {
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
					//XposedBridge.log("wakeUpNoUpdateLocked: " + String.valueOf(param.args[0]) + " | " + String.valueOf(param.args[1]) + " | " + String.valueOf(param.args[2]) + " | " + String.valueOf(param.args[3]) + " | " + String.valueOf(param.args[4]));
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void NoLightUpOnHeadsetHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakeUpNoUpdateLocked", long.class, String.class, int.class, String.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (param.args[1].equals("com.android.systemui:HEADSET")) param.setResult(false);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void ScramblePINHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardPINView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void NoPasswordHook() {
		try {
			//XposedHelpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, "handleStrongAuthRequiredChanged", int.class, int.class, XC_MethodReplacement.DO_NOTHING);
			XposedHelpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils$StrongAuthTracker", null, "isFingerprintAllowedForUser", int.class, XC_MethodReplacement.returnConstant(true));
			XposedHelpers.findAndHookMethod("com.android.internal.widget.LockPatternUtils", null, "isFingerprintAllowedForUser", int.class, XC_MethodReplacement.returnConstant(true));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

//	public static void NoPasswordKeyguardHook(LoadPackageParam lpparam) {
//		try {
//			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", XC_MethodReplacement.returnConstant(true));
//			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", int.class, XC_MethodReplacement.returnConstant(true));
//			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "isUnlockWithFingerprintPossible", int.class, XC_MethodReplacement.returnConstant(true));
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

	public static void EnhancedSecurityHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptPowerKeyDown", KeyEvent.class, boolean.class, new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		try {
			XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "powerLongPress", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		try {
			XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActions", new XC_MethodHook() {
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
		} catch (Throwable t) {}

		try{
			XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActionsInternal", new XC_MethodHook() {
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
		} catch (Throwable t) {}
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
//			XposedBridge.log("setLockScreenDisabled = " + state + ", " + user);
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

	private static boolean isUnlockedOnce = false;
	public static void NoScreenLockHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "reportSuccessfulStrongAuthUnlockAttempt", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					isUnlockedOnce = true;
				}
			});

			XposedHelpers.findAndHookMethod("android.app.admin.DevicePolicyManagerCompat", lpparam.classLoader, "reportSuccessfulFingerprintAttempt", DevicePolicyManager.class, int.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					isUnlockedOnce = true;
				}
			});

			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardHostView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
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

			XposedHelpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "doKeyguardLocked", Bundle.class, new XC_MethodHook() {
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

			XposedHelpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "setupLocked", new XC_MethodHook() {
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

			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardSecurityModel", lpparam.classLoader, "getSecurityMode", int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					boolean skip = Helpers.getSharedBoolPref(mContext, "pref_key_system_noscreenlock_skip", false);
					if (skip) return;

					int opt = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_noscreenlock", "1"));

					boolean isTrusted = false;
					if (opt == 4) isTrusted = isTrusted(mContext, lpparam.classLoader);
					if (opt == 1 || opt == 3 && !isUnlockedOnce || opt == 4 && !isTrusted) return;

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

			XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader), new XC_MethodHook(10) {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
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
			toast.setPadding(toast.getPaddingLeft() - Math.round(5 * density), toast.getPaddingTop(), toast.getPaddingRight(), toast.getPaddingBottom());

			TextView toastText = toast.findViewById(android.R.id.message);
			if (toastText == null) return;
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
		try {
			XposedHelpers.findAndHookMethod("android.widget.Toast", null, "makeText", Context.class, Looper.class, CharSequence.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					modifyIconLabelToast(param);
				}
			});
		} catch (Throwable t1) {
			try {
				XposedHelpers.findAndHookMethod("android.widget.Toast", null, "makeText", Context.class, CharSequence.class, int.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						modifyIconLabelToast(param);
					}
				});
			} catch (Throwable t2) {
				XposedBridge.log("[CustoMIUIzer][IconLableToasts] Failed to hook Toast.makeText()");
			}
		}
	}

	public static void DoubleTapToSleepHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void NotificationVolumeServiceHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "updateStreamVolumeAlias", boolean.class, String.class, new XC_MethodHook() {
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					int[] streamVolumeAlias = (int[])XposedHelpers.getObjectField(param.thisObject, "mStreamVolumeAlias");
					streamVolumeAlias[1] = 1;
					streamVolumeAlias[5] = 5;
					XposedHelpers.setObjectField(param.thisObject, "mStreamVolumeAlias", streamVolumeAlias);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void NotificationVolumeSettingsHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.settings.MiuiSoundSettings", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					PreferenceFragment fragment = (PreferenceFragment)param.thisObject;
					Resources modRes = Helpers.getModuleRes(fragment.getActivity());
					int iconRes = fragment.getResources().getIdentifier("ic_audio_notification", "drawable", "com.android.settings");
					int order = 6;

					Class<?> vsbCls = XposedHelpers.findClass("com.android.settings.sound.VolumeSeekBarPreference", lpparam.classLoader);
					Method[] initSeekBar = XposedHelpers.findMethodsByExactParameters(fragment.getClass(), void.class, String.class, int.class, int.class);
					if (vsbCls == null || initSeekBar.length == 0) {
						XposedBridge.log("[CustoMIUIzer][Separate Volume Mod] Unable to find class/method in Settings to hook");
						return;
					} else {
						initSeekBar[0].setAccessible(true);
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
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
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					TextView clock = (TextView)param.thisObject;
					if (clock.getId() == clock.getResources().getIdentifier("clock", "id", "com.android.systemui"))
					clock.setText(putSecondsIn(clock.getText()));
				}
			});

			XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader), new XC_MethodHook(10) {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void ExpandNotificationsHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateRowStates", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void PopupNotificationsHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "start", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					IntentFilter intentfilter = new IntentFilter("name.mikanoshi.customiuizer.mods.event.CHANGEFULLSCREEN");
					final Object mStatusBar = param.thisObject;
					mContext.registerReceiver(new BroadcastReceiver() {
						public void onReceive(final Context context, Intent intent) {
							String action = intent.getAction();
							if (action.equals("name.mikanoshi.customiuizer.mods.event.CHANGEFULLSCREEN")) {
								if (mStatusBar != null)
								XposedHelpers.setAdditionalInstanceField(mStatusBar, "mIsFullscreenApp", intent.getBooleanExtra("fullscreen", false));
							}
						}
					}, intentfilter);
				}
			});

			XposedBridge.hookAllMethods(findClass("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader), "addNotification", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void PopupNotificationsFSHook() {
		try {
			XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Activity act = (Activity)param.thisObject;
					if (act == null) return;
					int flags = act.getWindow().getAttributes().flags;
					Intent fullscreenIntent = new Intent("name.mikanoshi.customiuizer.mods.event.CHANGEFULLSCREEN");
					if (flags != 0 && (flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN && !act.getPackageName().equals("com.android.systemui"))
						fullscreenIntent.putExtra("fullscreen", true);
					else
						fullscreenIntent.putExtra("fullscreen", false);
					act.sendBroadcast(fullscreenIntent);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void RecentsBlurRatioHook(LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.systemui.recents.views.RecentsView", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context)param.args[0];
					Handler mHandler = new Handler(mContext.getMainLooper());

					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", 100);
					new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_recents_blur", 100) {
						@Override
						public void onChange(String name, int defValue) {
							XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", Helpers.getSharedIntPref(mContext, name, defValue));
						}
					}.onChange(false);
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.recents.views.RecentsView", lpparam.classLoader, "updateBlurRatio", float.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void DrawerBlurRatioHook(LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.systemui.statusbar.phone.StatusBarWindowManager", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context)param.args[0];
					Handler mHandler = new Handler(mContext.getMainLooper());

					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", 100);
					new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_drawer_blur", 100) {
						@Override
						public void onChange(String name, int defValue) {
							XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomBlurModifier", Helpers.getSharedIntPref(mContext, name, defValue));
						}
					}.onChange(false);
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarWindowManager", lpparam.classLoader, "setBlurRatio", float.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomBlurModifier") / 100f;
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onBlurRatioChanged", float.class, new XC_MethodHook(2) {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					try {
						param.args[0] = XposedHelpers.callMethod(param.thisObject, "getAppearFraction");
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void DrawerThemeBackgroundHook(LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context)param.args[0];
					Handler mHandler = new Handler(mContext.getMainLooper());

					XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier", 100);
					new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_drawer_opacity", 100) {
						@Override
						public void onChange(String name, int defValue) {
							XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier", Helpers.getSharedIntPref(mContext, name, defValue));
						}
					}.onChange(false);
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "onBlurRatioChanged", float.class, new XC_MethodHook(1) {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					param.args[0] = (float)param.args[0] * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier") / 100f;
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void HideNetworkTypeHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.SignalClusterView$PhoneState", lpparam.classLoader, "updateMobileType", String.class, new XC_MethodHook() {
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
								isMobileConnected = netCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
							}
						}
						// View parent = (View)((View)XposedHelpers.getSurroundingThis(param.thisObject)).getParent().getParent().getParent().getParent();
						// parent != null && parent.getId() != parent.getResources().getIdentifier("header_content", "id", lpparam.packageName)
						if (opt == 3 || (opt == 2 && !isMobileConnected)) {
							mMobileType.setText("");
							mSignalDualNotchMobileType.setText("");
						}
						//XposedBridge.log("[CustoMIUIzer] " + String.valueOf(parent) + ", " + String.valueOf(parent.getId()) + " != " + String.valueOf(mMobileType.getResources().getIdentifier("header_content", "id", lpparam.packageName)));
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void TrafficSpeedSpacingHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					TextView meter = (TextView)param.thisObject;
					if (meter != null) try {
						LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)meter.getLayoutParams();
						lp.rightMargin = Math.round(meter.getResources().getDisplayMetrics().density * 4);
						meter.setLayoutParams(lp);
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
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

		try {
			XposedHelpers.findAndHookMethod(ccCls, "showWirelessChargeAnimation", int.class, boolean.class, new XC_MethodHook() {
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
					} else XposedBridge.log("[CustoMIUIzer][ChargeAnimation1] Something is NULL! :)");
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		try {
			XposedHelpers.findAndHookMethod(ccCls, "showRapidChargeAnimation", new XC_MethodHook() {
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
					} else XposedBridge.log("[CustoMIUIzer][ChargeAnimation2] Something is NULL! :)");
				}
			});
		} catch (Throwable t) {}

		try {
			XposedHelpers.findAndHookMethod(ccCls, "showWirelessRapidChargeAnimation", new XC_MethodHook() {
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
					} else XposedBridge.log("[CustoMIUIzer][ChargeAnimation3] Something is NULL! :)");
				}
			});
		} catch (Throwable t) {}
	}

	public static void VolumeStepsHook(LoadPackageParam lpparam) {
		try {
			Class<?> audioCls = findClass("com.android.server.audio.AudioService", lpparam.classLoader);
			XposedHelpers.findAndHookMethod(audioCls, "createStreamStates", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					if (mContext != null) {
						int[] maxStreamVolume = (int[])XposedHelpers.getStaticObjectField(audioCls, "MAX_STREAM_VOLUME");
						for (int i = 0; i < maxStreamVolume.length; i++)
						maxStreamVolume[i] = Math.round(maxStreamVolume[i] * MainModule.mPrefs.getInt("system_volumesteps", 10) / 10.0f);
						XposedHelpers.setStaticObjectField(audioCls, "MAX_STREAM_VOLUME", maxStreamVolume);
					} else XposedBridge.log("[CustoMIUIzer][Volume Steps] Context is NULL!");
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void AutoBrightnessHook(LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, "updateAutoBrightness", boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				XposedBridge.log("mScreenAutoBrightness: " + XposedHelpers.getObjectField(param.thisObject, "mScreenAutoBrightness"));
			}
		});
	}

	private static long measureTime = 0;
	private static long txBytesTotal = 0;
	private static long rxBytesTotal = 0;
	private static long txSpeed = 0;
	private static long rxSpeed = 0;

	private static Pair<Long, Long> getTrafficBytes(Object thisObject) {
		Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
		Uri mNetworkUri = (Uri)XposedHelpers.getObjectField(thisObject, "mNetworkUri");
		Cursor query = mContext.getContentResolver().query(mNetworkUri, null, null, null, null);
		long tx = -1L;
		long rx = -1L;
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
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					Settings.System.putInt(mContext.getContentResolver(), "status_bar_network_speed_interval", MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void DetailedNetSpeedHook(LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllConstructors(findClass("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					TextView meter = (TextView)param.thisObject;
					float density = meter.getResources().getDisplayMetrics().density;
					meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8.0f);
					meter.setSingleLine(false);
					meter.setLines(2);
					meter.setMaxLines(2);
					meter.setLineSpacing(0, 0.7f);
					meter.setPadding(Math.round(meter.getPaddingLeft() + 3 * density), meter.getPaddingTop(), meter.getPaddingRight(), meter.getPaddingBottom());
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "getTotalByte", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Pair<Long, Long> bytes = getTrafficBytes(param.thisObject);
					txBytesTotal = bytes.first;
					rxBytesTotal = bytes.second;
					measureTime = nanoTime();
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "updateNetworkSpeed", new XC_MethodHook() {
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

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "setTextToViewList", CharSequence.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
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

					ArrayList<?> sViewList = (ArrayList<?>)XposedHelpers.getObjectField(param.thisObject, "sViewList");
					for (Object tv : sViewList)
						if (tv != null)
							((TextView)tv).setAlpha(rxSpeed == 0 && txSpeed == 0 ? 0.3f : 1.0f);
				}
			});

			XposedBridge.hookAllMethods(findClass("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader), "onTextChanged", XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void LockScreenAlbumArtHook(LoadPackageParam lpparam) {
		final String utilsClassName = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N ? "com.android.keyguard.MiuiKeyguardUtils" : "com.android.keyguard.wallpaper.KeyguardWallpaperUtils";
		final String getLockWallpaper = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N ? "getLockWallpaperCache" : "getLockWallpaper";

		try {
			XposedHelpers.findAndHookMethod(utilsClassName, lpparam.classLoader, "getLockWallpaperPreview", Context.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(findClass(utilsClassName, lpparam.classLoader), "mAlbumArt");
					if (mAlbumArt != null) param.setResult(new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt));
				}
			});

			XposedHelpers.findAndHookMethod(utilsClassName, lpparam.classLoader, getLockWallpaper, Context.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(findClass(utilsClassName, lpparam.classLoader), "mAlbumArt");
					if (mAlbumArt != null) param.setResult(new Pair<File, Drawable>(new File(""), new BitmapDrawable(((Context)param.args[0]).getResources(), mAlbumArt)));
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateMediaMetaData", boolean.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					MediaMetadata mMediaMetadata = (MediaMetadata)XposedHelpers.getObjectField(param.thisObject, "mMediaMetadata");
					Bitmap art = null;
					if (mMediaMetadata != null) {
						art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
						if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
						if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
					}
					Bitmap mAlbumArt = (Bitmap)XposedHelpers.getAdditionalStaticField(findClass(utilsClassName, lpparam.classLoader), "mAlbumArtSource");
					try {
						if (art == null && mAlbumArt == null) return;
						if (art != null && art.sameAs(mAlbumArt)) return;
					} catch (Throwable t) {}
					XposedHelpers.setAdditionalStaticField(findClass(utilsClassName, lpparam.classLoader), "mAlbumArtSource", art);

					int blur = Helpers.getSharedIntPref(mContext, "pref_key_system_albumartonlock_blur", 0);
					if (art != null && blur > 0)
						XposedHelpers.setAdditionalStaticField(findClass(utilsClassName, lpparam.classLoader), "mAlbumArt", Helpers.fastBlur(art, blur + 1));
					else
						XposedHelpers.setAdditionalStaticField(findClass(utilsClassName, lpparam.classLoader), "mAlbumArt", art);

					Intent setWallpaper = new Intent("com.miui.keyguard.setwallpaper");
					setWallpaper.putExtra("set_lock_wallpaper_result", true);
					mContext.sendBroadcast(setWallpaper);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

//		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "setKeyguardOtherViewVisibility", int.class, new XC_MethodHook() {
//			@Override
//			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//				(int)param.args[0] == 1 ? View.VISIBLE : View.GONE
//			}
//		});
	}

	public static void BetterPopupsNoHideHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "removeHeadsUpNotification", XC_MethodReplacement.DO_NOTHING);
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "removeOldHeadsUpNotification", XC_MethodReplacement.DO_NOTHING);

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager$HeadsUpEntry", lpparam.classLoader, "updateEntry", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					XposedHelpers.setObjectField(param.thisObject, "mRemoveHeadsUpRunnable", new Runnable() {
						@Override
						public void run() {}
					});
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "onExpandingFinished", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "mReleaseOnExpandFinish", true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

//		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "onReorderingAllowed", new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//				XposedBridge.log("onReorderingAllowed");
//			}
//		});
	}

	public static void BetterPopupsSwipeDownHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpTouchHelper", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
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
								XposedBridge.log("[CustoMIUIzer][BetterPopupsSwipeDown] Cannot get context!");
								return;
							}

							Intent expandNotif = new Intent("name.mikanoshi.customiuizer.mods.action.ExpandNotifications");
							expandNotif.putExtra("expand_only", true);
							mContext.sendBroadcast(expandNotif);
						}
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void RotationAnimationHook() {
		try {
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

			XModuleResources modRes = XModuleResources.createInstance(MainModule.MODULE_PATH, null);
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_0_enter", modRes.fwd(enter));
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_0_exit", modRes.fwd(exit));
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_180_enter", modRes.fwd(enter));
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_180_exit", modRes.fwd(exit));
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_minus_90_enter", modRes.fwd(enter));
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_minus_90_exit", modRes.fwd(exit));
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_plus_90_enter", modRes.fwd(enter));
			XResources.setSystemWideReplacement("android", "anim", "screen_rotate_plus_90_exit", modRes.fwd(exit));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void NoVersionCheckHook(LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllMethods(findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader), "checkDowngrade", XC_MethodReplacement.DO_NOTHING);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void ColorizedNotificationTitlesHook() {
		try {
			XposedBridge.hookAllMethods(findClass("android.app.Notification$Builder", null), "bindHeaderAppName", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void CompactNotificationsActionsRes() {
		try {
			XModuleResources modRes = XModuleResources.createInstance(MainModule.MODULE_PATH, null);
			XResources.setSystemWideReplacement("android", "dimen", "notification_action_height", modRes.fwd(R.dimen.notification_action_height));
			XResources.setSystemWideReplacement("android", "dimen", "notification_action_list_height", modRes.fwd(R.dimen.notification_action_height));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void CompactNotificationsPaddingRes(XC_InitPackageResources.InitPackageResourcesParam resparam) {
		try {
			XModuleResources modRes = XModuleResources.createInstance(MainModule.MODULE_PATH, resparam.res);
			resparam.res.setReplacement(resparam.packageName, "dimen", "notification_row_extra_padding", modRes.fwd(R.dimen.notification_row_extra_padding));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void HideFromRecentsHook(LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllConstructors(findClass("com.android.server.am.TaskRecord", lpparam.classLoader), new XC_MethodHook() {
				@Override
				@SuppressLint("WrongConstant")
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					String pkgName = null;
					ComponentName origActivity = (ComponentName)XposedHelpers.getObjectField(param.thisObject, "origActivity");
					ComponentName realActivity = (ComponentName)XposedHelpers.getObjectField(param.thisObject, "realActivity");
					String mCallingPackage = (String)XposedHelpers.getObjectField(param.thisObject, "mCallingPackage");

					if (realActivity != null) pkgName = realActivity.getPackageName();
					if (pkgName == null && origActivity != null) pkgName = origActivity.getPackageName();
					if (pkgName == null) pkgName = mCallingPackage;

					Context mContext = (Context)XposedHelpers.getObjectField(param.args[0], "mContext");
					Set<String> selectedApps = Helpers.getSharedStringSetPref(mContext, "pref_key_system_hidefromrecents_apps");
					if (selectedApps.contains(pkgName)) {
						Intent intent = (Intent)XposedHelpers.getObjectField(param.thisObject, "intent");
						Intent affinityIntent = (Intent)XposedHelpers.getObjectField(param.thisObject, "affinityIntent");
						if (intent != null) intent.addFlags(8388608);
						if (affinityIntent != null) affinityIntent.addFlags(8388608);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static List<String> hookedTiles = new ArrayList<String>();
	@SuppressLint("StaticFieldLeak") private static Context qsCtx = null;

	@SuppressLint("MissingPermission")
	public static void QSHapticHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSFactoryImpl", lpparam.classLoader, "createTileInternal", String.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Object mHost = XposedHelpers.getObjectField(param.thisObject, "mHost");
					qsCtx = (Context)XposedHelpers.callMethod(mHost, "getContext");
					Object res = param.getResult();
					if (res != null && !hookedTiles.contains(res.getClass().getCanonicalName())) try {
						XposedHelpers.findAndHookMethod(res.getClass().getCanonicalName(), lpparam.classLoader, "handleClick", new XC_MethodHook() {
							@Override
							protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
								int opt = Integer.parseInt(Helpers.getSharedStringPref(qsCtx, "pref_key_system_qshaptics", "1"));
								if (opt == 2)
									Helpers.performLightVibration(qsCtx);
								else if (opt == 3)
									Helpers.performStrongVibration(qsCtx);
							}
						});
						hookedTiles.add(res.getClass().getCanonicalName());
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void BackGestureAreaHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.fsgesture.GestureStubView", lpparam.classLoader, "getGestureStubWindowParam", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					WindowManager.LayoutParams lp = (WindowManager.LayoutParams)param.getResult();
					int pct = MainModule.mPrefs.getInt("controls_fsg_coverage", 60);
					lp.height = Math.round(lp.height / 60.0f * pct);
					param.setResult(lp);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void AutoGroupNotificationsHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.classLoader, "adjustAutogroupingSummary", int.class, String.class, String.class, boolean.class, new XC_MethodHook() {
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

			XposedHelpers.findAndHookMethod("com.android.server.notification.GroupHelper", lpparam.classLoader, "adjustNotificationBundling", List.class, boolean.class, new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void NoMoreIconHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "setIconsVisibility", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					Object mMoreIcon = XposedHelpers.getObjectField(param.thisObject, "mMoreIcon");
					if (mMoreIcon != null) XposedHelpers.setBooleanField(param.thisObject, "mForceHideMoreIcon", true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void ShowNotificationsAfterUnlockHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.miui.statusbar.ExpandedNotification", lpparam.classLoader, "setHasShownAfterUnlock", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					param.args[0] = false;
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static int appInfoIconResId;
	private static int forceCloseIconResId;
	public static void NotificationRowMenuRes(XC_InitPackageResources.InitPackageResourcesParam resparam) {
		try {
			XModuleResources modRes = XModuleResources.createInstance(MainModule.MODULE_PATH, resparam.res);
			appInfoIconResId = resparam.res.addResource(modRes, R.drawable.ic_appinfo);
			forceCloseIconResId = resparam.res.addResource(modRes, R.drawable.ic_forceclose);
			resparam.res.setReplacement(resparam.packageName, "dimen", "notification_menu_icon_size", modRes.fwd(R.dimen.notification_menu_icon_size));
			resparam.res.setReplacement(resparam.packageName, "dimen", "notification_menu_icon_padding", modRes.fwd(R.dimen.notification_menu_icon_padding));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void NotificationRowMenuHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "createMenuViews", boolean.class, new XC_MethodHook() {
				@Override
				@SuppressWarnings("unchecked")
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					try {
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						ArrayList<Object> mMenuItems = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mMenuItems");
						Class<?> nmiCls = findClass("com.android.systemui.statusbar.NotificationMenuRow$NotificationMenuItem", lpparam.classLoader);
						Constructor<?> nmiCtr = nmiCls.getConstructor(Context.class, String.class, findClass("com.android.systemui.statusbar.NotificationGuts.GutsContent", lpparam.classLoader), int.class);
						Object infoBtn = nmiCtr.newInstance(mContext, "Application info",
								LayoutInflater.from(mContext).inflate(mContext.getResources().getIdentifier("notification_info", "layout", lpparam.packageName), null, false),
								appInfoIconResId);
						Object forceCloseBtn = nmiCtr.newInstance(mContext, "Force close",
								LayoutInflater.from(mContext).inflate(mContext.getResources().getIdentifier("notification_info", "layout", lpparam.packageName), null, false),
								forceCloseIconResId);
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

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "onHeightUpdate", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					FrameLayout mMenuContainer = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mMenuContainer");
					if (mMenuContainer != null) mMenuContainer.setTranslationY(0);
				}
			});

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NotificationMenuRow", lpparam.classLoader, "setMenuLocation", new XC_MethodHook() {
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
						int height = (int)XposedHelpers.callMethod(mParent, "getHeight");
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

			XposedBridge.hookAllMethods(findClass("com.android.systemui.statusbar.stack.NotificationStackScrollLayout", lpparam.classLoader), "onMenuClicked", new XC_MethodHook() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void SelectiveVibrationHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.VibratorService", lpparam.classLoader, "systemReady", new XC_MethodHook() {
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

			XposedBridge.hookAllMethods(findClass("com.android.server.VibratorService", lpparam.classLoader), "vibrate", new XC_MethodHook() {
				@Override
				@SuppressWarnings("unchecked")
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					String pkgName = (String)param.args[1];
					if (pkgName == null) return;

					try {
						int opt = (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mVibrationMode");
						Set<String> selectedApps = (Set<String>)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mVibrationApps");
						boolean isSelected = selectedApps != null && selectedApps.contains(pkgName);
						if (opt == 2 && !isSelected || opt == 3 && isSelected) param.setResult(null);
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void QSGridRes(XC_InitPackageResources.InitPackageResourcesParam resparam) {
		try {
			resparam.res.setReplacement(resparam.packageName, "integer", "quick_settings_num_columns", 8);
			resparam.res.setReplacement(resparam.packageName, "integer", "quick_settings_num_rows", 2);
			resparam.res.setReplacement(resparam.packageName, "integer", "quick_settings_max_rows", 2);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

}