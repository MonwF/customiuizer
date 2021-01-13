package name.mikanoshi.customiuizer.prefs;

import android.annotation.SuppressLint;
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

public class PreferenceEx extends Preference implements PreferenceState {

	private final Resources res = getContext().getResources();
	private final int primary = res.getColor(R.color.preference_primary_text, getContext().getTheme());
	private final int secondary = res.getColor(R.color.preference_secondary_text, getContext().getTheme());
	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);
	private final int[] paddings = new int[] {0, 0, 0, 0};

	private final boolean child;
	private final boolean dynamic;
	private final boolean warning;
	private final boolean countAsSummary;
	private String customSummary = null;
	private boolean newmod = false;
	private boolean unsupported = false;

	public PreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.PreferenceEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.PreferenceEx_dynamic, false);
		child = xmlAttrs.getBoolean(R.styleable.PreferenceEx_child, false);
		warning = xmlAttrs.getBoolean(R.styleable.PreferenceEx_warning, false);
		countAsSummary = xmlAttrs.getBoolean(R.styleable.PreferenceEx_countAsSummary, false);
		xmlAttrs.recycle();
	}

	@Override
	@SuppressLint("SetTextI18n")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		TextView summary = finalView.findViewById(android.R.id.summary);
		TextView valSummary = finalView.findViewById(android.R.id.hint);

		summary.setVisibility(customSummary != null || countAsSummary || getSummary() == null || getSummary().equals("") ? View.GONE : View.VISIBLE);
		valSummary.setVisibility(customSummary != null || countAsSummary ? View.VISIBLE : View.GONE);
		if (customSummary != null)
			valSummary.setText(customSummary);
		else if (countAsSummary) {
			int count = Helpers.prefs.getStringSet(getKey(), new LinkedHashSet<String>()).size() + Helpers.prefs.getStringSet(getKey() + "_black", new LinkedHashSet<String>()).size();
			valSummary.setText(String.valueOf(count));
		} else
			valSummary.setText(null);
		if (warning)
			title.setTextColor(Helpers.markColor);
		else
			title.setTextColor(isEnabled() ? primary : secondary);
		title.setText(getTitle() +  (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		if (newmod) Helpers.applyNewMod(title);

		if (paddings[0] == 0) paddings[0] = finalView.getPaddingLeft();
		if (paddings[1] == 0) paddings[1] = finalView.getPaddingTop();
		if (paddings[2] == 0) paddings[2] = finalView.getPaddingRight();
		if (paddings[3] == 0) paddings[3] = finalView.getPaddingBottom();
		finalView.setPadding(paddings[0] + (child ? childpadding : 0), paddings[1], paddings[2], paddings[3]);

		return finalView;
	}

	public void setCustomSummary(String text) {
		customSummary = text;
		notifyChanged();
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

	@Override
	public void markAsNew() {
		newmod = true;
	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
	}

}
