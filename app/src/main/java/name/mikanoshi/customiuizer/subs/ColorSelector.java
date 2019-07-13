package name.mikanoshi.customiuizer.subs;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.ColorCircle;

public class ColorSelector extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		String key = args.getString("key");

		if (getView() == null) return;
		View selColor = getView().findViewById(R.id.selected_color);
		selColor.setBackgroundResource(R.drawable.rounded_corners);
		ColorCircle colorCircle = getView().findViewById(R.id.color_circle);
		colorCircle.setTag(key);
		colorCircle.setListener(new ColorCircle.ColorListener() {
			@Override
			public void onColorSelected(int color) {
				((GradientDrawable)selColor.getBackground()).setColor(color);
			}
		});
		((GradientDrawable)selColor.getBackground()).setColor(colorCircle.getColor());
	}

}