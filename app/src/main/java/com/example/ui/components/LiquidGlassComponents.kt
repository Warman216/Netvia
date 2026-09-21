package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard iOS squircle corner radii
 */
val SquircleLarge = RoundedCornerShape(24.dp)
val SquircleMedium = RoundedCornerShape(18.dp)
val SquircleSmall = RoundedCornerShape(12.dp)
val SquircleCard = RoundedCornerShape(22.dp)

/**
 * Apple-style Liquid Glass Squircle Container:
 * Features multi-layered translucent frosted gradients, subtle hairline specular rim highlights,
 * soft ambient drop shadows, and continuous squircle curvature.
 */
@Composable
fun LiquidGlassSquircleCard(
    modifier: Modifier = Modifier,
    shape: Shape = SquircleCard,
    tint: Color? = null,
    accentGlow: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.value.toLong() < 0xFF808080

    // Multi-layer specular glass gradients
    val glassFillBrush = if (isDark) {
        val baseTint = tint ?: Color(0xFF252529)
        Brush.verticalGradient(
            colors = listOf(
                baseTint.copy(alpha = 0.72f),
                baseTint.copy(alpha = 0.52f),
                Color(0xFF161618).copy(alpha = 0.85f)
            )
        )
    } else {
        val baseTint = tint ?: Color(0xFFFFFFFF)
        Brush.verticalGradient(
            colors = listOf(
                baseTint.copy(alpha = 0.88f),
                baseTint.copy(alpha = 0.68f),
                Color(0xFFF6F6FA).copy(alpha = 0.80f)
            )
        )
    }

    // Specular border sheen simulating light hitting rounded glass edge
    val rimBorderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                (accentGlow ?: Color.White).copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.02f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                (accentGlow ?: Color.White).copy(alpha = 0.45f),
                Color(0xFF000000).copy(alpha = 0.04f)
            )
        )
    }

    val shadowElevation: Dp = if (isDark) 6.dp else 4.dp
    val ambientShadowColor = if (isDark) Color(0x70000000) else Color(0x18000000)

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = ambientShadowColor,
                spotColor = ambientShadowColor
            )
            .clip(shape)
            .background(brush = glassFillBrush)
            .border(
                width = 1.dp,
                brush = rimBorderBrush,
                shape = shape
            ),
        content = content
    )
}
