package com.notisaver.main.start

import android.content.SharedPreferences
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.R
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.NotisaveSetting
import com.notisaver.utils.DrawableMatcher
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
internal class PolicyFragmentTest {

    private lateinit var application: NotisaveApplication
    private lateinit var defaultPreferences: SharedPreferences

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(application)
    }


    @Test
    fun check_all() {
        launchFragmentInContainer<WelcomeFragment>(
            themeResId = R.style.Theme_Notisaver_DayNight
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_policy_icon_view)
        ).check(
            ViewAssertions.matches(
                DrawableMatcher(R.drawable.ic_app)
            )
        )

        Espresso.onView(
            ViewMatchers.withText(
                application.getString(R.string.welcome_to, application.getString(R.string.app_name))
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.isDisplayed()
            )
        )

        val getString: (Int) -> String = application.resources::getString

        val summary = StringBuilder(getString(R.string.usage_policy_summary))

        val policyStr = getString(R.string.privacy_policy)
        val termOfServiceStr = getString(R.string.terms_of_service)

        var index = summary.indexOf("**")

        summary.replace(index, index + 2, policyStr)

        index = summary.indexOf("**")
        summary.replace(index, index + 2, termOfServiceStr)

        Espresso.onView(
            ViewMatchers.withText(summary.toString())
        ).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_policy_accept_button)
        ).perform(
            ViewActions.click()
        )

        Thread.sleep(100)

        ViewMatchers.assertThat(
            defaultPreferences.getBoolean(
                NotisaveSetting.PRE_USAGE_POLICY, false
            ), Matchers.`is`(true)
        )
    }

    @After
    fun clearData() {
        PreferenceManager.getDefaultSharedPreferences(
            application
        ).edit().clear().commit()
    }
}