package com.example.vpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.vpn.MainActivity
import com.example.vpn.R
import com.example.vpn.model.VpnConfig
import com.example.vpn.model.VpnRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.use

class LocalVpnService : android.net.VpnService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vpnInterface: ParcelFileDescriptor? = null
    private var currentConfig: VpnConfig? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val config = intent.getParcelableExtra<VpnConfig>(EXTRA_VPN_CONFIG)
                if (config != null) {
                    serviceScope.launch { startTunnel(config) }
                } else {
                    Log.w(TAG, "Missing VPN config, stopping service")
                    stopSelf()
                }
            }
            ACTION_DISCONNECT -> serviceScope.launch { stopTunnel() }
            else -> Log.d(TAG, "Unknown action: ${intent?.action}")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.launch { stopTunnel() }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onRevoke() {
        serviceScope.launch { stopTunnel() }
        super.onRevoke()
    }

    private suspend fun startTunnel(config: VpnConfig) = withContext(Dispatchers.IO) {
        if (vpnInterface != null) return@withContext
        currentConfig = config

        val builder = Builder()
            .setSession(config.sessionName)
            .setMtu(config.mtu)

        builder.addAddress(config.localAddress, config.localPrefix)
        config.routes.forEach { route: VpnRoute ->
            builder.addRoute(route.address, route.prefixLength)
        }
        config.dnsServers.forEach { dns -> builder.addDnsServer(dns) }

        val interfaceFd = builder.establish()
        if (interfaceFd == null) {
            Log.e(TAG, "Failed to establish VPN interface")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return@withContext
        }

        vpnInterface = interfaceFd
        startForeground(NOTIFICATION_ID, buildNotification("Connected to ${config.serverAddress}"))
        Log.i(TAG, "VPN tunnel established")
    }

    private suspend fun stopTunnel() = withContext(Dispatchers.IO) {
        val hadInterface = vpnInterface != null
        vpnInterface?.use {
            Log.i(TAG, "Closing VPN interface")
        }
        vpnInterface = null
        currentConfig = null
        if (hadInterface) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        stopSelf()
    }

    private fun buildNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_vpn_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager: NotificationManager = getSystemService() ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.vpn_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.vpn_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "LocalVpnService"
        private const val CHANNEL_ID = "vpn_channel"
        const val ACTION_CONNECT = "com.example.vpn.action.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.action.DISCONNECT"
        const val EXTRA_VPN_CONFIG = "extra_vpn_config"
        private const val NOTIFICATION_ID = 44
    }
}
