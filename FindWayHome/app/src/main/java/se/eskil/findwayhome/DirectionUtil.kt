/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.content.Context
import kotlin.math.roundToInt

class DirectionUtil {
    private val TAG = "DirectionUtil"

    /**
     * Calculates how many degrees to turn the image to point to the destination.
     *
     * @param bearingToDestination the direction to the destination in degrees relating to true north
     * , i.e. not magnetic north, ranging from -180 to 180.
     * @param bearingCompass the direction in degrees relating to true north
     * , i.e. not magnetic north, ranging from 0 to 360.
     * @return the degrees to turn the image, ranging from -180 to 180.
     */
    fun degreesToTurnImage(bearingToDestination : Float,
                           bearingCompass : Float) : Float {
        var degreesToTurn = 360f - bearingCompass + bearingToDestination

        if (degreesToTurn <= -360) {
            degreesToTurn = degreesToTurn + 360
        }

        if (degreesToTurn >= 360) {
            degreesToTurn = degreesToTurn - 360
        }

        return degreesToTurn
    }

    fun getDistanceString(meters : Float, context : Context) : String {
        return if (meters >= 1000f) {
            String.format(context.getString(R.string.distance_kilometers),
                "%.1f".format(meters / 1000f))
        } else {
            String.format(context.getString(R.string.distance_meters), meters.roundToInt())
        }
    }
}