package com.example.androidthings.lantern.search

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v7.app.AppCompatActivity
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.configuration.Discovery
import com.example.androidthings.lantern.connect.ConnectActivity
import kotlinx.android.synthetic.main.activity_projector_search.*
import java.util.*

class ProjectorSearchActivity : AppCompatActivity(),
        ProjectorListFragment.OnProjectorSelectedListener,
        Discovery.DiscoveryFailureListener {

    private val discoveryObserver: Observer = Observer { _, _ -> onDiscoveryUpdated() }
    private var currentFragment: Fragment? = null

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
        App.instance.discovery.failureListener = this
        App.instance.discovery.startDiscovery()
        update()
    }

    override fun onPause() {
        super.onPause()
        App.instance.discovery.deleteObserver(discoveryObserver)
        App.instance.discovery.stopDiscovery()
    }

    private fun onDiscoveryUpdated() {
        update()
    }

    override fun onStartDiscoveryFailure() {
        val snackBar = Snackbar.make(fragment_container, "Failed to start Nearby Connections", LENGTH_INDEFINITE)
        snackBar.setAction("Try again", { App.instance.discovery.startDiscovery() })
        snackBar.show()
    }

    override fun onProjectorSelected(endpoint: Discovery.Endpoint) {
        val intent = Intent(this, ConnectActivity::class.java).apply {
            putExtra(ConnectActivity.ARG_ENDPOINT_ID, endpoint.id)
            putExtra(ConnectActivity.ARG_NAME, endpoint.info.endpointName)
        }
        startActivity(intent)
    }

    private fun update() {
        if (App.instance.discovery.endpoints.isEmpty()) {
            showProjectorSearchFragment()
        } else {
            showProjectorListFragment()
        }
    }

    private fun showProjectorSearchFragment() {
        if (currentFragment is ProjectorSearchFragment) return
        supportActionBar?.hide()
        currentFragment = ProjectorSearchFragment()
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
