package com.example.androidthings.lantern.connect

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.home.HomeActivity
import kotlinx.android.synthetic.main.activity_projector_search.*

class ConnectActivity : AppCompatActivity() {

    companion object {
        const val ARG_ENDPOINT_ID = "ARG_ENDPOINT_ID"
        const val ARG_NAME = "ARG_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val name = savedInstanceState?.getString(ARG_NAME)
        showProjectorConnectingFragment(name ?: "Lantern")

        val endpointId = intent.getStringExtra(ARG_ENDPOINT_ID)
        if (endpointId != null) {
            App.instance.client.connectTo(endpointId, { showHomeActivity() }, { onRequestConnectionFailure() })
        }
    }

    private fun onRequestConnectionFailure() {
        val snackBar = Snackbar.make(fragment_container, "Failed to connect to projector", Snackbar.LENGTH_LONG)
        snackBar.show()
        finish()
    }

    private fun showProjectorConnectingFragment(name: String) {
        val connectingFragment = ProjectorConnectingFragment()
        connectingFragment.arguments = Bundle().apply { putString(ARG_NAME, name) }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, connectingFragment)
        fragmentTransaction.commit()
    }

    private fun showHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
