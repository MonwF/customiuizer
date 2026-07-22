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

import name.monwf.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam;
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

	public static void PowerKeyHook(SystemServerStartingParam lpparam) {
		ModuleHelper.hookAllMethods("com.android.server.policy.PhoneWindowManager", lpparam.getClassLoader(), "init", new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
									mContext.registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
			}
		});

		ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.getClassLoader(), "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									// Power and volkeys are pressed at the same time
									if (isVolumePressed) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									KeyEvent keyEvent = (KeyEvent)args[0];

									int keycode = keyEvent.getKeyCode();
									int action = keyEvent.getAction();
									int flags = keyEvent.getFlags();

									// Ignore repeated KeyEvents simulated on Power Key Up
									if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									if ((flags & KeyEvent.FLAG_FROM_SYSTEM) != KeyEvent.FLAG_FROM_SYSTEM || keycode != KeyEvent.KEYCODE_POWER) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }

									// Power long press
									final Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
									final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(thisObject, "mPowerManager");
									if (mPowerManager.isInteractive()) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									if (action == KeyEvent.ACTION_DOWN) {
										isPowerPressed = true;
										isPowerLongPressed = false;

										mHandler = (Handler)XposedHelpers.getObjectField(thisObject, "mHandler");

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
										{ skipped = true; result = 0; throwable = null; }
									}

									if (action == KeyEvent.ACTION_UP) {
										if (isPowerPressed && !isPowerLongPressed) try {
											if (isTorchEnabled(mContext)) setTorch(mContext, false);
											if (Helpers.mWakeLock != null && Helpers.mWakeLock.isHeld()) Helpers.mWakeLock.release();
											XposedHelpers.callMethod(mPowerManager, "wakeUp", SystemClock.uptimeMillis());
											{ skipped = true; result = 0; throwable = null; }
										} catch (Throwable t) {
											XposedHelpers.log(t);
										} else if (wasRaise2WakeEnabled && !isTorchEnabled(mContext)) {
											wasRaise2WakeEnabled = false;
											Settings.System.putInt(mContext.getContentResolver(), "pick_up_gesture_wakeup_mode", 1);
										}
										isPowerPressed = false;
										isWaitingForPowerLongPressed = false;
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
	}

	public static void VolumeMediaButtonsHook(SystemServerStartingParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.getClassLoader(), "interceptKeyBeforeQueueing", KeyEvent.class, int.class, new MethodHook() {
			@Override
			@SuppressLint("MissingPermission")
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									// Power and volkeys are pressed at the same time
									if (isPowerPressed) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									final KeyEvent keyEvent = (KeyEvent)args[0];

									int keycode = keyEvent.getKeyCode();
									int action = keyEvent.getAction();
									int flags = keyEvent.getFlags();

									// Ignore repeated KeyEvents simulated on volume Key Up
									if ((flags & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == KeyEvent.FLAG_VIRTUAL_HARD_KEY) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									if ((flags & KeyEvent.FLAG_FROM_SYSTEM) != KeyEvent.FLAG_FROM_SYSTEM || (keycode != KeyEvent.KEYCODE_VOLUME_UP && keycode != KeyEvent.KEYCODE_VOLUME_DOWN)) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }

									// Volume long press
									final Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
									final PowerManager mPowerManager = (PowerManager)XposedHelpers.getObjectField(thisObject, "mPowerManager");
									if (mPowerManager.isInteractive()) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									if (action == KeyEvent.ACTION_DOWN) {
										isVolumePressed = true;
										isVolumeLongPressed = false;

										mHandler = (Handler)XposedHelpers.getObjectField(thisObject, "mHandler");

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
										{ skipped = true; result = 0; throwable = null; }
									}

									if (action == KeyEvent.ACTION_UP) {
										isVolumePressed = false;
										// Kill all callbacks (removing only posted Runnable is not working... no idea)
										if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
										if (!isVolumeLongPressed) {
											AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
											TelecomManager tm = (TelecomManager)mContext.getSystemService(Context.TELECOM_SERVICE);
											WakeLock mBroadcastWakeLock = (WakeLock)XposedHelpers.getObjectField(thisObject, "mBroadcastWakeLock");
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
										{ skipped = true; result = 0; throwable = null; }
										isWaitingForVolumeLongPressed = false;
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
	}

	public static void VolumeMediaPlayerHook(PackageReadyParam lpparam) {
		Class<?> MediaPlayerCls = XposedHelpers.findClass("android.media.MediaPlayer", lpparam.getClassLoader());
		ModuleHelper.findAndHookMethod(MediaPlayerCls, "pause", new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									Context mContext = ModuleHelper.findContext(lpparam);
									int mStreamType = (int)XposedHelpers.findMethodExact(MediaPlayerCls, "getAudioStreamType").invoke(thisObject);
									if (mContext != null && (mStreamType == AudioManager.STREAM_MUSIC || mStreamType == 0x80000000)) {
										Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "SaveLastMusicPausedTime");
										intent.setPackage("com.android.systemui");
										mContext.sendBroadcast(intent);
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
	}

	public static void VolumeCursorHook(PackageReadyParam lpparam) {
		ModuleHelper.findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.getClassLoader(), "onKeyDown", int.class, KeyEvent.class, new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									InputMethodService ims = (InputMethodService)thisObject;
									int code = (int)args[0];
									if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
										String pkgName = Settings.Global.getString(ims.getContentResolver(), Helpers.modulePkg + ".foreground.package");
										if (MainModule.mPrefs.getStringSet("controls_volumecursor_apps").contains(pkgName)) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
										boolean swapDir = MainModule.mPrefs.getBoolean("controls_volumecursor_reverse");
										ims.sendDownUpKeyEvents(code == (swapDir ? KeyEvent.KEYCODE_VOLUME_DOWN : KeyEvent.KEYCODE_VOLUME_UP) ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
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

		ModuleHelper.findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.getClassLoader(), "onKeyUp", int.class, KeyEvent.class, new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									InputMethodService ims = (InputMethodService)thisObject;
									int code = (int)args[0];
									if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
										String pkgName = Settings.Global.getString(ims.getContentResolver(), Helpers.modulePkg + ".foreground.package");
										if (!MainModule.mPrefs.getStringSet("controls_volumecursor_apps").contains(pkgName))
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

	public static void NavBarButtonsHook(PackageReadyParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBarView", lpparam.getClassLoader(), "onFinishInflate", new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									FrameLayout navBar = (FrameLayout) thisObject;
									Context mContext = navBar.getContext();
									ViewGroup mHorizontal = (ViewGroup) XposedHelpers.getObjectField(thisObject, "mHorizontal");
									ViewGroup mVertical = (ViewGroup) XposedHelpers.getObjectField(thisObject, "mVertical");
									int navButtonsId = navBar.getResources().getIdentifier("nav_buttons", "id", lpparam.getPackageName());
									FrameLayout navButtons0 = mHorizontal.findViewById(navButtonsId);
									FrameLayout navButtons90 = mVertical.findViewById(navButtonsId);

									Class<?> kbrCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.phone.MiuiKeyButtonRipple", lpparam.getClassLoader());
									addCustomNavBarKeys(false, mContext, navButtons0, kbrCls);
									addCustomNavBarKeys(true, mContext, navButtons90, kbrCls);
									reposNavBarButtons(navBar);
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
			}
		});

		ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBarTransitions", lpparam.getClassLoader(), "applyDarkIntensity", float.class, new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									FrameLayout navbar = (FrameLayout)XposedHelpers.getObjectField(thisObject, "mView");
									boolean isDark = (float)chain.getArgs().get(0) > 0.5f;
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
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
			}
		});
		ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBarView", lpparam.getClassLoader(), "onConfigurationChanged", Configuration.class,
		new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									FrameLayout navbar = (FrameLayout) thisObject;
									reposNavBarButtons(navbar);
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
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

	public static void NavBarActionsHook(SystemServerStartingParam lpparam) {
		ModuleHelper.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "postKeyLongPress", new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									if (basePWMObject == null) basePWMObject = thisObject;
									if (basePWMContext == null) basePWMContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
									if (markShortcutTriggered == null) markShortcutTriggered = XposedHelpers.findMethodExact("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "markShortcutTriggered");

									int key = (int)args[0];
									if (key == KeyEvent.KEYCODE_BACK && MainModule.mPrefs.getInt("controls_backlong_action", 1) > 1) {
										((Handler)XposedHelpers.getObjectField(thisObject, "mHandler")).postDelayed(mBackLongPressAction, ViewConfiguration.getLongPressTimeout());
										{ skipped = true; result = null; throwable = null; }
									} else if (key == KeyEvent.KEYCODE_HOME && MainModule.mPrefs.getInt("controls_homelong_action", 1) > 1) {
										((Handler)XposedHelpers.getObjectField(thisObject, "mHandler")).postDelayed(mHomeLongPressAction, ViewConfiguration.getLongPressTimeout());
										{ skipped = true; result = null; throwable = null; }
									} else if (key == KeyEvent.KEYCODE_APP_SWITCH && MainModule.mPrefs.getInt("controls_menulong_action", 1) > 1) {
										((Handler)XposedHelpers.getObjectField(thisObject, "mHandler")).postDelayed(mMenuLongPressAction, ViewConfiguration.getLongPressTimeout());
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

		ModuleHelper.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.getClassLoader(), "removeKeyLongPress", new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									int key = (int)args[0];
									if (key == KeyEvent.KEYCODE_BACK)
										((Handler)XposedHelpers.getObjectField(thisObject, "mHandler")).removeCallbacks(mBackLongPressAction);
									else if (key == KeyEvent.KEYCODE_HOME)
										((Handler)XposedHelpers.getObjectField(thisObject, "mHandler")).removeCallbacks(mHomeLongPressAction);
									else if (key == KeyEvent.KEYCODE_APP_SWITCH)
										((Handler)XposedHelpers.getObjectField(thisObject, "mHandler")).removeCallbacks(mMenuLongPressAction);
			
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

	public static void FingerprintHapticSuccessHook(SystemServerStartingParam lpparam) {
		ModuleHelper.hookAllMethods("com.android.server.biometrics.sensors.AuthenticationClient", lpparam.getClassLoader(), "onAuthenticated", new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									boolean mAuthSuccess = XposedHelpers.getBooleanField(thisObject, "mAuthSuccess");
									if (!mAuthSuccess) { return XposedHelpers.throwOrReturn(throwable, result); }
									Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");

									boolean ignoreSystem = MainModule.mPrefs.getBoolean("controls_fingerprintsuccess_ignore");
									int opt = Integer.parseInt(MainModule.mPrefs.getString("controls_fingerprintsuccess", "1"));
									if (opt == 2)
										Helpers.performLightVibration(mContext, ignoreSystem);
									else if (opt == 3)
										Helpers.performStrongVibration(mContext, ignoreSystem);
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
			}
		});
	}

	public static void FingerprintHapticFailureHook(SystemServerStartingParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.biometrics.sensors.AcquisitionClient", lpparam.getClassLoader(), "vibrateError", new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {

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

	public static void FingerprintScreenOnHook(SystemServerStartingParam lpparam) {
		String authClient = "com.android.server.biometrics.sensors.AuthenticationClient";
		ModuleHelper.hookAllMethods(authClient, lpparam.getClassLoader(), "onAuthenticated", new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									boolean mAuthSuccess = XposedHelpers.getBooleanField(thisObject, "mAuthSuccess");
									if (mAuthSuccess) { return XposedHelpers.throwOrReturn(throwable, result); }
									Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
									PowerManager mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
									if (mPowerManager.isInteractive()) { return XposedHelpers.throwOrReturn(throwable, result); }
									if (!GlobalActions.commonSendAction(mContext, "WakeUp")) XposedHelpers.log("FingerprintScreenOnHook", "Failed to wake up device");
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
			}
		});
	}

	public static void BackGestureAreaHeightHook(PackageReadyParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "getGestureStubWindowParam", new MethodHook() {
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

									WindowManager.LayoutParams lp = (WindowManager.LayoutParams)result;
									int pct = MainModule.mPrefs.getInt("controls_fsg_coverage", 60);
									lp.height = Math.round(lp.height / 60.0f * pct);
									result = lp; throwable = null;
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
			}
		});
	}

	public static void BackGestureAreaWidthHook(PackageReadyParam lpparam) {
		ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "initScreenSizeAndDensity", int.class, new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									int pct = MainModule.mPrefs.getInt("controls_fsg_width", 100);
									if (pct == 100) { return XposedHelpers.throwOrReturn(throwable, result); }
									int mGestureStubDefaultSize = XposedHelpers.getIntField(thisObject, "mGestureStubDefaultSize");
									int mGestureStubSize  = XposedHelpers.getIntField(thisObject, "mGestureStubSize");
									mGestureStubDefaultSize = Math.round(mGestureStubDefaultSize * pct / 100f);
									mGestureStubSize = Math.round(mGestureStubSize * pct / 100f);
									XposedHelpers.setIntField(thisObject, "mGestureStubDefaultSize", mGestureStubDefaultSize);
									XposedHelpers.setIntField(thisObject, "mGestureStubSize", mGestureStubSize);
			
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
				return XposedHelpers.throwOrReturn(throwable, result);
			}
		});

		ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.getClassLoader(), "setSize", int.class, new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									int pct = MainModule.mPrefs.getInt("controls_fsg_width", 100);
									if (pct == 100) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									int mGestureStubDefaultSize = XposedHelpers.getIntField(thisObject, "mGestureStubDefaultSize");
									if ((int)args[0] == mGestureStubDefaultSize) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									args[0] = Math.round((int)args[0] * pct / 100f);
			
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

	public static void HideNavBarHook(PackageReadyParam lpparam) {
		ModuleHelper.hookAllConstructors("com.android.systemui.recents.OverviewProxyService", lpparam.getClassLoader(), new MethodHook() {
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
					final Object thisObject = chain.getThisObject();

									ArrayList mCallbacks = (ArrayList) ModuleHelper.getObjectFieldByPath(thisObject, "mCommandQueue.mCallbacks");
									Object callback = mCallbacks.get(mCallbacks.size() - 1);
									ModuleHelper.findAndHookMethod(callback.getClass(), "setWindowState", int.class, int.class, int.class, new MethodHook() {
										@Override
																				public Object intercept(XposedInterface.Chain chain) throws Throwable {
											boolean skipped = false;
											Object result = null;
											Throwable throwable = null;
											Object[] args = XposedHelpers.getArgsArray(chain);
											try {

																							Object GestureObserver = ModuleHelper.getDepInstance(lpparam.getClassLoader(), "com.miui.systemui.controller.GestureObserver");
																							XposedHelpers.setObjectField(GestureObserver, "mGestureLineEnable", true);
										
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
		ModuleHelper.hookAllMethods("com.android.systemui.navigationbar.NavigationBarController", lpparam.getClassLoader(), "createNavigationBar", new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {

									if (args.length >= 3) {
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
	}

	public static void PowerDoubleTapActionHook(SystemServerStartingParam lpparam) {
		boolean dtFromVolumeDown = MainModule.mPrefs.getBoolean("controls_volumedowndt_torch");
		final ArrayList<String> doubleTapResons = new ArrayList<String>();
		doubleTapResons.add("double_click_power");
		doubleTapResons.add("power_double_tap");
		doubleTapResons.add("double_click_power_key");
		ModuleHelper.findAndHookMethod("com.miui.server.input.util.ShortCutActionsUtils", lpparam.getClassLoader(), "triggerFunction", String.class, String.class, Bundle.class, boolean.class, new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									boolean dtFromVolumeDownNow = MainModule.mPrefs.getBoolean("controls_volumedowndt_torch");
									if (dtFromVolumeDownNow && "double_click_volume_down".equals(args[1])) {
										args[0] = "turn_on_torch";
									}
									else if (MainModule.mPrefs.getInt("controls_powerdt_action", 1) > 1 && doubleTapResons.contains(args[1])) {
										Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
										GlobalActions.handleAction(mContext, "controls_powerdt", true);
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

		if (dtFromVolumeDown) {
			ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiShortcutTriggerHelper", lpparam.getClassLoader(), "getDoubleVolumeDownKeyFunction", String.class, HookerClassHelper.returnConstant("launch_camera"));
			ModuleHelper.findAndHookMethod("com.android.server.input.shortcut.singlekeyrule.VolumeDownKeyRule", lpparam.getClassLoader(), "isEnableLaunchCamera", HookerClassHelper.returnConstant(true));
		}
	}

	public static void NoFingerprintWakeHook(SystemServerStartingParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiPhoneWindowManager", lpparam.getClassLoader(), "processBackFingerprintDpcenterEvent", KeyEvent.class, boolean.class, new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {

									boolean isScreenOn = (boolean)args[1];
									if (!isScreenOn) { skipped = true; result = null; throwable = null; }
			
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

	public static void AssistGestureActionHook(PackageReadyParam lpparam) {
		ModuleHelper.findAndHookMethod("com.android.systemui.assist.AssistManager", lpparam.getClassLoader(), "startAssist", Bundle.class, new MethodHook() {
			@Override
						public Object intercept(XposedInterface.Chain chain) throws Throwable {
				boolean skipped = false;
				Object result = null;
				Throwable throwable = null;
				Object[] args = XposedHelpers.getArgsArray(chain);
				try {
					final Object thisObject = chain.getThisObject();

									Bundle bundle = (Bundle)args[0];
									if (bundle == null || bundle.getInt("triggered_by", 0) != 83 || bundle.getInt("invocation_type", 0) != 1) { if (skipped) { return XposedHelpers.throwOrReturn(throwable, result); } return XposedHelpers.proceedOrThrow(chain, args, throwable); }
									Context mContext = (Context)XposedHelpers.getObjectField(thisObject, "mContext");
									String pos = bundle.getInt("inDirection", 0) == 1 ? "right" : "left";
									if (GlobalActions.handleAction(mContext, "controls_fsg_assist_" + pos, false, bundle)) {
										Helpers.performLightVibration(mContext);
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

		ModuleHelper.findAndHookMethod("com.android.systemui.assist.ui.DefaultUiController", lpparam.getClassLoader(), "logInvocationProgressMetrics", float.class, boolean.class, HookerClassHelper.DO_NOTHING);
	}



}