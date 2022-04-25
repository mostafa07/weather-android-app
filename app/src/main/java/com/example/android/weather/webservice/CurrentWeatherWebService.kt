package com.example.android.weather.webservice

import com.example.android.weather.data.model.source.remote.CurrentWeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrentWeatherWebService {

    @GET(CURRENT_WEATHER_END_POINT)
    suspend fun getCurrentWeather(
        @Query(LATITUDE_QUERY_PARAM) latitude: Int,
        @Query(LONGITUDE_QUERY_PARAM) longitude: Int,
        @Query(UNITS_QUERY_PARAM) units: String,
        @Query(APP_ID_QUERY_PARAM) appId: String
    ): CurrentWeatherApiResponse

    @GET(CURRENT_WEATHER_END_POINT)
    suspend fun getCurrentWeather(
        @Query(CITY_NAME_QUERY_PARAM) cityName: String,
        @Query(UNITS_QUERY_PARAM) units: String,
        @Query(APP_ID_QUERY_PARAM) appId: String
    ): CurrentWeatherApiResponse

    companion object {
        const val CURRENT_WEATHER_END_POINT = "weather"

        const val APP_ID_QUERY_PARAM = "appid"
        const val UNITS_QUERY_PARAM = "units"
        const val LATITUDE_QUERY_PARAM = "lat"
        const val LONGITUDE_QUERY_PARAM = "lon"

        const val CITY_NAME_QUERY_PARAM = "q"
    }
}