package name.monwf.customiuizer.subs;

import android.os.Bundle;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;

import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.prefs.SeekBarPreference;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class Launcher extends SubFragment {

	@Override
	public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
		super.onCreatePreferences(savedInstanceState, rootKey);
		selectSub();
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Preference.OnPreferenceClickListener openPrivacyAppEdit = new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (!Helpers.checkPermAndRequest((AppCompatActivity) getActivity(), Helpers.ACCESS_SECURITY_CENTER, Helpers.REQUEST_PERMISSIONS_SECURITY_CENTER)) return false;
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

		int opt = AppHelper.getStringAsIntOfAppPrefs("pref_key_launcher_mods", 1);

		switch (sub) {
			case "pref_key_launcher_cat_folders":
				SeekBarPreference folderCols = findPreference("pref_key_launcher_folder_cols");
				findPreference("pref_key_launcher_folderwidth").setEnabled(AppHelper.getIntOfAppPrefs("pref_key_launcher_folder_cols", 1) > 1);
				findPreference("pref_key_launcher_folderspace").setEnabled(AppHelper.getIntOfAppPrefs("pref_key_launcher_folder_cols", 1) > 3);
				folderCols.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						findPreference("pref_key_launcher_folderwidth").setEnabled(seekBar.getProgress() > 0);
						findPreference("pref_key_launcher_folderspace").setEnabled(seekBar.getProgress() > 2);
					}
				});
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
				findPreference("pref_key_launcher_pinch").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_spread").setOnPreferenceClickListener(openLauncherActions);
				findPreference("pref_key_launcher_swipeup").setEnabled(opt == 1);
				break;
			case "pref_key_launcher_cat_privacyapps":
				findPreference("pref_key_launcher_cat_privacyapps").setEnabled(opt == 1);
				findPreference("pref_key_launcher_privacyapps_list").setOnPreferenceClickListener(openPrivacyAppEdit);
				break;
			case "pref_key_launcher_cat_titles":
				findPreference("pref_key_launcher_renameapps_list").setOnPreferenceClickListener(openLaunchableList);
				break;
			case "pref_key_launcher_cat_bugfixes":
				findPreference("pref_key_launcher_fixanim").setEnabled(opt == 1);
				break;
			case "pref_key_launcher_cat_other":
				findPreference("pref_key_launcher_unlockgrids").setEnabled(opt == 1);
				findPreference("pref_key_launcher_hideseekpoints").setEnabled(opt == 1);
				findPreference("pref_key_launcher_nounlockanim").setEnabled(opt == 1);
				findPreference("pref_key_launcher_oldlaunchanim").setEnabled(opt == 1);
				findPreference("pref_key_launcher_closedrawer").setEnabled(opt == 1);
				break;
		}
	}
}