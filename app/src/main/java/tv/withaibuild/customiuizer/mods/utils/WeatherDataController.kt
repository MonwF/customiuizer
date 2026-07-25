package tv.withaibuild.customiuizer.mods.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

object WeatherDataController {

    @JvmField
    var weatherInfo: String = ""

    private var weakReferenceContext: WeakReference<Context>? = null
    private var weakReferenceRunnable: Runnable? = null
    private var timeTickReceiver: BroadcastReceiver? = null
    private var context: Context? = null

    private val lock = Any()
    private var controllerScope = newScope()

    private fun newScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private fun queryWeather() {
        val ctx: Context
        synchronized(lock) {
            ctx = weakReferenceContext?.get() ?: return
        }

        var cursor: Cursor? = null
        try {
            cursor = ctx.contentResolver.query(
                Uri.parse("content://weather/actualWeatherData/1"),
                null, null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                var newWeather = ""
                var columnIndex = cursor.getColumnIndex("description")
                if (columnIndex >= 0) {
                    newWeather = cursor.getString(columnIndex)
                }
                columnIndex = cursor.getColumnIndex("temperature")
                if (columnIndex >= 0) {
                    newWeather += (" " + cursor.getString(columnIndex))
                }
                weatherInfo = newWeather
            }
        } catch (ignored: Throwable) {
        } finally {
            cursor?.close()
        }
    }

    @JvmStatic
    fun refreshWeatherData(forceRefresh: Boolean) {
        controllerScope.launch {
            withContext(Dispatchers.IO) { queryWeather() }
            if (forceRefresh) {
                val r: Runnable?
                synchronized(lock) {
                    r = weakReferenceRunnable
                    weakReferenceRunnable = null
                }
                r?.run()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @JvmStatic
    fun initContext(context: Context, updateTimeRunnable: Runnable) {
        synchronized(lock) {
            weakReferenceContext = WeakReference(context)
            weakReferenceRunnable = updateTimeRunnable
        }

        // Cancel any pending work from a previous context and start fresh.
        controllerScope.cancel()
        controllerScope = newScope()

        val oldContext: Context?
        val oldReceiver: BroadcastReceiver?
        synchronized(lock) {
            oldContext = this.context
            oldReceiver = timeTickReceiver
        }
        oldReceiver?.let {
            try {
                oldContext?.unregisterReceiver(it)
            } catch (ignored: Throwable) {
            }
        }

        this.context = context
        timeTickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                refreshWeatherData(false)
            }
        }
        context.registerReceiver(
            timeTickReceiver,
            IntentFilter("android.intent.action.TIME_TICK"),
            Context.RECEIVER_NOT_EXPORTED
        )

        controllerScope.launch {
            delay(1800)
            refreshWeatherData(true)
        }
    }
}
