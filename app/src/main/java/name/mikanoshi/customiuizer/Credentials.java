package name.mikanoshi.customiuizer;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import javax.crypto.KeyGenerator;

public class Credentials extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			KeyguardManager km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
			if (km.isKeyguardSecure()) {
				try {
					KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder("dummy", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setUserAuthenticationRequired(true);
					KeyGenerator keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
					keygen.init(builder.build());
					keygen.generateKey();
					Toast.makeText(this, R.string.credentials_ok, Toast.LENGTH_SHORT).show();
					finish();
				} catch (Throwable e) {
					Intent authIntent = km.createConfirmDeviceCredentialIntent(getString(R.string.credentials_unlock), getString(R.string.dummy));
					startActivityForResult(authIntent, 0);
				}
			} else {
				finish();
				Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
				startActivity(intent);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
		if (resultCode == Activity.RESULT_OK)
		Toast.makeText(this, R.string.credentials_success, Toast.LENGTH_SHORT).show();
	}

}
