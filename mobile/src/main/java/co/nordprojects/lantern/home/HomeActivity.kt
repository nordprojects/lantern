package co.nordprojects.lantern.home

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import co.nordprojects.lantern.App
import co.nordprojects.lantern.R
import co.nordprojects.lantern.channels.ChannelsListActivity
import co.nordprojects.lantern.search.ProjectorSearchActivity
import co.nordprojects.lantern.settings.SettingsActivity
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_projector_display.*
import java.nio.channels.Channels
import java.util.*

class HomeActivity : AppCompatActivity(), ProjectorDisplayFragment.OnDirectionSelectedListener {

    private val connectionObserver: Observer = Observer { _, _ -> onConnectionChanged() }

    companion object {
        val TAG: String = HomeActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "ON CREATE HOME")

        setContentView(R.layout.activity_home)

        settings_button.setOnClickListener { showSettings() }

        val projectorFragment = ProjectorDisplayFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, projectorFragment)
        fragmentTransaction.commit()
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "Home activity on resume")
        App.instance.configClient.addObserver(connectionObserver)
    }

    override fun onPause() {
        super.onPause()
        App.instance.configClient.deleteObserver(connectionObserver)
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "ON STOP")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "ON DESTROY")
    }

    private fun onConnectionChanged() {
        Log.i(TAG, "onConnectionChanged ")
        if (App.instance.projector == null) {
            Log.i(TAG, "projector null ")
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
        intent.putExtra("direction", direction.toString())
        startActivity(intent)
    }

    private fun showProjectorSearchOnDisconnect() {
        setResult(-1)
        finish()
    }
}
