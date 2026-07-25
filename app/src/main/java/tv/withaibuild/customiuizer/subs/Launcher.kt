package tv.withaibuild.customiuizer.subs

import android.os.Bundle
import android.widget.SeekBar
import androidx.preference.Preference
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.SeekBarPreference
import tv.withaibuild.customiuizer.utils.AppHelper

class Launcher : SubFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        selectSub()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val openPrivacyAppEdit = Preference.OnPreferenceClickListener {
            openPrivacyAppEdit(this, 0)
            true
        }

        val openLaunchableList = Preference.OnPreferenceClickListener {
            openLaunchableList(it, this, 0)
            true
        }

        when (sub) {
            "pref_key_launcher_cat_folders" -> {
                val folderCols = findPreference<SeekBarPreference>("pref_key_launcher_folder_cols")
                findPreference<Preference>("pref_key_launcher_folderwidth")?.isEnabled = AppHelper.getIntOfAppPrefs("pref_key_launcher_folder_cols", 1) > 1
                findPreference<Preference>("pref_key_launcher_folderspace")?.isEnabled = AppHelper.getIntOfAppPrefs("pref_key_launcher_folder_cols", 1) > 3
                folderCols?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        findPreference<Preference>("pref_key_launcher_folderwidth")?.isEnabled = seekBar.progress > 0
                        findPreference<Preference>("pref_key_launcher_folderspace")?.isEnabled = seekBar.progress > 2
                    }
                })
            }
            "pref_key_launcher_cat_gestures" -> {
                findPreference<Preference>("pref_key_launcher_swipedown")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_swipedown2")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_swipeup")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_swipeup2")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_swiperight")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_swipeleft")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_shake")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_doubletap")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_pinch")?.setOnPreferenceClickListener(openLauncherActions)
                findPreference<Preference>("pref_key_launcher_spread")?.setOnPreferenceClickListener(openLauncherActions)
            }
            "pref_key_launcher_cat_privacyapps" -> findPreference<Preference>("pref_key_launcher_privacyapps_list")?.setOnPreferenceClickListener(openPrivacyAppEdit)
            "pref_key_launcher_cat_titles" -> findPreference<Preference>("pref_key_launcher_renameapps_list")?.setOnPreferenceClickListener(openLaunchableList)
        }
    }
}
