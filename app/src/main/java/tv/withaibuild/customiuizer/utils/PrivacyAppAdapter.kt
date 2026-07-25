package tv.withaibuild.customiuizer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import tv.withaibuild.customiuizer.R
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

@SuppressLint("WrongConstant")
class PrivacyAppAdapter(
    context: Context,
    arr: ArrayList<AppData>,
    private val privacyAppsMap: HashMap<Int, ArrayList<String>>
) : BaseAdapter(), Filterable {

    private val ctx: Context = context
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val filter = ItemFilter()
    private val originalAppList = arr
    private val filteredAppList = CopyOnWriteArrayList(arr).apply { addAll(arr) }

    init {
        sortList()
    }

    private fun sortList() {
        filteredAppList.sortWith { app1, app2 ->
            try {
                val app1checked = isPrivacyApp(app1.pkgName, app1.user)
                val app2checked = isPrivacyApp(app2.pkgName, app2.user)
                when {
                    app1checked && app2checked -> 0
                    app1checked -> -1
                    app2checked -> 1
                    else -> 0
                }
            } catch (_: Throwable) {
                0
            }
        }
    }

    override fun getCount(): Int = filteredAppList.size

    override fun getItem(position: Int): AppData = filteredAppList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    private fun isPrivacyApp(pkgName: String, user: Int): Boolean {
        return privacyAppsMap[user]?.contains(pkgName) ?: false
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = convertView ?: inflater.inflate(R.layout.applist_item11, parent, false)

        val itemIsDis = row.findViewById<ImageView>(R.id.icon_disable)
        val itemIsDual = row.findViewById<ImageView>(R.id.icon_dual)
        val itemChecked = row.findViewById<CheckBox>(android.R.id.checkbox)
        val itemTitle = row.findViewById<TextView>(android.R.id.title)
        val itemIcon = row.findViewById<ImageView>(android.R.id.icon)

        val ad = getItem(position)
        itemIcon.tag = position
        itemTitle.text = ad.label
        itemIsDis.visibility = if (ad.enabled) View.GONE else View.VISIBLE
        itemIsDual.visibility = if (ad.user != 0) View.VISIBLE else View.GONE

        val icon = Helpers.memoryCache[ad.pkgName + "|" + ad.actName]
        if (icon == null) {
            val dualIcon = arrayOf(ctx.resources.getDrawable(R.drawable.card_icon_default, ctx.theme))
            val crossfader = TransitionDrawable(dualIcon)
            crossfader.setCrossFadeEnabled(true)
            itemIcon.setImageDrawable(crossfader)
            BitmapCachedLoader(itemIcon, ad, ctx).execute()
        } else {
            itemIcon.setImageBitmap(icon)
        }

        try {
            itemChecked.visibility = View.VISIBLE
            itemChecked.isChecked = isPrivacyApp(ad.pkgName, ad.user)
        } catch (_: Throwable) {
            itemChecked.visibility = View.GONE
        }

        return row
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterString = constraint?.toString()?.lowercase(Locale.ROOT) ?: ""
            val results = FilterResults()
            val nlist = ArrayList<AppData>()

            for (app in originalAppList) {
                if (app.label.lowercase(Locale.ROOT).contains(filterString)) {
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
