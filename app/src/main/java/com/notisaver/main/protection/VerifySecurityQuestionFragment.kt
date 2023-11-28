package com.notisaver.main.protection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.notisaver.R
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.protection.setup.SetupPasswordViewModel
import com.notisaver.misc.findView

class VerifySecurityQuestionFragment : Fragment(R.layout.fragment_retrieve_password) {

    private val setupViewModel by viewModels<SetupPasswordViewModel>(this::requireActivity) {
        SetupPasswordViewModel.createFactory(requireActivity().application)
    }

    private lateinit var confirmButton: MaterialButton
    private lateinit var answerView: TextInputLayout
    private lateinit var questionView: AppCompatTextView
    private lateinit var warningView: AppCompatTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        questionView = findView(R.id.fragment_retrieve_password_question_view)
        answerView = findView(R.id.fragment_retrieve_password_answer_view)
        confirmButton = findView(R.id.fragment_retrieve_password_confirm_button)
        warningView = findView(R.id.fragment_retrieve_password_warning_view)

        val qTextId = setupViewModel.getQuestionTextId()
        if (qTextId == null) {
            questionView.setText(R.string.warning_question_skipped)
            questionView.isEnabled = false
            answerView.isEnabled = false
            confirmButton.isEnabled = false
            return
        }

        questionView.setText(qTextId)
        confirmButton.setOnClickListener {
            val intent = Intent()
            try {
                setupViewModel.confirmAnswerSecurityQuestion(getAnswer())

                intent.putExtra(
                    NotisaveSetting.VERIFY_QUESTION_CONFIRM,
                    NotisaveSetting.CONFIRM_SUCCESS
                )

                val activity = requireActivity()
                activity.setResult(
                    Activity.RESULT_OK,
                    intent
                )
                activity.finish()

            } catch (e: LocalPasswordManager.ConfirmWrongException) {
                intent.putExtra(
                    NotisaveSetting.VERIFY_QUESTION_CONFIRM,
                    NotisaveSetting.CONFIRM_FAILED
                )
                warningView.isVisible = true
            }
        }
    }

    private fun getAnswer() = answerView.editText?.text?.toString()?.trim().orEmpty()

}