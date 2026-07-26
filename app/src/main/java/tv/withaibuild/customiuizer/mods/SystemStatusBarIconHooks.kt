package tv.withaibuild.customiuizer.mods

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.ResourceHooks
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.Helpers

object SystemStatusBarIconHooks {

    @JvmStatic
    fun HideIconsBattery1Hook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.classLoader, "updateAll", object : MethodHook() {
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

                    val mBatteryIconView = XposedHelpers.getObjectField(thisObject, "mBatteryIconView") as ImageView
                    mBatteryIconView.visibility = View.GONE

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun HideIconsBattery2Hook(lpparam: PackageReadyParam) {
        val hideNormalPercentage = MainModule.mPrefs.getBoolean("system_statusbaricons_battery2")
        val batteryId = ResourceHooks.getFakeResId("batterview_in_statusbar")
        if (hideNormalPercentage) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
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

                        val mBatteryView = XposedHelpers.getObjectField(thisObject, "mBattery") as View
                        mBatteryView.setTag(batteryId, true)

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.KeyguardStatusBarView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
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

                        val mSystemIconsContainer = XposedHelpers.getObjectField(thisObject, "mSystemIconsContainer") as ViewGroup
                        val batteryResId = Helpers.getResId(mSystemIconsContainer.resources, "battery", "id", "com.android.systemui")
                        val mBatteryView = mSystemIconsContainer.findViewById<View>(batteryResId)
                        mBatteryView.setTag(batteryId, true)

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.classLoader, "updateChargeAndText", object : MethodHook() {
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

                    if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery4")) {
                        val mBatteryPercentMarkView = XposedHelpers.getObjectField(thisObject, "mBatteryPercentMarkView") as TextView?
                        mBatteryPercentMarkView?.visibility = View.GONE
                    }
                    if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery3")) {
                        val mBatteryChargingView = XposedHelpers.getObjectField(thisObject, "mBatteryChargingView") as ImageView?
                        mBatteryChargingView?.visibility = View.GONE
                        try {
                            val mBatteryChargingInView = XposedHelpers.getObjectField(thisObject, "mBatteryChargingInView") as ImageView?
                            mBatteryChargingInView?.visibility = View.GONE
                        } catch (ignore: Throwable) {}
                    }
                    if (hideNormalPercentage) {
                        val mBatteryView = thisObject as View
                        if (mBatteryView.getTag(batteryId) != null) {
                            var percentView = XposedHelpers.getObjectField(thisObject, "mBatteryPercentMarkView") as View?
                            percentView?.visibility = View.GONE
                            percentView = XposedHelpers.getObjectField(thisObject, "mBatteryPercentView") as View?
                            percentView?.visibility = View.GONE
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private var lastState = false
    private var mNextAlarmTime = 0L

    private fun updateAlarmVisibility(thisObject: Any) {
        try {
            val mIconController = XposedHelpers.getObjectField(thisObject, "mIconController")
            if (!lastState) {
                XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", false)
                return
            }

            val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
            val nowTime = java.lang.System.currentTimeMillis()
            var nextTime = mNextAlarmTime
            if (nextTime == 0L) {
                nextTime = ModuleHelper.getNextMIUIAlarmTime(mContext)
            }
            if (nextTime == 0L) nextTime = Helpers.getNextStockAlarmTime(mContext)

            var diffMSec = nextTime - nowTime
            if (diffMSec < 0) diffMSec += 7 * 24 * 60 * 60 * 1000
            val diffHours = (diffMSec - 59 * 1000) / (1000f * 60f * 60f)
            val vis = diffHours <= MainModule.mPrefs.getInt("system_statusbaricons_alarmn", 0)
            XposedHelpers.callMethod(mIconController, "setIconVisibility", "alarm_clock", vis)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    @JvmStatic
    fun HideIconsSelectiveAlarmHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.classLoader, object : MethodHook() {
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


                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val filter = IntentFilter()
                    filter.addAction("android.intent.action.TIME_TICK")
                    filter.addAction("android.intent.action.TIME_SET")
                    filter.addAction("android.intent.action.TIMEZONE_CHANGED")
                    filter.addAction("android.intent.action.LOCALE_CHANGED")
                    val oldalarmTimeReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "alarmTimeReceiver")
                    if (oldalarmTimeReceiver is BroadcastReceiver) {
                        try { mContext.unregisterReceiver(oldalarmTimeReceiver) } catch (ignore: Throwable) {}
                    }
                    val alarmTimeReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            updateAlarmVisibility(thisObject)
                        }
                    }
                    XposedHelpers.setAdditionalInstanceField(thisObject, "alarmTimeReceiver", alarmTimeReceiver)
                    mContext.registerReceiver(alarmTimeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

                    val mNextAlarmCallback = XposedHelpers.getObjectField(thisObject, "mNextAlarmCallback")
                    ModuleHelper.findAndHookMethod(mNextAlarmCallback.javaClass, "onAlarmChanged", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            var skipped = false
                            var result: Any? = null
                            var throwable: Throwable? = null
                            val args2 = XposedHelpers.getArgsArray(chain)
                            val thisObject2 = chain.thisObject
                            try {

                                lastState = args2[0] as Boolean
                                mNextAlarmTime = ModuleHelper.getNextMIUIAlarmTime(mContext)
                                updateAlarmVisibility(thisObject2)
                                skipped = true; result = null; throwable = null

                                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                                result = chain.proceed(args2)
                            } catch (t: Throwable) {
                                throwable = t
                                result = null
                            }
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                    })
                    ModuleHelper.findAndHookMethod(mNextAlarmCallback.javaClass, "onNextAlarmChanged", AlarmManager.AlarmClockInfo::class.java, object : MethodHook() {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            var skipped = false
                            var result: Any? = null
                            var throwable: Throwable? = null
                            val args2 = XposedHelpers.getArgsArray(chain)
                            val thisObject2 = chain.thisObject
                            try {

                                if (args2[0] == null) {
                                    lastState = false
                                }
                                mNextAlarmTime = ModuleHelper.getNextMIUIAlarmTime(mContext)
                                updateAlarmVisibility(thisObject2)
                                skipped = true; result = null; throwable = null

                                if (skipped) { return XposedHelpers.throwOrReturn(throwable, result) }
                                result = chain.proceed(args2)
                            } catch (t: Throwable) {
                                throwable = t
                                result = null
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
    }

    @JvmStatic
    fun DisplayWifiStandardHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarWifiView", lpparam.classLoader, "applyWifiState", object : MethodHook() {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var result: Any? = null
                var throwable: Throwable? = null
                val args = XposedHelpers.getArgsArray(chain)
                try {

                    val wifiState = args[0]
                    if (wifiState != null) {
                        val opt = MainModule.mPrefs.getStringAsInt("system_statusbaricons_wifistandard", 1)
                        if (opt == 1) { return XposedHelpers.proceedOrThrow(chain, args, throwable) }
                        val wifiStandard = XposedHelpers.getObjectField(wifiState, "wifiStandard") as Int
                        XposedHelpers.setObjectField(wifiState, "showWifiStandard", opt == 2 && wifiStandard > 0)
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
}
