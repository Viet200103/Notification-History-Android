package com.notisaver.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.notisaver.database.daos.AppMetaDataDao
import com.notisaver.database.daos.CategoryDao
import com.notisaver.database.daos.NotificationLogDao
import com.notisaver.database.entities.*
import com.notisaver.database.extra_relationships.CategoryAppMetaDataCrossRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        NotificationCategory::class, CategoryAppMetaDataCrossRef::class, AppMetaData::class,
        ONotification::class, BaseNotificationStyle::class, BigPictureNotificationStyle::class,
        BigTextNotificationStyle::class,
    ],
    views = [AppTemplate::class],
    exportSchema = true,
    version = 1
)
abstract class NotisaveDatabase : RoomDatabase() {

    abstract fun notificationLogDao(): NotificationLogDao
    abstract fun appMetaDataDao(): AppMetaDataDao
    abstract fun categoryDao(): CategoryDao

    enum class SortMode(val sortValue: Int){
        ASC(0), DESC(1),
    }

    companion object {

        private const val databaseName = "NotisaveDatabase"

        internal const val COL_LOG_ID = "logId"
        internal const val COL_CATEGORY_ID = "categoryId"
        internal const val COL_PACKAGE_HASHCODE = "packageHashcode"
        internal const val COL_STYLE_ID = "styleId"
        internal const val COL_PACKAGE = "packageName"
        internal const val COL_TIME_POST = "timePost"
        internal const val COL_SBN_KEY_HASHCODE = "sbnKeyHashcode"
        internal const val COL_TRACING = "isTracking"
        internal const val COL_ONGOING = "isOngoing"
        internal const val COL_CLEANABLE = "isCleanable"

        internal const val MESSAGE_CATEGORY_ID = "msg_app"
        internal const val HISTORY_CATEGORY_ID = "history"

        internal const val DEFAULT_PER_PAGE = 15
        internal const val DEFAULT_PER_PAGE_PACKAGE_GROUP = 5

        @Volatile
        private var INSTANCE: NotisaveDatabase? = null

        @JvmStatic
        internal fun getDatabase(context: Context): NotisaveDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotisaveDatabase::class.java,
                    databaseName
                ).addCallback(
                    RoomCallback(CoroutineScope(SupervisorJob()))
                ).build()
                INSTANCE = instance

                instance
            }
        }

        class RoomCallback(
            private val dbScope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let {
                    dbScope.launch {
                        it.categoryDao().insertCategory(
                            NotificationCategory(
                                MESSAGE_CATEGORY_ID, ""
                            )
                        )
                    }
                }
            }
        }
    }
}