package tv.withaibuild.customiuizer.subs

import android.os.Bundle
import android.widget.SeekBar
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.SeekBarPreference

class System_AutoBrightness : SubFragment() {

    private var maxBrightness: SeekBarPreference? = null
    private var minBrightness: SeekBarPreference? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        maxBrightness = findPreference("pref_key_system_autobrightness_max")
        minBrightness = findPreference("pref_key_system_autobrightness_min")
        minBrightness?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                if ((maxBrightness?.getValue() ?: 0) <= progress) {
                    maxBrightness?.setValue(progress + 1)
                }
                maxBrightness?.setMinValue(progress + 1)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}
