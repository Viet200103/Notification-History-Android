package com.notisaver.main.protection.setup

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.notisaver.R
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.findView

class SetupSecurityQuestionFragment : Fragment(R.layout.fragment_setup_question_security) {

    private var questionPositionSelected: Int = 0

    private val setupViewModel by viewModels<SetupPasswordViewModel>(this::requireActivity) {
        SetupPasswordViewModel.createFactory(requireActivity().application)
    }

    private lateinit var confirmButton: MaterialButton

    private lateinit var questionView: TextInputLayout
    private lateinit var answerView: TextInputLayout
    private lateinit var warningView: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback {
            SupportMaterialAlertDialog.createAlertDialog(
                requireContext(),
                R.string.retrieve_password,
                R.drawable.ic_warning,
                R.string.warning_skip_security_question,
                positiveListener = {
                    setupViewModel.skipSecurityQuestion()
                    requireActivity().finish()
                }
            ).show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionList = setupViewModel.getPasswordManager().getSecurityQuestionIds()
        val questionTextList = questionList.map {
            getString(it)
        }

        questionView = findView(R.id.fragment_setup_question_security_question_view)
        questionView.editText.let {
            it as AutoCompleteTextView
            it.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    questionTextList
                )
            )
            it.setText(questionTextList[questionPositionSelected], false)
            it.setOnItemClickListener { _, _, position, _ ->
                questionPositionSelected = position
            }
        }

        answerView = findView(R.id.fragment_setup_question_security_answer_view)
        warningView = findView(R.id.fragment_setup_question_security_warning_view)

        confirmButton = findView(R.id.fragment_setup_question_security_confirm_button)
        confirmButton.setOnClickListener {
            val answer = getAnswer()
            if (answer.isEmpty()) {
                warningView.isVisible = true
            } else {
                setupViewModel.saveSecurityAnswer(
                    setupViewModel.getSecurityQuestionCode(questionList[questionPositionSelected]),
                    answer
                )
                Toast.makeText(
                    requireContext().applicationContext,
                    R.string.setup_security_question_successfully,
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }
    }

    private fun getAnswer() = answerView.editText?.text?.toString()?.trim().orEmpty()
}