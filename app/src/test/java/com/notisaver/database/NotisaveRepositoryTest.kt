package com.notisaver.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.NotificationDataTest
import com.notisaver.database.entities.NStatusBarGroup
import com.notisaver.database.entities.NotificationLog
import com.notisaver.database.entities.ONotification
import com.notisaver.main.log.core.LogHelper
import com.notisaver.misc.asNotisaveApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config
@RunWith(AndroidJUnit4::class)
class NotisaveRepositoryTest {

    @Test
    fun test_delete_status_bar_group() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db: NotisaveDatabase = buildDatabase()
        val repository = NotisaveRepository(
            context, db
        )

        createSuitTest(context, repository)

        val nStatusBarGroup = NStatusBarGroup(
            header = NotificationLog(
                notification = ONotification(
                    logId = "suite.test.1.package.1.notification.1",
                    packageHashcode = "suite.test.1.package.hashcode.1",
                    sbnKeyHashcode = "suite.test.1.package.1.sbnkey.1",
                    timeAdded = 1,
                    timePost = 1
                )
            ),
            nCount = 0
        )

        runBlocking {
            repository.deleteNotifications(
                listOf(nStatusBarGroup), ""
            )

            Assert.assertNull(
                repository.getNotificationLog("suite.test.1.package.1.notification.1")
            )
        }

        runBlocking {
            val packageDir = LogHelper.getPackageHashcodeFolder(context, "suite.test.1.package.hashcode.1")

            Assert.assertEquals(
                packageDir.listFiles()?.size, 0
            )
        }

        runBlocking {
            Assert.assertEquals(
                repository.getCountNotification(), 3
            )
        }

        db.close()
    }

    @Test
    fun test_delete_notifications() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db: NotisaveDatabase = buildDatabase()

        val repository = NotisaveRepository(
            context, db
        )

        createSuitTest(context, repository)

        runBlocking {
            repository.clearNotification("suite.test.1.package.hashcode.1")
        }

        runBlocking {
            Assert.assertEquals(
                repository.getCountNotification(), 0
            )
        }

        runBlocking {
            val packageDir = LogHelper.getPackageHashcodeFolder(context, "suite.test.1.package.hashcode.1")

            Assert.assertEquals(
                packageDir.listFiles()?.size, 0
            )
        }

        db.close()
    }

    private fun buildDatabase() = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), NotisaveDatabase::class.java
    ).build()

    private fun createSuitTest(context: Context, repository: NotisaveRepository) {
        runBlocking {
            NotificationDataTest.createSuiteTest(
                context, repository
            )

            val packageDir = LogHelper.getPackageHashcodeFolder(context, "suite.test.1.package.hashcode.1")

            Assert.assertEquals(
                packageDir.listFiles()?.size, 3
            )
        }
    }
}
