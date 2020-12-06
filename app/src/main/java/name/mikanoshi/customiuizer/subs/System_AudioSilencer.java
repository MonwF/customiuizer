package name.mikanoshi.customiuizer.subs;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.mods.GlobalActions;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.SoundData;

public class System_AudioSilencer extends SubFragment {

	String key;
	Handler handler;
	ListView listView1;
	ListView listView2;
	SoundAdapter silencedAdapter;
	SoundAdapter playedAdapter;
	ArrayList<SoundData> silencedList = new ArrayList<SoundData>();
	ArrayList<SoundData> playedList = new ArrayList<SoundData>();
	BroadcastReceiver soundsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			playedList = intent.getParcelableArrayListExtra("sounds");
			if (playedList == null) playedList = new ArrayList<SoundData>();
			playedAdapter.notifyDataSetChanged();
			silencedAdapter.notifyDataSetChanged();
			updateProgressBar(true);
		}
	};
	SimpleDateFormat formatter = new SimpleDateFormat("H:mm:ss", Locale.getDefault());

	private void loadSilenced() {
		silencedList.clear();
		Set<String> silencedSet = Helpers.prefs.getStringSet(key, new LinkedHashSet<String>());
		if (silencedSet != null)
		for (String data: silencedSet)
		silencedList.add(SoundData.fromPref(data));
	}

	private void saveSilenced() {
		Set<String> silencedSet = new LinkedHashSet<String>();
		for (SoundData data: silencedList)
		silencedSet.add(data.toPref());
		Helpers.prefs.edit().putStringSet(key, silencedSet.size() == 0 ? null : silencedSet).apply();
		silencedAdapter.notifyDataSetChanged();
		playedAdapter.notifyDataSetChanged();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		key = args.getString("key");

		loadSilenced();

		silencedAdapter = new SoundAdapter(getContext(), true);
		playedAdapter = new SoundAdapter(getContext(), false);
		handler = new Handler();
		formatter.setTimeZone(TimeZone.getDefault());

		if (getView() != null) {
			listView1 = getView().findViewById(android.R.id.text1);
			listView2 = getView().findViewById(android.R.id.text2);

			TextView cat1 = getView().findViewById(R.id.sounds_silenced);
			TextView cat2 = getView().findViewById(R.id.sounds_played);
			int resId = getResources().getIdentifier("preference_category_background", "drawable", "miui");
			cat1.setBackgroundResource(resId);
			cat2.setBackgroundResource(resId);

			@SuppressLint("CutPasteId") ViewStub locationStub = getView().findViewById(R.id.refresh_list);
			locationStub.setLayoutResource(R.layout.pref_item);
			locationStub.inflate();

			@SuppressLint("CutPasteId") View refresh = getView().findViewById(R.id.refresh_list);
			((TextView)refresh.findViewById(android.R.id.title)).setText(R.string.system_audiosilencer_last_title);
			((TextView)refresh.findViewById(android.R.id.summary)).setText(R.string.system_audiosilencer_last_summ);
			Helpers.setMiuiPrefItem(refresh);
			refresh.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					playedList.clear();
					silencedAdapter.notifyDataSetChanged();
					playedAdapter.notifyDataSetChanged();
					updateProgressBar(false);
					fetchPlayedSounds();
				}
			});
		}
		listView1.setAdapter(silencedAdapter);
		listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SoundData data = silencedAdapter.getItem(position);
				silencedList.remove(data);
				saveSilenced();
			}
		});
		listView2.setAdapter(playedAdapter);
		listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SoundData data = playedAdapter.getItem(position);
				silencedList.add(data);
				saveSilenced();
			}
		});
		updateProgressBar(false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}

	Runnable getPlayedSounds = this::fetchPlayedSounds;

	void fetchPlayedSounds() {
		Intent intent = new Intent(GlobalActions.ACTION_PREFIX + "FetchPlayedSounds");
		intent.setPackage("android");
		getActivity().sendBroadcast(intent);
	}

	void updateProgressBar(boolean forceHide) {
		if (getView() != null) getView().findViewById(R.id.progress_bar).setVisibility(forceHide || playedList.size() > 0 ? View.GONE : View.VISIBLE);
	}

	void registerReceivers() {
		unregisterReceivers();
		getActivity().registerReceiver(soundsReceiver, new IntentFilter(GlobalActions.EVENT_PREFIX + "FetchPlayedSoundsData"));
		handler.postDelayed(getPlayedSounds, 1000);
	}

	void unregisterReceivers() {
		try {
			handler.removeCallbacks(getPlayedSounds);
			getActivity().unregisterReceiver(soundsReceiver);
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

	public class SoundAdapter extends BaseAdapter {
		private final boolean isSelected;
		private final LayoutInflater mInflater;

		SoundAdapter(Context context, boolean selected) {
			isSelected = selected;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			if (isSelected)
				return silencedList.size();
			else
				return playedList.size();
		}

		public SoundData getItem(int position) {
			return isSelected ? silencedList.get(position) : playedList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {
			return isSelected || !silencedList.contains(playedList.get(position));
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView != null) {
				row = convertView;
			} else {
				row = mInflater.inflate(R.layout.pref_item_detailed, parent, false);
				Helpers.setMiuiPrefItem(row);
			}

			TextView itemPackage = row.findViewById(android.R.id.title);
			TextView itemUid = row.findViewById(android.R.id.summary);
			TextView itemType = row.findViewById(android.R.id.text1);
			TextView itemTime = row.findViewById(android.R.id.text2);
			SoundData data = getItem(position);
			itemPackage.setText(data.caller);
			itemUid.setText(data.uid);
			itemType.setText("file".equals(data.type) ? getResources().getString(R.string.type_file) : "resource".equals(data.type) ? getResources().getString(R.string.type_res) : "uri".equals(data.type) ? "URI" : "");
			itemTime.setText(isSelected ? "" : formatter.format(data.time));

			if (isEnabled(position)) {
				row.setEnabled(true);
				itemPackage.setTextColor(getResources().getColor(R.color.preference_primary_text_color, getActivity().getTheme()));
				itemPackage.setAlpha(1.0f);
				itemUid.setAlpha(1.0f);
				itemType.setAlpha(1.0f);
				itemTime.setAlpha(1.0f);
			} else {
				row.setEnabled(false);
				itemPackage.setTextColor(getResources().getColor(R.color.preference_secondary_text_color, getActivity().getTheme()));
				itemPackage.setAlpha(0.5f);
				itemUid.setAlpha(0.5f);
				itemType.setAlpha(0.5f);
				itemTime.setAlpha(0.5f);
			}
			return row;
		}
	}

}
