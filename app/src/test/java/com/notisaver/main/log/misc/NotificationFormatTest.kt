package com.notisaver.main.log.misc

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.database.NotisaveDatabase
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationFormatTest {

    @Test
    fun test_default_sort_mode() {
        val format = NotificationFormat(
            ApplicationProvider.getApplicationContext()
        )

        format.clear()

        Assert.assertEquals(
            format.getSortMode("Test"), NotisaveDatabase.SortMode.DESC
        )
    }

    @Test
    fun test_set_sort_mode() {
        val format = NotificationFormat(
            ApplicationProvider.getApplicationContext()
        )

        format.setSortMode(
            "Test",
            NotisaveDatabase.SortMode.ASC
        )

        Assert.assertEquals(
            format.getSortMode("Test"), NotisaveDatabase.SortMode.ASC
        )

        format.setSortMode(
            "Test",
            NotisaveDatabase.SortMode.DESC
        )

        Assert.assertEquals(
            format.getSortMode("Test"), NotisaveDatabase.SortMode.DESC
        )
    }
}