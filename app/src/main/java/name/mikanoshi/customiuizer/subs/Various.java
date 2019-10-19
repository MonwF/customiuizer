package name.mikanoshi.customiuizer.subs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.CheckBoxPreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Various extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Activity act = getActivity();

		Preference updater = findPreference("pref_key_various_sysappsupdater");
		if (Helpers.isSysAppUpdaterInstalled(act)) updater.setSummary(R.string.various_sysappsupdater_summ2);

		updater.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				PackageManager pm = act.getPackageManager();
				try {
					pm.getPackageInfo("com.xiaomi.discover", PackageManager.GET_ACTIVITIES);
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_DEFAULT);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					intent.setComponent(new ComponentName("com.xiaomi.discover", "com.xiaomi.market.ui.UpdateAppsActivity"));
					act.startActivity(intent);
					act.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
				} catch (Throwable t) {
					Helpers.openURL(getActivity(), "https://www.apkmirror.com/apk/xiaomi-inc/system-app-updater/");
				}
				return true;
			}
		});

		findPreference("pref_key_various_alarmcompat_apps").setOnPreferenceClickListener(openAppsEdit);

		findPreference("pref_key_various_calluibright_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new System_CallUIBright(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.various_calluibright_title, R.xml.prefs_various_calluibright);
				return true;
			}
		});

		findPreference("pref_key_various_callreminder_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new System_CallReminder(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.various_callreminder_title, R.xml.prefs_various_callreminder);
				return true;
			}
		});

		try {
			ApplicationInfo pkgInfo = act.getPackageManager().getApplicationInfo("com.miui.packageinstaller", PackageManager.MATCH_DISABLED_COMPONENTS);
			if (!pkgInfo.enabled) throw new Throwable();
		} catch (Throwable e) {
			CheckBoxPreferenceEx pref = (CheckBoxPreferenceEx)findPreference("pref_key_various_miuiinstaller");
			pref.setChecked(false);
			pref.setUnsupported(true);
			pref.setSummary(R.string.various_miuiinstaller_error);
			if (getView() == null) return;
			ListView list = getView().findViewById(android.R.id.list);
			list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					if (!view.isEnabled()) {
						Helpers.openURL(getActivity(), "https://www.apkmirror.com/apk/xiaomi-inc/package-installer-3/");
						return true;
					} else return false;
				}
			});
		}
	}

}
