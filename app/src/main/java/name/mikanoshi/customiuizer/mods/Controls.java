package name.mikanoshi.customiuizer.mods;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

import static java.lang.System.currentTimeMillis;

public class Controls {

	private static boolean isPowerPressed = false;
	private static boolean isPowerLongPressed = false;
	private static boolean isVolumePressed = false;
	private static boolean isVolumeLongPressed = false;
	private static boolean isWaitingForPowerLongPressed = false;
	private static boolean isWaitingForVolumeLongPressed = false;
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

	public static void PowerKeyRes() {
		MainModule.resHooks.setObjectReplacement("android", "bool", "config_supportLongPressPowerWhenNonInteractive", true);
	}

	public static void PowerKeyHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "init", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mContext.registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
			}
		});

//		Helpers.findAndHookMethod("com.android.server.input.InputManagerService", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//				KeyEvent keyEvent = (KeyEvent)param.args[0];
//				Helpers.log("PowerKeyHook", "InputManagerService interceptKeyBeforeQueueing: | KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags());
//			}
//		});
//
//		Helpers.findAndHookMethod("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueingInternal", KeyEvent.class, int.class, boolean.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//				KeyEvent keyEvent = (KeyEvent)param.args[0];
//				Helpers.log("PowerKeyHook", "interceptKeyBeforeQueueingInternal: " + param.args[2] + " | KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags());
//			}
//		});

		Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isVolumePressed) return;
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
						//Helpers.log("PowerKeyHook", "interceptKeyBeforeQueueing: " + param.args[1] + ", isTracking: " + keyEvent.isTracking() + " | Source: " + keyEvent.getSource() + " | KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags());
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
	}

	public static void VolumeMediaButtonsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new XC_MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isPowerPressed) return;

				final KeyEvent keyEvent = (KeyEvent)param.args[0];

				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();

				// Ignore repeated KeyEvents simulated on volume Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) == KeyEvent.FLAG_FROM_SYSTEM && (keycode == KeyEvent.KEYCODE_VOLUME_UP || keycode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
					// Volume long press
					final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					final boolean isScreenOn = (boolean)XposedHelpers.callMethod(param.thisObject, "isScreenOn");
					if (!isScreenOn) {
						//Helpers.log("VolumeMediaButtonsHook", "interceptKeyBeforeQueueing: KeyCode: " + String.valueOf(keyEvent.getKeyCode()) + " | Action: " + String.valueOf(keyEvent.getAction()) + " | RepeatCount: " + String.valueOf(keyEvent.getRepeatCount())+ " | Flags: " + String.valueOf(keyEvent.getFlags()));
						if (action == KeyEvent.ACTION_DOWN) {
							isVolumePressed = true;
							isVolumeLongPressed = false;

							mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

							// Post only one delayed runnable that waits for long press timeout
							if (mHandler != null && !isWaitingForVolumeLongPressed)
							mHandler.postDelayed(new Runnable() {
								public void run() {
									if (isVolumePressed && GlobalActions.isMediaActionsAllowed(mContext)) {
										isVolumeLongPressed = true;
										switch (keyEvent.getKeyCode()) {
											case KeyEvent.KEYCODE_VOLUME_UP:
												int pref_mediaUp = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_controls_volumemedia_up", "0"));
												if (pref_mediaUp == 0) break;
												GlobalActions.sendDownUpKeyEvent(mContext, pref_mediaUp, true);
												break;
											case KeyEvent.KEYCODE_VOLUME_DOWN:
												int pref_mediaDown = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_controls_volumemedia_down", "0"));
												if (pref_mediaDown == 0) break;
												GlobalActions.sendDownUpKeyEvent(mContext, pref_mediaDown, true);
												break;
											default:
												break;
										}
									}
									isVolumePressed = false;
									isWaitingForVolumeLongPressed = false;
								}
							}, ViewConfiguration.getLongPressTimeout());

							isWaitingForVolumeLongPressed = true;
							param.setResult(0);
						}
						if (action == KeyEvent.ACTION_UP) {
							isVolumePressed = false;
							// Kill all callbacks (removing only posted Runnable is not working... no idea)
							if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
							if (!isVolumeLongPressed) {
								AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
								TelecomManager tm = (TelecomManager)mContext.getSystemService(Context.TELECOM_SERVICE);
								WakeLock mBroadcastWakeLock = (WakeLock)XposedHelpers.getObjectField(param.thisObject, "mBroadcastWakeLock");
								int k = AudioManager.ADJUST_RAISE;
								if (keycode != KeyEvent.KEYCODE_VOLUME_UP) k = AudioManager.ADJUST_LOWER;
								mBroadcastWakeLock.acquire(5000);
								// If music stream is playing, adjust its volume
								if (am.isMusicActive()) am.adjustStreamVolume(AudioManager.STREAM_MUSIC, k, 0);
								// If voice call is active while screen off by proximity sensor, adjust its volume
								else if (tm.isInCall()) am.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, k, 0);
								// If volume keys to wake option active, wake the device
								//else if (XposedHelpers.getBooleanField(param.thisObject, "mVolumeKeyWakeScreen"))
								//XposedHelpers.callMethod(mPowerManager, "wakeUpFromPowerKey", SystemClock.uptimeMillis());
								if (mBroadcastWakeLock.isHeld()) mBroadcastWakeLock.release();
							}
							param.setResult(0);
							isWaitingForVolumeLongPressed = false;
						}
					}
				}
			}
		});
	}

	public static void VolumeMediaPlayerHook() {
		Helpers.findAndHookMethod("android.media.MediaPlayer", null, "pause", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				try {
					Application mContext = (Application)XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentApplication");
					int mStreamType = (int)XposedHelpers.findMethodExact(XposedHelpers.findClass("android.media.MediaPlayer", null), "getAudioStreamType").invoke(param.thisObject);
					if (mContext != null && (mStreamType == AudioManager.STREAM_MUSIC || mStreamType == 0x80000000))
					mContext.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.SaveLastMusicPausedTime"));
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void VolumeCursorHook() {
		Helpers.findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				InputMethodService ims = (InputMethodService)param.thisObject;
				int code = (int)param.args[0];
				if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
					boolean swapDir = MainModule.mPrefs.getBoolean("controls_volumecursor_reverse");
					ims.sendDownUpKeyEvents(code == (swapDir ? KeyEvent.KEYCODE_VOLUME_DOWN : KeyEvent.KEYCODE_VOLUME_UP) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
					param.setResult(true);
				}
			}
		});

		Helpers.findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyUp", int.class, KeyEvent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				InputMethodService ims = (InputMethodService)param.thisObject;
				int code = (int)param.args[0];
				if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown())
				param.setResult(true);
			}
		});
	}

	private static boolean handleNavBarAction(Context context, String key) {
		int action = Helpers.getSharedIntPref(context, key + "_action", 1);
		if (action >= 85 && action <= 88) {
			if (GlobalActions.isMediaActionsAllowed(context))
			GlobalActions.sendDownUpKeyEvent(context, action, false);
			return true;
		} else if (action == 1) {
			try {
				Toast.makeText(Helpers.getModuleContext(context), R.string.controls_navbar_noaction, Toast.LENGTH_SHORT).show();
			} catch (Throwable t) {}
			return false;
		} else {
			return GlobalActions.handleAction(context, key);
		}
	}

	private static void addCustomNavBarKeys(boolean isVertical, Context mContext, LinearLayout navButtons, Class<?> kbrCls) {
		String pkgName = "com.android.systemui";
		float density = mContext.getResources().getDisplayMetrics().density;
		int two = Math.round(2 * density);

		Drawable dot;
		try {
			Context modCtx = Helpers.getModuleContext(mContext);
			Resources modRes = Helpers.getModuleRes(mContext);
			dot = modRes.getDrawable(R.drawable.ic_sysbar_dot, modCtx.getTheme());
		} catch (Throwable t) {
			XposedBridge.log(t);
			return;
		}

		int diff = (dot.getIntrinsicWidth() - dot.getIntrinsicHeight()) / 2;
		LinearLayout leftbtn = new LinearLayout(mContext);
		ImageView left = new ImageView(mContext);
		LinearLayout.LayoutParams lpl;
		if (isVertical)
			lpl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		else
			lpl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		leftbtn.setLayoutParams(lpl);
		left.setLayoutParams(lpl);
		left.setScaleType(ImageView.ScaleType.CENTER);
		left.setImageDrawable(dot);
		left.setScaleX(0.7f);
		left.setScaleY(0.7f);
		left.setAlpha(0.9f);
		left.setPadding(isVertical ? 0 : two, isVertical ? two + diff : 0, isVertical ? 0 : two, isVertical ? two + diff : 0);
		left.setTag("custom_left" + (isVertical ? "_vert" : "_horiz"));
		if (kbrCls != null) try {
			Drawable lripple = (Drawable)kbrCls.getConstructor(Context.class, View.class).newInstance(mContext, leftbtn);
			leftbtn.setBackground(lripple);
		} catch (Throwable t) {}
		leftbtn.setClickable(true);
		leftbtn.setHapticFeedbackEnabled(true);
		leftbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleNavBarAction(v.getContext(), "pref_key_controls_navbarleft");
			}
		});
		leftbtn.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return handleNavBarAction(v.getContext(), "pref_key_controls_navbarleftlong");
			}
		});
		leftbtn.addView(left);

		LinearLayout rightbtn = new LinearLayout(mContext);
		ImageView right = new ImageView(mContext);
		LinearLayout.LayoutParams lpr;
		if (isVertical)
			lpr = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		else
			lpr = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		rightbtn.setLayoutParams(lpr);
		right.setLayoutParams(lpr);
		right.setScaleType(ImageView.ScaleType.CENTER);
		right.setImageDrawable(dot);
		right.setScaleX(0.7f);
		right.setScaleY(0.7f);
		right.setAlpha(0.9f);
		right.setPadding(isVertical ? 0 : two, isVertical ? two + diff : 0, isVertical ? 0 : two, isVertical ? two + diff : 0);
		right.setTag("custom_right" + (isVertical ? "_vert" : "_horiz"));
		if (kbrCls != null) try {
			Drawable rripple = (Drawable)kbrCls.getConstructor(Context.class, View.class).newInstance(mContext, rightbtn);
			rightbtn.setBackground(rripple);
		} catch (Throwable t) {}
		rightbtn.setClickable(true);
		rightbtn.setHapticFeedbackEnabled(true);
		rightbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleNavBarAction(v.getContext(), "pref_key_controls_navbarright");
			}
		});
		rightbtn.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return handleNavBarAction(v.getContext(), "pref_key_controls_navbarrightlong");
			}
		});
		rightbtn.addView(right);

		View startPadding = navButtons.findViewById(navButtons.getResources().getIdentifier("start_padding", "id", pkgName));
		View sidePadding = navButtons.findViewById(navButtons.getResources().getIdentifier("side_padding", "id", pkgName));

		LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams)startPadding.getLayoutParams();
		LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)sidePadding.getLayoutParams();

		boolean hasLeftAction = MainModule.mPrefs.getInt("controls_navbarleft_action", 1) > 1 || MainModule.mPrefs.getInt("controls_navbarleftlong_action", 1) > 1;
		boolean hasRightAction = MainModule.mPrefs.getInt("controls_navbarright_action", 1) > 1 || MainModule.mPrefs.getInt("controls_navbarrightlong_action", 1) > 1;

		if (isVertical) {
			if (hasRightAction) {
				navButtons.addView(rightbtn, 0);
				lp2.weight = Math.round(lp2.weight * 0.5f);
				sidePadding.setLayoutParams(lp2);
			}
			if (hasLeftAction) {
				navButtons.addView(leftbtn, navButtons.getChildCount());
				lp1.weight = Math.round(lp1.weight * 0.5f);
				startPadding.setLayoutParams(lp1);
			}
		} else {
			if (hasLeftAction) {
				navButtons.addView(leftbtn, 0);
				lp1.weight = Math.round(lp1.weight * 0.5f);
				startPadding.setLayoutParams(lp1);
			}
			if (hasRightAction) {
				navButtons.addView(rightbtn, navButtons.getChildCount());
				lp2.weight = Math.round(lp2.weight * 0.5f);
				sidePadding.setLayoutParams(lp2);
			}
		}
	}

	public static void NavBarButtonsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "createNavigationBar", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Class<?> kbrCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.policy.KeyButtonRipple", lpparam.classLoader);
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (mContext == null) {
					Helpers.log("NavBarButtonsHook", "Cannot find context");
					return;
				}
				LinearLayout mNavigationBarView = (LinearLayout)XposedHelpers.getObjectField(param.thisObject, "mNavigationBarView");
				if (mNavigationBarView == null) {
					Helpers.log("NavBarButtonsHook", "Cannot find navbar layout");
					return;
				}
				ViewGroup rot0 = mNavigationBarView.findViewById(mNavigationBarView.getResources().getIdentifier("rot0", "id", lpparam.packageName));
				ViewGroup rot90 = mNavigationBarView.findViewById(mNavigationBarView.getResources().getIdentifier("rot90", "id", lpparam.packageName));
				LinearLayout navButtons0 = rot0.findViewById(mNavigationBarView.getResources().getIdentifier("nav_buttons", "id", lpparam.packageName));
				LinearLayout navButtons90 = rot90.findViewById(mNavigationBarView.getResources().getIdentifier("nav_buttons", "id", lpparam.packageName));

				addCustomNavBarKeys(false, mContext, navButtons0, kbrCls);
				addCustomNavBarKeys(true, mContext, navButtons90, kbrCls);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "switchSuit", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				LinearLayout navbar = (LinearLayout)param.thisObject;
				boolean isDark = (boolean)param.args[1];
				ImageView hleft = navbar.findViewWithTag("custom_left_horiz");
				ImageView vleft = navbar.findViewWithTag("custom_left_vert");
				ImageView hright = navbar.findViewWithTag("custom_right_horiz");
				ImageView vright = navbar.findViewWithTag("custom_right_vert");

				Context modCtx = Helpers.getModuleContext(navbar.getContext());
				Resources modRes = Helpers.getModuleRes(navbar.getContext());
				if (isDark) {
					Drawable darkImg = modRes.getDrawable(R.drawable.ic_sysbar_dot_dark, modCtx.getTheme());
					if (hleft != null) hleft.setImageDrawable(darkImg);
					if (vleft != null) vleft.setImageDrawable(darkImg);
					if (hright != null) hright.setImageDrawable(darkImg);
					if (vright != null) vright.setImageDrawable(darkImg);
				} else {
					Drawable lightImg = modRes.getDrawable(R.drawable.ic_sysbar_dot, modCtx.getTheme());
					if (hleft != null) hleft.setImageDrawable(lightImg);
					if (vleft != null) vleft.setImageDrawable(lightImg);
					if (hright != null) hright.setImageDrawable(lightImg);
					if (vright != null) vright.setImageDrawable(lightImg);
				}
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private static Context miuiPWMContext;
	private static Handler miuiPWMHandler;
	private static boolean wasScreenOn = false;
	private static boolean wasFingerprintUsed = false;
	private static boolean isFingerprintPressed = false;
	private static boolean isFingerprintLongPressed = false;
	private static Runnable singlePressFingerprint = new Runnable() {
		@Override
		public void run() {
			if (miuiPWMContext == null || miuiPWMHandler == null) return;
			miuiPWMHandler.removeCallbacks(longPressFingerprint);
			GlobalActions.handleAction(miuiPWMContext, "pref_key_controls_fingerprint1");
		}
	};
	private static Runnable longPressFingerprint = new Runnable() {
		@Override
		public void run() {
			if (isFingerprintPressed && miuiPWMContext != null) {
				isFingerprintLongPressed = true;
				Helpers.performStrongVibration(miuiPWMContext, true);
			}
		}
	};

	public static void FingerprintEventsHook(LoadPackageParam lpparam) {
		boolean skipInCamera = MainModule.mPrefs.getBoolean("controls_fingerprintskip");
		boolean skipInCall = MainModule.mPrefs.getBoolean("controls_fingerprintskip2");

		Helpers.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.classLoader, "processFingerprintNavigationEvent", KeyEvent.class, boolean.class, new XC_MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (skipInCamera) {
					Object mFocusedWindow = XposedHelpers.getObjectField(param.thisObject, "mFocusedWindow");
					if ((boolean)param.args[1] && mFocusedWindow != null) {
						String ownPkg = (String)XposedHelpers.callMethod(mFocusedWindow, "getOwningPackage");
						if ("com.android.camera".equals(ownPkg)) return;
					}
				}

				miuiPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				miuiPWMHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

				if (skipInCall && miuiPWMContext != null) {
					TelecomManager tm = (TelecomManager)miuiPWMContext.getSystemService(Context.TELECOM_SERVICE);
					if (tm.isInCall()) return;
				}

				KeyEvent keyEvent = (KeyEvent)param.args[0];
				if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.getAction() != KeyEvent.ACTION_DOWN) return;

				isFingerprintPressed = true;
				wasScreenOn = (boolean)XposedHelpers.callMethod(param.thisObject, "isScreenOn");
				wasFingerprintUsed = Settings.System.getInt(miuiPWMContext.getContentResolver(), "is_fingerprint_active", 0) == 1;

				if (wasScreenOn && !wasFingerprintUsed)
				if (Helpers.getSharedIntPref(miuiPWMContext, "pref_key_controls_fingerprintlong_action", 1) > 1) {
					int delay = Helpers.getSharedIntPref(miuiPWMContext, "pref_key_controls_fingerprintlong_delay", 0);
					miuiPWMHandler.postDelayed(longPressFingerprint, delay < 200 ? ViewConfiguration.getLongPressTimeout() : delay);
				}

				if (XposedHelpers.getAdditionalInstanceField(param.thisObject, "touchTime") == null)
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "touchTime", 0L);
			}

			@Override
			@SuppressLint("MissingPermission")
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (skipInCamera) {
					Object mFocusedWindow = XposedHelpers.getObjectField(param.thisObject, "mFocusedWindow");
					if ((boolean)param.args[1] && mFocusedWindow != null) {
						String ownPkg = (String)XposedHelpers.callMethod(mFocusedWindow, "getOwningPackage");
						if ("com.android.camera".equals(ownPkg)) return;
					}
				}

				if (skipInCall && miuiPWMContext != null) {
					TelecomManager tm = (TelecomManager)miuiPWMContext.getSystemService(Context.TELECOM_SERVICE);
					if (tm.isInCall()) return;
				}

				KeyEvent keyEvent = (KeyEvent)param.args[0];
				if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.getAction() != KeyEvent.ACTION_UP) return;

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

				long lastTouchTime = (long)XposedHelpers.getAdditionalInstanceField(param.thisObject, "touchTime");
				long currentTouchTime = currentTimeMillis();
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "touchTime", currentTouchTime);

				int delay = Helpers.getSharedIntPref(mContext, "pref_key_controls_fingerprint2_delay", 50);
				int dttimeout = delay < 200 ? ViewConfiguration.getDoubleTapTimeout() : delay;
				int dtaction = Helpers.getSharedIntPref(mContext, "pref_key_controls_fingerprint2_action", 1);
				if (wasScreenOn && !wasFingerprintUsed)
				if (dtaction > 1 && currentTouchTime - lastTouchTime < dttimeout) {
					mHandler.removeCallbacks(singlePressFingerprint);
					mHandler.removeCallbacks(longPressFingerprint);
					GlobalActions.handleAction(mContext, "pref_key_controls_fingerprint2");
					wasScreenOn = false;
				} else if (isFingerprintLongPressed) {
					GlobalActions.handleAction(mContext, "pref_key_controls_fingerprintlong");
					wasScreenOn = false;
				} else {
					mHandler.removeCallbacks(longPressFingerprint);
					mHandler.removeCallbacks(singlePressFingerprint);
					if (dtaction > 1)
						mHandler.postDelayed(singlePressFingerprint, dttimeout);
					else
						mHandler.post(singlePressFingerprint);
				}

				isFingerprintLongPressed = false;
				isFingerprintPressed = false;
			}
		});

		Helpers.hookAllMethods("com.android.server.fingerprint.FingerprintService", lpparam.classLoader, "startClient", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Settings.System.putInt(mContext.getContentResolver(), "is_fingerprint_active", 1);
			}
		});

		Helpers.hookAllMethods("com.android.server.fingerprint.FingerprintService", lpparam.classLoader, "removeClient", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Settings.System.putInt(mContext.getContentResolver(), "is_fingerprint_active", 0);
			}
		});
	}

//	public static void FingerprintHook(XC_LoadPackage.LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.server.devicepolicy.DevicePolicyManagerService", lpparam.classLoader, "reportSuccessfulFingerprintAttempt", int.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//				Helpers.log("reportSuccessfulFingerprintAttempt");
//				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//			}
//		});
//	}

	public static void FSGesturesHook() {
		Helpers.findAndHookMethod("android.provider.MiuiSettings$Global", null, "getBoolean", ContentResolver.class, String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					if (!"force_fsg_nav_bar".equals(param.args[1])) return;

					ContentResolver resolver = (ContentResolver)param.args[0];
					Context mContext = (Context)XposedHelpers.getObjectField(resolver, "mContext");

					if ("com.android.systemui".equals(mContext.getPackageName()))
					for (StackTraceElement el: Thread.currentThread().getStackTrace())
					if ("com.android.systemui.recents.BaseRecentsImpl".equals(el.getClassName())) {
						param.setResult(true);
						return;
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private static Context basePWMContext;
	private static Object basePWMObject;
	private static Method markShortcutTriggered;

	private static Runnable mBackLongPressAction = new Runnable() {
		@Override
		public void run() {
			try {
				if (basePWMContext == null || basePWMObject == null) return;
				if (GlobalActions.handleAction(basePWMContext, "pref_key_controls_backlong")) Helpers.performStrongVibration(basePWMContext);
				if (Helpers.getSharedIntPref(basePWMContext, "pref_key_controls_backlong_action", 1) != 1) markShortcutTriggered.invoke(basePWMObject);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	private static Runnable mHomeLongPressAction = new Runnable() {
		@Override
		public void run() {
			try {
				if (basePWMContext == null || basePWMObject == null) return;
				if (GlobalActions.handleAction(basePWMContext, "pref_key_controls_homelong")) Helpers.performStrongVibration(basePWMContext);
				if (Helpers.getSharedIntPref(basePWMContext, "pref_key_controls_homelong_action", 1) != 1) markShortcutTriggered.invoke(basePWMObject);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
	private static Runnable mMenuLongPressAction = new Runnable() {
		@Override
		public void run() {
			try {
				if (basePWMContext == null || basePWMObject == null) return;
				if (GlobalActions.handleAction(basePWMContext, "pref_key_controls_menulong")) Helpers.performStrongVibration(basePWMContext);
				if (Helpers.getSharedIntPref(basePWMContext, "pref_key_controls_menulong_action", 1) != 1) markShortcutTriggered.invoke(basePWMObject);
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};

	public static void NavBarActionsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "initInternal", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

				new Helpers.SharedPrefObserver(mContext, mHandler) {
					@Override
					public void onChange(Uri uri) {
						try {
							String type = uri.getPathSegments().get(1);
							if (!type.equals("integer")) return;
							String key = uri.getPathSegments().get(2);
							if (key.contains("pref_key_controls_backlong") || key.contains("pref_key_controls_homelong") || key.contains("pref_key_controls_menulong")) {
								if (key.contains("_action"))
									MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 1));
								else if (key.contains("_toggle"))
									MainModule.mPrefs.put(key, Helpers.getSharedIntPref(mContext, key, 0));
							}
						} catch (Throwable t) {
							XposedBridge.log(t);
						}
					}
				};
			}
		});

		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "postKeyLongPress", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if (basePWMObject == null) basePWMObject = param.thisObject;
				if (basePWMContext == null) basePWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (markShortcutTriggered == null) markShortcutTriggered = XposedHelpers.findMethodExact("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "markShortcutTriggered");

				int key = (int)param.args[0];
				if (key == KeyEvent.KEYCODE_BACK && MainModule.mPrefs.getInt("controls_backlong_action", 1) > 1) {
					((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).postDelayed(mBackLongPressAction, ViewConfiguration.getLongPressTimeout());
					param.setResult(null);
				} else if (key == KeyEvent.KEYCODE_HOME && MainModule.mPrefs.getInt("controls_homelong_action", 1) > 1) {
					((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).postDelayed(mHomeLongPressAction, ViewConfiguration.getLongPressTimeout());
					param.setResult(null);
				} else if (key == KeyEvent.KEYCODE_APP_SWITCH && MainModule.mPrefs.getInt("controls_menulong_action", 1) > 1) {
					((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).postDelayed(mMenuLongPressAction, ViewConfiguration.getLongPressTimeout());
					param.setResult(null);
				}
			}
		});

		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "removeKeyLongPress", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				try {
					int key = (int)param.args[0];
					if (key == KeyEvent.KEYCODE_BACK)
						((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).removeCallbacks(mBackLongPressAction);
					else if (key == KeyEvent.KEYCODE_HOME)
						((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).removeCallbacks(mHomeLongPressAction);
					else if (key == KeyEvent.KEYCODE_APP_SWITCH)
						((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).removeCallbacks(mMenuLongPressAction);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void NavbarHeightRes() {
		int opt = MainModule.mPrefs.getInt("controls_navbarheight", 26);
		String heightRes = "navigation_bar_height_" + (opt == 26 ? 47 : opt);
		MainModule.resHooks.setResReplacement("android", "dimen", "navigation_bar_height", heightRes);
		MainModule.resHooks.setResReplacement("android", "dimen", "navigation_bar_height_landscape", heightRes);
		MainModule.resHooks.setResReplacement("android", "dimen", "navigation_bar_width", heightRes);
	}

	public static void FingerprintHapticSuccessHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.fingerprint.AuthenticationClient", lpparam.classLoader, "onAuthenticated", int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				try {
					if (!((boolean)param.getResult())) return;
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//					boolean isScreenOn = (boolean)XposedHelpers.callMethod(param.thisObject, "isScreenOn");
//					if (!isScreenOn) {
//						WakeLock wl = ((PowerManager)mContext.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "miuizer:fingerprint_haptic_feedback");
//						wl.acquire(2000);
//					}

					boolean ignoreSystem = MainModule.mPrefs.getBoolean("controls_fingerprintsuccess_ignore");
					int opt = Integer.parseInt(MainModule.mPrefs.getString("controls_fingerprintsuccess", "1"));
					if (opt == 2)
						Helpers.performLightVibration(mContext, ignoreSystem);
					else if (opt == 3)
						Helpers.performStrongVibration(mContext, ignoreSystem);
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}
		});
	}

	public static void FingerprintHapticFailureHook(LoadPackageParam lpparam) {
		if (Helpers.isNougat())
			Helpers.findAndHookMethod("com.android.server.fingerprint.FingerprintUtils", lpparam.classLoader, "vibrateFingerprintError", Context.class, XC_MethodReplacement.returnConstant(null));
		else
			Helpers.findAndHookMethod("com.android.server.fingerprint.ClientMonitor", lpparam.classLoader, "vibrateError", XC_MethodReplacement.returnConstant(null));
	}

//	public static void AIButtonHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "startAiKeyService", String.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//				Helpers.log("AIButtonHook", "startAiKeyService: " + param.args[0]);
//			}
//		});
//	}

//	public static void SwapVolumeKeysHook(XC_LoadPackage.LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, String.class, int.class, new XC_MethodHook() {
//			@Override
//			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//				//Helpers.log("adjustStreamVolume: " + String.valueOf(param.args[0]) + ", " + String.valueOf(param.args[1]) + ", " + String.valueOf(param.args[2]) + ", " + String.valueOf(param.args[3]) + ", " + String.valueOf(param.args[4]) + ", " + String.valueOf(param.args[5]));
//				if ((Integer)param.args[0] != 0) try {
//					Context context = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//					int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
//					if (rotation == Surface.ROTATION_90) param.args[0] = -1 * (Integer)param.args[0];
//				} catch (Throwable t) {
//					XposedBridge.log(t);
//				}
//			}
//		});
//	}

}