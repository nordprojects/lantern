package com.example.androidthings.lantern.shared

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.net.InetAddress


/**
 * Searches for a cast device on the local network using DNS-SD
 *
 * Created by joerick on 15/02/18.
 */
class CastDiscoveryManager(private val context: Context) {
    companion object {
        val TAG: String = CastDiscoveryManager::class.java.simpleName
    }
    val discoveryManager: NsdManager by lazy {
        context.getSystemService(NsdManager::class.java) as NsdManager
    }
    private val _devices = mutableListOf<CastDevice>()
    val servicesToResolve = mutableListOf<NsdServiceInfo>()
    var isResolving = false
    val devices: List<CastDevice> get() = _devices

    var listener: Listener? = null

    interface Listener {
        fun devicesUpdated()
    }

    private fun resolveServicesIfNeeded() {
        // NsdManager can only resolve one device at once. So we resolve services by popping from
        // a queue and resolving one-at-once.
        if (isResolving) return
        val service = servicesToResolve.firstOrNull() ?: return
        servicesToResolve.removeAt(0)

        isResolving = true
        discoveryManager.resolveService(service, object : NsdManager.ResolveListener {
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "service resolved $serviceInfo")
                val device = CastDevice.withServiceInfo(serviceInfo)
                        ?: return

                _devices.add(device)
                Handler(Looper.getMainLooper()).post {
                    listener?.devicesUpdated()
                }
                isResolving = false
                resolveServicesIfNeeded()
            }
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.w(TAG, "Resolve error $errorCode for service $serviceInfo")
                isResolving = false
                resolveServicesIfNeeded()
            }
        })
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
        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "service found $serviceInfo")
            servicesToResolve.add(serviceInfo)
            resolveServicesIfNeeded()
        }
        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "service lost $serviceInfo")

            // remove the relevant device from the devices list
            val iterator = _devices.listIterator()
            var changed = false
            while (iterator.hasNext()) {
                val device = iterator.next()
                if (device.host == serviceInfo.host) {
                    iterator.remove()
                    changed = true
                }
            }

            if (changed) {
                Handler(Looper.getMainLooper()).post {
                    listener?.devicesUpdated()
                }
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