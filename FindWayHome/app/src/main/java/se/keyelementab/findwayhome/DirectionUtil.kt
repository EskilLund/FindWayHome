package se.keyelementab.findwayhome

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
}