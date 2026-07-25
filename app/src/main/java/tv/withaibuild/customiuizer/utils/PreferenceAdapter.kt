package tv.withaibuild.customiuizer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import tv.withaibuild.customiuizer.R

class PreferenceAdapter(context: Context, private val key: String, private val activities: Boolean) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val items = ArrayList<String>()

    init {
        updateItems()
    }

    fun updateItems() {
        items.clear()
        val itemStr = AppHelper.getStringOfAppPrefs(key, "") ?: ""
        if (itemStr.isEmpty()) return
        items.addAll(itemStr.trim().split("|"))
    }

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): String = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = convertView ?: inflater.inflate(R.layout.pref_item, parent, false)

        Helpers.setMiuiPrefItem(row)
        val dragHandle = row.findViewById<ImageView>(R.id.drag_handle)
        val itemIcon = row.findViewById<ImageView>(android.R.id.icon)
        val itemTitle = row.findViewById<TextView>(android.R.id.title)
        val itemSummary = row.findViewById<TextView>(android.R.id.summary)

        dragHandle.visibility = if (activities) View.GONE else View.VISIBLE
        (parent as? SortableListView)?.let { dragHandle.setOnTouchListener(it.getListenerForStartingSort()) }
        itemIcon.visibility = View.VISIBLE
        val uuid = getItem(position)
        val name = AppHelper.getActionNameLocal(row.context, key + "_" + uuid)
        if (name == null) {
            itemTitle.setText(R.string.notselected)
            itemSummary.visibility = View.GONE
        } else {
            if (activities) {
                val actStr = AppHelper.getStringOfAppPrefs(key + "_" + uuid + "_activity", "") ?: ""
                itemTitle.text = Helpers.getAppName(row.context, actStr, true) ?: ""
            } else {
                itemTitle.text = name.first
            }
            if (name.second.isNullOrEmpty()) {
                itemSummary.visibility = View.GONE
            } else {
                itemSummary.visibility = View.VISIBLE
                itemSummary.text = name.second
            }
        }

        try {
            val drawable = Helpers.getActionImageLocal(row.context, key + "_" + uuid)
            itemIcon.setImageDrawable(drawable ?: row.context.packageManager.getApplicationIcon(Helpers.modulePkg))
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        row.setPadding(row.paddingLeft, row.paddingTop, 0, row.paddingBottom)
        return row
    }
}
