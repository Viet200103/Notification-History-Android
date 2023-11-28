package com.notisaver.main.log.core

import android.app.Notification
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.*
import com.notisaver.main.log.model.StatusNotificationLog
import com.notisaver.misc.*
import com.notisaver.misc.createUUID
import com.notisaver.misc.getParcelableCompat
import com.notisaver.misc.hashString131
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

internal class NotificationHandler(
    private val logId: String = createUUID(true),
    private val context: Context,
    private val notisaveRepository: NotisaveRepository,
    private val dataFolder: File,
    private val workerScope: CoroutineScope,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    companion object {

        internal const val COMPAT_BIG_TEXT_STYLE =
            "androidx.core.app.NotificationCompat\$BigTextStyle"

        internal const val COMPAT_BIG_PICTURE_STYLE =
            "androidx.core.app.NotificationCompat\$BigPictureStyle"

        internal const val COMPAT_MESSAGE_STYLE =
            "androidx.core.app.NotificationCompat\$MessagingStyle"

        internal const val COMPAT_INBOX_STYlE =
            "androidx.core.app.NotificationCompat\$InboxStyle"

        internal const val COMPAT_CUSTOM_VIEW_STYLE =
            "androidx.core.app.NotificationCompat\$DecoratedCustomViewStyle"

        internal const val BIG_TEXT_STYLE = "android.app.Notification\$BigTextStyle"
        internal const val BIG_PICTURE_STYLE = "android.app.Notification\$BigPictureStyle"
        internal const val MESSAGE_STYLE = "android.app.Notification\$MessagingStyle"
        internal const val INBOX_STYlE = "android.app.Notification\$InboxStyle"
        internal const val MEDIA_STYLE = "android.app.Notification\$MediaStyle"
        internal const val CUSTOM_VIEW_STYLE = "android.app.Notification\$DecoratedCustomViewStyle"

        private const val LARGE_ICON_FILE = ".icon.large"

        //        internal const val SMALL_ICON_FILE = ".icon.small" cannot change use because have so much type of icon
        private const val BIG_PICTURE_FILE = ".picture"
        private const val BIG_LARGE_ICON_FILE = ".icon.blarge"

        internal fun createBigPictureFileName(logId: String) = logId + BIG_PICTURE_FILE
        internal fun createLargeIconFileName(logId: String) = logId + LARGE_ICON_FILE
        internal fun createBigLargeIconFileName(logId: String) = logId + BIG_LARGE_ICON_FILE

        internal fun getLargeIconFile(packageDir: File, notificationLog: NotificationLog): File {
            return File(
                packageDir,
                createLargeIconFileName(notificationLog.logId)
            )
        }

        internal fun getBigPictureFile(packageDir: File, notificationLog: NotificationLog): File {
            return File(
                packageDir,
                createBigPictureFileName(notificationLog.logId)
            )
        }

        internal fun getBigLargeIconFile(packageDir: File, notificationLog: NotificationLog): File {
            return File(
                packageDir,
                createBigLargeIconFileName(notificationLog.logId)
            )
        }
    }


    private var resultListener: ((Result<NotificationLog>) -> Unit)? = null

    suspend fun logPosted(statusLog: StatusNotificationLog, packageHashcode: String) = try {
        log(statusLog, packageHashcode)
    } catch (e: Exception) {
        resultListener?.invoke(Result.failure(e))
    }

    @Throws
    @WorkerThread
    private suspend fun log(statusLog: StatusNotificationLog, packageHashcode: String) {
        val sbnKeyHashcode = hashString131(statusLog.key)

        val notification = statusLog.notification

        val largeIcon = workerScope.async(workerDispatcher) {
            iconToFile(
                fileName = createLargeIconFileName(logId),
                icon = notification.getLargeIcon()
            )
        }

        var createStyle: Deferred<NotificationStyle?>? = null

        notification.extras?.let {
            val templateCompat = it.getCharSequence(NotificationCompat.EXTRA_COMPAT_TEMPLATE)
            val template = it.getCharSequence(NotificationCompat.EXTRA_TEMPLATE)

            if (templateCompat != null) {

                createStyle = workerScope.async(workerDispatcher) {
                    constructNotificationLogStyle(logId, templateCompat.toString(), notification)
                }
                templateCompat
            } else if (template != null) {

                createStyle = workerScope.async(workerDispatcher) {
                    constructNotificationLogStyle(logId, template.toString(), notification)
                }
                template
            } else null
        }

        val oNotification = ONotification(
            logId = this.logId,
            sbnKeyHashcode = sbnKeyHashcode,
            title = toString(NotificationCompat.getContentTitle(notification)),
            contentText = toString(NotificationCompat.getContentText(notification)),
            subText = toString(NotificationCompat.getSubText(notification))
                ?: toString(NotificationCompat.getContentInfo(notification)),
            isLargeIcon = largeIcon.await(),
            isOnGoing = statusLog.isOngoing,
            timePost = statusLog.postTime,
            timeAdded = System.currentTimeMillis(),
            packageHashcode = packageHashcode,
        )

        val notificationStyle = createStyle?.await()
        val notificationLog = NotificationLog(oNotification, notificationStyle)

        if (oNotification.isBlank() && notificationStyle?.isBlank() != false) {
            throw NotificationLogException("NotificationHandler: log is empty $notificationLog")
        }

        if (notisaveRepository.logNotification(oNotification)) {

            notificationStyle?.let { style ->
                if (notisaveRepository.logBaseNotificationStyle(style.baseStyle)) {

                    if (style.bigTextStyle != null) {
                        notisaveRepository.logBigTextStyle(style.bigTextStyle)
                    }

                    if (style.bigPictureStyle != null) {
                        notisaveRepository.logBigPictureStyle(style.bigPictureStyle)
                    }
                }
            }

            resultListener?.invoke(
                Result.success(notificationLog)
            )

        } else throw NotificationLogException("NotificationHandler: log notification is failed $notificationLog")
    }

    @Throws
    @WorkerThread
    private fun constructNotificationLogStyle(
        notificationId: String,
        templateStyle: String,
        notification: Notification
    ): NotificationStyle? {
        val extras = notification.extras

        val baseStyle = BaseNotificationStyle(
            styleId = notificationId,
            bigContentTitle = toString(extras.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG)),
            summaryText = toString(extras.getCharSequence(NotificationCompat.EXTRA_SUMMARY_TEXT))
        )

        var bigTextStyle: BigTextNotificationStyle? = null
        var bigPictureStyle: BigPictureNotificationStyle? = null

        try {
            when (templateStyle) {
                COMPAT_BIG_TEXT_STYLE, BIG_TEXT_STYLE -> {
                    bigTextStyle = BigTextNotificationStyle(
                        styleId = baseStyle.styleId,
                        bigText = extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT)
                            ?.toString(),
                    )
                    baseStyle.templateId = BaseNotificationStyle.BIG_TEXT_ID
                }

                COMPAT_BIG_PICTURE_STYLE, BIG_PICTURE_STYLE -> {
                    val bigLargeIcon = extras.getParcelableCompat(
                        NotificationCompat.EXTRA_LARGE_ICON_BIG,
                        Bitmap::class.java
                    )
                    bigPictureStyle = BigPictureNotificationStyle(
                        styleId = baseStyle.styleId,
                        isPicture = bitmapToFile(
                            dataFolder,
                            createBigPictureFileName(logId),
                            extras.getParcelableCompat(
                                NotificationCompat.EXTRA_PICTURE,
                                Bitmap::class.java
                            )
                        ),
                        isBigLargeIcon = bitmapToFile(
                            dataFolder,
                            createBigLargeIconFileName(logId),
                            bigLargeIcon
                        ),
                    ).also {
                        if (!it.isPicture) return null
                    }
                    baseStyle.templateId = BaseNotificationStyle.BIG_PICTURE_ID
                }

                COMPAT_INBOX_STYlE, INBOX_STYlE -> {
                    baseStyle.templateId = BaseNotificationStyle.INBOX_ID
                }

                COMPAT_MESSAGE_STYLE, MESSAGE_STYLE -> {
                    baseStyle.templateId = BaseNotificationStyle.MESSAGE_ID
                }

                COMPAT_CUSTOM_VIEW_STYLE, CUSTOM_VIEW_STYLE -> {
                    baseStyle.templateId = BaseNotificationStyle.CUSTOM_ID
                }

                MEDIA_STYLE -> {
                    baseStyle.templateId = BaseNotificationStyle.MEDIA_ID
                }
            }
        } catch (e: Exception) {
            Timber.d(e.message)
            return null
        }

        if (baseStyle.templateId == -1) return null

        return NotificationStyle(
            baseStyle = baseStyle,
            bigTextStyle = bigTextStyle,
            bigPictureStyle = bigPictureStyle
        )
    }

    @Throws
    @WorkerThread
    private fun iconToFile(fileName: String, icon: Icon?): Boolean {
        if (icon == null) return false
        return try {
            val bitmap = iconToBitmap(icon)
            if (bitmap != null) {
                bitmapToFile(dataFolder, fileName, bitmap)
            } else false
        } catch (e: Exception) {
            false
        }
    }

    @Throws
    @WorkerThread
    private fun iconToBitmap(icon: Icon?): Bitmap? {
        if (icon == null) return null
        return toBitmap(icon.loadDrawable(context))
    }

    /**
     *@see Drawable.toBitmap
     */
    @Throws
    private fun toBitmap(
        drawable: Drawable?,
        config: Bitmap.Config? = null
    ): Bitmap? {
        if (drawable == null) return null
        var height = drawable.intrinsicHeight
        var width = drawable.intrinsicWidth
        with(drawable) {
            if (this is BitmapDrawable) {
                if (bitmap == null) {
                    return null
                }

                if (config == null || bitmap.config == config) {
                    // Fast-path to return original. Bitmap.createScaledBitmap will do this check, but it
                    // involves allocation and two jumps into native code so we perform the check ourselves.
                    if (width == bitmap.width && height == bitmap.height) {
                        return bitmap
                    }
                    return Bitmap.createScaledBitmap(bitmap, width, height, true)
                }
            }
        }

        if (width <= 0 || height <= 0) {
            width = 1; height = 1
        }

        val bitmap = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)

        /*
              Compress with JPEG, because it does not support transparency background
              so that We need set color for background before convert to canvas unless the background
              default is BLACK
           */
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    @WorkerThread
    private fun bitmapToFile(dir: File, fileName: String, bitmap: Bitmap?): Boolean {
        if (bitmap == null) return false

        return try {
            val file = File(dir, fileName)
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: Exception) {
            Timber.d("encode bitmap fail: ${e.message}")
            false
        }
    }

    internal fun setResultObserver(action: (Result<NotificationLog>) -> Unit) {
        this.resultListener = action
    }

    private fun toString(text: CharSequence?): String? {
        return text?.toString()
    }
}