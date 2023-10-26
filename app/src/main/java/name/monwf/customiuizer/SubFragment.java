package name.monwf.customiuizer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import name.monwf.customiuizer.prefs.PreferenceCategoryEx;
import name.monwf.customiuizer.prefs.PreferenceState;
import name.monwf.customiuizer.prefs.SpinnerEx;
import name.monwf.customiuizer.prefs.SpinnerExFake;
import name.monwf.customiuizer.subs.AppSelector;
import name.monwf.customiuizer.subs.ColorSelector;
import name.monwf.customiuizer.subs.MultiAction;
import name.monwf.customiuizer.subs.SortableList;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.ColorCircle;
import name.monwf.customiuizer.utils.Helpers;

public class SubFragment extends PreferenceFragmentBase {
    private int contentResId = 0;
    public String settingTitle = "";
    protected String sub;
    protected Bundle catInfo = null;
    protected boolean isStandalone = false;
    private float order = 100.0f;
    private String highlightKey = null;
    public boolean padded = true;
    Helpers.SettingsType settingsType = Helpers.SettingsType.Preference;
    Helpers.ActionBarType abType = Helpers.ActionBarType.Edit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        settingsType = Helpers.SettingsType.values()[getArguments().getInt("settingsType")];
        abType = Helpers.ActionBarType.values()[getArguments().getInt("abType")];
        contentResId = getArguments().getInt("contentResId");
        settingTitle = getArguments().getString("titleResId");
        order = getArguments().getFloat("order") + 10.0f;
        catInfo = getArguments().getBundle("catInfo");
        sub = getArguments().getString("sub");
        isStandalone = getArguments().getBoolean("isStandalone");
        highlightKey = getArguments().getString("mod");
        if (abType == Helpers.ActionBarType.Edit) {
            isCustomActionBar = true;
        }
        toolbarMenu = toolbarMenu || isCustomActionBar;

        if (contentResId == 0) {
            getActivity().finish();
            return;
        }

        if (settingsType == Helpers.SettingsType.Preference) {
            super.onCreate(savedInstanceState, contentResId);
        } else {
            super.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadSharedPrefs();
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (isStandalone && catInfo != null && catInfo.getBoolean("isDynamic")) {
                actionBar.setTitle(settingTitle + " ⟲");
            }
            else if (!isStandalone && sub != null) {
                PreferenceScreen screen = getPreferenceScreen();
                PreferenceCategoryEx category = (PreferenceCategoryEx)screen.getPreference(0);
                if (category.isDynamic())
                    actionBar.setTitle(category.getTitle() + " ⟲");
                else
                    actionBar.setTitle(category.getTitle());
            }
            else {
                actionBar.setTitle(settingTitle);
            }
        }
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        if (settingsType == Helpers.SettingsType.Preference) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(contentResId, rootKey);
            PreferenceState highlightPref;
            if (highlightKey != null && (highlightPref = findPreference(highlightKey)) != null) {
                highlightPref.applyHighlight();
            }
            else {
                highlightKey = null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        LayoutInflater crtInflator = inflater.cloneInContext(requireContext());
        if (settingsType == Helpers.SettingsType.Preference) {
            return super.onCreateView(crtInflator, container, savedInstanceState);
        }
        View view = crtInflator.inflate(padded ? R.layout.prefs_common_padded : R.layout.prefs_common, container, false);
        crtInflator.inflate(contentResId, (FrameLayout)view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setTranslationZ(order);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (highlightKey != null) {
            RecyclerView mList = getListView();
            int position = ((PreferenceGroup.PreferencePositionCallback) mList.getAdapter())
                .getPreferenceAdapterPosition(highlightKey);
            highlightKey = null;
            if (position < 9) return;
            RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(mList.getContext()) {
                @Override protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(position);
            getView().postDelayed(() -> {
                mList.getLayoutManager().startSmoothScroll(smoothScroller);
            }, 380);
        }
    }

    public void saveSharedPrefs() {
        if (getView() == null) Log.e("miuizer", "View not yet ready!");
        ArrayList<View> nViews = Helpers.getChildViewsRecursive(getView().findViewById(R.id.container), false);
        for (View nView : nViews)
            if (nView != null) try {
                if (nView.getTag() != null)
                    if (nView instanceof TextView)
                        AppHelper.appPrefs.edit().putString((String)nView.getTag(), ((TextView)nView).getText().toString()).apply();
                    else if (nView instanceof SpinnerExFake) {
                        AppHelper.appPrefs.edit().putString((String)nView.getTag(), ((SpinnerExFake)nView).getValue()).apply();
                        ((SpinnerExFake)nView).applyOthers();
                    } else if (nView instanceof SpinnerEx)
                        AppHelper.appPrefs.edit().putInt((String)nView.getTag(), ((SpinnerEx)nView).getSelectedArrayValue()).apply();
                    else if (nView instanceof ColorCircle)
                        AppHelper.appPrefs.edit().putInt((String)nView.getTag(), ((ColorCircle)nView).getColor()).apply();
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
                        ((TextView)nView).setText(AppHelper.getStringOfAppPrefs((String)nView.getTag(), ""));
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

    public Preference.OnPreferenceClickListener openActivitiesList = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (!Helpers.checkPermAndRequest((AppCompatActivity) getActivity(), Helpers.ACCESS_SECURITY_CENTER, Helpers.REQUEST_PERMISSIONS_SECURITY_CENTER)) return false;
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
        openSubFragment(new MultiAction(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, pref.getTitle().toString(), R.layout.prefs_multiaction);
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
        openSubFragment(new ColorSelector(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, pref.getTitle().toString(), R.layout.fragment_selectcolor);
    }

    public void openActivitiesItemList(Preference pref) {
        Bundle args = new Bundle();
        args.putBoolean("activities", true);
        args.putString("key", pref.getKey());
        args.putString("titleResId", pref.getTitle().toString());
        openSubFragment(new SortableList(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, pref.getTitle().toString(), R.layout.prefs_sortable_list);
    }

    public void selectSub() {
        if (isStandalone) return;
        PreferenceScreen screen = getPreferenceScreen();
        int cnt = screen.getPreferenceCount();
        for (int i = cnt - 1; i >= 0; i--) {
            Preference pref = screen.getPreference(i);
            if (!pref.getKey().equals(sub))
                screen.removePreference(pref);
            else {
                PreferenceCategoryEx category = (PreferenceCategoryEx)pref;
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    if (category.isDynamic())
                        actionBar.setTitle(pref.getTitle() + " ⟲");
                    else
                        actionBar.setTitle(pref.getTitle());
                }
                category.hide();
            }
        }
    }

    public void finish() {
        AppCompatActivity act = (AppCompatActivity) getActivity();
        Helpers.hideKeyboard(act, getView());
        FragmentManager fragmentManager = getParentFragmentManager();
        if (fragmentManager == null || !isResumed()) {
            if (act != null) act.getSupportFragmentManager().popBackStack();
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