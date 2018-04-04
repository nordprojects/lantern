package com.example.androidthings.lantern.channels

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.view.Menu
import android.view.MenuItem
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.channels.config.ChannelConfigActivity
import com.example.androidthings.lantern.channels.config.ChannelConfigOptions
import com.example.androidthings.lantern.shared.ChannelConfiguration
import com.example.androidthings.lantern.shared.ChannelInfo
import com.example.androidthings.lantern.shared.Direction
import com.example.androidthings.lantern.shared.Rotation
import kotlinx.android.synthetic.main.activity_channels_list.*
import java.util.*

class ChannelsListActivity : AppCompatActivity(),
        ChannelListFragment.OnChannelSelectedListener {

    private var direction: Direction = Direction.FORWARD
    private var projectorObserver = Observer({_, _ -> projectorDidUpdate()})
    private val connectionObserver = Observer { _, _ -> checkConnectionStatus() }
    private var flipMenuItem: MenuItem? = null

    companion object {
        const val CONFIG_ACTIVITY_REQUEST = 1
        const val ARG_DIRECTION = "direction"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels_list)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)

        val directionString = intent.getStringExtra(ARG_DIRECTION)
        if (directionString != null) {
            direction = Direction.valueOf(directionString)

            val channelFragment = ChannelListFragment()
            val args = Bundle()
            args.putString(ARG_DIRECTION, directionString)
            channelFragment.arguments = args
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragmentContainer, channelFragment)
                commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.channel_list_menu, menu)
        flipMenuItem = menu.findItem(R.id.action_flip)
        updateFlipIcon()
        return true
    }

    override fun onResume() {
        super.onResume()
        App.instance.client.addObserver(connectionObserver)
        App.instance.client.activeConnection?.addObserver(projectorObserver)
        checkConnectionStatus()
    }

    override fun onPause() {
        super.onPause()
        App.instance.client.deleteObserver(connectionObserver)
        App.instance.client.activeConnection?.deleteObserver(projectorObserver)
    }

    private fun checkConnectionStatus() {
        if (App.instance.client.activeConnection == null) {
            finish()
        }
    }

    override fun onChannelSelected(channel: ChannelInfo) {
        val projector = App.instance.projector ?: return
        val currentConfig = projector.planes[direction] ?: return
        val config = ChannelConfiguration(channel.id, currentConfig.rotation)
        val subtitle = ChannelConfigOptions.channelTypeToSubtitle[channel.id]
        if (subtitle != null) {
            config.settings.put("subtitle", subtitle)
        }

        if (channel.customizable) {
            val activityClass = ChannelConfigOptions.channelTypeToActivity[channel.id]
            if (activityClass == null) {
                val snackBar = Snackbar.make(fragmentContainer, "Channel can be customized. Please update this app.", LENGTH_LONG)
                snackBar.setAction("Set Anyway", { sendConfig(config) })
                snackBar.show()
            } else {
                val intent = Intent(this, activityClass)
                intent.putExtra(ChannelConfigActivity.ARG_CONFIG, config)
                startActivityForResult(intent, CONFIG_ACTIVITY_REQUEST)
            }
        } else {
            sendConfig(config)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CONFIG_ACTIVITY_REQUEST) {
            when (resultCode) {
                ChannelConfigActivity.RESULT_CONFIG_SET -> {
                    val config = data?.getParcelableExtra<ChannelConfiguration>(ChannelConfigActivity.ARG_CONFIG)
                    if (config != null) {
                        sendConfig(config)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_flip -> {
            flipRotation()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun sendConfig(config: ChannelConfiguration) {
        val connection = App.instance.client.activeConnection!!
        connection.sendSetPlane(direction, config)

    }

    private fun projectorDidUpdate() {
        updateFlipIcon()
    }

    private fun updateFlipIcon() {
        val projector = App.instance.projector ?: return
        val config = projector.planes[direction] ?: return
        val flipMenuItem = flipMenuItem ?: return

        when(config.rotation) {
            Rotation.LANDSCAPE -> flipMenuItem.setIcon(R.drawable.ic_flip)
            Rotation.LANDSCAPE_UPSIDE_DOWN -> flipMenuItem.setIcon(R.drawable.ic_flip_upside_down)
        }
    }

    private fun flipRotation() {
        val projector = App.instance.projector ?: return
        val config = projector.planes[direction] ?: return
        val newRotation = when(config.rotation) {
            Rotation.LANDSCAPE -> Rotation.LANDSCAPE_UPSIDE_DOWN
            Rotation.LANDSCAPE_UPSIDE_DOWN -> Rotation.LANDSCAPE
        }
        val newConfig = config.copy(rotation = newRotation)
        sendConfig(newConfig)
    }
}
