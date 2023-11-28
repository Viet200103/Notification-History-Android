package com.notisaver.main.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.start.PermissionManager
import com.notisaver.misc.isNotificationAccessEnabled
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {

    private var accessWarningDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (NotisaveSetting.getInstance(this).isScreenCaptureEnabled.not()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        createWarningAcessDialog()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val isAccessed = isNotificationAccessEnabled(this@BaseActivity)
            if (isAccessed.not()) {
                try {
                    if (accessWarningDialog == null) {
                        createWarningAcessDialog()
                    }

                    if (accessWarningDialog?.isShowing != true) {
                        accessWarningDialog?.show()
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun createWarningAcessDialog() {
        accessWarningDialog = PermissionManager.createAccessNotificationRationaleDialog(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        accessWarningDialog = null
    }

}