package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Controls extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		CheckBoxPreference.OnPreferenceClickListener openActionEdit = new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle args = new Bundle();
				args.putString("key", preference.getKey());
				openSubFragment(new MultiAction(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, preference.getTitleRes(), R.layout.prefs_navbar_actions);
				return true;
			}
		};

		Preference actionPref;
		actionPref = findPreference("pref_key_controls_backlong");
		actionPref.setOnPreferenceClickListener(openActionEdit);
		actionPref = findPreference("pref_key_controls_homelong");
		actionPref.setOnPreferenceClickListener(openActionEdit);
		actionPref = findPreference("pref_key_controls_menulong");
		actionPref.setOnPreferenceClickListener(openActionEdit);

		actionPref = findPreference("pref_key_controls_navbarleft");
		actionPref.setOnPreferenceClickListener(openActionEdit);
		actionPref = findPreference("pref_key_controls_navbarleftlong");
		actionPref.setOnPreferenceClickListener(openActionEdit);
		actionPref = findPreference("pref_key_controls_navbarright");
		actionPref.setOnPreferenceClickListener(openActionEdit);
		actionPref = findPreference("pref_key_controls_navbarrightlong");
		actionPref.setOnPreferenceClickListener(openActionEdit);

		CheckBoxPreference.OnPreferenceClickListener openActionEdit2 = new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle args = new Bundle();
				args.putString("key", preference.getKey());
				openSubFragment(new MultiAction(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, preference.getTitleRes(), R.layout.prefs_controls_gestures);
				return true;
			}
		};

		actionPref = findPreference("pref_key_controls_fingerprint1");
		actionPref.setOnPreferenceClickListener(openActionEdit2);
		actionPref = findPreference("pref_key_controls_fingerprint2");
		actionPref.setOnPreferenceClickListener(openActionEdit2);
		actionPref = findPreference("pref_key_controls_fingerprintlong");
		actionPref.setOnPreferenceClickListener(openActionEdit2);
	}

}