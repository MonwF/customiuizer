package name.mikanoshi.customiuizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Set;

import name.mikanoshi.customiuizer.utils.Helpers;

public class MainActivity extends AppCompatActivity {

	MainFragment mainFrag = null;
	SharedPreferences.OnSharedPreferenceChangeListener prefsChanged;
	FileObserver fileObserver;
	boolean migrateOnExit = false;

	@Override
	protected void attachBaseContext(Context base) {
		try {
			super.attachBaseContext(Helpers.getLocaleContext(base));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
				Log.i("prefs", "Changed: " + key);
				requestBackup();
				Object val = sharedPrefs.getAll().get(key);
				String path = "";
				if (val instanceof String)
					path = "string/";
				else if (val instanceof Set<?>)
					path = "stringset/";
				else if (val instanceof Integer)
					path = "integer/";
				else if (val instanceof Boolean)
					path = "boolean/";
				getContentResolver().notifyChange(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/" + path + key), null);
				if (!path.equals(""))
				getContentResolver().notifyChange(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + "/pref/" + path + key), null);
			}
		};
		Helpers.prefs.registerOnSharedPreferenceChangeListener(prefsChanged);
		Helpers.fixPermissionsAsync(getApplicationContext());

		try {
			fileObserver = new FileObserver(Helpers.getSharedPrefsPath(), FileObserver.CLOSE_WRITE) {
				@Override
				public void onEvent(int event, String path) {
					Helpers.fixPermissionsAsync(getApplicationContext());
				}
			};
			fileObserver.startWatching();
		} catch (Throwable t) {
			Log.e("prefs", "Failed to start FileObserver!");
		}

//		Helpers.updateNewModsMarking(this);


		Toolbar myToolbar = findViewById(R.id.mainActionBar);
		setSupportActionBar(myToolbar);

		if (savedInstanceState != null)
		mainFrag = (MainFragment)getFragmentManager().getFragment(savedInstanceState, "mainFrag");
		if (mainFrag == null) {
			mainFrag = new MainFragment();
			getFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFrag).commit();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		getFragmentManager().putFragment(savedInstanceState, "mainFrag", mainFrag);
		super.onSaveInstanceState(savedInstanceState);
	}

	@SuppressLint("ApplySharedPref")
	protected void onDestroy() {
		try {
			if (prefsChanged != null) Helpers.prefs.unregisterOnSharedPreferenceChangeListener(prefsChanged);
			if (fileObserver != null) fileObserver.stopWatching();
			if (migrateOnExit) {
				boolean migrated = Helpers.migratePrefs();
				Helpers.prefs = Helpers.getSharedPrefs(this, true, true);
				Helpers.prefs.edit().putBoolean("miuizer_prefs_migrated", true).putInt("miuizer_prefs_migration_result", migrated ? 1 : 2).commit();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_container);
		if (fragment == null) {
			super.onBackPressed();
			return;
		}
		if (Helpers.shimmerAnim != null) Helpers.shimmerAnim.cancel();
		if (fragment instanceof SubFragment)
			((SubFragment)fragment).finish();
		else
			super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_container);
			if (fragment == null) {
				finish();
				return true;
			}
			if (Helpers.shimmerAnim != null) Helpers.shimmerAnim.cancel();
			if (fragment instanceof SubFragment)
				((SubFragment)fragment).finish();
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

	public void requestBackup() {
		new BackupManager(getApplicationContext()).dataChanged();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (grantResults.length == 0) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			return;
		}

		switch (requestCode) {
			case Helpers.REQUEST_PERMISSIONS_BACKUP:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.backupSettings(this);
				else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					Toast.makeText(this, R.string.permission_save, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
				break;
			case Helpers.REQUEST_PERMISSIONS_RESTORE:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.restoreSettings(this);
				else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					Toast.makeText(this, R.string.permission_restore, Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
				break;
			case Helpers.REQUEST_PERMISSIONS_WIFI:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Fragment frag = getFragmentManager().findFragmentById(R.id.fragment_container);
					if (frag instanceof name.mikanoshi.customiuizer.subs.System_NoScreenLock)
					((name.mikanoshi.customiuizer.subs.System_NoScreenLock)frag).openWifiNetworks();
				} else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
					Toast.makeText(this, R.string.permission_scan, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show();
				break;
			case Helpers.REQUEST_PERMISSIONS_REPORT:
					Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
}