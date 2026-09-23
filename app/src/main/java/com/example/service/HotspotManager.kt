package com.example.service

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.data.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.NetworkInterface

sealed class HotspotState {
    data object Idle : HotspotState()
    data class Starting(val initiatedBy: String, val isInternetSharing: Boolean = true) : HotspotState()
    data class Active(
        val ssid: String,
        val password: String,
        val startedAt: Long,
        val autoOffMinutes: Int,
        val triggeredBy: String,
        val isInternetSharing: Boolean = true
    ) : HotspotState()
    data class Error(val message: String, val errorCode: Int) : HotspotState()
}

object HotspotManager {
    private const val TAG = "HotspotManager"

    // Wi-Fi AP state constants from Android WifiManager hidden API
    private const val WIFI_AP_STATE_DISABLING = 10
    private const val WIFI_AP_STATE_DISABLED = 11
    private const val WIFI_AP_STATE_ENABLING = 12
    private const val WIFI_AP_STATE_ENABLED = 13
    private const val WIFI_AP_STATE_FAILED = 14
    private const val ACTION_WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED"
    private const val EXTRA_WIFI_AP_STATE = "wifi_state"

    private val _hotspotState = MutableStateFlow<HotspotState>(HotspotState.Idle)
    val hotspotState: StateFlow<HotspotState> = _hotspotState.asStateFlow()

    private var currentReservation: WifiManager.LocalOnlyHotspotReservation? = null
    private var autoOffJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Listener state tracking
    val isNotificationListenerConnected = MutableStateFlow(false)
    private var isReceiverRegistered = false
    private var applicationContext: Context? = null

    private val apStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_WIFI_AP_STATE_CHANGED) {
                val state = intent.getIntExtra(EXTRA_WIFI_AP_STATE, -1)
                Log.d(TAG, "WIFI_AP_STATE_CHANGED received: state=$state")
                handleSystemApStateChange(context, state)
            }
        }
    }

    /**
     * Initializes HotspotManager and registers the system AP broadcast receiver
     */
    fun initialize(context: Context) {
        val appContext = context.applicationContext
        this.applicationContext = appContext
        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter().apply {
                    addAction(ACTION_WIFI_AP_STATE_CHANGED)
                    addAction("android.net.conn.TETHER_STATE_CHANGED")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appContext.registerReceiver(apStateReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    appContext.registerReceiver(apStateReceiver, filter)
                }
                isReceiverRegistered = true
                Log.i(TAG, "HotspotManager initialized and AP state receiver registered")
            } catch (e: Exception) {
                Log.w(TAG, "Could not register AP state receiver: ${e.message}")
            }
        }

        // Check if system tethering is already active on app startup
        if (isSystemTetheringActive()) {
            val prefs = AppPreferences(appContext)
            _hotspotState.value = HotspotState.Active(
                ssid = prefs.customSsid,
                password = prefs.customPassword,
                startedAt = System.currentTimeMillis(),
                autoOffMinutes = prefs.autoOffMinutes,
                triggeredBy = "System Tethering",
                isInternetSharing = true
            )
            scheduleAutoOff(prefs.autoOffMinutes)
        }
    }

    private fun handleSystemApStateChange(context: Context, state: Int) {
        val prefs = AppPreferences(context)
        when (state) {
            WIFI_AP_STATE_ENABLED -> {
                Log.i(TAG, "System Wi-Fi Tethering (Internet Sharing Hotspot) is now ACTIVE")
                _hotspotState.value = HotspotState.Active(
                    ssid = prefs.customSsid,
                    password = prefs.customPassword,
                    startedAt = System.currentTimeMillis(),
                    autoOffMinutes = prefs.autoOffMinutes,
                    triggeredBy = "Internet Sharing Hotspot",
                    isInternetSharing = true
                )
                scheduleAutoOff(prefs.autoOffMinutes)
                HotspotForegroundService.updateNotification(context.applicationContext)
            }
            WIFI_AP_STATE_ENABLING -> {
                Log.d(TAG, "System Wi-Fi Tethering is enabling...")
                if (_hotspotState.value !is HotspotState.Active) {
                    _hotspotState.value = HotspotState.Starting("System Tethering", isInternetSharing = true)
                }
            }
            WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_DISABLING -> {
                Log.i(TAG, "System Wi-Fi Tethering is turned OFF")
                if (_hotspotState.value is HotspotState.Active) {
                    val active = _hotspotState.value as HotspotState.Active
                    if (active.isInternetSharing) {
                        autoOffJob?.cancel()
                        _hotspotState.value = HotspotState.Idle
                        HotspotForegroundService.updateNotification(context.applicationContext)
                    }
                } else if (_hotspotState.value is HotspotState.Starting) {
                    val starting = _hotspotState.value as HotspotState.Starting
                    if (starting.isInternetSharing) {
                        _hotspotState.value = HotspotState.Idle
                    }
                }
            }
            WIFI_AP_STATE_FAILED -> {
                Log.e(TAG, "System Wi-Fi Tethering failed")
                _hotspotState.value = HotspotState.Error("Wi-Fi Hotspot failed to activate in system", WIFI_AP_STATE_FAILED)
                HotspotForegroundService.updateNotification(context.applicationContext)
            }
        }
    }

    /**
     * Checks if any tethering network interface (ap0, softap, swlan, etc.) is currently up
     */
    fun isSystemTetheringActive(): Boolean {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
            for (iface in interfaces.asSequence()) {
                if (iface.isUp && !iface.isLoopback) {
                    val name = iface.name.lowercase()
                    if (name.startsWith("ap") || name.startsWith("softap") || name.startsWith("swlan") || name.startsWith("rndis")) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Could not check network interfaces: ${e.message}")
        }
        return false
    }

    fun isHotspotActive(): Boolean = _hotspotState.value is HotspotState.Active

    /**
     * Dynamically updates active hotspot credentials when user edits SSID or password
     */
    fun updateActiveCredentials(ssid: String, password: String, context: Context) {
        val current = _hotspotState.value
        if (current is HotspotState.Active) {
            _hotspotState.value = current.copy(ssid = ssid, password = password)
            HotspotForegroundService.updateNotification(context.applicationContext)
            Log.i(TAG, "Active hotspot credentials updated: SSID=$ssid, Password updated")
        }
    }

    @Synchronized
    fun startHotspot(
        context: Context,
        triggeredBy: String,
        autoOffMinutes: Int = 15,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        initialize(context)
        val prefs = AppPreferences(context)
        val mode = prefs.hotspotMode

        if (isHotspotActive()) {
            val activeState = _hotspotState.value as HotspotState.Active
            Log.d(TAG, "Hotspot is already active: ${activeState.ssid}")
            scheduleAutoOff(autoOffMinutes)
            onResult?.invoke(true, "Hotspot is already active (SSID: ${activeState.ssid})")
            return
        }

        if (mode == AppPreferences.HOTSPOT_MODE_INTERNET_SHARING) {
            startInternetSharingHotspot(context, triggeredBy, autoOffMinutes, onResult)
        } else {
            startLocalOnlyHotspot(context, triggeredBy, autoOffMinutes, onResult)
        }
    }

    /**
     * Initiates Internet Sharing Hotspot (Mobile Wi-Fi Tethering)
     * Copies custom credentials to clipboard and launches Android Hotspot settings
     */
    fun startInternetSharingHotspot(
        context: Context,
        triggeredBy: String,
        autoOffMinutes: Int = 15,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        initialize(context)
        val prefs = AppPreferences(context)
        val ssid = prefs.customSsid
        val password = prefs.customPassword

        // Copy credentials to clipboard so user can quickly paste in hotspot settings if needed
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Hotspot Password", password)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                context,
                "Password \"$password\" copied! Toggle Hotspot ON in settings.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.w(TAG, "Clipboard copy error: ${e.message}")
        }

        if (isSystemTetheringActive()) {
            _hotspotState.value = HotspotState.Active(
                ssid = ssid,
                password = password,
                startedAt = System.currentTimeMillis(),
                autoOffMinutes = autoOffMinutes,
                triggeredBy = triggeredBy,
                isInternetSharing = true
            )
            scheduleAutoOff(autoOffMinutes)
            HotspotForegroundService.updateNotification(context.applicationContext)
            onResult?.invoke(true, "Internet Sharing Hotspot is active (SSID: $ssid)")
            return
        }

        _hotspotState.value = HotspotState.Starting(initiatedBy = triggeredBy, isInternetSharing = true)
        openTetheringSettings(context)
        onResult?.invoke(true, "Opening System Wi-Fi Hotspot settings for Internet Sharing")
    }

    /**
     * Starts Local-Only Hotspot (P2P device-to-device network)
     */
    private fun startLocalOnlyHotspot(
        context: Context,
        triggeredBy: String,
        autoOffMinutes: Int = 15,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            val errorMsg = "Wi-Fi service is unavailable on this device"
            _hotspotState.value = HotspotState.Error(errorMsg, -1)
            onResult?.invoke(false, errorMsg)
            return
        }

        val prefs = AppPreferences(context)
        val customSsid = prefs.customSsid
        val customPassword = prefs.customPassword

        _hotspotState.value = HotspotState.Starting(initiatedBy = triggeredBy, isInternetSharing = false)

        try {
            wifiManager.startLocalOnlyHotspot(
                object : WifiManager.LocalOnlyHotspotCallback() {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        currentReservation = reservation

                        var ssid = customSsid
                        var password = customPassword

                        if (reservation != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val softApConfig = reservation.softApConfiguration
                                if (softApConfig != null) {
                                    val apSsid = softApConfig.ssid
                                    val apPass = softApConfig.passphrase
                                    if (!apSsid.isNullOrEmpty()) ssid = apSsid
                                    if (!apPass.isNullOrEmpty()) password = apPass
                                }
                            }
                            if (password == customPassword) {
                                @Suppress("DEPRECATION")
                                val wifiConfig = reservation.wifiConfiguration
                                if (wifiConfig != null) {
                                    val apSsid = wifiConfig.SSID
                                    val apPass = wifiConfig.preSharedKey
                                    if (!apSsid.isNullOrEmpty()) ssid = apSsid
                                    if (!apPass.isNullOrEmpty()) password = apPass
                                }
                            }
                        }

                        Log.i(TAG, "Local hotspot started successfully: SSID=$ssid")
                        _hotspotState.value = HotspotState.Active(
                            ssid = ssid,
                            password = password,
                            startedAt = System.currentTimeMillis(),
                            autoOffMinutes = autoOffMinutes,
                            triggeredBy = triggeredBy,
                            isInternetSharing = false
                        )
                        scheduleAutoOff(autoOffMinutes)
                        HotspotForegroundService.updateNotification(context.applicationContext)
                        onResult?.invoke(true, "Local hotspot started (SSID: $ssid)")
                    }

                    override fun onStopped() {
                        super.onStopped()
                        Log.i(TAG, "Local hotspot stopped callback received")
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
    fun stopHotspot(context: Context? = null) {
        autoOffJob?.cancel()
        val current = _hotspotState.value
        try {
            currentReservation?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing hotspot reservation", e)
        } finally {
            currentReservation = null
        }

        if (current is HotspotState.Active && current.isInternetSharing && context != null) {
            // Open settings to let user toggle off system tethering if active
            openTetheringSettings(context)
        }
        _hotspotState.value = HotspotState.Idle
        applicationContext?.let { HotspotForegroundService.updateNotification(it) }
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

    /**
     * Deep-links directly to Android's native Wi-Fi Tethering & Mobile Hotspot configuration
     */
    fun openTetheringSettings(context: Context) {
        val intentActions = listOf(
            "android.settings.WIFI_TETHER_SETTINGS",
            "android.settings.TETHER_SETTINGS",
            Settings.ACTION_WIRELESS_SETTINGS
        )
        for (action in intentActions) {
            try {
                val intent = Intent(action).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next action
            }
        }
    }
}
