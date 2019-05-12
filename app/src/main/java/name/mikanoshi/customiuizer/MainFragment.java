package name.mikanoshi.customiuizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import miui.app.ActionBar;
import miui.app.AlertDialog;

import name.mikanoshi.customiuizer.subs.Controls;
import name.mikanoshi.customiuizer.subs.Launcher;
import name.mikanoshi.customiuizer.subs.System;
import name.mikanoshi.customiuizer.subs.Various;
import name.mikanoshi.customiuizer.utils.Helpers;

public class MainFragment extends PreferenceFragmentBase {

	public boolean miuizerModuleActive = false;
	private System prefSystem = new System();
	private Launcher prefLauncher = new Launcher();
	private Controls prefControls = new Controls();
	private Various prefVarious = new Various();

	public MainFragment() {
		super();
		this.setRetainInstance(true);
	}

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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupImmersiveMenu();
		final Activity act = getActivity();
		final Handler handler = new Handler();

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
				}); else {
					final Activity act = getActivity();
					if (isFragmentReady(act) && !miuizerModuleActive)
					act.runOnUiThread(new Runnable() {
						public void run() {
							showXposedDialog(act);
						}
					});
				}

				String dataPath = getActivity().getFilesDir().getAbsolutePath();
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
					} catch (Throwable t) {}

					//noinspection ConditionCoveredByFurtherCondition
					if (last_build != 0 && BuildConfig.VERSION_CODE < last_build)
						handler.post(showUpdateNotification);
					else
						handler.post(hideUpdateNotification);
				} catch (Throwable t) {}
			}
		}).start();

		if (Helpers.prefs.getBoolean("pref_key_was_restore", false)) {
			Helpers.prefs.edit().putBoolean("pref_key_was_restore", false).apply();
			showRestoreInfoDialog();
		}

		CheckBoxPreference.OnPreferenceChangeListener toggleLauncherIcon = new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PackageManager pm = act.getPackageManager();
				if ((Boolean)newValue)
					pm.setComponentEnabledSetting(new ComponentName(act, GateWayLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				else
					pm.setComponentEnabledSetting(new ComponentName(act, GateWayLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				return true;
			}
		};

		CheckBoxPreference.OnPreferenceChangeListener toggleSettingsIcon = new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				PackageManager pm = act.getPackageManager();
				if ((Boolean)newValue)
					pm.setComponentEnabledSetting(new ComponentName(act, GateWaySettings.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				else
					pm.setComponentEnabledSetting(new ComponentName(act, GateWaySettings.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				return true;
			}
		};

		CheckBoxPreference.OnPreferenceChangeListener toggleForceLocale = new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				getActivity().recreate();
				return true;
			}
		};

		CheckBoxPreference.OnPreferenceClickListener sendCrashReport = new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				ACRA.getErrorReporter().handleException(null);
				return true;
			}
		};

		CheckBoxPreference.OnPreferenceClickListener openFeedbackEdit = new CheckBoxPreference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				openSubFragment(new SubFragment(), null, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, R.string.miuizer_acramail_title, R.layout.prefs_freedback);
				return true;
			}
		};

		CheckBoxPreference launcherIconPreference = (CheckBoxPreference)findPreference("pref_key_miuizer_launchericon");
		if (launcherIconPreference != null)
		launcherIconPreference.setOnPreferenceChangeListener(toggleLauncherIcon);
		CheckBoxPreference miuizerSettingsPreference = (CheckBoxPreference) findPreference("pref_key_miuizer_settingsicon");
		if (miuizerSettingsPreference != null)
		miuizerSettingsPreference.setOnPreferenceChangeListener(toggleSettingsIcon);
		CheckBoxPreference forceLocalePreference = (CheckBoxPreference) findPreference("pref_key_miuizer_forcelocale");
		if (forceLocalePreference != null)
		forceLocalePreference.setOnPreferenceChangeListener(toggleForceLocale);

		Preference miuizerCrashReportPreference = findPreference("pref_key_miuizer_sendreport");
		if (miuizerCrashReportPreference != null)
		miuizerCrashReportPreference.setOnPreferenceClickListener(sendCrashReport);
		Preference feedbackPreference = findPreference("pref_key_miuizer_feedback");
		if (feedbackPreference != null)
		feedbackPreference.setOnPreferenceClickListener(openFeedbackEdit);

		Preference issueTrackerPreference = findPreference("pref_key_issuetracker");
		issueTrackerPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://code.highspec.ru/Mikanoshi/CustoMIUIzer/issues");
				return true;
			}
		});

		Preference donateCryptoPagePreference = findPreference("pref_key_paycrypto");
		donateCryptoPagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference pref) {
				Helpers.openURL(act, "https://code.highspec.ru/cryptodonate");
				return true;
			}
		});

		Preference donateOtherPagePreference = findPreference("pref_key_payother");
		donateOtherPagePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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


		//Helpers.removePref(this, "pref_key_miuizer_force_material", "pref_key_miuizer");
	}

	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu_mods, menu);
		return true;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
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
					detailsIntent.setData(Uri.fromParts("package", "name.mikanoshi.customiuizer", null));
					startActivity(detailsIntent);
				} catch (Throwable e) {
					Helpers.openURL(getActivity(), "https://code.highspec.ru/Mikanoshi/CustoMIUIzer/releases");
				}
			case R.id.xposedinstaller:
				if (!Helpers.isXposedInstallerInstalled(getContext())) {
					Toast.makeText(getContext(), R.string.xposed_not_found, Toast.LENGTH_LONG).show();
					return true;
				}

				Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("com.solohsu.android.edxp.manager");
				try {
					if (intent != null) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					getContext().startActivity(intent);
					return true;
				} catch (Throwable e1) {
					intent = getContext().getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
					if (intent != null) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					try {
						getContext().startActivity(intent);
						return true;
					} catch (Throwable e2) {
						Toast.makeText(getContext(), R.string.xposed_not_found, Toast.LENGTH_LONG).show();
					}
					return false;
				}
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
						getContext().sendBroadcast(new Intent("name.mikanoshi.customiuizer.mods.action.FastReboot"));
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
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen parentPreferenceScreen, Preference preference) {
		if (preference != null) {
			PreferenceCategory modsCat = (PreferenceCategory)findPreference("prefs_cat");
			if (modsCat.findPreference(preference.getKey()) != null) {

				switch (preference.getKey()) {
					case "pref_key_system":
						openSubFragment(prefSystem, null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_mods, R.xml.prefs_system);
						break;
					case "pref_key_launcher":
						openSubFragment(prefLauncher, null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.launcher_mods, R.xml.prefs_launcher);
						return true;
					case "pref_key_controls":
						openSubFragment(prefControls, null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.controls_mods, R.xml.prefs_controls);
						break;
					case "pref_key_various":
						openSubFragment(prefVarious, null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.various_mods, R.xml.prefs_various);
						break;
				}
			}
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