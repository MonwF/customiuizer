package name.mikanoshi.customiuizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.backup.BackupManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.Locale;

import name.mikanoshi.customiuizer.utils.Helpers;

@SuppressLint("Registered")
public class ActivityEx extends Activity {
	public boolean launch = true;

	protected void onCreate(Bundle savedInstanceState) {
		setTheme(getResources().getIdentifier("Theme.Light.Settings", "style", "miui"));
		getTheme().applyStyle(R.style.MIUIPrefs, true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public void onBackPressed() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_container);
		if (fragment != null)
		if (((PreferenceFragmentBase)fragment).isAnimating) return;
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
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