package com.notisaver.main.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.notisaver.BuildConfig
import com.notisaver.R
import com.notisaver.misc.findView

class AboutFragment : Fragment(R.layout.fragment_about) {
    private lateinit var versionView: AppCompatTextView

    private lateinit var checkUpdateView: LinearLayoutCompat
    private lateinit var termServiceView: LinearLayoutCompat
    private lateinit var policyView: LinearLayoutCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appVersion = BuildConfig.VERSION_NAME

        versionView = findView(R.id.fragment_about_version_view)
        versionView.text = appVersion

        checkUpdateView = findView(R.id.fragment_about_check_for_update)
        checkUpdateView.setOnClickListener {
           // TODO
        }


        termServiceView = findView(R.id.fragment_about_term_of_service)
        termServiceView.setOnClickListener {
           // TODO
        }


        policyView = findView(R.id.fragment_about_privacy_policy)
        policyView.setOnClickListener {
           // TODO
        }
    }

}