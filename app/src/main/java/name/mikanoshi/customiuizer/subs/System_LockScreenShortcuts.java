package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.Preference;

import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.CheckBoxPreferenceEx;

public class System_LockScreenShortcuts extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_lockscreenshortcuts_right").setOnPreferenceClickListener(openLockScreenActions);

		boolean leftSwipeDisabled = ((CheckBoxPreferenceEx)findPreference("pref_key_system_lockscreenshortcuts_right_off")).isChecked();
		findPreference("pref_key_system_lockscreenshortcuts_right").setEnabled(!leftSwipeDisabled);
		findPreference("pref_key_system_lockscreenshortcuts_right_image").setEnabled(!leftSwipeDisabled);
		findPreference("pref_key_system_lockscreenshortcuts_right_off").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_lockscreenshortcuts_right").setEnabled(!(boolean)newValue);
				findPreference("pref_key_system_lockscreenshortcuts_right_image").setEnabled(!(boolean)newValue);
				return true;
			}
		});

		findPreference("pref_key_system_lockscreenshortcuts_left").setOnPreferenceClickListener(openSortableList);

		boolean rightSwipeDisabled = ((CheckBoxPreferenceEx)findPreference("pref_key_system_lockscreenshortcuts_left_off")).isChecked();
		findPreference("pref_key_system_lockscreenshortcuts_left").setEnabled(!rightSwipeDisabled);
		findPreference("pref_key_system_lockscreenshortcuts_left_skiplock").setEnabled(!rightSwipeDisabled);
		findPreference("pref_key_system_lockscreenshortcuts_left_off").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_lockscreenshortcuts_left").setEnabled(!(boolean)newValue);
				findPreference("pref_key_system_lockscreenshortcuts_left_skiplock").setEnabled(!(boolean)newValue);
				return true;
			}
		});
	}

}
