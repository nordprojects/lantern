package com.example.androidthings.lantern.channels.config

/**
 * Registry of Activities used for configuring channels. Add a subclass of ChannelConfigActivity
 * to allow users to update ChannelConfiguration.
 *
 * Add a default subtitle for a channel if required to be displayed on the HomeActivity when
 * that channel is being projected. This subtitle can also be set in the ChannelConfigActivity.
 *
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
