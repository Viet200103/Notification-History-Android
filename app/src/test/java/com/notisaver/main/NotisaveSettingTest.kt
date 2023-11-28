package com.notisaver.main

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class NotisaveSettingTest {

    private lateinit var notisaveSetting: NotisaveSetting
    private lateinit var defaultShare: SharedPreferences

    @Before
    fun onCreate() {
        val context: Context = ApplicationProvider.getApplicationContext()
        notisaveSetting = NotisaveSetting.getInstance(context)
        defaultShare = PreferenceManager.getDefaultSharedPreferences(context)
    }


    @Test
    fun test_fingerprint_default() {
        Assert.assertEquals(
            notisaveSetting.isFingerprintEnabled(), false
        )
    }

    @Test
    fun test_enable_fingerprint() {
        defaultShare.edit(true) {
            putBoolean(NotisaveSetting.PRE_ENABLE_FINGERPRINT, true)
        }

        Assert.assertEquals(
            notisaveSetting.isFingerprintEnabled(), true
        )
    }

    @Test
    fun test_scan_app_default() {
        Assert.assertEquals(
            notisaveSetting.isAppOnDeviceLoaded, false
        )
    }

    @Test
    fun test_scanned_app() {
        defaultShare.edit(true) {
            putBoolean(NotisaveSetting.PRE_APP_DEVICE_LOADED, true)
        }

        Assert.assertEquals(
            notisaveSetting.isAppOnDeviceLoaded, true
        )
    }


    @Test
    fun test_screen_capture_default() {
        Assert.assertEquals(
            notisaveSetting.isScreenCaptureEnabled, false
        )
    }

    @Test
    fun test_enable_screen_capture() {
        defaultShare.edit(true) {
            putBoolean(NotisaveSetting.PRE_SCREEN_CAPTURE, true)
        }

        Assert.assertEquals(
            notisaveSetting.isScreenCaptureEnabled, true
        )
    }

    @After
    fun onClose() {
        defaultShare.edit(true) {
            clear()
        }
    }
}