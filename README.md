# Lantern

The weather channel uses [OpenWeatherMap](http://openweathermap.org/) APIs to work. To compile your own,
signup for an account and generate an API key.

Then create a file at `things/src/main/res/values/secrets.xml`:

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="openweathermap_api_key">YOUR_KEY_HERE</string>
    </resources>
