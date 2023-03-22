package name.mikanoshi.customiuizer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import name.mikanoshi.customiuizer.mods.GlobalActions;

public class HelperReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context ctx, Intent intent) {
		if (intent.getAction() == null) return;

		String action = intent.getAction();
		if (action.equals("name.mikanoshi.customiuizer.SAVEEXCEPTION")) {
			try {
				Throwable thw = (Throwable)intent.getSerializableExtra("throwable");
				if (thw == null) return;
				StringWriter stackTrace = new StringWriter();

				File f = new File(ctx.getFilesDir().getAbsolutePath() + "/uncaught_exceptions");
				if (!f.exists()) f.createNewFile();
				
				try (FileOutputStream fOut = new FileOutputStream(f, true)) {
					try (OutputStreamWriter output = new OutputStreamWriter(fOut)) {
						output.write(stackTrace + "\n\n");
					}
				}
			} catch (Throwable t) {}
		}
		else if (action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
			Helpers.fixPermissionsAsync(ctx);
		}
		else if ("android.telephony.action.SECRET_CODE".equals(action)) {
			Intent startIntent = new Intent(GlobalActions.EVENT_PREFIX + "START_PRIVACY_SPACE");
			startIntent.setPackage("com.miui.home");
			ctx.sendBroadcast(startIntent);
		}
	}
}
