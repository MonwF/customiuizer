package name.mikanoshi.customiuizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import miui.app.ActionBar;
import miui.app.AlertDialog;
import miui.view.SearchActionMode;

import name.mikanoshi.customiuizer.mods.GlobalActions;
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

	public boolean miuizerModuleActive = false;
	private CategorySelector catSelector = new CategorySelector();
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
						hideKeyboard();
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
						hideKeyboard();
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

	private Runnable showUpdateNotification = new Runnable() {
		@Override
		public void run() {
			if (getView() != null) try {
				ImageView alert = getView().findViewById(R.id.update_alert);
				if (alert != null) alert.setVisibility(View.VISIBLE);
			} catch (Throwable e) {}
		}
	};

	private Runnable hideUpdateNotification = new Runnable() {
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
				}); else if (isFragmentReady(act) && !miuizerModuleActive)
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
		return inflater.inflate(R.layout.prefs_main, group, false);
	}

	private void setupImmersiveMenu() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) actionBar.showSplitActionBar(false, false);
		setImmersionMenuEnabled(true);

		if (getView() != null)
		if (getView().findViewById(R.id.update_alert) == null) {
			Button more = getView().findViewById(getResources().getIdentifier("more", "id", "miui"));
			if (more == null) return;
			float density = getResources().getDisplayMetrics().density;
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.END | Gravity.TOP;
			ImageView alert = new ImageView(getContext());
			alert.setImageResource(R.drawable.alert);
			alert.setAdjustViewBounds(true);
			alert.setMaxWidth(Math.round(16 * density));
			alert.setMaxHeight(Math.round(16 * density));
			alert.setLayoutParams(lp);
			alert.setId(R.id.update_alert);
			alert.setVisibility(View.GONE);
			((ViewGroup)more.getParent()).addView(alert);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setupImmersiveMenu();
	}

	private void openActionMode(boolean isNew) {
		actionModeNew = isNew;
		actionMode = startActionMode(actionModeCallback);
		// Hide stupid auto split actionbar
		try {
			ActionBar actionBar = getActionBar();
			Field mSplitViewField = actionBar.getClass().getDeclaredField("mSplitView");
			mSplitViewField.setAccessible(true);
			View mSplitView = (View)mSplitViewField.get(actionBar);
			if (mSplitView != null) mSplitView.setVisibility(View.GONE);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupImmersiveMenu();

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
					hideKeyboard();
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
		if (Helpers.areXposedBlacklistsEnabled()) {
			warning.setTitle(R.string.warning);
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
			locales = new String[] { "it", "ru-RU", "tr", "uk-UK", "zh-CN" };
		}

		ArrayList<String> localesArr = new ArrayList<String>(Arrays.asList(locales));
		ArrayList<String> localeNames = new ArrayList<String>();
		localesArr.add(0, "en");
		for (String locale: localesArr) try {
			Locale loc = Locale.forLanguageTag(locale);
			StringBuilder locStr = new StringBuilder(loc.getDisplayLanguage(loc));
			locStr.setCharAt(0, Character.toUpperCase(locStr.charAt(0)));
			localeNames.add(locStr.toString());
		} catch (Throwable t) {
			localeNames.add(Locale.getDefault().getDisplayLanguage(Locale.getDefault()));
		}

		localesArr.add(0, "auto");
		localeNames.add(0, getString(R.string.array_system_default));

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

	public void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputManager == null) return;
		View currentFocusedView = getActivity().getCurrentFocus();
		if (currentFocusedView != null)
		inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu_mods, menu);
 		if (Helpers.isNightMode(getActivity()))
		for (int i = 0; i < menu.size(); i++) try {
			MenuItem item = menu.getItem(i);
			SpannableString spanString = new SpannableString(item.getTitle().toString());
			spanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.preference_primary_text_color, getActivity().getTheme())), 0, spanString.length(), 0);
			item.setTitle(spanString);
		} catch (Throwable t) {}
		return true;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (menu.size() == 0) return;
		menu.getItem(0).setVisible(false);
		if (getView() == null) return;
		ImageView alert = getView().findViewById(R.id.update_alert);
		if (alert != null && alert.isShown()) menu.getItem(0).setVisible(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.get_update:
				try {
					Intent detailsIntent = new Intent("de.robv.android.xposed.installer.DOWNLOAD_DETAILS");
					detailsIntent.addCategory(Intent.CATEGORY_DEFAULT);
					detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					detailsIntent.setData(Uri.fromParts("package", Helpers.modulePkg, null));
					startActivity(detailsIntent);
				} catch (Throwable e) {
					Helpers.openURL(getActivity(), "https://code.highspec.ru/Mikanoshi/CustoMIUIzer/releases");
				}
			case R.id.xposedinstaller:
				return Helpers.openXposedApp(getContext());
			case R.id.backuprestore:
				showBackupRestoreDialog();
				return true;
			case R.id.softreboot:
				if (!miuizerModuleActive) {
					showXposedDialog(getActivity());
					return true;
				}

				AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
				alert.setTitle(R.string.soft_reboot);
				alert.setMessage(R.string.soft_reboot_ask);
				alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						getContext().sendBroadcast(new Intent(GlobalActions.ACTION_PREFIX + "FastReboot"));
					}
				});
				alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {}
				});
				alert.show();
				return true;
			case R.id.about:
				Bundle args = new Bundle();
				args.putInt("baseResId", R.layout.fragment_about);
				openSubFragment(new AboutFragment(), args, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.app_about, R.xml.prefs_about);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void backupSettings(Activity act) {
		String backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + Helpers.externalFolder;
		if (!Helpers.preparePathForBackup(act, backupPath)) return;
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(backupPath + Helpers.backupFile));
			output.writeObject(Helpers.prefs.getAll());

			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.do_backup);
			alert.setMessage(R.string.backup_ok);
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
		} catch (Throwable e) {
			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.warning);
			alert.setMessage(R.string.storage_cannot_backup);
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();

			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void restoreSettings(final Activity act) {
		if (!Helpers.checkStoragePerm(act, Helpers.REQUEST_PERMISSIONS_RESTORE)) return;
		if (!Helpers.checkStorageReadable(act)) return;
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + Helpers.externalFolder + Helpers.backupFile));
			Map<String, ?> entries = (Map<String, ?>)input.readObject();
			if (entries == null || entries.isEmpty()) throw new RuntimeException("Cannot read entries");

			SharedPreferences.Editor prefEdit = Helpers.prefs.edit();
			prefEdit.clear();
			for (Map.Entry<String, ?> entry: entries.entrySet()) {
				Object val = entry.getValue();
				String key = entry.getKey();

				if (val instanceof Boolean)
					prefEdit.putBoolean(key, (Boolean)val);
				else if (val instanceof Float)
					prefEdit.putFloat(key, (Float)val);
				else if (val instanceof Integer)
					prefEdit.putInt(key, (Integer)val);
				else if (val instanceof Long)
					prefEdit.putLong(key, (Long)val);
				else if (val instanceof String)
					prefEdit.putString(key, ((String)val));
				else if (val instanceof Set<?>)
					prefEdit.putStringSet(key, ((Set<String>)val));
			}
			prefEdit.apply();

			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.do_restore);
			alert.setMessage(R.string.restore_ok);
			alert.setCancelable(false);
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					act.finish();
					act.startActivity(act.getIntent());
				}
			});
			alert.show();
		} catch (Throwable t) {
			t.printStackTrace();
			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setTitle(R.string.warning);
			alert.setMessage(R.string.storage_cannot_restore);
			alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {}
			});
			alert.show();
		} finally {
			try {
				if (input != null) input.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	public void createReport() {
		ACRA.getErrorReporter().handleException(null);
	}

	public void showBackupRestoreDialog() {
		final Activity act = getActivity();

		AlertDialog.Builder alert = new AlertDialog.Builder(act);
		alert.setTitle(R.string.backup_restore);
		alert.setMessage(R.string.backup_restore_choose);
		alert.setPositiveButton(R.string.do_restore, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				restoreSettings(act);
			}
		});
		alert.setNegativeButton(R.string.do_backup, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				backupSettings(act);
			}
		});
		alert.show();
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

	public void showXposedDialog(Activity act) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle(R.string.warning);
			builder.setMessage(R.string.module_not_active);
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