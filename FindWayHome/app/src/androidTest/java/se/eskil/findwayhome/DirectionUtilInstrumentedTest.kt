/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DirectionUtilInstrumentedTest {
    private lateinit var appContext : Context
    private val directionUtil = DirectionUtil()

    @Before
    fun setup() {
        appContext = getInstrumentation().targetContext
    }

    @Test
    fun testDistanceString0Meters() {
        val meters = 0f
        assertEquals("Distance: 0 meters", directionUtil.getDistanceString(meters, appContext))
    }

    @Test
    fun testDistanceString70Meters() {
        val meters = 70f
        assertEquals("Distance: 70 meters", directionUtil.getDistanceString(meters, appContext))
    }

    @Test
    fun testDistanceString1000Meters() {
        val meters = 1000f
        assertEquals("Distance: 1.0 kilometers", directionUtil.getDistanceString(meters, appContext))
    }

    @Test
    fun testDistanceString1234Meters() {
        val meters = 1234f
        assertEquals("Distance: 1.2 kilometers", directionUtil.getDistanceString(meters, appContext))
    }

    @Test
    fun testDistanceString1249Meters() {
        val meters = 1249f
        assertEquals("Distance: 1.2 kilometers", directionUtil.getDistanceString(meters, appContext))
    }

    @Test
    fun testDistanceString1250Meters() {
        val meters = 1250f
        assertEquals("Distance: 1.3 kilometers", directionUtil.getDistanceString(meters, appContext))
    }

    @Test
    fun testDistanceString1299Meters() {
        val meters = 1299f
        assertEquals("Distance: 1.3 kilometers", directionUtil.getDistanceString(meters, appContext))
    }

    @Test
    fun testDistanceString10000Meters() {
        val meters = 10000f
        assertEquals("Distance: 10.0 kilometers", directionUtil.getDistanceString(meters, appContext))
    }
}