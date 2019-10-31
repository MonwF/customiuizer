package name.mikanoshi.customiuizer.utils;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;

public class ResolveInfoAdapter extends BaseAdapter implements Filterable {

	private Context ctx;
	private PackageManager pm;
	private LayoutInflater mInflater;
	private ThreadPoolExecutor pool;
	private ItemFilter mFilter = new ItemFilter();
	private ArrayList<ResolveInfo> originalAppList;
	private ArrayList<ResolveInfo> filteredAppList;

	public ResolveInfoAdapter(Context context, ArrayList<ResolveInfo> arr) {
		ctx = context;
		pm = ctx.getPackageManager();
		mInflater = LayoutInflater.from(context);
		originalAppList = arr;
		filteredAppList = arr;
		int cpuCount = Runtime.getRuntime().availableProcessors();
		pool = new ThreadPoolExecutor(cpuCount + 1, cpuCount * 2 + 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public int getCount() {
		return filteredAppList.size();
	}

	public ResolveInfo getItem(int position) {
		return filteredAppList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView != null)
			row = convertView;
		else
			row = mInflater.inflate(Helpers.is11() ? R.layout.applist_item11 : R.layout.applist_item, parent, false);

		ImageView itemIsDis = row.findViewById(R.id.icon_disable);
		TextView itemTitle = row.findViewById(android.R.id.title);
		ImageView itemIcon = row.findViewById(android.R.id.icon);

		ResolveInfo ri = getItem(position);
		itemIcon.setTag(position);

		AppData ad = new AppData();
		ad.pkgName = ri.activityInfo.applicationInfo.packageName;
		ad.actName = ri.activityInfo.name;
		ad.enabled = ri.activityInfo.enabled;
		ad.label = ri.loadLabel(pm).toString();

		itemTitle.setText(ad.label);
		itemIsDis.setVisibility(ad.enabled ? View.INVISIBLE : View.VISIBLE);
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

		//row.setEnabled(true);
		return row;
	}

	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			String filterString = constraint.toString().toLowerCase();
			FilterResults results = new FilterResults();

			int count = originalAppList.size();
			final ArrayList<ResolveInfo> nlist = new ArrayList<ResolveInfo>();
			ResolveInfo filterableData;

			for (int i = 0; i < count; i++) {
				filterableData = originalAppList.get(i);
				if (filterableData.loadLabel(pm).toString().toLowerCase().contains(filterString)) nlist.add(filterableData);
			}

			results.values = nlist;
			results.count = nlist.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredAppList = (ArrayList<ResolveInfo>)results.values;
			notifyDataSetChanged();
		}
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
}