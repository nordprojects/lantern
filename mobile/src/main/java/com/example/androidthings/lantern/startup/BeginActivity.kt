package com.example.androidthings.lantern.startup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.example.androidthings.lantern.R
import com.example.androidthings.lantern.search.ProjectorSearchActivity
import kotlinx.android.synthetic.main.activity_begin.*

class BeginActivity : AppCompatActivity() {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        private const val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_begin)

        if (savedInstanceState == null) { // only run fade in on first create
            textView.alpha = 0.0F
            textView.animate().apply {
                duration = 500
                startDelay = 500
            }.alpha(1.0F)

            beginButton.alpha = 0.0F
            beginButton.animate().apply {
                duration = 500
                startDelay = 1000
            }.alpha(1.0F)
        }

        beginButton.setOnClickListener { onBeginClicked() }
    }

    private fun onBeginClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        } else {
            showProjectorSearchActivity()
        }
    }

    private fun requestPermissions() {
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check ACCESS_COARSE_LOCATION permission
        if ((grantResults.size >= 5 && grantResults[4] == PackageManager.PERMISSION_GRANTED)) {
            showProjectorSearchActivity()
        } else {
            showPermissionsRequiredAlert()
        }
    }

    private fun showPermissionsRequiredAlert() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Location Required")
        alertDialog.setMessage("Lantern requires location permissions to find nearby projectors")
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", { dialog, _ ->
            dialog.dismiss()
        })
        alertDialog.show()
    }

    private fun showProjectorSearchActivity() {
        val intent = Intent(this, ProjectorSearchActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, R.anim.hold)
    }
}
