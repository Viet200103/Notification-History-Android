package com.notisaver

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.AppMetaData
import com.notisaver.database.entities.ONotification
import com.notisaver.main.log.core.LogHelper
import com.notisaver.main.log.core.NotificationHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


object NotificationDataTest {
    val packageTest = "com.notisaver.package.test"
    val CHANNEL_ID = "com.notisaver.test.chanel_id"
    val GROUP_KEY = "com.notisaver.test.group_key"

    fun createNotification(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("NotificationTestTitle")
            .setContentText("NotificationTestContent")
    }

    fun createNotificationBigTextCompat(context: Context): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("NotificationTestTitle")
            .setContentText("NotificationTestContent")

        val bigText = NotificationCompat.BigTextStyle()

        bigText.setBigContentTitle("NotificationTestBigTitle")
        bigText.setSummaryText("NotificationTestBigSummary")
        bigText.bigText("NotificationTestBigContent")

        builder.setStyle(bigText)

        return builder
    }

    fun createNotificationBigPictureCompat(context: Context): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("NotificationTestTitle")
            .setContentText("NotificationTestContent")

        val bigPicture = NotificationCompat.BigPictureStyle()

        val icon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_app
        )

        bigPicture.setBigContentTitle("NotificationTestBigTitle")
        bigPicture.setContentDescription("NotificationTestDescriptionPicture")
        bigPicture.setSummaryText("NotificationTestSummaryPicture")
        bigPicture.bigPicture(icon)

        builder.setStyle(bigPicture)

        return builder
    }

    fun createAppMetaData(
        packageHashCode: String,
        packageName: String,
        isCleanable: Boolean = false,
        isTracking: Boolean = true,
        isLogOnGoing: Boolean = false
    ): AppMetaData {
        return AppMetaData(
            packageHashCode, packageName, isCleanable, isTracking, isLogOnGoing
        )
    }

    suspend fun createSuiteTest(context: Context, repository: NotisaveRepository) {
        val package1 = "suite.test.1.package.1"
        val hashcode1 = "suite.test.1.package.hashcode.1"

        repository.logAppMetaData(
            AppMetaData(
                packageHashcode = hashcode1,
                packageName = package1,
                isCleanable = false,
                isTracking = false,
                isLogOnGoing = false
            )
        )

        repository.logNotification(
            ONotification(
                logId = "suite.test.1.package.1.notification.empty",
                packageHashcode = hashcode1,
                sbnKeyHashcode = "suite.test.1.sbnkey.empty",
            )
        )
        /////////// normal notification /////////////////
        repository.logNotification(
            ONotification(
                logId = "suite.test.1.package.1.notification.1",
                packageHashcode = hashcode1,
                sbnKeyHashcode = "suite.test.1.package.1.sbnkey.1",
                timePost = 1,
                timeAdded = 1,
                title = "NotificationNormal",
                contentText = "This is text normal notification",
                subText = "Subtext normal notification",
                isLargeIcon = true
            )
        )

        repository.logNotification(
            ONotification(
                logId = "suite.test.1.package.1.notification.2",
                packageHashcode = hashcode1,
                sbnKeyHashcode = "suite.test.1.package.1.sbnkey.1",
                timePost = 2,
                timeAdded = 2,
                title = "NotificationNormal2",
                contentText = "This is text normal notification2",
                subText = "Subtext normal notification2",
                isLargeIcon = true
            )
        )

        repository.logNotification(
            ONotification(
                logId = "suite.test.1.package.1.notification.3",
                packageHashcode = hashcode1,
                sbnKeyHashcode = "suite.test.1.package.1.sbnkey.1",
                timePost = 3,
                timeAdded = 3,
                title = "NotificationNormal3",
                contentText = "This is text normal notification3",
                subText = "Subtext normal notification3",
                isLargeIcon = true
            )
        )

        withContext(Dispatchers.IO) {
            File(
                LogHelper.getPackageHashcodeFolder(context, hashcode1),
                NotificationHandler.createLargeIconFileName("suite.test.1.package.1.notification.1")
            ).createNewFile()

            File(
                LogHelper.getPackageHashcodeFolder(context, hashcode1),
                NotificationHandler.createBigLargeIconFileName("suite.test.1.package.1.notification.1")
            ).createNewFile()

            File(
                LogHelper.getPackageHashcodeFolder(context, hashcode1),
                NotificationHandler.createBigPictureFileName("suite.test.1.package.1.notification.1")
            ).createNewFile()
        }
    }

}
