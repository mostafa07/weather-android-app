package com.example.android.weather.ui.util

import android.app.Activity
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.android.weather.data.model.app.CustomMessage
import com.google.android.material.snackbar.Snackbar

fun Activity.showSnackbar(rootView: View, message: CustomMessage, successFlag: Boolean) {
    val messageString = getString(message.messageResourceId, message.params)

    Snackbar.make(rootView, messageString, Snackbar.LENGTH_LONG)
        .setBackgroundTint(
            ContextCompat.getColor(
                applicationContext,
                if (successFlag) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            )
        )
        .setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
        .show()
}

fun Fragment.showSnackbar(rootView: View, message: CustomMessage, successFlag: Boolean) {
    activity?.showSnackbar(rootView, message, successFlag)
}

fun Activity.disableUserInteraction() {
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}

fun Activity.reEnableUserInteraction() {
    window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun Fragment.disableUserInteraction() {
    activity?.disableUserInteraction()
}

fun Fragment.reEnableUserInteraction() {
    activity?.reEnableUserInteraction()
}