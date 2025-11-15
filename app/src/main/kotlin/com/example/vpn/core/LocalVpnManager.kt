package com.example.vpn.core

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import com.example.vpn.model.VpnConfig
import com.example.vpn.service.LocalVpnService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalVpnManager(context: Context) {
    private val appContext = context.applicationContext

    private val _status = MutableStateFlow(VpnStatus.DISCONNECTED)
    val status: StateFlow<VpnStatus> = _status.asStateFlow()

    sealed interface Result {
        data object Started : Result
        data object Stopped : Result
        data class PermissionRequired(val intent: Intent) : Result
        data class Error(val message: String, val throwable: Throwable? = null) : Result
    }

    suspend fun connect(config: VpnConfig): Result {
        val prepareIntent = VpnService.prepare(appContext)
        if (prepareIntent != null) {
            _status.value = VpnStatus.PERMISSION_REQUIRED
            return Result.PermissionRequired(prepareIntent)
        }

        return try {
            _status.value = VpnStatus.CONNECTING
            val intent = Intent(appContext, LocalVpnService::class.java).apply {
                action = LocalVpnService.ACTION_CONNECT
                putExtra(LocalVpnService.EXTRA_VPN_CONFIG, config)
            }
            ContextCompat.startForegroundService(appContext, intent)
            _status.value = VpnStatus.CONNECTED
            Result.Started
        } catch (error: Throwable) {
            _status.value = VpnStatus.ERROR
            Result.Error("Unable to start VPN service", error)
        }
    }

    suspend fun disconnect(): Result {
        return try {
            _status.value = VpnStatus.DISCONNECTING
            val intent = Intent(appContext, LocalVpnService::class.java).apply {
                action = LocalVpnService.ACTION_DISCONNECT
            }
            ContextCompat.startForegroundService(appContext, intent)
            _status.value = VpnStatus.DISCONNECTED
            Result.Stopped
        } catch (error: Throwable) {
            _status.value = VpnStatus.ERROR
            Result.Error("Unable to stop VPN service", error)
        }
    }
}
