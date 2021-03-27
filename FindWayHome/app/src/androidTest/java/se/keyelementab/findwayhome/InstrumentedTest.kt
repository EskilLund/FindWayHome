package se.keyelementab.findwayhome

import android.content.Context
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
    private lateinit var appContext : Context

//    @get:Rule
//    val rule = activityScenarioRule<MainActivity>()


    @Before
    fun setup() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testFabAbout() {
        val scenario = launchActivity<MainActivity>()
        scenario.onActivity { activity ->
            onView(withId(R.id.fab)).perform(click())
        }
    }
}