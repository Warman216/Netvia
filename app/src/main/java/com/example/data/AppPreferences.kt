package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        "hotspot_trigger_preferences",
        Context.MODE_PRIVATE
    )

    var isAutoHotspotEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_HOTSPOT_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_HOTSPOT_ENABLED, value).apply()

    var filterMode: String
        get() = prefs.getString(KEY_FILTER_MODE, FILTER_MODE_ALL) ?: FILTER_MODE_ALL
        set(value) = prefs.edit().putString(KEY_FILTER_MODE, value).apply()

    var selectedPackages: Set<String>
        get() = prefs.getStringSet(KEY_SELECTED_PACKAGES, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_SELECTED_PACKAGES, value).apply()

    var ignoreOngoing: Boolean
        get() = prefs.getBoolean(KEY_IGNORE_ONGOING, true)
        set(value) = prefs.edit().putBoolean(KEY_IGNORE_ONGOING, value).apply()

    var autoOffMinutes: Int
        get() = prefs.getInt(KEY_AUTO_OFF_MINUTES, 15) // default 15 minutes
        set(value) = prefs.edit().putInt(KEY_AUTO_OFF_MINUTES, value).apply()

    var cooldownSeconds: Int
        get() = prefs.getInt(KEY_COOLDOWN_SECONDS, 20) // 20s cooldown between rapid triggers
        set(value) = prefs.edit().putInt(KEY_COOLDOWN_SECONDS, value).apply()

    var notifyOnTrigger: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_ON_TRIGGER, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_ON_TRIGGER, value).apply()

    var vibrateOnTrigger: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE_ON_TRIGGER, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE_ON_TRIGGER, value).apply()

    var customSsid: String
        get() = prefs.getString(KEY_CUSTOM_SSID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_SSID, value).apply()

    var customPassword: String
        get() = prefs.getString(KEY_CUSTOM_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_PASSWORD, value).apply()

    var lastTriggerTimestamp: Long
        get() = prefs.getLong(KEY_LAST_TRIGGER_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_TRIGGER_TIMESTAMP, value).apply()

    var totalTriggersCount: Int
        get() = prefs.getInt(KEY_TOTAL_TRIGGERS_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_TRIGGERS_COUNT, value).apply()

    fun incrementTriggersCount(): Int {
        val next = totalTriggersCount + 1
        totalTriggersCount = next
        return next
    }

    companion object {
        const val KEY_AUTO_HOTSPOT_ENABLED = "auto_hotspot_enabled"
        const val KEY_FILTER_MODE = "filter_mode"
        const val KEY_SELECTED_PACKAGES = "selected_packages"
        const val KEY_IGNORE_ONGOING = "ignore_ongoing"
        const val KEY_AUTO_OFF_MINUTES = "auto_off_minutes"
        const val KEY_COOLDOWN_SECONDS = "cooldown_seconds"
        const val KEY_NOTIFY_ON_TRIGGER = "notify_on_trigger"
        const val KEY_VIBRATE_ON_TRIGGER = "vibrate_on_trigger"
        const val KEY_CUSTOM_SSID = "custom_ssid"
        const val KEY_CUSTOM_PASSWORD = "custom_password"
        const val KEY_LAST_TRIGGER_TIMESTAMP = "last_trigger_timestamp"
        const val KEY_TOTAL_TRIGGERS_COUNT = "total_triggers_count"

        const val FILTER_MODE_ALL = "ALL"
        const val FILTER_MODE_SELECTED = "SELECTED"
    }
}
