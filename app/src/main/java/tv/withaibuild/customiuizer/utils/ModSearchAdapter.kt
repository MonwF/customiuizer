package tv.withaibuild.customiuizer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import tv.withaibuild.customiuizer.R
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

class ModSearchAdapter(context: Context) : BaseAdapter(), Filterable {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mFilter = ItemFilter()
    private val modsList = CopyOnWriteArrayList<ModData>()
    private var filterString = ""

    init {
        @SuppressLint("WrongConstant")
        val unused = 0
    }

    private fun sortList() {
        modsList.sortWith { app1, app2 ->
            val breadcrumbs = app1.breadcrumbs.compareTo(app2.breadcrumbs, ignoreCase = true)
            if (breadcrumbs == 0) app1.title.compareTo(app2.title, ignoreCase = true) else breadcrumbs
        }
    }

    override fun getCount(): Int = modsList.size

    override fun getItem(position: Int): ModData = modsList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = convertView ?: mInflater.inflate(R.layout.pref_item, parent, false)

        val itemTitle: TextView = row.findViewById(android.R.id.title)
        val itemSummary: TextView = row.findViewById(android.R.id.summary)

        val ad = getItem(position)

        val start = ad.title.lowercase(Locale.ROOT).indexOf(filterString)
        if (start >= 0) {
            val spannable = SpannableString(ad.title)
            spannable.setSpan(
                ForegroundColorSpan(Helpers.markColorVibrant),
                start,
                start + filterString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            itemTitle.setText(spannable, TextView.BufferType.SPANNABLE)
        } else {
            itemTitle.text = ad.title
        }
        itemSummary.text = ad.breadcrumbs

        return row
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            filterString = constraint.toString().lowercase(Locale.ROOT)
            val nlist = ArrayList<ModData>()

            for (filterableData in Helpers.allModsList) {
                if (constraint.toString() == Helpers.NEW_MODS_SEARCH_QUERY) {
                    if (Helpers.newMods.contains(filterableData.key)) nlist.add(filterableData)
                } else if (filterableData.title.lowercase(Locale.ROOT).contains(filterString)) {
                    nlist.add(filterableData)
                }
            }

            return FilterResults().apply {
                values = nlist
                count = nlist.size
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            modsList.clear()
            if (results != null && results.count > 0 && results.values != null) {
                modsList.addAll(results.values as ArrayList<ModData>)
            }
            sortList()
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter = mFilter
}
