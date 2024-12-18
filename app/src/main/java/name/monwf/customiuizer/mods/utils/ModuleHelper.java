package name.monwf.customiuizer.mods.utils;

import static name.monwf.customiuizer.mods.GlobalActions.ACTION_PREFIX;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.findClass;
import static name.monwf.customiuizer.mods.utils.XposedHelpers.log;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.MiuiMultiWindowUtils;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.github.libxposed.api.XposedModuleInterface;
import miui.app.MiuiFreeFormManager;
import miui.process.ForegroundInfo;
import miui.process.ProcessManager;
import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker;
import name.monwf.customiuizer.mods.utils.HookerClassHelper.MethodHook;
import name.monwf.customiuizer.utils.Helpers;


public class ModuleHelper {
    public static final String NOT_EXIST_SYMBOL = "ObjectFieldNotExist";

    public static final String prefsName = "customiuizer_prefs";

    public static String currentPackageName;

    @SuppressLint("StaticFieldLeak")
    private static Context mModuleContext = null;

    private final static int viewInfoTag = ResourceHooks.getFakeResId("view_info_tag");

    static HashSet<PreferenceObserver> prefObservers = new HashSet<PreferenceObserver>();

    static Class<?> ActivityThreadClass;

    static {
        ActivityThreadClass = null;
    }

    public static void printCallStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement el: stackTrace)
            if (el != null) {
                log(el.getClassName() + " $$ " + el.getMethodName());
            }
    }

    public static CustomMethodUnhooker hookMethod(Method method, MethodHook callback) {
        try {
            return XposedHelpers.doHookMethod(method, callback);
        } catch (Throwable t) {
            log("Failed to hook " + method.getName() + " method");
            return null;
        }
    }

    public static CustomMethodUnhooker findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {
            log("Failed to hook " + methodName + " method in " + className);
            return null;
        }
    }

    /**
     * Calls an instance or static method of the given object silently.
     *
     * @param obj        The object instance. A class reference is not sufficient!
     * @param methodName The method name.
     * @param args       The arguments for the method call.
     */
    public static Object callMethodSilently(Object obj, String methodName, Object... args) {
        try {
            return XposedHelpers.callMethod(obj, methodName, args);
        } catch (Throwable e) {
            XposedHelpers.log(e);
            return NOT_EXIST_SYMBOL;
        }
    }

    public static CustomMethodUnhooker findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {
            log("Failed to hook " + methodName + " method in " + clazz.getCanonicalName());
            return null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean findAndHookMethodSilently(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean findAndHookMethodSilently(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static CustomMethodUnhooker findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
        } catch (Throwable t) {
            log("Failed to hook constructor in " + className);
            return null;
        }
    }

    public static void hookAllConstructors(String className, ClassLoader classLoader, MethodHook callback) {
        try {
            Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
            if (hookClass == null || XposedHelpers.hookAllConstructors(hookClass, callback).size() == 0)
                log("Failed to hook " + className + " constructor");
        } catch (Throwable t) {
            log(t);
        }
    }

    public static void hookAllConstructors(Class<?> hookClass, MethodHook callback) {
        try {
            if (XposedHelpers.hookAllConstructors(hookClass, callback).size() == 0)
                log("Failed to hook " + hookClass.getCanonicalName() + " constructor");
        } catch (Throwable t) {
            log(t);
        }
    }

    public static void hookAllMethods(String className, ClassLoader classLoader, String methodName, MethodHook callback) {
        try {
            Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
            if (hookClass == null || XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() == 0)
                log("Failed to hook " + methodName + " method in " + className);
        } catch (Throwable t) {
            log(t);
        }
    }

    public static void hookAllMethods(Class<?> hookClass, String methodName, MethodHook callback) {
        try {
            if (XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() == 0)
                log("Failed to hook " + methodName + " method in " + hookClass.getCanonicalName());
        } catch (Throwable t) {
            log(t);
        }
    }

    public static Object proxySystemProperties(String method, String prop, String val, ClassLoader classLoader) {
        return XposedHelpers.callStaticMethod(XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader),
            method, prop, val);
    }

    public static Object proxySystemProperties(String method, String prop, int val, ClassLoader classLoader) {
        return XposedHelpers.callStaticMethod(XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader),
            method, prop, val);
    }

    public static boolean hookAllMethodsSilently(String className, ClassLoader classLoader, String methodName, MethodHook callback) {
        try {
            Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
            return hookClass != null && XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() > 0;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hookAllMethodsSilently(Class<?> hookClass, String methodName, MethodHook callback) {
        try {
            return hookClass != null && XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() > 0;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Object getStaticObjectFieldSilently(Class <?> clazz, String fieldName) {
        try {
            return XposedHelpers.getStaticObjectField(clazz, fieldName);
        } catch (Throwable t) {
            return NOT_EXIST_SYMBOL;
        }
    }

    public static Object getObjectFieldSilently(Object obj, String fieldName) {
        try {
            return XposedHelpers.getObjectField(obj, fieldName);
        } catch (Throwable t) {
            return NOT_EXIST_SYMBOL;
        }
    }

    public static int getUserId() {
        return (int)XposedHelpers.callStaticMethod(UserHandle.class, "getUserId", Process.myUid());
    }


    public static Context findContext() {
        Context context = null;
        try {
            if (ActivityThreadClass == null) {
                ActivityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", null);
            }
            context = (Application)XposedHelpers.callStaticMethod(ActivityThreadClass, "currentApplication");
            if (context == null) {
                Object currentActivityThread = XposedHelpers.callStaticMethod(ActivityThreadClass, "currentActivityThread");
                if (currentActivityThread != null) context = (Context)XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
            }
        } catch (Throwable ignore) {}
        return context;
    }

    public static Context findContext(XposedModuleInterface.PackageLoadedParam lpparam) {
        Context context = null;
        try {
            context = (Application)XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", lpparam.getClassLoader()), "currentApplication");
            if (context == null) {
                Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
                if (currentActivityThread != null) context = (Context)XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
            }
        } catch (Throwable ignore) {}
        return context;
    }

    public static String stringifyBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder string = new StringBuilder("Bundle{");
        for (String key : bundle.keySet()) {
            string.append(" ").append(key).append(" -> ").append(bundle.get(key)).append(";");
        }
        string.append(" }Bundle");
        return string.toString();
    }

    public static long getNextMIUIAlarmTime(Context context) {
        long nextTime = 0;
        try {
            nextTime = Settings.Global.getLong(context.getContentResolver(), "next_alarm_clock_long");
        } catch (Settings.SettingNotFoundException e) {}
        return nextTime;
    }
    public static void openAppInfo(Context context, String pkg, int user) {
        try {
            Intent intent = new Intent("miui.intent.action.APP_MANAGER_APPLICATION_DETAIL");
            intent.setPackage("com.miui.securitycenter");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.putExtra("package_name", pkg);
            if (user != 0) intent.putExtra("miui.intent.extra.USER_ID", user);
            context.startActivity(intent);
        } catch (Throwable t) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setData(Uri.parse("package:" + pkg));
                if (user != 0)
                    XposedHelpers.callMethod(context, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
                else
                    context.startActivity(intent);
            } catch (Throwable t2) {
                log(t2);
            }
        }
    }

    public interface PreferenceObserver {
        void onChange(String key);
    }

    public static void observePreferenceChange(PreferenceObserver prefObserver) {
        prefObservers.add(prefObserver);
    }

    public static void handlePreferenceChanged(@Nullable String key) {
        for (PreferenceObserver prefObserver:prefObservers) {
            prefObserver.onChange(key);
        }
    }

    public static synchronized Context getModuleContext(Context context) throws Throwable {
        return getModuleContext(context, null);
    }

    public static synchronized Context getModuleContext(Context context, Configuration config) throws Throwable {
        if (mModuleContext == null) {
            mModuleContext = context.createPackageContext(Helpers.modulePkg, Context.CONTEXT_IGNORE_SECURITY);
        }
        return config == null ? mModuleContext : mModuleContext.createConfigurationContext(config);
    }

    public static synchronized Resources getModuleRes(Context context) throws Throwable {
        Configuration config = context.getResources().getConfiguration();
        Context moduleContext = getModuleContext(context, config);
        return moduleContext.getResources();
    }

    public static Object getDepInstance(ClassLoader classLoader, String className) {
        Class<?> DependencyClass = findClass("com.android.systemui.Dependency", classLoader);
        return XposedHelpers.callStaticMethod(DependencyClass, "get", findClass(className, classLoader));
    }

    public static Object getViewInfo(View view, String key) {
        Object info = view.getTag(viewInfoTag);
        if (info == null) {
            return null;
        }
        HashMap<String, Object> viewInfo;
        viewInfo = (HashMap<String, Object>) info;
        return viewInfo.get(key);
    }
    public static void setViewInfo(View view, String key, Object value) {
        Object info = view.getTag(viewInfoTag);
        HashMap<String, Object> viewInfo;
        if (info == null) {
            viewInfo = new HashMap<String, Object>();
            view.setTag(viewInfoTag, viewInfo);
        }
        else {
            viewInfo = (HashMap<String, Object>) info;
        }
        viewInfo.put(key, value);
    }
    public static Bundle getFreeformOptions(Context mContext, String pkgName, PendingIntent pendingIntent, boolean ignoreCheck) throws PendingIntent.CanceledException {
        if (!ignoreCheck) {
            ForegroundInfo foregroundInfo = ProcessManager.getForegroundInfo();
            if (foregroundInfo != null) {
                String topPackage = foregroundInfo.mForegroundPackageName;
                if (pkgName.equals(topPackage)) {
                    return null;
                }
            }
            List<MiuiFreeFormManager.MiuiFreeFormStackInfo> freeFormStackInfoList = MiuiFreeFormManager.getAllFreeFormStackInfosOnDisplay(mContext.getDisplay() != null ? mContext.getDisplay().getDisplayId() : 0);
            int freeFormCount = 0;
            if (freeFormStackInfoList != null) {
                freeFormCount = freeFormStackInfoList.size();
            }
            if (freeFormCount == 2) return null;
            for (MiuiFreeFormManager.MiuiFreeFormStackInfo rootTaskInfo : freeFormStackInfoList) {
                if (pkgName.equals(rootTaskInfo.packageName)) return null;
            }
        }
        if (!pendingIntent.isActivity()) {
            Intent bIntent = new Intent(ACTION_PREFIX + "SetFreeFormPackage");
            bIntent.putExtra("package", pkgName);
            bIntent.setPackage("android");
            mContext.sendBroadcast(bIntent);
        }
        ActivityOptions options = MiuiMultiWindowUtils.getActivityOptions(mContext, pkgName, true, false);
        if (options != null) {
            XposedHelpers.callMethod(options, "setFreeformAnimation", false);
        }
        return options != null ? options.toBundle() : null;
    }
    public static Intent getFreeformIntent(String pkgName) {
        Intent intent = new Intent();
        if (!"com.tencent.tim".equals(pkgName)) {
            XposedHelpers.callMethod(intent, "addFlags", 134217728);
            XposedHelpers.callMethod(intent, "addFlags", 268435456);
            XposedHelpers.callMethod(intent, "addMiuiFlags", 256);
        }
        return intent;
    }
    private static int thermalId = -1;
    public static int getCPUThermalId() {
        if (thermalId != -1) return thermalId;
        for (var i = 2;i < 40;i = i + 2) {
            try {
                RandomAccessFile cpuReader = new RandomAccessFile("/sys/devices/virtual/thermal/thermal_zone" + i + "/type", "r");
                String sensorType = cpuReader.readLine();
                cpuReader.close();
                if (sensorType.startsWith("cpu-") || sensorType.startsWith("cpu_big")) {
                    thermalId = i;
                    break;
                }
            } catch (Throwable ign) {}
        }
        return thermalId;
    }

    public static void replacePkgAndFrameworkValue(String pkg, String type, String name, Object resValue) {
        if (!"android".equals(pkg)) {
            MainModule.resHooks.setThemeValueReplacement("android", type, name, resValue);
        }
        MainModule.resHooks.setThemeValueReplacement(pkg, type, name, resValue);
    }
    public static Object getObjectFieldByPath(Object target, String path) {
        if (target == null) return null;
        String[] pathes = path.split("\\.");
        for (String field:pathes) {
            target = getObjectFieldSilently(target, field);
            if (NOT_EXIST_SYMBOL.equals(target)) {
                return NOT_EXIST_SYMBOL;
            }
        }
        return target;
    }
}
