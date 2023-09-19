package name.monwf.customiuizer.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.IllegalFormatException;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class SeekBarPreference extends Preference implements PreferenceState {

	private boolean dynamic;
	private boolean newmod = false;
    private boolean highlight = false;
	private boolean unsupported = false;
	private final int childpadding = getContext().getResources().getDimensionPixelSize(R.dimen.preference_item_child_padding);

	private int mDefaultValue;
	private int mMinValue;
	private int mMaxValue;
	private int mStepValue;
	private int mNegativeShift;
	private final int indentLevel;

	private int mDisplayDividerValue;
	private boolean mUseDisplayDividerValue;
	private boolean mShowPlus;

	private String mFormat;
	private String mNote;
	private String mOffText;

	private int mSteppedMinValue;
	private int mSteppedMaxValue;

	private TextView mValue;
	private SeekBar mSeekBar;

	private SeekBar.OnSeekBarChangeListener mListener;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mListener = null;

		if (attrs != null) {
			TypedArray xmlAttrs = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);

			indentLevel = xmlAttrs.getInt(R.styleable.SeekBarPreference_indentLevel, 0);
			dynamic = xmlAttrs.getBoolean(R.styleable.SeekBarPreference_dynamic, false);
			mMinValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_minValue, 0);
			mMaxValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_maxValue, 10);
			mStepValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_stepValue, 1);
			mDefaultValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_android_defaultValue, 0);
			mNegativeShift = xmlAttrs.getInt(R.styleable.SeekBarPreference_negativeShift, 0);
			mShowPlus = xmlAttrs.getBoolean(R.styleable.SeekBarPreference_showplus, false);

			if (xmlAttrs.hasValue(R.styleable.SeekBarPreference_displayDividerValue)) {
				mUseDisplayDividerValue = true;
				mDisplayDividerValue = xmlAttrs.getInt(R.styleable.SeekBarPreference_displayDividerValue, 1);
			} else {
				mUseDisplayDividerValue = false;
				mDisplayDividerValue = 1;
			}

			if (mMinValue < 0) mMinValue = 0;
			if (mMaxValue <= mMinValue) mMaxValue = mMinValue + 1;

			if (mDefaultValue < mMinValue)
				mDefaultValue = mMinValue;
			else if (mDefaultValue > mMaxValue)
				mDefaultValue = mMaxValue;

			if (mStepValue <= 0) mStepValue = 1;

			mFormat = xmlAttrs.getString(R.styleable.SeekBarPreference_format);
			mNote = xmlAttrs.getString(R.styleable.SeekBarPreference_note);
			mOffText = xmlAttrs.getString(R.styleable.SeekBarPreference_offtext);

			xmlAttrs.recycle();
		} else {
			indentLevel = 0;
			mMinValue = 0;
			mMaxValue = 10;
			mStepValue = 1;
			mDefaultValue = 0;
		}

		mSteppedMinValue = Math.round((float)mMinValue / mStepValue);
		mSteppedMaxValue = Math.round((float)mMaxValue / mStepValue);
		setLayoutResource(R.layout.preference_seekbar12);
	}

	public void getView(View finalView) {
		TextView mTitle = finalView.findViewById(android.R.id.title);
		mTitle.setText(getTitle() + (unsupported ? " ⨯" : (dynamic ? " ⟲" : "")));
		mSeekBar.setAlpha(isEnabled() ? 1.0f : 0.75f);
		if (newmod) Helpers.applyNewMod(mTitle);
		if (highlight) {
			Helpers.applySearchItemHighlight(finalView);
		}

		int hrzPadding = (indentLevel + 1) * childpadding;
		finalView.setPadding(hrzPadding, 0, childpadding, 0);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder view) {
		super.onBindViewHolder(view);

		TextView mSummaryView = (TextView) view.findViewById(android.R.id.summary);
		if (!TextUtils.isEmpty(getSummary()))
			mSummaryView.setText(getSummary());
		else
			mSummaryView.setVisibility(View.GONE);

		TextView mNoteView = (TextView) view.findViewById(android.R.id.text1);
		if (mNote == null || mNote.equals(""))
			mNoteView.setVisibility(View.GONE);
		else
			mNoteView.setText(mNote);

		mValue = (TextView) view.findViewById(R.id.seekbar_value);
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
		mSeekBar.setMax(mSteppedMaxValue - mSteppedMinValue);

		setValue(AppHelper.getIntOfAppPrefs(getKey(), mDefaultValue));

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mListener != null) mListener.onStopTrackingTouch(seekBar);
				saveValue();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (mListener != null) mListener.onStartTrackingTouch(seekBar);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (mListener != null) mListener.onProgressChanged(seekBar, getValue(), fromUser);
				updateDisplay(progress);
			}
		});
		getView(view.itemView);
		view.setDividerAllowedAbove(false);
	}

	public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
		mListener = listener;
	}

	public int getMinValue() {
		return mMinValue;
	}

	public void setMinValue(int value) {
		mMinValue = value;
		updateAllValues();
	}

	public int getMaxValue() {
		return mMaxValue;
	}

	public void setMaxValue(int value) {
		mMaxValue = value;
		updateAllValues();
	}

	public int getStepValue() {
		return mStepValue;
	}

	public void setStepValue(int value) {
		mStepValue = value;
		updateAllValues();
	}

	public String getFormat() {
		return mFormat;
	}

	private void setFormat(String format) {
		mFormat = format;
		updateDisplay();
	}

	public void setFormat(int formatResId) {
		setFormat(getContext().getResources().getString(formatResId));
	}

	public int getValue() {
		return mSeekBar == null ? mDefaultValue : (mSeekBar.getProgress() + mSteppedMinValue) * mStepValue;
	}

	public void setValue(int value) {
		setValue(value, false);
	}
	public void setValue(int value, boolean save) {
		value = getBoundedValue(value) - mSteppedMinValue;
		mSeekBar.setProgress(value);
		updateDisplay(value);
		if (save) {
			saveValue();
		}
	}

	public void setDefaultValue(int value) {
		mDefaultValue = value;
	}

	private void updateAllValues() {
		int currentValue = getValue();
		if (mMaxValue <= mMinValue) mMaxValue = mMinValue + 1;
		mSteppedMinValue = Math.round((float)mMinValue / mStepValue);
		mSteppedMaxValue = Math.round((float)mMaxValue / mStepValue);

		if (mSeekBar != null) mSeekBar.setMax(mSteppedMaxValue - mSteppedMinValue);

		currentValue = getBoundedValue(currentValue) - mSteppedMinValue;

		if (mSeekBar != null) {
			mSeekBar.setProgress(currentValue);
			updateDisplay(currentValue);
		}
	}

	private int getBoundedValue(int value) {
		value = Math.round((float)value / mStepValue);
		if (value < mSteppedMinValue) value = mSteppedMinValue;
		if (value > mSteppedMaxValue) value = mSteppedMaxValue;
		return value;
	}

	private void updateDisplay() {
		updateDisplay(mSeekBar.getProgress());
	}

	private void updateDisplay(int value) {
		if (!TextUtils.isEmpty(mFormat)) {
			mValue.setVisibility(View.VISIBLE);
			value = (value + mSteppedMinValue) * mStepValue;
			String text;

			if (value == mDefaultValue && mOffText != null) {
				mValue.setText(mOffText);
				return;
			}

			if (mNegativeShift > 0) value -= mNegativeShift;

			try {
				if (mUseDisplayDividerValue) {
					float floatValue = (float)value / (float)mDisplayDividerValue;
					text = String.format(mFormat, floatValue);
				} else {
					text = String.format(mFormat, value);
				}
			} catch (IllegalFormatException e) {
				e.printStackTrace();
				text = Integer.toString(value);
			}
			if (mShowPlus && value > 0) text = "+" + text;
			mValue.setText(text);
		} else {
			mValue.setVisibility(View.GONE);
		}
	}

	private void saveValue() {
		AppHelper.appPrefs.edit().putInt(getKey(), getValue()).apply();
	}

	public void setUnsupported(boolean value) {
		unsupported = value;
		setEnabled(!value);
	}

	@Override
	public void markAsNew() {
		newmod = true;
	}

	@Override
	public void applyHighlight() {
		highlight = true;
	}
}

