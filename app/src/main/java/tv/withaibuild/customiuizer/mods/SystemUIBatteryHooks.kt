package tv.withaibuild.customiuizer.mods

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.BatteryIndicator

@Suppress("MemberVisibilityCanBePrivate")
object SystemUIBatteryHooks {
    private const val StatusBarCls = "com.android.systemui.statusbar.phone.CentralSurfacesImpl"

    @JvmStatic
    fun BatteryIndicatorHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.classLoader, "start", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                val sbWindowController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarWindowController")
                val mStatusBarWindow = XposedHelpers.getObjectField(sbWindowController, "mStatusBarWindowView") as ViewGroup

                val indicator = BatteryIndicator(mContext)
                mStatusBarWindow.addView(indicator)
                indicator.setAdjustViewBounds(false)
                indicator.init(param.getThisObject())
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator", indicator)
                val mNotificationIconAreaController = XposedHelpers.getObjectField(param.getThisObject(), "mNotificationIconAreaController")
                XposedHelpers.setAdditionalInstanceField(mNotificationIconAreaController, "mBatteryIndicator", indicator)
                val mBatteryController = XposedHelpers.getObjectField(param.getThisObject(), "mBatteryController")
                XposedHelpers.setAdditionalInstanceField(mBatteryController, "mBatteryIndicator", indicator)
                XposedHelpers.callMethod(mBatteryController, "fireBatteryLevelChanged")
                XposedHelpers.callMethod(mBatteryController, "firePowerSaveChanged")
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "updatePanelExpanded", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mPanelExpanded = XposedHelpers.getBooleanField(param.getThisObject(), "mPanelExpanded")
                val isKeyguardShowing = XposedHelpers.callMethod(param.getThisObject(), "isKeyguardShowing") as Boolean
                val mStatusBar = XposedHelpers.getObjectField(param.getThisObject(), "mCentralSurfaces")
                val indicator = XposedHelpers.getAdditionalInstanceField(mStatusBar, "mBatteryIndicator") as BatteryIndicator?
                indicator?.onExpandingChanged(!isKeyguardShowing && mPanelExpanded)
            }
        })

        ModuleHelper.findAndHookMethod(StatusBarCls, lpparam.classLoader, "updateIsKeyguard", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val isKeyguardShowing = XposedHelpers.callMethod(param.getThisObject(), "isKeyguardShowing") as Boolean
                val indicator = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator") as BatteryIndicator?
                indicator?.onKeyguardStateChanged(isKeyguardShowing)
            }
        })

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "onDarkChanged", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val indicator = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator") as BatteryIndicator?
                indicator?.onDarkModeChanged(param.getArgs()[1] as Float, param.getArgs()[2] as Int)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl", lpparam.classLoader, "fireBatteryLevelChanged", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val indicator = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator") as BatteryIndicator?
                val mLevel = XposedHelpers.getIntField(param.getThisObject(), "mLevel")
                val mCharging = XposedHelpers.getBooleanField(param.getThisObject(), "mCharging")
                val mCharged = XposedHelpers.getBooleanField(param.getThisObject(), "mCharged")
                indicator?.onBatteryLevelChanged(mLevel, mCharging, mCharged)
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader, "firePowerSaveChanged", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val indicator = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mBatteryIndicator") as BatteryIndicator?
                indicator?.onPowerSaveChanged(XposedHelpers.getBooleanField(param.getThisObject(), "mPowerSave"))
            }
        })
    }

    @JvmStatic
    fun StatusBarStyleBatteryIconHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.MiuiBatteryMeterView", lpparam.classLoader, "updateAll", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val batteryView = param.getThisObject() as LinearLayout
                val mBatteryTextDigitView = XposedHelpers.getObjectField(param.getThisObject(), "mBatteryTextDigitView") as TextView
                val mBatteryPercentView = XposedHelpers.getObjectField(param.getThisObject(), "mBatteryPercentView") as TextView
                val mBatteryPercentMarkView = XposedHelpers.getObjectField(param.getThisObject(), "mBatteryPercentMarkView") as TextView
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_swap_batteryicon_percentage")) {
                    batteryView.removeView(mBatteryPercentView)
                    batteryView.removeView(mBatteryPercentMarkView)
                    batteryView.addView(mBatteryPercentMarkView, 0)
                    batteryView.addView(mBatteryPercentView, 0)
                }
                var fontSize = MainModule.mPrefs.getInt("system_statusbar_batterystyle_fontsize", 15) * 0.5f
                if (fontSize > 7.5) {
                    mBatteryTextDigitView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
                    mBatteryPercentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
                }
                fontSize = MainModule.mPrefs.getInt("system_statusbar_batterystyle_mark_fontsize", 15) * 0.5f
                if (fontSize > 7.5) {
                    mBatteryPercentMarkView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
                }
                if (MainModule.mPrefs.getBoolean("system_statusbar_batterystyle_bold")) {
                    mBatteryTextDigitView.typeface = Typeface.DEFAULT_BOLD
                    mBatteryPercentView.typeface = Typeface.DEFAULT_BOLD
                }
                val res = batteryView.resources
                val leftMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    MainModule.mPrefs.getInt("system_statusbar_batterystyle_leftmargin", 0) * 0.5f,
                    res.displayMetrics
                ).toInt()
                var topMargin = 0
                val verticalOffset = MainModule.mPrefs.getInt("system_statusbar_batterystyle_verticaloffset", 8)
                if (verticalOffset != 8) {
                    topMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        (verticalOffset - 8) * 0.5f,
                        res.displayMetrics
                    ).toInt()
                }
                val rightMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    MainModule.mPrefs.getInt("system_statusbar_batterystyle_rightmargin", 0) * 0.5f,
                    res.displayMetrics
                ).toInt()
                val (digitRightMargin, markRightMargin) = if (MainModule.mPrefs.getBoolean("system_statusbaricons_battery4")) {
                    rightMargin to 0
                } else {
                    0 to rightMargin
                }
                if (leftMargin > 0 || topMargin != 8 || digitRightMargin > 0) {
                    mBatteryPercentView.setPaddingRelative(leftMargin, topMargin, digitRightMargin, 0)
                }

                val markVerticalOffset = MainModule.mPrefs.getInt("system_statusbar_batterystyle_mark_verticaloffset", 17)
                val markTopMargin = if (markVerticalOffset < 17) {
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        (markVerticalOffset - 8) * 0.5f,
                        res.displayMetrics
                    ).toInt()
                } else topMargin
                if (markVerticalOffset < 17 || markRightMargin > 0) {
                    mBatteryPercentMarkView.setPaddingRelative(0, markTopMargin, markRightMargin, 0)
                }
            }
        })
    }
}
