package name.mikanoshi.customiuizer.crashreport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.acra.ACRA;
import org.acra.config.CoreConfiguration;
import org.acra.interaction.ReportInteraction;
import org.acra.prefs.SharedPreferencesFactory;

import java.io.File;

import static org.acra.ACRA.LOG_TAG;

public class DialogInteraction implements ReportInteraction {
	static final String EXTRA_REPORT_FILE = "REPORT_FILE";
	static final String EXTRA_REPORT_CONFIG = "REPORT_CONFIG";

	public DialogInteraction() {
		super();
	}

	@Override
	public boolean performInteraction(Context context, CoreConfiguration config, File reportFile) {
		final SharedPreferences prefs = new SharedPreferencesFactory(context, config).create();
		if (prefs.getBoolean(ACRA.PREF_ALWAYS_ACCEPT, false)) return true;
		if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating CrashReportDialog for " + reportFile);
		final Intent dialogIntent = createCrashReportDialogIntent(context, config, reportFile);
		dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		context.startActivity(dialogIntent);
		return false;
	}

	private Intent createCrashReportDialogIntent(Context context, CoreConfiguration config, File reportFile) {
		if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Creating DialogIntent for " + reportFile);
		final Intent dialogIntent = new Intent(context, Dialog.class);
		dialogIntent.putExtra(EXTRA_REPORT_FILE, reportFile);
		dialogIntent.putExtra(EXTRA_REPORT_CONFIG, config);
		return dialogIntent;
	}
}