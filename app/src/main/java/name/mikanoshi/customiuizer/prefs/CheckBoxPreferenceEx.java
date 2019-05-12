package name.mikanoshi.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CheckBoxPreferenceEx extends CheckBoxPreference {

	private Resources res = getContext().getResources();
	private int primary = res.getColor(res.getIdentifier("preference_primary_text_light", "color", "miui"), getContext().getTheme());
	private int secondary = res.getColor(res.getIdentifier("preference_secondary_text_light", "color", "miui"), getContext().getTheme());

	public CheckBoxPreferenceEx(Context context) {
		super(context);
	}

	public CheckBoxPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CheckBoxPreferenceEx(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		title.setTextColor(isEnabled() ? primary : secondary);
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
}
