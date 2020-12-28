package name.mikanoshi.customiuizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.LineHeightSpan;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.acra.ACRA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import miui.app.AlertDialog;
import miui.view.SearchActionMode;

import name.mikanoshi.customiuizer.prefs.ListPreferenceEx;
import name.mikanoshi.customiuizer.prefs.PreferenceEx;
import name.mikanoshi.customiuizer.subs.CategorySelector;
import name.mikanoshi.customiuizer.subs.Controls;
import name.mikanoshi.customiuizer.subs.Launcher;
import name.mikanoshi.customiuizer.subs.System;
import name.mikanoshi.customiuizer.subs.Various;
import name.mikanoshi.customiuizer.utils.GuidePopup;
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
	boolean areGuidesCleared = false;
	boolean actionModeNew = false;
	ActionMode actionMode = null;
	SearchActionMode.Callback actionModeCallback = new SearchActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			if (searchView == null || listView == null) {
				if (mode != null) mode.finish();
				return false;
			}

			SearchActionMode samode = (SearchActionMode)mode;
			samode.setAnchorView(searchView);
			samode.setAnimateView(listView);
			samode.getSearchInput().setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					isSearchFocused = hasFocus;
				}
			});
			samode.getSearchInput().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					isSearchFocused = v.hasFocus();
				}
			});
			samode.getSearchInput().setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
						Helpers.hideKeyboard(getActivity(), v);
						resultView.requestFocus();
						return true;
					}
					return false;
				}
			});
			samode.getSearchInput().addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override
				public void afterTextChanged(Editable s) {
					findMod(s.toString().trim());
				}
			});

			if (actionModeNew) {
				actionModeNew = false;
				resultView.postDelayed(new Runnable() {
					@Override
					public void run() {
						samode.getSearchInput().setText(Helpers.NEW_MODS_SEARCH_QUERY);
						samode.getSearchInput().setSelection(1, 1);
						Helpers.hideKeyboard(getActivity(), getView());
						resultView.requestFocus();
					}
				}, getResources().getInteger(android.R.integer.config_longAnimTime));
			}

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			if (searchView == null || listView == null) {
				if (mode != null) mode.finish();
				return false;
			}

			SearchActionMode samode = (SearchActionMode)mode;
			samode.setAnchorView(searchView);
			samode.setAnimateView(listView);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			TextView input = search == null ? null : search.findViewById(android.R.id.input);
			if (input != null) input.setText("");
			findMod("");
			getActionBar().show();
			actionMode = null;
		}
	};

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
	@SuppressLint("MissingSuperCall")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.xml.prefs_main);
		addPreferencesFromResource(R.xml.prefs_main);

		final Activity act = getActivity();
		final Handler handler = new Handler(act.getMainLooper());

		if (Helpers.miuizerModuleActive && !Helpers.prefs.getBoolean("miuizer_prefs_migrated", false) && Helpers.usingNewSharedPrefs()) {
			((MainActivity)act).migrateOnExit = true;
			act.recreate();
			return;
		}

		if (Helpers.prefs.getBoolean("miuizer_prefs_migrated", false)) {
			int result = Helpers.prefs.getInt("miuizer_prefs_migration_result", 0);
			if (result > 0) {
				Helpers.prefs.edit().putInt("miuizer_prefs_migration_result", -1).apply();
				showPrefsMigrationDialog(result == 1);
			}
		}

		// Preventing launch delay
		new Thread(new Runnable() {
			public void run() {
				if (!Helpers.isXposedInstallerInstalled(act))
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(act);
						builder.setTitle(R.string.xposed_not_found);
						builder.setMessage(R.string.xposed_not_found_explain);
						builder.setNeutralButton(R.string.okay, null);
						AlertDialog dlg = builder.create();
						if (isFragmentReady(act)) dlg.show();
					}
				}); else if (isFragmentReady(act) && !Helpers.miuizerModuleActive)
				act.runOnUiThread(new Runnable() {
					public void run() {
						showXposedDialog(act);
					}
				}); else areGuidesCleared = true;

				String dataPath = act.getFilesDir().getAbsolutePath();
				HttpURLConnection connection = null;
				try {
					URL url = new URL("https://code.highspec.ru/Mikanoshi/CustoMIUIzer/raw/branch/master/last_build");
					//URL url = new URL("https://code.highspec.ru/last_build");
					connection = (HttpURLConnection)url.openConnection();
					connection.setDefaultUseCaches(false);
					connection.setUseCaches(false);
					connection.setRequestProperty("Pragma", "no-cache");
					connection.setRequestProperty("Cache-Control", "no-cache");
					connection.setRequestProperty("Expires", "-1");
					connection.connect();

					if (connection.getResponseCode() == HttpURLConnection.HTTP_OK || connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
						String last_build = "";

						try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
							last_build = reader.readLine().trim();
						} catch (Throwable t) { t.printStackTrace(); }

						File tmp = new File(dataPath);
						if (!tmp.exists())
							tmp.mkdirs();
						try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dataPath + "/last_build", false))) {
							writer.write(last_build);
						} catch (Throwable t) { t.printStackTrace(); }
					}
				} catch (Throwable t) {}

				try {
					if (connection != null) connection.disconnect();
				} catch (Throwable t) {}

				try (InputStream inputFile = new FileInputStream(dataPath + "/last_build")) {
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
					int last_build = 0;
					try {
						last_build = Integer.parseInt(reader.readLine().trim());
						reader.close();
					} catch (Throwable t) {}

					//noinspection ConditionCoveredByFurtherCondition
					if (last_build != 0 && BuildConfig.VERSION_CODE < last_build)
						handler.post(showUpdateNotification);
					else
						handler.post(hideUpdateNotification);
				} catch (Throwable t) {}

				Helpers.getAllMods(act, savedInstanceState != null);
			}
		}).start();

		if (Helpers.prefs.getBoolean("pref_key_was_restore", false)) {
			Helpers.prefs.edit().putBoolean("pref_key_was_restore", false).apply();
			showRestoreInfoDialog();
		}
	}

	public View onInflateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
		return inflater.inflate(Helpers.is12() ? R.layout.prefs_main12 : R.layout.prefs_main, group, false);
	}

	private void openActionMode(boolean isNew) {
		actionModeNew = isNew;
		actionMode = startActionMode(actionModeCallback);
		fixActionBar();
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

		searchView = getView().findViewById(R.id.searchView);
		setActionModeStyle(searchView);

		search = searchView.findViewById(android.R.id.inputArea);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openActionMode(false);
			}
		});
		search.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				openActionMode(true);
				return true;
			}
		});

		TextView searchInput = search.findViewById(android.R.id.input);
		searchInput.setHint(R.string.search_input_description);

		modsCat = null; markCat = null;
		listView = getView().findViewById(android.R.id.list);
		listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
			@Override
			public void onChildViewAdded(View parent, View child) {
				if (child == null) return;
				CharSequence title = ((TextView)child.findViewById(android.R.id.title)).getText();
				if (title.equals(getResources().getString(R.string.system_mods))) modsCat = child;
				if (title.equals(getResources().getString(R.string.miuizer_show_newmods_title))) markCat = child;
				if (modsCat != null && markCat != null) {
					if (areGuidesCleared) try {
						showGuides();
					} catch (Throwable t) {
						t.printStackTrace();
					}
					listView.setOnHierarchyChangeListener(null);
				}
			}

			@Override
			public void onChildViewRemoved(View parent, View child) {}
		});

		if (actionMode != null) actionMode.invalidate();
		final Activity act = getActivity();

		PreferenceEx warning = (PreferenceEx)findPreference("pref_key_warning");
		if (warning != null)
		if (!Helpers.usingNewSharedPrefs() && Helpers.areXposedBlacklistsEnabled()) {
			warning.setTitle(R.string.warning);
			if (act.getApplicationContext().getApplicationInfo().targetSdkVersion > 27)
				warning.setSummary(getString(R.string.warning_blacklist) + "\n" + getString(R.string.warning_blacklist_sdk));
			else
				warning.setSummary(R.string.warning_blacklist);
			warning.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Helpers.openXposedApp(getContext());
					return true;
				}
			});
		} else getPreferenceScreen().removePreference(warning);

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

		String[] locales;
		try {
			AssetManager am = getContext().getAssets();
			@SuppressWarnings("JavaReflectionMemberAccess") @SuppressLint("SoonBlockedPrivateApi")
			Method getNonSystemLocales = AssetManager.class.getDeclaredMethod("getNonSystemLocales");
			locales = (String[])getNonSystemLocales.invoke(am);
			if (locales == null) locales = new String[] {};
		} catch (Throwable t) {
			locales = new String[] { "de", "es", "it", "pt-BR", "ru-RU", "tr", "uk-UK", "zh-CN" };
		}

		ArrayList<String> localesArr = new ArrayList<String>(Arrays.asList(locales));
		ArrayList<SpannableString> localeNames = new ArrayList<SpannableString>();
		localesArr.add(0, "en");
		for (String locale: localesArr) try {
			Locale loc = Locale.forLanguageTag(locale);
			StringBuilder locStr = new StringBuilder(loc.getDisplayLanguage(loc));
			locStr.setCharAt(0, Character.toUpperCase(locStr.charAt(0)));
			SpannableString locSpanString;
			if (!locale.equals("en")) {
				String locStrPct = locStr.toString() + "\n" + Helpers.l10nProgress.get(locale);
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

		findPreference("pref_key_miuizer_holiday").setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				getActivity().recreate();
				return true;
			}
		});

		findPreference("pref_key_miuizer_sendreport").setOnPreferenceClickListener(new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (Helpers.checkStoragePerm(act, Helpers.REQUEST_PERMISSIONS_REPORT)) createReport();
				return true;
			}
		});

		findPreference("pref_key_miuizer_feedback").setOnPreferenceClickListener(new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new SubFragment(), null, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, R.string.miuizer_acramail_title, R.layout.prefs_freedback);
				return true;
			}
		});

		findPreference("pref_key_issuetracker").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://code.highspec.ru/Mikanoshi/CustoMIUIzer/issues");
				return true;
			}
		});

		findPreference("pref_key_payinapp").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Bundle args = new Bundle();
				args.putInt("baseResId", R.layout.fragment_inapp);
				openSubFragment(new InAppFragment(), args, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.support_donate_title, R.xml.prefs_inapp);
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

		findPreference("pref_key_miuizer_marknewmods").setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Helpers.updateNewModsMarking(getActivity(), Integer.parseInt((String)newValue));
				return true;
			}
		});

		//Helpers.removePref(this, "pref_key_miuizer_force_material", "pref_key_miuizer");
	}

	void showGuides() {
		if (getView() == null || getActivity() == null) return;
		Button more = getView().findViewById(getResources().getIdentifier("more", "id", "miui"));
		View search = getView().findViewById(android.R.id.inputArea);
		View overlay = getActivity().findViewById(R.id.guide_overlay);
		if (more == null || search == null || overlay == null || Helpers.prefs.getBoolean("miuizer_guides_shown", false)) return;

		overlay.setBackgroundColor(Helpers.isNightMode(getActivity()) ? Color.argb(150, 0, 0, 0) : Color.argb(150, 255, 255, 255));
		overlay.setVisibility(View.VISIBLE);
		overlay.bringToFront();

		showGuide(search, GuidePopup.ARROW_TOP_MODE, R.string.guide_search).setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				showGuide(markCat, GuidePopup.ARROW_BOTTOM_MODE, R.string.guide_marknew).setOnDismissListener(new PopupWindow.OnDismissListener() {
					@Override
					public void onDismiss() {
						showImmersionMenu();
						showGuide(more, GuidePopup.ARROW_TOP_MODE, R.string.guide_softreboot).setOnDismissListener(new PopupWindow.OnDismissListener() {
							@Override
							public void onDismiss() {
								dismissImmersionMenu(true);
								overlay.setVisibility(View.GONE);
							}
						});
					}
				});
			}
		});

		Helpers.prefs.edit().putBoolean("miuizer_guides_shown", true).apply();
	}

	@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
	GuidePopup showGuide(View anchor, int arrowMode, int textResId) {
		return showGuide(anchor, arrowMode, textResId, 0,  0);
	}

	@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
	GuidePopup showGuide(View anchor, int arrowMode, int textResId, int x, int y) {
		GuidePopup guidePopup = new GuidePopup(getActivity());
		guidePopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		guidePopup.setArrowMode(arrowMode);
		guidePopup.setGuideText(textResId);
		guidePopup.setOutsideTouchable(true);
		guidePopup.show(anchor, x, y);
		return guidePopup;
	}

	void findMod(String filter) {
		resultView.setVisibility(filter.equals("") ? View.GONE : View.VISIBLE);
		listView.setEnabled(filter.equals(""));
		ListAdapter adapter = resultView.getAdapter();
		if (adapter == null) return;
		((ModSearchAdapter)resultView.getAdapter()).getFilter().filter(filter);
	}

	public void createReport() {
		ACRA.getErrorReporter().handleException(null);
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