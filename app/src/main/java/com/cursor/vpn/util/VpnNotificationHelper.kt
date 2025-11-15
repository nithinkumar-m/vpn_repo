package com.cursor.vpn.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cursor.vpn.R
import com.cursor.vpn.core.ConnectionStatus
import com.cursor.vpn.ui.MainActivity

class VpnNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "vpn_channel"
        const val NOTIFICATION_ID = 1337
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        ensureChannel()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.vpn_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.vpn_notification_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(status: ConnectionStatus, sessionName: String): Notification {
        val contentText = when (status) {
            ConnectionStatus.CONNECTING -> context.getString(R.string.vpn_notification_connecting)
            ConnectionStatus.CONNECTED -> context.getString(R.string.vpn_notification_connected)
            ConnectionStatus.ERROR -> context.getString(R.string.vpn_status_error)
            ConnectionStatus.DISCONNECTED -> context.getString(R.string.vpn_notification_disconnected)
        }
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.vpn_notification_title))
            .setContentText(contentText)
            .setOngoing(status == ConnectionStatus.CONNECTED || status == ConnectionStatus.CONNECTING)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$sessionName • $contentText"))
            .build()
    }
}
