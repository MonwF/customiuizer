package name.mikanoshi.customiuizer.mods;

import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;

import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.Helpers.MethodHook;

public class PackagePermissions {

	@SuppressWarnings("unchecked")
	private static void doBefore(MethodHookParam param) {
		ArrayList<String> requestedPermissions = (ArrayList<String>)getObjectField(param.args[0], "requestedPermissions");
		param.setObjectExtra("orig_requested_permissions", requestedPermissions);
		//ArrayList<Boolean> requestedPermissionsRequired = (ArrayList<Boolean>)getObjectField(param.args[0], "requestedPermissionsRequired");
		//param.setObjectExtra("orig_requested_permissions_required", requestedPermissionsRequired);

		String pkgName = (String)getObjectField(param.args[0], "packageName");
		if (pkgName.equalsIgnoreCase(Helpers.modulePkg)) {
			requestedPermissions.add("miui.permission.READ_LOGS");
			requestedPermissions.add("miui.permission.DUMP_CACHED_LOG");
		}

		setObjectField(param.args[0], "requestedPermissions", requestedPermissions);
		//setObjectField(param.args[0], "requestedPermissionsRequired", requestedPermissionsRequired);
	}

	@SuppressWarnings("unchecked")
	private static void doAfter(MethodHookParam param) {
		ArrayList<String> origRequestedPermissions = (ArrayList<String>) param.getObjectExtra("orig_requested_permissions");
		if (origRequestedPermissions != null) setObjectField(param.args[0], "requestedPermissions", origRequestedPermissions);
		//ArrayList<Boolean> origRequestedPermissionsRequired = (ArrayList<Boolean>) param.getObjectExtra("orig_requested_permissions_required");
		//if (origRequestedPermissionsRequired != null) setObjectField(param.args[0], "requestedPermissionsRequired", origRequestedPermissionsRequired);
	}

	public static void init(LoadPackageParam lpparam) {
		// Allow signature level permissions for module
		if (!Helpers.findAndHookMethodSilently("com.android.server.pm.permission.PermissionManagerService", lpparam.classLoader, "grantSignaturePermission",
			String.class, "android.content.pm.PackageParser.Package", "com.android.server.pm.permission.BasePermission", "com.android.server.pm.permission.PermissionsState",
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					String pkgName = (String)XposedHelpers.getObjectField(param.args[1], "packageName");
					if (pkgName.equalsIgnoreCase(Helpers.modulePkg)) param.setResult(true);
				}
			}
		)) Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "grantSignaturePermission",
			String.class, "android.content.pm.PackageParser.Package", "com.android.server.pm.BasePermission", "com.android.server.pm.PermissionsState",
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					String pkgName = (String)XposedHelpers.getObjectField(param.args[1], "packageName");
					if (pkgName.equalsIgnoreCase(Helpers.modulePkg)) param.setResult(true);
				}
			}
		);

		if (!Helpers.findAndHookMethodSilently("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "verifySignatures",
			"com.android.server.pm.PackageSetting", "com.android.server.pm.PackageSetting", "android.content.pm.PackageParser.SigningDetails", boolean.class, boolean.class,
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					String pkgName = (String)XposedHelpers.getObjectField(param.args[0], "name");
					if (pkgName.equalsIgnoreCase(Helpers.modulePkg)) param.setResult(true);
				}
			}
		)) Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "verifySignaturesLP",
			"com.android.server.pm.PackageSetting", "android.content.pm.PackageParser.Package",
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					String pkgName = (String)XposedHelpers.getObjectField(param.args[1], "packageName");
					if (pkgName.equalsIgnoreCase(Helpers.modulePkg)) param.setResult(true);
				}
			}
		);

		// Add custom permissions for module
		if (!Helpers.findAndHookMethodSilently("com.android.server.pm.permission.PermissionManagerService", lpparam.classLoader, "grantPermissions",
			"android.content.pm.PackageParser$Package", boolean.class, String.class, "com.android.server.pm.permission.PermissionManagerInternal.PermissionCallback",
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					doBefore(param);
				}

				@Override
				protected void after(MethodHookParam param) throws Throwable {
					doAfter(param);
				}
			}
		)) Helpers.findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "grantPermissionsLPw",
			"android.content.pm.PackageParser$Package", boolean.class, String.class,
			new MethodHook() {
				@Override
				protected void before(MethodHookParam param) throws Throwable {
					doBefore(param);
				}

				@Override
				protected void after(MethodHookParam param) throws Throwable {
					doAfter(param);
				}
			}
		);

		// Make module appear as system app
		Helpers.hookAllMethods("com.android.server.pm.PackageManagerService", lpparam.classLoader, "queryIntentActivitiesInternal",
			new MethodHook() {
				@Override
				@SuppressWarnings("unchecked")
				protected void after(MethodHookParam param) throws Throwable {
					List<ResolveInfo> infos = (List<ResolveInfo>)param.getResult();
					if (infos != null)
					for (ResolveInfo info: infos)
					if (info != null && info.activityInfo != null && info.activityInfo.packageName.equalsIgnoreCase(Helpers.modulePkg))
					XposedHelpers.setObjectField(info, "system", true);
				}
			}
		);

		Helpers.findAndHookMethod("android.content.pm.ApplicationInfo", lpparam.classLoader, "isSystemApp",
			new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					ApplicationInfo ai = (ApplicationInfo)param.thisObject;
					if (ai != null && ai.packageName.equalsIgnoreCase(Helpers.modulePkg)) param.setResult(true);
				}
			}
		);

		//noinspection ResultOfMethodCallIgnored
		Helpers.findAndHookMethodSilently("android.content.pm.ApplicationInfo", lpparam.classLoader, "isSignedWithPlatformKey",
			new MethodHook() {
				@Override
				protected void after(MethodHookParam param) throws Throwable {
					ApplicationInfo ai = (ApplicationInfo)param.thisObject;
					if (ai != null && ai.packageName.equalsIgnoreCase(Helpers.modulePkg)) param.setResult(true);
				}
			}
		);

		try {
			Class<?> dpgpiClass = findClass("com.android.server.pm.DefaultPermissionGrantPolicyInjector", lpparam.classLoader);
			String[] MIUI_SYSTEM_APPS = (String[])XposedHelpers.getStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS");
			String[] MIUI_SYSTEM_APPS_NEW = new String[MIUI_SYSTEM_APPS.length + 1];
			MIUI_SYSTEM_APPS_NEW[MIUI_SYSTEM_APPS_NEW.length - 1] = Helpers.modulePkg;
			XposedHelpers.setStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS", MIUI_SYSTEM_APPS_NEW);
		} catch (Throwable t) {}
	}

}