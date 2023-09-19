package name.monwf.customiuizer.subs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.SubFragment;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;
import name.monwf.customiuizer.utils.PreferenceAdapter;
import name.monwf.customiuizer.utils.SortableListView;

@SuppressWarnings("ConstantConditions")
public class SortableList extends SubFragment {

	String key;
	String titleResId;
	boolean activities;
	SortableListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.padded = false;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		toolbarMenu = true;
		super.onActivityCreated(savedInstanceState);

		Bundle args = getArguments();
		key = args.getString("key");
		titleResId = args.getString("titleResId");
		activities = args.getBoolean("activities", false);

		if (getView() == null) return;

		listView = getView().findViewById(android.R.id.list);

		if (!activities) try {
			Field ssField = SortableListView.class.getDeclaredField("mSnapshotShadow");
			ssField.setAccessible(true);
			int lightShadow = getResources().getIdentifier("dynamic_listview_dragging_item_shadow_light", "drawable", "miui");
			int darkShadow = getResources().getIdentifier("dynamic_listview_dragging_item_shadow_dark", "drawable", "miui");
			ssField.set(listView, getResources().getDrawable(Helpers.isNightMode(getContext()) ? darkShadow : lightShadow, getContext().getTheme()));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		listView.setAdapter(new PreferenceAdapter(getContext(), key, activities));
		if (activities)	listView.setOnOrderChangedListener(null); else
		listView.setOnOrderChangedListener(new SortableListView.OnOrderChangedListener() {
			@Override
			public void OnOrderChanged(int oldPos, int newPos) {
				if (oldPos == newPos) return;
				String itemStr = AppHelper.getStringOfAppPrefs(key, "");
				if (itemStr.isEmpty()) return;
				String[] itemArr = itemStr.trim().split("\\|");
				ArrayList<String> itemList = new ArrayList<String>(Arrays.asList(itemArr));
				String uuid = itemList.get(oldPos);
				itemList.remove(oldPos);
				itemList.add(newPos, uuid);
				AppHelper.appPrefs.edit().putString(key, TextUtils.join("|", itemList)).apply();
				((PreferenceAdapter)listView.getAdapter()).updateItems();
				((PreferenceAdapter)listView.getAdapter()).notifyDataSetChanged();
			}
		});
		if (!activities) {
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
		}
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				deleteItem(position);

//				PopupMenu popup = new PopupMenu(getContext(), view);
//				popup.inflate(R.menu.menu_itemoptions);
//				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//					@Override
//					public boolean onMenuItemClick(MenuItem menuItem) {
//						switch (menuItem.getItemId()) {
//							case R.id.changeicon:
//								float density = getContext().getResources().getDisplayMetrics().density;
//								GuidePopup iconsPopup = new GuidePopup(getContext());
//								GridView grid = new GridView(getContext());
//								grid.setNumColumns(4);
//								grid.setAdapter(new IconGridAdapter(getContext()));
//								grid.setBackgroundColor(Color.rgb(108, 108, 111));
//								grid.setPadding(Math.round(7 * density), Math.round(5 * density), 0, Math.round(5 * density));
//								grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//									@Override
//									public void onItemClick(AdapterView<?> parent2, View view2, int position2, long id2) {
//										String uuid = ((PreferenceAdapter)parent.getAdapter()).getItem(position);
//										AppHelper.prefs.edit().putString(key + "_" + uuid + "_icon", ((IconGridAdapter)parent2.getAdapter()).getItem(position2)).apply();
//										iconsPopup.dismiss(true);
//										((PreferenceAdapter)parent.getAdapter()).notifyDataSetChanged();
//									}
//								});
//								iconsPopup.setContentView(grid);
//								iconsPopup.setArrowMode(parent.getChildAt(0) == view ? 1 : 0);
//								iconsPopup.setWidth(Math.round(200 * density));
//								iconsPopup.setContentWidth(Math.round(200 * density));
//								iconsPopup.showAsDropDown(view);
//								break;
//							case R.id.deleteitem:
//								PreferenceAdapter adapter = (PreferenceAdapter)listView.getAdapter();
//								String uuid = adapter.getItem(position);
//								String items = AppHelper.prefs.getString(key, "");
//								AppHelper.prefs.edit().putString(key, items == null || items.isEmpty() ? "" : items.replace(uuid, "").replace("||", "|").replaceAll("^\\|", "").replaceAll("\\|$", "")).apply();
//								adapter.updateItems();
//								adapter.notifyDataSetChanged();
//								invalidateOptionsMenu();
//								break;
//						}
//						return true;
//					}
//				});
//				popup.show();
				return true;
			}
		});
	}

	private String createNewUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
	}

	private void createNewItem(String uuid) {
		String items = AppHelper.getStringOfAppPrefs(key, "");
		AppHelper.appPrefs.edit().putString(key, items.isEmpty() ? uuid : items + "|" + uuid).apply();
		PreferenceAdapter adapter = (PreferenceAdapter)listView.getAdapter();
		adapter.updateItems();
		adapter.notifyDataSetChanged();
	}

	private void deleteItem(int position) {
		PreferenceAdapter adapter = (PreferenceAdapter)listView.getAdapter();
		String items = AppHelper.getStringOfAppPrefs(key, "");
		AppHelper.appPrefs.edit().putString(key, items.isEmpty() ? "" : items.replace(adapter.getItem(position), "").replace("||", "|").replaceAll("^\\|", "").replaceAll("\\|$", "")).apply();
		adapter.updateItems();
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.deleteitem) {
			Toast.makeText(getContext(), R.string.delete_item_info, Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.additem) {
			if (activities) {
				Bundle args = new Bundle();
				args.putBoolean("activity", true);
				args.putString("key", key);
				AppSelector activitySelect = new AppSelector();
				activitySelect.setTargetFragment(SortableList.this, 2);
				openSubFragment(activitySelect, args, Helpers.SettingsType.Edit, Helpers.ActionBarType.HomeUp, R.string.select_app, R.layout.prefs_app_selector);
			} else {
				//if (listView.getChildCount() >= 10) return true;
				createNewItem(createNewUUID());
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onPrepareOptionsMenu(Menu menu) {}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 2) {
				String activityValue = data.getStringExtra("activity");
				int activityUser = data.getIntExtra("user", 0);
				if (activityValue == null || activityUser < 0) return;

				String uuid = createNewUUID();
				AppHelper.appPrefs.edit().putInt(key + "_" + uuid + "_action", 20).putString(key + "_" + uuid + "_activity", activityValue).putInt(key + "_" + uuid + "_activity_user", activityUser).apply();
				createNewItem(uuid);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}