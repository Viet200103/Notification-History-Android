package com.notisaver.main.log.misc

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.notisaver.R
import com.notisaver.misc.findView

class SupportSelectionBarBinding private constructor(
    private val rootView: View
) {

    private val closeSelectModeButton: AppCompatImageView
    private val deleteButton: AppCompatImageView
    private val selectedCountView: AppCompatTextView

    init {
        selectedCountView = findView(R.id.selection_bar_count_view)
        deleteButton = findView(R.id.selection_bar_delete_button)
        closeSelectModeButton = findView(R.id.selection_bar_close_button)
    }

    internal fun setOnClickCloseListener(listener: View.OnClickListener) {
        closeSelectModeButton.setOnClickListener(listener)
    }

    internal fun setOnClickDeleteListener(listener: View.OnClickListener) {
        deleteButton.setOnClickListener(listener)
    }

    internal fun setContainerVisibility(visibility: Int) {
        rootView.visibility = visibility
    }

    internal fun setText(text: String) {
        selectedCountView.text = text
    }

    internal fun setCount(count: Int) {
        setText(
            rootView.context.getString(R.string.selected, count)
        )
    }

    companion object {

        @JvmStatic
        fun bind(
            containerLayout: View,
        ): SupportSelectionBarBinding {
            return SupportSelectionBarBinding(containerLayout)
        }
    }

    @Throws
    private fun <T : View> findView(@IdRes id: Int): T {
        return rootView.findView(id)
    }
}

class SelectionSupport : ISelectionSupport {

    var selectionObserver: ISelectionSupport.ISelectionObserver? = null
    var trigger: ISelectionSupport.ITriggerSelectionMode? = null

    override fun turnOnSelectionMode() {
        trigger?.turnOnSelectionMode()
    }

    override fun notifySelectedCountChange(count: Int) {
        trigger?.onSelectedCountChange(count)
    }

    override fun closeSelection() {
        trigger?.turnOffSelectionMode()
        selectionObserver?.onSelectionClose()
    }

    override fun performSelectionDelete() {
        selectionObserver?.onSelectedDelete()
    }
}

interface ISelectionSupport {
    interface ISelectionObserver {
        fun onSelectionClose()
        fun onSelectedDelete()
    }

    interface ITriggerSelectionMode {
        var isSelecting: Boolean
        fun turnOnSelectionMode()
        fun onSelectedCountChange(count: Int)
        fun turnOffSelectionMode()
    }

    fun turnOnSelectionMode()

    fun notifySelectedCountChange(count: Int)

    fun closeSelection()

    fun performSelectionDelete()
}