package com.example.androidthings.lantern

import android.net.Uri
import android.os.Bundle
import com.example.androidthings.lantern.channels.*
import com.example.androidthings.lantern.channels.nowplaying.NowPlayingChannel
import com.example.androidthings.lantern.channels.spaceporthole.SpacePortholeChannel
import com.example.androidthings.lantern.shared.ChannelConfiguration
import com.example.androidthings.lantern.shared.ChannelInfo


/**
 * Stores a the available channels with corresponding info for them.
 *
 * Created by joerick on 25/01/18.
 */
object ChannelsRegistry {
    val channelsWithInfo = arrayOf<Pair<() -> Channel, ChannelInfo>>(
            Pair(::CalendarChannel, ChannelInfo(
                    "calendar-clock",
                    "Augmented clock",
                    "Projects your Google calendar events around a real-world clock",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_clock"),
                    customizable = true
            )),
            Pair(::ScreenShot, ChannelInfo(
                    "ScreenShot",
                    "Take a picture",
                    "Take a picture of something that is under lantern and project it to any surface.",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_screenshot")
                    )),
            Pair(::NowPlayingChannel, ChannelInfo(
                    "now-playing",
                    "Now playing",
                    "Displays song information for whatever's playing through your Cast-enabled speaker",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_now_playing"),
                    customizable = true
            )),
            Pair(::AmbientWeatherChannel, ChannelInfo(
                    "ambient-weather",
                    "Weather caustics",
                    "Ambient water reflections react to open weather data for a chosen location",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_weather"),
                    customizable = true,
                    rotationDisabled = true
                    )),
            Pair(::SpacePortholeChannel, ChannelInfo(
                    "space-porthole",
                    "Space porthole",
                    "Explore the galaxy with this virtual telescope. Look out for Orion!",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_space"),
                    customizable = true,
                    rotationDisabled = true
            )),
            Pair(::LampChannel, ChannelInfo(
                    "lamp",
                    "Spotlight",
                    "Bring a little drama to your desktop",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_spotlight")
            )),
            Pair(::WebViewChannel, ChannelInfo(
                    "webview",
                    "Web view",
                    "Load a URL for your favourite site, a recipe or a fun web app you’re building",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_web_page"),
                    customizable = true
            )),
            Pair(::BlankChannel, ChannelInfo(
                    "blank",
                    "Blank",
                    "Hide projections for this direction",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_blank")
            )),
            Pair(::BatSignalChannel, ChannelInfo(
                    "bat-signal",
                    "Android-Signal",
                    "Summon Android when troubles about…",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_bat_signal")
            )),
            Pair(::InfoChannel, ChannelInfo(
                    "info",
                    "Lantern Info",
                    "Projects some useful information about the projector",
                    Uri.parse("android.resource://com.example.androidthings.lantern/drawable/banner_info")
            ))
    )

    val channelsInfo = channelsWithInfo.map { it.second }

    fun newChannelForConfig(config: ChannelConfiguration): Channel {
        val args = Bundle()
        args.putParcelable(Channel.ARG_CONFIG, config)
        var channel: Channel? = null
        var rotationDisabled = false

        for ((channelConstructor, info) in ChannelsRegistry.channelsWithInfo) {
            if (info.id == config.type) {
                channel = channelConstructor()
                rotationDisabled = info.rotationDisabled
                break
            }
        }

        if (rotationDisabled) {
            args.putBoolean(Channel.ARG_ROTATION_DISABLED, rotationDisabled)
        }

        if (channel == null) {
            channel = ErrorChannel()
            args.putString(ErrorChannel.ARG_MESSAGE, "Unknown channel type '${config.type}'")
        }

        channel.arguments = args
        return channel
    }
}
