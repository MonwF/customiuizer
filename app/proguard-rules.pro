-verbose

# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onPackageLoaded(...);
    public void onSystemServerLoaded(...);
}

-keep,allowoptimization,allowobfuscation @io.github.libxposed.api.annotations.* class * {
    @io.github.libxposed.api.annotations.BeforeInvocation <methods>;
    @io.github.libxposed.api.annotations.AfterInvocation <methods>;
}

-keep,allowoptimization class name.monwf.customiuizer.mods.utils.HookerClassHelper$MethodHook {
    <methods>;
}

-keepnames class name.monwf.customiuizer.GateWayLauncher

# Obfuscation
-repackageclasses
-allowaccessmodification

-dontwarn android.app.ActivityTaskManager$RootTaskInfo, android.util.Singleton
# -dontnote **
