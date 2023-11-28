package com.notisaver.database.extra_relationships

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.notisaver.database.NotisaveDatabase.Companion.COL_CATEGORY_ID
import com.notisaver.database.NotisaveDatabase.Companion.COL_PACKAGE_HASHCODE
import com.notisaver.database.entities.NotificationCategory
import com.notisaver.database.entities.AppMetaData

@Entity(
    primaryKeys = [COL_CATEGORY_ID, COL_PACKAGE_HASHCODE],
    foreignKeys = [
        ForeignKey(
            entity = NotificationCategory::class,
            parentColumns = [COL_CATEGORY_ID],
            childColumns = [COL_CATEGORY_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AppMetaData::class,
            parentColumns = [COL_PACKAGE_HASHCODE],
            childColumns = [COL_PACKAGE_HASHCODE],
        )
    ],
    indices = [
        Index(COL_CATEGORY_ID),
        Index(COL_PACKAGE_HASHCODE)
    ]
)
data class CategoryAppMetaDataCrossRef(
    @ColumnInfo(name = COL_CATEGORY_ID)
    val categoryId: String,
    @ColumnInfo(name = COL_PACKAGE_HASHCODE)
    val packageHashcode: String
)