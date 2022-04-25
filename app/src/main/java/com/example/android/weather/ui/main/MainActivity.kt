package com.example.android.weather.ui.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android.weather.R
import com.example.android.weather.databinding.ActivityMainBinding
import com.example.android.weather.ui.util.disableUserInteraction
import com.example.android.weather.ui.util.reEnableUserInteraction
import com.example.android.weather.ui.util.showSnackbar

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        handleIntent(intent)

        setupViewModelObservations()

        setContentView(binding.root)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
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
    }
}