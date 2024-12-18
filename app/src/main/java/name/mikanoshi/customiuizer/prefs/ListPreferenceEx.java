package name.mikanoshi.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

public class ListPreferenceEx extends ListPreference implements PreferenceState {

	private CharSequence sValue;
	private final Resources res = getContext().getResources();
	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);
	private final int secondary = res.getColor(R.color.preference_secondary_text, getContext().getTheme());
	private final int disableColor = res.getColor(R.color.preference_primary_text_disable, getContext().getTheme());
	private final boolean child;
	private final boolean dynamic;
	private boolean newmod = false;
	private boolean unsupported = false;
	private final boolean valueAsSummary;

	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceEx);
		child = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_child, false);
		dynamic = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_dynamic, false);
		valueAsSummary = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_valueAsSummary, false);
		xmlAttrs.recycle();
		setIconSpaceReserved(false);
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		int index = findIndexOfValue(value);
		if (index < 0 || index > getEntries().length - 1) return;
		sValue = getEntries()[index];
	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
	}

	public void getView(View finalView) {
		TextView title = finalView.findViewById(android.R.id.title);
		TextView summary = finalView.findViewById(android.R.id.summary);
		TextView valSummary = finalView.findViewById(android.R.id.hint);

		summary.setVisibility(valueAsSummary || getSummary() == null || getSummary().equals("") ? View.GONE : View.VISIBLE);
		valSummary.setVisibility(valueAsSummary ? View.VISIBLE : View.GONE);
		valSummary.setText(valueAsSummary ? sValue : "");
		if (valueAsSummary) {
			valSummary.setTextColor(isEnabled() ? secondary : disableColor);
		}
		title.setText(getTitle() + (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		if (newmod) Helpers.applyNewMod(title);

		int hrzPadding = childpadding + (child ? childpadding : 0);
		finalView.setPadding(hrzPadding, 0, childpadding, 0);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder view) {
		super.onBindViewHolder(view);
		TextView title = (TextView) view.findViewById(android.R.id.title);
		title.setMaxLines(3);

		TextView summary = (TextView) view.findViewById(android.R.id.summary);

		TextView valSummary = view.itemView.findViewById(android.R.id.hint);
		if (valSummary == null) {
			valSummary = new TextView(getContext());
			valSummary.setTextSize(TypedValue.COMPLEX_UNIT_PX, summary.getTextSize());
			valSummary.setPadding(summary.getPaddingLeft(), summary.getPaddingTop(), res.getDimensionPixelSize(R.dimen.preference_summary_padding_right), summary.getPaddingBottom());
			valSummary.setId(android.R.id.hint);
			((ViewGroup) view.itemView).addView(valSummary, 2);
		}

		getView(view.itemView);
	}

	@Override
	public void markAsNew() {
		newmod = true;
	}

}
