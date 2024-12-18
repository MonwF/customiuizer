package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import androidx.preference.Preference;
import android.text.format.DateFormat;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.widget.TimePicker;

import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Various_CallUIBright extends SubFragment {

	final String key = "pref_key_various_calluibright_night_";
	TimePickerDialog startTimePicker;
	TimePickerDialog endTimePicker;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Calendar time = Calendar.getInstance();
		boolean is24 = DateFormat.is24HourFormat(getActivity());

		int start_hour = Helpers.prefs.getInt(key + "start_hour", time.get(Calendar.HOUR_OF_DAY));
		int start_minute = Helpers.prefs.getInt(key + "start_minute", 0);
		updateStartTime(start_hour, start_minute);
		startTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
				Helpers.prefs.edit().putInt(key + "start_hour", hourOfDay).putInt(key + "start_minute", minutes).apply();
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

		int end_hour = Helpers.prefs.getInt(key + "end_hour", time.get(Calendar.HOUR_OF_DAY));
		int end_minute = Helpers.prefs.getInt(key + "end_minute", 0);
		updateEndTime(end_hour, end_minute);
		endTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
				Helpers.prefs.edit().putInt(key + "end_hour", hourOfDay).putInt(key + "end_minute", minutes).apply();
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
