package name.mikanoshi.customiuizer.mods;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Controls {

	private static boolean isPowerPressed = false;
	private static boolean isPowerLongPressed = false;
	private static boolean isWaitingForPowerLongPressed = false;
	private static Handler mHandler;

	private static boolean isTorchEnabled(Context mContext) {
		return Settings.Global.getInt(mContext.getContentResolver(), "torch_state", 0) != 0;
	}

	private static void setTorch(Context mContext, boolean state) {
		Intent intent = new Intent("miui.intent.action.TOGGLE_TORCH");
		intent.putExtra("miui.intent.extra.IS_ENABLE", state);
		mContext.sendBroadcast(intent);
	}

	private static BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			if (isTorchEnabled(context)) setTorch(context, false);
			if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
		}
	};

	public static void PowerKeyHook(XC_LoadPackage.LoadPackageParam lpparam) {

		try {

		XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "init", Context.class, "android.view.IWindowManager", "com.android.server.policy.WindowManagerPolicy.WindowManagerFuncs", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mContext.registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
			}
		});

		XposedHelpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				// Power and volkeys are pressed at the same time
//				if (isVolumePressed) return;

				KeyEvent keyEvent = (KeyEvent)param.args[0];

				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();

				// Ignore repeated KeyEvents simulated on Power Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM && keycode == KeyEvent.KEYCODE_POWER) {
					// Power long press
					final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
					final boolean isScreenOn = (boolean)XposedHelpers.callMethod(param.thisObject, "isScreenOn");
					if (!isScreenOn) {
						//XposedBridge.log("interceptKeyBeforeQueueing: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
						if (action == KeyEvent.ACTION_DOWN) {
							isPowerPressed = true;
							isPowerLongPressed = false;

							mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

							int longPressDelay = (Helpers.getSharedBoolPref(mContext, "pref_key_controls_powerflash_delay", false) ? ViewConfiguration.getLongPressTimeout() * 3 : ViewConfiguration.getLongPressTimeout()) + 500;
							// Post only one delayed runnable that waits for long press timeout
							if (!isWaitingForPowerLongPressed)
							mHandler.postDelayed(new Runnable() {
								@Override
								@SuppressLint("Wakelock")
								public void run() {
									if (isPowerPressed) {
										isPowerLongPressed = true;

										if (Helpers.mWakeLock == null)
											Helpers.mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "miuizer:flashlight");

										if (!isTorchEnabled(mContext) || !Helpers.mWakeLock.isHeld()) {
											setTorch(mContext, true);
											if (!Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.acquire(600000);
										} else {
											setTorch(mContext, true);
											if (Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
										}
									}
									isPowerPressed = false;
									isWaitingForPowerLongPressed = false;
								}
							}, longPressDelay);
							isWaitingForPowerLongPressed = true;
							param.setResult(0);
						}
						if (action == KeyEvent.ACTION_UP) {
							if (isPowerPressed && !isPowerLongPressed) try {
								if (isTorchEnabled(mContext)) setTorch(mContext, false);
								if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
								XposedHelpers.callMethod(mPowerManager, "wakeUp", SystemClock.uptimeMillis());
								param.setResult(0);
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
							isPowerPressed = false;
							isWaitingForPowerLongPressed = false;
						}
					}
				}
			}
		});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

//	public static void FingerprintHook(XC_LoadPackage.LoadPackageParam lpparam) {
//		try {
//			XposedHelpers.findAndHookMethod("com.android.server.devicepolicy.DevicePolicyManagerService", lpparam.classLoader, "reportSuccessfulFingerprintAttempt", int.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//					XposedBridge.log("reportSuccessfulFingerprintAttempt");
//					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//				}
//			});
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

//	public static void SwapVolumeKeysHook(XC_LoadPackage.LoadPackageParam lpparam) {
//		try {
//			XposedHelpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, String.class, int.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//					//XposedBridge.log("adjustStreamVolume: " + String.valueOf(param.args[0]) + ", " + String.valueOf(param.args[1]) + ", " + String.valueOf(param.args[2]) + ", " + String.valueOf(param.args[3]) + ", " + String.valueOf(param.args[4]) + ", " + String.valueOf(param.args[5]));
//					if ((Integer)param.args[0] != 0) try {
//						Context context = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//						int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
//						if (rotation == Surface.ROTATION_90) param.args[0] = -1 * (Integer)param.args[0];
//					} catch (Throwable t) {
//						XposedBridge.log(t);
//					}
//				}
//			});
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

}