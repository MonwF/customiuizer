package name.monwf.customiuizer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

import name.monwf.customiuizer.R;

public class ModSearchAdapter extends BaseAdapter implements Filterable {
	private final LayoutInflater mInflater;
	private final ItemFilter mFilter = new ItemFilter();
	private final CopyOnWriteArrayList<ModData> modsList = new CopyOnWriteArrayList<ModData>();
	private String filterString = "";

	@SuppressLint("WrongConstant")
	public ModSearchAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	private void sortList() {
		modsList.sort(new Comparator<ModData>() {
			public int compare(ModData app1, ModData app2) {
				int breadcrumbs = app1.breadcrumbs.compareToIgnoreCase(app2.breadcrumbs);
				if (breadcrumbs == 0)
					return app1.title.compareToIgnoreCase(app2.title);
				else
					return breadcrumbs;
			}
		});
	}

	public int getCount() {
		return modsList.size();
	}

	public ModData getItem(int position) {
		return modsList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView != null) {
			row = convertView;
		} else {
			row = mInflater.inflate(R.layout.pref_item, parent, false);
		}

		TextView itemTitle = row.findViewById(android.R.id.title);
		TextView itemSummary = row.findViewById(android.R.id.summary);

		ModData ad = getItem(position);

		int start = ad.title.toLowerCase().indexOf(filterString);
		if (start >= 0) {
			Spannable spannable = new SpannableString(ad.title);
			spannable.setSpan(new ForegroundColorSpan(Helpers.markColorVibrant), start, start + filterString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			itemTitle.setText(spannable, TextView.BufferType.SPANNABLE);
		} else {
			itemTitle.setText(ad.title);
		}
		itemSummary.setText(ad.breadcrumbs);

		return row;
	}

	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			filterString = constraint.toString().toLowerCase();
			final ArrayList<ModData> nlist = new ArrayList<ModData>();

			for (ModData filterableData: Helpers.allModsList) {
				if (constraint.toString().equals(Helpers.NEW_MODS_SEARCH_QUERY)) {
					if (Helpers.newMods.contains(filterableData.key)) nlist.add(filterableData);
				} else if (filterableData.title.toLowerCase().contains(filterString)) nlist.add(filterableData);
			}

			FilterResults results = new FilterResults();
			results.values = nlist;
			results.count = nlist.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results) {
			modsList.clear();
			if (results.count > 0 && results.values != null)
			modsList.addAll((ArrayList<ModData>)results.values);
			sortList();
			notifyDataSetChanged();
		}
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
}