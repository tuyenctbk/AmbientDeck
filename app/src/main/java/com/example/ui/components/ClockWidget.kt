package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DeckSettings
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ClockWidget(
    currentTime: String,
    currentSeconds: String,
    currentPeriod: String,
    currentDate: String,
    settings: DeckSettings,
    lastUpdatedTime: String?,
    isRefreshing: Boolean,
    onOpenSettings: () -> Unit,
    onManualRefresh: () -> Unit,
    isShort: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val focusScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "clock_focus_scale"
    )

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
            .scale(focusScale)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassOverlay)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) CyanAccent else GlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onOpenSettings() }
            .testTag("clock_widget_container")
    ) {
        val isCompact = maxWidth < 440.dp || isShort
        val isNarrow = maxWidth < 340.dp

        val timeFontSize = when {
            isShort -> 42.sp
            maxWidth < 320.dp -> 36.sp
            maxWidth < 360.dp -> 40.sp
            maxWidth < 440.dp -> 44.sp
            maxWidth < 640.dp -> 60.sp
            else -> 70.sp
        }
        val periodFontSize = if (isCompact) 13.sp else 18.sp
        val secondsFontSize = if (isCompact) 11.sp else 16.sp
        val dateFontSize = if (isCompact) 12.sp else 15.sp

        val lastUpdatedText = if (lastUpdatedTime != null) {
            stringResource(R.string.updated_at, lastUpdatedTime)
        } else {
            stringResource(R.string.just_now)
        }

        Column(
            modifier = Modifier.padding(
                horizontal = if (isShort) 14.dp else if (isCompact) 14.dp else 22.dp,
                vertical = if (isShort) 6.dp else if (isCompact) 8.dp else 16.dp
            )
        ) {
            // Top status line with location chip, layout tag, and action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isCompact) 4.dp else 6.dp)
            ) {
                // City / Coordinates pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3300E5FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("location_indicator_chip")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.location_icon),
                        tint = CyanAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.city_country_format, settings.selectedCity.name, settings.selectedCity.country),
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Layout preset tag (shown if space permits)
                if (!isNarrow) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(settings.layoutMode.titleRes).uppercase(),
                        color = SunsetAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFB300))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Actions row (desktop/tablet/wide screens only, since mobile has persistent dock)
                if (!isCompact) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Manual refresh button
                        IconButton(
                            onClick = onManualRefresh,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("clock_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh_data),
                                tint = if (isRefreshing) CyanAccent else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Settings quick trigger
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("clock_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = stringResource(R.string.deck_settings),
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Ambient Digital Clock
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = currentTime,
                    color = TextPrimary,
                    fontSize = timeFontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = if (isCompact) (-0.5).sp else (-1.5).sp,
                    modifier = Modifier.testTag("clock_time_display")
                )

                Column(
                    modifier = Modifier.padding(start = 6.dp, bottom = if (isCompact) 6.dp else 10.dp)
                ) {
                    Text(
                        text = currentPeriod,
                        color = CyanAccent,
                        fontSize = periodFontSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentSeconds,
                        color = TextSecondary,
                        fontSize = secondsFontSize,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Full Date and sync caption
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = currentDate,
                    color = TextSecondary,
                    fontSize = dateFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .testTag("clock_date_display")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = lastUpdatedText,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
