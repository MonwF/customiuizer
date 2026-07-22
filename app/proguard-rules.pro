# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onModuleLoaded(...);
    public void onPackageReady(...);
    public void onSystemServerStarting(...);
}

-keep,allowoptimization class name.monwf.customiuizer.mods.utils.HookerClassHelper$MethodHook {
    <methods>;
}

-keepnames class name.monwf.customiuizer.GateWayLauncher

# AndroidX Startup runs in the normal app process before LSPosed injects its API.
# Keep Startup and every manifest-discovered initializer out of R8 class merging,
# otherwise verifier dependencies from Xposed-only classes can leak into startup.
-keep class androidx.startup.** { *; }
-keep class * implements androidx.startup.Initializer { *; }

# Obfuscation
-repackageclasses
-allowaccessmodification

-dontwarn kotlin.jvm.internal.SourceDebugExtension
-dontwarn android.**
-dontwarn miui.**
# -dontnote **
