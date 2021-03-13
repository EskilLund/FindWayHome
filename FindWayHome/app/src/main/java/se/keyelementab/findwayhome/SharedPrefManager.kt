package se.keyelementab.findwayhome

import android.content.Context
import android.content.SharedPreferences
import android.location.Location


class SharedPrefManager {
    val KEY_LATITUDE = "destination_latitude"
    val KEY_LONGITUDE = "destination_longitude"

    fun setLongLat(context: Context, location: Location) {
        val sharedPreference = context.getSharedPreferences("PREFERENCE_DESTINATION", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        putDouble(editor, KEY_LONGITUDE, location.longitude)
        putDouble(editor, KEY_LATITUDE, location.latitude)
        editor.commit() // commit = sync, apply = async
    }

    fun getLongitude(context: Context) : Double {
        val sharedPreference =  context.getSharedPreferences("PREFERENCE_DESTINATION", Context.MODE_PRIVATE)
        return getDouble(sharedPreference, KEY_LONGITUDE)
    }

    fun getLatitude(context: Context) : Double {
        val sharedPreference = context.getSharedPreferences("PREFERENCE_DESTINATION", Context.MODE_PRIVATE)
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
        return java.lang.Double.longBitsToDouble(prefs.getLong(key, 0L))
    }
}