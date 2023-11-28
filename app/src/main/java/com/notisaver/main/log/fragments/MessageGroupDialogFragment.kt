package com.notisaver.main.log.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.notisaver.R
import com.notisaver.database.NotisaveDatabase
import com.notisaver.database.entities.AppInformation
import com.notisaver.database.entities.NotificationCategory
import com.notisaver.database.extra_relationships.CategoryAppMetaDataCrossRef
import com.notisaver.misc.asNotisaveApplication
import com.notisaver.misc.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageGroupDialogFragment : AppMultiChoiceDialogFragment() {

    private var messageCategory: NotificationCategory? = null
    private val messagePackageHashcodeSet: HashSet<String> = hashSetOf()

    private val repository by lazy {
        requireContext().asNotisaveApplication().notisaveRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch(Dispatchers.IO) {

            repository.loadCategory(NotisaveDatabase.MESSAGE_CATEGORY_ID).also { aggregation ->
                aggregation.appMetaDataList.onEach {
                    messagePackageHashcodeSet.add(it.packageHashcode)
                }
                messageCategory = aggregation.category
            }

        }.invokeOnCompletion {
            if (messageCategory == null) {
                Toast.makeText(requireContext(), R.string.please_try_again, Toast.LENGTH_SHORT).show()
                this.dismissNow()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = getString(R.string.add_to_group)
    }

    override fun onCheckedItemChange(packageHashcode: String, isChecked: Boolean) {
        selectedCount += if (isChecked) 1 else -1
        chooseAllCheckBox.isChecked = selectedCount == appChoiceAdapter.itemCount

        launch(Dispatchers.IO) {
            if (isChecked) {
                repository.addAppToCategory(
                    CategoryAppMetaDataCrossRef(
                        NotisaveDatabase.MESSAGE_CATEGORY_ID,
                        packageHashcode
                    )
                )
                messagePackageHashcodeSet.add(packageHashcode)
            } else {
                repository.removeAppFromCategory(
                    CategoryAppMetaDataCrossRef(
                        NotisaveDatabase.MESSAGE_CATEGORY_ID,
                        packageHashcode
                    )
                )
                messagePackageHashcodeSet.remove(packageHashcode)
            }
        }
    }

    override fun acquireCheckablePropertyApp(appInformation: AppInformation): Boolean {
        return messagePackageHashcodeSet.contains(appInformation.packageHashcode)
    }

    override fun onChangeAllChecked(isChecked: Boolean) {
        selectedCount = if (isChecked) {
            appChoiceAdapter.itemCount
        } else 0

        val relationshipList = ArrayList<CategoryAppMetaDataCrossRef>()
        val packageHashcodeList = hashSetOf<String>()

        appChoiceAdapter.currentList.onEach {
            it.isChecked = isChecked


            relationshipList.add(
                CategoryAppMetaDataCrossRef(
                    NotisaveDatabase.MESSAGE_CATEGORY_ID,
                    it.packageHashcode
                )
            )

            packageHashcodeList.add(it.packageHashcode)
        }

        launch {
            if (isChecked) {
                messagePackageHashcodeSet.addAll(packageHashcodeList)
                repository.addAppsToCategory(relationshipList)
            } else {
                messagePackageHashcodeSet.removeAll(packageHashcodeList)
                repository.removeAppsFromCategory(relationshipList)
            }

            withContext(Dispatchers.Main) {
                appChoiceAdapter.notifyItemRangeChanged(0, appChoiceAdapter.itemCount)
            }
        }
    }
}