package co.nordprojects.lantern

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.Log
import co.nordprojects.lantern.search.ProjectorSearchActivity
import android.support.design.widget.Snackbar
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.support.v7.app.AlertDialog


class SplashActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SplashActivity::class.java.simpleName
    }

    private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
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
            requestPermissions()
        })
        alertDialog.show()
    }

    private fun showProjectorSearchActivity() {
        val intent = Intent(this, ProjectorSearchActivity::class.java)
        startActivity(intent)
        finish()
    }
}
