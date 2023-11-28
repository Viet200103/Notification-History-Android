package com.notisaver.main.log.model

import android.app.Notification
import android.os.Build
import android.service.notification.StatusBarNotification

class StatusNotificationLog(
    val packageName: String,
    val overrideGroupKey: String? = null,
    val key: String,
    val notification: Notification,
    val isOngoing: Boolean,
    val postTime: Long
) {

    companion object {

        fun createFrom(statusBarNotification: StatusBarNotification?): StatusNotificationLog? {
            if (statusBarNotification == null) {
                return null
            }


            return StatusNotificationLog(
                statusBarNotification.packageName,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    statusBarNotification.overrideGroupKey
                } else null,
                statusBarNotification.key,
                statusBarNotification.notification,
                statusBarNotification.isOngoing,
                statusBarNotification.postTime
            )
        }
    }
}