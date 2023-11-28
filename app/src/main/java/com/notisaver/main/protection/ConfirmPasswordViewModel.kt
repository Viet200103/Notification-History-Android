package com.notisaver.main.protection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfirmPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val _passwordManager = LocalPasswordManager.getInstance(application)

    private val _confirmStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(
        _passwordManager.isCreatedPassword().not()
    )
    internal val confirmPasswordStateFlow = _confirmStateFlow.asStateFlow()

    internal val isCreatedPassword
        get() = _passwordManager.isCreatedPassword()

    internal val isSecurityQuestionSetup
        get() = _passwordManager.isSecurityQuestionSetup

    internal val isSecurityQuestionSkipped
        get() = _passwordManager.isSecurityQuestionSkipped

    internal val retrievePassword = MutableStateFlow(false)

    @Throws(
        LocalPasswordManager.IntruderSecurityException::class,
        LocalPasswordManager.ConfirmWrongException::class,
        LocalPasswordManager.PasswordSetupException::class
    )
    internal fun confirmPassword(password: String) {
        _passwordManager.confirmPassword(password)
        _confirmStateFlow.value = true
    }

    internal fun notifyFingerprintConfirmSuccess() {
        _confirmStateFlow.value = true
    }

    companion object {

        @JvmStatic
        fun createFactory(application: Application) =
            object : ViewModelProvider.AndroidViewModelFactory() {

                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ConfirmPasswordViewModel::class.java)) {
                        @Suppress("unchecked_cast")
                        return ConfirmPasswordViewModel(application) as T
                    } else throw ClassCastException()
                }
            }
    }
}