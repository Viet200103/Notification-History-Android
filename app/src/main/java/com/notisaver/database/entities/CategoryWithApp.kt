package com.notisaver.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.notisaver.database.NotisaveDatabase
import com.notisaver.database.extra_relationships.CategoryAppMetaDataCrossRef

data class CategoryWithApp(
    @Embedded
    val category: NotificationCategory,
    @Relation(
        parentColumn = NotisaveDatabase.COL_CATEGORY_ID,
        entityColumn = NotisaveDatabase.COL_PACKAGE_HASHCODE,
        associateBy = Junction(CategoryAppMetaDataCrossRef::class)
    )
    val appMetaDataList: List<AppMetaData>
)