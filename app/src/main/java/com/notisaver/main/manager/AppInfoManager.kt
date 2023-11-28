package com.notisaver.main.manager

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.notisaver.database.entities.AppMetaData
import com.notisaver.database.NotisaveRepository
import com.notisaver.database.entities.AppInformation
import com.notisaver.main.NotisaveApplication
import com.notisaver.main.NotisaveSetting
import com.notisaver.main.manager.AppInfoManager.AppState.*
import com.notisaver.misc.createPackageHashcode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class AppInfoManager private constructor(
    private val packageManager: PackageManager,
    private val notisaveRepository: NotisaveRepository,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    companion object {

        @Volatile
        private var INSTANCE: AppInfoManager? = null

        internal fun getInstance(
            notisave: NotisaveApplication,
            workerDispatcher: CoroutineDispatcher = Dispatchers.IO
        ): AppInfoManager {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = AppInfoManager(
                        notisave.packageManager,
                        notisave.notisaveRepository,
                        workerDispatcher
                    )
                }
            }
            return INSTANCE!!
        }
    }

    private val applicationInfoFlags = getApplicationInfoFlags()

    private val appInfoCache = ConcurrentHashMap<CharSequence, AppInformation>()
    private val appInfoMutex = Mutex()

    private val workerScope: CoroutineScope = CoroutineScope(SupervisorJob() + workerDispatcher)

    internal val appScanFlow by lazy {
        MutableStateFlow(false)
    }

    private val _appStateFlow = MutableSharedFlow<AppState>()
    internal val appStateFlow = _appStateFlow.asSharedFlow()

    internal fun scanAppOnDevice(notisaveSetting: NotisaveSetting) = workerScope.launch {
        appScanFlow.value = false

        val appMap = notisaveRepository.loadAppMetaData().associateByTo(HashMap()) {
            it.packageHashcode
        }

        if (!notisaveSetting.isAppOnDeviceLoaded) {
            val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(
                    ApplicationInfoFlags.of(
                        PackageManager.MATCH_UNINSTALLED_PACKAGES.toLong()
                    )
                )
            } else {
                @Suppress("Deprecation")
                packageManager.getInstalledApplications(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PackageManager.MATCH_UNINSTALLED_PACKAGES
                    } else {
                        PackageManager.GET_UNINSTALLED_PACKAGES
                    }
                )
            }

            installedApps.onEach { appInfo ->
                ensureActive()
                kotlin.runCatching {
                    val packageName = appInfo.packageName
                    val packageHashcode = createPackageHashcode(packageName)

                    if (!appMap.containsKey(packageHashcode)) {
                        if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {
                            val appMetaData = AppMetaData(
                                packageHashcode = packageHashcode,
                                packageName = packageName
                            )
                            appMap[packageHashcode] = appMetaData
                            notisaveRepository.logAppMetaData(appMetaData)
                        }
                    }
                }
            }

            notisaveSetting.setScannedAppDevice(true)
        }


        appMap.forEach { entry ->
            val appMeta = entry.value
            if (!appInfoCache.contains(appMeta.packageHashcode)) {
                bindToCacheAppInfo(appMeta)
            }
        }

        appScanFlow.value = true
    }

    internal suspend fun filterPackageHashcodeAppInfo(searchString: String) =
        withContext(workerDispatcher) {
            val packageHashcodeSet = hashSetOf<CharSequence>()
            appInfoMutex.withLock {
                for (entry in appInfoCache) {
                    if (!isActive) break
                    val value = entry.value
                    val name = value.name?.toString()?.lowercase()
                    val packageName = value.packageName.lowercase()
                    if (name?.contains(searchString) ?: packageName.contains(searchString)) {
                        packageHashcodeSet.add(value.packageHashcode)
                    }
                }
            }
            return@withContext packageHashcodeSet
        }

    internal suspend fun filterAppInfoInCache(searchString: String) =
        withContext(workerDispatcher) {
            val arrayList = ArrayList<AppInformation>()

            appInfoMutex.withLock {
                val query = searchString.lowercase()
                for (entry in appInfoCache) {
                    if (!isActive) break

                    val appInfo: AppInformation = entry.value
                    val name = appInfo.name?.toString()?.lowercase()
                    val packageName = appInfo.packageName.lowercase()
                    if (name?.contains(query) ?: packageName.contains(query)) {
                        arrayList.add(appInfo)
                    }
                }
            }

            return@withContext arrayList
        }

    internal suspend fun logAppMetaData(app: AppMetaData) {
        notisaveRepository.logAppMetaData(app)
    }

    internal fun getAppInformationLazyCache(packageHashcode: String): AppInformation? {
        val info = appInfoCache[packageHashcode]

        if (info == null) {
            workerScope.launch {
                bindToCacheAppInfo(packageHashcode)
            }
        }

        return info
    }

    internal suspend fun getAppInformationSafety(packageHashcode: String): AppInformation? {
        var info: AppInformation? = appInfoCache[packageHashcode]

        if (info == null) {
            notisaveRepository.getAppMetaData(packageHashcode)?.let { appMetaData ->
                info = AppInformation(appMetaData)
                fetchExtrasAppInfoFromDevice(info!!)
                appInfoCache[appMetaData.packageHashcode] = info!!
            }
        }

        return info
    }

    internal suspend fun bindToCacheAppInfo(packageHashcode: String) {
        notisaveRepository.getAppMetaData(packageHashcode)?.let {
            bindToCacheAppInfo(it)
        }
    }

    private fun bindToCacheAppInfo(appMetaData: AppMetaData) {
        val appInfo = AppInformation(appMetaData)

        fetchExtrasAppInfoFromDevice(appInfo)

        appInfoCache[appMetaData.packageHashcode] = appInfo
    }

    private fun fetchExtrasAppInfoFromDevice(appInfo: AppInformation) {
        try {
            val applicationInfo = getApplicationInfo(appInfo.packageName, applicationInfoFlags)

            if (appInfo.name == null) {
                appInfo.name = packageManager.getApplicationLabel(applicationInfo)
            }

            if (appInfo.icon == null) {
                appInfo.icon = packageManager.getApplicationIcon(appInfo.packageName)
            }
            appInfo.isInstalled = true
        } catch (e: Exception) {
            appInfo.isInstalled = false
        }
    }

    private suspend fun updateApp(appMetaData: AppMetaData) {
        notisaveRepository.updateAppMetaData(appMetaData)
    }

    internal fun getLauncherIntent(packageName: String): Intent? {
        return packageManager.getLaunchIntentForPackage(packageName)
    }

    private fun changeCleanableState(appMetaData: AppMetaData, state: Boolean) {
        workerScope.launch {
            appMetaData.isCleanable = state
            _appStateFlow.emit(
                CleanableStateChange(appMetaData.packageHashcode)
            )
            updateApp(appMetaData)
        }
    }

    internal fun disableCleanable(appInfo: AppInformation) {
        changeCleanableState(appInfo.appMetaData, false)
    }

    internal fun enableCleanable(appInfo: AppInformation) {
        changeCleanableState(appInfo.appMetaData, true)
    }

    private fun updateTrackingState(appMetaData: AppMetaData, state: Boolean) {
        workerScope.launch {
            appMetaData.isTracking = state
            _appStateFlow.emit(
                TrackingStateChange(appMetaData.packageHashcode)
            )
            updateApp(appMetaData)
        }
    }

    internal fun excludeApp(appInfo: AppInformation) {
        if (!appInfo.isTracking) return
        updateTrackingState(appInfo.appMetaData, false)
    }

    internal fun includeApp(appInfo: AppInformation) {
        if (appInfo.isTracking) return
        updateTrackingState(appInfo.appMetaData, true)
    }

    internal fun changeLogOngoingStateAllApps(appList: List<AppInformation>, isOngoing: Boolean) =
        workerScope.launch {
            val metaList = appList.map {
                it.appMetaData.apply {
                    isLogOnGoing = isOngoing
                }
            }
            notisaveRepository.updateLogOngoingAllApps(metaList)
        }

    private fun changeLogOngoingState(appInfo: AppInformation, isLogOngoing: Boolean) {
        workerScope.launch {
            val appMeta = appInfo.appMetaData
            appMeta.isLogOnGoing = isLogOngoing
            updateApp(appMeta)
        }
    }

    internal fun changeLogOngoingStateOfApp(packageHashcode: String, isLogOngoing: Boolean) {
        appInfoCache[packageHashcode]?.let {
            changeLogOngoingState(it, isLogOngoing)
        }
    }

    internal fun enableLogOngoing(appInfo: AppInformation) {
        changeLogOngoingState(appInfo, true)
    }

    internal fun disableLogOngoing(appInfo: AppInformation) {
        changeLogOngoingState(appInfo, false)
    }

    private fun getApplicationInfoFlags(): ApplicationInfoFlags? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ApplicationInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
        }
        return null
    }

    @Throws(NameNotFoundException::class)
    private fun getApplicationInfo(
        packageName: String,
        applicationInfoFlags: ApplicationInfoFlags?
    ): ApplicationInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && applicationInfoFlags != null) {
            packageManager.getApplicationInfo(packageName, applicationInfoFlags)
        } else {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES)
        }
    }

    internal fun clearCache() = appInfoCache.clear()

    internal fun setAppInfo(appInfo: AppInformation) {
        appInfoCache[appInfo.packageHashcode] = appInfo
    }

    sealed class AppState(val packageHashcode: String) {
        class CleanableStateChange(packageHashcode: String) : AppState(packageHashcode)
        class TrackingStateChange(packageHashcode: String) : AppState(packageHashcode)
    }
}