package com.notisaver.main.utilities

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.notisaver.R

class SupportMaterialAlertDialog {
    companion object {

        @JvmStatic
        fun createAlertDialog(
            context: Context,
            title: String?,
            iconId: Int,
            text: CharSequence,
            positiveTextId: Int = R.string.ok,
            negativeTextId: Int = R.string.cancel,
            negativeListener: (() -> Unit)? = null,
            positiveListener: (() -> Unit)?
        ): MaterialAlertDialogBuilder {
            return MaterialAlertDialogBuilder(context, R.style.Theme_Notisaver_AlertDialog_DayNight)
                .setMessage(text)
                .setIcon(iconId)
                .setTitle(title)
                .setNegativeButton(negativeTextId) { dialog, _ ->
                    negativeListener?.invoke()
                    dialog.dismiss()
                }
                .setPositiveButton(positiveTextId) { dialog, _ ->
                    positiveListener?.invoke()
                    dialog.dismiss()
                }
        }

        fun createAlertDialog(
            context: Context,
            titleId: Int,
            iconId: Int,
            contentId: Int,
            positiveTextId: Int = R.string.ok,
            negativeTextId: Int = R.string.cancel,
            negativeListener: (() -> Unit)? = null,
            positiveListener: (() -> Unit)? = null
        ): MaterialAlertDialogBuilder {
            return createAlertDialog(
                context,
                context.getString(titleId),
                iconId,
                context.getString(contentId),
                positiveTextId = positiveTextId,
                negativeTextId = negativeTextId,
                positiveListener = positiveListener,
                negativeListener = negativeListener
            )
        }

        @JvmStatic
        fun createCancelWhenPushDialog(context: Context, action: () -> Unit) = createAlertDialog(
            context,
            context.getString(R.string.mute_notification),
            R.drawable.ic_warning,
            context.getString(
                R.string.waring_cancel_when_push,
                context.getString(R.string.app_name)
            ) + "\n" + context.getString(R.string.do_you_want_to_contiune),
            positiveListener = action
        )

        @JvmStatic
        fun createWarningLogOngoingDialog(
            context: Context,
            appLabel: CharSequence,
            action: () -> Unit
        ) = createAlertDialog(
            context,
            (context.getString(R.string.pre_log_ongoing) + ": " + appLabel),
            R.drawable.ic_warning,
            context.getString(R.string.warning_log_ongoing) + "\n" + context.getString(R.string.do_you_want_to_contiune),
            positiveListener = action
        )

        @JvmStatic
        fun createWarningExcludeDialog(
            context: Context,
            appLabel: CharSequence,
            action: () -> Unit
        ) =
            createAlertDialog(
                context,
                (context.getString(R.string.option_exclude_app_short) + ": " + appLabel),
                R.drawable.ic_warning,
                context.getString(
                    R.string.warning_exclude_app,
                    context.getString(R.string.app_name)
                ) + "\n" + context.getString(R.string.do_you_want_to_contiune),
                positiveListener = action
            )

        @JvmStatic
        fun createWarningDeleteDialog(
            context: Context,
            action: () -> Unit
        ) = createAlertDialog(
            context,
            title = null,
            R.drawable.ic_warning,
            text = context.getString(R.string.waring_delete_notification) + "\n" + context.getString(
                R.string.do_you_want_to_contiune
            ),
            positiveListener = action
        )
    }
}