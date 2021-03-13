package se.keyelementab.findwayhome

import android.content.Context
import android.content.SharedPreferences
import android.location.Location


class SharedPrefManager {
    private val KEY_LATITUDE = "destination_latitude"
    private val KEY_LONGITUDE = "destination_longitude"
    private val SHARED_PREF_NAME = "PREFERENCE_DESTINATION"

    private val DEFAULT_VALUE = 0L

    fun setLongLat(context: Context, location: Location) {
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        putDouble(editor, KEY_LONGITUDE, location.longitude)
        putDouble(editor, KEY_LATITUDE, location.latitude)
        editor.commit() // commit = sync, apply = async
    }

    fun isDestinationSet(context: Context) : Boolean {
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreference.contains(KEY_LONGITUDE) && sharedPreference.contains(KEY_LATITUDE)
    }

    fun getLongitude(context: Context) : Double {
        val sharedPreference = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return getDouble(sharedPreference, KEY_LONGITUDE)
    }

    fun getLatitude(context: Context) : Double {
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
