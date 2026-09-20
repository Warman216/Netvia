package com.example.service

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
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

object HotspotManager {
    private const val TAG = "HotspotManager"

    private val _hotspotState = MutableStateFlow<HotspotState>(HotspotState.Idle)
    val hotspotState: StateFlow<HotspotState> = _hotspotState.asStateFlow()

    private var currentReservation: WifiManager.LocalOnlyHotspotReservation? = null
    private var autoOffJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Listener state tracking
    val isNotificationListenerConnected = MutableStateFlow(false)

    fun isHotspotActive(): Boolean = _hotspotState.value is HotspotState.Active

    @Synchronized
    fun startHotspot(
        context: Context,
        triggeredBy: String,
        autoOffMinutes: Int = 15,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        if (isHotspotActive()) {
            val activeState = _hotspotState.value as HotspotState.Active
            Log.d(TAG, "Hotspot is already active: ${activeState.ssid}")
            // Reset/extend the auto-off timer if triggered again
            scheduleAutoOff(autoOffMinutes)
            onResult?.invoke(true, "Hotspot is already running (SSID: ${activeState.ssid})")
            return
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            val errorMsg = "Wi-Fi service is unavailable on this device"
            _hotspotState.value = HotspotState.Error(errorMsg, -1)
            onResult?.invoke(false, errorMsg)
            return
        }

        _hotspotState.value = HotspotState.Starting(initiatedBy = triggeredBy)

        try {
            wifiManager.startLocalOnlyHotspot(
                object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        currentReservation = reservation

                        var ssid = "AndroidHotspot"
                        var password = ""

                        if (reservation != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val softApConfig = reservation.softApConfiguration
                                if (softApConfig != null) {
                                    ssid = softApConfig.ssid ?: "AndroidHotspot"
                                    password = softApConfig.passphrase ?: ""
                                }
                            }

                            if (password.isEmpty()) {
                                @Suppress("DEPRECATION")
                                val wifiConfig = reservation.wifiConfiguration
                                if (wifiConfig != null) {
                                    ssid = wifiConfig.SSID ?: ssid
                                    password = wifiConfig.preSharedKey ?: ""
                                }
                            }
                        }

                        Log.i(TAG, "Hotspot started successfully: SSID=$ssid")
                        _hotspotState.value = HotspotState.Active(
                            ssid = ssid,
                            password = password,
                            startedAt = System.currentTimeMillis(),
                            autoOffMinutes = autoOffMinutes,
                            triggeredBy = triggeredBy
                        )

                        scheduleAutoOff(autoOffMinutes)

                        // Also notify foreground service to update ongoing notification
                        HotspotForegroundService.updateNotification(context.applicationContext)

                        onResult?.invoke(true, "Hotspot started (SSID: $ssid)")
                    }

                    override fun onStopped() {
                        super.onStopped()
                        Log.i(TAG, "Hotspot stopped callback received")
                        currentReservation = null
                        autoOffJob?.cancel()
                        _hotspotState.value = HotspotState.Idle
                        HotspotForegroundService.updateNotification(context.applicationContext)
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        currentReservation = null
                        autoOffJob?.cancel()

                        val errorMsg = when (reason) {
                            WifiManager.LocalOnlyHotspotCallback.ERROR_NO_CHANNEL -> "Failed to start hotspot: No Wi-Fi channel available"
                            WifiManager.LocalOnlyHotspotCallback.ERROR_GENERIC -> "Failed to start hotspot (Generic error)"
                            WifiManager.LocalOnlyHotspotCallback.ERROR_INCOMPATIBLE_MODE -> "Hotspot failed: Incompatible mode"
                            WifiManager.LocalOnlyHotspotCallback.ERROR_TETHERING_DISALLOWED -> "Hotspot tethering disallowed by carrier/system policy"
                            else -> "Failed to start hotspot (code: $reason)"
                        }

                        Log.e(TAG, errorMsg)
                        _hotspotState.value = HotspotState.Error(errorMsg, reason)
                        HotspotForegroundService.updateNotification(context.applicationContext)
                        onResult?.invoke(false, errorMsg)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } catch (e: SecurityException) {
            val errorMsg = "Location or Nearby Devices permission required to start hotspot"
            Log.e(TAG, errorMsg, e)
            _hotspotState.value = HotspotState.Error(errorMsg, -2)
            onResult?.invoke(false, errorMsg)
        } catch (e: Exception) {
            val errorMsg = "Error launching hotspot: ${e.localizedMessage ?: "Unknown error"}"
            Log.e(TAG, errorMsg, e)
            _hotspotState.value = HotspotState.Error(errorMsg, -3)
            onResult?.invoke(false, errorMsg)
        }
    }

    @Synchronized
    fun stopHotspot() {
        autoOffJob?.cancel()
        try {
            currentReservation?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing hotspot reservation", e)
        } finally {
            currentReservation = null
            _hotspotState.value = HotspotState.Idle
        }
    }

    private fun scheduleAutoOff(minutes: Int) {
        autoOffJob?.cancel()
        if (minutes <= 0) return

        autoOffJob = scope.launch {
            delay(minutes * 60 * 1000L)
            Log.i(TAG, "Auto-off timer expired after $minutes minutes. Stopping hotspot.")
            stopHotspot()
        }
    }

    fun openTetheringSettings(context: Context) {
        try {
            val intent = Intent("android.settings.TETHER_SETTINGS").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Log.e(TAG, "Could not open tethering settings", ex)
            }
        }
    }
}
