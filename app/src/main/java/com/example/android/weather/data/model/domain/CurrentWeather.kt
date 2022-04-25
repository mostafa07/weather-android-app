package com.example.android.weather.data.model.domain

import com.example.android.weather.data.model.source.remote.CurrentWeatherApiResponse

data class CurrentWeather(
    val id: Int,
    val temperature: Double,
    val minTemperature: Double,
    val maxTemperature: Double,
    val feelsLike: Double,
    val city: String,
    val country: String,
    val description: String,
    val requestTime: Long
) {

    fun getRoundedTemperature(): Int {
        return this.temperature.toInt()
    }

    companion object {
        fun from(currentWeatherApiResponse: CurrentWeatherApiResponse): CurrentWeather {
            return CurrentWeather(
                id = currentWeatherApiResponse.id,
                temperature = currentWeatherApiResponse.main.temp,
                minTemperature = currentWeatherApiResponse.main.temp_min,
                maxTemperature = currentWeatherApiResponse.main.temp_max,
                feelsLike = currentWeatherApiResponse.main.feels_like,
                city = currentWeatherApiResponse.name,
                country = currentWeatherApiResponse.sys.country,
                description = currentWeatherApiResponse.weather[0].description,
                requestTime = currentWeatherApiResponse.dt
            )
        }
    }
}