package com.notisaver.database.entities

import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.notisaver.database.NotisaveDatabase.Companion.COL_CLEANABLE
import com.notisaver.database.NotisaveDatabase.Companion.COL_ONGOING
import com.notisaver.database.NotisaveDatabase.Companion.COL_PACKAGE
import com.notisaver.database.NotisaveDatabase.Companion.COL_PACKAGE_HASHCODE
import com.notisaver.database.NotisaveDatabase.Companion.COL_TRACING
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(
    indices = [Index(COL_PACKAGE, unique = true)]
)
data class AppMetaData(
    @ColumnInfo(name = COL_PACKAGE_HASHCODE)
    @PrimaryKey val packageHashcode: String,
    @ColumnInfo(name = COL_PACKAGE) val packageName: String,
    @ColumnInfo(name = COL_CLEANABLE) var isCleanable: Boolean = false,
    @ColumnInfo(name = COL_TRACING) var isTracking: Boolean = true,
    @ColumnInfo(name = COL_ONGOING) var isLogOnGoing: Boolean = false
) : Parcelable

data class AppInformation(
    val appMetaData: AppMetaData,
    var name: CharSequence? = null,
    var icon: Drawable? = null,
    var isInstalled: Boolean = false,
) {
    val packageHashcode
        get() = appMetaData.packageHashcode

    val packageName
        get() = appMetaData.packageName

    val isTracking
        get() = appMetaData.isTracking

    val isLogOnGoing
        get() = appMetaData.isLogOnGoing

    val isCleanable
        get() = appMetaData.isCleanable
}