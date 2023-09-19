package name.monwf.customiuizer.subs;

import android.os.Bundle;
import androidx.preference.Preference;

import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.utils.AppHelper;

public class System_Visualizer extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_visualizer_colorval").setEnabled("2".equals(AppHelper.getStringOfAppPrefs("pref_key_system_visualizer_color", "1")));
		findPreference("pref_key_system_visualizer_dyntime").setEnabled("5".equals(AppHelper.getStringOfAppPrefs("pref_key_system_visualizer_color", "1")));
		findPreference("pref_key_system_visualizer_color").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_visualizer_colorval").setEnabled("2".equals(newValue));
				findPreference("pref_key_system_visualizer_dyntime").setEnabled("5".equals(newValue));
				return true;
			}
		});

		findPreference("pref_key_system_visualizer_colorval").setOnPreferenceClickListener(openColorSelector);
	}

}
