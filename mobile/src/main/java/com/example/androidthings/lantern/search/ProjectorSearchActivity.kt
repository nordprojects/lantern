package com.example.androidthings.lantern.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.configuration.Discovery
import com.example.androidthings.lantern.connect.ConnectActivity
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
        App.instance.discovery.startDiscovery { onStartDiscoveryFailure() }
    }

    private fun onStartDiscoveryFailure() {
        showProjectorSearchFragment()
        searchFragment?.showError()
    }

    override fun onTryAgainClicked() {
        startDiscovery()
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
