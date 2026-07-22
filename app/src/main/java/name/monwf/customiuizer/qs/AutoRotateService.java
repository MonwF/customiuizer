package name.monwf.customiuizer.qs;

import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import name.monwf.customiuizer.R;
import name.monwf.customiuizer.utils.AppHelper;
import name.monwf.customiuizer.utils.Helpers;

public class AutoRotateService extends TileService {

	void updateTile() {
		Helpers.withinAppContext = true;
		Tile tile = this.getQsTile();
		if (tile == null) return;
		int state = getTileState();

		Icon newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_autorotate_disabled);
		String newLabel = getString(R.string.qs_toggle_autorotate);
		int newState = Tile.STATE_UNAVAILABLE;

		if (state == 0) {
			newLabel = getString(R.string.qs_toggle_autorotate_off);
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_autorotate_disabled);
			newState = Tile.STATE_INACTIVE;
		} else if (state == 1) {
			newLabel = getString(R.string.qs_toggle_autorotate_portrait);
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_autorotate_lock_portrait);
			newState = Tile.STATE_ACTIVE;
		} else if (state == 2) {
			newLabel = getString(R.string.qs_toggle_autorotate_landscape);
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_autorotate_lock_landscape);
			newState = Tile.STATE_ACTIVE;
		}

		tile.setLabel(newLabel);
		tile.setIcon(newIcon);
		tile.setState(newState);
		tile.updateTile();
	}

	private void switchTileState() {
		try {
			SharedPreferences prefs = AppHelper.getSharedPrefs(this, false);
			int state = prefs.getInt("pref_key_qs_autorotate_state", 0);
			state++;
			if (state > 2) state = 0;
			prefs.edit().putInt("pref_key_qs_autorotate_state", state).apply();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private int getTileState() {
		try {
			SharedPreferences prefs = AppHelper.getSharedPrefs(this, false);
			return prefs.getInt("pref_key_qs_autorotate_state", 0);
		} catch (Throwable t) {
			t.printStackTrace();
			return 0;
		}
	}

	@Override
	public void onTileAdded() {
		updateTile();
	}

	@Override
	public void onTileRemoved() {}

	@Override
	public void onClick() {
		switchTileState();
		updateTile();
	}

	@Override
	public void onStartListening() {
		updateTile();
	}

	@Override
	public void onStopListening() {}

}

