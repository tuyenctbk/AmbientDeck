package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.CryptoTicker
import com.example.data.model.FlightState
import com.example.data.model.HistoryItem
import com.example.data.model.WeatherData
import com.example.ui.InspectWidgetType
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun WidgetDetailDialog(
    widgetType: InspectWidgetType,
    weather: WeatherData?,
    flights: List<FlightState>,
    tickers: List<CryptoTicker>,
    historyItems: List<HistoryItem>,
    isCelsius: Boolean,
    isMph: Boolean,
    isAltitudeFeet: Boolean,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color(0xCC000000))
                .padding(10.dp)
                .testTag("widget_detail_dialog_scrim")
        ) {
            val isCompact = maxWidth < 460.dp
            val contentPadding = if (isCompact) 14.dp else 22.dp

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxHeight(if (maxHeight < 640.dp) 0.98f else 0.90f)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .testTag("widget_detail_card")
            ) {
                Column(modifier = Modifier.padding(contentPadding)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (widgetType) {
                                InspectWidgetType.WEATHER -> stringResource(R.string.weather_detail_title)
                                InspectWidgetType.RADAR -> stringResource(R.string.radar_detail_title, flights.size)
                                InspectWidgetType.CRYPTO -> stringResource(R.string.crypto_detail_title)
                                InspectWidgetType.HISTORY -> stringResource(R.string.history_detail_title)
                            },
                            color = CyanAccent,
                            fontSize = if (isCompact) 13.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("detail_dialog_close")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (widgetType) {
                        InspectWidgetType.WEATHER -> WeatherDetailView(weather, isCelsius, isCompact)
                        InspectWidgetType.RADAR -> RadarDetailView(flights, isAltitudeFeet, isMph, isCompact)
                        InspectWidgetType.CRYPTO -> CryptoDetailView(tickers)
                        InspectWidgetType.HISTORY -> HistoryDetailView(historyItems)
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WeatherDetailView(weather: WeatherData?, isCelsius: Boolean, isCompact: Boolean) {
    if (weather == null) return

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            // Air Quality Deep Dive Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(text = stringResource(R.string.air_quality_telemetry), color = SunsetAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = stringResource(R.string.us_aqi), color = TextMuted, fontSize = 10.sp)
                            Text(text = "${weather.aqiUs}", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = stringResource(R.string.eu_aqi), color = TextMuted, fontSize = 10.sp)
                            Text(text = "${weather.aqiEu}", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = stringResource(R.string.pm25_particles), color = TextMuted, fontSize = 10.sp)
                            Text(text = "%.1f µg/m³".format(weather.pm25), color = CyanAccent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = stringResource(R.string.pm10_particles), color = TextMuted, fontSize = 10.sp)
                            Text(text = "%.1f µg/m³".format(weather.pm10), color = CyanAccent, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(text = stringResource(R.string.outlook_7day), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        items(weather.forecastDays) { day ->
            val maxT = if (isCelsius) day.maxTemp else (day.maxTemp * 9 / 5 + 32)
            val minT = if (isCelsius) day.minTemp else (day.minTemp * 9 / 5 + 32)
            val unitStr = if (isCelsius) "°C" else "°F"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = day.dayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(55.dp))
                Text(text = day.conditionText, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "%.0f%s".format(minT, unitStr), color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "%.0f%s".format(maxT, unitStr), color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun RadarDetailView(flights: List<FlightState>, isAltitudeFeet: Boolean, isMph: Boolean, isCompact: Boolean) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(flights) { flight ->
            val altStr = if (isAltitudeFeet) "%.0f ft".format(flight.altitudeFeet) else "%.0f m".format(flight.altitudeMeters)
            val speedStr = if (isMph) "%.0f mph".format(flight.velocityKmh * 0.621371) else "%.0f kts".format(flight.velocityKnots)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                if (isCompact) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Flight, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = flight.callsign, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(text = "${flight.originCountry} • ${flight.icao24}", color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                        ) {
                            Column {
                                Text(text = stringResource(R.string.altitude_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = altStr, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(text = stringResource(R.string.speed_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = speedStr, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text(text = stringResource(R.string.heading_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = "%.0f°".format(flight.headingDegrees), color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(Icons.Default.Flight, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = flight.callsign, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(text = "${flight.originCountry} • ICAO: ${flight.icao24}", color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = stringResource(R.string.altitude_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = altStr, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = stringResource(R.string.speed_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = speedStr, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = stringResource(R.string.heading_label), color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(text = "%.0f°".format(flight.headingDegrees), color = CyanAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CryptoDetailView(tickers: List<CryptoTicker>) {
    val formatter = DecimalFormat("#,##0.00")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tickers) { ticker ->
            val isPos = ticker.changePercent24h >= 0
            val color = if (isPos) EmeraldGreen else CoralRed
            val priceStr = if (ticker.priceUsd < 1.0) "%.4f".format(ticker.priceUsd) else formatter.format(ticker.priceUsd)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(text = "${ticker.symbol} / USDT", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = ticker.name, color = TextMuted, fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "$$priceStr", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(text = "%+.2f%% (24h)".format(ticker.changePercent24h), color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailView(items: List<HistoryItem>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = item.year,
                    color = SunsetAmber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(60.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.category.uppercase(), color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.text, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}
