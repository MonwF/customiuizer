package name.monwf.customiuizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.service.RemotePreferences;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;
import name.monwf.customiuizer.mods.GlobalActions;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class MainActivity extends AppCompatActivity {

    MainFragment mainFrag = null;
    SharedPreferences.OnSharedPreferenceChangeListener prefsChanged;

    @Override
    protected void attachBaseContext(Context base) {
        try {
            super.attachBaseContext(AppHelper.getLocaleContext(base));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (AppHelper.remotePrefs == null) {
            XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
                public void onServiceBind(XposedService service) {
                    AppHelper.moduleActive = true;
                    AppHelper.remotePrefs = (RemotePreferences) service.getRemotePreferences(AppHelper.prefsName + "_remote");
                }
                public void onServiceDied(XposedService service) {
                    AppHelper.moduleActive = false;
                    AppHelper.remotePrefs = null;
                }
            });
        }

//		Helpers.updateNewModsMarking(this);

        Toolbar myToolbar = findViewById(R.id.mainActionBar);
        setSupportActionBar(myToolbar);
        if (savedInstanceState != null) {
            mainFrag = (MainFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mainFrag");
        }
        else if (mainFrag == null) {
            mainFrag = new MainFragment();
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container, mainFrag).commit();
        }

        HashSet<String> ignoreKeys = new HashSet<>();
        ignoreKeys.add("pref_key_miuizer_locale");
        ignoreKeys.add("pref_key_miuizer_launchericon");
        ignoreKeys.add("pref_key_miuizer_synced_from_lsposed");
        prefsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (AppHelper.remotePrefs == null) return;
//                AppHelper.log("app changed key: " + key);
                if (key == null) {
                    RemotePreferences.Editor prefEdit = AppHelper.remotePrefs.edit();
                    for (String remoteKey:AppHelper.remotePrefs.getAll().keySet()) {
                        prefEdit.remove(remoteKey);
                    }
                    prefEdit.apply();
                    return ;
                }
                if (ignoreKeys.contains(key)) return;
                Object val = sharedPreferences.getAll().get(key);
                RemotePreferences.Editor prefEdit = AppHelper.remotePrefs.edit();
                if (val == null) {
                    prefEdit.remove(key);
                }
                else if (val instanceof Boolean) {
                    prefEdit.putBoolean(key, (Boolean) val);
                }
                else if (val instanceof Float) {
                    prefEdit.putFloat(key, (Float)val);
                }
                else if (val instanceof Integer) {
                    prefEdit.putInt(key, (Integer) val);
                }
                else if (val instanceof Long) {
                    prefEdit.putLong(key, (Long) val);
                }
                else if (val instanceof String) {
                    prefEdit.putString(key, (String) val);
                }
                else if (val instanceof Set<?>) {
                    prefEdit.putStringSet(key, (Set<String>) val);
                }
                prefEdit.apply();
            }
        };

        AppHelper.appPrefs.registerOnSharedPreferenceChangeListener(prefsChanged);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        getSupportFragmentManager().putFragment(savedInstanceState, "mainFrag", mainFrag);
        super.onSaveInstanceState(savedInstanceState);
    }

    @SuppressLint("ApplySharedPref")
    protected void onDestroy() {
        try {
            if (prefsChanged != null) AppHelper.appPrefs.unregisterOnSharedPreferenceChangeListener(prefsChanged);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment == null) {
                finish();
                return true;
            }
            if (fragment instanceof MainFragment) {
                finish();
            }
            else {
                ((SubFragment)fragment).finish();
            }
            return true;
        }
        else if (item.getItemId() == R.id.resetsettings) {
            showResetSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showResetSettingsDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.reset_settings);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                AppHelper.appPrefs.edit().clear().commit();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.reset_settings_done);
                builder.setCancelable(true);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton){
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                });
                builder.show();
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length == 0) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        switch (requestCode) {
            case Helpers.REQUEST_PERMISSIONS_WIFI:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
                        Toast.makeText(this, R.string.permission_scan, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
                }
                break;
            case Helpers.REQUEST_PERMISSIONS_BLUETOOTH:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT))
                        Toast.makeText(this, R.string.permission_scan, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
                }
                break;
            case Helpers.REQUEST_PERMISSIONS_REPORT:
                Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}