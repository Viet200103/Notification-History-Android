package com.notisaver.main.log.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.MenuCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.notisaver.R
import com.notisaver.database.NotisaveDatabase
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.NStatusBarGroup
import com.notisaver.main.BUNDLE_KEY
import com.notisaver.main.MainViewModel
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.PACKAGE_HASHCODE_KEY
import com.notisaver.main.SEARCH_VALUE
import com.notisaver.main.log.activities.NotificationPackageActivity
import com.notisaver.main.log.adapters.NStatusBarPagingAdapter
import com.notisaver.main.log.adapters.NormalNotificationPagingAdapter
import com.notisaver.main.log.adapters.NotificationAdapter
import com.notisaver.main.log.adapters.NotificationLoadingAdapter
import com.notisaver.main.log.adapters.PackagePagingAdapter
import com.notisaver.main.log.intefaces.NotificationItem
import com.notisaver.main.log.intefaces.OnPackageClickListener
import com.notisaver.main.log.intefaces.OnStatusBarNotificationClickListener
import com.notisaver.main.log.misc.ISearchSupport
import com.notisaver.main.log.misc.ISelectionSupport
import com.notisaver.main.log.misc.NotificationFormat
import com.notisaver.main.log.viewmodels.NotificationViewModel
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

abstract class NotificationListFragment : Fragment(R.layout.fragment_notification_status_bar),
    OnStatusBarNotificationClickListener, OnPackageClickListener,
    NotificationViewModel.OnProcessStateListener, MenuProvider,
    ISelectionSupport.ISelectionObserver, ISearchSupport.ISearchObserver
{

    protected lateinit var notificationViewModel: MainViewModel

    private lateinit var notisaveApplication: NotisaveApplication

    protected val searchStateFlow = MutableStateFlow(String())
    protected val loadingFlow = MutableStateFlow(false)
    protected var dataJob: Job? = null

    protected lateinit var contentFlow: Flow<PagingData<out NotificationItem>>

    protected val notificationFormat by lazy {
        NotificationFormat(notisaveApplication)
    }
    protected val notisaveRepository: NotisaveRepository
        get() = notisaveApplication.notisaveRepository

    protected val appInfoManager
        get() = notisaveApplication.getAppInfoManager()


    private lateinit var notificationAdapter: NotificationAdapter<NotificationItem, RecyclerView.ViewHolder>

    protected lateinit var rootLayout: FrameLayout

    protected lateinit var emptyView: AppCompatTextView

    protected lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = requireActivity()

        notisaveApplication = activity.asNotisaveApplication()
        notificationViewModel = ViewModelProvider(
            activity, MainViewModel.Factory(notisaveApplication)
        )[MainViewModel::class.java]

        requireActivity().addMenuProvider(this, this, Lifecycle.State.RESUMED)

        setupAdapter()

        loadingFlow.value = true
        collectWhenStarted {
            notificationAdapter.loadStateFlow.collectLatest { loadStates ->

                if (loadStates.source.refresh is LoadState.NotLoading) {
                    onEmptyState(
                        (loadStates.append.endOfPaginationReached && notificationAdapter.itemCount < 1)
                    )
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout = findView(R.id.fragment_notification_status_bar_root_layout)
        emptyView = findView(R.id.layout_empty_state_view)

        refreshLayout = findView(R.id.fragment_notification_status_bar_swipe_layout)
        refreshLayout.setOnRefreshListener {
            loadingFlow.value = true
        }

        recyclerView = findView(R.id.fragment_notification_status_bar_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        notificationViewModel.setSelectionObserver(this)
        notificationViewModel.setDeleteProcessListener(this)
        notificationViewModel.setSearchObserver(this)
    }

    override fun onDestroy() {
        notificationViewModel.cancelSelectionObserver(this)
        notificationViewModel.cancelSearchObserver(this)
        super.onDestroy()
    }

    protected fun bindNotificationData() {
        dataJob = collectWhenCreated {
            contentFlow.collectLatest {

                val pagingData = it.map { nItem ->
                    appInfoManager.bindToCacheAppInfo(nItem.packageHashcode)
                    nItem
                }

                if (loadingFlow.value) {
                    notificationAdapter.submitData(PagingData.empty())
                    loadingFlow.value = false
                }

                notificationAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupAdapter() {
        notificationAdapter = getAdapter(
            notificationFormat.getForm(getCategoryId())
        )

        notificationAdapter.selectionModeOnListener = {
            notificationViewModel.turnOnSelectionMode()
        }

        notificationAdapter.selectedCountListener = {
            notificationViewModel.notifySelectedCountChange(it)
        }

        notificationAdapter.withLoadStateFooter(
            NotificationLoadingAdapter {
                notificationAdapter.retry()
            }
        )
    }

    override fun onProcessExecuting() {
        dataJob?.cancel()
    }

    override fun onProcessCompleted(e: Exception?) {
        notificationAdapter.clearSelectedItem()
        bindNotificationData()
    }

    override fun onProcessCanceled() {
        notificationAdapter.clearSelectedItem()
        bindNotificationData()
    }

    private fun getAdapter(form: NotificationFormat.Form): NotificationAdapter<NotificationItem, RecyclerView.ViewHolder> {
        @Suppress("UNCHECKED_CAST")
        return when (form) {
            NotificationFormat.Form.PACKAGE -> {
                PackagePagingAdapter(appInfoManager, this)
            }

            NotificationFormat.Form.NONE -> {
                NormalNotificationPagingAdapter(
                    appInfoManager, this::onNotificationClick
                )
            }

            NotificationFormat.Form.GROUP -> {
                NStatusBarPagingAdapter(appInfoManager, this)
            }
        } as NotificationAdapter<NotificationItem, RecyclerView.ViewHolder>
    }

    override fun onStatusBarNotificationClick(nStatusBarGroup: NStatusBarGroup) {

        if (nStatusBarGroup.nCount == 1) {
            openNotificationDialog(nStatusBarGroup.header.logId)
        } else {
            NotificationListDialogFragment.createInstance(
                nStatusBarGroup.sbnKeyHashcode,
                nStatusBarGroup.packageHashcode,
                searchStateFlow.value
            ).show(parentFragmentManager, "MoreNotification")
        }
    }

    override fun onNotificationClick(notificationItem: NotificationItem) {
        openNotificationDialog(notificationItem.id.toString())
    }

    override fun onPackageClick(
        packageHashcode: String,
        notificationItem: NotificationItem
    ) {
        val bundle = Bundle()

        bundle.putString(PACKAGE_HASHCODE_KEY, packageHashcode)
        bundle.putString(SEARCH_VALUE, searchStateFlow.value)

        val intent = Intent(requireContext(), NotificationPackageActivity::class.java).apply {
            putExtra(BUNDLE_KEY, bundle)
        }

        startActivity(intent)
    }

    private fun openNotificationDialog(logId: String) {
        NotificationInfoBottomSheet.createInstance(logId)
            .show(parentFragmentManager, "NotificationInfo")
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_notification_list, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_notification_list_group_by_id -> {
                updateForm(NotificationFormat.Form.GROUP)
            }

            R.id.menu_notification_list_group_by_app -> {
                updateForm(NotificationFormat.Form.PACKAGE)
            }

            R.id.menu_notification_list_group_by_none -> {
                updateForm(NotificationFormat.Form.NONE)
            }

            R.id.menu_notification_list_sort_by_asc -> {
                updateSortMode(NotisaveDatabase.SortMode.ASC)
            }

            R.id.menu_notification_list_sort_by_desc -> {
                updateSortMode(NotisaveDatabase.SortMode.DESC)
            }

            else -> return false
        }
        return true
    }

    private fun updateForm(form: NotificationFormat.Form) {
        val categoryId = getCategoryId()
        if (notificationFormat.getForm(categoryId) != form) {

            FirebaseAnalytics.getInstance(requireContext()).logEvent("onChangeNotificationListForm") {
                param(FirebaseAnalytics.Param.ITEM_CATEGORY, categoryId)
                param(FirebaseAnalytics.Param.CONTENT_TYPE, form.name)
            }

            notificationFormat.setForm(getCategoryId(), form)
            setupAdapter()
            recyclerView.adapter = notificationAdapter
            loadingFlow.value = true
        }
    }

    private fun updateSortMode(sortMode: NotisaveDatabase.SortMode) {
        if (notificationFormat.getSortMode(getCategoryId()) != sortMode) {
            notificationFormat.setSortMode(getCategoryId(), sortMode)
            loadingFlow.value = true
        }
    }

    override fun onSelectionClose() {
        notificationAdapter.turnOffSelectionMode()
    }

    override fun onSelectedDelete() {
        SupportMaterialAlertDialog.createWarningDeleteDialog(
            requireContext()
        ) {
            when (notificationFormat.getForm(getCategoryId())) {
                NotificationFormat.Form.PACKAGE -> {

                }

                NotificationFormat.Form.GROUP -> {
                    val statusBarAdapter = notificationAdapter as NStatusBarPagingAdapter
                    notificationViewModel.deleteNotification(
                        statusBarAdapter.getItemsSelected(), searchStateFlow.value
                    )
                }

                NotificationFormat.Form.NONE -> {
                    val detailAdapter = notificationAdapter as NormalNotificationPagingAdapter
                    notificationViewModel.deleteNotifications(
                        detailAdapter.getItemsSelected()
                    )
                }
            }
        }.show()
    }

    override fun onQueryChange(newText: String) {
        searchStateFlow.value = newText.trim()
    }

    override fun onQuerySubmitChange(submit: String) {
        searchStateFlow.value = submit.trim()
    }

    protected abstract fun getCategoryId(): String

    protected abstract fun getViewModel(): MainViewModel

    open fun onEmptyState(isEmpty: Boolean) {}
}