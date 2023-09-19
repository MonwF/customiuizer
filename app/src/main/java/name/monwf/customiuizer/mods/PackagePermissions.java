package name.monwf.customiuizer.mods;

import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;

import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedInterface.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;


public class PackagePermissions {

	private static final ArrayList<String> systemPackages = new ArrayList<String>();

//	@SuppressWarnings("unchecked")
//	private static void dobefore(final BeforeHookCallback param) {
//		ArrayList<String> requestedPermissions = (ArrayList<String>)getObjectField(param.getArgs()[0], "requestedPermissions");
//		param.setObjectExtra("orig_requested_permissions", requestedPermissions);
//		//ArrayList<Boolean> requestedPermissionsRequired = (ArrayList<Boolean>)getObjectField(param.getArgs()[0], "requestedPermissionsRequired");
//		//param.setObjectExtra("orig_requested_permissions_required", requestedPermissionsRequired);
//
//		String pkgName = (String)getObjectField(param.getArgs()[0], "packageName");
//		if (pkgName.equalsIgnoreCase(Helpers.modulePkg)) {
//			requestedPermissions.add("miui.permission.READ_LOGS");
//			requestedPermissions.add("miui.permission.DUMP_CACHED_LOG");
//		}
//
//		setObjectField(param.getArgs()[0], "requestedPermissions", requestedPermissions);
//		//setObjectField(param.getArgs()[0], "requestedPermissionsRequired", requestedPermissionsRequired);
//	}
//
//	@SuppressWarnings("unchecked")
//	private static void doafter(final AfterHookCallback param) {
//		ArrayList<String> origRequestedPermissions = (ArrayList<String>) param.getObjectExtra("orig_requested_permissions");
//		if (origRequestedPermissions != null) setObjectField(param.getArgs()[0], "requestedPermissions", origRequestedPermissions);
//		//ArrayList<Boolean> origRequestedPermissionsRequired = (ArrayList<Boolean>) param.getObjectExtra("orig_requested_permissions_required");
//		//if (origRequestedPermissionsRequired != null) setObjectField(param.getArgs()[0], "requestedPermissionsRequired", origRequestedPermissionsRequired);
//	}

	public static void hook(XposedModuleInterface.SystemServerLoadedParam lpparam) {
		systemPackages.add(Helpers.modulePkg);
		//systemPackages.add("com.miui.packageinstaller");

		// Allow signature level permissions for module
		String PMSCls = "com.android.server.pm.permission.PermissionManagerServiceImpl";
		ModuleHelper.hookAllMethods(PMSCls, lpparam.getClassLoader(), "shouldGrantPermissionBySignature",
			new MethodHook() {
				@Override
				protected void before(final BeforeHookCallback param) throws Throwable {
					String pkgName = (String)XposedHelpers.callMethod(param.getArgs()[0], "getPackageName");
					if (systemPackages.contains(pkgName)) param.returnAndSkip(true);
				}
			}
		);

		ModuleHelper.hookAllMethodsSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.getClassLoader(), "verifySignatures",
			new MethodHook() {
				@Override
				protected void before(final BeforeHookCallback param) throws Throwable {
					String pkgName = (String)XposedHelpers.callMethod(param.getArgs()[0], "getName");
					if (systemPackages.contains(pkgName)) param.returnAndSkip(true);
				}
			}
		);

//		// Add custom permissions for module
//		if (!ModuleHelper.findAndHookMethodSilently("com.android.server.pm.permission.PermissionManagerService", lpparam.getClassLoader(), "grantRequestedRuntimePermissions",
//			"android.content.pm.PackageParser$Package", int[].class, String[].class, int.class, "com.android.server.pm.permission.PermissionManagerServiceInternal.PermissionCallback",
//			new MethodHook() {
//				@Override
//				protected void before(final BeforeHookCallback param) throws Throwable {
//					doBefore(param);
//				}
//				@Override
//				protected void after(final AfterHookCallback param) throws Throwable {
//					doAfter(param);
//				}
//			}
//		)) if (!ModuleHelper.findAndHookMethodSilently("com.android.server.pm.permission.PermissionManagerService", lpparam.getClassLoader(), "grantPermissions",
//			"android.content.pm.PackageParser$Package", boolean.class, String.class, "com.android.server.pm.permission.PermissionManagerInternal.PermissionCallback",
//			new MethodHook() {
//				@Override
//				protected void before(final BeforeHookCallback param) throws Throwable {
//					doBefore(param);
//				}
//				@Override
//				protected void after(final AfterHookCallback param) throws Throwable {
//					doAfter(param);
//				}
//			}
//		)) ModuleHelper.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.getClassLoader(), "grantPermissionsLPw",
//			"android.content.pm.PackageParser$Package", boolean.class, String.class,
//			new MethodHook() {
//				@Override
//				protected void before(final BeforeHookCallback param) throws Throwable {
//					doBefore(param);
//				}
//				@Override
//				protected void after(final AfterHookCallback param) throws Throwable {
//					doAfter(param);
//				}
//			}
//		);

		// Make module appear as system app
		String ActQueryService = "com.android.server.pm.ComputerEngine";
		ModuleHelper.hookAllMethods(ActQueryService, lpparam.getClassLoader(), "queryIntentActivitiesInternal", new MethodHook() {
			@Override
			@SuppressWarnings("unchecked")
			protected void after(final AfterHookCallback param) throws Throwable {
				if (param.getArgs().length < 6) return;
				List<ResolveInfo> infos = (List<ResolveInfo>)param.getResult();
				if (infos != null) {
					for (ResolveInfo info: infos)
						if (info != null && info.activityInfo != null && systemPackages.contains(info.activityInfo.packageName))
							XposedHelpers.setObjectField(info, "system", true);
				}
			}
		});

//		// Causes module removal by system on updates
//		ModuleHelper.hookAllMethods("com.android.server.pm.PackageManagerService", lpparam.getClassLoader(), "getApplicationInfoInternal", new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				ApplicationInfo info = (ApplicationInfo)param.getResult();
//				if (info != null && systemPackages.contains(info.packageName)) {
//					info.flags |= ApplicationInfo.FLAG_SYSTEM;
//					param.returnAndSkip(info);
//				}
//			}
//		});

		ModuleHelper.findAndHookMethod("android.content.pm.ApplicationInfo", lpparam.getClassLoader(), "isSystemApp", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				ApplicationInfo ai = (ApplicationInfo)param.getThisObject();
				if (ai != null && systemPackages.contains(ai.packageName)) param.setResult(true);
			}
		});

		ModuleHelper.findAndHookMethodSilently("android.content.pm.ApplicationInfo", lpparam.getClassLoader(), "isSignedWithPlatformKey", new MethodHook() {
			@Override
			protected void after(final AfterHookCallback param) throws Throwable {
				ApplicationInfo ai = (ApplicationInfo)param.getThisObject();
				if (ai != null && systemPackages.contains(ai.packageName)) param.setResult(true);
			}
		});

		ModuleHelper.hookAllMethodsSilently("com.android.server.wm.ActivityRecordInjector", lpparam.getClassLoader(), "canShowWhenLocked", new MethodHook() {
			@Override
			protected void before(final BeforeHookCallback param) throws Throwable {
				param.returnAndSkip(true);
			}
		});

		try {
			Class<?> dpgpiClass = findClass("com.android.server.pm.MiuiDefaultPermissionGrantPolicy", lpparam.getClassLoader());
			String[] MIUI_SYSTEM_APPS = (String[])XposedHelpers.getStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS");
			ArrayList<String> mySystemApps = new ArrayList<String>(Arrays.asList(MIUI_SYSTEM_APPS));
			mySystemApps.addAll(systemPackages);
			XposedHelpers.setStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS", mySystemApps.toArray(new String[0]));
		} catch (Throwable t) {
			XposedHelpers.log(t);
		}
	}

}