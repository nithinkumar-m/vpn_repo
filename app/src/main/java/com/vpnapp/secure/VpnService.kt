package com.vpnapp.secure

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService as AndroidVpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import kotlin.concurrent.thread

class VpnService : AndroidVpnService() {

    companion object {
        const val ACTION_CONNECT = "com.vpnapp.secure.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.vpnapp.secure.ACTION_DISCONNECT"
        const val ACTION_VPN_STATUS = "com.vpnapp.secure.ACTION_VPN_STATUS"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "VPN_SERVICE_CHANNEL"
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var vpnThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                if (!isRunning) {
                    startVpn()
                }
            }
            ACTION_DISCONNECT -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn() {
        try {
            // Create VPN interface
            val builder = Builder()
            builder.setSession("SecureVPN")
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                .setMtu(1500)

            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                isRunning = true
                
                // Start foreground notification
                startForeground(NOTIFICATION_ID, createNotification("VPN Connected"))
                
                // Broadcast connection status
                broadcastStatus(true)
                
                // Start VPN thread
                vpnThread = thread {
                    runVpnConnection()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopVpn()
        }
    }

    private fun stopVpn() {
        isRunning = false
        
        vpnThread?.interrupt()
        vpnThread = null
        
        vpnInterface?.close()
        vpnInterface = null
        
        broadcastStatus(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun runVpnConnection() {
        try {
            val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
            val vpnOutput = FileOutputStream(vpnInterface!!.fileDescriptor)
            
            val tunnel = DatagramChannel.open()
            protect(tunnel.socket())
            
            // Connect to a VPN server (this is a simplified example)
            // In a real app, you would connect to an actual VPN server
            tunnel.connect(InetSocketAddress("8.8.8.8", 53))
            
            val packet = ByteBuffer.allocate(32767)
            
            while (isRunning) {
                // Read from VPN interface
                val length = vpnInput.read(packet.array())
                if (length > 0) {
                    // In a real VPN, you would process and forward packets here
                    packet.limit(length)
                    
                    // Forward packet through tunnel (simplified)
                    // tunnel.write(packet)
                    
                    packet.clear()
                }
                
                Thread.sleep(10)
            }
            
            tunnel.close()
        } catch (e: Exception) {
            if (isRunning) {
                e.printStackTrace()
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
                description = "VPN Connection Status"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Secure VPN")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_vpn)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun broadcastStatus(connected: Boolean) {
        val intent = Intent(ACTION_VPN_STATUS).apply {
            putExtra("connected", connected)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }
}
