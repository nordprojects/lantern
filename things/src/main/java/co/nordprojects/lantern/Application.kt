package co.nordprojects.lantern

import android.app.Application as AndroidApplication


class App : AndroidApplication() {
    val configManager: ConfigurationManager by lazy { ConfigurationManager(this) }
    val config: AppConfiguration
        get() = configManager.appConfig

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}