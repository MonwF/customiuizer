package name.mikanoshi.customiuizer.subs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Launcher extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//setupImmersiveMenu();

		findPreference("pref_key_launcher_mods").setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateLauncherMods((String)newValue);
				return true;
			}
		});

		CheckBoxPreference.OnPreferenceChangeListener switchPrivacyAppState = new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Settings.Secure.putInt(getActivity().getContentResolver(), "is_privacy_apps_enable", (boolean)newValue ? 1 : 0);
				return true;
			}
		};

		Preference.OnPreferenceClickListener openPrivacyAppEdit = new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openPrivacyAppEdit(Launcher.this, 0);
				return true;
			}
		};

		Preference.OnPreferenceClickListener openLaunchableList = new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openLaunchableList(preference, Launcher.this, 0);
				return true;
			}
		};

		findPreference("pref_key_launcher_swipedown").setOnPreferenceClickListener(openLauncherActions);
		findPreference("pref_key_launcher_swipedown2").setOnPreferenceClickListener(openLauncherActions);
		findPreference("pref_key_launcher_swipeup").setOnPreferenceClickListener(openLauncherActions);
		findPreference("pref_key_launcher_swipeup2").setOnPreferenceClickListener(openLauncherActions);
		findPreference("pref_key_launcher_swiperight").setOnPreferenceClickListener(openLauncherActions);
		findPreference("pref_key_launcher_swipeleft").setOnPreferenceClickListener(openLauncherActions);
		findPreference("pref_key_launcher_shake").setOnPreferenceClickListener(openLauncherActions);
		findPreference("pref_key_launcher_doubletap").setOnPreferenceClickListener(openLauncherActions);

		findPreference("pref_key_launcher_privacyapps_list").setOnPreferenceClickListener(openPrivacyAppEdit);
		findPreference("pref_key_launcher_privacyapps_gest").setOnPreferenceChangeListener(switchPrivacyAppState);
		findPreference("pref_key_launcher_renameapps_list").setOnPreferenceClickListener(openLaunchableList);

		if (!checkPermissions()) {
			Preference pref = findPreference("pref_key_launcher_privacyapps");
			pref.setEnabled(false);
			pref.setTitle(R.string.launcher_privacyapps_fail);
			findPreference("pref_key_launcher_privacyapps_list").setEnabled(false);
			findPreference("pref_key_launcher_privacyapps_gest").setEnabled(false);
		}

		updateLauncherMods(Helpers.prefs.getString("pref_key_launcher_mods", "1"));
	}

	@Override
	public void onResume() {
		super.onResume();

		if (checkPermissions()) {
			Preference pref = findPreference("pref_key_launcher_privacyapps_gest");
			((CheckBoxPreference)pref).setChecked(Settings.Secure.getInt(getActivity().getContentResolver(), "is_privacy_apps_enable", 0) == 1);
		}
	}

	private boolean checkPermissions() {
		PackageManager pm = getActivity().getPackageManager();
		return pm.checkPermission(Manifest.permission.WRITE_SECURE_SETTINGS, Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED &&
			   pm.checkPermission(Helpers.ACCESS_SECURITY_CENTER, Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
	}

	private void updateLauncherMods(String value) {
		int opt = Integer.parseInt(value);
		findPreference("pref_key_launcher_fixstatusbarmode").setEnabled(opt == 1);
		findPreference("pref_key_launcher_unlockgrids").setEnabled(opt == 1);
		findPreference("pref_key_launcher_foldershade").setEnabled(opt == 1);
		findPreference("pref_key_launcher_privacyapps").setEnabled(opt == 1);
		findPreference("pref_key_launcher_swipeup").setEnabled(opt == 1);
		findPreference("pref_key_launcher_hideseekpoints").setEnabled(opt == 1);
	}

//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu_launcher, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if (item.getItemId() == R.id.restartlauncher)
//		try {
//			getActivity().sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.RestartLauncher"));
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	private void setupImmersiveMenu() {
//		ActionBar actionBar = getActionBar();
//		if (actionBar != null) actionBar.showSplitActionBar(false, false);
//		setImmersionMenuEnabled(true);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		setupImmersiveMenu();
//	}

}