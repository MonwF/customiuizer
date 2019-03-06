package name.mikanoshi.customiuizer.prefs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import miui.app.AlertDialog;
import name.mikanoshi.customiuizer.R;

public class ListPreferenceEx extends ListPreference {

	AlertDialog mDialog;
	AlertDialog.Builder mBuilder;
	public int entriedRes;

	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		saveEntriesId(attrs);
	}

	public ListPreferenceEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		saveEntriesId(attrs);
	}

	private void saveEntriesId(AttributeSet attrs) {
		TypedArray ar = getContext().getResources().obtainAttributes(attrs, new int[] { android.R.attr.entries });
		entriedRes = ar.getResourceId(0, 0);
		ar.recycle();
	}

	@Override
	protected void showDialog(Bundle state) {
		mBuilder = new AlertDialog.Builder(getContext())
		.setTitle(getTitle())
		.setIcon(getDialogIcon())
		.setNegativeButton(android.R.string.cancel, null);

		View contentView = onCreateDialogView();
		if (contentView != null) {
			onBindDialogView(contentView);
			mBuilder.setView(contentView);
		} else {
			mBuilder.setMessage(getDialogMessage());
		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.select_dialog_singlechoice, getEntries());

		mBuilder.setSingleChoiceItems(adapter, findIndexOfValue(getValue()),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which >= 0 && getEntryValues() != null) {
						String value = getEntryValues()[which].toString();
						if (callChangeListener(value)) setValue(value);
					}
					dialog.dismiss();
				}
			}
		);

		mDialog = mBuilder.create();
        if (state != null) mDialog.onRestoreInstanceState(state);
        //mDialog.setOnDismissListener(this);
        mDialog.show();
	}
}
