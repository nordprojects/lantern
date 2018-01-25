package co.nordprojects.lantern

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.util.Observer
import android.app.Application as AndroidApplication


class App : AndroidApplication() {
    val configManager: ConfigurationManager by lazy { ConfigurationManager(this) }
    val config: AppConfiguration
        get() = configManager.appConfig
    val configServer: ConfigurationServer by lazy { ConfigurationServer(this) }
    val accelerometer: Accelerometer by lazy { Accelerometer() }

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        configServer.startAdvertising()
        accelerometer.startUpdating()
    }
}


/**
 * Provides access to, and persists, app configuration.
 *
 * Created by joerick on 16/01/18.
 */
class ConfigurationManager(val context: Context) {
    val TAG = ConfigurationManager::class.java.simpleName
    val CONFIG_FILE_PATH = "config.json"
    val appConfig = AppConfiguration()
    val appConfigObserver = Observer { _, _ -> save() }

    init {
        load()
        appConfig.addObserver(appConfigObserver)
    }

    private fun load(): Unit {
        try {
            val file = context.openFileInput(CONFIG_FILE_PATH)
            val jsonObject = JSONObject(file.reader().readText())
            file.close()
            appConfig.updateWithJson(jsonObject)
        }
        catch (e: Exception) {
            when (e) {
                is FileNotFoundException,
                is JSONException -> {
                    Log.w(TAG, "Failed to load settings file.", e)
                    appConfig.resetToDefaults()
                }
                else -> throw e
            }
        }
        catch (e: JSONException) {
            Log.w(TAG, "Corrupt settings file", e)
        }
    }

    private fun save(): Unit {
        val file = context.openFileOutput(CONFIG_FILE_PATH, Context.MODE_PRIVATE)
        val fileWriter = file.writer()
        fileWriter.write(appConfig.toJson().toString(2))
        fileWriter.close()
        file.close()

        Log.d(TAG, "Saved settings to ${CONFIG_FILE_PATH}")
    }
}
