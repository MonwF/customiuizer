package name.mikanoshi.customiuizer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import name.mikanoshi.customiuizer.utils.Helpers;

public class SnoozedActivity extends Activity {

	@Override
	protected void attachBaseContext(Context base) {
		try {
			super.attachBaseContext(Helpers.getLocaleContext(base));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		boolean mSDKFound = ((MainApplication)getApplication()).mStarted;
		if (mSDKFound) Helpers.setMiuiTheme(this, R.style.MIUIPrefs);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snoozed);

		if (!mSDKFound) {
			Toast.makeText(this, R.string.sdk_failed, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		getFragmentManager().beginTransaction().replace(R.id.fragment_container, new SnoozedFragment()).commit();
	}

}
