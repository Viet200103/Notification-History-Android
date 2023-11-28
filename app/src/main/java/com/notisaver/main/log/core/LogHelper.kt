package com.notisaver.main.log.core

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.notisaver.database.entities.ONotification
import com.notisaver.main.log.model.StatusNotificationLog
import java.io.File

class LogHelper {

    companion object {
        internal fun getPackageHashcodeFolder(context: Context, packageHashcode: String): File {
            return context.applicationContext.getDir(packageHashcode, Context.MODE_PRIVATE)
        }

        @Throws
        private fun deleteDataFile(packageFolder: File, fileName: String) {
            kotlin.runCatching {
                File(packageFolder, fileName).delete()
            }
        }

        internal fun deleteFileOfNotification(packageFolder: File, logId: String) {
            deleteDataFile(
                packageFolder,
                NotificationHandler.createBigLargeIconFileName(logId)
            )
            deleteDataFile(
                packageFolder,
                NotificationHandler.createBigPictureFileName(logId)
            )
            deleteDataFile(
                packageFolder,
                NotificationHandler.createLargeIconFileName(logId)
            )
        }

        internal fun deleteFileOfNotification(context: Context, oNotification: ONotification) {
            deleteFileOfNotification(
                getPackageHashcodeFolder(context, oNotification.packageHashcode), oNotification.logId
            )
        }

        internal fun deletePackageFolder(context: Context, packageHashcode: String) {
            kotlin.runCatching {
                getPackageHashcodeFolder(
                    context,
                    packageHashcode
                ).deleteRecursively()
            }
        }
    }

    internal fun isGroup(statusLog: StatusNotificationLog): Boolean {
        val isSummary = NotificationCompat.isGroupSummary(statusLog.notification)

        if (!isSummary) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (statusLog.overrideGroupKey != null) return true
        }

        val notification = statusLog.notification

        if (notification.group != null) return true

        return false
    }

}