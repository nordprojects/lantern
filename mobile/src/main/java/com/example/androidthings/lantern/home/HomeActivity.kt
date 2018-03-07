package com.example.androidthings.lantern.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.channels.ChannelsListActivity
import com.example.androidthings.lantern.configuration.ProjectorConnection
import com.example.androidthings.lantern.settings.SettingsActivity
import com.example.androidthings.lantern.shared.Direction
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity(), ProjectorDisplayFragment.OnDirectionSelectedListener {

    private val clientObserver = Observer { _, _ -> checkConnectionStatus() }
    private val connectionObserver = Observer { _, _ -> update() }
    private val projectorDisplayFragment = ProjectorDisplayFragment()
    private val lostConnectionFragment = LostConnectionFragment()
    private var connection: ProjectorConnection? = null

    companion object {
        const val DISCONNECT_ACTIVITY = "disconnect_from_projector"
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if (action == DISCONNECT_ACTIVITY) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.menu)
        toolbar.setNavigationOnClickListener { showSettings() }

        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_full_container, lostConnectionFragment)
            add(R.id.fragment_container, projectorDisplayFragment)
            hide(lostConnectionFragment)
        }.commit()

        registerReceiver(broadcastReceiver, IntentFilter(DISCONNECT_ACTIVITY))

        update()
    }

    private fun update() {
        title = App.instance.projector?.name ?: "Lantern"
        projectorDisplayFragment.projector = connection?.projectorState
    }

    override fun onResume() {
        super.onResume()
        connection = App.instance.client.activeConnection
        connection?.addObserver(connectionObserver)
        App.instance.client.addObserver(clientObserver)
        update()
        checkConnectionStatus()
    }

    override fun onPause() {
        super.onPause()
        connection?.deleteObserver(connectionObserver)
        App.instance.client.deleteObserver(clientObserver)
    }

    private fun checkConnectionStatus() {
        // Lost connection
        if (App.instance.client.activeConnection == null) {
            connection?.deleteObserver(connectionObserver)
            connection = null
            showLostConnection()
        } else {
            // New connection
            if (connection == null) {
                connection = App.instance.client.activeConnection
                connection?.addObserver(connectionObserver)
                showProjectorDisplay()
            }

            update()
        }
    }

    override fun onDirectionSelected(direction: Direction) {
        showChannelList(direction)
    }

    private fun showProjectorDisplay() {
        supportFragmentManager.beginTransaction().apply {
            show(projectorDisplayFragment)
            hide(lostConnectionFragment)
        }.commit()
    }

    private fun showLostConnection() {
        supportFragmentManager.beginTransaction().apply {
            hide(projectorDisplayFragment)
            show(lostConnectionFragment)
        }.commit()
    }

    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showChannelList(direction: Direction) {
        val intent = Intent(this, ChannelsListActivity::class.java)
        intent.putExtra(ChannelsListActivity.ARG_DIRECTION, direction.toString())
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
