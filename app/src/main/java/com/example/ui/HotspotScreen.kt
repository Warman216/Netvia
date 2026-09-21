package com.example.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.WifiTetheringOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.components.CustomizeCredentialsModal
import com.example.ui.components.HotspotCredentialsCard
import com.example.ui.components.LiquidGlassSquircleCard
import com.example.ui.components.SquircleCard
import com.example.ui.components.SquircleLarge
import com.example.ui.components.SquircleMedium
import com.example.ui.components.SquircleSmall
import com.example.ui.components.VibrantMeshGradientBackground
import com.example.ui.theme.GradientCyan
import com.example.ui.theme.GradientElectricBlue
import com.example.ui.theme.GradientMagenta
import com.example.ui.theme.GradientViolet
import com.example.ui.theme.GradientSunsetPink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.data.AppItem
import com.example.data.AppPreferences
import com.example.data.TriggerLogEntity
import com.example.service.HotspotManager
import com.example.service.HotspotState
import com.example.util.PermissionHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotspotScreen(
    viewModel: HotspotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hotspotState by viewModel.hotspotState.collectAsState()
    val healthStatus by viewModel.healthStatus.collectAsState()
    val isAutoEnabled by viewModel.isAutoHotspotEnabled.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val selectedPackages by viewModel.selectedPackages.collectAsState()
    val ignoreOngoing by viewModel.ignoreOngoing.collectAsState()
    val autoOffMinutes by viewModel.autoOffMinutes.collectAsState()
    val cooldownSeconds by viewModel.cooldownSeconds.collectAsState()
    val notifyOnTrigger by viewModel.notifyOnTrigger.collectAsState()
    val vibrateOnTrigger by viewModel.vibrateOnTrigger.collectAsState()
    val customSsid by viewModel.customSsid.collectAsState()
    val customPassword by viewModel.customPassword.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    var showAppPickerDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSimulateDialog by remember { mutableStateOf(false) }
    var showCredentialsDialog by remember { mutableStateOf(false) }

    // Re-check permissions whenever app returns to foreground
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshHealthStatus()
    }

    // Permission launchers
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshHealthStatus()
    }

    val isHotspotActive = hotspotState is HotspotState.Active

    VibrantMeshGradientBackground(
        modifier = modifier.fillMaxSize(),
        isHotspotActive = isHotspotActive
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(SquircleSmall)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GradientElectricBlue, GradientCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = "Hotspot Icon",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NetVia",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "Notification Auto-Start • Boot Resilient",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Details & How it Works"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refreshHealthStatus() },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh System Status"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Hero Status Card with Radar Wave & Hotspot Details
            item {
                HeroStatusCard(
                    hotspotState = hotspotState,
                    isAutoEnabled = isAutoEnabled,
                    healthStatus = healthStatus,
                    onToggleAuto = { viewModel.toggleAutoHotspot(it) },
                    onStartHotspot = { viewModel.startManualHotspot() },
                    onStopHotspot = { viewModel.stopManualHotspot() },
                    onOpenTetherSettings = { HotspotManager.openTetheringSettings(context) },
                    onSimulateNotification = { showSimulateDialog = true }
                )
            }

            // 2. Network Credentials Card (Customizable SSID & Password in Liquid Glass Squircle)
            item {
                HotspotCredentialsCard(
                    customSsid = customSsid,
                    customPassword = customPassword,
                    onConfigureClick = { showCredentialsDialog = true }
                )
            }

            // 3. Health & Boot Resilience Status Card
            item {
                HealthStatusCard(
                    healthStatus = healthStatus,
                    onRequestPermissions = {
                        val perms = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(perms.toTypedArray())
                    },
                    onOpenNotificationAccess = {
                        PermissionHelper.openNotificationAccessSettings(context)
                    },
                    onOpenBatteryOptimization = {
                        PermissionHelper.requestIgnoreBatteryOptimizations(context)
                    }
                )
            }

            // 3. Trigger Rules & Filtering Card
            item {
                TriggerRulesCard(
                    filterMode = filterMode,
                    selectedCount = selectedPackages.size,
                    ignoreOngoing = ignoreOngoing,
                    autoOffMinutes = autoOffMinutes,
                    cooldownSeconds = cooldownSeconds,
                    notifyOnTrigger = notifyOnTrigger,
                    vibrateOnTrigger = vibrateOnTrigger,
                    onSetFilterMode = { viewModel.setFilterMode(it) },
                    onOpenAppPicker = { showAppPickerDialog = true },
                    onToggleIgnoreOngoing = { viewModel.setIgnoreOngoing(it) },
                    onSetAutoOffMinutes = { viewModel.setAutoOffMinutes(it) },
                    onSetCooldownSeconds = { viewModel.setCooldownSeconds(it) },
                    onToggleNotify = { viewModel.setNotifyOnTrigger(it) },
                    onToggleVibrate = { viewModel.setVibrateOnTrigger(it) }
                )
            }

            // 4. Live Trigger Log Header & List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Activity & Trigger History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${recentLogs.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (recentLogs.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearLogs() },
                            modifier = Modifier.testTag("clear_logs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (recentLogs.isEmpty()) {
                item {
                    EmptyLogPlaceholder(
                        onSimulate = { showSimulateDialog = true }
                    )
                }
            } else {
                items(recentLogs, key = { it.id }) { log ->
                    LogItemCard(log = log)
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
    }

    // App Picker Dialog
    if (showAppPickerDialog) {
        AppPickerModal(
            apps = installedApps,
            onToggleApp = { viewModel.toggleAppSelected(it) },
            onDismiss = { showAppPickerDialog = false }
        )
    }

    // Info Dialog explaining Reboot and Notification Listener
    if (showInfoDialog) {
        InfoModal(onDismiss = { showInfoDialog = false })
    }

    // Customize Credentials Dialog
    if (showCredentialsDialog) {
        CustomizeCredentialsModal(
            initialSsid = customSsid,
            initialPassword = customPassword,
            onSave = { newSsid, newPassword ->
                viewModel.updateCustomSsid(newSsid)
                viewModel.updateCustomPassword(newPassword)
                showCredentialsDialog = false
                Toast.makeText(context, "Hotspot credentials saved: $newSsid", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCredentialsDialog = false }
        )
    }

    // Simulate Notification Dialog
    if (showSimulateDialog) {
        SimulateNotificationModal(
            onSimulate = { appName, title ->
                viewModel.simulateNotificationTrigger(appName, title)
                showSimulateDialog = false
                Toast.makeText(context, "Notification simulated from $appName", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showSimulateDialog = false }
        )
    }
}

@Composable
fun HeroStatusCard(
    hotspotState: HotspotState,
    isAutoEnabled: Boolean,
    healthStatus: HealthStatus,
    onToggleAuto: (Boolean) -> Unit,
    onStartHotspot: () -> Unit,
    onStopHotspot: () -> Unit,
    onOpenTetherSettings: () -> Unit,
    onSimulateNotification: () -> Unit
) {
    val context = LocalContext.current
    val isActive = hotspotState is HotspotState.Active
    val isStarting = hotspotState is HotspotState.Starting

    // Concentric radar wave animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LiquidGlassSquircleCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_status_card"),
        shape = SquircleCard,
        accentGlow = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row with Status Tag and Auto Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HOTSPOT TRIGGER ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isAutoEnabled) "Automatic on Notifications" else "Auto-Trigger Disabled",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Switch(
                    checked = isAutoEnabled,
                    onCheckedChange = onToggleAuto,
                    modifier = Modifier.testTag("auto_trigger_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulse Indicator Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleMedium)
                    .background(
                        if (isActive) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.22f),
                                    Color(0xFF00E676).copy(alpha = 0.18f),
                                    Color(0xFF0066FF).copy(alpha = 0.12f)
                                )
                            )
                        } else if (isStarting) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GradientViolet.copy(alpha = 0.25f),
                                    GradientElectricBlue.copy(alpha = 0.20f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = if (isActive) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF).copy(alpha = 0.6f), Color(0xFF00E676).copy(alpha = 0.6f))
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                            )
                        },
                        shape = SquircleMedium
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                Color(0xFF00E5FF).copy(alpha = 0.45f),
                                                Color(0xFF00E676).copy(alpha = 0.15f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) {
                                        Brush.linearGradient(
                                            listOf(Color(0xFF00E5FF), Color(0xFF00B09B))
                                        )
                                    } else if (isStarting) {
                                        Brush.linearGradient(
                                            listOf(GradientViolet, GradientElectricBlue)
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.WifiTethering else if (isStarting) Icons.Default.Autorenew else Icons.Outlined.WifiTetheringOff,
                                contentDescription = null,
                                tint = if (isActive || isStarting) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val stateLabel = when (hotspotState) {
                            is HotspotState.Active -> "HOTSPOT ACTIVE"
                            is HotspotState.Starting -> "STARTING HOTSPOT…"
                            is HotspotState.Error -> "ERROR STARTING"
                            else -> if (isAutoEnabled) "STANDBY (MONITORING)" else "IDLE"
                        }
                        val stateColor = when (hotspotState) {
                            is HotspotState.Active -> MaterialTheme.colorScheme.secondary
                            is HotspotState.Starting -> MaterialTheme.colorScheme.primary
                            is HotspotState.Error -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Text(
                            text = stateLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = stateColor
                        )

                        when (hotspotState) {
                            is HotspotState.Active -> {
                                Text(
                                    text = hotspotState.ssid,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (hotspotState.password.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Hotspot Password", hotspotState.password))
                                                Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                                            }
                                    ) {
                                        Text(
                                            text = "Key: ${hotspotState.password}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy password",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            is HotspotState.Starting -> {
                                Text(
                                    text = "Initiated by ${hotspotState.initiatedBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            is HotspotState.Error -> {
                                Text(
                                    text = hotspotState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            else -> {
                                Text(
                                    text = if (healthStatus.hasNotificationAccess) "Ready to turn on hotspot upon receiving notifications" else "Notification permission needed to detect notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(SquircleMedium)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF3B30), Color(0xFFFF453A))
                                )
                            )
                    ) {
                        Button(
                            onClick = onStopHotspot,
                            shape = SquircleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stop_hotspot_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Turn Off", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(SquircleMedium)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GradientElectricBlue, GradientCyan)
                                )
                            )
                    ) {
                        Button(
                            onClick = onStartHotspot,
                            shape = SquircleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_hotspot_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Turn On", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSimulateNotification,
                    shape = SquircleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_notification_button")
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Trigger")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shortcut to System Tethering
            TextButton(
                onClick = onOpenTetherSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("system_tethering_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Open System Hotspot & Tethering Settings",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun HealthStatusCard(
    healthStatus: HealthStatus,
    onRequestPermissions: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenBatteryOptimization: () -> Unit
) {
    LiquidGlassSquircleCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("health_status_card"),
        shape = SquircleCard,
        accentGlow = if (healthStatus.hasNotificationAccess && healthStatus.hasLocationOrNearby) GradientCyan else GradientSunsetPink
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "System Readiness & Boot Persistence",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Notification Access Permission
            HealthCheckRow(
                title = "Notification Listener Access",
                subtitle = if (healthStatus.hasNotificationAccess) "Granted • Able to intercept notifications" else "Required • Tap to grant in settings",
                isOk = healthStatus.hasNotificationAccess,
                buttonText = if (!healthStatus.hasNotificationAccess) "Grant" else null,
                onAction = onOpenNotificationAccess,
                testTag = "grant_notif_access_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Wi-Fi & Location Permission
            HealthCheckRow(
                title = "Wi-Fi & Location Permissions",
                subtitle = if (healthStatus.hasLocationOrNearby) "Granted • Ready to create local hotspot" else "Required • Needed by Android Wi-Fi API",
                isOk = healthStatus.hasLocationOrNearby,
                buttonText = if (!healthStatus.hasLocationOrNearby) "Allow" else null,
                onAction = onRequestPermissions,
                testTag = "allow_location_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Boot Persistence
            HealthCheckRow(
                title = "Device Reboot Persistence",
                subtitle = "Active • RECEIVE_BOOT_COMPLETED broadcast receiver registered",
                isOk = true,
                buttonText = null,
                onAction = {},
                testTag = null
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Battery Optimization
            HealthCheckRow(
                title = "Background Battery Optimization",
                subtitle = if (!healthStatus.isBatteryOptimized) "Unrestricted • 24/7 background reliability" else "Standard • Tap to disable optimization",
                isOk = !healthStatus.isBatteryOptimized,
                buttonText = if (healthStatus.isBatteryOptimized) "Optimize" else null,
                onAction = onOpenBatteryOptimization,
                testTag = "optimize_battery_button"
            )
        }
    }
}

@Composable
fun HealthCheckRow(
    title: String,
    subtitle: String,
    isOk: Boolean,
    buttonText: String?,
    onAction: () -> Unit,
    testTag: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isOk) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (buttonText != null) {
            Button(
                onClick = onAction,
                modifier = Modifier
                    .height(34.dp)
                    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = buttonText, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TriggerRulesCard(
    filterMode: String,
    selectedCount: Int,
    ignoreOngoing: Boolean,
    autoOffMinutes: Int,
    cooldownSeconds: Int,
    notifyOnTrigger: Boolean,
    vibrateOnTrigger: Boolean,
    onSetFilterMode: (String) -> Unit,
    onOpenAppPicker: () -> Unit,
    onToggleIgnoreOngoing: (Boolean) -> Unit,
    onSetAutoOffMinutes: (Int) -> Unit,
    onSetCooldownSeconds: (Int) -> Unit,
    onToggleNotify: (Boolean) -> Unit,
    onToggleVibrate: (Boolean) -> Unit
) {
    LiquidGlassSquircleCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trigger_rules_card"),
        shape = SquircleCard,
        accentGlow = GradientViolet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Automation Rules & Filters",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Filter Mode
            Text(
                text = "Target Applications",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterMode == AppPreferences.FILTER_MODE_ALL,
                    onClick = { onSetFilterMode(AppPreferences.FILTER_MODE_ALL) },
                    label = { Text("All Apps on Device") },
                    leadingIcon = {
                        if (filterMode == AppPreferences.FILTER_MODE_ALL) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("filter_all_apps_chip")
                )

                FilterChip(
                    selected = filterMode == AppPreferences.FILTER_MODE_SELECTED,
                    onClick = { onSetFilterMode(AppPreferences.FILTER_MODE_SELECTED) },
                    label = { Text("Specific Apps ($selectedCount)") },
                    leadingIcon = {
                        if (filterMode == AppPreferences.FILTER_MODE_SELECTED) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("filter_specific_apps_chip")
                )
            }

            if (filterMode == AppPreferences.FILTER_MODE_SELECTED) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenAppPicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choose_apps_button")
                ) {
                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select Apps to Trigger Hotspot ($selectedCount selected)")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ignore ongoing switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ignore Ongoing / Background Notifications",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Prevents download progress bars or music playback from repeatedly toggling hotspot",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = ignoreOngoing,
                    onCheckedChange = onToggleIgnoreOngoing,
                    modifier = Modifier.testTag("ignore_ongoing_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Auto-off duration
            Text(
                text = "Auto-Shutoff Timer",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Automatically stops hotspot after specified duration of activation",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val durations = listOf(0 to "Never", 5 to "5m", 10 to "10m", 15 to "15m", 30 to "30m", 60 to "1h")
                durations.forEach { (mins, label) ->
                    FilterChip(
                        selected = autoOffMinutes == mins,
                        onClick = { onSetAutoOffMinutes(mins) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cooldown period
            Text(
                text = "Trigger Cooldown Interval",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Prevents spamming when rapid notifications arrive in bursts",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val cooldowns = listOf(10 to "10s", 20 to "20s", 30 to "30s", 60 to "60s")
                cooldowns.forEach { (sec, label) ->
                    FilterChip(
                        selected = cooldownSeconds == sec,
                        onClick = { onSetCooldownSeconds(sec) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feedback: Notification & Vibration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Alert Notification on Trigger", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = notifyOnTrigger,
                    onCheckedChange = onToggleNotify,
                    modifier = Modifier.testTag("notify_on_trigger_switch")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Haptic Vibration on Trigger", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = vibrateOnTrigger,
                    onCheckedChange = onToggleVibrate,
                    modifier = Modifier.testTag("vibrate_on_trigger_switch")
                )
            }
        }
    }
}

@Composable
fun LogItemCard(log: TriggerLogEntity) {
    val formatter = remember { SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }

    LiquidGlassSquircleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SquircleSmall,
        accentGlow = if (log.wasHotspotTriggered) GradientCyan else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(SquircleSmall)
                    .background(
                        if (log.wasHotspotTriggered) {
                            Brush.linearGradient(
                                listOf(GradientCyan.copy(alpha = 0.25f), Color(0xFF00E676).copy(alpha = 0.15f))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.wasHotspotTriggered) Icons.Default.WifiTethering else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (log.wasHotspotTriggered) GradientCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.appName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (log.notificationTitle.isNotEmpty()) {
                    Text(
                        text = log.notificationTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = log.statusMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.wasHotspotTriggered) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyLogPlaceholder(onSimulate: () -> Unit) {
    LiquidGlassSquircleCard(
        modifier = Modifier.fillMaxWidth(),
        shape = SquircleCard,
        accentGlow = GradientElectricBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(SquircleMedium)
                    .background(
                        Brush.linearGradient(
                            listOf(GradientElectricBlue.copy(alpha = 0.25f), GradientCyan.copy(alpha = 0.15f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = GradientCyan,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No Notifications Intercepted Yet",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "When any app on this device receives a notification, it will be logged here and the hotspot will activate automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSimulate,
                modifier = Modifier.testTag("test_simulation_button")
            ) {
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Test Notification")
            }
        }
    }
}

@Composable
fun AppPickerModal(
    apps: List<AppItem>,
    onToggleApp: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Select Trigger Applications",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Hotspot will only turn on when notifications arrive from these apps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed apps…") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleApp(app.packageName) }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = app.isSelected,
                                onCheckedChange = { onToggleApp(app.packageName) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun InfoModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How NetVia Works", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "1. Notification Interception",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Android's NotificationListenerService inspects status bar notifications across any app on your device in real-time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "2. Automatic Wi-Fi Hotspot Ignition",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Upon notification arrival matching your rules, the app initiates Android's Local-Only Hotspot engine and displays your network credentials.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "3. Phone Reboot Resilience",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Registered via RECEIVE_BOOT_COMPLETED, the app automatically re-binds notification listeners and starts the monitoring foreground service whenever your phone restarts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Understood")
            }
        }
    )
}

@Composable
fun SimulateNotificationModal(
    onSimulate: (appName: String, title: String) -> Unit,
    onDismiss: () -> Unit
) {
    var appName by remember { mutableStateOf("WhatsApp") }
    var notificationTitle by remember { mutableStateOf("New message from Alex: Hey, where are you?") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Simulate Incoming Notification",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Test the hotspot trigger immediately by simulating a notification from any application:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("simulate_app_input")
                )

                OutlinedTextField(
                    value = notificationTitle,
                    onValueChange = { notificationTitle = it },
                    label = { Text("Notification Content") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("simulate_title_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSimulate(appName, notificationTitle) },
                modifier = Modifier.testTag("confirm_simulate_button")
            ) {
                Text("Trigger Hotspot")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
