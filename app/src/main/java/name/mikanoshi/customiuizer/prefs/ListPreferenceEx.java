package name.mikanoshi.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;

public class ListPreferenceEx extends ListPreference {

	private boolean valueAsSummary;
	private CharSequence sValue;

	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, new int[] { R.attr.valueAsSummary } );
		valueAsSummary = xmlAttrs.getBoolean(0, false);
		xmlAttrs.recycle();
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		sValue = getEntries()[findIndexOfValue(value)];
	}

	@Override
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView summary = finalView.findViewById(android.R.id.summary);
		TextView valSummary = finalView.findViewById(android.R.id.hint);
		summary.setVisibility(valueAsSummary ? View.GONE : View.VISIBLE);
		valSummary.setVisibility(valueAsSummary ? View.VISIBLE : View.GONE);
		valSummary.setText(valueAsSummary ? sValue : "");
		return finalView;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		ViewGroup view = (ViewGroup)super.onCreateView(parent);
		((TextView)view.findViewById(android.R.id.title)).setMaxLines(3);
		TextView summary = view.findViewById(android.R.id.summary);

		Resources res = getContext().getResources();
		summary.setTextColor(res.getColor(res.getIdentifier("preference_secondary_text_light", "color", "miui"), getContext().getTheme()));
		TextView valSummary = new TextView(getContext());
		valSummary.setTextSize(TypedValue.COMPLEX_UNIT_PX, summary.getTextSize());
		valSummary.setTextColor(summary.getCurrentTextColor());
		valSummary.setPadding(summary.getPaddingLeft(), summary.getPaddingTop(), res.getDimensionPixelSize(R.dimen.preference_summary_padding_right), summary.getPaddingBottom());
		valSummary.setId(android.R.id.hint);
		view.addView(valSummary, 2);
		return view;
	}
}
