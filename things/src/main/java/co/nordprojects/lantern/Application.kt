package co.nordprojects.lantern

import android.app.Application as AndroidApplication

/**
  * Created by joerick on 16/01/18.
 */
class Application : AndroidApplication() {
    val config = ConfigurationManager(this)

    override fun onCreate() {
        super.onCreate()

    }
}