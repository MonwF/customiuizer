package name.mikanoshi.customiuizer.crashreport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_EMAIL;

public class Dialog extends Activity {

	private CoreConfiguration config;
	private File reportFile;
	private String xposedLog;
	private EditText desc;
	
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	private String getXposedLog() {
		String errorLogPath = Helpers.getXposedInstallerErrorLog(this);
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(errorLogPath))))) {
			String line;
			while ((line = reader.readLine()) != null) sb.append(line).append("\n");
			return sb.toString();
		} catch (Throwable e) {
			return null;
		}
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
		} catch (Throwable e) {} finally {
			if (ifc != null) ifc.destroy();
		}
		return res;
	}
	
	private void sendCrash(final String xposedLogStr) {
		CrashReportPersister persister = new CrashReportPersister();
		SharedPreferences prefs;
		try {
			prefs = Helpers.getProtectedContext(this).getSharedPreferences(Helpers.prefsName, Context.MODE_PRIVATE);
		} catch (Throwable t) {
			Log.e("prefs", "Failed to use protected storage!");
			return;
		}
		final CrashReportData crashData;
		try {
			crashData = persister.load(reportFile);
			crashData.put(USER_COMMENT, desc.getText().toString());
			crashData.put(USER_EMAIL, prefs.getString("acra.user.email", ""));
		} catch (Throwable t) {
			t.printStackTrace();
			cancelReports();
			showFinishDialog(false, "cannot read report file");
			return;
		}

		try {
			String ROM = getProp("ro.modversion");
			String MIUI = getProp("ro.miui.ui.version.name");
			
			String kernel = System.getProperty("os.version");
			if (kernel == null) kernel = "";
			
			JSONObject buildData = (JSONObject)crashData.get("BUILD");
			buildData.put("ROM_VERSION", ROM);
			buildData.put("MIUI_VERSION", MIUI);
			buildData.put("KERNEL_VERSION", kernel);
			crashData.put("BUILD", buildData);

			StringBuilder sb = new StringBuilder();
			try (FileInputStream in = new FileInputStream(new File(getFilesDir().getAbsolutePath() + "/uncaught_exceptions"))) {
				try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {
					String line;
					while ((line = bufferedReader.readLine()) != null) sb.append(line).append("\n");
				} catch (Throwable t) {}
			} catch (Throwable t) {}

			crashData.put("SHARED_PREFERENCES", new JSONObject(prefs.getAll()));
			if (!sb.toString().isEmpty())
			crashData.put("UNCAUGHT_EXCEPTIONS", sb.toString());

			if (xposedLogStr == null || xposedLogStr.trim().equals(""))
				crashData.put(ReportField.CUSTOM_DATA, "Xposed log is empty...");
			else
				crashData.put(ReportField.CUSTOM_DATA, xposedLogStr);
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			crashData.put(ReportField.CUSTOM_DATA, "Retrieval failed. Stack trace:\n" + sw.toString());
		}

		new Thread(() -> {
			boolean res;
			try {
				res = sendReport(crashData);
			} catch (Throwable t) {
				res = false;
			}

			boolean finalRes = res;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (finalRes) {
						Helpers.emptyFile(getFilesDir().getAbsolutePath() + "/uncaught_exceptions", true);
						cancelReports();
						showFinishDialog(true, null);
					} else {
						showFinishDialog(false, "request failed");
					}
				}
			});
		}).start();
	}

	protected boolean sendReport(CrashReportData report) {
		try {
			String json = report.toJSON();

			//final String basicAuth = "Basic " + Base64.encodeToString("login:pass".getBytes(), Base64.NO_WRAP);
			URL url = new URL("https://code.highspec.ru/crashreports/reporter.php");
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.connect();
			try (OutputStream os = conn.getOutputStream()) {
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
					writer.write(json);
					writer.flush();
				} catch (Throwable t) {}
			} catch (Throwable t) {}
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) throw new ReportSenderException(String.valueOf(conn.getResponseMessage()));
			//Log.e("HTTP", "Report server response code: " + String.valueOf(conn.getResponseCode()));
			//Log.e("HTTP", "Report server response: " + conn.getResponseMessage());
			conn.disconnect();
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
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
	@SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
	protected final void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(0, 0);
		Helpers.setMiuiTheme(this, R.style.ApplyInvisible);
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

		int title = R.string.warning;
		int neutralText = R.string.crash_ignore;
		int text = R.string.crash_dialog;
		LinearLayout dialogView = new LinearLayout(this);
		dialogView.setOrientation(LinearLayout.VERTICAL);

		int tries = 5;
		xposedLog = null;
		while (xposedLog == null && tries > 0) try {
			tries--;
			xposedLog = getXposedLog();
			if (xposedLog == null) Thread.sleep(500);
		} catch (Throwable e) {}

		try {
			CrashReportPersister persister = new CrashReportPersister();
			CrashReportData crashData = persister.load(reportFile);
			String payload = crashData.toJSON();
			int payloadSize = payload.getBytes(StandardCharsets.UTF_8).length;
			boolean isManualReport = crashData.getString(ReportField.STACK_TRACE).contains("Report requested by developer");
			Log.e("AndroidRuntime", crashData.getString(ReportField.STACK_TRACE));

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
		} catch (Throwable e) {}

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
		final AlertDialog alertDlg = alert.show();
		alertDlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (desc != null && desc.getText().toString().trim().equals("")) {
					Toast.makeText(Dialog.this, R.string.crash_needs_desc, Toast.LENGTH_LONG).show();
				} else if (!isNetworkAvailable()) {
					Toast.makeText(Dialog.this, R.string.crash_needs_inet, Toast.LENGTH_LONG).show();
				} else {
					alertDlg.dismiss();
					sendCrash(xposedLog);
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
				blurRatio.set(layoutParams, 0.75f);
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
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}
}
