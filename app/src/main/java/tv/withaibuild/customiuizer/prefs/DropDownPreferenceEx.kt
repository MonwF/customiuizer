package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceViewHolder
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.utils.Helpers

class DropDownPreferenceEx(context: Context, attrs: AttributeSet?) : DropDownPreference(context, attrs), PreferenceState {

    private var sValue: CharSequence? = null
    private val res = context.resources
    private val primary = res.getColor(R.color.preference_primary_text, context.theme)
    private val secondary = res.getColor(R.color.preference_secondary_text, context.theme)
    private val childPadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding)

    private val indentLevel: Int
    private val dynamic: Boolean
    private var newmod = false
    private var highlight = false
    private var unsupported = false
    private val valueAsSummary: Boolean

    init {
        val xmlAttrs: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceEx)
        indentLevel = xmlAttrs.getInt(R.styleable.ListPreferenceEx_indentLevel, 0)
        dynamic = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_dynamic, false)
        valueAsSummary = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_valueAsSummary, false)
        xmlAttrs.recycle()
        isIconSpaceReserved = false
    }

    override fun setValue(value: String?) {
        super.setValue(value)
        val index = findIndexOfValue(value)
        val entries = entries
        if (entries != null && index >= 0 && index < entries.size) {
            sValue = entries[index]
        }
    }

    fun setUnsupported(value: Boolean) {
        unsupported = value
        isEnabled = !value
    }

    fun getView(finalView: View) {
        val title = finalView.findViewById<TextView>(android.R.id.title)
        val summary = finalView.findViewById<TextView>(android.R.id.summary)
        val valSummary = finalView.findViewById<TextView>(android.R.id.hint)

        summary?.visibility = if (valueAsSummary || summary == null || summary.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        valSummary?.visibility = if (valueAsSummary) View.VISIBLE else View.GONE
        valSummary?.text = if (valueAsSummary) sValue else ""
        if (valueAsSummary) valSummary?.setTextColor(if (Helpers.isNightMode(context)) secondary else primary)
        title?.setTextColor(if (isEnabled) primary else secondary)
        title?.text = (title?.text?.toString() ?: "") + if (unsupported) " ⨯" else if (dynamic) " ⟲" else ""
        if (newmod) title?.let { Helpers.applyNewMod(it) }
        if (highlight) Helpers.applySearchItemHighlight(finalView)
        val hrzPadding = (indentLevel + 1) * childPadding
        finalView.setPadding(hrzPadding, 0, childPadding, 0)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val title = holder.findViewById(android.R.id.title) as? TextView
        title?.maxLines = 3

        val summary = holder.findViewById(android.R.id.summary) as? TextView
        summary?.setTextColor(secondary)

        var valSummary = holder.itemView.findViewById<TextView>(android.R.id.hint)
        if (valSummary == null) {
            valSummary = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, summary?.textSize ?: textSize)
                setTextColor(summary?.currentTextColor ?: currentTextColor)
                setPadding(summary?.paddingLeft ?: 0, summary?.paddingTop ?: 0, res.getDimensionPixelSize(R.dimen.preference_summary_padding_right), summary?.paddingBottom ?: 0)
                id = android.R.id.hint
            }
            (holder.itemView as? ViewGroup)?.addView(valSummary, 2)
        }

        getView(holder.itemView)
    }

    override fun markAsNew() {
        newmod = true
    }

    override fun applyHighlight() {
        highlight = true
    }
}
