package com.notisaver.main.protection.setup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.notisaver.R
import com.notisaver.main.protection.LocalPasswordManager
import com.notisaver.misc.findView
import com.notisaver.misc.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetupPasscodeFragment : Fragment(R.layout.fragment_setup_passcode) {

    private val setupViewModel by viewModels<SetupPasswordViewModel>(this::requireActivity) {
        SetupPasswordViewModel.createFactory(requireActivity().application)
    }

    private lateinit var enterPasswordView: TextInputLayout
    private lateinit var confirmPasswordView: TextInputLayout
    private lateinit var warningView: AppCompatTextView
    private lateinit var createButton: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        warningView = findView(R.id.fragment_setup_passcode_warning_view)
        enterPasswordView = findView(R.id.fragment_setup_passcode_enter_password_view)
        confirmPasswordView = findView(R.id.fragment_setup_passcode_confirm_password_view)
        createButton = findView(R.id.fragment_setup_passcode_create_button)

        enterPasswordView.editText?.doAfterTextChanged { editable ->
            if (editable?.isNotEmpty() == true) {
                enterPasswordView.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            } else {
                enterPasswordView.endIconMode = TextInputLayout.END_ICON_NONE
            }
        }

        confirmPasswordView.editText?.doAfterTextChanged { editable ->
            if (editable?.isNotEmpty() == true) {
                confirmPasswordView.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            } else {
                confirmPasswordView.endIconMode = TextInputLayout.END_ICON_NONE
            }
        }

        if (setupViewModel.isRetrievePassword) {
            findView<View>(R.id.fragment_setup_passcode_advice_view).isVisible = false
        }

        setupCreatePassword()
    }

    private fun setupCreatePassword() {
        createButton.setOnClickListener {
            it.isEnabled = false
            launch(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        val password = getPassword()
                        val confirm = getConfirm()
                        setupViewModel.createPassword(password, confirm)
                    }
                    Toast.makeText(
                        requireContext().applicationContext,
                        R.string.setup_your_password_successfully,
                        Toast.LENGTH_SHORT
                    ).show()
                    setupViewModel.notifyPasswordCreated()
                } catch (e: LocalPasswordManager.PasswordLengthException) {
                    warningView.isVisible = true
                    warningView.setText(R.string.password_length_warning)
                } catch (e: LocalPasswordManager.PasswordNotMatchException) {
                    warningView.setText(R.string.password_not_match)
                    warningView.isVisible = true
                }
            }.invokeOnCompletion {
                createButton.isEnabled = true
            }

        }
    }

    private fun getPassword() = enterPasswordView.editText?.text?.toString()?.trim() ?: ""
    private fun getConfirm() = confirmPasswordView.editText?.text?.toString()?.trim() ?: ""

}