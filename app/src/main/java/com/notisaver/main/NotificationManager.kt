package com.notisaver.main

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.notisaver.BuildConfig
import com.notisaver.R
import com.notisaver.main.start.StartActivity

class NotificationManager(context: Context){
    private val applicationContext = context.applicationContext

    internal fun createNotificationListenerChancel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NotisaveSetting.LISTENER_SERVICE_RUNNING_NOTIFICATION_CHANEL
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(
                NotisaveSetting.LISTENER_SERVICE_RUNNING_NOTIFICATION_CHANEL_ID,
                name,
                importance
            )

            val notificationManager: NotificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    internal fun createListenerStatusNotification(): Notification {
        val resultIntent = Intent(applicationContext, StartActivity::class.java)

        val resultPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, resultIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(applicationContext, NotisaveSetting.LISTENER_SERVICE_RUNNING_NOTIFICATION_CHANEL_ID)
            .setContentTitle(
                applicationContext.getString(
                    R.string.notification_running_background,
                    applicationContext.getString(R.string.app_name)
                ) + if (BuildConfig.DEBUG) " - Debug" else ""
            )
            .setOngoing(true)
            .setContentIntent(resultPendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}