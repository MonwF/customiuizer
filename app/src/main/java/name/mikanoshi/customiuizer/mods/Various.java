package name.mikanoshi.customiuizer.mods;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
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