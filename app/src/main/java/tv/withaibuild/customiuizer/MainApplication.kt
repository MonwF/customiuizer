package tv.withaibuild.customiuizer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import java.util.Locale

class MainApplication : Application() {

    override fun attachBaseContext(base: Context) {
        Helpers.withinAppContext = true
        Helpers.appContentResolver = base.contentResolver
        val sp: SharedPreferences = AppHelper.getSharedPrefs(base, false)
        AppHelper.appPrefs = sp
        val locale = sp.getString("pref_key_miuizer_locale", "auto") ?: "auto"
        if (locale != "auto" && locale != "1") {
            Locale.setDefault(Locale.forLanguageTag(locale))
        }
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(
                NotificationChannel("customiuizer_default", getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
            )
        }
    }
}
