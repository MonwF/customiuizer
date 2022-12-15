package name.mikanoshi.customiuizer.mods;

import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XposedHelpers;

import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

public class PackagePermissions {

	private static final ArrayList<String> systemPackages = new ArrayList<String>();

//	@SuppressWarnings("unchecked")
//	private static void doBefore(MethodHookParam param) {
//		ArrayList<String> requestedPermissions = (ArrayList<String>)getObjectField(param.args[0], "requestedPermissions");
//		param.setObjectExtra("orig_requested_permissions", requestedPermissions);
//		//ArrayList<Boolean> requestedPermissionsRequired = (ArrayList<Boolean>)getObjectField(param.args[0], "requestedPermissionsRequired");
//		//param.setObjectExtra("orig_requested_permissions_required", requestedPermissionsRequired);
//
//		String pkgName = (String)getObjectField(param.args[0], "packageName");
//		if (pkgName.equalsIgnoreCase(Helpers.modulePkg)) {
//			requestedPermissions.add("miui.permission.READ_LOGS");
//			requestedPermissions.add("miui.permission.DUMP_CACHED_LOG");
//		}
//
//		setObjectField(param.args[0], "requestedPermissions", requestedPermissions);
//		//setObjectField(param.args[0], "requestedPermissionsRequired", requestedPermissionsRequired);
//	}
//
//	@SuppressWarnings("unchecked")
//	private static void doAfter(MethodHookParam param) {
//		ArrayList<String> origRequestedPermissions = (ArrayList<String>) param.getObjectExtra("orig_requested_permissions");
//		if (origRequestedPermissions != null) setObjectField(param.args[0], "requestedPermissions", origRequestedPermissions);
//		//ArrayList<Boolean> origRequestedPermissionsRequired = (ArrayList<Boolean>) param.getObjectExtra("orig_requested_permissions_required");
//		//if (origRequestedPermissionsRequired != null) setObjectField(param.args[0], "requestedPermissionsRequired", origRequestedPermissionsRequired);
//	}

	public static void hook(LoadPackageParam lpparam) {
		systemPackages.add(Helpers.modulePkg);
		//systemPackages.add("com.miui.packageinstaller");

		// Allow signature level permissions for module
		String PMSCls = Helpers.isTPlus() ? "com.android.server.pm.permission.PermissionManagerServiceImpl" : "com.android.server.pm.permission.PermissionManagerService";
		Helpers.hookAllMethods(PMSCls, lpparam.classLoader, "shouldGrantPermissionBySignature",
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					String pkgName = (String)XposedHelpers.callMethod(param.args[0], "getPackageName");
					if (systemPackages.contains(pkgName)) param.setResult(true);
				}
			}
		);

		Helpers.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "verifySignatures",
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					String pkgName = (String)XposedHelpers.callMethod(param.args[0], "getName");
					if (systemPackages.contains(pkgName)) param.setResult(true);
				}
			}
		);

//		// Add custom permissions for module
//		if (!Helpers.findAndHookMethodSilently("com.android.server.pm.permission.PermissionManagerService", lpparam.classLoader, "grantRequestedRuntimePermissions",
//			"android.content.pm.PackageParser$Package", int[].class, String[].class, int.class, "com.android.server.pm.permission.PermissionManagerServiceInternal.PermissionCallback",
//			new MethodHook() {
//				@Override
//				protected void before(MethodHookParam param) throws Throwable {
//					doBefore(param);
//				}
//				@Override
//				protected void after(MethodHookParam param) throws Throwable {
//					doAfter(param);
//				}
//			}
//		)) if (!Helpers.findAndHookMethodSilently("com.android.server.pm.permission.PermissionManagerService", lpparam.classLoader, "grantPermissions",
//			"android.content.pm.PackageParser$Package", boolean.class, String.class, "com.android.server.pm.permission.PermissionManagerInternal.PermissionCallback",
//			new MethodHook() {
//				@Override
//				protected void before(MethodHookParam param) throws Throwable {
//					doBefore(param);
//				}
//				@Override
//				protected void after(MethodHookParam param) throws Throwable {
//					doAfter(param);
//				}
//			}
//		)) Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "grantPermissionsLPw",
//			"android.content.pm.PackageParser$Package", boolean.class, String.class,
//			new MethodHook() {
//				@Override
//				protected void before(MethodHookParam param) throws Throwable {
//					doBefore(param);
//				}
//				@Override
//				protected void after(MethodHookParam param) throws Throwable {
//					doAfter(param);
//				}
//			}
//		);

		// Make module appear as system app
		String ActQueryService = Helpers.isTPlus() ? "com.android.server.pm.ComputerEngine" : "com.android.server.pm.PackageManagerService";
		Helpers.hookAllMethods(ActQueryService, lpparam.classLoader, "queryIntentActivitiesInternal", new MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void after(MethodHookParam param) throws Throwable {
				if (param.args.length < 6) return;
				List<ResolveInfo> infos = (List<ResolveInfo>)param.getResult();
				if (infos != null) {
					for (ResolveInfo info: infos)
						if (info != null && info.activityInfo != null && systemPackages.contains(info.activityInfo.packageName))
							XposedHelpers.setObjectField(info, "system", true);
				}
			}
		});

//		// Causes module removal by system on updates
//		Helpers.hookAllMethods("com.android.server.pm.PackageManagerService", lpparam.classLoader, "getApplicationInfoInternal", new MethodHook() {
//			@Override
//			protected void after(MethodHookParam param) throws Throwable {
//				ApplicationInfo info = (ApplicationInfo)param.getResult();
//				if (info != null && systemPackages.contains(info.packageName)) {
//					info.flags |= ApplicationInfo.FLAG_SYSTEM;
//					param.setResult(info);
//				}
//			}
//		});

		Helpers.findAndHookMethod("android.content.pm.ApplicationInfo", lpparam.classLoader, "isSystemApp", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				ApplicationInfo ai = (ApplicationInfo)param.thisObject;
				if (ai != null && systemPackages.contains(ai.packageName)) param.setResult(true);
			}
		});

		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("android.content.pm.ApplicationInfo", lpparam.classLoader, "isSignedWithPlatformKey", new MethodHook() {
			@Override
			protected void after(MethodHookParam param) throws Throwable {
				ApplicationInfo ai = (ApplicationInfo)param.thisObject;
				if (ai != null && systemPackages.contains(ai.packageName)) param.setResult(true);
			}
		});

		Helpers.hookAllMethodsSilently("com.android.server.wm.ActivityRecordInjector", lpparam.classLoader, "canShowWhenLocked", new MethodHook() {
			@Override
			protected void before(MethodHookParam param) throws Throwable {
				param.setResult(true);
			}
		});

		try {
			Class<?> dpgpiClass = findClass("com.android.server.pm.MiuiDefaultPermissionGrantPolicy", lpparam.classLoader);
			String[] MIUI_SYSTEM_APPS = (String[])XposedHelpers.getStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS");
			ArrayList<String> mySystemApps = new ArrayList<String>(Arrays.asList(MIUI_SYSTEM_APPS));
			mySystemApps.addAll(systemPackages);
			XposedHelpers.setStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS", mySystemApps.toArray(new String[0]));
		} catch (Throwable t) {
			Helpers.log(t);
		}
	}

}