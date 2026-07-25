package tv.withaibuild.customiuizer.subs

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import miui.os.Build
import tv.withaibuild.customiuizer.CredentialsLauncher
import tv.withaibuild.customiuizer.PrefsProvider
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.CheckBoxPreferenceEx
import tv.withaibuild.customiuizer.prefs.SeekBarPreference
import tv.withaibuild.customiuizer.qs.AutoRotateService
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import kotlin.math.roundToInt

class System : SubFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        selectSub()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (sub) {
            "pref_key_system_cat_recents" -> {
                toolbarMenu = true
                activeMenus = "launcher"
            }
            "pref_key_system_cat_statusbar",
            "pref_key_system_cat_lockscreen",
            "pref_key_system_cat_qs",
            "pref_key_system_cat_drawer" -> {
                toolbarMenu = true
                activeMenus = "systemui"
            }
            else -> toolbarMenu = false
        }
    }

    private inline fun openSystemSubFragment(preference: Preference, isDynamic: Boolean = false, xmlResId: Int) {
        val args = Bundle().apply {
            putBoolean("isStandalone", true)
            if (isDynamic) putBundle("catInfo", Bundle().apply { putBoolean("isDynamic", true) })
            putString("sub", preference.key)
        }
        openSubFragment(System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, preference.title?.toString() ?: "", xmlResId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        when (sub) {
            "pref_key_system_cat_screen" -> {
                findPreference<Preference>("pref_key_system_orientationlock")?.setOnPreferenceChangeListener { _, newValue ->
                    val act = activity ?: return@setOnPreferenceChangeListener false
                    val pm = act.packageManager
                    val state = if (newValue == true) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    pm.setComponentEnabledSetting(ComponentName(act, AutoRotateService::class.java), state, PackageManager.DONT_KILL_APP)
                    true
                }

                findPreference<Preference>("pref_key_system_autobrightness_cat")?.setOnPreferenceClickListener {
                    openSubFragment(System_AutoBrightness(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_autobrightness_title, R.xml.prefs_system_autobrightness)
                    true
                }
            }
            "pref_key_system_cat_audio" -> {
                findPreference<Preference>("pref_key_system_ignorecalls_apps")?.setOnPreferenceClickListener(openAppsEdit)
                findPreference<Preference>("pref_key_system_visualizer_cat")?.setOnPreferenceClickListener {
                    openSubFragment(System_Visualizer(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_visualizer_title, R.xml.prefs_system_visualizer)
                    true
                }
            }
            "pref_key_system_cat_vibration" -> {
                findPreference<Preference>("pref_key_system_vibration_apps")?.setOnPreferenceClickListener(openAppsEdit)
                findPreference<Preference>("pref_key_system_vibration_amp_cat")?.setOnPreferenceClickListener {
                    openSubFragment(System_VibrationAmp(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_vibration_amp_title, R.xml.prefs_system_vibration_amp)
                    true
                }
            }
            "pref_key_system_cat_toasts" -> findPreference<Preference>("pref_key_system_blocktoasts_apps")?.setOnPreferenceClickListener(openAppsEdit)

            "pref_key_system_cat_statusbar" -> {
                findPreference<Preference>("pref_key_system_statusbarcolor_apps")?.setOnPreferenceClickListener(openAppsEdit)
                findPreference<Preference>("pref_key_system_detailednetspeed_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, false, R.xml.prefs_system_detailednetspeed); true }
                findPreference<Preference>("pref_key_system_statusbar_batterytempandcurrent_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_statusbar_batterytempandcurrent); true }
                findPreference<Preference>("prefs_system_statusbar_showdevicetemperature_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_statusbar_showdevicetemperature); true }
                findPreference<Preference>("pref_key_system_statusbar_batterystyle_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, false, R.xml.prefs_system_statusbar_batterystyle); true }
                findPreference<Preference>("pref_key_system_statusbar_mobile_signal_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_statusbar_mobilesignal); true }
                findPreference<Preference>("pref_key_system_statusbaricons_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_hideicons); true }
                findPreference<Preference>("pref_key_system_statusbaricons_atright_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_statusbar_righticons); true }
                findPreference<Preference>("pref_key_system_statusbar_clocktweak_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_statusbar_clock); true }
                findPreference<Preference>("pref_key_system_batteryindicator_cat")?.setOnPreferenceClickListener {
                    openSubFragment(System_BatteryIndicator(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_batteryindicator_title, R.xml.prefs_system_batteryindicator)
                    true
                }
                findPreference<Preference>("pref_key_system_statusbarcontrols_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_statusbarcontrols); true }
            }
            "pref_key_system_cat_drawer" -> {
                findPreference<Preference>("pref_key_system_shortcut_app")?.setOnPreferenceClickListener {
                    openStandaloneApp(it, this, 0)
                    true
                }
                findPreference<Preference>("pref_key_system_clock_app")?.setOnPreferenceClickListener {
                    openStandaloneApp(it, this, 1)
                    true
                }
                findPreference<Preference>("pref_key_system_calendar_app")?.setOnPreferenceClickListener {
                    openStandaloneApp(it, this, 2)
                    true
                }
                findPreference<Preference>("pref_key_system_cc_clocktweak_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_controlcenter_clock); true }
            }
            "pref_key_system_cat_notifications" -> {
                findPreference<Preference>("pref_key_system_expandnotifs_apps")?.setOnPreferenceClickListener(openAppsEdit)
                findPreference<Preference>("pref_key_system_notify_openinfw_apps")?.setOnPreferenceClickListener(openAppsEdit)
                findPreference<Preference>("pref_key_system_colorizenotifs_apps")?.setOnPreferenceClickListener(openAppsEdit)
            }
            "pref_key_system_cat_qs" -> {
                if (Build.IS_INTERNATIONAL_BUILD) {
                    findPreference<Preference>("pref_key_system_cc_switch_qsandnotification")?.isVisible = false
                }
                findPreference<Preference>("pref_key_system_cc_tile_style_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, false, R.xml.prefs_system_controlcenter_themestyle); true }
            }
            "pref_key_system_cat_recents" -> findPreference<Preference>("pref_key_system_hidefromrecents_apps")?.setOnPreferenceClickListener(openAppsEdit)
            "pref_key_system_cat_betterpopups" -> {
                findPreference<Preference>("pref_key_system_betterpopups_allowfloat_apps")?.setOnPreferenceClickListener(openAppsBWEdit)
                findPreference<Preference>("pref_key_system_expandheadups_apps")?.setOnPreferenceClickListener(openAppsEdit)
            }
            "pref_key_system_cat_floatingwindows" -> findPreference<Preference>("pref_key_system_fw_forcein_actionsend_apps")?.setOnPreferenceClickListener(openAppsEdit)
            "pref_key_system_cat_applock" -> {
                findPreference<Preference>("pref_key_system_applock_list")?.setOnPreferenceClickListener {
                    openLockedAppEdit(this, 0)
                    true
                }
                findPreference<Preference>("pref_key_system_applock_skip_activities")?.setOnPreferenceClickListener(openActivitiesList)
            }
            "pref_key_system_cat_lockscreen" -> {
                findPreference<Preference>("pref_key_system_noscreenlock_cat")?.setOnPreferenceClickListener {
                    openSubFragment(System_NoScreenLock(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_noscreenlock_title, R.xml.prefs_system_noscreenlock)
                    true
                }
                findPreference<Preference>("pref_key_system_lockscreenshortcuts_cat")?.setOnPreferenceClickListener { openSystemSubFragment(it, true, R.xml.prefs_system_lockscreenshortcuts); true }
                findPreference<Preference>("pref_key_system_albumartonlock_cat")?.setOnPreferenceClickListener {
                    openSubFragment(SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_albumartonlock_title, R.xml.prefs_system_albumartonlock)
                    true
                }
                findPreference<Preference>("pref_key_system_charginginfo_cat")?.setOnPreferenceClickListener {
                    openSubFragment(SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_charginginfo_title, R.xml.prefs_system_charginginfo)
                    true
                }
                findPreference<Preference>("pref_key_system_lsalarm_cat")?.setOnPreferenceClickListener {
                    openSubFragment(SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_lsalarm_title, R.xml.prefs_system_alarmonlock)
                    true
                }
                findPreference<Preference>("pref_key_system_secureqs_cat")?.setOnPreferenceClickListener {
                    openSubFragment(SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_secureqs_title, R.xml.prefs_system_secureqs)
                    true
                }

                findPreference<Preference>("pref_key_system_credentials")?.setOnPreferenceChangeListener { _, newValue ->
                    val act = activity ?: return@setOnPreferenceChangeListener false
                    val pm = act.packageManager
                    val state = if (newValue == true) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    pm.setComponentEnabledSetting(ComponentName(act, CredentialsLauncher::class.java), state, PackageManager.DONT_KILL_APP)
                    true
                }

                activity?.let { act ->
                    findPreference<CheckBoxPreferenceEx>("pref_key_system_credentials")?.isChecked = act.packageManager.getComponentEnabledSetting(ComponentName(act, CredentialsLauncher::class.java)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                }

                if (Helpers.isDeviceEncrypted(context)) {
                    findPreference<CheckBoxPreferenceEx>("pref_key_system_nopassword")?.apply {
                        isChecked = false
                        setUnsupported(true)
                    }
                }
            }
            "pref_key_system_lockscreenshortcuts_cat" -> findPreference<Preference>("pref_key_system_lockscreenshortcuts_right")?.setOnPreferenceClickListener(openLockScreenActions)
            "pref_key_system_cat_other" -> {
                findPreference<Preference>("pref_key_system_forceclose_apps")?.setOnPreferenceClickListener(openAppsEdit)
                findPreference<Preference>("pref_key_system_nooverscroll_apps")?.setOnPreferenceClickListener(openAppsEdit)
                findPreference<Preference>("pref_key_system_cleanshare_apps")?.setOnPreferenceClickListener(openShareEdit)
                findPreference<Preference>("pref_key_system_cleanshare_test")?.setOnPreferenceClickListener {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "CustoMIUIzer is the best!")
                        type = "*/*"
                    }
                    context?.startActivity(Intent.createChooser(sendIntent, null))
                    true
                }

                findPreference<Preference>("pref_key_system_cleanopenwith_apps")?.setOnPreferenceClickListener(openOpenWithEdit)
                findPreference<Preference>("pref_key_system_cleanopenwith_test")?.setOnPreferenceClickListener {
                    AlertDialog.Builder(requireActivity()).apply {
                        setTitle(R.string.system_cleanopenwith_testdata)
                        setSingleChoiceItems(R.array.openwithtest, -1) { dialog, which ->
                            dialog.dismiss()
                            val type = when (which) {
                                0 -> "image/*"
                                1 -> "audio/*"
                                2 -> "video/*"
                                3 -> "text/*"
                                4 -> "application/zip"
                                else -> "*/*"
                            }
                            val viewIntent = Intent().apply {
                                action = Intent.ACTION_VIEW
                                setDataAndType(Uri.parse("content://${PrefsProvider.AUTHORITY}/test/$which"), type)
                            }
                            context.startActivity(Intent.createChooser(viewIntent, null))
                        }
                        setNeutralButton(android.R.string.cancel) { _, _ -> }
                        show()
                    }
                    true
                }

                findPreference<Preference>("pref_key_system_screenshot_cat")?.setOnPreferenceClickListener {
                    openSubFragment(System_ScreenshotConfig(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_screenshot_title, R.xml.prefs_system_screenshot)
                    true
                }

                AppHelper.appPrefs.edit().putInt("pref_key_system_animationscale_window", (Helpers.getAnimationScale(0) * 10).roundToInt()).apply()
                findPreference<SeekBarPreference>("pref_key_system_animationscale_window")?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        Helpers.setAnimationScale(0, seekBar.progress / 10f)
                    }
                })

                AppHelper.appPrefs.edit().putInt("pref_key_system_animationscale_transition", (Helpers.getAnimationScale(1) * 10).roundToInt()).apply()
                findPreference<SeekBarPreference>("pref_key_system_animationscale_transition")?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        Helpers.setAnimationScale(1, seekBar.progress / 10f)
                    }
                })

                AppHelper.appPrefs.edit().putInt("pref_key_system_animationscale_animator", (Helpers.getAnimationScale(2) * 10).roundToInt()).apply()
                findPreference<SeekBarPreference>("pref_key_system_animationscale_animator")?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        Helpers.setAnimationScale(2, seekBar.progress / 10f)
                    }
                })

                if (!checkAnimationPermission()) {
                    listOf("pref_key_system_animationscale_window", "pref_key_system_animationscale_transition", "pref_key_system_animationscale_animator").forEach { key ->
                        findPreference<Preference>(key)?.apply {
                            isEnabled = false
                            setSummary(R.string.launcher_privacyapps_fail)
                        }
                    }
                }
            }
            "pref_key_system_detailednetspeed_cat" -> {}
            "pref_key_system_statusbarcontrols_cat" -> {
                findPreference<Preference>("pref_key_system_statusbarcontrols_dt")?.setOnPreferenceClickListener(openStatusbarActions)
                findPreference<Preference>("pref_key_system_statusbarcontrols_dt_left")?.setOnPreferenceClickListener(openStatusbarActions)
                findPreference<Preference>("pref_key_system_statusbarcontrols_dt_right")?.setOnPreferenceClickListener(openStatusbarActions)
                findPreference<Preference>("pref_key_system_statusbarcontrols_longpress")?.setOnPreferenceClickListener(openStatusbarActions)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val key = when (requestCode) {
                0 -> "pref_key_system_shortcut_app"
                1 -> "pref_key_system_clock_app"
                2 -> "pref_key_system_calendar_app"
                else -> null
            }
            if (key != null) {
                AppHelper.appPrefs.edit()
                    .putString(key, data?.getStringExtra("app"))
                    .putInt(key + "_user", data?.getIntExtra("user", 0) ?: 0)
                    .apply()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkAnimationPermission(): Boolean {
        val pm = activity?.packageManager ?: return false
        return pm.checkPermission("android.permission.SET_ANIMATION_SCALE", Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED
    }
}
