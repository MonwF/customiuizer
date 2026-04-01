package name.monwf.customiuizer.mods;

import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.libxposed.api.XposedModuleInterface;
import name.monwf.customiuizer.mods.utils.XposedHelpers;
import name.monwf.customiuizer.utils.Helpers;


public class PackagePermissions {


	public static void hook(XposedModuleInterface.SystemServerLoadedParam lpparam) {
		ArrayList<String> systemPackages = new ArrayList<String>();
		systemPackages.add(Helpers.modulePkg);

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