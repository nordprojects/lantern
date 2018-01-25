package co.nordprojects.lantern

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import co.nordprojects.lantern.search.ProjectorSearchActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            showProjectorSearchActivity()
        }, 1000)
    }

    fun showProjectorSearchActivity() {
        val intent = Intent(this, ProjectorSearchActivity::class.java)
        startActivity(intent)
        finish()
    }
}
