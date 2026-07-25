package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import tv.withaibuild.customiuizer.R

class PreferenceCategoryEx(context: Context, attrs: AttributeSet?) : PreferenceCategory(context, attrs) {

    private val dynamic: Boolean
    private var state = 0 // 0-正常 1-纯区块 2-顶层隐藏
    private var unsupported = false

    init {
        val xmlAttrs: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PreferenceCategoryEx)
        dynamic = xmlAttrs.getBoolean(R.styleable.PreferenceCategoryEx_dynamic, false)
        state = xmlAttrs.getInt(R.styleable.PreferenceCategoryEx_state, 0)
        xmlAttrs.recycle()
        layoutResource = R.layout.preference_category
    }

    override fun onPrepareAddPreference(preference: Preference): Boolean {
        preference.onParentChanged(this, shouldDisableDependents())
        return true
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val title = holder.findViewById(android.R.id.title) as? TextView
        title?.text = (title?.text?.toString() ?: "") + if (unsupported) " ⨯" else if (dynamic) " ⟲" else ""
        title?.visibility = if (state == 2 || state == 1) View.GONE else View.VISIBLE
        val finalView = holder.itemView
        val childPadding = context.resources.getDimensionPixelSize(R.dimen.preference_item_child_padding)
        if (state == 2) {
            finalView.setPadding(childPadding, 0, childPadding, 0)
        } else {
            val verticalPadding = context.resources.getDimensionPixelSize(R.dimen.preference_item_padding_top)
            finalView.setPadding(childPadding, verticalPadding, childPadding, verticalPadding)
        }
    }

    fun setUnsupported(value: Boolean) {
        unsupported = value
        isEnabled = !value
    }

    fun isDynamic(): Boolean = dynamic

    fun hide() {
        state = 2
        notifyChanged()
    }
}
