package tv.withaibuild.customiuizer.mods

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Parcel
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.ArrayMap
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import miui.telephony.TelephonyManager
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.PrefMap
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.util.Locale
import java.util.Properties

@Suppress("MemberVisibilityCanBePrivate")
object SystemUIMonitorAndTileHooks {

    private data class TextIconInfo(
        var iconShow: Boolean = false,
        var iconType: Int = 0,
        var iconText: String = ""
    )

    @JvmStatic
    fun MonitorDeviceInfoHook(lpparam: PackageReadyParam, mPrefs: PrefMap) {
        val showBatteryDetail = mPrefs.getBoolean("system_statusbar_batterytempandcurrent")
        val showDeviceTemp = mPrefs.getBoolean("system_statusbar_showdevicetemperature")
        val dualRows = mPrefs.getBoolean("system_statusbar_dualrows")
        val batteryAtRight = showBatteryDetail && !dualRows && mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright")
        val tempAtRight = showDeviceTemp && !dualRows && mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright")
        val batteryAtLeft = showBatteryDetail && !mPrefs.getBoolean("system_statusbar_batterytempandcurrent_atright")
        val tempAtLeft = showDeviceTemp && !mPrefs.getBoolean("system_statusbar_showdevicetemperature_atright")
        val chargeUtilsClass = if (showBatteryDetail) XposedHelpers.findClassIfExists("com.miui.charge.ChargeUtils", lpparam.classLoader) else null

        val customIconTypes = ArrayList<Int>()
        if (batteryAtLeft || batteryAtRight) {
            customIconTypes.add(91)
        }
        if (tempAtLeft || tempAtRight) {
            customIconTypes.add(92)
        }
        if (customIconTypes.isNotEmpty()) {
            ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    val StatusBarIconHolder = XposedHelpers.findClass("com.android.systemui.statusbar.phone.StatusBarIconHolder", lpparam.classLoader)
                    val iconController = XposedHelpers.getObjectField(param.getThisObject(), "mStatusBarIconController")
                    for (iconType in customIconTypes) {
                        val slot = SystemUI.getSlotNameByType(iconType)
                        val mStatusBarIconList = XposedHelpers.getObjectField(iconController, "mStatusBarIconList")
                        var iconHolder = XposedHelpers.callMethod(mStatusBarIconList, "getIconHolder", 0, slot)
                        if (iconHolder == null) {
                            iconHolder = XposedHelpers.newInstance(StatusBarIconHolder)
                            XposedHelpers.setObjectField(iconHolder, "mType", iconType)
                            XposedHelpers.callMethod(iconController, "setIcon", slot, iconHolder)
                        }
                    }
                }
            })
            ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.StatusBarIconController\$IconManager", lpparam.classLoader, "addHolder", object : MethodHook() {
                override fun before(param: BeforeHookCallback) {
                    if (param.getArgs().size != 4) return
                    val iconHolder = param.getArgs()[3]
                    val type = XposedHelpers.getIntField(iconHolder, "mType")
                    if (type == 91 || type == 92) {
                        val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                        val lp = XposedHelpers.callMethod(param.getThisObject(), "onCreateLayoutParams") as LinearLayout.LayoutParams
                        val iconView = SystemUI.createStatusbarTextIcon(mContext, lp, type, true)
                        val i = param.getArgs()[0] as Int
                        val mGroup = XposedHelpers.getObjectField(param.getThisObject(), "mGroup") as ViewGroup
                        mGroup.addView(iconView, i)
                        SystemUI.mStatusbarTextIcons.add(iconView)
                        param.returnAndSkip(iconView)
                    }
                }
            })
        }
        ModuleHelper.findAndHookMethod("com.android.systemui.statusbar.views.NetworkSpeedView", lpparam.classLoader, "getSlot", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val nsView = param.getThisObject() as View
                val tagData = nsView.getTag(SystemUI.textIconTagId)
                if (tagData != null) {
                    param.returnAndSkip(SystemUI.getSlotNameByType(tagData as Int))
                }
            }
        })
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, object : MethodHook() {
            private var mBgHandler: Handler? = null
            override fun after(param: AfterHookCallback) {
                val mContext = param.getArgs()[0] as Context
                val mHandler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        if (msg.what == 100021) {
                            val tii = msg.obj as? TextIconInfo ?: return
                            for (tv in SystemUI.mStatusbarTextIcons) {
                                val tagData = tv.getTag(SystemUI.textIconTagId)
                                if (tagData != null) {
                                    val iconType = tagData as Int
                                    if (tii.iconType == iconType) {
                                        XposedHelpers.callMethod(tv, "setVisibilityByController", tii.iconShow)
                                        if (tii.iconShow) {
                                            XposedHelpers.callMethod(tv, "setNetworkSpeed", tii.iconText, "")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                mBgHandler = object : Handler(param.getArgs()[1] as Looper) {
                    override fun handleMessage(msg: Message) {
                        if (msg.what != 200021) return
                        var batteryInfo = ""
                        var deviceInfo = ""
                        var showBatteryInfo = showBatteryDetail
                        if (showBatteryInfo && mPrefs.getBoolean("system_statusbar_batterytempandcurrent_incharge") && chargeUtilsClass != null) {
                            val batteryStatus = ModuleHelper.getStaticObjectFieldSilently(chargeUtilsClass, "sBatteryStatus")
                            if (ModuleHelper.NOT_EXIST_SYMBOL == batteryStatus) {
                                showBatteryInfo = false
                            } else {
                                showBatteryInfo = XposedHelpers.callMethod(batteryStatus, "isCharging") as Boolean
                            }
                        }
                        val powerMgr = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                        val isScreenOn = powerMgr.isInteractive
                        if (isScreenOn) {
                            val props: Properties? = try {
                                FileInputStream("/sys/class/power_supply/battery/uevent").use { fis ->
                                    Properties().apply { load(fis) }
                                }
                            } catch (_: Throwable) {
                                null
                            }
                            val cpuProps: String? = if (showDeviceTemp) {
                                val thermalId = ModuleHelper.getCPUThermalId()
                                if (thermalId != -1) {
                                    try {
                                        RandomAccessFile("/sys/devices/virtual/thermal/thermal_zone$thermalId/temp", "r").use { it.readLine() }
                                    } catch (_: Throwable) {
                                        null
                                    }
                                } else null
                            } else null

                            if (showBatteryInfo && props != null) {
                                val opt = mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_content", 1)
                                var simpleTempVal = ""
                                if (opt == 1 || opt == 4) {
                                    val decimal = mPrefs.getBoolean("system_statusbar_batterytempandcurrent_temp_decimal")
                                    var tempVal = 0
                                    if (!TextUtils.isEmpty(props.getProperty("POWER_SUPPLY_TEMP"))) {
                                        tempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"))
                                    }
                                    simpleTempVal = if (decimal) {
                                        (tempVal / 10f).toString()
                                    } else {
                                        if (tempVal % 10 == 0) (tempVal / 10).toString() else (tempVal / 10f).toString()
                                    }
                                }
                                var currVal = ""
                                var preferred = "mA"
                                var currentRatio = 1000f
                                if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_fixcurrentratio")) {
                                    currentRatio = 1f
                                }
                                var curReadVal = 0
                                if (!TextUtils.isEmpty(props.getProperty("POWER_SUPPLY_CURRENT_NOW"))) {
                                    curReadVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_CURRENT_NOW"))
                                }
                                var rawCurr = -1 * Math.round(curReadVal / currentRatio)
                                if (opt == 1 || opt == 3 || opt == 5) {
                                    if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_positive")) {
                                        rawCurr = Math.abs(rawCurr)
                                    }
                                    currVal = if (Math.abs(rawCurr) > 999) {
                                        preferred = "A"
                                        String.format(Locale.ROOT, "%.2f", rawCurr / 1000f)
                                    } else {
                                        rawCurr.toString()
                                    }
                                }
                                val hideUnit = mPrefs.getStringAsInt("system_statusbar_batterytempandcurrent_hideunit", 0)
                                val tempUnit = if (hideUnit == 1 || hideUnit == 2) "" else "℃"
                                val powerUnit = if (hideUnit == 1 || hideUnit == 3) "" else "W"
                                val currUnit = if (hideUnit == 1 || hideUnit == 3) "" else preferred
                                var simpleWatt = ""
                                if (opt == 2 || opt == 4 || opt == 5) {
                                    var voltVal = 0f
                                    if (!TextUtils.isEmpty(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW"))) {
                                        voltVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_VOLTAGE_NOW")) / 1000f / 1000f
                                    }
                                    simpleWatt = String.format(Locale.ROOT, "%.2f", Math.abs(voltVal * rawCurr) / 1000)
                                }
                                batteryInfo = when (opt) {
                                    1 -> {
                                        val splitChar = if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow")) " " else "\n"
                                        if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            currVal + currUnit + splitChar + simpleTempVal + tempUnit
                                        } else {
                                            simpleTempVal + tempUnit + splitChar + currVal + currUnit
                                        }
                                    }
                                    4 -> {
                                        val splitChar = if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow")) " " else "\n"
                                        if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            simpleWatt + powerUnit + splitChar + simpleTempVal + tempUnit
                                        } else {
                                            simpleTempVal + tempUnit + splitChar + simpleWatt + powerUnit
                                        }
                                    }
                                    2 -> simpleWatt + powerUnit
                                    5 -> {
                                        val splitChar = if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_singlerow")) " " else "\n"
                                        if (mPrefs.getBoolean("system_statusbar_batterytempandcurrent_reverseorder")) {
                                            simpleWatt + powerUnit + splitChar + currVal + currUnit
                                        } else {
                                            currVal + currUnit + splitChar + simpleWatt + powerUnit
                                        }
                                    }
                                    else -> currVal + currUnit
                                }
                            }
                            if (showDeviceTemp && props != null && cpuProps != null) {
                                val batteryTempVal = Integer.parseInt(props.getProperty("POWER_SUPPLY_TEMP"))
                                val cpuTempVal = Integer.parseInt(cpuProps)
                                val simpleBatteryTemp = String.format(Locale.ROOT, "%.1f", batteryTempVal / 10f)
                                val simpleCpuTemp = String.format(Locale.ROOT, "%.1f", cpuTempVal / 1000f)
                                val opt = mPrefs.getStringAsInt("system_statusbar_showdevicetemperature_content", 1)
                                val hideUnit = mPrefs.getBoolean("system_statusbar_showdevicetemperature_hideunit")
                                val tempUnit = if (hideUnit) "" else "℃"
                                deviceInfo = when (opt) {
                                    1 -> {
                                        val splitChar = if (mPrefs.getBoolean("system_statusbar_showdevicetemperature_singlerow")) " " else "\n"
                                        if (mPrefs.getBoolean("system_statusbar_showdevicetemperature_reverseorder")) {
                                            simpleCpuTemp + tempUnit + splitChar + simpleBatteryTemp + tempUnit
                                        } else {
                                            simpleBatteryTemp + tempUnit + splitChar + simpleCpuTemp + tempUnit
                                        }
                                    }
                                    2 -> simpleBatteryTemp + tempUnit
                                    else -> simpleCpuTemp + tempUnit
                                }
                            }
                            if (showBatteryDetail) {
                                val tii = TextIconInfo(showBatteryInfo, 91, batteryInfo)
                                mHandler.obtainMessage(100021, tii).sendToTarget()
                            }
                            if (showDeviceTemp) {
                                val tii = TextIconInfo(true, 92, deviceInfo)
                                mHandler.obtainMessage(100021, tii).sendToTarget()
                            }
                        }
                        mBgHandler?.removeMessages(200021)
                        mBgHandler?.sendEmptyMessageDelayed(200021, 2000)
                    }
                }
                mBgHandler?.sendEmptyMessage(200021)
            }
        })
    }

    @JvmStatic
    @SuppressLint("DiscouragedApi")
    fun AddCustomTileHook(lpparam: PackageReadyParam) {
        val enable5G = MainModule.mPrefs.getBoolean("system_fivegtile")
        val enableFps = MainModule.mPrefs.getBoolean("system_cc_fpstile")
        val enableFloatingTime = MainModule.mPrefs.getBoolean("system_cc_floatingtimetile")
        ModuleHelper.findAndHookMethod("com.android.systemui.SystemUIApplication", lpparam.classLoader, "onCreate", object : MethodHook() {
            private var isListened = false
            override fun after(param: AfterHookCallback) {
                if (!isListened) {
                    isListened = true
                    val mContext = XposedHelpers.callMethod(param.getThisObject(), "getApplicationContext") as Context
                    val stockTilesResId = mContext.resources.getIdentifier("miui_quick_settings_tiles_stock", "string", lpparam.packageName)
                    val stockTiles = mContext.getString(stockTilesResId)
                    val sb = StringBuilder(stockTiles)
                    if (enable5G) sb.append(",custom_5G")
                    if (enableFps) sb.append(",custom_FPS")
                    if (enableFloatingTime) sb.append(",custom_floatingtime")
                    MainModule.resHooks.setObjectReplacement("com.android.systemui", "string", "miui_quick_settings_tiles_stock", sb.toString())
                }
            }
        })
        val ResourceIconClass = XposedHelpers.findClass("com.android.systemui.qs.tileimpl.QSTileImpl\$ResourceIcon", lpparam.classLoader)
        ModuleHelper.findAndHookMethod("com.android.systemui.qs.tileimpl.MiuiQSFactory", lpparam.classLoader, "createTile", String::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = param.getArgs()[0] as String
                if (tileName.startsWith("custom_")) {
                    val nfcField = "nfcTileProvider"
                    val provider = XposedHelpers.getObjectField(param.getThisObject(), nfcField)
                    val tile = XposedHelpers.callMethod(provider, "get")
                    XposedHelpers.setAdditionalInstanceField(tile, "customName", tileName)
                    XposedHelpers.callMethod(tile, "handleInitialize")
                    XposedHelpers.callMethod(tile, "handleStale")
                    param.returnAndSkip(tile)
                }
            }
        })
        val NfcTileCls = "com.android.systemui.qs.tiles.MiuiNfcTile"
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.classLoader, "isAvailable", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName") as String?
                if (tileName != null) {
                    when (tileName) {
                        "custom_5G" -> param.returnAndSkip(enable5G)
                        "custom_FPS" -> param.returnAndSkip(enableFps)
                        "custom_floatingtime" -> param.returnAndSkip(enableFloatingTime)
                        else -> param.returnAndSkip(false)
                    }
                }
            }
        })
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.classLoader, "getTileLabel", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName") as String?
                if (tileName != null) {
                    val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                    val modRes = ModuleHelper.getModuleRes(mContext)
                    when (tileName) {
                        "custom_5G" -> param.returnAndSkip(modRes.getString(R.string.qs_toggle_5g))
                        "custom_FPS" -> param.returnAndSkip(modRes.getString(R.string.qs_toggle_fps))
                        "custom_floatingtime" -> param.returnAndSkip(modRes.getString(R.string.qs_toggle_floatingtime))
                    }
                }
            }
        })
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.classLoader, "handleSetListening", Boolean::class.javaPrimitiveType!!, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName") as String?
                if (tileName != null) {
                    val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                    val mListening = param.getArgs()[0] as Boolean
                    when (tileName) {
                        "custom_5G" -> {
                            val resolver = mContext.contentResolver
                            val oldObserver = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "tileListener") as ContentObserver?
                            if (oldObserver != null) {
                                resolver.unregisterContentObserver(oldObserver)
                                XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "tileListener")
                            }
                            if (mListening) {
                                val contentObserver = object : ContentObserver(Handler(mContext.mainLooper)) {
                                    override fun onChange(selfChange: Boolean) {
                                        XposedHelpers.callMethod(param.getThisObject(), "refreshState")
                                    }
                                }
                                resolver.registerContentObserver(Settings.Global.getUriFor("fiveg_user_enable"), false, contentObserver)
                                resolver.registerContentObserver(Settings.Global.getUriFor("dual_nr_enabled"), false, contentObserver)
                                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "tileListener", contentObserver)
                            }
                        }
                        "custom_FPS" -> {
                            if (mListening) {
                                val ServiceManager = XposedHelpers.findClass("android.os.ServiceManager", lpparam.classLoader)
                                val mSurfaceFlinger = XposedHelpers.callStaticMethod(ServiceManager, "getService", "SurfaceFlinger")
                                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger", mSurfaceFlinger)
                            } else {
                                XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger")
                            }
                        }
                        "custom_floatingtime" -> {
                            val resolver = mContext.contentResolver
                            val oldObserver = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "tileListener") as ContentObserver?
                            if (oldObserver != null) {
                                resolver.unregisterContentObserver(oldObserver)
                                XposedHelpers.removeAdditionalInstanceField(param.getThisObject(), "tileListener")
                            }
                            if (mListening) {
                                val contentObserver = object : ContentObserver(Handler(mContext.mainLooper)) {
                                    override fun onChange(selfChange: Boolean) {
                                        XposedHelpers.callMethod(param.getThisObject(), "refreshState")
                                    }
                                }
                                resolver.registerContentObserver(Settings.System.getUriFor("miui_time_floating_window"), false, contentObserver)
                                XposedHelpers.setAdditionalInstanceField(param.getThisObject(), "tileListener", contentObserver)
                            }
                        }
                    }
                    param.returnAndSkip(null)
                }
            }
        })
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.classLoader, "handleShowStateMessage", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName") as String?
                if (tileName != null) {
                    param.returnAndSkip(null)
                }
            }
        })
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.classLoader, "getLongClickIntent", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName") as String?
                if (tileName == "custom_5G") {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    intent.component = ComponentName("com.android.phone", "com.android.phone.settings.MiuiFiveGNetworkSetting")
                    param.returnAndSkip(intent)
                } else if (tileName != null) {
                    param.returnAndSkip(null)
                }
            }
        })
        ModuleHelper.findAndHookMethod(NfcTileCls, lpparam.classLoader, "handleClick", View::class.java, object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName") as String?
                if (tileName != null) {
                    when (tileName) {
                        "custom_5G" -> {
                            val manager = TelephonyManager.getDefault()
                            manager.setUserFiveGEnabled(!manager.isUserFiveGEnabled())
                        }
                        "custom_FPS" -> {
                            val mSurfaceFlinger = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger") as IBinder?
                            if (mSurfaceFlinger != null) {
                                val mState = XposedHelpers.getObjectField(param.getThisObject(), "mState")
                                val enabled = XposedHelpers.getBooleanField(mState, "value")
                                val obtain = Parcel.obtain()
                                obtain.writeInterfaceToken("android.ui.ISurfaceComposer")
                                obtain.writeInt(if (enabled) 0 else 1)
                                mSurfaceFlinger.transact(1034, obtain, null, 0)
                                obtain.recycle()
                                XposedHelpers.callMethod(param.getThisObject(), "refreshState")
                            }
                        }
                        "custom_floatingtime" -> {
                            val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                            val isEnable = (XposedHelpers.callStaticMethod(Settings.System::class.java, "getIntForUser", mContext.contentResolver, "miui_time_floating_window", 0, -2) as Int) != 0
                            XposedHelpers.callStaticMethod(Settings.System::class.java, "putIntForUser", mContext.contentResolver, "miui_time_floating_window", if (isEnable) 0 else 1, -2)
                        }
                    }
                    param.returnAndSkip(null)
                }
            }
        })

        val tileOnResMap = ArrayMap<String, Int>()
        val tileOffResMap = ArrayMap<String, Int>()
        if (enable5G) {
            tileOnResMap["custom_5G"] = MainModule.resHooks.addFakeResource("ic_qs_m5g_on", R.drawable.ic_qs_5g_on, "drawable")
            tileOffResMap["custom_5G"] = MainModule.resHooks.addFakeResource("ic_qs_m5g_off", R.drawable.ic_qs_5g_off, "drawable")
        }
        if (enableFps) {
            tileOnResMap["custom_FPS"] = MainModule.resHooks.addFakeResource("ic_qs_mfps_on", R.drawable.ic_qs_fps_on, "drawable")
            tileOffResMap["custom_FPS"] = MainModule.resHooks.addFakeResource("ic_qs_mfps_off", R.drawable.ic_qs_fps_off, "drawable")
        }
        if (enableFloatingTime) {
            tileOnResMap["custom_floatingtime"] = MainModule.resHooks.addFakeResource("ic_qs_mfloatingtime_on", R.drawable.ic_qs_second_off, "drawable")
            tileOffResMap["custom_floatingtime"] = MainModule.resHooks.addFakeResource("ic_qs_mfloatingtime_off", R.drawable.ic_qs_second_on, "drawable")
        }
        ModuleHelper.hookAllMethods(NfcTileCls, lpparam.classLoader, "handleUpdateState", object : MethodHook() {
            override fun before(param: BeforeHookCallback) {
                val tileName = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "customName") as String?
                if (tileName != null) {
                    var isEnable = false
                    when (tileName) {
                        "custom_5G" -> {
                            val manager = TelephonyManager.getDefault()
                            isEnable = manager.isUserFiveGEnabled()
                        }
                        "custom_FPS" -> {
                            val mSurfaceFlinger = XposedHelpers.getAdditionalInstanceField(param.getThisObject(), "mSurfaceFlinger") as IBinder?
                            if (mSurfaceFlinger != null) {
                                val obtain = Parcel.obtain()
                                val obtain2 = Parcel.obtain()
                                obtain.writeInterfaceToken("android.ui.ISurfaceComposer")
                                obtain.writeInt(2)
                                mSurfaceFlinger.transact(1034, obtain, obtain2, 0)
                                isEnable = obtain2.readBoolean()
                                obtain2.recycle()
                                obtain.recycle()
                            }
                        }
                        "custom_floatingtime" -> {
                            val mContext = XposedHelpers.getObjectField(param.getThisObject(), "mContext") as Context
                            isEnable = (XposedHelpers.callStaticMethod(Settings.System::class.java, "getIntForUser", mContext.contentResolver, "miui_time_floating_window", 0, -2) as Int) != 0
                        }
                    }
                    if (tileName.startsWith("custom_")) {
                        val booleanState = param.getArgs()[0]
                        XposedHelpers.setObjectField(booleanState, "value", isEnable)
                        XposedHelpers.setObjectField(booleanState, "state", if (isEnable) 2 else 1)
                        val tileLabel = XposedHelpers.callMethod(param.getThisObject(), "getTileLabel") as String
                        XposedHelpers.setObjectField(booleanState, "label", tileLabel)
                        XposedHelpers.setObjectField(booleanState, "contentDescription", tileLabel)
                        XposedHelpers.setObjectField(booleanState, "expandedAccessibilityClassName", Switch::class.java.name)
                        val iconResId = if (isEnable) tileOnResMap[tileName] else tileOffResMap[tileName]
                        val mIcon = XposedHelpers.callStaticMethod(ResourceIconClass, "get", iconResId)
                        XposedHelpers.setObjectField(booleanState, "icon", mIcon)
                    }
                    param.returnAndSkip(null)
                }
            }
        })
    }
}
