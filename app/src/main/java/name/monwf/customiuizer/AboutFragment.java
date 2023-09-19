package name.monwf.customiuizer;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.preference.Preference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import name.monwf.customiuizer.utils.Helpers;

public class AboutFragment extends SubFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		headLayoutId = R.layout.fragment_about_head;
		tailLayoutId = R.layout.fragment_about_tail;
	}

	@Override
	protected void fixStubLayout(View view, int postion) {
		if (postion == 2) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
			lp.addRule(RelativeLayout.BELOW, android.R.id.list_container);
			view.setLayoutParams(lp);
		}
	}

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
		boolean isLangRu = getResources().getConfiguration().locale.getISO3Language().contains("ru");
		findPreference("pref_key_payother").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				if (isLangRu)
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

		if (isLangRu) {
			Preference pref4pda = findPreference("pref_key_4pda");
			pref4pda.setVisible(true);
			pref4pda.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference pref) {
					Helpers.openURL(act, "https://4pda.ru/forum/index.php?showtopic=945275");
					return true;
				}
			});
		}

		//Add version name to support title
		View view = getView();
		if (view != null) try {
			TextView version = view.findViewById(R.id.about_version);
			String versionName = BuildConfig.VERSION_NAME;
			if (BuildConfig.BUILD_TYPE.equals("develop")) {
				SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd", Locale.getDefault());
				Date buildDate = new Date(BuildConfig.BUILD_TIME);
				versionName = formatter.format(buildDate) + "-test";
			}
			version.setText(String.format(getResources().getString(R.string.about_version), versionName));
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
