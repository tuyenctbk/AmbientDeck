package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radar
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

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1600)
        isVisible = false
        delay(400)
        onSplashFinished()
    }

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(400)),
        modifier = modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
                .testTag("animated_splash_screen")
        ) {
            val isCompactHeight = maxHeight < 480.dp
            val isCompactWidth = maxWidth < 380.dp
            val canvasSize = if (isCompactHeight) 180.dp else if (isCompactWidth) 220.dp else 280.dp
            val iconSize = if (isCompactHeight) 42.dp else 60.dp
            val titleSize = if (isCompactHeight || isCompactWidth) 22.sp else 28.sp

            // Background pulsing aura canvas
            Canvas(modifier = Modifier.size(canvasSize)) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanAccent.copy(alpha = 0.2f), Color.Transparent),
                        center = center,
                        radius = (size.width / 2) * pulseScale
                    ),
                    center = center,
                    radius = (size.width / 2) * pulseScale
                )
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.25f),
                    center = center,
                    radius = (size.width * 0.38f) * pulseScale,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = EmeraldGreen.copy(alpha = 0.2f),
                    center = center,
                    radius = (size.width * 0.48f) * pulseScale,
                    style = Stroke(width = 1.dp.toPx())
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

                Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 16.dp))

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
