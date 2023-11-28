package com.notisaver.main.log.fragments

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.checkbox.MaterialCheckBox
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.database.entities.NotificationLog
import com.notisaver.main.KEY_NOTIFICATION_LOG_ID
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.log.fragments.NotificationInfoBottomSheet.OptionType.Exclude
import com.notisaver.main.log.fragments.NotificationInfoBottomSheet.OptionType.Include
import com.notisaver.main.log.fragments.NotificationInfoBottomSheet.OptionType.Open
import com.notisaver.main.log.fragments.NotificationInfoBottomSheet.OptionType.Remove
import com.notisaver.main.log.fragments.NotificationInfoBottomSheet.OptionType.Setting
import com.notisaver.main.log.misc.NotificationInfoItemBinding
import com.notisaver.main.manager.AppInfoManager
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.asNotisaveApplication
import com.notisaver.misc.calculateNoOfColumns
import com.notisaver.misc.collectWhenStarted
import com.notisaver.misc.createNotificationSettingIntent
import com.notisaver.misc.findView
import com.notisaver.misc.launch
import com.notisaver.misc.requireAppNotNull
import com.notisaver.misc.startLauncherIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

class NotificationInfoBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "NotificationBottomSheet"

        @JvmStatic
        internal fun createInstance(
            notificationLogId: String
        ): NotificationInfoBottomSheet {
            val bundle = Bundle().apply {
                putString(KEY_NOTIFICATION_LOG_ID, notificationLogId)
            }
            return NotificationInfoBottomSheet().apply {
                arguments = bundle
            }
        }
    }

    private lateinit var notisaveApplication: NotisaveApplication

    private val appInfoManager
        get() = AppInfoManager.getInstance(notisaveApplication)

    private val notisaveRepository get() = notisaveApplication.notisaveRepository

    private var loadingFlow = MutableStateFlow(false)

    private var appInfo: AppInformation? = null
    private lateinit var notificationLog: NotificationLog

    private lateinit var binding: NotificationInfoItemBinding

    private lateinit var recyclerView: RecyclerView

    private lateinit var cleanNotificationCheckBox: MaterialCheckBox
    private lateinit var logOngoingCheckbox: MaterialCheckBox

    private lateinit var settingContainer: LinearLayoutCompat

    private val optionAdapter by lazy {
        OptionAdapter(this::onOptionClickListener)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = BottomSheetDialog(requireContext(), theme)
        launch {
            bottomSheet.withStarted {
                bottomSheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    .let {
                        BottomSheetBehavior.from(it!!).state = BottomSheetBehavior.STATE_EXPANDED
                    }
            }
        }
        return bottomSheet
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notisaveApplication = requireContext().asNotisaveApplication()

        setStyle(
            DialogFragment.STYLE_NORMAL, R.style.Theme_Notisaver_NInfoBottomSheet_DayNight
        )

        lifecycleScope.launch(Dispatchers.IO) {
            loadingFlow.value = false
            try {
                val nLogId = arguments?.getString(KEY_NOTIFICATION_LOG_ID)
                    ?: throw NullPointerException("LogId must not be null")
                val tempLog = notisaveRepository.getNotificationLog(nLogId)
                    ?: throw NullPointerException("NotificationLog must not be null")

                appInfo = appInfoManager.getAppInformationSafety(tempLog.packageHashcode)
                notificationLog = tempLog

            } catch (e: NullPointerException) {
                return@launch cancel(
                    CancellationException(e.message)
                )
            }
            loadingFlow.value = true
        }.invokeOnCompletion {
            if (it != null) {
                Toast.makeText(requireContext(), R.string.error_no_data_found, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        collectWhenStarted(Dispatchers.Main) {
            loadingFlow.collect { isLoaded ->
                if (isLoaded) {
                    launch {
                        bindAppInfo()
                        bindNotificationInfo()
                        bindSettingForApp()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_notification_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = NotificationInfoItemBinding.bind(view)
        recyclerView = findView(R.id.bottom_sheet_notification_info_list_option)

        settingContainer = findView(R.id.bottom_sheet_notification_setting_container)
        settingContainer.isVisible = appInfo != null

        cleanNotificationCheckBox = findView(R.id.bottom_sheet_notification_info_clean_notification)
        logOngoingCheckbox = findView(R.id.bottom_sheet_notification_info_log_ongoing)
    }

    private fun bindNotificationInfo() {

        val oNotification = notificationLog.notification
        val notificationStyle = notificationLog.notificationStyle

        binding.setContentTitle(
            notificationStyle?.baseStyle?.bigContentTitle ?: oNotification.title
        )

        binding.bindNotificationLog(notificationLog)
    }

    private fun bindAppInfo() {
        binding.bindAppInfo(appInfo)
    }

    private fun bindSettingForApp() {
        bindOptions()
        cleanNotificationCheckBox.isChecked = appInfo?.isCleanable == true
        cleanNotificationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                SupportMaterialAlertDialog.createCancelWhenPushDialog(requireContext()) {
                    requireAppNotNull(appInfo) {
                        appInfoManager.enableCleanable(it)
                    }
                }.setOnCancelListener {
                    cleanNotificationCheckBox.isChecked = false
                }.show()
            } else {
                requireAppNotNull(appInfo) {
                    appInfoManager.disableCleanable(it)
                }
            }
        }

        logOngoingCheckbox.isChecked = appInfo?.isLogOnGoing == true
        logOngoingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requireAppNotNull(appInfo) {
                    SupportMaterialAlertDialog.createWarningLogOngoingDialog(
                        requireContext(), it.name ?: it.packageName
                    ) {
                        appInfoManager.enableLogOngoing(it)
                    }.setOnCancelListener {
                        logOngoingCheckbox.isChecked = false
                    }.show()
                }
            } else {
                requireAppNotNull(appInfo) {
                    appInfoManager.disableLogOngoing(it)
                }
            }
        }
    }

    private fun onOptionClickListener(position: Int, optionItem: OptionItem) {
        requireAppNotNull(appInfo) {
            when (optionItem.optionType) {
                Open -> {
                    startLauncherIntent(
                        requireContext(),
                        appInfoManager.getLauncherIntent(it.packageName)
                    )
                }

                Exclude -> {
                    if (it.isTracking)
                        SupportMaterialAlertDialog.createWarningExcludeDialog(
                            requireContext(),
                            appInfo?.name ?: it.packageName
                        ) {
                            appInfoManager.excludeApp(it)
                            optionAdapter.optionList[position] = createOptionItem(Include)
                            optionAdapter.notifyItemChanged(position)
                        }.show()
                }

                Include -> {
                    if (!it.isTracking) {
                        appInfoManager.includeApp(it)
                        optionAdapter.optionList[position] = createOptionItem(Exclude)
                        optionAdapter.notifyItemChanged(position)
                    }
                }

                Setting -> {
                    try {
                        startActivity(createNotificationSettingIntent(it.packageName))
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            R.string.package_not_found_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                Remove -> SupportMaterialAlertDialog.createWarningDeleteDialog(requireContext()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val result = notisaveRepository.deleteNotification(
                            notificationLog.notification
                        )
                        if (result) {
                            this@NotificationInfoBottomSheet.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.delete_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.show()
            }
        }
    }

    private fun bindOptions() {
        if (appInfo == null) {
            recyclerView.isVisible = false
            return
        }

        optionAdapter.optionList.clear()

        if (appInfo?.isInstalled == true) {
            optionAdapter.add(createOptionItem(Open))
        }

        optionAdapter.add(
            if (appInfo?.isTracking == true) {
                createOptionItem(Exclude)
            } else {
                createOptionItem(Include)
            }
        )

        if (appInfo?.isInstalled == true) {
            optionAdapter.add(createOptionItem(Setting))
        }

        optionAdapter.add(createOptionItem(Remove))

        val columnsOption = calculateNoOfColumns(
            requireContext(), 104f
        )

        recyclerView.apply {
            layoutManager = GridLayoutManager(context, columnsOption)
            adapter = optionAdapter
        }
    }

    private fun getDrawable(@DrawableRes drawableId: Int) = AppCompatResources.getDrawable(
        requireContext(),
        drawableId
    )

    private fun createOptionItem(optionId: OptionType): OptionItem {
        return when (optionId) {
            Open -> OptionItem(
                Open,
                R.string.option_open_app_short,
                getDrawable(R.drawable.ic_options_open_in_new)
            )

            Exclude -> OptionItem(
                Exclude,
                R.string.option_exclude_app_short,
                getDrawable(R.drawable.ic_option_blur_off)
            )

            Include -> OptionItem(
                Include,
                R.string.option_include_app_short,
                getDrawable(R.drawable.ic_option_blur_on)
            )

            Setting -> OptionItem(
                Setting,
                R.string.option_open_setting_short,
                getDrawable(R.drawable.ic_options_settings)
            )

            Remove -> OptionItem(
                Remove,
                R.string.option_remove_notification_short,
                getDrawable(R.drawable.ic_option_delete)
            )
        }
    }

    private fun requireAppNotNull(appInfo: AppInformation?, action: (AppInformation) -> Unit) {
        requireContext().requireAppNotNull(appInfo, action)
    }

    private data class OptionItem(
        val optionType: OptionType,
        @StringRes var title: Int,
        var icon: Drawable? = null,
    )

    private enum class OptionType {
        Open, Exclude, Include, Setting, Remove
    }

    private class OptionAdapter(
        private val optionChooseObserver: (Int, OptionItem) -> Unit
    ) : RecyclerView.Adapter<OptionViewHolder>() {
        var optionList = mutableListOf<OptionItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
            return OptionViewHolder(parent)
        }

        override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
            val item = optionList[position]
            with(holder) {
                iconView.setImageDrawable(item.icon)
                titleView.setText(item.title)
                containerLayout.setOnClickListener {
                    optionChooseObserver.invoke(position, item)
                }
            }
        }

        override fun getItemCount(): Int = optionList.size

        fun add(optionItem: OptionItem) {
            optionList.add(optionItem)
        }
    }

    private class OptionViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option_notification, parent, false)
    ) {
        val containerLayout =
            findView<LinearLayoutCompat>(R.id.item_option_notification_container_layout)
        val iconView = findView<AppCompatImageView>(R.id.item_option_notification_icon)
        val titleView = findView<AppCompatTextView>(R.id.item_option_notification_title)
    }
}