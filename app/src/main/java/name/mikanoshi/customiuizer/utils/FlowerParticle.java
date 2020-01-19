package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.github.jinatonic.confetti.confetto.Confetto;
import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.confetti.ConfettoInfo;

import java.util.Random;

import name.mikanoshi.customiuizer.R;

@SuppressWarnings("FieldCanBeLocal")
public class FlowerParticle extends Confetto {
	private Float prevX;
	private Float prevY;
	private final ConfettoInfo confettoInfo;
	private Bitmap snowflake;
	private float snowScale;
	private int[] petals = new int[] { R.drawable.confetti1, R.drawable.confetti1, R.drawable.confetti2, R.drawable.confetti2, R.drawable.confetti3, R.drawable.confetti3, R.drawable.petal };

	@SuppressWarnings("ConstantConditions")
	FlowerParticle(Context context, ConfettoInfo confettoInfo) {
		super();
		this.confettoInfo = confettoInfo;
		snowScale = 0.6f - (float)Math.random() * 0.15f;
		snowflake = BitmapFactory.decodeResource(context.getResources(), petals[new Random().nextInt(petals.length)]);

		int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) snowScale *= 1.5;
	}

	public int getHeight() {
		return 0;
	}

	public int getWidth() {
		return 0;
	}

	public void reset() {
		super.reset();
		this.prevX = null;
		this.prevY = null;
	}

	protected void configurePaint(Paint paint) {
		super.configurePaint(paint);
		paint.setColor(-1);
		paint.setAntiAlias(true);
	}

	protected void drawInternal(Canvas canvas, Matrix matrix, Paint paint, float x, float y, float rotation, float percentageAnimated) {
		if (prevX == null || prevY == null) {
			prevX = x;
			prevY = y;
		}

		switch (confettoInfo.getPrecipType()) {
			case CLEAR:
				break;
			case SNOW:
				matrix.postScale(snowScale, snowScale);
				matrix.postRotate(rotation, snowflake.getWidth() / 2f, snowflake.getHeight() / 2f);
				matrix.postTranslate(x, y);
				canvas.drawBitmap(snowflake, matrix, paint);
				break;
		}
		prevX = x;
		prevY = y;
	}

	public final ConfettoInfo getConfettoInfo() {
		return this.confettoInfo;
	}
}
