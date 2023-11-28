package com.notisaver.main.log.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.NotificationData
import com.notisaver.main.BUNDLE_KEY
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.PACKAGE_HASHCODE_KEY
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.utils.RecyclerViewItemCountAssertion
import kotlinx.coroutines.delay
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
class NotificationPackageActivityTest {

    private fun createLauncherTest1() {


        val notisaveApplication = ApplicationProvider.getApplicationContext<NotisaveApplication>()

        runBlocking {
            notisaveApplication.getAppInfoManager().setAppInfo(
                AppInformation(
                    NotificationData.getAppMetaSuite2(),
                    "AppInfoTest"
                )
            )

            NotificationData.createSuiteTest2(
                notisaveApplication, notisaveApplication.notisaveRepository
            )
        }

        val bundle = Bundle()

        bundle.putString(PACKAGE_HASHCODE_KEY, "suite.test.1.package.hashcode.2")

        val intent = Intent(notisaveApplication, NotificationPackageActivity::class.java).apply {
            putExtra(BUNDLE_KEY, bundle)
        }

        launchActivity<NotificationPackageActivity>(intent)
    }

    @Test
    fun test_token_status_normal() {
        createLauncherTest1()

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_problem)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_exclude_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_cleanable_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
        )
    }

    @Test
    fun test_token_status_ignore_feature() {
        val notisaveApplication = ApplicationProvider.getApplicationContext<NotisaveApplication>()

        runBlocking {
            notisaveApplication.deleteDatabase("NotisaveDatabase")

            notisaveApplication.getAppInfoManager().setAppInfo(
                AppInformation(
                    NotificationData.getAppMetaSuite1(),
                    "AppInfoTest"
                )
            )

            NotificationData.createSuiteTest1(
                notisaveApplication, notisaveApplication.notisaveRepository
            )
        }

        val bundle = Bundle()

        bundle.putString(PACKAGE_HASHCODE_KEY, "suite.test.1.package.hashcode.1")

        val intent = Intent(notisaveApplication, NotificationPackageActivity::class.java).apply {
            putExtra(BUNDLE_KEY, bundle)
        }

        launchActivity<NotificationPackageActivity>(intent)

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_problem)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_exclude_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_cleanable_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )
    }

    private fun openMenu() {
        Espresso.onView(
            ViewMatchers.withId(R.id.activity_notification_list_app_support_menu_view)
        ).perform(ViewActions.click())
    }
    @Test
    fun test_clear_notification() {
        createLauncherTest1()

        openMenu()

        Espresso.onView(
            ViewMatchers.withText(R.string.option_remove_notification)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("Cancel")
        ).inRoot(RootMatchers.isDialog()).perform(ViewActions.click())

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(1)
        )

        openMenu()

        Espresso.onView(
            ViewMatchers.withText(R.string.option_remove_notification)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).inRoot(RootMatchers.isDialog()).perform(ViewActions.click())

        runBlocking {
            delay(500)
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(0)
        )
    }

    @Test
    fun test_tracking_app() {
        createLauncherTest1()
        val repository = ApplicationProvider.getApplicationContext<NotisaveApplication>().notisaveRepository

        openMenu()
        Espresso.onView(
            ViewMatchers.withText(R.string.option_exclude_app_short)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository.getAppMetaData("suite.test.1.package.hashcode.2")?.isTracking, false
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_exclude_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )

        openMenu()

        Espresso.onView(
            ViewMatchers.withText(R.string.option_include_app_short)
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository.getAppMetaData("suite.test.1.package.hashcode.2")?.isTracking, true
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_detail_notification_app_token_exclude_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
        )
    }

    @Test
    fun test_log_ongoing() {
        createLauncherTest1()
        openMenu()

        val repository = ApplicationProvider.getApplicationContext<NotisaveApplication>().notisaveRepository

        Espresso.onView(
            ViewMatchers.withText(R.string.option_disable_log_ongoing)
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository.getAppMetaData("suite.test.1.package.hashcode.2")?.isLogOnGoing, false
            )
        }

        openMenu()

        Espresso.onView(
            ViewMatchers.withText(R.string.option_enable_log_ongoing)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository.getAppMetaData("suite.test.1.package.hashcode.2")?.isLogOnGoing, true
            )
        }
    }

    @Test
    fun test_search_notification() {
        createLauncherTest1()

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_notification_list_app_search_mode_view)
        ).perform(ViewActions.click())

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_notification_list_app_sub_header)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_notification_list_app_enter_search_view)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        ).perform(
            ViewActions.typeTextIntoFocusedView("NotificationNormal")
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(1)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click())
        )

        runBlocking {
            delay(500)
        }

        Espresso.onView(
            ViewMatchers.withText("NotificationNormal 2")
        ).inRoot(RootMatchers.isDialog()).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }

    @Test
    fun test_select_notification() {
        createLauncherTest1()

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(1)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.longClick())
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_notification_list_sub_header_container_layout)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_container_layout)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_count_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withText("Selected 1"))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(1)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click())
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_count_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withText("Selected 0"))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(1)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click())
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_delete_button)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("Cancel")
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(1)
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_delete_button)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.fragment_notification_list_recycler_view)
        ).check(
            RecyclerViewItemCountAssertion(0)
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_count_view)
        ).check(
            ViewAssertions.matches(ViewMatchers.withText("Selected 0"))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_close_button)
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.activity_notification_list_sub_header_container_layout)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.selection_bar_container_layout)
        ).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE))
        )
    }
}