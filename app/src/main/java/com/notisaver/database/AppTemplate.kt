package com.notisaver.database

import androidx.room.DatabaseView

@DatabaseView(
    "SELECT a.packageHashcode as aPackageHashcode, count(o.packageHashcode) as nCount FROM appmetadata AS a LEFT JOIN ONotification AS o ON a.packageHashcode = o.packageHashcode  GROUP BY a.packageHashcode ORDER BY nCount DESC"
)
data class AppTemplate(
    val aPackageHashcode: String,
    val nCount: Int
)