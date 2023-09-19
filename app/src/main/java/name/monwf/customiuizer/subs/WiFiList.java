package name.monwf.customiuizer.subs;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class WiFiList extends SubFragment {

	int scanInterval = 15 * 1000;
	String key;
	ListView listView1;
	ListView listView2;
	Handler handler;
	WiFiAdapter wifiAdapter1;
	WiFiAdapter wifiAdapter2;
	WifiManager wifiManager;
	List<ScanResult> wifiList = new ArrayList<ScanResult>();
	Set<String> bssids = new LinkedHashSet<String>();
	BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null) return;
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				wifiList = wifiManager.getScanResults();
				wifiAdapter1.notifyDataSetChanged();
				wifiAdapter2.notifyDataSetChanged();
				updateProgressBar();
			} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (netInfo == null) return;
				if (netInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) isWiFiReady();
				if (netInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED || netInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
					handler.removeCallbacks(getScanResults);
					handler.postDelayed(getScanResults, 1000);
				}
			}
		}
	};

	Runnable getScanResults = new Runnable() {
		@Override
		public void run() {
			try { wifiManager.startScan(); } catch (Throwable ignore) {}
			handler.postDelayed(getScanResults, scanInterval);
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
		bssids = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key, new LinkedHashSet<String>()));

		wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
		wifiAdapter1 = new WiFiAdapter(getContext(), true);
		wifiAdapter2 = new WiFiAdapter(getContext(), false);
		handler = new Handler();

		if (getView() != null) {
			listView1 = getView().findViewById(android.R.id.text1);
			listView2 = getView().findViewById(android.R.id.text2);

			@SuppressLint("CutPasteId") ViewStub locationStub = getView().findViewById(R.id.location_settings);
			locationStub.setLayoutResource(R.layout.pref_item);
			locationStub.inflate();

			@SuppressLint("CutPasteId") View location = getView().findViewById(R.id.location_settings);
			((TextView)location.findViewById(android.R.id.title)).setText(R.string.wifi_location_title);
			((TextView)location.findViewById(android.R.id.summary)).setText(R.string.wifi_location_summ);
			location.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			});
		}
		listView1.setAdapter(wifiAdapter1);
		listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Pair<String, String> sr = wifiAdapter1.getItem(position);
				bssids = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key, new LinkedHashSet<String>()));
				AppHelper.removeStringPair(bssids, sr.first);
				AppHelper.appPrefs.edit().putStringSet(key, bssids.size() == 0 ? null : bssids).apply();
				wifiAdapter1.notifyDataSetChanged();
				wifiAdapter2.notifyDataSetChanged();
			}
		});
		listView2.setAdapter(wifiAdapter2);
		listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Pair<String, String> sr = wifiAdapter2.getItem(position);
				bssids = new LinkedHashSet<String>(AppHelper.getStringSetOfAppPrefs(key, new LinkedHashSet<String>()));
				AppHelper.addStringPair(bssids, sr.first, sr.second);
				AppHelper.appPrefs.edit().putStringSet(key, bssids).apply();
				wifiAdapter1.notifyDataSetChanged();
				wifiAdapter2.notifyDataSetChanged();
			}
		});

		isWiFiReady();
		updateProgressBar();
	}

	void updateProgressBar() {
		if (getView() != null) getView().findViewById(R.id.progress_bar).setVisibility(!wifiManager.isWifiEnabled() || !isLocationServicesEnabled() || wifiList.size() > 0 ? View.GONE : View.VISIBLE);
	}

	public boolean isLocationServicesEnabled() {
		LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
		try {
			return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Throwable t) {
			return false;
		}
	}

	public void isWiFiReady() {
		if (!wifiManager.isWifiEnabled()) {
			Toast.makeText(getActivity(), R.string.request_wifi, Toast.LENGTH_SHORT).show();
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
		if (!isLocationServicesEnabled())
		Toast.makeText(getActivity(), R.string.request_location, Toast.LENGTH_LONG).show();
	}

	void registerReceivers() {
		unregisterReceivers();
		isWiFiReady();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		getActivity().registerReceiver(wifiReceiver, intentFilter);
		handler.postDelayed(getScanResults, 1000);
	}

	void unregisterReceivers() {
		try {
			handler.removeCallbacks(getScanResults);
			getActivity().unregisterReceiver(wifiReceiver);
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

	public class WiFiAdapter extends BaseAdapter {
		private final boolean isSelected;
		private final LayoutInflater mInflater;

		WiFiAdapter(Context context, boolean selected) {
			isSelected = selected;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			if (isSelected)
				return bssids.size();
			else
				return wifiList.size();
		}

		public Pair<String, String> getItem(int position) {
			if (isSelected) {
				if (bssids.size() == 0) return null;
				String[] network = bssids.toArray(new String[0])[position].split("\\|", 2);
				return new Pair<String, String>(network[0], network[1]);
			} else {
				return new Pair<String, String>(wifiList.get(position).BSSID, wifiList.get(position).SSID);
			}
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {
			return isSelected || !Helpers.containsStringPair(bssids, getItem(position).first);
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
				if (sr.first.equals(wifiManager.getConnectionInfo().getBSSID()))
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