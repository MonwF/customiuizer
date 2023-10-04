package name.monwf.customiuizer.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.palette.graphics.Palette;

import java.io.File;

import name.monwf.customiuizer.MainModule;
import name.monwf.customiuizer.mods.utils.ModuleHelper;
import name.monwf.customiuizer.mods.utils.XposedHelpers;

public class AudioVisualizer extends View {

	private int mHeight;
	private int mWidth;
	private final float mDensity;
	private final Paint mPaint;
	private Paint mGlowPaint;
	private Visualizer mVisualizer;
	private ObjectAnimator mVisualizerColorAnimator;
	private ObjectAnimator mVisualizerGlowColorAnimator;

	private final ValueAnimator[] mValueAnimators;
	private final float[] mFFTPoints;
	private final float[] mBands = {50, 90, 130, 180, 220, 260, 320, 380, 430, 520, 610, 700, 770, 920, 1080, 1270, 1480, 1720, 2000, 2320, 2700, 3135, 3700, 4400, 5300, 6400, 7700, 9500, 10500, 12000, 16000};
	private float maxDb = 50;
	private final int maxDp = 280;

	private boolean isMusicPlaying = false;
	public boolean isScreenOn = false;
	private boolean isOnKeyguard = false;
	private boolean isExpandedPanel = false;
	private boolean isOnCustomLockScreen = false;
	private boolean mPlaying;
	private boolean mDisplaying;
	private int mOpaqueColor;
	private int mColor;
	private final Handler mHandler;
	private Bitmap mArt;
	private Bitmap mProcessedArt;
	private final int mBandsNum = 31;
	private final int[] mRainbow = new int[mBandsNum];
	private final int[] mRainbowVertical = new int[mBandsNum];
	private final float[] mPositions = new float[mBandsNum];
	private final Path mLinePath = new Path();
	public boolean showOnCustom;
	private int animDur;
	private int transparency;
	public ColorMode colorMode;
	public BarStyle barStyle;
	public RenderType renderType;
	public int glowLevel;
	public int customColor;
	private int randomizeInterval;
	public boolean showInDrawer;
	public boolean showWithControllerOnly;

	private final AccelerateInterpolator accel = new AccelerateInterpolator();
	private final DecelerateInterpolator decel = new DecelerateInterpolator();
	private final Runnable randomizeColor = new Runnable() {
		@Override
		public void run() {
			if (colorMode != ColorMode.DYNAMIC) return;
			setColor(getRandomColor());
			mHandler.removeCallbacks(randomizeColor);
			mHandler.postDelayed(randomizeColor, randomizeInterval);
		}
	};

	int getRandomColor() {
		return Color.HSVToColor(new float[] {(float)(Math.random() * 360f), (float)(0.5f + Math.random() * 0.5f), (float)(0.75f + Math.random() * 0.25f) });
	}

	enum BarStyle {
		DUMMY, SOLID, SOLID_ROUNDED, DASHED, CIRCLES, LINE
	}

	enum ColorMode {
		DUMMY, MATCH, STATIC, RAINBOW_H, RAINBOW_V, DYNAMIC
	}

	enum RenderType {
		AUTO, LINES, PATH
	}

	public static boolean allZeros(byte[] array) {
		for (byte item: array) if (item != 0) return false;
		return true;
	}

	private final Visualizer.OnDataCaptureListener mVisualizerListener = new Visualizer.OnDataCaptureListener() {
		byte real, imaginary;
		int dbValue;
		float magnitude;

		@Override
		public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}

		@Override
		public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
			try {
				float bandWidth = (float)samplingRate / (float)fft.length;
				int band = 0;
				int i = 1;
				float maxHeight = Math.min(maxDp * mDensity, mHeight / 2.0f);

				while (band < mBandsNum && i < fft.length / 2) {
					//int n = 0;
					magnitude = 0;

					if (!allZeros(fft))
					while (i < fft.length / 2 && (i * bandWidth <= mBands[band] * samplingRate / 44100f)) {
						real = fft[i * 2];
						imaginary = fft[i * 2 + 1];
						magnitude = Math.max(magnitude, real * real + imaginary * imaginary);
						//n++;
						i++;
					}
					//magnitude /= n;

					//float amp = 0.75f + 0.5f * band / 31f;
					dbValue = magnitude > 0 ? (int)(10 * Math.log10(magnitude)) : 0;
					maxDb = Math.max(maxDb, dbValue);
					float oldVal = mFFTPoints[band * 4 + 3];
					float newVal = mFFTPoints[band * 4 + 1] - maxHeight * dbValue / maxDb;

					mValueAnimators[band].cancel();
					mValueAnimators[band].setInterpolator(newVal < oldVal ? decel : accel);
					mValueAnimators[band].setFloatValues(oldVal, newVal);
					mValueAnimators[band].start();

					band++;
				}
			} catch (Throwable t) {
				XposedHelpers.log(t);
			}
		}
	};

	private final Runnable mLinkVisualizer = new Runnable() {
		@Override
		public void run()  {
			try {
				mVisualizer = new Visualizer(0);
				mVisualizer.setEnabled(false);
				mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
				mVisualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
				mVisualizer.setDataCaptureListener(mVisualizerListener, Visualizer.getMaxCaptureRate(), false, true);
				mVisualizer.setEnabled(true);
			} catch (Throwable t){
				XposedHelpers.log(t);
			}

		}
	};

	private final Runnable mUnlinkVisualizer = new Runnable() {
		@Override
		public void run() {
			if (mVisualizer != null) try {
				mVisualizer.setEnabled(false);
				mVisualizer.release();
				mVisualizer = null;
			} catch (Throwable t){
				XposedHelpers.log(t);
			}
		}
	};

	private void updateGlowPaint() {
		mGlowPaint = new Paint(mPaint);
		if (glowLevel == 0) return;
		float scale = glowLevel / 100f;
		mGlowPaint.setPathEffect(null);
		mGlowPaint.setMaskFilter(new BlurMaskFilter(15 * mDensity * (1.25f + 0.25f * scale), BlurMaskFilter.Blur.NORMAL));
		mGlowPaint.setAlpha(Math.min(transparency, 180));
		mGlowPaint.setStrokeWidth((0.5f + 1.25f * scale) * mPaint.getStrokeWidth() * (barStyle == BarStyle.LINE ? 4f : (colorMode == ColorMode.RAINBOW_H ? 1.15f : 1.3f)));
		if (barStyle == BarStyle.SOLID || barStyle == BarStyle.DASHED || mGlowPaint.getStrokeCap() == Paint.Cap.ROUND) {
			mGlowPaint.setStrokeCap(Paint.Cap.SQUARE);
		}
	}

	public AudioVisualizer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayerType(View.LAYER_TYPE_HARDWARE, null);

		Resources res = context.getResources();
		mHeight = res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? res.getDisplayMetrics().heightPixels : res.getDisplayMetrics().widthPixels;
		mWidth = res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? res.getDisplayMetrics().widthPixels : res.getDisplayMetrics().heightPixels;
		mDensity = res.getDisplayMetrics().density;
		mColor = Color.TRANSPARENT;
		mOpaqueColor = Color.TRANSPARENT;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.MITER);
		mPaint.setColor(mColor);

		animDur = MainModule.mPrefs.getInt("system_visualizer_animdur", 65);
		mFFTPoints = new float[128];
		mValueAnimators = new ValueAnimator[mBandsNum];
		for (int i = 0; i < mBandsNum; i++) {
			final int j = i * 4 + 3;
			mValueAnimators[i] = new ValueAnimator();
			mValueAnimators[i].setDuration(animDur);
			mValueAnimators[i].addUpdateListener(animation -> {
				mFFTPoints[j] = (float)animation.getAnimatedValue();
				postInvalidate();
			});
			mPositions[i] = (i + 1) / (float)mBandsNum;
		}

		showOnCustom = MainModule.mPrefs.getBoolean("system_visualizer_custom");
		transparency = Math.round(255f - 255f * MainModule.mPrefs.getInt("system_visualizer_transp", 40) / 100f);
		colorMode = ColorMode.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_color", 1)];
		barStyle = BarStyle.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_style", 1)];
		renderType = RenderType.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_render", 0)];
		glowLevel = MainModule.mPrefs.getInt("system_visualizer_glowlevel", 50);
		customColor = MainModule.mPrefs.getInt("system_visualizer_colorval", Color.WHITE);
		randomizeInterval = MainModule.mPrefs.getInt("system_visualizer_dyntime", 10) * 1000;
		showInDrawer = MainModule.mPrefs.getBoolean("system_visualizer_drawer");
		showWithControllerOnly = MainModule.mPrefs.getBoolean("system_visualizer_controller");
		updateBarStyle();
		updateGlowPaint();
		updateRainbowColors();

		mHandler = new Handler(context.getMainLooper());
		ModuleHelper.observePreferenceChange(new ModuleHelper.PreferenceObserver() {
			public void onChange(String key) {
				try {
					switch (key) {
						case "pref_key_system_visualizer_animdur":
							animDur = MainModule.mPrefs.getInt("system_visualizer_animdur", 65);
							for (int i = 0; i < mBandsNum; i++)
							mValueAnimators[i].setDuration(animDur);
							break;
						case "pref_key_system_visualizer_transp":
							transparency = Math.round(255f - 255f * MainModule.mPrefs.getInt("system_visualizer_transp", 40) / 100f);
							setColor(mOpaqueColor);
							updateRainbowColors();
							break;
						case "pref_key_system_visualizer_color":
							colorMode = ColorMode.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_color", 1)];
							updateBarStyle();
							updateColorMode();
							break;
						case "pref_key_system_visualizer_style":
							barStyle = BarStyle.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_style", 1)];
							updateBarStyle();
							break;
						case "pref_key_system_visualizer_render":
							renderType = RenderType.values()[MainModule.mPrefs.getStringAsInt("system_visualizer_render", 0)];
							updateBarStyle();
							break;
						case "pref_key_system_visualizer_glowlevel":
							glowLevel = MainModule.mPrefs.getInt("system_visualizer_glowlevel", 50);
							updateGlowPaint();
							break;
						case "pref_key_system_visualizer_colorval":
							customColor = MainModule.mPrefs.getInt("system_visualizer_colorval", Color.WHITE);
							setColor(customColor);
							break;
						case "pref_key_system_visualizer_dyntime":
							randomizeInterval = MainModule.mPrefs.getInt("system_visualizer_dyntime", 10) * 1000;
							mHandler.removeCallbacks(randomizeColor);
							mHandler.post(randomizeColor);
							break;
						case "pref_key_system_visualizer_drawer":
							showInDrawer = MainModule.mPrefs.getBoolean("system_visualizer_drawer", false);
							break;
						case "pref_key_system_visualizer_controller":
							showWithControllerOnly = MainModule.mPrefs.getBoolean("system_visualizer_controller", false);
							break;
					}
				} catch (Throwable t) {
					XposedHelpers.log(t);
				}
			}
		});
	}

	public AudioVisualizer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AudioVisualizer(Context context) {
		this(context, null, 0);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mArt = null;
		mProcessedArt = null;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		float barUnit = w / (float)mBandsNum;
		float barWidth = barUnit * 0.80f;
		mHeight = h;
		mWidth = w;
		mPaint.setStrokeWidth(barWidth);
		updateBarStyle();

		for (int i = 0; i < mBandsNum; i++) {
			mFFTPoints[i * 4] = mFFTPoints[i * 4 + 2] = i * barUnit + (barWidth / 2);
			mFFTPoints[i * 4 + 1] = h;
			mFFTPoints[i * 4 + 3] = h;
		}
	}

	@Override
	public boolean hasOverlappingRendering() {
		return mDisplaying;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		try {
			if (mVisualizer == null || !mVisualizer.getEnabled()) return;
		} catch (Throwable t) { return; }

		if (barStyle == BarStyle.LINE) {
			mLinePath.reset();
			mLinePath.moveTo(0, mFFTPoints[3]);
			for (int i = 1; i < mBandsNum; i++)
			mLinePath.lineTo(i == mBandsNum - 1 ? mWidth : mFFTPoints[i * 4 + 2], mFFTPoints[i * 4 + 3]);
			if (glowLevel > 0)
			canvas.drawPath(mLinePath, mGlowPaint);
			canvas.drawPath(mLinePath, mPaint);
			return;
		}

		boolean drawAsLines;
		if (renderType == RenderType.LINES)
			drawAsLines = true;
		else if (renderType == RenderType.PATH)
			drawAsLines = false;
		else
			drawAsLines = glowLevel == 0;

		if (drawAsLines) {
			if (glowLevel > 0)
			canvas.drawLines(mFFTPoints, mGlowPaint);
			canvas.drawLines(mFFTPoints, mPaint);
		} else {
			mLinePath.reset();
			for (int i = 0; i < mBandsNum; i++) {
				mLinePath.moveTo(mFFTPoints[i * 4], mFFTPoints[i * 4 + 1]);
				mLinePath.lineTo(mFFTPoints[i * 4], mFFTPoints[i * 4 + 3]);
			}
			if (glowLevel > 0)
			canvas.drawPath(mLinePath, mGlowPaint);
			canvas.drawPath(mLinePath, mPaint);
		}
	}

	public void setPlaying(boolean playing) {
		if (mPlaying != playing) {
			mPlaying = playing;
			checkStateChanged();
		}
	}

	public interface PaletteAsyncListener {
		void onGenerated(Palette palette);
	}

	private final PaletteAsyncListener paletteResult = new PaletteAsyncListener() {
		@Override
		public void onGenerated(Palette palette) {
			try {
				int color = Color.TRANSPARENT;
				color = palette.getLightVibrantColor(color);
				if (color == Color.TRANSPARENT) color = palette.getVibrantColor(color);
				if (color == Color.TRANSPARENT) color = palette.getDarkVibrantColor(color);
				setColor(color);
			} catch (Throwable t) {
				XposedHelpers.log(t);
			}
		}
	};

	private static class PaletteTask extends AsyncTask<Bitmap, Void, Palette> {
		PaletteAsyncListener resultListener;

		PaletteTask(PaletteAsyncListener listener) {
			resultListener = listener;
		}

		@Override
		protected Palette doInBackground(Bitmap... bitmaps) {
			try {
				return Palette.from(bitmaps[0]).generate();
			} catch (Throwable t) {
				XposedHelpers.log(t);
				return null;
			}
		}

		public void onPostExecute(Palette palette) {
			resultListener.onGenerated(palette);
		}
	}

	public void setBitmap() {
		try {
			if (mProcessedArt != null && mArt != null && !mProcessedArt.isRecycled() && !mArt.isRecycled() && mProcessedArt.sameAs(mArt)) return;
			mProcessedArt = mArt;
			if (mProcessedArt != null) {
				new PaletteTask(paletteResult).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mProcessedArt);
			} else {
				setColor(Color.TRANSPARENT);
			}
		} catch (Throwable t) {
			XposedHelpers.log(t);
		}
	}

	public void setColor(int color) {
		if (color == Color.TRANSPARENT) color = Color.WHITE;
		int newColor = Color.argb(transparency, Color.red(color), Color.green(color), Color.blue(color));
		if (mColor == newColor) return;
		mColor = newColor;
		mOpaqueColor = color;
		if (mVisualizer != null) {
			if (mVisualizerColorAnimator != null) mVisualizerColorAnimator.cancel();
			mVisualizerColorAnimator = ObjectAnimator.ofArgb(mPaint, "color", mPaint.getColor(), mColor);
			mVisualizerColorAnimator.setStartDelay(Math.round(600 * animDur / 65f));
			mVisualizerColorAnimator.setDuration(Math.round(1200 * animDur / 65f));
			mVisualizerColorAnimator.start();

			if (glowLevel > 0) {
				if (mVisualizerGlowColorAnimator != null) mVisualizerGlowColorAnimator.cancel();
				mVisualizerGlowColorAnimator = ObjectAnimator.ofArgb(mGlowPaint, "color", mGlowPaint.getColor(), mColor);
				mVisualizerGlowColorAnimator.setStartDelay(Math.round(600 * animDur / 65f));
				mVisualizerGlowColorAnimator.setDuration(Math.round(1200 * animDur / 65f));
				mVisualizerGlowColorAnimator.start();
			}
		} else {
			mPaint.setColor(mColor);
			if (glowLevel > 0) mGlowPaint.setColor(mColor);
		}
	}

	private void updateColorMode() {
		if (!isMusicPlaying) return;
		if (colorMode == ColorMode.MATCH)
			setBitmap();
		else if (colorMode == ColorMode.DYNAMIC)
			setColor(getRandomColor());
		else if (colorMode == ColorMode.STATIC)
			setColor(customColor);
		else
			setColor(Color.WHITE);
	}

	private void updateRainbowColors() {
		float jump = 300f / (float)mBandsNum;
		for (int i = 0; i < mRainbow.length; i++)
			mRainbow[i] = Color.HSVToColor(transparency, new float[]{jump * i, 1.0f, 1.0f});

		for (int i = 0; i < mRainbowVertical.length; i++) {
			float h = 140 + jump * i;
			if (h > 360) h -= 360;
			mRainbowVertical[i] = Color.HSVToColor(transparency, new float[]{h, 1.0f, 1.0f});
		}
	}

	private void updateBarStyle() {
		if (colorMode == ColorMode.RAINBOW_H)
			mPaint.setShader(new LinearGradient(0, 0, mWidth, 0, mRainbow, mPositions, Shader.TileMode.MIRROR));
		else if (colorMode == ColorMode.RAINBOW_V) {
			float maxHeight = Math.min(0.85f * maxDp * mDensity, mHeight / 2.0f);
			mPaint.setShader(new LinearGradient(0, mHeight, 0, mHeight - maxHeight, mRainbowVertical, mPositions, Shader.TileMode.CLAMP));
		} else
			mPaint.setShader(null);

		if (barStyle == BarStyle.SOLID) {
			mPaint.setPathEffect(null);
			mPaint.setStrokeCap(Paint.Cap.BUTT);
		} else if (barStyle == BarStyle.SOLID_ROUNDED) {
			mPaint.setPathEffect(null);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
		} else if (barStyle == BarStyle.DASHED) {
			mPaint.setPathEffect(new DashPathEffect(new float[]{4 * mDensity, 2 * mDensity}, 0));
			mPaint.setStrokeCap(Paint.Cap.BUTT);
		} else if (barStyle == BarStyle.CIRCLES) {
			mPaint.setPathEffect(new DashPathEffect(new float[]{1.0f, mPaint.getStrokeWidth() + mDensity}, 0));
			mPaint.setStrokeCap(Paint.Cap.ROUND);
		} else if (barStyle == BarStyle.LINE) {
			mPaint.setPathEffect(new CornerPathEffect(18 * mDensity));
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(3 * mDensity);
		}

		updateGlowPaint();
	}

	public void updateViewState(boolean isPlaying, boolean isKeyguard, boolean isExpanded) {
		isMusicPlaying = isPlaying;
		isOnKeyguard = isKeyguard;
		isExpandedPanel = showInDrawer && !isOnKeyguard && isExpanded;
		isOnCustomLockScreen = new File("/data/system/theme/lockscreen").exists();
		updatePlaying();
	}

	public void updateScreenOn(boolean isOn) {
		isScreenOn = isOn;
		updatePlaying();
	}

	public void updateMusicArt(Bitmap art) {
		mArt = art;
		updateColorMode();
	}

	public void updatePlaying() {
		setPlaying(isScreenOn && isMusicPlaying && ((isOnKeyguard && (!isOnCustomLockScreen || showOnCustom)) || isExpandedPanel));
	}

	private void checkStateChanged() {
		if (mPlaying) {
			if (!mDisplaying) {
				mDisplaying = true;
				AsyncTask.execute(mLinkVisualizer);
				mHandler.removeCallbacks(randomizeColor);
				mHandler.postDelayed(randomizeColor, randomizeInterval);
				animate().alpha(1.0f).withEndAction(null).setDuration(Math.round(800 * animDur / 65f));
			}
		} else {
			if (mDisplaying) {
				mDisplaying = false;
				mHandler.removeCallbacks(randomizeColor);
				if (isOnKeyguard) {
					animate().alpha(0.0f).withEndAction(new Runnable() {
						@Override
						public void run() {
							AsyncTask.execute(mUnlinkVisualizer);
						}
					}).setDuration(Math.round(600 * animDur / 65f));
				} else {
					setAlpha(0.0f);
					AsyncTask.execute(mUnlinkVisualizer);
				}
			}
		}
	}

}
