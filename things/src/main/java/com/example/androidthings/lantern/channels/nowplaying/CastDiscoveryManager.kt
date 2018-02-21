package com.example.androidthings.lantern.channels.nowplaying

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.content.Context
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Searches for a cast device on the local network using DNS-SD
 *
 * Created by joerick on 15/02/18.
 */
class CastDiscoveryManager(private val context: Context) {
    val TAG = this::class.java.simpleName
    val discoveryManager: NsdManager by lazy {
        context.getSystemService(NsdManager::class.java) as NsdManager
    }
    private val _devices = mutableListOf<CastDevice>()
    val devices: List<CastDevice> get() = _devices

    var listener: Listener? = null

    interface Listener {
        fun devicesUpdated()
    }

    data class CastDevice(val name: String,
                          val id: String,
                          val host: InetAddress,
                          val port: Int) {
        val hostString = host.hostAddress

        companion object {
            fun withServiceInfo(serviceInfo: NsdServiceInfo): CastDevice? {
                val idBytes = serviceInfo.attributes["id"] ?: return null
                val nameBytes = serviceInfo.attributes["fn"] ?: return null
                val host = serviceInfo.host ?: return null
                val port = serviceInfo.port

                return CastDevice(
                        String(nameBytes, Charsets.UTF_8),
                        String(idBytes, Charsets.UTF_8),
                        host,
                        port
                )
            }
        }
    }

    private val nsdDiscoveryListener = object : NsdManager.DiscoveryListener {
        val TAG = this::class.java.simpleName
        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            discoveryManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    val device = CastDevice.withServiceInfo(serviceInfo) ?: return

                    _devices.add(device)
                    listener?.devicesUpdated()
                }
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "Resolve error $errorCode for service $serviceInfo")
                }
            })
        }
        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            val changed = _devices.removeIf {
                it.host == serviceInfo.host
            }

            if (changed) {
                listener?.devicesUpdated()
            }
        }
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.w(TAG, "Start discovery error $errorCode for serviceType $serviceType")
        }
        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.w(TAG, "Stop discovery error $errorCode for serviceType $serviceType")
        }
        override fun onDiscoveryStarted(serviceType: String?) {}
        override fun onDiscoveryStopped(serviceType: String?) {}
    }

    fun startDiscovering() {
        discoveryManager.discoverServices(
                "_googlecast._tcp",
                NsdManager.PROTOCOL_DNS_SD,
                nsdDiscoveryListener
        )
    }

    fun stopDiscovering() {
        discoveryManager.stopServiceDiscovery(nsdDiscoveryListener)
        _devices.clear()
    }
}