package com.notisaver.main.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.notisaver.R
import com.notisaver.database.entities.AppInformation
import com.notisaver.main.log.fragments.AppMultiChoiceDialogFragment
import com.notisaver.misc.launch
import kotlinx.coroutines.Dispatchers

class OnGoingDialogFragment : AppMultiChoiceDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.pre_log_ongoing)

        descriptionView.isVisible = true
        descriptionView.text = getText(R.string.pre_log_ongoing_summary)
    }

    override fun onChangeAllChecked(isChecked: Boolean) {
        selectedCount = if (isChecked) {
            appChoiceAdapter.itemCount
        } else 0

        val arrayList = ArrayList<AppInformation>()
        appChoiceAdapter.currentList.onEach {
            it.isChecked = isChecked
            appInfoManager.getAppInformationLazyCache(it.packageHashcode)?.let { app ->
                arrayList.add(app)
            }
        }

        appInfoManager.changeLogOngoingStateAllApps(arrayList, isChecked).invokeOnCompletion {
            launch(Dispatchers.Main) {
                appChoiceAdapter.notifyItemRangeChanged(0, appChoiceAdapter.itemCount)
            }
        }
    }

    override fun onCheckedItemChange(packageHashcode: String, isChecked: Boolean) {
        selectedCount += if (isChecked) 1 else -1
        chooseAllCheckBox.isChecked = selectedCount == appChoiceAdapter.itemCount
        appInfoManager.changeLogOngoingStateOfApp(packageHashcode, isChecked)
    }

    override fun acquireCheckablePropertyApp(appInformation: AppInformation): Boolean {
        return appInformation.isLogOnGoing
    }
}