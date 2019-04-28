package name.mikanoshi.customiuizer.mods;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
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

	private static boolean isTrustedNetwork(Context mContext) {
		Set<String> trustedNetworks = Helpers.getSharedStringSetPref(mContext, "pref_key_system_noscreenlock_wifi");
		WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		return Helpers.containsWifiPair(trustedNetworks, wifiManager.getConnectionInfo().getBSSID());
	}

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

//			XposedHelpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "onFinishedGoingToSleep", int.class, boolean.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
//					if (Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_noscreenlock", "1")) == 3)
//					XposedHelpers.callMethod(param.thisObject, "cancelPendingLock");
//				}
//			});

			XposedHelpers.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "doKeyguardLocked", Bundle.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					boolean skip = Helpers.getSharedBoolPref(mContext, "pref_key_system_noscreenlock_skip", false);
					if (!skip) return;
					int opt = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_system_noscreenlock", "1"));
					boolean isTrusted = false;
					if (opt == 4) isTrusted = isTrustedNetwork(mContext);
					if (opt == 2 || opt == 3 && isUnlockedOnce || opt == 4 && isTrusted) {
						param.setResult(null);
						XposedHelpers.callMethod(param.thisObject, "setShowingLocked", false);
						XposedHelpers.callMethod(param.thisObject, "hideLocked");
						XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
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
							NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
							if (netInfo.isConnected() && Integer.parseInt(Helpers.getSharedStringPref(context, "pref_key_system_noscreenlock", "1")) == 4) {
								boolean isShowing = (boolean)XposedHelpers.callMethod(param.thisObject, "isShowing");
								if (!isShowing) return;
								boolean isTrusted = isTrustedNetwork(mContext);
								if (isTrusted) {
									boolean skip = Helpers.getSharedBoolPref(mContext, "pref_key_system_noscreenlock_skip", false);
									if (skip) {
										XposedHelpers.callMethod(param.thisObject, "setShowingLocked", false);
										XposedHelpers.callMethod(param.thisObject, "hideLocked");
										XposedHelpers.callMethod(XposedHelpers.getObjectField(param.thisObject, "mUpdateMonitor"), "reportSuccessfulStrongAuthUnlockAttempt");
									}  else {
										XposedHelpers.callMethod(param.thisObject, "resetStateLocked");
									}
								}
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
					if (opt == 4) isTrusted = isTrustedNetwork(mContext);
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

			XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", lpparam.classLoader, "updateStatusBarWindowBlur", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
					View mThemeBackgroundView = (View)XposedHelpers.getObjectField(param.thisObject, "mThemeBackgroundView");
					if (mThemeBackgroundView != null) mThemeBackgroundView.setAlpha(mThemeBackgroundView.getAlpha() * (int)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCustomOpacityModifier") / 100f);
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
					TextView mMobileType = (TextView)XposedHelpers.getObjectField(param.thisObject, "mMobileType");
					TextView mSignalDualNotchMobileType = (TextView)XposedHelpers.getObjectField(XposedHelpers.getSurroundingThis(param.thisObject), "mSignalDualNotchMobileType");
					try {
						View parent = (View)((View)XposedHelpers.getSurroundingThis(param.thisObject)).getParent().getParent().getParent().getParent();
						if (parent != null && parent.getId() != parent.getResources().getIdentifier("header_content", "id", lpparam.packageName)) {
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
		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader, "onAttachedToWindow", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				Settings.System.putInt(mContext.getContentResolver(), "status_bar_network_speed_interval", Helpers.getSharedIntPref(mContext, "pref_key_system_netspeedinterval", 4) * 1000);
			}
		});
	}

	public static void DetailedNetSpeedHook(LoadPackageParam lpparam) {
		XposedBridge.hookAllConstructors(findClass("com.android.systemui.statusbar.NetworkSpeedView", lpparam.classLoader), new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				TextView meter = (TextView)param.thisObject;
				meter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8.0f);
				meter.setSingleLine(false);
				meter.setLines(2);
				meter.setMaxLines(2);
				meter.setLineSpacing(0, 0.7f);
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
					ConnectivityManager mConnectivityManager = (ConnectivityManager)XposedHelpers.getObjectField(param.thisObject, "mConnectivityManager");
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

				ArrayList<?> sViewList = (ArrayList<?>)XposedHelpers.getObjectField(param.thisObject, "sViewList");
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

				for (Object tv: sViewList)
				if (tv != null) ((TextView)tv).setAlpha(rxSpeed == 0 && txSpeed == 0 ? 0.3f: 1.0f);
			}
		});
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