package com.example.android.weather.data.repository

import com.example.android.weather.BuildConfig
import com.example.android.weather.data.model.domain.CurrentWeather
import com.example.android.weather.webservice.CurrentWeatherWebService
import com.example.android.weather.webservice.builder.RetrofitServiceBuilder

object CurrentWeatherRepository {

    private val currentWeatherWebService: CurrentWeatherWebService =
        RetrofitServiceBuilder.buildService(CurrentWeatherWebService::class.java)


    suspend fun retrieveCurrentWeather(latitude: Number, longitude: Number): CurrentWeather {
        return CurrentWeather.from(
            currentWeatherWebService.getCurrentWeather(
                latitude = latitude.toInt(),
                longitude = longitude.toInt(),
                units = "metric",
                appId = BuildConfig.OPEN_WEATHER_MAP_API_KEY
            )
        )
    }

    suspend fun retrieveCurrentWeather(cityName: String): CurrentWeather {
        return CurrentWeather.from(
            currentWeatherWebService.getCurrentWeather(
                cityName = cityName,
                units = "metric",
                appId = BuildConfig.OPEN_WEATHER_MAP_API_KEY
            )
        )
    }
}