package name.monwf.customiuizer.subs;

import android.os.Bundle;
import android.os.Handler;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.prefs.ListPreferenceEx;

public class System_AirplaneModeConfig extends SubFragment {
	ArrayList<String> radios;
	ArrayList<String> radios_toggle;

	Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			new Handler(getContext().getMainLooper()).post(System_AirplaneModeConfig.this::processValues);
			return true;
		}
	};

	private void processValues() {
		radios.clear();
		radios_toggle.clear();
		PreferenceScreen screen = getPreferenceScreen();
		for (int i = 0; i < screen.getPreferenceCount(); i++) {
			if (!(screen.getPreference(i) instanceof ListPreferenceEx)) continue;
			ListPreferenceEx pref = ((ListPreferenceEx)screen.getPreference(i));
			String dev = null;
			switch (pref.getKey()) {
				case "pref_key_system_airplanemodeconfig_cell": dev = "cell"; break;
				case "pref_key_system_airplanemodeconfig_bt": dev = "bluetooth"; break;
				case "pref_key_system_airplanemodeconfig_wifi": dev = "wifi"; break;
				case "pref_key_system_airplanemodeconfig_nfc": dev = "nfc"; break;
				case "pref_key_system_airplanemodeconfig_wimax": dev = "wimax"; break;
			}
			if (dev == null) continue;
			String val = pref.getValue();
			if ("1".equals(val)) {
				radios.add(dev);
				radios_toggle.add(dev);
			} else if ("2".equals(val)) radios.add(dev);
		}
		Settings.Global.putString(getActivity().getContentResolver(), "airplane_mode_radios", TextUtils.join(",", radios));
		Settings.Global.putString(getActivity().getContentResolver(), "airplane_mode_toggleable_radios",  TextUtils.join(",", radios_toggle));
	}

	private void setupPref(String name, String dev) {
		ListPreferenceEx pref = (ListPreferenceEx)findPreference(name);
		if (radios.contains(dev) && radios_toggle.contains(dev)) pref.setValue("1");
		else if (radios.contains(dev)) pref.setValue("2");
		else pref.setValue("0");
		pref.setOnPreferenceChangeListener(listener);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		try {
			radios = new ArrayList<String>(Arrays.asList(Settings.Global.getString(getActivity().getContentResolver(), "airplane_mode_radios").split(",")));
		} catch (Throwable t) {
			radios = new ArrayList<String>();
		}
		try {
			radios_toggle = new ArrayList<String>(Arrays.asList(Settings.Global.getString(getActivity().getContentResolver(), "airplane_mode_toggleable_radios").split(",")));
		} catch (Throwable t) {
			radios_toggle = new ArrayList<String>();
		}

		setupPref("pref_key_system_airplanemodeconfig_cell", "cell");
		setupPref("pref_key_system_airplanemodeconfig_bt", "bluetooth");
		setupPref("pref_key_system_airplanemodeconfig_wifi", "wifi");
		setupPref("pref_key_system_airplanemodeconfig_nfc", "nfc");
		setupPref("pref_key_system_airplanemodeconfig_wimax", "wimax");
		findPreference("pref_key_system_airplanemodeconfig_note").setEnabled(false);
	}
}
