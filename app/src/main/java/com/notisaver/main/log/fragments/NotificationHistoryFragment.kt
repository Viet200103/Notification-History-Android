package com.notisaver.main.log.fragments

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import com.notisaver.R
import com.notisaver.database.NotisaveDatabase.Companion.HISTORY_CATEGORY_ID
import com.notisaver.main.MainViewModel
import com.notisaver.main.log.misc.NotificationFormat
import com.notisaver.misc.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest

class NotificationHistoryFragment : NotificationListFragment(), MenuProvider {

//    private val notificationViewModel by viewModels<MainViewModel>(this::requireActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectWhenStarted {
            loadingFlow.collectLatest {
                if (it) {
                    bindContent()
                }
                refreshLayout.isRefreshing = it
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun bindContent() {
        dataJob?.cancel()
        contentFlow = searchStateFlow.flatMapLatest {searchValue ->
            val sortMode = notificationFormat.getSortMode(getCategoryId())

            when (notificationFormat.getForm(getCategoryId())) {
                NotificationFormat.Form.PACKAGE -> {
                    notisaveRepository.searchPackagePaging(
                        searchValue,
                        sortMode
                    )
                }
                NotificationFormat.Form.NONE -> {
                    notisaveRepository.searchNotificationPaging(
                        searchValue, sortMode
                    )
                }
                NotificationFormat.Form.GROUP -> notisaveRepository.searchNStatusBarGroupPaging(searchValue, sortMode)
            }

        }
        bindNotificationData()
    }

    override fun getCategoryId(): String {
        return HISTORY_CATEGORY_ID
    }

    override fun onEmptyState(isEmpty: Boolean) {
        emptyView.isInvisible = !isEmpty
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (super.onMenuItemSelected(menuItem)) {
            return true
        }

        when(menuItem.itemId) {
            R.id.menu_notification_history_search -> {
                notificationViewModel.turnOnSearchMode()
            }
            else -> return false
        }

        return true
    }


    override fun getViewModel(): MainViewModel {
        return notificationViewModel
    }
}