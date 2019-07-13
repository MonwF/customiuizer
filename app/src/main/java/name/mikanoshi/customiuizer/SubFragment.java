package name.mikanoshi.customiuizer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import miui.app.ActionBar;
import miui.widget.ClearableEditText;
import name.mikanoshi.customiuizer.prefs.SpinnerEx;
import name.mikanoshi.customiuizer.prefs.SpinnerExFake;
import name.mikanoshi.customiuizer.subs.AppSelector;
import name.mikanoshi.customiuizer.subs.MultiAction;
import name.mikanoshi.customiuizer.utils.ColorCircle;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.ModData;

public class SubFragment extends PreferenceFragmentBase {

	private int baseResId = 0;
	private int resId = 0;
	private int titleId = 0;
	private float order = 100.0f;
	private String scrollToKey = null;
	public boolean padded = true;
	Helpers.SettingsType settingsType = Helpers.SettingsType.Preference;
	Helpers.ActionBarType abType = Helpers.ActionBarType.Edit;

	public SubFragment() {
		super();
		this.setRetainInstance(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		settingsType = Helpers.SettingsType.values()[getArguments().getInt("settingsType")];
		abType = Helpers.ActionBarType.values()[getArguments().getInt("abType")];
		baseResId = getArguments().getInt("baseResId");
		resId = getArguments().getInt("contentResId");
		titleId = getArguments().getInt("titleResId");
		order = getArguments().getFloat("order") + 10.0f;
		scrollToKey = getArguments().getString("scrollToKey", null);

		if (resId == 0) {
			getActivity().finish();
			return;
		}

		if (settingsType == Helpers.SettingsType.Preference) {
			super.onCreate(savedInstanceState, resId);
			addPreferencesFromResource(resId);
		} else {
			super.onCreate(savedInstanceState);
		}
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadSharedPrefs();

		ActionBar actionBar = getActionBar();
		if (actionBar != null)
		if (abType == Helpers.ActionBarType.Edit) {
			actionBar.setCustomView(getResources().getIdentifier("edit_mode_title", "layout", "miui"));
			actionBar.setDisplayShowCustomEnabled(true);

			View customView = actionBar.getCustomView();
			((TextView)customView.findViewById(android.R.id.title)).setText(titleId);

			TextView cancelBtn = customView.findViewById(android.R.id.button1);
			cancelBtn.setBackgroundResource(getResources().getIdentifier(Helpers.isNightMode(getContext()) ? "action_mode_title_button_cancel_dark" : "action_mode_title_button_cancel_light", "drawable", "miui"));
			cancelBtn.setText(null);
			cancelBtn.setContentDescription(getText(android.R.string.cancel));
			cancelBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					finish();
				}
			});
			TextView applyBtn = customView.findViewById(android.R.id.button2);
			applyBtn.setBackgroundResource(getResources().getIdentifier(Helpers.isNightMode(getContext()) ? "action_mode_title_button_confirm_dark" : "action_mode_title_button_confirm_light", "drawable", "miui"));
			applyBtn.setText(null);
			applyBtn.setContentDescription(getText(android.R.string.ok));
			applyBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					saveSharedPrefs();
					finish();
				}
			});
		} else {
			actionBar.setTitle(titleId);
		}

		if (scrollToKey != null) try {
			ListView listView = getView().findViewById(android.R.id.list);
			int order = 0;
			for (ModData mod: Helpers.allModsList)
			if (mod.key.equals(scrollToKey)) {
				order = mod.order;
				break;
			}
			scrollToKey = null;
			listView.clearFocus();
			int fOrder = order;
			listView.post(new Runnable() {
				@Override
				public void run() {
					listView.setSelection(fOrder);
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public View onInflateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
		if (settingsType == Helpers.SettingsType.Preference)
		return baseResId != 0 ? inflater.inflate(baseResId, group, false) : super.onInflateView(inflater, group, bundle);

		View view = inflater.inflate(padded ? R.layout.prefs_common_padded : R.layout.prefs_common, group, false);
		inflater.inflate(resId, (FrameLayout)view);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.setTranslationZ(order);
	}

	public void saveSharedPrefs() {
		if (getView() == null) Log.e("miuizer", "View not yet ready!");
		ArrayList<View> nViews = Helpers.getChildViewsRecursive(getView().findViewById(R.id.container), false);
		for (View nView : nViews)
		if (nView != null) try {
			if (nView.getTag() != null)
			if (nView instanceof TextView)
				Helpers.prefs.edit().putString((String)nView.getTag(), ((TextView)nView).getText().toString()).apply();
			else if (nView instanceof SpinnerExFake) {
				Helpers.prefs.edit().putString((String)nView.getTag(), ((SpinnerExFake)nView).getValue()).apply();
				((SpinnerExFake)nView).applyOthers();
			} else if (nView instanceof SpinnerEx)
				Helpers.prefs.edit().putInt((String)nView.getTag(), ((SpinnerEx)nView).getSelectedArrayValue()).apply();
			else if (nView instanceof ColorCircle) {
				Helpers.prefs.edit().putInt((String)nView.getTag(), ((ColorCircle)nView).getColor()).apply();
				((ColorCircle)nView).saveCirclePosition();
			}
		} catch (Throwable e) {
			Log.e("miuizer", "Cannot save sub preference!");
		}
	}

	public void loadSharedPrefs() {
		if (getView() == null) Log.e("miuizer", "View not yet ready!");
		ArrayList<View> nViews = Helpers.getChildViewsRecursive(getView().findViewById(R.id.container), false);
		for (View nView : nViews)
		if (nView != null) try {
			if (nView.getTag() != null)
			if (nView instanceof TextView) {
				((TextView)nView).setText(Helpers.prefs.getString((String)nView.getTag(), ""));
				if (nView instanceof ClearableEditText) nView.setBackgroundResource(getResources().getIdentifier(Helpers.isNightMode(getContext()) ? "edit_text_bg_dark" : "edit_text_bg_light", "drawable", "miui"));
			}
		} catch (Throwable e) {
			Log.e("miuizer", "Cannot load sub preference!");
		}
	}

	public void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		View currentFocusedView = getActivity().getCurrentFocus();
		if (currentFocusedView != null)
		inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public Preference.OnPreferenceClickListener openAppsEdit = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			openApps(preference.getKey());
			return true;
		}
	};

	public Preference.OnPreferenceClickListener openShareEdit = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			openShare(preference.getKey());
			return true;
		}
	};

	public Preference.OnPreferenceClickListener openLauncherActions = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			openMultiAction(preference, R.layout.prefs_launcher_actions);
			return true;
		}
	};

	public Preference.OnPreferenceClickListener openControlsActions = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			openMultiAction(preference, R.layout.prefs_controls_actions);
			return true;
		}
	};

	public Preference.OnPreferenceClickListener openNavbarActions = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			openMultiAction(preference, R.layout.prefs_navbar_actions);
			return true;
		}
	};

	public Preference.OnPreferenceClickListener openRecentsActions = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			openMultiAction(preference, R.layout.prefs_recents_actions);
			return true;
		}
	};

	void openApps(String key) {
		Bundle args = new Bundle();
		args.putString("key", key);
		args.putBoolean("multi", true);
		AppSelector appSelector = new AppSelector();
		appSelector.setTargetFragment(this, 0);
		openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
	}

	void openShare(String key) {
		Bundle args = new Bundle();
		args.putString("key", key);
		args.putBoolean("multi", true);
		args.putBoolean("share", true);
		AppSelector appSelector = new AppSelector();
		appSelector.setTargetFragment(this, 0);
		openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
	}

	void openMultiAction(Preference pref, int resId) {
		Bundle args = new Bundle();
		args.putString("key", pref.getKey());
		openSubFragment(new MultiAction(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, pref.getTitleRes(), resId);
	}

	public void openStandaloneApp(Preference pref, Fragment targetFrag, int resultId) {
		Bundle args = new Bundle();
		args.putString("key", pref.getKey());
		args.putBoolean("standalone", true);
		AppSelector appSelector = new AppSelector();
		appSelector.setTargetFragment(targetFrag, resultId);
		openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
	}

	public void openPrivacyAppEdit(Fragment targetFrag, int resultId) {
		Bundle args = new Bundle();
		args.putBoolean("privacy", true);
		AppSelector appSelector = new AppSelector();
		appSelector.setTargetFragment(targetFrag, resultId);
		openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
	}

	public void openLaunchableList(Preference pref, Fragment targetFrag, int resultId) {
		Bundle args = new Bundle();
		args.putString("key", pref.getKey());
		args.putBoolean("custom_titles", true);
		AppSelector appSelector = new AppSelector();
		appSelector.setTargetFragment(targetFrag, resultId);
		openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.launcher_renameapps_list_title, R.layout.prefs_app_selector);
	}

	public void finish() {
		if (isAnimating) return;
		if (getActivity() == null) return;
		hideKeyboard();
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager == null || !isResumed()) {
			getActivity().getFragmentManager().popBackStack();
		} else {
			fragmentManager.popBackStackImmediate();
		}
	}
}