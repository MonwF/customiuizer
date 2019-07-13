package name.mikanoshi.customiuizer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import name.mikanoshi.customiuizer.R;

public class ColorCircle extends View {

	private float posX;
	private float posY;
	private float posXin;
	private float posYin;
	private float radius;
	private float innerRadius;
	private Bitmap bitmap;
	private int offset;
	private Paint paint1;
	private Paint paint2;
	private ColorListener listener;
	private int mColor;

	public ColorCircle(Context context) {
		this(context, null);
	}

	public ColorCircle(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public ColorCircle(Context context, AttributeSet attributeSet, int i) {
		super(context, attributeSet, i);
		init(context);
	}

	public int getColor() {
		return mColor;
	}

	public void saveCirclePosition() {
		Helpers.prefs.edit().putFloat("visualizer_circle_x", this.posX).putFloat("visualizer_circle_y", this.posY)
							.putFloat("visualizer_circle_x_in", this.posXin).putFloat("visualizer_circle_y_in", this.posYin).apply();
	}

	private void init(Context context) {
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		this.paint1 = new Paint();
		this.paint2 = new Paint();
		this.bitmap = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.palette, context.getTheme())).getBitmap();
		this.offset = (int)context.getResources().getDimension(R.dimen.screen_color_preview_offset);
		float diameter = context.getResources().getDimension(R.dimen.screen_color_preview_diameter);
		this.radius = diameter / 2.0f - 3.0f;
		this.innerRadius = this.radius - this.offset;
		this.posX = Helpers.prefs.getFloat("visualizer_circle_x", diameter / 2.0f);
		this.posY = Helpers.prefs.getFloat("visualizer_circle_y", diameter / 2.0f);
		this.posXin = Helpers.prefs.getFloat("visualizer_circle_x_in", diameter / 2.0f);
		this.posYin = Helpers.prefs.getFloat("visualizer_circle_y_in", diameter / 2.0f);
		this.mColor = this.bitmap.getPixel((int)this.posX, (int)this.posY);
		postInvalidate();
	}

	public interface ColorListener {
		void onColorSelected(int color);
	}

	public void setListener(ColorListener colorListener) {
		this.listener = colorListener;
	}

	private float distanceToCenter(float f, float f2) {
		return (float)Math.sqrt(Math.pow((double)(this.radius - f), 2.0d) + Math.pow((double)(this.radius - f2), 2.0d));
	}

	private float distanceToInnerCenter(float f, float f2) {
		return (float)Math.sqrt(Math.pow((double)(this.radius - f), 2.0d) + Math.pow((double)(this.radius - f2), 2.0d));
	}

	private boolean isInCircle(float f, float f2) {
		return distanceToCenter(f, f2) <= this.radius;
	}

	private boolean isInInnerCircle(float f, float f2) {
		return distanceToInnerCenter(f, f2) <= this.innerRadius;
	}

	private void limitByCircle(float f, float f2) {
		float d = distanceToCenter(f, f2);
		float abs = (Math.abs(f - this.radius) * this.radius) / d;
		float abs2 = (Math.abs(f2 - this.radius) * this.radius) / d;
		float correction = 1.5f;
		if (f > this.radius) {
			if (f2 > this.radius) {
				this.posY = this.radius + abs2 + correction;
			} else if (f2 < this.radius) {
				this.posY = this.radius - abs2 + correction;
			}
			this.posX = this.radius + abs + correction;
		} else if (f < this.radius) {
			if (f2 - this.radius > 0.0f) {
				this.posY = this.radius + abs2 + correction;
			} else if (f2 - this.radius < 0.0f) {
				this.posY = this.radius - abs2 + correction;
			}
			this.posX = this.radius - abs + correction;
		}
	}

	private void limitByInnerCircle(float f, float f2) {
		float d = distanceToInnerCenter(f, f2);
		float abs = (Math.abs(f - this.innerRadius) * this.innerRadius) / d;
		float abs2 = (Math.abs(f2 - this.innerRadius) * this.innerRadius) / d;
		float correction = 1.5f + this.offset / 2f;
		if (f > this.innerRadius) {
			if (f2 > this.innerRadius) {
				this.posYin = this.innerRadius + abs2 + correction;
			} else if (f2 < this.innerRadius) {
				this.posYin = this.innerRadius - abs2 + correction;
			}
			this.posXin = this.innerRadius + abs + correction;
		} else if (f < this.innerRadius) {
			if (f2 - this.innerRadius > 0.0f) {
				this.posYin = this.innerRadius + abs2 + correction;
			} else if (f2 - this.innerRadius < 0.0f) {
				this.posYin = this.innerRadius - abs2 + correction;
			}
			this.posXin = this.innerRadius - abs + correction;
		}
	}

	public boolean dispatchTouchEvent(MotionEvent motionEvent) {
		getParent().requestDisallowInterceptTouchEvent(true);
		if (!isEnabled()) return true;

		float x = motionEvent.getX();
		float y = motionEvent.getY();

		if (isInCircle(x, y)) {
			this.posX = x;
			this.posY = y;
		} else {
			limitByCircle(x, y);
		}

		if (isInInnerCircle(x, y)) {
			this.posXin = x;
			this.posYin = y;
		} else {
			limitByInnerCircle(x, y);
		}

		this.posX = Math.max(0, Math.min(bitmap.getWidth() - 1, Math.round(this.posX)));
		this.posY = Math.max(0, Math.min(bitmap.getHeight() - 1, Math.round(this.posY)));
		this.mColor = this.bitmap.getPixel((int)this.posX, (int)this.posY);
		if (this.listener != null) this.listener.onColorSelected(this.mColor);
		postInvalidate();
		return true;
	}

	@SuppressLint("DrawAllocation")
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.paint1.setMaskFilter(null);
		canvas.drawBitmap(this.bitmap, null, new Rect(this.offset, this.offset, getWidth() - this.offset, getHeight() - this.offset), this.paint1);
		this.paint2.setColor(Color.CYAN);
		this.paint2.setStrokeWidth(2.0f);
		this.paint2.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(this.posXin, this.posYin, (float)this.offset, this.paint2);
		this.paint2.setColor(Color.WHITE);
		this.paint2.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas.drawCircle(this.posXin, this.posYin, (float)(this.offset - 2), this.paint2);
	}

}