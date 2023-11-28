package com.notisaver

import com.notisaver.main.log.activities.NotificationPackageActivity
import com.notisaver.main.log.fragments.NotificationInfoBottomSheetTest
import com.notisaver.main.settings.OtherActivityTest
import com.notisaver.main.start.PolicyFragmentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@SuiteClasses(
    NotificationInfoBottomSheetTest::class,
    NotificationPackageActivity::class,
    PolicyFragmentTest::class,
    OtherActivityTest::class,
)
internal class SuiteClassTestAll