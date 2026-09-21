package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GradientCyan
import com.example.ui.theme.GradientElectricBlue
import com.example.ui.theme.GradientMagenta
import com.example.ui.theme.GradientViolet

/**
 * Standard iOS / macOS squircle corner radii
 */
val SquircleLarge = RoundedCornerShape(24.dp)
val SquircleMedium = RoundedCornerShape(18.dp)
val SquircleSmall = RoundedCornerShape(12.dp)
val SquircleCard = RoundedCornerShape(22.dp)

/**
 * Vibrant Animated Mesh Gradient Background Canvas:
 * Paints dynamic, fluid gradient orbs with soft blur-like blending
 * to give NetVia an energetic, luminous modern cyber-aesthetic.
 */
@Composable
fun VibrantMeshGradientBackground(
    modifier: Modifier = Modifier,
    isHotspotActive: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.value.toLong() < 0xFF808080
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_shift")

    val phaseX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phaseX"
    )

    val phaseY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phaseY"
    )

    val baseBgColor = if (isDark) Color(0xFF090A12) else Color(0xFFF3F5FC)

    Box(modifier = modifier.fillMaxSize().background(baseBgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Orb 1: Electric Blue / Cyan (Top Left / Moving diagonally)
            val orb1Center = Offset(
                x = w * (0.2f + 0.25f * phaseX),
                y = h * (0.12f + 0.15f * phaseY)
            )
            val orb1Radius = w * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isHotspotActive) {
                        listOf(
                            Color(0xFF00E5FF).copy(alpha = if (isDark) 0.38f else 0.28f),
                            Color(0xFF00B09B).copy(alpha = if (isDark) 0.25f else 0.16f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            GradientElectricBlue.copy(alpha = if (isDark) 0.35f else 0.24f),
                            GradientCyan.copy(alpha = if (isDark) 0.20f else 0.12f),
                            Color.Transparent
                        )
                    },
                    center = orb1Center,
                    radius = orb1Radius
                ),
                center = orb1Center,
                radius = orb1Radius
            )

            // Orb 2: Violet / Magenta (Center Right)
            val orb2Center = Offset(
                x = w * (0.85f - 0.2f * phaseY),
                y = h * (0.42f + 0.18f * phaseX)
            )
            val orb2Radius = w * 0.82f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GradientViolet.copy(alpha = if (isDark) 0.30f else 0.20f),
                        GradientMagenta.copy(alpha = if (isDark) 0.18f else 0.10f),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = orb2Radius
                ),
                center = orb2Center,
                radius = orb2Radius
            )

            // Orb 3: Emerald Glow (Bottom Left)
            val orb3Center = Offset(
                x = w * (0.15f + 0.3f * phaseY),
                y = h * (0.78f - 0.15f * phaseX)
            )
            val orb3Radius = w * 0.7f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isHotspotActive) {
                        listOf(
                            Color(0xFF00E676).copy(alpha = if (isDark) 0.35f else 0.25f),
                            Color(0xFF00B0FF).copy(alpha = if (isDark) 0.18f else 0.12f),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            Color(0xFF5856D6).copy(alpha = if (isDark) 0.26f else 0.16f),
                            Color(0xFF0066FF).copy(alpha = if (isDark) 0.14f else 0.08f),
                            Color.Transparent
                        )
                    },
                    center = orb3Center,
                    radius = orb3Radius
                ),
                center = orb3Center,
                radius = orb3Radius
            )
        }

        // Foreground content container
        content()
    }
}

/**
 * Liquid Glass Squircle Card with Gradient Accents & Specular Rim Sheen
 */
@Composable
fun LiquidGlassSquircleCard(
    modifier: Modifier = Modifier,
    shape: Shape = SquircleCard,
    tint: Color? = null,
    accentGlow: Color? = null,
    gradientFill: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.value.toLong() < 0xFF808080

    // Multi-layer specular glass gradients
    val glassFillBrush = gradientFill ?: if (isDark) {
        val baseTint = tint ?: Color(0xFF1E202B)
        Brush.verticalGradient(
            colors = listOf(
                baseTint.copy(alpha = 0.78f),
                baseTint.copy(alpha = 0.62f),
                Color(0xFF11121A).copy(alpha = 0.88f)
            )
        )
    } else {
        val baseTint = tint ?: Color(0xFFFFFFFF)
        Brush.verticalGradient(
            colors = listOf(
                baseTint.copy(alpha = 0.92f),
                baseTint.copy(alpha = 0.76f),
                Color(0xFFF1F3FA).copy(alpha = 0.85f)
            )
        )
    }

    // Specular border sheen simulating light hitting rounded glass edge
    val rimBorderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                (accentGlow ?: GradientCyan).copy(alpha = 0.40f),
                (accentGlow ?: GradientViolet).copy(alpha = 0.20f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                (accentGlow ?: GradientElectricBlue).copy(alpha = 0.50f),
                Color(0xFF0066FF).copy(alpha = 0.12f)
            )
        )
    }

    val shadowElevation: Dp = if (isDark) 8.dp else 6.dp
    val ambientShadowColor = if (isDark) Color(0x90000000) else Color(0x22003388)

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = ambientShadowColor,
                spotColor = (accentGlow ?: GradientElectricBlue).copy(alpha = 0.22f)
            )
            .clip(shape)
            .background(brush = glassFillBrush)
            .border(
                width = 1.2.dp,
                brush = rimBorderBrush,
                shape = shape
            ),
        content = content
    )
}
