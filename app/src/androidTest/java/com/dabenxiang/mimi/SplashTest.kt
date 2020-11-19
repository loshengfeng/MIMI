package com.dabenxiang.mimi

import android.content.Context
import android.content.res.Resources
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.dabenxiang.mimi.view.main.MainActivity
import com.dabenxiang.mimi.view.splash.SplashFragment
import com.dabenxiang.mimi.view.splash.SplashViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class ExampleInstrumentedTest {

    private val fragmentViewModel: SplashViewModel = mockk(relaxed = true)
    private val fragment = SplashFragment()

    @get: Rule
    val rule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @get:Rule
    val fragmentRule = createRule(fragment, module {
        single(override = true) {
            fragmentViewModel
        }
    })


    @Test
    fun testFragmentsShow(){
        var resources: Resources? = null
        var context: Context? = null
        rule.scenario.onActivity {
            resources = it.resources
            context =it.applicationContext

        }

        verify {

        }

//        Espresso.onView(withId(R.id.action_ad
//                .perform(ViewActions.click())
//
//        Espresso.onView(withId(R.id.add_container)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }



    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.dabenxiang.mimi", appContext.packageName)
    }
}
