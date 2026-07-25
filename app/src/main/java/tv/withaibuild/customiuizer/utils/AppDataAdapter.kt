package tv.withaibuild.customiuizer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.TransitionDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import tv.withaibuild.customiuizer.R
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

class AppDataAdapter(
    private val ctx: Context,
    arr: ArrayList<AppData>,
    private val aType: AppHelper.AppAdapterType = AppHelper.AppAdapterType.Default,
    private val key: String? = null,
    private val bwlist: Boolean = false
) : BaseAdapter(), Filterable {

    private val inflater: LayoutInflater = LayoutInflater.from(ctx)
    private val filter = ItemFilter()
    private val originalAppList = ArrayList<AppData>(arr)
    private val filteredAppList = CopyOnWriteArrayList<AppData>(arr)
    private var selectedApp = ""
    private var selectedUser = 0
    private var selectedApps: MutableSet<String> = linkedSetOf()
    private var selectedAppsBlack: MutableSet<String> = linkedSetOf()
    private var multiUserSupport = false

    init {
        when (aType) {
            AppHelper.AppAdapterType.Mutli -> {
                selectedApps = AppHelper.getStringSetOfAppPrefs(key, linkedSetOf())
                if (bwlist) selectedAppsBlack = AppHelper.getStringSetOfAppPrefs(key + "_black", linkedSetOf())
                multiUserSupport = key in arrayOf("pref_key_system_cleanshare_apps", "pref_key_system_cleanopenwith_apps")
                if (multiUserSupport) {
                    val toAdd = HashSet<String>()
                    val iter = selectedApps.iterator()
                    while (iter.hasNext()) {
                        val item = iter.next()
                        if (!item.contains("|")) {
                            toAdd.add("$item|0")
                            iter.remove()
                        }
                    }
                    selectedApps.addAll(toAdd)
                } else {
                    removeDualUsers()
                }
            }
            AppHelper.AppAdapterType.Standalone -> {
                selectedApp = AppHelper.getStringOfAppPrefs(key, "") ?: ""
                selectedUser = AppHelper.getIntOfAppPrefs(key + "_user", 0)
                AppData().apply {
                    pkgName = ""
                    actName = ""
                    label = ctx.getString(R.string.array_default)
                    enabled = true
                }.let {
                    originalAppList.add(0, it)
                    filteredAppList.add(0, it)
                }
            }
            AppHelper.AppAdapterType.Default -> {
                if (key?.contains("pref_key_system_applock_skip_activities") == true) removeDualUsers()
            }
            else -> {}
        }
        sortList()
    }

    private fun removeDualUsers() {
        originalAppList.removeAll { it.user != 0 }
        filteredAppList.clear()
        filteredAppList.addAll(originalAppList)
    }

    fun updateSelectedApps() {
        if (aType == AppHelper.AppAdapterType.Mutli) {
            selectedApps = AppHelper.getStringSetOfAppPrefs(key, linkedSetOf())
            if (bwlist) selectedAppsBlack = AppHelper.getStringSetOfAppPrefs(key + "_black", linkedSetOf())
        } else if (aType == AppHelper.AppAdapterType.Standalone) {
            selectedApp = AppHelper.getStringOfAppPrefs(key, "") ?: ""
            selectedUser = AppHelper.getIntOfAppPrefs(key + "_user", 0)
        }
        notifyDataSetChanged()
    }

    private fun shouldSelect(pkgName: String, user: Int): Boolean {
        return if (!multiUserSupport) {
            selectedApps.contains(pkgName) || selectedApps.contains("$pkgName|0")
        } else {
            selectedApps.contains("$pkgName|$user")
        }
    }

    private fun shouldSelectBW(pkgName: String): Boolean {
        return selectedApps.contains(pkgName) || selectedAppsBlack.contains(pkgName)
    }

    private fun sortList() {
        filteredAppList.sortWith { app1, app2 ->
            when (aType) {
                AppHelper.AppAdapterType.Mutli -> {
                    if (selectedApps.isEmpty() && selectedAppsBlack.isEmpty()) return@sortWith 0
                    val app1checked = if (bwlist) shouldSelectBW(app1.pkgName) else shouldSelect(app1.pkgName, app1.user)
                    val app2checked = if (bwlist) shouldSelectBW(app2.pkgName) else shouldSelect(app2.pkgName, app2.user)
                    when {
                        app1checked && app2checked -> 0
                        app1checked -> -1
                        app2checked -> 1
                        else -> 0
                    }
                }
                AppHelper.AppAdapterType.Standalone -> {
                    if (app1.pkgName == "" && app1.actName == "") return@sortWith -1
                    if (app2.pkgName == "" && app2.actName == "") return@sortWith 1
                    val app1checked = selectedApp == "${app1.pkgName}|${app1.actName}" && selectedUser == app1.user
                    val app2checked = selectedApp == "${app2.pkgName}|${app2.actName}" && selectedUser == app2.user
                    when {
                        app1checked && app2checked -> 0
                        app1checked -> -1
                        app2checked -> 1
                        else -> 0
                    }
                }
                AppHelper.AppAdapterType.Activities -> {
                    app1.actName.lowercase(Locale.ROOT).compareTo(app2.actName.lowercase(Locale.ROOT))
                }
                else -> 0
            }
        }
    }

    override fun getCount(): Int = filteredAppList.size

    override fun getItem(position: Int): AppData = filteredAppList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder = (convertView?.tag as? ViewHolder) ?: run {
            val row = inflater.inflate(R.layout.applist_item11, parent, false)
            ViewHolder(row).also { row.tag = it }
        }

        if (!bwlist && holder.checked.tag != true) {
            holder.checked.tag = true
        }

        val ad = getItem(position)
        holder.title.text = ad.label
        holder.disableIcon.visibility = if (ad.enabled) View.GONE else View.VISIBLE

        if (aType == AppHelper.AppAdapterType.Activities) {
            holder.icon.visibility = View.GONE
            (holder.container.layoutParams as? LinearLayout.LayoutParams)?.let {
                it.leftMargin = 0
                holder.container.layoutParams = it
            }
        } else {
            holder.icon.tag = position
            val icon = Helpers.memoryCache[ad.pkgName + "|" + ad.actName]
            if (icon == null) {
                val dualIcon = arrayOf(ctx.resources.getDrawable(R.drawable.card_icon_default, ctx.theme))
                val crossfader = TransitionDrawable(dualIcon)
                crossfader.setCrossFadeEnabled(true)
                holder.icon.setImageDrawable(crossfader)
                BitmapCachedLoader(holder.icon, ad, ctx).execute()
            } else {
                holder.icon.setImageBitmap(icon)
            }
        }

        when (aType) {
            AppHelper.AppAdapterType.Mutli -> {
                holder.summary.visibility = View.GONE
                if (bwlist) {
                    holder.stateIcon.visibility = View.VISIBLE
                    holder.stateIcon.setImageResource(
                        when {
                            selectedApps.contains(ad.pkgName) -> R.drawable.icon_action_allow
                            selectedAppsBlack.contains(ad.pkgName) -> R.drawable.icon_action_disallow
                            else -> R.drawable.icon_action_default
                        }
                    )
                } else {
                    holder.checked.visibility = View.VISIBLE
                    holder.checked.isChecked = shouldSelect(ad.pkgName, ad.user)
                }
                holder.dualIcon.visibility = if (ad.user != 0) View.VISIBLE else View.GONE
            }
            AppHelper.AppAdapterType.CustomTitles -> {
                holder.summary.text = AppHelper.getStringOfAppPrefs(key + ":" + ad.pkgName + "|" + ad.actName + "|" + ad.user, "") ?: ""
                holder.summary.visibility = if (TextUtils.isEmpty(holder.summary.text)) View.GONE else View.VISIBLE
                holder.dualIcon.visibility = if (ad.user != 0) View.VISIBLE else View.GONE
            }
            AppHelper.AppAdapterType.Standalone -> {
                holder.checked.visibility = View.VISIBLE
                holder.checked.isChecked = (selectedApp == "" && ad.pkgName == "" && ad.actName == "") ||
                        ((ad.pkgName + "|" + ad.actName) == selectedApp && ad.user == selectedUser)
                holder.dualIcon.visibility = if (ad.user != 0) View.VISIBLE else View.GONE
            }
            AppHelper.AppAdapterType.Activities -> {
                holder.summary.text = ad.actName.replace(".", ".\u200B")
                holder.summary.visibility = if (TextUtils.isEmpty(holder.summary.text)) View.GONE else View.VISIBLE
                holder.summary.setSingleLine(false)
                holder.summary.maxLines = Integer.MAX_VALUE
                holder.summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                holder.dualIcon.visibility = if (ad.user != 0) View.VISIBLE else View.GONE
            }
            else -> {
                holder.summary.visibility = View.GONE
                holder.dualIcon.visibility = if (ad.user != 0) View.VISIBLE else View.GONE
            }
        }

        return holder.root
    }

    private class ViewHolder(val root: View) {
        val disableIcon: ImageView = root.findViewById(R.id.icon_disable)
        val dualIcon: ImageView = root.findViewById(R.id.icon_dual)
        val checked: CheckBox = root.findViewById(android.R.id.checkbox)
        val stateIcon: ImageView = root.findViewById(android.R.id.selectedIcon)
        val title: TextView = root.findViewById(android.R.id.title)
        val summary: TextView = root.findViewById(android.R.id.summary)
        val icon: ImageView = root.findViewById(android.R.id.icon)
        val container: View = root.findViewById(R.id.container)
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterString = constraint?.toString()?.lowercase(Locale.ROOT) ?: ""
            val results = FilterResults()
            val nlist = ArrayList<AppData>()

            for (app in originalAppList) {
                if (aType == AppHelper.AppAdapterType.Activities && app.actName.lowercase(Locale.ROOT).contains(filterString)) {
                    nlist.add(app)
                } else if ((aType == AppHelper.AppAdapterType.Standalone && app.pkgName == "" && app.actName == "") ||
                    app.label.lowercase(Locale.ROOT).contains(filterString)) {
                    nlist.add(app)
                }
            }

            results.values = nlist
            results.count = nlist.size
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredAppList.clear()
            if (results != null && results.count > 0 && results.values != null) {
                filteredAppList.addAll(results.values as ArrayList<AppData>)
            }
            sortList()
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter = filter
}
