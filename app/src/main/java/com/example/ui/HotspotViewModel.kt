package com.example.ui

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppItem
import com.example.data.AppPreferences
import com.example.data.TriggerLogEntity
import com.example.service.HotspotForegroundService
import com.example.service.HotspotManager
import com.example.service.HotspotState
import com.example.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HealthStatus(
    val hasNotificationAccess: Boolean = false,
    val hasLocationOrNearby: Boolean = false,
    val hasPostNotifications: Boolean = false,
    val isBatteryOptimized: Boolean = true,
    val isListenerServiceActive: Boolean = false
)

class HotspotViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val preferences = AppPreferences(context)
    private val database = AppDatabase.getInstance(context)

    val hotspotState: StateFlow<HotspotState> = HotspotManager.hotspotState

    val recentLogs: StateFlow<List<TriggerLogEntity>> = database.triggerLogDao()
        .getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _healthStatus = MutableStateFlow(HealthStatus())
    val healthStatus: StateFlow<HealthStatus> = _healthStatus.asStateFlow()

    // Settings state
    private val _isAutoHotspotEnabled = MutableStateFlow(preferences.isAutoHotspotEnabled)
    val isAutoHotspotEnabled: StateFlow<Boolean> = _isAutoHotspotEnabled.asStateFlow()

    private val _filterMode = MutableStateFlow(preferences.filterMode)
    val filterMode: StateFlow<String> = _filterMode.asStateFlow()

    private val _selectedPackages = MutableStateFlow(preferences.selectedPackages)
    val selectedPackages: StateFlow<Set<String>> = _selectedPackages.asStateFlow()

    private val _ignoreOngoing = MutableStateFlow(preferences.ignoreOngoing)
    val ignoreOngoing: StateFlow<Boolean> = _ignoreOngoing.asStateFlow()

    private val _autoOffMinutes = MutableStateFlow(preferences.autoOffMinutes)
    val autoOffMinutes: StateFlow<Int> = _autoOffMinutes.asStateFlow()

    private val _cooldownSeconds = MutableStateFlow(preferences.cooldownSeconds)
    val cooldownSeconds: StateFlow<Int> = _cooldownSeconds.asStateFlow()

    private val _notifyOnTrigger = MutableStateFlow(preferences.notifyOnTrigger)
    val notifyOnTrigger: StateFlow<Boolean> = _notifyOnTrigger.asStateFlow()

    private val _vibrateOnTrigger = MutableStateFlow(preferences.vibrateOnTrigger)
    val vibrateOnTrigger: StateFlow<Boolean> = _vibrateOnTrigger.asStateFlow()

    private val _customSsid = MutableStateFlow(preferences.customSsid)
    val customSsid: StateFlow<String> = _customSsid.asStateFlow()

    private val _customPassword = MutableStateFlow(preferences.customPassword)
    val customPassword: StateFlow<String> = _customPassword.asStateFlow()

    private val _hotspotMode = MutableStateFlow(preferences.hotspotMode)
    val hotspotMode: StateFlow<String> = _hotspotMode.asStateFlow()

    private val _totalTriggers = MutableStateFlow(preferences.totalTriggersCount)
    val totalTriggers: StateFlow<Int> = _totalTriggers.asStateFlow()

    // Installed apps
    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    init {
        HotspotManager.initialize(context)
        refreshHealthStatus()
        loadInstalledApps()

        // Start foreground service if enabled
        if (preferences.isAutoHotspotEnabled) {
            HotspotForegroundService.start(context)
        }
    }

    fun refreshHealthStatus() {
        val hasNotif = PermissionHelper.isNotificationAccessGranted(context)
        val hasLoc = PermissionHelper.isLocationOrNearbyGranted(context)
        val hasPost = PermissionHelper.isPostNotificationsGranted(context)
        val isBatOpt = !PermissionHelper.isIgnoringBatteryOptimizations(context)
        val isListenerActive = HotspotManager.isNotificationListenerConnected.value || hasNotif

        _healthStatus.value = HealthStatus(
            hasNotificationAccess = hasNotif,
            hasLocationOrNearby = hasLoc,
            hasPostNotifications = hasPost,
            isBatteryOptimized = isBatOpt,
            isListenerServiceActive = isListenerActive
        )
    }

    fun toggleAutoHotspot(enabled: Boolean) {
        preferences.isAutoHotspotEnabled = enabled
        _isAutoHotspotEnabled.value = enabled
        if (enabled) {
            HotspotForegroundService.start(context)
        } else {
            HotspotForegroundService.updateNotification(context)
        }
    }

    fun startManualHotspot() {
        HotspotManager.startHotspot(
            context = context,
            triggeredBy = "Manual User Trigger",
            autoOffMinutes = _autoOffMinutes.value
        ) { success, msg ->
            viewModelScope.launch(Dispatchers.IO) {
                database.triggerLogDao().insertLog(
                    TriggerLogEntity(
                        packageName = context.packageName,
                        appName = "Hotspot Trigger",
                        notificationTitle = "Manual Activation",
                        notificationText = "User initiated hotspot directly from dashboard",
                        wasHotspotTriggered = success,
                        statusMessage = msg
                    )
                )
            }
        }
    }

    fun stopManualHotspot() {
        HotspotManager.stopHotspot(context)
        viewModelScope.launch(Dispatchers.IO) {
            database.triggerLogDao().insertLog(
                TriggerLogEntity(
                    packageName = context.packageName,
                    appName = "Hotspot Trigger",
                    notificationTitle = "Manual Deactivation",
                    notificationText = "User stopped hotspot directly from dashboard",
                    wasHotspotTriggered = false,
                    statusMessage = "Hotspot turned off"
                )
            )
        }
    }

    fun simulateNotificationTrigger(appName: String = "Test App", title: String = "Incoming Notification") {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.incrementTriggersCount()
            _totalTriggers.value = preferences.totalTriggersCount

            HotspotManager.startHotspot(
                context = context,
                triggeredBy = appName,
                autoOffMinutes = _autoOffMinutes.value
            ) { success, msg ->
                viewModelScope.launch(Dispatchers.IO) {
                    database.triggerLogDao().insertLog(
                        TriggerLogEntity(
                            packageName = "com.test.simulation",
                            appName = appName,
                            notificationTitle = title,
                            notificationText = "Simulated notification trigger test",
                            wasHotspotTriggered = success,
                            statusMessage = msg
                        )
                    )
                }
            }
        }
    }

    fun setFilterMode(mode: String) {
        preferences.filterMode = mode
        _filterMode.value = mode
        HotspotForegroundService.updateNotification(context)
    }

    fun toggleAppSelected(pkg: String) {
        val current = _selectedPackages.value.toMutableSet()
        if (current.contains(pkg)) {
            current.remove(pkg)
        } else {
            current.add(pkg)
        }
        preferences.selectedPackages = current
        _selectedPackages.value = current

        // Update installed apps list selection state
        _installedApps.value = _installedApps.value.map {
            if (it.packageName == pkg) it.copy(isSelected = current.contains(pkg)) else it
        }
        HotspotForegroundService.updateNotification(context)
    }

    fun setIgnoreOngoing(ignore: Boolean) {
        preferences.ignoreOngoing = ignore
        _ignoreOngoing.value = ignore
    }

    fun setAutoOffMinutes(minutes: Int) {
        preferences.autoOffMinutes = minutes
        _autoOffMinutes.value = minutes
    }

    fun setCooldownSeconds(seconds: Int) {
        preferences.cooldownSeconds = seconds
        _cooldownSeconds.value = seconds
    }

    fun setNotifyOnTrigger(notify: Boolean) {
        preferences.notifyOnTrigger = notify
        _notifyOnTrigger.value = notify
    }

    fun setVibrateOnTrigger(vibrate: Boolean) {
        preferences.vibrateOnTrigger = vibrate
        _vibrateOnTrigger.value = vibrate
    }

    fun setHotspotMode(mode: String) {
        preferences.hotspotMode = mode
        _hotspotMode.value = mode
    }

    fun updateCustomSsid(ssid: String) {
        val trimmed = ssid.trim()
        if (trimmed.isNotEmpty()) {
            preferences.customSsid = trimmed
            _customSsid.value = trimmed
            HotspotManager.updateActiveCredentials(trimmed, _customPassword.value, context)
        }
    }

    fun updateCustomPassword(password: String) {
        preferences.customPassword = password
        _customPassword.value = password
        HotspotManager.updateActiveCredentials(_customSsid.value, password, context)
    }

    fun clearLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            database.triggerLogDao().clearLogs()
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingApps.value = true
            try {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                val selected = preferences.selectedPackages

                val apps = resolveInfos.mapNotNull { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    if (pkg == context.packageName) null
                    else {
                        val label = resolveInfo.loadLabel(pm).toString()
                        AppItem(
                            packageName = pkg,
                            appName = label,
                            isSelected = selected.contains(pkg)
                        )
                    }
                }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }

                withContext(Dispatchers.Main) {
                    _installedApps.value = apps
                    _isLoadingApps.value = false
                }
            } catch (e: Exception) {
                _isLoadingApps.value = false
            }
        }
    }
}
