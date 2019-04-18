package name.mikanoshi.customiuizer;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.TextView;

import name.mikanoshi.customiuizer.utils.Helpers;

public class AboutFragment extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();

		Preference miuizerSitePreference = findPreference("pref_key_website");
		miuizerSitePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://code.highspec.ru/Mikanoshi/CustoMIUIzer");
				return true;
			}
		});

		//Add version name to support title
		if (getView() != null) try {
			TextView version = getView().findViewById(R.id.about_version);
			version.setText(String.format(getResources().getString(R.string.about_version), act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName));
		} catch (Throwable e) {
			//Shouldn't happen...
			e.printStackTrace();
		}
	}

}
