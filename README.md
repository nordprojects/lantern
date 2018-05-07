# Android Things Lantern

![Lantern on a desk](https://user-images.githubusercontent.com/1244317/39310781-38b61c86-4963-11e8-8011-fd3ec4d3f234.jpg)

Lantern combines an Ikea lamp, laser projector and Android Things to create a connected projector that explores the relationships between surfaces and content.

This repo contains all the app code that powers Lantern. 

The project is split into three modules:

- `/things` - the Android Things app
- `/mobile` - the companion mobile app
- `/shared` - code used by both apps

For instructions on how to build the hardware, see [our project page on Hackster.io](https://www.hackster.io/nord-projects/lantern-9f0c28).

## How it works

![Now Playing on a speaker](https://user-images.githubusercontent.com/1244317/39310848-65eb29a8-4963-11e8-8d10-12c93b4fd03f.png)

Lantern imagines a future where projections are used to present ambient information and relevant UI around everyday objects. Point it at a clock to see your appointments, or point to speaker to display the currently playing song. As opposed to a screen, when it’s no longer needed, the projections simply fade away.

![Lantern head tilt](https://user-images.githubusercontent.com/1244317/39312133-ac6c0d40-4966-11e8-80ae-60e09fd77f3e.gif)

Lantern is set-up and controlled using the companion app for Android. They communicate using *Nearby Connections*, a protocol developed by Google to facilitate local peer-to-peer communication with nearby devices.

Lantern is built around the concept of ‘channels’ – app fragments that can be configured through the companion app, and display projected UI. Each surface has a different channel, so Lantern will display something different on the table, the wall and the ceiling.

By building on Android Things, we benefit from a modern, familiar technology stack, which makes connectivity and graphics really simple to implement and iterate. Both the Android Things code and the companion app are written in Kotlin, which has been a joy to work with.

## Technical overview

![Technical Architecture](https://user-images.githubusercontent.com/1244317/39315774-6fea22ea-496f-11e8-9473-a3ae96dfd0cc.png)

There are two main components to the Lantern software - the ‘Things’ app (`/things`), which runs on Android Things on a Raspberry Pi, and the Companion app (`/mobile`)` which runs on an Android phone.

The hardware is built as an ‘Ikea hack’, with a 3D-printed enclosure, a Raspberry Pi, a laser projector (HDMI), an accelerometer (I2C) and a few off-the-shelf wires and connectors.

## How to make your own Lantern

![iso components](https://user-images.githubusercontent.com/1244317/39315851-9d03373a-496f-11e8-8380-38af303ca893.jpg)

Note: this guide focuses on the software side of Lantern. For more in-depth instructions, see the [guide on Hackster.io]((https://www.hackster.io/nord-projects/lantern-9f0c28)).

#### Step 1: Assemble the hardware

<img width=50% alt="Ikea Hack" src="https://user-images.githubusercontent.com/1244317/39311197-2bcbc506-4964-11e8-8a81-b3f9d9c10bef.gif"/><img width=50% alt="3D Printer" src="https://user-images.githubusercontent.com/1244317/39311146-0954191a-4964-11e8-8f55-ff0a7075e9d4.gif"/>
<img width=100% alt="Hardware Matrix" src="https://user-images.githubusercontent.com/1244317/39310948-a00c39c4-4963-11e8-8a8e-3434561f23f3.gif"/>


To build the hardware, you’ll hack the Ikea Tertial lamp, 3d-print the enclosure, and assemble it with the Raspberry Pi, projector and the other components.

For more information, read the [assembly instructions](https://www.hackster.io/nord-projects/lantern-9f0c28).

#### Step 2: Install Android Things

![Android Things Setup Utility](https://user-images.githubusercontent.com/1244317/39311683-7f4ef49a-4965-11e8-8e0c-06439157822e.png)

Android Things for Raspberry Pi comes as an image that can the flashed to an SD card. Our favourite way to do this is to use the android-things-setup-utility, which can be downloaded from the [Android Things Console](https://partner.android.com/things/console/#/tools).

Download it, and run as superuser e.g.

    sudo ~/Downloads/android-things-setup-utility/android-things-setup-utility-macos

or 

    sudo ~/Downloads/android-things-setup-utility/android-things-setup-utility-linux

On Windows, right-click the executable file and choose ‘Run as administrator’

When prompted, choose ‘Install Android Things’, ‘Raspberry Pi’ and then ‘Development image’. 

Insert your SD card; Android Things will then be written to it.

#### Step 3: Boot Android Things and connect

![Connect Ethernet](https://user-images.githubusercontent.com/1244317/39311717-93f63782-4965-11e8-801b-012b15c97cb2.png)

Insert the SD card into the Raspberry Pi and connect the power. Once Android Things starts, we'll setup the WiFi connection.

Connect an ethernet cable from the Pi to your computer (or to something on your network like a router).

Run `android-things-setup-utility` again, this time choosing to configure WiFi. Follow the on-screen prompts to complete the setup.

Once connected, open a terminal and use adb to connect to the device.

    adb connect android.local


#### Step 4: Build and install the code

![Lantern Projection](https://user-images.githubusercontent.com/1244317/39311635-5fe2fbc4-4965-11e8-82a7-08eaad3ffc2b.png)

Open this repo in [Android Studio](https://developer.android.com/studio/) and wait for Gradle to sync. Once finished, you should have a `things` Run Configuration in the menu in the toolbar. Choose `things` and hit Run.

Turn on the projector. Once the code is built, Lantern will start 🎉 !

#### Step 5: Assemble the lamp

![Assemble](https://user-images.githubusercontent.com/1244317/39313304-937dbb32-4969-11e8-8f87-5c4cbbfc6bf0.png)

Insert the hardware into the lamp and attach it with the thumbscrews.

#### Step 6: Install the companion app and configure your Lantern

![Companion App](https://user-images.githubusercontent.com/1244317/39311591-3fef14ec-4965-11e8-9101-9b0233b61b57.png)

Connect your Android phone (with [dev mode](https://developer.android.com/studio/debug/dev-options#enable) enabled). In Android Studio, choose the `mobile` Run Configuration and hit Run.

Using the companion app, connect the Lantern and play with the channels we’ve created!

## Making your own channels

To create your own channel, create a Kotlin file at
`things/src/main/java/com/example/androidthings/lantern/channels/MyChannel.kt`. Then paste
the following code to begin:

```kotlin
package com.example.androidthings.lantern.channels

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.androidthings.lantern.Channel

class MyChannel : Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = TextView(context)
        view.text = "Hello world from my new Lantern channel!"
        view.textSize = 30f
        view.gravity = Gravity.CENTER
        return view

        // alternatively, you can load from a layout file!
        // return inflater.inflate(R.layout.my_channel, viewGroup, false)
    }        
}
```

Finally, you need to register the channel so it can be found from the app. Open the file
`things/src/main/java/com/example/androidthings/lantern/ChannelsRegistry.kt` and add your new
channel to the `channelsWithInfo` list. e.g.

```kotlin
    Pair(::MyChannel, ChannelInfo(
            "my-channel",
            "My brand-new Lantern channel",
            "It may not look like much, but this channel is going places!"
    )),
```

That's it! Now build the project and run on your Raspberry Pi. Now select the channel with the
mobile app to see it run. (You don't need to update the mobile app, because the things app sends a list of available 
channels when it connects.)

#### Configuration Screen on mobile

If you create a channel that needs configuration (e.g. a URL to display), you can create a configuration Activity in the companion mobile app.

To add a channel configuration screen, first create an activity and then add it to the list of available activities using the following steps:

 -  Subclass `ChannelConfigActivity`
 -  Add your new subclass to the AndroidManifest.xml file, making sure to set it's parent activity to
    `".channels.ChannelsListActivity"`

    ```xml
        <activity
            android:name=".channels.config.WebConfigActivity"
            android:configChanges="orientation"
            android:label="Customize"
            android:parentActivityName=".channels.ChannelsListActivity"
            android:screenOrientation="portrait" />
    ```

 -  Add a toolbar to the activity layout file...
 
    ```xml
        <android.support.v7.widget.Toolbar
            style="@style/ToolBarStyle"
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="?attr/colorPrimary"
            android:minHeight="?attr/actionBarSize"
            android:theme="?attr/actionBarTheme" />
    ```
    
    ...and set it as the action bar in `onCreate`
     
    ```kotlin
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)
    ```
    
 -  Add an entry to `ChannelConfigOptions` linking it to the channel ID.

    ```kotlin
        "webview" to WebConfigActivity::class.java
    ```

 -  Add the `customizable = true` flag to the ChannelInfo instance in `ChannelsRegistry` in the
    things app
 -  To update the config from within your activity, add values to `config.settings` or
    `config.secrets` and call `finishWithConfigUpdate()`

## Development

While developing on Lantern, you might want to skip the StartupActivity. To do this, add
`--ez quickStart true` to the Launch Flags of your Run Configuration.


#### Configure the weather channel

To get local weather data for the ambient weather caustics you need to create a `secrets.xml`.

Create a file at `things/src/main/res/values/secrets.xml`, with the following contents:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="openweathermap_api_key">YOUR_KEY_HERE</string>
</resources>
```

This file is ignored by Git, to prevent accidental key-sharing.

The weather channel uses [OpenWeatherMap](http://openweathermap.org/) APIs to work. To compile your 
own, signup for an account and generate an API key, and add it to the `secrets.xml`
`openweathermap_api_key` entry.