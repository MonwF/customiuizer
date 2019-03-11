package name.mikanoshi.customiuizer;

import android.app.Application;
import android.content.Context;

import com.google.auto.service.AutoService;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.data.StringFormat;

import miui.core.SdkManager;

import static org.acra.ReportField.*;

@AcraCore(
	reportContent = { REPORT_ID, APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME, FILE_PATH, PHONE_MODEL, BRAND, PRODUCT, ANDROID_VERSION,
		BUILD, TOTAL_MEM_SIZE, AVAILABLE_MEM_SIZE, CUSTOM_DATA, STACK_TRACE, INITIAL_CONFIGURATION, CRASH_CONFIGURATION, DISPLAY, USER_COMMENT, USER_EMAIL,
		USER_APP_START_DATE, USER_CRASH_DATE, DUMPSYS_MEMINFO, LOGCAT, INSTALLATION_ID, DEVICE_FEATURES, ENVIRONMENT, SHARED_PREFERENCES,
		SETTINGS_SYSTEM, SETTINGS_SECURE, SETTINGS_GLOBAL },
	sharedPreferencesName = "customiuizer_prefs",
	//additionalSharedPreferences = {"customiuizer_prefs"},
	logcatArguments = { "-t", "500", "-v", "time" },
	buildConfigClass = BuildConfig.class,
	reportFormat = StringFormat.JSON
)
@AcraDialog(
	reportDialogClass = CrashReportDialog.class
)

public class MainApplication extends Application {

	public MainApplication() {
		SdkManager.initialize(this, null);
	}

	public void onCreate() {
		super.onCreate();
		SdkManager.start(null);
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		ACRA.init(this);
		ACRA.DEV_LOGGING = true;
	}

}
