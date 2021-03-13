package se.keyelementab.findwayhome

import android.content.Context
import android.location.Location
import android.util.Log

class DirectionManager {
    private val TAG = "DirectionManager"

    /**
     * TODO
     *
     * @param bearingToDestination the direction to the destination in degrees relating to true north
     * , i.e. not magnetic north, ranging from -180 to 180.
     * @param bearingCompass the direction in degrees relating to true north
     * , i.e. not magnetic north, ranging from 0 to 360.
     * @return the degrees to turn the image, ranging from -180 to 180.
     */
    fun degreesToTurnImage(bearingToDestination : Float,
                           bearingCompass : Float) : Int {


        var degreesToTurn = 360f - bearingCompass + bearingToDestination

   //     Log.d(TAG, "bearingToDestination " + bearingToDestination)
     //   Log.d(TAG, "bearingCompass " + bearingCompass)
//        Log.d(TAG, "degreesToTurn " + degreesToTurn)

        if (degreesToTurn <= -360) {
            degreesToTurn = degreesToTurn + 360
        }

        if (degreesToTurn >= 360) {
            degreesToTurn = degreesToTurn - 360
        }

        return degreesToTurn.toInt()
    }
}