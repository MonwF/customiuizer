package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.Preference;

import java.util.Objects;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class System_NoScreenLock extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_noscreenlock_wifi").setEnabled(Objects.equals(Helpers.prefs.getString("pref_key_system_noscreenlock", "1"), "3"));
		findPreference("pref_key_system_noscreenlock_bt").setEnabled(Objects.equals(Helpers.prefs.getString("pref_key_system_noscreenlock", "1"), "3"));
		findPreference("pref_key_system_noscreenlock").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_noscreenlock_wifi").setEnabled(newValue.equals("3"));
				findPreference("pref_key_system_noscreenlock_bt").setEnabled(newValue.equals("3"));
				return true;
			}
		});

		findPreference("pref_key_system_noscreenlock_wifi").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!Helpers.checkFinePerm(getActivity(), Helpers.REQUEST_PERMISSIONS_WIFI)) return false;
				openWifiNetworks();
				return true;
			}
		});

		findPreference("pref_key_system_noscreenlock_bt").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openBtNetworks();
				return true;
			}
		});
	}

	public void openWifiNetworks() {
		Bundle args = new Bundle();
		args.putString("key", "pref_key_system_noscreenlock_wifi");
		openSubFragment(new WiFiList(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.wifi_networks, R.layout.prefs_wifi_networks);
	}

	public void openBtNetworks() {
		Bundle args = new Bundle();
		args.putString("key", "pref_key_system_noscreenlock_bt");
		openSubFragment(new BTList(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.bt_devices, R.layout.prefs_bt_networks);
	}

}
