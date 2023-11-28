package com.notisaver.main.log.main

import android.app.Notification
import android.service.notification.NotificationListenerService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.NotificationDataTest
import com.notisaver.database.entities.AppMetaData
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.log.core.NotisaveListenerService
import com.notisaver.main.log.model.StatusNotificationLog
import com.notisaver.misc.createPackageHashcode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment.application

@RunWith(AndroidJUnit4::class)
class NotisaveListenerServiceTest {

    private val packageName = "com.notisaver.handler.package.test"
    private val packageHashcode =  createPackageHashcode(packageName)

    private lateinit var notification: Notification
    private lateinit var notisaveApplication: NotisaveApplication

    private lateinit var listenerService: NotisaveListenerService
    private lateinit var statusLog: StatusNotificationLog

    @Before
    fun onCreate() {
        notisaveApplication = ApplicationProvider.getApplicationContext()
        notification = NotificationDataTest.createNotification(notisaveApplication).setOngoing(true).build()
        statusLog = StatusNotificationLog(
            packageName = packageName,
            overrideGroupKey = null,
            key = "key",
            notification = notification,
            isOngoing = true,
            postTime = -1
        )
        listenerService = NotisaveListenerService()
        listenerService.notisaveApplication = notisaveApplication
    }

    @Test
    fun test_log_normal() {
        runBlocking {
            listenerService.onNotificationPosted(statusLog)
            delay(1000)
        }

        runBlocking {
            notisaveApplication.notisaveRepository.getNCountOfPackage(packageHashcode).take(1).collect {
                Assert.assertTrue(it == 1)
            }
        }
    }

    @Test
    fun test_log_deny_ongoing() {
        runBlocking {
            notisaveApplication.notisaveRepository.logAppMetaData(
                AppMetaData(packageHashcode = packageHashcode, packageName = packageName, isLogOnGoing = false)
            )

            listenerService.onNotificationPosted(statusLog)
            delay(2000)
        }

        runBlocking {
            notisaveApplication.notisaveRepository.getNCountOfPackage(packageHashcode).take(1).collect {
                Assert.assertTrue(it == 0)
            }
        }
    }

    @Test
    fun test_log_deny_tracking() {

        runBlocking {
            notisaveApplication.notisaveRepository.logAppMetaData(
                AppMetaData(packageHashcode = packageHashcode, packageName = packageName, isTracking = false)
            )

            listenerService.onNotificationPosted(statusLog)
            delay(1000)
        }

        runBlocking {
            notisaveApplication.notisaveRepository.getNCountOfPackage(packageHashcode).take(1).collect {
                Assert.assertTrue(it == 0)
            }
        }
    }
}