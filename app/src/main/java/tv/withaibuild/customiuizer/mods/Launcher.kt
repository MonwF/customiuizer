package tv.withaibuild.customiuizer.mods

import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetProviderInfo
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.os.UserHandle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Set
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedInterface
import miui.process.ForegroundInfo
import miui.process.ProcessManager
import miui.security.SecurityManager
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.ShakeManager
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.Helpers

object Launcher {

    private var mDetectorHorizontal: GestureDetector? = null

    @JvmStatic
    fun HomescreenSwipesHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onVerticalGesture", Int::class.javaPrimitiveType!!, MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {
                    if (XposedHelpers.callMethod(thisObject, "isInNormalEditingMode") as Boolean) {
                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        return XposedHelpers.proceedOrThrow(chain, args, throwable)
                    }
                    var key: String? = null
                    val helperContext = (thisObject as ViewGroup).context
                    var numOfFingers = 1
                    if (args[1] != null) numOfFingers = (args[1] as MotionEvent).pointerCount
                    when (args[0] as Int) {
                        11 -> {
                            key = if (numOfFingers == 1) "launcher_swipedown" else if (numOfFingers == 2) "launcher_swipedown2" else key
                            if (GlobalActions.handleAction(helperContext, key)) { skipped = true; result = true; throwable = null }
                        }
                        10 -> {
                            key = if (numOfFingers == 1) "launcher_swipeup" else if (numOfFingers == 2) "launcher_swipeup2" else key
                            if (GlobalActions.handleAction(helperContext, key)) { skipped = true; result = true; throwable = null }
                        }
                    }
                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.uioverrides.StatusBarSwipeController", lpparam.classLoader, "canInterceptTouch", MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) { skipped = true; result = false; throwable = null }
                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.uioverrides.AllAppsSwipeController", lpparam.classLoader, "canInterceptTouch", MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null }
                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        // content_center, global_search, notification_bar
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.classLoader, "getPullDownGesture", Context::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) { result = "no_action"; throwable = null }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        // content_center, global_search
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.allapps.LauncherMode", lpparam.classLoader, "getSlideUpGesture", Context::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = "no_action"; throwable = null }
                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        if (ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "isGlobalSearchEnable", Context::class.java, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var skipped = false
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null }
                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })) {
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.classLoader, "isTopSearchEnable", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var skipped = false
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        if (MainModule.mPrefs.getInt("launcher_swipedown_action", 1) > 1) { skipped = true; result = false; throwable = null }
                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.search.SearchEdgeLayout", lpparam.classLoader, "isBottomGlobalSearchEnable", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var skipped = false
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null }
                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
            ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "isGlobalSearchBottomEffectEnable", Context::class.java, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var skipped = false
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null }
                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
        } else if (!ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "allowedSlidingUpToStartGolbalSearch", Context::class.java, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var skipped = false
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        if (MainModule.mPrefs.getInt("launcher_swipeup_action", 1) > 1) { skipped = true; result = false; throwable = null }
                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })) {
            if (lpparam.packageName == "com.miui.home") XposedHelpers.log("HomescreenSwipesHook", "Cannot disable swipe up search")
        }
    }

    @JvmStatic
    fun HotSeatSwipesHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.overlay.assistant.AssistantOverlaySwipeController", lpparam.classLoader, "canInterceptTouch", MotionEvent::class.java, object : MethodHook() {
            private var mHotHeatTouchRect: Rect? = null
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()
                    val args = XposedHelpers.getArgsArray(chain)

                    val canInterceptTouch = result as Boolean
                    if (canInterceptTouch) {
                        val rect = mHotHeatTouchRect ?: Rect().also { mHotHeatTouchRect = it }
                        val mLauncher = XposedHelpers.getObjectField(thisObject, "mLauncher")
                        val mHotSeats = XposedHelpers.callMethod(mLauncher, "getHotSeats") as FrameLayout
                        mHotSeats.getHitRect(rect)
                        val motionEvent = args[0] as MotionEvent
                        if (rect.contains(motionEvent.x.toInt(), motionEvent.y.toInt())) {
                            result = false
                            throwable = null
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.hotseats.HotSeats", lpparam.classLoader, "dispatchTouchEvent", MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val ev = args[0] as MotionEvent?
                    if (ev == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }

                    val hotSeat = thisObject as ViewGroup
                    val helperContext = hotSeat.context
                    if (mDetectorHorizontal == null) mDetectorHorizontal = GestureDetector(helperContext, SwipeListenerHorizontal(hotSeat))
                    mDetectorHorizontal?.onTouchEvent(ev)

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private class SwipeListenerHorizontal(cellLayout: Any) : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_MIN_DISTANCE_HORIZ: Int
        private val SWIPE_THRESHOLD_VELOCITY: Int
        val helperContext: Context = (cellLayout as ViewGroup).context

        init {
            val density = helperContext.resources.displayMetrics.density
            SWIPE_MIN_DISTANCE_HORIZ = Math.round(75 * density)
            SWIPE_THRESHOLD_VELOCITY = Math.round(33 * density)
        }

        override fun onDown(e: MotionEvent): Boolean {
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null) return false

            if (e2.x - e1.x > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                return GlobalActions.handleAction(helperContext, "launcher_swiperight")

            if (e1.x - e2.x > SWIPE_MIN_DISTANCE_HORIZ && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                return GlobalActions.handleAction(helperContext, "launcher_swipeleft")

            return false
        }
    }

    @JvmStatic
    fun ShakeHook(lpparam: PackageReadyParam) {
        val shakeMgrKey = "MIUIZER_SHAKE_MGR"

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onResume", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    var shakeMgr = XposedHelpers.getAdditionalInstanceField(thisObject, shakeMgrKey) as ShakeManager?
                    if (shakeMgr == null) {
                        shakeMgr = ShakeManager(thisObject as Context)
                        XposedHelpers.setAdditionalInstanceField(thisObject, shakeMgrKey, shakeMgr)
                    }
                    val launcherActivity = thisObject as Activity
                    val sensorMgr = launcherActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    shakeMgr.reset()
                    sensorMgr.registerListener(shakeMgr, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onPause", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    if (XposedHelpers.getAdditionalInstanceField(thisObject, shakeMgrKey) == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val launcherActivity = thisObject as Activity
                    val sensorMgr = launcherActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    sensorMgr.unregisterListener(XposedHelpers.getAdditionalInstanceField(thisObject, shakeMgrKey) as ShakeManager)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private fun modifyTitle(thisObject: Any) {
        val isApplicatoin = XposedHelpers.callMethod(thisObject, "isApplicatoin") as Boolean
        if (!isApplicatoin) return
        val pkgName = XposedHelpers.callMethod(thisObject, "getPackageName") as String
        val actName = XposedHelpers.callMethod(thisObject, "getClassName") as String
        val user = XposedHelpers.getObjectField(thisObject, "user") as UserHandle
        val newTitle = MainModule.mPrefs.getString("launcher_renameapps_list:" + pkgName + "|" + actName + "|" + user.hashCode(), "")
        if (!TextUtils.isEmpty(newTitle)) XposedHelpers.setObjectField(thisObject, "mLabel", newTitle)
    }

    @JvmStatic
    fun NoClockHideHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "updateStatusBarClock", Long::class.javaPrimitiveType!!, HookerClassHelper.DO_NOTHING)
    }

    @JvmStatic
    fun RenameShortcutsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                        override fun onChange(key: String?) {
                            try {
                                if (key == null || !key.contains("pref_key_launcher_renameapps_list")) return
                                val newTitle = MainModule.mPrefs.getString(key, "")
                                var mAllLoadedApps: HashSet<*>? = null
                                if (XposedHelpers.findFieldIfExists(thisObject.javaClass, "mAllLoadedShortcut") != null)
                                    mAllLoadedApps = XposedHelpers.getObjectField(thisObject, "mAllLoadedShortcut") as? HashSet<*>
                                else if (XposedHelpers.findFieldIfExists(thisObject.javaClass, "mAllLoadedApps") != null)
                                    mAllLoadedApps = XposedHelpers.getObjectField(thisObject, "mAllLoadedApps") as? HashSet<*>
                                val act = thisObject as Activity
                                if (mAllLoadedApps != null)
                                    for (shortcut in mAllLoadedApps) {
                                        val shortcutObj = shortcut ?: continue
                                        val isApplicatoin = XposedHelpers.callMethod(shortcutObj, "isApplicatoin") as Boolean
                                        if (!isApplicatoin) continue
                                        val pkgName = XposedHelpers.callMethod(shortcutObj, "getPackageName") as String
                                        val actName = XposedHelpers.callMethod(shortcutObj, "getClassName") as String
                                        val user = XposedHelpers.getObjectField(shortcutObj, "user") as UserHandle
                                        if (("pref_key_launcher_renameapps_list:" + pkgName + "|" + actName + "|" + user.hashCode()) == key) {
                                            val newStr: CharSequence? = if (TextUtils.isEmpty(newTitle)) XposedHelpers.getAdditionalInstanceField(shortcutObj, "mLabelOrig") as? CharSequence else newTitle
                                            XposedHelpers.setObjectField(shortcutObj, "mLabel", newStr)

                                            act.runOnUiThread {
                                                if (lpparam.packageName == "com.miui.home") {
                                                    XposedHelpers.callMethod(shortcutObj, "updateBuddyIconView", act)
                                                } else {
                                                    val buddyIconView = XposedHelpers.callMethod(shortcutObj, "getBuddyIconView")
                                                    if (buddyIconView != null) XposedHelpers.callMethod(buddyIconView, "updateInfo", thisObject, shortcutObj)
                                                }
                                            }
                                            break
                                        }
                                    }
                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                        }
                    }, thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onDestroy", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    ModuleHelper.removePreferenceObserver(chain.getThisObject())
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllConstructors("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()
                    val args = XposedHelpers.getArgsArray(chain)

                    XposedHelpers.setAdditionalInstanceField(thisObject, "mLabelOrig", XposedHelpers.getObjectField(thisObject, "mLabel"))
                    if (args.size > 0) modifyTitle(thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "loadToggleInfo", Context::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    XposedHelpers.setAdditionalInstanceField(thisObject, "mLabelOrig", XposedHelpers.getObjectField(thisObject, "mLabel"))
                    modifyTitle(thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "setLabelAndUpdateDB", CharSequence::class.java, Context::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()
                    val args = XposedHelpers.getArgsArray(chain)

                    XposedHelpers.setAdditionalInstanceField(thisObject, "mLabelOrig", args[0])
                    modifyTitle(thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ShortcutInfo", lpparam.classLoader, "load", Context::class.java, Cursor::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    modifyTitle(thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethodsSilently("com.miui.home.launcher.BaseAppInfo", lpparam.classLoader, "resetTitle", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    modifyTitle(thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun CloseFolderOnLaunchHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "launch", "com.miui.home.launcher.ShortcutInfo", View::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) != 2) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mHasLaunchedAppFromFolder = XposedHelpers.getBooleanField(thisObject, "mHasLaunchedAppFromFolder")
                    if (mHasLaunchedAppFromFolder) XposedHelpers.callMethod(thisObject, "closeFolder")

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun FSGesturesHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "usingFsGesture", HookerClassHelper.returnConstant(true))

        ModuleHelper.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader, "createAndAddNavStubView", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    val fsg = XposedHelpers.getAdditionalStaticField(
                        XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader),
                        "REAL_FORCE_FSG_NAV_BAR"
                    ) as Boolean
                    if (!fsg) { skipped = true; result = null; throwable = null }
                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethodSilently("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader, "updateFsgWindowState", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val fsg = XposedHelpers.getAdditionalStaticField(
                        XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader),
                        "REAL_FORCE_FSG_NAV_BAR"
                    ) as Boolean
                    if (fsg) { return XposedHelpers.throwOrReturn(throwable, result) }

                    val mNavStubView = XposedHelpers.getObjectField(thisObject, "mNavStubView")
                    val mWindowManager = XposedHelpers.getObjectField(thisObject, "mWindowManager")
                    if (mWindowManager != null && mNavStubView != null) {
                        XposedHelpers.callMethod(mWindowManager, "removeView", mNavStubView)
                        XposedHelpers.setObjectField(thisObject, "mNavStubView", null)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethodSilently("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.classLoader, "getGlobalBoolean", ContentResolver::class.java, String::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    if (args[1] != "force_fsg_nav_bar") { return XposedHelpers.throwOrReturn(throwable, result) }

                    for (el in Thread.currentThread().stackTrace) {
                        if ("com.miui.home.recents.BaseRecentsImpl" == el.className) {
                            XposedHelpers.setAdditionalStaticField(
                                XposedHelpers.findClass("com.miui.home.recents.BaseRecentsImpl", lpparam.classLoader),
                                "REAL_FORCE_FSG_NAV_BAR",
                                result
                            )
                            result = true
                            throwable = null
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.classLoader, "onTouchEvent", MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val event = args[0] as MotionEvent
                    if (event.action != MotionEvent.ACTION_DOWN) {
                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        return XposedHelpers.proceedOrThrow(chain, args, throwable)
                    }
                    val foregroundInfo = ProcessManager.getForegroundInfo()
                    if (foregroundInfo != null) {
                        val pkgName = foregroundInfo.mForegroundPackageName
                        if (pkgName != null && MainModule.mPrefs.getStringSet("controls_fsg_horiz_apps").contains(pkgName)) { skipped = true; result = false; throwable = null }
                    }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    class DoubleTapController(context: Context, actionKey: String) {
        private val MAX_DURATION = 500L
        private var mActionDownRawX: Float = 0f
        private var mActionDownRawY: Float = 0f
        private var mClickCount: Int = 0
        val mContext: Context = context
        private val mActionKey: String = actionKey
        private var mFirstClickRawX: Float = 0f
        private var mFirstClickRawY: Float = 0f
        private var mLastClickTime: Long = 0L
        private var mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop * 2

        fun isDoubleTapEvent(motionEvent: MotionEvent): Boolean {
            val action = motionEvent.actionMasked
            if (action == MotionEvent.ACTION_DOWN) {
                mActionDownRawX = motionEvent.rawX
                mActionDownRawY = motionEvent.rawY
                return false
            } else if (action != MotionEvent.ACTION_UP) {
                return false
            } else {
                val rawX = motionEvent.rawX
                val rawY = motionEvent.rawY
                if (Math.abs(rawX - mActionDownRawX) <= mTouchSlop.toFloat() && Math.abs(rawY - mActionDownRawY) <= mTouchSlop.toFloat()) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime > MAX_DURATION || rawY - mFirstClickRawY > mTouchSlop.toFloat() || rawX - mFirstClickRawX > mTouchSlop.toFloat()) {
                        mClickCount = 0
                    }
                    mClickCount++
                    if (mClickCount == 1) {
                        mFirstClickRawX = rawX
                        mFirstClickRawY = rawY
                        mLastClickTime = SystemClock.elapsedRealtime()
                        return false
                    } else if (Math.abs(rawY - mFirstClickRawY) <= mTouchSlop.toFloat() && Math.abs(rawX - mFirstClickRawX) <= mTouchSlop.toFloat() && SystemClock.elapsedRealtime() - mLastClickTime <= MAX_DURATION) {
                        mClickCount = 0
                        return true
                    }
                }
                mClickCount = 0
                return false
            }
        }

        fun onDoubleTapEvent() {
            GlobalActions.handleAction(mContext, mActionKey)
        }
    }

    @JvmStatic
    fun LauncherDoubleTapHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.miui.home.launcher.Workspace", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()
                    val args = XposedHelpers.getArgsArray(chain)

                    if (args.size != 3) { return XposedHelpers.throwOrReturn(throwable, result) }
                    var mDoubleTapControllerEx = XposedHelpers.getAdditionalInstanceField(thisObject, "mDoubleTapControllerEx")
                    if (mDoubleTapControllerEx != null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    mDoubleTapControllerEx = DoubleTapController(args[0] as Context, "launcher_doubletap")
                    XposedHelpers.setAdditionalInstanceField(thisObject, "mDoubleTapControllerEx", mDoubleTapControllerEx)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "dispatchTouchEvent", MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val mDoubleTapControllerEx = XposedHelpers.getAdditionalInstanceField(thisObject, "mDoubleTapControllerEx") as? DoubleTapController
                    if (mDoubleTapControllerEx == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    if (!mDoubleTapControllerEx.isDoubleTapEvent(args[0] as MotionEvent)) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    val mCurrentScreenIndex = XposedHelpers.getIntField(thisObject, if (lpparam.packageName == "com.miui.home") "mCurrentScreenIndex" else "mCurrentScreen")
                    val cellLayout = XposedHelpers.callMethod(thisObject, "getCellLayout", mCurrentScreenIndex)
                    if (XposedHelpers.callMethod(cellLayout, "lastDownOnOccupiedCell") as Boolean) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    if (XposedHelpers.callMethod(thisObject, "isInNormalEditingMode") as Boolean) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    mDoubleTapControllerEx.onDoubleTapEvent()

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun TitleShadowHook(lpparam: PackageReadyParam) {
        if (lpparam.packageName == "com.miui.home")
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader, "getIconTitleShadowColor", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val color = result as Int
                        if (color == Color.TRANSPARENT) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color))
                        throwable = null
                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
        else
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader, "getTitleShadowColor", Int::class.javaPrimitiveType!!, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val color = result as Int
                        if (color == Color.TRANSPARENT) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = Color.argb(Math.round(Color.alpha(color) + (255 - Color.alpha(color)) / 1.9f), Color.red(color), Color.green(color), Color.blue(color))
                        throwable = null
                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
    }

    @JvmStatic
    fun HideNavBarHook(lpparam: PackageReadyParam) {
        val showNavBar = booleanArrayOf(true)
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.classLoader, "onSystemUiFlagsChanged", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val flags = args[0] as Int
                    val newState = (flags and 2) == 0
                    if (newState != showNavBar[0]) {
                        showNavBar[0] = newState
                    }
                    args[0] = flags and -3

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.home.recents.views.RecentsContainer", lpparam.classLoader, "showLandscapeOverviewGestureView", Boolean::class.javaPrimitiveType!!, HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.classLoader, "isImmersive", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    skipped = true
                    result = !showNavBar[0]
                    throwable = null

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.classLoader, "onPointerEvent", MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val mIsInFsMode = XposedHelpers.getBooleanField(thisObject, "mIsInFsMode")
                    if (!mIsInFsMode) {
                        val motionEvent = args[0] as MotionEvent
                        if (motionEvent.action == 0) {
                            XposedHelpers.setObjectField(thisObject, "mHideGestureLine", true)
                        }
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.home.recents.NavStubView", lpparam.classLoader, "updateScreenSize", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.getThisObject()
                try {

                    XposedHelpers.setObjectField(thisObject, "mHideGestureLine", false)

                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideSeekPointsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.pageindicators.AllAppsIndicator", lpparam.classLoader, "shouldHide", HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.pageindicators.AllAppsIndicator", lpparam.classLoader, "hideAllAppsArrow", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val mLauncher = XposedHelpers.getObjectField(thisObject, "mLauncher")
                    if (mLauncher == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val workspace = XposedHelpers.getObjectField(mLauncher, "mWorkspace") as View
                    val isInEditingMode = XposedHelpers.callMethod(workspace, "isInNormalEditingMode") as Boolean
                    val mContext = workspace.context
                    var mHandler = XposedHelpers.getAdditionalInstanceField(workspace, "mHandlerEx") as Handler?
                    if (mHandler == null) {
                        mHandler = Handler(mContext.mainLooper, object : Handler.Callback {
                            override fun handleMessage(msg: Message): Boolean {
                                val seekBar = msg.obj as? View
                                if (seekBar != null) {
                                    seekBar.animate().alpha(0.0f).setDuration(300).withEndAction {
                                        seekBar.visibility = View.GONE
                                    }
                                }
                                return true
                            }
                        })
                        XposedHelpers.setAdditionalInstanceField(workspace, "mHandlerEx", mHandler)
                    }
                    if (mHandler.hasMessages(666)) mHandler.removeMessages(666)
                    val mScreenSeekBar = XposedHelpers.getObjectField(thisObject, "mScreenIndicator") as View
                    mScreenSeekBar.animate().cancel()
                    if (!isInEditingMode && MainModule.mPrefs.getBoolean("launcher_hideseekpoints_edit")) {
                        mScreenSeekBar.alpha = 0.0f
                        mScreenSeekBar.visibility = View.GONE
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }
                    mScreenSeekBar.visibility = View.VISIBLE
                    mScreenSeekBar.animate().alpha(1.0f).setDuration(300)
                    if (!isInEditingMode) {
                        val msg = Message.obtain(mHandler, 666)
                        msg.obj = mScreenSeekBar
                        mHandler.sendMessageDelayed(msg, 600)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun InfiniteScrollHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "getSnapToScreenIndex", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()
                    val args = XposedHelpers.getArgsArray(chain)

                    if (args[0] != result) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val screenCount = XposedHelpers.callMethod(thisObject, "getScreenCount") as Int
                    if (args[2] as Int == -1 && args[0] as Int == 0)
                    { result = screenCount; throwable = null }
                    else if (args[2] as Int == 1 && args[0] as Int == screenCount - 1)
                    { result = 0; throwable = null }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenView", lpparam.classLoader, "getSnapUnitIndex", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val mCurrentScreenIndex = XposedHelpers.getIntField(thisObject, if (lpparam.packageName == "com.miui.home") "mCurrentScreenIndex" else "mCurrentScreen")
                    if (mCurrentScreenIndex != result as Int) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val screenCount = XposedHelpers.callMethod(thisObject, "getScreenCount") as Int
                    if (result as Int == 0)
                    { result = screenCount; throwable = null }
                    else if (result as Int == screenCount - 1)
                    { result = 0; throwable = null }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun UnlockGridsRes() {
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_x", 3)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_y", 4)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_x_min", 3)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_y_min", 4)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_x_max", 8)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_cell_count_y_max", 10)
    }

    @JvmStatic
    fun UnlockGridsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mScreenCellsConfig"), "setVisible", true)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        val DeviceConfigClass = XposedHelpers.findClass("com.miui.home.launcher.DeviceConfig", lpparam.classLoader)
        ModuleHelper.findAndHookMethod(DeviceConfigClass, "loadCellsCountConfig", Context::class.java, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val sCellCountY = XposedHelpers.getStaticObjectField(DeviceConfigClass, "sCellCountY") as Int
                    if (sCellCountY > 6) {
                        val cellHeight = XposedHelpers.callStaticMethod(DeviceConfigClass, "getCellHeight") as Int
                        XposedHelpers.setStaticObjectField(DeviceConfigClass, "sFolderCellHeight", cellHeight)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ScreenUtils", lpparam.classLoader, "getScreenCellsSizeOptions", Context::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    val arrayList = ArrayList<CharSequence>()
                    var cellCountXMin = 3
                    val cellCountXMax = 8
                    var cellCountYMin = 4
                    val cellCountYMax = 10
                    while (cellCountXMin <= cellCountXMax) {
                        for (i in cellCountYMin..cellCountYMax) {
                            arrayList.add(cellCountXMin.toString() + "x" + i)
                        }
                        cellCountXMin++
                    }
                    skipped = true
                    result = arrayList
                    throwable = null

                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                return try { chain.proceed() } catch (t: Throwable) { XposedHelpers.throwOrReturn(t, null) }
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.compat.LauncherCellCountCompatNoWord", lpparam.classLoader, "setLoadResCellConfig", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    args[0] = true

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "isCellSizeChangedByTheme", object : MethodHook() {
            var nowordHook: HookerClassHelper.CustomMethodUnhooker? = null
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    nowordHook = ModuleHelper.findAndHookMethod("com.miui.home.launcher.common.Utilities", lpparam.classLoader, "isNoWordModel", HookerClassHelper.returnConstant(false))

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }

                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    nowordHook?.unhook()
                    nowordHook = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun FolderColumnsRes(folderCols: Int) {
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "integer", "config_folder_columns_count", folderCols)
    }

    private fun setFolderWidth(thisObject: Any) {
        if (MainModule.mPrefs.getBoolean("launcher_folderwidth")) {
            val mContent = XposedHelpers.getObjectField(thisObject, "mContent") as GridView
            val lp = mContent.layoutParams
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            mContent.layoutParams = lp
        }
    }

    @JvmStatic
    fun FolderColumnsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    setFolderWidth(thisObject)
                    val cols = MainModule.mPrefs.getInt("launcher_folder_cols", 1)
                    if (cols > 3 && MainModule.mPrefs.getBoolean("launcher_folderspace")) {
                        val mBackgroundView = XposedHelpers.getObjectField(thisObject, "mBackgroundView") as ViewGroup
                        mBackgroundView.setPadding(
                            mBackgroundView.paddingLeft / 3,
                            mBackgroundView.paddingTop,
                            mBackgroundView.paddingRight / 3,
                            mBackgroundView.paddingBottom
                        )
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Folder", lpparam.classLoader, "resetViewsLayoutParams", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    setFolderWidth(thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.miui.home.launcher.Folder", lpparam.classLoader, "onLayout", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    if (!MainModule.mPrefs.getBoolean("launcher_folderwidth")) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mContent = XposedHelpers.getObjectField(thisObject, "mContent") as GridView
                    val mFakeIcon = XposedHelpers.getObjectField(thisObject, "mFakeIcon") as ImageView
                    mFakeIcon.layout(mContent.left, mContent.top, mContent.right, mContent.top + mContent.width)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun IconScaleHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ShortcutIcon", lpparam.classLoader, "restoreToInitState", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val mIconContainer = XposedHelpers.getObjectField(thisObject, "mIconContainer") as? ViewGroup
                    if (mIconContainer == null || mIconContainer.getChildAt(0) == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val multx = Math.sqrt((MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f).toDouble()).toFloat()
                    mIconContainer.getChildAt(0).scaleX = multx
                    mIconContainer.getChildAt(0).scaleY = multx

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val multx = Math.sqrt((MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f).toDouble()).toFloat()

                    val mIconContainer = XposedHelpers.getObjectField(thisObject, "mIconContainer") as? ViewGroup
                    if (mIconContainer != null && mIconContainer.getChildAt(0) != null) {
                        mIconContainer.getChildAt(0).scaleX = multx
                        mIconContainer.getChildAt(0).scaleY = multx
                        mIconContainer.clipToPadding = false
                        mIconContainer.clipChildren = false
                    }

                    if (multx > 1) {
                        val mMessage = XposedHelpers.getObjectField(thisObject, "mMessage") as? TextView
                        if (mMessage != null)
                            mMessage.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                                override fun afterTextChanged(s: Editable) {
                                    val maxWidth = mMessage.resources.getDimensionPixelSize(mMessage.resources.getIdentifier("icon_message_max_width", "dimen", lpparam.packageName))
                                    mMessage.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST))
                                    mMessage.translationX = -mMessage.measuredWidth * (multx - 1) / 2f
                                    mMessage.translationY = mMessage.measuredHeight * (multx - 1) / 2f
                                }
                            })
                    }

                    XposedHelpers.setAdditionalInstanceField(thisObject, "mMessageAnimationOrig", XposedHelpers.getObjectField(thisObject, "mMessageAnimation"))
                    XposedHelpers.setObjectField(thisObject, "mMessageAnimation", object : Runnable {
                        override fun run() {
                            try {
                                val mMessageAnimationOrig = XposedHelpers.getAdditionalInstanceField(thisObject, "mMessageAnimationOrig") as Runnable
                                mMessageAnimationOrig.run()
                                val mIsShowMessageAnimation = XposedHelpers.getBooleanField(thisObject, "mIsShowMessageAnimation")
                                if (mIsShowMessageAnimation) {
                                    val mMessage = XposedHelpers.getObjectField(thisObject, "mMessage") as View
                                    mMessage.animate().cancel()
                                    mMessage.animate().scaleX(multx).scaleY(multx).setStartDelay(0).start()
                                }
                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                        }
                    })

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "getIconLocation", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val multx = Math.sqrt((MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f).toDouble()).toFloat()
                    val rect = result as Rect?
                    if (rect == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    rect.right = rect.left + Math.round(rect.width() * multx)
                    rect.bottom = rect.top + Math.round(rect.height() * multx)
                    result = rect
                    throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.gadget.ClearButton", lpparam.classLoader, "onCreate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val mIconContainer = XposedHelpers.getObjectField(thisObject, "mIconContainer") as? ViewGroup
                    if (mIconContainer == null || mIconContainer.getChildAt(0) == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val multx = Math.sqrt((MainModule.mPrefs.getInt("launcher_iconscale", 100) / 100f).toDouble()).toFloat()
                    mIconContainer.getChildAt(0).scaleX = multx
                    mIconContainer.getChildAt(0).scaleY = multx

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun TitleFontSizeHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val mTitle = XposedHelpers.getObjectField(thisObject, "mTitle") as? TextView
                    if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5).toFloat())

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.classLoader, "fromXml", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    val buddyIcon = XposedHelpers.callMethod(args[3], "getBuddyIconView", args[2])
                    if (buddyIcon == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mTitle = XposedHelpers.getObjectField(buddyIcon, "mTitle") as? TextView
                    if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5).toFloat())

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.miui.home.launcher.ShortcutIcon", lpparam.classLoader, "createShortcutIcon", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val buddyIcon = result
                    if (buddyIcon == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mTitle = XposedHelpers.getObjectField(buddyIcon, "mTitle") as? TextView
                    if (mTitle != null) mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5).toFloat())

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.miui.home.launcher.common.Utilities", lpparam.classLoader, "adaptTitleStyleToWallpaper", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    val mTitle = args[1] as? TextView
                    if (mTitle != null && mTitle.id == mTitle.resources.getIdentifier("icon_title", "id", "com.miui.home"))
                        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, MainModule.mPrefs.getInt("launcher_titlefontsize", 5).toFloat())

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun TitleTopMarginHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val mTitleContainer = XposedHelpers.getObjectField(thisObject, "mTitleContainer") as? ViewGroup
                    if (mTitleContainer == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val lp = mTitleContainer.layoutParams
                    val opt = Math.round((MainModule.mPrefs.getInt("launcher_titletopmargin", 0) - 11) * mTitleContainer.resources.displayMetrics.density)
                    if (lp is RelativeLayout.LayoutParams) {
                        lp.topMargin = opt
                        mTitleContainer.layoutParams = lp
                    } else {
                        mTitleContainer.translationY = opt.toFloat()
                        mTitleContainer.clipChildren = false
                        mTitleContainer.clipToPadding = false
                        (mTitleContainer.parent as? ViewGroup)?.clipChildren = false
                        (mTitleContainer.parent as? ViewGroup)?.clipToPadding = false
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun PrivacyFolderHook(lpparam: PackageReadyParam) {
        if (MainModule.mPrefs.getBoolean("launcher_privacyapps_gest")) {
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "registerBroadcastReceivers", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val thisObject = chain.getThisObject()

                        val act = thisObject as Activity
                        val intentFilter = IntentFilter()
                        intentFilter.addAction("android.telephony.action.SECRET_CODE")
                        intentFilter.addDataAuthority("233233", null)
                        intentFilter.addDataScheme("android_secret_code")

                        val oldsecretCodeReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "secretCodeReceiver")
                        if (oldsecretCodeReceiver is BroadcastReceiver) {
                            try { act.unregisterReceiver(oldsecretCodeReceiver) } catch (ignore: Throwable) {}
                        }
                        val secretCodeReceiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                try {
                                    if (intent.action == null) return
                                    if ("android.telephony.action.SECRET_CODE" == intent.action) {
                                        XposedHelpers.setAdditionalInstanceField(thisObject, "fromSecretCode", true)
                                        XposedHelpers.callMethod(thisObject, "startSecurityHide")
                                    }
                                } catch (t: Throwable) {
                                    XposedHelpers.log(t)
                                }
                            }
                        }
                        XposedHelpers.setAdditionalInstanceField(thisObject, "secretCodeReceiver", secretCodeReceiver)
                        act.registerReceiver(secretCodeReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
        }
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "startSecurityHide", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.getThisObject()
                try {

                    if (XposedHelpers.getAdditionalInstanceField(thisObject, "fromSecretCode") != null) {
                        XposedHelpers.removeAdditionalInstanceField(thisObject, "fromSecretCode")
                        return XposedHelpers.proceedOrThrow(chain, throwable)
                    }
                    if (GlobalActions.handleAction(thisObject as Activity, "launcher_spread")) {
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }
                    val opt = MainModule.mPrefs.getBoolean("launcher_privacyapps_gest")
                    if (opt) { skipped = true; result = null; throwable = null }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onDestroy", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    val act = chain.getThisObject() as Activity
                    val secretCodeReceiver = XposedHelpers.getAdditionalInstanceField(act, "secretCodeReceiver")
                    if (secretCodeReceiver is BroadcastReceiver) {
                        try { act.unregisterReceiver(secretCodeReceiver) } catch (ignore: Throwable) {}
                    }
                    val fetchAppConfigReceiver = XposedHelpers.getAdditionalInstanceField(act, "fetchAppConfigReceiver")
                    if (fetchAppConfigReceiver is BroadcastReceiver) {
                        try { act.unregisterReceiver(fetchAppConfigReceiver) } catch (ignore: Throwable) {}
                    }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideTitlesHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.ItemIcon", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val mTitleContainer = XposedHelpers.getObjectField(thisObject, "mTitleContainer") as? View
                    if (mTitleContainer != null) mTitleContainer.visibility = View.GONE

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HorizontalSpacingRes() {
        val opt = MainModule.mPrefs.getInt("launcher_horizmargin", 0) - 21
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "workspace_cell_padding_side", opt)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "workspace_cell_padding_side_no_word", opt)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "workspace_cell_padding_side_rotatable", opt)
    }

    @JvmStatic
    fun IndicatorHeightRes() {
        val opt = MainModule.mPrefs.getInt("launcher_indicatorheight", 9)
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "slide_bar_height", opt)
    }

    @JvmStatic
    fun ShowHotseatTitlesHook(lpparam: PackageReadyParam) {
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "bool", "config_hide_hotseats_app_title", false)
        ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.Launcher", lpparam.classLoader, "createItemIcon", ViewGroup::class.java, "com.miui.home.launcher.ItemInfo", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    args[2] = false

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun FolderBlurHook(lpparam: PackageReadyParam) {
        val BlurUtils = XposedHelpers.findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.classLoader)
        if (BlurUtils != null) {
            ModuleHelper.hookAllMethods(BlurUtils, "getLauncherBlur", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var skipped = false
                    var result: Any? = null
                    var throwable: Throwable? = null
                    val args = XposedHelpers.getArgsArray(chain)
                    try {

                        val isFolderShowing = XposedHelpers.callMethod(args[0], "isFolderShowing") as Boolean
                        if (isFolderShowing) {
                            val blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0)
                            val blurRatio = blurPct / 100f
                            skipped = true
                            result = blurRatio
                            throwable = null
                        }

                        if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                        result = chain.proceed(args)
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.classLoader, "open", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val thisObject = chain.getThisObject()

                        val launcher = XposedHelpers.getObjectField(thisObject, "mLauncher") as Activity

                        val blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0)
                        val blurRatio = blurPct / 100f
                        XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", blurRatio, launcher.window, true)

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.FolderCling", lpparam.classLoader, "close", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val thisObject = chain.getThisObject()
                        val args = XposedHelpers.getArgsArray(chain)

                        val launcher = XposedHelpers.getObjectField(thisObject, "mLauncher") as Activity
                        XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", 0f, launcher.window, args[0])

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })

            ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "cancelShortcutMenu", Int::class.javaPrimitiveType!!, "com.miui.home.launcher.shortcuts.CancelShortcutMenuReason", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val thisObject = chain.getThisObject()

                        val isFolderShowing = XposedHelpers.callMethod(thisObject, "isFolderShowing") as Boolean
                        if (isFolderShowing) {
                            val blurPct = MainModule.mPrefs.getInt("launcher_folderblur_opacity", 0)
                            val blurRatio = blurPct / 100f
                            val launcher = thisObject as Activity
                            XposedHelpers.callStaticMethod(BlurUtils, "fastBlur", blurRatio, launcher.window, true)
                        }

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
        }
    }

    private fun scaleStiffness(`val`: Float, scale: Float): Float {
        return (if (scale < 1.0f) 2f / scale else 1.0f / scale) * `val`
    }

    @JvmStatic
    fun FixAnimHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.animate.SpringAnimator", lpparam.classLoader, "getSpringForce", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    var scale = Helpers.getAnimationScale(2)
                    if (scale == 1.0f) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    if (scale == 0f) scale = 0.01f
                    args[2] = scaleStiffness(args[2] as Float, scale)

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        val hook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.getThisObject()
                try {

                    var scale = Helpers.getAnimationScale(2)
                    if (scale == 1.0f) { return XposedHelpers.proceedOrThrow(chain, throwable) }
                    if (scale == 0f) scale = 0.01f
                    XposedHelpers.setFloatField(thisObject, "mCenterXStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mCenterXStiffness"), scale))
                    XposedHelpers.setFloatField(thisObject, "mCenterYStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mCenterYStiffness"), scale))
                    XposedHelpers.setFloatField(thisObject, "mWidthStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mWidthStiffness"), scale))
                    XposedHelpers.setFloatField(thisObject, "mRadiusStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mRadiusStiffness"), scale))
                    XposedHelpers.setFloatField(thisObject, "mAlphaStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mAlphaStiffness"), scale))
                    try {
                        XposedHelpers.setFloatField(thisObject, "mRatioStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mRatioStiffness"), scale))
                    } catch (t: Throwable) {
                        XposedHelpers.setFloatField(thisObject, "mRadioStiffness", scaleStiffness(XposedHelpers.getFloatField(thisObject, "mRadioStiffness"), scale))
                    }

                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }

        if (!ModuleHelper.hookAllMethodsSilently("com.miui.home.recents.util.RectFSpringAnim", lpparam.classLoader, "start", hook))
            ModuleHelper.hookAllMethods("com.miui.home.recents.util.RectFSpringAnim", lpparam.classLoader, "initAllAnimations", hook)
    }

    @JvmStatic
    fun DockMarginTopHook(lpparam: PackageReadyParam) {
        val opt = MainModule.mPrefs.getInt("launcher_dock_topmargin", 0)
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "calcHotSeatsMarginTop", Context::class.java, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    skipped = true
                    result = Math.round(Helpers.dp2px(opt.toFloat()))
                    throwable = null

                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                return try { chain.proceed() } catch (t: Throwable) { XposedHelpers.throwOrReturn(t, null) }
            }
        })
    }

    @JvmStatic
    fun DockMarginBottomHook(lpparam: PackageReadyParam) {
        val opt = MainModule.mPrefs.getInt("launcher_dock_bottommargin", 0)
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "calcHotSeatsMarginBottom", Context::class.java, Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    skipped = true
                    result = Math.round(Helpers.dp2px(opt.toFloat()))
                    throwable = null

                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                return try { chain.proceed() } catch (t: Throwable) { XposedHelpers.throwOrReturn(t, null) }
            }
        })
    }

    @JvmStatic
    fun DockHeightHook(lpparam: PackageReadyParam) {
        val dockHeight = MainModule.mPrefs.getInt("launcher_dock_height", 60)
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "calcHotSeatsHeight", Context::class.java, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    skipped = true
                    result = Math.round(Helpers.dp2px(dockHeight.toFloat()))
                    throwable = null

                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                return try { chain.proceed() } catch (t: Throwable) { XposedHelpers.throwOrReturn(t, null) }
            }
        })
    }

    @JvmStatic
    fun WorkspaceCellPaddingTopHook(lpparam: PackageReadyParam) {
        val opt = MainModule.mPrefs.getInt("launcher_topmargin", 0) - 21
        val hook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    skipped = true
                    result = Math.round(Helpers.dp2px(opt.toFloat()))
                    throwable = null

                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                return try { chain.proceed() } catch (t: Throwable) { XposedHelpers.throwOrReturn(t, null) }
            }
        }

        val newLauncher = ModuleHelper.findAndHookMethodSilently("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "getWorkspaceCellPaddingTop", Context::class.java, hook)
        if (!newLauncher) {
            ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "getWorkspaceCellPaddingTop", hook)
        }
    }

    @JvmStatic
    fun IndicatorMarginTopHook(lpparam: PackageReadyParam) {
        val opt = MainModule.mPrefs.getInt("launcher_indicator_topmargin", 0) - 21
        MainModule.resHooks.setThemeValueReplacement("com.miui.home", "dimen", "slide_bar_margin_top", opt)
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.util.DimenUtils1X", lpparam.classLoader, "getDimensionPixelSize", Context::class.java, String::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val resKey = args[1] as String
                    if ("slide_bar_margin_top" == resKey) {
                        skipped = true
                        result = Math.round(Helpers.dp2px(opt.toFloat()))
                        throwable = null
                    }

                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                return try { chain.proceed(args) } catch (t: Throwable) { XposedHelpers.throwOrReturn(t, null) }
            }
        })
    }

    @JvmStatic
    fun HorizontalWidgetSpacingHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "getMiuiWidgetSizeSpec", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    if (args.size < 4) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val spec = result as Long
                    var width = spec shr 32
                    var height = spec - ((spec shr 32) shl 32)
                    val opt = Math.round((MainModule.mPrefs.getInt("launcher_horizwidgetmargin", 0) - 21) * Resources.getSystem().displayMetrics.density) * 2
                    width -= opt.toLong()
                    result = (width shl 32) or height

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.miui.home.launcher.MIUIWidgetUtil", lpparam.classLoader, "getMiuiWidgetPadding", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    result = Rect()
                    throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun FixAppInfoLaunchHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.shortcuts.ShortcutMenuManager", lpparam.classLoader, "startAppDetailsActivity", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val component = XposedHelpers.callMethod(args[0], "getComponentName") as ComponentName?
                    if (component == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    val view = args[1] as View?
                    if (view == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    val userHandle = XposedHelpers.callMethod(args[0], "getUserHandle") as UserHandle?
                    ModuleHelper.openAppInfo(view.context, component.packageName, userHandle?.hashCode() ?: 0)
                    skipped = true
                    result = null
                    throwable = null

                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                result = chain.proceed(args)
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun NoWidgetOnlyHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.CellLayout", lpparam.classLoader, "setScreenType", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    args[0] = 0

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun NoUnlockAnimationHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.miui.launcher.utils.MiuiSettingsUtils", lpparam.classLoader, "isSystemAnimationOpen", HookerClassHelper.returnConstant(false))
    }

    @JvmStatic
    fun NoZoomAnimationHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.classLoader, "startShortcutMenuLayerFadeOutAnim", HookerClassHelper.DO_NOTHING)
        ModuleHelper.hookAllMethods("com.miui.home.recents.util.SpringAnimationUtils", lpparam.classLoader, "startShortcutMenuLayerFadeInAnim", HookerClassHelper.DO_NOTHING)
    }

    @JvmStatic
    fun UseOldLaunchAnimationHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.miui.home.recents.QuickstepAppTransitionManagerImpl", lpparam.classLoader, "hasControlRemoteAppTransitionPermission", HookerClassHelper.returnConstant(false))
    }

    @JvmStatic
    @SuppressLint("SourceLockedOrientationActivity")
    fun ReverseLauncherPortraitHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onCreate", Bundle::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val act = thisObject as Activity
                    act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideFromRecentsHook(lpparam: PackageReadyParam) {
        val ActivityManagerWrapper = XposedHelpers.findClassIfExists("com.android.systemui.shared.recents.system.ActivityManagerWrapper", lpparam.classLoader)
        val TaskInfoCompat = XposedHelpers.findClassIfExists("com.android.systemui.shared.recents.model.GroupedRecentTaskInfoCompat", lpparam.classLoader)
        if (TaskInfoCompat == null) {
            XposedHelpers.log("HideFromRecentsHook", "hook failed")
            return
        }
        ModuleHelper.findAndHookMethod(ActivityManagerWrapper!!, "needRemoveTask", TaskInfoCompat, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    if (args[0] != null) {
                        val mainTask = XposedHelpers.getObjectField(args[0], "mMainTaskInfo")
                        var componentName = XposedHelpers.getObjectField(mainTask, "topActivity") as ComponentName?
                        var pkgName: String? = null
                        if (componentName != null) {
                            pkgName = componentName.packageName
                        } else {
                            val baseIntent = XposedHelpers.getObjectField(mainTask, "baseIntent") as Intent?
                            if (baseIntent != null && baseIntent.component != null) {
                                pkgName = baseIntent.component!!.packageName
                            }
                        }
                        if (pkgName != null) {
                            val selectedApps = MainModule.mPrefs.getStringSet("system_hidefromrecents_apps")
                            if (selectedApps.contains(pkgName)) {
                                result = true
                                throwable = null
                            }
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun MaxHotseatIconsCountHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "getHotseatMaxCount", HookerClassHelper.returnConstant(666))
    }

    @JvmStatic
    fun RecentsBlurRatioHook(lpparam: PackageReadyParam) {
        val utilsClass = XposedHelpers.findClassIfExists("com.miui.home.launcher.common.BlurUtils", lpparam.classLoader)
        if (utilsClass == null) {
            XposedHelpers.log("RecentsBlurRatioHook", "Cannot find blur utility class")
            return
        }

        ModuleHelper.hookAllMethods(utilsClass, "fastBlurWhenEnterRecents", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val mIsFromFsGesture = XposedHelpers.getBooleanField(args[1], "mIsFromFsGesture")
                    if (!mIsFromFsGesture) {
                        val launcher = args[0] as Activity
                        val blurRatio = MainModule.mPrefs.getInt("system_recents_blur", 100) / 100f
                        XposedHelpers.callStaticMethod(utilsClass, "fastBlur", blurRatio, launcher.window, args[2])
                        skipped = true
                        result = null
                        throwable = null
                    }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.hookAllMethods(utilsClass, "fastBlurWhenGestureResetTaskView", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    XposedHelpers.setAdditionalStaticField(utilsClass, "customBlurRatio", true)

                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods(utilsClass, "fastBlur", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (args.size == 3) {
                        if (XposedHelpers.getAdditionalStaticField(utilsClass, "customBlurRatio") != null) {
                            val blurRatio = MainModule.mPrefs.getInt("system_recents_blur", 100) / 100f
                            args[0] = blurRatio
                            XposedHelpers.removeAdditionalStaticField(utilsClass, "customBlurRatio")
                        }
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun CloseFolderOrDrawerOnLaunchShortcutMenuHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.shortcuts.AppShortcutMenuItem", lpparam.classLoader, "getOnClickListener", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val listener = result as View.OnClickListener
                    result = View.OnClickListener {
                        listener.onClick(it)
                        val appCls = XposedHelpers.findClassIfExists("com.miui.home.launcher.Application", lpparam.classLoader)
                        if (appCls == null) return@OnClickListener
                        val launcher = XposedHelpers.callStaticMethod(appCls, "getLauncher")
                        if (launcher == null) return@OnClickListener
                        if (MainModule.mPrefs.getBoolean("launcher_closedrawer")) XposedHelpers.callMethod(launcher, "hideAppView")
                        if (MainModule.mPrefs.getStringAsInt("launcher_closefolders", 1) > 1) XposedHelpers.callMethod(launcher, "closeFolder")
                    }
                    throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun CloseDrawerOnLaunchHook(lpparam: PackageReadyParam) {
        val hook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.getThisObject()
                try {

                    XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mLauncher"), "hideAppView")

                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.AppsListFragment", lpparam.classLoader, "onClick", View::class.java, hook)
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.allapps.category.fragment.RecommendCategoryAppListFragment", lpparam.classLoader, "onClick", View::class.java, hook)
    }

    @JvmStatic
    fun AssistGestureActionHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shared.recents.system.AssistManager", lpparam.classLoader, "isSupportGoogleAssist", Int::class.javaPrimitiveType!!, HookerClassHelper.returnConstant(true))
        val FsGestureHelper = XposedHelpers.findClassIfExists("com.miui.home.recents.FsGestureAssistHelper", lpparam.classLoader)
        ModuleHelper.findAndHookMethod(FsGestureHelper!!, "canTriggerAssistantAction", Float::class.javaPrimitiveType!!, Float::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val isDisabled = XposedHelpers.callStaticMethod(FsGestureHelper, "isAssistantGestureDisabled", args[2]) as Boolean
                    if (!isDisabled) {
                        val mAssistantWidth = XposedHelpers.getIntField(thisObject, "mAssistantWidth")
                        val f = args[0] as Float
                        val f2 = args[1] as Float
                        if (f < mAssistantWidth || f > f2 - mAssistantWidth) {
                            skipped = true
                            result = true
                            throwable = null
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                    }
                    skipped = true
                    result = false
                    throwable = null

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        val inDirection = intArrayOf(0)

        ModuleHelper.hookAllMethods(FsGestureHelper!!, "handleTouchEvent", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()
                    val args = XposedHelpers.getArgsArray(chain)

                    val motionEvent = args[0] as MotionEvent
                    if (motionEvent.action == 0) {
                        val mDownX = XposedHelpers.getFloatField(thisObject, "mDownX")
                        val mAssistantWidth = XposedHelpers.getIntField(thisObject, "mAssistantWidth")
                        inDirection[0] = if (mDownX < mAssistantWidth) 0 else 1
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.recents.SystemUiProxyWrapper", lpparam.classLoader, "startAssistant", Bundle::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val bundle = args[0] as Bundle
                    bundle.putInt("inDirection", inDirection[0])

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun SwipeAndStopActionHook(lpparam: PackageReadyParam) {
        val ReadyStateEnum = XposedHelpers.findClassIfExists("com.miui.home.recents.GestureBackArrowView\$ReadyState", lpparam.classLoader)
        if (ReadyStateEnum == null) return
        val states = ReadyStateEnum.enumConstants ?: return
        var recentState: Any? = null
        var backState: Any? = null
        for (o in states) {
            val enumStr = o.toString()
            if ("READY_STATE_RECENT" == enumStr) recentState = o
            else if ("READY_STATE_BACK" == enumStr) backState = o
        }
        val finalBackState = backState
        val finalRecentState = recentState
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureBackArrowView", lpparam.classLoader, "setReadyFinish", ReadyStateEnum, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val mReadyState = XposedHelpers.getObjectField(thisObject, "mReadyState")
                    val readyState = args[0]
                    if (readyState != mReadyState) {
                        val disableVibrate = MainModule.mPrefs.getBoolean("controls_fsg_swipeandstop_disablevibrate")
                        val view = thisObject as View
                        XposedHelpers.setObjectField(view, "mRecentTaskIcon", null)
                        if (mReadyState == finalBackState && readyState == finalRecentState) {
                            val mScale = XposedHelpers.getFloatField(view, "mScale")
                            XposedHelpers.callMethod(view, "changeScale", mScale, 1.17f, 200, false)
                            if (!disableVibrate) {
                                Helpers.performStrongVibration(view.context, true)
                            }
                        } else if (mReadyState == finalRecentState) {
                            val mScale = XposedHelpers.getFloatField(view, "mScale")
                            XposedHelpers.callMethod(view, "changeScale", mScale, 1.0f, 200, true)
                        }
                        XposedHelpers.setObjectField(view, "mReadyState", readyState)
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        val GestureStubViewClass = XposedHelpers.findClass("com.miui.home.recents.GestureStubView", lpparam.classLoader)
        ModuleHelper.findAndHookMethod(GestureStubViewClass, "disableQuickSwitch", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    args[0] = false

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod(GestureStubViewClass, "isDisableQuickSwitch", HookerClassHelper.returnConstant(false))
        val gestureStubViews = arrayOfNulls<Any>(1)
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView\$3", lpparam.classLoader, "onSwipeStop", Boolean::class.javaPrimitiveType!!, Float::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val isFinished = args[0] as Boolean
                    if (isFinished) {
                        val outerThis = XposedHelpers.getSurroundingThis(thisObject)
                        gestureStubViews[0] = outerThis
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }

                try {
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val isFinished = args[0] as Boolean
                    if (isFinished) {
                        gestureStubViews[0] = null
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.home.recents.GestureStubView", lpparam.classLoader, "getNextTask", Context::class.java, Boolean::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    val nextTaskInfo = args[1] as Boolean
                    if (!nextTaskInfo || gestureStubViews[0] == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val outerThis = gestureStubViews[0]
                    ModuleHelper.callMethodSilently(outerThis, "onBackCancelled")
                    val mContext = XposedHelpers.getObjectField(outerThis, "mContext") as Context
                    val mGestureStubPos = args[2] as Int
                    val bundle = Bundle()
                    bundle.putInt("inDirection", mGestureStubPos)
                    GlobalActions.handleAction(mContext, "controls_fsg_swipeandstop", false, bundle)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun DisableUnlockWallpaperScale(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.miwallpaper.manager.WallpaperServiceController", lpparam.classLoader, "noNeedDesktopWallpaperScaleAnim", HookerClassHelper.returnConstant(true))
    }

    @JvmStatic
    fun DisableLauncherWallpaperScale(lpparam: PackageReadyParam) {
        val WallpaperZoomManagerKtClass = XposedHelpers.findClassIfExists("com.miui.home.launcher.wallpaper.WallpaperZoomManagerKt", lpparam.classLoader)
        if (MainModule.mPrefs.getBoolean("launcher_disable_wallpaperscale")) {
            XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", false)
            ModuleHelper.findAndHookMethod("com.miui.home.recents.DimLayer", lpparam.classLoader, "isSupportDim", HookerClassHelper.returnConstant(false))
            return
        }
        ModuleHelper.hookAllMethods("com.miui.home.recents.OverviewState", lpparam.classLoader, "onStateEnabled", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    if (WallpaperZoomManagerKtClass != null) {
                        XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", false)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }

                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    if (WallpaperZoomManagerKtClass != null) {
                        XposedHelpers.setStaticBooleanField(WallpaperZoomManagerKtClass, "ZOOM_ENABLED", true)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideStatusBarInRecentsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.common.DeviceLevelUtils", lpparam.classLoader, "isHideStatusBarWhenEnterRecents", HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.DeviceConfig", lpparam.classLoader, "keepStatusBarShowingForBetterPerformance", HookerClassHelper.returnConstant(false))
    }

    @JvmStatic
    fun DisableLauncherLogHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.miui.home.launcher.AnalyticalDataCollectorJobService", lpparam.classLoader, "onStartJob", HookerClassHelper.returnConstant(false))
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.AnalyticalDataCollector", lpparam.classLoader, "canTrackLaunchAppEvent", HookerClassHelper.returnConstant(false))
        val OneTrackInterfaceUtils = XposedHelpers.findClassIfExists("com.miui.home.launcher.common.OneTrackInterfaceUtils", lpparam.classLoader)
        if (OneTrackInterfaceUtils != null) {
            XposedHelpers.setStaticObjectField(OneTrackInterfaceUtils, "IS_ENABLE", false)
        }
    }

    @JvmStatic
    fun LauncherPinchHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onPinching", Float::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val dampingScale = XposedHelpers.callMethod(thisObject, "getDampingScale", args[0]) as Float
                    val screenScaleRatio = XposedHelpers.callMethod(thisObject, "getScreenScaleRatio") as Float
                    if (dampingScale < screenScaleRatio)
                        if (MainModule.mPrefs.getInt("launcher_pinch_action", 1) > 1) { skipped = true; result = false; throwable = null }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Workspace", lpparam.classLoader, "onPinchingEnd", Float::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.getThisObject()
                try {

                    val dampingScale = XposedHelpers.callMethod(thisObject, "getDampingScale", args[0]) as Float
                    val screenScaleRatio = XposedHelpers.callMethod(thisObject, "getScreenScaleRatio") as Float
                    if (dampingScale < screenScaleRatio)
                        if (GlobalActions.handleAction((thisObject as View).context, "launcher_pinch")) {
                            XposedHelpers.callMethod(thisObject, "finishCurrentGesture")

                            val pinchingStateEnum = XposedHelpers.findClass("com.miui.home.launcher.Workspace\$PinchingState", lpparam.classLoader)
                            val stateFollow = XposedHelpers.getStaticObjectField(pinchingStateEnum, "FOLLOW")
                            val stateReadyToEdit = XposedHelpers.getStaticObjectField(pinchingStateEnum, "READY_TO_EDIT")

                            val mState = XposedHelpers.getObjectField(thisObject, "mState")
                            XposedHelpers.setObjectField(thisObject, "mState", stateFollow)
                            if (mState == stateReadyToEdit)
                                XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mLauncher"), "changeEditingEntryViewToHotseats")
                            XposedHelpers.callMethod(thisObject, "resetCellScreenScale", args[0])

                            skipped = true
                            result = null
                            throwable = null
                        }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun ResizableWidgetsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("android.appwidget.AppWidgetHostView", lpparam.classLoader, "getAppWidgetInfo", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val widgetInfo = result as AppWidgetProviderInfo?
                    if (widgetInfo == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    widgetInfo.resizeMode = AppWidgetProviderInfo.RESIZE_VERTICAL or AppWidgetProviderInfo.RESIZE_HORIZONTAL
                    widgetInfo.minHeight = 0
                    widgetInfo.minWidth = 0
                    widgetInfo.minResizeHeight = 0
                    widgetInfo.minResizeWidth = 0
                    result = widgetInfo
                    throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun WallpaperColorModeHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader, "setCurrentStatusBarAreaColorMode", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val v = MainModule.mPrefs.getStringAsInt("launcher_wallpaper_colormode", 1)
                    if (v > 1) {
                        args[0] = if (v == 2) 2 else 0
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.WallpaperUtils", lpparam.classLoader, "setCurrentWallpaperColorMode", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val v = MainModule.mPrefs.getStringAsInt("launcher_wallpaper_colormode", 1)
                    if (v > 1) {
                        args[0] = if (v == 2) 2 else 0
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun setupLauncher(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "registerBroadcastReceivers", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.getThisObject()

                    val act = thisObject as Activity
                    val intentFilter = IntentFilter()
                    intentFilter.addAction(GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG")

                    val oldfetchAppConfigReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "fetchAppConfigReceiver")
                    if (oldfetchAppConfigReceiver is BroadcastReceiver) {
                        try { act.unregisterReceiver(oldfetchAppConfigReceiver) } catch (ignore: Throwable) {}
                    }
                    val fetchAppConfigReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            try {
                                if (intent.action == null) return
                                if ((GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG") == intent.action) {
                                    val pushIntent = Intent(GlobalActions.EVENT_PREFIX + "PUSHAPPCONFIG")
                                    pushIntent.setPackage(Helpers.modulePkg)
                                    val datatype = intent.getStringExtra("DATATYPE")
                                    pushIntent.putExtra("DATATYPE", datatype)
                                    if ("privacy" == datatype) {
                                        @Suppress("WrongConstant")
                                        val mSecurityManager = context.getSystemService("security") as SecurityManager
                                        val privacyAppsMap = HashMap<Int, MutableList<String>>()
                                        privacyAppsMap[0] = mSecurityManager.getAllPrivacyApps(0) as MutableList<String>
                                        privacyAppsMap[999] = mSecurityManager.getAllPrivacyApps(999) as MutableList<String>
                                        pushIntent.putExtra("privacyAppsMap", privacyAppsMap)
                                        context.sendBroadcast(pushIntent)
                                    } else if ("privacy_change" == datatype) {
                                        val userId = intent.getIntExtra("userId", 0)
                                        val pkgName = intent.getStringExtra("app")
                                        val privacy = intent.getBooleanExtra("privacy", false)
                                        @Suppress("WrongConstant")
                                        val mSecurityManager = context.getSystemService("security") as SecurityManager
                                        if (pkgName != null) mSecurityManager.setPrivacyApp(pkgName, userId, privacy)
                                        context.contentResolver.notifyChange(Uri.parse("content://com.miui.securitycenter.provider/update_privacyapps_icon"), null)
                                    }
                                }
                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                        }
                    }
                    XposedHelpers.setAdditionalInstanceField(thisObject, "fetchAppConfigReceiver", fetchAppConfigReceiver)
                    act.registerReceiver(fetchAppConfigReceiver, intentFilter, Context.RECEIVER_EXPORTED)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.home.launcher.Launcher", lpparam.classLoader, "onDestroy", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {
                    val act = chain.getThisObject() as Activity
                    val secretCodeReceiver = XposedHelpers.getAdditionalInstanceField(act, "secretCodeReceiver")
                    if (secretCodeReceiver is BroadcastReceiver) {
                        try { act.unregisterReceiver(secretCodeReceiver) } catch (ignore: Throwable) {}
                    }
                    val fetchAppConfigReceiver = XposedHelpers.getAdditionalInstanceField(act, "fetchAppConfigReceiver")
                    if (fetchAppConfigReceiver is BroadcastReceiver) {
                        try { act.unregisterReceiver(fetchAppConfigReceiver) } catch (ignore: Throwable) {}
                    }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }
}
