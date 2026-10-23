package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class LayoutMode(val displayName: String, val description: String) {
    CINEMATIC("Cinematic Wall", "Hero 4K art backdrop with floating glassmorphic widgets"),
    GRID("Kiosk Multi-Panel", "Structured dense grid dashboard displaying all feeds simultaneously"),
    ZEN("Zen Clock Minimal", "Minimalist large typography clock and subtle ambient weather")
}

enum class ArtCategory(val displayName: String) {
    SCENIC("Scenic Landscapes"),
    CYBER_NIGHT("Cyberpunk & Night Lights"),
    SPACE("Cosmic & Astrophotography"),
    ARCHITECTURE("Modern Architecture"),
    FINE_ART("Fine Art & Museum")
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
    val conditionText: String,
    val uvIndex: Double = 3.5,
    val aqiUs: Int = 32,
    val aqiEu: Int = 28,
    val pm25: Double = 8.2,
    val pm10: Double = 14.5,
    val dailyMax: Double = 22.0,
    val dailyMin: Double = 14.0,
    val forecastDays: List<DayForecast> = emptyList()
)

data class DayForecast(
    val dayName: String,
    val weatherCode: Int,
    val conditionText: String,
    val maxTemp: Double,
    val minTemp: Double
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
    val category: String = "Event"
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
