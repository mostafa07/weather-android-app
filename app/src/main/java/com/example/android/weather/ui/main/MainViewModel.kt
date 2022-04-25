package com.example.android.weather.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.weather.R
import com.example.android.weather.data.model.app.CustomMessage
import com.example.android.weather.data.model.domain.CurrentWeather
import com.example.android.weather.data.repository.CurrentWeatherRepository
import com.example.android.weather.exception.BusinessException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private var _currentWeather: MutableLiveData<CurrentWeather?> = MutableLiveData(null)
    val currentWeather: LiveData<CurrentWeather?>
        get() = _currentWeather

    private val _successMessage: MutableLiveData<CustomMessage> = MutableLiveData()
    val successMessage: LiveData<CustomMessage>
        get() = _successMessage

    private val _errorMessage: MutableLiveData<CustomMessage> = MutableLiveData()
    val errorMessage: LiveData<CustomMessage>
        get() = _errorMessage

    private val _isContentLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isContentLoading: LiveData<Boolean>
        get() = _isContentLoading


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    fun getCurrentWeather(latitude: Number, longitude: Number) {
        viewModelScope.launch {
            showLoading()
            try {
                val response = CurrentWeatherRepository.retrieveCurrentWeather(
                    latitude = latitude,
                    longitude = longitude
                )
                _currentWeather.value = response
            } catch (exception: Exception) {
                _currentWeather.value = null
                setErrorMessage(exception)
            }
            hideLoading()
        }
    }

    fun getCurrentWeather(cityName: String) {
        viewModelScope.launch {
            showLoading()
            try {
                val response = CurrentWeatherRepository.retrieveCurrentWeather(cityName = cityName)
                _currentWeather.value = response
            } catch (exception: Exception) {
                _currentWeather.value = null
                setErrorMessage(exception)
            }
            hideLoading()
        }
    }


    private fun setSuccessMessage(message: CustomMessage) {
        _successMessage.value = message
    }

    private fun setErrorMessage(errorMessage: CustomMessage) {
        _errorMessage.value = errorMessage
    }

    private fun setErrorMessage(t: Throwable) {
        if (t is BusinessException) {
            setErrorMessage(t.businessMessage)
        } else {
            t.printStackTrace()
            setErrorMessage(CustomMessage(R.string.operation_failed))
        }
    }

    private fun showLoading() {
        _isContentLoading.value = true
    }

    private fun hideLoading() {
        _isContentLoading.value = false
    }
}