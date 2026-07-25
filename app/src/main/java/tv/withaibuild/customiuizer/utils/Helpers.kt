package tv.withaibuild.customiuizer.utils

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.LruCache
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceScreen
import miui.util.HapticFeedbackUtil
import org.xmlpull.v1.XmlPullParser
import tv.withaibuild.customiuizer.BuildConfig
import tv.withaibuild.customiuizer.PrefsProvider
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.prefs.PreferenceCategoryEx
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap

@Suppress("WeakerAccess")
object Helpers {

    @JvmField
    val modulePkg = BuildConfig.APPLICATION_ID

    // public static final String versionFile = "xposed_version";
    // public static final String wallpaperFile = "lockscreen_wallpaper";

    const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

    const val MIUIZER_NS = "http://schemas.android.com/apk/res-auto"

    const val ACCESS_SECURITY_CENTER = "com.miui.securitycenter.permission.ACCESS_SECURITY_CENTER_PROVIDER"

    const val NEW_MODS_SEARCH_QUERY = "\uD83C\uDD95"

    @JvmField
    var shareAppsList: ArrayList<AppData> = ArrayList()

    @JvmField
    var openWithAppsList: ArrayList<AppData> = ArrayList()

    @JvmField
    var launchableAppsList: ArrayList<AppData> = ArrayList()

    @JvmField
    val allModsList = ArrayList<ModData>()

    @JvmField
    val markColor = Color.rgb(205, 73, 97)

    @JvmField
    val markColorVibrant = Color.rgb(255, 0, 0)

    const val REQUEST_PERMISSIONS_WIFI = 3

    const val REQUEST_PERMISSIONS_REPORT = 4

    const val REQUEST_PERMISSIONS_BLUETOOTH = 5

    const val REQUEST_PERMISSIONS_SECURITY_CENTER = 6

    @JvmField
    var withinAppContext = false

    @JvmField
    var appContentResolver: ContentResolver? = null

    private val ICON_CACHE_KB = (
        Runtime.getRuntime().maxMemory() / 1024 / 8
        ).toInt().coerceIn(1024, 16 * 1024)

    @JvmField
    val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(ICON_CACHE_KB) {
        override fun sizeOf(key: String, icon: Bitmap): Int {
            return icon.allocationByteCount / 1024
        }
    }

    @JvmField
    var mWakeLock: PowerManager.WakeLock? = null

    @JvmField
    var showNewMods = true

    @JvmField
    val newMods = HashSet(listOf("pref_key_launcher_nozoomanim"))

    object MimeType {
        const val IMAGE = 1
        const val AUDIO = 2
        const val VIDEO = 4
        const val DOCUMENT = 8
        const val ARCHIVE = 16
        const val LINK = 32
        const val OTHERS = 64
        const val ALL = IMAGE or AUDIO or VIDEO or DOCUMENT or ARCHIVE or LINK or OTHERS
    }

    fun interface InputCallback {
        fun onInputFinished(key: String?, text: String?)
    }

    @JvmStatic
    fun setMiuiPrefItem(item: View?) {
        item ?: return
        item.setBackgroundResource(R.drawable.list_item_bg)
        val title = item.findViewById<TextView>(android.R.id.title)
        var resId = item.resources.getIdentifier("preference_item_bg", "drawable", "miui")
        if (resId != 0) item.setBackgroundResource(resId)
        resId = item.resources.getIdentifier("normal_text_size", "dimen", "miui")
        if (resId != 0 && title != null) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, item.resources.getDimensionPixelSize(resId).toFloat())
        }
        resId = item.resources.getIdentifier("secondary_text_size", "dimen", "miui")
        if (resId != 0) {
            val summary = item.findViewById<TextView>(android.R.id.summary)
            val text1 = item.findViewById<TextView>(android.R.id.text1)
            val text2 = item.findViewById<TextView>(android.R.id.text2)
            val size = item.resources.getDimensionPixelSize(resId).toFloat()
            summary?.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            text1?.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            text2?.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        }
        if (title != null && "header" == title.tag) {
            val resIdSize = item.resources.getIdentifier("preference_category_text_size", "dimen", "miui")
            if (resIdSize != 0) title.setTextSize(TypedValue.COMPLEX_UNIT_PX, item.resources.getDimensionPixelSize(resIdSize).toFloat())
        }

        val resIdLeft = item.resources.getIdentifier("preference_item_padding_left", "dimen", "miui")
        val resIdRight = item.resources.getIdentifier("preference_item_padding_right", "dimen", "miui")
        val resIdTop = item.resources.getIdentifier("preference_item_padding_top", "dimen", "miui")
        val resIdBottom = item.resources.getIdentifier("preference_item_padding_bottom", "dimen", "miui")
        val paddingLeft = if (resIdLeft == 0) item.paddingLeft else item.resources.getDimensionPixelSize(resIdLeft)
        val paddingRight = if (resIdRight == 0) item.paddingRight else item.resources.getDimensionPixelSize(resIdRight)
        val paddingTop = if (resIdTop == 0) item.paddingTop else item.resources.getDimensionPixelSize(resIdTop)
        val paddingBottom = if (resIdBottom == 0) item.paddingBottom else item.resources.getDimensionPixelSize(resIdBottom)
        item.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    @JvmStatic
    fun isNightMode(context: Context): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    @JvmStatic
    fun getMutableActivityPendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags = flags or PendingIntent.FLAG_MUTABLE
        return PendingIntent.getActivity(context, requestCode, intent, flags)
    }

    @JvmStatic
    fun getImmutableActivityPendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, requestCode, intent, flags)
    }

    @JvmStatic
    fun isDeviceEncrypted(context: Context?): Boolean {
        context ?: return false
        val policyMgr = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val encryption = policyMgr?.storageEncryptionStatus ?: return false
        return encryption == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
            encryption == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING ||
            encryption == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER
    }

    @JvmStatic
    fun launchActivity(act: AppCompatActivity, pkg: String, cmp: String) {
        launchActivity(act, pkg, cmp, false)
    }

    @JvmStatic
    fun launchActivity(act: AppCompatActivity, pkg: String, cmp: String, silent: Boolean): Boolean {
        val pm = act.packageManager
        return try {
            pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            intent.component = ComponentName(pkg, cmp)
            act.startActivity(intent)
            act.overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit)
            true
        } catch (t: Throwable) {
            if (!silent) Toast.makeText(act, R.string.various_hiddenfeatures_not_found, Toast.LENGTH_LONG).show()
            false
        }
    }

    @JvmStatic
    fun hideKeyboard(act: AppCompatActivity?, view: View?) {
        view ?: return
        try {
            val context = act ?: view.context
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return
            val token: android.os.IBinder?
            val currentFocusedView = act?.currentFocus ?: view
            token = if (currentFocusedView != null) currentFocusedView.windowToken else null
            if (token != null) inputManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @JvmStatic
    fun showOKDialog(context: Context, title: Int, text: Int) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(text)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    @JvmStatic
    fun checkStorageReadable(context: Context): Boolean {
        val state = Environment.getExternalStorageState()
        return if (state == Environment.MEDIA_MOUNTED_READ_ONLY || state == Environment.MEDIA_MOUNTED) {
            true
        } else {
            showOKDialog(context, R.string.warning, R.string.storage_unavailable)
            false
        }
    }

    @JvmStatic
    fun checkSettingsPerm(act: AppCompatActivity): Boolean {
        return act.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun checkPermAndRequest(act: AppCompatActivity, perm: String, action: Int): Boolean {
        return if (act.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
            act.requestPermissions(arrayOf(perm), action)
            false
        } else {
            true
        }
    }

    @JvmStatic
    fun getNextStockAlarmTime(context: Context): Long {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return 0
        val aci = alarmMgr.nextAlarmClock
        return aci?.triggerTime ?: 0
    }

    @JvmStatic
    fun updateNewModsMarking(context: Context, opt: Int) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(modulePkg, 0)
            val appInstalled = System.currentTimeMillis() - File(appInfo.sourceDir).lastModified()
//            Log.e("miuizer", "installed: $appInstalled msecs or ${appInstalled / (1000 * 60 * 60)} hrs")
            showNewMods = when (opt) {
                0 -> false
                4 -> true
                else -> appInstalled < (if (opt == 1) 1 else if (opt == 2) 3 else 7) * 24 * 60 * 60 * 1000
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @JvmStatic
    fun applyNewMod(title: TextView) {
        val titleStr = title.text
        val newModStr = title.resources.getString(R.string.miuizer_new_mod) + " "
        val start = titleStr.length + 3
        val end = start + newModStr.length
        val ssb = SpannableStringBuilder(title.text.toString() + "   " + newModStr)
        ssb.setSpan(ForegroundColorSpan(markColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(RelativeSizeSpan(0.75f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        title.text = ssb
    }

    @JvmStatic
    fun applySearchItemHighlight(finalView: View) {
        val highColor = finalView.resources.getColor(R.color.color_popup_background, finalView.context.theme)
        val colorAnim = ObjectAnimator.ofInt(finalView, "backgroundColor", highColor, Color.TRANSPARENT)
        colorAnim.duration = 1200
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.repeatCount = 1
        colorAnim.startDelay = 300
        colorAnim.start()
    }

    @JvmStatic
    fun openURL(context: Context?, url: String) {
        if (context == null) return
        val uriIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(uriIntent)
    }

    @JvmStatic
    fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        )
    }

    @JvmStatic
    @JvmOverloads
    fun getChildViewsRecursive(view: View?, includeContainers: Boolean = true): ArrayList<View> {
        view ?: return ArrayList()
        return if (view is ViewGroup) {
            val list = ArrayList<View>()
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (includeContainers) list.add(view)
                list.addAll(getChildViewsRecursive(child, includeContainers))
            }
            list
        } else {
            val list = ArrayList<View>()
            list.add(view)
            list
        }
    }

    private fun getModTitle(res: Resources, title: String?): String? {
        if (title == null) return null
        val titleResId = title.substring(1).toIntOrNull() ?: return null
        if (titleResId <= 0) return null
        return res.getString(titleResId)
    }

    private fun checkMultiUserPermission(context: Context): Boolean {
        return context.packageManager.checkPermission("android.permission.INTERACT_ACROSS_USERS", modulePkg) == PackageManager.PERMISSION_GRANTED
    }

    private fun getAppContentResolver(): ContentResolver? {
        if (appContentResolver != null) return appContentResolver
        try {
            val appGlobals = Class.forName("android.app.AppGlobals")
            val app = appGlobals.getMethod("getInitialApplication").invoke(null)
            if (app is Context) return app.contentResolver
        } catch (t: Throwable) {
            // ignore
        }
        try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val app = activityThread.getMethod("currentApplication").invoke(null)
            if (app is Context) return app.contentResolver
        } catch (t: Throwable) {
            // ignore
        }
        return null
    }

    private fun getAnimationScaleKey(type: Int): String {
        return when (type) {
            0 -> Settings.Global.WINDOW_ANIMATION_SCALE
            1 -> Settings.Global.TRANSITION_ANIMATION_SCALE
            2 -> Settings.Global.ANIMATOR_DURATION_SCALE
            else -> Settings.Global.WINDOW_ANIMATION_SCALE
        }
    }

    @JvmStatic
    fun getAnimationScale(type: Int): Float {
        val resolver = getAppContentResolver() ?: return 1.0f
        return try {
            Settings.Global.getFloat(resolver, getAnimationScaleKey(type), 1.0f)
        } catch (t: Throwable) {
            t.printStackTrace()
            1.0f
        }
    }

    @JvmStatic
    fun setAnimationScale(type: Int, value: Float) {
        val resolver = getAppContentResolver() ?: return
        val key = getAnimationScaleKey(type)
        var written = false
        try {
            written = Settings.Global.putFloat(resolver, key, value)
        } catch (e: SecurityException) {
            // app lacks WRITE_SECURE_SETTINGS, fall through to root
        } catch (e: IllegalArgumentException) {
            // app lacks WRITE_SECURE_SETTINGS, fall through to root
        } catch (t: Throwable) {
            t.printStackTrace()
            return
        }
        if (!written) try {
            val pb = ProcessBuilder("su", "-c", "settings put global $key $value")
            val p = pb.start()
            p.waitFor()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun getPackageInfoAsUser(): Method? {
        return try {
            PackageManager::class.java.getMethod("getPackageInfoAsUser", String::class.java, Integer.TYPE, Integer.TYPE)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
            null
        }
    }

    @JvmStatic
    fun getInstalledApps(context: Context) {
        val pm = context.packageManager
        var includeDualApps = checkMultiUserPermission(context)
        val packageInfoMethod = getPackageInfoAsUser()
        if (packageInfoMethod == null) includeDualApps = false

        val packs = pm.getInstalledApplications(PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS)
        val installedApps = ArrayList<AppData>()
        for (pack in packs) try {
            val app = AppData().apply {
                enabled = pack.enabled
                label = pack.loadLabel(pm).toString()
                pkgName = pack.packageName
                actName = "-"
            }
            installedApps.add(app)
            if (includeDualApps) try {
                if (packageInfoMethod?.invoke(pm, app.pkgName, 0, 999) != null) {
                    val appDual = AppData().apply {
                        enabled = pack.enabled
                        label = pack.loadLabel(pm).toString()
                        pkgName = pack.packageName
                        actName = "-"
                        user = 999
                    }
                    installedApps.add(appDual)
                }
            } catch (ignore: Throwable) {
            }
        } catch (e: Throwable) {
            XposedHelpers.log(e)
        }
        installedApps.sortWith { a, b -> a.label.compareTo(b.label, ignoreCase = true) }
        AppHelper.installedAppsList = installedApps
    }

    @SuppressLint("DiscouragedPrivateApi")
    @JvmStatic
    fun getLaunchableApps(context: Context) {
        val pm = context.packageManager
        var includeDualApps = checkMultiUserPermission(context)
        val packageInfoMethod = getPackageInfoAsUser()
        if (packageInfoMethod == null) includeDualApps = false

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val packs = pm.queryIntentActivities(mainIntent, 0)
        val launchable = ArrayList<AppData>()
        for (pack in packs) try {
            val app = AppData().apply {
                pkgName = pack.activityInfo.applicationInfo.packageName
                actName = pack.activityInfo.name
                enabled = pack.activityInfo.enabled
                label = pack.loadLabel(pm).toString()
            }
            launchable.add(app)
            if (includeDualApps) try {
                if (packageInfoMethod?.invoke(pm, app.pkgName, 0, 999) != null) {
                    val appDual = AppData().apply {
                        pkgName = pack.activityInfo.applicationInfo.packageName
                        actName = pack.activityInfo.name
                        enabled = pack.activityInfo.enabled
                        label = pack.loadLabel(pm).toString()
                        user = 999
                    }
                    launchable.add(appDual)
                }
            } catch (ignore: Throwable) {
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        launchable.sortWith { a, b -> a.label.compareTo(b.label, ignoreCase = true) }
        launchableAppsList = launchable
    }

    @JvmStatic
    fun getShareApps(context: Context) {
        val pm = context.packageManager
        var includeDualApps = checkMultiUserPermission(context)
        val packageInfoMethod = getPackageInfoAsUser()
        if (packageInfoMethod == null) includeDualApps = false

        val mainIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "*/*"
            putExtra("CustoMIUIzer", true)
        }
        val packs = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL or PackageManager.MATCH_DISABLED_COMPONENTS)
        val share = ArrayList<AppData>()
        for (pack in packs) try {
            val exists = share.any { it.pkgName == pack.activityInfo.applicationInfo.packageName }
            if (exists) continue
            val app = AppData().apply {
                pkgName = pack.activityInfo.applicationInfo.packageName
                actName = "-"
                enabled = pack.activityInfo.applicationInfo.enabled
                label = pack.activityInfo.applicationInfo.loadLabel(pm).toString()
            }
            share.add(app)
            if (includeDualApps) try {
                if (packageInfoMethod?.invoke(pm, app.pkgName, 0, 999) != null) {
                    val appDual = AppData().apply {
                        pkgName = pack.activityInfo.applicationInfo.packageName
                        actName = "-"
                        enabled = pack.activityInfo.applicationInfo.enabled
                        label = pack.activityInfo.applicationInfo.loadLabel(pm).toString()
                        user = 999
                    }
                    share.add(appDual)
                }
            } catch (ignore: Throwable) {
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        share.sortWith { a, b -> a.label.compareTo(b.label, ignoreCase = true) }
        shareAppsList = share
    }

    @JvmStatic
    fun getOpenWithApps(context: Context) {
        val pm = context.packageManager
        var includeDualApps = checkMultiUserPermission(context)
        val packageInfoMethod = getPackageInfoAsUser()
        if (packageInfoMethod == null) includeDualApps = false

        val mainIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(Uri.parse("content://${PrefsProvider.AUTHORITY}/test/5"), "*/*")
            putExtra("CustoMIUIzer", true)
        }
        val mainIntent2 = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("https://github.com")
            putExtra("CustoMIUIzer", true)
        }
        val packs = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL or PackageManager.MATCH_DISABLED_COMPONENTS).toMutableList()
        packs.addAll(pm.queryIntentActivities(mainIntent2, PackageManager.MATCH_ALL))

        val openWith = ArrayList<AppData>()
        for (pack in packs) try {
            val exists = openWith.any { it.pkgName == pack.activityInfo.applicationInfo.packageName }
            if (exists) continue
            val app = AppData().apply {
                pkgName = pack.activityInfo.applicationInfo.packageName
                actName = "-"
                enabled = pack.activityInfo.applicationInfo.enabled
                label = pack.activityInfo.applicationInfo.loadLabel(pm).toString()
            }
            openWith.add(app)
            if (includeDualApps) try {
                if (packageInfoMethod?.invoke(pm, app.pkgName, 0, 999) != null) {
                    val appDual = AppData().apply {
                        pkgName = pack.activityInfo.applicationInfo.packageName
                        actName = "-"
                        enabled = pack.activityInfo.applicationInfo.enabled
                        label = pack.activityInfo.applicationInfo.loadLabel(pm).toString()
                        user = 999
                    }
                    openWith.add(appDual)
                }
            } catch (ignore: Throwable) {
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        openWith.sortWith { a, b -> a.label.compareTo(b.label, ignoreCase = true) }
        openWithAppsList = openWith
    }

    @JvmStatic
    fun getAppName(context: Context, pkgActName: String): CharSequence? {
        return getAppName(context, pkgActName, false)
    }

    @JvmStatic
    fun getAppName(context: Context, pkgActName: String, forcePkg: Boolean): CharSequence? {
        val pm = context.packageManager
        val notSelected = context.resources.getString(R.string.notselected)
        val pkgActArray = pkgActName.split("\\|".toRegex())

        if (pkgActName != notSelected) {
            if (!forcePkg && pkgActArray.size >= 2 && pkgActArray[1].isNotBlank()) {
                return try {
                    pm.getActivityInfo(ComponentName(pkgActArray[0], pkgActArray[1]), 0).loadLabel(pm).toString()
                } catch (e: Throwable) {
                    null
                }
            } else if (pkgActArray[0].isNotBlank()) {
                return try {
                    val ai = pm.getApplicationInfo(pkgActArray[0], 0)
                    pm.getApplicationLabel(ai)
                } catch (e: Throwable) {
                    null
                }
            }
        }
        return null
    }

    @JvmStatic
    fun getAppIcon(context: Context, pkgActName: String): Drawable? {
        return getAppIcon(context, pkgActName, false)
    }

    @JvmStatic
    fun getAppIcon(context: Context, pkgActName: String, forcePkg: Boolean): Drawable? {
        val pm = context.packageManager
        val notSelected = context.resources.getString(R.string.notselected)
        val pkgActArray = pkgActName.split("\\|".toRegex())

        if (pkgActName != notSelected) {
            if (!forcePkg && pkgActArray.size >= 2 && pkgActArray[1].isNotBlank()) {
                return try {
                    pm.getActivityIcon(ComponentName(pkgActArray[0], pkgActArray[1]))
                } catch (e: Throwable) {
                    null
                }
            } else if (pkgActArray[0].isNotBlank()) {
                return try {
                    pm.getApplicationIcon(pkgActArray[0])
                } catch (e: Throwable) {
                    null
                }
            }
        }
        return null
    }

    @JvmStatic
    fun getShortcutIcon(context: Context, key: String): Drawable {
        val shortcutIconPath = context.filesDir.path + "/shortcuts/" + key + "_shortcut.png"
        val shortcutIconFile = File(shortcutIconPath)
        val shortcutIcon: Drawable? = if (shortcutIconFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(shortcutIconFile.absolutePath)
            if (bitmap != null) BitmapDrawable(context.resources, bitmap) else null
        } else null
        val layers = arrayOf(shortcutIcon ?: ColorDrawable())
        val insetShortcutIcon = LayerDrawable(layers)
        val padding = (5 * context.resources.displayMetrics.density).toInt()
        insetShortcutIcon.setLayerInset(0, padding, padding, padding, padding)
        return insetShortcutIcon
    }

    @Suppress("ConstantConditions")
    @JvmStatic
    fun getActionImageLocal(context: Context, key: String): Drawable? {
        return try {
            val action = AppHelper.getIntOfAppPrefs(key + "_action", 1)
            when (action) {
                8 -> getAppIcon(context, AppHelper.getStringOfAppPrefs(key + "_app", "") ?: "")
                9 -> getShortcutIcon(context, key)
                20 -> getAppIcon(context, AppHelper.getStringOfAppPrefs(key + "_activity", "") ?: "", true)
                else -> null
            }
        } catch (t: Throwable) {
            null
        }
    }

    private fun parsePrefXml(context: Context, xmlResId: Int) {
        val res = context.resources
        var lastPrefSub: String? = null
        var lastPrefSubTitle: String? = null
        var lastPrefSubSubTitle: String? = null
        var catResId = 0
        var catPrefKey: ModData.ModCat? = null

        when (xmlResId) {
            R.xml.prefs_system -> {
                catResId = R.string.system_mods
                catPrefKey = ModData.ModCat.pref_key_system
            }
            R.xml.prefs_launcher -> {
                catResId = R.string.launcher_title
                catPrefKey = ModData.ModCat.pref_key_launcher
            }
            R.xml.prefs_controls -> {
                catResId = R.string.controls_mods
                catPrefKey = ModData.ModCat.pref_key_controls
            }
            R.xml.prefs_various -> {
                catResId = R.string.various_mods
                catPrefKey = ModData.ModCat.pref_key_various
            }
        }

        try {
            res.getXml(xmlResId).use { xml ->
                var eventType = xml.eventType
                var order = 0
                val prefCatExName = PreferenceCategoryEx::class.java.canonicalName
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && PreferenceScreen::class.java.simpleName != xml.name) try {
                        if (xml.name == prefCatExName) {
                            if (xml.getAttributeValue(ANDROID_NS, "key") != null) {
                                lastPrefSub = xml.getAttributeValue(ANDROID_NS, "key")
                                lastPrefSubTitle = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"))
                                lastPrefSubSubTitle = null
                                order = 1
                            } else {
                                lastPrefSubSubTitle = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"))
                                order++
                            }
                            eventType = xml.next()
                            continue
                        }

                        val isChild = xml.getAttributeBooleanValue(MIUIZER_NS, "child", false)
                        if (!isChild) {
                            val titleStr = getModTitle(res, xml.getAttributeValue(ANDROID_NS, "title"))
                            if (titleStr != null) {
                                val modData = ModData()
                                modData.title = titleStr
                                modData.breadcrumbs = res.getString(catResId) +
                                    (if (lastPrefSubTitle == null) "" else "/$lastPrefSubTitle" +
                                    (if (lastPrefSubSubTitle == null) "" else "/$lastPrefSubSubTitle"))
                                modData.key = xml.getAttributeValue(ANDROID_NS, "key") ?: ""
                                modData.cat = catPrefKey!!
                                modData.sub = lastPrefSub ?: ""
                                modData.order = order
                                allModsList.add(modData)
                                // Log.e("miuizer", modData.key + " = " + modData.order)
                            }
                        }
                        order++
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                    eventType = xml.next()
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @JvmStatic
    fun getAllMods(context: Context, force: Boolean) {
        if (force) allModsList.clear()
        else if (allModsList.size > 0) return
        parsePrefXml(context, R.xml.prefs_system)
        parsePrefXml(context, R.xml.prefs_launcher)
        parsePrefXml(context, R.xml.prefs_controls)
        parsePrefXml(context, R.xml.prefs_various)
    }

    @JvmStatic
    fun performLightVibration(context: Context) {
        performLightVibration(context, false)
    }

    @JvmStatic
    fun performLightVibration(context: Context, ignoreOff: Boolean) {
        performVibration(context, false, ignoreOff)
    }

    @JvmStatic
    fun performStrongVibration(context: Context) {
        performVibration(context, true, false)
    }

    @JvmStatic
    fun performStrongVibration(context: Context, ignoreOff: Boolean) {
        performVibration(context, true, ignoreOff)
    }

    @JvmStatic
    fun performVibration(context: Context, isStrong: Boolean, ignoreOff: Boolean) {
        if (context == null) return
        val mHapticFeedbackUtil = HapticFeedbackUtil(context, false)
        mHapticFeedbackUtil.performHapticFeedback(
            if (isStrong) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.VIRTUAL_KEY,
            ignoreOff
        )
    }

    @JvmStatic
    fun performCustomVibration(context: Context, vibration: Int, ownPattern: String) {
        if (vibration == 0) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        val pattern = when (vibration) {
            1 -> {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                return
            }
            2 -> {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                return
            }
            3 -> longArrayOf(0, 250, 250, 250)
            4 -> longArrayOf(0, 250, 150, 125, 100, 125)
            5 -> longArrayOf(0, 150, 150, 100, 250, 150, 150, 100)
            6 -> longArrayOf(0, 100, 150, 100, 150, 100)
            7 -> {
                if (TextUtils.isEmpty(ownPattern)) return
                getVibrationPattern(ownPattern)
            }
            else -> return
        }
        try {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } catch (t: Throwable) {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    @JvmStatic
    fun getVibrationPattern(patternStr: String): LongArray {
        return try {
            if (TextUtils.isEmpty(patternStr)) return LongArray(0)
            val sPattern = patternStr.split(",")
            LongArray(sPattern.size) { i ->
                if (TextUtils.isEmpty(sPattern[i])) 0L else java.lang.Long.parseLong(sPattern[i])
            }
        } catch (t: Throwable) {
            LongArray(0)
        }
    }

    @JvmStatic
    fun getCacheFilePath(filename: String): String? {
        return when {
            File("/cache").canWrite() -> "/cache/$filename"
            File("/data/cache").canWrite() -> "/data/cache/$filename"
            File("/data/tmp").canWrite() -> "/data/tmp/$filename"
            else -> null
        }
    }

    @JvmStatic
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val mClipData = ClipData.newPlainText("", text)
        clipboard?.setPrimaryClip(mClipData)
    }

    @JvmStatic
    fun copyFile(from: String, to: String): Boolean {
        return try {
            Files.copy(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun containsStringPair(hayStack: Set<String>?, needle: String): Boolean {
        if (hayStack.isNullOrEmpty()) return false
        for (pair in hayStack) {
            val needles = pair.split("\\|".toRegex())
            if (needles[0].equals(needle, ignoreCase = true)) return true
        }
        return false
    }

    @JvmStatic
    fun fastBlur(sentBitmap: Bitmap, radius: Int): Bitmap? {
        val bitmap = sentBitmap.copy(sentBitmap.config!!, true)

        if (radius < 1) return null

        val w = bitmap.width
        val h = bitmap.height

        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)

        val vmin = IntArray(Math.max(w, h))

        val divsum = ((div + 1) shr 1) * ((div + 1) shr 1)
        val dv = IntArray(256 * divsum) { i -> i / divsum }

        var yw = 0
        var yi = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        for (y in 0 until h) {
            rinsum = 0; ginsum = 0; binsum = 0
            routsum = 0; goutsum = 0; boutsum = 0
            var rsum = 0; var gsum = 0; var bsum = 0
            for (i in -radius..radius) {
                var p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
            }
            stackpointer = radius

            for (x in 0 until w) {
                if (rsum < dv.size) r[yi] = dv[rsum]
                if (gsum < dv.size) g[yi] = dv[gsum]
                if (bsum < dv.size) b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                var p = pix[yw + vmin[x]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
            }
            yw += w
        }
        for (x in 0 until w) {
            rinsum = 0; ginsum = 0; binsum = 0
            routsum = 0; goutsum = 0; boutsum = 0
            var rsum = 0; var gsum = 0; var bsum = 0
            var yp = -radius * w
            for (i in -radius..radius) {
                yi = Math.max(0, yp) + x

                sir = stack[i + radius]

                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]

                rbs = r1 - Math.abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
            }
            yi = x
            stackpointer = radius
            for (y in 0 until h) {
                pix[yi] = ((0xff000000.toInt() and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum])

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                var p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)

        return bitmap
    }

    @JvmStatic
    fun constrain(amount: Int, low: Int, high: Int): Int {
        return if (amount < low) low else if (amount > high) high else amount
    }

    @JvmStatic
    fun constrain(amount: Float, low: Float, high: Float): Float {
        return if (amount < low) low else if (amount > high) high else amount
    }

    @JvmStatic
    fun lerp(start: Float, stop: Float, amount: Float): Float {
        return start + (stop - start) * amount
    }

    @JvmStatic
    fun lerp(start: Int, stop: Int, amount: Float): Float {
        return lerp(start.toFloat(), stop.toFloat(), amount)
    }

    /**
     * Returns the interpolation scalar (s) that satisfies the equation: value = lerp(a, b, s)
     *
     * If a == b, then this function will return 0.
     */
    @JvmStatic
    fun lerpInv(a: Float, b: Float, value: Float): Float {
        return if (a != b) (value - a) / (b - a) else 0.0f
    }

    /** Returns the single argument constrained between [0.0, 1.0].  */
    @JvmStatic
    fun saturate(value: Float): Float {
        return constrain(value, 0.0f, 1.0f)
    }

    /** Returns the saturated (constrained between [0, 1]) result of lerpInv.  */
    @JvmStatic
    fun lerpInvSat(a: Float, b: Float, value: Float): Float {
        return saturate(lerpInv(a, b, value))
    }

    @JvmStatic
    fun norm(start: Float, stop: Float, value: Float): Float {
        return (value - start) / (stop - start)
    }

    private fun sq(f: Float): Float {
        return f * f
    }

    @JvmStatic
    fun exp(f: Float): Float {
        return kotlin.math.exp(f.toDouble()).toFloat()
    }

    @JvmStatic
    fun convertGammaToLinearFloat(i: Float, max: Int, f: Float, f2: Float): Float {
        val norm = norm(0.0f, max.toFloat(), i)
        val R = 0.4f
        val A = 0.2146f
        val B = 0.2847f
        val C = 0.4719f
        val value = if (norm <= R) sq(norm / R) else exp((norm - C) / A) + B
        return lerp(f, f2, constrain(value, 0.0f, 12.0f) / 12.0f)
    }

    private val resIdCache = ConcurrentHashMap<String, Int>()

    @JvmStatic
    fun getResId(res: Resources?, name: String?, defType: String?, defPackage: String?): Int {
        if (res == null || name == null || defType == null || defPackage == null) return 0
        val key = "$defPackage:$defType/$name"
        val cached = resIdCache[key]
        if (cached != null) return cached
        val id = res.getIdentifier(name, defType, defPackage)
        resIdCache[key] = id
        return id
    }
}
