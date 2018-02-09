package co.nordprojects.lantern.search

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.util.Log
import co.nordprojects.lantern.App
import co.nordprojects.lantern.home.HomeActivity
import co.nordprojects.lantern.R
import co.nordprojects.lantern.configuration.ConfigurationClient
import co.nordprojects.lantern.configuration.ConnectionState
import kotlinx.android.synthetic.main.activity_projector_search.*

class ProjectorSearchActivity : AppCompatActivity(),
        ProjectorListFragment.OnProjectorSelectedListener,
        ConfigurationClient.ConfigurationClientUpdatedListener {

    companion object {
        val TAG: String = ProjectorSearchActivity::class.java.simpleName
        val HOME_ACTIVITY_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projector_search)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)
        toolbar.setNavigationOnClickListener {
            //TODO - restart nearby discovery and clear endpoints?
        }
    }

    override fun onPause() {
        super.onPause()
        App.instance.configClient.listener = null
    }

    override fun onResume() {
        super.onResume()
        App.instance.configClient.listener = this
        if (App.instance.configClient.connectionState == ConnectionState.UNINITIALISED) {
            App.instance.configClient.startDiscovery()
        }
        update()
    }

    override fun onConfigurationClientUpdated() {
        update()
    }

    override fun onStartDiscoveryFailure() {
        val snackBar = Snackbar.make(fragment_container, "Failed to start Nearby Connections", LENGTH_INDEFINITE)
        snackBar.setAction("Try again", { App.instance.configClient.startDiscovery() })
        snackBar.show()
    }

    override fun onRequestConnectionFailure() {
        val snackBar = Snackbar.make(fragment_container, "Failed to connect to projector", LENGTH_LONG)
        snackBar.show()
    }

    override fun onProjectorSelected(endpointID: String) {
        App.instance.configClient.connectTo(endpointID)
    }

    private fun update() {
        Log.i(TAG, "UPDATE CONNECTION ${App.instance.configClient.connectionState}")
        when (App.instance.configClient.connectionState) {
            ConnectionState.LOOKING_FOR_ENDPOINTS -> {
                showProjectorSearchFragment()
            }
            ConnectionState.ENDPOINTS_AVAILABLE -> {
                showProjectorListFragment()
            }
            ConnectionState.CONNECTING_TO_ENDPOINT -> {
                showProjectorConnectingFragment()
            }
            ConnectionState.CONNECTED -> {
                showHomeActivity()
            }
        }
    }

    private fun showProjectorSearchFragment() {
        supportActionBar?.hide()
        val searchFragment = ProjectorSearchFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, searchFragment)
        fragmentTransaction.commit()
    }

    private fun showProjectorListFragment() {
        supportActionBar?.show()
        val listFragment = ProjectorListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, listFragment)
        fragmentTransaction.commit()
    }

    private fun showProjectorConnectingFragment() {
        supportActionBar?.show()
        val connectingFragment = ProjectorConnectingFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, connectingFragment)
        fragmentTransaction.commit()
    }

    private fun showHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivityForResult(intent, HOME_ACTIVITY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HOME_ACTIVITY_REQUEST) {
            when (resultCode) {
                HomeActivity.RESULT_DISCONNECTED -> {
                    val snackBar = Snackbar.make(fragment_container, "Lost connection to projector", LENGTH_LONG)
                    snackBar.show()
                }
                Activity.RESULT_CANCELED -> {
                    App.instance.configClient.disconnect()
                }
            }
        }
    }
}
