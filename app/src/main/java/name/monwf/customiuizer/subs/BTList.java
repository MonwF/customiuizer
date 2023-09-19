package name.monwf.customiuizer.subs;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.mods.GlobalActions;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class BTList extends SubFragment {

	int fetchInterval = 15 * 1000;
	String key;
	ListView listView1;
	ListView listView2;
	Handler handler;
	BTAdapter btAdapter1;
	BTAdapter btAdapter2;
	List<Pair<String, String>> btList = new ArrayList<Pair<String, String>>();
	Set<String> addresses = new LinkedHashSet<String>();
	BroadcastReceiver devicesReceiver = new BroadcastReceiver() {
		@SuppressLint("MissingPermission")
		@Override
		public void onReceive(Context context, Intent intent) {
			ArrayList<BluetoothDevice> deviceList = intent.getParcelableArrayListExtra("device_list");
			btList.clear();
			if (deviceList != null) {
				for (BluetoothDevice device: deviceList) {
					btList.add(new Pair<String, String>(device.getAddress(), device.getName()));
				}
			}
			btAdapter1.notifyDataSetChanged();
			btAdapter2.notifyDataSetChanged();
			updateProgressBar();
		}
	};

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
		addresses = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key, new LinkedHashSet<String>()));

		btAdapter1 = new BTAdapter(getContext(), true);
		btAdapter2 = new BTAdapter(getContext(), false);
		handler = new Handler();

		if (getView() != null) {
			listView1 = getView().findViewById(android.R.id.text1);
			listView2 = getView().findViewById(android.R.id.text2);

			@SuppressLint("CutPasteId") ViewStub locationStub = getView().findViewById(R.id.fetch_devices);
			locationStub.setLayoutResource(R.layout.pref_item);
			locationStub.inflate();

			@SuppressLint("CutPasteId") View location = getView().findViewById(R.id.fetch_devices);
			((TextView)location.findViewById(android.R.id.title)).setText(R.string.bt_fetch_devices_title);
			((TextView)location.findViewById(android.R.id.summary)).setText(R.string.bt_fetch_devices_summ);
			location.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					btList.clear();
					btAdapter1.notifyDataSetChanged();
					btAdapter2.notifyDataSetChanged();
					updateProgressBar();
					fetchCachedDevices();
				}
			});
		}
		listView1.setAdapter(btAdapter1);
		listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Pair<String, String> sr = btAdapter1.getItem(position);
				addresses = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key, new LinkedHashSet<String>()));
				AppHelper.removeStringPair(addresses, sr.first);
				AppHelper.appPrefs.edit().putStringSet(key, addresses.size() == 0 ? null : addresses).apply();
				btAdapter1.notifyDataSetChanged();
				btAdapter2.notifyDataSetChanged();
			}
		});
		listView2.setAdapter(btAdapter2);
		listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Pair<String, String> sr = btAdapter2.getItem(position);
				addresses = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key, new LinkedHashSet<String>()));
				AppHelper.addStringPair(addresses, sr.first, sr.second);
				AppHelper.appPrefs.edit().putStringSet(key, addresses).apply();
				btAdapter1.notifyDataSetChanged();
				btAdapter2.notifyDataSetChanged();
			}
		});

		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			Toast.makeText(getActivity(), R.string.request_bt, Toast.LENGTH_SHORT).show();
		}
		updateProgressBar();
	}

	Runnable getCachedDevices = new Runnable() {
		@Override
		public void run() {
			fetchCachedDevices();
			handler.postDelayed(getCachedDevices, fetchInterval);
		}
	};

	void fetchCachedDevices() {
		Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "FetchCachedDevices");
		intent.setPackage("com.android.systemui");
		getActivity().sendBroadcast(intent);
	}

	void updateProgressBar() {
		if (getView() != null) getView().findViewById(R.id.progress_bar).setVisibility(!BluetoothAdapter.getDefaultAdapter().isEnabled() || btList.size() > 0 ? View.GONE : View.VISIBLE);
	}

	void registerReceivers() {
		unregisterReceivers();
		getActivity().registerReceiver(devicesReceiver, new IntentFilter(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE"));
		handler.postDelayed(getCachedDevices, 1000);
	}

	void unregisterReceivers() {
		try {
			handler.removeCallbacks(getCachedDevices);
			getActivity().unregisterReceiver(devicesReceiver);
		} catch (Throwable t) {}
	}

	@Override
	public void onDestroy() {
		unregisterReceivers();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		unregisterReceivers();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceivers();
	}

	public class BTAdapter extends BaseAdapter {
		private final boolean isSelected;
		private final LayoutInflater mInflater;

		BTAdapter(Context context, boolean selected) {
			isSelected = selected;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			if (isSelected)
				return addresses.size();
			else
				return btList.size();
		}

		public Pair<String, String> getItem(int position) {
			if (isSelected) {
				if (addresses.size() == 0) return null;
				String[] network = addresses.toArray(new String[0])[position].split("\\|", 2);
				return new Pair<String, String>(network[0], network[1]);
			} else {
				return btList.get(position);
			}
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {
			return isSelected || !Helpers.containsStringPair(addresses, getItem(position).first);
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView != null) {
				row = convertView;
			} else {
				row = mInflater.inflate(R.layout.pref_item, parent, false);
			}

			TextView itemTitle = row.findViewById(android.R.id.title);
			TextView itemSumm = row.findViewById(android.R.id.summary);
			Pair<String, String> sr = getItem(position);
			itemTitle.setText(sr.second);
			itemSumm.setText(sr.first);

			if (isEnabled(position)) {
				row.setEnabled(true);

				boolean isBonded = false;
				@SuppressLint("MissingPermission")
				Set<BluetoothDevice> bonded = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
				for (BluetoothDevice device: bonded)
				if (device.getAddress().equals(sr.first)) isBonded = true;

				if (isBonded)
					itemTitle.setTextColor(getResources().getColor(R.color.highlight_normal_light, getActivity().getTheme()));
				else
					itemTitle.setTextColor(getResources().getColor(R.color.preference_primary_text_color, getActivity().getTheme()));
				itemTitle.setAlpha(1.0f);
				itemSumm.setAlpha(1.0f);
			} else {
				row.setEnabled(false);
				itemTitle.setTextColor(getResources().getColor(R.color.preference_secondary_text_color, getActivity().getTheme()));
				itemTitle.setAlpha(0.5f);
				itemSumm.setAlpha(0.5f);
			}
			return row;
		}
	}

}