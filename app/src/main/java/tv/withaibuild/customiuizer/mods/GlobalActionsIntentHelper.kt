package tv.withaibuild.customiuizer.mods

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import tv.withaibuild.customiuizer.MainModule

object GlobalActionsIntentHelper {

    enum class IntentType {
        APP, ACTIVITY, SHORTCUT
    }

    @JvmStatic
    fun getIntent(context: Context, pref: String, intentType: IntentType, skipLock: Boolean): Intent? {
        return try {
            val key = pref + when (intentType) {
                IntentType.APP -> "_app"
                IntentType.ACTIVITY -> "_activity"
                IntentType.SHORTCUT -> "_shortcut_intent"
            }

            val prefValue = MainModule.mPrefs.getString(key, "")
            if (prefValue.isEmpty()) return null

            val intent = if (intentType == IntentType.SHORTCUT) {
                Intent.parseUri(prefValue, 0)
            } else {
                val pkgAppArray = prefValue.split("\\|".toRegex())
                if (pkgAppArray.size < 2) return null
                Intent().apply {
                    component = ComponentName(pkgAppArray[0], pkgAppArray[1])
                    val user = MainModule.mPrefs.getInt(key + "_user", 0)
                    if (user != 0) putExtra("user", user)
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

            if (intentType == IntentType.APP) {
                intent.action = Intent.ACTION_MAIN
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
            }

            if (skipLock) {
                intent.addFlags(335544320)
                intent.putExtra("StartActivityWhenLocked", true)
            }

            intent
        } catch (t: Throwable) {
            tv.withaibuild.customiuizer.mods.utils.XposedHelpers.log(t)
            null
        }
    }

    @JvmStatic
    fun launchAppIntent(context: Context, key: String, skipLock: Boolean): Boolean {
        return launchIntent(context, getIntent(context, key, IntentType.APP, skipLock))
    }

    @JvmStatic
    fun launchActivityIntent(context: Context, key: String, skipLock: Boolean): Boolean {
        return launchIntent(context, getIntent(context, key, IntentType.ACTIVITY, skipLock))
    }

    @JvmStatic
    fun launchShortcutIntent(context: Context, key: String, skipLock: Boolean): Boolean {
        return launchIntent(context, getIntent(context, key, IntentType.SHORTCUT, skipLock))
    }

    @JvmStatic
    fun launchIntent(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false
        val bIntent = Intent(GlobalActions.ACTION_PREFIX + "LaunchIntent")
        bIntent.putExtra("intent", intent)
        context.sendBroadcast(bIntent)
        return true
    }
}
