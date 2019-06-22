package name.mikanoshi.customiuizer.subs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.Settings;
import android.widget.SeekBar;

import java.util.Objects;

import name.mikanoshi.customiuizer.CredentialsLauncher;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.CheckBoxPreferenceEx;
import name.mikanoshi.customiuizer.prefs.ListPreferenceEx;
import name.mikanoshi.customiuizer.prefs.SeekBarPreference;
import name.mikanoshi.customiuizer.qs.QuickSettingsService;
import name.mikanoshi.customiuizer.utils.Helpers;

public class System extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_vibration_apps").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_system_vibration", "1"), "1"));
		findPreference("pref_key_system_vibration").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_vibration_apps").setEnabled(!newValue.equals("1"));
				return true;
			}
		});

		findPreference("pref_key_system_vibration_apps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openApps("pref_key_system_vibration_apps");
				return true;
			}
		});

		findPreference("pref_key_system_expandnotifs_apps").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_system_expandnotifs", "1"), "1"));
		findPreference("pref_key_system_expandnotifs").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_expandnotifs_apps").setEnabled(!newValue.equals("1"));
				return true;
			}
		});

		findPreference("pref_key_system_expandnotifs_apps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openApps("pref_key_system_expandnotifs_apps");
				return true;
			}
		});

		findPreference("pref_key_system_hidefromrecents_apps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openApps("pref_key_system_hidefromrecents_apps");
				return true;
			}
		});

		findPreference("pref_key_system_popupnotif_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new System_PopupNotif(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_popupnotif_title, R.xml.prefs_system_popupnotif);
				return true;
			}
		});

		findPreference("pref_key_system_detailednetspeed_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new SubFragment(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_detailednetspeed_title, R.xml.prefs_system_detailednetspeed);
				return true;
			}
		});

		findPreference("pref_key_system_noscreenlock_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new System_NoScreenLock(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_noscreenlock_title, R.xml.prefs_system_noscreenlock);
				return true;
			}
		});

		findPreference("pref_key_system_credentials").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PackageManager pm = getActivity().getPackageManager();
				if ((Boolean)newValue)
					pm.setComponentEnabledSetting(new ComponentName(getActivity(), CredentialsLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				else
					pm.setComponentEnabledSetting(new ComponentName(getActivity(), CredentialsLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				return true;
			}
		});

		((CheckBoxPreferenceEx)findPreference("pref_key_system_credentials")).setChecked(getActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(getActivity(), CredentialsLauncher.class)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

		findPreference("pref_key_system_orientationlock").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PackageManager pm = getActivity().getPackageManager();
				if ((Boolean)newValue)
					pm.setComponentEnabledSetting(new ComponentName(getActivity(), QuickSettingsService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				else
					pm.setComponentEnabledSetting(new ComponentName(getActivity(), QuickSettingsService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				return true;
			}
		});

		((SeekBarPreference)findPreference("pref_key_system_qqsgridcolumns")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (!fromUser) return;
				if (progress < 3) progress = 5;
				Settings.Secure.putInt(getActivity().getContentResolver(), "sysui_qqs_count", progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		findPreference("pref_key_system_shortcut_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle args = new Bundle();
				args.putString("key", "pref_key_system_shortcut_app");
				args.putBoolean("standalone", true);
				AppSelector appSelector = new AppSelector();
				appSelector.setTargetFragment(System.this, 0);
				openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
				return true;
			}
		});

		findPreference("pref_key_system_clock_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle args = new Bundle();
				args.putString("key", "pref_key_system_clock_app");
				args.putBoolean("standalone", true);
				AppSelector appSelector = new AppSelector();
				appSelector.setTargetFragment(System.this, 1);
				openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
				return true;
			}
		});

		findPreference("pref_key_system_calendar_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle args = new Bundle();
				args.putString("key", "pref_key_system_calendar_app");
				args.putBoolean("standalone", true);
				AppSelector appSelector = new AppSelector();
				appSelector.setTargetFragment(System.this, 2);
				openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
				return true;
			}
		});

		findPreference("pref_key_system_statusbarcolor_apps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openApps("pref_key_system_statusbarcolor_apps");
				return true;
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			((ListPreferenceEx)findPreference("pref_key_system_autogroupnotif")).setUnsupported(true);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			String key = null;
			if (requestCode == 0) key = "pref_key_system_shortcut_app";
			else if (requestCode == 1) key = "pref_key_system_clock_app";
			else if (requestCode == 2) key = "pref_key_system_calendar_app";
			if (key != null) Helpers.prefs.edit().putString(key, data.getStringExtra("activity")).apply();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}