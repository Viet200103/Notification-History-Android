package com.notisaver.main.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.notisaver.LanguageManager
import com.notisaver.R
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.NotisaveSetting.Companion.ACTION_SETUP_PASSCODE
import com.notisaver.main.NotisaveSetting.Companion.ACTIVITY_ACTION
import com.notisaver.main.NotisaveSetting.Companion.PRE_BATTERY_WHITELIST
import com.notisaver.main.NotisaveSetting.Companion.PRE_LANGUAGE
import com.notisaver.main.NotisaveSetting.Companion.PRE_LISTENER_STATUS
import com.notisaver.main.NotisaveSetting.Companion.PRE_NLOG_ONGOING
import com.notisaver.main.NotisaveSetting.Companion.PRE_NOTIFICATION_PERMISSION
import com.notisaver.main.RETURN_RESULT
import com.notisaver.main.protection.LocalPasswordManager
import com.notisaver.main.protection.activities.ConfirmPasswordActivity
import com.notisaver.main.protection.activities.PasswordActivity
import com.notisaver.main.start.PermissionManager
import com.notisaver.misc.*
import kotlinx.coroutines.launch


class SettingFragment : PreferenceFragmentCompat() {

    private lateinit var confirmPasswordLauncher: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var notificationRationaleDialog: AlertDialog? = null

    private var preListenerStatus: Preference? = null
    private var prefTotalSaved: Preference? = null
    private var preLanguage: Preference? = null
    private var preLogOngoing: Preference? = null
    private var prePasscode: SwitchPreferenceCompat? = null
    private var preFingerprint: SwitchPreferenceCompat? = null
    private var preNotificationPermission: Preference? = null
    private var preBatteryWhiteList: Preference? = null
    private var preKeepAlive: SwitchPreferenceCompat? = null;

    private var preListenerStateChange: OnSharedPreferenceChangeListener? = null

    private val notisaveApplication by lazy {
        requireContext().asNotisaveApplication()
    }

    private val localPasswordManager by lazy {
        LocalPasswordManager.getInstance(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        confirmPasswordLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (
                result.resultCode == Activity.RESULT_OK &&
                result.data?.getIntExtra(
                    NotisaveSetting.PASSWORD_CONFIRM, NotisaveSetting.CONFIRM_FAILED
                ) == NotisaveSetting.CONFIRM_SUCCESS
            ) {
                if (localPasswordManager.isCreatedPassword()) {
                    localPasswordManager.clear()
                    preFingerprint?.isChecked = false
                    prePasscode?.isChecked = false
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                val activity =  requireActivity()
                val isShould = PermissionManager.shouldShowPostNotificationRationale(activity)
                if (isGranted.not() && isShould.not()) {

                    if (notificationRationaleDialog == null) {
                        notificationRationaleDialog = PermissionManager.createPostNotificationRequireSettingDialog(
                            activity,
                            agreeAction = {
                                startActivity(
                                    createNotificationSettingIntent(requireContext().packageName)
                                )
                            }
                        ).create()
                    }

                    notificationRationaleDialog?.show()
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        setupListenerStatus()
        setupTotalSavedStatus()
        setupSettingOnGoing()
        setupSettingLanguage()
        setupBatteryOptimization()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setupNotificationPermission()
        }

        prePasscode = findPreference(NotisaveSetting.PRE_SETUP_PASSCODE)

        preFingerprint = findPreference(NotisaveSetting.PRE_ENABLE_FINGERPRINT)

        preKeepAlive = findPreference(NotisaveSetting.PRE_KEEP_ALIVE)

        preListenerStateChange = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                NotisaveSetting.PRE_SETUP_PASSCODE -> {
                    setupPasscode(sharedPreferences.getBoolean(key, false))
                }
                NotisaveSetting.PRE_ENABLE_FINGERPRINT -> {
                    if (sharedPreferences.getBoolean(key, false)) {
                        setupFingerprint()
                    }
                }
                NotisaveSetting.PRE_SCREEN_CAPTURE -> {
                    Toast.makeText(requireContext(), R.string.retart_app, Toast.LENGTH_SHORT).show()
                }
                NotisaveSetting.PRE_KEEP_ALIVE -> {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                        when(PermissionManager.isPostNotificationGranted(requireContext())) {
                            true -> {
                                doKeepAppLive()
                            }
                            false -> {
                                if (preKeepAlive?.isChecked == true) {
                                    PermissionManager.requestPostNotificationPermission(
                                        requireActivity(), requestPermissionLauncher
                                    )
                                    preKeepAlive?.isChecked = false
                                }
                            }
                        }

                    } else {
                        doKeepAppLive()
                    }
                }
            }
        }

        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(
            preListenerStateChange
        )

        findPreference<Preference>(NotisaveSetting.PRE_OTHER_TRANSLATE)?.intent?.apply {
            data = Uri.parse(getString(R.string.support_translate_link))
        }
    }

    private fun setupListenerStatus() {
        preListenerStatus = findPreference(PRE_LISTENER_STATUS)
        preListenerStatus?.setOnPreferenceClickListener {
            try {
                startActivity(
                    createAccessNotificationIntent(requireContext())
                )
            } catch (e: Exception) {
                startActivity(
                    createNormalAccessNotificationIntent()
                )
            }
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupNotificationPermission() {
        preNotificationPermission = findPreference(PRE_NOTIFICATION_PERMISSION)
        preNotificationPermission?.setOnPreferenceClickListener {
            val context = requireContext()
            val isGranted = PermissionManager.isPostNotificationGranted(context)

            if (isGranted) {
                startLauncherIntent(
                    context, createNotificationSettingIntent(context.packageName)
                )
            } else {
                PermissionManager.requestPostNotificationPermission(requireActivity(), requestPermissionLauncher)
            }
            true
        }
    }

    private fun setupBatteryOptimization() {
        preBatteryWhiteList = findPreference(PRE_BATTERY_WHITELIST)
        preBatteryWhiteList?.setOnPreferenceClickListener {
            openTutorialBattery()
            true
        }
    }
    private fun openTutorialBattery() {
        val batteryFragment = TutorialBatteryFragment()
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction
            .add(android.R.id.content, batteryFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun doKeepAppLive() {
        val context = requireContext()
        NotisaveSetting.getInstance(
            context
        ).doKeepAppLive(context)
    }

    private fun setupTotalSavedStatus() {
        prefTotalSaved = findPreference(NotisaveSetting.PRE_TOTAL_SAVED)
        lifecycleScope.launch {
            val count = notisaveApplication.notisaveRepository.getCountNotification()
            val summaryText = notisaveApplication.resources.getQuantityString(
                R.plurals.count_notification, count, count
            )
            prefTotalSaved?.summary = summaryText
        }
    }
    private fun setupSettingOnGoing() {
        preLogOngoing = findPreference(PRE_NLOG_ONGOING)
        preLogOngoing?.setOnPreferenceClickListener {
            OnGoingDialogFragment().show(
                parentFragmentManager, OnGoingDialogFragment::class.java.name
            )
            true
        }
    }

    private fun setupPasscode(isTurnOn: Boolean) {
        if (isTurnOn) {
            if (!localPasswordManager.isCreatedPassword()) {
                val intent =
                    Intent(requireContext(), PasswordActivity::class.java).apply {
                        putExtra(ACTIVITY_ACTION, ACTION_SETUP_PASSCODE)
                    }
                startActivity(intent)
            }
        } else {
            if (localPasswordManager.isCreatedPassword()) {

                val intent = Intent(
                    requireContext(), ConfirmPasswordActivity::class.java
                )
                intent.putExtra(RETURN_RESULT, true)

                confirmPasswordLauncher.launch(intent)
            }
        }
    }

    private fun setupFingerprint() {

        val context = requireContext()

        val biometricManager = BiometricManager.from(context)

        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
            Toast.makeText(context, R.string.device_not_support_fingerprint, Toast.LENGTH_SHORT).show()
            preFingerprint?.isChecked = false
            return
        }

        if (!localPasswordManager.isCreatedPassword()) {
            Toast.makeText(context, R.string.let_setup_your_password, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setupSettingLanguage() {
        val languageManager = LanguageManager.getInstance(
            requireContext().asNotisaveApplication()
        )

        var currentLangCode = languageManager.getCurrentLanguageCode()

        preLanguage = findPreference(PRE_LANGUAGE)
        preLanguage?.summary = getString(
            languageManager.getCurrentLanguageName(currentLangCode) ?: R.string.default_language_system
        )

        val langCodeList = languageManager.languageCodes

        var currentIndex = langCodeList.indexOf(languageManager.getCurrentLanguageCode())
        var selectedIndex = currentIndex

        val languageNames = languageManager.languageNames

        preLanguage?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext(), R.style.Theme_Notisaver_AlertDialog_DayNight)
                .setTitle(R.string.pre_general_language)
                .setIcon(R.drawable.ic_language)
                .setSingleChoiceItems(
                    languageNames.toTypedArray(), currentIndex
                ) { _, which ->
                    selectedIndex = which
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setPositiveButton(R.string.ok) { _, _ ->
                    currentIndex = selectedIndex
                    currentLangCode = langCodeList[currentIndex]
                    languageManager.changeLanguageInApp(currentLangCode)
                }.show()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            if (isNotificationAccessEnabled(requireContext())) {
                preListenerStatus?.setSummary(R.string.pre_listener_status_enabled)
            } else {
                preListenerStatus?.setSummary(R.string.pre_listener_status_disabled)
            }
        }

        lifecycleScope.launch {
            val context = requireContext()
            val isNotOptimize = NotisaveSetting.isNotOptimizeBatterEnabled(context)

            if (isNotOptimize) {
                preBatteryWhiteList?.setSummary(R.string.pre_listener_status_enabled)
            } else {
                preBatteryWhiteList?.setSummary(R.string.pre_listener_status_disabled)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (PermissionManager.isPostNotificationGranted(requireContext())) {
                    preNotificationPermission?.setSummary(R.string.pre_listener_status_enabled)
                } else {
                    preNotificationPermission?.setSummary(R.string.pre_listener_status_disabled)
                    preKeepAlive?.isChecked = false
                }
            }
        }

        prePasscode?.isChecked = LocalPasswordManager.getInstance(requireContext()).isCreatedPassword()
    }

    override fun onDestroy() {
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
            preListenerStateChange
        )
        super.onDestroy()
    }
}