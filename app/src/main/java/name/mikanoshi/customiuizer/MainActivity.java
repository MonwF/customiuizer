package name.mikanoshi.customiuizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

public class MainActivity extends ActivityEx {

	public static final int REQUEST_BACKUP_PERMISSIONS = 1;
	MainFragment mainFrag = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Helpers.dataPath = getFilesDir().getPath();
		Helpers.backupPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CustoMIUIzer/";
		if (!launch) return;
		mainFrag = new MainFragment();
		getFragmentManager().beginTransaction().replace(R.id.fragment_container, mainFrag).commit();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_BACKUP_PERMISSIONS:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					mainFrag.backupSettings(this);
				else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
					Toast.makeText(this, "Do you want to write backup or not?", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, "You'll have to manually enable permission for this option now. Good job!", Toast.LENGTH_LONG).show();
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

}