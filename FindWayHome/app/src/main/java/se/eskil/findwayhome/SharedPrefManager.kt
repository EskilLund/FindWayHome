/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import androidx.annotation.VisibleForTesting

class SharedPrefManager {
    private val KEY_LATITUDE = "destination_latitude"
    private val KEY_LONGITUDE = "destination_longitude"
    private val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
    @VisibleForTesting
    public val SHARED_PREF_NAME = "PREFERENCE_DESTINATION"
    private val DEFAULT_VALUE = 0L

    /** Cache values so that the Shared Pref doesn't have to be read every time. */
    var destinationLatCache : Double? = null
    var destinationLongCache : Double? = null

    fun setDestination(context: Context, location: Location) {
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        putDouble(editor, KEY_LONGITUDE, location.longitude)
        putDouble(editor, KEY_LATITUDE, location.latitude)
        editor.commit() // commit = sync, apply = async
        destinationLatCache = location.latitude
        destinationLongCache = location.longitude
    }

    fun isDestinationSet(context: Context) : Boolean {
        if (destinationLatCache != null || destinationLongCache != null) {
            return true
        }
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreference.contains(KEY_LONGITUDE) && sharedPreference.contains(KEY_LATITUDE)
    }

    fun getDestination(context: Context) : Location {
        val destination = Location(LocationManager.GPS_PROVIDER)
        destination.latitude = getLatitude(context)
        destination.longitude = getLongitude(context)
        return destination
    }

    fun isDisclaimerAccepted(context: Context) : Boolean {
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreference.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
    }

    fun setDisclaimerAccepted(context: Context) {
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putBoolean(KEY_DISCLAIMER_ACCEPTED, true)
        editor.commit() // commit = sync, apply = async
    }

    private fun getLongitude(context: Context) : Double {
        if (destinationLongCache != null) {
            return destinationLongCache!!
        }
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return getDouble(sharedPreference, KEY_LONGITUDE)
    }

    private fun getLatitude(context: Context) : Double {
        if (destinationLatCache != null) {
            return destinationLatCache!!
        }
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return getDouble(sharedPreference, KEY_LATITUDE)
    }

    /**
     * Util method, as {@link SharedPreferences} can not store {@link Double} without conversion.
     */
    private fun putDouble(edit: SharedPreferences.Editor, key: String, value: Double) {
        edit.putLong(key, java.lang.Double.doubleToRawLongBits(value))
    }

    /**
     * Util method, as {@link SharedPreferences} can not store {@link Double} without conversion.
     */
    private fun getDouble(prefs: SharedPreferences, key: String): Double {
        return java.lang.Double.longBitsToDouble(prefs.getLong(key, DEFAULT_VALUE))
    }
}
