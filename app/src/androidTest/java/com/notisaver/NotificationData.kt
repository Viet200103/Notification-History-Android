package com.notisaver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.AppMetaData
import com.notisaver.database.entities.BaseNotificationStyle
import com.notisaver.database.entities.BigPictureNotificationStyle
import com.notisaver.database.entities.BigTextNotificationStyle
import com.notisaver.database.entities.ONotification
import com.notisaver.main.log.core.LogHelper
import com.notisaver.main.log.core.NotificationHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.NullPointerException


object NotificationData {

    internal fun getAppMetaSuite1() = AppMetaData(
        packageHashcode = "suite.test.1.package.hashcode.1",
        packageName = "suite.test.1.package.1",
        isCleanable = true,
        isTracking = false,
        isLogOnGoing = false
    )

    internal fun getAppMetaSuite2() = AppMetaData(
        packageHashcode = "suite.test.1.package.hashcode.2",
        packageName = "suite.test.1.package.2",
        isCleanable = false,
        isTracking = true,
        isLogOnGoing = true
    )

    suspend fun createSuiteTest1(context: Context, repository: NotisaveRepository) {
        val hashcode1 = "suite.test.1.package.hashcode.1"

        repository.logAppMetaData(getAppMetaSuite1())

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
        kotlin.run {
            val packageDir = LogHelper.getPackageHashcodeFolder(context, hashcode1)
            if (!packageDir.exists()) {
                packageDir.mkdirs()
            }

            saveBitmapToFile(
                packageDir,
                fileName = NotificationHandler.createLargeIconFileName("suite.test.1.package.1.notification.1"),
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_app),
            )
        }


        //////////////////////big picture///////////////////////////////
        repository.logNotification(
            ONotification(
                logId = "suite.test.1.package.1.notification.4",
                packageHashcode = hashcode1,
                sbnKeyHashcode = "suite.test.1.package.1.sbnkey.1",
                timePost = 4,
                timeAdded = 4,
                title = "NotificationBigPicture",
                contentText = "This is normal content",
                subText = "BigPicture subtext",
            )
        )

        repository.logBaseNotificationStyle(
            BaseNotificationStyle(
                styleId = "suite.test.1.package.1.notification.4",
                bigContentTitle = "BigPicture title",
                summaryText = "BigPicture summary",
                BaseNotificationStyle.BIG_PICTURE_ID
            )
        )

        repository.logBigPictureStyle(
            BigPictureNotificationStyle(
                styleId = "suite.test.1.package.1.notification.4",
                isBigLargeIcon = true,
                isPicture = true
            )
        )

        kotlin.run {
            val packageDir = LogHelper.getPackageHashcodeFolder(context, hashcode1)
            if (!packageDir.exists()) {
                packageDir.mkdirs()
            }

            saveBitmapToFile(
                packageDir,
                fileName = NotificationHandler.createLargeIconFileName("suite.test.1.package.1.notification.4"),
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_app),
            )

            saveBitmapToFile(
                packageDir,
                fileName = NotificationHandler.createBigPictureFileName("suite.test.1.package.1.notification.4"),
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_app),
            )
        }

    }

    suspend fun createSuiteTest2(context: Context, repository: NotisaveRepository) {
        val hashcode2 = "suite.test.1.package.hashcode.2"

        repository.logAppMetaData(getAppMetaSuite2())

        /////////////////////////////////////////////////////////

        repository.logNotification(
            ONotification(
                logId = "suite.test.1.package.2.notification.2",
                packageHashcode = hashcode2,
                sbnKeyHashcode = "suite.test.1.package.2.sbnkey.1",
                timePost = 2,
                timeAdded = 2,
                title = "NotificationNormal 2",
                contentText = "This is text normal notification 2",
                subText = "Subtext normal notification 2"
            )
        )
        //////////////// big text /////////////////////////////
        repository.logNotification(
            ONotification(
                logId = "suite.test.1.package.2.notification.3",
                packageHashcode = hashcode2,
                sbnKeyHashcode = "suite.test.1.package.2.sbnkey.1",
                timePost = 3,
                timeAdded = 3,
                title = "NotificationBigText",
                contentText = "This is normal content",
                subText = "BigText subtext",
            )
        )

        repository.logBaseNotificationStyle(
            BaseNotificationStyle(
                styleId = "suite.test.1.package.2.notification.3",
                bigContentTitle = "BigText title",
                summaryText = "BigText summary",
                BaseNotificationStyle.BIG_TEXT_ID
            )
        )

        repository.logBigTextStyle(
            BigTextNotificationStyle(
                styleId = "suite.test.1.package.2.notification.3",
                bigText = "This is big text content"
            )
        )
    }

    private fun saveBitmapToFile(
        dir: File?,
        fileName: String,
        bm: Bitmap?,
    ): Boolean {
        val imageFile = File(dir, fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imageFile)
            bm?.compress(CompressFormat.PNG,100, fos) ?: throw NullPointerException("bitmap is null")
            fos.close()
            return true
        } catch (e: IOException) {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
        return false
    }
}