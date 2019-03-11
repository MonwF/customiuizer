package name.mikanoshi.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;

public class ListPreferenceEx extends ListPreference {

	private boolean valueAsSummary = false;

	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, new int[] { R.attr.valueAsSummary } );
		valueAsSummary = xmlAttrs.getBoolean(0, false);
		xmlAttrs.recycle();
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		if (valueAsSummary) setSummary(getEntries()[findIndexOfValue(value)]);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		ViewGroup view = (ViewGroup)super.onCreateView(parent);
		TextView summary = view.findViewById(android.R.id.summary);

		Resources res = getContext().getResources();
		summary.setTextColor(res.getColor(res.getIdentifier("preference_secondary_text_light", "color", "miui"), getContext().getTheme()));
		if (valueAsSummary) {
			summary.setPadding(summary.getPaddingLeft(), summary.getPaddingTop(), res.getDimensionPixelSize(R.dimen.preference_summary_padding_right), summary.getPaddingBottom());
			((ViewGroup)summary.getParent()).removeView(summary);
			view.addView(summary, 2);
		}
		return view;
	}
}
