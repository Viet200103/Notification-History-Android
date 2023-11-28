package com.notisaver.main.settings

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.notisaver.R
import com.notisaver.misc.createDetailsSettingIntent
import com.notisaver.misc.findView
import com.notisaver.misc.startLauncherIntent
import kotlinx.coroutines.launch


class TutorialBatteryFragment : AppCompatDialogFragment(R.layout.layout_battery_optimization) {

    private var backButton: AppCompatImageView? = null
    private var detailButton: MaterialButton? = null
    private var whitelistButton: MaterialButton? = null

    private var descriptionView: AppCompatTextView? = null

    private var statusView: AppCompatTextView? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton = findView(R.id.layout_battery_optimization_back_button)
        backButton?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        statusView = findView(R.id.layout_battery_optimization_status)

        descriptionView = findView(R.id.layout_battery_optimization_description_view)
        descriptionView?.let {
            val nameApp = it.context.getString(R.string.app_name)
            val description = it.context.getString(R.string.battery_optimization_explain, nameApp, nameApp)
            it.text = description
        }

        detailButton = findView(R.id.layout_battery_optimization_detail_setting)
        detailButton?.setOnClickListener {

            firebaseAnalytics.logEvent("onBatteryDetailSetting", Bundle())

            val context = it.context
            startLauncherIntent(
                context, createDetailsSettingIntent(context.packageName)
            )
        }

        whitelistButton = findView(R.id.layout_battery_optimization_battery_white_list)
        whitelistButton?.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            startLauncherIntent(
                it.context, intent
            )

            firebaseAnalytics.logEvent("onBatteryWhitelistClick", Bundle())
        }

        val assetManager = requireContext().assets
        val folderName = getString(R.string.battery_optimization_folder_name)

        lifecycleScope.launch {
            val imageStream = assetManager.open("$folderName/choose_battery_usage.jpg")
            val bitmap = BitmapFactory.decodeStream(imageStream)
            findView<AppCompatImageView>(R.id.layout_battery_optimization_image_battery_usage).setImageBitmap(bitmap)
        }

        lifecycleScope.launch {
            val imageStream = assetManager.open("$folderName/choose_unrestricted_mode.jpg")
            val bitmap = BitmapFactory.decodeStream(imageStream)

            findView<AppCompatImageView>(
                R.id.layout_battery_optimization_image_battery_usage_unrestricted_mode
            ).setImageBitmap(bitmap)
        }

        lifecycleScope.launch {
            val imageStream = assetManager.open("$folderName/choose_not_optimized.jpg")
            val bitmap = BitmapFactory.decodeStream(imageStream)

            findView<AppCompatImageView>(
                R.id.layout_battery_optimization_image_not_optimized
            ).setImageBitmap(bitmap)
        }

        lifecycleScope.launch {
            val imageStream = assetManager.open("$folderName/choose_all_app.jpg")
            val bitmap = BitmapFactory.decodeStream(imageStream)

            findView<AppCompatImageView>(
                R.id.layout_battery_optimization_image_all_list
            ).setImageBitmap(bitmap)
        }

        lifecycleScope.launch {
            val imageStream = assetManager.open("$folderName/find_notisaver.jpg")
            val bitmap = BitmapFactory.decodeStream(imageStream)

            findView<AppCompatImageView>(
                R.id.layout_battery_optimization_image_find_notisaver
            ).setImageBitmap(bitmap)
        }

        lifecycleScope.launch {
            val imageStream = assetManager.open("$folderName/choose_do_not_optimize.jpg")
            val bitmap = BitmapFactory.decodeStream(imageStream)

            findView<AppCompatImageView>(
                R.id.layout_battery_optimization_image_dialog_unrestricted_mode
            ).setImageBitmap(bitmap)
        }
    }

    override fun onResume() {
        super.onResume()

        val context = requireContext()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as (PowerManager)

        if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            statusView?.setText(R.string.not_battery_optimization_status_enabled)
        } else {
            statusView?.setText(R.string.not_battery_optimization_status_disabled)
        }
    }
}