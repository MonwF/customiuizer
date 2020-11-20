package name.mikanoshi.customiuizer.mods;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Fragment;
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
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import miui.app.ActionBar;
import miui.app.AlertDialog;

import name.mikanoshi.customiuizer.MainModule;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static java.lang.System.currentTimeMillis;

public class Various {

	public static PackageInfo mLastPackageInfo;
	public static void AppInfoHook(LoadPackageParam lpparam) {
		if (Helpers.is12())
			Helpers.findAndHookMethod("com.miui.appmanager.AMAppInfomationActivity", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
				@Override
				protected void after(final MethodHookParam param) throws Throwable {
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable() {
						@Override
						public void run() {
							final Activity act = (Activity)param.thisObject;
							Fragment frag = act.getFragmentManager().findFragmentById(android.R.id.content);
							if (frag == null) {
								Helpers.log("AppInfoHook", "Unable to find fragment");
								return;
							}

							final Resources modRes;
							try {
								modRes = Helpers.getModuleRes(act);
								Field piField = XposedHelpers.findFirstFieldByExactType(frag.getClass(), PackageInfo.class);
								mLastPackageInfo = (PackageInfo)piField.get(frag);
								Method[] addPref = XposedHelpers.findMethodsByExactParameters(frag.getClass(), void.class, String.class, String.class, String.class);
								if (mLastPackageInfo == null || addPref.length == 0) {
									Helpers.log("AppInfoHook", "Unable to find field/class/method in SecurityCenter to hook");
									return;
								} else {
									addPref[0].setAccessible(true);
								}
								addPref[0].invoke(frag, "apk_versioncode", modRes.getString(R.string.appdetails_apk_version_code), String.valueOf(mLastPackageInfo.versionCode));
								addPref[0].invoke(frag, "apk_filename", modRes.getString(R.string.appdetails_apk_file), mLastPackageInfo.applicationInfo.sourceDir);
								addPref[0].invoke(frag, "data_path", modRes.getString(R.string.appdetails_data_path), mLastPackageInfo.applicationInfo.dataDir);
								addPref[0].invoke(frag, "app_uid", modRes.getString(R.string.appdetails_app_uid), String.valueOf(mLastPackageInfo.applicationInfo.uid));
								addPref[0].invoke(frag, "target_sdk", modRes.getString(R.string.appdetails_sdk), String.valueOf(mLastPackageInfo.applicationInfo.targetSdkVersion));
								handler.post(new Runnable() {
									@Override
									public void run() {
										try {
											addPref[0].invoke(frag, "open_in_store", modRes.getString(R.string.appdetails_playstore), "");
											addPref[0].invoke(frag, "launch_app", modRes.getString(R.string.appdetails_launch), "");
										} catch (Throwable t) {
											XposedBridge.log(t);
										}
									}
								});
							} catch (Throwable t) {
								XposedBridge.log(t);
								return;
							}

							XposedBridge.hookAllMethods(frag.getClass(), "onPreferenceTreeClick", new MethodHook() {
								@Override
								protected void before(final MethodHookParam param) throws Throwable {
									String key = (String)XposedHelpers.callMethod(param.args[0], "getKey");
									String title = (String)XposedHelpers.callMethod(param.args[0], "getTitle");
									switch (key) {
										case "apk_filename":
											((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.sourceDir));
											Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
											param.setResult(true);
											break;
										case "data_path":
											((ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(title, mLastPackageInfo.applicationInfo.dataDir));
											Toast.makeText(act, act.getResources().getIdentifier("app_manager_copy_pkg_to_clip", "string", act.getPackageName()), Toast.LENGTH_SHORT).show();
											param.setResult(true);
											break;
										case "open_in_store":
											try {
												Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mLastPackageInfo.packageName));
												launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
												act.startActivity(launchIntent);
											} catch (android.content.ActivityNotFoundException anfe) {
												Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + mLastPackageInfo.packageName));
												launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
												act.startActivity(launchIntent);
											}
											param.setResult(true);
											break;
										case "launch_app":
											Intent launchIntent = act.getPackageManager().getLaunchIntentForPackage(mLastPackageInfo.packageName);
											if (launchIntent == null) {
												Toast.makeText(act, modRes.getString(R.string.appdetails_nolaunch), Toast.LENGTH_SHORT).show();
											} else {
												int user = 0;
												try {
													int uid = act.getIntent().getIntExtra("am_app_uid", -1);
													user = (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", uid);
												} catch (Throwable t) {
													XposedBridge.log(t);
												}

												launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
												if (user != 0) try {
													XposedHelpers.callMethod(act, "startActivityAsUser", launchIntent, XposedHelpers.newInstance(UserHandle.class, user));
												} catch (Throwable t) {
													XposedBridge.log(t);
												} else {
													act.startActivity(launchIntent);
												}
											}
											param.setResult(true);
											break;
									}
								}
							});
						}
					});
				}
			});
		else
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
					addPref[0].invoke(act, "apk_versioncode", modRes.getString(R.string.appdetails_apk_version_code), String.valueOf(mPackageInfo.versionCode));
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
								int user = 0;
								try {
									int uid = act.getIntent().getIntExtra("am_app_uid", -1);
									user = (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", uid);
								} catch (Throwable t) {
									XposedBridge.log(t);
								}

								launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
								if (user != 0) try {
									XposedHelpers.callMethod(act, "startActivityAsUser", launchIntent, XposedHelpers.newInstance(UserHandle.class, user));
								} catch (Throwable t) {
									XposedBridge.log(t);
								} else {
									act.startActivity(launchIntent);
								}
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

	public static void AppsDisableServiceHook(LoadPackageParam lpparam) {
		Helpers.hookAllMethods("com.android.server.pm.PackageManagerServiceInjector", lpparam.classLoader, "isAllowedDisable", XC_MethodReplacement.returnConstant(true));
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
				if ("com.android.settings".equals(mPackageInfo.packageName) || "com.google.android.packageinstaller".equals(mPackageInfo.packageName) || "com.android.packageinstaller".equals(mPackageInfo.packageName)) {
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

				String key = "pref_key_various_calluibright_night";
				boolean checkNight = Helpers.getSharedBoolPref(act, key, false);
				if (checkNight) {
					int start_hour = Helpers.getSharedIntPref(act, key + "_start_hour", 0);
					int start_minute = Helpers.getSharedIntPref(act, key + "_start_minute", 0);
					int end_hour = Helpers.getSharedIntPref(act, key + "_end_hour", 0);
					int end_minute = Helpers.getSharedIntPref(act, key + "_end_minute", 0);

					SimpleDateFormat formatter = new SimpleDateFormat("H:m", Locale.ENGLISH);
					formatter.setTimeZone(TimeZone.getDefault());
					Date start = formatter.parse(start_hour + ":" + start_minute);
					Date end = formatter.parse(end_hour + ":" + end_minute);
					Date now = formatter.parse(formatter.format(new Date()));
					if (start == null || end == null || now == null) return;

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
							if (ringtone.isPlaying()) ringtone.stop();
							ringtone.setAudioAttributes(new AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_NOTIFICATION).build());
							ringtone.play();
							XposedHelpers.setAdditionalInstanceField(param.thisObject, "mCurrentRingtone", ringtone);
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

		Helpers.findAndHookMethod("com.android.server.telecom.MiuiMissedCallNotifierImpl", lpparam.classLoader, "cancelMissedCallNotification", String.class, boolean.class, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Ringtone ringtone = (Ringtone)XposedHelpers.getAdditionalInstanceField(param.thisObject, "mCurrentRingtone");
				if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
			}
		});
	}

	private static TextView createTitleTextView(Context context, ViewGroup.LayoutParams lp, int resId) {
		TextView tv = new TextView(context);
		tv.setMaxLines(1);
		tv.setSingleLine(true);
		tv.setGravity(Gravity.START);
		tv.setLayoutParams(lp);
		tv.setTextAppearance(resId != -1 ? resId : android.R.style.TextAppearance_DeviceDefault);
		return tv;
	}

	private static TextView createValueTextView(Context context, ViewGroup.LayoutParams lp, int resId, int gravity) {
		TextView tv = new TextView(context);
		tv.setMaxLines(1);
		tv.setSingleLine(true);
		tv.setGravity(gravity);
		tv.setEllipsize(TextUtils.TruncateAt.START);
		tv.setLayoutParams(lp);
		tv.setTextAppearance(resId != -1 ? resId : android.R.style.TextAppearance_DeviceDefault);
		return tv;
	}

	public static void AppInfoDuringInstallHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.packageinstaller.PackageInstallerActivity", lpparam.classLoader, "startInstallConfirm", new MethodHook() {
			@Override
			@SuppressLint("SetTextI18n")
			protected void after(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				PackageInfo mPkgInfo = (PackageInfo)XposedHelpers.getObjectField(param.thisObject, "mPkgInfo");
				if (mPkgInfo == null) return;

				float density = act.getResources().getDisplayMetrics().density;

				TypedArray a = act.obtainStyledAttributes(new int[]{android.R.attr.textAppearance});
				int resId = a.getResourceId(0, -1);
				a.recycle();

				PackageInfo mAppInfo = null;
				try {
					PackageManager mPm = (PackageManager)XposedHelpers.getObjectField(param.thisObject, "mPm");
					mAppInfo = mPm.getPackageInfo(mPkgInfo.packageName, 0);
				} catch (Throwable t) {}

				Resources modRes = Helpers.getModuleRes(act);

				LinearLayout container = new LinearLayout(act);
				LinearLayout.LayoutParams lpCont = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lpCont.setMargins(Math.round(16.0f * density), Math.round(8.0f * density), Math.round(16.0f * density), Math.round(4.0f * density));
				container.setLayoutParams(lpCont);
				container.setOrientation(LinearLayout.VERTICAL);

				LinearLayout name = new LinearLayout(act);
				name.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				name.setOrientation(LinearLayout.HORIZONTAL);

				LinearLayout.LayoutParams lpInfoTitle = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lpInfoTitle.setMargins(0, 0, Math.round(20.0f * density), 0);
				TextView infoTitle = createTitleTextView(act, lpInfoTitle, resId);
				infoTitle.setText(modRes.getString(R.string.various_installappinfo_package));
				LinearLayout.LayoutParams lpInfo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				lpInfo.weight = 1;
				TextView info = createValueTextView(act, lpInfo, resId, Gravity.END);
				info.setText(mPkgInfo.applicationInfo.packageName);

				name.addView(infoTitle);
				name.addView(info);

				TableLayout table = new TableLayout(act);
				LinearLayout.LayoutParams lpTable = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				table.setLayoutParams(lpTable);
				table.setColumnStretchable(0, false);
				table.setColumnStretchable(1, true);
				table.setColumnStretchable(2, false);
				table.setColumnShrinkable(0, false);
				table.setColumnShrinkable(1, true);
				table.setColumnShrinkable(2, false);

				TableLayout.LayoutParams lpRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
				lpRow.gravity = Gravity.BOTTOM;
				TableRow row1 = new TableRow(act); row1.setLayoutParams(lpRow);
				TableRow row2 = new TableRow(act); row2.setLayoutParams(lpRow);
				TableRow row3 = new TableRow(act); row3.setLayoutParams(lpRow);

				TableRow.LayoutParams lp0 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				lp0.column = 0;
				lp0.setMargins(0, 0, Math.round(20.0f * density), 0);
				TableRow.LayoutParams lp1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
				lp1.column = 1;
				lp1.weight = 1;
				FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				lp2.gravity = Gravity.BOTTOM | Gravity.END;
				lp2.setMargins(0, 0, Math.round(20.0f * density), 0);
				TableRow.LayoutParams lp3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
				lp3.column = 2;

				TextView infoTitle1 = createTitleTextView(act, lp0, resId);
				infoTitle1.setText(modRes.getString(R.string.various_installappinfo_vername));
				TextView infoTitle2 = createTitleTextView(act, lp0, resId);
				infoTitle2.setText(modRes.getString(R.string.various_installappinfo_vercode));
				TextView infoTitle3 = createTitleTextView(act, lp0, resId);
				infoTitle3.setText(modRes.getString(R.string.various_installappinfo_sdk));

				FrameLayout dummy1 = new FrameLayout(act); dummy1.setLayoutParams(lp1);
				FrameLayout dummy2 = new FrameLayout(act); dummy2.setLayoutParams(lp1);
				FrameLayout dummy3 = new FrameLayout(act); dummy3.setLayoutParams(lp1);

				String current = modRes.getString(R.string.various_installappinfo_current);

				TextView info1current = createValueTextView(act, lp2, resId, Gravity.START);
				if (mAppInfo != null) {
					SpannableString span = new SpannableString(current + " " + mAppInfo.versionName);
					span.setSpan(new RelativeSizeSpan(0.7f), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					info1current.setText(span);
				} else info1current.setText(" ");

				TextView info2current = createValueTextView(act, lp2, resId, Gravity.START);
				if (mAppInfo != null) {
					SpannableString span = new SpannableString(current + " " + mAppInfo.versionCode);
					span.setSpan(new RelativeSizeSpan(0.7f), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					info2current.setText(span);
				} else info2current.setText(" ");

				TextView info3current = createValueTextView(act, lp2, resId, Gravity.START);
				if (mAppInfo != null) {
					SpannableString span = new SpannableString(current + " " + mAppInfo.applicationInfo.minSdkVersion + "-" + mAppInfo.applicationInfo.targetSdkVersion);
					span.setSpan(new RelativeSizeSpan(0.7f), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					info3current.setText(span);
				} else info3current.setText(" ");

				TextView info1 = createValueTextView(act, lp3, resId, Gravity.END);
				info1.setText(mPkgInfo.versionName);
				TextView info2 = createValueTextView(act, lp3, resId, Gravity.END);
				info2.setText(String.valueOf(mPkgInfo.versionCode));
				TextView info3 = createValueTextView(act, lp3, resId, Gravity.END);
				info3.setText(mPkgInfo.applicationInfo.minSdkVersion + "-" + mPkgInfo.applicationInfo.targetSdkVersion);

				row1.addView(infoTitle1);
				row2.addView(infoTitle2);
				row3.addView(infoTitle3);

				row1.addView(dummy1); dummy1.addView(info1current);
				row2.addView(dummy2); dummy2.addView(info2current);
				row3.addView(dummy3); dummy3.addView(info3current);

				row1.addView(info1);
				row2.addView(info2);
				row3.addView(info3);

				table.addView(row1);
				table.addView(row2);
				table.addView(row3);

				container.addView(name);
				container.addView(table);

				ViewGroup parent = (ViewGroup)act.findViewById(act.getResources().getIdentifier("install_confirm_question", "id", "com.android.packageinstaller")).getParent();
				if (parent.getChildCount() == 1)
					((ViewGroup)parent.getParent()).addView(container, ((ViewGroup)parent.getParent()).getChildCount() - 1);
				else
					parent.addView(container, parent.getChildCount() - 1);
			}
		});
	}

	public static void AppInfoDuringMiuiInstallHook(LoadPackageParam lpparam) {
		Method[] methods = XposedHelpers.findMethodsByExactParameters(findClass("com.android.packageinstaller.PackageInstallerActivity", lpparam.classLoader), void.class, String.class);
		if (methods.length == 0) {
			Helpers.log("AppInfoDuringMiuiInstallHook", "Cannot find appropriate method");
			return;
		}
		for (Method method: methods)
		Helpers.hookMethod(method, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				TextView version = act.findViewById(act.getResources().getIdentifier("install_version", "id", lpparam.packageName));
				Field fPkgInfo = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), PackageInfo.class);
				PackageInfo mPkgInfo = (PackageInfo)fPkgInfo.get(param.thisObject);
				if (version == null || mPkgInfo == null) return;

				TextView source = act.findViewById(act.getResources().getIdentifier("install_source", "id", lpparam.packageName));
				source.setGravity(Gravity.CENTER_HORIZONTAL);
				source.setText(mPkgInfo.packageName);

				PackageInfo mAppInfo = null;
				try {
					mAppInfo = act.getPackageManager().getPackageInfo(mPkgInfo.packageName, 0);
				} catch (Throwable t) {}

				//String size = "";
				//String[] texts = version.getText().toString().split("\\|");
				//if (texts.length >= 2) size = texts[1].trim();

				Resources modRes = Helpers.getModuleRes(act);

				SpannableStringBuilder builder = new SpannableStringBuilder();
				//if (!TextUtils.isEmpty(size)) builder.append(size).append("\n");
				builder.append(modRes.getString(R.string.various_installappinfo_vername)).append(":\t\t");
				if (mAppInfo != null) builder.append(mAppInfo.versionName).append("  ➟  ");
				builder.append(mPkgInfo.versionName).append("\n");
				builder.append(modRes.getString(R.string.various_installappinfo_vercode)).append(":\t\t");
				if (mAppInfo != null) builder.append(String.valueOf(mAppInfo.versionCode)).append("  ➟  ");
				builder.append(String.valueOf(mPkgInfo.versionCode)).append("\n");
				builder.append(modRes.getString(R.string.various_installappinfo_sdk)).append(":\t\t");
				if (mAppInfo != null) builder.append(String.valueOf(mAppInfo.applicationInfo.minSdkVersion)).append("-").append(String.valueOf(mAppInfo.applicationInfo.targetSdkVersion)).append("  ➟  ");
				builder.append(String.valueOf(mPkgInfo.applicationInfo.minSdkVersion)).append("-").append(String.valueOf(mPkgInfo.applicationInfo.targetSdkVersion));

				version.setGravity(Gravity.CENTER_HORIZONTAL);
				version.setSingleLine(false);
				version.setMaxLines(10);
				version.setText(builder);
				version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.09f);
			}
		});
	}

	public static void MiuiPackageInstallerServiceHook(LoadPackageParam lpparam) {
		MethodHook hook = new MethodHook() {
			@Override
			@SuppressWarnings({"unchecked"})
			protected void after(MethodHookParam param) throws Throwable {
				try {
					if (param.args[0] == null) return;
					Intent origIntent = (Intent)param.args[0];
					Intent intent = (Intent)origIntent.clone();
					String action = intent.getAction();
					if (!Intent.ACTION_INSTALL_PACKAGE.equals(action)) return;
					//XposedBridge.log(intent.getPackage() + ": " + intent.getType() + " | " + intent.getDataString());
					boolean res = false;
					try {
						res = XposedHelpers.callMethod(param.thisObject, "getPackageInfo", "com.miui.packageinstaller", 0, 0) != null;
					} catch (Throwable e) {}
					if (!res) return;

					List<ResolveInfo> resolved = new ArrayList<ResolveInfo>((List<ResolveInfo>)param.getResult());
					ResolveInfo resolveInfo;
					Iterator<ResolveInfo> itr = resolved.iterator();
					while (itr.hasNext()) {
						resolveInfo = itr.next();
						if (resolveInfo.activityInfo != null && !"com.miui.packageinstaller".equals(resolveInfo.activityInfo.packageName)) itr.remove();
					}

					if (resolved.size() > 0) param.setResult(resolved);
				} catch (Throwable t) {
					if (!(t instanceof BadParcelableException)) XposedBridge.log(t);
				}
			}
		};

		if (!Helpers.findAndHookMethodSilently("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", Intent.class, String.class, int.class, int.class, int.class, boolean.class, boolean.class, hook))
		Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal", Intent.class, String.class, int.class, int.class, hook);
	}

	public static void MiuiPackageInstallerHook(LoadPackageParam lpparam) {
		Helpers.findAndHookMethod("com.android.packageinstaller.PackageInstallerActivity", lpparam.classLoader, "onCreate", Bundle.class, new MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void before(MethodHookParam param) throws Throwable {
				Activity act = (Activity)param.thisObject;
				List<ApplicationInfo> packs = act.getPackageManager().getInstalledApplications(PackageManager.MATCH_SYSTEM_ONLY | PackageManager.MATCH_DISABLED_COMPONENTS);
				for (Field field: param.thisObject.getClass().getDeclaredFields())
				if (field.getType() == List.class) {
					field.setAccessible(true);
					ArrayList<String> whiteList = (ArrayList<String>)field.get(param.thisObject);
					if (whiteList == null || whiteList.size() <= 1) continue;
					for (ApplicationInfo pack: packs)
					if (!whiteList.contains(pack.packageName)) whiteList.add(pack.packageName);
				}
			}
		});
	}

	public static void CollapseMIUITitlesHook(LoadPackageParam lpparam, XC_MethodHook.MethodHookParam param, int opt) {
		Application app = (Application)param.thisObject;
		String pkgName = app.getPackageName();
		boolean isMIUIapp = pkgName.startsWith("com.miui") || pkgName.startsWith("com.xiaomi");
		if (!isMIUIapp) isMIUIapp = app.checkSelfPermission("miui.permission.USE_INTERNAL_GENERAL_API") == PackageManager.PERMISSION_GRANTED;
		if (!isMIUIapp) return;
		Class<?> aabvCls = XposedHelpers.findClassIfExists("com.miui.internal.widget.AbsActionBarView", lpparam.classLoader);
		if (aabvCls != null)
		Helpers.hookAllConstructors(aabvCls, new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				XposedHelpers.setIntField(param.thisObject, "mExpandState", ActionBar.STATE_COLLAPSE);
				XposedHelpers.setIntField(param.thisObject, "mInnerExpandState", ActionBar.STATE_COLLAPSE);
				if (opt == 3)
				XposedHelpers.setBooleanField(param.thisObject, "mResizable", false);
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