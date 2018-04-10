package com.example.androidthings.lantern.channels.config

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.shared.ChannelConfiguration
import java.util.Observer

/**
 * Base class for channel configuration activities. Subclass this and set properties on config,
 * before calling finishWithConfigUpdate().
 *
 * Created by Michael Colville on 12/02/2018.
 */

open class ChannelConfigActivity : AppCompatActivity() {

    private val connectionObserver = Observer { _, _ -> checkConnectionStatus() }

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

    override fun onResume() {
        super.onResume()
        App.instance.client.addObserver(connectionObserver)
        checkConnectionStatus()
    }

    override fun onPause() {
        super.onPause()
        App.instance.client.deleteObserver(connectionObserver)
    }

    private fun checkConnectionStatus() {
        if (App.instance.client.activeConnection == null) {
            finish()
        }
    }
}
