package name.mikanoshi.customiuizer.crashreport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.file.BulkReportDeleter;
import org.acra.file.CrashReportPersister;
import org.acra.sender.ReportSenderException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;

import miui.app.ProgressDialog;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.utils.Helpers;

import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

public class Dialog extends Activity {

	private ProgressDialog loader;
	private CrashReportData crashData;
	private CoreConfiguration config;
	private File reportFile;
	private StringBuilder debugLog = new StringBuilder();
	private EditText desc;
	String errorText = null;

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	void showFinishDialog(boolean isOk, String details) {
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle(R.string.crash_result);
		dlg.setCancelable(true);
		if (isOk)
			dlg.setMessage(R.string.crash_ok);
		else {
			String errorTxt = getResources().getString(R.string.crash_error);
			if (details != null) errorTxt += ": " + details;
			dlg.setMessage(errorTxt);
		}
		dlg.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		dlg.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		});
		dlg.show();
	}

	private String getProp(String prop) {
		String res = "";
		Process ifc = null;
		try {
			ifc = Runtime.getRuntime().exec("getprop " + prop);
			BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()), 2048);
			res = bis.readLine();
		} catch (Throwable t) {} finally {
			if (ifc != null) ifc.destroy();
		}
		return res;
	}

	private String getXposedPropVersion(File propFile) {
		String version = "unknown";
		try (FileInputStream inputStream = new FileInputStream(propFile)) {
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
				while (true) {
					String readLine = null;
					try {
						readLine = bufferedReader.readLine();
					} catch (Throwable t) {
						t.printStackTrace();
					}
					if (readLine == null) break;

					String[] split = readLine.split("=", 2);
					if (split.length == 2) {
						String line = split[0].trim();
						if (line.charAt(0) != '#' && "version".equals(line)) {
							version = split[1].trim();
							break;
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return version.equals("unknown") ? version + " (" + Helpers.xposedVersion + ")" : version;
	}

	private void sendCrash() {
		crashData.put(USER_COMMENT, desc.getText().toString());

		loader.setMessage(getResources().getString(R.string.crash_sending_report));
		loader.show();

		new Thread(() -> {
			final boolean res = sendReport();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					loader.hide();
					if (res) {
						Helpers.emptyFile(Helpers.getProtectedContext(Dialog.this).getFilesDir().getAbsolutePath() + "/uncaught_exceptions", true);
						cancelReports();
						showFinishDialog(true, null);
					} else {
						showFinishDialog(false, errorText == null ? "REQUEST_ERROR" : errorText);
					}
				}
			});
		}).start();
	}

	protected boolean sendReport() {
		try {
			byte[] jsonData = crashData.toJSON().getBytes(StandardCharsets.UTF_8);
			if (jsonData.length == 0) {
				errorText = "ZERO_LENGTH";
				return false;
			}

			//final String basicAuth = "Basic " + Base64.encodeToString("login:pass".getBytes(), Base64.NO_WRAP);
			URL url = new URL("https://code.highspec.ru/crashreports/reporter.php");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Encoding", "gzip");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.connect();

			try (OutputStream os = conn.getOutputStream()) {
				try (GZIPOutputStream dataStream = new GZIPOutputStream(os)) {
					dataStream.write(jsonData);
					dataStream.flush();
				} catch (Throwable t) {}
			} catch (Throwable t) {}

//			StringBuilder builder = new StringBuilder();
//			try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
//				String tmp;
//				while ((tmp = in.readLine()) != null) builder.append(tmp);
//			}
//			Log.e("miuizer", "Response: " + builder.toString());

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) throw new ReportSenderException(String.valueOf(conn.getResponseMessage()));
			//Log.e("HTTP", "Report server response code: " + String.valueOf(conn.getResponseCode()));
			//Log.e("HTTP", "Report server response: " + conn.getResponseMessage());
			conn.disconnect();
			return true;
		} catch (Throwable t) {
			errorText = t.getMessage();
			t.printStackTrace();
			return false;
		}
	}

	protected final void cancelReports() {
		(new Thread(() -> (new BulkReportDeleter(this)).deleteReports(false, 0))).start();
	}

	protected void cancelReportsAndFinish() {
		cancelReports();
		finish();
	}
	
	int densify(int size) {
		return Math.round(getResources().getDisplayMetrics().density * size);
	}

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(0, 0);
		Helpers.setMiuiTheme(this, R.style.ApplyInvisible, true);
		super.onCreate(savedInstanceState);

		if (getIntent().getBooleanExtra("FORCE_CANCEL", false)) {
			cancelReportsAndFinish();
			return;
		}

		Serializable sConfig = getIntent().getSerializableExtra(DialogInteraction.EXTRA_REPORT_CONFIG);
		Serializable sReportFile = getIntent().getSerializableExtra(DialogInteraction.EXTRA_REPORT_FILE);
		if (sConfig instanceof CoreConfiguration && sReportFile instanceof File) {
			config = (CoreConfiguration) sConfig;
			reportFile = (File)sReportFile;
		} else {
			ACRA.log.w(ACRA.LOG_TAG, "Illegal or incomplete call of CrashReportDialog.");
			finish();
		}

		loader = new ProgressDialog(this);
		loader.setMessage(getResources().getString(R.string.crash_collecting_report));
		loader.setCancelable(false);
		loader.setCanceledOnTouchOutside(false);
		loader.show();

		File sdcardLog = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Helpers.externalFolder + Helpers.logFile);
		if (sdcardLog.exists()) {
			debugLog.append("Log on external storage found, removing\n");
			sdcardLog.delete();
		}
		debugLog.append("Asking System UI to collect Xposed log\n");
		sendBroadcast(new Intent(GlobalActions.ACTION_PREFIX + "CollectLogs"));

		final Activity act = this;
		new Thread(new Runnable() {
			public void run() {
				try { Thread.sleep(1500); } catch (Throwable t) {}
				act.runOnUiThread(Dialog.this::showReportDialog);
			}
		}).start();
	}

	@SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
	void showReportDialog() {
		int title = R.string.warning;
		int neutralText = R.string.crash_ignore;
		int text = R.string.crash_dialog;
		LinearLayout dialogView = new LinearLayout(this);
		dialogView.setOrientation(LinearLayout.VERTICAL);

		String errorLog = Helpers.getXposedInstallerErrorLog(this);
		debugLog.append("Installer log path: ").append(errorLog).append("\n");

		String xposedLog = null;
		try {
			File sdcardLog = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Helpers.externalFolder + Helpers.logFile);
			File errorLogFile = null;
			if (sdcardLog.exists() && sdcardLog.canRead()) {
				debugLog.append("Log found on external storage: ").append(sdcardLog.getAbsolutePath()).append("\n");
				errorLogFile = sdcardLog;
			} else if (errorLog != null) {
				errorLogFile = new File(errorLog);
				debugLog.append("Log found: ").append(errorLog).append("\n");
			} else debugLog.append("No Xposed log found!\n");

			if (errorLogFile != null)
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(errorLogFile)))) {
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) sb.append(line).append("\n");
				xposedLog = sb.toString();
			} catch (Throwable t) {
				debugLog.append("Error reading log: ").append(t.getMessage()).append("\n");
			}

			if (sdcardLog.exists()) sdcardLog.delete();
		} catch (Throwable t) {}

		SharedPreferences prefs = getSharedPreferences(Helpers.prefsName, Context.MODE_PRIVATE);
		try {
			prefs = Helpers.getProtectedContext(this).getSharedPreferences(Helpers.prefsName, Context.MODE_PRIVATE);
		} catch (Throwable t) {
			Log.e("miuizer", "Failed to use protected storage!");
		}

		CrashReportPersister persister = new CrashReportPersister();
		try {
			crashData = persister.load(reportFile);
			crashData.put(USER_EMAIL, prefs.getString("acra.user.email", ""));
		} catch (Throwable t) {
			t.printStackTrace();
			cancelReports();
			showFinishDialog(false, "REPORT_READ_ERROR");
			return;
		}

		try {
			//Log.e("AndroidRuntime", crashData.getString(ReportField.STACK_TRACE));

			String ROM = getProp("ro.modversion");
			String MIUI = getProp("ro.miui.ui.version.name");

			String kernel = System.getProperty("os.version");
			if (kernel == null) kernel = "";

			JSONObject buildData = (JSONObject)crashData.get("BUILD");
			buildData.put("ROM_VERSION", ROM);
			buildData.put("MIUI_VERSION", MIUI);
			buildData.put("KERNEL_VERSION", kernel);
			crashData.put("BUILD", buildData);
			crashData.put("DEBUG_LOG", debugLog.toString());

			StringBuilder sb = new StringBuilder();
			try (FileInputStream in = new FileInputStream(new File(Helpers.getProtectedContext(this).getFilesDir().getAbsolutePath() + "/uncaught_exceptions"))) {
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {
					String line;
					while ((line = bufferedReader.readLine()) != null) sb.append(line).append("\n");
				} catch (Throwable t) {}
			} catch (Throwable t) {}

			final String[] xposedPropFiles = new String[]{
					"/system/framework/edconfig.jar", // EdXposed
					"/system/xposed.prop", // Classic
					"/magisk/xposed/system/xposed.prop",
					"/magisk/PurifyXposed/system/xposed.prop",
					"/su/xposed/system/xposed.prop",
					"/vendor/xposed.prop",
					"/xposed/xposed.prop",
					"/xposed.prop",
					"/su/xposed/xposed.prop"
			};
			for (String prop: xposedPropFiles) {
				File propFile = new File(prop);
				if (propFile.exists() && propFile.canRead()) {
					crashData.put("XPOSED_VERSION", getXposedPropVersion(propFile));
					break;
				}
			}

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			PackageManager pkgMgr = getPackageManager();
			ResolveInfo launcherInfo = pkgMgr.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (launcherInfo != null) {
				PackageInfo packageInfo = pkgMgr.getPackageInfo(launcherInfo.activityInfo.packageName, 0);
				if (packageInfo != null) crashData.put("LAUNCHER_VERSION", launcherInfo.activityInfo.loadLabel(pkgMgr) + " " + packageInfo.versionName);
			}

			if (!crashData.containsKey("XPOSED_VERSION")) try {
				PackageInfo taichiInfo = pkgMgr.getPackageInfo("me.weishu.exp", 0);
				if (taichiInfo != null)
				crashData.put("XPOSED_VERSION", taichiInfo.applicationInfo.loadLabel(pkgMgr) + " " + taichiInfo.versionName);
			} catch (Throwable t) {}

			crashData.put("SHARED_PREFERENCES", new JSONObject(prefs.getAll()));
			if (!sb.toString().isEmpty())
				crashData.put("UNCAUGHT_EXCEPTIONS", sb.toString());

			if (xposedLog == null || xposedLog.trim().equals(""))
				crashData.put(ReportField.CUSTOM_DATA, "Xposed log is empty...");
			else
				crashData.put(ReportField.CUSTOM_DATA, xposedLog);
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			crashData.put(ReportField.CUSTOM_DATA, "Retrieval failed. Stack trace:\n" + sw.toString());
		}

		int payloadSize;
		try {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				try (GZIPOutputStream dataStream = new GZIPOutputStream(os)) {
					dataStream.write(crashData.toJSON().getBytes(StandardCharsets.UTF_8));
					dataStream.flush();
				}
				payloadSize = os.size();
			}
		} catch (Throwable t) {
			t.printStackTrace();
			cancelReports();
			showFinishDialog(false, "JSON_PAYLOAD_ERROR");
			return;
		}

		if (payloadSize == 0) {
			cancelReports();
			showFinishDialog(false, "EMPTY_PAYLOAD_ERROR");
			return;
		}

		try {
			TextView mainText = new TextView(this);
			mainText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			mainText.setGravity(Gravity.START);
			mainText.setPadding(0, 0, 0, densify(10));
			mainText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			mainText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

			TextView descText = new TextView(this);
			descText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			descText.setGravity(Gravity.START);
			descText.setPadding(0, 0, 0, densify(5));
			descText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			descText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

			desc = new EditText(this);
			desc.setGravity(Gravity.TOP | Gravity.START);
			desc.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
			desc.setSingleLine(false);
			desc.setPadding(densify(5), densify(5), densify(5), densify(5));

			boolean isManualReport = crashData.getString(ReportField.STACK_TRACE).contains("Report requested by developer");
			if (isManualReport) {
				title = R.string.crash_confirm;
				text = R.string.crash_dialog_manual;
				neutralText = R.string.cancel;
				descText.setText(R.string.crash_dialog_manual_desc);

				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, densify(80));
				lp.setMargins(0, densify(5), 0, densify(10));
				desc.setLayoutParams(lp);
			} else {
				descText.setText(R.string.crash_dialog_manual_desc2);

				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, densify(60));
				lp.setMargins(0, densify(5), 0, densify(10));
				desc.setLayoutParams(lp);
				desc.setFocusable(false);
				desc.setFocusableInTouchMode(false);
				desc.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						v.setFocusable(true);
						v.setFocusableInTouchMode(true);
						v.performClick();
						return false;
					}
				});
			}

			mainText.setText(text);

			TextView feedbackNote = new TextView(this);
			feedbackNote.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			feedbackNote.setGravity(Gravity.START);
			feedbackNote.setPadding(0, 0, 0, 0);
			feedbackNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			feedbackNote.setText(R.string.crash_dialog_note);

			dialogView.addView(mainText);
			dialogView.addView(descText);
			dialogView.addView(desc);

			String email = Helpers.prefs.getString("acra.user.email", "");
			if (Objects.equals(email, ""))
				dialogView.addView(feedbackNote);

			mainText.setText(mainText.getText() + "\n" + getResources().getString(R.string.crash_dialog_manual_size) + ": " + Math.round(payloadSize / 1024.0f) + " KB");
		} catch (Throwable t) {}

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setView(dialogView);
		alert.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				cancelReportsAndFinish();
			}
		});
		alert.setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				cancelReportsAndFinish();
			}
		});
		alert.setPositiveButton(R.string.crash_send, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {}
		});
		loader.hide();
		final AlertDialog alertDlg = alert.show();
		alertDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (desc != null && desc.getText().toString().trim().equals("")) {
					Toast.makeText(Dialog.this, R.string.crash_needs_desc, Toast.LENGTH_LONG).show();
				} else if (!isNetworkAvailable()) {
					Toast.makeText(Dialog.this, R.string.crash_needs_inet, Toast.LENGTH_LONG).show();
				} else {
					try {
						InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						View currentFocusedView = getCurrentFocus();
						if (currentFocusedView != null)
							inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Throwable t) {
						Log.e("miuizer", t.getMessage());
					}
					alertDlg.dismiss();
					sendCrash();
				}
			}
		});
	}

	protected final CoreConfiguration getConfig() {
		return config;
	}

	@SuppressWarnings({"deprecation", "JavaReflectionMemberAccess"})
	private void updateBlurRatio() {
		try {
			View rootView = getWindow().getDecorView();
			if (rootView.getLayoutParams() instanceof WindowManager.LayoutParams) {
				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)rootView.getLayoutParams();
				layoutParams.flags |= WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
				Field blurRatio = WindowManager.LayoutParams.class.getDeclaredField("blurRatio");
				blurRatio.setAccessible(true);
				blurRatio.set(layoutParams, Helpers.isNightMode(this) ? 0.5f :  0.75f);
				getWindowManager().updateViewLayout(rootView, layoutParams);
			}
		} catch (Throwable t) {}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateBlurRatio();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		updateBlurRatio();
	}

	@Override
	public void onDetachedFromWindow() {
		if (loader != null && loader.isShowing()) loader.dismiss();
		super.onDetachedFromWindow();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}
}
