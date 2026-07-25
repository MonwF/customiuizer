package tv.withaibuild.customiuizer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.recyclerview.widget.RecyclerView

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import tv.withaibuild.customiuizer.subs.CategorySelector
import tv.withaibuild.customiuizer.subs.Controls
import tv.withaibuild.customiuizer.subs.Launcher
import tv.withaibuild.customiuizer.subs.System as SubSystem
import tv.withaibuild.customiuizer.subs.Various
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import tv.withaibuild.customiuizer.utils.ModData
import tv.withaibuild.customiuizer.utils.ModSearchAdapter

class MainFragment : PreferenceFragmentBase() {

    private val catSelector = CategorySelector()

    @JvmField
    var prefSystem = SubSystem()

    @JvmField
    var prefLauncher = Launcher()

    @JvmField
    var prefControls = Controls()

    @JvmField
    var prefVarious = Various()

    private var mActionMenu: Menu? = null
    private var listView: RecyclerView? = null
    private var resultView: ListView? = null

    private var isSearchFocused = false
    private var inSearchView = 0
    private var lastFilter: String? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun isFragmentReady(act: AppCompatActivity?): Boolean {
        return act != null && !act.isFinishing && isAdded
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        toolbarMenu = true
        activeMenus = "all"
        super.onCreate(savedInstanceState, R.xml.prefs_main)
        tailLayoutId = R.layout.prefs_main12

        val act = activity as? AppCompatActivity ?: return

        scope.launch(Dispatchers.IO) {
            Helpers.getAllMods(act, savedInstanceState != null)
        }

        checkModuleIsActive()
    }

    private fun checkModuleIsActive() {
        scope.launch {
            delay(800L)
            if (!isActive) return@launch
            val act = activity as? AppCompatActivity ?: return@launch
            if (isFragmentReady(act) && !AppHelper.moduleActive) {
                showXposedDialog(act)
            }
        }
    }

    override fun onCreatePreferences(@Nullable savedInstanceState: Bundle?, @Nullable rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.prefs_main, rootKey)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        mActionMenu = menu
        val searchMenuItem = mActionMenu?.findItem(R.id.search_btn) ?: return

        val searchView = MenuItemCompat.getActionView(searchMenuItem) as? SearchView ?: return
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                for (i in 0 until (mActionMenu?.size() ?: 0)) {
                    val menuItem = mActionMenu?.getItem(i) ?: continue
                    menuItem.isVisible = menuItem.itemId != R.id.edit_confirm
                }
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                for (i in 0 until (mActionMenu?.size() ?: 0)) {
                    val menuItem = mActionMenu?.getItem(i) ?: continue
                    menuItem.isVisible = menuItem.itemId == R.id.search_btn
                }
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    inSearchView = 1
                }
                findMod(newText ?: "")
                return false
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            isSearchFocused = hasFocus
        }

        if (inSearchView == 2) {
            MenuItemCompat.expandActionView(searchMenuItem)
            searchView.setQuery(lastFilter, false)
            searchView.clearFocus()
        }
    }

    override fun fixStubLayout(view: View, postion: Int) {
        if (postion == 2) {
            val lp = view.layoutParams
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = lp
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val actionBar: ActionBar? = getActionBar()
        actionBar?.setTitle(R.string.app_name)

        val view = this.view ?: return

        resultView = view.findViewById(R.id.custom)
        resultView?.setDivider(null)
        resultView?.setDividerHeight(0)
        resultView?.adapter = ModSearchAdapter(requireActivity())
        resultView?.setOnItemClickListener { parent: AdapterView<*>, _, position: Int, _ ->
            inSearchView = 2
            val mod = parent.adapter?.getItem(position) as? ModData
            if (mod != null) {
                openModCat(mod.cat?.name ?: return@setOnItemClickListener, mod.sub, mod.key)
            }
        }
        resultView?.setOnTouchListener { _, event: MotionEvent ->
            if (isSearchFocused) {
                isSearchFocused = false
                scope.launch {
                    delay(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
                    Helpers.hideKeyboard(activity as? AppCompatActivity, this@MainFragment.view)
                    resultView?.requestFocus()
                }
            }
            false
        }

        listView = getListView()

        findPreference<Preference>("pref_key_miuizer_launchericon")?.setOnPreferenceChangeListener { _, newValue ->
            val act = activity as? AppCompatActivity ?: return@setOnPreferenceChangeListener false
            val pm = act.packageManager
            val component = ComponentName(act, GateWayLauncher::class.java)
            if (newValue == true) {
                pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            } else {
                pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            }
            true
        }
    }

    private fun findMod(filter: String) {
        if (inSearchView == 2) return
        lastFilter = filter
        resultView?.visibility = if (filter == "") View.GONE else View.VISIBLE
        listView?.isEnabled = filter == ""
        val adapter = resultView?.adapter ?: return
        (adapter as ModSearchAdapter).filter.filter(filter)
    }

    private fun openModCat(cat: String, sub: String?, mod: String): Boolean {
        val bundle = Bundle().apply {
            putString("cat", cat)
            if (sub != null) putString("sub", sub)
            putString("mod", mod)
        }
        catSelector.setTargetFragment(this, 0)
        return when (cat) {
            "pref_key_system" -> {
                if (sub == null) {
                    openSubFragment(catSelector, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_mods, R.xml.prefs_system_cat)
                } else {
                    openSubFragment(prefSystem, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.system_mods, R.xml.prefs_system)
                }
                false
            }
            "pref_key_launcher" -> {
                if (sub == null) {
                    openSubFragment(catSelector, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.launcher_title, R.xml.prefs_launcher_cat)
                } else {
                    openSubFragment(prefLauncher, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.launcher_title, R.xml.prefs_launcher)
                }
                true
            }
            "pref_key_controls" -> {
                if (sub == null) {
                    openSubFragment(catSelector, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.controls_mods, R.xml.prefs_controls_cat)
                } else {
                    openSubFragment(prefControls, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.controls_mods, R.xml.prefs_controls)
                }
                false
            }
            "pref_key_various" -> {
                openSubFragment(prefVarious, bundle, AppHelper.SettingsType.Preference, AppHelper.ActionBarType.HomeUp, R.string.various_mods, R.xml.prefs_various)
                false
            }
            else -> false
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val key = preference.key ?: return super.onPreferenceTreeClick(preference)
        val modsCat = findPreference<PreferenceCategory>("prefs_cat")
        return if (modsCat?.findPreference<Preference>(key) != null && openModCat(key, null, key)) {
            true
        } else {
            super.onPreferenceTreeClick(preference)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
