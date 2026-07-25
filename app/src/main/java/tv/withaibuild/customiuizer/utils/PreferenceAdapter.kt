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
        val holder = (convertView?.tag as? ViewHolder) ?: run {
            val row = inflater.inflate(R.layout.pref_item, parent, false)
            Helpers.setMiuiPrefItem(row)
            ViewHolder(row).also { row.tag = it }
        }

        holder.dragHandle.visibility = if (activities) View.GONE else View.VISIBLE
        (parent as? SortableListView)?.let { holder.dragHandle.setOnTouchListener(it.getListenerForStartingSort()) }
        holder.icon.visibility = View.VISIBLE
        val uuid = getItem(position)
        val name = AppHelper.getActionNameLocal(holder.root.context, key + "_" + uuid)
        if (name == null) {
            holder.title.setText(R.string.notselected)
            holder.summary.visibility = View.GONE
        } else {
            if (activities) {
                val actStr = AppHelper.getStringOfAppPrefs(key + "_" + uuid + "_activity", "") ?: ""
                holder.title.text = Helpers.getAppName(holder.root.context, actStr, true) ?: ""
            } else {
                holder.title.text = name.first
            }
            if (name.second.isNullOrEmpty()) {
                holder.summary.visibility = View.GONE
            } else {
                holder.summary.visibility = View.VISIBLE
                holder.summary.text = name.second
            }
        }

        try {
            val drawable = Helpers.getActionImageLocal(holder.root.context, key + "_" + uuid)
            holder.icon.setImageDrawable(drawable ?: holder.root.context.packageManager.getApplicationIcon(Helpers.modulePkg))
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        holder.root.setPadding(holder.root.paddingLeft, holder.root.paddingTop, 0, holder.root.paddingBottom)
        return holder.root
    }

    private class ViewHolder(val root: View) {
        val dragHandle: ImageView = root.findViewById(R.id.drag_handle)
        val icon: ImageView = root.findViewById(android.R.id.icon)
        val title: TextView = root.findViewById(android.R.id.title)
        val summary: TextView = root.findViewById(android.R.id.summary)
    }
}
