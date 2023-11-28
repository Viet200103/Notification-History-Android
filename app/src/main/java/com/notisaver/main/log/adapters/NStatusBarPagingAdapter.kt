package com.notisaver.main.log.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.database.entities.BaseNotificationStyle
import com.notisaver.database.entities.NStatusBarGroup
import com.notisaver.database.entities.NotificationLog
import com.notisaver.main.log.intefaces.OnStatusBarNotificationClickListener
import com.notisaver.main.log.core.LogHelper
import com.notisaver.main.log.core.NotificationHandler
import com.notisaver.main.log.misc.TimeLine
import com.notisaver.main.manager.AppInfoManager
import com.notisaver.misc.changeVisibleView
import com.notisaver.misc.findView
import java.io.File

class NStatusBarPagingAdapter(
    private val appManager: AppInfoManager,
    private val nItemClickListener: OnStatusBarNotificationClickListener,
) : NotificationAdapter<NStatusBarGroup, NStatusBarPagingAdapter.NStatusBarHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NStatusBarHolder {
        return NStatusBarHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_status_bar_notification_group, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: NStatusBarHolder, position: Int) {
        val nStatusBarGroup = getItem(position)

        if (nStatusBarGroup == null) {
            holder.clear()
            return
        }

        val nLog = nStatusBarGroup.header

        val appInfo = appManager.getAppInformationLazyCache(nLog.packageHashcode)
        holder.bindAppHeader(appInfo)

        holder.bindHeader(nLog)
        holder.bindNCountMore(nStatusBarGroup.nCount)

        if (isMultiSelectMode) {
            holder.setSelected(selectedHashSet.contains(nStatusBarGroup.sbnKeyHashcode))
        } else {
            holder.setSelected(false)
        }

        holder.containerLayout.setOnClickListener {
            if (isMultiSelectMode) {
                setHolderSelected(holder, nStatusBarGroup)
            } else {
                nItemClickListener.onStatusBarNotificationClick(nStatusBarGroup)
            }
        }

        holder.containerLayout.setOnLongClickListener {

            if (!isMultiSelectMode) {
                isMultiSelectMode = true
                selectionModeOnListener.invoke()
            }

            setHolderSelected(holder, nStatusBarGroup)
            true
        }

//        holder.countGroupView.setOnClickListener {
//            if (isMultiSelectMode) {
//                setHolderSelected(holder, nStatusBarGroup)
//            } else {
//                nItemClickListener.onMoreNotificationClick(nStatusBarGroup)
//            }
//        }
    }

    private fun setHolderSelected(holder: NStatusBarHolder, nStatusBarGroup: NStatusBarGroup) {
        val isSelected = !selectedHashSet.contains(nStatusBarGroup.sbnKeyHashcode)
        holder.setSelected(isSelected)
        changeSelectedStateItem(nStatusBarGroup, isSelected)
    }

    private fun changeSelectedStateItem(nStatusBarGroup: NStatusBarGroup, isSelected: Boolean) {
        if (isSelected) {
            selectedHashSet.add(nStatusBarGroup.sbnKeyHashcode)
        } else {
            selectedHashSet.remove(nStatusBarGroup.sbnKeyHashcode)
        }
        selectedCountListener.invoke(selectedHashSet.size)
    }

    internal fun getItemsSelected(): List<NStatusBarGroup> {
        val list = arrayListOf<NStatusBarGroup>()
        snapshot().forEach {
            if (it != null) {
                if (selectedHashSet.contains(it.sbnKeyHashcode)) {
                    list.add(it)
                }
            }
        }
        return list
    }

    class NStatusBarHolder(itemView: View) : ViewHolder(itemView) {

        internal val containerLayout: LinearLayoutCompat = findView(
            R.id.item_recent_notification_layout_container
        )

        private val appIconView = findView<ShapeableImageView>(
            R.id.layout_small_icon_app_view
        )

        internal val appHeaderContainerLayout = findView<LinearLayoutCompat>(
            R.id.layout_small_icon_app_container_layout
        )

        private val appNameTextView = findView<AppCompatTextView>(
            R.id.layout_small_icon_app_name_view
        )

        private val countGroupView = findView<AppCompatTextView>(
            R.id.item_recent_notification_count_group
        )

        private val subTextView = findView<AppCompatTextView>(R.id.item_recent_notification_sub_text)
        private val timeTextView = findView<AppCompatTextView>(R.id.item_recent_notification_time)
        private val titleNoView = findView<AppCompatTextView>(R.id.item_recent_notification_title)
        private val contentView = findView<AppCompatTextView>(R.id.item_recent_notification_content_text)
        private val largeIconView = findView<ShapeableImageView>(R.id.item_status_bar_notification_group_large_icon_view)

        internal fun bindAppHeader(appInfo: AppInformation?) {
            appIconView.setImageDrawable(
                appInfo?.icon ?: ContextCompat.getDrawable(context, R.drawable.ic_extension)
            )

            appNameTextView.text =
                appInfo?.name ?: appInfo?.packageName ?: context.getString(R.string.unknown_app)
        }

        internal fun bindHeader(nLog: NotificationLog) {
            val oNotification = nLog.notification

            oNotification.title?.let {
                changeVisibleView(titleNoView, true)
                titleNoView.text = it
            } ?: changeVisibleView(titleNoView, false)

            changeVisibleView(contentView, true)

            oNotification.contentText?.let {
                contentView.text = it
            } ?: kotlin.run {
                if (nLog.templateId == BaseNotificationStyle.CUSTOM_ID) {
                    contentView.setText(R.string.custom_content)
                } else {
                    changeVisibleView(contentView, false)
                }
            }

            oNotification.subText?.let {
                changeVisibleView(subTextView, true)
                subTextView.text = it
            } ?: changeVisibleView(subTextView, false)

            if (oNotification.isLargeIcon) {
                val largeIconFile = File(
                    LogHelper.getPackageHashcodeFolder(itemView.context, oNotification.packageHashcode),
                    NotificationHandler.createLargeIconFileName(oNotification.logId)
                )

                largeIconView.isVisible = true
                Glide.with(itemView)
                    .load(largeIconFile)
                    .placeholder(R.drawable.ic_image_not_found)
                    .into(largeIconView)
            } else {
                clearLargeIcon()
            }

            try {
                setTimeLine(
                    TimeLine.getInstance(itemView.context).formatTime(oNotification.timePost)
                )
                timeTextView.visibility = View.VISIBLE
            } catch (e: Exception) {
                setTimeLine("")
                timeTextView.visibility = View.GONE
            }
        }

        private fun setTimeLine(time: CharSequence) {
            timeTextView.text = time
        }

        internal fun bindNCountMore(nCount: Int) {
            if (nCount <= 1) {
                changeVisibleView(countGroupView, false)
            } else {
                changeVisibleView(countGroupView, true)
                countGroupView.text = "+".plus("${nCount - 1}")
            }
        }

        internal fun setSelected(isSelected: Boolean) {
            containerLayout.isSelected = isSelected
            countGroupView.isSelected = isSelected
        }

        internal fun clear() {
            appIconView.setImageDrawable(null)
            subTextView.text = null
            titleNoView.text = null
            countGroupView.text = null
            appNameTextView.text = null
            timeTextView.text = null
            containerLayout.setOnClickListener(null)
            clearLargeIcon()
        }

        private fun clearLargeIcon() {
            Glide.with(itemView).clear(largeIconView)
            largeIconView.isInvisible = true
            largeIconView.setImageDrawable(null)
        }

        internal val context
            get() = itemView.context
    }

    companion object {
        @JvmStatic
        internal val diffUtil
            get() = object : DiffUtil.ItemCallback<NStatusBarGroup>() {
                override fun areItemsTheSame(
                    oldItem: NStatusBarGroup,
                    newItem: NStatusBarGroup
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: NStatusBarGroup,
                    newItem: NStatusBarGroup
                ): Boolean {
                    return oldItem == newItem
                }

            }
    }
}