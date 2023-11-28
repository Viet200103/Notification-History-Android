package com.notisaver.main.protection.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.notisaver.R
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.activities.FullScreenActivity
import com.notisaver.main.protection.setup.SetupPasscodeFragment
import com.notisaver.main.protection.setup.SetupPasswordViewModel
import com.notisaver.main.protection.setup.SetupSecurityQuestionFragment
import com.notisaver.main.protection.VerifySecurityQuestionFragment
import com.notisaver.misc.collectWhenCreated
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class PasswordActivity : FullScreenActivity() {

    private lateinit var setupViewModel: SetupPasswordViewModel

    private lateinit var backButton: AppCompatImageButton

    private var action = -1

    private val pagerAdapter by lazy {
        PasswordFragmentStateAdapter(this)
    }

    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel = ViewModelProvider(
            this,
            SetupPasswordViewModel.createFactory(this.application)
        )[SetupPasswordViewModel::class.java]

        setContentView(R.layout.activity_password)

        action = intent.getIntExtra(NotisaveSetting.ACTIVITY_ACTION, -1)
        Timber.d("PasswordActivity-Test")
        try {

            when (action) {
                NotisaveSetting.ACTION_SETUP_PASSCODE -> {
                    pagerAdapter.add(
                        SetupPasscodeFragment()
                    )
                    pagerAdapter.add(
                        SetupSecurityQuestionFragment()
                    )
                    collectWhenCreated {
                        setupViewModel.passwordCreatedStateFlow.collectLatest {
                            if (it) {
                                viewPager2.setCurrentItem(1, true)
                            }
                        }
                    }
                }
                NotisaveSetting.ACTION_RETRIEVE_PASSCODE -> {
                    pagerAdapter.add(
                        SetupPasscodeFragment()
                    )
                    setupViewModel.setupRetrievePasswordFlow()
                    collectWhenCreated {
                        setupViewModel.passwordCreatedStateFlow.collectLatest {
                            if (it) finish()
                        }
                    }
                }
                NotisaveSetting.ACTION_VERIFY_SECURITY_QUESTION -> {
                    pagerAdapter.add(
                        VerifySecurityQuestionFragment()
                    )
                }
                else -> throw IllegalStateException("PasswordActivity wrong action")
            }

        } catch (e: IllegalStateException) {
            Timber.d(e.message)
            Toast.makeText(this, getString(R.string.please_try_again), Toast.LENGTH_SHORT).show()
            this.finish()
        }

        viewPager2 = findViewById(R.id.activity_password_view_pager)
        viewPager2.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
        }

        backButton = findViewById(R.id.activity_password_back_button)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private class PasswordFragmentStateAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        private var fragmentList = arrayListOf<Fragment>()

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }

        fun add(fragment: Fragment) {
            fragmentList.add(fragment)
        }
    }
}