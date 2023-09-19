package name.monwf.customiuizer.subs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragmentWithSearch;
import name.monwf.customiuizer.utils.AppData;
import name.monwf.customiuizer.utils.AppDataAdapter;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;
import name.monwf.customiuizer.utils.Helpers.MimeType;
import name.monwf.customiuizer.utils.LockedAppAdapter;
import name.monwf.customiuizer.utils.PrivacyAppAdapter;

public class AppSelector extends SubFragmentWithSearch {

	boolean initialized = false;
	boolean standalone = false;
	boolean multi = false;
	boolean bwlist = false;
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
		bwlist = getArguments().getBoolean("bw");
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
				Context context = getValidContext();
				if (context == null || !AppSelector.this.isAdded()) return;
				if (multi && key != null) {
					if (openwith) {
						if (Helpers.openWithAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(context, Helpers.openWithAppsList, Helpers.AppAdapterType.Mutli, key));
					} else if (share) {
						if (Helpers.shareAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(context, Helpers.shareAppsList, Helpers.AppAdapterType.Mutli, key));
					} else {
						if (Helpers.installedAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(context, Helpers.installedAppsList, Helpers.AppAdapterType.Mutli, key, bwlist));
					}
				} else if (privacy) {
					if (Helpers.installedAppsList == null) return;
					listView.setAdapter(new PrivacyAppAdapter(context, Helpers.installedAppsList));
				} else if (applock) {
					if (Helpers.installedAppsList == null) return;
					listView.setAdapter(new LockedAppAdapter(context, Helpers.installedAppsList));
				} else if (customTitles) {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(context, Helpers.launchableAppsList, Helpers.AppAdapterType.CustomTitles, key));
				} else if (standalone && key != null) {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(context, Helpers.launchableAppsList, Helpers.AppAdapterType.Standalone, key));
				} else if (activity) {
					if (Helpers.installedAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(context, Helpers.installedAppsList, Helpers.AppAdapterType.Default, key));
				} else {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(context, Helpers.launchableAppsList));
				}
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (multi && key != null) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							Set<String> selectedApps = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key, new LinkedHashSet<String>()));
							if (bwlist) {
								Set<String> selectedAppsBlack = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key + "_black", new LinkedHashSet<String>()));
								if (selectedApps.contains(app.pkgName)) {
									selectedApps.remove(app.pkgName);
									selectedAppsBlack.add(app.pkgName);
								} else if (selectedAppsBlack.contains(app.pkgName)) {
									selectedApps.remove(app.pkgName);
									selectedAppsBlack.remove(app.pkgName);
								} else {
									selectedApps.add(app.pkgName);
									selectedAppsBlack.remove(app.pkgName);
								}
								AppHelper.appPrefs.edit().putStringSet(key + "_black", selectedAppsBlack).apply();
							} else if (selectedApps.contains(share || openwith ? app.pkgName + "|" + app.user : app.pkgName)) {
								selectedApps.remove(share || openwith ? app.pkgName + "|" + app.user : app.pkgName);
							} else {
								selectedApps.add(share || openwith ? app.pkgName + "|" + app.user : app.pkgName);
								if (openwith) {
									String mimeKey = key + "_" + app.pkgName + "|" + app.user;
									int mimeFlags = AppHelper.getIntOfAppPrefs(mimeKey, MimeType.ALL);
									final boolean[] checkedTypes = new boolean[] {
										(mimeFlags & MimeType.IMAGE) == MimeType.IMAGE,
										(mimeFlags & MimeType.AUDIO) == MimeType.AUDIO,
										(mimeFlags & MimeType.VIDEO) == MimeType.VIDEO,
										(mimeFlags & MimeType.DOCUMENT) == MimeType.DOCUMENT,
										(mimeFlags & MimeType.ARCHIVE) == MimeType.ARCHIVE,
										(mimeFlags & MimeType.LINK) == MimeType.LINK,
										(mimeFlags & MimeType.OTHERS) == MimeType.OTHERS,
									};
									AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
									builder.setTitle(R.string.system_cleanopenwith_datatype);
									builder.setMultiChoiceItems(R.array.mimetypes, checkedTypes, new DialogInterface.OnMultiChoiceClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which, boolean isChecked) {
											checkedTypes[which] = isChecked;
										}
									});
									builder.setCancelable(true);
									builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											int sum = 0;
											int order = 0;
											for (boolean checkedType: checkedTypes) {
												if (checkedType) sum += Math.pow(2, order);
												order++;
											}
											AppHelper.appPrefs.edit().putInt(mimeKey, sum).apply();
										}
									});
									builder.show();
								}
							}
							AppHelper.appPrefs.edit().putStringSet(key, selectedApps).apply();
							((AppDataAdapter)parent.getAdapter()).updateSelectedApps();
						} else if (activity) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							final Bundle args = new Bundle();
							args.putString("key", key);
							args.putString("package", app.pkgName);
							args.putInt("user", app.user);
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
								setPrivacyApp.invoke(mSecurityManager, app.pkgName, app.user, !(boolean)isPrivacyApp.invoke(mSecurityManager, app.pkgName, app.user));
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
								Method getApplicationAccessControlEnabledAsUser = mSecurityManager.getClass().getDeclaredMethod("getApplicationAccessControlEnabledAsUser", String.class, int.class);
								getApplicationAccessControlEnabledAsUser.setAccessible(true);
								Method setApplicationAccessControlEnabledForUser = mSecurityManager.getClass().getDeclaredMethod("setApplicationAccessControlEnabledForUser", String.class, boolean.class, int.class);
								setApplicationAccessControlEnabledForUser.setAccessible(true);
								setApplicationAccessControlEnabledForUser.invoke(mSecurityManager, app.pkgName, !(boolean)getApplicationAccessControlEnabledAsUser.invoke(mSecurityManager, app.pkgName, app.user), app.user);
								LockedAppAdapter adapter = (LockedAppAdapter)parent.getAdapter();
								adapter.notifyDataSetChanged();
							} catch (Throwable t) {
								t.printStackTrace();
							}
						} else if (customTitles) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							AppHelper.showInputDialog(getActivity(), key + ":" + app.pkgName + "|" + app.actName + "|" + app.user, R.string.launcher_renameapps_modified, 0, 1, new Helpers.InputCallback() {
								@Override
								public void onInputFinished(String key, String text){
									if (TextUtils.isEmpty(text))
										AppHelper.appPrefs.edit().remove(key).apply();
									else
										AppHelper.appPrefs.edit().putString(key, text).apply();
									((AppDataAdapter)parent.getAdapter()).notifyDataSetChanged();
								}
							});
						} else {
							final Intent intent = new Intent(getActivity(), this.getClass());
							AppData app = (AppData)parent.getAdapter().getItem(position);
							if (app.pkgName.equals("") && app.actName.equals(""))
								intent.putExtra("app", "");
							else
								intent.putExtra("app", app.pkgName + "|" + app.actName);
							intent.putExtra("user", app.user);
							getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
							finish();
						}
					}
				});
				if (getView() != null) getView().findViewById(R.id.am_progressBar).setVisibility(View.GONE);
			}
		};
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity act = getActivity();
		if (initialized) {
			process.run();
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						sleep(animDur);
					} catch (Throwable e) {
					}
					if (act != null) try {
						if (activity || privacy || applock || (multi && key != null)) {
							if (openwith) {
								if (Helpers.openWithAppsList == null) Helpers.getOpenWithApps(act);
							} else if (share) {
								if (Helpers.shareAppsList == null) Helpers.getShareApps(act);
							} else {
								if (Helpers.installedAppsList == null)
									Helpers.getInstalledApps(act);
							}
						} else {
							if (Helpers.launchableAppsList == null) Helpers.getLaunchableApps(act);
						}
						initialized = true;
						act.runOnUiThread(process);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
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