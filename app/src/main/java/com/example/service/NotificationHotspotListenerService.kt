package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.AppPreferences
import com.example.data.TriggerLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationHotspotListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var preferences: AppPreferences
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences(this)
        database = AppDatabase.getInstance(this)
        Log.i(TAG, "NotificationHotspotListenerService created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected successfully")
        HotspotManager.isNotificationListenerConnected.value = true

        // Ensure foreground monitor service is running
        HotspotForegroundService.start(applicationContext)

        scope.launch {
            database.triggerLogDao().insertLog(
                TriggerLogEntity(
                    packageName = packageName,
                    appName = "System / Listener",
                    notificationTitle = "Notification Listener Active",
                    notificationText = "Connected to notification stream",
                    wasHotspotTriggered = false,
                    statusMessage = "Monitoring started"
                )
            )
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected")
        HotspotManager.isNotificationListenerConnected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return

        // 1. Never trigger on our own notifications to avoid infinite loops
        if (pkg == packageName) return

        // 2. Read preferences
        if (!preferences.isAutoHotspotEnabled) {
            return
        }

        // 3. Extract notification metadata
        val notification = sbn.notification ?: return
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        if (isOngoing && preferences.ignoreOngoing) {
            Log.d(TAG, "Ignoring ongoing notification from $pkg")
            return
        }

        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "New Notification"
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        val packageManager = applicationContext.packageManager
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            pkg
        }

        // 4. App filter check
        if (preferences.filterMode == AppPreferences.FILTER_MODE_SELECTED) {
            val selected = preferences.selectedPackages
            if (!selected.contains(pkg)) {
                Log.d(TAG, "Notification from $pkg skipped by app filter")
                return
            }
        }

        // 5. Cooldown check
        val now = System.currentTimeMillis()
        val cooldownMs = preferences.cooldownSeconds * 1000L
        val lastTrigger = preferences.lastTriggerTimestamp
        val timeSinceLast = now - lastTrigger

        if (timeSinceLast < cooldownMs && HotspotManager.isHotspotActive()) {
            val remainingSec = ((cooldownMs - timeSinceLast) / 1000).toInt()
            Log.d(TAG, "Trigger skipped due to active cooldown: ${remainingSec}s remaining")
            scope.launch {
                database.triggerLogDao().insertLog(
                    TriggerLogEntity(
                        packageName = pkg,
                        appName = appName,
                        notificationTitle = title,
                        notificationText = text.take(120),
                        wasHotspotTriggered = false,
                        statusMessage = "Cooldown active (${remainingSec}s remaining)"
                    )
                )
            }
            return
        }

        // 6. Execute Hotspot activation
        preferences.lastTriggerTimestamp = now
        preferences.incrementTriggersCount()

        Log.i(TAG, "Triggering Hotspot on notification from: $appName ($pkg)")

        HotspotManager.startHotspot(
            context = applicationContext,
            triggeredBy = appName,
            autoOffMinutes = preferences.autoOffMinutes
        ) { success, message ->
            scope.launch {
                database.triggerLogDao().insertLog(
                    TriggerLogEntity(
                        packageName = pkg,
                        appName = appName,
                        notificationTitle = title,
                        notificationText = text.take(120),
                        wasHotspotTriggered = success,
                        statusMessage = message
                    )
                )
                database.triggerLogDao().trimOldLogs()
            }

            if (success) {
                if (preferences.vibrateOnTrigger) {
                    vibratePhone()
                }
                if (preferences.notifyOnTrigger) {
                    showTriggerNotification(appName, title)
                }
            }
        }
    }

    private fun vibratePhone() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(200)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    private fun showTriggerNotification(appName: String, title: String) {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            2,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, HotspotForegroundService.CHANNEL_TRIGGER_ID)
            .setContentTitle("Hotspot Activated Automatically")
            .setContentText("Triggered by $appName: $title")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(HotspotForegroundService.TRIGGER_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted for trigger alert")
        }
    }

    companion object {
        private const val TAG = "NotifHotspotListener"
    }
}
