package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import name.mikanoshi.customiuizer.MainFragment;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class CategorySelector extends SubFragment {

	String cat = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle args = getArguments();
		cat = args.getString("cat");
		if ("pref_key_system".equals(cat)) {
			toolbarMenu = true;
			activeMenus ="systemui";
		}
		else if ("pref_key_launcher".equals(cat)) {
			toolbarMenu = true;
			activeMenus ="launcher";
		}
		else {
			toolbarMenu = false;
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PreferenceScreen screen = findPreference("pref_key_cat");
		int cnt = screen.getPreferenceCount();
		for (int i = 0; i < cnt; i++) {
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

}