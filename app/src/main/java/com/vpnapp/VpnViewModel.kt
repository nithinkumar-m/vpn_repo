package com.vpnapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VpnViewModel : ViewModel() {
    
    private val _connectionState = MutableLiveData<VpnManager.VpnState>(
        VpnManager.VpnState.DISCONNECTED
    )
    val connectionState: LiveData<VpnManager.VpnState> = _connectionState
    
    fun updateState(state: VpnManager.VpnState) {
        _connectionState.value = state
    }
}
