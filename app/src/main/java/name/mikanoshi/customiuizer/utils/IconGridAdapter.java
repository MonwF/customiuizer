package name.mikanoshi.customiuizer.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;

import name.mikanoshi.customiuizer.R;

public class IconGridAdapter implements ListAdapter {

	private final LayoutInflater mInflater;

	public IconGridAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		try {
			PackageManager manager = context.getPackageManager();
			Resources resources = manager.getResourcesForApplication("com.android.systemui");
			for (int i = Helpers.shortcutIcons.size() - 1; i >= 0; i--) try {
				int resId = resources.getIdentifier("keyguard_left_view_" + Helpers.shortcutIcons.get(i), "drawable", "com.android.systemui");
				if (resId == 0) Helpers.shortcutIcons.remove(i);
			} catch (Throwable t) {}
		} catch (Throwable t) {}
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {}

	@Override
	public int getCount() {
		return Helpers.shortcutIcons.size();
	}

	@Override
	public String getItem(int position) {
		return Helpers.shortcutIcons.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View cell;
		if (convertView != null)
			cell = convertView;
		else
			cell = mInflater.inflate(R.layout.grid_item, parent, false);

		ImageView icon = cell.findViewById(android.R.id.icon);
		String iconResName = Helpers.shortcutIcons.get(position);

		if ("miuizer".equals(iconResName))
			icon.setImageResource(R.drawable.keyguard_bottom_miuizer_shortcut_img);
		else try {
			PackageManager manager = cell.getContext().getPackageManager();
			Resources resources = manager.getResourcesForApplication("com.android.systemui");
			int resId = resources.getIdentifier("keyguard_left_view_" + iconResName, "drawable", "com.android.systemui");
			Drawable drawable = resources.getDrawable(resId, cell.getContext().getTheme());
			icon.setImageDrawable(drawable);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return cell;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}