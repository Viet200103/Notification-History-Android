package com.notisaver.main.log.main

import android.app.Notification
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.NotificationDataTest
import com.notisaver.database.entities.AppMetaData
import com.notisaver.database.entities.BaseNotificationStyle
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.log.core.LogHelper
import com.notisaver.main.log.core.NotificationHandler
import com.notisaver.main.log.model.StatusNotificationLog
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHandlerTest {

    @Test
    fun test_big_picture_file_name() {
        val logId = "1"

        val fName = NotificationHandler.createBigPictureFileName(logId)

        Assert.assertEquals(fName, "1.picture")
    }

    @Test
    fun test_large_file_name() {
        val logId = "1"

        val fName = NotificationHandler.createLargeIconFileName(logId)

        Assert.assertEquals(fName, "1.icon.large")
    }

    @Test
    fun test_big_large_icon_file_name() {
        val logId = "1"

        val fName = NotificationHandler.createBigLargeIconFileName(logId)

        Assert.assertEquals(fName, "1.icon.blarge")
    }

    @Test
    fun test_notification_normal() {
        runBlocking {
            val packageHashcode = "com.noitsaver.handler.hashcode.test"
            val packageName = "com.notisaver.handler.package.test"

            val notisaveApplication =
                ApplicationProvider.getApplicationContext<NotisaveApplication>()

            notisaveApplication.notisaveRepository.logAppMetaData(
                AppMetaData(packageHashcode, packageName)
            )

            val handler = NotificationHandler(
                logId = "NId.Test",
                notisaveApplication,
                notisaveApplication.notisaveRepository,
                LogHelper.getPackageHashcodeFolder(notisaveApplication, packageHashcode),
                workerScope = this
            )

            val nBuilder = NotificationDataTest.createNotification(notisaveApplication)

            val statusLog = StatusNotificationLog(
                packageName, null, "1", nBuilder.build(), false, -1
            )

            handler.logPosted(statusLog, packageHashcode)

            val nLog = notisaveApplication.notisaveRepository.getNotificationLog("NId.Test")

            Assert.assertNotNull(nLog)

            Assert.assertNull(nLog?.notificationStyle)

            val no = nLog!!.notification

            Assert.assertEquals(
                no.title, "NotificationTestTitle"
            )

            Assert.assertEquals(
                no.contentText, "NotificationTestContent"
            )
        }
    }


    private fun test_notification_big_text(notification: Notification) = runBlocking {
        val packageHashcode = "com.noitsaver.handler.hashcode.test"
        val packageName = "com.notisaver.handler.package.test"

        val notisaveApplication = ApplicationProvider.getApplicationContext<NotisaveApplication>()

        notisaveApplication.notisaveRepository.logAppMetaData(
            AppMetaData(packageHashcode, packageName)
        )

        val handler = NotificationHandler(
            logId = "NId.Test",
            notisaveApplication,
            notisaveApplication.notisaveRepository,
            LogHelper.getPackageHashcodeFolder(notisaveApplication, packageHashcode),
            workerScope = this
        )

        val statusLog = StatusNotificationLog(
            packageName, null, "1", notification, false, -1
        )

        handler.logPosted(statusLog, packageHashcode)

        val nLog = notisaveApplication.notisaveRepository.getNotificationLog("NId.Test")

        Assert.assertNotNull(
            nLog?.notificationStyle
        )

        val style = nLog!!.notificationStyle!!

        val baseStyle = style.baseStyle

        Assert.assertEquals(
            baseStyle.styleId, "NId.Test"
        )

        Assert.assertTrue(
            baseStyle.templateId == BaseNotificationStyle.BIG_TEXT_ID
        )

        Assert.assertEquals(
            baseStyle.summaryText, "NotificationTestBigSummary"
        )

        Assert.assertEquals(
            baseStyle.bigContentTitle, "NotificationTestBigTitle"
        )

        Assert.assertEquals(
            style.bigTextStyle?.bigText, "NotificationTestBigContent"
        )
    }

    @Test
    fun test_notification_big_text_compat() {
        val nBuilder = NotificationDataTest.createNotificationBigTextCompat(
            ApplicationProvider.getApplicationContext()
        )

        test_notification_big_text(nBuilder.build())
    }


    @Test
    fun test_notification_big_picture_compat() {
        runBlocking {
            val packageHashcode = "com.noitsaver.handler.hashcode.test"
            val packageName = "com.notisaver.handler.package.test"

            val notisaveApplication =
                ApplicationProvider.getApplicationContext<NotisaveApplication>()

            notisaveApplication.notisaveRepository.logAppMetaData(
                AppMetaData(packageHashcode, packageName)
            )

            val handler = NotificationHandler(
                logId = "NId.Test",
                notisaveApplication,
                notisaveApplication.notisaveRepository,
                LogHelper.getPackageHashcodeFolder(notisaveApplication, packageHashcode),
                workerScope = this
            )

            val nBuilder = NotificationDataTest.createNotificationBigPictureCompat(notisaveApplication)

            val statusLog = StatusNotificationLog(
                packageName, null, "1", nBuilder.build(), false, -1
            )

            handler.logPosted(statusLog, packageHashcode)

            val nLog = notisaveApplication.notisaveRepository.getNotificationLog("NId.Test")

            val style = nLog!!.notificationStyle

            Assert.assertNotNull(style)

            val baseStyle = style!!.baseStyle

            Assert.assertEquals(
                baseStyle.styleId, "NId.Test"
            )

            Assert.assertTrue(
                baseStyle.templateId == BaseNotificationStyle.BIG_PICTURE_ID
            )

            Assert.assertEquals(
                baseStyle.summaryText, "NotificationTestSummaryPicture"
            )

            Assert.assertEquals(
                baseStyle.bigContentTitle, "NotificationTestBigTitle"
            )

            val bigPictureStyle = nLog.notificationStyle!!.bigPictureStyle!!

            Assert.assertTrue(
                bigPictureStyle.isPicture
            )
        }
    }
}