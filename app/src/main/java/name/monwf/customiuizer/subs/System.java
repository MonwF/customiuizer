package name.monwf.customiuizer.subs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;

import java.util.Objects;

import miui.os.Build;
import name.monwf.customiuizer.CredentialsLauncher;
import name.monwf.customiuizer.PrefsProvider;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.prefs.CheckBoxPreferenceEx;
import name.monwf.customiuizer.prefs.ListPreferenceEx;
import name.monwf.customiuizer.prefs.PreferenceEx;
import name.monwf.customiuizer.prefs.SeekBarPreference;
import name.monwf.customiuizer.qs.AutoRotateService;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class System extends SubFragment {

	@Override
	public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
		super.onCreatePreferences(savedInstanceState, rootKey);
		selectSub();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ("pref_key_system_cat_recents".equals(sub)) {
			toolbarMenu = true;
			activeMenus = "launcher";
		}
		else if ("pref_key_system_cat_statusbar".equals(sub)
			|| "pref_key_system_cat_lockscreen".equals(sub)
			|| "pref_key_system_cat_qs".equals(sub)
			|| "pref_key_system_cat_drawer".equals(sub)
		) {
			toolbarMenu = true;
			activeMenus = "systemui";
		}
		else {
			toolbarMenu = false;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		switch (sub) {
			case "pref_key_system_cat_screen":
				findPreference("pref_key_system_orientationlock").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						PackageManager pm = getActivity().getPackageManager();
						if ((Boolean)newValue)
							pm.setComponentEnabledSetting(new ComponentName(getActivity(), AutoRotateService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
						else
							pm.setComponentEnabledSetting(new ComponentName(getActivity(), AutoRotateService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						return true;
					}
				});

				findPreference("pref_key_system_autobrightness_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_AutoBrightness(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_autobrightness_title, R.xml.prefs_system_autobrightness);
						return true;
					}
				});

				break;
			case "pref_key_system_cat_audio":
				findPreference("pref_key_system_ignorecalls_apps").setOnPreferenceClickListener(openAppsEdit);
				findPreference("pref_key_system_visualizer_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_Visualizer(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_visualizer_title, R.xml.prefs_system_visualizer);
						return true;
					}
				});
				break;
			case "pref_key_system_cat_vibration":
				findPreference("pref_key_system_vibration_apps").setOnPreferenceClickListener(openAppsEdit);
				findPreference("pref_key_system_vibration_amp_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_VibrationAmp(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_vibration_amp_title, R.xml.prefs_system_vibration_amp);
						return true;
					}
				});
				break;
			case "pref_key_system_cat_toasts":
				findPreference("pref_key_system_blocktoasts_apps").setOnPreferenceClickListener(openAppsEdit);
				break;

			case "pref_key_system_cat_statusbar":
				findPreference("pref_key_system_statusbarcolor_apps").setOnPreferenceClickListener(openAppsEdit);
				findPreference("pref_key_system_detailednetspeed_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_detailednetspeed);
						return true;
					}
				});
				findPreference("pref_key_system_statusbar_batterytempandcurrent_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_statusbar_batterytempandcurrent);
						return true;
					}
				});
				findPreference("prefs_system_statusbar_showdevicetemperature_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_statusbar_showdevicetemperature);
						return true;
					}
				});
				findPreference("pref_key_system_statusbar_batterystyle_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_statusbar_batterystyle);
						return true;
					}
				});
				findPreference("pref_key_system_statusbar_mobile_signal_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_statusbar_mobilesignal);
						return true;
					}
				});
				findPreference("pref_key_system_statusbaricons_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_hideicons);
						return true;
					}
				});
				findPreference("pref_key_system_statusbaricons_atright_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_statusbar_righticons);
						return true;
					}
				});
				findPreference("pref_key_system_statusbar_clocktweak_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_statusbar_clock);
						return true;
					}
				});
				findPreference("pref_key_system_batteryindicator_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_BatteryIndicator(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_batteryindicator_title, R.xml.prefs_system_batteryindicator);
						return true;
					}
				});

				findPreference("pref_key_system_statusbarcontrols_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_statusbarcontrols);
						return true;
					}
				});

				break;
			case "pref_key_system_cat_drawer":
				findPreference("pref_key_system_shortcut_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openStandaloneApp(preference, System.this, 0);
						return true;
					}
				});

				findPreference("pref_key_system_clock_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openStandaloneApp(preference, System.this, 1);
						return true;
					}
				});

				findPreference("pref_key_system_calendar_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openStandaloneApp(preference, System.this, 2);
						return true;
					}
				});

				findPreference("pref_key_system_cc_clocktweak_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_controlcenter_clock);
						return true;
					}
				});

				break;
			case "pref_key_system_cat_notifications":
				findPreference("pref_key_system_expandnotifs_apps").setOnPreferenceClickListener(openAppsEdit);

				findPreference("pref_key_system_notify_openinfw_apps").setOnPreferenceClickListener(openAppsEdit);
				findPreference("pref_key_system_colorizenotifs_apps").setOnPreferenceClickListener(openAppsEdit);
				break;
			case "pref_key_system_cat_qs":
				if (Build.IS_INTERNATIONAL_BUILD) {
					findPreference("pref_key_system_cc_switch_qsandnotification").setVisible(false);
				}

				findPreference("pref_key_system_cc_tile_style_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_controlcenter_themestyle);
						return true;
					}
				});
				break;
			case "pref_key_system_cat_recents":
				findPreference("pref_key_system_hidefromrecents_apps").setOnPreferenceClickListener(openAppsEdit);
				break;
			case "pref_key_system_cat_betterpopups":
				findPreference("pref_key_system_betterpopups_allowfloat_apps").setOnPreferenceClickListener(openAppsBWEdit);
				findPreference("pref_key_system_expandheadups_apps").setOnPreferenceClickListener(openAppsEdit);
				break;
			case "pref_key_system_cat_floatingwindows":
				findPreference("pref_key_system_fw_forcein_actionsend_apps").setOnPreferenceClickListener(openAppsEdit);
				break;
			case "pref_key_system_cat_applock":
				findPreference("pref_key_system_applock_list").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openLockedAppEdit(System.this, 0);
						return true;
					}
				});

				findPreference("pref_key_system_applock_skip_activities").setOnPreferenceClickListener(openActivitiesList);
				break;
			case "pref_key_system_cat_lockscreen":
				findPreference("pref_key_system_noscreenlock_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_NoScreenLock(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_noscreenlock_title, R.xml.prefs_system_noscreenlock);
						return true;
					}
				});

				findPreference("pref_key_system_lockscreenshortcuts_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Bundle args = new Bundle();
						args.putBoolean("isStandalone", true);
						Bundle catInfo = new Bundle();
						catInfo.putBoolean("isDynamic", true);
						args.putBundle("catInfo", catInfo);
						args.putString("sub", preference.getKey());
						String title = preference.getTitle().toString();
						openSubFragment(new System(), args, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, title, R.xml.prefs_system_lockscreenshortcuts);
						return true;
					}
				});

				findPreference("pref_key_system_albumartonlock_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_albumartonlock_title, R.xml.prefs_system_albumartonlock);
						return true;
					}
				});

				findPreference("pref_key_system_charginginfo_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_charginginfo_title, R.xml.prefs_system_charginginfo);
						return true;
					}
				});

				findPreference("pref_key_system_lsalarm_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_lsalarm_title, R.xml.prefs_system_alarmonlock);
						return true;
					}
				});

				findPreference("pref_key_system_secureqs_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new SubFragment(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_secureqs_title, R.xml.prefs_system_secureqs);
						return true;
					}
				});

				findPreference("pref_key_system_credentials").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						PackageManager pm = getActivity().getPackageManager();
						if ((Boolean)newValue)
							pm.setComponentEnabledSetting(new ComponentName(getActivity(), CredentialsLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
						else
							pm.setComponentEnabledSetting(new ComponentName(getActivity(), CredentialsLauncher.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						return true;
					}
				});

				((CheckBoxPreferenceEx)findPreference("pref_key_system_credentials")).setChecked(getActivity().getPackageManager().getComponentEnabledSetting(new ComponentName(getActivity(), CredentialsLauncher.class)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
				if (Helpers.isDeviceEncrypted(getContext())) {
					CheckBoxPreferenceEx nopwd = findPreference("pref_key_system_nopassword");
					nopwd.setChecked(false);
					nopwd.setUnsupported(true);
				}

				break;

			case "pref_key_system_lockscreenshortcuts_cat":
				findPreference("pref_key_system_lockscreenshortcuts_right").setOnPreferenceClickListener(openLockScreenActions);
				break;
			case "pref_key_system_cat_other":
				findPreference("pref_key_system_forceclose_apps").setOnPreferenceClickListener(openAppsEdit);

				findPreference("pref_key_system_nooverscroll_apps").setOnPreferenceClickListener(openAppsEdit);

				findPreference("pref_key_system_cleanshare_apps").setOnPreferenceClickListener(openShareEdit);
				findPreference("pref_key_system_cleanshare_test").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent sendIntent = new Intent();
						sendIntent.setAction(Intent.ACTION_SEND);
						sendIntent.putExtra(Intent.EXTRA_TEXT, "CustoMIUIzer is the best!");
						sendIntent.setType("*/*");
						getContext().startActivity(Intent.createChooser(sendIntent, null));
						return true;
					}
				});

				findPreference("pref_key_system_cleanopenwith_apps").setOnPreferenceClickListener(openOpenWithEdit);
				findPreference("pref_key_system_cleanopenwith_test").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
						alert.setTitle(R.string.system_cleanopenwith_testdata);
						alert.setSingleChoiceItems(R.array.openwithtest, -1, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								Intent viewIntent = new Intent();
								viewIntent.setAction(Intent.ACTION_VIEW);
								String type = "*/*";
								if (which == 0)
									type = "image/*";
								else if (which == 1)
									type = "audio/*";
								else if (which == 2)
									type = "video/*";
								else if (which == 3)
									type = "text/*";
								else if (which == 4)
									type = "application/zip";
								viewIntent.setDataAndType(Uri.parse("content://" + PrefsProvider.AUTHORITY + "/test/" + which), type);
								getContext().startActivity(Intent.createChooser(viewIntent, null));
							}
						});
						alert.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
						alert.show();
						return true;
					}
				});

				findPreference("pref_key_system_screenshot_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_ScreenshotConfig(), null, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_screenshot_title, R.xml.prefs_system_screenshot);
						return true;
					}
				});

				AppHelper.appPrefs.edit().putInt("pref_key_system_animationscale_window", Math.round(Helpers.getAnimationScale(0) * 10)).apply();
				((SeekBarPreference)findPreference("pref_key_system_animationscale_window")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						Helpers.setAnimationScale(0, seekBar.getProgress() / 10f);
					}
				});

				AppHelper.appPrefs.edit().putInt("pref_key_system_animationscale_transition", Math.round(Helpers.getAnimationScale(1) * 10)).apply();
				((SeekBarPreference)findPreference("pref_key_system_animationscale_transition")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						Helpers.setAnimationScale(1, seekBar.getProgress() / 10f);
					}
				});

				AppHelper.appPrefs.edit().putInt("pref_key_system_animationscale_animator", Math.round(Helpers.getAnimationScale(2) * 10)).apply();
				((SeekBarPreference)findPreference("pref_key_system_animationscale_animator")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						Helpers.setAnimationScale(2, seekBar.getProgress() / 10f);
					}
				});

				if (!checkAnimationPermission()) {
					Preference pref = findPreference("pref_key_system_animationscale_window");
					pref.setEnabled(false);
					pref.setSummary(R.string.launcher_privacyapps_fail);
					pref = findPreference("pref_key_system_animationscale_transition");
					pref.setEnabled(false);
					pref.setSummary(R.string.launcher_privacyapps_fail);
					pref = findPreference("pref_key_system_animationscale_animator");
					pref.setEnabled(false);
					pref.setSummary(R.string.launcher_privacyapps_fail);
				}

				break;
			case "pref_key_system_detailednetspeed_cat":
				break;
			case "pref_key_system_statusbarcontrols_cat":
				findPreference("pref_key_system_statusbarcontrols_dt").setOnPreferenceClickListener(openStatusbarActions);
				findPreference("pref_key_system_statusbarcontrols_dt_left").setOnPreferenceClickListener(openStatusbarActions);
				findPreference("pref_key_system_statusbarcontrols_dt_right").setOnPreferenceClickListener(openStatusbarActions);
				findPreference("pref_key_system_statusbarcontrols_longpress").setOnPreferenceClickListener(openStatusbarActions);
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			String key = null;
			if (requestCode == 0) key = "pref_key_system_shortcut_app";
			else if (requestCode == 1) key = "pref_key_system_clock_app";
			else if (requestCode == 2) key = "pref_key_system_calendar_app";
			if (key != null) AppHelper.appPrefs.edit().putString(key, data.getStringExtra("app")).putInt(key + "_user", data.getIntExtra("user", 0)).apply();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean checkAnimationPermission() {
		PackageManager pm = getActivity().getPackageManager();
		return pm.checkPermission("android.permission.SET_ANIMATION_SCALE", Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
	}

}