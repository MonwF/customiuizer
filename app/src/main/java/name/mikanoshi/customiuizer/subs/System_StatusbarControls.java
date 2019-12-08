package name.mikanoshi.customiuizer.subs;

import android.os.Bundle;

import name.mikanoshi.customiuizer.SubFragment;

public class System_StatusbarControls extends SubFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		findPreference("pref_key_system_statusbarcontrols_dt").setOnPreferenceClickListener(openStatusbarActions);
	}

}
