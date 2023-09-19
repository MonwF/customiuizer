package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import java.util.LinkedHashSet;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class PreferenceEx extends Preference implements PreferenceState {
	private final Resources res = getContext().getResources();
	private final int secondary = res.getColor(R.color.preference_secondary_text, getContext().getTheme());
	private final int disableColor = res.getColor(R.color.preference_primary_text_disable, getContext().getTheme());
	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);

	private final int indentLevel;
	private final boolean dynamic;
	private final boolean warning;
	private final boolean countAsSummary;
	private final boolean longClickable;
	private String customSummary = null;
	private boolean newmod = false;
    private boolean highlight = false;
	private boolean unsupported = false;
	View.OnLongClickListener longPressListener;

	public PreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.PreferenceEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.PreferenceEx_dynamic, false);
		indentLevel = xmlAttrs.getInt(R.styleable.PreferenceEx_indentLevel, 0);
		warning = xmlAttrs.getBoolean(R.styleable.PreferenceEx_warning, false);
		countAsSummary = xmlAttrs.getBoolean(R.styleable.PreferenceEx_countAsSummary, false);
		longClickable = xmlAttrs.getBoolean(R.styleable.PreferenceEx_longClickable, false);
		xmlAttrs.recycle();
		setIconSpaceReserved(false);
	}

	public void getView(View finalView) {
		TextView title = finalView.findViewById(android.R.id.title);
		TextView summary = finalView.findViewById(android.R.id.summary);
		TextView valSummary = finalView.findViewById(android.R.id.hint);

		summary.setVisibility(customSummary != null || countAsSummary || getSummary() == null || getSummary().equals("") ? View.GONE : View.VISIBLE);
		valSummary.setVisibility(customSummary != null || countAsSummary ? View.VISIBLE : View.GONE);
		if (customSummary != null || countAsSummary) {
			valSummary.setTextColor(isEnabled() ? secondary : disableColor);
		}
		if (customSummary != null)
			valSummary.setText(customSummary);
		else if (countAsSummary) {
			int count = AppHelper.getStringSetOfAppPrefs(getKey(), new LinkedHashSet<String>()).size() + AppHelper.getStringSetOfAppPrefs(getKey() + "_black", new LinkedHashSet<String>()).size();
			valSummary.setText(String.valueOf(count));
		} else
			valSummary.setText(null);
		if (warning) {
			title.setTextColor(Helpers.markColor);
		}
		title.setText(getTitle() +  (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		if (newmod) Helpers.applyNewMod(title);
		if (highlight) {
			Helpers.applySearchItemHighlight(finalView);
		}

		int hrzPadding = (indentLevel + 1) * childpadding;
		finalView.setPadding(hrzPadding, 0, childpadding, 0);
		if (longClickable) {
			finalView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (longPressListener != null) {
						return longPressListener.onLongClick(finalView);
					}
					return false;
				}
			});
		}
	}

	public void setLongPressListener(View.OnLongClickListener ll) {
		longPressListener = ll;
	}

	public void setCustomSummary(String text) {
		customSummary = text;
		notifyChanged();
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

	@Override
	public void applyHighlight() {
		highlight = true;
	}

//	public void setNotice(boolean value) {
//		notice = value;
//		setEnabled(!value);
//	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
	}

}
