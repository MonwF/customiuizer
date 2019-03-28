package name.mikanoshi.customiuizer.mods;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

import static java.lang.System.currentTimeMillis;

public class System {

	public static void ScreenAnimHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "initialize", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					XposedHelpers.setObjectField(param.thisObject, "mColorFadeEnabled", true);
					XposedHelpers.setObjectField(param.thisObject, "mColorFadeFadesConfig", true);
				}

				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");

					//ObjectAnimator mColorFadeOnAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mColorFadeOnAnimator");
					//mColorFadeOnAnimator.setDuration(250);
					ObjectAnimator mColorFadeOffAnimator = (ObjectAnimator)XposedHelpers.getObjectField(param.thisObject, "mColorFadeOffAnimator");
					//mColorFadeOffAnimator.setDuration(Helpers.getSharedIntPref(mContext, "pref_key_system_screenanim_duration", 0));
					new Helpers.SharedPrefObserver(mContext, mHandler, "pref_key_system_screenanim_duration", 0) {
						@Override
						public void onChange(String name, int defValue) {
							if (mColorFadeOffAnimator == null) return;
							int val = Helpers.getSharedIntPref(mContext, name, defValue);
							if (val == 0) val = 250;
							mColorFadeOffAnimator.setDuration(val);
						}
					}.onChange(false);
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
					if ((param.args[1]).equals("android.server.power:POWER")) param.setResult(false);
					//XposedBridge.log("wakeUpNoUpdateLocked: " + String.valueOf(param.args[0]) + " | " + String.valueOf(param.args[1]) + " | " + String.valueOf(param.args[2]) + " | " + String.valueOf(param.args[3]) + " | " + String.valueOf(param.args[4]));
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

	public static void NoPasswordHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", XC_MethodReplacement.returnConstant(true));
			XposedHelpers.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor$StrongAuthTracker", lpparam.classLoader, "isUnlockingWithFingerprintAllowed", int.class, XC_MethodReplacement.returnConstant(true));
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

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

	public static void IconLabelToastsHook() {
		try {
			XposedHelpers.findAndHookMethod("android.widget.Toast", null, "makeText", Context.class, Looper.class, CharSequence.class, int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
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

	public static void VolumeCursorHook() {
		try {
			XposedHelpers.findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyDown", int.class, KeyEvent.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					InputMethodService ims = (InputMethodService)param.thisObject;
					int code = (int)param.args[0];
					if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown()) {
						ims.sendDownUpKeyEvents(code == KeyEvent.KEYCODE_VOLUME_UP ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT);
						param.setResult(true);
					}
				}
			});

			XposedHelpers.findAndHookMethod("android.inputmethodservice.InputMethodService", null, "onKeyUp", int.class, KeyEvent.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					InputMethodService ims = (InputMethodService)param.thisObject;
					int code = (int)param.args[0];
					if ((code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN) && ims.isInputViewShown())
					param.setResult(true);
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
						View notification = mStackScroller.getChildAt(i);
						if (notification != null && notification.getClass().getSimpleName().equalsIgnoreCase("ExpandableNotificationRow"))
						XposedHelpers.callMethod(notification, "setSystemExpanded", true);
					}
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}



//	public static void RotationAnimationHook(XC_LoadPackage.LoadPackageParam lpparam) {
//		try {
//			XposedHelpers.findAndHookMethod("com.android.server.wm.ScreenRotationAnimation", lpparam.classLoader, "startAnimation", "android.view.SurfaceControl.Transaction", long.class, float.class, int.class, int.class, boolean.class, int.class, int.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//					int mRotateOption = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_rotateanim", "1"));
//					if (mRotateOption == 2)
//						param.args[2] = 0.01f;
//					else if (mRotateOption == 3) {
//						param.args[6] = mContext.getResources().getIdentifier("screen_rotate_0_exit", "anim", "android");
//						param.args[7] = mContext.getResources().getIdentifier("screen_rotate_0_enter", "anim", "android");
//					}
//				}

//				@Override
//				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//					Context modContext = Helpers.getModuleContext(mContext);
//					int mRotateOption = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_rotateanim", "1"));
//					if (mRotateOption <= 1) return;
//
//					long limit = (long)param.args[1];
//					float scale = (float)param.args[2];
//					int width = (int)param.args[3];
//					int height = (int)param.args[4];
//					int mOriginalWidth = (int)XposedHelpers.getObjectField(param.thisObject, "mOriginalWidth");
//					int mOriginalHeight = (int)XposedHelpers.getObjectField(param.thisObject, "mOriginalHeight");
//
////					XposedBridge.log("limit: " + String.valueOf(limit));
////					XposedBridge.log("scale: " + String.valueOf(scale));
////					XposedBridge.log("width/height: " + String.valueOf(width) + "x" + String.valueOf(height));
////					XposedBridge.log("mOriginalWidth/mOriginalHeight: " + String.valueOf(mOriginalWidth) + "x" + String.valueOf(mOriginalHeight));
//
//					Animation mRotateEnterAnimation = AnimationUtils.loadAnimation(modContext, R.anim.xfade_enter);
//					mRotateEnterAnimation.initialize(width, height, mOriginalWidth, mOriginalHeight);
//					mRotateEnterAnimation.scaleCurrentDuration(scale);
//					mRotateEnterAnimation.restrictDuration(limit);
//
//					Animation mRotateExitAnimation = AnimationUtils.loadAnimation(modContext, R.anim.xfade_exit);
//					mRotateExitAnimation.initialize(width, height, mOriginalWidth, mOriginalHeight);
//					if (mRotateOption == 2)	mRotateExitAnimation.setDuration(10);
//					mRotateExitAnimation.scaleCurrentDuration(scale);
//					mRotateExitAnimation.restrictDuration(limit);
//
//					XposedHelpers.setObjectField(param.thisObject, "mRotateEnterAnimation", mRotateEnterAnimation);
//					XposedHelpers.setObjectField(param.thisObject, "mRotateExitAnimation", mRotateExitAnimation);
//				}
//			});
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

}