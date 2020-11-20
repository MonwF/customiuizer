package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Surface;
import android.view.WindowManager;

import com.github.jinatonic.confetti.confetto.Confetto;
import com.github.matteobattilana.weather.confetti.ConfettoInfo;

import java.util.Random;

import name.mikanoshi.customiuizer.R;

@SuppressWarnings("FieldCanBeLocal")
public class FilthyParticle extends Confetto {
	private final Context mContext;
	private float startX;
	private float startY;
	private int signX;
	private int signY;
	private int distance;
	private int maxAlpha;
	private final ConfettoInfo confettoInfo;
	private final Bitmap filth;
	private final float filthScale;
	private final int[] viruses = new int[] { R.drawable.virus1, R.drawable.virus2, R.drawable.virus3 };

	@SuppressWarnings("ConstantConditions")
	private void randomizeStartPoint() {
		int width = mContext.getResources().getDisplayMetrics().widthPixels;
		int height = mContext.getResources().getDisplayMetrics().heightPixels;
		float gapX = width / 20.0f;
		float gapY = height / 15.0f;

		int rotation = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		boolean isLandscape = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
		Random rand = new Random();
		float selector = rand.nextFloat();
		if (selector < 0.25f) {
			startX = rand.nextFloat() * (isLandscape ? gapY : gapX);
			startY = rand.nextFloat() * (isLandscape ? width : height);
		} else if (selector >= 0.25f && selector < 0.5f) {
			startX = width - rand.nextFloat() * (isLandscape ? gapY : gapX);
			startY = rand.nextFloat() * (isLandscape ? width : height);
		} else if (selector >= 0.5f && selector < 0.75f) {
			startX = rand.nextFloat() * (isLandscape ? height : width);
			startY = rand.nextFloat() * (isLandscape ? gapX : gapY);
		} else {
			startX = rand.nextFloat() * (isLandscape ? height : width);
			startY = height - rand.nextFloat() * (isLandscape ? gapX : gapY);
		}
		signX = rand.nextInt(3) - 1;
		signY = rand.nextInt(3) - 1;
		maxAlpha = rand.nextInt(50) + 40;
		distance = rand.nextInt(76) + 75;
	}

	FilthyParticle(Context context, ConfettoInfo confettoInfo) {
		super();
		this.confettoInfo = confettoInfo;
		mContext = context;
		filthScale = 0.65f - new Random().nextFloat() * 0.15f;
		filth = BitmapFactory.decodeResource(context.getResources(), viruses[new Random().nextInt(viruses.length)]);
		randomizeStartPoint();
	}

	public int getHeight() {
		return 0;
	}

	public int getWidth() {
		return 0;
	}

	public void reset() {
		super.reset();
		randomizeStartPoint();
	}

	protected void configurePaint(Paint paint) {
		super.configurePaint(paint);
		paint.setColor(-1);
		paint.setAntiAlias(true);
	}

	protected void drawInternal(Canvas canvas, Matrix matrix, Paint paint, float x, float y, float rotation, float percentageAnimated) {
		matrix.postScale(filthScale, filthScale);
		matrix.postRotate(rotation, filth.getWidth() / 2f, filth.getHeight() / 2f);
		matrix.postTranslate(startX + signX * distance * percentageAnimated, startY + signY * distance * percentageAnimated);
		if (percentageAnimated < 0.1f)
			paint.setAlpha(Math.round(maxAlpha * percentageAnimated / 0.1f));
		else if (percentageAnimated > 0.9f)
			paint.setAlpha(Math.round(maxAlpha * (1.0f - percentageAnimated) / 0.1f));
		else
			paint.setAlpha(maxAlpha);
		canvas.drawBitmap(filth, matrix, paint);
	}

	public final ConfettoInfo getConfettoInfo() {
		return this.confettoInfo;
	}
}
