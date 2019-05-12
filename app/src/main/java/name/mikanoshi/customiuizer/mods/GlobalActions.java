package name.mikanoshi.customiuizer.mods;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import miui.os.SystemProperties;
import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static java.lang.System.currentTimeMillis;

@SuppressWarnings("WeakerAccess")
public class GlobalActions {

	public static Object mStatusBar = null;
//	public static FloatingSelector floatSel = null;

	public static boolean handleAction(int action, int extraLaunch, int extraToggle, Context helperContext) {
		switch (action) {
			case 2: return expandNotifications(helperContext);
			case 3: return expandEQS(helperContext);
			case 4: return lockDevice(helperContext);
			case 5: return goToSleep(helperContext);
			case 6: return takeScreenshot(helperContext);
			case 7: return openRecents(helperContext);
			case 8: return launchApp(helperContext, extraLaunch);
			case 9: return launchShortcut(helperContext, extraLaunch);
			case 10: return toggleThis(helperContext, extraToggle);
			case 11: return switchToPrevApp(helperContext);
			case 12: return openPowerMenu(helperContext);
			default: return false;
		}
	}

	private static BroadcastReceiver mSBReceiver = new BroadcastReceiver() {
		@SuppressLint("WrongConstant")
		public void onReceive(final Context context, Intent intent) {
			try {
				Resources modRes = Helpers.getModuleRes(context);
				String action = intent.getAction();
				if (action == null) return;

//				if (action.equals("name.mikanoshi.customiuizer.mods.action.ShowQuickRecents")) {
//					if (floatSel == null) floatSel = new FloatingSelector(context);
//					floatSel.show();
//				} else if (action.equals("name.mikanoshi.customiuizer.mods.action.HideQuickRecents")) {
//					if (floatSel != null) floatSel.hide();
//				}

				if (mStatusBar != null) {
					if (action.equals("name.mikanoshi.customiuizer.mods.action.ExpandNotifications")) {
						try {
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
					}

					if (action.equals("name.mikanoshi.customiuizer.mods.action.ExpandSettings")) {
						try {
							Object mNotificationPanel = XposedHelpers.getObjectField(mStatusBar, "mNotificationPanel");
							boolean mPanelExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mPanelExpanded");
							boolean mQsExpanded = (boolean)XposedHelpers.getObjectField(mNotificationPanel, "mQsExpanded");
							if (mPanelExpanded) {
								if (mQsExpanded)
									XposedHelpers.callMethod(mStatusBar, "animateCollapsePanels");
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
					}

					if (action.equals("name.mikanoshi.customiuizer.mods.action.OpenRecents")) {
						try {
							Object mRecents = XposedHelpers.getObjectField(mStatusBar, "mRecents");
							XposedHelpers.callMethod(mRecents, "toggleRecentApps");
						} catch (Throwable t) {
							// Open only
							Intent recents = new Intent("com.android.systemui.recents.TOGGLE_RECENTS");
							recents.setComponent(new ComponentName ("com.android.systemui", "com.android.systemui.recents.RecentsActivity"));
							recents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(recents);
						}
					}

					Object mToggleManager = XposedHelpers.getObjectField(mStatusBar, "mToggleManager");
					if (mToggleManager == null) return;
					if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleGPS")) {
						boolean mGpsEnable = (boolean)XposedHelpers.getObjectField(mToggleManager, "mGpsEnable");
						if (mGpsEnable)
							Toast.makeText(context, modRes.getString(R.string.toggle_gps_off), Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(context, modRes.getString(R.string.toggle_gps_on), Toast.LENGTH_SHORT).show();
						XposedHelpers.callMethod(mToggleManager, "toggleGps");
					}
					if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleFlashlight")) {
						boolean mTorchEnable = (boolean)XposedHelpers.getObjectField(mToggleManager, "mTorchEnable");
						if (mTorchEnable)
							Toast.makeText(context, modRes.getString(R.string.toggle_flash_off), Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(context, modRes.getString(R.string.toggle_flash_on), Toast.LENGTH_SHORT).show();
						XposedHelpers.callMethod(mToggleManager, "toggleTorch");
					}
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}
	};

	private static BroadcastReceiver mGlobalReceiver = new BroadcastReceiver() {
		@SuppressLint({"MissingPermission", "WrongConstant"})
		public void onReceive(final Context context, Intent intent) {
			try {

			Resources modRes = Helpers.getModuleRes(context);
			String action = intent.getAction();
			if (action == null) return;
			// Actions
			if (action.equals("name.mikanoshi.customiuizer.mods.action.FastReboot")) {
				SystemProperties.set("ctl.restart", "surfaceflinger");
				SystemProperties.set("ctl.restart", "zygote");
			}
			if (action.equals("name.mikanoshi.customiuizer.mods.action.GoToSleep")) {
				XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 7, 0);
			}
			if (action.equals("name.mikanoshi.customiuizer.mods.action.LockDevice")) {
				XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 7, 0);
				Class<?> clsWMG = XposedHelpers.findClass("android.view.WindowManagerGlobal", null);
				Object wms = XposedHelpers.callStaticMethod(clsWMG, "getWindowManagerService");
				XposedHelpers.callMethod(wms, "lockNow", (Object)null);
			}
			if (action.equals("name.mikanoshi.customiuizer.mods.action.TakeScreenshot")) {
				context.sendBroadcast(new Intent("android.intent.action.CAPTURE_SCREENSHOT"));
			}
			/*
			if (action.equals("name.mikanoshi.customiuizer.mods.action.killForegroundAppShedule")) {
				if (mHandler == null) return;
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						removeTask(context, true);
					}
				}, 1000);
			}
			*/
//			if (action.equals("name.mikanoshi.customiuizer.mods.action.killForegroundApp")) {
//				removeTask(context);
//			}
//
//			if (action.equals("name.mikanoshi.customiuizer.mods.action.SimulateMenu")) {
//				new Thread(new Runnable() {
//					public void run() {
//						Instrumentation inst = new Instrumentation();
//						inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
//					}
//				}).start();
//			}
//
			if (action.equals("name.mikanoshi.customiuizer.mods.action.SwitchToPrevApp")) {
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

			if (action.equals("name.mikanoshi.customiuizer.mods.action.OpenPowerMenu")) {
				Class<?> clsWMG = XposedHelpers.findClass("android.view.WindowManagerGlobal", null);
				Object wms = XposedHelpers.callStaticMethod(clsWMG, "getWindowManagerService");
				XposedHelpers.callMethod(wms, "showGlobalActions");
			}

			// Toggles
			if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleWiFi")) {
				WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
				if (wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(false);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show();
				} else {
					wifiManager.setWifiEnabled(true);
					Toast.makeText(context, modRes.getString(R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleBluetooth")) {
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.disable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_off), Toast.LENGTH_SHORT).show();
				} else {
					mBluetoothAdapter.enable();
					Toast.makeText(context, modRes.getString(R.string.toggle_bt_on), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleNFC")) {
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
			if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleSoundProfile")) {
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
			if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleAutoBrightness")) {
				if (Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autobright_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleAutoRotation")) {
				if (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
					Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show();
				} else {
					Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
					Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show();
				}
			}
			if (action.equals("name.mikanoshi.customiuizer.mods.action.ToggleMobileData")) {
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

//			String className = "com.htc.app.HtcShutdownThread";
//			if (Helpers.isLP()) className = "com.android.internal.policy.impl.HtcShutdown.HtcShutdownThread";
//
//			if (action.equals("name.mikanoshi.customiuizer.mods.action.APMReboot")) {
//				setStaticObjectField(findClass(className, null), "mRebootReason", "oem-11");
//				setStaticBooleanField(findClass(className, null), "mReboot", true);
//				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
//				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
//			}
//			if (action.equals("name.mikanoshi.customiuizer.mods.action.APMRebootRecovery")) {
//				setStaticObjectField(findClass(className, null), "mRebootReason", "recovery");
//				setStaticBooleanField(findClass(className, null), "mReboot", true);
//				setStaticBooleanField(findClass(className, null), "mRebootSafeMode", false);
//				callStaticMethod(findClass(className, null), "shutdownInner", context, false);
//			}
//			if (action.equals("name.mikanoshi.customiuizer.mods.action.APMRebootBootloader")) {
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

	public static void miuizerInit(LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod(Helpers.modulePkg + ".MainFragment", lpparam.classLoader, "onActivityCreated", Bundle.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					XposedHelpers.setBooleanField(param.thisObject, "miuizerModuleActive", true);
				}
			});
			Helpers.emptyFile(lpparam.appInfo.dataDir + "/files/uncaught_exceptions", false);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static int settingsIconResId;
	public static void miuizerSettingsResInit(XC_InitPackageResources.InitPackageResourcesParam resparam) {
		try {
			XModuleResources modRes = XModuleResources.createInstance(MainModule.MODULE_PATH, resparam.res);
			settingsIconResId = resparam.res.addResource(modRes, R.drawable.ic_miuizer_settings);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void miuizerSettingsInit(LoadPackageParam lpparam) {
		try {
			findAndHookMethod("com.android.settings.MiuiSettings", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				@SuppressWarnings("unchecked")
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					Map<String, Integer> map = (Map<String, Integer>)XposedHelpers.getStaticObjectField(findClass("com.android.settings.MiuiSettings", lpparam.classLoader), "CATEGORY_MAP");
					map.put("com.android.settings.category.customiuizer", settingsIconResId);
					XposedHelpers.setStaticObjectField(findClass("com.android.settings.MiuiSettings", lpparam.classLoader), "CATEGORY_MAP", map);
					//XposedBridge.log(map.toString());
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
/*
		findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, Map.class, boolean.class, String.class, String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[1];
				boolean bool = (boolean)param.args[2];
				String str1 = (String)param.args[3];
				String str2 = (String)param.args[4];

				XposedBridge.log("Tile with Map:");
				XposedBridge.log("Map:");
				for (Map.Entry<Pair<String, String>, Object> entry: map.entrySet())
				XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
				XposedBridge.log("- - - - - - - - - -");
				XposedBridge.log(String.valueOf(bool));
				XposedBridge.log("Str1: " + str1);
				XposedBridge.log("Str2: " + str2);
				XposedBridge.log("- - - - - - - - - -");
			}
		});

		findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, UserHandle.class, String.class, Map.class, String.class, ArrayList.class, boolean.class, boolean.class, String.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				UserHandle uh = (UserHandle)param.args[1];
				String str1 = (String)param.args[2];
				Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[3];
				String str2 = (String)param.args[4];
				ArrayList list = (ArrayList)param.args[5];
				boolean bool1 = (boolean)param.args[6];
				boolean bool2 = (boolean)param.args[7];
				String str3 = (String)param.args[8];

				XposedBridge.log("Tile with ArrayList:");
				XposedBridge.log(uh.toString());
				XposedBridge.log("Str1: " + str1);
				XposedBridge.log("Map:");
				for (Map.Entry<Pair<String, String>, Object> entry: map.entrySet())
				XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
				XposedBridge.log("- - - - - - - - - -");
				XposedBridge.log("Str2: " + str2);
				XposedBridge.log("ArrayList:");
				for (Object entry: list)
				XposedBridge.log(entry.toString());
				XposedBridge.log("- - - - - - - - - -");
				XposedBridge.log(String.valueOf(bool1));
				XposedBridge.log(String.valueOf(bool2));
				XposedBridge.log("Str3: " + str3);
				XposedBridge.log("= = = = = = = = = =");
			}
		});

		findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, UserHandle.class, Intent.class, Map.class, String.class, List.class, boolean.class, boolean.class, boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				UserHandle uh = (UserHandle)param.args[1];
				Intent intent = (Intent)param.args[2];
				Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[3];
				String str = (String)param.args[4];
				List list = (List)param.args[5];
				boolean bool1 = (boolean)param.args[6];
				boolean bool2 = (boolean)param.args[7];
				boolean bool3 = (boolean)param.args[8];
				boolean bool4 = (boolean)param.args[9];

				XposedBridge.log("Tile with List:");
				XposedBridge.log(uh.toString());
				XposedBridge.log(intent.toString());
				XposedBridge.log("Map:");
				for (Map.Entry<Pair<String, String>, Object> entry: map.entrySet())
				XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
				XposedBridge.log("- - - - - - - - - -");
				XposedBridge.log("Str: " + str);
				XposedBridge.log("List:");
				for (Object entry: list)
				XposedBridge.log(entry.toString());
				XposedBridge.log("- - - - - - - - - -");
				XposedBridge.log(String.valueOf(bool1));
				XposedBridge.log(String.valueOf(bool2));
				XposedBridge.log(String.valueOf(bool3));
				XposedBridge.log(String.valueOf(bool4));
				XposedBridge.log("= = = = = = = = = =");
			}
		});

		findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, "com.android.settingslib.drawer.Tile", ActivityInfo.class, ApplicationInfo.class, PackageManager.class, Map.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				ApplicationInfo ai = (ApplicationInfo)param.args[3];
				if (ai.packageName.equalsIgnoreCase(modulePkg) && !(boolean)XposedHelpers.callMethod(ai, "isSystemApp")) ai.flags |= 1;
			}

			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Object tile = param.args[1];
				ActivityInfo ai = (ActivityInfo)param.args[2];
				ApplicationInfo ai2 = (ApplicationInfo)param.args[3];
				Map<Pair<String, String>, Object> map = (Map<Pair<String, String>, Object>)param.args[5];
				boolean bool = (boolean)param.args[6];

				if (ai2.packageName.equalsIgnoreCase(modulePkg)) {
					XposedBridge.log("Result: " + String.valueOf((boolean)param.getResult()));
					XposedBridge.log("Title: " + XposedHelpers.getObjectField(tile, "title"));
					XposedBridge.log("Summary: " + XposedHelpers.getObjectField(tile, "summary"));
					XposedBridge.log("Cat: " + XposedHelpers.getObjectField(tile, "category"));
					XposedBridge.log("Key: " + XposedHelpers.getObjectField(tile, "key"));
					XposedBridge.log("Priority: " + String.valueOf((int)XposedHelpers.getObjectField(tile, "priority")));
					XposedBridge.log("cCN: " + ((ArrayList)XposedHelpers.getObjectField(tile, "cCN")).toString());
					XposedBridge.log("cCM: " + String.valueOf((boolean)XposedHelpers.getObjectField(tile, "cCM")));
					XposedBridge.log("Icon: " + ((Icon)XposedHelpers.getObjectField(tile, "icon")).toString());
					XposedBridge.log(ai.toString());
					XposedBridge.log(ai2.toString());
					//XposedBridge.log("Map:");
					//for (Map.Entry<Pair<String, String>, Object> entry : map.entrySet())
					//XposedBridge.log(entry.getKey().first + " = " + entry.getKey().second);
					XposedBridge.log(String.valueOf(bool));
					XposedBridge.log("= = = = = = = = = =");
				}
			}
		});

		findAndHookMethod("com.android.settingslib.drawer.l", lpparam.classLoader, "a", Context.class, Map.class, "com.android.settingslib.drawer.Tile", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
				Map map = (Map)param.args[1];
				Object tile = (Object)param.args[2];

				XposedBridge.log("Tile:");
				XposedBridge.log(tile.toString());
				XposedBridge.log(XposedHelpers.getObjectField(tile, "title").toString());
				XposedBridge.log(XposedHelpers.getObjectField(tile, "summary").toString());
				XposedBridge.log(String.valueOf((int)XposedHelpers.getObjectField(tile, "priority")));
				XposedBridge.log(map.toString());
				XposedBridge.log("- - - - - - - - - -");
			}
		});
*/
	}
	
	public static void setupUnhandledCatcher() {
		try {
			findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					try {
						final Context ctx = (Application)param.thisObject;
						if (ctx == null || ctx.getPackageName().equals("name.mikanoshi.customiuizer")) return;
						Class<?> clsUEH = Thread.getDefaultUncaughtExceptionHandler().getClass();
						XposedHelpers.findAndHookMethod(clsUEH, "uncaughtException", Thread.class, Throwable.class, new XC_MethodHook() {
							@Override
							protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
								if (param.args[1] != null) {
									Intent intent = new Intent("name.mikanoshi.customiuizer.SAVEEXCEPTION");
									intent.putExtra("throwable", (Throwable)param.args[1]);
									ctx.sendBroadcast(intent);
								}
							}
						});
					} catch (Throwable t) {}
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void setupGlobalActions(LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllConstructors(XposedHelpers.findClass("com.android.server.accessibility.AccessibilityManagerService", lpparam.classLoader), new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mGlobalContext = (Context)param.args[0];

					IntentFilter intentfilter = new IntentFilter();

					// Actions
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.GoToSleep");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.LockDevice");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.TakeScreenshot");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.killForegroundApp");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.SwitchToPrevApp");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.OpenPowerMenu");
					//intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.killForegroundAppShedule");
					//intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.SimulateMenu");

					// Toggles
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleWiFi");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleBluetooth");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleNFC");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleSoundProfile");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleAutoBrightness");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleAutoRotation");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleMobileData");

					//APM
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.FastReboot");

					mGlobalContext.registerReceiver(mGlobalReceiver, intentfilter);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

		try {
			XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager", lpparam.classLoader), "init", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					IntentFilter intentfilter = new IntentFilter();
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.SaveLastMusicPausedTime");
					intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.RestartLauncher");
					mContext.registerReceiver(new BroadcastReceiver() {
						public void onReceive(final Context context, Intent intent) {
							String action = intent.getAction();
							if (action == null) return;

							try {
								if (action.equals("name.mikanoshi.customiuizer.mods.action.SaveLastMusicPausedTime")) {
									Settings.System.putLong(context.getContentResolver(), "last_music_paused_time", currentTimeMillis());
								} else if (action.equals("name.mikanoshi.customiuizer.mods.action.RestartLauncher")) {
									ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
									Method forceStopPackage = am.getClass().getMethod("forceStopPackage", String.class);
									forceStopPackage.setAccessible(true);
									forceStopPackage.invoke(am, "com.miui.home");
								}
							} catch (Throwable t) {
								XposedBridge.log(t);
							}
						}
					}, intentfilter);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}

//		try {
//			XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActivityStackSupervisor", lpparam.classLoader), "getComponentRestrictionForCallingPackage", new XC_MethodHook() {
//				@Override
//				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//					ActivityInfo ai = (ActivityInfo)param.args[0];
//					if (ai != null && ai.name.equals("com.miui.privacyapps.ui.PrivacyAppsOperationTutorialActivity")) param.setResult(0);
//				}
//			});
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
	}

	public static void setupStatusBar(LoadPackageParam lpparam) {
		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "start", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				mStatusBar = param.thisObject;
				Context mStatusBarContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				IntentFilter intentfilter = new IntentFilter();

				intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ExpandNotifications");
				intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ExpandSettings");
				intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.OpenRecents");

				intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleGPS");
				intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ToggleFlashlight");
				intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.ShowQuickRecents");
				intentfilter.addAction("name.mikanoshi.customiuizer.mods.action.HideQuickRecents");
				mStatusBarContext.registerReceiver(mSBReceiver, intentfilter);
			}
		});

//		XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.HeaderView", lpparam.classLoader, "onFinishInflate", new XC_MethodHook() {
//			@Override
//			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				TextView mClock = (TextView)XposedHelpers.getObjectField(param.thisObject, "mClock");
//				RelativeLayout.LayoutParams clp = (RelativeLayout.LayoutParams)mClock.getLayoutParams();
//				clp.height = 0;
//				mClock.setLayoutParams(clp);
//
//				RelativeLayout heaverView = (RelativeLayout)param.thisObject;
//				TextView temp = new TextView(heaverView.getContext());
//				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//				lp.addRule(RelativeLayout.ABOVE, heaverView.getContext().getResources().getIdentifier("date_time", "id", "com.android.systemui"));
//				lp.addRule(RelativeLayout.ALIGN_LEFT);
//				lp.setMarginStart(heaverView.getContext().getResources().getDimensionPixelSize(heaverView.getContext().getResources().getIdentifier("expanded_notification_weather_temperature_right", "dimen", "com.android.systemui")));
//				temp.setLayoutParams(lp);
//				temp.setText("CPU 666° Battery 999° PCB 333°");
//				temp.setTextAppearance(heaverView.getContext().getResources().getIdentifier("TextAppearance.StatusBar.Expanded.Weather", "style", "com.android.systemui"));
//				heaverView.setGravity(Gravity.TOP);
//				heaverView.addView(temp);
//			}
//		});
//
//		XposedHelpers.findAndHookMethod("com.android.systemui.CustomizedUtils", lpparam.classLoader, "getNotchExpandedHeaderViewHeight", Context.class, int.class, new XC_MethodHook() {
//			@Override
//			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				param.setResult((int)param.getResult() + 65);
//			}
//		});
	}

	// Actions
	public static boolean expandNotifications(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.ExpandNotifications"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean expandEQS(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.ExpandSettings"));
			return false;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean lockDevice(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.LockDevice"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean goToSleep(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.GoToSleep"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean launchApp(Context context, int action) {
		try {
			String pkgAppName = null;
			switch (action) {
				case 1: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_launcher_swipedown_app", null); break;
				case 2: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_launcher_swipedown2_app", null); break;
				case 3: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_launcher_swipeup_app", null); break;
				case 4: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_launcher_swipeup2_app", null); break;
				case 5: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_launcher_swiperight_app", null); break;
				case 6: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_launcher_swipeleft_app", null); break;
				case 7: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_launcher_shake_app", null); break;
				case 8: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_controls_navbarleft_app", null); break;
				case 9: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_controls_navbarleftlong_app", null); break;
				case 10: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_controls_navbarright_app", null); break;
				case 11: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_controls_navbarrightlong_app", null); break;
				case 12: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_controls_fingerprint1_app", null); break;
				case 13: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_controls_fingerprint2_app", null); break;
				case 14: pkgAppName = Helpers.getSharedStringPref(context, "pref_key_controls_fingerprintlong_app", null); break;
			}

			if (pkgAppName != null) {
				String[] pkgAppArray = pkgAppName.split("\\|");

				ComponentName name = new ComponentName(pkgAppArray[0], pkgAppArray[1]);
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setComponent(name);
				context.startActivity(intent);

				return true;
			} else return false;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean launchShortcut(Context context, int action) {
		try {
			String intentString = null;
			switch (action) {
				case 1: intentString = Helpers.getSharedStringPref(context, "pref_key_launcher_swipedown_shortcut_intent", null); break;
				case 2: intentString = Helpers.getSharedStringPref(context, "pref_key_launcher_swipedown2_shortcut_intent", null); break;
				case 3: intentString = Helpers.getSharedStringPref(context, "pref_key_launcher_swipeup_shortcut_intent", null); break;
				case 4: intentString = Helpers.getSharedStringPref(context, "pref_key_launcher_swipeup2_shortcut_intent", null); break;
				case 5: intentString = Helpers.getSharedStringPref(context, "pref_key_launcher_swiperight_shortcut_intent", null); break;
				case 6: intentString = Helpers.getSharedStringPref(context, "pref_key_launcher_swipeleft_shortcut_intent", null); break;
				case 7: intentString = Helpers.getSharedStringPref(context, "pref_key_launcher_shake_shortcut_intent", null); break;
				case 8: intentString = Helpers.getSharedStringPref(context, "pref_key_controls_navbarleft_shortcut_intent", null); break;
				case 9: intentString = Helpers.getSharedStringPref(context, "pref_key_controls_navbarleftlong_shortcut_intent", null); break;
				case 10: intentString = Helpers.getSharedStringPref(context, "pref_key_controls_navbarright_shortcut_intent", null); break;
				case 11: intentString = Helpers.getSharedStringPref(context, "pref_key_controls_navbarrightlong_shortcut_intent", null); break;
				case 12: intentString = Helpers.getSharedStringPref(context, "pref_key_controls_fingerprint1_shortcut_intent", null); break;
				case 13: intentString = Helpers.getSharedStringPref(context, "pref_key_controls_fingerprint2_shortcut_intent", null); break;
				case 14: intentString = Helpers.getSharedStringPref(context, "pref_key_controls_fingerprintlong_shortcut_intent", null); break;
			}

			if (intentString != null) {
				Intent shortcutIntent = Intent.parseUri(intentString, 0);
				shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				context.startActivity(shortcutIntent);
				return true;
			} else return false;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean takeScreenshot(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.TakeScreenshot"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

//	public static boolean killForegroundApp(Context context) {
//		try {
//			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.killForegroundApp"));
//			return true;
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//			return false;
//		}
//	}
//
//	public static boolean simulateMenu(Context context) {
//		try {
//			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.SimulateMenu"));
//			return true;
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//			return false;
//		}
//	}
//
	public static boolean openRecents(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.OpenRecents"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean switchToPrevApp(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.SwitchToPrevApp"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

	public static boolean openPowerMenu(Context context) {
		try {
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.OpenPowerMenu"));
			return true;
		} catch (Throwable t) {
			XposedBridge.log(t);
			return false;
		}
	}

//	public static boolean showQuickRecents(Context context) {
//		try {
//			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.ShowQuickRecents"));
//			return true;
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//			return false;
//		}
//	}
//
//	public static boolean hideQuickRecents(Context context) {
//		try {
//			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.HideQuickRecents"));
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
				default: return false;
			}
			context.sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.Toggle" + whatStr));
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
//
//	public static void collapseDrawer(Context mContext) {
//		try {
//			Object sbservice = mContext.getSystemService("statusbar");
//			Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
//			Method hidesb;
//			if (Build.VERSION.SDK_INT >= 17) {
//				hidesb = statusbarManager.getMethod("collapsePanels");
//			} else {
//				hidesb = statusbarManager.getMethod("collapse");
//			}
//			hidesb.setAccessible(true);
//			hidesb.invoke(sbservice);
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}
//
//	public static void dismissKeyguard() {
//		Object amn = XposedHelpers.callStaticMethod(findClass("android.app.ActivityManagerNative", null), "getDefault");
//		if (Helpers.isLP())
//			XposedHelpers.callMethod(amn, "keyguardWaitingForActivityDrawn");
//		else
//			XposedHelpers.callMethod(amn, "dismissKeyguardOnNextActivity");
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
		Helpers.performVibration(mContext);
	}
}