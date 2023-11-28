package com.notisaver.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.database.entities.AppMetaData
import com.notisaver.database.entities.ONotification
import com.notisaver.misc.createPackageHashcode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@Config
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class AppTemplateTest {

    private lateinit var repository: NotisaveRepository
    private lateinit var db: NotisaveDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, NotisaveDatabase::class.java
        ).build()
        repository = NotisaveRepository(
            context, db, UnconfinedTestDispatcher()
        )
    }

    @Test
    fun load_AppTemplate() = runTest(UnconfinedTestDispatcher()){
        repository.logAppMetaData(
            listOf(
                AppMetaData(
                    packageHashcode = "com.test.template.1.hashcode",
                    packageName = "com.test.template.1"
                ),
                AppMetaData(
                    packageHashcode = "com.test.template.2",
                    packageName = "com.test.template.2"
                ),
            )
        )

        repository.logNotification(
            ONotification(
                sbnKeyHashcode = "hashcodeTest",
                timePost = 1234567890,
                logId = "logIdTest",
                title = "Test",
                contentText = "contentTest",
                packageHashcode = "com.test.template.1.hashcode"
            )
        )

        repository.logNotification(
            ONotification(
                sbnKeyHashcode = "hashcodeTest2",
                timePost = 1234567890,
                logId = "logIdTest2",
                title = "Test2",
                contentText = "contentTest2",
                packageHashcode = "com.test.template.1.hashcode"
            )
        )

        repository.loadAppTemplate().take(1).collect {
            Assert.assertTrue(
                it.size ==2
            )

            Assert.assertEquals(
                it[0].aPackageHashcode, "com.test.template.1.hashcode"
            )

            Assert.assertEquals(
                it[0].nCount,2
            )

            Assert.assertEquals(
                it[1].nCount,0
            )
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}