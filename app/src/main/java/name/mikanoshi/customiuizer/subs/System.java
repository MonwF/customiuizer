package name.mikanoshi.customiuizer.subs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.provider.Settings;
import android.widget.SeekBar;

import java.lang.reflect.Method;
import java.util.Objects;

import miui.app.AlertDialog;
import name.mikanoshi.customiuizer.CredentialsLauncher;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SharedPrefsProvider;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.CheckBoxPreferenceEx;
import name.mikanoshi.customiuizer.prefs.ListPreferenceEx;
import name.mikanoshi.customiuizer.prefs.SeekBarPreference;
import name.mikanoshi.customiuizer.qs.AutoRotateService;
import name.mikanoshi.customiuizer.utils.Helpers;

public class System extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		String sub = args.getString("sub");
		if (sub == null) sub = "";

		selectSub("pref_key_system", sub);
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

				int min = getResources().getInteger(getResources().getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android"));
				int max = getResources().getInteger(getResources().getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android"));
				SeekBarPreference minBrightness = (SeekBarPreference)findPreference("pref_key_system_minbrightness");
				minBrightness.setDefaultValue(Math.round((max - min) / 2.0));
				minBrightness.setMinValue(min);
				minBrightness.setMaxValue(max);

				break;
			case "pref_key_system_cat_audio":
				findPreference("pref_key_system_ignorecalls_apps").setOnPreferenceClickListener(openAppsEdit);

				findPreference("pref_key_system_visualizer_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_Visualizer(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_visualizer_title, R.xml.prefs_system_visualizer);
						return true;
					}
				});
				break;
			case "pref_key_system_cat_vibration":
				findPreference("pref_key_system_vibration_apps").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_system_vibration", "1"), "1"));
				findPreference("pref_key_system_vibration").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						findPreference("pref_key_system_vibration_apps").setEnabled(!newValue.equals("1"));
						return true;
					}
				});

				findPreference("pref_key_system_vibration_apps").setOnPreferenceClickListener(openAppsEdit);
				break;
			case "pref_key_system_cat_toasts":
				findPreference("pref_key_system_blocktoasts_apps").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_system_blocktoasts", "1"), "1"));
				findPreference("pref_key_system_blocktoasts").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						findPreference("pref_key_system_blocktoasts_apps").setEnabled(!newValue.equals("1"));
						return true;
					}
				});

				findPreference("pref_key_system_blocktoasts_apps").setOnPreferenceClickListener(openAppsEdit);
				break;

			case "pref_key_system_cat_statusbar":
				findPreference("pref_key_system_statusbarcolor_apps").setOnPreferenceClickListener(openAppsEdit);

				findPreference("pref_key_system_detailednetspeed_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new SubFragment(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_detailednetspeed_title, R.xml.prefs_system_detailednetspeed);
						return true;
					}
				});

				findPreference("pref_key_system_statusbaricons_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new SubFragment(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_statusbaricons_title, R.xml.prefs_system_hideicons);
						return true;
					}
				});

				findPreference("pref_key_system_batteryindicator_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_BatteryIndicator(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_batteryindicator_title, R.xml.prefs_system_batteryindicator);
						return true;
					}
				});

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

				findPreference("pref_key_system_popupnotif_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_PopupNotif(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_popupnotif_title, R.xml.prefs_system_popupnotif);
						return true;
					}
				});

				break;
			case "pref_key_system_cat_notifications":
				findPreference("pref_key_system_expandnotifs_apps").setOnPreferenceClickListener(openAppsEdit);
				findPreference("pref_key_system_expandnotifs_apps").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_system_expandnotifs", "1"), "1"));
				findPreference("pref_key_system_expandnotifs").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						findPreference("pref_key_system_expandnotifs_apps").setEnabled(!newValue.equals("1"));
						return true;
					}
				});

				if (Helpers.isNougat()) ((ListPreferenceEx)findPreference("pref_key_system_autogroupnotif")).setUnsupported(true);

				break;
			case "pref_key_system_cat_qs":
				findPreference("pref_key_system_qshaptics_ignore").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_system_qshaptics", "1"), "1"));
				findPreference("pref_key_system_qshaptics").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						findPreference("pref_key_system_qshaptics_ignore").setEnabled(!newValue.equals("1"));
						return true;
					}
				});

				((SeekBarPreference)findPreference("pref_key_system_qqsgridcolumns")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if (!fromUser) return;
						if (progress < 3) progress = 5;
						try {
							Settings.Secure.putInt(getActivity().getContentResolver(), "sysui_qqs_count", progress);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}
				});

				break;
			case "pref_key_system_cat_recents":
				findPreference("pref_key_system_hidefromrecents_apps").setOnPreferenceClickListener(openAppsEdit);
				findPreference("pref_key_system_recommended_first").setOnPreferenceClickListener(openRecentsActions);
				findPreference("pref_key_system_recommended_second").setOnPreferenceClickListener(openRecentsActions);
				findPreference("pref_key_system_recommended_third").setOnPreferenceClickListener(openRecentsActions);
				findPreference("pref_key_system_recommended_fourth").setOnPreferenceClickListener(openRecentsActions);
				break;
			case "pref_key_system_cat_applock":
				findPreference("pref_key_system_applock_list").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openLockedAppEdit(System.this, 0);
						return true;
					}
				});

				if (!checkSecurityPermission()) {
					Preference pref = findPreference("pref_key_system_applock_list");
					pref.setEnabled(false);
					pref.setSummary(R.string.launcher_privacyapps_fail);
				}

				break;
			case "pref_key_system_cat_lockscreen":
				findPreference("pref_key_system_noscreenlock_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_NoScreenLock(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_noscreenlock_title, R.xml.prefs_system_noscreenlock);
						return true;
					}
				});

				findPreference("pref_key_system_lockscreenshortcuts_cat").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						openSubFragment(new System_LockScreenShortcuts(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.system_lockscreenshortcuts_title, R.xml.prefs_system_lockscreenshortcuts);
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

				break;
			case "pref_key_system_cat_other":
				findPreference("pref_key_system_forceclose_apps").setOnPreferenceClickListener(openAppsEdit);

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
								viewIntent.setDataAndType(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/test/" + which), type);
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

				Helpers.prefs.edit().putInt("pref_key_system_animationscale_window", Math.round(getAnimationScale(0) * 10)).apply();
				((SeekBarPreference)findPreference("pref_key_system_animationscale_window")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						setAnimationScale(0, seekBar.getProgress() / 10f);
					}
				});

				Helpers.prefs.edit().putInt("pref_key_system_animationscale_transition", Math.round(getAnimationScale(1) * 10)).apply();
				((SeekBarPreference)findPreference("pref_key_system_animationscale_transition")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						setAnimationScale(1, seekBar.getProgress() / 10f);
					}
				});

				Helpers.prefs.edit().putInt("pref_key_system_animationscale_animator", Math.round(getAnimationScale(2) * 10)).apply();
				((SeekBarPreference)findPreference("pref_key_system_animationscale_animator")).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						setAnimationScale(2, seekBar.getProgress() / 10f);
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

				findPreference("pref_key_system_defaultusb_unsecure").setEnabled(!Objects.equals(Helpers.prefs.getString("pref_key_system_defaultusb", "none"), "none"));
				ListPreferenceEx defaultUSB = (ListPreferenceEx)findPreference("pref_key_system_defaultusb");
				defaultUSB.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue) {
						findPreference("pref_key_system_defaultusb_unsecure").setEnabled(!newValue.equals("none"));
						return true;
					}
				});

				if (!checkUSBPermission()) {
					Preference pref = findPreference("pref_key_system_defaultusb");
					pref.setEnabled(false);
					pref.setSummary(R.string.launcher_privacyapps_fail);
					findPreference("pref_key_system_defaultusb_unsecure").setEnabled(false);
				}

				if (!Helpers.isPiePlus()) ((CheckBoxPreferenceEx)findPreference("pref_key_system_magnifier")).setUnsupported(true);

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
			if (key != null) Helpers.prefs.edit().putString(key, data.getStringExtra("app")).apply();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("JavaReflectionInvocation")
	@SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
	float getAnimationScale(int type) {
		try {
			Class<?> smClass = Class.forName("android.os.ServiceManager");
			Method getService = smClass.getDeclaredMethod("getService", String.class);
			getService.setAccessible(true);
			Object manager = getService.invoke(smClass, "window");

			Class<?> wmsClass = Class.forName("android.view.IWindowManager$Stub");
			Method asInterface = wmsClass.getDeclaredMethod("asInterface", IBinder.class);
			asInterface.setAccessible(true);
			Object wm = asInterface.invoke(wmsClass, manager);

			Method getAnimationScale = wm.getClass().getDeclaredMethod("getAnimationScale", int.class);
			getAnimationScale.setAccessible(true);
			return (float)getAnimationScale.invoke(wm, type);
		} catch (Throwable t) {
			t.printStackTrace();
			return 0.0f;
		}
	}

	@SuppressWarnings("JavaReflectionInvocation")
	@SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
	void setAnimationScale(int type, float value) {
		try {
			Class<?> smClass = Class.forName("android.os.ServiceManager");
			Method getService = smClass.getDeclaredMethod("getService", String.class);
			getService.setAccessible(true);
			Object manager = getService.invoke(smClass, "window");

			Class<?> wmsClass = Class.forName("android.view.IWindowManager$Stub");
			Method asInterface = wmsClass.getDeclaredMethod("asInterface", IBinder.class);
			asInterface.setAccessible(true);
			Object wm = asInterface.invoke(wmsClass, manager);

			Method setAnimationScale = wm.getClass().getDeclaredMethod("setAnimationScale", int.class, float.class);
			setAnimationScale.setAccessible(true);
			setAnimationScale.invoke(wm, type, value);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private boolean checkSecurityPermission() {
		PackageManager pm = getActivity().getPackageManager();
		return pm.checkPermission(Helpers.ACCESS_SECURITY_CENTER, Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
	}

	private boolean checkUSBPermission() {
		PackageManager pm = getActivity().getPackageManager();
		return pm.checkPermission("android.permission.MANAGE_USB", Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
	}

	private boolean checkAnimationPermission() {
		PackageManager pm = getActivity().getPackageManager();
		return pm.checkPermission("android.permission.SET_ANIMATION_SCALE", Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
	}

}