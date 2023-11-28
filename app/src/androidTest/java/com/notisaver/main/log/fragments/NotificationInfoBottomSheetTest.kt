package com.notisaver.main.log.fragments

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.notisaver.NotificationData
import com.notisaver.R
import com.notisaver.database.NotisaveRepository
import com.notisaver.main.KEY_NOTIFICATION_LOG_ID
import com.notisaver.main.NotisaveApplication
import com.notisaver.utils.hasDrawable
import com.notisaver.utils.setState
import com.notisaver.utils.withDrawable
import com.notisaver.utils.withIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class NotificationInfoBottomSheetTest {

    private var repository: NotisaveRepository? = null

    @Before
    fun onCreate() = runTest {
        ApplicationProvider.getApplicationContext<NotisaveApplication>().let {
            it.deleteDatabase("NotisaveDatabase")
            NotificationData.createSuiteTest1(it, it.notisaveRepository)
            NotificationData.createSuiteTest2(it, it.notisaveRepository)
            repository = it.notisaveRepository
        }
    }

    @Test
    fun check_layout_screen() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.1.notification.empty")

        val scenario = launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        scenario.onFragment {

            val dialog = it.dialog

            ViewMatchers.assertThat(
                dialog, Matchers.notNullValue()
            )
            dialog!!

            ViewMatchers.assertThat(
                dialog.isShowing, Matchers.`is`(true)
            )

            dialog as BottomSheetDialog

            it.lifecycleScope.launch(Dispatchers.IO) {
                Espresso.onView(
                    ViewMatchers.withId(com.google.android.material.R.id.design_bottom_sheet)
                ).perform(
                    setState(
                        BottomSheetBehavior.STATE_EXPANDED
                    )
                )
            }
        }
    }

    @Test
    fun test_app_not_checked() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.1.notification.1")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_clean_notification)
        ).check(
            ViewAssertions.matches(ViewMatchers.isChecked())
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isCleanable, false
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_clean_notification)
        ).check(
            ViewAssertions.matches(ViewMatchers.isNotChecked())
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("Cancel")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isCleanable, false
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_clean_notification)
        ).check(
            ViewAssertions.matches(ViewMatchers.isNotChecked())
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isCleanable, true
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_log_ongoing)
        ).check(
            ViewAssertions.matches(ViewMatchers.isNotChecked())
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("Cancel")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isLogOnGoing, false
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_log_ongoing)
        ).check(
            ViewAssertions.matches(ViewMatchers.isNotChecked())
        ).perform(
            ViewActions.click()
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isLogOnGoing, true
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_log_ongoing)
        ).check(
            ViewAssertions.matches(ViewMatchers.isChecked())
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isLogOnGoing, false
            )
        }
    }

    @Test
    fun test_app_checked() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.2.notification.2")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_clean_notification)
        ).check(
            ViewAssertions.matches(ViewMatchers.isNotChecked())
        )


        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_log_ongoing)
        ).check(
            ViewAssertions.matches(ViewMatchers.isChecked())
        )
    }

    @Test
    fun test_empty_notification() {

        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.1.notification.empty")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_title)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.GONE
                )
            )
        )


        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_large_icon)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.GONE
                )
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_sub_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.GONE
                )
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_content_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.GONE
                )
            )
        )
    }

    @Test
    fun test_normal_notification() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.1.notification.1")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        runBlocking {
            delay(500)
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_small_icon_app_name_view)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("suite.test.1.package.1")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_title)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("NotificationNormal")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_large_icon)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                hasDrawable()
            )
        ).check(
            ViewAssertions.matches(
                Matchers.not(
                    withDrawable(R.drawable.ic_image_not_found)
                )
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_sub_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("Subtext normal notification")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_content_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("This is text normal notification")
            )
        )
    }

    @Test
    fun test_big_text_style() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.2.notification.3")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        runBlocking {
            delay(500)
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_small_icon_app_name_view)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("suite.test.1.package.2")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_title)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("BigText title")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_sub_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("BigText summary")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_content_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("This is big text content")
            )
        )
    }

    @Test
    fun test_big_picture_style() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.1.notification.4")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_small_icon_app_name_view)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("suite.test.1.package.1")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_title)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("BigPicture title")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_sub_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("BigPicture subtext")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_content_text)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withText("BigPicture summary")
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_large_icon)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                hasDrawable()
            )
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.layout_notification_info_big_picture)
        ).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE
                )
            )
        ).check(
            ViewAssertions.matches(
                hasDrawable()
            )
        ).check(
            ViewAssertions.matches(
                Matchers.not(
                    withDrawable(R.drawable.ic_image_not_found)
                )
            )
        )
    }

    @Test
    fun test_app_tracking() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.1.notification.empty")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        runBlocking {
            delay(500)
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_list_option)
        ).perform(
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0)
        )

        Espresso.onView(
            withIndex(
                ViewMatchers.hasDescendant(ViewMatchers.withText(R.string.option_include_app_short)), 0
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_list_option)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click())
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isTracking, true
            )
        }

        Espresso.onView(
            withIndex(
                ViewMatchers.hasDescendant(ViewMatchers.withText(R.string.option_exclude_app_short)), 0
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_list_option)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click())
        )

        Espresso.onView(
            ViewMatchers.withText("Cancel")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isTracking, true
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_list_option)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click())
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertEquals(
                repository?.getAppMetaData("suite.test.1.package.hashcode.1")?.isTracking, false
            )
        }

        Espresso.onView(
            withIndex(
                ViewMatchers.hasDescendant(ViewMatchers.withText(R.string.option_include_app_short)), 0
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }

    @Test
    fun test_remove() {
        val bundle = bundleOf(KEY_NOTIFICATION_LOG_ID to "suite.test.1.package.1.notification.empty")

        launchFragment<NotificationInfoBottomSheet>(
            bundle, R.style.Theme_Notisaver_DayNight
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_list_option)
        ).perform(
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(1)
        )

        Espresso.onView(
            withIndex(
                ViewMatchers.hasDescendant(ViewMatchers.withText(R.string.option_remove_notification_short)), 1
            )
        ).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_list_option)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, ViewActions.click())
        )

        Espresso.onView(
            ViewMatchers.withText("Cancel")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertNotNull(
                repository?.getNotificationLog("suite.test.1.package.1.notification.empty")
            )
        }

        Espresso.onView(
            ViewMatchers.withId(R.id.bottom_sheet_notification_info_list_option)
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, ViewActions.click())
        )

        Espresso.onView(
            ViewMatchers.withText("OK")
        ).inRoot(
            RootMatchers.isDialog()
        ).perform(
            ViewActions.click()
        )

        runBlocking {
            Assert.assertNull(
                repository?.getNotificationLog("suite.test.1.package.1.notification.empty")
            )
        }
    }

    @After
    fun onClose() = runTest {
        repository = null
    }
}