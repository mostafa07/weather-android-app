package com.example.android.weather.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.Menu
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.android.weather.BuildConfig
import com.example.android.weather.R
import com.example.android.weather.databinding.ActivityMainBinding
import com.example.android.weather.service.location.ForegroundOnlyLocationService
import com.example.android.weather.ui.util.disableUserInteraction
import com.example.android.weather.ui.util.reEnableUserInteraction
import com.example.android.weather.ui.util.showSnackbar
import com.example.android.weather.util.toText
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private lateinit var searchView: SearchView

    private var mIsForegroundOnlyLocationServiceBound = false
    private var mForegroundOnlyLocationService: ForegroundOnlyLocationService? = null
    private var mForegroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver? = null

    // Monitors connection to the service
    private val mForegroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected")

            val binder = service as ForegroundOnlyLocationService.LocalBinder
            mForegroundOnlyLocationService = binder.service
            mIsForegroundOnlyLocationServiceBound = true

            initLocationData()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.d("onServiceDisconnected")

            mForegroundOnlyLocationService = null
            mIsForegroundOnlyLocationServiceBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.executePendingBindings()

        handleIntent(intent)

        setupViewModelObservations()

        mForegroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
        initLocationData()

        setContentView(binding.root)
    }

    override fun onStart() {
        Timber.d("onStart")
        super.onStart()

        val serviceIntent = Intent(this@MainActivity, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, mForegroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mForegroundOnlyBroadcastReceiver!!,
            IntentFilter(ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onPause() {
        Timber.d("onPause")

        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(mForegroundOnlyBroadcastReceiver!!)

        super.onPause()
    }

    override fun onStop() {
        Timber.d("onStop")

        if (mIsForegroundOnlyLocationServiceBound) {
            unbindService(mForegroundOnlyServiceConnection)
            mIsForegroundOnlyLocationServiceBound = false
        }

        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        searchView = menu.findItem(R.id.search).actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconified = false
        }

        return true
    }


    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            query?.let { viewModel.getCurrentWeather(it) }
        }
    }

    private fun setupViewModelObservations() {
        viewModel.successMessage.observe(this) {
            showSnackbar(binding.root, it, true)
        }

        viewModel.errorMessage.observe(this) {
            showSnackbar(binding.root, it, false)
        }

        viewModel.isContentLoading.observe(this) { isLoading ->
            binding.shimmerLayout.shimmerFrameLayout.showShimmer(isLoading)

            if (isLoading) {
                disableUserInteraction()
            } else {
                reEnableUserInteraction()
            }
        }

        viewModel.currentWeather.observe(this) {
            it?.let {
                searchView.apply {
                    isIconified = false
                    setQuery(it.city, false)
                }
            }
        }
    }

    private fun initLocationData() {
        Timber.d("initLocationData")

        if (isForegroundPermissionApproved()) {
            subscribeToLocationUpdatesBasedOnMode()
        } else {
            requestForegroundPermissions()
        }
    }

    private fun isForegroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        val provideRationale = isForegroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide additional rationale.
        if (provideRationale) {
            Snackbar.make(
                binding.root,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you receive empty arrays.
                    Timber.d("User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    subscribeToLocationUpdatesBasedOnMode()
                }
                else -> {
                    // Permission denied.
                    Snackbar.make(
                        binding.root,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) { openAppSettingsScreen() }
                        .show()
                }
            }
        }
    }

    private fun subscribeToLocationUpdatesBasedOnMode() {
        val isRealTimeMode = false
        mForegroundOnlyLocationService?.subscribeToLocationUpdates(isRealTimeMode)
            ?: Timber.d("Service Not Bound")
    }

    private fun openAppSettingsScreen() {
        // Build intent that displays the App settings screen.
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }


    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Timber.d(" ForegroundOnlyBroadcastReceiver : onReceive")

            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            Timber.wtf("Foreground location Received: ${location.toText()}")

            location?.let {
                viewModel.getCurrentWeather(
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        }
    }


    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 1312
    }
}