package tv.withaibuild.customiuizer.subs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import tv.withaibuild.customiuizer.utils.PreferenceAdapter
import tv.withaibuild.customiuizer.utils.SortableListView
import java.util.UUID
import java.util.Locale

class SortableList : SubFragment() {

    private var key: String? = null
    private var titleResId: String? = null
    private var activities = false
    private var listView: SortableListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        this.padded = false
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        toolbarMenu = true
        super.onActivityCreated(savedInstanceState)

        val args = arguments
        key = args?.getString("key")
        titleResId = args?.getString("titleResId")
        activities = args?.getBoolean("activities", false) ?: false

        val v = view ?: return
        val ctx = context ?: return
        val mKey = key ?: return

        listView = v.findViewById(android.R.id.list)

        if (!activities) try {
            val ssField = SortableListView::class.java.getDeclaredField("mSnapshotShadow")
            ssField.isAccessible = true
            val lightShadow = ctx.resources.getIdentifier("dynamic_listview_dragging_item_shadow_light", "drawable", "miui")
            val darkShadow = ctx.resources.getIdentifier("dynamic_listview_dragging_item_shadow_dark", "drawable", "miui")
            val drawable = ctx.resources.getDrawable(if (Helpers.isNightMode(ctx)) darkShadow else lightShadow, ctx.theme)
            ssField.set(listView, drawable)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        listView?.adapter = PreferenceAdapter(ctx, mKey, activities)
        if (activities) {
            listView?.setOnOrderChangedListener(null)
        } else {
            listView?.setOnOrderChangedListener(object : SortableListView.OnOrderChangedListener {
                override fun OnOrderChanged(oldPos: Int, newPos: Int) {
                    if (oldPos == newPos) return
                    val itemStr = AppHelper.getStringOfAppPrefs(mKey, "") ?: return
                    if (itemStr.isEmpty()) return
                    val itemList = itemStr.trim().split("|").toMutableList()
                    val uuid = itemList.removeAt(oldPos)
                    itemList.add(newPos, uuid)
                    AppHelper.appPrefs.edit().putString(mKey, itemList.joinToString("|")).apply()
                    (listView?.adapter as? PreferenceAdapter)?.run {
                        updateItems()
                        notifyDataSetChanged()
                    }
                }
            })
        }

        if (!activities) {
            listView?.setOnItemClickListener { parent, _, position, _ ->
                val uuid = (parent.adapter as? PreferenceAdapter)?.getItem(position) ?: return@setOnItemClickListener
                val itemArgs = Bundle().apply {
                    putString("key", mKey + "_" + uuid)
                    putInt("actions", MultiAction.Actions.LOCKSCREEN.ordinal)
                }
                openSubFragment(MultiAction(), itemArgs, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.Edit, titleResId, R.layout.prefs_multiaction)
            }
        }

        listView?.setOnItemLongClickListener { _, _, position, _ ->
            deleteItem(position)
            true
        }
    }

    private fun createNewUUID(): String =
        UUID.randomUUID().toString().replace("-", "").lowercase(Locale.ROOT)

    private fun createNewItem(uuid: String) {
        val mKey = key ?: return
        val items = AppHelper.getStringOfAppPrefs(mKey, "") ?: ""
        AppHelper.appPrefs.edit().putString(mKey, if (items.isEmpty()) uuid else "$items|$uuid").apply()
        (listView?.adapter as? PreferenceAdapter)?.run {
            updateItems()
            notifyDataSetChanged()
        }
    }

    private fun deleteItem(position: Int) {
        val mKey = key ?: return
        val adapter = listView?.adapter as? PreferenceAdapter ?: return
        val items = AppHelper.getStringOfAppPrefs(mKey, "") ?: ""
        if (items.isEmpty()) return
        val itemList = items.split("|").toMutableList().apply { removeAt(position) }
        AppHelper.appPrefs.edit().putString(mKey, itemList.joinToString("|")).apply()
        adapter.updateItems()
        adapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val mKey = key
        return when (item.itemId) {
            R.id.deleteitem -> {
                Toast.makeText(context, R.string.delete_item_info, Toast.LENGTH_SHORT).show()
                true
            }
            R.id.additem -> {
                if (activities) {
                    val args = Bundle().apply {
                        putBoolean("activity", true)
                        putString("key", mKey)
                    }
                    val activitySelect = AppSelector().apply { setTargetFragment(this@SortableList, 2) }
                    openSubFragment(activitySelect, args, AppHelper.SettingsType.Edit, AppHelper.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector)
                } else {
                    createNewItem(createNewUUID())
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 2 && data != null) {
            val mKey = key ?: return
            val activityValue = data.getStringExtra("activity") ?: return
            val activityUser = data.getIntExtra("user", 0)
            if (activityUser < 0) return

            val uuid = createNewUUID()
            AppHelper.appPrefs.edit()
                .putInt(mKey + "_" + uuid + "_action", 20)
                .putString(mKey + "_" + uuid + "_activity", activityValue)
                .putInt(mKey + "_" + uuid + "_activity_user", activityUser)
                .apply()
            createNewItem(uuid)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
