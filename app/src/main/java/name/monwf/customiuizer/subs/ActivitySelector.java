package name.monwf.customiuizer.subs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragmentWithSearch;
import name.monwf.customiuizer.mods.GlobalActions;
import name.monwf.customiuizer.utils.AppData;
import name.monwf.customiuizer.utils.AppDataAdapter;
import name.monwf.customiuizer.utils.Helpers;

public class ActivitySelector extends SubFragmentWithSearch {

	String pkg = null;
	String key = null;
	int user = 0;
	ArrayList<AppData> activities = new ArrayList<AppData>();

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
		pkg = args.getString("package");
		user = args.getInt("user");

		final Runnable process = new Runnable() {
			@Override
			public void run() {
				if (activities.size() == 0) {
					Toast.makeText(getActivity(), R.string.no_activities_found, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				listView.setAdapter(new AppDataAdapter(getActivity().getApplicationContext(), activities, Helpers.AppAdapterType.Activities, null));
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						AppData appdData = ((AppDataAdapter)parent.getAdapter()).getItem(position);
						final Intent intent = new Intent(getActivity(), this.getClass());
						intent.putExtra("activity", appdData.pkgName + "|" + appdData.actName);
						intent.putExtra("user", user);
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
						finish();
					}
				});
				listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
						AppData appdData = ((AppDataAdapter)parent.getAdapter()).getItem(position);
						Intent intent = new Intent(getActivity(), this.getClass());
						intent.setComponent(new ComponentName(appdData.pkgName, appdData.actName));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						intent.putExtra("user", user);
						Intent bIntent = new Intent(GlobalActions.ACTION_PREFIX + "LaunchIntent");
						bIntent.putExtra("intent", intent);
						getActivity().sendBroadcast(bIntent);
						return true;
					}
				});
				if (getView() != null) getView().findViewById(R.id.am_progressBar).setVisibility(View.GONE);
			}
		};

		new Thread() {
			@Override
			public void run() {
				try { sleep(animDur); } catch (Throwable e) {}
				try {
					activities.clear();
					PackageManager pm = getActivity().getPackageManager();
					PackageInfo pi = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
					if (pi.activities != null)
					for (ActivityInfo info: pi.activities) {
						AppData appData = new AppData();
						appData.pkgName = pkg;
						appData.actName = info.name != null ? info.name : "";
						appData.label = (String)info.loadLabel(pm);
						appData.enabled = info.enabled;
						activities.add(appData);
					}
					getActivity().runOnUiThread(process);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}