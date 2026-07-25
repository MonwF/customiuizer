package tv.withaibuild.customiuizer.mods

import io.github.libxposed.api.XposedModuleInterface
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.Helpers

object PackagePermissions {

    private val systemPackages = ArrayList<String>()

    @JvmStatic
    fun hook(lpparam: XposedModuleInterface.SystemServerStartingParam) {
        systemPackages.add(Helpers.modulePkg)

        try {
            val dpgpiClass = XposedHelpers.findClass(
                "com.android.server.pm.MiuiDefaultPermissionGrantPolicy",
                lpparam.classLoader
            )
            @Suppress("UNCHECKED_CAST")
            val miuiSystemApps = XposedHelpers.getStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS") as Array<String>
            val mySystemApps = ArrayList(miuiSystemApps.asList())
            mySystemApps.addAll(systemPackages)
            XposedHelpers.setStaticObjectField(dpgpiClass, "MIUI_SYSTEM_APPS", mySystemApps.toTypedArray())
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }
}
