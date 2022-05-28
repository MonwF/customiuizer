package name.mikanoshi.customiuizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.LineHeightSpan;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import name.mikanoshi.customiuizer.prefs.ListPreferenceEx;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.subs.CategorySelector;
import name.mikanoshi.customiuizer.subs.Controls;
import name.mikanoshi.customiuizer.subs.Launcher;
import name.mikanoshi.customiuizer.subs.System;
import name.mikanoshi.customiuizer.subs.Various;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.ModData;
import name.mikanoshi.customiuizer.utils.ModSearchAdapter;

public class MainFragment extends PreferenceFragmentBase {

	private final CategorySelector catSelector = new CategorySelector();
	public System prefSystem = new System();
	public Launcher prefLauncher = new Launcher();
	public Controls prefControls = new Controls();
	public Various prefVarious = new Various();
	private View searchView = null;
	private ListView listView = null;
	private ListView resultView = null;
	private LinearLayout search = null;
	private View modsCat = null;
	private View markCat = null;
	boolean isSearchFocused = false;
	ActionMode actionMode = null;

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

	private boolean isFragmentReady(Activity act) {
		return act != null && !act.isFinishing() && MainFragment.this.isAdded();
	}
	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container,
							 Bundle savedInstanceState) {
		return LayoutInflater.from(getActivity()).inflate(R.layout.prefs_main12, container, false);
	}

	@Override
	@SuppressLint("MissingSuperCall")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.xml.prefs_main);
		addPreferencesFromResource(R.xml.prefs_main);

		final Activity act = getActivity();

		if (Helpers.miuizerModuleActive && !Helpers.prefs.getBoolean("miuizer_prefs_migrated", false) && Helpers.usingNewSharedPrefs()) {
			((MainActivity)act).migrateOnExit = true;
			act.recreate();
			return;
		}

		// Preventing launch delay
		new Thread(new Runnable() {
			public void run() {
				if (isFragmentReady(act) && !Helpers.miuizerModuleActive)
				act.runOnUiThread(new Runnable() {
					public void run() {
						showXposedDialog(act);
					}
				});

				if (Helpers.prefs.getBoolean("miuizer_prefs_migrated", false)) {
					int result = Helpers.prefs.getInt("miuizer_prefs_migration_result", 0);
					if (result > 0) {
						Helpers.prefs.edit().putInt("miuizer_prefs_migration_result", -1).apply();
						act.runOnUiThread(new Runnable() {
							public void run() {
								showPrefsMigrationDialog(result == 1);
							}
						});
					}
				}

				Helpers.getAllMods(act, savedInstanceState != null);
			}
		}).start();

		if (Helpers.prefs.getBoolean("pref_key_was_restore", false)) {
			Helpers.prefs.edit().putBoolean("pref_key_was_restore", false).apply();
			showRestoreInfoDialog();
		}
	}

	private static class SetLineOverlap implements LineHeightSpan {
		private int originalBottom = 15;
		private int originalDescent = 13;
		private final Boolean overlap;
		private Boolean overlapSaved = false;

		SetLineOverlap(Boolean overlap) {
			this.overlap = overlap;
		}

		@Override
		public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
			if (overlap) {
				if (!overlapSaved) {
					originalBottom = fm.bottom;
					originalDescent = fm.descent;
					overlapSaved = true;
				}
				fm.bottom += fm.top;
				fm.descent += fm.top;
			} else {
				fm.bottom = originalBottom;
				fm.descent = originalDescent;
				overlapSaved = false;
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		supressMenu = true;
		super.onActivityCreated(savedInstanceState);

		if (getView() == null) return;

		resultView = getView().findViewById(android.R.id.custom);
		resultView.setAdapter(new ModSearchAdapter(getActivity()));
		resultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ModData mod = (ModData)parent.getAdapter().getItem(position);
				openModCat(mod.cat.name(), mod.sub, mod.key);
			}
		});
		resultView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if (actionMode != null && isSearchFocused) {
					isSearchFocused = false;
					Handler handler = new Handler(v.getContext().getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							Helpers.hideKeyboard(getActivity(), getView());
						}
					}, getResources().getInteger(android.R.integer.config_shortAnimTime));
				}
				return false;
			}
		});
		setViewBackground(resultView);
		final Activity act = getActivity();

		PreferenceEx warning = (PreferenceEx)findPreference("pref_key_warning");
		if (warning != null) {
			getPreferenceScreen().removePreference(warning);
		}

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

		String[] locales = new String[] { "ru-RU", "zh-CN" };

		ArrayList<String> localesArr = new ArrayList<String>(Arrays.asList(locales));
		ArrayList<SpannableString> localeNames = new ArrayList<SpannableString>();
		localesArr.add(0, "en");
		for (String locale: localesArr) try {
			Locale loc = Locale.forLanguageTag(locale);
			StringBuilder locStr = new StringBuilder(loc.getDisplayLanguage(loc));
			locStr.setCharAt(0, Character.toUpperCase(locStr.charAt(0)));
			SpannableString locSpanString;
			if (!locale.equals("en")) {
				String locStrPct = locStr + "\n" + Helpers.l10nProgress.get(locale) + "%";
				int fullTextLength = locStrPct.length();
				locSpanString = new SpannableString(locStrPct);
				locSpanString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), locStr.toString().length(), fullTextLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				locSpanString.setSpan(new SetLineOverlap(true), 1, fullTextLength - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				locSpanString.setSpan(new SetLineOverlap(false), fullTextLength - 1, fullTextLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else locSpanString = new SpannableString(locStr.toString());
			localeNames.add(locSpanString);
		} catch (Throwable t) {
			localeNames.add(new SpannableString(Locale.getDefault().getDisplayLanguage(Locale.getDefault())));
		}

		localesArr.add(0, "auto");
		localeNames.add(0, new SpannableString(getString(R.string.array_system_default)));

		ListPreferenceEx locale = (ListPreferenceEx)findPreference("pref_key_miuizer_locale");
		locale.setEntries(localeNames.toArray(new CharSequence[0]));
		locale.setEntryValues(localesArr.toArray(new CharSequence[0]));
		locale.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				getActivity().recreate();
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

		findPreference("pref_key_github").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			@SuppressWarnings("deprecation")
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://github.com/monwf/customiuizer");
				return true;
			}
		});
	}

	void findMod(String filter) {
		resultView.setVisibility(filter.equals("") ? View.GONE : View.VISIBLE);
		listView.setEnabled(filter.equals(""));
		ListAdapter adapter = resultView.getAdapter();
		if (adapter == null) return;
		((ModSearchAdapter)resultView.getAdapter()).getFilter().filter(filter);
	}

	// PreferenceScreens management
	private boolean openModCat(String cat) {
		return openModCat(cat, null, null);
	}

	private boolean openModCat(String cat, String sub, String mod) {
		Bundle bundle = new Bundle();
		bundle.putString("cat", cat);
		bundle.putString("sub", sub);
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
	public boolean onPreferenceTreeClick(PreferenceScreen parentPreferenceScreen, Preference preference) {
		if (preference != null) {
			PreferenceCategory modsCat = (PreferenceCategory)findPreference("prefs_cat");
			if (modsCat.findPreference(preference.getKey()) != null)
			if (openModCat(preference.getKey())) return true;
		}
		return super.onPreferenceTreeClick(parentPreferenceScreen, preference);
	}

	private void showRestoreInfoDialog() {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.warning);
			builder.setMessage(R.string.backup_restore_info);
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			AlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void showPrefsMigrationDialog(boolean success) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.warning);
			builder.setMessage(success ? R.string.prefs_migration_success : R.string.prefs_migration_failure);
			builder.setCancelable(false);
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton){}
			});
			AlertDialog dlg = builder.create();
			dlg.show();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

//	private void showNotYetDialog() {
//		try {
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//			builder.setTitle(R.string.info);
//			builder.setMessage(R.string.not_yet);
//			builder.setCancelable(true);
//			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton){}
//			});
//			AlertDialog dlg = builder.create();
//			dlg.show();
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//	}
}