package name.mikanoshi.customiuizer.utils;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;

public class PrefBackupAgent extends BackupAgentHelper {

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(Helpers.getProtectedContext(this), "customiuizer_prefs");
			addHelper("prefs_helper", helper);
		} catch (Throwable t) {}
	}
	
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		super.onRestore(data, appVersionCode, newState);
		try {
			SharedPreferences prefs = Helpers.getProtectedContext(this).getSharedPreferences("customiuizer_prefs", Context.MODE_PRIVATE);
			prefs.edit().putBoolean("pref_key_was_restore", true).apply();
		} catch (Throwable t) {}
	}
}
