package com.notisaver.database.entities

import android.os.Parcelable
import androidx.room.*
import com.notisaver.database.NotisaveDatabase.Companion.COL_LOG_ID
import com.notisaver.database.NotisaveDatabase.Companion.COL_STYLE_ID
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationStyle(
    @Embedded
    val baseStyle: BaseNotificationStyle,

    @Relation(
        parentColumn = COL_STYLE_ID,
        entityColumn = COL_STYLE_ID
    )
    val bigTextStyle: BigTextNotificationStyle? = null,

    @Relation(
        parentColumn = COL_STYLE_ID,
        entityColumn = COL_STYLE_ID,
    )
    val bigPictureStyle: BigPictureNotificationStyle? = null,
): Parcelable {
    fun getTemplateStyle(): String {
        return baseStyle.getTemplateStyle()
    }

    internal val templateId
        get() = baseStyle.templateId


    fun isBlank() = baseStyle.isBlank()
}

////////////////////////////////////////////////////////////////////////////////////////
interface INotificationStyle {
    val styleId: String
}

////////////////////////////////////////////////////////////////////////////////////////
@Parcelize
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ONotification::class,
            parentColumns = [COL_LOG_ID],
            childColumns = [COL_STYLE_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class BaseNotificationStyle(
    @PrimaryKey
    @ColumnInfo(name = COL_STYLE_ID)
    override val styleId: String,
    val bigContentTitle: String? = null,
    val summaryText: String? = null,
    var templateId: Int = -1
// TODO("add template column because some style we cannot log can just use base style
//  so that to know it used to be what style, template style should be stored")
) : INotificationStyle, Parcelable {

    companion object {
        internal const val BIG_TEXT_ID = 0
        internal const val BIG_PICTURE_ID = 1
        internal const val INBOX_ID = 2
        internal const val MESSAGE_ID = 3
        internal const val CUSTOM_ID = 4
        internal const val MEDIA_ID = 5

        internal const val BIG_TEXT_STYLE = "BText"
        internal const val BIG_PICTURE_STYLE = "BPicture"
        internal const val INBOX_STYLE = "Inbox"
        internal const val MESSAGE_STYLE = "Message"
        internal const val CUSTOM_STYLE = "Custom"
        internal const val MEDIA_STYLE = "Media"
    }

    fun getTemplateStyle(): String = when(templateId) {
        BIG_PICTURE_ID -> BIG_PICTURE_STYLE
        BIG_TEXT_ID -> BIG_TEXT_STYLE
        INBOX_ID -> INBOX_STYLE
        MESSAGE_ID -> MESSAGE_STYLE
        MEDIA_ID -> MEDIA_STYLE
        CUSTOM_ID -> CUSTOM_STYLE
        else -> throw IllegalArgumentException()
    }

    fun isBlank() = bigContentTitle.isNullOrBlank() && summaryText.isNullOrBlank() && templateId == -1
}

////////////////////////////////////////////////////////////////////////////////////////
@Parcelize
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BaseNotificationStyle::class,
            parentColumns = [COL_STYLE_ID],
            childColumns = [COL_STYLE_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class BigTextNotificationStyle(
    @PrimaryKey
    @ColumnInfo(name = COL_STYLE_ID)
    override val styleId: String,
    val bigText: String? = null
) : INotificationStyle, Parcelable
////////////////////////////////////////////////////////////////////////////////////////
@Parcelize
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = BaseNotificationStyle::class,
            parentColumns = [COL_STYLE_ID],
            childColumns = [COL_STYLE_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class BigPictureNotificationStyle(
    @PrimaryKey
    @ColumnInfo(name = COL_STYLE_ID)
    override val styleId: String,
    val isPicture: Boolean = true,
    val isBigLargeIcon: Boolean = false
): INotificationStyle, Parcelable


