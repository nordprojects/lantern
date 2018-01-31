package co.nordprojects.lantern.search

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.nordprojects.lantern.App
import co.nordprojects.lantern.home.HomeActivity
import co.nordprojects.lantern.R
import co.nordprojects.lantern.configuration.ConfigurationClient
import co.nordprojects.lantern.configuration.ConnectionState

class ProjectorSearchActivity : AppCompatActivity(),
        ProjectorSelectFragment.OnProjectorSelectedListener,
        ConfigurationClient.ConfigurationClientUpdatedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projector_search)
        App.instance.configClient.listener = this
        update()
    }

    override fun onPause() {
        super.onPause()
        App.instance.configClient.listener = null
    }

    override fun onResume() {
        super.onResume()
        App.instance.configClient.listener = this
    }

    override fun onConfigurationClientUpdated() {
        update()
    }

    override fun onProjectorSelected(endpointID: String) {
        App.instance.configClient.connectTo(endpointID)
    }

    private fun update() {
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
            else -> { throw IllegalArgumentException("Can't handle connection state ${App.instance.configClient.connectionState}") }
        }
    }

    private fun showProjectorSearchFragment() {
        val searchFragment = ProjectorSearchFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, searchFragment)
        fragmentTransaction.commit()
    }

    private fun showProjectorListFragment() {
        val listFragment = ProjectorSelectFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, listFragment)
        fragmentTransaction.commit()
    }

    private fun showProjectorConnectingFragment() {
        val connectingFragment = ProjectorConnectingFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, connectingFragment)
        fragmentTransaction.commit()
    }

    private fun showHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}
