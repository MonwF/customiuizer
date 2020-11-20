package name.mikanoshi.customiuizer.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;

public class PreferenceCategoryEx extends PreferenceCategory {

	private final boolean dynamic;
	private final boolean empty;
	private boolean hidden;

	public PreferenceCategoryEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.PreferenceCategoryEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.PreferenceCategoryEx_dynamic, false);
		empty = xmlAttrs.getBoolean(R.styleable.PreferenceCategoryEx_empty, false);
		hidden = xmlAttrs.getBoolean(R.styleable.PreferenceCategoryEx_hidden, false);
		xmlAttrs.recycle();
	}

	@Override
	public boolean onPrepareAddPreference(Preference preference) {
		preference.onParentChanged(this, shouldDisableDependents());
		return true;
	}

	@Override
	@SuppressLint("SetTextI18n")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		title.setText(getTitle() + (dynamic ? " ⟲" : ""));
		title.setVisibility(hidden || empty ? View.GONE : View.VISIBLE);
		if (hidden) {
			finalView.setBackground(null);
			finalView.setPadding(
				finalView.getPaddingLeft(),
				finalView.getPaddingTop() + Math.round(getContext().getResources().getDisplayMetrics().density * 10),
				finalView.getPaddingRight(),
				finalView.getPaddingBottom()
			);
		}
		return finalView;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void hide() {
		hidden = true;
		this.notifyChanged();
	}

	public void show() {
		hidden = false;
		this.notifyChanged();
	}

}
