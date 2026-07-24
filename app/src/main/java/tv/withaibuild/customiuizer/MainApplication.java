package tv.withaibuild.customiuizer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import tv.withaibuild.customiuizer.utils.AppHelper;
import tv.withaibuild.customiuizer.utils.Helpers;


public class MainApplication extends Application {
	@Override
	protected void attachBaseContext(Context base) {
		Helpers.withinAppContext = true;
		SharedPreferences sp = AppHelper.getSharedPrefs(base, false);
		AppHelper.appPrefs = sp;
		String locale = sp.getString("pref_key_miuizer_locale", "auto");
		if (!"auto".equals(locale) && !"1".equals(locale)) Locale.setDefault(Locale.forLanguageTag(locale));
		super.attachBaseContext(base);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (nm != null) {
				android.app.NotificationChannel channel = new android.app.NotificationChannel("customiuizer_default", getString(R.string.app_name), android.app.NotificationManager.IMPORTANCE_LOW);
				nm.createNotificationChannel(channel);
			}
		}
	}
}
