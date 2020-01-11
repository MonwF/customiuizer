package name.mikanoshi.customiuizer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import miui.app.ActionBar;
import miui.app.AlertDialog;
import miui.util.AttributeResolver;
import miui.widget.ProgressBar;

import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.utils.SnoozeData;
import name.mikanoshi.customiuizer.utils.SnoozedAdapter;

public class SnoozedFragment extends PreferenceFragmentBase {

	boolean loading = false;
	ListView listView = null;
	ProgressBar loader = null;
	TextView empty = null;
	static public ArrayList<SnoozeData> snoozedList = new ArrayList<SnoozeData>();
	Handler handler = null;
	Runnable fetchFailed = new Runnable() {
		@Override
		public void run() {
			finishLoading();
			Toast.makeText(getContext(), R.string.snooze_fetch_fail, Toast.LENGTH_LONG).show();
		}
	};

	BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		@Override
		@SuppressWarnings("ConstantConditions")
		public void onReceive(Context context, Intent intent) {
			handler.removeCallbacks(fetchFailed);
			HashMap<String, ArrayList<SnoozeData>> tmpList = new HashMap<String, ArrayList<SnoozeData>>();
			Bundle extras = intent.getExtras();
			if (extras != null && extras.size() > 0)
			for (String key: extras.keySet()) {
				Bundle bundle = extras.getBundle(key);
				if (bundle != null) {
					SnoozeData data = new SnoozeData();
					data.key = key;
					data.user = bundle.getInt("user");
					data.pkg = bundle.getString("package");
					data.canceled = bundle.getBoolean("canceled");
					data.created = bundle.getLong("created");
					data.updated = bundle.getLong("updated");
					data.reposted = bundle.getLong("reposted");
					data.channel = bundle.getString("channel", "");
					data.color = bundle.getInt("color");
					data.title = bundle.getString("title", "");
					data.text = bundle.getString("text", "");
					data.messages = bundle.getInt("messages", 0);
					data.icon = bundle.getParcelable("icon");
					if (!tmpList.containsKey(data.pkg)) tmpList.put(data.pkg, new ArrayList<SnoozeData>());
					tmpList.get(data.pkg).add(data);
				}
			}
			for (String pkg: tmpList.keySet()) {
				SnoozeData header = new SnoozeData();
				header.pkg = pkg;
				header.header = true;
				snoozedList.add(header);
				snoozedList.addAll(tmpList.get(pkg));
			}
			finishLoading();
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler(getContext().getMainLooper());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setTitle(R.string.title_snoozed);
			actionBar.showSplitActionBar(true, true);
		}
		setImmersionMenuEnabled(false);

		if (getView() == null) return;

		listView = getView().findViewById(android.R.id.list);
		listView.setAdapter(new SnoozedAdapter(getContext()));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				final SnoozeData data = snoozedList.get(position);
				AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
				dialog.setItems(R.array.snoozedactions, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0: unsnooze(data); break;
							case 1:
								setCanceled(data, true);
								snoozedList.get(position).canceled = true;
								updateListView();
								break;
							case 2:
								setCanceled(data, false);
								snoozedList.get(position).canceled = false;
								updateListView();
								break;
						}
					}
				});
				AlertDialog alert = dialog.create();
				alert.show();

				final int listResID = getResources().getIdentifier("select_dialog_listview", "id", "miui");
				final ListView listView = alert.findViewById(listResID);
				if (listView == null) return;
				listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
					@Override
					public void onChildViewAdded(View parent, View child) {
						if (child == null) return;
						try {
							int index = ((ViewGroup)parent).indexOfChild(child);
							if ((index == 1 && data.canceled) || (index == 2 && !data.canceled)) child.setEnabled(false);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}

					@Override
					public void onChildViewRemoved(View parent, View child) {}
				});
			}
		});

		loader = getView().findViewById(R.id.am_progressBar);
		empty = getView().findViewById(android.R.id.empty);
	}

	public View onInflateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
		return inflater.inflate(R.layout.fragment_snoozed, group, false);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem update = menu.add(R.string.menu_update);
		update.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		update.setIcon(AttributeResolver.resolveDrawable(getContext(), getResources().getIdentifier("actionBarRefreshIcon", "attr", "miui")));
		update.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				fetchSnoozed();
				return true;
			}
		});
		return true;
	}

	public void onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setEnabled(!loading);
	}

	@Override
	public void onPause() {
		getContext().unregisterReceiver(updateReceiver);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		getContext().registerReceiver(updateReceiver, new IntentFilter(GlobalActions.EVENT_PREFIX + "UpdateSnoozedNotifications"));
		fetchSnoozed();
	}

	private void fetchSnoozed() {
		View root = getView();
		if (root == null) return;
		startLoading();
		root.postDelayed(new Runnable() {
			@Override
			public void run() {
				getContext().sendBroadcast(new Intent(GlobalActions.ACTION_PREFIX + "GetSnoozedNotifications"));
			}
		}, 300);
		handler.removeCallbacks(fetchFailed);
		handler.postDelayed(fetchFailed, 5000);
	}

	private void unsnooze(SnoozeData data) {
		startLoading();
		Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "UnsnoozeNotification");
		intent.putExtra("key", data.key);
		intent.putExtra("user", data.user);
		getContext().sendBroadcast(intent);
	}

	private void setCanceled(SnoozeData data, boolean isCancaled) {
		Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "CancelNotification");
		intent.putExtra("key", data.key);
		intent.putExtra("user", data.user);
		intent.putExtra("package", data.pkg);
		intent.putExtra("canceled", isCancaled);
		getContext().sendBroadcast(intent);
	}

	private void updateListView() {
		if (listView != null) ((SnoozedAdapter)listView.getAdapter()).notifyDataSetChanged();
	}

	private void startLoading() {
		if (loading) return;
		loading = true;
		invalidateOptionsMenu();
		snoozedList.clear();
		updateListView();
		if (loader != null) loader.setVisibility(View.VISIBLE);
		if (empty != null) empty.setVisibility(View.GONE);
	}

	private void finishLoading() {
		loading = false;
		invalidateOptionsMenu();
		updateListView();
		if (loader != null) loader.setVisibility(View.GONE);
		if (empty != null) empty.setVisibility(snoozedList.size() == 0 ? View.VISIBLE : View.GONE);
	}

}