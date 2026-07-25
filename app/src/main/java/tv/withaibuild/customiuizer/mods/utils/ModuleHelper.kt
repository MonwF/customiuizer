package tv.withaibuild.customiuizer.mods.utils

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import android.util.MiuiMultiWindowUtils
import android.view.View
import io.github.libxposed.api.XposedModuleInterface
import miui.app.MiuiFreeFormManager
import miui.process.ForegroundInfo
import miui.process.ProcessManager
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.utils.Helpers
import java.io.RandomAccessFile
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class ModuleHelper private constructor() {

    interface PreferenceObserver {
        fun onChange(key: String?)
    }

    companion object {
        const val NOT_EXIST_SYMBOL = "ObjectFieldNotExist"
        const val prefsName = "customiuizer_prefs"

        @JvmField
        var currentPackageName: String? = null

        @SuppressLint("StaticFieldLeak")
        @JvmField
        var mModuleContext: Context? = null

        @SuppressLint("StaticFieldLeak")
        @JvmField
        var mCachedContext: Context? = null

        @JvmField
        var cachedModuleRes: Resources? = null

        @JvmField
        var cachedModuleConfig: Configuration? = null

        private val viewInfoTag = ResourceHooks.getFakeResId("view_info_tag")
        private val prefObservers = CopyOnWriteArraySet<PreferenceObserver>()
        private const val PREF_OBSERVER_FIELD = "customiuizer_prefObserver"

        @JvmField
        internal var ActivityThreadClass: Class<*>? = null

        private val depInstanceCache = ConcurrentHashMap<Class<*>, Any?>()

        private var DependencyClass: Class<*>? = null

        private var DependencyGetMethod: Method? = null

        private var thermalId = -1

        @JvmStatic
        fun printCallStack() {
            val stackTrace = Thread.currentThread().stackTrace
            for (el in stackTrace) {
                if (el != null) {
                    XposedHelpers.log(el.className + " $$ " + el.methodName)
                }
            }
        }

        @JvmStatic
        fun hookMethod(method: Method, callback: MethodHook): CustomMethodUnhooker? {
            return try {
                XposedHelpers.doHookMethod(method, callback)
            } catch (t: Throwable) {
                XposedHelpers.log("Failed to hook " + method.name + " method")
                null
            }
        }

        @JvmStatic
        fun findAndHookMethod(className: String, classLoader: ClassLoader?, methodName: String, vararg parameterTypesAndCallback: Any?): CustomMethodUnhooker? {
            return try {
                XposedHelpers.findAndHookMethod(className, classLoader, methodName, *parameterTypesAndCallback)
            } catch (t: Throwable) {
                XposedHelpers.log("Failed to hook " + methodName + " method in " + className)
                null
            }
        }

        @JvmStatic
        fun callMethodSilently(obj: Any?, methodName: String, vararg args: Any?): Any? {
            return try {
                XposedHelpers.callMethod(obj, methodName, *args)
            } catch (e: Throwable) {
                XposedHelpers.log(e)
                NOT_EXIST_SYMBOL
            }
        }

        @JvmStatic
        fun findAndHookMethod(clazz: Class<*>, methodName: String, vararg parameterTypesAndCallback: Any?): CustomMethodUnhooker? {
            return try {
                XposedHelpers.findAndHookMethod(clazz, methodName, *parameterTypesAndCallback)
            } catch (t: Throwable) {
                XposedHelpers.log("Failed to hook " + methodName + " method in " + clazz?.canonicalName)
                null
            }
        }

        @JvmStatic
        @Suppress("UNUSED_RETURN_VALUE")
        fun findAndHookMethodSilently(className: String, classLoader: ClassLoader?, methodName: String, vararg parameterTypesAndCallback: Any?): Boolean {
            return try {
                XposedHelpers.findAndHookMethod(className, classLoader, methodName, *parameterTypesAndCallback)
                true
            } catch (t: Throwable) {
                false
            }
        }

        @JvmStatic
        @Suppress("UNUSED_RETURN_VALUE")
        fun findAndHookMethodSilently(clazz: Class<*>, methodName: String, vararg parameterTypesAndCallback: Any?): Boolean {
            return try {
                XposedHelpers.findAndHookMethod(clazz, methodName, *parameterTypesAndCallback)
                true
            } catch (t: Throwable) {
                false
            }
        }

        @JvmStatic
        fun findAndHookConstructor(className: String, classLoader: ClassLoader?, vararg parameterTypesAndCallback: Any?): CustomMethodUnhooker? {
            return try {
                XposedHelpers.findAndHookConstructor(className, classLoader, *parameterTypesAndCallback)
            } catch (t: Throwable) {
                XposedHelpers.log("Failed to hook constructor in " + className)
                null
            }
        }

        @JvmStatic
        fun hookAllConstructors(className: String, classLoader: ClassLoader?, callback: MethodHook) {
            try {
                val hookClass = XposedHelpers.findClassIfExists(className, classLoader)
                if (hookClass == null || XposedHelpers.hookAllConstructors(hookClass, callback).isEmpty()) {
                    XposedHelpers.log("Failed to hook " + className + " constructor")
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }

        @JvmStatic
        fun hookAllConstructors(hookClass: Class<*>?, callback: MethodHook) {
            try {
                if (XposedHelpers.hookAllConstructors(hookClass, callback).isEmpty()) {
                    XposedHelpers.log("Failed to hook " + hookClass?.canonicalName + " constructor")
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }

        @JvmStatic
        fun hookAllMethods(className: String, classLoader: ClassLoader?, methodName: String, callback: MethodHook) {
            try {
                val hookClass = XposedHelpers.findClassIfExists(className, classLoader)
                if (hookClass == null || XposedHelpers.hookAllMethods(hookClass, methodName, callback).isEmpty()) {
                    XposedHelpers.log("Failed to hook " + methodName + " method in " + className)
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }

        @JvmStatic
        fun hookAllMethods(hookClass: Class<*>?, methodName: String, callback: MethodHook) {
            try {
                if (XposedHelpers.hookAllMethods(hookClass, methodName, callback).isEmpty()) {
                    XposedHelpers.log("Failed to hook " + methodName + " method in " + hookClass?.canonicalName)
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }

        @JvmStatic
        fun proxySystemProperties(method: String, prop: String, value: String, classLoader: ClassLoader?): Any? {
            val sysPropClass = XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader) ?: return null
            return XposedHelpers.callStaticMethod(sysPropClass, method, prop, value)
        }

        @JvmStatic
        fun proxySystemProperties(method: String, prop: String, value: Int, classLoader: ClassLoader?): Any? {
            val sysPropClass = XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader) ?: return null
            return XposedHelpers.callStaticMethod(sysPropClass, method, prop, value)
        }

        @JvmStatic
        fun hookAllMethodsSilently(className: String, classLoader: ClassLoader?, methodName: String, callback: MethodHook): Boolean {
            return try {
                val hookClass = XposedHelpers.findClassIfExists(className, classLoader)
                hookClass != null && XposedHelpers.hookAllMethods(hookClass, methodName, callback).isNotEmpty()
            } catch (t: Throwable) {
                false
            }
        }

        @JvmStatic
        fun hookAllMethodsSilently(hookClass: Class<*>?, methodName: String, callback: MethodHook): Boolean {
            return try {
                hookClass != null && XposedHelpers.hookAllMethods(hookClass, methodName, callback).isNotEmpty()
            } catch (t: Throwable) {
                false
            }
        }

        @JvmStatic
        fun getStaticObjectFieldSilently(clazz: Class<*>, fieldName: String): Any? {
            return try {
                XposedHelpers.getStaticObjectField(clazz, fieldName)
            } catch (t: Throwable) {
                NOT_EXIST_SYMBOL
            }
        }

        @JvmStatic
        fun getObjectFieldSilently(obj: Any?, fieldName: String): Any? {
            return try {
                XposedHelpers.getObjectField(obj, fieldName)
            } catch (t: Throwable) {
                NOT_EXIST_SYMBOL
            }
        }

        @JvmStatic
        fun getUserId(): Int {
            return Process.myUid() / 100000
        }

        @JvmStatic
        fun findContext(): Context? {
            if (mCachedContext != null) return mCachedContext
            var context: Context? = null
            try {
                val atClass = ActivityThreadClass
                    ?: XposedHelpers.findClass("android.app.ActivityThread", null).also { ActivityThreadClass = it }
                context = XposedHelpers.callStaticMethod(atClass, "currentApplication") as? Application
                if (context == null) {
                    val currentActivityThread = XposedHelpers.callStaticMethod(atClass, "currentActivityThread")
                    if (currentActivityThread != null) {
                        context = XposedHelpers.callMethod(currentActivityThread, "getSystemContext") as? Context
                    }
                }
            } catch (ignore: Throwable) {
            }
            if (context != null) mCachedContext = context
            return context
        }

        @JvmStatic
        fun findContext(lpparam: XposedModuleInterface.PackageReadyParam?): Context? {
            var context: Context? = null
            try {
                val classLoader = lpparam?.classLoader
                context = XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", classLoader),
                    "currentApplication"
                ) as? Application
                if (context == null) {
                    val currentActivityThread = XposedHelpers.callStaticMethod(
                        XposedHelpers.findClass("android.app.ActivityThread", null),
                        "currentActivityThread"
                    )
                    if (currentActivityThread != null) {
                        context = XposedHelpers.callMethod(currentActivityThread, "getSystemContext") as? Context
                    }
                }
            } catch (ignore: Throwable) {
            }
            return context
        }

        @JvmStatic
        fun stringifyBundle(bundle: Bundle?): String? {
            if (bundle == null) return null
            val string = StringBuilder("Bundle{")
            for (key in bundle.keySet()) {
                string.append(" ").append(key).append(" -> ").append(bundle.get(key)).append(";")
            }
            string.append(" }Bundle")
            return string.toString()
        }

        @JvmStatic
        fun getNextMIUIAlarmTime(context: Context): Long {
            var nextTime = 0L
            try {
                nextTime = Settings.Global.getLong(context.contentResolver, "next_alarm_clock_long")
            } catch (e: Settings.SettingNotFoundException) {
            }
            return nextTime
        }

        @JvmStatic
        fun openAppInfo(context: Context, pkg: String, user: Int) {
            try {
                val intent = Intent("miui.intent.action.APP_MANAGER_APPLICATION_DETAIL")
                intent.setPackage("com.miui.securitycenter")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                intent.putExtra("package_name", pkg)
                if (user != 0) intent.putExtra("miui.intent.extra.USER_ID", user)
                context.startActivity(intent)
            } catch (t: Throwable) {
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    intent.data = Uri.parse("package:$pkg")
                    if (user != 0) {
                        val userHandle = XposedHelpers.newInstance(UserHandle::class.java, user) as UserHandle
                        XposedHelpers.callMethod(context, "startActivityAsUser", intent, userHandle)
                    } else {
                        context.startActivity(intent)
                    }
                } catch (t2: Throwable) {
                    XposedHelpers.log(t2)
                }
            }
        }

        @JvmStatic
        fun observePreferenceChange(prefObserver: PreferenceObserver?) {
            if (prefObserver != null) prefObservers.add(prefObserver)
        }

        @JvmStatic
        fun observePreferenceChange(prefObserver: PreferenceObserver?, owner: Any?) {
            if (prefObserver == null) return
            if (owner == null) {
                observePreferenceChange(prefObserver)
                return
            }
            val old = XposedHelpers.getAdditionalInstanceField(owner, PREF_OBSERVER_FIELD)
            if (old is PreferenceObserver) {
                prefObservers.remove(old)
            }
            XposedHelpers.setAdditionalInstanceField(owner, PREF_OBSERVER_FIELD, prefObserver)
            prefObservers.add(prefObserver)
        }

        @JvmStatic
        fun removePreferenceObserver(owner: Any?) {
            val old = XposedHelpers.removeAdditionalInstanceField(owner, PREF_OBSERVER_FIELD)
            if (old is PreferenceObserver) {
                prefObservers.remove(old)
            }
        }

        @JvmStatic
        fun handlePreferenceChanged(key: String?) {
            for (prefObserver in prefObservers) {
                prefObserver.onChange(key)
            }
        }

        @JvmStatic
        @Synchronized
        @JvmOverloads
        fun getModuleContext(context: Context, config: Configuration? = null): Context {
            if (mModuleContext == null) {
                mModuleContext = context.createPackageContext(Helpers.modulePkg, Context.CONTEXT_IGNORE_SECURITY)
            }
            return if (config == null) mModuleContext!! else mModuleContext!!.createConfigurationContext(config)
        }

        @JvmStatic
        @Synchronized
        fun getModuleRes(context: Context): Resources {
            val newConfig = context.resources.configuration
            if (cachedModuleRes != null && cachedModuleConfig == newConfig) {
                return cachedModuleRes!!
            }
            val config = Configuration(newConfig)
            val moduleContext = getModuleContext(context, config)
            cachedModuleRes = moduleContext.resources
            cachedModuleConfig = config
            return cachedModuleRes!!
        }

        @JvmStatic
        fun getDepInstance(classLoader: ClassLoader?, className: String): Any? {
            return try {
                val clazz = XposedHelpers.findClass(className, classLoader)
                val cached = depInstanceCache[clazz]
                if (cached != null) return cached

                if (DependencyClass == null || DependencyClass?.classLoader != classLoader) {
                    DependencyClass = XposedHelpers.findClass("com.android.systemui.Dependency", classLoader)
                    DependencyGetMethod = DependencyClass?.getDeclaredMethod("get", Class::class.java)
                    DependencyGetMethod?.isAccessible = true
                }
                val instance = DependencyGetMethod?.invoke(null, clazz)
                if (instance != null) depInstanceCache[clazz] = instance
                instance
            } catch (t: Throwable) {
                XposedHelpers.log(t)
                null
            }
        }

        @JvmStatic
        fun getViewInfo(view: View?, key: String): Any? {
            if (view == null) return null
            val info = view.getTag(viewInfoTag)
            if (info == null) return null
            @Suppress("UNCHECKED_CAST")
            val viewInfo = info as HashMap<String, Any?>
            return viewInfo[key]
        }

        @JvmStatic
        fun setViewInfo(view: View?, key: String, value: Any?) {
            if (view == null) return
            val info = view.getTag(viewInfoTag)
            val viewInfo: HashMap<String, Any?> = if (info == null) {
                val newInfo = HashMap<String, Any?>()
                view.setTag(viewInfoTag, newInfo)
                newInfo
            } else {
                @Suppress("UNCHECKED_CAST")
                info as HashMap<String, Any?>
            }
            viewInfo[key] = value
        }

        @JvmStatic
        @Throws(PendingIntent.CanceledException::class)
        fun getFreeformOptions(mContext: Context, pkgName: String, pendingIntent: PendingIntent, ignoreCheck: Boolean): Bundle? {
            if (!ignoreCheck) {
                val foregroundInfo: ForegroundInfo? = ProcessManager.getForegroundInfo()
                if (foregroundInfo != null) {
                    val topPackage = foregroundInfo.mForegroundPackageName
                    if (pkgName == topPackage) return null
                }
                val freeFormStackInfoList = MiuiFreeFormManager.getAllFreeFormStackInfosOnDisplay(
                    mContext.display?.displayId ?: 0
                )
                val freeFormCount = freeFormStackInfoList?.size ?: 0
                if (freeFormCount == 2) return null
                freeFormStackInfoList?.forEach { rootTaskInfo ->
                    if (pkgName == rootTaskInfo.packageName) return null
                }
            }
            if (!pendingIntent.isActivity) {
                val bIntent = Intent(tv.withaibuild.customiuizer.mods.GlobalActions.ACTION_PREFIX + "SetFreeFormPackage")
                bIntent.putExtra("package", pkgName)
                bIntent.setPackage("android")
                mContext.sendBroadcast(bIntent)
            }
            val options: ActivityOptions? = MiuiMultiWindowUtils.getActivityOptions(mContext, pkgName, true, false)
            if (options != null) {
                XposedHelpers.callMethod(options, "setFreeformAnimation", false)
            }
            return options?.toBundle()
        }

        @JvmStatic
        fun getFreeformIntent(pkgName: String): Intent {
            val intent = Intent()
            if (pkgName != "com.tencent.tim") {
                XposedHelpers.callMethod(intent, "addFlags", 134217728)
                XposedHelpers.callMethod(intent, "addFlags", 268435456)
                XposedHelpers.callMethod(intent, "addMiuiFlags", 256)
            }
            return intent
        }

        @JvmStatic
        fun getCPUThermalId(): Int {
            if (thermalId != -1) return thermalId
            for (i in 2 until 40 step 2) {
                try {
                    RandomAccessFile("/sys/devices/virtual/thermal/thermal_zone$i/type", "r").use { cpuReader ->
                        val sensorType = cpuReader.readLine()
                        if (sensorType != null && (sensorType.startsWith("cpu-") || sensorType.startsWith("cpu_big"))) {
                            thermalId = i
                        }
                    }
                } catch (ign: Throwable) {
                }
            }
            return thermalId
        }

        @JvmStatic
        fun replacePkgAndFrameworkValue(pkg: String, type: String, name: String, resValue: Any?) {
            if (pkg != "android") {
                MainModule.resHooks.setThemeValueReplacement("android", type, name, resValue)
            }
            MainModule.resHooks.setThemeValueReplacement(pkg, type, name, resValue)
        }

        @JvmStatic
        fun getObjectFieldByPath(target: Any?, path: String): Any? {
            if (target == null) return null
            var obj: Any? = target
            for (field in path.split('.')) {
                obj = getObjectFieldSilently(obj, field)
                if (obj == NOT_EXIST_SYMBOL) {
                    return NOT_EXIST_SYMBOL
                }
            }
            return obj
        }
    }
}
