package name.mikanoshi.customiuizer.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

public class CheckBoxPreferenceEx extends SwitchPreference implements PreferenceState {

	private final Resources res = getContext().getResources();
	private final int primary = res.getColor(R.color.preference_primary_text, getContext().getTheme());
	private final int secondary = res.getColor(R.color.preference_secondary_text, getContext().getTheme());
	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);
	private final int[] paddings = new int[] {0, 0, 0, 0};

	private final boolean child;
	private final boolean dynamic;
	private boolean newmod = false;
	private boolean unsupported = false;

	public CheckBoxPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreferenceEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.CheckBoxPreferenceEx_dynamic, false);
		child = xmlAttrs.getBoolean(R.styleable.CheckBoxPreferenceEx_child, false);
		xmlAttrs.recycle();
	}

	@Override
	@SuppressLint("SetTextI18n")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
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
		View view = super.onCreateView(parent);

		TextView title = view.findViewById(android.R.id.title);
		title.setMaxLines(3);
		title.setTextColor(primary);

		TextView summary = view.findViewById(android.R.id.summary);
		summary.setTextColor(secondary);

		return view;
	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
	}

	@Override
	public void markAsNew() {
		newmod = true;
	}

}
