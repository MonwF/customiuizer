package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.Preference;

import name.mikanoshi.customiuizer.SubFragment;

public class Controls extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Preference actionPref;
		actionPref = findPreference("pref_key_controls_backlong");
		actionPref.setOnPreferenceClickListener(openNavbarActions);
		actionPref = findPreference("pref_key_controls_homelong");
		actionPref.setOnPreferenceClickListener(openNavbarActions);
		actionPref = findPreference("pref_key_controls_menulong");
		actionPref.setOnPreferenceClickListener(openNavbarActions);

		actionPref = findPreference("pref_key_controls_navbarleft");
		actionPref.setOnPreferenceClickListener(openNavbarActions);
		actionPref = findPreference("pref_key_controls_navbarleftlong");
		actionPref.setOnPreferenceClickListener(openNavbarActions);
		actionPref = findPreference("pref_key_controls_navbarright");
		actionPref.setOnPreferenceClickListener(openNavbarActions);
		actionPref = findPreference("pref_key_controls_navbarrightlong");
		actionPref.setOnPreferenceClickListener(openNavbarActions);

		actionPref = findPreference("pref_key_controls_fingerprint1");
		actionPref.setOnPreferenceClickListener(openControlsActions);
		actionPref = findPreference("pref_key_controls_fingerprint2");
		actionPref.setOnPreferenceClickListener(openControlsActions);
		actionPref = findPreference("pref_key_controls_fingerprintlong");
		actionPref.setOnPreferenceClickListener(openControlsActions);
	}

}