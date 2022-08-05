package name.mikanoshi.customiuizer.qs;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import name.mikanoshi.customiuizer.R;

public class FiveGService extends TileService {

	void updateTile() {
		Tile tile = this.getQsTile();
		if (tile == null) return;

		Icon newIcon;
		int newState;

		UiModeManager uiManager = (UiModeManager)getSystemService(Context.UI_MODE_SERVICE);
		if (uiManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_darkmode_enabled);
			newState = Tile.STATE_ACTIVE;
		} else {
			newIcon = Icon.createWithResource(getApplicationContext(),	R.drawable.ic_qs_darkmode_disabled);
			newState = Tile.STATE_INACTIVE;
		}

		tile.setIcon(newIcon);
		tile.setState(newState);
		tile.updateTile();
	}

	private void switchTileState() {
		try {
			UiModeManager uiManager = (UiModeManager)getSystemService(Context.UI_MODE_SERVICE);
			uiManager.setNightMode(uiManager.getNightMode() != UiModeManager.MODE_NIGHT_YES ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO);
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

}

