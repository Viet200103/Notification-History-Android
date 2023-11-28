package com.notisaver.main.log.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notisaver.R
import com.notisaver.misc.findView

class NotificationLoadingAdapter(
    private val retry: () -> Unit
): LoadStateAdapter<NotificationLoadingAdapter.LoadStateHolder>() {

    override fun onBindViewHolder(holder: LoadStateHolder, loadState: LoadState) {
        if (loadState is LoadState.Error) {
            retry.invoke()
            return
        }

        holder.containerLayout.isVisible = loadState is LoadState.Loading
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateHolder {
        return LoadStateHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_notification_loading, parent, false
            )
        )
    }

    class LoadStateHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val containerLayout = findView<FrameLayout>(R.id.item_notification_loading_container_layout)
    }
}