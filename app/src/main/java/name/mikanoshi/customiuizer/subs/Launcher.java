package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.Helpers;

public class Launcher extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//setupImmersiveMenu();

		CheckBoxPreference.OnPreferenceClickListener openSwipeEdit = new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Bundle args = new Bundle();
				args.putString("key", preference.getKey());
				openSubFragment(new MultiAction(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, preference.getTitleRes(), R.layout.prefs_swipe_gestures);
				return true;
			}
		};

		Preference swipePref;
		swipePref = findPreference("pref_key_launcher_swipedown");
		swipePref.setOnPreferenceClickListener(openSwipeEdit);
		swipePref = findPreference("pref_key_launcher_swipedown2");
		swipePref.setOnPreferenceClickListener(openSwipeEdit);
		swipePref = findPreference("pref_key_launcher_swipeup");
		swipePref.setOnPreferenceClickListener(openSwipeEdit);
		swipePref = findPreference("pref_key_launcher_swipeup2");
		swipePref.setOnPreferenceClickListener(openSwipeEdit);
		swipePref = findPreference("pref_key_launcher_swiperight");
		swipePref.setOnPreferenceClickListener(openSwipeEdit);
		swipePref = findPreference("pref_key_launcher_swipeleft");
		swipePref.setOnPreferenceClickListener(openSwipeEdit);
		swipePref = findPreference("pref_key_launcher_shake");
		swipePref.setOnPreferenceClickListener(openSwipeEdit);
	}

//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu_launcher, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if (item.getItemId() == R.id.restartlauncher)
//		try {
//			getActivity().sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.RestartLauncher"));
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	private void setupImmersiveMenu() {
//		ActionBar actionBar = getActionBar();
//		if (actionBar != null) actionBar.showSplitActionBar(false, false);
//		setImmersionMenuEnabled(true);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		setupImmersiveMenu();
//	}

}