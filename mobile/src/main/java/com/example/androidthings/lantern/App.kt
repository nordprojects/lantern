package com.example.androidthings.lantern

import android.app.Application
import com.example.androidthings.lantern.configuration.Discovery
import com.example.androidthings.lantern.configuration.ProjectorClient
import com.example.androidthings.lantern.configuration.ProjectorState

/**
 * Created by Michael Colville on 30/01/2018.
 */
class App : Application() {
    val client: ProjectorClient by lazy { ProjectorClient(this) }
    val discovery: Discovery by lazy { Discovery(this) }
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
