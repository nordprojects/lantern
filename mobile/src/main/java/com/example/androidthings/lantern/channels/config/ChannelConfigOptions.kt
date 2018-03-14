package com.example.androidthings.lantern.channels.config


/**
 * Created by Michael Colville on 12/02/2018.
 */
object ChannelConfigOptions {
    val channelTypeToActivity = mapOf(
            "message" to MessageConfigActivity::class.java,
            "calendar-clock" to CalendarConfigActivity::class.java,
            "webview" to WebConfigActivity::class.java,
            "ambient-weather" to WeatherConfigActivity::class.java,
            "now-playing" to NowPlayingConfigActivity::class.java,
            "space-porthole" to SpaceConfigActivity::class.java
    )

    val channelTypeToSubtitle = mapOf(
            "blank" to "Projections hidden for this direction"
    )
}
