package name.monwf.customiuizer.subs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;

import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.GetPathUtils;

public class System_ScreenshotConfig extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String format = AppHelper.getStringOfAppPrefs("pref_key_system_screenshot_format", "1");
		findPreference("pref_key_system_screenshot_quality").setEnabled("2".equals(format) || "4".equals(format));
		findPreference("pref_key_system_screenshot_format").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_screenshot_quality").setEnabled("2".equals(newValue) || "4".equals(newValue));
				return true;
			}
		});

		String path = AppHelper.getStringOfAppPrefs("pref_key_system_screenshot_path", "1");
		String dir = AppHelper.getStringOfAppPrefs("pref_key_system_screenshot_mypath", "");
		findPreference("pref_key_system_screenshot_mypath").setEnabled("4".equals(path));
		findPreference("pref_key_system_screenshot_mypath").setSummary(dir);
		findPreference("pref_key_system_screenshot_path").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_screenshot_mypath").setEnabled("4".equals(newValue));
				return true;
			}
		});

		findPreference("pref_key_system_screenshot_mypath").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				startActivityForResult(Intent.createChooser(intent, null), 0);
				return true;
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == 0) {
			String dir = GetPathUtils.getDirectoryPathFromUri(getActivity(), data.getData());
			if (dir == null) dir = "";
			findPreference("pref_key_system_screenshot_mypath").setSummary(dir);
			AppHelper.appPrefs.edit().putString("pref_key_system_screenshot_mypath", dir).apply();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
