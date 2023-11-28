package com.notisaver.main.log.fragments

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.notisaver.R
import com.notisaver.database.entities.NotificationLog
import com.notisaver.main.BUNDLE_KEY
import com.notisaver.main.log.adapters.NormalNotificationPagingAdapter
import com.notisaver.main.log.misc.SupportSelectionBarBinding
import com.notisaver.main.log.viewmodels.NotificationViewModel
import com.notisaver.main.utilities.SupportMaterialAlertDialog
import com.notisaver.misc.asNotisaveApplication
import com.notisaver.misc.collectWhenCreated
import com.notisaver.misc.findView
import com.notisaver.misc.getParcelableCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.parcelize.Parcelize

class NotificationListDialogFragment : AppCompatDialogFragment(R.layout.dialog_fragment_notification_list) {

    private val notificationViewModel by viewModels<NotificationViewModel> {
        NotificationViewModel.Factory(
            requireContext().asNotisaveApplication()
        )
    }

    private val notificationAdapter: NormalNotificationPagingAdapter by lazy {
        NormalNotificationPagingAdapter(
            appInfoManager,
            this::onNotificationLogClick
        )
    }

    private lateinit var dataBundle: DataBundle

    private lateinit var contentFlow: Flow<PagingData<NotificationLog>>

    private lateinit var selectionBarBinding: SupportSelectionBarBinding

    private val appInfoManager
        get() =  notificationViewModel.appInfoManager

    private val notisaveRepository
        get() = notificationViewModel.repository

    private lateinit var recyclerView: RecyclerView

    private lateinit var closeButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            arguments?.getParcelableCompat(BUNDLE_KEY, DataBundle::class.java)?.let {
                dataBundle = it
            } ?: throw NullPointerException()
        } catch (e: NullPointerException) {
            Toast.makeText(requireContext(), R.string.error_no_data_found, Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        contentFlow = notisaveRepository.searchNotificationPaging(
            dataBundle.sbnKeyHashcode,  dataBundle.searchQuery
        )

        collectWhenCreated(Dispatchers.IO) {
            contentFlow.collectLatest {
                notificationAdapter.submitData(it)
            }
        }

        notificationViewModel.listenProcessWaiting(
            requireContext(), this
        ) {
            notificationAdapter.clearSelectedItem()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = findView(R.id.dialog_fragment_notification_list_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }

        closeButton = findView(R.id.dialog_fragment_notification_list_close_button)
        closeButton.setOnClickListener {
            this.dismiss()
        }

        selectionBarBinding = SupportSelectionBarBinding.bind(
            findView(R.id.selection_bar_container_layout)
        )
        selectionBarBinding.setOnClickCloseListener {
            notificationAdapter.turnOffSelectionMode()
            closeButton.visibility = View.VISIBLE
            selectionBarBinding.setContainerVisibility(View.INVISIBLE)
        }
        selectionBarBinding.setOnClickDeleteListener {
            SupportMaterialAlertDialog.createWarningDeleteDialog(
                requireContext()
            ) {
                notificationViewModel.deleteNotifications(
                    notificationAdapter.getItemsSelected()
                )
            }.show()
        }

        notificationAdapter.selectedCountListener = {
            selectionBarBinding.setCount(it)
        }

        notificationAdapter.selectionModeOnListener = {
            closeButton.visibility = View.INVISIBLE
            selectionBarBinding.setContainerVisibility(View.VISIBLE)
        }

    }

    override fun getTheme(): Int {
        return R.style.Theme_Notisaver_FullScreenDialogFragment_DayNight
    }

    private fun onNotificationLogClick(notificationLog: NotificationLog) {
        NotificationInfoBottomSheet.createInstance(notificationLog.logId)
            .show(parentFragmentManager, NotificationInfoBottomSheet.TAG)
    }

    companion object {
        @JvmStatic
        fun createInstance(data: DataBundle): NotificationListDialogFragment {
            val bundle = Bundle().apply {
                putParcelable(BUNDLE_KEY, data)
            }
            return NotificationListDialogFragment().apply {
                arguments = bundle
            }
        }

        @JvmStatic
        fun createInstance(
            sbnKeyHashcode: String,
            packageHashcode: String,
            searchQuery: String
        ): NotificationListDialogFragment = createInstance(
            DataBundle(sbnKeyHashcode, packageHashcode, searchQuery)
        )
    }

    @Parcelize
    data class DataBundle(
        val sbnKeyHashcode: String,
        val packageHashcode: String,
        val searchQuery: String
    ): Parcelable
}