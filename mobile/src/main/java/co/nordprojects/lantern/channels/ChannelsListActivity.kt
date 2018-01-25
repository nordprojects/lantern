package co.nordprojects.lantern.channels

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.nordprojects.lantern.R
import kotlinx.android.synthetic.main.activity_channels_list.*

class ChannelsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels_list)

        val direction = intent.getStringExtra("direction")
        direction_text.text = direction
    }
}
