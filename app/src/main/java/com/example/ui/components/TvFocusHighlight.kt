package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextPrimary

/**
 * Visual badge displayed on dashboard cards when focused via TV remote D-Pad navigation.
 * Features an animated pulsating beacon and localized action prompt.
 */
@Composable
fun TvFocusBadge(
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.tv_dpad_hint),
    accentColor: Color = CyanAccent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tv_beacon_pulse")
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_scale"
    )

    AnimatedVisibility(
        visible = isFocused,
        enter = fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.85f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.22f))
                .border(1.dp, accentColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("tv_focus_badge")
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .scale(beaconPulse)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}
