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
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

@SuppressLint("WrongConstant")
class LockedAppAdapter(context: Context, arr: ArrayList<AppData>) : BaseAdapter(), Filterable {

    private val ctx: Context = context
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val filter = ItemFilter()
    private val originalAppList = arr
    private val filteredAppList = CopyOnWriteArrayList(arr).apply { addAll(arr) }
    private var mSecurityManager: Any? = null
    private var getApplicationAccessControlEnabledAsUser: Method? = null

    init {
        try {
            mSecurityManager = context.getSystemService("security")
            getApplicationAccessControlEnabledAsUser = mSecurityManager?.javaClass?.getDeclaredMethod(
                "getApplicationAccessControlEnabledAsUser", String::class.java, Int::class.javaPrimitiveType
            )
            getApplicationAccessControlEnabledAsUser?.isAccessible = true
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        sortList()
    }

    private fun sortList() {
        filteredAppList.sortWith { app1, app2 ->
            try {
                val app1checked = getApplicationAccessControlEnabledAsUser?.invoke(mSecurityManager, app1.pkgName, app1.user) as? Boolean ?: false
                val app2checked = getApplicationAccessControlEnabledAsUser?.invoke(mSecurityManager, app2.pkgName, app2.user) as? Boolean ?: false
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

    override fun isEnabled(position: Int): Boolean {
        val ad = getItem(position)
        return ad.pkgName != "com.miui.securitycenter"
    }

    override fun getCount(): Int = filteredAppList.size

    override fun getItem(position: Int): AppData = filteredAppList[position]

    override fun getItemId(position: Int): Long = position.toLong()

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
            itemChecked.isChecked = getApplicationAccessControlEnabledAsUser?.invoke(mSecurityManager, ad.pkgName, ad.user) as? Boolean ?: false
        } catch (_: Throwable) {
            itemChecked.visibility = View.GONE
        }

        val enabled = ad.pkgName != "com.miui.securitycenter"
        itemIcon.alpha = if (enabled) 1.0f else 0.5f
        itemTitle.alpha = if (enabled) 1.0f else 0.5f
        itemChecked.visibility = if (enabled) View.VISIBLE else View.INVISIBLE
        row.isEnabled = enabled

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
