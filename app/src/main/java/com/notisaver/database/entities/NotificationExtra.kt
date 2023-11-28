package com.notisaver.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.notisaver.database.NotisaveDatabase
import com.notisaver.main.log.intefaces.NotificationItem

data class NStatusBarGroup(
    @Embedded var header: NotificationLog,
    var nCount: Int,
) : NotificationItem {

    override val id: String
        get() = header.sbnKeyHashcode

    override val packageHashcode: String
        get() = header.packageHashcode

    val sbnKeyHashcode: String
        get() = header.sbnKeyHashcode
}

data class ShortNotification(
    @ColumnInfo(name = NotisaveDatabase.COL_PACKAGE_HASHCODE)
    override val packageHashcode: String,
    @ColumnInfo(name = NotisaveDatabase.COL_SBN_KEY_HASHCODE)
    val sbnKeyHashcode: String,
    @ColumnInfo(name = NotisaveDatabase.COL_LOG_ID)
    val logId: String,
    val title: String? = null,
    val contentText: String? = null,
    @ColumnInfo(name = NotisaveDatabase.COL_TIME_POST)
    val timePost: Long = -1,
    val countOfGroup: Int = 0,
): NotificationItem {
    override val id: CharSequence
        get() = logId

}

data class NotificationPackage(
    override val packageHashcode: String,
    val timePost: Long,
    private val subList: ArrayList<ShortNotification> = arrayListOf(),
) : NotificationItem {
    private var count: Int = 0
    override val id: String
        get() = packageHashcode

    internal fun addNotification(shortNotification: ShortNotification) {
        if (subList.size < 3) {
            subList.add(shortNotification)
        }
        count += shortNotification.countOfGroup
    }

    internal fun getNumberOfNotification() = count

    internal fun getReviewList(): List<ShortNotification> = subList
}