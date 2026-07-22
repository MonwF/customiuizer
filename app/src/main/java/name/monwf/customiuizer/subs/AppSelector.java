package name.monwf.customiuizer.subs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AlertDialog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragmentWithSearch;
import name.monwf.customiuizer.mods.GlobalActions;
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
	boolean configFetched = false;
	String key = null;
	Runnable process = null;
	HashMap<Integer, ArrayList<String>> mPrivacyAppsMap = null;

	BroadcastReceiver configReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(GlobalActions.EVENT_PREFIX + "PUSHAPPCONFIG")) {
				String datatype = intent.getStringExtra("DATATYPE");
				if ("privacy".equals(datatype)) {
					configFetched = true;
					initialized = true;
					mPrivacyAppsMap = (HashMap<Integer, ArrayList<String>>) intent.getSerializableExtra("privacyAppsMap");
					getActivity().runOnUiThread(process);
				}
			}
		}
	};

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
						listView.setAdapter(new AppDataAdapter(context, Helpers.openWithAppsList, AppHelper.AppAdapterType.Mutli, key));
					} else if (share) {
						if (Helpers.shareAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(context, Helpers.shareAppsList, AppHelper.AppAdapterType.Mutli, key));
					} else {
						if (AppHelper.installedAppsList == null) return;
						listView.setAdapter(new AppDataAdapter(context, AppHelper.installedAppsList, AppHelper.AppAdapterType.Mutli, key, bwlist));
					}
				} else if (privacy) {
					if (AppHelper.installedAppsList == null) return;
					listView.setAdapter(new PrivacyAppAdapter(context, AppHelper.installedAppsList, mPrivacyAppsMap));
				} else if (applock) {
					if (AppHelper.installedAppsList == null) return;
					listView.setAdapter(new LockedAppAdapter(context, AppHelper.installedAppsList));
				} else if (customTitles) {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(context, Helpers.launchableAppsList, AppHelper.AppAdapterType.CustomTitles, key));
				} else if (standalone && key != null) {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(context, Helpers.launchableAppsList, AppHelper.AppAdapterType.Standalone, key));
				} else if (activity) {
					if (AppHelper.installedAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(context, AppHelper.installedAppsList, AppHelper.AppAdapterType.Default, key));
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
							openSubFragment(activitySelect, args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.select_activity, R.layout.prefs_app_selector);
						} else if (privacy) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							AppSelector.this.toggleAppPrivacy(app);
							PrivacyAppAdapter adapter = (PrivacyAppAdapter)parent.getAdapter();
							adapter.notifyDataSetChanged();
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
			new Thread(() -> {
				try {
					Thread.sleep(animDur);
				} catch (Throwable e) {
				}
				if (act != null) try {
					if (activity || privacy || applock || (multi && key != null)) {
						if (openwith) {
							if (Helpers.openWithAppsList == null) {
								Helpers.getOpenWithApps(act);
							}
						} else if (share) {
							if (Helpers.shareAppsList == null) {
								Helpers.getShareApps(act);
							}
						} else {
							if (AppHelper.installedAppsList == null) {
								Helpers.getInstalledApps(act);
							}
						}
					} else {
						if (Helpers.launchableAppsList == null) {
							Helpers.getLaunchableApps(act);
						}
					}
					if (privacy && !configFetched) {
						return;
					}
					initialized = true;
					act.runOnUiThread(process);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}).start();

			registerReceivers();
		}
	}

	void registerReceivers() {
		if (privacy) {
			getValidContext().registerReceiver(configReceiver, new IntentFilter(GlobalActions.EVENT_PREFIX + "PUSHAPPCONFIG"), Context.RECEIVER_EXPORTED);
			Intent intent = new Intent(GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG");
			intent.putExtra("DATATYPE", "privacy");
			intent.setPackage("com.miui.home");
			getValidContext().sendBroadcast(intent);
		}
	}

	void unregisterReceivers() {
		try {
			getValidContext().unregisterReceiver(configReceiver);
		} catch (Throwable t) {}
	}

	@Override
	public void onDestroy() {
		unregisterReceivers();
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == getTargetRequestCode()) {
			getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void toggleAppPrivacy(AppData app) {
		if (mPrivacyAppsMap == null) return;
		int user = app.user;
		ArrayList<String> privacyApps = mPrivacyAppsMap.get(user);
		if (privacyApps == null) {
			privacyApps = new ArrayList<String>();
			mPrivacyAppsMap.put(user, privacyApps);
		}
		boolean privacy = !privacyApps.contains(app.pkgName);
		if (privacyApps.contains(app.pkgName)) {
			privacyApps.remove(app.pkgName);
		} else {
			privacyApps.add(app.pkgName);
		}
		Intent intent = new Intent(GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG");
		intent.putExtra("DATATYPE", "privacy_change");
		intent.putExtra("app", app.pkgName);
		intent.putExtra("userId", app.user);
		intent.putExtra("privacy", privacy);
		intent.setPackage("com.miui.home");
		getValidContext().sendBroadcast(intent);
	}
}