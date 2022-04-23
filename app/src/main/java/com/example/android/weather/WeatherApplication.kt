package com.example.android.weather

import android.app.Application
import androidx.viewbinding.BuildConfig
import timber.log.Timber

class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}