package name.mikanoshi.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedHashSet;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

public class PreferenceEx extends Preference {

	private Resources res = getContext().getResources();
	private int primary = res.getColor(res.getIdentifier("preference_primary_text_light", "color", "miui"), getContext().getTheme());
	private int secondary = res.getColor(res.getIdentifier("preference_secondary_text_light", "color", "miui"), getContext().getTheme());

	private boolean countAsSummary;

	public PreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, new int[] { R.attr.countAsSummary } );
		countAsSummary = xmlAttrs.getBoolean(0, false);
		xmlAttrs.recycle();
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		TextView summary = finalView.findViewById(android.R.id.summary);
		TextView valSummary = finalView.findViewById(android.R.id.hint);

		summary.setVisibility(countAsSummary || summary.getText() == null || summary.getText().equals("") ? View.GONE : View.VISIBLE);
		valSummary.setVisibility(countAsSummary ? View.VISIBLE : View.GONE);
		valSummary.setText(countAsSummary ? String.valueOf(Helpers.prefs.getStringSet(getKey(), new LinkedHashSet<String>()).size()) : null);
		title.setTextColor(isEnabled() ? primary : secondary);

		return finalView;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		ViewGroup view = (ViewGroup)super.onCreateView(parent);

		TextView title = view.findViewById(android.R.id.title);
		title.setMaxLines(3);
		title.setTextColor(primary);

		TextView summary = view.findViewById(android.R.id.summary);
		summary.setTextColor(secondary);

		TextView valSummary = new TextView(getContext());
		valSummary.setTextSize(TypedValue.COMPLEX_UNIT_PX, summary.getTextSize());
		valSummary.setTextColor(summary.getCurrentTextColor());
		valSummary.setPadding(summary.getPaddingLeft(), summary.getPaddingTop(), res.getDimensionPixelSize(R.dimen.preference_summary_padding_right), summary.getPaddingBottom());
		valSummary.setId(android.R.id.hint);
		view.addView(valSummary, 2);

		return view;
	}
}
