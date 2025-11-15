package com.vpnapp

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class VpnManager(private val context: Context) {
    
    private val _connectionState = MutableLiveData<VpnState>()
    val connectionState: LiveData<VpnState> = _connectionState
    
    enum class VpnState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        ERROR
    }
    
    init {
        _connectionState.value = VpnState.DISCONNECTED
    }
    
    fun connect() {
        _connectionState.value = VpnState.CONNECTING
        
        // Permission already checked, start VPN service
        val serviceIntent = Intent(context, com.vpnapp.VpnService::class.java).apply {
            action = com.vpnapp.VpnService.ACTION_CONNECT
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        
        // Update state after a short delay to allow service to start
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _connectionState.value = VpnState.CONNECTED
        }, 500)
    }
    
    fun disconnect() {
        _connectionState.value = VpnState.DISCONNECTING
        
        val serviceIntent = Intent(context, com.vpnapp.VpnService::class.java).apply {
            action = com.vpnapp.VpnService.ACTION_DISCONNECT
        }
        context.startService(serviceIntent)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _connectionState.value = VpnState.DISCONNECTED
        }, 300)
    }
}
