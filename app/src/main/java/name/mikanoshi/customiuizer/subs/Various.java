package name.mikanoshi.customiuizer.subs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
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
				} catch (PackageManager.NameNotFoundException e) {
					Helpers.openURL(getActivity(), "https://www.apkmirror.com/apk/xiaomi-inc/system-app-updater/");
				}

				return true;
			}
		});

	}

}
