package com.example.androidthings.lantern.settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.androidthings.lantern.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

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
}
