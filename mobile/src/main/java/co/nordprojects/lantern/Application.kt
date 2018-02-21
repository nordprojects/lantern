package co.nordprojects.lantern

import android.app.Application
import co.nordprojects.lantern.configuration.ProjectorClient
import co.nordprojects.lantern.configuration.ProjectorState

/**
 * Created by Michael Colville on 30/01/2018.
 */
class App : Application() {
    val client: ProjectorClient by lazy { ProjectorClient(this) }
    val projector: ProjectorState?
        get() = client.activeConnection?.projectorState


    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
