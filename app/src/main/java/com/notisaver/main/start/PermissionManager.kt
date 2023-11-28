package com.notisaver.main.start

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.notisaver.R
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.createAccessNotificationIntent
import com.notisaver.misc.createNormalAccessNotificationIntent

class PermissionManager {

    companion object {
        internal fun createAccessNotificationRationaleDialog(context: Context): AlertDialog {

            return SupportMaterialAlertDialog.createAlertDialog(
                context,
                context.getString(R.string.access_notifications),
                R.drawable.ic_phone_listener,
                context.getString(R.string.access_notifications_summary, context.getString(R.string.app_name)),
                positiveListener = {
                    try {
                        context.startActivity(
                            createAccessNotificationIntent(context)
                        )
                    } catch (e: Exception) {
                        context.startActivity(
                            createNormalAccessNotificationIntent()
                        )
                    }
                }
            ).create()
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        internal fun isPostNotificationGranted(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun createPostNotificationRationaleDialog(
            context: Context,
            agreeAction: () -> Unit,
            cancelAction: (() -> Unit)? = null
        ) = SupportMaterialAlertDialog.createAlertDialog(
            context,
            context.getString(R.string.notification_permission),
            R.drawable.ic_notification,
            context.getString(
                R.string.post_notification_rationale,
                context.getString(R.string.app_name)
            ),
            positiveTextId = R.string.allow,
            negativeListener = cancelAction,
            positiveListener = agreeAction
        )

        internal fun createPostNotificationRequireSettingDialog(
            context: Context,
            agreeAction: (() -> Unit)? = null,
            cancelAction: (() -> Unit)? = null
        ) = SupportMaterialAlertDialog.createAlertDialog(
            context,
            context.getString(R.string.notification_permission),
            R.drawable.ic_notification,
            context.getString(
                R.string.post_notification_rationale,
                context.getString(R.string.app_name)
            ) + " " + context.getString(R.string.post_notification_from_setting),
            positiveTextId = R.string.allow,
            negativeListener = cancelAction,
            positiveListener = agreeAction
        )

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        internal fun requestPostNotificationPermission(
            activity: Activity,
            launcher: ActivityResultLauncher<String>,
            cancelAction: (() -> Unit)? = null
        ) {
            val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
            when {
                shouldShowPostNotificationRationale(activity) -> {
                    createPostNotificationRationaleDialog(
                        activity, agreeAction = { launcher.launch(notificationPermission) }, cancelAction
                    ).show()
                }
                else -> launcher.launch(notificationPermission)
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        internal fun shouldShowPostNotificationRationale(activity: Activity): Boolean {
            return activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}