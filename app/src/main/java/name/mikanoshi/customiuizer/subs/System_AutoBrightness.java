package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;
import android.widget.SeekBar;

import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.prefs.SeekBarPreference;
import name.mikanoshi.customiuizer.utils.Helpers;

public class System_AutoBrightness extends SubFragment {
	SeekBarPreference minBrightness;
	SeekBarPreference maxBrightness;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		maxBrightness = (SeekBarPreference)findPreference("pref_key_system_autobrightness_max");
		minBrightness = (SeekBarPreference)findPreference("pref_key_system_autobrightness_min");
		minBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (!fromUser) return;
				if (maxBrightness.getValue() <= progress) maxBrightness.setValue(progress + 1);
				maxBrightness.setMinValue(progress + 1);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

}
