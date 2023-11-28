package com.notisaver.main.protection

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.biometric.BiometricPrompt
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import com.notisaver.R
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.start.AppStartViewModel
import com.notisaver.misc.findView
import com.notisaver.misc.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class ConfirmPasswordFragment : Fragment(R.layout.fragment_password) {

    private val confirmViewModel by viewModels<ConfirmPasswordViewModel>(this::requireActivity) {
        AppStartViewModel.createFactory(requireActivity().application)
    }


    private lateinit var enterPasswordView: TextInputLayout
    private lateinit var forgottenView: AppCompatTextView
    private lateinit var fingerprintView: AppCompatTextView
    private lateinit var warningView: AppCompatTextView
    private lateinit var confirmButton: CardView

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        warningView = findView(R.id.fragment_password_warning_view)
        enterPasswordView = findView(R.id.fragment_password_enter_password_view)
        forgottenView = findView(R.id.fragment_password_forgotten_view)
        confirmButton = findView(R.id.fragment_password_confirm_button)

        enterPasswordView.editText?.doAfterTextChanged { editable ->
            if (editable?.isNotEmpty() == true) {
                enterPasswordView.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            } else {
                enterPasswordView.endIconMode = TextInputLayout.END_ICON_NONE
            }
        }

//        confirmButton.setText(R.string.start)
        confirmButton.setOnClickListener {
            it.isEnabled
            launch(Dispatchers.Main) {
                try {
                    withContext(Dispatchers.IO) {
                        confirmViewModel.confirmPassword(getPassword())
                    }
                } catch (e: LocalPasswordManager.ConfirmWrongException) {
                    warningView.isVisible = true
                    warningView.setText(R.string.password_wrong)
                }
            }.invokeOnCompletion {
                confirmButton.isEnabled = true
            }
        }

        forgottenView.setOnClickListener {
            confirmViewModel.retrievePassword.value = true
        }

        fingerprintView = findView(R.id.fragment_password_fingerprint_view)
        if (
            NotisaveSetting.getInstance(requireContext()).isFingerprintEnabled()
        ) {
            setupConfirmByFingerprint()
            fingerprintView.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            fingerprintView.isVisible = false
        }
    }

    private fun setupConfirmByFingerprint() {
        executor = ContextCompat.getMainExecutor(requireActivity())

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    confirmViewModel.notifyFingerprintConfirmSuccess()
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.fingerprint_authentication))
            .setNegativeButtonText(getString(R.string.use_password))
            .setDescription(getString(R.string.scan_fingerprint_description))
            .build()

        if (NotisaveSetting.getInstance(requireContext()).isFingerprintEnabled()) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun getPassword() = enterPasswordView.editText?.text?.toString()?.trim() ?: ""
}