package co.nordprojects.lantern

import android.app.Application
import co.nordprojects.lantern.configuration.ConfigurationClient
import co.nordprojects.lantern.configuration.ProjectorConfiguration

/**
 * Created by Michael Colville on 30/01/2018.
 */
class App : Application() {
    val configClient: ConfigurationClient by lazy { ConfigurationClient(this) }
    val projector: ProjectorConfiguration?
        get() = configClient.activeConnection?.projectorConfig


    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
