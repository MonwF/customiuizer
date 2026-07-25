package tv.withaibuild.customiuizer.subs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope

import java.lang.reflect.Method
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashSet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragmentWithSearch
import tv.withaibuild.customiuizer.mods.GlobalActions
import tv.withaibuild.customiuizer.utils.AppData
import tv.withaibuild.customiuizer.utils.AppDataAdapter
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import tv.withaibuild.customiuizer.utils.LockedAppAdapter
import tv.withaibuild.customiuizer.utils.PrivacyAppAdapter

class AppSelector : SubFragmentWithSearch() {

    private var initialized = false
    private var standalone = false
    private var multi = false
    private var bwlist = false
    private var privacy = false
    private var applock = false
    private var customTitles = false
    private var share = false
    private var openwith = false
    private var selectActivity = false
    private var configFetched = false
    private var key: String? = null
    private var mPrivacyAppsMap: HashMap<Int, ArrayList<String>>? = null

    private val configReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != GlobalActions.EVENT_PREFIX + "PUSHAPPCONFIG") return
            val datatype = intent.getStringExtra("DATATYPE")
            if (datatype == "privacy") {
                configFetched = true
                initialized = true
                mPrivacyAppsMap = intent.getSerializableExtra("privacyAppsMap") as? HashMap<Int, ArrayList<String>>
                lifecycleScope.launch(Dispatchers.Main) {
                    if (isAdded) setupList()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.padded = false
        super.onCreate(savedInstanceState)

        val args = arguments
        standalone = args?.getBoolean("standalone", false) ?: false
        multi = args?.getBoolean("multi", false) ?: false
        bwlist = args?.getBoolean("bw", false) ?: false
        privacy = args?.getBoolean("privacy", false) ?: false
        applock = args?.getBoolean("applock", false) ?: false
        customTitles = args?.getBoolean("custom_titles", false) ?: false
        share = args?.getBoolean("share", false) ?: false
        openwith = args?.getBoolean("openwith", false) ?: false
        selectActivity = args?.getBoolean("activity", false) ?: false
        key = args?.getString("key")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (initialized) {
            setupList()
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                delay(animDur.toLong())
                loadApps()
                if (privacy && !configFetched) return@launch
                initialized = true
                withContext(Dispatchers.Main) {
                    if (isAdded) setupList()
                }
            }
            registerReceivers()
        }
    }

    private fun loadApps() {
        val ctx = context ?: return
        try {
            if (selectActivity || privacy || applock || (multi && key != null)) {
                if (openwith) {
                    if (Helpers.openWithAppsList == null) {
                        Helpers.getOpenWithApps(ctx)
                    }
                } else if (share) {
                    if (Helpers.shareAppsList == null) {
                        Helpers.getShareApps(ctx)
                    }
                } else {
                    if (AppHelper.installedAppsList == null) {
                        Helpers.getInstalledApps(ctx)
                    }
                }
            } else {
                if (Helpers.launchableAppsList == null) {
                    Helpers.getLaunchableApps(ctx)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setupList() {
        val context = context ?: return
        if (!isAdded) return

        val currentKey = key ?: ""
        if (multi && currentKey != "") {
            if (openwith) {
                if (Helpers.openWithAppsList == null) return
                listView?.adapter = AppDataAdapter(context, Helpers.openWithAppsList, AppHelper.AppAdapterType.Mutli, currentKey, bwlist)
            } else if (share) {
                if (Helpers.shareAppsList == null) return
                listView?.adapter = AppDataAdapter(context, Helpers.shareAppsList, AppHelper.AppAdapterType.Mutli, currentKey, bwlist)
            } else {
                if (AppHelper.installedAppsList == null) return
                listView?.adapter = AppDataAdapter(context, AppHelper.installedAppsList, AppHelper.AppAdapterType.Mutli, currentKey, bwlist)
            }
        } else if (privacy) {
            if (AppHelper.installedAppsList == null) return
            listView?.adapter = PrivacyAppAdapter(context, AppHelper.installedAppsList, mPrivacyAppsMap)
        } else if (applock) {
            if (AppHelper.installedAppsList == null) return
            listView?.adapter = LockedAppAdapter(context, AppHelper.installedAppsList)
        } else if (customTitles) {
            if (Helpers.launchableAppsList == null) return
            listView?.adapter = AppDataAdapter(context, Helpers.launchableAppsList, AppHelper.AppAdapterType.CustomTitles, currentKey)
        } else if (standalone && currentKey != "") {
            if (Helpers.launchableAppsList == null) return
            listView?.adapter = AppDataAdapter(context, Helpers.launchableAppsList, AppHelper.AppAdapterType.Standalone, currentKey)
        } else if (selectActivity) {
            if (AppHelper.installedAppsList == null) return
            listView?.adapter = AppDataAdapter(context, AppHelper.installedAppsList, AppHelper.AppAdapterType.Default, currentKey)
        } else {
            if (Helpers.launchableAppsList == null) return
            listView?.adapter = AppDataAdapter(context, Helpers.launchableAppsList)
        }

        listView?.setOnItemClickListener { parent: AdapterView<*>, _, position: Int, _ ->
            val app = (parent.adapter?.getItem(position) as? AppData) ?: return@setOnItemClickListener
            if (multi && key != null) {
                handleMultiSelect(parent, app)
            } else if (selectActivity) {
                val args = Bundle()
                args.putString("key", key)
                args.putString("package", app.pkgName)
                args.putInt("user", app.user)
                val activitySelect = ActivitySelector()
                activitySelect.setTargetFragment(this@AppSelector, targetRequestCode)
                openSubFragment(activitySelect, args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.select_activity, R.layout.prefs_app_selector)
            } else if (privacy) {
                toggleAppPrivacy(app)
                (parent.adapter as? PrivacyAppAdapter)?.notifyDataSetChanged()
            } else if (applock) {
                handleAppLock(app, parent)
            } else if (customTitles) {
                val k = key ?: return@setOnItemClickListener
                AppHelper.showInputDialog(activity, k + ":" + app.pkgName + "|" + app.actName + "|" + app.user, R.string.launcher_renameapps_modified, 0, 1) { _, text ->
                    if (TextUtils.isEmpty(text)) {
                        AppHelper.appPrefs.edit().remove(k + ":" + app.pkgName + "|" + app.actName + "|" + app.user).apply()
                    } else {
                        AppHelper.appPrefs.edit().putString(k + ":" + app.pkgName + "|" + app.actName + "|" + app.user, text).apply()
                    }
                    (parent.adapter as? AppDataAdapter)?.notifyDataSetChanged()
                }
            } else {
                val intent = Intent()
                if (app.pkgName == "" && app.actName == "") {
                    intent.putExtra("app", "")
                } else {
                    intent.putExtra("app", app.pkgName + "|" + app.actName)
                }
                intent.putExtra("user", app.user)
                targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                finish()
            }
        }

        view?.findViewById<View>(R.id.am_progressBar)?.visibility = View.GONE
    }

    private fun handleMultiSelect(parent: AdapterView<*>, app: AppData) {
        val selectedApps = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))
        if (bwlist) {
            val selectedAppsBlack = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key + "_black", LinkedHashSet()))
            when {
                selectedApps.contains(app.pkgName) -> {
                    selectedApps.remove(app.pkgName)
                    selectedAppsBlack.add(app.pkgName)
                }
                selectedAppsBlack.contains(app.pkgName) -> {
                    selectedApps.remove(app.pkgName)
                    selectedAppsBlack.remove(app.pkgName)
                }
                else -> {
                    selectedApps.add(app.pkgName)
                    selectedAppsBlack.remove(app.pkgName)
                }
            }
            AppHelper.appPrefs.edit().putStringSet(key + "_black", selectedAppsBlack).apply()
        } else {
            val identifier = if (share || openwith) app.pkgName + "|" + app.user else app.pkgName
            if (selectedApps.contains(identifier)) {
                selectedApps.remove(identifier)
            } else {
                selectedApps.add(identifier)
                if (openwith) {
                    val mimeKey = key + "_" + app.pkgName + "|" + app.user
                    val mimeFlags = AppHelper.getIntOfAppPrefs(mimeKey, Helpers.MimeType.ALL)
                    val checkedTypes = booleanArrayOf(
                        (mimeFlags and Helpers.MimeType.IMAGE) == Helpers.MimeType.IMAGE,
                        (mimeFlags and Helpers.MimeType.AUDIO) == Helpers.MimeType.AUDIO,
                        (mimeFlags and Helpers.MimeType.VIDEO) == Helpers.MimeType.VIDEO,
                        (mimeFlags and Helpers.MimeType.DOCUMENT) == Helpers.MimeType.DOCUMENT,
                        (mimeFlags and Helpers.MimeType.ARCHIVE) == Helpers.MimeType.ARCHIVE,
                        (mimeFlags and Helpers.MimeType.LINK) == Helpers.MimeType.LINK,
                        (mimeFlags and Helpers.MimeType.OTHERS) == Helpers.MimeType.OTHERS
                    )
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setTitle(R.string.system_cleanopenwith_datatype)
                    builder.setMultiChoiceItems(R.array.mimetypes, checkedTypes) { _, which, isChecked ->
                        checkedTypes[which] = isChecked
                    }
                    builder.setCancelable(true)
                    builder.setPositiveButton(android.R.string.ok) { _, _ ->
                        var sum = 0
                        var order = 0
                        for (checkedType in checkedTypes) {
                            if (checkedType) sum += 1 shl order
                            order++
                        }
                        AppHelper.appPrefs.edit().putInt(mimeKey, sum).apply()
                    }
                    builder.show()
                }
            }
        }
        AppHelper.appPrefs.edit().putStringSet(key, selectedApps).apply()
        (parent.adapter as? AppDataAdapter)?.updateSelectedApps()
    }

    @SuppressLint("WrongConstant")
    private fun handleAppLock(app: AppData, parent: AdapterView<*>) {
        val act = activity ?: return
        try {
            val mSecurityManager = act.getSystemService("security") ?: return
            val smClass = (mSecurityManager as Any)::class.java
            val getApplicationAccessControlEnabledAsUser: Method = smClass.getDeclaredMethod("getApplicationAccessControlEnabledAsUser", String::class.java, Int::class.javaPrimitiveType)
            getApplicationAccessControlEnabledAsUser.isAccessible = true
            val setApplicationAccessControlEnabledForUser: Method = smClass.getDeclaredMethod("setApplicationAccessControlEnabledForUser", String::class.java, Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            setApplicationAccessControlEnabledForUser.isAccessible = true
            val enabled = getApplicationAccessControlEnabledAsUser.invoke(mSecurityManager, app.pkgName, app.user) as? Boolean ?: return
            setApplicationAccessControlEnabledForUser.invoke(mSecurityManager, app.pkgName, !enabled, app.user)
            (parent.adapter as? LockedAppAdapter)?.notifyDataSetChanged()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun registerReceivers() {
        if (privacy) {
            val ctx = getValidContext()
            ctx.registerReceiver(configReceiver, IntentFilter(GlobalActions.EVENT_PREFIX + "PUSHAPPCONFIG"), Context.RECEIVER_EXPORTED)
            val intent = Intent(GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG")
            intent.putExtra("DATATYPE", "privacy")
            intent.setPackage("com.miui.home")
            ctx.sendBroadcast(intent)
        }
    }

    private fun unregisterReceivers() {
        try {
            getValidContext().unregisterReceiver(configReceiver)
        } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        unregisterReceivers()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == targetRequestCode) {
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun toggleAppPrivacy(app: AppData) {
        val map = mPrivacyAppsMap ?: return
        val user = app.user
        val privacyApps = map[user] ?: ArrayList<String>().also { map[user] = it }
        val isPrivate = !privacyApps.contains(app.pkgName)
        if (privacyApps.contains(app.pkgName)) {
            privacyApps.remove(app.pkgName)
        } else {
            privacyApps.add(app.pkgName)
        }
        val intent = Intent(GlobalActions.EVENT_PREFIX + "FETCHAPPCONFIG")
        intent.putExtra("DATATYPE", "privacy_change")
        intent.putExtra("app", app.pkgName)
        intent.putExtra("userId", app.user)
        intent.putExtra("privacy", isPrivate)
        intent.setPackage("com.miui.home")
        getValidContext().sendBroadcast(intent)
    }
}
