/**
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <eckelundgren@gmail.com> wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Eskil
 */
package se.eskil.findwayhome

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep


/**
 * Instrumented test, testing {@link MainActivity}.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private lateinit var appContext : Context

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
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testFirstStart() {
        // Clear everything in the SharedPreferences
        // Clear everything in the SharedPreferences
//        val sharedPreferences: SharedPreferences = appContext.getSharedPreferences(KEY_SP_PACKAGE, Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.clear()
//        editor.commit()

        val scenario = launchActivity<MainActivity>()
        //scenario.moveToState(Lifecycle.State.CREATED)
        //  scenario.onActivity { activity ->
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.fabAbout)).perform(click())
        onView(withId(R.id.okayButton)).perform(click())

        //  }
    }


    @Test
    fun testFabAbout() {
        val scenario = launchActivity<MainActivity>()
        //scenario.moveToState(Lifecycle.State.CREATED)
      //  scenario.onActivity { activity ->
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.fabAbout)).perform(click())
        onView(withId(R.id.okayButton)).perform(click())

        //  }
    }

    @Test
    fun testFabSetDestination() {
        val scenario = launchActivity<MainActivity>()
        //scenario.moveToState(Lifecycle.State.CREATED)
        //  scenario.onActivity { activity ->
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.fabSetDestination)).perform(click())
        sleep(7000)
        onView(withId(R.id.distanceTextView)).check(matches(isDisplayed()))
    }
}