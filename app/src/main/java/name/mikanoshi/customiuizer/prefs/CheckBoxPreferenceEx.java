package name.mikanoshi.customiuizer.prefs;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import miui.widget.SlidingButton;
import name.mikanoshi.customiuizer.R;

public class CheckBoxPreferenceEx extends CheckBoxPreference {

	public CheckBoxPreferenceEx(Context context) {
		super(context);
	}

	public CheckBoxPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckBoxPreferenceEx(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onBindView(View rootView) {
		super.onBindView(rootView);

		// Update arrow id and hide if needed
		View arrow = rootView.findViewById(R.id.arrow_right);
		if (arrow != null) {
			arrow.setId(rootView.getResources().getIdentifier("arrow_right", "id", "miui"));
			View widget = rootView.findViewById(android.R.id.widget_frame);
			if (widget != null && widget.getVisibility() == View.VISIBLE)
			arrow.setVisibility(View.GONE);
		}

		// Fix click on checkbox
		final CheckBoxPreferenceEx cb = this;
		SlidingButton sb = rootView.findViewById(android.R.id.checkbox);
		sb.setOnPerformCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				cb.setChecked(isChecked);
				cb.getOnPreferenceChangeListener().onPreferenceChange(cb, isChecked);
			}
		});
	}
}
