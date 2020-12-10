package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import name.mikanoshi.customiuizer.MainFragment;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class CategorySelector extends SubFragment {

	String cat = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		cat = args.getString("cat");

		if ("pref_key_system".equals(cat)) {
			if (!Helpers.is12()) {
				Preference pref = findPreference("pref_key_system_cat_floatingwindows");
				if (pref != null) ((PreferenceScreen)findPreference("pref_key_cat")).removePreference(pref);
			}
		}

		PreferenceScreen screen = (PreferenceScreen)findPreference("pref_key_cat");
		int cnt = screen.getPreferenceCount();
		for (int i = 0; i < cnt; i++)
		screen.getPreference(i).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!(preference instanceof PreferenceEx)) return false;
				Bundle bundle = new Bundle();
				bundle.putString("sub", preference.getKey());
				MainFragment mainFrag = ((MainFragment)getTargetFragment());
				switch (cat) {
					case "pref_key_system":
						openSubFragment(mainFrag.prefSystem, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_mods, R.xml.prefs_system);
						break;
					case "pref_key_launcher":
						openSubFragment(mainFrag.prefLauncher, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.launcher_title, R.xml.prefs_launcher);
						break;
					case "pref_key_controls":
						openSubFragment(mainFrag.prefControls, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.controls_mods, R.xml.prefs_controls);
						break;
				}
				return true;
			}
		});
	}

}