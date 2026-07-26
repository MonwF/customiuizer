package tv.withaibuild.customiuizer.mods

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.KeyguardManager
import android.app.Notification
import android.app.PendingIntent
import android.app.WallpaperColors
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaMetadata
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.os.UserHandle
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Pair
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.Display
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceControl
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import miui.os.Build
import miui.process.ForegroundInfo
import miui.process.ProcessManager
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.ResourceHooks
import tv.withaibuild.customiuizer.mods.utils.StepCounterController
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.BatteryIndicator
import tv.withaibuild.customiuizer.utils.Helpers
import tv.withaibuild.customiuizer.utils.PrefMap
import java.lang.reflect.Field
import java.net.NetworkInterface
import java.util.ArrayList
import java.util.Comparator
import java.util.Enumeration
import java.util.HashMap
import java.util.HashSet

object SystemUI {

    private val StatusBarCls = "com.android.systemui.statusbar.phone.CentralSurfacesImpl"

    private var statusbarTextIconLayoutResId = 0

    val textIconTagId = ResourceHooks.getFakeResId("text_icon_tag")
    private val viewInitedTag = ResourceHooks.getFakeResId("view_inited_tag")

    @JvmStatic
    fun setupStatusBar(mContext: Context) {
        statusbarTextIconLayoutResId = MainModule.resHooks.addFakeResource("statusbar_text_icon", R.layout.statusbar_text_icon, "layout")
        if (MainModule.mPrefs.getBoolean("system_statusbar_topmargin")) {
            val topMargin = MainModule.mPrefs.getInt("system_statusbar_topmargin_val", 1)
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_padding_top", topMargin)
        }
        if (MainModule.mPrefs.getBoolean("system_statusbar_horizmargin")) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_padding_start", 0)
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_padding_end", 0)
        }
        if (MainModule.mPrefs.getBoolean("system_cc_enable_style_switch")) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "integer", "force_use_control_panel", 0)
        }
        if (MainModule.mPrefs.getBoolean("system_volumetimer")) {
            val module_volume_timer_segments = intArrayOf(0, 1800, 3600, 7200, 10800, 14400, 18000, 21600, 28800, 36000, 43200)
            MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "integer-array", "miui_volume_timer_segments", module_volume_timer_segments)
        }
        val iconSize = MainModule.mPrefs.getInt("system_statusbar_iconsize", 6)
        if (iconSize > 6) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_size", iconSize)
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_clock_size", iconSize + 0.4f)
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_drawing_size", iconSize)
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_drawing_size_dark", iconSize)
            val notifyPadding = 2.5f * iconSize / 13
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_notification_icon_padding", notifyPadding)
            val iconHeight = 20.5f * iconSize / 13
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_icon_height", iconHeight)
        }
        if (MainModule.mPrefs.getBoolean("system_cc_show_stepcount")) {
            StepCounterController.initContext(mContext)
        }
        if (!MainModule.mPrefs.getBoolean("system_drawer_hidedate")) {
            val drawerDateSize = MainModule.mPrefs.getInt("system_drawer_date_fontsize", 12)
            if (drawerDateSize > 12) {
                MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "qs_control_header_date_size", drawerDateSize)
            }
        }
        if (MainModule.mPrefs.getBoolean("system_taptounlock")) {
            MainModule.resHooks.setResReplacement("com.android.systemui", "string", "default_lockscreen_unlock_hint_text", R.string.system_taptounlock_title)
        }
        val userActivityTimeout = MainModule.mPrefs.getInt("system_lstimeout", 3)
        if (userActivityTimeout > 3) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "integer", "config_lockScreenDisplayTimeout", userActivityTimeout * 1000)
        }
        Settings.System.putLong(mContext.contentResolver, "systemui_restart_time", java.lang.System.currentTimeMillis())
    }

    @JvmStatic
    fun getSlotNameByType(mIconType: Int): String {
        var slotName = ""
        if (mIconType == 91) {
            slotName = "battery_info"
        } else if (mIconType == 92) {
            slotName = "device_temp"
        }
        return slotName
    }

    @JvmStatic
    fun MonitorDeviceInfoHook(lpparam: PackageReadyParam, mPrefs: PrefMap) {
        SystemUIMonitorAndTileHooks.MonitorDeviceInfoHook(lpparam, mPrefs)
    }

    private fun getIconTextView(iconView: View): TextView {
        return XposedHelpers.getObjectField(iconView, "mNetworkSpeedNumberText") as TextView
    }

    @JvmStatic
    fun initStatusbarTextIcon(mContext: Context, iconType: Int, iconView: View, fromController: Boolean) {
        if (!fromController) {
            XposedHelpers.callMethod(iconView, "setBlocked", false)
        }
        val iconTextView = getIconTextView(iconView)
        val res = mContext.resources
        val styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui")
        iconTextView.setTextAppearance(styleId)
        var subKey = ""
        if (iconType == 91) {
            subKey = "batterytempandcurrent"
        } else if (iconType == 92) {
            subKey = "showdevicetemperature"
        }
        val fontSize = MainModule.mPrefs.getInt("system_statusbar_${subKey}_fontsize", 16) * 0.5f
        val opt = MainModule.mPrefs.getStringAsInt("system_statusbar_${subKey}_content", 1)
        if ((opt == 1 || opt == 4 || opt == 5) && !MainModule.mPrefs.getBoolean("system_statusbar_${subKey}_singlerow")) {
            iconTextView.maxLines = 2
            iconTextView.setLineSpacing(0f, if (fontSize > 8.5f) 0.85f else 0.9f)
        }
        iconTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
        if (MainModule.mPrefs.getBoolean("system_statusbar_${subKey}_bold")) {
            iconTextView.typeface = Typeface.DEFAULT_BOLD
        }
        var leftMargin = MainModule.mPrefs.getInt("system_statusbar_${subKey}_leftmargin", 8)
        leftMargin = Helpers.dp2px(leftMargin * 0.5f).toInt()
        var rightMargin = MainModule.mPrefs.getInt("system_statusbar_${subKey}_rightmargin", 8)
        rightMargin = Helpers.dp2px(rightMargin * 0.5f).toInt()
        var topMargin = 0
        val verticalOffset = MainModule.mPrefs.getInt("system_statusbar_${subKey}_verticaloffset", 8)
        if (verticalOffset != 8) {
            topMargin = Helpers.dp2px((verticalOffset - 8) * 0.5f).toInt()
        }
        iconTextView.setPaddingRelative(leftMargin, topMargin, rightMargin, 0)
        val fixedWidth = MainModule.mPrefs.getInt("system_statusbar_${subKey}_fixedcontent_width", 10)
        if (fixedWidth > 10) {
            val lp = iconTextView.layoutParams as LinearLayout.LayoutParams
            lp.width = Helpers.dp2px(fixedWidth.toFloat()).toInt()
            iconTextView.layoutParams = lp
        }

        val align = MainModule.mPrefs.getStringAsInt("system_statusbar_${subKey}_align", 1)
        if (align == 2) {
            iconTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        } else if (align == 3) {
            iconTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        } else if (align == 4) {
            iconTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        }
    }

    @JvmStatic
    fun createStatusbarTextIcon(mContext: Context, lp: LinearLayout.LayoutParams, iconType: Int, fromController: Boolean): View {
        val iconView = LayoutInflater.from(mContext).inflate(statusbarTextIconLayoutResId, null)
        iconView.setTag(textIconTagId, iconType)
        iconView.layoutParams = lp
        val mNumber = iconView.findViewWithTag<View>("network_speed_number")
        XposedHelpers.setObjectField(iconView, "mNetworkSpeedNumberText", mNumber)
        val mUnit = iconView.findViewWithTag<View>("network_speed_unit")
        XposedHelpers.setObjectField(iconView, "mNetworkSpeedUnitText", mUnit)
        initStatusbarTextIcon(mContext, iconType, iconView, fromController)
        return iconView
    }

    val mStatusbarTextIcons = ArrayList<View>()

    @JvmStatic
    fun AddCustomTileHook(lpparam: PackageReadyParam) {
        SystemUIMonitorAndTileHooks.AddCustomTileHook(lpparam)
    }

    @JvmStatic
    fun DualRowsStatusbarHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                var firstRowLeftPadding = 0
                var firstRowRightPadding = 0
                if (MainModule.mPrefs.getBoolean("system_statusbar_dualrows_firstrow_horizmargin")) {
                    firstRowLeftPadding = MainModule.mPrefs.getInt("system_statusbar_dualrows_firstrow_horizmargin_left", 0)
                    firstRowRightPadding = MainModule.mPrefs.getInt("system_statusbar_dualrows_firstrow_horizmargin_right", 0)
                }
                val clock2Rows = MainModule.mPrefs.getBoolean("system_statusbar_dualrows_clock_span2rows")
                val sbView = param.getThisObject() as FrameLayout
                val mContext = sbView.context
                val leftContainer = XposedHelpers.getObjectField(sbView, "mStatusBarLeftContainer") as LinearLayout
                leftContainer.setTag("mStatusBarLeftContainer")
                val statusBarcontents = leftContainer.parent as LinearLayout
                val leftLayout = LinearLayout(mContext)
                val rightLayout = LinearLayout(mContext)
                statusBarcontents.addView(leftLayout, 0)
                statusBarcontents.addView(rightLayout)
                val leftGroup: LinearLayout

                if (clock2Rows) {
                    val mMiuiClock = XposedHelpers.getObjectField(sbView, "mClock") as TextView
                    leftContainer.removeView(mMiuiClock)
                    leftGroup = LinearLayout(mContext)
                    leftLayout.addView(mMiuiClock)
                    leftLayout.addView(leftGroup)
                    leftLayout.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    val groupLp = LinearLayout.LayoutParams(0, -1, 1f)
                    leftGroup.layoutParams = groupLp
                } else {
                    leftGroup = leftLayout
                    if (firstRowLeftPadding > 0) {
                        leftContainer.setPaddingRelative(firstRowLeftPadding, 0, 0, 0)
                    }
                }
                statusBarcontents.removeView(leftContainer)
                leftGroup.addView(leftContainer)
                val secondLeft = LinearLayout(mContext)
                leftGroup.addView(secondLeft)
                leftLayout.id = leftContainer.id
                leftContainer.id = View.NO_ID
                XposedHelpers.setObjectField(sbView, "mStatusBarLeftContainer", leftLayout)

                val rightContainer = XposedHelpers.getObjectField(param.getThisObject(), "mSystemIconArea") as ViewGroup
                val mFullscreenStatusBarNotificationIconArea = XposedHelpers.getObjectField(param.getThisObject(), "mFullscreenStatusBarNotificationIconArea") as View
                rightContainer.removeView(mFullscreenStatusBarNotificationIconArea)
                secondLeft.addView(mFullscreenStatusBarNotificationIconArea)
                val mDripStatusBarNotificationIconArea = XposedHelpers.getObjectField(param.getThisObject(), "mDripStatusBarNotificationIconArea") as View
                leftContainer.removeView(mDripStatusBarNotificationIconArea)
                secondLeft.addView(mDripStatusBarNotificationIconArea)
                secondLeft.orientation = LinearLayout.VERTICAL
                val leftLp = LinearLayout.LayoutParams(-1, 0, 1f)
                leftContainer.layoutParams = leftLp
                secondLeft.layoutParams = leftLp
                secondLeft.gravity = Gravity.START or Gravity.CENTER_VERTICAL

                XposedHelpers.setObjectField(param.getThisObject(), "mSystemIconArea", rightLayout)
                val firstRight = LinearLayout(mContext)
                rightLayout.addView(firstRight)
                firstRight.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                if (firstRowRightPadding > 0) {
                    firstRight.setPaddingRelative(0, 0, firstRowRightPadding, 0)
                }
                val secondRight = LinearLayout(mContext)
                rightLayout.addView(secondRight)
                secondRight.gravity = Gravity.END or Gravity.CENTER_VERTICAL

                rightLayout.orientation = LinearLayout.VERTICAL
                val rightLp = LinearLayout.LayoutParams(-1, 0, 1f)
                firstRight.layoutParams = rightLp
                secondRight.layoutParams = rightLp

                val rightChildCount = rightContainer.childCount
                for (i in rightChildCount - 1 downTo 0) {
                    val child = rightContainer.getChildAt(i)
                    rightContainer.removeView(child)
                    firstRight.addView(child, 0)
                }

                val resSystemIconsId = sbView.resources.getIdentifier("system_icons", "id", lpparam.packageName)
                rightLayout.id = resSystemIconsId

                val showBatteryDetail = MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent")
                val showDeviceTemp = MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature")
                val batteryAtRight = showBatteryDetail && MainModule.mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright")
                val tempAtRight = showDeviceTemp && MainModule.mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright")
                val customIconTypes = ArrayList<Int>()
                if (batteryAtRight) {
                    customIconTypes.add(91)
                }
                if (tempAtRight) {
                    customIconTypes.add(92)
                }
                if (!customIconTypes.isEmpty()) {
                    val DarkIconDispatcher = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.plugins.DarkIconDispatcher")
                    for (iconType in customIconTypes) {
                        val iconView = createStatusbarTextIcon(mContext, LinearLayout.LayoutParams(-2, -2), iconType, false)
                        secondRight.addView(iconView, 0)
                        mStatusbarTextIcons.add(iconView)
                        XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", iconView)
                    }
                }

                statusBarcontents.removeView(rightContainer)

                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "leftLayout", leftLayout)
                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "rightLayout", rightLayout)

                if (MainModule.mPrefs.getBoolean("system_statusbar_netspeed_atsecondrow")) {
                    ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setNetworkSpeedIcon", object : MethodHook() {
                        private var networkSpeedView: View? = null
                        override fun after(param: AfterHookCallback) {
                            val networkSpeedState = param.getArgs()[0]
                            if (networkSpeedView == null) {
                                val ctx = secondRight.context
                                val layoutResId = ctx.resources.getIdentifier("network_speed", "layout", "com.android.systemui")
                                networkSpeedView = LayoutInflater.from(ctx).inflate(layoutResId, null)
                                secondRight.addView(networkSpeedView, 0, LinearLayout.LayoutParams(-2, -2))
                                val DarkIconDispatcher = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.plugins.DarkIconDispatcher")
                                XposedHelpers.callMethod(DarkIconDispatcher, "addDarkReceiver", networkSpeedView)
                            }
                            if (networkSpeedView != null) {
                                XposedHelpers.callMethod(networkSpeedView, "setBlocked", false)
                                XposedHelpers.callMethod(networkSpeedView, "setNetworkSpeed",
                                    XposedHelpers.getObjectField(networkSpeedState, "networkSpeedNumber"),
                                    XposedHelpers.getObjectField(networkSpeedState, "networkSpeedUnit")
                                )
                                XposedHelpers.callMethod(networkSpeedView, "setVisibilityByController",
                                    XposedHelpers.getObjectField(networkSpeedState, "visible")
                                )
                            }
                        }
                    })
                }
            }
        })

        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "updateCutoutLocation", object : MethodHook(-1000) {
            override fun after(param: AfterHookCallback) {
                val mCurrentStatusBarType = XposedHelpers.getObjectField(param.getThisObject(), "mCurrentStatusBarType") as Int
                val leftLayout = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "leftLayout") as LinearLayout?
                val rightLayout = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "rightLayout") as LinearLayout?

                if (leftLayout != null && rightLayout != null) {
                    if (mCurrentStatusBarType == 0) {
                        val leftWidth = MainModule.mPrefs.getInt("system_statusbar_dualrows_left_ratio", 4)
                        val leftLayoutLp = LinearLayout.LayoutParams(0, -1, leftWidth.toFloat())
                        leftLayout.layoutParams = leftLayoutLp
                        val rightLayoutLp = LinearLayout.LayoutParams(0, -1, (10 - leftWidth).toFloat())
                        rightLayout.layoutParams = rightLayoutLp
                    } else {
                        val leftLayoutLp = LinearLayout.LayoutParams(0, -1, 1f)
                        leftLayout.layoutParams = leftLayoutLp
                        val rightLayoutLp = LinearLayout.LayoutParams(0, -1, 1f)
                        rightLayout.layoutParams = rightLayoutLp
                    }
                }
            }
        })
    }

    private fun initDigitalSignalView(mContext: Context, digitalTextView: TextView) {
        val res = mContext.resources
        val styleId = res.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui")
        digitalTextView.setTextAppearance(styleId)
        val subKey = "mobile_digital_signal"
        val fontSize = MainModule.mPrefs.getInt("system_statusbar_${subKey}_fontsize", 26) * 0.5f
        if (MainModule.mPrefs.getBoolean("system_statusbar_${subKey}_in2rows")) {
            digitalTextView.maxLines = 2
            digitalTextView.setLineSpacing(0f, if (fontSize > 8.5f) 0.85f else 0.9f)
        }
        digitalTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize)
        if (MainModule.mPrefs.getBoolean("system_statusbar_${subKey}_bold")) {
            digitalTextView.typeface = Typeface.DEFAULT_BOLD
        }
        var leftMargin = MainModule.mPrefs.getInt("system_statusbar_${subKey}_leftmargin", 8)
        leftMargin = Helpers.dp2px(leftMargin * 0.5f).toInt()
        var rightMargin = MainModule.mPrefs.getInt("system_statusbar_${subKey}_rightmargin", 8)
        rightMargin = Helpers.dp2px(rightMargin * 0.5f).toInt()
        var topMargin = 0
        val verticalOffset = MainModule.mPrefs.getInt("system_statusbar_${subKey}_verticaloffset", 8)
        if (verticalOffset != 8) {
            topMargin = Helpers.dp2px((verticalOffset - 8) * 0.5f).toInt()
        }
        digitalTextView.setPaddingRelative(leftMargin, topMargin, rightMargin, 0)
        val align = MainModule.mPrefs.getStringAsInt("system_statusbar_${subKey}_align", 1)
        if (align == 2) {
            digitalTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        } else if (align == 3) {
            digitalTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        } else if (align == 4) {
            digitalTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
        }
    }

    @JvmStatic
    fun StatusBarDigitalSignalHook(lpparam: PackageReadyParam) {
        val signalLevelMap = SparseIntArray()
        val MobileStatusTrackerClass = XposedHelpers.findClass("com.android.systemui.statusbar.mobile.MobileStatusTracker", lpparam.classLoader)
        val mCallback = XposedHelpers.findField(MobileStatusTrackerClass, "mCallback")
        ModuleHelper.findAndHookMethod(mCallback.type, "onMobileStatusChanged", Boolean::class.javaPrimitiveType!!, "com.android.systemui.statusbar.mobile.MobileStatusTracker\$MobileStatus", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mobileStatus = param.getArgs()[1]
                val mobileSignalController = XposedHelpers.getSurroundingThis(param.getThisObject())
                val subscriptionInfo = XposedHelpers.getObjectField(mobileSignalController, "mSubscriptionInfo") as SubscriptionInfo
                val sid = subscriptionInfo.subscriptionId
                val signalStrength = XposedHelpers.getObjectField(mobileStatus, "signalStrength")
                if (signalStrength != null) {
                    val dbm = XposedHelpers.callMethod(signalStrength, "getDbm") as Int
                    signalLevelMap.put(sid, dbm)
                }
            }
        })
        val stateUpdateHook = object : MethodHook() {
            private var initAction = false
            override fun before(param: BeforeHookCallback) {
                if (param.getMember().name == "updateState") {
                    return
                }
                val mState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                initAction = mState == null
            }

            override fun after(param: AfterHookCallback) {
                val updateStateMethod = param.getMember().name == "updateState"
                val mMobile = XposedHelpers.getObjectField(param.getThisObject(), "mMobile") as View
                val signalImageContainer = mMobile.parent as FrameLayout
                if (initAction) {
                    val digitalView = TextView(signalImageContainer.context)
                    initDigitalSignalView(signalImageContainer.context, digitalView)
                    signalImageContainer.addView(digitalView)
                    digitalView.setTag("digitalSignalView")
                    mMobile.visibility = View.GONE
                }
                if (updateStateMethod || initAction) {
                    val mobileIconState = param.getArgs()[0]
                    val visible = XposedHelpers.getBooleanField(mobileIconState, "visible")
                    if (!visible) return
                    val airplane = XposedHelpers.getBooleanField(mobileIconState, "airplane")
                    if (airplane) return
                    val dualRows = MainModule.mPrefs.getBoolean("system_statusbar_mobile_digital_signal_in2rows")
                    val subId = XposedHelpers.getObjectField(mobileIconState, "subId") as Int
                    val digitalView = signalImageContainer.findViewWithTag<TextView>("digitalSignalView")
                    val hideUnit = MainModule.mPrefs.getBoolean("system_statusbar_mobile_digital_signal_hideunit")
                    if (dualRows) {
                        val slotId = SubscriptionManager.getSlotIndex(subId)
                        if (slotId == 0) {
                            val subSubId = SubscriptionManager.getSubscriptionId(1)
                            digitalView?.text = signalLevelMap.get(subId).toString() + (if (hideUnit) "" else "dBm") +
                                "\n" + signalLevelMap.get(subSubId).toString() + (if (hideUnit) "" else "dBm")
                        }
                    } else {
                        digitalView?.text = signalLevelMap.get(subId).toString() + (if (hideUnit) "" else "dBm")
                    }
                }
                if (!updateStateMethod) {
                    initAction = false
                }
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", stateUpdateHook)
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "updateState", stateUpdateHook)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyDarknessInternal", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mMobileTypeSingle = XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle") as TextView
                val digitalView = (param.getThisObject() as LinearLayout).findViewWithTag<TextView>("digitalSignalView")
                if (digitalView != null) {
                    digitalView.setTextColor(mMobileTypeSingle.currentTextColor)
                }
            }
        })
        val dualRows = MainModule.mPrefs.getBoolean("system_statusbar_mobile_digital_signal_in2rows")
        if (dualRows) {
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setMobileIcons", object : MethodHook() {
                private var isHooked = false
                override fun before(param: BeforeHookCallback) {
                    if (!isHooked) {
                        isHooked = true
                    }
                    val iconStates = param.getArgs()[1] as List<*>
                    if (iconStates.size == 2) {
                        val iconState0 = iconStates[0]
                        val iconState1 = iconStates[1]
                        val mainIconState: Any
                        val subIconState: Any
                        val subId = XposedHelpers.getObjectField(iconState0, "subId") as Int
                        val slotId = SubscriptionManager.getSlotIndex(subId)
                        if (slotId == 0) {
                            mainIconState = iconState0!!
                            subIconState = iconState1!!
                        } else {
                            mainIconState = iconState1!!
                            subIconState = iconState0!!
                        }
                        XposedHelpers.setObjectField(subIconState, "visible", false)
                        val subDataConnected = XposedHelpers.getObjectField(subIconState, "dataConnected") as Boolean
                        if (subDataConnected) {
                            val syncFields = arrayOf("showName", "activityIn", "activityOut", "dataConnected")
                            for (field in syncFields) {
                                XposedHelpers.setObjectField(mainIconState, field, XposedHelpers.getObjectField(subIconState, field))
                            }
                        }
                        param.getArgs()[1] = iconStates
                    }
                }
            })
        }
    }

    private fun getSignalLevel(res: Resources, resId: Int, cache: SparseIntArray): Int {
        if (resId == 0) return 6
        val idx = cache.indexOfKey(resId)
        if (idx >= 0) return cache.valueAt(idx)
        var level = 6
        try {
            val name = res.getResourceName(resId)
            if (name != null && name.contains("signal")) {
                if (name.contains("null")) {
                    level = 6
                } else {
                    val i = name.lastIndexOf("signal_")
                    if (i != -1) {
                        var start = i + "signal_".length
                        var end = start
                        while (end < name.length && Character.isDigit(name[end])) end++
                        if (end > start) {
                            try {
                                level = name.substring(start, end).toInt()
                                if (level < 0 || level > 5) level = 6
                            } catch (ignore: NumberFormatException) {
                            }
                        }
                    }
                }
            }
        } catch (t: Throwable) {
        }
        cache.put(resId, level)
        return level
    }

    private val DUAL_SIGNAL_WHITE_TINT = ColorStateList.valueOf(Color.WHITE)

    private val DUAL_SIGNAL_BLACK_TINT = ColorStateList.valueOf(Color.BLACK)

    private fun applyDualSignalDrawables(mobileView: Any?, mobileIconState: Any?, subLevel: Int, systemUIRes: Resources?, signalResToLevelMap: SparseIntArray, dualSignalResMap: HashMap<String, Int>, selectedIconStyle: String): Boolean {
        if (systemUIRes == null) return false
        val mainSignalResId = XposedHelpers.getIntField(mobileIconState, "strengthId")
        var mainLevel = getSignalLevel(systemUIRes, mainSignalResId, signalResToLevelMap)
        if (mainLevel == 6) mainLevel = 0
        var subLevelVar = subLevel
        if (subLevelVar == 6) subLevelVar = 0
        val mLight = XposedHelpers.getBooleanField(mobileView, "mLight")
        val mUseTint = XposedHelpers.getBooleanField(mobileView, "mUseTint")
        val mSmallRoaming = XposedHelpers.getObjectField(mobileView, "mSmallRoaming")
        val mMobile = XposedHelpers.getObjectField(mobileView, "mMobile")
        if (mMobile == null || mSmallRoaming == null) return false
        var colorMode = ""
        if (mUseTint && selectedIconStyle != "theme") {
            colorMode = "_tint"
        } else if (!mLight) {
            colorMode = "_dark"
        }
        var iconStyle = ""
        if (selectedIconStyle.isNotEmpty()) {
            iconStyle = "_$selectedIconStyle"
        }
        val sim1IconId = "statusbar_signal_1_${mainLevel}${colorMode}${iconStyle}"
        val sim2IconId = "statusbar_signal_2_${subLevelVar}${colorMode}${iconStyle}"
        val sim1ResId = dualSignalResMap[sim1IconId]
        val sim2ResId = dualSignalResMap[sim2IconId]
        if (sim1ResId == null || sim1ResId == 0 || sim2ResId == null || sim2ResId == 0) return false
        XposedHelpers.callMethod(mMobile, "setImageResource", sim1ResId)
        XposedHelpers.callMethod(mSmallRoaming, "setImageResource", sim2ResId)
        var tintList: ColorStateList? = null
        val mMobileRoaming = XposedHelpers.getObjectField(mobileView, "mMobileRoaming")
        if (mMobileRoaming != null) {
            tintList = XposedHelpers.callMethod(mMobileRoaming, "getImageTintList") as ColorStateList?
        }
        if (tintList == null) {
            tintList = if (mLight) DUAL_SIGNAL_WHITE_TINT else DUAL_SIGNAL_BLACK_TINT
        }
        XposedHelpers.callMethod(mMobile, "setImageTintList", tintList)
        XposedHelpers.callMethod(mSmallRoaming, "setImageTintList", tintList)
        return true
    }

    @JvmStatic
    fun DualRowSignalHook(lpparam: PackageReadyParam) {
        val mobileTypeSingle = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single")
        if (!mobileTypeSingle) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_half_to_top_distance", 3)
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_mobile_left_inout_over_strength", 0)
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "status_bar_mobile_type_middle_to_strength_start", -0.4f)
        }

        val dualSignalResMap = HashMap<String, Int>()
        val colorModeList = arrayOf("", "dark", "tint")
        val selectedIconStyle = MainModule.mPrefs.getString("system_statusbar_dualsimin2rows_style", "")

        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", object : MethodHook() {
            private var isHooked = false
            override fun after(param: AfterHookCallback) {
                if (!isHooked) {
                    isHooked = true
                    val mContext = XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext") as Context
                    val modRes = ModuleHelper.getModuleRes(mContext)
                    for (slot in 1..2) {
                        for (lvl in 0..5) {
                            for (colorMode in colorModeList) {
                                if (selectedIconStyle != "theme" || colorMode != "tint") {
                                    val dualIconResName = "statusbar_signal_${slot}_${lvl}" + (if (colorMode.isNotEmpty()) "_$colorMode" else "") + (if (selectedIconStyle.isNotEmpty()) "_$selectedIconStyle" else "")
                                    val iconResId = modRes.getIdentifier(dualIconResName, "drawable", Helpers.modulePkg)
                                    dualSignalResMap[dualIconResName] = MainModule.resHooks.addFakeResource(dualIconResName, iconResId, "drawable")
                                }
                            }
                        }
                    }
                }
            }
        })

        val systemUIRes = arrayOfNulls<Resources>(1)
        val signalResToLevelMap = SparseIntArray()
        val signalStates = intArrayOf(-1, -1) // main-subId, sub-level
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setMobileIcons", object : MethodHook() {
            private var isHooked = false
            override fun before(param: BeforeHookCallback) {
                if (!isHooked) {
                    isHooked = true
                    val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                    val res = mContext.resources
                    systemUIRes[0] = res
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_0", "drawable", lpparam.packageName), 0)
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_1", "drawable", lpparam.packageName), 1)
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_2", "drawable", lpparam.packageName), 2)
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_3", "drawable", lpparam.packageName), 3)
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_4", "drawable", lpparam.packageName), 4)
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_5", "drawable", lpparam.packageName), 5)
                    signalResToLevelMap.put(res.getIdentifier("stat_sys_signal_null", "drawable", lpparam.packageName), 6)
                }
                val iconStates = param.getArgs()[1] as List<*>
                if (iconStates.size == 2) {
                    val mainIconState = iconStates[0]
                    val subIconState = iconStates[1]
                    XposedHelpers.setObjectField(subIconState, "visible", false)
                    val subSignalResId = XposedHelpers.getIntField(subIconState, "strengthId")
                    signalStates[0] = XposedHelpers.getIntField(mainIconState, "subId")
                    signalStates[1] = getSignalLevel(systemUIRes[0]!!, subSignalResId, signalResToLevelMap)
                    val subDataConnected = XposedHelpers.getObjectField(subIconState, "dataConnected") as Boolean
                    if (subDataConnected) {
                        val syncFields = arrayOf("showName", "activityIn", "activityOut", "dataConnected")
                        for (field in syncFields) {
                            XposedHelpers.setObjectField(mainIconState, field, XposedHelpers.getObjectField(subIconState, field))
                        }
                    }
                    param.getArgs()[1] = iconStates
                }
            }
        })

        val stateUpdateHook = object : MethodHook() {
            private var initAction = false
            override fun before(param: BeforeHookCallback) {
                if (param.getMember().name == "updateState") {
                    return
                }
                val mState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                initAction = mState == null
            }
            override fun after(param: AfterHookCallback) {
                val updateStateMethod = param.getMember().name == "updateState"
                if (updateStateMethod || initAction) {
                    val mobileIconState = param.getArgs()[0]
                    val visible = XposedHelpers.getBooleanField(mobileIconState, "visible")
                    if (!visible) return
                    val airplane = XposedHelpers.getBooleanField(mobileIconState, "airplane")
                    if (airplane) return
                    val subId = XposedHelpers.getIntField(mobileIconState, "subId")
                    if (signalStates[0] == -1 || subId != signalStates[0]) return
                    val mSmallHd = XposedHelpers.getObjectField(param.getThisObject(), "mSmallHd")
                    XposedHelpers.callMethod(mSmallHd, "setVisibility", View.GONE)
                    val mSmallRoaming = XposedHelpers.getObjectField(param.getThisObject(), "mSmallRoaming")
                    XposedHelpers.callMethod(mSmallRoaming, "setVisibility", View.VISIBLE)
                }
                if (!updateStateMethod) {
                    initAction = false
                }
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", stateUpdateHook)
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "updateState", stateUpdateHook)

        val resetImageDrawable = object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mobileIconState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                if (mobileIconState == null) return
                val visible = XposedHelpers.getBooleanField(mobileIconState, "visible")
                val airplane = XposedHelpers.getBooleanField(mobileIconState, "airplane")
                val subId = XposedHelpers.getIntField(mobileIconState, "subId")
                if (!visible || airplane || subId != signalStates[0]) return
                applyDualSignalDrawables(param.getThisObject(), mobileIconState, signalStates[1], systemUIRes[0], signalResToLevelMap, dualSignalResMap, selectedIconStyle)
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyDarknessInternal", resetImageDrawable)

        val onDarkChangedSetter = object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mobileIconState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                if (mobileIconState == null) return
                val visible = XposedHelpers.getBooleanField(mobileIconState, "visible")
                val airplane = XposedHelpers.getBooleanField(mobileIconState, "airplane")
                val subId = XposedHelpers.getIntField(mobileIconState, "subId")
                if (!visible || airplane || subId != signalStates[0]) return
                applyDualSignalDrawables(param.getThisObject(), mobileIconState, signalStates[1], systemUIRes[0], signalResToLevelMap, dualSignalResMap, selectedIconStyle)
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "onDarkChanged", onDarkChangedSetter)

        val rightMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_rightmargin", 0)
        val leftMargin = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_leftmargin", 0)
        val iconScale = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_scale", 10)
        val verticalOffset = MainModule.mPrefs.getInt("system_statusbar_dualsimin2rows_verticaloffset", 8)
        if (rightMargin > 0 || leftMargin > 0 || iconScale != 10 || verticalOffset != 8) {
            val initHook = object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val mobileView = param.getThisObject() as LinearLayout
                    val inited = mobileView.getTag(viewInitedTag)
                    if (inited == null) {
                        mobileView.setTag(viewInitedTag, true)
                    } else {
                        return
                    }
                    val rightSpacing = Helpers.dp2px(rightMargin * 0.5f).toInt()
                    val leftSpacing = Helpers.dp2px(leftMargin * 0.5f).toInt()
                    mobileView.setPadding(leftSpacing, 0, rightSpacing, 0)
                    val mMobile = XposedHelpers.getObjectField(param.getThisObject(), "mMobile") as View
                    if (verticalOffset != 8) {
                        val marginTop = Helpers.dp2px((verticalOffset - 8) * 0.5f)
                        val mobileIcon = mMobile.parent as FrameLayout
                        mobileIcon.translationY = marginTop
                    }
                    if (iconScale != 10) {
                        val mSmallRoaming = XposedHelpers.getObjectField(param.getThisObject(), "mSmallRoaming") as View
                        val layoutParams = mMobile.layoutParams as FrameLayout.LayoutParams?
                            ?: FrameLayout.LayoutParams(-2, Helpers.dp2px(2.0f * iconScale).toInt())
                        layoutParams.height = Helpers.dp2px(2.0f * iconScale).toInt()
                        layoutParams.gravity = Gravity.CENTER
                        mMobile.layoutParams = layoutParams
                        mSmallRoaming.layoutParams = layoutParams
                    }
                }
            }
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "setDripEnd", Boolean::class.javaPrimitiveType!!, initHook)
        }
    }

    @JvmStatic
    fun StatusBarIconsPositionAdjustHook(lpparam: PackageReadyParam, moveLeft: Boolean) {
        val mPrefs = MainModule.mPrefs
        val dualRows = mPrefs.getBoolean("system_statusbar_dualrows")
        val swapWifiSignal = mPrefs.getBoolean("system_statusbaricons_swap_wifi_mobile")
        val moveSignalLeft = mPrefs.getBoolean("system_statusbaricons_wifi_mobile_atleft")
        val netspeedAtRow2 = dualRows && mPrefs.getBoolean("system_statusbar_netspeed_atsecondrow")
        val showBatteryDetail = mPrefs.getBoolean("system_statusbar_batterytempandcurrent")
        val showDeviceTemp = mPrefs.getBoolean("system_statusbar_showdevicetemperature")
        val batteryAtRight = showBatteryDetail && !dualRows && mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright")
        val tempAtRight = showDeviceTemp && !dualRows && mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright")
        val batteryAtLeft = showBatteryDetail && !mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright")
        val tempAtLeft = showDeviceTemp && !mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright")

        val leftIcons = HashSet<String>()
        if (!netspeedAtRow2 && mPrefs.getBoolean("system_statusbar_netspeed_atleft")) {
            leftIcons.add("network_speed")
        }
        if (mPrefs.getBoolean("system_statusbar_gps_atleft")) {
            leftIcons.add("location")
        }
        if (mPrefs.getBoolean("system_statusbar_alarm_atleft")) {
            leftIcons.add("alarm_clock")
        }
        if (mPrefs.getBoolean("system_statusbar_sound_atleft")) {
            leftIcons.add("volume")
        }
        if (mPrefs.getBoolean("system_statusbar_dnd_atleft")) {
            leftIcons.add("zen")
        }
        if (batteryAtLeft) {
            leftIcons.add("battery_info")
        }
        if (tempAtLeft) {
            leftIcons.add("device_temp")
        }

        val signalRelatedIcons: List<String>
        signalRelatedIcons = if (!swapWifiSignal) {
            listOf("no_sim", "hd", "mobile", "demo_mobile", "airplane", "hotspot", "wifi", "demo_wifi")
        } else {
            listOf("hotspot", "wifi", "demo_wifi", "no_sim", "hd", "mobile", "demo_mobile", "airplane")
        }

        val leftBlockList = ArrayList<String>()
        val keyguardRightBlockList = ArrayList<String>()

        ModuleHelper.findAndHookConstructor("com.android.systemui.statusbar.phone.StatusBarIconList", lpparam.classLoader, Array<String>::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val allStatusIcons = ArrayList((param.getArgs()[0] as Array<String>).toList())
                val MiuiIconManagerUtils = XposedHelpers.findClass("com.android.systemui.statusbar.phone.MiuiIconManagerUtils", lpparam.classLoader)
                val rightBlockList = ModuleHelper.getStaticObjectFieldSilently(MiuiIconManagerUtils, "RIGHT_BLOCK_LIST") as? ArrayList<String> ?: ArrayList()
                val customIcons = ArrayList<String>()
                if (batteryAtLeft || batteryAtRight) {
                    customIcons.add("battery_info")
                }
                if (tempAtLeft || tempAtRight) {
                    customIcons.add("device_temp")
                }
                if (!customIcons.isEmpty()) {
                    val netspeedIndex = allStatusIcons.indexOf("network_speed") + 1
                    allStatusIcons.addAll(netspeedIndex, customIcons)
                }
                if (netspeedAtRow2) {
                    rightBlockList.add("network_speed")
                }
                if (mPrefs.getBoolean("system_statusbar_alarm_atright")) {
                    rightBlockList.remove("alarm_clock")
                }
                if (mPrefs.getBoolean("system_statusbar_btbattery_atright")) {
                    rightBlockList.remove("bluetooth_handsfree_battery")
                }
                if (mPrefs.getBoolean("system_statusbar_nfc_atright")) {
                    rightBlockList.remove("nfc")
                }
                if (mPrefs.getBoolean("system_statusbar_headset_atright")) {
                    rightBlockList.remove("headset")
                }
                if (mPrefs.getBoolean("system_statusbar_vpn_atright")) {
                    rightBlockList.remove("vpn")
                }
                if (moveLeft) {
                    keyguardRightBlockList.addAll(rightBlockList)
                    for (slotName in allStatusIcons) {
                        if (leftIcons.contains(slotName)) {
                            rightBlockList.add(slotName)
                        } else {
                            leftBlockList.add(slotName)
                        }
                    }
                }
                XposedHelpers.setStaticObjectField(MiuiIconManagerUtils, "RIGHT_BLOCK_LIST", rightBlockList)
                if (swapWifiSignal) {
                    val realSignalIcons = ArrayList<String>()
                    for (slotName in signalRelatedIcons) {
                        if (allStatusIcons.contains(slotName)) {
                            realSignalIcons.add(slotName)
                        }
                    }
                    allStatusIcons.removeAll(signalRelatedIcons)
                    allStatusIcons.addAll(realSignalIcons)
                }
                if (!customIcons.isEmpty() || swapWifiSignal) {
                    param.getArgs()[0] = allStatusIcons.toTypedArray()
                }
            }
        })

        if (moveLeft) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onAttachedToWindow", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val mStatusBar = param.getThisObject() as FrameLayout
                    val IconsContainer = XposedHelpers.findClass("com.android.systemui.statusbar.views.MiuiStatusIconContainer", lpparam.classLoader)
                    val iconContainer = XposedHelpers.newInstance(IconsContainer, mStatusBar.context) as LinearLayout
                    iconContainer.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    iconContainer.setTag("leftIconsContainer")
                    val leftContainer: LinearLayout
                    if (dualRows) {
                        leftContainer = mStatusBar.findViewWithTag<View>("mStatusBarLeftContainer") as LinearLayout
                        leftContainer.addView(iconContainer)
                    } else {
                        val leftNotifyContainer = XposedHelpers.getObjectField(mStatusBar, "mDripStatusBarNotificationIconArea") as View
                        leftContainer = leftNotifyContainer.parent as LinearLayout
                        leftContainer.addView(iconContainer, leftContainer.indexOfChild(leftNotifyContainer))
                    }
                    val miuiIconManagerFactory = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.phone.MiuiIconManagerFactory")

                    val DarkIconManager = XposedHelpers.findClass("com.android.systemui.statusbar.phone.StatusBarIconController\$DarkIconManager", lpparam.classLoader)
                    val mDarkIconManager = XposedHelpers.newInstance(DarkIconManager,
                        iconContainer,
                        XposedHelpers.getObjectField(miuiIconManagerFactory, "mStatusBarPipelineFlags"),
                        XposedHelpers.getObjectField(miuiIconManagerFactory, "mMobileContextProvider"),
                        XposedHelpers.getObjectField(miuiIconManagerFactory, "mDarkIconDispatcher")
                    )

                    val iconController = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.phone.StatusBarIconController")
                    XposedHelpers.callMethod(iconController, "addIconGroup", mDarkIconManager)
                    XposedHelpers.callMethod(iconContainer, "setIgnoredSlots", leftBlockList)
                }
            })

            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiCollapsedStatusBarFragment", lpparam.classLoader, "updateStatusBarVisibilities", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
                private var lastShowLeftIcons = -1
                override fun after(param: AfterHookCallback) {
                    val mLastIsFocusedNotifPromptViewShowing = XposedHelpers.getBooleanField(param.getThisObject(), "mLastIsFocusedNotifPromptViewShowing")
                    val mIsShowNotifPromptView = XposedHelpers.getBooleanField(param.getThisObject(), "mIsShowNotifPromptView")
                    val mLastModifiedVisibility = XposedHelpers.getObjectField(param.getThisObject(), "mLastModifiedVisibility")
                    val showSystemInfo = XposedHelpers.getBooleanField(mLastModifiedVisibility, "showSystemInfo")
                    val showLeftIcons = showSystemInfo && (!mIsShowNotifPromptView || !mLastIsFocusedNotifPromptViewShowing)
                    val showFlag = if (showLeftIcons) 1 else 0
                    if (showFlag == lastShowLeftIcons) return
                    lastShowLeftIcons = showFlag
                    val mStatusBar = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBar") as FrameLayout
                    val leftIconContainer = mStatusBar.findViewWithTag<View>("leftIconsContainer")
                    if (leftIconContainer != null) {
                        leftIconContainer.visibility = if (showLeftIcons) View.VISIBLE else View.INVISIBLE
                    }
                }
            })

            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.classLoader, "miuiOnAttachedToWindow", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val mTintedIconManager = XposedHelpers.getObjectField(param.getThisObject(), "mTintedIconManager")
                    val mBlockList = XposedHelpers.getObjectField(mTintedIconManager, "mBlockList") as ArrayList<Any>
                    mBlockList.clear()
                    mBlockList.addAll(keyguardRightBlockList)
                    val statusBarIconController = XposedHelpers.getObjectField(mTintedIconManager, "mController")
                    XposedHelpers.callMethod(statusBarIconController, "refreshIconGroup", mTintedIconManager)
                }
            })
        }
    }

    @JvmStatic
    fun StatusBarClockPositionHook(lpparam: PackageReadyParam) {
        val pos = MainModule.mPrefs.getStringAsInt("system_statusbar_clock_position", 1)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val sbView = param.getThisObject() as FrameLayout
                val mContext = sbView.context
                val mClockView = XposedHelpers.getObjectField(param.getThisObject(), "mClock") as TextView
                val leftIconsContainer = mClockView.parent as LinearLayout
                leftIconsContainer.removeView(mClockView)
                val spaceView = XposedHelpers.getObjectField(param.getThisObject(), "mCutoutSpace") as View
                val mContentsContainer = spaceView.parent as LinearLayout
                val spaceIndex = mContentsContainer.indexOfChild(spaceView)
                val rightContainer = LinearLayout(mContext)
                val rightLp = LinearLayout.LayoutParams(0, -1, 1.0f)
                val mSystemIconArea = XposedHelpers.getObjectField(param.getThisObject(), "mSystemIconArea") as View
                mContentsContainer.removeView(mSystemIconArea)
                mContentsContainer.addView(rightContainer, spaceIndex + 1, rightLp)
                rightContainer.addView(mSystemIconArea)

                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
                if (pos == 2) {
                    lp.gravity = Gravity.CENTER
                    mContentsContainer.addView(mClockView, spaceIndex, lp)
                } else {
                    rightContainer.addView(mClockView, lp)
                }
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader, "updateLayoutForCutout", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mCurrentStatusBarType = XposedHelpers.getObjectField(param.getThisObject(), "mCurrentStatusBarType") as Int
                val mSystemIconArea = XposedHelpers.getObjectField(param.getThisObject(), "mSystemIconArea") as View
                val mStatusBarLeftContainer = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarLeftContainer") as View
                if (mCurrentStatusBarType == 0) {
                    val mSystemIconAreaLp = mSystemIconArea.layoutParams as LinearLayout.LayoutParams
                    mSystemIconAreaLp.width = 0
                    mSystemIconAreaLp.weight = 1.0f
                    if (pos == 2) {
                        val rightContainer = mSystemIconArea.parent as LinearLayout
                        val mDripStatusBarNotificationIconArea = XposedHelpers.getObjectField(param.getThisObject(), "mDripStatusBarNotificationIconArea") as View
                        mDripStatusBarNotificationIconArea.visibility = View.VISIBLE
                        val mStatusBarLeftContainerLp = mStatusBarLeftContainer.layoutParams as LinearLayout.LayoutParams
                        mStatusBarLeftContainerLp.width = 0
                        mStatusBarLeftContainerLp.weight = 1.0f
                        val sbView = param.getThisObject() as FrameLayout
                        val leftPadding = sbView.paddingStart
                        val rightPadding = sbView.paddingEnd
                        if (Math.abs(leftPadding - rightPadding) > 12) {
                            val topPadding = sbView.paddingTop
                            val bottomPadding = sbView.paddingBottom
                            mStatusBarLeftContainer.setPadding(leftPadding, 0, 0, 0)
                            rightContainer.setPadding(0, 0, rightPadding, 0)
                            sbView.setPadding(0, topPadding, 0, bottomPadding)
                            var focusedNotifView = sbView.findViewWithTag<View>("focused_notif_view")
                            if (focusedNotifView == null) {
                                val focusedNotifViewResId = sbView.resources.getIdentifier("focused_notif_view", "id", "com.android.systemui")
                                if (focusedNotifViewResId > 0) {
                                    focusedNotifView = sbView.findViewById<View>(focusedNotifViewResId)
                                    focusedNotifView.setTag("focused_notif_view")
                                }
                            }
                            focusedNotifView?.let {
                                it.setPaddingRelative(leftPadding, it.paddingTop, 0, 0)
                            }
                        }
                    }
                } else {
                    if (pos == 2) {
                        val mCutoutSpace = XposedHelpers.getObjectField(param.getThisObject(), "mCutoutSpace") as View
                        mCutoutSpace.visibility = View.GONE
                        mStatusBarLeftContainer.setPadding(0, 0, 0, 0)
                        val rightContainer = mSystemIconArea.parent as LinearLayout
                        rightContainer.setPadding(0, 0, 0, 0)
                    }
                }
            }
        })
        if (pos == 2) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "updateNotificationIconAreaInnnerParent", object : MethodHook() {
                private var originType = 0
                override fun before(param: BeforeHookCallback) {
                    val mCurrentStatusBarType = XposedHelpers.getIntField(param.getThisObject(), "mCurrentStatusBarType")
                    if (mCurrentStatusBarType == 0) {
                        XposedHelpers.setObjectField(param.getThisObject(), "mCurrentStatusBarType", 1)
                    }
                    originType = mCurrentStatusBarType
                }
                override fun after(param: AfterHookCallback) {
                    XposedHelpers.setObjectField(param.getThisObject(), "mCurrentStatusBarType", originType)
                }
            })
        }
    }

    private var measureTime = 0L
    private var txBytesTotal = 0L
    private var rxBytesTotal = 0L
    private var txSpeed = 0L
    private var rxSpeed = 0L

    private fun getTrafficBytes(): Pair<Long, Long> {
        var tx = -1L
        var rx = -1L

        try {
            val list = NetworkInterface.getNetworkInterfaces()
            while (list != null && list.hasMoreElements()) {
                val iface = list.nextElement()
                if (iface.isUp && !iface.isVirtual && !iface.isLoopback && !iface.isPointToPoint && "" != iface.name) {
                    tx += TrafficStats.getTxBytes(iface.name)
                    rx += TrafficStats.getRxBytes(iface.name)
                }
            }
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            tx = TrafficStats.getTotalTxBytes()
            rx = TrafficStats.getTotalRxBytes()
        }

        return Pair(tx, rx)
    }

    @SuppressLint("DefaultLocale")
    private fun humanReadableByteCount(ctx: Context, bytes: Long): String {
        try {
            val modRes = ModuleHelper.getModuleRes(ctx)
            val hideSecUnit = MainModule.mPrefs.getBoolean("system_detailednetspeed_secunit")
            var unitSuffix = modRes.getString(R.string.Bs)
            if (hideSecUnit) {
                unitSuffix = ""
            }
            var f = bytes / 1024.0f
            var expIndex = 0
            if (f > 999.0f) {
                expIndex = 1
                f /= 1024.0f
            }
            val pre = modRes.getString(R.string.speedunits)[expIndex]
            return (if (f < 100.0f) String.format("%.1f", f) else String.format("%.0f", f)) + String.format("%s" + unitSuffix, pre)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            return ""
        }
    }

    @JvmStatic
    fun DetailedNetSpeedHook(lpparam: PackageReadyParam) {
        val NetworkSpeedController = XposedHelpers.findClassIfExists("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader)
        if (NetworkSpeedController == null) {
            XposedHelpers.log("DetailedNetSpeedHook", "No NetworkSpeed view or controller")
            return
        }

        val mBgHandlerField = XposedHelpers.findField(NetworkSpeedController, "mBgHandler")
        ModuleHelper.findAndHookMethod(mBgHandlerField.type, "handleMessage", Message::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val message = param.getArgs()[0] as Message
                if (message.what == 200001) {
                    val thisObect = XposedHelpers.getSurroundingThis(param.getThisObject())
                    var isConnected = false
                    val mContext = XposedHelpers.getObjectField(thisObect, "mContext") as Context
                    val mConnectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val nw = mConnectivityManager.activeNetwork
                    if (nw != null) {
                        val capabilities = mConnectivityManager.getNetworkCapabilities(nw)
                        if (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))) {
                            isConnected = true
                        }
                    }
                    if (isConnected) {
                        val nanoTime = java.lang.System.nanoTime()
                        var newTime = nanoTime - measureTime
                        measureTime = nanoTime
                        if (newTime > 12000000000L || newTime == 0L) newTime = Math.round(4 * Math.pow(10.0, 9.0))
                        val bytes = getTrafficBytes()
                        val newTxBytes = bytes.first!!
                        val newRxBytes = bytes.second!!
                        var newTxBytesFixed = newTxBytes - txBytesTotal
                        var newRxBytesFixed = newRxBytes - rxBytesTotal
                        if (newTxBytesFixed < 0 || txBytesTotal == 0L) newTxBytesFixed = 0
                        if (newRxBytesFixed < 0 || rxBytesTotal == 0L) newRxBytesFixed = 0
                        txSpeed = Math.round(newTxBytesFixed / (newTime / Math.pow(10.0, 9.0)))
                        rxSpeed = Math.round(newRxBytesFixed / (newTime / Math.pow(10.0, 9.0)))
                        txBytesTotal = newTxBytes
                        rxBytesTotal = newRxBytes
                    } else {
                        txSpeed = 0
                        rxSpeed = 0
                    }
                }
            }
        })

        ModuleHelper.hookAllMethods(NetworkSpeedController, "updateText", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                val hideLow = MainModule.mPrefs.getBoolean("system_detailednetspeed_low")
                val lowLevel = MainModule.mPrefs.getInt("system_detailednetspeed_lowlevel", 1) * 1024

                val speedStyle = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_style", 1)

                var txarrow = ""
                var rxarrow = ""
                if (speedStyle == 2) {
                    val icons = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_icon", 2)
                    if (icons == 2) {
                        txarrow = if (txSpeed < lowLevel) "△" else "▲"
                        rxarrow = if (rxSpeed < lowLevel) "▽" else "▼"
                    } else if (icons == 3) {
                        txarrow = if (txSpeed < lowLevel) " ☖" else " ☗"
                        rxarrow = if (rxSpeed < lowLevel) " ⛉" else " ⛊"
                    }
                }

                val strArr = arrayOfNulls<String>(2)
                val rx = if (hideLow && rxSpeed < lowLevel) "" else humanReadableByteCount(mContext, rxSpeed) + rxarrow
                if (speedStyle == 2) {
                    val tx = if (hideLow && txSpeed < lowLevel) "" else humanReadableByteCount(mContext, txSpeed) + txarrow
                    strArr[0] = "$tx\n$rx"
                } else {
                    strArr[0] = rx
                }
                strArr[1] = ""
                param.getArgs()[0] = strArr
            }
        })
    }

    private fun initNetSpeedStyle(speedView: LinearLayout) {
        val speedStyle = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_style", 1)
        val numberView = getIconTextView(speedView)
        val unitView = XposedHelpers.getObjectField(speedView, "mNetworkSpeedUnitText") as TextView

        val fontSize = MainModule.mPrefs.getInt("system_netspeed_fontsize", 13)
        if (fontSize > 13) {
            numberView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f)
            if (speedStyle == 1) {
                unitView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f)
            }
        }

        val boldFont = MainModule.mPrefs.getBoolean("system_netspeed_boldfont")
        if (boldFont) {
            numberView.typeface = Typeface.DEFAULT_BOLD
            if (speedStyle == 1) {
                unitView.typeface = Typeface.DEFAULT_BOLD
            }
        }

        val fixedWidth = MainModule.mPrefs.getInt("system_netspeed_fixedcontent_width", 10)
        val singleOrDual = speedStyle == 2 || speedStyle == 3
        if (singleOrDual) {
            numberView.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            unitView.visibility = View.GONE
        }
        if (fixedWidth > 10 || singleOrDual) {
            val lp = numberView.layoutParams as LinearLayout.LayoutParams
            if (fixedWidth > 10) {
                lp.width = Helpers.dp2px(fixedWidth.toFloat()).toInt()
            }
            if (singleOrDual) {
                lp.topMargin = 0
                lp.height = -1
                lp.bottomMargin = 0
            }
            numberView.layoutParams = lp
            unitView.layoutParams = lp
        }

        var leftMargin = MainModule.mPrefs.getInt("system_netspeed_leftmargin", 0)
        leftMargin = Helpers.dp2px(leftMargin * 0.5f).toInt()
        var rightMargin = MainModule.mPrefs.getInt("system_netspeed_rightmargin", 0)
        rightMargin = Helpers.dp2px(rightMargin * 0.5f).toInt()
        var topMargin = 0
        val verticalOffset = MainModule.mPrefs.getInt("system_netspeed_verticaloffset", 8)
        if (verticalOffset != 8) {
            topMargin = Helpers.dp2px((verticalOffset - 8) * 0.5f).toInt()
        }
        speedView.translationY = topMargin.toFloat()
        speedView.setPaddingRelative(leftMargin, 0, rightMargin, 0)

        val align = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_align", 1)
        if (align > 1) {
            var alignVal = View.TEXT_ALIGNMENT_TEXT_START
            if (align == 3) {
                alignVal = View.TEXT_ALIGNMENT_CENTER
            } else if (align == 4) {
                alignVal = View.TEXT_ALIGNMENT_TEXT_END
            }
            numberView.textAlignment = alignVal
            unitView.textAlignment = alignVal
        }

        if (speedStyle == 2) {
            var spacing = 0.9f
            numberView.setSingleLine(false)
            numberView.maxLines = 2
            if (0.5 * fontSize > 8.5f) {
                spacing = 0.85f
            }
            numberView.setLineSpacing(0f, spacing)
        }
    }

    @JvmStatic
    fun NetSpeedStyleHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                if (param.getThisObject() == null) return
                val speedView = param.getThisObject() as LinearLayout
                val inited = speedView.getTag(viewInitedTag)
                val tag = speedView.tag as? String
                if (inited == null && tag != "slot_text_icon") {
                    speedView.setTag(viewInitedTag, true)
                    speedView.postDelayed({ initNetSpeedStyle(speedView) }, 200)
                }
            }
        })

        val useClockStyle = MainModule.mPrefs.getBoolean("system_netspeed_use_clock_style")
        if (useClockStyle) {
            ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val speedView = param.getThisObject() as LinearLayout
                    val tag = speedView.tag as? String
                    if (tag != "slot_text_icon") {
                        val numberView = getIconTextView(speedView)
                        val unitView = XposedHelpers.getObjectField(speedView, "mNetworkSpeedUnitText") as TextView
                        val styleId = speedView.resources.getIdentifier("TextAppearance.StatusBar.Clock", "style", "com.android.systemui")
                        numberView.setTextAppearance(styleId)
                        val speedStyle = MainModule.mPrefs.getStringAsInt("system_detailednetspeed_style", 1)
                        if (speedStyle == 1) {
                            unitView.setTextAppearance(styleId)
                        }
                    }
                }
            })
        }
    }

    @JvmStatic
    fun NetSpeedIntervalHook(lpparam: PackageReadyParam) {
        val NetworkSpeedController = XposedHelpers.findClass("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader)
        val mBgHandlerField = XposedHelpers.findField(NetworkSpeedController, "mBgHandler")
        ModuleHelper.findAndHookMethod(mBgHandlerField.type, "handleMessage", Message::class.java, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val message = param.getArgs()[0] as Message
                if (message.what == 200001) {
                    val mBgHandler = param.getThisObject() as Handler
                    mBgHandler.removeMessages(200001)
                    val newInterval = MainModule.mPrefs.getInt("system_netspeedinterval", 4) * 1000L
                    mBgHandler.sendEmptyMessageDelayed(200001, newInterval)
                }
            }
        })
    }

    @JvmStatic
    fun MobileNetworkTypeHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.connectivity.MobileSignalController", lpparam.classLoader, "getMobileTypeName", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val net = param.getResult() as String
                if (MainModule.mPrefs.getBoolean("system_4gtolte")) {
                    if ("4G" == net) param.setResult("LTE")
                    else if ("4G+" == net) param.setResult("LTE+")
                } else {
                    val mobileType = MainModule.mPrefs.getString("system_statusbar_mobile_showname", "")
                    param.setResult(mobileType)
                }
            }
        })
    }

    @JvmStatic
    fun DisableFakeClockAnimHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "setMNCSwitching", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mNCSwitching = param.getArgs()[0] as Boolean
                if (!mNCSwitching) {
                    val mFakeClock = XposedHelpers.getObjectField(param.getThisObject(), "fakeStatusBarClockController")
                    XposedHelpers.setObjectField(mFakeClock, "ncSwitching", true)
                }
            }
        })
    }

    @JvmStatic
    fun MobileTypeSingleHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "updateMobileTypeLayout", HookerClassHelper.DO_NOTHING)
        val stateHook = object : MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            private var initAction = false

            override fun before(param: BeforeHookCallback) {
                XposedHelpers.setObjectField(param.getArgs()[0], "showMobileDataTypeSingle", true)
                if (param.getMember().name == "updateState") {
                    return
                }
                val mState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                initAction = mState == null
            }

            override fun after(param: AfterHookCallback) {
                val updateStateMethod = param.getMember().name == "updateState"
                if (updateStateMethod || initAction) {
                    val mMobileLeftContainer = XposedHelpers.getObjectField(param.getThisObject(), "mMobileLeftContainer")
                    XposedHelpers.callMethod(mMobileLeftContainer, "setVisibility", View.GONE)
                }
                if (!updateStateMethod) {
                    initAction = false
                }
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", stateHook)
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "updateState", stateHook)

        val initHook = object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mobileView = param.getThisObject() as View
                val inited = ModuleHelper.getViewInfo(mobileView, "mobileTypeHook")
                if (inited == null) {
                    ModuleHelper.setViewInfo(mobileView, "mobileTypeHook", true)
                } else {
                    return
                }
                val mMobileGroup = XposedHelpers.getObjectField(param.getThisObject(), "mMobileGroup") as LinearLayout
                val mMobileTypeSingle = XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle") as TextView
                if (!MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single_atleft")) {
                    mMobileGroup.removeView(mMobileTypeSingle)
                    mMobileGroup.addView(mMobileTypeSingle)
                }
                val mlp = mMobileTypeSingle.layoutParams as ViewGroup.MarginLayoutParams
                var leftMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_leftmargin", 4)
                mlp.leftMargin = Helpers.dp2px(leftMargin * 0.5f).toInt()
                val rightMargin = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_rightmargin", 0)
                if (rightMargin > 0) {
                    mlp.rightMargin = Helpers.dp2px(rightMargin * 0.5f).toInt()
                }
                val verticalOffset = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_verticaloffset", 8)
                if (verticalOffset != 8) {
                    mlp.topMargin = Helpers.dp2px((verticalOffset - 8) * 0.5f).toInt()
                }
                mMobileTypeSingle.layoutParams = mlp
                val fontSize = MainModule.mPrefs.getInt("system_statusbar_mobiletype_single_fontsize", 27)
                mMobileTypeSingle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 0.5f)
                if (MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single_bold")) {
                    mMobileTypeSingle.typeface = Typeface.DEFAULT_BOLD
                }
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "setDripEnd", Boolean::class.javaPrimitiveType!!, initHook)
    }

    private var pluginLoader: ClassLoader? = null

    /**
     * Extract the miui.systemui.plugin ClassLoader from a PluginFactory instance.
     * Tolerates field-name changes by falling back to type-based reflection.
     */
    private fun extractPluginLoader(factory: Any?): ClassLoader? {
        val safeFactory = factory ?: return null
        val clazz = safeFactory.javaClass
        val appInfo = try {
            XposedHelpers.getObjectField(safeFactory, "mAppInfo") as? ApplicationInfo
        } catch (e: Throwable) {
            val field = clazz.declaredFields.firstOrNull { it.type == ApplicationInfo::class.java } ?: return null
            field.isAccessible = true
            field.get(safeFactory) as? ApplicationInfo
        } ?: return null
        if (appInfo.packageName != "miui.systemui.plugin") return null

        val loaderFactory = try {
            XposedHelpers.getObjectField(safeFactory, "mClassLoaderFactory")
        } catch (e: Throwable) {
            val field = clazz.declaredFields.firstOrNull { f ->
                f.name.contains("ClassLoader", ignoreCase = true) && try {
                    f.type.getMethod("get").parameterTypes.isEmpty()
                } catch (e: NoSuchMethodException) {
                    false
                }
            } ?: return null
            field.isAccessible = true
            field.get(safeFactory)
        } ?: return null
        return XposedHelpers.callMethod(loaderFactory, "get") as? ClassLoader
    }

    @JvmStatic
    fun VolumeDialogAutohideDelayHook(classLoader: ClassLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "computeTimeoutH", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mHovering = XposedHelpers.getBooleanField(param.getThisObject(), "mHovering")
                if (mHovering) {
                    param.returnAndSkip(16000)
                    return
                }
                val mSafetyWarning = try {
                    XposedHelpers.getObjectField(param.getThisObject(), "mIsSafetyShowing") as Boolean
                } catch (e: Throwable) {
                    XposedHelpers.getObjectField(param.getThisObject(), "mSafetyWarning") as Boolean
                }
                if (mSafetyWarning) {
                    val opt = MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0)
                    param.returnAndSkip(if (opt > 0) opt else 5000)
                    return
                }
                val mExpanded = XposedHelpers.getBooleanField(param.getThisObject(), "mExpanded")
                val opt = MainModule.mPrefs.getInt(if (mExpanded) "system_volumedialogdelay_expanded" else "system_volumedialogdelay_collapsed", 0)
                if (opt > 0) param.returnAndSkip(opt)
            }
        })
    }

    private var blurCollapsed = 0.0f
    private var blurExpanded = 0.0f

    @JvmStatic
    fun BlurVolumeDialogBackgroundHook(classLoader: ClassLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "updateDialogWindowH", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mWindow = XposedHelpers.getObjectField(param.getThisObject(), "mWindow") as Window
                mWindow.setDimAmount(0.0f)
                val mExpanded = XposedHelpers.getBooleanField(param.getThisObject(), "mExpanded")
                var blurRatio = blurCollapsed
                val isVisible = param.getArgs()[0] as Boolean
                if (mExpanded && !isVisible) {
                    blurRatio = blurExpanded
                }
                if (!mExpanded && blurCollapsed > 0.001f) {
                    mWindow.clearFlags(8)
                }
                if (mExpanded) {
                    XposedHelpers.callMethod(param.getThisObject(), "startBlurAnim", 0f, blurRatio, 0)
                }
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "showH", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                if (blurCollapsed > 0.001f) {
                    val mWindow = XposedHelpers.getObjectField(param.getThisObject(), "mWindow") as Window
                    mWindow.clearFlags(8)
                    XposedHelpers.callMethod(param.getThisObject(), "startBlurAnim", 0f, blurCollapsed, 0)
                }
            }
        })
        ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", classLoader, "initDialog", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                blurCollapsed = MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) / 100f
                blurExpanded = MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) / 100f
                ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
                    override fun onChange(key: String?) {
                        try {
                            if (key == "pref_key_system_volumeblur_collapsed") {
                                blurCollapsed = MainModule.mPrefs.getInt(key, 0) / 100f
                            }
                            if (key == "pref_key_system_volumeblur_expanded") {
                                blurExpanded = MainModule.mPrefs.getInt(key, 0) / 100f
                            }
                        } catch (t: Throwable) {
                            XposedHelpers.log(t)
                        }
                    }
                })
            }
        })
    }

    @JvmStatic
    fun BlurMTKVolumeBarHook(classLoader: ClassLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.Util", classLoader, "isSupportBlurS", HookerClassHelper.returnConstant(true))
    }

    @JvmStatic
    fun initControlCenter() {
        val loader = pluginLoader ?: return
        if (MainModule.mPrefs.getBoolean("system_nosilentvibrate")) {
            ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", loader, "vibrateH", HookerClassHelper.DO_NOTHING)
        }
        if (MainModule.mPrefs.getInt("system_volumedialogdelay_collapsed", 0) > 0 || MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0) > 0) {
            VolumeDialogAutohideDelayHook(loader)
        }
        if (MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) > 0 || MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) > 0) {
            BlurVolumeDialogBackgroundHook(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_volumebar_blur_mtk")) {
            BlurMTKVolumeBarHook(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_volumetimer")) {
            VolumeTimerValuesRes(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_cc_tile_roundedrect")) {
            CCTileCornerHook(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_cc_volume_showpct")) {
            ShowVolumePctHook(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_qs_hideoperator")
            || MainModule.mPrefs.getBoolean("system_cc_hideoperator_delimiter")
            || MainModule.mPrefs.getBoolean("system_cc_show_stepcount")
        ) {
            CCHeaderHook(loader)
        }
        val customCCGrid = MainModule.mPrefs.getInt("system_ccgridcolumns", 4) > 4
        if (customCCGrid) {
            SystemCCGridHookLoader(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_cc_hide_edit")
            || MainModule.mPrefs.getBoolean("system_cc_hide_profile_monitoring")
        ) {
            CCHideEditButtonHook(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_cc_btandtorch_ascard")) {
            CCBluetoothAsCardHook(loader)
        }
        if (MainModule.mPrefs.getBoolean("system_cc_tile_enabled_color")) {
            CCTileColorHook()
        }
        if (MainModule.mPrefs.getBoolean("system_cc_card_enabled_color")) {
            CCCardColorHook()
        }
        if (MainModule.mPrefs.getBoolean("system_cc_slider_color_enable")) {
            CCSliderColorHook()
        }
    }

    @JvmStatic
    fun CCHeaderHook(classLoader: ClassLoader) {
        val hideOperator = MainModule.mPrefs.getBoolean("system_qs_hideoperator")
        val hideDelimiter = MainModule.mPrefs.getBoolean("system_cc_hideoperator_delimiter")
        val showStep = MainModule.mPrefs.getBoolean("system_cc_show_stepcount")
        val stepViewId = ResourceHooks.getFakeResId("cc_step_view")
        val tag = "StepInControlCenter"
        val hideViewHook = object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val headerView = XposedHelpers.callMethod(param.getThisObject(), "getView") as ViewGroup
                if (hideOperator || hideDelimiter) {
                    val resId = headerView.resources.getIdentifier("header_carrier_view", "id", "miui.systemui.plugin")
                    val mCarrierText = headerView.findViewById<TextView>(resId)
                    if (hideOperator) {
                        mCarrierText.text = ""
                    } else {
                        mCarrierText.text = mCarrierText.text.toString().replace(" | ", "")
                    }
                }
                if (showStep) {
                    val stepView = headerView.findViewWithTag<TextView>(tag)
                    if (stepView != null) {
                        val promptInfo = XposedHelpers.getObjectField(param.getThisObject(), "promptInfo")
                        val miuiPromptInfo = XposedHelpers.getObjectField(param.getThisObject(), "miuiPromptInfo")
                        var viz = View.GONE
                        if (promptInfo == null && miuiPromptInfo == null) {
                            val CommonUtils = XposedHelpers.findClass("miui.systemui.util.CommonUtils", classLoader)
                            val INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE")
                            val verticalMode = XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", headerView.context) as Boolean
                            if (verticalMode) {
                                viz = View.VISIBLE
                            }
                        }
                        stepView.visibility = viz
                    }
                }
            }
        }
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.header.StatusHeaderController", classLoader, "adjustCarrierOrPrompt", hideViewHook)

        if (showStep) {
            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.header.StatusHeaderController", classLoader, "onExpandChange", Float::class.javaPrimitiveType!!, object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val headerView = XposedHelpers.callMethod(param.getThisObject(), "getView") as ViewGroup
                    val stepView = headerView.findViewWithTag<TextView>(tag)
                    if (stepView != null) {
                        stepView.translationY = param.getArgs()[0] as Float
                    }
                }
            })
            val initStepViewHook = object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val headerView = XposedHelpers.callMethod(param.getThisObject(), "getView") as ViewGroup
                    var stepView = headerView.findViewWithTag<TextView>(tag)
                    if (stepView == null) {
                        StepCounterController.removeStepViewByTag(tag)
                        stepView = TextView(headerView.context)
                        stepView.id = stepViewId
                        val res = headerView.resources
                        val styleId = res.getIdentifier("TextAppearance.Header.Text", "style", "miui.systemui.plugin")
                        stepView.setTextAppearance(styleId)
                        stepView.setTag(tag)
                        headerView.addView(stepView)
                        StepCounterController.addStepView(stepView)
                    }
                }
            }
            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.header.StatusHeaderController", classLoader, "createStatusBarViews", initStepViewHook)

            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.header.StatusHeaderController", classLoader, "updateConstraint", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val headerView = XposedHelpers.callMethod(param.getThisObject(), "getView") as ViewGroup
                    val CommonUtils = XposedHelpers.findClass("miui.systemui.util.CommonUtils", classLoader)
                    val INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE")
                    val verticalMode = XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", headerView.context) as Boolean
                    if (verticalMode) {
                        val ConstraintSetClass = classLoader.loadClass("androidx.constraintlayout.widget.ConstraintSet")
                        val constraintSet = XposedHelpers.newInstance(ConstraintSetClass)
                        XposedHelpers.callMethod(constraintSet, "clone", headerView)
                        val carrierId = headerView.resources.getIdentifier("header_carrier_view", "id", "miui.systemui.plugin")
                        val iconsId = headerView.resources.getIdentifier("header_status_bar_icons", "id", "miui.systemui.plugin")
                        val dimId = headerView.resources.getIdentifier("header_carrier_vertical_mode_margin_bottom", "dimen", "miui.systemui.plugin")
                        val marginBottom = headerView.resources.getDimensionPixelSize(dimId)
                        XposedHelpers.callMethod(constraintSet, "connect", stepViewId, 4, iconsId, 3, marginBottom)
                        XposedHelpers.callMethod(constraintSet, "connect", stepViewId, 7, carrierId, 6, Helpers.dp2px(4f).toInt())
                        XposedHelpers.callMethod(constraintSet, "applyTo", headerView)
                    }
                }
            })
        }
    }

    @JvmStatic
    fun hasControlCenterModifications(): Boolean {
        return MainModule.mPrefs.getBoolean("system_nosilentvibrate")
            || MainModule.mPrefs.getInt("system_volumedialogdelay_collapsed", 0) > 0
            || MainModule.mPrefs.getInt("system_volumedialogdelay_expanded", 0) > 0
            || MainModule.mPrefs.getInt("system_volumeblur_collapsed", 0) > 0
            || MainModule.mPrefs.getInt("system_volumeblur_expanded", 0) > 0
            || MainModule.mPrefs.getBoolean("system_volumebar_blur_mtk")
            || MainModule.mPrefs.getBoolean("system_volumetimer")
            || MainModule.mPrefs.getBoolean("system_cc_tile_roundedrect")
            || MainModule.mPrefs.getBoolean("system_cc_volume_showpct")
            || MainModule.mPrefs.getBoolean("system_qs_hideoperator")
            || MainModule.mPrefs.getBoolean("system_cc_hideoperator_delimiter")
            || MainModule.mPrefs.getBoolean("system_cc_show_stepcount")
            || MainModule.mPrefs.getInt("system_ccgridcolumns", 4) > 4
            || MainModule.mPrefs.getBoolean("system_cc_hide_edit")
            || MainModule.mPrefs.getBoolean("system_cc_hide_profile_monitoring")
            || MainModule.mPrefs.getBoolean("system_cc_btandtorch_ascard")
            || MainModule.mPrefs.getBoolean("system_cc_tile_enabled_color")
            || MainModule.mPrefs.getBoolean("system_cc_card_enabled_color")
            || MainModule.mPrefs.getBoolean("system_cc_slider_color_enable")
    }

    @JvmStatic
    fun ControlCenterPluginHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.shared.plugins.PluginInstance\$PluginFactory", lpparam.classLoader, "createPlugin", object : MethodHook() {
            private var isHooked = false
            override fun before(param: BeforeHookCallback) {
                if (isHooked) return
                val loader = extractPluginLoader(param.getThisObject()) ?: return
                isHooked = true
                if (pluginLoader == null) {
                    pluginLoader = loader
                    initControlCenter()
                }
            }
        })
    }

    private var iconScaleRatio = 1f

    @JvmStatic
    fun SystemCCGridHookLoader(pluginLoader: ClassLoader) {
        val cols = MainModule.mPrefs.getInt("system_ccgridcolumns", 4)
        iconScaleRatio = 4f / cols
        val resizeIconFrame = object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val thisView = param.getThisObject() as FrameLayout
                val resId = thisView.resources.getIdentifier("icon_frame", "id", "miui.systemui.plugin")
                val iconFrame = thisView.findViewById<View>(resId)
                val iconSize = Helpers.dp2px(68f * iconScaleRatio).toInt()
                iconFrame.layoutParams.width = iconSize
                iconFrame.layoutParams.height = iconSize

                if (param.getMember().name == "onFinishInflate") {
                    XposedHelpers.callMethod(thisView, "changeExpand")
                }
            }
        }
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSTileItemView", pluginLoader, "updateSize", resizeIconFrame)
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSTileItemView", pluginLoader, "onFinishInflate", resizeIconFrame)
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSTileItemView", pluginLoader, "updateContainerHeight", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val iconSize = Helpers.dp2px(85f * iconScaleRatio + 1).toInt()
                XposedHelpers.setObjectField(param.getThisObject(), "containerHeight", iconSize)
            }
        })

        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelController", pluginLoader, "setUseSeparatedPanels", Boolean::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                if (param.getArgs()[0] == null) {
                    param.returnAndSkip(null)
                    return
                }
                val bool = param.getArgs()[0] as Boolean
                val oldVal = XposedHelpers.getObjectField(param.getThisObject(), "useSeparatedPanels")
                if (bool == oldVal) {
                    param.returnAndSkip(null)
                    return
                }
                XposedHelpers.setObjectField(param.getThisObject(), "useSeparatedPanels", bool)
                val horizontalMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "horizontalMainPanel") as LinearLayout
                val leftMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel") as ViewGroup
                horizontalMainPanel.removeView(leftMainPanel)
                if (!bool) {
                    horizontalMainPanel.addView(leftMainPanel)
                    val layoutParams = leftMainPanel.layoutParams
                    (layoutParams as ViewGroup.MarginLayoutParams).setMarginEnd(0)
                    horizontalMainPanel.orientation = LinearLayout.VERTICAL
                } else {
                    horizontalMainPanel.addView(leftMainPanel, 0)
                    val marginId = horizontalMainPanel.resources.getIdentifier("control_center_horizontal_margin_center", "dimen", "miui.systemui.plugin")
                    val marginEnd = horizontalMainPanel.resources.getDimensionPixelSize(marginId)
                    XposedHelpers.setObjectField(param.getThisObject(), "panelMargin", marginEnd)
                    val layoutParams = leftMainPanel.layoutParams
                    (layoutParams as ViewGroup.MarginLayoutParams).setMarginEnd(marginEnd)
                    horizontalMainPanel.orientation = LinearLayout.HORIZONTAL
                }
                param.returnAndSkip(null)
            }
        })

        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelContentDistributor", pluginLoader, "distributePanels", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val horizontal = param.getArgs()[0] as Boolean
                if (!horizontal && XposedHelpers.getBooleanField(param.getThisObject(), "inited")) {
                    val rightPanelContent = XposedHelpers.getObjectField(param.getThisObject(), "rightPanelContent") as ArrayList<*>
                    val leftPanelContent = XposedHelpers.getObjectField(param.getThisObject(), "leftPanelContent") as ArrayList<Any>
                    val size = rightPanelContent.size
                    for (i in size - 1 downTo 0) {
                        val controller = rightPanelContent[i]
                        val className = controller?.javaClass?.canonicalName ?: ""
                        if (className.contains("EditButtonController")
                            || className.contains("SecurityFooterController")
                            || className.contains("QSListController")
                        ) {
                            rightPanelContent.removeAt(i)
                            leftPanelContent.add(controller)
                        } else if (className.contains("FooterSpaceController")) {
                            rightPanelContent.removeAt(i)
                        }
                    }
                    leftPanelContent.sortWith(Comparator { lhs, rhs ->
                        val leftPriority = XposedHelpers.callMethod(lhs, "getPriority") as Int
                        val rightPriority = XposedHelpers.callMethod(rhs, "getPriority") as Int
                        leftPriority - rightPriority
                    })
                }
            }
        })

        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelController", pluginLoader, "updatePanelSize", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val useSeparatedPanels = XposedHelpers.getObjectField(param.getThisObject(), "useSeparatedPanels") as? Boolean
                if (useSeparatedPanels != true) {
                    val leftMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel") as ViewGroup
                    val rightMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "rightMainPanel") as ViewGroup
                    val panelWidth = XposedHelpers.getIntField(param.getThisObject(), "panelWidth")
                    leftMainPanel.layoutParams.width = panelWidth
                    leftMainPanel.layoutParams.height = -2
                    rightMainPanel.layoutParams.width = panelWidth
                    rightMainPanel.layoutParams.height = -2
                    param.returnAndSkip(null)
                }
            }
        })

        val MainPanelAdapter = XposedHelpers.findClass("miui.systemui.controlcenter.panel.main.recyclerview.MainPanelAdapter", pluginLoader)

        val spanSizeHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val adapter = XposedHelpers.getSurroundingThis(param.getThisObject())
                val leftAdapter = XposedHelpers.getAdditionalInstanceField(adapter, "leftAdapter") != null
                if (leftAdapter) {
                    val companion = XposedHelpers.getStaticObjectField(MainPanelAdapter, "Companion")
                    val contentMap = XposedHelpers.getObjectField(adapter, "contentMap")
                    val panelItem = XposedHelpers.callMethod(companion, "getItem", contentMap, param.getArgs()[0])
                    if (panelItem == null) {
                        param.returnAndSkip(cols)
                    } else {
                        param.returnAndSkip(XposedHelpers.callMethod(panelItem, "getSpanSize"))
                    }
                }
            }
        }

        ModuleHelper.hookAllMethods("miui.systemui.controlcenter.panel.main.recyclerview.MainPanelAdapter\$Factory", pluginLoader, "create", object : MethodHook() {
            private var hooked = false
            override fun after(param: AfterHookCallback) {
                if (!hooked) {
                    hooked = true
                    XposedHelpers.setAdditionalInstanceField(param.getResult(), "leftAdapter", true)
                    val layoutManager = XposedHelpers.getObjectField(param.getResult(), "layoutManager")
                    XposedHelpers.callMethod(layoutManager, "setSpanCount", cols)
                    val spanSizeLookup = XposedHelpers.callMethod(layoutManager, "getSpanSizeLookup")
                    ModuleHelper.findAndHookMethod(spanSizeLookup.javaClass, "getSpanSize", Int::class.javaPrimitiveType!!, spanSizeHook)
                }
            }
        })

        val columnsReplaceHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                param.returnAndSkip(cols)
            }
        }
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.header.HeaderSpaceController", pluginLoader, "getSpanSize", columnsReplaceHook)
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.security.SecurityFooterController", pluginLoader, "getSpanSize", columnsReplaceHook)
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.qs.EditButtonController", pluginLoader, "getSpanSize", columnsReplaceHook)
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.qs.QSListController\$EditModeDividerTextItem", pluginLoader, "getSpanSize", columnsReplaceHook)

        // handle secondary panel show
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "updateVisibility", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val CommonUtils = XposedHelpers.findClass("miui.systemui.util.CommonUtils", pluginLoader)
                val INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE")
                val mContext = XposedHelpers.callMethod(param.getThisObject(), "getContext")
                val verticalMode = XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", mContext) as Boolean
                if (verticalMode) {
                    val leftMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel") as ViewGroup
                    leftMainPanel.visibility = param.getArgs()[0] as Int
                }
            }
        })
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "forceToShow", Object::class.java, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val leftMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel") as ViewGroup
                leftMainPanel.alpha = 1.0f
            }
        })
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "onAnimUpdate", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val CommonUtils = XposedHelpers.findClass("miui.systemui.util.CommonUtils", pluginLoader)
                val INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE")
                val mContext = XposedHelpers.callMethod(param.getThisObject(), "getContext")
                val verticalMode = XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", mContext) as Boolean
                if (verticalMode) {
                    val leftMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel") as ViewGroup
                    val rightMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "rightMainPanel") as ViewGroup
                    val alpha = rightMainPanel.alpha
                    leftMainPanel.alpha = alpha
                }
            }
        })
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.panel.main.MainPanelAnimController", pluginLoader, "onConfigurationChanged", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val i = param.getArgs()[0] as Int
                if ((i and 128) != 0) {
                    val CommonUtils = XposedHelpers.findClass("miui.systemui.util.CommonUtils", pluginLoader)
                    val INSTANCE = XposedHelpers.getStaticObjectField(CommonUtils, "INSTANCE")
                    val mContext = XposedHelpers.callMethod(param.getThisObject(), "getContext")
                    val verticalMode = XposedHelpers.callMethod(INSTANCE, "getInVerticalMode", mContext) as Boolean
                    val leftMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "leftMainPanel") as ViewGroup
                    val rightMainPanel = XposedHelpers.getObjectField(param.getThisObject(), "rightMainPanel") as ViewGroup
                    if (verticalMode) {
                        leftMainPanel.alpha = rightMainPanel.alpha
                        leftMainPanel.visibility = rightMainPanel.visibility
                    } else {
                        leftMainPanel.alpha = 1.0f
                        leftMainPanel.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    @JvmStatic
    fun CCHideEditButtonHook(pluginLoader: ClassLoader) {
        val hideEdit = MainModule.mPrefs.getBoolean("system_cc_hide_edit")
        val hideSecurity = MainModule.mPrefs.getBoolean("system_cc_hide_profile_monitoring")
        if (!hideEdit && !hideSecurity) return

        fun shouldHide(controller: Any?): Boolean {
            val className = controller?.javaClass?.canonicalName ?: ""
            return (hideEdit && className.contains("EditButtonController"))
                || (hideSecurity && className.contains("SecurityFooterController"))
        }

        // Filter the source list when the distributor is first created.
        ModuleHelper.hookAllConstructors("miui.systemui.controlcenter.panel.main.MainPanelContentDistributor", pluginLoader, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val thisObj = param.getThisObject()
                val rawChildControllers = try {
                    XposedHelpers.getObjectField(thisObj, "childControllers")
                } catch (t: Throwable) {
                    return
                }

                val childControllers: MutableList<Any?> = when (rawChildControllers) {
                    is MutableList<*> -> rawChildControllers.toMutableList()
                    is List<*> -> ArrayList(rawChildControllers as Collection<Any?>)
                    else -> return
                }

                val iter = childControllers.iterator()
                while (iter.hasNext()) {
                    if (shouldHide(iter.next())) iter.remove()
                }

                try {
                    XposedHelpers.setObjectField(thisObj, "childControllers", childControllers)
                } catch (ignored: Throwable) {
                    // final or unmodifiable field: rely on the distributePanels fallback
                }
            }
        })

        // Also remove from the panel content lists each time they are rebuilt.
        ModuleHelper.hookAllMethods("miui.systemui.controlcenter.panel.main.MainPanelContentDistributor", pluginLoader, "distributePanels", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val thisObj = param.getThisObject()
                val rightPanelContent = try {
                    XposedHelpers.getObjectField(thisObj, "rightPanelContent") as? ArrayList<*>
                } catch (t: Throwable) {
                    null
                } ?: return
                val leftPanelContent = try {
                    XposedHelpers.getObjectField(thisObj, "leftPanelContent") as? ArrayList<*>
                } catch (t: Throwable) {
                    null
                } ?: return

                val sizeRight = rightPanelContent.size
                for (i in sizeRight - 1 downTo 0) {
                    if (shouldHide(rightPanelContent[i])) rightPanelContent.removeAt(i)
                }
                val sizeLeft = leftPanelContent.size
                for (i in sizeLeft - 1 downTo 0) {
                    if (shouldHide(leftPanelContent[i])) leftPanelContent.removeAt(i)
                }
            }
        })
    }

    @JvmStatic
    fun CCBluetoothAsCardHook(pluginLoader: ClassLoader) {
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.QSController", pluginLoader, "getCardStyleTileSpecs", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                param.returnAndSkip(listOf("wifi", "cell", "bt", "flashlight"))
            }
        })
    }

    @JvmStatic
    fun CCTileColorHook() {
        val customColor = MainModule.mPrefs.getInt("system_cc_tile_enabled_color_custom", 0xff277af7.toInt())
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_enabled_color", customColor)
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_warning_color", customColor)

        val iconColor = MainModule.mPrefs.getInt("system_cc_tile_enabled_iconcolor_custom", 0xffffffff.toInt())
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_icon_enabled_color", iconColor)
    }

    @JvmStatic
    fun CCCardColorHook() {
        val customColor = MainModule.mPrefs.getInt("system_cc_card_enabled_color_custom", 0xff3482ff.toInt())
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_cellular_color", customColor)
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_enabled_color", customColor)
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_flashlight_color", customColor)

        val primaryColor = MainModule.mPrefs.getInt("system_cc_card_enabled_primary_textcolor", 0xffffffff.toInt())
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_primary_text_enabled_color", primaryColor)
        val secondaryColor = MainModule.mPrefs.getInt("system_cc_card_enabled_secondary_textcolor", 0x80ffffff.toInt())
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "qs_card_secondary_text_enabled_color", secondaryColor)

        val iconColor = MainModule.mPrefs.getInt("system_cc_card_enabled_iconcolor_custom", 0xffffffff.toInt())
        if (iconColor != 0xffffffff.toInt()) {
            val loader = pluginLoader ?: return
            ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSCardItemIconView", loader, "updateResources", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    XposedHelpers.setObjectField(param.getThisObject(), "iconColor", iconColor)
                }
            })
        }
    }

    @JvmStatic
    fun CCSliderColorHook() {
        val customColor = MainModule.mPrefs.getInt("system_cc_slider_progress_color", 0xffffffff.toInt())
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "toggle_slider_progress_color", customColor)
        val blendColors = intArrayOf(customColor, 3)
        MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "integer-array", "toggle_slider_progress_blend_colors", blendColors)

        val iconColor = MainModule.mPrefs.getInt("system_cc_slider_icon_color", 0xff959595.toInt())
        if (iconColor != 0xff959595.toInt()) {
            MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "color", "toggle_slider_icon_color", iconColor)
            val iconBlendColors = intArrayOf(iconColor, 3)
            MainModule.resHooks.setThemeValueReplacement("miui.systemui.plugin", "integer-array", "toggle_slider_icon_blend_colors", iconBlendColors)
        }
    }

    @JvmStatic
    fun VolumeTimerValuesRes(pluginLoader: ClassLoader) {
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "initTimerString", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                val mTimeSegmentTitle = arrayOfNulls<String>(11)
                val timerOffId = mContext.resources.getIdentifier("timer_off", "string", "miui.systemui.plugin")
                val minuteId = mContext.resources.getIdentifier("timer_30_minutes", "string", "miui.systemui.plugin")
                val hourId = mContext.resources.getIdentifier("timer_1_hour", "string", "miui.systemui.plugin")
                mTimeSegmentTitle[0] = mContext.resources.getString(timerOffId)
                mTimeSegmentTitle[1] = mContext.resources.getString(minuteId, 30)
                mTimeSegmentTitle[2] = mContext.resources.getString(hourId, 1)
                mTimeSegmentTitle[3] = mContext.resources.getString(hourId, 2)
                mTimeSegmentTitle[4] = mContext.resources.getString(hourId, 3)
                mTimeSegmentTitle[5] = mContext.resources.getString(hourId, 4)
                mTimeSegmentTitle[6] = mContext.resources.getString(hourId, 5)
                mTimeSegmentTitle[7] = mContext.resources.getString(hourId, 6)
                mTimeSegmentTitle[8] = mContext.resources.getString(hourId, 8)
                mTimeSegmentTitle[9] = mContext.resources.getString(hourId, 10)
                mTimeSegmentTitle[10] = mContext.resources.getString(hourId, 12)
                XposedHelpers.setObjectField(param.getThisObject(), "mTimeSegmentTitle", mTimeSegmentTitle)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.TimerItem", pluginLoader, "getTimePos", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val timer = XposedHelpers.getObjectField(param.getThisObject(), "mTimerTime")
                val halfTimerWidth = (XposedHelpers.callMethod(timer, "getWidth") as Int) / 2.0f
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                val mTimerSeekbarWidth = ModuleHelper.getObjectFieldSilently(param.getThisObject(), "mTimerSeekbarWidth")
                val seekbarWidthResId: Int
                if (mTimerSeekbarWidth == ModuleHelper.NOT_EXIST_SYMBOL) {
                    seekbarWidthResId = mContext.resources.getIdentifier("miui_volume_timer_seelbar_width", "dimen", "miui.systemui.plugin")
                } else {
                    seekbarWidthResId = mTimerSeekbarWidth as Int
                }
                val mTimerSeekbarMarginLeft = mContext.resources.getIdentifier("miui_volume_timer_seekbar_margin_left", "dimen", "miui.systemui.plugin")
                val seekWidth = mContext.resources.getDimension(seekbarWidthResId)
                val marginLeft = mContext.resources.getDimensionPixelSize(mTimerSeekbarMarginLeft)
                val seg = XposedHelpers.getObjectField(param.getThisObject(), "mDeterminedSegment") as Int
                param.returnAndSkip(seekWidth / 10 * seg + marginLeft - halfTimerWidth)
            }
        })

        val segHook = object : MethodHook() {
            private var prevSeg = 0
            override fun before(param: BeforeHookCallback) {
                prevSeg = XposedHelpers.getIntField(param.getThisObject(), "mCurrentSegment")
                if (prevSeg < 3 || (prevSeg == 3 && XposedHelpers.getIntField(param.getThisObject(), "mDeterminedSegment") == 3)) {
                    XposedHelpers.setIntField(param.getThisObject(), "mCurrentSegment", 0)
                }
            }
            override fun after(param: AfterHookCallback) {
                XposedHelpers.setIntField(param.getThisObject(), "mCurrentSegment", prevSeg)
            }
        }

        ModuleHelper.findAndHookMethod("com.android.systemui.miui.volume.MiuiVolumeTimerDrawableHelper", pluginLoader, "updateDrawables", segHook)
    }

    @JvmStatic
    fun CCTileCornerHook(pluginLoader: ClassLoader) {
        ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", pluginLoader, "getCornerRadius", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val radius = 20 * iconScaleRatio
                param.returnAndSkip(Helpers.dp2px(radius))
            }
        })
        val radiusHook = object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val drawable = param.getResult()
                val gradientDrawable = drawable as? GradientDrawable
                if (gradientDrawable != null) {
                    val radius = 20 * iconScaleRatio
                    gradientDrawable.cornerRadius = Helpers.dp2px(radius)
                }
            }
        }
        ModuleHelper.hookAllMethods("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", pluginLoader, "getDisabledBackgroundDrawable", radiusHook)
        ModuleHelper.hookAllMethods("miui.systemui.controlcenter.qs.tileview.QSTileItemIconView", pluginLoader, "getActiveBackgroundDrawable", radiusHook)
    }

    private var isSlidingStart = false
    private var isSliding = false
    private var tapStartX = 0f
    private var tapStartY = 0f
    private var tapStartPointers = 0f
    private var tapStartBrightness = 0f
    private var topMinimumBacklight = 0.0f
    private var topMaximumBacklight = 1.0f
    private var currentTouchX = 0f
    private var currentTouchTime = 0L
    private var currentDownTime = 0L
    private var currentDownX = 0f
    private var nextBrightNess = -999f

    @JvmStatic
    fun StatusBarGesturesHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "setExpandedHeightInternal", Float::class.javaPrimitiveType!!, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mExpandedFraction = XposedHelpers.getFloatField(param.getThisObject(), "mExpandedFraction")
                if (mExpandedFraction > 0.33f) {
                    currentTouchTime = 0L
                    currentTouchX = 0f
                    currentDownTime = 0L
                    currentDownX = 0f
                }
            }
        })

        val hook = object : MethodHook() {
            private var mBrightnessController: Any? = null
            private var sbHeight = -1

            @SuppressLint("SetTextI18n")
            override fun before(param: BeforeHookCallback) {
                val clsName = param.getThisObject()!!.javaClass.simpleName
                val isInControlCenter = "ControlCenterWindowViewImpl" == clsName
                if (isInControlCenter) {
                    if (param.getArgs().size == 2 && (param.getArgs()[1] as Boolean)) {
                        return
                    }
                    val statusBarStateController = XposedHelpers.getObjectField(param.getThisObject(), "statusBarStateController")
                    val state = XposedHelpers.callMethod(statusBarStateController, "getState") as Int
                    if (state == 1 || state == 2) {
                        return
                    }
                }
                val mContext = (param.getThisObject() as View).context
                val res = mContext.resources
                if (sbHeight == -1) {
                    sbHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height_default", "dimen", "android"))
                }
                val event = param.getArgs()[0] as MotionEvent
                var mDisplayManager: Any? = null
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        tapStartX = event.x
                        tapStartY = event.y
                        isSlidingStart = !isInControlCenter || tapStartY <= sbHeight
                        tapStartPointers = 1f
                        if (mBrightnessController == null) {
                            val mControlCenterController: Any? = if (isInControlCenter) {
                                XposedHelpers.getObjectField(param.getThisObject(), "controlCenterController")
                            } else {
                                ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.controlcenter.policy.ControlCenterControllerImpl")
                            }
                            mBrightnessController = XposedHelpers.callMethod(XposedHelpers.getObjectField(mControlCenterController, "brightnessController"), "get")
                        }
                        mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager")
                        val mDisplayId = mContext.display.displayId
                        topMinimumBacklight = XposedHelpers.getObjectField(mBrightnessController, "mMinimumBacklight") as Float
                        topMaximumBacklight = XposedHelpers.getObjectField(mBrightnessController, "mMaximumBacklight") as Float
                        tapStartBrightness = XposedHelpers.callMethod(mDisplayManager, "getBrightness", mDisplayId) as Float
                        if (isSlidingStart) {
                            currentDownTime = java.lang.System.currentTimeMillis()
                            currentDownX = tapStartX
                        } else {
                            currentDownTime = 0L
                            currentDownX = 0f
                        }
                        nextBrightNess = -999f
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        tapStartPointers = event.pointerCount.toFloat()
                    }
                    MotionEvent.ACTION_UP -> {
                        val lastTouchTime = currentTouchTime
                        val lastTouchX = currentTouchX
                        currentTouchTime = java.lang.System.currentTimeMillis()
                        currentTouchX = event.x
                        val mTouchX = currentTouchX
                        val mTouchTime = currentTouchTime
                        if (currentTouchTime - lastTouchTime < 250L && Math.abs(currentTouchX - lastTouchX) < 80F) {
                            currentTouchTime = 0L
                            currentTouchX = 0F
                            val screenWidth = res.displayMetrics.widthPixels
                            var actionKey = "system_statusbarcontrols_dt"
                            if (mTouchX * 5 < screenWidth) {
                                actionKey = "system_statusbarcontrols_dt_left"
                            } else if (mTouchX > screenWidth * 0.8) {
                                actionKey = "system_statusbarcontrols_dt_right"
                            }
                            GlobalActions.handleAction(mContext, actionKey)
                        } else if ((mTouchTime - currentDownTime > 600 && mTouchTime - currentDownTime < 4000)
                            && Math.abs(mTouchX - currentDownX) < 80F) {
                            if (MainModule.mPrefs.getBoolean("system_statusbarcontrols_longpress_vibrate")) {
                                val ignoreOff = MainModule.mPrefs.getBoolean("system_statusbarcontrols_longpress_vibrate_ignoreoff")
                                Helpers.performStrongVibration(mContext, ignoreOff)
                            }
                            GlobalActions.handleAction(mContext, "system_statusbarcontrols_longpress")
                        }
                        if (nextBrightNess > -10) {
                            mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager")
                            val displayId = XposedHelpers.getIntField(mBrightnessController, "mDisplayId")
                            XposedHelpers.callMethod(mDisplayManager, "setBrightness", displayId, nextBrightNess)
                            nextBrightNess = -999f
                        }
                        currentDownTime = 0L
                        currentDownX = 0f
                        isSlidingStart = false
                        isSliding = false
                        nextBrightNess = -999f
                    }
                    MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                        isSlidingStart = false
                        isSliding = false
                        nextBrightNess = -999f
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!isSlidingStart) return
                        if (event.y - tapStartY > sbHeight) {
                            currentDownTime = 0L
                            currentDownX = 0f
                            return
                        }
                        val metrics = res.displayMetrics
                        val delta = event.x - tapStartX
                        if (delta == 0f) return
                        if (!isSliding && Math.abs(delta) > metrics.widthPixels / 10f) isSliding = true
                        if (!isSliding) return
                        val opt = MainModule.mPrefs.getStringAsInt(if (tapStartPointers == 2f) "system_statusbarcontrols_dual" else "system_statusbarcontrols_single", 1)
                        if (opt == 2) {
                            val sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_bright", 2)
                            var ratio = delta / metrics.widthPixels
                            ratio = (if (sens == 1) 0.66f else if (sens == 3) 1.66f else 1.0f) * ratio * 0.618f
                            val nextLevel = Math.min(topMaximumBacklight, Math.max(topMinimumBacklight, tapStartBrightness + (topMaximumBacklight - topMinimumBacklight) * ratio))
                            mDisplayManager = XposedHelpers.getObjectField(mBrightnessController, "mDisplayManager")
                            val displayId = XposedHelpers.getIntField(mBrightnessController, "mDisplayId")
                            XposedHelpers.callMethod(mDisplayManager, "setTemporaryBrightness", displayId, nextLevel)
                            nextBrightNess = nextLevel
                        } else if (opt == 3) {
                            val sens = MainModule.mPrefs.getStringAsInt("system_statusbarcontrols_sens_vol", 2)
                            if (Math.abs(delta) < metrics.widthPixels / ((if (sens == 1) 0.66f else if (sens == 3) 1.66f else 1.0f) * 20 * metrics.density)) return
                            tapStartX = event.x
                            val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            @Suppress("WrongConstant")
                            audioManager.adjustVolume(if (delta > 0) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER, (1 shl 12) or AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_ALLOW_RINGER_MODES or AudioManager.FLAG_VIBRATE)
                        }
                    }
                }
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader, "onInterceptTouchEvent", MotionEvent::class.java, hook)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader, "onTouchEvent", MotionEvent::class.java, hook)
        ModuleHelper.hookAllMethods("com.android.systemui.shared.plugins.PluginInstance\$PluginFactory", lpparam.classLoader, "createPlugin", object : MethodHook() {
            private var isHooked = false
            override fun before(param: BeforeHookCallback) {
                if (isHooked) return
                val loader = extractPluginLoader(param.getThisObject()) ?: return
                isHooked = true
                if (pluginLoader == null) pluginLoader = loader
                ModuleHelper.findAndHookMethod("miui.systemui.controlcenter.windowview.ControlCenterWindowViewImpl", loader, "handleMotionEvent", MotionEvent::class.java, Boolean::class.javaPrimitiveType!!, hook)
            }
        })
    }

    @JvmStatic
    fun HorizMarginHook(lpparam: PackageReadyParam) {
        val horizHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val leftMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_left", 16)
                val leftMarginPx = Helpers.dp2px(leftMargin.toFloat()).toInt()
                val rightMargin = MainModule.mPrefs.getInt("system_statusbar_horizmargin_right", 16)
                val rightMarginPx = Helpers.dp2px(rightMargin.toFloat()).toInt()
                param.returnAndSkip(Pair(leftMarginPx, rightMarginPx))
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarContentInsetsProvider", lpparam.classLoader, "getStatusBarContentInsetsForCurrentRotation", horizHook)
    }

    @JvmStatic
    fun LockScreenTopMarginHook(lpparam: PackageReadyParam) {
        val statusBarPaddingTop = IntArray(1)
        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mContext = XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext") as Context
                val dimenResId = mContext.resources.getIdentifier("status_bar_padding_top", "dimen", lpparam.packageName)
                statusBarPaddingTop[0] = mContext.resources.getDimensionPixelSize(dimenResId)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.classLoader, "updateViewStatusBarPaddingTop", View::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val view = param.getArgs()[0] as View?
                if (view != null) {
                    view.setPadding(view.paddingLeft, statusBarPaddingTop[0], view.paddingRight, view.paddingBottom)
                    param.returnAndSkip(null)
                }
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiKeyguardStatusBarView", lpparam.classLoader, "onFinishInflate", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                XposedHelpers.callMethod(param.getThisObject(), "onDensityOrFontScaleChanged")
            }
        })
    }

    @JvmStatic
    fun HideIconsVoWiFiHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.MiuiOperatorCustomizedPolicy\$MiuiOperatorConfig", lpparam.classLoader, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                param.getArgs()[3] = true
            }
        })
    }

    @JvmStatic
    fun HideIconsSignalHook(lpparam: PackageReadyParam) {
        val stateHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mobileIconState = param.getArgs()[0]
                var shouldUpdate = "updateState" == param.getMember().name
                if (!shouldUpdate) {
                    val mState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                    shouldUpdate = mState == null
                }
                if (!shouldUpdate) return
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_signal")) {
                    if (!MainModule.mPrefs.getBoolean("system_statusbaricons_signal_wificonnected") || XposedHelpers.getBooleanField(mobileIconState, "wifiAvailable")) {
                        XposedHelpers.setObjectField(mobileIconState, "visible", false)
                        return
                    }
                }
                val subId = XposedHelpers.getObjectField(mobileIconState, "subId") as Int
                val dataSubId = SubscriptionManager.getActiveDataSubscriptionId()
                val slotId = SubscriptionManager.getSlotIndex(subId)
                if ((MainModule.mPrefs.getBoolean("system_statusbaricons_sim1") && slotId == 0)
                    || (MainModule.mPrefs.getBoolean("system_statusbaricons_sim2") && slotId == 1)
                    || (MainModule.mPrefs.getBoolean("system_statusbaricons_sim_nodata") && subId != dataSubId)
                ) {
                    XposedHelpers.setObjectField(mobileIconState, "visible", false)
                    return
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_roaming")) {
                    XposedHelpers.setObjectField(mobileIconState, "roaming", false)
                }
                if (MainModule.mPrefs.getBoolean("system_statusbaricons_volte")) {
                    XposedHelpers.setObjectField(mobileIconState, "volte", false)
                    XposedHelpers.setObjectField(mobileIconState, "speechHd", false)
                }
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", stateHook)
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "updateState", stateHook)
    }

    private fun checkSlot(slotName: String?): Boolean {
        return try {
            ("headset" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_headset"))
                || ("volume" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_sound"))
                || ("zen" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_dnd"))
                || ("alarm_clock" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_alarm"))
                || ("managed_profile" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_profile"))
                || ("vpn" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_vpn"))
                || ("airplane" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_airplane"))
                || ("nfc" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_nfc"))
                || ("second_space" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_secondspace"))
                || ("location" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_gps"))
                || ("wifi" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_wifi"))
                || ("hotspot" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_hotspot"))
                || ("no_sim" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_nosims"))
                || ("bluetooth_handsfree_battery" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_btbattery"))
                || ("ble_unlock_mode" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_ble_unlock"))
                || ("bluetooth" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_bluetoothicn"))
                || ("hd" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_volte"))
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            false
        }
    }

    @JvmStatic
    fun HideIconsHook(lpparam: PackageReadyParam) {
        val iconHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val iconType = param.getArgs()[0] as String
                if (checkSlot(iconType)) {
                    param.getArgs()[1] = false
                }
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.StatusBarIconControllerImpl", lpparam.classLoader, "setIconVisibility", String::class.java, Boolean::class.javaPrimitiveType!!, iconHook)
    }

    @JvmStatic
    fun HideIconsFromSystemManager(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.CommandQueue", lpparam.classLoader, "setIcon", String::class.java, "com.android.internal.statusbar.StatusBarIcon", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val slotName = param.getArgs()[0] as String
                if (("stealth" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_privacy"))
                    || ("mute" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_mute"))
                    || ("speakerphone" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_speaker"))
                    || ("call_record" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_record"))
                    || ("wireless_headset" == slotName && MainModule.mPrefs.getBoolean("system_statusbaricons_wireless_headset"))
                ) {
                    XposedHelpers.setObjectField(param.getArgs()[1], "visible", false)
                }
            }
        })
    }

    @JvmStatic
    fun BatteryIndicatorHook(lpparam: PackageReadyParam) {
        SystemUIBatteryHooks.BatteryIndicatorHook(lpparam)
    }

    @JvmStatic
    fun TempHideOverlaySystemUIHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.wm.shell.pip.PipTaskOrganizer", lpparam.classLoader, "onTaskAppeared", object : MethodHook() {
            private var isActListened = false
            override fun after(param: AfterHookCallback) {
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                if (!isActListened) {
                    isActListened = true
                    val intentFilter = IntentFilter()
                    intentFilter.addAction("miui.intent.TAKE_SCREENSHOT")
                    val thisObject = param.getThisObject()
                    mContext.registerReceiver(object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val action = intent.action ?: return
                            if (action == "miui.intent.TAKE_SCREENSHOT") {
                                val state = intent.getBooleanExtra("IsFinished", true)
                                val mState = XposedHelpers.getObjectField(thisObject, "mPipTransitionState")
                                val isPip = XposedHelpers.callMethod(mState, "isInPip") as Boolean
                                if (isPip) {
                                    val mSurfaceControlTransactionFactory = XposedHelpers.getObjectField(thisObject, "mSurfaceControlTransactionFactory")
                                    val transaction = XposedHelpers.callMethod(mSurfaceControlTransactionFactory, "getTransaction") as SurfaceControl.Transaction
                                    val mLeash = XposedHelpers.getObjectField(thisObject, "mLeash") as SurfaceControl
                                    transaction.setVisibility(mLeash, state)
                                    transaction.apply()
                                }
                            }
                        }
                    }, intentFilter, Context.RECEIVER_EXPORTED)
                }
            }
        })
    }

    private fun processAlbumArt(context: Context?, bitmap: Bitmap?): Bitmap? {
        if (context == null || bitmap == null) return bitmap
        val rescale = MainModule.mPrefs.getStringAsInt("system_albumartonlock_scale", 1)
        val grayscale = MainModule.mPrefs.getBoolean("system_albumartonlock_gray")
        if (rescale == 1 && !grayscale) return bitmap

        val paint = Paint()
        val transformation = Matrix()
        var width = 0
        var height = 0

        if (grayscale) {
            width = bitmap.width
            height = bitmap.height
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            paint.colorFilter = ColorMatrixColorFilter(matrix)
        }

        if (rescale != 1) {
            val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val point = Point()
            display.getRealSize(point)
            width = point.x
            height = point.y

            val originalWidth = bitmap.width.toFloat()
            val originalHeight = bitmap.height.toFloat()
            val scale = if (rescale == 2) {
                Math.min(width / originalWidth, height / originalHeight)
            } else {
                Math.max(width / originalWidth, height / originalHeight)
            }
            val xTranslation = (width - originalWidth * scale) / 2.0f
            val yTranslation = (height - originalHeight * scale) / 2.0f

            transformation.postTranslate(xTranslation, yTranslation)
            transformation.preScale(scale, scale)
            paint.isFilterBitmap = true
        }

        val processed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(processed)
        canvas.drawBitmap(bitmap, transformation, paint)
        return processed
    }

    @JvmStatic
    fun LockScreenAlbumArtHook(lpparam: PackageReadyParam) {
        val MiuiThemeUtilsClass = XposedHelpers.findClassIfExists("com.android.keyguard.utils.MiuiKeyguardUtils", lpparam.classLoader)

        ModuleHelper.hookAllConstructors("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val isDefaultLockScreenTheme = XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme") as Boolean
                if (isDefaultLockScreenTheme) {
                    val mBlurRatioChangedListener = XposedHelpers.getObjectField(param.getThisObject(), "mBlurRatioChangedListener")
                    val notificationShadeDepthController = XposedHelpers.getObjectField(param.getThisObject(), "mDepthController")
                    val listeners = XposedHelpers.getObjectField(notificationShadeDepthController, "listeners") as ArrayList<Any>
                    listeners.remove(mBlurRatioChangedListener)
                    val view = XposedHelpers.getObjectField(param.getThisObject(), "mThemeBackgroundView") as View
                    view.alpha = 1.0f

                    val intentFilter = IntentFilter()
                    intentFilter.addAction(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART")
                    view.context.registerReceiver(object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val action = intent.action ?: return
                            if (action == GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART") {
                                try {
                                    XposedHelpers.callMethod(param.getThisObject(), "updateThemeBackgroundVisibility")
                                } catch (e: Throwable) {
                                }
                            }
                        }
                    }, intentFilter, Context.RECEIVER_NOT_EXPORTED)
                }
            }
        })
        val screenStates = booleanArrayOf(false) // isAod
        val updateLockscreenHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val isDefaultLockScreenTheme = XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme") as Boolean
                if (!isDefaultLockScreenTheme) {
                    return
                }
                val view = XposedHelpers.getObjectField(param.getThisObject(), "mThemeBackgroundView") as View
                val isOnShade = XposedHelpers.callMethod(param.getThisObject(), "isOnShade") as Boolean
                if (isOnShade || screenStates[0]) {
                    view.visibility = View.GONE
                } else {
                    val mAlbumArt = XposedHelpers.getAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt")
                    if (mAlbumArt != null) {
                        view.background = BitmapDrawable(view.context.resources, mAlbumArt as Bitmap)
                    }
                    view.visibility = if (mAlbumArt != null) View.VISIBLE else View.GONE
                }
                param.returnAndSkip(null)
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "updateThemeBackgroundVisibility", updateLockscreenHook)
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "linkageViewAnim", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val screenOn = param.getArgs()[0] as Boolean
                screenStates[0] = !screenOn
                XposedHelpers.callMethod(param.getThisObject(), "updateThemeBackgroundVisibility")
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "updateMediaMetaData", Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                val isDefaultLockScreenTheme = XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme") as Boolean
                if (!isDefaultLockScreenTheme) {
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null)
                    XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null)
                    return
                }
                val mMediaMetadata = XposedHelpers.getObjectField(param.getThisObject(), "mMediaMetadata") as MediaMetadata?
                var art: Bitmap? = null
                if (mMediaMetadata != null) {
                    art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    if (art == null) art = mMediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
                }
                val mAlbumArt = XposedHelpers.getAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource") as Bitmap?
                try {
                    if (art == null && mAlbumArt == null) return
                    if (art != null && art.sameAs(mAlbumArt)) return
                } catch (ignore: Throwable) {
                }
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", art)

                val blur = MainModule.mPrefs.getInt("system_albumartonlock_blur", 0)
                val blurArt = processAlbumArt(mContext, if (art != null && blur > 0) Helpers.fastBlur(art, blur + 1) else art)
                XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", blurArt)

                val updateAlbumWallpaper = Intent(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART")
                updateAlbumWallpaper.setPackage("com.android.systemui")
                mContext.sendBroadcast(updateAlbumWallpaper)

                if (blurArt != null) {
                    val updateFakeWallpaper = Intent("miui.intent.action.LOCK_WALLPAPER_CHANGED")
                    updateFakeWallpaper.setPackage("com.android.systemui")
                    val fromBitmap = WallpaperColors.fromBitmap(blurArt)
                    val isWallpaperColorLight = (fromBitmap.colorHints and 1) == 1
                    updateFakeWallpaper.putExtra("is_wallpaper_color_light", isWallpaperColorLight)
                    mContext.sendBroadcast(updateFakeWallpaper)
                }
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.NotificationMediaManager", lpparam.classLoader, "dispatchUpdateMediaMetaData", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val isDefaultLockScreenTheme = XposedHelpers.callStaticMethod(MiuiThemeUtilsClass, "isDefaultKeyguardNotTheme") as Boolean
                if (isDefaultLockScreenTheme) {
                    val mMediaController = XposedHelpers.getObjectField(param.getThisObject(), "mMediaController")
                    if (mMediaController == null) {
                        XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArtSource", null)
                        XposedHelpers.setAdditionalStaticField(MiuiThemeUtilsClass, "mAlbumArt", null)
                    }
                    val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                    val updateAlbumWallpaper = Intent(GlobalActions.EVENT_PREFIX + "UPDATE_LS_ALBUM_ART")
                    updateAlbumWallpaper.setPackage("com.android.systemui")
                    mContext.sendBroadcast(updateAlbumWallpaper)
                }
            }
        })
    }

    @JvmStatic
    fun LockScreenShortcutHook(lpparam: PackageReadyParam) {
        val rightActionKey = "system_lockscreenshortcuts_right_action"
        ModuleHelper.findAndHookMethod("com.android.keyguard.injector.KeyguardBottomAreaInjector", lpparam.classLoader, "updateLeftIcon", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val thisObject = param.getThisObject()
                val mLeftButton = XposedHelpers.getObjectField(thisObject, "mLeftButton") as ImageView?
                if (mLeftButton == null) return
                if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val mDarkMode = XposedHelpers.getBooleanField(thisObject, "mBottomIconRectIsDeep")
                    val iconImg = if (mDarkMode) R.drawable.keyguard_bottom_flashlight_img_light else R.drawable.keyguard_bottom_flashlight_img_dark
                    val iconDrawable = ResourcesCompat.getDrawable(ModuleHelper.getModuleRes(mContext), iconImg, mContext.theme)
                    XposedHelpers.callMethod(mLeftButton, "setImageDrawable", iconDrawable, false)
                    val mFlashlightController = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.policy.FlashlightController")
                    val isOn = XposedHelpers.callMethod(mFlashlightController, "isEnabled") as Boolean
                    XposedHelpers.callMethod(mLeftButton, "setCircleRadiusWithoutAnimation", if (isOn) 66f else 0f)
                } else if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off")) {
                    mLeftButton.visibility = View.GONE
                }
            }
        })
        if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")) {
            ModuleHelper.hookAllConstructors("com.android.keyguard.injector.KeyguardBottomAreaInjector", lpparam.classLoader, object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                    val resolver = mContext.contentResolver
                    val oldObserver = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "torchListener") as ContentObserver?
                    if (oldObserver != null) {
                        resolver.unregisterContentObserver(oldObserver)
                    }
                    val torchObserver = object : ContentObserver(Handler()) {
                        override fun onChange(selfChange: Boolean) {
                            if (selfChange) return
                            XposedHelpers.callMethod(param.getThisObject(), "updateLeftIcon")
                        }
                    }
                    resolver.registerContentObserver(Settings.Global.getUriFor("torch_state"), false, torchObserver)
                    XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "torchListener", torchObserver)
                }
            })
        }

        val updateRightButtonHook = object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val thisObject = param.getThisObject()
                val mRightButton = XposedHelpers.getObjectField(thisObject, "mRightButton") as ImageView?
                if (mRightButton == null) return
                if (MainModule.mPrefs.getInt(rightActionKey, 1) > 1) {
                    val mContext = XposedHelpers.getObjectField(thisObject, "mContext") as Context
                    val mDarkMode = XposedHelpers.getBooleanField(thisObject, "mBottomIconRectIsDeep")
                    val iconImg = if (mDarkMode) R.drawable.keyguard_bottom_miuizer_img_dark else R.drawable.keyguard_bottom_miuizer_img_light
                    val iconDrawable = ResourcesCompat.getDrawable(ModuleHelper.getModuleRes(mContext), iconImg, mContext.theme)
                    mRightButton.setImageDrawable(iconDrawable)
                } else if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
                    mRightButton.visibility = View.GONE
                }
            }
        }
        ModuleHelper.findAndHookMethod("com.android.keyguard.injector.KeyguardBottomAreaInjector", lpparam.classLoader, "updateRightIcon", updateRightButtonHook)
        ModuleHelper.findAndHookMethod("com.android.keyguard.injector.KeyguardBottomAreaInjector", lpparam.classLoader, "updateRightAffordanceViewLayoutVisibility", updateRightButtonHook)

        val leftAction = MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_tapaction")
        val rightAction = MainModule.mPrefs.getInt(rightActionKey, 1) > 1

        if (leftAction || rightAction) {
            ModuleHelper.findAndHookMethod("com.android.keyguard.injector.KeyguardBottomAreaInjector", lpparam.classLoader, "updateIcons", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val mLeftButton = XposedHelpers.getObjectField(param.getThisObject(), "mLeftButton") as View?
                    if (mLeftButton == null) {
                        return
                    }
                    if (leftAction) {
                        mLeftButton.setOnLongClickListener { v: View ->
                            val mFlashlightController = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.policy.FlashlightController")
                            val z = !(XposedHelpers.callMethod(mFlashlightController, "isEnabled") as Boolean)
                            XposedHelpers.callMethod(mFlashlightController, "setFlashlight", z)
                            true
                        }
                        mLeftButton.setOnClickListener(null)
                    }
                    if (rightAction) {
                        val mRightButton = XposedHelpers.getObjectField(param.getThisObject(), "mRightButton") as View
                        mRightButton.setOnLongClickListener { v: View ->
                            GlobalActions.handleAction(v.context, "system_lockscreenshortcuts_right", true)
                            true
                        }
                        mRightButton.setOnClickListener(null)
                    }
                }
            })
        }

        ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.classLoader, "setTranslation", Float::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mCurrentScreen = XposedHelpers.getIntField(param.getThisObject(), "mCurrentScreen")
                if (mCurrentScreen != 1) return
                if ((param.getArgs()[0] as Float) < 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off"))
                    param.getArgs()[0] = 0.0f
                else if ((param.getArgs()[0] as Float) > 0 && MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_left_off"))
                    param.getArgs()[0] = 0.0f
            }
        })

        if (MainModule.mPrefs.getBoolean("system_lockscreenshortcuts_right_off")) {
            ModuleHelper.findAndHookMethod("com.android.keyguard.KeyguardMoveHelper", lpparam.classLoader, "endMotion", Float::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!, object : MethodHook() {
                override fun before(param: BeforeHookCallback) {
                    val mCurrentScreen = XposedHelpers.getIntField(param.getThisObject(), "mCurrentScreen")
                    if (mCurrentScreen != 1) return
                    val mTranslation = XposedHelpers.getFloatField(param.getThisObject(), "mTranslation")
                    val velocityTracker = XposedHelpers.getObjectField(param.getThisObject(), "mVelocityTracker") as VelocityTracker?
                    val xVelocity: Float = if (velocityTracker == null) {
                        0.0f
                    } else {
                        velocityTracker.computeCurrentVelocity(1000)
                        velocityTracker.xVelocity
                    }
                    if (xVelocity * mTranslation < 0.01f) {
                        param.returnAndSkip(null)
                    }
                }
            })
            ModuleHelper.hookAllMethods("com.android.keyguard.KeyguardMoveRightController", lpparam.classLoader, "onTouchDown", object : MethodHook() {
                override fun before(param: BeforeHookCallback) {
                    param.returnAndSkip(null)
                }
            })
            ModuleHelper.hookAllMethods("com.android.keyguard.KeyguardMoveRightController", lpparam.classLoader, "onTouchMove", object : MethodHook() {
                override fun before(param: BeforeHookCallback) {
                    param.returnAndSkip(true)
                }
            })
        }
    }

    @JvmStatic
    fun LockScreenSecureLaunchHook() {
        ModuleHelper.findAndHookMethod(Activity::class.java, "onCreate", Bundle::class.java, object : MethodHook() {
            @Suppress("ConstantConditions")
            override fun after(param: AfterHookCallback) {
                val act = param.getThisObject() as Activity
                if (act == null) return
                val intent = act.intent
                if (intent == null) return
                val mFromSecureKeyguard = intent.getBooleanExtra("StartActivityWhenLocked", false)
                var mStartedFromLockScreen = false
                try {
                    mStartedFromLockScreen = XposedHelpers.getAdditionalInstanceField(act.application, "wasStartedFromLockScreen") as Boolean
                } catch (ignore: Throwable) {
                }
                if (mFromSecureKeyguard || mStartedFromLockScreen) {
                    XposedHelpers.setAdditionalInstanceField(act.application, "wasStartedFromLockScreen", true)
                    act.setShowWhenLocked(true)
                    act.setInheritShowWhenLocked(true)
                }
            }
        })
    }

    @JvmStatic
    fun SecureQSTilesHook(lpparam: PackageReadyParam) {
        val clickHook = object : MethodHook(XposedInterface.PRIORITY_HIGHEST) {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getObjectField(param.getThisObject(), "mTileSpec") as String
                var name = tileName
                if (name.startsWith("intent(")) name = "intent"
                else if (name.startsWith("custom(")) name = "custom"
                val secureTitles = HashSet<String>()
                if (MainModule.mPrefs.getBoolean("system_secureqs_wifi")) secureTitles.add("wifi")
                if (MainModule.mPrefs.getBoolean("system_secureqs_bt")) secureTitles.add("bt")
                if (MainModule.mPrefs.getBoolean("system_secureqs_mobiledata")) secureTitles.add("cell")
                if (MainModule.mPrefs.getBoolean("system_secureqs_airplane")) secureTitles.add("airplane")
                if (MainModule.mPrefs.getBoolean("system_secureqs_location")) secureTitles.add("gps")
                if (MainModule.mPrefs.getBoolean("system_secureqs_hotspot")) secureTitles.add("hotspot")
                if (MainModule.mPrefs.getBoolean("system_secureqs_nfc")) secureTitles.add("nfc")
                if (MainModule.mPrefs.getBoolean("system_secureqs_sync")) secureTitles.add("sync")
                if (MainModule.mPrefs.getBoolean("system_secureqs_custom")) {
                    secureTitles.add("intent")
                    secureTitles.add("custom")
                }
                if (secureTitles.contains(name)) {
                    val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                    val kgMgr = mContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    if (!kgMgr.isKeyguardLocked || !kgMgr.isKeyguardSecure) return
                    val activityStater = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.plugins.ActivityStarter")
                    XposedHelpers.callMethod(activityStater, "postQSRunnableDismissingKeyguard", true, Runnable {
                        val keepOpened = MainModule.mPrefs.getBoolean("system_secureqs_keepopened")
                        if (keepOpened) {
                            val handler = Handler(mContext.mainLooper)
                            handler.postDelayed({
                                val openCCIntent = Intent(GlobalActions.ACTION_PREFIX + "ExpandSettings")
                                openCCIntent.setPackage("com.android.systemui")
                                mContext.sendBroadcast(openCCIntent)
                            }, 800)
                        }
                    })
                    val mStatusBar = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.CommandQueue")
                    XposedHelpers.callMethod(mStatusBar, "animateCollapsePanels", 0, false)
                    param.returnAndSkip(null)
                }
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader, "click", View::class.java, clickHook)
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader, "longClick", View::class.java, clickHook)
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader, "secondaryClick", View::class.java, clickHook)
    }

    @JvmStatic
    fun ExtendedPowerMenuHook(lpparam: PackageReadyParam) {
        val fastbootTitleId = MainModule.resHooks.addFakeResource("epm_fastboot_title", R.string.system_epm_action_fastboot_title, "string")
        val recoveryTitleId = MainModule.resHooks.addFakeResource("epm_recovery_title", R.string.system_epm_action_recovery_title, "string")

        var actionId = -1
        val DialogClass = XposedHelpers.findClass("com.android.systemui.globalactions.GlobalActionsDialogLite", lpparam.classLoader)
        ModuleHelper.findAndHookConstructor("com.android.systemui.globalactions.GlobalActionsDialogLite\$SinglePressAction", lpparam.classLoader, DialogClass, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                if (actionId == 1) {
                    param.getArgs()[2] = fastbootTitleId
                } else if (actionId == 2) {
                    param.getArgs()[2] = recoveryTitleId
                }
            }
            override fun after(param: AfterHookCallback) {
                actionId = -1
            }
        })
        val PowerActionClass = XposedHelpers.findClass("com.android.systemui.globalactions.GlobalActionsDialogLite\$PowerOptionsAction", lpparam.classLoader)
        ModuleHelper.findAndHookMethod("com.android.systemui.globalactions.GlobalActionsDialogLite", lpparam.classLoader, "createActionItems", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mItems = XposedHelpers.getObjectField(param.getThisObject(), "mItems") as ArrayList<Any>
                actionId = 1
                val fastbootAction = XposedHelpers.newInstance(PowerActionClass, param.getThisObject())
                actionId = 2
                val recoveryAction = XposedHelpers.newInstance(PowerActionClass, param.getThisObject())
                mItems.add(fastbootAction)
                mItems.add(recoveryAction)
            }
        })

        ModuleHelper.findAndHookMethod(PowerActionClass, "onPress", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mMessageResId = XposedHelpers.getIntField(param.getThisObject(), "mMessageResId")
                if (mMessageResId == fastbootTitleId || mMessageResId == recoveryTitleId) {
                    val actionsDialog = XposedHelpers.getSurroundingThis(param.getThisObject())
                    val mContext = XposedHelpers.getObjectField(actionsDialog, "mContext") as Context
                    val modRes = ModuleHelper.getModuleRes(mContext)
                    val SystemUIDialogClass = XposedHelpers.findClass("com.android.systemui.statusbar.phone.SystemUIDialog", lpparam.classLoader)
                    val confirmDlg = XposedHelpers.newInstance(SystemUIDialogClass, mContext) as AlertDialog
                    confirmDlg.setTitle(
                        modRes.getString(
                            if (mMessageResId == recoveryTitleId) R.string.system_epm_action_recovery_confirm_title else R.string.system_epm_action_fastboot_confirm_title
                        )
                    )
                    confirmDlg.setButton(-1, Resources.getSystem().getString(android.R.string.ok), DialogInterface.OnClickListener { _, _ ->
                        val pm = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                        val mService = XposedHelpers.getObjectField(pm, "mService")
                        if (mMessageResId == recoveryTitleId) {
                            XposedHelpers.callMethod(mService, "reboot", false, "recovery", false)
                        } else {
                            XposedHelpers.callMethod(mService, "reboot", false, "bootloader", false)
                        }
                    })
                    confirmDlg.setButton(-2, Resources.getSystem().getString(android.R.string.cancel), DialogInterface.OnClickListener { _, _ -> })
                    confirmDlg.show()
                    param.returnAndSkip(null)
                }
            }
        })

        ModuleHelper.findAndHookMethod("com.android.systemui.plugins.PluginEnablerImpl", lpparam.classLoader, "isEnabled", ComponentName::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val componentName = param.getArgs()[0] as ComponentName
                if (componentName.className.contains("GlobalActions")) {
                    param.returnAndSkip(false)
                }
            }
        })
    }

    @JvmStatic
    fun HideDismissViewHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.shade.MiuiNotificationPanelViewController", lpparam.classLoader, "updateDismissView", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mDismissView = XposedHelpers.getObjectField(param.getThisObject(), "mDismissView") as View?
                if (mDismissView != null) {
                    mDismissView.visibility = View.GONE
                    param.returnAndSkip(null)
                }
            }
        })
    }

    @JvmStatic
    fun HideNoficationAccessIconHook(lpparam: PackageReadyParam) {
        val hideViewHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mShortCut = XposedHelpers.getObjectField(param.getThisObject(), "mShortCut") as View?
                if (mShortCut != null) {
                    mShortCut.visibility = View.GONE
                    param.returnAndSkip(null)
                }
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiQSHeaderView", lpparam.classLoader, "updateShortCutVisibility", hideViewHook)
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.MiuiNotificationHeaderView", lpparam.classLoader, "updateShortCutVisibility", hideViewHook)
    }

    @JvmStatic
    fun HideNoNotificationsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout", lpparam.classLoader, "updateEmptyShadeView", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                param.getArgs()[1] = 0
                param.getArgs()[2] = 0
                val mEmptyShadeView = XposedHelpers.getObjectField(param.getThisObject(), "mEmptyShadeView") as View
                mEmptyShadeView.setOnClickListener(null)
                XposedHelpers.callMethod(mEmptyShadeView, "setVisible", false, false)
                param.returnAndSkip(null)
            }
        })
    }

    @JvmStatic
    fun ReplaceShortcutAppHook(lpparam: PackageReadyParam) {
        val openAppHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mContext = ModuleHelper.findContext(lpparam)
                var user = 0
                var pkgAppName = ""
                when (param.getMember().name) {
                    "startCalendarApp" -> {
                        user = MainModule.mPrefs.getInt("system_calendar_app_user", 0)
                        pkgAppName = MainModule.mPrefs.getString("system_calendar_app", "")
                    }
                    "startClockApp" -> {
                        user = MainModule.mPrefs.getInt("system_clock_app_user", 0)
                        pkgAppName = MainModule.mPrefs.getString("system_clock_app", "")
                    }
                    "startSettingsApp" -> {
                        user = MainModule.mPrefs.getInt("system_shortcut_app_user", 0)
                        pkgAppName = MainModule.mPrefs.getString("system_shortcut_app", "")
                    }
                }
                if (pkgAppName.isNotEmpty()) {
                    val pkgAppArray = pkgAppName.split("\\|".toRegex())
                    if (pkgAppArray.size < 2) return

                    val name = ComponentName(pkgAppArray[0], pkgAppArray[1])
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    intent.component = name
                    if (user != 0) {
                        try {
                            val mStatusBar = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.phone.CentralSurfaces")
                            XposedHelpers.callMethod(mStatusBar, "collapsePanels")
                            XposedHelpers.callMethod(mContext, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle::class.java, user))
                        } catch (t: Throwable) {
                            XposedHelpers.log(t)
                        }
                    } else {
                        val activityStarter = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.plugins.ActivityStarter")
                        XposedHelpers.callMethod(activityStarter, "startActivity", intent, true)
                    }
                    param.returnAndSkip(null)
                }
            }
        }
        if (MainModule.mPrefs.getString("system_shortcut_app", "").isNotEmpty()) {
            ModuleHelper.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startSettingsApp", openAppHook)
        }
        if (MainModule.mPrefs.getString("system_calendar_app", "").isNotEmpty()) {
            ModuleHelper.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startCalendarApp", Context::class.java, openAppHook)
        }
        if (MainModule.mPrefs.getString("system_clock_app", "").isNotEmpty()) {
            ModuleHelper.findAndHookMethod("com.miui.systemui.util.CommonUtil", lpparam.classLoader, "startClockApp", openAppHook)
        }
    }

    @JvmStatic
    fun StatusBarStyleBatteryIconHook(lpparam: PackageReadyParam) {
        SystemUIBatteryHooks.StatusBarStyleBatteryIconHook(lpparam)
    }

    @JvmStatic
    fun ForceClockUseSystemFontsHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.clock.MiuiBaseClock", lpparam.classLoader, "updateViewsTextSize", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mTimeText = XposedHelpers.getObjectField(param.getThisObject(), "mTimeText") as TextView
                mTimeText.typeface = Typeface.DEFAULT
            }
        })
        ModuleHelper.findAndHookMethod("com.miui.clock.MiuiLeftTopLargeClock", lpparam.classLoader, "onLanguageChanged", String::class.java, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mTimeText = XposedHelpers.getObjectField(param.getThisObject(), "mCurrentDateLarge") as TextView
                mTimeText.typeface = Typeface.DEFAULT
            }
        })
    }

    @JvmStatic
    fun HideStatusBarWhenCaptureHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment", lpparam.classLoader, "onViewCreated", View::class.java, Bundle::class.java, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val view = param.getArgs()[0] as View
                view.context.registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (intent.action == "miui.intent.TAKE_SCREENSHOT") {
                            val finished = intent.getBooleanExtra("IsFinished", true)
                            view.visibility = if (finished) View.VISIBLE else View.INVISIBLE
                        }
                    }
                }, IntentFilter("miui.intent.TAKE_SCREENSHOT"), Context.RECEIVER_EXPORTED)
            }
        })
    }

    @JvmStatic
    fun HideNavBarBeforeScreenshotHook(lpparam: PackageReadyParam) {
        val hideNavHook = object : MethodHook() {
            var visibleState = 0
            override fun after(param: AfterHookCallback) {
                val view = XposedHelpers.getObjectField(param.getThisObject(), "mView") as View
                view.context.registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        if (intent.action == "miui.intent.TAKE_SCREENSHOT") {
                            val finished = intent.getBooleanExtra("IsFinished", true)
                            if (!finished) {
                                visibleState = view.visibility
                            }
                            view.visibility = if (finished) visibleState else View.INVISIBLE
                        }
                    }
                }, IntentFilter("miui.intent.TAKE_SCREENSHOT"), Context.RECEIVER_EXPORTED)
            }
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.navigationbar.NavigationBar", lpparam.classLoader, "onInit", hideNavHook)
    }

    private var clickNotifyOptions: Bundle? = null

    @JvmStatic
    fun OpenNotifyInFloatingWindowHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods(PendingIntent::class.java, "sendAndReturnResult", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                if (param.getArgs().size != 7) return
                if (clickNotifyOptions != null) {
                    param.getArgs()[6] = clickNotifyOptions
                }
            }
            override fun after(param: AfterHookCallback) {
                if (param.getArgs().size != 7) return
                clickNotifyOptions = null
            }
        })
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter", lpparam.classLoader, "onNotificationClicked", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val notificationEntry = param.getArgs()[0]
                val mSbn = XposedHelpers.getObjectField(notificationEntry, "mSbn")
                val notify = XposedHelpers.callMethod(mSbn, "getNotification") as Notification
                val pendingIntent = notify.contentIntent ?: return
                val mKeyguardStateController = XposedHelpers.getObjectField(param.getThisObject(), "mKeyguardStateController")
                if (XposedHelpers.getBooleanField(mKeyguardStateController, "mShowing")) return

                val opPkg = XposedHelpers.callMethod(mSbn, "getOpPkg") as String?
                val mPkgName = XposedHelpers.callMethod(mSbn, "getPackageName") as String?
                val isSubstituteNotification = !TextUtils.equals(mPkgName, opPkg)
                val pkgName = (if (isSubstituteNotification) mPkgName else pendingIntent.creatorPackage) ?: return

                val foregroundInfo = ProcessManager.getForegroundInfo()
                if (foregroundInfo != null) {
                    val topPackage = foregroundInfo.mForegroundPackageName
                    if (pkgName == topPackage || "com.miui.home" == topPackage) {
                        return
                    }
                }
                val whitelist = MainModule.mPrefs.getBoolean("system_notify_openinfw_in_whitelist")
                val appInList = MainModule.mPrefs.getStringSet("system_notify_openinfw_apps").contains(pkgName)
                if (whitelist xor appInList) {
                    return
                }
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                clickNotifyOptions = ModuleHelper.getFreeformOptions(mContext, pkgName, pendingIntent, true)
            }
        })
    }

    @SuppressLint("StaticFieldLeak")
    private var mPct: TextView? = null

    private fun initPct(container: ViewGroup, source: Int, context: Context) {
        val res = context.resources
        if (mPct == null) {
            mPct = TextView(container.context)
            mPct!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            mPct!!.gravity = Gravity.CENTER
            val density = res.displayMetrics.density
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = Math.round(MainModule.mPrefs.getInt("system_showpct_top", 28) * density)
            lp.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            mPct!!.setPadding(Math.round(20 * density), Math.round(10 * density), Math.round(18 * density), Math.round(12 * density))
            mPct!!.layoutParams = lp
            try {
                val modRes = ModuleHelper.getModuleRes(context)
                mPct!!.setTextColor(modRes.getColor(R.color.color_on_surface_variant, context.theme))
                mPct!!.background = ResourcesCompat.getDrawable(modRes, R.drawable.input_background, context.theme)
            } catch (err: Throwable) {
                XposedHelpers.log(err)
            }
            container.addView(mPct)
        }
        mPct!!.setTag(source)
        mPct!!.visibility = View.GONE
    }

    private fun removePct(mPctText: TextView?) {
        if (mPctText != null) {
            mPctText.visibility = View.GONE
            val p = mPctText.parent as ViewGroup
            p.removeView(mPctText)
            mPct = null
        }
    }

    private fun startShowPct(lpparam: PackageReadyParam, mContext: Context) {
        val controlCenter = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.controlcenter.phone.ControlPanelWindowManager")
        val controlCenterWindowView = XposedHelpers.getObjectField(controlCenter, "windowView")
        val windowView = XposedHelpers.callMethod(controlCenterWindowView, "getView") as ViewGroup
        initPct(windowView, 2, mContext)
        mPct!!.visibility = View.VISIBLE
    }

    @JvmStatic
    fun BrightnessPctHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.classLoader, "onStart", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                startShowPct(lpparam, mContext)
            }
        })

        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.classLoader, "setToggleSliderBase", object : MethodHook() {
            private var inited = false
            override fun after(param: AfterHookCallback) {
                if (!inited && param.getArgs()[0] != null) {
                    inited = true
                    val className = param.getArgs()[0]!!.javaClass.simpleName
                    if ("ToggleSliderViewHolder" == className) return
                    val brightnessSeekBar = param.getArgs()[0]
                    val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                    val mOnSeekBarChangeListener = XposedHelpers.getObjectField(brightnessSeekBar, "mOnSeekBarChangeListener") ?: return
                    ModuleHelper.findAndHookMethod(mOnSeekBarChangeListener.javaClass, "onStartTrackingTouch", SeekBar::class.java, object : MethodHook() {
                        override fun before(param: BeforeHookCallback) {
                            val thisObject = XposedHelpers.getSurroundingThis(param.getThisObject())
                            if (brightnessSeekBar != thisObject) return
                            startShowPct(lpparam, mContext)
                        }
                    })
                }
            }
        })

        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.classLoader, "onStop", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                removePct(mPct)
            }
        })

        val BrightnessUtils = XposedHelpers.findClassIfExists("com.android.systemui.controlcenter.policy.BrightnessUtils", lpparam.classLoader)
        ModuleHelper.hookAllMethods("com.android.systemui.controlcenter.policy.MiuiBrightnessController", lpparam.classLoader, "onChanged", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val pctTag = mPct?.getTag() as? Int ?: 0
                if (pctTag == 0 || mPct == null) return
                val currentLevel = param.getArgs()[3] as Int
                if (BrightnessUtils != null) {
                    val maxLevel = XposedHelpers.getStaticObjectField(BrightnessUtils, "GAMMA_SPACE_MAX") as Int
                    mPct!!.text = ((currentLevel * 100) / maxLevel).toString() + "%"
                }
            }
        })
    }

    @JvmStatic
    fun ShowVolumePctHook(pluginLoader: ClassLoader) {
        val MiuiVolumeDialogImpl = XposedHelpers.findClassIfExists("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", pluginLoader)
        ModuleHelper.findAndHookMethod(MiuiVolumeDialogImpl, "showVolumeDialogH", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mDialogView = XposedHelpers.getObjectField(param.getThisObject(), "mDialogView") as View
                val windowView = mDialogView.parent as FrameLayout
                initPct(windowView, 3, windowView.context)
            }
        })

        ModuleHelper.findAndHookMethod(MiuiVolumeDialogImpl, "dismissH", Int::class.javaPrimitiveType!!, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                removePct(mPct)
            }
        })

        ModuleHelper.hookAllMethods("com.android.systemui.miui.volume.MiuiVolumeDialogImpl\$VolumeSeekBarChangeListener", pluginLoader, "onProgressChanged", object : MethodHook() {
            private var nowLevel = -233
            override fun after(param: AfterHookCallback) {
                if (nowLevel == param.getArgs()[1] as Int) return
                val pctTag = mPct?.getTag() as? Int ?: 0
                if (pctTag != 3 || mPct == null) return
                val mColumn = XposedHelpers.getObjectField(param.getThisObject(), "mColumn")
                val ss = XposedHelpers.getObjectField(mColumn, "ss")
                if (ss == null) return
                if (XposedHelpers.getIntField(mColumn, "stream") == 10) return

                val fromUser = param.getArgs()[2] as Boolean
                val currentLevel: Int = if (fromUser) {
                    param.getArgs()[1] as Int
                } else {
                    val anim = XposedHelpers.getObjectField(mColumn, "anim") as ObjectAnimator?
                    if (anim == null || !anim.isRunning) return
                    XposedHelpers.getIntField(mColumn, "animTargetProgress")
                }
                nowLevel = currentLevel
                mPct!!.visibility = View.VISIBLE
                val levelMin = XposedHelpers.getIntField(ss, "levelMin")
                var adjustedLevel = currentLevel
                if (levelMin > 0 && adjustedLevel < levelMin * 1000) {
                    adjustedLevel = levelMin * 1000
                }
                val seekBar = param.getArgs()[0] as SeekBar
                val max = seekBar.max
                val maxLevel = max / 1000
                if (adjustedLevel != 0) {
                    val i3 = maxLevel - 1
                    adjustedLevel = if (adjustedLevel == max) maxLevel else (adjustedLevel * i3 / max) + 1
                }
                mPct!!.text = ((adjustedLevel * 100) / maxLevel).toString() + "%"
            }
        })
    }

    @JvmStatic
    fun NotificationImportanceHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader, "updateStatusBarIcons", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mNotificationEntries = XposedHelpers.getObjectField(param.getThisObject(), "mNotificationEntries") as? List<Any> ?: return
                if (mNotificationEntries.isNotEmpty()) {
                    val arrayList = ArrayList<Any>()
                    for (item in mNotificationEntries) {
                        val notifyEntry = XposedHelpers.callMethod(item, "getRepresentativeEntry")
                        val importance = XposedHelpers.callMethod(notifyEntry, "getImportance") as Int
                        if (importance > 1) {
                            arrayList.add(item)
                        }
                    }
                    if (arrayList.size != mNotificationEntries.size) {
                        XposedHelpers.setObjectField(param.getThisObject(), "mNotificationEntries", arrayList)
                    }
                }
            }
        })
    }

    @JvmStatic
    fun RemovePackageNotificationsLimitHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.collection.coordinator.CountLimitCoordinator", lpparam.classLoader, "attach", HookerClassHelper.DO_NOTHING)
    }

    @JvmStatic
    fun DisableFoldNotificationsHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.collection.coordinator.FoldCoordinator", lpparam.classLoader, "attach", HookerClassHelper.DO_NOTHING)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.NotificationUtil", lpparam.classLoader, "shouldSuppressFold", HookerClassHelper.returnConstant(true))
    }

    @JvmStatic
    fun DisableStrongToastHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllMethods("com.android.systemui.toast.MIUIStrongToastControl", lpparam.classLoader, "showCustomStrongToast", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                var blockToast = MainModule.mPrefs.getBoolean("system_notif_disable_strong_toast_always", true)
                if (!blockToast) {
                    val dnd = MainModule.mPrefs.getBoolean("system_notif_disable_strong_toast_dnd", false)
                    if (dnd) {
                        val zenModeController = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.systemui.statusbar.policy.ZenModeController")
                        blockToast = XposedHelpers.callMethod(zenModeController, "isZenModeOn") as Boolean
                    }
                }
                if (blockToast) {
                    param.returnAndSkip(null)
                }
            }
        })
    }

    @JvmStatic
    fun TweakStrongToastHook(lpparam: PackageReadyParam) {
        val toastWidth = MainModule.mPrefs.getInt("system_notif_strong_toast_width", 100)
        if (toastWidth < 100) {
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "strong_toast_width_window", Math.ceil(3.37 * toastWidth))
            MainModule.resHooks.setThemeValueReplacement("com.android.systemui", "dimen", "strong_toast_width", Math.ceil(3.2 * toastWidth))
            ModuleHelper.hookAllMethods("com.android.systemui.toast.MIUIStrongToast", lpparam.classLoader, "showCustomStrongToast", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val mStrongToastBottomView = XposedHelpers.getObjectField(param.getThisObject(), "mStrongToastBottomView") as View
                    mStrongToastBottomView.visibility = View.GONE
                    val mRLLeft = XposedHelpers.getObjectField(param.getThisObject(), "mRLLeft") as RelativeLayout
                    val layoutParams = mRLLeft.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.leftMargin = 0
                    mRLLeft.layoutParams = layoutParams
                }
            })
            ModuleHelper.findAndHookMethod("com.android.systemui.toast.MIUIStrongToast", lpparam.classLoader, "getWindowParam", object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val lp = param.getResult() as WindowManager.LayoutParams
                    lp.width = Helpers.dp2px(3.2f * toastWidth).toInt()
                    param.setResult(lp)
                }
            })
        }
    }

    @JvmStatic
    fun HideSafeVolumeDlgHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.volume.VolumeUI", lpparam.classLoader, "start", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val volumeDialogComponent = XposedHelpers.getObjectField(param.getThisObject(), "mVolumeComponent")
                val volumeDialogControllerImpl = XposedHelpers.getObjectField(volumeDialogComponent, "mController")
                XposedHelpers.setObjectField(volumeDialogControllerImpl, "mShowSafetyWarning", false)
                val audioManager = XposedHelpers.getObjectField(volumeDialogControllerImpl, "mAudio")
                XposedHelpers.callMethod(audioManager, "disableSafeMediaVolume")
            }
        })
    }

    @JvmStatic
    fun DisableHeadsUpWhenMuteHook(lpparam: PackageReadyParam) {
        var mMuteVisible = false
        val disableHeadsUpHook = object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                if (mMuteVisible) {
                    param.returnAndSkip(false)
                }
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl", lpparam.classLoader, "canAlertAwakeCommon", disableHeadsUpHook)
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy", lpparam.classLoader, "updateVolumeZen", object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                mMuteVisible = XposedHelpers.getBooleanField(param.getThisObject(), "mMuteVisible")
            }
        })
    }

    @JvmStatic
    fun DisableKeyguardEditorHook(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.android.keyguard.KeyguardEditorHelper", lpparam.classLoader, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mMiuiKeyguardUpdateMonitorCallback = XposedHelpers.getObjectField(param.getThisObject(), "mMiuiKeyguardUpdateMonitorCallback")
                val keyguardUpdateMonitorInjector = ModuleHelper.getDepInstance(lpparam.classLoader, "com.android.keyguard.injector.KeyguardUpdateMonitorInjector")
                XposedHelpers.callMethod(keyguardUpdateMonitorInjector, "removeCallback", mMiuiKeyguardUpdateMonitorCallback)
                XposedHelpers.setObjectField(param.getThisObject(), "mIsMagazinePreViewVisibility", true)
            }
        })
    }

    @JvmStatic
    fun HideLockscreenZenModeHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.notification.zen.ZenModeViewController", lpparam.classLoader, "updateVisibility", object : MethodHook() {
            private var manuallyDismissed = false
            override fun before(param: BeforeHookCallback) {
                manuallyDismissed = XposedHelpers.getBooleanField(param.getThisObject(), "manuallyDismissed")
                XposedHelpers.setObjectField(param.getThisObject(), "manuallyDismissed", true)
            }
            override fun after(param: AfterHookCallback) {
                XposedHelpers.setObjectField(param.getThisObject(), "manuallyDismissed", manuallyDismissed)
            }
        })
    }

    @JvmStatic
    fun LongClickTileOpenInFreeFormHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader, "handleLongClick", View::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val longClickIntent = XposedHelpers.callMethod(param.getThisObject(), "getLongClickIntent") as Intent?
                if (longClickIntent != null) {
                    val action = longClickIntent.action
                    val isSettings = action?.startsWith("android.settings") == true
                    if (!isSettings && longClickIntent.component != null) {
                        val foregroundInfo = ProcessManager.getForegroundInfo()
                        if (foregroundInfo != null) {
                            val topPackage = foregroundInfo.mForegroundPackageName
                            if ("com.miui.home" == topPackage) {
                                return
                            }
                        }
                        val bIntent = Intent(GlobalActions.ACTION_PREFIX + "SetFreeFormPackage")
                        bIntent.putExtra("package", longClickIntent.component!!.packageName)
                        bIntent.setPackage("android")
                        val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                        mContext.sendBroadcast(bIntent)
                    }
                }
            }
        })
    }

    @JvmStatic
    fun CollapseCCAfterClickHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileImpl", lpparam.classLoader, "click", View::class.java, object : MethodHook() {
            override fun after(param: AfterHookCallback) {
                val mState = XposedHelpers.callMethod(param.getThisObject(), "getState")
                val state = XposedHelpers.getIntField(mState, "state")
                if (state != 0) {
                    val tileSpec = XposedHelpers.callMethod(param.getThisObject(), "getTileSpec") as String
                    if (tileSpec != "edit") {
                        val mHost = XposedHelpers.getObjectField(param.getThisObject(), "mHost")
                        XposedHelpers.callMethod(mHost, "collapsePanels")
                    }
                }
            }
        })
    }

    @JvmStatic
    fun SwitchCCAndNotificationHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.phone.MiuiPhoneStatusBarView", lpparam.classLoader, "handleEvent", MotionEvent::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val mPanelController = XposedHelpers.getObjectField(param.getThisObject(), "mPanelController")
                val useCC = XposedHelpers.callMethod(mPanelController, "isExpandable") as Boolean
                if (useCC) {
                    val bar = param.getThisObject() as FrameLayout
                    val mControlPanelWindowManager = XposedHelpers.getObjectField(param.getThisObject(), "mControlPanelWindowManager")
                    val dispatchToControlPanel = XposedHelpers.callMethod(mControlPanelWindowManager, "dispatchToControlPanel", param.getArgs()[0], bar.width) as Boolean
                    XposedHelpers.callMethod(mControlPanelWindowManager, "setTransToControlPanel", dispatchToControlPanel)
                    param.returnAndSkip(dispatchToControlPanel)
                    return
                }
                param.returnAndSkip(false)
            }
        })
        ModuleHelper.findAndHookMethod("com.android.systemui.controlcenter.phone.ControlPanelWindowManager", lpparam.classLoader, "dispatchToControlPanel", MotionEvent::class.java, Float::class.javaPrimitiveType!!, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val added = XposedHelpers.getBooleanField(param.getThisObject(), "added")
                if (added) {
                    val controlCenterController = XposedHelpers.getObjectField(param.getThisObject(), "controlCenterController")
                    val useCC = XposedHelpers.getBooleanField(controlCenterController, "useControlCenter")
                    if (useCC) {
                        val motionEvent = param.getArgs()[0] as MotionEvent
                        if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                            XposedHelpers.setObjectField(param.getThisObject(), "mDownX", motionEvent.rawX)
                        }
                        val controlCenterWindowView = XposedHelpers.getObjectField(param.getThisObject(), "windowView")
                        if (controlCenterWindowView == null) {
                            param.returnAndSkip(false)
                        } else {
                            val mDownX = XposedHelpers.getFloatField(param.getThisObject(), "downX")
                            val width = param.getArgs()[1] as Float
                            if (mDownX < width / 2.0f) {
                                param.returnAndSkip(XposedHelpers.callMethod(controlCenterWindowView, "handleMotionEvent", motionEvent, true))
                            } else {
                                param.returnAndSkip(false)
                            }
                        }
                        return
                    }
                }
                param.returnAndSkip(false)
            }
        })
    }

    @JvmStatic
    fun HideMobileNetworkIndicatorHook(lpparam: PackageReadyParam) {
        val singleMobileType = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_single")
        val showOnWifi = MainModule.mPrefs.getBoolean("system_statusbar_mobiletype_show_wificonnected")
        val hideMobileActivity = object : MethodHook() {
            private var initAction = false
            override fun before(param: BeforeHookCallback) {
                if ("updateState" == param.getMember().name) {
                    return
                }
                val mState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                initAction = mState == null
            }
            override fun after(param: AfterHookCallback) {
                val updateStateMethod = "updateState" == param.getMember().name
                if (updateStateMethod || initAction) {
                    val opt = MainModule.mPrefs.getStringAsInt("system_mobiletypeicon", 1)
                    val hideIndicator = MainModule.mPrefs.getBoolean("system_networkindicator_mobile")
                    val mMobileType = XposedHelpers.getObjectField(param.getThisObject(), "mMobileType") as View
                    val dataConnected = XposedHelpers.getBooleanField(param.getArgs()[0], "dataConnected")
                    val wifiAvailable = XposedHelpers.getObjectField(param.getArgs()[0], "wifiAvailable") as Boolean
                    if (opt == 3) {
                        if (singleMobileType) {
                            val mMobileTypeSingle = XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle") as TextView
                            mMobileTypeSingle.visibility = View.GONE
                        } else {
                            mMobileType.visibility = View.GONE
                        }
                    } else if (opt == 1) {
                        val viz = if (dataConnected && (!wifiAvailable || showOnWifi)) View.VISIBLE else View.GONE
                        if (singleMobileType) {
                            val mMobileTypeSingle = XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle") as TextView
                            mMobileTypeSingle.visibility = viz
                        } else {
                            mMobileType.visibility = viz
                        }
                    } else if (opt == 2) {
                        val viz = if (!wifiAvailable || showOnWifi) View.VISIBLE else View.GONE
                        if (singleMobileType) {
                            val mMobileTypeSingle = XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle") as TextView
                            mMobileTypeSingle.visibility = viz
                        } else {
                            mMobileType.visibility = viz
                        }
                    }
                    val mLeftInOut = XposedHelpers.getObjectField(param.getThisObject(), "mLeftInOut") as View
                    if (hideIndicator) {
                        val mRightInOut = XposedHelpers.getObjectField(param.getThisObject(), "mRightInOut") as View
                        mLeftInOut.visibility = View.GONE
                        mRightInOut.visibility = View.GONE
                    }
                    if (wifiAvailable && showOnWifi && (dataConnected || opt == 2)) {
                        if (!Build.IS_INTERNATIONAL_BUILD) {
                            val mSmallHd = XposedHelpers.getObjectField(param.getThisObject(), "mSmallHd") as View
                            mSmallHd.visibility = View.GONE
                        }
                        if (opt != 2) {
                            val viz = View.VISIBLE
                            if (singleMobileType) {
                                val mMobileTypeSingle = XposedHelpers.getObjectField(param.getThisObject(), "mMobileTypeSingle") as TextView
                                mMobileTypeSingle.visibility = viz
                            } else {
                                mMobileType.visibility = viz
                            }
                        }
                    }
                    if (!singleMobileType) {
                        val mMobileLeftContainer = XposedHelpers.getObjectField(param.getThisObject(), "mMobileLeftContainer") as View
                        mMobileLeftContainer.visibility = if (mMobileType.visibility == View.GONE && mLeftInOut.visibility == View.GONE) View.GONE else View.VISIBLE
                    }
                }
                if (!updateStateMethod) {
                    initAction = false
                }
            }
        }
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "applyMobileState", hideMobileActivity)
        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.StatusBarMobileView", lpparam.classLoader, "updateState", hideMobileActivity)
    }

    @JvmStatic
    fun NoLightUpOnChargeHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.miui.charge.MiuiChargeController", lpparam.classLoader, "shouldShowChargeAnim", HookerClassHelper.returnConstant(false))
    }

    @JvmStatic
    fun HidePrivacyIndicatorHook(lpparam: PackageReadyParam) {
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.privacy.MiuiPrivacyControllerImpl", lpparam.classLoader, "setStatus", Int::class.javaPrimitiveType!!, String::class.java, Bundle::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                param.returnAndSkip(null)
            }
        })
    }
}
