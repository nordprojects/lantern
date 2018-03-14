package com.example.androidthings.lantern.channels.config

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.SpannableString
import android.text.style.UnderlineSpan
import com.example.androidthings.lantern.LocationConverter
import com.example.androidthings.lantern.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_space_config.*

class SpaceConfigActivity : ChannelConfigActivity() {

    private var locationProvider: FusedLocationProviderClient? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_space_config)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)

        setChannelButton.setOnClickListener { updateConfig() }

        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationProvider?.lastLocation?.addOnSuccessListener {
            currentLatitude = it.latitude
            currentLongitude = it.longitude
            update()
        }
    }

    private fun update() {
        val lat = LocationConverter.latitudeAsDMS(currentLatitude ?: 0.0, 4)
        val long = LocationConverter.longitudeAsDMS(currentLongitude ?: 0.0, 4)
        val span = SpannableString("$lat\n$long")
        span.setSpan(UnderlineSpan(), 0, span.length, 0)
        locationTextView.text = span
    }

    private fun updateConfig() {
        if (currentLatitude == null || currentLongitude == null) {
            Snackbar.make(container, "Current location not available", Snackbar.LENGTH_LONG).show()
            return
        } else {
            config.settings.put("latitude", currentLatitude!!)
            config.settings.put("longitude", currentLongitude!!)
            config.settings.put("subtitle", "‘Stars and Constellations’")
            config.settings.put("subtitleVia", "via Processing")
        }

        finishWithConfigUpdate()
    }
}
