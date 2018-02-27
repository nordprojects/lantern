package com.example.androidthings.lantern.startup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.example.androidthings.lantern.R


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler().postDelayed({
            showBeginActivity()
        }, 1000)
    }

    private fun showBeginActivity() {
        val intent = Intent(this, BeginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, R.anim.hold)
        finish()
    }
}
