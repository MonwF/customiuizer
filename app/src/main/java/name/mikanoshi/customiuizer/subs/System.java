package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.Preference;

import java.util.Objects;

import name.mikanoshi.customiuizer.MainFragment;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class System extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_noscreenlock_wifi").setEnabled(Objects.equals(Helpers.prefs.getString("pref_key_system_noscreenlock", "1"), "4"));
		findPreference("pref_key_system_noscreenlock").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_noscreenlock_wifi").setEnabled(newValue.equals("4"));
				return true;
			}
		});

		findPreference("pref_key_system_noscreenlock_wifi").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!Helpers.checkWiFiPerm(getActivity(), Helpers.REQUEST_PERMISSIONS_WIFI)) return false;
				openWifiNetworks();
				return true;
			}
		});

		findPreference("pref_key_system_popupnotif_apps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openAutoApps();
				return true;
			}
		});
	}

	public void openWifiNetworks() {
		Bundle args = new Bundle();
		args.putString("key", "pref_key_system_noscreenlock_wifi");
		openSubFragment(new WiFiList(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.wifi_networks, R.layout.prefs_wifi_networks);
	}

//	public void openBTNetworks() {
//		Bundle args = new Bundle();
//		args.putString("key", "pref_key_system_noscreenlock_bt");
//		openSubFragment(new BTList(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.bt_devices, R.layout.prefs_bt_networks);
//	}

	public void openAutoApps() {
		Bundle args = new Bundle();
		args.putString("key", "pref_key_system_popupnotif_apps");
		args.putBoolean("multi", true);
		AppSelector appSelector = new AppSelector();
		appSelector.setTargetFragment(System.this, 0);
		openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
	}

}
