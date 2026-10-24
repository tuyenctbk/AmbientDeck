package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlightState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlightRadarWidget(
    flights: List<FlightState>,
    centerLat: Double,
    centerLon: Double,
    isAltitudeFeet: Boolean,
    isKnots: Boolean,
    onInspect: () -> Unit,
    isShort: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Infinite radar sweep angle & sonar ping
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val sonarPing by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar_ping"
    )

    var isFocused by remember { mutableStateOf(false) }

    val focusScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "radar_focus_scale"
    )

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
            .scale(focusScale)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassOverlay)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) EmeraldGreen else GlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onInspect() }
            .testTag("flight_radar_widget_card")
    ) {
        val isCompact = maxWidth < 440.dp || isShort
        val isNarrow = maxWidth < 340.dp
        val padding = if (isShort) 6.dp else if (isCompact) 8.dp else 18.dp
        val radarScopeSize = when {
            isShort -> 68.dp
            isNarrow -> 70.dp
            isCompact -> 74.dp
            else -> 140.dp
        }

        Column(modifier = Modifier.padding(padding)) {
            // Widget Title & Live Count Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = stringResource(R.string.radar_icon_desc),
                        tint = EmeraldGreen,
                        modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.radar_title).uppercase(),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.aircraft_active_format, flights.size),
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isShort || isCompact) 4.dp else 12.dp))

            // Radar Scope Canvas + Nearest Flight Info Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Radar Scope Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(radarScopeSize)
                        .clip(CircleShape)
                        .background(Color(0xFF091017))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), CircleShape)
                        .testTag("radar_scope_canvas")
                ) {
                    Canvas(modifier = Modifier.size(radarScopeSize - 4.dp)) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2

                        // Concentric Range Rings
                        drawCircle(
                            color = EmeraldGreen.copy(alpha = 0.15f),
                            radius = radius * 0.33f,
                            center = center,
                            style = Stroke(width = 1f)
                        )
                        drawCircle(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            radius = radius * 0.66f,
                            center = center,
                            style = Stroke(width = 1f)
                        )
                        drawCircle(
                            color = EmeraldGreen.copy(alpha = 0.3f),
                            radius = radius * 0.95f,
                            center = center,
                            style = Stroke(width = 1f)
                        )

                        // Crosshairs (North, South, East, West)
                        drawLine(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            start = Offset(center.x, 0f),
                            end = Offset(center.x, size.height),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            start = Offset(0f, center.y),
                            end = Offset(size.width, center.y),
                            strokeWidth = 1f
                        )

                        // Rotating Radar Beam / Sweep Line
                        val sweepRad = Math.toRadians(sweepAngle.toDouble())
                        val endX = center.x + radius * cos(sweepRad).toFloat()
                        val endY = center.y + radius * sin(sweepRad).toFloat()
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, EmeraldGreen),
                                start = center,
                                end = Offset(endX, endY)
                            ),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2f
                        )

                        // Center Ground Station Dot & expanding sonar ring
                        drawCircle(
                            color = EmeraldGreen.copy(alpha = (1f - sonarPing) * 0.4f),
                            radius = radius * 0.9f * sonarPing,
                            center = center,
                            style = Stroke(width = 1.2f)
                        )
                        drawCircle(
                            color = CyanAccent,
                            radius = 3.5f,
                            center = center
                        )

                        // Plot Live Aircraft
                        val maxDelta = 1.3
                        flights.take(8).forEach { flight ->
                            val dx = (((flight.longitude - centerLon) / maxDelta) * (radius * 0.85f)).toFloat()
                            val dy = (-((flight.latitude - centerLat) / maxDelta) * (radius * 0.85f)).toFloat()
                            val planeX = (center.x + dx).coerceIn(4f, size.width - 4f)
                            val planeY = (center.y + dy).coerceIn(4f, size.height - 4f)

                            // Aircraft aura and dot blip
                            drawCircle(
                                color = EmeraldGreen.copy(alpha = 0.35f),
                                radius = 6f,
                                center = Offset(planeX, planeY)
                            )
                            drawCircle(
                                color = EmeraldGreen,
                                radius = 3f,
                                center = Offset(planeX, planeY)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(if (isCompact) 10.dp else 16.dp))

                // Nearest Flight Details Spotlight
                val nearest = flights.firstOrNull()
                Column(modifier = Modifier.weight(1f)) {
                    if (nearest != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = stringResource(R.string.flight_icon_desc),
                                tint = CyanAccent,
                                modifier = Modifier
                                    .size(15.dp)
                                    .rotate(nearest.headingDegrees.toFloat())
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = nearest.callsign,
                                color = TextPrimary,
                                fontSize = if (isCompact) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("nearest_flight_callsign")
                            )
                        }

                        val countryText = if (nearest.originCountry.isBlank() || nearest.originCountry.equals("Unknown", ignoreCase = true)) {
                            stringResource(R.string.unknown_country)
                        } else {
                            nearest.originCountry
                        }

                        Text(
                            text = countryText,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Altitude, Velocity, Heading using FlowRow
                        val altText = if (isAltitudeFeet) {
                            stringResource(R.string.altitude_feet_format, nearest.altitudeFeet)
                        } else {
                            stringResource(R.string.altitude_meters_format, nearest.altitudeMeters)
                        }
                        val speedText = if (isKnots) {
                            stringResource(R.string.speed_knots_format, nearest.velocityKnots)
                        } else {
                            stringResource(R.string.speed_kmh_format, nearest.velocityKmh)
                        }
                        val headingText = stringResource(R.string.heading_degrees_format, nearest.headingDegrees)

                        androidx.compose.foundation.layout.FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Column {
                                Text(text = stringResource(R.string.altitude_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = altText, color = TextPrimary, fontSize = if (isCompact) 11.sp else 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(text = stringResource(R.string.groundspeed_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = speedText, color = TextPrimary, fontSize = if (isCompact) 11.sp else 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(text = stringResource(R.string.heading_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = headingText, color = CyanAccent, fontSize = if (isCompact) 11.sp else 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.radar_scanning),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.click_inspect_flights),
                        color = EmeraldGreen.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
