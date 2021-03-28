package se.keyelementab.findwayhome

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log

/**
 * Handles the interaction with the GPS.
 */
class GPSManager : LocationListener {
    private val TAG = "GPSManager"

    private val LOCATION_UPDATE_TIME_MS = 5000L
    private val LOCATION_UPDATE_DISTANCE_METERS = 0f

    private val locationManager : LocationManager
    private val gpsListener: GPSListener

    private var managerStarted = false


    interface GPSListener {
        fun onGPSUpdate(location: Location)
    }

    constructor(context : Context, gpsListener: GPSListener) {
        this.locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        this.gpsListener = gpsListener
    }

    @SuppressLint("MissingPermission") // the permission is checked before using checkPermissions
    fun startGPSManager() {
        Log.d(TAG, "startGPSManager")
        if (managerStarted) {
            Log.d(TAG, "startGPSManager already started")
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_UPDATE_TIME_MS,
            LOCATION_UPDATE_DISTANCE_METERS,
            this
        )
    }

    fun stopGPSManager() {
        Log.d(TAG, "stopGPSManager")
        if (!managerStarted) {
            Log.d(TAG, "stopGPSManager already stopped")
        }

        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        gpsListener.onGPSUpdate(location)
    }


}