package co.nordprojects.lantern.settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.nordprojects.lantern.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsFragment = SettingsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, settingsFragment)
        fragmentTransaction.commit()
    }
}
