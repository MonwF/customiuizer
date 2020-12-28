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
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.Set;

import com.github.jinatonic.confetti.ConfettiManager;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;

import name.mikanoshi.customiuizer.utils.FilthGenerator;
import name.mikanoshi.customiuizer.utils.FlowerGenerator;
import name.mikanoshi.customiuizer.utils.GravitySensor;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.SnowGenerator;

public class MainActivity extends Activity {

	MainFragment mainFrag = null;
	SharedPreferences.OnSharedPreferenceChangeListener prefsChanged;
	FileObserver fileObserver;
	GravitySensor angleListener;
	WeatherView weatherView;
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
		boolean mSDKFound = ((MainApplication)getApplication()).mStarted;
		if (mSDKFound) Helpers.setMiuiTheme(this, R.style.MIUIPrefs);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (!mSDKFound) {
			Toast.makeText(this, R.string.sdk_failed, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		Helpers.detectHoliday();
		weatherView = findViewById(R.id.weather_view);
		ImageView mHolidayHeader = findViewById(R.id.holiday_header);
		if (Helpers.currentHoliday == Helpers.Holidays.NEWYEAR) {
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			weatherView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			weatherView.setPrecipType(PrecipType.SNOW);
			weatherView.setSpeed(50);
			weatherView.setEmissionRate(rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270 ? 8 : 4);
			weatherView.setFadeOutPercent(0.75f);
			weatherView.setAngle(0);
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)weatherView.getLayoutParams();
			lp.height = getResources().getDisplayMetrics().heightPixels / (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270 ? 2 : 3);
			weatherView.setLayoutParams(lp);
			try {
				ConfettiManager manager = weatherView.getConfettiManager();
				Field confettoGenerator = ConfettiManager.class.getDeclaredField("confettoGenerator");
				confettoGenerator.setAccessible(true);
				confettoGenerator.set(manager, new SnowGenerator(this));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			weatherView.resetWeather();
			weatherView.setVisibility(View.VISIBLE);
			weatherView.getConfettiManager().setRotationalVelocity(0, 45);
			angleListener = new GravitySensor(this, weatherView);
			angleListener.setOrientation(rotation);
			angleListener.setSpeed(50);
			angleListener.start();

			mHolidayHeader.setImageResource(R.drawable.newyear_header);
			mHolidayHeader.setVisibility(View.VISIBLE);
		} else if (Helpers.currentHoliday == Helpers.Holidays.LUNARNEWYEAR) {
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			weatherView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			weatherView.setPrecipType(PrecipType.SNOW);
			weatherView.setSpeed(35);
			weatherView.setEmissionRate(rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270 ? 4 : 2);
			weatherView.setFadeOutPercent(0.75f);
			weatherView.setAngle(0);
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)weatherView.getLayoutParams();
			lp.height = getResources().getDisplayMetrics().heightPixels / (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270 ? 3 : 4);
			weatherView.setLayoutParams(lp);
			try {
				ConfettiManager manager = weatherView.getConfettiManager();
				Field confettoGenerator = ConfettiManager.class.getDeclaredField("confettoGenerator");
				confettoGenerator.setAccessible(true);
				confettoGenerator.set(manager, new FlowerGenerator(this));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			weatherView.resetWeather();
			weatherView.setVisibility(View.VISIBLE);
			weatherView.getConfettiManager().setRotationalVelocity(0, 45);
			angleListener = new GravitySensor(this, weatherView);
			angleListener.setOrientation(rotation);
			angleListener.setSpeed(35);
			angleListener.start();

			mHolidayHeader.setImageResource(R.drawable.lunar_newyear_header);
			mHolidayHeader.setVisibility(View.VISIBLE);
		} else if (Helpers.currentHoliday == Helpers.Holidays.PANDEMIC) {
			weatherView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			weatherView.setPrecipType(PrecipType.CLEAR);
			weatherView.setSpeed(0);
			weatherView.setEmissionRate(0.6f);
			weatherView.setFadeOutPercent(1.0f);
			weatherView.setAngle(0);
			try {
				ConfettiManager manager = weatherView.getConfettiManager();
				Field confettoGenerator = ConfettiManager.class.getDeclaredField("confettoGenerator");
				confettoGenerator.setAccessible(true);
				confettoGenerator.set(manager, new FilthGenerator(this));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			weatherView.resetWeather();
			weatherView.setVisibility(View.VISIBLE);
			weatherView.getConfettiManager().setRotationalVelocity(0, 15).setTTL(30000);
		} else {
			((ViewGroup)weatherView.getParent()).removeView(weatherView);
			((ViewGroup)mHolidayHeader.getParent()).removeView(mHolidayHeader);
		}

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

		Helpers.updateNewModsMarking(this);

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
			if (angleListener != null) angleListener.stop();
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
		if (fragment instanceof MainFragment && ((MainFragment)fragment).actionMode != null)
			((MainFragment)fragment).actionMode.finish();
		else if (fragment instanceof SubFragment)
			((SubFragment)fragment).finish();
		else
			super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			PreferenceFragmentBase fragment = (PreferenceFragmentBase)getFragmentManager().findFragmentById(R.id.fragment_container);
			if (fragment != null && fragment.getView() != null && !fragment.supressMenu) try {
				fragment.getView().post(fragment::showImmersionMenu);
				return true;
			} catch (Throwable t) {}
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
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.createReport();
				else
					Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show();
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	public void onPause() {
		if (angleListener != null) angleListener.onPause();
		if (weatherView != null) weatherView.getConfettiManager().terminate();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (weatherView != null) weatherView.getConfettiManager().animate();
		if (angleListener != null) angleListener.onResume();
	}

}