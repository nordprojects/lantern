# Lantern

The weather channel uses [OpenWeatherMap](http://openweathermap.org/) APIs to work. To compile your own,
signup for an account and generate an API key.

Then create a file at `things/src/main/res/values/secrets.xml`:

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="openweathermap_api_key">YOUR_KEY_HERE</string>
    </resources>

## Channel Configuration Screen

If you create a channel on the Lantern and it needs to be configured, for example to give it a URL to display, you can create a configuration screen in the companion mobile app.

To add a channel config to the mobile app you must create an activity and add it to the list of available config activites using the following steps

- Subcalss `ChannelConfigActivity`
- Add that to the AndoirdManifest.xml file, making sure to set it's parent to `".channels.ChannelsListActivity"`
```
    <activity
        android:name=".channels.config.WebConfigActivity"
        android:configChanges="orientation"
        android:label="Customize"
        android:parentActivityName=".channels.ChannelsListActivity"
        android:screenOrientation="portrait" />
```
- Add a toolbar to the activity layout file and set it as the action bar in `onCreate`
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
```
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationIcon(R.drawable.back_chevron)
```
- Add an entry to `ChannelConfigOptions` linking it to the channel id set in `ChannelsRegistry` in the things app
```
    "webview" to WebConfigActivity::class.java
```
- Add the `customizable = true` flag to `ChannelsRegistry` in the things app
- To update the config from within your activity you can add them to `config.settings` or `config.secrets` and call `finishWithConfigUpdate()`