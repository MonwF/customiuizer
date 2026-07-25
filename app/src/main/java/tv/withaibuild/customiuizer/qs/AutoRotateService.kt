package tv.withaibuild.customiuizer.qs

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers

class AutoRotateService : TileService() {

    private fun updateTile() {
        Helpers.withinAppContext = true
        val tile = qsTile ?: return
        val state = getTileState()

        val (newIcon, newLabel, newState) = when (state) {
            0 -> Triple(
                Icon.createWithResource(applicationContext, R.drawable.ic_qs_autorotate_disabled),
                getString(R.string.qs_toggle_autorotate_off),
                Tile.STATE_INACTIVE
            )
            1 -> Triple(
                Icon.createWithResource(applicationContext, R.drawable.ic_qs_autorotate_lock_portrait),
                getString(R.string.qs_toggle_autorotate_portrait),
                Tile.STATE_ACTIVE
            )
            2 -> Triple(
                Icon.createWithResource(applicationContext, R.drawable.ic_qs_autorotate_lock_landscape),
                getString(R.string.qs_toggle_autorotate_landscape),
                Tile.STATE_ACTIVE
            )
            else -> Triple(
                Icon.createWithResource(applicationContext, R.drawable.ic_qs_autorotate_disabled),
                getString(R.string.qs_toggle_autorotate),
                Tile.STATE_UNAVAILABLE
            )
        }

        tile.label = newLabel
        tile.icon = newIcon
        tile.state = newState
        tile.updateTile()
    }

    private fun switchTileState() {
        try {
            val prefs = AppHelper.getSharedPrefs(this, false)
            var state = prefs.getInt("pref_key_qs_autorotate_state", 0)
            state++
            if (state > 2) state = 0
            prefs.edit().putInt("pref_key_qs_autorotate_state", state).apply()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun getTileState(): Int {
        return try {
            val prefs = AppHelper.getSharedPrefs(this, false)
            prefs.getInt("pref_key_qs_autorotate_state", 0)
        } catch (t: Throwable) {
            t.printStackTrace()
            0
        }
    }

    override fun onTileAdded() {
        updateTile()
    }

    override fun onTileRemoved() {}

    override fun onClick() {
        switchTileState()
        updateTile()
    }

    override fun onStartListening() {
        updateTile()
    }

    override fun onStopListening() {}
}
