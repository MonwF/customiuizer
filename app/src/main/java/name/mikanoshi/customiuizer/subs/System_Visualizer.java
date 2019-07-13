package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.Preference;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class System_Visualizer extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_visualizer_colorval").setEnabled("2".equals(Helpers.prefs.getString("pref_key_system_visualizer_color", "1")));
		findPreference("pref_key_system_visualizer_color").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_visualizer_colorval").setEnabled("2".equals(newValue));
				return true;
			}
		});

		findPreference("pref_key_system_visualizer_colorval").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle args = new Bundle();
				args.putString("key", preference.getKey());
				openSubFragment(new ColorSelector(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, preference.getTitleRes(), R.layout.fragment_selectcolor);
				return true;
			}
		});
	}

}
