package com.notisaver.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.PowerManager
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager

class NotisaveSetting(
    private val defaultPrefers: SharedPreferences
) {
    companion object {
        internal const val PRE_USAGE_POLICY = "pre_policy"
        internal const val PRE_FIRST_START = "first_start"

        internal const val PRE_LISTENER_STATUS = "pre_listener_status"
        internal const val PRE_TOTAL_SAVED = "pre_general_total_saved"
        internal const val PRE_LANGUAGE = "pre_general_language"
        internal const val PRE_SCREEN_CAPTURE = "pre_screen_capture"
        internal const val PRE_NLOG_ONGOING = "pre_log_ongoing"
        internal const val PRE_SETUP_PASSCODE = "pre_setup_passcode"
        internal const val PRE_ENABLE_FINGERPRINT = "pre_enable_fingerprint"
        internal const val PRE_OTHER_TRANSLATE = "pre_other_translate_language"
        internal const val PRE_APP_DEVICE_LOADED = "pre_app_device_loaded"
        internal const val PRE_KEEP_ALIVE = "pre_keep_alive"
        internal const val PRE_NOTIFICATION_PERMISSION = "pre_notification_permission"
        internal const val PRE_BATTERY_WHITELIST = "pre_battery_whitelist"

        internal const val ACTIVITY_ACTION = "a_action"
        internal const val ACTION_SETTING = 0xA0
        internal const val ACTION_ABOUT = 0xA1
        internal const val ACTION_SETUP_PASSCODE = 0xA2
        internal const val ACTION_RETRIEVE_PASSCODE = 0xA3
        internal const val ACTION_VERIFY_SECURITY_QUESTION = 0xA4
        internal const val ACTION_NOTIFICATION_MANAGER = 0xA5

        internal const val VERIFY_QUESTION_CONFIRM = "verify_question"
        internal const val PASSWORD_CONFIRM = "password_confirm"

        internal const val CONFIRM_FAILED = -1
        internal const val CONFIRM_SUCCESS = 0

        internal const val LISTENER_SERVICE_RUNNING_NOTIFICATION_CHANEL ="Service State"
        internal const val LISTENER_SERVICE_RUNNING_NOTIFICATION_CHANEL_ID = "RUNNING_CHANEL_ID"
        internal const val LISTENER_STATUS_NOTIFICATION_RUNNING_NO_ID: Int = 456527745
        internal const val ACTION_KEEP_ALIVE = "com.notisave.services.KEEP_ALIVE"

        @Volatile
        private var Instances: NotisaveSetting? = null

        internal fun getInstance(context: Context): NotisaveSetting {
            return Instances ?: synchronized(this) {
                val instances = NotisaveSetting(
                    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                )
                instances.also {
                    Instances = it
                }
            }
        }

        internal fun isNotOptimizeBatterEnabled(context: Context): Boolean {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as (PowerManager)
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
    }

    internal fun isFingerprintEnabled() = defaultPrefers.getBoolean(PRE_ENABLE_FINGERPRINT, false)

    val isEnabledKeepLive: Boolean
        get() = defaultPrefers.getBoolean(PRE_KEEP_ALIVE, false)

    internal val isAppOnDeviceLoaded
        get() = defaultPrefers.getBoolean(PRE_APP_DEVICE_LOADED, false)

    internal fun setScannedAppDevice(isLoaded: Boolean) {
        defaultPrefers.edit(true) {
            putBoolean(PRE_APP_DEVICE_LOADED, isLoaded)
        }
    }

    internal fun doKeepAppLive(context: Context) {
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)

        val intent = Intent(ACTION_KEEP_ALIVE).apply {
            putExtra(ACTION_KEEP_ALIVE, isEnabledKeepLive)
        }

        localBroadcastManager.sendBroadcast(intent)
    }

    internal val isScreenCaptureEnabled
        get() = defaultPrefers.getBoolean(PRE_SCREEN_CAPTURE, true)
}