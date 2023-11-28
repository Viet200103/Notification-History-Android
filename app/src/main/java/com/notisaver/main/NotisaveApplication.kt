package com.notisaver.main

import android.app.Application
import com.notisaver.BuildConfig
import com.notisaver.database.NotisaveDatabase
import com.notisaver.database.NotisaveRepository
import com.notisaver.main.manager.AppInfoManager
import timber.log.Timber


class NotisaveApplication : Application() {

    val notisaveRepository by lazy {
        NotisaveRepository(
            context = this,
            NotisaveDatabase.getDatabase(this),
        )
    }

    val notificationManager by lazy {
        NotificationManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager.createNotificationListenerChancel()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    internal fun getAppInfoManager() = AppInfoManager.getInstance(this)
}