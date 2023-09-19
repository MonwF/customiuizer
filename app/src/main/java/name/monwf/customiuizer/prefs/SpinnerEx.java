package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.appcompat.widget.AppCompatSpinner;

import java.lang.reflect.Field;
import java.util.ArrayList;

import name.monwf.customiuizer.R;

public class SpinnerEx extends AppCompatSpinner {

	public CharSequence[] entries;
	public int[] entryValues;
	private final ArrayList<Integer> disabledItems = new ArrayList<Integer>();
	private final Resources res = getContext().getResources();
	private final int childpadding = res.getDimensionPixelSize(R.dimen.preference_item_child_padding);

	public SpinnerEx(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.entries, R.attr.entryValues } );
		entries = xmlAttrs.getTextArray(0);
		if (xmlAttrs.getResourceId(1, 0) != 0) entryValues = getResources().getIntArray(xmlAttrs.getResourceId(1, 0));
		xmlAttrs.recycle();
		this.setPadding(childpadding, 0, childpadding, 0);
		try {
			Field mPopup = AppCompatSpinner.class.getDeclaredField("mPopup");
			mPopup.setAccessible(true);
			final float scale = getResources().getDisplayMetrics().density;
			androidx.appcompat.widget.ListPopupWindow popupWindow = (androidx.appcompat.widget.ListPopupWindow) mPopup.get(this);
			popupWindow.setHeight((int) (40 * 10 * scale));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private int findIndex(int val, int[] vals) {
		for (int i = 0; i < vals.length; i++)
		if (vals[i] == val) return i;
		return -1;
	}

	public void init(int val) {
		if (entries == null || entryValues == null) return;
		ArrayAdapterEx newAdapter = new ArrayAdapterEx(getContext(), android.R.layout.simple_spinner_item, entries);
		setAdapter(newAdapter);
		setSelection(findIndex(val, entryValues));
	}

	public int getSelectedArrayValue() {
		return entryValues[getSelectedItemPosition()];
	}

	class ArrayAdapterEx extends ArrayAdapter<CharSequence> {

		ArrayAdapterEx(Context context, int resource, CharSequence[] objects) {
			super(context, resource, objects);
		}

		@Override
		public boolean isEnabled(int position) {
			return !disabledItems.contains(position);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			View view = super.getDropDownView(position, convertView, parent);
			view.setEnabled(isEnabled(position));
			return view;
		}
	}
}
