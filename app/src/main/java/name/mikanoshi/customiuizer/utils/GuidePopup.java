package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import miui.widget.ArrowPopupWindow;
import name.mikanoshi.customiuizer.R;

public class GuidePopup extends ArrowPopupWindow {
	private LinearLayout layout;

	public GuidePopup(Context context) {
		super(context);
	}

	private void setText(String text) {
		if (TextUtils.isEmpty(text)) return;

		String[] txt = text.split("\n");
		if (txt.length == 0) return;

		LayoutInflater infalter = this.getLayoutInflater();
		for (String str: txt) {
			TextView textView = (TextView)infalter.inflate(getContext().getResources().getIdentifier("guide_popup_text_view", "layout", "miui"), null);
			textView.setText(str);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
			if (Helpers.isNightMode(getContext()))
			textView.setTextColor(getContext().getResources().getColor(R.color.guide_popup_text, getContext().getTheme()));
			textView.setSingleLine(true);
			textView.setMaxHeight(Integer.MAX_VALUE);
			textView.setMaxWidth(Integer.MAX_VALUE);
			this.layout.addView(textView);
		}
	}

	protected void onPrepareWindow() {
		super.onPrepareWindow();
		this.setFocusable(true);
		this.layout = (LinearLayout)this.getLayoutInflater().inflate(getContext().getResources().getIdentifier("guide_popup_content_view", "layout", "miui"), null, false);
		this.setContentView(this.layout);
		this.mArrowPopupView.enableShowingAnimation(false);
	}

	public void setGuideText(int resId) {
		this.setGuideText(this.getContext().getString(resId));
	}

	private void setGuideText(String text) {
		this.setText(text);
	}

	public void show(View anchor) {
		this.show(anchor, 0, 0);
	}

	public void show(View anchor, int x, int y) {
		super.show(anchor, x, y);
	}
}

