package tv.withaibuild.customiuizer.mods.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import tv.withaibuild.customiuizer.mods.GlobalActions
import kotlin.math.abs

class ShakeManager(private val helperContext: Context) : SensorEventListener {

    private var xAccel: Float = 0f
    private var yAccel: Float = 0f
    private var zAccel: Float = 0f

    private var xPreviousAccel: Float = 0f
    private var yPreviousAccel: Float = 0f
    private var zPreviousAccel: Float = 0f

    private var firstUpdate = true
    private var shakeInitiated = false
    private var lastShakeEvent = System.currentTimeMillis()

    fun reset() {
        xAccel = 0f
        yAccel = 0f
        zAccel = 0f
        xPreviousAccel = 0f
        yPreviousAccel = 0f
        zPreviousAccel = 0f
        firstUpdate = true
        shakeInitiated = false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Don't care...
    }

    override fun onSensorChanged(se: SensorEvent) {
        updateAccelParameters(se.values[0], se.values[1], se.values[2])
        when {
            !shakeInitiated && isAccelerationChanged() -> shakeInitiated = true
            shakeInitiated && isAccelerationChanged() -> executeShakeActionDelayed()
            shakeInitiated && !isAccelerationChanged() -> shakeInitiated = false
        }
    }

    private fun updateAccelParameters(xNewAccel: Float, yNewAccel: Float, zNewAccel: Float) {
        if (firstUpdate) {
            xPreviousAccel = xNewAccel
            yPreviousAccel = yNewAccel
            zPreviousAccel = zNewAccel
            firstUpdate = false
        } else {
            xPreviousAccel = xAccel
            yPreviousAccel = yAccel
            zPreviousAccel = zAccel
        }
        xAccel = xNewAccel
        yAccel = yNewAccel
        zAccel = zNewAccel
    }

    private fun isAccelerationChanged(): Boolean {
        val deltaX = abs(xPreviousAccel - xAccel)
        val deltaY = abs(yPreviousAccel - yAccel)
        val deltaZ = abs(zPreviousAccel - zAccel)
        val shakeThresholdX = 4f
        val shakeThresholdY = 4f
        val shakeThresholdZ = 8f
        return (deltaX > shakeThresholdX && deltaY > shakeThresholdY)
            || (deltaX > shakeThresholdX && deltaZ > shakeThresholdZ)
            || (deltaY > shakeThresholdY && deltaZ > shakeThresholdZ)
    }

    private fun executeShakeActionDelayed() {
        val now = System.currentTimeMillis()
        val shakeEventThrottle = 750
        if (now - lastShakeEvent > shakeEventThrottle) {
            lastShakeEvent = now
            executeShakeAction()
        }
    }

    private fun executeShakeAction() {
        GlobalActions.handleAction(helperContext, "pref_key_launcher_shake")
    }
}
