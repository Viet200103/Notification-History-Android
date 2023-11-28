package com.notisaver.main.start

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.notisaver.R
import com.notisaver.misc.findView

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {
    private val viewModel by viewModels<AppStartViewModel>(this::requireActivity) {
        AppStartViewModel.createFactory(requireActivity().application)
    }

    private lateinit var welcomeView: AppCompatTextView
    private lateinit var contentView: AppCompatTextView
    private lateinit var acceptButton: CardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        welcomeView = findView(R.id.fragment_policy_welcome_view)
        welcomeView.text = getString(R.string.welcome_to, getString(R.string.app_name))

        val messageBuilder = SpannableStringBuilder(
            getString(R.string.usage_policy_summary)
        )

        var index = messageBuilder.indexOf("**")

        val policyHttps = getString(R.string.privacy_policy_https)
        val policyStr = getString(R.string.privacy_policy)

        messageBuilder.replace(index, index + 2, policyStr)

        messageBuilder.setSpan(
            object: ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(policyHttps))
                    )
                }

            }, index, index + policyStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        index = messageBuilder.indexOf("**")

        val termOfServiceHttps = getString(R.string.terms_of_service_https)
        val termOfServiceStr = getString(R.string.terms_of_service)

        messageBuilder.replace(index, index + 2, termOfServiceStr)

        messageBuilder.setSpan(
            object: ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(termOfServiceHttps))
                    )
                }

            }, index, index + termOfServiceStr.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        contentView = findView(R.id.fragment_policy_content_view)
        contentView.movementMethod = LinkMovementMethod.getInstance()
        contentView.text = messageBuilder

        acceptButton = findView(R.id.fragment_policy_accept_button)
        acceptButton.setOnClickListener {
            viewModel.acceptPolicy()
        }
    }
}