package com.notisaver.database

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.paging.util.INITIAL_ITEM_COUNT
import androidx.room.paging.util.INVALID
import androidx.room.paging.util.ThreadSafeInvalidationObserver
import androidx.room.paging.util.getClippedRefreshKey
import androidx.room.paging.util.getLimit
import androidx.room.paging.util.getOffset
import androidx.room.withTransaction
import com.notisaver.database.entities.NotificationPackage
import com.notisaver.database.entities.ShortNotification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class PackagePagingSource(
    private val database: NotisaveDatabase,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val argument: ConditionArguments,
) : PagingSource<Int, NotificationPackage>() {

    internal val itemCount: AtomicInteger = AtomicInteger(INITIAL_ITEM_COUNT)

    private val observer = ThreadSafeInvalidationObserver(
        tables = arrayOf("ONotification"),
        onInvalidated = ::invalidate
    )

    override fun getRefreshKey(state: PagingState<Int, NotificationPackage>): Int? {
        return state.getClippedRefreshKey()
    }

    override val jumpingSupported: Boolean
        get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NotificationPackage> {
        return withContext(workerDispatcher) {
            observer.registerIfNecessary(database)
            val tempCount = itemCount.get()

            if (tempCount == INITIAL_ITEM_COUNT) {
                initialLoad(params)
            } else {
                nonInitialLoad(params, tempCount)
            }
        }
    }

    private suspend fun initialLoad(params: LoadParams<Int>): LoadResult<Int, NotificationPackage> {
        return database.withTransaction {

            val tempCount = argument.packageHashcodeList?.let {
                notificationLogDao.getNumberPackage(it, argument.searchValue)
            } ?: notificationLogDao.getNumberPackage(argument.searchValue)

            itemCount.set(tempCount)
            queryDatabase(params, tempCount)
        }
    }

    private suspend fun nonInitialLoad(
        params: LoadParams<Int>,
        tempCount: Int
    ): LoadResult<Int, NotificationPackage> {
        val loadResult = queryDatabase(params, tempCount)

        database.invalidationTracker.refreshVersionsSync()
        @Suppress("UNCHECKED_CAST")
        return if (invalid) INVALID as LoadResult.Invalid<Int, NotificationPackage> else loadResult
    }

    private suspend fun queryDatabase(
        params: LoadParams<Int>,
        itemCount: Int
    ): LoadResult<Int, NotificationPackage> {
        val key = params.key ?: 0
        val limit = getLimit(params, key)
        val offset = getOffset(params, key, itemCount)

        val supportQuery = RoomQuerySupport.buildNotificationPageQuery(
            limit, offset, argument.searchValue, argument.sortMode, argument.packageHashcodeList
        )

        val nList = mapNotificationList(notificationLogDao.searchNotificationPackage(supportQuery))


        val data = when (argument.sortMode) {
            NotisaveDatabase.SortMode.DESC -> nList.sortedByDescending { it.timePost }
            NotisaveDatabase.SortMode.ASC -> nList.sortedBy { it.timePost }
        }

        val nextPosToLoad = offset + data.size

        val nextKey =
            if (data.isEmpty() || data.size < limit || nextPosToLoad >= itemCount) {
                null
            } else {
                nextPosToLoad
            }

        val prevKey = if (offset <= 0 || data.isEmpty()) null else offset

        return LoadResult.Page(
            data = data,
            prevKey = prevKey,
            nextKey = nextKey,
            itemsBefore = offset,
            itemsAfter = maxOf(0, itemCount - nextPosToLoad)
        )
    }

    private fun mapNotificationList(dataList: List<ShortNotification>): List<NotificationPackage> {
        val packageMap = LinkedHashMap<String, NotificationPackage>()

        dataList.forEach {
            var nPackage = packageMap[it.packageHashcode]
            if (nPackage == null) {
                nPackage = NotificationPackage(it.packageHashcode, it.timePost)
                packageMap[it.packageHashcode] = nPackage
            }
            nPackage.addNotification(it)
        }

        return packageMap.values.toList()
    }

    private val notificationLogDao
        get() = database.notificationLogDao()


    data class ConditionArguments(
        val searchValue: String,
        val sortMode: NotisaveDatabase.SortMode,
        val packageHashcodeList: List<String>?
    )
}