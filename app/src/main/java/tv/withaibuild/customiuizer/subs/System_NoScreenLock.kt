package tv.withaibuild.customiuizer.subs

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.ListPreferenceEx
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers

class System_NoScreenLock : SubFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val noScreenLock = AppHelper.getStringOfAppPrefs("pref_key_system_noscreenlock", "1")
        findPreference<Preference>("pref_key_system_noscreenlock_wifi")?.isEnabled = noScreenLock == "3"
        findPreference<Preference>("pref_key_system_noscreenlock_bt")?.isEnabled = noScreenLock == "3"
        findPreference<Preference>("pref_key_system_noscreenlock")?.setOnPreferenceChangeListener { _, newValue ->
            findPreference<Preference>("pref_key_system_noscreenlock_wifi")?.isEnabled = newValue == "3"
            findPreference<Preference>("pref_key_system_noscreenlock_bt")?.isEnabled = newValue == "3"
            true
        }

        findPreference<Preference>("pref_key_system_noscreenlock_wifi")?.setOnPreferenceClickListener {
            if (!Helpers.checkPermAndRequest(activity as? AppCompatActivity ?: return@setOnPreferenceClickListener false, Manifest.permission.ACCESS_FINE_LOCATION, Helpers.REQUEST_PERMISSIONS_WIFI)) return@setOnPreferenceClickListener false
            openWifiNetworks()
            true
        }

        findPreference<Preference>("pref_key_system_noscreenlock_bt")?.setOnPreferenceClickListener {
            if (!Helpers.checkPermAndRequest(activity as? AppCompatActivity ?: return@setOnPreferenceClickListener false, Manifest.permission.BLUETOOTH_CONNECT, Helpers.REQUEST_PERMISSIONS_BLUETOOTH)) return@setOnPreferenceClickListener false
            openBtNetworks()
            true
        }

        if (Helpers.isDeviceEncrypted(context)) {
            val req = findPreference<ListPreferenceEx>("pref_key_system_noscreenlock_req")
            req?.setValue("3")
            req?.isEnabled = false
        }
    }

    fun openWifiNetworks() {
        val args = Bundle().apply { putString("key", "pref_key_system_noscreenlock_wifi") }
        openSubFragment(WiFiList(), args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.wifi_networks, R.layout.prefs_wifi_networks)
    }

    fun openBtNetworks() {
        val args = Bundle().apply { putString("key", "pref_key_system_noscreenlock_bt") }
        openSubFragment(BTList(), args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.bt_devices, R.layout.prefs_bt_networks)
    }
}
