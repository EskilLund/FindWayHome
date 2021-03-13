package se.keyelementab.findwayhome

import org.junit.Assert
import org.junit.Test

class DirectionManagerTest {
    private val directionManager by lazy { DirectionManager() }

    @Test
    fun facingWestDestinationNorth() {
        val bearingCToDestinationNorth = 0f // ranging from -180 to 180
        val bearingCompassFacingWest = 270f // ranging from 0 to 360
        Assert.assertEquals(90, directionManager.degreesToTurnImage(bearingCToDestinationNorth,
            bearingCompassFacingWest))
    }
}