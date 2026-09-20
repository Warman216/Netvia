package com.example

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppPreferences
import com.example.service.BootReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
    @Test
    fun testPreferencesDefaultsAndUpdates() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = AppPreferences(context)

        // Default should be auto hotspot enabled
        assertTrue(prefs.isAutoHotspotEnabled)
        assertEquals(AppPreferences.FILTER_MODE_ALL, prefs.filterMode)

        // Update preference
        prefs.autoOffMinutes = 30
        assertEquals(30, prefs.autoOffMinutes)

        prefs.filterMode = AppPreferences.FILTER_MODE_SELECTED
        assertEquals(AppPreferences.FILTER_MODE_SELECTED, prefs.filterMode)
    }

    @Test
    fun testBootReceiverAcceptsBootCompletedAction() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val receiver = BootReceiver()
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        // Receiver should handle BOOT_COMPLETED without crashing
        receiver.onReceive(context, intent)
    }
}
