package name.mikanoshi.customiuizer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;
import android.widget.Toast;

import name.mikanoshi.customiuizer.utils.Helpers;

public class MainActivity extends ActivityEx {

	MainFragment mainFrag = null;
	SharedPreferences.OnSharedPreferenceChangeListener prefsChanged;
	FileObserver mFileObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			Helpers.fixPermissionsAsync(this);
			Helpers.prefs = Helpers.getProtectedContext(this).getSharedPreferences(Helpers.prefsName, Context.MODE_PRIVATE);
		} catch (Throwable t) {
			Log.e("prefs", "Failed to use protected storage!");
		}

		super.onCreate(savedInstanceState);

		prefsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
				Log.i("prefs", "Changed: " + key);
				requestBackup();
				Object val = sharedPrefs.getAll().get(key);
				String path = "/";
				if (val instanceof String)
					path = "/string/";
				else if (val instanceof Integer)
					path = "/integer/";
				getContentResolver().notifyChange(Uri.parse("content://" + SharedPrefsProvider.AUTHORITY + path + key), null);
			}
		};
		Helpers.prefs.registerOnSharedPreferenceChangeListener(prefsChanged);

		try {
			mFileObserver = new FileObserver(Helpers.getProtectedContext(this).getDataDir() + "/shared_prefs", FileObserver.CLOSE_WRITE) {
				@Override
				public void onEvent(int event, String path) {
					Helpers.fixPermissionsAsync(MainActivity.this);
				}
			};
			mFileObserver.startWatching();
		} catch (Throwable t) {
			Log.e("prefs", "Failed to start FileObserver!");
		}

		if (!launch) return;
		mainFrag = new MainFragment();
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFrag).commit();
	}

	protected void onDestroy() {
		Helpers.prefs.unregisterOnSharedPreferenceChangeListener(prefsChanged);
		super.onDestroy();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case Helpers.REQUEST_PERMISSIONS_BACKUP:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.backupSettings(this);
				else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					Toast.makeText(this, "Do you want to write backup or not?", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, "You'll have to manually enable permission for this option now. Good job!", Toast.LENGTH_LONG).show();
				break;
			case Helpers.REQUEST_PERMISSIONS_RESTORE:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.restoreSettings(this);
				else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					Toast.makeText(this, "Do you want to restore backup or not?", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, "You'll have to manually enable permission for this option now. Good job!", Toast.LENGTH_LONG).show();
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

}