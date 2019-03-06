package name.mikanoshi.customiuizer.prefs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import miui.app.AlertDialog;
import name.mikanoshi.customiuizer.R;

public class SpinnerEx extends Spinner {

	private DialogPopup mPopup;
	private ArrayAdapterEx<CharSequence> mListAdapter;
	private CharSequence[] entries;
	private ArrayList<Integer> disabledItems = new ArrayList<Integer>();
	public int[] entryValues;

	public SpinnerEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPopup = new DialogPopup();

		final TypedArray xmlAttrs = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.entries, android.R.attr.prompt, R.attr.entryValues } );
		if (xmlAttrs.getResourceId(1, 0) != 0) mPopup.setPromptText(getResources().getString(xmlAttrs.getResourceId(1, 0)));
		entries = xmlAttrs.getTextArray(0);
		if (xmlAttrs.getResourceId(2, 0) != 0) entryValues = getResources().getIntArray(xmlAttrs.getResourceId(2, 0));
		xmlAttrs.recycle();
	}

	@Override
	public boolean performClick() {
		if (!mPopup.isShowing())
		mPopup.show(getTextDirection(), getTextAlignment());
		return true;
	}

	private int findIndex(int val, int[] vals) {
		for (int i = 0; i < vals.length; i++)
		if (vals[i] == val) return i;
		return -1;
	}

	public void init(int val, int itemId) {
		if (entries == null || entryValues == null) return;
		mListAdapter = new ArrayAdapterEx<CharSequence>(getContext(), itemId, entries);
		setSelection(findIndex(val, entryValues));
	}

	public void addDisabledItems(int item) {
		disabledItems.add(item);
	}

	public int getSelectedArrayValue() {
		return entryValues[getSelectedItemPosition()];
	}

	class ArrayAdapterEx<CharSequence> extends ArrayAdapter<CharSequence> {

		ArrayAdapterEx(Context context, int resource, CharSequence[] objects) {
			super(context, resource, objects);
		}

		@Override
		public boolean isEnabled(int position) {
			return !disabledItems.contains(position);
		}
	}

	private class DialogPopup implements DialogInterface.OnClickListener {
		private AlertDialog mPopup;
		private CharSequence mPrompt;

		public void dismiss() {
			if (mPopup != null) {
				mPopup.dismiss();
				mPopup = null;
			}
		}

		public boolean isShowing() {
			return mPopup != null && mPopup.isShowing();
		}

		public void setPromptText(CharSequence hintText) {
			mPrompt = hintText;
		}

		public CharSequence getHintText() {
			return mPrompt;
		}

		public void show(int textDirection, int textAlignment) {
			if (mListAdapter == null) return;
			AlertDialog.Builder builder = new AlertDialog.Builder(getPopupContext());
			if (mPrompt != null) builder.setTitle(mPrompt);
			mPopup = builder.setSingleChoiceItems(mListAdapter, getSelectedItemPosition(), this).create();
			final ListView listView = mPopup.getListView();
			listView.setTextDirection(textDirection);
			listView.setTextAlignment(textAlignment);
			mPopup.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					for (Integer disabledItem: disabledItems)
					if (disabledItem < listView.getChildCount())
					listView.getChildAt(disabledItem).setEnabled(false);
				}
			});
			mPopup.show();
		}

		public void onClick(DialogInterface dialog, int which) {
			setSelection(which);
			performItemClick(null, which, mListAdapter.getItemId(which));
			dismiss();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mPopup != null && mPopup.isShowing()) mPopup.dismiss();
	}

	@Override
	public void setPrompt(CharSequence prompt) {
		mPopup.setPromptText(prompt);
	}

	@Override
	public void setPromptId(int promptId) {
		setPrompt(getContext().getText(promptId));
	}

	@Override
	public CharSequence getPrompt() {
		return mPopup.getHintText();
	}
}
