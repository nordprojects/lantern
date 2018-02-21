package com.example.androidthings.lantern.channels.config

import android.os.Bundle
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.activity_calendar_config.*

class CalendarConfigActivity : ChannelConfigActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_config)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)

        setChannelButton.setOnClickListener { updateConfig() }
    }

    private fun updateConfig() {
        config.secrets!!.put("url", editText.text)
        finishWithConfigUpdate()
    }
}
