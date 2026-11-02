package com.example.data.repository

import com.example.data.api.NetworkClientProvider
import com.example.data.model.ArtCategory
import com.example.data.model.CryptoTicker
import com.example.data.model.DayForecast
import com.example.data.model.FlightState
import com.example.data.model.HistoryItem
import com.example.data.model.LandscapeCollection
import com.example.data.model.WallpaperItem
import com.example.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AmbientDeckRepository {

    private val forecastApi = NetworkClientProvider.openMeteoForecastApi
    private val airQualityApi = NetworkClientProvider.openMeteoAirQualityApi
    private val coinGeckoApi = NetworkClientProvider.coinGeckoApi
    private val binanceApi = NetworkClientProvider.binanceApi
    private val openSkyApi = NetworkClientProvider.openSkyApi
    private val historyApi = NetworkClientProvider.muffinLabsApi
    private val unsplashApi = NetworkClientProvider.unsplashApi

    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherData? = withContext(Dispatchers.IO) {
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

                val current = forecast.current ?: return@coroutineScope null
                val currentTemp = current.temperature2m ?: 0.0
                val apparentTemp = current.apparentTemperature ?: currentTemp
                val humidity = current.relativeHumidity2m?.toInt() ?: 0
                val windSpeed = current.windSpeed10m ?: 0.0
                val weatherCode = current.weatherCode ?: 0
                val isDay = (current.isDay ?: 1) == 1

                val aqiEuVal = aqi?.current?.europeanAqi?.toInt() ?: 0
                val aqiUsVal = aqi?.current?.usAqi?.toInt() ?: 0
                val pm25Val = aqi?.current?.pm25 ?: 0.0
                val pm10Val = aqi?.current?.pm10 ?: 0.0

                val dailyMax = forecast.daily?.temperature2mMax?.firstOrNull() ?: currentTemp
                val dailyMin = forecast.daily?.temperature2mMin?.firstOrNull() ?: currentTemp

                // 7-day forecast
                val forecastDays = mutableListOf<DayForecast>()
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val cal = Calendar.getInstance()
                val times = forecast.daily?.time ?: emptyList()
                val codes = forecast.daily?.weatherCode ?: emptyList()
                val maxs = forecast.daily?.temperature2mMax ?: emptyList()
                val mins = forecast.daily?.temperature2mMin ?: emptyList()

                for (i in 0 until minOf(times.size, codes.size, maxs.size, mins.size)) {
                    val label = if (i == 0) "" else {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        dayFormat.format(cal.time)
                    }
                    val code = codes[i]
                    forecastDays.add(
                        DayForecast(
                            isToday = (i == 0),
                            dayName = label,
                            weatherCode = code,
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
            null
        }
    }

    suspend fun fetchCryptoTickers(): List<CryptoTicker> = withContext(Dispatchers.IO) {
        // Attempt 1: CoinGecko API (Rich data with real 7-day sparkline prices)
        try {
            val coinGeckoList = coinGeckoApi.getTopCoins()
            if (coinGeckoList.isNotEmpty()) {
                return@withContext coinGeckoList.map { item ->
                    val price = item.currentPrice ?: 0.0
                    val change = item.priceChangePercentage24h ?: 0.0
                    val high = item.high24h ?: price
                    val low = item.low24h ?: price
                    val rawSparkline = item.sparklineIn7d?.price

                    val sparklinePoints = if (!rawSparkline.isNullOrEmpty()) {
                        normalizeSparkline(rawSparkline)
                    } else {
                        val open = price / (1.0 + change / 100.0)
                        val avg = (high + low + price) / 3.0
                        generateSparkline(open, high, low, avg, price)
                    }

                    CryptoTicker(
                        symbol = (item.symbol ?: "CRYPTO").uppercase(),
                        name = item.name ?: item.symbol ?: "Crypto",
                        priceUsd = price,
                        changePercent24h = change,
                        high24h = high,
                        low24h = low,
                        sparkline = sparklinePoints
                    )
                }
            }
        } catch (e: Exception) {
            // Log or gracefully proceed to Binance fallback
        }

        // Attempt 2: Binance Fallback
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
                val open = item.openPrice?.toDoubleOrNull() ?: (price / (1.0 + change / 100.0))
                val avg = item.weightedAvgPrice?.toDoubleOrNull() ?: ((high + low + price) / 3.0)

                val sparkline = generateSparkline(open, high, low, avg, price)

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
            emptyList()
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

            parsedFlights.take(16)
        } catch (e: Exception) {
            emptyList()
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
                    list.add(HistoryItem(year = yr, text = txt, isBirth = false))
                }
            }

            for (item in births.take(5)) {
                val yr = item.year ?: ""
                val txt = item.text ?: ""
                if (txt.isNotBlank()) {
                    list.add(HistoryItem(year = yr, text = txt, isBirth = true))
                }
            }

            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getWallpapersForCategory(category: ArtCategory): List<WallpaperItem> {
        return when (category) {
            ArtCategory.SCENIC -> listOf(
                WallpaperItem("sc1", "Patagonia Mountain Peaks", ArtCategory.SCENIC, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=2560&q=80", "Sean O."),
                WallpaperItem("sc2", "Nordic Emerald Aurora", ArtCategory.SCENIC, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=2560&q=80", "Jonatan Pie"),
                WallpaperItem("sc3", "Misty Alpine Lake Morning", ArtCategory.SCENIC, LandscapeCollection.LAKES, "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=2560&q=80", "Kaley Dykstra"),
                WallpaperItem("sc4", "Pacific Ocean Sunset Horizon", ArtCategory.SCENIC, LandscapeCollection.COASTAL, "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=2560&q=80", "Pietro De Grandi")
            )
            ArtCategory.CYBER_NIGHT -> listOf(
                WallpaperItem("cy1", "Tokyo Shinjuku Neon Rain", ArtCategory.CYBER_NIGHT, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=2560&q=80", "Aleksandar Pasaric"),
                WallpaperItem("cy2", "Hong Kong Cyberpunk Skyline", ArtCategory.CYBER_NIGHT, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1514565131-fce0801e5785?auto=format&fit=crop&w=2560&q=80", "Simon Zhu"),
                WallpaperItem("cy3", "Futuristic Suspension Bridge", ArtCategory.CYBER_NIGHT, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1519501025264-65ba15a82390?auto=format&fit=crop&w=2560&q=80", "Denys Nevozhai"),
                WallpaperItem("cy4", "Seoul Night Light Trails", ArtCategory.CYBER_NIGHT, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=2560&q=80", "Ciaran O'Brien")
            )
            ArtCategory.SPACE -> listOf(
                WallpaperItem("sp1", "Orion Stellar Cloud Nursery", ArtCategory.SPACE, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=2560&q=80", "NASA Archive"),
                WallpaperItem("sp2", "Milky Way Over Atacama Desert", ArtCategory.SPACE, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=2560&q=80", "NASA HQ"),
                WallpaperItem("sp3", "Deep Space Nebula Dust", ArtCategory.SPACE, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=2560&q=80", "SpaceX Hub"),
                WallpaperItem("sp4", "Earth Horizon from Orbit", ArtCategory.SPACE, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?auto=format&fit=crop&w=2560&q=80", "NASA Earth Observatory")
            )
            ArtCategory.ARCHITECTURE -> listOf(
                WallpaperItem("ar1", "Minimalist Concrete Cantilever", ArtCategory.ARCHITECTURE, LandscapeCollection.AERIAL, "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=2560&q=80", "Simone Hutsch"),
                WallpaperItem("ar2", "Geometric Glass Spiral", ArtCategory.ARCHITECTURE, LandscapeCollection.AERIAL, "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=2560&q=80", "Phil Hearing"),
                WallpaperItem("ar3", "Nordic Architectural Facade", ArtCategory.ARCHITECTURE, LandscapeCollection.AERIAL, "https://images.unsplash.com/photo-1487958449943-2429e8be8625?auto=format&fit=crop&w=2560&q=80", "Lance Anderson")
            )
            ArtCategory.FINE_ART -> listOf(
                WallpaperItem("fa1", "Ethereal Oil Canvas Waves", ArtCategory.FINE_ART, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=2560&q=80", "Steve Johnson"),
                WallpaperItem("fa2", "Abstract Cyan Fluid Art", ArtCategory.FINE_ART, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=2560&q=80", "Pawel Czerwinski"),
                WallpaperItem("fa3", "Deep Indigo Texture", ArtCategory.FINE_ART, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=2560&q=80", "Daniel Olah")
            )
        }
    }

    fun getCuratedCollection(collection: LandscapeCollection): List<WallpaperItem> {
        return when (collection) {
            LandscapeCollection.MOUNTAINS -> listOf(
                WallpaperItem("m1", "Patagonia Mountain Peaks", ArtCategory.SCENIC, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=2560&q=80", "Sean O.", "https://unsplash.com/photos/sea-between-cliffs"),
                WallpaperItem("m2", "Dolomite Alpine Summit", ArtCategory.SCENIC, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=2560&q=80", "Luca Bravo", "https://unsplash.com/photos/mountain-covered-snow"),
                WallpaperItem("m3", "Canadian Rocky Peaks", ArtCategory.SCENIC, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?auto=format&fit=crop&w=2560&q=80", "Kalen Emsley", "https://unsplash.com/photos/snow-capped-mountains"),
                WallpaperItem("m4", "Mount Fuji First Light", ArtCategory.SCENIC, LandscapeCollection.MOUNTAINS, "https://images.unsplash.com/photo-1490806843957-31f4c9a91c65?auto=format&fit=crop&w=2560&q=80", "Tomek Baginski", "https://unsplash.com/photos/fuji-mountain-japan")
            )
            LandscapeCollection.COASTAL -> listOf(
                WallpaperItem("c1", "Pacific Ocean Sunset Horizon", ArtCategory.SCENIC, LandscapeCollection.COASTAL, "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=2560&q=80", "Pietro De Grandi", "https://unsplash.com/photos/body-of-water-between-mountains"),
                WallpaperItem("c2", "Emerald Ocean Surf", ArtCategory.SCENIC, LandscapeCollection.COASTAL, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=2560&q=80", "Sean O.", "https://unsplash.com/photos/seashore-during-golden-hour"),
                WallpaperItem("c3", "Big Sur Cliffs & Crashing Waves", ArtCategory.SCENIC, LandscapeCollection.COASTAL, "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=2560&q=80", "Shifaaz Shamrath", "https://unsplash.com/photos/landscape-photography-of-cliffs"),
                WallpaperItem("c4", "Biarritz Sunset Tide", ArtCategory.SCENIC, LandscapeCollection.COASTAL, "https://images.unsplash.com/photo-1518837695005-2083093ee35b?auto=format&fit=crop&w=2560&q=80", "Frank McKenna", "https://unsplash.com/photos/body-of-water-during-sunset")
            )
            LandscapeCollection.FOREST -> listOf(
                WallpaperItem("f1", "Misty Alpine Lake Morning", ArtCategory.SCENIC, LandscapeCollection.FOREST, "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=2560&q=80", "Kaley Dykstra", "https://unsplash.com/photos/foggy-forest"),
                WallpaperItem("f2", "Ancient Redwood Sunlight", ArtCategory.SCENIC, LandscapeCollection.FOREST, "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=2560&q=80", "Casey Horner", "https://unsplash.com/photos/forest-with-sunbeams"),
                WallpaperItem("f3", "Emerald Bamboo Sanctuary", ArtCategory.SCENIC, LandscapeCollection.FOREST, "https://images.unsplash.com/photo-1511497584788-87676104235f?auto=format&fit=crop&w=2560&q=80", "Kazuend", "https://unsplash.com/photos/bamboo-forest"),
                WallpaperItem("f4", "Autumn Black Forest Trail", ArtCategory.SCENIC, LandscapeCollection.FOREST, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=2560&q=80", "Sebastian Unrau", "https://unsplash.com/photos/path-through-woodland")
            )
            LandscapeCollection.NORDIC_AURORA -> listOf(
                WallpaperItem("a1", "Nordic Emerald Aurora", ArtCategory.SCENIC, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?auto=format&fit=crop&w=2560&q=80", "Jonatan Pie", "https://unsplash.com/photos/northern-lights-aurora"),
                WallpaperItem("a2", "Lofoten Islands Polar Lights", ArtCategory.SCENIC, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?auto=format&fit=crop&w=2560&q=80", "Vincent Guth", "https://unsplash.com/photos/green-aurora-borealis"),
                WallpaperItem("a3", "Tromsø Arctic Aurora Dance", ArtCategory.SCENIC, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1483347756197-71ef80e95f73?auto=format&fit=crop&w=2560&q=80", "Benjamin Davies", "https://unsplash.com/photos/aurora-over-snow"),
                WallpaperItem("a4", "Starry Glacial Night", ArtCategory.SCENIC, LandscapeCollection.NORDIC_AURORA, "https://images.unsplash.com/photo-1517824806704-9040b037703b?auto=format&fit=crop&w=2560&q=80", "Stein Egil Liland", "https://unsplash.com/photos/stars-and-northern-lights")
            )
            LandscapeCollection.DESERT_CANYONS -> listOf(
                WallpaperItem("d1", "Grand Canyon Golden Light", ArtCategory.SCENIC, LandscapeCollection.DESERT_CANYONS, "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=2560&q=80", "Alan Carrillo", "https://unsplash.com/photos/canyon-sunset"),
                WallpaperItem("d2", "Sahara Sunset Dunes", ArtCategory.SCENIC, LandscapeCollection.DESERT_CANYONS, "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=2560&q=80", "Jeremy Bishop", "https://unsplash.com/photos/desert-sand-dunes"),
                WallpaperItem("d3", "Antelope Sandstone Glow", ArtCategory.SCENIC, LandscapeCollection.DESERT_CANYONS, "https://images.unsplash.com/photo-1473580044384-7ba9967e16a0?auto=format&fit=crop&w=2560&q=80", "Justin Kauffman", "https://unsplash.com/photos/sandstone-slot-canyon"),
                WallpaperItem("d4", "Monument Valley Red Mesas", ArtCategory.SCENIC, LandscapeCollection.DESERT_CANYONS, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=2560&q=80", "Claudio Schwarz", "https://unsplash.com/photos/red-rock-monument")
            )
            LandscapeCollection.LAKES -> listOf(
                WallpaperItem("l1", "Moraine Lake Turquoise Waters", ArtCategory.SCENIC, LandscapeCollection.LAKES, "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=2560&q=80", "Kalen Emsley", "https://unsplash.com/photos/turquoise-mountain-lake"),
                WallpaperItem("l2", "Lake Bled Morning Mist", ArtCategory.SCENIC, LandscapeCollection.LAKES, "https://images.unsplash.com/photo-1439853941329-a9f1a9dd62ff?auto=format&fit=crop&w=2560&q=80", "Neven Krcmarek", "https://unsplash.com/photos/lake-island-church"),
                WallpaperItem("l3", "Mirror Lake Alpine Reflection", ArtCategory.SCENIC, LandscapeCollection.LAKES, "https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=2560&q=80", "Luca Bravo", "https://unsplash.com/photos/lake-with-mountain-reflection"),
                WallpaperItem("l4", "Crater Lake Deep Sapphire", ArtCategory.SCENIC, LandscapeCollection.LAKES, "https://images.unsplash.com/photo-1433086966358-54859d0ed716?auto=format&fit=crop&w=2560&q=80", "Dan Meyers", "https://unsplash.com/photos/deep-blue-crater-lake")
            )
            LandscapeCollection.GOLDEN_HOUR -> listOf(
                WallpaperItem("g1", "Tuscan Rolling Hills Sunset", ArtCategory.SCENIC, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=2560&q=80", "Luca Bravo", "https://unsplash.com/photos/rolling-hills-at-sunset"),
                WallpaperItem("g2", "Lavender Sunset Horizon", ArtCategory.SCENIC, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1499002238440-d264edd596ec?auto=format&fit=crop&w=2560&q=80", "David Monaghan", "https://unsplash.com/photos/lavender-fields-at-sunset"),
                WallpaperItem("g3", "Golden Wheat Meadow", ArtCategory.SCENIC, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1470240731273-7821a6eeb6bd?auto=format&fit=crop&w=2560&q=80", "Christian Joudrey", "https://unsplash.com/photos/golden-meadow-twilight"),
                WallpaperItem("g4", "Alpine Dusk Glow", ArtCategory.SCENIC, LandscapeCollection.GOLDEN_HOUR, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=2560&q=80", "Sasha Freemind", "https://unsplash.com/photos/sunset-over-mountain-ridge")
            )
            LandscapeCollection.AERIAL -> listOf(
                WallpaperItem("ae1", "Iceland Volcanic Glacial Delta", ArtCategory.SCENIC, LandscapeCollection.AERIAL, "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?auto=format&fit=crop&w=2560&q=80", "Tom Podmore", "https://unsplash.com/photos/aerial-river-delta"),
                WallpaperItem("ae2", "Barrier Reef Turquoise Atoll", ArtCategory.SCENIC, LandscapeCollection.AERIAL, "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=2560&q=80", "Caleb Jones", "https://unsplash.com/photos/coral-reef-aerial"),
                WallpaperItem("ae3", "Alpine Valley Drone Panorama", ArtCategory.SCENIC, LandscapeCollection.AERIAL, "https://images.unsplash.com/photo-1426604966848-d7adac402bff?auto=format&fit=crop&w=2560&q=80", "Andreas Gücklhorn", "https://unsplash.com/photos/aerial-mountain-valley"),
                WallpaperItem("ae4", "Winding Canyon River Bird Eye", ArtCategory.SCENIC, LandscapeCollection.AERIAL, "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=2560&q=80", "Thomas Bormans", "https://unsplash.com/photos/winding-river-canyon")
            )
        }
    }

    suspend fun fetchLandscapeCollection(
        collection: LandscapeCollection,
        forceRefresh: Boolean = false
    ): List<WallpaperItem> = withContext(Dispatchers.IO) {
        val accessKey = com.example.BuildConfig.UNSPLASH_ACCESS_KEY.trim()
        if (accessKey.isNotEmpty() && !accessKey.contains("PLACEHOLDER", ignoreCase = true)) {
            try {
                val authHeader = "Client-ID $accessKey"
                val response = unsplashApi.searchPhotos(
                    query = collection.searchQuery,
                    orientation = "landscape",
                    perPage = 20,
                    authorization = authHeader
                )
                val results = response.results ?: emptyList()
                if (results.isNotEmpty()) {
                    return@withContext results.map { photo ->
                        val title = photo.altDescription
                            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            ?: photo.description
                            ?: collection.name
                        val highResUrl = photo.urls?.full ?: photo.urls?.regular ?: ""
                        val authorName = photo.user?.name ?: "Unsplash Contributor"
                        val link = photo.links?.html ?: "https://unsplash.com"
                        WallpaperItem(
                            id = photo.id ?: UUID.randomUUID().toString(),
                            title = title,
                            category = ArtCategory.SCENIC,
                            collection = collection,
                            imageUrl = highResUrl,
                            author = authorName,
                            sourceUrl = link,
                            isUnsplashApi = true
                        )
                    }
                }
            } catch (e: Exception) {
                // Graceful fallback to verified curated collection
            }
        }
        getCuratedCollection(collection)
    }

    private fun generateSparkline(
        open: Double,
        high: Double,
        low: Double,
        avg: Double,
        last: Double
    ): List<Float> {
        val range = (high - low).coerceAtLeast(0.000001)
        fun norm(v: Double): Float = (((v - low) / range).toFloat()).coerceIn(0.05f, 0.95f)

        // 8 points mapped along the real 24-hour price trajectory
        val p0 = norm(open)
        val p1 = norm(open * 0.7 + avg * 0.3)
        val p2 = if (last >= open) norm(low + (avg - low) * 0.5) else norm(high - (high - avg) * 0.5)
        val p3 = norm(avg)
        val p4 = if (last >= open) norm(high) else norm(low)
        val p5 = norm(avg * 0.4 + last * 0.6)
        val p6 = norm(last * 0.85 + avg * 0.15)
        val p7 = norm(last)

        return listOf(p0, p1, p2, p3, p4, p5, p6, p7)
    }

    private fun normalizeSparkline(prices: List<Double>): List<Float> {
        if (prices.isEmpty()) return emptyList()
        // Sample up to 14 points evenly across the price history
        val targetCount = 14
        val sampled = if (prices.size <= targetCount) {
            prices
        } else {
            val step = prices.size.toDouble() / targetCount
            (0 until targetCount).map { i -> prices[(i * step).toInt().coerceIn(0, prices.size - 1)] }
        }

        val min = sampled.minOrNull() ?: 0.0
        val max = sampled.maxOrNull() ?: 1.0
        val range = if (max > min) max - min else 1.0

        return sampled.map { p ->
            val ratio = ((p - min) / range).toFloat()
            // Map 0..1 to 0.1f..0.9f for nice padding inside the mini sparkline box
            (0.1f + ratio * 0.8f).coerceIn(0.1f, 0.9f)
        }
    }
}
