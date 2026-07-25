package tv.withaibuild.customiuizer.subs

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.utils.AppHelper

class System_BatteryIndicator : SubFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val colorval = AppHelper.getStringOfAppPrefs("pref_key_system_batteryindicator_color", "1")
        findPreference<Preference>("pref_key_system_batteryindicator_colorval1")?.isEnabled = colorval != "3"
        findPreference<Preference>("pref_key_system_batteryindicator_colorval2")?.isEnabled = colorval != "3"
        findPreference<Preference>("pref_key_system_batteryindicator_colorval3")?.isEnabled = colorval != "3"
        findPreference<Preference>("pref_key_system_batteryindicator_colorval4")?.isEnabled = colorval != "3"
        findPreference<Preference>("pref_key_system_batteryindicator_color")?.setOnPreferenceChangeListener { _, newValue ->
            findPreference<Preference>("pref_key_system_batteryindicator_colorval1")?.isEnabled = newValue != "3"
            findPreference<Preference>("pref_key_system_batteryindicator_colorval2")?.isEnabled = newValue != "3"
            findPreference<Preference>("pref_key_system_batteryindicator_colorval3")?.isEnabled = newValue != "3"
            findPreference<Preference>("pref_key_system_batteryindicator_colorval4")?.isEnabled = newValue != "3"
            true
        }

        findPreference<Preference>("pref_key_system_batteryindicator_test")?.setOnPreferenceClickListener {
            activity?.sendBroadcast(Intent("tv.withaibuild.customiuizer.mods.BatteryIndicatorTest"))
            true
        }
    }
}
