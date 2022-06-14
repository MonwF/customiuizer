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

		findPreference("pref_key_paycrypto").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://code.highspec.ru/cryptodonate");
				return true;
			}
		});

		findPreference("pref_key_payother").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			@SuppressWarnings("deprecation")
			public boolean onPreferenceClick(Preference pref) {
				if (getResources().getConfiguration().locale.getISO3Language().contains("ru"))
					Helpers.openURL(act, "https://mikanoshi.name/donate/");
				else
					Helpers.openURL(act, "https://en.mikanoshi.name/donate/");
				return true;
			}
		});

		findPreference("pref_key_xda").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://forum.xda-developers.com/t/mod-xposed-3-2-1-customiuizer-customize-your-miui-rom.3910732/");
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
