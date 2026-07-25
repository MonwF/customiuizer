package tv.withaibuild.customiuizer.subs

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.PreferenceEx
import tv.withaibuild.customiuizer.utils.Helpers

class Various_HiddenFeatures : SubFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val act = activity as? AppCompatActivity ?: return

        val batteryOptimization = findPreference<PreferenceEx>("pref_key_various_batteryoptimization")
        batteryOptimization?.setCustomSummary("AOSP")
        batteryOptimization?.setOnPreferenceClickListener {
            val intent = Intent("android.intent.action.MAIN").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                component = ComponentName("com.android.settings", "com.android.settings.SubSettings")
                putExtra(":settings:show_fragment", "com.android.settings.applications.manageapplications.ManageApplications")
                val bundle = Bundle().apply { putString("classname", "com.android.settings.Settings\$HighPowerApplicationsActivity") }
                putExtra(":settings:show_fragment_args", bundle)
            }
            act.startActivity(intent)
            act.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit)
            true
        }

        val runningServices = findPreference<PreferenceEx>("pref_key_various_runningservices")
        runningServices?.setCustomSummary("AOSP")
        runningServices?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                component = ComponentName("com.android.settings", "com.android.settings.SubSettings")
                putExtra(":settings:show_fragment", "com.android.settings.applications.RunningServices")
            }
            act.startActivity(intent)
            act.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit)
            true
        }

        val aospNotif = findPreference<PreferenceEx>("pref_key_various_aospnotif")
        aospNotif?.setCustomSummary("AOSP")
        aospNotif?.setOnPreferenceClickListener {
            if (!Helpers.launchActivity(act, "com.android.settings", "com.android.settings.Settings\$AppAndNotificationDashboardActivity", true)) {
                Helpers.launchActivity(act, "com.android.settings", "com.android.settings.Settings\$ConfigureNotificationSettingsActivity")
            }
            true
        }
    }
}
