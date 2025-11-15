package com.vpnapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

class VpnService : VpnService() {
    
    companion object {
        private const val TAG = "VpnService"
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val VPN_MTU = 1500
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "vpn_channel"
        
        const val ACTION_CONNECT = "com.vpnapp.CONNECT"
        const val ACTION_DISCONNECT = "com.vpnapp.DISCONNECT"
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        serviceScope.cancel()
    }
    
    private fun connect() {
        if (isRunning) {
            Log.d(TAG, "VPN already connected")
            return
        }
        
        try {
            val builder = Builder()
            builder.setSession("VPNApp")
            builder.addAddress(VPN_ADDRESS, 32)
            builder.addRoute(VPN_ROUTE, 0)
            builder.setMtu(VPN_MTU)
            
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                isRunning = true
                startForeground(NOTIFICATION_ID, createNotification("VPN Connected"))
                startVpnThread()
                Log.d(TAG, "VPN connected successfully")
            } else {
                Log.e(TAG, "Failed to establish VPN interface")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting VPN", e)
            disconnect()
        }
    }
    
    private fun disconnect() {
        if (!isRunning) return
        
        isRunning = false
        
        try {
            vpnInterface?.close()
            vpnInterface = null
            stopForeground(true)
            stopSelf()
            Log.d(TAG, "VPN disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting VPN", e)
        }
    }
    
    private fun startVpnThread() {
        serviceScope.launch {
            val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
            val vpnOutput = FileOutputStream(vpnInterface!!.fileDescriptor)
            val buffer = ByteArray(VPN_MTU)
            
            while (isRunning && vpnInterface != null) {
                try {
                    val length = vpnInput.read(buffer)
                    if (length > 0) {
                        // Process VPN packets here
                        // This is a basic implementation - in a real VPN,
                        // you would decrypt/encrypt and route packets
                        vpnOutput.write(buffer, 0, length)
                    }
                } catch (e: Exception) {
                    if (isRunning) {
                        Log.e(TAG, "Error processing VPN packets", e)
                    }
                    break
                }
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
    
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN App")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    fun isConnected(): Boolean = isRunning
}
