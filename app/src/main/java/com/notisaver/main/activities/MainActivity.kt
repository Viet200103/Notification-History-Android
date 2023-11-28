package com.notisaver.main.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.notisaver.R
import com.notisaver.main.MainViewModel
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.NotisaveSetting.Companion.ACTION_ABOUT
import com.notisaver.main.NotisaveSetting.Companion.ACTION_NOTIFICATION_MANAGER
import com.notisaver.main.NotisaveSetting.Companion.ACTION_SETTING
import com.notisaver.main.NotisaveSetting.Companion.ACTIVITY_ACTION
import com.notisaver.main.log.fragments.NotificationHistoryFragment
import com.notisaver.main.log.fragments.NotificationMessageFragment
import com.notisaver.main.log.misc.ISearchSupport
import com.notisaver.main.log.misc.ISelectionSupport
import com.notisaver.main.log.misc.SupportSearchBarBinding
import com.notisaver.main.log.misc.SupportSelectionBarBinding
import com.notisaver.main.log.viewmodels.NotificationViewModel
import com.notisaver.main.settings.SettingActivity
import com.notisaver.main.settings.TutorialBatteryFragment
import com.notisaver.main.start.PermissionManager
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.MailUtil
import com.notisaver.misc.WaitingProcessDialog
import com.notisaver.misc.asNotisaveApplication
import com.notisaver.misc.collectWhenStarted
import com.notisaver.misc.createNotificationSettingIntent
import com.notisaver.misc.isNotificationAccessEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null

    private lateinit var toolbar: MaterialToolbar

    private val stateAdapter by lazy {
        NCategoryStateAdapter(this)
    }

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var navigationView: NavigationView

    private lateinit var selectionBarBinding: SupportSelectionBarBinding
    private lateinit var searchBarBinding: SupportSearchBarBinding

    private lateinit var notificationViewModel: MainViewModel

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationViewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(asNotisaveApplication())
        )[MainViewModel::class.java]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setupPostNotificationPermission()
        }

        setupDeleteObserver()
        setContentView(R.layout.activity_main)

        baseSetup()
        setupSearchBar()
        setupSelectionBar()

        backPressedCallback = createBackPressSelectionBarCallBack()
        onBackPressedDispatcher.addCallback(backPressedCallback)

        lifecycleScope.launch(Dispatchers.IO) {
            requestPostNotification.start()

            if (isShouldRequestBatteryWarning()) {
                triggerNotOptimizeBattery.start()
            }
        }
    }

    private fun setupDeleteObserver() {
        val waitingProcessDialog = WaitingProcessDialog(this).apply {
            setContent(R.string.process_delete_notification)
            setCancelListener {
                notificationViewModel.cancelDeleteProcess()
            }
        }

        collectWhenStarted(Dispatchers.Main) {
            notificationViewModel.deleteProcessFlow.collectLatest {
                when (it) {
                    NotificationViewModel.ProcessState.PROCESSING -> {
                        waitingProcessDialog.show()
                    }

                    NotificationViewModel.ProcessState.COMPLETED -> {
                        waitingProcessDialog.cancel()
                    }

                    NotificationViewModel.ProcessState.CANCELED -> {
                        waitingProcessDialog.cancel()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requestPostNotification.cancel()
        triggerNotOptimizeBattery.cancel()
    }

    private fun setupSearchBar() {
        searchBarBinding = SupportSearchBarBinding.bind(
            findViewById(R.id.search_bar_container_layout)
        )

        notificationViewModel.setSearchTrigger(
            object : ISearchSupport.ITriggerSearchObserver {
                override var isSearching: Boolean = false

                override fun onSearchModeOn() {
                    isSearching = true
                    backPressedCallback.isEnabled = true
                    toolbar.isInvisible = true
                    searchBarBinding.setContainerVisibility(View.VISIBLE)
                    searchBarBinding.requestFocus()
                }

                override fun onSearchClose() {
                    isSearching = false
                    searchBarBinding.hideKeyboard()
                    visualiseToolbar()
                    searchBarBinding.clearText()
                    searchBarBinding.setContainerVisibility(View.INVISIBLE)
                }
            }
        )

        searchBarBinding.setOnQueryChange {
            notificationViewModel.onQueryChange(it)
        }

        searchBarBinding.setOnClickCloseListener {
            notificationViewModel.closeSearch()
        }
    }

    private fun setupSelectionBar() {
        selectionBarBinding = SupportSelectionBarBinding.bind(
            findViewById(R.id.selection_bar_container_layout)
        )

        notificationViewModel.setSelectionTrigger(
            object : ISelectionSupport.ITriggerSelectionMode {
                override var isSelecting: Boolean = false

                override fun turnOnSelectionMode() {
                    isSelecting = true
                    toolbar.isInvisible = true
                    backPressedCallback.isEnabled = true
                    selectionBarBinding.setContainerVisibility(View.VISIBLE)
                }

                override fun onSelectedCountChange(count: Int) {
                    selectionBarBinding.setCount(count)
                }

                override fun turnOffSelectionMode() {
                    isSelecting = false
                    visualiseToolbar()
                    selectionBarBinding.setContainerVisibility(View.INVISIBLE)
                }
            }
        )

        selectionBarBinding.setOnClickCloseListener {
            notificationViewModel.closeSelection()
        }

        selectionBarBinding.setOnClickDeleteListener {
            notificationViewModel.performSelectionDelete()
        }
    }

    private fun baseSetup() {
        toolbar = findViewById(R.id.activity_main_toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.activity_main_drawer_layout)
        toolbar.setNavigationOnClickListener {
            drawerLayout.open()
        }

        navigationView = findViewById(R.id.activity_main_navigation_view)
        navigationView.setNavigationItemSelectedListener(this)


        val tabLayout = findViewById<TabLayout>(R.id.activity_main_tab_layout)


        val viewPager = findViewById<ViewPager2>(R.id.activity_main_view_pager_2)

        viewPager.apply {
            offscreenPageLimit = 3
            adapter = stateAdapter
        }
        viewPager.currentItem = 1

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.setText(R.string.notification_message)
                    FirebaseAnalytics.getInstance(this).logEvent(
                        "tab_message", Bundle()
                    )
                }

                1 -> {
                    tab.setText(R.string.notification_history)
                    FirebaseAnalytics.getInstance(this).logEvent(
                        "tab_history", Bundle()
                    )
                }
            }


        }.attach()

        viewPager.isUserInputEnabled = false
    }

    private fun visualiseToolbar() {
        toolbar.isVisible =
            notificationViewModel.isSearching == false && notificationViewModel.isSelecting == false
    }

    private fun createBackPressSelectionBarCallBack() = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (notificationViewModel.isSelecting) {
                notificationViewModel.closeSelection()
            } else if (notificationViewModel.isSearching) {
                notificationViewModel.closeSearch()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private class NCategoryStateAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> NotificationMessageFragment()
                1 -> NotificationHistoryFragment()
                else -> throw IllegalStateException()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_main_notification_manager -> {
                startSettingActivity(ACTION_NOTIFICATION_MANAGER)
            }

            R.id.menu_main_setting -> {
                startSettingActivity(ACTION_SETTING)
            }

            R.id.menu_main_feedback -> {
                MailUtil.feedback(this)
            }

            R.id.menu_main_about -> {
                startSettingActivity(ACTION_ABOUT)
            }

            R.id.menu_main_share -> {
                // TODO
            }

            R.id.menu_rate -> {
                // TODO
            }
        }
        drawerLayout.close()
        return true
    }

    private fun startSettingActivity(action: Int) {
        startActivity(
            Intent(this, SettingActivity::class.java).apply {
                putExtra(ACTIVITY_ACTION, action)
            }
        )
    }

    private fun requestNotOptimizeBattery() {
        val isNotOptimize = NotisaveSetting.isNotOptimizeBatterEnabled(this)
        if (isNotOptimize) {
            return
        }

        val isAccessed = isNotificationAccessEnabled(this)
        if (!isAccessed) {
            return
        }

        val appName = getString(R.string.app_name)

        SupportMaterialAlertDialog.createAlertDialog(
            this,
            getString(R.string.pre_battery_optimization),
            R.drawable.ic_battery,
            getString(
                R.string.battry_optimization_dialog_explain,
                appName, appName
            ),
            positiveTextId = R.string.turn_on,
            positiveListener = this::openNotBatteryDialog
        ).show()
    }

    private fun openNotBatteryDialog() {
        try {
            val fragmentTag = "BatteryFragment"

            if (supportFragmentManager.findFragmentByTag(fragmentTag) != null) {
                return
            }

            val batteryFragment = TutorialBatteryFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction
                .add(android.R.id.content, batteryFragment, fragmentTag)
                .addToBackStack(null)
                .commit()
        } catch (_: Exception) {
        }
    }

    private val triggerNotOptimizeBattery = object : CountDownTimer(2000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            requestNotOptimizeBattery()
        }
    }

    private val requestPostNotification = object : CountDownTimer(2000, 1000) {

        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPostNotificationPermission()
            }
        }
    }

    private fun isShouldRequestBatteryWarning(): Boolean {
        if (supportFragmentManager.findFragmentByTag("BatteryFragment") != null) {
            return false
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupPostNotificationPermission() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

                val isShouldRationale = PermissionManager.shouldShowPostNotificationRationale(this)

                if (isGranted.not() && isShouldRationale.not()) {
                    try {
                        PermissionManager.createPostNotificationRequireSettingDialog(
                            this@MainActivity,
                            agreeAction = {
                                startActivity(
                                    createNotificationSettingIntent(packageName)
                                )
                            }
                        ).show()
                    } catch (_: Exception) {
                        Timber.d("RequestPermission: Post notification is failed")
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationPermission() {
        val isGranted = PermissionManager.isPostNotificationGranted(this)

        if (isGranted.not()) {
            requestPermissionLauncher?.let {
                PermissionManager.requestPostNotificationPermission(this, it)
            }
        }
    }

}