package name.monwf.customiuizer.subs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.prefs.SpinnerEx;
import name.monwf.customiuizer.prefs.SpinnerExFake;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class MultiAction extends SubFragment {

	private SpinnerExFake appLaunch = null;
	private SpinnerExFake shortcutLaunch = null;
	private SpinnerExFake activityLaunch = null;
	private String key = null;
	private String appValue = null;
	private int appUser = -1;
	private String activityValue = null;
	private int activityUser = -1;
	private String shortcutValue = null;
	private String shortcutName = null;
	private String shortcutIcon = null;
	private String shortcutIconPath = null;
	private Intent shortcutIntent = null;

	public enum Actions {
		NAVBAR, LAUNCHER, CONTROLS, LOCKSCREEN, LAUNCH, STATUSBAR
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		key = args.getString("key");
		Actions actions = Actions.values()[args.getInt("actions")];

		int entriesResId = 0;
		int entryValuesResId = 0;

		switch (actions) {
			case NAVBAR:
				entriesResId = R.array.global_actions_navbar;
				entryValuesResId = R.array.global_actions_navbar_val;
				break;
			case LAUNCHER:
				entriesResId = R.array.global_actions_launcher;
				entryValuesResId = R.array.global_actions_launcher_val;
				break;
			case CONTROLS:
				entriesResId = R.array.global_actions_controls;
				entryValuesResId = R.array.global_actions_controls_val;
				break;
			case STATUSBAR:
				entriesResId = R.array.global_actions_statusbar;
				entryValuesResId = R.array.global_actions_statusbar_val;
				break;
			case LOCKSCREEN:
				entriesResId = R.array.global_lockscreen_actions;
				entryValuesResId = R.array.global_lockscreen_actions_val;
				break;
			case LAUNCH:
				entriesResId = R.array.global_launch_actions;
				entryValuesResId = R.array.global_launch_actions_val;
				break;
		}

		SpinnerEx actionSpinner = getView().findViewById(R.id.action);
		actionSpinner.entries = getResources().getStringArray(entriesResId);
		actionSpinner.entryValues = getResources().getIntArray(entryValuesResId);
		actionSpinner.setTag(key + "_action");
		//if (key.equals("pref_key_launcher_swipedown"))
		//actionSpinner.addDisabledItems(1);
		actionSpinner.init(AppHelper.getIntOfAppPrefs(key + "_action", 1));
		actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateControls((SpinnerEx)parent, position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				updateControls((SpinnerEx)parent, 0);
			}
		});

		appLaunch = getView().findViewById(R.id.app_to_launch);
		appLaunch.setTag(key + "_app");
		appLaunch.setValue(appValue != null ? appValue : AppHelper.getStringOfAppPrefs(key + "_app", null));
		appLaunch.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
			@Override
			@SuppressLint("SetTextI18n")
			public void onChildViewAdded(View parent, View child) {
				if (child instanceof TextView && child.getId() == android.R.id.text1) {
					TextView appLaunchLabel = ((TextView)child);

					String pkgAppName = appLaunch.getValue();
					if (pkgAppName != null) {
						CharSequence label = Helpers.getAppName(getContext(), pkgAppName);
						if (label != null) {
							appLaunchLabel.setText(label + ((appUser != -1 ? appUser : AppHelper.getIntOfAppPrefs(key + "_app_user", 0)) != 0 ? " *" : ""));
							return;
						}
					}

					appLaunchLabel.setText(R.string.notselected);
					appLaunchLabel.setAlpha(0.5f);
				}
			}

			@Override
			public void onChildViewRemoved(View parent, View child) {}
		});
		appLaunch.setOnTouchListener(new View.OnTouchListener() {
			@Override
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					AppSelector appSelect = new AppSelector();
					appSelect.setTargetFragment(MultiAction.this, 0);
					openSubFragment(appSelect, null, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
				}
				return false;
			}
		});

		shortcutLaunch = getView().findViewById(R.id.shortcut_to_launch);
		shortcutLaunch.setTag(key + "_shortcut");
		shortcutLaunch.setValue(shortcutValue != null ? shortcutValue : AppHelper.getStringOfAppPrefs(key + "_shortcut", null));
		shortcutLaunch.addValue(key + "_shortcut_intent", shortcutIntent);
		shortcutLaunch.addValue(key + "_shortcut_name", shortcutName);

		shortcutIconPath = getContext().getFilesDir() + "/shortcuts/" + key + "_shortcut.png";
		File shortcutIconFile;
		if (shortcutIcon != null)
			shortcutIconFile = new File(shortcutIcon);
		else
			shortcutIconFile = new File(shortcutIconPath);
		if (shortcutIconFile.exists()) {
			ImageView sIcon = getView().findViewById(R.id.shortcut_icon);
			Bitmap sBmp = BitmapFactory.decodeFile(shortcutIconFile.getAbsolutePath());
			sIcon.setImageBitmap(sBmp);
		}

		shortcutLaunch.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
			@Override
			public void onChildViewAdded(View parent, View child) {
				if (child instanceof TextView && child.getId() == android.R.id.text1) {
					TextView shortcutLaunchLabel = ((TextView)child);

					String pkgAppName = shortcutLaunch.getValue();
					if (pkgAppName != null) {
						CharSequence label = Helpers.getAppName(getContext(), pkgAppName);
						if (label != null) {
							shortcutLaunchLabel.setText(label);
							return;
						}
					}

					shortcutLaunchLabel.setText(R.string.notselected);
					shortcutLaunchLabel.setAlpha(0.5f);
				}
			}

			@Override
			public void onChildViewRemoved(View parent, View child) {}
		});
		shortcutLaunch.setOnTouchListener(new View.OnTouchListener() {
			@Override
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ShortcutSelector shortcutSelect = new ShortcutSelector();
					shortcutSelect.setTargetFragment(MultiAction.this, 1);
					Bundle args = new Bundle();
					args.putString("key", key + "_shortcut");
					openSubFragment(shortcutSelect, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_shortcut, R.layout.prefs_app_selector);
				}
				return false;
			}
		});

		SpinnerEx toggleSpinner = getView().findViewById(R.id.toggle);
		toggleSpinner.setTag(key + "_toggle");
		toggleSpinner.init(AppHelper.getIntOfAppPrefs(key + "_toggle", 1));

		activityLaunch = getView().findViewById(R.id.activity_to_launch);
		activityLaunch.setTag(key + "_activity");
		activityLaunch.setValue(activityValue != null ? activityValue : AppHelper.getStringOfAppPrefs(key + "_activity", null));
		String val = activityLaunch.getValue();
		((TextView)getView().findViewById(R.id.activity_class)).setText(val != null && !val.equals("") ? val.replace("|", "/\u200B").replace(".", ".\u200B") : "");
		activityLaunch.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
			@Override
			@SuppressLint("SetTextI18n")
			public void onChildViewAdded(View parent, View child) {
				if (child instanceof TextView && child.getId() == android.R.id.text1) {
					TextView appLaunchLabel = ((TextView)child);

					String pkgAppName = activityLaunch.getValue();
					if (pkgAppName != null) {
						CharSequence label = Helpers.getAppName(getContext(), pkgAppName, true);
						if (label != null) {
							appLaunchLabel.setText(label + ((activityUser != -1 ? activityUser : AppHelper.getIntOfAppPrefs(key + "_activity_user", 0)) != 0 ? " *" : ""));
							return;
						}
					}

					appLaunchLabel.setText(R.string.notselected);
					appLaunchLabel.setAlpha(0.5f);
				}
			}

			@Override
			public void onChildViewRemoved(View parent, View child) {}
		});
		activityLaunch.setOnTouchListener(new View.OnTouchListener() {
			@Override
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Bundle args = new Bundle();
					args.putBoolean("activity", true);
					AppSelector activitySelect = new AppSelector();
					activitySelect.setTargetFragment(MultiAction.this, 2);
					openSubFragment(activitySelect, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
				}
				return false;
			}
		});
	}

	void updateControls(SpinnerEx spinner, int position) {
		if (getView() == null) return;
		View apps = getView().findViewById(R.id.apps_group);
		View shortcuts = getView().findViewById(R.id.shortcuts_group);
		View activities = getView().findViewById(R.id.activities_group);
		View toggles = getView().findViewById(R.id.toggles_group);

		apps.setVisibility(View.GONE);
		shortcuts.setVisibility(View.GONE);
		activities.setVisibility(View.GONE);
		toggles.setVisibility(View.GONE);
		if (spinner.entryValues[position] == 8)
			apps.setVisibility(View.VISIBLE);
		else if (spinner.entryValues[position] == 9)
			shortcuts.setVisibility(View.VISIBLE);
		else if (spinner.entryValues[position] == 10)
			toggles.setVisibility(View.VISIBLE);
		else if (spinner.entryValues[position] == 20)
			activities.setVisibility(View.VISIBLE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 0) {
				appValue = data.getStringExtra("app");
				appUser = data.getIntExtra("user", 0);
			}
			if (requestCode == 1) {
				shortcutValue = data.getStringExtra("shortcut_contents");
				shortcutName = data.getStringExtra("shortcut_name");
				shortcutIcon = data.getStringExtra("shortcut_icon");
				shortcutIntent = data.getParcelableExtra("shortcut_intent");
			}
			if (requestCode == 2) {
				activityValue = data.getStringExtra("activity");
				activityUser = data.getIntExtra("user", 0);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void saveSharedPrefs() {
		File tmpIconFile = new File(getContext().getFilesDir() + "/shortcuts/tmp.png");
		if (tmpIconFile.exists()) {
			File prefIconFile = new File(shortcutIconPath);
			prefIconFile.delete();
			tmpIconFile.renameTo(prefIconFile);
		}
		if (appUser != -1) AppHelper.appPrefs.edit().putInt(key + "_app_user", appUser).apply();
		if (activityUser != -1) AppHelper.appPrefs.edit().putInt(key + "_activity_user", activityUser).apply();
		super.saveSharedPrefs();
	}

	@Override
	public void onDestroy() {
		File tmpIconFile = new File(getContext().getFilesDir() + "/shortcuts/tmp.png");
		if (tmpIconFile.exists()) tmpIconFile.delete();
		super.onDestroy();
	}

}