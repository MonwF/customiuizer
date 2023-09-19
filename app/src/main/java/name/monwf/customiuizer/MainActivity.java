package name.monwf.customiuizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.service.RemotePreferences;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;
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

		XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
			public void onServiceBind(XposedService service) {
				AppHelper.moduleActive = true;
				HashSet<String> ignoreKeys = new HashSet<>();
				ignoreKeys.add("pref_key_miuizer_locale");
				ignoreKeys.add("pref_key_miuizer_settingsiconpos");
				ignoreKeys.add("pref_key_miuizer_synced_from_lsposed");
				RemotePreferences sp = (RemotePreferences) service.getRemotePreferences(AppHelper.prefsName + "_remote");
				prefsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
						if (ignoreKeys.contains(key)) return;
						Object val = sharedPreferences.getAll().get(key);
						RemotePreferences.Editor prefEdit = sp.edit();
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
							prefEdit.putInt(key, (Integer)val);
						}
						else if (val instanceof Long) {
							prefEdit.putLong(key, (Long)val);
						}
						else if (val instanceof String) {
							prefEdit.putString(key, ((String)val));
						}
						else if (val instanceof Set<?>) {
							prefEdit.putStringSet(key, ((Set<String>)val));
						}
						prefEdit.apply();
					}
				};

				// sync from lsposed prefs
				if (!AppHelper.appPrefs.getBoolean("pref_key_miuizer_synced_from_lsposed", false)) {
					AppHelper.syncPrefsToAnother(sp.getAll(), AppHelper.appPrefs, false, null);
					AppHelper.appPrefs.edit().putBoolean("pref_key_miuizer_synced_from_lsposed", true).apply();
				}
				AppHelper.appPrefs.registerOnSharedPreferenceChangeListener(prefsChanged);
			}
			public void onServiceDied(XposedService service) {
				AppHelper.moduleActive = false;
			}
		});

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
			if (fragment instanceof MainFragment)
				finish();
			else {
				((SubFragment)fragment).finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
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