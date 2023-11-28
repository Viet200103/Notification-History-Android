package com.notisaver.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomQuerySupportTest {

    @Test
    fun test_notification_package_query() {
        val query = RoomQuerySupport.buildNotificationPageQuery(
            1, 5, "SearchTest", NotisaveDatabase.SortMode.ASC,
            listOf("123", "124", "125")
        )

        Assert.assertEquals(
            query.argCount, 15
        )
    }

    @Test
    fun test_notification_status_bar_group_query() {
        val query = RoomQuerySupport.buildNStatusBarGroupQuery(
            "SearchTest", NotisaveDatabase.SortMode.ASC, listOf("123", "124", "125")
        )

        Assert.assertEquals(
            query.argCount, 8
        )
    }
}