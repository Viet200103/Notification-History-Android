package com.notisaver.main.manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notisaver.R
import com.notisaver.database.AppTemplate
import com.notisaver.misc.changeVisibleView
import com.notisaver.misc.findView

class AppAdapter(
    private val optionAppClickListener: OnOptionAppClickListener,
    private val appInfoManager: AppInfoManager,
) : ListAdapter<AppTemplate, AppAdapter.AppViewHolder>(getDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appTemplate = getItem(position)

        val appInfo = appInfoManager.getAppInformationLazyCache(appTemplate.aPackageHashcode)

        holder.iconView.setImageResource(R.drawable.ic_extension)
        holder.appTitleView.setText(R.string.unknown_app)

        changeVisibleView(holder.tokenProblemView, true)
        changeVisibleView(holder.tokenExcludeView, false)
        changeVisibleView(holder.tokenCleanableView, false)


        if (appInfo != null) {
            with(appInfo) {
                holder.appTitleView.text = packageName

                changeVisibleView(holder.tokenExcludeView, !isTracking)
                changeVisibleView(holder.tokenCleanableView, isCleanable)
            }

            holder.tokenProblemView.isVisible = appInfo.isInstalled != true

            appInfo.let {
                if (it.icon != null) holder.iconView.setImageDrawable(it.icon)
                if (it.name != null) holder.appTitleView.text = it.name
            }

        }

        holder.setCountTemplate(appTemplate)

        with(holder) {
            containerLayout.setOnClickListener {
                optionAppClickListener.onAppClick(appTemplate.aPackageHashcode)
            }

            optionsView.setOnClickListener {
                appInfo?.let {appInfo ->
                    supportMenu.updateOption(appInfo.appMetaData)
                    supportMenu.show()
                } ?: kotlin.run {
                    Toast.makeText(it.context, R.string.error_app_unknown, Toast.LENGTH_SHORT).show()
                }
            }

            supportMenu.setOnItemClickListener { menuItem ->
                optionAppClickListener.onOptionSelected(appTemplate.aPackageHashcode, menuItem)
                true
            }
        }
    }

    class AppViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        internal val supportMenu by lazy {
            AppSupportMenu(itemView.context, optionsView)
        }

        internal val containerLayout = findView<LinearLayoutCompat>(R.id.item_app_layout_container)
        internal val optionsView = findView<AppCompatImageView>(R.id.item_app_more_options_view)

        internal val iconView = findView<AppCompatImageView>(R.id.item_app_icon_view)
        internal val appTitleView = findView<AppCompatTextView>(R.id.item_app_name_view)
        private val countNotificationView = findView<AppCompatTextView>(R.id.item_app_count_notification_view)
        internal val tokenProblemView = findView<AppCompatImageView>(R.id.activity_detail_notification_app_token_problem)
        internal val tokenCleanableView = findView<AppCompatImageView>(R.id.item_app_count_notification_token_cleanable_view)
        internal val tokenExcludeView =findView<AppCompatImageView>(R.id.item_app_count_notification_token_exclude_view)

        internal fun setCountTemplate(appTemplate: AppTemplate) {
            countNotificationView.text = itemView.resources.getQuantityString(
                R.plurals.number_notification,
                appTemplate.nCount,
                appTemplate.nCount
            )
        }
    }

    companion object {

        @JvmStatic
        fun getDiffUtil() =  object : DiffUtil.ItemCallback<AppTemplate>() {
            override fun areItemsTheSame(oldItem: AppTemplate, newItem: AppTemplate): Boolean {
                return  oldItem.aPackageHashcode == newItem.aPackageHashcode
            }

            override fun areContentsTheSame(oldItem: AppTemplate, newItem: AppTemplate): Boolean {
                return oldItem == newItem
            }
        }
    }
}