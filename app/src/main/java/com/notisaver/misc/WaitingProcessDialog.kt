package com.notisaver.misc

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.button.MaterialButton
import com.notisaver.R

class WaitingProcessDialog(context: Context) : Dialog(context, R.style.Theme_Notisaver_AlertDialog_DayNight) {

    private val cancelButton by lazy<MaterialButton> {
        findViewById(R.id.dialog_waiting_process_cancel_button)
    }

    private val contentView by lazy<AppCompatTextView> {
        findViewById(R.id.dialog_waiting_process_content_view)
    }

    init {
        setContentView(R.layout.dialog_waiting_process)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    internal fun setContent(@StringRes contentId: Int) {
        contentView.setText(contentId)
    }

    internal fun setCancelListener(listener: () -> Unit) {
        cancelButton.setOnClickListener {
            setContent(R.string.canceling)
            listener.invoke()
        }
    }
}