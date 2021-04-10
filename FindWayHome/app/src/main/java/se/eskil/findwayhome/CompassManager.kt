/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Handles the interaction with the compass sensor.
 */
class CompassManager : SensorEventListener {
    private val TAG = "CompassManager"

    public val COMPASS_UPDATE_DELAY_MS = 200
    private var latestCompassUpdateTimeMs : Long? = null

    private var sensorManager : SensorManager? = null
    private var compassListener: CompassListener? = null

    private var managerStarted = false


    interface CompassListener {
        fun onCompassHeading(heading: Float)
    }


    fun startCompassManager(context : Context, compassListener: CompassListener) {
        Log.d(TAG, "startCompassManager")
        if (managerStarted) {
            Log.d(TAG, "startCompassManager already started")
            return
        }

        synchronized(this) {
            this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            this.compassListener = compassListener

            sensorManager!!.registerListener(
                this,
                sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI)
            }
    }

    fun stopCompassManager() {
        Log.d(TAG, "stopGPSManager")
        if (!managerStarted) {
            Log.d(TAG, "stopGPSManager already stopped")
            return
        }
        synchronized(this) {
            sensorManager!!.unregisterListener(this)
            compassListener = null
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // these calls are too frequent and updates are not needed that often. Ignoring calls until
        // COMPASS_UPDATE_DELAY_MS millisecs have passed.
        if (latestCompassUpdateTimeMs != null &&
            (System.currentTimeMillis() - latestCompassUpdateTimeMs!!) < COMPASS_UPDATE_DELAY_MS) {
            return
        }
        latestCompassUpdateTimeMs = System.currentTimeMillis()

        val degree = Math.round(event!!.values[0]).toFloat()
        Log.d(TAG, "onSensorChanged, bearing: " + degree)
        synchronized(this) {
            if (compassListener != null) {
                compassListener!!.onCompassHeading(degree)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged, accuracy: " + accuracy)
    }

}