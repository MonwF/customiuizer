package name.monwf.customiuizer.subs;

import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;

import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.utils.AppHelper;

public class System_BatteryIndicator extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String colorval = AppHelper.getStringOfAppPrefs("pref_key_system_batteryindicator_color", "1");
		findPreference("pref_key_system_batteryindicator_colorval1").setEnabled(!"3".equals(colorval));
		findPreference("pref_key_system_batteryindicator_colorval2").setEnabled(!"3".equals(colorval));
		findPreference("pref_key_system_batteryindicator_colorval3").setEnabled(!"3".equals(colorval));
		findPreference("pref_key_system_batteryindicator_colorval4").setEnabled(!"3".equals(colorval));
		findPreference("pref_key_system_batteryindicator_color").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_batteryindicator_colorval1").setEnabled(!"3".equals(newValue));
				findPreference("pref_key_system_batteryindicator_colorval2").setEnabled(!"3".equals(newValue));
				findPreference("pref_key_system_batteryindicator_colorval3").setEnabled(!"3".equals(newValue));
				findPreference("pref_key_system_batteryindicator_colorval4").setEnabled(!"3".equals(newValue));
				return true;
			}
		});

		findPreference("pref_key_system_batteryindicator_colorval1").setOnPreferenceClickListener(openColorSelector);
		findPreference("pref_key_system_batteryindicator_colorval2").setOnPreferenceClickListener(openColorSelector);
		findPreference("pref_key_system_batteryindicator_colorval3").setOnPreferenceClickListener(openColorSelector);
		findPreference("pref_key_system_batteryindicator_colorval4").setOnPreferenceClickListener(openColorSelector);

		findPreference("pref_key_system_batteryindicator_test").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				getActivity().sendBroadcast(new Intent("name.monwf.customiuizer.mods.BatteryIndicatorTest"));
				return true;
			}
		});
	}

}