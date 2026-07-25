package tv.withaibuild.customiuizer.tasker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import tv.withaibuild.customiuizer.mods.GlobalActions

class UnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE)
        if (bundle != null) {
            val sendIntent = Intent().apply {
                action = GlobalActions.ACTION_PREFIX + "UnlockSetForced"
                putExtras(bundle)
            }
            context.sendBroadcast(sendIntent)
        }
    }
}
