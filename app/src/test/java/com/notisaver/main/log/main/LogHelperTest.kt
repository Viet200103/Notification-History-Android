package com.notisaver.main.log.main

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.NotificationDataTest
import com.notisaver.R
import com.notisaver.database.entities.ONotification
import com.notisaver.main.log.core.NotificationHandler
import com.notisaver.main.log.core.LogHelper
import com.notisaver.main.log.model.StatusNotificationLog
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
internal class LogHelperTest {
    private val packageHashcode = "com.notisaver.test"
    private val logId = "com.notisaver.log.id"

    @Test
    fun test_get_package_folder() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val dir = LogHelper.getPackageHashcodeFolder(context, packageHashcode)

        Assert.assertEquals(dir.name, "app_${packageHashcode}")
    }

    @Test
    fun test_delete_notification_package_file() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageFolder = LogHelper.getPackageHashcodeFolder(context, packageHashcode)

        val file1 = File(packageFolder, NotificationHandler.createBigLargeIconFileName(logId))
        val file2 = File(packageFolder, NotificationHandler.createBigPictureFileName(logId))
        val file3 = File(packageFolder, NotificationHandler.createLargeIconFileName(logId))

        file1.createNewFile()
        file2.createNewFile()
        file3.createNewFile()

        LogHelper.deleteFileOfNotification(packageFolder, logId)

        Assert.assertEquals(packageFolder.listFiles()?.size, 0)
    }

    @Test
    fun test_delete_notification_package_file_2() {
        val oNotification = ONotification(
            "delete.package.hashcode",
            logId = logId,
            packageHashcode = packageHashcode
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageFolder = LogHelper.getPackageHashcodeFolder(context, packageHashcode)

        val file1 = File(packageFolder, NotificationHandler.createBigLargeIconFileName(logId))
        val file2 = File(packageFolder, NotificationHandler.createBigPictureFileName(logId))
        val file3 = File(packageFolder, NotificationHandler.createLargeIconFileName(logId))

        file1.createNewFile()
        file2.createNewFile()
        file3.createNewFile()

        LogHelper.deleteFileOfNotification(context, oNotification)

        Assert.assertEquals(packageFolder.listFiles()?.size, 0)
    }

    @Test
    fun test_delete_package_folder() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val packageFolder = LogHelper.getPackageHashcodeFolder(context, packageHashcode)

        val file1 = File(packageFolder, NotificationHandler.createBigLargeIconFileName(logId))
        val file2 = File(packageFolder, NotificationHandler.createBigPictureFileName(logId))
        val file3 = File(packageFolder, NotificationHandler.createLargeIconFileName(logId))

        file1.createNewFile()
        file2.createNewFile()
        file3.createNewFile()

        LogHelper.deletePackageFolder(context, packageHashcode)

        Assert.assertEquals(packageFolder.exists(), false)
    }

    fun test_is_not_group() {
        val logHelper = LogHelper()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val nGroup = NotificationCompat.Builder(context, NotificationDataTest.CHANNEL_ID)
            .setContentTitle("Notficiation Group")
            .setContentText("this is a group")
            .setSmallIcon(R.drawable.ic_app)
            .setGroup(NotificationDataTest.GROUP_KEY)
            .setGroupSummary(false)
            .build()

        val isGroup = logHelper.isGroup(
            StatusNotificationLog(
                NotificationDataTest.packageTest, "override_key", "normal key", nGroup, false, -1
            )
        )

        Assert.assertEquals(isGroup, false)
    }

    @Test
    fun test_is_group() {
        val logHelper = LogHelper()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val nGroup = NotificationCompat.Builder(context, NotificationDataTest.CHANNEL_ID)
            .setContentTitle("Notficiation Group")
            .setContentText("this is a group")
            .setGroup(NotificationDataTest.GROUP_KEY)
            .setSmallIcon(R.drawable.ic_app)
            .setGroupSummary(true)
            .build()

        val isGroup = logHelper.isGroup(
            StatusNotificationLog(
                NotificationDataTest.packageTest, null, "normal key", nGroup, false, -1
            )
        )

        Assert.assertEquals(isGroup, true)
    }

    @Test
    fun test_is_group_override_key() {
        val logHelper = LogHelper()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val nGroup = NotificationCompat.Builder(context, NotificationDataTest.CHANNEL_ID)
            .setContentTitle("Notficiation Group")
            .setContentText("this is a group")
            .setSmallIcon(R.drawable.ic_app)
            .setGroupSummary(true)
            .build()

        val isGroup = logHelper.isGroup(
            StatusNotificationLog(
                NotificationDataTest.packageTest, "override_key", "normal key", nGroup, false, -1
            )
        )

        Assert.assertEquals(isGroup, true)
    }

    @Test
    fun test_is_group_no_key() {
        val logHelper = LogHelper()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val nGroup = NotificationCompat.Builder(context, NotificationDataTest.CHANNEL_ID)
            .setContentTitle("Notficiation Group")
            .setContentText("this is a group")
            .setSmallIcon(R.drawable.ic_app)
            .setGroupSummary(true)
            .build()

        val isGroup = logHelper.isGroup(
            StatusNotificationLog(
                NotificationDataTest.packageTest, null, "normal key", nGroup, false, -1
            )
        )

        Assert.assertEquals(isGroup, false)
    }
}