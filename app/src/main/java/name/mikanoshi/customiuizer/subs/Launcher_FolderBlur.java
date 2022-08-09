package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;

import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.PreferenceCategoryEx;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Launcher_FolderBlur extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		int ver = Helpers.getMIUILauncherVersion(getContext());
		((PreferenceCategoryEx)findPreference("pref_key_launcher_folderbackblur")).setUnsupported(ver > 41601600);
		((PreferenceCategoryEx)findPreference("pref_key_launcher_folderwallblur")).setUnsupported(ver > 41601600 && ver < 422003000);
	}

}
