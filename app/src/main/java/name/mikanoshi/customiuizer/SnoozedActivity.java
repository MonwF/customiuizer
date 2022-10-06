package name.mikanoshi.customiuizer;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import name.mikanoshi.customiuizer.utils.Helpers;

public class SnoozedActivity extends AppCompatActivity {

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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snoozed);

		if (!mSDKFound) {
			finish();
			return;
		}

		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SnoozedFragment()).commit();
	}

}
