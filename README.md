# Lantern

## Setup

Before compiling the project for the first time you need to create a `secrets.xml`.

Create a file at `things/src/main/res/values/secrets.xml`, with the following contents:

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="openweathermap_api_key">YOUR_KEY_HERE</string>
    </resources>

This file is ignored by Git, to prevent accidental key-sharing.

The weather channel uses [OpenWeatherMap](http://openweathermap.org/) APIs to work. To compile your 
own, signup for an account and generate an API key, and add it to the `secrets.xml`
`openweathermap_api_key` entry.

## Development

While developing on Lantern, you might want to skip the StartupActivity. To do this, add
`--ez quickStart true` to the Launch Flags of your Run Configuration.

### Creating a channel

To create your own channel, create a Kotlin file at
`things/src/main/java/com/example/androidthings/lantern/channels/MyChannel.kt`. Then paste
the following code to begin:

    package com.example.androidthings.lantern.channels
    
    import android.os.Bundle
    import android.view.Gravity
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import com.example.androidthings.lantern.Channel
    
    class MyChannel: Channel() {
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

Finally, you need to register the channel so it can be found from the app. Open the file
`things/src/main/java/com/example/androidthings/lantern/ChannelsRegistry.kt` and add your new
channel to the `channelsWithInfo` list. e.g.

            Pair(::MyChannel, ChannelInfo(
                    "my-channel",
                    "My brand-new Lantern channel",
                    "It may not look like much, but this channel is going places!"
            )),

That's it! Now build the project and run on your Raspberry Pi, and select the channel with the
mobile app (you don't need to update the mobile app, the things app sends a list of available 
channels when it connects).

#### Things app architecture

```
        ________                                                           
       / ______ \  Nearby                  ┌──────────────────────────────┐
        / ____ \   Connection              │                              │
         ' __ '                            │       StartupActivity        │
          '  '                             │                              │
┌─────────────────────┐                    │      ┌───────────────┐       │
│                     │                    │      │               │       │
│ ConfigurationServer │                    │      │  InfoChannel  │       │
│                     │                    │      │               │       │
└─────────────────────┘                    │      └───────────────┘       │
           │                               │                              │
           │                               │                              │
           │                               │                              │
           │                               └──────────────────────────────┘
           │                                                               
           ▼                                                               
 ┌──────────────────┐                      ┌──────────────────────────────┐
 │                  │                      │                              │
 │ AppConfiguration │─────────────────────>│         MainActivity         │
 │                  │                      │                              │
 └──────────────────┘                      │        ┌───────────────┐     │
                                           │       ┌┴──────────────┐│     │
                                           │      ┌┴──────────────┐││     │
                                           │      │               │││     │
 ┌──────────────────┐                      │      │   MyChannel   │├┘     │
 │                  │                      │      │               ├┘      │
 │  Accelerometer   │─────────────────────>│      └───────────────┘       │
 │                  │                      │                              │
 └──────────────────┘                      └──────────────────────────────┘
```
 
The channels are, in fact, [Support Fragments](Fragment), so you can use any of the fragment
callbacks in your channels. 

The MainActivity switches between channels by showing/hiding them. So if you want to respond to
being no longer visible (to save CPU cycles), implement
[Fragment.onHiddenChanged()](onHiddenChanged).

[Fragment]: https://developer.android.com/reference/android/support/v4/app/Fragment.html
[onHiddenChanged]: https://developer.android.com/reference/android/support/v4/app/Fragment.html#onHiddenChanged(boolean)

#### Configuration Screen on mobile

If you create a channel on the Lantern that needs configuration, for example to give it a URL
to display, you can create a configuration screen in the companion mobile app.

To add a channel config to the mobile app you must create an activity and add it to the list of
available config activities using the following steps

 -  Subclass `ChannelConfigActivity`
 -  Add you new subclass to the AndroidManifest.xml file, making sure to set it's parent activity to
    `".channels.ChannelsListActivity"`

    ```
        <activity
            android:name=".channels.config.WebConfigActivity"
            android:configChanges="orientation"
            android:label="Customize"
            android:parentActivityName=".channels.ChannelsListActivity"
            android:screenOrientation="portrait" />
    ```

 -  Add a toolbar to the activity layout file...
 
    ```
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
     
    ```
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.back_chevron)
    ```
    
 -  Add an entry to `ChannelConfigOptions` linking it to the channel ID.

    ```
        "webview" to WebConfigActivity::class.java
    ```

 -  Add the `customizable = true` flag to the ChannelInfo instance in `ChannelsRegistry` in the
    things app
 -  To update the config from within your activity, add values to `config.settings` or
    `config.secrets` and call `finishWithConfigUpdate()`
