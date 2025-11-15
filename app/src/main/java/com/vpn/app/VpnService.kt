package com.vpn.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService as AndroidVpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

class VpnService : AndroidVpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnThread: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val TAG = "VpnService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "vpn_channel"
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val VPN_DNS = "8.8.8.8"
        private const val VPN_MTU = 1500
        
        private var isServiceRunning = false
        
        const val ACTION_CONNECT = "com.vpn.app.CONNECT"
        const val ACTION_DISCONNECT = "com.vpn.app.DISCONNECT"
        
        fun isConnected(): Boolean = isServiceRunning
        
        suspend fun disconnect() {
            isServiceRunning = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> startVpn()
            ACTION_DISCONNECT -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isServiceRunning) {
            Log.d(TAG, "VPN already running")
            return
        }

        try {
            val builder = Builder()
            builder.setSession("VPN Session")
                .addAddress(VPN_ADDRESS, 30)
                .addRoute(VPN_ROUTE, 0)
                .addDnsServer(VPN_DNS)
                .setMtu(VPN_MTU)
                .setBlocking(false)

            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                isServiceRunning = true
                startForeground(NOTIFICATION_ID, createNotification())
                startVpnThread()
                Log.d(TAG, "VPN started successfully")
            } else {
                Log.e(TAG, "Failed to establish VPN interface")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopSelf()
        }
    }

    private fun stopVpn() {
        isServiceRunning = false
        vpnThread?.cancel()
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
        Log.d(TAG, "VPN stopped")
    }

    private fun startVpnThread() {
        vpnThread = serviceScope.launch {
            try {
                val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
                val vpnOutput = FileOutputStream(vpnInterface!!.fileDescriptor)
                val buffer = ByteArray(VPN_MTU)

                while (isServiceRunning && isActive) {
                    val length = vpnInput.read(buffer)
                    if (length > 0) {
                        // Process VPN packets here
                        // This is a basic implementation - in a real VPN, you would:
                        // 1. Parse IP packets
                        // 2. Encrypt/decrypt data
                        // 3. Forward to VPN server
                        // 4. Handle responses
                        Log.d(TAG, "Received packet: $length bytes")
                    }
                    delay(10) // Small delay to prevent CPU spinning
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in VPN thread", e)
            } finally {
                stopVpn()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN connection status"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN Connected")
            .setContentText("Your VPN is active")
            .setSmallIcon(R.drawable.ic_vpn_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        serviceScope.cancel()
    }
}
