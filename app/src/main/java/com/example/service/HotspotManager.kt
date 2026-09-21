package com.example.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HotspotState {
    data object Idle : HotspotState()
    data class Starting(val initiatedBy: String) : HotspotState()
    data class Active(
        val ssid: String,
        val password: String,
        val startedAt: Long,
        val autoOffMinutes: Int,
        val triggeredBy: String
    ) : HotspotState()
    data class Error(val message: String, val errorCode: Int) : HotspotState()
}

/**
 * Opens Android's internet-tethering settings.
 *
 * LocalOnlyHotspot is deliberately not used here: it creates an isolated Wi-Fi
 * network and cannot share the phone's mobile-data connection. Android does not
 * expose a generally-available API for ordinary applications to silently start
 * and configure the internet hotspot. The system UI is therefore required.
 */
object HotspotManager {
    private const val TAG = "HotspotManager"

    // This action is public at runtime but is absent from some Android compile SDK stubs.
    private const val ACTION_TETHER_SETTINGS = "android.settings.TETHER_SETTINGS"

    private val _hotspotState = MutableStateFlow<HotspotState>(HotspotState.Idle)
    val hotspotState: StateFlow<HotspotState> = _hotspotState.asStateFlow()

    private var autoOffJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    val isNotificationListenerConnected = MutableStateFlow(false)

    fun isHotspotActive(): Boolean = _hotspotState.value is HotspotState.Active

    @Synchronized
    fun startHotspot(
        context: Context,
        triggeredBy: String,
        autoOffMinutes: Int = 15,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        _hotspotState.value = HotspotState.Starting(initiatedBy = triggeredBy)

        try {
            openTetheringSettings(appContext)

            val message =
                "Open Android Hotspot & Tethering settings to enable internet sharing"
            Log.i(TAG, message)
            _hotspotState.value = HotspotState.Error(message, ERROR_USER_ACTION_REQUIRED)
            onResult?.invoke(false, message)
        } catch (e: Exception) {
            val message =
                "Unable to open Hotspot & Tethering settings: " +
                    (e.localizedMessage ?: "Unknown error")
            Log.e(TAG, message, e)
            _hotspotState.value = HotspotState.Error(message, ERROR_SETTINGS_UNAVAILABLE)
            onResult?.invoke(false, message)
        }
    }

    @Synchronized
    fun stopHotspot() {
        autoOffJob?.cancel()
        _hotspotState.value = HotspotState.Idle
        // A regular application cannot reliably turn system tethering off either.
        // The user can disable it from Android's tethering settings.
    }

    private fun scheduleAutoOff(minutes: Int) {
        autoOffJob?.cancel()
        if (minutes <= 0) return
        autoOffJob = scope.launch {
            delay(minutes * 60 * 1000L)
            Log.i(TAG, "Auto-off timer expired after $minutes minutes.")
            stopHotspot()
        }
    }

    fun openTetheringSettings(context: Context) {
        val appContext = context.applicationContext
        try {
            appContext.startActivity(Intent(ACTION_TETHER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            try {
                appContext.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Could not open tethering settings", fallbackException)
                throw fallbackException
            }
        }
    }

    private const val ERROR_USER_ACTION_REQUIRED = -4
    private const val ERROR_SETTINGS_UNAVAILABLE = -5
}
