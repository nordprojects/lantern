package com.example.androidthings.lantern.channels.config

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.androidthings.lantern.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_weather_config.*
import kotlinx.android.synthetic.main.item_row_weather.view.*


class WeatherConfigActivity : ChannelConfigActivity() {

    enum class WeatherType {
        LOCATION, CALM, CALM_AND_SUNNY, RAIN, HEAVY_RAIN, GENTLE_WIND, WINDY
    }

    data class WeatherOption(
            val title: String,
            val subtitle: String,
            val icon: Int,
            val type: WeatherType)

    var items = listOf(
            WeatherOption("Lantern’s current location", "", R.drawable.ic_current_location, WeatherType.LOCATION),
            WeatherOption("Rain", "Mawsynram, India", R.drawable.ic_rain, WeatherType.RAIN),
            WeatherOption("Sunny", "Death Valley, USA", R.drawable.ic_sunny, WeatherType.CALM_AND_SUNNY),
            WeatherOption("Windy", "Galway, Ireland", R.drawable.ic_windy, WeatherType.WINDY),
            WeatherOption("Calm", "Gold Coast, Australia", R.drawable.ic_calm, WeatherType.CALM),
            WeatherOption("Breeze", "Marseille, France", R.drawable.ic_gentle_breeze, WeatherType.GENTLE_WIND)
            )

    var selectedItem: WeatherOption = items[0]
        set(value) {
            field = value
            recyclerView.adapter.notifyDataSetChanged()
        }

    private var locationProvider: FusedLocationProviderClient? = null
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_config)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WeatherListAdapter()

        setChannelButton.setOnClickListener { updateConfig() }

        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationProvider?.lastLocation?.addOnSuccessListener { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
            }
        }
    }

    private fun updateConfig() {
        if (selectedItem.type == WeatherType.LOCATION) {
            if (currentLatitude == null || currentLongitude == null) {
                Snackbar.make(container, "Current location not available", Snackbar.LENGTH_LONG).show()
                return
            } else {
                config.settings.put("latitude", currentLatitude!!)
                config.settings.put("longitude", currentLongitude!!)
                config.settings.put("subtitle", "‘Current location’")
                config.settings.put("subtitleVia", "via Open Weather Map")
            }
        } else {
            config.settings.put("weatherOverride", selectedItem.type.toString())
            config.settings.put("subtitle", "‘${selectedItem.subtitle}’")
            config.settings.put("subtitleVia", "via Open Weather Map")
        }

        finishWithConfigUpdate()
    }

    inner class WeatherListAdapter: RecyclerView.Adapter<WeatherListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row_weather, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindItem(items[position])
        }

        inner class ViewHolder(var view: View):  RecyclerView.ViewHolder(view) {

            private var item: WeatherOption? = null

            fun bindItem(item: WeatherOption) {
                this.item = item

                val spannable = SpannableString("${item.title} ${item.subtitle}")
                val textAppearance = TextAppearanceSpan(this@WeatherConfigActivity, R.style.robotoRegular)
                spannable.setSpan(textAppearance, item.title.length, spannable.length, 0)
                view.textView.text = spannable

                view.imageView.setImageResource(item.icon)
                view.radioButton.isChecked = item == selectedItem
                view.setOnClickListener { selectedItem = item }
                view.radioButton.isClickable = false
            }
        }
    }
}
