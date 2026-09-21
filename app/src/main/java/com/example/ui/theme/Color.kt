package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Apple iOS System Palette (San Francisco / Cupertino Design System)
// System Accents
val AppleBlue = Color(0xFF007AFF)
val AppleBlueDark = Color(0xFF0A84FF)
val AppleGreen = Color(0xFF34C759)
val AppleGreenDark = Color(0xFF30D158)
val AppleIndigo = Color(0xFF5856D6)
val AppleOrange = Color(0xFFFF9500)
val AppleRed = Color(0xFFFF3B30)
val AppleRedDark = Color(0xFFFF453A)
val AppleTeal = Color(0xFF5AC8FA)

// iOS Backgrounds & Surfaces (Light mode)
val AppleSystemGroupedBackgroundLight = Color(0xFFF2F2F7)
val AppleSystemBackgroundLight = Color(0xFFFFFFFF)
val AppleSecondarySystemGroupedBackgroundLight = Color(0xFFFFFFFF)
val AppleLabelLight = Color(0xFF000000)
val AppleSecondaryLabelLight = Color(0xFF6C6C70)
val AppleTertiaryLabelLight = Color(0xFF8E8E93)
val AppleSeparatorLight = Color(0xFFE5E5EA)
val AppleFillLight = Color(0xFF787880).copy(alpha = 0.16f)

// iOS Backgrounds & Surfaces (Dark mode)
val AppleSystemGroupedBackgroundDark = Color(0xFF000000)
val AppleSystemBackgroundDark = Color(0xFF1C1C1E)
val AppleSecondarySystemGroupedBackgroundDark = Color(0xFF2C2C2E)
val AppleLabelDark = Color(0xFFFFFFFF)
val AppleSecondaryLabelDark = Color(0xFFEBEBF5).copy(alpha = 0.60f)
val AppleTertiaryLabelDark = Color(0xFFEBEBF5).copy(alpha = 0.30f)
val AppleSeparatorDark = Color(0xFF38383A)
val AppleFillDark = Color(0xFF787880).copy(alpha = 0.32f)

// Compatibility aliases for active states
val EmeraldActive = AppleGreen
val RoseError = AppleRed

