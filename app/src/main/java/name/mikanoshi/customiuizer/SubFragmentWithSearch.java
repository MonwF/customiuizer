package name.mikanoshi.customiuizer;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import name.mikanoshi.customiuizer.utils.AppDataAdapter;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.LockedAppAdapter;
import name.mikanoshi.customiuizer.utils.PrivacyAppAdapter;
import name.mikanoshi.customiuizer.utils.ResolveInfoAdapter;

public class SubFragmentWithSearch extends SubFragment {

	public ListView listView = null;
	View searchView = null;
	LinearLayout search = null;
	boolean isSearchFocused = false;
	TextView textInput = null;

	public void setActionModeStyle(View searchView) {
		boolean isNight = Helpers.isNightMode(getValidContext());
		if (searchView != null) try {
			searchView.setSaveFromParentEnabled(false);
			Drawable drawable = getResources().getDrawable(getResources().getIdentifier(isNight ? "search_mode_bg_dark" : "search_mode_bg_light", "drawable", "miui"), getValidContext().getTheme());
			try {
				int colorResId = getResources().getIdentifier(isNight ? "primary_color_dark" : "primary_color_light", "color", "miui");
				if (colorResId != 0 && drawable instanceof LayerDrawable) {
					drawable = ((LayerDrawable)drawable).getDrawable(0);
					if (drawable instanceof GradientDrawable)
						((GradientDrawable)drawable).setColor(getResources().getColor(colorResId, getValidContext().getTheme()));
				}
			} catch (Throwable ignore) {}
			searchView.setBackground(drawable);
			LinearLayout inputArea = searchView.findViewById(android.R.id.inputArea);
			inputArea.setBackgroundResource(getResources().getIdentifier(isNight ? "search_mode_edit_text_bg_dark" : "search_mode_edit_text_bg_light", "drawable", "miui"));
			if (Helpers.is11()) {
				ViewGroup.LayoutParams lp1 = searchView.getLayoutParams();
				int resId = getResources().getIdentifier("action_bar_default_height", "dimen", "miui");
				lp1.height = getResources().getDimensionPixelSize(resId == 0 ? R.dimen.secondary_text_size : resId);
				searchView.setLayoutParams(lp1);
				FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams)inputArea.getLayoutParams();
				resId = getResources().getIdentifier("searchbar_bg_height", "dimen", "miui");
				lp2.height = getResources().getDimensionPixelSize(resId == 0 ? R.dimen.searchbar_bg_height : resId);
				inputArea.setLayoutParams(lp2);
			}
			ImageView inputIcon = searchView.findViewById(R.id.inputIcon);
			inputIcon.setImageResource(getResources().getIdentifier(isNight ? "edit_text_search_dark" : "edit_text_search", "drawable", "miui"));
			TextView input = searchView.findViewById(android.R.id.input);
			int fontSize = getResources().getIdentifier(Helpers.is11() ? "edit_text_font_size" : "secondary_text_size", "dimen", "miui");
			input.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(fontSize == 0 ? R.dimen.secondary_text_size : fontSize));
			input.setHintTextColor(getResources().getColor(getResources().getIdentifier(isNight ? "edit_text_search_hint_color_dark" : "edit_text_search_hint_color_light", "color", "miui"), getValidContext().getTheme()));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getView() == null) return;

		searchView = getView().findViewById(R.id.searchView);
		setActionModeStyle(searchView);
		search = searchView.findViewById(android.R.id.inputArea);
		textInput = searchView.findViewById(android.R.id.input);

		textInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				isSearchFocused = hasFocus;
			}
		});
		textInput.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isSearchFocused = v.hasFocus();
			}
		});
		textInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					Helpers.hideKeyboard((AppCompatActivity) getActivity(), v);
					listView.requestFocus();
					return true;
				}
				return false;
			}
		});
		textInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable s) {
				applyFilter(s.toString().trim());
			}
		});

		listView = getView().findViewById(android.R.id.list);
		listView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if (isSearchFocused) {
					isSearchFocused = false;
					Helpers.hideKeyboard((AppCompatActivity) getActivity(), v);
				}
				return false;
			}
		});
	}

	void applyFilter(String filter) {
		if (listView == null) return;
		ListAdapter adapter = listView.getAdapter();
		if (adapter == null) return;
		if (adapter instanceof AppDataAdapter)
			((AppDataAdapter)listView.getAdapter()).getFilter().filter(filter);
		else if (adapter instanceof PrivacyAppAdapter)
			((PrivacyAppAdapter)listView.getAdapter()).getFilter().filter(filter);
		else if (adapter instanceof LockedAppAdapter)
			((LockedAppAdapter)listView.getAdapter()).getFilter().filter(filter);
		else if (adapter instanceof ResolveInfoAdapter)
			((ResolveInfoAdapter)listView.getAdapter()).getFilter().filter(filter);
	}
}