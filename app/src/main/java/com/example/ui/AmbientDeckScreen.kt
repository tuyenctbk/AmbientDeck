package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.manager.PromptType
import com.example.data.model.LayoutMode
import com.example.ui.components.AmbientBackdrop
import com.example.ui.components.ClockWidget
import com.example.ui.components.CryptoTickerWidget
import com.example.ui.components.EngagementDialog
import com.example.ui.components.FlightRadarWidget
import com.example.ui.components.HistoryWidget
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SplashScreen
import com.example.ui.components.WeatherAqiWidget
import com.example.ui.components.WidgetDetailDialog
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun AmbientDeckScreen(
    viewModel: AmbientDeckViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoidBlack)
            .testTag("ambient_deck_screen_root")
    ) {
        // 1. Ambient 4K Artwork Canvas
        AmbientBackdrop(wallpaper = uiState.currentWallpaper)

        // 2. Responsive Dashboard with Burn-In Mitigation Offset Layer
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = uiState.pixelShiftX.dp, y = uiState.pixelShiftY.dp)
                .safeDrawingPadding()
        ) {
            val isLandscapePhone = maxWidth >= 580.dp && maxHeight < 500.dp
            val isWide = maxWidth >= 600.dp || isLandscapePhone
            val isCompactScreen = maxWidth < 440.dp
            val isShortScreen = maxHeight < 620.dp
            val screenHorizontalPadding = if (maxWidth < 420.dp) 12.dp else if (maxWidth < 600.dp) 16.dp else 24.dp
            val screenVerticalPadding = if (isShortScreen) 6.dp else if (isCompactScreen) 10.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = screenHorizontalPadding, vertical = screenVerticalPadding)
            ) {
                // Main widget canvas area - cards scroll cleanly above the dock
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = uiState.settings.layoutMode,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(320)) + scaleIn(initialScale = 0.97f, animationSpec = tween(320)))
                                .togetherWith(fadeOut(animationSpec = tween(200)))
                        },
                        label = "layout_mode_transition",
                        modifier = Modifier.fillMaxSize()
                    ) { targetMode ->
                        when (targetMode) {
                            LayoutMode.CINEMATIC -> CinematicLayout(uiState, viewModel, isWide, isShortScreen)
                            LayoutMode.GRID -> GridLayout(uiState, viewModel, isWide, isShortScreen)
                            LayoutMode.ZEN -> ZenMinimalLayout(uiState, viewModel)
                        }
                    }
                }

                // Dedicated Bottom Control Bar & Wallpaper Attribution Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isShortScreen) 4.dp else 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wallpaper metadata watermark
                    val wallpaper = uiState.currentWallpaper
                    if (wallpaper != null) {
                        Text(
                            text = stringResource(
                                R.string.ambient_art_by,
                                wallpaper.title,
                                wallpaper.author
                            ),
                            color = TextMuted.copy(alpha = 0.85f),
                            fontSize = if (isShortScreen || isCompactScreen) 10.sp else 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .testTag("wallpaper_metadata_tag")
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    FloatingControlBar(
                        isRefreshing = uiState.isRefreshing,
                        isCompact = isCompactScreen || isShortScreen,
                        onCycleWallpaper = { viewModel.cycleWallpaper() },
                        onCycleLayout = {
                            val nextMode = when (uiState.settings.layoutMode) {
                                LayoutMode.CINEMATIC -> LayoutMode.GRID
                                LayoutMode.GRID -> LayoutMode.ZEN
                                LayoutMode.ZEN -> LayoutMode.CINEMATIC
                            }
                            viewModel.onSelectLayoutMode(nextMode)
                        },
                        onOpenSettings = { viewModel.onToggleSettings(true) },
                        onRefresh = { viewModel.refreshAllData() }
                    )
                }
            }
        }

        // 3. Settings Modal Sheet
        if (uiState.isSettingsOpen) {
            SettingsDialog(
                settings = uiState.settings,
                usageStats = uiState.usageStats,
                onClose = { viewModel.onToggleSettings(false) },
                onSelectLocation = { viewModel.onSelectLocation(it) },
                onSelectCustomLocation = { name, lat, lon -> viewModel.onSelectCustomLocation(name, lat, lon) },
                onSelectLayoutMode = { viewModel.onSelectLayoutMode(it) },
                onSelectArtCategory = { viewModel.onSelectArtCategory(it) },
                onToggleWidget = { viewModel.onToggleWidget(it) },
                onToggleTempUnit = { viewModel.onToggleTempUnit() },
                onToggleSpeedUnit = { viewModel.onToggleSpeedUnit() },
                onToggleAltitudeUnit = { viewModel.onToggleAltitudeUnit() },
                onToggleBurnInProtection = { viewModel.onToggleBurnInProtection() },
                onOpenRate = { viewModel.onOpenPrompt(PromptType.RATE) },
                onOpenShare = { viewModel.onOpenPrompt(PromptType.SHARE) },
                onOpenUpdate = { viewModel.onOpenPrompt(PromptType.UPDATE) },
                onOpenPrivacyPolicy = { viewModel.onTogglePrivacyPolicy(true) }
            )
        }

        // 4. Widget Inspect Modal
        uiState.inspectedWidget?.let { inspectType ->
            WidgetDetailDialog(
                widgetType = inspectType,
                weather = uiState.weather,
                flights = uiState.flights,
                tickers = uiState.cryptoTickers,
                historyItems = uiState.historyItems,
                isCelsius = uiState.settings.isCelsius,
                isMph = uiState.settings.isMph,
                isAltitudeFeet = uiState.settings.isAltitudeFeet,
                onClose = { viewModel.onInspectWidget(null) }
            )
        }

        // 5. Smart Engagement Dialog (Update, Rate, Share)
        uiState.pendingPrompt?.let { promptType ->
            EngagementDialog(
                promptType = promptType,
                onRateApp = { viewModel.onRateApp() },
                onRateLater = { viewModel.onRateLater() },
                onRateNever = { viewModel.onRateNever() },
                onShareApp = { viewModel.onShareApp() },
                onDismissShare = { viewModel.onDismissShare() },
                onUpdateApp = { viewModel.onUpdateApp() },
                onDismissUpdate = { viewModel.onDismissUpdate() }
            )
        }

        // 6. In-App Privacy Policy Dialog
        if (uiState.showPrivacyPolicy) {
            PrivacyPolicyDialog(
                onClose = { viewModel.onTogglePrivacyPolicy(false) }
            )
        }

        // 7. Initial Animated Splash Screen
        AnimatedVisibility(
            visible = uiState.showSplash,
            enter = androidx.compose.animation.EnterTransition.None,
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(400)),
            modifier = Modifier.fillMaxSize()
        ) {
            SplashScreen(
                onSplashFinished = { viewModel.onDismissSplash() }
            )
        }
    }
}

@Composable
private fun CinematicLayout(
    uiState: DeckUiState,
    viewModel: AmbientDeckViewModel,
    isWide: Boolean,
    isShort: Boolean
) {
    val rowSpacing = if (isShort) 6.dp else if (!isWide) 8.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (isWide) {
            // Wide Screen: Side-by-side Clock & Weather
            Row(
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.settings.showClock) {
                    ClockWidget(
                        currentTime = uiState.currentTime,
                        currentSeconds = uiState.currentSeconds,
                        currentPeriod = uiState.currentPeriod,
                        currentDate = uiState.currentDate,
                        settings = uiState.settings,
                        lastUpdatedTime = uiState.lastUpdatedTime,
                        isRefreshing = uiState.isRefreshing,
                        onOpenSettings = { viewModel.onToggleSettings(true) },
                        onManualRefresh = { viewModel.refreshAllData() },
                        isShort = isShort,
                        modifier = Modifier.weight(1.15f)
                    )
                }

                if (uiState.settings.showWeather) {
                    WeatherAqiWidget(
                        weather = uiState.weather,
                        isCelsius = uiState.settings.isCelsius,
                        isMph = uiState.settings.isMph,
                        onInspect = { viewModel.onInspectWidget(InspectWidgetType.WEATHER) },
                        isShort = isShort,
                        modifier = Modifier.weight(1.25f)
                    )
                }
            }
        } else {
            // Compact / Vertical: Stacked
            if (uiState.settings.showClock) {
                ClockWidget(
                    currentTime = uiState.currentTime,
                    currentSeconds = uiState.currentSeconds,
                    currentPeriod = uiState.currentPeriod,
                    currentDate = uiState.currentDate,
                    settings = uiState.settings,
                    lastUpdatedTime = uiState.lastUpdatedTime,
                    isRefreshing = uiState.isRefreshing,
                    onOpenSettings = { viewModel.onToggleSettings(true) },
                    onManualRefresh = { viewModel.refreshAllData() },
                    isShort = isShort,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(rowSpacing))
            }

            if (uiState.settings.showWeather) {
                WeatherAqiWidget(
                    weather = uiState.weather,
                    isCelsius = uiState.settings.isCelsius,
                    isMph = uiState.settings.isMph,
                    onInspect = { viewModel.onInspectWidget(InspectWidgetType.WEATHER) },
                    isShort = isShort,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(rowSpacing))

        // Middle Row: Crypto Tickers full width
        if (uiState.settings.showCrypto && uiState.cryptoTickers.isNotEmpty()) {
            CryptoTickerWidget(
                tickers = uiState.cryptoTickers,
                onInspect = { viewModel.onInspectWidget(InspectWidgetType.CRYPTO) },
                isShort = isShort,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(rowSpacing))
        }

        // Bottom Row: Overhead Radar & Today in History
        if (isWide) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.settings.showRadar) {
                    FlightRadarWidget(
                        flights = uiState.flights,
                        centerLat = uiState.settings.selectedCity.latitude,
                        centerLon = uiState.settings.selectedCity.longitude,
                        isAltitudeFeet = uiState.settings.isAltitudeFeet,
                        isKnots = !uiState.settings.isMph,
                        onInspect = { viewModel.onInspectWidget(InspectWidgetType.RADAR) },
                        isShort = isShort,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                if (uiState.settings.showHistory) {
                    HistoryWidget(
                        historyItems = uiState.historyItems,
                        activeIndex = uiState.activeHistoryIndex,
                        onNext = { viewModel.nextHistoryItem() },
                        onPrev = { viewModel.prevHistoryItem() },
                        onInspect = { viewModel.onInspectWidget(InspectWidgetType.HISTORY) },
                        isShort = isShort,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }
        } else {
            if (uiState.settings.showRadar) {
                FlightRadarWidget(
                    flights = uiState.flights,
                    centerLat = uiState.settings.selectedCity.latitude,
                    centerLon = uiState.settings.selectedCity.longitude,
                    isAltitudeFeet = uiState.settings.isAltitudeFeet,
                    isKnots = !uiState.settings.isMph,
                    onInspect = { viewModel.onInspectWidget(InspectWidgetType.RADAR) },
                    isShort = isShort,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(rowSpacing))
            }

            if (uiState.settings.showHistory) {
                HistoryWidget(
                    historyItems = uiState.historyItems,
                    activeIndex = uiState.activeHistoryIndex,
                    onNext = { viewModel.nextHistoryItem() },
                    onPrev = { viewModel.prevHistoryItem() },
                    onInspect = { viewModel.onInspectWidget(InspectWidgetType.HISTORY) },
                    isShort = isShort,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isShort || !isWide) 6.dp else 16.dp))
    }
}

@Composable
private fun GridLayout(
    uiState: DeckUiState,
    viewModel: AmbientDeckViewModel,
    isWide: Boolean,
    isShort: Boolean
) {
    val rowSpacing = if (isShort) 6.dp else if (!isWide) 8.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (isWide) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                ClockWidget(
                    currentTime = uiState.currentTime,
                    currentSeconds = uiState.currentSeconds,
                    currentPeriod = uiState.currentPeriod,
                    currentDate = uiState.currentDate,
                    settings = uiState.settings,
                    lastUpdatedTime = uiState.lastUpdatedTime,
                    isRefreshing = uiState.isRefreshing,
                    onOpenSettings = { viewModel.onToggleSettings(true) },
                    onManualRefresh = { viewModel.refreshAllData() },
                    isShort = isShort,
                    modifier = Modifier.weight(1f)
                )

                WeatherAqiWidget(
                    weather = uiState.weather,
                    isCelsius = uiState.settings.isCelsius,
                    isMph = uiState.settings.isMph,
                    onInspect = { viewModel.onInspectWidget(InspectWidgetType.WEATHER) },
                    isShort = isShort,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(rowSpacing))

            Row(
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                FlightRadarWidget(
                    flights = uiState.flights,
                    centerLat = uiState.settings.selectedCity.latitude,
                    centerLon = uiState.settings.selectedCity.longitude,
                    isAltitudeFeet = uiState.settings.isAltitudeFeet,
                    isKnots = !uiState.settings.isMph,
                    onInspect = { viewModel.onInspectWidget(InspectWidgetType.RADAR) },
                    isShort = isShort,
                    modifier = Modifier.weight(1.2f)
                )

                HistoryWidget(
                    historyItems = uiState.historyItems,
                    activeIndex = uiState.activeHistoryIndex,
                    onNext = { viewModel.nextHistoryItem() },
                    onPrev = { viewModel.prevHistoryItem() },
                    onInspect = { viewModel.onInspectWidget(InspectWidgetType.HISTORY) },
                    isShort = isShort,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            ClockWidget(
                currentTime = uiState.currentTime,
                currentSeconds = uiState.currentSeconds,
                currentPeriod = uiState.currentPeriod,
                currentDate = uiState.currentDate,
                settings = uiState.settings,
                lastUpdatedTime = uiState.lastUpdatedTime,
                isRefreshing = uiState.isRefreshing,
                onOpenSettings = { viewModel.onToggleSettings(true) },
                onManualRefresh = { viewModel.refreshAllData() },
                isShort = isShort,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(rowSpacing))

            WeatherAqiWidget(
                weather = uiState.weather,
                isCelsius = uiState.settings.isCelsius,
                isMph = uiState.settings.isMph,
                onInspect = { viewModel.onInspectWidget(InspectWidgetType.WEATHER) },
                isShort = isShort,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(rowSpacing))

            FlightRadarWidget(
                flights = uiState.flights,
                centerLat = uiState.settings.selectedCity.latitude,
                centerLon = uiState.settings.selectedCity.longitude,
                isAltitudeFeet = uiState.settings.isAltitudeFeet,
                isKnots = !uiState.settings.isMph,
                onInspect = { viewModel.onInspectWidget(InspectWidgetType.RADAR) },
                isShort = isShort,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(rowSpacing))

            HistoryWidget(
                historyItems = uiState.historyItems,
                activeIndex = uiState.activeHistoryIndex,
                onNext = { viewModel.nextHistoryItem() },
                onPrev = { viewModel.prevHistoryItem() },
                onInspect = { viewModel.onInspectWidget(InspectWidgetType.HISTORY) },
                isShort = isShort,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(rowSpacing))

        // Grid Row: Crypto Market Ticker
        CryptoTickerWidget(
            tickers = uiState.cryptoTickers,
            onInspect = { viewModel.onInspectWidget(InspectWidgetType.CRYPTO) },
            isShort = isShort,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(if (isShort || !isWide) 6.dp else 16.dp))
    }
}

@Composable
private fun ZenMinimalLayout(
    uiState: DeckUiState,
    viewModel: AmbientDeckViewModel
) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val isCompactZen = maxWidth < 420.dp
        val clockFontSize = when {
            maxWidth < 340.dp -> 44.sp
            maxWidth < 420.dp -> 54.sp
            maxWidth < 600.dp -> 72.sp
            maxWidth < 900.dp -> 92.sp
            else -> 110.sp
        }
        val secondsFontSize = when {
            maxWidth < 420.dp -> 14.sp
            maxWidth < 600.dp -> 18.sp
            else -> 24.sp
        }
        val dateFontSize = when {
            maxWidth < 420.dp -> 14.sp
            maxWidth < 600.dp -> 17.sp
            else -> 22.sp
        }
        val zenPadding = when {
            maxWidth < 420.dp -> 14.dp
            maxWidth < 600.dp -> 20.dp
            else -> 32.dp
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(zenPadding)
        ) {
            // Giant Zen Clock
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = uiState.currentTime,
                    color = TextPrimary,
                    fontSize = clockFontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = uiState.currentSeconds,
                    color = CyanAccent,
                    fontSize = secondsFontSize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = if (isCompactZen) 8.dp else 16.dp, start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = uiState.currentDate,
                color = TextSecondary,
                fontSize = dateFontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ambient Weather & Location Pill
            uiState.weather?.let { w ->
                val displayTemp = if (uiState.settings.isCelsius) w.currentTemp else (w.currentTemp * 9 / 5 + 32)
                val unitStr = if (uiState.settings.isCelsius) "°C" else "°F"
                val conditionStr = stringResource(com.example.data.model.getWeatherConditionRes(w.weatherCode))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassOverlay)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.onToggleSettings(true) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.zen_weather_format,
                            uiState.settings.selectedCity.name,
                            displayTemp,
                            unitStr,
                            conditionStr,
                            w.aqiUs
                        ),
                        color = TextPrimary,
                        fontSize = if (isCompactZen) 12.sp else 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingControlBar(
    isRefreshing: Boolean,
    isCompact: Boolean = false,
    onCycleWallpaper: () -> Unit,
    onCycleLayout: () -> Unit,
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBarFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassOverlay)
            .border(
                width = if (isBarFocused) 2.dp else 1.dp,
                color = if (isBarFocused) CyanAccent else GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .focusable()
            .onFocusChanged { isBarFocused = it.isFocused }
            .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 4.dp else 6.dp)
            .testTag("floating_control_bar")
    ) {
        // Next Wallpaper
        IconButton(
            onClick = onCycleWallpaper,
            modifier = Modifier.size(if (isCompact) 30.dp else 36.dp).testTag("action_next_wallpaper")
        ) {
            Icon(Icons.Default.Image, contentDescription = stringResource(R.string.next_wallpaper), tint = TextSecondary, modifier = Modifier.size(if (isCompact) 15.dp else 18.dp))
        }

        // Layout Preset
        IconButton(
            onClick = onCycleLayout,
            modifier = Modifier.size(if (isCompact) 30.dp else 36.dp).testTag("action_cycle_layout")
        ) {
            Icon(Icons.Default.Dashboard, contentDescription = stringResource(R.string.switch_layout), tint = SunsetAmber, modifier = Modifier.size(if (isCompact) 15.dp else 18.dp))
        }

        // Refresh Data
        IconButton(
            onClick = onRefresh,
            modifier = Modifier.size(if (isCompact) 30.dp else 36.dp).testTag("action_refresh")
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(if (isCompact) 14.dp else 16.dp),
                    strokeWidth = 2.dp,
                    color = CyanAccent
                )
            } else {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_data), tint = EmeraldGreen, modifier = Modifier.size(if (isCompact) 15.dp else 18.dp))
            }
        }

        // Deck Settings
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(if (isCompact) 30.dp else 36.dp).testTag("action_open_settings")
        ) {
            Icon(Icons.Default.Tune, contentDescription = stringResource(R.string.deck_settings), tint = CyanAccent, modifier = Modifier.size(if (isCompact) 15.dp else 18.dp))
        }
    }
}
