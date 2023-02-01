package name.mikanoshi.customiuizer.mods;

import static java.lang.System.currentTimeMillis;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Application;
import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import name.mikanoshi.customiuizer.GateWaySettings;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

@SuppressWarnings("WeakerAccess")
public class GlobalActions {

	public static Object mStatusBar = null;
	public static final String ACTION_PREFIX = "name.mikanoshi.customiuizer.mods.action.";
	public static final String EVENT_PREFIX = "name.mikanoshi.customiuizer.mods.event.";

	public static boolean handleAction(Context context, String key) {
		return handleAction(context, key, false);
	}

	public static boolean handleAction(Context context, String key, boolean skipLock) {
		if (key == null || key.isEmpty()) return false;
		int action = Helpers.getSharedIntPref(context, key + "_action", 1);
		if (action <= 1) return false;
		if (action >= 85 && action <= 88) {
			if (GlobalActions.isMediaActionsAllowed(context))
				GlobalActions.sendDownUpKeyEvent(context, action, false);
			return true;
		}
		switch (action) {
			case 2: return expandNotifications(context);
			case 3: return expandEQS(context);
			case 4: return lockDevice(context);
			case 5: return goToSleep(context);
			case 6: return takeScreenshot(context);
			case 7: return openRecents(context);
			case 8: return launchAppIntent(context, key, skipLock);
			case 9: return launchShortcutIntent(context, key, skipLock);
			case 10: return toggleThis(context, Helpers.getSharedIntPref(context, key + "_toggle", 0));
			case 11: return switchToPrevApp(context);
			case 12: return openPowerMenu(context);
			case 13: return clearMemory(context);
			case 14: return toggleColorInversion(context);
			case 15: return goBack(context);
			case 16: return simulateMenu(context);
			case 17: return openVolumeDialog(context);
			case 18: return volumeUp(context);
			case 19: return volumeDown(context);
			case 20: return launchActivityIntent(context, key, skipLock);
			case 21: return switchKeyboard(context);
			case 22: return switchOneHandedLeft(context);
			case 23: return switchOneHandedRight(context);
			case 24: return forceClose(context);
			case 25: return scrollToTop(context);
			default: return false;
		}
	}

	public static int getActionResId(int action) {
		switch (action) {
			case 0:
			case 1: return R.string.notselected;
			case 2: return R.string.array_global_actions_notif;
			case 3: return R.string.array_global_actions_eqs;
			case 4: return R.string.array_global_actions_lock;
			case 5: return R.string.array_global_actions_sleep;
			case 6: return R.string.array_global_actions_screenshot;
			case 7: return R.string.array_global_actions_recents;
			case 11: return R.string.array_global_actions_back;
			case 12: return R.string.array_global_actions_powermenu_short;
			case 13: return R.string.array_global_actions_clearmemory;
			case 14: return R.string.array_global_actions_invertcolors;
			case 15: return R.string.array_global_actions_goback;
			case 16: return R.string.array_global_actions_menu;
			case 17: return R.string.array_global_actions_volume;
			case 18: return R.string.array_global_actions_volume_up;
			case 19: return R.string.array_global_actions_volume_down;
			case 21: return R.string.array_global_actions_switchkeyboard;
			case 22: return R.string.array_global_actions_onehanded_left;
			case 23: return R.string.array_global_actions_onehanded_right;
			case 24: return R.string.array_global_actions_forceclose;
			case 25: return R.string.array_global_actions_scrolltotop;
			default: return 0;
		}
	}

	private static final BroadcastReceiver mSBReceiver = new BroadcastReceiver() {
		@SuppressLint("WrongConstant")
		public void onReceive(final Context context, Intent intent) {
			try {
				Resources modRes = Helpers.getModuleRes(context);
				String action = intent.getAction();
				if (action == null) return;

				if (action.equals(ACTION_PREFIX + "RestartSystemUI")) {
					Process.sendSignal(Process.myPid(), Process.SIGNAL_KILL);
				}
//				else if (action.equals(ACTION_PREFIX + "CopyToExternal")) {
//					try {
//						String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + Helpers.externalFolder;
//						new File(dir).mkdirs();
//
//						int copyAction = intent.getIntExtra("action", 0);
//						if (copyAction == 1) {
//							Helpers.copyFile(intent.getStringExtra("from"), dir + Helpers.wallpaperFile);
//							Intent lockIntent = new Intent("miui.intent.action.SET_LOCK_WALLPAPER");
//							lockIntent.setPackage("com.android.thememanager");
//							lockIntent.putExtra("lockWallpaperPath", dir + Helpers.wallpaperFile);
//							context.sendBroadcast(lockIntent);
//						} else if (copyAction == 2) {
//							File xposedVersion = new File(dir + Helpers.versionFile);
//							xposedVersion.createNewFile();
//							try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(xposedVersion, false))) {
//								out.write(intent.getStringExtra("data"));
//							}
//						}
//					} catch (Throwable t) {
//						XposedBridge.log(t);
//					}
//				}
				else if (action.equals(ACTION_PREFIX + "CollectXposedLog")) {
					try {
						String errorLogPath = Helpers.getXposedInstallerErrorLog(context);
						if (errorLogPath != null)
							try (InputStream in = new FileInputStream(errorLogPath)) {
								String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + Helpers.externalFolder;
								new File(dir).mkdirs();
								File sdcardLog = new File(dir + Helpers.logFile);
								sdcardLog.createNewFile();
								try (OutputStream out = new FileOutputStream(sdcardLog, false)) {
									byte[] buf = new byte[1024];
									int len;
									while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
								}
							}
					} catch (Throwable t) {
						XposedBridge.log(t);
					}
				}
				else if (action.equals(ACTION_PREFIX + "ClearMemory")) {
					Intent clearIntent = new Intent("com.android.systemui.taskmanager.Clear");
					clearIntent.putExtra("show_toast", true);
					//clearIntent.putExtra("clean_type", -1);
					context.sendBroadcast(clearIntent);
				}
				else if (action.equals(ACTION_PREFIX + "RestartLauncher")) {
					ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
					XposedHelpers.callMethod(am, "forceStopPackage", "com.miui.home");
				}
				else if (action.equals(ACTION_PREFIX + "ScrollToTop")) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							try {
								Method injectInputEventMethod = InputManager.class.getDeclaredMethod("injectInputEvent", InputEvent.class, int.class);
								Method instanceMethod = InputManager.class.getDeclaredMethod("getInstance");
								InputManager im = (InputManager) instanceMethod.invoke(InputManager.class);
								long uptimeMillis = SystemClock.uptimeMillis();
								MotionEvent swipeDownEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_DOWN, 500, 500, 0);
								swipeDownEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN);
								injectInputEventMethod.invoke(im, swipeDownEvt, 1);
								MotionEvent swipeMoveEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis + 25, MotionEvent.ACTION_MOVE, 500, 240000, 0);
								swipeMoveEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN);
								injectInputEventMethod.invoke(im, swipeMoveEvt, 2);
								MotionEvent swipeUpEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis + 25, MotionEvent.ACTION_UP, 500, 240000, 0);
								swipeUpEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN);
								injectInputEventMethod.invoke(im, swipeUpEvt, 2);
								swipeDownEvt.recycle();
								swipeMoveEvt.recycle();
								swipeUpEvt.recycle();
							}
							catch (Throwable e) {
								Helpers.log("err: " + e);
							}
						}
					}, 100L);
				}

				if (mStatusBar != null) {
					if (action.equals(ACTION_PREFIX + "ExpandNotifications")) try {
						Object mNotificationPanel = XposedHelpers.getObjectField(mStatusBar, "mNotificationPanel");
						boolean mPanelExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mPanelExpanded");
						boolean mQsExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mQsExpanded");
						boolean expandOnly = intent.getBooleanExtra("expand_only", false);
						if (mPanelExpanded) {
							if (!expandOnly)
							if (mQsExpanded)
								XposedHelpers.callMethod(mStatusBar, "closeQs");
							else
								XposedHelpers.callMethod(mStatusBar, "animateCollapsePanels");
						} else {
							XposedHelpers.callMethod(mStatusBar, "animateExpandNotificationsPanel");
						}
					} catch (Throwable t) {
						// Expand only
						long token = Binder.clearCallingIdentity();
						XposedHelpers.callMethod(context.getSystemService("statusbar"), "expandNotificationsPanel");
						Binder.restoreCallingIdentity(token);
					}

					if (action.equals(ACTION_PREFIX + "ExpandSettings")) try {
						boolean forceExpand = intent.getBooleanExtra("forceExpand", false);
						Object mControlCenterController = XposedHelpers.getObjectField(mStatusBar, "mControlCenterController");
						boolean isUseControlCenter = (boolean)XposedHelpers.callMethod(mControlCenterController, "isUseControlCenter");
						if (isUseControlCenter) {
							if (forceExpand || (boolean)XposedHelpers.callMethod(mControlCenterController, "isCollapsed"))
								XposedHelpers.callMethod(mControlCenterController, "openPanel");
							else
								XposedHelpers.callMethod(mControlCenterController, "collapseControlCenter", true);
							return;
						}

						Object mNotificationPanel = XposedHelpers.getObjectField(mStatusBar, "mNotificationPanelViewController");
						boolean mPanelExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mPanelExpanded");
						boolean mQsExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mQsExpanded");
						if (!forceExpand && mPanelExpanded) {
							if (mQsExpanded)
								XposedHelpers.callMethod(mStatusBar, "animateCollapsePanels", 0, false);
							else
								XposedHelpers.callMethod(mNotificationPanel, "setQsExpanded", true);
						} else {
							XposedHelpers.callMethod(mStatusBar, "animateExpandSettingsPanel", (Object)null);
						}
					} catch (Throwable t) {
						// Expand only
						long token = Binder.clearCallingIdentity();
						XposedHelpers.callMethod(context.getSystemService("statusbar"), "expandSettingsPanel");
						Binder.restoreCallingIdentity(token);
					}

					if (action.equals(ACTION_PREFIX + "OpenRecents")) try {
						Object mRecents = XposedHelpers.getObjectField(mStatusBar, "mRecents");
						XposedHelpers.callMethod(mRecents, "toggleRecentApps");
					} catch (Throwable t) {
						// Open only
						Intent recents = new Intent("com.android.systemui.recents.TOGGLE_RECENTS");
						recents.setComponent(new ComponentName ("com.android.systemui", "com.android.systemui.recents.RecentsActivity"));
						recents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(recents);
					}

					if (action.equals(ACTION_PREFIX + "OpenVolumeDialog")) try {
						Object mVolumeComponent = XposedHelpers.getObjectField(mStatusBar, "mVolumeComponent");
						Object mVolumeDialogPlugin = XposedHelpers.getObjectField(mVolumeComponent, "mDialog");
						Object miuiVolumeDialog = XposedHelpers.getObjectField(mVolumeDialogPlugin, "mVolumeDialogImpl");
						if (miuiVolumeDialog == null) {
							Helpers.log("OpenVolumeDialog", "MIUI volume dialog is NULL!");
							return;
						}

						Handler mHandler = (Handler)XposedHelpers.getObjectField(miuiVolumeDialog, "mHandler");
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								boolean mShowing = XposedHelpers.getBooleanField(miuiVolumeDialog, "mShowing");
								boolean mExpanded = XposedHelpers.getBooleanField(miuiVolumeDialog, "mExpanded");

								AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
								boolean isInCall = am.getMode() == AudioManager.MODE_IN_CALL || am.getMode() == AudioManager.MODE_IN_COMMUNICATION;
								if (mShowing) {
									if (mExpanded || isInCall)
										XposedHelpers.callMethod(miuiVolumeDialog, "dismissH", 1);
									else {
										Object mDialogView = XposedHelpers.getObjectField(miuiVolumeDialog, "mDialogView");
										View mExpandButton = (View)XposedHelpers.getObjectField(mDialogView, "mExpandButton");
										View.OnClickListener mClickExpand = (View.OnClickListener)XposedHelpers.getObjectField(mDialogView, "expandListener");
										mClickExpand.onClick(mExpandButton);
									}
								} else {
									Object mController = XposedHelpers.getObjectField(mVolumeDialogPlugin, "mController");
									if (isInCall) {
										XposedHelpers.callMethod(mController, "setActiveStream", 0);
										XposedHelpers.setBooleanField(miuiVolumeDialog, "mNeedReInit", true);
									} else if (am.isMusicActive()) {
										XposedHelpers.callMethod(mController, "setActiveStream", 3);
										XposedHelpers.setBooleanField(miuiVolumeDialog, "mNeedReInit", true);
									}
									XposedHelpers.callMethod(miuiVolumeDialog, "showH", 1);
								}
							}
						});
					} catch (Throwable t) {
						XposedBridge.log(t);
					}

					if (action.equals(ACTION_PREFIX + "ToggleHotspot")) {
						Object mHotspotController = XposedHelpers.callStaticMethod(findClass("com.android.systemui.Dependency", context.getClassLoader()), "get", findClassIfExists("com.android.systemui.statusbar.policy.HotspotController", context.getClassLoader()));
						if (mHotspotController == null) return;
						boolean mHotspotSupported = (boolean)XposedHelpers.callMethod(mHotspotController, "isHotspotSupported");
						if (!mHotspotSupported) return;
						boolean mHotspotEnabled = (boolean)XposedHelpers.callMethod(mHotspotController, "isHotspotEnabled");
						if (mHotspotEnabled)
							Toast.makeText(context, modRes.getString(R.string.toggle_hotspot_off), Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(context, modRes.getString(R.string.toggle_hotspot_on), Toast.LENGTH_SHORT).show();
						XposedHelpers.callMethod(mHotspotController, "setHotspotEnabled", !mHotspotEnabled);
					}

					if (action.equals(ACTION_PREFIX + "ToggleFlashlight")) {
						XposedHelpers.callStaticMethod(findClass("com.miui.systemui.util.CommonUtil", context.getClassLoader()), "toggleTorch");
					}
					// @todo fix gps toggle
//					Object mToggleManager = XposedHelpers.getObjectField(mStatusBar, "mToggleManager");
//					if (mToggleManager == null) return;
//					if (action.equals(ACTION_PREFIX + "ToggleGPS")) {
//						boolean mGpsEnable = (boolean)XposedHelpers.getObjectField(mToggleManager, "mGpsEnable");
//						if (mGpsEnable)
//							Toast.makeText(context, modRes.getString(R.string.toggle_gps_off), Toast.LENGTH_SHORT).show();
//						else
//							Toast.makeText(context, modRes.getString(R.string.toggle_gps_on), Toast.LENGTH_SHORT).show();
//						XposedHelpers.callMethod(mToggleManager, "toggleGps");
//					}
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};

	private static final BroadcastReceiver mGlobalReceiver = new BroadcastReceiver() {
		@SuppressWarnings("ConstantConditions")
		@SuppressLint({"MissingPermission", "WrongConstant", "NewApi"})
		public void onReceive(final Context context, Intent intent) {
			try {

			Resources modRes = Helpers.getModuleRes(context);
			String action = intent.getAction();
			if (action == null) return;
			// Actions
			if (action.equals(ACTION_PREFIX + "FastReboot")) {
				Helpers.proxySystemProperties("set", "ctl.restart", "surfaceflinger", null);
				Helpers.proxySystemProperties("set", "ctl.restart", "zygote", null);
			}
			if (action.equals(ACTION_PREFIX + "RunParasitic")) {
				Intent intent2 = new Intent();
				intent2.setAction("android.intent.action.MAIN");
				intent2.addCategory("org.lsposed.manager.LAUNCH_MANAGER");
				intent2.setClassName("com.android.shell", "com.android.shell.BugreportWarningActivity");
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				context.startActivity(intent2);
			}
			if (action.equals(ACTION_PREFIX + "WakeUp")) {
				XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "wakeUp", SystemClock.uptimeMillis());
			}
			if (action.equals(ACTION_PREFIX + "GoToSleep")) {
				XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 4, 0);
			}
			if (action.equals(ACTION_PREFIX + "LockDevice")) {
				XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 7, 0);
			}
			if (action.equals(ACTION_PREFIX + "TakeScreenshot")) {
				context.sendBroadcast(new Intent("android.intent.action.CAPTURE_SCREENSHOT"));
			}
			/*
			if (action.equals(ACTION_PREFIX + "KillForegroundAppShedule")) {
				if (mHandler == null) return;
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						removeTask(context, true);
					}
				}, 1000);
			}
			*/
//			if (action.equals(ACTION_PREFIX + "KillForegroundApp")) {
//				removeTask(context);
//			}

			if (action.equals(ACTION_PREFIX + "GoBack")) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
					}
				}).start();
			}

			if (action.equals(ACTION_PREFIX + "SwitchToPrevApp")) {
				PackageManager pm = context.getPackageManager();
				Intent intent_home = new Intent(Intent.ACTION_MAIN);
				intent_home.addCategory(Intent.CATEGORY_HOME);
				intent_home.addCategory(Intent.CATEGORY_DEFAULT);
				List<ResolveInfo> launcherList = pm.queryIntentActivities(intent_home, 0);

				ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
				@SuppressWarnings("deprecation")
				List<RecentTaskInfo> rti = am.getRecentTasks(Integer.MAX_VALUE, 0);

				Intent recentIntent;
				for (RecentTaskInfo rtitem: rti) try {
					//noinspection deprecation
					if (am.getRunningTasks(1).get(0).topActivity == rtitem.topActivity) continue;

					boolean isLauncher = false;
					recentIntent = new Intent(rtitem.baseIntent);
					if (rtitem.origActivity != null) recentIntent.setComponent(rtitem.origActivity);
					ComponentName resolvedAct = recentIntent.resolveActivity(pm);

					if (resolvedAct != null)
						for (ResolveInfo launcher: launcherList)
					if (!launcher.activityInfo.packageName.equals("com.android.settings") && launcher.activityInfo.packageName.equals(resolvedAct.getPackageName())) {
						isLauncher = true;
						break;
					}

					if (!isLauncher) {
//						if (Helpers.getHtcHaptic(context)) {
//							Vibrator vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
//							if (XMain.pref.getBoolean("pref_key_controls_longpresshaptic_enable", false))
//								vibe.vibrate(XMain.pref.getInt("pref_key_controls_longpresshaptic", 21));
//							else
//								vibe.vibrate(21);
//						}
						if (rtitem.id >= 0)
							am.moveTaskToFront(rtitem.id, 0);
						else
							context.startActivity(recentIntent);
						break;
					}
				} catch (Throwable t) {
					XposedBridge.log(t);
				}
			}

			if (action.equals(ACTION_PREFIX + "LaunchIntent")) {
				Intent launchIntent = intent.getParcelableExtra("intent");
				if (launchIntent != null) {
					int user = 0;
					if (launchIntent.hasExtra("user")) {
						user = launchIntent.getIntExtra("user", 0);
						launchIntent.removeExtra("user");
					}
					if (user != 0)
						XposedHelpers.callMethod(context, "startActivityAsUser", launchIntent, XposedHelpers.newInstance(UserHandle.class, user));
					else
						context.startActivity(launchIntent);
				}
			}

			if (action.equals(ACTION_PREFIX + "VolumeUp")) {
				AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
			}

			if (action.equals(ACTION_PREFIX + "VolumeDown")) {
				AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 1 << 12 /* FLAG_FROM_KEY */ | AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
			}

			if (action.equals(ACTION_PREFIX + "OpenPowerMenu")) {
				Class<?> clsWMG = XposedHelpers.findClass("android.view.WindowManagerGlobal", null);
				Object wms = XposedHelpers.callStaticMethod(clsWMG, "getWindowManagerService");
				XposedHelpers.callMethod(wms, "showGlobalActions");
			}

			if (action.equals(ACTION_PREFIX + "SwitchKeyboard")) {
				context.sendBroadcast(
					new Intent("com.android.server.InputMethodManagerService.SHOW_INPUT_METHOD_PICKER").setPackage("android")
				);
			}

			if (action.equals(ACTION_PREFIX + "SwitchOneHandedLeft")) {
				Intent handyIntent = new Intent("miui.action.handymode.changemode");
				handyIntent.putExtra("mode", 1);
				context.sendBroadcast(handyIntent);
			}

			if (action.equals(ACTION_PREFIX + "SwitchOneHandedRight")) {
				Intent handyIntent = new Intent("miui.action.handymode.changemode");
				handyIntent.putExtra("mode", 2);
				context.sendBroadcast(handyIntent);
			}

			if (action.equals(ACTION_PREFIX + "ToggleColorInversion")) {
				int opt = Settings.Secure.getInt(context.getContentResolver(), "accessibility_display_inversion_enabled");
				int conflictProp = (int) Helpers.proxySystemProperties("getInt", "ro.df.effect.conflict", 0, null);
				int conflictProp2 = (int) Helpers.proxySystemProperties("getInt", "ro.vendor.df.effect.conflict", 0, null);
				boolean hasConflict = conflictProp == 1 || conflictProp2 == 1;
				Object dfMgr = XposedHelpers.callStaticMethod(XposedHelpers.findClass("miui.hardware.display.DisplayFeatureManager", null), "getInstance");
				if (hasConflict && opt == 0) XposedHelpers.callMethod(dfMgr, "setScreenEffect", 15, 1);
				Settings.Secure.putInt(context.getContentResolver(), "accessibility_display_inversion_enabled", opt == 0 ? 1 : 0);
				if (hasConflict && opt != 0) XposedHelpers.callMethod(dfMgr, "setScreenEffect", 15, 0);
			}

			// Toggles
			if (action.equals(ACTION_PREFIX + "ToggleWiFi")) {
				WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
				if (wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(false);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show();
				} else {
					wifiManager.setWifiEnabled(true);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals(ACTION_PREFIX + "ToggleBluetooth")) {
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.disable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_off), Toast.LENGTH_SHORT).show();
				} else {
					mBluetoothAdapter.enable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals(ACTION_PREFIX + "ToggleNFC")) {
				Class<?> clsNfcAdapter = XposedHelpers.findClass("android.nfc.NfcAdapter", null);
				NfcAdapter mNfcAdapter = (NfcAdapter)XposedHelpers.callStaticMethod(clsNfcAdapter, "getNfcAdapter", context);
				if (mNfcAdapter == null) return;

				Method enableNFC = clsNfcAdapter.getDeclaredMethod("enable");
				Method disableNFC = clsNfcAdapter.getDeclaredMethod("disable");
				enableNFC.setAccessible(true);
				disableNFC.setAccessible(true);

				if (mNfcAdapter.isEnabled()) {
					disableNFC.invoke(mNfcAdapter);
					Toast.makeText(context, modRes.getString(R.string.toggle_nfc_off), Toast.LENGTH_SHORT).show();
				} else {
					enableNFC.invoke(mNfcAdapter);
					Toast.makeText(context, modRes.getString(R.string.toggle_nfc_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals(ACTION_PREFIX + "ToggleSoundProfile")) {
				AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				int currentMode = am.getRingerMode();
				if (currentMode == 0) {
					am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					Toast.makeText(context, modRes.getString(R.string.toggle_sound_vibrate), Toast.LENGTH_SHORT).show();
				} else if (currentMode == 1) {
					am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					Toast.makeText(context, modRes.getString(R.string.toggle_sound_normal), Toast.LENGTH_SHORT).show();
				} else if (currentMode == 2) {
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					Toast.makeText(context, modRes.getString(R.string.toggle_sound_silent), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals(ACTION_PREFIX + "ToggleAutoBrightness")) {
				if (Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals(ACTION_PREFIX + "ToggleAutoRotation")) {
				if (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation());
					Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals(ACTION_PREFIX + "ToggleMobileData")) {
				TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				Method setMTE = TelephonyManager.class.getDeclaredMethod("setDataEnabled", boolean.class);
				@SuppressWarnings("ALL")
				Method getMTE = TelephonyManager.class.getDeclaredMethod("getDataEnabled");
				setMTE.setAccessible(true);
				getMTE.setAccessible(true);

				if ((Boolean)getMTE.invoke(telManager)) {
					setMTE.invoke(telManager, false);
					Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_off), Toast.LENGTH_SHORT).show();
				} else {
					setMTE.invoke(telManager, true);
					Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_on), Toast.LENGTH_SHORT).show();
				}
			}

//			if (action.equals(ACTION_PREFIX + "QueryXposedService")) try {
//				Class<?> smCls = XposedHelpers.findClass("android.os.ServiceManager", null);
//				Object activity_service = XposedHelpers.callStaticMethod(smCls, "getService", "activity");
//
//				Binder heartBeat = new Binder();
//				Parcel data = Parcel.obtain();
//				Parcel reply = Parcel.obtain();
//				data.writeInterfaceToken("LSPosed");
//				data.writeInt(2);
//				data.writeString("lsp-cli:" + UUID.randomUUID().toString());
//				data.writeStrongBinder(heartBeat);
//
//				ArrayList<IBinder> resArr = new ArrayList<IBinder>(1);
//				if ((boolean)XposedHelpers.callMethod(activity_service, "transact", 1598837584, data, reply, 0)) {
//					reply.readException();
//					IBinder serviceBinder = reply.readStrongBinder();
//					if (serviceBinder == null) Helpers.log("XposedService", "binder null"); else {
//						Helpers.log("XposedService", "serviceBinder: " + serviceBinder);
//						//var service = ILSPApplicationService.Stub.asInterface(serviceBinder);
//						//if (service.requestInjectedManagerBinder(resArr) == null) {
//						//	System.out.println("not a manager");
//						//	return null;
//						//}
//						//if (resArr.size() > 0)
//						//	return ILSPManagerService.Stub.asInterface(resArr.get(0));
//						//else
//						//	System.out.println("arr size 0");
//					}
//				} else {
//					Helpers.log("XposedService", "transact fail: " + reply.dataSize());
//				}
//			} catch (Throwable t) {
//				XposedBridge.log(t);
//			}

//			String className = "com.htc.app.HtcShutdownThread";
//			if (Helpers.isLP()) className = "com.android.internal.policy.impl.HtcShutdown.HtcShutdownThread";
//
//			if (action.equals(ACTION_PREFIX + "APMReboot")) {
//				setStaticObjectField(findClass(className, null), "mRebootReason", "oem-11");
//				setStaticBooleanField(findClass(className, null), "mReboot", true);
//				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
//				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
//			}
//			if (action.equals(ACTION_PREFIX + "APMRebootRecovery")) {
//				setStaticObjectField(findClass(className, null), "mRebootReason", "recovery");
//				setStaticBooleanField(findClass(className, null), "mReboot", true);
//				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
//				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
//			}
//			if (action.equals(ACTION_PREFIX + "APMRebootBootloader")) {
//				setStaticObjectField(findClass(className, null), "mRebootReason", "bootloader");
//				setStaticBooleanField(findClass(className, null), "mReboot", true);
//				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
//				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
//			}
//
			} catch(Throwable t) {
				XposedBridge.log(t);
			}
		}
	};
//
//	private static void removeTask(Context context) {
//		try {
//			final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//			@SuppressWarnings("deprecation")
//			final List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
//			final Method removeTask;
//			if (Helpers.isLP2())
//				removeTask = am.getClass().getMethod("removeTask", new Class[] { int.class });
//			else
//				removeTask = am.getClass().getMethod("removeTask", new Class[] { int.class, int.class });
//			final Method forceStopPackage = am.getClass().getMethod("forceStopPackage", new Class[] { String.class });
//			removeTask.setAccessible(true);
//			forceStopPackage.setAccessible(true);
//			String thisPkg = taskInfo.get(0).topActivity.getPackageName();
//
//			boolean isLauncher = false;
//			boolean isAllowed = true;
//			PackageManager pm = context.getPackageManager();
//			Intent intent_home = new Intent(Intent.ACTION_MAIN);
//			intent_home.addCategory(Intent.CATEGORY_HOME);
//			intent_home.addCategory(Intent.CATEGORY_DEFAULT);
//			List<ResolveInfo> launcherList = pm.queryIntentActivities(intent_home, 0);
//
//			for (ResolveInfo launcher: launcherList)
//			if (launcher.activityInfo.packageName.equals(thisPkg)) isLauncher = true;
//			if (thisPkg.equalsIgnoreCase("com.htc.android.worldclock")) isAllowed = false;
//
//			if (isLauncher) {
//				XposedHelpers.callMethod(((PowerManager)context.getSystemService(Context.POWER_SERVICE)), "goToSleep", SystemClock.uptimeMillis());
//			} else if (isAllowed) {
//				// Removes from recents also
//				if (Helpers.isLP2())
//					removeTask.invoke(am, Integer.valueOf(taskInfo.get(0).id));
//				else
//					removeTask.invoke(am, Integer.valueOf(taskInfo.get(0).id), Integer.valueOf(1));
//				// Force closes all package parts
//				forceStopPackage.invoke(am, thisPkg);
//			}
//
//			if (isLauncher || isAllowed) {
//				if (Helpers.getHtcHaptic(context)) {
//					Vibrator vibe = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
//					if (XMain.pref.getBoolean("pref_key_controls_longpresshaptic_enable", false))
//						vibe.vibrate(XMain.pref.getInt("pref_key_controls_longpresshaptic", 30));
//					else
//						vibe.vibrate(30);
//				}
//			}
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

	public static void miuizerHook(LoadPackageParam lpparam) {
		try {
			XposedHelpers.setStaticBooleanField(findClass(Helpers.modulePackage + ".utils.Helpers", lpparam.classLoader), "miuizerModuleActive", true);
//			XposedHelpers.setStaticObjectField(findClass(Helpers.modulePackage + ".utils.Helpers", lpparam.classLoader), "xposedVersion", XposedBridge.getXposedVersion());
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		Helpers.emptyFile(lpparam.appInfo.dataDir + "/files/uncaught_exceptions", false);
	}

	private static int settingsIconResId;
	public static void miuizerSettingsRes() {
		settingsIconResId = MainModule.resHooks.addResource("ic_miuizer_settings", R.drawable.ic_miuizer_settings11);
	}

	public static void miuizerSettings12Hook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.settings.MiuiSettings", lpparam.classLoader, "updateHeaderList", List.class, new MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void after(final MethodHookParam param) throws Throwable {
				if (param.args[0] == null) return;

				Context mContext = ((Activity)param.thisObject).getBaseContext();
				int opt = Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_miuizer_settingsiconpos", "1"));
				if (opt == 0) return;

				Resources modRes = Helpers.getModuleRes(mContext);
				Class<?> headerCls = XposedHelpers.findClassIfExists("com.android.settingslib.miuisettings.preference.PreferenceActivity$Header", lpparam.classLoader);
				if (headerCls == null) return;

				Object header = XposedHelpers.newInstance(headerCls);
				XposedHelpers.setLongField(header, "id", 666);
				XposedHelpers.setObjectField(header, "intent", new Intent().setClassName(Helpers.modulePkg, GateWaySettings.class.getCanonicalName()));
				XposedHelpers.setIntField(header, "iconRes", settingsIconResId);
				XposedHelpers.setObjectField(header, "title", modRes.getString(R.string.app_name));
				Bundle bundle = new Bundle();
				ArrayList<UserHandle> users = new ArrayList<UserHandle>();
				users.add((UserHandle)XposedHelpers.newInstance(UserHandle.class, 0));
				bundle.putParcelableArrayList("header_user", users);
				XposedHelpers.setObjectField(header, "extras", bundle);

				int themes = mContext.getResources().getIdentifier("launcher_settings", "id", mContext.getPackageName());
				int special = mContext.getResources().getIdentifier("other_special_feature_settings", "id", mContext.getPackageName());

				List<Object> headers = (List<Object>)param.args[0];
				int position = 0;
				for (Object head: headers) {
					position++;
					long id = XposedHelpers.getLongField(head, "id");
					if (opt == 1 && id == -1) { headers.add(position - 1, header); return; }
					if (opt == 2 && id == themes) { headers.add(position, header); return; }
					if (opt == 3 && id == special) { headers.add(position, header); return; }
				}
				if (headers.size() > 25)
					headers.add(25, header);
				else
					headers.add(header);
			}
		});
	}

	public static void setupSystemHelpers() {
		Helpers.findAndHookMethod(Application.class, "onCreate", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				final Context ctx = (Application)param.thisObject;
				if (ctx == null || ctx.getPackageName().equals(Helpers.modulePkg)) return;
				if (Thread.getDefaultUncaughtExceptionHandler() != null)
				//noinspection ResultOfMethodCallIgnored
				Helpers.findAndHookMethodSilently(Thread.getDefaultUncaughtExceptionHandler().getClass(), "uncaughtException", Thread.class, Throwable.class, new MethodHook() {
					@Override
					protected void before(MethodHookParam param) throws Throwable {
						if (param.args[1] != null) try {
							Intent intent = new Intent("name.mikanoshi.customiuizer.SAVEEXCEPTION");
							intent.putExtra("throwable", (Throwable)param.args[1]);
							intent.setPackage(Helpers.modulePkg);
							ctx.sendBroadcast(intent);
						} catch (Throwable t) {}
					}
				});
			}
		});
	}

	public static void setupForegroundMonitor(LoadPackageParam lpparam) {
		String methodSystemuiChange = Helpers.isTPlus() ? "updateSystemBarAttributes" : "updateSystemUiVisibilityLw";
		Helpers.hookAllMethods("com.android.server.wm.DisplayPolicy", lpparam.classLoader, methodSystemuiChange, new MethodHook() {
			private String pkgName = null;
			private boolean fullScreen = false;
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (mContext != null) try {
					String focusedApp;
					if (Helpers.isTPlus()) {
						focusedApp = (String) XposedHelpers.getObjectField(param.thisObject, "mFocusedApp");
					}
					else {
						Object mSystemUiControllingWindow = XposedHelpers.getObjectField(param.thisObject, "mSystemUiControllingWindow");
						WindowManager.LayoutParams mAttrs = (WindowManager.LayoutParams)XposedHelpers.getObjectField(mSystemUiControllingWindow, "mAttrs");
						focusedApp = mAttrs.packageName;
					}

					boolean focusAppChanged = false;
					boolean fullscreenChanged = false;
					if (focusedApp != null && !focusedApp.equals(pkgName)) {
						pkgName = focusedApp;
						focusAppChanged = true;
					}
					boolean isFullScreen = XposedHelpers.getBooleanField(param.thisObject, Helpers.isTPlus() ? "mTopIsFullscreen" : "mLastFocusIsFullscreen");
					if (fullScreen != isFullScreen) {
						fullScreen = isFullScreen;
						fullscreenChanged = true;
					}
					if (fullscreenChanged || focusAppChanged) {
						Handler mHandler = (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");
						boolean finalFullscreenChanged = fullscreenChanged;
						boolean finalFocusAppChanged = focusAppChanged;
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (finalFullscreenChanged) {
									Settings.Global.putInt(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.fullscreen", fullScreen ? 1 : 0);
								}
								if (finalFocusAppChanged) {
									Settings.Global.putString(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.package", pkgName);
								}
							}
						});
					}
				} catch (Throwable t) {
					Helpers.log("ForegroundMonitor", t);
				}
			}
		});
	}

	public static void setupGlobalActions(LoadPackageParam lpparam) {
		Helpers.hookAllConstructors("com.android.server.accessibility.AccessibilityManagerService", lpparam.classLoader, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mGlobalContext = (Context)param.args[0];

				IntentFilter intentfilter = new IntentFilter();

				// Actions
				intentfilter.addAction(ACTION_PREFIX + "WakeUp");
				intentfilter.addAction(ACTION_PREFIX + "GoToSleep");
				intentfilter.addAction(ACTION_PREFIX + "LockDevice");
				intentfilter.addAction(ACTION_PREFIX + "TakeScreenshot");
				intentfilter.addAction(ACTION_PREFIX + "KillForegroundApp");
				intentfilter.addAction(ACTION_PREFIX + "SwitchToPrevApp");
				intentfilter.addAction(ACTION_PREFIX + "GoBack");
				intentfilter.addAction(ACTION_PREFIX + "OpenPowerMenu");
				intentfilter.addAction(ACTION_PREFIX + "SwitchKeyboard");
				intentfilter.addAction(ACTION_PREFIX + "SwitchOneHandedLeft");
				intentfilter.addAction(ACTION_PREFIX + "SwitchOneHandedRight");
				intentfilter.addAction(ACTION_PREFIX + "ToggleColorInversion");
				intentfilter.addAction(ACTION_PREFIX + "VolumeUp");
				intentfilter.addAction(ACTION_PREFIX + "VolumeDown");
				intentfilter.addAction(ACTION_PREFIX + "LaunchIntent");
				//intentfilter.addAction(ACTION_PREFIX + "KillForegroundAppShedule");

				// Toggles
				intentfilter.addAction(ACTION_PREFIX + "ToggleWiFi");
				intentfilter.addAction(ACTION_PREFIX + "ToggleBluetooth");
				intentfilter.addAction(ACTION_PREFIX + "ToggleNFC");
				intentfilter.addAction(ACTION_PREFIX + "ToggleSoundProfile");
				intentfilter.addAction(ACTION_PREFIX + "ToggleAutoBrightness");
				intentfilter.addAction(ACTION_PREFIX + "ToggleAutoRotation");
				intentfilter.addAction(ACTION_PREFIX + "ToggleMobileData");

				// Tools
				intentfilter.addAction(ACTION_PREFIX + "FastReboot");
//				intentfilter.addAction(ACTION_PREFIX + "RunParasitic");
				//intentfilter.addAction(ACTION_PREFIX + "QueryXposedService");

				mGlobalContext.registerReceiver(mGlobalReceiver, intentfilter);
			}
		});

		Helpers.hookAllMethods("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, "initInternal", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction(ACTION_PREFIX + "SimulateMenu");
				intentfilter.addAction(ACTION_PREFIX + "ForceClose");
				intentfilter.addAction(ACTION_PREFIX + "SaveLastMusicPausedTime");
				final Object thisObject = param.thisObject;
				mContext.registerReceiver(new BroadcastReceiver() {
					public void onReceive(final Context context, Intent intent) {
						String action = intent.getAction();
						if (action == null) return;

						if (action.equals(ACTION_PREFIX + "SimulateMenu")) try {
							Field fRequestShowMenu = findField(thisObject.getClass().getSuperclass(), "mRequestShowMenu");
							fRequestShowMenu.setAccessible(true);
							fRequestShowMenu.set(thisObject, true);
							Method markShortcutTriggered = findMethodExact(thisObject.getClass().getSuperclass(), "markShortcutTriggered");
							markShortcutTriggered.setAccessible(true);
							markShortcutTriggered.invoke(thisObject);
							Method injectEvent = findMethodExact(thisObject.getClass().getSuperclass(), "injectEvent", int.class);
							injectEvent.setAccessible(true);
							injectEvent.invoke(thisObject, 82);
						} catch (Throwable t1) {
							try {
								Handler mHandler = (Handler)XposedHelpers.getObjectField(thisObject, "mHandler");
								mHandler.sendMessageDelayed(mHandler.obtainMessage(1, "show_menu"), ViewConfiguration.getLongPressTimeout());
							} catch (Throwable t2) {
								XposedBridge.log(t2);
							}
						}

						if (action.equals(ACTION_PREFIX + "ForceClose")) try {
							Method closeApp = findMethodExact(thisObject.getClass().getSuperclass(), "closeApp", boolean.class);
							closeApp.setAccessible(true);
							closeApp.invoke(thisObject, false);
						} catch (Throwable t) {
							XposedBridge.log(t);
						}

						if (action.equals(ACTION_PREFIX + "SaveLastMusicPausedTime")) {
							Settings.System.putLong(context.getContentResolver(), "last_music_paused_time", currentTimeMillis());
						}
					}
				}, intentfilter);
			}
		});
	}

	public static void setupStatusBar(LoadPackageParam lpparam) {
		Class<?> StatusBarClass;
		if (Helpers.isTPlus()) {
			StatusBarClass = findClassIfExists("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);
		}
		else {
			StatusBarClass = findClassIfExists("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader);
		}
		if (StatusBarClass == null) return;
		Helpers.findAndHookMethod(StatusBarClass, "start", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				mStatusBar = param.thisObject;
				Context mStatusBarContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();

				intentfilter.addAction(ACTION_PREFIX + "ExpandNotifications");
				intentfilter.addAction(ACTION_PREFIX + "ExpandSettings");
				intentfilter.addAction(ACTION_PREFIX + "OpenRecents");
				intentfilter.addAction(ACTION_PREFIX + "OpenVolumeDialog");

				intentfilter.addAction(ACTION_PREFIX + "ToggleGPS");
				intentfilter.addAction(ACTION_PREFIX + "ToggleHotspot");
				intentfilter.addAction(ACTION_PREFIX + "ToggleFlashlight");
				intentfilter.addAction(ACTION_PREFIX + "ShowQuickRecents");
				intentfilter.addAction(ACTION_PREFIX + "HideQuickRecents");

				intentfilter.addAction(ACTION_PREFIX + "ClearMemory");
				intentfilter.addAction(ACTION_PREFIX + "CollectXposedLog");
				intentfilter.addAction(ACTION_PREFIX + "RestartSystemUI");
				intentfilter.addAction(ACTION_PREFIX + "RestartLauncher");
//				intentfilter.addAction(ACTION_PREFIX + "CopyToExternal");

				intentfilter.addAction(ACTION_PREFIX + "ScrollToTop");

				mStatusBarContext.registerReceiver(mSBReceiver, intentfilter);
			}
		});
	}

	// Actions
	public static boolean expandNotifications(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "ExpandNotifications"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean expandEQS(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "ExpandSettings"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean lockDevice(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "LockDevice"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean wakeUp(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "WakeUp"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean goToSleep(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "GoToSleep"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	enum IntentType {
		APP, ACTIVITY, SHORTCUT
	}

	public static Intent getIntent(Context context, String pref, IntentType intentType, boolean skipLock) {
		try {
			if (intentType == IntentType.APP) pref += "_app";
			else if (intentType == IntentType.ACTIVITY) pref += "_activity";
			else if (intentType == IntentType.SHORTCUT) pref += "_shortcut_intent";

			String prefValue = Helpers.getSharedStringPref(context, pref, null);
			if (prefValue == null) return null;

			Intent intent = new Intent();
			if (intentType == IntentType.SHORTCUT) {
				intent = Intent.parseUri(prefValue, 0);
			} else {
				String[] pkgAppArray = prefValue.split("\\|");
				if (pkgAppArray.length < 2) return null;
				ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
				intent.setComponent(name);
				int user = Helpers.getSharedIntPref(context, pref + "_user", 0);
				if (user != 0) intent.putExtra("user", user);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			if (intentType == IntentType.APP) {
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
			}

			if (skipLock) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.putExtra("ShowCameraWhenLocked", true);
				intent.putExtra("StartActivityWhenLocked", true);
			}

			return intent;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return null;
		}
	}

	public static boolean takeScreenshot(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "TakeScreenshot"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

//	public static boolean killForegroundApp(Context context) {
//		try {
//			context.sendBroadcast(new Intent(ACTION_PREFIX + "KillForegroundApp"));
//			return true;
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//			return false;
//		}
//	}

	public static boolean simulateMenu(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "SimulateMenu"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean forceClose(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "ForceClose"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean openRecents(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "OpenRecents"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean launchAppIntent(Context context, String key, boolean skipLock) {
		return launchIntent(context, getIntent(context, key, IntentType.APP, skipLock));
	}

	public static boolean launchActivityIntent(Context context, String key, boolean skipLock) {
		return launchIntent(context, getIntent(context, key, IntentType.ACTIVITY, skipLock));
	}

	public static boolean launchShortcutIntent(Context context, String key, boolean skipLock) {
		return launchIntent(context, getIntent(context, key, IntentType.SHORTCUT, skipLock));
	}

	public static boolean launchIntent(Context context, Intent intent) {
		if (intent == null) return false;
		Intent bIntent = new Intent(ACTION_PREFIX + "LaunchIntent");
		bIntent.putExtra("intent", intent);
		context.sendBroadcast(bIntent);
		return true;
	}

	public static boolean openVolumeDialog(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "OpenVolumeDialog"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean volumeUp(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "VolumeUp"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean volumeDown(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "VolumeDown"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean goBack(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "GoBack"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean switchToPrevApp(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "SwitchToPrevApp"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean scrollToTop(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "ScrollToTop"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean openPowerMenu(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "OpenPowerMenu"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean switchKeyboard(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "SwitchKeyboard"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean switchOneHandedLeft(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "SwitchOneHandedLeft"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean switchOneHandedRight(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "SwitchOneHandedRight"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean toggleColorInversion(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "ToggleColorInversion"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean clearMemory(Context context) {
		try {
			context.sendBroadcast(new Intent(ACTION_PREFIX + "ClearMemory"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

//	public static boolean showQuickRecents(Context context) {
//		try {
//			context.sendBroadcast(new Intent(ACTION_PREFIX + "ShowQuickRecents"));
//			return true;
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//			return false;
//		}
//	}
//
//	public static boolean hideQuickRecents(Context context) {
//		try {
//			context.sendBroadcast(new Intent(ACTION_PREFIX + "HideQuickRecents"));
//			return true;
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//			return false;
//		}
//	}

	public static boolean toggleThis(Context context, int what) {
		try {
			String whatStr;
			switch (what) {
				case 1: whatStr = "WiFi"; break;
				case 2: whatStr = "Bluetooth"; break;
				case 3: whatStr = "GPS"; break;
				case 4: whatStr = "NFC"; break;
				case 5: whatStr = "SoundProfile"; break;
				case 6: whatStr = "AutoBrightness"; break;
				case 7: whatStr = "AutoRotation"; break;
				case 8: whatStr = "Flashlight"; break;
				case 9: whatStr = "MobileData"; break;
				case 10: whatStr = "Hotspot"; break;
				default: return false;
			}
			context.sendBroadcast(new Intent(ACTION_PREFIX + "Toggle" + whatStr));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

//	public static ColorFilter createColorFilter(boolean fromModule) {
//		int brightness = 0;
//		int saturation = 0;
//		int hue = 0;
//
//		if (Helpers.isLP()) {
//			brightness = 100;
//			saturation = -100;
//			hue = 0;
//		} else if (fromModule) {
//			if (XMain.pref != null) {
//				brightness = XMain.pref.getInt("pref_key_colorfilter_brightValue", 100) - 100;
//				saturation = XMain.pref.getInt("pref_key_colorfilter_satValue", 100) - 100;
//				hue = XMain.pref.getInt("pref_key_colorfilter_hueValue", 180) - 180;
//			}
//		} else {
//			if (Helpers.prefs != null) {
//				brightness = Helpers.prefs.getInt("pref_key_colorfilter_brightValue", 100) - 100;
//				saturation = Helpers.prefs.getInt("pref_key_colorfilter_satValue", 100) - 100;
//				hue = Helpers.prefs.getInt("pref_key_colorfilter_hueValue", 180) - 180;
//			}
//		}
//
//		if (brightness == 0 && saturation == 0 && hue == 0)
//			return null;
//		else if (brightness == 100 && saturation == -100)
//			return ColorFilterGenerator.adjustColor(100, 100, -100, -180);
//		else
//			return ColorFilterGenerator.adjustColor(brightness, 0, saturation, hue);
//	}
//
//	public static void sendMediaButton(Context mContext, KeyEvent keyEvent) {
//		try {
//			if (Build.VERSION.SDK_INT >= 19) {
//				AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
//				if (mContext != null) am.dispatchMediaKeyEvent(keyEvent);
//			} else {
//				// Get binder from ServiceManager.checkService(String)
//				IBinder iBinder  = (IBinder) Class.forName("android.os.ServiceManager").getDeclaredMethod("checkService", String.class).invoke(null, Context.AUDIO_SERVICE);
//				// Get audioService from IAudioService.Stub.asInterface(IBinder)
//				Object audioService  = Class.forName("android.media.IAudioService$Stub").getDeclaredMethod("asInterface", IBinder.class).invoke(null, iBinder);
//				// Dispatch keyEvent using IAudioService.dispatchMediaKeyEvent(KeyEvent)
//				Class.forName("android.media.IAudioService").getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class).invoke(audioService, keyEvent);
//			}
//		}  catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

	public static boolean isMediaActionsAllowed(Context mContext) {
		AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		boolean isMusicActive = am.isMusicActive();
		boolean isMusicActiveRemotely  = (Boolean)XposedHelpers.callMethod(am, "isMusicActiveRemotely");
		boolean isAllowed = isMusicActive || isMusicActiveRemotely;
		if (!isAllowed) {
			long mCurrentTime = currentTimeMillis();
			long mLastPauseTime = Settings.System.getLong(mContext.getContentResolver(), "last_music_paused_time", mCurrentTime);
			if (mCurrentTime - mLastPauseTime < 10 * 60 * 1000) isAllowed = true;
		}
		return isAllowed;
	}

	public static void sendDownUpKeyEvent(Context mContext, int keyCode, boolean vibrate) {
		AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));

		if (vibrate && Helpers.getSharedBoolPref(mContext, "pref_key_controls_volumemedia_vibrate", true))
		Helpers.performStrongVibration(mContext, Helpers.getSharedBoolPref(mContext, "pref_key_controls_volumemedia_vibrate_ignore", false));
	}
}