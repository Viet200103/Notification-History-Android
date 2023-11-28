package com.notisaver.main.log.intefaces

interface NotificationItem {
    val id: CharSequence
    val packageHashcode: String
    override fun equals(other: Any?): Boolean
}