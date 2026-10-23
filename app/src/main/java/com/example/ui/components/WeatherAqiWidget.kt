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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.WeatherData
import com.example.data.model.getWeatherConditionRes
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun WeatherAqiWidget(
    weather: WeatherData?,
    isCelsius: Boolean,
    isMph: Boolean,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassOverlay)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) CyanAccent else GlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onInspect() }
            .testTag("weather_aqi_widget_card")
    ) {
        val isCompact = maxWidth < 380.dp
        val padding = if (isCompact) 14.dp else 18.dp

        if (weather == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(padding)
            ) {
                Text(
                    text = stringResource(R.string.loading_weather),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            val conditionStr = stringResource(getWeatherConditionRes(weather.weatherCode))

            Column(modifier = Modifier.padding(padding)) {
                // Header line: Title + AQI Pill
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
                            imageVector = getWeatherIcon(weather.weatherCode, weather.isDay),
                            contentDescription = conditionStr,
                            tint = if (weather.isDay) SunsetAmber else CyanAccent,
                            modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.weather_aqi_title),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // AQI Quality Indicator Pill
                    val (aqiColor, aqiLabel) = getAqiLevel(weather.aqiUs)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(aqiColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(aqiColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.aqi_format, weather.aqiUs, aqiLabel),
                            color = aqiColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Main Temp & Condition Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        val displayTemp = if (isCelsius) weather.currentTemp else (weather.currentTemp * 9 / 5 + 32)
                        val unitStr = if (isCelsius) "°C" else "°F"

                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "%.0f".format(displayTemp),
                                color = TextPrimary,
                                fontSize = if (isCompact) 36.sp else 44.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("weather_temp_text")
                            )
                            Text(
                                text = unitStr,
                                color = CyanAccent,
                                fontSize = if (isCompact) 16.sp else 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                            )
                        }

                        Text(
                            text = conditionStr,
                            color = TextPrimary,
                            fontSize = if (isCompact) 13.sp else 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Key telemetry: Humidity, Wind, High/Low
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val displayWind = if (isMph) weather.windSpeed * 0.621371 else weather.windSpeed
                        val windUnit = if (isMph) stringResource(R.string.mph_label) else stringResource(R.string.kmh_label)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.humidity_format, weather.humidity), color = TextSecondary, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.wind_format, displayWind, windUnit), color = TextSecondary, fontSize = 11.sp)
                        }

                        val maxVal = if (isCelsius) weather.dailyMax else (weather.dailyMax * 9 / 5 + 32)
                        val minVal = if (isCelsius) weather.dailyMin else (weather.dailyMin * 9 / 5 + 32)
                        Text(
                            text = stringResource(R.string.temp_high_low, maxVal, minVal),
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pollutant mini tags with FlowRow so it never exceeds screen or card width
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PollutantChip(label = stringResource(R.string.pm25_label), value = "%.1f µg/m³".format(weather.pm25))
                    PollutantChip(label = stringResource(R.string.pm10_label), value = "%.1f µg/m³".format(weather.pm10))
                    PollutantChip(label = stringResource(R.string.eu_aqi_label), value = "${weather.aqiEu}")
                    Text(
                        text = stringResource(R.string.click_for_7day),
                        color = CyanAccent.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PollutantChip(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x22FFFFFF))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = "$label: ", color = TextSecondary, fontSize = 10.sp)
        Text(text = value, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun getWeatherIcon(weatherCode: Int, isDay: Boolean): ImageVector {
    return when (weatherCode) {
        0, 1 -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
        2, 3, 45, 48 -> Icons.Default.Cloud
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Default.WaterDrop
        else -> Icons.Default.WbSunny
    }
}

@Composable
private fun getAqiLevel(aqiUs: Int): Pair<Color, String> {
    return when {
        aqiUs <= 50 -> EmeraldGreen to stringResource(R.string.aqi_good)
        aqiUs <= 100 -> SunsetAmber to stringResource(R.string.aqi_moderate)
        aqiUs <= 150 -> Color(0xFFFF9100) to stringResource(R.string.aqi_sensitive)
        else -> CoralRed to stringResource(R.string.aqi_unhealthy)
    }
}
