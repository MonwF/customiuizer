package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers

class PreferenceEx(context: Context, attrs: AttributeSet?) : Preference(context, attrs), PreferenceState {

    private val indentLevel: Int
    private val dynamic: Boolean
    private val warning: Boolean
    private val countAsSummary: Boolean
    private val longClickable: Boolean
    private var customSummary: String? = null
    private var newmod = false
    private var highlight = false
    private var unsupported = false
    private var longPressListener: View.OnLongClickListener? = null

    init {
        val xmlAttrs: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PreferenceEx)
        dynamic = xmlAttrs.getBoolean(R.styleable.PreferenceEx_dynamic, false)
        indentLevel = xmlAttrs.getInt(R.styleable.PreferenceEx_indentLevel, 0)
        warning = xmlAttrs.getBoolean(R.styleable.PreferenceEx_warning, false)
        countAsSummary = xmlAttrs.getBoolean(R.styleable.PreferenceEx_countAsSummary, false)
        longClickable = xmlAttrs.getBoolean(R.styleable.PreferenceEx_longClickable, false)
        xmlAttrs.recycle()
        isIconSpaceReserved = false
    }

    fun getView(finalView: View) {
        val title = finalView.findViewById<TextView>(android.R.id.title)
        val summary = finalView.findViewById<TextView>(android.R.id.summary)
        val valSummary = finalView.findViewById<TextView>(android.R.id.hint)
        val res: Resources = context.resources

        summary?.visibility = if (customSummary != null || countAsSummary || summary == null || summary.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        valSummary?.visibility = if (customSummary != null || countAsSummary) View.VISIBLE else View.GONE
        if (customSummary != null || countAsSummary) {
            val secondary = res.getColor(R.color.preference_secondary_text, context.theme)
            val disableColor = res.getColor(R.color.preference_primary_text_disable, context.theme)
            valSummary?.setTextColor(if (isEnabled) secondary else disableColor)
        }
        when {
            customSummary != null -> valSummary?.text = customSummary
            countAsSummary -> {
                val count = AppHelper.getStringSetOfAppPrefs(key, linkedSetOf()).size +
                        AppHelper.getStringSetOfAppPrefs(key + "_black", linkedSetOf()).size
                valSummary?.text = count.toString()
            }
            else -> valSummary?.text = null
        }
        if (warning) {
            title?.setTextColor(Helpers.markColor)
        }
        title?.text = title?.text.toString() + if (unsupported) " ⨯" else if (dynamic) " ⟲" else ""
        if (newmod) title?.let { Helpers.applyNewMod(it) }
        if (highlight) Helpers.applySearchItemHighlight(finalView)

        val childPadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding)
        val hrzPadding = (indentLevel + 1) * childPadding
        finalView.setPadding(hrzPadding, 0, childPadding, 0)
        if (longClickable) {
            finalView.setOnLongClickListener { longPressListener?.onLongClick(finalView) ?: false }
        }
    }

    fun setLongPressListener(ll: View.OnLongClickListener?) {
        longPressListener = ll
    }

    fun setCustomSummary(text: String?) {
        customSummary = text
        notifyChanged()
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

    fun setUnsupported(value: Boolean) {
        unsupported = value
        isEnabled = !value
    }
}
