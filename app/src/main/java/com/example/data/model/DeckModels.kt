package com.example.data.model

import androidx.annotation.StringRes
import com.example.R

enum class LayoutMode(@StringRes val titleRes: Int, @StringRes val descRes: Int) {
    CINEMATIC(R.string.layout_cinematic_name, R.string.layout_cinematic_desc),
    GRID(R.string.layout_grid_name, R.string.layout_grid_desc),
    ZEN(R.string.layout_zen_name, R.string.layout_zen_desc)
}

enum class ArtCategory(@StringRes val titleRes: Int) {
    SCENIC(R.string.art_scenic),
    CYBER_NIGHT(R.string.art_cyber),
    SPACE(R.string.art_space),
    ARCHITECTURE(R.string.art_architecture),
    FINE_ART(R.string.art_fine_art)
}

data class LocationPreset(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

data class WeatherData(
    val currentTemp: Double,
    val apparentTemp: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val uvIndex: Double = 0.0,
    val aqiUs: Int = 0,
    val aqiEu: Int = 0,
    val pm25: Double = 0.0,
    val pm10: Double = 0.0,
    val dailyMax: Double = 0.0,
    val dailyMin: Double = 0.0,
    val forecastDays: List<DayForecast> = emptyList()
)

data class DayForecast(
    val isToday: Boolean = false,
    val dayName: String = "",
    val weatherCode: Int = 0,
    val maxTemp: Double = 0.0,
    val minTemp: Double = 0.0
)

data class CryptoTicker(
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val changePercent24h: Double,
    val high24h: Double,
    val low24h: Double,
    val sparkline: List<Float> = emptyList()
)

data class FlightState(
    val icao24: String,
    val callsign: String,
    val originCountry: String,
    val altitudeMeters: Double,
    val altitudeFeet: Double,
    val velocityKmh: Double,
    val velocityKnots: Double,
    val headingDegrees: Double,
    val latitude: Double,
    val longitude: Double,
    val verticalRate: Double
)

data class HistoryItem(
    val year: String,
    val text: String,
    val isBirth: Boolean = false
)

data class WallpaperItem(
    val id: String,
    val title: String,
    val category: ArtCategory,
    val imageUrl: String,
    val author: String
)

data class DeckSettings(
    val layoutMode: LayoutMode = LayoutMode.CINEMATIC,
    val selectedCity: LocationPreset = DefaultLocations.NEW_YORK,
    val isCelsius: Boolean = true,
    val isMph: Boolean = false,
    val isAltitudeFeet: Boolean = true,
    val showWeather: Boolean = true,
    val showCrypto: Boolean = true,
    val showRadar: Boolean = true,
    val showHistory: Boolean = true,
    val showClock: Boolean = true,
    val artCategory: ArtCategory = ArtCategory.SCENIC,
    val wallpaperIntervalSec: Int = 60,
    val burnInShiftEnabled: Boolean = true
)

object DefaultLocations {
    val NEW_YORK = LocationPreset("nyc", "New York", "USA", 40.7128, -74.0060)
    val LONDON = LocationPreset("lon", "London", "UK", 51.5074, -0.1278)
    val TOKYO = LocationPreset("tyo", "Tokyo", "Japan", 35.6762, 139.6503)
    val PARIS = LocationPreset("par", "Paris", "France", 48.8566, 2.3522)
    val SAN_FRANCISCO = LocationPreset("sfo", "San Francisco", "USA", 37.7749, -122.4194)
    val SINGAPORE = LocationPreset("sin", "Singapore", "SG", 1.3521, 103.8198)
    val SYDNEY = LocationPreset("syd", "Sydney", "Australia", -33.8688, 151.2093)
    val BERLIN = LocationPreset("ber", "Berlin", "Germany", 52.5200, 13.4050)
    val DUBAI = LocationPreset("dxb", "Dubai", "UAE", 25.2048, 55.2708)

    val ALL = listOf(NEW_YORK, LONDON, TOKYO, PARIS, SAN_FRANCISCO, SINGAPORE, SYDNEY, BERLIN, DUBAI)
}

@StringRes
fun getWeatherConditionRes(weatherCode: Int): Int {
    return when (weatherCode) {
        0 -> R.string.weather_clear_sky
        1 -> R.string.weather_mainly_clear
        2 -> R.string.weather_partly_cloudy
        3 -> R.string.weather_overcast
        45, 48 -> R.string.weather_foggy
        51, 53, 55 -> R.string.weather_light_drizzle
        61, 63, 65 -> R.string.weather_rain
        71, 73, 75 -> R.string.weather_snowfall
        77 -> R.string.weather_snow_grains
        80, 81, 82 -> R.string.weather_rain_showers
        85, 86 -> R.string.weather_snow_showers
        95 -> R.string.weather_thunderstorm
        96, 99 -> R.string.weather_severe_storm
        else -> R.string.weather_partly_cloudy
    }
}
