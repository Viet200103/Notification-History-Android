package com.notisaver.main.log.core

import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.AppMetaData
import java.util.concurrent.ConcurrentHashMap

class AppMetaCacheManager private constructor(
    private val repository: NotisaveRepository
) {

    private val metaCache = ConcurrentHashMap<String, AppMetaData>();

    private fun getAppMetaDataInCache(packageHashcode: String) = metaCache[packageHashcode]

    internal suspend fun getAppMetaData(packageHashcode: String): AppMetaData? {
        return getAppMetaDataInCache(packageHashcode) ?: repository.getAppMetaData(packageHashcode)
    }

    internal suspend fun logAppMetaData(app: AppMetaData) {
        repository.logAppMetaData(app)
        metaCache[app.packageHashcode] = app
    }

    companion object {

        private var INSTANCE: AppMetaCacheManager? = null

        internal fun getInstance(repository: NotisaveRepository): AppMetaCacheManager {

            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = AppMetaCacheManager(repository)
                }
            }

            return INSTANCE!!
        }
    }


}