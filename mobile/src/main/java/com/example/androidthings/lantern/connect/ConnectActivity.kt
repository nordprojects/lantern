package com.example.androidthings.lantern.connect

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.home.HomeActivity

class ConnectActivity : AppCompatActivity(),
    ProjectorConnectingFragment.ConnectingFragmentListener {

    companion object {
        const val ARG_ENDPOINT_ID = "ARG_ENDPOINT_ID"
        const val ARG_NAME = "ARG_NAME"
    }

    private var connectingFragment: ProjectorConnectingFragment? = null
    private var endpointId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val name = savedInstanceState?.getString(ARG_NAME)
        showProjectorConnectingFragment(name ?: "Lantern")

        endpointId = intent.getStringExtra(ARG_ENDPOINT_ID)
        requestConnection()
    }

    private fun requestConnection() {
        val endpointId = endpointId
        if (endpointId != null) {
            App.instance.client.connectTo(endpointId,
                    { showHomeActivity() },
                    { onRequestConnectionFailure() })
            connectingFragment?.showConnecting()
        }
    }

    private fun onRequestConnectionFailure() {
        connectingFragment?.showError()
    }

    override fun onTryAgainClicked() {
        requestConnection()
    }

    private fun showProjectorConnectingFragment(name: String) {
        val fragment = ProjectorConnectingFragment()
        connectingFragment = fragment
        fragment.arguments = Bundle().apply { putString(ARG_NAME, name) }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun showHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
