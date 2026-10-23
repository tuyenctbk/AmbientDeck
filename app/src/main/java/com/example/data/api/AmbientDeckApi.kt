package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoForecastResponse
}

interface OpenMeteoAirQualityApi {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "european_aqi,us_aqi,pm10,pm2_5"
    ): OpenMeteoAirQualityResponse
}

interface BinanceApi {
    @GET("api/v3/ticker/24hr")
    suspend fun get24hTickers(
        @Query("symbols") symbolsJsonArray: String
    ): List<BinanceTickerResponse>
}

interface OpenSkyApi {
    @GET("api/states/all")
    suspend fun getStates(
        @Query("lamin") lamin: Double,
        @Query("lomin") lomin: Double,
        @Query("lamax") lamax: Double,
        @Query("lomax") lomax: Double
    ): OpenSkyResponse
}

interface MuffinLabsApi {
    @GET("date")
    suspend fun getTodayInHistory(): MuffinLabsHistoryResponse
}

object NetworkClientProvider {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val openMeteoForecastApi: OpenMeteoForecastApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoForecastApi::class.java)
    }

    val openMeteoAirQualityApi: OpenMeteoAirQualityApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://air-quality-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoAirQualityApi::class.java)
    }

    val binanceApi: BinanceApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BinanceApi::class.java)
    }

    val openSkyApi: OpenSkyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://opensky-network.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenSkyApi::class.java)
    }

    val muffinLabsApi: MuffinLabsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://history.muffinlabs.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MuffinLabsApi::class.java)
    }
}
