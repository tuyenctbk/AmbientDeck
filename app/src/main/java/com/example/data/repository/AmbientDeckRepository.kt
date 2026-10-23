package com.example.data.repository

import com.example.data.api.NetworkClientProvider
import com.example.data.model.ArtCategory
import com.example.data.model.CryptoTicker
import com.example.data.model.DayForecast
import com.example.data.model.FlightState
import com.example.data.model.HistoryItem
import com.example.data.model.WallpaperItem
import com.example.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class AmbientDeckRepository {

    private val forecastApi = NetworkClientProvider.openMeteoForecastApi
    private val airQualityApi = NetworkClientProvider.openMeteoAirQualityApi
    private val binanceApi = NetworkClientProvider.binanceApi
    private val openSkyApi = NetworkClientProvider.openSkyApi
    private val historyApi = NetworkClientProvider.muffinLabsApi

    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherData = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                val forecastDeferred = async { forecastApi.getForecast(latitude, longitude) }
                val aqiDeferred = async {
                    try {
                        airQualityApi.getAirQuality(latitude, longitude)
                    } catch (e: Exception) {
                        null
                    }
                }

                val forecast = forecastDeferred.await()
                val aqi = aqiDeferred.await()

                val current = forecast.current
                val currentTemp = current?.temperature2m ?: 21.5
                val apparentTemp = current?.apparentTemperature ?: currentTemp
                val humidity = current?.relativeHumidity2m?.toInt() ?: 55
                val windSpeed = current?.windSpeed10m ?: 12.0
                val weatherCode = current?.weatherCode ?: 0
                val isDay = (current?.isDay ?: 1) == 1

                val aqiEuVal = aqi?.current?.europeanAqi?.toInt() ?: 25
                val aqiUsVal = aqi?.current?.usAqi?.toInt() ?: 30
                val pm25Val = aqi?.current?.pm25 ?: 6.5
                val pm10Val = aqi?.current?.pm10 ?: 12.0

                val dailyMax = forecast.daily?.temperature2mMax?.firstOrNull() ?: (currentTemp + 4.0)
                val dailyMin = forecast.daily?.temperature2mMin?.firstOrNull() ?: (currentTemp - 4.0)

                // 7-day forecast
                val forecastDays = mutableListOf<DayForecast>()
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val cal = Calendar.getInstance()
                val times = forecast.daily?.time ?: emptyList()
                val codes = forecast.daily?.weatherCode ?: emptyList()
                val maxs = forecast.daily?.temperature2mMax ?: emptyList()
                val mins = forecast.daily?.temperature2mMin ?: emptyList()

                for (i in 0 until minOf(times.size, codes.size, maxs.size, mins.size)) {
                    val label = if (i == 0) "Today" else {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        dayFormat.format(cal.time)
                    }
                    val code = codes[i]
                    forecastDays.add(
                        DayForecast(
                            dayName = label,
                            weatherCode = code,
                            conditionText = mapWeatherCodeToText(code),
                            maxTemp = maxs[i],
                            minTemp = mins[i]
                        )
                    )
                }

                WeatherData(
                    currentTemp = currentTemp,
                    apparentTemp = apparentTemp,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    weatherCode = weatherCode,
                    isDay = isDay,
                    conditionText = mapWeatherCodeToText(weatherCode),
                    aqiUs = aqiUsVal,
                    aqiEu = aqiEuVal,
                    pm25 = pm25Val,
                    pm10 = pm10Val,
                    dailyMax = dailyMax,
                    dailyMin = dailyMin,
                    forecastDays = forecastDays
                )
            }
        } catch (e: Exception) {
            // Graceful fallback for offline / connectivity hiccups
            getFallbackWeather(latitude)
        }
    }

    suspend fun fetchCryptoTickers(): List<CryptoTicker> = withContext(Dispatchers.IO) {
        try {
            val symbolsJson = "[\"BTCUSDT\",\"ETHUSDT\",\"SOLUSDT\",\"BNBUSDT\",\"DOGEUSDT\",\"ADAUSDT\",\"AVAXUSDT\",\"XRPUSDT\"]"
            val responses = binanceApi.get24hTickers(symbolsJson)

            val nameMap = mapOf(
                "BTCUSDT" to "Bitcoin",
                "ETHUSDT" to "Ethereum",
                "SOLUSDT" to "Solana",
                "BNBUSDT" to "BNB Chain",
                "DOGEUSDT" to "Dogecoin",
                "ADAUSDT" to "Cardano",
                "AVAXUSDT" to "Avalanche",
                "XRPUSDT" to "XRP Ledger"
            )

            responses.map { item ->
                val price = item.lastPrice?.toDoubleOrNull() ?: 0.0
                val change = item.priceChangePercent?.toDoubleOrNull() ?: 0.0
                val high = item.highPrice?.toDoubleOrNull() ?: price
                val low = item.lowPrice?.toDoubleOrNull() ?: price

                // Generate 8-point mini sparkline based on trend
                val sparkline = generateSparkline(price, change, high, low)

                CryptoTicker(
                    symbol = item.symbol.removeSuffix("USDT"),
                    name = nameMap[item.symbol] ?: item.symbol.removeSuffix("USDT"),
                    priceUsd = price,
                    changePercent24h = change,
                    high24h = high,
                    low24h = low,
                    sparkline = sparkline
                )
            }
        } catch (e: Exception) {
            getFallbackCrypto()
        }
    }

    suspend fun fetchOverheadFlights(latitude: Double, longitude: Double): List<FlightState> = withContext(Dispatchers.IO) {
        try {
            val delta = 1.3
            val lamin = latitude - delta
            val lamax = latitude + delta
            val lomin = longitude - (delta * 1.3)
            val lomax = longitude + (delta * 1.3)

            val response = openSkyApi.getStates(lamin, lomin, lamax, lomax)
            val states = response.states

            val parsedFlights = mutableListOf<FlightState>()
            if (states != null) {
                for (row in states) {
                    if (row.size >= 12) {
                        val icao = row[0] as? String ?: continue
                        val rawCallsign = (row[1] as? String)?.trim() ?: ""
                        val callsign = if (rawCallsign.isNotEmpty()) rawCallsign else "FLT-${icao.takeLast(4).uppercase()}"
                        val country = row[2] as? String ?: "Unknown"
                        val lon = (row[5] as? Number)?.toDouble() ?: continue
                        val lat = (row[6] as? Number)?.toDouble() ?: continue
                        val altMeters = (row[7] as? Number)?.toDouble() ?: continue
                        val onGround = row[8] as? Boolean ?: false
                        val velocityMs = (row[9] as? Number)?.toDouble() ?: 0.0
                        val heading = (row[10] as? Number)?.toDouble() ?: 0.0
                        val verticalRate = (row[11] as? Number)?.toDouble() ?: 0.0

                        if (!onGround && altMeters > 500) {
                            parsedFlights.add(
                                FlightState(
                                    icao24 = icao,
                                    callsign = callsign,
                                    originCountry = country,
                                    altitudeMeters = altMeters,
                                    altitudeFeet = altMeters * 3.28084,
                                    velocityKmh = velocityMs * 3.6,
                                    velocityKnots = velocityMs * 1.94384,
                                    headingDegrees = heading,
                                    latitude = lat,
                                    longitude = lon,
                                    verticalRate = verticalRate
                                )
                            )
                        }
                    }
                }
            }

            if (parsedFlights.isNotEmpty()) {
                parsedFlights.take(16)
            } else {
                generateRepresentativeFlights(latitude, longitude)
            }
        } catch (e: Exception) {
            generateRepresentativeFlights(latitude, longitude)
        }
    }

    suspend fun fetchTodayInHistory(): List<HistoryItem> = withContext(Dispatchers.IO) {
        try {
            val response = historyApi.getTodayInHistory()
            val events = response.data?.events ?: emptyList()
            val births = response.data?.births ?: emptyList()

            val list = mutableListOf<HistoryItem>()
            for (item in events.take(10)) {
                val yr = item.year ?: ""
                val txt = item.text ?: ""
                if (txt.isNotBlank()) {
                    list.add(HistoryItem(year = yr, text = txt, category = "Event"))
                }
            }

            for (item in births.take(5)) {
                val yr = item.year ?: ""
                val txt = item.text ?: ""
                if (txt.isNotBlank()) {
                    list.add(HistoryItem(year = yr, text = "Birth: $txt", category = "Birth"))
                }
            }

            if (list.isNotEmpty()) list else getFallbackHistory()
        } catch (e: Exception) {
            getFallbackHistory()
        }
    }

    fun getWallpapersForCategory(category: ArtCategory): List<WallpaperItem> {
        return when (category) {
            ArtCategory.SCENIC -> listOf(
                WallpaperItem("sc1", "Patagonia Mountain Peaks", ArtCategory.SCENIC, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=2560&q=80", "Sean O."),
                WallpaperItem("sc2", "Nordic Emerald Aurora", ArtCategory.SCENIC, "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=2560&q=80", "Jonatan Pie"),
                WallpaperItem("sc3", "Misty Alpine Lake Morning", ArtCategory.SCENIC, "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=2560&q=80", "Kaley Dykstra"),
                WallpaperItem("sc4", "Pacific Ocean Sunset Horizon", ArtCategory.SCENIC, "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=2560&q=80", "Pietro De Grandi")
            )
            ArtCategory.CYBER_NIGHT -> listOf(
                WallpaperItem("cy1", "Tokyo Shinjuku Neon Rain", ArtCategory.CYBER_NIGHT, "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=2560&q=80", "Aleksandar Pasaric"),
                WallpaperItem("cy2", "Hong Kong Cyberpunk Skyline", ArtCategory.CYBER_NIGHT, "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=2560&q=80", "Simon Zhu"),
                WallpaperItem("cy3", "Futuristic Suspension Bridge", ArtCategory.CYBER_NIGHT, "https://images.unsplash.com/photo-1519501025264-65ba15a82390?auto=format&fit=crop&w=2560&q=80", "Denys Nevozhai"),
                WallpaperItem("cy4", "Seoul Night Light Trails", ArtCategory.CYBER_NIGHT, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=2560&q=80", "Ciaran O'Brien")
            )
            ArtCategory.SPACE -> listOf(
                WallpaperItem("sp1", "Orion Stellar Cloud Nursery", ArtCategory.SPACE, "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=2560&q=80", "NASA Archive"),
                WallpaperItem("sp2", "Milky Way Over Atacama Desert", ArtCategory.SPACE, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=2560&q=80", "NASA HQ"),
                WallpaperItem("sp3", "Deep Space Nebula Dust", ArtCategory.SPACE, "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=2560&q=80", "SpaceX Hub"),
                WallpaperItem("sp4", "Earth Horizon from Orbit", ArtCategory.SPACE, "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=2560&q=80", "NASA Earth Observatory")
            )
            ArtCategory.ARCHITECTURE -> listOf(
                WallpaperItem("ar1", "Minimalist Concrete Cantilever", ArtCategory.ARCHITECTURE, "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=2560&q=80", "Simone Hutsch"),
                WallpaperItem("ar2", "Geometric Glass Spiral", ArtCategory.ARCHITECTURE, "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=2560&q=80", "Phil Hearing"),
                WallpaperItem("ar3", "Nordic Architectural Facade", ArtCategory.ARCHITECTURE, "https://images.unsplash.com/photo-1487958449943-2429e8be8625?auto=format&fit=crop&w=2560&q=80", "Lance Anderson")
            )
            ArtCategory.FINE_ART -> listOf(
                WallpaperItem("fa1", "Ethereal Oil Canvas Waves", ArtCategory.FINE_ART, "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=2560&q=80", "Steve Johnson"),
                WallpaperItem("fa2", "Abstract Cyan Fluid Art", ArtCategory.FINE_ART, "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=2560&q=80", "Pawel Czerwinski"),
                WallpaperItem("fa3", "Deep Indigo Texture", ArtCategory.FINE_ART, "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=2560&q=80", "Daniel Olah")
            )
        }
    }

    private fun mapWeatherCodeToText(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snowfall"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Severe Storm"
            else -> "Partly Cloudy"
        }
    }

    private fun generateSparkline(price: Double, changePercent: Double, high: Double, low: Double): List<Float> {
        val points = mutableListOf<Float>()
        val startRatio = if (changePercent >= 0) 0.35f else 0.75f
        points.add(startRatio)
        points.add((startRatio + 0.1f * (if (changePercent >= 0) 1 else -1)).coerceIn(0.1f, 0.9f))
        points.add((startRatio + 0.05f).coerceIn(0.1f, 0.9f))
        points.add((startRatio + 0.2f * (if (changePercent >= 0) 1 else -1)).coerceIn(0.1f, 0.9f))
        points.add((startRatio + 0.15f * (if (changePercent >= 0) 1 else -1)).coerceIn(0.1f, 0.9f))
        points.add((startRatio + 0.3f * (if (changePercent >= 0) 1 else -1)).coerceIn(0.1f, 0.9f))
        points.add((startRatio + 0.25f * (if (changePercent >= 0) 1 else -1)).coerceIn(0.1f, 0.9f))
        val endRatio = if (changePercent >= 0) 0.85f else 0.2f
        points.add(endRatio)
        return points
    }

    private fun generateRepresentativeFlights(centerLat: Double, centerLon: Double): List<FlightState> {
        val simulated = listOf(
            Triple("UAL412", "United Airlines", 0.4 to 0.5),
            Triple("AAL889", "American Airlines", -0.6 to 0.3),
            Triple("DAL104", "Delta Air Lines", 0.3 to -0.7),
            Triple("BAW178", "British Airways", -0.4 to -0.5),
            Triple("AFR022", "Air France", 0.8 to 0.2),
            Triple("SWA210", "Southwest Airlines", -0.2 to 0.8)
        )

        return simulated.mapIndexed { index, (callsign, country, offset) ->
            val lat = centerLat + offset.first
            val lon = centerLon + offset.second
            val altMeters = 7500.0 + (index * 600)
            val speedMs = 210.0 + (index * 15)
            val heading = (index * 58.0 + 35.0) % 360.0

            FlightState(
                icao24 = "sim${1000 + index}",
                callsign = callsign,
                originCountry = country,
                altitudeMeters = altMeters,
                altitudeFeet = altMeters * 3.28084,
                velocityKmh = speedMs * 3.6,
                velocityKnots = speedMs * 1.94384,
                headingDegrees = heading,
                latitude = lat,
                longitude = lon,
                verticalRate = if (index % 2 == 0) 2.5 else -1.2
            )
        }
    }

    private fun getFallbackWeather(latitude: Double): WeatherData {
        return WeatherData(
            currentTemp = 21.0,
            apparentTemp = 20.5,
            humidity = 58,
            windSpeed = 14.2,
            weatherCode = 1,
            isDay = true,
            conditionText = "Mainly Clear",
            aqiUs = 34,
            aqiEu = 28,
            pm25 = 7.4,
            pm10 = 13.0,
            dailyMax = 25.0,
            dailyMin = 16.0,
            forecastDays = listOf(
                DayForecast("Today", 1, "Mainly Clear", 25.0, 16.0),
                DayForecast("Wed", 2, "Partly Cloudy", 24.0, 15.0),
                DayForecast("Thu", 0, "Clear Sky", 26.0, 17.0),
                DayForecast("Fri", 61, "Showers", 22.0, 14.0),
                DayForecast("Sat", 2, "Partly Cloudy", 23.0, 15.0)
            )
        )
    }

    private fun getFallbackCrypto(): List<CryptoTicker> {
        return listOf(
            CryptoTicker("BTC", "Bitcoin", 67840.0, 2.45, 68500.0, 66100.0, listOf(0.3f, 0.4f, 0.35f, 0.6f, 0.55f, 0.7f, 0.85f)),
            CryptoTicker("ETH", "Ethereum", 3540.0, 3.12, 3610.0, 3420.0, listOf(0.4f, 0.45f, 0.5f, 0.65f, 0.6f, 0.78f, 0.88f)),
            CryptoTicker("SOL", "Solana", 158.4, 5.84, 162.0, 149.0, listOf(0.2f, 0.35f, 0.4f, 0.55f, 0.7f, 0.8f, 0.92f)),
            CryptoTicker("BNB", "BNB Chain", 584.2, -0.65, 595.0, 580.0, listOf(0.7f, 0.65f, 0.6f, 0.55f, 0.5f, 0.45f, 0.4f)),
            CryptoTicker("DOGE", "Dogecoin", 0.128, 1.80, 0.134, 0.122, listOf(0.4f, 0.45f, 0.5f, 0.48f, 0.6f, 0.7f, 0.75f)),
            CryptoTicker("AVAX", "Avalanche", 28.5, 4.20, 29.8, 27.1, listOf(0.3f, 0.4f, 0.45f, 0.6f, 0.7f, 0.82f, 0.88f))
        )
    }

    private fun getFallbackHistory(): List<HistoryItem> {
        return listOf(
            HistoryItem("1947", "Chuck Yeager breaks the sound barrier in the Bell X-1 rocket airplane.", "Aviation Milestone"),
            HistoryItem("1968", "Apollo 7 performs the first live American television broadcast from orbit.", "Space Exploration"),
            HistoryItem("1908", "The Chicago Cubs defeat the Detroit Tigers to win the World Series.", "Sports Legend"),
            HistoryItem("1964", "Martin Luther King Jr. becomes the youngest recipient of the Nobel Peace Prize.", "World History"),
            HistoryItem("1979", "The first national gay rights march draws 100,000 people to Washington D.C.", "Civil Rights")
        )
    }
}
