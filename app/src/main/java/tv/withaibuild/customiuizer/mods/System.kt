package tv.withaibuild.customiuizer.mods

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.MiuiNotification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.WallpaperManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BadParcelableException
import android.os.Binder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.os.UserHandle
import android.provider.MediaStore
import android.telephony.PhoneStateListener
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.ArrayMap
import android.util.Pair
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import miui.os.Build
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import org.json.JSONObject
import org.luckypray.dexkit.query.FindMethod
import org.luckypray.dexkit.query.matchers.MethodMatcher
import org.luckypray.dexkit.result.MethodData
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.ResourceConstants
import tv.withaibuild.customiuizer.mods.utils.ResourceHooks
import tv.withaibuild.customiuizer.mods.utils.WeatherDataController
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.AudioVisualizer
import tv.withaibuild.customiuizer.utils.Helpers
import tv.withaibuild.customiuizer.utils.Helpers.MimeType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collection
import java.util.Collections
import java.util.Date
import java.util.HashSet
import java.util.Iterator
import java.util.List
import java.util.Locale
import java.util.Map
import java.util.Properties
import java.util.TimeZone
import java.util.function.Consumer

object System {

    @JvmStatic
    fun ScreenAnimHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "initialize", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    try {
                        XposedHelpers.setObjectField(thisObject, "mColorFadeEnabled", true)
                        XposedHelpers.setObjectField(thisObject, "mColorFadeFadesConfig", true)
                    } catch (ignore: Throwable) {}

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

                    val mColorFadeOffAnimator = XposedHelpers.getObjectField(thisObject, "mColorFadeOffAnimator") as ObjectAnimator?
                    if (mColorFadeOffAnimator != null) {
                        var value = MainModule.mPrefs.getInt("system_screenanim_duration", 0)
                        if (value == 0) value = 250
                        mColorFadeOffAnimator.duration = value.toLong()
                    }
                    ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                        override fun onChange(key: String?) {
                            if (key?.contains("system_screenanim_duration") == true) {
                                if (mColorFadeOffAnimator == null) return
                                var value2 = MainModule.mPrefs.getInt("system_screenanim_duration", 0)
                                if (value2 == 0) value2 = 250
                                mColorFadeOffAnimator.duration = value2.toLong()
                            }
                        }
                    }, thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun NoAccessDeviceLogsRequest(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.logcat.LogcatManagerService", lpparam.classLoader, "onLogAccessRequested", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    XposedHelpers.callMethod(thisObject, "declineRequest", args[0])
                    skipped = true; result = null; throwable = null

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
    fun NoLightUpOnChargeHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.power.PowerManagerService", lpparam.classLoader, "wakePowerGroupLocked", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val reason = args[3] as String?
                    if (reason == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    if (
                        reason.startsWith("android.server.power:PLUGGED")
                        || reason == "com.android.systemui:RAPID_CHARGE"
                        || reason == "com.android.systemui:WIRELESS_CHARGE"
                        || reason == "com.android.systemui:WIRELESS_RAPID_CHARGE"
                    ) { skipped = true; result = null; throwable = null }

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
    fun ScramblePINHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardPINView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mViews = XposedHelpers.getObjectField(thisObject, "mViews") as Array<Array<View?>>?
                    val mRandomViews = ArrayList<View>()
                    if (mViews != null) {
                        for (row in 1..3) {
                            for (col in 0..2) {
                                mViews[row][col]?.let { mRandomViews.add(it) }
                            }
                        }
                        mViews[4][1]?.let { mRandomViews.add(it) }
                        Collections.shuffle(mRandomViews)

                        val pinview = thisObject as View
                        val row1 = pinview.findViewById<ViewGroup>(Helpers.getResId(pinview.resources, "row1", "id", "com.android.systemui"))
                        val row2 = pinview.findViewById<ViewGroup>(Helpers.getResId(pinview.resources, "row2", "id", "com.android.systemui"))
                        val row3 = pinview.findViewById<ViewGroup>(Helpers.getResId(pinview.resources, "row3", "id", "com.android.systemui"))
                        val row4 = pinview.findViewById<ViewGroup>(Helpers.getResId(pinview.resources, "row4", "id", "com.android.systemui"))

                        row1.removeAllViews()
                        row2.removeAllViews()
                        row3.removeAllViews()
                        row4.removeViewAt(1)

                        mViews[1] = arrayOf(mRandomViews[0], mRandomViews[1], mRandomViews[2])
                        row1.addView(mRandomViews[0])
                        row1.addView(mRandomViews[1])
                        row1.addView(mRandomViews[2])

                        mViews[2] = arrayOf(mRandomViews[3], mRandomViews[4], mRandomViews[5])
                        row2.addView(mRandomViews[3])
                        row2.addView(mRandomViews[4])
                        row2.addView(mRandomViews[5])

                        mViews[3] = arrayOf(mRandomViews[6], mRandomViews[7], mRandomViews[8])
                        row3.addView(mRandomViews[6])
                        row3.addView(mRandomViews[7])
                        row3.addView(mRandomViews[8])

                        mViews[4] = arrayOf(null, mRandomViews[9], mViews[4][2])
                        row4.addView(mRandomViews[9], 1)

                        XposedHelpers.setObjectField(thisObject, "mViews", mViews)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun NoPasswordHook(lpparam: PackageReadyParam) {
        val isAllowed = "isBiometricAllowedForUser"
        ModuleHelper.findAndHookMethod("com.android.internal.widget.LockPatternUtils\$StrongAuthTracker", lpparam.classLoader, isAllowed, Boolean::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("com.android.internal.widget.LockPatternUtils", lpparam.classLoader, isAllowed, Int::class.javaPrimitiveType!!, HookerClassHelper.returnConstant(true))
    }

    @JvmStatic
    fun EnhancedSecurityHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "interceptPowerKeyDown", KeyEvent::class.java, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mPWMContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val kgMgr = mPWMContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    if (kgMgr.isKeyguardLocked && kgMgr.isKeyguardSecure) {
                        val mHandler = XposedHelpers.getObjectField(thisObject, "mHandler") as Handler?
                        if (mHandler != null) {
                            val mEndCallLongPress = XposedHelpers.getObjectField(thisObject, "mEndCallLongPress") as Runnable?
                            if (mEndCallLongPress != null) mHandler.removeCallbacks(mEndCallLongPress)
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        val preventPowerHook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    val mPWMContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val kgMgr = mPWMContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    if (kgMgr.isKeyguardLocked && kgMgr.isKeyguardSecure) { skipped = true; result = null; throwable = null }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }

        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "powerLongPress", Long::class.javaPrimitiveType!!, preventPowerHook)
        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActions", preventPowerHook)
        ModuleHelper.findAndHookMethod("com.android.server.policy.PhoneWindowManager", lpparam.classLoader, "showGlobalActionsInternal", preventPowerHook)
    }

    private fun isAuthOnce(): Boolean {
        val req = MainModule.mPrefs.getStringAsInt("system_noscreenlock_req", 1)
        if (req <= 1) return true
        if (req == 2 && !isUnlockedWithFingerprint && !isUnlockedWithStrong) return false
        if (req == 3 && !isUnlockedWithStrong) return false
        return true
    }

    private fun isTrusted(mContext: Context, classLoader: ClassLoader): Boolean {
        return isTrustedWiFi(mContext) || isTrustedBt(classLoader)
    }

    private fun isTrustedWiFi(mContext: Context): Boolean {
        val wifiManager = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        if (wifiManager == null || !wifiManager.isWifiEnabled) return false
        val trustedNetworks = MainModule.mPrefs.getStringSet("system_noscreenlock_wifi")
        val bssid = wifiManager.connectionInfo.bssid ?: ""
        return Helpers.containsStringPair(trustedNetworks, bssid)
    }

    @SuppressLint("MissingPermission")
    private fun isTrustedBt(classLoader: ClassLoader): Boolean {
        try {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled) return false
            val trustedDevices = MainModule.mPrefs.getStringSet("system_noscreenlock_bt")
            val mController = ModuleHelper.getDepInstance(classLoader, "com.android.systemui.statusbar.policy.BluetoothController")
            val cachedDevices = XposedHelpers.callMethod(mController, "getDevices") as Collection<*>?
            if (cachedDevices != null) {
                for (device in cachedDevices) {
                    val mDevice = XposedHelpers.getObjectField(device, "mDevice") as BluetoothDevice?
                    if (mDevice == null) continue
                    if (mDevice.bondState == BluetoothDevice.BOND_BONDED &&
                        XposedHelpers.callMethod(device, "isConnected") as Boolean &&
                        Helpers.containsStringPair(trustedDevices, mDevice.address ?: "")
                    ) return true
                }
            }
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
        return false
    }

    private fun isUnlocked(mContext: Context, classLoader: ClassLoader): Boolean {
        if (!isAuthOnce()) return false
        var opt = MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1)
        if (forcedOption == 1) opt = 2
        if (opt == 2) return true
        return if (opt == 3) isTrusted(mContext, classLoader) else false
    }

    private var isUnlockedInnerCall = false
    private var isUnlockedWithFingerprint = false
    private var isUnlockedWithStrong = false
    private var isChargingInfoHooked = false
    private var forcedOption = -1

    @JvmStatic
    fun NoScreenLockHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "handleKeyguardDone", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    if (isUnlockedInnerCall) {
                        isUnlockedInnerCall = false
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }
                    isUnlockedWithStrong = true

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader, "onFingerprintAuthenticated", Int::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    isUnlockedWithFingerprint = true

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardSecurityContainerController", lpparam.classLoader, "onInit", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mContext = XposedHelpers.callMethod(thisObject, "getContext") as Context
                    val oldunlockStrongAuthReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "unlockStrongAuthReceiver")
                    if (oldunlockStrongAuthReceiver is BroadcastReceiver) {
                        try { mContext.unregisterReceiver(oldunlockStrongAuthReceiver) } catch (ignore: Throwable) {}
                    }
                    val unlockStrongAuthReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            try {
                                val mCallback = XposedHelpers.getObjectField(thisObject, "mKeyguardSecurityCallback")
                                XposedHelpers.callMethod(mCallback, "reportUnlockAttempt", 0, 0, 0, true)
                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                        }
                    }
                    XposedHelpers.setAdditionalInstanceField(thisObject, "unlockStrongAuthReceiver", unlockStrongAuthReceiver)
                    mContext.registerReceiver(unlockStrongAuthReceiver, IntentFilter(GlobalActions.ACTION_PREFIX + "UnlockStrongAuth"), Context.RECEIVER_NOT_EXPORTED)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "doKeyguardLocked", Bundle::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    if (forcedOption == 0) { return XposedHelpers.proceedOrThrow(chain, throwable) }
                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    if (!isUnlocked(mContext, lpparam.classLoader)) { return XposedHelpers.proceedOrThrow(chain, throwable) }

                    val skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip")
                    if (skip) {
                        XposedHelpers.callMethod(thisObject, "keyguardDone")
                        skipped = true; result = null; throwable = null
                    }
                    isUnlockedInnerCall = true
                    val unlockIntent = Intent(GlobalActions.ACTION_PREFIX + "UnlockStrongAuth")
                    unlockIntent.setPackage("com.android.systemui")
                    mContext.sendBroadcast(unlockIntent)

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.keyguard.KeyguardViewMediator", lpparam.classLoader, "setupLocked", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val filter = IntentFilter()
                    filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                    filter.addAction(GlobalActions.ACTION_PREFIX + "UnlockSetForced")
                    filter.addAction(GlobalActions.ACTION_PREFIX + "BTConnectionChanged")
                    val oldnoScreenLockReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "noScreenLockReceiver")
                    if (oldnoScreenLockReceiver is BroadcastReceiver) {
                        try { mContext.unregisterReceiver(oldnoScreenLockReceiver) } catch (ignore: Throwable) {}
                    }
                    val noScreenLockReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val action = intent.action ?: return

                            if (action == GlobalActions.ACTION_PREFIX + "UnlockSetForced")
                                forcedOption = intent.getIntExtra("system_noscreenlock_force", -1)

                            val isShowing = XposedHelpers.getBooleanField(thisObject, "mShowing")
                            if (!isShowing) return
                            if (!isAuthOnce()) return

                            var isTrusted = false
                            if (forcedOption == 1) isTrusted = true
                            else if (forcedOption != 0 && MainModule.mPrefs.getStringAsInt("system_noscreenlock", 1) == 3) {
                                if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                                    val netInfo = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                                    if (netInfo == null) return
                                    if (netInfo.state != NetworkInfo.State.CONNECTED && netInfo.state != NetworkInfo.State.DISCONNECTED)
                                        return
                                    if (netInfo.isConnected) isTrusted = isTrustedWiFi(mContext)
                                } else if (action == GlobalActions.ACTION_PREFIX + "BTConnectionChanged") {
                                    isTrusted = isTrustedBt(lpparam.classLoader)
                                }
                            }

                            if (isTrusted) {
                                val skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip")
                                if (skip)
                                    XposedHelpers.callMethod(thisObject, "keyguardDone")
                                else
                                    XposedHelpers.callMethod(thisObject, "resetStateLocked", false)
                                isUnlockedInnerCall = true
                                val unlockIntent = Intent(GlobalActions.ACTION_PREFIX + "UnlockStrongAuth")
                                unlockIntent.setPackage("com.android.systemui")
                                mContext.sendBroadcast(unlockIntent)
                            } else try {
                                XposedHelpers.callMethod(thisObject, "resetStateLocked", true)
                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                        }
                    }
                    XposedHelpers.setAdditionalInstanceField(thisObject, "noScreenLockReceiver", noScreenLockReceiver)
                    mContext.registerReceiver(noScreenLockReceiver, filter, Context.RECEIVER_EXPORTED)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardSecurityModel", lpparam.classLoader, "getSecurityMode", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    if (forcedOption == 0) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val skip = MainModule.mPrefs.getBoolean("system_noscreenlock_skip")
                    if (skip) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mKeyguardUpdateMonitor = XposedHelpers.getObjectField(thisObject, "mKeyguardUpdateMonitor")
                    val mContext = XposedHelpers.getObjectField(mKeyguardUpdateMonitor, "mContext") as Context
                    if (!isUnlocked(mContext, lpparam.classLoader)) { return XposedHelpers.throwOrReturn(throwable, result) }

                    val securityModeEnum = XposedHelpers.findClass("com.android.keyguard.KeyguardSecurityModel\$SecurityMode", lpparam.classLoader)
                    val securityModeNone = XposedHelpers.getStaticObjectField(securityModeEnum, "None")
                    val securityModePassword = XposedHelpers.getStaticObjectField(securityModeEnum, "Password")
                    val securityModePattern = XposedHelpers.getStaticObjectField(securityModeEnum, "Pattern")
                    val securityModePin = XposedHelpers.getStaticObjectField(securityModeEnum, "PIN")

                    val secModeResult = result
                    if (securityModePassword == secModeResult ||
                        securityModePattern == secModeResult ||
                        securityModePin == secModeResult
                    ) { result = securityModeNone; throwable = null }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val mContext = args[0] as Context
                    val oldfetchCachedDevicesReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "fetchCachedDevicesReceiver")
                    if (oldfetchCachedDevicesReceiver is BroadcastReceiver) {
                        try { mContext.unregisterReceiver(oldfetchCachedDevicesReceiver) } catch (ignore: Throwable) {}
                    }
                    val fetchCachedDevicesReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val deviceList = ArrayList<BluetoothDevice>()
                            val updateIntent = Intent(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE")
                            val cachedDevices = XposedHelpers.callMethod(thisObject, "getDevices") as Collection<*>?
                            if (cachedDevices != null) {
                                for (device in cachedDevices) {
                                    val mDevice = XposedHelpers.getObjectField(device, "mDevice") as BluetoothDevice?
                                    if (mDevice != null) deviceList.add(mDevice)
                                }
                            }
                            updateIntent.putParcelableArrayListExtra("device_list", deviceList)
                            updateIntent.setPackage(Helpers.modulePkg)
                            mContext.sendBroadcast(updateIntent)
                        }
                    }
                    XposedHelpers.setAdditionalInstanceField(thisObject, "fetchCachedDevicesReceiver", fetchCachedDevicesReceiver)
                    mContext.registerReceiver(fetchCachedDevicesReceiver, IntentFilter(GlobalActions.ACTION_PREFIX + "FetchCachedDevices"), Context.RECEIVER_EXPORTED)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader, "updateConnected", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context?
                    if (mContext != null) {
                        mContext.sendBroadcast(Intent(GlobalActions.ACTION_PREFIX + "BTConnectionChanged"))
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun DoubleTapToSleepHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.NotificationsQuickSettingsContainer", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val view = thisObject as View
                    ModuleHelper.setViewInfo(view, "currentTouchTime", 0L)
                    ModuleHelper.setViewInfo(view, "currentTouchX", 0F)
                    ModuleHelper.setViewInfo(view, "currentTouchY", 0F)

                    view.setOnTouchListener { v, event ->
                        if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false

                        val lastTouchTime = ModuleHelper.getViewInfo(view, "currentTouchTime") as Long
                        val lastTouchX = ModuleHelper.getViewInfo(view, "currentTouchX") as Float
                        val lastTouchY = ModuleHelper.getViewInfo(view, "currentTouchY") as Float

                        var currentTouchTime = java.lang.System.currentTimeMillis()
                        val currentTouchX = event.x
                        val currentTouchY = event.y

                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(currentTouchX - lastTouchX) < 100F && Math.abs(currentTouchY - lastTouchY) < 100F) {
                            val keyguardMgr = v.context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                            if (keyguardMgr.isKeyguardLocked) GlobalActions.commonSendAction(v.context, "GoToSleep")
                            currentTouchTime = 0L
                        }

                        ModuleHelper.setViewInfo(view, "currentTouchTime", currentTouchTime)
                        ModuleHelper.setViewInfo(view, "currentTouchX", currentTouchX)
                        ModuleHelper.setViewInfo(view, "currentTouchY", currentTouchY)

                        false
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun ViewWifiPasswordHook(lpparam: PackageReadyParam) {
        val titleId = MainModule.resHooks.addFakeResource("system_wifipassword_btn_title", R.string.system_wifipassword_btn_title, "string")
        val dlgTitleId = MainModule.resHooks.addFakeResource("system_wifi_password_dlgtitle", R.string.system_wifi_password_dlgtitle, "string")
        ModuleHelper.hookAllMethods("com.android.settings.wifi.SavedAccessPointPreference", lpparam.classLoader, "onBindViewHolder", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val view = XposedHelpers.getObjectField(thisObject, "mView") as View
                    val btnId = Helpers.getResId(view.resources, "btn_delete", "id", "com.android.settings")
                    val button = view.findViewById<Button>(btnId)
                    button.setText(titleId)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        val wifiSharedKey = arrayOfNulls<String?>(1)
        val passwordTitle = arrayOfNulls<String?>(1)
        ModuleHelper.findAndHookMethod("miuix.appcompat.app.AlertDialog\$Builder", lpparam.classLoader, "setTitle", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (wifiSharedKey[0] != null) {
                        args[0] = dlgTitleId
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("miuix.appcompat.app.AlertDialog\$Builder", lpparam.classLoader, "setMessage", CharSequence::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (wifiSharedKey[0] != null) {
                        var str = args[0] as CharSequence
                        str = "$str\n${passwordTitle[0]}: ${wifiSharedKey[0]}"
                        args[0] = str
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.hookAllMethods("miuix.appcompat.app.AlertDialog", lpparam.classLoader, "onCreate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    if (wifiSharedKey[0] != null) {
                        val messageView = XposedHelpers.callMethod(thisObject, "getMessageView") as TextView
                        messageView.setTextIsSelectable(true)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.hookAllMethods("com.android.settings.wifi.MiuiSavedAccessPointsWifiSettings", lpparam.classLoader, "showDeleteDialog", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    val wifiEntry = args[0]
                    val canShare = XposedHelpers.callMethod(wifiEntry, "canShare") as Boolean
                    if (canShare) {
                        if (passwordTitle[0] == null) {
                            val modRes = ModuleHelper.getModuleRes(XposedHelpers.callMethod(thisObject, "getContext") as Context)
                            passwordTitle[0] = modRes.getString(R.string.system_wifi_password_label)
                        }
                        val mWifiManager = XposedHelpers.getObjectField(thisObject, "mWifiManager")
                        val wifiConfiguration = XposedHelpers.callMethod(wifiEntry, "getWifiConfiguration")
                        val WifiDppUtilsClass = XposedHelpers.findClass("com.android.settings.wifi.dpp.WifiDppUtils", lpparam.classLoader)
                        var sharedKey = XposedHelpers.callStaticMethod(WifiDppUtilsClass, "getPresharedKey", mWifiManager, wifiConfiguration) as String
                        sharedKey = XposedHelpers.callStaticMethod(WifiDppUtilsClass, "removeFirstAndLastDoubleQuotes", sharedKey) as String
                        wifiSharedKey[0] = sharedKey
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

                    val wifiEntry = args[0]
                    val canShare = XposedHelpers.callMethod(wifiEntry, "canShare") as Boolean
                    if (canShare) {
                        wifiSharedKey[0] = null
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun StatusBarClockTweakHook(lpparam: PackageReadyParam) { SystemClockHooks.StatusBarClockTweakHook(lpparam) }

    @JvmStatic
    fun CCClockTweakHook(lpparam: PackageReadyParam) { SystemClockHooks.CCClockTweakHook(lpparam) }

    @JvmStatic
    fun CCClockCenterAlignHook(lpparam: PackageReadyParam) { SystemClockHooks.CCClockCenterAlignHook(lpparam) }

    @JvmStatic
    fun ExpandNotificationsHook(lpparam: PackageReadyParam) {
        val feedbackMethod = "setFeedbackIcon"
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader, feedbackMethod, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    val mOnKeyguard = XposedHelpers.getBooleanField(thisObject, "mOnKeyguard")
                    if (!mOnKeyguard) {
                        val notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getEntry"), "mSbn")
                        val pkgName = XposedHelpers.callMethod(notification, "getPackageName") as String
                        val opt = Integer.parseInt(MainModule.mPrefs.getString("system_expandnotifs", "1") ?: "1")
                        val isSelected = MainModule.mPrefs.getStringSet("system_expandnotifs_apps")?.contains(pkgName) ?: false
                        if ((opt == 2 && !isSelected) || (opt == 3 && isSelected))
                            XposedHelpers.callMethod(thisObject, "setSystemExpanded", true)
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
    fun ExpandHeadsUpHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader, "setHeadsUp", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val mOnKeyguard = XposedHelpers.getBooleanField(thisObject, "mOnKeyguard")
                    val showHeadsUp = args[0] as Boolean
                    if (!mOnKeyguard && showHeadsUp) {
                        val notifyRow = thisObject as View
                        val notification = XposedHelpers.getObjectField(XposedHelpers.callMethod(thisObject, "getEntry"), "mSbn")
                        val pkgName = XposedHelpers.callMethod(notification, "getPackageName") as String
                        val opt = MainModule.mPrefs.getStringAsInt("system_expandheadups", 1)
                        val isSelected = MainModule.mPrefs.getStringSet("system_expandheadups_apps")?.contains(pkgName) ?: false
                        if ((opt == 2 && !isSelected) || (opt == 3 && isSelected)) {
                            val oldExpandNotify = XposedHelpers.getAdditionalInstanceField(thisObject, "expandNotifyRunnable") as Runnable?
                            if (oldExpandNotify != null) notifyRow.removeCallbacks(oldExpandNotify)
                            val expandNotify = Runnable {
                                val mExpandClickListener = XposedHelpers.getObjectField(thisObject, "mExpandClickListener") as View.OnClickListener
                                mExpandClickListener.onClick(notifyRow)
                            }
                            XposedHelpers.setAdditionalInstanceField(thisObject, "expandNotifyRunnable", expandNotify)
                            notifyRow.postDelayed(expandNotify, 60)
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
    fun DrawerBlurRatioHook(lpparam: PackageReadyParam) {
        val mCustomBlurModifier = intArrayOf(0)
        ModuleHelper.hookAllConstructors("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val thisObject = chain.thisObject
                    mCustomBlurModifier[0] = MainModule.mPrefs.getInt("system_drawer_blur", 100)
                    ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                        override fun onChange(key: String?) {
                            if (key?.contains("system_drawer_blur") == true) {
                                mCustomBlurModifier[0] = MainModule.mPrefs.getInt("system_drawer_blur", 100)
                            }
                        }
                    }, thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationShadeDepthController\$updateBlurCallback\$1", lpparam.classLoader, "doFrame", Long::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    val parentCtrl = XposedHelpers.getSurroundingThis(thisObject)
                    val mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt")
                    XposedHelpers.setAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier", mCustomBlurModifier[0])

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

                    val parentCtrl = XposedHelpers.getSurroundingThis(thisObject)
                    val mBlurUtils = XposedHelpers.getObjectField(parentCtrl, "blurUtilsExt")
                    XposedHelpers.removeAdditionalInstanceField(mBlurUtils, "mCustomBlurModifier")

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BlurUtilsExt", lpparam.classLoader, "applyBlur", View::class.java, Float::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    val multiplier = XposedHelpers.getAdditionalInstanceField(thisObject, "mCustomBlurModifier")
                    if (multiplier != null) {
                        val ratio = args[1] as Float
                        val newRatio = ratio * (multiplier as Int) / 100f
                        args[1] = newRatio
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.classLoader, "setBlurRatio", Float::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    args[0] = (args[0] as Float) * mCustomBlurModifier[0] / 100f

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
    fun ChargeAnimationHook(lpparam: PackageReadyParam) {
        val timeout = MainModule.mPrefs.getInt("system_chargeanimtime", 20) * 1000
        ModuleHelper.findAndHookMethod("com.miui.charge.container.MiuiChargeAnimationView", lpparam.classLoader, "getAnimationDuration", HookerClassHelper.returnConstant(timeout))
    }

    private var mMaximumBacklight = 0f
    private var mMinimumBacklight = 0f
    private var backlightMaxLevel = 0

    private fun constrainValue(value: Float): Float {
        var newVal = value
        if (newVal < 0) newVal = 0f
        if (newVal > 1) newVal = 1f

        val limitmin = MainModule.mPrefs.getBoolean("system_autobrightness_limitmin")
        val limitmax = MainModule.mPrefs.getBoolean("system_autobrightness_limitmax")
        val min_pct = MainModule.mPrefs.getInt("system_autobrightness_min", 25)
        val max_pct = MainModule.mPrefs.getInt("system_autobrightness_max", 75)

        val min = Helpers.convertGammaToLinearFloat(min_pct / 100f * backlightMaxLevel, backlightMaxLevel, mMinimumBacklight, mMaximumBacklight)
        val max = Helpers.convertGammaToLinearFloat(max_pct / 100f * backlightMaxLevel, backlightMaxLevel, mMinimumBacklight, mMaximumBacklight)

        if (limitmin && newVal < min) newVal = min
        if (limitmax && newVal > max) newVal = max
        return newVal
    }

    @JvmStatic
    fun AutoBrightnessRangeHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, "clampScreenBrightness", Float::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val value = result as Float
                    if (value >= 0) {
                        val res = constrainValue(value)
                        result = res; throwable = null
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllConstructors("com.android.server.display.AutomaticBrightnessController", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    XposedHelpers.setLongField(thisObject, "mBrighteningLightDebounceConfig", 1000L)
                    XposedHelpers.setLongField(thisObject, "mDarkeningLightDebounceConfig", 1200L)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "clampScreenBrightness", Float::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val value = result as Float
                    if (value >= 0) {
                        val res = constrainValue(value)
                        result = res; throwable = null
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllConstructors("com.android.server.display.DisplayPowerController", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    val res = Resources.getSystem()
                    val minBrightnessLevel = res.getInteger(Helpers.getResId(res, "config_screenBrightnessSettingMinimum", "integer", "android"))
                    val maxBrightnessLevel = res.getInteger(Helpers.getResId(res, "config_screenBrightnessSettingMaximum", "integer", "android"))
                    val backlightBit = res.getInteger(Helpers.getResId(res, "config_backlightBit", "integer", "android.miui"))
                    backlightMaxLevel = (1 shl backlightBit) - 1
                    mMinimumBacklight = (minBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1)
                    mMaximumBacklight = (maxBrightnessLevel - 1) * 1.0f / (backlightMaxLevel - 1)

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
    fun AutoBrightnessAfterScreenOffHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.display.DisplayPowerController", lpparam.classLoader, "setScreenState", Int::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            var stateChanged = false
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    val state = args[0] as Int
                    val reportOnly = args[1] as Boolean
                    val mUseAutoBrightness = XposedHelpers.getBooleanField(thisObject, "mUseAutoBrightness")
                    if (state == 1 && mUseAutoBrightness && !reportOnly) {
                        val mPowerState = XposedHelpers.getObjectField(thisObject, "mPowerState")
                        val mScreenState = XposedHelpers.getIntField(mPowerState, "mScreenState")
                        stateChanged = state != mScreenState
                    } else {
                        stateChanged = false
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

                    if (stateChanged) {
                        val readyToUpdateDisplayState = XposedHelpers.callMethod(thisObject, "readyToUpdateDisplayState") as Boolean
                        if (readyToUpdateDisplayState) {
                            val mHandler = XposedHelpers.getObjectField(thisObject, "mHandler") as Handler
                            val msg = mHandler.obtainMessage(255)
                            mHandler.sendMessage(msg)
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
    fun BetterPopupsHideDelayHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethodSilently(MiuiNotification::class.java, "getFloatTime", HookerClassHelper.returnConstant(0))
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    var delay = MainModule.mPrefs.getInt("system_betterpopups_delay", 0) * 1000
                    if (delay == 0) delay = 5000
                    XposedHelpers.setIntField(thisObject, "mMinimumDisplayTime", delay)
                    XposedHelpers.setIntField(thisObject, "mHeadsUpNotificationDecay", delay)
                    ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                        override fun onChange(key: String?) {
                            if (key?.contains("system_betterpopups_delay") == true) {
                                var delay2 = MainModule.mPrefs.getInt("system_betterpopups_delay", 0) * 1000
                                if (delay2 == 0) delay2 = 5000
                                XposedHelpers.setIntField(thisObject, "mMinimumDisplayTime", delay2)
                                XposedHelpers.setIntField(thisObject, "mHeadsUpNotificationDecay", delay2)
                            }
                        }
                    }, thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun BetterPopupsNoHideHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "removeHeadsUpNotification", HookerClassHelper.DO_NOTHING)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "removeOldHeadsUpNotification", HookerClassHelper.DO_NOTHING)

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager\$HeadsUpEntry", lpparam.classLoader, "updateEntry", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    XposedHelpers.setObjectField(thisObject, "mRemoveHeadsUpRunnable", Runnable { })

                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManager", lpparam.classLoader, "onExpandingFinished", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    XposedHelpers.setBooleanField(thisObject, "mReleaseOnExpandFinish", true)

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
    fun NoVersionCheckHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "checkDowngrade", HookerClassHelper.DO_NOTHING)
    }

    @JvmStatic
    fun ColorizeNotificationCardHook(lpparam: PackageReadyParam) {
        SystemColorizeNotificationHooks.ColorizeNotificationCardHook(lpparam)
    }

    @JvmStatic
    fun QSHapticHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader, "click", View::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mState = XposedHelpers.callMethod(thisObject, "getState")
                    val state = XposedHelpers.getIntField(mState, "state")
                    if (state != 0) {
                        val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                        val ignoreSystem = MainModule.mPrefs.getBoolean("system_qshaptics_ignore")
                        val opt = MainModule.mPrefs.getStringAsInt("system_qshaptics", 1)
                        if (opt == 2)
                            Helpers.performLightVibration(mContext, ignoreSystem)
                        else if (opt == 3)
                            Helpers.performStrongVibration(mContext, ignoreSystem)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun ShowNotificationsAfterUnlockHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.interruption.KeyguardNotificationVisibilityProviderImpl", lpparam.classLoader, "shouldHideNotification", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val notification = XposedHelpers.getObjectField(args[0], "mSbn")
                    XposedHelpers.setObjectField(notification, "mHasShownAfterUnlock", false)

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
    fun NotificationRowMenuHook(lpparam: PackageReadyParam) {
        val appInfoIconResId = MainModule.resHooks.addFakeResource("ic_appinfo", R.drawable.ic_appinfo12, "drawable")
        val forceCloseIconResId = MainModule.resHooks.addFakeResource("ic_forceclose", R.drawable.ic_forceclose12, "drawable")
        val openInFwIconResId = MainModule.resHooks.addFakeResource("ic_openinfw", R.drawable.ic_openinfw, "drawable")
        val appInfoDescId = MainModule.resHooks.addFakeResource("miui_notification_menu_appinfo_title", R.string.system_notifrowmenu_appinfo, "string")
        val forceCloseDescId = MainModule.resHooks.addFakeResource("miui_notification_menu_forceclose_title", R.string.system_notifrowmenu_forceclose, "string")
        val openInFwDescId = MainModule.resHooks.addFakeResource("miui_notification_menu_openinfw_title", R.string.system_notifrowmenu_openinfw, "string")
        MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "notification_menu_icon_padding", 0)
        MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "miui_notification_modal_menu_margin_left_right", 3)
        MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "miui_notification_modal_menu_icon_bg_size", 50)

        val MiuiNotificationMenuItem = XposedHelpers.findClass("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow.MiuiNotificationMenuItem", lpparam.classLoader)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.classLoader, "createMenuViews", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val mMenuItems = XposedHelpers.getObjectField(thisObject, "mMenuItems") as ArrayList<Any>

                    var infoBtn: Any? = null
                    var forceCloseBtn: Any? = null
                    var openFwBtn: Any? = null
                    val MenuItem = MiuiNotificationMenuItem.constructors[0]
                    try {
                        infoBtn = MenuItem.newInstance(mContext, appInfoDescId, null, appInfoIconResId)
                        forceCloseBtn = MenuItem.newInstance(mContext, forceCloseDescId, null, forceCloseIconResId)
                        openFwBtn = MenuItem.newInstance(mContext, openInFwDescId, null, openInFwIconResId)
                    } catch (t1: Throwable) {
                        XposedHelpers.log(t1)
                    }
                    if (infoBtn == null || forceCloseBtn == null || openFwBtn == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val notification = XposedHelpers.getObjectField(thisObject, "mSbn")
                    mMenuItems.add(infoBtn)
                    mMenuItems.add(forceCloseBtn)
                    mMenuItems.add(openFwBtn)
                    val menuMargin = XposedHelpers.getObjectField(thisObject, "mMenuMargin") as Int
                    val mMenuContainer = XposedHelpers.getObjectField(thisObject, "mMenuContainer") as LinearLayout
                    val pkgName = XposedHelpers.callMethod(notification, "getPackageName") as String
                    val mInfoBtn = XposedHelpers.callMethod(infoBtn, "getMenuView") as View
                    var mForceCloseBtn: View? = null
                    if (pkgName != "android") {
                        mForceCloseBtn = XposedHelpers.callMethod(forceCloseBtn, "getMenuView") as View
                    }
                    val mOpenFwBtn = XposedHelpers.callMethod(openFwBtn, "getMenuView") as View
                    val expandNotifyRow = XposedHelpers.getObjectField(thisObject, "mParent")
                    val itemClick = View.OnClickListener { view ->
                        if (view == null) return@OnClickListener
                        val uid = XposedHelpers.getIntField(notification, "mAppUid")
                        var user = 0
                        try {
                            user = XposedHelpers.callStaticMethod(UserHandle::class.java, "getUserId", uid) as Int
                        } catch (t: Throwable) {
                            XposedHelpers.log(t)
                        }

                        if (view == mInfoBtn) {
                            ModuleHelper.openAppInfo(mContext, pkgName, user)
                        } else if (view == mForceCloseBtn) {
                            val am = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                            if (user != 0)
                                XposedHelpers.callMethod(am, "forceStopPackageAsUser", pkgName, user)
                            else
                                XposedHelpers.callMethod(am, "forceStopPackage", pkgName)
                            try {
                                val appName = mContext.packageManager.getApplicationLabel(mContext.packageManager.getApplicationInfo(pkgName, 0))
                                Toast.makeText(mContext, ModuleHelper.getModuleRes(mContext).getString(R.string.force_closed, appName), Toast.LENGTH_SHORT).show()
                            } catch (ignore: Throwable) {}
                        } else if (view == mOpenFwBtn) {
                            val miniWindowPkg = XposedHelpers.callMethod(expandNotifyRow, "getMiniWindowTargetPkg") as String
                            val notifyIntent = XposedHelpers.callMethod(expandNotifyRow, "getPendingIntent") as PendingIntent
                            try {
                                val options = ModuleHelper.getFreeformOptions(mContext, miniWindowPkg, notifyIntent, true)
                                notifyIntent.send(mContext, 0, ModuleHelper.getFreeformIntent(miniWindowPkg), null, null, null, options)
                            } catch (e: PendingIntent.CanceledException) {
                                throw RuntimeException(e)
                            }
                        }
                        val ModalControllerForDep = "com.android.systemui.statusbar.notification.modal.ModalController"
                        val ModalController = ModuleHelper.getDepInstance(lpparam.classLoader, ModalControllerForDep)
                        XposedHelpers.callMethod(ModalController, "animExitModal", "OTHER")
                        val mCommandQueue = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.CommandQueue")
                        XposedHelpers.callMethod(mCommandQueue, "animateCollapsePanels", 0, false)
                    }
                    mInfoBtn.setOnClickListener(itemClick)
                    mOpenFwBtn.setOnClickListener(itemClick)
                    val layoutParams = LinearLayout.LayoutParams(-2, -2)
                    layoutParams.leftMargin = menuMargin * 2
                    layoutParams.rightMargin = menuMargin * 2
                    mMenuContainer.addView(mInfoBtn)
                    if (mForceCloseBtn != null) {
                        mForceCloseBtn.setOnClickListener(itemClick)
                        mMenuContainer.addView(mForceCloseBtn)
                    }
                    mMenuContainer.addView(mOpenFwBtn)
                    val titleId = Helpers.getResId(mContext.resources, "modal_menu_title", "id", "com.android.systemui")
                    val panelWidth = mContext.resources.displayMetrics.widthPixels
                    val menuWidth = (panelWidth / mMenuItems.size) - (menuMargin * 2)
                    mMenuItems.forEach { obj ->
                        val menuView = XposedHelpers.callMethod(obj, "getMenuView") as View
                        menuView.layoutParams = layoutParams
                        menuView.findViewById<TextView>(titleId)?.maxWidth = menuWidth
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private fun checkVibration(pkgName: String, thisObject: Any): Boolean {
        try {
            val opt = XposedHelpers.getAdditionalInstanceField(thisObject, "mVibrationMode") as Int
            val selectedApps = XposedHelpers.getAdditionalInstanceField(thisObject, "mVibrationApps") as Set<String>?
            val isSelected = selectedApps != null && selectedApps.contains(pkgName)
            return (opt == 2 && !isSelected) || (opt == 3 && isSelected)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            return false
        }
    }

    @JvmStatic
    fun SelectiveVibrationHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.vibrator.VibratorManagerService", lpparam.classLoader, "systemReady", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationMode", Integer.parseInt(MainModule.mPrefs.getString("system_vibration", "1") ?: "1"))
                    ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                        override fun onChange(key: String?) {
                            if (key?.endsWith("system_vibration") == true) {
                                XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationMode", MainModule.mPrefs.getStringAsInt("system_vibration", 1))
                            }
                        }
                    }, thisObject)

                    XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationApps", MainModule.mPrefs.getStringSet("system_vibration_apps"))
                    ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                        override fun onChange(key: String?) {
                            if (key?.contains("system_vibration_apps") == true) {
                                XposedHelpers.setAdditionalInstanceField(thisObject, "mVibrationApps", MainModule.mPrefs.getStringSet("system_vibration_apps"))
                            }
                        }
                    }, thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.android.server.vibrator.VibratorManagerService", lpparam.classLoader, "vibrate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    val pkgName = args[1] as String?
                    if (pkgName == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    if (checkVibration(pkgName, thisObject)) { skipped = true; result = null; throwable = null }

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
    fun NoDuckingHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.audio.FocusRequester", lpparam.classLoader, "handleFocusLoss", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if ((args[0] as Int) == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) { skipped = true; result = null; throwable = null }

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
    fun OrientationLockHook(lpparam: SystemServerStartingParam) {
        val windowClass = "com.android.server.wm.DisplayRotation"
        val rotMethod = "rotationForOrientation"
        ModuleHelper.hookAllMethods(windowClass, lpparam.classLoader, rotMethod, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    if ((args[0] as Int) == -1) {
                        val opt = MainModule.mPrefs.getInt("qs_autorotate_state", 0)
                        var prevOrient = args[1] as Int
                        val res = result as Int
                        if (opt == 1) {
                            if (prevOrient != 0 && prevOrient != 2) prevOrient = 0
                            if (res == 1 || res == 3) { result = prevOrient; throwable = null }
                        } else if (opt == 2) {
                            if (prevOrient != 1 && prevOrient != 3) prevOrient = 1
                            if (res == 0 || res == 2) { result = prevOrient; throwable = null }
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
    fun StatusBarHeightHook(lpparam: PackageReadyParam) {
        val opt = MainModule.mPrefs.getInt("system_statusbarheight", 11)
        val heightDpi = if (opt == 11) 27 else opt
        val pkgName = lpparam.packageName
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height_default", heightDpi)
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height", heightDpi)
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height_portrait", heightDpi)
        ModuleHelper.replacePkgAndFrameworkValue(pkgName, "dimen", "status_bar_height_landscape", heightDpi)
    }

    @JvmStatic
    fun HideMemoryCleanHook(lpparam: PackageReadyParam, isInLauncher: Boolean) {
        val raClass = if (isInLauncher) "com.miui.home.recents.views.RecentsContainer" else "com.android.systemui.recents.RecentsActivity"
        if (isInLauncher && XposedHelpers.findClassIfExists(raClass, lpparam.classLoader) == null) return
        ModuleHelper.findAndHookMethod(raClass, lpparam.classLoader, "setupVisible", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mMemoryAndClearContainer = XposedHelpers.getObjectField(thisObject, "mMemoryAndClearContainer") as ViewGroup?
                    if (mMemoryAndClearContainer != null) mMemoryAndClearContainer.visibility = View.GONE

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun StatusBarBackgroundHook(lpparam: PackageReadyParam) { SystemStatusBarBackgroundHooks.StatusBarBackgroundHook(lpparam) }

    @JvmStatic
    fun StatusBarBackgroundCompatHook(lpparam: PackageReadyParam) { SystemStatusBarBackgroundHooks.StatusBarBackgroundCompatHook(lpparam) }

    private fun checkToast(pkgName: String): Boolean {
        try {
            val opt = MainModule.mPrefs.getStringAsInt("system_blocktoasts", 1)
            val selectedApps = MainModule.mPrefs.getStringSet("system_blocktoasts_apps")
            val isSelected = selectedApps != null && selectedApps.contains(pkgName)
            return (opt == 2 && !isSelected) || (opt == 3 && isSelected)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            return false
        }
    }

    @JvmStatic
    fun SelectiveToastsHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.notification.NotificationManagerService", lpparam.classLoader, "tryShowToast", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val pkgName = XposedHelpers.getObjectField(args[0], "pkg") as String?
                    if (pkgName == null) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    if (checkToast(pkgName)) { skipped = true; result = false; throwable = null }

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
    fun CleanShareMenuHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", lpparam.classLoader, "run", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mOriginalIntent = XposedHelpers.getObjectField(thisObject, "mOriginalIntent") as Intent?
                    if (mOriginalIntent == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val action = mOriginalIntent.action
                    if (action == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    if (action != Intent.ACTION_SEND && action != Intent.ACTION_SENDTO && action != Intent.ACTION_SEND_MULTIPLE) { return XposedHelpers.throwOrReturn(throwable, result) }
                    if (mOriginalIntent.dataString != null && mOriginalIntent.dataString!!.contains(":")) { return XposedHelpers.throwOrReturn(throwable, result) }

                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val mAimPackageName = XposedHelpers.getObjectField(thisObject, "mAimPackageName") as String?
                    if (mAimPackageName == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps") ?: emptySet<String>()
                    val mRootView = XposedHelpers.getObjectField(thisObject, "mRootView") as View
                    val appResId1 = Helpers.getResId(mContext.resources, "app1", "id", "android.miui")
                    val appResId2 = Helpers.getResId(mContext.resources, "app2", "id", "android.miui")
                    val removeOriginal = selectedApps.contains(mAimPackageName) || selectedApps.contains(mAimPackageName + "|0")
                    val removeDual = selectedApps.contains(mAimPackageName + "|999")
                    val originalApp = mRootView.findViewById<View>(appResId1)
                    val dualApp = mRootView.findViewById<View>(appResId2)
                    if (removeOriginal) dualApp?.performClick()
                    else if (removeDual) originalApp?.performClick()

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun CleanShareMenuServiceHook(lpparam: SystemServerStartingParam) {
        val hook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    try {
                        if (args[0] == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                        if (args.size < 6) { return XposedHelpers.throwOrReturn(throwable, result) }
                        val origIntent = args[0] as Intent
                        val intent = origIntent.clone() as Intent
                        val action = intent.action
                        if (action == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SENDTO && action != Intent.ACTION_SEND_MULTIPLE) { return XposedHelpers.throwOrReturn(throwable, result) }
                        if (intent.dataString != null && intent.dataString!!.contains(":")) { return XposedHelpers.throwOrReturn(throwable, result) }
                        if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) { return XposedHelpers.throwOrReturn(throwable, result) }
                        val selectedApps = MainModule.mPrefs.getStringSet("system_cleanshare_apps") ?: emptySet<String>()
                        val resolved = result as? List<ResolveInfo> ?: return XposedHelpers.throwOrReturn(throwable, result)
                        val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                        val pm = mContext.packageManager
                        val itr = resolved.iterator()
                        while (itr.hasNext()) {
                            val resolveInfo = itr.next()
                            val removeOriginal = selectedApps.contains(resolveInfo.activityInfo.packageName) || selectedApps.contains(resolveInfo.activityInfo.packageName + "|0")
                            val removeDual = selectedApps.contains(resolveInfo.activityInfo.packageName + "|999")
                            var hasDual = false
                            try {
                                hasDual = XposedHelpers.callMethod(pm, "getPackageInfoAsUser", resolveInfo.activityInfo.packageName, 0, 999) != null
                            } catch (ignore: Throwable) {}
                            if ((removeOriginal && !hasDual) || (removeOriginal && hasDual && removeDual)) itr.remove()
                        }
                        result = resolved; throwable = null
                    } catch (t: Throwable) {
                        if (t !is BadParcelableException) XposedHelpers.log(t)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }

        val ActQueryService = "com.android.server.pm.ComputerEngine"
        ModuleHelper.hookAllMethods(ActQueryService, lpparam.classLoader, "queryIntentActivitiesInternal", hook)
    }

    private fun hideMimeType(mimeFlags: Int, mimeType: String?): Boolean {
        var dataType = MimeType.OTHERS
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) dataType = MimeType.IMAGE
            else if (mimeType.startsWith("audio/")) dataType = MimeType.AUDIO
            else if (mimeType.startsWith("video/")) dataType = MimeType.VIDEO
            else if (mimeType.startsWith("text/") ||
                mimeType.startsWith("application/pdf") ||
                mimeType.startsWith("application/msword") ||
                mimeType.startsWith("application/vnd.ms-") ||
                mimeType.startsWith("application/vnd.openxmlformats-")) dataType = MimeType.DOCUMENT
            else if (mimeType.startsWith("application/vnd.android.package-archive") ||
                mimeType.startsWith("application/zip") ||
                mimeType.startsWith("application/x-zip") ||
                mimeType.startsWith("application/octet-stream") ||
                mimeType.startsWith("application/rar") ||
                mimeType.startsWith("application/x-rar") ||
                mimeType.startsWith("application/x-tar") ||
                mimeType.startsWith("application/x-bzip") ||
                mimeType.startsWith("application/gzip") ||
                mimeType.startsWith("application/x-lz") ||
                mimeType.startsWith("application/x-compress") ||
                mimeType.startsWith("application/x-7z") ||
                mimeType.startsWith("application/java-archive")) dataType = MimeType.ARCHIVE
            else if (mimeType.startsWith("link/")) dataType = MimeType.LINK
        }
        return (mimeFlags and dataType) == dataType
    }

    private fun getContentType(context: Context, intent: Intent): String? {
        val scheme = intent.scheme
        val linkSchemes = scheme == "http" || scheme == "https" || scheme == "vnd.youtube"
        var mimeType = intent.type
        if (mimeType == null && linkSchemes) mimeType = "link/*"
        if (mimeType == null && intent.data != null) try {
            mimeType = context.contentResolver.getType(intent.data!!)
        } catch (ignore: Throwable) {}
        return mimeType
    }

    private fun isRemoveApp(isDynamic: Boolean, context: Context, pkgName: String, selectedApps: Set<String>, mimeType: String?): Pair<Boolean, Boolean> {
        val key = "system_cleanopenwith_apps"
        val mimeFlags0: Int
        val mimeFlags999: Int
        if (isDynamic) {
            mimeFlags0 = MainModule.mPrefs.getInt("${key}_${pkgName}|0", MimeType.ALL)
            mimeFlags999 = MainModule.mPrefs.getInt("${key}_${pkgName}|999", MimeType.ALL)
        } else {
            mimeFlags0 = MainModule.mPrefs.getInt("${key}_${pkgName}|0", MimeType.ALL)
            mimeFlags999 = MainModule.mPrefs.getInt("${key}_${pkgName}|999", MimeType.ALL)
        }
        val removeOriginal = (selectedApps.contains(pkgName) || selectedApps.contains(pkgName + "|0")) && hideMimeType(mimeFlags0, mimeType)
        val removeDual = selectedApps.contains(pkgName + "|999") && hideMimeType(mimeFlags999, mimeType)
        return Pair(removeOriginal, removeDual)
    }

    @JvmStatic
    fun CleanOpenWithMenuHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("miui.securityspace.XSpaceResolverActivityHelper.ResolverActivityRunner", lpparam.classLoader, "run", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mOriginalIntent = XposedHelpers.getObjectField(thisObject, "mOriginalIntent") as Intent?
                    if (mOriginalIntent == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val action = mOriginalIntent.action
                    if (action != Intent.ACTION_VIEW) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val mAimPackageName = XposedHelpers.getObjectField(thisObject, "mAimPackageName") as String?
                    if (mAimPackageName == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val selectedApps = MainModule.mPrefs.getStringSet("system_cleanopenwith_apps") ?: emptySet<String>()
                    val mimeType = getContentType(mContext, mOriginalIntent)
                    val isRemove = isRemoveApp(true, mContext, mAimPackageName, selectedApps, mimeType)

                    val mRootView = XposedHelpers.getObjectField(thisObject, "mRootView") as View
                    val appResId1 = Helpers.getResId(mContext.resources, "app1", "id", "android.miui")
                    val appResId2 = Helpers.getResId(mContext.resources, "app2", "id", "android.miui")
                    val originalApp = mRootView.findViewById<View>(appResId1)
                    val dualApp = mRootView.findViewById<View>(appResId2)
                    if (isRemove.first) dualApp?.performClick()
                    else if (isRemove.second) originalApp?.performClick()

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun CleanOpenWithMenuServiceHook(lpparam: SystemServerStartingParam) {
        val hook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    try {
                        if (args[0] == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                        if (args.size < 6) { return XposedHelpers.throwOrReturn(throwable, result) }
                        val origIntent = args[0] as Intent
                        val intent = origIntent.clone() as Intent
                        val action = intent.action
                        if (action != Intent.ACTION_VIEW) { return XposedHelpers.throwOrReturn(throwable, result) }
                        if (intent.hasExtra("CustoMIUIzer") && intent.getBooleanExtra("CustoMIUIzer", false)) { return XposedHelpers.throwOrReturn(throwable, result) }
                        val scheme = intent.scheme
                        val validSchemes = scheme == "http" || scheme == "https" || scheme == "vnd.youtube"
                        if (intent.type == null && !validSchemes) { return XposedHelpers.throwOrReturn(throwable, result) }

                        val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                        val mimeType = getContentType(mContext, intent)

                        val key = "system_cleanopenwith_apps"
                        val selectedApps = MainModule.mPrefs.getStringSet(key) ?: emptySet<String>()
                        val resolved = result as? List<ResolveInfo> ?: return XposedHelpers.throwOrReturn(throwable, result)
                        val pm = mContext.packageManager
                        val itr = resolved.iterator()
                        while (itr.hasNext()) {
                            val resolveInfo = itr.next()
                            val isRemove = isRemoveApp(false, mContext, resolveInfo.activityInfo.packageName, selectedApps, mimeType)
                            var hasDual = false
                            try {
                                hasDual = XposedHelpers.callMethod(pm, "getPackageInfoAsUser", resolveInfo.activityInfo.packageName, 0, 999) != null
                            } catch (ignore: Throwable) {}
                            if ((isRemove.first && !hasDual) || (isRemove.first && hasDual && isRemove.second)) itr.remove()
                        }

                        result = resolved; throwable = null
                    } catch (t: Throwable) {
                        if (t !is BadParcelableException) XposedHelpers.log(t)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }

        val ActQueryService = "com.android.server.pm.ComputerEngine"
        ModuleHelper.hookAllMethods(ActQueryService, lpparam.classLoader, "queryIntentActivitiesInternal", hook)
    }

    @JvmStatic
    fun AppLockHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.miui.server.SecurityManagerService", lpparam.classLoader, "removeAccessControlPassLocked", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    if (args[1] != "*") { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    val mode = XposedHelpers.callMethod(thisObject, "getAccessControlLockMode", args[0]) as Int
                    if (mode != 1) { skipped = true; result = null; throwable = null }

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

    private fun saveLastCheck(thisObject: Any, pkgName: String?, userId: Int) {
        var enabled = false
        if (pkgName != null && pkgName != "com.miui.home") enabled = XposedHelpers.callMethod(thisObject, "getApplicationAccessControlEnabledAsUser", pkgName, userId) as Boolean
        val userState = XposedHelpers.callMethod(thisObject, "getUserStateLocked", userId)
        XposedHelpers.setAdditionalInstanceField(userState, "mAccessControlLastCheckSaved",
            if (enabled) ArrayMap<String, Long>(XposedHelpers.getObjectField(userState, "mAccessControlLastCheck") as ArrayMap<String, Long>) else null
        )
    }

    private fun checkLastCheck(thisObject: Any, userId: Int) {
        val userState = XposedHelpers.callMethod(thisObject, "getUserStateLocked", userId)
        val mAccessControlLastCheckSaved = XposedHelpers.getAdditionalInstanceField(userState, "mAccessControlLastCheckSaved") as ArrayMap<String, Long>?
        if (mAccessControlLastCheckSaved == null) return
        val mAccessControlLastCheck = XposedHelpers.getObjectField(userState, "mAccessControlLastCheck") as ArrayMap<String, Long>
        if (mAccessControlLastCheck.size == 0) return
        val timeout = MainModule.mPrefs.getInt("system_applock_timeout", 1) * 60L * 1000L
        for (pair in mAccessControlLastCheck) {
            val pkg = pair.key
            val time = pair.value
            if (mAccessControlLastCheckSaved.containsKey(pkg)) {
                val oldTime = mAccessControlLastCheckSaved[pkg]
                if (time != oldTime) {
                    mAccessControlLastCheck.put(pkg, time + (timeout - 60000L))
                    XposedHelpers.setObjectField(userState, "mAccessControlLastCheck", mAccessControlLastCheck)
                }
            } else {
                mAccessControlLastCheck.put(pkg, time + (timeout - 60000L))
                XposedHelpers.setObjectField(userState, "mAccessControlLastCheck", mAccessControlLastCheck)
            }
        }
    }

    @JvmStatic
    fun AppLockTimeoutHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "addAccessControlPassForUser", String::class.java, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    saveLastCheck(thisObject, args[0] as String?, args[1] as Int)

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

                    checkLastCheck(thisObject, args[1] as Int)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "checkAccessControlPassLocked", String::class.java, Intent::class.java, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    saveLastCheck(thisObject, args[0] as String?, args[2] as Int)

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

                    checkLastCheck(thisObject, args[2] as Int)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService", lpparam.classLoader, "activityResume", Intent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    val intent = args[0] as Intent
                    if (intent.component != null)
                        saveLastCheck(thisObject, intent.component?.packageName, 0)

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

                    val intent = args[0] as Intent
                    if (intent.component != null)
                        checkLastCheck(thisObject, 0)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private var audioViz: AudioVisualizer? = null
    private var isKeyguardShowing = false
    private var isNotificationPanelExpanded = false
    private var mMediaController: MediaController? = null

    private fun updateAudioVisualizerState(context: Context?) {
        if (audioViz == null || context == null) return
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val isMusicPlaying = am != null && am.isMusicActive
        var isPlaying = false
        if (mMediaController == null || mMediaController?.playbackState == null || mMediaController?.playbackState?.state != PlaybackState.STATE_PLAYING) {
            if (audioViz?.showWithControllerOnly == false) isPlaying = isMusicPlaying
        } else {
            isPlaying = isMusicPlaying && mMediaController?.playbackState?.state == PlaybackState.STATE_PLAYING
        }
        audioViz?.updateViewState(isPlaying, isKeyguardShowing, isNotificationPanelExpanded)
    }

    @JvmStatic
    fun AudioVisualizerHook(lpparam: PackageReadyParam) {
        val screenAndDoze = booleanArrayOf(false, false)
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "onViewAttachedToWindow", View::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mNotificationPanel = XposedHelpers.getObjectField(thisObject, "panelView") as FrameLayout?
                    if (mNotificationPanel == null) {
                        XposedHelpers.log("AudioVisualizerHook", "Cannot find mNotificationPanel")
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }

                    val mContext = mNotificationPanel.context
                    val visFrame = FrameLayout(mContext)
                    visFrame.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    val audioVizLocal = AudioVisualizer(mContext)
                    audioViz = audioVizLocal
                    audioVizLocal.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM)
                    audioVizLocal.isClickable = false
                    visFrame.addView(audioVizLocal)
                    visFrame.isClickable = false
                    val themebkg = mNotificationPanel.findViewById<View>(Helpers.getResId(mContext.resources, "keyguard_background_layer", "id", lpparam.packageName))

                    var order = 0
                    if (themebkg != null) order = Math.max(order, mNotificationPanel.indexOfChild(themebkg))
                    mNotificationPanel.addView(visFrame, order + 1)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader, "start", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mScreenObserver = XposedHelpers.getObjectField(thisObject, "mScreenObserver")
                    val ScreenObserverCls = mScreenObserver.javaClass
                    ModuleHelper.findAndHookMethod(ScreenObserverCls, "onScreenTurnedOff", object : MethodHook() {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            var result: Any?
                            var throwable: Throwable? = null
                            try {
                                result = chain.proceed()
                            } catch (t: Throwable) {
                                throwable = t
                                result = null
                            }
                            try {

                                screenAndDoze[0] = false
                                if (audioViz != null) audioViz?.updateScreenOn(false)

                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                    })

                    ModuleHelper.findAndHookMethod(ScreenObserverCls, "onScreenTurnedOn", object : MethodHook() {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            var result: Any?
                            var throwable: Throwable? = null
                            try {
                                result = chain.proceed()
                            } catch (t: Throwable) {
                                throwable = t
                                result = null
                            }
                            try {

                                screenAndDoze[0] = true
                                if (audioViz != null) audioViz?.updateScreenOn(!screenAndDoze[1])

                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                    })

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader, "updateDozingState", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mDozing = XposedHelpers.getBooleanField(thisObject, "mDozing")
                    screenAndDoze[1] = mDozing
                    if (audioViz != null) audioViz?.updateScreenOn(!mDozing && screenAndDoze[0])

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.KeyguardStateControllerImpl", lpparam.classLoader, "notifyKeyguardState", Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val isKeyguardShowingNew = args[0] as Boolean
                    if (isKeyguardShowing != isKeyguardShowingNew) {
                        isKeyguardShowing = isKeyguardShowingNew
                        isNotificationPanelExpanded = false
                        updateAudioVisualizerState(XposedHelpers.getObjectField(thisObject, "mContext") as Context)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "updatePanelExpanded", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val isNotificationPanelExpandedNew = XposedHelpers.getBooleanField(thisObject, "mPanelExpanded")
                    if (isNotificationPanelExpanded != isNotificationPanelExpandedNew) {
                        isNotificationPanelExpanded = isNotificationPanelExpandedNew
                        val mNotificationPanel = XposedHelpers.getObjectField(thisObject, "panelView") as FrameLayout
                        updateAudioVisualizerState(mNotificationPanel.context)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "updateMediaMetaData", Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    if (audioViz == null) { return XposedHelpers.proceedOrThrow(chain, throwable) }
                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    if (!screenAndDoze[0] || screenAndDoze[1]) {
                        audioViz?.updateScreenOn(false)
                        return XposedHelpers.proceedOrThrow(chain, throwable)
                    } else audioViz?.isScreenOn = true

                    val mMediaMetadata = XposedHelpers.getObjectField(thisObject, "mMediaMetadata") as MediaMetadata?
                    var art: Bitmap? = null
                    if (mMediaMetadata != null) {
                        art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                        if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                        if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
                    }
                    if (art == null) {
                        val wallpaperMgr = WallpaperManager.getInstance(mContext)
                        @SuppressLint("MissingPermission")
                        val wallpaperDrawable = wallpaperMgr.drawable
                        if (wallpaperDrawable is BitmapDrawable) {
                            art = wallpaperDrawable.bitmap
                        }
                    }

                    mMediaController = XposedHelpers.getObjectField(thisObject, "mMediaController") as MediaController?
                    updateAudioVisualizerState(mContext)
                    audioViz?.updateMusicArt(art)

                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private var audioFocusPkg: String? = null

    private fun removeListener(thisObject: Any) {
        val mRecords = XposedHelpers.getObjectField(thisObject, "mRecords") as ArrayList<Any>?
        if (mRecords == null) return
        for (record in mRecords) {
            val callingPackage = XposedHelpers.getObjectField(record, "callingPackage") as String?
            val events = XposedHelpers.getIntField(record, "events")
            val selectedApps = MainModule.mPrefs.getStringSet("system_ignorecalls_apps")
            if ((events and PhoneStateListener.LISTEN_CALL_STATE) == PhoneStateListener.LISTEN_CALL_STATE && callingPackage != null && selectedApps != null && selectedApps.contains(callingPackage)) {
                val newEvents = events and PhoneStateListener.LISTEN_CALL_STATE.inv()
                XposedHelpers.setIntField(record, "events", newEvents)
            }
        }
    }

    @JvmStatic
    fun NoCallInterruptionHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.audio.AudioService", lpparam.classLoader, "requestAudioFocus", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (args[4] == "AudioFocus_For_Phone_Ring_And_Calls" && audioFocusPkg != null && MainModule.mPrefs.getStringSet("system_ignorecalls_apps")?.contains(audioFocusPkg) == true)
                        { skipped = true; result = 1; throwable = null }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                try {
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val res = result as Int
                    if (res != AudioManager.AUDIOFOCUS_REQUEST_FAILED && args[4] != "AudioFocus_For_Phone_Ring_And_Calls")
                        audioFocusPkg = args[5] as String?

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.classLoader, "notifyCallState", Int::class.javaPrimitiveType!!, String::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    removeListener(thisObject)

                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.server.TelephonyRegistry", lpparam.classLoader, "notifyCallStateForPhoneId", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, String::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    removeListener(thisObject)

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
    fun AllRotationsHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllConstructors("com.android.server.wm.DisplayRotation", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    XposedHelpers.setIntField(thisObject, "mAllowAllRotations", if (MainModule.mPrefs.getStringAsInt("system_allrotations2", 1) == 2) 1 else 0)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideIconsBattery1Hook(lpparam: PackageReadyParam) { SystemStatusBarIconHooks.HideIconsBattery1Hook(lpparam) }

    @JvmStatic
    fun HideIconsBattery2Hook(lpparam: PackageReadyParam) { SystemStatusBarIconHooks.HideIconsBattery2Hook(lpparam) }

    @JvmStatic
    fun HideIconsSelectiveAlarmHook(lpparam: PackageReadyParam) { SystemStatusBarIconHooks.HideIconsSelectiveAlarmHook(lpparam) }

    @JvmStatic
    fun DisplayWifiStandardHook(lpparam: PackageReadyParam) { SystemStatusBarIconHooks.DisplayWifiStandardHook(lpparam) }

    @JvmStatic
    fun ForceCloseHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllConstructors("com.android.server.policy.BaseMiuiPhoneWindowManager", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mSystemKeyPackages = XposedHelpers.getObjectField(thisObject, "mSystemKeyPackages") as HashSet<String>
                    mSystemKeyPackages.remove("com.miui.securitycenter")
                    mSystemKeyPackages.remove("com.miui.securityadd")
                    mSystemKeyPackages.remove("com.android.phone")
                    mSystemKeyPackages.remove("com.android.mms")
                    mSystemKeyPackages.remove("com.android.contacts")
                    mSystemKeyPackages.remove("com.miui.home")
                    mSystemKeyPackages.remove("com.jeejen.family.miui")
                    mSystemKeyPackages.remove("com.miui.backup")
                    mSystemKeyPackages.remove("com.xiaomi.mihomemanager")
                    mSystemKeyPackages.addAll(MainModule.mPrefs.getStringSet("system_forceclose_apps")?.toMutableSet() ?: mutableSetOf())

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun DisableAnyNotificationBlockHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.classLoader, "isBlockable", HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.classLoader, "setBlockable", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
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
    }

    @JvmStatic
    fun DisableAnyNotificationBlockHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.classLoader, "isBlockable", HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("android.app.NotificationChannel", lpparam.classLoader, "setBlockable", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
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
    }

    @JvmStatic
    fun DisableAnyNotificationHook(lpparam: PackageReadyParam) {
        if (lpparam.packageName.contains("systemui")) {
            val NotifyManagerCls = XposedHelpers.findClass("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.classLoader)
            XposedHelpers.setStaticBooleanField(NotifyManagerCls, "USE_WHITE_LISTS", false)
        }
        ModuleHelper.hookAllMethods("miui.util.NotificationFilterHelper", lpparam.classLoader, "isNotificationForcedEnabled", HookerClassHelper.returnConstant(false))
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "isNotificationForcedFor", Context::class.java, String::class.java, HookerClassHelper.returnConstant(false))
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "canSystemNotificationBeBlocked", String::class.java, HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "containNonBlockableChannel", String::class.java, HookerClassHelper.returnConstant(false))
        ModuleHelper.findAndHookMethod("miui.util.NotificationFilterHelper", lpparam.classLoader, "getNotificationForcedEnabledList", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    { skipped = true; result = HashSet<String>(); throwable = null }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
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
    fun NotificationImportanceHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.settings.notification.BaseNotificationSettings", lpparam.classLoader, "setPrefVisible", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val pref = args[0]
                    if (pref != null) {
                        val prefKey = XposedHelpers.callMethod(pref, "getKey") as String?
                        if (prefKey == "importance") {
                            args[1] = true
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
        ModuleHelper.findAndHookMethod("com.android.settings.notification.ChannelNotificationSettings", lpparam.classLoader, "setupChannelDefaultPrefs", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val pref = XposedHelpers.callMethod(thisObject, "findPreference", "importance")
                    XposedHelpers.setObjectField(thisObject, "mImportance", pref)
                    val mBackupImportance = XposedHelpers.getObjectField(thisObject, "mBackupImportance") as Int
                    if (mBackupImportance > 0) {
                        val index = XposedHelpers.callMethod(pref, "findSpinnerIndexOfValue", mBackupImportance.toString()) as Int
                        if (index > -1) {
                            XposedHelpers.callMethod(pref, "setValueIndex", index)
                        }
                        val ImportanceListener = XposedHelpers.findClassIfExists("androidx.preference.Preference\$OnPreferenceChangeListener", lpparam.classLoader)
                            ?: return XposedHelpers.throwOrReturn(throwable, result)
                        val handler = InvocationHandler { _, method, args2 ->
                            if (method.name == "onPreferenceChange") {
                                val mBackupImportance2 = Integer.parseInt(args2[1] as String)
                                XposedHelpers.setObjectField(thisObject, "mBackupImportance", mBackupImportance2)
                                val mChannel = XposedHelpers.getObjectField(thisObject, "mChannel") as NotificationChannel
                                mChannel.importance = mBackupImportance2
                                XposedHelpers.callMethod(mChannel, "lockFields", 4)
                                val mBackend = XposedHelpers.getObjectField(thisObject, "mBackend")
                                val mPkg = XposedHelpers.getObjectField(thisObject, "mPkg") as String
                                val mUid = XposedHelpers.getObjectField(thisObject, "mUid") as Int
                                XposedHelpers.callMethod(mBackend, "updateChannel", mPkg, mUid, mChannel)
                                XposedHelpers.callMethod(thisObject, "updateDependents", false)
                            }
                            true
                        }
                        val mImportanceListener = Proxy.newProxyInstance(
                            lpparam.classLoader,
                            arrayOf(ImportanceListener),
                            handler
                        )
                        XposedHelpers.callMethod(pref, "setOnPreferenceChangeListener", mImportanceListener)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideProximityWarningHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.classLoader, "showHint", HookerClassHelper.DO_NOTHING)
        ModuleHelper.findAndHookMethod("com.android.server.policy.MiuiScreenOnProximityLock", lpparam.classLoader, "prepareHintWindow", HookerClassHelper.DO_NOTHING)
    }

    @JvmStatic
    fun HideLockScreenClockHook(lpparam: PackageReadyParam) {
        val mToAod = booleanArrayOf(false)
        val hideClockHook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {
                    val visibility = args[0] as Int
                    if (visibility == View.VISIBLE && !mToAod[0]) {
                        skipped = true; result = null; throwable = null
                    }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                try {
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        ModuleHelper.findAndHookMethod("com.android.keyguard.clock.KeyguardClockContainer", lpparam.classLoader, "setVisibility", Int::class.javaPrimitiveType!!, hideClockHook)
        ModuleHelper.findAndHookMethod("com.android.keyguard.clock.KeyguardClockContainer", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    XposedHelpers.callMethod(thisObject, "setVisibility", View.GONE)
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.keyguard.clock.KeyguardClockContainer", lpparam.classLoader, "doAnimationToAod", Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {
                    mToAod[0] = args[0] as Boolean
                    if (mToAod[0]) {
                        XposedHelpers.callMethod(thisObject, "setVisibility", View.VISIBLE)
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
                    val mToAodLocal = args[0] as Boolean
                    if (!mToAodLocal) {
                        XposedHelpers.callMethod(thisObject, "setVisibility", View.GONE)
                    }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun FirstVolumePressHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.audio.AudioService\$VolumeController", lpparam.classLoader, "suppressAdjustment", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val streamType = args[0] as Int
                    if (streamType != AudioManager.STREAM_MUSIC) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val isMuteAdjust = args[2] as Boolean
                    if (isMuteAdjust) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mController = XposedHelpers.getObjectField(thisObject, "mController")
                    if (mController == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = false; throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun DisableSystemIntegrityHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("android.util.apk.ApkSignatureVerifier", lpparam.classLoader, "getMinimumSignatureSchemeVersionForTargetSdk", Int::class.javaPrimitiveType!!, HookerClassHelper.returnConstant(1))
    }

    @JvmStatic
    fun NoSignatureVerifyServiceHook(lpparam: SystemServerStartingParam) {
        val SignDetails = XposedHelpers.findClassIfExists("android.content.pm.SigningDetails", lpparam.classLoader) ?: return
        val signUnknown = XposedHelpers.getStaticObjectField(SignDetails, "UNKNOWN")
        ModuleHelper.hookAllMethods(SignDetails, "checkCapability", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    if (thisObject == signUnknown || args[0] == signUnknown) {
                        return XposedHelpers.throwOrReturn(null, false)
                    }
                    val flags = args[1] as Int
                    if (flags != 4) { skipped = true; result = true; throwable = null }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllConstructors("android.util.jar.StrictJarVerifier", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    XposedHelpers.setObjectField(thisObject, "signatureSchemeRollbackProtectionsEnforced", false)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.classLoader, "verifyMessageDigest", HookerClassHelper.returnConstant(true))
        ModuleHelper.hookAllMethods("android.util.jar.StrictJarVerifier", lpparam.classLoader, "verify", HookerClassHelper.returnConstant(true))
        ModuleHelper.hookAllMethods("com.android.server.pm.PackageManagerServiceUtils", lpparam.classLoader, "verifySignatures", HookerClassHelper.returnConstant(false))
        ModuleHelper.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.classLoader, "doesSignatureMatchForPermissions", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val packageName = XposedHelpers.callMethod(args[1], "getPackageName") as String
                    val sourcePackageName = args[0] as String
                    if (sourcePackageName == packageName) {
                        skipped = true; result = true; throwable = null
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
        ModuleHelper.hookAllMethods("com.android.server.pm.InstallPackageHelper", lpparam.classLoader, "cannotInstallWithBadPermissionGroups", HookerClassHelper.returnConstant(false))
        ModuleHelper.hookAllMethods("com.android.server.pm.permission.PermissionManagerServiceImpl", lpparam.classLoader, "shouldGrantPermissionBySignature", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val isSystem = XposedHelpers.callMethod(args[0], "isSystem") as Boolean
                    if (isSystem) {
                        skipped = true; result = true; throwable = null
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
        ModuleHelper.findAndHookMethod("android.content.pm.ApplicationInfo", lpparam.classLoader, "isSignedWithPlatformKey", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val isSystemSign = result as Boolean
                    if (!isSystemSign) {
                        val flags = XposedHelpers.getIntField(thisObject, "flags")
                        result = (flags and 1) != 0 || (flags and 128) != 0
                        throwable = null
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun ScreenDimTimeHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.power.PowerManagerService", lpparam.classLoader, "readConfigurationLocked", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val opt = MainModule.mPrefs.getInt("system_dimtime", 0) / 100f
                    XposedHelpers.setIntField(thisObject, "mMaximumScreenDimDurationConfig", 600000)
                    XposedHelpers.setFloatField(thisObject, "mMaximumScreenDimRatioConfig", opt)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun NoOverscrollAppHook(lpparam: PackageReadyParam) {
        val hookParam = object : MethodHook() {
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
        }

        val sblCls = XposedHelpers.findClassIfExists("miuix.springback.view.SpringBackLayout", lpparam.classLoader)
        if (sblCls != null) {
            ModuleHelper.hookAllConstructors(sblCls, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any?
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val thisObject = chain.thisObject

                        try {
                            XposedHelpers.callMethod(thisObject, "setSpringBackEnable", false)
                        } catch (t: Throwable) {
                            try { XposedHelpers.setBooleanField(thisObject, "mSpringBackEnable", false) } catch (ignore: Throwable) {}
                        }

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
            ModuleHelper.findAndHookMethodSilently(sblCls, "setSpringBackEnable", Boolean::class.javaPrimitiveType!!, hookParam)
        }

        val rrvCls = XposedHelpers.findClassIfExists("androidx.recyclerview.widget.RemixRecyclerView", lpparam.classLoader)
        if (rrvCls != null) {
            ModuleHelper.hookAllConstructors(rrvCls, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any?
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val thisObject = chain.thisObject

                        (thisObject as View).overScrollMode = View.OVER_SCROLL_NEVER
                        try {
                            XposedHelpers.callMethod(thisObject, "setSpringEnabled", false)
                        } catch (t: Throwable) {
                            try { XposedHelpers.setBooleanField(thisObject, "mSpringEnabled", false) } catch (ignore: Throwable) {}
                        }

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
            ModuleHelper.findAndHookMethodSilently(rrvCls, "setSpringEnabled", Boolean::class.javaPrimitiveType!!, hookParam)
        }

        ModuleHelper.findAndHookMethod("android.widget.AbsListView", lpparam.classLoader, "initAbsListView", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    (thisObject as AbsListView).overScrollMode = View.OVER_SCROLL_NEVER

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
    fun RemoveSecureHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowState", lpparam.classLoader, "isSecureLocked", HookerClassHelper.returnConstant(false))
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowSurfaceController", lpparam.classLoader, "setSecure", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
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
        ModuleHelper.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    var flags = args[2] as Int
                    val secureFlag = 128
                    flags = flags and secureFlag.inv()
                    args[2] = flags

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.hookAllMethods("com.android.server.wm.WindowManagerServiceImpl", lpparam.classLoader, "notAllowCaptureDisplay", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                try {

                    skipped = true; result = false; throwable = null

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
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
    fun RemoveActStartConfirmHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.miui.server.SecurityManagerService\$LocalService", lpparam.classLoader, "checkAllowStartActivity", HookerClassHelper.returnConstant(true))
    }

    @JvmStatic
    fun AllowAllKeyguardHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.classLoader, "isEnableKeyguard", HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.classLoader, "canShowOnKeyguard", Context::class.java, String::class.java, String::class.java, HookerClassHelper.returnConstant(true))
    }

    @JvmStatic
    fun AllowAllFloatHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.ExpandedNotification", lpparam.classLoader, "isEnableFloat", HookerClassHelper.returnConstant(true))
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.NotificationSettingsManager", lpparam.classLoader, "canFloat", Context::class.java, String::class.java, String::class.java, HookerClassHelper.returnConstant(true))
    }

    private val formatter = SimpleDateFormat("H:m", Locale.ENGLISH)

    @JvmStatic
    fun MuffledVibrationHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.VibratorService", lpparam.classLoader, "doVibratorOn", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    val ratio_ringer = MainModule.mPrefs.getInt("system_vibration_amp_ringer", 100) / 100f
                    val ratio_notif = MainModule.mPrefs.getInt("system_vibration_amp_notif", 100) / 100f
                    val ratio_other = MainModule.mPrefs.getInt("system_vibration_amp_other", 100) / 100f

                    var isRingtone = false
                    var isNotification = false
                    val mCurrentVibration = XposedHelpers.getObjectField(thisObject, "mCurrentVibration")
                    if (mCurrentVibration != null) try {
                        isRingtone = XposedHelpers.callMethod(mCurrentVibration, "isRingtone") as Boolean
                        isNotification = XposedHelpers.callMethod(mCurrentVibration, "isNotification") as Boolean
                    } catch (t: Throwable) {
                        val mUsageHint = XposedHelpers.getIntField(mCurrentVibration, "mUsageHint")
                        isRingtone = mUsageHint == 6
                        isNotification = mUsageHint == 5 || mUsageHint == 7 || mUsageHint == 8 || mUsageHint == 9
                    }

                    val ratio = when {
                        isRingtone -> ratio_ringer
                        isNotification -> ratio_notif
                        else -> ratio_other
                    }
                    if (ratio == 1.0f) {
                        return XposedHelpers.proceedOrThrow(chain, args, throwable)
                    }

                    val key = "system_vibration_amp_period"
                    val start_hour = MainModule.mPrefs.getInt(key + "_start_hour", 0)
                    val start_minute = MainModule.mPrefs.getInt(key + "_start_minute", 0)
                    val end_hour = MainModule.mPrefs.getInt(key + "_end_hour", 0)
                    val end_minute = MainModule.mPrefs.getInt(key + "_end_minute", 0)

                    formatter.timeZone = TimeZone.getDefault()
                    val start = formatter.parse("$start_hour:$start_minute")
                    val end = formatter.parse("$end_hour:$end_minute")
                    val now = formatter.parse(formatter.format(Date()))

                    val insidePeriod = if (start!!.before(end)) now!!.after(start) && now.before(end) else now!!.before(end) || now.after(start)
                    if (!insidePeriod) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }

                    var mSupportsAmplitudeControl = false
                    try {
                        mSupportsAmplitudeControl = XposedHelpers.getBooleanField(thisObject, "mSupportsAmplitudeControl")
                    } catch (ignore: Throwable) {}

                    if (mSupportsAmplitudeControl)
                        args[1] = Math.round((if (args[1] as Int == -1) XposedHelpers.getIntField(thisObject, "mDefaultVibrationAmplitude") else args[1] as Int) * ratio)
                    else
                        args[0] = Math.max(3L, Math.round((args[0] as Long) * ratio.toDouble()))

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private fun hookUpdateTime(alarmTime: TextView) {
        try {
            val mContext = alarmTime.context
            var timestamp = ModuleHelper.getNextMIUIAlarmTime(mContext)
            if (timestamp == 0L && MainModule.mPrefs.getBoolean("system_lsalarm_all"))
                timestamp = Helpers.getNextStockAlarmTime(mContext)
            if (timestamp == 0L) {
                alarmTime.text = ""
                return
            }

            val alarmStr = StringBuilder()
            alarmStr.append(ModuleHelper.getModuleRes(mContext).getString(R.string.system_statusbaricons_alarm_title)).append(": ")
            val format = MainModule.mPrefs.getStringAsInt("system_lsalarm_format", 1)
            if (format == 1 || format == 3) {
                val dateFormat = SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), if (DateFormat.is24HourFormat(mContext)) "EHmm" else "EHmma"), Locale.getDefault())
                dateFormat.timeZone = TimeZone.getDefault()
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = timestamp
                alarmStr.append(dateFormat.format(calendar.time))
            }
            if (format == 2 || format == 3) {
                val timeStr = StringBuilder(DateUtils.getRelativeTimeSpanString(timestamp, java.lang.System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE))
                timeStr[0] = timeStr[0].lowercaseChar()
                alarmStr.append(if (format == 3) " ($timeStr)" else timeStr)
            }
            alarmTime.text = alarmStr
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    @JvmStatic
    fun LockScreenAlarmHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.KeyguardIndicationController", lpparam.classLoader, "setIndicationArea", ViewGroup::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mTopIndicationView = XposedHelpers.getObjectField(thisObject, "mTopIndicationView") as TextView
                    mTopIndicationView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    mTopIndicationView.visibility = View.VISIBLE
                    val MiuiGxzwUtils = XposedHelpers.findClassIfExists("com.miui.keyguard.biometrics.fod.MiuiGxzwUtils", lpparam.classLoader)
                    var hasUdfs = true
                    if (MiuiGxzwUtils != null) {
                        val isGxzwLowPosition = XposedHelpers.callStaticMethod(MiuiGxzwUtils, "isGxzwLowPosition") as Boolean
                        hasUdfs = isGxzwLowPosition
                    }
                    val layoutParams = mTopIndicationView.layoutParams as LinearLayout.LayoutParams
                    layoutParams.bottomMargin = Helpers.dp2px((if (hasUdfs) 80 else 20).toFloat()).toInt()
                    mTopIndicationView.layoutParams = layoutParams
                    val mInitialTextColorState = XposedHelpers.getObjectField(thisObject, "mInitialTextColorState") as ColorStateList
                    mTopIndicationView.setTextColor(mInitialTextColorState)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.KeyguardIndicationController", lpparam.classLoader, "updateDeviceEntryIndication", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mTopIndicationView = XposedHelpers.getObjectField(thisObject, "mTopIndicationView") as TextView
                    hookUpdateTime(mTopIndicationView)
                    val mInitialTextColorState = XposedHelpers.getObjectField(thisObject, "mInitialTextColorState") as ColorStateList
                    mTopIndicationView.setTextColor(mInitialTextColorState)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.keyguard.injector.KeyguardBottomAreaInjector", lpparam.classLoader, "handleBottomButtonClickedAnimation", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val mTopIndicationView = ModuleHelper.getObjectFieldByPath(thisObject, "mKeyguardIndicationInjector.mKeyguardIndicationController.mTopIndicationView") as TextView
                    val showTips = args[0] as Boolean
                    if (showTips) {
                        mTopIndicationView.visibility = View.GONE
                    } else {
                        mTopIndicationView.visibility = View.VISIBLE
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun ScreenshotConfigHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("android.content.ContentResolver", lpparam.classLoader, "update", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (args.size != 4) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    val contentValues = args[1] as ContentValues
                    var displayName = contentValues.getAsString("_display_name")
                    if (displayName != null && displayName.contains("Screenshot")) {
                        val format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2)
                        val ext = if (format <= 2) ".jpg" else if (format == 3) ".png" else ".webp"

                        displayName = displayName.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext
                        contentValues.put("_display_name", displayName)
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("android.content.ContentResolver", lpparam.classLoader, "insert", Uri::class.java, ContentValues::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val imgUri = args[0] as Uri
                    val contentValues = args[1] as ContentValues
                    var displayName = contentValues.getAsString("_display_name")
                    if (MediaStore.Images.Media.EXTERNAL_CONTENT_URI == imgUri && displayName != null && displayName.contains("Screenshot")) {
                        val folder = MainModule.mPrefs.getStringAsInt("system_screenshot_path", 1)
                        val dir = MainModule.mPrefs.getString("system_screenshot_mypath", "")
                        val format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2)
                        val ext = if (format <= 2) ".jpg" else if (format == 3) ".png" else ".webp"

                        var mScreenshotDir: File? = null
                        displayName = displayName.replace(".png", "").replace(".jpg", "").replace(".webp", "") + ext
                        if (folder > 1) {
                            mScreenshotDir = if (folder == 4 && !TextUtils.isEmpty(dir)) File(dir) else File(Environment.getExternalStoragePublicDirectory(if (folder == 2) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_DCIM), "Screenshots")
                            if (!mScreenshotDir.exists()) mScreenshotDir.mkdirs()
                            val relativePath = mScreenshotDir.path.replace(Environment.getExternalStorageDirectory().path + File.separator, "")
                            contentValues.put("relative_path", relativePath)
                            if (contentValues.getAsString("_data") != null) {
                                contentValues.put("_data", mScreenshotDir.path + "/" + displayName)
                            }
                        }
                        contentValues.put("_display_name", displayName)
                    }

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        val format = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2)
        if (format > 2) {
            val methodData = XposedHelpers.bridge.findMethod(FindMethod.create()
                .excludePackages("android", "androidx", "com.xiaomi", "com.google.json", "kotlin", "kotlinx.coroutines", "miuix")
                .matcher(MethodMatcher.create().usingStrings("saveBitmapToUri: external storage"))
            ).firstOrThrow { RuntimeException("Method not found") }

            val changeFormatHook = object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    val args = XposedHelpers.getArgsArray(chain)
                    try {

                        if (args.size < 7) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                        val compress = if (format <= 2) Bitmap.CompressFormat.JPEG else if (format == 3) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.WEBP
                        args[4] = compress

                        result = chain.proceed(args)
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            }
            try {
                val method = methodData.getMethodInstance(lpparam.classLoader)
                ModuleHelper.hookMethod(method, changeFormatHook)
            } catch (ignore: Throwable) {
            }
        }

        ModuleHelper.hookAllMethods("android.graphics.Bitmap", lpparam.classLoader, "compress", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    var quality = args[1] as Int
                    if (quality != 100 || (args[2] is ByteArrayOutputStream)) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    val format2 = MainModule.mPrefs.getStringAsInt("system_screenshot_format", 2)
                    quality = MainModule.mPrefs.getInt("system_screenshot_quality", 100)
                    if (format2 == 3) {
                        quality = 100
                    }
                    val compress = if (format2 <= 2) Bitmap.CompressFormat.JPEG else if (format2 == 3) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.WEBP
                    args[0] = compress
                    args[1] = quality

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
    fun ToastTimeHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.notification.NotificationManagerService", lpparam.classLoader, "showNextToastLocked", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mContext = XposedHelpers.callMethod(thisObject, "getContext") as Context?
                    val mHandler = XposedHelpers.getObjectField(thisObject, "mHandler") as Handler?
                    val mToastQueue = XposedHelpers.getObjectField(thisObject, "mToastQueue") as ArrayList<Any>?
                    if (mContext == null || mHandler == null || mToastQueue == null || mToastQueue.size == 0) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mod = (MainModule.mPrefs.getInt("system_toasttime", 0) - 4) * 1000
                    for (record in mToastQueue)
                        if (mHandler.hasMessages(2, record)) {
                            mHandler.removeCallbacksAndMessages(record)
                            val duration = XposedHelpers.getIntField(record, "duration")
                            val delay = Math.max(1000, (if (duration == 1) 3500 else 2000) + mod)
                            mHandler.sendMessageDelayed(Message.obtain(mHandler, 2, record), delay.toLong())
                        }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        val windowClass = "com.android.server.wm.DisplayPolicy"
        ModuleHelper.hookAllMethods(windowClass, lpparam.classLoader, "adjustWindowParamsLw", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                val thisObject = chain.thisObject
                try {

                    val lp = if (args.size == 1) args[0] else args[1]
                    XposedHelpers.setAdditionalInstanceField(thisObject, "mPrevHideTimeout", XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds"))

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

                    val lp = if (args.size == 1) args[0] else args[1]
                    val mPrevHideTimeout = XposedHelpers.getAdditionalInstanceField(thisObject, "mPrevHideTimeout") as Long
                    val mHideTimeout = XposedHelpers.getLongField(lp, "hideTimeoutMilliseconds")
                    if (mPrevHideTimeout == -1L || mHideTimeout == -1L) { return XposedHelpers.throwOrReturn(throwable, result) }

                    var dur = 0L
                    if (mPrevHideTimeout == 1000L || mPrevHideTimeout == 4000L || mPrevHideTimeout == 5000L || mPrevHideTimeout == 7000L || mPrevHideTimeout != mHideTimeout)
                        dur = Math.max(1000, 3500 + (MainModule.mPrefs.getInt("system_toasttime", 0) - 4) * 1000).toLong()
                    if (dur != 0L) XposedHelpers.setLongField(lp, "hideTimeoutMilliseconds", dur)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun ClearAllTasksHook(lpparam: SystemServerStartingParam) {
        val wpuClass = "com.android.server.wm.WindowProcessUtils"
        ModuleHelper.hookAllMethods(wpuClass, lpparam.classLoader, "getPerceptibleRecentAppList", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    result = null; throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun TapToUnlockHook(lpparam: PackageReadyParam) {
        val NotificationPanelController = XposedHelpers.findClassIfExists("com.android.systemui.shade.NotificationPanelViewController", lpparam.classLoader)
        if (NotificationPanelController == null) {
            XposedHelpers.log("NotificationPanelController not found")
            return
        }

        val mTouchHandlerField = XposedHelpers.findField(NotificationPanelController, "mTouchHandler")
        XposedHelpers.findAndHookMethod(mTouchHandlerField.type, "handleMiuiTouch", MotionEvent::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val event = args[0] as MotionEvent
                    if (event.pointerCount > 1) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val action = event.actionMasked
                    if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val thisObect = XposedHelpers.getSurroundingThis(thisObject)
                    val isOnKeyguard = XposedHelpers.callMethod(thisObect, "isOnKeyguard") as Boolean
                    val mQsController = XposedHelpers.getObjectField(thisObect, "mQsController")
                    val mExpanded = XposedHelpers.getBooleanField(mQsController, "mExpanded")
                    if (isOnKeyguard && !mExpanded) {
                        if (action == MotionEvent.ACTION_UP) {
                            val mKeyguardPanelViewInjector = XposedHelpers.getObjectField(thisObect, "mKeyguardPanelViewInjector")
                            val mKeyguardMoveHelper = XposedHelpers.getObjectField(mKeyguardPanelViewInjector, "mKeyguardMoveHelper")
                            val mCurrentScreen = XposedHelpers.getIntField(mKeyguardMoveHelper, "mCurrentScreen")
                            if (mCurrentScreen == 0) { return XposedHelpers.throwOrReturn(throwable, result) }
                            val keyguardBottomAreaInjector = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.keyguard.injector.KeyguardBottomAreaInjector")
                            if (!XposedHelpers.getBooleanField(keyguardBottomAreaInjector, "mTouchAtKeyguardBottomArea")) { return XposedHelpers.throwOrReturn(throwable, result) }
                            val mContext = XposedHelpers.getObjectField(keyguardBottomAreaInjector, "mContext") as Context
                            val mTouchDownX = XposedHelpers.getFloatField(keyguardBottomAreaInjector, "mTouchDownX")
                            val mTouchDownY = XposedHelpers.getFloatField(keyguardBottomAreaInjector, "mTouchDownY")
                            val slop = ViewConfiguration.get(mContext).scaledTouchSlop
                            if (Math.abs(event.x - mTouchDownX) > slop || Math.abs(event.y - mTouchDownY) > slop)
                                { return XposedHelpers.throwOrReturn(throwable, result) }
                            val statusBarKeyguardViewManager = XposedHelpers.getObjectField(thisObect, "statusBarKeyguardViewManager")
                            XposedHelpers.callMethod(statusBarKeyguardViewManager, "showBouncer", true)
                            result = true; throwable = null
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
    fun TempHideOverlayAppHook(lpparam: SystemServerStartingParam) {
        val flagIndex = 2
        ModuleHelper.hookAllConstructors("com.android.server.wm.WindowSurfaceController", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val windowType = args[4] as Int
                    if (windowType != WindowManager.LayoutParams.TYPE_PHONE
                        && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
                        && windowType != WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                        && windowType != WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                    var flags = args[flagIndex] as Int
                    val skipFlag = 64
                    flags = flags or skipFlag
                    args[flagIndex] = flags

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
    fun GalleryScreenshotPathHook(lpparam: PackageReadyParam) {
        val MIUIStorageConstants = XposedHelpers.findClass("com.miui.gallery.storage.constants.MIUIStorageConstants", lpparam.classLoader)
        val folder = MainModule.mPrefs.getStringAsInt("system_gallery_screenshots_path", 1)
        var ssPath = ""
        if (folder == 2) {
            ssPath = Environment.DIRECTORY_PICTURES + File.separator + "Screenshots"
        } else if (folder == 3) {
            ssPath = Environment.DIRECTORY_DCIM + File.separator + "Screenshots"
        }
        if (folder > 1) {
            XposedHelpers.setStaticObjectField(MIUIStorageConstants, "DIRECTORY_SCREENSHOT_PATH", ssPath)
        }
    }

    @JvmStatic
    fun ScrambleAppLockPINHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.miui.applicationlock.widget.MiuiNumericInputView", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val keys = thisObject as LinearLayout
                    val mRandomViews = ArrayList<View>()
                    var bottom0: View? = null; var bottom2: View? = null
                    for (row in 0..3) {
                        val cols = keys.getChildAt(row) as ViewGroup
                        for (col in 0..2) {
                            if (row == 3)
                                if (col == 0) {
                                    bottom0 = cols.getChildAt(col)
                                    continue
                                } else if (col == 2) {
                                    bottom2 = cols.getChildAt(col)
                                    continue
                                }
                            mRandomViews.add(cols.getChildAt(col))
                        }
                        cols.removeAllViews()
                    }

                    Collections.shuffle(mRandomViews)

                    var cnt = 0
                    for (row in 0..3)
                        for (col in 0..2) {
                            val cols = keys.getChildAt(row) as ViewGroup
                            if (row == 3)
                                if (col == 0) {
                                    bottom0?.let { cols.addView(it) }
                                    continue
                                } else if (col == 2) {
                                    bottom2?.let { cols.addView(it) }
                                    continue
                                }
                            cols.addView(mRandomViews[cnt])
                            cnt++
                        }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun ChargingInfoHook(lpparam: PackageReadyParam) {
        if (isChargingInfoHooked) return
        isChargingInfoHooked = true
        ModuleHelper.findAndHookMethod("com.miui.charge.ChargeUtils", lpparam.classLoader, "getChargingHintText", Int::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, Context::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    val charge = args[0] as Int
                    val hint = result as String?

                    if (charge <= 100 && hint != null && isKeyguardIndicationCaller()) {
                        val showCurr = MainModule.mPrefs.getBoolean("system_charginginfo_current")
                        val showVolt = MainModule.mPrefs.getBoolean("system_charginginfo_voltage")
                        val showWatt = MainModule.mPrefs.getBoolean("system_charginginfo_wattage")
                        val showTemp = MainModule.mPrefs.getBoolean("system_charginginfo_temp")

                        val values = ArrayList<String>()
                        var props: Properties? = null
                        var fis: FileInputStream? = null
                        try {
                            fis = FileInputStream("/sys/class/power_supply/battery/uevent")
                            props = Properties()
                            props.load(fis)
                        } catch (ign: Throwable) {
                        } finally {
                            try {
                                fis?.close()
                            } catch (ign: Throwable) {
                            }
                        }
                        if (props != null) {
                            val currVal = Math.abs(Integer.parseInt(props.getProperty("POWER_SUPPLY_CURRENT_NOW") ?: "0")) / 1000f / 1000f
                            if (showCurr) values.add(String.format(Locale.US, "%.2f", currVal) + " A")
                            val voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW") ?: "0") / 1000f / 1000f
                            if (showVolt)
                                values.add(String.format(Locale.US, "%.1f", voltVal) + " V")
                            if (showWatt)
                                values.add(String.format(Locale.US, "%.1f", voltVal * currVal) + " W")
                            if (showTemp) {
                                val tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP") ?: "0")
                                values.add(Math.round(tempVal / 10f).toString() + " ℃")
                            }
                        }
                        if (values.size == 0) { return XposedHelpers.throwOrReturn(throwable, result) }
                        val info = TextUtils.join(" · ", values)

                        if (hint.contains(info)) { return XposedHelpers.throwOrReturn(throwable, result) }

                        val opt = MainModule.mPrefs.getStringAsInt("system_charginginfo_view", 1)
                        if (opt == 1)
                            { result = hint + "\n" + info; throwable = null }
                        else if (opt == 2)
                            { result = hint + " · " + info; throwable = null }
                        else if (opt == 3)
                            { result = info + " · " + hint; throwable = null }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardIndicationTextView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val opt = MainModule.mPrefs.getStringAsInt("system_charginginfo_view", 1)
                    if (opt != 1) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val indicator = thisObject as TextView
                    indicator.isSingleLine = false

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private fun isKeyguardIndicationCaller(): Boolean {
        try {
            for (e in Thread.currentThread().stackTrace) {
                val className = e.className
                if (className.contains("KeyguardIndication")) return true
                if (className.contains("MiuiCharge") || className.contains("miui.charge")) return false
            }
        } catch (ignore: Throwable) {}
        return false
    }

    @JvmStatic
    fun NoSOSHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.keyguard.EmergencyButtonController", lpparam.classLoader, "updateEmergencyCallButton", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    val mSOS = XposedHelpers.getObjectField(thisObject, "mView") as Button
                    if (mSOS.visibility == View.VISIBLE) {
                        mSOS.visibility = View.INVISIBLE
                    }
                    skipped = true; result = null; throwable = null

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
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
    fun ForceDarkAllAppsHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.ForceDarkAppListProvider", lpparam.classLoader, "fillDarkModeAppSettingsInfo", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    XposedHelpers.callMethod(args[0], "setShowInSettings", true)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        if (!Build.IS_INTERNATIONAL_BUILD) {
            ModuleHelper.findAndHookMethod("com.android.server.ForceDarkAppListManager", lpparam.classLoader, "getDarkModeAppList", Long::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    try {

                        XposedHelpers.setStaticBooleanField(Build::class.java, "IS_INTERNATIONAL_BUILD", true)

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

                        XposedHelpers.setStaticBooleanField(Build::class.java, "IS_INTERNATIONAL_BUILD", false)

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
        }
        ModuleHelper.findAndHookMethod("com.android.server.ForceDarkAppListManager", lpparam.classLoader, "shouldShowInSettings", ApplicationInfo::class.java, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (args[0] == null) {
                        return XposedHelpers.throwOrReturn(null, false)
                    }
                    val applicationInfo = args[0] as ApplicationInfo
                    val flags = applicationInfo.flags
                    val systemApp = (flags and 1) != 0 || (flags and 128) != 0 || applicationInfo.uid < 10000
                    skipped = true; result = !systemApp; throwable = null

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
    fun MaxNotificationIconsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconContainer", lpparam.classLoader, "resetViewStates", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    var opt = MainModule.mPrefs.getStringAsInt("system_maxsbicons", 0)
                    val maxIcons = XposedHelpers.getIntField(thisObject, "mMaxStaticIcons")
                    opt = if (opt == -1) 999 else opt
                    if (opt != maxIcons && maxIcons != 0) {
                        XposedHelpers.setIntField(thisObject, "mMaxStaticIcons", opt)
                        XposedHelpers.setIntField(thisObject, "mMaxIconsOnLockscreen", opt)
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
    fun AutoDismissExpandedPopupsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.HeadsUpManagerPhone\$HeadsUpEntryPhone", lpparam.classLoader, "updateEntry", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val headsUpEntry = thisObject
                    val expanded = XposedHelpers.getBooleanField(headsUpEntry, "expanded")
                    val remoteInputActive = XposedHelpers.getBooleanField(headsUpEntry, "remoteInputActive")
                    val mEntry = XposedHelpers.getObjectField(headsUpEntry, "mEntry")
                    val rowPinned = XposedHelpers.callMethod(mEntry, "isRowPinned") as Boolean
                    if (expanded && rowPinned && !remoteInputActive) {
                        val headsUpManagerPhone = XposedHelpers.getSurroundingThis(headsUpEntry)
                        val mHandler = XposedHelpers.getObjectField(headsUpManagerPhone, "mHandler") as Handler
                        val mRemoveAlertRunnable = XposedHelpers.getObjectField(headsUpEntry, "mRemoveAlertRunnable") as Runnable
                        val extended = XposedHelpers.getBooleanField(headsUpEntry, "extended")
                        mHandler.removeCallbacks(mRemoveAlertRunnable)
                        mHandler.postDelayed(mRemoveAlertRunnable, if (extended) 10000L else 4500L)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarNotificationPresenter", lpparam.classLoader, "onExpandClicked", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    val expanded = args[1] as Boolean
                    val mKeyguardStateController = XposedHelpers.getObjectField(thisObject, "mKeyguardStateController")
                    val mShowing = XposedHelpers.getBooleanField(mKeyguardStateController, "mShowing")
                    if (expanded && !mShowing) {
                        val headsUpManagerPhone = XposedHelpers.getObjectField(thisObject, "mHeadsUpManager")
                        val headsUpEntry = XposedHelpers.callMethod(headsUpManagerPhone, "getHeadsUpEntry", XposedHelpers.getObjectField(args[0], "mKey"))
                        if (headsUpEntry != null) {
                            val isRowPinned = XposedHelpers.callMethod(args[0], "isRowPinned") as Boolean
                            if (isRowPinned) {
                                val mHandler = XposedHelpers.getObjectField(headsUpManagerPhone, "mHandler") as Handler
                                val mRemoveAlertRunnable = XposedHelpers.getObjectField(headsUpEntry, "mRemoveAlertRunnable") as Runnable
                                mHandler.removeCallbacks(mRemoveAlertRunnable)
                                mHandler.postDelayed(mRemoveAlertRunnable, 4500L)
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
    fun BetterPopupsAllowFloatHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow", lpparam.classLoader, "updateMiniWindowBar", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    val pkgName = XposedHelpers.callMethod(thisObject, "getMiniWindowTargetPkg") as String
                    val selectedApps = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps")
                    val selectedAppsBlack = MainModule.mPrefs.getStringSet("system_betterpopups_allowfloat_apps_black")
                    val mAppMiniWindowManager = XposedHelpers.callMethod(thisObject, "getMAppMiniWindowManager")
                    val notificationSettingsManager = XposedHelpers.getObjectField(mAppMiniWindowManager, "notificationSettingsManager")
                    val mAllowNotificationSlide = XposedHelpers.getObjectField(notificationSettingsManager, "mAllowNotificationSlide") as List<String>
                    if (selectedApps?.contains(pkgName) == true) {
                        mAllowNotificationSlide.add(pkgName)
                    } else if (selectedAppsBlack?.contains(pkgName) == true) {
                        mAllowNotificationSlide.remove(pkgName)
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
    private fun DisableFloatingWindowBlacklistHook(cl: ClassLoader) {
        val clearHook = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {

                    val blackList = result as List<String>?
                    if (blackList != null) {
                        blackList.clear()
                        blackList.add("com.android.camera")
                    }
                    result = blackList
                    throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", cl, "getListFromCloudData", clearHook)
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowAdapter", cl, "getStartFromFreeformBlackListFromCloud", clearHook)
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "getFreeformBlackList", clearHook)
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "getFreeformBlackListFromCloud", clearHook)
        ModuleHelper.hookAllMethods("android.util.MiuiMultiWindowAdapter", cl, "setFreeformBlackList", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val blackList = ArrayList<String>()
                    blackList.add("com.android.camera")
                    args[0] = blackList

                    result = chain.proceed(args)
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.findAndHookMethod("android.util.MiuiMultiWindowUtils", cl, "isForceResizeable", HookerClassHelper.returnConstant(true))
        ModuleHelper.hookAllMethodsSilently("android.util.MiuiMultiWindowUtils", cl, "isPkgMainActivityResizeable", HookerClassHelper.returnConstant(true))
    }

    @JvmStatic
    fun DisableSideBarSuggestionHook(lpparam: PackageReadyParam) {
        DisableFloatingWindowBlacklistHook(lpparam.classLoader)
    }

    @JvmStatic
    fun NoFloatingWindowBlacklistHook(lpparam: SystemServerStartingParam) {
        MainModule.resHooks.setThemeValueReplacement("android", "string-array", "freeform_black_list", ResourceConstants.module_resize_black_list)
        DisableFloatingWindowBlacklistHook(lpparam.classLoader)
        ModuleHelper.findAndHookMethod("com.android.server.wm.MiuiFreeformUtilImpl", lpparam.classLoader, "supportsFreeform", HookerClassHelper.returnConstant(true))
    }

    private var freeformCallingPackage: String? = "SkipCheck"
    private var nextFreeformPackage: String? = ModuleHelper.NOT_EXIST_SYMBOL

    private fun shouldOpenInFreeForm(intent: Intent?, callingPackage: String?): Boolean {
        if (intent == null || intent.component == null) return false
        val fwBlackList = ArrayList<String>()
        fwBlackList.add("com.miui.home")
        fwBlackList.add("com.android.camera")
        fwBlackList.add("com.android.systemui")
        val pkgName = intent.component!!.packageName
        if (fwBlackList.contains(pkgName)) return false
        var openInFw = false
        val openFwWhenShare = MainModule.mPrefs.getBoolean("system_fw_forcein_actionsend")
        val compClassName = intent.component!!.className
        if (openFwWhenShare) {
            val whitelist = MainModule.mPrefs.getBoolean("system_fw_forcein_actionsend_in_whitelist")
            val appInList = MainModule.mPrefs.getStringSet("system_fw_forcein_actionsend_apps")?.contains(pkgName) == true
            if (whitelist xor appInList) {
                return false
            }
            if ("com.miui.packageinstaller" == pkgName && compClassName.contains("InstallPrepareAlertActivity")) {
                return true
            }
            if (Intent.ACTION_SEND == intent.action && pkgName != callingPackage) {
                openInFw = true
            } else if ("com.tencent.mm" == pkgName && compClassName.contains(".plugin.base.stub.WXEntryActivity")) {
                openInFw = true
            } else if ("com.tencent.mobileqq" == pkgName && (
                compClassName.contains(".activity.JumpActivity")
                || compClassName.contains(".activity.LoginActivity")
                || compClassName.contains(".agent.AgentActivity")
            )) {
                openInFw = true
            }
        }
        val openSettingFromSystemUI = MainModule.mPrefs.getBoolean("system_cc_freeform_when_longclick")
        if (openSettingFromSystemUI && "com.android.systemui" == callingPackage
            && ("com.android.settings" == pkgName
                || ("com.android.phone" == pkgName && compClassName.contains(".settings.MobileNetworkSettings"))
            )
        ) {
            openInFw = true
        }
        if (!openInFw) {
            openInFw = pkgName == nextFreeformPackage
        }
        return openInFw
    }

    @JvmStatic
    fun OpenAppInFreeFormHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.ActivityTaskManagerService", lpparam.classLoader, "onSystemReady", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val intentFilter = IntentFilter()
                    intentFilter.addAction(GlobalActions.ACTION_PREFIX + "SetFreeFormPackage")
                    val mReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val action = intent.action
                            if (action == null) return

                            if (action == GlobalActions.ACTION_PREFIX + "SetFreeFormPackage") {
                                nextFreeformPackage = intent.getStringExtra("package") ?: ModuleHelper.NOT_EXIST_SYMBOL
                            }
                        }
                    }
                    mContext.registerReceiver(mReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.findAndHookMethod("com.miui.server.SecurityManagerService\$LocalService", lpparam.classLoader, "checkGameBoosterPayPassAsUser", String::class.java, Intent::class.java, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    if (freeformCallingPackage == null || "SkipCheck" == freeformCallingPackage) {
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }
                    if ("com.miui.packageinstaller" != freeformCallingPackage && freeformCallingPackage == args[0]) {
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }
                    var openInFw = result as Boolean
                    if (!openInFw) {
                        val intent = args[1] as Intent
                        openInFw = shouldOpenInFreeForm(intent, freeformCallingPackage)
                    }
                    // XposedHelpers.log("actInfo: " + openInFw + " - " + args[0] + " - " + freeformCallingPackage + " | " + args[1]);
                    if (openInFw) {
                        nextFreeformPackage = ModuleHelper.NOT_EXIST_SYMBOL
                    }
                    result = openInFw; throwable = null

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        ModuleHelper.hookAllMethods("com.android.server.wm.ActivityStarterImpl", lpparam.classLoader, "checkStartActivityByFreeForm", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    if (args[1] != null) {
                        val safeOptions = args[7]
                        if (safeOptions != null) {
                            val ao = XposedHelpers.getObjectField(safeOptions, "mOriginalOptions") as ActivityOptions?
                            if (ao != null && XposedHelpers.getIntField(ao, "mLaunchWindowingMode") == 5) {
                                freeformCallingPackage = "SkipCheck"
                                return XposedHelpers.proceedOrThrow(chain, args, throwable)
                            }
                        }
                        freeformCallingPackage = args[6] as String
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
    fun MultiWindowPlusHook(lpparam: SystemServerStartingParam) {
        MainModule.resHooks.setThemeValueReplacement("android", "string-array", "miui_resize_black_list", ResourceConstants.module_resize_black_list)
        val AtmClass = XposedHelpers.findClassIfExists("com.android.server.wm.ActivityTaskManagerServiceImpl", lpparam.classLoader)
        if (AtmClass != null) {
            ModuleHelper.findAndHookMethod(AtmClass, "updateResizeBlackList", Context::class.java, HookerClassHelper.DO_NOTHING)
            ModuleHelper.findAndHookMethod(AtmClass, "getSplitScreenBlackListFromXml", HookerClassHelper.DO_NOTHING)
            ModuleHelper.hookAllMethods(AtmClass, "inResizeBlackList", HookerClassHelper.returnConstant(false))
        }
    }

    @JvmStatic
    fun MultiWindowPlusHook(lpparam: PackageReadyParam) {
        if (lpparam.packageName == "com.miui.home") {
            ModuleHelper.findAndHookMethodSilently("com.android.systemui.shared.recents.model.Task", lpparam.classLoader, "isSupportSplit", HookerClassHelper.returnConstant(true))
            ModuleHelper.hookAllMethods("com.miui.home.recents.views.RecentMenuView", lpparam.classLoader, "onMessageEvent", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any?
                    var throwable: Throwable? = null
                    try {
                        result = chain.proceed()
                    } catch (t: Throwable) {
                        throwable = t
                        result = null
                    }
                    try {
                        val thisObject = chain.thisObject

                        val mHandler = XposedHelpers.getObjectField(thisObject, "mHandler") as Handler
                        val oldMultiWindowEnableRunnable = XposedHelpers.getAdditionalInstanceField(thisObject, "multiWindowEnableRunnable") as Runnable?
                        if (oldMultiWindowEnableRunnable != null) mHandler.removeCallbacks(oldMultiWindowEnableRunnable)
                        val multiWindowEnableRunnable = Runnable {
                            val mMenuItemMultiWindow = XposedHelpers.getObjectField(thisObject, "mMenuItemMultiWindow") as ImageView
                            val mMenuItemSmallWindow = XposedHelpers.getObjectField(thisObject, "mMenuItemSmallWindow") as ImageView
                            mMenuItemMultiWindow.isEnabled = true
                            mMenuItemMultiWindow.imageAlpha = 255
                            mMenuItemSmallWindow.isEnabled = true
                            mMenuItemSmallWindow.imageAlpha = 255
                        }
                        XposedHelpers.setAdditionalInstanceField(thisObject, "multiWindowEnableRunnable", multiWindowEnableRunnable)
                        mHandler.postDelayed(multiWindowEnableRunnable, 200)

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
        }
    }

    @JvmStatic
    fun MinimalNotificationViewHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBar", lpparam.classLoader, "updateNotification", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    if (args.size != 3) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val expandableRow = XposedHelpers.getObjectField(args[0], "row")
                    val mNotificationData = XposedHelpers.getObjectField(thisObject, "mNotificationData")
                    val newLowPriority = XposedHelpers.callMethod(mNotificationData, "isAmbient", XposedHelpers.callMethod(args[1], "getKey")) as Boolean && !(XposedHelpers.callMethod(XposedHelpers.callMethod(args[1], "getNotification"), "isGroupSummary") as Boolean)
                    val hasEntry = XposedHelpers.callMethod(mNotificationData, "get", XposedHelpers.getObjectField(args[0], "key")) != null
                    val isLowPriority = XposedHelpers.callMethod(expandableRow, "isLowPriority") as Boolean
                    XposedHelpers.callMethod(expandableRow, "setIsLowPriority", newLowPriority)
                    val hasLowPriorityChanged = hasEntry && isLowPriority != newLowPriority
                    XposedHelpers.callMethod(expandableRow, "setLowPriorityStateUpdated", hasLowPriorityChanged)
                    XposedHelpers.callMethod(expandableRow, "updateNotification", args[0])

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun NotificationChannelSettingsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.row.MiuiNotificationMenuRow", lpparam.classLoader, "createMenuViews", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val entry = XposedHelpers.callMethod(XposedHelpers.getObjectField(thisObject, "mParent"), "getEntry")
                    val channelId = XposedHelpers.callMethod(XposedHelpers.callMethod(entry, "getChannel"), "getId") as String
                    if ("miscellaneous" == channelId) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val notification = XposedHelpers.getObjectField(entry, "mSbn")
                    val nuCls = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.notification.NotificationUtil", lpparam.classLoader)
                    val isHybrid = if (nuCls != null) XposedHelpers.callStaticMethod(nuCls, "isHybrid", notification) as Boolean else false
                    if (isHybrid) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val mInfoItem = XposedHelpers.getObjectField(thisObject, "mInfoItem")
                    val mIcon = XposedHelpers.getObjectField(mInfoItem, "mIcon") as ImageView
                    mIcon.setOnClickListener(View.OnClickListener {
                        try {
                            val bundle = Bundle()
                            bundle.putString("android.provider.extra.CHANNEL_ID", channelId)
                            val pkgName = XposedHelpers.callMethod(notification, "getPackageName") as String
                            bundle.putString("package", pkgName)
                            val appUid = XposedHelpers.getIntField(notification, "mAppUid")
                            bundle.putInt("uid", appUid)
                            bundle.putString("miui.targetPkg", pkgName)
                            val intent = Intent("android.intent.action.MAIN")
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra(":android:show_fragment", "com.android.settings.notification.ChannelNotificationSettings")
                            intent.putExtra(":android:show_fragment_args", bundle)
                            intent.setClassName("com.android.settings", "com.android.settings.SubSettings")
                            XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.getStaticObjectField(UserHandle::class.java, "CURRENT"))
                            val modalController = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.notification.modal.ModalController")
                            XposedHelpers.callMethod(modalController, "animExitModal", 50L, true, "MORE", false)
                            val statusBar = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.CommandQueue")
                            XposedHelpers.callMethod(statusBar, "animateCollapsePanels", 0, false)
                        } catch (ignore: Throwable) {
                            XposedHelpers.log(ignore)
                        }
                    })

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun SkipAppLockHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.miui.server.AccessController", lpparam.classLoader, "skipActivity", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    val intent = args[0] as Intent?
                    if (intent == null || intent.component == null) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val pkgName = intent.component!!.packageName
                    val actName = intent.component!!.className
                    val key = "system_applock_skip_activities"
                    val itemStr = MainModule.mPrefs.getString(key, "")
                    if (itemStr == null || itemStr.isEmpty()) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val itemArr = itemStr.trim().split("\\|".toRegex())
                    for (uuid in itemArr) {
                        val pkgAct = MainModule.mPrefs.getString(key + "_" + uuid + "_activity", "")
                        if (pkgAct == pkgName + "|" + actName) { result = true; throwable = null }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideLockScreenHintHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.KeyguardIndicationController", lpparam.classLoader, "updateDeviceEntryIndication", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    XposedHelpers.setObjectField(thisObject, "mPersistentUnlockMessage", "")

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
    fun HideLockScreenStatusBarHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mKeyguardStatusBar = thisObject as View
                    mKeyguardStatusBar.visibility = View.GONE
                    mKeyguardStatusBar.translationY = -499f

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun MuteVisibleNotificationsHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.policy.MiuiAlertManager", lpparam.classLoader, "buzzBeepBlink", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val powerMgr = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (powerMgr.isInteractive) {
                        skipped = true; result = null; throwable = null
                    }

                    if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
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
    fun NetworkIndicatorWifi(lpparam: PackageReadyParam) {
        val hideWifiActivity = object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mWifiActivityView = XposedHelpers.getObjectField(thisObject, "mWifiActivityView")
                    XposedHelpers.callMethod(mWifiActivityView, "setVisibility", View.INVISIBLE)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.classLoader, "applyWifiState", hideWifiActivity)
    }

    @JvmStatic
    fun SetLockscreenWallpaperHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllMethods("com.android.server.wallpaper.WallpaperManagerService", lpparam.classLoader, "setWallpaper", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject
                    val args = XposedHelpers.getArgsArray(chain)

                    if (throwable != null || result == null || args[5] as Int == 1 || "com.android.thememanager" == args[1]) { return XposedHelpers.throwOrReturn(throwable, result) }

                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    if (mContext == null) { return XposedHelpers.throwOrReturn(throwable, result) }

                    var handleIncomingUser = 0
                    try {
                        handleIncomingUser = XposedHelpers.callStaticMethod(ActivityManager::class.java, "handleIncomingUser", Binder.getCallingPid(), Binder.getCallingUid(), args[7], false, true, "changing wallpaper", null) as Int
                    } catch (ignore: Throwable) {}
                    val wallpaperData = XposedHelpers.callMethod(thisObject, "getWallpaperSafeLocked", handleIncomingUser, args[5])
                    val wallpaper = XposedHelpers.getObjectField(wallpaperData, "wallpaperFile") as File

                    Handler(mContext.mainLooper).postDelayed({
                        try {
                            if (!wallpaper.exists()) return@postDelayed

                            val lockWallpaperPath = "/data/system/theme/thirdparty_lock_wallpaper"
                            Helpers.copyFile(wallpaper.absolutePath, lockWallpaperPath)
                            val ThemeUtils = XposedHelpers.findClass("miui.content.res.ThemeNativeUtils", lpparam.classLoader)
                            XposedHelpers.callStaticMethod(ThemeUtils, "updateFilePermissionWithThemeContext", lockWallpaperPath)
                            val data = JSONObject()
                            val ex = JSONObject()
                            try {
                                val lockWallpaper = File(lockWallpaperPath)
                                ex
                                    .put("link_type", "0")
                                    .put("title_size", "26")
                                    .put("item_id", "wallpaper1")
                                    .put("title_color", "#ffffffff")
                                    .put("index_in_album", "1")
                                    .put("tag_list", "CustoMIUIzer,mod")
                                    .put("content_color", "#ffffffff")
                                    .put("total_of_album", "1")
                                    .put("img_level", "0")
                                    .put("album_id", "1")
                                    .put("title_customized", "0")
                                    .put("lks_entry_text", "Some wallpaper")

                                data
                                    .put("authority", "tv.withaibuild.customiuizer.mods.set_lockscreen_wallpaper")
                                    .put("content", "Wallpaper set by some app")
                                    .put("contentColorValue", 0)
                                    .put("cp", "CustoMIUIzer")
                                    .put("cpColorValue", 0)
                                    .put("definition", -1)
                                    .put("ex", ex.toString())
                                    .put("fromColorValue", 0)
                                    .put("hasAcc", false)
                                    .put("indexInAlbum", -1)
                                    .put("isAd", false)
                                    .put("isCustom", false)
                                    .put("isFd", false)
                                    .put("isFrontCover", false)
                                    .put("key", "wallpaper1")
                                    .put("like", false)
                                    .put("linkType", 0)
                                    .put("noApply", false)
                                    .put("noDislike", false)
                                    .put("noSave", false)
                                    .put("noShare", false)
                                    .put("pos", 0)
                                    .put("supportLike", true)
                                    .put("title", "Some wallpaper")
                                    .put("titleColorValue", 0)
                                    .put("titleTextSize", -1)
                                    .put("totalOfAlbum", -1)
                                    .put("wallpaperUri", lockWallpaper.toURI())
                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }

                            val setIntent = Intent("com.miui.miwallpaper.UPDATE_LOCKSCREEN_WALLPAPER")
                            setIntent.putExtra("wallpaperInfo", data.toString())
                            setIntent.putExtra("apply", true)
                            mContext.sendBroadcast(setIntent)
                        } catch (t: Throwable) {
                            XposedHelpers.log(t)
                        }
                    }, 1800)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun BetterPopupsCenteredHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.HeadsUpManagerInjector", lpparam.classLoader, "miuiHeadsUpInset", Context::class.java, object : MethodHook() {
            private var mHeadsUpPaddingTop = 0
            private var mHeadsUpHeight = 0
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val args = XposedHelpers.getArgsArray(chain)

                    val context = args[0] as Context
                    val resources = context.resources
                    if (mHeadsUpPaddingTop == 0) {
                        val dimId = Helpers.getResId(resources, "heads_up_status_bar_padding", "dimen", "com.android.systemui")
                        mHeadsUpPaddingTop = resources.getDimensionPixelSize(dimId)
                        mHeadsUpHeight = resources.getDimensionPixelSize(Helpers.getResId(resources, "notification_max_heads_up_height", "dimen", "com.android.systemui"))
                    }
                    if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        val mHeadsUpInset = result as Int
                        val mStatusBarHeight = mHeadsUpInset - mHeadsUpPaddingTop
                        val topMargin = (context.resources.displayMetrics.heightPixels + mStatusBarHeight - mHeadsUpHeight) / 2
                        result = topMargin; throwable = null
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun WallpaperScaleLevelHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.hookAllConstructors("com.android.server.wm.WallpaperController", lpparam.classLoader, object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val scale = MainModule.mPrefs.getInt("system_other_wallpaper_scale", 6) / 10.0f
                    XposedHelpers.setObjectField(thisObject, "mMaxWallpaperScale", scale)
                    ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                        override fun onChange(key: String?) {
                            if (key?.contains("system_other_wallpaper_scale") == true) {
                                val value = MainModule.mPrefs.getInt("system_other_wallpaper_scale", 6)
                                XposedHelpers.setObjectField(thisObject, "mMaxWallpaperScale", value / 10.0f)
                            }
                        }
                    }, thisObject)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun Disable72hStrongAuthHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.locksettings.LockSettingsStrongAuth", lpparam.classLoader, "rescheduleStrongAuthTimeoutAlarm", Long::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, HookerClassHelper.DO_NOTHING)
    }

    @JvmStatic
    fun AllowUntrustedTouchHook(lpparam: SystemServerStartingParam) {
        ModuleHelper.findAndHookMethod("com.android.server.wm.WindowState", lpparam.classLoader, "getTouchOcclusionMode", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any?
                var throwable: Throwable? = null
                try {
                    result = chain.proceed()
                } catch (t: Throwable) {
                    throwable = t
                    result = null
                }
                try {
                    val thisObject = chain.thisObject

                    val mode = result as Int
                    if (mode == 1) { result = 2; throwable = null }
                    else {
                        val mAttrs = XposedHelpers.getObjectField(thisObject, "mAttrs") as WindowManager.LayoutParams
                        if (mAttrs.type == WindowManager.LayoutParams.TYPE_TOAST) {
                            result = 2; throwable = null
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }
}
