package name.mikanoshi.customiuizer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HelperReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context ctx, Intent intent) {
		if (intent.getAction() == null) return;
		
		if (intent.getAction().equals("name.mikanoshi.customiuizer.SAVEEXCEPTION")) {
			try {
				Throwable thw = (Throwable)intent.getSerializableExtra("throwable");
				StringWriter stackTrace = new StringWriter();
				thw.printStackTrace(new PrintWriter(stackTrace));
				
				File f = new File(ctx.getFilesDir().getAbsolutePath() + "/uncaught_exceptions");
				if (!f.exists()) f.createNewFile();
				
				try (FileOutputStream fOut = new FileOutputStream(f, true)) {
					try (OutputStreamWriter output = new OutputStreamWriter(fOut)) {
						output.write(stackTrace.toString() + "\n\n");
					}
				}
			} catch (Throwable t) {}
		}
	}
}
