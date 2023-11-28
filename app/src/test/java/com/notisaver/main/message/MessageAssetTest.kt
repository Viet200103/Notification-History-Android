package com.notisaver.main.message

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.main.log.core.MessageAsset
import com.notisaver.misc.createPackageHashcode
import org.json.JSONException
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.lang.Exception

@Config
@RunWith(AndroidJUnit4::class)
class MessageAssetTest {

    private lateinit var messageAsset: MessageAsset

    @Before
    fun onCreate() {
        messageAsset = MessageAsset(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun test_isPrepared_not_contain_package() {
        val packageHashcode = createPackageHashcode("come.notisaver.test")

        try {
            messageAsset.isPrepared(packageHashcode)
        } catch (e: Exception) {
            Assert.assertEquals(e is JSONException, true)
        }

    }

    @Test
    fun test_isPrepared_contain_package() {
        val packageHashcode = createPackageHashcode("com.facebook.orca")

        val isPrepared = messageAsset.isPrepared(packageHashcode)

        Assert.assertEquals(isPrepared, false)
    }


    @Test
    fun test_updatePrepared() {
        val packageHashcode = createPackageHashcode("com.facebook.orca")

        messageAsset.updatePrepared(packageHashcode)

        val isPrepared = messageAsset.isPrepared(packageHashcode)

        Assert.assertEquals(isPrepared, true)
    }

    @Test
    fun onClose() {
        ApplicationProvider.getApplicationContext<Context>().deleteFile("message_json")
    }
}