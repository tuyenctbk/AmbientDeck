package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2600)
        isVisible = false
        delay(400)
        onSplashFinished()
    }

    val transition = rememberInfiniteTransition(label = "splash_effects")
    val pulseScale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )
    val particleAngle by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_angle"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.EnterTransition.None,
        exit = fadeOut(animationSpec = tween(400)),
        modifier = modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
                .clickable {
                    isVisible = false
                    onSplashFinished()
                }
                .focusable()
                .testTag("animated_splash_screen")
        ) {
            val isCompactHeight = maxHeight < 480.dp
            val isCompactWidth = maxWidth < 380.dp
            val canvasSize = if (isCompactHeight) 220.dp else if (isCompactWidth) 260.dp else 340.dp
            val iconSize = if (isCompactHeight) 44.dp else 64.dp
            val titleSize = if (isCompactHeight || isCompactWidth) 22.sp else 28.sp

            // Background pulsing aura & holographic radar sweep canvas
            Canvas(modifier = Modifier.size(canvasSize)) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = size.width / 2

                // Deep ambient radial aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanAccent.copy(alpha = 0.28f), Color.Transparent),
                        center = center,
                        radius = baseRadius * pulseScale
                    ),
                    center = center,
                    radius = baseRadius * pulseScale
                )

                // Outer radar guide ring
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.22f),
                    center = center,
                    radius = baseRadius * 0.90f,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // Middle harmonic telemetry ring
                drawCircle(
                    color = EmeraldGreen.copy(alpha = 0.25f),
                    center = center,
                    radius = baseRadius * 0.65f * pulseScale,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Inner core ring
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.35f),
                    center = center,
                    radius = baseRadius * 0.40f,
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Rotating radar sweep beam
                val sweepRad = Math.toRadians(sweepAngle.toDouble())
                val beamEnd = Offset(
                    x = (center.x + (baseRadius * 0.88f) * cos(sweepRad)).toFloat(),
                    y = (center.y + (baseRadius * 0.88f) * sin(sweepRad)).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(CyanAccent.copy(alpha = 0.7f), Color.Transparent),
                        start = center,
                        end = beamEnd
                    ),
                    start = center,
                    end = beamEnd,
                    strokeWidth = 2.dp.toPx()
                )

                // Orbiting satellite blips
                val pRad1 = Math.toRadians(particleAngle.toDouble())
                val p1 = Offset(
                    x = (center.x + (baseRadius * 0.65f * pulseScale) * cos(pRad1)).toFloat(),
                    y = (center.y + (baseRadius * 0.65f * pulseScale) * sin(pRad1)).toFloat()
                )
                drawCircle(
                    color = CyanAccent,
                    radius = 3.5.dp.toPx(),
                    center = p1
                )
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.35f),
                    radius = 7.dp.toPx(),
                    center = p1
                )

                val pRad2 = Math.toRadians((particleAngle + 180.0))
                val p2 = Offset(
                    x = (center.x + (baseRadius * 0.90f) * cos(pRad2)).toFloat(),
                    y = (center.y + (baseRadius * 0.90f) * sin(pRad2)).toFloat()
                )
                drawCircle(
                    color = SunsetAmber,
                    radius = 3.dp.toPx(),
                    center = p2
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(iconSize)
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 10.dp else 18.dp))

                Text(
                    text = stringResource(R.string.splash_title).uppercase(),
                    color = TextPrimary,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = if (isCompactWidth) 2.sp else 4.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))

                Text(
                    text = stringResource(R.string.splash_subtitle).uppercase(),
                    color = SunsetAmber,
                    fontSize = if (isCompactHeight) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 14.dp))

                Text(
                    text = stringResource(R.string.splash_loading),
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
