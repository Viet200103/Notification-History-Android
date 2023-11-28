package com.notisaver.main.log.intefaces

import com.notisaver.database.entities.NStatusBarGroup

interface OnStatusBarNotificationClickListener {
    fun onStatusBarNotificationClick(nStatusBarGroup: NStatusBarGroup)
}

interface OnPackageClickListener {

    fun onNotificationClick(notificationItem: NotificationItem)

    fun onPackageClick(packageHashcode: String, notificationItem: NotificationItem)
}