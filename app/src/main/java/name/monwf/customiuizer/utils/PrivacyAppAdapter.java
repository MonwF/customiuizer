package name.monwf.customiuizer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import name.monwf.customiuizer.R;

public class PrivacyAppAdapter extends BaseAdapter implements Filterable {
	private final Context ctx;
	private final LayoutInflater mInflater;
	private final ThreadPoolExecutor pool;
	private final ItemFilter mFilter = new ItemFilter();
	private final ArrayList<AppData> originalAppList;
	private final CopyOnWriteArrayList<AppData> filteredAppList = new CopyOnWriteArrayList<AppData>();

	private HashMap<Integer, ArrayList<String>> mPrivacyAppsMap;

	@SuppressLint("WrongConstant")
	public PrivacyAppAdapter(Context context, ArrayList<AppData> arr, HashMap<Integer, ArrayList<String>> privacyAppsMap) {
		ctx = context;
		mInflater = LayoutInflater.from(context);
		originalAppList = arr;
		filteredAppList.addAll(arr);
		int cpuCount = Runtime.getRuntime().availableProcessors();
		pool = new ThreadPoolExecutor(cpuCount + 1, cpuCount * 2 + 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		mPrivacyAppsMap = privacyAppsMap;
		sortList();
	}

	private void sortList() {
		filteredAppList.sort(new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				try {
					boolean app1checked = isPrivacyApp(app1.pkgName, app1.user);
					boolean app2checked = isPrivacyApp(app2.pkgName, app2.user);
					if (app1checked && app2checked)
						return 0;
					else if (app1checked)
						return -1;
					else if (app2checked)
						return 1;
					return 0;
				} catch (Throwable t) {
					return 0;
				}
			}
		});
	}

	public int getCount() {
		return filteredAppList.size();
	}

	public AppData getItem(int position) {
		return filteredAppList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	private boolean isPrivacyApp(String pkgName, int user) {
		if (mPrivacyAppsMap != null && mPrivacyAppsMap.containsKey(user)) {
			ArrayList<String> privacyApps = mPrivacyAppsMap.get(user);
			return privacyApps.contains(pkgName);
		}
		return false;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView != null)
			row = convertView;
		else
			row = mInflater.inflate(R.layout.applist_item11, parent, false);

		ImageView itemIsDis = row.findViewById(R.id.icon_disable);
		ImageView itemIsDual = row.findViewById(R.id.icon_dual);
		CheckBox itemChecked = row.findViewById(android.R.id.checkbox);
		TextView itemTitle = row.findViewById(android.R.id.title);
		ImageView itemIcon = row.findViewById(android.R.id.icon);

		AppData ad = getItem(position);
		itemIcon.setTag(position);
		itemTitle.setText(ad.label);
		itemIsDis.setVisibility(ad.enabled ? View.GONE : View.VISIBLE);
		itemIsDual.setVisibility(ad.user != 0 ? View.VISIBLE : View.GONE);
		Bitmap icon = Helpers.memoryCache.get(ad.pkgName + "|" + ad.actName);

		if (icon == null) {
			Drawable[] dualIcon = new Drawable[1];
			dualIcon[0] = ctx.getResources().getDrawable(R.drawable.card_icon_default, ctx.getTheme());
			TransitionDrawable crossfader = new TransitionDrawable(dualIcon);
			crossfader.setCrossFadeEnabled(true);
			itemIcon.setImageDrawable(crossfader);
			(new BitmapCachedLoader(itemIcon, ad, ctx)).executeOnExecutor(pool);
		} else {
			itemIcon.setImageBitmap(icon);
		}

		try {
			itemChecked.setVisibility(View.VISIBLE);
			itemChecked.setChecked(isPrivacyApp(ad.pkgName, ad.user));
		} catch (Throwable t) {
			itemChecked.setVisibility(View.GONE);
		}

		return row;
	}

	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			String filterString = constraint.toString().toLowerCase();
			FilterResults results = new FilterResults();

			int count = originalAppList.size();
			final ArrayList<AppData> nlist = new ArrayList<AppData>();
			AppData filterableData;

			for (int i = 0; i < count; i++) {
				filterableData = originalAppList.get(i);
				if (filterableData.label.toLowerCase().contains(filterString)) nlist.add(filterableData);
			}

			results.values = nlist;
			results.count = nlist.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredAppList.clear();
			if (results.count > 0 && results.values != null)
			filteredAppList.addAll((ArrayList<AppData>)results.values);
			sortList();
			notifyDataSetChanged();
		}
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
}