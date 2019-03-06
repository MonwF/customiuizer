package name.mikanoshi.customiuizer;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import miui.view.SearchActionMode;
import name.mikanoshi.customiuizer.utils.AppDataAdapter;

public class SubFragmentWithSearch extends SubFragment {

	public ListView listView = null;
	ActionMode actionMode = null;
	boolean isSearchFocused = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

		TextView search = getView().findViewById(android.R.id.input);
		search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actionMode = startActionMode(new SearchActionMode.Callback() {
					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {

						mode.getMenuInflater().inflate(R.menu.menu_mods, menu);

						SearchActionMode samode = (SearchActionMode)mode;
						samode.setAnchorView(getView().findViewById(R.id.am_search_view));
						samode.setAnimateView(getView().findViewById(android.R.id.list));
						samode.getSearchInput().setOnFocusChangeListener(new View.OnFocusChangeListener() {
							@Override
							public void onFocusChange(View v, boolean hasFocus) {
								isSearchFocused = hasFocus;
							}
						});
						samode.getSearchInput().setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								isSearchFocused = v.hasFocus();
							}
						});
						samode.getSearchInput().setOnEditorActionListener(new TextView.OnEditorActionListener() {
							@Override
							public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
								if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
									hideKeyboard();
									listView.requestFocus();
									return true;
								}
								return false;
							}
						});
						samode.getSearchInput().addTextChangedListener(new TextWatcher() {
							@Override
							public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

							@Override
							public void onTextChanged(CharSequence s, int start, int before, int count) {}

							@Override
							public void afterTextChanged(Editable s) {
								String filter = s.toString().trim();
								((AppDataAdapter)listView.getAdapter()).getFilter().filter(filter);
							}
						});
						return true;
					}

					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						return true;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
						return true;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						listView.clearTextFilter();
						getActionBar().show();
						actionMode = null;
					}
				});
			}
		});

		listView = getView().findViewById(android.R.id.list);
		listView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (actionMode != null && isSearchFocused) {
					isSearchFocused = false;
					hideKeyboard();
				}
				return false;
			}
		});
	}
}