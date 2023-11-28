package com.notisaver.main.settings

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.notisaver.R
import com.notisaver.main.NotisaveSetting.Companion.ACTION_ABOUT
import com.notisaver.main.NotisaveSetting.Companion.ACTION_NOTIFICATION_MANAGER
import com.notisaver.main.NotisaveSetting.Companion.ACTION_SETTING
import com.notisaver.main.NotisaveSetting.Companion.ACTIVITY_ACTION
import com.notisaver.main.activities.BaseActivity
import com.notisaver.main.log.viewmodels.NotificationViewModel
import com.notisaver.main.manager.NotificationManagementFragment
import com.notisaver.misc.asNotisaveApplication
import timber.log.Timber

class SettingActivity : BaseActivity() {
    private lateinit var toolbar: MaterialToolbar

    private var action = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        toolbar = findViewById(R.id.activity_other_toolbar)

        action = intent.getIntExtra(ACTIVITY_ACTION, -1)

        when(action) {
            ACTION_SETTING -> toolbar.setTitle(R.string.setting)
            ACTION_ABOUT -> toolbar.setTitle(R.string.about)
            ACTION_NOTIFICATION_MANAGER -> toolbar.setTitle(R.string.notification_manager)
        }

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (action == ACTION_NOTIFICATION_MANAGER) {
            ViewModelProvider(
                this, NotificationViewModel.Factory(asNotisaveApplication())
            )[NotificationViewModel::class.java]
        }

        if (savedInstanceState == null) {
            try {
                val fragment = getFragmentWithAction()

                supportFragmentManager.beginTransaction().add(
                    R.id.activity_other_fragment_container, fragment
                ).commitNowAllowingStateLoss()
            } catch (e: IllegalArgumentException) {
                Timber.d(e.message)
                Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show()
                this.finish()
            }
        }
    }
    
    @Throws
    private fun getFragmentWithAction() = when(action) {
        ACTION_SETTING -> SettingFragment()
        ACTION_ABOUT -> AboutFragment()
        ACTION_NOTIFICATION_MANAGER -> NotificationManagementFragment()
        else -> throw IllegalArgumentException("OtherActivity action is wrong")
    }
}