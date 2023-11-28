package com.notisaver.main.settings

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.R
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.NotisaveSetting
import com.notisaver.utils.atPosition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
internal class OtherActivityTest {

    @get:Rule
    val activityScenario = ActivityScenarioRule<SettingActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(), SettingActivity::class.java
        ).apply {
            putExtra(NotisaveSetting.ACTIVITY_ACTION, NotisaveSetting.ACTION_SETTING)
        }
    )

    private lateinit var application: NotisaveApplication

    @Before
    fun setup() {

        application = ApplicationProvider.getApplicationContext()
    }

//    @Test
//    fun check_setting_layout() {
//        Espresso.onView(
//            allOf(
//                ViewMatchers.withText(R.string.setting),
//                ViewMatchers.isDescendantOfA(
//                    ViewMatchers.withId(R.id.activity_other_toolbar)
//                )
//            )
//        ).check(
//            ViewAssertions.matches(
//                ViewMatchers.isDisplayed()
//            )
//        )
//
//        Espresso.onView(
//            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
//        ).check(
//            ViewAssertions.matches(ViewMatchers.isDisplayed())
//        )
//
//        Espresso.onView(
//            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
//        ).check(
//            ViewAssertions.matches(
//                atPosition(
//                    0,
//                    ViewMatchers.hasDescendant(
//                        ViewMatchers.withText(R.string.pre_general)
//                    )
//                )
//            )
//        )
//
//        Espresso.onView(
//            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
//        ).check(
//            ViewAssertions.matches(
//                atPosition(
//                    2,
//                    allOf(
//                        ViewMatchers.hasDescendant(
//                            ViewMatchers.withText(R.string.pre_general_language)
//                        ),
//                        ViewMatchers.hasDescendant(
//                            ViewMatchers.withText(R.string.default_language_system)
//                        )
//                    )
//                )
//            )
//        )
//
//        Espresso.onView(
//            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
//        ).check(
//            ViewAssertions.matches(
//                atPosition(
//                    4,
//                    ViewMatchers.hasDescendant(
//                        ViewMatchers.withText(R.string.pre_notification_log)
//                    )
//                )
//            )
//        )
//
//        Espresso.onView(
//            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
//        ).check(
//            ViewAssertions.matches(
//                atPosition(
//                    5,
//                    allOf(
//                        ViewMatchers.hasDescendant(
//                            ViewMatchers.withText(R.string.pre_listener_status)
//                        ),
//                        ViewMatchers.hasDescendant(
//                            ViewMatchers.withText(R.string.pre_listener_status_disabled)
//                        )
//                    )
//                )
//            )
//        )
//
//
//        Espresso.onView(
//            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
//        ).check(
//            ViewAssertions.matches(
//                atPosition(
//                    8,
//                    allOf(
//                        ViewMatchers.hasDescendant(
//                            ViewMatchers.withText(R.string.pre_log_ongoing)
//                        ),
//                        ViewMatchers.hasDescendant(
//                            ViewMatchers.withText(R.string.pre_log_ongoing_summary)
//                        )
//                    )
//                )
//            )
//        )
//    }

    @Test
    fun check_language_setting() {

        Thread.sleep(200)

        Espresso.onView(
            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                2,
                ViewActions.click()
            )
        )

        Thread.sleep(200)

        Espresso.onData(
            CoreMatchers.anything()
        ).inAdapterView(
            ViewMatchers.withId(
                com.google.android.material.R.id.select_dialog_listview
            )
        ).atPosition(0).check(
            ViewAssertions.matches(ViewMatchers.withText(R.string.default_language_system))
        )

        Espresso.onData(
            CoreMatchers.anything()
        ).inAdapterView(
            ViewMatchers.withId(
                com.google.android.material.R.id.select_dialog_listview
            )
        ).atPosition(1).check(
            ViewAssertions.matches(ViewMatchers.withText("English"))
        )

        Espresso.onData(
            CoreMatchers.anything()
        ).inAdapterView(
            ViewMatchers.withId(
                com.google.android.material.R.id.select_dialog_listview
            )
        ).atPosition(2).check(
            ViewAssertions.matches(ViewMatchers.withText("Tiếng Việt"))
        ).perform(ViewActions.click())

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withId(androidx.preference.R.id.recycler_view)
        ).check(
            ViewAssertions.matches(
                allOf(
                    ViewMatchers.hasDescendant(ViewMatchers.withText("Ngôn ngữ")),
                    ViewMatchers.hasDescendant(ViewMatchers.withText("Tiếng Việt"))
                )
            )
        )
    }

}