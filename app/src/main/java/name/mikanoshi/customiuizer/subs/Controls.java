package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import android.widget.Toast;

import java.util.Objects;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Controls extends SubFragment {

	@Override
	public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
		super.onCreatePreferences(savedInstanceState, rootKey);
		selectSub();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		switch (sub) {
			case "pref_key_controls_cat_power":
				findPreference("pref_key_controls_powerdt").setOnPreferenceClickListener(openLaunchActions);
				break;
			case "pref_key_controls_cat_volume":
				findPreference("pref_key_controls_volumecursor_apps").setOnPreferenceClickListener(openAppsEdit);
				break;
			case "pref_key_controls_cat_navbar":
				findPreference("pref_key_controls_backlong").setOnPreferenceClickListener(openNavbarActions);
				findPreference("pref_key_controls_homelong").setOnPreferenceClickListener(openNavbarActions);
				findPreference("pref_key_controls_menulong").setOnPreferenceClickListener(openNavbarActions);

				findPreference("pref_key_controls_navbarleft").setOnPreferenceClickListener(openNavbarActions);
				findPreference("pref_key_controls_navbarleftlong").setOnPreferenceClickListener(openNavbarActions);
				findPreference("pref_key_controls_navbarright").setOnPreferenceClickListener(openNavbarActions);
				findPreference("pref_key_controls_navbarrightlong").setOnPreferenceClickListener(openNavbarActions);
				break;
			case "pref_key_controls_cat_fingerprint":
				findPreference("pref_key_controls_fingerprint1").setOnPreferenceClickListener(openControlsActions);
				findPreference("pref_key_controls_fingerprint2").setOnPreferenceClickListener(openControlsActions);
				findPreference("pref_key_controls_fingerprintlong").setOnPreferenceClickListener(openControlsActions);

				findPreference("pref_key_controls_fingerprintsuccess_ignore").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_controls_fingerprintsuccess", "1"), "1"));
				findPreference("pref_key_controls_fingerprintsuccess").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						findPreference("pref_key_controls_fingerprintsuccess_ignore").setEnabled(!newValue.equals("1"));
						return true;
					}
				});

				findPreference("pref_key_controls_fingerprint_accept").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						String other = Helpers.prefs.getString("pref_key_controls_fingerprint_reject", "1");
						if (newValue.equals(other)) {
							Helpers.prefs.edit().putString("pref_key_controls_fingerprint_reject", "1").apply();
							String msg = getResources().getString(R.string.controls_fingerprint_conflict) + " " + getResources().getString(R.string.controls_fingerprint_conflict_reset_reject);
							Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
							getActivity().recreate();
						}
						return true;
					}
				});

				findPreference("pref_key_controls_fingerprint_reject").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						String other = Helpers.prefs.getString("pref_key_controls_fingerprint_accept", "1");
						if (newValue.equals(other)) {
							Helpers.prefs.edit().putString("pref_key_controls_fingerprint_accept", "1").apply();
							String msg = getResources().getString(R.string.controls_fingerprint_conflict) + " " + getResources().getString(R.string.controls_fingerprint_conflict_reset_accept);
							Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
							getActivity().recreate();
						}
						return true;
					}
				});
				
				break;
			case "pref_key_controls_cat_fsg":
				findPreference("pref_key_controls_fsg_horiz_apps").setOnPreferenceClickListener(openAppsEdit);
				findPreference("pref_key_controls_fsg_assist").setOnPreferenceClickListener(openNavbarActions);
				findPreference("pref_key_controls_fsg_swipeandstop").setOnPreferenceClickListener(openNavbarActions);
				break;
		}
	}

}