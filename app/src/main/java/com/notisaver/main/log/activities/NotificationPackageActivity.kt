package com.notisaver.main.log.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.imageview.ShapeableImageView
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.main.BUNDLE_KEY
import com.notisaver.main.PACKAGE_HASHCODE_KEY
import com.notisaver.main.activities.BaseActivity
import com.notisaver.main.log.fragments.NotificationSublistFragment
import com.notisaver.main.log.misc.ISelectionSupport
import com.notisaver.main.log.misc.SupportSelectionBarBinding
import com.notisaver.main.log.viewmodels.NotificationPackageViewModel
import com.notisaver.main.log.viewmodels.NotificationPackageViewModel.*
import com.notisaver.main.manager.AppInfoManager
import com.notisaver.main.manager.AppSupportMenu
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class NotificationPackageActivity : BaseActivity() {

    private val appInfoManager by lazy {
        asNotisaveApplication().getAppInfoManager()
    }

    private val imManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private var isSearchMode = false

    private val viewModel by viewModels<NotificationPackageViewModel> {
        Factory(asNotisaveApplication())
    }

    private var appInfo: AppInformation? = null

    private lateinit var backButton: AppCompatImageView
    private lateinit var appIconView: ShapeableImageView
    private lateinit var appTitleView: AppCompatTextView

    private var countView: AppCompatTextView? = null

    private lateinit var tokenExcludeView: AppCompatImageView
    private lateinit var tokenCancelView: AppCompatImageView
    private lateinit var tokenProblemView: AppCompatImageView

    private lateinit var supportMenu: AppSupportMenu
    private lateinit var supportMenuView: FrameLayout

    private lateinit var searchModeButton: FrameLayout
    private lateinit var enterSearchView: AppCompatEditText

    private lateinit var emptyView: AppCompatTextView

    private lateinit var headerContainerLayout: LinearLayoutCompat
    private lateinit var subHeaderContainerLayout: FrameLayout
    private lateinit var appSubHeaderLayout: LinearLayoutCompat

    private lateinit var selectionBarBinding: SupportSelectionBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageHashcode = intent?.getBundleExtra(BUNDLE_KEY)?.getString(PACKAGE_HASHCODE_KEY) ?: kotlin.run {
                errorExecute()
                return
        }

        viewModel.updatePackage(packageHashcode)

        collectWhenCreated {
            viewModel.appInfoFlow.collectLatest {
                appInfo = it
                bindAppTitleBar()
            }
        }

        setupNotificationCountObserver()

        setContentView(R.layout.activity_list_notification)

        if (savedInstanceState == null) {

            val fragment = NotificationSublistFragment()

            supportFragmentManager.beginTransaction().add(
                R.id.activity_detail_notification_container_fragment_layout,
                fragment
            ).commit()
        }

        setupAppStateChangeObserver()

        bindViews()
        setupEmptyState()
        setupSupportMenu()
        setupSearchMode()

        selectionBarBinding = SupportSelectionBarBinding.bind(
            findViewById(R.id.selection_bar_container_layout),
        )

        headerContainerLayout =  findViewById(R.id.activity_notification_list_header_container_layout)
        subHeaderContainerLayout = findViewById(R.id.activity_notification_list_sub_header_container_layout)
        appSubHeaderLayout = findViewById(R.id.activity_notification_list_app_sub_header)

        selectionBarBinding.setOnClickCloseListener {
            viewModel.closeSelection()
        }

        selectionBarBinding.setOnClickDeleteListener {
            viewModel.performSelectionDelete()
        }

        viewModel.setTrigger(
            object : ISelectionSupport.ITriggerSelectionMode {
                override var isSelecting: Boolean = false

                override fun turnOnSelectionMode() {
                    isSelecting = true
                    selectionBarBinding.setContainerVisibility(View.VISIBLE)
                    headerContainerLayout.visibility = View.INVISIBLE
                    subHeaderContainerLayout.visibility = View.GONE
                }

                override fun onSelectedCountChange(count: Int) {
                    launch(Dispatchers.Main) {
                        selectionBarBinding.setCount(count)
                    }
                }

                override fun turnOffSelectionMode() {
                    isSelecting = false
                    selectionBarBinding.setContainerVisibility(View.INVISIBLE)
                    headerContainerLayout.visibility = View.VISIBLE
                    subHeaderContainerLayout.visibility = View.VISIBLE
                }

            }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupNotificationCountObserver() {
        collectWhenCreated {
            viewModel.nCountFlow.collectLatest(
                this@NotificationPackageActivity::setNotificationCount
            )
        }
    }

    private fun bindAppTitleBar() {
        changeVisibleView(tokenProblemView, appInfo?.isInstalled != true)

        unKnownName()
        unknownAppIcon()
        changeVisibleView(tokenCancelView, false)
        changeVisibleView(tokenExcludeView, false)

        appInfo?.let { info ->

                if (info.name == null) {
                    appTitleView.text = info.packageName
                } else {
                    appTitleView.text = info.name
                }

                if (info.icon != null) appIconView.setImageDrawable(info.icon)

            changeVisibleView(tokenExcludeView, !info.isTracking)
            changeVisibleView(tokenCancelView, info.isCleanable)
        }
    }

    private fun bindViews() {

        backButton = findViewById(R.id.activity_notification_list_app_back_view)
        backButton.setOnClickListener {
            if (isSearchMode) {
                isSearchMode = false

                searchModeButton.isVisible = true
                appSubHeaderLayout.isVisible = true

                enterSearchView.editableText?.clear()
                enterSearchView.visibility = View.INVISIBLE

                imManager.hideSoftInputFromWindow(
                    enterSearchView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            } else {
                finish()
            }
        }

        appIconView = findViewById(R.id.activity_detail_notification_app_icon_view)
        appTitleView = findViewById(R.id.activity_notification_list_app_name_view)

        countView = findViewById(R.id.activity_notification_list_app_count_view)

        tokenProblemView = findViewById(R.id.activity_detail_notification_app_token_problem)
        tokenExcludeView = findViewById(R.id.activity_detail_notification_app_token_exclude_view)
        tokenCancelView = findViewById(R.id.activity_detail_notification_app_token_cleanable_view)
    }

    private fun setupSupportMenu() {
        supportMenuView = findViewById(R.id.activity_notification_list_app_support_menu_view)
        supportMenu = AppSupportMenu(this, supportMenuView)
        supportMenu.getMenu().removeItem(R.id.menu_app_option_item_delete_app)

        setupMenuListener()
        supportMenuView.setOnClickListener {
            requireAppNotNull(appInfo) {
                supportMenu.updateOption(it.appMetaData)
                supportMenu.show()
            }
        }
    }

    private fun setupSearchMode() {
        enterSearchView = findViewById(R.id.activity_notification_list_app_enter_search_view)
        enterSearchView.addTextChangedListener {
            viewModel.searchNotification(it?.trim()?.toString() ?: String())
        }

        searchModeButton = findViewById(R.id.activity_notification_list_app_search_mode_view)
        searchModeButton.setOnClickListener {
            isSearchMode = true

            it.visibility = View.INVISIBLE

            enterSearchView.isVisible = true
            appSubHeaderLayout.isInvisible = true


            enterSearchView.setText(
                viewModel.searchStateFlow.value
            )
            enterSearchView.requestFocus()

            imManager.showSoftInput(
                enterSearchView,
                InputMethodManager.SHOW_IMPLICIT
            )
        }
    }

    private fun setupEmptyState() {
        emptyView = findViewById(R.id.layout_empty_state_view)
        emptyView.setText(R.string.empty_list)
        collectWhenStarted {
            viewModel.emptyStateFlow.collectLatest { isEmpty ->
                emptyView.isInvisible = !isEmpty
            }
        }
    }

    private fun setupAppStateChangeObserver() = collectWhenStarted {
        appInfoManager.appStateFlow.collect { data ->
            if (appInfo == null) return@collect
            if (data.packageHashcode != appInfo!!.packageHashcode) return@collect

            when (data) {
                is AppInfoManager.AppState.CleanableStateChange -> {
                    changeVisibleView(tokenCancelView, appInfo!!.isCleanable)
                }

                is AppInfoManager.AppState.TrackingStateChange -> {
                    changeVisibleView(tokenExcludeView, !appInfo!!.isTracking)
                }
            }
        }
    }

    private fun setNotificationCount(count: Int) {
        countView?.text = resources.getQuantityString(
            R.plurals.number_notification, count, count
        )
    }

    private fun unknownAppIcon() {
        appIconView.setImageResource(R.drawable.ic_extension)
    }

    private fun unKnownName() {
        appTitleView.setText(R.string.unknown_app)
    }

    private fun errorExecute() {
        Toast.makeText(this, R.string.error_no_data_found, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupMenuListener() {
        supportMenu.setOnItemClickListener {
            requireAppNotNull(appInfo) {appInfo ->
                when (it.itemId) {
                    R.id.menu_app_option_item_open_app -> {
                        try {
                            if (!appInfo.isInstalled) throw RuntimeException()
                            startLauncherIntent(
                                this, appInfoManager.getLauncherIntent(appInfo.packageName)
                            )
                        } catch (e: Exception) {
                            Toast.makeText(this, R.string.package_not_found_error, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    R.id.menu_app_option_item_open_setting -> {
                        try {
                            if (!appInfo.isInstalled) throw RuntimeException()
                            startActivity(createNotificationSettingIntent(appInfo.packageName))
                        } catch (e: Exception) {
                            Toast.makeText(this, R.string.package_not_found_error, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    R.id.menu_app_option_item_exclude -> {
                        SupportMaterialAlertDialog.createWarningExcludeDialog(this, appInfo.packageName) {
                            appInfoManager.excludeApp(appInfo)
                        }.show()
                    }

                    R.id.menu_app_option_item_include -> {
                        appInfoManager.includeApp(appInfo)
                    }

                    R.id.menu_app_option_item_enable_log_ongoing -> {
                        SupportMaterialAlertDialog.createWarningLogOngoingDialog(this, appInfo.packageName) {
                            appInfoManager.enableLogOngoing(appInfo)
                        }.show()
                    }

                    R.id.menu_app_option_item_disable_log_ongoing -> {
                        appInfoManager.disableLogOngoing(appInfo)
                    }

                    R.id.menu_app_option_item_enable_cancel_when_push -> {
                        SupportMaterialAlertDialog.createCancelWhenPushDialog(this) {
                            appInfoManager.enableCleanable(appInfo)
                        }.show()
                    }

                    R.id.menu_app_option_item_disable_cancel_when_push -> {
                        appInfoManager.disableCleanable(appInfo)
                    }

                    R.id.menu_app_option_item_clear -> {
                        SupportMaterialAlertDialog.createWarningDeleteDialog(this) {
                            viewModel.clearNotification(appInfo.packageHashcode)
                        }.show()
                    }
                }
            }
            false
        }
    }
}