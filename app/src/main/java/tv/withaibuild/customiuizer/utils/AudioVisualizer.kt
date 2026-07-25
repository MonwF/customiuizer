package tv.withaibuild.customiuizer.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.io.File
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class AudioVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mHeight = 0
    private var mWidth = 0
    private val mDensity: Float = context.resources.displayMetrics.density
    private val mPaint: Paint
    private var mGlowPaint = Paint()
    private var mVisualizer: Visualizer? = null
    private var mVisualizerColorAnimator: ObjectAnimator? = null
    private var mVisualizerGlowColorAnimator: ObjectAnimator? = null

    private val mValueAnimators: Array<ValueAnimator>
    private val mFFTPoints = FloatArray(128)
    private val mBands = floatArrayOf(
        50f, 90f, 130f, 180f, 220f, 260f, 320f, 380f, 430f, 520f, 610f, 700f, 770f, 920f, 1080f,
        1270f, 1480f, 1720f, 2000f, 2320f, 2700f, 3135f, 3700f, 4400f, 5300f, 6400f, 7700f, 9500f,
        10500f, 12000f, 16000f
    )
    private var maxDb = 50f
    private val maxDp = 280

    private var isMusicPlaying = false
    @JvmField
    var isScreenOn = false
    private var isOnKeyguard = false
    private var isExpandedPanel = false
    private var isOnCustomLockScreen = false
    private var mPlaying = false
    private var mDisplaying = false
    private var mOpaqueColor = Color.TRANSPARENT
    private var mColor = Color.TRANSPARENT

    private var mArt: Bitmap? = null
    private var mProcessedArt: Bitmap? = null

    private val mBandsNum = 31
    private val mRainbow = IntArray(mBandsNum)
    private val mRainbowVertical = IntArray(mBandsNum)
    private val mPositions = FloatArray(mBandsNum)
    private val mLinePath = Path()
    private val mHsv = FloatArray(3)
    private val mDashIntervals = FloatArray(2)

    @JvmField
    var showOnCustom = false
    private var animDur = 0
    private var transparency = 0
    private lateinit var colorMode: ColorMode
    private lateinit var barStyle: BarStyle
    private lateinit var renderType: RenderType
    private var glowLevel = 0
    private var customColor = 0
    private var randomizeInterval = 0
    @JvmField
    var showInDrawer = false
    @JvmField
    var showWithControllerOnly = false

    private val accel = AccelerateInterpolator()
    private val decel = DecelerateInterpolator()

    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var randomizeColorJob: Job? = null

    enum class BarStyle {
        DUMMY, SOLID, SOLID_ROUNDED, DASHED, CIRCLES, LINE
    }

    enum class ColorMode {
        DUMMY, MATCH, STATIC, RAINBOW_H, RAINBOW_V, DYNAMIC
    }

    enum class RenderType {
        AUTO, LINES, PATH
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)

        val res: Resources = context.resources
        mHeight = if (res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            res.displayMetrics.heightPixels
        } else {
            res.displayMetrics.widthPixels
        }
        mWidth = if (res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            res.displayMetrics.widthPixels
        } else {
            res.displayMetrics.heightPixels
        }

        mPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.MITER
            color = mColor
        }

        animDur = MainModule.mPrefs.getInt("system_visualizer_animdur", 65)
        mValueAnimators = Array(mBandsNum) { i ->
            val j = i * 4 + 3
            ValueAnimator().apply {
                duration = animDur.toLong()
                addUpdateListener { animation ->
                    mFFTPoints[j] = animation.animatedValue as Float
                    postInvalidate()
                }
            }
        }

        for (i in 0 until mBandsNum) {
            mPositions[i] = (i + 1) / mBandsNum.toFloat()
        }

        showOnCustom = MainModule.mPrefs.getBoolean("system_visualizer_custom")
        transparency = (255f - 255f * MainModule.mPrefs.getInt("system_visualizer_transp", 40) / 100f).roundToInt()
        colorMode = ColorMode.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_color", 1)]
        barStyle = BarStyle.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_style", 1)]
        renderType = RenderType.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_render", 0)]
        glowLevel = MainModule.mPrefs.getInt("system_visualizer_glowlevel", 50)
        customColor = MainModule.mPrefs.getInt("system_visualizer_colorval", Color.WHITE)
        randomizeInterval = MainModule.mPrefs.getInt("system_visualizer_dyntime", 10) * 1000
        showInDrawer = MainModule.mPrefs.getBoolean("system_visualizer_drawer")
        showWithControllerOnly = MainModule.mPrefs.getBoolean("system_visualizer_controller")

        updateBarStyle()
        updateGlowPaint()
        updateRainbowColors()

        ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
            override fun onChange(key: String?) {
                try {
                    when (key) {
                        "pref_key_system_visualizer_animdur" -> {
                            animDur = MainModule.mPrefs.getInt("system_visualizer_animdur", 65)
                            for (i in 0 until mBandsNum) {
                                mValueAnimators[i].duration = animDur.toLong()
                            }
                        }
                        "pref_key_system_visualizer_transp" -> {
                            transparency = (255f - 255f * MainModule.mPrefs.getInt("system_visualizer_transp", 40) / 100f).roundToInt()
                            setColor(mOpaqueColor)
                            updateRainbowColors()
                        }
                        "pref_key_system_visualizer_color" -> {
                            colorMode = ColorMode.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_color", 1)]
                            updateBarStyle()
                            updateColorMode()
                        }
                        "pref_key_system_visualizer_style" -> {
                            barStyle = BarStyle.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_style", 1)]
                            updateBarStyle()
                        }
                        "pref_key_system_visualizer_render" -> {
                            renderType = RenderType.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_render", 0)]
                            updateBarStyle()
                        }
                        "pref_key_system_visualizer_glowlevel" -> {
                            glowLevel = MainModule.mPrefs.getInt("system_visualizer_glowlevel", 50)
                            updateGlowPaint()
                        }
                        "pref_key_system_visualizer_colorval" -> {
                            customColor = MainModule.mPrefs.getInt("system_visualizer_colorval", Color.WHITE)
                            setColor(customColor)
                        }
                        "pref_key_system_visualizer_dyntime" -> {
                            randomizeInterval = MainModule.mPrefs.getInt("system_visualizer_dyntime", 10) * 1000
                            randomizeColorJob?.cancel()
                            randomizeColorJob = viewScope.launch { runRandomizeColor() }
                        }
                        "pref_key_system_visualizer_drawer" ->
                            showInDrawer = MainModule.mPrefs.getBoolean("system_visualizer_drawer", false)
                        "pref_key_system_visualizer_controller" ->
                            showWithControllerOnly = MainModule.mPrefs.getBoolean("system_visualizer_controller", false)
                    }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
            }
        })
    }

    private val mVisualizerListener = object : Visualizer.OnDataCaptureListener {
        private var real: Byte = 0
        private var imaginary: Byte = 0
        private var dbValue = 0
        private var magnitude = 0f

        override fun onWaveFormDataCapture(visualizer: Visualizer, bytes: ByteArray, samplingRate: Int) {}

        override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {
            try {
                val bandWidth = samplingRate.toFloat() / fft.size
                val silentFrame = allZeros(fft)
                var band = 0
                var i = 1
                val maxHeight = min(0.85f * maxDp * mDensity, mHeight / 2.0f)

                while (band < mBandsNum && i < fft.size / 2) {
                    magnitude = 0f

                    if (!silentFrame) {
                        while (i < fft.size / 2 && (i * bandWidth <= mBands[band] * samplingRate / 44100f)) {
                            real = fft[i * 2]
                            imaginary = fft[i * 2 + 1]
                            magnitude = max(magnitude, (real * real + imaginary * imaginary).toFloat())
                            i++
                        }
                    }

                    dbValue = if (magnitude > 0) (10 * log10(magnitude)).toInt() else 0
                    maxDb = max(maxDb, dbValue.toFloat())
                    val oldVal = mFFTPoints[band * 4 + 3]
                    val newVal = mFFTPoints[band * 4 + 1] - maxHeight * dbValue / maxDb

                    mValueAnimators[band].cancel()
                    mValueAnimators[band].interpolator = if (newVal < oldVal) decel else accel
                    mValueAnimators[band].setFloatValues(oldVal, newVal)
                    mValueAnimators[band].start()

                    band++
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
            }
        }
    }

    private fun allZeros(array: ByteArray): Boolean = array.all { it == 0.toByte() }

    private fun getRandomColor(): Int {
        mHsv[0] = (Math.random() * 360f).toFloat()
        mHsv[1] = 0.5f + (Math.random() * 0.5f).toFloat()
        mHsv[2] = 0.75f + (Math.random() * 0.25f).toFloat()
        return Color.HSVToColor(mHsv)
    }

    private fun updateGlowPaint() {
        mGlowPaint = Paint(mPaint)
        if (glowLevel == 0) return
        val scale = glowLevel / 100f
        mGlowPaint.pathEffect = null
        mGlowPaint.maskFilter = BlurMaskFilter(15 * mDensity * (1.25f + 0.25f * scale), BlurMaskFilter.Blur.NORMAL)
        mGlowPaint.alpha = min(transparency, 180)
        mGlowPaint.strokeWidth = (0.5f + 1.25f * scale) * mPaint.strokeWidth * if (barStyle == BarStyle.LINE) 4f else if (colorMode == ColorMode.RAINBOW_H) 1.15f else 1.3f
        if (barStyle == BarStyle.SOLID || barStyle == BarStyle.DASHED || mGlowPaint.strokeCap == Paint.Cap.ROUND) {
            mGlowPaint.strokeCap = Paint.Cap.SQUARE
        }
    }

    private val onPaletteGenerated: (Palette?) -> Unit = { palette ->
        try {
            var color = palette?.let {
                var c = Color.TRANSPARENT
                c = it.getLightVibrantColor(c)
                if (c == Color.TRANSPARENT) c = it.getVibrantColor(c)
                if (c == Color.TRANSPARENT) c = it.getDarkVibrantColor(c)
                c
            } ?: Color.TRANSPARENT
            setColor(color)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    fun setBitmap() {
        try {
            if (mProcessedArt === mArt && mArt != null) return
            mProcessedArt = mArt
            val art = mProcessedArt
            if (art != null) {
                viewScope.launch {
                    val palette = withContext(Dispatchers.Default) { Palette.from(art).generate() }
                    onPaletteGenerated(palette)
                }
            } else {
                setColor(Color.TRANSPARENT)
            }
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    fun setColor(color: Int) {
        var c = color
        if (c == Color.TRANSPARENT) c = Color.WHITE
        val newColor = Color.argb(transparency, Color.red(c), Color.green(c), Color.blue(c))
        if (mColor == newColor) return
        mColor = newColor
        mOpaqueColor = c

        val viz = mVisualizer
        if (viz != null) {
            mVisualizerColorAnimator?.cancel()
            mVisualizerColorAnimator = ObjectAnimator.ofArgb(mPaint, "color", mPaint.color, mColor).apply {
                startDelay = (600 * animDur / 65f).roundToInt().toLong()
                duration = (1200 * animDur / 65f).roundToInt().toLong()
                start()
            }

            if (glowLevel > 0) {
                mVisualizerGlowColorAnimator?.cancel()
                mVisualizerGlowColorAnimator = ObjectAnimator.ofArgb(mGlowPaint, "color", mGlowPaint.color, mColor).apply {
                    startDelay = (600 * animDur / 65f).roundToInt().toLong()
                    duration = (1200 * animDur / 65f).roundToInt().toLong()
                    start()
                }
            }
        } else {
            mPaint.color = mColor
            if (glowLevel > 0) mGlowPaint.color = mColor
        }
    }

    private fun updateColorMode() {
        if (!isMusicPlaying) return
        when (colorMode) {
            ColorMode.MATCH -> setBitmap()
            ColorMode.DYNAMIC -> setColor(getRandomColor())
            ColorMode.STATIC -> setColor(customColor)
            else -> setColor(Color.WHITE)
        }
    }

    private fun updateRainbowColors() {
        val jump = 300f / mBandsNum
        mHsv[1] = 1.0f
        mHsv[2] = 1.0f
        for (i in 0 until mRainbow.size) {
            mHsv[0] = jump * i
            mRainbow[i] = Color.HSVToColor(transparency, mHsv)
        }

        for (i in 0 until mRainbowVertical.size) {
            var h = 140f + jump * i
            if (h > 360) h -= 360f
            mHsv[0] = h
            mRainbowVertical[i] = Color.HSVToColor(transparency, mHsv)
        }
    }

    private fun updateBarStyle() {
        when (colorMode) {
            ColorMode.RAINBOW_H -> mPaint.shader = LinearGradient(0f, 0f, mWidth.toFloat(), 0f, mRainbow, mPositions, Shader.TileMode.MIRROR)
            ColorMode.RAINBOW_V -> {
                val maxHeight = min(0.85f * maxDp * mDensity, mHeight / 2.0f)
                mPaint.shader = LinearGradient(0f, mHeight.toFloat(), 0f, mHeight - maxHeight, mRainbowVertical, mPositions, Shader.TileMode.CLAMP)
            }
            else -> mPaint.shader = null
        }

        when (barStyle) {
            BarStyle.SOLID -> {
                mPaint.pathEffect = null
                mPaint.strokeCap = Paint.Cap.BUTT
            }
            BarStyle.SOLID_ROUNDED -> {
                mPaint.pathEffect = null
                mPaint.strokeCap = Paint.Cap.ROUND
            }
            BarStyle.DASHED -> {
                mDashIntervals[0] = 4 * mDensity
                mDashIntervals[1] = 2 * mDensity
                mPaint.pathEffect = DashPathEffect(mDashIntervals, 0f)
                mPaint.strokeCap = Paint.Cap.BUTT
            }
            BarStyle.CIRCLES -> {
                mDashIntervals[0] = 1.0f
                mDashIntervals[1] = mPaint.strokeWidth + mDensity
                mPaint.pathEffect = DashPathEffect(mDashIntervals, 0f)
                mPaint.strokeCap = Paint.Cap.ROUND
            }
            BarStyle.LINE -> {
                mPaint.pathEffect = CornerPathEffect(18 * mDensity)
                mPaint.strokeCap = Paint.Cap.ROUND
                mPaint.strokeWidth = 3 * mDensity
            }
            else -> {}
        }

        updateGlowPaint()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mArt = null
        mProcessedArt = null
        viewScope.cancel()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val barUnit = w / mBandsNum.toFloat()
        val barWidth = barUnit * 0.80f
        mHeight = h
        mWidth = w
        mPaint.strokeWidth = barWidth
        updateBarStyle()

        for (i in 0 until mBandsNum) {
            mFFTPoints[i * 4] = i * barUnit + (barWidth / 2)
            mFFTPoints[i * 4 + 1] = h.toFloat()
            mFFTPoints[i * 4 + 2] = mFFTPoints[i * 4]
            mFFTPoints[i * 4 + 3] = h.toFloat()
        }
    }

    override fun hasOverlappingRendering(): Boolean = mDisplaying

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        try {
            if (mVisualizer?.enabled != true) return
        } catch (t: Throwable) {
            return
        }

        if (barStyle == BarStyle.LINE) {
            mLinePath.reset()
            mLinePath.moveTo(0f, mFFTPoints[3])
            for (i in 1 until mBandsNum) {
                mLinePath.lineTo(if (i == mBandsNum - 1) mWidth.toFloat() else mFFTPoints[i * 4 + 2], mFFTPoints[i * 4 + 3])
            }
            if (glowLevel > 0) {
                canvas.drawPath(mLinePath, mGlowPaint)
            }
            canvas.drawPath(mLinePath, mPaint)
            return
        }

        val drawAsLines = when (renderType) {
            RenderType.LINES -> true
            RenderType.PATH -> false
            else -> glowLevel == 0
        }

        if (drawAsLines) {
            if (glowLevel > 0) {
                canvas.drawLines(mFFTPoints, mGlowPaint)
            }
            canvas.drawLines(mFFTPoints, mPaint)
        } else {
            mLinePath.reset()
            for (i in 0 until mBandsNum) {
                mLinePath.moveTo(mFFTPoints[i * 4], mFFTPoints[i * 4 + 1])
                mLinePath.lineTo(mFFTPoints[i * 4], mFFTPoints[i * 4 + 3])
            }
            if (glowLevel > 0) {
                canvas.drawPath(mLinePath, mGlowPaint)
            }
            canvas.drawPath(mLinePath, mPaint)
        }
    }

    fun setPlaying(playing: Boolean) {
        if (mPlaying != playing) {
            mPlaying = playing
            checkStateChanged()
        }
    }

    private suspend fun linkVisualizer() = withContext(Dispatchers.IO) {
        try {
            val visualizer = Visualizer(0)
            visualizer.enabled = false
            visualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
            visualizer.scalingMode = Visualizer.SCALING_MODE_NORMALIZED
            visualizer.setDataCaptureListener(mVisualizerListener, Visualizer.getMaxCaptureRate(), false, true)
            visualizer.enabled = true
            mVisualizer = visualizer
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    private suspend fun unlinkVisualizer() = withContext(Dispatchers.IO) {
        try {
            mVisualizer?.let {
                it.enabled = false
                it.release()
            }
            mVisualizer = null
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }

    private suspend fun runRandomizeColor() {
        while (coroutineContext.isActive && colorMode == ColorMode.DYNAMIC) {
            setColor(getRandomColor())
            delay(randomizeInterval.toLong())
        }
    }

    fun updateViewState(isPlaying: Boolean, isKeyguard: Boolean, isExpanded: Boolean) {
        isMusicPlaying = isPlaying
        isOnKeyguard = isKeyguard
        isExpandedPanel = showInDrawer && !isOnKeyguard && isExpanded
        isOnCustomLockScreen = File("/data/system/theme/lockscreen").exists()
        updatePlaying()
    }

    fun updateScreenOn(isOn: Boolean) {
        isScreenOn = isOn
        updatePlaying()
    }

    fun updateMusicArt(art: Bitmap?) {
        mArt = art
        updateColorMode()
    }

    fun updatePlaying() {
        setPlaying(isScreenOn && isMusicPlaying && ((isOnKeyguard && (!isOnCustomLockScreen || showOnCustom)) || isExpandedPanel))
    }

    private fun checkStateChanged() {
        if (mPlaying) {
            if (!mDisplaying) {
                mDisplaying = true
                viewScope.launch { linkVisualizer() }
                randomizeColorJob?.cancel()
                randomizeColorJob = viewScope.launch { runRandomizeColor() }
                animate().alpha(1.0f).withEndAction(null).setDuration((800 * animDur / 65f).roundToInt().toLong())
            }
        } else {
            if (mDisplaying) {
                mDisplaying = false
                randomizeColorJob?.cancel()
                if (isOnKeyguard) {
                    animate()
                        .alpha(0.0f)
                        .withEndAction { viewScope.launch { unlinkVisualizer() } }
                        .setDuration((600 * animDur / 65f).roundToInt().toLong())
                } else {
                    alpha = 0.0f
                    viewScope.launch { unlinkVisualizer() }
                }
            }
        }
    }
}
