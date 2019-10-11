package name.mikanoshi.customiuizer.qs;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import name.mikanoshi.customiuizer.R;
import name.mikanoshi.customiuizer.utils.Helpers;

public class DarkModeService extends TileService {

	void updateTile() {
		Tile tile = this.getQsTile();
		if (tile == null) return;

		Icon newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_darkmode_disabled);
		String newLabel = getString(R.string.qs_toggle_darkmode);
		int newState = Tile.STATE_UNAVAILABLE;

		UiModeManager uiManager = (UiModeManager)getSystemService(Context.UI_MODE_SERVICE);
		if (uiManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
			newLabel = getString(R.string.array_color_dark);
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_darkmode_enabled);
			newState = Tile.STATE_ACTIVE;
		} else if (uiManager.getNightMode() == UiModeManager.MODE_NIGHT_NO) {
			newLabel = getString(R.string.array_color_light);
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_darkmode_disabled);
			newState = Tile.STATE_INACTIVE;
		} else if (uiManager.getNightMode() == UiModeManager.MODE_NIGHT_AUTO) {
			newLabel = getString(R.string.array_color_auto);
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_darkmode_enabled);
			newState = Tile.STATE_ACTIVE;
		}

		tile.setLabel(newLabel);
		tile.setIcon(newIcon);
		tile.setState(newState);
		tile.updateTile();
	}

	private void switchTileState() {
		try {
			if (!checkUIModePermission()) {
				Toast.makeText(this, R.string.qs_toggle_darkmode_noperm, Toast.LENGTH_LONG).show();
			} else {
				UiModeManager uiManager = (UiModeManager)getSystemService(Context.UI_MODE_SERVICE);
				if (uiManager.getNightMode() == UiModeManager.MODE_NIGHT_NO) uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
				else if (uiManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) uiManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
				else if (uiManager.getNightMode() == UiModeManager.MODE_NIGHT_AUTO) uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
			}
		} catch (Throwable t) {
			t.printStackTrace();
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

	private boolean checkUIModePermission() {
		PackageManager pm = getPackageManager();
		return pm.checkPermission("android.permission.MODIFY_DAY_NIGHT_MODE", Helpers.modulePkg) == PackageManager.PERMISSION_GRANTED;
	}

}

