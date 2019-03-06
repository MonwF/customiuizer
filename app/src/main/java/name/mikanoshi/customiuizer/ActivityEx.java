package name.mikanoshi.customiuizer;

import android.app.Activity;
import android.app.Fragment;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import name.mikanoshi.customiuizer.utils.Helpers;

public class ActivityEx extends Activity {
	public int mThemeBackground = 1;
	public boolean launch = true;
	SharedPreferences.OnSharedPreferenceChangeListener prefsChanged;

	protected void onCreate(Bundle savedInstanceState) {
		setTheme(getResources().getIdentifier("Theme.Light.Settings", "style", "miui"));
		getTheme().applyStyle(R.style.MIUIPrefs, true);
		super.onCreate(savedInstanceState);
		Helpers.prefs = getSharedPreferences(Helpers.prefsName, Context.MODE_PRIVATE);
		prefsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				Log.w("prefs", "Changed: " + key);
				requestBackup();
			}
		};
		Helpers.prefs.registerOnSharedPreferenceChangeListener(prefsChanged);

		mThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_miuizer_material_background", "1"));
		//if (mThemeBackground == 2) getTheme().applyStyle(R.style.MaterialThemeDark, true);

		setContentView(R.layout.activity_main);
	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_container);
		if (fragment != null)
		if (((PreferenceFragmentBase)fragment).isAnimating) return;
		super.onBackPressed();
	}

	protected void onDestroy() {
		Helpers.prefs.unregisterOnSharedPreferenceChangeListener(prefsChanged);
		super.onDestroy();
	}

	public void updateTheme(int newBkg) {
		int newThemeBackground;
		if (newBkg == 0)
			newThemeBackground = Integer.parseInt(Helpers.prefs.getString("pref_key_miuizer_material_background", "1"));
		else
			newThemeBackground = newBkg;
		if (newThemeBackground != mThemeBackground) recreate();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (launch) updateTheme(0);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void requestBackup() {
		BackupManager bm = new BackupManager(getApplicationContext());
		bm.dataChanged();
	}
}