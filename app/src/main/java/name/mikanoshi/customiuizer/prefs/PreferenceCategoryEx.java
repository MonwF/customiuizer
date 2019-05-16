package name.mikanoshi.customiuizer.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;

public class PreferenceCategoryEx extends PreferenceCategory {

	private boolean dynamic;

	public PreferenceCategoryEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.PreferenceCategoryEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.PreferenceCategoryEx_dynamic, false);
		xmlAttrs.recycle();
	}

	@Override
	@SuppressLint("SetTextI18n")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		title.setText(getTitle() + (dynamic ? " ⟲" : ""));
		return finalView;
	}

}
