package com.notisaver.main.protection.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notisaver.main.protection.LocalPasswordManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SetupPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val _passwordManager = LocalPasswordManager.getInstance(application)

    private val _passwordCreatedStateFlow by lazy {
        MutableStateFlow(_passwordManager.isCreatedPassword())
    }

    internal val passwordCreatedStateFlow by lazy {
        _passwordCreatedStateFlow.asStateFlow()
    }

    internal var isRetrievePassword = false
        private set

    @Throws
    internal fun createPassword(password: String, confirm: String) {
        _passwordManager.createPassword(password, confirm)
    }

    internal fun notifyPasswordCreated() {
        _passwordCreatedStateFlow.value = true
    }

    internal fun getPasswordManager() = _passwordManager

    internal fun saveSecurityAnswer(questionCode: Int, answer: String) {
        _passwordManager.saveSecurityAnswer(questionCode, answer)
    }

    internal fun getSecurityQuestionCode(questionRes: Int): Int {
        return _passwordManager.getSecurityQuestionCode(questionRes)
    }

    internal fun skipSecurityQuestion() {
        _passwordManager.skipSecurityQuestion()
    }

    internal fun getQuestionTextId(): Int? = _passwordManager.getQuestionTextId()

    internal fun setupRetrievePasswordFlow() {
        _passwordCreatedStateFlow.value = false
        isRetrievePassword = true
    }

    @Throws
    internal fun confirmAnswerSecurityQuestion(answer: String) = _passwordManager.confirmAnswerSecurityQuestion(answer)

    companion object {

        @JvmStatic
        fun createFactory(application: Application) =
            object : ViewModelProvider.AndroidViewModelFactory() {

                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SetupPasscodeFragment::class.java)) {
                        @Suppress("unchecked_cast")
                        return SetupPasswordViewModel(application) as T
                    } else throw ClassCastException()
                }
            }
    }
}