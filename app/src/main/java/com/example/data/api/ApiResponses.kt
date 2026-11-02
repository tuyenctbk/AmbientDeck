package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Open-Meteo Weather Forecast
@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "current") val current: OpenMeteoCurrent? = null,
    @Json(name = "daily") val daily: OpenMeteoDaily? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "is_day") val isDay: Int? = null,
    @Json(name = "precipitation") val precipitation: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null
)

// Open-Meteo Air Quality
@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    @Json(name = "current") val current: OpenMeteoAirQualityCurrent? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityCurrent(
    @Json(name = "european_aqi") val europeanAqi: Double? = null,
    @Json(name = "us_aqi") val usAqi: Double? = null,
    @Json(name = "pm10") val pm10: Double? = null,
    @Json(name = "pm2_5") val pm25: Double? = null
)

// Binance 24h Ticker
@JsonClass(generateAdapter = true)
data class BinanceTickerResponse(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "lastPrice") val lastPrice: String? = null,
    @Json(name = "priceChangePercent") val priceChangePercent: String? = null,
    @Json(name = "highPrice") val highPrice: String? = null,
    @Json(name = "lowPrice") val lowPrice: String? = null,
    @Json(name = "openPrice") val openPrice: String? = null,
    @Json(name = "weightedAvgPrice") val weightedAvgPrice: String? = null,
    @Json(name = "volume") val volume: String? = null
)

// CoinGecko Markets Response
@JsonClass(generateAdapter = true)
data class CoinGeckoMarketItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "current_price") val currentPrice: Double? = null,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @Json(name = "high_24h") val high24h: Double? = null,
    @Json(name = "low_24h") val low24h: Double? = null,
    @Json(name = "sparkline_in_7d") val sparklineIn7d: CoinGeckoSparkline? = null
)

@JsonClass(generateAdapter = true)
data class CoinGeckoSparkline(
    @Json(name = "price") val price: List<Double>? = null
)

// OpenSky Network Response
@JsonClass(generateAdapter = true)
data class OpenSkyResponse(
    @Json(name = "time") val time: Long? = null,
    @Json(name = "states") val states: List<List<Any?>>? = null
)

// MuffinLabs History API Response
@JsonClass(generateAdapter = true)
data class MuffinLabsHistoryResponse(
    @Json(name = "date") val date: String? = null,
    @Json(name = "data") val data: MuffinLabsData? = null
)

@JsonClass(generateAdapter = true)
data class MuffinLabsData(
    @Json(name = "Events") val events: List<MuffinLabsEventItem>? = null,
    @Json(name = "Births") val births: List<MuffinLabsEventItem>? = null
)

@JsonClass(generateAdapter = true)
data class MuffinLabsEventItem(
    @Json(name = "year") val year: String? = null,
    @Json(name = "text") val text: String? = null
)

// Unsplash Image Search API
@JsonClass(generateAdapter = true)
data class UnsplashSearchResponse(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "total_pages") val totalPages: Int? = null,
    @Json(name = "results") val results: List<UnsplashPhotoItem>? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashPhotoItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "alt_description") val altDescription: String? = null,
    @Json(name = "urls") val urls: UnsplashUrls? = null,
    @Json(name = "user") val user: UnsplashUser? = null,
    @Json(name = "links") val links: UnsplashLinks? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashUrls(
    @Json(name = "raw") val raw: String? = null,
    @Json(name = "full") val full: String? = null,
    @Json(name = "regular") val regular: String? = null,
    @Json(name = "small") val small: String? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashUser(
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null
)

@JsonClass(generateAdapter = true)
data class UnsplashLinks(
    @Json(name = "html") val html: String? = null
)
