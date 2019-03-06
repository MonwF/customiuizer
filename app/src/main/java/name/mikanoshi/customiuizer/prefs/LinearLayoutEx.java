package name.mikanoshi.customiuizer.prefs;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class LinearLayoutEx extends LinearLayout {
	public LinearLayoutEx(Context context) {
		super(context);
	}

	public LinearLayoutEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LinearLayoutEx(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public float getXFraction() {
		Log.e("xFraction", "get");
		final int width = getWidth();
		return (width != 0) ? getX() / getWidth() : getX();
	}

	public void setXFraction(float xFraction) {
		Log.e("xFraction", "set" + String.valueOf(xFraction));
		final int width = getWidth();
		float newWidth = (width > 0) ? (xFraction * width) : -9999;
		setX(newWidth);
	}
}
