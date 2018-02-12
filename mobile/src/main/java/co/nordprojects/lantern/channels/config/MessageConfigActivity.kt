package co.nordprojects.lantern.channels.config

import android.os.Bundle
import co.nordprojects.lantern.R
import kotlinx.android.synthetic.main.activity_message_config.*

class MessageConfigActivity : ChannelConfigActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_config)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)

        setChannelButton.setOnClickListener { updateConfig() }
    }

    private fun updateConfig() {
        config.settings.put("message", editText.text)
        finishWithConfigUpdate()
    }
}
