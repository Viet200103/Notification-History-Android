package com.notisaver.main.log.viewmodels

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notisaver.R
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.NStatusBarGroup
import com.notisaver.database.entities.NotificationLog
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.manager.AppInfoManager
import com.notisaver.misc.WaitingProcessDialog
import com.notisaver.misc.collectWhenCreated
import com.notisaver.misc.collectWhenStarted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.jvm.Throws

open class NotificationViewModel(
    val repository: NotisaveRepository,
    val appInfoManager: AppInfoManager
) : ViewModel() {

    private var _deleteProcessListener: OnProcessStateListener? = null

    private var _deleteProcessJob: Job? = null
    private val _deleteProcessFlow = MutableStateFlow(ProcessState.INIT)
    internal val deleteProcessFlow = _deleteProcessFlow.asStateFlow()

    enum class ProcessState {
        INIT, PROCESSING, COMPLETED, CANCELED,
    }

    @Throws
    internal fun deleteNotification(nList: List<NStatusBarGroup>, searchQuery: String) {
        deleteWithFLow {
            repository.deleteNotifications(nList, searchQuery)
        }
    }

    internal fun cancelDeleteProcess() {
        _deleteProcessJob?.cancel()
        _deleteProcessListener?.onProcessCanceled()
    }

    private fun deleteWithFLow(action: suspend () -> Unit) {
        val tempJob = viewModelScope.launch {
            updateState(ProcessState.PROCESSING)
            _deleteProcessListener?.onProcessExecuting()
            try {
                action.invoke()
                updateState(ProcessState.COMPLETED)
                _deleteProcessListener?.onProcessCompleted(null)
            } catch (e: Exception) {
                _deleteProcessListener?.onProcessCompleted(e)
            }
        }
        tempJob.invokeOnCompletion {
            if (it != null) {
                updateState(ProcessState.CANCELED)
            }
        }
        _deleteProcessJob = tempJob
    }

    private fun updateState(state: ProcessState) =
        _deleteProcessFlow.update { state }

    fun deleteNotifications(nList: List<NotificationLog>) {
        deleteWithFLow {
            repository.deleteNotifications(nList)
        }
    }


    interface OnProcessStateListener {
        fun onProcessCompleted(e: Exception?)
        fun onProcessExecuting()
        fun onProcessCanceled()
    }

    internal fun setDeleteProcessListener(listener: OnProcessStateListener) {
        if (_deleteProcessListener != listener && _deleteProcessFlow.value == ProcessState.PROCESSING) {
            cancelDeleteProcess()
        }
        _deleteProcessListener = listener
    }

    internal fun clearNotification(packageHashcode: String) {
        deleteWithFLow {
            repository.clearNotification(packageHashcode)
        }
    }

    internal fun deletePackage(packageHashcode: String) {
        deleteWithFLow {
            repository.deletePackage(packageHashcode)
        }
    }

    internal fun listenProcessWaiting(context: Context, lifecycleOwner: LifecycleOwner, refresh: () -> Unit) {
        val waitingProcessDialog = WaitingProcessDialog(context).apply {
            setContent(R.string.process_delete_notification)
            setCancelListener {
                cancelDeleteProcess()
            }
        }

        lifecycleOwner.collectWhenCreated(Dispatchers.Main) {
            deleteProcessFlow.collectLatest {
                when(it) {
                    ProcessState.PROCESSING -> {
                        waitingProcessDialog.show()
                    }
                    ProcessState.COMPLETED -> {
                        refresh.invoke()
                        waitingProcessDialog.dismiss()
                    }
                    ProcessState.CANCELED -> {
                        refresh.invoke()
                        waitingProcessDialog.dismiss()
                    }
                    else -> {}
                }
            }
        }
    }

    class Factory(
        private val notisaveApplication: NotisaveApplication,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotificationViewModel(
                    notisaveApplication.notisaveRepository,
                    notisaveApplication.getAppInfoManager()
                ) as T
            }
            throw ClassCastException()
        }
    }
}