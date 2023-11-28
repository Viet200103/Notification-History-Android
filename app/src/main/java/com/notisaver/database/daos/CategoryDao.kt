package com.notisaver.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.notisaver.database.entities.CategoryWithApp
import com.notisaver.database.entities.NotificationCategory
import com.notisaver.database.extra_relationships.CategoryAppMetaDataCrossRef
import java.util.ArrayList

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: NotificationCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppMetaData(relationship: CategoryAppMetaDataCrossRef): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppMetaData(relationshipList: ArrayList<CategoryAppMetaDataCrossRef>)

    @Delete
    suspend fun removeAppMetaData(relationship: CategoryAppMetaDataCrossRef)

    @Delete
    suspend fun removeAppMetaData(relationshipList: ArrayList<CategoryAppMetaDataCrossRef>)

    @Transaction
    @Query("SELECT * FROM  category where categoryId = :categoryId")
    suspend fun loadCategory(categoryId: String): CategoryWithApp

    @Query("SELECT packageHashcode FROM CategoryAppMetaDataCrossRef WHERE categoryId = :messageCategoryId")
    suspend fun loadPackageHashcodeList(messageCategoryId: String): List<String>

    @Query("SELECT EXISTS (SELECT 1 FROM CategoryAppMetaDataCrossRef LIMIT 1)")
    suspend fun hasMessageItemGroup(): Boolean
}