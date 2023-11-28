package com.notisaver.main.log.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.notisaver.R
import com.notisaver.database.entities.NotificationLog
import com.notisaver.main.log.misc.NotificationInfoItemBinding
import com.notisaver.main.manager.AppInfoManager
import com.notisaver.misc.findView

class NormalNotificationPagingAdapter(
    private val appInfoManager: AppInfoManager,
    private val listener: (NotificationLog) -> Unit
) : NotificationAdapter<NotificationLog, NormalNotificationPagingAdapter.DetailInfoHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailInfoHolder {
        return DetailInfoHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_notification_log_detail_info, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DetailInfoHolder, position: Int) {
        val nLog = getItem(position)

        if (nLog == null) {
            holder.binding.clearView()
            return
        }

        holder.binding.resetView()
        holder.binding.bindNotificationLog(nLog)

        holder.binding.bindAppInfo(
            appInfoManager.getAppInformationLazyCache(nLog.packageHashcode)
        )

        if (isMultiSelectMode) {
            holder.setSelected(
                selectedHashSet.contains(nLog.logId)
            )
        } else {
            holder.setSelected(false)
        }

        holder.containerLayout.setOnLongClickListener {

            if (!isMultiSelectMode) {
                isMultiSelectMode = true
                selectionModeOnListener.invoke()
            }

            setHolderSelected(holder, nLog)
            true
        }

        holder.containerLayout.setOnClickListener {
            if (isMultiSelectMode) {
                setHolderSelected(holder, nLog)
            } else {
                listener.invoke(nLog)
            }
        }
    }

    private fun setHolderSelected(holder: DetailInfoHolder, notificationLog: NotificationLog) {
        val isSelect = !selectedHashSet.contains(notificationLog.logId)
        holder.setSelected(isSelect)
        changeSelectedStateItem(notificationLog, isSelect)
    }

    private fun changeSelectedStateItem(notificationLog: NotificationLog, isSelected: Boolean) {
        if (isSelected) {
            selectedHashSet.add(notificationLog.logId)
        } else {
            selectedHashSet.remove(notificationLog.logId)
        }
        selectedCountListener.invoke(selectedHashSet.size)
    }

    fun getItemsSelected(): List<NotificationLog> {
        val list = arrayListOf<NotificationLog>()
        snapshot().forEach {
            if (it != null) {
                if (selectedHashSet.contains(it.logId)) {
                    list.add(it)
                }
            }
        }
        return list
    }

    class DetailInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val containerLayout = findView<FrameLayout>(R.id.item_notification_log_detail_info_container)
        val binding = NotificationInfoItemBinding.bind(itemView)

        fun setSelected(isSelected: Boolean) {
            containerLayout.isSelected = isSelected
        }
    }

    companion object {
        @JvmStatic
        internal val diffUtil
            get() = object : DiffUtil.ItemCallback<NotificationLog>() {
                override fun areItemsTheSame(
                    oldItem: NotificationLog,
                    newItem: NotificationLog
                ): Boolean {
                    return oldItem.id == newItem.id
                }


                override fun areContentsTheSame(
                    oldItem: NotificationLog,
                    newItem: NotificationLog
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}

