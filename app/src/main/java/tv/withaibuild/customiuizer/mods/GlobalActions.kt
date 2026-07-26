package tv.withaibuild.customiuizer.mods

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Instrumentation
import android.app.NotificationManager
import android.app.UiModeManager
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.hardware.input.InputManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock
import android.os.UserHandle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.MiuiMultiWindowUtils
import android.util.SparseBooleanArray
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.Helpers

object GlobalActions {

    const val ACTION_PREFIX = "tv.withaibuild.customiuizer.mods.action."
    const val EVENT_PREFIX = "tv.withaibuild.customiuizer.mods.event."

    @JvmStatic
    fun handleAction(context: Context, key: String?): Boolean {
        return handleAction(context, key, false)
    }

    @JvmStatic
    fun handleAction(context: Context, key: String?, skipLock: Boolean): Boolean {
        return handleAction(context, key, skipLock, null)
    }

    @JvmStatic
    fun handleAction(context: Context, key: String?, skipLock: Boolean, bundle: Bundle?): Boolean {
        if (key == null || key.isEmpty()) return false
        val action = MainModule.mPrefs.getInt(key + "_action", 1)
        if (action <= 1) return false
        if (action in 85..88) {
            if (isMediaActionsAllowed(context)) {
                sendDownUpKeyEvent(context, action, false)
            }
            return true
        }
        return when (action) {
            2 -> commonSendAction(context, "ExpandNotifications")
            3 -> commonSendAction(context, "ExpandSettings")
            4 -> commonSendAction(context, "LockDevice")
            5 -> commonSendAction(context, "GoToSleep")
            6 -> commonSendAction(context, "TakeScreenshot")
            7 -> commonSendAction(context, "OpenRecents")
            8 -> GlobalActionsIntentHelper.launchAppIntent(context, key, skipLock)
            9 -> GlobalActionsIntentHelper.launchShortcutIntent(context, key, skipLock)
            10 -> toggleThis(context, MainModule.mPrefs.getInt(key + "_toggle", 0))
            11 -> commonSendAction(context, "SwitchToPrevApp")
            12 -> commonSendAction(context, "OpenPowerMenu")
            13 -> commonSendAction(context, "ClearMemory")
            14 -> commonSendAction(context, "ToggleColorInversion")
            15 -> commonSendAction(context, "GoBack")
            16 -> commonSendAction(context, "SimulateMenu")
            17 -> commonSendAction(context, "OpenVolumeDialog")
            18 -> commonSendAction(context, "VolumeUp")
            19 -> commonSendAction(context, "VolumeDown")
            20 -> GlobalActionsIntentHelper.launchActivityIntent(context, key, skipLock)
            22 -> commonSendAction(context, "SwitchOneHanded")
            23 -> commonSendAction(context, "ClearNotifications")
            24 -> commonSendAction(context, "ForceClose")
            25 -> commonSendAction(context, "ScrollToTop")
            26 -> showSidebar(context, bundle)
            27 -> commonSendAction(context, "FloatingWindow")
            28 -> commonSendAction(context, "PinningWindow")
            29 -> commonSendAction(context, "SplitScreen")
            else -> false
        }
    }

    @JvmStatic
    fun getActionResId(action: Int): Int {
        return when (action) {
            0, 1 -> R.string.notselected
            2 -> R.string.array_global_actions_notif
            3 -> R.string.array_global_actions_eqs
            4 -> R.string.array_global_actions_lock
            5 -> R.string.array_global_actions_sleep
            6 -> R.string.array_global_actions_screenshot
            7 -> R.string.array_global_actions_recents
            11 -> R.string.array_global_actions_back
            12 -> R.string.array_global_actions_powermenu_short
            13 -> R.string.array_global_actions_clearmemory
            14 -> R.string.array_global_actions_invertcolors
            15 -> R.string.array_global_actions_goback
            16 -> R.string.array_global_actions_menu
            17 -> R.string.array_global_actions_volume
            18 -> R.string.array_global_actions_volume_up
            19 -> R.string.array_global_actions_volume_down
            22 -> R.string.array_global_actions_onehanded_left
            23 -> R.string.array_global_actions_clear_notifs
            24 -> R.string.array_global_actions_forceclose
            25 -> R.string.array_global_actions_scrolltotop
            26 -> R.string.array_global_actions_expandsidebar
            27 -> R.string.array_global_actions_floatingwindow
            28 -> R.string.array_global_actions_pinningwindow
            29 -> R.string.array_global_actions_splitscreen
            else -> 0
        }
    }

    @JvmField
    val mSBReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("WrongConstant", "MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val modRes = ModuleHelper.getModuleRes(context)
                val action = intent.action
                if (action == null) return

                when (action) {
                    ACTION_PREFIX + "RestartSystemUI" -> Process.killProcess(Process.myPid())
                    ACTION_PREFIX + "FastReboot" -> {
                        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                        val mService = XposedHelpers.getObjectField(pm, "mService")
                        XposedHelpers.callMethod(mService, "reboot", false, null, false)
                    }
                    ACTION_PREFIX + "ClearNotifications" -> {
                        val nms = XposedHelpers.callStaticMethod(NotificationManager::class.java, "getService")
                        XposedHelpers.callMethod(nms, "cancelAllNotifications", null, 0)
                    }
                    ACTION_PREFIX + "ClearMemory" -> {
                        val clearIntent = Intent("com.android.systemui.taskmanager.Clear")
                        clearIntent.putExtra("show_toast", true)
                        context.sendBroadcast(clearIntent)
                    }
                    ACTION_PREFIX + "RestartLauncher" -> {
                        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        XposedHelpers.callMethod(am, "forceStopPackage", "com.miui.home")
                    }
                    ACTION_PREFIX + "RestartSecurityCenter" -> {
                        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        XposedHelpers.callMethod(am, "forceStopPackage", "com.miui.securitycenter")
                    }
                    ACTION_PREFIX + "FloatingWindow" -> {
                        try {
                            MiuiMultiWindowUtils.startSmallFreeformForControlCenter(context)
                        } catch (err: Throwable) {
                            XposedHelpers.log(err)
                        }
                    }
                    ACTION_PREFIX + "SwitchOneHanded" -> {
                        Settings.Secure.putInt(context.contentResolver, "one_handed_mode_activated", 1)
                        return
                    }
                    ACTION_PREFIX + "ScrollToTop" -> {
                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                val injectInputEventMethod = InputManager::class.java.getDeclaredMethod("injectInputEvent", InputEvent::class.java, Int::class.javaPrimitiveType)
                                val instanceMethod = InputManager::class.java.getDeclaredMethod("getInstance")
                                val im = instanceMethod.invoke(InputManager::class.java) as InputManager
                                val uptimeMillis = SystemClock.uptimeMillis()
                                val swipeDownEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis, MotionEvent.ACTION_DOWN, 500f, 500f, 0)
                                swipeDownEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN)
                                injectInputEventMethod.invoke(im, swipeDownEvt, 1)
                                val swipeMoveEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis + 25, MotionEvent.ACTION_MOVE, 500f, 240000f, 0)
                                swipeMoveEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN)
                                injectInputEventMethod.invoke(im, swipeMoveEvt, 2)
                                val swipeUpEvt = MotionEvent.obtain(uptimeMillis, uptimeMillis + 25, MotionEvent.ACTION_UP, 500f, 240000f, 0)
                                swipeUpEvt.setSource(InputDevice.SOURCE_TOUCHSCREEN)
                                injectInputEventMethod.invoke(im, swipeUpEvt, 2)
                                swipeDownEvt.recycle()
                                swipeMoveEvt.recycle()
                                swipeUpEvt.recycle()
                            } catch (e: Throwable) {
                                XposedHelpers.log("err: $e")
                            }
                        }, 100L)
                    }
                    ACTION_PREFIX + "ExpandNotifications" -> {
                        val mStatusBar = ModuleHelper.getDepInstance(context.classLoader, "com.android.systemui.statusbar.phone.CentralSurfaces")
                        val callbacks = XposedHelpers.getObjectField(mStatusBar, "mCommandQueueCallbacks")
                        XposedHelpers.callMethod(callbacks, "animateExpandNotificationsPanel")
                    }
                    ACTION_PREFIX + "ExpandSettings" -> {
                        val forceExpand = intent.getBooleanExtra("forceExpand", false)
                        val mStatusBar = ModuleHelper.getDepInstance(context.classLoader, "com.android.systemui.statusbar.phone.CentralSurfaces")
                        val mControlCenterController = XposedHelpers.getObjectField(mStatusBar, "mControlCenterController")
                        val isUseControlCenter = XposedHelpers.callMethod(mControlCenterController, "isUseControlCenter") as Boolean
                        if (isUseControlCenter) {
                            if (forceExpand || XposedHelpers.callMethod(mControlCenterController, "isCollapsed") as Boolean) {
                                val lazyControlCenter = XposedHelpers.getObjectField(mControlCenterController, "controlCenter")
                                val controlCenter = XposedHelpers.callMethod(lazyControlCenter, "get")
                                XposedHelpers.callMethod(controlCenter, "animateExpandSettingsPanel", "")
                            } else {
                                XposedHelpers.callMethod(mControlCenterController, "collapseControlCenter", true, true)
                            }
                            return
                        }
                        val callbacks = XposedHelpers.getObjectField(mStatusBar, "mCommandQueueCallbacks")
                        XposedHelpers.callMethod(callbacks, "animateExpandSettingsPanel", "")
                    }
                    ACTION_PREFIX + "OpenRecents" -> {
                        val recentIntent = Intent("SYSTEM_ACTION_RECENTS")
                        recentIntent.setPackage("com.android.systemui")
                        context.sendBroadcast(recentIntent)
                    }
                    ACTION_PREFIX + "OpenVolumeDialog" -> {
                        val mStatusBar = ModuleHelper.getDepInstance(context.classLoader, "com.android.systemui.statusbar.phone.CentralSurfaces")
                        val mVolumeComponent = XposedHelpers.getObjectField(mStatusBar, "mVolumeComponent")
                        val mVolumeDialogPlugin = XposedHelpers.getObjectField(mVolumeComponent, "mDialog")
                        val miuiVolumeDialog = XposedHelpers.getObjectField(mVolumeDialogPlugin, "mVolumeDialogImpl")
                        if (miuiVolumeDialog == null) {
                            XposedHelpers.log("OpenVolumeDialog", "MIUI volume dialog is NULL!")
                            return
                        }

                        val mHandler = XposedHelpers.getObjectField(miuiVolumeDialog, "mHandler") as Handler
                        mHandler.post {
                            val mShowing = XposedHelpers.getBooleanField(miuiVolumeDialog, "mShowing")
                            val mExpanded = XposedHelpers.getBooleanField(miuiVolumeDialog, "mExpanded")

                            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            val isInCall = am.mode == AudioManager.MODE_IN_CALL || am.mode == AudioManager.MODE_IN_COMMUNICATION
                            if (mShowing) {
                                if (mExpanded || isInCall) {
                                    XposedHelpers.callMethod(miuiVolumeDialog, "dismissH", 1)
                                } else {
                                    val mDialogView = XposedHelpers.getObjectField(miuiVolumeDialog, "mDialogView")
                                    val mExpandButton = XposedHelpers.getObjectField(mDialogView, "mExpandButton") as View
                                    val mClickExpand = XposedHelpers.getObjectField(mDialogView, "expandListener") as View.OnClickListener
                                    mClickExpand.onClick(mExpandButton)
                                }
                            } else {
                                val mController = XposedHelpers.getObjectField(mVolumeDialogPlugin, "mController")
                                if (isInCall) {
                                    XposedHelpers.callMethod(mController, "setActiveStream", 0)
                                    XposedHelpers.setBooleanField(miuiVolumeDialog, "mNeedReInit", true)
                                } else if (am.isMusicActive()) {
                                    XposedHelpers.callMethod(mController, "setActiveStream", 3)
                                    XposedHelpers.setBooleanField(miuiVolumeDialog, "mNeedReInit", true)
                                }
                                XposedHelpers.callMethod(miuiVolumeDialog, "showH", 1)
                            }
                        }
                    }
                    ACTION_PREFIX + "ToggleHotspot" -> {
                        val mHotspotController = ModuleHelper.getDepInstance(context.classLoader, "com.android.systemui.statusbar.policy.HotspotController")
                        if (mHotspotController == null) return
                        val mHotspotSupported = XposedHelpers.callMethod(mHotspotController, "isHotspotSupported") as Boolean
                        if (!mHotspotSupported) return
                        val mHotspotEnabled = XposedHelpers.callMethod(mHotspotController, "isHotspotEnabled") as Boolean
                        if (mHotspotEnabled) {
                            Toast.makeText(context, modRes.getString(R.string.toggle_hotspot_off), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, modRes.getString(R.string.toggle_hotspot_on), Toast.LENGTH_SHORT).show()
                        }
                        XposedHelpers.callMethod(mHotspotController, "setHotspotEnabled", !mHotspotEnabled)
                    }
                    ACTION_PREFIX + "ToggleZenMode" -> {
                        val zenModeController = ModuleHelper.getDepInstance(context.classLoader, "com.android.systemui.statusbar.policy.ZenModeController")
                        val zenModeEnabled = XposedHelpers.callMethod(zenModeController, "isZenModeOn") as Boolean
                        if (zenModeEnabled) {
                            XposedHelpers.callMethod(zenModeController, "setZen", 0, "DNDTile")
                        } else {
                            XposedHelpers.callMethod(zenModeController, "setZen", 1, "DNDTile")
                        }
                    }
                    ACTION_PREFIX + "ToggleFlashlight" -> {
                        XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.miui.systemui.util.CommonUtil", context.classLoader), "toggleTorch")
                    }
                    ACTION_PREFIX + "ToggleGPS" -> {
                        val locationController = ModuleHelper.getDepInstance(context.classLoader, "com.android.systemui.statusbar.policy.LocationController")
                        val mGpsEnable = XposedHelpers.callMethod(locationController, "isLocationEnabled") as Boolean
                        if (mGpsEnable) {
                            Toast.makeText(context, modRes.getString(R.string.toggle_gps_off), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, modRes.getString(R.string.toggle_gps_on), Toast.LENGTH_SHORT).show()
                        }
                        XposedHelpers.callMethod(locationController, "setLocationEnabled", !mGpsEnable)
                    }
                    ACTION_PREFIX + "ToggleNightMode" -> {
                        Settings.System.putInt(context.contentResolver, "dark_mode_enable_by_setting", 1)
                        val mUiModeManager = context.getSystemService("uimode") as UiModeManager
                        val nightMode = mUiModeManager.nightMode == 2
                        XposedHelpers.callMethod(mUiModeManager, "setNightModeActivated", !nightMode)
                    }
                    ACTION_PREFIX + "ToggleWiFi" -> {
                        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        if (wifiManager.isWifiEnabled) {
                            wifiManager.setWifiEnabled(false)
                            Toast.makeText(context, modRes.getString(R.string.toggle_wifi_off), Toast.LENGTH_SHORT).show()
                        } else {
                            wifiManager.setWifiEnabled(true)
                            Toast.makeText(context, modRes.getString(R.string.toggle_wifi_on), Toast.LENGTH_SHORT).show()
                        }
                    }
                    ACTION_PREFIX + "ToggleBluetooth" -> {
                        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                        if (mBluetoothAdapter.isEnabled) {
                            mBluetoothAdapter.disable()
                            Toast.makeText(context, modRes.getString(R.string.toggle_bt_off), Toast.LENGTH_SHORT).show()
                        } else {
                            mBluetoothAdapter.enable()
                            Toast.makeText(context, modRes.getString(R.string.toggle_bt_on), Toast.LENGTH_SHORT).show()
                        }
                    }
                    ACTION_PREFIX + "ToggleNFC" -> {
                        val clsNfcAdapter = XposedHelpers.findClass("android.nfc.NfcAdapter", null)
                        val mNfcAdapter = XposedHelpers.callStaticMethod(clsNfcAdapter, "getNfcAdapter", context) as NfcAdapter?
                        if (mNfcAdapter == null) return

                        val enableNFC = clsNfcAdapter.getDeclaredMethod("enable")
                        val disableNFC = clsNfcAdapter.getDeclaredMethod("disable")
                        enableNFC.isAccessible = true
                        disableNFC.isAccessible = true

                        if (mNfcAdapter.isEnabled) {
                            disableNFC.invoke(mNfcAdapter)
                            Toast.makeText(context, modRes.getString(R.string.toggle_nfc_off), Toast.LENGTH_SHORT).show()
                        } else {
                            enableNFC.invoke(mNfcAdapter)
                            Toast.makeText(context, modRes.getString(R.string.toggle_nfc_on), Toast.LENGTH_SHORT).show()
                        }
                    }
                    ACTION_PREFIX + "ToggleSoundProfile" -> {
                        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        val currentMode = am.ringerMode
                        when (currentMode) {
                            0 -> {
                                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                                Toast.makeText(context, modRes.getString(R.string.toggle_sound_vibrate), Toast.LENGTH_SHORT).show()
                            }
                            1 -> {
                                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
                                Toast.makeText(context, modRes.getString(R.string.toggle_sound_normal), Toast.LENGTH_SHORT).show()
                            }
                            2 -> {
                                am.setRingerMode(AudioManager.RINGER_MODE_SILENT)
                                Toast.makeText(context, modRes.getString(R.string.toggle_sound_silent), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    ACTION_PREFIX + "ToggleAutoRotation" -> {
                        if (Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
                            Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1)
                            Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_on), Toast.LENGTH_SHORT).show()
                        } else {
                            val rotation = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
                            Settings.System.putInt(context.contentResolver, Settings.System.USER_ROTATION, rotation)
                            Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
                            Toast.makeText(context, modRes.getString(R.string.toggle_autorotate_off), Toast.LENGTH_SHORT).show()
                        }
                    }
                    ACTION_PREFIX + "ToggleMobileData" -> {
                        val telManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        if (telManager.isDataEnabled) {
                            telManager.setDataEnabledForReason(0, false)
                            Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_off), Toast.LENGTH_SHORT).show()
                        } else {
                            telManager.setDataEnabledForReason(0, true)
                            Toast.makeText(context, modRes.getString(R.string.toggle_mobiledata_on), Toast.LENGTH_SHORT).show()
                        }
                    }
                    ACTION_PREFIX + "WakeUp" -> {
                        XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "wakeUp", SystemClock.uptimeMillis())
                    }
                    ACTION_PREFIX + "GoToSleep" -> {
                        XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 4, 0)
                    }
                    ACTION_PREFIX + "LockDevice" -> {
                        XposedHelpers.callMethod(context.getSystemService(Context.POWER_SERVICE), "goToSleep", SystemClock.uptimeMillis(), 7, 0)
                    }
                    ACTION_PREFIX + "TakeScreenshot" -> {
                        context.sendBroadcast(Intent("android.intent.action.CAPTURE_SCREENSHOT"))
                    }
                    ACTION_PREFIX + "GoBack" -> {
                        Thread {
                            Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
                        }.start()
                    }
                    ACTION_PREFIX + "VolumeUp" -> {
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, 1 shl 12 /* FLAG_FROM_KEY */ or AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_ALLOW_RINGER_MODES or AudioManager.FLAG_PLAY_SOUND or AudioManager.FLAG_VIBRATE)
                    }
                    ACTION_PREFIX + "VolumeDown" -> {
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, 1 shl 12 /* FLAG_FROM_KEY */ or AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_ALLOW_RINGER_MODES or AudioManager.FLAG_PLAY_SOUND or AudioManager.FLAG_VIBRATE)
                    }
                    ACTION_PREFIX + "OpenPowerMenu" -> {
                        val mCommandQueue = ModuleHelper.getDepInstance(context.classLoader, "com.android.systemui.statusbar.CommandQueue")
                        XposedHelpers.callMethod(mCommandQueue, "showGlobalActionsMenu")
                    }
                    ACTION_PREFIX + "LaunchIntent" -> {
                        val launchIntent = intent.getParcelableExtra<Intent>("intent")
                        if (launchIntent != null) {
                            var user = 0
                            if (launchIntent.hasExtra("user")) {
                                user = launchIntent.getIntExtra("user", 0)
                                launchIntent.removeExtra("user")
                            }
                            if (user != 0) {
                                XposedHelpers.callMethod(context, "startActivityAsUser", launchIntent, XposedHelpers.newInstance(UserHandle::class.java, user))
                            } else {
                                context.startActivity(launchIntent)
                            }
                        }
                    }
                    ACTION_PREFIX + "SaveLastMusicPausedTime" -> {
                        Settings.System.putLong(context.contentResolver, "last_music_paused_time", java.lang.System.currentTimeMillis())
                    }
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }
    }

    @JvmStatic
    fun miuizerSettingsHook(lpparam: PackageReadyParam) {
        val settingsIconResId = MainModule.resHooks.addFakeResource("ic_miuizer_settings", R.drawable.ic_miuizer_settings, "drawable")
        ModuleHelper.findAndHookMethod("com.android.settings.MiuiSettings", lpparam.classLoader, "updateHeaderList", List::class.java, object : MethodHook() {
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
                    if (chain.getArgs()[0] == null) return XposedHelpers.throwOrReturn(throwable, result)

                    val mContext = (chain.getThisObject() as Activity).baseContext
                    val opt = MainModule.mPrefs.getStringAsInt("miuizer_settingsiconpos", 1)

                    val headerCls = XposedHelpers.findClassIfExists("com.android.settingslib.miuisettings.preference.PreferenceActivity\$Header", lpparam.classLoader)
                    if (headerCls == null) return XposedHelpers.throwOrReturn(throwable, result)

                    val modRes = ModuleHelper.getModuleRes(mContext)
                    val header = XposedHelpers.newInstance(headerCls)
                    XposedHelpers.setLongField(header, "id", 666L)
                    val intent = Intent()
                    intent.setClassName(Helpers.modulePkg, "tv.withaibuild.customiuizer.MainActivity")
                    intent.putExtra("from.settings", true)
                    XposedHelpers.setObjectField(header, "intent", intent)
                    XposedHelpers.setIntField(header, "iconRes", settingsIconResId)
                    XposedHelpers.setObjectField(header, "title", modRes.getString(R.string.app_name))
                    val bundle = Bundle()
                    val users = ArrayList<UserHandle>()
                    users.add(XposedHelpers.newInstance(UserHandle::class.java, 0) as UserHandle)
                    bundle.putParcelableArrayList("header_user", users)
                    XposedHelpers.setObjectField(header, "extras", bundle)

                    val themes = mContext.resources.getIdentifier("launcher_settings", "id", mContext.packageName)
                    val special = mContext.resources.getIdentifier("other_special_feature_settings", "id", mContext.packageName)

                    val headers = chain.getArgs()[0] as MutableList<Any>
                    var position = 0
                    for (head in headers) {
                        position++
                        val id = XposedHelpers.getLongField(head, "id")
                        if (opt == 1 && id == -1L) {
                            headers.add(position - 1, header)
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                        if (opt == 2 && id == themes.toLong()) {
                            headers.add(position, header)
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                        if (opt == 3 && id == special.toLong()) {
                            headers.add(position, header)
                            return XposedHelpers.throwOrReturn(throwable, result)
                        }
                    }
                    if (headers.size > 25) headers.add(25, header) else headers.add(header)
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
        ModuleHelper.hookAllMethods("com.android.settings.MiuiSettings\$HeaderAdapter", lpparam.classLoader, "setIcon", object : MethodHook() {
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
                    val args = chain.getArgs()
                    val iconRes = XposedHelpers.getIntField(args[1], "iconRes")
                    if (iconRes == settingsIconResId) {
                        val icon = XposedHelpers.getObjectField(args[0], "icon") as ImageView
                        val iconSize = XposedHelpers.getIntField(XposedHelpers.getSurroundingThis(chain.getThisObject()!!), "mNormalIconSize")
                        icon.layoutParams.height = iconSize
                    }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    @JvmStatic
    fun setupForegroundMonitor(lpparam: PackageReadyParam) {
        ModuleHelper.hookAllConstructors("com.android.systemui.statusbar.policy.NetworkSpeedController", lpparam.classLoader, object : MethodHook() {
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
                    val thisObject = chain.getThisObject()

                    val mContext = chain.getArgs()[0] as Context
                    val mBgHandler = XposedHelpers.getObjectField(thisObject, "mBgHandler") as Handler
                    ModuleHelper.findAndHookMethod("com.miui.systemui.functions.MiuiTopActivityObserver", lpparam.classLoader, "updateTopActivity", object : MethodHook() {
                        private var pkgName = ""
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            var result2: Any?
                            var throwable2: Throwable? = null
                            try {
                                result2 = chain.proceed()
                            } catch (t: Throwable) {
                                throwable2 = t
                                result2 = null
                            }
                            try {
                                val thisObject2 = chain.getThisObject()

                                val mTopActivity = XposedHelpers.getObjectField(thisObject2, "mTopActivity") as ComponentName?
                                if (mTopActivity != null && pkgName != mTopActivity.packageName) {
                                    pkgName = mTopActivity.packageName!!
                                    Settings.Global.putString(mContext.contentResolver, Helpers.modulePkg + ".foreground.package", pkgName)
                                }
                            } catch (t: Throwable) {
                                XposedHelpers.log(t)
                            }
                            return XposedHelpers.throwOrReturn(throwable2, result2)
                        }
                    })
                    if (MainModule.mPrefs.getStringAsInt("various_showcallui", 0) > 0) {
                        ModuleHelper.hookAllMethods("com.android.systemui.statusbar.phone.SystemBarAttributesListener", lpparam.classLoader, "onSystemBarAttributesChanged", object : MethodHook() {
                            private var fullScreen = false
                            override fun intercept(chain: XposedInterface.Chain): Any? {
                                var result2: Any?
                                var throwable2: Throwable? = null
                                try {
                                    result2 = chain.proceed()
                                } catch (t: Throwable) {
                                    throwable2 = t
                                    result2 = null
                                }
                                try {
                                    val thisObject2 = chain.getThisObject()

                                    val statusBarStateController = XposedHelpers.getObjectField(thisObject2, "statusBarStateController")
                                    val isFullScreen = XposedHelpers.getBooleanField(statusBarStateController, "mIsFullscreen")
                                    if (fullScreen != isFullScreen) {
                                        mBgHandler.post {
                                            Settings.Global.putInt(mContext.contentResolver, Helpers.modulePkg + ".foreground.fullscreen", if (fullScreen) 1 else 0)
                                        }
                                    }
                                    fullScreen = isFullScreen
                                } catch (t: Throwable) {
                                    XposedHelpers.log(t)
                                }
                                return XposedHelpers.throwOrReturn(throwable2, result2)
                            }
                        })
                    }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
                return XposedHelpers.throwOrReturn(throwable, result)
            }
        })
    }

    private val customActionKeys = arrayOf(
        "controls_backlong", "controls_homelong", "controls_menulong",
        "controls_powerdt",
        "controls_fsg_assist_left", "controls_fsg_assist_right",
        "controls_fsg_swipeandstop",
        "controls_navbarleft", "controls_navbarleftlong",
        "controls_navbarright", "controls_navbarrightlong",
        "launcher_swipedown", "launcher_swipeup", "launcher_swipedown2", "launcher_swipeup2",
        "launcher_swipeleft", "launcher_swiperight",
        "launcher_doubletap", "launcher_pinch", "launcher_shake", "launcher_spread",
        "system_statusbarcontrols", "system_statusbarcontrols_longpress",
        "system_lockscreenshortcuts_left", "system_lockscreenshortcuts_right"
    )

    @Volatile
    private var customActionCodeMap: SparseBooleanArray? = null

    @Volatile
    private var customToggleMap: SparseBooleanArray? = null

    @Volatile
    private var customActionsReady = false

    @JvmStatic
    private fun ensureCustomActionMaps() {
        if (customActionsReady) return
        synchronized(GlobalActions::class.java) {
            if (customActionsReady) return
            val actionMap = SparseBooleanArray()
            val toggleMap = SparseBooleanArray()
            for (key in customActionKeys) {
                val action = MainModule.mPrefs.getInt(key + "_action", 1)
                if (action > 1) actionMap.put(action, true)
                if (action == 10) {
                    val toggle = MainModule.mPrefs.getInt(key + "_toggle", 0)
                    if (toggle > 0) toggleMap.put(toggle, true)
                }
            }
            customActionCodeMap = actionMap
            customToggleMap = toggleMap
            customActionsReady = true
        }
    }

    @JvmStatic
    fun hasCustomActions(): Boolean {
        ensureCustomActionMaps()
        return customActionCodeMap!!.size() > 0
    }

    @JvmStatic
    fun hasActionCode(code: Int): Boolean {
        ensureCustomActionMaps()
        return customActionCodeMap!!.get(code)
    }

    @JvmStatic
    fun hasToggle(what: Int): Boolean {
        ensureCustomActionMaps()
        return customToggleMap!!.get(what)
    }

    @JvmStatic
    fun setupGlobalActions(lpparam: XposedModuleInterface.SystemServerStartingParam) {
        GlobalActionSystemServerHooks.setupGlobalActions(lpparam)
    }

    @JvmStatic
    fun setupStatusBar(lpparam: PackageReadyParam) {
        GlobalActionSystemServerHooks.setupStatusBar(lpparam)
    }

    @JvmStatic
    fun launchAppIntent(context: Context, key: String, skipLock: Boolean): Boolean = GlobalActionsIntentHelper.launchAppIntent(context, key, skipLock)

    @JvmStatic
    fun launchActivityIntent(context: Context, key: String, skipLock: Boolean): Boolean = GlobalActionsIntentHelper.launchActivityIntent(context, key, skipLock)

    @JvmStatic
    fun launchShortcutIntent(context: Context, key: String, skipLock: Boolean): Boolean = GlobalActionsIntentHelper.launchShortcutIntent(context, key, skipLock)

    @JvmStatic
    fun launchIntent(context: Context, intent: Intent): Boolean = GlobalActionsIntentHelper.launchIntent(context, intent)

    private fun showSidebar(context: Context, bundle: Bundle?): Boolean {
        return try {
            val showIntent = Intent(ACTION_PREFIX + "ShowSideBar")
            showIntent.setPackage("com.miui.securitycenter")
            if (bundle != null) {
                showIntent.putExtra("actionInfo", bundle)
            }
            context.sendBroadcast(showIntent)
            true
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            false
        }
    }

    @JvmStatic
    fun commonSendAction(context: Context, action: String): Boolean {
        return try {
            context.sendBroadcast(Intent(ACTION_PREFIX + action))
            true
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            false
        }
    }

    private fun toggleThis(context: Context, what: Int): Boolean {
        return try {
            val whatStr = when (what) {
                1 -> "WiFi"
                2 -> "Bluetooth"
                3 -> "GPS"
                4 -> "NFC"
                5 -> "SoundProfile"
                6 -> "AutoBrightness"
                7 -> "AutoRotation"
                8 -> "Flashlight"
                9 -> "MobileData"
                10 -> "Hotspot"
                11 -> "ZenMode"
                12 -> "NightMode"
                else -> return false
            }
            context.sendBroadcast(Intent(ACTION_PREFIX + "Toggle" + whatStr))
            true
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            false
        }
    }

    @JvmStatic
    fun isMediaActionsAllowed(mContext: Context): Boolean {
        val am = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val isMusicActive = am.isMusicActive
        val isMusicActiveRemotely = XposedHelpers.callMethod(am, "isMusicActiveRemotely") as Boolean
        var isAllowed = isMusicActive || isMusicActiveRemotely
        if (!isAllowed) {
            val mCurrentTime = java.lang.System.currentTimeMillis()
            val mLastPauseTime = Settings.System.getLong(mContext.contentResolver, "last_music_paused_time", mCurrentTime)
            if (mCurrentTime - mLastPauseTime < 10 * 60 * 1000) isAllowed = true
        }
        return isAllowed
    }

    @JvmStatic
    fun sendDownUpKeyEvent(mContext: Context, keyCode: Int, vibrate: Boolean) {
        val am = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))

        if (vibrate && MainModule.mPrefs.getBoolean("controls_volumemedia_vibrate", true)) {
            Helpers.performStrongVibration(mContext, MainModule.mPrefs.getBoolean("controls_volumemedia_vibrate_ignore"))
        }
    }
}
