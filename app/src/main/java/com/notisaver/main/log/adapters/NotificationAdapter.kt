package com.notisaver.main.log.adapters

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.notisaver.main.log.intefaces.NotificationItem

abstract class NotificationAdapter<T : NotificationItem, VH : RecyclerView.ViewHolder> constructor(
    diffCallback: DiffUtil.ItemCallback<T>
) : PagingDataAdapter<T, VH>(diffCallback) {

    protected val selectedHashSet = HashSet<String>()

    internal var selectedCountListener: (Int) -> Unit = {}

    internal var selectionModeOnListener: () -> Unit = {}

    protected var isMultiSelectMode: Boolean = false

    internal fun turnOffSelectionMode() {
        isMultiSelectMode = false
        if (selectedHashSet.isNotEmpty()) {
            selectedHashSet.clear()
            notifyItemRangeChanged(0, this.itemCount)
        }
    }

    internal fun clearSelectedItem() {
        selectedHashSet.clear()
        selectedCountListener.invoke(0)
    }
}