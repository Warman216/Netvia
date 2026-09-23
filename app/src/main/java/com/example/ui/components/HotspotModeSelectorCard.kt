package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.service.HotspotManager
import com.example.ui.theme.GradientCyan
import com.example.ui.theme.GradientElectricBlue
import com.example.ui.theme.GradientViolet

/**
 * Liquid Glass Card allowing the user to select between
 * Internet Sharing Hotspot (Mobile Tethering) and Local-Only P2P Hotspot
 */
@Composable
fun HotspotModeSelectorCard(
    currentMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isInternetSharing = currentMode == AppPreferences.HOTSPOT_MODE_INTERNET_SHARING

    LiquidGlassSquircleCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hotspot_mode_selector_card"),
        shape = SquircleCard,
        accentGlow = if (isInternetSharing) GradientCyan else GradientViolet
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = if (isInternetSharing) GradientCyan else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hotspot Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(SquircleSmall)
                        .background(
                            if (isInternetSharing) {
                                Brush.horizontalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.25f), Color(0xFF00E676).copy(alpha = 0.2f)))
                            } else {
                                Brush.horizontalGradient(listOf(GradientViolet.copy(alpha = 0.25f), GradientElectricBlue.copy(alpha = 0.2f)))
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isInternetSharing) "INTERNET SHARING" else "LOCAL P2P",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isInternetSharing) Color(0xFF00E5FF) else GradientViolet
                    )
                }
            }

            Text(
                text = "Choose whether connected devices receive full internet access via cellular data or connect purely locally",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 1: Internet Sharing Hotspot (Default / Primary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleMedium)
                    .background(
                        if (isInternetSharing) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.18f),
                                    Color(0xFF00E676).copy(alpha = 0.12f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            )
                        }
                    )
                    .border(
                        width = if (isInternetSharing) 1.5.dp else 1.dp,
                        brush = if (isInternetSharing) {
                            Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00E676)))
                        } else {
                            Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent))
                        },
                        shape = SquircleMedium
                    )
                    .clickable {
                        onModeSelected(AppPreferences.HOTSPOT_MODE_INTERNET_SHARING)
                    }
                    .padding(14.dp)
                    .testTag("mode_internet_sharing_option")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(SquircleSmall)
                            .background(
                                if (isInternetSharing) {
                                    Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF00B09B)))
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = if (isInternetSharing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Internet Sharing Hotspot",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECOMMENDED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Text(
                            text = "Routes mobile LTE/5G data to all devices (Laptops, iPads, TV, gaming, etc.) with full web access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (isInternetSharing) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isInternetSharing) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Local-Only Hotspot (P2P)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SquircleMedium)
                    .background(
                        if (!isInternetSharing) {
                            Brush.horizontalGradient(
                                listOf(
                                    GradientViolet.copy(alpha = 0.22f),
                                    GradientElectricBlue.copy(alpha = 0.15f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            )
                        }
                    )
                    .border(
                        width = if (!isInternetSharing) 1.5.dp else 1.dp,
                        brush = if (!isInternetSharing) {
                            Brush.horizontalGradient(listOf(GradientViolet, GradientElectricBlue))
                        } else {
                            Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent))
                        },
                        shape = SquircleMedium
                    )
                    .clickable {
                        onModeSelected(AppPreferences.HOTSPOT_MODE_LOCAL_ONLY)
                    }
                    .padding(14.dp)
                    .testTag("mode_local_only_option")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(SquircleSmall)
                            .background(
                                if (!isInternetSharing) {
                                    Brush.linearGradient(listOf(GradientViolet, GradientElectricBlue))
                                } else {
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = if (!isInternetSharing) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local-Only Hotspot (P2P)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Local device-to-device network without sharing mobile data. Good for offline transfers and local file sharing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (!isInternetSharing) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (!isInternetSharing) GradientViolet else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
