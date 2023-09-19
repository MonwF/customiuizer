package name.monwf.customiuizer.utils;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import name.monwf.customiuizer.R;

public class PreferenceAdapter extends BaseAdapter {
	private final LayoutInflater mInflater;
	private final String key;
	private final boolean activities;
	private final ArrayList<String> items = new ArrayList<String>();

	public PreferenceAdapter(Context context, String prefKey, boolean actOnly) {
		mInflater = LayoutInflater.from(context);
		key = prefKey;
		activities = actOnly;
		updateItems();
	}

	public void updateItems() {
		items.clear();
		String itemStr = AppHelper.getStringOfAppPrefs(key, "");
		if (itemStr == null || itemStr.isEmpty()) return;
		String[] itemArr = itemStr.trim().split("\\|");
		items.addAll(Arrays.asList(itemArr));
	}

	public int getCount() {
		return items.size();
	}

	public String getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("ClickableViewAccessibility")
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView != null)
			row = convertView;
		else
			row = mInflater.inflate(R.layout.pref_item, parent, false);

		Helpers.setMiuiPrefItem(row);
		ImageView dragHandle = row.findViewById(R.id.drag_handle);
		ImageView itemIcon = row.findViewById(android.R.id.icon);
		TextView itemTitle = row.findViewById(android.R.id.title);
		TextView itemSummary = row.findViewById(android.R.id.summary);

		dragHandle.setVisibility(activities ? View.GONE : View.VISIBLE);
		dragHandle.setOnTouchListener(((SortableListView)parent).getListenerForStartingSort());
		itemIcon.setVisibility(View.VISIBLE);
		String uuid = getItem(position);
		Pair<String, String> name = AppHelper.getActionNameLocal(row.getContext(), key + "_" + uuid);
		if (name == null) {
			itemTitle.setText(R.string.notselected);
			itemSummary.setVisibility(View.GONE);
		} else {
			if (activities) {
				String actStr = AppHelper.getStringOfAppPrefs(key + "_" + uuid + "_activity", "");
				itemTitle.setText(actStr == null ? "" : Helpers.getAppName(row.getContext(), actStr, true));
			} else {
				itemTitle.setText(name.first);
			}
			if (name.second == null || name.second.isEmpty()) {
				itemSummary.setVisibility(View.GONE);
			} else {
				itemSummary.setVisibility(View.VISIBLE);
				itemSummary.setText(name.second);
			}
		}

		try {
			Drawable drawable = Helpers.getActionImageLocal(row.getContext(), key + "_" + uuid);
			itemIcon.setImageDrawable(drawable != null ? drawable : row.getContext().getPackageManager().getApplicationIcon(Helpers.modulePkg));
		} catch (Throwable t) {
			t.printStackTrace();
		}

		row.setPadding(row.getPaddingLeft(), row.getPaddingTop(), 0, row.getPaddingBottom());
		return row;
	}

}