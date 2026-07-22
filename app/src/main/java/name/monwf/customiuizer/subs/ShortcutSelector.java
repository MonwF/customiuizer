package name.monwf.customiuizer.subs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragmentWithSearch;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.ResolveInfoAdapter;

public class ShortcutSelector extends SubFragmentWithSearch {

	String key = null;
	String keyContents = null;
	ArrayList<ResolveInfo> shortcuts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		key = args.getString("key");
		keyContents = AppHelper.getStringOfAppPrefs(key, null);

		Intent shortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
		PackageManager pm = getActivity().getPackageManager();
		shortcuts = new ArrayList<ResolveInfo>(pm.queryIntentActivities(shortcutIntent, 0));

		listView.setAdapter(new ResolveInfoAdapter(getContext(), shortcuts));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent createShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
				ResolveInfo app = (ResolveInfo)parent.getAdapter().getItem(position);
				ComponentName cn = new ComponentName(app.activityInfo.packageName, app.activityInfo.name);
				createShortcutIntent.setComponent(cn);
				keyContents = app.activityInfo.packageName + "|" + app.activityInfo.name;
				startActivityForResult(createShortcutIntent, 7350);
			}
		});

		if (getView() != null)
		getView().findViewById(R.id.am_progressBar).setVisibility(View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != 7350) return;
		if (resultCode == Activity.RESULT_OK) {
			Bitmap icon = null;
			Intent.ShortcutIconResource iconResId = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);

			if (iconResId != null) try {
				Context mContext = getActivity().createPackageContext(iconResId.packageName, Context.CONTEXT_IGNORE_SECURITY);
				icon = BitmapFactory.decodeResource(mContext.getResources(), mContext.getResources().getIdentifier(iconResId.resourceName, "drawable", iconResId.packageName));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			if (icon == null) icon = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

			Intent intent = new Intent(getContext(), this.getClass());

			if (icon != null && key != null) try {
				String dir = getContext().getFilesDir() + "/shortcuts";
				String fileName = dir + "/tmp.png";

				File shortcutsDir = new File(dir);
				shortcutsDir.mkdirs();
				File shortcutFileName = new File(fileName);
				try (FileOutputStream shortcutOutStream = new FileOutputStream(shortcutFileName, false)) {
					if (icon.compress(Bitmap.CompressFormat.PNG, 100, shortcutOutStream))
					intent.putExtra("shortcut_icon", fileName);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}

			intent.putExtra("shortcut_contents", keyContents);
			intent.putExtra("shortcut_name", data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME));
			intent.putExtra("shortcut_intent", (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT));
			getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}