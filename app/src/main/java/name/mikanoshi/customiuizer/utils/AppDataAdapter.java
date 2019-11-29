package name.mikanoshi.customiuizer.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import name.mikanoshi.customiuizer.R;

public class AppDataAdapter extends BaseAdapter implements Filterable {
	private Context ctx;
	private LayoutInflater mInflater;
	private ThreadPoolExecutor pool;
	private ItemFilter mFilter = new ItemFilter();
	private ArrayList<AppData> originalAppList = new ArrayList<AppData>();
	private ArrayList<AppData> filteredAppList = new ArrayList<AppData>();
	private String key = null;
	private String selectedApp;
	private int selectedUser = 0;
	private Set<String> selectedApps = new LinkedHashSet<String>();
	private Helpers.AppAdapterType aType = Helpers.AppAdapterType.Default;
	private boolean multiUserSupport = false;

	public AppDataAdapter(Context context, ArrayList<AppData> arr) {
		ctx = context;
		mInflater = LayoutInflater.from(context);
		originalAppList.addAll(arr);
		filteredAppList.addAll(arr);
		int cpuCount = Runtime.getRuntime().availableProcessors();
		pool = new ThreadPoolExecutor(cpuCount + 1, cpuCount * 2 + 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public AppDataAdapter(Context context, ArrayList<AppData> arr, Helpers.AppAdapterType adapterType, String prefKey) {
		this(context, arr);
		key = prefKey;
		aType = adapterType;
		if (aType == Helpers.AppAdapterType.Mutli) {
			selectedApps = Helpers.prefs.getStringSet(key, new LinkedHashSet<String>());
			ArrayList<String> multiUserMods = new ArrayList<String>();
			multiUserMods.add("pref_key_system_cleanshare_apps");
			multiUserMods.add("pref_key_system_cleanopenwith_apps");
			multiUserSupport = multiUserMods.contains(key);
			if (multiUserSupport) {
				HashSet<String> selectedAppsAdd = new HashSet<String>();
				Iterator iter = selectedApps.iterator();
				while (iter.hasNext()) {
					String item = ((String)iter.next());
					if (!item.contains("|")) {
						selectedAppsAdd.add(item + "|0");
						iter.remove();
					}
				}
				if (selectedAppsAdd.size() > 0) selectedApps.addAll(selectedAppsAdd);
			} else {
				Iterator iter = originalAppList.iterator();
				while (iter.hasNext()) if (((AppData)iter.next()).user != 0) iter.remove();
				filteredAppList.clear();
				filteredAppList.addAll(originalAppList);
			}
		} else if (aType == Helpers.AppAdapterType.Standalone) {
			selectedApp = Helpers.prefs.getString(key, "");
			selectedUser = Helpers.prefs.getInt(key + "_user", 0);
			AppData noApp = new AppData();
			noApp.pkgName = "";
			noApp.actName = "";
			noApp.label = ctx.getResources().getString(R.string.array_default);
			noApp.enabled = true;
			originalAppList.add(0, noApp);
			filteredAppList.add(0, noApp);
		}
		sortList();
	}

	public void updateSelectedApps() {
		if (aType == Helpers.AppAdapterType.Mutli)
			selectedApps = Helpers.prefs.getStringSet(key, new LinkedHashSet<String>());
		else if (aType == Helpers.AppAdapterType.Standalone) {
			selectedApp = Helpers.prefs.getString(key, "");
			selectedUser = Helpers.prefs.getInt(key + "_user", 0);
		}
		notifyDataSetChanged();
	}

	private boolean shouldSelect(String pkgName, int user) {
		return (!multiUserSupport && (selectedApps.contains(pkgName) || selectedApps.contains(pkgName + "|0"))) || (multiUserSupport && selectedApps.contains(pkgName + "|" + user));
	}

	private void sortList() {
		filteredAppList.sort(new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				if (aType == Helpers.AppAdapterType.Mutli && selectedApps.size() > 0) {
					boolean app1checked = shouldSelect(app1.pkgName, app1.user);
					boolean app2checked = shouldSelect(app2.pkgName, app2.user);
					if (app1checked && app2checked)
						return 0;
					else if (app1checked)
						return -1;
					else if (app2checked)
						return 1;
					return 0;
				} else if (aType == Helpers.AppAdapterType.Standalone) {
					if (app1.pkgName.equals("") && app1.actName.equals("")) return -1;
					if (app2.pkgName.equals("") && app2.actName.equals("")) return 1;
					boolean app1checked = selectedApp.equals(app1.pkgName + "|" + app1.actName) && selectedUser == app1.user;
					boolean app2checked = selectedApp.equals(app2.pkgName + "|" + app2.actName) && selectedUser == app2.user;
					if (app1checked && app2checked)
						return 0;
					else if (app1checked)
						return -1;
					else if (app2checked)
						return 1;
					return 0;
				} else if (aType == Helpers.AppAdapterType.Activities) {
					return app1.actName.toLowerCase().compareTo(app2.actName.toLowerCase());
				} else return 0;
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

	public View getView(final int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView != null)
			row = convertView;
		else
			row = mInflater.inflate(Helpers.is11() ? R.layout.applist_item11 : R.layout.applist_item, parent, false);

		ImageView itemIsDis = row.findViewById(R.id.icon_disable);
		ImageView itemIsDual = row.findViewById(R.id.icon_dual);
		CheckBox itemChecked = row.findViewById(android.R.id.checkbox);
		if (itemChecked.getTag() == null || !(boolean)itemChecked.getTag()) {
			itemChecked.setTag(true);
			Helpers.setMiuiCheckbox(itemChecked);
		}
		TextView itemTitle = row.findViewById(android.R.id.title);
		TextView itemSummary = row.findViewById(android.R.id.summary);
		ImageView itemIcon = row.findViewById(android.R.id.icon);

		AppData ad = getItem(position);
		itemTitle.setText(ad.label);
		itemIsDis.setVisibility(ad.enabled ? View.GONE : View.VISIBLE);

		if (aType == Helpers.AppAdapterType.Activities) {
			itemIcon.setVisibility(View.GONE);
			View container = row.findViewById(R.id.container);
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)container.getLayoutParams();
			lp.leftMargin = 0;
			container.setLayoutParams(lp);
		} else {
			itemIcon.setTag(position);
			Bitmap icon = Helpers.memoryCache.get(ad.pkgName + "|" + ad.actName);
			//int iconSize = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
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
		}

		if (aType == Helpers.AppAdapterType.Mutli) {
			itemSummary.setVisibility(View.GONE);
			itemChecked.setVisibility(View.VISIBLE);
			itemChecked.setChecked(selectedApps.size() > 0 && shouldSelect(ad.pkgName, ad.user));
			itemIsDual.setVisibility(ad.user != 0 ? View.VISIBLE : View.GONE);
		} else if (aType == Helpers.AppAdapterType.CustomTitles) {
			itemSummary.setText(Helpers.prefs.getString(key + ":" + ad.pkgName + "|" + ad.actName + "|" + ad.user, ""));
			itemSummary.setVisibility(TextUtils.isEmpty(itemSummary.getText()) ? View.GONE : View.VISIBLE);
			itemIsDual.setVisibility(ad.user != 0 ? View.VISIBLE : View.GONE);
		} else if (aType == Helpers.AppAdapterType.Standalone) {
			itemChecked.setVisibility(View.VISIBLE);
			itemChecked.setChecked((selectedApp.equals("") && ad.pkgName.equals("") && ad.actName.equals("")) || ((ad.pkgName + "|" + ad.actName).equals(selectedApp) && ad.user == selectedUser));
			itemIsDual.setVisibility(ad.user != 0 ? View.VISIBLE : View.GONE);
		} else if (aType == Helpers.AppAdapterType.Activities) {
			itemSummary.setText(ad.actName.replace(".", ".\u200B"));
			itemSummary.setVisibility(TextUtils.isEmpty(itemSummary.getText()) ? View.GONE : View.VISIBLE);
			itemSummary.setSingleLine(false);
			itemSummary.setMaxLines(Integer.MAX_VALUE);
			itemSummary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
			itemIsDual.setVisibility(ad.user != 0 ? View.VISIBLE : View.GONE);
		} else {
			itemSummary.setVisibility(View.GONE);
			itemIsDual.setVisibility(ad.user != 0 ? View.VISIBLE : View.GONE);
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
			final ArrayList<AppData> nlist = new ArrayList<AppData>();
			AppData filterableData;

			for (int i = 0; i < count; i++) {
				filterableData = originalAppList.get(i);
				if (aType == Helpers.AppAdapterType.Activities && filterableData.actName.toLowerCase().contains(filterString)) nlist.add(filterableData);
				else if ((aType == Helpers.AppAdapterType.Standalone && filterableData.pkgName.equals("") && filterableData.actName.equals("")) || filterableData.label.toLowerCase().contains(filterString)) nlist.add(filterableData);
			}

			results.values = nlist;
			results.count = nlist.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredAppList = (ArrayList<AppData>)results.values;
			sortList();
			notifyDataSetChanged();
		}
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
}