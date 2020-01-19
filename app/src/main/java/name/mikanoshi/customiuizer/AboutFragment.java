package name.mikanoshi.customiuizer;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

import name.mikanoshi.customiuizer.utils.Helpers;

public class AboutFragment extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();

		findPreference("pref_key_website").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://code.highspec.ru/Mikanoshi/CustoMIUIzer");
				return true;
			}
		});

		findPreference("pref_key_xda").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://forum.xda-developers.com/xposed/modules/mod-customiuizer-customize-miui-rom-t3910732");
				return true;
			}
		});

		findPreference("pref_key_4pda").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://4pda.ru/forum/index.php?showtopic=945275");
				return true;
			}
		});

		//Add version name to support title
		View view = getView();
		if (view != null) try {
			TextView version = view.findViewById(R.id.about_version);
			version.setText(String.format(getResources().getString(R.string.about_version), act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName));
			if (Helpers.currentHoliday == Helpers.Holidays.NEWYEAR) view.findViewById(R.id.santa_hat).setVisibility(View.VISIBLE);
			else if (Helpers.currentHoliday == Helpers.Holidays.LUNARNEWYEAR) {
				view.findViewById(R.id.lunar_animal).setVisibility(View.VISIBLE);
				view.findViewById(R.id.decoration).setVisibility(View.VISIBLE);
			}
		} catch (Throwable e) {
			//Shouldn't happen...
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (getView() == null) return;
		getView().findViewById(R.id.miuizer_icon).setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
		super.onConfigurationChanged(newConfig);
	}

}
