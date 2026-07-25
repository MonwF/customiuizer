# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onModuleLoaded(...);
    public void onPackageReady(...);
    public void onSystemServerStarting(...);
}

# Hook callbacks are loaded only inside LSPosed target processes. Prevent R8 from
# merging them into ordinary app/startup classes, which would make the settings
# app resolve the compileOnly libxposed API during process initialization.
-keep,allowobfuscation class * implements io.github.libxposed.api.XposedInterface$Hooker { *; }

-keepnames class tv.withaibuild.customiuizer.GateWayLauncher

# BuildConfig is referenced for APPLICATION_ID/version checks and must survive obfuscation.
-keep class tv.withaibuild.customiuizer.BuildConfig { *; }

# Keep public static entry points and fields of hook classes so R8 does not inline/remove
# methods that are only reached from the settings app or invoked by class-name strings.
-keepclassmembers class tv.withaibuild.customiuizer.mods.** {
    public static <methods>;
    public <fields>;
}

# Pure-Java utilities are used in both the module and UI; keep their public APIs.
-keep class tv.withaibuild.customiuizer.utils.PrefMap { *; }
-keep class tv.withaibuild.customiuizer.utils.Helpers { *; }
-keep class tv.withaibuild.customiuizer.utils.AppHelper { *; }

# Obfuscation
-repackageclasses
-allowaccessmodification

-dontwarn kotlin.jvm.internal.SourceDebugExtension
-dontwarn android.**
-dontwarn miui.**
-dontnote android.**, miui.**, com.android.**
# -dontnote **
