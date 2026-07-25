package tv.withaibuild.customiuizer.subs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.GetPathUtils

class System_ScreenshotConfig : SubFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val format = AppHelper.getStringOfAppPrefs("pref_key_system_screenshot_format", "1")
        findPreference<Preference>("pref_key_system_screenshot_quality")?.isEnabled = format == "2" || format == "4"
        findPreference<Preference>("pref_key_system_screenshot_format")?.setOnPreferenceChangeListener { _, newValue ->
            findPreference<Preference>("pref_key_system_screenshot_quality")?.isEnabled = newValue == "2" || newValue == "4"
            true
        }

        val path = AppHelper.getStringOfAppPrefs("pref_key_system_screenshot_path", "1")
        var dir = AppHelper.getStringOfAppPrefs("pref_key_system_screenshot_mypath", "")
        findPreference<Preference>("pref_key_system_screenshot_mypath")?.apply {
            isEnabled = path == "4"
            summary = dir
        }
        findPreference<Preference>("pref_key_system_screenshot_path")?.setOnPreferenceChangeListener { _, newValue ->
            findPreference<Preference>("pref_key_system_screenshot_mypath")?.isEnabled = newValue == "4"
            true
        }

        findPreference<Preference>("pref_key_system_screenshot_mypath")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(Intent.createChooser(intent, null), 0)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            var dir = GetPathUtils.getDirectoryPathFromUri(activity ?: return, data?.data)
            if (dir == null) dir = ""
            findPreference<Preference>("pref_key_system_screenshot_mypath")?.summary = dir
            AppHelper.appPrefs!!.edit().putString("pref_key_system_screenshot_mypath", dir).apply()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
