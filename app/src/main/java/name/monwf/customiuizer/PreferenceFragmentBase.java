package name.monwf.customiuizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import name.monwf.customiuizer.mods.GlobalActions;
import name.monwf.customiuizer.subs.WebPage;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class PreferenceFragmentBase extends PreferenceFragmentCompat {

    private Context actContext = null;
    protected boolean toolbarMenu = false;
    protected int animDur = 350;
    protected String activeMenus = "";

    public static final int PICK_BACKFILE = 11;
    public static final int SAVE_BACKFILE = 12;
    protected boolean isCustomActionBar = false;
    protected int headLayoutId = 0;
    protected int tailLayoutId = 0;
    protected String pageUrl;
    protected HashMap<Integer, String> mapKeys = new HashMap<Integer, String>() {{
        put(R.id.search_btn, "search");
        put(R.id.restartlauncher, "launcher");
        put(R.id.restartsystemui, "systemui");
        put(R.id.restartsecuritycenter, "securitycenter");
        put(R.id.edit_confirm, "edit");
        put(R.id.softreboot, "reboot");
        put(R.id.backuprestore, "settings");
        put(R.id.resetsettings, "reset");
        put(R.id.about, "about");
        put(R.id.openinweb, "openinweb");
    }};

    protected ActionBar getActionBar() {
        AppCompatActivity act = (AppCompatActivity) getActivity();
        return act.getSupportActionBar();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (toolbarMenu) {
            inflater.inflate(R.menu.menu_mods, menu);
        }
        if (isCustomActionBar) {
            MenuItem item;
            for (int i = 0; i < menu.size(); i++) {
                item = menu.getItem(i);
                item.setVisible(item.getItemId() == R.id.edit_confirm);
            }
        }
        else {
            MenuItem item;
            for (int i = 0; i < menu.size(); i++) {
                item = menu.getItem(i);
                int menuId = item.getItemId();
                String menuKey = mapKeys.get(menuId);
                if (activeMenus.equals("all") && (menuId == R.id.edit_confirm || menuId == R.id.openinweb)) {
                    item.setVisible(false);
                }
                else if (activeMenus.equals("all")) {
                    item.setVisible(true);
                }
                else if (menuKey != null && activeMenus.contains(menuKey)) {
                    item.setVisible(true);
                }
                else {
                    item.setVisible(false);
                }
            }
        }
    }

    public void confirmEdit() {}

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        getPreferenceManager().setSharedPreferencesName(AppHelper.prefsName);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent actionIntent;
        switch (item.getItemId()) {
            case R.id.edit_confirm:
                confirmEdit();
                return true;
            case R.id.restartlauncher:
                if (!AppHelper.moduleActive) {
                    showXposedDialog((AppCompatActivity) getActivity());
                    return true;
                }
                actionIntent = new Intent(GlobalActions.ACTION_PREFIX + "RestartLauncher");
                actionIntent.setPackage("com.android.systemui");
                getValidContext().sendBroadcast(actionIntent);
                return true;
            case R.id.restartsystemui:
                if (!AppHelper.moduleActive) {
                    showXposedDialog((AppCompatActivity) getActivity());
                    return true;
                }
                actionIntent = new Intent(GlobalActions.ACTION_PREFIX + "RestartSystemUI");
                actionIntent.setPackage("com.android.systemui");
                getValidContext().sendBroadcast(actionIntent);
                return true;
            case R.id.restartsecuritycenter:
                actionIntent = new Intent(GlobalActions.ACTION_PREFIX + "RestartSecurityCenter");
                actionIntent.setPackage("com.android.systemui");
                getValidContext().sendBroadcast(actionIntent);
                return true;
            case R.id.backuprestore:
                showBackupRestoreDialog();
                return true;
            case R.id.openinweb:
                Helpers.openURL(getValidContext(), pageUrl);
                return true;
            case R.id.softreboot:
                if (!AppHelper.moduleActive) {
                    showXposedDialog((AppCompatActivity) getActivity());
                    return true;
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(getValidContext());
                alert.setTitle(R.string.soft_reboot);
                alert.setMessage(R.string.soft_reboot_ask);
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "FastReboot");
                        intent.setPackage("com.android.systemui");
                        getValidContext().sendBroadcast(intent);
                    }
                });
                alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                alert.show();
                return true;
            case R.id.about:
                openSubFragment(new AboutFragment(), null, Helpers.SettingsType.Preference, Helpers.ActionBarType.HomeUp, R.string.app_about, R.xml.prefs_about);
                return true;
        }
        return false;
    }

    public void showXposedDialog(AppCompatActivity act) {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.module_not_active);
        builder.setCancelable(true);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton){}
        });
        AlertDialog dlg = builder.create();
        dlg.show();
    }

    public void showBackupRestoreDialog() {
        final AppCompatActivity act = (AppCompatActivity) getActivity();

        AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(R.string.backup_restore);
        alert.setMessage(R.string.backup_restore_choose);
        alert.setPositiveButton(R.string.do_restore, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                restoreSettings(act);
            }
        });
        alert.setNegativeButton(R.string.do_backup, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                backupSettings(act);
            }
        });
        alert.show();
    }

    private void initFragment() {
        setHasOptionsMenu(toolbarMenu);
        ActionBar actionBar = getActionBar();

        boolean showBack;
        if (this instanceof MainFragment) {
            AppCompatActivity act = (AppCompatActivity) getActivity();
            showBack = act.getIntent().getBooleanExtra("from.settings", false);
        } else showBack = true;

        actionBar.setDisplayHomeAsUpEnabled(showBack);
    }

    @SuppressLint("WorldReadableFiles")
    public void onCreate(Bundle savedInstanceState, int pref_defaults) {
        super.onCreate(savedInstanceState);
        try {
            PreferenceManager.setDefaultValues(getValidContext(), pref_defaults, false);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    protected void fixStubLayout(View view, int postion) {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (headLayoutId > 0) {
            ViewStub vs = view.findViewById(R.id.head_stub);
            vs.setLayoutResource(headLayoutId);
            View renderView = vs.inflate();
            fixStubLayout(renderView, 1);
        }
        if (tailLayoutId > 0) {
            ViewStub vs = view.findViewById(R.id.tail_stub);
            vs.setLayoutResource(tailLayoutId);
            View renderView = vs.inflate();
            fixStubLayout(renderView, 2);
        }
        initFragment();
    }

    public void openWebPage(String url) {
        Bundle args = new Bundle();
        args.putString("pageUrl", url);
        openSubFragment(new WebPage(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, "", R.layout.fragment_webpage);
    }

    public void openSubFragment(Fragment fragment, Bundle args, Helpers.SettingsType settingsType, Helpers.ActionBarType abType, int titleResId, int contentResId) {
        openSubFragment(fragment, args, settingsType, abType, getResources().getString(titleResId), contentResId);
    }

    public void openSubFragment(Fragment fragment, Bundle args, Helpers.SettingsType settingsType, Helpers.ActionBarType abType, String title, int contentResId) {
        if (args == null) args = new Bundle();
        args.putInt("settingsType", settingsType.ordinal());
        args.putInt("abType", abType.ordinal());
        args.putString("titleResId", title);
        args.putInt("contentResId", contentResId);
        float order = 100.0f;
        try {
            if (getView() != null) order = getView().getTranslationZ();
        } catch (Throwable t) {}
        args.putFloat("order", order);
        if (fragment.getArguments() == null) {
            fragment.setArguments(args);
        } else {
            fragment.getArguments().clear();
            fragment.getArguments().putAll(args);
        }
        getParentFragmentManager().beginTransaction().setReorderingAllowed(true)
            .setCustomAnimations(R.animator.fragment_open_enter, R.animator.fragment_open_exit, R.animator.fragment_close_enter, R.animator.fragment_close_exit)
            .replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss();
        getParentFragmentManager().executePendingTransactions();
    }

    /*
    @Override
    public Animator onCreateAnimator(int transit, boolean enter, final int nextAnim) {
        if (nextAnim == 0) return null;
        Configuration config = getResources().getConfiguration();
        float density = getResources().getDisplayMetrics().density;
        final float scrWidth = config.screenWidthDp * density;

        final View top = getView();
        if (top == null) return null;
        final View content = getListView();
        //ValueAnimator.setFrameDelay(17);
        ValueAnimator valAnimator = new ValueAnimator();
        valAnimator.setDuration(animDur);
        valAnimator.setFloatValues(0.0f, 1.0f);
        valAnimator.setInterpolator(new DecelerateInterpolator(2.5f));

        if (nextAnim == R.animator.fragment_open_enter || nextAnim == R.animator.fragment_open_exit)
            valAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
//				Log.e("animation", "start on: " + PreferenceFragmentBase.this.getClass().getCanonicalName());
                    isAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
//				Log.e("animation", "end on: " + PreferenceFragmentBase.this.getClass().getCanonicalName());
                    isAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            }); else isAnimating = false;

        valAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (content == null) return;
                float val = (float)animation.getAnimatedValue();
                if (nextAnim == R.animator.fragment_open_enter) {
                    top.setX(scrWidth * (1.0f - val));
                    content.setAlpha(0.6f + val * 0.4f);
                } else if (nextAnim == R.animator.fragment_open_exit) {
                    top.setX(-scrWidth / 4.0f * val);
                    top.setAlpha(1.0f - val * 0.4f);
                } else if (nextAnim == R.animator.fragment_close_enter) {
                    top.setX(-scrWidth / 4.0f * (1.0f - val));
                    top.setAlpha(0.6f + val * 0.4f);
                } else if (nextAnim == R.animator.fragment_close_exit) {
                    top.setX(scrWidth * val);
                    content.setAlpha(1.0f - val * 0.4f);
                }
            }
        });

        return valAnimator;
    }
     */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.actContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.actContext = null;
    }

    public Context getValidContext() {
        if (actContext != null) return actContext;
        return getActivity() == null ? getContext() : getActivity().getApplicationContext();
    }

    public void backupSettings(AppCompatActivity act) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "customiuizer_backup_" + new SimpleDateFormat("MMddHHmmss").format(new java.util.Date()));
        startActivityForResult(intent, SAVE_BACKFILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == MainFragment.PICK_BACKFILE
            && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                doRestoreSettings(uri);
            }
        }
        else if (requestCode == MainFragment.SAVE_BACKFILE
            && resultCode == Activity.RESULT_OK) {
            ObjectOutputStream output = null;
            try {
                output = new ObjectOutputStream(getValidContext().getContentResolver().openOutputStream(resultData.getData()));
                output.writeObject(AppHelper.appPrefs.getAll());

                AlertDialog.Builder alert = new AlertDialog.Builder(getValidContext());
                alert.setTitle(R.string.do_backup);
                alert.setMessage(R.string.backup_ok);
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                alert.show();
            } catch (Throwable e) {
                e.printStackTrace();
                AlertDialog.Builder alert = new AlertDialog.Builder(getValidContext());
                alert.setTitle(R.string.warning);
                alert.setMessage(getString(R.string.storage_cannot_backup) + "\n" + e.getMessage());
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                alert.show();
            } finally {
                try {
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void restoreSettings(final AppCompatActivity act) {
        if (!Helpers.checkStorageReadable(act)) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        startActivityForResult(intent, PICK_BACKFILE);
    }

    public void doRestoreSettings(Uri uri) {
        ObjectInputStream input = null;
        final AppCompatActivity act = (AppCompatActivity) getActivity();
        try {
            input = new ObjectInputStream(act.getContentResolver().openInputStream(uri));
            Map<String, ?> entries = (Map<String, ?>)input.readObject();
            AppHelper.syncPrefsToAnother(entries, AppHelper.appPrefs, 1, null, false);
            AlertDialog.Builder alert = new AlertDialog.Builder(act);
            alert.setTitle(R.string.do_restore);
            alert.setMessage(R.string.restore_ok);
            alert.setCancelable(false);
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    act.finish();
                    act.startActivity(act.getIntent());
                }
            });
            alert.show();
        } catch (Throwable t) {
            t.printStackTrace();
            AlertDialog.Builder alert = new AlertDialog.Builder(act);
            alert.setTitle(R.string.warning);
            alert.setMessage(R.string.storage_cannot_restore);
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {}
            });
            alert.show();
        } finally {
            try {
                if (input != null) input.close();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}