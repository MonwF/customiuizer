package name.mikanoshi.customiuizer.subs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SharedPrefsProvider;
import name.mikanoshi.customiuizer.SubFragmentWithSearch;
import name.mikanoshi.customiuizer.utils.AppData;
import name.mikanoshi.customiuizer.utils.AppDataAdapter;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.LockedAppAdapter;
import name.mikanoshi.customiuizer.utils.PrivacyAppAdapter;

public class AppSelector extends SubFragmentWithSearch {

	boolean initialized = false;
	boolean standalone = false;
	boolean multi = false;
	boolean privacy = false;
	boolean applock = false;
	boolean customTitles = false;
	boolean share = false;
	boolean openwith = false;
	boolean activity = false;
	String key = null;
	Runnable process = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);

		standalone = getArguments().getBoolean("standalone");
		multi = getArguments().getBoolean("multi");
		privacy = getArguments().getBoolean("privacy");
		applock = getArguments().getBoolean("applock");
		customTitles = getArguments().getBoolean("custom_titles");
		share = getArguments().getBoolean("share");
		openwith = getArguments().getBoolean("openwith");
		activity = getArguments().getBoolean("activity");
		key = getArguments().getString("key");

		process = new Runnable() {
			@Override
			public void run() {
				if (multi && key != null) {
					if (openwith) {
						if (Helpers.openWithAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(getContext(), Helpers.openWithAppsList, Helpers.AppAdapterType.Mutli, key));
					} else if (share) {
						if (Helpers.shareAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(getContext(), Helpers.shareAppsList, Helpers.AppAdapterType.Mutli, key));
					} else {
						if (Helpers.installedAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(getContext(), Helpers.installedAppsList, Helpers.AppAdapterType.Mutli, key));
					}
				} else if (privacy) {
					if (Helpers.installedAppsList == null) return;
					listView.setAdapter(new PrivacyAppAdapter(getContext(), Helpers.installedAppsList));
				} else if (applock) {
					if (Helpers.installedAppsList == null) return;
					listView.setAdapter(new LockedAppAdapter(getContext(), Helpers.installedAppsList));
				} else if (customTitles) {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(getContext(), Helpers.launchableAppsList, Helpers.AppAdapterType.CustomTitles, key));
				} else if (standalone && key != null) {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(getContext(), Helpers.launchableAppsList, Helpers.AppAdapterType.Standalone, key));
				} else if (activity) {
					if (Helpers.installedAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(getContext(), Helpers.installedAppsList, Helpers.AppAdapterType.Default, key));
				} else {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(getContext(), Helpers.launchableAppsList));
				}
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (multi && key != null) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							Set<String> selectedApps = new LinkedHashSet<String>(Helpers.prefs.getStringSet(key, new LinkedHashSet<String>()));
							if (selectedApps.contains(app.pkgName))
								selectedApps.remove(app.pkgName);
							else
								selectedApps.add(app.pkgName);
							Helpers.prefs.edit().putStringSet(key, selectedApps).apply();
							((AppDataAdapter)parent.getAdapter()).updateSelectedApps();
						} else if (activity) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							Bundle args = new Bundle();
							args.putString("key", key);
							args.putString("package", app.pkgName);
							ActivitySelector activitySelect = new ActivitySelector();
							activitySelect.setTargetFragment(AppSelector.this, getTargetRequestCode());
							openSubFragment(activitySelect, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_activity, R.layout.prefs_app_selector);
						} else if (privacy) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							try {
								@SuppressLint("WrongConstant") Object mSecurityManager = getActivity().getSystemService("security");
								Method isPrivacyApp = mSecurityManager.getClass().getDeclaredMethod("isPrivacyApp", String.class, int.class);
								isPrivacyApp.setAccessible(true);
								Method setPrivacyApp = mSecurityManager.getClass().getDeclaredMethod("setPrivacyApp", String.class, int.class, boolean.class);
								setPrivacyApp.setAccessible(true);
								setPrivacyApp.invoke(mSecurityManager, app.pkgName, 0, !(boolean)isPrivacyApp.invoke(mSecurityManager, app.pkgName, 0));
								PrivacyAppAdapter adapter = (PrivacyAppAdapter)parent.getAdapter();
								adapter.notifyDataSetChanged();
								getActivity().getContentResolver().notifyChange(Uri.parse("content://com.miui.securitycenter.provider/update_privacyapps_icon"), null);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						} else if (applock) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							try {
								@SuppressLint("WrongConstant") Object mSecurityManager = getActivity().getSystemService("security");
								Method getApplicationAccessControlEnabled = mSecurityManager.getClass().getDeclaredMethod("getApplicationAccessControlEnabled", String.class);
								getApplicationAccessControlEnabled.setAccessible(true);
								Method setApplicationAccessControlEnabled = mSecurityManager.getClass().getDeclaredMethod("setApplicationAccessControlEnabled", String.class, boolean.class);
								setApplicationAccessControlEnabled.setAccessible(true);
								setApplicationAccessControlEnabled.invoke(mSecurityManager, app.pkgName, !(boolean)getApplicationAccessControlEnabled.invoke(mSecurityManager, app.pkgName));
								LockedAppAdapter adapter = (LockedAppAdapter)parent.getAdapter();
								adapter.notifyDataSetChanged();
							} catch (Throwable t) {
								t.printStackTrace();
							}
						} else if (customTitles) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							Helpers.showInputDialog(getActivity(), key + ":" + app.pkgName + "|" + app.actName, R.string.launcher_renameapps_modified, new Helpers.InputCallback() {
								@Override
								public void onInputFinished(String key, String text){
									if (TextUtils.isEmpty(text))
										Helpers.prefs.edit().remove(key).apply();
									else
										Helpers.prefs.edit().putString(key, text).apply();
									((AppDataAdapter)parent.getAdapter()).notifyDataSetChanged();
									getActivity().getContentResolver().notifyChange(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/pref/string/" + key), null);
								}
							});
						} else {
							Intent intent = new Intent(getContext(), this.getClass());
							AppData app = (AppData)parent.getAdapter().getItem(position);
							if (app.pkgName.equals("") && app.actName.equals(""))
								intent.putExtra("app", "");
							else
								intent.putExtra("app", app.pkgName + "|" + app.actName);
							getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
							finish();
						}
					}
				});
				if (getView() != null) getView().findViewById(R.id.am_progressBar).setVisibility(View.GONE);
				initialized = true;
			}
		};

		new Thread() {
			@Override
			public void run() {
				try { sleep(animDur); } catch (Throwable e) {}
				try {
					if (activity || privacy || applock || (multi && key != null)) {
						if (openwith) {
							if (Helpers.openWithAppsList == null) Helpers.getOpenWithApps(getActivity());
						} else if (share) {
							if (Helpers.shareAppsList == null) Helpers.getShareApps(getActivity());
						} else {
							if (Helpers.installedAppsList == null) Helpers.getInstalledApps(getActivity());
						}
					} else {
						if (Helpers.launchableAppsList == null) Helpers.getLaunchableApps(getActivity());
					}
					getActivity().runOnUiThread(process);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (initialized) process.run();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == getTargetRequestCode()) {
			getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}