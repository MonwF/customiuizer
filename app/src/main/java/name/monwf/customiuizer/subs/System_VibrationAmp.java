package name.monwf.customiuizer.subs;

import android.os.Bundle;
import androidx.preference.Preference;
import android.text.format.DateFormat;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.widget.TimePicker;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.prefs.PreferenceEx;
import name.monwf.customiuizer.utils.AppHelper;

public class System_VibrationAmp extends SubFragment {

	final String key = "pref_key_system_vibration_amp_period_";
	TimePickerDialog startTimePicker;
	TimePickerDialog endTimePicker;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Calendar time = Calendar.getInstance();
		boolean is24 = DateFormat.is24HourFormat(getActivity());

		int start_hour = AppHelper.getIntOfAppPrefs(key + "start_hour", time.get(Calendar.HOUR_OF_DAY));
		int start_minute = AppHelper.getIntOfAppPrefs(key + "start_minute", 0);
		updateStartTime(start_hour, start_minute);
		startTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
				AppHelper.appPrefs.edit().putInt(key + "start_hour", hourOfDay).putInt(key + "start_minute", minutes).apply();
				updateStartTime(hourOfDay, minutes);
			}
		}, start_hour, start_minute, is24);

		findPreference(key + "start").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startTimePicker.show();
				return true;
			}
		});

		int end_hour = AppHelper.getIntOfAppPrefs(key + "end_hour", time.get(Calendar.HOUR_OF_DAY));
		int end_minute = AppHelper.getIntOfAppPrefs(key + "end_minute", 0);
		updateEndTime(end_hour, end_minute);
		endTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
				AppHelper.appPrefs.edit().putInt(key + "end_hour", hourOfDay).putInt(key + "end_minute", minutes).apply();
				updateEndTime(hourOfDay, minutes);
			}
		}, end_hour, end_minute, is24);

		findPreference(key + "end").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				endTimePicker.show();
				return true;
			}
		});
	}

	void updateStartTime(int hr, int min) {
		Calendar time = Calendar.getInstance();
		time.set(Calendar.HOUR_OF_DAY, hr);
		time.set(Calendar.MINUTE, min);
		((PreferenceEx)findPreference(key + "start")).setCustomSummary(DateFormat.getTimeFormat(getActivity()).format(time.getTime()));
	}

	void updateEndTime(int hr, int min) {
		Calendar time = Calendar.getInstance();
		time.set(Calendar.HOUR_OF_DAY, hr);
		time.set(Calendar.MINUTE, min);
		((PreferenceEx)findPreference(key + "end")).setCustomSummary(DateFormat.getTimeFormat(getActivity()).format(time.getTime()));
	}

}
