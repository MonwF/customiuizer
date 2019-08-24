package name.mikanoshi.customiuizer.prefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.IllegalFormatException;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

public class SeekBarPreference extends Preference implements PreferenceState {

	private boolean dynamic;
	private boolean newmod = false;

	private int mDefaultValue;
	private int mMinValue;
	private int mMaxValue;
	private int mStepValue;

	private int mDisplayDividerValue;
	private boolean mUseDisplayDividerValue;

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
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);

			dynamic = a.getBoolean(R.styleable.SeekBarPreference_dynamic, false);
			mMinValue = a.getInt(R.styleable.SeekBarPreference_minValue, 0);
			mMaxValue = a.getInt(R.styleable.SeekBarPreference_maxValue, 10);
			mStepValue = a.getInt(R.styleable.SeekBarPreference_stepValue, 1);
			mDefaultValue = a.getInt(R.styleable.SeekBarPreference_android_defaultValue, 0);

			if (a.hasValue(R.styleable.SeekBarPreference_displayDividerValue)) {
				mUseDisplayDividerValue = true;
				mDisplayDividerValue = a.getInt(R.styleable.SeekBarPreference_displayDividerValue, 1);
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

			mFormat = a.getString(R.styleable.SeekBarPreference_format);
			mNote = a.getString(R.styleable.SeekBarPreference_note);
			mOffText = a.getString(R.styleable.SeekBarPreference_offtext);

			a.recycle();
		} else {
			mMinValue = 0;
			mMaxValue = 10;
			mStepValue = 1;
			mDefaultValue = 0;
		}

		mSteppedMinValue = Math.round((float)mMinValue / mStepValue);
		mSteppedMaxValue = Math.round((float)mMaxValue / mStepValue);
	}

	@Override
	@SuppressLint("SetTextI18n")
	public View getView(View view, ViewGroup parent) {
		View finalView = super.getView(view, parent);
		TextView title = finalView.findViewById(android.R.id.title);
		title.setText(getTitle() + (dynamic ? " ⟲" : ""));
		if (newmod) Helpers.applyNewMod(title);
		return finalView;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		setLayoutResource(R.layout.preference_seekbar);

		View view = super.onCreateView(parent);

		TextView mSummaryView = view.findViewById(android.R.id.summary);
		if (!TextUtils.isEmpty(getSummary()))
			mSummaryView.setText(getSummary());
		else
			mSummaryView.setVisibility(View.GONE);

		TextView mNoteView = view.findViewById(android.R.id.text1);
		if (mNote == null || mNote.equals(""))
			mNoteView.setVisibility(View.GONE);
		else
			mNoteView.setText(mNote);

		mValue = view.findViewById(R.id.seekbar_value);
		mSeekBar = view.findViewById(R.id.seekbar);
		mSeekBar.setMax(mSteppedMaxValue - mSteppedMinValue);

		setValue(Helpers.prefs.getInt(getKey(), mDefaultValue));

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

		return view;
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

	private int getValue() {
		return mSeekBar == null ? mDefaultValue : (mSeekBar.getProgress() + mSteppedMinValue) * mStepValue;
	}

	private void setValue(int value) {
		value = getBoundedValue(value) - mSteppedMinValue;
		mSeekBar.setProgress(value);
		updateDisplay(value);
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
			mValue.setText(text);
		} else {
			mValue.setVisibility(View.GONE);
		}
	}

	private void saveValue() {
		Helpers.prefs.edit().putInt(getKey(), getValue()).apply();
	}

	@Override
	public void markAsNew() {
		newmod = true;
	}
}