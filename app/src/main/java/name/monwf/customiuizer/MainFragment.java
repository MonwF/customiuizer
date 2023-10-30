package name.monwf.customiuizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import name.monwf.customiuizer.prefs.ListPreferenceEx;
import name.monwf.customiuizer.prefs.PreferenceEx;
import name.monwf.customiuizer.subs.CategorySelector;
import name.monwf.customiuizer.subs.Controls;
import name.monwf.customiuizer.subs.Launcher;
import name.monwf.customiuizer.subs.System;
import name.monwf.customiuizer.subs.Various;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;
import name.monwf.customiuizer.utils.ModData;
import name.monwf.customiuizer.utils.ModSearchAdapter;

public class MainFragment extends PreferenceFragmentBase {

	private final CategorySelector catSelector = new CategorySelector();
	public System prefSystem = new System();
	public Launcher prefLauncher = new Launcher();
	public Controls prefControls = new Controls();
	public Various prefVarious = new Various();
	private Menu mActionMenu;
	private RecyclerView listView = null;
	private ListView resultView = null;
	boolean isSearchFocused = false;
	int inSearchView = 0;
	String lastFilter;

	private final Runnable showUpdateNotification = new Runnable() {
		@Override
		public void run() {
			if (getView() != null) try {
				ImageView alert = getView().findViewById(R.id.update_alert);
				if (alert != null) alert.setVisibility(View.VISIBLE);
			} catch (Throwable e) {}
		}
	};

	private final Runnable hideUpdateNotification = new Runnable() {
		@Override
		public void run() {
			if (getView() != null) try {
				ImageView alert = getView().findViewById(R.id.update_alert);
				if (alert != null) alert.setVisibility(View.GONE);
			} catch (Throwable e) {}
		}
	};

	private boolean isFragmentReady(AppCompatActivity act) {
		return act != null && !act.isFinishing() && MainFragment.this.isAdded();
	}

	@Override
	@SuppressLint("MissingSuperCall")
	public void onCreate(Bundle savedInstanceState) {
		toolbarMenu = true;
		activeMenus = "all";
		super.onCreate(savedInstanceState, R.xml.prefs_main);
		tailLayoutId = R.layout.prefs_main12;
		final AppCompatActivity act = (AppCompatActivity) getActivity();

		// Preventing launch delay
		new Thread(() -> {
			Helpers.getAllMods(act, savedInstanceState != null);
		}).start();

		checkModuleIsActive();
	}

	private void checkModuleIsActive() {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				final AppCompatActivity act = (AppCompatActivity) getActivity();
				if (isFragmentReady(act) && !AppHelper.moduleActive) {
					act.runOnUiThread(new Runnable() {
						public void run() {
							showXposedDialog(act);
						}
					});
				}
			}
		}, 800);
	}

	@Override
	public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
		super.onCreatePreferences(savedInstanceState, rootKey);
		setPreferencesFromResource(R.xml.prefs_main, rootKey);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		mActionMenu = menu;
		MenuItem searchMenuItem = mActionMenu.findItem(R.id.search_btn);

		SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
		MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem searchItem) {
				MenuItem item;
				for (int i = 0; i < mActionMenu.size(); i++) {
					item = mActionMenu.getItem(i);
					item.setVisible(item.getItemId() != R.id.edit_confirm && item.getItemId() != R.id.openinweb);
				}
				return true;
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem searchItem) {
				MenuItem item = null;
				for (int i = 0; i < mActionMenu.size(); i++) {
					item = mActionMenu.getItem(i);
					item.setVisible(item.getItemId() == R.id.search_btn);
				}
				return true;
			}
		});
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (newText.length() > 0) {
					inSearchView = 1;
				}
				findMod(newText);
				return false;
			}
		});
		searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				isSearchFocused = hasFocus;
			}
		});
		if (inSearchView == 2) {
			MenuItemCompat.expandActionView(searchMenuItem);
			searchView.setQuery(lastFilter, false);
			searchView.clearFocus();
		}
	}

	@Override
	protected void fixStubLayout(View view, int postion) {
		if (postion == 2) {
			ViewGroup.LayoutParams lp = view.getLayoutParams();
			lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
			view.setLayoutParams(lp);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.app_name);

		if (getView() == null) return;

		resultView = getView().findViewById(R.id.custom);
		resultView.setDivider(null);
		resultView.setDividerHeight(0);
		resultView.setAdapter(new ModSearchAdapter(getActivity()));
		resultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				inSearchView = 2;
				ModData mod = (ModData)parent.getAdapter().getItem(position);
				openModCat(mod.cat.name(), mod.sub, mod.key);
			}
		});
		resultView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if (isSearchFocused) {
					isSearchFocused = false;
					Handler handler = new Handler(v.getContext().getMainLooper());
					handler.postDelayed(() -> {
						Helpers.hideKeyboard((AppCompatActivity) getActivity(), getView());
					}, getResources().getInteger(android.R.integer.config_shortAnimTime));
					resultView.requestFocus();
				}
				return false;
			}
		});

		listView = getListView();
		final Activity act = getActivity();

//		PreferenceEx warning = findPreference("pref_key_warning");
//		if (warning != null) {
//			getPreferenceScreen().removePreference(warning);
//		}

		findPreference("pref_key_miuizer_launchericon").setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PackageManager pm = act.getPackageManager();
				if ((Boolean)newValue)
					pm.setComponentEnabledSetting(new ComponentName(act, GateWayLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				else
					pm.setComponentEnabledSetting(new ComponentName(act, GateWayLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				return true;
			}
		});

		String[] locales = new String[] { "zh-CN", "zh-TW", "ru-RU", "ja-JP", "vi-VN", "cs-CZ", "pt-BR", "tr-TR", "es-ES" };

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

		findPreference("pref_key_github").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Configuration config = act.getResources().getConfiguration();
				String lang = config.getLocales().get(0).getLanguage();
				if ("zh".equals(lang)) {
					Helpers.openURL(act, "https://github.com/MonwF/customiuizer/blob/main/README_zh.md");
				}
				else if ("ja".equals(lang)) {
					Helpers.openURL(act, "https://github.com/MonwF/customiuizer/blob/main/README_jp.md");
				}
				else if ("pt".equals(lang)) {
					Helpers.openURL(act, "https://github.com/MonwF/customiuizer/blob/main/README_pt-BR.md");
				}
				else {
					Helpers.openURL(act, "https://github.com/MonwF/customiuizer");
				}
				return true;
			}
		});

		Configuration config = act.getResources().getConfiguration();
		if (config.getLocales().get(0).getCountry().equals("CN")) {
			PreferenceEx releasesEntry = findPreference("pref_key_releases");
			releasesEntry.setVisible(true);
			String releasesUrl = "https://rz3kv5wa4g.jiandaoyun.com/dash/650e43a383027ec3225083e9";
			releasesEntry.setLongPressListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Helpers.copyToClipboard(getValidContext(), releasesUrl);
					Toast.makeText(getValidContext(), "链接已复制", Toast.LENGTH_SHORT).show();
					return true;
				}
			});
			releasesEntry.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference pref) {
					openWebPage(releasesUrl);
					return true;
				}
			});

//			PreferenceEx contactEntry = findPreference("pref_key_contact");
//			contactEntry.setVisible(true);
//			contactEntry.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//				@Override
//				public boolean onPreferenceClick(Preference pref) {
//					Intent schemeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("coolmarket://u/217384"))
//						.setPackage("com.coolapk.market");
//					startActivity(schemeIntent);
//					return true;
//				}
//			});
		}
		PreferenceEx donateEntry = findPreference("pref_key_donate");
		donateEntry.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				if (!config.getLocales().get(0).getCountry().equals("CN")) {
					Helpers.openURL(act, "https://www.paypal.com/paypalme/tpsxj");
				}
				else {
					openSubFragment(new SubFragment(), null, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, pref.getTitle().toString(), R.layout.fragment_donate);
				}
				return true;
			}
		});
	}

	void findMod(String filter) {
		if (inSearchView == 2) return;
		lastFilter = filter;
		resultView.setVisibility(filter.equals("") ? View.GONE : View.VISIBLE);
		listView.setEnabled(filter.equals(""));
		ListAdapter adapter = resultView.getAdapter();
		if (adapter == null) return;
		((ModSearchAdapter)resultView.getAdapter()).getFilter().filter(filter);
	}

	private boolean openModCat(String cat, String sub, String mod) {
		Bundle bundle = new Bundle();
		bundle.putString("cat", cat);
		if (sub != null) {
			bundle.putString("sub", sub);
		}
		bundle.putString("mod", mod);
		catSelector.setTargetFragment(this, 0);
		switch (cat) {
			case "pref_key_system":
				if (sub == null)
					openSubFragment(catSelector, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_mods, R.xml.prefs_system_cat);
				else
					openSubFragment(prefSystem, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_mods, R.xml.prefs_system);
				return false;
			case "pref_key_launcher":
				if (sub == null)
					openSubFragment(catSelector, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.launcher_title, R.xml.prefs_launcher_cat);
				else
					openSubFragment(prefLauncher, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.launcher_title, R.xml.prefs_launcher);
				return true;
			case "pref_key_controls":
				if (sub == null)
					openSubFragment(catSelector, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.controls_mods, R.xml.prefs_controls_cat);
				else
					openSubFragment(prefControls, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.controls_mods, R.xml.prefs_controls);
				return false;
			case "pref_key_various":
				openSubFragment(prefVarious, bundle, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.various_mods, R.xml.prefs_various);
				return false;
			default:
				return false;
		}
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		if (preference != null) {
			PreferenceCategory modsCat = findPreference("prefs_cat");
			if (modsCat.findPreference(preference.getKey()) != null && openModCat(preference.getKey(), null, null)) {
				return true;
			}
		}
		return super.onPreferenceTreeClick(preference);
	}
}