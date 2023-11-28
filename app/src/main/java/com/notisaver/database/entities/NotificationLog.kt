package com.notisaver.database.entities

import android.os.Parcelable
import androidx.room.*
import com.notisaver.database.NotisaveDatabase.Companion.COL_LOG_ID
import com.notisaver.database.NotisaveDatabase.Companion.COL_STYLE_ID
import com.notisaver.main.log.intefaces.NotificationItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationLog (
    @Embedded
    internal var notification: ONotification,

    @Relation(
        entity = BaseNotificationStyle::class,
        entityColumn = COL_STYLE_ID,
        parentColumn = COL_LOG_ID
    )
    internal var notificationStyle: NotificationStyle? = null
) : NotificationItem, Parcelable {

    internal val templateStyle
        get() = notificationStyle?.getTemplateStyle()

    internal val templateId
        get() = notificationStyle?.baseStyle?.templateId

    internal val sbnKeyHashcode
        get() = notification.sbnKeyHashcode

    internal val timePost
        get() = notification.timePost

    internal val timeAdded
        get() = notification.timeAdded

    internal val logId
        get() = notification.logId

    override val packageHashcode
        get() = notification.packageHashcode

    internal val isNew
        get() = notification.isNew

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationLog

        if (notification != other.notification) return false
        return true
    }

    override fun hashCode(): Int {
        return 31 * notification.hashCode() + (notificationStyle?.hashCode() ?: 0)
    }

    fun setIsNew(isNew: Boolean) {
        notification.isNew = isNew
    }

    override val id: CharSequence
        get() = notification.logId
}
