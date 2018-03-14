package com.example.androidthings.lantern.channels.config

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
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

        val text = "Get a public URL in iCal format from your Google Calendar"
        val settingsLinkText = "settings page"
        val spannable = SpannableString("$text $settingsLinkText.")
        spannable.setSpan(UnderlineSpan(), text.length + 1, text.length + 1 + settingsLinkText.length,0)
        settingsTextView.text = spannable

        settingsTextView.setOnClickListener { onSettingsLinkClicked() }
    }

    private fun updateConfig() {
        config.secrets!!.put("url", editText.text)
        finishWithConfigUpdate()
    }

    private fun onSettingsLinkClicked() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://support.google.com/calendar/answer/37083?hl=en"))
        startActivity(intent)
    }
}
