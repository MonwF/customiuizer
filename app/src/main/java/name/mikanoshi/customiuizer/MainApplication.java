package name.mikanoshi.customiuizer;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.Locale;

import name.mikanoshi.customiuizer.utils.Helpers;


public class MainApplication extends Application {

	public boolean mStarted = false;

	@Override
	protected void attachBaseContext(Context base) {
		Context pContext;
		try {
			pContext = Helpers.getProtectedContext(base);
			Helpers.prefs = Helpers.getSharedPrefs(pContext, false);
			String locale = Helpers.prefs.getString("pref_key_miuizer_locale", "auto");
			if (locale != null && !"auto".equals(locale) && !"1".equals(locale)) Locale.setDefault(Locale.forLanguageTag(locale));
		} catch (Throwable t) {
			pContext = base;
			Log.e("miuizer", "Failed to use protected storage!");
		}
		super.attachBaseContext(pContext);
		mStarted = true;
	}

}
