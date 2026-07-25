package tv.withaibuild.customiuizer.subs

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope

import java.util.ArrayList

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

class ActivitySelector : SubFragmentWithSearch() {

    private var pkg: String? = null
    private var key: String? = null
    private var user = 0
    private val activities = ArrayList<AppData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        this.padded = false
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val args = arguments
        key = args?.getString("key")
        pkg = args?.getString("package")
        user = args?.getInt("user", 0) ?: 0

        lifecycleScope.launch(Dispatchers.IO) {
            delay(animDur.toLong())
            loadActivities()
            withContext(Dispatchers.Main) {
                if (isAdded) setupList()
            }
        }
    }

    private suspend fun loadActivities() {
        activities.clear()
        val context = context ?: return
        val pm = context.packageManager
        try {
            val pi = pm.getPackageInfo(pkg ?: return, PackageManager.GET_ACTIVITIES)
            if (pi.activities != null) {
                for (info in pi.activities) {
                    val appData = AppData()
                    appData.pkgName = pkg
                    appData.actName = info.name ?: ""
                    appData.label = info.loadLabel(pm).toString()
                    appData.enabled = info.enabled
                    activities.add(appData)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setupList() {
        val act = activity
        if (act == null || !isAdded) return
        if (activities.isEmpty()) {
            Toast.makeText(act, R.string.no_activities_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        listView?.adapter = AppDataAdapter(act.applicationContext, activities, AppHelper.AppAdapterType.Activities, null)
        listView?.setOnItemClickListener { parent: AdapterView<*>, _, position: Int, _ ->
            val appData = (parent.adapter?.getItem(position) as? AppData) ?: return@setOnItemClickListener
            val intent = Intent()
            intent.putExtra("activity", appData.pkgName + "|" + appData.actName)
            intent.putExtra("user", user)
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            finish()
        }
        listView?.setOnItemLongClickListener { parent: AdapterView<*>, _, position: Int, _ ->
            val appData = (parent.adapter?.getItem(position) as? AppData) ?: return@setOnItemLongClickListener true
            val intent = Intent()
            intent.setComponent(ComponentName(appData.pkgName, appData.actName))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            intent.putExtra("user", user)
            val bIntent = Intent(GlobalActions.ACTION_PREFIX + "LaunchIntent")
            bIntent.putExtra("intent", intent)
            activity?.sendBroadcast(bIntent)
            true
        }
        view?.findViewById<View>(R.id.am_progressBar)?.visibility = View.GONE
    }
}
