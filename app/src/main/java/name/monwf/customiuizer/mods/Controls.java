package name.monwf.customiuizer.mods;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedInterface.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;
import io.github.libxposed.api.XposedModuleInterface.SystemServerLoadedParam;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.mods.utils.HookerClassHelper;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;

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

	public static void PowerKeyHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "init", new MethodHook() {
			@Override
			protected void after(AfterHookCallback param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
				mContext.registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
			}
		});

		ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.getClassLoader(), "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isVolumePressed) return;
				KeyEvent keyEvent = (KeyEvent)param.getArgs()[0];

				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();

				// Ignore repeated KeyEvents simulated on Power Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) != KeyEvent.FLAG_FROM_SYSTEM || keycode != KeyEvent.KEYCODE_POWER) return;

				// Power long press
				final Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
				final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.getThisObject(), "mPowerManager");
				if (mPowerManager.isInteractive()) return;
				//XposedHelpers.log("PowerKeyHook", "interceptKeyBeforeQueueing: " + param.getArgs()[1] + ", isTracking: " + keyEvent.isTracking() + " | Source: " + keyEvent.getSource() + " | KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags());
				if (action == KeyEvent.ACTION_DOWN) {
					isPowerPressed = true;
					isPowerLongPressed = false;

					mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");

					int longPressDelay = (MainModule.mPrefs.getBoolean("controls_powerflash_delay") ? ViewConfiguration.getLongPressTimeout() * 3 : ViewConfiguration.getLongPressTimeout()) + 500;
					// Post only one delayed runnable that waits for long press timeout
					if (!isWaitingForPowerLongPressed) {
						mHandler.postDelayed(new Runnable() {
							@Override
							@SuppressLint("Wakelock")
							public void run() {
								if (isPowerPressed) {
									isPowerLongPressed = true;

									if (Helpers.mWakeLock == null) {
										Helpers.mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "miuizer:flashlight");
									}

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
					}

					isWaitingForPowerLongPressed = true;
					param.returnAndSkip(0);
				}

				if (action == KeyEvent.ACTION_UP) {
					if (isPowerPressed && !isPowerLongPressed) try {
						if (isTorchEnabled(mContext)) setTorch(mContext, false);
						if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
						XposedHelpers.callMethod(mPowerManager, "wakeUp", SystemClock.uptimeMillis());
						param.returnAndSkip(0);
					} catch (Throwable t) {
						XposedHelpers.log(t);
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

	public static void VolumeMediaButtonsHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.getClassLoader(), "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
			protected void before(final BeforeHookCallback param) throws Throwable {
				// Power and volkeys are pressed at the same time
				if (isPowerPressed) return;
				final KeyEvent keyEvent = (KeyEvent)param.getArgs()[0];

				int keycode = keyEvent.getKeyCode();
				int action = keyEvent.getAction();
				int flags = keyEvent.getFlags();

				// Ignore repeated KeyEvents simulated on volume Key Up
				if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) return;
				if ((flags & KeyEvent.FLAG_FROM_SYSTEM) != KeyEvent.FLAG_FROM_SYSTEM || (keycode != KeyEvent.KEYCODE_VOLUME_UP && keycode != KeyEvent.KEYCODE_VOLUME_DOWN)) return;

				// Volume long press
				final Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
				final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(param.getThisObject(), "mPowerManager");
				if (mPowerManager.isInteractive()) return;
				//XposedHelpers.log("VolumeMediaButtonsHook", "interceptKeyBeforeQueueing: KeyCode: " + keyEvent.getKeyCode() + " | Action: " + keyEvent.getAction() + " | RepeatCount: " + keyEvent.getRepeatCount()+ " | Flags: " + keyEvent.getFlags() + " | " + mPowerManager.isInteractive());
				if (action == KeyEvent.ACTION_DOWN) {
					isVolumePressed = true;
					isVolumeLongPressed = false;

					mHandler = (Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler");

					// Post only one delayed runnable that waits for long press timeout
					if (mHandler != null && !isWaitingForVolumeLongPressed) {
						mHandler.postDelayed(new Runnable() {
							public void run() {
								if (isVolumePressed && GlobalActions.isMediaActionsAllowed(mContext)) {
									isVolumeLongPressed = true;
									switch (keyEvent.getKeyCode()) {
										case KeyEvent.KEYCODE_VOLUME_UP:
											int pref_mediaUp = MainModule.mPrefs.getStringAsInt("controls_volumemedia_up", 0);
											if (pref_mediaUp == 0) break;
											GlobalActions.sendDownUpKeyEvent(mContext, pref_mediaUp, true);
											break;
										case KeyEvent.KEYCODE_VOLUME_DOWN:
											int pref_mediaDown = MainModule.mPrefs.getStringAsInt("controls_volumemedia_down", 0);
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
					}

					isWaitingForVolumeLongPressed = true;
					param.returnAndSkip(0);
				}

				if (action == KeyEvent.ACTION_UP) {
					isVolumePressed = false;
					// Kill all callbacks (removing only posted Runnable is not working... no idea)
					if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
					if (!isVolumeLongPressed) {
						AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
						TelecomManager tm = (TelecomManager)mContext.getSystemService(Context.TELECOM_SERVICE);
						WakeLock mBroadcastWakeLock = (WakeLock)XposedHelpers.getObjectField(param.getThisObject(), "mBroadcastWakeLock");
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
					param.returnAndSkip(0);
					isWaitingForVolumeLongPressed = false;
				}
			}
		});
	}

	public static void VolumeMediaPlayerHook(PackageLoadedParam lpparam) {
		Class<?> MediaPlayerCls = XposedHelpers.findClass("android.media.MediaPlayer", lpparam.getClassLoader());
		ModuleHelper.findAndHookMethod(MediaPlayerCls, "pause", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				Context mContext = ModuleHelper.findContext(lpparam);
				int mStreamType = (int)XposedHelpers.findMethodExact(MediaPlayerCls, "getAudioStreamType").invoke(param.getThisObject());
				if (mContext != null && (mStreamType == AudioManager.STREAM_MUSIC || mStreamType == 0x80000000)) {
					Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "SaveLastMusicPausedTime");
					intent.setPackage("com.android.systemui");
					mContext.sendBroadcast(intent);
				}
			}
		});
	}

	public static void VolumeCursorHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.getClassLoader(), "onKeyDown", int.class, KeyEvent.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				InputMethodService ims = (InputMethodService)param.getThisObject();
				int code = (int)param.getArgs()[0];
				if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
					String pkgName = Settings.Global.getString(ims.getContentResolver(), Helpers.modulePkg + ".foreground.package");
					if (MainModule.mPrefs.getStringSet("controls_volumecursor_apps").contains(pkgName)) return;
					boolean swapDir = MainModule.mPrefs.getBoolean("controls_volumecursor_reverse");
					ims.sendDownUpKeyEvents(code == (swapDir ? KeyEvent.KEYCODE_VOLUME_DOWN : KeyEvent.KEYCODE_VOLUME_UP) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
					param.returnAndSkip(true);
				}
			}
		});

		ModuleHelper.findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.getClassLoader(), "onKeyUp", int.class, KeyEvent.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				InputMethodService ims = (InputMethodService)param.getThisObject();
				int code = (int)param.getArgs()[0];
				if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
					String pkgName = Settings.Global.getString(ims.getContentResolver(), Helpers.modulePkg + ".foreground.package");
					if (!MainModule.mPrefs.getStringSet("controls_volumecursor_apps").contains(pkgName))
						param.returnAndSkip(true);
				}
			}
		});
	}

	private static boolean handleNavBarAction(Context context, String key) {
		int action = MainModule.mPrefs.getInt(key + "_action", 1);
		if (action >= 85 && action <= 88) {
			if (GlobalActions.isMediaActionsAllowed(context)) {
				GlobalActions.sendDownUpKeyEvent(context, action, false);
			}
			return true;
		} else if (action == 1) {
			try {
				Toast.makeText(ModuleHelper.getModuleContext(context), R.string.controls_navbar_noaction, Toast.LENGTH_SHORT).show();
			} catch (Throwable ignore) {}
			return false;
		} else {
			return GlobalActions.handleAction(context, key);
		}
	}

	private static void reposNavBarButtons(FrameLayout navbar) {
		Context mContext = navbar.getContext();
		int displayRotation = navbar.getContext().getDisplay().getRotation();
		float density = mContext.getResources().getDisplayMetrics().density;
		int margin = Math.round(MainModule.mPrefs.getInt("controls_navbarmargin", 0) * density);
		if (displayRotation == Surface.ROTATION_0) {
			ImageView hleft = navbar.findViewWithTag("custom_left_horiz");
			if (hleft != null) {
				LinearLayout leftbtn = (LinearLayout) hleft.getParent();
				FrameLayout.LayoutParams lpl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
				lpl.leftMargin += margin;
				lpl.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
				leftbtn.setLayoutParams(lpl);
			}

			ImageView hright = navbar.findViewWithTag("custom_right_horiz");
			if (hright != null) {
				LinearLayout rightbtn = (LinearLayout) hright.getParent();
				FrameLayout.LayoutParams lpr = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
				lpr.rightMargin += margin;
				lpr.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
				rightbtn.setLayoutParams(lpr);
			}
		}
		else  {
			ImageView vleft = navbar.findViewWithTag("custom_left_vert");
			ImageView vright = navbar.findViewWithTag("custom_right_vert");

			LinearLayout leftbtn = null;
			if (vleft != null) {
				leftbtn = (LinearLayout) vleft.getParent();
			}
			FrameLayout.LayoutParams lpl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

			LinearLayout rightbtn = null;
			if (vright != null) {
				rightbtn = (LinearLayout) vright.getParent();
			}
			FrameLayout.LayoutParams lpr = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			if (displayRotation == Surface.ROTATION_270) {
				lpl.topMargin += margin;
				lpl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

				lpr.bottomMargin += margin;
				lpr.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			}
			else if (displayRotation == Surface.ROTATION_90) {
				lpr.topMargin += margin;
				lpr.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

				lpl.bottomMargin += margin;
				lpl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			}
			if (leftbtn != null) leftbtn.setLayoutParams(lpl);
			if (rightbtn != null) rightbtn.setLayoutParams(lpr);
		}
	}

	private static void addCustomNavBarKeys(boolean isVertical, Context mContext, FrameLayout navButtons, Class<?> kbrCls) {
		Drawable dot1;
		Drawable dot2;
		try {
			Context modCtx = ModuleHelper.getModuleContext(mContext);
			Resources modRes = ModuleHelper.getModuleRes(mContext);
			dot1 = modRes.getDrawable(R.drawable.ic_sysbar_dot_bottomleft, modCtx.getTheme());
			dot2 = modRes.getDrawable(R.drawable.ic_sysbar_dot_topright, modCtx.getTheme());
		} catch (Throwable t) {
			XposedHelpers.log(t);
			return;
		}

		LinearLayout leftbtn = new LinearLayout(mContext);
		ImageView left = new ImageView(mContext);

		LinearLayout.LayoutParams lplc;
		if (isVertical)
			lplc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		else
			lplc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
		left.setLayoutParams(lplc);
		left.setImageDrawable(dot1);
		left.setAlpha(0.9f);
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
				handleNavBarAction(v.getContext(), "controls_navbarleft");
			}
		});
		leftbtn.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return handleNavBarAction(v.getContext(), "controls_navbarleftlong");
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
		right.setImageDrawable(dot2);
		right.setAlpha(0.9f);
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
				handleNavBarAction(v.getContext(), "controls_navbarright");
			}
		});
		rightbtn.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return handleNavBarAction(v.getContext(), "controls_navbarrightlong");
			}
		});
		rightbtn.addView(right);

		boolean hasLeftAction = MainModule.mPrefs.getInt("controls_navbarleft_action", 1) > 1 || MainModule.mPrefs.getInt("controls_navbarleftlong_action", 1) > 1;
		boolean hasRightAction = MainModule.mPrefs.getInt("controls_navbarright_action", 1) > 1 || MainModule.mPrefs.getInt("controls_navbarrightlong_action", 1) > 1;

//		float part = 0.55f;
		if (isVertical) {
			if (hasRightAction) {
				navButtons.addView(rightbtn, 0);
//				lp2.weight = Math.round(lp2.weight * part);
			}
			if (hasLeftAction) {
				navButtons.addView(leftbtn, navButtons.getChildCount());
//				lp1.weight = Math.round(lp1.weight * part);
			}
		} else {
			if (hasLeftAction) {
				navButtons.addView(leftbtn, 0);
//				lp1.weight = Math.round(lp1.weight * part);
			}
			if (hasRightAction) {
				navButtons.addView(rightbtn, navButtons.getChildCount());
//				lp2.weight = Math.round(lp2.weight * part);
			}
		}
	}

	public static void NavBarButtonsHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				FrameLayout navBar = (FrameLayout) param.getThisObject();
				Context mContext = navBar.getContext();
				ViewGroup mHorizontal = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "mHorizontal");
				ViewGroup mVertical = (ViewGroup) XposedHelpers.getObjectField(param.getThisObject(), "mVertical");
				int navButtonsId = navBar.getResources().getIdentifier("nav_buttons", "id", lpparam.getPackageName());
				FrameLayout navButtons0 = mHorizontal.findViewById(navButtonsId);
				FrameLayout navButtons90 = mVertical.findViewById(navButtonsId);

				Class<?> kbrCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.phone.MiuiKeyButtonRipple", lpparam.getClassLoader());
				addCustomNavBarKeys(false, mContext, navButtons0, kbrCls);
				addCustomNavBarKeys(true, mContext, navButtons90, kbrCls);
				reposNavBarButtons(navBar);
			}
		});

		ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBarTransitions", lpparam.getClassLoader(), "applyDarkIntensity", float.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				FrameLayout navbar = (FrameLayout)XposedHelpers.getObjectField(param.getThisObject(), "mView");
				boolean isDark = (float)param.getArgs()[0] > 0.5f;
				ImageView hleft = navbar.findViewWithTag("custom_left_horiz");
				ImageView vleft = navbar.findViewWithTag("custom_left_vert");
				ImageView hright = navbar.findViewWithTag("custom_right_horiz");
				ImageView vright = navbar.findViewWithTag("custom_right_vert");

				Context modCtx = ModuleHelper.getModuleContext(navbar.getContext());
				Resources modRes = ModuleHelper.getModuleRes(navbar.getContext());
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
		ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBarView", lpparam.getClassLoader(), "onConfigurationChanged", Configuration.class,
		new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				FrameLayout navbar = (FrameLayout) param.getThisObject();
				reposNavBarButtons(navbar);
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
				if (GlobalActions.handleAction(basePWMContext, "controls_backlong")) Helpers.performStrongVibration(basePWMContext);
				if (MainModule.mPrefs.getInt("controls_backlong_action", 1) != 1) markShortcutTriggered.invoke(basePWMObject);
			} catch (Throwable t) {
				XposedHelpers.log(t);
			}
		}
	};
	private static final Runnable mHomeLongPressAction = new Runnable() {
		@Override
		public void run() {
			try {
				if (basePWMContext == null || basePWMObject == null) return;
				if (GlobalActions.handleAction(basePWMContext, "controls_homelong")) Helpers.performStrongVibration(basePWMContext);
				if (MainModule.mPrefs.getInt("controls_homelong_action", 1) != 1) markShortcutTriggered.invoke(basePWMObject);
			} catch (Throwable t) {
				XposedHelpers.log(t);
			}
		}
	};
	private static final Runnable mMenuLongPressAction = new Runnable() {
		@Override
		public void run() {
			try {
				if (basePWMContext == null || basePWMObject == null) return;
				if (GlobalActions.handleAction(basePWMContext, "controls_menulong")) Helpers.performStrongVibration(basePWMContext);
				if (MainModule.mPrefs.getInt("controls_menulong_action", 1) != 1) markShortcutTriggered.invoke(basePWMObject);
			} catch (Throwable t) {
				XposedHelpers.log(t);
			}
		}
	};

	public static void NavBarActionsHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "postKeyLongPress", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				if (basePWMObject == null) basePWMObject = param.getThisObject();
				if (basePWMContext == null) basePWMContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
				if (markShortcutTriggered == null) markShortcutTriggered = XposedHelpers.findMethodExact("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "markShortcutTriggered");

				int key = (int)param.getArgs()[0];
				if (key == KeyEvent.KEYCODE_BACK && MainModule.mPrefs.getInt("controls_backlong_action", 1) > 1) {
					((Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler")).postDelayed(mBackLongPressAction, ViewConfiguration.getLongPressTimeout());
					param.returnAndSkip(null);
				} else if (key == KeyEvent.KEYCODE_HOME && MainModule.mPrefs.getInt("controls_homelong_action", 1) > 1) {
					((Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler")).postDelayed(mHomeLongPressAction, ViewConfiguration.getLongPressTimeout());
					param.returnAndSkip(null);
				} else if (key == KeyEvent.KEYCODE_APP_SWITCH && MainModule.mPrefs.getInt("controls_menulong_action", 1) > 1) {
					((Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler")).postDelayed(mMenuLongPressAction, ViewConfiguration.getLongPressTimeout());
					param.returnAndSkip(null);
				}
			}
		});

		ModuleHelper.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "removeKeyLongPress", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				int key = (int)param.getArgs()[0];
				if (key == KeyEvent.KEYCODE_BACK)
					((Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler")).removeCallbacks(mBackLongPressAction);
				else if (key == KeyEvent.KEYCODE_HOME)
					((Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler")).removeCallbacks(mHomeLongPressAction);
				else if (key == KeyEvent.KEYCODE_APP_SWITCH)
					((Handler)XposedHelpers.getObjectField(param.getThisObject(), "mHandler")).removeCallbacks(mMenuLongPressAction);
			}
		});
	}

	public static void FingerprintHapticSuccessHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.hookAllMethods("com.android.server.biometrics.sensors.AuthenticationClient", lpparam.getClassLoader(), "onAuthenticated", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				boolean mAuthSuccess = XposedHelpers.getBooleanField(param.getThisObject(), "mAuthSuccess");
				if (!mAuthSuccess) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");

				boolean ignoreSystem = MainModule.mPrefs.getBoolean("controls_fingerprintsuccess_ignore");
				int opt = Integer.parseInt(MainModule.mPrefs.getString("controls_fingerprintsuccess", "1"));
				if (opt == 2)
					Helpers.performLightVibration(mContext, ignoreSystem);
				else if (opt == 3)
					Helpers.performStrongVibration(mContext, ignoreSystem);
			}
		});
	}

	public static void FingerprintHapticFailureHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.biometrics.sensors.AcquisitionClient", lpparam.getClassLoader(), "vibrateError", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				param.returnAndSkip(null);
			}
		});
	}

	public static void FingerprintScreenOnHook(SystemServerLoadedParam lpparam) {
		String authClient = "com.android.server.biometrics.sensors.AuthenticationClient";
		ModuleHelper.hookAllMethods(authClient, lpparam.getClassLoader(), "onAuthenticated", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				boolean mAuthSuccess = XposedHelpers.getBooleanField(param.getThisObject(), "mAuthSuccess");
				if (mAuthSuccess) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
				PowerManager mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
				if (mPowerManager.isInteractive()) return;
				if (!GlobalActions.commonSendAction(mContext, "WakeUp")) XposedHelpers.log("FingerprintScreenOnHook", "Failed to wake up device");
			}
		});
	}

	public static void BackGestureAreaHeightHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "getGestureStubWindowParam", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				WindowManager.LayoutParams lp = (WindowManager.LayoutParams)param.getResult();
				int pct = MainModule.mPrefs.getInt("controls_fsg_coverage", 60);
				lp.height = Math.round(lp.height / 60.0f * pct);
				param.setResult(lp);
			}
		});
	}

	public static void BackGestureAreaWidthHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "initScreenSizeAndDensity", int.class, new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				int pct = MainModule.mPrefs.getInt("controls_fsg_width", 100);
				if (pct == 100) return;
				int mGestureStubDefaultSize = XposedHelpers.getIntField(param.getThisObject(), "mGestureStubDefaultSize");
				int mGestureStubSize  = XposedHelpers.getIntField(param.getThisObject(), "mGestureStubSize");
				mGestureStubDefaultSize = Math.round(mGestureStubDefaultSize * pct / 100f);
				mGestureStubSize = Math.round(mGestureStubSize * pct / 100f);
				XposedHelpers.setIntField(param.getThisObject(), "mGestureStubDefaultSize", mGestureStubDefaultSize);
				XposedHelpers.setIntField(param.getThisObject(), "mGestureStubSize", mGestureStubSize);
			}
		});

		ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "setSize", int.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				int pct = MainModule.mPrefs.getInt("controls_fsg_width", 100);
				if (pct == 100) return;
				int mGestureStubDefaultSize = XposedHelpers.getIntField(param.getThisObject(), "mGestureStubDefaultSize");
				if ((int)param.getArgs()[0] == mGestureStubDefaultSize) return;
				param.getArgs()[0] = Math.round((int)param.getArgs()[0] * pct / 100f);
			}
		});
	}

	public static void HideNavBarHook(PackageLoadedParam lpparam) {
		ModuleHelper.hookAllConstructors("com.android.systemui.recents.OverviewProxyService", lpparam.getClassLoader(), new MethodHook() {
			@Override
			protected void after(AfterHookCallback param) throws Throwable {
				ArrayList mCallbacks = (ArrayList) ModuleHelper.getObjectFieldByPath(param.getThisObject(), "mCommandQueue.mCallbacks");
				Object callback = mCallbacks.get(mCallbacks.size() - 1);
				ModuleHelper.findAndHookMethod(callback.getClass(), "setWindowState", int.class, int.class, int.class, new MethodHook() {
					@Override
					protected void before(final BeforeHookCallback param) throws Throwable {
						Object GestureObserver = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.miui.systemui.controller.GestureObserver");
						XposedHelpers.setObjectField(GestureObserver, "mGestureLineEnable", true);
					}
				});
			}
		});
		ModuleHelper.hookAllMethods("com.android.systemui.navigationbar.NavigationBarController", lpparam.getClassLoader(), "createNavigationBar", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				if (param.getArgs().length >= 3) {
					param.returnAndSkip(null);
				}
			}
		});
	}

	public static void PowerDoubleTapActionHook(SystemServerLoadedParam lpparam) {
		boolean dtFromVolumeDown = MainModule.mPrefs.getBoolean("controls_volumedowndt_torch");
		final ArrayList<String> doubleTapResons = new ArrayList<String>();
		doubleTapResons.add("double_click_power");
		doubleTapResons.add("power_double_tap");
		doubleTapResons.add("double_click_power_key");
		ModuleHelper.findAndHookMethod("com.miui.server.input.util.ShortCutActionsUtils", lpparam.getClassLoader(), "triggerFunction", String.class, String.class, Bundle.class, boolean.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				boolean dtFromVolumeDownNow = MainModule.mPrefs.getBoolean("controls_volumedowndt_torch");
				if (dtFromVolumeDownNow && "double_click_volume_down".equals(param.getArgs()[1])) {
					param.getArgs()[0] = "turn_on_torch";
				}
				else if (MainModule.mPrefs.getInt("controls_powerdt_action", 1) > 1 && doubleTapResons.contains(param.getArgs()[1])) {
					Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
					GlobalActions.handleAction(mContext, "controls_powerdt", true);
					param.returnAndSkip(true);
				}
			}
		});

		if (dtFromVolumeDown) {
			ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiShortcutTriggerHelper", lpparam.getClassLoader(), "getDoubleVolumeDownKeyFunction", String.class, HookerClassHelper.returnConstant("launch_camera"));
			ModuleHelper.findAndHookMethod("com.android.server.input.shortcut.singlekeyrule.VolumeDownKeyRule", lpparam.getClassLoader(), "isEnableLaunchCamera", HookerClassHelper.returnConstant(true));
		}
	}

	public static void NoFingerprintWakeHook(SystemServerLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.getClassLoader(), "processBackFingerprintDpcenterEvent", KeyEvent.class, boolean.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				boolean isScreenOn = (boolean)param.getArgs()[1];
				if (!isScreenOn) param.returnAndSkip(null);
			}
		});
	}

	public static void AssistGestureActionHook(PackageLoadedParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.systemui.assist.AssistManager", lpparam.getClassLoader(), "startAssist", Bundle.class, new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				Bundle bundle = (Bundle)param.getArgs()[0];
				if (bundle == null || bundle.getInt("triggered_by", 0) != 83 || bundle.getInt("invocation_type", 0) != 1) return;
				Context mContext = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
				String pos = bundle.getInt("inDirection", 0) == 1 ? "right" : "left";
				if (GlobalActions.handleAction(mContext, "controls_fsg_assist_" + pos, false, bundle)) {
					Helpers.performLightVibration(mContext);
					param.returnAndSkip(null);
				}
			}
		});

		ModuleHelper.findAndHookMethod("com.android.systemui.assist.ui.DefaultUiController", lpparam.getClassLoader(), "logInvocationProgressMetrics", float.class, boolean.class, HookerClassHelper.DO_NOTHING);
	}

//	public static void AIButtonHook(PackageLoadedParam lpparam) {
//		ModuleHelper.findAndHookMethod("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "startAiKeyService", String.class, new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				XposedHelpers.log("AIButtonHook", "startAiKeyService: " + param.getArgs()[0]);
//			}
//		});
//	}

//	public static void SwapVolumeKeysHook(PackageLoadedParam lpparam) {
//		ModuleHelper.findAndHookMethod("com.android.server.audio.AudioService", lpparam.getClassLoader(), "adjustSuggestedStreamVolume", int.class, int.class, int.class, String.class, String.class, int.class, new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				//XposedHelpers.log("adjustStreamVolume: " + String.valueOf(param.getArgs()[0]) + ", " + String.valueOf(param.getArgs()[1]) + ", " + String.valueOf(param.getArgs()[2]) + ", " + String.valueOf(param.getArgs()[3]) + ", " + String.valueOf(param.getArgs()[4]) + ", " + String.valueOf(param.getArgs()[5]));
//				if ((Integer)param.getArgs()[0] != 0) try {
//					Context context = (Context)XposedHelpers.getObjectField(param.getThisObject(), "mContext");
//					int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
//					if (rotation == Surface.ROTATION_90) param.getArgs()[0] = -1 * (Integer)param.getArgs()[0];
//				} catch (Throwable t) {
//					XposedHelpers.log(t);
//				}
//			}
//		});
//	}

}