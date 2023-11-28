package com.notisaver.main.log.main

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.database.NotisaveDatabase
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.AppMetaData
import com.notisaver.main.log.core.AppMetaCacheManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config
@RunWith(AndroidJUnit4::class)
internal class AppMetaCacheManagerTest {

    private lateinit var repository: NotisaveRepository
    private lateinit var database: NotisaveDatabase

    private lateinit var manager: AppMetaCacheManager

    @Before
    fun onCreate() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, NotisaveDatabase::class.java
        ).build()

        repository = NotisaveRepository(
            context, database
        )

        manager = AppMetaCacheManager.getInstance(repository)
    }

    @Test
    fun test_get_app_null() {
        val app: AppMetaData?
        runBlocking {
            app = repository.getAppMetaData("123456789")
        }

        Assert.assertEquals(app, null)
    }

    @Test
    fun test_log_app_get_not_null() {
        val app = AppMetaData(
            "123456789",
            "123456789_package_name"
        )

        val appLogged: AppMetaData?
        runBlocking {
            manager.logAppMetaData(app)
            appLogged = manager.getAppMetaData("123456789")
        }

        Assert.assertEquals(appLogged?.packageHashcode, "123456789")
    }

    @After
    fun onClose() {
        database.close()
    }
}