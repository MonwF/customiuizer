package name.mikanoshi.customiuizer.subs;

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

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragmentWithSearch;
import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.utils.AppData;
import name.mikanoshi.customiuizer.utils.AppDataAdapter;
import name.mikanoshi.customiuizer.utils.Helpers;

public class ActivitySelector extends SubFragmentWithSearch {

	String pkg = null;
	String key = null;
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

		final Runnable process = new Runnable() {
			@Override
			public void run() {
				if (activities.size() == 0) {
					Toast.makeText(getActivity(), R.string.no_activities_found, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				listView.setAdapter(new AppDataAdapter(getContext(), activities, Helpers.AppAdapterType.Activities, null));
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						AppData appdData = ((AppDataAdapter)parent.getAdapter()).getItem(position);
						getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent(getContext(), this.getClass()).putExtra("activity", appdData.pkgName + "|" + appdData.actName));
						finish();
					}
				});
				listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
						AppData appdData = ((AppDataAdapter)parent.getAdapter()).getItem(position);
						Intent intent = new Intent(getContext(), this.getClass());
						intent.setComponent(new ComponentName(appdData.pkgName, appdData.actName));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						Intent bIntent = new Intent(GlobalActions.ACTION_PREFIX + "LaunchIntent");
						bIntent.putExtra("intent", intent);
						getContext().sendBroadcast(bIntent);
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