package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.utils.Helpers;

public class ListPreferenceEx extends ListPreference implements PreferenceState {
	private final int indentLevel;
	private final boolean dynamic;
	private boolean newmod = false;
    private boolean highlight = false;
	private boolean unsupported = false;
	private final boolean valueAsSummary;
	private String mListDefaultValue;

	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.ListPreferenceEx);
		indentLevel = xmlAttrs.getInt(R.styleable.ListPreferenceEx_indentLevel, 0);
		dynamic = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_dynamic, false);
		valueAsSummary = xmlAttrs.getBoolean(R.styleable.ListPreferenceEx_valueAsSummary, false);
		xmlAttrs.recycle();
		setIconSpaceReserved(false);
	}

	@Override
	protected void notifyChanged() {
		super.notifyChanged();
		boolean disableDependents = shouldDisableDependents();
		this.notifyDependencyChange(disableDependents);
	}

	@Override
	protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
		Object val = a.getString(index);
		mListDefaultValue = val == null ? null : (String) val;
		return val;
	}

	@Override
	public boolean shouldDisableDependents() {
		boolean shouldDisable = TextUtils.equals(mListDefaultValue, getValue());
		return shouldDisable || super.shouldDisableDependents();
	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
	}

	public void getView(View finalView) {
		TextView title = finalView.findViewById(android.R.id.title);
		TextView summary = finalView.findViewById(android.R.id.summary);
		TextView valSummary = finalView.findViewById(android.R.id.hint);
		Resources res = getContext().getResources();

		summary.setVisibility(valueAsSummary || getSummary() == null || getSummary().equals("") ? View.GONE : View.VISIBLE);
		valSummary.setVisibility(valueAsSummary ? View.VISIBLE : View.GONE);
		valSummary.setText(valueAsSummary ? getEntry() : "");
		if (valueAsSummary) {
			int disableColor = res.getColor(R.color.preference_primary_text_disable, getContext().getTheme());
			int secondary = res.getColor(R.color.preference_secondary_text, getContext().getTheme());
			valSummary.setTextColor(isEnabled() ? secondary : disableColor);
		}
		title.setText(getTitle() + (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		if (newmod) Helpers.applyNewMod(title);
		if (highlight) {
			Helpers.applySearchItemHighlight(finalView);
		}

		int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);
		int hrzPadding = (indentLevel + 1) * childpadding;
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
			valSummary.setPadding(summary.getPaddingLeft(), summary.getPaddingTop(), getContext().getResources().getDimensionPixelSize(R.dimen.preference_summary_padding_right), summary.getPaddingBottom());
			valSummary.setId(android.R.id.hint);
			((ViewGroup) view.itemView).addView(valSummary, 2);
		}

		getView(view.itemView);
	}

	@Override
	public void markAsNew() {
		newmod = true;
	}

	@Override
	public void applyHighlight() {
		highlight = true;
	}
}
