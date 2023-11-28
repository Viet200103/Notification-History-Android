package com.notisaver.database.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.notisaver.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBigPictureNotificationStyle(bigPictureNotificationStyle: BigPictureNotificationStyle): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBigTextNotificationStyle(bigTextNotificationStyle: BigTextNotificationStyle): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBaseNotificationStyle(baseNotificationStyle: BaseNotificationStyle): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertONotification(on: ONotification): Long

    ////////////////////////////////////////////////////////////////

    @Transaction
    @Query(
        "SELECT *, count(*) as nCount FROM ONotification " +
                "GROUP BY sbnKeyHashcode " +
                "HAVING MAX(timePost) " +
                "ORDER BY timePost DESC"
    )
    suspend fun loadNStatusBarGroup(): List<NStatusBarGroup>

    @Transaction
    @RawQuery(observedEntities = [NStatusBarGroup::class])
    fun loadNStatusBarGroupPaging(
        query: SupportSQLiteQuery
    ): PagingSource<Int, NStatusBarGroup>

    @Transaction
    @Query(
        "SELECT * FROM ONotification WHERE sbnKeyHashcode = :sbnKeyHashcode AND " +
                "(CASE WHEN length(:searchValue) = 0 THEN 1 " +
                "ELSE (" +
                "(title LIKE '%' || :searchValue || '%') OR " +
                "(contentText LIKE '%' || :searchValue || '%') OR " +
                "(subText LIKE '%' || :searchValue || '%')" +
                ") END) " +
                "ORDER BY timePost DESC"
    )
    fun loadNotificationPaging(
        sbnKeyHashcode: String,
        searchValue: String
    ): PagingSource<Int, NotificationLog>

    @Transaction
    @RawQuery(observedEntities = [NotificationLog::class])
    fun loadNotificationPaging(
        query: SupportSQLiteQuery
    ): PagingSource<Int, NotificationLog>

    @Transaction
    @Query("SELECT * FROM ONotification")
    fun loadAllNotifications(): List<NotificationLog>

    @Query("SELECT COUNT(*) FROM ONotification WHERE packageHashcode = :packageHashcode")
    fun loadNCountOfPackage(packageHashcode: String): Flow<Int>

    @Transaction
    @Query(
        "SELECT *, count(*) AS nCount FROM ONotification " +
                "WHERE packageHashcode = :packageHashcode AND " +
                "(CASE WHEN length(:searchValue) = 0 THEN 1 " +
                "ELSE (" +
                "(title LIKE '%' || :searchValue || '%') OR " +
                "(contentText LIKE '%' || :searchValue || '%') OR " +
                "(subText LIKE '%' || :searchValue || '%')" +
                ") END) " +
                "GROUP BY sbnKeyHashcode HAVING MAX(timePost) ORDER BY timePost DESC"
    )
    fun searchNotificationPaging(
        packageHashcode: String,
        searchValue: String
    ): PagingSource<Int, NStatusBarGroup>

    @RawQuery()
    suspend fun searchNotificationPackage(query: SupportSQLiteQuery): List<ShortNotification>

    @Query(
        "SELECT COUNT(DISTINCT packageHashcode) FROM ONotification  WHERE (CASE WHEN length(:searchValue) = 0 THEN 1 ELSE ((title LIKE '%' || :searchValue || '%') OR (contentText LIKE '%' || :searchValue || '%') OR (subText LIKE '%' || :searchValue || '%')) END) "
    )
    suspend fun getNumberPackage(searchValue: String): Int

    @Query(
        "SELECT COUNT(DISTINCT packageHashcode) FROM ONotification WHERE packageHashcode in (:packageHashcodeList) AND (CASE WHEN length(:searchValue) = 0 THEN 1 ELSE ((title LIKE '%' || :searchValue || '%') OR (contentText LIKE '%' || :searchValue || '%') OR (subText LIKE '%' || :searchValue || '%')) END) "
    )
    suspend fun getNumberPackage(packageHashcodeList: List<String>, searchValue: String): Int

    @Query(
        "SELECT logId FROM ONotification " +
                "WHERE sbnKeyHashcode = :sbnKeyHashcode AND timePost <= :topTimePost AND " +
                "(CASE WHEN length(:searchValue) = 0 THEN 1 " +
                "ELSE (" +
                "(title LIKE '%' || :searchValue || '%') OR " +
                "(contentText LIKE '%' || :searchValue || '%') OR " +
                "(subText LIKE '%' || :searchValue || '%')" +
                ") END) "
    )
    suspend fun loadLogIds(
        sbnKeyHashcode: String,
        topTimePost: Long,
        searchValue: String
    ): List<String>

    @Query(
        "SELECT logId FROM ONotification " +
                "WHERE packageHashcode = :packageHashcode " +
                "LIMIT :limit"
    )
    suspend fun loadLimitedLogIds(packageHashcode: String, limit: Int): List<String>

    @Transaction
    @Query("SELECT * FROM ONotification WHERE logId = :nLogId")
    suspend fun getNotificationLog(nLogId: String): NotificationLog?

    @Query("SELECT COUNT(*) FROM ONotification")
    suspend fun getCountNotification(): Int

    ////////////////////////////////////////////////////////////////
    @Update
    suspend fun updateNotifications(notificationList: List<ONotification>)

    @Update
    suspend fun updateNotification(on: ONotification): Int

    ////////////////////////////////////////////////////////////////
    @Delete
    suspend fun deleteNotifications(oNotification: ONotification): Int

    @Transaction
    @Query("UPDATE ONotification set isNew = 0 where isNew = 1")
    suspend fun checkAllNotificationIsRead()
}