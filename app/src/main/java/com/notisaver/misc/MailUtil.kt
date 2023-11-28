package com.notisaver.misc

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.notisaver.BuildConfig
import com.notisaver.R


class MailUtil {


    companion object {
        internal fun feedback(context: Context) {
            val i = Intent(Intent.ACTION_SEND)
            val appName = context.getString(R.string.app_name)
            val subject = "[${BuildConfig.VERSION_NAME}]$appName - ${context.getString(R.string.feedback)}"

            i.type = "message/rfc822"
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf("vietclient200103@gmail.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, subject)

            try {
                context.startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    R.string.not_fault_mail_browser,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}