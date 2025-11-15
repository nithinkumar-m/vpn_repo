package com.example.vpn.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vpn.core.LocalVpnManager
import com.example.vpn.core.VpnStatus
import com.example.vpn.model.VpnConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val vpnManager = LocalVpnManager(application)

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VpnEvent>()
    val events: SharedFlow<VpnEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            vpnManager.status.collectLatest { status ->
                _uiState.update { it.copy(status = status) }
            }
        }
    }

    fun onServerAddressChanged(value: String) {
        _uiState.update { it.copy(form = it.form.copy(serverAddress = value)) }
    }

    fun onServerPortChanged(value: String) {
        _uiState.update { it.copy(form = it.form.copy(serverPort = value.filter { ch -> ch.isDigit() }.take(5))) }
    }

    fun onSharedSecretChanged(value: String) {
        _uiState.update { it.copy(form = it.form.copy(sharedSecret = value)) }
    }

    fun onCredentialsChanged(username: String, password: String) {
        _uiState.update { it.copy(form = it.form.copy(username = username, password = password)) }
    }

    fun connect() {
        val config = _uiState.value.toConfig()
        viewModelScope.launch {
            appendLog("Requesting VPN connection to ${config.serverAddress}:${config.serverPort}")
            when (val result = vpnManager.connect(config)) {
                is LocalVpnManager.Result.PermissionRequired -> {
                    _events.emit(VpnEvent.PermissionRequired(result.intent))
                }
                is LocalVpnManager.Result.Error -> setError(result.message)
                else -> appendLog("VPN service started")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            appendLog("Disconnecting VPN")
            when (val result = vpnManager.disconnect()) {
                is LocalVpnManager.Result.Error -> setError(result.message)
                else -> appendLog("VPN disconnected")
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                appendLog("VPN permission granted")
                connect()
            } else {
                setError("User denied VPN permission")
            }
        }
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(status = VpnStatus.ERROR, lastError = message) }
        appendLog("Error: $message")
    }

    private fun appendLog(message: String) {
        _uiState.update {
            val logs = (listOf(message) + it.logs).take(20)
            it.copy(logs = logs)
        }
    }
}

data class VpnUiState(
    val form: VpnFormState = VpnFormState(),
    val status: VpnStatus = VpnStatus.DISCONNECTED,
    val lastError: String? = null,
    val logs: List<String> = emptyList()
) {
    fun toConfig(): VpnConfig {
        return VpnConfig(
            serverAddress = form.serverAddress.ifBlank { VpnConfig().serverAddress },
            serverPort = form.serverPort.toIntOrNull() ?: VpnConfig().serverPort,
            sharedSecret = form.sharedSecret,
            username = form.username,
            password = form.password
        )
    }
}

data class VpnFormState(
    val serverAddress: String = VpnConfig().serverAddress,
    val serverPort: String = VpnConfig().serverPort.toString(),
    val sharedSecret: String = VpnConfig().sharedSecret,
    val username: String = VpnConfig().username,
    val password: String = VpnConfig().password
)

sealed interface VpnEvent {
    data class PermissionRequired(val intent: Intent) : VpnEvent
}
