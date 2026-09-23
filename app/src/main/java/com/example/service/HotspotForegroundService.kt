package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HotspotForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        HotspotManager.initialize(this)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_HOTSPOT -> {
                HotspotManager.stopHotspot(this)
                updateNotificationInternal()
            }
            ACTION_UPDATE_NOTIFICATION -> {
                updateNotificationInternal()
            }
            else -> {
                startForegroundWithNotification()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            }
            startForeground(NOTIFICATION_ID, notification, type)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotificationInternal() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildForegroundNotification())
    }

    private fun buildForegroundNotification(): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentState = HotspotManager.hotspotState.value
        val prefs = AppPreferences(this)

        val (title, contentText, isHotspotOn) = when (currentState) {
            is HotspotState.Active -> {
                val modeTitle = if (currentState.isInternetSharing) "Internet Sharing Active" else "Local Hotspot Active"
                val passInfo = if (currentState.password.isNotEmpty()) " • Key: ${currentState.password}" else ""
                Triple(
                    "$modeTitle: ${currentState.ssid}",
                    "Triggered by ${currentState.triggeredBy}$passInfo",
                    true
                )
            }
            is HotspotState.Starting -> {
                val modeTitle = if (currentState.isInternetSharing) "Internet Sharing Hotspot" else "Local Hotspot"
                Triple("Starting $modeTitle…", "Triggered by ${currentState.initiatedBy}", false)
            }
            else -> {
                val filterText = if (prefs.filterMode == AppPreferences.FILTER_MODE_ALL) "All apps" else "${prefs.selectedPackages.size} app(s)"
                Triple(
                    "Hotspot Trigger Active",
                    if (prefs.isAutoHotspotEnabled) "Monitoring incoming notifications ($filterText)" else "Auto-hotspot is paused",
                    false
                )
            }
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_MONITOR_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isHotspotOn) {
            val stopIntent = Intent(this, HotspotForegroundService::class.java).apply {
                action = ACTION_STOP_HOTSPOT
            }
            val stopPendingIntent = PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Turn Off Hotspot",
                stopPendingIntent
            )
        }

        return builder.build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val monitorChannel = NotificationChannel(
                CHANNEL_MONITOR_ID,
                getString(R.string.channel_name_monitor),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_desc_monitor)
                setShowBadge(false)
            }

            val triggerChannel = NotificationChannel(
                CHANNEL_TRIGGER_ID,
                getString(R.string.channel_name_triggers),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_desc_triggers)
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannel(monitorChannel)
            manager.createNotificationChannel(triggerChannel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val TRIGGER_NOTIFICATION_ID = 1002
        const val CHANNEL_MONITOR_ID = "channel_hotspot_monitor"
        const val CHANNEL_TRIGGER_ID = "channel_hotspot_triggers"

        const val ACTION_START = "com.example.action.START_MONITOR"
        const val ACTION_STOP_HOTSPOT = "com.example.action.STOP_HOTSPOT"
        const val ACTION_UPDATE_NOTIFICATION = "com.example.action.UPDATE_NOTIFICATION"

        fun start(context: Context) {
            val intent = Intent(context, HotspotForegroundService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Background start restriction fallback
                e.printStackTrace()
            }
        }

        fun updateNotification(context: Context) {
            val intent = Intent(context, HotspotForegroundService::class.java).apply {
                action = ACTION_UPDATE_NOTIFICATION
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
