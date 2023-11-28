package com.notisaver.main.log.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.notisaver.R
import com.notisaver.database.entities.NStatusBarGroup
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.log.adapters.NStatusBarPagingAdapter
import com.notisaver.main.log.intefaces.OnStatusBarNotificationClickListener
import com.notisaver.main.log.misc.ISelectionSupport
import com.notisaver.main.log.viewmodels.NotificationPackageViewModel
import com.notisaver.main.log.viewmodels.NotificationViewModel
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class NotificationSublistFragment : Fragment(R.layout.fragment_notification_list),
    OnStatusBarNotificationClickListener, SwipeRefreshLayout.OnRefreshListener,  ISelectionSupport.ISelectionObserver  {
    private val notisaveApplication: NotisaveApplication by lazy(LazyThreadSafetyMode.NONE) {
        requireContext().asNotisaveApplication()
    }

    private val viewModel by viewModels<NotificationPackageViewModel>(this::requireActivity) {
        NotificationPackageViewModel.Factory(
            notisaveApplication
        )
    }

    private lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var recyclerView: RecyclerView

    private val pagingAdapter by lazy {
        NStatusBarPagingAdapter(
            notisaveApplication.getAppInfoManager(), this
        )
    }

    private var dataJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindData()

        pagingAdapter.selectionModeOnListener = {
            viewModel.turnOnSelectionMode()
        }

        pagingAdapter.selectedCountListener = {
            viewModel.notifySelectedCountChange(it)
        }

        val waitingProcessDialog = WaitingProcessDialog(requireContext()).apply {
            setContent(R.string.process_delete_notification)
            setCancelListener {
                viewModel.cancelDeleteProcess()
            }
        }

        collectWhenStarted(Dispatchers.Main) {
            viewModel.deleteProcessFlow.collectLatest {
                when(it) {
                    NotificationViewModel.ProcessState.PROCESSING -> {
                        dataJob?.cancel()
                        waitingProcessDialog.show()
                    }
                    NotificationViewModel.ProcessState.COMPLETED -> {
                        pagingAdapter.clearSelectedItem()
                        bindData()
                        waitingProcessDialog.dismiss()
                    }
                    NotificationViewModel.ProcessState.CANCELED -> {
                        pagingAdapter.clearSelectedItem()
                        bindData()
                        waitingProcessDialog.dismiss()
                    }
                    else -> {}
                }
            }
        }

        viewModel.setSelectionObserver(this)

        collectWhenStarted {
            pagingAdapter.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.source.refresh is LoadState.NotLoading) {

                    val displayEmptyMessage =
                        (loadStates.append.endOfPaginationReached && pagingAdapter.itemCount < 1)

                    viewModel.emptyStateFlow.emit(displayEmptyMessage)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout = findView(R.id.fragment_notification_time_list_refresh_layout)
        refreshLayout.setOnRefreshListener(this)

        recyclerView = findView(R.id.fragment_notification_list_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pagingAdapter
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun bindData() {
        dataJob = collectWhenCreated(Dispatchers.IO) {
            viewModel.pagingDataFlow.collectLatest {
                withContext(Dispatchers.Main) {
                    pagingAdapter.submitData(it)
                }
            }
        }
    }

    override fun onStatusBarNotificationClick(nStatusBarGroup: NStatusBarGroup) {
        if (nStatusBarGroup.nCount == 1) {
            NotificationInfoBottomSheet.createInstance(nStatusBarGroup.header.logId)
                .show(parentFragmentManager, NotificationHistoryFragment::class.java.name)
        } else {
            NotificationListDialogFragment.createInstance(
                nStatusBarGroup.sbnKeyHashcode,
                nStatusBarGroup.packageHashcode,
                viewModel.searchStateFlow.value
            ).show(parentFragmentManager, NotificationHistoryFragment::class.java.name)
        }
    }

    override fun onSelectionClose() {
        pagingAdapter.turnOffSelectionMode()
    }

    override fun onSelectedDelete() {
        SupportMaterialAlertDialog.createWarningDeleteDialog(
            requireContext()
        ) {
            viewModel.deleteNotification(
                pagingAdapter.getItemsSelected(),
                viewModel.searchStateFlow.value
            )
        }.show()
    }

    override fun onRefresh() {
        pagingAdapter.refresh()
        refreshLayout.isRefreshing = false
    }
}