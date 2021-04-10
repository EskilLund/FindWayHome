/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

/**
 * Handles the interaction with the GPS.
 */
class GPSManager : LocationListener {
    private val TAG = "GPSManager"

    private val LOCATION_UPDATE_TIME_MS = 5000L
    private val LOCATION_UPDATE_DISTANCE_METERS = 0f

    private var locationManager : LocationManager? = null
    private var gpsListener: GPSListener? = null

    private var managerStarted = false


    interface GPSListener {
        fun onGPSUpdate(location: Location)
        fun onGPSSensorsNotExisting()
    }

    @SuppressLint("MissingPermission") // the permission is checked before using checkPermissions
    fun startGPSManager(context : Context, gpsListener: GPSListener) {
        Log.d(TAG, "startGPSManager")
        synchronized(this) {
            if (managerStarted) {
                Log.d(TAG, "startGPSManager already started")
                return
            }

            this.locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!this.locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gpsListener.onGPSSensorsNotExisting()
                return
            }

            this.gpsListener = gpsListener

            this.locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_TIME_MS,
                    LOCATION_UPDATE_DISTANCE_METERS,
                    this)

            managerStarted = true
        }
    }

    fun stopGPSManager() {
        Log.d(TAG, "stopGPSManager")
        synchronized(this) {
            if (!managerStarted) {
                Log.d(TAG, "stopGPSManager already stopped")
                return
            }

            locationManager!!.removeUpdates(this)
            gpsListener = null

            managerStarted = false
        }
    }

    override fun onLocationChanged(location: Location) {
        synchronized(this) {
            if (gpsListener != null) {
                gpsListener!!.onGPSUpdate(location)
            }
        }
    }

    override fun onStatusChanged(provider : String, status : Int, extras : Bundle) {
        // ignoring
    }
}