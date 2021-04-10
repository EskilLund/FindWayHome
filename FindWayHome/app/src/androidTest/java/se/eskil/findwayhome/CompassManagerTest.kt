/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import se.eskil.findwayhome.CompassManager.CompassListener
import java.util.concurrent.CountDownLatch


/**
 * Instrumented test, testing {@link CompassManager}.
 */
@RunWith(AndroidJUnit4::class)
class CompassManagerTest {
    private lateinit var appContext : Context
    private lateinit var compassManager : CompassManager

    /**
     * Rule to grant permission when needed.
     */
    @Rule @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

//    @get:Rule
//    val rule = activityScenarioRule<MainActivity>()


    @Before
    fun setup() {
        // Context of the app under test.
        appContext = getInstrumentation().targetContext
        compassManager = CompassManager()
    }

    @Test
    fun testStartAndStop() {
        val listener = TestCompassListener()
        compassManager.startCompassManager(appContext, listener)
        listener.latch.await()
        compassManager.stopCompassManager()
    }

    class TestCompassListener : CompassListener {
        val latch = CountDownLatch(1)
        override fun onCompassHeading(heading: Float) {
            latch.countDown()
        }

        override fun onCompassSensorsNotExisting() {
            latch.countDown()
        }
    }
}