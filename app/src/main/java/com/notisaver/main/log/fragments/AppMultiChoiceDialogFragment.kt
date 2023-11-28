package com.notisaver.main.log.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.main.NotisaveSetting
import com.notisaver.misc.asNotisaveApplication
import com.notisaver.misc.collectWhenCreated
import com.notisaver.misc.collectWhenStarted
import com.notisaver.misc.findView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AppMultiChoiceDialogFragment :
    AppCompatDialogFragment(R.layout.dialog_fragment_app_multi_choice) {

    protected val appInfoManager by lazy {
        requireContext().asNotisaveApplication().getAppInfoManager()
    }

    private lateinit var scanningLayout: LinearLayoutCompat

    private lateinit var recyclerView: RecyclerView
    protected lateinit var chooseAllCheckBox: MaterialCheckBox
    protected lateinit var descriptionView: AppCompatTextView

    protected lateinit var toolbar: MaterialToolbar

    protected var selectedCount: Int = 0

    protected val appChoiceAdapter by lazy {
        AppChoiceAdapter(this::onCheckedItemChange)
    }

    private val contentLoadingFlow = MutableStateFlow(false)
    private var contentLoadingJob: Job? = null

    private var searchStateFlow = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        invokeRescan()

        collectWhenCreated {
            searchStateFlow.collectLatest {
                loadData()
            }
        }

        collectWhenStarted {
            contentLoadingFlow.collectLatest {
                scanningLayout.isInvisible = !it
                recyclerView.isInvisible = it
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = findView(R.id.dialog_fragment_app_multi_choice_toolbar)

        (toolbar.menu.findItem(R.id.menu_notification_manager_search).actionView as SearchView).apply {
            setOnQueryTextListener(queryListener)
        }

        toolbar.setNavigationOnClickListener {
            this.dismiss()
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_notification_manager_rescan -> {
                    invokeRescan()
                }
            }
            true
        }

        descriptionView = findView(R.id.dialog_fragment_multi_choice_description)

        scanningLayout = findView(R.id.dialog_fragment_app_multi_choice_loading_layout)

        recyclerView = findView(R.id.dialog_fragment_app_switch_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = appChoiceAdapter
        }

        chooseAllCheckBox = findView(R.id.dialog_fragment_app_multi_choice_check_all)

        chooseAllCheckBox.setOnClickListener {
            onChangeAllChecked(chooseAllCheckBox.isChecked)
        }
    }

    override fun getTheme(): Int {
        return R.style.Theme_Notisaver_FullScreenDialogFragment
    }

    private val queryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            searchStateFlow.value = newText?.trim()?.lowercase().orEmpty()
            return true
        }

    }

    private fun loadData() {
        contentLoadingJob?.cancel()
        contentLoadingJob = lifecycleScope.launch(Dispatchers.IO) {
            contentLoadingFlow.update { true }
            while (true) {
                if (!isActive) return@launch
                if (appInfoManager.appScanFlow.value) break
            }

            val list = appInfoManager.filterAppInfoInCache(searchStateFlow.value).map {
                CheckableApp(it, acquireCheckablePropertyApp(it))
            }.sortedByDescending {
                it.isChecked
            }

            withContext(Dispatchers.Main) {
                appChoiceAdapter.submitList(list)
            }
        }

        contentLoadingJob?.invokeOnCompletion {
            contentLoadingFlow.update { false }
        }
    }

    abstract fun onCheckedItemChange(packageHashcode: String, isChecked: Boolean)

    abstract fun acquireCheckablePropertyApp(appInformation: AppInformation): Boolean

    abstract fun onChangeAllChecked(isChecked: Boolean)

    private fun invokeRescan() {
        appInfoManager.scanAppOnDevice(
            NotisaveSetting.getInstance(requireContext())
        )
        loadData()
    }

    protected class AppChoiceAdapter(
        private val onChangeState: (String, Boolean) -> Unit
    ) : ListAdapter<CheckableApp, SwitchHolder>(
        object : DiffUtil.ItemCallback<CheckableApp>() {
            override fun areItemsTheSame(oldItem: CheckableApp, newItem: CheckableApp): Boolean {
                return oldItem.packageHashcode == newItem.packageHashcode
            }

            override fun areContentsTheSame(oldItem: CheckableApp, newItem: CheckableApp): Boolean {
                return oldItem == newItem
            }

        }
    ) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): SwitchHolder {
            return SwitchHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_selected_app, parent, false)
            )
        }

        override fun onBindViewHolder(holder: SwitchHolder, position: Int) {
            val template = getItem(position)

            holder.ongoingCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != template.isChecked) {
                    template.isChecked = isChecked
                    onChangeState.invoke(template.packageHashcode, isChecked)
                }
            }

            holder.ongoingCheckBox.isChecked = template.isChecked
            holder.appNameView.text = template.title

            holder.appIconView.setImageResource(R.drawable.ic_extension)

            template.appInformation.icon?.let {
                holder.appIconView.setImageDrawable(it)
            }

            holder.containerLayout.setOnClickListener {
                val isChecked = !holder.ongoingCheckBox.isChecked
                holder.ongoingCheckBox.isChecked = isChecked
            }
        }
    }

    protected class SwitchHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val containerLayout = findView<LinearLayoutCompat>(R.id.item_app_layout_container)
        val ongoingCheckBox = findView<MaterialCheckBox>(R.id.item_app_ongoing_check_box)
        val appNameView = findView<AppCompatTextView>(R.id.item_app_ongoing_name_view)
        val appIconView = findView<ShapeableImageView>(R.id.item_app_ongoing_icon_view)
    }

    data class CheckableApp(
        val appInformation: AppInformation,
        var isChecked: Boolean
    ) {
        val packageHashcode
            get() = appInformation.packageHashcode

        val title
            get() = appInformation.name ?: appInformation.packageName
    }
}