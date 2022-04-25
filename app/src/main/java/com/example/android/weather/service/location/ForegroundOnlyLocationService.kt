package com.example.android.weather.service.location

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.android.weather.R
import com.example.android.weather.ui.main.MainActivity
import com.example.android.weather.util.toText
import com.google.android.gms.location.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ForegroundOnlyLocationService : Service() {

    private val mLocalBinder = LocalBinder()

    private var mIsConfigurationChange = false
    private var mIsServiceRunningInForeground = false

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback

    private var mCurrentLocation: Location? = null

    @SuppressLint("LongLogTag")
    override fun onCreate() {
        Timber.d("OnCreate")
        super.onCreate()

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(30)
            fastestInterval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(30)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                mCurrentLocation = locationResult.lastLocation

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, mCurrentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                if (mIsServiceRunningInForeground) {
                    mNotificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(mCurrentLocation)
                    )
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")

        val cancelLocationTrackingFromNotification = intent.getBooleanExtra(
            EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false
        )
        if (cancelLocationTrackingFromNotification) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }

        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    @SuppressLint("LongLogTag")
    override fun onBind(intent: Intent): IBinder {
        Timber.d("onBind")

        // MainActivity (client) comes into foreground and binds to service, so the service can become a background services.
        stopForeground(true)
        mIsServiceRunningInForeground = false
        mIsConfigurationChange = false
        return mLocalBinder
    }

    @SuppressLint("LongLogTag")
    override fun onRebind(intent: Intent?) {
        Timber.d("onRebind")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service can become a background services.
        stopForeground(true)
        mIsServiceRunningInForeground = false
        mIsConfigurationChange = false
        super.onRebind(intent)
    }

    @SuppressLint("LongLogTag")
    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity, we do nothing.
        if (!mIsConfigurationChange) {
            Timber.d("Start foreground service")
            val notification = generateNotification(mCurrentLocation)
            startForeground(NOTIFICATION_ID, notification)
            mIsServiceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    @SuppressLint("LongLogTag")
    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
    }

    @SuppressLint("LongLogTag")
    override fun onConfigurationChanged(newConfig: Configuration) {
        Timber.d("onConfigurationChanged")
        super.onConfigurationChanged(newConfig)
        mIsConfigurationChange = true
    }

    @SuppressLint("LongLogTag")
    fun subscribeToLocationUpdates(isRealtimeMode: Boolean) {
        Timber.d("SubscribeToLocationUpdates")

        startService(Intent(applicationContext, ForegroundOnlyLocationService::class.java))

        mLocationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            fastestInterval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            if (!isRealtimeMode) numUpdates = 1
        }

        try {
            mFusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    @SuppressLint("LongLogTag")
    fun unsubscribeToLocationUpdates() {
        Timber.d("UnsubscribeToLocationUpdates")

        try {
            val removeTask = mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Location Callback removed.")
                    stopSelf()
                } else {
                    Timber.d("Failed to remove Location Callback.")
                }
            }
        } catch (unlikely: SecurityException) {
            Timber.e("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    @SuppressLint("LongLogTag")
    private fun generateNotification(location: Location?): Notification {
        Timber.d("generateNotification()")

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val mainNotificationText = location?.toText() ?: getString(R.string.no_location_text)
        val titleText = getString(R.string.app_name)

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT
            )

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            mNotificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val cancelIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0
        )

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launch, getString(R.string.launch_activity),
                activityPendingIntent
            )
            .addAction(
                R.drawable.ic_cancel,
                getString(R.string.stop_location_updates_button_text),
                servicePendingIntent
            )
            .build()
    }


    inner class LocalBinder : Binder() {
        internal val service: ForegroundOnlyLocationService
            get() = this@ForegroundOnlyLocationService
    }

    companion object {
        private const val PACKAGE_NAME = "com.example.emiratesauctionsweatherapp"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 12345678
        private const val NOTIFICATION_CHANNEL_ID = "emiratesauctionsweatherapp_channel_01"
    }
}

