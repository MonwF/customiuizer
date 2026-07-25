package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import java.util.IllegalFormatException
import java.util.Locale
import kotlin.math.roundToInt

class SeekBarPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr), PreferenceState {

    private var dynamic = false
    private var newmod = false
    private var highlight = false
    private var unsupported = false

    private var defaultValue = 0
    private var minValue = 0
    private var maxValue = 10
    private var stepValue = 1
    private var negativeShift = 0
    private val indentLevel: Int

    private var displayDividerValue = 1
    private var useDisplayDividerValue = false
    private var showPlus = false

    private var format: String? = null
    private var note: String? = null
    private var offText: String? = null

    private var steppedMinValue = 0
    private var steppedMaxValue = 0

    private var valueView: TextView? = null
    private var seekBar: SeekBar? = null

    private var listener: SeekBar.OnSeekBarChangeListener? = null

    init {
        if (attrs != null) {
            val xmlAttrs: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference)
            indentLevel = xmlAttrs.getInt(R.styleable.SeekBarPreference_indentLevel, 0)
            dynamic = xmlAttrs.getBoolean(R.styleable.SeekBarPreference_dynamic, false)
            minValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_minValue, 0)
            maxValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_maxValue, 10)
            stepValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_stepValue, 1)
            defaultValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_android_defaultValue, 0)
            negativeShift = xmlAttrs.getInt(R.styleable.SeekBarPreference_negativeShift, 0)
            showPlus = xmlAttrs.getBoolean(R.styleable.SeekBarPreference_showplus, false)

            if (xmlAttrs.hasValue(R.styleable.SeekBarPreference_displayDividerValue)) {
                useDisplayDividerValue = true
                displayDividerValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_displayDividerValue, 1)
            }

            if (minValue < 0) minValue = 0
            if (maxValue <= minValue) maxValue = minValue + 1

            if (defaultValue < minValue) defaultValue = minValue
            else if (defaultValue > maxValue) defaultValue = maxValue

            if (stepValue <= 0) stepValue = 1

            format = xmlAttrs.getString(R.styleable.SeekBarPreference_format)
            note = xmlAttrs.getString(R.styleable.SeekBarPreference_note)
            offText = xmlAttrs.getString(R.styleable.SeekBarPreference_offtext)

            xmlAttrs.recycle()
        } else {
            indentLevel = 0
        }

        steppedMinValue = (minValue.toFloat() / stepValue).roundToInt()
        steppedMaxValue = (maxValue.toFloat() / stepValue).roundToInt()
        layoutResource = R.layout.preference_seekbar12
    }

    fun getView(finalView: View) {
        val mTitle = finalView.findViewById<TextView>(android.R.id.title)
        mTitle?.text = (title?.toString() ?: "") + if (unsupported) " ⨯" else if (dynamic) " ⟲" else ""
        seekBar?.alpha = if (isEnabled) 1.0f else 0.75f
        if (newmod) mTitle?.let { Helpers.applyNewMod(it) }
        if (highlight) Helpers.applySearchItemHighlight(finalView)

        val childPadding = context.resources.getDimensionPixelSize(R.dimen.preference_item_child_padding)
        val hrzPadding = (indentLevel + 1) * childPadding
        finalView.setPadding(hrzPadding, 0, childPadding, 0)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val mSummaryView = holder.findViewById(android.R.id.summary) as? TextView
        if (!summary.isNullOrEmpty()) {
            mSummaryView?.text = summary
        } else {
            mSummaryView?.visibility = View.GONE
        }

        val mNoteView = holder.findViewById(android.R.id.text1) as? TextView
        if (note.isNullOrEmpty()) {
            mNoteView?.visibility = View.GONE
        } else {
            mNoteView?.text = note
        }

        valueView = holder.findViewById(R.id.seekbar_value) as? TextView
        seekBar = holder.findViewById(R.id.seekbar) as? SeekBar
        seekBar?.max = steppedMaxValue - steppedMinValue

        setValue(AppHelper.getIntOfAppPrefs(key, defaultValue))

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                listener?.onStopTrackingTouch(seekBar)
                saveValue()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                listener?.onStartTrackingTouch(seekBar)
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                listener?.onProgressChanged(seekBar, getValue(), fromUser)
                updateDisplay(progress)
            }
        })
        getView(holder.itemView)
        holder.setDividerAllowedAbove(false)
    }

    fun setOnSeekBarChangeListener(listener: SeekBar.OnSeekBarChangeListener?) {
        this.listener = listener
    }

    fun getMinValue(): Int = minValue

    fun setMinValue(value: Int) {
        minValue = value
        updateAllValues()
    }

    fun getMaxValue(): Int = maxValue

    fun setMaxValue(value: Int) {
        maxValue = value
        updateAllValues()
    }

    fun getStepValue(): Int = stepValue

    fun setStepValue(value: Int) {
        stepValue = value
        updateAllValues()
    }

    fun getFormat(): String? = format

    private fun setFormat(format: String?) {
        this.format = format
        updateDisplay()
    }

    fun setFormat(formatResId: Int) {
        setFormat(context.resources.getString(formatResId))
    }

    fun getValue(): Int = seekBar?.let { (it.progress + steppedMinValue) * stepValue } ?: defaultValue

    fun setValue(value: Int) = setValue(value, false)

    fun setValue(value: Int, save: Boolean) {
        val progress = getBoundedValue(value) - steppedMinValue
        seekBar?.setProgress(progress)
        updateDisplay(progress)
        if (save) saveValue()
    }

    fun setDefaultValue(value: Int) {
        defaultValue = value
    }

    private fun updateAllValues() {
        var currentValue = getValue()
        if (maxValue <= minValue) maxValue = minValue + 1
        steppedMinValue = (minValue.toFloat() / stepValue).roundToInt()
        steppedMaxValue = (maxValue.toFloat() / stepValue).roundToInt()

        seekBar?.max = steppedMaxValue - steppedMinValue

        currentValue = getBoundedValue(currentValue) - steppedMinValue

        seekBar?.let {
            it.setProgress(currentValue)
            updateDisplay(currentValue)
        }
    }

    private fun getBoundedValue(value: Int): Int {
        var v = (value.toFloat() / stepValue).roundToInt()
        if (v < steppedMinValue) v = steppedMinValue
        if (v > steppedMaxValue) v = steppedMaxValue
        return v
    }

    private fun updateDisplay() {
        seekBar?.let { updateDisplay(it.progress) }
    }

    private fun updateDisplay(value: Int) {
        val mValue = valueView ?: return
        if (!TextUtils.isEmpty(format)) {
            mValue.visibility = View.VISIBLE
            var realValue = (value + steppedMinValue) * stepValue

            if (realValue == defaultValue && offText != null) {
                mValue.text = offText
                return
            }

            if (negativeShift > 0) realValue -= negativeShift

            val fmt = format ?: ""
            val text = try {
                if (useDisplayDividerValue) {
                    val floatValue = realValue.toFloat() / displayDividerValue
                    String.format(Locale.US, fmt, floatValue)
                } else {
                    String.format(Locale.US, fmt, realValue)
                }
            } catch (e: IllegalFormatException) {
                e.printStackTrace()
                realValue.toString()
            }
            mValue.text = if (showPlus && realValue > 0) "+$text" else text
        } else {
            mValue.visibility = View.GONE
        }
    }

    private fun saveValue() {
        val k = key ?: return
        AppHelper.appPrefs.edit().putInt(k, getValue()).apply()
    }

    fun setUnsupported(value: Boolean) {
        unsupported = value
        isEnabled = !value
    }

    override fun markAsNew() {
        newmod = true
    }

    override fun applyHighlight() {
        highlight = true
    }
}
