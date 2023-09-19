package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.utils.Helpers;

public class CheckBoxPreferenceEx extends SwitchPreference implements PreferenceState {
	private final Resources res = getContext().getResources();

	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);

	private final int indentLevel;
	private final boolean dynamic;
	private boolean newmod = false;
    private boolean highlight = false;
	private boolean unsupported = false;

	public CheckBoxPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreferenceEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.CheckBoxPreferenceEx_dynamic, false);
		indentLevel = xmlAttrs.getInt(R.styleable.CheckBoxPreferenceEx_indentLevel, 0);
		xmlAttrs.recycle();
		setIconSpaceReserved(false);
	}

	public void getView(View finalView) {
		TextView title = finalView.findViewById(android.R.id.title);
		title.setText(getTitle() + (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		if (newmod) Helpers.applyNewMod(title);
		if (highlight) {
			Helpers.applySearchItemHighlight(finalView);
		}
		int hrzPadding = (indentLevel + 1) * childpadding;
		finalView.setPadding(hrzPadding, 0, childpadding, 0);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder view) {
		super.onBindViewHolder(view);
		TextView title = (TextView) view.findViewById(android.R.id.title);
		title.setMaxLines(3);

		getView(view.itemView);
	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
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
