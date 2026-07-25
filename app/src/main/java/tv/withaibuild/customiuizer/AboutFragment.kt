package tv.withaibuild.customiuizer

import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.preference.Preference
import java.util.Locale

class AboutFragment : SubFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headLayoutId = R.layout.fragment_about_head
        tailLayoutId = R.layout.fragment_about_tail
    }

    override fun fixStubLayout(view: View?, postion: Int) {
        if (postion == 2) {
            val lp = view?.layoutParams as? RelativeLayout.LayoutParams ?: return
            lp.addRule(RelativeLayout.BELOW, android.R.id.list_container)
            view.layoutParams = lp
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val locales = arrayOf("zh-CN", "ru-RU", "ja-JP", "vi-VN", "cs-CZ", "pt-BR", "tr-TR", "es-ES")

        val localesArr = ArrayList<String>(locales.asList())
        val localeNames = ArrayList<SpannableString>()
        localesArr.add(0, "en")
        for (locale in localesArr) try {
            val loc = Locale.forLanguageTag(locale)
            val locStr: StringBuilder
            val locSpanString: SpannableString
            if (locale == "zh-TW") {
                locStr = StringBuilder("繁體中文 (台灣)")
            } else {
                locStr = StringBuilder(loc.getDisplayLanguage(loc))
                locStr.setCharAt(0, Character.toUpperCase(locStr[0]))
                if (locale == "pt-BR") {
                    locStr.append(" (Brasil)")
                }
            }
            locSpanString = SpannableString(locStr.toString())
            localeNames.add(locSpanString)
        } catch (t: Throwable) {
            localeNames.add(SpannableString(Locale.getDefault().getDisplayLanguage(Locale.getDefault())))
        }

        localesArr.add(0, "auto")
        localeNames.add(0, SpannableString(getString(R.string.array_system_default)))

        val locale = findPreference<tv.withaibuild.customiuizer.prefs.ListPreferenceEx>("pref_key_miuizer_locale")
        locale?.entries = localeNames.toTypedArray()
        locale?.entryValues = localesArr.toTypedArray()
        locale?.setOnPreferenceChangeListener { _, _ ->
            activity?.recreate()
            true
        }

        // Add version name to support title
        val view = view
        if (view != null) try {
            val version = view.findViewById<TextView>(R.id.about_version)
            val versionName = validContext.packageManager.getPackageInfo(validContext.packageName, 0).versionName
            version?.text = String.format(Locale.US, getString(R.string.about_version), versionName)
        } catch (e: Throwable) {
            // Shouldn't happen...
            e.printStackTrace()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (view == null) return
        view?.findViewById<View>(R.id.miuizer_icon)?.visibility =
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) View.GONE else View.VISIBLE
        super.onConfigurationChanged(newConfig)
    }
}
