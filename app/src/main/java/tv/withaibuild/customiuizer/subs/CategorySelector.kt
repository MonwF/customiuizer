package tv.withaibuild.customiuizer.subs

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import tv.withaibuild.customiuizer.MainFragment
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.PreferenceEx
import tv.withaibuild.customiuizer.utils.AppHelper

class CategorySelector : SubFragment() {

    private var cat: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        cat = arguments?.getString("cat")
        when (cat) {
            "pref_key_system" -> {
                toolbarMenu = true
                activeMenus = "systemui"
            }
            "pref_key_launcher" -> {
                toolbarMenu = true
                activeMenus = "launcher"
            }
            else -> toolbarMenu = false
        }
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val screen = findPreference<PreferenceScreen>("pref_key_cat")
        val cnt = screen?.preferenceCount ?: 0
        for (i in 0 until cnt) {
            screen?.getPreference(i)?.setOnPreferenceClickListener { preference ->
                if (preference !is PreferenceEx) return@setOnPreferenceClickListener false
                val bundle = Bundle().apply { putString("sub", preference.key) }
                val mainFrag = targetFragment as? MainFragment ?: return@setOnPreferenceClickListener false
                when (cat) {
                    "pref_key_system" -> openSubFragment(mainFrag.prefSystem, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_mods, R.xml.prefs_system)
                    "pref_key_launcher" -> openSubFragment(mainFrag.prefLauncher, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.launcher_title, R.xml.prefs_launcher)
                    "pref_key_controls" -> openSubFragment(mainFrag.prefControls, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.controls_mods, R.xml.prefs_controls)
                }
                true
            }
        }
    }
}
