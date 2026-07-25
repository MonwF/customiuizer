package tv.withaibuild.customiuizer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import io.github.libxposed.service.RemotePreferences
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers

class MainActivity : AppCompatActivity() {

    private var mainFrag: MainFragment? = null
    private var prefsChanged: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun attachBaseContext(base: Context) {
        try {
            super.attachBaseContext(AppHelper.getLocaleContext(base) ?: base)
        } catch (t: Throwable) {
            t.printStackTrace()
            super.attachBaseContext(base)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (AppHelper.remotePrefs == null) {
            XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
                override fun onServiceBind(service: XposedService) {
                    AppHelper.moduleActive = true
                    AppHelper.remotePrefs = service.getRemotePreferences(AppHelper.prefsName + "_remote") as RemotePreferences
                }

                override fun onServiceDied(service: XposedService) {
                    AppHelper.moduleActive = false
                    AppHelper.remotePrefs = null
                }
            })
        }

        val myToolbar = findViewById<Toolbar>(R.id.mainActionBar)
        setSupportActionBar(myToolbar)
        if (savedInstanceState != null) {
            mainFrag = supportFragmentManager.getFragment(savedInstanceState, "mainFrag") as? MainFragment
        } else if (mainFrag == null) {
            mainFrag = MainFragment()
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, mainFrag!!)
                .commit()
        }

        val ignoreKeys = HashSet<String>().apply {
            add("pref_key_miuizer_locale")
            add("pref_key_miuizer_launchericon")
            add("pref_key_miuizer_synced_from_lsposed")
        }

        prefsChanged = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (AppHelper.remotePrefs == null) return@OnSharedPreferenceChangeListener
            if (key == null) {
                val prefEdit = AppHelper.remotePrefs!!.edit()
                for (remoteKey in AppHelper.remotePrefs!!.all.keys) {
                    prefEdit.remove(remoteKey)
                }
                prefEdit.apply()
                return@OnSharedPreferenceChangeListener
            }
            if (ignoreKeys.contains(key)) return@OnSharedPreferenceChangeListener
            val value = sharedPreferences.all[key] ?: run {
                AppHelper.remotePrefs!!.edit().remove(key).apply()
                return@OnSharedPreferenceChangeListener
            }
            val prefEdit = AppHelper.remotePrefs!!.edit()
            when (value) {
                is Boolean -> prefEdit.putBoolean(key, value)
                is Float -> prefEdit.putFloat(key, value)
                is Int -> prefEdit.putInt(key, value)
                is Long -> prefEdit.putLong(key, value)
                is String -> prefEdit.putString(key, value)
                is Set<*> -> @Suppress("UNCHECKED_CAST") prefEdit.putStringSet(key, value as Set<String>)
            }
            prefEdit.apply()
        }

        prefsChanged?.let { AppHelper.appPrefs!!.registerOnSharedPreferenceChangeListener(it) }
    }

    fun navToSubFragment(
        fragment: Fragment,
        args: Bundle,
        settingsType: AppHelper.SettingsType,
        abType: AppHelper.ActionBarType,
        titleResId: Int,
        contentResId: Int
    ) {
        navToSubFragment(fragment, args, settingsType, abType, resources.getString(titleResId), contentResId)
    }

    fun navToSubFragment(
        fragment: Fragment,
        args: Bundle,
        settingsType: AppHelper.SettingsType,
        abType: AppHelper.ActionBarType,
        title: String,
        contentResId: Int
    ) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is PreferenceFragmentBase) {
            currentFragment.openSubFragment(fragment, args, settingsType, abType, title, contentResId)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mainFrag?.let { supportFragmentManager.putFragment(outState, "mainFrag", it) }
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("ApplySharedPref")
    override fun onDestroy() {
        try {
            prefsChanged?.let { AppHelper.appPrefs!!.unregisterOnSharedPreferenceChangeListener(it) }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment == null) {
                    finish()
                } else if (fragment is MainFragment) {
                    finish()
                } else if (fragment is SubFragment) {
                    fragment.finish()
                }
                true
            }
            R.id.resetsettings -> {
                showResetSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showResetSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reset_settings)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                AppHelper.appPrefs!!.edit().clear().apply()
                AlertDialog.Builder(this)
                    .setTitle(R.string.reset_settings_done)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        Process.killProcess(Process.myPid())
                    }
                    .show()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_MENU) true else super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty()) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        when (requestCode) {
            Helpers.REQUEST_PERMISSIONS_WIFI -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
                        Toast.makeText(this, R.string.permission_scan, Toast.LENGTH_LONG).show()
                    else
                        Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show()
                }
            }
            Helpers.REQUEST_PERMISSIONS_BLUETOOTH -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT))
                        Toast.makeText(this, R.string.permission_scan, Toast.LENGTH_LONG).show()
                    else
                        Toast.makeText(this, R.string.permission_permanent, Toast.LENGTH_LONG).show()
                }
            }
            Helpers.REQUEST_PERMISSIONS_REPORT -> Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show()
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}
