package se.eskil.findwayhome

import android.content.Context
import android.content.SharedPreferences
import android.location.Location


class SharedPrefManager {
    private val KEY_LATITUDE = "destination_latitude"
    private val KEY_LONGITUDE = "destination_longitude"
    private val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
    private val SHARED_PREF_NAME = "PREFERENCE_DESTINATION"
    private val DEFAULT_VALUE = 0L

    /** Cache values so that the Shared Pref doesn't have to be read every time. */
    var destinationLatCache : Double? = null
    var destinationLongCache : Double? = null

    fun setLongLat(context: Context, location: Location) {
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

    fun getLongitude(context: Context) : Double {
        if (destinationLongCache != null) {
            return destinationLongCache!!
        }
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return getDouble(sharedPreference, KEY_LONGITUDE)
    }

    fun getLatitude(context: Context) : Double {
        if (destinationLatCache != null) {
            return destinationLatCache!!
        }
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return getDouble(sharedPreference, KEY_LATITUDE)
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
