package com.example.androidthings.lantern.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.configuration.Discovery
import com.example.androidthings.lantern.connect.ConnectActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import kotlinx.android.synthetic.main.activity_projector_search.*
import java.util.*

class ProjectorSearchActivity : AppCompatActivity(),
        ProjectorListFragment.OnProjectorSelectedListener,
         ProjectorSearchFragment.SearchFragmentListener {

    private val discoveryObserver: Observer = Observer { _, _ -> update() }
    private var currentFragment: Fragment? = null
    private var searchFragment: ProjectorSearchFragment? = null
    private val startTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projector_search)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)
    }

    override fun onResume() {
        super.onResume()
        App.instance.discovery.addObserver(discoveryObserver)
        startDiscovery()
        update()
    }

    override fun onPause() {
        super.onPause()
        App.instance.discovery.deleteObserver(discoveryObserver)
        App.instance.discovery.stopDiscovery()
    }

    private fun startDiscovery() {
        showProjectorSearchFragment()
        searchFragment?.showSearch()
        App.instance.discovery.startDiscovery { error ->
            onStartDiscoveryFailure(error)
        }
    }

    private fun onStartDiscoveryFailure(error: Exception) {
        showProjectorSearchFragment()
        when(error) {
            is Discovery.TimeoutException -> { searchFragment?.showTimeoutError() }
            is ApiException -> {
                when(error.statusCode) {
                    ConnectionsStatusCodes.MISSING_PERMISSION_ACCESS_COARSE_LOCATION -> {
                        searchFragment?.showPermissionsError()
                    }
                    ConnectionsStatusCodes.STATUS_BLUETOOTH_ERROR -> {
                        searchFragment?.showBluetoothError()
                    }
                    else -> { searchFragment?.showUnknownError() }
                }
            }
            else -> { searchFragment?.showUnknownError() }
        }
    }

    override fun onTryAgainClicked() {
        startDiscovery()
    }

    override fun onSettingsClicked() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    }

    override fun onProjectorSelected(endpoint: Discovery.Endpoint) {
        val intent = Intent(this, ConnectActivity::class.java).apply {
            putExtra(ConnectActivity.ARG_ENDPOINT_ID, endpoint.id)
            putExtra(ConnectActivity.ARG_NAME, endpoint.info.endpointName)
        }
        startActivity(intent)
    }

    private fun update() {
        // Delay showing list to allow search animation to play out
        if (System.currentTimeMillis() - startTime < 4250) {
            Handler().postDelayed({ update() }, 1000)
            return
        }

        if (App.instance.discovery.endpoints.size > 0) {
            showProjectorListFragment()
        }
    }

    private fun showProjectorSearchFragment() {
        if (currentFragment is ProjectorSearchFragment) return
        supportActionBar?.hide()
        searchFragment = ProjectorSearchFragment()
        currentFragment = searchFragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, currentFragment)
        }.commit()
    }

    private fun showProjectorListFragment() {
        if (currentFragment is ProjectorListFragment) return
        supportActionBar?.show()
        currentFragment = ProjectorListFragment()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, currentFragment)
        }.commit()
    }
}
