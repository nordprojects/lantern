package com.example.androidthings.lantern.settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.configuration.ConnectionState
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.Observer

class SettingsActivity : AppCompatActivity() {

    private val connectionObserver = Observer { _, _ -> checkConnectionStatus() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)

        val settingsFragment = SettingsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, settingsFragment)
        fragmentTransaction.commit()
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
        if (App.instance.client.connectionState == ConnectionState.DISCONNECTED) {
            finish()
        }
    }
}
