package com.notisaver.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.notisaver.database.NotisaveDatabase
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    indices = [Index(NotisaveDatabase.COL_PACKAGE_HASHCODE), Index(NotisaveDatabase.COL_LOG_ID, unique = true)],
    primaryKeys = [NotisaveDatabase.COL_SBN_KEY_HASHCODE, NotisaveDatabase.COL_TIME_POST],
    foreignKeys = [
        ForeignKey(
            entity = AppMetaData::class,
            parentColumns = [NotisaveDatabase.COL_PACKAGE_HASHCODE],
            childColumns = [NotisaveDatabase.COL_PACKAGE_HASHCODE],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class ONotification(
    @ColumnInfo(name = NotisaveDatabase.COL_SBN_KEY_HASHCODE)
    val sbnKeyHashcode: String,
    @ColumnInfo(name = NotisaveDatabase.COL_TIME_POST)
    val timePost: Long = -1,
    @ColumnInfo(name = NotisaveDatabase.COL_LOG_ID)
    val logId: String,
    val title: String? = null,
    val contentText: String? = null,
    val subText: String? = null,
    val isLargeIcon: Boolean = false,
    val isOnGoing: Boolean = false,
    var isNew: Boolean = true, // isRecent
    val timeAdded: Long = -1,
    @ColumnInfo(name = NotisaveDatabase.COL_PACKAGE_HASHCODE)
    val packageHashcode: String
) : Parcelable {

    internal fun isBlank(): Boolean {
        return title.isNullOrBlank()
                && contentText.isNullOrBlank()
                && subText.isNullOrBlank()
    }

    override fun toString(): String {
        return "ONotification(sbnKeyHashcode='$sbnKeyHashcode', timePost=$timePost, logId='$logId', title=$title, contentText=$contentText, subText=$subText, isLargeIcon=$isLargeIcon, isOnGoing=$isOnGoing, isNew=$isNew, timeAdded=$timeAdded, packageHashcode='$packageHashcode')"
    }

}