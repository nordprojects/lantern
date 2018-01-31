package co.nordprojects.lantern.home

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.nordprojects.lantern.R
import co.nordprojects.lantern.channels.ChannelsListActivity
import co.nordprojects.lantern.settings.SettingsActivity
import co.nordprojects.lantern.shared.Direction
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_projector_display.*
import java.nio.channels.Channels

class HomeActivity : AppCompatActivity(), ProjectorDisplayFragment.OnDirectionSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        settings_button.setOnClickListener { showSettings() }

        val projectorFragment = ProjectorDisplayFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, projectorFragment)
        fragmentTransaction.commit()
    }

    override fun onDirectionSelected(direction: Direction) {
        showChannelList(direction)
    }

    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun showChannelList(direction: Direction) {
        val intent = Intent(this, ChannelsListActivity::class.java)
        intent.putExtra("direction", direction.toString())
        startActivity(intent)
    }
}
