package name.monwf.customiuizer;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import name.monwf.customiuizer.prefs.ListPreferenceEx;
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

		String[] locales = new String[] { "zh-CN", "ru-RU", "ja-JP", "vi-VN", "cs-CZ", "pt-BR", "tr-TR", "es-ES" };

		ArrayList<String> localesArr = new ArrayList<String>(Arrays.asList(locales));
		ArrayList<SpannableString> localeNames = new ArrayList<SpannableString>();
		localesArr.add(0, "en");
		for (String locale: localesArr) try {
			Locale loc = Locale.forLanguageTag(locale);
			StringBuilder locStr;
			SpannableString locSpanString;
			if (locale.equals("zh-TW")) {
				locStr = new StringBuilder("繁體中文 (台灣)");
			}
			else {
				locStr = new StringBuilder(loc.getDisplayLanguage(loc));
				locStr.setCharAt(0, Character.toUpperCase(locStr.charAt(0)));
				if (locale.equals("pt-BR")) {
					locStr.append(" (Brasil)");
				}
			}
			locSpanString = new SpannableString(locStr.toString());
			localeNames.add(locSpanString);
		} catch (Throwable t) {
			localeNames.add(new SpannableString(Locale.getDefault().getDisplayLanguage(Locale.getDefault())));
		}

		localesArr.add(0, "auto");
		localeNames.add(0, new SpannableString(getString(R.string.array_system_default)));

		ListPreferenceEx locale = findPreference("pref_key_miuizer_locale");
		locale.setEntries(localeNames.toArray(new CharSequence[0]));
		locale.setEntryValues(localesArr.toArray(new CharSequence[0]));
		locale.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				getActivity().recreate();
				return true;
			}
		});

		//Add version name to support title
		View view = getView();
		if (view != null) try {
			TextView version = view.findViewById(R.id.about_version);
			String versionName = getValidContext().getPackageManager()
				.getPackageInfo(getValidContext().getPackageName(), 0).versionName;
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
