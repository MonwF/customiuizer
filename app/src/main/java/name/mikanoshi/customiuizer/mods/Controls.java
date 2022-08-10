package name.mikanoshi.customiuizer.mods;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Method;

import static java.lang.System.currentTimeMillis;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

public class Controls {

	private static boolean isPowerPressed = false;
	private static boolean isPowerLongPressed = false;
	private static boolean isVolumePressed = false;
	private static boolean isVolumeLongPressed = false;
	private static boolean isWaitingForPowerLongPressed = false;
	private static boolean isWaitingForVolumeLongPressed = false;
	private static boolean wasRaise2WakeEnabled = false;
	private static Handler mHandler;

	private static boolean isTorchEnabled(Context mContext) {
		return Settings.Global.getInt(mContext.getContentResolver(), "torch_state", 0) != 0;
	}

	private static void setTorch(Context context, boolean state) {
		if (state) {
			int wakeup = Settings.System.getInt(context.getContentResolver(), "pick_up_gesture_wakeup_mode", 0);
			wasRaise2WakeEnabled = wakeup == 1;
			if (wasRaise2WakeEnabled) Settings.System.putInt(context.getContentResolver(), "pick_up_gesture_wakeup_mode", 0);
		}
		Intent intent = new Intent("miui.intent.action.TOGGLE_TORCH");
		intent.putExtra("miui.intent.extra.IS_ENABLE", state);
		context.sendBroadcast(intent);
	}

	private static final BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
		public void onReceive(final Context context, Intent intent) {
			if (isTorchEnabled(context)) setTorch(context, false);
			if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
			if (wasRaise2WakeEnabled) {
				wasRaise2WakeEnabled = false;
				Settings.System.putInt(context.getContentResolver(), "pick_up_gesture_wakeup_mode", 1);
			}
		}
	};

	public static void PowerKeyRes() {
		MainModule.resHooks.setObjectReplacement("android", "bool", "config_supportLongPressPowerWhenNonInteractive", true);
	}

	public static void PowerKeyHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "init", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				mContext.registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
			}
		});

//		Helpers.findAndHookMethod("com.android.server.input.InputManagerService", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				KeyEvent keyEvent = (KeyEvent)param.args[0];
//				Helpers.log("PowerKeyHook", "InputManagerService interceptKeyBeforeQueueing: | KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags());
//			}
//		});
//
//		Helpers.findAndHookMethod("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueingInternal", KeyEvent.class, int.class, boolean.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				KeyEvent keyEvent = (KeyEvent)param.args[0];
//				Helpers.log("PowerKeyHook", "interceptKeyBeforeQueueingInternal: " + param.args[2] + " | KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags());
//			}
//		});

		Helpers.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isVolumePressed) return;
				KeyEvent keyEvent = (KeyEvent)param.args[0];

				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();

				// Ignore repeated KeyEvents simulated on Power Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) != KeyEvent.FLAG_FROM_SYSTEM || keycode != KeyEvent.KEYCODE_POWER) return;

				// Power long press
				final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
				if (mPowerManager.isInteractive()) return;
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
					} else if (wasRaise2WakeEnabled && !isTorchEnabled(mContext)) {
						wasRaise2WakeEnabled = false;
						Settings.System.putInt(mContext.getContentResolver(), "pick_up_gesture_wakeup_mode", 1);
					}
					isPowerPressed = false;
					isWaitingForPowerLongPressed = false;
				}
			}
		});
	}

	public static void VolumeMediaButtonsHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.classLoader, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
			protected void before(final MethodHookParam param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isPowerPressed) return;
				final KeyEvent keyEvent = (KeyEvent)param.args[0];

				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();

				// Ignore repeated KeyEvents simulated on volume Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) != KeyEvent.FLAG_FROM_SYSTEM || (keycode != KeyEvent.KEYCODE_VOLUME_UP && keycode != KeyEvent.KEYCODE_VOLUME_DOWN)) return;

				// Volume long press
				final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.thisObject, "mPowerManager");
				if (mPowerManager.isInteractive()) return;
				//Helpers.log("VolumeMediaButtonsHook", "interceptKeyBeforeQueueing: KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags() + " | " + mPowerManager.isInteractive());
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
						else if (Settings.System.getInt(mContext.getContentResolver(), "volumekey_wake_screen", 0) == 1)
						XposedHelpers.callMethod(mPowerManager, "wakeUp", SystemClock.uptimeMillis());
						if (mBroadcastWakeLock.isHeld()) mBroadcastWakeLock.release();
					}
					param.setResult(0);
					isWaitingForVolumeLongPressed = false;
				}
			}
		});
	}

	public static void VolumeMediaPlayerHook() {
		Helpers.findAndHookMethod("android.media.MediaPlayer", null, "pause", new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				Context mContext = Helpers.findContext();
				int mStreamType = (int)XposedHelpers.findMethodExact(XposedHelpers.findClass("android.media.MediaPlayer", null), "getAudioStreamType").invoke(param.thisObject);
				if (mContext != null && (mStreamType == AudioManager.STREAM_MUSIC || mStreamType == 0x80000000)) {
					Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "SaveLastMusicPausedTime");
					intent.setPackage("android");
					mContext.sendBroadcast(intent);
				}
			}
		});
	}

	public static void VolumeCursorHook() {
		Helpers.findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyDown", int.class, KeyEvent.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				InputMethodService ims = (InputMethodService)param.thisObject;
				int code = (int)param.args[0];
				if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
					String pkgName = Settings.Global.getString(ims.getContentResolver(), Helpers.modulePkg + ".foreground.package");
					if (MainModule.mPrefs.getStringSet("controls_volumecursor_apps").contains(pkgName)) return;
					boolean swapDir = MainModule.mPrefs.getBoolean("controls_volumecursor_reverse");
					ims.sendDownUpKeyEvents(code == (swapDir ? KeyEvent.KEYCODE_VOLUME_DOWN : KeyEvent.KEYCODE_VOLUME_UP) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
					param.setResult(true);
				}
			}
		});

		Helpers.findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyUp", int.class, KeyEvent.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				InputMethodService ims = (InputMethodService)param.thisObject;
				int code = (int)param.args[0];
				if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
					String pkgName = Settings.Global.getString(ims.getContentResolver(), Helpers.modulePkg + ".foreground.package");
					if (!MainModule.mPrefs.getStringSet("controls_volumecursor_apps").contains(pkgName)) param.setResult(true);
				}
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
			} catch (Throwable ignore) {}
			return false;
		} else {
			return GlobalActions.handleAction(context, key);
		}
	}

	private static void addCustomNavBarKeys(boolean isVertical, Context mContext, FrameLayout navButtons, Class<?> kbrCls) {
		String pkgName = "com.android.systemui";
		float density = mContext.getResources().getDisplayMetrics().density;
		int two = Math.round(2 * density);
		int margin = Math.round(MainModule.mPrefs.getInt("controls_navbarmargin", 0) * density);

		Drawable dot1;
		Drawable dot2;
		try {
			Context modCtx = Helpers.getModuleContext(mContext);
			Resources modRes = Helpers.getModuleRes(mContext);
			dot1 = modRes.getDrawable(R.drawable.ic_sysbar_dot_bottomleft, modCtx.getTheme());
			dot2 = modRes.getDrawable(R.drawable.ic_sysbar_dot_topright, modCtx.getTheme());
		} catch (Throwable t) {
			XposedBridge.log(t);
			return;
		}

		int diff1 = (dot1.getIntrinsicWidth() - dot1.getIntrinsicHeight()) / 2;
		int diff2 = (dot2.getIntrinsicWidth() - dot2.getIntrinsicHeight()) / 2;
		LinearLayout leftbtn = new LinearLayout(mContext);
		ImageView left = new ImageView(mContext);

		LinearLayout.LayoutParams lplc;
		if (isVertical)
			lplc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		else
			lplc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		left.setLayoutParams(lplc);

		LinearLayout.LayoutParams lpl = new LinearLayout.LayoutParams(lplc);
		if (isVertical)
			lpl.bottomMargin += margin;
		else
			lpl.leftMargin += margin;
		leftbtn.setLayoutParams(lpl);

		left.setScaleType(ImageView.ScaleType.CENTER);
		left.setImageDrawable(dot1);
		left.setScaleX(0.7f);
		left.setScaleY(0.7f);
		left.setAlpha(0.9f);
		left.setPadding(isVertical ? 0 : two, isVertical ? two + diff1 : 0, isVertical ? 0 : two, isVertical ? two + diff1 : 0);
		left.setTag("custom_left" + (isVertical ? "_vert" : "_horiz"));
		if (kbrCls != null) try {
			Drawable lripple = (Drawable)kbrCls.getConstructor(Context.class, View.class).newInstance(mContext, leftbtn);
			leftbtn.setBackground(lripple);
		} catch (Throwable ignore) {}
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
		LinearLayout.LayoutParams lprc;
		if (isVertical)
			lprc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		else
			lprc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		right.setLayoutParams(lprc);

		FrameLayout.LayoutParams lpr = new FrameLayout.LayoutParams(lprc);
		if (isVertical) {
			lpr.topMargin += margin;
			lpr.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		}
		else {
			lpr.rightMargin += margin;
			lpr.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		}
		rightbtn.setLayoutParams(lpr);

		right.setScaleType(ImageView.ScaleType.CENTER);
		right.setImageDrawable(dot2);
		right.setScaleX(0.7f);
		right.setScaleY(0.7f);
		right.setAlpha(0.9f);
		right.setPadding(isVertical ? 0 : two, isVertical ? two + diff2 : 0, isVertical ? 0 : two, isVertical ? two + diff2 : 0);
		right.setTag("custom_right" + (isVertical ? "_vert" : "_horiz"));
		if (kbrCls != null) try {
			Drawable rripple = (Drawable)kbrCls.getConstructor(Context.class, View.class).newInstance(mContext, rightbtn);
			rightbtn.setBackground(rripple);
		} catch (Throwable ignore) {}
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

//		View startPadding = navButtons.findViewById(navButtons.getResources().getIdentifier("start_padding", "id", pkgName));
//		View sidePadding = navButtons.findViewById(navButtons.getResources().getIdentifier("side_padding", "id", pkgName));
//
//		LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams)startPadding.getLayoutParams();
//		LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)sidePadding.getLayoutParams();

		boolean hasLeftAction = MainModule.mPrefs.getInt("controls_navbarleft_action", 1) > 1 || MainModule.mPrefs.getInt("controls_navbarleftlong_action", 1) > 1;
		boolean hasRightAction = MainModule.mPrefs.getInt("controls_navbarright_action", 1) > 1 || MainModule.mPrefs.getInt("controls_navbarrightlong_action", 1) > 1;

		float part = 0.55f;
		if (isVertical) {
			if (hasRightAction) {
				navButtons.addView(rightbtn, 0);
//				lp2.weight = Math.round(lp2.weight * part);
//				sidePadding.setLayoutParams(lp2);
//				sidePadding.setPadding(sidePadding.getPaddingLeft(), sidePadding.getPaddingTop() / 3, sidePadding.getPaddingRight(), sidePadding.getPaddingBottom());
			}
			if (hasLeftAction) {
				navButtons.addView(leftbtn, navButtons.getChildCount());
//				lp1.weight = Math.round(lp1.weight * part);
//				startPadding.setLayoutParams(lp1);
			}
		} else {
			if (hasLeftAction) {
				navButtons.addView(leftbtn, 0);
//				lp1.weight = Math.round(lp1.weight * part);
//				startPadding.setLayoutParams(lp1);
			}
			if (hasRightAction) {
				navButtons.addView(rightbtn, navButtons.getChildCount());
//				lp2.weight = Math.round(lp2.weight * part);
//				sidePadding.setLayoutParams(lp2);
//				sidePadding.setPadding(sidePadding.getPaddingLeft(), sidePadding.getPaddingTop(), sidePadding.getPaddingRight() / 3, sidePadding.getPaddingBottom());
			}
		}
	}

	public static void NavBarButtonsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.navigationbar.NavigationBar", lpparam.classLoader, "onViewAttachedToWindow", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (mContext == null) {
					Helpers.log("NavBarButtonsHook", "Cannot find context");
					return;
				}
				FrameLayout mNavigationBarView = (FrameLayout)XposedHelpers.getObjectField(param.thisObject, "mNavigationBarView");
				if (mNavigationBarView == null) {
					Helpers.log("NavBarButtonsHook", "Cannot find navbar layout");
					return;
				}
				ViewGroup mHorizontal = mNavigationBarView.findViewById(mNavigationBarView.getResources().getIdentifier("horizontal", "id", lpparam.packageName));
				ViewGroup mVertical = mNavigationBarView.findViewById(mNavigationBarView.getResources().getIdentifier("vertical", "id", lpparam.packageName));
				FrameLayout navButtons0 = mHorizontal.findViewById(mNavigationBarView.getResources().getIdentifier("nav_buttons", "id", lpparam.packageName));
				FrameLayout navButtons90 = mVertical.findViewById(mNavigationBarView.getResources().getIdentifier("nav_buttons", "id", lpparam.packageName));

				Class<?> kbrCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.phone.MiuiKeyButtonRipple", lpparam.classLoader);
				addCustomNavBarKeys(false, mContext, navButtons0, kbrCls);
				addCustomNavBarKeys(true, mContext, navButtons90, kbrCls);
			}
		});

		Helpers.hookAllMethods("com.android.systemui.navigationbar.NavigationBarInflaterView", lpparam.classLoader, "updateBackground", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				FrameLayout navbar = (FrameLayout)param.thisObject;
				boolean isDark = (boolean)param.args[0];
				ImageView hleft = navbar.findViewWithTag("custom_left_horiz");
				ImageView vleft = navbar.findViewWithTag("custom_left_vert");
				ImageView hright = navbar.findViewWithTag("custom_right_horiz");
				ImageView vright = navbar.findViewWithTag("custom_right_vert");

				Context modCtx = Helpers.getModuleContext(navbar.getContext());
				Resources modRes = Helpers.getModuleRes(navbar.getContext());
				if (isDark) {
					Drawable darkImg1 = modRes.getDrawable(R.drawable.ic_sysbar_dot_bottomleft_dark, modCtx.getTheme());
					Drawable darkImg2 = modRes.getDrawable(R.drawable.ic_sysbar_dot_topright_dark, modCtx.getTheme());
					if (hleft != null) hleft.setImageDrawable(darkImg1);
					if (vleft != null) vleft.setImageDrawable(darkImg1);
					if (hright != null) hright.setImageDrawable(darkImg2);
					if (vright != null) vright.setImageDrawable(darkImg2);
				} else {
					Drawable lightImg1 = modRes.getDrawable(R.drawable.ic_sysbar_dot_bottomleft, modCtx.getTheme());
					Drawable lightImg2 = modRes.getDrawable(R.drawable.ic_sysbar_dot_topright, modCtx.getTheme());
					if (hleft != null) hleft.setImageDrawable(lightImg1);
					if (vleft != null) vleft.setImageDrawable(lightImg1);
					if (hright != null) hright.setImageDrawable(lightImg2);
					if (vright != null) vright.setImageDrawable(lightImg2);
				}
			}
		});
	}

	@SuppressLint("MissingPermission")
	private static boolean handleCallAction(int action) {
		TelecomManager tm = (TelecomManager)miuiPWMContext.getSystemService(Context.TELECOM_SERVICE);
		if (tm == null) return false;
		if (!tm.isInCall()) return false;
		int callState = (int)XposedHelpers.callMethod(tm, "getCallState");
		if (callState == TelephonyManager.CALL_STATE_RINGING) {
			int accept = MainModule.mPrefs.getStringAsInt("controls_fingerprint_accept", 1);
			int reject = MainModule.mPrefs.getStringAsInt("controls_fingerprint_reject", 1);
			if (action == accept) {
				XposedHelpers.callMethod(tm, "acceptRingingCall");
				return true;
			} else if (action == reject) {
				XposedHelpers.callMethod(tm, "endCall");
				return true;
			}
		} else if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
			int hangup = MainModule.mPrefs.getStringAsInt("controls_fingerprint_hangup", 1);
			if (action == hangup) {
				XposedHelpers.callMethod(tm, "endCall");
				return true;
			}
		}
		return MainModule.mPrefs.getBoolean("controls_fingerprintskip2");
	}

	@SuppressLint("StaticFieldLeak")
	private static Context miuiPWMContext;
	private static Handler miuiPWMHandler;
	private static boolean hasDoubleTap = false;
	private static boolean wasScreenOn = false;
	private static boolean wasFingerprintUsed = false;
	private static boolean isFingerprintPressed = false;
	private static boolean isFingerprintLongPressed = false;
	private static boolean isFingerprintLongPressHandled = false;
	private static final Runnable singlePressFingerprint = new Runnable() {
		@Override
		public void run() {
			if (miuiPWMContext == null || miuiPWMHandler == null) return;
			miuiPWMHandler.removeCallbacks(longPressFingerprint);
			if (!handleCallAction(2)) GlobalActions.handleAction(miuiPWMContext, "pref_key_controls_fingerprint1");
		}
	};
	private static final Runnable longPressFingerprint = new Runnable() {
		@Override
		public void run() {
			if (isFingerprintPressed) {
				isFingerprintLongPressed = true;
				isFingerprintLongPressHandled = handleCallAction(4);
				Helpers.performStrongVibration(miuiPWMContext, true);
			}
		}
	};

	public static void FingerprintEventsHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "initInternal", new MethodHook() {
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
						if (!key.contains("pref_key_controls_fingerprint")) return;

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

		Helpers.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.classLoader, "processFingerprintNavigationEvent", KeyEvent.class, boolean.class, new MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
			protected void before(MethodHookParam param) throws Throwable {
				if (MainModule.mPrefs.getBoolean("controls_fingerprintskip")) {
					Object mFocusedWindow = XposedHelpers.getObjectField(param.thisObject, "mFocusedWindow");
					if ((boolean)param.args[1] && mFocusedWindow != null) {
						String ownPkg = (String)XposedHelpers.callMethod(mFocusedWindow, "getOwningPackage");
						if ("com.android.camera".equals(ownPkg)) return;
					}
				}

				miuiPWMContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				miuiPWMHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
				if (miuiPWMContext == null || miuiPWMHandler == null) return;

				boolean isInCall = false;
				if (miuiPWMContext != null) {
					TelecomManager tm = (TelecomManager)miuiPWMContext.getSystemService(Context.TELECOM_SERVICE);
					isInCall = tm != null && tm.isInCall();
				}

				KeyEvent keyEvent = (KeyEvent)param.args[0];
				if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.getAction() != KeyEvent.ACTION_DOWN) return;

				isFingerprintPressed = true;
				wasScreenOn = (boolean)XposedHelpers.callMethod(param.thisObject, "isScreenOnInternal");
				wasFingerprintUsed = Settings.System.getInt(miuiPWMContext.getContentResolver(), "is_fingerprint_active", 0) == 1;

				hasDoubleTap = false;
				if (wasScreenOn && !wasFingerprintUsed) {
					int delay = MainModule.mPrefs.getInt("controls_fingerprintlong_delay", 0);
					if (isInCall) {
						int accept = MainModule.mPrefs.getStringAsInt("controls_fingerprint_accept", 1);
						int reject = MainModule.mPrefs.getStringAsInt("controls_fingerprint_reject", 1);
						int hangup = MainModule.mPrefs.getStringAsInt("controls_fingerprint_hangup", 1);
						hasDoubleTap = accept == 3 || reject == 3 || hangup == 3;
						if (accept == 4 || reject == 4 || hangup == 4)
						miuiPWMHandler.postDelayed(longPressFingerprint, delay < 200 ? ViewConfiguration.getLongPressTimeout() : delay);
					} else {
						int dtaction = MainModule.mPrefs.getInt("controls_fingerprint2_action", 1);
						hasDoubleTap = dtaction > 1;
						if (MainModule.mPrefs.getInt("controls_fingerprintlong_action", 1) > 1)
						miuiPWMHandler.postDelayed(longPressFingerprint, delay < 200 ? ViewConfiguration.getLongPressTimeout() : delay);
					}
				}

				if (XposedHelpers.getAdditionalInstanceField(param.thisObject, "touchTime") == null) {
					XposedHelpers.setAdditionalInstanceField(param.thisObject, "touchTime", 0L);
				}
			}

			@Override
			@SuppressLint("MissingPermission")
			protected void after(MethodHookParam param) throws Throwable {
				if (MainModule.mPrefs.getBoolean("controls_fingerprintskip")) {
					Object mFocusedWindow = XposedHelpers.getObjectField(param.thisObject, "mFocusedWindow");
					if ((boolean)param.args[1] && mFocusedWindow != null) {
						String ownPkg = (String)XposedHelpers.callMethod(mFocusedWindow, "getOwningPackage");
						if ("com.android.camera".equals(ownPkg)) return;
					}
				}

				KeyEvent keyEvent = (KeyEvent)param.args[0];
				if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_DPAD_CENTER || keyEvent.getAction() != KeyEvent.ACTION_UP) return;

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

				long lastTouchTime = (long)XposedHelpers.getAdditionalInstanceField(param.thisObject, "touchTime");
				long currentTouchTime = currentTimeMillis();
				XposedHelpers.setAdditionalInstanceField(param.thisObject, "touchTime", currentTouchTime);

				int delay = MainModule.mPrefs.getInt("controls_fingerprint2_delay", 50);
				int dttimeout = delay < 200 ? ViewConfiguration.getDoubleTapTimeout() : delay;
				if (wasScreenOn && !wasFingerprintUsed) {
					if (hasDoubleTap && currentTouchTime - lastTouchTime < dttimeout) {
						mHandler.removeCallbacks(singlePressFingerprint);
						mHandler.removeCallbacks(longPressFingerprint);
						if (!handleCallAction(3)) GlobalActions.handleAction(mContext, "pref_key_controls_fingerprint2");
						wasScreenOn = false;
					} else if (isFingerprintLongPressed) {
						if (!isFingerprintLongPressHandled) GlobalActions.handleAction(mContext, "pref_key_controls_fingerprintlong");
						isFingerprintLongPressHandled = false;
						wasScreenOn = false;
					} else {
						mHandler.removeCallbacks(longPressFingerprint);
						mHandler.removeCallbacks(singlePressFingerprint);
						if (hasDoubleTap)
							mHandler.postDelayed(singlePressFingerprint, dttimeout);
						else
							mHandler.post(singlePressFingerprint);
					}
				}

				isFingerprintLongPressed = false;
				isFingerprintPressed = false;
			}
		});

		String fpService = "com.android.server.biometrics.BiometricServiceBase";
		Helpers.hookAllMethods(fpService, lpparam.classLoader, "startClient", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
			Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
			Settings.System.putInt(mContext.getContentResolver(), "is_fingerprint_active", 1);
			}
		});

		Helpers.hookAllMethods(fpService, lpparam.classLoader, "removeClient", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
			Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
			Settings.System.putInt(mContext.getContentResolver(), "is_fingerprint_active", 0);
			}
		});
	}

	@SuppressLint("StaticFieldLeak")
	private static Context basePWMContext;
	private static Object basePWMObject;
	private static Method markShortcutTriggered;

	private static final Runnable mBackLongPressAction = new Runnable() {
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
	private static final Runnable mHomeLongPressAction = new Runnable() {
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
	private static final Runnable mMenuLongPressAction = new Runnable() {
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
		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "initInternal", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
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

		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "postKeyLongPress", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
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

		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "removeKeyLongPress", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				int key = (int)param.args[0];
				if (key == KeyEvent.KEYCODE_BACK)
					((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).removeCallbacks(mBackLongPressAction);
				else if (key == KeyEvent.KEYCODE_HOME)
					((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).removeCallbacks(mHomeLongPressAction);
				else if (key == KeyEvent.KEYCODE_APP_SWITCH)
					((Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler")).removeCallbacks(mMenuLongPressAction);
			}
		});
	}

	public static void NavbarHeightRes() {
		int opt = MainModule.mPrefs.getInt("controls_navbarheight", 19);
		int heightDpi = opt == 19 ? 47 : opt;
		MainModule.resHooks.setDensityReplacement("*", "dimen", "navigation_bar_height", heightDpi);
		MainModule.resHooks.setDensityReplacement("*", "dimen", "navigation_bar_height_landscape", heightDpi);
		MainModule.resHooks.setDensityReplacement("*", "dimen", "navigation_bar_frame_height", heightDpi);
		MainModule.resHooks.setDensityReplacement("*", "dimen", "navigation_bar_frame_height_landscape", heightDpi);
		MainModule.resHooks.setDensityReplacement("*", "dimen", "navigation_bar_gesture_height", heightDpi);
		MainModule.resHooks.setDensityReplacement("*", "dimen", "navigation_bar_width", heightDpi);
		MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "navigation_bar_size", heightDpi);
		//MainModule.resHooks.setDensityReplacement("com.android.systemui", "dimen", "navigation_extra_key_width", heightDpi);
	}

	public static void FingerprintHapticSuccessHook(LoadPackageParam lpparam) {
		String authClient = "com.android.server.biometrics.AuthenticationClient";
		Helpers.hookAllMethods(authClient, lpparam.classLoader, "onAuthenticated", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (!((boolean)param.getResult())) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");

				boolean ignoreSystem = MainModule.mPrefs.getBoolean("controls_fingerprintsuccess_ignore");
				int opt = Integer.parseInt(MainModule.mPrefs.getString("controls_fingerprintsuccess", "1"));
				if (opt == 2)
					Helpers.performLightVibration(mContext, ignoreSystem);
				else if (opt == 3)
					Helpers.performStrongVibration(mContext, ignoreSystem);
			}
		});
	}

	public static void FingerprintHapticFailureHook(LoadPackageParam lpparam) {
		String monitorClass = "com.android.server.biometrics.ClientMonitor";
		Helpers.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "vibrate", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
				for (StackTraceElement el : stackTrace)
				if (monitorClass.equals(el.getClassName()) && "vibrateError".equals(el.getMethodName())) {
					param.setResult(null);
					return;
				}
			}
		});
	}

	public static void FingerprintScreenOnHook(LoadPackageParam lpparam) {
		String authClient = "com.android.server.biometrics.AuthenticationClient";
		Helpers.hookAllMethods(authClient, lpparam.classLoader, "onAuthenticated", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if ((boolean)param.getResult()) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				PowerManager mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
				if (mPowerManager.isInteractive()) return;
				if (!GlobalActions.wakeUp(mContext)) Helpers.log("FingerprintScreenOnHook", "Failed to wake up device");
			}
		});
	}

	public static void BackGestureAreaHeightHook(LoadPackageParam lpparam, boolean isNative) {
		if (!Helpers.findAndHookMethodSilently(isNative ? "com.android.systemui.fsgesture.GestureStubView" : "com.miui.home.recents.GestureStubView", lpparam.classLoader, "getGestureStubWindowParam", new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				WindowManager.LayoutParams lp = (WindowManager.LayoutParams)param.getResult();
				int pct = MainModule.mPrefs.getInt("controls_fsg_coverage", 60);
				lp.height = Math.round(lp.height / 60.0f * pct);
				param.setResult(lp);
			}
		})) if (isNative) Helpers.log("BackGestureAreaHeightHook", "Cannot hook GestureStubView");
	}

	public static void BackGestureAreaWidthHook(LoadPackageParam lpparam, boolean isNative) {
		if (!Helpers.findAndHookMethodSilently(isNative ? "com.android.systemui.fsgesture.GestureStubView" : "com.miui.home.recents.GestureStubView", lpparam.classLoader, "initScreenSizeAndDensity", int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				int pct = MainModule.mPrefs.getInt("controls_fsg_width", 100);
				if (pct == 100) return;
				int mGestureStubDefaultSize = XposedHelpers.getIntField(param.thisObject, "mGestureStubDefaultSize");
				int mGestureStubSize  = XposedHelpers.getIntField(param.thisObject, "mGestureStubSize");
				mGestureStubDefaultSize = Math.round(mGestureStubDefaultSize * pct / 100f);
				mGestureStubSize = Math.round(mGestureStubSize * pct / 100f);
				XposedHelpers.setIntField(param.thisObject, "mGestureStubDefaultSize", mGestureStubDefaultSize);
				XposedHelpers.setIntField(param.thisObject, "mGestureStubSize", mGestureStubSize);
			}
		})) if (isNative) Helpers.log("BackGestureAreaWidthHook", "Cannot hook GestureStubView1");

		if (!Helpers.findAndHookMethodSilently(isNative ? "com.android.systemui.fsgesture.GestureStubView" : "com.miui.home.recents.GestureStubView", lpparam.classLoader, "setSize", int.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				int pct = MainModule.mPrefs.getInt("controls_fsg_width", 100);
				if (pct == 100) return;
				int mGestureStubDefaultSize = XposedHelpers.getIntField(param.thisObject, "mGestureStubDefaultSize");
				if ((int)param.args[0] == mGestureStubDefaultSize) return;
				param.args[0] = Math.round((int)param.args[0] * pct / 100f);
			}
		})) if (isNative) Helpers.log("BackGestureAreaWidthHook", "Cannot hook GestureStubView2");
	}

	public static void HideNavBarHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.statusbar.phone.NavigationModeControllerExt", lpparam.classLoader, "hideNavigationBar", XC_MethodReplacement.returnConstant(true));
	}

	public static void ImeBackAltIconHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "setImeWindowStatus", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Object mNavigationBarView = XposedHelpers.getObjectField(param.thisObject, "mNavigationBarView");
				if (mNavigationBarView != null) XposedHelpers.callMethod(mNavigationBarView, "setNavigationIconHints", param.args[1], false);
			}
		});
	}

	public static void PowerDoubleTapActionHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "launchApp", Intent.class, new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				Intent intent = (Intent)param.args[0];
				if (intent == null) return;
				if (!"android.media.action.STILL_IMAGE_CAMERA".equals(intent.getAction())) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (GlobalActions.handleAction(mContext, "pref_key_controls_powerdt", true)) param.setResult(true);
			}
		});
	}

	public static void NoScreenshotChordRes() {
		MainModule.resHooks.setObjectReplacement("android", "bool", "config_enableScreenshotChord", false);
	}

	public static void NoFingerprintWakeHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.classLoader, "processBackFingerprintDpcenterEvent", KeyEvent.class, boolean.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				boolean isScreenOn = (boolean)param.args[1];
				if (!isScreenOn) param.setResult(null);
			}
		});
	}

	public static void AssistGestureActionHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.systemui.assist.AssistManager", lpparam.classLoader, "startAssist", Bundle.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				Bundle bundle = (Bundle)param.args[0];
				if (bundle == null || bundle.getInt("triggered_by", 0) != 83 || bundle.getInt("invocation_type", 0) != 1) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (GlobalActions.handleAction(mContext, "pref_key_controls_fsg_assist")) {
					Helpers.performLightVibration(mContext);
					param.setResult(null);
				}
			}
		});

		Helpers.hookAllConstructors("com.android.systemui.assist.ui.DefaultUiController", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)param.args[0];
				Handler mHandler = new Handler(mContext.getMainLooper());

				new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_controls_fsg_assist_action", 1) {
					@Override
					public void onChange(String name, int defValue) {
						Object mInvocationLightsView = XposedHelpers.getObjectField(param.thisObject, "mInvocationLightsView");
						if (mInvocationLightsView == null) return;
						int opt = Helpers.getSharedIntPref(mContext, name, defValue);
						if (opt > 1)
							XposedHelpers.callMethod(mInvocationLightsView, "setColors", Color.parseColor("#424A60"), Color.parseColor("#EC7C6D"), Color.parseColor("#EC7C6D"), Color.parseColor("#424A60"));
						else
							XposedHelpers.callMethod(mInvocationLightsView, "setColors", -16776961, -65536, -256, -16711936);
					}
				}.onChange(false);
			}
		});
	}

//	public static void AIButtonHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "startAiKeyService", String.class, new MethodHook() {
//			@Override
//			protected void before(MethodHookParam param) throws Throwable {
//				Helpers.log("AIButtonHook", "startAiKeyService: " + param.args[0]);
//			}
//		});
//	}

//	public static void SwapVolumeKeysHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.server.audio.AudioService", lpparam.classLoader, "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, String.class, int.class, new MethodHook() {
//			@Override
//			protected void before(MethodHookParam param) throws Throwable {
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