package com.notisaver.database.daos

import androidx.room.*
import com.notisaver.database.AppTemplate
import com.notisaver.database.entities.AppMetaData
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMetaDataDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAppMetaData(appMetaData: AppMetaData): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAppMetaData(list: List<AppMetaData>)

    @Update
    suspend fun updateLogOngoingAllApps(list: List<AppMetaData>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppMetaData(metaData: AppMetaData): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppMetaData(list: List<AppMetaData>)

    @Query("SELECT * FROM appmetadata")
    suspend fun loadAllAppMetaData(): List<AppMetaData>

    @Query("SELECT * FROM appmetadata WHERE packageHashcode = :packageHashcode")
    suspend fun loadAppMetaData(packageHashcode: String): AppMetaData?

    @Query("SELECT  * FROM APPTEMPLATE")
    fun loadAppTemplate(): Flow<List<AppTemplate>>

    @Delete
    fun deleteApp(appMetaData: AppMetaData): Int
}