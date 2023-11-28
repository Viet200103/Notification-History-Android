package com.notisaver

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.notisaver.main.NotisaveApplication

class LanguageManager(
    private val notisaveApplication: NotisaveApplication
) {
    private val languageMap = linkedMapOf(
        SYSTEM_LANGUAGE_CODE to R.string.default_language_system,
        EN_LANGUAGE_CODE to R.string.english_language,
        VI_LANGUAGE_CODE to R.string.vietnam_language,
        ES_LANGUAGE_CODE to R.string.spanish_language
    )

    internal val languageCodes
        get() = languageMap.keys.toList()

    internal val languageNames
        get() = languageMap.values.map {
            notisaveApplication.getString(it)
        }

    internal fun getCurrentLanguageCode(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return SYSTEM_LANGUAGE_CODE
        return locales[0]?.language ?: SYSTEM_LANGUAGE_CODE
    }

    internal fun getCurrentLanguageName(currentLangCode: String): Int? {
        return languageMap[currentLangCode]
    }

    internal fun changeLanguageInApp(languageCode: String) {
        if (languageCode == SYSTEM_LANGUAGE_CODE) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.getEmptyLocaleList()
            )
        } else {
            val localeList = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    companion object {
        internal const val SYSTEM_LANGUAGE_CODE = "system"
        internal const val EN_LANGUAGE_CODE = "en"
        internal const val VI_LANGUAGE_CODE = "vi"
        internal const val ES_LANGUAGE_CODE = "es"


        @Volatile
        private var Instances: LanguageManager? = null

        internal fun getInstance(notisaveApplication: NotisaveApplication): LanguageManager {
            return Instances ?: synchronized(this) {
                val instances = LanguageManager(notisaveApplication)
                instances.also {
                    Instances = it
                }
            }
        }
    }
}