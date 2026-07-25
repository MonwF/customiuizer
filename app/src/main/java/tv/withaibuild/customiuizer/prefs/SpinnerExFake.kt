package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Pair
import tv.withaibuild.customiuizer.utils.AppHelper

class SpinnerExFake(context: Context, attrs: AttributeSet?) : SpinnerEx(context, attrs) {

    var value: String? = null
    private val others = ArrayList<Pair<String, String>>()

    fun addValue(key: String, newValue: String?) {
        val v = newValue ?: AppHelper.getStringOfAppPrefs(key, null)
        if (v != null) others.add(Pair(key, v))
    }

    fun addValue(key: String, newValue: Intent?) {
        val sVal = if (newValue == null) {
            AppHelper.getStringOfAppPrefs(key, null)
        } else {
            newValue.toUri(0)
        }
        if (sVal != null) others.add(Pair(key, sVal))
    }

    fun applyOthers() {
        if (others.isEmpty()) return
        val editor = AppHelper.appPrefs.edit()
        for (pref in others) {
            editor.putString(pref.first, pref.second)
        }
        editor.apply()
    }
}
