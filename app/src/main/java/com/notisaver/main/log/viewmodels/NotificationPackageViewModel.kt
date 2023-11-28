package com.notisaver.main.log.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.NStatusBarGroup
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.log.misc.ISelectionSupport
import com.notisaver.main.log.misc.SelectionSupport
import com.notisaver.main.manager.AppInfoManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationPackageViewModel(
    repository: NotisaveRepository,
    appInfoManager: AppInfoManager
) : NotificationViewModel(repository, appInfoManager), ISelectionSupport {

    private val supportSelection = SelectionSupport()

    private val _packageHashcodeFlow = MutableStateFlow(String())

    private val _searchStateFlow = MutableStateFlow(String())
    internal val searchStateFlow = _searchStateFlow.asStateFlow()

    internal val appInfoFlow = _packageHashcodeFlow.map {packageHashcode ->
        appInfoManager.getAppInformationLazyCache(packageHashcode)
    }

    internal val emptyStateFlow = MutableStateFlow(false)

    @ExperimentalCoroutinesApi
    internal val pagingDataFlow: Flow<PagingData<NStatusBarGroup>> =
        _searchStateFlow.flatMapLatest {
            searchRepo(_packageHashcodeFlow.value, it)
        }

    @ExperimentalCoroutinesApi
    internal val nCountFlow: StateFlow<Int> = _packageHashcodeFlow.flatMapLatest {
        repository.getNCountOfPackage(it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    private fun searchRepo(packageHashcode: String, queryString: String) =
        repository.searchNStatusBarGroupPaging(packageHashcode, queryString)

    internal fun updatePackage(packageHashcode: String) {
        viewModelScope.launch {
            _packageHashcodeFlow.emit(packageHashcode)
            _searchStateFlow.emit("")
        }
    }

    internal fun searchNotification(searchQuery: String) {
        _searchStateFlow.value = searchQuery
    }

    internal fun setSelectionObserver(selectionObserver: ISelectionSupport.ISelectionObserver?) {
        supportSelection.selectionObserver = selectionObserver
    }

    internal fun setTrigger(trigger: ISelectionSupport.ITriggerSelectionMode) {
        supportSelection.trigger = trigger
    }

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

    class Factory(
        private val notisaveApplication: NotisaveApplication,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationPackageViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotificationPackageViewModel(
                    notisaveApplication.notisaveRepository,
                    notisaveApplication.getAppInfoManager()
                ) as T
            }
            throw ClassCastException()
        }
    }
}
