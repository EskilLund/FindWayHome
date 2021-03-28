package se.keyelementab.findwayhome

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CompassManager : SensorEventListener {
    private val TAG = "CompassManager"

    private val COMPASS_UPDATE_DELAY_MS = 200
    private var latestCompassUpdateTimeMs : Long? = null


    private lateinit var context : Context
    private lateinit var sensorManager : SensorManager
    private lateinit var compassListener: CompassListener

    interface CompassListener {
        fun onCompassHeading(heading: Float)
    }

    constructor(context : Context, compassListener: CompassListener) {
        this.context = context
        this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        this.compassListener = compassListener
    }

    fun startCompassManager() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI)
    }

    fun stopCompassManager() {
        sensorManager.unregisterListener(this)
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
        compassListener.onCompassHeading(degree)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged, accuracy: " + accuracy)
    }

}