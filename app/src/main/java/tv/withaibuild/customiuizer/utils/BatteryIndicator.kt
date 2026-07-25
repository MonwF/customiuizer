package tv.withaibuild.customiuizer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import android.animation.ArgbEvaluator
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import kotlin.math.min
import kotlin.math.roundToInt

class BatteryIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    protected var mDisplayWidth = 0
    protected var mDensity = 0f
    protected var mStatusBarHeight = 0
    protected var mIsBeingCharged = false
    protected var mIsExtremePowerSave = false
    protected var mIsPowerSave = false
    protected val mLowLevelSystem =
        resources.getInteger(resources.getIdentifier("config_lowBatteryWarningLevel", "integer", "android"))
    protected var mPowerLevel = 0
    protected var mTestPowerLevel = 0
    private var mFullColor = Color.GREEN
    private var mLowColor = Color.RED
    private var mPowerSaveColor = Color.rgb(245, 166, 35)
    private var mChargingColor = Color.YELLOW
    private var mLowLevel = mLowLevelSystem
    private var mHeight = 5
    private var mGlow = 0
    private var mTransparency = 0
    private var mPadding = 0
    private var mVisibility = View.VISIBLE
    private var mColorMode = ColorMode.DISCRETE
    private var mTesting = false
    private var mRounded = false
    private var mCentered = false
    private var mExpanded = false
    private var mOnKeyguard = false

    private var mScreenshot = false
    private var mBottom = false
    private var mLimited = false
    private var mTintColor = Color.argb(153, 0, 0, 0)
    private var mStatusBar: Any? = null

    private val mArgbEvaluator = ArgbEvaluator()
    private val mRainbowColors = IntArray(15)
    private val mRainbowPositions = FloatArray(15)
    private val mHsv = FloatArray(3)
    private val mCornerRadii = FloatArray(8)
    private val mRectShape = RectShape()
    private var mRoundRectShape: RoundRectShape? = null
    private var mRainbowShaderFactory: ShapeDrawable.ShaderFactory? = null
    private var mShapeHeight = -1
    private var mShapeRounded = false

    private var mStatusBarHeightResId = 0

    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var testJob: Job? = null

    enum class ColorMode {
        DUMMY, DISCRETE, GRADUAL, RAINBOW
    }

    init {
        updateDisplaySize()
    }

    fun init(statusBar: Any?) {
        mStatusBar = statusBar

        try {
            val shape = ShapeDrawable()
            val paint = shape.paint
            paint.style = Paint.Style.FILL
            paint.isAntiAlias = true
            shape.setIntrinsicWidth(9999)
            setImageDrawable(shape)
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }

        updateParameters()
        ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
            override fun onChange(key: String?) {
                try {
                    if (!mTesting && key?.contains("pref_key_system_batteryindicator") == true) {
                        viewScope.launch { updateParameters(); update() }
                    }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
            }
        })

        val intentFilter = IntentFilter()
        intentFilter.addAction("tv.withaibuild.customiuizer.mods.BatteryIndicatorTest")
        if (MainModule.mPrefs.getBoolean("system_hidestatusbar_whenscreenshot")) {
            intentFilter.addAction("miui.intent.TAKE_SCREENSHOT")
        }
        context.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if ("miui.intent.TAKE_SCREENSHOT" == intent.action) {
                        val finished = intent.getBooleanExtra("IsFinished", true)
                        updateScreenShotState(!finished)
                    } else {
                        testJob?.cancel()
                        startTest()
                    }
                }
            },
            intentFilter,
            Context.RECEIVER_EXPORTED
        )
    }

    private fun startTest() {
        mTesting = true
        mTestPowerLevel = 100
        testJob?.cancel()
        testJob = viewScope.launch {
            while (true) {
                mTestPowerLevel--
                if (mTestPowerLevel >= 0) {
                    update()
                    delay(if (mTestPowerLevel == mLowLevel - 1) 300L else 20L)
                } else {
                    mTesting = false
                    delay(1000L)
                    updateParameters()
                    update()
                    break
                }
            }
        }
    }

    private fun postUpdate() {
        viewScope.launch { update() }
    }

    fun updateScreenShotState(screenshot: Boolean) {
        if (mScreenshot == screenshot) return
        mScreenshot = screenshot
        if (!mScreenshot && !mLimited) {
            this.visibility = mVisibility
        }
        update()
    }

    fun onExpandingChanged(expanded: Boolean) {
        if (mExpanded == expanded) return
        mExpanded = expanded
        update()
    }

    fun onKeyguardStateChanged(showing: Boolean) {
        if (mOnKeyguard == showing) return
        mOnKeyguard = showing
        update()
    }

    fun onDarkModeChanged(intensity: Float, tintColor: Int) {
        if (mTintColor == tintColor) return
        mTintColor = tintColor
        update()
    }

    fun onBatteryLevelChanged(powerLevel: Int, isCharging: Boolean, isCharged: Boolean) {
        if (mPowerLevel == powerLevel && mIsBeingCharged == isCharging && !isCharged) return
        mPowerLevel = powerLevel
        mIsBeingCharged = isCharging && !isCharged
        update()
    }

    fun onPowerSaveChanged(isPowerSave: Boolean) {
        if (mIsPowerSave == isPowerSave) return
        mIsPowerSave = isPowerSave
        update()
    }

    fun onExtremePowerSaveChanged(isExtremePowerSave: Boolean) {
        if (mIsExtremePowerSave == isExtremePowerSave) return
        mIsExtremePowerSave = isExtremePowerSave
        update()
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDisplaySize()
        postUpdate()
    }

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateDisplaySize()
            postUpdate()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel()
    }

    fun update() {
        if (mScreenshot) {
            this.visibility = View.GONE
        } else {
            if (mLimited) this.visibility = if (mExpanded || mOnKeyguard) mVisibility else View.GONE
        }
        clearAnimation()
        updateDrawable()
    }

    fun updateDisplaySize() {
        mDisplayWidth = measuredWidth
        mDensity = resources.displayMetrics.density
        if (mStatusBarHeightResId == 0) {
            mStatusBarHeightResId = resources.getIdentifier("status_bar_height", "dimen", "android")
        }
        mStatusBarHeight = resources.getDimensionPixelSize(mStatusBarHeightResId)
    }

    protected fun updateParameters() {
        mColorMode = ColorMode.values()[MainModule.mPrefs.getStringAsInt("system_batteryindicator_color", 1)]
        mFullColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval1", Color.GREEN)
        mLowColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval2", Color.RED)
        mPowerSaveColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval3", Color.rgb(245, 166, 35))
        mChargingColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval4", Color.YELLOW)
        mLowLevel = MainModule.mPrefs.getInt("system_batteryindicator_lowlevel", mLowLevelSystem)
        mHeight = MainModule.mPrefs.getInt("system_batteryindicator_height", 5)
        mGlow = MainModule.mPrefs.getInt("system_batteryindicator_glow", 0)
        mRounded = MainModule.mPrefs.getBoolean("system_batteryindicator_rounded")
        mBottom = MainModule.mPrefs.getStringAsInt("system_batteryindicator_align", 1) == 2
        mCentered = MainModule.mPrefs.getBoolean("system_batteryindicator_centered")
        mLimited = MainModule.mPrefs.getBoolean("system_batteryindicator_limitvis")
        mTransparency = MainModule.mPrefs.getInt("system_batteryindicator_transp", 0)
        mPadding = MainModule.mPrefs.getInt("system_batteryindicator_padding", 0)
        mVisibility = if (MainModule.mPrefs.getBoolean("system_batteryindicator")) View.VISIBLE else View.GONE

        val lp = layoutParams as FrameLayout.LayoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        lp.gravity = if (mBottom) Gravity.BOTTOM else Gravity.TOP
        layoutParams = lp

        try {
            imageAlpha = 255 - (255 * mTransparency / 100f).roundToInt()
        } catch (ignored: Throwable) {
        }
        this.visibility = mVisibility
        this.scaleType = if (mCentered) ImageView.ScaleType.CENTER else ImageView.ScaleType.MATRIX
        imageMatrix = null
        if (mColorMode == ColorMode.RAINBOW) {
            updateRainbowColors()
        }
    }

    private fun updateRainbowColors() {
        val steps = mRainbowColors.size
        val jump = 300f / steps
        for (i in 0 until steps) {
            mRainbowPositions[i] = i / (steps - 1).toFloat()
            var c = (if (mCentered) 240f else 0f) + jump * i
            if (c > 360) c -= 360f
            mHsv[0] = c
            mHsv[1] = 1.0f
            mHsv[2] = 1.0f
            mRainbowColors[i] = Color.HSVToColor(255, mHsv)
        }
    }

    protected fun updateDrawable() {
        try {
            val level = if (mTesting) mTestPowerLevel else mPowerLevel
            var color = mFullColor
            if (!mTesting && mIsBeingCharged)
                color = mChargingColor
            else if (!mTesting && (mIsPowerSave || mIsExtremePowerSave))
                color = mPowerSaveColor
            else if (level <= mLowLevel)
                color = mLowColor

            val shape = drawable as ShapeDrawable
            shape.shaderFactory = null
            val paint = shape.paint
            paint.shader = null

            if (color == Color.TRANSPARENT && mStatusBar != null) {
                try {
                    color = if (mExpanded) {
                        Color.WHITE
                    } else {
                        if (mOnKeyguard) {
                            val isLightWallpaperStatusBar = XposedHelpers.getBooleanField(
                                XposedHelpers.getObjectField(mStatusBar, "mKeyguardIndicationController"),
                                "mDarkStyle"
                            )
                            if (isLightWallpaperStatusBar) Color.argb(153, 0, 0, 0) else Color.WHITE
                        } else {
                            mTintColor
                        }
                    }
                } catch (t: Throwable) {
                    XposedHelpers.log(t)
                }
            }

            val mDisplayPadding = (mPadding / 100f * mDisplayWidth).roundToInt()

            if (mColorMode == ColorMode.GRADUAL) {
                if (level > mLowLevel && !mTesting && !mIsBeingCharged && !mIsPowerSave && !mIsExtremePowerSave) {
                    val fraction = 1f - (level - mLowLevel) / (100f - mLowLevel)
                    color = mArgbEvaluator.evaluate(fraction, color, mLowColor) as Int
                }
            } else if (mColorMode == ColorMode.RAINBOW) {
                if (mRainbowShaderFactory == null) {
                    mRainbowShaderFactory = object : ShapeDrawable.ShaderFactory() {
                        override fun resize(width: Int, height: Int): Shader {
                            return if (mCentered) {
                                LinearGradient(
                                    width / 2f - (mDisplayWidth - mDisplayPadding * 2) / 2f,
                                    height / 2f,
                                    (mDisplayWidth - mDisplayPadding * 2).toFloat(),
                                    height / 2f,
                                    mRainbowColors,
                                    mRainbowPositions,
                                    Shader.TileMode.CLAMP
                                )
                            } else {
                                LinearGradient(
                                    0f,
                                    height / 2f,
                                    (mDisplayWidth - mDisplayPadding * 2).toFloat(),
                                    height / 2f,
                                    mRainbowColors,
                                    mRainbowPositions,
                                    Shader.TileMode.CLAMP
                                )
                            }
                        }
                    }
                }
                shape.shaderFactory = mRainbowShaderFactory
            }

            paint.color = color
            if (mRounded) {
                if (!mShapeRounded || mHeight != mShapeHeight) {
                    mCornerRadii.fill(mHeight.toFloat())
                    mRoundRectShape = RoundRectShape(mCornerRadii, null, null)
                    mShapeHeight = mHeight
                    mShapeRounded = true
                }
                shape.shape = mRoundRectShape
            } else {
                shape.shape = mRectShape
                mShapeRounded = false
            }

            val mWidth = ((mDisplayWidth - mDisplayPadding * 2) * level / 100f).roundToInt()
            val density = mDensity
            val sbHeight = mStatusBarHeight
            if (mGlow == 0) {
                paint.clearShadowLayer()
                if (mBottom)
                    setPadding(mDisplayPadding, 0, mDisplayPadding, -mHeight)
                else
                    setPadding(mDisplayPadding, -mHeight, mDisplayPadding, 0)
                shape.setIntrinsicHeight(mHeight * 2)
                shape.setIntrinsicWidth(mWidth)
            } else {
                val shadowPadding = sbHeight - mHeight
                paint.setShadowLayer(
                    (mGlow / 100f) * (sbHeight - 9 * density),
                    (if (mCentered || mDisplayPadding > 0) 0f else shadowPadding / 2f),
                    (if (mBottom) mHeight - 10 else 10 - mHeight).toFloat(),
                    Color.argb(
                        min(
                            (mGlow / 100f * 255).roundToInt(),
                            (255 - mTransparency / 100f * 255).roundToInt()
                        ),
                        Color.red(color), Color.green(color), Color.blue(color)
                    )
                )
                if (mDisplayPadding == 0)
                    setPadding(
                        if (mCentered) 0 else -shadowPadding,
                        if (mBottom) shadowPadding else -shadowPadding,
                        if (mCentered) 0 else min(mDisplayWidth - mWidth, shadowPadding),
                        if (mBottom) -shadowPadding else shadowPadding
                    )
                else
                    setPadding(
                        mDisplayPadding,
                        if (mBottom) shadowPadding else -shadowPadding,
                        mDisplayPadding,
                        if (mBottom) -shadowPadding else shadowPadding
                    )
                shape.setIntrinsicHeight(sbHeight)
                shape.setIntrinsicWidth(mWidth + if (mCentered) 0 else (if (mDisplayPadding == 0) shadowPadding else 0))
            }

            invalidate()
        } catch (t: Throwable) {
            XposedHelpers.log(t)
        }
    }
}
