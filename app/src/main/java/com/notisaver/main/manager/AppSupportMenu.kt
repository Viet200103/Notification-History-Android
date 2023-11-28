package com.notisaver.main.manager

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuCompat
import com.notisaver.R
import com.notisaver.database.entities.AppMetaData

class AppSupportMenu(
    context: Context,
    view: View,
) {

    private val popupMenu = PopupMenu(context, view).apply {
        inflate(R.menu.menu_app_options)
        MenuCompat.setGroupDividerEnabled(menu, true)
        setForceShowIcon(true)
    }

    internal fun setOnItemClickListener(listener: PopupMenu.OnMenuItemClickListener) {
        popupMenu.setOnMenuItemClickListener(listener)
    }

    internal fun updateOption(appMetaData: AppMetaData?) {
        popupMenu.menu.let {
            val include = it.findItem(R.id.menu_app_option_item_include)
            val exclude = it.findItem(R.id.menu_app_option_item_exclude)
            val cleanable = it.findItem(R.id.menu_app_option_item_enable_cancel_when_push)
            val disCleanable = it.findItem(R.id.menu_app_option_item_disable_cancel_when_push)
            val logOngoing = it.findItem(R.id.menu_app_option_item_enable_log_ongoing)
            val disLogOngoing = it.findItem(R.id.menu_app_option_item_disable_log_ongoing)

            if (appMetaData == null) {
                include.isVisible = false
                exclude.isVisible = false
                cleanable.isVisible = false
                disCleanable.isVisible = false
                return
            }

            if (appMetaData.isTracking) {
                include.isVisible = false
                exclude.isVisible = true
            } else {
                include.isVisible = true
                exclude.isVisible = false
            }

            if (appMetaData.isCleanable) {
                cleanable.isVisible = false
                disCleanable.isVisible = true
            } else {
                cleanable.isVisible = true
                disCleanable.isVisible = false
            }

            if (appMetaData.isLogOnGoing) {
                disLogOngoing.isVisible = true
                logOngoing.isVisible = false
            } else {
                disLogOngoing.isVisible = false
                logOngoing.isVisible = true
            }
        }
    }

    internal fun show() {
        popupMenu.show()
    }

    internal fun getMenu() = popupMenu.menu
}