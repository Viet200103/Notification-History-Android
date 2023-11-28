package com.notisaver.main.protection.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.notisaver.R
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.RETURN_RESULT
import com.notisaver.main.activities.FullScreenActivity
import com.notisaver.main.protection.ConfirmPasswordFragment
import com.notisaver.main.protection.ConfirmPasswordViewModel
import com.notisaver.main.start.LoadingActivity
import com.notisaver.misc.collectWhenCreated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest

class ConfirmPasswordActivity : FullScreenActivity() {

    private lateinit var verifyQuestionActivityLauncher: ActivityResultLauncher<Intent>

    private lateinit var backButton: AppCompatImageButton

    private var _confirmViewModel: ConfirmPasswordViewModel? = null
    private val confirmViewModel
        get() = _confirmViewModel!!

    private var isReturnResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerVerifyQuestionActivityLauncher()
        setContentView(R.layout.activity_confirm_password)

        _confirmViewModel = ViewModelProvider(
            this,
            ConfirmPasswordViewModel.createFactory(this.application)
        )[ConfirmPasswordViewModel::class.java]

        isReturnResult = intent.getBooleanExtra(RETURN_RESULT, false)
        backButton = findViewById(R.id.activity_confirm_password_back_button)

        if (isReturnResult) {
            backButton.isVisible = true
            backButton.setOnClickListener {
                setResultReturn(
                    NotisaveSetting.CONFIRM_FAILED
                )
                finish()
            }
        }

        if (confirmViewModel.isCreatedPassword) {

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction().add(
                    R.id.activity_confirm_password_fragment_container, ConfirmPasswordFragment()
                ).commitNow()
            }

            setupRecreateQuestionSecurity()
            setupListenResultConfirm()
        } else {
            startLoadingActivity()
        }

        setupRetrievePassword()
    }

    private fun setupRecreateQuestionSecurity() {
        if (!confirmViewModel.isSecurityQuestionSkipped && !confirmViewModel.isSecurityQuestionSetup) {
            val intent = Intent(
                this, PasswordActivity::class.java
            )
            intent.putExtra(NotisaveSetting.ACTIVITY_ACTION, NotisaveSetting.ACTION_SETUP_PASSCODE)

            startActivity(intent)
        }
    }

    private fun setupListenResultConfirm() {
        collectWhenCreated(Dispatchers.IO) {
            confirmViewModel.confirmPasswordStateFlow.collectLatest {
                if (it) {
                    if (isReturnResult) {

                        setResultReturn(
                            NotisaveSetting.CONFIRM_SUCCESS
                        )
                        finish()

                    } else startLoadingActivity()
                }
            }
        }
    }

    private fun setupRetrievePassword() {
        collectWhenCreated(Dispatchers.IO) {
            confirmViewModel.retrievePassword.collectLatest {
                if (it) {
                    val intent = Intent(
                        this, PasswordActivity::class.java
                    )
                    intent.putExtra(
                        NotisaveSetting.ACTIVITY_ACTION,
                        NotisaveSetting.ACTION_VERIFY_SECURITY_QUESTION
                    )
                    verifyQuestionActivityLauncher.launch(intent)

                    confirmViewModel.retrievePassword.value = false
                }
            }
        }
    }

    private fun registerVerifyQuestionActivityLauncher() {
        verifyQuestionActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (
                result.resultCode == Activity.RESULT_OK &&
                result.data?.getIntExtra(
                    NotisaveSetting.VERIFY_QUESTION_CONFIRM, NotisaveSetting.CONFIRM_FAILED
                ) == NotisaveSetting.CONFIRM_SUCCESS
            ) {
                val intent = Intent(
                    this, PasswordActivity::class.java
                )
                intent.putExtra(
                    NotisaveSetting.ACTIVITY_ACTION,
                    NotisaveSetting.ACTION_RETRIEVE_PASSCODE
                )

                startActivity(intent)
            }
        }
    }

    private fun startLoadingActivity() {
        startActivity(
            Intent(this@ConfirmPasswordActivity, LoadingActivity::class.java)
        )
        finish()
    }

    private fun setResultReturn(resultCode: Int) {
        val intent = Intent().apply {
            putExtra(
                NotisaveSetting.PASSWORD_CONFIRM,
                resultCode
            )
        }

        setResult(RESULT_OK, intent)
    }
}