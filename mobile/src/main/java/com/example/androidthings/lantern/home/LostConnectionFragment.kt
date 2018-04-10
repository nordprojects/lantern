package com.example.androidthings.lantern.home

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.example.androidthings.lantern.App
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.fragment_lost_connection.*

/**
 * Error screen shown when a nearby connection is lost, giving the option to reconnect.
 *
 * Created by Michael Colville.
 */
class LostConnectionFragment : Fragment() {
    companion object {
        private val TAG: String = LostConnectionFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lost_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reconnectButton.setOnClickListener { reconnectPressed() }
    }

    private fun reconnectPressed() {
        val endpoint = App.instance.client.lastEndpoint
        if (endpoint != null) {
            showProgress()
            App.instance.client.connectTo(endpoint, {
                Log.i(TAG, "Connection successful")
                hideProgress()
            }, {
                Log.i(TAG, "Connection failed")
                val snackBar = Snackbar.make(view!!, "Failed to reconnect to projector", Snackbar.LENGTH_LONG)
                snackBar.show()
                hideProgress()
            })
        }
    }

    private fun showProgress() {
        reconnectButton.visibility = INVISIBLE
        progressBar.visibility = VISIBLE
    }

    private fun hideProgress() {
        reconnectButton.visibility = VISIBLE
        progressBar.visibility = INVISIBLE
    }
}

