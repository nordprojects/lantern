package co.nordprojects.lantern

import android.content.Context
import android.util.Log
import android.view.Display
import co.nordprojects.lantern.comms.ConfigurationServer
import co.nordprojects.lantern.hardware.Accelerometer
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
    private lateinit var advertisingName: String

    override fun onCreate() {
        super.onCreate()
        instance = this

        setDisplayDPI()

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
        fileWriter.write(config.toJson().toString(2))
        fileWriter.close()
        file.close()

        Log.d(TAG, "Saved settings to $CONFIG_FILE_PATH")
    }

    private fun setDisplayDPI() {
        val screenManager = ScreenManager(Display.DEFAULT_DISPLAY)
        screenManager.setDisplayDensity(213) // 720p TV density
    }
}
