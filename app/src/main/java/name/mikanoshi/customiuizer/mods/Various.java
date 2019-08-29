package name.mikanoshi.customiuizer.mods;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import miui.app.AlertDialog;

import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

import static java.lang.System.currentTimeMillis;

public class Various {

	public static void AppInfoHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.miui.appmanager.AMAppInfomationActivity", lpparam.classLoader, "onLoadFinished", new MethodHook() {
			@Override
			@SuppressWarnings("deprecation")
			protected void after(final MethodHookParam param) throws Throwable {
				final PreferenceActivity act = (PreferenceActivity)param.thisObject;
				Field piField = XposedHelpers.findFirstFieldByExactType(act.getClass(), PackageInfo.class);
				final PackageInfo mPackageInfo = (PackageInfo)piField.get(act);
				final Resources modRes = Helpers.getModuleRes(act);
				Method[] addPref = XposedHelpers.findMethodsByExactParameters(act.getClass(), void.class, String.class, String.class, String.class);
				if (mPackageInfo == null || addPref.length == 0) {
					Helpers.log("AppInfoHook", "Unable to find field/class/method in SecurityCenter to hook");
					return;
				} else {
					addPref[0].setAccessible(true);
				}
				addPref[0].invoke(act, "apk_filename", modRes.getString(R.string.appdetails_apk_file), mPackageInfo.applicationInfo.sourceDir);
				addPref[0].invoke(act, "data_path", modRes.getString(R.string.appdetails_data_path), mPackageInfo.applicationInfo.dataDir);
				addPref[0].invoke(act, "app_uid", modRes.getString(R.string.appdetails_app_uid), String.valueOf(mPackageInfo.applicationInfo.uid));
				addPref[0].invoke(act, "target_sdk", modRes.getString(R.string.appdetails_sdk), String.valueOf(mPackageInfo.applicationInfo.targetSdkVersion));
				addPref[0].invoke(act, "open_in_store", modRes.getString(R.string.appdetails_playstore), "");
				addPref[0].invoke(act, "launch_app", modRes.getString(R.string.appdetails_launch), "");

				act.findPreference("apk_filename").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(preference.getTitle(), mPackageInfo.applicationInfo.sourceDir));
						Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
						return true;
					}
				});

				act.findPreference("data_path").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(preference.getTitle(), mPackageInfo.applicationInfo.dataDir));
						Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
						return true;
					}
				});

				act.findPreference("open_in_store").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						try {
							Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mPackageInfo.packageName));
							launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							act.startActivity(launchIntent);
						} catch (android.content.ActivityNotFoundException anfe) {
							Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + mPackageInfo.packageName));
							launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							act.startActivity(launchIntent);
						}
						return true;
					}
				});

				act.findPreference("launch_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent launchIntent = act.getPackageManager().getLaunchIntentForPackage(mPackageInfo.packageName);
						if (launchIntent == null) {
							Toast.makeText(act, modRes.getString(R.string.appdetails_nolaunch), Toast.LENGTH_SHORT).show();
						} else {
							launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
							act.startActivity(launchIntent);
						}
						return true;
					}
				});
			}
		});
	}

	public static void AppsDefaultSortHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				Bundle bundle = (Bundle)param.args[0];
				if (bundle == null) bundle = new Bundle();
				int order = Integer.parseInt(Helpers.getSharedStringPref((Context)param.thisObject, "pref_key_various_appsort", "0"));
				bundle.putInt("current_sory_type", order); // Xiaomi noob typos :)
				bundle.putInt("current_sort_type", order); // Future proof, they may fix it someday :D
				param.args[0] = bundle;
			}
		});

//		Helpers.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.classLoader, "onSaveInstanceState", Bundle.class, new MethodHook() {
//			@Override
//			protected void after(final MethodHookParam param) throws Throwable {
//				Bundle bundle = (Bundle)param.args[0];
//				if (bundle == null) bundle = new Bundle();
//				bundle.putInt("current_sory_type", 1); // Xiaomi noob typos :)
//				bundle.putInt("current_sort_type", 1); // Future proof, they may fix it someday :D
//				Helpers.log("onSaveInstanceState: " + String.valueOf(bundle));
//			}
//		});
	}

	private static void setAppState(final Activity act, String pkgName, MenuItem item, boolean enable) {
		try {
			PackageManager pm = act.getPackageManager();
			pm.setApplicationEnabledSetting(pkgName, enable ? PackageManager.COMPONENT_ENABLED_STATE_DEFAULT : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
			int state = pm.getApplicationEnabledSetting(pkgName);
			boolean isEnabledOrDefault = (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
			if ((enable && isEnabledOrDefault) || (!enable && !isEnabledOrDefault)) {
				item.setTitle(act.getResources().getIdentifier(enable ? "app_manager_disable_text" : "app_manager_enable_text", "string", "com.miui.securitycenter"));
				Toast.makeText(act, act.getResources().getIdentifier(enable ? "app_manager_enabled" : "app_manager_disabled", "string", "com.miui.securitycenter"), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(act, Helpers.getModuleRes(act).getString(R.string.disable_app_fail), Toast.LENGTH_LONG).show();
			}
			new Handler().postDelayed(act::invalidateOptionsMenu, 500);
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void AppsDisableHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "onCreateOptionsMenu", Menu.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				Menu menu = (Menu)param.args[0];
				MenuItem dis = menu.add(0, 666, 1, act.getResources().getIdentifier("app_manager_disable_text", "string", lpparam.packageName));
				dis.setIcon(act.getResources().getIdentifier("action_button_stop", "drawable", lpparam.packageName));
				dis.setEnabled(true);
				dis.setShowAsAction(1);
				//XposedHelpers.setAdditionalInstanceField(param.thisObject, "mDisableButton", dis);

				PackageManager pm = act.getPackageManager();
				Field piField = XposedHelpers.findFirstFieldByExactType(act.getClass(), PackageInfo.class);
				PackageInfo mPackageInfo = (PackageInfo)piField.get(act);
				ApplicationInfo appInfo = pm.getApplicationInfo(mPackageInfo.packageName, PackageManager.GET_META_DATA);
				boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				boolean isUpdatedSystem = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

				dis.setTitle(act.getResources().getIdentifier(appInfo.enabled ? "app_manager_disable_text" : "app_manager_enable_text", "string", lpparam.packageName));

				if (!appInfo.enabled || (isSystem && !isUpdatedSystem)) {
					MenuItem item = menu.findItem(2);
					if (item != null) item.setVisible(false);
				}
			}
		});

		Helpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "onOptionsItemSelected", MenuItem.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				MenuItem item = (MenuItem)param.args[0];
				if (item == null || item.getItemId() != 666) return;

				Activity act = (Activity)param.thisObject;
				Resources modRes = Helpers.getModuleRes(act);
				Field piField = XposedHelpers.findFirstFieldByExactType(act.getClass(), PackageInfo.class);
				PackageInfo mPackageInfo = (PackageInfo)piField.get(act);
				if ("com.android.settings".equals(mPackageInfo.packageName)) {
					Toast.makeText(act, modRes.getString(R.string.disable_app_settings), Toast.LENGTH_SHORT).show();
					return;
				}

				PackageManager pm = act.getPackageManager();
				ApplicationInfo appInfo = pm.getApplicationInfo(mPackageInfo.packageName, PackageManager.GET_META_DATA);
				boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
				int state = pm.getApplicationEnabledSetting(mPackageInfo.packageName);
				boolean isEnabledOrDefault = (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
				if (isEnabledOrDefault) {
					if (isSystem) {
						String title = modRes.getString(R.string.disable_app_title);
						String text = modRes.getString(R.string.disable_app_text);
						new AlertDialog.Builder(act).setTitle(title).setMessage(text).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								setAppState(act, mPackageInfo.packageName, item, false);
							}
						}).setNegativeButton(android.R.string.cancel, null).show();
					} else setAppState(act, mPackageInfo.packageName, item, false);
				} else setAppState(act, mPackageInfo.packageName, item, true);
				param.setResult(true);
			}
		});
	}

	public static void AlarmCompatHook() {
		Helpers.findAndHookMethod("android.provider.Settings$System", null, "getStringForUser", ContentResolver.class, String.class, int.class, new MethodHook() {
			@Override
			protected void before(final MethodHookParam param) throws Throwable {
				ContentResolver resolver = (ContentResolver)param.args[0];
				String pkgName = (String)XposedHelpers.callMethod(resolver, "getPackageName");
				String key = (String)param.args[1];
				if ("next_alarm_formatted".equals(key) && MainModule.mPrefs.getStringSet("various_alarmcompat_apps").contains(pkgName))
				param.args[1] = "next_alarm_clock_formatted";
			}
		});
	}

	public static void AlarmCompatServiceHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.AlarmManagerService", lpparam.classLoader, "onBootPhase", int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				if ((int)param.args[0] != 500 /*PHASE_SYSTEM_SERVICES_READY*/) return;

				Context mContext = (Context)XposedHelpers.callMethod(param.thisObject, "getContext");
				if (mContext == null) {
					Helpers.log("AlarmCompatServiceHook", "Context is NULL");
					return;
				}
				ContentResolver resolver = mContext.getContentResolver();
				ContentObserver alarmObserver = new ContentObserver(new Handler()) {
					@Override
					public void onChange(boolean selfChange) {
						if (selfChange) return;
						XposedHelpers.setAdditionalInstanceField(param.thisObject, "mNextAlarmTime", Helpers.getNextMIUIAlarmTime(mContext));
					}
				};
				alarmObserver.onChange(false);
				resolver.registerContentObserver(Settings.System.getUriFor("next_alarm_clock_formatted"), false, alarmObserver);
			}
		});

		Helpers.findAndHookMethod("com.android.server.AlarmManagerService", lpparam.classLoader, "getNextAlarmClockImpl", int.class, new MethodHook() {
			@Override
			protected void after(final MethodHookParam param) throws Throwable {
				Context mContext = (Context)XposedHelpers.callMethod(param.thisObject, "getContext");
				String pkgName = mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
				Object mNextAlarmTime = XposedHelpers.getAdditionalInstanceField(param.thisObject, "mNextAlarmTime");
				if (mNextAlarmTime != null && MainModule.mPrefs.getStringSet("various_alarmcompat_apps").contains(pkgName))
				param.setResult((long)mNextAlarmTime == 0 ? null : new AlarmManager.AlarmClockInfo((long)mNextAlarmTime, null));
			}
		});
	}

	public static void ShowCallUIHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.incallui.InCallPresenter", lpparam.classLoader, "startUi", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				if (!(boolean)param.getResult() || !"INCOMING".equals(param.args[0].toString())) return;
				try {
					boolean isCarMode = (boolean)XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.incallui.util.Utils.CarMode", lpparam.classLoader), "isCarMode");
					if (isCarMode) return;
				} catch (Throwable t) {
					XposedBridge.log(t);
				}

				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
				if (MainModule.mPrefs.getStringAsInt("various_showcallui", 0) == 1)
				if (Settings.Global.getInt(mContext.getContentResolver(), Helpers.modulePkg + ".foreground.fullscreen", 0) == 1) return;

				XposedHelpers.callMethod(param.thisObject, "showInCall", false, false);
				Object mStatusBarNotifier = XposedHelpers.getObjectField(param.thisObject, "mStatusBarNotifier");
				if (mStatusBarNotifier != null) XposedHelpers.callMethod(mStatusBarNotifier, "cancelInCall");
				param.setResult(true);
			}
		});
	}

	public static void InCallBrightnessHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.incallui.InCallActivity", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;

				int opt = Integer.parseInt(Helpers.getSharedStringPref(act, "pref_key_various_calluibright_type", "0"));
				if (opt == 1 || opt == 2) {
					Object presenter = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.incallui.InCallPresenter", lpparam.classLoader), "getInstance");
					if (presenter == null) {
						Helpers.log("InCallBrightnessHook", "InCallPresenter is null");
						return;
					}

					String state = String.valueOf(XposedHelpers.callMethod(presenter, "getInCallState"));
					if (opt == 1 && !"INCOMING".equals(state)) return;
					else if (opt == 2 && !"OUTGOING".equals(state) && !"PENDING_OUTGOING".equals(state)) return;
				}

				boolean checkNight = Helpers.getSharedBoolPref(act, "pref_key_various_calluibright_night", false);
				if (checkNight) {
					int start_hour = Helpers.getSharedIntPref(act, "pref_key_various_calluibright_night_start_hour", 0);
					int start_minute = Helpers.getSharedIntPref(act, "pref_key_various_calluibright_night_start_minute", 0);
					int end_hour = Helpers.getSharedIntPref(act, "pref_key_various_calluibright_night_end_hour", 0);
					int end_minute = Helpers.getSharedIntPref(act, "pref_key_various_calluibright_night_end_minute", 0);

					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
					Date start = formatter.parse(start_hour + ":" + start_minute);
					Date end = formatter.parse(end_hour + ":" + end_minute);
					Date now = formatter.parse(formatter.format(new Date()));

					boolean isNight = start.before(end) ? now.after(start) && now.before(end) : now.before(end) || now.after(start);
					if (isNight) return;
				}

				WindowManager.LayoutParams params = act.getWindow().getAttributes();
				int val = Helpers.getSharedIntPref(act, "pref_key_various_calluibright_val", 0);
				if (val == 0) return;
				params.screenBrightness = val / 100f;
				act.getWindow().setAttributes(params);
			}
		});
	}

	public static void CallReminderHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.server.telecom.MiuiMissedCallNotifierImpl", lpparam.classLoader, "startRepeatReminder", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.setResult(null);
				if ((int)XposedHelpers.callMethod(param.thisObject, "getRepeatTimes") > 0) {
					Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
					AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
					int interval = Helpers.getSharedIntPref(mContext, "pref_key_various_callreminder_interval", 5);
					PendingIntent repeatAlarmPendingIntent = (PendingIntent)XposedHelpers.callMethod(param.thisObject, "getRepeatAlarmPendingIntent");
					alarmManager.cancel(repeatAlarmPendingIntent);
					alarmManager.setExact(AlarmManager.RTC_WAKEUP, currentTimeMillis() + interval * 60 * 1000, repeatAlarmPendingIntent);
				}
			}
		});

		Helpers.findAndHookMethod("com.android.server.telecom.MiuiMissedCallNotifierImpl", lpparam.classLoader, "onRepeatReminder", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.setResult(null);
				Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");

				Object remindTime = XposedHelpers.callMethod(param.thisObject, "getMinAndIncreaseMissCallRemindTime", true);
				int repeatTimes = remindTime == null ? -1 : (int)XposedHelpers.callMethod(param.thisObject, "getRepeatTimes") - XposedHelpers.getIntField(remindTime, "remindTimes");
				if (repeatTimes >= 0) {
					String uriStr = Helpers.getSharedStringPref(mContext, "pref_key_various_callreminder_sound", "");
					if (!TextUtils.isEmpty(uriStr)) {
						Uri uri = Uri.parse(uriStr);
						Ringtone ringtone = RingtoneManager.getRingtone(mContext, uri);
						if (ringtone != null) {
							ringtone.setAudioAttributes(new AudioAttributes.Builder().setLegacyStreamType(5).build());
							ringtone.play();
						}
					}

					Helpers.performCustomVibration(mContext,
						Integer.parseInt(Helpers.getSharedStringPref(mContext, "pref_key_various_callreminder_vibration", "0")),
						Helpers.getSharedStringPref(mContext, "pref_key_various_callreminder_vibration_own", "")
					);
				}

				if (repeatTimes > 0) {
					int interval = Helpers.getSharedIntPref(mContext, "pref_key_various_callreminder_interval", 5);
					((AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE)).setExact(AlarmManager.RTC_WAKEUP, currentTimeMillis() + interval * 60 * 1000, (PendingIntent)XposedHelpers.callMethod(param.thisObject, "getRepeatAlarmPendingIntent"));
				} else {
					XposedHelpers.callMethod(param.thisObject, "cancelRepeatReminder");
				}
			}
		});
	}

//	public static void LargeCallerPhotoHook(LoadPackageParam lpparam) {
//		Helpers.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.classLoader, "setCallCardImage", Drawable.class, boolean.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				param.args[1] = true;
//			}
//		});
//
//		Helpers.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.classLoader, "showBigAvatar", boolean.class, Drawable.class, new MethodHook() {
//			@Override
//			protected void before(final MethodHookParam param) throws Throwable {
//				//Helpers.log("showBigAvatar: " + String.valueOf(param.args[0]) + " | " + String.valueOf(param.args[1]));
//				if (param.args[1] == null)
//					param.setResult(null);
//				else
//					param.args[0] = true;
//			}
//		});
//	}

}