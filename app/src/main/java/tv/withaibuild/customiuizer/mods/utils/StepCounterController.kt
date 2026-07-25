package tv.withaibuild.customiuizer.mods.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

object StepCounterController {

    private val stepViewList = CopyOnWriteArrayList<TextView>()
    private var context: Context? = null
    private var timeTickReceiver: BroadcastReceiver? = null
    private var pendingUpdateJob: Job? = null

    private var scope: CoroutineScope = newScope()

    private fun newScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @JvmStatic
    fun removeStepViewByTag(tag: String) {
        stepViewList.removeIf { tag == it.tag }
        if (stepViewList.isEmpty()) {
            pendingUpdateJob?.cancel()
            pendingUpdateJob = null
        }
    }

    @JvmStatic
    fun addStepView(sv: TextView) {
        stepViewList.add(sv)
        pendingUpdateJob?.cancel()
        val ctx = context ?: return
        pendingUpdateJob = scope.launch {
            delay(3000L)
            updateSteps(ctx)
        }
    }

    @JvmStatic
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun initContext(context: Context) {
        // Cancel any pending work from a previous context and recreate the scope.
        scope.cancel()
        scope = newScope()
        pendingUpdateJob?.cancel()
        pendingUpdateJob = null

        val oldContext = this.context
        val oldReceiver = timeTickReceiver
        oldReceiver?.let {
            try {
                oldContext?.unregisterReceiver(it)
            } catch (ignored: Throwable) {
            }
        }

        this.context = context
        timeTickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                scope.launch { updateSteps(context) }
            }
        }
        context.registerReceiver(
            timeTickReceiver,
            IntentFilter("android.intent.action.TIME_TICK"),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    private suspend fun updateSteps(context: Context) {
        if (stepViewList.isEmpty()) return

        val newText = withContext(Dispatchers.IO) {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(
                    Uri.parse("content://com.mi.health.provider.main/activity/steps/brief"),
                    arrayOf("steps", "goal"),
                    null, null, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val stepCount = cursor.getString(0)
                    val stepGoal = cursor.getString(1)
                    val text = "$stepCount/$stepGoal"
                    if (text == stepsWithGoal) {
                        null
                    } else {
                        stepsWithGoal = text
                        text
                    }
                } else {
                    null
                }
            } catch (t: Throwable) {
                XposedHelpers.log(t)
                null
            } finally {
                cursor?.close()
            }
        }

        newText?.let { text ->
            for (tv in stepViewList) {
                tv.text = text
            }
        }
    }

    private var stepsWithGoal: String? = null
}
