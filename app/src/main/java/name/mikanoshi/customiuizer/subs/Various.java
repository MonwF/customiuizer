package name.mikanoshi.customiuizer.subs;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.preference.Preference;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.CheckBoxPreferenceEx;
import name.mikanoshi.customiuizer.prefs.ListPreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Various extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Activity act = getActivity();

		findPreference("pref_key_various_alarmcompat_apps").setOnPreferenceClickListener(openAppsEdit);

		findPreference("pref_key_various_calluibright_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new Various_CallUIBright(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.various_calluibright_title, R.xml.prefs_various_calluibright);
				return true;
			}
		});

		findPreference("pref_key_various_callreminder_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new Various_CallReminder(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.various_callreminder_title, R.xml.prefs_various_callreminder);
				return true;
			}
		});

		findPreference("pref_key_various_hiddenfeatures_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new Various_HiddenFeatures(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.various_hiddenfeatures_title, R.xml.prefs_various_hiddenfeatures);
				return true;
			}
		});

		if (!Helpers.is12()) {
			ListPreferenceEx pref = (ListPreferenceEx)findPreference("pref_key_various_collapsemiuititles");
			pref.setUnsupported(true);
		}

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
						Helpers.openURL(getActivity(), "https://www.apkmirror.com/apk/xiaomi-inc/xiaomi-package-installer/");
						return true;
					} else return false;
				}
			});
		}
	}

}
