package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import name.monwf.customiuizer.R;

public class PreferenceCategoryEx extends PreferenceCategory {
	private final boolean dynamic;
	private int state; // 0-正常 1-纯区块 2-顶层隐藏
	private boolean unsupported = false;
	private final Resources res = getContext().getResources();
	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);

	public PreferenceCategoryEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, R.styleable.PreferenceCategoryEx);
		dynamic = xmlAttrs.getBoolean(R.styleable.PreferenceCategoryEx_dynamic, false);
		state = xmlAttrs.getInt(R.styleable.PreferenceCategoryEx_state, 0);
		xmlAttrs.recycle();
		setLayoutResource(R.layout.preference_category);
	}

	@Override
	public boolean onPrepareAddPreference(Preference preference) {
		preference.onParentChanged(this, shouldDisableDependents());
		return true;
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder view) {
		super.onBindViewHolder(view);
		TextView title = (TextView) view.findViewById(android.R.id.title);
		title.setText(getTitle() + (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		title.setVisibility((state == 2 || state == 1) ? View.GONE : View.VISIBLE);
		View finalView = view.itemView;
		if (state == 2) {
			finalView.setPadding(childpadding, 0, childpadding, 0);
		}
		else {
			int vertialPadding = getContext().getResources().getDimensionPixelSize(R.dimen.preference_item_padding_top);
			finalView.setPadding(childpadding, vertialPadding, childpadding, vertialPadding);
		}
	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void hide() {
		state = 2;
		this.notifyChanged();
	}
}
