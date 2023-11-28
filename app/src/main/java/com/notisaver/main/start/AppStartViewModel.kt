package com.notisaver.main.start

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.protection.LocalPasswordManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppStartViewModel(application: Application) : AndroidViewModel(application) {
    private val defaultPrefers = PreferenceManager.getDefaultSharedPreferences(application)


    internal val policyStateFlow = MutableStateFlow(isPolicyAccepted)


    internal val startFlow: StateFlow<Boolean> = policyStateFlow.asStateFlow()

    internal fun acceptPolicy() {
        viewModelScope.launch {
            defaultPrefers.edit(true) {
                putBoolean(NotisaveSetting.PRE_USAGE_POLICY, true)
            }
            policyStateFlow.value = true
        }
    }

    internal val isPolicyAccepted
        get() = defaultPrefers.getBoolean(
            NotisaveSetting.PRE_USAGE_POLICY, false
        )

    companion object {

        @JvmStatic
        fun createFactory(application: Application) =
            object : ViewModelProvider.AndroidViewModelFactory() {

                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AppStartViewModel::class.java)) {
                        @Suppress("unchecked_cast")
                        return AppStartViewModel(application) as T
                    } else throw ClassCastException()
                }
            }
    }
}