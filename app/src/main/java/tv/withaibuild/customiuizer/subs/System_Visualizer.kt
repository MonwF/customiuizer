package tv.withaibuild.customiuizer.subs

import android.os.Bundle
import androidx.preference.Preference
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.utils.AppHelper

class System_Visualizer : SubFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        findPreference<Preference>("pref_key_system_visualizer_colorval")?.isEnabled = "2" == AppHelper.getStringOfAppPrefs("pref_key_system_visualizer_color", "1")
        findPreference<Preference>("pref_key_system_visualizer_dyntime")?.isEnabled = "5" == AppHelper.getStringOfAppPrefs("pref_key_system_visualizer_color", "1")
        findPreference<Preference>("pref_key_system_visualizer_color")?.setOnPreferenceChangeListener { _, newValue ->
            findPreference<Preference>("pref_key_system_visualizer_colorval")?.isEnabled = "2" == newValue
            findPreference<Preference>("pref_key_system_visualizer_dyntime")?.isEnabled = "5" == newValue
            true
        }
    }
}
