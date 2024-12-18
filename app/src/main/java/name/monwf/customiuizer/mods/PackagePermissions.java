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

	public static void hook(XposedModuleInterface.SystemServerLoadedParam lpparam) {
		systemPackages.add(Helpers.modulePkg);
		//systemPackages.add("com.miui.packageinstaller");

		// Allow signature level permissions for module
//		String PMSCls = "com.android.server.pm.permission.PermissionManagerServiceImpl";
//		ModuleHelper.hookAllMethods(PMSCls, lpparam.getClassLoader(), "shouldGrantPermissionBySignature",
//			new MethodHook() {
//				@Override
//				protected void before(final BeforeHookCallback param) throws Throwable {
//					String pkgName = (String)XposedHelpers.callMethod(param.getArgs()[0], "getPackageName");
//					if (systemPackages.contains(pkgName)) param.returnAndSkip(true);
//				}
//			}
//		);
//
//		ModuleHelper.hookAllMethods("com.android.server.pm.PackageManagerServiceUtils", lpparam.getClassLoader(), "verifySignatures",
//			new MethodHook() {
//				@Override
//				protected void before(final BeforeHookCallback param) throws Throwable {
//					String pkgName = (String)XposedHelpers.callMethod(param.getArgs()[0], "getName");
//					if (systemPackages.contains(pkgName)) param.returnAndSkip(true);
//				}
//			}
//		);
//
//		// Make module appear as system app
//		String ActQueryService = "com.android.server.pm.ComputerEngine";
//		ModuleHelper.hookAllMethods(ActQueryService, lpparam.getClassLoader(), "queryIntentActivitiesInternal", new MethodHook() {
//			@Override
//			@SuppressWarnings("unchecked")
//			protected void after(final AfterHookCallback param) throws Throwable {
//				if (param.getArgs().length < 6) return;
//				List<ResolveInfo> infos = (List<ResolveInfo>)param.getResult();
//				if (infos != null) {
//					for (ResolveInfo info: infos)
//						if (info != null && info.activityInfo != null && systemPackages.contains(info.activityInfo.packageName))
//							XposedHelpers.setObjectField(info, "system", true);
//				}
//			}
//		});
//
//		ModuleHelper.findAndHookMethod("android.content.pm.ApplicationInfo", lpparam.getClassLoader(), "isSystemApp", new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				ApplicationInfo ai = (ApplicationInfo)param.getThisObject();
//				if (ai != null && systemPackages.contains(ai.packageName)) param.setResult(true);
//			}
//		});
//
//		ModuleHelper.findAndHookMethod("android.content.pm.ApplicationInfo", lpparam.getClassLoader(), "isSignedWithPlatformKey", new MethodHook() {
//			@Override
//			protected void after(final AfterHookCallback param) throws Throwable {
//				ApplicationInfo ai = (ApplicationInfo)param.getThisObject();
//				if (ai != null && systemPackages.contains(ai.packageName)) param.setResult(true);
//			}
//		});
//
//		ModuleHelper.hookAllMethods("com.android.server.wm.ActivityRecordInjector", lpparam.getClassLoader(), "canShowWhenLocked", new MethodHook() {
//			@Override
//			protected void before(final BeforeHookCallback param) throws Throwable {
//				param.returnAndSkip(true);
//			}
//		});

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