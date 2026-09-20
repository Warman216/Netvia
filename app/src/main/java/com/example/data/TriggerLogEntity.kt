package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trigger_logs")
data class TriggerLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val appName: String,
    val notificationTitle: String,
    val notificationText: String,
    val wasHotspotTriggered: Boolean,
    val statusMessage: String
)
