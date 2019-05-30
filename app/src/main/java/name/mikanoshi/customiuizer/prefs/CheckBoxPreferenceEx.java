package name.mikanoshi.customiuizer.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;

public class CheckBoxPreferenceEx extends CheckBoxPreference {

	private boolean unsupported = false;
	private Resources res = getContext().getResources();
	private int primary = res.getColor(res.getIdentifier("preference_primary_text_light", "color", "miui"), getContext().getTheme());
	private int secondary = res.getColor(res.getIdentifier("preference_secondary_text_light", "color", "miui"), getContext().getTheme());

	private boolean dynamic;

	public CheckBoxPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxPreferenceEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.CheckBoxPreferenceEx_dynamic, false);
		xmlAttrs.recycle();
	}

	@Override
	@SuppressLint("SetTextI18n")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		title.setTextColor(isEnabled() ? primary : secondary);
		title.setText(getTitle() + (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
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
}
