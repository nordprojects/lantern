package co.nordprojects.lantern

import android.app.Application
import co.nordprojects.lantern.configuration.ConfigurationClient

/**
 * Created by Michael Colville on 30/01/2018.
 */
class App : Application() {
    val configClient: ConfigurationClient by lazy { ConfigurationClient(this) }

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
