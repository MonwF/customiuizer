package name.monwf.customiuizer.utils;

import android.animation.ArgbEvaluator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;

public class BatteryIndicator extends androidx.appcompat.widget.AppCompatImageView {
    protected int mDisplayWidth;
    protected boolean mIsBeingCharged;
    protected boolean mIsExtremePowerSave;
    protected boolean mIsPowerSave;
    protected final int mLowLevelSystem = getResources().getInteger(getResources().getIdentifier("config_lowBatteryWarningLevel", "integer", "android"));
    protected int mPowerLevel;
    protected int mTestPowerLevel;
    private int mFullColor = Color.GREEN;
    private int mLowColor = Color.RED;
    private int mPowerSaveColor = Color.rgb(245, 166, 35);
    private int mChargingColor = Color.YELLOW;
    private int mLowLevel = mLowLevelSystem;
    private int mHeight = 5;
    private int mGlow = 0;
    private int mTransparency = 0;
    private int mPadding = 0;
    private int mVisibility = View.VISIBLE;
    private ColorMode mColorMode = ColorMode.DISCRETE;
    private boolean mTesting = false;
    private boolean mRounded = false;
    private boolean mCentered = false;
    private boolean mExpanded = false;
    private boolean mOnKeyguard = false;

    private boolean mScreenshot = false;
    private boolean mBottom = false;
    private boolean mLimited = false;
    private int mTintColor = Color.argb(153, 0, 0, 0);
    private Object mStatusBar = null;

    enum ColorMode {
        DUMMY, DISCRETE, GRADUAL, RAINBOW
    }

    public BatteryIndicator(Context context) {
        super(context);
        updateDisplaySize();
    }

    public BatteryIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        updateDisplaySize();
    }

    public void init(Object statusBar) {
        mStatusBar = statusBar;

        try {
            ShapeDrawable shape = new ShapeDrawable();
            Paint paint = shape.getPaint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            shape.setIntrinsicWidth(9999);
            setImageDrawable(shape);
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }

        updateParameters();
        ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
            public void onChange(String key) {
                try {
                    if (!mTesting && key.contains("pref_key_system_batteryindicator")) {
                        var mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(() -> {
                            updateParameters();
                            update();
                        });
                    }
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("name.monwf.customiuizer.mods.BatteryIndicatorTest");
        if (MainModule.mPrefs.getBoolean("system_hidestatusbar_whenscreenshot")) {
            intentFilter.addAction("miui.intent.TAKE_SCREENSHOT");
        }
        getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("miui.intent.TAKE_SCREENSHOT".equals(intent.getAction())) {
                    boolean finished = intent.getBooleanExtra("IsFinished", true);
                    BatteryIndicator bi = BatteryIndicator.this;
                    bi.updateScreenShotState(!finished);
                }
                else {
                    removeCallbacks(step);
                    startTest();
                }
            }
        }, intentFilter);
    }

    Runnable step = new Runnable() {
        @Override
        public void run() {
            mTestPowerLevel--;
            if (mTestPowerLevel >= 0) {
                update();
                postDelayed(step, mTestPowerLevel == mLowLevel - 1 ? 300 : 20);
            } else {
                removeCallbacks(step);
                mTesting = false;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateParameters();
                        update();
                    }
                }, 1000);
            }
        }
    };

    private void startTest() {
        mTesting = true;
        mTestPowerLevel = 100;
        post(step);
    }

//	int lightPos = 0;
//	Runnable chargingAnim = new Runnable() {
//		@Override
//		public void run() {
//			if (!mTesting) {
//				lightPos += 3;
//				if (lightPos > 180) lightPos = 0;
//				int val = (int)(90 * Math.exp(-Math.pow(lightPos - 90, 2) / 180f));
//				Paint p = ((ShapeDrawable)getDrawable()).getPaint();
//				int color = p.getColor();
//
//				int runColor = Color.WHITE;
//				if (mColorMode == ColorMode.LIGHTDARK)
//					runColor = Color.argb(
//						Color.alpha(color),
//						Math.max(0, Math.min(255, Math.round(Color.red(color) + val / 180f  * (Color.red(~color) - Color.red(color))))),
//						Math.max(0, Math.min(255, Math.round(Color.green(color) + val / 180f  * (Color.green(~color) - Color.green(color))))),
//						Math.max(0, Math.min(255, Math.round(Color.blue(color) + val / 180f * (Color.blue(~color) - Color.blue(color)))))
//					);
//				else if (mColorMode != ColorMode.RAINBOW)
//					runColor = Color.argb(
//						Color.alpha(color),
//						Math.max(0, Math.min(255, Color.red(color) + val)),
//						Math.max(0, Math.min(255, Color.green(color) + val)),
//						Math.max(0, Math.min(255, Color.blue(color) + val))
//					);
//				p.setColorFilter(new PorterDuffColorFilter(runColor, PorterDuff.Mode.SRC_ATOP));
//				invalidate();
//			}
//			if (!mIsCharged) postDelayed(chargingAnim, 33);
//		}
//	};
//
//	private void startChargingAnim() {
//		post(chargingAnim);
//	}
//
//	private void stopChargingAnim() {
//		removeCallbacks(chargingAnim);
//		((ShapeDrawable)getDrawable()).getPaint().setColorFilter(null);
//		invalidate();
//	}

    private void postUpdate() {
        post(BatteryIndicator.this::update);
    }

    public void updateScreenShotState(boolean screenshot) {
        if (mScreenshot == screenshot) return;
        mScreenshot = screenshot;
        if (!mScreenshot && !mLimited) {
            this.setVisibility(mVisibility);
        }
        update();
    }

    public void onExpandingChanged(boolean expanded) {
        if (mExpanded == expanded) return;
        mExpanded = expanded;
        update();
    }

    public void onKeyguardStateChanged(boolean showing) {
        if (mOnKeyguard == showing) return;
        mOnKeyguard = showing;
        update();
    }

    public void onDarkModeChanged(float intensity, int tintColor) {
        //if (intensity != 0.0f && intensity != 1.0f) return;
        if (mTintColor == tintColor) return;
        mTintColor = tintColor;
        update();
    }

    public void onBatteryLevelChanged(int powerLevel, boolean isCharging, boolean isCharged) {
        if (this.mPowerLevel == powerLevel && this.mIsBeingCharged == isCharging && !isCharged) return;
        this.mPowerLevel = powerLevel;
        this.mIsBeingCharged = isCharging && !isCharged;
//		if (isCharging != this.mIsCharged) {
//			this.mIsCharged = isCharging;
//			if (!this.mIsCharged)
//				startChargingAnim();
//			else
//				stopChargingAnim();
//		}
        update();
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
        if (this.mIsPowerSave == isPowerSave) return;
        this.mIsPowerSave = isPowerSave;
        update();
    }

    public void onExtremePowerSaveChanged(boolean isExtremePowerSave) {
        if (this.mIsExtremePowerSave == isExtremePowerSave ) return;
        this.mIsExtremePowerSave = isExtremePowerSave;
        update();
    }

    @Override
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateDisplaySize();
        postUpdate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateDisplaySize();
            postUpdate();
        }
    }

    public void update() {
        if (mScreenshot) {
            this.setVisibility(View.GONE);
        }
        else {
            if (mLimited) this.setVisibility(mExpanded || mOnKeyguard ? mVisibility : View.GONE);
        }
        clearAnimation();
        updateDrawable();
    }

    public void updateDisplaySize() {
        this.mDisplayWidth = getMeasuredWidth();
    }

    protected void updateParameters() {
        mColorMode = ColorMode.values()[MainModule.mPrefs.getStringAsInt("system_batteryindicator_color", 1)];
        mFullColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval1", Color.GREEN);
        mLowColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval2", Color.RED);
        mPowerSaveColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval3", Color.rgb(245, 166, 35));
        mChargingColor = MainModule.mPrefs.getInt("system_batteryindicator_colorval4", Color.YELLOW);
        mLowLevel = MainModule.mPrefs.getInt("system_batteryindicator_lowlevel", mLowLevelSystem);
        mHeight = MainModule.mPrefs.getInt("system_batteryindicator_height", 5);
        mGlow = MainModule.mPrefs.getInt("system_batteryindicator_glow", 0);
        mRounded = MainModule.mPrefs.getBoolean("system_batteryindicator_rounded");
        mBottom = MainModule.mPrefs.getStringAsInt("system_batteryindicator_align", 1) == 2;
        mCentered = MainModule.mPrefs.getBoolean("system_batteryindicator_centered");
        mLimited = MainModule.mPrefs.getBoolean("system_batteryindicator_limitvis");
        mTransparency = MainModule.mPrefs.getInt("system_batteryindicator_transp", 0);
        mPadding = MainModule.mPrefs.getInt("system_batteryindicator_padding", 0);
        mVisibility = MainModule.mPrefs.getBoolean("system_batteryindicator") ? View.VISIBLE : View.GONE;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.gravity = mBottom ? Gravity.BOTTOM : Gravity.TOP;
        setLayoutParams(lp);
        try { this.setImageAlpha(255 - Math.round(255 * mTransparency / 100f)); } catch (Throwable ignore) {};
        this.setVisibility(mVisibility);
        this.setScaleType(mCentered ? ScaleType.CENTER : ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        matrix.setTranslate(0, 0);
        matrix.setScale(1, 1);
        this.setImageMatrix(new Matrix());
    }

    protected void updateDrawable() {
        try {
            int level = this.mTesting ? this.mTestPowerLevel : this.mPowerLevel;
            int color = this.mFullColor;
            if (!this.mTesting && this.mIsBeingCharged)
                color = this.mChargingColor;
            else if (!this.mTesting && (this.mIsPowerSave || this.mIsExtremePowerSave))
                color = this.mPowerSaveColor;
            else if (level <= this.mLowLevel)
                color = this.mLowColor;

            ShapeDrawable shape = (ShapeDrawable)getDrawable();
            shape.setShaderFactory(null);
            Paint paint = shape.getPaint();
            paint.setShader(null);

            if (color == Color.TRANSPARENT && mStatusBar != null)
                try {
                    if (mExpanded) {
                        color = Color.WHITE;
                    } else {
                        if (mOnKeyguard) {
                            boolean isLightWallpaperStatusBar = XposedHelpers.getBooleanField(XposedHelpers.getObjectField(mStatusBar, "mKeyguardIndicationController"), "mDarkStyle");
                            color = isLightWallpaperStatusBar ? Color.argb(153, 0, 0, 0) : Color.WHITE;
                        } else {
                            color = mTintColor;
                        }
                    }
                } catch (Throwable t) {
                    XposedHelpers.log(t);
                }

            int mDisplayPadding = Math.round(mPadding / 100f * this.mDisplayWidth);

            if (mColorMode == ColorMode.GRADUAL) {
                color = level <= this.mLowLevel || (!this.mTesting && (this.mIsBeingCharged || this.mIsPowerSave || this.mIsExtremePowerSave)) ? color : (int)new ArgbEvaluator().evaluate(1f - (level - this.mLowLevel) / (100f - this.mLowLevel), color, mLowColor);
            } else if (mColorMode == ColorMode.RAINBOW) {
                int steps = 15;
                float jump = 300f / (float)steps;
                float[] pos = new float[steps];
                int[] rainbow = new int[steps];
                for (int i = 0; i < steps; i++) {
                    pos[i] = i / (float)(steps - 1);
                    float c = (mCentered ? 240 : 0) + jump * i;
                    if (c > 360) c -= 360;
                    rainbow[i] = Color.HSVToColor(255, new float[]{ c, 1.0f, 1.0f});
                }
                shape.setShaderFactory(new ShapeDrawable.ShaderFactory() {
                    @Override
                    public Shader resize(int width, int height) {
                        if (mCentered)
                            return new LinearGradient(width / 2f - (mDisplayWidth - mDisplayPadding * 2) / 2f, height / 2f, (mDisplayWidth - mDisplayPadding * 2), height / 2f, rainbow, pos, Shader.TileMode.CLAMP);
                        else
                            return new LinearGradient(0, height / 2f, (mDisplayWidth - mDisplayPadding * 2), height / 2f, rainbow, pos, Shader.TileMode.CLAMP);
                    }
                });
            }
            paint.setColor(color);
            shape.setShape(mRounded ? new RoundRectShape(new float[] { mHeight, mHeight, mHeight, mHeight, mHeight, mHeight, mHeight, mHeight }, null, null) : new RectShape());

            int mWidth = Math.round((this.mDisplayWidth - mDisplayPadding * 2) * level / 100f);
            float mDensity = getResources().getDisplayMetrics().density;
            int sbHeight = getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"));
            if (mGlow == 0) {
                paint.clearShadowLayer();
                if (mBottom)
                    setPadding(mDisplayPadding, 0, mDisplayPadding, -mHeight);
                else
                    setPadding(mDisplayPadding, -mHeight, mDisplayPadding, 0);
                shape.setIntrinsicHeight(mHeight * 2);
                shape.setIntrinsicWidth(mWidth);
            } else {
                int shadowPadding = sbHeight - mHeight;
                paint.setShadowLayer(
                    (mGlow / 100f) * (sbHeight - 9 * mDensity),
                    (mCentered || mDisplayPadding > 0) ? 0 : shadowPadding / 2f,
                    mBottom ? mHeight - 10 : 10 - mHeight,
                    Color.argb(Math.min(Math.round(mGlow / 100f * 255), Math.round(255 - mTransparency / 100f * 255)), Color.red(color), Color.green(color), Color.blue(color))
                );
                if (mDisplayPadding == 0)
                    setPadding(mCentered ? 0 : -shadowPadding, mBottom ? shadowPadding : -shadowPadding, mCentered ? 0 : Math.min(mDisplayWidth - mWidth, shadowPadding), mBottom ? -shadowPadding : shadowPadding);
                else
                    setPadding(mDisplayPadding, mBottom ? shadowPadding : -shadowPadding, mDisplayPadding, mBottom ? -shadowPadding : shadowPadding);
                shape.setIntrinsicHeight(sbHeight);
                shape.setIntrinsicWidth(mWidth + (mCentered ? 0 : (mDisplayPadding == 0 ? shadowPadding : 0)));
            }

            invalidate();
        } catch (Throwable t) {
            XposedHelpers.log(t);
        }
    }

}