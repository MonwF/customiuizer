package name.monwf.customiuizer.subs;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.preference.Preference;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.prefs.CheckBoxPreferenceEx;
import name.monwf.customiuizer.utils.AppHelper;

public class Various extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_various_alarmcompat_apps").setOnPreferenceClickListener(openAppsEdit);

		findPreference("pref_key_various_calluibright_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new Various_CallUIBright(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.various_calluibright_title, R.xml.prefs_various_calluibright);
				return true;
			}
		});

		findPreference("pref_key_various_hiddenfeatures_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new Various_HiddenFeatures(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.various_hiddenfeatures_title, R.xml.prefs_various_hiddenfeatures);
				return true;
			}
		});

		try {
			final Activity act = getActivity();
			ApplicationInfo pkgInfo = act.getPackageManager().getApplicationInfo("com.miui.packageinstaller", PackageManager.MATCH_DISABLED_COMPONENTS);
			if (!pkgInfo.enabled) throw new Throwable();
		} catch (Throwable e) {
			CheckBoxPreferenceEx pref = findPreference("pref_key_various_miuiinstaller");
			pref.setChecked(false);
			pref.setUnsupported(true);
			pref.setSummary(R.string.various_miuiinstaller_error);
		}
	}

}
