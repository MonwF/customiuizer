package name.mikanoshi.customiuizer.subs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Various_HiddenFeatures extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Activity act = getActivity();

		Preference updater = findPreference("pref_key_various_sysappsupdater");
		if (Helpers.isSysAppUpdaterInstalled(act)) updater.setSummary(R.string.various_sysappsupdater_summ2);
		updater.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!Helpers.launchActivity(act, "com.xiaomi.discover", "com.xiaomi.market.ui.UpdateAppsActivity", true))
				Helpers.openURL(act, "https://www.apkmirror.com/apk/xiaomi-inc/system-app-updater/");
				return true;
			}
		});

		PreferenceEx aosp = (PreferenceEx)findPreference("pref_key_various_memorystats");
		aosp.setCustomSummary("AOSP");
		aosp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Helpers.launchActivity(act, "com.android.settings", "com.android.settings.Settings$MemorySettingsActivity");
				return true;
			}
		});

		aosp = (PreferenceEx)findPreference("pref_key_various_runningservices");
		aosp.setCustomSummary("AOSP");
		aosp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.SubSettings"));
				intent.putExtra(":settings:show_fragment", "com.android.settings.applications.RunningServices");
				act.startActivity(intent);
				act.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
				return true;
			}
		});

		aosp = (PreferenceEx)findPreference("pref_key_various_appusagestats");
		aosp.setCustomSummary("AOSP");
		aosp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Helpers.launchActivity(act, "com.android.settings", "com.android.settings.UsageStatsActivity");
				return true;
			}
		});

		aosp = (PreferenceEx)findPreference("pref_key_various_aospsearch");
		aosp.setCustomSummary("AOSP");
		aosp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Helpers.launchActivity(act, "com.android.settings.intelligence", "com.android.settings.intelligence.search.SearchActivity");
				return true;
			}
		});

		aosp = (PreferenceEx)findPreference("pref_key_various_aospnotif");
		aosp.setCustomSummary("AOSP");
		aosp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!Helpers.launchActivity(act, "com.android.settings", "com.android.settings.Settings$AppAndNotificationDashboardActivity", true))
				Helpers.launchActivity(act, "com.android.settings", "com.android.settings.Settings$ConfigureNotificationSettingsActivity");
				return true;
			}
		});

		aosp = (PreferenceEx)findPreference("pref_key_various_aospnotiflog");
		aosp.setCustomSummary("AOSP");
		aosp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Helpers.launchActivity(act, "com.android.settings", "com.android.settings.Settings$NotificationStationActivity");
				return true;
			}
		});

		findPreference("pref_key_various_clearspeaker").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Helpers.launchActivity(act, "com.android.settings", "com.android.settings.Settings$SpeakerSettingsActivity");
				return true;
			}
		});

	}

}
