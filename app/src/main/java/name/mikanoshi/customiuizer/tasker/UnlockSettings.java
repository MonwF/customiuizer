package name.mikanoshi.customiuizer.tasker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import name.mikanoshi.customiuizer.R;

public class UnlockSettings extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tasker_unlock);

		Button ok = findViewById(R.id.force_ok);
		ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int opt = ((RadioGroup)findViewById(R.id.force_option)).getCheckedRadioButtonId();
				int val = -1;
				if (opt == R.id.force_locked) val = 0;
				else if (opt == R.id.force_unlocked) val = 1;
				Intent resultIntent = new Intent();
				resultIntent.putExtra(Constants.EXTRA_STRING_BLURB, getString(val == 1 ? R.string.system_noscreenlock_force_unlocked : (val == 0 ? R.string.system_noscreenlock_force_locked : R.string.system_noscreenlock_force_off)));
				Bundle bundle = new Bundle();
				bundle.putInt("system_noscreenlock_force", val);
				//bundle.putString("net.dinglisch.android.tasker.extras.VARIABLE_REPLACE_KEYS", "lockstate");
				//resultIntent.putExtra("net.dinglisch.android.tasker.RELEVANT_VARIABLES", new String[] { "%lockstate\nLock state\nCurrent state of lock override:<br><b>0</b> force locked<br><b>1</b> force unlocked<br><b>-1</b> do not force"});
				//resultIntent.putExtra("net.dinglisch.android.tasker.extras.REQUESTED_TIMEOUT", 3000);
				resultIntent.putExtra(Constants.EXTRA_BUNDLE, bundle);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}
		});

		Bundle bundle = getIntent().getBundleExtra(Constants.EXTRA_BUNDLE);
		if (bundle != null) {
			int opt = bundle.getInt("system_noscreenlock_force", -1);
			((RadioGroup)findViewById(R.id.force_option)).check(opt == 0 ? R.id.force_locked : (opt == 1 ? R.id.force_unlocked : R.id.force_off));
		}
	}

}
