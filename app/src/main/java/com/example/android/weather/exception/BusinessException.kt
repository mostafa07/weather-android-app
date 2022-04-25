package com.example.android.weather.exception

import com.example.android.weather.data.model.app.CustomMessage

class BusinessException(val businessMessage: CustomMessage) : Exception()