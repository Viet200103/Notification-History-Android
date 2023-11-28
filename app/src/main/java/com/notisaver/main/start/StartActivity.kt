package com.notisaver.main.start

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.notisaver.R
import com.notisaver.main.activities.FullScreenActivity
import com.notisaver.main.log.misc.TimeLine
import com.notisaver.main.protection.activities.ConfirmPasswordActivity
import com.notisaver.misc.collectWhenCreated
import kotlinx.coroutines.flow.collectLatest


class StartActivity : FullScreenActivity() {

    private var _viewModel: AppStartViewModel? = null
    private val viewModel
        get() = _viewModel!!

    private val pagerAdapter: StartFragmentStateAdapter by lazy {
        StartFragmentStateAdapter(this)
    }
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TimeLine.getInstance(this).resetTodayTime()
        setContentView(R.layout.activity_start)

        _viewModel = ViewModelProvider(
            this,
            AppStartViewModel.createFactory(this.application)
        )[AppStartViewModel::class.java]

        collectWhenCreated {
            viewModel.startFlow.collectLatest {
                if (it) {
                    startActivity(
                        Intent(this@StartActivity, ConfirmPasswordActivity::class.java)
                    )
                    finish()
                }
            }
        }

        if (!viewModel.isPolicyAccepted) {
            pagerAdapter.add(WelcomeFragment())
        }

        viewPager2 = findViewById(R.id.activity_start_viewpager2)
        viewPager2.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }

    }

    private class StartFragmentStateAdapter(fragmentActivity: FragmentActivity) :
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