package name.mikanoshi.customiuizer.subs;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import miui.widget.SeekBar;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.ColorCircle;

public class ColorSelector extends SubFragment {

	String key;
	ColorCircle colorCircle;
	TextView white;
	TextView black;
	TextView auto;
	View selColor;

	void updateSelColor(int color) {
		((GradientDrawable)selColor.getBackground()).setColors(color == Color.TRANSPARENT ? new int[]{ Color.WHITE, Color.BLACK } : new int[]{ color, color });
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		key = args.getString("key");

		if (getView() == null) return;
		selColor = getView().findViewById(R.id.selected_color);
		selColor.setBackgroundResource(R.drawable.rounded_corners);
		colorCircle = getView().findViewById(R.id.color_circle);
		colorCircle.setTag(key);
		colorCircle.init();
		colorCircle.setListener(this::updateSelColor);
		if (savedInstanceState != null) colorCircle.setColor(savedInstanceState.getInt("colorCircleColor"));
		int currentColor = colorCircle.getColor();
		updateSelColor(currentColor);

		SeekBar value = getView().findViewById(R.id.hsv_value);
		value.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
				if (!fromUser) return;
				colorCircle.setValue(progress / 100f);
			}

			@Override
			public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
		});
		float[] hsv = new float[3];
		Color.RGBToHSV(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), hsv);
		value.setProgress((int)(hsv[2] * 100), false);

		white = getView().findViewById(R.id.white_color);
		black = getView().findViewById(R.id.black_color);
		auto = getView().findViewById(R.id.auto_color);

		white = getView().findViewById(R.id.white_color);
		white.setSelected(currentColor == Color.WHITE);
		white.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSelected(1);
				colorCircle.setColor(Color.WHITE);
				value.setProgress(100, false);
			}
		});

		black = getView().findViewById(R.id.black_color);
		black.setSelected(currentColor == Color.BLACK);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSelected(2);
				colorCircle.setColor(Color.BLACK);
				value.setProgress(0, false);
			}
		});

		auto = getView().findViewById(R.id.auto_color);
		if (key.contains("pref_key_system_batteryindicator")) auto.setVisibility(View.VISIBLE);
		auto.setSelected(currentColor == Color.TRANSPARENT);
		auto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSelected(3);
				colorCircle.setColor(Color.TRANSPARENT);
				value.setProgress(0, false);
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt("colorCircleColor", colorCircle.getColor());
		super.onSaveInstanceState(savedInstanceState);
	}

	void setSelected(int btn) {
		white.setSelected(false);
		black.setSelected(false);
		auto.setSelected(false);
		switch (btn) {
			case 1: white.setSelected(true); break;
			case 2: black.setSelected(true); break;
			case 3: auto.setSelected(true); break;
		}
	}

}