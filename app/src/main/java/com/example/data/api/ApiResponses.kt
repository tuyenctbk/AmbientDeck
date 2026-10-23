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
    @Json(name = "volume") val volume: String? = null
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
