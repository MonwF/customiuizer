package name.mikanoshi.customiuizer.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

public class ListPreferenceEx extends ListPreference implements PreferenceState {

	private CharSequence sValue;
	private final Resources res = getContext().getResources();
	private final int primary = res.getColor(R.color.preference_primary_text, getContext().getTheme());
	private final int secondary = res.getColor(R.color.preference_secondary_text, getContext().getTheme());
	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);
	private final int[] paddings = new int[] {0, 0, 0, 0};

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

	@Override
	@SuppressLint("SetTextI18n")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		TextView summary = finalView.findViewById(android.R.id.summary);
		TextView valSummary = finalView.findViewById(android.R.id.hint);

		summary.setVisibility(valueAsSummary || getSummary() == null || getSummary().equals("") ? View.GONE : View.VISIBLE);
		valSummary.setVisibility(valueAsSummary ? View.VISIBLE : View.GONE);
		valSummary.setText(valueAsSummary ? sValue : "");
		if (valueAsSummary && Helpers.is11()) valSummary.setTextColor(Helpers.isNightMode(getContext()) ? secondary : primary);
		title.setTextColor(isEnabled() ? primary : secondary);
		title.setText(getTitle() + (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		if (newmod) Helpers.applyNewMod(title);

		if (paddings[0] == 0) paddings[0] = finalView.getPaddingLeft();
		if (paddings[1] == 0) paddings[1] = finalView.getPaddingTop();
		if (paddings[2] == 0) paddings[2] = finalView.getPaddingRight();
		if (paddings[3] == 0) paddings[3] = finalView.getPaddingBottom();
		finalView.setPadding(paddings[0] + (child ? childpadding : 0), paddings[1], paddings[2], paddings[3]);

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

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		final Window window = getDialog().getWindow();
		if (window == null) return;

		final int listResID = res.getIdentifier("select_dialog_listview", "id", "miui");
		final ListView listView = window.findViewById(listResID);
		if (listView != null)
		listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
			@Override
			public void onChildViewAdded(View parent, View child) {
				if (child == null) return;
				if (child instanceof CheckedTextView) try {
					if (((CheckedTextView)child).isChecked())
						((CheckedTextView)child).setTextColor(res.getColor(res.getIdentifier("highlight_normal_light", "color", "miui"), getContext().getTheme()));
					else if (Helpers.isNightMode(getContext()))
						((CheckedTextView)child).setTextColor(res.getColor(res.getIdentifier("list_text_color_normal_dark", "color", "miui"), getContext().getTheme()));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}

			@Override
			public void onChildViewRemoved(View parent, View child) {}
		});
	}

	@Override
	public void markAsNew() {
		newmod = true;
	}

}
