package tv.withaibuild.customiuizer.subs

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.ColorCircle
import tv.withaibuild.customiuizer.utils.Helpers
import java.util.Locale

class ColorSelector : SubFragment(), ColorCircle.ColorListener {

    private var key: String? = null
    private var colorCircle: ColorCircle? = null
    private var white: TextView? = null
    private var black: TextView? = null
    private var auto: TextView? = null
    private var selectedColorHint: TextView? = null
    private var selectedColorView: View? = null
    private var hsvBar: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        padded = false
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val v = view ?: return
        key = arguments?.getString("key")
        val mKey = key ?: return

        selectedColorView = v.findViewById(R.id.selected_color)
        selectedColorView?.setBackgroundResource(R.drawable.rounded_corners)
        selectedColorHint = v.findViewById(R.id.selected_color_hint)
        colorCircle = v.findViewById(R.id.color_circle)

        val prefColor = AppHelper.getIntOfAppPrefs(mKey, Color.WHITE)
        colorCircle?.init(prefColor)
        colorCircle?.setListener(this)
        if (savedInstanceState != null) {
            val savedColor = savedInstanceState.getInt("colorCircleColor")
            colorCircle?.setColor(savedColor, true)
        }
        val currentColor = colorCircle?.getColor() ?: prefColor
        updateSelColor(currentColor)

        hsvBar = v.findViewById(R.id.hsv_value)
        hsvBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                colorCircle?.setValue(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        val hsv = FloatArray(3)
        Color.RGBToHSV(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), hsv)
        hsvBar?.setProgress((hsv[2] * 100).toInt(), false)

        val alphaBar = v.findViewById<SeekBar>(R.id.alpha_value)
        alphaBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                colorCircle?.setAlphaVal(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        alphaBar.setProgress(Color.alpha(currentColor), false)

        white = v.findViewById(R.id.white_color)
        black = v.findViewById(R.id.black_color)
        auto = v.findViewById(R.id.auto_color)

        white?.isSelected = currentColor == Color.WHITE
        white?.setOnClickListener {
            setSelected(1)
            colorCircle?.setColor(Color.WHITE)
            hsvBar?.setProgress(100, false)
        }

        black?.isSelected = currentColor == Color.BLACK
        black?.setOnClickListener {
            setSelected(2)
            colorCircle?.setColor(Color.BLACK)
            hsvBar?.setProgress(0, false)
        }

        auto?.apply {
            if (mKey.contains("pref_key_system_batteryindicator")) visibility = View.VISIBLE
            isSelected = currentColor == Color.TRANSPARENT
            setOnClickListener {
                setSelected(3)
                colorCircle?.setColor(Color.TRANSPARENT)
                hsvBar?.setProgress(0, false)
            }
        }

        selectedColorHint?.setOnClickListener {
            AppHelper.showInputDialog(activity, selectedColorHint?.text?.toString(), R.string.array_static, 0, 1, object : Helpers.InputCallback {
                override fun onInputFinished(key: String?, text: String?) {
                    if (key != null && !TextUtils.isEmpty(text?.trim())) {
                        try {
                            colorCircle?.setColor(Color.parseColor(text), true)
                        } catch (_: IllegalArgumentException) {
                        }
                    }
                }
            }, false)
        }
    }

    override fun onColorSelected(color: Int) {
        updateSelColor(color)
    }

    private fun updateSelColor(color: Int) {
        val gd = selectedColorView?.background as? GradientDrawable
        gd?.colors = if (color == Color.TRANSPARENT) intArrayOf(Color.WHITE, Color.BLACK) else intArrayOf(color, color)
        selectedColorHint?.text = String.format(Locale.US, "#%08X", color)
    }

    private fun setSelected(btn: Int) {
        white?.isSelected = false
        black?.isSelected = false
        auto?.isSelected = false
        when (btn) {
            1 -> white?.isSelected = true
            2 -> black?.isSelected = true
            3 -> auto?.isSelected = true
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        colorCircle?.getColor()?.let { savedInstanceState.putInt("colorCircleColor", it) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun saveSharedPrefs() {
        key?.let { AppHelper.appPrefs.edit().putInt(it, colorCircle?.getColor() ?: Color.WHITE).apply() }
        super.saveSharedPrefs()
    }

    override fun loadSharedPrefs() {}
}
