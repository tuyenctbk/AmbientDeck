package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.WallpaperItem
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VoidBlack

@Composable
fun AmbientBackdrop(
    wallpaper: WallpaperItem?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .testTag("ambient_backdrop_container")
    ) {
        // Crossfade wallpaper transition
        Crossfade(
            targetState = wallpaper?.imageUrl,
            animationSpec = tween(durationMillis = 1200),
            label = "wallpaper_crossfade"
        ) { url ->
            if (url != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = wallpaper?.title ?: stringResource(R.string.app_name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Subtle fallback procedural ambient dark gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF131D2D),
                                    Color(0xFF090D14),
                                    Color(0xFF04060A)
                                )
                            )
                        )
                )
            }
        }

        // Multi-tier dark scrims to ensure UI text readability from 10ft TV distance
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC070A0F), // Top bar dark scrim
                            Color(0x990A0E17), // Center ambient transparency
                            Color(0xF0070A0F)  // Bottom cards solid contrast scrim
                        )
                    )
                )
        )

        // Radial dark vignette around edges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x66000000),
                            Color(0xDD000000)
                        )
                    )
                )
        )

        // Wallpaper metadata watermark
        if (wallpaper != null) {
            Text(
                text = stringResource(R.string.ambient_art_by, wallpaper.title, wallpaper.author),
                color = TextMuted.copy(alpha = 0.7f),
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 12.dp)
                    .testTag("wallpaper_metadata_tag")
            )
        }
    }
}
