package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.ArtCategory
import com.example.data.model.DeckSettings
import com.example.data.model.DefaultLocations
import com.example.data.model.LayoutMode
import com.example.data.model.LocationPreset
import com.example.ui.WidgetToggleType
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.data.manager.UsageStats
import com.example.ui.theme.VoidBlack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    settings: DeckSettings,
    usageStats: UsageStats = UsageStats(1, 0, 0),
    onClose: () -> Unit,
    onSelectLocation: (LocationPreset) -> Unit,
    onSelectCustomLocation: (String, Double, Double) -> Unit,
    onSelectLayoutMode: (LayoutMode) -> Unit,
    onSelectArtCategory: (ArtCategory) -> Unit,
    onToggleWidget: (WidgetToggleType) -> Unit,
    onToggleTempUnit: () -> Unit,
    onToggleSpeedUnit: () -> Unit,
    onToggleAltitudeUnit: () -> Unit,
    onToggleBurnInProtection: () -> Unit,
    onOpenRate: () -> Unit = {},
    onOpenShare: () -> Unit = {},
    onOpenUpdate: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    var customCityName by remember { mutableStateOf("") }
    var customLatStr by remember { mutableStateOf("") }
    var customLonStr by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

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
                .testTag("settings_dialog_scrim")
        ) {
            val isCompact = maxWidth < 460.dp
            val isSmallScreen = maxWidth < 540.dp
            val contentPadding = if (isCompact) 14.dp else 22.dp

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxHeight(if (maxHeight < 640.dp) 0.98f else 0.92f)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .testTag("settings_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Row
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_title),
                                    color = TextPrimary,
                                    fontSize = if (isCompact) 18.sp else 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = stringResource(R.string.settings_subtitle),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("settings_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // SECTION 1: Location & Sky Coordinates
                    SectionHeader(
                        icon = Icons.Default.LocationCity,
                        title = stringResource(R.string.sec_location_title)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DefaultLocations.ALL.forEach { city ->
                            val isSelected = settings.selectedCity.name == city.name
                            SelectableChip(
                                label = "${city.name}, ${city.country}",
                                isSelected = isSelected,
                                onClick = { onSelectLocation(city) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Coordinate toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showCustomInput = !showCustomInput }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showCustomInput) stringResource(R.string.hide_custom_gps) else stringResource(R.string.show_custom_gps),
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showCustomInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isCompact) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = customCityName,
                                    onValueChange = { customCityName = it },
                                    label = { Text(stringResource(R.string.city_location_name), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = customLatStr,
                                        onValueChange = { customLatStr = it },
                                        label = { Text(stringResource(R.string.latitude), fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                    OutlinedTextField(
                                        value = customLonStr,
                                        onValueChange = { customLonStr = it },
                                        label = { Text(stringResource(R.string.longitude), fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanAccent,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                }
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = customCityName,
                                    onValueChange = { customCityName = it },
                                    label = { Text(stringResource(R.string.city_location_name), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                OutlinedTextField(
                                    value = customLatStr,
                                    onValueChange = { customLatStr = it },
                                    label = { Text(stringResource(R.string.latitude), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.9f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                OutlinedTextField(
                                    value = customLonStr,
                                    onValueChange = { customLonStr = it },
                                    label = { Text(stringResource(R.string.longitude), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.9f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val lat = customLatStr.toDoubleOrNull()
                                val lon = customLonStr.toDoubleOrNull()
                                if (lat != null && lon != null && customCityName.isNotBlank()) {
                                    onSelectCustomLocation(customCityName, lat, lon)
                                    showCustomInput = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = VoidBlack),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.apply_custom_coords), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 2: Layout Preset
                    SectionHeader(
                        icon = Icons.Default.Dashboard,
                        title = stringResource(R.string.sec_layout_title)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSmallScreen) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LayoutMode.values().forEach { mode ->
                                val isSelected = settings.layoutMode == mode
                                val modeName = when (mode) {
                                    LayoutMode.CINEMATIC -> stringResource(R.string.layout_cinematic_name)
                                    LayoutMode.GRID -> stringResource(R.string.layout_grid_name)
                                    LayoutMode.ZEN -> stringResource(R.string.layout_zen_name)
                                }
                                val modeDesc = when (mode) {
                                    LayoutMode.CINEMATIC -> stringResource(R.string.layout_cinematic_desc)
                                    LayoutMode.GRID -> stringResource(R.string.layout_grid_desc)
                                    LayoutMode.ZEN -> stringResource(R.string.layout_zen_desc)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0x3300E5FF) else DarkSurfaceElevated)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) CyanAccent else DarkBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectLayoutMode(mode) }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = modeName,
                                            color = if (isSelected) CyanAccent else TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = modeDesc,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LayoutMode.values().forEach { mode ->
                                val isSelected = settings.layoutMode == mode
                                val modeName = when (mode) {
                                    LayoutMode.CINEMATIC -> stringResource(R.string.layout_cinematic_name)
                                    LayoutMode.GRID -> stringResource(R.string.layout_grid_name)
                                    LayoutMode.ZEN -> stringResource(R.string.layout_zen_name)
                                }
                                val modeDesc = when (mode) {
                                    LayoutMode.CINEMATIC -> stringResource(R.string.layout_cinematic_desc)
                                    LayoutMode.GRID -> stringResource(R.string.layout_grid_desc)
                                    LayoutMode.ZEN -> stringResource(R.string.layout_zen_desc)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0x3300E5FF) else DarkSurfaceElevated)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) CyanAccent else DarkBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectLayoutMode(mode) }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = modeName,
                                            color = if (isSelected) CyanAccent else TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = modeDesc,
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 3: Art & Ambient Canvas Theme
                    SectionHeader(
                        icon = Icons.Default.Landscape,
                        title = stringResource(R.string.sec_art_title)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ArtCategory.values().forEach { cat ->
                            val catName = when (cat) {
                                ArtCategory.SCENIC -> stringResource(R.string.art_scenic)
                                ArtCategory.CYBER_NIGHT -> stringResource(R.string.art_cyber)
                                ArtCategory.SPACE -> stringResource(R.string.art_space)
                                ArtCategory.ARCHITECTURE -> stringResource(R.string.art_architecture)
                                ArtCategory.FINE_ART -> stringResource(R.string.art_fine_art)
                            }
                            SelectableChip(
                                label = catName,
                                isSelected = settings.artCategory == cat,
                                onClick = { onSelectArtCategory(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 4: Measurement Units & Hardware
                    SectionHeader(
                        icon = Icons.Default.Tune,
                        title = stringResource(R.string.sec_units_title)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isCompact) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ToggleUnitBox(
                                title = stringResource(R.string.temperature),
                                value = if (settings.isCelsius) stringResource(R.string.celsius_label) else stringResource(R.string.fahrenheit_label),
                                onClick = onToggleTempUnit
                            )
                            ToggleUnitBox(
                                title = stringResource(R.string.wind_speed),
                                value = if (settings.isMph) stringResource(R.string.mph_label) else stringResource(R.string.kmh_label),
                                onClick = onToggleSpeedUnit
                            )
                            ToggleUnitBox(
                                title = stringResource(R.string.flight_altitude),
                                value = if (settings.isAltitudeFeet) stringResource(R.string.feet_label) else stringResource(R.string.meters_label),
                                onClick = onToggleAltitudeUnit
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ToggleUnitBox(
                                title = stringResource(R.string.temperature),
                                value = if (settings.isCelsius) stringResource(R.string.celsius_label) else stringResource(R.string.fahrenheit_label),
                                onClick = onToggleTempUnit,
                                modifier = Modifier.weight(1f)
                            )
                            ToggleUnitBox(
                                title = stringResource(R.string.wind_speed),
                                value = if (settings.isMph) stringResource(R.string.mph_label) else stringResource(R.string.kmh_label),
                                onClick = onToggleSpeedUnit,
                                modifier = Modifier.weight(1f)
                            )
                            ToggleUnitBox(
                                title = stringResource(R.string.flight_altitude),
                                value = if (settings.isAltitudeFeet) stringResource(R.string.feet_label) else stringResource(R.string.meters_label),
                                onClick = onToggleAltitudeUnit,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 5: Widget Visibility & OLED Protection
                    SectionHeader(
                        icon = Icons.Default.Visibility,
                        title = stringResource(R.string.sec_widgets_title)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WidgetToggleRow(stringResource(R.string.widget_weather_label), settings.showWeather) { onToggleWidget(WidgetToggleType.WEATHER) }
                        WidgetToggleRow(stringResource(R.string.widget_crypto_label), settings.showCrypto) { onToggleWidget(WidgetToggleType.CRYPTO) }
                        WidgetToggleRow(stringResource(R.string.widget_radar_label), settings.showRadar) { onToggleWidget(WidgetToggleType.RADAR) }
                        WidgetToggleRow(stringResource(R.string.widget_history_label), settings.showHistory) { onToggleWidget(WidgetToggleType.HISTORY) }
                        WidgetToggleRow(stringResource(R.string.widget_burnin_label), settings.burnInShiftEnabled) { onToggleBurnInProtection() }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SECTION 6: App Experience & Support
                    SectionHeader(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.sec_engagement_title)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Usage Telemetry Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .testTag("usage_telemetry_card")
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.usage_telemetry_title),
                                color = SunsetAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.usage_sessions, usageStats.sessions),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(R.string.usage_active_time, usageStats.activeTimeMinutes),
                                    color = CyanAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(R.string.usage_interactions, usageStats.interactionCount),
                                    color = EmeraldGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onOpenRate,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("settings_rate_button")
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_rate_app), color = TextPrimary, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenShare,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("settings_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_share_app), color = TextPrimary, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenUpdate,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("settings_update_button")
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_check_update), color = TextPrimary, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenPrivacyPolicy,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("settings_privacy_button")
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_privacy_policy), color = TextPrimary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Done Button
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = VoidBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_done_button")
                    ) {
                        Text(stringResource(R.string.apply_return_dashboard), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = SunsetAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SelectableChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0x3300E5FF) else DarkSurfaceElevated)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) CyanAccent else DarkBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (isSelected) CyanAccent else TextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ToggleUnitBox(title: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column {
            Text(text = title, color = TextMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun WidgetToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = TextPrimary, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanAccent,
                checkedTrackColor = Color(0xFF004D56),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkBorder
            )
        )
    }
}
