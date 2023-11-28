package com.notisaver.main.log.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.button.MaterialButton
import com.notisaver.R
import com.notisaver.database.NotisaveDatabase
import com.notisaver.main.MainViewModel
import com.notisaver.main.log.misc.NotificationFormat
import com.notisaver.misc.collectWhenStarted
import com.notisaver.misc.findView
import com.notisaver.misc.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest

class NotificationMessageFragment : NotificationListFragment() {

    private var packageHashcodeList: List<String> = listOf()

    private lateinit var groupContainerLayout: LinearLayoutCompat
    private lateinit var addGroupButton: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().addMenuProvider(this, this, Lifecycle.State.RESUMED)

        collectWhenStarted {
            loadingFlow.collectLatest {
                if (it) {
                    loadData()
                }
                refreshLayout.isRefreshing = it
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LayoutInflater.from(view.context).inflate(
            R.layout.layout_add_to_group, rootLayout, true
        )

        groupContainerLayout = findView(R.id.layout_add_to_group_add_container_layout)
        addGroupButton = findView(R.id.layout_add_to_group_add_button)
        addGroupButton.setOnClickListener {
            openGroupDialog()
        }
    }

    override fun getCategoryId(): String {
        return NotisaveDatabase.MESSAGE_CATEGORY_ID
    }

    override fun getViewModel(): MainViewModel {
        return notificationViewModel
    }

    private fun loadData() = launch(Dispatchers.IO) {
        loadPackages()
        bindContent()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun bindContent() {
        dataJob?.cancel()

        contentFlow = searchStateFlow.flatMapLatest { searchValue ->

            val sortMode = notificationFormat.getSortMode(getCategoryId())

            when (notificationFormat.getForm(getCategoryId())) {
                NotificationFormat.Form.PACKAGE -> {
                    notisaveRepository.searchPackagePaging(
                        searchValue,
                        sortMode,
                        packageHashcodeList
                    )
                }

                NotificationFormat.Form.NONE -> {
                    notisaveRepository.searchNotificationPaging(
                        searchValue, sortMode, packageHashcodeList
                    )
                }

                NotificationFormat.Form.GROUP -> notisaveRepository.searchNStatusBarGroupPaging(
                    searchValue,
                    sortMode,
                    packageHashcodeList
                )
            }
        }

        bindNotificationData()
    }

    private suspend fun loadPackages() {
        packageHashcodeList =
            notisaveRepository.loadPackageHashcodeList(NotisaveDatabase.MESSAGE_CATEGORY_ID)
    }

    override fun onEmptyState(isEmpty: Boolean) {
        if (isEmpty && searchStateFlow.value.isEmpty()) {
            groupContainerLayout.isVisible = true
            emptyView.isInvisible = true
        } else {
            emptyView.isInvisible = !isEmpty
            groupContainerLayout.isInvisible = true
        }
    }

    private fun openGroupDialog() {
        MessageGroupDialogFragment().show(
            childFragmentManager, MessageGroupDialogFragment::class.java.name
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        menu.findItem(R.id.menu_message_support_add_group).isVisible = true
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (super.onMenuItemSelected(menuItem)) {
            return true
        }
        when (menuItem.itemId) {
            R.id.menu_message_support_add_group -> {
                openGroupDialog()
            }

            R.id.menu_notification_history_search -> {
                notificationViewModel.turnOnSearchMode()
            }
        }
        return true
    }
}