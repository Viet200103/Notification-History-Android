package com.notisaver.main.log.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.database.entities.NotificationPackage
import com.notisaver.database.entities.ShortNotification
import com.notisaver.main.log.adapters.PackagePagingAdapter.*
import com.notisaver.main.log.intefaces.NotificationItem
import com.notisaver.main.log.intefaces.OnPackageClickListener
import com.notisaver.main.log.misc.TimeLine
import com.notisaver.main.manager.AppInfoManager
import com.notisaver.misc.findView

class PackagePagingAdapter(
    private val appInfoManager: AppInfoManager,
    private val packageListener: OnPackageClickListener
) : NotificationAdapter<NotificationPackage, PackageViewHolder>(diffUtil) {

    companion object {
        internal val diffUtil
            get() = object : DiffUtil.ItemCallback<NotificationPackage>() {
                override fun areItemsTheSame(
                    oldItem: NotificationPackage,
                    newItem: NotificationPackage
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: NotificationPackage,
                    newItem: NotificationPackage
                ): Boolean {
                    return oldItem == newItem
                }

            }
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val notificationPackage = getItem(position)

        if (notificationPackage == null) {
            holder.clear()
            return
        }

        holder.bindAppHeader(
            appInfoManager.getAppInformationLazyCache(notificationPackage.packageHashcode)
        )

        holder.bindNotificationPackage(notificationPackage)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        return PackageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_notification_package_group, parent, false
            ), packageListener
        )
    }

    class PackageViewHolder(
        itemView: View,
        private val packageListener: OnPackageClickListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val containerLayout = findView<LinearLayoutCompat>(
            R.id.item_notification_package_group_layout_container
        )

        private val appIconView = findView<ShapeableImageView>(
            R.id.item_notification_package_group_icon_view
        )

        private val appNameTextView = findView<AppCompatTextView>(
            R.id.item_notification_package_group_name_view
        )

        private val countView = findView<AppCompatTextView>(
            R.id.item_notification_package_group_count_view
        )

        private val recyclerView: RecyclerView = findView(
            R.id.item_notification_package_group_recycler_view
        )

        private val shortNotificationAdapter = ShortNotificationAdapter(packageListener::onNotificationClick)

        private var notificationPackage: NotificationPackage? = null

        init {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = shortNotificationAdapter

            containerLayout.setOnClickListener {
                notificationPackage?.let {
                    packageListener.onPackageClick(it.packageHashcode, it)
                }
            }
        }

        internal fun bindNotificationPackage(notificationPackage: NotificationPackage) {
            this.notificationPackage = notificationPackage


            bindCount(
                notificationPackage.getNumberOfNotification()
            )

            bindReview(
                notificationPackage.getReviewList()
            )
        }

        internal fun bindAppHeader(appInfo: AppInformation?) {
            appIconView.setImageDrawable(
                appInfo?.icon ?: ContextCompat.getDrawable(context, R.drawable.ic_extension)
            )

            appNameTextView.text =
                appInfo?.name ?: appInfo?.packageName ?: context.getString(R.string.unknown_app)
        }

        private fun bindCount(count: Int) {
            countView.text = itemView.resources.getQuantityString(
                R.plurals.number_notification, count, count
            )
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun bindReview(list: List<ShortNotification>) {
            shortNotificationAdapter.subList = list
            shortNotificationAdapter.notifyDataSetChanged()
        }

        internal fun clear() {
            bindReview(emptyList())
            appIconView.setImageDrawable(null)
            countView.text = null
            appNameTextView.text = null
            containerLayout.setOnClickListener(null)
        }

        internal val context
            get() = itemView.context


        private class ShortNotificationAdapter(
            private val onNotificationClick: (NotificationItem) -> Unit
        ) : RecyclerView.Adapter<ShortNotificationHolder>() {
            var subList: List<ShortNotification> = listOf()

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ShortNotificationHolder {
                return ShortNotificationHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_short_notification, parent, false)
                )
            }

            override fun getItemCount(): Int {
                return subList.size
            }

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: ShortNotificationHolder, position: Int) {
                val shortNotification = subList[position]

                holder.titleView.text = shortNotification.title

                if (shortNotification.contentText != null) {
                    holder.contentView.apply {
                        text = shortNotification.contentText
                        isVisible = true
                    }
                } else {
                    holder.contentView.apply {
                        text = ""
                        isVisible = false
                    }
                }

                val timeLine = TimeLine.getInstance(holder.itemView.context)

                holder.timeView.text = timeLine.formatTime(shortNotification.timePost)

                holder.containerLayout.setOnClickListener {
                    onNotificationClick.invoke(shortNotification)
                }
            }

        }

        private class ShortNotificationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val containerLayout = findView<LinearLayoutCompat>(
                R.id.item_short_notification_container_layout
            )

            val titleView = findView<AppCompatTextView>(
                R.id.item_short_notification_title_view
            )

            val contentView = findView<AppCompatTextView>(
                R.id.item_short_notification_content_view
            )

            val timeView = findView<AppCompatTextView>(
                R.id.item_short_notification_time_view
            )
        }
    }
}