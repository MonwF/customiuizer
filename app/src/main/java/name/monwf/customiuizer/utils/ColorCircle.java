package name.monwf.customiuizer.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import name.monwf.customiuizer.R;

public class ColorCircle extends View {

	private float posXin;
	private float posYin;
	private float radius;
	private float innerRadius;
	private int offset;
	private int alphaVal;
	private final Paint paint1 = new Paint();
	private final Paint paint1a = new Paint();
	private final Paint paint2 = new Paint();
	private final Paint paint3 = new Paint();
	private ColorListener listener;
	private boolean mTransparent = false;
	private final float[] mColor = new float[3];
	private boolean initialized = false;

	public ColorCircle(Context context) {
		this(context, null);
	}

	public ColorCircle(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public ColorCircle(Context context, AttributeSet attributeSet, int i) {
		super(context, attributeSet, i);
	}

	public int getColor() {
		if (this.mTransparent) {
			return Color.TRANSPARENT;
		}
		int resColor = Color.HSVToColor(this.mColor);
		return (this.alphaVal << 24) | (resColor & 0x00FFFFFF);
	}

	public void setColor(int color) {
		this.setColor(color, false);
	}

	public void setColor(int color, boolean setAlpha) {
		this.mTransparent = color == Color.TRANSPARENT;
		Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), this.mColor);
		if (setAlpha) {
			this.alphaVal = Color.alpha(color);
		}
		if (this.listener != null) this.listener.onColorSelected(getColor());
		PointF coords = getPointForColor();
		updatePickerPos(coords.x, coords.y);
		postInvalidate();
	}

	public void setAlphaVal(int alphaV) {
		this.alphaVal = alphaV;
		if (this.listener != null) this.listener.onColorSelected(getColor());
	}

	private void update() {
		float diameter = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels) *
						(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 0.7f : 0.8f);
		this.radius = diameter / 2.0f;
		this.innerRadius = this.radius - this.offset * 2;
		this.getLayoutParams().width = Math.round(diameter);
		this.getLayoutParams().height = Math.round(diameter);

		int steps = 6;
		int[] colors = new int[steps + 1];
		float[] hsv = new float[] { 0.0f, 1.0f, 1.0f };
		for (int i = 0; i < steps; i++) {
			hsv[0] = (360f / steps) * i;
			colors[i] = Color.HSVToColor(hsv);
		}
		colors[steps] = colors[0];

		this.paint1.setShader(new SweepGradient(this.radius, this.radius, colors, null));
		this.paint1a.setShader(new RadialGradient(this.radius, this.radius, this.radius, 0xFFFFFFFF, 0x00FFFFFF, android.graphics.Shader.TileMode.CLAMP));
	}

	public PointF getPointForColor() {
		float hue = this.mColor[0];
		float sat = this.mColor[1];
		PointF point = new PointF();
		point.x = (float)(this.radius + this.radius * sat * Math.cos(Math.toRadians(hue)));
		point.y = (float)(this.radius + this.radius * sat * Math.sin(Math.toRadians(hue)));
		return point;
	}

	public void getColorForPoint(int x, int y) {
		x -= this.radius;
		y -= this.radius;
		this.mColor[0] = (float)(Math.toDegrees(Math.atan2(y, x)) + 360f) % 360f;
		this.mColor[1] = Math.max(0f, Math.min(1f, (float)(Math.hypot(x, y) / this.radius)));
		this.mTransparent = false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		update();
		postInvalidate();
	}

	public void init(int prefColor) {
		this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		this.paint1.setAntiAlias(true);
		this.paint1a.setAntiAlias(true);
		this.paint2.setAntiAlias(true);
		this.paint2.setColor(Color.CYAN);
		this.paint2.setStrokeWidth(2.0f);
		this.paint2.setStyle(Paint.Style.STROKE);
		this.paint3.setAntiAlias(true);
		this.paint3.setColor(Color.WHITE);
		this.paint3.setStyle(Paint.Style.FILL_AND_STROKE);
		this.offset = (int)getResources().getDimension(R.dimen.screen_color_preview_offset);
		this.mTransparent = prefColor == Color.TRANSPARENT;
		this.alphaVal = Color.alpha(prefColor);
		Color.RGBToHSV(Color.red(prefColor), Color.green(prefColor), Color.blue(prefColor), this.mColor);
		update();
		PointF coords = getPointForColor();
		updatePickerPos(coords.x, coords.y);
		initialized = true;
		postInvalidate();
	}

	public void setValue(float value) {
		this.mTransparent = false;
		this.mColor[2] = value;
		if (this.listener != null) this.listener.onColorSelected(getColor());
		postInvalidate();
	}

	public interface ColorListener {
		void onColorSelected(int color);
	}

	public void setListener(ColorListener colorListener) {
		this.listener = colorListener;
	}

	private float distanceToCenter(float f, float f2) {
		return (float)Math.hypot((double)(this.radius - f), (double)(this.radius - f2));
	}

	private boolean isInCircle(float f, float f2, float radius) {
		return distanceToCenter(f, f2) <= radius;
	}

	private void limitByCircle(float f, float f2, float radius) {
		float angle = (float)Math.atan2(f - this.radius, f2 - this.radius);
		this.posXin = radius + (float)(radius * Math.sin(angle)) + this.offset * 2;
		this.posYin = radius + (float)(radius * Math.cos(angle)) + this.offset * 2;
	}

	private void updatePickerPos(float x, float y) {
		if (isInCircle(x, y, this.innerRadius)) {
			this.posXin = x;
			this.posYin = y;
		} else {
			limitByCircle(x, y, this.innerRadius);
		}
	}

	public boolean dispatchTouchEvent(MotionEvent motionEvent) {
		if (!initialized) return false;

		getParent().requestDisallowInterceptTouchEvent(true);
		if (!isEnabled()) return true;

		float x = motionEvent.getX();
		float y = motionEvent.getY();
		updatePickerPos(x, y);
		getColorForPoint((int)x, (int)y);
		if (this.listener != null) this.listener.onColorSelected(getColor());
		postInvalidate();

		return true;
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!initialized) return;
		canvas.drawCircle(this.radius, this.radius, this.radius - this.offset * 2, paint1);
		canvas.drawCircle(this.radius, this.radius, this.radius - this.offset * 2, paint1a);
		canvas.drawCircle(this.posXin, this.posYin, (float)this.offset, this.paint2);
		canvas.drawCircle(this.posXin, this.posYin, (float)(this.offset - 2), this.paint3);
	}

}