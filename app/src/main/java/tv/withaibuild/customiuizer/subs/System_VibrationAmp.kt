package tv.withaibuild.customiuizer.subs

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.preference.Preference
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.PreferenceEx
import tv.withaibuild.customiuizer.utils.AppHelper
import java.util.Calendar

class System_VibrationAmp : SubFragment() {

    private val key = "pref_key_system_vibration_amp_period_"
    private var startTimePicker: TimePickerDialog? = null
    private var endTimePicker: TimePickerDialog? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val time = Calendar.getInstance()
        val is24 = DateFormat.is24HourFormat(activity)

        val startHour = AppHelper.getIntOfAppPrefs(key + "start_hour", time.get(Calendar.HOUR_OF_DAY))
        val startMinute = AppHelper.getIntOfAppPrefs(key + "start_minute", 0)
        updateStartTime(startHour, startMinute)
        startTimePicker = TimePickerDialog(activity, { _, hourOfDay, minutes ->
            AppHelper.appPrefs.edit().putInt(key + "start_hour", hourOfDay).putInt(key + "start_minute", minutes).apply()
            updateStartTime(hourOfDay, minutes)
        }, startHour, startMinute, is24)

        findPreference<Preference>(key + "start")?.setOnPreferenceClickListener {
            startTimePicker?.show()
            true
        }

        val endHour = AppHelper.getIntOfAppPrefs(key + "end_hour", time.get(Calendar.HOUR_OF_DAY))
        val endMinute = AppHelper.getIntOfAppPrefs(key + "end_minute", 0)
        updateEndTime(endHour, endMinute)
        endTimePicker = TimePickerDialog(activity, { _, hourOfDay, minutes ->
            AppHelper.appPrefs.edit().putInt(key + "end_hour", hourOfDay).putInt(key + "end_minute", minutes).apply()
            updateEndTime(hourOfDay, minutes)
        }, endHour, endMinute, is24)

        findPreference<Preference>(key + "end")?.setOnPreferenceClickListener {
            endTimePicker?.show()
            true
        }
    }

    private fun updateStartTime(hr: Int, min: Int) {
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hr)
            set(Calendar.MINUTE, min)
        }
        findPreference<PreferenceEx>(key + "start")?.setCustomSummary(DateFormat.getTimeFormat(activity).format(time.time))
    }

    private fun updateEndTime(hr: Int, min: Int) {
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hr)
            set(Calendar.MINUTE, min)
        }
        findPreference<PreferenceEx>(key + "end")?.setCustomSummary(DateFormat.getTimeFormat(activity).format(time.time))
    }
}
