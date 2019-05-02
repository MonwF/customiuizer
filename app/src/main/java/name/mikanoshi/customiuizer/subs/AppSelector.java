package name.mikanoshi.customiuizer.subs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.LinkedHashSet;
import java.util.Set;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragmentWithSearch;
import name.mikanoshi.customiuizer.utils.AppData;
import name.mikanoshi.customiuizer.utils.AppDataAdapter;
import name.mikanoshi.customiuizer.utils.Helpers;

public class AppSelector extends SubFragmentWithSearch {

	boolean multi = false;
	String key = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		multi = getArguments().getBoolean("multi");
		key = getArguments().getString("key");

		final Runnable process = new Runnable() {
			@Override
			public void run() {
				if (multi && key != null) {
					if (Helpers.installedAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(getContext(), Helpers.installedAppsList, key));
				} else {
					if (Helpers.launchableAppsList == null) return;
					listView.setAdapter(new AppDataAdapter(getContext(), Helpers.launchableAppsList));
				}
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (multi) {
							AppData app = (AppData)parent.getAdapter().getItem(position);
							Set<String> selectedApps = new LinkedHashSet<String>(Helpers.prefs.getStringSet(key, new LinkedHashSet<String>()));
							if (selectedApps.contains(app.pkgName))
								selectedApps.remove(app.pkgName);
							else
								selectedApps.add(app.pkgName);
							Helpers.prefs.edit().putStringSet(key, selectedApps).apply();
							AppDataAdapter adapter = (AppDataAdapter)parent.getAdapter();
							adapter.updateSelectedApps();
							adapter.notifyDataSetChanged();
						} else {
							Intent intent = new Intent(getContext(), this.getClass());
							AppData app = (AppData)parent.getAdapter().getItem(position);
							intent.putExtra("activity", app.pkgName + "|" + app.actName);
							getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
							finish();
						}
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
					if (multi && key != null) {
						if (Helpers.installedAppsList == null) Helpers.getInstalledApps(getActivity());
					} else {
						if (Helpers.launchableAppsList == null) Helpers.getLaunchableApps(getActivity());
					}
					getActivity().runOnUiThread(process);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}