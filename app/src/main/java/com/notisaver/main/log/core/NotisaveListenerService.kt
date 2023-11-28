package com.notisaver.main.log.core

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.notisaver.database.NotisaveDatabase
import com.notisaver.database.entities.AppMetaData
import com.notisaver.database.extra_relationships.CategoryAppMetaDataCrossRef
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.log.model.StatusNotificationLog
import com.notisaver.misc.asNotisaveApplication
import com.notisaver.misc.createPackageHashcode
import kotlinx.coroutines.*
import timber.log.Timber

// No rename class
class NotisaveListenerService : NotificationListenerService() {

    companion object {
        const val TAG = "NotisaveListenerService"
    }


    private var isConnected = false

    private val ioDispatcher = Dispatchers.IO

    private val logDispatcher = Dispatchers.IO

    private val workerScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    internal lateinit var notisaveApplication: NotisaveApplication
    private val notificationManager
        get() = notisaveApplication.notificationManager

    private val appMetaCacheManager
        get() = AppMetaCacheManager.getInstance(notisaveApplication.notisaveRepository)

    private val logHelper by lazy {
        LogHelper()
    }

    private val messageAsset by lazy {
        MessageAsset(notisaveApplication)
    }

    override fun onCreate() {
        super.onCreate()
        notisaveApplication = this.asNotisaveApplication()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            keepAliveReceiver,
            IntentFilter().apply {
                addAction(NotisaveSetting.ACTION_KEEP_ALIVE)
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null
            && !intent.action.isNullOrEmpty()
            && intent.action == "android.service.notification.NotificationListenerService"
        ) {
            tryReconnectService()
        }
        return START_STICKY
    }

    override fun onListenerConnected() {
        isConnected = true

        if (NotisaveSetting.getInstance(this).isEnabledKeepLive) {
            keepAlive()
        }

        workerScope.launch(ioDispatcher) {
            try {
                activeNotifications.forEach { sbn ->
                    ensureActive()
                    handleSbn(StatusNotificationLog.createFrom(sbn), this)
                }
            } catch (_: Exception) {
                tryReconnectService()
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        onNotificationPosted(StatusNotificationLog.createFrom(sbn))
    }

    internal fun onNotificationPosted(statusLog: StatusNotificationLog?) {
        workerScope.launch(ioDispatcher) {
            handleSbn(statusLog, this)
        }
    }

    private suspend fun handleSbn(sbn: StatusNotificationLog?, logScope: CoroutineScope) {
        if (sbn == null) return

        val packageHashcode = createPackageHashcode(sbn.packageName)

        var appMetaData = appMetaCacheManager.getAppMetaData(packageHashcode)

        if (appMetaData == null) {

            Timber.d("AppMetaData is null ${sbn.packageName}")

            appMetaData = AppMetaData(
                packageHashcode = packageHashcode,
                packageName = sbn.packageName
            )

            appMetaCacheManager.logAppMetaData(appMetaData)

            logScope.launch {
                try {
                    if (!messageAsset.isPrepared(packageHashcode)) {
                        val relation = CategoryAppMetaDataCrossRef(
                            NotisaveDatabase.MESSAGE_CATEGORY_ID, packageHashcode
                        )

                        if (notisaveApplication.notisaveRepository.addAppToCategory(relation)) {
                            messageAsset.updatePrepared(packageHashcode)
                        }
                    }
                } catch (_: Exception) {

                }
            }

            logNotification(appMetaData, sbn, logScope)

            return
        }

        if (!appMetaData.isTracking) {
            return
        }

        Timber.d("AppMetaData: ${sbn.packageName} LogOngoing: ${appMetaData.isLogOnGoing} || ${sbn.isOngoing}")
        if (!appMetaData.isLogOnGoing && sbn.isOngoing) {
            return
        }

        logNotification(appMetaData, sbn, logScope)
    }

    private suspend fun logNotification(
        appMetaData: AppMetaData,
        statusLog: StatusNotificationLog,
        logScope: CoroutineScope
    ) {
        if (logHelper.isGroup(statusLog)) return

        logScope.launch(ioDispatcher) {
            try {
                val handler = NotificationHandler(
                    dataFolder = LogHelper.getPackageHashcodeFolder(
                        notisaveApplication,
                        appMetaData.packageHashcode
                    ),
                    context = notisaveApplication,
                    notisaveRepository = notisaveApplication.notisaveRepository,
                    workerScope = this,
                    workerDispatcher = logDispatcher
                )

                handler.setResultObserver { result ->
                    Timber.d("NotificationLogResult: \nappPackage: ${appMetaData.packageName}\nresult: $result")
                    result.onSuccess {

                        if (!appMetaData.isCleanable) {
                            return@onSuccess
                        }

                        cancelNotification(statusLog.key)
                    }
                }

                handler.logPosted(statusLog, appMetaData.packageHashcode)

            } catch (e: Exception) {
                Timber.d("NotificationLogResult error: " + e.message)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        workerScope.cancel()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keepAliveReceiver)
    }

    override fun onListenerDisconnected() {
        isConnected = false
    }

    private fun tryReconnectService() {
        Timber.d("$TAG try connect service at API: ${Build.VERSION.SDK_INT}")

        toggleNotificationListenerService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotisaveListenerService::class.java.name))
        }
    }

    private fun toggleNotificationListenerService() {
        Timber.d("$TAG toggleNotificationListenerService() called")
        val thisComponent = ComponentName(
            this,
            NotisaveListenerService::class.java
        )
        val pm = packageManager
        pm.setComponentEnabledSetting(
            thisComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            thisComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }


    private fun keepAlive() {
        if (isConnected) {
            startForeground(
                NotisaveSetting.LISTENER_STATUS_NOTIFICATION_RUNNING_NO_ID,
                notificationManager.createListenerStatusNotification()
            )
        }
    }

    private fun cancelStatusNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        val notificationManager: NotificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.cancel(NotisaveSetting.LISTENER_STATUS_NOTIFICATION_RUNNING_NO_ID)

    }

    private val keepAliveReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            if (action == NotisaveSetting.ACTION_KEEP_ALIVE) {
                val isKeep = intent.getBooleanExtra(NotisaveSetting.ACTION_KEEP_ALIVE, false)
                if (isKeep) {
                    keepAlive()
                } else {
                    cancelStatusNotification()
                }
            }
        }
    }
}

