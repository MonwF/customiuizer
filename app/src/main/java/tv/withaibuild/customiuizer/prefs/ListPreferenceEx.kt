package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.PreferenceViewHolder
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.utils.Helpers

class ListPreferenceEx(context: Context, attrs: AttributeSet?) : ListPreference(context, attrs), PreferenceState {

    private val indentLevel: Int
    private val dynamic: Boolean
    private var newmod = false
    private var highlight = false
    private var unsupported = false
    private val valueAsSummary: Boolean
    private var listDefaultValue: String? = null

    init {
        val xmlAttrs: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceEx)
        indentLevel = xmlAttrs.getInt(R.styleable.ListPreferenceEx_indentLevel, 0)
        dynamic = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_dynamic, false)
        valueAsSummary = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_valueAsSummary, false)
        xmlAttrs.recycle()
        isIconSpaceReserved = false
    }

    override fun notifyChanged() {
        super.notifyChanged()
        notifyDependencyChange(shouldDisableDependents())
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        val value = a.getString(index)
        listDefaultValue = value
        return value
    }

    override fun shouldDisableDependents(): Boolean {
        return TextUtils.equals(listDefaultValue, value) || super.shouldDisableDependents()
    }

    fun setUnsupported(value: Boolean) {
        unsupported = value
        isEnabled = !value
    }

    fun getView(finalView: View) {
        val title = finalView.findViewById<TextView>(android.R.id.title)
        val summary = finalView.findViewById<TextView>(android.R.id.summary)
        val valSummary = finalView.findViewById<TextView>(android.R.id.hint)
        val res = context.resources

        summary?.visibility = if (valueAsSummary || summary == null || summary.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        valSummary?.visibility = if (valueAsSummary) View.VISIBLE else View.GONE
        valSummary?.text = if (valueAsSummary) entry else ""
        if (valueAsSummary) {
            val disableColor = res.getColor(R.color.preference_primary_text_disable, context.theme)
            val secondary = res.getColor(R.color.preference_secondary_text, context.theme)
            valSummary?.setTextColor(if (isEnabled) secondary else disableColor)
        }
        title?.text = (title?.text?.toString() ?: "") + if (unsupported) " ⨯" else if (dynamic) " ⟲" else ""
        if (newmod) title?.let { Helpers.applyNewMod(it) }
        if (highlight) Helpers.applySearchItemHighlight(finalView)

        val childPadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding)
        val hrzPadding = (indentLevel + 1) * childPadding
        finalView.setPadding(hrzPadding, 0, childPadding, 0)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val title = holder.findViewById(android.R.id.title) as? TextView
        title?.maxLines = 3

        val summary = holder.findViewById(android.R.id.summary) as? TextView

        var valSummary = holder.itemView.findViewById<TextView>(android.R.id.hint)
        if (valSummary == null) {
            valSummary = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, summary?.textSize ?: textSize)
                setPadding(summary?.paddingLeft ?: 0, summary?.paddingTop ?: 0, context.resources.getDimensionPixelSize(R.dimen.preference_summary_padding_right), summary?.paddingBottom ?: 0)
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
