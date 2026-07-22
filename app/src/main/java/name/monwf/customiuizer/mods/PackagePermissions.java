package name.monwf.customiuizer.mods;

import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;

import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import name.monwf.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback;
import io.github.libxposed.api.XposedModuleInterface;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;


public class PackagePermissions {

	private static final ArrayList<String> systemPackages = new ArrayList<String>();

	public static void hook(XposedModuleInterface.SystemServerStartingParam lpparam) {
		systemPackages.add(Helpers.modulePkg);
		//systemPackages.add("com.miui.packageinstaller");


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