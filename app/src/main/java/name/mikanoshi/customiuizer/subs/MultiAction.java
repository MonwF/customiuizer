package name.mikanoshi.customiuizer.subs;

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

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.SpinnerEx;
import name.mikanoshi.customiuizer.prefs.SpinnerExFake;
import name.mikanoshi.customiuizer.utils.Helpers;

public class MultiAction extends SubFragment {

	private SpinnerExFake appLaunch = null;
	private SpinnerExFake shortcutLaunch = null;
	private String key = null;
	private String appValue = null;
	private String shortcutValue = null;
	private String shortcutName = null;
	private String shortcutIcon = null;
	private String shortcutIconPath = null;
	private Intent shortcutIntent = null;

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		key = args.getString("key");

		SpinnerEx actionSpinner = getView().findViewById(R.id.action);
		actionSpinner.setTag(key + "_action");
		if (key.equals("pref_key_launcher_swipedown"))
		actionSpinner.addDisabledItems(1);
		actionSpinner.init(Helpers.prefs.getInt(key + "_action", 1));
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
		appLaunch.setValue(appValue != null ? appValue : Helpers.prefs.getString(key + "_app", null));
		appLaunch.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
			@Override
			public void onChildViewAdded(View parent, View child) {
				if (child instanceof TextView && child.getId() == android.R.id.text1) {
					TextView appLaunchLabel = ((TextView)child);

					String pkgAppName = appLaunch.getValue();
					if (pkgAppName != null) {
						CharSequence label = Helpers.getAppName(getContext(), pkgAppName);
						if (label != null) {
							appLaunchLabel.setText(label);
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
		shortcutLaunch.setValue(shortcutValue != null ? shortcutValue : Helpers.prefs.getString(key + "_shortcut", null));
		shortcutLaunch.addValue(key + "_shortcut_intent", shortcutIntent);
		shortcutLaunch.addValue(key + "_shortcut_name", shortcutName);

		shortcutIconPath = getActivity().getFilesDir() + "/shortcuts" + "/" + key + "_shortcut.png";
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
		toggleSpinner.init(Helpers.prefs.getInt(key + "_toggle", 1));
	}

	void updateControls(SpinnerEx spinner, int position) {
		if (getView() == null) return;
		View apps = getView().findViewById(R.id.apps_group);
		View shortcuts = getView().findViewById(R.id.shortcuts_group);
		View toggles = getView().findViewById(R.id.toggles_group);

		apps.setVisibility(View.GONE);
		shortcuts.setVisibility(View.GONE);
		toggles.setVisibility(View.GONE);
		if (spinner.entryValues[position] == 8)
			apps.setVisibility(View.VISIBLE);
		else if (spinner.entryValues[position] == 9)
			shortcuts.setVisibility(View.VISIBLE);
		else if (spinner.entryValues[position] == 10)
			toggles.setVisibility(View.VISIBLE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 0) appValue = data.getStringExtra("activity");
			if (requestCode == 1) {
				shortcutValue = data.getStringExtra("shortcut_contents");
				shortcutName = data.getStringExtra("shortcut_name");
				shortcutIcon = data.getStringExtra("shortcut_icon");
				shortcutIntent = data.getParcelableExtra("shortcut_intent");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public void saveSharedPrefs() {
		File tmpIconFile = new File(getActivity().getFilesDir() + "/shortcuts/tmp.png");
		if (tmpIconFile.exists()) {
			File prefIconFile = new File(shortcutIconPath);
			prefIconFile.delete();
			tmpIconFile.renameTo(prefIconFile);
		}
		super.saveSharedPrefs();
	}

	@Override
	public void onDestroy() {
		File tmpIconFile = new File(getActivity().getFilesDir() + "/shortcuts/tmp.png");
		if (tmpIconFile.exists()) tmpIconFile.delete();
		super.onDestroy();
	}

}