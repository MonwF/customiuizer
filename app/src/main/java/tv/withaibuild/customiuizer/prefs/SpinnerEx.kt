package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.ListPopupWindow
import tv.withaibuild.customiuizer.R

open class SpinnerEx(context: Context, attrs: AttributeSet?) : AppCompatSpinner(context, attrs) {

    var entries: Array<CharSequence>? = null
    var entryValues: IntArray? = null
    private val disabledItems = ArrayList<Int>()

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SpinnerEx, 0, 0)
        entries = typedArray.getTextArray(R.styleable.SpinnerEx_android_entries)
        val entryValuesResId = typedArray.getResourceId(R.styleable.SpinnerEx_entryValues, 0)
        if (entryValuesResId != 0) {
            entryValues = resources.getIntArray(entryValuesResId)
        }
        typedArray.recycle()

        val childPadding = context.resources.getDimensionPixelSize(R.dimen.preference_item_child_padding)
        setPadding(childPadding, 0, childPadding, 0)

        try {
            val mPopup = AppCompatSpinner::class.java.getDeclaredField("mPopup")
            mPopup.isAccessible = true
            val popupWindow = mPopup.get(this) as ListPopupWindow
            popupWindow.setHeight((40 * 10 * resources.displayMetrics.density).toInt())
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun addDisabledItems(position: Int) {
        disabledItems.add(position)
    }

    private fun findIndex(value: Int, values: IntArray?): Int {
        values ?: return -1
        for (i in values.indices) {
            if (values[i] == value) return i
        }
        return -1
    }

    fun init(value: Int) {
        val ent = entries ?: return
        val vals = entryValues ?: return
        val adapter = ArrayAdapterEx(context, android.R.layout.simple_spinner_item, ent)
        setAdapter(adapter)
        setSelection(findIndex(value, vals))
    }

    fun getSelectedArrayValue(): Int {
        return entryValues?.getOrNull(selectedItemPosition) ?: 0
    }

    private inner class ArrayAdapterEx(
        context: Context,
        resource: Int,
        objects: Array<CharSequence>
    ) : ArrayAdapter<CharSequence>(context, resource, objects) {

        override fun isEnabled(position: Int): Boolean = !disabledItems.contains(position)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            view.isEnabled = isEnabled(position)
            return view
        }
    }
}
