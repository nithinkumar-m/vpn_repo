package com.cursor.vpn.service

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.content.ContextCompat
import com.cursor.vpn.core.ConnectionStatus
import com.cursor.vpn.core.VpnContract
import com.cursor.vpn.data.VpnConfig
import com.cursor.vpn.tunnel.VpnTunnelRunner
import com.cursor.vpn.util.VpnNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SecureVpnService : VpnService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnJob: Job? = null
    private var interfaceDescriptor: ParcelFileDescriptor? = null
    private lateinit var notificationHelper: VpnNotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper = VpnNotificationHelper(this)
    }

    override fun onDestroy() {
        stopVpn()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val config = intent.getParcelableExtra<VpnConfig>(EXTRA_CONFIG)
                if (config != null) {
                    startVpn(config)
                } else {
                    stopSelf()
                }
            }
            ACTION_STOP -> stopVpn()
            else -> stopSelf()
        }
        return START_STICKY
    }

    private fun startVpn(config: VpnConfig) {
        stopVpn()
        startForeground(
            VpnNotificationHelper.NOTIFICATION_ID,
            notificationHelper.buildNotification(ConnectionStatus.CONNECTING, config.sessionName)
        )
        vpnJob = serviceScope.launch {
            broadcastStatus(ConnectionStatus.CONNECTING, "Preparing secure tunnel")
            try {
                interfaceDescriptor = establishInterface(config)
                broadcastStatus(ConnectionStatus.CONNECTED, "Tunnel established for ${config.sessionName}")
                startForeground(
                    VpnNotificationHelper.NOTIFICATION_ID,
                    notificationHelper.buildNotification(ConnectionStatus.CONNECTED, config.sessionName)
                )
                val descriptor = interfaceDescriptor ?: return@launch
                VpnTunnelRunner(config, ::broadcastLog).run(descriptor)
            } catch (t: Throwable) {
                broadcastError(t)
            } finally {
                cleanupInterface()
                stopForegroundCompat()
                broadcastStatus(ConnectionStatus.DISCONNECTED, "Tunnel stopped")
                stopSelf()
            }
        }
    }

    private fun stopVpn() {
        vpnJob?.cancel()
        vpnJob = null
        cleanupInterface()
        stopForegroundCompat()
    }

    private fun establishInterface(config: VpnConfig): ParcelFileDescriptor {
        val builder = Builder()
            .setSession(config.sessionName)
            .setMtu(config.mtu)
            .addAddress(config.virtualAddress, 32)
            .addDnsServer(config.dnsServer)
            .addRoute("0.0.0.0", 0)
        return builder.establish() ?: throw IllegalStateException("Unable to create VPN interface")
    }

    private fun cleanupInterface() {
        interfaceDescriptor?.close()
        interfaceDescriptor = null
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun broadcastStatus(status: ConnectionStatus, log: String? = null) {
        val intent = Intent(VpnContract.ACTION_VPN_STATUS).apply {
            putExtra(VpnContract.EXTRA_STATUS, status.name)
            log?.let { putExtra(VpnContract.EXTRA_LOG, it) }
        }
        sendBroadcast(intent)
    }

    private fun broadcastLog(message: String) {
        val intent = Intent(VpnContract.ACTION_VPN_STATUS).apply {
            putExtra(VpnContract.EXTRA_STATUS, ConnectionStatus.CONNECTED.name)
            putExtra(VpnContract.EXTRA_LOG, message)
        }
        sendBroadcast(intent)
    }

    private fun broadcastError(throwable: Throwable) {
        val intent = Intent(VpnContract.ACTION_VPN_STATUS).apply {
            putExtra(VpnContract.EXTRA_STATUS, ConnectionStatus.ERROR.name)
            putExtra(VpnContract.EXTRA_ERROR, throwable.message ?: throwable.toString())
        }
        sendBroadcast(intent)
    }

    companion object {
        private const val ACTION_START = "com.cursor.vpn.action.START"
        private const val ACTION_STOP = "com.cursor.vpn.action.STOP"
        private const val EXTRA_CONFIG = "extra_config"

        fun start(context: Context, config: VpnConfig) {
            val intent = Intent(context, SecureVpnService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CONFIG, config)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SecureVpnService::class.java).apply {
                action = ACTION_STOP
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
