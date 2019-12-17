package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.Preference;

import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class System_ScreenshotConfig extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String format = Helpers.prefs.getString("pref_key_system_screenshot_format", "1");
		findPreference("pref_key_system_screenshot_quality").setEnabled("2".equals(format) || "4".equals(format));
		findPreference("pref_key_system_screenshot_format").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				findPreference("pref_key_system_screenshot_quality").setEnabled("2".equals(newValue) || "4".equals(newValue));
				return true;
			}
		});
	}

}
