package com.notisaver.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notisaver.database.NotisaveDatabase.Companion.COL_CATEGORY_ID

@Entity(tableName = "Category")
class NotificationCategory(
    @PrimaryKey
    @ColumnInfo(name = COL_CATEGORY_ID)
    val categoryId: String,
    val name: String
)