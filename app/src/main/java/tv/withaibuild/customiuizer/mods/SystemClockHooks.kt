package tv.withaibuild.customiuizer.mods

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.WeatherDataController
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.Helpers
import java.util.ArrayList

object SystemClockHooks {

    private fun initClockStyle(mClock: TextView, clockName: String) {
        val res = mClock.resources
        val subKey = if (clockName == "clock") "statusbar" else "cc"
        val statusBarClock = clockName == "clock"
        val enableCustomFormat = !statusBarClock || MainModule.mPrefs.getBoolean("system_${subKey}_clock_customformat_enable")
        val customFormat = MainModule.mPrefs.getString("system_${subKey}_clock_customformat", "")
        val dualRows = enableCustomFormat && customFormat.contains("\n")
        if (statusBarClock) {
            val dimStep = 0.5f
            var fontSize = MainModule.mPrefs.getInt("system_statusbar_clock_fontsize", 13)
            if (fontSize > 13) {
                mClock.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * dimStep)
            }
            if (dualRows) {
                val multiplier = if (0.5f * fontSize > 8.5f) 0.85f else 0.9f
                mClock.setLineSpacing(0f, multiplier)
            }
            when (MainModule.mPrefs.getStringAsInt("system_statusbar_clock_align", 1)) {
                2 -> mClock.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                3 -> mClock.textAlignment = View.TEXT_ALIGNMENT_CENTER
                4 -> mClock.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            }
            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_bold")) {
                mClock.typeface = Typeface.DEFAULT_BOLD
            }
            var leftMargin = MainModule.mPrefs.getInt("system_statusbar_clock_leftmargin", 0)
            leftMargin = Helpers.dp2px(leftMargin * dimStep).toInt()
            var rightMargin = MainModule.mPrefs.getInt("system_statusbar_clock_rightmargin", 0)
            rightMargin = Helpers.dp2px(rightMargin * dimStep).toInt()
            val defaultVerticalOffset = 8
            val verticalOffset = MainModule.mPrefs.getInt("system_statusbar_clock_verticaloffset", defaultVerticalOffset)
            if (verticalOffset != defaultVerticalOffset) {
                val marginTop = Helpers.dp2px((verticalOffset - defaultVerticalOffset) * dimStep)
                mClock.translationY = marginTop
            }

            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_chip")) {
                val lp = mClock.layoutParams as LinearLayout.LayoutParams
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lp.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                if (leftMargin > 0) lp.leftMargin = leftMargin
                if (rightMargin > 0) lp.rightMargin = rightMargin
                mClock.layoutParams = lp

                val useMonet = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_usemonet")
                val customTextColor = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_customtextcolor")

                var startColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_startcolor", 0x8F7C4DFF.toInt())
                var endColor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_endcolor", 0x2FA7FFEB.toInt())
                if (useMonet) {
                    mClock.setTextColor(mClock.resources.getColor(android.R.color.system_accent1_0, null))
                    startColor = mClock.resources.getColor(android.R.color.system_accent1_600, null)
                    endColor = startColor
                } else if (customTextColor) {
                    val textcolor = MainModule.mPrefs.getInt("system_statusbar_clock_chip_textcolor", 0xFFFFFFFF.toInt())
                    mClock.setTextColor(textcolor)
                }
                val chipDrawable = GradientDrawable()
                val verticalOrientation = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_orientation_vertical")
                chipDrawable.orientation = if (verticalOrientation) GradientDrawable.Orientation.TOP_BOTTOM else GradientDrawable.Orientation.LEFT_RIGHT
                chipDrawable.colors = intArrayOf(startColor, endColor)
                chipDrawable.shape = GradientDrawable.RECTANGLE
                var horizPadding = MainModule.mPrefs.getInt("system_statusbar_clock_chip_horizpadding", 0)
                var vertPadding = MainModule.mPrefs.getInt("system_statusbar_clock_chip_verticalpadding", 0)
                if (horizPadding > 0) {
                    horizPadding = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        horizPadding.toFloat(),
                        res.displayMetrics
                    ).toInt()
                }
                if (vertPadding > 0 || horizPadding > 0) {
                    chipDrawable.setPadding(horizPadding, vertPadding, horizPadding, vertPadding)
                }
                var radiusPx = MainModule.mPrefs.getInt("system_statusbar_clock_chip_radius", 0)
                if (radiusPx > 0) {
                    radiusPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        radiusPx.toFloat(),
                        res.displayMetrics
                    ).toInt()
                    chipDrawable.cornerRadius = radiusPx.toFloat()
                }
                mClock.background = chipDrawable
            } else {
                if (leftMargin > 0 || rightMargin > 0) {
                    val lp = mClock.layoutParams as LinearLayout.LayoutParams
                    lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    lp.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                    if (leftMargin > 0) lp.leftMargin = leftMargin
                    if (rightMargin > 0) lp.rightMargin = rightMargin
                    mClock.layoutParams = lp
                }
            }
            val fixedWidth = MainModule.mPrefs.getInt("system_statusbar_clock_fixedcontent_width", 10)
            if (fixedWidth > 10) {
                val lp = mClock.layoutParams
                lp.width = (mClock.resources.displayMetrics.density * fixedWidth).toInt()
                mClock.layoutParams = lp
            }
        }
        if (dualRows) {
            mClock.setSingleLine(false)
            mClock.maxLines = 2
        }
    }

    private fun getShowSeconds(): Boolean {
        val sbShowSeconds = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_seconds")
        val customFormat = MainModule.mPrefs.getString("system_statusbar_clock_customformat", "")
        val enableCustomFormat = MainModule.mPrefs.getBoolean("system_statusbar_clock_customformat_enable")
        return (enableCustomFormat && customFormat.contains("ss")) || (!enableCustomFormat && sbShowSeconds)
    }

    private fun getCCShowSeconds(): Boolean {
        val customFormat = MainModule.mPrefs.getString("system_cc_clock_customformat", "")
        return customFormat.contains("ss")
    }

    private class SecondTicker(private val clockController: Any, private val context: Context) : Runnable {
        private val handler = Handler(context.mainLooper)
        private var running = false

        fun start() {
            running = true
            scheduleNextTick()
        }

        fun stop() {
            running = false
            handler.removeCallbacks(this)
        }

        override fun run() {
            if (!running) return
            try {
                val calendar = XposedHelpers.getObjectField(clockController, "mCalendar")
                XposedHelpers.callMethod(calendar, "setTimeInMillis", java.lang.System.currentTimeMillis())
                XposedHelpers.setObjectField(clockController, "mIs24", DateFormat.is24HourFormat(context))
                val clockListeners = XposedHelpers.getObjectField(clockController, "mClockListeners") as ArrayList<Any>
                for (listener in clockListeners) {
                    val clock = listener as View
                    if (ModuleHelper.getViewInfo(clock, "showSeconds") != null) {
                        XposedHelpers.callMethod(clock, "updateTime")
                    }
                }
            } catch (t: Throwable) {
                XposedHelpers.log("SecondTicker", t)
            }
            scheduleNextTick()
        }

        private fun scheduleNextTick() {
            if (!running) return
            val delay = 1000L - java.lang.System.currentTimeMillis() % 1000L
            handler.postDelayed(this, delay)
        }
    }

    private fun initSecondTicker(clockController: Any) {
        val ccShowSeconds = getCCShowSeconds()
        val finalSbShowSeconds = getShowSeconds()
        val mContext = XposedHelpers.getObjectField(clockController, "mContext") as Context
        val previousTicker = XposedHelpers.getAdditionalInstanceField(clockController, "secondTicker") as SecondTicker?
        if (previousTicker != null) {
            previousTicker.stop()
            XposedHelpers.removeAdditionalInstanceField(clockController, "secondTicker")
        }
        if (ccShowSeconds || finalSbShowSeconds) {
            val ticker = SecondTicker(clockController, mContext)
            XposedHelpers.setAdditionalInstanceField(clockController, "secondTicker", ticker)
            ticker.start()
        }
    }

    private fun initWeatherInfoHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.classLoader, object : MethodHook() {
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
                    val mWeatherRunnable = Runnable { XposedHelpers.callMethod(thisObject, "updateTime") }
                    WeatherDataController.initContext(mContext, mWeatherRunnable)

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun StatusBarClockTweakHook(lpparam: PackageReadyParam) {
        val enableWeatherParam = MainModule.mPrefs.getBoolean("system_statusbar_enable_weather_param")
        if (enableWeatherParam) {
            initWeatherInfoHook(lpparam)
        }
        val hideStatusbarClock = MainModule.mPrefs.getBoolean("system_statusbaricons_clock")
        val statusbarClockTweak = !hideStatusbarClock && MainModule.mPrefs.getBoolean("system_statusbar_clocktweak")
        val ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak")
        val scheduleHook = object : MethodHook() {
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

                    initSecondTicker(thisObject)
                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    if (getShowSeconds() || getCCShowSeconds()) {
                        val oldReceiver = XposedHelpers.getAdditionalInstanceField(thisObject, "customiuizer_timeSetReceiver") as BroadcastReceiver?
                        if (oldReceiver != null) try { mContext.unregisterReceiver(oldReceiver) } catch (ignore: Throwable) {}
                        val mUpdateTimeReceiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                initSecondTicker(thisObject)
                            }
                        }
                        XposedHelpers.setAdditionalInstanceField(thisObject, "customiuizer_timeSetReceiver", mUpdateTimeReceiver)
                        val timeSetIntent = IntentFilter()
                        timeSetIntent.addAction("android.intent.action.TIME_SET")
                        mContext.registerReceiver(mUpdateTimeReceiver, timeSetIntent, Context.RECEIVER_NOT_EXPORTED)
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        if (ccClockTweak || statusbarClockTweak) {
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.MiuiStatusBarClockController", lpparam.classLoader, scheduleHook)
        }
        val hideDateView = MainModule.mPrefs.getBoolean("system_cc_hidedate")
        val hideDrawerDate = MainModule.mPrefs.getBoolean("system_drawer_hidedate")
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, object : MethodHook() {
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

                    val clock = thisObject as TextView
                    if (args.size != 3) { return XposedHelpers.throwOrReturn(throwable, result) }
                    val clockId = Helpers.getResId(clock.resources, "clock", "id", "com.android.systemui")
                    val bigClockId = Helpers.getResId(clock.resources, "big_time", "id", "com.android.systemui")
                    val dateClockId = Helpers.getResId(clock.resources, "date_time", "id", "com.android.systemui")
                    val horizDateClockId = Helpers.getResId(clock.resources, "horizontal_date_time", "id", "com.android.systemui")
                    val thisClockId = clock.id
                    if (clockId == thisClockId) {
                        ModuleHelper.setViewInfo(clock, "clockName", "clock")
                        if (statusbarClockTweak && getShowSeconds()) {
                            ModuleHelper.setViewInfo(clock, "showSeconds", true)
                        }
                    } else if (bigClockId == thisClockId) {
                        ModuleHelper.setViewInfo(clock, "clockName", "ccClock")
                        if (ccClockTweak) {
                            if (getCCShowSeconds()) {
                                ModuleHelper.setViewInfo(clock, "showSeconds", true)
                            }
                            initClockStyle(clock, "ccClock")
                        }
                    } else if (thisClockId == horizDateClockId) {
                        ModuleHelper.setViewInfo(clock, "clockName", "drawerDate")
                    } else if (dateClockId == thisClockId) {
                        val ccDate = clock.javaClass.canonicalName?.contains("ControlCenterDateView") ?: false
                        if (ccDate) {
                            ModuleHelper.setViewInfo(clock, "clockName", "ccDate")
                        }
                        if (!ccDate) {
                            ModuleHelper.setViewInfo(clock, "clockName", "drawerDate")
                        }
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })

        val clockFormatBuilder = object : ThreadLocal<StringBuilder>() {
            override fun initialValue(): StringBuilder {
                return StringBuilder(32)
            }
        }
        val clockTextBuilder = object : ThreadLocal<StringBuilder>() {
            override fun initialValue(): StringBuilder {
                return StringBuilder(32)
            }
        }
        val updateTimeHook = object : MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            override fun intercept(chain: XposedInterface.Chain): Any? {
                var skipped = false
                var result: Any? = null
                var throwable: Throwable? = null
                val thisObject = chain.thisObject
                try {

                    val clock = thisObject as TextView
                    val clockName = ModuleHelper.getViewInfo(clock, "clockName") as String?
                    val mContext = clock.context
                    if (("ccDate" == clockName && hideDateView)
                        || ("drawerDate" == clockName && hideDrawerDate)
                        || ("clock" == clockName && hideStatusbarClock)
                    ) {
                        clock.text = ""
                        return XposedHelpers.throwOrReturn(throwable, result)
                    }

                    val mMiuiStatusBarClockController = XposedHelpers.getObjectField(clock, "mMiuiStatusBarClockController")
                    val mCalendar = XposedHelpers.getObjectField(mMiuiStatusBarClockController, "mCalendar")
                    var timeFmt: String? = null
                    if ("ccClock" == clockName) {
                        if (ccClockTweak) {
                            val customFormat = MainModule.mPrefs.getString("system_cc_clock_customformat", "")
                            if (customFormat.isNotEmpty()) {
                                timeFmt = customFormat
                            }
                        }
                    } else if ("ccDate" == clockName) {
                        val ccDateFormat = MainModule.mPrefs.getString("system_cc_dateformat", "")
                        if (ccDateFormat.isNotEmpty()) {
                            timeFmt = ccDateFormat
                        }
                    } else if ("drawerDate" == clockName) {
                        val drawerDateFormat = MainModule.mPrefs.getString("system_drawer_dateformat", "")
                        if (drawerDateFormat.isNotEmpty()) {
                            timeFmt = drawerDateFormat
                        }
                    } else if ("clock" == clockName && statusbarClockTweak) {
                        val customFormat = MainModule.mPrefs.getString("system_statusbar_clock_customformat", "")
                        val enableCustomFormat = MainModule.mPrefs.getBoolean("system_statusbar_clock_customformat_enable") && customFormat.isNotEmpty()
                        if (enableCustomFormat) {
                            timeFmt = customFormat
                        } else {
                            val showSeconds = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_seconds")
                            val is24 = MainModule.mPrefs.getBoolean("system_statusbar_clock_24hour_format")
                            val showAmpm = MainModule.mPrefs.getBoolean("system_statusbar_clock_show_ampm")
                            val hourIn2d = MainModule.mPrefs.getBoolean("system_statusbar_clock_leadingzero")
                            val fmt = if (showAmpm) "fmt_time_12hour_minute_pm" else "fmt_time_12hour_minute"
                            val fmtResId = Helpers.getResId(mContext.resources, fmt, "string", "com.android.systemui")
                            timeFmt = mContext.getString(fmtResId)
                            if (showSeconds) {
                                val mmIdx = timeFmt.indexOf(":mm")
                                if (mmIdx >= 0) {
                                    timeFmt = timeFmt.substring(0, mmIdx) + ":mm:ss" + timeFmt.substring(mmIdx + 3)
                                }
                            }
                            var hourStr = "h"
                            if (is24) hourStr = "H"
                            if (hourIn2d) hourStr += hourStr
                            val colonIdx = timeFmt.indexOf(':')
                            if (colonIdx > 0) {
                                timeFmt = hourStr + timeFmt.substring(colonIdx)
                            }
                        }
                    }
                    if (timeFmt != null) {
                        if (enableWeatherParam) {
                            val weatherInfo = WeatherDataController.weatherInfo
                            if (weatherInfo != null) timeFmt = timeFmt.replace("tq", weatherInfo)
                        }
                        val formatSb = clockFormatBuilder.get()
                        formatSb.setLength(0)
                        formatSb.append(timeFmt)
                        val textSb = clockTextBuilder.get()
                        textSb.setLength(0)
                        XposedHelpers.callMethod(mCalendar, "format", mContext, textSb, formatSb)
                        clock.text = textSb.toString()
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
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, "updateTime", updateTimeHook)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiStatusBarClock", lpparam.classLoader, "updateTime", updateTimeHook)
        if (hideDateView || hideDrawerDate || hideStatusbarClock) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, "onAttachedToWindow", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var result: Any? = null
                    var throwable: Throwable? = null
                    val thisObject = chain.thisObject
                    try {

                        val clock = thisObject as TextView
                        val clockName = ModuleHelper.getViewInfo(clock, "clockName") as String?
                        if (("ccDate" == clockName && hideDateView)
                            || ("drawerDate" == clockName && hideDrawerDate)
                            || ("clock" == clockName && hideStatusbarClock)
                        ) {
                            XposedHelpers.setObjectField(thisObject, "mAttached", true)
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
        if (statusbarClockTweak) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onAttachedToWindow", object : MethodHook() {
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

                        val clock = XposedHelpers.getObjectField(thisObject, "mClock") as TextView
                        initClockStyle(clock, "clock")

                    } catch (t: Throwable) {
                        XposedHelpers.log(t)
                    }
                    return XposedHelpers.throwOrReturn(throwable, result)
                }
            })
            val customTextColor = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_customtextcolor")
            val useMonet = MainModule.mPrefs.getBoolean("system_statusbar_clock_chip_usemonet")
            if (MainModule.mPrefs.getBoolean("system_statusbar_clock_chip") && (customTextColor || useMonet)) {
                ModuleHelper.hookAllMethods("com.android.systemui.statusbar.views.MiuiClock", lpparam.classLoader, "onDarkChanged", object : MethodHook() {
                    override fun intercept(chain: XposedInterface.Chain): Any? {
                        var skipped = false
                        var result: Any? = null
                        var throwable: Throwable? = null
                        val thisObject = chain.thisObject
                        try {

                            val clock = thisObject as TextView
                            val clockName = ModuleHelper.getViewInfo(clock, "clockName") as String?
                            if ("clock" == clockName) {
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
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.FakeStatusBarClockController", lpparam.classLoader, "initState", object : MethodHook() {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    var skipped = false
                    var result: Any? = null
                    var throwable: Throwable? = null
                    val thisObject = chain.thisObject
                    try {

                        val useLeft = XposedHelpers.getBooleanField(thisObject, "useLeft")
                        if (!useLeft) {
                            val mFakeClock = XposedHelpers.getObjectField(thisObject, "fakeStatusBarClock")
                            if (mFakeClock == null) { skipped = true; result = null; throwable = null }
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
    }

    @JvmStatic
    fun CCClockTweakHook(lpparam: PackageReadyParam) {
        val ccClockSize = MainModule.mPrefs.getInt("system_cc_clock_fontsize", 9)
        if (ccClockSize > 9) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "qs_control_header_clock_size", ccClockSize)
        }
        val ccClockHook = object : MethodHook() {
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

                    val clock = XposedHelpers.getObjectField(thisObject, "mBigTime") as TextView
                    val ccClockTweak = MainModule.mPrefs.getBoolean("system_cc_clocktweak")
                    val useSystemFonts = MainModule.mPrefs.getBoolean("system_qs_force_systemfonts")
                    if (ccClockTweak) {
                        val defaultVerticalOffset = 10
                        val verticalOffset = MainModule.mPrefs.getInt("system_cc_clock_verticaloffset", defaultVerticalOffset)
                        if (verticalOffset != defaultVerticalOffset) {
                            val marginTop = Helpers.dp2px((verticalOffset - defaultVerticalOffset).toFloat())
                            clock.translationY = marginTop
                        }
                    }
                    if (useSystemFonts) {
                        clock.typeface = Typeface.DEFAULT
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.classLoader, "updateResources", ccClockHook)
    }

    @JvmStatic
    fun CCClockCenterAlignHook(lpparam: PackageReadyParam) {
        val centerClock = MainModule.mPrefs.getBoolean("system_cc_clock_centeralign")
        val centerDate = !MainModule.mPrefs.getBoolean("system_drawer_hidedate") && MainModule.mPrefs.getBoolean("system_drawer_date_centeralign")
        val ccClockHook = object : MethodHook() {
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

                    val clock = XposedHelpers.getObjectField(thisObject, "mBigTime") as TextView
                    val mPolicyVisibility = XposedHelpers.getIntField(clock, "mPolicyVisibility")
                    val clockContainer = XposedHelpers.getObjectField(thisObject, "mNotificationHeaderClockContainer") as LinearLayout
                    if (mPolicyVisibility == 0 || mPolicyVisibility == 4) {
                        clockContainer.gravity = Gravity.CENTER_HORIZONTAL
                    } else {
                        clockContainer.gravity = Gravity.START
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        if (centerClock) {
            ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.classLoader, "updateLayout", ccClockHook)
        }
        val clockMarginHook = object : MethodHook() {
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

                    if (centerClock) {
                        val clock = XposedHelpers.getObjectField(thisObject, "mBigTime") as TextView
                        val lp = clock.layoutParams as LinearLayout.LayoutParams
                        lp.leftMargin = 0
                        clock.layoutParams = lp

                        val mWeatherCity = ModuleHelper.getObjectFieldSilently(thisObject, "mWeatherCity")
                        if (mWeatherCity != ModuleHelper.NOT_EXIST_SYMBOL) {
                            val weatherContainer = (mWeatherCity as View).parent as ViewGroup
                            weatherContainer.visibility = View.GONE
                        }
                    }
                    if (centerDate) {
                        val dateView = XposedHelpers.getObjectField(thisObject, "mDateView") as TextView
                        val dateContainer = dateView.parent as LinearLayout
                        dateContainer.gravity = Gravity.CENTER_HORIZONTAL
                    }

                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.classLoader, "onFinishInflate", clockMarginHook)
    }
}
