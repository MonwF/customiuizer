package name.mikanoshi.customiuizer.mods;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import name.mikanoshi.customiuizer.R;

import name.mikanoshi.customiuizer.utils.Helpers;

public class Various {

	public static void AppInfoHook(XC_LoadPackage.LoadPackageParam lpparam) {
		try {
			XposedBridge.hookAllMethods(XposedHelpers.findClass("com.miui.appmanager.AMAppInfomationActivity", lpparam.classLoader), "onLoadFinished", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					final PreferenceActivity act = (PreferenceActivity)param.thisObject;
					final PackageInfo mPackageInfo = (PackageInfo)XposedHelpers.getObjectField(act, "mPackageInfo");
					final Resources modRes = Helpers.getModuleRes(act);
					Method[] addPref = XposedHelpers.findMethodsByExactParameters(act.getClass(), void.class, String.class, String.class, String.class);
					if (addPref.length == 0) {
						XposedBridge.log("[CustoMIUIzer][AppInfo] Unable to find class/method in SecurityCenter to hook");
						return;
					} else {
						addPref[0].setAccessible(true);
					}
					addPref[0].invoke(act, "apk_filename", modRes.getString(R.string.appdetails_apk_file), mPackageInfo.applicationInfo.sourceDir);
					addPref[0].invoke(act, "data_path", modRes.getString(R.string.appdetails_data_path), mPackageInfo.applicationInfo.dataDir);
					addPref[0].invoke(act, "app_uid", modRes.getString(R.string.appdetails_app_uid), String.valueOf(mPackageInfo.applicationInfo.uid));
					addPref[0].invoke(act, "target_sdk", modRes.getString(R.string.appdetails_sdk), String.valueOf(mPackageInfo.applicationInfo.targetSdkVersion));
					addPref[0].invoke(act, "launch_app", modRes.getString(R.string.appdetails_launch), "");

					@SuppressWarnings("deprecation")
					Preference pref = act.findPreference("launch_app");
					pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	public static void AppsDefaultSortHook(XC_LoadPackage.LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					Bundle bundle = (Bundle)param.args[0];
					if (bundle == null) bundle = new Bundle();
					int order = Integer.parseInt(Helpers.getSharedStringPref((Context)param.thisObject, "pref_key_various_appsort", "0"));
					bundle.putInt("current_sory_type", order); // Xiaomi noob typos :)
					bundle.putInt("current_sort_type", order); // Future proof, they may fix it someday :D
					param.args[0] = bundle;
				}
			});

//			XposedHelpers.findAndHookMethod("com.miui.appmanager.AppManagerMainActivity", lpparam.classLoader, "onSaveInstanceState", Bundle.class, new XC_MethodHook() {
//				@Override
//				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
//					Bundle bundle = (Bundle)param.args[0];
//					if (bundle == null) bundle = new Bundle();
//					bundle.putInt("current_sory_type", 1); // Xiaomi noob typos :)
//					bundle.putInt("current_sort_type", 1); // Future proof, they may fix it someday :D
//					XposedBridge.log("onSaveInstanceState: " + String.valueOf(bundle));
//				}
//			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
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

	public static void AppsDisableHook(XC_LoadPackage.LoadPackageParam lpparam) {
		try {
			XposedHelpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "onCreateOptionsMenu", Menu.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					Activity act = (Activity)param.thisObject;
					Menu menu = (Menu)param.args[0];
					MenuItem dis = menu.add(0, 666, 1, act.getResources().getIdentifier("app_manager_disable_text", "string", lpparam.packageName));
					dis.setIcon(act.getResources().getIdentifier("action_button_stop", "drawable", lpparam.packageName));
					dis.setEnabled(true);
					dis.setShowAsAction(1);
					//XposedHelpers.setAdditionalInstanceField(param.thisObject, "mDisableButton", dis);

					PackageManager pm = act.getPackageManager();
					String mPackageName = (String)XposedHelpers.getObjectField(act, "mPackageName");
					ApplicationInfo appInfo = pm.getApplicationInfo(mPackageName, PackageManager.GET_META_DATA);
					boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
					boolean isUpdatedSystem = (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

					dis.setTitle(act.getResources().getIdentifier(appInfo.enabled ? "app_manager_disable_text" : "app_manager_enable_text", "string", lpparam.packageName));

					if (!appInfo.enabled || (isSystem && !isUpdatedSystem)) {
						MenuItem item = menu.findItem(2);
						if (item != null) item.setVisible(false);
					}
				}
			});

			XposedHelpers.findAndHookMethod("com.miui.appmanager.ApplicationsDetailsActivity", lpparam.classLoader, "onOptionsItemSelected", MenuItem.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
					MenuItem item = (MenuItem)param.args[0];
					if (item == null || item.getItemId() != 666) return;

					Activity act = (Activity)param.thisObject;
					Resources modRes = Helpers.getModuleRes(act);
					String mPackageName = (String)XposedHelpers.getObjectField(act, "mPackageName");
					if ("com.android.settings".equals(mPackageName)) {
						Toast.makeText(act, modRes.getString(R.string.disable_app_settings), Toast.LENGTH_SHORT).show();
						return;
					}

					boolean mIsSystem = XposedHelpers.getBooleanField(act, "mIsSystem");
					PackageManager pm = act.getPackageManager();
					int state = pm.getApplicationEnabledSetting(mPackageName);
					boolean isEnabledOrDefault = (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
					if (isEnabledOrDefault) {
						if (mIsSystem) {

							String title = modRes.getString(R.string.disable_app_title);
							String text = modRes.getString(R.string.disable_app_text);
							new AlertDialog.Builder(act).setTitle(title).setMessage(text).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									setAppState(act, mPackageName, item, false);
								}
							}).setNegativeButton(android.R.string.cancel, null).show();
						} else setAppState(act, mPackageName, item, false);
					} else setAppState(act, mPackageName, item, true);
					param.setResult(true);
				}
			});
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

//	public static void LargeCallerPhotoHook(XC_LoadPackage.LoadPackageParam lpparam) {
//		try {
//			XposedHelpers.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.classLoader, "setCallCardImage", Drawable.class, boolean.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//					param.args[1] = true;
//				}
//			});
//
//			XposedHelpers.findAndHookMethod("com.android.incallui.CallCardFragment", lpparam.classLoader, "showBigAvatar", boolean.class, Drawable.class, new XC_MethodHook() {
//				@Override
//				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//					//XposedBridge.log("showBigAvatar: " + String.valueOf(param.args[0]) + " | " + String.valueOf(param.args[1]));
//					if (param.args[1] == null)
//						param.setResult(null);
//					else
//						param.args[0] = true;
//				}
//			});
//		} catch (Throwable t) {
//			XposedBridge.log(t);
//		}
//	}

}