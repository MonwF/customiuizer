package name.mikanoshi.customiuizer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import name.mikanoshi.customiuizer.MainActivity;
import name.mikanoshi.customiuizer.R;

public class DownloadAndUnZip extends AsyncTask<String, Integer, String> {
	WeakReference<Activity> act;
	ProgressDialog mProgressDialog;

	public DownloadAndUnZip(Activity actzip) {
		this.act = new WeakReference<Activity>(actzip);
		final DownloadAndUnZip task = this;
		mProgressDialog = new ProgressDialog(actzip);
		mProgressDialog.setTitle(Helpers.l10n(actzip, R.string.download_title));
		mProgressDialog.setMessage(Helpers.l10n(actzip, R.string.download_desc));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				task.cancel(true);
			}
		});
	}

	@Override
	protected String doInBackground(String... sUrl) {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		
		try {
			URL url = new URL(sUrl[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
				return null; // "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
			}
			int fileLength = connection.getContentLength();

			input = connection.getInputStream();
			File tmp = new File(Helpers.dataPath);
			if (!tmp.exists()) tmp.mkdirs();
			tmp.setReadable(true, false);
			tmp.setWritable(true, false);
			tmp.setExecutable(true, false);
			output = new FileOutputStream(Helpers.dataPath + "strings.zip", false);

			byte[] data = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				if (isCancelled()) {
					input.close();
					break;
				}
				total += count;
				if (fileLength > 0) publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
			}
		} catch (Exception e) {
			if (act.get() != null && !act.get().isFinishing())
			act.get().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder alert = new AlertDialog.Builder(act.get());
					alert.setTitle(Helpers.l10n(act.get(), R.string.warning));
					alert.setView(Helpers.createCenteredText(act.get(), R.string.download_failed));
					alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {}
					});
					alert.show();
				}
			});
			e.printStackTrace();
			return null;
		}
		
		try {
			if (output != null) output.close();
			if (input != null) input.close();
			if (connection != null) connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "OK";
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			String buildIdBefore = "";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Helpers.dataPath + "version")))) {
				buildIdBefore = br.readLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
			AlertDialog.Builder alert = new AlertDialog.Builder(act.get());
			boolean unpacked = unpackZip(Helpers.dataPath, "strings.zip");
			if (act.get() != null && !act.get().isFinishing()) {
				if (unpacked) {
					String buildIdAfter = "";
					try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Helpers.dataPath + "version")))) {
						buildIdAfter = br.readLine();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (buildIdAfter == "") {
						alert.setTitle(Helpers.l10n(act.get(), R.string.warning));
						alert.setView(Helpers.createCenteredText(act.get(), R.string.download_version_problem));
						alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
					} else if (!buildIdBefore.equals(buildIdAfter)) {
						alert.setTitle(Helpers.l10n(act.get(), R.string.success));
						alert.setView(Helpers.createCenteredText(act.get(), R.string.download_succeeded));
						alert.setCancelable(false);
						alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								Helpers.l10n = null;
								Helpers.cLang = "";
								Activity activity = act.get();
								activity.startActivity(new Intent(activity, MainActivity.class));
								activity.finish();
							}
						});
					} else {
						alert.setTitle(Helpers.l10n(act.get(), R.string.warning));
						alert.setView(Helpers.createCenteredText(act.get(), R.string.download_same_version));
						alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {}
						});
					}
				} else {
					alert.setTitle(Helpers.l10n(act.get(), R.string.warning));
					alert.setView(Helpers.createCenteredText(act.get(), R.string.download_unzip_failed));
					alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {}
					});
				}
				alert.show();
			}
		}
		if (act.get() != null && !act.get().isFinishing() && mProgressDialog.isShowing()) try { mProgressDialog.dismiss(); } catch (Throwable t) {}
	}
	
	private boolean unpackZip(String path, String zipname) {
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(path + zipname)))) {
			String filename;
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) {
				filename = ze.getName();
				File fmd = new File(path + filename);
				if (ze.isDirectory()) {
					fmd.mkdirs();
					fmd.setReadable(true, false);
					fmd.setWritable(true, false);
					fmd.setExecutable(true, false);
					continue;
				}
				try (FileOutputStream fout = new FileOutputStream(fmd, false)) {
					while ((count = zis.read(buffer)) != -1) fout.write(buffer, 0, count);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				zis.closeEntry();
				fmd.setReadable(true, false);
				fmd.setWritable(true, false);
				fmd.setExecutable(true, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}