package com.notisaver

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class LanguageManagerTest {
    private val languageManager = LanguageManager.getInstance(
        ApplicationProvider.getApplicationContext()
    )


    @Test
    fun test_language_list() {
        val languageCodes = languageManager.languageCodes
        val languageNames = languageManager.languageNames

        Assert.assertTrue(
            languageCodes.size == 4 && languageNames.size == 4
        )
    }

    @Test
    fun test_language_name() {
        Assert.assertTrue(
            languageManager.getCurrentLanguageName("ada") == null
        )

        Assert.assertEquals(
            languageManager.getCurrentLanguageName(
                LanguageManager.SYSTEM_LANGUAGE_CODE
            ), R.string.default_language_system
        )

        Assert.assertEquals(
            languageManager.getCurrentLanguageName(
                LanguageManager.VI_LANGUAGE_CODE
            ), R.string.vietnam_language
        )

        Assert.assertEquals(
            languageManager.getCurrentLanguageName(
                LanguageManager.ES_LANGUAGE_CODE
            ), R.string.spanish_language
        )
    }
}