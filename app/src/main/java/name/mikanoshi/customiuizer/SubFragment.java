package name.mikanoshi.customiuizer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import java.util.ArrayList;

import miui.widget.ClearableEditText;
import name.mikanoshi.customiuizer.prefs.PreferenceCategoryEx;
import name.mikanoshi.customiuizer.prefs.SpinnerEx;
import name.mikanoshi.customiuizer.prefs.SpinnerExFake;
import name.mikanoshi.customiuizer.subs.AppSelector;
import name.mikanoshi.customiuizer.subs.ColorSelector;
import name.mikanoshi.customiuizer.subs.MultiAction;
import name.mikanoshi.customiuizer.subs.SortableList;
import name.mikanoshi.customiuizer.utils.ColorCircle;
import name.mikanoshi.customiuizer.utils.Helpers;

public class SubFragment extends PreferenceFragmentBase {

    private int baseResId = 0;
    private int resId = 0;
    public int titleId = 0;
    private float order = 100.0f;
    public boolean padded = true;
    Helpers.SettingsType settingsType = Helpers.SettingsType.Preference;
    Helpers.ActionBarType abType = Helpers.ActionBarType.Edit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        settingsType = Helpers.SettingsType.values()[getArguments().getInt("settingsType")];
        abType = Helpers.ActionBarType.values()[getArguments().getInt("abType")];
        baseResId = getArguments().getInt("baseResId");
        resId = getArguments().getInt("contentResId");
        titleId = getArguments().getInt("titleResId");
        order = getArguments().getFloat("order") + 10.0f;

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
        if (abType == Helpers.ActionBarType.Edit) {
            isCustomActionBar = true;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        supressMenu = supressMenu || abType == Helpers.ActionBarType.Edit;
        super.onActivityCreated(savedInstanceState);
        loadSharedPrefs();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(titleId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutInflater crtInflator = LayoutInflater.from(getActivity());
        if (settingsType == Helpers.SettingsType.Preference) {
            if (baseResId != 0) {
                return LayoutInflater.from(getActivity()).inflate(baseResId, container, false);
            }
            else {
                return super.onCreateView(crtInflator, container, savedInstanceState);
            }
        }
        View view = crtInflator.inflate(padded ? R.layout.prefs_common_padded : R.layout.prefs_common, container, false);
        crtInflator.inflate(resId, (FrameLayout)view);
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
                    else if (nView instanceof ColorCircle)
                        Helpers.prefs.edit().putInt((String)nView.getTag(), ((ColorCircle)nView).getColor()).apply();
            } catch (Throwable e) {
                Log.e("miuizer", "Cannot save sub preference!");
            }
    }

    public void loadSharedPrefs() {
        if (getView() == null) Log.e("miuizer", "View not yet ready!");
        ArrayList<View> nViews = Helpers.getChildViewsRecursive(getView().findViewById(R.id.container), false);
        for (View nView: nViews)
            if (nView != null) try {
                if (nView.getTag() != null)
                    if (nView instanceof TextView) {
                        ((TextView)nView).setText(Helpers.prefs.getString((String)nView.getTag(), ""));
                        if (nView instanceof ClearableEditText) nView.setBackgroundResource(getResources().getIdentifier(Helpers.isNightMode(getValidContext()) ? "edit_text_bg_dark" : "edit_text_bg_light", "drawable", "miui"));
                    }
            } catch (Throwable e) {
                Log.e("miuizer", "Cannot load sub preference!");
            }
    }

    public Preference.OnPreferenceClickListener openAppsEdit = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openApps(preference.getKey());
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openAppsBWEdit = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openAppsBW(preference.getKey());
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

    public Preference.OnPreferenceClickListener openOpenWithEdit = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openOpenWith(preference.getKey());
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openLauncherActions = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openMultiAction(preference, MultiAction.Actions.LAUNCHER);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openControlsActions = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openMultiAction(preference, MultiAction.Actions.CONTROLS);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openNavbarActions = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openMultiAction(preference, MultiAction.Actions.NAVBAR);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openRecentsActions = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openMultiAction(preference, MultiAction.Actions.RECENTS);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openStatusbarActions = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openMultiAction(preference, MultiAction.Actions.STATUSBAR);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openLockScreenActions = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openMultiAction(preference, MultiAction.Actions.LOCKSCREEN);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openLaunchActions = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openMultiAction(preference, MultiAction.Actions.LAUNCH);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openSortableList = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openSortableItemList(preference);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openActivitiesList = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openActivitiesItemList(preference);
            return true;
        }
    };

    public Preference.OnPreferenceClickListener openColorSelector = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            openColorSelector(preference);
            return true;
        }
    };

    void openApps(String key) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putBoolean("multi", true);
        AppSelector appSelector = new AppSelector();
        appSelector.setTargetFragment(this, 0);
        openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_apps, R.layout.prefs_app_selector);
    }

    void openAppsBW(String key) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putBoolean("multi", true);
        args.putBoolean("bw", true);
        AppSelector appSelector = new AppSelector();
        appSelector.setTargetFragment(this, 0);
        openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_apps, R.layout.prefs_app_selector);
    }

    void openShare(String key) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putBoolean("multi", true);
        args.putBoolean("share", true);
        AppSelector appSelector = new AppSelector();
        appSelector.setTargetFragment(this, 0);
        openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_apps, R.layout.prefs_app_selector);
    }

    void openOpenWith(String key) {
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putBoolean("multi", true);
        args.putBoolean("openwith", true);
        AppSelector appSelector = new AppSelector();
        appSelector.setTargetFragment(this, 0);
        openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_apps, R.layout.prefs_app_selector);
    }

    void openMultiAction(Preference pref, MultiAction.Actions actions) {
        Bundle args = new Bundle();
        args.putString("key", pref.getKey());
        args.putInt("actions", actions.ordinal());
        openSubFragment(new MultiAction(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, pref.getTitleRes(), R.layout.prefs_multiaction);
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
        openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_apps, R.layout.prefs_app_selector);
    }

    public void openLockedAppEdit(Fragment targetFrag, int resultId) {
        Bundle args = new Bundle();
        args.putBoolean("applock", true);
        AppSelector appSelector = new AppSelector();
        appSelector.setTargetFragment(targetFrag, resultId);
        openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_apps, R.layout.prefs_app_selector);
    }

    public void openLaunchableList(Preference pref, Fragment targetFrag, int resultId) {
        Bundle args = new Bundle();
        args.putString("key", pref.getKey());
        args.putBoolean("custom_titles", true);
        AppSelector appSelector = new AppSelector();
        appSelector.setTargetFragment(targetFrag, resultId);
        openSubFragment(appSelector, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.launcher_renameapps_list_title, R.layout.prefs_app_selector);
    }

    public void openColorSelector(Preference pref) {
        Bundle args = new Bundle();
        args.putString("key", pref.getKey());
        openSubFragment(new ColorSelector(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, pref.getTitleRes(), R.layout.fragment_selectcolor);
    }

    public void openSortableItemList(Preference pref) {
        Bundle args = new Bundle();
        args.putString("key", pref.getKey());
        args.putInt("titleResId", pref.getTitleRes());
        openSubFragment(new SortableList(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, pref.getTitleRes(), R.layout.prefs_sortable_list);
    }

    public void openActivitiesItemList(Preference pref) {
        Bundle args = new Bundle();
        args.putBoolean("activities", true);
        args.putString("key", pref.getKey());
        args.putInt("titleResId", pref.getTitleRes());
        openSubFragment(new SortableList(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, pref.getTitleRes(), R.layout.prefs_sortable_list);
    }

    public void selectSub(String cat, String sub) {
        PreferenceScreen screen = (PreferenceScreen)findPreference(cat);
        int cnt = screen.getPreferenceCount();
        for (int i = cnt - 1; i >= 0; i--) {
            Preference pref = screen.getPreference(i);
            if (!pref.getKey().equals(sub))
                screen.removePreference(pref);
            else {
                PreferenceCategoryEx category = (PreferenceCategoryEx)pref;
                if (category.isDynamic())
                    getActionBar().setTitle(pref.getTitle() + " ⟲");
                else
                    getActionBar().setTitle(pref.getTitleRes());
                category.hide();
            }
        }
    }

    public void finish() {
        //View view = getView();
        //if (isAnimating && view != null) ((ViewGroup)view.getParent()).removeView(view);
        if (isAnimating) return;
        if (Helpers.shimmerAnim != null) Helpers.shimmerAnim.cancel();
        Helpers.hideKeyboard(getActivity(), getView());
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager == null || !isResumed()) {
            Activity act = getActivity();
            if (act != null) act.getFragmentManager().popBackStack();
        } else {
            fragmentManager.popBackStackImmediate();
        }
    }

    @Override
    public void confirmEdit() {
        saveSharedPrefs();
        finish();
    }
}