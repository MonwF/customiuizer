package tv.withaibuild.customiuizer.subs

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.Preference
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.CheckBoxPreferenceEx
import tv.withaibuild.customiuizer.utils.AppHelper

class Various : SubFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        findPreference<Preference>("pref_key_various_alarmcompat_apps")?.setOnPreferenceClickListener(openAppsEdit)

        findPreference<Preference>("pref_key_various_calluibright_cat")?.setOnPreferenceClickListener {
            openSubFragment(Various_CallUIBright(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.various_calluibright_title, R.xml.prefs_various_calluibright)
            true
        }

        findPreference<Preference>("pref_key_various_hiddenfeatures_cat")?.setOnPreferenceClickListener {
            openSubFragment(Various_HiddenFeatures(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.various_hiddenfeatures_title, R.xml.prefs_various_hiddenfeatures)
            true
        }

        try {
            val act = activity ?: throw Throwable()
            val pkgInfo = act.packageManager.getApplicationInfo("com.miui.packageinstaller", PackageManager.MATCH_DISABLED_COMPONENTS)
            if (!pkgInfo.enabled) throw Throwable()
        } catch (e: Throwable) {
            val pref = findPreference<CheckBoxPreferenceEx>("pref_key_various_miuiinstaller")
            pref?.isChecked = false
            pref?.setUnsupported(true)
            pref?.setSummary(R.string.various_miuiinstaller_error)
        }
    }
}
