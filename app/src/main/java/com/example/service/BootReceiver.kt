package com.example.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.AppPreferences
import com.example.data.TriggerLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        Log.i(TAG, "BootReceiver received action: $action")

        val validBootActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )

        if (validBootActions.contains(action)) {
            val prefs = AppPreferences(context)

            // Re-request rebind to NotificationListenerService across reboot
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    val component = ComponentName(context, NotificationHotspotListenerService::class.java)
                    NotificationListenerService.requestRebind(component)
                    Log.i(TAG, "Requested rebind for NotificationHotspotListenerService")
                } catch (e: Exception) {
                    Log.w(TAG, "Rebind request skipped or failed: ${e.message}")
                }
            }

            if (prefs.isAutoHotspotEnabled) {
                // Start foreground monitor to keep process active
                HotspotForegroundService.start(context)

                // Log reboot event into local database
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        db.triggerLogDao().insertLog(
                            TriggerLogEntity(
                                packageName = "android",
                                appName = "Android System",
                                notificationTitle = "Device Reboot Completed",
                                notificationText = "Reboot action: $action",
                                wasHotspotTriggered = false,
                                statusMessage = "Auto-hotspot monitor restored across reboot"
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to log reboot event", e)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "HotspotBootReceiver"
    }
}
