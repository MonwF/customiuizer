package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import tv.withaibuild.customiuizer.MainActivity
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.subs.ColorSelector
import tv.withaibuild.customiuizer.utils.AppHelper

class ColorPreferenceEx(context: Context, attrs: AttributeSet?) : PreferenceEx(context, attrs) {

    init {
        setOnPreferenceClickListener {
            val args = Bundle().apply { putString("key", key) }
            (context as? MainActivity)?.navToSubFragment(ColorSelector(), args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.Edit, title?.toString() ?: "", R.layout.fragment_selectcolor)
            true
        }
    }
}
