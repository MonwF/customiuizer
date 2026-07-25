package tv.withaibuild.customiuizer.subs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.prefs.SpinnerEx
import tv.withaibuild.customiuizer.prefs.SpinnerExFake
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import java.io.File

@SuppressLint("ClickableViewAccessibility")
class MultiAction : SubFragment() {

    private var appLaunch: SpinnerExFake? = null
    private var shortcutLaunch: SpinnerExFake? = null
    private var activityLaunch: SpinnerExFake? = null
    private var key: String? = null
    private var appValue: String? = null
    private var appUser = -1
    private var activityValue: String? = null
    private var activityUser = -1
    private var shortcutValue: String? = null
    private var shortcutName: String? = null
    private var shortcutIcon: String? = null
    private var shortcutIconPath: String? = null
    private var shortcutIntent: Intent? = null

    enum class Actions {
        NAVBAR, LAUNCHER, CONTROLS, LOCKSCREEN, LAUNCH, STATUSBAR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        padded = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val v = view ?: return
        val ctx = context ?: return
        val mKey = arguments?.getString("key") ?: return
        key = mKey
        val actions = Actions.values()[arguments?.getInt("actions") ?: 0]

        val (entriesResId, entryValuesResId) = when (actions) {
            Actions.NAVBAR -> R.array.global_actions_navbar to R.array.global_actions_navbar_val
            Actions.LAUNCHER -> R.array.global_actions_launcher to R.array.global_actions_launcher_val
            Actions.CONTROLS -> R.array.global_actions_controls to R.array.global_actions_controls_val
            Actions.STATUSBAR -> R.array.global_actions_statusbar to R.array.global_actions_statusbar_val
            Actions.LOCKSCREEN -> R.array.global_lockscreen_actions to R.array.global_lockscreen_actions_val
            Actions.LAUNCH -> R.array.global_launch_actions to R.array.global_launch_actions_val
        }

        val actionSpinner = v.findViewById<SpinnerEx>(R.id.action)
        actionSpinner.entries = resources.getStringArray(entriesResId)
        actionSpinner.entryValues = resources.getIntArray(entryValuesResId)
        actionSpinner.tag = mKey + "_action"
        actionSpinner.init(AppHelper.getIntOfAppPrefs(mKey + "_action", 1))
        actionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateControls(parent as SpinnerEx, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                updateControls(parent as SpinnerEx, 0)
            }
        }

        appLaunch = v.findViewById(R.id.app_to_launch)
        appLaunch?.apply {
            tag = mKey + "_app"
            setValue(appValue ?: AppHelper.getStringOfAppPrefs(mKey + "_app", null))
            setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onChildViewAdded(parent: View, child: View) {
                    if (child !is TextView || child.id != android.R.id.text1) return
                    val pkgAppName = value
                    if (pkgAppName != null) {
                        val label = Helpers.getAppName(ctx, pkgAppName)
                        if (label != null) {
                            val user = if (appUser != -1) appUser else AppHelper.getIntOfAppPrefs(mKey + "_app_user", 0)
                            child.text = "$label ${if (user != 0) " *" else ""}"
                            return
                        }
                    }
                    child.setText(R.string.notselected)
                    child.alpha = 0.5f
                }

                override fun onChildViewRemoved(parent: View, child: View) {}
            })
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    AppSelector().also { it.setTargetFragment(this@MultiAction, 0) }.let {
                        openSubFragment(it, null, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector)
                    }
                }
                false
            }
        }

        shortcutLaunch = v.findViewById(R.id.shortcut_to_launch)
        shortcutLaunch?.apply {
            tag = mKey + "_shortcut"
            setValue(shortcutValue ?: AppHelper.getStringOfAppPrefs(mKey + "_shortcut", null))
            shortcutIntent?.let { addValue(mKey + "_shortcut_intent", it) }
            shortcutName?.let { addValue(mKey + "_shortcut_name", it) }
        }

        shortcutIconPath = ctx.filesDir.path + "/shortcuts/" + mKey + "_shortcut.png"
        val shortcutIconFile = shortcutIcon?.let { File(it) } ?: File(shortcutIconPath)
        if (shortcutIconFile.exists()) {
            val sIcon = v.findViewById<ImageView>(R.id.shortcut_icon)
            BitmapFactory.decodeFile(shortcutIconFile.absolutePath)?.let { sIcon.setImageBitmap(it) }
        }

        shortcutLaunch?.apply {
            setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View, child: View) {
                    if (child !is TextView || child.id != android.R.id.text1) return
                    val pkgAppName = value
                    if (pkgAppName != null) {
                        val label = Helpers.getAppName(ctx, pkgAppName)
                        if (label != null) {
                            child.text = label
                            return
                        }
                    }
                    child.setText(R.string.notselected)
                    child.alpha = 0.5f
                }

                override fun onChildViewRemoved(parent: View, child: View) {}
            })
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val args = Bundle().apply { putString("key", mKey + "_shortcut") }
                    ShortcutSelector().also { it.setTargetFragment(this@MultiAction, 1) }.let {
                        openSubFragment(it, args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.select_shortcut, R.layout.prefs_app_selector)
                    }
                }
                false
            }
        }

        val toggleSpinner = v.findViewById<SpinnerEx>(R.id.toggle)
        toggleSpinner.tag = mKey + "_toggle"
        toggleSpinner.init(AppHelper.getIntOfAppPrefs(mKey + "_toggle", 1))

        activityLaunch = v.findViewById(R.id.activity_to_launch)
        activityLaunch?.apply {
            tag = mKey + "_activity"
            setValue(activityValue ?: AppHelper.getStringOfAppPrefs(mKey + "_activity", null))
            value?.takeIf { it.isNotEmpty() }?.replace("|", "/\u200B")?.replace(".", ".\u200B").let {
                v.findViewById<TextView>(R.id.activity_class).text = it ?: ""
            }
            setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onChildViewAdded(parent: View, child: View) {
                    if (child !is TextView || child.id != android.R.id.text1) return
                    val pkgAppName = value
                    if (pkgAppName != null) {
                        val label = Helpers.getAppName(ctx, pkgAppName, true)
                        if (label != null) {
                            val user = if (activityUser != -1) activityUser else AppHelper.getIntOfAppPrefs(mKey + "_activity_user", 0)
                            child.text = "$label ${if (user != 0) " *" else ""}"
                            return
                        }
                    }
                    child.setText(R.string.notselected)
                    child.alpha = 0.5f
                }

                override fun onChildViewRemoved(parent: View, child: View) {}
            })
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val args = Bundle().apply { putBoolean("activity", true) }
                    AppSelector().also { it.setTargetFragment(this@MultiAction, 2) }.let {
                        openSubFragment(it, args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector)
                    }
                }
                false
            }
        }
    }

    private fun updateControls(spinner: SpinnerEx, position: Int) {
        val v = view ?: return
        val apps = v.findViewById<View>(R.id.apps_group)
        val shortcuts = v.findViewById<View>(R.id.shortcuts_group)
        val activities = v.findViewById<View>(R.id.activities_group)
        val toggles = v.findViewById<View>(R.id.toggles_group)

        apps.visibility = View.GONE
        shortcuts.visibility = View.GONE
        activities.visibility = View.GONE
        toggles.visibility = View.GONE

        when (spinner.entryValues[position]) {
            8 -> apps.visibility = View.VISIBLE
            9 -> shortcuts.visibility = View.VISIBLE
            10 -> toggles.visibility = View.VISIBLE
            20 -> activities.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                0 -> {
                    appValue = data.getStringExtra("app")
                    appUser = data.getIntExtra("user", 0)
                }
                1 -> {
                    shortcutValue = data.getStringExtra("shortcut_contents")
                    shortcutName = data.getStringExtra("shortcut_name")
                    shortcutIcon = data.getStringExtra("shortcut_icon")
                    shortcutIntent = data.getParcelableExtra("shortcut_intent")
                }
                2 -> {
                    activityValue = data.getStringExtra("activity")
                    activityUser = data.getIntExtra("user", 0)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun saveSharedPrefs() {
        context?.let { ctx ->
            val tmpIconFile = File(ctx.filesDir.path + "/shortcuts/tmp.png")
            if (tmpIconFile.exists()) {
                val prefIconFile = File(shortcutIconPath)
                prefIconFile.delete()
                tmpIconFile.renameTo(prefIconFile)
            }
        }
        val mKey = key
        if (mKey != null) {
            if (appUser != -1) AppHelper.appPrefs.edit().putInt(mKey + "_app_user", appUser).apply()
            if (activityUser != -1) AppHelper.appPrefs.edit().putInt(mKey + "_activity_user", activityUser).apply()
        }
        super.saveSharedPrefs()
    }

    override fun onDestroy() {
        context?.let { ctx ->
            val tmpIconFile = File(ctx.filesDir.path + "/shortcuts/tmp.png")
            if (tmpIconFile.exists()) tmpIconFile.delete()
        }
        super.onDestroy()
    }
}
