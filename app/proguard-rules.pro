-verbose

# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onModuleLoaded(...);
    public void onPackageReady(...);
    public void onSystemServerStarting(...);
}

-keepnames class name.monwf.customiuizer.GateWayLauncher

# Obfuscation
-repackageclasses
-allowaccessmodification

#-dontwarn android.app.ActivityTaskManager$RootTaskInfo, android.util.Singleton
-dontwarn kotlin.jvm.internal.SourceDebugExtension
-dontwarn android.**
-dontwarn android.view.**
-dontwarn miui.**
# -dontnote **
