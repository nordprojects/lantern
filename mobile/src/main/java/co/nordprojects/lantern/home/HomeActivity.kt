package co.nordprojects.lantern.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.nordprojects.lantern.App
import co.nordprojects.lantern.R
import co.nordprojects.lantern.channels.ChannelsListActivity
import co.nordprojects.lantern.configuration.ConnectionState
import co.nordprojects.lantern.settings.SettingsActivity
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity(), ProjectorDisplayFragment.OnDirectionSelectedListener {

    private val connectionObserver: Observer = Observer { _, _ -> onConnectionChanged() }
    private val projectorConfigObserver = Observer { _, _ -> projectorConfigUpdated() }

    companion object {
        private val TAG: String = HomeActivity::class.java.simpleName
        const val RESULT_DISCONNECTED = 2
        const val DISCONNECT_ACTIVITY = "disconnect_from_projector"
        const val ARG_DIRECTION = "direction"
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
        App.instance.projector?.let {
            title = it.name
        }

        val projectorFragment = ProjectorDisplayFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, projectorFragment)
        fragmentTransaction.commit()

        registerReceiver(broadcastReceiver, IntentFilter(DISCONNECT_ACTIVITY))
    }

    private fun projectorConfigUpdated() {
        App.instance.projector?.let {
            title = it.name
        }
    }

    override fun onResume() {
        super.onResume()
        App.instance.projector?.addObserver(projectorConfigObserver)
        App.instance.client.addObserver(connectionObserver)
        projectorConfigUpdated()
        checkConnectionStatus()
    }

    override fun onPause() {
        super.onPause()
        App.instance.projector?.deleteObserver(projectorConfigObserver)
        App.instance.client.deleteObserver(connectionObserver)
    }

    private fun onConnectionChanged() {
        checkConnectionStatus()
    }

    private fun checkConnectionStatus() {
        if (App.instance.client.connectionState == ConnectionState.DISCONNECTED) {
            showProjectorSearchOnDisconnect()
        }
    }

    override fun onDirectionSelected(direction: Direction) {
        showChannelList(direction)
    }

    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showChannelList(direction: Direction) {
        val intent = Intent(this, ChannelsListActivity::class.java)
        intent.putExtra(ARG_DIRECTION, direction.toString())
        startActivity(intent)
    }

    private fun showProjectorSearchOnDisconnect() {
        setResult(RESULT_DISCONNECTED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}
