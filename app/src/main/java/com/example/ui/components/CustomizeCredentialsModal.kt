package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.service.HotspotManager
import kotlin.random.Random

/**
 * Cupertino Squircle modal dialog for customizing Hotspot SSID and Password
 */
@Composable
fun CustomizeCredentialsModal(
    initialSsid: String,
    initialPassword: String,
    onSave: (ssid: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var ssid by remember(initialSsid) { mutableStateOf(initialSsid) }
    var password by remember(initialPassword) { mutableStateOf(initialPassword) }
    var showPassword by remember { mutableStateOf(false) }

    val isSsidValid = ssid.trim().isNotEmpty()
    val isPasswordValid = password.length >= 8

    fun generateRandomPassword() {
        val chars = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val randomStr = (1..10)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
        password = "netvia-$randomStr"
        showPassword = true
        Toast.makeText(context, "Generated strong password", Toast.LENGTH_SHORT).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = SquircleLarge,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Configure Hotspot Credentials",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Set your desired Wi-Fi network name and security password for Internet Sharing and auto-triggered hotspot.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // SSID Field
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("Network Name (SSID)") },
                    placeholder = { Text("e.g. NetVia-Hotspot") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    isError = !isSsidValid,
                    supportingText = {
                        if (!isSsidValid) {
                            Text("SSID cannot be empty", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Name broadcasted to connecting devices")
                        }
                    },
                    shape = SquircleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_ssid_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Hotspot Password") },
                    placeholder = { Text("Minimum 8 characters") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { generateRandomPassword() }) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Generate strong password",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = !isPasswordValid,
                    supportingText = {
                        if (!isPasswordValid) {
                            Text(
                                "Password too short: ${password.length}/8 characters (WPA2 requires >= 8)",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("${password.length} characters (Valid WPA2/WPA3 passphrase)")
                        }
                    },
                    shape = SquircleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_password_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isSsidValid && isPasswordValid) {
                                onSave(ssid.trim(), password)
                            }
                        }
                    )
                )

                // Quick action: Save & Configure in Android System Settings
                FilledTonalButton(
                    onClick = {
                        if (isSsidValid && isPasswordValid) {
                            onSave(ssid.trim(), password)
                            try {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Hotspot Password", password)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(
                                    context,
                                    "Password copied! Paste into System Hotspot Settings.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (_: Exception) {}
                            HotspotManager.openTetheringSettings(context)
                        }
                    },
                    enabled = isSsidValid && isPasswordValid,
                    shape = SquircleMedium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy & Configure in Android Hotspot")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your password is saved directly in NetVia and applied across all hotspot sessions.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSsidValid && isPasswordValid) {
                        onSave(ssid.trim(), password)
                    }
                },
                enabled = isSsidValid && isPasswordValid,
                shape = SquircleMedium,
                modifier = Modifier.testTag("save_credentials_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Credentials")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = SquircleMedium,
                modifier = Modifier.testTag("cancel_credentials_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
