package name.mikanoshi.customiuizer.subs;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SortableListView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import miui.app.ActionBar;
import miui.util.AttributeResolver;

import miui.widget.PopupMenu;
import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.SubFragment;
import name.mikanoshi.customiuizer.utils.GuidePopup;
import name.mikanoshi.customiuizer.utils.Helpers;
import name.mikanoshi.customiuizer.utils.IconGridAdapter;
import name.mikanoshi.customiuizer.utils.PreferenceAdapter;

public class SortableList extends SubFragment {

	String key;
	int titleResId;
	SortableListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ActionBar actionBar = getActionBar();
		if (actionBar != null) actionBar.showSplitActionBar(true, true);

		Bundle args = getArguments();
		key = args.getString("key");
		titleResId = args.getInt("titleResId");

		if (getView() == null) return;

		listView = getView().findViewById(android.R.id.list);

		try {
			Field ssField = SortableListView.class.getDeclaredField("mSnapshotShadow");
			ssField.setAccessible(true);
			int lightShadow = getResources().getIdentifier("dynamic_listview_dragging_item_shadow_light", "drawable", "miui");
			int darkShadow = getResources().getIdentifier("dynamic_listview_dragging_item_shadow_dark", "drawable", "miui");
			ssField.set(listView, getResources().getDrawable(Helpers.isNightMode(getContext()) ? darkShadow : lightShadow, getContext().getTheme()));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		listView.setAdapter(new PreferenceAdapter(getContext(), key));
		listView.setOnOrderChangedListener(new SortableListView.OnOrderChangedListener() {
			@Override
			public void OnOrderChanged(int oldPos, int newPos) {
				if (oldPos == newPos) return;
				String itemStr = Helpers.prefs.getString(key, "");
				if (itemStr == null) return;
				itemStr = itemStr.trim();
				String[] itemArr = itemStr.trim().split("\\|");
				ArrayList<String> itemList = new ArrayList<String>(Arrays.asList(itemArr));
				String uuid = itemList.get(oldPos);
				itemList.remove(oldPos);
				itemList.add(newPos, uuid);
				Helpers.prefs.edit().putString(key, TextUtils.join("|", itemList)).apply();
				((PreferenceAdapter)listView.getAdapter()).updateItems();
				((PreferenceAdapter)listView.getAdapter()).notifyDataSetChanged();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String uuid = ((PreferenceAdapter)parent.getAdapter()).getItem(position);
				Bundle args = new Bundle();
				args.putString("key", key + "_" + uuid);
				args.putInt("actions", MultiAction.Actions.LOCKSCREEN.ordinal());
				openSubFragment(new MultiAction(), args, Helpers.SettingsType.Edit, Helpers.ActionBarType.Edit, titleResId, R.layout.prefs_multiaction);
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				PopupMenu popup = new PopupMenu(getContext(), view);
				popup.inflate(R.menu.menu_itemoptions);
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						switch (menuItem.getItemId()) {
							case R.id.changeicon:
								float density = getContext().getResources().getDisplayMetrics().density;
								GuidePopup iconsPopup = new GuidePopup(getContext());
								GridView grid = new GridView(getContext());
								grid.setNumColumns(4);
								grid.setAdapter(new IconGridAdapter(getContext()));
								grid.setBackgroundColor(Color.rgb(108, 108, 111));
								grid.setPadding(Math.round(7 * density), Math.round(5 * density), 0, Math.round(5 * density));
								grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
									@Override
									public void onItemClick(AdapterView<?> parent2, View view2, int position2, long id2) {
										String uuid = ((PreferenceAdapter)parent.getAdapter()).getItem(position);
										Helpers.prefs.edit().putString(key + "_" + uuid + "_icon", ((IconGridAdapter)parent2.getAdapter()).getItem(position2)).apply();
										iconsPopup.dismiss(true);
										((PreferenceAdapter)parent.getAdapter()).notifyDataSetChanged();
									}
								});
								iconsPopup.setContentView(grid);
								iconsPopup.setArrowMode(parent.getChildAt(0) == view ? 1 : 0);
								iconsPopup.setWidth(Math.round(200 * density));
								iconsPopup.setContentWidth(Math.round(200 * density));
								iconsPopup.showAsDropDown(view);
								break;
							case R.id.deleteitem:
								PreferenceAdapter adapter = (PreferenceAdapter)listView.getAdapter();
								String uuid = adapter.getItem(position);
								String items = Helpers.prefs.getString(key, "");
								Helpers.prefs.edit().putString(key, items == null || items.isEmpty() ? "" : items.replace(uuid, "").replace("||", "|").replaceAll("^\\|", "").replaceAll("\\|$", "")).apply();
								adapter.updateItems();
								adapter.notifyDataSetChanged();
								invalidateOptionsMenu();
								break;
						}
						return true;
					}
				});
				popup.show();
				return true;
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_itemactions, menu);
		menu.getItem(0).setEnabled(listView.getChildCount() < 8);
		menu.getItem(0).setIcon(AttributeResolver.resolveDrawable(getActivity(), getResources().getIdentifier("actionBarNewIcon", "attr", "miui")));
		menu.getItem(1).setIcon(AttributeResolver.resolveDrawable(getActivity(), getResources().getIdentifier("actionBarDeleteIcon", "attr", "miui")));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.deleteitem) {
			Toast.makeText(getContext(), R.string.delete_item_info, Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.additem) {
			if (listView.getChildCount() >= 8) return true;
			String uuid = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
			String items = Helpers.prefs.getString(key, "");
			Helpers.prefs.edit().putString(key, items == null || items.isEmpty() ? uuid : items + "|" + uuid).apply();
			PreferenceAdapter adapter = (PreferenceAdapter)listView.getAdapter();
			adapter.updateItems();
			adapter.notifyDataSetChanged();
			invalidateOptionsMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onPrepareOptionsMenu(Menu menu) {

	}

}