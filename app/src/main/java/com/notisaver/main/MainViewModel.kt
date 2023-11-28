package com.notisaver.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notisaver.database.NotisaveRepository
import com.notisaver.main.log.viewmodels.NotificationViewModel
import com.notisaver.main.log.misc.ISearchSupport
import com.notisaver.main.log.misc.ISelectionSupport
import com.notisaver.main.log.misc.SearchSupport
import com.notisaver.main.log.misc.SelectionSupport
import com.notisaver.main.manager.AppInfoManager

class MainViewModel(
    repository: NotisaveRepository,
    appInfoManager: AppInfoManager
) : NotificationViewModel(repository, appInfoManager),
    ISelectionSupport, ISearchSupport {
    private val supportSelection = SelectionSupport()
    private val supportSearch = SearchSupport()

    internal fun setSelectionObserver(selectionObserver: ISelectionSupport.ISelectionObserver?) {
        supportSelection.selectionObserver?.let {
            if (it != selectionObserver) {
                cancelSelectionObserver(it)
            }
        }
        supportSelection.selectionObserver = selectionObserver
    }

    internal fun setSelectionTrigger(trigger: ISelectionSupport.ITriggerSelectionMode) {
        supportSelection.trigger = trigger
    }

    internal val isSelecting
        get() = supportSelection.trigger?.isSelecting == true

    override fun turnOnSelectionMode() {
        supportSelection.turnOnSelectionMode()
    }

    override fun notifySelectedCountChange(count: Int) {
        supportSelection.notifySelectedCountChange(count)
    }

    override fun closeSelection() {
        supportSelection.closeSelection()
    }

    override fun performSelectionDelete() {
        supportSelection.performSelectionDelete()
    }

    internal fun cancelSelectionObserver(selectionObserver: ISelectionSupport.ISelectionObserver) {
        if (supportSelection.selectionObserver == selectionObserver) {
            closeSelection()
            supportSelection.selectionObserver = null
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    internal fun setSearchObserver(searchObserver: ISearchSupport.ISearchObserver) {
        supportSearch.searchObserver?.let {
            if (searchObserver != it) {
                cancelSearchObserver(it)
            }
        }
        supportSearch.searchObserver = searchObserver
    }

    internal fun setSearchTrigger(trigger: ISearchSupport.ITriggerSearchObserver) {
        supportSearch.trigger = trigger
    }

    internal val isSearching
        get() = supportSearch.trigger?.isSearching == true

    override fun turnOnSearchMode() {
        supportSearch.turnOnSearchMode()
    }

    override fun closeSearch() {
        supportSearch.closeSearch()
    }

    override fun submitQuery(submit: String?) {
        supportSearch.submitQuery(submit)
    }

    override fun onQueryChange(newText: String?) {
        supportSearch.onQueryChange(newText)
    }

    internal fun cancelSearchObserver(searchObserver: ISearchSupport.ISearchObserver) {
        if (supportSearch.searchObserver == searchObserver) {
            closeSearch()
            supportSearch.searchObserver = null
        }
    }

    class Factory(
        private val notisaveApplication: NotisaveApplication,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(
                    notisaveApplication.notisaveRepository,
                    notisaveApplication.getAppInfoManager()
                ) as T
            }
            throw ClassCastException()
        }
    }
}