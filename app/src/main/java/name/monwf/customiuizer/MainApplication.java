package name.monwf.customiuizer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;


public class MainApplication extends Application {
	@Override
	protected void attachBaseContext(Context base) {
		Helpers.withinAppContext = true;
		SharedPreferences sp = null;
		try {
			sp = AppHelper.getSharedPrefs(base, false);
		} catch (Throwable t) {
			try {
				sp = AppHelper.getSharedPrefs(base, true);
			} catch (Throwable ignore) {}
		}
		AppHelper.appPrefs = sp;
		if (sp != null) {
			String locale = sp.getString("pref_key_miuizer_locale", "auto");
			if (!"auto".equals(locale) && !"1".equals(locale)) Locale.setDefault(Locale.forLanguageTag(locale));
		}
		super.attachBaseContext(base);
		AppHelper.registerRemotePrefsSync();
		AppHelper.syncToProtected(sp, base);
	}
}
