package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ScreensaverStyle
import com.example.data.model.WallpaperItem
import com.example.data.model.WeatherData
import com.example.data.model.getWeatherConditionRes
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay

@Composable
fun ScreensaverOverlay(
    style: ScreensaverStyle,
    currentWallpaper: WallpaperItem?,
    wallpapers: List<WallpaperItem>,
    currentTime: String,
    currentPeriod: String,
    currentDate: String,
    weather: WeatherData?,
    isCelsius: Boolean,
    onWake: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var activePhotoIndex by remember { mutableStateOf(0) }
    var positionCorner by remember { mutableStateOf(0) } // 0: Top-Left, 1: Top-Right, 2: Bottom-Right, 3: Bottom-Left

    // Auto-advance photo slideshow every 12 seconds in SLIDESHOW mode
    LaunchedEffect(style, wallpapers) {
        if (style == ScreensaverStyle.SLIDESHOW && wallpapers.isNotEmpty()) {
            while (true) {
                delay(12000L)
                activePhotoIndex = (activePhotoIndex + 1) % wallpapers.size
            }
        }
    }

    // Anti-burn-in position shift every 25 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(25000L)
            positionCorner = (positionCorner + 1) % 4
        }
    }

    val displayPhoto = if (wallpapers.isNotEmpty()) {
        wallpapers[activePhotoIndex.coerceIn(0, wallpapers.size - 1)]
    } else {
        currentWallpaper
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onWake
            )
            .testTag("screensaver_overlay_container")
    ) {
        if (style == ScreensaverStyle.SLIDESHOW && displayPhoto != null) {
            // Ambient Wallpaper Background with Crossfade
            Crossfade(
                targetState = displayPhoto.imageUrl,
                animationSpec = tween(durationMillis = 1500),
                label = "screensaver_crossfade"
            ) { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Cinematic Darkening Scrim (60% dimming)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
            )
        } else {
            // Ultra-Dim Night Clock Mode (85% dimming)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD9000000))
            )
        }

        // Floating Anti-Burn-In Clock & Weather Pod
        val alignment = when (positionCorner) {
            0 -> Alignment.TopStart
            1 -> Alignment.TopEnd
            2 -> Alignment.BottomEnd
            else -> Alignment.BottomStart
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = alignment
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x660B0E14))
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                // Time Display
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = currentTime,
                        color = TextPrimary,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp
                    )
                    if (currentPeriod.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentPeriod,
                            color = CyanAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                Text(
                    text = currentDate,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Weather Row
                weather?.let { w ->
                    Spacer(modifier = Modifier.height(10.dp))
                    val temp = if (isCelsius) w.currentTemp.toInt() else (w.currentTemp * 9 / 5 + 32).toInt()
                    val unit = if (isCelsius) "°C" else "°F"
                    val condName = stringResource(getWeatherConditionRes(w.weatherCode))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (w.isDay) Icons.Default.WbSunny else Icons.Default.Nightlight,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$temp$unit • $condName",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Photo Attribution Watermark in Slideshow Mode
                if (style == ScreensaverStyle.SLIDESHOW && displayPhoto != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayPhoto.title + " • " + displayPhoto.author,
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Bottom Wake Hint Indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x88000000))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.screensaver_dismiss_hint),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
