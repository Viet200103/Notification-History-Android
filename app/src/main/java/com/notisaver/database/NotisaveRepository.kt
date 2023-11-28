package com.notisaver.database

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.notisaver.database.entities.*
import com.notisaver.database.extra_relationships.CategoryAppMetaDataCrossRef
import com.notisaver.main.log.core.LogHelper
import com.notisaver.misc.createPageConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.lang.NullPointerException
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class NotisaveRepository constructor(
    context: Context,
    internal val database: NotisaveDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    private val applicationContext = context.applicationContext

    @WorkerThread
    internal suspend fun loadCategory(categoryId: String): CategoryWithApp =
        withContext(ioDispatcher) {
            categoryDao.loadCategory(categoryId)
        }

    @WorkerThread
    internal suspend fun addAppToCategory(relationship: CategoryAppMetaDataCrossRef) = withContext(ioDispatcher) {
        categoryDao.insertAppMetaData(relationship) != -1L
    }

    @WorkerThread
    internal suspend fun addAppsToCategory(relationshipList: ArrayList<CategoryAppMetaDataCrossRef>) {
        withContext(ioDispatcher) {
            categoryDao.insertAppMetaData(relationshipList)
        }
    }

    @WorkerThread
    internal suspend fun loadPackageHashcodeList(messageCategoryId: String): List<String> =
        withContext(ioDispatcher) {
            categoryDao.loadPackageHashcodeList(messageCategoryId)
        }

    @WorkerThread
    internal suspend fun removeAppFromCategory(relationship: CategoryAppMetaDataCrossRef) {
        withContext(ioDispatcher) {
            categoryDao.removeAppMetaData(relationship)
        }
    }

    @WorkerThread
    internal suspend fun removeAppsFromCategory(relationshipList: ArrayList<CategoryAppMetaDataCrossRef>) {
        withContext(ioDispatcher) {
            categoryDao.removeAppMetaData(relationshipList)
        }
    }

    @WorkerThread
    internal suspend fun logAppMetaData(metaData: AppMetaData): Boolean =
        withContext(ioDispatcher) {
            checkInsert(appMetaDataDao.insertAppMetaData(metaData))
        }

    @WorkerThread
    internal suspend fun logAppMetaData(list: List<AppMetaData>) {
        withContext(ioDispatcher) {
            appMetaDataDao.insertAppMetaData(list)
        }
    }

    @WorkerThread
    internal suspend fun loadAppMetaData(): List<AppMetaData> = withContext(ioDispatcher) {
        appMetaDataDao.loadAllAppMetaData()
    }

    internal suspend fun getAppMetaData(packageHashcode: String): AppMetaData? =
        withContext(ioDispatcher) {
            appMetaDataDao.loadAppMetaData(packageHashcode)
        }

    @WorkerThread
    internal suspend fun updateAppMetaData(appMetaData: AppMetaData): Boolean =
        withContext(ioDispatcher) {
            appMetaDataDao.updateAppMetaData(appMetaData) != 0
        }

    @WorkerThread
    internal suspend fun updateLogOngoingAllApps(list: List<AppMetaData>) =
        withContext(ioDispatcher) {
            appMetaDataDao.updateLogOngoingAllApps(list)
        }

    @WorkerThread
    internal suspend fun deleteApp(appMetaData: AppMetaData) = withContext(ioDispatcher) {
        val result = appMetaDataDao.deleteApp(appMetaData) != 0
        if (result) {
            LogHelper.deletePackageFolder(applicationContext, appMetaData.packageHashcode)
        }
        result
    }

    ////////////////////////////////////////////////////////////////////////////////
    @WorkerThread
    internal suspend fun logBigTextStyle(bigTextNotificationStyle: BigTextNotificationStyle): Boolean =
        withContext(ioDispatcher) {
            checkInsert(notificationDao.insertBigTextNotificationStyle(bigTextNotificationStyle))
        }

    @WorkerThread
    internal suspend fun logBigPictureStyle(bigPictureNotificationStyle: BigPictureNotificationStyle): Boolean =
        withContext(ioDispatcher) {
            checkInsert(
                notificationDao.insertBigPictureNotificationStyle(
                    bigPictureNotificationStyle
                )
            )
        }

    @WorkerThread
    internal suspend fun logBaseNotificationStyle(style: BaseNotificationStyle): Boolean =
        withContext(ioDispatcher) {
            checkInsert(notificationDao.insertBaseNotificationStyle(style))
        }

    @WorkerThread
    internal suspend fun logNotification(on: ONotification): Boolean = withContext(ioDispatcher) {
        checkInsert(database.notificationLogDao().insertONotification(on))
    }

    internal fun searchNStatusBarGroupPaging(
        searchValue: String,
        sortMode: NotisaveDatabase.SortMode,
        packageHashcode: List<String>? = null
    ): Flow<PagingData<NStatusBarGroup>> {
        val querySupport = RoomQuerySupport.buildNStatusBarGroupQuery(
            searchValue, sortMode, packageHashcode
        )
        return Pager(
            createPageConfig(false)
        ) {
            notificationDao.loadNStatusBarGroupPaging(querySupport)
        }.flow
    }

    internal fun searchNotificationPaging(
        sbnKeyHashcode: String,
        searchValue: String
    ): Flow<PagingData<NotificationLog>> {
        return Pager(
            createPageConfig(false)
        ) {
            notificationDao.loadNotificationPaging(sbnKeyHashcode, searchValue)
        }.flow
    }

    internal fun searchNotificationPaging(
        searchValue: String,
        sortMode: NotisaveDatabase.SortMode,
        packageHashcodeList: List<String>? = null,
    ): Flow<PagingData<NotificationLog>> {
        val supportQuery = RoomQuerySupport.buildNotificationQuery(
            searchValue, sortMode, packageHashcodeList
        )
        return Pager(
            createPageConfig(true)
        ) {
            notificationDao.loadNotificationPaging(supportQuery)
        }.flow
    }

    internal fun searchNStatusBarGroupPaging(
        packageHashcode: String,
        queryString: String
    ): Flow<PagingData<NStatusBarGroup>> {
        return Pager(
            config = createPageConfig(true)
        ) {
            notificationDao.searchNotificationPaging(packageHashcode, queryString)
        }.flow
    }

    internal fun searchPackagePaging(
        searchValue: String,
        sortMode: NotisaveDatabase.SortMode = NotisaveDatabase.SortMode.DESC,
        packageHashcodeList: List<String>? = null
    ): Flow<PagingData<NotificationPackage>> {

        return Pager(
            config = PagingConfig(
                NotisaveDatabase.DEFAULT_PER_PAGE_PACKAGE_GROUP,
                enablePlaceholders = false
            )
        ) {

            PackagePagingSource(
                database,
                ioDispatcher,
                PackagePagingSource.ConditionArguments(
                    searchValue, sortMode, packageHashcodeList
                )
            )
        }.flow
    }

    @WorkerThread
    internal fun getNCountOfPackage(packageHashcode: String): Flow<Int> {
        return notificationDao.loadNCountOfPackage(packageHashcode)
    }

    internal suspend fun getCountNotification(): Int = withContext(ioDispatcher) {
        notificationDao.getCountNotification()
    }

    @WorkerThread
    internal suspend fun loadLogIds(
        sbnKeyHashcode: String,
        topTimePost: Long,
        searchValue: String
    ): List<String> =
        withContext(ioDispatcher) {
            notificationDao.loadLogIds(sbnKeyHashcode, topTimePost, searchValue)
        }

    @WorkerThread
    internal fun loadAppTemplate(): Flow<List<AppTemplate>> = appMetaDataDao.loadAppTemplate()

    @WorkerThread
    internal suspend fun deleteNotification(on: ONotification): Boolean  = withContext(ioDispatcher) {
        val packageDir = LogHelper.getPackageHashcodeFolder(applicationContext, on.packageHashcode)

        deleteNotificationWithId(packageDir, on.logId)
    }

    @WorkerThread
    internal suspend fun deleteNotifications(notificationList: List<NotificationLog>) {
        withContext(ioDispatcher) {
            notificationList.forEach {
                ensureActive()
                deleteNotificationWithId(
                    LogHelper.getPackageHashcodeFolder(applicationContext, it.packageHashcode), it.logId
                )
            }
        }
    }

    internal suspend fun deleteNotifications(nList: List<NStatusBarGroup>, searchValue: String) =
        withContext(ioDispatcher) {
            nList.onEach {
                val header = it.header
                val packageFolder = LogHelper.getPackageHashcodeFolder(applicationContext, it.packageHashcode)

                val logIds = loadLogIds(header.sbnKeyHashcode, header.timePost, searchValue)

                logIds.onEach { logId ->

                    ensureActive()
                    deleteNotificationWithId(packageFolder, logId)
                }
            }
        }

    private suspend fun deleteNotificationWithId(packageFolder: File, logId: String): Boolean {
        val arrSql = RoomQuerySupport.buildDeleteNotificationWithId()
        return database.withTransaction {
            val result = getWriteDatabase().delete(arrSql[0], arrSql[1], arrayOf(logId)) != 0

            if (result) {
                LogHelper.deleteFileOfNotification(packageFolder, logId)
            }

            return@withTransaction result
        }
    }


    internal suspend fun clearNotification(packageHashcode: String) = withContext(ioDispatcher) {
        deleteNotificationWithPackage(
            packageHashcode,
            LogHelper.getPackageHashcodeFolder(applicationContext, packageHashcode)
        )
    }

    private suspend fun deleteNotificationWithPackage(packageHashcode: String, packageDir: File) {

        while (true) {
            val list = notificationDao.loadLimitedLogIds(packageHashcode, 1000)
            if (list.isEmpty()) break
            list.forEach { logId ->
                coroutineContext.ensureActive()
                deleteNotificationWithId(packageDir, logId)
            }
        }
    }

    internal suspend fun deletePackage(packageHashcode: String) = withContext(ioDispatcher) {
        val packageDir = LogHelper.getPackageHashcodeFolder(applicationContext, packageHashcode)

        deleteNotificationWithPackage(packageHashcode, packageDir)

        database.withTransaction {
            val appSql = RoomQuerySupport.buildDeleteAppMeta()
            val categorySql = RoomQuerySupport.buildDeleteAppInCategory()

            val writeDatabase = getWriteDatabase()

            writeDatabase.delete(
                categorySql[0], categorySql[1], arrayOf(packageHashcode)
            )

            writeDatabase.delete(
                appSql[0], appSql[1], arrayOf(packageHashcode)
            )

            packageDir.deleteRecursively()
        }
    }

    private fun getWriteDatabase() = database.openHelper.writableDatabase

    private val notificationDao
        get() = database.notificationLogDao()

    private val appMetaDataDao
        get() = database.appMetaDataDao()

    private val categoryDao
        get() = database.categoryDao()

    private fun checkInsert(rowId: Long): Boolean {
        return rowId != -1L
    }

    internal suspend fun getNotificationLog(nLogId: String) = withContext(ioDispatcher) {
        notificationDao.getNotificationLog(nLogId)
    }
}