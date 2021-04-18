/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 * This is based on https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/CompassActivity.java
 */
package se.eskil.findwayhome

import android.content.Context
import android.hardware.*
import android.location.Location
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread


/**
 * Handles the interaction with the compass sensor.
 */
class CompassManager : SensorEventListener {
    private val TAG = "CompassManager"

    public val COMPASS_UPDATE_DELAY_MS = 200
    private var latestCompassUpdateTimeMs : Long? = null

    private var windowManager : WindowManager? = null
    private var sensorManager : SensorManager? = null
    private var gsensor: Sensor? = null
    private var msensor: Sensor? = null
    private var deprecatedSensor: Sensor? = null
    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val R = FloatArray(9)
    private val I = FloatArray(9)

    private var compassListener: CompassListener? = null
    private var managerStarted = false

    private var location: Location? = null

    interface CompassListener {
        @WorkerThread
        fun onCompassHeading(heading: Float)
        @UiThread
        fun onCompassSensorsNotExisting()
    }

    @UiThread
    fun startCompassManager(context: Context, compassListener: CompassListener) {
        Log.d(TAG, "startCompassManager")
        synchronized(this) {
            if (managerStarted) {
                Log.d(TAG, "startCompassManager already started")
                return
            }

            this.windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            this.gsensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            this.msensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            this.deprecatedSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION)

            if ((this.gsensor == null || this.msensor == null) && deprecatedSensor == null) {
                compassListener.onCompassSensorsNotExisting()
                return
            }

            this.compassListener = compassListener

            sensorManager!!.registerListener(
                    this,
                    gsensor,
                    SensorManager.SENSOR_DELAY_GAME
            )
            sensorManager!!.registerListener(
                    this,
                    msensor,
                    SensorManager.SENSOR_DELAY_GAME
            )
            sensorManager!!.registerListener(
                    this,
                    deprecatedSensor,
                    SensorManager.SENSOR_DELAY_GAME
            )

            managerStarted = true
        }
    }

    @UiThread
    fun stopCompassManager() {
        Log.d(TAG, "stopGPSManager")
        synchronized(this) {
            if (!managerStarted) {
                Log.d(TAG, "stopGPSManager already stopped")
                return
            }
            sensorManager!!.unregisterListener(this)
            compassListener = null

            managerStarted = false
        }
    }

    @UiThread
    fun setLocation(location: Location) {
        synchronized(this) {
            this.location = location
        }
    }

    @WorkerThread
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        // these calls are too frequent and updates are not needed that often. Ignoring calls until
        // COMPASS_UPDATE_DELAY_MS millisecs have passed.
        if (latestCompassUpdateTimeMs != null &&
            (System.currentTimeMillis() - latestCompassUpdateTimeMs!!) < COMPASS_UPDATE_DELAY_MS) {
            return
        }
        latestCompassUpdateTimeMs = System.currentTimeMillis()

        val alpha = 0.30f // lower value = faster
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                val azimuth = (event.values[0] + getRotationOffset() + 360) % 360
                if (mGravity[0] == 0f && mGeomagnetic[0] == 0f) {
                    // only use the Sensor.TYPE_ORIENTATION as a backup if the other sensors aren't working
                    if (compassListener != null) {
                        compassListener!!.onCompassHeading(azimuth)
                    }
                }
                return
            }

            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0]
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1]
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2]
            }

            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2]
            }

            val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + 360) % 360
                Log.d(TAG, "onSensorChanged, bearing magnetic north: " + azimuth)

                if (location != null) {
                    // converts magnetic north to true north
                    val geoField = GeomagneticField(
                            location!!.getLatitude().toFloat(),
                            location!!.getLongitude().toFloat(),
                            location!!.getAltitude().toFloat(),
                            System.currentTimeMillis()
                    )
                    Log.d(TAG, "Magnetic declination: " + geoField.declination)
                    azimuth = (azimuth + geoField.declination) % 360
                }

                if (compassListener != null) {
                    compassListener!!.onCompassHeading(azimuth)
                }
            }
        }
    }

    @WorkerThread
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged, accuracy: " + accuracy)
    }

    fun getRotationOffset(): Int {
        if (windowManager == null) {
            return 0
        }

        return when (windowManager!!.getDefaultDisplay().getRotation()) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
    }
}
}