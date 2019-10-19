package name.mikanoshi.customiuizer.subs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Launcher extends SubFragment {

	String sub = "";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		sub = args.getString("sub");
		if (sub == null) sub = "";

		selectSub("pref_key_launcher", sub);

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

		@SuppressWarnings("ConstantConditions")
		int opt = Integer.parseInt(Helpers.prefs.getString("pref_key_launcher_mods", "1"));

		switch (sub) {
			case "pref_key_launcher_cat_folders":
				break;
			case "pref_key_launcher_cat_gestures":
				findPreference("pref_key_launcher_swipedown").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_swipedown2").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_swipeup").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_swipeup2").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_swiperight").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_swipeleft").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_shake").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_doubletap").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_swipeup").setEnabled(opt == 1);
				break;
			case "pref_key_launcher_cat_privacyapps":
				findPreference("pref_key_launcher_cat_privacyapps").setEnabled(opt == 1);
				findPreference("pref_key_launcher_privacyapps_list").setOnPreferenceClickListener(openPrivacyAppEdit);

				if (!checkPermissions()) {
					Preference pref = findPreference("pref_key_launcher_cat_privacyapps");
					pref.setEnabled(false);
					pref.setTitle(R.string.launcher_privacyapps_fail);
					findPreference("pref_key_launcher_privacyapps_list").setEnabled(false);
				}

				break;
			case "pref_key_launcher_cat_titles":
				findPreference("pref_key_launcher_renameapps_list").setOnPreferenceClickListener(openLaunchableList);
				break;
			case "pref_key_launcher_cat_other":
				findPreference("pref_key_launcher_fixstatusbarmode").setEnabled(opt == 1);
				findPreference("pref_key_launcher_unlockgrids").setEnabled(opt == 1);
				findPreference("pref_key_launcher_hideseekpoints").setEnabled(opt == 1);
				findPreference("pref_key_launcher_googlediscover").setEnabled(opt == 1);
				break;
		}
	}

	private boolean checkPermissions() {
		PackageManager pm = getActivity().getPackageManager();
		return pm.checkPermission(Manifest.permission.WRITE_SECURE_SETTINGS, Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED &&
			   pm.checkPermission(Helpers.ACCESS_SECURITY_CENTER, Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
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
//			getActivity().sendBroadcast(new Intent(GlobalActions.ACTION_PREFIX + "RestartLauncher"));
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