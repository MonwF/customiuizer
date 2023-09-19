package name.monwf.customiuizer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class CredentialsShortcut extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent();
		Intent launchIntent = new Intent(this, Credentials.class);
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.credentials_unlock));
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_credentials));

		setResult(RESULT_OK, intent);
		finish();
	}

}
