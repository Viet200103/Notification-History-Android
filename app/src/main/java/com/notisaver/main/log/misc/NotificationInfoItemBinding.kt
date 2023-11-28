package com.notisaver.main.log.misc

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.main.log.core.NotificationHandler
import com.notisaver.database.entities.BaseNotificationStyle
import com.notisaver.database.entities.NotificationLog
import com.notisaver.database.entities.ONotification
import com.notisaver.main.log.core.LogHelper
import com.notisaver.misc.findView
import com.notisaver.misc.changeVisibleView
import java.io.File

class NotificationInfoItemBinding private constructor(
    private val root: View,
) {

    private val applicationContext = root.context.applicationContext

    private val appIconView = findView<ShapeableImageView>(
        R.id.layout_small_icon_app_view
    )
    private val largeIconView = findView<ShapeableImageView>(
        R.id.layout_notification_info_large_icon
    )

    private val bigPictureView = findView<AppCompatImageView>(
        R.id.layout_notification_info_big_picture
    )

    private val subTextView = findView<AppCompatTextView>(R.id.layout_notification_info_sub_text)
    private val appTitleView = findView<AppCompatTextView>(R.id.layout_small_icon_app_name_view)
    private val timeView = findView<AppCompatTextView>(R.id.layout_notification_info_time)
    private val titleNoView = findView<AppCompatTextView>(R.id.layout_notification_info_title)
    private val contentView =
        findView<AppCompatTextView>(R.id.layout_notification_info_content_text)

    companion object {
        @JvmStatic
        @Throws
        internal fun bind(view: View): NotificationInfoItemBinding {
            return NotificationInfoItemBinding(view.findView(R.id.item_status_bar_notification_group_layout_container))
        }

        @JvmStatic
        @Throws
        internal fun showTextOrHide(textView: AppCompatTextView, text: CharSequence?) {
            if (text != null) {
                textView.text = text
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
        }
    }

    internal fun setContentTitle(title: String?) {
        showTextOrHide(
            titleNoView,
            title
        )
    }

    private fun setContentText(text: String?) {
        showTextOrHide(
            contentView,
            text
        )
    }

    private fun setSubText(text: String?) {
        showTextOrHide(
            subTextView,
            text
        )
    }

    private fun setTimeLine(time: CharSequence) {
        timeView.text = time
    }

    internal fun bindAppInfo(appInformation: AppInformation?) {
        changeVisibleView(appIconView, true)
        changeVisibleView(appTitleView, true)

        appIconView.setImageDrawable(
            appInformation?.icon ?: ContextCompat.getDrawable(root.context, R.drawable.ic_extension)
        )

        val appTitle = appInformation?.name ?: appInformation?.packageName
        if (appTitle != null) {
            appTitleView.text = appTitle
        } else {
            appTitleView.setText(R.string.unknown_app)
        }
    }

    private fun defaultDisplay(oNotification: ONotification) {
        showTextOrHide(subTextView, oNotification.subText)
        showTextOrHide(titleNoView, oNotification.title)
        showTextOrHide(contentView, oNotification.contentText)
    }

    internal fun resetView() {
        changeVisibleView(appIconView, false)
        changeVisibleView(appTitleView, false)
        changeVisibleView(largeIconView, false)
        changeVisibleView(bigPictureView, false)
    }

    internal fun clearView() {
        appIconView.setImageDrawable(null)
        bigPictureView.setImageDrawable(null)
        largeIconView.setImageDrawable(null)

        Glide.with(root).let {
            it.clear(appIconView)
            it.clear(bigPictureView)
            it.clear(largeIconView)
        }


        timeView.text = null
        appTitleView.text = null
        subTextView.text = null
        titleNoView.text = null
        contentView.text = null
    }

    internal fun bindNotificationLog(notificationLog: NotificationLog) {
        val oNotification = notificationLog.notification
        val notificationStyle = notificationLog.notificationStyle

        setContentTitle(
            notificationStyle?.baseStyle?.bigContentTitle ?: oNotification.title
        )

        val packageDir = LogHelper.getPackageHashcodeFolder(applicationContext, oNotification.packageHashcode)

        if (oNotification.isLargeIcon) {
            bindLargeIcon(
                NotificationHandler.getLargeIconFile(
                    packageDir, notificationLog
                )
            )
        } else bindLargeIcon(null)

        notificationStyle?.let {
            setSubText(it.baseStyle.summaryText ?: oNotification.subText)
            setContentTitle(it.baseStyle.bigContentTitle ?: oNotification.title)

            when (notificationStyle.templateId) {

                BaseNotificationStyle.BIG_TEXT_ID -> {
                    setContentText(it.bigTextStyle?.bigText ?: oNotification.contentText)
                }

                BaseNotificationStyle.BIG_PICTURE_ID -> {
                    val isHasPicture = it.bigPictureStyle?.isPicture ?: false
                    val isBigLargeIcon = it.bigPictureStyle?.isBigLargeIcon ?: false

                    changeVisibleView(bigPictureView, isHasPicture)

                    setContentTitle(it.baseStyle.bigContentTitle)
                    setSubText(oNotification.subText)
                    setContentText(it.baseStyle.summaryText)

                    Glide.with(root).load(
                        NotificationHandler.getBigPictureFile(packageDir, notificationLog)
                    ).placeholder(R.drawable.ic_image_not_found).centerCrop().into(bigPictureView)

                    if (isBigLargeIcon) {
                        bindLargeIcon(
                            NotificationHandler.getBigLargeIconFile(packageDir, notificationLog)
                        )
                    }
                }

                BaseNotificationStyle.INBOX_ID, BaseNotificationStyle.MESSAGE_ID, BaseNotificationStyle.MEDIA_ID -> {
                    showTextOrHide(contentView, oNotification.contentText)
                }

                BaseNotificationStyle.CUSTOM_ID -> {
                    setContentText(
                        oNotification.contentText ?: root.context.getString(R.string.custom_content)
                    )
                }

                else -> defaultDisplay(oNotification)
            }

        } ?: kotlin.run {
            defaultDisplay(oNotification)
        }

        try {
            setTimeLine(TimeLine.getInstance(applicationContext).formatTime(oNotification.timePost))
        } catch (e: Exception) {
            setTimeLine("")
            changeVisibleView(timeView, false)
        }
    }

    private fun bindLargeIcon(iconFile: File?) {
        if (iconFile == null) {
            clearLargeIconView()
            return
        }
        try {
            changeVisibleView(largeIconView, true)
            Glide.with(root)
                .load(iconFile)
                .placeholder(R.drawable.ic_image_not_found)
                .into(largeIconView)
        } catch (_: Exception) {
            clearLargeIconView()
        }
    }

    private fun clearLargeIconView() {
        largeIconView.setImageDrawable(null)
        changeVisibleView(largeIconView, false)
        Glide.with(root).clear(largeIconView)
    }

    @Throws
    private fun <T : View> findView(@IdRes id: Int): T {
        return root.findView(id)
    }
}