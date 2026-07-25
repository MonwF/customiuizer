package tv.withaibuild.customiuizer.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RadialGradient
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import tv.withaibuild.customiuizer.R

class ColorCircle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var posXin = 0f
    private var posYin = 0f
    private var radius = 0f
    private var innerRadius = 0f
    private var offset = 0
    private var alphaVal = 0
    private val paint1 = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint1a = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 2.0f
        style = Paint.Style.STROKE
    }
    private val paint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL_AND_STROKE
    }
    private var listener: ColorListener? = null
    private var mTransparent = false
    private val mColor = FloatArray(3)
    private var initialized = false
    private val displayMetrics = resources.displayMetrics
    private val tmpColorPoint = PointF()

    interface ColorListener {
        fun onColorSelected(color: Int)
    }

    fun setListener(colorListener: ColorListener?) {
        listener = colorListener
    }

    fun getColor(): Int {
        if (mTransparent) return Color.TRANSPARENT
        val resColor = Color.HSVToColor(mColor)
        return (alphaVal shl 24) or (resColor and 0x00FFFFFF)
    }

    fun setColor(color: Int) = setColor(color, false)

    fun setColor(color: Int, setAlpha: Boolean) {
        mTransparent = color == Color.TRANSPARENT
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), mColor)
        if (setAlpha) alphaVal = Color.alpha(color)
        listener?.onColorSelected(getColor())
        val coords = getPointForColor()
        updatePickerPos(coords.x, coords.y)
        postInvalidate()
    }

    fun setAlphaVal(alphaV: Int) {
        alphaVal = alphaV
        listener?.onColorSelected(getColor())
    }

    fun setValue(value: Float) {
        mTransparent = false
        mColor[2] = value
        listener?.onColorSelected(getColor())
        postInvalidate()
    }

    fun init(prefColor: Int) {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        paint1.isAntiAlias = true
        paint1a.isAntiAlias = true
        paint2.isAntiAlias = true
        paint3.isAntiAlias = true
        offset = resources.getDimension(R.dimen.screen_color_preview_offset).toInt()
        mTransparent = prefColor == Color.TRANSPARENT
        alphaVal = Color.alpha(prefColor)
        Color.RGBToHSV(Color.red(prefColor), Color.green(prefColor), Color.blue(prefColor), mColor)
        update()
        val coords = getPointForColor()
        updatePickerPos(coords.x, coords.y)
        initialized = true
        postInvalidate()
    }

    private fun update() {
        val diameter = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels) *
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 0.7f else 0.8f
        radius = diameter / 2.0f
        innerRadius = radius - offset * 2
        layoutParams?.let {
            it.width = Math.round(diameter)
            it.height = Math.round(diameter)
        }

        val steps = 6
        val colors = IntArray(steps + 1)
        val hsv = floatArrayOf(0f, 1f, 1f)
        for (i in 0 until steps) {
            hsv[0] = (360f / steps) * i
            colors[i] = Color.HSVToColor(hsv)
        }
        colors[steps] = colors[0]

        paint1.shader = SweepGradient(radius, radius, colors, null)
        paint1a.shader = RadialGradient(radius, radius, radius, 0xFFFFFFFF.toInt(), 0x00FFFFFF, android.graphics.Shader.TileMode.CLAMP)
    }

    private fun getPointForColor(): PointF {
        val hue = mColor[0]
        val sat = mColor[1]
        tmpColorPoint.x = (radius + radius * sat * Math.cos(Math.toRadians(hue.toDouble()))).toFloat()
        tmpColorPoint.y = (radius + radius * sat * Math.sin(Math.toRadians(hue.toDouble()))).toFloat()
        return tmpColorPoint
    }

    fun getColorForPoint(x: Int, y: Int) {
        val dx = x - radius
        val dy = y - radius
        mColor[0] = ((Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())) + 360f) % 360f).toFloat()
        mColor[1] = (Math.hypot(dx.toDouble(), dy.toDouble()) / radius).toFloat().coerceIn(0f, 1f)
        mTransparent = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        update()
        postInvalidate()
    }

    private fun distanceToCenter(f: Float, f2: Float): Float {
        return Math.hypot((radius - f).toDouble(), (radius - f2).toDouble()).toFloat()
    }

    private fun isInCircle(f: Float, f2: Float, radius: Float): Boolean {
        return distanceToCenter(f, f2) <= radius
    }

    private fun limitByCircle(f: Float, f2: Float, radius: Float) {
        val angle = Math.atan2((f - radius).toDouble(), (f2 - radius).toDouble()).toFloat()
        posXin = radius + (radius * Math.sin(angle.toDouble())).toFloat() + offset * 2
        posYin = radius + (radius * Math.cos(angle.toDouble())).toFloat() + offset * 2
    }

    private fun updatePickerPos(x: Float, y: Float) {
        if (isInCircle(x, y, innerRadius)) {
            posXin = x
            posYin = y
        } else {
            limitByCircle(x, y, innerRadius)
        }
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        if (!initialized) return false

        parent.requestDisallowInterceptTouchEvent(true)
        if (!isEnabled) return true

        val x = motionEvent.x
        val y = motionEvent.y
        updatePickerPos(x, y)
        getColorForPoint(x.toInt(), y.toInt())
        listener?.onColorSelected(getColor())
        postInvalidate()

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!initialized) return
        canvas.drawCircle(radius, radius, radius - offset * 2, paint1)
        canvas.drawCircle(radius, radius, radius - offset * 2, paint1a)
        canvas.drawCircle(posXin, posYin, offset.toFloat(), paint2)
        canvas.drawCircle(posXin, posYin, (offset - 2).toFloat(), paint3)
    }
}
