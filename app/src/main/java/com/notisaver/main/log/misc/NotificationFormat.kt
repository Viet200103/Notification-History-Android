package com.notisaver.main.log.misc

import android.content.Context
import androidx.core.content.edit
import com.notisaver.database.NotisaveDatabase

class NotificationFormat(context: Context) {

    enum class Form(val value: Int){
        PACKAGE(2), GROUP(1), NONE(0)
    }


    private val formatRefers = context.getSharedPreferences(
        "NotificationForm", Context.MODE_PRIVATE
    )

    internal fun setForm(categoryId: String, form: Form) {
        formatRefers.edit(true) {
            putInt(getFormKey(categoryId), form.value)
        }
    }

    internal fun getForm(categoryId: String): Form {
        return when(formatRefers.getInt(getFormKey(categoryId), Form.GROUP.value)) {
            Form.PACKAGE.value -> Form.PACKAGE
            Form.NONE.value -> Form.NONE
            else -> Form.GROUP
        }
    }

    internal fun setSortMode(categoryId: String, sortMode: NotisaveDatabase.SortMode) {
        formatRefers.edit(true) {
            putInt(getSortKey(categoryId), sortMode.sortValue)
        }
    }

    internal fun getSortMode(categoryId: String): NotisaveDatabase.SortMode {
        return when (
            formatRefers.getInt(
                getSortKey(categoryId),
                NotisaveDatabase.SortMode.DESC.sortValue
            )
        ) {
            NotisaveDatabase.SortMode.ASC.sortValue -> NotisaveDatabase.SortMode.ASC
            else -> NotisaveDatabase.SortMode.DESC
        }
    }

    private fun getSortKey(categoryId: String) = "$categoryId.s"
    private fun getFormKey(categoryId: String) = "$categoryId.f"

    internal fun clear() {
        formatRefers.edit(true) {
            clear()
        }
    }
}