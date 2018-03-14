package com.example.androidthings.lantern.channels.config

import android.os.Bundle
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.shared.CastDiscoveryManager
import kotlinx.android.synthetic.main.activity_now_playing_config.*


class NowPlayingConfigActivity: ChannelConfigActivity(),
        NowPlayingCastFragment.OnCastDeviceSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_now_playing_config)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)
        toolbar.setNavigationOnClickListener { finish() }
        title = "Customize"

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, NowPlayingCastFragment())
        }.commit()
    }

    override fun onCastDeviceSelectedSelected(device: CastDiscoveryManager.CastDevice?) {
        val device = device ?: return
        config.settings.put("castId", device.id)
        config.settings.put("subtitle", "‘${device.name}’")
        config.settings.put("subtitleVia", "via Chromecast Audio")

        finishWithConfigUpdate()
    }
}
