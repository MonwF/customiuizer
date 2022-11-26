package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;

import java.util.Objects;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.ListPreferenceEx;
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
				if (!Helpers.checkFinePerm((AppCompatActivity) getActivity(), Helpers.REQUEST_PERMISSIONS_WIFI)) return false;
				openWifiNetworks();
				return true;
			}
		});

		findPreference("pref_key_system_noscreenlock_bt").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!Helpers.checkBluetoothPerm((AppCompatActivity) getActivity(), Helpers.REQUEST_PERMISSIONS_BLUETOOTH)) return false;
				openBtNetworks();
				return true;
			}
		});

		if (Helpers.isDeviceEncrypted(getContext())) {
			ListPreferenceEx req = (ListPreferenceEx)findPreference("pref_key_system_noscreenlock_req");
			req.setValue("3");
			req.setEnabled(false);
		}
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
