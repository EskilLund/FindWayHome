package se.eskil.findwayhome

import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionUtilTest {
    private val floatDelta = 1.0f
    private val directionUtil = DirectionUtil()

    @Test
    fun facingWestDestinationNorth() {
        val bearingToDestinationNorth = 0f // ranging from -180 to 180
        val bearingCompassFacingWest = 270f // ranging from 0 to 360

        assertEquals(90f, directionUtil.degreesToTurnImage(bearingToDestinationNorth,
            bearingCompassFacingWest), floatDelta)
    }

    @Test
    fun facingNorthDestinationNorth() {
        val bearingToDestinationNorth = 0f // ranging from -180 to 180
        val bearingCompassFacingNorth = 0f // ranging from 0 to 360

        assertEquals(0f, directionUtil.degreesToTurnImage(bearingToDestinationNorth,
            bearingCompassFacingNorth), floatDelta)
    }

    @Test
    fun facingNorthDestinationSouth() {
        val bearingToDestinationSouth = 180f // ranging from -180 to 180
        val bearingCompassFacingNorth = 0f // ranging from 0 to 360

        assertEquals(180f, directionUtil.degreesToTurnImage(bearingToDestinationSouth,
            bearingCompassFacingNorth), floatDelta)
    }
}