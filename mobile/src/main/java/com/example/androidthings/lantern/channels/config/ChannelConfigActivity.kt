package com.example.androidthings.lantern.channels.config

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.example.androidthings.lantern.shared.ChannelConfiguration

/**
 * Created by Michael Colville on 12/02/2018.
 */

open class ChannelConfigActivity : AppCompatActivity() {
    val config: ChannelConfiguration by lazy {
        intent.getParcelableExtra<ChannelConfiguration>(ARG_CONFIG)
    }

    companion object {
        const val ARG_CONFIG = "config"
        const val RESULT_CONFIG_SET = 2
    }

    open fun finishWithConfigUpdate() {
        val data = Intent()
        data.putExtra(ChannelConfigActivity.ARG_CONFIG, config)
        setResult(RESULT_CONFIG_SET, data)
        finish()
    }
}
