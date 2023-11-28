package com.notisaver.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.NotificationDataTest
import com.notisaver.database.daos.AppMetaDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config
@RunWith(AndroidJUnit4::class)
internal class AppMetadataDaoTest {

    @Test
    fun test_insertAppMetaData() {
        val db: NotisaveDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), NotisaveDatabase::class.java
        ).build()

        val appDao: AppMetaDataDao = db.appMetaDataDao()

        val appMeta = NotificationDataTest.createAppMetaData(
            packageHashCode = "com.appmeta.hashcode",
            packageName = "com.appmeta",
            isCleanable = false,
            isTracking = false,
            isLogOnGoing = false
        )

        runBlocking(Dispatchers.IO) {
            appDao.insertAppMetaData(appMeta)

            val expectedApp = appDao.loadAppMetaData("com.appmeta.hashcode")

            Assert.assertNotNull(appMeta)

            Assert.assertEquals(
                expectedApp?.packageHashcode, "com.appmeta.hashcode"
            )

            Assert.assertEquals(
                expectedApp?.packageName, "com.appmeta"
            )
        }
    }

    @Test
    fun test_updateAppMetaData() {
        val db: NotisaveDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), NotisaveDatabase::class.java
        ).build()

        val appDao: AppMetaDataDao = db.appMetaDataDao()

        val appMeta = NotificationDataTest.createAppMetaData(
            packageHashCode = "com.appmeta.hashcode",
            packageName = "com.appmeta",
            isCleanable = false,
            isTracking = false,
            isLogOnGoing = false
        )

        runBlocking(Dispatchers.IO) {
            appDao.insertAppMetaData(appMeta)

            appMeta.isCleanable = true
            appMeta.isTracking = true
            appMeta.isLogOnGoing = true

            appDao.updateAppMetaData(appMeta)

            val expectedApp = appDao.loadAppMetaData("com.appmeta.hashcode")

            Assert.assertEquals(
                expectedApp?.isTracking, true
            )

            Assert.assertEquals(
                expectedApp?.isCleanable, true
            )

            Assert.assertEquals(
                expectedApp?.isLogOnGoing, true
            )
        }
    }


    @Test
    fun test_deleteApp() {
        val db: NotisaveDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), NotisaveDatabase::class.java
        ).build()

        val appDao: AppMetaDataDao = db.appMetaDataDao()

        val appMeta = NotificationDataTest.createAppMetaData(
            packageHashCode = "com.appmeta.hashcode",
            packageName = "com.appmeta",
            isCleanable = false,
            isTracking = false,
            isLogOnGoing = false
        )

        runBlocking(Dispatchers.IO){
            appDao.insertAppMetaData(appMeta)

            appDao.deleteApp(appMeta)

            Assert.assertNull(
                appDao.loadAppMetaData("com.appmeta.hashcode")
            )
        }
    }

}