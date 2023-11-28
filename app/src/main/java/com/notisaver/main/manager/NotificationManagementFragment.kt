package com.notisaver.main.manager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.notisaver.R
import com.notisaver.database.AppTemplate
import com.notisaver.main.BUNDLE_KEY
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.PACKAGE_HASHCODE_KEY
import com.notisaver.main.log.activities.NotificationPackageActivity
import com.notisaver.main.log.viewmodels.NotificationViewModel
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.asNotisaveApplication
import com.notisaver.misc.collectWhenCreated
import com.notisaver.misc.collectWhenStarted
import com.notisaver.misc.createNotificationSettingIntent
import com.notisaver.misc.findView
import com.notisaver.misc.startLauncherIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotificationManagementFragment : Fragment(R.layout.fragment_notification_manager),
    OnOptionAppClickListener, MenuProvider {

    private val notificationViewModel by viewModels<NotificationViewModel>(this::requireActivity)

    private val notisaveApplication by lazy {
        requireContext().asNotisaveApplication()
    }

    private val appInfoManager
        get() = AppInfoManager.getInstance(notisaveApplication)

    private val notisaveRepository
        get() = notisaveApplication.notisaveRepository

    private val contentLoadingFLow = MutableStateFlow(false)
    private var contentLoadingJob: Job? = null

    private val searchStateFlow = MutableStateFlow("")

    private var appTemplateList = listOf<AppTemplate>()

    private lateinit var scanningLayout: LinearLayoutCompat
    private lateinit var appRecyclerView: RecyclerView

    private val appAdapter: AppAdapter by lazy(LazyThreadSafetyMode.NONE) {
        AppAdapter(this, appInfoManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        invokeRescanAppOnDevice()

        collectWhenCreated {
            notisaveRepository.loadAppTemplate().conflate().collectLatest {
                appTemplateList = it
            }
        }

        collectWhenCreated {
            searchStateFlow.collectLatest {
                loadData()
            }
        }

        collectWhenStarted {
            contentLoadingFLow.collectLatest {
                scanningLayout.isInvisible = !it
                appRecyclerView.isInvisible = it
            }
        }

        collectWhenCreated {
            appInfoManager.appStateFlow.collectLatest { state ->
                if (!contentLoadingFLow.value) {
                    val index = appAdapter.currentList.indexOfFirst {
                        state.packageHashcode == it.aPackageHashcode
                    }
                    appAdapter.notifyItemChanged(index)
                }
            }
        }

        notificationViewModel.listenProcessWaiting(requireContext(), this) {
            loadData()
        }
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            contentLoadingFLow.update { true }
            while (true) {
                if (!isActive) return@launch
                if (appInfoManager.appScanFlow.value) break
            }

            val packageHashSet = appInfoManager.filterPackageHashcodeAppInfo(searchStateFlow.value)

            val list = appTemplateList.filter {
                packageHashSet.contains(it.aPackageHashcode)
            }

            withContext(Dispatchers.Main) {
                appAdapter.submitList(list)
            }
            contentLoadingFLow.update { false }
        }
    }

    private fun invokeRescanAppOnDevice() {
        appInfoManager.scanAppOnDevice(
            NotisaveSetting.getInstance(requireContext())
        )
        loadData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        scanningLayout = findView(R.id.fragment_notification_manager_app_loading_layout)

        appRecyclerView = findView(R.id.fragment_notification_manager_app_recycler_view)
        appRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appAdapter
        }
    }


    override fun onAppClick(packageHashcode: String) {

        val bundle = Bundle()

        bundle.putString(PACKAGE_HASHCODE_KEY, packageHashcode)

        val intent = Intent(requireContext(), NotificationPackageActivity::class.java).apply {
            putExtra(BUNDLE_KEY, bundle)
        }

        startActivity(intent)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_notification_manager, menu)
        val searchItem = menu.findItem(R.id.menu_notification_manager_search)

        val searchView: SearchView? = searchItem.actionView as SearchView?

        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchStateFlow.update {
                        newText?.trim()?.lowercase().orEmpty()
                    }
                    return true
                }
            }
        )
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_notification_manager_rescan -> {
                FirebaseAnalytics.getInstance(requireContext()).logEvent("onRescanApp", Bundle())
                invokeRescanAppOnDevice()
            }
        }
        return true
    }

    override fun onOptionSelected(
        packageHashcode: String,
        itemMenuSelected: MenuItem,
    ) {
        val context = requireContext()
        val appInfo = appInfoManager.getAppInformationLazyCache(packageHashcode)

        if (appInfo == null) {
            Toast.makeText(context, R.string.please_try_again, Toast.LENGTH_SHORT).show()
            return
        }


        when (itemMenuSelected.itemId) {
            R.id.menu_app_option_item_open_app -> {
                try {
                    startLauncherIntent(
                        context, appInfoManager.getLauncherIntent(appInfo.packageName)
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.package_not_found_error, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            R.id.menu_app_option_item_open_setting -> {
                try {
                    startActivity(createNotificationSettingIntent(appInfo.packageName))
                } catch (e: Exception) {
                    Toast.makeText(context, R.string.setting_open_error, Toast.LENGTH_SHORT).show()
                }
            }

            R.id.menu_app_option_item_exclude -> {
                SupportMaterialAlertDialog.createWarningExcludeDialog(
                    context, appInfo.name ?: appInfo.packageName
                ) {
                    appInfoManager.excludeApp(appInfo)
                }.show()
            }

            R.id.menu_app_option_item_include -> {
                appInfoManager.includeApp(appInfo)
            }

            R.id.menu_app_option_item_enable_log_ongoing -> {
                SupportMaterialAlertDialog.createWarningLogOngoingDialog(
                    context, appInfo.name ?: appInfo.packageName
                ) {
                    appInfoManager.enableLogOngoing(appInfo)
                }.show()
            }

            R.id.menu_app_option_item_disable_log_ongoing -> {
                appInfoManager.disableLogOngoing(appInfo)
            }

            R.id.menu_app_option_item_enable_cancel_when_push -> {
                SupportMaterialAlertDialog.createCancelWhenPushDialog(context) {
                    appInfoManager.enableCleanable(appInfo)
                }.show()
            }

            R.id.menu_app_option_item_disable_cancel_when_push -> {
                appInfoManager.disableCleanable(appInfo)
            }

            R.id.menu_app_option_item_clear -> {
                SupportMaterialAlertDialog.createWarningDeleteDialog(requireContext()) {
                    notificationViewModel.clearNotification(appInfo.packageHashcode)
                }.show()
            }

            R.id.menu_app_option_item_delete_app -> {
                SupportMaterialAlertDialog.createWarningDeleteDialog(context) {
                    notificationViewModel.deletePackage(appInfo.packageHashcode)
                }.show()
            }
        }
    }
}