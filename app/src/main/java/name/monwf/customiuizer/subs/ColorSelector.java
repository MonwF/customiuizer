package name.monwf.customiuizer.subs;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import android.widget.SeekBar;
import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.ColorCircle;
import name.monwf.customiuizer.utils.Helpers;

public class ColorSelector extends SubFragment {

	String key;
	ColorCircle colorCircle;
	TextView white;
	TextView black;
	TextView auto;
	TextView selectedColorHint;
	View selectedColorView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}

	void updateSelColor(int color) {
		((GradientDrawable) selectedColorView.getBackground()).setColors(color == Color.TRANSPARENT ? new int[]{ Color.WHITE, Color.BLACK } : new int[]{ color, color });
		selectedColorHint.setText(String.format("#%08X", color));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		key = args.getString("key");

		if (getView() == null) return;
		selectedColorView = getView().findViewById(R.id.selected_color);
		selectedColorView.setBackgroundResource(R.drawable.rounded_corners);
		selectedColorHint = getView().findViewById(R.id.selected_color_hint);
		colorCircle = getView().findViewById(R.id.color_circle);
		colorCircle.setTag(key);
		int prefColor = AppHelper.getIntOfAppPrefs(key, Color.WHITE);
		colorCircle.init(prefColor);
		colorCircle.setListener(this::updateSelColor);
		if (savedInstanceState != null) {
			int savedColor = savedInstanceState.getInt("colorCircleColor");
			colorCircle.setColor(savedColor, true);
		}
		int currentColor = colorCircle.getColor();
		updateSelColor(currentColor);

		SeekBar hsvBar = getView().findViewById(R.id.hsv_value);
		hsvBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
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
		hsvBar.setProgress((int)(hsv[2] * 100), false);

		SeekBar alphaBar = getView().findViewById(R.id.alpha_value);
		alphaBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
				if (!fromUser) return;
				colorCircle.setAlphaVal(progress);
			}

			@Override
			public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
		});
		alphaBar.setProgress(Color.alpha(currentColor), false);

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
				hsvBar.setProgress(100, false);
			}
		});

		black = getView().findViewById(R.id.black_color);
		black.setSelected(currentColor == Color.BLACK);
		black.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSelected(2);
				colorCircle.setColor(Color.BLACK);
				hsvBar.setProgress(0, false);
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
				hsvBar.setProgress(0, false);
			}
		});

		selectedColorHint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppHelper.showInputDialog(getActivity(), selectedColorHint.getText().toString(), R.string.array_static, 0, 1, new Helpers.InputCallback() {
					@Override
					public void onInputFinished(String key, String text){
						if (key != null && !TextUtils.isEmpty(text.trim())) {
							try {
								colorCircle.setColor(Color.parseColor(text), true);
							}
							catch (IllegalArgumentException e) {
							}
						}
					}
				}, false);
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