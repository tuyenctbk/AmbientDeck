package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.manager.EngagementManager
import com.example.data.manager.PromptType
import com.example.data.model.ArtCategory
import com.example.data.model.CryptoTicker
import com.example.data.model.DeckSettings
import com.example.data.model.DefaultLocations
import com.example.data.model.FlightState
import com.example.data.model.HistoryItem
import com.example.data.model.LayoutMode
import com.example.data.model.LocationPreset
import com.example.data.model.WallpaperItem
import com.example.data.model.WeatherData
import com.example.data.repository.AmbientDeckRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class InspectWidgetType {
    WEATHER,
    RADAR,
    CRYPTO,
    HISTORY
}

enum class WidgetToggleType {
    WEATHER,
    CRYPTO,
    RADAR,
    HISTORY,
    CLOCK
}

data class DeckUiState(
    val showSplash: Boolean = true,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val currentTime: String = "12:00",
    val currentSeconds: String = ":00",
    val currentPeriod: String = "PM",
    val currentDate: String = "Wednesday, Oct 14",
    val weather: WeatherData? = null,
    val cryptoTickers: List<CryptoTicker> = emptyList(),
    val flights: List<FlightState> = emptyList(),
    val historyItems: List<HistoryItem> = emptyList(),
    val activeHistoryIndex: Int = 0,
    val currentWallpaper: WallpaperItem? = null,
    val wallpapers: List<WallpaperItem> = emptyList(),
    val wallpaperIndex: Int = 0,
    val settings: DeckSettings = DeckSettings(),
    val pixelShiftX: Float = 0f,
    val pixelShiftY: Float = 0f,
    val inspectedWidget: InspectWidgetType? = null,
    val isSettingsOpen: Boolean = false,
    val lastUpdated: String = "Just now",
    val pendingPrompt: com.example.data.manager.PromptType? = null,
    val showPrivacyPolicy: Boolean = false
)

class AmbientDeckViewModel(
    private val repository: AmbientDeckRepository = AmbientDeckRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckUiState())
    val uiState: StateFlow<DeckUiState> = _uiState.asStateFlow()

    private var clockJob: Job? = null
    private var dataPollingJob: Job? = null
    private var wallpaperJob: Job? = null

    init {
        loadWallpapers(_uiState.value.settings.artCategory)
        startClockLoop()
        refreshAllData()
        startDataPolling()
        startWallpaperRotation()
    }

    private fun startClockLoop() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
            val secFormat = SimpleDateFormat(":ss", Locale.getDefault())
            val periodFormat = SimpleDateFormat("a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

            var tickCounter = 0
            while (isActive) {
                val now = Date()
                tickCounter++

                // Pixel shift every 120 seconds for OLED burn-in mitigation
                val shiftX = if (_uiState.value.settings.burnInShiftEnabled) {
                    ((tickCounter / 120) % 5 - 2).toFloat()
                } else 0f
                val shiftY = if (_uiState.value.settings.burnInShiftEnabled) {
                    (((tickCounter + 60) / 120) % 5 - 2).toFloat()
                } else 0f

                _uiState.update { current ->
                    current.copy(
                        currentTime = timeFormat.format(now),
                        currentSeconds = secFormat.format(now),
                        currentPeriod = periodFormat.format(now),
                        currentDate = dateFormat.format(now),
                        pixelShiftX = shiftX,
                        pixelShiftY = shiftY
                    )
                }
                delay(1000)
            }
        }
    }

    private fun startWallpaperRotation() {
        wallpaperJob?.cancel()
        wallpaperJob = viewModelScope.launch {
            while (isActive) {
                delay(_uiState.value.settings.wallpaperIntervalSec * 1000L)
                cycleWallpaper()
            }
        }
    }

    fun cycleWallpaper() {
        _uiState.update { current ->
            if (current.wallpapers.isEmpty()) current
            else {
                val nextIndex = (current.wallpaperIndex + 1) % current.wallpapers.size
                current.copy(
                    wallpaperIndex = nextIndex,
                    currentWallpaper = current.wallpapers[nextIndex]
                )
            }
        }
    }

    private fun startDataPolling() {
        dataPollingJob?.cancel()
        dataPollingJob = viewModelScope.launch {
            var counter = 0
            while (isActive) {
                delay(5000) // 5s tick
                counter += 5

                // Crypto every 25 seconds
                if (counter % 25 == 0 && _uiState.value.settings.showCrypto) {
                    pollCrypto()
                }

                // Flights every 20 seconds
                if (counter % 20 == 0 && _uiState.value.settings.showRadar) {
                    pollFlights()
                }

                // Weather every 10 minutes (600s)
                if (counter % 600 == 0 && _uiState.value.settings.showWeather) {
                    pollWeather()
                }

                // Auto cycle history facts every 30 seconds
                if (counter % 30 == 0 && _uiState.value.settings.showHistory) {
                    nextHistoryItem()
                }
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val city = _uiState.value.settings.selectedCity

            val weather = repository.fetchWeather(city.latitude, city.longitude)
            val crypto = repository.fetchCryptoTickers()
            val flights = repository.fetchOverheadFlights(city.latitude, city.longitude)
            val history = repository.fetchTodayInHistory()

            val timeStamp = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    isRefreshing = false,
                    weather = weather,
                    cryptoTickers = crypto,
                    flights = flights,
                    historyItems = history,
                    lastUpdated = "Updated at $timeStamp"
                )
            }
        }
    }

    private suspend fun pollWeather() {
        val city = _uiState.value.settings.selectedCity
        val weather = repository.fetchWeather(city.latitude, city.longitude)
        _uiState.update { it.copy(weather = weather) }
    }

    private suspend fun pollCrypto() {
        val crypto = repository.fetchCryptoTickers()
        _uiState.update { it.copy(cryptoTickers = crypto) }
    }

    private suspend fun pollFlights() {
        val city = _uiState.value.settings.selectedCity
        val flights = repository.fetchOverheadFlights(city.latitude, city.longitude)
        _uiState.update { it.copy(flights = flights) }
    }

    fun onSelectLocation(preset: LocationPreset) {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(selectedCity = preset)
            )
        }
        refreshAllData()
    }

    fun onSelectCustomLocation(name: String, lat: Double, lon: Double) {
        val customPreset = LocationPreset("custom", name, "Custom", lat, lon)
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(selectedCity = customPreset)
            )
        }
        refreshAllData()
    }

    fun onSelectLayoutMode(mode: LayoutMode) {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(layoutMode = mode)
            )
        }
    }

    fun onSelectArtCategory(category: ArtCategory) {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(artCategory = category)
            )
        }
        loadWallpapers(category)
        startWallpaperRotation()
    }

    private fun loadWallpapers(category: ArtCategory) {
        val list = repository.getWallpapersForCategory(category)
        _uiState.update { current ->
            current.copy(
                wallpapers = list,
                wallpaperIndex = 0,
                currentWallpaper = list.firstOrNull()
            )
        }
    }

    fun onToggleWidget(type: WidgetToggleType) {
        _uiState.update { current ->
            val settings = current.settings
            val updated = when (type) {
                WidgetToggleType.WEATHER -> settings.copy(showWeather = !settings.showWeather)
                WidgetToggleType.CRYPTO -> settings.copy(showCrypto = !settings.showCrypto)
                WidgetToggleType.RADAR -> settings.copy(showRadar = !settings.showRadar)
                WidgetToggleType.HISTORY -> settings.copy(showHistory = !settings.showHistory)
                WidgetToggleType.CLOCK -> settings.copy(showClock = !settings.showClock)
            }
            current.copy(settings = updated)
        }
    }

    fun onToggleTempUnit() {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(isCelsius = !current.settings.isCelsius)
            )
        }
    }

    fun onToggleSpeedUnit() {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(isMph = !current.settings.isMph)
            )
        }
    }

    fun onToggleAltitudeUnit() {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(isAltitudeFeet = !current.settings.isAltitudeFeet)
            )
        }
    }

    fun onToggleBurnInProtection() {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(burnInShiftEnabled = !current.settings.burnInShiftEnabled)
            )
        }
    }

    fun onInspectWidget(widget: InspectWidgetType?) {
        _uiState.update { it.copy(inspectedWidget = widget) }
    }

    fun onToggleSettings(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun nextHistoryItem() {
        _uiState.update { current ->
            if (current.historyItems.isEmpty()) current
            else {
                val next = (current.activeHistoryIndex + 1) % current.historyItems.size
                current.copy(activeHistoryIndex = next)
            }
        }
    }

    fun prevHistoryItem() {
        _uiState.update { current ->
            if (current.historyItems.isEmpty()) current
            else {
                val prev = if (current.activeHistoryIndex - 1 < 0) current.historyItems.size - 1 else current.activeHistoryIndex - 1
                current.copy(activeHistoryIndex = prev)
            }
        }
    }

    private var engagementManager: EngagementManager? = null

    fun initEngagement(context: Context) {
        if (engagementManager == null) {
            val manager = EngagementManager(context)
            engagementManager = manager
            manager.recordAppLaunch()

            // Check pending prompt after initial settling delay (6 seconds)
            viewModelScope.launch {
                delay(6000)
                val prompt = manager.checkPendingPrompt(currentVersionCode = 1)
                if (prompt != null) {
                    _uiState.update { it.copy(pendingPrompt = prompt) }
                }
            }
        }
    }

    fun onDismissSplash() {
        _uiState.update { it.copy(showSplash = false) }
    }

    fun onOpenPrompt(promptType: PromptType) {
        _uiState.update { it.copy(pendingPrompt = promptType) }
    }

    fun onRateApp() {
        engagementManager?.onRateApp()
        _uiState.update { it.copy(pendingPrompt = null) }
    }

    fun onRateLater() {
        engagementManager?.onRateLater()
        _uiState.update { it.copy(pendingPrompt = null) }
    }

    fun onRateNever() {
        engagementManager?.onRateNever()
        _uiState.update { it.copy(pendingPrompt = null) }
    }

    fun onShareApp() {
        engagementManager?.onShareApp()
        _uiState.update { it.copy(pendingPrompt = null) }
    }

    fun onDismissShare() {
        engagementManager?.onDismissShare()
        _uiState.update { it.copy(pendingPrompt = null) }
    }

    fun onUpdateApp() {
        engagementManager?.onUpdateApp()
        _uiState.update { it.copy(pendingPrompt = null) }
    }

    fun onDismissUpdate() {
        engagementManager?.onDismissUpdate(versionCode = 2)
        _uiState.update { it.copy(pendingPrompt = null) }
    }

    fun onTogglePrivacyPolicy(show: Boolean) {
        _uiState.update { it.copy(showPrivacyPolicy = show) }
    }
}
