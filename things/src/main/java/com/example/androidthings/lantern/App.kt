package com.example.androidthings.lantern

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import com.example.androidthings.lantern.comms.ConfigurationServer
import com.example.androidthings.lantern.hardware.Accelerometer
import com.google.android.things.device.ScreenManager
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.util.*
import android.app.Application as AndroidApplication

class App : AndroidApplication() {
    companion object {
        lateinit var instance: App
            private set

        private val TAG = App::class.java.simpleName
        private const val CONFIG_FILE_PATH = "config.json"
    }

    val config: AppConfiguration by lazy { AppConfiguration(this) }
    val accelerometer: Accelerometer by lazy { Accelerometer() }

    private val configServer: ConfigurationServer by lazy { ConfigurationServer(this) }
    private val configObserver = Observer { _, _ -> configUpdated() }
    lateinit var advertisingName: String

    override fun onCreate() {
        super.onCreate()
        instance = this

        setupDisplay()

        loadConfig()
        advertisingName = config.name

        configServer.startAdvertising(advertisingName)
        accelerometer.startUpdating()

        config.addObserver(configObserver)
    }

    private fun configUpdated() {
        saveConfig()

        if (advertisingName != config.name) {
            advertisingName = config.name

            // for the name change to be reflected in the nearby advertisement, we have to restart
            // advertising.
            configServer.stopAdvertising()
            configServer.startAdvertising(advertisingName)
        }
    }

    private fun loadConfig() {
        try {
            val file = openFileInput(CONFIG_FILE_PATH)
            val jsonObject = JSONObject(file.reader().readText())
            file.close()
            config.updateWithJson(jsonObject)
        }
        catch (e: Exception) {
            when (e) {
                is FileNotFoundException,
                is JSONException -> {
                    Log.w(TAG, "Failed to load settings file.", e)
                    config.resetToDefaults()
                }
                else -> throw e
            }
        }
    }

    private fun saveConfig() {
        val file = openFileOutput(CONFIG_FILE_PATH, Context.MODE_PRIVATE)
        val fileWriter = file.writer()
        fileWriter.write(config.toJson(includingSecrets = true).toString(2))
        fileWriter.close()
        file.close()

        Log.d(TAG, "Saved settings to $CONFIG_FILE_PATH")
    }

    private fun setupDisplay() {
        val windowManager = getSystemService(WindowManager::class.java)
        val screenMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(screenMetrics)
        val screenHeight = screenMetrics.heightPixels

        val screenManager = ScreenManager.getInstance(Display.DEFAULT_DISPLAY)

        val density = when (screenHeight) {
            1080 -> 320 // xhdpi
            720 -> 213 // tvdpi
            else -> screenHeight * 160 / 540 // a linear scaling according to height
        }
        Log.i(TAG, "Screen height is ${screenHeight}px, setting density to $density")
        screenManager.setDisplayDensity(density)
        screenManager.setBrightness(255)

        screenManager.lockRotation(ScreenManager.ROTATION_0)
    }
}
